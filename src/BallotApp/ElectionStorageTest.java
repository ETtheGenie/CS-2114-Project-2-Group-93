package BallotApp;

import static org.junit.Assert.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Tests binary election saving, overwriting, loading, listing and bad files.
 * Test files are created in temporary directories, never in real elections.
 */
public class ElectionStorageTest
{
    /** Saves and restores all three ballot methods and proposal votes. */
    @Test
    public void testSaveLoadAndOverwrite()
        throws IOException
    {
        Path folder = Files.createTempDirectory("rapidballot-storage-");
        ElectionStorage storage = new ElectionStorage(folder);
        ElectionSession s = new ElectionSession("Club Election");
        s.addCandidateContest("Plurality", Arrays.asList("A", "B"));
        s.addCandidateContest("Majority", Arrays.asList("C", "D"), 2);
        s.addCandidateContest("Ranked", Arrays.asList("E", "F", "G"), 3);
        s.addProposal("Approve funding?");
        s.recordCandidateVote(1, "A", "V1", "Hall");
        s.recordCandidateVote(1, "B", "V2", "");
        s.recordCandidateVote(2, "C", "V1", "");
        s.recordCandidateVote(3, Arrays.asList("G", "E", "F"), "V1", "");
        s.recordProposalVote(1, true, "V1", "");
        s.recordProposalVote(1, false, "V2", "");
        storage.save(s, "Election A");
        assertEquals(Arrays.asList("Election A"), storage.listSavedElections());
        ElectionSession restored = storage.load("Election A");
        assertEquals("Club Election", restored.getName());
        assertEquals(3, restored.getCandidateContests().size());
        assertEquals(1, restored.getProposals().size());
        assertEquals(3, restored.getCandidateContest(3).getVotingMethod());
        assertEquals(
            Arrays.asList("G", "E", "F"),
            restored.getCandidateContest(3).getVotes().get(0)
                .getRankedChoices());
        assertEquals(
            "V2",
            restored.getCandidateContest(1).getVotes().get(0).getVoterId());
        assertEquals(1, restored.getProposal(1).getYesCount());
        assertEquals(1, restored.getProposal(1).getNoCount());
        assertTrue(restored.correctProposalVote(1, "V1"));
        assertEquals(0, restored.getProposal(1).getYesCount());
        // Replacing an existing save requires no confirmation.
        s.recordProposalVote(1, true, "V3", "");
        storage.save(s, "Election A");
        assertEquals(
            3,
            storage.load("Election A").getProposal(1).getVoteCount());
        storage.save(s, "Election B");
        assertEquals(
            Arrays.asList("Election A", "Election B"),
            storage.listSavedElections());
    }


