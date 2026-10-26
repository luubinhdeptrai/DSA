# Sliding Window — Exercises (Java 21)

## 1. Introduction

This workbook complements [Sliding Window — Essential Concepts](./Sliding_Window_Essential_Concepts.md). The [roadmap's Sliding Window section](./DSA_Interview_Roadmap.md#5-sliding-window) determines required coverage, practice priorities, and readiness. The aim is to derive a method from the contract, maintain its invariant, and explain why the method is appropriate.

There are **34 original exercises**: 17 implementation challenges, 2 validity investigations with coding tasks, 7 complexity investigations, and 8 debugging challenges. There are also **6 curated platform problems**. Multiple methods or scenarios under one ID share one attempt record.

| Level or practice group | IDs | Purpose |
| --- | --- | --- |
| Level 1: fundamentals | F1-F4 | Boundaries, rolling totals, averages, and elementary counts |
| Level 2: fixed-size patterns | W1-W3 | Live frequencies, compound constraints, and inventory matching |
| Level 2: variable-size patterns | V1-V4 | Valid ranges, duplicates, shrinking, and repeated requirements |
| Validity investigations | R1-R2 | Test assumptions and derive an alternative when needed |
| Optional advanced implementation | O1 | Moving extrema using prior Stack reasoning |
| Level 3: curated practice | N1-N6 | Roadmap E/A/C problems |
| Level 4: mixed challenges | X1-X5 | Choose a technique independently |
| Supporting practice | C1-C7, D1-D8 | Total costs, hidden work, and reproducible defects |

Prerequisites are Java arrays/strings, collections, hashing, complexity analysis, Two Pointers, Stack, and the concepts guide. Binary Search is useful background for comparing search methods, but does not supply a universal window rule. No graph, tree, heap, or dynamic-programming implementation is required. V4 develops coverage reasoning; O1, C7, and the platform Hard problems are optional advanced practice.

Read each statement with hints and old code closed. For a cold attempt, use a blank editor and explain your reasoning aloud. Consult the linked theory after a concrete attempt identifies a gap.

### Progress records

All checkboxes start unchecked. Check a box after completing an attempt, testing or investigating its failure, and recording a postmortem. A checked box does **not** mean independent success.

| Result | Meaning from the roadmap |
| --- | --- |
| I | Independently solved, implemented, explained, and tested |
| H | Needed a conceptual hint |
| S | Needed substantial solution review, or could not finish |
| — | Not attempted |

Record the last date, I/H/S result, total minutes including explanation, next review date, and main lesson. Keep any brief attempt history on that same record. Opening a conceptual hint makes the attempt H; substantial help or an unfinished attempt is S.

For N1-N6, use the **existing roadmap problem Log** as the canonical detailed record; the workbook entries point to it rather than creating a second history. Topic-level T/I/P/R remains exclusively in the [roadmap dashboard](./DSA_Interview_Roadmap.md#four-independent-progress-gates). No progress is inferred from this document's creation.

### Navigation

- [Workflow and shared contracts](#2-problem-solving-workflow)
- [Level 1](#3-level-1-fundamentals)
- [Fixed-size practice](#4-level-2-fixed-size-windows)
- [Variable-size practice](#5-level-2-variable-size-windows)
- [Validity investigations](#6-window-validity-and-counterexamples)
- [Optional extrema practice](#7-optional-advanced-extension)
- [Curated problems](#8-level-3-curated-platform-problems)
- [Mixed challenges](#9-level-4-mixed-interview-challenges)
- [Complexity](#10-complexity-analysis-exercises)
- [Debugging](#11-debugging-challenges)
- [Mocks](#12-mock-interview-practice)
- [Review](#13-revision-and-spaced-repetition)
- [Readiness](#14-completion-and-readiness)
- [References and delayed cost checks](#15-references-and-delayed-complexity-checks)

## 2. Problem-Solving Workflow

Use this fourteen-step process for meaningful exercises, especially V1-V4, R1-R2, and X1-X5:

1. Read and restate the exact output.
2. Identify constraints: contiguity, order, signs, duplicates, alphabet, mutation, ties, and absence.
3. Propose a correct brute-force approach.
4. Analyze that baseline's time and auxiliary space.
5. Identify repeated calculations or lost information.
6. Determine whether a sliding window has a valid movement rule.
7. Define the active range and maintained state, or the state of a better alternative.
8. State the invariant at a precise point in the loop.
9. Derive expansion and removal rules from the condition.
10. Decide when the answer may be recorded or counted.
11. Implement in Java 21.
12. Test ordinary and risk-focused edge cases.
13. Explain initialization, preservation, candidate coverage, and termination.
14. Derive total time and auxiliary space, including preprocessing, validation, allocation, and output work.

For **F2, W1, W3, and V1-V4**, submit a short invariant sheet: what [left,right) contains; what every counter means; state before/after expansion; why each removal is safe; what restores or breaks validity; when the answer changes; why no required candidate is skipped; why the loops terminate; and what bounds total endpoint movement. Trace at least one case with multiple removals.

For X1-X5, first select the method, then describe its state and invariant. Use the window sheet if it fits your choice; otherwise describe the equivalent correctness and progress obligations. For O1, additionally state the meaning of a candidate index, its expiry condition, and the dominance rule.

Use a trace table such as `right | incoming | left before | outgoing actions | state after repair | recorded answer`. A shrinking interval does not prove correctness unless discarded candidates are justified. See [window invariants](./Sliding_Window_Essential_Concepts.md#3-window-boundaries-and-invariants).

### Shared input, output, and cost contracts

Unless an exercise narrows or overrides these rules:

- Inputs and nested rows are non-null, read-only, and unchanged during the call. Preserve the original order. Input arrays and strings have lengths in `0..100_000`; output lengths follow each task's contract.
- Integer elements may be negative, duplicated, or use the full signed int range. Positive/nonnegative restrictions are stated explicitly. `MIN` and `MAX` denote int limits; `LMAX` is `Long.MAX_VALUE`.
- Strings are case-sensitive. Each task states **lowercase English a..z** or **ASCII char values 0..127**. These exercises do not silently assume arbitrary Unicode fits a small frequency array.
- Indices are zero-based. Window outputs use **half-open [from,to)** boundaries. A longest problem that permits an empty result returns (0,0); a required nonempty range with no solution returns (-1,-1), unless specified otherwise.
- Equal-length optimum ranges choose the earliest start. Arrays of answers are returned in the specified input/start order. Duplicate output positions are included only when the contract calls for them.
- `repeat(v,n)` in test notation means n copies of v. Tuple and array displays are mathematical notation, not Java literals. Use `L` on large Java integer literals.
- Auxiliary space excludes input and required returned output, but includes intermediate lists, copies, maps, deques, and call frames. An intermediate ArrayList followed by an int[] conversion still counts as auxiliary storage.
- Hash-based expected bounds require justified hashing/key-cost assumptions. O(n) pointer movement does not excuse an O(n) validity check at every step. Include constant setup work for empty input even when a target is abbreviated as O(n).
- Follow stated preconditions without adding unrelated validation scans. When a task explicitly specifies an exception, test that rejection separately from valid inputs.

Put the member snippets into a class such as `Practice` and add `import java.util.*;`. No external library is needed. The shared output type is:

```java
public record Window(int from, int to) {}
```

Each starter intentionally throws `UnsupportedOperationException`. Replace it in your practice code. No completed implementation is supplied for the original coding tasks. C snippets are analysis subjects; D snippets are intentionally defective repair subjects.

For small inputs, write independent enumeration or scan oracles. Compare primitive arrays element by element, verify input preservation, and validate both range contents and tie rules. For F3, check NaN positions with `Double.isNaN` and compare finite averages using tolerance `1e-9 * max(1,abs(expected))`. Never execute a suspected nonterminating D snippet in the main test runner; use a separate process with a timeout.

## 3. Level 1: Fundamentals

### F1. Enumerate full-window boundaries

**Difficulty:** Easy. **Objective:** calculate legal starts and exclusive ends. **Prerequisites:** array indexing and interval notation.

Given integer length n in 0..100,000 and positive integer k, return every length-k contiguous range as a Window[], ordered by increasing start. No element values are supplied. If k>n, return an empty array. Reject k<=0 with IllegalArgumentException. Every returned interval must satisfy `0 <= from < to <= n` and `to-from=k`.

```java
public static Window[] fixedRanges(int n, int k) {
    // TODO: Enumerate every legal half-open range once.
    throw new UnsupportedOperationException();
}
```

| n; k | Output |
| --- | --- |
| `6; 3` | `[(0,3),(1,4),(2,5),(3,6)]` |
| `3; 1` | `[(0,1),(1,2),(2,3)]` |
| `3; 3` | `[(0,3)]` |
| `2; 3` | `[]` |
| `0; 1` | `[]` |
| `1; 1` | `[(0,1)]` |

**Targets:** O(w+1) time, O(1) auxiliary, O(w) output for w full windows. **Complete when:** the table and k<=0 rejection pass; you derive w, identify the incoming/outgoing indices for each transition, and explain why the first full range needs initialization before ordinary one-in/one-out movement. Follow-up: is boundary n a valid element index?

- [ ] **F1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F2. Minimum and maximum fixed totals

**Difficulty:** Easy. **Objective:** maintain a rolling total and compare complete candidates. **Prerequisites:** F1, long arithmetic, and min/max aggregation.

Given an arbitrary int array and positive k, inspect all contiguous ranges of exactly k elements. Return the smallest sum and its earliest start, followed by the largest sum and its earliest start. Negative values and duplicates are allowed. If k>n, return `(0,-1,0,-1)`; the sums are placeholders when their starts are -1. Reject k<=0. Do not store every window sum.

```java
public record SumExtremes(
        long minimum, int minimumStart, long maximum, int maximumStart) {}
public static SumExtremes fixedTotalExtremes(int[] values, int k) {
    // TODO: Maintain the active total and two best complete candidates.
    throw new UnsupportedOperationException();
}
```

| values; k | (minimum, minimumStart, maximum, maximumStart) |
| --- | --- |
| `[2,1,5,1,3,2]; 3` | `(6,3,9,2)` |
| `[-4,-2,-7]; 2` | `(-9,1,-6,0)` |
| `[3,3,3]; 2` | `(6,0,6,0)` |
| `[7]; 1` | `(7,0,7,0)` |
| `[MAX,MAX]; 2` | `(4294967294,0,4294967294,0)` |
| `[]; 1` | `(0,-1,0,-1)` |
| `[1,2]; 3` | `(0,-1,0,-1)` |

**Targets:** O(n) time, O(1) auxiliary and fixed-size output. **Complete when:** the invariant sheet and brute-force comparisons pass, including k=1, k=n, all-negative totals, and int extremes. Demonstrate on [2,1,5,1], k=3 how forgetting the outgoing 2 corrupts the second total. Follow-up: why is zero a dangerous initial maximum?

- [ ] **F2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F3. Centered moving averages

**Difficulty:** Medium. **Objective:** connect window boundaries to output alignment and fractional results. **Prerequisites:** F2 and floating-point division.

Given an arbitrary int array and integer radius r in 0..100,000, return a double[] of length n. At position i, average the contiguous range `[i-r,i+r+1)` if that entire range exists. Otherwise output `Double.NaN` at that position. Radius zero averages each singleton. Empty input returns an empty array. Negative values and duplicates are valid. Reject a negative radius; use only constant auxiliary state beyond the output.

```java
public static double[] centeredAverages(int[] values, int radius) {
    // TODO: Align each full average with its center; mark missing ranges NaN.
    throw new UnsupportedOperationException();
}
```

| values; radius | Output |
| --- | --- |
| `[2,1,5,1,3,2]; 1` | `[NaN,8/3,7/3,3,2,NaN]` |
| `[-5,-1,-3]; 1` | `[NaN,-3,NaN]` |
| `[4,-2]; 0` | `[4,-2]` |
| `[1,2,3]; 2` | `[NaN,NaN,NaN]` |
| `[MAX,MAX,MAX]; 1` | `[NaN,2147483647,NaN]` |
| `[]; 0` | `[]` |

**Targets:** O(n) time, O(1) auxiliary, O(n) output. **Complete when:** NaN locations, center alignment, finite-value tolerance, negative-radius rejection, and wide totals pass. Test a singleton with radius 0 and 1. Explain why an intermediate array of all sums violates the auxiliary-space target. Follow-up: why is -1 a poor absence marker for these averages?

<details>
<summary>Hint 1</summary>

A full centered range has a fixed number of elements. Some output positions may have no corresponding full range.

</details>
<details>
<summary>Hint 2</summary>

Determine the center from a full window's endpoints. Maintain one rolling total while the output still follows center order.

</details>
<details>
<summary>Hint 3</summary>

Use a long total, convert before division, and initialize unavailable output positions deliberately. Compare NaN with its predicate rather than ==.

</details>

- [ ] **F3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F4. Inspect a lowercase interval

**Difficulty:** Easy. **Objective:** distinguish occurrence totals from distinct character kinds. **Prerequisites:** char indexing and arrays.

Given lowercase English text and valid bounds `0 <= from <= to <= text.length()`, return a 26-element int[] with the frequencies in the contiguous interval [from,to), ordered a through z. Duplicates and empty intervals are allowed. All output counts are zero for an empty interval. Characters outside the interval do not contribute. Sortedness is irrelevant.

```java
public static int[] intervalCounts(String text, int from, int to) {
    // TODO: Return counts for exactly [from, to), in a..z order.
    throw new UnsupportedOperationException();
}
```

Tables show only nonzero buckets; `{}` means all 26 buckets are zero.

| text; from; to | Nonzero counts |
| --- | --- |
| `"banana"; 0; 6` | `{a:3,b:1,n:2}` |
| `"abac"; 1; 3` | `{a:1,b:1}` |
| `"aaaa"; 1; 4` | `{a:3}` |
| `"a"; 0; 1` | `{a:1}` |
| `"abc"; 1; 1` | `{}` |
| `""; 0; 0` | `{}` |

**Targets:** O(k+26) time for k=to-from, O(1) auxiliary, 26 output counters. **Complete when:** all buckets match the interval, their sum is k, and you can derive the number of distinct kinds from positive buckets. Explain what must change if uppercase or non-ASCII input becomes valid. Follow-up: why does a set lose information needed here?

- [ ] **F4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 4. Level 2: Fixed-Size Windows

These drills share fixed-length movement but maintain different information. Use [fixed rolling windows](./Sliding_Window_Essential_Concepts.md#4-fixed-size-rolling-windows) and [frequency matching](./Sliding_Window_Essential_Concepts.md#5-fixed-size-frequency-matching) after attempting the derivation.

### W1. Distinct counts in each fixed range

**Difficulty:** Medium. **Objective:** maintain a live frequency map while values enter and leave. **Prerequisites:** F1-F4 and map updates.

Given an arbitrary int array and positive k, return the number of distinct values in each length-k contiguous range, in increasing start order. Negative values and duplicates are valid. If k>n, return an empty int[]. Reject k<=0. A value appearing several times still contributes only one kind.

```java
public static int[] fixedDistinctCounts(int[] values, int k) {
    // TODO: Maintain live frequencies, including removal of outgoing copies.
    throw new UnsupportedOperationException();
}
```

| values; k | Output |
| --- | --- |
| `[4,1,4,2,2]; 3` | `[2,3,2]` |
| `[0,0,0]; 2` | `[1,1]` |
| `[MIN,MAX,MIN]; 2` | `[2,2]` |
| `[4,1,4]; 1` | `[1,1,1]` |
| `[4,1,4]; 3` | `[2]` |
| `[]; 1` | `[]` |
| `[1,2]; 3` | `[]` |

**Targets:** expected O(n) time, O(min(u,k+1)+1) auxiliary for u distinct input values, and O(w) output for w full ranges. **Complete when:** the invariant sheet and per-window set oracle agree, zero-count keys cannot distort the answer, and repeated outgoing values remain present until their final copy leaves. Follow-up: how does add-before-remove affect peak live state?

<details>
<summary>Hint 1</summary>

Removing one occurrence does not necessarily remove its kind from the range.

</details>
<details>
<summary>Hint 2</summary>

Keep exact positive frequencies so that a count of keys represents live distinct kinds.

</details>
<details>
<summary>Hint 3</summary>

Update the outgoing key and delete it only at zero. Record the result after the active length has returned to k.

</details>

- [ ] **W1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### W2. Distinct letters with a vowel quota

**Difficulty:** Medium. **Objective:** maintain two validity summaries for fixed-length substrings. **Prerequisites:** F4 and frequency-array updates.

Given lowercase English text, positive k, and integer vowels in 0..k, count the length-k contiguous substrings that have **no repeated letter** and **exactly vowels vowel occurrences**. Vowels are a,e,i,o,u. Input letters may repeat; only qualifying ranges must be distinct. Count overlapping ranges separately. Empty input or k>n gives zero. Reject k<=0 or a vowel quota outside 0..k.

```java
public static int countDistinctVowelWindows(String text, int k, int vowels) {
    // TODO: Maintain the state needed to test both conditions after each shift.
    throw new UnsupportedOperationException();
}
```

| text; k; vowels | Output |
| --- | --- |
| `"abecid"; 3; 1` | `2` |
| `"abaca"; 3; 2` | `0` |
| `"aaab"; 2; 1` | `1` |
| `"bcd"; 3; 0` | `1` |
| `"aei"; 3; 3` | `1` |
| `"a"; 1; 1` | `1` |
| `""; 1; 0` | `0` |
| `"ab"; 3; 1` | `0` |

**Targets:** O(n+26) time, O(26) auxiliary, scalar output. **Complete when:** enumeration agrees with all examples and generated short strings; incoming and outgoing updates preserve both summaries. Test all-identical text and k>26. Explain why scanning every substring anew misses the target for a general variable alphabet.

<details>
<summary>Hint 1</summary>

The vowel quota and repetition restriction describe different properties of the same range.

</details>
<details>
<summary>Hint 2</summary>

Only the incoming and outgoing letters change their frequencies. Think about which frequency transitions change the number of repeated kinds.

</details>
<details>
<summary>Hint 3</summary>

A counter for vowel occurrences and a counter for repetition violations can be maintained alongside 26 frequencies. Evaluate them only for a full-length range.

</details>

- [ ] **W2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### W3. All inventory matches

**Difficulty:** Medium. **Objective:** match required multiplicities and preserve overlapping results. **Prerequisites:** F4 and fixed-size frequency state.

Given lowercase English text and required, return every start index whose contiguous substring has exactly required's character multiset. Its length therefore equals required.length(). Return starts in ascending order, including overlaps. If required is empty, every boundary 0..n is a match. If it is longer than text, return an empty int[]. Repeated required characters must be matched separately.

```java
public static int[] inventoryStarts(String text, String required) {
    // TODO: Return every exact-inventory start, including overlapping matches.
    throw new UnsupportedOperationException();
}
```

| text; required | Output |
| --- | --- |
| `"baacaba"; "aab"` | `[0,4]` |
| `"aaaa"; "aa"` | `[0,1,2]` |
| `"abb"; "aab"` | `[]` |
| `"abc"; "b"` | `[1]` |
| `""; ""` | `[0]` |
| `"ab"; ""` | `[0,1,2]` |
| `"a"; "aa"` | `[]` |

**Targets:** O(n+m+t) time for text length n, requirement length m, and t matches; O(26+t) auxiliary is allowed for collecting results, plus O(t) returned output. If claiming O(26) auxiliary, explain how output sizing avoids an additional t-sized temporary collection. **Complete when:** the invariant sheet distinguishes membership from multiplicity, the frequency oracle agrees, and empty requirements/overlaps are handled deliberately. Follow-up: would the same state verify literal character order?

<details>
<summary>Hint 1</summary>

Matching inventory ignores internal order but preserves the number of copies of each letter.

</details>
<details>
<summary>Hint 2</summary>

Maintain the difference between current and required frequencies. A fixed alphabet permits constant-size comparisons.

</details>
<details>
<summary>Hint 3</summary>

Update both entering and leaving buckets, then test a full range. If maintaining a mismatch count, update it when a bucket enters or leaves zero.

</details>

- [ ] **W3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 5. Level 2: Variable-Size Windows

The answer may be a range, a length, or a count of ranges. Derive answer timing from that output; a correct best-length update does not automatically count every valid interval. Consult [longest-window reasoning](./Sliding_Window_Essential_Concepts.md#6-variable-windows-and-longest-valid-ranges) and [shortest-window reasoning](./Sliding_Window_Essential_Concepts.md#7-shortest-valid-and-covering-windows) after your attempt.

### V1. Count ranges with at most K kinds

**Difficulty:** Medium. **Objective:** turn a valid boundary into a count without missing suffixes. **Prerequisites:** W1 and variable-window invariants.

Given an arbitrary int array and nonnegative k, count all **nonempty contiguous ranges** containing at most k distinct values. Duplicate positions create different ranges even when their contents are equal. Negative values are valid. Empty input and k=0 return zero. Reject negative k. Return long because the count can exceed int.

```java
public static long countAtMostKinds(int[] values, int k) {
    // TODO: Count every qualifying nonempty range exactly once.
    throw new UnsupportedOperationException();
}
```

| values; k | Output |
| --- | --- |
| `[1,2,1,3]; 2` | `8` |
| `[4,4,4]; 1` | `6` |
| `[1,2,3]; 1` | `3` |
| `[1,2,1,3]; 3` | `10` |
| `[1,2]; 0` | `0` |
| `[]; 2` | `0` |
| `[-1,-1,2]; 1` | `4` |
| `repeat(0,100000); 1` | `5000050000` |

**Targets:** expected O(n) time, O(min(u,k+1)+1) auxiliary for u distinct values, scalar output. **Complete when:** the invariant sheet proves which starts are valid for each right end, a quadratic enumerator agrees on short arrays, and the large count is safe. Trace a step that removes several elements. Follow-up: why is adding one per valid right endpoint insufficient?

<details>
<summary>Hint 1</summary>

A valid interval can contain other valid intervals ending at the same position.

</details>
<details>
<summary>Hint 2</summary>

After repairing excess kinds, consider all starts from the retained left boundary through the last included element.

</details>
<details>
<summary>Hint 3</summary>

Count those nonempty suffixes using the interval length and accumulate in long. Preserve exact frequencies while advancing left.

</details>

- [ ] **V1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### V2. Longest range under a character-copy limit

**Difficulty:** Medium. **Objective:** repair duplicate violations and record only valid candidates. **Prerequisites:** frequency arrays and longest-range invariants.

Given **ASCII** text and nonnegative limit, return the longest contiguous range in which every char value occurs at most limit times. Matching is case-sensitive, so a and A differ. Input duplicates are allowed. With limit=1, the task is longest substring without repeated characters. Empty text or limit=0 returns (0,0); reject a negative limit. Equal-length optima choose the earliest start.

```java
public static Window longestCappedCharacters(String text, int limit) {
    // TODO: Return the earliest longest range satisfying every copy limit.
    throw new UnsupportedOperationException();
}
```

| text; limit | Output |
| --- | --- |
| `"abcbad"; 1` | `(2,6)` |
| `"aA!a"; 1` | `(0,3)` |
| `"aaabb"; 2` | `(1,5)` |
| `"bbbb"; 1` | `(0,1)` |
| `""; 2` | `(0,0)` |
| `"abc"; 0` | `(0,0)` |
| `"ab"; 10` | `(0,2)` |

**Targets:** O(n+128) time, O(128) auxiliary, fixed-size output. **Complete when:** the invariant sheet identifies which count can newly violate the constraint, every removal updates its actual outgoing character, and enumeration confirms earliest ties. Test a case needing multiple removals before the repeated character is removed. Follow-up: why would a 26-slot lowercase index be invalid for this contract?

<details>
<summary>Hint 1</summary>

Adding one character can change only one count, although repairing it may remove several different characters.

</details>
<details>
<summary>Hint 2</summary>

Extend once, restore the copy limit, then compare the valid range with the best. Explain why a rejected start cannot be repaired by later extension.

</details>
<details>
<summary>Hint 3</summary>

Keep the alphabet model and indices consistent. Strict improvement in length preserves the earliest equal-length result.

</details>

- [ ] **V2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### V3. Shortest nonnegative quota run

**Difficulty:** Medium. **Objective:** derive a shortest-valid update order and its sign assumption. **Prerequisites:** rolling sums and variable windows.

Input values are **nonnegative ints**. Given positive long target, return the shortest nonempty contiguous range whose sum is at least target. Equal lengths choose the earliest start. Empty input or insufficient total returns (-1,-1). Zero values and duplicates are valid. Reject target<=0; do not assume negative elements belong to this contract.

```java
public static Window shortestQuotaRun(int[] values, long target) {
    // TODO: Derive when to record a valid candidate and when to contract.
    throw new UnsupportedOperationException();
}
```

| values; target | Output |
| --- | --- |
| `[1,4,2,1,6]; 7` | `(3,5)` |
| `[0,7,0]; 7` | `(1,2)` |
| `[2,2,2]; 4` | `(0,2)` |
| `[2,3]; 6` | `(-1,-1)` |
| `[]; 1` | `(-1,-1)` |
| `[0,0]; 1` | `(-1,-1)` |
| `[MAX,MAX]; 2147483648` | `(0,2)` |
| `[MAX]; LMAX` | `(-1,-1)` |

**Targets:** O(n) time, O(1) auxiliary, fixed-size output. **Complete when:** the invariant sheet justifies recording before a potentially destructive removal, small-input enumeration agrees, and zero values cannot stall the loop. Explain why an invalid suffix cannot become valid by further removal under these assumptions. Follow-up: which implication fails if negative values are introduced?

<details>
<summary>Hint 1</summary>

For a shortest objective, finding one valid interval creates an opportunity to try shorter ones.

</details>
<details>
<summary>Hint 2</summary>

Record a valid interval before removing its first item. Nonnegative values determine what can happen to the sum afterward.

</details>
<details>
<summary>Hint 3</summary>

Use a long sum and an explicit missing-answer state. Progress depends on moving the boundary, even when removing zero leaves the sum unchanged.

</details>

- [ ] **V3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### V4. Minimum repeated-character coverage

**Difficulty:** Medium — coverage extension. **Objective:** distinguish required kinds from required occurrences. **Prerequisites:** W3, V3, and threshold-crossing count updates.

Given **ASCII**, case-sensitive strings text and required, return the length of the shortest contiguous substring containing every required character with its multiplicity. Extra characters are allowed inside the substring. Empty required returns 0. Impossible nonempty coverage returns -1, including when text is empty or too short. Input and requirement duplicates are valid.

```java
public static int minimumCoverLength(String text, String required) {
    // TODO: Track repeated requirements and record only covered candidates.
    throw new UnsupportedOperationException();
}
```

| text; required | Output |
| --- | --- |
| `"ABAACB"; "AAB"` | `3` |
| `"xxAByA"; "AAB"` | `4` |
| `"AB"; "AA"` | `-1` |
| `"aA"; "AA"` | `-1` |
| `"aA"; "aA"` | `2` |
| `"AAAA"; "AAA"` | `3` |
| `""; ""` | `0` |
| `""; "A"` | `-1` |
| `"A"; "AA"` | `-1` |

**Targets:** O(n+m+128) time, O(128) auxiliary, scalar output. **Complete when:** the invariant sheet defines exactly what each validity summary counts, repeated requirements and surplus removal pass, and a substring enumerator agrees on small cases. Explain what happens when a required count drops from sufficient to insufficient. This develops coverage reasoning; it does not make the optional platform Hard mandatory.

<details>
<summary>Hint 1</summary>

Knowing that every required character kind is present is insufficient when a kind is required more than once.

</details>
<details>
<summary>Hint 2</summary>

Choose either satisfied kinds or missing occurrences as a summary, and define its threshold transitions precisely.

</details>
<details>
<summary>Hint 3</summary>

Adding surplus copies must not satisfy an extra kind. Removing a surplus copy must not immediately invalidate coverage. Record before the removal that may break validity.

</details>

- [ ] **V4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 6. Window Validity and Counterexamples

### R1. Audit six movement proposals

**Difficulty:** Medium. **Objective:** test whether a movement rule is justified by its domain. **Prerequisites:** fixed/variable invariants and simple enumeration.

For each proposal below, decide whether its stated rule is sound for the stated objective and domain. For a sound proposal, give the needed state invariant and safe-movement argument. For an unsound proposal, supply a concrete failing input, expected result, actual lost candidate, and a correct alternative. Include at least one counterexample of your own beyond the supplied examples.

| Proposal | Domain and objective | Proposed movement |
| --- | --- | --- |
| A | Signed integers; every fixed-length sum for positive k | Add incoming and subtract outgoing; visit every legal start |
| B | Arbitrary values; longest range with at most K kinds, K>=0 | Expand, shrink while kinds exceed K, then record |
| C | Nonnegative integers; shortest sum >= positive target | Expand, record while valid, then remove left |
| D | Signed integers; any nonempty exact-target sum | Whenever sum exceeds target, permanently remove left items until it does not |
| E | Signed integers; shortest sum >= positive target | Use the same record-and-shrink rule as C |
| F | Nonnegative integers; longest sum <= nonnegative budget | Expand, shrink while over budget, then record |

As a small independent oracle, implement the following exact-sum membership method by enumerating all nonempty contiguous ranges. **For this exercise only, n<=40.** Target is long in [-10^15,10^15]; elements may be any int, including negative values and duplicates. Empty input returns false. A match need not exist.

```java
public static boolean hasExactRunOracle(int[] values, long target) {
    // TODO: Build a simple independent enumerator for the reasoning experiments.
    throw new UnsupportedOperationException();
}
```

| values; target | Oracle output |
| --- | --- |
| `[4,-1]; 3` | `true` |
| `[1,-1,5]; 5` | `true` |
| `[-2,-3]; -5` | `true` |
| `[]; 0` | `false` |
| `[0]; 0` | `true` |
| `[2,4]; 3` | `false` |
| `[MAX,MAX]; 4294967294` | `true` |

**Targets:** oracle O(n²) time, O(1) auxiliary, boolean output. The reasoning deliverable has no runtime target. **Complete when:** all six proposals have a proof or witness, the oracle passes, and your explanation does not reject all negative-input windows indiscriminately. Prefix sums/maps may supply an alternative for exact sums. A correct quadratic baseline is enough for a signed shortest-threshold counterexample; an advanced prefix deque is not required.

<details>
<summary>Hint 1</summary>

Ask whether adding or removing an item changes the relevant quantity in a predictable direction.

</details>
<details>
<summary>Hint 2</summary>

A permanent discard needs a statement about future candidates, not just the current sum. Fixed-size enumeration has a different proof obligation.

</details>
<details>
<summary>Hint 3</summary>

Try a positive value followed by a negative one, and try removing a negative value from a currently insufficient sum. Use prefix differences to retain earlier starts when necessary.

</details>

- [ ] **R1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### R2. Count exact-sum ranges with signed values

**Difficulty:** Medium. **Objective:** replace an unsuitable movement rule with information about earlier boundaries. **Prerequisites:** R1 and [prefix sums](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries).

Given an arbitrary signed int array and long target in [-10^15,10^15], count every nonempty contiguous range whose sum equals target. Return long. Repeated values and repeated sums are valid; equal-content ranges at different positions count separately. Empty input and no matches return zero. Preserve all input values and order.

```java
public static long countExactRuns(int[] values, long target) {
    // TODO: Derive a method valid for arbitrary signed inputs and multiplicity.
    throw new UnsupportedOperationException();
}
```

| values; target | Output |
| --- | --- |
| `[1,-1,1]; 1` | `3` |
| `[0,0,0]; 0` | `6` |
| `[4,-1]; 3` | `1` |
| `[2,2,2]; 4` | `2` |
| `[-2,-2]; -2` | `2` |
| `[]; 0` | `0` |
| `[MAX,MAX]; 4294967294` | `1` |
| `repeat(0,100000); 0` | `5000050000` |

**Targets:** expected O(n) time, O(n) auxiliary, scalar output. **Complete when:** a range enumerator agrees, your proof counts each pair of boundaries once, and the empty-prefix/query-order decisions exclude empty ranges without losing starts at zero. Explain why merely tracking whether a prefix exists can undercount. Follow-up: what different state would be needed for the longest exact-sum range?

<details>
<summary>Hint 1</summary>

An exact range sum can be expressed using two prefix totals even when values are negative.

</details>
<details>
<summary>Hint 2</summary>

At one right boundary, several earlier prefix positions may satisfy the same equation.

</details>
<details>
<summary>Hint 3</summary>

Store prefix frequencies, include the initial zero prefix, and query earlier positions before adding the current one. Use long for totals and the number of ranges.

</details>

- [ ] **R2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 7. Optional Advanced Extension

### O1. Fixed-range spread

**Difficulty:** Medium — optional advanced. **Objective:** maintain extrema whose outgoing values cannot be undone by subtraction. **Prerequisites:** [monotonic Stack reasoning](./Stack/Stack_Essential_Concepts.md#8-monotonic-stack-fundamentals) and the concepts guide's [deque extension](./Sliding_Window_Essential_Concepts.md#12-monotonic-deque-optional-advanced).

Given arbitrary int values and positive k, return `maximum-minimum` for every contiguous length-k range, ordered by start. Return long[] because the difference may exceed int. Duplicates and negative values are valid. k>n gives an empty output; reject k<=0. This exercise is optional and does not block ordinary readiness.

```java
public static long[] fixedRangeSpreads(int[] values, int k) {
    // TODO: Retain sufficient unexpired candidates for both extrema.
    throw new UnsupportedOperationException();
}
```

| values; k | Output |
| --- | --- |
| `[4,1,3,3,7]; 3` | `[3,2,4]` |
| `[5,5,5]; 2` | `[0,0]` |
| `[-3,-1,-2]; 2` | `[2,1]` |
| `[MIN,MAX]; 2` | `[4294967295]` |
| `[8,2]; 1` | `[0,0]` |
| `[]; 1` | `[]` |
| `[1,2]; 3` | `[]` |

**Targets:** O(n) total time, O(k) auxiliary, O(w) output. **Complete when:** a per-window scan agrees, you state expiry and dominance invariants, duplicate identities are handled correctly, and each candidate has a bounded lifetime. Follow-up: why does retaining just the previous minimum and maximum fail when either expires?

<details>
<summary>Hint 1</summary>

After an extreme value leaves, the next extreme must come from candidates retained earlier.

</details>
<details>
<summary>Hint 2</summary>

Consider separate ordered candidate sets for the minimum and maximum, with indices recording expiry.

</details>
<details>
<summary>Hint 3</summary>

Discard expired indices and dominated tail candidates using consistent equality policies. Widen before subtracting the two extreme int values.

</details>

- [ ] **O1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 8. Level 3: Curated Platform Problems

Priority and difficulty are separate: E is Essential, A is Additional, C is Optional advanced; Easy/Medium/Hard are platform labels. The order below matches the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Read each official statement for its exact contract. These concise objectives do not reproduce the statements.

For every problem, derive a baseline and invariant, implement from a blank editor, test, and explain total costs. Maintain date/result/minutes/review/lesson fields on the existing roadmap problem Log only. A checked navigation box here is not a second record of independence.

### N1. Best Time to Buy and Sell Stock

**Priority: E. Difficulty: Easy.** [Official problem](https://leetcode.com/problems/best-time-to-buy-and-sell-stock/).

Objective: maintain information about earlier positions while evaluating a later choice. A running-minimum scan is a natural approach; do not force an expandable/shrinkable window onto it. Completion evidence: explain the time-order constraint, decreasing/equal-price cases, and why the saved state is sufficient. Follow-up: what changes if the transaction rules change?

- [ ] **N1** — Canonical record: **Best Time to Buy and Sell Stock** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N2. Longest Substring Without Repeating Characters

**Priority: E. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/longest-substring-without-repeating-characters/).

Objective: preserve a duplicate-free interval and record the best only when its invariant supports the result. Preparation: V2. Completion evidence: repeated-character tests, multiple removals, clear character model, and total pointer-movement analysis. Follow-up: if using last-seen indices, how do you prevent the left boundary from moving backward?

- [ ] **N2** — Canonical record: **Longest Substring Without Repeating Characters** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N3. Permutation in String

**Priority: E. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/permutation-in-string/).

Objective: maintain fixed-length character inventory, including repeated requirements. Preparation: W3. Completion evidence: distinguish frequency equality from set equality and literal order; justify alphabet and mismatch/comparison cost. Follow-up: how does changing existence to counting or returning all starts affect the output and storage?

- [ ] **N3** — Canonical record: **Permutation in String** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N4. Longest Repeating Character Replacement

**Priority: A. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/longest-repeating-character-replacement/).

Objective: derive the replacement-budget condition from current frequencies. Preparation: V2 and the concepts guide's replacement discussion. Completion evidence: explain the meaning of the maximum-frequency statistic and include its maintenance cost. A historical maximum requires a different proof from exact current-window validity. Follow-up: would your state also justify returning a valid witness range?

- [ ] **N4** — Canonical record: **Longest Repeating Character Replacement** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N5. Minimum Window Substring

**Priority: C. Difficulty: Hard.** [Official problem](https://leetcode.com/problems/minimum-window-substring/).

Objective: investigate shortest multiset coverage and precise threshold transitions. Preparation: V4. Completion evidence: duplicate requirements, surplus removal, impossible coverage, and correct substring boundaries. Keep best indices during the search and account for materializing the final output. This optional Hard does not block ordinary topic readiness.

- [ ] **N5** — Canonical record: **Minimum Window Substring** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N6. Sliding Window Maximum

**Priority: C. Difficulty: Hard.** [Official problem](https://leetcode.com/problems/sliding-window-maximum/).

Objective: investigate a monotonic deque with unexpired, undominated candidate indices. Preparation: O1 and prior Stack knowledge. Completion evidence: prove each removal reason, handle equal values, and distinguish auxiliary state from output. This optional extension is not required to earn ordinary readiness.

- [ ] **N6** — Canonical record: **Sliding Window Maximum** Log in the [roadmap](./DSA_Interview_Roadmap.md#5-sliding-window). Maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

## 9. Level 4: Mixed Interview Challenges

Hide the topic label, hints, and prior code. For each challenge, compare at least two plausible techniques before coding. Decide whether contiguity matters, whether candidate size is fixed, whether validity supports safe shrinking, what state is needed, and whether preprocessing is worthwhile. Choose among the earlier array, hashing, sorting, Two Pointers, and window skills; no advanced algorithm is mandatory.

The metadata below describes the reasoning goal without naming the intended method. The stated complexity target is part of the interview contract. Explain the assumptions behind any expected bound.

### X1. Repair-log budget

**Difficulty:** Medium. **Objective:** select the longest feasible span and justify discarded candidates. **Prerequisites:** earlier sequence-processing and arithmetic skills.

Each nonnegative int in costs is the cost of one event. Given a nonnegative long budget, return the longest **contiguous** event range whose total cost is at most budget. Zero costs and duplicates are allowed. Empty input, or no feasible nonempty range, returns (0,0). Equal lengths choose the earliest start. Costs may reach MAX and budget may reach LMAX; preserve input.

```java
public static Window repairLogRange(int[] costs, long budget) {
    // TODO: Choose a method and return the earliest longest feasible range.
    throw new UnsupportedOperationException();
}
```

| costs; budget | Output |
| --- | --- |
| `[2,0,3,1,1]; 4` | `(1,4)` |
| `[2,0,3,1,1]; 5` | `(1,5)` |
| `[0,0,5,0]; 0` | `(0,2)` |
| `[5]; 4` | `(0,0)` |
| `[]; 0` | `(0,0)` |
| `[MAX,1,MAX]; 2147483648` | `(0,2)` |

**Targets:** O(n) time, O(1) auxiliary, fixed-size output. **Complete when:** the invariant sheet and range enumerator agree, including zero budget and equal-length optima. Explain precisely how the nonnegative domain affects your choices. Follow-up: how would rebates represented by negative values affect the proof?

<details>
<summary>Hint 1</summary>

An event with zero cost can lengthen a feasible span without increasing its total.

</details>
<details>
<summary>Hint 2</summary>

With nonnegative costs, extending an over-budget range cannot repair its original start.

</details>
<details>
<summary>Hint 3</summary>

Use a wide total, restore feasibility before recording length, and retain the earlier range when lengths tie.

</details>

- [ ] **X1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X2. Balanced ledger episodes

**Difficulty:** Medium. **Objective:** retain the information needed when different histories reach the same state. **Prerequisites:** earlier array and collection skills.

Given arbitrary signed int entries and long target in [-10^15,10^15], return the longest **nonempty contiguous** range whose sum equals target. Negative values, zeros, and duplicates are valid. If no match exists, including empty input even with target zero, return (-1,-1). Equal lengths choose the earliest start. Preserve input.

```java
public static Window longestLedgerEpisode(int[] entries, long target) {
    // TODO: Choose state that preserves the longest matching range and tie rule.
    throw new UnsupportedOperationException();
}
```

| entries; target | Output |
| --- | --- |
| `[3,-1,-2,4,0]; 4` | `(0,5)` |
| `[2,-2,2,-2]; 0` | `(0,4)` |
| `[1,2,1,2]; 3` | `(0,2)` |
| `[1,2]; 8` | `(-1,-1)` |
| `[]; 0` | `(-1,-1)` |
| `[0]; 0` | `(0,1)` |
| `[4,-1]; 3` | `(0,2)` |
| `[MAX,MAX]; 4294967294` | `(0,2)` |

**Targets:** expected O(n) time, O(n) auxiliary, fixed-size output. **Complete when:** a quadratic oracle agrees, your state choice preserves length and tie semantics, and you explain whether a current-sum comparison permits discarding an earlier start. Follow-up: what changes when the requested output becomes the number of matching episodes?

<details>
<summary>Hint 1</summary>

The same cumulative total may occur at several earlier positions.

</details>
<details>
<summary>Hint 2</summary>

Express a range sum as a difference between totals at two boundaries. For one fixed end, determine which matching earlier boundary gives the longest range.

</details>
<details>
<summary>Hint 3</summary>

Retain the earliest position for each earlier prefix total, include boundary zero, and avoid using the current boundary as an empty answer.

</details>

- [ ] **X2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X3. Nearby repeat alerts

**Difficulty:** Medium. **Objective:** respect distance, occurrence identity, and strictly earlier positions. **Prerequisites:** earlier sequence and collection skills.

Given arbitrary int IDs and integer distance in 0..100,000, return every index i for which an earlier index j satisfies `ids[j]==ids[i]` and `1 <= i-j <= distance`. Return each qualifying i once, in ascending order, even if several earlier matches exist. IDs may be negative or repeated. Empty input, distance zero, or no alerts gives an empty int[]. Preserve input.

```java
public static int[] nearbyRepeatAlerts(int[] ids, int distance) {
    // TODO: Report each qualifying current position once using earlier evidence.
    throw new UnsupportedOperationException();
}
```

| ids; distance | Output |
| --- | --- |
| `[8,3,8,8,3]; 2` | `[2,3]` |
| `[8,3,8,8,3]; 3` | `[2,3,4]` |
| `[8,8]; 0` | `[]` |
| `[4,4,4]; 1` | `[1,2]` |
| `[1,2,3]; 10` | `[]` |
| `[MIN,MAX,MIN]; 2` | `[2]` |
| `[]; 5` | `[]` |

**Targets:** expected O(n) time, O(u+t) auxiliary is allowed for u distinct IDs and t alerts, plus O(t) returned output. State the actual bound of your chosen design. **Complete when:** an earlier-position scan oracle agrees and the current position cannot match itself. Follow-up: what history can be discarded once a newer occurrence of an ID is seen?

<details>
<summary>Hint 1</summary>

Among earlier equal IDs, consider which occurrence is closest to the current index.

</details>
<details>
<summary>Hint 2</summary>

Compare retaining the most recent position per ID with maintaining only recent membership; either design must preserve duplicate semantics.

</details>
<details>
<summary>Hint 3</summary>

Check prior information before updating it with i. If retaining latest positions, a single distance comparison determines whether at least one earlier equal ID qualifies.

</details>

- [ ] **X3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X4. Closest paired reading

**Difficulty:** Medium. **Objective:** choose an approach when the answer concerns arbitrary positions. **Prerequisites:** earlier array and arithmetic skills.

Given an **unsorted** int array and long target in [-10^12,10^12], choose two **distinct indices**, which need not be adjacent. Return the smallest mathematical absolute error `abs(values[i]+values[j]-target)` as long. Duplicates are valid and may form a pair if they occupy different indices. With fewer than two elements, return -1. Preserve input; the output does not require the pair's indices.

```java
public static long closestPairError(int[] values, long target) {
    // TODO: Choose a method for arbitrary index pairs and preserve the input.
    throw new UnsupportedOperationException();
}
```

| values; target | Output |
| --- | --- |
| `[9,1,6,3]; 8` | `1` |
| `[5,5]; 10` | `0` |
| `[-5,-2,7]; 3` | `1` |
| `[MAX,MAX]; 4294967294` | `0` |
| `[MIN,MIN]; 0` | `4294967296` |
| `[]; 0` | `-1` |
| `[8]; 8` | `-1` |

**Targets:** O(n log(n+1)) time or better, O(n) auxiliary allowed, scalar output. Include any library workspace and input-preserving copy. **Complete when:** all-pairs enumeration agrees, arithmetic is widened before addition/subtraction, and you explain why a contiguous-range representation does not describe all candidates. Follow-up: would a method that checks only exact complements solve this approximate objective?

<details>
<summary>Hint 1</summary>

The original adjacency of two chosen values does not affect the required error.

</details>
<details>
<summary>Hint 2</summary>

Consider whether an ordered copy lets one pair boundary move in a direction that rules out other candidates.

</details>
<details>
<summary>Hint 3</summary>

Use two different positions in the ordered copy, update the best error before moving, and compute sums and differences as long.

</details>

- [ ] **X4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X5. Range audit requests

**Difficulty:** Medium. **Objective:** answer a batch of read-only requests without unnecessary repeated work. **Prerequisites:** earlier array and arithmetic skills.

Given arbitrary signed int values and an int[][] queries, each query is exactly `[from,to]` describing the **half-open** range [from,to). Bounds are guaranteed valid: `0 <= from <= to <= n`. Return each requested sum as long, in query order. Queries may repeat, overlap, or arrive in any order. Empty ranges sum to zero. Empty values permit only [0,0] queries; empty queries gives an empty output. Preserve both inputs.

```java
public static long[] auditRangeTotals(int[] values, int[][] queries) {
    // TODO: Choose reusable state for independently specified ranges.
    throw new UnsupportedOperationException();
}
```

| values; queries | Output |
| --- | --- |
| `[3,-2,5,1]; [[0,4],[1,3],[2,2],[0,1]]` | `[7,3,0,3]` |
| `[]; [[0,0]]` | `[0]` |
| `[1,2]; []` | `[]` |
| `[MAX,MAX]; [[0,2],[1,2]]` | `[4294967294,2147483647]` |
| `[-2,5]; [[0,2],[0,2],[1,1]]` | `[3,3,0]` |

**Targets:** O(n+q) time, O(n) auxiliary, O(q) output for q requests. **Complete when:** a direct per-query sum oracle agrees, empty boundaries are safe, and you explain why unrelated query endpoints need not move monotonically. Follow-up: how would frequent updates to values change the preprocessing tradeoff?

<details>
<summary>Hint 1</summary>

Every requested interval overlaps two initial segments of the same unchanged array.

</details>
<details>
<summary>Hint 2</summary>

Choose a summary at each boundary so one range can be reconstructed from two summaries.

</details>
<details>
<summary>Hint 3</summary>

Include a summary for the empty prefix, use long storage, and keep query order when constructing the result.

</details>

- [ ] **X5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 10. Complexity Analysis Exercises

These executable snippets are **analysis subjects**, not starter solutions for the earlier exercises. They return diagnostic scalars so that you can verify a trace. Keep the [delayed cost checks](#15-references-and-delayed-complexity-checks) closed until your derivation is written.

For **every C exercise**, answer:

1. What is the input size, and which additional independent sizes matter?
2. What is the time complexity?
3. What is the auxiliary space, including temporary objects and library work?
4. Why do the operation counts imply those bounds?
5. How many times can each pointer or loop variable move, and does a scan restart?
6. Which Java API costs are hidden by concise expressions?
7. Can the exact output be computed more efficiently?
8. Which input-domain, alphabet, hashing, or implementation assumptions affect the answer?

The time/space **analysis target** is a tight or justified bound under the stated contract; numerical bounds are delayed to preserve the exercise. Completion requires the derivation, an output trace, a boundary test, and an improvement or justified reason to retain the code. Do not run a deliberately expensive baseline at maximum input size merely to confirm its slowness.

### C1. Recompute every interval total

**Difficulty:** Medium. **Objective:** distinguish the number of intervals from work inside each interval. **Prerequisites:** nested loops and summations.

Input is an arbitrary signed int array, with duplicates and empty input allowed. Return the number of nonempty contiguous ranges with a strictly positive sum, as long. Empty input and no positive ranges return zero.

```java
public static long c1(int[] values) {
    long positive = 0;
    for (int left = 0; left < values.length; left++) {
        for (int right = left + 1; right <= values.length; right++) {
            long sum = 0;
            for (int i = left; i < right; i++) sum += values[i];
            if (sum > 0) positive++;
        }
    }
    return positive;
}
```

Examples: `[1,-1,2] -> 4`; `[1,1] -> 3`; `[0,0] -> 0`; `[] -> 0`. Trace every counted range in the first example. Follow-up: if one sum is maintained while right advances for a fixed left, which factor disappears, and which repeated work remains?

<details>
<summary>Hint 1</summary>

There are many intervals, and their lengths are not all constant.

</details>
<details>
<summary>Hint 2</summary>

Add the number of element visits over all choices of left and right.

</details>
<details>
<summary>Hint 3</summary>

Contrast rebuilding the sum for each pair of boundaries with extending one running total for each fixed start.

</details>

- [ ] **C1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C2. Two ways to count high fixed totals

**Difficulty:** Medium. **Objective:** compare repeated calculation with incremental state. **Prerequisites:** F2 and independent size variables.

Input values are arbitrary ints; k is positive; threshold is any long. Both methods count length-k contiguous ranges whose sum is **strictly greater** than threshold. Duplicates and negative values are valid. If k>n, including empty input, return zero. Count fits int.

```java
public static int c2Recompute(int[] values, int k, long threshold) {
    if (k > values.length) return 0;
    int count = 0;
    for (int left = 0; left <= values.length - k; left++) {
        long sum = 0;
        for (int i = left; i < left + k; i++) sum += values[i];
        if (sum > threshold) count++;
    }
    return count;
}
public static int c2Rolling(int[] values, int k, long threshold) {
    if (k > values.length) return 0;
    long sum = 0;
    int count = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        if (right > k) sum -= values[right - k - 1];
        if (right >= k && sum > threshold) count++;
    }
    return count;
}
```

Both methods: `([2,1,5,1,3,2],3,7) -> 2`; `([-4,-2,-7],2,-8) -> 1`; `([],1,0) -> 0`; `([5],1,5) -> 0`. Keep n and k distinct, including k=1 and k=n. Follow-up: why is a blanket “the first method is quadratic” less precise than a bound in both n and k?

<details>
<summary>Hint 1</summary>

First count how many full windows exist.

</details>
<details>
<summary>Hint 2</summary>

One method repeats k additions per start; the other reuses overlapping work.

</details>
<details>
<summary>Hint 3</summary>

Include the immediate k>n branch and distinguish scalar output from a method returning every total.

</details>

- [ ] **C2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C3. Count repair movements

**Difficulty:** Medium. **Objective:** analyze a nested while over a shared forward-only boundary. **Prerequisites:** variable-window state and amortization.

Input values are nonnegative ints, duplicates allowed, and budget is a nonnegative long. Return the number of left-boundary advances performed by this code, **not** a best range. Empty input returns zero. Zero values are valid; the counter never exceeds n.

```java
public static int c3(int[] values, long budget) {
    int left = 0, removals = 0;
    long sum = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        while (sum > budget) {
            sum -= values[left++];
            removals++;
        }
    }
    return removals;
}
```

Examples: `([2,1,3,0],3) -> 2`; `([5],0) -> 1`; `([0,0,0],0) -> 0`; `([],0) -> 0`. Explain why left cannot overrun right under this contract. Follow-up: can one outer iteration still do a large amount of work?

<details>
<summary>Hint 1</summary>

A position removed from this state is never reinserted.

</details>
<details>
<summary>Hint 2</summary>

Count left movements over the entire scan, and count final failed while checks separately.

</details>
<details>
<summary>Hint 3</summary>

The bound depends on monotone pointer travel and constant-time updates, not on assuming every outer iteration has one removal.

</details>

- [ ] **C3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C4. Array and map summaries

**Difficulty:** Medium. **Objective:** include validation scans and distinguish alphabet size from input length. **Prerequisites:** W1, frequency arrays, and HashMap operations.

Input text is ASCII and case-sensitive, with duplicates allowed; k is positive. Both methods return the **sum of distinct-character counts** over all length-k substrings, as long. k>n gives zero. Empty input is valid.

```java
public static long c4Array(String text, int k) {
    if (k > text.length()) return 0;
    int[] counts = new int[128];
    long total = 0;
    for (int right = 1; right <= text.length(); right++) {
        counts[text.charAt(right - 1)]++;
        if (right > k) counts[text.charAt(right - k - 1)]--;
        if (right >= k) {
            for (int count : counts) if (count > 0) total++;
        }
    }
    return total;
}
public static long c4Map(String text, int k) {
    if (k > text.length()) return 0;
    Map<Character, Integer> counts = new HashMap<>();
    long total = 0;
    for (int right = 1; right <= text.length(); right++) {
        char incoming = text.charAt(right - 1);
        counts.put(incoming, counts.getOrDefault(incoming, 0) + 1);
        if (right > k) {
            char outgoing = text.charAt(right - k - 1);
            int remaining = counts.get(outgoing) - 1;
            if (remaining == 0) counts.remove(outgoing);
            else counts.put(outgoing, remaining);
        }
        if (right >= k) total += counts.size();
    }
    return total;
}
```

Both methods: `("abaca",3) -> 7`; `("aaa",2) -> 2`; `("aA",1) -> 2`; `("",1) -> 0`. Analyze the fixed ASCII version, then replace 128 conceptually with an input-dependent alphabet size Σ. Follow-up: which summary could avoid rescanning all buckets?

<details>
<summary>Hint 1</summary>

The array version's inner loop is over the alphabet, not the current substring.

</details>
<details>
<summary>Hint 2</summary>

Separate initialization, endpoint updates, and validation cost per full range. Qualify hash-operation costs.

</details>
<details>
<summary>Hint 3</summary>

A distinct counter changes only on zero-to-one and one-to-zero transitions. Include a possible temporary extra kind before removal when analyzing map space.

</details>

- [ ] **C4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C5. Copy and rescan each prefix

**Difficulty:** Medium. **Objective:** expose hidden substring and repeated-scan costs. **Prerequisites:** strings, sets, and allocation accounting.

Input text is ASCII and case-sensitive; duplicates and empty input are valid. Return the sum of the distinct-character counts of every **nonempty prefix**, as long. Empty input returns zero. This is a diagnostic total, not a longest-substring answer.

```java
public static long c5(String text) {
    long total = 0;
    for (int right = 1; right <= text.length(); right++) {
        String prefix = text.substring(0, right);
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < prefix.length(); i++) {
            seen.add(prefix.charAt(i));
        }
        total += seen.size();
    }
    return total;
}
```

Examples: `"aba" -> 5`; `"aaa" -> 3`; `"aA!" -> 6`; `"" -> 0`. Distinguish peak live auxiliary memory from cumulative allocated text, assuming unreachable temporaries can be reclaimed. Follow-up: can the exact same output be produced without constructing any prefix strings?

<details>
<summary>Hint 1</summary>

The right boundary moves forward, but the inner scan restarts at zero.

</details>
<details>
<summary>Hint 2</summary>

Sum prefix lengths and account for both copying and scanning them.

</details>
<details>
<summary>Hint 3</summary>

Each new prefix differs from the previous one by a single incoming character. Consider retaining its distinct-state summary.

</details>

- [ ] **C5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C6. Reusable boundary totals

**Difficulty:** Medium. **Objective:** analyze preprocessing separately from independent queries. **Prerequisites:** prefix differences and long arithmetic.

Input values are arbitrary signed ints. Each query is a two-element int row describing valid half-open [from,to) bounds; empty and repeated ranges are allowed. Return the largest absolute requested sum, or zero if there are no queries. Inputs may be empty under those bounds. The shared size limits make every difference and absolute value fit long.

```java
public static long c6(int[] values, int[][] queries) {
    long[] prefix = new long[values.length + 1];
    for (int i = 0; i < values.length; i++) {
        prefix[i + 1] = prefix[i] + values[i];
    }
    long largest = 0;
    for (int[] query : queries) {
        long sum = prefix[query[1]] - prefix[query[0]];
        largest = Math.max(largest, Math.abs(sum));
    }
    return largest;
}
```

Examples: `([3,-2,5,1],[[0,4],[1,3],[2,2]]) -> 7`; `([-4,-2],[[0,2],[1,2]]) -> 6`; `([],[[0,0]]) -> 0`; `([1],[]) -> 0`. Let n and q vary independently. Follow-up: does q=0 avoid building prefix in this implementation?

<details>
<summary>Hint 1</summary>

The two loops process different input collections.

</details>
<details>
<summary>Hint 2</summary>

Charge the prefix-array construction once, then count the work of each query.

</details>
<details>
<summary>Hint 3</summary>

The prefix array is auxiliary storage because the output is a scalar. Empty queries do not skip earlier code.

</details>

- [ ] **C6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C7. Count dominated candidates — optional advanced

**Difficulty:** Medium — optional advanced. **Objective:** account for deque expiry and dominance without multiplying loop bounds. **Prerequisites:** O1 and Stack amortization.

Input values are arbitrary ints, duplicates allowed, and k is positive. Return how many candidates this code removes **from the back** because of dominance; front expiry removals are not counted. k>n returns zero. Empty input is valid. The output is a diagnostic count, not window maxima or spreads.

```java
public static int c7(int[] values, int k) {
    if (k > values.length) return 0;
    Deque<Integer> candidates = new ArrayDeque<>();
    int backRemovals = 0;
    for (int right = 1; right <= values.length; right++) {
        int left = right - k;
        while (!candidates.isEmpty() && candidates.peekFirst() < left) {
            candidates.pollFirst();
        }
        int incoming = right - 1;
        while (!candidates.isEmpty() &&
                values[candidates.peekLast()] <= values[incoming]) {
            candidates.pollLast();
            backRemovals++;
        }
        candidates.offerLast(incoming);
    }
    return backRemovals;
}
```

Examples: `([6,2,2,5,1,5,3],3) -> 4`; `([3,2,1],2) -> 0`; `([1,2,3],3) -> 2`; `([5,5],1) -> 0`; `([],1) -> 0`. Explain the k=1 duplicate case and the order of expiry versus dominance. Follow-up: what input maximizes the number of retained candidates?

<details>
<summary>Hint 1</summary>

An index removed from either endpoint cannot be removed again.

</details>
<details>
<summary>Hint 2</summary>

Charge every removal to the earlier insertion of that index. The two while loops share candidate lifetimes.

</details>
<details>
<summary>Hint 3</summary>

Use the documented amortized endpoint costs of ArrayDeque, and bound peak unexpired candidates by k.

</details>

- [ ] **C7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 11. Debugging Challenges

Every snippet below is intentionally incorrect but valid Java 21. Its **required contract** describes the correct behavior. The table supplies test expectations, not the snippet's observed output. Repair the shown signature in your practice workspace.

For every D exercise: identify each defect; construct a failing input of your own; record observed versus required behavior; name the violated invariant or assumption; correct the code; verify termination and edge cases; and derive the corrected complexity. Check independent defects separately so one repair does not hide another. All shared read-only and non-null rules apply.

### D1. Growing totals masquerading as fixed windows

**Difficulty:** Medium. **Objective:** separate outgoing-state, initialization, and absence errors. **Prerequisites:** F2.

Required contract: return the maximum sum over length-k contiguous ranges of an arbitrary int array. k is positive; negatives and duplicates are valid. If k>n, including empty input, return `Long.MIN_VALUE`. This sentinel is outside every attainable sum under the shared bounds.

```java
public static long d1(int[] values, int k) {
    long sum = 0, best = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        if (right >= k) best = Math.max(best, sum);
    }
    return best;
}
```

| values; k | Required output |
| --- | --- |
| `[2,1,5,1]; 3` | `8` |
| `[-4,-2,-7]; 2` | `-6` |
| `[]; 1` | `LMIN` |
| `[7]; 1` | `7` |
| `[1,2]; 3` | `LMIN` |

Here LMIN means Long.MIN_VALUE. **Repair targets:** O(n) time, O(1) auxiliary. **Complete when:** every defect is independently explained, all-negative windows use a real candidate, and no-full-window behavior follows the contract.

<details>
<summary>Hint 1</summary>

Check what sum represents after more than k values have been processed.

</details>
<details>
<summary>Hint 2</summary>

A default best of zero may describe no legal candidate. Absence is another state that needs a deliberate representation.

</details>
<details>
<summary>Hint 3</summary>

Restore the exact fixed interval, initialize the optimum consistently with negative results, and preserve the absence sentinel until a full range exists.

</details>

- [ ] **D1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D2. A missing final substring

**Difficulty:** Medium. **Objective:** repair mixed boundary conventions and reassess repeated copying. **Prerequisites:** fixed ranges and String.substring.

Required contract: ASCII text, positive k; count every length-k contiguous substring containing no repeated char value. Input duplicates are allowed. Empty input or k>n returns zero. Matching is case-sensitive and overlapping candidates count separately.

```java
public static int d2(String text, int k) {
    int left = 0, count = 0;
    for (int right = 0; right < text.length(); right++) {
        if (right - left == k) {
            String part = text.substring(left, right);
            Set<Character> seen = new HashSet<>();
            for (int i = 0; i < part.length(); i++) {
                seen.add(part.charAt(i));
            }
            if (seen.size() == k) count++;
            left++;
        }
    }
    return count;
}
```

| text; k | Required output |
| --- | --- |
| `"abc"; 3` | `1` |
| `"abcd"; 2` | `3` |
| `"a"; 1` | `1` |
| `"aa"; 2` | `0` |
| `""; 1` | `0` |
| `"ab"; 3` | `0` |

**Repair targets:** O(n+128) time, O(128) auxiliary. First explain the coverage defect; then meet the target without constructing and rescanning every substring. **Complete when:** singleton/full-input/final-position candidates are included and the cost analysis counts all work in the original as well as repaired code.

<details>
<summary>Hint 1</summary>

Which exclusive right boundary is needed to include the last character?

</details>
<details>
<summary>Hint 2</summary>

Write the candidate intervals actually visited by the loop for text length 4 and k=2.

</details>
<details>
<summary>Hint 3</summary>

Choose one consistent end convention. Incremental frequencies can replace repeated substring and set construction after the coverage defect is understood.

</details>

- [ ] **D2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D3. Distinct keys that never disappear

**Difficulty:** Medium. **Objective:** align a map's physical entries with its logical state. **Prerequisites:** W1 and variable windows.

Required contract: arbitrary int array, nonnegative k; return the length of the longest contiguous range with at most k distinct values. Empty input and k=0 return zero. Negatives and duplicates are valid.

```java
public static int d3(int[] values, int k) {
    Map<Integer, Integer> counts = new HashMap<>();
    int left = 0, best = 0;
    for (int right = 1; right <= values.length; right++) {
        int incoming = values[right - 1];
        counts.put(incoming, counts.getOrDefault(incoming, 0) + 1);
        while (counts.size() > k) {
            int outgoing = values[left++];
            counts.put(outgoing, counts.get(outgoing) - 1);
        }
        best = Math.max(best, right - left);
    }
    return best;
}
```

| values; k | Required output |
| --- | --- |
| `[1,2]; 1` | `1` |
| `[1]; 0` | `0` |
| `[4,4]; 1` | `2` |
| `[]; 1` | `0` |

**Repair targets:** expected O(n) time, O(min(u,k+1)+1) auxiliary. **Complete when:** the repair handles zero-count transitions and cannot read past the active range. Show what size reports for a map with one zero-valued mapping and one positive-valued mapping.

<details>
<summary>Hint 1</summary>

Decreasing a value stored in a map does not automatically delete its key.

</details>
<details>
<summary>Hint 2</summary>

The condition assumes map.size equals the number of live kinds. State exactly when that assumption stops being true.

</details>
<details>
<summary>Hint 3</summary>

Remove exhausted mappings or maintain another correct distinct counter. Keep the rest of the frequency invariant intact.

</details>

- [ ] **D3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D4. Recording a duplicate-containing range

**Difficulty:** Medium. **Objective:** repair answer timing and incomplete validity restoration. **Prerequisites:** V2.

Required contract: ASCII, case-sensitive text; return the length of its longest contiguous substring with no repeated char value. Empty input returns zero. Input duplicates are valid.

```java
public static int d4(String text) {
    int[] counts = new int[128];
    int left = 0, best = 0;
    for (int right = 1; right <= text.length(); right++) {
        char incoming = text.charAt(right - 1);
        counts[incoming]++;
        best = Math.max(best, right - left);
        if (counts[incoming] > 1) {
            counts[text.charAt(left)]--;
            left++;
        }
    }
    return best;
}
```

| text | Required output |
| --- | --- |
| `"aa"` | `1` |
| `"abba"` | `2` |
| `"abc"` | `3` |
| `""` | `0` |

**Repair targets:** O(n+128) time, O(128) auxiliary. **Complete when:** you demonstrate why moving the best update alone is insufficient, repair all required removals, and compare against substring enumeration. Follow-up: when does this algorithm promise validity?

<details>
<summary>Hint 1</summary>

An expanded range can be invalid before any repair occurs.

</details>
<details>
<summary>Hint 2</summary>

Removing an unrelated first character may leave both copies of the incoming character inside the range.

</details>
<details>
<summary>Hint 3</summary>

Restore the full invariant before measuring a candidate. Verify two independent repairs on aa and abba.

</details>

- [ ] **D4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D5. A minimum that cannot improve

**Difficulty:** Medium. **Objective:** repair optimum initialization, recording order, and no-answer behavior. **Prerequisites:** V3.

Required contract: nonnegative int values, positive long target; return the length of the shortest nonempty contiguous range with sum>=target, or -1 if none exists. Empty input is valid and returns -1; duplicates and zeros are allowed.

```java
public static int d5(int[] values, long target) {
    int left = 0, best = 0;
    long sum = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        while (sum >= target) {
            sum -= values[left++];
            best = Math.min(best, right - left);
        }
    }
    return best;
}
```

| values; target | Required output |
| --- | --- |
| `[5]; 5` | `1` |
| `[2,3]; 10` | `-1` |
| `[1,4,2]; 6` | `2` |
| `[]; 1` | `-1` |
| `[0,0,5]; 5` | `1` |

**Repair targets:** O(n) time, O(1) auxiliary. **Complete when:** a sentinel represents “not found,” every recorded candidate is still valid, and absence is converted to -1. Explain why changing only the initial value does not fix the recording point.

<details>
<summary>Hint 1</summary>

Can any positive valid length improve a minimum initialized to zero?

</details>
<details>
<summary>Hint 2</summary>

Inspect the state just before and just after removing the only element from [5].

</details>
<details>
<summary>Hint 3</summary>

Record the valid range before destructive contraction, and distinguish an untouched sentinel from an actual answer.

</details>

- [ ] **D5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D6. Two kinds or two occurrences?

**Difficulty:** Medium. **Objective:** identify a validity-summary mismatch without rejecting correct alternate designs. **Prerequisites:** V4.

Required contract: ASCII case-sensitive text and required; return the shortest covering substring length, 0 for empty required, and -1 for impossible nonempty coverage. Required characters may repeat. Extra text characters are allowed.

```java
public static int d6(String text, String required) {
    if (required.isEmpty()) return 0;
    int[] need = new int[128], have = new int[128];
    int requiredKinds = 0;
    for (int i = 0; i < required.length(); i++) {
        char c = required.charAt(i);
        if (need[c] == 0) requiredKinds++;
        need[c]++;
    }
    int left = 0, formed = 0, best = Integer.MAX_VALUE;
    for (int right = 1; right <= text.length(); right++) {
        char incoming = text.charAt(right - 1);
        if (need[incoming] > 0) {
            have[incoming]++;
            if (have[incoming] <= need[incoming]) formed++;
        }
        while (formed == requiredKinds) {
            best = Math.min(best, right - left);
            char outgoing = text.charAt(left++);
            if (need[outgoing] > 0) {
                if (have[outgoing] <= need[outgoing]) formed--;
                have[outgoing]--;
            }
        }
    }
    return best == Integer.MAX_VALUE ? -1 : best;
}
```

| text; required | Required output |
| --- | --- |
| `"AA"; "AAB"` | `-1` |
| `"ABA"; "AAB"` | `3` |
| `"AB"; "AB"` | `2` |
| `""; "A"` | `-1` |
| `"AB"; ""` | `0` |

**Repair targets:** O(n+m+128) time, O(128) auxiliary. **Complete when:** you name what formed actually counts, choose one consistent satisfaction model, and test duplicate requirements and surplus removal. Both a matched-occurrence model and a satisfied-kind model can be valid; the defect is an inconsistent comparison, not the mere choice of model.

<details>
<summary>Hint 1</summary>

Track formed after adding the first and second A when the requirement is AAB.

</details>
<details>
<summary>Hint 2</summary>

Compare the unit counted by formed with the unit counted by requiredKinds.

</details>
<details>
<summary>Hint 3</summary>

Either change the comparison to match the occurrence-based state, or change both entering and leaving transitions to count satisfied kinds consistently.

</details>

- [ ] **D6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D7. Signed totals and a narrow accumulator

**Difficulty:** Medium. **Objective:** separate an invalid movement rule from numeric overflow. **Prerequisites:** R1-R2.

Required contract: arbitrary signed int values and long target in [-10^15,10^15]; return whether any **nonempty contiguous** exact-sum range exists. Duplicates and zeros are allowed. Empty input returns false. The target need not be attainable.

```java
public static boolean d7(int[] values, long target) {
    int left = 0, sum = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        while (left < right && sum > target) {
            sum -= values[left++];
        }
        if (left < right && sum == target) return true;
    }
    return false;
}
```

| values; target | Required output |
| --- | --- |
| `[4,-1]; 3` | `true` |
| `[MAX,MAX]; 4294967294` | `true` |
| `[3]; 3` | `true` |
| `[]; 0` | `false` |
| `[0]; 0` | `true` |

**Repair targets:** expected O(n) time, O(n) auxiliary. **Complete when:** you show independent witnesses for overflow and unjustified elimination, preserve the signed-input contract, and compare the corrected method against enumeration. Simply restricting input or widening sum is not a complete repair.

<details>
<summary>Hint 1</summary>

A later negative value can reduce an earlier excessive total.

</details>
<details>
<summary>Hint 2</summary>

The numeric type and the elimination argument are separate obligations. Repairing one does not establish the other.

</details>
<details>
<summary>Hint 3</summary>

Keep earlier prefix information using long totals, query before inserting the current boundary, and exclude empty ranges.

</details>

- [ ] **D7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D8. A repair loop that revisits the same position

**Difficulty:** Medium. **Objective:** restore state/boundary consistency and prove termination. **Prerequisites:** duplicate-free windows and sets.

Required contract: ASCII case-sensitive text; return the length of the longest contiguous substring with no repeated char value. Input duplicates and empty input are valid; empty returns zero.

```java
public static int d8(String text) {
    Set<Character> inside = new HashSet<>();
    int left = 0, best = 0;
    for (int right = 1; right <= text.length(); right++) {
        char incoming = text.charAt(right - 1);
        while (inside.contains(incoming)) {
            inside.remove(text.charAt(left));
        }
        inside.add(incoming);
        best = Math.max(best, right - left);
    }
    return best;
}
```

| text | Required behavior |
| --- | --- |
| `"abba"` | Return `2` and terminate |
| `"aa"` | Return `1` |
| `"abc"` | Return `3` |
| `""` | Return `0` |

**Repair targets:** expected O(n) time, O(min(n,128)+1) auxiliary. Run the suspected hang in an isolated process with a timeout. **Complete when:** the failing case terminates, the set describes exactly the retained range, and each successful while iteration has a progress argument. A set-based unique-window method can be correct; explain which update is missing here.

<details>
<summary>Hint 1</summary>

Removing an item from a collection does not change an integer boundary.

</details>
<details>
<summary>Hint 2</summary>

Trace the second b in abba. After removing a once, what will the next iteration try to remove?

</details>
<details>
<summary>Hint 3</summary>

Synchronize outgoing-state removal with boundary advancement, and check that the recorded length uses the repaired interval.

</details>

- [ ] **D8** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 12. Mock Interview Practice

Use existing exercises for mocks; these are not additional exercise IDs or duplicate logs. A typical Medium session may take roughly 35-45 minutes including discussion, implementation, tests, and follow-ups. Adjust to the interview format; a fast attempt with an unsupported invariant is not independent success.

| Session | Suggested selection | Evidence to collect |
| --- | --- | --- |
| Mechanics | Cold F2 and a changed W1 input | Boundary convention, outgoing state, negative totals, exact distinct counts |
| Validity | V2 or V3, followed by an R1 counterexample | Safe shrinking, answer timing, sign/alphabet assumptions |
| Repeated requirements | W3 or V4 | Multiplicity, surplus removal, impossible matches |
| Mixed selection | An unattempted X1-X5 statement with topic/hints hidden | Baseline, technique choice, valid invariant, actual total cost |
| Repair | One unattempted D challenge | Reproducible failure, invariant repair, termination, regression controls |
| Later transfer | A different unseen range problem in a separate session | Retained independent reasoning rather than remembered code |

For selected problems, explain aloud what each boundary means, what the active state represents, why an item enters/leaves, when the answer is recorded, why discarded candidates cannot improve it, why the loop terminates, and how total costs follow. If your chosen algorithm is not a window, explain its state with the same precision.

Mock-interviewer follow-ups:

- What is true immediately after expansion, and what is true only after repair?
- Why does this removal preserve every necessary candidate?
- Can a later negative value make a discarded start useful again?
- Is this counter counting occurrences, distinct kinds, or satisfied kinds?
- What happens when a surplus required character leaves?
- Why is an empty input different from an impossible nonempty requirement?
- How many times can left advance over the whole algorithm?
- What work is hidden in substring, a full-map comparison, or copying an input?
- Could a simpler scan or a different representation answer this workload more efficiently?
- Which changed assumption would invalidate your solution?

Do not consult a model answer before making a concrete attempt. Record the result and lesson on the exercised problem's existing record. If the solution is familiar, count the session as retention practice and choose a different unseen problem for readiness evidence.

## 13. Revision and Spaced Repetition

Use the [roadmap review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): reviews around **1, 3, 7, and 14 days after a learning attempt** are useful starting points, then increase gaps after repeated cold success. These are flexible aids, not deadlines. A missed date does not require restarting the topic.

A cold re-solve begins with the statement, blank editor, and closed hints/notes/old code. Reconstruct the baseline, algorithm choice, state, invariant, updates, tests, proof, and costs. Compare with earlier work only after finishing. Update the same attempt record with date, I/H/S, total minutes, lesson, and next review. For platform problems, update only the existing roadmap Log.

Prioritize:

1. S results, including unfinished work.
2. H results.
3. Incorrect invariants or invalid monotonicity assumptions.
4. Repeated boundary, zero-count, absence, or overflow mistakes.
5. Independently solved work due for review.

Repair the specific gap with a small relevant drill, then attempt a different variant. Memorized code can indicate familiarity, but does not establish transfer. There is no second revision ledger here.

## 14. Completion and Readiness

The [roadmap gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) and [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) remain authoritative. The following milestones describe evidence rather than duplicating the topic dashboard.

| Milestone | Evidence |
| --- | --- |
| Level 1 | F1-F4 reconstructed without copied code; correct boundaries, outgoing updates, averages, counts, absence, and numeric types |
| Level 2 core | W1-W3 and V1-V3 explained and implemented; fixed versus variable state, valid answer timing, duplicate repair, and total movement justified |
| Coverage breadth | V4's repeated-requirement reasoning demonstrated; distinguish kinds/occurrences and explain removal across a threshold |
| Validity judgment | R1 gives proofs or counterexamples; R2 supplies a correct signed exact-sum alternative |
| Essential platform practice | N1-N3 independently solved or cold re-solved, tested, and explained; supports P |
| Additional practice | N4 attempted after Essentials or for a frequency-budget gap; keep its A classification |
| Optional advanced | O1, C7, N5, and N6 deepen coverage/deque reasoning; optional Hard completion does not block ordinary readiness |
| Interview readiness | Repeated independent unfamiliar variants across separate sessions, including technique selection and communication |

For **T**, explain the essential concepts without notes. For **I**, demonstrate the roadmap's fixed-sum and distinct-character implementations, justified boundaries, and negative-number counterexample with an appropriate alternative. The broader Level 2 route builds reliability; exercise counts alone do not earn I. For **P**, independent Essential practice is required. For **R**, add unfamiliar transfer under the common rubric.

Before marking Sliding Window Interview Ready, demonstrate that you can choose between fixed and variable windows, justify validity under expansion/removal, maintain exact state, handle repeated requirements and missing answers, implement Java 21 safely, account for hidden work, and reject unsuitable windows. Solve unseen variants without hints across separate sessions and explain an alternative's tradeoff. Include at least one fixed-frequency task, one variable-validity task, and one technique-selection case over those sessions; they need not be optional Hard problems.

C/D work diagnoses gaps rather than imposing a quota. A failed cold attempt should trigger targeted review and an honest update to the affected roadmap gate. Keep the assessment date and compact unfamiliar-problem evidence at the [roadmap's Sliding Window readiness paragraph](./DSA_Interview_Roadmap.md#5-sliding-window).

### Coverage map

| Learning objective | Evidence in this workbook |
| --- | --- |
| Fixed boundaries, empty/singleton/k>n behavior | F1-F3, D1-D2 |
| Rolling sums, extrema of totals, averages, overflow | F2-F3, C2, D1, D7 |
| Frequency arrays and maps; outgoing counts | F4, W1-W3, C4, D3 |
| Longest valid, duplicate handling, answer timing | V2, X1, D4, D8 |
| At-most-K kinds and counting valid ranges | V1, W1 |
| Shortest valid and repeated coverage | V3-V4, D5-D6 |
| Validity counterexamples and signed alternatives | R1-R2, X2, D7 |
| Prefix preprocessing and independent queries | X5, C6 |
| Alternative technique selection | X1-X5, N1 |
| Amortized movement and hidden work | C1-C7 |
| Optional deque extrema and expiry | O1, C7, N6 |
| Independent explanation and retention | Mocks, existing attempt records, cold review |

## 15. References and Delayed Complexity Checks

### Repository references

| Reference | Purpose |
| --- | --- |
| [Sliding Window concepts](./Sliding_Window_Essential_Concepts.md) | Primary theory, derivations, and original teaching implementations |
| [Window invariants](./Sliding_Window_Essential_Concepts.md#3-window-boundaries-and-invariants) | State, validity, and answer timing |
| [Frequency state](./Sliding_Window_Essential_Concepts.md#8-frequency-counting-and-character-models) | Kinds, occurrences, alphabet models, and threshold crossings |
| [Amortized window costs](./Sliding_Window_Essential_Concepts.md#9-amortized-complexity-and-complete-costs) | Movement versus update/validation work |
| [Unsuitable windows](./Sliding_Window_Essential_Concepts.md#10-when-sliding-window-is-inappropriate) | Counterexamples and assumptions |
| [Prefix alternatives](./Sliding_Window_Essential_Concepts.md#11-prefix-sum-and-hashmap-alternatives) | Earlier boundaries and objective-specific state |
| [Roadmap](./DSA_Interview_Roadmap.md#5-sliding-window) | Required scope, E/A/C priorities, and canonical platform Logs |
| [Java Arrays](./Java_Array.md) and [Java Strings](./Java_String.md) | Indexing, copies, substring, and character units |
| [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) | Counting versus membership and expected hashing costs |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) | Complete time/space derivation |
| [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Prefix and lookup prerequisites |
| [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements) | Candidate-elimination and progress proofs |
| [Stack](./Stack/Stack_Essential_Concepts.md#13-deriving-time-and-space-complexity) | Candidate lifetime accounting |
| [Testing concepts](./Testing_Essential_Concepts.md) | Contract-based edge cases and independent oracles |

Java 21 API details were checked through Context7 against official Oracle documentation. [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) and [HashSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html) document their operations and hashing-dependent performance. [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) defines UTF-16 indices and exclusive substring ends. [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) documents endpoint behavior and amortized operation costs. The code's total complexity still depends on how often those operations are performed.

### Delayed complexity checks

Open these only after completing the eight analysis questions. They check costs and possible improvements, without giving implementations for the coding exercises.

<details>
<summary>C1 cost check</summary>

For length n, there are n(n+1)/2 nonempty ranges. The sum of all their lengths is n(n+1)(n+2)/6, so the shown code takes Θ(n³+1) time, O(1) auxiliary space, and scalar output. Keeping a running sum as right advances for each fixed left reduces a straightforward baseline to Θ(n²+1). That still resets the end scan for each start; it is not a single forward-only window.

</details>

<details>
<summary>C2 cost check</summary>

When 1<=k<=n, let w=n-k+1. Recompute takes Θ(wk) time; rolling takes Θ(n). Both use O(1) auxiliary space and scalar output. With k>n, each returns in O(1). Recompute is linear for k=1 and for k=n, and can be quadratic when k and w both scale with n. State the parameterized bound before collapsing it to a worst case over k.

</details>

<details>
<summary>C3 cost check</summary>

The outer boundary moves n times; the shared left boundary advances at most n times total. Successful while iterations are charged to removed positions, and at most one final failed check occurs per outer iteration. Total time is Θ(n+1), auxiliary space O(1), output scalar. Nonnegative values and budget>=0 ensure an empty range cannot remain over budget. A single iteration may remove many items without making total work quadratic.

</details>

<details>
<summary>C4 cost check</summary>

For 1<=k<=n and w=n-k+1, the array version performs O(128+n+128w) work, which is O(n) for a fixed ASCII alphabet. It uses O(128) auxiliary state. Replacing 128 by variable Σ gives O(Σ+n+Σw); initialization also counts. A separate distinct counter can reduce validation to constant time while keeping O(Σ) state.

The map version has expected O(n) time and O(min(u,k+1)+1) peak auxiliary state, where u is the distinct input alphabet. Counts must remain positive for size to mean distinct live kinds. A temporary extra key can appear before removal. Both return scalars, and both return immediately when k>n.

</details>

<details>
<summary>C5 cost check</summary>

The prefixes have lengths 1 through n, and each is scanned from scratch. Total time is expected Θ(n²+1), with the ordinary Java 21 cost model also charging materialized substring contents. Peak live auxiliary memory is O(n+u+1) for a current copied prefix and its set, where u<=128 here; scalar output is O(1). Cumulative copied content can be Θ(n²), which is different from peak live memory.

A retained set for the growing prefix can preserve the same diagnostic sum with expected O(n) work and O(u+1) auxiliary space, without constructing prefix strings. Merely saying right moves n times overlooks the restarted inner scans.

</details>

<details>
<summary>C6 cost check</summary>

Let n be values.length and q be queries.length. Time is Θ(n+q+1); the prefix array uses Θ(n+1) auxiliary space, while output is a scalar. Each query needs two boundary lookups and fixed arithmetic after preprocessing. With q=0, the shown implementation still constructs the prefix array; an early return could avoid that work. The input bounds make the long subtraction and absolute value safe.

</details>

<details>
<summary>C7 cost check</summary>

For k<=n, every index is appended once and removed at most once, from either the front or the back. The two removal loops do not give each occurrence two independent lifetimes. Total work is O(n), including amortized ArrayDeque endpoint costs, with O(k) peak auxiliary state and scalar output. A decreasing input can retain k candidates. The k>n path is O(1).

The back-removal count is at most n-1. For k=1, an older index expires before the incoming one is compared, so equal values need not cause any back removal. Changing processing order could change this diagnostic count even when a separately designed maximum-output algorithm remained correct.

</details>

### Validation scope

Authoring checks verified **172 table cases** directly from the document using independent enumeration, frequency counting, exact-integer arithmetic, and closed-form checks for large counts. All **35 Java blocks** compiled together for Java 21 with `--release 21 -Xlint:all -Werror`. A temporary runner passed **90 runtime checks** covering the analysis examples, API behavior, numeric bounds, and all eight deliberate defects. D8's infinite loop was reproduced in an isolated process with a timeout; partial repairs also confirmed independent defects in D4, D5, and D7.

All **53 local links and anchors** resolved. Content hashes confirmed that all **22 pre-existing files in DSA_Guide** were unchanged and this workbook was the only added file there. Temporary validation programs remained outside the repository.

These checks do not mark learner progress or verify a future learner implementation. Starter bodies remain deliberately unfinished. Use independent small-input oracles and the explicit contracts when testing your own work; hashing assumptions and alphabet restrictions remain part of the stated bounds.






