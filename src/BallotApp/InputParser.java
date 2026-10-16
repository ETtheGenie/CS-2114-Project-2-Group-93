package BallotApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates text entered into the RapidBallot terminal. Every parser returns a
 * cleaned value or throws IllegalArgumentException. This class does not print
 * messages, read from Scanner, or change votes. RapidBallotApp catches the
 * exceptions and asks the user to try again. Call parseNumberInRange for all
 * numeric menus, IDs, and counts; call parseRequiredText for session names,
 * positions, and voter IDs; call parseChoiceFromList for candidate and
 * saved-election selection.
 */
public final class InputParser
{
    /** Maximum length of session, position, and candidate names. */
    public static final int MAX_NAME_LENGTH = 60;
    /** Maximum trimmed voter ID length. */
    public static final int MAX_VOTER_ID_LENGTH = 40;
    /** Maximum optional location length. */
    public static final int MAX_LOCATION_LENGTH = 80;
    /** Maximum proposal question length. */
    public static final int MAX_PROPOSAL_LENGTH = 200;
    /** Maximum candidate contests per session. */
    public static final int MAX_CONTESTS = 20;
    /** Maximum proposals per session. */
    public static final int MAX_PROPOSALS = 20;
    /** Maximum candidates within one contest. */
    public static final int MAX_CANDIDATES = 20;

    /**
     * This class contains only static methods and cannot be instantiated.
     *
     * Time complexity: O(1).
     */
    private InputParser()
    {
        // No instance state.
    }


    /**
     * Parses an integer within the given inclusive range. Blank text,
     * non-integers, and numbers outside the range are rejected.
     *
     * Time complexity: O(L), where L is the input length.
     *
     * @param s
     *            text entered by the user
     * @param min
     *            smallest allowed number
     * @param max
     *            largest allowed number
     * @return the validated integer
     * @throws IllegalArgumentException
     *             if the text or range is invalid
     */
    public static int parseNumberInRange(String s, int min, int max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Invalid numeric range.");
        }
        if (s == null || s.trim().isEmpty())
        {
            throw new IllegalArgumentException(
                "Enter a whole number from " + min + " to " + max + ".");
        }

        int number;
        try
        {
            number = Integer.parseInt(s.trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(
                "Enter a whole number from " + min + " to " + max + ".");
        }

        if (number < min || number > max)
        {
            throw new IllegalArgumentException(
                "Choose a number from " + min + " to " + max + ".");
        }
        return number;
    }


    /**
     * Validates the two setup counts together. A session must contain at least
     * one candidate contest or one proposal.
     *
     * Time complexity: O(1).
     *
     * @param contestCount
     *            number of candidate contests
     * @param proposalCount
     *            number of proposals
     * @throws IllegalArgumentException
     *             if counts exceed limits or both are zero
     */
    public static void validateSetupCounts(int contestCount, int proposalCount)
    {
        if (contestCount < 0 || contestCount > MAX_CONTESTS || proposalCount < 0
            || proposalCount > MAX_PROPOSALS)
        {
            throw new IllegalArgumentException(
                "Invalid contest or proposal count.");
        }
        if (contestCount == 0 && proposalCount == 0)
        {
            throw new IllegalArgumentException(
                "Create at least one contest or proposal.");
        }
    }


    /**
     * Matches an entered name to one item in a provided list, ignoring case.
     * Returns the original spelling from the list. Used for candidates and
     * other menus where users type an available option rather than a number.
     *
     * Time complexity: O(N^2 L) for random-access options, where N is the
     * option count and L is the maximum option length; indexed access on
     * other List types may add cost.
     *
     * @param s
     *            text entered by the user
     * @param options
     *            allowed names or choices
     * @return the matching option as stored in the list
     * @throws IllegalArgumentException
     *             for unknown choices or invalid options
     */
    public static String parseChoiceFromList(String s, List<String> options)
    {
        if (options == null || options.isEmpty())
        {
            throw new IllegalArgumentException("No choices are available.");
        }
        if (s == null || s.trim().isEmpty())
        {
            throw new IllegalArgumentException(
                "Enter one of the available choices.");
        }

        String choice = s.trim();
        for (String option : options)
        {
            if (option == null || option.trim().isEmpty())
            {
                throw new IllegalArgumentException(
                    "The list of choices is invalid.");
            }
        }

        // Reject duplicated options so a typed name has only one meaning.
        for (int i = 0; i < options.size(); i++)
        {
            for (int j = i + 1; j < options.size(); j++)
            {
                if (options.get(i).trim()
                    .equalsIgnoreCase(options.get(j).trim()))
                {
                    throw new IllegalArgumentException(
                        "The list of choices contains duplicates.");
                }
            }
        }

        for (String option : options)
        {
            if (choice.equalsIgnoreCase(option.trim()))
            {
                return option;
            }
        }
        throw new IllegalArgumentException(
            "Unknown choice: " + choice
                + ". Enter one of the available choices.");
    }


