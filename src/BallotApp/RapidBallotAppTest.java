package BallotApp;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.junit.Test;

/**
 * Runs complete simulated terminal sessions against RapidBallotApp. Invalid
 * entries should re-prompt without losing accepted election data.
 */
public class RapidBallotAppTest
{
    /** Error messages appear and bad menu choices are requested again. */
    @Test
    public void testMainMenuErrorsAndExit()
    {
        String output = runScript("garbage", "9", "3", "4");
        assertTrue(output.contains("Welcome to RapidBallot!"));
        assertTrue(output.contains("Error:"));
        assertTrue(output.contains("No active election"));
        assertTrue(output.contains("Goodbye!"));
    }


    /** Candidate, proposal, correction and result options form one workflow. */
    @Test
    public void testWholeVotingWorkflow()
    {
        String output = runScript(
            "1",
            "Club",
            "1",
            "1",
            "Lead",
            "1",
            "2",
            "Alice",
            "alice",
            "Bob",
            "Approve?",
            "1",
            "1",
            "alice",
            "V1",
            "",
            "1",
            "1",
            "Bob",
            "V1",
            "",
            "V2",
            "2",
            "1",
            "maybe",
            "yes",
            "V1",
            "",
            "4",
            "3",
            "1",
            "1",
            "V1",
            "4",
            "6",
            "3",
            "6",
            "4");
        assertTrue(output.contains("That candidate name is already used"));
        assertTrue(output.contains("Voter ID already recorded"));
        assertTrue(output.contains("Enter yes or no"));
        assertTrue(output.contains("Candidate vote recorded"));
        assertTrue(output.contains("Proposal vote recorded"));
        assertTrue(output.contains("Vote removed"));
        assertTrue(output.contains("Winner: Bob"));
        assertTrue(output.contains("Result: Passed"));
        assertTrue(output.contains("Goodbye!"));
    }


    /**
     * Setup validates both-zero counts, invalid choices and duplicate names.
     */
    @Test
    public void testSetupInvalidFieldsAndNoProposals()
    {
        String output = runScript(
            "1",
            "",
            "Election",
            "0",
            "0",
            "1",
            "0",
            "Lead",
            "9",
            "2",
            "0",
            "2",
            "A",
            "a",
            "B",
            "2",
            "4",
            "6",
            "4");
        assertTrue(output.contains("cannot be blank"));
        assertTrue(output.contains("Create at least one contest or proposal"));
        assertTrue(
            output.contains("A contest needs")
                || output.contains("Choose a number"));
        assertTrue(output.contains("already used"));
        assertTrue(output.contains("no proposals"));
    }


    /** Ranked-choice and majority methods are dispatched by the controller. */
    @Test
    public void testRankedAndMajorityModes()
    {
        String output = runScript(
            "1",
            "Mixed",
            "2",
            "0",
            "Lead",
            "2",
            "2",
            "A",
            "B",
            "Chair",
            "3",
            "3",
            "X",
            "Y",
            "Z",
            "1",
            "1",
            "A",
            "M1",
            "",
            "1",
            "2",
            "invalid",
            "X",
            "Y",
            "Z",
            "R1",
            "",
            "4",
            "6",
            "4");
        assertTrue(output.contains("Unknown choice"));
        assertTrue(output.contains("Voting method: Majority required"));
        assertTrue(output.contains("Voting method: Ranked choice"));
        assertTrue(output.contains("Winner: A"));
        assertTrue(output.contains("Winner: X"));
    }


    /**
     * A session without candidate contests supports proposal votes and
     * correction.
     */
    @Test
    public void testProposalOnlyAndMissingCorrection()
    {
        String output = runScript(
            "1",
            "Proposals",
            "0",
            "1",
            "Budget?",
            "1",
            "2",
            "1",
            "1",
            "no",
            "p1",
            "",
            "3",
            "1",
            "3",
            "2",
            "1",
            "missing",
            "4",
            "6",
            "4");
        assertTrue(output.contains("no candidate contests"));
        assertTrue(output.contains("No matching vote found"));
        assertTrue(output.contains("Not passed"));
    }


    /** EOF during setup and voting exits without incomplete records. */
    @Test
    public void testEndOfInputAtSeveralSteps()
    {
        String[] scripts = { "", "1\n", "1\nClub\n", "1\nClub\n1\n",
            "1\nClub\n1\n0\nLead\n", "1\nClub\n1\n0\nLead\n1\n1\nA\n1\n1\n",
            "1\nClub\n0\n1\nApprove?\n2\n1\nyes\nV1\n" };
        for (String script : scripts)
        {
            String output = runRawScript(script);
            assertTrue(output.contains("Goodbye!"));
            assertFalse(output.contains("Exception in thread"));
        }
    }


