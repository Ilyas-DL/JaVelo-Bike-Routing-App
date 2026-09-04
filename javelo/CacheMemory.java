package ch.epfl.javelo.gui;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores map up to MAX_SIZE, if more data is stored, automatically remove
 * the least used entry.
 * @param <K> first parameter (key)
 * @param <V> second parameter (value)
 * @author Eden Kahane (346481).
 */
public final class CacheMemory<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

    /**
     * LinkedHashMap with a maximum size of MAX_SIZE
     */
    public CacheMemory(int maxSize) {
            super(maxSize, 1, true);
            this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
    }
}
