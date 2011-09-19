package ch.usi.dag.disl.staticinfo.analysis;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.dislclass.snippet.Snippet;
import ch.usi.dag.disl.dislclass.snippet.marker.MarkedRegion;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

public class BasicBlockAnalysis extends AbstractStaticAnalysis {

	public BasicBlockAnalysis() {
		super();
	}

	public BasicBlockAnalysis(ClassNode classNode, MethodNode methodNode,
			Snippet snippet, MarkedRegion markedRegion) {
		super(classNode, methodNode, snippet, markedRegion);
	}

	public int getBBSize() {

		int count = 1;
		AbstractInsnNode start;
		List<AbstractInsnNode> ends;

		start = staticAnalysisData.getMarkedRegion().getStart();
		ends = staticAnalysisData.getMarkedRegion().getEnds();

		while (!ends.contains(start)) {
			
			if (start.getOpcode() != 1) {
				count++;
			}
			
			start = start.getNext();
		}

		return count;
	}

	public int getBBindex() {

		CtrlFlowGraph cfg = new CtrlFlowGraph(
				staticAnalysisData.getMethodNode());
		return cfg.getIndex(staticAnalysisData.getMarkedRegion().getStart());
	}
	
	public boolean isFirstOfLoop() {

		CtrlFlowGraph cfg = CtrlFlowGraph.build(staticAnalysisData
				.getMethodNode());
		return cfg.getBB(staticAnalysisData.getMarkedRegion().getStart())
				.isLoop();
	}
}
