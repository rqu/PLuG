package ch.usi.dag.disl.snippet;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Holds information about a region where a snippet will be woven. The shadow
 * contains two type of regions. A logical region, which is available directly
 * in the shadow, is designed mainly for static analysis and represents a region
 * of bytecode which is logically captured by a marker. In contrast, a weaving
 * region is designed as a guidance for the weaver, and indicates where exactly
 * should the code be woven.
 */
public final class Shadow {

    protected final ClassNode classNode;
    protected final MethodNode methodNode;

    protected final Snippet snippet;

    private final AbstractInsnNode regionStart;
    private final List <AbstractInsnNode> regionEnds;

    private final WeavingRegion weavingRegion;


    /**
     * Holds exact information where the code will be woven. This structure is
     * a guiding source for the weaver.
     */
    public static class WeavingRegion {

        // NOTE: "ends" can be null. This means, that we have the special case
        // where we need to generate before and after snippets on the same
        // position.
        // This is for example case of putting snippets before and after
        // region that includes only return instruction.
        // In this case, after has to be generated also before the return
        // instruction otherwise is never invoked.
        // "ends" containing null notifies the weaver about this situation.

        private AbstractInsnNode start;

        private List<AbstractInsnNode> ends;

        private AbstractInsnNode afterThrowStart;
        private AbstractInsnNode afterThrowEnd;


        public WeavingRegion (
            final AbstractInsnNode start, final List <AbstractInsnNode> ends,
            final AbstractInsnNode afterThrowStart, final AbstractInsnNode afterThrowEnd
        ) {
            super ();
            this.start = start;
            this.ends = ends;
            this.afterThrowStart = afterThrowStart;
            this.afterThrowEnd = afterThrowEnd;
        }


        public AbstractInsnNode getStart() {
            return start;
        }

        public List<AbstractInsnNode> getEnds() {
            return ends;
        }

        public AbstractInsnNode getAfterThrowStart() {
            return afterThrowStart;
        }

        public AbstractInsnNode getAfterThrowEnd() {
            return afterThrowEnd;
        }

        public void setStart(final AbstractInsnNode start) {
            this.start = start;
        }

        public void setEnds(final List<AbstractInsnNode> ends) {
            this.ends = ends;
        }

        public void setAfterThrowStart(final AbstractInsnNode afterThrowStart) {
            this.afterThrowStart = afterThrowStart;
        }

        public void setAfterThrowEnd(final AbstractInsnNode afterThrowEnd) {
            this.afterThrowEnd = afterThrowEnd;
        }

    }


    public Shadow (
        final ClassNode classNode, final MethodNode methodNode, final Snippet snippet,
        final AbstractInsnNode regionStart, final List <AbstractInsnNode> regionEnds,
        final WeavingRegion weavingRegion
    ) {
        super ();
        this.classNode = classNode;
        this.methodNode = methodNode;
        this.snippet = snippet;
        this.regionStart = regionStart;
        this.regionEnds = regionEnds;
        this.weavingRegion = weavingRegion;
    }


    // special copy constructor for caching support
    public Shadow (final Shadow that) {
        this.classNode = that.classNode;
        this.methodNode = that.methodNode;
        this.snippet = that.snippet;
        this.regionStart = that.regionStart;
        this.regionEnds = that.regionEnds;
        this.weavingRegion = that.weavingRegion;
    }


    /**
     * Returns class node of the class where the shadow is defined.
     */
    public ClassNode getClassNode() {
        return classNode;
    }

    /**
     * Returns method node of the method where the shadow is defined.
     */
    public MethodNode getMethodNode() {
        return methodNode;
    }

    /**
     * Returns snippet that will be woven.
     */
    public Snippet getSnippet() {
        return snippet;
    }

    /**
     * Returns region start (this region is designed for static analysis).
     */
    public AbstractInsnNode getRegionStart() {
        return regionStart;
    }

    /**
     * Returns region ends (this region is designed for static analysis).
     */
    public List<AbstractInsnNode> getRegionEnds() {
        return regionEnds;
    }

    /**
     * Returns weaving region (this region is designed for weaver).
     */
    public WeavingRegion getWeavingRegion() {
        return weavingRegion;
    }
}