    /** Loads a previously saved election and displays the correct result. */
    @Test
    public void testLoadAndResave()
        throws IOException
    {
        String name = "TestLoad" + System.nanoTime();
        Path directory = Paths.get("elections");
        Files.createDirectories(directory);
        ElectionSession s = new ElectionSession("Stored Session");
        s.addProposal("Approve?");
        s.recordProposalVote(1, true, "1", "");
        ElectionStorage storage = new ElectionStorage(directory);
        storage.save(s, name);
        List<String> names = storage.listSavedElections();
        int choice = names.indexOf(name) + 1;
        assertTrue(choice > 0);
        try
        {
            String output =
                runScript("2", Integer.toString(choice), "4", "5", "6", "4");
            assertTrue(output.contains("Election loaded: Stored Session"));
            assertTrue(output.contains("Result: Passed"));
            assertTrue(output.contains("Election saved as: " + name));
        }
        finally
        {
            Files.deleteIfExists(directory.resolve(name + ".rball"));
        }
    }


    /**
     * An invalid save name retries the field and can still save successfully.
     */
    @Test
    public void testSaveInvalidFilename()
        throws IOException
    {
        String name = "TestSave" + System.nanoTime();
        try
        {
            String output = runScript(
                "1",
                "Club",
                "0",
                "1",
                "Approve?",
                "5",
                "../escape",
                name,
                "6",
                "4");
            assertTrue(output.contains("Save names"));
            assertTrue(output.contains("Election saved as: " + name));
        }
        finally
        {
            Files.deleteIfExists(Paths.get("elections", name + ".rball"));
        }
    }


    /** Closed scanner exits cleanly instead of crashing. */
    @Test
    public void testClosedScanner()
    {
        Scanner scanner = new Scanner("");
        scanner.close();
        String output = runApp(scanner);
        assertTrue(output.contains("Input is no longer available"));
        assertTrue(output.contains("Goodbye!"));
    }


