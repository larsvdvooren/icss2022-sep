package nl.han.ica.datastructures;

public class HANQueue<T> implements IHANQueue<T> {

    private final HANLinkedList<T> hanLinkedList;

    public HANQueue() {
        hanLinkedList = new HANLinkedList();
    }

    // Removes all elements from the queue
    @Override
    public void clear() {
        hanLinkedList.clear();
    }

    // Returns true if the queue is empty
    @Override
    public boolean isEmpty() {
        // if size is 0, the queue is empty
        return hanLinkedList.getSize() == 0;
    }

    // Adds value T to the back of the queue
    @Override
    public void enqueue(T value) {
        // insert value at the end of the linked list
        hanLinkedList.insert(hanLinkedList.getSize(), value);
    }

    // Dequeues value at the front of the queue
    @Override
    public T dequeue() {
        // get the first value
        T value = hanLinkedList.getFirst();
        // remove the first value
        hanLinkedList.removeFirst();
        // return the value
        return value;
    }

    // Returns value at the front of the queue without removing it
    @Override
    public T peek() {
        return hanLinkedList.getFirst();
    }

    // Returns the size of the queue
    @Override
    public int getSize() {
        return hanLinkedList.getSize();
    }
}
