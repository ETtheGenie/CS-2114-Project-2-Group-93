package BallotApp;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Tests candidate ballot fields, equality, defensive copying, and errors. */
public class CandidateVoteTest
{
    /** A single choice stores normalized values and one preference. */
    @Test
    public void testSingleChoice()
    {
        CandidateVote vote = new CandidateVote(2, " Alice ", " V1 ", " ");
        assertEquals(2, vote.getContestId());
        assertEquals("Alice", vote.getCandidateName());
        assertEquals("V1", vote.getVoterId());
        assertEquals("Unspecified", vote.getLocation());
        assertEquals(Arrays.asList("Alice"), vote.getRankedChoices());
    }


    /** Ranked choices are stored independently from the original list. */
    @Test
    public void testRankingsAndDefensiveCopy()
    {
        List<String> names =
            new ArrayList<String>(Arrays.asList(" Alice ", " Bob "));
        CandidateVote vote = new CandidateVote(1, names, "V2", "Hall");
        names.clear();
        assertEquals("Alice", vote.getCandidateName());
        assertEquals("Hall", vote.getLocation());
        List<String> obtained = vote.getRankedChoices();
        obtained.clear();
        assertEquals(Arrays.asList("Alice", "Bob"), vote.getRankedChoices());
    }


    /**
     * IDs compare by exact capitalization, independent of choices and contest.
     */
    @Test
    public void testEquals()
    {
        CandidateVote a = new CandidateVote(1, "Alice", " X ", "");
        CandidateVote b = new CandidateVote(2, "Bob", "X", "Library");
        CandidateVote c = new CandidateVote(1, "Alice", "x", "");
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(null));
        assertFalse(a.equals("X"));
        assertFalse(a.equals(new ProposalVote(1, true, "X", "")));
    }


    /** Invalid single-choice ballot values throw IllegalArgumentException. */
    @Test
    public void testInvalidSingleChoice()
    {
        IllegalArgumentException caught = null;
        try
        {
            new CandidateVote(0, "Alice", "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateVote(1, " ", "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateVote(1, "Alice", " ", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateVote(1, "Alice", "V", null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Invalid ranked ballots throw before recording anything. */
    @Test
    public void testInvalidRanking()
    {
        List<List<String>> bad = Arrays.asList(
            new ArrayList<String>(),
            Arrays.asList("Alice", "alice"),
            Arrays.asList("Alice", null));
        for (List<String> ranking : bad)
        {
            IllegalArgumentException caught = null;
            try
            {
                new CandidateVote(1, ranking, "V", "");
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
            new CandidateVote(0, Arrays.asList("A"), "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateVote(1, (List<String>)null, "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        List<String> longRanking = new ArrayList<String>();
        for (int i = 0; i < 21; i++)
        {
            longRanking.add("A" + i);
        }
        caught = null;
        try
        {
            new CandidateVote(1, longRanking, "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new CandidateVote(1, Arrays.asList("A"), "", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }
}
