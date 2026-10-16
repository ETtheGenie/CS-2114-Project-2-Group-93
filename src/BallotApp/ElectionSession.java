
package BallotApp;

import java.util.ArrayList;
import java.util.List;

/**
 * One named election session containing candidate contests and proposals.
 * Contest and proposal IDs are fixed, one-based positions in their lists. The
 * session creates contests and routes votes and corrections to them.
 */
public class ElectionSession
{
    private String name;
    private ArrayList<CandidateContest> candidateContests;
    private ArrayList<ProposalContest> proposals;

    /**
     * Creates an empty election session.
     *
     * @param name
     *            session name (maximum 60 characters)
     * @throws IllegalArgumentException
     *             if the name is invalid
     */
    public ElectionSession(String name)
    {
        this.name =
            InputParser.parseRequiredText(name, InputParser.MAX_NAME_LENGTH);
        candidateContests = new ArrayList<CandidateContest>();
        proposals = new ArrayList<ProposalContest>();
    }


    /**
     * Adds a candidate contest using plurality voting.
     *
     * @param position
     *            name of the position
     * @param names
     *            candidate names
     * @return the new contest
     * @throws IllegalArgumentException
     *             for invalid setup or too many contests
     */
    public
        CandidateContest
        addCandidateContest(String position, List<String> names)
    {
        return addCandidateContest(position, names, 1);
    }


    /**
     * Adds a yes/no proposal and assigns its next one-based ID.
     *
     * @param question
     *            proposal question (maximum 200 characters)
     * @return the new proposal
     * @throws IllegalArgumentException
     *             for an invalid question or too many proposals
     */
    public ProposalContest addProposal(String question)
    {
        if (proposals.size() >= InputParser.MAX_PROPOSALS)
        {
            throw new IllegalArgumentException(
                "Maximum number of proposals reached.");
        }

        ProposalContest proposal =
            new ProposalContest(proposals.size() + 1, question);
        proposals.add(proposal);
        return proposal;
    }


    /**
     * Finds a candidate contest by its one-based ID.
     *
     * @param id
     *            candidate contest ID
     * @return the matching contest
     * @throws IllegalArgumentException
     *             if the ID does not exist
     */
    public CandidateContest getCandidateContest(int id)
    {
        if (id < 1 || id > candidateContests.size())
        {
            throw new IllegalArgumentException(
                "Invalid candidate contest ID: " + id);
        }
        return candidateContests.get(id - 1);
    }


    /**
     * Finds a proposal by its one-based ID.
     *
     * @param id
     *            proposal ID
     * @return the matching proposal
     * @throws IllegalArgumentException
     *             if the ID does not exist
     */
    public ProposalContest getProposal(int id)
    {
        if (id < 1 || id > proposals.size())
        {
            throw new IllegalArgumentException("Invalid proposal ID: " + id);
        }
        return proposals.get(id - 1);
    }


    /**
     * Returns a copy of the candidate contest list in ID order. Changing the
     * list does not change which contests belong to the session.
     *
     * @return copy of candidate contests
     */
    public List<CandidateContest> getCandidateContests()
    {
        return new ArrayList<CandidateContest>(candidateContests);
    }


    /**
     * Returns a copy of the proposal list in ID order.
     *
     * @return copy of proposals
     */
    public List<ProposalContest> getProposals()
    {
        return new ArrayList<ProposalContest>(proposals);
    }


    /**
     * Records one candidate choice in a plurality or majority contest.
     * Candidate names are matched ignoring case and stored canonically.
     *
     * @param id
     *            candidate contest ID
     * @param candidate
     *            candidate name entered by the voter
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @return the recorded vote
     * @throws IllegalArgumentException
     *             if the vote or contest is invalid
     */
    public CandidateVote recordCandidateVote(
        int id,
        String candidate,
        String voterId,
        String location)
    {
        CandidateContest contest = getCandidateContest(id);
        if (contest.getVotingMethod() == 3)
        {
            throw new IllegalArgumentException(
                "Ranked-choice contests require a ranked ballot.");
        }

        String validName = contest.resolveCandidateName(candidate);
        CandidateVote vote =
            new CandidateVote(id, validName, voterId, location);
        contest.recordVote(vote);
        return vote;
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
     * @return the recorded vote
     * @throws IllegalArgumentException
     *             if the vote or proposal is invalid
     */
    public
        ProposalVote
        recordProposalVote(int id, boolean yes, String voterId, String location)
    {
        ProposalContest proposal = getProposal(id);
        ProposalVote vote = new ProposalVote(id, yes, voterId, location);
        proposal.recordVote(vote);
        return vote;
    }


    /**
     * Removes a candidate ballot with the given voter ID.
     *
     * @param id
     *            candidate contest ID
     * @param voterId
     *            voter ID to remove (trimmed, case-sensitive)
     * @return true if removed; false if no matching vote exists
     * @throws IllegalArgumentException
     *             if the contest or voter ID is invalid
     */
    public boolean correctCandidateVote(int id, String voterId)
    {
        return getCandidateContest(id).removeVote(voterId);
    }


    /**
     * Removes a proposal ballot with the given voter ID.
     *
     * @param id
     *            proposal ID
     * @param voterId
     *            voter ID to remove (trimmed, case-sensitive)
     * @return true if removed; false if no matching vote exists
     * @throws IllegalArgumentException
     *             if the proposal or voter ID is invalid
     */
    public boolean correctProposalVote(int id, String voterId)
    {
        return getProposal(id).removeVote(voterId);
    }


    /**
     * Returns the validated name of this session.
     *
     * @return session name
     */
    public String getName()
    {
        return name;
    }


    /**
     * Adds a candidate contest using the selected voting method.
     *
     * @param position
     *            name of the position
     * @param names
     *            candidate names
     * @param method
     *            1 = plurality, 2 = majority required, 3 = ranked choice
     * @return the new contest
     * @throws IllegalArgumentException
     *             for invalid setup or too many contests
     */
    public
        CandidateContest
        addCandidateContest(String position, List<String> names, int method)
    {
        if (candidateContests.size() >= InputParser.MAX_CONTESTS)
        {
            throw new IllegalArgumentException(
                "Maximum number of candidate contests reached.");
        }

        CandidateContest contest = new CandidateContest(
            candidateContests.size() + 1,
            position,
            names,
            method);
        candidateContests.add(contest);
        return contest;
    }


    /**
     * Records a complete ranked ballot in a ranked-choice contest. Each
     * candidate must be ranked exactly once.
     *
     * @param id
     *            candidate contest ID
     * @param rankings
     *            names from first choice to last choice
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @return the recorded ballot
     * @throws IllegalArgumentException
     *             for invalid rankings or duplicate ID
     */
    public CandidateVote recordCandidateVote(
        int id,
        List<String> rankings,
        String voterId,
        String location)
    {
        CandidateContest contest = getCandidateContest(id);
        if (contest.getVotingMethod() != 3)
        {
            throw new IllegalArgumentException(
                "This contest accepts only a single candidate choice.");
        }

        List<String> validRankings =
            InputParser.parseRankedChoices(rankings, contest.getCandidates());
        CandidateVote vote =
            new CandidateVote(id, validRankings, voterId, location);
        contest.recordVote(vote);
        return vote;
    }
}
