package ch.usi.dag.disl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.dislclass.loader.ClassByteLoader;
import ch.usi.dag.disl.dislclass.localvar.SyntheticLocalVar;
import ch.usi.dag.disl.dislclass.parser.ClassParser;
import ch.usi.dag.disl.dislclass.processor.Proc;
import ch.usi.dag.disl.dislclass.snippet.Snippet;
import ch.usi.dag.disl.dislclass.snippet.marker.MarkedRegion;
import ch.usi.dag.disl.exception.DiSLException;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.InitException;
import ch.usi.dag.disl.exception.ProcessorException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.exception.StaticInfoException;
import ch.usi.dag.disl.guard.SnippetGuard;
import ch.usi.dag.disl.processor.generator.PIResolver;
import ch.usi.dag.disl.processor.generator.ProcGenerator;
import ch.usi.dag.disl.processor.generator.ProcInstance;
import ch.usi.dag.disl.processor.generator.ProcMethodInstance;
import ch.usi.dag.disl.staticinfo.StaticInfo;
import ch.usi.dag.disl.weaver.Weaver;
import ch.usi.dag.jborat.agent.Instrumentation;

// TODO better public API - marker pkg should be in disl pkg, class visibility (and pkg) cleanup everywhere
//  - maybe expose classes in user package and other are considered non visible :)
// TODO javadoc comment all
public class DiSL implements Instrumentation {

	final String PROP_DYNAMIC_BYPASS = "disl.dynbypass";
	final boolean allDynamicBypass = Boolean.getBoolean(PROP_DYNAMIC_BYPASS);
	
	final String PROP_DEBUG = "disl.debug";
	final boolean debug = Boolean.getBoolean(PROP_DEBUG);

	List<Snippet> snippets;

	// list of static analysis instances
	// validity of an instance is for one instrumented class
	// instances are created lazily when needed in StaticInfo
	Map<Class<?>, Object> staticAnalysisInstances;

	private void reportError(Exception e) {

		// error report for user (input) errors
		System.err.println("DiSL error: " + e.getMessage());
		Throwable cause = e.getCause();
		while (cause != null) {
			System.err.println("  Inner error: " + cause.getMessage());
			cause = cause.getCause();
		}
	}
	
	// this method should be called only once
	public synchronized void initialize() throws Exception {

		if(snippets != null) {
			throw new DiSLFatalException("DiSL cannot be initialized twice");
		}
		
		// report every exception within our code - don't let anyone mask it
		try {

			List<InputStream> dislClasses = ClassByteLoader.loadDiSLClasses();
			
			if(dislClasses == null) {
				throw new InitException("Cannot load DiSL classes. Please set" +
						" the property " + ClassByteLoader.PROP_DISL_CLASSES
						+ " or supply jar with DiSL classes"
						+ " and proper manifest");
			}

			// *** parse disl classes ***
			// - create snippets
			// - create static analysis methods

			ClassParser parser = new ClassParser();

			for (InputStream classIS : dislClasses) {
				parser.parse(classIS);
			}

			// initialize processors
			Map<Type, Proc> processors = parser.getProcessors();
			for (Proc processor : processors.values()) {
				processor.init(parser.getAllLocalVars());
			}

			List<Snippet> parsedSnippets = parser.getSnippets();

			// initialize snippets
			for (Snippet snippet : parsedSnippets) {
				snippet.init(parser.getAllLocalVars(), processors,
						allDynamicBypass);
			}
			
			// initialize snippets variable
			//  - this is set when everything is ok
			//  - it serves as initialization flag
			snippets = parsedSnippets;

			// TODO put checker here
			// like After should catch normal and abnormal execution
			// but if you are using After (AfterThrowing) with BasicBlockMarker
			// or InstructionMarker that doesn't throw exception, then it is
			// probably something, you don't want - so just warn the user
			// also it can warn about unknown opcodes if you let user to
			// specify this for InstructionMarker
		}
		catch (Exception e) {
			
			reportError(e);

			if(debug) {
				e.printStackTrace();
			}
			
			// signal error
			throw e;
		}
	}
	
	/**
	 * Instruments a method in a class.
	 * 
	 * NOTE: This method changes the classNode argument
	 * 
	 * @param classNode
	 *            class that will be instrumented
	 * @param methodNode
	 *            method in the classNode argument, that will be instrumented
	 */
	private boolean instrumentMethod(ClassNode classNode, MethodNode methodNode)
			throws ReflectionException, StaticInfoException,
			ProcessorException {

		// skip abstract methods
		if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) {
			return false;
		}
		
		// skip native methods
		if ((methodNode.access & Opcodes.ACC_NATIVE) != 0) {
			return false;
		}
		
		// *** match snippet scope ***

		List<Snippet> matchedSnippets = new LinkedList<Snippet>();

		for (Snippet snippet : snippets) {

			// snippet matching
			if (snippet.getScope().matches(classNode.name, methodNode)) {
				matchedSnippets.add(snippet);
			}
		}