    /**
     * Validates an entire ranked-choice ballot. The controller should collect
     * one candidate name per rank, then pass those strings to this method.
     * Every candidate must appear exactly once. Returns canonical spellings.
     *
     * Time complexity: O(K^3 L), where K is the candidate count and L is the
     * maximum candidate name length.
     *
     * @param enteredChoices
     *            names entered in ranked order
     * @param candidates
     *            all available candidate names
     * @return validated candidate names in ranked order
     * @throws IllegalArgumentException
     *             for missing, repeated, or unknown names
     */
    public static
        List<String>
        parseRankedChoices(List<String> enteredChoices, List<String> candidates)
    {
        if (candidates == null || candidates.isEmpty()
            || candidates.size() > MAX_CANDIDATES)
        {
            throw new IllegalArgumentException("Invalid candidate list.");
        }
        if (enteredChoices == null
            || enteredChoices.size() != candidates.size())
        {
            throw new IllegalArgumentException(
                "Rank every candidate exactly once.");
        }

        List<String> remaining = new ArrayList<String>(candidates);
        List<String> ranking = new ArrayList<String>();
        for (String choice : enteredChoices)
        {
            String candidate = parseChoiceFromList(choice, remaining);
            ranking.add(candidate);
            remaining.remove(candidate);
        }
        return ranking;
    }


    /**
     * Parses 'yes' or 'no' (case-insensitive) for proposal votes and prompts
     * requiring a yes/no answer.
     *
     * Time complexity: O(L), where L is the input length.
     *
     * @param s
     *            entered text
     * @return true for yes and false for no
     * @throws IllegalArgumentException
     *             for any other answer
     */
    public static boolean parseYesNo(String s)
    {
        if (s == null)
        {
            throw new IllegalArgumentException("Enter yes or no.");
        }
        String response = s.trim();
        if (response.equalsIgnoreCase("yes"))
        {
            return true;
        }
        if (response.equalsIgnoreCase("no"))
        {
            return false;
        }
        throw new IllegalArgumentException("Enter yes or no.");
    }


    /**
     * Trims required text and checks its maximum length.
     *
     * Time complexity: O(L), where L is the input length.
     *
     * @param s
     *            entered text
     * @param max
     *            largest allowed number of characters
     * @return trimmed, nonblank text
     * @throws IllegalArgumentException
     *             if invalid
     */
    public static String parseRequiredText(String s, int max)
    {
        if (max < 1)
        {
            throw new IllegalArgumentException(
                "Maximum length must be positive.");
        }
        if (s == null || s.trim().isEmpty())
        {
            throw new IllegalArgumentException("This field cannot be blank.");
        }
        String text = s.trim();
        if (text.length() > max)
        {
            throw new IllegalArgumentException(
                "Input must be at most " + max + " characters long.");
        }
        return text;
    }


    /**
     * Validates a new candidate name and rejects a repeated name, ignoring
     * capitalization and surrounding spaces.
     *
     * Time complexity: O(N L), where N is the existing candidate count and L
     * is the maximum name length.
     *
     * @param s
     *            entered candidate name
     * @param existingNames
     *            candidates already entered for this contest
     * @return trimmed candidate name
     * @throws IllegalArgumentException
     *             for invalid or duplicate names
     */
    public static
        String
        parseCandidateName(String s, List<String> existingNames)
    {
        String candidate = parseRequiredText(s, MAX_NAME_LENGTH);
        if (existingNames == null)
        {
            throw new IllegalArgumentException(
                "Candidate list cannot be null.");
        }
        if (existingNames.size() >= MAX_CANDIDATES)
        {
            throw new IllegalArgumentException("Too many candidates.");
        }
        for (String existing : existingNames)
        {
            if (existing == null)
            {
                throw new IllegalArgumentException(
                    "Candidate list contains null.");
            }
            if (candidate.equalsIgnoreCase(existing.trim()))
            {
                throw new IllegalArgumentException(
                    "That candidate name is already used.");
            }
        }
        return candidate;
    }


    /**
     * Trims an optional location; blank becomes 'Unspecified'.
     *
     * Time complexity: O(L), where L is the input length.
     *
     * @param s
     *            entered location
     * @return cleaned location or 'Unspecified'
     * @throws IllegalArgumentException
     *             if null or longer than 80 characters
     */
    public static String parseLocation(String s)
    {
        if (s == null)
        {
            throw new IllegalArgumentException("Location cannot be null.");
        }
        String location = s.trim();
        if (location.length() > MAX_LOCATION_LENGTH)
        {
            throw new IllegalArgumentException(
                "Location must be at most " + MAX_LOCATION_LENGTH
                    + " characters long.");
        }
        if (location.isEmpty())
        {
            return "Unspecified";
        }
        return location;
    }


    /**
     * Validates the name of a save file without its extension. Only letters,
     * numbers, spaces, underscores, and hyphens are accepted. The first
     * character must be a letter or number to avoid path names.
     *
     * Time complexity: O(L), where L is the input length.
     *
     * @param s
     *            entered save name
     * @return trimmed name suitable for use as a simple filename
     * @throws IllegalArgumentException
     *             for invalid characters or length
     */
    public static String parseSaveName(String s)
    {
        String name = parseRequiredText(s, MAX_NAME_LENGTH);
        if (!Character.isLetterOrDigit(name.charAt(0)))
        {
            throw new IllegalArgumentException(
                "Save names must start with a letter or number.");
        }
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ' && c != '-'
                && c != '_')
            {
                throw new IllegalArgumentException(
                    "Save names may contain only letters, numbers, spaces, "
                        + "hyphens, or underscores.");
            }
        }
        return name;
    }

}
