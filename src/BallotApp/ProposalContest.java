package BallotApp;

import java.util.List;

/**
 * One yes/no proposal within an election session.

 * Stores ProposalVote objects in LinkedChain<ProposalVote>. A given voter ID
 * may appear at most once in this proposal. The proposal passes only when yes
 * votes strictly exceed no votes, so a tie fails.

 */
public class ProposalContest
{

    private int proposalId;
    private String question;
    private ElectionChain<ProposalVote> votes;

    /**
     * Creates a proposal with an empty vote chain.
     *
     * @param id
     *            positive one-based proposal ID
     * @param question
     *            nonblank question of at most 200 characters
     * @throws IllegalArgumentException
     *             for invalid inputs
     */
    public ProposalContest(int id, String question)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the proposal ID.
     *
     * @return one-based proposal ID
     */
    public int getProposalId()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the proposal's question.
     *
     * @return proposal question
     */
    public String getQuestion()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Records an immutable vote matching this proposal, rejecting any duplicate
     * voter ID within the proposal. Rejected operations leave the stored chain
     * unchanged.
     *
     * @param vote
     *            proposed ballot
     * @throws IllegalArgumentException
     *             for invalid or duplicate ballot
     */
    public void recordVote(ProposalVote vote)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Removes a ballot matching the case-sensitive, trimmed voter ID.
     *
     * @param voterId
     *            ID of ballot to remove
     * @return true if a ballot was removed; false if absent
     */
    public boolean removeVote(String voterId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Traverses stored ballots to recalculate the number of yes votes.
     *
     * @return number of yes votes
     */
    public int getYesCount()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Traverses stored ballots to recalculate the number of no votes.
     *
     * @return number of no votes
     */
    public int getNoCount()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the number of stored proposal ballots.
     *
     * @return chain size
     */
    public int getVoteCount()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * STRETCH: Returns a defensive snapshot of all stored proposal votes so
     * ElectionStorage can write the complete session safely.
     *
     * @return copy of stored ballots, newest first
     */
    public List<ProposalVote> getVotes()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Builds the vote-count report: passed if yes > no, otherwise not passed.
     * An empty proposal reports "No votes recorded".
     *
     * @return formatted yes/no counts and outcome
     */
    public String getResultSummary()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
