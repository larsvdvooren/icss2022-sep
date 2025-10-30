package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {

    private HANLinkedList<T> hanLinkedList;

    public HANStack() {
        hanLinkedList = new HANLinkedList<>();
    }

    // Pushes value T to the top of the stack
    @Override
    public void push(T value) {
        hanLinkedList.addFirst(value);
    }

    // Removes value at top of stack and returns it
    @Override
    public T pop() {
            // Get the first element
        T value = hanLinkedList.getFirst();
        // Remove the first element
        hanLinkedList.removeFirst();
        // Return the popped value
        return value;
    }

    // Returns value at the top of the stack without removing it
    @Override
    public T peek() {
        // Return the first element without removing it
        return hanLinkedList.getFirst();
    }
}

