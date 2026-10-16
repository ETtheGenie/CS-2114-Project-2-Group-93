package BallotApp;

import java.util.ArrayList;
import java.util.List;

/**
 * One named election session containing candidate contests and proposals.
 * <p>
 * Contest/proposal IDs are fixed one-based list positions. The session owns
 * creation, lookup, and routing for voting and correction; individual contests
 * enforce their own vote rules. No contest deletion is supported.
 * </p>
 */
public class ElectionSession
{

    private String name;
    private ArrayList<CandidateContest> candidateContests;
    private ArrayList<ProposalContest> proposals;

    /**
     * Creates an empty session with a validated, nonblank name (max 60).
     *
     * @param name
     *            election session name
     * @throws IllegalArgumentException
     *             if name is invalid
     */
    public ElectionSession(String name)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Adds a plurality contest with 1-20 distinct candidate names. The original
     * MVP signature defaults to plurality for compatibility.
     *
     * @param position
     *            contest/position name
     * @param names
     *            canonical candidate names
     * @return newly added contest with its one-based ID
     * @throws IllegalArgumentException
     *             for invalid setup or capacity
     */
    public
        CandidateContest
        addCandidateContest(String position, List<String> names)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Adds one yes/no proposal, assigning the next one-based ID.
     *
     * @param question
     *            proposal question (max 200 characters)
     * @return newly created proposal
     * @throws IllegalArgumentException
     *             for an invalid question or capacity
     */
    public ProposalContest addProposal(String question)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Retrieves a candidate contest using its one-based ID.
     *
     * @param id
     *            candidate contest ID
     * @return matching contest
     * @throws IllegalArgumentException
     *             for a nonexistent ID
     */
    public CandidateContest getCandidateContest(int id)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Retrieves a proposal using its one-based ID.
     *
     * @param id
     *            proposal ID
     * @return matching proposal
     * @throws IllegalArgumentException
     *             for a nonexistent ID
     */
    public ProposalContest getProposal(int id)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns a defensive copy of the candidate contest list. Changing the
     * returned list cannot alter session membership.
     *
     * @return shallow copy of contests in ID order
     */
    public List<CandidateContest> getCandidateContests()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns a defensive copy of the proposal list.
     *
     * @return shallow copy of proposals in ID order
     */
    public List<ProposalContest> getProposals()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Records a single-choice candidate ballot in the selected contest.
     * Resolves the canonical candidate name; repeated voter IDs in the same
     * contest are rejected without changing existing votes.
     *
     * @param id
     *            candidate contest ID
     * @param candidate
     *            chosen candidate name
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @return accepted immutable vote record
     * @throws IllegalArgumentException
     *             for invalid data or duplicate ID
     */
    public CandidateVote recordCandidateVote(
        int id,
        String candidate,
        String voterId,
        String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Records a yes/no vote in the selected proposal.
     *
     * @param id
     *            proposal ID
     * @param yes
     *            true for yes, false for no
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @return accepted immutable vote record
     * @throws IllegalArgumentException
     *             for invalid data or duplicate ID
     */
    public
        ProposalVote
        recordProposalVote(int id, boolean yes, String voterId, String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Removes a candidate ballot with the given voter ID, if present.
     *
     * @param id
     *            candidate contest ID
     * @param voterId
     *            voter ID to remove (trimmed, case-sensitive)
     * @return true if exactly one record was removed; otherwise false
     */
    public boolean correctCandidateVote(int id, String voterId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Removes a proposal vote with the given voter ID, if present.
     *
     * @param id
     *            proposal ID
     * @param voterId
     *            voter ID to remove (trimmed, case-sensitive)
     * @return true if exactly one record was removed; otherwise false
     */
    public boolean correctProposalVote(int id, String voterId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the session's validated name.
     *
     * @return session name
     */
    public String getName()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * STRETCH: Adds a contest configured for a specific voting method. The
     * method remains fixed for the contest once votes are recorded.
     *
     * @param position
     *            contest/position name
     * @param names
     *            allowed candidate names
     * @param method
     *            voting method (1 = plurality, 2 = majority, 3 = ranked)
     * @return newly added contest
     * @throws IllegalArgumentException
     *             for invalid setup
     */
    public
        CandidateContest
        addCandidateContest(String position, List<String> names, int method)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * STRETCH: Records an ordered candidate ranking for an IRV contest. Every
     * candidate must appear exactly once in the rankings.
     *
     * @param id
     *            candidate contest ID
     * @param rankings
     *            preferred candidates, first choice first
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @return accepted immutable vote record
     * @throws IllegalArgumentException
     *             for invalid rankings or duplicate ID
     */
    public CandidateVote recordCandidateVote(
        int id,
        List<String> rankings,
        String voterId,
        String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
