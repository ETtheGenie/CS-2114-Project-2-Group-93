package BallotApp;

/**
 * Immutable record of one voter's yes/no proposal ballot.
 * 
 * Equality and hash code depend ONLY on the trimmed, case-sensitive voter ID
 * and the ProposalVote class. Different voters may cast the same yes/no choice;
 * a voter ID can appear once per proposal.
 */
public final class ProposalVote
{

    private final int proposalId;
    private final boolean yesVote;
    private final String voterId;
    private final String location;

    /**
     * Creates an immutable proposal ballot. Validates the positive ID, required
     * voter ID, and optional location. Blank location becomes "Unspecified".
     *
     * @param proposalId
     *            positive proposal ID
     * @param yesVote
     *            true for yes and false for no
     * @param voterId
     *            required ID, trimmed but case-sensitive
     * @param location
     *            optional location, maximum 80 characters
     * @throws IllegalArgumentException
     *             for invalid data
     */
    public ProposalVote(
        int proposalId,
        boolean yesVote,
        String voterId,
        String location)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the proposal ID.
     *
     * @return positive proposal ID
     */
    public int getProposalId()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Indicates whether this is a yes vote.
     *
     * @return true for yes, false for no
     */
    public boolean isYesVote()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the trimmed, case-sensitive voter ID.
     *
     * @return voter ID
     */
    public String getVoterId()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns the normalized location.
     *
     * @return location or "Unspecified"
     */
    public String getLocation()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Compares only the voter ID and ProposalVote class, irrespective of yes/no
     * choice, proposal ID, or location.
     *
     * @param other
     *            object to compare
     * @return true for a ProposalVote with the same voter ID
     */
    @Override
    public boolean equals(Object other)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Returns a hash based on normalized voter ID, consistent with equals.
     *
     * @return voter-ID hash
     */
    @Override
    public int hashCode()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
