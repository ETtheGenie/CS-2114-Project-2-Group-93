package BallotApp;

import java.util.Iterator;

/**
 * Generic, singly-linked collection that permits duplicate nonnull items.
 * Additions occur at the head (newest first). Searching and removal use
 * equals(), not identity. No election or ballot rules appear in this reusable
 * data structure. Iteration is read-only.
 *
 * @param <T>
 *            element type; not restricted to any vote class
 */
public class ElectionChain<T>
{

    private Node<T> head;
    private int numberOfEntries;

    /**
     * Creates an empty chain.
     */
    public ElectionChain()
    {
        head = null;
        numberOfEntries = 0;
    }


    /**
     * Adds an entry at the head of the chain. O(1).
     *
     * @param entry
     *            entry to add
     * @throws IllegalArgumentException
     *             if entry is null
     */
    public void add(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Entry cannot be null.");
        }

        head = new Node<T>(entry, head);
        numberOfEntries++;
    }


    /**
     * Removes the first entry equal to the given entry. O(N). Other entries,
     * including duplicates, remain in the chain.
     *
     * @param entry
     *            entry to find and remove
     * @return true if a matching entry was removed; false if not found
     * @throws IllegalArgumentException
     *             if entry is null
     */
    public boolean remove(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Entry cannot be null.");
        }

        if (head == null)
        {
            return false;
        }

        if (head.getData().equals(entry))
        {
            head = head.getNext();
            numberOfEntries--;
            return true;
        }

        Node<T> current = head;
        while (current.getNext() != null)
        {
            if (current.getNext().getData().equals(entry))
            {
                current.setNext(current.getNext().getNext());
                numberOfEntries--;
                return true;
            }
            current = current.getNext();
        }

        return false;
    }


    /**
     * Checks whether the chain contains an entry equal to the given entry.
     * O(N).
     *
     * @param entry
     *            entry to search for
     * @return true if an equal entry exists; false otherwise
     * @throws IllegalArgumentException
     *             if entry is null
     */
    public boolean contains(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Entry cannot be null.");
        }

        Node<T> current = head;
        while (current != null)
        {
            if (current.getData().equals(entry))
            {
                return true;
            }
            current = current.getNext();
        }

        return false;
    }


    /**
     * Counts every entry equal to the given entry, including duplicates. O(N).
     *
     * @param entry
     *            entry to count
     * @return number of matching entries
     * @throws IllegalArgumentException
     *             if entry is null
     */
    public int count(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Entry cannot be null.");
        }

        int matches = 0;
        Node<T> current = head;
        while (current != null)
        {
            if (current.getData().equals(entry))
            {
                matches++;
            }
            current = current.getNext();
        }

        return matches;
    }


    /**
     * Retrieves the entry at the given zero-based index. Index 0 is the newest
     * entry, because entries are added at the head. O(N).
     *
     * @param index
     *            index of the entry to retrieve
     * @return the entry at the requested index
     * @throws IndexOutOfBoundsException
     *             if index is not in the chain
     */
    public T get(int index)
    {
        if (index < 0 || index >= numberOfEntries)
        {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++)
        {
            current = current.getNext();
        }

        return current.getData();
    }


    /**
     * Returns the number of entries in the chain. O(1).
     *
     * @return chain size
     */
    public int size()
    {
        return numberOfEntries;
    }


    /**
     * Checks whether the chain is empty. O(1).
     *
     * @return true if the chain has no entries
     */
    public boolean isEmpty()
    {
        return numberOfEntries == 0;
    }


    /**
     * Removes all entries from the chain. O(1).
     */
    public void clear()
    {
        head = null;
        numberOfEntries = 0;
    }
}
