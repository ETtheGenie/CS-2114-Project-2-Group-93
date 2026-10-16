
package BallotApp;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal controller for RapidBallot. Handles menus, input, voting,
 * corrections, results, saving, and loading. Invalid input is caught and the
 * user is asked to try that field again.
 */
public class RapidBallotApp
{
    private Scanner scanner;
    private ElectionSession session;
    private boolean running;

    /** Manages election files in the elections folder. */
    private ElectionStorage storage;

    /** Filename of the active session, or null if it has not been saved. */
    private String activeFilename;

    /**
     * Creates a controller with no active session.
     *
     * @param scanner
     *            source of terminal input
     * @throws IllegalArgumentException
     *             if scanner is null
     */
    public RapidBallotApp(Scanner scanner)
    {
        if (scanner == null)
        {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        this.scanner = scanner;
        session = null;
        activeFilename = null;
        running = true;
        storage = new ElectionStorage(Paths.get("elections"));
    }


    /** Runs until the user exits or input ends. */
    public void run()
    {
        System.out.println("Welcome to RapidBallot!");
        runMainMenu();
        System.out.println("Goodbye!");
    }


    /**
     * Starts the program using keyboard input.
     *
     * @param args
     *            unused command-line arguments
     */
    public static void main(String[] args)
    {
        try (Scanner input = new Scanner(System.in))
        {
            RapidBallotApp app = new RapidBallotApp(input);
            app.run();
        }
    }


    /**
     * Collects a session name, contest setup, and proposal setup. Does not
     * replace the active session until setup is completely finished.
     */
    private void runAdminSetup()
    {
        System.out.println("\n--- New Election Setup ---");

        String name =
            readRequiredText("Session name: ", InputParser.MAX_NAME_LENGTH);
        if (name == null)
        {
            return;
        }

        Integer contestCount = null;
        Integer proposalCount = null;

        // Both counts can be zero individually, but not at the same time.
        while (running)
        {
            contestCount = readNumber(
                "Number of candidate contests (0-20): ",
                0,
                InputParser.MAX_CONTESTS);
            if (contestCount == null)
            {
                return;
            }

            proposalCount = readNumber(
                "Number of proposals (0-20): ",
                0,
                InputParser.MAX_PROPOSALS);
            if (proposalCount == null)
            {
                return;
            }

            try
            {
                InputParser.validateSetupCounts(contestCount, proposalCount);
                break;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }

        ElectionSession newSession;
        try
        {
            newSession = new ElectionSession(name);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        for (int i = 0; i < contestCount && running;)
        {
            System.out.println("\n--- Candidate Contest " + (i + 1) + " ---");

            String position = readRequiredText(
                "Position name: ",
                InputParser.MAX_NAME_LENGTH);
            if (position == null)
            {
                return;
            }

            System.out.println("Voting systems:");
            System.out.println("1. Plurality");
            System.out.println("2. Majority required");
            System.out.println("3. Ranked choice");
            Integer method = readValidMenuChoice(3);
            if (method == null)
            {
                return;
            }

            Integer candidateCount = readNumber(
                "Number of candidates (1-20): ",
                1,
                InputParser.MAX_CANDIDATES);
            if (candidateCount == null)
            {
                return;
            }

            List<String> names = new ArrayList<String>();
            for (int j = 0; j < candidateCount && running;)
            {
                String entry = readLine("Candidate " + (j + 1) + " name: ");
                if (entry == null)
                {
                    return;
                }
                try
                {
                    String validName =
                        InputParser.parseCandidateName(entry, names);
                    names.add(validName);
                    j++;
                }
                catch (IllegalArgumentException e)
                {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            try
            {
                newSession.addCandidateContest(position, names, method);
                i++;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error creating contest: " + e.getMessage());
                System.out.println("Please enter this contest again.");
            }
        }

        for (int i = 0; i < proposalCount && running;)
        {
            System.out.println("\n--- Proposal " + (i + 1) + " ---");
            String question = readRequiredText(
                "Proposal question: ",
                InputParser.MAX_PROPOSAL_LENGTH);
            if (question == null)
            {
                return;
            }

            try
            {
                newSession.addProposal(question);
                i++;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }

        if (running)
        {
            session = newSession;
            activeFilename = null;
            System.out.println("\nElection created: " + session.getName());
        }
    }


    /** Collects and records one candidate vote (or complete ranking). */
    private void runCandidateVoting()
    {
        List<CandidateContest> contests = session.getCandidateContests();
        if (contests.isEmpty())
        {
            System.out.println("This election has no candidate contests.");
            return;
        }

        System.out.println("\nCandidate contests:");
        for (CandidateContest contest : contests)
        {
            System.out
                .println(contest.getContestId() + ". " + contest.getPosition());
        }

        Integer id =
            readNumber("Choose a contest number: ", 1, contests.size());
        if (id == null)
        {
            return;
        }

        CandidateContest contest;
        try
        {
            contest = session.getCandidateContest(id);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        String candidate = null;
        List<String> rankings = new ArrayList<String>();

        if (contest.getVotingMethod() == 3)
        {
            List<String> remaining = contest.getCandidates();
            System.out.println(
                "Rank all candidates, from favorite to least favorite.");

            for (int rank = 1; !remaining.isEmpty() && running; rank++)
            {
                System.out.println("Available candidates:");
                for (String name : remaining)
                {
                    System.out.println("- " + name);
                }

                String choice =
                    readChoiceFromList("Rank " + rank + " name: ", remaining);
                if (choice == null)
                {
                    return;
                }
                rankings.add(choice);
                remaining.remove(choice);
            }
        }
        else
        {
            System.out.println("Candidates:");
            for (String name : contest.getCandidates())
            {
                System.out.println("- " + name);
            }
            candidate =
                readChoiceFromList("Candidate name: ", contest.getCandidates());
            if (candidate == null)
            {
                return;
            }
        }

        String voterId =
            readRequiredText("Voter ID: ", InputParser.MAX_VOTER_ID_LENGTH);
        if (voterId == null)
        {
            return;
        }
        String location = readLocation();
        if (location == null)
        {
            return;
        }

        // If this ID was already used, keep the selection and ask for only
        // a new voter ID. A rejected ballot never changes the stored votes.
        while (running)
        {
            try
            {
                if (contest.getVotingMethod() == 3)
                {
                    session
                        .recordCandidateVote(id, rankings, voterId, location);
                }
                else
                {
                    session
                        .recordCandidateVote(id, candidate, voterId, location);
                }
                System.out.println("Candidate vote recorded.");
                return;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
                voterId = readRequiredText(
                    "Re-enter voter ID: ",
                    InputParser.MAX_VOTER_ID_LENGTH);
                if (voterId == null)
                {
                    return;
                }
            }
        }
    }


    /** Collects and records one yes/no proposal vote. */
    private void runProposalVoting()
    {
        List<ProposalContest> proposals = session.getProposals();
        if (proposals.isEmpty())
        {
            System.out.println("This election has no proposals.");
            return;
        }

        System.out.println("\nProposals:");
        for (ProposalContest proposal : proposals)
        {
            System.out.println(
                proposal.getProposalId() + ". " + proposal.getQuestion());
        }

        Integer id =
            readNumber("Choose a proposal number: ", 1, proposals.size());
        if (id == null)
        {
            return;
        }

        Boolean yes = readYesNo("Vote yes or no: ");
        if (yes == null)
        {
            return;
        }
        String voterId =
            readRequiredText("Voter ID: ", InputParser.MAX_VOTER_ID_LENGTH);
        if (voterId == null)
        {
            return;
        }
        String location = readLocation();
        if (location == null)
        {
            return;
        }

        while (running)
        {
            try
            {
                session.recordProposalVote(id, yes, voterId, location);
                System.out.println("Proposal vote recorded.");
                return;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
                voterId = readRequiredText(
                    "Re-enter voter ID: ",
                    InputParser.MAX_VOTER_ID_LENGTH);
                if (voterId == null)
                {
                    return;
                }
            }
        }
    }


    /** Removes a vote by voter ID and returns to the session menu. */
    private void runCorrectVote()
    {
        System.out.println("\nCorrect a vote:");
        System.out.println("1. Candidate contest");
        System.out.println("2. Proposal");
        System.out.println("3. Cancel");

        Integer type = readValidMenuChoice(3);
        if (type == null || type == 3)
        {
            return;
        }

        int count;
        if (type == 1)
        {
            count = session.getCandidateContests().size();
            for (CandidateContest contest : session.getCandidateContests())
            {
                System.out.println(
                    contest.getContestId() + ". " + contest.getPosition());
            }
        }
        else
        {
            count = session.getProposals().size();
            for (ProposalContest proposal : session.getProposals())
            {
                System.out.println(
                    proposal.getProposalId() + ". " + proposal.getQuestion());
            }
        }

        if (count == 0)
        {
            System.out.println("No entries of that type are configured.");
            return;
        }

        Integer id = readNumber("Choose a contest/proposal number: ", 1, count);
        if (id == null)
        {
            return;
        }
        String voterId = readRequiredText(
            "Voter ID to remove: ",
            InputParser.MAX_VOTER_ID_LENGTH);
        if (voterId == null)
        {
            return;
        }

        while (running)
        {
            try
            {
                boolean removed;
                if (type == 1)
                {
                    removed = session.correctCandidateVote(id, voterId);
                }
                else
                {
                    removed = session.correctProposalVote(id, voterId);
                }

                if (removed)
                {
                    System.out
                        .println("Vote removed. You can now cast a new vote.");
                }
                else
                {
                    System.out.println("No matching vote found.");
                }
                return;
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
                voterId = readRequiredText(
                    "Re-enter voter ID: ",
                    InputParser.MAX_VOTER_ID_LENGTH);
                if (voterId == null)
                {
                    return;
                }
            }
        }
    }


    /** Displays results for every candidate contest and proposal. */
    private void runViewResults()
    {
        System.out.println("\n--- Results: " + session.getName() + " ---");

        for (CandidateContest contest : session.getCandidateContests())
        {
            try
            {
                int method = contest.getVotingMethod();
                if (method == 1)
                {
                    System.out.println(contest.getPluralityResultSummary());
                }
                else if (method == 2)
                {
                    System.out.println(contest.getMajorityResultSummary());
                }
                else if (method == 3)
                {
                    System.out.println(contest.getRankedChoiceResultSummary());
                }
                else
                {
                    System.out
                        .println("Invalid voting method for this contest.");
                }
            }
            catch (IllegalArgumentException e)
            {
                System.out
                    .println("Error displaying results: " + e.getMessage());
            }
            System.out.println();
        }

        for (ProposalContest proposal : session.getProposals())
        {
            try
            {
                System.out.println(proposal.getResultSummary());
            }
            catch (IllegalArgumentException e)
            {
                System.out
                    .println("Error displaying results: " + e.getMessage());
            }
            System.out.println();
        }
    }


    /**
     * Prints a prompt and returns one input line. EOF ends the application.
     *
     * @param prompt
     *            text to display
     * @return entered text, or null at EOF
     */
    private String readLine(String prompt)
    {
        System.out.print(prompt);
        try
        {
            if (!scanner.hasNextLine())
            {
                running = false;
                return null;
            }
            return scanner.nextLine();
        }
        catch (IllegalStateException e)
        {
            System.out.println("Input is no longer available.");
            running = false;
            return null;
        }
    }


    /**
     * Reads a numbered menu option, retrying incorrect input.
     *
     * @param max
     *            largest valid choice
     * @return menu choice, or null at EOF
     */
    private Integer readValidMenuChoice(int max)
    {
        return readNumber("Enter your choice: ", 1, max);
    }


    /** Shows the global menu for creating, loading, resuming, or exiting. */
    private void runMainMenu()
    {
        while (running)
        {
            System.out.println("\n--- RapidBallot Main Menu ---");
            if (session != null)
            {
                System.out.println("Active election: " + session.getName());
            }
            System.out.println("1. Create new election");
            System.out.println("2. Load saved election");
            System.out.println("3. Resume active election");
            System.out.println("4. Exit");

            Integer choice = readValidMenuChoice(4);
            if (choice == null)
            {
                return;
            }

            if (choice == 1)
            {
                ElectionSession previous = session;
                runAdminSetup();
                if (running && session != previous)
                {
                    runSessionMenu();
                }
            }
            else if (choice == 2)
            {
                ElectionSession previous = session;
                runLoadElection();
                if (running && session != previous)
                {
                    runSessionMenu();
                }
            }
            else if (choice == 3)
            {
                if (session == null)
                {
                    System.out.println(
                        "No active election. Create or load one first.");
                }
                else
                {
                    runSessionMenu();
                }
            }
            else
            {
                running = false;
            }
        }
    }


    /** Shows the election menu until the user returns to the main menu. */
    private void runSessionMenu()
    {
        boolean inSessionMenu = true;
        while (running && inSessionMenu)
        {
            System.out.println("\n--- Election: " + session.getName() + " ---");
            System.out.println("1. Record candidate vote");
            System.out.println("2. Record proposal vote");
            System.out.println("3. Correct a vote");
            System.out.println("4. View results");
            System.out.println("5. Save election");
            System.out.println("6. Return to main menu");

            Integer choice = readValidMenuChoice(6);
            if (choice == null)
            {
                return;
            }

            if (choice == 1)
            {
                runCandidateVoting();
            }
            else if (choice == 2)
            {
                runProposalVoting();
            }
            else if (choice == 3)
            {
                runCorrectVote();
            }
            else if (choice == 4)
            {
                runViewResults();
            }
            else if (choice == 5)
            {
                runSaveElection();
            }
            else
            {
                inSessionMenu = false;
            }
        }
    }


    /**
     * Saves the current election, automatically overwriting an existing save.
     */
    private void runSaveElection()
    {
        if (session == null)
        {
            System.out.println("No election to save.");
            return;
        }

        String filename = activeFilename;
        while (running)
        {
            if (filename == null)
            {
                filename = readSaveName();
                if (filename == null)
                {
                    return;
                }
            }

            try
            {
                storage.save(session, filename);
                activeFilename = filename;
                System.out.println("Election saved as: " + filename);
                return;
            }
            catch (IOException | IllegalArgumentException e)
            {
                System.out
                    .println("Could not save election: " + e.getMessage());
                System.out.println("1. Try a different save name");
                System.out.println("2. Return to election menu");
                Integer choice = readValidMenuChoice(2);
                if (choice == null || choice == 2)
                {
                    return;
                }
                filename = null;
            }
        }
    }


    /** Lists available saves and loads one into a new active session. */
    private void runLoadElection()
    {
        List<String> savedElections;
        try
        {
            savedElections = storage.listSavedElections();
        }
        catch (IOException e)
        {
            System.out
                .println("Could not list saved elections: " + e.getMessage());
            return;
        }

        if (savedElections.isEmpty())
        {
            System.out.println("No saved elections found.");
            return;
        }

        while (running)
        {
            System.out.println("\n--- Saved Elections ---");
            for (int i = 0; i < savedElections.size(); i++)
            {
                System.out.println((i + 1) + ". " + savedElections.get(i));
            }
            System.out.println("0. Return to main menu");

            Integer choice = readNumber(
                "Choose a saved election: ",
                0,
                savedElections.size());
            if (choice == null || choice == 0)
            {
                return;
            }

            String filename = savedElections.get(choice - 1);
            try
            {
                ElectionSession loaded = storage.load(filename);
                // Only replace the active session after loading has succeeded.
                session = loaded;
                activeFilename = filename;
                System.out.println("Election loaded: " + session.getName());
                return;
            }
            catch (IOException | IllegalArgumentException e)
            {
                System.out
                    .println("Could not load election: " + e.getMessage());
                System.out
                    .println("Choose another election or enter 0 to cancel.");
            }
        }
    }


    /** Reads any integer in the given inclusive range. */
    private Integer readNumber(String prompt, int min, int max)
    {
        while (running)
        {
            String input = readLine(prompt);
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseNumberInRange(input, min, max);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }


    /** Reads required text up to the given maximum length. */
    private String readRequiredText(String prompt, int max)
    {
        while (running)
        {
            String input = readLine(prompt);
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseRequiredText(input, max);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }


    /** Reads a candidate name from the given allowed list. */
    private String readChoiceFromList(String prompt, List<String> choices)
    {
        while (running)
        {
            String input = readLine(prompt);
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseChoiceFromList(input, choices);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }


    /** Reads a yes/no answer. Null means EOF, not a no vote. */
    private Boolean readYesNo(String prompt)
    {
        while (running)
        {
            String input = readLine(prompt);
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseYesNo(input);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }


    /** Reads an optional location, using Unspecified for a blank line. */
    private String readLocation()
    {
        while (running)
        {
            String input =
                readLine("Location (optional; press Enter to skip): ");
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseLocation(input);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }


    /** Reads a safe filename for saving an election. */
    private String readSaveName()
    {
        while (running)
        {
            String input = readLine("Save name (without .rball): ");
            if (input == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseSaveName(input);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }
}
