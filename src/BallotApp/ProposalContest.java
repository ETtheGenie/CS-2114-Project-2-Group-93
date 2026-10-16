
package BallotApp;

import java.util.ArrayList;
import java.util.List;

/**
 * One yes/no proposal within an election session. Stores ProposalVote objects
 * in an ElectionChain. Each voter ID may appear only once in this proposal. The
 * proposal passes only when yes votes strictly exceed no votes; a tie does not
 * pass.
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
     *             if the ID or question is invalid
     */
    public ProposalContest(int id, String question)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException("Proposal ID must be positive.");
        }

        proposalId = id;
        this.question = InputParser
            .parseRequiredText(question, InputParser.MAX_PROPOSAL_LENGTH);
        votes = new ElectionChain<ProposalVote>();
    }


    /**
     * Returns the proposal ID.
     *
     * @return one-based proposal ID
     */
    public int getProposalId()
    {
        return proposalId;
    }


    /**
     * Returns the proposal question.
     *
     * @return proposal question
     */
    public String getQuestion()
    {
        return question;
    }


    /**
     * Records a vote for this proposal. A voter ID can only be used once.
     * Invalid votes are rejected without changing the stored votes.
     *
     * @param vote
     *            vote to record
     * @throws IllegalArgumentException
     *             if the vote is null, belongs to a different proposal, or has
     *             an already-used voter ID
     */
    public void recordVote(ProposalVote vote)
    {
        if (vote == null)
        {
            throw new IllegalArgumentException("Vote cannot be null.");
        }
        if (vote.getProposalId() != proposalId)
        {
            throw new IllegalArgumentException(
                "Vote belongs to another proposal.");
        }
        if (votes.contains(vote))
        {
            throw new IllegalArgumentException("Voter ID already recorded.");
        }

        votes.add(vote);
    }


    /**
     * Removes a vote by its trimmed, case-sensitive voter ID.
     *
     * @param voterId
     *            voter ID to remove
     * @return true if a vote was removed, false if none matched
     * @throws IllegalArgumentException
     *             if the voter ID is invalid
     */
    public boolean removeVote(String voterId)
    {
        String id = InputParser
            .parseRequiredText(voterId, InputParser.MAX_VOTER_ID_LENGTH);

        // ProposalVote.equals() compares only voter IDs, so the yes/no
        // value and location of this search vote do not matter.
        ProposalVote searchVote = new ProposalVote(proposalId, false, id, "");
        return votes.remove(searchVote);
    }


    /**
     * Recounts the yes votes from the stored ballots.
     *
     * @return number of yes votes
     */
    public int getYesCount()
    {
        int count = 0;
        for (int i = 0; i < votes.size(); i++)
        {
            if (votes.get(i).isYesVote())
            {
                count++;
            }
        }
        return count;
    }


    /**
     * Recounts the no votes from the stored ballots.
     *
     * @return number of no votes
     */
    public int getNoCount()
    {
        int count = 0;
        for (int i = 0; i < votes.size(); i++)
        {
            if (!votes.get(i).isYesVote())
            {
                count++;
            }
        }
        return count;
    }


    /**
     * Returns the number of stored votes.
     *
     * @return vote count
     */
    public int getVoteCount()
    {
        return votes.size();
    }


    /**
     * Returns a separate list of the stored votes for saving an election.
     * Changing this list does not change the votes in the proposal.
     *
     * @return copy of votes, newest first
     */
    public List<ProposalVote> getVotes()
    {
        List<ProposalVote> copy = new ArrayList<ProposalVote>();
        for (int i = 0; i < votes.size(); i++)
        {
            copy.add(votes.get(i));
        }
        return copy;
    }


    /**
     * Builds the results for this proposal. Yes must exceed no to pass. Reports
     * "No votes recorded" if the proposal is empty.
     *
     * @return formatted results with vote counts and outcome
     */
    public String getResultSummary()
    {
        String result = "Proposal " + proposalId + ": " + question;

        if (votes.isEmpty())
        {
            return result + "\nNo votes recorded";
        }

        int yes = getYesCount();
        int no = getNoCount();
        result += "\nYes: " + yes + "\nNo: " + no;

        if (yes > no)
        {
            result += "\nResult: Passed";
        }
        else
        {
            result += "\nResult: Not passed";
        }
        return result;
    }
}
