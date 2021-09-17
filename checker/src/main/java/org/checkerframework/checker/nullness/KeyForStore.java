package org.checkerframework.checker.nullness;

import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAbstractStore;

public class KeyForStore extends CFAbstractStore<KeyForValue, KeyForStore> {
    public KeyForStore(
            CFAbstractAnalysis<KeyForValue, KeyForStore, ?> analysis, boolean sequentialSemantics) {
        this(analysis, sequentialSemantics, false);
    }

    public KeyForStore(
            CFAbstractAnalysis<KeyForValue, KeyForStore, ?> analysis,
            boolean sequentialSemantics,
            boolean isBottom) {
        super(analysis, sequentialSemantics, isBottom);
    }

    protected KeyForStore(CFAbstractStore<KeyForValue, KeyForStore> other) {
        super(other);
    }
}
