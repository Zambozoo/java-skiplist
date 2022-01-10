package util;

import java.util.*;
/*Bi-directional "Tower-Based" SkipList
* Node<F> { F val; Node<F>[] nexts; Node<F>[] prevs; int[] dists;
* TODO: SplitIterator
* TODO: Optimizations in addAll, contains, removeLastOccurrence, Iterators
* TODO: ConcurrentModification Errors
*/

public class SkipList<E> extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    public static void main(String[] args) {
        new SkipList<String>().toArray(new Object[]{});
    }

    //region Helper Classes
    private static class Node<F> {
        F value;
        Node<F>[] nextArr;
        Node<F>[] prevArr;
        int[] distArr;//Distance to next node.

        public Node(F value, int depth) {
            this.value = value;
            prevArr = new Node[depth];
            nextArr = new Node[depth];
            distArr = new int[depth];
        }

        public Node(F value) {
            this(value, Integer.numberOfTrailingZeros(RANDOM.nextInt() << 1));
        }
    }

    class SkipListIterator implements ListIterator<E>{
        Node<E> curNode;
        int index = 0;
        int lastIndex = -1;
        boolean modified = true;
        public SkipListIterator(int index) {
            this.index = index;
            curNode = getNode(index);
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

    private Node<E> head = new Node<>(null, MAX_DEPTH);
    private int size;

    private static final int MAX_DEPTH = 32;
    private static final Random RANDOM = new Random();

    //region Constructor Detail
    /**
     * <p>Constructs an empty list.</p>
     */
    public SkipList() {
        Arrays.fill(head.distArr, 1);
    }

    /**
     * <p>Constructs a list containing the elements of the specified collection, in the order they are returned by the collection's iterator.</p>
     * @param c the collection whose elements are to be placed into this list
     */
    public SkipList(Collection<? extends E> c) {
        addAll(c);
    }
    //endregion


    //region Method Detail
    /**
     * <p>Returns the first element in this list.</p>
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */
    @Override
    public E getFirst() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.nextArr[0].value;
    }

    /**
     * <p>Returns the last element in this list.</p>
     * @return the last element in this list
     * @throws NoSuchElementException if this list is empty
     */
    @Override
    public E getLast() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return head.prevArr[0].value;
    }

    /**
     * <p>Removes and returns the first element from this list.</p>
     * @throws NoSuchElementException if this list is empty
     * @return the first element from this list
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
                n.nextArr[i].prevArr[i] = null;
                head.nextArr[i] = n.nextArr[i];
                head.distArr[i] += n.distArr[i] - 1;
            }
            size--;
            return n.value;
        }
    }

    /**
     * <p>Removes and returns the last element from this list.</p>
     * @throws NoSuchElementException if this list is empty
     * @return the last element from this list
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
                n.prevArr[i].nextArr[i] = null;
                head.prevArr[i] = n.prevArr[i];
                n.prevArr[i].distArr[i] += n.distArr[i] - 1;
            }
            size--;
            return n.value;
        }
    }

    /**
     * <p>Inserts the specified element at the beginning of this list.</p>>
     * @param e the element to add
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
     * <p>Inserts the specified element to the end of this list.</p>>
     * @param e the element to add
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
     * <p>Returns true if this list contains the specified element.
     * More formally, returns true if and only if this list contains at least one element e such that (o==null ? e==null : o.equals(e)).</p>
     * @param o element whose presence in this list is to be tested
     * @return true if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * <p>Returns the number of elements in this list.</p>
     * @return The number of elements in this list
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * <p>Appends the specified element to the end of this list.</p>
     * <p>This method is equivalent to {@link SkipList#addLast(E)}.</p>
     * @param e element to be appended to this list
     * @return true (as specified by {@link java.util.Collection#add(E)})
     */
    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * <p>Removes the first occurrence of the specified element from this list, if it is present.
     * If this list does not contain the element, it is unchanged.
     * More formally, removes the element with the lowest index i such that (o==null ? get(i)==null : o.equals(get(i)))
     * (if such an element exists).
     * Returns true if this list contained the specified element
     * (or equivalently, if this list changed as a result of the call).</p>
     * @param o element to be removed from this list, if present
     * @return true if this list contained the specified element
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
     * <p>Appends all of the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's iterator.
     * The behavior of this operation is undefined if the specified collection is modified
     * while the operation is in progress.
     * (Note that this will occur if the specified collection is this list, and it's nonempty.)</p>
     * @param c collection containing elements to be added to this list
     * @return true if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see AbstractCollection#add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * <p>currently at that position (if any) and any subsequent elements to the right
     * (increases their indices). The new elements will appear in the list in the order
     * that they are returned by the specified collection's iterator.</p>
     * @param index index at which to insert the first element from the specified collection
     * @param c collection containing elements to be added to this list
     * @return true if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: "+ index + ", Size: " + size);
        }
        for(E e : c) {
            add(index++, e);
        }
        return true;
    }

    /**
     * <p>Removes all of the elements from this list.
     * The list will be empty after this call returns.</p>>
     */
    @Override
    public void clear() {
        size = 0;
        Arrays.fill(head.distArr, 1);
        Arrays.fill(head.nextArr, null);
        Arrays.fill(head.prevArr, null);
    }

    // Positional Access Operations
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

    /**
     * <p>Returns the element at the specified position in this list.</p>
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index in out of range (index < 0 || index >= size())</>
     */
    @Override
    public E get(int index) {
        return getNode(index).value;
    }

    /**
     * <p>Replaces the element at the specified position in this list with the specified element.</p>
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    @Override
    public E set(int index, E element) {
        Node<E> n = getNode(index);
        if(n != null) {
            E value = n.value;
            n.value = element;
            return value;
        }
        throw new IndexOutOfBoundsException("Index: "+ index + ", Size: " + size);
    }

    /**
     * <p>Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right
     * (adds one to their indices).</p>>
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     */
    @Override
    public void add(int index, E element) {
        if(index >= 0 && index <= size) {
            Node<E> n =new Node<>(element);
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
            throw new IndexOutOfBoundsException("Index: "+ index + ", Size: " + size);
        }
    }


    /**
     * <p>Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.</p>>
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
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
            throw new IndexOutOfBoundsException("Index: "+ index + ", Size: " + size);
        }
    }

    // Search Operations
    /**
     * <p>Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     * More formally, returns the lowest index i such that (o==null ? get(i)==null : o.equals(get(i))),
     * or -1 if there is no such index.</p>
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in this list,
     *          or -1 if this list does not contain the element
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

    /**
     * <p>Returns the index of the last occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     * More formally, returns the highest index i such that (o==null ? get(i)==null : o.equals(get(i))),
     * or -1 if there is no such index.</p>
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in this list,
     *          or -1 if this list does not contain the element
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

    // Queue operations.
    /**
     * <p>Retrieves, but does not remove, the head (first element) of this list.</p>
     * <p>This method is equivalent to {@link SkipList#peekFirst()}.</p>
     * @return the head of this list, or null if this list is empty
     */
    @Override
    public E peek() {
        return peekFirst();
    }

    /**
     * <p>Retrieves, but does not remove, the head (first element) of this list.</p>
     * <p>This method is equivalent to {@link SkipList#getFirst()}.</p>
     * @return the head of this list
     * @throws NoSuchElementException is this list is empty
     */
    @Override
    public E element() {
        return getFirst();
    }

    /**
     * <p>Retrieves and removes the head (first element) of this list.</p>>
     * @return the head of this list, or null if this list is empty
     */
    @Override
    public E poll() {
        return (size == 0) ? null : removeFirst();
    }

    /**
     * <p>Retrieves and removes the head (first element) of this list.</p>>
     * <p>This method is equivalent to {@link SkipList#removeFirst()}.</p>
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * <p>Adds the specified element as the tail (last element) of this list.</p>
     * @param e the element to add
     * @return true (as specified by {@link java.util.Queue#offer(E)}
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }

    // Deque operations
    /**
     * <p>Inserts the specified element at the front of this list.</p>
     * @param e the element to insert
     * @return true (as specified by {@link java.util.Deque#offerFirst(E)}
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * <p>Inserts the specified element at the end of this list.</p>
     * @param e the element to insert
     * @return true (as specified by {@link java.util.Deque#offerLast(E)}
     */
    @Override
    public boolean offerLast(E e) {
        return add(e);
    }

    /**
     * <p>Retrieves, but does not remove, the first element of this list, or returns null if this list is empty.</p>
     * @return the first element of this list, or null if this list is empty
     */
    @Override
    public E peekFirst() {
        return (size == 0) ? null : getFirst();
    }

    /**
     * <p>Retrieves, but does not remove, the last element of this list, or returns null if this list is empty.</p>
     * @return the last element of this list, or null if this list is empty
     */
    @Override
    public E peekLast() {
        return (size == 0) ? null : getLast();
    }

    /**
     * <p>Retrieves and removes the first element of this list, or returns null if this list is empty.</p>
     * @return the first element of this list, or null if this list is empty
     */
    @Override
    public E pollFirst() {
        return (size == 0) ? null : removeFirst();
    }

    /**
     * <p>Retrieves and removes the last element of this list, or returns null if this list is empty.</p>
     * @return the last element of this list, or null if this list is empty
     */
    @Override
    public E pollLast() {
        return (size == 0) ? null : removeLast();
    }

    /**
     * <p>Pushes an element onto the stack represented by this list.
     * In other words, inserts the element at the front of this list.</p>
     * <p>This method is equivalent to {@link SkipList#addFirst(E)}.</p>
     * @param e the element to push
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * <p>Pops an element from the stack represented by this list.
     * In other words, removes and returns the first element of this list.</p>
     * <p>This method is equivalent to {@link SkipList#removeFirst()}.</p>
     * @return the element at the front of this list (which is the top of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * <p>Removes the first occurrence of the specified element in this list
     * (when traversing the list from head to tail).
     * If the list does not contain the element, it is unchanged.</p>
     * @param o element to be removed from this list, if present
     * @return true if the list contained the specified element
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * <p>Removes the last occurrence of the specified element in this list
     * (when traversing the list from head to tail).
     * If the list does not contain the element, it is unchanged.</p>
     * @param o element to be removed from this list, if present
     * @return true if the list contained the specified element
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
     * <p>Returns a list-iterator of the elements in this list
     * (in proper sequence), starting at the specified position in the list.
     * Obeys the general contract of {@link java.util.List#listIterator(int)}.</p>
     * @param index index of the first element to be returned from the list-iterator (by a call to next)
     * @return a ListIterator of the elements in this list (in proper sequence),
     *          starting at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        if(index < 0 || index < size) {
            throw new IndexOutOfBoundsException("Index: "+ index + ", Size: " + size);
        }
        return new SkipListIterator(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    @Override
    public Iterator<E> iterator(){
        return new ForwardIterator();
    }

    /**
     * <p>Returns a shallow copy of this LinkedList. (The elements themselves are not cloned.)</p>
     * @return a shallow copy of this LinkedList instance
     * @see java.lang.Cloneable
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
     * <p>Returns an array containing all of the elements in this list in proper sequence
     * (from first to last element).</p>
     * <p>The returned array will be "safe" in that no references to it are maintained by this list.
     * (In other words, this method must allocate a new array).
     * The caller is thus free to modify the returned array.</p>
     * <p>This method acts as bridge between array-based and collection-based APIs.</p>
     * @return an array containing all of the elements in this list in proper sequence
     * @see java.util.Arrays#asList(Object[])
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
     * <p>Returns an array containing all of the elements in this list in proper sequence
     * (from first to last element); the runtime type of the returned array is that of the specified array.
     * If the list fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the specified array and the size of this list.</p>
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list),
     * the element in the array immediately following the end of the list is set to null.
     * (This is useful in determining the length of the list only if the caller knows
     * that the list does not contain any null elements.)</p>
     *
     * <p>Like the {@link SkipList#toArray()} method, this method acts as bridge between array-based and collection-based APIs.
     * Further, this method allows precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.</p>
     *
     * <p>Suppose x is a list known to contain only strings.
     * The following code can be used to dump the list into a newly allocated array of String:</p>
     * <pre><code>String[] y = x.toArray(new String[0]);</code></pre>
     * <p>Note that toArray(new Object[0]) is identical in function to toArray().</p>
     *
     * @param a the array into which the elements of the list are to be stored, if it is big enough;
     *          otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype
     *          of the runtime type of every element in this list
     * @throws NullPointerException if the specified array is null
     */
    @Override
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

    //Serialize
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        for (Node<E> x = head.nextArr[0]; x != null; x = x.nextArr[0])
            s.writeObject(x.value);
    }

    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for (int i = 0; i < size; i++)
            add((E)s.readObject());
    }

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