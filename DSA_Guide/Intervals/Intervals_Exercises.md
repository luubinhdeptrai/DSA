# Intervals — Exercises (Java 21)

Decide what each boundary includes, what the output must preserve, and what makes an earlier decision final. This workbook complements [Intervals Essential Concepts](./Intervals_Essential_Concepts.md). Keep its implementations closed during an independent attempt.

## Contents

- [1. Purpose and working conventions](#1-purpose-and-working-conventions)
- [2. Derivation and validation workflow](#2-derivation-and-validation-workflow)
- [3. Level 1 — Interval fundamentals](#3-level-1--interval-fundamentals)
- [4. Level 2 — Core interval operations](#4-level-2--core-interval-operations)
- [5. Level 3 — Selection and proof](#5-level-3--selection-and-proof)
- [6. Level 3 — Scheduling and concurrency](#6-level-3--scheduling-and-concurrency)
- [7. Level 4 — Curated practice](#7-level-4--curated-practice)
- [8. Level 5 — Mixed interview challenges](#8-level-5--mixed-interview-challenges)
- [9. Complexity laboratory](#9-complexity-laboratory)
- [10. Debugging laboratory](#10-debugging-laboratory)
- [11. Offline query workshop — optional](#11-offline-query-workshop--optional)
- [12. Mock interviews](#12-mock-interviews)
- [13. Revision and spaced repetition](#13-revision-and-spaced-repetition)
- [14. Completion and readiness](#14-completion-and-readiness)
- [15. References and author validation](#15-references-and-author-validation)

## 1. Purpose and working conventions

The [Phase 4 Intervals roadmap](./DSA_Interview_Roadmap.md#11-intervals) determines scope, E/A/C priorities, and readiness. Follow Understand → Implement → Practice → Explain → Review → Validate. Owning completed documents is not evidence that you can derive a solution under interview conditions.

There are **52 original exercises**: **20 coding tasks** (18 ordinary and two optional partial query helpers), **eight tracing/Java investigations**, **two Greedy proof investigations**, **seven complexity investigations**, **12 debugging challenges**, **one optional offline-query investigation**, and **two mocks**. There are also **six curated platform problems**, in the exact roadmap order. A mock reuses tasks and assesses communication; it does not add another coding problem.

### Prerequisites and entry checks

| Reference | Demonstration before progressing |
| --- | --- |
| [Intervals concepts](./Intervals_Essential_Concepts.md#3-endpoint-conventions-and-empty-ranges) | Explain [3,3] versus [3,3), then one touching pair |
| [Java Arrays](./Java_Array.md#copying-arrays) | Draw outer slots, mutable row objects, and an outer clone |
| [Lists](./Java_List_ArrayList_LinkedList.md#aliases-and-shallow-copies) | Explain what a List<int[]> stores and what conversion copies |
| [Heap / Priority Queue](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#8-java-21-priorityqueue) | Explain minimum-head removal, duplicates, and non-sorted iteration |
| [Heap comparator safety](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#9-comparators-and-numeric-safety) | Produce a failing int-subtraction comparison |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#156-arrayssort) | Include copy, library sort workspace, scan, and output |
| [Roadmap foundations](./DSA_Interview_Roadmap.md#phase-0-foundations) | State an invariant and distinguish a proof from passing examples |

Before Level 2, finish the endpoint and copying diagnostics. Before Level 3, implement a preserving merge and explain its frontier. Before heap tasks, demonstrate peek/poll/offer and count total removals. Before Level 5, hide pattern references and optional hints. Repair a failed prerequisite locally; no Graphs or DP implementation is required here.

### Shared coding contracts

These are part of **every original coding task**, unless a local contract explicitly overrides a rule.

- Use only Java 21 standard-library features, single-threaded completed calls, and feasible allocations. Calls must be independent; no stale static answers. A collection has at most **100000 rows/elements** as a trusted size precondition unless a smaller limit is stated. This is a teaching limit, not a hardware guarantee.
- Endpoints can be any int, including negative values and MIN_VALUE/MAX_VALUE. Rows contain **exactly two endpoints** with start<=end. Required null inputs/rows/elements cause **NPE**; bad row length, reversed endpoints, and checked domain violations cause **IAE**. Do not silently swap endpoints. Validate every required input before a trivial return, even when an early conflict is already known. If independent violations coexist, either relevant exception is acceptable.
- Properties explicitly labelled **trusted preconditions** need not be rescanned. All other stated shape, endpoint, ordering, and value restrictions must be checked. X2 deliberately trusts its interval structure so the target query remains logarithmic.
- Unless stated otherwise, ranges live on the **real-number line**, although endpoints are ints. Closed [s,e] includes both boundaries and a point [t,t]. Half-open [s,e) excludes e; [t,t) is empty. A local contract says whether empties are ignored, rejected, or returned. Touching half-open ranges do not overlap, even when a union policy coalesces them.
- Arbitrary collections can be unsorted, overlapping, nested, and contain duplicate rows unless restricted. Duplicate bookings are distinct demand; duplicate activities are distinct choices. Read-only input row aliasing is allowed. Original order, row identity, and row contents must remain unchanged. Sorting a private copy is allowed; sorting caller input is not.
- Every returned mutable container and interval row is newly allocated and independent of input, other output rows, and results of another call. A new outer array alone is insufficient. Immutable record elements may be shared where explicitly allowed. Exact ordering is stated per task; never normalize a test in a way that hides missing rows, extra duplicates, or broken ownership.
- There are no hidden cleanup gaps, weights, priorities, flexible start times, or room types. X6 explicitly introduces a cleanup period. Selection assumes one resource, fixed positive durations, and equal reward one.
- Auxiliary space excludes input and **required returned output** but includes private copies, temporary lists, event arrays, heap capacity, and object-sort workspace. Let n=interval count, q=query count, m=output rows, p=nonempty bookings, and k=peak active heap size. Add constant setup at n=0; expressions with log(n+1) cover it naturally.
- Widen before arithmetic that can overflow int. Real duration e-s and closed **integer** size e-s+1 are different quantities. Comparators must compare safely rather than subtract keys.

### Starter and attempt conventions

Create a scratch class `IntervalsWorkbook` with `import java.util.*;` outside this guide. **Every Java fence is a member of that class**, in document order. The enum and records are members too. Add your own helpers as needed. TODO exceptions are placeholders, not the required input-error behavior. Tracing specimens and deliberately faulty code are executable; complexity wrappers that call TODO methods need a learner implementation or an explicitly identified test fixture.

Each item starts unchecked. Record last date, result **I/H/S/—**, time including explanation, next review date, and one mistake/lesson. I=independently solved and tested; H=conceptual hint; S=substantial solution review or unfinished; —=unattempted. A checked H/S attempt does not demonstrate independent success.

The [roadmap owns T/I/P/R](./DSA_Interview_Roadmap.md#four-independent-progress-gates). Original histories stay beside their exercises. Curated records below point to the roadmap's canonical per-problem Logs: keep dates and assistance history there, and reference that entry here. Do not maintain a second revision dashboard or duplicate dated histories.

## 2. Derivation and validation workflow

For every coding task, prepare this card before opening hints:

1. Define membership, empty/point behavior, valid input, touching, and ownership.
2. Classify the required result: union, insertion, selected subset, yes/no conflict, current demand, historical peak, timeline, or query answer.
3. Give a simple baseline with complete costs. Repeated all-pairs merging can require repeated rescans; do not call it quadratic without counting them.
4. Use existing sorted/disjoint structure if promised. Otherwise justify a sorting key or explain why sorting adds no value.
5. State the frontier, last-selection, event, heap, or search invariant. Specify what changes on a tie and what remains unchanged when a candidate is skipped.
6. Prove initialization, preservation, termination, and the requested result. For Greedy, exchange into an optimum or provide another valid proof; examples alone are insufficient.
7. Include validation, preparation, sorting, scans, heap operations, output materialization, and maximum live storage.
8. Test ordinary, minimal, duplicate, nested, touching, empty/point, negative, and overflow cases as applicable. Test every promised rejection with one fault at a time.
9. Explain a plausible alternative, its failed assumption or extra cost, and a contract change that would make it useful.

**Shared ownership test:** snapshot outer order, row identities, and values; invoke the method; compare all three. For each returned row, assert it is not any input row or another output row, then edit it and verify input and sibling rows remain unchanged. Repeat calls, including a call after an invalid input. Scalar APIs still need input-preservation tests. F3 applies the same checks to list elements; O2 has a deliberately mutable queue contract.

**Independent oracles for small tests:** compare merged coverage with connected components of the overlap graph; compare a maximum selection count with exhaustive subsets; compare peak counts with direct membership at critical coordinates; compare insertion with a general union oracle; compare a fast point query with a linear scan. Test logical values and ownership separately. Agreement between two implementations that share the same faulty predicate is weak evidence.

### Coverage map

| Roadmap / practical objective | Exercises |
| --- | --- |
| Endpoint inclusion, emptiness, overlap | T1, F1, M2, I2, D1–D2 |
| Safe start/end ordering and Java ownership | T2, F2–F3, J1, D3, D11 |
| Closed merge implementation drill | T3, M1, D4–D5 |
| Three-region insertion | T4, I1–I2, D6 |
| Earliest-finish selection and removals | T5, G1, P1–P2, D7 |
| Free meeting conflict practice | S1, X3 |
| Free sweep/heap concurrency implementation drill | T6–T7, S2–S4, D8–D10 |
| Choosing a simpler method on unfamiliar input | X1–X6, K1–K2 |
| Full time/storage accounting | C1–C7 |
| Optional offline queries | O1–O3, curated L6 |

For each Medium or challenging coding exercise, hints progress from observation to invariant to implementation direction. Opening one before finishing changes an independent attempt to H; copying substantial code changes it to S. References support learning, not a cold attempt.

## 3. Level 1 — Interval fundamentals

Read only the contract first. Complete manual boundary reasoning before coding a predicate.

### T1 — Boundary evidence

**Difficulty:** Easy.

For each row below, write the convention, a mathematical intersection condition, the boolean overlap result, and why strict or non-strict comparisons are justified. Also state the intersection itself. A diagram is evidence only when it labels included endpoints. Reverse A and B and verify symmetry.

| Case | A | B | Required models |
| --- | --- | --- | --- |
| Partial | [1,4] | [3,7] | Closed and half-open |
| Nested | [0,9] | [2,5] | Closed and half-open |
| Identical | [2,6] | [2,6] | Closed and half-open |
| Disjoint | [-4,-2] | [1,3] | Closed and half-open |
| Touching | [1,3] | [3,5] | Closed and half-open |
| Equal starts | [2,4] | [2,8] | Closed and half-open |
| Equal ends | [0,6] | [4,6] | Closed and half-open |
| Point/empty inside | [3,3] | [1,5] | Closed and half-open |
| Point/empty at boundary | [5,5] | [1,5] | Closed and half-open |
| Two equal zero-width rows | [3,3] | [3,3] | Closed and half-open |
| Open interior | (3,4) | (3,4) | Open real intervals |
| Mixed equality | [1,3] | (3,5] | Exactly these brackets |

Schematic test to annotate:

```text
A closed:     1 [-------] 3
B closed:               3 [-------] 5
A half-open:  1 [-------) 3
B half-open:            3 [-------) 5
```

Checkpoint tests: touching is true closed and false half-open; a closed [3,3] overlaps [1,5], while [3,3) overlaps nothing; the open (3,4) intersection is nonempty on a real axis. All first three positive-width pairs overlap under either uniform model. Disjoint and mixed-equality rows do not. Explain every remaining row rather than copying these checkpoints. Reject [5,2] as invalid, not empty. **Completion:** all 12 rows reasoned, with a counterexample to applying the usual two strict cross-inequalities to an empty half-open row.


- [ ] **T1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### F1 — Two-range overlap contract

**Difficulty:** Easy. **Objective:** Translate membership into a symmetric constant-time predicate.

**Prerequisites:** [Overlap derivation](./Intervals_Essential_Concepts.md#4-deriving-overlap-and-disjointness); T1.

**Problem, input, and output:** Return whether rows a and b share any real point. closed=true interprets both as closed; false interprets both as half-open. This is one pair, so no sorting or heap is needed.

**Contract:** Both rows are checked. Equality is a closed point or an empty half-open range. No ordering between the two rows is assumed. Equal rows and all int endpoints are allowed. Touching overlaps only in closed mode. Return one boolean; inputs remain unchanged. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[1,4],[3,7],true]` | `true` | Partial |
| `[[1,3],[3,5],true]` | `true` | Closed touching |
| `[[1,3],[3,5],false]` | `false` | Half-open touching |
| `[[3,3],[1,5],false]` | `false` | Empty inside |
| `[[3,3],[1,5],true]` | `true` | Point inside |
| `[[-4,-2],[1,3],true]` | `false` | Negative disjoint |
| `[[-2147483648,2147483647],[2147483647,2147483647],true]` | `true` | Maximum point |
| `[[5,2],[1,3],true]` | `IAE` | Reversed |
| `[null,[1,3],false]` | `NPE` | Required row |

**Additional edge and ownership checks:** Add identical nonempty ranges, equal starts, equal ends, two empty rows, and row lengths 0/1/3. Verify symmetry without modifying either row.

**Java 21 starter:**

```java
public static boolean overlap(int[] a, int[] b, boolean closed) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO F1");
}
```

**Target costs:** O(1) time, O(1) auxiliary, O(1) output.

**Completion criteria and explanation:** Derive both operators and explain why an empty half-open operand cannot overlap. Do not rely on positive coordinates. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Ask which part of the axis could belong to both ranges.

</details>

<details>
<summary>Hint 2</summary>

Compare the later start with the earlier end, then reason about equality.

</details>

<details>
<summary>Hint 3</summary>

Validate row shape before indexing; make the selected convention control the boundary comparison.

</details>

- [ ] **F1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### T2 — Ordering cards

**Difficulty:** Easy.

Manually order closed rows `[[3,8],[1,6],[3,4],[-2,6],[1,3],[3,4]]` under three keys: start ascending/end ascending; end ascending/start ascending; start descending/end ascending. Mark duplicate row identities A–F before sorting. Identify which ties are fully equal and therefore affected by stability.

Checkpoints: first order begins [-2,6],[1,3],[1,6] and ends [3,8]; the end-first order begins [1,3]; the descending-start order begins the two [3,4] rows and then [3,8]. Derive the complete orders yourself. For every order, decide whether the ordinary merge-frontier scan or earliest-finish proof applies without redesign.

Compare reversing the whole two-key comparator with reversing only its start key. Predict the order of [3,4] and [3,8] in each. Test safe comparison on MIN_VALUE versus 1 in both directions; the subtraction MIN_VALUE-1 wraps to a positive number. Explain int[][] versus int[] comparator overloads and which outer array Arrays.sort changes. **Completion:** all orders, comparator signs, reference identities, and a counterexample to an unsuitable key are explained. Then implement F2.


- [ ] **T2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### F2 — Independent ordered rows

**Difficulty:** Easy. **Objective:** Implement safe multi-key ordering without exposing mutable input rows.

**Prerequisites:** [Sorting](./Intervals_Essential_Concepts.md#5-sorting-intervals-in-java); T2; Java Arrays.

**Problem, input, and output:** Return an independently allocated copy ordered by the specified SortOrder. START_ASC_END_ASC sorts start then end ascending; END_ASC_START_ASC sorts end then start ascending; START_DESC_END_ASC sorts start descending but end ascending on ties.

**Contract:** All rows are valid closed ranges, including point/duplicate ranges. Input may be unsorted and overlapping. Order is exact by the named keys; fully equal rows have indistinguishable values, but output rows must still be independent. Required order is nonnull. No merging; touching remains separate. Use Arrays.sort with a safe Comparator. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[3,8],[1,6],[3,4],[-2,6],[1,3],[3,4]],"START_ASC_END_ASC"]` | `[[-2,6],[1,3],[1,6],[3,4],[3,4],[3,8]]` | Start and tie |
| `[[[3,8],[1,6],[3,4],[-2,6],[1,3],[3,4]],"END_ASC_START_ASC"]` | `[[1,3],[3,4],[3,4],[-2,6],[1,6],[3,8]]` | End and tie |
| `[[[3,8],[1,6],[3,4],[-2,6],[1,3],[3,4]],"START_DESC_END_ASC"]` | `[[3,4],[3,4],[3,8],[1,3],[1,6],[-2,6]]` | Reverse only primary |
| `[[],"END_ASC_START_ASC"]` | `[]` | Empty |
| `[[[2147483647,2147483647],[-2147483648,-2147483648]],"START_ASC_END_ASC"]` | `[[-2147483648,-2147483648],[2147483647,2147483647]]` | Full range |
| `[[[2,1]],"START_ASC_END_ASC"]` | `IAE` | Reversed |
| `[[],null]` | `NPE` | Validate order even when empty |

**Additional edge and ownership checks:** One row; equal ends and starts; two input slots pointing to the same int[]; edit an output row and check both input slots are unchanged.

**Java 21 starter:**

```java
enum SortOrder { START_ASC_END_ASC, END_ASC_START_ASC, START_DESC_END_ASC }

public static int[][] orderedCopy(int[][] intervals, SortOrder order) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO F2");
}
```

**Target costs:** O(n log(n+1)+1) worst-case time; O(n+1) worst-case auxiliary sort storage; O(n+1) required output.

**Completion criteria and explanation:** Explain why reversing an already composed comparator also reverses its secondary key. Separate output rows from sort workspace. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

An outer array and its rows have separate identities.

</details>

<details>
<summary>Hint 2</summary>

Define the primary and tie order independently; reversal placement matters.

</details>

<details>
<summary>Hint 3</summary>

Copy every two-int row before returning it, and use comparingInt or Integer.compare for each key.

</details>

- [ ] **F2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### F3 — A detached interval export

**Difficulty:** Easy. **Objective:** Handle List<int[]> conversion and independent output ownership.

**Prerequisites:** Java Lists and [row ownership](./Intervals_Essential_Concepts.md#14-arrays-references-and-ownership).

**Problem, input, and output:** Given a List<int[]> rows, return an int[][] containing exactly the same endpoint values in the same list order. Use an ArrayList<int[]> as a temporary assembly buffer and convert it with toArray; do not merge or sort.

**Contract:** The list is nonnull and may be any standard List implementation, including LinkedList or a fixed-size view. Its elements are checked closed intervals; duplicates, points, overlaps, and shared row identities are allowed. Return fresh outer storage and one independent row per occurrence. The caller list and rows remain unchanged. Iteration must not assume constant-time indexed access. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[4,6],[1,2],[4,6]]]` | `[[4,6],[1,2],[4,6]]` | Preserve order and duplicates |
| `[[]]` | `[]` | Empty list |
| `[[[3,3]]]` | `[[3,3]]` | Point |
| `[[[1,2],null]]` | `NPE` | Null element |
| `[[[2,1]]]` | `IAE` | Invalid row |
| `[null]` | `NPE` | Null list |

**Additional edge and ownership checks:** Run with ArrayList, LinkedList, Arrays.asList, and the same row object repeated twice. Editing one output row must affect neither its duplicate nor the caller.

**Java 21 starter:**

```java
public static int[][] detachedRows(List<int[]> rows) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO F3");
}
```

**Target costs:** O(n+1) time, O(n+1) auxiliary list-reference buffer, O(n+1) output. Cloned rows eventually returned are output; temporary backing references remain auxiliary.

**Completion criteria and explanation:** Explain why toArray alone cannot isolate row contents and why repeated LinkedList.get(i) would miss the linear target. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Collection conversion copies elements, which are references here.

</details>

<details>
<summary>Hint 2</summary>

Create separate row values before adding them to an owned buffer.

</details>

<details>
<summary>Hint 3</summary>

Use sequential iteration, validate every row, and distinguish the temporary list backing array from the returned outer array.

</details>

- [ ] **F3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 4. Level 2 — Core interval operations

Complete each manual trace before its implementation task. Use [merge theory](./Intervals_Essential_Concepts.md#6-merge-intervals-with-a-covered-frontier) and [three-region insertion](./Intervals_Essential_Concepts.md#7-insert-through-three-ordered-regions) after a genuine attempt.

### T3 — A growing coverage frontier

**Difficulty:** Easy.

Closed input: `[[7,9],[2,6],[1,4],[2,3],[6,7],[11,11],[11,13]]`. First sort by start then end. Fill a row for each processed interval with: current range, frontier before, comparison, chosen action, frontier after, and finalized components. Sketch the covered set before and after a nested row.

```text
Input fragments (schematic):
[1---4] [2-----6] [2-3] [6-7] [7-9]   {11} [11--13]
                                  gap
```

Checkpoints: the sorted first three rows are [1,4],[2,3],[2,6]; the frontier after them is [1,6]. Final output is [[1,9],[11,13]]. The point at 11 joins its closed neighbor. Repeat with [] → [], [[4,4]] → [[4,4]], and [[-4,-1],[-3,2]] → [[-4,2]].

Before implementation, describe a repeated pair-merging baseline and count how many full searches it might repeat. State exactly what makes an old component final after a gap. **Completion:** all seven state rows, including containment and equality, match the covered set without shrinking it.


- [ ] **T3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### M1 — Normalize closed coverage

**Difficulty:** Medium. **Objective:** Implement the roadmap merge drill with a proved frontier and safe output construction.

**Prerequisites:** T3, F1–F3; [closed merging](./Intervals_Essential_Concepts.md#6-merge-intervals-with-a-covered-frontier).

**Problem, input, and output:** Return the union of all input closed real ranges as ascending-by-start, pairwise disjoint closed rows. Combine every overlapping component, including shared boundary points. Preserve every covered point and add none.

**Contract:** Input is arbitrary, not initially sorted or disjoint. start==end is a valid point. Duplicates, nesting, negative/extreme endpoints are allowed. Touching closed rows merge. Empty input returns a fresh empty outer array. All returned rows are independent. Validate the whole input. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[7,9],[2,6],[1,4],[2,3],[6,7],[11,11],[11,13]]]` | `[[1,9],[11,13]]` | Trace fixture |
| `[[]]` | `[]` | Empty |
| `[[[4,4]]]` | `[[4,4]]` | Point singleton |
| `[[[1,2],[4,5]]]` | `[[1,2],[4,5]]` | Disjoint |
| `[[[1,10],[2,3],[4,5]]]` | `[[1,10]]` | Nested frontier |
| `[[[2,4],[2,8],[2,8]]]` | `[[2,8]]` | Equal starts and duplicates |
| `[[[0,6],[4,6]]]` | `[[0,6]]` | Equal ends |
| `[[[1,3],[3,5]]]` | `[[1,5]]` | Touching |
| `[[[-4,-1],[-3,2]]]` | `[[-4,2]]` | Negative |
| `[[[-2147483648,0],[0,2147483647]]]` | `[[-2147483648,2147483647]]` | Full span |
| `[[[0,1],[3,2]]]` | `IAE` | Validate later row |
| `[[[0,1],null]]` | `NPE` | Null later row |

**Additional edge and ownership checks:** Already sorted and reverse-sorted versions must agree. Add many disjoint rows, all identical rows, aliased input rows, and merge the result again to test idempotence with independent containers.

**Java 21 starter:**

```java
public static int[][] mergeClosed(int[][] intervals) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO M1");
}
```

**Target costs:** O(n log(n+1)+1) worst-case time; O(n+1) auxiliary for private preparation, object sorting, and any assembly buffer; O(m+1) output.

**Completion criteria and explanation:** Present baseline, key choice, overlap predicate, processed-prefix union invariant, why only the last component can change, and why max coverage survives containment. Explain the conversion/ownership design. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

A future interval can extend only a component it reaches.

</details>

<details>
<summary>Hint 2</summary>

Choose an order that makes future starts monotone; earlier separated components then become final.

</details>

<details>
<summary>Hint 3</summary>

When combining, retain the farthest reached end, and allocate output rows independently of caller rows.

</details>

- [ ] **M1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### M2 — Two half-open normalization policies

**Difficulty:** Medium. **Objective:** Separate actual overlap from optional coalescing of an adjacent union.

**Prerequisites:** M1; [empty ranges and coalescing](./Intervals_Essential_Concepts.md#3-endpoint-conventions-and-empty-ranges).

**Problem, input, and output:** Normalize half-open real ranges. If coalesceTouching=false, combine exactly the connected components of positive-width overlaps, leaving merely touching components separate. If true, also join adjacent nonempty runs. Discard all empty rows after validation.

**Contract:** Arbitrary unsorted overlapping input; duplicate rows and negative/extreme coordinates allowed. start==end is allowed but contributes nothing. Touching is never overlap; the boolean selects the output-normalization policy. Return nonempty independent rows in start order, with no overlap, and no adjacency when coalescing is enabled. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,3],[3,5]],false]` | `[[1,3],[3,5]]` | Touching separate |
| `[[[1,3],[3,5]],true]` | `[[1,5]]` | Touching coalesced |
| `[[[4,4],[3,6],[1,4]],false]` | `[[1,6]]` | Empty ignored and real overlap |
| `[[[3,3]],true]` | `[]` | Only empty |
| `[[],false]` | `[]` | Empty |
| `[[[2,7],[2,4],[2,7]],false]` | `[[2,7]]` | Containment and duplicate |
| `[[[-4,-2],[-2,0]],false]` | `[[-4,-2],[-2,0]]` | Negative touching |
| `[[[0,0],[8,7]],true]` | `IAE` | Validate before filtering |

**Additional edge and ownership checks:** Test both policies on one nonempty range, disjoint ranges, equal ends, all empty rows, and MIN/MAX coordinates. The covered set must be identical under both policies although output rows can differ.

**Java 21 starter:**

```java
public static int[][] mergeHalfOpen(int[][] intervals, boolean coalesceTouching) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO M2");
}
```

**Target costs:** O(n log(n+1)+1) time, O(n+1) auxiliary, O(m+1) output.

**Completion criteria and explanation:** Give the policy-specific frontier invariant, a touching pair with different output shapes, and an explanation that neither shape changes set membership. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Removing an empty row cannot alter a union.

</details>

<details>
<summary>Hint 2</summary>

Decide separately whether equal boundaries should join in the output.

</details>

<details>
<summary>Hint 3</summary>

Derive the join comparison from the boolean policy; keep the same farthest-end principle as M1.

</details>

- [ ] **M2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### T4 — Three consecutive regions

**Difficulty:** Medium.

Closed existing rows `[[0,1],[3,5],[7,8],[11,14],[18,20]]`, new row `[5,12]`. Label every existing row A (entirely before), B (combined), or C (after). Record the growing frontier after each B row, then count the exact output rows before allocating.

Checkpoints: A has one row; B has three; C has one. Frontier states while combining are [3,12],[3,12],[3,14], and output is [[0,1],[3,14],[18,20]]. Explain why the original end 12 is not the final boundary.

Then reinterpret the same endpoints as half-open with an overlap-only policy: output becomes [[0,1],[3,5],[5,8],[11,14],[18,20]]? Accept or refute this proposed answer by membership and a growing-frontier trace. **Test checkpoint:** the correct half-open result has four rows, and the new component reaches 14; derive all rows yourself. Test an empty insertion [5,5) separately.

**Completion:** justify before/overlap/after comparisons for both models, show why no sort is needed, and explain why multiple linear passes remain linear. Existing touching closed rows violate the disjoint precondition; do not silently normalize them.

<details>
<summary>Hint 1</summary>

Check whether the row ending exactly at the insertion start belongs to the before region.

</details>

<details>
<summary>Hint 2</summary>

Even when that first touching row stays separate, later positive-width overlaps can extend the new end.

</details>

<details>
<summary>Hint 3</summary>

Track the current end, not the original new end; then count prefix, one nonempty merged component, and suffix.

</details>

- [ ] **T4 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### I1 — One new closed range

**Difficulty:** Medium. **Objective:** Use checked sorted/disjoint structure to insert in linear time.

**Prerequisites:** T4 and M1; [three regions](./Intervals_Essential_Concepts.md#7-insert-through-three-ordered-regions).

**Problem, input, and output:** Insert one closed range into an existing closed union. Return the normalized union of existing plus inserted. Do not sort. Use region boundaries and exact final-array allocation to meet the auxiliary target.

**Contract:** Check every existing row and inserted. Existing is already sorted and closed-disjoint, so previous.end<next.start; reject violations with IAE. Points are allowed. Existing duplicate or touching rows therefore violate its contract. inserted may equal, contain, touch, or alias an existing row. Preserve both arguments; return independent start-ordered rows. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[],[2,2]]` | `[[2,2]]` | Empty |
| `[[[3,5],[8,10]],[0,1]]` | `[[0,1],[3,5],[8,10]]` | Before |
| `[[[3,5],[8,10]],[12,14]]` | `[[3,5],[8,10],[12,14]]` | After |
| `[[[1,2],[7,8]],[4,5]]` | `[[1,2],[4,5],[7,8]]` | Between |
| `[[[1,4],[8,10]],[3,6]]` | `[[1,6],[8,10]]` | One overlap |
| `[[[0,1],[3,5],[7,8],[11,14],[18,20]],[5,12]]` | `[[0,1],[3,14],[18,20]]` | Several |
| `[[[1,9]],[3,4]]` | `[[1,9]]` | Contained insertion |
| `[[[1,2],[4,6]],[0,8]]` | `[[0,8]]` | Covers all |
| `[[[1,3],[5,7]],[3,5]]` | `[[1,7]]` | Touch both |
| `[[[2,4],[8,9]],[2,6]]` | `[[2,6],[8,9]]` | Equal start |
| `[[[1,3],[3,5]],[8,9]]` | `IAE` | Existing closed touching |
| `[[[8,9],[1,2]],[4,5]]` | `IAE` | Existing order |
| `[[],[3,2]]` | `IAE` | New reversed |

**Additional edge and ownership checks:** Add negative/extreme ranges, no-overlap insertion, inserted identical by identity to an existing row, and null/bad-shape inserted even when existing is empty.

**Java 21 starter:**

```java
public static int[][] insertClosed(int[][] existing, int[] inserted) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO I1");
}
```

**Target costs:** O(n+1) time; O(1) auxiliary beyond O(m+1) required output, m<=n+1. An intermediate growing list would change the auxiliary bound.

**Completion criteria and explanation:** State each region predicate, prove the middle union stays one range, explain exact sizing and fresh prefix/suffix rows, and count every validation/boundary/copy pass. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

The existing collection already supplies the order and separation a general merge would create.

</details>

<details>
<summary>Hint 2</summary>

Only a consecutive middle region can interact with the insertion.

</details>

<details>
<summary>Hint 3</summary>

Locate boundaries before materializing the result; allocate once and copy surviving rows into their final positions.

</details>

- [ ] **I1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### I2 — An insertion with an empty option

**Difficulty:** Medium. **Objective:** Re-derive all insertion boundaries under half-open semantics.

**Prerequisites:** I1, M2; [changed insertion rules](./Intervals_Essential_Concepts.md#7-insert-through-three-ordered-regions).

**Problem, input, and output:** Insert a half-open range into a sorted, nonoverlapping half-open collection. Combine actual overlaps only; leave merely touching ranges separate. An empty insertion changes no covered range and returns independent copies of existing rows.

**Contract:** Existing rows must be strictly nonempty start<end, sorted by start, and pairwise nonoverlapping, permitting previous.end==next.start; check these conditions. Empty existing collection is allowed. inserted has start<=end and may be empty. Existing duplicates are invalid; inserted may duplicate a row. Do not sort or mutate inputs. Return start-ordered fresh rows and preserve overlap-only boundaries. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,3],[3,5]],[5,7]]` | `[[1,3],[3,5],[5,7]]` | All touching |
| `[[[1,3],[5,7]],[3,5]]` | `[[1,3],[3,5],[5,7]]` | Touch both |
| `[[[1,4],[6,9]],[3,7]]` | `[[1,9]]` | Actual bridge |
| `[[[1,4],[6,9]],[4,4]]` | `[[1,4],[6,9]]` | Empty insertion |
| `[[],[2,2]]` | `[]` | Both empty |
| `[[],[-2,0]]` | `[[-2,0]]` | Nonempty new |
| `[[[0,1],[3,5],[7,8],[11,14],[18,20]],[5,12]]` | `[[0,1],[3,5],[5,14],[18,20]]` | Trace change |
| `[[[1,5]],[2,3]]` | `[[1,5]]` | Contained |
| `[[[1,1]],[2,3]]` | `IAE` | Empty existing forbidden |
| `[[[1,4],[3,5]],[8,9]]` | `IAE` | Existing overlap |

**Additional edge and ownership checks:** Before/after/between insertion; equal starts/ends; alias inserted with an existing row; negative/full-range coordinates. An empty insertion must not bypass full existing validation.

**Java 21 starter:**

```java
public static int[][] insertHalfOpen(int[][] existing, int[] inserted) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO I2");
}
```

**Target costs:** O(n+1) time, O(1) auxiliary with exact allocation and repeated constant-number scans; O(m+1) required output, m<=n+1.

**Completion criteria and explanation:** Explain every changed equality rule relative to I1 and why empty insertion needs a separate case. Prove no merely adjacent run is coalesced. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

An empty insertion contributes no point, regardless of where its endpoints lie.

</details>

<details>
<summary>Hint 2</summary>

With nonempty ranges, equality at a shared boundary belongs to a separated region.

</details>

<details>
<summary>Hint 3</summary>

Handle empty insertion after validation; otherwise derive before, combine, and after boundaries consistently and size the final array.

</details>

- [ ] **I2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### J1 — Which row changed?

**Difficulty:** Medium.

Predict the returned strings without running this specimen. Then execute it, draw the reference graph, and explain the distinction between sorting an outer clone, editing a shared row, replacing a backed-list slot, and editing a copied row.

```java
static List<String> ownershipTrace() {
    int[][] original = {{6, 9}, {1, 3}};
    int[][] shallow = original.clone();
    Arrays.sort(shallow, Comparator.comparingInt(a -> a[0]));
    String first = Arrays.deepToString(original);
    shallow[0][1] = 4;
    List<int[]> buffer = new ArrayList<>(Arrays.asList(shallow));
    int[][] exported = buffer.toArray(int[][]::new);
    exported[1][1] = 10;
    List<int[]> view = Arrays.asList(original);
    view.set(0, new int[]{7, 8});
    int[][] independent = new int[original.length][];
    for (int i = 0; i < original.length; i++) {
        independent[i] = original[i].clone();
    }
    independent[1][1] = 99;
    return List.of(first, Arrays.deepToString(original),
            Arrays.deepToString(exported), Arrays.deepToString(independent),
            Boolean.toString(exported[0] == original[1]),
            Boolean.toString(exported[1] == original[0]));
}
```

Checkpoints: first is "[[6, 9], [1, 3]]"; final original is "[[7, 8], [1, 4]]"; identity booleans are true then false. Derive the other two full strings. The old [6,10] row still exists through exported after original's slot is replaced. What would view.add throw? Would replacing buffer with List.copyOf prevent edits to its int[] elements?

**Tests:** execute the six observations; repeat the call to ensure local state resets. Rebuild the example with one row shared in both original slots and predict how editing versus replacing differs. **Completion:** explain all values and identities, and repair a proposed export that promises independent rows but only calls original.clone().

<details>
<summary>Hint 1</summary>

Follow row objects, not just array indexes.

</details>

<details>
<summary>Hint 2</summary>

A list element replacement and an edit inside an element have different targets.

</details>

<details>
<summary>Hint 3</summary>

Label the two original rows A and B, then introduce a new C only at view.set.

</details>

- [ ] **J1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 5. Level 3 — Selection and proof

The objective changes from preserving all coverage to choosing a maximum compatible subset. Use [selection](./Intervals_Essential_Concepts.md#8-select-compatible-activities) and [proof theory](./Intervals_Essential_Concepts.md#9-prove-the-earliest-finish-choice) after attempting the derivation.

### T5 — A retained schedule

**Difficulty:** Medium.

Positive-duration half-open activities, labelled by input index: `[[0,10],[1,3],[3,5],[2,6],[5,8],[8,9],[7,9]]`. Propose a rule, test it, and produce an end/start/index ordered trace with candidate ID, previous selected end, keep/skip, selected IDs, and new end. Ties use end ascending, then start ascending, then original index ascending.

Checkpoints: the first chosen IDs are 1 and 2. The candidate [2,6) is skipped without moving lastEnd. The final retained IDs are [1,2,4,5]; maximum count is 4 and minimum removals 3. Test [] → zero/zero, [[-5,-2],[-2,0]] → two/zero, and two identical [1,4) rows → one/one. A [3,3) activity is invalid for this selection contract, unlike an ignored empty booking.

**Completion:** all seven candidate transitions and a selection invariant. Compare a closed positive-duration contract where touching conflicts, and identify the boundary change without changing the objective.

<details>
<summary>Hint 1</summary>

Preserving a small release boundary leaves more future candidates feasible.

</details>

<details>
<summary>Hint 2</summary>

The last boundary belongs to a chosen activity, not every scanned candidate.

</details>

<details>
<summary>Hint 3</summary>

Represent the absence of any selection explicitly rather than inventing -1 as an initial timestamp.

</details>

- [ ] **T5 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### G1 — Keep activities and report their identities

**Difficulty:** Medium. **Objective:** Derive earliest-finish selection, preserve identity, and obtain minimum removals from its complement.

**Prerequisites:** T5, safe ordering, P1 proof; [exchange argument](./Intervals_Essential_Concepts.md#9-prove-the-earliest-finish-choice).

**Problem, input, and output:** Return original input indexes of a maximum-cardinality compatible selection. Activities occupy [s,e), so equality between an end and next start permits reuse. For deterministic output, process by end ascending, start ascending, then original index ascending, keeping a candidate whenever it fits after the last chosen activity. Report retained count and minimum removals in your explanation as result.length and n-result.length.

**Contract:** Require start<end for every activity and reject empty rows. Arbitrary order, nesting, duplicates, negative/extreme endpoints allowed. Each occurrence is a separate identity. One resource, fixed times, no weights, travel or setup. Output IDs are unique and in selected chronological order. Caller rows/order are unchanged; a fresh int[] is required. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[0,10],[1,3],[3,5],[2,6],[5,8],[8,9],[7,9]]]` | `[1,2,4,5]` | Identity trace |
| `[[]]` | `[]` | Empty |
| `[[[2,5]]]` | `[0]` | One activity |
| `[[[1,3],[3,5]]]` | `[0,1]` | Touching |
| `[[[1,4],[1,4],[1,4]]]` | `[0]` | Duplicate identities |
| `[[[-5,-2],[-2,0],[0,3]]]` | `[0,1,2]` | Negative chain |
| `[[[0,4],[1,4],[4,6]]]` | `[0,2]` | Equal finish tie |
| `[[[-2147483648,-1],[-1,2147483647]]]` | `[0,1]` | No numeric sentinel |
| `[[[3,3]]]` | `IAE` | Empty rejected |

**Additional edge and ownership checks:** All overlapping; a long early starter versus several short successors; comparator extremes; input aliasing. Compare retained count with exhaustive subsets on small inputs, then separately validate the deterministic chosen IDs.

**Java 21 starter:**

```java
public static int[] selectActivities(int[][] activities) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO G1");
}
```

**Target costs:** O(n log(n+1)+1) time; O(n+1) auxiliary for ordering identities, sort storage, and any selection buffer; O(r+1) output for r selected IDs.

**Completion criteria and explanation:** State the rule before implementing it, supply P1-style exchange reasoning, justify minimum removals=n-r, and construct a counterexample to earliest start. Explain why weighted reward would change the proof obligation. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

The next decision should leave as many possible future starts available as possible.

</details>

<details>
<summary>Hint 2</summary>

Compare the finish of your choice with the first activity of an optimal schedule.

</details>

<details>
<summary>Hint 3</summary>

Carry original indexes through a private ordered representation; update the last selected end only on acceptance.

</details>

- [ ] **G1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### P1 — Exchange into an optimum

**Difficulty:** Medium.

Prove the maximum-count rule under the G1 contract without assuming an optimum already uses your preferred first activity. Name a Greedy first choice G and an optimal first choice O1; state the crucial endpoint inequality, show every follower still fits after replacement, preserve cardinality and distinct identity, then identify the smaller residual problem. Include initialization, induction/iteration, and termination. Explain why tied earliest finishes do not invalidate existence of a Greedy-compatible optimum.

**Proof tests:** on [[0,4],[1,3],[3,6]], the optimum count is 2; show a valid exchange with [1,3). On [[0,2],[1,2],[2,3]], the optimum count is also 2 despite a tied first finish. On [] the count is 0. Explain why a selected first activity cannot also be a later positive-duration follower in the same chronological schedule.

**Completion:** each claim names its assumption. "It leaves more room" without a comparison and replacement construction is incomplete. Show why n minus maximum kept is minimum unweighted removals. Then explain which step stops proving optimal total reward if activities carry unequal weights.

<details>
<summary>Hint 1</summary>

Start with an arbitrary optimum, not with your own chosen schedule.

</details>

<details>
<summary>Hint 2</summary>

Compare release times and keep the optimum's followers unchanged.

</details>

<details>
<summary>Hint 3</summary>

After replacing just one activity, express the remaining input as the same problem constrained to starts after that release.

</details>

- [ ] **P1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### P2 — Tempting rules under examination

**Difficulty:** Medium.

For each fully specified rule, construct a small counterexample, trace the rule, exhibit a better feasible subset, and name the failed reasoning. Do not treat passing random tests as a proof.

| Proposed rule | Reproducible test seed | Required comparison |
| --- | --- | --- |
| Take earliest start, then repeatedly take earliest start compatible after it | [0,9),[1,3),[3,5),[5,7) | Rule keeps 1; feasible optimum keeps 3 |
| Choose globally shortest duration, discard all conflicts, repeat on either side | [0,5),[4,6),[5,10) | Unique shortest blocks both neighbors; 1 versus 2 |
| Select earliest finish to maximize reward | [0,2) reward 1, [2,4) reward 1, [0,4) reward 9 | Reward 2 versus 9 |
| In G1, assign lastEnd after every scanned row, even when skipped | [1,3),[0,4),[3,5) | Rule keeps 1; optimum keeps 2 |

All ranges have positive duration and touching is compatible. For each row, create another example using negative endpoints or a tie while retaining the failure. Distinguish an invalid forward latest-finish rule from a separately proved backward latest-start rule; the direction and compatibility condition are part of an algorithm.

**Completion:** four explained witnesses and controls where each bad rule happens to agree, plus one sentence identifying when selection is unnecessary because every booking must be retained. No implementation of weighted scheduling is required.

<details>
<summary>Hint 1</summary>

Fix the entire proposed policy before evaluating its first choice.

</details>

<details>
<summary>Hint 2</summary>

Look for one choice that blocks two consecutive compatible alternatives.

</details>

<details>
<summary>Hint 3</summary>

Keep the examples small enough to enumerate all subsets and certify the optimum.

</details>

- [ ] **P2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 6. Level 3 — Scheduling and concurrency

S1 is free practice for the core Meeting Rooms skill; S2 and S3 cover the Meeting Rooms II mechanics without Premium access. Bookings must all be retained. Selection and merging cannot substitute for multiplicity-aware demand.

### S1 — Can one room host every booking?

**Difficulty:** Easy. **Objective:** Detect a conflict and justify why sorted adjacent checks suffice.

**Prerequisites:** F1/F2; [meeting conflicts](./Intervals_Essential_Concepts.md#10-detect-meeting-conflicts).

**Problem, input, and output:** Return whether every half-open booking can be attended by one person. Empty bookings consume no time and are ignored after complete validation. Provide a pairwise baseline, then an ordering-based implementation.

**Contract:** Unsorted arbitrary rows with start<=end; duplicates count as separate bookings. Touching nonempty bookings are compatible. Only empty input or only empty bookings returns true. No setup gaps. Input is preserved and all rows checked before returning, even if an earlier pair conflicts. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[8,10],[1,4],[4,7],[5,5]]]` | `true` | Touching and empty |
| `[[[6,8],[1,4],[4,7],[5,5]]]` | `false` | One conflict |
| `[[[1,9],[2,3],[4,5]]]` | `false` | Nested |
| `[[[2,6],[2,6]]]` | `false` | Duplicate |
| `[[[1,4],[1,2]]]` | `false` | Equal start |
| `[[[3,3]]]` | `true` | Empty booking |
| `[[]]` | `true` | Empty input |
| `[[[-4,-2],[-2,0]]]` | `true` | Negative touching |
| `[[[1,5],[2,6],[9,8]]]` | `IAE` | Later invalid not masked by conflict |
| `[[[1,5],[2,6],null]]` | `NPE` | Later null |

**Additional edge and ownership checks:** One nonempty range; all disjoint; all overlapping; MIN/MAX with an empty boundary booking. Add a sorted fixture and explain what work could be skipped under a trusted sorted contract.

**Java 21 starter:**

```java
public static boolean canAttendAll(int[][] bookings) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO S1");
}
```

**Target costs:** O(n log(n+1)+1) worst-case time, O(n+1) auxiliary for preserving preparation and object sorting; O(1) output.

**Completion criteria and explanation:** Prove that no adjacent conflict in start order implies no conflict anywhere. Explain why a previous-row end is sufficient for this yes/no early detector but not a general coverage frontier. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

The answer only asks whether one conflict exists.

</details>

<details>
<summary>Hint 2</summary>

After start ordering, an overlap cannot hide beyond every adjacent pair.

</details>

<details>
<summary>Hint 3</summary>

Remove empty bookings logically after validation; make equality follow the half-open membership rule.

</details>

- [ ] **S1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### T6 — Simultaneous endpoint events

**Difficulty:** Medium.

Use bookings `[[1,4],[2,6],[4,5],[4,7],[6,8],[8,10],[5,5]]`. Construct events only for nonempty half-open rows; choose the tie order from membership, not memory. Fill time, delta, count before, count after, and historical maximum for every event. Give the count after each complete timestamp group too.

```text
At time 4:
A ends:       [1----4)
C begins:          [4--5)
D begins:          [4------7)
B continues:    [2------6)
```

Checkpoints: 12 events; after all events at times 1,2,4,5,6,7,8,10, counts are [1,2,3,2,2,1,1,0]; half-open peak is 3. At a tied time, intermediate event states need not equal the final physical occupancy at that time. Explain why the correct tie order cannot invent a larger peak.

Reinterpret all seven rows as closed ranges: [5,5] is now a point, there are 14 events, and the peak is 4. Also test closed [[1,3],[3,5]] → 2 versus half-open → 1, and one [3,3] → 1 closed versus 0 half-open. Explain why keeping only a net delta per timestamp is insufficient for closed point peaks.

**Completion:** both event orders and complete half-open table, explicit initial/final state, and a proof that maximum demand is visible at the chosen event phases.

<details>
<summary>Hint 1</summary>

Ask whether an interval ending at t still contains t.

</details>

<details>
<summary>Hint 2</summary>

Empty half-open rows cannot contribute demand events.

</details>

<details>
<summary>Hint 3</summary>

For a closed sweep, identify the state after starts but before ends; for half-open, identify the state after the whole timestamp group.

</details>

- [ ] **T6 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### S2 — Peak demand under an explicit convention

**Difficulty:** Medium. **Objective:** Implement event-based concurrency with convention-specific tie handling.

**Prerequisites:** T6; [event sweeps](./Intervals_Essential_Concepts.md#11-sweep-through-endpoint-events).

**Problem, input, and output:** Return maximum simultaneous interval demand. closed=false models half-open bookings; closed=true models closed ranges with point occupancy. Each row is one demand unit. Use a sorted endpoint-event representation and maintain a historical maximum.

**Contract:** Arbitrary unsorted/overlapping duplicates allowed. Validate all start<=end rows. In half-open mode ignore empties; in closed mode retain points. Touching conflicts only in closed mode. Counts fit int under n<=100000; 2*n event count also fits. Preserve inputs. No assignment of room identities is requested. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,4],[2,6],[4,5],[4,7],[6,8],[8,10],[5,5]],false]` | `3` | Half-open trace |
| `[[[1,4],[2,6],[4,5],[4,7],[6,8],[8,10],[5,5]],true]` | `4` | Closed trace |
| `[[[1,3],[3,5]],false]` | `1` | End/start tie |
| `[[[1,3],[3,5]],true]` | `2` | Closed tie |
| `[[[3,3]],false]` | `0` | Empty |
| `[[[3,3]],true]` | `1` | Point |
| `[[],true]` | `0` | No ranges |
| `[[[1,6],[1,6],[2,5]],false]` | `3` | All overlap |
| `[[[-2147483648,0],[0,2147483647]],false]` | `1` | Extreme touching |
| `[[[1,1],[8,7]],false]` | `IAE` | Validate before ignoring |

**Additional edge and ownership checks:** Equal starts, equal ends, repeated points, all disjoint, only empty bookings, and a null later row. Compare each result with direct membership counts at all starts, and both modes with one endpoint flip.

**Java 21 starter:**

```java
public static int peakSweep(int[][] intervals, boolean closed) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO S2");
}
```

**Target costs:** O(n log(n+1)+1) time, O(n+1) auxiliary event/sort storage; O(1) output. Explain p nonempty half-open rows versus n closed rows.

**Completion criteria and explanation:** Define event representation, initial active=0, tie order, meaning of intermediate states, peak update phase, termination, and why merging duplicates would lose demand. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Demand changes only when crossing a boundary or visiting an included endpoint.

</details>

<details>
<summary>Hint 2</summary>

Equal-coordinate events must expose the correct physical peak for the chosen model.

</details>

<details>
<summary>Hint 3</summary>

Use safe time comparisons and a secondary event-kind rule; never implement closed inclusion by adding one to int endpoints.

</details>

- [ ] **S2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### T7 — What remains active?

**Difficulty:** Medium.

Trace a true active-end min-heap on the six nonempty T6 bookings. For every start, record sorted current row, ends before cleanup, all expired ends, ends after offer, current size, and historical maximum. Display heaps as sorted **multisets for explanation**, not as a claim about Java iteration.

Checkpoints: before [6,8), the multiset is [5,6,7]; cleanup removes [5,6]; after offering 8 it is [7,8]. Before [8,10), remove [7,8]; final heap is [10], while historical peak remains 3. Exactly six offers and five polls occur. Also trace [[1,2],[1,10],[3,4]] → peak 2, and two [2,5) bookings → peak 2.

Explain what every end represents before and after cleanup; why the smallest end is decisive; why one or several polls can occur; and why no discarded end can matter at a later sorted start. Compare with a different heap holding one latest end per **allocated room**. A one-poll-per-arrival policy can be correct under that allocated-room invariant; it does not claim to store exactly the active bookings. Do not label it incorrect just because it polls once.

**Completion:** all six states and lower-bound/achievability reasoning for minimum rooms=peak. Explain why final active size is a different statistic.

<details>
<summary>Hint 1</summary>

A booking contributes only while its half-open membership test holds.

</details>

<details>
<summary>Hint 2</summary>

If the minimum end has not expired, no stored end has expired.

</details>

<details>
<summary>Hint 3</summary>

Count total polls by charging each to a previous offer, and retain the largest size seen rather than just the last size.

</details>

- [ ] **T7 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### S3 — Minimum rooms from current active bookings

**Difficulty:** Medium. **Objective:** Use a min-heap with a precise active-set invariant and explain resource optimality.

**Prerequisites:** T7; [active-end heap](./Intervals_Essential_Concepts.md#12-track-active-bookings-with-a-min-heap); Heap concepts.

**Problem, input, and output:** Return the minimum number of identical rooms needed to retain all half-open bookings. Implement a true active-end heap: before admitting each nonempty booking, remove every expired previously admitted booking. Track peak active occupancy.

**Contract:** Unsorted start<=end rows, including duplicates, negatives, and empty bookings. Validate all, then ignore empties. A room is reusable exactly at an end/start tie. Preserve inputs. Empty-only input needs zero rooms. No room IDs or mutable booking objects are returned. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,4],[2,6],[4,5],[4,7],[6,8],[8,10]]]` | `3` | Earlier peak exceeds final size |
| `[[[1,2],[1,10],[3,4]]]` | `2` | Minimum direction |
| `[[[1,3],[3,5]]]` | `1` | Reuse at equality |
| `[[[2,5],[2,5]]]` | `2` | Duplicate demand |
| `[[[1,9],[2,8],[3,7]]]` | `3` | All overlap |
| `[[[1,1],[2,2]]]` | `0` | Only empty |
| `[[]]` | `0` | No bookings |
| `[[[-5,-2],[-2,0],[0,3]]]` | `1` | Negative chain |
| `[[[1,2],[7,6]]]` | `IAE` | Later invalid |

**Additional edge and ownership checks:** Several expirations at one arrival, tied starts with different ends, a long booking hiding short expired ones, aliased input rows, and MIN/MAX. Compare with S2(false) and an independent direct-count oracle.

**Java 21 starter:**

```java
public static int minimumRooms(int[][] bookings) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO S3");
}
```

**Target costs:** O(n+p log(p+1)+p log(k+1)+1), hence O(n log(n+1)+1), total time; O(n+k+1)=O(n+1) auxiliary with preserving order preparation; O(1) output. Heap backing capacity follows peak occupancy, not final size.

**Completion criteria and explanation:** State the active-set invariant, sorted admission reason, equality expiry rule, why all expired ends leave, and why peak is both a lower bound and achievable room count. Compare the event representation with the heap representation. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

At a new start, earlier bookings matter only if they still occupy a room.

</details>

<details>
<summary>Hint 2</summary>

Keep the next release visible and repeat cleanup until that release is in the future.

</details>

<details>
<summary>Hint 3</summary>

Filter empty bookings after validation, sort a private representation by start, and maintain a separate historical maximum.

</details>

- [ ] **S3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### S4 — A demand-change report

**Difficulty:** Medium. **Objective:** Group tied events and report actual changes rather than fabricated intermediate states.

**Prerequisites:** T6 and S2; [grouped event meaning](./Intervals_Essential_Concepts.md#11-sweep-through-endpoint-events).

**Problem, input, and output:** For half-open bookings return rows [time,activeAfterTime] for every time at which total demand changes. Group all simultaneous starts/ends; omit times whose net effect is zero. Include the final change back to zero. Return rows in strictly increasing time order.

**Contract:** Input booking rows follow the checked half-open contract, including ignored empties and duplicate demand. Output rows are **time/count records**, not intervals: their first field may exceed their second. Count is nonnegative; at most 2*p rows. Output containers are independent. No leading artificial [time,0] row; empty-only input returns []. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,4],[2,6],[4,5],[4,7],[6,8],[8,10],[5,5]]]` | `[[1,1],[2,2],[4,3],[5,2],[7,1],[10,0]]` | Omit zero-net times 6 and 8 |
| `[[[1,3],[3,5]]]` | `[[1,1],[5,0]]` | Unchanged demand at 3 |
| `[[[2,4],[2,4]]]` | `[[2,2],[4,0]]` | Multiplicity |
| `[[[3,3]]]` | `[]` | Empty booking |
| `[[]]` | `[]` | No changes |
| `[[[-2,0],[0,1]]]` | `[[-2,1],[1,0]]` | Negative timeline |
| `[[[4,2]]]` | `IAE` | Invalid interval |

**Additional edge and ownership checks:** Disjoint bookings must include intermediate zero states; repeated equal starts and ends; MIN/MAX timestamps. Reconstruct demand on every gap from the report and compare with direct membership. Check changing one returned count cannot affect any input.

**Java 21 starter:**

```java
public static int[][] demandChanges(int[][] bookings) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO S4");
}
```

**Target costs:** O(n log(n+1)+1) time; O(n+1) auxiliary events/sort/assembly; O(m+1) output with m<=2*p.

**Completion criteria and explanation:** Explain why a half-open grouped net change is sufficient, why closed point demand needs different phases, and how final zero differs from an initially empty result. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Several event deltas at one coordinate describe one final demand state there.

</details>

<details>
<summary>Hint 2</summary>

The report records changes in the aggregate count, not changes in booking identity.

</details>

<details>
<summary>Hint 3</summary>

Compare the count before and after a whole timestamp group, appending only when those values differ.

</details>

- [ ] **S4 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 7. Level 4 — Curated practice

The following six problems preserve the [roadmap order and classifications](./DSA_Interview_Roadmap.md#11-intervals). E/A/C is learning priority; Easy/Medium/Hard is platform difficulty. Read the official statement for its exact constraints and signature. Local exercise validation and mutation contracts do not silently become platform requirements. These summaries are objectives, not copied problem statements or solutions.

### L1 — Merge Intervals

**Official URL:** [Merge Intervals](https://leetcode.com/problems/merge-intervals/). **Priority:** E. **Difficulty:** Medium.

**Objective:** Preserve union coverage under sorting and nested boundaries. **Prerequisites:** M1 and T3. **Skills assessed:** Derive the overlap rule, prove the frontier, test containment/equal starts/points, and account for copying.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L1 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Merge Intervals”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

### L2 — Insert Interval

**Official URL:** [Insert Interval](https://leetcode.com/problems/insert-interval/). **Priority:** E. **Difficulty:** Medium.

**Objective:** Exploit already sorted/disjoint input. **Prerequisites:** I1 and T4. **Skills assessed:** Derive all three regions, test before/after/between/touching, and explain the linear bound.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L2 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Insert Interval”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

### L3 — Non-overlapping Intervals

**Official URL:** [Non-overlapping Intervals](https://leetcode.com/problems/non-overlapping-intervals/). **Priority:** E. **Difficulty:** Medium.

**Objective:** Turn maximum compatible count into minimum removals. **Prerequisites:** G1, P1, P2. **Skills assessed:** Prove the selection rule, respect the statement's touching-compatible rule, and test ties and nested activities.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L3 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Non-overlapping Intervals”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

### L4 — Meeting Rooms

**Official URL:** [Meeting Rooms](https://leetcode.com/problems/meeting-rooms/). **Priority:** A. **Difficulty:** Easy. **LeetCode Premium**, as marked in the roadmap.

**Objective:** Recognize whether one resource can attend all bookings. **Prerequisites:** S1 and F1. **Skills assessed:** Explain sorted adjacent sufficiency and contract-sensitive endpoints; S1 is the free equivalent.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L4 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Meeting Rooms”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

### L5 — Meeting Rooms II

**Official URL:** [Meeting Rooms II](https://leetcode.com/problems/meeting-rooms-ii/). **Priority:** A. **Difficulty:** Medium. **LeetCode Premium**, as marked in the roadmap.

**Objective:** Count simultaneous resource demand. **Prerequisites:** S2, S3, T6, T7. **Skills assessed:** Compare event ties with heap expiry, preserve duplicates, and prove minimum resources=peak; S2/S3 are free equivalents.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L5 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Meeting Rooms II”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

### L6 — Minimum Interval to Include Each Query

**Official URL:** [Minimum Interval to Include Each Query](https://leetcode.com/problems/minimum-interval-to-include-each-query/). **Priority:** C. **Difficulty:** Hard. **Optional advanced; does not block ordinary readiness.**

**Objective:** Investigate optional offline query ordering and minimum eligible candidates. **Prerequisites:** O1–O3, safe long arithmetic, heap ordering. **Skills assessed:** Preserve duplicate query positions, distinguish eligibility from validity, and analyze n and q separately.

**Attempt procedure:** work from the statement alone, record a baseline, derive the invariant, implement in Java 21, test an adversarial boundary, and explain costs. If you use a hint, record H; substantial solution review or an unfinished attempt is S. A later cold solve can establish I.

- [ ] **L6 attempt pointer** — Canonical record: [roadmap Intervals Log](./DSA_Interview_Roadmap.md#11-intervals), “Minimum Interval to Include Each Query”. Last / Result / Minutes / Next review / Mistake: — (record values once there; link or identify that entry here).

Premium access is **not required**. Use S1 for conflict detection and S2/S3 for concurrency evidence, then decide whether paid Additional practice is useful. The Hard problem and O1–O3 are optional extensions. Ordinary readiness still requires the free scheduling concepts, independent Essential solves, and unfamiliar transfer.

## 8. Level 5 — Mixed interview challenges

Hide hints and theory references. For each task decide independently whether sorting is needed, which boundary matters, what is already ordered/disjoint, whether a heap adds useful state, and whether the goal is union, selection, concurrency, or another previously learned operation. The titles deliberately do not name the method. Explain a simpler baseline before optimizing.

### X1 — A machine usage invoice

**Difficulty:** Medium. **Objective:** Compute occupied duration without double-charging overlaps.

**Prerequisites:** Core endpoint reasoning and a completed Level 2 task; identify the relevant reference after attempting.

**Problem, input, and output:** A machine is billed once per unit of real time during which at least one usage record is active. Return the total duration covered by all half-open records, counting overlap only once. No rate multiplier or rounding is applied.

**Contract:** Unsorted overlapping start<=end rows; empties are accepted and contribute zero. Duplicates do not increase billed coverage. Touching records add contiguous duration without simultaneous overlap. All int endpoints, no mutation, scalar long result; total union duration is at most 4294967295 under this axis. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[5,9],[1,4],[3,7]]]` | `8` | Shared coverage once |
| `[[[1,3],[3,5]]]` | `4` | Adjacent |
| `[[[1,2],[4,6]]]` | `3` | Gap not billed |
| `[[[2,8],[3,4],[2,8]]]` | `6` | Nested/duplicate |
| `[[[3,3]]]` | `0` | Empty |
| `[[]]` | `0` | No usage |
| `[[[-2147483648,2147483647]]]` | `4294967295` | Widened duration |
| `[[[4,1]]]` | `IAE` | Invalid |

**Additional edge and ownership checks:** One interval; negatives; sorted and shuffled copies; repeated aliases. Compare with a small-domain oracle that measures covered gaps between distinct endpoints. Never enumerate the entire full-int domain.

**Java 21 starter:**

```java
public static long billedDuration(int[][] usage) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X1");
}
```

**Target costs:** O(n log(n+1)+1) worst-case time; O(n+1) auxiliary under input preservation; O(1) output. Derive why long is sufficient.

**Completion criteria and explanation:** Identify what should be counted once versus with multiplicity, explain whether boundary points affect real duration, and defend the chosen state without an unnecessary resource-count structure. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

An occupied point contributes once even if several records contain it.

</details>

<details>
<summary>Hint 2</summary>

An ordered covered frontier can summarize what has already been billed.

</details>

<details>
<summary>Hint 3</summary>

Accumulate lengths only for completed coverage components; promote before subtraction.

</details>

- [ ] **X1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### X2 — An approved coordinate

**Difficulty:** Medium. **Objective:** Use a strong input precondition without adding unnecessary preparation.

**Prerequisites:** Earlier Binary Search fundamentals and interval membership; hide pattern labels on the first attempt.

**Problem, input, and output:** Return whether coordinate point belongs to any approved closed range. The data is a trusted prevalidated catalogue, reused by many independent calls. Do not modify it or build a new index per query.

**Contract:** Required outer array is checked nonnull. Row shape/non-nullness/start<=end, start order, and closed disjointness are **trusted preconditions** and need not be rescanned. Thus previous.end<next.start. Points allowed, duplicates disallowed by disjointness, negative/full-int endpoints allowed. An empty catalogue returns false. Query is any int. Touching catalogue rows are outside the contract; a query equal to a stored boundary is included. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,3],[6,8],[10,10]],3]` | `true` | Included end |
| `[[[1,3],[6,8],[10,10]],4]` | `false` | Gap |
| `[[[1,3],[6,8],[10,10]],10]` | `true` | Point range |
| `[[[1,3]],0]` | `false` | Before |
| `[[[1,3]],4]` | `false` | After |
| `[[],2]` | `false` | Empty |
| `[[[-2147483648,-1],[1,2147483647]],2147483647]` | `true` | Extreme |
| `[null,0]` | `NPE` | Outer null |

**Additional edge and ownership checks:** Query at every start/end and just within a safe neighboring gap. Do not add invalid-row rejection tests because rows are trusted here. Explain the separate O(n) cost if the producer had to validate the catalogue first.

**Java 21 starter:**

```java
public static boolean approved(int[][] catalogue, int point) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X2");
}
```

**Target costs:** O(log(n+1)+1) time and O(1) auxiliary/output per query, including the O(1) outer null check. A full validation scan would violate this per-query target.

**Completion criteria and explanation:** State a shrinking search invariant, identify why at most one catalogue range can contain the point, and explain why a per-call sort or heap would waste existing structure. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Only one disjoint catalogue range can contain the coordinate.

</details>

<details>
<summary>Hint 2</summary>

The start boundary gives a monotone fact about which ranges remain possible.

</details>

<details>
<summary>Hint 3</summary>

Keep a bounded candidate region and check endpoint inclusion on the surviving candidate; avoid point+1 arithmetic.

</details>

- [ ] **X2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### X3 — Staff on duty at one instant

**Difficulty:** Easy. **Objective:** Choose the work needed for one query rather than solving a larger timeline problem.

**Prerequisites:** F1 and half-open membership.

**Problem, input, and output:** Return how many half-open duty records contain the given time. Each record is a distinct worker assignment. Only this single instant is queried; do not return the all-time maximum.

**Contract:** Arbitrary unsorted start<=end rows, duplicates allowed and counted separately. Empty rows contain no instant. A shift ending at time is absent, one beginning at time is present. Full int domain, no mutation, fully checked input, scalar int output. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,4],[2,6],[4,7],[4,4]],4]` | `2` | Release and arrival |
| `[[[1,3],[3,5]],3]` | `1` | Touching |
| `[[[2,5],[2,5]],2]` | `2` | Duplicate workers |
| `[[[3,3]],3]` | `0` | Empty |
| `[[],4]` | `0` | No staff |
| `[[[-2147483648,2147483647]],2147483647]` | `0` | Excluded largest end |
| `[[[-2147483648,2147483647]],-2147483648]` | `1` | Included smallest start |
| `[[[1,4],[6,5]],2]` | `IAE` | Validate later row |

**Additional edge and ownership checks:** One range before/inside/after; negative time; all rows contain time versus none. Check the answer against a direct per-row truth table and preserve input identities.

**Java 21 starter:**

```java
public static int staffAt(int[][] shifts, int time) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X3");
}
```

**Target costs:** O(n+1) time, O(1) auxiliary, O(1) output.

**Completion criteria and explanation:** Justify the simplest sufficient approach and explain why global sorting, Greedy selection, and a heap are unnecessary for one count at one instant. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Every record can answer its own membership question independently.

</details>

<details>
<summary>Hint 2</summary>

Retain multiplicity while combining the per-row decisions.

</details>

<details>
<summary>Hint 3</summary>

Read each row once, validate it, and count only membership under the half-open convention.

</details>

- [ ] **X3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### X4 — Open laboratory windows

**Difficulty:** Medium. **Objective:** Use ordered data to describe the uncovered part of a fixed window.

**Prerequisites:** Endpoint reasoning and ordered scans; choose the approach before reading hints.

**Problem, input, and output:** Given a half-open laboratory opening window [open,close) and reserved periods, return every maximal nonempty available subrange inside that window. Reservations can extend partly or wholly outside the window. Return start-ordered fresh rows.

**Contract:** Require open<close, otherwise IAE. Reservations must be strictly nonempty, sorted, and pairwise nonoverlapping under half-open semantics; previous.end<=next.start is allowed. Check all rows and ordering. Duplicates/empty reservations are therefore invalid. Negative/extreme endpoints allowed. Preserve inputs. Touching reservations leave no positive-duration available gap. Returned availability may touch a reservation boundary but must not overlap it. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,3],[5,7]],0,10]` | `[[0,1],[3,5],[7,10]]` | Three gaps |
| `[[[1,3],[3,5]],0,6]` | `[[0,1],[5,6]]` | No artificial touching gap |
| `[[[-5,2],[4,12]],0,10]` | `[[2,4]]` | Clip to window |
| `[[],2,7]` | `[[2,7]]` | Fully available |
| `[[[0,10]],2,7]` | `[]` | Fully reserved |
| `[[[-5,-1],[11,12]],0,10]` | `[[0,10]]` | Outside reservations |
| `[[],3,3]` | `IAE` | Empty opening window |
| `[[[1,1]],0,4]` | `IAE` | Empty reservation |
| `[[[1,4],[3,6]],0,8]` | `IAE` | Overlapping reservations |

**Additional edge and ownership checks:** Reservations ending at open or starting at close; entire MIN..MAX window; final gap absent; one small middle reservation. Verify sorted maximal gaps and equality of covered-plus-available portions within the window.

**Java 21 starter:**

```java
public static int[][] openWindows(int[][] reserved, int open, int close) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X4");
}
```

**Target costs:** O(n+1) time; O(1) auxiliary with a counting pass and exact output allocation, or O(n+1) auxiliary with an explicitly justified temporary buffer; O(m+1) output. Choose and defend one target.

**Completion criteria and explanation:** Explain why existing order removes preparation, how outside reservations affect state, why gaps are maximal, and why points alone are not available-duration intervals. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Only the part inside the laboratory opening window matters.

</details>

<details>
<summary>Hint 2</summary>

An advancing boundary can separate already classified time from still-unclassified time.

</details>

<details>
<summary>Hint 3</summary>

Clip relevant boundaries to the window, emit only positive gaps, and remember the possible final suffix.

</details>

- [ ] **X4 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### X5 — Readings for requested spans

**Difficulty:** Medium. **Objective:** Recognize that endpoint notation can describe an aggregate over array positions.

**Prerequisites:** Arrays & Hashing range-query fundamentals and long arithmetic.

**Problem, input, and output:** Given int readings and q index requests [from,to), return the sum of readings at indexes from through to-1 for each request, preserving request order. Each query is independent, even when its positions overlap another query.

**Contract:** Let n=readings.length and q=request count, each <=100000 size precondition. Request rows must be nonnull length 2 with 0<=from<=to<=n; check bounds. Empty requests return zero. Requests may be unsorted/overlapping/duplicate. Reading values can be any int, including negatives; endpoints are indexes rather than real times. Preserve both inputs, return a fresh long[q]. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[4,-2,7,1],[[0,4],[1,3],[2,2],[0,1]]]` | `[10,5,0,4]` | Mixed spans |
| `[[],[[0,0]]]` | `[0]` | Empty readings |
| `[[3,5],[]]` | `[]` | No queries |
| `[[2147483647,2147483647],[[0,2],[0,2]]]` | `[4294967294,4294967294]` | Large duplicate sum |
| `[[-2147483648,-2147483648],[[0,2]]]` | `[-4294967296]` | Negative sum |
| `[[1,2],[[0,3]]]` | `IAE` | Out of bounds |
| `[[1,2],[[1,0]]]` | `IAE` | Reversed indexes |
| `[null,[]]` | `NPE` | Required readings |

**Additional edge and ownership checks:** First/last singleton requests, every possible small span, repeated query row identity, null/length-3 request, and a later invalid query. Compare with a direct summation oracle; no query sorting is required.

**Java 21 starter:**

```java
public static long[] spanTotals(int[] readings, int[][] requests) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X5");
}
```

**Target costs:** O(n+q+1) time, O(n+1) auxiliary, O(q+1) output. Use separate n and q; explain the maximum possible absolute accumulated sum.

**Completion criteria and explanation:** Identify the requested quantity before choosing a range algorithm, explain which previous prefix technique fits, and why merging query ranges would lose individual answers. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Each output is an aggregate of array entries, not a covered real set.

</details>

<details>
<summary>Hint 2</summary>

Repeated independent queries can reuse preparation over the same reading sequence.

</details>

<details>
<summary>Hint 3</summary>

Store widened cumulative totals with an empty-prefix slot, then derive each half-open request from two positions.

</details>

- [ ] **X5 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### X6 — Vehicles awaiting service

**Difficulty:** Medium. **Objective:** Adapt resource availability to an explicit extra release constraint and widened time.

**Prerequisites:** Core scheduling and safe arithmetic; select the model independently first.

**Problem, input, and output:** Each nonempty trip [s,e) uses one vehicle. After arriving at e, that vehicle requires gap units of service and is reusable at e+gap. Return the minimum fleet size to operate every trip. All vehicles are identical; service uses that same vehicle but no separate shared workshop resource.

**Contract:** Trip rows checked start<=end; empty trips are ignored entirely and require no service. gap is an int with gap>=0, checked even for no trips. Original endpoints are any int, so effective releases can exceed MAX_VALUE and must be modeled in long. Duplicate nonempty trips are separate demand. Trips may be unsorted/overlapping; preserve inputs. Availability at exact release permits reuse. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[1,3],[3,5]],0]` | `1` | No gap |
| `[[[1,3],[3,5]],1]` | `2` | Service prevents immediate reuse |
| `[[[1,3],[4,6]],1]` | `1` | Exact service release |
| `[[[1,1],[2,2]],9]` | `0` | Empty trips ignored |
| `[[],0]` | `0` | No trips |
| `[[[2147483645,2147483647],[2147483646,2147483647]],10]` | `2` | Release beyond int |
| `[[[-5,-3],[-2,0]],1]` | `1` | Negative time |
| `[[],-1]` | `IAE` | Invalid gap |
| `[[[4,3]],1]` | `IAE` | Invalid trip |

**Additional edge and ownership checks:** All overlapping; several releases at one start; gap=MAX_VALUE; one nonempty trip plus many empties; compare gap=0 with S3. Explain bounds on a widened release and why truncating it is incorrect.

**Java 21 starter:**

```java
public static int fleetSize(int[][] trips, int gap) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO X6");
}
```

**Target costs:** O(n log(n+1)+1) time and O(n+1) auxiliary under preserving preparation; O(1) output. Include the chosen release-state operations in the derivation.

**Completion criteria and explanation:** Translate the contract into effective occupancy, prove reuse is safe, and explain why a room-counting answer on the original endpoints alone can be too small. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Arrival is not necessarily the moment the vehicle becomes reusable.

</details>

<details>
<summary>Hint 2</summary>

Model the entire period in which one vehicle is unavailable.

</details>

<details>
<summary>Hint 3</summary>

Widen e before adding gap, use a consistent release type, and test equality at the effective release.

</details>

- [ ] **X6 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 9. Complexity laboratory

For **each C exercise**, answer all ten questions: (1) input-size variables; (2) validation/copy preprocessing; (3) sorting and comparator work; (4) rows/events scanned; (5) total heap operations; (6) time; (7) peak auxiliary space; (8) output space; (9) caller mutation; (10) assumptions, including already ordered input, key cost, and allocation growth. Use n for intervals, q for queries, p for nonempty bookings, k for peak heap occupancy, and m for output rows. Explain irrelevant variables rather than silently assuming n=q.

Small numeric checkpoints below check operation counting, not the final asymptotic answers. **Do not read coding-task target costs while answering.** The [concepts cost chapter](./Intervals_Essential_Concepts.md#15-time-and-complete-auxiliary-space) and [complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md) are delayed review references; no full answer key is placed beside these questions.

### C1 — Every unordered pair

**Difficulty:** Easy.

This counts candidate pair inspections, not overlapping pairs. Preconditions: 0<=n<=100000, trusted. Add the cost of one constant-time F1 call per visit in your analysis, but do not replace pair inspection with an early-return algorithm.

```java
static long pairVisits(int n) {
    long visits = 0;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            visits++;
        }
    }
    return visits;
}
```

**Tests:** n=0 → 0; 1 → 0; 2 → 1; 4 → 6; 5 → 10. Derive the exact formula before simplifying. If a conflict detector can return early, give a best-case witness and a worst-case witness, accounting for any promised full validation. **Completion:** the ten-question analysis plus a summation and the numeric checks.


- [ ] **C1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C2 — An outer clone and an ordering pass

**Difficulty:** Medium.

Preconditions: valid rows, n<=100000; no endpoint mutation. This specimen reports the smallest start after sorting, or 0 for no rows. It is not an independent-row export.

```java
static int sortAndInspect(int[][] intervals) {
    int[][] order = intervals.clone();
    Arrays.sort(order, Comparator.comparingInt((int[] a) -> a[0])
            .thenComparingInt(a -> a[1]));
    return order.length == 0 ? 0 : order[0][0];
}
```

**Tests:** [[4,8],[-2,3],[1,5]] → -2; [] → 0; [[MAX,MAX],[MIN,MIN]] → MIN. Caller order and values remain unchanged. Would changing order[0][1] be safe under the same contract? Include object-array sorting workspace; distinguish a sorted-input adaptive best case from an arbitrary-input worst-case bound. Discuss a comparator whose extracted key costs h rather than constant time. **Completion:** ten questions and an ownership graph.

<details>
<summary>Hint 1</summary>

A returned scalar does not describe how much intermediate storage was needed.

</details>

<details>
<summary>Hint 2</summary>

int[][] takes the object-array comparator sorting path.

</details>

<details>
<summary>Hint 3</summary>

Count the cloned reference array and possible sort workspace, then separately identify whether any rows were cloned.

</details>

- [ ] **C2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C3 — Several core calls in one report

**Difficulty:** Medium.

The named methods are the learner TODO implementations, under their published targets. Inputs are respectively arbitrary valid closed rows, a valid closed-disjoint catalogue plus inserted row, strictly positive half-open activities, and valid half-open bookings. The wrapper returns four summary counts; it does not supply any core implementation.

```java
static int[] coreReport(int[][] ranges, int[][] existing, int[] inserted,
        int[][] activities, int[][] bookings) {
    int[][] union = mergeClosed(ranges);
    int[][] expanded = insertClosed(existing, inserted);
    int[] chosen = selectActivities(activities);
    boolean oneRoom = canAttendAll(bookings);
    return new int[]{union.length, expanded.length, chosen.length,
            oneRoom ? 1 : 0};
}
```

Let the four collection sizes be a,b,c,d before simplifying any equal-size case. Count validation, sorting where needed, scanning, exact output allocation, and temporary result storage. A helper's returned array becomes auxiliary relative to coreReport because coreReport returns only four ints. Do not multiply costs of sequential calls.

**Test:** ranges=[[1,3],[3,5]], existing=[[1,2],[7,8]], inserted=[3,6], activities=[[1,3],[3,5]], bookings=[[1,4],[2,3]] → [1,3,2,0]. With all collections empty and inserted=[2,2], result=[0,1,0,1]. This wrapper cannot run until its four TODO dependencies are implemented or replaced by explicitly identified test fixtures. **Completion:** ten questions, separate a/b/c/d terms, and an explanation of output ownership at two API boundaries.

<details>
<summary>Hint 1</summary>

Each helper solves a different contract with different useful preconditions.

</details>

<details>
<summary>Hint 2</summary>

Sequential phases add; peak live storage follows objects that coexist.

</details>

<details>
<summary>Hint 3</summary>

Classify the intermediate union, expanded array, and chosen IDs relative to the outer wrapper, not their helper names.

</details>

- [ ] **C3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C4 — Events after ordering

**Difficulty:** Medium.

This is only a fold over a **trusted correctly ordered event stream** from nonempty half-open intervals. Its record is a teaching representation; it does not validate or sort events. Calls contain +/-1 deltas, balanced and with no negative active prefix under the correct tie order.

```java
record Change(int time, int delta) {}

static long[] eventFoldMetrics(Change[] orderedEvents) {
    long visits = 0;
    int active = 0;
    int peak = 0;
    for (Change event : orderedEvents) {
        visits++;
        active += event.delta();
        peak = Math.max(peak, active);
    }
    return new long[]{visits, active, peak};
}
```

**Tests:** no events → [0,0,0]; (1,+1),(3,-1),(3,+1),(5,-1) → [4,0,1]; the twelve T6 half-open events → [12,0,3]. Separate this fragment's costs from the complete S2 wrapper that validates, filters, constructs, sorts, and folds events. How many events remain when n=7,p=6? What changes when all n closed rows include a point? Explain why an answer about only this loop cannot establish the whole sweep's complexity. **Completion:** ten questions for both fragment and complete API.

<details>
<summary>Hint 1</summary>

Count events in terms of retained bookings, not raw rows.

</details>

<details>
<summary>Hint 2</summary>

The preordered input assumption moves work outside this fragment; it does not erase that work from S2.

</details>

<details>
<summary>Hint 3</summary>

Build a phase-by-phase cost expression before dropping dominated terms.

</details>

- [ ] **C4 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C5 — Cleanup work across arrivals

**Difficulty:** Medium.

This helper mutates a caller-owned min-heap of end times. It is a fragment of active-set cleanup, not a complete room-counting solution. Preconditions: heap is valid, nonnull, and uses natural Integer order; entries may be duplicated.

```java
static int releaseThrough(PriorityQueue<Integer> ends, int start) {
    int removed = 0;
    while (!ends.isEmpty() && ends.peek() <= start) {
        ends.poll();
        removed++;
    }
    return removed;
}
```

**Tests:** heap [2,4,9], start=4 → removed=2, remaining multiset [9]; empty heap → 0; heap [5,5], start=4 → 0; same heap, start=5 → 2. Displays are multisets, never iteration promises.

Now assume a full S3 run admits p bookings exactly once, invokes this helper before each admission, and never reinserts a removed booking. Bound total successful polls and failed final loop tests. Give a witness with many removals at one arrival and a witness with no removal. Analyze sorting plus all offers/polls in terms of p,k; account for growth-triggered array copies and retained capacity. **Completion:** ten questions and an aggregate charge showing why nesting alone does not establish quadratic work.

<details>
<summary>Hint 1</summary>

An end can leave the active heap only after it entered.

</details>

<details>
<summary>Hint 2</summary>

Charge each successful poll to that unique earlier offer.

</details>

<details>
<summary>Hint 3</summary>

Distinguish a single cleanup call, all cleanup calls, and the whole API including sorted preparation.

</details>

- [ ] **C5 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C6 — Temporary copies and returned objects

**Difficulty:** Medium.

Preconditions: valid two-int rows, n<=100000. This deliberately discards all copies and returns only a scalar observation. Analyze actual live structures rather than counting variable names.

```java
static long copiedEndpointCount(int[][] intervals) {
    int[][] rows = new int[intervals.length][];
    List<int[]> buffer = new ArrayList<>();
    long cells = 0;
    for (int i = 0; i < intervals.length; i++) {
        rows[i] = intervals[i].clone();
        cells += rows[i].length;
        buffer.add(rows[i]);
    }
    int[][] outer = buffer.toArray(int[][]::new);
    if (outer.length != rows.length) {
        throw new AssertionError("unexpected conversion size");
    }
    return cells;
}
```

**Tests:** [] → 0; [[2,2]] → 2; three valid rows → 6. Compare classifying these row copies as auxiliary here versus output in F3. Explain how many row objects exist when input slots alias, whether the two intermediate outer arrays share row objects, and whether toArray adds another layer of row copies. A JVM may optimize allocations, but analyze the source-level algorithm conservatively rather than assuming escape-analysis elimination. **Completion:** ten questions and separate counts of endpoint cells, row objects, and reference arrays.

<details>
<summary>Hint 1</summary>

The public output here is one long.

</details>

<details>
<summary>Hint 2</summary>

Several containers can point at the same copied row without creating more endpoint cells.

</details>

<details>
<summary>Hint 3</summary>

Count n row clones once, but count each simultaneously live outer/reference buffer separately.

</details>

- [ ] **C6 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### C7 — Intervals and queries are separate workloads

**Difficulty:** Medium.

This arithmetic model counts one visit for each interval-query pair; it deliberately contains **no query-answer algorithm**. Preconditions: 0<=n,q<=100000. Run only small numeric tests; derive the formula for large inputs.

```java
static long queryPairVisits(int n, int q) {
    long visits = 0;
    for (int query = 0; query < q; query++) {
        for (int interval = 0; interval < n; interval++) {
            visits++;
        }
    }
    return visits;
}
```

**Tests:** (n,q)=(0,4) → 0; (3,0) → 0; (3,4) → 12; (5,2) → 10. Add validation and required q output slots when interpreting a real brute-force query API; n*q=0 does not make output construction free.

**Optional offline analysis:** without writing the Hard solver, suppose intervals and indexed queries are privately sorted; each interval enters a size-heap once and is removed at most once; each query reads a valid root or absence after lazy cleanup. Derive preparation, heap, indexed output, and peak storage bounds in n and q. Contrast true active-end storage with lazy invalid entries in a size-heap; k cannot automatically mean current coverage in both. Explain why O(n+q) pointer moves do not imply O(n+q) total time. **Completion:** ten questions for the baseline model; the advanced comparison is optional and does not block ordinary readiness.

<details>
<summary>Hint 1</summary>

A different q does not change the number n of interval candidates per query.

</details>

<details>
<summary>Hint 2</summary>

Count admissions and removals over the whole offline run, then add the sorting work.

</details>

<details>
<summary>Hint 3</summary>

Query records and the final answer array have different space roles; preserving original positions adds indexed assignments, not a search for matching duplicate values.

</details>

- [ ] **C7 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 10. Debugging laboratory

Every specimen below is **intentionally faulty under its local contract**. Inputs satisfy its trusted preconditions; missing general API validation is not the target defect. Faulty methods are separate names so they can coexist with your TODO implementations. Do not call a snippet wrong under a contract it never promised.

For **each D exercise**, (1) identify the defect; (2) construct your own failing input before using the supplied witness; (3) explain the failure step by step; (4) name the violated invariant; (5) repair it in your scratch file; (6) verify the corrected result, a control, and relevant boundaries; (7) analyze full time, auxiliary/output space, and mutation. No complete repaired implementation is provided. The supplied actual/required results are reproducible tests, not model solutions.

### D1 — An empty booking that seems busy

**Difficulty:** Easy.

Intended: true iff two valid half-open rows share any point, including allowed empty rows.

```java
static boolean badEmptyOverlap(int[] a, int[] b) {
    return a[0] < b[1] && b[0] < a[1];
}
```

**Witness:** [3,3) versus [1,5) gives true; required false. **Control:** [1,4) versus [2,5) gives true correctly. Add two empty rows, an empty row at an outer boundary, and swapped arguments. Explain the hidden assumption under which the two-inequality form is valid. **Completion:** seven debugging obligations and a predicate handling the full domain.


- [ ] **D1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D2 — A point disappears at a boundary

**Difficulty:** Easy.

Intended: true iff two valid **closed** rows overlap, including points and touching ranges.

```java
static boolean badClosedTouch(int[] a, int[] b) {
    return Math.max(a[0], b[0]) < Math.min(a[1], b[1]);
}
```

**Witness:** [1,3],[3,5] gives false; required true. [3,3],[3,3] also gives false; required true. **Control:** [1,4],[2,5] gives true correctly. Add a disjoint negative pair. Explain what exact intersection remains when the derived boundaries are equal. **Completion:** seven obligations and a convention-specific comparison explanation.


- [ ] **D2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D3 — Safe-looking numeric shortcuts

**Difficulty:** Medium.

Both rows are valid closed ranges. The first method is meant to return a comparator sign by start. The second is meant to return the number of **integer points** in one closed range, not its real duration.

```java
static int badCompareStart(int[] a, int[] b) {
    return a[0] - b[0];
}

static long badIntegerSize(int[] interval) {
    return (long) (interval[1] - interval[0] + 1);
}
```

**Witnesses:** a=[MIN,MIN], b=[1,1] yields positive MAX, but the comparator sign must be negative. [MIN,MAX] yields integer size 0, but requires 4294967296. **Controls:** [1,2] compared with [3,4] is negative; [2,5] has integer size 4. Test reversed comparison, equal keys, and [MAX,MAX] size 1. Do not demand a specific nonzero comparator magnitude. **Completion:** seven obligations for both expressions and an explanation of why casting a finished int calculation cannot undo overflow.

<details>
<summary>Hint 1</summary>

The result type is not necessarily the type used by earlier arithmetic.

</details>

<details>
<summary>Hint 2</summary>

Comparison needs an ordering sign; inclusive size needs a widened difference.

</details>

<details>
<summary>Hint 3</summary>

Inspect where promotion occurs and test both directions of the extreme-key comparison.

</details>

- [ ] **D3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D4 — A shrinking covered frontier

**Difficulty:** Easy.

Preconditions: currentEnd is the end of a valid closed merged frontier and next overlaps that frontier. Intended result: end of their union.

```java
static int badFrontierEnd(int currentEnd, int[] next) {
    return next[1];
}
```

**Witness:** currentEnd=10,next=[3,5] returns 5; required 10. **Control:** currentEnd=4,next=[3,7] returns 7 correctly. Build a full input [[1,10],[2,3],[4,5]] and explain which later gap decisions a shrinking end corrupts. Test equal ends and equal starts. **Completion:** seven obligations and a coverage-monotonicity argument.


- [ ] **D4 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D5 — A plausible merge order

**Difficulty:** Medium.

Preconditions: arbitrary valid nonempty-or-empty closed collection. Intended output is the same union as M1 with independent rows. The code preserves inputs, so find the logical ordering defect.

```java
static int[][] badEndOrderedMerge(int[][] intervals) {
    int[][] order = intervals.clone();
    Arrays.sort(order, Comparator.comparingInt((int[] a) -> a[1])
            .thenComparingInt(a -> a[0]));
    List<int[]> out = new ArrayList<>();
    for (int[] next : order) {
        if (out.isEmpty() || next[0] > out.get(out.size() - 1)[1]) {
            out.add(next.clone());
        } else {
            int[] last = out.get(out.size() - 1);
            last[1] = Math.max(last[1], next[1]);
        }
    }
    return out.toArray(int[][]::new);
}
```

**Witness:** [[1,10],[2,3]] produces [[2,10]], required [[1,10]]. **Control:** [[1,2],[4,5]] remains unchanged in value. Trace the missing covered start and identify the unproved monotonic fact used by the frontier. Add a negative enclosing interval and a point inside it. **Completion:** seven obligations, including why merely increasing the end cannot recover a lost left boundary.

<details>
<summary>Hint 1</summary>

Which boundary is assumed final when a component is first created?

</details>

<details>
<summary>Hint 2</summary>

End order does not prevent a later processed interval from beginning farther left.

</details>

<details>
<summary>Hint 3</summary>

Repair the order to match the ordinary invariant, or derive a genuinely different algorithm with a full proof.

</details>

- [ ] **D5 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D6 — An insertion region includes too much

**Difficulty:** Easy.

Preconditions: existing is one valid closed row, inserted is another. This helper should identify exactly the before region for closed insertion.

```java
static boolean badBeforeRegion(int[] existing, int[] inserted) {
    return existing[1] <= inserted[0];
}
```

**Witness:** existing=[1,3],inserted=[3,5] returns true, required false. The combined closed output must be [[1,5]], not two rows. **Control:** [1,2] before [4,5] is true. Use existing [[1,3],[5,7]] with inserted [3,5] as a full two-boundary integration test; required [[1,7]]. Explain why the supplied comparison is appropriate for a different nonempty half-open overlap-only contract. **Completion:** seven obligations and all three region predicates stated consistently.


- [ ] **D6 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D7 — A skipped choice changes the schedule

**Difficulty:** Medium.

Preconditions: all activities have start<end. Intended result: maximum compatible half-open count; input preserved.

```java
static int badSelectionCount(int[][] activities) {
    int[][] order = activities.clone();
    Arrays.sort(order, Comparator.comparingInt((int[] a) -> a[1])
            .thenComparingInt(a -> a[0]));
    boolean selected = false;
    int lastEnd = 0;
    int count = 0;
    for (int[] a : order) {
        if (!selected || a[0] >= lastEnd) {
            count++;
            selected = true;
        }
        lastEnd = a[1];
    }
    return count;
}
```

**Witness:** [[1,3],[0,4],[3,5]] gives 1; required 2. **Control:** [[1,3],[3,5]] gives 2. Test negative chains, all-overlapping duplicates, and empty input. Explain whether the earliest-finish exchange proof validates this particular state update. **Completion:** seven obligations and a distinction between last scanned end and last selected end.

<details>
<summary>Hint 1</summary>

The state name should match the object whose end it stores.

</details>

<details>
<summary>Hint 2</summary>

Rejecting a candidate must not make the retained schedule finish later.

</details>

<details>
<summary>Hint 3</summary>

Trace the update on the skipped middle row before deciding where it belongs.

</details>

- [ ] **D7 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D8 — A fabricated peak at a timestamp

**Difficulty:** Medium.

Preconditions: all bookings are strictly nonempty half-open ranges. Intended result: peak demand. The Change record is defined in C4.

```java
static int badEventPeak(int[][] bookings) {
    List<Change> events = new ArrayList<>();
    for (int[] row : bookings) {
        events.add(new Change(row[0], 1));
        events.add(new Change(row[1], -1));
    }
    events.sort(Comparator.comparingInt(Change::time).thenComparing(
            Comparator.comparingInt(Change::delta).reversed()));
    int active = 0;
    int peak = 0;
    for (Change event : events) {
        active += event.delta();
        peak = Math.max(peak, active);
    }
    return peak;
}
```

**Witness:** [[1,3],[3,5]] gives 2; required 1. **Control:** [[1,4],[2,3]] gives 2. The six nonempty T6 bookings produce 4 here; required 3. Name the time where the extra demand is invented. Then explain under which closed contract this ordering would be useful, including points. **Completion:** seven obligations and a membership-based tie proof.

<details>
<summary>Hint 1</summary>

Inspect exact occupancy at the shared coordinate.

</details>

<details>
<summary>Hint 2</summary>

A temporary intermediate count is unsafe if it includes a departed half-open booking.

</details>

<details>
<summary>Hint 3</summary>

Change the event-kind order consistently with the contract, and separately handle empty rows in a public wrapper.

</details>

- [ ] **D8 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D9 — The wrong release is visible

**Difficulty:** Medium.

Preconditions: strictly nonempty half-open bookings. Intended result: peak with a true active-set heap, preserving input.

```java
static int badMaximumEndHeap(int[][] bookings) {
    int[][] order = bookings.clone();
    Arrays.sort(order, Comparator.comparingInt(a -> a[0]));
    PriorityQueue<Integer> ends = new PriorityQueue<>(Comparator.reverseOrder());
    int peak = 0;
    for (int[] row : order) {
        while (!ends.isEmpty() && ends.peek() <= row[0]) {
            ends.poll();
        }
        ends.offer(row[1]);
        peak = Math.max(peak, ends.size());
    }
    return peak;
}
```

**Witness:** [[1,2],[1,10],[3,4]] gives 3; required 2. **Control:** [[1,3],[3,5]] gives 1. At the third arrival, identify the expired end hidden behind the head. Add several short bookings beneath one long booking. **Completion:** seven obligations and the implication needed to conclude that no stored booking has expired.

<details>
<summary>Hint 1</summary>

The next booking to release a room need not have the greatest end.

</details>

<details>
<summary>Hint 2</summary>

A root-based stop condition must imply a fact about every remaining entry.

</details>

<details>
<summary>Hint 3</summary>

Match the heap direction to the expiration proof, then test an expired small end beneath a large live end.

</details>

- [ ] **D9 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D10 — One removal and a claim about active count

**Difficulty:** Medium.

This helper receives a valid natural-order min-heap of previously admitted booking ends. Intended postcondition: remove **all** ends<=start and return the number still active before a new arrival. Queue mutation is required; it must preserve all later ends with multiplicity.

```java
static int badActiveCleanup(PriorityQueue<Integer> ends, int start) {
    if (!ends.isEmpty() && ends.peek() <= start) {
        ends.poll();
    }
    return ends.size();
}
```

**Witness:** ends=[2,4,9],start=5 returns 2 and leaves [4,9]; required 1 and [9]. **Control:** ends=[2,9],start=5 returns 1 and leaves [9]. Test empty heap and duplicate expirations [5,5] at start 5. This defect concerns the stated current-active-set postcondition. A separate correct one-end-per-allocated-room algorithm can reuse at most one room per arrival and poll once; do not claim that algorithm is wrong without its own failing contract.

**Completion:** seven obligations, including a post-state test rather than only a final room-count test.

<details>
<summary>Hint 1</summary>

How many previous bookings can finish between successive arrivals?

</details>

<details>
<summary>Hint 2</summary>

One successful removal does not establish that the next minimum is live.

</details>

<details>
<summary>Hint 3</summary>

Continue checking until the root proves all remaining ends are later; distinguish this helper from an allocated-room pool.

</details>

- [ ] **D10 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D11 — A new container with shared contents

**Difficulty:** Medium.

Preconditions: valid rows in any order. Intended result: independent same-order rows, preserving input and permitting independent output edits. This faulty export uses a list and conversion but does not meet that ownership contract.

```java
static int[][] badSharedExport(int[][] intervals) {
    List<int[]> rows = new ArrayList<>();
    Collections.addAll(rows, intervals);
    return rows.toArray(int[][]::new);
}
```

**Witness:** input=[[1,3],[5,7]], call export, then assign output[0][1]=99. Actual input becomes [[1,99],[5,7]]; required unchanged [[1,3],[5,7]]. **Control:** before the edit, numeric output values match and output!=input, so a value-only test falsely passes. Test the same int[] in two input slots; editing one output row must not change its sibling under the intended contract.

**Completion:** seven obligations and a reference graph showing exactly which allocations are missing. Explain why replacing the list constructor with new ArrayList<>(Arrays.asList(intervals)) still leaves row sharing.

<details>
<summary>Hint 1</summary>

Separate outer-container identity from row identity.

</details>

<details>
<summary>Hint 2</summary>

toArray transfers the list's element references into array slots.

</details>

<details>
<summary>Hint 3</summary>

Create independent row values at the ownership boundary, including repeated input identities.

</details>

- [ ] **D11 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### D12 — The last count is not the largest count

**Difficulty:** Medium.

Preconditions: strictly nonempty half-open bookings. Intended result: historical peak demand; input preserved. The heap cleanup itself is correct for the active-set interpretation.

```java
static int badLastActiveCount(int[][] bookings) {
    int[][] order = bookings.clone();
    Arrays.sort(order, Comparator.comparingInt(a -> a[0]));
    PriorityQueue<Integer> ends = new PriorityQueue<>();
    for (int[] row : order) {
        while (!ends.isEmpty() && ends.peek() <= row[0]) {
            ends.poll();
        }
        ends.offer(row[1]);
    }
    return ends.size();
}
```

**Witness:** [[1,4],[2,5],[8,9]] gives 1; required 2. **Control:** [[1,5],[2,4]] gives 2 correctly. On the six T6 bookings, actual is 1 and required is 3. Explain why a correct current-state invariant does not prove the returned statistic. Test empty input, all overlapping, all disjoint, and several simultaneous expirations. **Completion:** seven obligations and an explicit historical statistic separate from live state.

<details>
<summary>Hint 1</summary>

Ask whether demand can fall after reaching its maximum.

</details>

<details>
<summary>Hint 2</summary>

The last arrival samples one point in the execution history.

</details>

<details>
<summary>Hint 3</summary>

Record the requested historical quantity as the active state changes; do not reinterpret the active heap as an allocated-room heap.

</details>

- [ ] **D12 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 11. Offline query workshop — optional

**Optional advanced.** This is preparation for curated L6, not a required readiness gate. O1 and O2 are isolated helpers, and O3 is a reasoning trace. The workbook deliberately omits the complete interval-admission/query-answer loop and the full Hard solution. Reuse [optional query theory](./Intervals_Essential_Concepts.md#13-offline-interval-queries--optional-advanced) after an attempt.

Here an interval is a **closed integer range** and its score is the number of integer points, not real duration. Full int endpoints require long size arithmetic. Queries can repeat, and answers belong to their original positions. An eligible-by-start interval may already have expired; those are separate facts.

### O1 — Indexed query preparation — optional

**Difficulty:** Medium. **Objective:** Change processing order while retaining every original identity.

**Prerequisites:** F2 and immutable records; optional offline-query introduction.

**Problem, input, and output:** Return fresh QuerySlot records for each query occurrence, sorted by value ascending then original index ascending. Preserve the caller query array. This helper prepares order only and does not answer any interval query.

**Contract:** queries is nonnull, q<=100000 size precondition, any int values including duplicates/negatives. Return a fresh QuerySlot[] with q newly constructed immutable records. OriginalIndex is the original zero-based position. Output displays [value,index]. There are no interval rows to validate in this helper. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[9,4,7,4,0]]` | `[[0,4],[4,1],[4,3],[7,2],[9,0]]` | Duplicate positions |
| `[[]]` | `[]` | Empty |
| `[[5]]` | `[[5,0]]` | Singleton |
| `[[2147483647,-2147483648,0]]` | `[[-2147483648,1],[0,2],[2147483647,0]]` | Safe ordering |
| `[null]` | `NPE` | Null queries |

**Additional edge and ownership checks:** All queries equal; already ordered/reverse ordered; repeated calls create independent arrays and fresh records. Test reconstruction of original values using originalIndex without searching by value.

**Java 21 starter:**

```java
record QuerySlot(int value, int originalIndex) {}

public static QuerySlot[] queryOrder(int[] queries) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO O1");
}
```

**Target costs:** O(q log(q+1)+1) time, O(q+1) worst-case sort workspace, O(q+1) required output. Inside a later solver this prepared array would instead be auxiliary.

**Completion criteria and explanation:** Explain why duplicates cannot be represented by one value-to-index map entry and why restoring answer order is indexed placement. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

Equal values can still belong to different requested output slots.

</details>

<details>
<summary>Hint 2</summary>

Retain the original index before changing the order.

</details>

<details>
<summary>Hint 3</summary>

Sort immutable value/index records with safe keys and a deterministic tie rule.

</details>

- [ ] **O1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### O2 — Minimum valid exposed candidate — optional

**Difficulty:** Medium. **Objective:** Distinguish lazy head validity from a true active-set heap.

**Prerequisites:** Heap minimum semantics and optional closed integer queries.

**Problem, input, and output:** Given a size-ordered candidate heap, remove expired heads until the head covers the current query or the heap is empty. Return the valid head size, or -1 when none remains. Do not admit new intervals or solve a batch of queries.

**Contract:** Required heap nonnull, checked. Trusted preconditions: heap uses size ascending then end ascending; every immutable Candidate has a positive correctly computed long size from a valid closed range already eligible by start<=query; no null entries. Query is any int. End==query remains valid. Mutating this queue by polling expired heads is required; do not change records or remove a valid head. Expired non-head entries may remain. Heap displays below are [end,size] candidates in priority order, not iterator order. All shared coding rules in Section 1 also apply.

**Examples and tests:** arguments appear in method-parameter order. MIN/MAX are written as their exact int values. Exception rows test the promised rejection behavior, rather than a successful output.

| Arguments | Expected result | Purpose |
| --- | --- | --- |
| `[[[3,2],[5,6],[9,6]],6]` | `6` | Purge two heads, leave [9,6] |
| `[[[8,1]],8]` | `1` | Closed point stays |
| `[[[2,3]],3]` | `-1` | All expired |
| `[[],4]` | `-1` | Empty heap |
| `[[[5,1],[2,3]],5]` | `1` | Expired non-head may remain |
| `[[[2147483647,4294967296]],0]` | `4294967296` | Long size |
| `[null,0]` | `NPE` | Required heap |

**Additional edge and ownership checks:** Verify actual remaining multiset as well as return value: first fixture leaves [[9,6]], point fixture remains [[8,1]], all-expired leaves [], buried-expired fixture retains both rows. Add duplicate candidates and tied sizes. Do not test malformed heaps outside trusted preconditions.

**Java 21 starter:**

```java
record Candidate(int end, long size) {}

public static long smallestAfterCleanup(PriorityQueue<Candidate> candidates, int query) {
    // TODO: derive, implement, and test the stated contract.
    throw new UnsupportedOperationException("TODO O2");
}
```

**Target costs:** For h initial entries and r removed heads, O((r+1) log(h+1)+1) is a valid bound; tighter O(r log(h+1)+1). O(1) auxiliary beyond the supplied heap; O(1) scalar output. Across an offline run each admitted candidate is removed at most once.

**Completion criteria and explanation:** Prove why a valid minimum-size head is sufficient even if invalid candidates remain buried, and why heap size cannot be used as current coverage count. Pass the shared derivation, validation, and independence checks in Section 2.

<details>
<summary>Hint 1</summary>

The queue is ordered by candidate score, not by expiration time.

</details>

<details>
<summary>Hint 2</summary>

Only a valid exposed minimum can answer the query; an invalid exposed minimum is useless forever as queries move right.

</details>

<details>
<summary>Hint 3</summary>

Inspect and poll heads repeatedly, preserve end equality, and stop as soon as the minimum is valid.

</details>

- [ ] **O2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### O3 — An offline answer ledger — optional

**Difficulty:** Medium.

Closed integer ranges `[[1,6],[2,4],[5,10],[9,9]]`; queries in original order `[9,4,7,4,0]`. Without implementing the full solver, fill a ledger with sorted query/value index, newly start-eligible ranges, expired heads removed, minimum valid score, and original answer slot. Derive inclusive sizes in long. Explain why size ordering differs from S3 end ordering.

**Checkpoints:** sorted (value,index) sequence is (0,4),(4,1),(4,3),(7,2),(9,0). Restored answers are [1,3,6,3,-1]. At query 7, the size-3 range [2,4] and size-6 range [1,6] are expired; [5,10] remains. At query 9, [9,9] is still valid. At query 4, retain a range ending at 4.

**Further tests:** no intervals with queries [2,2] yields [-1,-1]; no queries yields []; range [MIN,MAX], query 0 has size 4294967296. For query 5, valid [5,5] size 1 may sit ahead of expired [0,2] size 3; a stored-size count of 2 does not mean two ranges cover 5. Record candidates as sorted multisets only for explanation.

**Completion:** preserve both duplicate-query slots, explain eligibility and expiry separately, state the lazy invariant, and derive costs in n and q. Compare with a direct scan baseline for small inputs. No full implementation is required or supplied.

<details>
<summary>Hint 1</summary>

Processing order and answer order need not be the same.

</details>

<details>
<summary>Hint 2</summary>

Once a query moves right, a range ending to its left cannot become valid again.

</details>

<details>
<summary>Hint 3</summary>

Trace admissions and head removals separately; write each answer through the stored original index.

</details>

- [ ] **O3 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 12. Mock interviews

Use approximately **35–45 minutes**, adjusted to the interview format: clarify, propose a baseline, derive, implement, test, and explain. The interviewer hides topic tags, target-cost text, hints, and earlier code. Reusing a remembered example is communication practice; a readiness assessment needs an unseen input/contract variant. A hint makes the attempt H even if the final code is correct.

### K1 — Coverage and a changed boundary

**Difficulty:** Medium.

Ask a partner to select M1 or I1 and provide an unseen range input. Before coding, state whether the input is arbitrary or already sorted/disjoint, whether output rows must be independent, and what touching means. Derive a baseline and invariant aloud. After implementation, the interviewer switches to a fully specified half-open policy and introduces an empty row or empty insertion; re-state the changed domain rather than editing one operator blindly.

**Calibration tests:** closed [[-3,0],[0,4],[2,3]] normalizes to [[-3,4]]. Half-open overlap-only normalizes the same endpoints to [[-3,0],[0,4]]. Inserting closed [2,6] into [[0,2],[5,8]] yields [[0,8]]; under nonempty half-open overlap-only it yields [[0,2],[2,8]]. Input preservation and fresh rows are required.

**Follow-ups:** Why can only one frontier remain unfinished? Which assumption makes sorting unnecessary? Where could a shallow copy fail? What are the output and auxiliary costs separately? What progress measure proves termination? Would an empty insertion allow skipping validation?

**Pass evidence:** correct Java from a blank editor, tested endpoint/ownership cases, a proof connecting each comparison to the contract, and defensible cost analysis. Record recognition, implementation, proof, and communication gaps in this one attempt record. A partner must add a new variant for R evidence; these published calibration cases alone are familiar practice.

<details>
<summary>Hint 1</summary>

Restate the output objective before choosing the ordering.

</details>

<details>
<summary>Hint 2</summary>

A changed endpoint convention can alter both valid inputs and region boundaries.

</details>

<details>
<summary>Hint 3</summary>

Write the invariant and equality cases beside the relevant branch before revising code.

</details>

- [ ] **K1 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —

### K2 — Demand or choice?

**Difficulty:** Medium.

Ask a partner to choose an unseen form of S1, S2, S3, or one X task without naming the technique. Begin by distinguishing whether every record must be retained, whether only one instant is queried, and whether the answer is a count, selected set, union, or historical statistic. Give two plausible approaches and justify your choice. Finish with an adversarial tie and an arithmetic or ownership test.

**Calibration tests:** [[1,4],[2,5],[5,7]] requires 2 half-open rooms, has maximum compatible activity count 2, and has demand 1 at time 5. These answers refer to different questions. With closed ranges, the room/demand interpretation must include shared points; at time 5 two closed ranges contain it. For X3 alone, a global event sort is unnecessary. Also use [[1,2],[1,10],[3,4]] to challenge heap direction; half-open peak is 2.

**Follow-ups:** Why does merging lose information? Why do ends leave before starts here? Can several heap removals occur on one arrival? Can a one-poll allocated-room algorithm still be correct? How would reward weights affect a selection proof? Which costs arise before the main loop? Can your final live state be smaller than an earlier peak?

**Pass evidence:** independently choose a suitable method, derive ties and invariant, implement/test, and compare alternatives. For ordinary readiness, demonstrate unfamiliar success again in a separate session and store the assessment once in the roadmap. Premium and optional Hard completion are not conditions.

<details>
<summary>Hint 1</summary>

Different outputs require retaining different information about the same input.

</details>

<details>
<summary>Hint 2</summary>

Test a touching pair and a duplicate pair against your proposed state.

</details>

<details>
<summary>Hint 3</summary>

Count total heap admissions/removals and state whether the heap means active bookings, allocated rooms, or minimum-score candidates.

</details>

- [ ] **K2 attempt** — Last: — | Result: — | Minutes: — | Next review: — | Mistake / lesson: —


## 13. Revision and spaced repetition

Use the [roadmap review approach](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): roughly **1, 3, 7, and 14 days** after learning, adjusted to retention and available time. Missed dates do not force a restart. After a failure or hint, shorten the next gap; after repeated cold success, lengthen it and test a new statement.

For a cold re-solve, open only the statement and shared/local contracts. Hide previous code, theory implementations, hints, target answers, and this chapter's pattern map. Reconstruct the baseline, sorting decision, invariant, correctness argument, and costs; implement from a blank editor; test one ordinary and several risk-focused cases; only then compare with previous work.

Prioritize S/unfinished attempts, then H attempts; then errors in overlap/empty rules, sorting choice, Greedy proof, equal-time event order, heap semantics, and shared-row mutation. Fix the smallest missing prerequisite, then redo the exercise cold. If a familiar code shape comes immediately from memory, ask for an unseen contract variation before treating it as transfer.

Update the existing exercise record with date, I/H/S, minutes, next due date, and a precise lesson. For curated problems update the canonical roadmap Log and keep the pointer here. These records are the review queue; there is no second revision ledger. A successful cold curated solve supports P, while unfamiliar repeatable performance supports R. Use the [common rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) to judge conceptual failures without treating every self-corrected typo as lost mastery.

## 14. Completion and readiness

These are evidence milestones, not another topic dashboard. All remain unchecked until you demonstrate them. Store topic-level T/I/P/R and assessment dates only in the [roadmap](./DSA_Interview_Roadmap.md#11-intervals).

- [ ] **Fundamentals:** read a row as a set on its stated axis and distinguish union, selection, and multiplicity-aware demand (T1).
- [ ] **Endpoint convention:** explain a touching pair whose result changes with the model; distinguish closed points from empty half-open rows (T1/F1).
- [ ] **Overlap:** derive a symmetric predicate, including an empty operand and int extrema, without memorized inequality substitutions (F1).
- [ ] **Ordering and Java:** implement start/end/reversed-primary keys safely; explain outer sorting, row aliasing, and list conversion (F2/F3/J1).
- [ ] **Merging:** implement M1 from a blank editor, preserve nested/equal-start/equal-end/touching/point coverage, and state the frontier proof; adapt the half-open output policy (M2).
- [ ] **Insertion:** implement linear three-region insertion, avoid unnecessary sorting, handle empty insertion under the correct contract, and distinguish output from auxiliary allocation (I1/I2).
- [ ] **Selection:** return a maximum compatible set, derive removals, and give an exchange argument plus a counterexample to an invalid rule (G1/P1/P2).
- [ ] **Conflicts:** implement free S1 and prove adjacent checks suffice after appropriate ordering/empty handling.
- [ ] **Sweep:** derive equal-time event order from membership, count peak demand, explain closed point peaks, and test empty inputs (S2/T6).
- [ ] **Heap:** implement free S3, maintain the true active-set invariant, remove all expired ends, track peak, and explain why peak rooms suffice (T7).
- [ ] **Essential practice:** independently solve or cold re-solve L1–L3 with tests, proof, and full costs; update canonical roadmap Logs.
- [ ] **Additional breadth:** demonstrate scheduling mechanics with S1–S3; Premium L4/L5 are optional paid practice, never an access prerequisite.
- [ ] **Optional advanced:** if chosen, investigate O1–O3/L6 with original query indexes and lazy validity. Leaving this item unchecked does not block ordinary readiness.
- [ ] **Interview readiness:** choose methods on unfamiliar Level 5/mocked variants across separate sessions, communicate assumptions/proofs/costs, and pass all dimensions of the common rubric.

For **T**, explain essential concepts without notes. For **I**, complete the roadmap's blank-editor merge and concurrency drills with adversarial tests, and reliably derive insertion and selection; the workbook offers practice in both sweep and heap reasoning. For **P**, establish independent Essential problem evidence. For **R**, derive unfamiliar endpoint/tie rules, choose union versus selection versus active counting, explain Java mutation and sorting costs, and repeat successful transfer across sessions. Reading this file, compiling its starters, or checking an H/S attempt is not a substitute for those gates.

Use follow-up questions to test retained understanding: What becomes final after each step? Why is no important interval skipped? Which comparison changes under a different endpoint policy? Why does the algorithm terminate? What would invalidate the Greedy proof? What extra storage does Java allocate? What simpler method fits if the input is already sorted or only one query is asked? Answers must refer to the chosen contract, not a memorized pattern name.

## 15. References and author validation

Repository sources inspected for this workbook:

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Intervals](./DSA_Interview_Roadmap.md#11-intervals), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [four gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category), and [reviews](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition).
- [Intervals Essential Concepts](./Intervals_Essential_Concepts.md): theoretical authority for membership, frontiers, insertion, selection proof, sweep phases, heap meanings, ownership, and optional queries.
- [Heap / Priority Queue Essential Concepts](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md): [Java operations](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#8-java-21-priorityqueue) and [complete costs](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#16-complete-complexity-analysis).
- [Java Arrays](./Java_Array.md): [copying](./Java_Array.md#copying-arrays) and [multidimensional arrays](./Java_Array.md#8-multidimensional-and-jagged-arrays).
- [Java Lists / ArrayList / LinkedList](./Java_List_ArrayList_LinkedList.md): [conversions](./Java_List_ArrayList_LinkedList.md#array-and-list-conversions) and [aliases](./Java_List_ArrayList_LinkedList.md#aliases-and-shallow-copies).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): [Arrays.sort](./Complexity/Complexity_Analysis_and_Foundations.md#156-arrayssort), allocation, sequential phases, output, and amortized costs.
- [Binary Search concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) and [Arrays & Hashing prefix queries](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries): alternatives used in mixed challenges.
- [Backtracking workbook](./Backtracking_Exercises.md): repository conventions for TODO starters, optional hints, ownership tests, and compact attempt records.

Java 21 APIs were checked through Context7 and official documentation. Object-array sorting is stable and may use linear temporary reference storage; comparator reversal applies to the comparator it receives, so secondary-key placement matters. [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), [Comparator](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html)

A PriorityQueue exposes the least element under its ordering, preserves duplicate entries, and does not promise sorted iteration. Its operation costs and backing-capacity behavior must be included in full API analysis. [PriorityQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)

List/array conversion transfers element references; arrays containing mutable rows need a deliberate ownership policy. Arrays.asList is a fixed-size backed view. [Collection.toArray](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collection.html), [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html), [Arrays.asList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html)

Official problem links are in Section 7. Priorities, order, difficulties, and Premium labels follow the local roadmap; no paid statement content is required or reproduced. The optional Hard problem remains separate from ordinary readiness.

### Author validation

Completed **2026-10-31**. The content audit verified **15 sections**, **five progressive levels**, **52 original exercises**, **20 coding starters**, **132 optional hints**, and all **six curated problems in exact E/E/E/A/A/C order**. Every exercise has an unchecked attempt record; the six curated records point to the roadmap's existing Logs. All **75 local links**, including heading anchors, resolve.

All **40 Java blocks** were assembled in one scratch class and compiled with Java 21 using `--release 21 -Xlint:all -Werror`. The **20 learner methods remain TODOs**. Compilation establishes valid scaffolding, not completed learner solutions. All **172 published coding-example rows**, including the specified exception cases, were matched against the rendered tables and executed using temporary validation fixtures. Those fixtures reused applicable algorithms from the concepts guide and supplied author-only adapters for the changed contracts; they are not included in this workbook or a repository solutions file.

Independent runtime checks passed **1,138,797 assertions** over **11,412 interval collections**, **9,631 activity-selection cases**, and **472 insertions**, including exhaustive small-domain collections, seeded extreme-int cases, aliases, and a 100000-booking boundary fixture. Checks used overlap-graph components for union, exhaustive subsets for optimal selection counts, direct membership for demand, direct summation for reading queries, and coverage complements for available windows. They also checked deterministic identities/order, duplicate multiplicity, empty/point rules, widened arithmetic, preserving row identities, fresh output ownership, repeated calls, and **61 additional invalid-input cases**. Temporary query helpers were compared with direct candidate scans; no complete Hard solver was added.

The exact published faulty methods and executable tracing/cost specimens were exercised. The runtime suite includes **31 bug witness/control checks** across all **12 debugging exercises**, with further published witnesses in the trace checks, and **173 focused checks** for Java references, operation counts, proofs' small examples, mock calibration, and optional query outputs. A separate **484-check trace audit** verified seven merge states, three insertion-frontier updates, 12 half-open event states, 14 closed events, six active-heap states, ordering/selection checkpoints, candidate cleanup, and **450 overlap comparisons** against direct point/interior membership. The C3 wrapper was executed with its four TODO dependencies explicitly redirected to temporary fixtures; it was not presented as a completed learner implementation.

Only **Intervals_Exercises.md** was created by this task inside DSA_Guide. Authoring, compiled classes, instrumentation, and oracle files stayed outside the repository in a temporary directory. SHA-256 checks confirmed all **35 remaining pre-existing files** are unchanged. Six unrelated files under **implementation-guides/** were present in the initial inventory but disappeared during the task outside these edits; they were neither modified nor restored by this work. No workbook validation issue remains unresolved.

