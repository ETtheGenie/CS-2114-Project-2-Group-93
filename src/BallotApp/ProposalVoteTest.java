package BallotApp;

import static org.junit.Assert.*;
import org.junit.Test;

/** Checks proposal ballot normalization, accessors, equality, and bad data. */
public class ProposalVoteTest
{
    /**
     * The record contains a proposal ID, a yes/no answer, and voter details.
     */
    @Test
    public void testConstructorAndGetters()
    {
        ProposalVote yes = new ProposalVote(2, true, " X ", "   ");
        ProposalVote no = new ProposalVote(1, false, "Y", "Hall");
        assertEquals(2, yes.getProposalId());
        assertTrue(yes.isYesVote());
        assertEquals("X", yes.getVoterId());
        assertEquals("Unspecified", yes.getLocation());
        assertFalse(no.isYesVote());
        assertEquals("Hall", no.getLocation());
    }


    /** Equality depends only on the case-sensitive voter ID. */
    @Test
    public void testEquals()
    {
        ProposalVote a = new ProposalVote(1, true, "V1", "");
        ProposalVote b = new ProposalVote(2, false, " V1 ", "Hall");
        ProposalVote c = new ProposalVote(1, true, "v1", "");
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(null));
        assertFalse(a.equals("V1"));
        assertFalse(a.equals(new CandidateVote(1, "A", "V1", "")));
    }


    /** Required fields and length limits are checked by the constructor. */
    @Test
    public void testInvalidValues()
    {
        IllegalArgumentException caught = null;
        try
        {
            new ProposalVote(0, true, "V", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new ProposalVote(1, true, null, "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new ProposalVote(1, true, " ", "");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            new ProposalVote(1, true, "V", null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }
}
