package BallotApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable record of one voter's ballot in a candidate contest. Two
 * CandidateVote objects are equal if their trimmed, case-sensitive voter IDs
 * match. Candidate contests use this rule to prevent duplicate voter IDs.
 */
public final class CandidateVote
{
    private final int contestId;
    private final String candidateName;
    private final String voterId;
    private final String location;

    /**
     * The candidates in preference order. Single-choice ballots have one name.
     * The list is copied when stored and when returned to protect the ballot.
     */
    private List<String> rankedChoices;

    /**
     * Creates a single-choice ballot.
     *
     * Time complexity: O(L), where L is the total length of the supplied
     * text.
     *
     * @param contestId
     *            positive candidate contest ID
     * @param candidateName
     *            selected candidate name
     * @param voterId
     *            required voter ID (up to 40 characters)
     * @param location
     *            optional location (up to 80 characters)
     * @throws IllegalArgumentException
     *             if any field is invalid
     */
    public CandidateVote(
        int contestId,
        String candidateName,
        String voterId,
        String location)
    {
        if (contestId <= 0)
        {
            throw new IllegalArgumentException("Contest ID must be positive.");
        }

        this.contestId = contestId;
        this.candidateName = InputParser
            .parseRequiredText(candidateName, InputParser.MAX_NAME_LENGTH);
        this.voterId = InputParser
            .parseRequiredText(voterId, InputParser.MAX_VOTER_ID_LENGTH);
        this.location = InputParser.parseLocation(location);

        rankedChoices = new ArrayList<String>();
        rankedChoices.add(this.candidateName);
    }


    /**
     * Creates a ranked-choice ballot. Rankings are listed from first to last.
     * The contest is responsible for checking that all available candidates
     * have been ranked.
     *
     * Time complexity: O(K^2 L), where K is the ranking length and L is the
     * maximum candidate name length.
     *
     * @param contestId
     *            positive candidate contest ID
     * @param rankings
     *            ordered candidate names, from most to least preferred
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @throws IllegalArgumentException
     *             if the ballot is invalid
     */
    public CandidateVote(
        int contestId,
        List<String> rankings,
        String voterId,
        String location)
    {
        if (contestId <= 0)
        {
            throw new IllegalArgumentException("Contest ID must be positive.");
        }
        if (rankings == null || rankings.isEmpty()
            || rankings.size() > InputParser.MAX_CANDIDATES)
        {
            throw new IllegalArgumentException(
                "A ballot must rank between 1 and 20 candidates.");
        }

        List<String> cleanedRankings = new ArrayList<String>();
        for (String name : rankings)
        {
            String cleanedName = InputParser
                .parseRequiredText(name, InputParser.MAX_NAME_LENGTH);
            for (String previous : cleanedRankings)
            {
                if (previous.equalsIgnoreCase(cleanedName))
                {
                    throw new IllegalArgumentException(
                        "Each candidate can appear only once in a ranking.");
                }
            }
            cleanedRankings.add(cleanedName);
        }

        this.contestId = contestId;
        this.rankedChoices = cleanedRankings;
        this.candidateName = cleanedRankings.get(0);
        this.voterId = InputParser
            .parseRequiredText(voterId, InputParser.MAX_VOTER_ID_LENGTH);
        this.location = InputParser.parseLocation(location);
    }


    /**
     * Time complexity: O(1).
     *
     * @return the candidate contest ID
     */
    public int getContestId()
    {
        return contestId;
    }


    /**
     * Time complexity: O(1).
     *
     * @return the selected candidate, or the first preference for ranked
     *             ballots
     */
    public String getCandidateName()
    {
        return candidateName;
    }


    /**
     * Time complexity: O(1).
     *
     * @return the trimmed, case-sensitive voter ID
     */
    public String getVoterId()
    {
        return voterId;
    }


    /**
     * Time complexity: O(1).
     *
     * @return the location, or "Unspecified" if none was given
     */
    public String getLocation()
    {
        return location;
    }


    /**
     * Compares ballots using only their voter IDs, preserving letter case. The
     * candidate choice, contest ID, and location are not compared.
     *
     * Time complexity: O(L), where L is the voter ID length.
     *
     * @param other
     *            object to compare with this vote
     * @return true if the other object is a CandidateVote with the same voter
     *             ID
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof CandidateVote))
        {
            return false;
        }
        CandidateVote otherVote = (CandidateVote)other;
        return voterId.equals(otherVote.voterId);
    }


    /**
     * Returns a copy of the candidate rankings so callers cannot change the
     * choices stored in this ballot.
     *
     * Time complexity: O(K), where K is the ranking length.
     *
     * @return copy of the ranking list, in preference order
     */
    public List<String> getRankedChoices()
    {
        return new ArrayList<String>(rankedChoices);
    }
}
