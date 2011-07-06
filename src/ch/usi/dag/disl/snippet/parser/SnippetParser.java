package ch.usi.dag.disl.snippet.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.disl.exception.AnalysisException;
import ch.usi.dag.disl.exception.AnnotParserException;
import ch.usi.dag.disl.exception.DiSLException;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.snippet.marker.Marker;
import ch.usi.dag.disl.snippet.scope.Scope;
import ch.usi.dag.disl.snippet.scope.ScopeImpl;
import ch.usi.dag.disl.snippet.syntheticlocal.SyntheticLocalVar;
import ch.usi.dag.disl.staticinfo.analysis.Analysis;
import ch.usi.dag.disl.staticinfo.analysis.AnalysisInfo;
import ch.usi.dag.disl.util.ClassFactory;
import ch.usi.dag.disl.util.Constants;
import ch.usi.dag.disl.util.InsnListHelper;

/**
 * The parser takes annotated java file as input and creates Snippet classes
 */
public class SnippetParser {

	private final String ANALYSIS_PACKAGE_PREFIX;

	private List<Snippet> snippets = new LinkedList<Snippet>();
	private Map<String, SyntheticLocalVar> syntheticLocalVars =
		new HashMap<String, SyntheticLocalVar>();

	public SnippetParser() {
		super();

		// ANALYSIS_PACKAGE_PREFIX
		// created on the fly so we don't have to care about renaming etc.

		String iAnalysisName = Type.getType(Analysis.class).getInternalName();
		int lastDelim = iAnalysisName
				.lastIndexOf(Constants.INTERNAL_PACKAGE_DELIM);
		ANALYSIS_PACKAGE_PREFIX = iAnalysisName.substring(0, lastDelim);
	}

	public List<Snippet> getSnippets() {

		return snippets;
	}

	public Map<String, SyntheticLocalVar> getSyntheticLocalVars() {

		return syntheticLocalVars;
	}

	public void parse(byte[] classAsBytes) throws DiSLException {

		// NOTE this method can be called many times

		ClassReader cr = new ClassReader(classAsBytes);
		ClassNode classNode = new ClassNode();
		cr.accept(classNode, 0);

		// support for synthetic local
		// - if two synthetic local vars with the same name are defined
		// in different files they will be prefixed with class name as it is
		// also in byte code

		// parse annotations
		Map<String, SyntheticLocalVar> slVars = parseSyntheticLocalVars(
				classNode.name, classNode.fields);

		// get static initialization code
		InsnList origInitCodeIL = null;
		for (Object methodObj : classNode.methods) {

			// cast - ASM still uses Java 1.4 interface
			MethodNode method = (MethodNode) methodObj;

			// get the code
			if (method.name.equals(Constants.STATIC_INIT_NAME)) {
				origInitCodeIL = method.instructions;
				break;
			}
		}

		// parse init code for synthetic local vars and assigns them accordingly
		if (origInitCodeIL != null) {
			parseInitCodeForSLV(origInitCodeIL, slVars);
		}

		// add local vars from this class to others
		syntheticLocalVars.putAll(slVars);

		for (Object methodObj : classNode.methods) {

			// cast - ASM still uses Java 1.4 interface
			MethodNode method = (MethodNode) methodObj;

			// skip the constructor
			if (method.name.equals(Constants.CONSTRUCTOR_NAME)) {
				continue;
			}

			// skip static initializer
			if (method.name.equals(Constants.STATIC_INIT_NAME)) {
				continue;
			}

			snippets.addAll(parseSnippets(classNode.name, method));
		}
	}

