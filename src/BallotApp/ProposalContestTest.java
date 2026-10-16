package BallotApp;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

/**
 * Tests proposal vote recording, corrections, counting, and result summaries.
 */
public class ProposalContestTest
{
    /** Construction normalizes the question and begins with zero votes. */
    @Test
    public void testConstruction()
    {
        ProposalContest p = new ProposalContest(2, " Approve budget? ");
        assertEquals(2, p.getProposalId());
        assertEquals("Approve budget?", p.getQuestion());
        assertEquals(0, p.getVoteCount());
        assertTrue(p.getResultSummary().contains("No votes recorded"));
        IllegalArgumentException caught = null;
        try
        {
            new ProposalContest(0, "Valid");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new ProposalContest(1, " ");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** Votes remain distinct for different IDs even with matching answers. */
    @Test
    public void testCountsResultsAndCopies()
    {
        ProposalContest p = new ProposalContest(1, "Approve?");
        p.recordVote(new ProposalVote(1, true, "A", ""));
        p.recordVote(new ProposalVote(1, true, "B", ""));
        p.recordVote(new ProposalVote(1, false, "C", ""));
        assertEquals(3, p.getVoteCount());
        assertEquals(2, p.getYesCount());
        assertEquals(1, p.getNoCount());
        assertTrue(p.getResultSummary().contains("Passed"));
        List<ProposalVote> copy = p.getVotes();
        assertEquals("C", copy.get(0).getVoterId());
        copy.clear();
        assertEquals(3, p.getVoteCount());
        assertTrue(p.removeVote(" A "));
        assertEquals(1, p.getYesCount());
        assertTrue(p.getResultSummary().contains("Not passed"));
        assertFalse(p.removeVote("A"));
        assertTrue(p.removeVote("B"));
        assertTrue(p.removeVote("C"));
        assertEquals(0, p.getNoCount());
        assertTrue(p.getResultSummary().contains("No votes recorded"));
    }


    /**
     * Invalid votes and duplicate IDs leave previously stored votes unchanged.
     */
    @Test
    public void testRejectedVotes()
    {
        ProposalContest p = new ProposalContest(1, "Approve?");
        p.recordVote(new ProposalVote(1, true, "A", ""));
        IllegalArgumentException caught = null;
        try
        {
            p.recordVote(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            p.recordVote(new ProposalVote(2, true, "B", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            p.recordVote(new ProposalVote(1, false, "A", ""));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            p.removeVote(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(1, p.getVoteCount());
        assertEquals(1, p.getYesCount());
    }
}
