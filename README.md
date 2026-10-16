# RapidBallot — Project 2 Deliverable 3: Prototype + Tests

## Project Overview

RapidBallot is a Java terminal application for small elections in clubs, classrooms, and student organizations. An administrator creates a named election session with candidate contests and/or yes/no proposals. Users can cast ballots, remove an incorrect ballot by voter ID, and view totals and results. The application uses the same generic ElectionChain<T> implementation to store and retrieve three different element types: CandidateVote for candidate ballots, ProposalVote for proposal ballots, and String for saved election filenames.

The **MVP workflow is implemented**, along with two stretch features: selectable voting methods (plurality, majority-required, and ranked choice) and saving/loading multiple election sessions. A graphical interface and formatted results export are not implemented.

### Main Java files

| Class | Responsibility |
| --- | --- |
| `RapidBallotApp` | Terminal menus, setup, voting, correction, result display, save/load navigation, and input retry loops |

| `InputParser` | Static validation and normalization of entered strings and numbers |

| `ElectionSession` | Owns candidate contests and proposals and routes vote operations |

| `CandidateContest` | Stores candidate ballots, prevents duplicate voter IDs, and calculates results for three voting methods |

| `ProposalContest` | Stores yes/no ballots and calculates pass/fail results |

| `CandidateVote` / `ProposalVote` | Individual ballot records with validated voter IDs and locations |

| `ElectionChain<T>` / `Node<T>` | Reusable generic, singly linked storage with head insertion and value-based searching/removal |

| `ElectionStorage` | Versioned binary `.rball` files, save/load operations, and validation of reconstructed sessions |


## Running and Testing

1. Keep the application source files in the `BallotApp` package in Eclipse.
2. Run `RapidBallotApp.main()` as a Java application.
3. From the main menu, create a new election or load a saved election. In an active session, record candidate or proposal votes, correct a vote by voter ID, view results, or save the session.
4. Add **JUnit 4** to the project's build path, keep the test classes in package `BallotApp`, and run them as **JUnit Test** in Eclipse.

Save files use the `.rball` extension and are stored in an `elections` directory relative to the program's working directory. Saving a session under an existing name overwrites that file **without confirmation**, as deliberately chosen for this prototype. Saving is not automatic when exiting.

**Test status:** The current generated suite contains 70 JUnit 4 test methods across 11 test classes. In the prior local JUnit-compatible test runner, 70 passed and measured line coverage was approximately **94.8%**, not 100%.
---

## Reflection

### 1. What changed from our specification, and why?

The original specification described a terminal MVP with `RapidBallotApp`, `InputParser`, `ElectionSession`, separate candidate/proposal contest and vote classes, and a generic `LinkedChain<T>`. The basic feature set is present, but we made several implementation changes:

- **More stretch functionality than originally required.** Although the revised scope excluded persistence and alternative voting methods from the MVP, we implemented them during prototype development. `ElectionStorage` was added for saving, loading, and listing elections. `CandidateContest` gained `votingMethod` (1 = plurality, 2 = majority-required, 3 = ranked-choice) and specialized result methods. `CandidateVote` gained an ordered rankings constructor and `getRankedChoices()`; both contest classes gained `getVotes()` for persistence. These changes allow the same program to support more election types and continue elections between runs.

- **Different terminal navigation.** Instead of one menu with five choices, `RapidBallotApp` now has a main menu for creating/loading/resuming elections and a separate active-session menu for voting, correcting, viewing results, saving, and returning. This keeps file management separate from casting ballots. Corrections remove the old ballot; the user can then submit a replacement through the normal voting menu.

- **Simplified and expanded parsing API.** The planned `parseMenuChoice`, `parseCandidateChoice`, and `parseVoterId` methods were not implemented as separate methods. We instead use general methods including `parseNumberInRange`, `parseChoiceFromList`, and `parseRequiredText` to avoid unnecessary repeated code. We added `validateSetupCounts`, `parseCandidateName`, `parseRankedChoices`, and `parseSaveName` for the expanded workflow.

- **Validation responsibilities were retained.** `InputParser` rejects and normalizes bad text; the model constructors and contest classes independently enforce valid data and prevent duplicate voter IDs; `RapidBallotApp` catches expected `IllegalArgumentException`s and retries input where appropriate. End-of-input ends the program instead of creating an incomplete vote. One difference is that entering zero candidate contests **and** zero proposals causes the controller to re-ask both setup counts instead of only one field.

