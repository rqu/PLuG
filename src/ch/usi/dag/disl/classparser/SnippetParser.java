package ch.usi.dag.disl.classparser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.MarkerException;
import ch.usi.dag.disl.exception.ParserException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.exception.ScopeParserException;
import ch.usi.dag.disl.exception.SnippetParserException;
import ch.usi.dag.disl.exception.StaticContextGenException;
import ch.usi.dag.disl.guard.SnippetGuard;
import ch.usi.dag.disl.marker.Marker;
import ch.usi.dag.disl.marker.Parameter;
import ch.usi.dag.disl.scope.Scope;
import ch.usi.dag.disl.scope.ScopeImpl;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.snippet.SnippetUnprocessedCode;
import ch.usi.dag.disl.staticcontext.StaticContext;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.Constants;
import ch.usi.dag.disl.util.ReflectionHelper;

/**
 * The parser takes annotated java file as input and creates Snippet classes
 */
public class SnippetParser extends AbstractParser {

	private List<Snippet> snippets = new LinkedList<Snippet>();

	public List<Snippet> getSnippets() {

		return snippets;
	}
	
	public void parse(ClassNode classNode) throws ParserException,
			SnippetParserException, ReflectionException, ScopeParserException,
			StaticContextGenException, MarkerException {

		// NOTE: this method can be called many times

		// process local variables
		processLocalVars(classNode);

		for (MethodNode method : classNode.methods) {

			// skip the constructor
			if (method.name.equals(Constants.CONSTRUCTOR_NAME)) {
				continue;
			}

			// skip static initializer
			if (method.name.equals(Constants.STATIC_INIT_NAME)) {
				continue;
			}

			snippets.add(parseSnippet(classNode.name, method));
		}
	}
	
	// parse snippet
	private Snippet parseSnippet(String className, MethodNode method)
			throws SnippetParserException, ReflectionException,
			ScopeParserException, StaticContextGenException, MarkerException {

		// check annotation
		if (method.invisibleAnnotations == null) {
			throw new SnippetParserException("DiSL anottation for method "
					+ className + "." + method.name + " is missing");
		}

		// check only one annotation
		if (method.invisibleAnnotations.size() > 1) {
			throw new SnippetParserException("Method " + className + "."
					+ method.name + " can have only one DiSL anottation");
		}

		// check static
		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			throw new SnippetParserException("Method " + className + "."
					+ method.name + " should be declared as static");
		}

		// check return type
		if (!Type.getReturnType(method.desc).equals(Type.VOID_TYPE)) {
			throw new SnippetParserException("Method " + className + "."
					+ method.name + " cannot return value");
		}
		
		// no exception can be thrown
		if(! method.exceptions.isEmpty()) {
			throw new SnippetParserException("Method " + className + "."
					+ method.name + " cannot throw any exception");
		}

		AnnotationNode annotation = method.invisibleAnnotations.get(0);

		SnippetAnnotationData annotData = 
			parseMethodAnnotation(className + "." + method.name, annotation);

		// ** marker **
		Marker marker = 
			getMarker(annotData.marker, annotData.args);

		// ** scope **
		Scope scope = new ScopeImpl(annotData.scope);

		// ** guard **
		SnippetGuard guard = 
			(SnippetGuard) ParserHelper.getGuard(annotData.guard);
		
		// ** parse used static and dynamic context **
		Context context = parseContext(method.desc);

		// ** checks **

		// detect empty snippets
		if (AsmHelper.containsOnlyReturn(method.instructions)) {
			throw new SnippetParserException("Method " + className + "."
					+ method.name + " cannot be empty");
		}

		// context arguments (local variables 1, 2, ...) cannot be stored or
		// overwritten, may be used only in method calls
		usesContextProperly(className, method.name, method.desc,
				method.instructions);

		// ** create unprocessed code holder class **
		// code is processed after everything is parsed
		SnippetUnprocessedCode uscd = new SnippetUnprocessedCode(className,
				method.name, method.instructions, method.tryCatchBlocks,
				context.getStaticContexts(), context.usesDynamicContext(),
				annotData.dynamicBypass);

