package moe.plushie.armourers_workshop.utils;

import moe.plushie.armourers_workshop.core.utils.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EnumToEnumMap<K extends Enum<K>, V extends Enum<V>> {

    private final List<K> idToKey;
    private final List<V> idToValue;

    public EnumToEnumMap(List<K> idToKey, List<V> idToValue) {
        this.idToKey = idToKey;
        this.idToValue = idToValue;
    }

    public static <K extends Enum<K>, V extends Enum<V>> EnumToEnumMap<K, V> byName(K defaultKey, V defaultValue) {
        var keys = defaultKey.getDeclaringClass().getEnumConstants();
        var values = defaultValue.getDeclaringClass().getEnumConstants();
        var nameToKey = new HashMap<String, K>();
        for (var key : keys) {
            nameToKey.put(key.name(), key);
        }
        var nameToValue = new HashMap<String, V>();
        for (var value : values) {
            nameToValue.put(value.name(), value);
        }
        var idToKey = Collections.compactMap(values, value -> nameToKey.getOrDefault(value.name(), defaultKey));
        var idToValue = Collections.compactMap(keys, key -> nameToValue.getOrDefault(key.name(), defaultValue));
        return new EnumToEnumMap<>(idToKey, idToValue);
    }

    public K getKey(V value) {
        return idToKey[value.ordinal()];
    }

    public V getValue(K key) {
        return idToValue[key.ordinal()];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumToEnumMap<?, ?> map)) return false;
        return Objects.equals(idToKey, map.idToKey) && Objects.equals(idToValue, map.idToValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idToKey, idToValue);
    }
}
