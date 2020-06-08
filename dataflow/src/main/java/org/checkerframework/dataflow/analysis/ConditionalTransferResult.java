package org.checkerframework.dataflow.analysis;

import java.util.Map;
import java.util.StringJoiner;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implementation of a {@link TransferResult} with two non-exceptional store; one for the 'then'
 * edge and one for 'else'. The result of {@code getRegularStore} will be the least upper bound of
 * the two underlying stores.
 *
 * @param <S> the {@link Store} used to keep track of intermediate results
 */
public class ConditionalTransferResult<A extends AbstractValue<A>, S extends Store<S>>
        extends TransferResult<A, S> {

    /** Whether the store changed. */
    private final boolean storeChanged;

    /** The 'then' result store. */
    protected final StoreSet<S> thenStore;

    /** The 'else' result store. */
    protected final StoreSet<S> elseStore;

    /**
     * <em>Exceptions</em>: If the corresponding {@link org.checkerframework.dataflow.cfg.node.Node}
     * throws an exception, then it is assumed that no special handling is necessary and the store
     * before the corresponding {@link org.checkerframework.dataflow.cfg.node.Node} will be passed
     * along any exceptional edge.
     *
     * <p><em>Aliasing</em>: {@code thenStore} and {@code elseStore} are not allowed to be used
     * anywhere outside of this class (including use through aliases). Complete control over the
     * objects is transfered to this class.
     *
     * @see #ConditionalTransferResult(AbstractValue, Store, Store, Map, boolean)
     */
    public ConditionalTransferResult(
            @Nullable A value, StoreSet<S> thenStore, StoreSet<S> elseStore, boolean storeChanged) {
        this(value, thenStore, elseStore, null, storeChanged);
    }

    /** @see #ConditionalTransferResult(AbstractValue, Store, Store, Map, boolean) */
    public ConditionalTransferResult(
            @Nullable A value, StoreSet<S> thenStore, StoreSet<S> elseStore) {
        this(value, thenStore, elseStore, false);
    }

    /** @see #ConditionalTransferResult(AbstractValue, Store, Store, Map, boolean) */
    public ConditionalTransferResult(
            A value,
            StoreSet<S> thenStore,
            StoreSet<S> elseStore,
            Map<TypeMirror, StoreSet<S>> exceptionalStores) {
        this(value, thenStore, elseStore, exceptionalStores, false);
    }

    /**
     * Create a {@code ConditionalTransferResult} with {@code thenStore} as the resulting store if
     * the corresponding {@link org.checkerframework.dataflow.cfg.node.Node} evaluates to {@code
     * true} and {@code elseStore} otherwise.
     *
     * <p>For the meaning of storeChanged, see {@link
     * org.checkerframework.dataflow.analysis.TransferResult#storeChanged}.
     *
     * <p><em>Exceptions</em>: If the corresponding {@link
     * org.checkerframework.dataflow.cfg.node.Node} throws an exception, then the corresponding
     * store in {@code exceptionalStores} is used. If no exception is found in {@code
     * exceptionalStores}, then it is assumed that no special handling is necessary and the store
     * before the corresponding {@link org.checkerframework.dataflow.cfg.node.Node} will be passed
     * along any exceptional edge.
     *
     * <p><em>Aliasing</em>: {@code thenStore}, {@code elseStore}, and any store in {@code
     * exceptionalStores} are not allowed to be used anywhere outside of this class (including use
     * through aliases). Complete control over the objects is transfered to this class.
     */
    public ConditionalTransferResult(
            @Nullable A value,
            StoreSet<S> thenStore,
            StoreSet<S> elseStore,
            @Nullable Map<TypeMirror, StoreSet<S>> exceptionalStores,
            boolean storeChanged) {
        super(value, exceptionalStores);
        this.thenStore = thenStore;
        this.elseStore = elseStore;
        this.storeChanged = storeChanged;
    }

    /** The regular result store. */
    @Override
    public StoreSet<S> getRegularStore() {
        return thenStore.leastUpperBound(elseStore);
    }

    @Override
    public StoreSet<S> getThenStore() {
        return thenStore;
    }

    @Override
    public StoreSet<S> getElseStore() {
        return elseStore;
    }

    @Override
    public boolean containsTwoStores() {
        return true;
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        result.add("RegularTransferResult(");
        result.add("  resultValue = " + resultValue);
        result.add("  thenStore = " + thenStore);
        result.add("  elseStore = " + elseStore);
        result.add(")");
        return result.toString();
    }

    /** @see org.checkerframework.dataflow.analysis.TransferResult#storeChanged() */
    @Override
    public boolean storeChanged() {
        return storeChanged;
    }
}
