package nl.han.ica.datastructures;

import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private ListNode<T> head;

    // Adds value T to the front of the list
    @Override
    public void addFirst(T value) {
        if (head == null) {
            head = new ListNode<>(value);
        } else {
            ListNode<T> newNode = new ListNode<>(value);
            newNode.setNext(head);
            head = newNode;
        }
    }

    // Removes all elements from the list
    @Override
    public void clear() {
        // Set head to null to clear the list
        head = null;
    }

    // Inserts value T at index position
    @Override
    public void insert(int index, T value) {
        // Get size of the list
        int size = getSize();
        // checks if index is a valid value above or at 0 and below or at size
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", Size: " + size);
        }
        // if index is 0, add to the front
        if (index == 0) {
            addFirst(value);
        } else {
            // Else start at the head node
            ListNode<T> current = head;
            // Traverse to the node before the one we want
            for (int i = 0; i < index - 1; i++) {
                current = current.getNext();
            }
            // Create the new node
            ListNode<T> newNode = new ListNode<>(value);
            // Insert the new node
            newNode.setNext(current.getNext());
            // Link the previous node to the new node
            current.setNext(newNode);
        }
    }

    // Deletes value at position pos
    @Override
    public void delete(int pos) {
        // checks if pos is a valid value above or at 0 and below or at size
        if (pos < 0 || pos > getSize()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + pos + ", Size: " + getSize());
        }
        // if pos == 0, remove the first element
        if (pos == 0) {
            removeFirst();
        } else {
            // Else start at the head node
            ListNode<T> current = head;
            // Traverse to the node before the one we want
            for (int i = 0; i < pos - 1; i++) {
                current = current.getNext();
            }
            // if the next node exists, skip it to delete.
            if (current.getNext() != null) {
                // Link the previous node to the node after skipping the deleted one
                current.setNext(current.getNext().getNext());
            }
        }
    }

    // Returns value T at position pos
    @Override
    public T get(int pos) {
        // checks if pos is a valid value above or at 0 and below or at size
        if (pos < 0 || pos >= getSize()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + pos + ", Size: " + getSize());
        }
        // Start at the head node
        ListNode<T> current = head;
        // Traverse to the node before the one we want
        for (int i = 0; i < pos; i++) {
            // Move to the next node
            current = current.getNext();
        }
        // Return the value at the position
        return current.getValue();
    }

    // Removes the first element from the list
    @Override
    public void removeFirst() {
        // Check if the list is empty
        if (head == null) {
            throw new NoSuchElementException("Cannot remove from empty list");
        }
        // List not empty, so remove the first element
        head = head.getNext();
    }

    // Returns the first element in the list
    @Override
    public T getFirst() {
        // Check if the list is empty
        if (head == null) {;
            throw new NoSuchElementException("Cannot get from empty list");
        }
        // List not empty, return the first elements value
        return head.getValue();
    }


    // Traverses the list to count the number of nodes, minus the head node
    @Override
    public int getSize() {
        int size = 0;
        ListNode<T> current = head;
        // Count nodes until it hits the end of the list
        while (current != null) {
            size++;
            current = current.getNext();
        }
        // subtract 1 for the head node
        size--;
        return size;
    }
}
