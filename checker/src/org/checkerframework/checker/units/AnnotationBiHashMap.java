package org.checkerframework.checker.units;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.m;

/**
 * A BiHashMap is a hash map which uses 2 keys as a pair to map to each value.
 *
 * <p>In this BiHashMap, we map (AnnotationMirror, AnnotationMirror)s to AnnotationMirrors. However,
 * since multiple objects of class AnnotationMirror can represent the same annotation, thus having
 * different hashcodes, we use the fully qualified string name of each AnnotationMirror as the
 * internal keys for the BiHashMap.
 *
 * <p>The maps are sensitive to the element values of each annotation, as {@link m}({@link
 * Prefix#kilo}) is not the same unit as {@link m}({@link Prefix#milli}).
 *
 * @see AnnotationMirror
 */
public class AnnotationBiHashMap {

    // stores the set of annotation mirrors used as k1
    private final Set<AnnotationMirror> k1Keys;

    // maps (k1, k2) to an annotation mirror, where k1 and k2 are respectively
    // the fully qualified names of two annotation mirrors that are the
    // arguments of some operation
    private final Map<String, Map<String, AnnotationMirror>> typeCheckMap;

    // maps k1 to a mapping of annotation mirrors to annotation mirrors, where
    // k1 is the fully qualified name of the left hand side annotation mirror of
    // some operation
    private final Map<String, Map<AnnotationMirror, AnnotationMirror>> inferenceMap;

    public AnnotationBiHashMap() {
        k1Keys = new HashSet<AnnotationMirror>();
        typeCheckMap = new HashMap<String, Map<String, AnnotationMirror>>();
        inferenceMap = new HashMap<String, Map<AnnotationMirror, AnnotationMirror>>();
    }

    /**
     * Adds the triple (key1, key2, val) to the map.
     *
     * @param key1 An annotation mirror.
     * @param key2 An annotation mirror.
     * @param val An annotation mirror.
     * @return val If insertion is successful, or the existing value at (key1, key2).
     */
    public AnnotationMirror put(
            AnnotationMirror key1, AnnotationMirror key2, AnnotationMirror val) {

        Map<String, AnnotationMirror> innerTCMap;
        Map<AnnotationMirror, AnnotationMirror> innerInfMap;

        String k1 = toKey(key1);
        String k2 = toKey(key2);

        if (typeCheckMap.containsKey(k1)) {
            // if k1 is in the map, obtain the inner map
            innerTCMap = typeCheckMap.get(k1);
            innerInfMap = inferenceMap.get(k1);
        } else {
            // if k1 is not in the map, add it to the set and create the inner map
            k1Keys.add(key1);
            innerTCMap = new HashMap<String, AnnotationMirror>();
            typeCheckMap.put(k1, innerTCMap);
            innerInfMap = new HashMap<AnnotationMirror, AnnotationMirror>();
            inferenceMap.put(k1, innerInfMap);
        }

        // if k2 is not in the inner map, add val to the inner map
        if (!innerTCMap.containsKey(k2)) {
            innerTCMap.put(k2, val);
            innerInfMap.put(key2, val);
        }
        return innerTCMap.get(k2);
    }

    /**
     * Obtains and returns the annotation mirror indexed by (key1, key2) from the map.
     *
     * @param key1 An annotation mirror.
     * @param key2 An annotation mirror.
     * @return The annotation mirror indexed by (key1, key2).
     */
    public AnnotationMirror get(AnnotationMirror key1, AnnotationMirror key2) {
        String k1 = toKey(key1);
        String k2 = toKey(key2);
        if (typeCheckMap.containsKey(k1) && typeCheckMap.get(k1).containsKey(k2)) {
            return typeCheckMap.get(k1).get(k2);
        }
        return null;
    }

