package BallotApp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Swing interface for the RapidBallot terminal workflow.
 *
 * The GUI keeps presentation logic separate from election logic. It uses the
 * public methods on ElectionSession, CandidateContest, and ProposalContest.
 *
 * @author Ben Seyoum
 * @version 2026.10.16
 */
public class RapidBallotGUI
{
    private static final String HOME = "home";
    private static final String MENU = "menu";
    private static final String CANDIDATE = "candidate";
    private static final String PROPOSAL = "proposal";
    private static final String CORRECT = "correct";
    private static final String RESULTS = "results";

    private ElectionSession session;
    private ElectionStorage storage;
    private String activeFilename;
    private JFrame frame;
    private CardLayout screens;
    private JPanel screenPanel;
    private JLabel activeSessionLabel;
    private JButton resumeButton;
    private JLabel sessionTitle;

    private JComboBox<String> candidateContestBox;
    private JComboBox<String> candidateBox;
    private JLabel candidatePrompt;
    private JTextField rankedField;
    private JTextField candidateVoterField;
    private JTextField candidateLocationField;
    private JLabel candidateStatus;

    private JComboBox<String> proposalBox;
    private JComboBox<String> proposalChoiceBox;
    private JTextField proposalVoterField;
    private JTextField proposalLocationField;
    private JLabel proposalStatus;

    private JComboBox<String> correctionTypeBox;
    private JComboBox<String> correctionItemBox;
    private JTextField correctionVoterField;
    private JLabel correctionStatus;

    private JTextArea resultsArea;


    /**
     * Creates the GUI without an active election, like the terminal main menu.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     */
    public RapidBallotGUI()
    {
        this(null);
    }


    /**
     * Creates and displays the GUI with an optional active election.
     *
     * Time complexity: O(1) for the fixed set of UI controls, excluding UI
     * framework work.
     *
     * @param electionSession
     *            session used by the GUI
     */
    public RapidBallotGUI(ElectionSession electionSession)
    {
        session = electionSession;
        storage = new ElectionStorage(Paths.get("elections"));
        buildWindow();
    }