- **The test plan expanded.** We now have JUnit test classes for the parser, chain, nodes, both ballot records, both contest classes, session, storage, and controller. Tests cover normal operations, invalid inputs, empty collections, absent removals, voting methods, persistence, and scripted terminal workflows. The tests were generated with GenAI assistance and must be run and reviewed by the team in Eclipse before their results can be reported as verified JUnit 4 results.

- **Two planned API features were left out.** `CandidateVote` and `ProposalVote` implement `equals(Object)` but not the specified `hashCode()`. The iterator interface planned for the generic chain was also omitted to keep implementation within the team's current Java experience. These are deliberate simplifications, but they differ from the submitted specification and must be resolved or acknowledged for the final project.

**Origin of changes:** The initial class plan, behavior rules, and division of responsibilities came from the team's submitted specification and scope. The additional features and many implementation choices were developed iteratively in GenAI-assisted coding discussions, with changes requested during that process (including the chain rename, separate node class, and simplified Java techniques). 

### 2. What changed in the generic LinkedChain?

We renamed the planned `LinkedChain<T>` to `ElectionChain<T>` and used a separate public `Node<T>` class rather than the planned private nested node. We implemented head insertion and public `add`, `remove`, `contains`, `count`, `get`, `size`, `isEmpty`, and `clear` operations. The chain accepts any non-null `T`, permits duplicate values, uses `equals()` to compare elements, and has **no election-specific rules**. Both `CandidateContest` and `ProposalContest` access their chains only through those public methods.

We did **not** implement `Iterable<T>` or `iterator()`, even though the specification required them. For readability, result calculations loop over `votes.get(i)`. Because retrieving index `i` walks the linked chain, a complete tally or defensive copy using repeated indexed access takes **O(N²)** time rather than the **O(N)** traversal estimated in the specification. Head insertion remains **O(1)**, while `contains`, `count`, and `remove` are **O(N)**. The current implementation is correct for the small MVP use case, but the asymptotic performance discrepancy is real.

To satisfy the Exceeds requirement for generic class reuse, we expanded ElectionChain<T> to store three different element types: CandidateVote, ProposalVote, and String. The third type was introduced in ElectionStorage.listSavedElections(), where saved election filenames are temporarily stored in ElectionChain<String> and retrieved through its public get() method before being sorted alphabetically. This change required no modifications to the generic chain itself and demonstrates that the same implementation can support different types of application data.

### 3. What happened to our unknowns and risks?

- **Object equality — resolved for current use.** Both vote classes compare trimmed, case-sensitive voter IDs only when the other object is the same vote class. This lets a contest reject repeated voter IDs while allowing different voters to select the same candidate or yes/no answer. Vote identity does not compare contest IDs because each contest owns its own chain. The remaining discrepancy is the missing `hashCode()` required by the original specification.

- **Linked-node removal — resolved and tested.** `ElectionChain.remove()` handles an empty chain, the first item, middle items, last item, and absent items. A successful removal deletes exactly one matching entry and reduces the size; a failed removal does not change stored data. The generated `ElectionChainTest` exercises these cases.

- **Vote tallying — resolved, with a performance trade-off.** `CandidateContest` and `ProposalContest` recalculate counts from ballots rather than maintaining separate counters. This prevents totals from drifting after corrections. Plurality ties are reported; proposal ties do not pass; empty contests report `No votes recorded`. Repeated indexed chain traversal is slower than the original Big-O estimate.

- **Stretch features — partially resolved.** Persistence works for ordinary save/load operations using a versioned binary `.rball` format. Plurality, majority-required, and instant-runoff ranked-choice voting are implemented. Ranked ballots must include every candidate exactly once. When multiple candidates tie for the fewest votes in an elimination round, the implementation reports an **unresolved elimination tie** instead of choosing a tie-breaker. The full set of configurable transfer/tie rules from the stretch idea is not implemented.

### 4. What does not work yet or is not finished?

The ordinary MVP path is implemented, but several items remain:

- The measured coverage for JUnit testing was 94.8%, not 100%. More implementation needs to be done in order to achieve 100% code coverage. 
- `ElectionChain` lacks the specified iterator, its node is not private/nested, and the vote classes lack `hashCode()`. Several tally methods do not meet their originally estimated time complexity.
- Ranked-choice contests may finish with an unresolved elimination tie; there is no automatic tie-break policy.
- The GUI and formatted results export do not exist. Those are stretch features, not MVP blockers.
- Persistence does not use atomic file replacement or a checksum; a failed overwrite can damage an earlier save, and structurally valid changes to saved bytes may not be detected. These are known prototype limitations, not failures of the normal MVP workflow.

