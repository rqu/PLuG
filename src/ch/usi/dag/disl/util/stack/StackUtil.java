package ch.usi.dag.disl.util.stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

public class StackUtil {

	// generate a basic analyzer
	public static Analyzer<BasicValue> getBasicAnalyzer() {
		return new Analyzer<BasicValue>(new BasicVerifier());
	}

	// calculate the stack size
	public static int getOffset(Frame<BasicValue> frame) {
		int offset = 0;

		for (int i = frame.getStackSize() - 1; i >= 0; i--) {

			BasicValue v = frame.getStack(i);
			offset += v.getSize();
		}

		return offset;
	}

	// generate an instruction list to backup the stack
	public static InsnList enter(Frame<BasicValue> frame, int offset) {

		InsnList ilst = new InsnList();

		for (int i = frame.getStackSize() - 1; i >= 0; i--) {

			BasicValue v = frame.getStack(i);

			ilst.add(new VarInsnNode(v.getType().getOpcode(Opcodes.ISTORE),
					offset));
			offset += v.getSize();
		}

		return ilst;
	}

	// generate an instruction list to restore the stack
	public static InsnList exit(Frame<BasicValue> frame, int offset) {
		InsnList ilst = new InsnList();
		ilst.add(new LabelNode());

		for (int i = frame.getStackSize() - 1; i >= 0; i--) {

			BasicValue v = frame.getStack(i);

			ilst.insertBefore(ilst.getFirst(), new VarInsnNode(v.getType()
					.getOpcode(Opcodes.ILOAD), offset));
			offset += v.getSize();
		}

		return ilst;
	}

	public static BasicValue getBasicValue(Frame<BasicValue> frame, int depth) {
		return frame.getStack(frame.getStackSize() - 1 - depth);
	}

	// generate a source analyzer
	public static Analyzer<SourceValue> getSourceAnalyzer() {
		return new Analyzer<SourceValue>(new SourceInterpreter());
	}

	public static SourceValue getSourceValue(Frame<SourceValue> frame, int depth) {
		return frame.getStack(frame.getStackSize() - 1 - depth);
	}

}
