package BallotApp;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Tests valid, invalid, and boundary input for every InputParser method. All
 * expected exceptions use explicit try/catch checks.
 */
public class InputParserTest
{
    /** Confirms numbers are trimmed, converted, and range checked. */
    @Test
    public void testNumbers()
    {
        assertEquals(0, InputParser.parseNumberInRange(" 0 ", 0, 20));
        assertEquals(20, InputParser.parseNumberInRange("20", 0, 20));
        assertEquals(-2, InputParser.parseNumberInRange("-2", -5, 5));
        assertEquals(2, InputParser.parseNumberInRange("+2", 1, 5));
        String[] invalid =
            { "", "   ", "abc", "1.0", "2147483648", "-1", "21" };
        for (String s : invalid)
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseNumberInRange(s, 0, 20);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull("Must reject " + s, caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseNumberInRange(null, 0, 20);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseNumberInRange("4", 5, 1);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Both types of election counts cannot be zero together. */
    @Test
    public void testSetupCounts()
    {
        InputParser.validateSetupCounts(1, 0);
        InputParser.validateSetupCounts(0, 1);
        InputParser.validateSetupCounts(20, 20);
        int[][] invalid =
            { { 0, 0 }, { -1, 1 }, { 21, 0 }, { 0, -1 }, { 0, 21 } };
        for (int[] c : invalid)
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.validateSetupCounts(c[0], c[1]);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
    }


    /**
     * Matches names without regard to capitalization and preserves spelling.
     */
    @Test
    public void testChoiceFromList()
    {
        List<String> choices = Arrays.asList("Alice", "Bob");
        assertEquals(
            "Alice",
            InputParser.parseChoiceFromList(" aLiCe ", choices));
        assertEquals("Bob", InputParser.parseChoiceFromList("bob", choices));
        List<List<String>> badLists = Arrays.asList(
            new ArrayList<String>(),
            Arrays.asList("A", "a"),
            Arrays.asList("A", null),
            Arrays.asList("A", " "));
        for (List<String> opts : badLists)
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseChoiceFromList("A", opts);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseChoiceFromList("A", null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        for (String bad : new String[] { "", " ", "Mallory" })
        {
            caught = null;
            try
            {
                InputParser.parseChoiceFromList(bad, choices);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        caught = null;
        try
        {
            InputParser.parseChoiceFromList(null, choices);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Rank choices exactly once and canonicalize each candidate name. */
    @Test
    public void testRankedChoices()
    {
        List<String> candidates = Arrays.asList("Alice", "Bob", "Carol");
        assertEquals(
            Arrays.asList("Carol", "Alice", "Bob"),
            InputParser.parseRankedChoices(
                Arrays.asList("carol", "Alice", "BOB"),
                candidates));
        List<List<String>> invalid = Arrays.asList(
            Arrays.asList("Alice", "Bob"),
            Arrays.asList("Alice", "Alice", "Carol"),
            Arrays.asList("Alice", "Bob", "Unknown"));
        for (List<String> ranking : invalid)
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseRankedChoices(ranking, candidates);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseRankedChoices(null, candidates);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseRankedChoices(Arrays.asList("Alice"), null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseRankedChoices(
                Arrays.asList("Alice"),
                new ArrayList<String>());
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        List<String> oversized = new ArrayList<String>();
        for (int i = 0; i < 21; i++)
        {
            oversized.add("Name" + i);
        }
        caught = null;
        try
        {
            InputParser.parseRankedChoices(oversized, oversized);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Yes and no are the only accepted proposal answers. */
    @Test
    public void testYesNo()
    {
        assertTrue(InputParser.parseYesNo(" YeS "));
        assertFalse(InputParser.parseYesNo("no"));
        for (String bad : new String[] { "", "y", "true", "1" })
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseYesNo(bad);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseYesNo(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Required text trims spaces and rejects invalid lengths. */
    @Test
    public void testRequiredText()
    {
        assertEquals("Club", InputParser.parseRequiredText(" Club ", 4));
        String[] invalid = { "", "   ", "abcdef" };
        for (String bad : invalid)
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseRequiredText(bad, 4);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseRequiredText(null, 4);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseRequiredText("A", 0);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /**
     * Candidate names must be distinct, including case-insensitive comparisons.
     */
    @Test
    public void testCandidateNames()
    {
        assertEquals(
            "Alice",
            InputParser.parseCandidateName(" Alice ", Arrays.asList("Bob")));
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseCandidateName("alice", Arrays.asList("Alice"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseCandidateName("Alice", null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseCandidateName("Alice", Arrays.asList("Bob", null));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        List<String> full = new ArrayList<String>();
        for (int i = 0; i < 20; i++)
        {
            full.add("C" + i);
        }
        caught = null;
        try
        {
            InputParser.parseCandidateName("New", full);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser.parseCandidateName("", Arrays.asList("Bob"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Location supports blank input but rejects null and excessive length. */
    @Test
    public void testLocation()
    {
        assertEquals("Unspecified", InputParser.parseLocation("  "));
        assertEquals("Hall", InputParser.parseLocation(" Hall "));
        assertEquals(
            80,
            InputParser
                .parseLocation(new String(new char[80]).replace('\0', 'x'))
                .length());
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseLocation(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            InputParser
                .parseLocation(new String(new char[81]).replace('\0', 'x'));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /**
     * Save filenames accept safe characters and reject separators and symbols.
     */
    @Test
    public void testSaveName()
    {
        assertEquals(
            "Election 1_A-B",
            InputParser.parseSaveName(" Election 1_A-B "));
        for (String bad : new String[] { ".hidden", "_hidden", "a/b", "a:b", "",
            "!bad" })
        {
            IllegalArgumentException caught = null;
            try
            {
                InputParser.parseSaveName(bad);
            }
            catch (IllegalArgumentException e)
            {
                caught = e;
            }
            assertNotNull(caught);
        }
        IllegalArgumentException caught = null;
        try
        {
            InputParser.parseSaveName(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }
}
