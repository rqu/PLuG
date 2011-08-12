package ch.usi.dag.disl.snippet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.exception.StaticAnalysisException;
import ch.usi.dag.disl.snippet.localvars.LocalVars;
import ch.usi.dag.disl.snippet.localvars.SyntheticLocalVar;
import ch.usi.dag.disl.snippet.localvars.ThreadLocalVar;
import ch.usi.dag.disl.util.Constants;
import ch.usi.dag.disl.util.ReflectionHelper;

public class UnprocessedSnippetCode {

	String className;
	String methodName;
	InsnList instructions;
	List<TryCatchBlockNode> tryCatchBlocks;
	Set<String> declaredStaticAnalyses;
	boolean usesDynamicAnalysis;

	public UnprocessedSnippetCode(String className, String methodName,
			InsnList instructions, List<TryCatchBlockNode> tryCatchBlocks,
			Set<String> declaredStaticAnalyses, boolean usesDynamicAnalysis) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.instructions = instructions;
		this.tryCatchBlocks = tryCatchBlocks;
		this.declaredStaticAnalyses = declaredStaticAnalyses;
		this.usesDynamicAnalysis = usesDynamicAnalysis;
	}

	// TODO ! no DiSL exception

	public SnippetCode process(LocalVars allLVs)
			throws StaticAnalysisException, ReflectionException {

		Set<SyntheticLocalVar> slvList = new HashSet<SyntheticLocalVar>();

		Set<ThreadLocalVar> tlvList = new HashSet<ThreadLocalVar>();

		Map<String, Method> staticAnalyses = new HashMap<String, Method>();

		// create list of synthetic local variables
		for (AbstractInsnNode instr : instructions.toArray()) {

			// *** Parse synthetic local variables ***

			SyntheticLocalVar slv = insnUsesField(instr,
					allLVs.getSyntheticLocals());

			if (slv != null) {
				slvList.add(slv);
				continue;
			}

			// *** Parse synthetic local variables ***

			ThreadLocalVar tlv = insnUsesField(instr, allLVs.getThreadLocals());

			if (tlv != null) {
				tlvList.add(tlv);
				continue;
			}

			// *** Parse static analysis methods in use ***

			StaticAnalysisMethod anlMtd = insnInvokesStaticAnalysis(
					declaredStaticAnalyses, instr, staticAnalyses.keySet());

			if (anlMtd != null) {
				staticAnalyses.put(anlMtd.getId(), anlMtd.getRefM());
				continue;
			}
		}

		// TODO ! analysis checking
		// arguments (local variables 1, 2, ...) may be used only in method
		// calls

		// TODO ! dynamic analysis method argument checking
		// values of arguments should be only constant values

		return new SnippetCode(instructions, tryCatchBlocks, slvList, tlvList,
				staticAnalyses, usesDynamicAnalysis);
	}

	/**
	 * Determines if the instruction uses some field defined in
	 * allPossibleFieldNames map. If the field is found in supplied map, the
	 * corresponding mapped object is returned.
	 * 
	 * @param <T>
	 *            type of the return value
	 * @param instr
	 *            instruction to test
	 * @param allPossibleFieldNames
	 *            map with all possible field names as keys
	 * @return object from a map, that corresponds with matched field name
	 */
	private <T> T insnUsesField(AbstractInsnNode instr,
			Map<String, T> allPossibleFieldNames) {

		// check - instruction uses field
		if (!(instr instanceof FieldInsnNode)) {
			return null;
		}

		FieldInsnNode fieldInstr = (FieldInsnNode) instr;

		// get whole name of the field
		String wholeFieldName = fieldInstr.owner + SyntheticLocalVar.NAME_DELIM
				+ fieldInstr.name;

		// check - it is SyntheticLocal variable (it's defined in snippet)
		return allPossibleFieldNames.get(wholeFieldName);
	}

	class StaticAnalysisMethod {

		private String id;
		private Method refM;

		public StaticAnalysisMethod(String id, Method refM) {
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

	private StaticAnalysisMethod insnInvokesStaticAnalysis(
			Set<String> knownStAnClasses, AbstractInsnNode instr,
			Set<String> knownMethods) throws StaticAnalysisException,
			ReflectionException {

		// check - instruction invokes method
		if (!(instr instanceof MethodInsnNode)) {
			return null;
		}

		MethodInsnNode methodInstr = (MethodInsnNode) instr;

		// check - we've found static analysis
		if (!knownStAnClasses.contains(methodInstr.owner)) {
			return null;
		}

		// crate ASM Method object
		org.objectweb.asm.commons.Method asmMethod = new org.objectweb.asm.commons.Method(
				methodInstr.name, methodInstr.desc);

		// check method argument
		// no argument is allowed
		Type[] methodArguments = asmMethod.getArgumentTypes();

		if (methodArguments.length != 0) {
			throw new StaticAnalysisException("Static analysis method "
					+ methodInstr.name + " in the class " + methodInstr.owner
					+ " shouldn't have a parameter.");
		}

		Type methodReturn = asmMethod.getReturnType();

		// only basic types + String are allowed as return type
		if (!(methodReturn.equals(Type.BOOLEAN_TYPE)
				|| methodReturn.equals(Type.BYTE_TYPE)
				|| methodReturn.equals(Type.CHAR_TYPE)
				|| methodReturn.equals(Type.DOUBLE_TYPE)
				|| methodReturn.equals(Type.FLOAT_TYPE)
				|| methodReturn.equals(Type.INT_TYPE)
				|| methodReturn.equals(Type.LONG_TYPE)
				|| methodReturn.equals(Type.SHORT_TYPE) || methodReturn
				.equals(Type.getType(String.class)))) {

			throw new StaticAnalysisException("Static analysis method "
					+ methodInstr.name + " in the class " + methodInstr.owner
					+ " can have only basic type or String as a return type.");
		}

		// crate static analysis method id
		String methodID = methodInstr.owner
				+ Constants.STATIC_ANALYSIS_METHOD_DELIM + methodInstr.name;

		if (knownMethods.contains(methodID)) {
			return null;
		}

		// resolve static analysis class
		Class<?> stAnClass = ReflectionHelper.resolveClass(Type
				.getObjectType(methodInstr.owner));

		Method method = ReflectionHelper.resolveMethod(stAnClass,
				methodInstr.name);

		return new StaticAnalysisMethod(methodID, method);
	}
}