		// if there is nothing to instrument -> quit
		// just to be faster out
		if (matchedSnippets.isEmpty()) {
			return false;
		}

		// *** create markings ***

		// markings according to snippets for viewing
		Map<Snippet, List<MarkedRegion>> snippetMarkings =
			new HashMap<Snippet, List<MarkedRegion>>();

		for (Snippet snippet : matchedSnippets) {

			// marking
			List<MarkedRegion> marking = snippet.getMarker().mark(methodNode);

			// select markings according to snippet guard
			List<MarkedRegion> selectedMarking =
				selectMarkingWithGuard(snippet, marking, classNode, methodNode);
			
			// add to map
			if(! selectedMarking.isEmpty()) {
				snippetMarkings.put(snippet, selectedMarking);
			}
		}
		
		// *** compute static info ***

		// prepares StaticInfo class (computes static analysis)
		StaticInfo staticInfo = new StaticInfo(staticAnalysisInstances,
				classNode, methodNode, snippetMarkings);

		// *** used synthetic local vars in snippets ***
		// weaver needs list of synthetic locals that are actively used in
		// selected (matched) snippets

		Set<SyntheticLocalVar> usedSLVs = new HashSet<SyntheticLocalVar>();
		for (Snippet snippet : snippetMarkings.keySet()) {
			usedSLVs.addAll(snippet.getCode().getReferencedSLVs());
		}

		// *** prepare processors ***

		PIResolver piResolver = new ProcGenerator().compute(classNode,
				methodNode, snippetMarkings);

		// *** used synthetic local vars in processors ***
		
		// include SLVs from processor methods into usedSLV
		for(ProcInstance pi : piResolver.getAllProcInstances()) {
			
			for(ProcMethodInstance pmi : pi.getMethods()) {
				
				usedSLVs.addAll(pmi.getCode().getReferencedSLVs());
			}
		}

		// *** viewing ***

		Weaver.instrument(classNode, methodNode, snippetMarkings,
				new LinkedList<SyntheticLocalVar>(usedSLVs), staticInfo,
				piResolver);

		if(debug) {
			System.out.println("--- instumentation of " + classNode.name + "."
					+ methodNode.name);
		}
		
		return true;
	}

	private List<MarkedRegion> selectMarkingWithGuard(Snippet snippet,
			List<MarkedRegion> marking,
			ClassNode classNode,
			MethodNode methodNode) {
		
		SnippetGuard guard = snippet.getGuard();
		
		if(guard == null) {
			return marking;
		}

		List<MarkedRegion> selectedMarking = new LinkedList<MarkedRegion>();

		// check guard for each marking
		for(MarkedRegion markedRegion : marking) {

			if(guard.isApplicable(classNode, methodNode, snippet, 
					markedRegion)) {

				selectedMarking.add(markedRegion);
			}
		}
		
		return selectedMarking;
	}

	// this method is thread safe after initialization
	private boolean instrument(ClassNode classNode) throws DiSLException {

		if (snippets == null) {
			throw new DiSLFatalException("DiSL was not initialized");
		}
		
		boolean classChanged = false;
		
		// report every exception within our code - don't let anyone mask it
		try {

			// list of static analysis instances
			// validity of an instance is for one instrumented class
			staticAnalysisInstances = new HashMap<Class<?>, Object>();

			// instrument all methods in a class
			for (MethodNode methodNode : classNode.methods) {

				boolean methodChanged = instrumentMethod(classNode, methodNode);
				
				classChanged = classChanged || methodChanged;
			}

		}
		catch (DiSLException e) {
			
			reportError(e);

			if(debug) {
				e.printStackTrace();
			}
			
			// signal error
			throw e;
		}
		
		return classChanged;
	}

	private byte[] instrument(ClassReader classReader) throws DiSLException {
	
		// AfterInitBodyMarker uses AdviceAdapter
		//  - classNode with API param is required by ASM 4.0 guidelines
		ClassNode classNode = new ClassNode(Opcodes.ASM4);
		classReader.accept(classNode, ClassReader.SKIP_DEBUG
				| ClassReader.EXPAND_FRAMES);
		
		
		if(instrument(classNode)) {
		
			// DiSL uses some instructions available only in higher versions
			final int REQUIRED_VERSION = Opcodes.V1_5;
			final int MAJOR_V_MASK = 0xFFFF;
			
			int requiredMajorVersion = REQUIRED_VERSION & MAJOR_V_MASK;
			int classMajorVersion = classNode.version & MAJOR_V_MASK;
			
			if (classMajorVersion < requiredMajorVersion) {
				classNode.version = REQUIRED_VERSION;
			}
			
			// return as bytes
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(cw);
			return cw.toByteArray();
		}
		
		return null;
	}
	
	public byte[] instrument(byte[] classAsBytes) throws Exception {

		ClassReader cr = new ClassReader(classAsBytes);

		return instrument(cr);
	}
	
	public byte[] instrument(InputStream classAsStream) throws Exception {
		
    	ClassReader cr = new ClassReader(classAsStream);
    	
    	return instrument(cr);
	}
}