    /** Proposal-only sessions are valid; empty sessions may not be saved. */
    @Test
    public void testProposalOnlyAndEmptySession()
        throws IOException
    {
        ElectionStorage storage =
            new ElectionStorage(Files.createTempDirectory("rapidballot-only-"));
        ElectionSession s = new ElectionSession("Only Proposals");
        assertTrue(storage.listSavedElections().isEmpty());
        IOException caught = null;
        try
        {
            storage.save(s, "Empty");
        }
        catch (IOException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        s.addProposal("Question?");
        storage.save(s, "Valid");
        assertEquals(1, storage.load("Valid").getProposals().size());
    }


    /** Constructor and filenames cannot select invalid filesystem targets. */
    @Test
    public void testInvalidTargets()
        throws IOException
    {
        IllegalArgumentException caught = null;
        try
        {
            new ElectionStorage(null);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        Path dir = Files.createTempDirectory("rapidballot-path-");
        Path existingFile = Files.createTempFile(dir, "not-a-folder", ".txt");
        caught = null;
        try
        {
            new ElectionStorage(existingFile);
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        ElectionStorage storage = new ElectionStorage(dir);
        ElectionSession s = new ElectionSession("Valid");
        s.addProposal("Question?");
        caught = null;
        try
        {
            storage.save(null, "Valid");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            storage.save(s, "../escape");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        caught = null;
        try
        {
            storage.load("../escape");
        }
        catch (IllegalArgumentException e)
        {
            caught = e;
        }
        assertNotNull(caught);
        IOException io = null;
        try
        {
            storage.load("Missing");
        }
        catch (IOException e)
        {
            io = e;
        }
        assertNotNull(io);
        Files.createDirectory(dir.resolve("Folder.rball"));
        io = null;
        try
        {
            storage.save(s, "Folder");
        }
        catch (IOException e)
        {
            io = e;
        }
        assertNotNull(io);
    }


    /** Bad filenames are ignored when listing; valid names remain sorted. */
    @Test
    public void testListFiltersNamesAndNonFiles()
        throws IOException
    {
        Path dir = Files.createTempDirectory("rapidballot-list-");
        Files.write(dir.resolve("Z.rball"), new byte[] { 0 });
        Files.write(dir.resolve("A.rball"), new byte[] { 0 });
        Files.write(dir.resolve("!bad.rball"), new byte[] { 0 });
        Files.write(dir.resolve("not-a-save.txt"), new byte[] { 0 });
        Files.createDirectory(dir.resolve("Directory.rball"));
        assertEquals(
            Arrays.asList("A", "Z"),
            new ElectionStorage(dir).listSavedElections());
        // A storage location that becomes a regular file cannot be listed.
        Path separate = Files.createTempDirectory("rapidballot-changing-");
        Path gone = separate.resolve("becomes-file");
        ElectionStorage storage = new ElectionStorage(gone);
        Files.write(gone, new byte[] { 1 });
        IOException caught = null;
        try
        {
            storage.listSavedElections();
        }
        catch (IOException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /**
     * Reject files that are truncated, mis-versioned or contain extra bytes.
     */
    @Test
    public void testMalformedHeadersCountsAndTrailingBytes()
        throws IOException
    {
        Path dir = Files.createTempDirectory("rapidballot-corrupt-");
        ElectionStorage storage = new ElectionStorage(dir);
        for (int mode = 0; mode < 7; mode++)
        {
            Path file = dir.resolve("Broken.rball");
            try (
                DataOutputStream out =
                    new DataOutputStream(Files.newOutputStream(file)))
            {
                if (mode == 0)
                {
                    out.writeByte(1);
                }
                else
                {
                    out.writeUTF(mode == 1 ? "NOT_BALLOT" : "RAPIDBALLOT");
                    out.writeInt(mode == 2 ? 999 : 1);
                    if (mode >= 3)
                    {
                        out.writeUTF("Election");
                        out.writeInt(mode == 3 ? -1 : mode == 4 ? 21 : 0);
                        if (mode >= 5)
                        {
                            out.writeInt(mode == 5 ? 0 : 1);
                            if (mode != 5)
                                out.writeUTF("Question?");
                            if (mode != 5)
                                out.writeInt(0);
                            if (mode == 6)
                            {
                                out.writeByte(99);
                            }
                        }
                    }
                }
            }
            IOException caught = null;
            try
            {
                storage.load("Broken");
            }
            catch (IOException e)
            {
                caught = e;
            }
            assertNotNull(
                "Expected malformed file rejection mode " + mode,
                caught);
        }
    }


    /** Invalid candidate counts, rankings and vote counts fail validation. */
    @Test
    public void testInvalidCandidateRecords()
        throws IOException
    {
        Path dir = Files.createTempDirectory("rapidballot-bad-candidate-");
        ElectionStorage storage = new ElectionStorage(dir);
        for (int mode = 0; mode < 9; mode++)
        {
            try (
                DataOutputStream out = new DataOutputStream(
                    Files.newOutputStream(dir.resolve("Bad.rball"))))
            {
                startFile(out, 1);
                out.writeUTF("Lead");
                out.writeInt(mode == 1 ? 5 : mode == 6 ? 3 : 1);
                out.writeInt(mode == 0 ? 0 : mode == 2 ? 21 : 2);
                if (mode != 0 && mode != 2)
                {
                    out.writeUTF("A");
                    out.writeUTF("B");
                    out.writeInt(mode == 3 ? -1 : 1);
                    if (mode != 3)
                    {
                        out.writeUTF("V1");
                        out.writeUTF("");
                        out.writeInt(
                            mode == 4 ? 0 : mode == 5 ? 3 : mode == 6 ? 1 : 1);
                        if (mode == 6)
                        {
                            out.writeUTF("A");
                        }
                        else if (mode == 7)
                        {
                            out.writeUTF("Missing");
                        }
                        else if (mode == 8)
                        {
                            out.writeUTF(" ");
                        }
                        else if (mode != 4 && mode != 5)
                        {
                            out.writeUTF("A");
                        }
                    }
                }
                out.writeInt(0);
            }
            IOException caught = null;
            try
            {
                storage.load("Bad");
            }
            catch (IOException e)
            {
                caught = e;
            }
            assertNotNull("Mode " + mode, caught);
        }
    }


    /** Invalid proposal counts, yes/no bytes and repeated IDs are rejected. */
    @Test
    public void testInvalidProposalRecords()
        throws IOException
    {
        Path dir = Files.createTempDirectory("rapidballot-bad-proposal-");
        ElectionStorage storage = new ElectionStorage(dir);
        for (int mode = 0; mode < 5; mode++)
        {
            try (
                DataOutputStream out = new DataOutputStream(
                    Files.newOutputStream(dir.resolve("Bad.rball"))))
            {
                startFile(out, 0);
                out.writeInt(mode == 0 ? -1 : mode == 1 ? 21 : 1);
                if (mode > 1)
                {
                    out.writeUTF("Approve?");
                    out.writeInt(mode == 2 ? -1 : mode == 4 ? 2 : 1);
                    if (mode != 2)
                    {
                        out.writeUTF("V1");
                        out.writeUTF("");
                        out.writeByte(mode == 3 ? 2 : 1);
                        if (mode == 4)
                        {
                            out.writeUTF("V1");
                            out.writeUTF("");
                            out.writeByte(0);
                        }
                    }
                }
            }
            IOException caught = null;
            try
            {
                storage.load("Bad");
            }
            catch (IOException e)
            {
                caught = e;
            }
            assertNotNull("Mode " + mode, caught);
        }
    }


    /** A multi-choice ballot in a plurality file is rejected on load. */
    @Test
    public void testSingleChoiceRejectsMultipleRanks()
        throws IOException
    {
        Path dir = Files.createTempDirectory("rapidballot-too-many-ranks-");
        try (
            DataOutputStream out = new DataOutputStream(
                Files.newOutputStream(dir.resolve("Bad.rball"))))
        {
            startFile(out, 1);
            out.writeUTF("Lead");
            out.writeInt(1);
            out.writeInt(2);
            out.writeUTF("A");
            out.writeUTF("B");
            out.writeInt(1);
            out.writeUTF("V1");
            out.writeUTF("");
            out.writeInt(2);
            out.writeUTF("A");
            out.writeUTF("B");
            out.writeInt(0);
        }
        IOException caught = null;
        try
        {
            new ElectionStorage(dir).load("Bad");
        }
        catch (IOException e)
        {
            caught = e;
        }
        assertNotNull(caught);
    }


    /** A never-created save directory initially contains no saved elections. */
    @Test
    public void testMissingDirectoryList()
        throws IOException
    {
        Path notCreated =
            Files.createTempDirectory("rapidballot-none-").resolve("missing");
        assertTrue(
            new ElectionStorage(notCreated).listSavedElections().isEmpty());
    }


    /**
     * Writes common header and contest-count fields for malformed-file tests.
     */
    private void startFile(DataOutputStream out, int contestCount)
        throws IOException
    {
        out.writeUTF("RAPIDBALLOT");
        out.writeInt(1);
        out.writeUTF("Election");
        out.writeInt(contestCount);
    }
}