		// return whole snippet
		return new Snippet(className, method.name, annotData.type, marker,
				scope, guard, annotData.order, uscd);
	}

	private SnippetAnnotationData parseMethodAnnotation(String fullMethodName,
			AnnotationNode annotation) throws SnippetParserException {

		Type annotationType = Type.getType(annotation.desc);

		// after annotation
		if (annotationType.equals(Type.getType(After.class))) {
			return parseMethodAnnotFields(After.class, annotation);
		}

		// after normal execution annotation
		if (annotationType.equals(Type.getType(AfterReturning.class))) {
			return parseMethodAnnotFields(AfterReturning.class, annotation);
		}

		// after abnormal execution annotation
		if (annotationType.equals(Type.getType(AfterThrowing.class))) {
			return parseMethodAnnotFields(AfterThrowing.class, annotation);
		}

		// before annotation
		if (annotationType.equals(Type.getType(Before.class))) {
			return parseMethodAnnotFields(Before.class, annotation);
		}

		// unknown annotation
		throw new SnippetParserException("Method " + fullMethodName
				+ " has unsupported DiSL annotation");
	}

	// data holder for AnnotationParser
	private static class SnippetAnnotationData {

		public Class<?> type;
		
		// annotation values
		public Type marker = null;
		public String args = null; // default
		public String scope = null;
		public Type guard = null; // default
		public int order = 100; // default
		public boolean dynamicBypass = false; // default
		
		public SnippetAnnotationData(Class<?> type) {
			this.type = type;
		}
	}
	
	private SnippetAnnotationData parseMethodAnnotFields(Class<?> type,
			AnnotationNode annotation) {

		SnippetAnnotationData sad = new SnippetAnnotationData(type);
		ParserHelper.parseAnnotation(sad, annotation);
		
		if (sad.marker == null || sad.scope == null) {

			throw new DiSLFatalException("Missing attribute in annotation "
					+ type.toString()
					+ ". This may happen if annotation class is changed but"
					+ " data holder class is not.");
		}

		return sad;
	}
	
	private Marker getMarker(Type markerType, String markerParam)
			throws ReflectionException, MarkerException {
		
		// get marker class
		Class<?> markerClass = ReflectionHelper.resolveClass(markerType);

		// instantiate marker WITHOUT Parameter as an argument
		if(markerParam == null) {
			try {
				return (Marker) ReflectionHelper.createInstance(markerClass);
			}
			catch(ReflectionException e) {
				
				if(e.getCause() instanceof NoSuchMethodException) {
					throw new MarkerException("Marker " + markerClass.getName()
							+ " requires \"param\" annotation attribute"
							+ " declared",
							e);
				}
				
				throw e;
			}
		}

		// try to instantiate marker WITH Parameter as an argument
		try {
			return (Marker) ReflectionHelper.createInstance(markerClass,
					new Parameter(markerParam));
		}
		catch(ReflectionException e) {
			
			if(e.getCause() instanceof NoSuchMethodException) {
				throw new MarkerException("Marker " + markerClass.getName()
						+ " does not support \"param\" attribute", e);
			}
			
			throw e;
		}
	}

	private static class Context {

		private Set<String> staticContexts;
		private boolean usesDynamicContext;

		public Context(Set<String> staticContexts, boolean usesDynamicContext) {
			super();
			this.staticContexts = staticContexts;
			this.usesDynamicContext = usesDynamicContext;
		}

		public Set<String> getStaticContexts() {
			return staticContexts;
		}

		public boolean usesDynamicContext() {
			return usesDynamicContext;
		}
	}

	private Context parseContext(String methodDesc)
			throws ReflectionException, StaticContextGenException {

		Set<String> knownStCo = new HashSet<String>();
		boolean usesDynamicContext = false;

		for (Type argType : Type.getArgumentTypes(methodDesc)) {

			// skip dynamic context class - don't check anything
			if (argType.equals(Type.getType(DynamicContext.class))) {
				usesDynamicContext = true;
				continue;
			}

			Class<?> argClass = ReflectionHelper.resolveClass(argType);

			// static context should implement context interface
			if (!implementsStaticContext(argClass)) {
				throw new StaticContextGenException(argClass.getName()
						+ " does not implement StaticContext interface and"
						+ " cannot be used as advice method parameter");
			}

			knownStCo.add(argType.getInternalName());
		}

		return new Context(knownStCo, usesDynamicContext);
	}

	/**
	 * Searches for StaticContext interface. Searches through whole class
	 * hierarchy.
	 * 
	 * @param classToSearch
	 */
	private boolean implementsStaticContext(Class<?> classToSearch) {

		// through whole hierarchy...
		while (classToSearch != null) {

			// ...through all interfaces...
			for (Class<?> iface : classToSearch.getInterfaces()) {

				// ...search for StaticContext interface
				if (iface.equals(StaticContext.class)) {
					return true;
				}
			}

			classToSearch = classToSearch.getSuperclass();
		}

		return false;
	}

	private void usesContextProperly(String className, String methodName,
			String methodDescriptor, InsnList instructions)
			throws SnippetParserException {

		Type[] types = Type.getArgumentTypes(methodDescriptor);
		int maxArgIndex = 0;

		// count the max index of arguments
		for (int i = 0; i < types.length; i++) {

			// add number of occupied slots
			maxArgIndex += types[i].getSize();
		}

		// The following code assumes that all disl advices are static
		for (AbstractInsnNode instr : instructions.toArray()) {

			switch (instr.getOpcode()) {
			// test if the context is stored somewhere else
			case Opcodes.ALOAD: {

				int local = ((VarInsnNode) instr).var;

				if (local < maxArgIndex
						&& instr.getNext().getOpcode() == Opcodes.ASTORE) {
					throw new SnippetParserException("In advice " + className
							+ "." + methodName + " - advice parameter"
							+ " (context) cannot be stored into local"
							+ " variable");
				}

				break;
			}
				// test if something is stored in the context
			case Opcodes.ASTORE: {

				int local = ((VarInsnNode) instr).var;

				if (local < maxArgIndex) {
					throw new SnippetParserException("In advice " + className
							+ "." + methodName + " - advice parameter"
							+ " (context) cannot overwritten");
				}

				break;
			}
			}
		}
	}
}