---

## Next Steps

The following tasks are listed **in intended order of completion**. Primary owners are proposed from the division of work in the original specification: Ethan (terminal flow and parsing), Harrison (generic chain and vote logic), Ben (stretch features), and Mihn (testing).

| Order | Remaining task | Primary owner (proposed) | Tests or evidence needed |

| --- | --- | --- | --- |

| 1 | Run the entire JUnit 4 suite in Eclipse; fix any import or runtime failures and verify the MVP menu path, invalid input, and EOF behavior | **Mihn** | Run all test classes with JUnit 4; exercise terminal retries, duplicate voter IDs, corrections, and end-of-input |

| 2 | Verify that the newly implemented ElectionChain<String> functionality correctly stores and retrieves saved election filenames without changing the behavior of listSavedElections() | **Harrison** | Extend `ElectionChainTest` and `ElectionStorageTest` with multiple filenames, sorting, and empty-directory cases |

| 3 | Review specification compatibility: `LinkedChain` naming, separate `Node`, omitted iterator, missing `hashCode()`, and actual vs. promised Big-O; implement necessary fixes or document approved deviations | **Harrison** | Add equality/hash-code consistency tests if implemented; iterator/empty/end tests if implemented; rerun chain and contest tests|

| 4 | Complete remaining MVP regression fixes and documentation of what users see after bad input; clean out unrelated placeholder code | **Ethan** | Expand `RapidBallotAppTest` for menu retries, zero/zero setup, absent correction, and cancellation/EOF paths |

| 5 | Strengthen the already working save/load stretch feature, including safe overwrites and better detection of damaged files | **Ben** | `ElectionStorageTest`: overwrite failure, corrupted bytes, missing files, unsafe names, and full-session round trips |

| 6 | Finalize ranked-choice tie handling and determine whether any additional transfer rules are necessary | **Harrison** | `CandidateContestTest`: tied eliminations, transferred ballots, three-or-more rounds, and empty ballots |

| 7 | Build the planned GUI as an alternative to terminal input while reusing the existing model/session classes | **Ben** | GUI input-validation tests, workflow tests for both vote types, duplicate-ID feedback, and comparison with terminal results |

| 8 | Add and maintain tests for all newly implemented features and check coverage again after each integration step | **Mihn** | New JUnit cases for the third chain type, GUI, persistence errors, and revised ranked-choice rules |

| 9 | Update final project documentation, accurate per-file attribution, examples of terminal use, and final submission cleanup | **Ethan** | Cross-check source files, test results, README statements, and rubric requirements |

### Stretch-goal commitments

| Stretch goal from scope | Status | Work remaining |

| Save and reopen multiple elections | **Committed — implemented in prototype** | Improve failed-overwrite handling and damaged-file detection |

| Administrator-selected plurality, majority, and ranked-choice methods | **Committed — implemented in prototype** | Resolve elimination ties and decide on additional configurable transfer rules |

| Graphical user interface | **Committed for future development (per current README)** | Build GUI while keeping the existing election logic |

| Formatted results export with date, time, and contest details | **Dropped from current plan** | None unless the team later changes scope |

### New tests to add

Before the final deliverable, expand the test suite for any changed APIs and for each unfinished feature: actual Eclipse/JUnit 4 execution, iteration/hash-code behavior if added, save failures/corruption, ranked-choice tie policies, and GUI validation. Every new public method should have a normal case and an applicable bad-input or boundary case, following the test plan in the specification.

---

## Work Attribution

| `RapidBallotApp.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `InputParser.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `ElectionSession.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integrationw |

| `CandidateContest.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `ProposalContest.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `CandidateVote.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `ProposalVote.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `ElectionChain.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `Node.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integrationr |

| `ElectionStorage.java` | Ethan | Implemented class and methods, used GenAI to help with structure and code integration |

| `RapidBallotAppTest.java` | Ethan | Drafted JUnit tests; confirm Eclipse execution and team revisions |

| `InputParserTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `ElectionSessionTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `CandidateContestTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `ProposalContestTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `CandidateVoteTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `ProposalVoteTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `ElectionChainTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `NodeTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

| `ElectionStorageTest.java` | Ethan | Drafted JUnit tests; confirm review/execution |

**How GenAI was used:** ChatGPT helped draft and revise Java class implementations, suggest validation and voting logic, generate JUnit test sources, review code behavior, and organize this README. Team members are responsible for reviewing, understanding, testing, correcting, and accurately attributing the submitted work.
