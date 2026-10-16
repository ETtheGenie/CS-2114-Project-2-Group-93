package BallotApp;

import static org.junit.Assert.*;
import org.junit.Test;

/** Tests both Node constructors and its simple accessor/mutator methods. */
public class NodeTest
{
    /** A new node without a successor has a null next reference. */
    @Test
    public void testSingleNode()
    {
        Node<String> node = new Node<String>("first");
        assertEquals("first", node.getData());
        assertNull(node.getNext());
        node.setNext(new Node<String>("second"));
        assertEquals("second", node.getNext().getData());
        node.setNext(null);
        assertNull(node.getNext());
    }


    /** The second constructor stores both references. */
    @Test
    public void testLinkedConstructor()
    {
        Node<Integer> next = new Node<Integer>(2);
        Node<Integer> first = new Node<Integer>(1, next);
        assertEquals(Integer.valueOf(1), first.getData());
        assertSame(next, first.getNext());
    }
}
