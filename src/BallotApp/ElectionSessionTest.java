package BallotApp;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Verifies the session's contest management and vote/correction routing. */
public class ElectionSessionTest
{
    /** Each contest/proposal is assigned a separate one-based ID. */
    @Test
    public void testSetupAndGetters()
    {
        ElectionSession s = new ElectionSession(" Club ");
        assertEquals("Club", s.getName());
        assertTrue(s.getCandidateContests().isEmpty());
        assertTrue(s.getProposals().isEmpty());
        CandidateContest first =
            s.addCandidateContest("President", Arrays.asList("A", "B"));
        CandidateContest second =
            s.addCandidateContest("Secretary", Arrays.asList("C"), 2);
        ProposalContest proposal = s.addProposal("Approve?");
        assertEquals(1, first.getContestId());
        assertEquals(2, second.getContestId());
        assertEquals(2, second.getVotingMethod());
        assertSame(first, s.getCandidateContest(1));
        assertSame(proposal, s.getProposal(1));
        List<CandidateContest> copy = s.getCandidateContests();
        copy.clear();
        assertEquals(2, s.getCandidateContests().size());
        List<ProposalContest> proposalCopy = s.getProposals();
        proposalCopy.clear();
        assertEquals(1, s.getProposals().size());
    }


    /** Normal voting and corrections preserve independent contest records. */
    @Test
    public void testVoteRouting()
    {
        ElectionSession s = new ElectionSession("Election");
        s.addCandidateContest("Lead", Arrays.asList("Alice", "Bob"));
        s.addProposal("Approve?");
        CandidateVote c = s.recordCandidateVote(1, "alice", " V1 ", "");
        ProposalVote p = s.recordProposalVote(1, true, "V1", "Hall");
        assertEquals("Alice", c.getCandidateName());
        assertEquals("V1", c.getVoterId());
        assertTrue(p.isYesVote());
        assertEquals(1, s.getCandidateContest(1).getVoteCount());
        assertEquals(1, s.getProposal(1).getVoteCount());
        assertFalse(s.correctCandidateVote(1, "Missing"));
        assertTrue(s.correctCandidateVote(1, " V1 "));
        assertFalse(s.correctCandidateVote(1, "V1"));
        assertEquals(1, s.getProposal(1).getVoteCount());
        assertTrue(s.correctProposalVote(1, "V1"));
        assertFalse(s.correctProposalVote(1, "V1"));
    }


    /** Ranked-choice voting only belongs in ranked candidate contests. */
    @Test
    public void testRankedRouting()
    {
        ElectionSession s = new ElectionSession("Election");
        s.addCandidateContest("Rank", Arrays.asList("Alice", "Bob"), 3);
        s.addCandidateContest("Single", Arrays.asList("A", "B"));
        CandidateVote vote =
            s.recordCandidateVote(1, Arrays.asList("bob", "alice"), "V", "");
        assertEquals(Arrays.asList("Bob", "Alice"), vote.getRankedChoices());
        IllegalArgumentException caught = null;
        try
        {
            s.recordCandidateVote(1, "Bob", "Bad", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.recordCandidateVote(2, Arrays.asList("A", "B"), "Bad", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.recordCandidateVote(1, Arrays.asList("Bob", "Bob"), "Bad", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /**
     * Invalid IDs, duplicate votes, or invalid session names must be rejected.
     */
    @Test
    public void testInvalidInputs()
    {
        IllegalArgumentException caught = null;
        try
        {
            new ElectionSession(" ");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        ElectionSession s = new ElectionSession("Valid");
        caught = null;
        try
        {
            s.getCandidateContest(0);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.getProposal(1);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.addCandidateContest("Bad", Arrays.asList("A", "A"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.addProposal("");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(0, s.getCandidateContests().size());
        assertEquals(0, s.getProposals().size());
        s.addCandidateContest("Lead", Arrays.asList("A"));
        s.recordCandidateVote(1, "A", "V", "");
        caught = null;
        try
        {
            s.recordCandidateVote(1, "A", "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(1, s.getCandidateContest(1).getVoteCount());
    }


    /**
     * Hard session capacity limits reject additional entries without mutation.
     */
    @Test
    public void testCapacityLimits()
    {
        ElectionSession s = new ElectionSession("Election");
        for (int i = 0; i < 20; i++)
        {
            s.addCandidateContest("Position " + i, Arrays.asList("A"));
            s.addProposal("Proposal " + i);
        }
        IllegalArgumentException caught = null;
        try
        {
            s.addCandidateContest("Extra", Arrays.asList("A"));
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            s.addProposal("Extra");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        assertEquals(20, s.getCandidateContests().size());
        assertEquals(20, s.getProposals().size());
    }
}
