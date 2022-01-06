package util;

import java.util.*;
/*Bi-directional "Tower-Based" SkipList
* Node<F> { F val; Node<F>[] nexts; Node<F>[] prevs; int[] dists;
* TODO: SplitIterator
*/

public class SkipList<E> extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    //region Constructor, Variables, Node
    private static class Node<F> {
        F value;
        Node<F>[] nextArr;
        Node<F>[] prevArr;
        int[] distArr;//Distance to next node.

        /***
         * Generates a node with a specified height.
         * @param value Value of generated node.
         * @param depth Depth of generated node, [1, 32]
         */
        @SuppressWarnings("unchecked")
        public Node(F value, int depth) {
            this.value = value;
            prevArr = new Node[depth];
            nextArr = new Node[depth];
            distArr = new int[depth];
        }

        /***
         * Generates a new node with a random height between 1 and 32.
         * probability(height) = 2 ^ -height
         * @param value Value of generated node
         */
        public Node(F value) {
            this(value, Integer.numberOfTrailingZeros(RANDOM.nextInt() << 1));
        }
    }

    private Node<E> head = new Node<>(null, MAX_DEPTH);
    private int size;

    private static final int MAX_DEPTH = 32;
    private static final Random RANDOM = new Random();

    /***
     * Initializes an empty SkipList.
     * Produces an empty head node.
     */
    public SkipList() {
        Arrays.fill(head.distArr, 1);
    }

    /***
     * Initializes a populated SkipList.
     * @param c Collection to populate SkipList
     */
    public SkipList(Collection<? extends E> c) {
        addAll(c);
    }
    //endregion

    //region Get

    /***
     * @return True if SkipList has a size of 0
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /***
     * @return Amount of elements in SkipList, [0, INT_MAX]
     */
    @Override
    public int size() {
        return size;
    }

    /***
     * @param o Object to query
     * @return True if Object is an element of SkipList
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /***
     * @param o Object to query
     * @return First index of Object, or -1 if not present
     */
    @Override
    public int indexOf(Object o) {
        int index = 0;
        if(size > 0) {
            if (o == null) {
                for (Node<E> x = head.nextArr[0]; x != null; x = x.nextArr[0]) {
                    if (x.value == null)
                        return index;
                    index++;
                }
            } else {
                for (Node<E> x = head.nextArr[0]; x != null; x = x.nextArr[0]) {
                    if (o.equals(x.value))
                        return index;
                    index++;
                }
            }
        }
        return -1;
    }

    /***
     * @param o Object to query
     * @return Last index of object, or -1 if not present.
     */
    @Override
    public int lastIndexOf(Object o) {
        int index = size;
        if(size > 0) {
            if (o == null) {
                for (Node<E> x = head.prevArr[0]; x != null; x = x.prevArr[0]) {
                    index--;
                    if (x.value == null)
                        return index;
                }
            } else {
                for (Node<E> x = head.prevArr[0]; x != null; x = x.prevArr[0]) {
                    index--;
                    if (o.equals(x.value))
                        return index;
                }
            }
        }
        return -1;
    }

    /***
     * @param index Index of requested element.
     * @return Value of element at requested index.
     */
    @Override
    public E get(int index) {
        return getNode(index).value;
    }
    private Node<E> getNode(int index) {
        if(index < 0 || index >= size) {
            throw new NoSuchElementException();
        }
        Node<E> curNode = head;
        index++;
        for(int i = MAX_DEPTH - 1; i >= 0; i--) {
            while (curNode.nextArr[i] != null && index - curNode.distArr[i] >= 0) {
                index -= curNode.distArr[i];
                curNode = curNode.nextArr[i];
            }
        }
        return curNode;
    }

    /***
     * @exception NoSuchElementException Thrown if SkipList is empty
     * @return First element in SkipList
     */
    @Override
    public E getFirst() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.nextArr[0].value;
    }

    /***
     * @exception NoSuchElementException Thrown if SkipList is empty
     * @return Last element in SkipList
     */
    @Override
    public E getLast() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.prevArr[0].value;
    }

    /***
     * @exception NoSuchElementException Thrown if SkipList is empty
     * @return First element in SkipList
     */
    @Override
    public E element() {
        return getFirst();
    }

    /***
     * @return First element in SkipList, or null if empty
     */
    @Override
    public E peekFirst() {
        return (size == 0) ? null : getFirst();
    }

    /***
     * @return Last element in SkipList, or null if empty
     */
    @Override
    public E peekLast() {
        return (size == 0) ? null : getLast();
    }

    /***
     * @return First element in SkipList, or null if empty
     */
    @Override
    public E peek() {
        return peekFirst();
    }
    //endregion

    //region Add
    /**
     * Append element to SkipList.
     * @param e Element to be appended.
     * @return True if successful.
     */
    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * Prepend element to SkipList.
     * @param e Element to be prepended.
     * @return True if successful.
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Append element to SkipList.
     * @param e Element to be appended.
     * @return True if successful.
     */
    @Override
    public boolean offerLast(E e) {
        return add(e);
    }

    /**
     * Append element to SkipList.
     * @param e Element to be appended.
     * @return True if successful.
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * Append element to SkipList.
     * @param e Element to be appended.
     * @return True if successful.
     */
    @Override
    public void addLast(E e) {
        Node<E> n = new Node<>(e);
        int i = MAX_DEPTH - 1;
        //Rows[depth >= n.length][-1] == null
        for(; i >= n.distArr.length && head.prevArr[i] == null; i--) {
            head.distArr[i]++;
        }
        //Rows[depth >= n.length][-1] != null
        for(; i >= n.distArr.length; i--) {
            head.prevArr[i].distArr[i]++;
        }
        //Rows[0 <= depth < n.length][-1] == null
        for(; i >= 0 && head.prevArr[i] == null; i--) {
            head.nextArr[i] = n;
            head.prevArr[i] = n;
        }
        //Rows[depth < n.length][-1] != null
        for(; i >= 0; i--) {
            head.prevArr[i].nextArr[i] = n;
            n.prevArr[i] = head.prevArr[i];
            head.prevArr[i] = n;
        }
        Arrays.fill(n.distArr, 1);
        size++;
    }

    /**
     * Prepend element to SkipList.
     * @param e Element to be appended.
     */
    @Override
    public void addFirst(E e) {
        Node<E> n = new Node<>(e);
        int i = MAX_DEPTH - 1;
        //Rows[depth >= n.length][0] == null
        for(; i >= n.distArr.length; i--) {
            head.distArr[i]++;
        }
        //Rows[depth < n.length][0] == null
        for(; i >= 0 && head.nextArr[i] == null; i--) {
            n.distArr[i] = head.distArr[i];
            head.distArr[i] = 1;
            head.prevArr[i] = n;
            head.nextArr[i] = n;
        }
        //Rows[depth < n.length][0] != null
        for(; i >= 0; i--) {
            n.nextArr[i] = head.nextArr[i];
            n.nextArr[i].prevArr[i] = n;
            head.nextArr[i] = n;
            n.distArr[i] = head.distArr[i];
            head.distArr[i] = 1;
        }
        size++;
    }

    /**
     * Insert element into SkipList.
     * @param index Index to insert Element
     * @param e Element to be appended.
     */
    @Override
    public void add(int index, E e) {
        if(index >= 0 && index <= size) {
            Node<E> n =new Node<>(e);
            Node<E> curNode = head;
            int i = MAX_DEPTH - 1;
            //Rows[depth >= n.length][0]
            for(; i >= n.distArr.length; i--) {
                while (curNode.nextArr[i] != null && index - curNode.distArr[i] >= 0) {
                    index -= curNode.distArr[i];
                    curNode = curNode.nextArr[i];
                }
                curNode.distArr[i]++;
            }
            //Rows[depth < n.length][0]
            for(; i >= 0; i--) {
                while (curNode.nextArr[i] != null && index - curNode.distArr[i] >= 0) {
                    index -= curNode.distArr[i];
                    curNode = curNode.nextArr[i];
                }
                if (curNode.nextArr[i] != null) {
                    n.nextArr[i] = curNode.nextArr[i];
                    n.nextArr[i].prevArr[i] = n;
                } else {
                    head.prevArr[i] = n;
                }
                if (curNode != head) {
                    n.prevArr[i] = curNode;
                }
                curNode.nextArr[i] = n;
                n.distArr[i] = curNode.distArr[i] - index;
                curNode.distArr[i] = index + 1;
            }
            size++;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Prepend element to SkipList.
     * @param e Element to be appended.
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }


    /**
     * @param index Index to set
     * @param element Value to set
     * @return Value if valid index, else null
     */
    @Override
    public E set(int index, E element) {
        Node<E> n = getNode(index);
        if(n != null) {
            E value = n.value;
            n.value = element;
            return value;
        }
        return null;
    }
    //endregion

    //region Delete

    /**
     * Clear all elements from SkipList.
     */
    @Override
    public void clear() {
        size = 0;
        Arrays.fill(head.distArr, 1);
        Arrays.fill(head.nextArr, null);
        Arrays.fill(head.prevArr, null);
    }

    /**
     * Removes first instance of Object.
     * @param o Object to be removed.
     * @return True if successful.
     */
    @Override
    public boolean remove(Object o) {
        int i = indexOf(o);
        if(i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    /**
     * Removes first instance of Object.
     * @param o Object to be removed.
     * @return True if successful.
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * Removes last instance of Object.
     * @param o Object to be removed.
     * @return True if successful.
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if(i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    /**
     * Removes first element of SkipList.
     * @throws NoSuchElementException Thrown if list is empty.
     * @return Value of removed element.
     */
    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else {
            Node<E> n = head.nextArr[0];
            int i = MAX_DEPTH - 1;
            //Row[depth >= n.length]
            for(; i >= n.distArr.length; i--) {
                head.distArr[i]--;
            }
            //Row[depth < n.length].length == 1
            for(; i >= 0 && n.nextArr[i] == null; i--) {
                head.nextArr[i] = null;
                head.prevArr[i] = null;
                head.distArr[i] += n.distArr[i] - 1;
            }
            //Row[depth < n.length].length > 1
            for(; i >= 0; i--) {
                //noinspection ConstantConditions
                n.nextArr[i].prevArr[i] = null;
                head.nextArr[i] = n.nextArr[i];
                head.distArr[i] += n.distArr[i] - 1;
            }
            size--;
            return n.value;
        }
    }

    /**
     * Removes last element of SkipList.
     * @throws NoSuchElementException Thrown if list is empty.
     * @return Value of removed element.
     */
    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else {
            Node<E> n = head.prevArr[0];
            int i = MAX_DEPTH - 1;
            //Row[depth >= n.length][-1] == null
            for(; i >= n.distArr.length && head.prevArr[i] == null; i--) {
                head.distArr[i]--;
            }
            //Row[depth >= n.length][-1] != null
            for(; i >= n.distArr.length; i--) {
                head.prevArr[i].distArr[i]--;
            }
            //Row[depth < n.length].length == 1
            for(; i >= 0 && n.prevArr[i] == null; i--) {
                head.prevArr[i] = null;
                head.nextArr[i] = null;
                head.distArr[i] += n.distArr[i] - 1;
            }
            //Row[depth < n.length].length > 1
            for(; i >= 0; i--) {
                //noinspection ConstantConditions
                n.prevArr[i].nextArr[i] = null;
                head.prevArr[i] = n.prevArr[i];
                n.prevArr[i].distArr[i] += n.distArr[i] - 1;
            }
            size--;
            return n.value;
        }
    }

    /**
     * Removes first element of SkipList.
     * @throws NoSuchElementException Thrown if list is empty.
     * @return Value of removed element.
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * Removes Element at requested index of SkipList.
     * @param index Index to be removed, [0, size)
     * @throws NoSuchElementException Thrown if index is invalid.
     * @return Value of removed element if valid index, else null.
     */
    @Override
    public E remove(int index) {
        if(index >= 0 && index < size) {
            Node<E> curNode = head;
            int i = MAX_DEPTH - 1;
            //Rows[depth >= n.length][0]
            while(true) {
                while (curNode.nextArr[i] != null && index - curNode.distArr[i] >= -1) {
                    index -= curNode.distArr[i];
                    curNode = curNode.nextArr[i];
                }
                if(index == -1) {
                    //Rows[depth < n.length]
                    for(; i >= 0; i--) {
                        Node<E> n = curNode.prevArr[i] == null ? head : curNode.prevArr[i];
                        n.nextArr[i] = curNode.nextArr[i];
                        n.distArr[i] += curNode.distArr[i] - 1;
                        (curNode.nextArr[i] == null ? head : curNode.nextArr[i]).prevArr[i] = curNode.prevArr[i];
                    }
                    size--;
                    return curNode.value;
                }
                curNode.distArr[i]--;
                i--;
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Removes first element of SkipList.
     * @return Value of removed element, or null if SkipList is empty.
     */
    @Override
    public E poll() {
        return (size == 0) ? null : removeFirst();
    }

    /**
     * Removes first element of SkipList.
     * @return Value of removed element, or null if SkipList is empty.
     */
    @Override
    public E pollFirst() {
        return (size == 0) ? null : removeFirst();
    }

    /**
     * Removes last element of SkipList.
     * @return Value of removed element, or null if SkipList is empty.
     */
    @Override
    public E pollLast() {
        return (size == 0) ? null : removeLast();
    }


    /**
     * Removes first element of SkipList.
     * @throws NoSuchElementException Thrown if list is empty.
     * @return Value of removed element, or null if SkipList is empty.
     */
    @Override
    public E pop() {
        return removeFirst();
    }
    //endregion

    //region Copy

    /**
     * @return Array of elements in SkipList in order.
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        if(size > 0) {
            int i = 0;
            for (Node<E> n = head.nextArr[0]; n != null; n = n.nextArr[0]) {
                result[i++] = n.value;
            }
        }
        return result;
    }

    /**
     * @return Array of elements in SkipList in order.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        if(size > 0) {
            int i = 0;
            for (Node<E> n = head.nextArr[0]; n != null; n = n.nextArr[0]) {
                ((Object[]) a)[i++] = n.value;
            }

            if (a.length > size) {
                a[size] = null;
            }
        }
        return a;
    }

    /**
     * @return Shallow copy of SkipList.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SkipList<E> clone = (SkipList<E>) super.clone();

        clone.head = null;
        clone.size = 0;
        for (Node<E> x = head.nextArr[0]; x != null; x = x.nextArr[0])
            clone.add(x.value);

        return clone;
    }

    /**
     * Writes object state to Output Stream.
     * @param s Output Stream to write to.
     * @throws java.io.IOException Thrown if an I/O error occurs.
     */
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        for (Node<E> x = head.nextArr[0]; x != null; x = x.nextArr[0])
            s.writeObject(x.value);
    }

    /**
     * Reads object state from Input Stream.
     * @param s Input Stream to read from.
     * @throws java.io.IOException Thrown if an I/O error occurs.
     * @throws ClassNotFoundException Thrown if the class of a serialized object could not be found.
     */
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for (int i = 0; i < size; i++)
            add((E)s.readObject());
    }
    //endregion

    //region Iterate

    /**
     * Returns an ordered list-iterator of the elements in SkipList, starting at the specified index.
     * @param index Index to begin at.
     * @return List-Iterator of SkipList.
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new SkipListIterator(index);
    }

    /**
     * @return Ordered iterator over the elements in SkipList.
     */
    @Override
    public Iterator<E> iterator(){
        return new ForwardIterator();
    }

    /**
     * @return Reverse order iterator over the elements in SkipList.
     */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * Iterator Class for ListIterator
     */
    class SkipListIterator implements ListIterator<E>{
        Node<E> curNode;
        int index = 0;
        int lastIndex = -1;
        boolean modified = true;
        public SkipListIterator(int index) {
            this.index = index;
            curNode = getNode(index);
        }

        public SkipListIterator(){
            curNode = head == null ? null : head.nextArr[0];
        }
        @Override
        public boolean hasNext() {
            return curNode != null;
        }

        @Override
        public E next() {
            if(curNode == null) {
                throw new NoSuchElementException();
            }
            modified = false;
            lastIndex = index;
            index++;
            E value = curNode.value;
            curNode = curNode.nextArr[0];
            return value;
        }

        @Override
        public boolean hasPrevious() {
            return curNode.prevArr[0] != null;
        }

        @Override
        public E previous() {
            if(curNode.prevArr[0] == null) {
                throw new NoSuchElementException();
            }
            modified = false;
            lastIndex = index;
            index--;
            curNode = curNode.prevArr[0];
            return curNode.value;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index-1;
        }

        @Override
        public void remove() {
            if(modified) {
                throw new IllegalStateException();
            }
            SkipList.this.remove(index);
            index--;
            modified = true;
        }

        @Override
        public void set(E e) {
            if(modified) {
                throw new IllegalStateException();
            }
            (lastIndex > index ? curNode : curNode.prevArr[0]).value = e;
        }

        @Override
        public void add(E e) {
            modified = true;
            SkipList.this.add(index++, e);
        }
    }

    /**
     * ForwardIterator class for Iterator
     */
    class ForwardIterator implements Iterator<E> {
        Node<E> curNode;
        public ForwardIterator() {
            curNode = head == null ? null : head.nextArr[0];
        }

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
    }

    /**
     * DescendingIterator class for descendingIterator
     */
    class DescendingIterator implements Iterator<E>{
        Node<E> curNode;
        public DescendingIterator(){
            curNode = head == null ? null : head.prevArr[0];
        }
        @Override
        public boolean hasNext() {
            return curNode != null;
        }
        @Override
        public E next() {
            E value = curNode.value;
            curNode = curNode.prevArr[0];
            return value;
        }
    }
    //endregion

    /**
     * @return Verbose String representation of SkipList.
     */
    @SuppressWarnings({"StringConcatenationInLoop", "unused"})
    private String toStringVerbose(){
        String result = "Size:" + size;
        int depth = MAX_DEPTH - 1;
        while(head.nextArr[depth] == null){
            depth--;
        }
        String[] rows = new String[depth];
        Arrays.fill(rows,"");

        Node<E> curNode = head;
        String spacer = "";
        while(curNode != null) {
            int max = curNode.distArr[0];
            for(int i : curNode.distArr)
                if(max < i)
                    max = i;
            int maxLen = (curNode.value + "(" + max + ")"+ ",").length();

            String emptyLine = "";
            for(int i = 0; i < maxLen; i++)
                emptyLine += "-";

            int i = depth - 1;
            for(; i >= curNode.distArr.length; i--)
                rows[i] += emptyLine;
            for(; i >= 0; i--) {
                String line = spacer + curNode.value + "(" + curNode.distArr[i] + ")";
                while(line.length() < maxLen)
                    line += "-";
                rows[i] += line;
            }
            spacer = ",";
            curNode = curNode.nextArr[0];
        }
        for(String row : rows) {
            result = "[" + row + "]\n" + result;
        }
        return result;
    }
}