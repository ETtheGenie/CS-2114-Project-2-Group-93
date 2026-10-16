# RapidBallot — Java skeleton (MVP + selected stretch goals)

## What is included

The 8 original MVP top-level classes from **CS 2114 Project 2 Spec.pdf**:

- `RapidBallotApp.java` — terminal controller and menu helpers.
- `InputParser.java` — static text validation.
- `ElectionSession.java` — named session and contest/proposal routing.
- `CandidateContest.java` — candidate races, ballot storage, tallying.
- `ProposalContest.java` — yes/no proposals and results.
- `CandidateVote.java` — immutable candidate ballot.
- `ProposalVote.java` — immutable yes/no ballot.
- `LinkedChain.java` — generic singly linked chain with nested `Node<E>` and `ChainIterator`.

And 1 new class for the *user-requested stretch goals*:

- `ElectionStorage.java` — save, load, list, and validation API.

All the MVP method signatures from the spec (pp. 5–7) are included, even where a stretch goal adds overloads. New public/private APIs are marked `STRETCH` in Javadoc. The code is intentionally unimplemented: constructors and operational methods throw `UnsupportedOperationException("Not implemented yet.")` until we implement them one at a time. The utility class private constructor and unsupported iterator removal are exceptions; those are trivial complete behaviors. No active application behavior is implemented.

## Proposed stretch-goal design (not specified in the MVP)

### Voting methods

- Every `CandidateContest` stores an `int votingMethod` (1 = plurality, 2 = majority required, 3 = ranked choice). The original three-argument constructor and `ElectionSession.addCandidateContest(position, names)` default to option `1` when implemented.
- During administrator setup, `RapidBallotApp` asks the user to select option 1/2/3 for each candidate contest and stores it through `ElectionSession.addCandidateContest(position, names, int method)`.
- During results, `RapidBallotApp.runViewResults()` checks `contest.getVotingMethod()` and dispatches to `getPluralityResultSummary()`, `getMajorityResultSummary()`, or `getRankedChoiceResultSummary()`. The existing MVP `getResultSummary()` remains available as the plurality result. The contest classes still own the tally logic, not the controller.
- Method 2 (majority-required): more than half of valid ballots required; otherwise no winner is declared.
- Method 3 (ranked-choice): instant-runoff voting (IRV). This proposal requires each ranked ballot to rank **every** configured candidate exactly once. Counting rules for elimination ties remain to be agreed on.
- `CandidateVote` retains its MVP single-choice constructor and getter. For IRV it also contains an immutable ordered ranking. `getCandidateName()` returns the first preference, and `tallyVotes()` returns first-choice counts. Elimination-round results appear in `getRankedChoiceResultSummary()`.

### Persistence and session navigation

- `ElectionStorage` holds a save-directory `Path`, saves/loads by filename, lists saved files, and validates complete reconstructed sessions before they can replace the active session.
- `CandidateContest.getVotes()` and `ProposalContest.getVotes()` return defensive snapshots so saved files can capture every ballot without breaking encapsulation. Loading can rebuild sessions through validated constructors and `recordVote` methods.
- `RapidBallotApp.runMainMenu()` creates or loads a session; `runSessionMenu()` operates on it. `activeFilename` tracks the current file so a loaded session can be saved again.
- The chosen file extension, encoding, overwrite-confirmation policy, and whether saves should be atomic are to be determined during implementation; standard Java library classes are sufficient. A version marker and safe-name checking are recommended.

## Unresolved decisions (defer until relevant methods)

1. Ranked-choice elimination ties: declare an unresolved tie, use a tie-break criterion, or another policy?
2. Full versus partial rankings: this scaffold assumes *full rankings*.
3. Save format and overwriting: a versioned text format and confirmation before replacement are recommended.

## Compile

These files are in the **default Java package** (no `package` declaration) because the course project package name was not provided. If your IDE/project uses a named package, add the **same matching package declaration** to each `.java` file.

```bash
javac *.java
```

Compilation checks Java syntax and type references only; running the app will throw `UnsupportedOperationException` until methods are implemented. The stub classes were compiled successfully with Java 21 and contain no syntax errors.

## Source-of-truth policy

MVP APIs follow the submitted specification. New stretch features are proposals and can be revised before writing implementation logic. Individual class/method implementations should replace stub bodies without changing signatures unless agreed first.




## Next Steps:

The model, contest, session, and storage layers are implemented, and everything compiles under Java. Two hashCode warnings come up but can be fixed easily. And our stretch goal of  a GUI for our project will be committed in the future.

What’s left for the MVP: Polishing our classes and making sure everything runs as intended , Every signature from the spec (pp. 5-7) matches, making sure No menu path reaches an UnsupportedOperationException

Small cleanups:

-Delete EthanGearhart.java. Placeholder that prints “Ethan exists.”
-Add hashCode() to CandidateVote and ProposalVote to clear the compiler warnings.
-Decide whether our ElectionChain + top-level Node setup is acceptable.

Stretch goals:
Implementing a GUI for our entire app. 
The voting methods and ElectionStorage already have been written. What remains is the controller side:

Testing: A coverage check to find branch gaps, Testing for our GUI when it gets implemented

Documentation and polish
-Rewrite the README. It still describes an unimplemented skeleton, LinkedChain, and the default package, and we now use package BallotApp;.
-Record the decisions we actually implemented: IRV tie policy, full vs. partial rankings, and save format and overwrite behavior
-Remove STRETCH and “Not implemented” wording from the Javadoc and add a “How to run” section with sample output