	private Map<String, SyntheticLocalVar> parseSyntheticLocalVars(
			String className, List<?> fields) throws AnnotParserException {

		Map<String, SyntheticLocalVar> result =
			new HashMap<String, SyntheticLocalVar>();

		for (Object fieldObj : fields) {

			// cast - ASM still uses Java 1.4 interface
			FieldNode field = (FieldNode) fieldObj;

			if (field.invisibleAnnotations == null) {
				throw new AnnotParserException("DiSL annotation for field "
						+ field.name + " is missing");
			}

			if (field.invisibleAnnotations.size() > 1) {
				throw new AnnotParserException("Field " + field.name
						+ " may have only one anotation");
			}

			AnnotationNode annotation =
				(AnnotationNode) field.invisibleAnnotations.get(0);

			Type annotationType = Type.getType(annotation.desc);

			// check annotation type
			if (!annotationType.equals(Type.getType(SyntheticLocal.class))) {
				throw new AnnotParserException("Field " + field.name
						+ " has unsupported DiSL annotation");
			}

			// check if field is static
			if ((field.access & Opcodes.ACC_STATIC) == 0) {
				throw new AnnotParserException("Field " + field.name
						+ " declared as SyntheticLocal but is not static");
			}

			// add to results
			SyntheticLocalVar slv =
				new SyntheticLocalVar(className, field.name);
			result.put(slv.getID(), slv);
		}

		return result;
	}

	private void parseInitCodeForSLV(InsnList origInitCodeIL,
			Map<String, SyntheticLocalVar> slVars) {

		// first initialization instruction for some field
		AbstractInsnNode firstInitInsn = origInitCodeIL.getFirst();

		for (AbstractInsnNode instr : origInitCodeIL.toArray()) {

			// if our instruction is field
			if (instr instanceof FieldInsnNode) {

				FieldInsnNode fieldInstr = (FieldInsnNode) instr;

				// get whole name of the field
				String wholeFieldName = fieldInstr.owner
						+ SyntheticLocalVar.NAME_DELIM + fieldInstr.name;

				SyntheticLocalVar slv = slVars.get(wholeFieldName);

				if (slv == null) {
					throw new DiSLFatalException(
							"Initialization of static field " + wholeFieldName
									+ " found, but no such field declared");
				}

				// clone part of the asm code
				InsnList initASMCode = InsnListHelper.cloneList(origInitCodeIL,
						firstInitInsn, instr);

				// store the code
				slv.setInitASMCode(initASMCode);

				// prepare first init for next field
				firstInitInsn = instr.getNext();
			}

			// if opcode is return then we are done
			if (InsnListHelper.isReturn(instr.getOpcode())) {
				break;
			}
		}
	}

	protected List<Snippet> parseSnippets(String className, MethodNode method)
			throws DiSLException {

		List<Snippet> result = new LinkedList<Snippet>();

		if (method.invisibleAnnotations == null) {
			throw new AnnotParserException("DiSL anottation for method "
					+ method.name + " is missing");
		}

		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			throw new AnnotParserException("DiSL method " + method.name
					+ " should be declared as static");
		}

		// more annotations on one snippet
		// supported but we will have multiple snippets ;)
		for (Object annotationObj : method.invisibleAnnotations) {

			MethodAnnotationData annotData =
			// cast - ASM still uses Java 1.4 interface
			parseMethodAnnotation((AnnotationNode) annotationObj);

			// if this is unknown annotation
			if (!annotData.isKnown()) {
				throw new AnnotParserException("Method " + method.name
						+ " has unsupported DiSL annotation");
			}

			// marker
			Marker marker = 
				(Marker) ClassFactory.createInstance(annotData.getMarker());

			// scope
			Scope scope = new ScopeImpl(annotData.getScope());

			// process code
			SnippetCodeData scd = processSnippetCode(className,
					method.instructions);

			// whole snippet
			result.add(new Snippet(annotData.getType(), marker, scope,
					annotData.getOrder(), scd.getAsmCode(), scd
							.getReferencedSLV(), scd.getAnalyses()));
		}

