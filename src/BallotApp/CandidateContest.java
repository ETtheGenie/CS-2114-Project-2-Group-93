
package BallotApp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One candidate election for a named position. Stores votes in an ElectionChain
 * and prevents duplicate voter IDs. Results are calculated from the stored
 * votes.
 */
public class CandidateContest
{
    private int contestId;
    private String position;
    private ArrayList<String> candidates;
    private ElectionChain<CandidateVote> votes;

    /** 1 = plurality, 2 = majority required, 3 = ranked choice. */
    private int votingMethod;

    /**
     * Creates a candidate contest using plurality voting.
     *
     * Time complexity: O(K^2 L), where K is the number of candidates and L is
     * the maximum name length.
     *
     * @param id
     *            positive contest ID
     * @param position
     *            required position name
     * @param names
     *            1-20 unique candidate names
     */
    public CandidateContest(int id, String position, List<String> names)
    {
        this(id, position, names, 1);
    }


    /**
     * Creates a contest with the selected voting method.
     *
     * Time complexity: O(K^2 L), where K is the number of candidates and L is
     * the maximum name length.
     *
     * @param id
     *            positive contest ID
     * @param position
     *            required position name (maximum 60 characters)
     * @param names
     *            1-20 unique candidate names
     * @param method
     *            1 = plurality, 2 = majority required, 3 = ranked choice
     * @throws IllegalArgumentException
     *             if any input is invalid
     */
    public CandidateContest(
        int id,
        String position,
        List<String> names,
        int method)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException("Contest ID must be positive.");
        }
        if (method < 1 || method > 3)
        {
            throw new IllegalArgumentException(
                "Voting method must be 1, 2, or 3.");
        }
        if (names == null || names.size() < 1
            || names.size() > InputParser.MAX_CANDIDATES)
        {
            throw new IllegalArgumentException(
                "A contest needs 1 to 20 candidates.");
        }

        this.contestId = id;
        this.position = InputParser
            .parseRequiredText(position, InputParser.MAX_NAME_LENGTH);
        this.candidates = new ArrayList<String>();

        for (String name : names)
        {
            // This also rejects blank, overly long, and repeated names.
            String validName = InputParser.parseCandidateName(name, candidates);
            candidates.add(validName);
        }

        this.votingMethod = method;
        this.votes = new ElectionChain<CandidateVote>();
    }


    /**
     * Time complexity: O(1).
     *
     * @return the contest ID
     */
    public int getContestId()
    {
        return contestId;
    }


    /**
     * Time complexity: O(1).
     *
     * @return the position name
     */
    public String getPosition()
    {
        return position;
    }


    /**
     * Time complexity: O(K), where K is the number of candidates.
     *
     * @return a copy of the candidate names in setup order
     */
    public List<String> getCandidates()
    {
        return new ArrayList<String>(candidates);
    }


    /**
     * Finds the stored spelling of a candidate name, ignoring case.
     *
     * Time complexity: O(K^2 L) for random-access candidate lists, where K is
     * the candidate count and L is the maximum name length.
     *
     * @param s
     *            entered candidate name
     * @return matching stored candidate name
     * @throws IllegalArgumentException
     *             if no candidate matches
     */
    public String resolveCandidateName(String s)
    {
        return InputParser.parseChoiceFromList(s, candidates);
    }


    /**
     * Adds one valid ballot, rejecting a repeated voter ID. Single-choice
     * ballots must be used for methods 1 and 2. Ranked-choice ballots must rank
     * every candidate exactly once.
     *
     * Time complexity: O(K^3 L + V L) for ranked ballots and O(K^2 L + V L)
     * for single-choice ballots, where V is the stored vote count.
     *
     * @param vote
     *            ballot to record
     * @throws IllegalArgumentException
     *             if the ballot is invalid
     */
    public void recordVote(CandidateVote vote)
    {
        if (vote == null)
        {
            throw new IllegalArgumentException("Vote cannot be null.");
        }
        if (vote.getContestId() != contestId)
        {
            throw new IllegalArgumentException(
                "Vote belongs to another contest.");
        }

        List<String> rankings = vote.getRankedChoices();
        if (votingMethod == 3)
        {
            // Check the full ranking and its exact canonical spelling.
            List<String> validRankings =
                InputParser.parseRankedChoices(rankings, candidates);
            for (int i = 0; i < rankings.size(); i++)
            {
                if (!rankings.get(i).equals(validRankings.get(i)))
                {
                    throw new IllegalArgumentException(
                        "Use the stored candidate names in the ranking.");
                }
            }
        }
        else
        {
            if (rankings.size() != 1)
            {
                throw new IllegalArgumentException(
                    "This contest accepts one candidate per vote.");
            }
            String canonicalName =
                resolveCandidateName(vote.getCandidateName());
            if (!vote.getCandidateName().equals(canonicalName))
            {
                throw new IllegalArgumentException(
                    "Use the stored candidate name.");
            }
        }

        if (votes.contains(vote))
        {
            throw new IllegalArgumentException("Voter ID already recorded.");
        }
        votes.add(vote);
    }


    /**
     * Removes the vote for a trimmed, case-sensitive voter ID.
     *
     * Time complexity: O(V L), where V is the stored vote count and L is the
     * voter ID length.
     *
     * @param voterId
     *            ID to remove
     * @return true if removed, false if no matching vote exists
     */
    public boolean removeVote(String voterId)
    {
        String id = InputParser
            .parseRequiredText(voterId, InputParser.MAX_VOTER_ID_LENGTH);

        // CandidateVote.equals() compares only the voter ID.
        CandidateVote searchVote =
            new CandidateVote(contestId, candidates.get(0), id, "");
        return votes.remove(searchVote);
    }


    /**
     * Counts first-choice votes, including zero for candidates with no votes.
     * Preserves the order in which candidates were added.
     *
     * Time complexity: O(K + V^2), where K is the candidate count and V is
     * the stored vote count; repeated indexed chain access causes the
     * quadratic term.
     *
     * @return candidate names mapped to their vote counts
     */
    public Map<String, Integer> tallyVotes()
    {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String name : candidates)
        {
            counts.put(name, 0);
        }

        for (int i = 0; i < votes.size(); i++)
        {
            String name = votes.get(i).getCandidateName();
            counts.put(name, counts.get(name) + 1);
        }
        return counts;
    }


    /**
     * Time complexity: Same as getPluralityResultSummary(): O(V^2 + R^2),
     * where V is the vote count and R is the result length.
     *
     * @return the original MVP plurality results
     */
    public String getResultSummary()
    {
        return getPluralityResultSummary();
    }


    /**
     * Time complexity: O(1).
     *
     * @return the number of stored ballots
     */
    public int getVoteCount()
    {
        return votes.size();
    }


    /**
     * Time complexity: O(V^2), where V is the stored vote count; each indexed
     * chain lookup traverses from the head.
     *
     * @return a defensive copy of all ballots, newest first
     */
    public List<CandidateVote> getVotes()
    {
        List<CandidateVote> copy = new ArrayList<CandidateVote>();
        for (int i = 0; i < votes.size(); i++)
        {
            copy.add(votes.get(i));
        }
        return copy;
    }


    /**
     * Time complexity: O(1).
     *
     * @return 1 for plurality, 2 for majority, 3 for ranked choice
     */
    public int getVotingMethod()
    {
        return votingMethod;
    }


    /**
     * Reports first-choice totals and the highest-scoring candidate or tie.
     *
     * Time complexity: O(V^2 + R^2), where V is the vote count and R is the
     * result length; repeated string concatenation can be quadratic.
     *
     * @return formatted plurality results
     */
    public String getPluralityResultSummary()
    {
        String result = getTotalsText() + "\nVoting method: Plurality";
        if (votes.isEmpty())
        {
            return result + "\nNo votes recorded";
        }

        Map<String, Integer> counts = tallyVotes();
        int highest = -1;
        List<String> leaders = new ArrayList<String>();

        for (String name : candidates)
        {
            int count = counts.get(name);
            if (count > highest)
            {
                highest = count;
                leaders.clear();
                leaders.add(name);
            }
            else if (count == highest)
            {
                leaders.add(name);
            }
        }

        if (leaders.size() == 1)
        {
            return result + "\nWinner: " + leaders.get(0);
        }

        result += "\nTie between: ";
        for (int i = 0; i < leaders.size(); i++)
        {
            if (i > 0)
            {
                result += ", ";
            }
            result += leaders.get(i);
        }
        return result;
    }


    /**
     * Reports a winner only when a candidate has more than half the votes.
     *
     * Time complexity: O(V^2 + R^2), where V is the vote count and R is the
     * result length.
     *
     * @return formatted majority-required results
     */
    public String getMajorityResultSummary()
    {
        String result = getTotalsText() + "\nVoting method: Majority required";
        if (votes.isEmpty())
        {
            return result + "\nNo votes recorded";
        }

        Map<String, Integer> counts = tallyVotes();
        for (String name : candidates)
        {
            if (counts.get(name) > votes.size() / 2)
            {
                return result + "\nWinner: " + name;
            }
        }
        return result + "\nNo majority winner";
    }


    /**
     * Runs instant-runoff voting using each ballot's ranked preferences. The
     * lowest candidate is removed each round, and ballots transfer to the next
     * remaining candidate. If several candidates are tied for last place, the
     * election reports an unresolved elimination tie.
     *
     * Time complexity: O(K V^2 + K^3 V L + R^2), where K is the candidate
     * count, V is the vote count, L is the maximum name length, and R is the
     * result length.
     *
     * @return round-by-round ranked-choice results
     */
    public String getRankedChoiceResultSummary()
    {
        String result = "Contest " + contestId + ": " + position
            + "\nVoting method: Ranked choice";
        if (votes.isEmpty())
        {
            return result + "\nNo votes recorded";
        }

        List<String> remaining = new ArrayList<String>(candidates);
        int round = 1;

        while (remaining.size() > 0)
        {
            int[] counts = new int[remaining.size()];
            int countedBallots = 0;

            // Count each ballot for its highest-ranked remaining candidate.
            for (int i = 0; i < votes.size(); i++)
            {
                List<String> rankings = votes.get(i).getRankedChoices();
                for (String choice : rankings)
                {
                    int index = remaining.indexOf(choice);
                    if (index != -1)
                    {
                        counts[index]++;
                        countedBallots++;
                        break;
                    }
                }
            }

            result += "\nRound " + round + ":";
            for (int i = 0; i < remaining.size(); i++)
            {
                result += "\n" + remaining.get(i) + ": " + counts[i];
            }

            if (countedBallots == 0)
            {
                return result + "\nNo active ballots remaining";
            }

            // A candidate with more than half the active votes wins.
            for (int i = 0; i < remaining.size(); i++)
            {
                if (counts[i] > countedBallots / 2)
                {
                    return result + "\nWinner: " + remaining.get(i);
                }
            }

            int lowest = counts[0];
            int lowestIndex = 0;
            int lowestTies = 1;
            for (int i = 1; i < counts.length; i++)
            {
                if (counts[i] < lowest)
                {
                    lowest = counts[i];
                    lowestIndex = i;
                    lowestTies = 1;
                }
                else if (counts[i] == lowest)
                {
                    lowestTies++;
                }
            }

            // We cannot choose whom to eliminate without a tie-break rule.
            if (lowestTies > 1)
            {
                result += "\nUnresolved elimination tie between: ";
                boolean first = true;
                for (int i = 0; i < remaining.size(); i++)
                {
                    if (counts[i] == lowest)
                    {
                        if (!first)
                        {
                            result += ", ";
                        }
                        result += remaining.get(i);
                        first = false;
                    }
                }
                return result;
            }

            result += "\nEliminated: " + remaining.get(lowestIndex);
            remaining.remove(lowestIndex);
            round++;
        }
        return result + "\nNo winner";
    }


    /**
     * Builds the heading and first-choice totals shared by two result methods.
     *
     * Time complexity: O(V^2 + R^2), where V is the vote count and R is the
     * result length.
     *
     * @return contest heading followed by all candidate totals
     */
    private String getTotalsText()
    {
        String result = "Contest " + contestId + ": " + position;
        Map<String, Integer> counts = tallyVotes();
        for (String name : candidates)
        {
            result += "\n" + name + ": " + counts.get(name);
        }
        return result;
    }
}
