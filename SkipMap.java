package util;

import java.util.*;
/*Bi-directional "Tower-Based" SkipMap
 * Node<K,V> { K key; F val; Node<F>[] nexts; Node<F>[] prevs; }
 */
public class SkipMap<K,V> extends AbstractMap<K,V> implements NavigableMap<K,V>, Cloneable, java.io.Serializable {
    public static class Entry<K,V> implements Map.Entry<K,V> {
        Entry<K,V>[] nextArr;
        Entry<K,V>[] prevArr;
        K key;
        V value;
        @SuppressWarnings("unchecked")
        public Entry(K key, V value, int depth){
            this.key = key;
            this.value = value;
            prevArr = new Entry[depth];
            nextArr = new Entry[depth];
        }
        public Entry(K key, V value) {
            this(key, value, Integer.numberOfTrailingZeros(RANDOM.nextInt()) + 1);
        }
        public int size() {
            return nextArr.length;
        }
        public void clear(){
            Arrays.fill(nextArr, null);
            Arrays.fill(prevArr, null);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return null;
        }
    }

    Entry<K,V> head =new Entry<>(null, null, MAX_DEPTH);
    int size;
    private static final int MAX_DEPTH = 32;
    private static final Random RANDOM = new Random();


    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }
    private void remove(Entry e){
        for(int i = 0; i < size(); i++) {
            (e.prevArr[i] == null ? head : e.prevArr[i]).nextArr[i] = e.nextArr[i];
            (e.nextArr[i] == null ? head : e.nextArr[i]).prevArr[i] = e.prevArr[i];
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return null;
    }

    @Override
    public K lowerKey(K key) {
        return null;
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return null;
    }

    @Override
    public K floorKey(K key) {
        return null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return null;
    }

    @Override
    public K ceilingKey(K key) {
        return null;
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return null;
    }

    @Override
    public K higherKey(K key) {
        return null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        return head.nextArr[0];
    }

    @Override
    public Entry<K, V> lastEntry() {
        return head.prevArr[0];
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return null;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return null;
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return null;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return null;
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return null;
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return null;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return null;
    }

    @Override
    public K firstKey() {
        return null;
    }

    @Override
    public K lastKey() {
        return null;
    }
}