    /**
     * Builds the frame and all screens.
     *
     * Time complexity: O(1) for the fixed set of UI controls, excluding UI
     * framework work.
     */
    private void buildWindow()
    {
        frame = new JFrame("RapidBallot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(760, 560));

        screens = new CardLayout();
        screenPanel = new JPanel(screens);

        screenPanel.add(buildHome(), HOME);
        screenPanel.add(buildMenu(), MENU);
        screenPanel.add(buildCandidateScreen(), CANDIDATE);
        screenPanel.add(buildProposalScreen(), PROPOSAL);
        screenPanel.add(buildCorrectionScreen(), CORRECT);
        screenPanel.add(buildResultsScreen(), RESULTS);

        frame.add(screenPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        updateSessionDisplay();
        showScreen(session == null ? HOME : MENU);
        frame.setVisible(true);
    }


    /**
     * Creates the main menu for creating, loading, or resuming elections.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     */
    private JPanel buildHome()
    {
        JPanel panel = makeScreenPanel("RapidBallot Main Menu");
        JPanel buttons = new JPanel(new GridLayout(4, 1, 12, 12));
        buttons.setBorder(BorderFactory.createEmptyBorder(55, 160, 40, 160));

        JButton createButton = new JButton("Create New Election");
        JButton loadButton = new JButton("Load Saved Election");
        resumeButton = new JButton("Resume Active Election");
        JButton exitButton = new JButton("Exit");

        createButton.addActionListener(e -> createElection());
        loadButton.addActionListener(e -> loadElection());
        resumeButton.addActionListener(e -> showScreen(MENU));
        exitButton.addActionListener(e -> frame.dispose());

        buttons.add(createButton);
        buttons.add(loadButton);
        buttons.add(resumeButton);
        buttons.add(exitButton);
        activeSessionLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(activeSessionLabel, BorderLayout.SOUTH);
        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Creates the active-election menu.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return menu panel
     */
    private JPanel buildMenu()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        sessionTitle = new JLabel("RapidBallot", SwingConstants.CENTER);
        sessionTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        panel.add(sessionTitle, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(6, 1, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(35, 160, 35, 160));

        JButton candidateButton = new JButton("Candidate Vote");
        JButton proposalButton = new JButton("Proposal Vote");
        JButton correctionButton = new JButton("Correct Vote");
        JButton resultsButton = new JButton("View Results");
        JButton saveButton = new JButton("Save Election");
        JButton homeButton = new JButton("Return to Main Menu");

        candidateButton.addActionListener(e -> {
            refreshCandidateContests();
            showScreen(CANDIDATE);
        });

        proposalButton.addActionListener(e -> {
            refreshProposals();
            showScreen(PROPOSAL);
        });

        correctionButton.addActionListener(e -> {
            refreshCorrectionItems();
            showScreen(CORRECT);
        });

        resultsButton.addActionListener(e -> {
            refreshResults();
            showScreen(RESULTS);
        });
        saveButton.addActionListener(e -> saveElection());
        homeButton.addActionListener(e -> showScreen(HOME));

        buttons.add(candidateButton);
        buttons.add(proposalButton);
        buttons.add(correctionButton);
        buttons.add(resultsButton);
        buttons.add(saveButton);
        buttons.add(homeButton);

        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Collects the same election setup fields as the terminal controller.
     * Cancelling any prompt leaves the active election unchanged.
     *
     * Time complexity: O(T + sum K_i^2 L), where T is entered text length,
     * K_i is each contest's candidate count, and L is maximum name length.
     */
    private void createElection()
    {
        String name = askRequiredText("Session name:",
            InputParser.MAX_NAME_LENGTH);
        if (name == null)
        {
            return;
        }

        Integer contestCount;
        Integer proposalCount;
        while (true)
        {
            contestCount = askNumber("Number of candidate contests (0-20):",
                0, InputParser.MAX_CONTESTS);
            if (contestCount == null)
            {
                return;
            }
            proposalCount = askNumber("Number of proposals (0-20):",
                0, InputParser.MAX_PROPOSALS);
            if (proposalCount == null)
            {
                return;
            }
            try
            {
                InputParser.validateSetupCounts(contestCount, proposalCount);
                break;
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }

        ElectionSession newSession = new ElectionSession(name);
        for (int i = 0; i < contestCount;)
        {
            String position = askRequiredText(
                "Candidate contest " + (i + 1) + " position name:",
                InputParser.MAX_NAME_LENGTH);
            if (position == null)
            {
                return;
            }
            Integer method = askVotingMethod();
            if (method == null)
            {
                return;
            }
            Integer candidateCount = askNumber(
                "Number of candidates for " + position + " (1-20):",
                1, InputParser.MAX_CANDIDATES);
            if (candidateCount == null)
            {
                return;
            }

            List<String> names = new ArrayList<String>();
            for (int j = 0; j < candidateCount;)
            {
                String candidate = askCandidateName(
                    "Candidate " + (j + 1) + " name:", names);
                if (candidate == null)
                {
                    return;
                }
                names.add(candidate);
                j++;
            }
            try
            {
                newSession.addCandidateContest(position, names, method);
                i++;
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }

        for (int i = 0; i < proposalCount;)
        {
            String question = askRequiredText(
                "Proposal " + (i + 1) + " question:",
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
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }

        activateSession(newSession, null);
    }


    /**
     * Lets the user select and load a saved election. A failed load does not
     * replace the active election.
     *
     * Time complexity: O(D^2 + D log D + sum load costs), where D is saved
     * files; listing currently retrieves each name by linked-chain index.
     */
    private void loadElection()
    {
        List<String> names;
        try
        {
            names = storage.listSavedElections();
        }
        catch (IOException exception)
        {
            showError("Could not list saved elections: "
                + exception.getMessage());
            return;
        }
        if (names.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "No saved elections found.");
            return;
        }

        while (true)
        {
            String filename = (String)JOptionPane.showInputDialog(frame,
                "Choose a saved election:", "Load Election",
                JOptionPane.QUESTION_MESSAGE, null,
                names.toArray(new String[0]), names.get(0));
            if (filename == null)
            {
                return;
            }
            try
            {
                ElectionSession loaded = storage.load(filename);
                activateSession(loaded, filename);
                return;
            }
            catch (IOException | IllegalArgumentException exception)
            {
                showError("Could not load election: "
                    + exception.getMessage());
            }
        }
    }


    /**
     * Saves the active election, reusing its current save name when available.
     * As in the terminal, saving under an existing name overwrites that file.
     *
     * Time complexity: O(sum save costs over attempts).
     */
    private void saveElection()
    {
        if (session == null)
        {
            showError("No election to save.");
            return;
        }
        String filename = activeFilename;
        while (true)
        {
            if (filename == null)
            {
                filename = askSaveName();
                if (filename == null)
                {
                    return;
                }
            }
            try
            {
                storage.save(session, filename);
                activeFilename = filename;
                JOptionPane.showMessageDialog(frame,
                    "Election saved as: " + filename);
                return;
            }
            catch (IOException | IllegalArgumentException exception)
            {
                showError("Could not save election: "
                    + exception.getMessage());
                int retry = JOptionPane.showConfirmDialog(frame,
                    "Try a different save name?", "Save Election",
                    JOptionPane.YES_NO_OPTION);
                if (retry != JOptionPane.YES_OPTION)
                {
                    return;
                }
                filename = null;
            }
        }
    }


    /**
     * Makes a completed setup or load the active session.
     *
     * Time complexity: O(1), excluding UI framework work.
     */
    private void activateSession(ElectionSession next, String filename)
    {
        session = next;
        activeFilename = filename;
        candidateStatus.setText(" ");
        proposalStatus.setText(" ");
        correctionStatus.setText(" ");
        candidateVoterField.setText("");
        candidateLocationField.setText("");
        rankedField.setText("");
        proposalVoterField.setText("");
        proposalLocationField.setText("");
        correctionVoterField.setText("");
        updateSessionDisplay();
        showScreen(MENU);
    }


    /**
     * Updates the active-election labels and Resume button.
     *
     * Time complexity: O(L), where L is the session name length.
     */
    private void updateSessionDisplay()
    {
        String name = session == null ? null : session.getName();
        activeSessionLabel.setText(name == null
            ? "No active election"
            : "Active election: " + name);
        resumeButton.setEnabled(name != null);
        sessionTitle.setText(name == null ? "RapidBallot" : name);
        frame.setTitle(name == null ? "RapidBallot" : "RapidBallot - " + name);
    }


    /**
     * Prompts until the user enters valid required text or cancels.
     *
     * Time complexity: O(T), where T is total text entered across attempts.
     */
    private String askRequiredText(String prompt, int max)
    {
        while (true)
        {
            String entered = JOptionPane.showInputDialog(frame, prompt);
            if (entered == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseRequiredText(entered, max);
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }
    }


    /**
     * Prompts until the user enters a number in range or cancels.
     *
     * Time complexity: O(T), where T is total text entered across attempts.
     */
    private Integer askNumber(String prompt, int min, int max)
    {
        while (true)
        {
            String entered = JOptionPane.showInputDialog(frame, prompt);
            if (entered == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseNumberInRange(entered, min, max);
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }
    }


    /**
     * Prompts for a unique candidate name or cancels setup.
     *
     * Time complexity: O(A N L + T), where A is attempts, N is existing
     * candidates, L is maximum name length, and T is total entered text.
     */
    private String askCandidateName(String prompt, List<String> existing)
    {
        while (true)
        {
            String entered = JOptionPane.showInputDialog(frame, prompt);
            if (entered == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseCandidateName(entered, existing);
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }
    }


    /**
     * Returns the selected voting method, or null when setup is cancelled.
     *
     * Time complexity: O(1) for three fixed choices.
     */
    private Integer askVotingMethod()
    {
        String[] methods = { "Plurality", "Majority required",
            "Ranked choice" };
        String choice = (String)JOptionPane.showInputDialog(frame,
            "Voting method:", "Candidate Contest",
            JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
        if (choice == null)
        {
            return null;
        }
        return Arrays.asList(methods).indexOf(choice) + 1;
    }


    /**
     * Prompts until the user enters a valid save name or cancels.
     *
     * Time complexity: O(T), where T is total text entered across attempts.
     */
    private String askSaveName()
    {
        while (true)
        {
            String entered = JOptionPane.showInputDialog(frame,
                "Save name (without .rball):");
            if (entered == null)
            {
                return null;
            }
            try
            {
                return InputParser.parseSaveName(entered);
            }
            catch (IllegalArgumentException exception)
            {
                showError(exception.getMessage());
            }
        }
    }


    /**
     * Shows a validation or storage error.
     *
     * Time complexity: O(L), where L is the message length.
     */
    private void showError(String message)
    {
        JOptionPane.showMessageDialog(frame, message, "RapidBallot",
            JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Creates the candidate voting screen.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return candidate panel
     */
    private JPanel buildCandidateScreen()
    {
        JPanel panel = makeScreenPanel("Candidate Vote");
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        candidateContestBox = new JComboBox<String>();
        candidateBox = new JComboBox<String>();
        candidatePrompt = new JLabel("Candidate:");
        rankedField = new JTextField();
        rankedField.setVisible(false);

        candidateVoterField = new JTextField();
        candidateLocationField = new JTextField();
        candidateStatus = new JLabel(" ");

        candidateContestBox.addActionListener(e -> {
            refreshCandidateChoices();
        });

        form.add(new JLabel("Contest:"));
        form.add(candidateContestBox);
        form.add(candidatePrompt);
        form.add(candidateBox);
        form.add(new JLabel("Ranking (comma separated):"));
        form.add(rankedField);
        form.add(new JLabel("Voter ID:"));
        form.add(candidateVoterField);
        form.add(new JLabel("Location (optional):"));
        form.add(candidateLocationField);

        JButton submit = new JButton("Submit Candidate Vote");
        submit.addActionListener(e -> submitCandidateVote());
        form.add(submit);
        form.add(candidateStatus);

        JButton back = new JButton("Back");
        back.addActionListener(e -> showScreen(MENU));
        form.add(back);
        form.add(new JLabel(""));

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Creates the proposal voting screen.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return proposal panel
     */
    private JPanel buildProposalScreen()
    {
        JPanel panel = makeScreenPanel("Proposal Vote");
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(45, 80, 45, 80));

        proposalBox = new JComboBox<String>();
        proposalChoiceBox = new JComboBox<String>(
            new String[] { "Yes", "No" });
        proposalVoterField = new JTextField();
        proposalLocationField = new JTextField();
        proposalStatus = new JLabel(" ");

        form.add(new JLabel("Proposal:"));
        form.add(proposalBox);
        form.add(new JLabel("Vote:"));
        form.add(proposalChoiceBox);
        form.add(new JLabel("Voter ID:"));
        form.add(proposalVoterField);
        form.add(new JLabel("Location (optional):"));
        form.add(proposalLocationField);

        JButton submit = new JButton("Submit Proposal Vote");
        submit.addActionListener(e -> submitProposalVote());
        form.add(submit);
        form.add(proposalStatus);

        JButton back = new JButton("Back");
        back.addActionListener(e -> showScreen(MENU));
        form.add(back);
        form.add(new JLabel(""));

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Creates the vote correction screen.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return correction panel
     */
    private JPanel buildCorrectionScreen()
    {
        JPanel panel = makeScreenPanel("Correct Vote");
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(65, 80, 65, 80));

        correctionTypeBox = new JComboBox<String>(
            new String[] { "Candidate Contest", "Proposal" });
        correctionItemBox = new JComboBox<String>();
        correctionVoterField = new JTextField();
        correctionStatus = new JLabel(" ");

        correctionTypeBox.addActionListener(e -> refreshCorrectionItems());

        form.add(new JLabel("Vote type:"));
        form.add(correctionTypeBox);
        form.add(new JLabel("Contest / proposal:"));
        form.add(correctionItemBox);
        form.add(new JLabel("Voter ID to remove:"));
        form.add(correctionVoterField);

        JButton remove = new JButton("Remove Vote");
        remove.addActionListener(e -> correctVote());
        form.add(remove);
        form.add(correctionStatus);

        JButton back = new JButton("Back");
        back.addActionListener(e -> showScreen(MENU));
        form.add(back);
        form.add(new JLabel(""));

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Creates the results screen.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return results panel
     */
    private JPanel buildResultsScreen()
    {
        JPanel panel = makeScreenPanel("Election Results");

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton refresh = new JButton("Refresh");
        JButton back = new JButton("Back");

        refresh.addActionListener(e -> refreshResults());
        back.addActionListener(e -> showScreen(MENU));

        buttons.add(refresh);
        buttons.add(back);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }


    /**
     * Creates a panel with a common title area.
     *
     * Time complexity: O(1), excluding UI framework work.
     *
     * @param titleText
     *            screen title
     * @return panel
     */
    private JPanel makeScreenPanel(String titleText)
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        panel.add(title, BorderLayout.NORTH);
        return panel;
    }


    /**
     * Refreshes the candidate contest drop-down.
     *
     * Time complexity: O(C + K), where C is the contest count and K is the
     * selected contest's candidate count.
     */
    private void refreshCandidateContests()
    {
        candidateContestBox.removeAllItems();

        for (CandidateContest contest : session.getCandidateContests())
        {
            candidateContestBox.addItem(
                contest.getContestId() + " - " + contest.getPosition());
        }

        refreshCandidateChoices();
    }


    /**
     * Updates candidate controls for the currently selected contest.
     *
     * Time complexity: O(C + K), where C is the contest count and K is the
     * selected contest's candidate count.
     */
    private void refreshCandidateChoices()
    {
        candidateBox.removeAllItems();
        int index = candidateContestBox.getSelectedIndex();

        if (index < 0)
        {
            return;
        }

        CandidateContest contest = session.getCandidateContests().get(index);
        for (String candidate : contest.getCandidates())
        {
            candidateBox.addItem(candidate);
        }

        boolean ranked = contest.getVotingMethod() == 3;
        candidateBox.setEnabled(!ranked);
        rankedField.setVisible(ranked);
        candidatePrompt.setText(ranked
            ? "Candidate (ranking field used):"
            : "Candidate:");
    }


    /**
     * Records a candidate vote using ElectionSession.
     *
     * Time complexity: O(K^3 L + V L + T) for ranked votes and O(K^2 L + V L
     * + T) otherwise, where V is stored votes and T is entered text length.
     */
    private void submitCandidateVote()
    {
        int index = candidateContestBox.getSelectedIndex();
        if (index < 0)
        {
            candidateStatus.setText("No candidate contests available.");
            return;
        }

        CandidateContest contest = session.getCandidateContests().get(index);
        String voterId = candidateVoterField.getText();
        String location = candidateLocationField.getText();

        try
        {
            if (contest.getVotingMethod() == 3)
            {
                List<String> rankings = parseRanking(rankedField.getText());
                session.recordCandidateVote(
                    contest.getContestId(),
                    rankings,
                    voterId,
                    location);
            }
            else
            {
                String candidate = (String)candidateBox.getSelectedItem();
                session.recordCandidateVote(
                    contest.getContestId(),
                    candidate,
                    voterId,
                    location);
            }

            candidateStatus.setText("Vote recorded.");
            candidateVoterField.setText("");
            candidateLocationField.setText("");
            rankedField.setText("");
        }
        catch (IllegalArgumentException exception)
        {
            candidateStatus.setText("Error: " + exception.getMessage());
        }
    }


    /**
     * Converts a comma-separated ranking into a list.
     *
     * Time complexity: O(L), where L is the ranking text length.
     *
     * @param text
     *            ranking text
     * @return ranking entries
     */
    private List<String> parseRanking(String text)
    {
        List<String> rankings = new ArrayList<String>();
        if (text == null || text.trim().isEmpty())
        {
            return rankings;
        }

        String[] parts = text.split(",");
        for (String part : parts)
        {
            rankings.add(part.trim());
        }
        return rankings;
    }


    /**
     * Refreshes the proposal drop-down.
     *
     * Time complexity: O(P), where P is the proposal count.
     */
    private void refreshProposals()
    {
        proposalBox.removeAllItems();
        for (ProposalContest proposal : session.getProposals())
        {
            proposalBox.addItem(
                proposal.getProposalId() + " - " + proposal.getQuestion());
        }
    }


    /**
     * Records a proposal vote using ElectionSession.
     *
     * Time complexity: O(P + V L + T), where P is the proposal count, V is
     * stored votes, and T is entered text length.
     */
    private void submitProposalVote()
    {
        int index = proposalBox.getSelectedIndex();
        if (index < 0)
        {
            proposalStatus.setText("No proposals available.");
            return;
        }

        ProposalContest proposal = session.getProposals().get(index);
        boolean yes = proposalChoiceBox.getSelectedIndex() == 0;

        try
        {
            session.recordProposalVote(
                proposal.getProposalId(),
                yes,
                proposalVoterField.getText(),
                proposalLocationField.getText());

            proposalStatus.setText("Vote recorded.");
            proposalVoterField.setText("");
            proposalLocationField.setText("");
        }
        catch (IllegalArgumentException exception)
        {
            proposalStatus.setText("Error: " + exception.getMessage());
        }
    }


    /**
     * Refreshes the correction contest/proposal drop-down.
     *
     * Time complexity: O(C + P), where C and P are contest and proposal
     * counts.
     */
    private void refreshCorrectionItems()
    {
        if (correctionItemBox == null)
        {
            return;
        }

        correctionItemBox.removeAllItems();
        boolean candidateType = correctionTypeBox.getSelectedIndex() == 0;

        if (candidateType)
        {
            for (CandidateContest contest : session.getCandidateContests())
            {
                correctionItemBox.addItem(
                    contest.getContestId() + " - " + contest.getPosition());
            }
        }
        else
        {
            for (ProposalContest proposal : session.getProposals())
            {
                correctionItemBox.addItem(
                    proposal.getProposalId() + " - "
                        + proposal.getQuestion());
            }
        }
    }


    /**
     * Removes a vote by voter ID.
     *
     * Time complexity: O(C + P + V L), where V is the selected item's vote
     * count and L is the voter ID length.
     */
    private void correctVote()
    {
        int index = correctionItemBox.getSelectedIndex();
        if (index < 0)
        {
            correctionStatus.setText("Nothing is available to correct.");
            return;
        }

        String voterId = correctionVoterField.getText();

        try
        {
            boolean removed;
            if (correctionTypeBox.getSelectedIndex() == 0)
            {
                CandidateContest contest =
                    session.getCandidateContests().get(index);
                removed = session.correctCandidateVote(
                    contest.getContestId(), voterId);
            }
            else
            {
                ProposalContest proposal = session.getProposals().get(index);
                removed = session.correctProposalVote(
                    proposal.getProposalId(), voterId);
            }

            correctionStatus.setText(
                removed ? "Vote removed." : "No matching vote found.");
            correctionVoterField.setText("");
        }
        catch (IllegalArgumentException exception)
        {
            correctionStatus.setText("Error: " + exception.getMessage());
        }
    }


    /**
     * Rebuilds the results text from the current session.
     *
     * Time complexity: O(sum(V_i^2 + K_i^3 V_i + R_i^2)), where each V_i is
     * an item's vote count, K_i its candidate count, and R_i its result
     * length.
     */
    private void refreshResults()
    {
        StringBuilder output = new StringBuilder();
        output.append(session.getName()).append("\n");
        output.append("========================================\n\n");

        List<CandidateContest> contests = session.getCandidateContests();
        if (contests.isEmpty())
        {
            output.append("No candidate contests.\n\n");
        }
        else
        {
            for (CandidateContest contest : contests)
            {
                output.append("Candidate Contest #")
                    .append(contest.getContestId())
                    .append(": ")
                    .append(contest.getPosition())
                    .append("\n");

                if (contest.getVotingMethod() == 1)
                {
                    output.append(contest.getPluralityResultSummary());
                }
                else if (contest.getVotingMethod() == 2)
                {
                    output.append(contest.getMajorityResultSummary());
                }
                else
                {
                    output.append(contest.getRankedChoiceResultSummary());
                }
                output.append("\n\n");
            }
        }

        List<ProposalContest> proposals = session.getProposals();
        if (proposals.isEmpty())
        {
            output.append("No proposals.\n");
        }
        else
        {
            for (ProposalContest proposal : proposals)
            {
                output.append("Proposal #")
                    .append(proposal.getProposalId())
                    .append(": ")
                    .append(proposal.getQuestion())
                    .append("\n")
                    .append(proposal.getResultSummary())
                    .append("\n\n");
            }
        }

        resultsArea.setText(output.toString());
        resultsArea.setCaretPosition(0);
    }


    /**
     * Shows one GUI screen.
     *
     * Time complexity: O(1) for the fixed set of screens, excluding UI
     * framework work.
     *
     * @param name
     *            screen name
     */
    private void showScreen(String name)
    {
        screens.show(screenPanel, name);
    }


    /**
     * Opens the graphical main menu without an active election.
     *
     * Time complexity: O(1) for fixed UI controls, excluding UI framework
     * work.
     *
     * @param args
     *            unused command-line arguments
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new RapidBallotGUI());
    }
}
