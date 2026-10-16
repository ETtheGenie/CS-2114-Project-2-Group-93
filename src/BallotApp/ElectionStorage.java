
package BallotApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Saves and loads complete RapidBallot elections in a simple binary format.
 * Each save file has a format version so invalid files can be rejected. Save
 * names do not contain the .rball extension.
 */
public class ElectionStorage
{
    private static final String EXTENSION = ".rball";
    private static final String HEADER = "RAPIDBALLOT";
    private static final int VERSION = 1;

    private Path directory;

    /**
     * Creates a storage manager for the given directory.
     *
     * @param directory
     *            folder containing saved elections
     * @throws IllegalArgumentException
     *             if directory is null or not a folder
     */
    public ElectionStorage(Path directory)
    {
        if (directory == null)
        {
            throw new IllegalArgumentException(
                "Save directory cannot be null.");
        }
        this.directory = directory.toAbsolutePath().normalize();
        if (Files.exists(this.directory) && !Files.isDirectory(this.directory))
        {
            throw new IllegalArgumentException(
                "Save location is not a directory.");
        }
    }


    /**
     * Saves every contest, proposal, voting method, and ballot. An existing
     * save with the same name is overwritten automatically.
     *
     * @param session
     *            election to save
     * @param filename
     *            save name without .rball
     * @throws IOException
     *             if the election cannot be written
     * @throws IllegalArgumentException
     *             if the session or name is invalid
     */
    public void save(ElectionSession session, String filename)
        throws IOException
    {
        if (session == null)
        {
            throw new IllegalArgumentException(
                "Election session cannot be null.");
        }
        Path file = getSavePath(filename);
        validateSession(session);
        Files.createDirectories(directory);

        try (
            DataOutputStream out =
                new DataOutputStream(Files.newOutputStream(file)))
        {
            out.writeUTF(HEADER);
            out.writeInt(VERSION);
            out.writeUTF(session.getName());

            List<CandidateContest> contests = session.getCandidateContests();
            out.writeInt(contests.size());
            for (CandidateContest contest : contests)
            {
                out.writeUTF(contest.getPosition());
                out.writeInt(contest.getVotingMethod());

                List<String> candidates = contest.getCandidates();
                out.writeInt(candidates.size());
                for (String candidate : candidates)
                {
                    out.writeUTF(candidate);
                }

                List<CandidateVote> votes = contest.getVotes();
                out.writeInt(votes.size());

                // The chain inserts at the head, so write oldest votes first.
                for (int i = votes.size() - 1; i >= 0; i--)
                {
                    CandidateVote vote = votes.get(i);
                    out.writeUTF(vote.getVoterId());
                    out.writeUTF(vote.getLocation());

                    List<String> rankings = vote.getRankedChoices();
                    out.writeInt(rankings.size());
                    for (String candidate : rankings)
                    {
                        out.writeUTF(candidate);
                    }
                }
            }

            List<ProposalContest> proposals = session.getProposals();
            out.writeInt(proposals.size());
            for (ProposalContest proposal : proposals)
            {
                out.writeUTF(proposal.getQuestion());

                List<ProposalVote> votes = proposal.getVotes();
                out.writeInt(votes.size());

                // Preserve the original order when votes are loaded again.
                for (int i = votes.size() - 1; i >= 0; i--)
                {
                    ProposalVote vote = votes.get(i);
                    out.writeUTF(vote.getVoterId());
                    out.writeUTF(vote.getLocation());
                    out.writeBoolean(vote.isYesVote());
                }
            }
        }
    }


