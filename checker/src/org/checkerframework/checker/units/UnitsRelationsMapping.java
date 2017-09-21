package org.checkerframework.checker.units;

import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;

/**
 * Organizes units relationships for a given arithmetic operation into a mapping table.
 *
 * <p>Each UnitsRelationsMapping object uses two {@link AnnotationBiHashMap}s to store and manage
 * the relationships. The xyMap stores each relation (x, y, value) in a {@literal Map<x, Map<y,
 * value>>} data structure. The yxMap stores each relation (x, y, value) in a {@literal Map<y,
 * Map<x, value>>} data structure. Both are necessary for providing fast access to the rows and
 * columns of the mapping table for inference.
 */
public final class UnitsRelationsMapping {
    private AnnotationBiHashMap xyMap;
    private AnnotationBiHashMap yxMap;

    public UnitsRelationsMapping() {
        xyMap = new AnnotationBiHashMap();
        yxMap = new AnnotationBiHashMap();
    }

    /**
     * Inserts the relation (x, y, val) into the mapping iff (x, y) isn't already mapped to some
     * value.
     *
     * @param x An AnnotationMirror.
     * @param y An AnnotationMirror.
     * @param val An AnnotationMirror.
     * @return val If insertion is successful, or the existing value at (x, y).
     */
    public AnnotationMirror put(AnnotationMirror x, AnnotationMirror y, AnnotationMirror val) {
        yxMap.put(y, x, val);
        return xyMap.put(x, y, val);
    }

    /**
     * Obtains the value indexed by (x, y) in the mapping.
     *
     * @param x An AnnotationMirror.
     * @param y An AnnotationMirror.
     * @return val value indexed by (x, y) as An AnnotationMirror., or null if (x, y) doesn't map to
     *     any values.
     */
    public AnnotationMirror get(AnnotationMirror x, AnnotationMirror y) {
        return xyMap.get(x, y);
    }

    /**
     * Returns all relations with first index x in the mapping. This is used for constant_variable
     * serialization.
     *
     * @param x An AnnotationMirror.
     * @return An immutable map of (y to val) for all (x, y, val) relations, or null if x doesn't
     *     map to any values.
     */
    public Map<AnnotationMirror, AnnotationMirror> getAllY(AnnotationMirror x) {
        return xyMap.getInnerMap(x);
    }

    /**
     * Returns all relations with second index y in the mapping. This is used for variable_constant
     * serialization.
     *
     * @param y An AnnotationMirror.
     * @return An immutable map of (x to val) for all (x, y, val) relations, or null if y doesn't
     *     map to any values.
     */
    public Map<AnnotationMirror, AnnotationMirror> getAllX(AnnotationMirror y) {
        return yxMap.getInnerMap(y);
    }

    /**
     * Returns all relations in the mapping. This is used for variable_variable serialization.
     *
     * @return An immutable map of all (x, y, val) relations.
     */
    public Map<AnnotationMirror, Map<AnnotationMirror, AnnotationMirror>> getAllXY() {
        return xyMap.getFullMap();
    }

    /**
     * Returns a set of all units used as the primary keys of the mapping.
     *
     * @return All units used as the primary keys of the mapping.
     */
    public Set<AnnotationMirror> xKeySet() {
        return xyMap.keySet();
    }

    /**
     * Returns a set of all units used as the secondary keys of the mapping for the given primary
     * key.
     *
     * @param x AnnotationMirror of the primary key.
     * @return All units used as the secondary keys of the mapping for the given primary key, or
     *     null if the primary key is not in the mapping.
     */
    protected Set<AnnotationMirror> yKeySet(AnnotationMirror x) {
        return xyMap.containsK1Key(x) ? xyMap.getInnerMap(x).keySet() : null;
    }

    /**
     * Checks to see if (x, y) maps to a value in the mapping.
     *
     * @param x An AnnotationMirror.
     * @param y An AnnotationMirror.
     * @return True iff (x, y) maps to a value in the mapping.
     */
    public boolean containsKeys(AnnotationMirror x, AnnotationMirror y) {
        return xyMap.containsKeys(x, y) && yxMap.containsKeys(y, x);
    }

    /** Removes all relationships in the mapping. */
    public void clear() {
        xyMap.clear();
        yxMap.clear();
    }

    /** Returns a count of how many relations are stored in the mapping */
    public int size() {
        return xyMap.size();
    }

    /**
     * Creates and returns a deep copy of the mapping, note that the underlying AnnotationMirrors
     * are shallow-copied.
     *
     * @return A deep copy of the mapping.
     */
    public UnitsRelationsMapping deepCopy() {
        UnitsRelationsMapping clone = new UnitsRelationsMapping();
        clone.xyMap = this.xyMap.deepCopy();
        clone.yxMap = this.yxMap.deepCopy();
        return clone;
    }
}
