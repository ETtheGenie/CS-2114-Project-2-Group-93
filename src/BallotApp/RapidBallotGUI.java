package BallotApp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Swing prototype for RapidBallot.
 *
 * The GUI keeps presentation logic separate from election logic. It uses the
 * public methods on ElectionSession, CandidateContest, and ProposalContest.
 *
 * @author Ben Seyoum
 * @version 2026.10.16
 */
public class RapidBallotGUI
{
    private static final String MENU = "menu";
    private static final String CANDIDATE = "candidate";
    private static final String PROPOSAL = "proposal";
    private static final String CORRECT = "correct";
    private static final String RESULTS = "results";

    private ElectionSession session;
    private JFrame frame;
    private CardLayout screens;
    private JPanel screenPanel;

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
     * Creates and displays the GUI for an election session.
     *
     * Time complexity: O(1) for the fixed set of UI controls, excluding UI
     * framework work.
     *
     * @param electionSession
     *            session used by the GUI
     */
    public RapidBallotGUI(ElectionSession electionSession)
    {
        if (electionSession == null)
        {
            throw new IllegalArgumentException(
                "Election session cannot be null.");
        }

        session = electionSession;
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
        frame = new JFrame("RapidBallot - " + session.getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(760, 560));

        screens = new CardLayout();
        screenPanel = new JPanel(screens);

        screenPanel.add(buildMenu(), MENU);
        screenPanel.add(buildCandidateScreen(), CANDIDATE);
        screenPanel.add(buildProposalScreen(), PROPOSAL);
        screenPanel.add(buildCorrectionScreen(), CORRECT);
        screenPanel.add(buildResultsScreen(), RESULTS);

        frame.add(screenPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    /**
     * Creates the main menu.
     *
     * Time complexity: O(1) for the fixed set of UI controls.
     *
     * @return menu panel
     */
    private JPanel buildMenu()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("RapidBallot", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(4, 1, 12, 12));
        buttons.setBorder(BorderFactory.createEmptyBorder(55, 160, 55, 160));

        JButton candidateButton = new JButton("Candidate Vote");
        JButton proposalButton = new JButton("Proposal Vote");
        JButton correctionButton = new JButton("Correct Vote");
        JButton resultsButton = new JButton("View Results");

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

        buttons.add(candidateButton);
        buttons.add(proposalButton);
        buttons.add(correctionButton);
        buttons.add(resultsButton);

        panel.add(buttons, BorderLayout.CENTER);
        return panel;
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
     * Standalone preview using sample election data. This allows the GUI to be
     * tested before it is wired into the terminal controller.
     *
     * Time complexity: O(1) for fixed sample data and UI controls, excluding
     * UI framework work.
     *
     * @param args
     *            unused command-line arguments
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            ElectionSession demo = new ElectionSession("GUI Prototype");
            demo.addCandidateContest(
                "President",
                Arrays.asList("Alex", "Jordan", "Taylor"));
            demo.addCandidateContest(
                "Treasurer",
                Arrays.asList("Morgan", "Casey"),
                2);
            demo.addProposal("Approve the student activity budget?");

            new RapidBallotGUI(demo);
        });
    }
}
