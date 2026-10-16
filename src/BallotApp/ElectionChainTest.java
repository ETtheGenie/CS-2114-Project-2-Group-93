package BallotApp;

import static org.junit.Assert.*;
import org.junit.Test;

/** Tests generic head insertion, lookup, removal, counts, and edge cases. */
public class ElectionChainTest
{
    /** Adds entries to the head and returns the correct newest-first order. */
    @Test
    public void testAddGetAndSize()
    {
        ElectionChain<String> chain = new ElectionChain<String>();
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
        chain.add("A");
        chain.add("B");
        chain.add("C");
        assertEquals(3, chain.size());
        assertFalse(chain.isEmpty());
        assertEquals("C", chain.get(0));
        assertEquals("B", chain.get(1));
        assertEquals("A", chain.get(2));
    }


    /**
     * Duplicate entries are valid; remove removes exactly one matching entry.
     */
    @Test
    public void testCountContainsAndDuplicates()
    {
        ElectionChain<String> chain = new ElectionChain<String>();
        assertFalse(chain.contains("A"));
        assertEquals(0, chain.count("A"));
        chain.add("A");
        chain.add("B");
        chain.add("A");
        assertTrue(chain.contains(new String("A")));
        assertFalse(chain.contains("Z"));
        assertEquals(2, chain.count("A"));
        assertEquals(1, chain.count("B"));
        assertEquals(0, chain.count("Z"));
        assertTrue(chain.remove("A"));
        assertEquals(1, chain.count("A"));
        assertEquals(2, chain.size());
        assertFalse(chain.remove("Z"));
        assertEquals(2, chain.size());
    }


    /** Removing first, interior, last, and sole nodes preserves links. */
    @Test
    public void testRemovePositions()
    {
        ElectionChain<Integer> chain = new ElectionChain<Integer>();
        assertFalse(chain.remove(1));
        
        chain.add(1);
        chain.add(2);
        chain.add(3);
        chain.add(4);
        assertTrue(chain.remove(3));
        assertEquals(Integer.valueOf(4), chain.get(0));
        assertEquals(Integer.valueOf(2), chain.get(1));
        assertTrue(chain.remove(4));
        
        assertTrue(chain.remove(2));
        assertFalse(chain.remove(2));
        
        assertTrue(chain.remove(1));
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
        
    }


    /** Clear resets the chain, including when already empty. */
    @Test
    public void testClear()
    {
        ElectionChain<String> chain = new ElectionChain<String>();
        chain.clear();
        chain.add("A");
        chain.add("B");
        chain.clear();
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
        chain.add("C");
        assertEquals("C", chain.get(0));
    }


    /** Null queries must throw, rather than changing the stored entries. */
    @Test
    public void testNullRejection()
    {
        ElectionChain<String> chain = new ElectionChain<String>();
        chain.add("A");
        IllegalArgumentException caught = null;
        try
        {
            chain.add(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            chain.remove(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            chain.contains(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            chain.count(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(1, chain.size());
    }


    /** Negative and out-of-range indices throw IndexOutOfBoundsException. */
    @Test
    public void testInvalidIndex()
    {
        ElectionChain<Integer> chain = new ElectionChain<Integer>();
        chain.add(10);
        IndexOutOfBoundsException caught = null;
        try
        {
            chain.get(-1);
        }
        catch (IndexOutOfBoundsException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            chain.get(1);
        }
        catch (IndexOutOfBoundsException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(Integer.valueOf(10), chain.get(0));
    }
}