    /**
     * Null scanner is rejected using the requested explicit exception pattern.
     */
    @Test
    public void testNullScanner()
    {
        IllegalArgumentException caught = null;
        try
        {
            new RapidBallotApp(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** The main entry point handles a supplied end-of-input stream. */
    @Test
    public void testMainMethod()
    {
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream("4\n".getBytes()));
            System.setOut(new PrintStream(bytes));
            RapidBallotApp.main(new String[0]);
        }
        finally
        {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        assertTrue(bytes.toString().contains("Goodbye!"));
    }


    /** Exercises end-of-input at individual prompts in setup and voting. */
    @Test
    public void testEofAtIndividualPrompts()
    {
        String setup = "1\nClub\n1\n0\n";
        String single = setup + "Lead\n1\n2\nAlice\nBob\n";
        String ranked = setup + "Lead\n3\n2\nAlice\nBob\n";
        String proposal = "1\nClub\n0\n1\nApprove?\n";
        String[] partialScripts = { setup, setup + "Lead\n",
            setup + "Lead\n1\n", setup + "Lead\n1\n2\n", "1\nClub\n0\n1\n",
            single + "1\n", single + "1\n1\n", single + "1\n1\nAlice\n",
            single + "1\n1\nAlice\nV1\n", ranked + "1\n1\n",
            ranked + "1\n1\nAlice\n", ranked + "1\n1\nAlice\nBob\nV1\n",
            proposal + "2\n", proposal + "2\n1\n", proposal + "2\n1\nyes\n",
            proposal + "2\n1\nyes\nV1\n", proposal + "3\n", proposal + "3\n2\n",
            proposal + "3\n2\n1\n", single + "3\n1\n", single + "3\n1\n1\n",
            single + "3\n1\n1\nV1\n", single + "5\n" };
        for (String script : partialScripts)
        {
            String output = runRawScript(script);
            assertTrue(output.contains("Goodbye!"));
            assertFalse(output.contains("Exception in thread"));
        }
    }


    /**
     * Invalid and duplicate proposal IDs request a new ID, not an entire
     * ballot.
     */
    @Test
    public void testDuplicateProposalVoteRetry()
    {
        String output = runScript(
            "1",
            "Club",
            "0",
            "1",
            "Approve?",
            "2",
            "1",
            "yes",
            "V1",
            "",
            "2",
            "1",
            "no",
            "V1",
            "",
            "V2",
            "4",
            "6",
            "4");
        assertTrue(output.contains("Voter ID already recorded"));
        assertTrue(output.contains("Re-enter voter ID"));
        assertTrue(output.contains("Yes: 1"));
        assertTrue(output.contains("No: 1"));
    }


    /** Invalid optional locations re-prompt the location only. */
    @Test
    public void testLocationRetry()
    {
        String longLocation = new String(new char[81]).replace('\0', 'X');
        String output = runScript(
            "1",
            "Club",
            "0",
            "1",
            "Approve?",
            "2",
            "1",
            "yes",
            "V1",
            longLocation,
            "Library",
            "4",
            "6",
            "4");
        assertTrue(output.contains("Location must be at most"));
        assertTrue(output.contains("Proposal vote recorded"));
    }


    /**
     * A failed save operation offers a retry or return to the election menu.
     */
    @Test
    public void testFailedSaveReturnsToMenu()
        throws IOException
    {
        String name = "TestBadSave" + System.nanoTime();
        Path block = Paths.get("elections", name + ".rball");
        Files.createDirectories(block);
        try
        {
            String output = runScript(
                "1",
                "Club",
                "0",
                "1",
                "Approve?",
                "5",
                name,
                "2",
                "6",
                "4");
            assertTrue(output.contains("Could not save election"));
            assertTrue(output.contains("Try a different save name"));
            assertTrue(output.contains("Goodbye!"));
        }
        finally
        {
            Files.deleteIfExists(block);
        }
    }


    /** A damaged save is reported and the user may return to the menu. */
    @Test
    public void testCorruptSavedElection()
        throws IOException
    {
        String name = "TestCorrupt" + System.nanoTime();
        Path directory = Paths.get("elections");
        Files.createDirectories(directory);
        Path file = directory.resolve(name + ".rball");
        Files.write(file, new byte[] { 1, 2, 3 });
        try
        {
            int index = new ElectionStorage(directory).listSavedElections()
                .indexOf(name) + 1;
            assertTrue(index > 0);
            String output = runScript("2", Integer.toString(index), "0", "4");
            assertTrue(output.contains("Could not load election"));
            assertTrue(output.contains("Choose another election"));
            assertTrue(output.contains("Goodbye!"));
        }
        finally
        {
            Files.deleteIfExists(file);
        }
    }


    /** Once the list of saved elections appears, an EOF exits cleanly. */
    @Test
    public void testEofDuringLoadSelection()
        throws IOException
    {
        String name = "TestLoadEof" + System.nanoTime();
        Path directory = Paths.get("elections");
        Files.createDirectories(directory);
        ElectionSession election = new ElectionSession("Stored");
        election.addProposal("Approve?");
        new ElectionStorage(directory).save(election, name);
        try
        {
            String output = runRawScript("2\n");
            assertTrue(output.contains("Saved Elections"));
            assertTrue(output.contains("Goodbye!"));
        }
        finally
        {
            Files.deleteIfExists(directory.resolve(name + ".rball"));
        }
    }


    /** A failed save may be retried with a different name. */
    @Test
    public void testFailedSaveRetryAnotherName()
        throws IOException
    {
        String bad = "TestBlocked" + System.nanoTime();
        String good = "TestRecovered" + System.nanoTime();
        Path blocked = Paths.get("elections", bad + ".rball");
        Files.createDirectories(blocked);
        try
        {
            String output = runScript(
                "1",
                "Club",
                "0",
                "1",
                "Approve?",
                "5",
                bad,
                "1",
                good,
                "6",
                "4");
            assertTrue(output.contains("Could not save election"));
            assertTrue(output.contains("Election saved as: " + good));
        }
        finally
        {
            Files.deleteIfExists(blocked);
            Files.deleteIfExists(Paths.get("elections", good + ".rball"));
        }
    }


    /**
     * A duplicate candidate ID causes a retry; EOF during the retry is safe.
     */
    @Test
    public void testEofDuringDuplicateCandidateRetry()
    {
        String output = runRawScript(
            "1\nClub\n1\n0\nLead\n1\n2\nA\nB\n" + "1\n1\nA\nV\n\n"
                + "1\n1\nB\nV\n\n");
        assertTrue(output.contains("Voter ID already recorded"));
        assertTrue(output.contains("Re-enter voter ID"));
        assertTrue(output.contains("Goodbye!"));
    }


    /** A duplicate proposal ID causes a retry; EOF during the retry is safe. */
    @Test
    public void testEofDuringDuplicateProposalRetry()
    {
        String output = runRawScript(
            "1\nClub\n0\n1\nApprove?\n" + "2\n1\nyes\nV\n\n"
                + "2\n1\nno\nV\n\n");
        assertTrue(output.contains("Voter ID already recorded"));
        assertTrue(output.contains("Re-enter voter ID"));
        assertTrue(output.contains("Goodbye!"));
    }


    /** Runs the terminal controller with numbered input lines. */
    private String runScript(String... inputs)
    {
        return runRawScript(String.join("\n", inputs) + "\n");
    }


    /** Provides input exactly as written, including deliberate EOF. */
    private String runRawScript(String input)
    {
        return runApp(new Scanner(input));
    }


    /** Captures output and restores the console after the test. */
    private String runApp(Scanner scanner)
    {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try
        {
            System.setOut(new PrintStream(bytes));
            new RapidBallotApp(scanner).run();
        }
        finally
        {
            System.setOut(original);
            scanner.close();
        }
        return bytes.toString();
    }
}
