package BallotApp;

import java.util.List;

/**
 * Immutable record of one voter's candidate ballot.
 
 * Equality and hash code depend ONLY on the trimmed, case-sensitive voter ID
 * and the CandidateVote class, not on candidate, ranking, contest ID, or
 * location. This supports duplicate-ID detection within a specific contest's
 * LinkedChain.
 
 */
public final class CandidateVote
{

    private final int contestId;
    private final String candidateName;
    private final String voterId;
    private final String location;

    /**
     * STRETCH: Immutable ranked preferences. A single-choice ballot contains
     * one choice; an IRV ballot contains all candidates in order.
     */
    private final List<String> rankedChoices;

    /**
     * Creates an immutable single-choice candidate ballot (MVP signature).
     * Required text is trimmed; blank location becomes "Unspecified". The
     * session resolves the canonical candidate name before calling.
     *
     * @param contestId
     *            positive candidate contest ID
     * @param candidateName
     *            selected canonical candidate name
     * @param voterId
     *            required voter ID (1-40 characters after trimming)
     * @param location
     *            optional location (maximum 80 characters)
     * @throws IllegalArgumentException
     *             for invalid fields
     */
    public CandidateVote(
        int contestId,
        String candidateName,
        String voterId,
        String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the candidate contest ID.
     *
     * @return positive contest ID
     */
    public int getContestId()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the selected candidate; for ranked-choice ballots this is the
     * highest-ranked (first-preference) candidate.
     *
     * @return canonical first-choice candidate name
     */
    public String getCandidateName()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the trimmed but case-sensitive voter ID.
     *
     * @return voter ID
     */
    public String getVoterId()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the location ("Unspecified" for blank input).
     *
     * @return normalized location
     */
    public String getLocation()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Compares only exact voter ID after normalization and requires the other
     * object to be a CandidateVote. The selected candidate, contest, and
     * location do not contribute to equality.
     *
     * @param other
     *            object to compare
     * @return true for a CandidateVote with the same voter ID
     */
    @Override
    public boolean equals(Object other)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Computes a hash consistent with equals: equal IDs hash equally.
     *
     * @return hash based on the case-sensitive normalized voter ID
     */
    @Override
    public int hashCode()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * STRETCH: Creates an immutable ranked-choice ballot. The input is an
     * ordered preference list; the contest later validates that each configured
     * candidate appears exactly once.
     *
     * @param contestId
     *            positive candidate contest ID
     * @param rankings
     *            preferred candidate names, most preferred first
     * @param voterId
     *            required voter ID
     * @param location
     *            optional location
     * @throws IllegalArgumentException
     *             for invalid fields
     */
    public CandidateVote(
        int contestId,
        List<String> rankings,
        String voterId,
        String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * STRETCH: Returns an immutable or defensive-copy list of preferences in
     * ballot order. For single-choice ballots, the list has one item.
     *
     * @return ordered preferences
     */
    public List<String> getRankedChoices()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
