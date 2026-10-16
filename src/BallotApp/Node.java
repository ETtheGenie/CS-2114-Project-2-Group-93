package BallotApp;

/**
 * Represents one node in a singly linked chain.
 * Each node stores data and a reference to the next node.
 *
 * @param <T> type of data stored in the node
 * 
 * @author ethang06
 * @version 2026.16.10
 * 
 */
public class Node<T> {

    private T data;
    private Node<T> next;

    /**
     * Creates a node with the given data and no next node.
     *
     * @param data data to store
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
    }

    /**
     * Creates a node with the given data and next node.
     *
     * @param data data to store
     * @param next reference to the next node, or null
     */
    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    /**
     * Returns the data stored in this node.
     *
     * @return the node's data
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the next node in the chain.
     *
     * @return the next node, or null if this is the last node
     */
    public Node<T> getNext() {
        return next;
    }

    /**
     * Changes the reference to the next node.
     *
     * @param next the new next node, or null
     */
    public void setNext(Node<T> next) {
        this.next = next;
    }
}