		return result;
	}

	// data holder for parseMethodAnnotation methods
	private class MethodAnnotationData {

		private boolean known;
		private Class<?> type;
		private Type marker;
		private String scope;
		private int order;

		public MethodAnnotationData() {
			this.known = false;
		}

		public MethodAnnotationData(Class<?> type, Type marker, String scope,
				int order) {
			super();

			this.known = true;
			this.type = type;
			this.marker = marker;
			this.scope = scope;
			this.order = order;
		}

		public boolean isKnown() {
			return known;
		}

		public Class<?> getType() {
			return type;
		}

		public Type getMarker() {
			return marker;
		}

		public String getScope() {
			return scope;
		}

		public int getOrder() {
			return order;
		}
	}

	protected MethodAnnotationData parseMethodAnnotation(
			AnnotationNode annotation) {

		Type annotationType = Type.getType(annotation.desc);

		// after annotation
		if (annotationType.equals(Type.getType(After.class))) {
			return parseMethodAnnotFields(After.class, annotation,
					annotation.values);
		}

		// after normal execution annotation
		if (annotationType.equals(Type.getType(AfterReturning.class))) {
			return parseMethodAnnotFields(AfterReturning.class, annotation,
					annotation.values);
		}

		// after abnormal execution annotation
		if (annotationType.equals(Type.getType(AfterThrowing.class))) {
			return parseMethodAnnotFields(AfterThrowing.class, annotation,
					annotation.values);
		}

		// before annotation
		if (annotationType.equals(Type.getType(Before.class))) {
			return parseMethodAnnotFields(Before.class, annotation,
					annotation.values);
		}

		// unknown annotation
		return new MethodAnnotationData();
	}

	private MethodAnnotationData parseMethodAnnotFields(Class<?> type,
			AnnotationNode annotation, List<?> annotValues) {

		Iterator<?> it = annotValues.iterator();

		String name = null;
		Type marker = null;
		String scope = null;
		Integer order = null;

		while (it.hasNext()) {

			name = (String) it.next();

			if (name.equals("marker")) {

				marker = (Type) it.next();
				continue;
			}

			if (name.equals("scope")) {

				scope = (String) it.next();
				continue;
			}

			if (name.equals("order")) {

				order = (Integer) it.next();
				continue;
			}

			throw new DiSLFatalException("Unknow field " + name
					+ " in annotation " + type.toString()
					+ ". This may happen if annotation class is changed but"
					+ " parser is not.");
		}

		if (marker == null || scope == null || order == null) {

			throw new DiSLFatalException("Missing field in annotation "
					+ type.toString()
					+ ". This may happen if annotation class is changed but"
					+ " parser is not.");
		}

		return new MethodAnnotationData(type, marker, scope, order);
	}

	private class SnippetCodeData {

		private InsnList asmCode;
		private Set<SyntheticLocalVar> referencedSLV;
		private Map<String, Method> analyses;

		public SnippetCodeData(InsnList asmCode,
				Set<SyntheticLocalVar> referencedSLV,
				Map<String, Method> analyses) {
			super();
			this.asmCode = asmCode;
			this.referencedSLV = referencedSLV;
			this.analyses = analyses;
		}

		public InsnList getAsmCode() {
			return asmCode;
		}

		public Set<SyntheticLocalVar> getReferencedSLV() {
			return referencedSLV;
		}

		public Map<String, Method> getAnalyses() {
			return analyses;
		}
	}

	private SnippetCodeData processSnippetCode(String className,
			InsnList snippetCode) throws DiSLException {

		// clone the instrucition list
		InsnList asmCode = InsnListHelper.cloneList(snippetCode);

		// detect empty stippets
		if (InsnListHelper.containsOnlyReturn(asmCode)) {
			return new SnippetCodeData(null, null, null);
		}

		// remove returns in snippet (in asm code)
		InsnListHelper.removeReturns(asmCode);

		Set<SyntheticLocalVar> slvList = new HashSet<SyntheticLocalVar>();

		Map<String, Method> analyses = new HashMap<String, Method>();

		// create list of synthetic local variables
		for (AbstractInsnNode instr : asmCode.toArray()) {

			// *** Parse synthetic local variables ***

			String slvName = insnUsesSLV(instr, className);
			
			if(slvName != null) {
				slvList.add(syntheticLocalVars.get(slvName));
			}

			// *** Parse analysis classes in use ***

			AnalysisMethod anlMtd = 
				insnInvokesAnalysis(instr, analyses.keySet());
			
			if(anlMtd != null) {
				analyses.put(anlMtd.getId(), anlMtd.getRefM());
			}
		}

		return new SnippetCodeData(asmCode, slvList, analyses);
	}

	private String insnUsesSLV(AbstractInsnNode instr, String className) {

		// check - instruction uses field
		if (! (instr instanceof FieldInsnNode)) {
			return null;
		}
		
		FieldInsnNode fieldInstr = (FieldInsnNode) instr;

		// check - it is SyntheticLocal variable (it's defined in snippet)
		if (! className.equals(fieldInstr.owner)) {
			return null;
		}
		
		// get whole name of the field
		String wholeFieldName = fieldInstr.owner
				+ SyntheticLocalVar.NAME_DELIM + fieldInstr.name;
		
		return wholeFieldName;
	}
	
	class AnalysisMethod {
		
		private String id;
		private Method refM;
		
		public AnalysisMethod(String id, Method refM) {
			super();
			this.id = id;
			this.refM = refM;
		}

		public String getId() {
			return id;
		}

		public Method getRefM() {
			return refM;
		}
	}
	
	private AnalysisMethod insnInvokesAnalysis(AbstractInsnNode instr,
			Set<String> knownMethods) throws AnalysisException, DiSLException {
		
		// check - instruction invokes method
		if (! (instr instanceof MethodInsnNode)) {
			return null;
		}

		MethodInsnNode methodInstr = (MethodInsnNode) instr;

		// check - we've found analysis
		if (! methodInstr.owner.startsWith(ANALYSIS_PACKAGE_PREFIX)) {
			return null;
		}

		// crate ASM Method object
		org.objectweb.asm.commons.Method asmMethod = 
			new org.objectweb.asm.commons.Method(
					methodInstr.name, methodInstr.desc);

		// check method argument
		// only one method argument (AnalysisInfo) is allowed
		Type[] methodArguments = asmMethod.getArgumentTypes();
		
		if(methodArguments.length != 1) {
			throw new AnalysisException("Analysis method " + methodInstr.name
					+ " in class " + methodInstr.owner
					+ " should have only one parameter.");
		}
		
		if(! methodArguments[0].equals(Type.getType(AnalysisInfo.class))) {
			throw new AnalysisException("Parameter in alysis method "
					+ methodInstr.name + " in class " + methodInstr.owner
					+ " should be of type "
					+ AnalysisInfo.class.getCanonicalName());
		}
		
		// crate analysis method id
		String methodID = methodInstr.owner + Constants.ANALYSIS_METHOD_DELIM
				+ methodInstr.name;

		if(knownMethods.contains(methodID)) {
			return null;
		}
		
		// resolve analysis class
		Class<?> analysisClass = ClassFactory.resolve(
				Type.getObjectType(methodInstr.owner));

		// NOTE: we don't check if the class is implementing
		// Analysis interface - implementation is not mandatory

		// resolve method
		Method method = null;
		try {
			method = analysisClass.getMethod(
					methodInstr.name, AnalysisInfo.class);
		}
		catch(NoSuchMethodException e) {
			throw new AnalysisException(
					"Method " + methodInstr.name + " in class "
					+ methodInstr.owner + " cannot be found."
					+ " Snippet was probably compiled against modified"
					+ " (different) class");
		}
		
		// check if the method is static
		if(! Modifier.isStatic(method.getModifiers())) {
			throw new AnalysisException(
					"Analysis method " + methodInstr.name + " in class "
					+ methodInstr.owner + " should be static.");
		}
		
		return new AnalysisMethod(methodID, method);
	}
}
