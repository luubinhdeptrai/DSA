# Backtracking — Exercises (Java 21)

Derive a search from its contract, test what happens between sibling branches, and defend every omitted choice. This workbook complements [Backtracking Essential Concepts](./Backtracking_Essential_Concepts.md). Keep that reference closed during an independent attempt.

## Contents

- [1. Purpose and working conventions](#1-purpose-and-working-conventions)
- [2. Derivation and validation workflow](#2-derivation-and-validation-workflow)
- [3. Level 1 — Fundamentals](#3-level-1--fundamentals)
- [4. Level 2 — Core enumeration](#4-level-2--core-enumeration)
- [5. Level 3 — Duplicates and safe pruning](#5-level-3--duplicates-and-safe-pruning)
- [6. Level 3 — Board search](#6-level-3--board-search)
- [7. Level 4 — Curated practice](#7-level-4--curated-practice)
- [8. Level 5 — Mixed interview challenges](#8-level-5--mixed-interview-challenges)
- [9. Complexity laboratory](#9-complexity-laboratory)
- [10. Debugging laboratory](#10-debugging-laboratory)
- [11. Optional N-Queens workshop](#11-optional-n-queens-workshop)
- [12. Mock interviews](#12-mock-interviews)
- [13. Revision and spaced repetition](#13-revision-and-spaced-repetition)
- [14. Completion and readiness](#14-completion-and-readiness)
- [15. References and author validation](#15-references-and-author-validation)

## 1. Purpose and working conventions

The [Phase 3 roadmap](./DSA_Interview_Roadmap.md#10-backtracking) is authoritative for scope, priorities, and readiness. Follow its [learning loop](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy): Understand → Implement → Practice → Explain → Review → Validate. Prepared documents demonstrate neither implementation skill nor independent problem selection.

This workbook has **five progressive levels**, **52 original exercises**, and **seven curated problems**. The original exercises comprise **20 coding exercises** (19 ordinary exercises and one optional queen-validity helper), **seven tracing/Java-state drills**, **three pruning investigations**, **seven complexity investigations**, **12 debugging challenges**, **one optional queen-state workshop**, and **two mock interviews**. Reusing an exercise in a mock does not create another coding problem. N-Queens is optional.

### Prerequisites and diagnostic

| Existing material | Demonstration before progressing |
| --- | --- |
| [Recursion contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) | State a smaller subproblem and a decreasing progress measure |
| [Recursion: time, depth, space](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) | Count total calls separately from maximum live frames |
| [Trees: recursive aggregation](./Trees/Trees_Essential_Concepts.md#9-recursive-contracts-and-subtree-aggregation) | Explain why visiting an existing tree need not enumerate an implicit exponential tree |
| [Tries: wildcard DFS](./Tries/Tries_Essential_Concepts.md#9-wildcard-search-using-dfs) | Explain first-success versus first-failure behavior |
| [Lists and ArrayList](./Java_List_ArrayList_LinkedList.md), [Java Strings](./Java_String.md) | Trace shared references, list snapshots, removal overloads, and builder length |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) | Include candidate scans, sorting, copied outputs, and live working objects |

Before Level 1, explain the difference between a primitive parameter and a shared object reference. Before Level 2, finish T1/T3/F1 without notes. Before Level 3, implement an enumerator that passes a snapshot-independence test. Before Level 5, hide the topic labels and hints and explain a baseline before selecting an algorithm. Return to a prerequisite when the diagnostic exposes a gap.

### Shared coding contracts

These rules are part of **every original coding exercise**; local requirements override them explicitly.

- Use Java 21 and its standard library, single-threaded ordinary completed calls, and feasible memory. Methods are independent across calls; no static accumulated results. Integer values may include negative numbers, zero, duplicates, and full int endpoints unless an exercise restricts them.
- Reject required null inputs, nested arrays/rows, strings, or required elements with **NullPointerException (NPE)**. Validate stated numeric ranges and alphabet/domain restrictions with **IllegalArgumentException (IAE)**. Complete required validation before returning an empty/trivial result or mutating caller-owned state. If multiple independent violations coexist, either applicable exception is acceptable.
- Bounds explicitly called **size preconditions**, and properties explicitly called **trusted preconditions**, need not be rescanned. They delimit feasible exercises. Other stated value-domain requirements are checked. This distinction prevents an unnecessary linear scan from invalidating a promised logarithmic or constant helper.
- Inputs are read-only unless temporary mutation is explicitly permitted. Sorting a private copy is allowed when stated; sorting caller input is not. Every output list container is fresh and mutable; nested list outputs have independent mutable inner containers. Immutable Integer/String/record elements may be shared.
- In all enumeration exercises, save completed list paths as independent snapshots. Output accumulated during one call is shared intentionally; working paths are restored. Ordinary return restores all caller-visible temporary state on success and failure.
- Outer result order is irrelevant unless a trace explicitly fixes DFS order. Compare canonicalized logical results **and** check original output count equals distinct count when duplicates are forbidden. Normalizing into a set alone can conceal duplicate outputs. Inner order follows each exercise's representation rule.
- Use `ArrayList<Integer>` for integer paths and `boolean[]` for occurrence usage where requested. An input position and its numeric value are different identities. Repeated values do not automatically permit unlimited reuse.
- Auxiliary space excludes input and required output; include copies, sorting workspace, buffers, visited arrays, and call frames. Let `B` be number of output objects and `S` their total elements/characters; even an empty inner list costs a container.
- Small recursion caps are teaching limits, not a guarantee about every JVM's stack. No numeric overflow is permitted. Use long when the sum of valid ints can exceed int.

Board contracts are stated in Section 6. Graph algorithms and DP are not prerequisites for mandatory work.

### Starter convention

Make your own scratch file outside this guide with `import java.util.*;` and class `BacktrackingWorkbook`. **Every Java fence in this document is a member of that class**, in document order. Fully qualified functional-interface names avoid additional imports. Add helpers when needed. TODO methods throw UnsupportedOperationException solely as placeholders; that exception is not the required invalid-input behavior.

Do not read the concepts implementations during a cold solve. Tracing and intentionally flawed specimens are executable teaching examples. Complexity wrappers that call a TODO method require a learner implementation or a clearly identified test fixture; compiling a starter is not evidence that it solves the exercise.

### One attempt record, one source of history

Each item starts unchecked. Record last date, I/H/S/—, elapsed minutes including explanation, next review, and one mistake/lesson. **I** = independently solved and tested; **H** = conceptual hint; **S** = substantial solution review or unfinished; **—** = no attempt. Checking an attempted H/S item does not establish independence.

The [roadmap](./DSA_Interview_Roadmap.md#four-independent-progress-gates) owns T/I/P/R. Curated records below are pointers to its canonical per-problem Logs: fill the dates/result there and use a pointer here, rather than maintaining duplicate histories. Original exercise histories stay beside their exercise. The existing record also serves as the review queue.

## 2. Derivation and validation workflow

For every coding exercise, write a derivation card **before coding**:

1. Define answer identity: ordered sequence, unordered membership, value multiset, coordinate route, boolean, or scalar. Specify whether one witness or all answers are required.
2. Give the recursive contract, including entry invariant and return-state promise. State which positions/values remain eligible.
3. Describe available choices, validity tests, completion/base cases, and what is saved or returned.
4. Pair each owned mutation with its inverse. Describe the exact path/used/builder/board state after a child and after the caller's undo.
5. Prove soundness. Take an arbitrary valid answer and follow its choices to prove completeness. When duplicates are forbidden, prove one canonical route per logical answer.
6. For every prune, name its assumptions and prove an impossible completion or a retained equivalent representative. Give a counterexample under a changed assumption.
7. Prove termination from a finite progress measure. Separate maximum depth from all explored states.
8. Derive validation/preparation + candidate checks + updates + output-copying time. Report peak auxiliary and required output separately.
9. Explain why a previously studied alternative is less suitable, or choose that alternative when enumeration is unnecessary.

**Completion standard for all coding items:** implement from the stated signature, compile, run every listed example, add at least two risk-focused tests, compare small inputs against an independent simple oracle, check input preservation and repeated calls, and explain correctness/costs aloud. For nested lists, mutate one returned list and confirm no other result or prior call changes. For boards, compare all cells before/after both success and failure. For internal state, a test-only entry/exit assertion may capture path/marks and verify equality on return; do not retain such snapshots in the claimed optimized algorithm.

| Roadmap objective | Practical evidence |
| --- | --- |
| Decision state, choices, completion, progress | T1–T3, F1, E1–E3 |
| Choose → explore → undo; snapshots | F1–F2, T3/T7, D1–D3/D8/D11/D12 |
| Subsets and permutations of three values | E1–E2, T1–T2 |
| Groups, reusable versus single-use choices | E3–E4, T5, D4 |
| Duplicate-aware combination drill | U1–U3, T4, P2, D5 |
| Safe pruning and sign assumptions | S1, P1–P3, D6 |
| Board matcher and path-local visitation | B1–B3, T6, D7/D9/D10 |
| Search tree, stack, output-copying costs | C1–C7 |
| Essential independent practice | L1–L7, exact E/E/E/A/E/A/C order |
| Unfamiliar selection and communication | X1–X6, M1–M2 |
| Optional constraint satisfaction | O1–O2 and L7; does not block ordinary readiness |

For correctness language, reuse [the proof framework](./Backtracking_Essential_Concepts.md#14-soundness-completeness-and-uniqueness). “Recursion checks everything” is not a proof: specify which choices remain, why none are lost, and why stale state cannot suppress a sibling.


## 3. Level 1 — Fundamentals

Begin with traces before implementing a whole enumerator. Reuse [implicit decision trees](./Backtracking_Essential_Concepts.md#3-implicit-decision-trees) and [choose/explore/undo](./Backtracking_Essential_Concepts.md#5-choose-explore-and-undo) only after a concrete attempt.

### T1. Complete a binary decision tree

**Difficulty:** Easy. **Objective:** connect a call's index with the decided prefix. **Prerequisites:** reachable base cases and sequential calls.

For [1,2], use include first, exclude second, increasing i on both edges. Save only when i equals array length. Complete the diagram without running code:

```text
                         (i=0, path=[])
                         /             \
                  include 1            exclude 1
                (i=1, path=[1])       (i=1, path=____)
                   /       \              /       \
             include 2   exclude 2      ____      ____
              (2,____)    (2,____)     (2,____)  (2,____)
```

Fill a table with helper entry number, i, path at entry, decision, saved output if any, and path on return. Label every base call. Count helper entries including root/leaves, outputs, total copied elements, maximum decisions on one branch, and maximum simultaneously live helper frames. Then draw [1,2,3] yourself. Explain why each position doubles the membership choices, while the live stack follows only one route.

**Completion:** all leaves and undo events accounted for; no sibling inherits another's choice. Explain whether swapping branch order changes the logical result set.

<details>
<summary>Hint 1</summary>

At entry, only positions strictly before i have been decided.

</details>

<details>
<summary>Hint 2</summary>

Both children advance the same index; their paths differ only in whether the current value is included.

</details>

<details>
<summary>Hint 3</summary>

Count calls by levels, but count frames along a longest root-to-leaf route. Do not add the stack sizes of sequential siblings.

</details>

- [ ] **T1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### T3. Trace who owns the undo

**Difficulty:** Easy. **Objective:** distinguish child-owned and caller-owned changes. **Prerequisites:** T1 and mutable lists.

The inner pair of changes below simulates a child that restores its own deeper edit. Predict both runs, with callerUndoes true and false. Do not edit the code before recording your prediction.

```java
static List<List<Integer>> undoTrace(boolean callerUndoes) {
    List<Integer> path = new ArrayList<>(List.of(4));
    List<List<Integer>> seen = new ArrayList<>();
    path.add(6);                    // caller chooses
    path.add(5);                    // simulated child's deeper choice
    seen.add(new ArrayList<>(path));
    path.remove(path.size() - 1);    // child restores its own edit
    seen.add(new ArrayList<>(path));
    if (callerUndoes) {
        path.remove(path.size() - 1);
    }
    path.add(9);                    // next sibling's candidate
    seen.add(new ArrayList<>(path));
    path.remove(path.size() - 1);
    seen.add(new ArrayList<>(path));
    return seen;
}
```

| Boundary | Path with caller undo | Path without caller undo | Who owns the pending edit? |
| --- | --- | --- | --- |
| Entry | [4] | [4] | None |
| Child observation | ____ | ____ | ____ |
| Child returned | ____ | ____ | ____ |
| Before trying 9 | ____ | ____ | ____ |
| Final return | ____ | ____ | ____ |

Explain which returned observation first reveals the faulty sibling prefix. Why would asking the child to remove both additions violate the child's entry/return contract? Why do snapshots alone not fix missing undo?

**Completion:** predict all four observations for each run and identify the exact entry state each simulated frame must restore.

- [ ] **T3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### F1. Borrow a mutable prefix

**Difficulty:** Easy. **Learning objective:** Separate saved observations from a shared working path and restore a nonempty entry state.

**Prerequisites:** T1/T3; ArrayList references and indexed removal.

**Problem and output:** Given a caller-owned prefix and an array of alternatives, temporarily append each alternative separately, save that extended path, then restore the prefix before trying the next. After all alternatives, save one final snapshot of the restored prefix. This is a lifecycle drill, not a recursive enumerator. Return observations in candidate-array order followed by the final prefix.

**Input, constraints, and ownership:** prefix is an ArrayList<Integer> with no null elements. Its length p and candidates length m are each at most 20 (size preconditions). All int values and repeated candidates/prefix values are legal. One alternative is appended per observation; equal alternatives intentionally produce equal observations. No sorting. Temporary prefix mutation is permitted, but its contents and object identity must be preserved on return. Candidates remain unchanged. Required nulls are checked before mutation.

**Java 21 starter**

```java
public static List<List<Integer>> observeChoices(ArrayList<Integer> prefix, int[] candidates) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("F1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| prefix=[6], candidates=[2,9] | [[6,2],[6,9],[6]]; prefix remains [6] | Sibling restoration |
| prefix=[9,4], candidates=[9] | [[9,4,9],[9,4]]; prefix remains [9,4] | Remove position, not first equal value |
| prefix=[], candidates=[] | [[]] | Final empty observation |
| prefix=[0,-2], candidates=[] | [[0,-2]] | Nonempty entry state |
| prefix=[], candidates=[0,0] | [[0],[0],[]] with distinct inner objects | Multiplicity intentionally retained |
| prefix=[null], candidates=[] | NPE; prefix unchanged | Full validation before trivial result |
| prefix=[], candidates=null | NPE; prefix unchanged | Required array |

**Target time:** O((m+1)*(p+1)) including every snapshot.

**Target auxiliary space:** O(p+1) transient prefix capacity is a safe bound; no second growing working path is needed. Returned observations take O((m+1)*(p+1)).

**Completion evidence:** Trace entry, append, simulated child observation, undo, and next sibling. Mutate one saved observation and show that neither the prefix nor another observation changes. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

An observation describes one alternative, not all earlier alternatives together.

</details>

<details>
<summary>Hint 2</summary>

The prefix may already contain the value being appended.

</details>

<details>
<summary>Hint 3</summary>

Record the entry size; undo the appended position. Saved observations need their own containers.

</details>

- [ ] **F1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### T7. References, removal overloads, and immutable snapshots

**Difficulty:** Medium. **Objective:** reason from Java references rather than printed syntax. **Prerequisites:** F1 and the existing List/String references.

```java
static List<String> referenceTrace() {
    List<Integer> path = new ArrayList<>(List.of(5));
    List<List<Integer>> out = new ArrayList<>();
    out.add(path);
    path.add(2);
    out.add(new ArrayList<>(path));
    path.remove(0);

    List<Integer> byIndex = new ArrayList<>(List.of(1, 7, 1));
    List<Integer> byValue = new ArrayList<>(byIndex);
    byIndex.remove(1);
    byValue.remove(Integer.valueOf(1));

    StringBuilder text = new StringBuilder("r");
    text.append("ab");
    String saved = text.toString();
    text.setLength(1);
    return List.of(out.toString(), path.toString(),
            Boolean.toString(out.get(0) == path),
            Boolean.toString(out.get(1) == path),
            byIndex.toString(), byValue.toString(), saved, text.toString());
}
```

Predict all eight returned strings. Draw object arrows for path and both out entries before and after the removal. Identify which operation copies a container, which copies immutable text, and which stores a reference. Then change path after the final observation in your scratch copy and predict which earlier entries would change.

The Java API contracts distinguish indexed removal, first-equal-object removal, and a new independent list container. [ArrayList API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html) Builder truncation changes the working text, while an earlier toString result remains independent. [StringBuilder API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html)

**Completion:** correct values and identity explanations; distinguish restoration from snapshotting. Explain why Integer elements need no recursive deep copy here.

<details>
<summary>Hint 1</summary>

A list holds references; two references can identify one mutable object.

</details>

<details>
<summary>Hint 2</summary>

Inspect the argument type passed to each remove call before reasoning about its numeric value.

</details>

<details>
<summary>Hint 3</summary>

Track the contents of each allocated object separately. A copied container and an immutable String snapshot have different ownership from a stored path reference.

</details>

- [ ] **T7 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### F2. Build option labels

**Difficulty:** Medium. **Learning objective:** Use an append-only StringBuilder with exact saved-length restoration.

**Prerequisites:** F1; recursion contracts; Java UTF-16 strings.

**Problem and output:** Choose one string from every row of options and concatenate the choices. Return one String per choice-index sequence. Equal strings from different choice sequences remain separate outputs. Enumerate all sequences, including the one empty sequence when there are no rows. Outer order is irrelevant; compare a multiset of strings.

**Input, constraints, and ownership:** options and every row/token are nonnull. At most 6 rows, 3 choices per row, 6 UTF-16 code units per token are size preconditions. Empty tokens, empty rows, duplicate tokens, and arbitrary UTF-16 contents are allowed. Each row contributes exactly one choice; a token from another row can have equal text. No sorting or input mutation. Validate every row/token before searching, even when an earlier row is empty. Use one working StringBuilder; saved outputs are immutable Strings.

**Java 21 starter**

```java
public static List<String> buildLabels(String[][] options) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("F2");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [["ab","x"],["Q",""]] | ["abQ","ab","xQ","x"] | Multi-character and empty append |
| [] | [""] | Empty product |
| [[]] | [] | No complete choice |
| [["a","a"],[""]] | ["a","a"] | Duplicate choice paths retained |
| [[""],[""]] | [""] | Index advances on empty choices |
| [["hi"]] | ["hi"] | Singleton |
| [[],[null]] | NPE | Validate later row before early no-output result |
| [null] | NPE | Null row |

**Target time:** O(P+V+A+S+1), where P counts row headers and token references validated, V helper entries, A characters appended across edges, and S output characters. Explain amortized builder growth.

**Target auxiliary space:** O(M+d+1), where M is the longest live concatenation and d the number of rows; output O(B+S).

**Completion evidence:** Give the helper promise for row and builder contents. Check the builder returns to an arbitrary nonempty test prefix after a helper call. Explain why subtracting one character is unsafe. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

An empty row and an empty token have different meanings.

</details>

<details>
<summary>Hint 2</summary>

The row index advances independently of the number of appended characters.

</details>

<details>
<summary>Hint 3</summary>

Save the earlier builder length at each choice and restore it after exploring; capture immutable text at completion.

</details>

- [ ] **F2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.




## 4. Level 2 — Core enumeration

Use the output identity rule to choose the state. Subsets, groups, and arrangements may share a path list, but they do not share eligibility rules. Explain the distinctions before coding.

### E1. Enumerate distinct-value subsets

**Difficulty:** Medium. **Learning objective:** Derive include/exclude state, base cases, and output copying.

**Prerequisites:** T1, F1; [subset theory](./Backtracking_Essential_Concepts.md#7-subsets-through-include-and-exclude).

**Problem and output:** Return every subset of values once. Members of each result follow their original input order; outer order is irrelevant. Implement include/exclude recursion with an index and a shared ArrayList<Integer> path. Return all subsets, including the empty subset.

**Input, constraints, and ownership:** Nonnull int[]; n<=12 and pairwise distinct values are trusted preconditions. Any int, including zero/negative/extreme values, is legal. Each input position is single-use. Sorting is not allowed because the original-order representation is required. Input is read-only. Distinctness need not be validated; duplicates belong to U1.

**Java 21 starter**

```java
public static List<List<Integer>> subsets(int[] values) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("E1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [] | [[]] | One empty subset |
| [5] | [[],[5]] | One decision |
| [1,2] | [[],[1],[2],[1,2]] | Two-position tree |
| [1,2,3] | [[],[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]] | Roadmap three-value drill |
| [0,-2] | [[],[0],[-2],[0,-2]] | Original order and signed values |
| [Integer.MIN_VALUE,Integer.MAX_VALUE] | [[],[Integer.MIN_VALUE],[Integer.MAX_VALUE],[Integer.MIN_VALUE,Integer.MAX_VALUE]] | No arithmetic on elements |
| null | NPE | Required reference |

**Target time:** O((n+1)*2^n) including copied outputs; give a tighter expression for total copied elements when n>=1.

**Target auxiliary space:** O(n+1) path/call frames, excluding O(B+S) output.

**Completion evidence:** Write the full recursive contract, choices, leaf meaning, ownership, soundness/completeness/uniqueness proof, and a termination measure. Explain the 2^n position decisions. Verify 8 results for three values and independently owned empty/nonempty lists. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Each position needs exactly one yes/no decision.

</details>

<details>
<summary>Hint 2</summary>

The index separates the decided prefix from the undecided suffix.

</details>

<details>
<summary>Hint 3</summary>

Save at the point where every position has been decided; the include branch must release its addition before exclusion.

</details>

- [ ] **E1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### T2. Fill an arrangement tree

**Difficulty:** Medium. **Objective:** relate chosen order to position marks. **Prerequisites:** T1; used[] tracking.

Use [1,2,3], scan candidate indexes ascending at each depth, and save at path length three. Complete all omitted branches:

```text
                             []
                  /           |           \
                [1]          [2]         [3]
              /     \       /   \        /   \
           [1,2]   [1,3]   ____ ____    ____ ____
              |       |      |    |       |    |
             ____    ____   ____ ____    ____ ____
```

Trace the entire first root branch, including return events. For each call record path, used[0..2], next candidate considered, output, and state after undo. At prefix [1,3], explain why input index 1 remains available even though it is less than the last chosen index. Count states per depth, full outputs, copied elements, maximum frames, and candidate-loop checks if every non-base call scans all three indexes.

**Completion:** prove path contains exactly the marked input positions; show root marks are all false before choosing 2. Derive the factorial leaf count without confusing it with total calls.

<details>
<summary>Hint 1</summary>

Input index is an occurrence identity, not the current output slot.

</details>

<details>
<summary>Hint 2</summary>

At chosen length j, there are n-j unused choices, even though the implementation may scan all n indexes.

</details>

<details>
<summary>Hint 3</summary>

Record an undo for both path and used. Count loop checks only at non-base states, including checks that skip used positions.

</details>

- [ ] **T2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### E2. Arrange every distinct occurrence

**Difficulty:** Medium. **Learning objective:** Derive an order-sensitive search using path-local used[] state.

**Prerequisites:** T2, E1; [permutation theory](./Backtracking_Essential_Concepts.md#8-permutations-through-unused-positions).

**Problem and output:** Return all full-length ordered arrangements. Each input position appears exactly once per result. Outer order is irrelevant; order inside a result defines its identity. Use a boolean[] indexed by input position and one mutable path. Empty input has one empty arrangement.

**Input, constraints, and ownership:** n<=8 and pairwise distinct int values are trusted preconditions; negative/zero/extreme ints are legal. No reuse in one result. Input remains unchanged; no sorting is needed. Any input index may fill the next slot if unused. Null array throws NPE.

**Java 21 starter**

```java
public static List<List<Integer>> permutations(int[] values) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("E2");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [] | [[]] | Empty arrangement |
| [-3] | [[-3]] | Single occurrence |
| [0,2] | [[0,2],[2,0]] | Earlier index remains eligible |
| [1,2,3] | [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]] | Six distinct outputs |
| [Integer.MAX_VALUE,Integer.MIN_VALUE] | [[Integer.MAX_VALUE,Integer.MIN_VALUE],[Integer.MIN_VALUE,Integer.MAX_VALUE]] | No subtraction comparator |
| null | NPE | Required reference |

**Target time:** O(n*n!+1), counting candidate scans and n-element output snapshots.

**Target auxiliary space:** O(n+1) for used/path/stack; output O((n+1)*n!).

**Completion evidence:** Relate every true used entry to the current path. Explain why increasing start alone loses answers, why there are n! outputs, and why n! output objects alone undercounts copying. Verify both path and used are restored on return. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

An output position can take any remaining input occurrence.

</details>

<details>
<summary>Hint 2</summary>

Depth is chosen count; eligibility is a separate set of occurrence marks.

</details>

<details>
<summary>Hint 3</summary>

Every chosen occurrence owns two changes: its mark and its appended path element.

</details>

- [ ] **E2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### E3. Select exact-size groups

**Difficulty:** Medium. **Learning objective:** Use increasing eligible indexes and a proved capacity prune.

**Prerequisites:** E1/E2; combinations versus arrangements.

**Problem and output:** Return every k-position group exactly once. Group members follow input-index order; outer order is irrelevant. Values are distinct, so this is also unique value membership. Use start-index recursion; do not generate all permutations and deduplicate afterward.

**Input, constraints, and ownership:** n<=12 and distinct int values are trusted preconditions. Check 0<=k<=n, otherwise IAE. All int values allowed. Each position at most once; no reuse. Input read-only; no sorting. For k=0 return one empty group.

**Java 21 starter**

```java
public static List<List<Integer>> groups(int[] values, int k) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("E3");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [], k=0 | [[]] | Empty group |
| [4], k=1 | [[4]] | One full group |
| [9,2,7], k=2 | [[9,2],[9,7],[2,7]] | Index order, not numeric order |
| [1,2,3], k=3 | [[1,2,3]] | Capacity-pruned single chain |
| [0,-1], k=0 | [[]] | No selection |
| [0,-1], k=1 | [[0],[-1]] | Signed candidates |
| [1,2], k=3 | IAE | Insufficient requested supply |
| [1], k=-1 | IAE | Negative k |
| null, k=0 | NPE | Null checked even at k=0 |

**Target time:** O(V+k*B+1), where B=binomial(n,k) and V explored prefixes. With capacity pruning, justify V<=1+k*B.

**Target auxiliary space:** O(k+1), excluding O((k+1)*B) output.

**Completion evidence:** Prove canonical increasing indexes give completeness and uniqueness. Derive a loop/call cutoff from remaining available positions and needed slots; test k=0 and k=n. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

One membership set has exactly one increasing index sequence.

</details>

<details>
<summary>Hint 2</summary>

Count how many slots remain after selecting the next position.

</details>

<details>
<summary>Hint 3</summary>

A candidate must leave enough later occurrences for the remaining group; derive the inequality before coding.

</details>

- [ ] **E3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### T5. Compare two supply policies

**Difficulty:** Medium. **Objective:** trace start eligibility and safe pruning. **Prerequisites:** E3; positive targets.

Use sorted candidates [2,3,7], target 7, cap 3. Scan increasing candidate indexes; check remaining==0 before the cap, and stop scanning when the current candidate exceeds remaining. Do the complete trace once with reuse and once with single-use supply.

```text
([], remaining=7, start=0)
   / choose 2               | choose 3          \ choose 7
([2], remaining=5, start=?)  (____,____,____)     (____,____,____)
   /         |      \
  ____      ____     X or child? justify
```

Use columns entry number, start, path, remaining, eligible indexes, reason for pruning/output, and restored entry path. Identify which branches differ only because of supply. Count entries and output copies separately for both policies; do not count a loop break as a recursive entry. Repeat a smaller comparison for [2], target 4, cap 2.

**Completion:** give the next start as a function of the actually chosen index j, and prove it matches the policy. Explain why restarting at zero changes duplicate behavior.

<details>
<summary>Hint 1</summary>

Supply concerns whether the chosen occurrence/value remains available after this choice.

</details>

<details>
<summary>Hint 2</summary>

The chosen index j need not equal the helper's entry start.

</details>

<details>
<summary>Hint 3</summary>

A cap counts chosen elements. Strict positivity and sorted order provide a separate reason to stop scanning larger candidates.

</details>

- [ ] **T5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### E4. Plan positive bundles under two supply rules

**Difficulty:** Medium. **Learning objective:** Separate target progress, candidate reuse, and pick limits.

**Prerequisites:** E3; [target choices](./Backtracking_Essential_Concepts.md#10-reusable-and-single-use-target-choices).

**Problem and output:** Return all unordered bundles summing to target using at most cap elements. If reusable is true, a candidate value may be chosen repeatedly; otherwise each input position may be used once. Each result is nondecreasing. Duplicate value bundles are forbidden; outer order is irrelevant. Return every valid bundle, not just the first.

**Input, constraints, and ownership:** n<=8 is a size precondition. Check candidates are distinct and strictly positive, target in 0..24, cap in 0..8; invalid values throw IAE. A private sorted copy is allowed; caller array unchanged. Zero target has exactly the empty bundle, even cap=0, but candidate validation still precedes that answer. Zero/negative candidates are rejected. Candidates may exceed target.

**Java 21 starter**

```java
public static List<List<Integer>> bundles(int[] candidates, int target, int cap, boolean reusable) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("E4");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [7,2,3], target=7, cap=3, reusable=true | [[2,2,3],[7]] | Sorted representation, reuse |
| [7,2,3], target=7, cap=3, reusable=false | [[7]] | Single-use supply |
| [2], target=4, cap=2, reusable=true | [[2,2]] | Same candidate again |
| [2], target=4, cap=2, reusable=false | [] | Cannot reuse one position |
| [2,3], target=6, cap=2, reusable=true | [[3,3]] | Cap excludes three 2s |
| [], target=0, cap=0, reusable=false | [[]] | Empty solution |
| [], target=1, cap=2, reusable=true | [] | No supply |
| [5], target=3, cap=3, reusable=true | [] | Candidate too large |
| [2], target=2, cap=0, reusable=true | [] | Positive target with no picks |
| [0,2], target=0, cap=2, reusable=true | IAE | Validation before zero target |
| [-1,3], target=2, cap=2, reusable=true | IAE | No signed-domain pruning assumptions |
| [2,2], target=4, cap=2, reusable=false | IAE | Explicit distinctness policy |
| [2], target=25, cap=2, reusable=true | IAE | Target range |
| [2], target=2, cap=-1, reusable=true | IAE | Cap range |
| null, target=0, cap=0, reusable=true | NPE | Required reference |

**Target time:** Aim for O(n log(n+1)+V+S+1) with sorted positive candidates and at most one too-large break per call. Define d<=min(cap,floor(target/q)) for minimum positive q, also d<=n when single-use; handle no candidates separately.

**Target auxiliary space:** O(n+d+1), including sorted copy/path/stack and sorting workspace allowance; output O(B+S).

**Completion evidence:** Explain eligibility after choosing index j in both modes without conflating it with start+1. Prove overshoot pruning and zero-target completion under positivity. Compare small cases against multiplicity-vector enumeration. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Order-equivalent bundles should follow one canonical value order.

</details>

<details>
<summary>Hint 2</summary>

Supply policy changes which candidate remains eligible after a choice.

</details>

<details>
<summary>Hint 3</summary>

Keep remaining target and chosen count separate. A sorted break requires a proof about every later eligible value.

</details>

- [ ] **E4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.




## 5. Level 3 — Duplicates and safe pruning

The [duplicate-handling](./Backtracking_Essential_Concepts.md#11-duplicate-aware-enumeration) and [pruning](./Backtracking_Essential_Concepts.md#12-safe-pruning) references explain the proof obligations. Here you must supply those proofs for changed contracts.

### T4. Distinguish equal siblings from needed multiplicity

**Difficulty:** Medium. **Objective:** reason about occurrence identity and logical output identity. **Prerequisites:** E1/E3.

For sorted [1,2,2], label occurrences 1a,2b,2c. In a start-index subset search, output each current prefix before exploring children. Complete the diagram, marking retained versus suppressed edges:

```text
                         []
             /1a          |2b          \2c
           [1]            [2]          ______
         /2b  \2c          |2c
       [1,2]   ____        ____
          |2c
         ____
```

List which position subsets produce equal value outputs without suppression. Supply a same-depth rule and explain why [2,2] survives. Trace the state with prefix [1,2] and start=2: should the remaining 2 be skipped? Change the rule to “skip whenever i>0 and a[i]==a[i-1]” and construct the missing outputs yourself. Finally repeat the choice analysis at the root and prefix [2b] of a full-arrangement search; explain why start alone cannot express that used[] state.

**Completion:** separate soundness, coverage, and uniqueness. Sorting alone does not prove any duplicate route was removed.

<details>
<summary>Hint 1</summary>

Equal values can represent interchangeable siblings and also separate usable supply.

</details>

<details>
<summary>Hint 2</summary>

A child's eligibility boundary differs from its parent's boundary.

</details>

<details>
<summary>Hint 3</summary>

For arrangements, ask whether an equal predecessor is still available for this same output slot or was already consumed by the prefix.

</details>

- [ ] **T4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### U1. Enumerate unique value subsets

**Difficulty:** Medium. **Learning objective:** Keep multiplicity while suppressing equivalent sibling choices.

**Prerequisites:** E1/E3, T4; [duplicate theory](./Backtracking_Essential_Concepts.md#11-duplicate-aware-enumeration).

**Problem and output:** Return every distinct value-multiset subset once. Each result is nondecreasing; outer order is irrelevant. Equal input occurrences supply multiple copies, so [2,2] must remain possible when two 2s exist. Generate canonical choices during search rather than using a result set to repair duplicated enumeration.

**Input, constraints, and ownership:** Nonnull int[]; n<=12 size precondition. Duplicates, all-equal arrays, signed values, zero and extreme ints are legal. Each position single-use; no unlimited reuse. Sort a private copy if useful; input unchanged. Empty input yields [[]].

**Java 21 starter**

```java
public static List<List<Integer>> uniqueSubsets(int[] values) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("U1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [1,2,2] | [[],[1],[2],[1,2],[2,2],[1,2,2]] | Same-depth suppression |
| [2,2] | [[],[2],[2,2]] | Preserve multiplicity |
| [] | [[]] | Empty |
| [0,0,0] | [[],[0],[0,0],[0,0,0]] | All equal |
| [3,-1,3] | [[],[-1],[3],[-1,3],[3,3],[-1,3,3]] | Sort copy |
| [5] | [[],[5]] | Singleton |
| null | NPE | Required reference |

**Target time:** O(n log(n+1)+n*V+S+1) is a safe loop-based target; if every prefix is emitted then V=B=product(frequency+1). Do not omit checks of skipped siblings.

**Target auxiliary space:** O(n+1) copy/path/stack, output O(B+S).

**Completion evidence:** Write a same-depth skip condition and prove why its retained representative preserves all future multiplicities. Show why globally removing duplicate values fails. Verify uniqueness before canonical-set comparison. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Equal values create equivalent siblings but may be needed at successive depths.

</details>

<details>
<summary>Hint 2</summary>

Sorting makes equal candidate occurrences adjacent while preserving their counts.

</details>

<details>
<summary>Hint 3</summary>

Compare a repeated candidate with this call's eligibility boundary, not with a global rule that bans all later duplicates.

</details>

- [ ] **U1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### U2. Choose unique groups with repeated values

**Difficulty:** Medium. **Learning objective:** Combine a completion-size constraint with duplicate-aware supply.

**Prerequisites:** E3 and U1.

**Problem and output:** Return all distinct size-k value-multiset groups, members nondecreasing, each once. Each original occurrence supplies at most one element. Do not deduplicate the input itself or emit every subset and filter afterward.

**Input, constraints, and ownership:** n<=12 size precondition; all int values and duplicates legal. Check 0<=k<=n, otherwise IAE. Private sorting allowed; caller input unchanged. k=0 returns [[]]. Equal values are not reusable beyond their occurrence counts.

**Java 21 starter**

```java
public static List<List<Integer>> uniqueGroups(int[] values, int k) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("U2");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [1,2,2], k=2 | [[1,2],[2,2]] | Canonical groups |
| [2,2], k=1 | [[2]] | Sibling equivalence |
| [2,2], k=2 | [[2,2]] | Supply retained |
| [3,-1,3], k=2 | [[-1,3],[3,3]] | Signed sorted copy |
| [], k=0 | [[]] | Empty |
| [0,0,0], k=0 | [[]] | Zero chosen |
| [0,0,0], k=3 | [[0,0,0]] | All occurrences |
| [1], k=2 | IAE | Invalid k |
| [1], k=-1 | IAE | Negative k |
| null, k=0 | NPE | Required reference |

**Target time:** O(n log(n+1)+n*V+k*B+1) is a safe bound for a loop with sibling suppression and capacity pruning.

**Target auxiliary space:** O(n+k+1), including private copy/path/frames; output O((k+1)*B).

**Completion evidence:** This is the roadmap's duplicate-aware combination implementation drill. Give separate proofs for capacity pruning and sibling suppression. Use an index-mask oracle followed by value normalization only in tests. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The capacity question counts remaining occurrences, including repeated ones.

</details>

<details>
<summary>Hint 2</summary>

The completion test differs from U1, but answer identity remains a value multiset.

</details>

<details>
<summary>Hint 3</summary>

At a fixed depth retain one representative per equal next value without blocking the next occurrence in its child.

</details>

- [ ] **U2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### U3. Arrange repeated values without repeated answers

**Difficulty:** Medium. **Learning objective:** Distinguish canonical occurrence ordering from start-index eligibility.

**Prerequisites:** E2, U1; used[] invariants.

**Problem and output:** Return every distinct full-length value arrangement exactly once. All input occurrences must be consumed in each result. Outer order is irrelevant; inner order matters. Use position marks and prevent equivalent choices during generation.

**Input, constraints, and ownership:** n<=8 size precondition. Any int values, including duplicates, zero and negatives. Each occurrence single-use; repeated equal values still appear with their full input multiplicity. Private sorting allowed; caller input read-only. Empty input has one empty arrangement.

**Java 21 starter**

```java
public static List<List<Integer>> uniquePermutations(int[] values) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("U3");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [1,2,2] | [[1,2,2],[2,1,2],[2,2,1]] | Three unique sequences |
| [2,2] | [[2,2]] | Both occurrences needed |
| [0,0,0] | [[0,0,0]] | One output, multiple depths |
| [-1,0,-1] | [[-1,-1,0],[-1,0,-1],[0,-1,-1]] | Negative duplicates |
| [] | [[]] | Empty |
| [7] | [[7]] | Singleton |
| null | NPE | Required reference |

**Target time:** O(n log(n+1)+n*V+n*B+1) for scanning n indexes at each non-base state. B=n!/product(frequency!), but an all-equal input still causes quadratic candidate scanning in this design.

**Target auxiliary space:** O(n+1) copy/marks/path/stack, output O((n+1)*B).

**Completion evidence:** Explain why a duplicate predecessor being unused is different from it already belonging to the prefix. Prove both all-occurrence use and value-output uniqueness. Do not substitute the start-index subset condition blindly. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Choose a consistent identity order for equal occurrences.

</details>

<details>
<summary>Hint 2</summary>

At one output slot, two unused equal occurrences are interchangeable.

</details>

<details>
<summary>Hint 3</summary>

A later equal occurrence becomes necessary after its canonical predecessor has already been used along this path.

</details>

- [ ] **U3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### P1. Audit sign and sorting assumptions

**Difficulty:** Medium. **Objective:** turn a proposed prune into a proof or counterexample. **Prerequisites:** E4; signed sums.

For each claim below, state the exact contract under which it is valid, construct a failing contract/input if it is not universal, and draw the omitted branch. Count accepted recursive entries before and after the rule on your chosen small example.

| Proposed optimization | Investigation |
| --- | --- |
| Return immediately when current sum equals target | Compare strictly positive candidates with a single-use zero and a zero-sum signed extension |
| Reject every sum above target | Inspect [6,-1], target 5, in original input order |
| Break at the first candidate greater than remaining | Compare sorted [3,8] and unsorted [8,3] with remaining 3 |
| Unlimited reuse always progresses | Examine candidate 0, then candidates -1 and 1, without a pick cap |
| A cap makes all positive-only pruning valid on signed values | Separate finite depth from correctness of overshoot and completion tests |

Do not execute an unbounded nonprogressing recursion. Demonstrate a repeated state on paper or with a test-only depth limit, clearly distinguished from a correct solver. Discuss whether unlimited signed reuse may create infinitely many valid outputs rather than merely a slow finite search.

**Completion:** five assumption/counterexample analyses, a finite-state termination argument for your repaired domain, and no claim that pruning universally makes the worst case polynomial.

<details>
<summary>Hint 1</summary>

A prune must cover every allowed continuation, including negative or zero contributions.

</details>

<details>
<summary>Hint 2</summary>

Progress, answer completeness, and ordering are separate properties.

</details>

<details>
<summary>Hint 3</summary>

Use a two-element witness for overshoot and a one-element zero witness for nonprogress. A cap fixes only the depth problem.

</details>

- [ ] **P1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### S1. Find signed position selections

**Difficulty:** Medium. **Learning objective:** Retain valid zero/signed extensions and avoid unproved overshoot pruning.

**Prerequisites:** E1; long arithmetic; P1.

**Problem and output:** Return every subset of input indexes whose values sum to target. Each result is an increasing list of indexes, not values. Different positions remain different answers even when values are equal. Return all qualifying subsets, including the empty one when appropriate.

**Input, constraints, and ownership:** n<=12 size precondition. All int values allowed, including duplicates, zero, negatives and endpoints. Any long target is legal. Each position at most once; no unlimited reuse. Do not sort or mutate input. Use long accumulated sums, which fit under these bounds; avoid potentially overflowing target-minus-sum arithmetic for arbitrary long targets. Only null input needs rejection.

**Java 21 starter**

```java
public static List<List<Integer>> signedSelections(int[] values, long target) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("S1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [6,-1], target=5 | [[0,1]] | Overshoot can recover |
| [0,0], target=0 | [[],[0],[1],[0,1]] | Do not stop at first zero sum |
| [2,2], target=2 | [[0],[1]] | Position identity |
| [-1,1,0], target=0 | [[],[2],[0,1],[0,1,2]] | Zero-sum extensions |
| [], target=0 | [[]] | Empty success |
| [], target=1 | [] | Empty failure |
| [3], target=2 | [] | No solution |
| [Integer.MAX_VALUE,Integer.MAX_VALUE], target=4294967294L | [[0,1]] | Long sum |
| [Integer.MIN_VALUE], target=Long.MAX_VALUE | [] | Arbitrary long target |
| null, target=0 | NPE | Required reference |

**Target time:** O(2^n+S+1) using constant-work decisions and output copies, bounded by O((n+1)*2^n). A signed-safe optional bound requires a separate proof and preprocessing account.

**Target auxiliary space:** O(n+1) path/frames, output O(B+S).

**Completion evidence:** Give a counterexample to stopping enumeration immediately when the current sum equals target. Compare against all index masks; prove unique decision routes without sorting or value deduplication. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Equal-valued positions are still different selectable records.

</details>

<details>
<summary>Hint 2</summary>

A prefix can pass through the target and later return to it.

</details>

<details>
<summary>Hint 3</summary>

Use completion of all positional decisions to decide which complete selections to save; an early success return changes the output set.

</details>

- [ ] **S1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### P2. Capacity pruning and equivalent routes

**Difficulty:** Medium. **Objective:** justify two different reasons to omit a branch. **Prerequisites:** E3/U2.

For values [4,7,9,12,15], k=3, inspect states (start=3,path size=1), (start=4,path size=1), (start=3,path size=2), and (start=5,path size=3). Decide whether each can finish, is already finished, or must fail. Derive an upper bound for the next eligible index from n and needed. Count E3 helper entries with and without that capacity bound for n=5,k=3, including calls that eventually fail.

Then use [1,2,2,2], k=3. Explain why capacity counts occurrences while duplicate suppression identifies equivalent value choices. Give an injective canonical representation argument for retained outputs. Test a proposed global duplicate skip using [2,2], k=2. Finally explain why the single-use availability bound is invalid for a reusable candidate [2] asked to supply three picks.

**Completion:** explicit inequalities, base-case ordering, a counterexample for each mistaken generalization, and two separate correctness proofs.

<details>
<summary>Hint 1</summary>

Needed slots must be compared with remaining supply, not distinct-value count.

</details>

<details>
<summary>Hint 2</summary>

After choosing index j, only n-j-1 later occurrences remain in a single-use search.

</details>

<details>
<summary>Hint 3</summary>

Keep finished states separate from impossible states. A duplicate rule must retain a representative with enough equal occurrences available below it.

</details>

- [ ] **P2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### P3. Necessary board filters

**Difficulty:** Medium. **Objective:** distinguish a necessary filter from a complete existence test. **Prerequisites:** board movement rules from Section 6.

Assess these filters before writing B1: reject if L>R*C; reject if the word requires more occurrences of some letter than the board supplies; accept whenever all required letter counts are available; permanently mark every cell visited by any failed route.

Use board ["ab","xx"] with word "aba" to reason about reuse. Use ["ax","xb"] with word "ab" to test the sufficiency of counts. Construct a separate example where a cell must become eligible again in another candidate route. Explain the cost of validation and a 26-letter frequency precheck, and why neither changes the worst-case exponential search claim.

**Completion:** classify all four proposals, trace a rejected or wrongly accepted route for each unsafe one, and explain the visitation lifetime required by word-path search.

<details>
<summary>Hint 1</summary>

Counts do not encode adjacency.

</details>

<details>
<summary>Hint 2</summary>

A failed route may consume a cell that is useful with different ancestors.

</details>

<details>
<summary>Hint 3</summary>

Write visited's meaning as a statement about the current route, then check whether permanent marking preserves that meaning.

</details>

- [ ] **P3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



## 6. Level 3 — Board search

### Shared board contract

In B1–B3, board and word and every row are nonnull. Check rectangular shape and lowercase English a..z characters in **all cells and all word positions**, even for an empty word. Ragged shape/invalid alphabet throws IAE; required null references throw NPE. Zero rows and zero columns are legal. A displayed row string such as ["ab","cd"] means `new char[][]{{'a','b'},{'c','d'}}`; [] means no rows, ["",""] means two empty rows.

Coordinates are zero-based. Only orthogonal moves are legal. A coordinate cannot be used twice within one route, but it is available again for other routes and later public calls. Repeated letters are legal and do not identify coordinates. Empty word means a valid empty route; a nonempty word on a board with no cells has no route. Check the entire input before an early result. Size bounds are local preconditions.

For a deterministic **trace only**, enumerate starts row-major and neighbors down, right, up, left. Public result order/which successful route is explored first is otherwise unconstrained. Never add a diagonal move silently.

### T6. Trace failed exploration and successful cleanup

**Difficulty:** Medium. **Objective:** trace local marks across a failed branch and an early successful return. **Prerequisites:** T3; board contract.

Use this original board and target "cabd":

```text
        column 0 1 2
row 0          c a b
row 1          a x d
```

At helper entry, ancestors mark exactly the already matched prefix; the proposed cell is not marked yet. Reject invalid proposals before marking; after matching, mark, then use success at the last character. Thus no extra helper call beyond the word is required. Start at (0,0), follow the trace order above, and stop after the first witness.

| Event | Target index | Proposed coordinate | Marks after event | Reason / return |
| --- | --- | --- | --- | --- |
| Root match | 0 | (0,0) | {(0,0)} | Explore |
| First downward proposal | 1 | (1,0) | ____ | ____ |
| Next downward proposal | 2 | (2,0) | ____ | ____ |
| Complete the failed branch | ____ | ____ | ____ | ____ |
| Try a sibling from the root | ____ | ____ | ____ | ____ |
| Final-character success | ____ | ____ | ____ | ____ |
| Every returning accepted frame | ____ | ____ | ____ | ____ |

Expand the table into **all entries and all accepted-cell cleanup events**. Count rejected entries as calls; do not count undo as another call. Report total calls, maximum live helper frames, marks made over time, and final marked set. Repeat the outcome/restoration reasoning for "cabdc" and for the one-cell board ["c"], word "c". Then repeat the successful query on the same board object.

**Completion:** match call versus event counts, show successful unwinding restores root entry state, and explain why short-circuit success does not excuse leaving temporary marks.

<details>
<summary>Hint 1</summary>

A failed downward choice does not prove all root continuations fail.

</details>

<details>
<summary>Hint 2</summary>

A boundary/mismatch/visited rejection creates no new mark to undo.

</details>

<details>
<summary>Hint 3</summary>

Trace cleanup during return propagation separately from the logical true value. The last matched cell owns cleanup too.

</details>

- [ ] **T6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### B1. Match a word with separate visitation

**Difficulty:** Medium. **Learning objective:** Model a proposed cell, target index, and path-local visited coordinates.

**Prerequisites:** T6; E2 restoration; [board theory](./Backtracking_Essential_Concepts.md#13-board-search-and-path-local-visitation).

**Problem and output:** Return whether any four-directional, non-repeating coordinate path spells word. One witness is sufficient. Use a separate boolean[][] visited matrix and leave the board read-only throughout. The empty route spells the empty word.

**Input, constraints, and ownership:** Apply the shared board contract below: R,C<=6 and L<=10 are size preconditions. Row aliasing is allowed in this read-only variant; different logical coordinates remain different cells. Cells can appear in different candidate routes or repeated calls but cannot repeat within one route. Validate before early empty-word success.

**Java 21 starter**

```java
public static boolean existsPath(char[][] board, String word) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("B1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| ["cab","axd"], word="cabd" | true | Turn after a failed downward branch |
| ["cab","axd"], word="cc" | false | No adjacent second c |
| ["ab","xx"], word="aba" | false | Original cell cannot be reused |
| ["aa","aa"], word="aaaa" | true | Repeated letters, distinct coordinates |
| ["aa","aa"], word="aaaaa" | false | Longer than cell count |
| ["a"], word="a" | true | One-cell success |
| ["a"], word="b" | false | One-cell mismatch |
| [], word="" | true | Empty route |
| [], word="a" | false | Empty board |
| ["",""], word="a" | false | Zero-width rows |
| ["ab","a"], word="" | IAE | Ragged shape checked before empty word |
| [null], word="" | NPE | Null row |
| ["A"], word="" | IAE | Alphabet checked before early success |
| ["a"], word="A" | IAE | Invalid target alphabet |

**Target time:** Validation O(R+R*C+L+1), plus O(V) actual helper entries. Conservative search O(R*C*4^L) for L>=1; justify a tighter no-immediate-return bound if desired.

**Target auxiliary space:** O(R+R*C+L+1), including visited row headers and stack; boolean output O(1).

**Completion evidence:** State the helper contract before marking the proposed cell. Explain initialization, soundness, completeness, termination, and cleanup on both outcomes. Test repeated calls and an aliased-row board; compare small boards against a coordinate-path oracle. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The same coordinate with different consumed cells has a different remaining search problem.

</details>

<details>
<summary>Hint 2</summary>

Visited means used by ancestors in the current route, not processed forever.

</details>

<details>
<summary>Hint 3</summary>

Separate rejected proposals from accepted cells. Every accepted mark needs cleanup even when a child returns true.

</details>

- [ ] **B1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### B2. Match with temporary board marks

**Difficulty:** Medium. **Learning objective:** Trade a visited matrix for a stricter mutation/ownership contract.

**Prerequisites:** B1; reserved sentinels and early-return cleanup.

**Problem and output:** Implement the same boolean word-path query using temporary cell marking, without a separate visited matrix. Restore every board cell on ordinary success and failure. Return only whether a route exists.

**Input, constraints, and ownership:** Shared board contract; R,C<=6, L<=10 size preconditions. Additional trusted preconditions: row char[] objects are pairwise distinct, and caller permits temporary mutation with no concurrent observer. No row replacement is allowed. Lowercase-only data reserves NUL as a marker. Do not accept arbitrary sentinel collisions by assumption; the alphabet is checked. Sorting is inapplicable.

**Java 21 starter**

```java
public static boolean existsPathMarked(char[][] board, String word) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("B2");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| ["cab","axd"], word="cabd" | true; board unchanged | Successful unwinding |
| ["cab","axd"], word="cabdc" | false; board unchanged | Failed routes cleaned |
| ["ab","xx"], word="aba" | false; board unchanged | No cell reuse |
| ["a"], word="a" | true; board remains ["a"] | Leaf cleanup |
| ["a"], word="b" | false; board remains ["a"] | Mismatch |
| [], word="" | true | Empty route |
| ["",""], word="a" | false | No cells |
| ["aa","aa"], word="aaaa" | true; board unchanged | Repeated letters |
| ["a"], word="aa" | false; board unchanged | Length precheck |
| ["\u0000"], word="" | IAE | Reserved marker invalid as input |
| ["ab","a"], word="" | IAE | Full shape validation |
| null, word="" | NPE | Required board |

**Target time:** Same validation/search bounds as B1. Explain why temporary marking changes storage but not the exponential search family.

**Target auxiliary space:** O(L+1) stack and scalar state, without allocating a board copy or visited matrix inside the algorithm. Test copies are outside algorithm cost.

**Completion evidence:** Check both character contents and row identities before/after every test. Run the successful query twice. Explain why aliased rows are outside this contract and why shallow outer-array cloning would not fix that issue. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

A marker must be distinguishable from every permitted input character.

</details>

<details>
<summary>Hint 2</summary>

The cell's saved character belongs to the frame that changed it.

</details>

<details>
<summary>Hint 3</summary>

Structure cleanup so that every ordinary return, including the last-character base and a successful child, passes through restoration.

</details>

- [ ] **B2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



For the read-only B1, construct `char[] row={'a','a'}; char[][] board={row,row};` and query "aaaa". The logical grid has four coordinates. Explain why B2's independent-row precondition matters for this fixture. This is an ownership comparison, not an instruction to make B2 handle an excluded input by allocating a hidden full copy.

```java
public record Cell(int row, int col) {}
```

In B3 examples, [r,c] denotes one Cell(r,c), so nested brackets distinguish a route list from a list of route lists.

### B3. List coordinate routes

**Difficulty:** Challenging. **Learning objective:** Change an existence search into complete witness enumeration without conflating equal text with equal routes.

**Prerequisites:** B1, E1 snapshot tests; record equality.

**Problem and output:** Return all coordinate routes spelling word. A route is a list of immutable Cell(row,col) records in traversal order. Different coordinate sequences are different answers even if they spell the same text. No duplicate coordinate sequences. Return all witnesses; empty word has one empty route. Outer order is irrelevant.

**Input, constraints, and ownership:** Shared board contract; smaller size preconditions R,C<=3 and L<=6. Board is read-only, aliased rows allowed, separate visitation required. Each coordinate once per route but may recur in another output. Cell is defined below. Every returned route container is an independent snapshot; immutable Cell objects may be shared.

**Java 21 starter**

```java
public static List<List<Cell>> routes(char[][] board, String word) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("B3");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| ["ab","ba"], word="aba" | [[[0,0],[0,1],[1,1]],[[0,0],[1,0],[1,1]],[[1,1],[0,1],[0,0]],[[1,1],[1,0],[0,0]]] | Four distinct routes |
| ["ab","ba"], word="a" | [[[0,0]],[[1,1]]] | Different starts |
| ["aa"], word="aa" | [[[0,0],[0,1]],[[0,1],[0,0]]] | Same cells, different sequence |
| ["ab"], word="aba" | [] | No reuse |
| [], word="" | [[]] | One empty route |
| [], word="a" | [] | No start |
| ["a"], word="a" | [[[0,0]]] | One-cell route |
| ["a"], word="b" | [] | No match |
| ["A"], word="" | IAE | Validate before empty route |
| [null], word="a" | NPE | Required row |

**Target time:** O(R+R*C+L+V+L*B+1), where V includes rejected proposals. Give a conservative exponential upper bound and account for recording every length-L route.

**Target auxiliary space:** O(R+R*C+L+1) working state, plus O(B+L*B) output.

**Completion evidence:** Explain which early-return decisions from B1 become invalid. Check route adjacency, spelling, coordinate uniqueness, complete oracle agreement, duplicate count, input preservation, and independent inner lists. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The boolean answer discarded witness identity; this output preserves it.

</details>

<details>
<summary>Hint 2</summary>

A completed route is a snapshot, and exploration must still reach other starts and siblings.

</details>

<details>
<summary>Hint 3</summary>

Keep path order separate from visited membership. Saving one route does not end exhaustive enumeration.

</details>

- [ ] **B3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



**Interview follow-ups:** Would a permanent visited matrix answer the same question? Why is (row,col,targetIndex) alone insufficient as a general memoization key? If only one witness were requested, what work could stop early? If the board were immutable and rows shared, which representation respects that ownership contract? Explain these without requiring a DP implementation.


## 7. Level 4 — Curated practice

These are the **seven exact roadmap problems in its original order**. Priority belongs to this guide; platform difficulty is a separate classification. Subsets II remains **Additional** and appears **before Word Search**, as in the [roadmap](./DSA_Interview_Roadmap.md#10-backtracking). Attempt it at that position when useful for duplicate practice, or defer its platform solve while still demonstrating the core duplicate-control drill U2. N-Queens is optional.

Read the official statement for current platform constraints and signatures. The workbook's null/empty/supply contracts are deliberately explicit educational APIs and need not match every platform domain. No platform solutions or full statements are reproduced here.

For each entry below, use its pointer record to locate the single canonical roadmap Log. The Log should hold last date, I/H/S, total minutes, next review date, and mistake/lesson. Do not duplicate its dated history in this workbook.

### L1. Subsets

**Official problem:** [78. Subsets](https://leetcode.com/problems/subsets/). **Priority:** E — Essential. **Difficulty:** Medium.

**Objective:** derive binary decisions and independent output snapshots. **Prerequisites:** T1, F1, E1 and a clear empty-subset interpretation. **Assessed skills:** call contract, positional progress, output ownership, soundness/completeness, copied-output cost.

Before coding, explain which positions have been decided at entry and why every subset has one route. Test empty/singleton domains where permitted, normal distinct values, and result independence. Follow-up: if only the number of subsets were requested, would materializing them still be appropriate? Do not count an editorial-assisted attempt as I.

- [ ] **L1 attempt** — Canonical Log: [roadmap Backtracking — Subsets](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Fill those fields in that Log and use this record as its pointer.

### L2. Combination Sum

**Official problem:** [39. Combination Sum](https://leetcode.com/problems/combination-sum/). **Priority:** E — Essential. **Difficulty:** Medium.

**Objective:** derive reusable positive choices and canonical unordered output. **Prerequisites:** E3/E4, T5, P1. **Assessed skills:** start eligibility, remaining-target meaning, positivity/termination, pruning proof, output copying.

State why the chosen candidate can remain eligible and why earlier candidates should not reopen indiscriminately. Test no result, one repeated candidate, several bundles, and an input whose order differs from your desired representation. Follow-up: how would single-use supply or zero candidates change the proof? Do not transfer E4's cap into the platform contract without justification.

- [ ] **L2 attempt** — Canonical Log: [roadmap Backtracking — Combination Sum](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Keep history in that Log.

### L3. Permutations

**Official problem:** [46. Permutations](https://leetcode.com/problems/permutations/). **Priority:** E — Essential. **Difficulty:** Medium.

**Objective:** enumerate ordered occurrence choices. **Prerequisites:** T2, E2, T3. **Assessed skills:** used[]/path consistency, factorial count, complete restoration, inner-order identity.

Explain why a smaller input index can still be eligible after a larger index was selected. Test three values, minimal input, two consecutive calls, and independent snapshots. Follow-up: what must change when equal values become legal? Derive materialization cost from both number and length of outputs.

- [ ] **L3 attempt** — Canonical Log: [roadmap Backtracking — Permutations](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Keep history in that Log.

### L4. Subsets II

**Official problem:** [90. Subsets II](https://leetcode.com/problems/subsets-ii/). **Priority:** A — Additional. **Difficulty:** Medium.

**Objective:** suppress equivalent siblings without removing valid equal-value supply. **Prerequisites:** T4, U1/U2. **Assessed skills:** occurrence versus answer identity, private sorting ownership, canonical-route proof, output uniqueness.

Test [1,2,2], all-equal input, and a repeated value that must appear twice in a valid answer. Give a counterexample to a skip condition that works only on distinct values. Follow-up: why is generating all positional subsets and deduplicating afterward a different work/space profile?

- [ ] **L4 attempt** — Canonical Log: [roadmap Backtracking — Subsets II](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Keep history in that Log.

### L5. Word Search

**Official problem:** [79. Word Search](https://leetcode.com/problems/word-search/). **Priority:** E — Essential. **Difficulty:** Medium.

**Objective:** derive path-local coordinate validity and correct early-success cleanup. **Prerequisites:** T6, P3, B1/B2. **Assessed skills:** boundaries, spelling progress, non-reuse, ownership, return-state promises, depth versus exponential branching.

Read the platform's actual alphabet before choosing a marker. Test a single cell, a turn, repeated characters, impossible reuse, and both successful/failed restoration in your scratch implementation. Follow-up: how does a requirement to return every route change control flow and output cost?

- [ ] **L5 attempt** — Canonical Log: [roadmap Backtracking — Word Search](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Keep history in that Log.

### L6. Palindrome Partitioning

**Official problem:** [131. Palindrome Partitioning](https://leetcode.com/problems/palindrome-partitioning/). **Priority:** A — Additional. **Difficulty:** Medium.

**Objective:** transfer canonical boundary choices to valid string pieces. **Prerequisites:** X6 or a cold boundary-choice derivation, two-pointer palindrome checks, F2's copying analysis, and [the partition bridge](./Backtracking_Essential_Concepts.md#18-choosing-another-technique). **Assessed skills:** committed-prefix state, piece validity, full coverage, substring work, output ownership.

Describe what changes after a piece is chosen and why discovering one valid partition does not finish enumeration. Test repeated letters, a whole-string palindrome, and a text whose only legal pieces are single letters. Follow-up: distinguish the cost of repeated validity checks from recording partitions. A DP precomputation is an optional later comparison, not a mandatory prerequisite.

- [ ] **L6 attempt** — Canonical Log: [roadmap Backtracking — Palindrome Partitioning](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Keep history in that Log.

### L7. N-Queens

**Official problem:** [51. N-Queens](https://leetcode.com/problems/n-queens/). **Priority:** C — Optional advanced challenge. **Difficulty:** Hard.

**Objective:** investigate incremental row/column/diagonal constraints. **Prerequisites:** ordinary core readiness, O1/O2, reliable restoration proofs. **Assessed skills:** row-by-row state, conflict keys, pruning, cleanup, output-board materialization.

Before attempting the platform, derive diagonal key ranges and validate a candidate against direct pairwise conflicts. Explain why column uniqueness alone is insufficient. Discuss candidate-scan and board-copying costs separately. The workshop supplies only a candidate-helper starter and a partial state exercise; no full solver is supplied.

- [ ] **L7 attempt** — Canonical Log: [roadmap Backtracking — N-Queens](./DSA_Interview_Roadmap.md#10-backtracking); Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —. Optional; keep history in that Log.

Titles and platform difficulties were checked against their official pages. The repository roadmap remains the source of E/A/C classifications.


## 8. Level 5 — Mixed interview challenges

Hide this section's hints and the concepts pattern tables before reading an item. The titles do not identify an algorithm. Write a baseline and compare at least two plausible techniques before coding. The target cost is an acceptance requirement; it is not a substitute for deriving the technique.

For **each X item**, answer: Must multiple candidate solutions be explored? Does the output require enumeration? Is an exponential search space relevant or avoidable? Which constraints permit pruning/elimination? Do repeated values create repeated answers? Could simple recursion, an existing-structure DFS, HashSet, two pointers, sliding window, sorting, or another previously learned method satisfy the contract more directly? No advanced Graph/DP implementation is required.

### X1. Station schedules

**Difficulty:** Medium. **Learning objective:** Derive a constrained ordered output from an unfamiliar statement.

**Prerequisites:** Earlier core exercises; matrix indexing. Hide hints until after an independent attempt.

**Problem and output:** Stations have IDs 0..n-1. Produce every schedule visiting each station exactly once such that no consecutive pair (a,b) has blocked[a][b]=true. The restriction is directed. Return schedules as ID lists; order inside a schedule matters, outer order does not. Empty n has one empty schedule.

**Input, constraints, and ownership:** n in 0..8 is checked. blocked must be a nonnull n-by-n boolean matrix with nonnull rows; wrong shape throws IAE. Diagonal entries are ignored because a station cannot immediately follow itself without reuse. Input is read-only; aliased read-only rows are allowed. IDs are distinct by construction, single-use, and never negative. Duplicate schedules forbidden. No sorting. This is a tiny sequence-constraint problem; advanced graph algorithms are unnecessary.

**Java 21 starter**

```java
public static List<List<Integer>> stationSchedules(int n, boolean[][] blocked) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| n=3, only blocked[0][1]=true | [[0,2,1],[1,0,2],[1,2,0],[2,1,0]] | Directed adjacency constraint |
| n=2, all entries false | [[0,1],[1,0]] | Two valid schedules |
| n=2, blocked[0][1]=blocked[1][0]=true | [] | No valid schedule |
| n=1, blocked=[[true]] | [[0]] | Diagonal irrelevant |
| n=0, blocked=[] | [[]] | Empty schedule |
| n=2, blocked=[[false]] | IAE | Wrong matrix shape |
| n=1, blocked=[null] | NPE | Null row |
| n=9, blocked=[] | IAE | Range |

**Target time:** Derive it before revealing hints. Accept a proved O(n*n!+1) worst-case bound including copies and scans, plus O(n^2+1) input-shape validation allowance. Pruning can shrink actual V without eliminating the unrestricted family.

**Target auxiliary space:** O(n+1) working state, excluding read-only matrix and O((n+1)*B) output.

**Completion evidence:** Explain why checking only the newest neighboring pair is enough to preserve validity, and why one rejected pair cannot eliminate all other next stations. Give complete coverage and uniqueness arguments and a fixture with asymmetric restrictions. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The output requires all valid orders, so one locally attractive schedule is insufficient.

</details>

<details>
<summary>Hint 2</summary>

What facts distinguish the legal next station after a chosen prefix?

</details>

<details>
<summary>Hint 3</summary>

Keep occurrence eligibility and the final chosen station available; apply the local restriction before exploring a candidate.

</details>

- [ ] **X1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### X2. Admission ledger

**Difficulty:** Easy. **Learning objective:** Choose an efficient method when the task requests only repeated equality queries.

**Prerequisites:** Arrays & Hashing; equality and duplicate semantics.

**Problem and output:** For every arrival code, return whether that integer occurs anywhere in allowed. Each query is independent: matching a code does not consume permission. Return a fresh boolean[] in arrivals order. Do not enumerate selections of allowed entries.

**Input, constraints, and ownership:** allowed and arrivals each have at most 100000 entries (size preconditions). All ints, duplicates, zero and negatives legal. Inputs read-only; sorting a private copy is allowed as an alternative with its own cost. Output has exactly arrivals.length entries. Null arrays throw NPE.

**Java 21 starter**

```java
public static boolean[] admission(int[] allowed, int[] arrivals) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X2");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| allowed=[7,2,7], arrivals=[2,9,7,2] | [true,false,true,true] | Repeat permission |
| allowed=[], arrivals=[0] | [false] | No permission |
| allowed=[0], arrivals=[] | [] | No queries |
| allowed=[-1,0], arrivals=[0,-1,1] | [true,true,false] | Signed and zero |
| allowed=[Integer.MIN_VALUE], arrivals=[Integer.MIN_VALUE,Integer.MAX_VALUE] | [true,false] | Endpoints |
| allowed=null, arrivals=[] | NPE | Validate even no queries |

**Target time:** Expected O(n+m+1), n=allowed length and m=arrivals length, under standard hash-table assumptions; contrast a sorted-copy alternative.

**Target auxiliary space:** O(u+1), u distinct allowed codes, plus O(m) output.

**Completion evidence:** Justify why no alternative combinations need exploration. Compare to a nested-loop oracle. Explain why duplicate allowed entries and repeated arrivals must not consume state. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Only whether a value occurs matters.

</details>

<details>
<summary>Hint 2</summary>

Build a reusable membership representation once for this batch.

</details>

<details>
<summary>Hint 3</summary>

A set of values answers equality queries; mark the expected-time assumption and preserve query order.

</details>

- [ ] **X2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### X3. Pairing quote

**Difficulty:** Medium. **Learning objective:** Use ordering to eliminate impossible candidates without enumerating subsets.

**Prerequisites:** Two Pointers and numeric widening; hide topic hints.

**Problem and output:** Return whether two distinct positions in an already nondecreasing array have a sum exactly equal to target. One pair suffices; do not return all pairs. Equal values may form a pair if they occupy different positions.

**Input, constraints, and ownership:** n<=100000 and nondecreasing input are trusted preconditions. All int values and duplicates permitted; target is any long. Input read-only; no sorting needed. Null throws NPE. A position cannot pair with itself. Compute sums in long.

**Java 21 starter**

```java
public static boolean hasQuote(int[] sorted, long target) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X3");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| [-4,-1,0,3,7], target=6 | true | Signed pair |
| [2,2], target=4 | true | Two occurrences |
| [2], target=4 | false | Cannot reuse one position |
| [], target=0 | false | No positions |
| [0,0], target=0 | true | Zero pair |
| [Integer.MIN_VALUE,Integer.MAX_VALUE], target=-1 | true | Long sum |
| [Integer.MAX_VALUE,Integer.MAX_VALUE], target=4294967294L | true | Widen before addition |
| [1,2,3], target=Long.MIN_VALUE | false | Arbitrary target |
| null, target=0 | NPE | Required reference |

**Target time:** O(n+1) worst case; derive a monotone elimination argument instead of searching all 2^n subsets.

**Target auxiliary space:** O(1), scalar output.

**Completion evidence:** Prove why eliminating one endpoint cannot lose a solution under the sorted precondition. Explain how the contract would change if all pairs or an unsorted input were required. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The ordering lets one failed sum rule out more than one pair.

</details>

<details>
<summary>Hint 2</summary>

Compare the smallest and largest still-eligible positions.

</details>

<details>
<summary>Hint 3</summary>

Reason about all pairs involving one endpoint before deciding which boundary moves.

</details>

- [ ] **X3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### X4. Quiet transmission span

**Difficulty:** Medium. **Learning objective:** Recognize a contiguous-range query and maintain a useful interval invariant.

**Prerequisites:** Strings, Arrays & Hashing, Sliding Window.

**Problem and output:** Return the length of the longest contiguous substring of text in which no character repeats. Only the maximum length is requested, not every candidate substring. Empty text returns zero.

**Input, constraints, and ownership:** Nonnull lowercase English a..z String; length<=100000 size precondition. Validate every character; any other UTF-16 code unit throws IAE. Repeated characters are legal in the input but cannot repeat inside an eligible span. Input immutable. No reordering, sorting, or noncontiguous selection.

**Java 21 starter**

```java
public static int quietSpan(String text) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X4");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| "abcaef" | 5 | Best span bcaef |
| "aaaa" | 1 | All equal |
| "" | 0 | Empty |
| "abc" | 3 | Whole input |
| "abba" | 2 | Do not move a left boundary backward |
| "dvdf" | 3 | Overlapping spans |
| "aA" | IAE | Alphabet |
| null | NPE | Required reference |

**Target time:** O(L+1), including full alphabet validation; justify total boundary moves.

**Target auxiliary space:** O(1) for the fixed 26-letter alphabet; scalar output.

**Completion evidence:** Contrast contiguous spans with arbitrary subsequences. Verify against an O(L^2) substring oracle on small texts. Explain why an exponential decision tree would compute unrequested objects. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

A valid interval can often be extended until one repeated character invalidates it.

</details>

<details>
<summary>Hint 2</summary>

Track enough information to restore validity while retaining useful overlap.

</details>

<details>
<summary>Hint 3</summary>

A repeated character requires moving the start beyond its relevant earlier occurrence, never backward.

</details>

- [ ] **X4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



Use this node type for X5. Node value is a reading, not necessarily a unique ID.

```java
public static final class ReadingNode {
    public final int value;
    public ReadingNode left;
    public ReadingNode right;
    public ReadingNode(int value) {
        this.value = value;
    }
}
```

### X5. Terminal readings

**Difficulty:** Easy. **Learning objective:** Distinguish traversing supplied structure from enumerating alternative arrangements.

**Prerequisites:** Trees; custom-node identity; recursion depth.

**Problem and output:** Given a binary tree of ReadingNode objects, return the reading value at every leaf. A leaf has no left or right child. Outer order is irrelevant; equal readings at different leaves must appear repeatedly. Null root returns an empty list. Visit each real node at most once.

**Input, constraints, and ownership:** ReadingNode is defined below. At most 10000 nodes and node-height<=64 are size preconditions. The structure is a proper finite tree with no cycle or shared child (trusted). Reading ints may be any value. Null child/root means absence, not invalid input. Do not mutate nodes or copy whole subtrees. Return fresh mutable output.

**Java 21 starter**

```java
public static List<Integer> terminalReadings(ReadingNode root) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X5");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| root=null | [] | Empty tree |
| singleton(7) | [7] | Root is a leaf |
| root=5, left=leaf(2), right=leaf(9) | [2,9] | Ignore internal value |
| root=8, left=null, right=leaf(0) | [0] | One child is not a leaf |
| root=-1, left=leaf(4), right=leaf(4) | [4,4] | Leaf identities retain duplicates |
| root=0, left=node(3,left=leaf(-2),right=null), right=leaf(6) | [-2,6] | Uneven shape |

**Target time:** O(N+1) for N nodes, including B leaf outputs. Explain why binary branching here does not imply 2^N work.

**Target auxiliary space:** O(h+1) for a depth-first traversal, plus O(B) output; h is node-height.

**Completion evidence:** State a subtree return/append contract, identify the exact leaf predicate, and compare a chain and branching tree. Explain whether used[] and choose/undo are needed for this requested scalar-per-leaf output. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

The input already determines which children exist.

</details>

<details>
<summary>Hint 2</summary>

A node with one missing child still has a continuation.

</details>

<details>
<summary>Hint 3</summary>

A traversal visits structural children; it need not explore subsets or permutations of those children.

</details>

- [ ] **X5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



### X6. Ticket separators

**Difficulty:** Medium. **Learning objective:** Derive complete segmentation choices and local validity from a compact encoding.

**Prerequisites:** String indexes, recursion, snapshots; no DP required.

**Problem and output:** Insert separators into a digit string so every nonempty piece represents a decimal integer from 1 through 255 and has no leading zero. Return all lists of original piece strings. Concatenating a result must reproduce text exactly. Piece order matters, different cut positions define different answers, and duplicate partitions are forbidden. Empty text has one empty partition.

**Input, constraints, and ownership:** text is nonnull; length<=12 size precondition. Validate all characters are ASCII '0'..'9'; otherwise IAE. Zero digits may occur inside pieces but a piece cannot start with '0'. Pieces have at most three characters. Input immutable; no sorting. Every character used exactly once in order; no overlap or gaps. Inner lists fresh, String pieces immutable, outer order irrelevant.

**Java 21 starter**

```java
public static List<List<String>> ticketParts(String text) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("X6");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| "111" | [["1","1","1"],["1","11"],["11","1"],["111"]] | All cut patterns valid |
| "101" | [["10","1"],["101"]] | Zero within pieces |
| "256" | [["2","5","6"],["2","56"],["25","6"]] | Whole piece too large |
| "1001" | [["100","1"]] | No leading-zero suffix piece |
| "0" | [] | No valid first piece |
| "" | [[]] | Empty partition |
| "7" | [["7"]] | Singleton |
| "01" | [] | Leading zero |
| "1x" | IAE | Full validation |
| null | NPE | Required reference |

**Target time:** O(L+V+S+1) with at most three bounded-length piece checks per state; conservative O((L+1)*3^L) including output is acceptable after derivation. S counts saved piece references plus their character contents.

**Target auxiliary space:** O(L+1) live partition/stack and bounded-length pieces; output O(B+S).

**Completion evidence:** Prove each valid cut sequence is reached once, why a zero-start piece is invalid, and why finding one partition cannot end enumeration. Compare to a cut-mask oracle on short strings. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

A partial answer commits a complete prefix of the original text.

</details>

<details>
<summary>Hint 2</summary>

The next decision is a boundary, and numeric validity applies to the newly formed piece.

</details>

<details>
<summary>Hint 3</summary>

Try only lengths that can fit the numeric range; restore the piece list before trying another ending boundary.

</details>

- [ ] **X6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



**Communication drill:** for one enumeration item and one non-enumeration item, give a two-minute explanation of output identity, the deciding constraint, state ownership, correctness, and costs. Explain why your rejected alternative does extra work or assumes something the input does not provide. Keep the result under the same X-item record.


## 9. Complexity laboratory

Read [search-tree time and space](./Backtracking_Essential_Concepts.md#15-search-tree-time-and-complete-space-costs) after attempting these. Answers are deliberately not placed beside the questions. A tight bound needs code-specific reasoning; do not label every search O(2^n).

Use **n** input elements, **b** maximum eligible branching, **d** maximum chosen-decision depth, **R,C** grid dimensions, **L** word/text length, **V** total helper entries, **D** maximum simultaneously live helper frames, **B** output count, and **S** total output elements/characters. Root and base calls count in V/D. A last-character board base can give D=L; an enumerator with a separate empty-suffix call can give D=d+1. Define which convention your implementation uses.

For **every C item**, answer all eleven questions: (1) state; (2) branching; (3) decision depth; (4) explored states; (5) local work; (6) recording cost; (7) total time; (8) maximum frames; (9) auxiliary storage; (10) required output size; (11) assumptions controlling the bound. Then give a small instrumented count and a family that stresses the bound. Missing output costs or confusing V with D does not pass.

### C1. Count a binary call tree

**Difficulty:** Easy. **Objective:** distinguish a branching run from its scalar result. **Preconditions:** 0<=remaining<=20; sufficient resources.

```java
static long binaryCensus(int remaining) {
    if (remaining == 0) {
        return 1;
    }
    return binaryCensus(remaining - 1)
            + binaryCensus(remaining - 1);
}
```

Derive recurrences for result, calls, and maximum frames separately. Trace remaining=0,2,3. Does the scalar return require storing all leaves? Compare this execution with a direct arithmetic computation of the same scalar, then with E1's materialized result contract. Generalize V<=sum(j=0..d)b^j and handle b=0 and b=1 separately.

**Completion:** exact small counts, recurrence justification, scalar-output space, and a reason the arithmetic shortcut does not produce the actual E1 subsets.

- [ ] **C1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C2. Count copied subset cells

**Difficulty:** Medium. **Objective:** include work done inside a called enumerator and storage retained by a wrapper.

```java
static long totalSubsetCells(int[] values) {
    List<List<Integer>> saved = subsets(values); // E1 TODO prerequisite
    long cells = 0;
    for (List<Integer> one : saved) {
        cells += one.size();
    }
    return cells;
}
```

Assume E1 is implemented with one shared path, leaf snapshots, and index decisions. For n=0,1,3, count B and S. Does the wrapper's loop cost S or B when ArrayList.size is constant time? Does that remove the earlier copying work? Relative to this wrapper's scalar output, classify the entire retained saved list as output or auxiliary memory. Contrast that classification with E1's own API.

**Completion:** count how often one input position appears across all subsets, keep the empty-list container in the n=0 account, and justify both API-relative space answers.

<details><summary>Hint 1</summary>

Choose a fixed element and pair each subset omitting it with the subset including it.

</details>

<details><summary>Hint 2</summary>

An already materialized list can be measured cheaply after expensive construction.

</details>

<details><summary>Hint 3</summary>

Space categories follow the public return contract: an intermediate list hidden inside a scalar wrapper is not its required output.

</details>

- [ ] **C2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C3. Candidate scans in arrangements

**Difficulty:** Medium. **Objective:** count rejected candidate checks as real work.

This is a **single-frame scan fragment**, not an arrangement solver. Preconditions: nonnull used/visit; visit returns used to its entry state, performs no lasting caller-visible mutation, and may stand for the corresponding child search in the hypothetical analysis.

```java
static void scanArrangementChoices(boolean[] used,
        java.util.function.IntConsumer visit) {
    for (int i = 0; i < used.length; i++) {
        if (!used[i]) {
            visit.accept(i);
        }
    }
}
```

Analyze an E2 implementation that scans this way at each non-base state and marks/pushes/undoes around each child. Derive counts by depth using falling factorials. For n=3, distinguish all loop checks from successful child edges and leaves. Explain n! versus n*n! when outputs are copied. Contrast an all-distinct input with U3's all-equal input: one output does not imply constant local scanning.

**Completion:** a state count, candidate-check count, depth bound, copying term, and an all-equal counterexample to an unjustified O(n*B) claim for a scan-every-index deduplicated implementation.

<details><summary>Hint 1</summary>

The loop scans all positions even when few remain eligible.

</details>

<details><summary>Hint 2</summary>

Count non-base states separately; complete states need not scan candidates.

</details>

<details><summary>Hint 3</summary>

For equal values under canonical use, inspect the number of non-base depths and the length of each scan.

</details>

- [ ] **C3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C4. Target and capacity bounds

**Difficulty:** Medium. **Objective:** connect preparation, successful edges, and failed break checks.

This fragment only reports eligible candidate indexes to a callback. Preconditions: sorted positive a, valid start, remaining>=0; visit does not modify a. It is not a target-sum solver.

```java
static void scanAffordable(int[] a, int start, int remaining,
        java.util.function.IntConsumer visit) {
    for (int j = start; j < a.length; j++) {
        if (a[j] > remaining) {
            break;
        }
        visit.accept(j);
    }
}
```

Analyze E4 using this loop shape. Why can loop iterations be charged to child edges plus at most one failed break per entered call? Include sorting a private copy and output snapshots. Derive depth from cap, minimum positive value q, target, and single-use n. Handle n=0 and n=1 without hiding them in n^d. Compare the measured V for T5's two modes.

Separately analyze E3 with and without its capacity bound. Derive an output-sensitive bound using B=binomial(n,k); check k=0 and k=n. Explain why the same availability prune cannot be transferred to unlimited reuse.

**Completion:** define every variable, justify each depth bound under its actual assumptions, and avoid treating an observed small V as a polynomial worst-case proof.

<details><summary>Hint 1</summary>

A loop that immediately descends differs from a loop that checks many rejected siblings.

</details>

<details><summary>Hint 2</summary>

Each chosen positive value consumes target, but the cap is a separate limit.

</details>

<details><summary>Hint 3</summary>

For capacity-pruned groups, charge extendable prefixes to paths leading to completed groups. Do not forget the empty group.

</details>

- [ ] **C4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C5. Duplicate-aware search and retained output

**Difficulty:** Medium. **Objective:** keep unique output count separate from candidate scans.

```java
static long countUniqueArrangementCells(int[] values) {
    List<List<Integer>> saved = uniquePermutations(values); // U3 prerequisite
    long cells = 0;
    for (List<Integer> one : saved) {
        cells += one.size();
    }
    return cells;
}
```

Use [1,2,2], four equal values, and n distinct values. Derive B from value frequencies. Analyze a sorted used[] implementation that scans all n positions per non-base call. Compare B, V, candidate scans, copies, and maximum frames. For U1, derive B from independent multiplicity choices instead of the arrangement formula.

Now compare three designs: canonical generation; generate all positional answers then store copies in a HashSet; canonical generation with a private sorted copy. State hashing assumptions and account for discarded snapshots, key comparisons/hashes, sorting, and output retention. Do not assume a set makes the earlier search disappear.

**Completion:** a defensible cost expression in V/B/S, an all-equal scan count, and a clear auxiliary-space classification for this scalar wrapper.

<details><summary>Hint 1</summary>

Permutations and subsets identify different logical objects when values repeat.

</details>

<details><summary>Hint 2</summary>

Deduplicating after generation still pays for every attempted candidate and constructed key.

</details>

<details><summary>Hint 3</summary>

Keep candidate-check counts explicit until the input family justifies simplifying them. Treat retained saved lists as intermediate memory in this wrapper.

</details>

- [ ] **C5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C6. Board recursion and copied visitation

**Difficulty:** Medium. **Objective:** account for geometry, failed proposals, and live matrix copies.

The following is a utility specimen, not a board solver. Preconditions: nonnull source/rows, rectangular R-by-C matrix; empty rows legal.

```java
static boolean[][] copyVisits(boolean[][] source) {
    boolean[][] copy = new boolean[source.length][];
    for (int r = 0; r < source.length; r++) {
        copy[r] = source[r].clone();
    }
    return copy;
}
```

Compare B1, B2, and a hypothetical DFS that calls copyVisits for every child while each parent retains its own matrix for later siblings. Derive copying time and peak live matrix space, including R row headers when C=0. Count stack independently.

For L>=1, bound accepted prefixes from one start: the first step has up to four directions; later steps cannot immediately return to the prior used cell. Explain why rejected boundary/mismatch calls add work but only a constant number per accepted nonterminal state. Compare conservative 4^L and a tighter 3-based upper bound, without asserting exact counts. Handle L=0 and L>R*C separately. For B3 add route-copying output.

**Completion:** time, depth, visited/copy space, output, and validation costs for all variants; use T6's actual run only as a small measured example.

<details><summary>Hint 1</summary>

Maximum live frames follow one route even when many routes are tried.

</details>

<details><summary>Hint 2</summary>

A copied matrix is not a single boolean; all retained ancestor copies remain reachable in the stated design.

</details>

<details><summary>Hint 3</summary>

Sum possible route prefixes by their lengths, multiply by starts, then add validation and recording costs. Do not substitute L=0 into an unexplained negative exponent.

</details>

- [ ] **C6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### C7. Immutable prefixes versus shared buffers

**Difficulty:** Medium. **Objective:** distinguish calls, cumulative copying, and peak retained content. **Preconditions:** prefix nonnull ASCII, 0<=remaining<=256 for this small experiment.

```java
static long copiedPrefixChain(String prefix, int remaining) {
    if (remaining == 0) {
        return prefix.length();
    }
    String next = prefix + "x";
    long below = copiedPrefixChain(next, remaining - 1);
    return below + prefix.length();
}
```

Start with empty prefix and remaining=5, then generalize to n. Under the ordinary materialized-string model in which concatenation copies its content, count total copied characters and the prefix lengths simultaneously retained while ancestors still need their prefix after the child returns. State the model; do not claim byte-exact memory or an API guarantee about JIT allocation.

Compare this single chain with F2's branching token choices. For F2 define V, total appended characters A, maximum live builder length M, number of outputs B, and total output characters S. Empty or duplicate tokens change text length or output equality without necessarily changing call counts. Explain why builder reuse does not remove output snapshot costs, and why saved-length undo applies only while the earlier prefix is preserved.

**Completion:** distinguish a linear call count from repeated-prefix copying and distinguish peak storage from lifetime allocation. Explain both one-chain and branching cases without treating every string operation as O(1).

<details><summary>Hint 1</summary>

Each new immutable prefix contains the old prefix again.

</details>

<details><summary>Hint 2</summary>

The parent uses prefix after its recursive child, so consider retained ancestors as well as the deepest text.

</details>

<details><summary>Hint 3</summary>

Sum lengths along one active chain for peak content, and across all explored edges for cumulative allocation. Output copies remain a separate required cost.

</details>

- [ ] **C7 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



## 10. Debugging laboratory

Every specimen below is deliberately flawed under its stated intended contract. Treat each as an isolated executable experiment. Preconditions are trusted for these debugging fragments; a fragment that omits production validation is not defective for that reason unless validation is the stated issue. Do not “repair” the problem by weakening the intended output or restoration contract.

For **every D item**, submit: (1) defect location; (2) your own smallest failing input plus a passing control; (3) a line-by-line incorrect-state trace; (4) violated invariant/contract; (5) a corrected scratch implementation; (6) restoration/ownership checks; (7) regression tests including repeated calls where relevant; (8) corrected time, depth, auxiliary/output costs. First predict; then execute. The hints do not provide complete corrections.

### D1. A saved list that keeps changing

**Difficulty:** Easy. **Intended contract:** for each a[i], return a fresh one-element list [a[i]], preserving occurrence order; independent inner objects. Preconditions: nonnull a, n<=8, any ints.

```java
static List<List<Integer>> badSnapshots(int[] a) {
    List<Integer> path = new ArrayList<>();
    List<List<Integer>> result = new ArrayList<>();
    for (int value : a) {
        path.add(value);
        result.add(path);
        path.remove(path.size() - 1);
    }
    return result;
}
```

Start with [3,8]; include [] and [0] as controls. Predict values and reference identity separately. Explain why fixing restoration would not address this defect: the path already returns to its entry contents.

<details><summary>Hint 1</summary>

A saved reference is not a record of earlier contents.

</details>

<details><summary>Hint 2</summary>

Count allocated inner-list objects, not add calls.

</details>

<details><summary>Hint 3</summary>

The output container needs an ownership boundary at each observation.

</details>

- [ ] **D1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D2. A sibling inherits earlier choices

**Difficulty:** Easy. **Intended contract:** the F1 observation sequence, including exact restoration of the caller prefix. Preconditions: nonnull mutable prefix with nonnull Integers, nonnull choices; lengths<=8.

```java
static List<List<Integer>> badSiblingPrefix(
        ArrayList<Integer> prefix, int[] choices) {
    List<List<Integer>> result = new ArrayList<>();
    for (int value : choices) {
        prefix.add(value);
        result.add(new ArrayList<>(prefix));
    }
    result.add(new ArrayList<>(prefix));
    return result;
}
```

Use prefix=[4], choices=[6,9], then choices=[]. Check both output and caller-visible prefix. Explain why every inner result can be independent while the algorithm is still wrong.

<details><summary>Hint 1</summary>

Each alternative should start from the same ancestor prefix.

</details>

<details><summary>Hint 2</summary>

The saved snapshots protect output, not the next sibling's state.

</details>

<details><summary>Hint 3</summary>

Identify which loop iteration owns each append and where that owned change should end.

</details>

- [ ] **D2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D3. Marks outlive their route

**Difficulty:** Medium. **Intended contract:** E2's full arrangements. Preconditions: nonnull distinct a, n<=7.

```java
static List<List<Integer>> badUsed(int[] a) {
    List<List<Integer>> out = new ArrayList<>();
    badUsedFrom(a, new boolean[a.length], new ArrayList<>(), out);
    return out;
}
private static void badUsedFrom(int[] a, boolean[] used,
        List<Integer> path, List<List<Integer>> out) {
    if (path.size() == a.length) {
        out.add(new ArrayList<>(path));
        return;
    }
    for (int i = 0; i < a.length; i++) {
        if (used[i]) {
            continue;
        }
        used[i] = true;
        path.add(a[i]);
        badUsedFrom(a, used, path, out);
        path.remove(path.size() - 1);
    }
}
```

Trace [3,8] and [3]. On the first return to root, compare path with used. Construct a three-value regression and verify the repaired implementation restores all marks, not just the last one.

<details><summary>Hint 1</summary>

Path contents are only part of the shared state.

</details>

<details><summary>Hint 2</summary>

A true used entry must mean the position belongs to the current prefix.

</details>

<details><summary>Hint 3</summary>

The frame that acquires one unused occurrence must release its mark after its child restores deeper changes.

</details>

- [ ] **D3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D4. The wrong supply policy

**Difficulty:** Medium. **Intended contract:** return whether a single-use subset of positive distinct a sums to target. Preconditions: sorted positive distinct a, n<=6; target in 0..12. Empty subset is valid at target zero.

```java
static boolean badSingleUse(int[] a, int target) {
    return badSupplyFrom(a, 0, target);
}
private static boolean badSupplyFrom(int[] a, int start, int remaining) {
    if (remaining == 0) {
        return true;
    }
    for (int j = start; j < a.length; j++) {
        if (a[j] > remaining) {
            break;
        }
        if (badSupplyFrom(a, j, remaining - a[j])) {
            return true;
        }
    }
    return false;
}
```

Trace [2], target 4 and target 2. What exactly does the child start permit? Is this a termination defect, an answer defect, or both under the stated positive domain? Then consider what would happen if candidate 0 were admitted without changing anything else.

<details><summary>Hint 1</summary>

Numeric progress does not prove that supply restrictions were respected.

</details>

<details><summary>Hint 2</summary>

The selected occurrence remains eligible in the child.

</details>

<details><summary>Hint 3</summary>

Derive next eligibility from the actual chosen index and the single-use contract.

</details>

- [ ] **D4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D5. A duplicate rule removes valid supply

**Difficulty:** Medium. **Intended contract:** all unique value subsets as in U1. Preconditions: sorted nonnull a, n<=8.

```java
static List<List<Integer>> badDuplicateSkip(int[] a) {
    List<List<Integer>> out = new ArrayList<>();
    badDuplicateFrom(a, 0, new ArrayList<>(), out);
    return out;
}
private static void badDuplicateFrom(int[] a, int start,
        List<Integer> path, List<List<Integer>> out) {
    out.add(new ArrayList<>(path));
    for (int i = start; i < a.length; i++) {
        if (i > 0 && a[i] == a[i - 1]) {
            continue;
        }
        path.add(a[i]);
        badDuplicateFrom(a, i + 1, path, out);
        path.remove(path.size() - 1);
    }
}
```

Use [1,2,2], then [2,2], then a distinct-value control. Identify the first missed valid multiset and explain why no later branch can recover it. A lower output count alone is not evidence of correct deduplication.

<details><summary>Hint 1</summary>

The second equal occurrence can be necessary below a prefix already using the first.

</details>

<details><summary>Hint 2</summary>

A sibling relationship is local to one call.

</details>

<details><summary>Hint 3</summary>

Tie the duplicate guard to the current eligibility boundary while preserving descendant supply.

</details>

- [ ] **D5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D6. An unproved signed prune

**Difficulty:** Medium. **Intended contract:** existence of a single-use signed subset summing to target. Preconditions: nonnull a, n<=12; initial i=0,total=0; target any long; recursive sums fit long.

```java
static boolean badSigned(int[] a, int i, long total, long target) {
    if (total > target) {
        return false;
    }
    if (i == a.length) {
        return total == target;
    }
    return badSigned(a, i + 1, total + a[i], target)
            || badSigned(a, i + 1, total, target);
}
```

Trace [6,-1], target 5. Add a negative-target witness and a nonnegative passing control. Explain why sorting alone does not establish the needed sign property for every eligible suffix. Compare a repaired unpruned baseline with any proposed signed-safe bound.

<details><summary>Hint 1</summary>

A later choice may lower the total.

</details>

<details><summary>Hint 2</summary>

The first guard can discard a branch before its remaining negative contribution is considered.

</details>

<details><summary>Hint 3</summary>

Either remove the unsupported shortcut or prove a bound using actual remaining supply; do not silently reject signed inputs.

</details>

- [ ] **D6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D7. A successful leaf leaks its marker

**Difficulty:** Medium. **Intended contract:** this accepted-cell fragment returns whether the matching cell completes the query and leaves board unchanged. Preconditions: valid r,c, nonnull lowercase board; wanted lowercase; last describes whether this is the final character.

```java
static boolean badMarkedLeaf(char[][] board, int r, int c,
        char wanted, boolean last) {
    if (board[r][c] != wanted) {
        return false;
    }
    char saved = board[r][c];
    board[r][c] = '\0';
    if (last) {
        return true;
    }
    board[r][c] = saved;
    return false;
}
```

Use ["c"], wanted='c', last=true twice on the same object. Compare last=false and a mismatching wanted character. Does testing only failed searches expose the defect? State what would happen to all active ancestor marks in a full DFS with similar early returns.

<details><summary>Hint 1</summary>

The boolean can be correct while the post-call board is wrong.

</details>

<details><summary>Hint 2</summary>

Trace the success return without mentally executing statements below it.

</details>

<details><summary>Hint 3</summary>

Every path leaving an accepted frame needs cleanup, including the final-character base.

</details>

- [ ] **D7 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D8. Undo by value changes an ancestor

**Difficulty:** Easy. **Intended contract:** temporarily append candidate and restore path exactly. Preconditions: nonnull mutable ArrayList with nonnull Integer elements; any int candidate.

```java
static void badTailUndo(ArrayList<Integer> path, int candidate) {
    path.add(candidate);
    path.remove(Integer.valueOf(candidate));
}
```

Use path=[6,1], candidate=6; then path=[6,1], candidate=9. Explain why the second test can hide the defect. Would replacing the argument with primitive candidate fix it? Give a separate exception or wrong-index witness for that proposed repair.

<details><summary>Hint 1</summary>

A value may already occur in the ancestor prefix.

</details>

<details><summary>Hint 2</summary>

Object removal searches for the first equal occurrence.

</details>

<details><summary>Hint 3</summary>

Undo should identify the owned appended position, independent of its value.

</details>

- [ ] **D8 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D9. A boundary guard admits an invalid row

**Difficulty:** Easy. **Intended contract:** return whether any proposed integer coordinate is valid and matches wanted. Preconditions: nonnull rectangular board with at least one nonempty row.

```java
static boolean badCoordinate(char[][] board, int r, int c, char wanted) {
    if (r < 0 || r > board.length
            || c < 0 || c >= board[0].length) {
        return false;
    }
    return board[r][c] == wanted;
}
```

Test the coordinate just below ["c"], the valid origin, a negative row, and a column equal to width. Identify the exact first invalid row value. Explain why the character comparison cannot be trusted to reject an already invalid access.

<details><summary>Hint 1</summary>

Array length is not a valid index.

</details>

<details><summary>Hint 2</summary>

Compare the row guard with the column guard.

</details>

<details><summary>Hint 3</summary>

Validity must be established before indexing either dimension.

</details>

- [ ] **D9 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D10. A walk is mistaken for a non-repeating route

**Difficulty:** Medium. **Intended contract:** B1's no-reuse existence query on a nonempty word. Preconditions: nonempty rectangular lowercase board at most 2-by-2; nonempty lowercase word length<=6.

```java
static boolean badWalk(char[][] board, String word) {
    for (int r = 0; r < board.length; r++) {
        for (int c = 0; c < board[0].length; c++) {
            if (badWalkFrom(board, word, r, c, 0)) {
                return true;
            }
        }
    }
    return false;
}
private static boolean badWalkFrom(char[][] b, String w, int r, int c, int at) {
    if (r < 0 || r >= b.length || c < 0 || c >= b[0].length
            || b[r][c] != w.charAt(at)) {
        return false;
    }
    if (at == w.length() - 1) {
        return true;
    }
    return badWalkFrom(b, w, r + 1, c, at + 1)
            || badWalkFrom(b, w, r, c + 1, at + 1)
            || badWalkFrom(b, w, r - 1, c, at + 1)
            || badWalkFrom(b, w, r, c - 1, at + 1);
}
```

Use ["ab","xx"] with "aba", then "ab". Trace the offending coordinate sequence. Explain why advancing the word index proves termination but does not prove route validity. A total-cell length precheck alone is insufficient here.

<details><summary>Hint 1</summary>

Matching the next character is only one validity condition.

</details>

<details><summary>Hint 2</summary>

Which coordinates has the current candidate route already consumed?

</details>

<details><summary>Hint 3</summary>

Add state with path-local lifetime. A permanent mark fixes one defect by introducing a different completeness defect.

</details>

- [ ] **D10 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D11. The wrong inverse of an append

**Difficulty:** Medium. **Intended contract:** append a token temporarily and restore the original builder text. Preconditions: nonnull text/token, text contains an arbitrary existing prefix.

```java
static void badTextUndo(StringBuilder text, String token) {
    text.append(token);
    text.setLength(text.length() - 1);
}
```

Use prefix "q" with token "ab", token "", and token "x"; also use empty prefix/token. Predict exceptions as well as changed content. Propose an entry/return assertion that would detect the problem inside a deeper F2 helper.

<details><summary>Hint 1</summary>

A token need not contain exactly one character.

</details>

<details><summary>Hint 2</summary>

An empty append still leaves this code removing something.

</details>

<details><summary>Hint 3</summary>

The inverse should restore the saved boundary, not a guessed amount based on the final length.

</details>

- [ ] **D11 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### D12. One invocation edits another's answer

**Difficulty:** Medium. **Intended contract:** each call returns a fresh single-entry list containing a fresh one-element list [value]. Prior answers remain unchanged.

```java
static final List<List<Integer>> staleAnswers = new ArrayList<>();
static List<List<Integer>> badRequest(int value) {
    staleAnswers.add(new ArrayList<>(List.of(value)));
    return staleAnswers;
}
```

Starting from empty staleAnswers in an isolated fixture, retain the result of badRequest(3), then call badRequest(8). Inspect both references afterward. Would clearing the shared outer list at the start preserve the first caller's result? Explain why an invocation-owned result container is a different lifetime choice from one shared field.

**Completion:** test two calls, two retained references, and a caller modifying one result. Include output ownership in the corrected contract, not only result values.

<details><summary>Hint 1</summary>

A newly allocated inner list does not make the outer list independent.

</details>

<details><summary>Hint 2</summary>

The first caller still holds the shared result object during the second invocation.

</details>

<details><summary>Hint 3</summary>

State used by one search should outlive its frames only through that search's own returned result, unless persistence is explicitly requested.

</details>

- [ ] **D12 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.



## 11. Optional N-Queens workshop

**Optional advanced material.** O1, O2, and L7 are not required for ordinary Backtracking interview readiness. The [theory section](./Backtracking_Essential_Concepts.md#16-optional-constraint-satisfaction) supplies the constraint model; this workshop asks you to demonstrate it without a full solution listing.

### O2. Acquire and restore constraint marks

**Difficulty:** Challenging, optional. **Objective:** derive row-by-row state and ownership of column/diagonal marks. **Prerequisites:** choose/explore/undo, candidate validity, ordinary core readiness.

For n=4, begin with no queens. Place row 0 at column 1, then assess every column of row 1. Use column key c, difference key r-c+n-1, and sum key r+c.

| Stage | Chosen columns by row | Occupied columns | Difference keys | Sum keys |
| --- | --- | --- | --- | --- |
| Entry | [] | {} | {} | {} |
| After row 0, col 1 | [1] | ____ | ____ | ____ |
| Allowed row-1 candidate | ____ | ____ | ____ | ____ |
| After undoing row 1 | [1] | ____ | ____ | ____ |
| After undoing row 0 | [] | {} | {} | {} |

Complete the remaining rows of one valid four-row placement on paper, then undo in reverse order. Reject at least one same-column and two different diagonal conflicts. Draw the column vector as a board with Q and dots. Compare with a direct pairwise conflict test; derive key ranges for arbitrary n.

**Partial implementation task:** implement O1 only, then write a skeleton for one row's candidate loop in your scratch file with named placeholders for candidate validation, acquiring three keys, recording a column, delegating the next row, and undoing all owned changes. Leave the complete solver as the optional L7 investigation. State what must be true before blindly clearing a key can preserve ancestors.

**Complexity investigation:** compare scanning previous rows to O(1) mask checks. Bound prefix states using unused columns, then include scanning n columns per non-base state. If B complete n-by-n boards are returned, account separately for n^2 characters per board. Explain why pruning by diagonals does not justify a general polynomial claim.

**Completion:** accurate mask table, pairwise verification, restoration to each entry state, key-range proof, and a cost expression that includes actual output format. A complete platform solution is not necessary for O2.

<details><summary>Hint 1</summary>

Advancing one row after each placement makes row uniqueness automatic.

</details>

<details><summary>Hint 2</summary>

Two cells share one diagonal family when their row-minus-column or row-plus-column values agree.

</details>

<details><summary>Hint 3</summary>

Only acquire unused keys; then each returning frame can safely clear exactly the keys it acquired after children restore theirs.

</details>

- [ ] **O2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### O1. Validate one queen candidate (optional)

**Difficulty:** Medium, optional. **Learning objective:** Implement only a constant-time constraint check, without a full solver.

**Prerequisites:** O2 or [optional theory](./Backtracking_Essential_Concepts.md#16-optional-constraint-satisfaction); boolean arrays.

**Problem and output:** Return whether a queen at (row,col) conflicts with no occupied column or diagonal represented by the three supplied masks. Do not place a queen or recurse. difference uses row-col+n-1; sum uses row+col. Arrays describe a legal prefix of queens in earlier rows.

**Input, constraints, and ownership:** Check 1<=n<=12, 0<=row,col<n, columns.length==n, difference.length==sum.length==2*n-1; wrong ranges/lengths throw IAE and required null arrays throw NPE. Correct mask contents for a legal earlier-row prefix are trusted. Read-only masks. No sorting/output collection/candidate reuse. Distinct mask arrays are a trusted ownership precondition.

**Java 21 starter**

```java
public static boolean queenCandidate(int n, int row, int col, boolean[] columns, boolean[] difference, boolean[] sum) {
    // TODO: derive the state, implement, and test the contract.
    throw new UnsupportedOperationException("O1");
}
```

**Examples and required edge tests**

| Input | Expected result | Risk checked |
| --- | --- | --- |
| n=1,row=0,col=0; all masks empty | true | Smallest board |
| n=4,row=1,col=0; columns={1}, difference={2}, sum={1} | false | Sum diagonal |
| n=4,row=1,col=1; same masks | false | Column |
| n=4,row=1,col=2; same masks | false | Difference diagonal |
| n=4,row=1,col=3; same masks | true | Allowed candidate |
| n=4,row=0,col=4; correctly sized empty masks | IAE | Out-of-range column |
| n=0,row=0,col=0; empty arrays | IAE | Invalid n |
| n=4,row=0,col=0; columns length=3, others length=7 | IAE | Mask length |
| n=4,row=0,col=0; columns=null, others length=7 | NPE | Required reference |

**Target time:** O(1), including length/range checks; no scan of prior rows.

**Target auxiliary space:** O(1), boolean output. Masks are read-only inputs here, not newly allocated working arrays.

**Completion evidence:** Compare every candidate against a direct pairwise conflict check for small legal prefixes. Explain why the mask keys fit the declared ranges. This helper and L7 do not block ordinary readiness. Apply the Section 2 completion standard.

<details>
<summary>Hint 1</summary>

Each conflict family needs one direct indexed lookup.

</details>

<details>
<summary>Hint 2</summary>

The difference diagonal needs an offset to avoid negative indexes.

</details>

<details>
<summary>Hint 3</summary>

Validate dimensions before access; combine the three occupancy tests without changing their inputs.

</details>

- [ ] **O1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.




## 12. Mock interviews

Use the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) and [Phase 7 protocol](./DSA_Interview_Roadmap.md#phase-7-mixed-practice-and-interview-preparation). A typical Medium session is approximately 35–45 minutes including clarification, coding, tests, and explanation; adjust to the target interview. Opening a conceptual hint changes the result to H even if the implementation is fast.

### M1. Derive and defend an unfamiliar enumerator

**Difficulty:** Medium mock. **Objective:** demonstrate recognition, state design, restoration, and complete output without remembered code.

Have an interviewer select an unread X1 or X6, or a genuinely unfamiliar comparable constrained enumerator. If you already know both, the interviewer must change the problem meaningfully and supply a complete contract; merely renaming values does not establish transfer. Keep the workbook's hints and target explanations closed.

Spend the session clarifying output identity and constraints, proposing brute force, deriving state, coding from a blank editor, testing, and explaining costs. The interviewer should ask:

- What does one helper promise on entry and return?
- Which facts belong in primitive parameters, path, used marks, and shared output?
- Why is this output valid? How do you follow an arbitrary valid output through your choices?
- Can two routes produce the same logical answer?
- What proves the chosen prune safe under the stated sign/supply constraints?
- What happens after a successful child returns?
- What changes if the question requests only existence?
- Which time term counts output construction, and what is live at maximum depth?

**Pass evidence:** independent correct Java, a precise restoration and progress argument, complete small-oracle agreement, and honest costs. Record the main gap and follow-up under this item; put category R evidence only in the roadmap when repeated unseen success across sessions supports it.

<details><summary>Hint 1</summary>

If used for learning, first restate what makes two answers equal.

</details>

<details><summary>Hint 2</summary>

Write a helper promise before deciding which parameters it needs.

</details>

<details><summary>Hint 3</summary>

Trace one child return and its next sibling with every mutable object visible. Using this guidance makes the mock H.

</details>

- [ ] **M1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


### M2. Select a technique and survive a changed constraint

**Difficulty:** Medium mock. **Objective:** avoid forcing enumeration onto a simpler workload and communicate tradeoffs.

Have the interviewer choose an unread X2–X5 and one short board or duplicate-control follow-up from B1/U2. Familiar tasks may be used for review, but category R requires a genuinely changed or unseen variant. Use a two-problem format suited to your interview.

For the first problem, state the simplest baseline and show whether lookup, sorting, two pointers, a window, or a traversal meets the output without exploring alternative solutions. For the follow-up, identify exactly what is path-local, what must be copied, and which old invariant a changed requirement invalidates. The interviewer may change input order, allow duplicate values, require all witnesses, or forbid temporary mutation; state the new contract before revising the method.

**Pass evidence:** a justified technique choice, a correctly revised invariant, tested Java for the agreed task, two adversarial cases, and costs including preprocessing/stack/output. Explain why at least one plausible alternative fails or performs unnecessary work. Do not label the first DFS witness shortest without a proof. No advanced Graph/DP implementation is required.

<details><summary>Hint 1</summary>

Required operations and output often distinguish the technique more clearly than story words.

</details>

<details><summary>Hint 2</summary>

Identify the exact old assumption removed by the follow-up.

</details>

<details><summary>Hint 3</summary>

Recheck answer identity, eligibility, and state ownership before changing code. If this guidance is used, record H.

</details>

- [ ] **M2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Mistake/lesson: —.


## 13. Revision and spaced repetition

Use the [roadmap review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): approximately **1, 3, 7, and 14 days** after the learning attempt, adjusted to retention and available time. These are flexible reminders, not deadlines. Keep each item's next review in its existing record; curated history stays in the canonical roadmap Log. Do not create another review ledger.

For a cold re-solve, open only the statement and signature. Close previous code, concepts, hints, and output traces. Reconstruct the contract, baseline, invariant, pruning proof, termination, and costs; implement from a blank editor; test before looking back. Compare only after completion, record I/H/S honestly, and note the one reasoning mistake that mattered most. A remembered solution needs an unfamiliar variant before it supports R.

Prioritize S and repeated H, then incorrect recursive contracts, missing restoration, duplicate-control mistakes, unsafe pruning, complexity errors, and unresolved board edge cases. A useful review rotates a small ownership drill, one core enumerator, and a mixed recognition task instead of repeating only familiar subsets.

After a restoration failure, test both a nonempty entry prefix and a successful early return. After a duplicate mistake, test all-equal input and required repeated occurrences. After an unsafe prune, produce a counterexample before repairing it. After a cost mistake, count calls, candidate checks, copied elements, and maximum frames independently. Retest at a later session; passing immediately after reading the fix is not a cold success.

If review invalidates a prerequisite, use the roadmap's Reviewing status and reassess affected gates. Minor independently corrected syntax errors need not erase unrelated evidence. No exercise is automatically marked complete by this document's existence or author validation.

## 14. Completion and readiness

The table below is an evidence map, not a duplicate topic dashboard. Update T/I/P/R only in the [roadmap](./DSA_Interview_Roadmap.md#four-independent-progress-gates). Use the records already attached to exercises; there is no second progress percentage.

| Milestone | Demonstration | Gate relationship |
| --- | --- | --- |
| Understand decision trees | T1/T2: distinguish total states, depth, and outputs | T evidence |
| Define state and recursive contracts | State eligibility, validity, completion, ownership, and progress without notes | T; required before I |
| Choose → explore → undo | F1/T3 and F2: restore a nonempty path/builder prefix exactly | I evidence |
| Generate subsets | E1 from blank editor, three values, independent snapshots, 2^n proof | Core I drill |
| Generate arrangements | E2 from blank editor, used/path restored, factorial and copying explanation | Core I drill |
| Generate combinations | E3/E4: canonical order, supply policy, target/cap meaning | I evidence |
| Handle duplicate values | U2 from blank editor, plus explain U1/U3 distinction without losing multiplicity | Core I drill; duplicate ideas are not optional |
| Prune safely | P1/P2/S1: prove assumptions and construct counterexamples when they change | T/I evidence |
| Search boards | B1 and ownership comparison with B2; success/failure restoration and repeated calls | Core I drill |
| Explain correctness | Soundness, completeness, uniqueness where required, restoration, termination | Every implementation/practice assessment |
| Analyze search cost | C1–C7: define V/D/B/S and count scans, preparation, copies, output | T/I/P/R explanations |
| Complete Essential practice | Independent or cold independent L1/L2/L3/L5 with tests and explanations | P |
| Add targeted breadth | L4/L6 after relevant foundations or to repair a gap | Additional platform completion is not a blanket gate |
| Investigate advanced constraints | O1/O2/L7 only when desired | Optional; never blocks ordinary readiness |
| Demonstrate interview transfer | Unseen X-style tasks and mocks across separate sessions with no conceptual hints | R plus common rubric |

Before reporting ordinary Backtracking interview readiness, demonstrate all of the following in your existing evidence: select the technique from an unfamiliar statement; derive state, choices, constraints and bases; implement exploration independently; restore mutable Java state; enumerate subsets/permutations/groups; preserve needed repeated occurrences without repeated logical results; justify every prune; implement path-local visitation; prevent shared-output aliases; separate search size from recursion depth; include copying and preparation costs; and explain coverage during a mock.

A single polished solution or a large count of attempted exercises is insufficient. The [Backtracking readiness paragraph](./DSA_Interview_Roadmap.md#10-backtracking) asks for an unseen enumerator with coverage, validity, uniqueness, restoration, copying, termination, and a defensible bound. Combine that with the common rubric's recognition, Java correctness, edge tests, repeatable independence, and communication. N-Queens completion is not part of ordinary readiness.

## 15. References and author validation

### Repository references

All paths below were discovered in this repository. Reuse these sources for targeted repair rather than rereading every chapter before each attempt.

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [progress gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), [common rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category), and [review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition).
- [Backtracking Essential Concepts](./Backtracking_Essential_Concepts.md): [state design](./Backtracking_Essential_Concepts.md#4-designing-the-search-state), [references and snapshots](./Backtracking_Essential_Concepts.md#6-java-references-snapshots-and-buffers), [safe pruning](./Backtracking_Essential_Concepts.md#12-safe-pruning), [correctness](./Backtracking_Essential_Concepts.md#14-soundness-completeness-and-uniqueness), and [costs](./Backtracking_Essential_Concepts.md#15-search-tree-time-and-complete-space-costs).
- [Recursion Essential Concepts](./Recursion/Recursion_Essential_Concepts.md): [contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness), [references](./Recursion/Recursion_Essential_Concepts.md#13-java-references-and-recursive-state), [branching](./Recursion/Recursion_Essential_Concepts.md#9-multiple-branch-recursion).
- [Trees Essential Concepts](./Trees/Trees_Essential_Concepts.md): [path meanings](./Trees/Trees_Essential_Concepts.md#13-tree-paths-and-returned-state), [time/height/space](./Trees/Trees_Essential_Concepts.md#15-time-height-width-and-complete-space).
- [Tries Essential Concepts](./Tries/Tries_Essential_Concepts.md): [wildcard DFS](./Tries/Tries_Essential_Concepts.md#9-wildcard-search-using-dfs). Trie-guided multiword board search remains separate optional Tries material.
- [Java List, ArrayList, and LinkedList](./Java_List_ArrayList_LinkedList.md), [Java Strings](./Java_String.md), [Java Arrays](./Java_Array.md), and [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) and [space](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity).
- For mixed-task repair: [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md), [Sliding Window](./Sliding_Window/Sliding_Window_Essential_Concepts.md), and [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).

### Primary sources

Java 21 API details were checked using Context7 and the primary [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html) and [StringBuilder](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html) documentation. The official problem pages linked at L1–L7 support their titles and difficulties; the repository roadmap supplies priorities and order. No full platform statement or solution is reproduced.

### Author validation

Completed **2026-10-31**. The workbook contains **15 sections**, **52 original exercises**, **seven curated problems** in exact **E/E/E/A/E/A/C** order, **59 unchecked attempt records**, and **150 optional hint panels**. All **84 local links**, including heading anchors, resolve.

All **43 Java blocks** were assembled into one scratch class and compiled with Java 21 using `--release 21 -Xlint:all -Werror`. The **20 learner starters remain TODOs**. Compilation checks syntax and integration; it does not establish learner-solution correctness.

Independent example oracles verified **all 178 coding test rows** with **311 assertions**. They used position masks for subsets/groups/signed selections, iterative next-permutation for arrangements, multiplicity vectors for bundles, Cartesian products for text choices, coordinate-route expansion for board witnesses, cut masks for ticket partitions, and simple scans/pairwise checks for mixed tasks. Comparisons respected each exercise's output order, multiplicity, representation, and invalid-input policy.

Java runtime validation passed **308,041 assertions**, including **41 witness/control checks** across all **12 published faulty specimens**, the two Java-state traces, and executable analysis fragments. To test feasibility and state ownership without filling learner starters, temporary fixtures extracted the existing concepts guide's algorithms. These fixtures were compared with independent oracles across **1,095 array cases**, **2,496 target/supply/cap cases**, and **11,813 board cases**. Checks covered complete logical result sets, uniqueness, independent snapshots, preserved inputs, successful/failed board restoration, row identity, repeated calls, and the read-only aliased-row case. **Ten direct helper tests** verified restoration of nonempty entry prefixes, builder contents, and preexisting ancestor visitation/board marks. These are checks of reference fixtures and published specimens, **not executions of unfinished learner implementations**.

Additional instrumentation passed **85 checks across 11 measured search cases**, verifying subset/permutation/group counts, duplicate candidate scanning, both supply traces, board entries/depth/cleanup, and optional queen constraint restoration. The C2/C5 scalar wrappers were exercised with test-only calls to the corresponding theory fixtures; their published calls still refer to learner TODO prerequisites. Partial diagrams and tables deliberately retain blanks; validation did not insert answer keys or complete exercise solutions.

SHA-256 checks confirmed that all **39 files present at task start** were unchanged. Only **Backtracking_Exercises.md** was created by this task inside DSA_Guide; compilation and validation artifacts stayed in a temporary directory. No prerequisite dependency is missing, and no validation issue remains unresolved.