    /**
     * Generates and returns an immutable map of (key2 to val) indexed by key1 from the map.
     *
     * @param key1 An annotation mirror.
     * @return An immutable map of (key2 to val) indexed by key1.
     */
    // used for inference only
    public Map<AnnotationMirror, AnnotationMirror> getInnerMap(AnnotationMirror key1) {
        String k1 = toKey(key1);
        if (inferenceMap.containsKey(k1)) {
            return Collections.unmodifiableMap(inferenceMap.get(k1));
        } else {
            return null;
        }
    }

    /**
     * Generates and returns an immutable version of the BiHashMap.
     *
     * @return An immutable version of the BiHashMap as mappings between annotation mirrors.
     */
    public Map<AnnotationMirror, Map<AnnotationMirror, AnnotationMirror>> getFullMap() {
        Map<AnnotationMirror, Map<AnnotationMirror, AnnotationMirror>> fullMap =
                new HashMap<AnnotationMirror, Map<AnnotationMirror, AnnotationMirror>>();
        for (AnnotationMirror key1 : k1Keys) {
            fullMap.put(key1, getInnerMap(key1));
        }
        return Collections.unmodifiableMap(fullMap);
    }

    /**
     * Returns the set of annotation mirrors used as the key1 keys of the BiHashMap.
     *
     * @return Set of keys.
     */
    public Set<AnnotationMirror> keySet() {
        return k1Keys;
    }

    /**
     * Checks to see if key is in the set of key1 keys.
     *
     * @param key An annotation mirror.
     * @return True if key is in the set of key1 keys, false otherwise.
     */
    public boolean containsK1Key(AnnotationMirror key) {
        return typeCheckMap.containsKey(toKey(key));
    }

    /**
     * Checks to see if (key1, key2) is mapped to a value in the map.
     *
     * @param key1 An annotation mirror.
     * @param key2 An annotation mirror.
     * @return True if (key1, key2) is mapped to a value.
     */
    public boolean containsKeys(AnnotationMirror key1, AnnotationMirror key2) {
        String k1 = toKey(key1);
        String k2 = toKey(key2);
        return typeCheckMap.containsKey(k1) && typeCheckMap.get(k1).containsKey(k2);
    }

    /** Clears the BiHashMap of all values. */
    public void clear() {
        k1Keys.clear();
        typeCheckMap.clear();
        inferenceMap.clear();
    }

    /**
     * Returns the number of values in the BiHashMap.
     *
     * @return The number of values in the BiHashMap.
     */
    public int size() {
        int count = 0;
        for (String key : typeCheckMap.keySet()) {
            count += typeCheckMap.get(key).size();
        }
        return count;
    }

    /**
     * Returns a deep copy of the structure of the BiHashMap, note that AnnotationMirrors are not
     * deep copied.
     *
     * @return The deep copy of the structure of the BiHashMap.
     */
    protected AnnotationBiHashMap deepCopy() {
        AnnotationBiHashMap clone = new AnnotationBiHashMap();

        // copy k1Keys
        for (AnnotationMirror key : this.k1Keys) {
            clone.k1Keys.add(key);
        }

        // copy typeCheckMap
        for (String key1 : this.typeCheckMap.keySet()) {
            Map<String, AnnotationMirror> innerMap = new HashMap<String, AnnotationMirror>();
            clone.typeCheckMap.put(key1, innerMap);

            for (String key2 : this.typeCheckMap.get(key1).keySet()) {
                innerMap.put(key2, this.typeCheckMap.get(key1).get(key2));
            }
        }

        // copy inferenceMap
        for (String key1 : this.inferenceMap.keySet()) {
            Map<AnnotationMirror, AnnotationMirror> innerMap =
                    new HashMap<AnnotationMirror, AnnotationMirror>();
            clone.inferenceMap.put(key1, innerMap);

            for (AnnotationMirror key2 : this.inferenceMap.get(key1).keySet()) {
                innerMap.put(key2, this.inferenceMap.get(key1).get(key2));
            }
        }

        return clone;
    }

    /**
     * Obtains the string representation of the input annotation mirror, used as the keys to
     * typeCheckMap and inferenceMap.
     */
    private String toKey(AnnotationMirror key) {
        return key.toString().intern();
    }
}