    /**
     * Loads a saved election into a NEW session. A bad file causes IOException,
     * so the controller's current session is never changed by this method.
     *
     * @param filename
     *            save name without .rball
     * @return reconstructed election
     * @throws IOException
     *             if the file is missing, damaged, or unreadable
     * @throws IllegalArgumentException
     *             if the save name is invalid
     */
    public ElectionSession load(String filename)
        throws IOException
    {
        Path file = getSavePath(filename);

        try (
            DataInputStream in =
                new DataInputStream(Files.newInputStream(file)))
        {
            if (!HEADER.equals(in.readUTF()) || in.readInt() != VERSION)
            {
                throw new IOException("Invalid or unsupported election file.");
            }

            ElectionSession loaded = new ElectionSession(in.readUTF());
            int contestCount = in.readInt();

            if (contestCount < 0 || contestCount > InputParser.MAX_CONTESTS)
            {
                throw new IOException("Invalid number of candidate contests.");
            }

            for (int i = 0; i < contestCount; i++)
            {
                String position = in.readUTF();
                int method = in.readInt();
                int candidateCount = in.readInt();

                if (candidateCount < 1
                    || candidateCount > InputParser.MAX_CANDIDATES)
                {
                    throw new IOException("Invalid number of candidates.");
                }

                List<String> candidates = new ArrayList<String>();
                for (int j = 0; j < candidateCount; j++)
                {
                    candidates.add(in.readUTF());
                }

                CandidateContest contest =
                    loaded.addCandidateContest(position, candidates, method);

                int voteCount = in.readInt();
                if (voteCount < 0)
                {
                    throw new IOException("Invalid candidate vote count.");
                }

                for (int j = 0; j < voteCount; j++)
                {
                    String voterId = in.readUTF();
                    String location = in.readUTF();
                    int rankCount = in.readInt();

                    if (rankCount < 1 || rankCount > candidateCount)
                    {
                        throw new IOException("Invalid ballot ranking count.");
                    }

                    List<String> rankings = new ArrayList<String>();
                    for (int k = 0; k < rankCount; k++)
                    {
                        rankings.add(in.readUTF());
                    }

                    if (method == 3)
                    {
                        loaded.recordCandidateVote(
                            contest.getContestId(),
                            rankings,
                            voterId,
                            location);
                    }
                    else
                    {
                        if (rankCount != 1)
                        {
                            throw new IOException(
                                "Single-choice ballot has multiple choices.");
                        }

                        loaded.recordCandidateVote(
                            contest.getContestId(),
                            rankings.get(0),
                            voterId,
                            location);
                    }
                }
            }

            int proposalCount = in.readInt();
            if (proposalCount < 0 || proposalCount > InputParser.MAX_PROPOSALS)
            {
                throw new IOException("Invalid number of proposals.");
            }

            for (int i = 0; i < proposalCount; i++)
            {
                ProposalContest proposal = loaded.addProposal(in.readUTF());

                int voteCount = in.readInt();
                if (voteCount < 0)
                {
                    throw new IOException("Invalid proposal vote count.");
                }

                for (int j = 0; j < voteCount; j++)
                {
                    String voterId = in.readUTF();
                    String location = in.readUTF();
                    int yesByte = in.readUnsignedByte();

                    if (yesByte != 0 && yesByte != 1)
                    {
                        throw new IOException("Invalid yes/no vote value.");
                    }

                    loaded.recordProposalVote(
                        proposal.getProposalId(),
                        yesByte == 1,
                        voterId,
                        location);
                }
            }

            if (in.read() != -1)
            {
                throw new IOException(
                    "Unexpected extra data in election file.");
            }

            validateSession(loaded);
            return loaded;
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException(
                "Invalid saved election: " + e.getMessage(),
                e);
        }
    }


    /**
     * Lists save names (without .rball) in alphabetical order. Returns an empty
     * list if the save directory does not exist yet.
     *
     * @return names of available saved elections
     * @throws IOException
     *             if the directory cannot be read
     */
    public List<String> listSavedElections()
        throws IOException
    {
        List<String> names = new ArrayList<String>();

        if (!Files.exists(directory))
        {
            return names;
        }
        if (!Files.isDirectory(directory))
        {
            throw new IOException("Save location is not a directory.");
        }

        try (
            DirectoryStream<Path> files =
                Files.newDirectoryStream(directory, "*" + EXTENSION))
        {
            for (Path file : files)
            {
                if (Files.isRegularFile(file) && !Files.isSymbolicLink(file))
                {
                    String fullName = file.getFileName().toString();
                    String name = fullName
                        .substring(0, fullName.length() - EXTENSION.length());

                    try
                    {
                        InputParser.parseSaveName(name);
                        names.add(name);
                    }
                    catch (IllegalArgumentException e)
                    {
                        // Ignore files that do not have a valid save name.
                    }
                }
            }
        }

        Collections.sort(names);
        return names;
    }


    /**
     * Validates that a session can be safely reconstructed. New temporary
     * contests replay each ballot, checking IDs, rankings, and duplicate IDs.
     *
     * @param loaded
     *            session to validate
     * @throws IOException
     *             if any required election rule is broken
     */
    private void validateSession(ElectionSession loaded)
        throws IOException
    {
        if (loaded == null)
        {
            throw new IOException("Missing election session.");
        }

        List<CandidateContest> contests = loaded.getCandidateContests();
        List<ProposalContest> proposals = loaded.getProposals();

        if (contests.size() > InputParser.MAX_CONTESTS
            || proposals.size() > InputParser.MAX_PROPOSALS
            || (contests.isEmpty() && proposals.isEmpty()))
        {
            throw new IOException("Invalid election setup.");
        }

        try
        {
            ElectionSession check = new ElectionSession(loaded.getName());

            for (int i = 0; i < contests.size(); i++)
            {
                CandidateContest original = contests.get(i);

                if (original == null || original.getContestId() != i + 1)
                {
                    throw new IOException("Invalid candidate contest ID.");
                }

                CandidateContest copy = check.addCandidateContest(
                    original.getPosition(),
                    original.getCandidates(),
                    original.getVotingMethod());

                for (CandidateVote vote : original.getVotes())
                {
                    copy.recordVote(vote);
                }
            }

            for (int i = 0; i < proposals.size(); i++)
            {
                ProposalContest original = proposals.get(i);

                if (original == null || original.getProposalId() != i + 1)
                {
                    throw new IOException("Invalid proposal ID.");
                }

                ProposalContest copy =
                    check.addProposal(original.getQuestion());

                for (ProposalVote vote : original.getVotes())
                {
                    copy.recordVote(vote);
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException(
                "Invalid election data: " + e.getMessage(),
                e);
        }
    }


    /**
     * Validates a save name and builds a path inside this storage directory.
     */
    private Path getSavePath(String filename)
    {
        String name = InputParser.parseSaveName(filename);
        Path file = directory.resolve(name + EXTENSION).normalize();

        if (!file.getParent().equals(directory) || Files.isSymbolicLink(file))
        {
            throw new IllegalArgumentException("Invalid save file path.");
        }
        return file;
    }
}
