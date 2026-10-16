package BallotApp;

/**
 * Immutable record of one voter's yes/no proposal ballot. Two ProposalVote
 * objects are equal when their trimmed, case-sensitive voter IDs match.
 * Different voters may cast the same yes/no choice, but each voter ID can
 * appear only once within a particular proposal.
 */
public final class ProposalVote
{
    private final int proposalId;
    private final boolean yesVote;
    private final String voterId;
    private final String location;

    /**
     * Creates a proposal ballot and validates its information.
     *
     * @param proposalId
     *            positive proposal ID
     * @param yesVote
     *            true for yes, false for no
     * @param voterId
     *            required voter ID (up to 40 characters)
     * @param location
     *            optional location (up to 80 characters)
     * @throws IllegalArgumentException
     *             if any field is invalid
     */
    public ProposalVote(
        int proposalId,
        boolean yesVote,
        String voterId,
        String location)
    {
        if (proposalId <= 0)
        {
            throw new IllegalArgumentException("Proposal ID must be positive.");
        }

        this.proposalId = proposalId;
        this.yesVote = yesVote;
        this.voterId = InputParser
            .parseRequiredText(voterId, InputParser.MAX_VOTER_ID_LENGTH);
        this.location = InputParser.parseLocation(location);
    }


    /**
     * @return the positive proposal ID
     */
    public int getProposalId()
    {
        return proposalId;
    }


    /**
     * @return true for a yes vote, false for a no vote
     */
    public boolean isYesVote()
    {
        return yesVote;
    }


    /**
     * @return the trimmed, case-sensitive voter ID
     */
    public String getVoterId()
    {
        return voterId;
    }


    /**
     * @return the location, or "Unspecified" for blank input
     */
    public String getLocation()
    {
        return location;
    }


    /**
     * Compares proposal votes using only their voter IDs. Proposal ID, yes/no
     * choice, and location do not affect equality.
     *
     * @param other
     *            object to compare
     * @return true if other is a ProposalVote with the same voter ID
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ProposalVote))
        {
            return false;
        }

        ProposalVote otherVote = (ProposalVote)other;
        return voterId.equals(otherVote.voterId);
    }
}
