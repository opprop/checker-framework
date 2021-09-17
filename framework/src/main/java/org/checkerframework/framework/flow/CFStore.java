package org.checkerframework.framework.flow;

/** The default store used in the Checker Framework. */
public class CFStore extends CFAbstractStore<CFValue, CFStore> {

    public CFStore(CFAbstractAnalysis<CFValue, CFStore, ?> analysis, boolean sequentialSemantics) {
        this(analysis, sequentialSemantics, false);
    }

    public CFStore(
            CFAbstractAnalysis<CFValue, CFStore, ?> analysis,
            boolean sequentialSemantics,
            boolean isBottom) {
        super(analysis, sequentialSemantics, isBottom);
    }

    /**
     * Copy constructor.
     *
     * @param other the CFStore to copy
     */
    public CFStore(CFAbstractStore<CFValue, CFStore> other) {
        super(other);
    }
}
