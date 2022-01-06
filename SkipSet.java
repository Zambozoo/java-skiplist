package util;

import java.util.*;
/*Bi-directional "Tower-Based" SkipSet
 * Node<F> { F val; Node<F>[] nexts; Node<F>[] prevs; }
 */

public class SkipSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable {
    public class Node<F> {
        Node<F>[] nextArr;
        Node<F>[] prevArr;
        F value;
        @SuppressWarnings("unchecked")
        public Node(F value, int depth){
            this.value = value;
            prevArr = new Node[depth];
            nextArr = new Node[depth];
        }
        public Node(F value) {
            this(value, Integer.numberOfTrailingZeros(RANDOM.nextInt()) + 1);
        }
        public int size() {
            return nextArr.length;
        }
        public void clear(){
            Arrays.fill(nextArr, null);
            Arrays.fill(prevArr, null);
        }
    }
    Node<E> head = new Node<>(null, MAX_DEPTH);
    int size;
    private static final int MAX_DEPTH = 32;
    private static final Random RANDOM = new Random();

    //region Get
    @Override
    public E first() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.nextArr[0].value;
    }
    @Override
    public E last() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.prevArr[0].value;
    }
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return getNode(o) != null;
    }

    @SuppressWarnings("unchecked")
    private Node<E> getNode(Object o) {
        Comparable<? super E> k = (Comparable<? super E>) o;
        Node<E> curNode = head;
        for (int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.nextArr[i] != null) {
                int cmp = k.compareTo(curNode.nextArr[i].value);
                if (cmp > 0) {
                    curNode = curNode.nextArr[i];
                } else if (cmp < 0) {
                    break;
                } else {
                    return curNode.nextArr[i];
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E lower(E e) {
        Comparable<? super E> k = (Comparable<? super E>) e;
        Node<E> curNode = head;
        for (int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.nextArr[i] != null) {
                if (k.compareTo(curNode.nextArr[i].value) > 0) {
                    curNode = curNode.nextArr[i];
                } else {
                    break;
                }
            }
        }
        return curNode.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E floor(E e) {
        Comparable<? super E> k = (Comparable<? super E>) e;
        Node<E> curNode = head;
        for (int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.nextArr[i] != null) {
                int cmp = k.compareTo(curNode.nextArr[i].value);
                if (cmp > 0) {
                    curNode = curNode.nextArr[i];
                } else if(cmp < 0) {
                    break;
                } else {
                    return e;
                }
            }
        }
        return curNode.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E ceiling(E e) {
        Comparable<? super E> k = (Comparable<? super E>) e;
        Node<E> curNode = head;
        for (int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.prevArr[i] != null) {
                int cmp = k.compareTo(curNode.prevArr[i].value);
                if (cmp < 0) {
                    curNode = curNode.prevArr[i];
                } else if (cmp > 0){
                    break;
                } else {
                    return e;
                }
            }
        }
        return curNode.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E higher(E e) {
        Comparable<? super E> k = (Comparable<? super E>) e;
        Node<E> curNode = head;
        for (int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.prevArr[i] != null) {
                if (k.compareTo(curNode.prevArr[i].value) < 0) {
                    curNode = curNode.prevArr[i];
                } else {
                    break;
                }
            }
        }
        return curNode.value;
    }

    //endregion

    //region Add
    @Override
    @SuppressWarnings("unchecked")
    public boolean add(E e) {
        if(!contains(e)) {
            Node<E> n = new Node<>(e);
            Comparable<? super E> k = (Comparable<? super E>) e;
            Node<E> curNode = head;
            int i = MAX_DEPTH - 1;
            for (; i >= n.size(); i--) {
                while (curNode.nextArr[i] != null) {
                    int cmp = k.compareTo(curNode.nextArr[i].value);
                    if (cmp > 0) {
                        curNode = curNode.nextArr[i];
                    } else {
                        break;
                    }
                }
            }
            for (; i >= 0; i--) {
                while (curNode.nextArr[i] != null) {
                    int cmp = k.compareTo(curNode.nextArr[i].value);
                    if (cmp > 0) {
                        curNode = curNode.nextArr[i];
                    } else {
                        break;
                    }
                }
                (curNode.nextArr[i] == null ? head : curNode.nextArr[i]).prevArr[i] = n;
                n.nextArr[i] = curNode.nextArr[i];
                if(curNode != head) {
                    n.prevArr[i] = curNode;
                }
                curNode.nextArr[i] = n;
            }
            size++;
            return true;
        }
        return false;
    }
    //endregion

    //region Remove
    @Override
    public E pollFirst() {
        Node<E> n = head.nextArr[0];
        if(n == null) {
            return null;
        } else {
            remove(n);
            size--;
            return n.value;
        }
    }

    @Override
    public E pollLast() {
        Node<E> n = head.prevArr[0];
        if(n == null) {
            return null;
        } else {
            remove(n);
            size--;
            return n.value;
        }
    }

    @Override
    public boolean remove(Object o) {
        Node<E> n = getNode(o);
        if (n != null) {
            remove(n);
            size--;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        size = 0;
        head.clear();
    }

    public void remove(Node<E> n){
        for(int i = 0; i < size(); i++) {
            (n.prevArr[i] == null ? head : n.prevArr[i]).nextArr[i] = n.nextArr[i];
            (n.nextArr[i] == null ? head : n.nextArr[i]).prevArr[i] = n.prevArr[i];
        }
    }
    //endregion

    //TODO SplitIterator, Clones, etc
    //region Iterate
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            Node<E> curNode = head.nextArr[0];
            @Override
            public boolean hasNext() {
                return curNode != null;
            }

            @Override
            public E next() {
                E value = curNode.value;
                curNode = curNode.nextArr[0];
                return value;
            }
        };
    }

    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }
    //endregion

    //region Clone
    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return null;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    }

    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    }
    //endregion
}
