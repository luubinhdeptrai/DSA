# Two Pointers — Exercises (Java 21)

- [1. Introduction](#1-introduction)
- [2. Problem-Solving Workflow](#2-problem-solving-workflow)
- [3. Level 1 — Fundamental Pointer Operations](#3-level-1--fundamental-pointer-operations)
- [4. Level 2 — Core Algorithmic Patterns](#4-level-2--core-algorithmic-patterns)
- [5. Level 3 — NeetCode / LeetCode Practice](#5-level-3--neetcode--leetcode-practice)
- [6. Level 4 — Unfamiliar Interview Challenges](#6-level-4--unfamiliar-interview-challenges)
- [7. Complexity Analysis Exercises](#7-complexity-analysis-exercises)
- [8. Debugging Exercises](#8-debugging-exercises)
- [9. Mock Interview Questions](#9-mock-interview-questions)
- [10. Revision and Spaced Repetition](#10-revision-and-spaced-repetition)
- [11. Two Pointers Completion Criteria](#11-two-pointers-completion-criteria)
- [12. Learning References and Delayed Checks](#12-learning-references-and-delayed-checks)

## 1. Introduction

This workbook turns the [Two Pointers roadmap requirements](./DSA_Interview_Roadmap.md#2-two-pointers) into independent practice. Use [Two Pointers — Essential Concepts](./Two_Pointers_Essential_Concepts.md) for theory, then close it before coding. Prerequisites are Java arrays and strings, loops, hashing, sorted order, and basic complexity analysis. Sliding Window and advanced algorithms are not required.

There are **28 original exercises**: 15 implementation problems, six complexity exercises, and seven debugging challenges. There are also **five roadmap problems**, in their original E/A/C order. The mock interview section reuses these tasks rather than adding another problem list.

| Level | Exercises | Purpose |
| --- | --- | --- |
| 1 — Fundamental pointer operations | F1–F4 | Bounds, mirrored positions, safe writes, and coordinated traversal |
| 2 — Core algorithmic patterns | P1–P7 | Derive movements, handle multiplicities, and prove correctness |
| 3 — NeetCode / LeetCode practice | N1–N5 | Apply the exact platform contract independently |
| 4 — Unfamiliar interview challenges | X1–X4 | Choose an algorithm from the statement with hints closed |

Work through one or two tasks per session. On an initial Level 4 attempt, read only the statement, contract, tests, and target bounds; keep hints and theory links closed. Do not inspect the corresponding concepts example before attempting a cold implementation.

### Objective coverage

| Roadmap objective | Practice evidence |
| --- | --- |
| Distinguish opposite-end, read/write, and two-sequence positions | F1–F4, P1–P4, P7 |
| Justify movement without losing a valid or better answer | P1, P2, P6, X1, X4 |
| Understand sorting and original positions | P5, P6, X2, X3, D7 |
| Suppress duplicate results at the correct level | P3–P5, N3, D6 |
| Separate mutation, logical length, auxiliary storage, and output | F3, P3–P5, P7, X3, C4–C6 |
| Implement the roadmap's pair, removal, and palindrome drills | P1, F3, P2 |
| Derive linear pointer travel and quadratic anchor work | C1–C3, P5 |
| Demonstrate unfamiliar recognition and communication | X1–X4 and Section 9 |
| Explore boundary maxima optionally | N5; never a mandatory readiness gate |

### Shared contracts and notation

These rules apply to every exercise unless a narrower contract is stated. Inputs are non-null; allocations are feasible; array and string lengths are at most 100,000. All indices are zero-based, ranges are half-open, and duplicate values are allowed. `MIN` and `MAX` in tests mean `Integer.MIN_VALUE` and `Integer.MAX_VALUE`. Numeric targets and counts declared `long` are mathematical values, not overflowing int expressions. Invalid inputs outside a stated precondition need not be validated.

Every string exercise here uses **ASCII code units 0–127**, not arbitrary Unicode characters. Case-sensitive tasks compare exact code units. Filtered tasks retain only ASCII letters/digits and compare them after ASCII lowercasing. Empty filtered content is valid. Do not silently broaden this contract to full Unicode normalization or case folding; consult [Java Strings](./Java_String.md) and [the concepts guide's Java decisions](./Two_Pointers_Essential_Concepts.md#11-java-21-implementation-decisions).

**Space convention:** auxiliary space excludes the required returned output, as in the concepts guide. Report output storage separately. A preprocessing clone, oversized temporary buffer, or list later copied into the final result is auxiliary. Use `S_sort(n)` for the chosen Java sort's workspace; sorting a mutable array does not prove constant auxiliary space. Hash-table targets are expected/amortized under ordinary fixed-size integer-key hashing, not unconditional worst-case guarantees. Read an O(n) bound as O(n+1) when accounting for empty-input overhead.

The test tables list arguments in method order inside an outer pair of brackets. For example, `[[2,5,2],2]` means an array and a rejected value. A prefix result such as `1; [5]` means return 1 and leave `[5]` in positions `[0,1)`; **the tail is ignored**. For methods returning arrays/lists, compare contents, not reference identity. Tests belong in a separate scratch class outside this guide.

### One compact record per exercise

Each checkbox records a completed attempt, testing, and postmortem. It does **not** automatically mean independent success. Fill the same line after reviews: date, result, minutes, next due date, and one useful mistake or lesson. Retain short history on that line rather than copying it into a review ledger.

- **I:** independently correct, tested, and explained without hints.
- **H:** a conceptual hint was needed, including a revealed pattern or theory link during the attempt.
- **S:** substantial solution/code review was needed, or the attempt could not be finished.

The [roadmap's T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain authoritative. For N1–N5, choose this workbook as the detailed attempt log and keep only a reference to it in your roadmap problem row if you update that row yourself; alternatively keep the detailed log in the roadmap and use the workbook field as a reference. Do not maintain two copies of dates/results. This workbook starts with every record unchecked.

## 2. Problem-Solving Workflow

Use all eleven steps for P, N, and X tasks. For F tasks, a few sentences and a trace are sufficient; do not force an asymptotic improvement when the useful improvement is less memory or safer mutation.

1. Restate the input and required output, including what counts as the same answer.
2. Identify size, ordering, arithmetic, character, mutation, and index constraints.
3. Describe a simple correct brute-force approach before selecting a pattern.
4. Derive that baseline's time and auxiliary space.
5. Identify repeated work or avoidable allocation.
6. Identify a useful ordering fact or invariant; state what each position means.
7. Derive an improved algorithm from that fact.
8. Prove each move is safe, including equality, duplicate, and exhaustion branches.
9. Implement from a blank editor using Java 21.
10. Run the listed tests and add a counterexample to one plausible wrong approach.
11. Derive final time, auxiliary space, output storage, and termination; record the result honestly.

Before moving a pointer, finish this sentence: “Everything I am discarding is already processed, impossible, or unable to improve the recorded answer because ___.” For in-place work, additionally explain why a write cannot destroy unread input. Use the [movement-proof reference](./Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements) only after an independent attempt.

### Starter and testing conventions

Place the implementation stubs inside a scratch class named `Practice`; import `java.util.*` for P5 and whichever collections you choose later. Replace each placeholder yourself. Analysis/debug snippets later in the workbook are intentionally executable material to inspect; they are not solutions to the implementation tasks.

This optional harness checks F3's result contract without implementing its algorithm. Add independent tests for the method you are studying. The harness's defensive copies do not count toward the algorithm's space bound.

```java
public final class PracticeSmoke {
    public static void main(String[] args) {
        int[] values = {2, 5, 2, -1, 0, 2};
        int length = Practice.removeChosen(values, 2);
        if (length != 3 || !java.util.Arrays.equals(
                java.util.Arrays.copyOf(values, length), new int[]{5, -1, 0})) {
            throw new AssertionError("F3 logical prefix is incorrect");
        }
        System.out.println("F3 smoke test passed");
    }
}
```

For read-only methods, compare every input against a saved copy after the call. For P5, verify the input multiset is preserved even if its order changes. For P7, save the valid buffer prefix and incoming array before calling. For P1, validate any returned witness against the original input and enumerate all pairs on small inputs to check a claimed absence. For P5, compare sets of canonical value triples and also assert the returned list itself contains no duplicate rows.

## 3. Level 1 — Fundamental Pointer Operations

### F1. Reverse a selected range

**Difficulty:** Easy. **Objective:** initialize opposite ends and stop safely at meeting/crossing. **Prerequisites:** array indexing, swaps, half-open ranges; [pointer fundamentals](./Two_Pointers_Essential_Concepts.md#2-two-pointers-fundamentals).

**Problem and contract:** Reverse `values[from..to)` in place. Input may be in any order and contain any ints, including duplicates. Require `0 <= from <= to <= values.length`. Return nothing. Positions outside the range must retain their original values; inside it, the exact reverse is required. No original indices are returned and no new output array is allowed.

```java
public static void reverseRange(int[] values, int from, int to) {
    throw new UnsupportedOperationException("TODO F1");
}
```

**Target:** O(k) time for k = to−from; O(1) auxiliary space; no allocated output.

| Case | Input arguments | Expected full array after call |
| --- | --- | --- |
| Selected middle | `[[9,1,2,3,8],1,4]` | `[9,3,2,1,8]` |
| Empty input | `[[],0,0]` | `[]` |
| Empty suffix | `[[1,2],2,2]` | `[1,2]` |
| Singleton | `[[7],0,1]` | `[7]` |
| Two extremes | `[[MIN,MAX],0,2]` | `[MAX,MIN]` |
| Reverse ordered | `[[3,2,1],0,3]` | `[1,2,3]` |
| All equal | `[[4,4,4,4],0,4]` | `[4,4,4,4]` |

In the first example, only `1,2,3` reverse; the two outside values stay fixed. **Complete when:** all tests pass, you explain why the center needs no swap, and your trace never dereferences `to` or a negative index. Compare with a copied-range baseline before claiming constant space.

- [ ] F1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F2. Count mirrored disagreements

**Difficulty:** Easy. **Objective:** distinguish paired obligations from visits to every position. **Prerequisites:** `String.charAt`, integer division, exact character comparison; [Java Strings](./Java_String.md).

**Problem and contract:** Return the number of pairs `(i, n−1−i)` with `i < n−1−i` whose characters differ. Text is ASCII in arbitrary order. Every character, including punctuation and spaces, matters; comparison is case-sensitive. Count each mirrored pair once and ignore an unpaired center. Strings remain unchanged; output is one int, not the mismatching indices.

```java
public static int mirroredDisagreements(String text) {
    throw new UnsupportedOperationException("TODO F2");
}
```

**Target:** O(n) time, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected result |
| --- | --- | --- |
| One failed pair | `["abca"]` | `1` |
| Empty | `[""]` | `0` |
| Center only | `["Z"]` | `0` |
| Case matters | `["Aa"]` | `1` |
| Direct crossing | `["aa"]` | `0` |
| All pairs differ | `["abcd"]` | `2` |
| Punctuation counts | `["a!"]` | `1` |

For `abca`, the outer `a/a` pair agrees and the inner `b/c` pair disagrees. **Complete when:** the count is at most `n/2`, your loop compares exactly that many pairs, and you explain why frequency counting cannot answer this positional question.

- [ ] F2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F3. Remove one chosen value stably

**Difficulty:** Easy. **Objective:** implement the roadmap's stable removal drill and distinguish read from write. **Prerequisites:** mutation and logical prefixes; [read/write reasoning](./Two_Pointers_Essential_Concepts.md#4-same-direction-readwrite-pointers).

**Problem and contract:** Remove every occurrence of `rejected` from an arbitrary-order int array. Modify the same array and return retained length k. Its first k entries must be exactly the retained original occurrences in their original relative order. The tail is unspecified; do not allocate a replacement output. Any int may be rejected, including zero or an extreme. Equal retained values remain separate occurrences.

```java
public static int removeChosen(int[] values, int rejected) {
    throw new UnsupportedOperationException("TODO F3");
}
```

**Target:** O(n) time, O(1) auxiliary space; scalar length plus mutated prefix.

| Case | Input arguments | Expected length; prefix |
| --- | --- | --- |
| Mixed removals | `[[2,5,2,-1,0,2],2]` | `3; [5,-1,0]` |
| Empty | `[[],2]` | `0; []` |
| Remove everything | `[[2,2],2]` | `0; []` |
| Remove nothing | `[[-1,0,4],2]` | `3; [-1,0,4]` |
| First rejected | `[[2,5],2]` | `1; [5]` |
| Singleton retained | `[[MAX],MIN]` | `1; [MAX]` |
| Extreme duplicates | `[[MIN,0,MIN,MAX],MIN]` | `2; [0,MAX]` |

In the first row, 5 precedes −1 because that was their original order. Values beyond index 2 are irrelevant. **Complete when:** your invariant identifies the valid prefix and unread region, and you can show why rejecting an item advances only the inspection role. Explain the cost of repeatedly shifting a suffix as a baseline.

- [ ] F3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F4. Match a required sequence

**Difficulty:** Easy. **Objective:** coordinate indices belonging to different sequences. **Prerequisites:** exact string comparison and [two-sequence scans](./Two_Pointers_Essential_Concepts.md#5-coordinated-scans-across-two-sequences).

**Problem and contract:** Return whether all characters of `required` can be selected from `available` in their original order, with distinct increasing positions. Selected positions need not be adjacent. Both ASCII strings are case-sensitive, may contain repeats/punctuation, and have no sortedness assumption. Empty required text always succeeds. Preserve both strings; return only a boolean.

```java
public static boolean sequenceAvailable(String required, String available) {
    throw new UnsupportedOperationException("TODO F4");
}
```

**Target:** O(n+m) time for the two lengths, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Gaps allowed | `["ace","abcde"]` | `true` |
| Wrong order | `["aec","abcde"]` | `false` |
| Both empty | `["",""]` | `true` |
| Nothing required | `["","abc"]` | `true` |
| Nothing available | `["a",""]` | `false` |
| Reuse forbidden | `["aa","a"]` | `false` |
| Repeats available | `["aa","aba"]` | `true` |
| Case matters | `["A","a"]` | `false` |

The first row can select positions 0, 2, and 4. **Complete when:** you explain why a mismatch can consume available text but cannot consume a required character, and why comparing the two indices to each other is not an exhaustion test. Sketch a baseline that enumerates possible selections.

- [ ] F4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 4. Level 2 — Core Algorithmic Patterns

For every P task, write the eleven-step reasoning record before opening hints. Completion requires all contract tests, a movement invariant, a termination argument, and justified costs, plus the task-specific check below.

### P1. Find a sorted pair witness

**Difficulty:** Medium. **Objective:** turn ordered elimination into a valid witness. **Prerequisites:** F1, long arithmetic; [opposite-end pointers](./Two_Pointers_Essential_Concepts.md#3-opposite-end-pointers).

**Problem and contract:** Given a nondecreasing array of arbitrary ints and a long target, return any two indices `[i,j]` with `0 <= i < j < n` and mathematical sum equal to target. Return an empty int array if none exists. Equal values at distinct indices are allowed. Preserve the array. Indices refer to the supplied array; do not sort it. Multiple witnesses are acceptable, with increasing indices inside the result.

```java
public static int[] pairAtTarget(int[] sorted, long target) {
    throw new UnsupportedOperationException("TODO P1");
}
```

**Target:** O(n) time, O(1) auxiliary space, O(1) output.

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Tied witnesses | `[[-3,-1,1,4,6],5]` | Any of `[[1,4],[2,3]]` |
| Empty | `[[],0]` | `[]` |
| Cannot reuse center | `[[3],6]` | `[]` |
| Equal values | `[[3,3],6]` | `[0,1]` |
| No solution | `[[1,2,4,8],7]` | `[]` |
| Beyond int sum | `[[MAX,MAX],4294967294]` | `[0,1]` |
| Opposite extremes | `[[MIN,MAX],-1]` | `[0,1]` |
| Target outside pair range | `[[0,0],9223372036854775807]` | `[]` |

For the first row, both −1+6 and 1+4 equal 5. **Complete when:** your absence result agrees with pair enumeration on small inputs and your proof covers negative and duplicate values. **Interviewer:** which entire family of pairs does one too-small sum eliminate?

<details><summary>Hint 1 — observation</summary>

For one endpoint, identify the smallest and largest possible remaining partners.

</details>
<details><summary>Hint 2 — invariant</summary>

Keep all unresolved candidate pairs inside one interval. Use a sorted inequality to eliminate an endpoint that cannot participate in a solution.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Require distinct positions before reading a pair. Promote an operand before adding; compare the full sum directly with the long target.

</details>

- [ ] P1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P2. Verify a filtered mirror

**Difficulty:** Medium. **Objective:** finish mirrored obligations while skipping irrelevant symbols. **Prerequisites:** F2 and ASCII classification; [filtered comparison](./Two_Pointers_Essential_Concepts.md#34-filtered-palindrome-checking-finish-mirrored-obligations).

**Problem and contract:** Return whether the ASCII letters/digits in `text`, after lowercasing, form a palindrome. Ignore other ASCII code units. Input order matters and must not be sorted; repeated letters/digits are allowed. Preserve the string, allocate no cleaned string/array, and return a boolean. Zero or one retained symbol is a palindrome.

```java
public static boolean filteredMirror(String text) {
    throw new UnsupportedOperationException("TODO P2");
}
```

**Target:** O(n) time, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Mixed case and separators | `["A, b-a"]` | `true` |
| Empty | `[""]` | `true` |
| Fully ignored | `["!? --"]` | `true` |
| Retained center | `["-7-"]` | `true` |
| Direct crossing | `["aA"]` | `true` |
| Meaningful mismatch | `["0P"]` | `false` |
| Digits included | `["1 a 1"]` | `true` |
| Same counts, wrong order | `["aabb"]` | `false` |

`A, b-a` retains `aba`. **Complete when:** every dereference is protected even on fully ignored input, and you can count total movement despite nested skip loops. **Interviewer:** why may punctuation be skipped while a meaningful mismatch ends the check?

<details><summary>Hint 1 — observation</summary>

The next obligation is between the outermost retained symbols, not necessarily the current raw endpoints.

</details>
<details><summary>Hint 2 — invariant</summary>

All consumed meaningful symbols have matched their partners. Skipping an ignored symbol creates no new obligation.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Check bounds before `charAt`, including inside skips. After skips, recheck meeting/crossing; after a successful comparison, consume both symbols.

</details>

- [ ] P2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P3. Cap each sorted run

**Difficulty:** Medium. **Objective:** adapt deduplication to a multiplicity contract. **Prerequisites:** F3, sorted runs; [read/write patterns](./Two_Pointers_Essential_Concepts.md#4-same-direction-readwrite-pointers).

**Problem and contract:** In a nondecreasing int array, retain at most `cap` occurrences of each value, where `0 <= cap <= n+1`. Mutate the original array and return k. Its first k entries must be the retained occurrences in original relative order; the tail is unspecified. Cap zero removes everything. Preserve the first permitted occurrences conceptually, although equal ints have indistinguishable values. Do not allocate another array.

```java
public static int capSortedRuns(int[] values, int cap) {
    throw new UnsupportedOperationException("TODO P3");
}
```

**Target:** O(n) time, O(1) auxiliary space; scalar length and prefix.

| Case | Input arguments | Expected length; prefix |
| --- | --- | --- |
| Cap two | `[[-2,-2,-2,0,0,5],2]` | `5; [-2,-2,0,0,5]` |
| Cap one | `[[-2,-2,-2,0,0,5],1]` | `3; [-2,0,5]` |
| Cap zero | `[[1,1,2],0]` | `0; []` |
| Empty | `[[],1]` | `0; []` |
| No suppression | `[[7],2]` | `1; [7]` |
| One run | `[[4,4,4,4],2]` | `2; [4,4]` |
| Extreme run boundaries | `[[MIN,MIN,0,MAX,MAX],1]` | `3; [MIN,0,MAX]` |

Only the third −2 is discarded in the first row. **Complete when:** cap zero never causes an invalid reference, and your invariant explains both acceptance and write safety. Explain why the same rule cannot globally cap unsorted `[1,2,1]` without reconsidering state.

<details><summary>Hint 1 — observation</summary>

All occurrences of a value form one contiguous run; history from earlier runs is unnecessary.

</details>
<details><summary>Hint 2 — invariant</summary>

Maintain the valid output prefix and enough state to know how many copies of the current run it already contains.

</details>
<details><summary>Hint 3 — implementation clue</summary>

You can track the current run's accepted count explicitly. Alternatively reason about an output position `cap` places behind write, but guard that position and handle cap zero first.

</details>

- [ ] P3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P4. Keep shared occurrences

**Difficulty:** Medium. **Objective:** coordinate sorted sources without losing multiplicities. **Prerequisites:** F4, sorted comparisons; [two-sequence scans](./Two_Pointers_Essential_Concepts.md#5-coordinated-scans-across-two-sequences).

**Problem and contract:** Given two nondecreasing int arrays, return a new ascending array containing each value exactly `min(count in first, count in second)` times. Both inputs are read-only; they may be empty, contain duplicates, or even refer to the same array. The returned array must be fresh and have exactly the answer length. Return values, not original indices; output order is ascending.

```java
public static int[] commonOccurrences(int[] first, int[] second) {
    throw new UnsupportedOperationException("TODO P4");
}
```

**Target:** O(n+m) time, O(1) auxiliary space, O(k) output for k matches. An oversized buffer followed by a trim has extra auxiliary storage and misses this target.

| Case | Input arguments | Expected array |
| --- | --- | --- |
| Unequal multiplicities | `[[-2,1,1,1,4],[1,1,2,4,4]]` | `[1,1,4]` |
| Both empty | `[[],[]]` | `[]` |
| One empty | `[[],[1,1]]` | `[]` |
| Disjoint | `[[1,3],[2,4]]` | `[]` |
| Equal runs | `[[2,2,2],[2,2]]` | `[2,2]` |
| Extreme matches | `[[MIN,0,MAX],[MIN,MAX]]` | `[MIN,MAX]` |
| Equal contents | `[[1,2],[1,2]]` | `[1,2]` |

There are two shared 1 occurrences and one shared 4 in the first row. Also run the last row using the **same reference** for both arguments and check that the output is a different object. **Complete when:** a smaller head is discarded only with a proof, matching consumes the correct occurrences, and output allocation is included honestly.

<details><summary>Hint 1 — observation</summary>

A value smaller than the other sequence's smallest remaining value has no possible partner there.

</details>
<details><summary>Hint 2 — invariant</summary>

Consume unmatched smaller heads; consume one occurrence from both sources on equality. The result covers exactly the matches from the consumed prefixes.

</details>
<details><summary>Hint 3 — implementation clue</summary>

The inputs remain available. A counting pass can determine the exact output length; reset both source indices for a second pass that fills it.

</details>

- [ ] P4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P5. Enumerate unique target triples

**Difficulty:** Medium. **Objective:** reduce a combination search and suppress duplicate values at the right level. **Prerequisites:** P1, lists, sorting; [fixed-anchor reasoning](./Two_Pointers_Essential_Concepts.md#6-fixed-anchor-plus-two-pointers).

**Problem and contract:** Given up to 3,000 arbitrary-order ints and any long target, return every distinct value triple obtainable from three distinct input positions whose sum equals target. Each returned row contains exactly three values in nondecreasing order. Rows may appear in any order, but no value triple may repeat. Equal values within a triple are allowed only when enough input occurrences exist. You may reorder `values` in place, including sorting it; preserve its multiset. Original indices are not required. Empty/fewer-than-three inputs return an empty list.

```java
public static java.util.List<java.util.List<Integer>> targetTriples(
        int[] values, long target) {
    throw new UnsupportedOperationException("TODO P5");
}
```

**Target:** O(n²+n log(n+1)+q) time, O(1+S_sort(n)) auxiliary space, O(q) returned output for q triples. Build directly into the returned list; report any additional temporary result collection if you use one.

| Case | Input arguments | Expected rows, in any order |
| --- | --- | --- |
| Several triples | `[[2,-2,0,1,-1,2,-1],0]` | `[[-2,0,2],[-1,-1,2],[-1,0,1]]` |
| Empty | `[[],0]` | `[]` |
| Too short | `[[1,2],3]` | `[]` |
| Repeated values needed | `[[-1,-1,2],0]` | `[[-1,-1,2]]` |
| One value result | `[[0,0,0,0],0]` | `[[0,0,0]]` |
| No solution | `[[1,2,3],7]` | `[]` |
| Three-term widening | `[[MIN,0,MAX],-1]` | `[[MIN,0,MAX]]` |
| Extreme target match | `[[MIN,MIN,MIN],-6442450944]` | `[[MIN,MIN,MIN]]` |
| Reverse ordered | `[[3,2,1,0,-1],3]` | `[[-1,1,3],[0,1,2]]` |

The first row includes `[-1,-1,2]` because two different −1 occurrences exist. **Complete when:** small exhaustive triple enumeration produces the same set, no returned row repeats, and you explain why duplicate suppression would be wrong for counting index triples. **Interviewer:** why must the first usable equal anchor survive?

<details><summary>Hint 1 — observation</summary>

Fix one chosen value. What simpler relation remains between the other two values?

</details>
<details><summary>Hint 2 — invariant</summary>

After sorting, process each distinct anchor value once and restrict pair positions to its suffix. Equality must produce one value result and then make progress past redundant partner results.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Compare an anchor with its previous value, not its next value. Keep bounds guards around partner skips. Compare a widened three-term sum directly with target; subtracting from an arbitrary long target can itself overflow.

</details>

- [ ] P5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P6. Maximize a boundary score

**Difficulty:** Medium. **Objective:** justify discarding a dominated boundary. **Prerequisites:** opposite-end bounds and long multiplication; [boundary elimination](./Two_Pointers_Essential_Concepts.md#7-dominated-boundary-elimination).

**Problem and contract:** Each nonnegative int `capacity[i]` belongs to fixed position i. A pair `i < j` has score `(j−i) * min(capacity[i],capacity[j])`. Return the greatest score as long; return 0 for fewer than two positions. Preserve the arbitrary-order array. Duplicates and zeros are valid. Sorting is forbidden because positions define distances. Return only the score, so tied maximizing pairs need no tie-breaking.

```java
public static long bestBoundaryScore(int[] capacity) {
    throw new UnsupportedOperationException("TODO P6");
}
```

**Target:** O(n) time, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected score |
| --- | --- | --- |
| Interior optimum | `[[1,4,3,2]]` | `4` |
| Tied optima | `[[2,7,3,6,4]]` | `12` |
| Empty | `[[]]` | `0` |
| Singleton | `[[7]]` | `0` |
| All zero | `[[0,0,0]]` | `0` |
| Equal boundaries | `[[5,5,5]]` | `10` |
| Two elements | `[[2,9]]` | `2` |
| Long product | `[[MAX,0,MAX]]` | `4294967294` |

In the first row, positions 1 and 3 score `2 * 2 = 4`. **Complete when:** pair enumeration confirms the maximum on small arrays and you can bound every narrower pair using a discarded boundary. **Interviewer:** must the next evaluated score increase? Does the proof require sorted heights?

<details><summary>Hint 1 — observation</summary>

One endpoint limits the usable capacity while both determine distance.

</details>
<details><summary>Hint 2 — invariant</summary>

First record the current score. Show that keeping its limiting endpoint and shrinking distance cannot beat that recorded score.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Advance a limiting endpoint; on equal capacities, justify choosing either one. Widen an operand before multiplying capacity by distance.

</details>

- [ ] P6 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P7. Merge into reserved capacity

**Difficulty:** Medium. **Objective:** choose a write direction that protects unread values. **Prerequisites:** F3 and sorted merging; [coordinated scans](./Two_Pointers_Essential_Concepts.md#5-coordinated-scans-across-two-sequences).

**Problem and contract:** `buffer[0..used)` and the entire `incoming` array are nondecreasing. Require `0 <= used <= buffer.length`, `buffer.length == used + incoming.length`, and distinct array objects. The unused buffer tail contains arbitrary ints, not meaningful values. Mutate the buffer to contain the complete ascending merge of both valid sources, including all duplicate occurrences. Preserve `incoming`. Return nothing; every buffer position is meaningful afterward. Combined length is at most 100,000. No extra output array is allowed and source identity need not be observable for equal ints.

```java
public static void mergeReserved(int[] buffer, int used, int[] incoming) {
    throw new UnsupportedOperationException("TODO P7");
}
```

**Target:** O(used+m) time, O(1) auxiliary space, no allocated output.

| Case | Input arguments | Expected full buffer |
| --- | --- | --- |
| Interleaving and ties | `[[1,4,4,99,99,99],3,[2,4,7]]` | `[1,2,4,4,4,7]` |
| Both empty | `[[],0,[]]` | `[]` |
| First source empty | `[[99,-3],0,[-2,0]]` | `[-2,0]` |
| Second source empty | `[[-1,2],2,[]]` | `[-1,2]` |
| Incoming comes first | `[[3,4,0,0],2,[1,2]]` | `[1,2,3,4]` |
| Extreme values | `[[MAX,8],1,[MIN]]` | `[MIN,MAX]` |
| Equal runs | `[[2,2,7,7],2,[2,2]]` | `[2,2,2,2]` |

The 99s in the first row provide storage only; they are not part of the merge. **Complete when:** every original occurrence appears exactly once, incoming is unchanged, and your invariant proves that writing never destroys a still-needed buffer value. Explain why ordinary forward merge into this same buffer can fail.

<details><summary>Hint 1 — observation</summary>

Free positions are at one end. Consider which final output position can be filled without disturbing the valid prefix.

</details>
<details><summary>Hint 2 — invariant</summary>

Build a correct output suffix by choosing the largest remaining source value. Track the unconsumed valid prefix separately from reserved capacity.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Initialize source positions from `used−1` and `incoming.length−1`, not the physical buffer end. Decide what remains necessary when either source is exhausted.

</details>

- [ ] P7 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 5. Level 3 — NeetCode / LeetCode Practice

The following titles, difficulties, and priorities match the [local roadmap](./DSA_Interview_Roadmap.md#2-two-pointers). **E = Essential; A = Additional; C = Optional advanced challenge.** They are the roadmap's tiers. Open the original statement for its exact current constraints and judge signature; the local exercise interfaces are not substitutes for that contract. Start from a blank Java 21 editor with platform tags and solutions hidden. Record assistance honestly even after an accepted submission.

### N1. Valid Palindrome — Easy — E

[Original problem](https://leetcode.com/problems/valid-palindrome/). **Objective:** turn a character-domain contract into a guarded filtered comparison. **Prerequisites:** F2 and P2. State the platform's character assumptions before choosing APIs. Derive a cleaned-copy baseline and an O(n)-time, O(1)-auxiliary comparison; test ignored-only text, a retained center, mixed case, and a mismatch. Use the platform's valid-input domain when adapting local tests.

**Completion:** accepted Java, no copied solution, a skip-loop invariant, and a cold re-solve if the first attempt was assisted. Explain why nested skips do not multiply the scan cost.

- [ ] N1 — **E** attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### N2. Two Sum II - Input Array Is Sorted — Medium — E

[Original problem](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/). **Objective:** exploit supplied ordering and return the judge's required indices. **Prerequisites:** P1. Translate zero-based local witnesses into the original statement's index convention. Use its existence/uniqueness guarantees, not an invented no-solution requirement. Derive O(n) time and O(1) auxiliary space; explain distinct positions, duplicate values, and safe arithmetic under the actual bounds.

**Completion:** accepted Java plus a proof of both inequality branches, a two-element test, and a trace ending at the correct pair. Explain what fails if the sortedness precondition disappears.

- [ ] N2 — **E** attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### N3. 3Sum — Medium — E

[Original problem](https://leetcode.com/problems/3sum/). **Objective:** enumerate unique value results using anchors and justified duplicate suppression. **Prerequisites:** P5. Derive the cubic baseline, then O(n²) scanning plus sorting/output work. Clarify whether your practice implementation may mutate the supplied array; if you choose preservation, count the copy. Report sort workspace and O(q) output, not just the scalar pointer state.

**Completion:** accepted Java and checks for repeated anchors, repeated partner values, a triple requiring two equal values, and no solution. Explain why counting index triples would need different duplicate handling. Do not use platform acceptance as evidence of unfamiliar recognition.

- [ ] N3 — **E** attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### N4. Container With Most Water — Medium — A

[Original problem](https://leetcode.com/problems/container-with-most-water/). **Objective:** prove dominated-boundary elimination. **Prerequisites:** P6. Begin with all index pairs, then justify the O(n)-time, O(1)-auxiliary improvement. Keep original geometry intact; use the platform's bounds to justify the result type. Test equal boundaries, zeros where permitted, and an optimum away from the initial pair.

**Completion:** accepted Java and a bound on every alternative using a discarded boundary. Explain why moving the taller endpoint has no equivalent guarantee. This problem is Additional, but the underlying core boundary proof still needs evidence through P6 or another suitable task.

- [ ] N4 — **A** attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### N5. Trapping Rain Water — Hard — C

[Original problem](https://leetcode.com/problems/trapping-rain-water/). **Objective:** settle individual contributions from two-sided maxima. **Prerequisites:** reliable core patterns and [optional boundary-maximum reasoning](./Two_Pointers_Essential_Concepts.md#8-optional-advanced-boundary-maximum-reasoning). First derive a correct preprocessing solution with O(n) time and O(n) auxiliary storage. If that proof is secure, investigate the O(n)-time, O(1)-auxiliary alternative. A stack solution is not required for this workbook.

**Completion, if attempted:** accepted Java, a per-position depth argument, an explicit settlement invariant, and tests for monotone heights, equal walls, zeros, and separated basins under the original constraints. Explain why this computes a different quantity from N4. **This optional Hard never blocks ordinary Two Pointers readiness.**

- [ ] N5 — **C** attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 6. Level 4 — Unfamiliar Interview Challenges

Hide the hints and do not infer the algorithm from this workbook's topic. For each X task, compare at least two plausible approaches, reject one using its cost or a concrete counterexample, and explain how the contract drives the choice. The required knowledge is limited to material already studied. Targets constrain the final design; they do not excuse an unjustified movement.

### X1. Two crates for dispatch

**Difficulty:** Medium. **Objective:** derive an efficient count without enumerating every accepted result. **Prerequisites:** arrays, inequalities, counting, and safe long arithmetic.

**Problem and contract:** `masses` contains nonnegative int masses in nondecreasing order. Two different crate positions can be dispatched together when their combined mass is at most nonnegative long `limit`. Return the number of valid unordered position pairs. Duplicate masses represent different crates, so their position combinations count separately. Preserve the array and return a long count; no pair list or original-index witness is required.

```java
public static long countDispatches(int[] masses, long limit) {
    throw new UnsupportedOperationException("TODO X1");
}
```

**Target:** O(n) time, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected count |
| --- | --- | --- |
| Some pairs fit | `[[1,2,2,4],4]` | `3` |
| Empty | `[[],0]` | `0` |
| Singleton | `[[0],0]` | `0` |
| Equal zero crates | `[[0,0,0],0]` | `3` |
| No pair fits | `[[3,4],6]` | `0` |
| Exact boundary | `[[3,4],7]` | `1` |
| All pairs fit | `[[1,2,3,4],100]` | `6` |
| Widened mass | `[[MAX,MAX],4294967294]` | `1` |
| Large count | `[100000 zero entries,0]` | `4999950000` |

In the first row, the 1 can pair with either 2, and the two 2s can pair with each other. **Complete when:** brute enumeration agrees on short arrays, each pair is counted once, and you prove both your bulk counting step and your elimination step. **Interviewer:** which duplicates must remain distinct, and why does the count require long even for zero masses?

<details><summary>Hint 1 — observation</summary>

If the largest remaining partner fits with one crate, what can you conclude about all smaller remaining partners?

</details>
<details><summary>Hint 2 — direction and optional theory</summary>

Count a disjoint block of partners at once, then remove the crate whose entire block is resolved. Compare with the counting proof in [fixed-anchor reasoning](./Two_Pointers_Essential_Concepts.md#6-fixed-anchor-plus-two-pointers), but this task has only two choices and no anchor loop.

</details>
<details><summary>Hint 3 — implementation clue</summary>

For a fitting endpoint pair, the block size is their index distance. Otherwise use the smallest remaining partner to rule out one endpoint. Use long for both sum and count.

</details>

- [ ] X1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X2. Receipts from the archive

**Difficulty:** Medium. **Objective:** honor original positions and deterministic tie-breaking under a time target. **Prerequisites:** the shared workbook prerequisites and integer bounds.

**Problem and contract:** `amounts` is an arbitrary-order array of ints. Return `[i,j]` with `i < j` and sum equal to long target. Among all valid pairs choose the **smallest j**, then the **smallest i** for that j. Return an empty array when none exists. Target lies between −4294967296 and 4294967294 inclusive. Duplicate amounts and negative amounts are valid. The original array must remain unchanged; returned positions refer to that original array. Do not sort away its chronology.

```java
public static int[] firstReceiptPair(int[] amounts, long target) {
    throw new UnsupportedOperationException("TODO X2");
}
```

**Target:** expected O(n) time, O(n) auxiliary space, O(1) output. Explain the expectation assumption.

| Case | Input arguments | Expected pair |
| --- | --- | --- |
| Earliest completion | `[[10,4,6,0],10]` | `[1,2]` |
| Empty | `[[],0]` | `[]` |
| No self-use | `[[3],6]` | `[]` |
| First duplicate occurrence | `[[3,3,3],6]` | `[0,1]` |
| Earliest partner tie | `[[4,4,2],6]` | `[0,2]` |
| Nonadjacent negatives | `[[-5,8,2],-3]` | `[0,2]` |
| No solution | `[[3,2,4],8]` | `[]` |
| Sorting would change positions | `[[40,10,30],40]` | `[1,2]` |
| Extreme sum | `[[MAX,MAX],4294967294]` | `[0,1]` |
| Opposite extremes | `[[MIN,MAX],-1]` | `[0,1]` |

In the first row, `[1,2]` finishes before `[0,3]`, despite its larger first index. **Complete when:** an exhaustive oracle ordered by j and then i agrees, the input remains unchanged, and you explain why sorting bare values cannot provide the required answer. **Interviewer:** what information would a plain set lose?

<details><summary>Hint 1 — observation</summary>

When a receipt arrives, only earlier positions can be its partner. Which earlier position should survive if an amount repeats?

</details>
<details><summary>Hint 2 — direction and optional theory</summary>

Visit possible finishing positions in increasing order and retain the first position for each earlier amount. Revisit [complement lookup](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup) if needed.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Look up the complement before recording the current receipt. Long arithmetic/keys can represent a complement outside the int range; under the stated target bounds that subtraction fits long. Do not overwrite an earlier index for an existing amount.

</details>

- [ ] X2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X3. Packing the daily dispatch

**Difficulty:** Medium. **Objective:** preserve arrival order while enforcing a global per-label allowance. **Prerequisites:** the shared workbook prerequisites and array mutation.

**Problem and contract:** `labels` contains arbitrary-order int labels, including negatives and duplicates. Retain only the first `cap` occurrences of each distinct label, where `0 <= cap <= n+1`. Pack retained entries into the same array, preserve their original relative order, and return the logical length k; its tail is unspecified. You may keep state per distinct label but may not allocate a second n-element output array. Labels are values, not returned original indices. No sortedness is supplied.

```java
public static int packFirstCopies(int[] labels, int cap) {
    throw new UnsupportedOperationException("TODO X3");
}
```

**Target:** expected O(n) time, O(d+1) auxiliary space for d distinct labels; scalar length plus prefix.

| Case | Input arguments | Expected length; prefix |
| --- | --- | --- |
| Separated repeats | `[[4,1,4,2,1,4,1],2]` | `5; [4,1,4,2,1]` |
| First occurrences only | `[[4,1,4,2,1,4,1],1]` | `3; [4,1,2]` |
| Empty | `[[],1]` | `0; []` |
| Zero allowance | `[[1,2,1],0]` | `0; []` |
| Already distinct | `[[3,2,1],1]` | `3; [3,2,1]` |
| One label | `[[7,7,7],2]` | `2; [7,7]` |
| Extreme labels | `[[MIN,MAX,MIN,0,MAX],1]` | `3; [MIN,MAX,0]` |

The final two occurrences in the first row exceed their labels' allowances. **Complete when:** an independent occurrence counter/filter agrees, the retained order is exact, and you separately prove acceptance and safe mutation. **Interviewer:** why is remembering only the most recently accepted label insufficient? What contract would make sorting acceptable?

<details><summary>Hint 1 — observation</summary>

An earlier occurrence may be separated from the next one by unrelated labels. The most recent value does not summarize that history.

</details>
<details><summary>Hint 2 — direction and optional theory</summary>

Combine per-label counts with a stable output prefix. Compare [frequency counting](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting) and [read/write invariants](./Two_Pointers_Essential_Concepts.md#4-same-direction-readwrite-pointers).

</details>
<details><summary>Hint 3 — implementation clue</summary>

Read each original value before any write, decide whether its allowance remains, and append accepted values. The output cannot grow faster than the consumed input.

</details>

- [ ] X3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X4. Letters at the checkpoint

**Difficulty:** Medium. **Objective:** compare ordered content without materializing normalized copies. **Prerequisites:** ASCII strings, case conversion, and bounds.

**Problem and contract:** Two ASCII messages are equivalent if their retained letters/digits, lowercased and kept in original order, are identical. Return that boolean. Both strings are read-only, have arbitrary order, and may contain repeats. Ignore other ASCII symbols; empty retained sequences are equal. Neither sorting nor rearranging symbols is part of the definition. Do not allocate cleaned strings, arrays, or collections.

```java
public static boolean sameCheckpointMessage(String first, String second) {
    throw new UnsupportedOperationException("TODO X4");
}
```

**Target:** O(n+m) time, O(1) auxiliary space, scalar output.

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Separators differ | `["A-1 b!","a1B"]` | `true` |
| Empty sequences | `["","---"]` | `true` |
| Both ignored | `["!?"," -- "]` | `true` |
| Same counts, wrong order | `["ab","ba"]` | `false` |
| One extra symbol | `["a","a2"]` | `false` |
| Ignored suffix | `["a!!!","A"]` | `true` |
| Duplicate count differs | `["aa","a"]` | `false` |
| Digit mismatch | `["0","O"]` | `false` |

Both messages in the first row retain `a1b`. **Complete when:** your exhaustion rule handles ignored suffixes and one-sided remaining content, and you explain why a frequency map could accept a wrong order. **Interviewer:** what does each consumed prefix guarantee, and which positions are allowed to advance after a mismatch?

<details><summary>Hint 1 — observation</summary>

Only the next retained symbol from each message can satisfy the next equality obligation.

</details>
<details><summary>Hint 2 — direction and optional theory</summary>

Advance independently over ignored symbols in each input. Compare their next meaningful symbols in original order. Use [coordinated-scan reasoning](./Two_Pointers_Essential_Concepts.md#5-coordinated-scans-across-two-sequences), not a frequency summary.

</details>
<details><summary>Hint 3 — implementation clue</summary>

After both skip phases, check each index against its own string length. If one message has ended, decide whether the other still has a meaningful symbol before returning.

</details>

- [ ] X4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 7. Complexity Analysis Exercises

Analyze the code **as written**, then discuss an improvement that preserves its output. These are analysis exercises, so the snippet supplies the method signature and behavior; your deliverable is a written analysis, not another completed implementation. The tables are executable behavior checks, not complexity answers. Answers are separated from the questions in Section 12.

For **each C exercise**, give (1) worst-case time, preferably a tight bound, (2) peak live auxiliary space and separate output storage, (3) an operation-count explanation, (4) a possible optimization or a reason none improves the relevant bound, and (5) assumptions affecting the result. State best-case differences when early exhaustion matters. Complete it only after tracing an ordinary and boundary input and answering all five parts. Use n/m for independent lengths; do not silently assume they are equal. Ignore the test harness's memory.

Assume ordinary Java 21 arrays/flat strings, constant-time primitive operations/indexing, and feasible allocations. For repeated immutable construction, analyze character-copy/allocation work rather than assuming an unspecified JIT transformation removes it. For memory, count reachable live working objects, not all unreclaimed allocations since execution began. Explain cumulative allocation separately where useful. The [complexity guide](./Complexity/Complexity_Analysis_and_Foundations.md), [pointer cost discussion](./Two_Pointers_Essential_Concepts.md#10-deriving-time-and-space-complexity), and [string costs](./Java_String.md#8-complexity-expectations-for-dsa) are review references after the first attempt.

### C1. Independent source positions

**Difficulty:** Easy. **Objective:** count progress across two independent inputs. **Prerequisites:** loop counting and array bounds. **Contract:** arbitrary-order int arrays of lengths n and m, each at most 100,000; duplicates allowed; both read-only. Return the long sum of all occurrences from both arrays. No output ordering or original-index witness applies. Under these bounds the sum fits long.

```java
public static long sourceTotal(int[] first, int[] second) {
    int i = 0;
    int j = 0;
    long total = 0;
    while (i < first.length || j < second.length) {
        if (i < first.length) total += first[i++];
        if (j < second.length) total += second[j++];
    }
    return total;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Unequal lengths | `[[1,2],[4]]` | `7` |
| Both empty | `[[],[]]` | `0` |
| One source empty | `[[],[-1,0,2]]` | `1` |
| Wide accumulation | `[[MAX],[MAX]]` | `4294967294` |

The first row adds three occurrences, despite two indices appearing in one loop. **Analysis target:** derive time and auxiliary space yourself; explain how `max(n,m)` iterations relate to the number of reads. Can a general exact sum avoid inspecting an arbitrary unseen element?

- [ ] C1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C2. Nested skips with shared progress

**Difficulty:** Medium. **Objective:** separate syntactic nesting from repeated traversal. **Prerequisites:** ASCII filtering, opposite-end bounds, loop sums. **Contract:** arbitrary ASCII text, read-only; repeated symbols allowed. Return how many pairs of retained outer symbols the scan visits. It does not test their equality. Input order is unchanged; output is a scalar count.

```java
public static int retainedPairVisits(String text) {
    int left = 0;
    int right = text.length() - 1;
    int visits = 0;
    while (left < right) {
        while (left < right && !Character.isLetterOrDigit(text.charAt(left))) {
            left++;
        }
        while (left < right && !Character.isLetterOrDigit(text.charAt(right))) {
            right--;
        }
        if (left >= right) break;
        visits++;
        left++;
        right--;
    }
    return visits;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Odd retained count | `["!ab--C?"]` | `1` |
| Empty | `[""]` | `0` |
| Fully ignored | `["---"]` | `0` |
| Retained singleton | `["7"]` | `0` |
| Two pairs | `["a1B2"]` | `2` |

The first row retains three symbols and visits one outer pair. **Analysis target:** derive total time and auxiliary space, including the nested loops; identify a valid input family attaining your worst-case bound. Discuss whether allocating a filtered copy improves either bound.

<details><summary>Hint 1 — observation</summary>

Do either of the indices reset when control returns to the outer loop?

</details>
<details><summary>Hint 2 — counting direction</summary>

Charge every index update to a position it permanently passes. Bound the total charges across all three loops.

</details>
<details><summary>Hint 3 — implementation costs</summary>

Account for scalar state, `charAt`, and classification under ASCII. An index inspected as a stopping point may be read again, but only a bounded number of times per progress step.

</details>

- [ ] C2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C3. Resetting the suffix for each anchor

**Difficulty:** Medium. **Objective:** identify the scope within which pointer travel is monotone. **Prerequisites:** arithmetic sums and fixed-anchor scans. **Contract:** input is a scalar `0 <= n <= 100000`; no array order, mutation, duplicate, or witness requirement applies. Return the number of inner-loop visits. `visits` is long because the possible count exceeds int.

```java
public static long anchorVisits(int n) {
    long visits = 0;
    for (int anchor = 0; anchor + 2 < n; anchor++) {
        int left = anchor + 1;
        int right = n - 1;
        while (left < right) {
            visits++;
            if ((left + right) % 2 == 0) left++;
            else right--;
        }
    }
    return visits;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Three suffix scans | `[5]` | `6` |
| Empty size | `[0]` | `0` |
| One position | `[1]` | `0` |
| Too few for a triple | `[2]` | `0` |
| One visit | `[3]` | `1` |

For n=5 the anchors contribute 3, 2, and 1 visits. **Analysis target:** derive time/auxiliary space of the executed code; then ask whether the scalar result can be computed without performing those visits. Do not transfer that optimization to a real triple search without considering its value-dependent work.

<details><summary>Hint 1 — observation</summary>

Within an anchor, how much does `right−left` change per iteration?

</details>
<details><summary>Hint 2 — counting direction</summary>

Compute visits for each anchor, then sum the decreasing suffix lengths. The parity choice does not change the amount of progress.

</details>
<details><summary>Hint 3 — arithmetic clue</summary>

A closed-form arithmetic-series calculation must treat n below 3 correctly and widen before multiplication.

</details>

- [ ] C3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C4. A copy, a sort, and a gap scan

**Difficulty:** Medium. **Objective:** include preprocessing and look for unnecessary work. **Prerequisites:** copying, primitive sorting, algebra of sums. **Contract:** arbitrary-order int array with duplicates; original unchanged. Return the sum of adjacent gaps in an ascending copy, or 0 for fewer than two values. Output is a scalar; no sorted indices are returned.

```java
public static long sortedGapTotal(int[] input) {
    int[] work = input.clone();
    java.util.Arrays.sort(work);
    long total = 0;
    for (int i = 1; i < work.length; i++) {
        total += (long) work[i] - work[i - 1];
    }
    return total;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Unordered values | `[[-2,7,1]]` | `9` |
| Empty | `[[]]` | `0` |
| Singleton | `[[3]]` | `0` |
| All equal | `[[4,4,4]]` | `0` |
| Reverse order | `[[3,2,1]]` | `2` |
| Extreme gap | `[[MAX,MIN]]` | `4294967295` |

The first row has gaps 3 and 6. **Analysis target:** derive complete time and auxiliary space using `S_sort(n)`; distinguish the scan's cost from the whole method. Can the exact scalar be obtained from less information than a fully sorted sequence?

<details><summary>Hint 1 — observation</summary>

Write out a sum of three consecutive sorted gaps and cancel repeated terms.

</details>
<details><summary>Hint 2 — counting direction</summary>

The clone, sort, and scan are sequential phases. The clone remains live during sorting and is not returned output.

</details>
<details><summary>Hint 3 — implementation costs</summary>

Account for Java's sort workspace without asserting a universal constant-space guarantee. Any replacement must preserve the original and widen subtraction before computing an extreme gap.

</details>

- [ ] C4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C5. Assemble alternating endpoints

**Difficulty:** Medium. **Objective:** distinguish a linear number of moves from the cost per move. **Prerequisites:** immutable strings, concatenation, output storage. **Contract:** arbitrary ASCII string, duplicates included, read-only. Return a new value containing last character, first character, next-last, next-first, and so on, using each position exactly once. The exact resulting order matters; no sorting is permitted. The empty result need not be a distinct String object.

```java
public static String endpointText(String text) {
    String result = "";
    int left = 0;
    int right = text.length() - 1;
    while (left <= right) {
        result += text.charAt(right--);
        if (left <= right) result += text.charAt(left++);
    }
    return result;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Even length | `["abcd"]` | `"dacb"` |
| Odd length | `["abc"]` | `"cab"` |
| Empty | `[""]` | `""` |
| Singleton | `["Z"]` | `"Z"` |
| Duplicate symbols | `["aabb"]` | `"baba"` |

`abcd` contributes d, a, c, then b. **Analysis target:** report time, peak live auxiliary storage, output storage, and cumulative copied characters. Discuss an implementation change that improves time while producing the same sequence; include its conversion to the returned String.

<details><summary>Hint 1 — observation</summary>

How many existing characters must survive when a new immutable result gains one character?

</details>
<details><summary>Hint 2 — counting direction</summary>

Sum the sizes of successive result values. Do not confuse that sum with the largest simultaneously live working data.

</details>
<details><summary>Hint 3 — implementation clue</summary>

A mutable builder or character buffer can fill the required order. Its working storage and the final String are separate objects for the space analysis.

</details>

- [ ] C5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C6. Measure each remaining slice

**Difficulty:** Medium. **Objective:** expose hidden linear library operations inside a monotone scan. **Prerequisites:** substring bounds, array conversion, peak versus cumulative allocation. **Contract:** arbitrary ASCII string, read-only, repetitions allowed. Return the sum of remaining slice lengths as both endpoints move inward. The slices are temporary, not required output; output is a long scalar. No sorting or original-index output applies.

```java
public static long sliceVolume(String text) {
    long total = 0;
    for (int left = 0, right = text.length(); left < right; left++, right--) {
        total += text.substring(left, right).toCharArray().length;
    }
    return total;
}
```

| Case | Input arguments | Expected result |
| --- | --- | --- |
| Even length | `["abcd"]` | `6` |
| Odd length | `["abcde"]` | `9` |
| Empty | `[""]` | `0` |
| Singleton | `["x"]` | `1` |
| Two symbols | `["ab"]` | `2` |

The first row measures lengths 4 and 2. **Analysis target:** include both library operations; assess time and auxiliary space even if the first full-range substring is returned without copying. Propose an improvement using only boundaries, then consider a closed form. Do not run enormous quadratic allocations merely to infer the bound.

<details><summary>Hint 1 — observation</summary>

Does measuring a slice require actually constructing its characters?

</details>
<details><summary>Hint 2 — counting direction</summary>

The measured lengths decrease by two. `toCharArray()` still allocates/copies even when a full-range substring is reused.

</details>
<details><summary>Hint 3 — implementation clue</summary>

Replace materialization with a length expression first. For a closed form, count iterations separately for odd/even n and widen the product.

</details>

- [ ] C6 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 8. Debugging Exercises

Each snippet below contains a reproducible defect; a snippet may contain more than one. **Do not use them as templates.** The stated contract describes the required behavior, not what the buggy method actually does. The visible tests show correct expected outputs. First find a failing input yourself; reveal hints only after recording a concrete trace.

For **every D exercise**, identify all defects, construct a minimal witness for each, explain the first point where correctness/progress breaks, repair the code in your scratch class, state the corrected invariant, and derive corrected time/auxiliary/output costs. Run the supplied regression cases and a case that separates two plausible diagnoses. Completion requires an observed failure followed by a passing repair and explanation, not merely spotting a suspicious line. Keep a step limit or external timeout when investigating nontermination.

### D1. A search that misses a valid pair

**Difficulty:** Medium. **Objective:** check each inequality branch against the ordering proof. **Prerequisites:** sorted pair reasoning. **Contract:** read-only nondecreasing arbitrary-int array, duplicates allowed, any long target; return whether a pair of distinct positions sums to target. No witness ordering applies. **Corrected target:** O(n) time, O(1) auxiliary space, scalar output.

```java
public static boolean wrongDirection(int[] a, long target) {
    int left = 0;
    int right = a.length - 1;
    while (left < right) {
        long sum = (long) a[left] + a[right];
        if (sum == target) return true;
        if (sum < target) right--;
        else left++;
    }
    return false;
}
```

| Case | Input arguments | Correct expected result |
| --- | --- | --- |
| Interior partner | `[[1,2,4,8],10]` | `true` |
| Other inequality branch | `[[1,2,4,8],6]` | `true` |
| Empty | `[[],0]` | `false` |
| Equal values | `[[3,3],6]` | `true` |
| Impossible | `[[1,2],9]` | `false` |

The first row has 2+8. **Repair check:** for each discarded endpoint, prove that *all* its unresolved partners fail, not merely that the next sum might move in a desired direction.

<details><summary>Hint 1 — observation</summary>

Try a valid pair that uses one current boundary and an interior element.

</details>
<details><summary>Hint 2 — invariant</summary>

An unresolved interval must still contain a solution if one exists. Write the inequality for every partner of the endpoint you remove.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

For `[1,2,4,8]`, target 10, the initial sum is 9. The next state removes the 8 required by the valid pair. Target 6 exposes the other wrong branch.

</details>

- [ ] D1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D2. A comparison that never finishes

**Difficulty:** Medium. **Objective:** prove progress on every non-returning branch. **Prerequisites:** filtered ASCII comparison and guarded access. **Contract:** same filtering/equality rules as P2, arbitrary-order ASCII text, read-only; return a boolean and terminate on every valid input. **Corrected target:** O(n) time, O(1) auxiliary space, scalar output.

```java
public static boolean stalledMirror(String text) {
    int left = 0;
    int right = text.length() - 1;
    while (left < right) {
        while (left < right && !Character.isLetterOrDigit(text.charAt(left))) left++;
        while (left < right && !Character.isLetterOrDigit(text.charAt(right))) right--;
        if (left >= right) return true;
        if (Character.toLowerCase(text.charAt(left))
                != Character.toLowerCase(text.charAt(right))) return false;
    }
    return true;
}
```

| Case | Input arguments | Correct expected result |
| --- | --- | --- |
| Matching pair | `["aa"]` | `true` |
| Matching after skips | `["!A-a?"]` | `true` |
| Mismatch | `["ab"]` | `false` |
| Fully ignored | `["---"]` | `true` |
| Empty | `[""]` | `true` |

The first row has one successful mirrored obligation and should finish. **Repair check:** trace the state after a successful comparison and demonstrate a strictly decreasing unresolved measure. Re-run empty/ignored inputs so a progress fix does not introduce an out-of-bounds access.

<details><summary>Hint 1 — observation</summary>

Which branches return, and which branch reaches the loop guard again?

</details>
<details><summary>Hint 2 — invariant</summary>

Correctness of a comparison does not establish termination. A completed obligation must become consumed work.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

On `aa`, both indices and both symbols are identical at the start of every iteration. Confirm repeated state with a bounded trace or process timeout.

</details>

- [ ] D2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D3. Deduplication's first output slot

**Difficulty:** Medium. **Objective:** distinguish an empty output prefix from one with a valid previous value. **Prerequisites:** sorted compaction and array bounds. **Contract:** mutate a nondecreasing int array to keep one occurrence per distinct value in ascending prefix order; return k, ignore the tail. Any ints/duplicates allowed; no replacement array. **Corrected target:** O(n) time, O(1) auxiliary space; scalar length and prefix.

```java
public static int missingFirstCase(int[] a) {
    int write = 0;
    for (int read = 0; read < a.length; read++) {
        if (a[read] != a[write - 1]) {
            a[write++] = a[read];
        }
    }
    return write;
}
```

| Case | Input arguments | Correct length; prefix |
| --- | --- | --- |
| Singleton | `[[7]]` | `1; [7]` |
| Empty | `[[]]` | `0; []` |
| Duplicate runs | `[[-1,-1,0,2,2]]` | `3; [-1,0,2]` |
| All equal | `[[MAX,MAX]]` | `1; [MAX]` |

The singleton must become the first output value. **Repair check:** explain which expression becomes legal only after at least one acceptance. Do not assume arbitrary int inputs have a convenient unused sentinel value.

<details><summary>Hint 1 — observation</summary>

What is the valid index range of an output prefix of length zero?

</details>
<details><summary>Hint 2 — invariant</summary>

The acceptance condition needs a separate meaning before the first retained value exists. Short-circuit ordering matters.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

On `[7]`, the first iteration reads index −1. Moving a bounds check after that dereference cannot prevent the exception.

</details>

- [ ] D3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D4. A wider destination type

**Difficulty:** Medium. **Objective:** audit distinctness and expression types independently. **Prerequisites:** pair bounds, Java numeric promotion. **Contract:** read-only nondecreasing int array, duplicates allowed, any long target; return whether two distinct indices sum to target. **Corrected target:** O(n) time, O(1) auxiliary space, scalar output. There are two independent defects to demonstrate.

```java
public static boolean wideDestination(int[] a, long target) {
    int left = 0;
    int right = a.length - 1;
    while (left <= right) {
        long sum = (long) (a[left] + a[right]);
        if (sum == target) return true;
        if (sum < target) left++;
        else right--;
    }
    return false;
}
```

| Case | Input arguments | Correct expected result |
| --- | --- | --- |
| Singleton is not a pair | `[[3],6]` | `false` |
| Large positive sum | `[[MAX,MAX],4294967294]` | `true` |
| Large negative sum | `[[MIN,MIN],-4294967296]` | `true` |
| Equal distinct values | `[[3,3],6]` | `true` |
| Empty | `[[],0]` | `false` |

The first row has no two positions; the second has a legitimate sum beyond int. **Repair check:** fixing only the loop condition must still fail an arithmetic witness, and fixing only promotion must still fail a distinctness witness. Explain both repairs' proof obligations.

<details><summary>Hint 1 — observation</summary>

Check whether the loop ever compares an element with itself. Separately inspect the type of the addition before its result is assigned.

</details>
<details><summary>Hint 2 — invariant</summary>

Valid candidate state requires two positions; valid sum reasoning requires the mathematical sum to survive expression evaluation.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

`[3]`, target 6 returns true. On `[MAX,MAX]`, the int addition wraps to −2 before the cast, so the target 4294967294 is missed.

</details>

- [ ] D4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D5. A prefix with the wrong contents

**Difficulty:** Medium. **Objective:** distinguish a correct count from correct in-place writes. **Prerequisites:** F3 and array assignment semantics. **Contract:** arbitrary-order int array, duplicates allowed; remove every chosen value stably, return k, mutate only the same array, and interpret only its first k values. **Corrected target:** O(n) time, O(1) auxiliary space; scalar length and prefix.

```java
public static int reversedWrite(int[] a, int rejected) {
    int write = 0;
    for (int read = 0; read < a.length; read++) {
        if (a[read] != rejected) {
            a[read] = a[write];
            write++;
        }
    }
    return write;
}
```

| Case | Input arguments | Correct length; prefix |
| --- | --- | --- |
| First value rejected | `[[2,5],2]` | `1; [5]` |
| Several retained | `[[0,3,0,4],0]` | `2; [3,4]` |
| Empty | `[[],0]` | `0; []` |
| All retained | `[[1,2],9]` | `2; [1,2]` |
| All removed | `[[0,0],0]` | `0; []` |

The first row returns length 1 even in the buggy code, so a length-only test misses the defect. **Repair check:** assert exact prefix contents and explain source versus destination roles; do not demand any particular tail contents.

<details><summary>Hint 1 — observation</summary>

When read and write differ, which slot currently holds the accepted original value?

</details>
<details><summary>Hint 2 — invariant</summary>

The next output slot must receive the accepted input value, while unread future values remain intact.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

`[2,5]`, rejecting 2, becomes `[2,2]` and claims prefix `[2]`. Its count alone is correct.

</details>

- [ ] D5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D6. Skipping a needed anchor

**Difficulty:** Medium. **Objective:** explain which equal anchor can be omitted and why. **Prerequisites:** fixed-anchor pair reasoning. **Contract:** read-only nondecreasing arbitrary-int array of length at most 3,000, duplicates allowed, any long target. Return whether three distinct positions have the target sum. Output is a boolean, not unique triples or an index count. **Corrected target:** O(n²) time, O(1) auxiliary space, scalar output; sortedness is already supplied.

```java
public static boolean skippedAnchor(int[] a, long target) {
    for (int anchor = 0; anchor + 2 < a.length; anchor++) {
        if (a[anchor] == a[anchor + 1]) continue;
        int left = anchor + 1;
        int right = a.length - 1;
        while (left < right) {
            long sum = (long) a[anchor] + a[left] + a[right];
            if (sum == target) return true;
            if (sum < target) left++;
            else right--;
        }
    }
    return false;
}
```

| Case | Input arguments | Correct expected result |
| --- | --- | --- |
| Two equal values required | `[[-1,-1,2],0]` | `true` |
| All equal | `[[0,0,0,0],0]` | `true` |
| Too short | `[[1,2],3]` | `false` |
| Distinct values | `[[-2,0,2],0]` | `true` |
| Impossible | `[[1,2,3],9]` | `false` |

The first row needs both −1 positions. **Repair check:** preserve the earliest usable equal anchor and explain whether any duplicate skipping is actually necessary for a boolean existence result. Then explain why index-triple counting would impose a different rule.

<details><summary>Hint 1 — observation</summary>

Equal values can occupy distinct positions required by one valid answer.

</details>
<details><summary>Hint 2 — invariant</summary>

Skipping an anchor is safe only if an already processed anchor covered every value possibility it would offer.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

For `[-1,-1,2]`, the only possible anchor is skipped before any pair is tested. Its equal successor has a shorter suffix and cannot replace it.

</details>

- [ ] D6 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D7. Correct values, incorrect provenance

**Difficulty:** Medium. **Objective:** audit mutation and index meaning across preprocessing. **Prerequisites:** sorting, pair reasoning, and maps. **Contract:** arbitrary-order read-only int array, duplicates allowed, long target in the pair-sum range −4294967296 through 4294967294. Return any original indices `[i,j]` with `i < j` and correct sum, or empty if none. Unlike X2, no earliest-finishing tie rule applies. **Corrected target:** expected O(n) time, O(n) auxiliary space, O(1) output; discuss a provenance-preserving sorting alternative and its additional time.

```java
public static int[] lostPositions(int[] values, long target) {
    java.util.Arrays.sort(values);
    int left = 0;
    int right = values.length - 1;
    while (left < right) {
        long sum = (long) values[left] + values[right];
        if (sum == target) return new int[]{left, right};
        if (sum < target) left++;
        else right--;
    }
    return new int[0];
}
```

| Case | Input arguments | Correct expected result |
| --- | --- | --- |
| Original positions matter | `[[40,10,30],40]` | `[1,2]` |
| Order not supplied | `[[3,2,4],6]` | `[1,2]` |
| Equal occurrences | `[[2,2],4]` | `[0,1]` |
| Empty | `[[],0]` | `[]` |
| No answer | `[[3,1],9]` | `[]` |

Every row also requires the input to remain unchanged. In the first row, only original positions 1 and 2 contain 10 and 30. **Repair check:** test both returned indices and preservation. Evaluate two suggested partial repairs: “sort a clone” and “delete the sort.” Construct a counterexample to each before choosing a complete design.

<details><summary>Hint 1 — observation</summary>

Does a sorted index still identify the same receipt in the caller's original array?

</details>
<details><summary>Hint 2 — invariant</summary>

Preserving the original object is separate from preserving provenance. Ordered elimination also needs an order precondition that the caller has not supplied.

</details>
<details><summary>Hint 3 — failure checkpoint</summary>

For `[40,10,30]`, the snippet returns `[0,1]` and mutates the input. Cloning fixes only mutation. Deleting the sort misses 2+4 on `[3,2,4]`, target 6.

</details>

- [ ] D7 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 9. Mock Interview Questions

Use an unattempted X task, or have a partner adapt one contract without revealing the intended technique. The familiar P/N tasks are useful for communication rehearsal, but do not become unfamiliar readiness evidence merely because they are timed. Record the session on the chosen exercise's existing line, including minutes and assistance; put actual readiness evidence in the roadmap's category paragraph. These are session formats, not additional exercises with separate logs.

**Short explanation round, about 10–15 minutes:** choose P1, F3, P5, or P6. Restate the contract, give the brute-force cost, describe the invariant, prove one nontrivial move, and trace a boundary case without executing code. The interviewer selects follow-ups below. No model answer is supplied.

**Implementation round, about 35–45 minutes:** choose one unseen statement with tags/hints closed. Spend a few minutes clarifying assumptions and baseline, derive the improvement, implement in Java 21, test, then explain costs and an alternative. These times are pacing aids, not pass/fail deadlines. If a hint is needed, record H and revisit cold.

| Follow-up | What the learner must address |
| --- | --- |
| What does each index mean before the loop starts? | Boundaries, consumed work, unread data, or output length |
| Which answers disappear when that index moves? | A specific inequality, completed obligation, or dominance bound |
| Why are no required answers skipped? | Preservation of the candidate set or exact output coverage |
| How do equal values affect your move? | Distinct indices, unique values, or occurrence multiplicity |
| What prevents an infinite loop? | Progress on every non-returning branch, including equality |
| Why is this nested loop linear here but quadratic with anchors? | Scope of monotone movement and where positions reset |
| What changes if mutation is prohibited? | Copy/output costs or an alternative representation |
| What changes if original indices or arrival order are required? | Provenance and whether sorting changes the problem |
| Why is hashing less suitable, or preferable, for this exact task? | Needed state, expected costs, memory, and ordering |
| Which Java expression can overflow before assignment? | Operand types and numerical bounds |
| Which test would falsify your invariant fastest? | A minimal witness tied to an actual risk |
| What would change for Unicode code points? | A revised character contract and traversal/storage analysis |

After a mock, identify the weakest dimension: recognition, proof, implementation, edge cases, costs, or communication. Select a drill that targets that gap rather than repeating every task. A successful explanation must be your own reasoning, not a recitation of an opened hint.

## 10. Revision and Spaced Repetition

Use roughly **1, 3, 7, and 14 days after the learning attempt** as flexible review intervals, consistent with the [roadmap's revision process](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition). Bring the next review forward after S or repeated H; lengthen gaps after reliable independent success. Missing a date does not restart the topic.

For a cold re-solve, open only the statement and contract. Hide previous code, hints, pattern mappings, and solutions. Reconstruct the baseline and proof, implement from a blank editor, run risk-focused tests, and explain the final cost. Compare with the earlier attempt afterward. Append the new I/H/S outcome to the **same attempt line** and update its due date/lesson. Do not erase assistance history when a later attempt earns I.

Prioritize unfinished S attempts, repeated H, failed mocks, and due reviews with fragile proof or edge handling. If an exact solution feels memorized, use a different unseen variant to assess transfer. A cold re-solve can support P; repeated unfamiliar success across separate sessions supports R. Keep the roadmap's dashboard as the sole aggregate tracker and the chosen per-problem record as the sole detailed review queue.

## 11. Two Pointers Completion Criteria

These are evidence requirements, not another checkbox dashboard. Update the existing [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) and use the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) when the evidence is earned. Producing this workbook or running its validation does not mark learner progress.

| Milestone | Required evidence |
| --- | --- |
| Level 1 complete | F1–F4 implemented from blank code, all valid tests handled, bounds and read/write/source roles explained; assisted attempts later re-solved independently |
| Level 2 complete | P1–P7 correct and explained; safe movement, duplicate policy, write safety, termination, mutation, and output costs demonstrated; all assisted core mechanisms reproduced cold |
| Theory gate T | Explain the core patterns and their preconditions without notes, including counterexamples to unjustified moves and sorting that changes semantics |
| Implementation gate I | Rebuild the roadmap's sorted pair, stable removal, and no-cleaned-copy palindrome drills; use Level 2 work to demonstrate the broader core concepts |
| Essential practice P | N1–N3 independently solved or cold re-solved in Java 21, tested, and explained; acceptance alone and remembered code are insufficient |
| Move to Additional | Essential reasoning is reliable, or N4 addresses a specific boundary-proof gap; retain N4's A classification and demonstrate the core boundary concept even if this platform problem is deferred |
| Attempt optional Hard | Core mechanisms and bounds are reliable and you want the extension; start N5 from the two-sided depth definition, then derive improvements; deferring it never blocks ordinary readiness |
| Level 4 assessment | Solve mixed statements with hints closed, justify algorithm selection, preserve contract semantics, and explain why a plausible alternative fails or costs more; retry assisted work cold |
| Readiness gate R | Across separate sessions, independently solve unfamiliar sorted/filtered variants and a mixed selection task; justify every move, handle duplicates/crossing/output order, derive costs, and communicate tradeoffs without prompts |

Use C1–C6 to close complexity gaps and D1–D7 to establish reliable debugging, not as a percentage-based shortcut to readiness. A gate is supported by demonstrated skills, not a count of checked boxes. After a conceptual failure on cold review, mark the roadmap category Reviewing and reconsider the affected gate; an independently corrected isolated syntax typo does not invalidate every earlier skill.

## 12. Learning References and Delayed Checks

### Repository references

- [Two Pointers roadmap scope and priorities](./DSA_Interview_Roadmap.md#2-two-pointers), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Two Pointers — Essential Concepts](./Two_Pointers_Essential_Concepts.md), especially [proofs](./Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements), [costs](./Two_Pointers_Essential_Concepts.md#10-deriving-time-and-space-complexity), and [Java decisions](./Two_Pointers_Essential_Concepts.md#11-java-21-implementation-decisions).
- [Java Arrays](./Java_Array.md), including [copying](./Java_Array.md#copying-arrays), and [Java Strings](./Java_String.md), including [complexity expectations](./Java_String.md#8-complexity-expectations-for-dsa).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md) for operation counts and space conventions; this workbook uses the concepts guide's explicit exclusion of required returned output and always reports that output separately.
- [Arrays & Hashing — Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) for complement lookup, frequency state, and processed-prefix reasoning. Use its [exercise workbook](./Arrays_and_Hashing/Arrays_and_Hashing_Exercises.md) only when an earlier prerequisite needs practice.

Java API behavior was checked with Context7 against the official Java SE 21 documentation: [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) specifies ascending primitive sorting; [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) describes UTF-16 indexing and allocated character-array conversion; [Character](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html) supplies classification and simple case conversion. The ASCII contract above determines which symbols these exercises retain. Time/space derivations use the stated cost model and include copying/workspace rather than treating an API call as constant work.

### Complexity checks — reveal only after C1–C6

These are concise cost checks, not model interview answers. Write your own explanation before opening them; mark the attempt H if they supply a missing conceptual step.

<details><summary>C1 check</summary>

There are `max(n,m)` iterations and exactly n+m element reads: Θ(n+m+1) time, Θ(1) auxiliary space, scalar output. General exact summation requires inspecting arbitrary input values, so there is no asymptotic improvement under this access model. Separate loops may simplify presentation but do not change the bound. The long accumulator is safe under the declared length/int bounds.

</details>
<details><summary>C2 check</summary>

Worst-case Θ(n+1) time and Θ(1) auxiliary space, scalar output. Both indices move monotonically and their total travel is bounded by n; nested skips do not restart the scan. ASCII classification/indexing has constant cost. A cleaned copy is unnecessary and would add Θ(n) storage without improving worst-case time. Counting retained symbols in one forward scan and halving the count is another linear-time, constant-space approach to this particular count.

</details>
<details><summary>C3 check</summary>

For n at least 3, visits are `(n−2)+(n−3)+...+1`, so the code has Θ(n²) time and Θ(1) auxiliary space; smaller n takes constant work. Output is one long. This scalar count can instead be calculated as `(n−1)(n−2)/2` when n is at least 3, with a widened product, giving O(1) time/space. That simplification does not remove the value-dependent work of a real fixed-anchor search.

</details>
<details><summary>C4 check</summary>

Cloning costs Θ(n), the documented sorting bound is O(n log(n+1)), and the scan is Θ(n): overall O(n log(n+1)+1), not merely linear. Auxiliary space is O(n+S_sort(n)+1); output is scalar. The gap sum telescopes to maximum minus minimum when nonempty, with 0 for n below 2. A direct min/max scan gives Θ(n+1) time and Θ(1) auxiliary space without mutation. Java's primitive sort is not a universal O(1)-workspace promise.

</details>
<details><summary>C5 check</summary>

There are n appended characters, but successive immutable results have lengths 1 through n. Under the stated flat-string copy model, time and cumulative copied characters are Θ(n²); peak live auxiliary storage is O(n), and required output is Θ(n). Old unreachable results are not all simultaneously live. A suitably sized builder or char buffer gives Θ(n) construction time, with O(n) working storage and Θ(n) final output; final String construction still costs linear work. Empty-input work is constant.

</details>
<details><summary>C6 check</summary>

Lengths are `n,n−2,n−4,...` while positive: Θ(n²) total copying, O(n) peak live auxiliary space, scalar output. A full-range substring shortcut does not avoid `toCharArray` copying and later slices. Summing `right−left` instead gives O(n) time/O(1) auxiliary space. With `k = ceil(n/2)`, the sum is `k * (n−k+1)`, allowing O(1) time/space using a widened product. Cumulative allocation and peak live space are different quantities.

</details>

### Validation scope

The implementation stubs deliberately throw until the learner completes them; no coding solutions or separate solutions file are provided. The executable analysis/debug fragments and test harness use only standard Java 21. Expected results and deliberate failure cases are validated separately from learner work. Validation does not imply any LeetCode submission or completed attempt.

All 15 starter methods, six analysis methods, seven deliberately buggy methods, and the standalone smoke harness compiled with Temurin Java 21 using `javac --release 21 -Xlint:all -Werror`. A temporary validation harness outside the repository passed **7,041 checks**, including all **181 test-table rows**, 800 seeded trials of the analysis methods, all 128 ASCII code units in text checks, and the documented bug witnesses. Direct enumeration, filtering/counting, and independent formulas checked expected results. D2's exact infinite loop was reproduced in a child process and stopped by an external timeout; D4/D7 partial repairs were also shown insufficient. The smoke harness was compiled, not run against unfinished learner stubs.

All **56 local Markdown links**, including heading anchors, were checked. The five platform entries were compared with the roadmap, all 33 attempt records remain unchecked, and all 15 pre-existing workspace files were preserved. Only this workbook was added to the repository; no implementation or solutions file was added.
