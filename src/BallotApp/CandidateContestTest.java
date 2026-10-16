package BallotApp;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Exercises candidate configuration, vote storage, and all three result rules.
 */
public class CandidateContestTest
{
    /**
     * Default and specified constructors retain setup and use defensive lists.
     */
    @Test
    public void testConstructorsAndGetters()
    {
        List<String> names =
            new ArrayList<String>(Arrays.asList("Alice", "Bob"));
        CandidateContest c = new CandidateContest(2, " President ", names);
        assertEquals(2, c.getContestId());
        assertEquals("President", c.getPosition());
        assertEquals(1, c.getVotingMethod());
        names.clear();
        assertEquals(2, c.getCandidates().size());
        List<String> copy = c.getCandidates();
        copy.clear();
        assertEquals(2, c.getCandidates().size());
        assertEquals("Alice", c.resolveCandidateName(" alice "));
        assertEquals(
            3,
            new CandidateContest(1, "Lead", Arrays.asList("A"), 3)
                .getVotingMethod());
        assertTrue(c.getResultSummary().contains("No votes recorded"));
    }


    /** Invalid constructor arguments cannot establish an election. */
    @Test
    public void testInvalidConfigurations()
    {
        IllegalArgumentException caught = null;
        try
        {
            new CandidateContest(0, "Lead", Arrays.asList("A"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", Arrays.asList("A"), 0);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", Arrays.asList("A"), 4);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", new ArrayList<String>());
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        List<String> longList = new ArrayList<String>();
        for (int i = 0; i < 21; i++)
        {
            longList.add("A" + i);
        }
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", longList);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, "Lead", Arrays.asList("A", "a"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateContest(1, " ", Arrays.asList("A"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /**
     * Counts are recalculated, retaining zero totals and canonical name order.
     */
    @Test
    public void testPluralityTotalsTieAndWinner()
    {
        CandidateContest c = new CandidateContest(
            1,
            "Lead",
            Arrays.asList("Alice", "Bob", "Carol"));
        c.recordVote(new CandidateVote(1, "Alice", "A", ""));
        c.recordVote(new CandidateVote(1, "Bob", "B", ""));
        Map<String, Integer> counts = c.tallyVotes();
        assertEquals(
            Arrays.asList("Alice", "Bob", "Carol"),
            new ArrayList<String>(counts.keySet()));
        assertEquals(Integer.valueOf(0), counts.get("Carol"));
        assertTrue(
            c.getPluralityResultSummary().contains("Tie between: Alice, Bob"));
        c.recordVote(new CandidateVote(1, "Alice", "C", ""));
        assertEquals(3, c.getVoteCount());
        assertTrue(c.getResultSummary().contains("Winner: Alice"));
        List<CandidateVote> copy = c.getVotes();
        assertEquals("C", copy.get(0).getVoterId());
        copy.clear();
        assertEquals(3, c.getVoteCount());
        assertTrue(c.removeVote(" C "));
        assertFalse(c.removeVote("C"));
        assertEquals(2, c.getVoteCount());
        IllegalArgumentException caught = null;
        try
        {
            c.resolveCandidateName("Missing");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Majority requires more than one-half of submitted ballots. */
    @Test
    public void testMajority()
    {
        CandidateContest c =
            new CandidateContest(1, "Lead", Arrays.asList("A", "B", "C"), 2);
        assertTrue(c.getMajorityResultSummary().contains("No votes recorded"));
        c.recordVote(new CandidateVote(1, "A", "1", ""));
        c.recordVote(new CandidateVote(1, "B", "2", ""));
        assertTrue(c.getMajorityResultSummary().contains("No majority winner"));
        c.recordVote(new CandidateVote(1, "A", "3", ""));
        assertTrue(c.getMajorityResultSummary().contains("Winner: A"));
    }


    /**
     * Ranked ballots transfer after an elimination, or report unresolved tie.
     */
    @Test
    public void testRankedTransferAndTie()
    {
        CandidateContest c =
            new CandidateContest(1, "Lead", Arrays.asList("A", "B", "C"), 3);
        assertTrue(
            c.getRankedChoiceResultSummary().contains("No votes recorded"));
        c.recordVote(
            new CandidateVote(1, Arrays.asList("A", "B", "C"), "1", ""));
        c.recordVote(
            new CandidateVote(1, Arrays.asList("A", "B", "C"), "2", ""));
        c.recordVote(
            new CandidateVote(1, Arrays.asList("B", "A", "C"), "3", ""));
        c.recordVote(
            new CandidateVote(1, Arrays.asList("B", "A", "C"), "4", ""));
        c.recordVote(
            new CandidateVote(1, Arrays.asList("C", "B", "A"), "5", ""));
        String result = c.getRankedChoiceResultSummary();
        assertTrue(result.contains("Eliminated: C"));
        assertTrue(result.contains("Round 2:"));
        assertTrue(result.contains("Winner: B"));
        assertEquals(Integer.valueOf(1), c.tallyVotes().get("C"));

        CandidateContest tied =
            new CandidateContest(2, "Lead", Arrays.asList("A", "B", "C"), 3);
        tied.recordVote(
            new CandidateVote(2, Arrays.asList("A", "B", "C"), "1", ""));
        tied.recordVote(
            new CandidateVote(2, Arrays.asList("A", "B", "C"), "2", ""));
        tied.recordVote(
            new CandidateVote(2, Arrays.asList("B", "A", "C"), "3", ""));
        tied.recordVote(
            new CandidateVote(2, Arrays.asList("C", "A", "B"), "4", ""));
        assertTrue(
            tied.getRankedChoiceResultSummary()
                .contains("Unresolved elimination tie"));

        CandidateContest immediate =
            new CandidateContest(3, "Lead", Arrays.asList("A", "B"), 3);
        immediate
            .recordVote(new CandidateVote(3, Arrays.asList("A", "B"), "1", ""));
        assertTrue(
            immediate.getRankedChoiceResultSummary().contains("Winner: A"));
    }


    /** No invalid ballot may affect the stored totals. */
    @Test
    public void testRejectedBallots()
    {
        CandidateContest c =
            new CandidateContest(1, "Lead", Arrays.asList("Alice", "Bob"));
        c.recordVote(new CandidateVote(1, "Alice", "V", ""));
        IllegalArgumentException caught = null;
        try
        {
            c.recordVote(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(new CandidateVote(2, "Alice", "Other", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(new CandidateVote(1, "Bob", "V", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(new CandidateVote(1, "alice", "Other", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(new CandidateVote(1, "Missing", "Other", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(
                new CandidateVote(
                    1,
                    Arrays.asList("Alice", "Bob"),
                    "Other",
                    ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.removeVote(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(1, c.getVoteCount());
    }


    /**
     * A ranked contest rejects incomplete or noncanonical candidate rankings.
     */
    @Test
    public void testRejectedRankings()
    {
        CandidateContest c =
            new CandidateContest(1, "Lead", Arrays.asList("Alice", "Bob"), 3);
        IllegalArgumentException caught = null;
        try
        {
            c.recordVote(new CandidateVote(1, "Alice", "1", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(
                new CandidateVote(1, Arrays.asList("alice", "Bob"), "2", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            c.recordVote(
                new CandidateVote(
                    1,
                    Arrays.asList("Alice", "Unknown"),
                    "3",
                    ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(0, c.getVoteCount());
    }
}
