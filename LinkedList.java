package editor;

import java.util.Iterator;

import javafx.scene.text.Text;

class LinkedList<T> implements Iterable<T> {

    class LinkedListIterator implements Iterator<T> {
        private Node k;

        LinkedListIterator() {
            //k = frontSent.next;
            k = frontSent;
        }

        public boolean hasNext() {
            //return (k != backSent);
            return (k.next != backSent);
        }

        public T next() {
            if (hasNext()) {
                //T rv = k.item;
                //k = k.next;
                //return rv;
                k = k.next;
                return k.item;
            }
            return null;
        }

        boolean isNextCursorNode() {
            //if (k == cursor.next) {
            if (k == cursor) {
                return true;
            }
            return false;
        }

        Node currentNode() {
            return k;
        }
    }

    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    class Node {
        public T item;
        public Node prev;
        public Node next;

        public Node(T i, Node p, Node n) {
            item = i;
            prev = p;
            next = n;
        }
    }
    /**
     * Creates a front sentinel and a back sentinel
     * and instance variable size
     */
    private Node frontSent;
    private Node backSent;
    private int size;
    Node cursor;

    /**
     * Creates an empty Linked List
     */
    LinkedList() {
        size = 0;
        frontSent = new Node(null, null, null);
        backSent = new Node(null, null, null);
        frontSent.next = backSent;
        backSent.prev = frontSent;
        backSent.next = frontSent;
        cursor = backSent;
    }

    /**
     * Returns true if deque is empty, otherwise returns false
     */
    boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of items in the deque
     */
    int size() {
        return size;
    }

    void add(T i) {
        size += 1;
        Node previousNode = cursor.prev;
        Node nextNode = cursor;
        Node addedNode = new Node(i, previousNode, nextNode);
        previousNode.next = addedNode;
        nextNode.prev = addedNode;
    }

    T remove() {
        if (cursor.prev == frontSent) {
            return null;
        }
        size -= 1;
        Node currentNode = cursor;
        Node removedNode = cursor.prev;
        Node newPreviousNode = cursor.prev.prev;
        currentNode.prev = newPreviousNode;
        newPreviousNode.next = currentNode;
        return removedNode.item;
    }

    void insertNode(Node n) {
        size += 1;
        Node previousNode = cursor.prev;
        Node nextNode = cursor;
        previousNode.next = n;
        nextNode.prev = n;
        n.prev = previousNode;
        n.next = nextNode;
    }

    Node removeNode() {
        if (cursor.prev == frontSent) {
            System.out.println("Why is this happening");
            return null;
        }
        size -= 1;
        Node currentNode = cursor;
        Node removedNode = cursor.prev;
        Node newPreviousNode = cursor.prev.prev;
        currentNode.prev = newPreviousNode;
        newPreviousNode.next = currentNode;
        return removedNode;
    }

    void moveCursorRight() {
        if (cursor != backSent) {
            cursor = cursor.next;
        }
    }

    void moveCursorLeft() {
        if (cursor.prev != frontSent) {
            cursor = cursor.prev;
        }
    }

    void setCursorNode(Node n) {
        cursor = n;
    }

    T getCurrentItem() {
        return cursor.item;
    }

    void moveCursorToBack() {
        cursor = backSent;
    }

    T getBack() {
        if (backSent.prev != frontSent) {
            return backSent.prev.item;
        }
        return null;
    }
}
