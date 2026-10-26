# Binary Search - Exercises (Java 21)

## 1. Introduction

This workbook turns the [Binary Search concepts guide](./Binary_Search_Essential_Concepts.md) into evidence that you can derive, implement, test, and explain an algorithm independently. The [roadmap's Binary Search topic](./DSA_Interview_Roadmap.md#4-binary-search) determines the required coverage and E/A/C priorities.

There are **31 original exercises**: 17 implementation problems, 7 complexity investigations, and 7 debugging challenges. There are also **6 curated platform problems**. Multiple methods inside one exercise share one attempt record.

| Level | Work | Purpose |
| --- | --- | --- |
| 1 | F1-F3 | Understand exact search, intervals, progress, and arithmetic |
| 2 | B1-B4, P1-P2, A1-A3 | Derive boundaries, rotated search, matrix search, and answer searches |
| 3 | N1-N6 | Apply the roadmap's curated problems with E/A/C priorities |
| 4 | X1-X5 | Choose techniques from unfamiliar statements |
| Supporting practice | C1-C7, D1-D7 | Account for total work and repair real defects |

Prerequisites are Java arrays, strings, collections, complexity analysis, Arrays & Hashing, Two Pointers, and Stack. Revisit [Java Arrays](./Java_Array.md), [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md), and [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md) when those skills obstruct an attempt. No tree, heap, graph, or dynamic-programming knowledge is required.

Read a problem before opening its hints or corresponding theory. Use a blank editor. A passing test is evidence about that input; your invariant explains all valid inputs. Keep implementation notes beside your own code, not in a new solutions document in this repository.

### Attempt records and authority

Every exercise starts unchecked. Check its box only after an attempt, testing or failure investigation, and a postmortem. A checked box does **not** mean independent success. Result codes follow the [roadmap's learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy):

| Result | Meaning |
| --- | --- |
| I | Solved independently, with correct Java, explanation, and edge-case checks |
| H | Needed a conceptual hint |
| S | Needed substantial solution review, or could not finish |
| — | Not attempted |

Record the date, result, total minutes including explanation, next review date, and main mistake or lesson. Opening a conceptual hint changes the attempt to H; substantial help or an unfinished attempt is S. Keep short histories on the existing record. For N1-N6, the workbook checkbox is a navigation aid: all detailed fields belong in that problem's **existing roadmap Log**, referenced below. Do not keep a second platform-problem history here.

Topic-level **T / I / P / R** remains solely in the [roadmap dashboard](./DSA_Interview_Roadmap.md#four-independent-progress-gates). Workbook ownership, checkboxes, and submission acceptance do not earn those gates automatically.

### Navigation

- [Workflow and shared contracts](#2-problem-solving-workflow)
- [Level 1](#3-level-1-fundamentals)
- [Level 2 route](#4-level-2-core-patterns)
- [Boundary drills](#5-dedicated-boundary-search-drills)
- [Rotated and matrix searches](#6-other-ordered-search-spaces)
- [Answer searches](#7-binary-search-on-answer-practice)
- [Level 3](#8-level-3-curated-platform-problems)
- [Level 4](#9-level-4-mixed-interview-challenges)
- [Complexity](#10-complexity-analysis-exercises)
- [Debugging](#11-debugging-challenges)
- [Mocks](#12-mock-interview-practice)
- [Review](#13-revision-and-spaced-repetition)
- [Readiness](#14-completion-and-readiness)
- [References and delayed checks](#15-references-and-delayed-complexity-checks)

## 2. Problem-Solving Workflow

Use these twelve steps for every implementation exercise, especially A1-A3 and X1-X5. In mixed challenges, decide which improvement the evidence supports; binary search is only one candidate.

1. Read the statement and restate the required output.
2. Clarify constraints, sortedness, duplicates, mutation, absence, and numeric ranges.
3. Propose a straightforward correct approach.
4. Determine that baseline's time and auxiliary space.
5. Identify a usable order or monotone condition, or explain its absence.
6. Determine the candidate search space, if a search is justified.
7. Establish an invariant before choosing updates.
8. Derive each boundary update and prove that it preserves candidates.
9. Implement in Java 21 with appropriate types.
10. Prove initialization, preservation, termination, and the returned result.
11. Analyze total complexity, including preprocessing and predicate work.
12. Test edge cases and compare against a simple independent oracle on small inputs.

### The ten-part invariant sheet

Complete this sheet for **F2, B1, B4, P1, A2, A3, and X5**. For X5, first justify your chosen method. For a different valid method, supply the equivalent state and progress proof.

| Item | Your explanation must identify |
| --- | --- |
| 1. Candidate space | Indices, boundary positions, values, or another domain |
| 2. Initial bounds | Their exact values and why the answer is covered |
| 3. Convention | Closed `[left,right]` or half-open `[left,right)`; distinguish element indices from possible boundary answers |
| 4. Invariant | Facts about both discarded regions and the retained answer |
| 5. Midpoint | Rounding direction and arithmetic safety |
| 6. Test | The precise comparison or predicate, including equality |
| 7. Elimination | Why each discarded candidate cannot be the required answer |
| 8. Progress | A nonnegative integer measure that strictly decreases |
| 9. Termination | The loop's exit condition and meaning |
| 10. Result | Why the returned index/value or sentinel satisfies the contract |

For each selected exercise, trace a normal and a boundary case using `left | right | mid | observation | discarded region | remaining measure`. Include a two-candidate state. Explain what breaks if you replace one justified update with `left = mid` or move a boundary past a still-possible answer. Do not copy a template and invent its invariant afterward. See [intervals and invariants](./Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants).

### Shared contracts and Java setup

These rules apply to every original exercise unless its statement narrows or overrides them.

- Inputs, rows, elements of object collections, and callbacks are non-null. All inputs are **read-only**. No concurrent changes occur.
- Each ordinary array or list has length `0..100_000`. Matrix outer length and each row length have that bound separately; their product may exceed `int`. Rows may share the same underlying array. B4 has its own virtual-domain bound.
- Integer elements may use the full signed `int` range unless stated otherwise. `MIN` and `MAX` in examples mean `Integer.MIN_VALUE` and `Integer.MAX_VALUE`; `LMIN` and `LMAX` mean the corresponding `long` limits.
- Indices are zero-based. Sorted means **nondecreasing**, so duplicates are permitted unless explicitly excluded. Specified sortedness, rectangularity, and monotonicity are preconditions; you need not validate them with an extra scan.
- Arrays in tables are mathematical input notation, not Java literals. `repeat(v,k)` means k copies of v. `[]` is empty. Use `L` on large Java integer literals.
- Auxiliary space excludes input and required output, but includes copies, helper collections, sorting workspace, and recursion frames. State output storage separately. Use `S_sort(n)` when a sorting implementation's workspace matters.
- Complexity targets describe worst-case work unless explicitly marked expected. Write `1 + log(n+1)` when a bound must include constant work on an empty input. Short forms such as `O(log(n+1))` below mean logarithmic search work plus constant setup/return work.
- Core search drills F1-F2, B1-B4, P1-P2, and A1-A3 must implement their search logic themselves. No `Arrays.binarySearch`, streams hiding the search, or copied subranges. B2-B3 may reuse your own boundary helpers.
- Standard Java 21 only. Put the member snippets into a class such as `Practice` and import `java.util.*` and `java.util.function.*`. Record declarations are class members. Starter bodies deliberately throw `UnsupportedOperationException`; replace them only in your own practice code.

For tests, compare arrays element by element and check input preservation. If any matching index is allowed, verify `index >= 0 && values[index] == target` and the required range, rather than demanding one index. Verify absence separately. Test empty, singleton, two-element, odd/even, duplicates/all-equal, negative, endpoint, missing, and overflow cases where the contract permits them. For small generated sorted arrays, compare boundary answers with a scan. For answer searches, enumerate a small domain and check both the answer and the adjacent infeasible candidate. Do not run deliberately nonterminating debug code in your main test process.

## 3. Level 1: Fundamentals

### F1. Compare a scan with a closed search

**Difficulty:** Easy. **Objective:** connect sorted order to candidate elimination. **Prerequisites:** array traversal and basic Big O.

Given a sorted `int[] values` and `int target`, implement a linear scan and an iterative binary search using a **closed** candidate interval. Both return any index containing the target, or `-1` if absent. Duplicates and empty input are valid; neither method promises the first occurrence. The target need not exist.

```java
public static int linearIndex(int[] values, int target) {
    // TODO: Return any matching index, or -1.
    throw new UnsupportedOperationException();
}
public static int closedIndex(int[] values, int target) {
    // TODO: Derive and maintain a closed candidate interval.
    throw new UnsupportedOperationException();
}
```

| values; target | Required result |
| --- | --- |
| `[2,5,8,12,16,23,38]; 23` | `5` |
| `[2,5,8,12,16,23,38]; 15` | `-1` |
| `[]; 7` | `-1` |
| `[7]; 7` / `[7]; 8` | `0` / `-1` |
| `[1,2]; 2` | `1` |
| `[4,4,4]; 4` | Any of `0,1,2` |
| `[MIN,0,MAX]; MIN` / target `MAX` | `0` / `2` |

**Targets:** scan `O(n)` time, binary search `O(log(n+1))` time; both `O(1)` auxiliary. **Complete when:** both pass the table and a 100,000-element sorted input; you trace the first two examples, explain initialization and the loop guard, and bound the number of value probes. Explain why their duplicate outputs may differ. Follow-up: what assumption would fail on an unsorted input?

- [ ] **F1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F2. Search only the requested range

**Difficulty:** Easy. **Objective:** maintain a half-open interval without losing original indices. **Prerequisites:** F1 and interval notation.

Given a sorted array, valid endpoints `0 <= from <= to <= values.length`, and an `int target`, search only **`[from,to)`**. Return any matching **original array index** inside that range, or `-1`. Duplicates and empty ranges are allowed. A match elsewhere does not count. No subarray copy is allowed.

```java
public static int rangeIndex(int[] values, int from, int to, int target) {
    // TODO: Search [from, to); preserve original index coordinates.
    throw new UnsupportedOperationException();
}
```

| values; from; to; target | Required result |
| --- | --- |
| `[1,3,3,3,8]; 2; 4; 3` | Either `2` or `3` |
| `[1,3,3,3,8]; 0; 1; 3` | `-1` |
| `[1,3,3,3,8]; 1; 1; 3` | `-1` |
| `[1,3,3,3,8]; 4; 5; 8` | `4` |
| `[]; 0; 0; 0` | `-1` |
| `[MIN,MAX]; 0; 2; MAX` | `1` |

**Targets:** with `k = to-from`, `O(log(k+1))` time and `O(1)` auxiliary. **Complete when:** the ten-part invariant sheet and tests pass, including a middle range in a large array. Explain why adding `from` again to an already-global result is wrong. Follow-up: why can `to` be a valid boundary but an invalid element index?

- [ ] **F2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F3. Two safe midpoints

**Difficulty:** Medium. **Objective:** control rounding and overflow independently of a search template. **Prerequisites:** Java integer division and widening.

For `long left, right` satisfying **`0 <= left <= right <= LMAX`**, return the mathematical floor and ceiling of `(left+right)/2`. The interval is inclusive and nonempty; there is always an answer. Do not use floating point, `BigInteger`, or a library midpoint operation.

```java
public record Midpoints(long lower, long upper) {}
public static Midpoints midpoints(long left, long right) {
    // TODO: Compute both midpoints without overflowing intermediate values.
    throw new UnsupportedOperationException();
}
```

| left; right | (lower, upper) |
| --- | --- |
| `0; 0` | `(0,0)` |
| `0; 1` | `(0,1)` |
| `2; 5` | `(3,4)` |
| `1500000000; 2000000000` | `(1750000000,1750000000)` |
| `0; LMAX` | `(4611686018427387903,4611686018427387904)` |
| `LMAX-1; LMAX` | `(LMAX-1,LMAX)` |
| `LMAX; LMAX` | `(LMAX,LMAX)` |

**Targets:** `O(1)` time and auxiliary space; fixed-size output. **Complete when:** all examples pass and you prove `left <= lower <= upper <= right` and `upper-lower <= 1`. Explain why a lower midpoint paired with `left = mid` can fail to progress. Follow-up: why is “store the sum in a long” insufficient if the addition already overflowed?

<details>
<summary>Hint 1</summary>

The distance between the endpoints fits even when their sum does not.

</details>
<details>
<summary>Hint 2</summary>

Express both answers relative to the left endpoint. An odd distance separates the two answers.

</details>
<details>
<summary>Hint 3</summary>

For upward rounding, split division and remainder. Adding one to a maximum-sized distance before division can overflow.

</details>

- [ ] **F3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 4. Level 2: Core Patterns

This route maps objectives to their single exercise location. F1-F2 already supply exact search; re-solve them cold rather than creating duplicate implementations here.

| Objective | Exercise | Evidence |
| --- | --- | --- |
| Exact search; closed and half-open intervals | F1-F2 | Correct absence and progress arguments |
| Lower/upper bounds; insertion positions | B1 | Both strictness choices and n sentinel |
| First/last occurrence; frequency | B2 | Equality guards and duplicate counts |
| Strict/inclusive predecessors; timestamps | B3 | Last eligible index, including duplicate times |
| First-true / last-true | B4 | Both monotone directions and empty/all-false cases |
| Rotated distinct arrays | P1 | Justified sorted-half selection |
| Globally ordered matrix | P2 | Virtual indexing and a wide domain |
| Minimum feasible answer | A1-A2 | Bounds, monotonicity, and checker cost |
| Maximum feasible answer | A3 | Feasible prefix and safe progress |
| Transfer and technique selection | X1-X5 | Independent choice after Level 3 practice |

Use [boundary theory](./Binary_Search_Essential_Concepts.md#5-boundary-search), [rotated search](./Binary_Search_Essential_Concepts.md#6-binary-search-in-rotated-sorted-arrays), and [answer search](./Binary_Search_Essential_Concepts.md#7-binary-search-on-answer) after a concrete attempt. For every core drill, a remembered implementation without an invariant is unfinished evidence.

## 5. Dedicated Boundary Search Drills

### B1. Before and after equal values

**Difficulty:** Medium. **Objective:** distinguish lower and upper boundaries. **Prerequisites:** F2 and sorted-prefix reasoning.

For sorted `int[] values` and `int target`, return the first index whose value is **>= target** in `lowerBound` and **> target** in `upperBound`. If no element qualifies, return `n`. These are insertion positions before and after existing equal values; the array is not modified. Duplicates and empty input are valid. Boundary answers lie in inclusive `[0,n]`, although valid element indices stop at `n-1`.

```java
public static int lowerBound(int[] values, int target) {
    // TODO: First value >= target, or values.length.
    throw new UnsupportedOperationException();
}
public static int upperBound(int[] values, int target) {
    // TODO: First value > target, or values.length.
    throw new UnsupportedOperationException();
}
```

| values; target | (lower, upper) |
| --- | --- |
| `[1,3,3,3,8]; 3` | `(1,4)` |
| `[1,3,3,3,8]; 5` | `(4,4)` |
| `[1,3,3,3,8]; 0` | `(0,0)` |
| `[1,3,3,3,8]; 9` | `(5,5)` |
| `[]; 3` | `(0,0)` |
| `[4,4,4]; 4` | `(0,3)` |
| `[MIN,MAX]; MAX` | `(1,2)` |

**Targets:** `O(log(n+1))` time per method, `O(1)` auxiliary. **Complete when:** both ten-part sheets, traces, and scan-oracle tests pass. Include singleton matches and missing targets between neighboring values. Follow-up: why does an equality hit not immediately determine either boundary?

<details>
<summary>Hint 1</summary>

Ask what must be true of every value strictly before the desired position.

</details>
<details>
<summary>Hint 2</summary>

For each method, separate an already-rejected prefix from an already-qualified suffix. The answer may be the boundary n.

</details>
<details>
<summary>Hint 3</summary>

Derive which side keeps mid when it qualifies. The comparison's equality case differs between the methods.

</details>

- [ ] **B1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### B2. Summarize an occurrence block

**Difficulty:** Medium. **Objective:** turn boundaries into verified occurrence answers. **Prerequisites:** B1.

Given sorted `int[] values` and `int target`, return `Occurrence(first,last,count)`. First and last are **inclusive element indices**, and count is the number of equal elements. If absent, return exactly `(-1,-1,0)`. Duplicates and empty input are valid; a match is not guaranteed. You may call your own B1 methods.

```java
public record Occurrence(int first, int last, int count) {}
public static Occurrence occurrences(int[] values, int target) {
    // TODO: Guard boundary positions before treating them as matches.
    throw new UnsupportedOperationException();
}
```

| values; target | (first, last, count) |
| --- | --- |
| `[1,3,3,3,8]; 3` | `(1,3,3)` |
| `[1,3,3,3,8]; 5` | `(-1,-1,0)` |
| `[1,3,3,3,8]; 1` / target `8` | `(0,0,1)` / `(4,4,1)` |
| `[]; 0` | `(-1,-1,0)` |
| `[4,4,4]; 4` | `(0,2,3)` |
| `[MAX]; MAX` | `(0,0,1)` |
| `[7]; 6` | `(-1,-1,0)` |

**Targets:** `O(log(n+1))` time, `O(1)` auxiliary and fixed-size output. **Complete when:** the result matches a scan oracle and you explain both the absence guard and the conversion from an exclusive endpoint to an inclusive last index. Follow-up: can an arbitrary `Arrays.binarySearch` hit replace the first occurrence?

<details>
<summary>Hint 1</summary>

Equal values form one contiguous block.

</details>
<details>
<summary>Hint 2</summary>

Describe that block with two boundary positions; neither boundary alone guarantees membership.

</details>
<details>
<summary>Hint 3</summary>

Handle empty and absent blocks before accessing an element. Avoid expressing the strict boundary using target + 1.

</details>

- [ ] **B2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### B3. Strict and inclusive timestamp predecessors

**Difficulty:** Medium. **Objective:** distinguish the latest timestamp before a query from the latest at or before it. **Prerequisites:** B1 and `long` comparisons.

Given sorted `long[] times` and a full-range `long query`, return `int[2]`: the largest index with `times[i] < query`, then the largest index with `times[i] <= query`. Each missing answer is `-1`. Empty input and duplicate timestamps are valid; the inclusive answer chooses the **last** equal timestamp. All values may span `LMIN..LMAX`. Do not adjust query by adding or subtracting one.

```java
public static int[] predecessors(long[] times, long query) {
    // TODO: Return [strict predecessor index, inclusive predecessor index].
    throw new UnsupportedOperationException();
}
```

| times; query | Output |
| --- | --- |
| `[10,20,20,35]; 20` | `[0,2]` |
| `[10,20,20,35]; 25` | `[2,2]` |
| `[10,20,20,35]; 9` | `[-1,-1]` |
| `[10,20,20,35]; 40` | `[3,3]` |
| `[]; 0` | `[-1,-1]` |
| `[LMIN,LMAX]; LMIN` | `[-1,0]` |
| `[LMIN,LMAX]; LMAX` | `[0,1]` |
| `[5,5]; 5` | `[-1,1]` |

**Targets:** `O(log(n+1))` time, `O(1)` auxiliary; two output integers. **Complete when:** both strictness choices pass the table and random scan comparisons. Explain the relationship to lower/upper boundaries and why time differences need not be computed. Follow-up: which extra structure would separate histories for multiple string keys?

<details>
<summary>Hint 1</summary>

The last eligible element sits immediately before the first ineligible one.

</details>
<details>
<summary>Hint 2</summary>

The two versions differ in whether equality belongs to the eligible prefix.

</details>
<details>
<summary>Hint 3</summary>

Search using direct long comparisons. Convert the resulting boundary to an index and preserve -1 when the prefix is empty.

</details>

- [ ] **B3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### B4. Two monotone oracle contracts

**Difficulty:** Medium. **Objective:** derive searches without an underlying array. **Prerequisites:** B1, `IntPredicate`, and boolean monotonicity.

Input is `int n` in `0..MAX` plus a deterministic `IntPredicate` defined only for indices `0..n-1`. Each evaluation costs `O(1)` time and space. For `firstTrue`, responses have shape false-then-true; return the first true index, or `n` if none. For `lastTrue`, responses have shape true-then-false; return the last true index, or `-1` if none. Either run may be empty. For `n=0`, never call the predicate. Never evaluate sentinel index n.

```java
public static int firstTrue(int n, IntPredicate predicate) {
    // TODO: False prefix, true suffix; absent answer is n.
    throw new UnsupportedOperationException();
}
public static int lastTrue(int n, IntPredicate predicate) {
    // TODO: True prefix, false suffix; absent answer is -1.
    throw new UnsupportedOperationException();
}
```

| Method; response sequence | Output |
| --- | --- |
| `firstTrue; [false,false,false,true,true]` | `3` |
| `lastTrue; [true,true,true,false,false]` | `2` |
| `firstTrue; [false,false]` / `lastTrue; [false,false]` | `2` / `-1` |
| `firstTrue; [true,true]` / `lastTrue; [true,true]` | `0` / `1` |
| `firstTrue; []` / `lastTrue; []` | `0` / `-1` |
| `firstTrue; [true]` / `lastTrue; [true]` | `0` / `0` |

Also test virtual `n=MAX` with `i -> i == MAX-1` for firstTrue: answer `MAX-1`. With an always-false callback, answers are `MAX` and `-1` respectively. Allocate no boolean array for these tests.

**Targets:** `O(log(n+1))` predicate calls, `O(1)` auxiliary. **Complete when:** both ten-part sheets are derived independently; every possible transition for small n passes; callbacks record no invalid probes. Explain how total cost changes when one evaluation takes `O(k)`. Follow-up: is having at least one true answer sufficient to justify binary search?

<details>
<summary>Hint 1</summary>

An observation is useful only because it constrains all responses on one side.

</details>
<details>
<summary>Hint 2</summary>

Write the known prefix and suffix facts for each direction. A boundary answer can lie just outside the element domain.

</details>
<details>
<summary>Hint 3</summary>

Keep sentinel positions as boundaries rather than evaluating them. Check zero- and two-candidate intervals explicitly.

</details>

- [ ] **B4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 6. Other Ordered Search Spaces

### P1. Locate a rotated distinct value

**Difficulty:** Medium. **Objective:** justify eliminating a half when the full array is not sorted. **Prerequisites:** F1 and sorted-half reasoning.

The input `int[] values` is a rotation of a **strictly increasing** array. No duplicates are permitted. Empty and unrotated arrays are valid. Given an `int target`, return its unique original index or `-1` if absent.

```java
public static int rotatedIndex(int[] values, int target) {
    // TODO: Identify a sorted half and justify whether it can contain target.
    throw new UnsupportedOperationException();
}
```

| values; target | Output |
| --- | --- |
| `[14,18,22,3,6,9,11]; 6` | `4` |
| `[14,18,22,3,6,9,11]; 18` | `1` |
| `[14,18,22,3,6,9,11]; 7` | `-1` |
| `[2,1]; 1` / target `2` | `1` / `0` |
| `[1,2,3]; 3` | `2` |
| `[MAX,MIN]; MIN` | `1` |
| `[]; 1` / `[4]; 4` | `-1` / `0` |

**Targets:** `O(log(n+1))` time, `O(1)` auxiliary. **Complete when:** the invariant sheet explains both sorted-half branches, every rotation of small distinct arrays passes, and missing targets terminate. Follow-up: explain why allowing many duplicate values can remove the evidence needed for logarithmic elimination; do not treat duplicates as valid inputs to this version.

<details>
<summary>Hint 1</summary>

The rotation introduces at most one drop inside the array.

</details>
<details>
<summary>Hint 2</summary>

After checking the midpoint, identify a half whose endpoints certify its order. Compare the target with that half's value range.

</details>
<details>
<summary>Hint 3</summary>

Make endpoint inclusivity consistent with the already-checked midpoint. Test a two-element rotation before a large example.

</details>

- [ ] **P1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### P2. First eligible matrix position

**Difficulty:** Medium. **Objective:** search a virtual order without flattening storage. **Prerequisites:** B1, rectangular arrays, widening arithmetic.

Given a rectangular `int[][] matrix` and `int target`, rows are sorted and every row's last value is **<=** the next row's first value. Duplicates are allowed. Return the first **row-major flattened position** whose value is `>= target`, as a `long`. If none qualifies, return `N = rows * columns`. Empty outer arrays and zero-column matrices have N=0 and return 0. Shared row arrays are permitted; never mutate or flatten the matrix.

```java
public static long matrixLowerBound(int[][] matrix, int target) {
    // TODO: Search virtual positions; return N when no value qualifies.
    throw new UnsupportedOperationException();
}
```

| matrix; target | Output |
| --- | --- |
| `[[1,4,7],[9,13,18]]; 8` | `3` |
| `[[1,4,7],[9,13,18]]; 13` | `4` |
| `[[1,4,7],[9,13,18]]; 19` | `6` |
| `[[1,4,7],[9,13,18]]; 0` | `0` |
| `[[2,2],[2,3]]; 2` / target `3` | `0` / `3` |
| `[]; 1` / `[[],[]]; 1` | `0` / `0` |
| `[[MAX]]; MAX` | `0` |

Large test: create one 50,000-element zero row and a 50,000-element outer array whose every entry refers to that row. It is a valid globally ordered matrix with **2,500,000,000 virtual elements**. Target 1 returns `2500000000L`; target 0 returns 0.

**Targets:** `O(log(N+1))` time and `O(1)` auxiliary. **Complete when:** the table and shared-row test pass, multiplication is widened before it occurs, and you explain why no division by zero or sentinel dereference occurs. Follow-up: are independently sorted rows and columns enough to establish this row-major order?

<details>
<summary>Hint 1</summary>

The output describes a position in an ordered sequence, even though that sequence is not physically stored as one array.

</details>
<details>
<summary>Hint 2</summary>

Use a wide boundary domain. Translate only actual element positions into row and column coordinates.

</details>
<details>
<summary>Hint 3</summary>

Separate empty dimensions before coordinate arithmetic. A cast after an overflowing int product does not repair that product.

</details>

- [ ] **P2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 7. Binary Search on Answer Practice

For A1-A3, begin with a small enumerated baseline. Write a separate feasibility specification before search code: candidate meaning, true/false direction, checker cost, a monotonicity proof, and guaranteed bounds. A search with an O(n) checker has that scan cost at every probe. Read [answer-search theory](./Binary_Search_Essential_Concepts.md#7-binary-search-on-answer) only after your first attempt.

### A1. First capacity day

**Difficulty:** Medium. **Objective:** derive a minimum feasible answer in a simple numeric domain. **Prerequisites:** B4 and sums of consecutive integers.

Day k adds k units of capacity, so after day k the total is `1+2+...+k`. Given `long quota` in `0..1_000_000_000`, return the smallest nonnegative day whose cumulative capacity is **at least** quota. Day zero has capacity zero. An answer always exists in inclusive `[0,quota]`. There are no array inputs or duplicate semantics. Do not use floating-point roots.

```java
public static long firstCapacityDay(long quota) {
    // TODO: Define a safe constant-time checker and find the earliest day.
    throw new UnsupportedOperationException();
}
```

| quota | Output |
| --- | --- |
| `0` | `0` |
| `1` | `1` |
| `3` | `2` |
| `10` | `4` |
| `11` | `5` |
| `1000000000` | `44721` |

**Targets:** `O(log(quota+1))` time, `O(1)` auxiliary. **Complete when:** you compare small quotas against day-by-day accumulation, prove the bound, and check the returned day and its predecessor when positive. For the large test, capacities on days 44720 and 44721 are 999961560 and 1000006281. Explain why the arithmetic fits long for this candidate domain. Follow-up: what part of your reasoning must change if quota can be LMAX?

<details>
<summary>Hint 1</summary>

Waiting another day cannot reduce accumulated capacity.

</details>
<details>
<summary>Hint 2</summary>

Describe a false prefix followed by a true suffix over days. Establish the high endpoint without guessing a fixed constant.

</details>
<details>
<summary>Hint 3</summary>

Use the arithmetic-series sum with operands widened before multiplication. Check the largest intermediate value permitted by these bounds.

</details>

- [ ] **A1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### A2. Earliest production total

**Difficulty:** Medium. **Objective:** separate a scanning predicate from minimum-feasible search. **Prerequisites:** A1, integer division, and long accumulators.

Each independent source with positive period p finishes units at times p, 2p, 3p, and so on. Given unsorted `int[] periods` with elements `1..1_000_000_000` and `long required` in `0..1_000_000_000`, return the earliest nonnegative integer time at which **at least** required units have finished across all sources. Duplicate periods are allowed. Required zero returns 0, including with no sources. Empty sources with positive required return `-1`; otherwise an answer exists. Finishes at the queried time count.

```java
public static long earliestProductionDay(int[] periods, long required) {
    // TODO: Establish bounds and search using a separately reasoned checker.
    throw new UnsupportedOperationException();
}
public static boolean productionReady(int[] periods, long day, long required) {
    // TODO: day is in [0, 10^18]; decide whether enough units have finished.
    throw new UnsupportedOperationException();
}
```

The helper accepts the same periods/required bounds and any day in `0..10^18`, including empty input. Its answer for required zero is true.

| periods; required | Output of earliestProductionDay |
| --- | --- |
| `[2,3]; 5` | `6` |
| `[2,3]; 1` | `2` |
| `[3,3,3]; 4` | `6` |
| `[7]; 3` | `21` |
| `[]; 1` / `[]; 0` | `-1` / `0` |
| `[1000000000]; 1000000000` | `1000000000000000000` |

Checker tests: `([2,3], day=5, required=5) -> false`; day 6 gives true. With 100,000 periods of 1, day `10^18`, required `10^9`, the answer is true even though summing every contribution without a threshold guard can overflow long.

**Targets:** checker `O(n)` time; total `O(n * (1+log R))` for nonempty input, where R is your bounded candidate-count; `O(1)` auxiliary. Constant time for immediate empty/zero exits is allowed. **Complete when:** the ten-part sheet proves monotonicity and bounds, checker tests are independent of search tests, small answers match time enumeration, and accumulation cannot overflow. Follow-up: why can the fastest source alone establish a valid upper bound?

<details>
<summary>Hint 1</summary>

Count completed units at a candidate time instead of simulating every completion.

</details>
<details>
<summary>Hint 2</summary>

Counts never decrease as time increases. An upper bound needs a feasible construction, even if it is far from optimal.

</details>
<details>
<summary>Hint 3</summary>

Widen bound multiplication before it happens. Once enough units are known to exist, exact accumulation of all remaining contributions is unnecessary.

</details>

- [ ] **A2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### A3. Largest allocation cap

**Difficulty:** Medium. **Objective:** derive a maximum feasible answer and reject an unsuitable predicate. **Prerequisites:** B4 and A2.

Given unsorted nonnegative `int[] requests` and `long budget` in `0..LMAX`, choose an integer cap c in inclusive `[0,max(requests)]`. Each request receives `min(request,c)` units. Return the **largest** cap whose total allocation is **at most** budget. Duplicate and zero requests are valid. For empty input, define the only candidate as c=0 and return 0. An answer always exists because c=0 is feasible. If every cap fits, return the maximum request.

```java
public static int largestAllocationCap(int[] requests, long budget) {
    // TODO: Derive a maximum-feasible search and guarantee progress.
    throw new UnsupportedOperationException();
}
```

| requests; budget | Output |
| --- | --- |
| `[2,7,9]; 10` | `4` |
| `[2,7,9]; 9` | `3` |
| `[2,7,9]; 0` | `0` |
| `[2,7,9]; 18` | `9` |
| `[5,5]; 9` | `4` |
| `[]; 7` / `[0,0]; 0` | `0` / `0` |
| `[MAX,MAX]; MAX` | `1073741823` |
| `[MAX]; MAX` | `MAX` |

**Targets:** `O(n * (1+log R))` time for nonempty input, with R your candidate-count; `O(1)` auxiliary. Empty input costs constant time. **Complete when:** the ten-part sheet and exhaustive small-cap comparisons pass; the maximum cap is safe; sums are long; an all-true predicate does not stall.

**Required counterexample:** someone proposes “total allocation **equals** budget” as the predicate. List its truth values for caps 0 through 9 with requests [2,7,9] and budget 10. Explain why it is unsuitable for first-true or last-true search. Then use budget 9 to show that the original problem still has an answer even when no cap spends exactly the budget. Follow-up: which midpoint rounding works with your chosen retained endpoint?

<details>
<summary>Hint 1</summary>

Raising a cap never lowers the total allocation.

</details>
<details>
<summary>Hint 2</summary>

Feasible caps form a prefix. Decide whether you keep a known feasible endpoint or save a best answer outside the unresolved interval.

</details>
<details>
<summary>Hint 3</summary>

Check the two-candidate state and the MAX endpoint. Using a wider internal bound or a carefully computed upper midpoint can prevent overflow and stalled updates.

</details>

- [ ] **A3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 8. Level 3: Curated Platform Problems

The following order, difficulties, and priorities match the [roadmap practice list](./DSA_Interview_Roadmap.md#4-binary-search). **Difficulty** is the platform label. **Priority** is this roadmap's learning choice: E Essential, A Additional, C Optional advanced challenge. Read each official statement for its current contract; the short objectives below do not replace it.

For each problem, complete the twelve-step workflow, test the submitted code, and explain the invariant aloud. Keep solutions and editorials closed on an independent attempt. A remembered accepted implementation does not establish transfer.

### N1. Binary Search

**Priority: E. Difficulty: Easy.** [Official problem](https://leetcode.com/problems/binary-search/).

Objective: implement exact search with a consistent interval, safe midpoint, correct missing-target result, and justified progress. Preparation: F1-F2. Completion evidence: independent implementation, ordinary/minimal/missing tests, and a time/space argument. Follow-up: what changes if the required output becomes the first equal index?

- [ ] **N1** — Canonical attempt record: existing **Binary Search** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N2. Search a 2D Matrix

**Priority: A. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/search-a-2d-matrix/).

Objective: establish the global ordering required by a virtual flattened search and keep coordinate arithmetic safe. Preparation: P2. Completion evidence: distinguish membership from a boundary result, test row transitions, and account for rectangular dimensions. Follow-up: would the same method work if only each row and each column were sorted?

- [ ] **N2** — Canonical attempt record: existing **Search a 2D Matrix** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N3. Search in Rotated Sorted Array

**Priority: E. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/search-in-rotated-sorted-array/).

Objective: recognize a sorted half, determine whether it can contain the target, and preserve all remaining candidates. Preparation: P1. Completion evidence: handle both orientations, no rotation, a two-element rotation, and absence; derive each inequality. Follow-up: which conclusion becomes unreliable with repeated values around the pivot?

- [ ] **N3** — Canonical attempt record: existing **Search in Rotated Sorted Array** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N4. Koko Eating Bananas

**Priority: E. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/koko-eating-bananas/).

Objective: define a monotone speed predicate, derive a valid answer domain, and count the work of all pile checks. Preparation: A1-A2. Completion evidence: explain why faster speeds cannot require more hours, implement integer ceiling division safely, and use a sufficiently wide hours accumulator. State total complexity in terms of pile count and searched speed range. Follow-up: why is equality with the hour limit the wrong general definition of feasibility?

- [ ] **N4** — Canonical attempt record: existing **Koko Eating Bananas** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N5. Time Based Key-Value Store

**Priority: A. Difficulty: Medium.** [Official problem](https://leetcode.com/problems/time-based-key-value-store/).

Objective: combine key-specific histories with predecessor search; explain the timestamp-order assumption that permits efficient append and lookup. Preparation: B3 and [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md). Completion evidence: distinguish absent keys from queries before their first timestamp, test exact/in-between/later queries, and analyze per-key history length separately from the number of keys. Follow-up: what changes if insertions arrive out of timestamp order?

- [ ] **N5** — Canonical attempt record: existing **Time Based Key-Value Store** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

### N6. Median of Two Sorted Arrays

**Priority: C. Difficulty: Hard.** [Official problem](https://leetcode.com/problems/median-of-two-sorted-arrays/).

Objective: investigate a partition search whose correctness depends on cross-array boundary relationships. Attempt after core drills and Essential practice are reliable. Compare with a simpler merged-order baseline, then justify which partition domain can be searched. Handle empty-side partitions, odd/even total length, and safe averaging. No full implementation is provided here.

This is an **optional advanced investigation**. It does not block ordinary Binary Search T/I/P/R or internship readiness.

- [ ] **N6** — Canonical attempt record: existing **Median of Two Sorted Arrays** Log in the [roadmap](./DSA_Interview_Roadmap.md#4-binary-search). Fields to maintain there: Last date —; Result —; Minutes —; Next review —; Lesson —.

## 9. Level 4: Mixed Interview Challenges

Hide this workbook's topic label and the hints before attempting these problems. Titles describe the task, not the intended technique. Prerequisites are only the earlier arrays, hashing, sorting, two-pointer, and search skills; choose among them yourself.

For each challenge, compare at least two plausible approaches. State whether an order or monotone condition exists, what the candidate space would be, whether discarding half is justified, and whether preprocessing is worthwhile. Meet the target without changing mutation or output semantics. Hints may reveal a technique when deliberately opened.

### X1. Archival rank requests

**Difficulty:** Medium. **Objective:** choose useful preprocessing for repeated queries. **Prerequisites:** earlier sequence-processing skills.

Given unsorted `int[] values` and unsorted `int[] queries`, return an `int[]` in query order. For each query q, report how many **positions** in values hold a number **<= q**. Duplicate values each contribute one; repeated queries get repeated answers. Empty values produce zero for every query; empty queries produce an empty result. Every count exists and fits int. Preserve both inputs.

```java
public static int[] archivalRanks(int[] values, int[] queries) {
    // TODO: Choose and justify preprocessing for these read-only inputs.
    throw new UnsupportedOperationException();
}
```

| values; queries | Output |
| --- | --- |
| `[4,1,4,-2]; [4,0,3,5]` | `[4,1,2,4]` |
| `[]; [0,1]` | `[0,0]` |
| `[4,1]; []` | `[]` |
| `[MIN,MAX,MIN]; [MIN,0,MAX]` | `[2,2,3]` |
| `[5,5]; [4,5,6]` | `[0,2,2]` |

**Targets:** with n values and m queries, `O(n log(n+1) + m(1+log(n+1)))` time or better; `O(n)` auxiliary is allowed; output `O(m)`. Account for every copy and any library workspace. **Complete when:** tests match a per-query scan oracle, inputs remain unchanged, and your reasoning counts repeated positions correctly. Follow-up: for just one query, is the preprocessing still justified?

<details>
<summary>Hint 1</summary>

All queries ask the same kind of question about a fixed collection.

</details>
<details>
<summary>Hint 2</summary>

Consider creating an order once. The result for a query is a boundary position, not a matching value's arbitrary index.

</details>
<details>
<summary>Hint 3</summary>

Preserve the original by copying before sorting, then choose the equality direction that counts all values equal to q. Include the copy and sorting workspace in space analysis.

</details>

- [ ] **X1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X2. Invoice origins

**Difficulty:** Medium. **Objective:** preserve the information repeated lookups actually need. **Prerequisites:** earlier collection and array skills.

Given unsorted `int[] ids` and unsorted `int[] queries`, return for each query the **first original index** with that ID, or `-1` if absent. Duplicate IDs and repeated queries are valid. Empty ids gives only missing results; empty queries gives an empty output. IDs can be negative or use int extremes. Preserve input order and contents.

```java
public static int[] invoiceOrigins(int[] ids, int[] queries) {
    // TODO: Preserve first-position semantics while answering repeated queries.
    throw new UnsupportedOperationException();
}
```

| ids; queries | Output |
| --- | --- |
| `[8,4,8,-1]; [8,5,-1]` | `[0,-1,3]` |
| `[]; [8]` | `[-1]` |
| `[8]; []` | `[]` |
| `[MAX,MIN,MAX]; [MAX,MIN,0]` | `[0,1,-1]` |
| `[4,4]; [4,4]` | `[0,0]` |

**Targets:** expected `O(n+m)` time, `O(u)` auxiliary for u distinct IDs; output `O(m)`. State and justify the data-structure assumptions behind an expected bound. **Complete when:** results match a scan oracle, duplicates keep their earliest index, and you state why raw binary search on ids lacks a valid elimination rule. Follow-up: what information would sorting need to retain, and what cost would it add?

<details>
<summary>Hint 1</summary>

The output requires original positions, not ordered ranks.

</details>
<details>
<summary>Hint 2</summary>

Summarize each distinct ID by the earliest position seen so far.

</details>
<details>
<summary>Hint 3</summary>

A map can answer repeated lookups. Avoid replacing an existing first index when a duplicate appears, and distinguish absence from the valid index zero.

</details>

- [ ] **X2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X3. Paired allowance count

**Difficulty:** Medium. **Objective:** exploit relationships between two ordered sequences. **Prerequisites:** earlier sequence-processing and numeric-bound skills.

Given two sorted `int[]` arrays a and b and a full-range `long limit`, count all cross-array index pairs `(i,j)` such that the mathematical sum `a[i]+b[j] <= limit`. Duplicate positions are distinct choices. Negative values are allowed; arrays may be empty, giving zero pairs. Return a `long` count, which can reach `10^10` under the shared size bounds. No pairs are output individually.

```java
public static long pairedAllowanceCount(int[] a, int[] b, long limit) {
    // TODO: Count every qualifying index pair exactly once.
    throw new UnsupportedOperationException();
}
```

| a; b; limit | Output |
| --- | --- |
| `[-2,1,4]; [-1,2]; 2` | `3` |
| `[1,2]; [2,3]; 4` | `3` |
| `[1,1]; [1,1,1]; 2` | `6` |
| `[]; [1]; 10` | `0` |
| `[MAX]; [MAX]; 4294967294` | `1` |
| `repeat(0,100000); repeat(0,100000); 0` | `10000000000` |

**Targets:** `O(n+m)` time, `O(1)` auxiliary, scalar output. **Complete when:** small cases match nested enumeration, the proof shows which group of pairs is counted at each step, and both addition and count accumulation are wide enough. Follow-up: compare your method with an independent boundary search for every element of a.

<details>
<summary>Hint 1</summary>

For a fixed element in one array, qualifying partners in the other form a prefix.

</details>
<details>
<summary>Hint 2</summary>

As you visit one array in sorted order, examine whether the partner boundary ever needs to reverse direction.

</details>
<details>
<summary>Hint 3</summary>

A moving boundary can count a whole group at once. Widen an operand before adding two int values, and accumulate the number of pairs in long.

</details>

- [ ] **X3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X4. Closest calibration reading

**Difficulty:** Easy. **Objective:** choose a method proportional to the actual workload. **Prerequisites:** array traversal and widening.

Given an **unsorted** `int[] readings` and one `int target`, return the index minimizing the mathematical absolute difference from target. Break ties by **earliest original index**. Duplicates are allowed. Empty input returns `-1`; otherwise a closest reading always exists, even without an exact match. Preserve input. Differences can exceed int.

```java
public static int closestReading(int[] readings, int target) {
    // TODO: Choose a method for one query and preserve earliest-index ties.
    throw new UnsupportedOperationException();
}
```

| readings; target | Output |
| --- | --- |
| `[8,2,6]; 5` | `2` |
| `[4,6]; 5` | `0` |
| `[4,4]; 4` | `0` |
| `[MIN,MAX]; 0` | `1` |
| `[MIN]; MAX` | `0` |
| `[]; 7` | `-1` |

**Targets:** `O(n)` time and `O(1)` auxiliary. **Complete when:** the tie and extreme tests pass, and you explain whether ordering or preprocessing could improve this one-query task. Follow-up: why can applying `Math.abs` after an overflowing int subtraction give the wrong answer?

- [ ] **X4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X5. Workshop finish time

**Difficulty:** Medium. **Objective:** derive a method under start delays, limited output, and possible impossibility. **Prerequisites:** earlier array, arithmetic, and invariant skills.

A machine has nonnegative `start`, positive `period`, and nonnegative `capacity`. It finishes units at `start+period, start+2*period, ...` until it has finished capacity units, then stops. Simultaneous completions all count, including completions exactly at the queried time.

Given an unsorted `Machine[] machines` and `long required`, return the earliest nonnegative integer time by which **at least** required units have finished. Return `-1` if impossible; required zero returns 0. Machines may repeat; zero-capacity machines and empty input are valid. Fields satisfy `start=0..10^9`, `period=1..10^9`, `capacity=0..10^9`, and `required=0..10^14`. Times can exceed int; the largest last-completion time is `10^18+10^9`.

```java
public record Machine(int start, int period, int capacity) {}
public static long workshopFinishTime(Machine[] machines, long required) {
    // TODO: Establish possibility and choose a method that meets the target.
    throw new UnsupportedOperationException();
}
```

Table triples mean `(start,period,capacity)`.

| machines; required | Output |
| --- | --- |
| `[(0,2,2),(1,3,2)]; 3` | `4` |
| `[(0,2,2),(1,3,2)]; 4` | `7` |
| `[(0,2,2),(1,3,2)]; 5` | `-1` |
| `[(5,2,3)]; 1` / required `3` | `7` / `11` |
| `[]; 0` / `[]; 1` | `0` / `-1` |
| `[(0,1,0)]; 1` | `-1` |
| `[(1000000000,1000000000,1000000000)]; 1000000000` | `1000000001000000000` |

**Targets:** `O(n * (1+log R))` time or better for nonempty input, where R is the size of a justified finite time domain; `O(1)` auxiliary. **Complete when:** the invariant sheet, impossibility proof, bound proof, and independent small completion-event enumeration agree. Explain why no per-unit simulation is needed at maximum capacity. Follow-up: what changes if the task asks for the individual completion events themselves?

<details>
<summary>Hint 1</summary>

Separate whether the machines can ever produce enough from when that amount first becomes available.

</details>
<details>
<summary>Hint 2</summary>

A later time cannot undo a completion. Count finished units at a candidate time, accounting for both delayed starts and capacity limits.

</details>
<details>
<summary>Hint 3</summary>

Build a feasible upper bound from last-completion times of productive machines. Widen before multiplying; cap each contribution and handle times before a machine starts.

</details>

- [ ] **X5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 10. Complexity Analysis Exercises

These are **analysis subjects**, not implementations to submit for F/B/P/A/X exercises. The executable snippets return small diagnostic values so you can check your understanding. Keep the [delayed cost checks](#15-references-and-delayed-complexity-checks) closed until you have written an answer.

For **each C exercise**, answer all seven questions:

1. What does n measure?
2. Which other independent input sizes or value-range sizes matter?
3. What is the worst-case time complexity? Identify any useful best case separately.
4. What is the auxiliary space, including call stacks and temporary objects?
5. Why do the loop iterations, calls, or allocations give that bound?
6. Which ordering, access, comparator, hash, or implementation assumptions affect it?
7. Can you improve the code while preserving its exact output and input contract?

Completion means a written derivation, a hand trace matching the supplied output, and an executable check on a boundary input. The **time/space target is to derive a tight or justified bound**, then compare with the delayed checks; it is intentionally not printed beside the questions. All snippets are class members with the shared imports and read-only input rules. Where library sorting is involved, name its time and workspace costs separately.

### C1. Two independent membership scans

**Difficulty:** Easy. **Objective:** distinguish independent array sizes. **Prerequisites:** linear search and additive costs.

Inputs a and b are unsorted int arrays, duplicates allowed; target is an int. Return the number of these two arrays that contain target, so output is always 0, 1, or 2. Empty arrays contribute zero; multiple matches in one array contribute only one.

```java
public static int c1(int[] a, int[] b, int target) {
    int present = 0;
    for (int value : a) {
        if (value == target) {
            present++;
            break;
        }
    }
    for (int value : b) {
        if (value == target) {
            present++;
            break;
        }
    }
    return present;
}
```

Examples: `([1,3], [3,3,5], 3) -> 2`; `([], [2], 1) -> 0`; `([7], [], 7) -> 1`. Check absence from two long arrays and a hit at the first position of each. Explain why using one n for unrelated lengths hides information. Follow-up: would sorting both arrays for this single query improve the total bound?

- [ ] **C1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C2. Iterative and recursive decision paths

**Difficulty:** Medium. **Objective:** separate search depth from auxiliary call-stack space. **Prerequisites:** exact search and [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity).

Both methods take a sorted int array, duplicates allowed, and an int target. They return the sum of `mid+1` over visited midpoint positions, including the hit if there is one. This is a diagnostic checksum, **not a match index**. An absent target still has a checksum; an empty array returns 0. The sum fits long under the shared bounds. The helper receives only the closed ranges generated by the wrapper.

```java
public static long c2Iterative(int[] a, int target) {
    int left = 0, right = a.length - 1;
    long checksum = 0;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        checksum += mid + 1L;
        if (a[mid] == target) break;
        if (a[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return checksum;
}
public static long c2Recursive(int[] a, int target) {
    return c2Visit(a, target, 0, a.length - 1);
}
private static long c2Visit(int[] a, int target, int left, int right) {
    if (left > right) return 0;
    int mid = left + (right - left) / 2;
    long marker = mid + 1L;
    if (a[mid] == target) return marker;
    if (a[mid] < target) return marker + c2Visit(a, target, mid + 1, right);
    return marker + c2Visit(a, target, left, mid - 1);
}
```

Both methods: `([2,5,8], 8) -> 5`; `([2,5,8], 6) -> 5`; `([7], 7) -> 1`; `([], 0) -> 0`. Trace two recursive frames and identify what remains live. Follow-up: what if each call copied its remaining subarray?

<details>
<summary>Hint 1</summary>

Only one branch is called at each recursive step.

</details>
<details>
<summary>Hint 2</summary>

Count the depth of a shrinking range separately from the number of active frames.

</details>
<details>
<summary>Hint 3</summary>

Java does not promise tail-call elimination; these calls also have a pending addition. No subarray is copied in the shown code.

</details>

- [ ] **C2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C3. Sorting once for repeated membership

**Difficulty:** Medium. **Objective:** count preprocessing and preserve input ownership. **Prerequisites:** sorting/search APIs and array copying.

Inputs values and queries are unsorted int arrays; duplicates are allowed. Return the number of query positions whose value appears at least once in values. Repeated successful queries count repeatedly. Empty queries or empty values return 0.

```java
public static int c3(int[] values, int[] queries) {
    int[] ordered = values.clone();
    Arrays.sort(ordered);
    int hits = 0;
    for (int query : queries) {
        if (Arrays.binarySearch(ordered, query) >= 0) hits++;
    }
    return hits;
}
```

Examples: `([4,1,4], [4,2,1,4]) -> 3`; `([], [1]) -> 0`; `([2], []) -> 0`. Check that values is unchanged. The returned count fits int. Follow-up: does an empty queries array stop this implementation from copying and sorting? Use `S_sort(n)` in your space derivation.

<details>
<summary>Hint 1</summary>

Separate the one-time setup from per-query work.

</details>
<details>
<summary>Hint 2</summary>

Let n be the value count and q the query count. Neither is bounded by the other.

</details>
<details>
<summary>Hint 3</summary>

The clone consumes memory even if sorting uses little additional workspace. A library hit is enough for membership, but would not identify a particular duplicate.

</details>

- [ ] **C3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C4. Ordered list queries

**Difficulty:** Medium. **Objective:** connect indexed access to actual search cost. **Prerequisites:** list implementations and library contracts.

Input ordered is a sorted `ArrayList<Integer>` with non-null elements; queries is an unsorted int array. Duplicates are valid. Return the number of successful query positions, as in C3. Either input may be empty.

```java
public static int c4(ArrayList<Integer> ordered, int[] queries) {
    int hits = 0;
    for (int query : queries) {
        if (Collections.binarySearch(ordered, query) >= 0) hits++;
    }
    return hits;
}
```

Examples: `([1,4,4], [4,2,1]) -> 2`; `([], [1]) -> 0`; `([1], []) -> 0`. Assume Integer comparison costs constant time. Follow-up: change the parameter to `List<Integer>` and pass a large LinkedList. Explain both the library's iterator-based traversal cost and why a handwritten repeated `get(mid)` search can have a different cost. See [Lists](./Java_List_ArrayList_LinkedList.md).

<details>
<summary>Hint 1</summary>

Counting comparisons alone is insufficient when reaching a position costs work.

</details>
<details>
<summary>Hint 2</summary>

Distinguish random access from sequential links; the API can choose different mechanisms.

</details>
<details>
<summary>Hint 3</summary>

Account for all q queries and consult the documented Collections.binarySearch behavior for a large non-RandomAccess list.

</details>

- [ ] **C4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C5. Count feasibility probes

**Difficulty:** Medium. **Objective:** include scanning work inside an answer search. **Prerequisites:** monotone predicates and nested costs.

Input needs is a **nonempty**, unsorted array of nonnegative ints no greater than limit. Limit is `0..10^9`; reserve is `0..10^14`. Duplicates and zero needs are valid. The snippet counts how many candidate grants it tests before converging; it returns the **probe count**, not the final grant. A grant equal to limit is guaranteed feasible. Every scan computes a long deficit that fits the given bounds.

```java
public static int c5(int[] needs, long reserve, int limit) {
    long left = 0, right = limit;
    int probes = 0;
    while (left < right) {
        long mid = left + (right - left) / 2;
        long deficit = 0;
        for (int need : needs) {
            deficit += Math.max(0L, (long) need - mid);
        }
        probes++;
        if (deficit <= reserve) right = mid;
        else left = mid + 1;
    }
    return probes;
}
```

Examples: `([2,7,9], 6, 9) -> 4`; `([0], 0, 0) -> 0`; `([5], 0, 5) -> 2`. Test an all-feasible domain and a domain with only its endpoint feasible. Define R explicitly. Follow-up: does early exit in some predicate evaluations establish a constant-time worst-case checker?

<details>
<summary>Hint 1</summary>

There are two sources of work: how many candidates are tested and what each test does.

</details>
<details>
<summary>Hint 2</summary>

The numeric search range is independent of the array length.

</details>
<details>
<summary>Hint 3</summary>

Multiply the full scan cost by the maximum number of probes; keep constant setup for the one-candidate case.

</details>

- [ ] **C5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C6. Queries across independent rows

**Difficulty:** Medium. **Objective:** retain different row sizes in a nested-search bound. **Prerequisites:** summations and binary-search API costs.

Input sortedRows is a non-null **jagged** array: each non-null int row is sorted, but rows need no ordering relationship and may have different lengths. Targets is an unsorted int array. Duplicates are allowed. Return the number of `(row index, target index)` pairs for which that target occurs somewhere in that row. Multiple equal values in one row contribute only once per target position. Empty rows never match; empty outer input or targets returns 0. The count can reach `10^10` and is long.

```java
public static long c6(int[][] sortedRows, int[] targets) {
    long hits = 0;
    for (int[] row : sortedRows) {
        for (int target : targets) {
            if (Arrays.binarySearch(row, target) >= 0) hits++;
        }
    }
    return hits;
}
```

Examples: `([[1,3],[],[3,3,7]], [3,2,3]) -> 4`; `([], [3]) -> 0`; `([[1],[]], []) -> 0`. Let row lengths be `n_1,...,n_m` and target count q. Account for empty rows and for traversing the outer array when q=0. Follow-up: when could a shared preprocessed summary answer a different workload more efficiently?

<details>
<summary>Hint 1</summary>

The two loops do not scan every element of every row.

</details>
<details>
<summary>Hint 2</summary>

Sum the cost of q searches over the individual row lengths.

</details>
<details>
<summary>Hint 3</summary>

Even searching an empty row and advancing the outer loop have constant costs. An expression containing only log of the total number of elements loses that work.

</details>

- [ ] **C6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C7. Search with copied text

**Difficulty:** Medium. **Objective:** identify expensive work hidden inside a shrinking loop. **Prerequisites:** array copies, strings, and geometric sums.

Input sorted is an array of non-null ASCII strings in lexicographic order; key is a non-null ASCII string. Duplicates and empty strings are valid; the array may be empty. Total characters stored in array entries, counting repeated entries, are at most `10^6`; key length is at most 1000. Return the sum of the lengths of the joined prefixes constructed by this code, as a long. This diagnostic is not an insertion position. The end passed to `copyOfRange` is exclusive.

```java
public static long c7(String[] sorted, String key) {
    int left = 0, right = sorted.length;
    long copiedCharacters = 0;
    while (left < right) {
        int mid = left + (right - left) / 2;
        String[] prefix = Arrays.copyOfRange(sorted, left, mid + 1);
        String joined = String.join("", prefix);
        copiedCharacters += joined.length();
        if (sorted[mid].compareTo(key) < 0) left = mid + 1;
        else right = mid;
    }
    return copiedCharacters;
}
```

Examples: `(["a","bb","ccc"], "bb") -> 4`; same array with key `"z" -> 6`; `([], "a") -> 0`; `([""], "") -> 0`. Include long common-prefix strings. Define `L = max(1, longest input-string length, key length)` for an upper bound. Distinguish peak live temporary space from cumulative allocated characters, assuming unreachable temporaries can be reclaimed. Follow-up: can the exact diagnostic output be computed without constructing the joined strings?

<details>
<summary>Hint 1</summary>

The loop is short, but some operations process a sizeable part of its current interval.

</details>
<details>
<summary>Hint 2</summary>

Add the copied interval lengths over all iterations; they shrink geometrically. Comparisons also inspect characters.

</details>
<details>
<summary>Hint 3</summary>

Array copying copies references; joining creates character content. For improvement, ask whether only lengths are needed to preserve this return value.

</details>

- [ ] **C7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 11. Debugging Challenges

Each snippet below is **intentionally incorrect** but valid Java 21. The contract describes the required behavior, not what the snippet currently does. Do not copy these into a reference implementation.

For each D exercise:

1. Identify every independent defect.
2. Construct a minimal failing input in addition to the supplied diagnostic cases.
3. Record observed versus required behavior, including hangs or exceptions.
4. Explain the invariant, precondition, or numeric fact that was violated.
5. Correct the code in your practice workspace and verify termination.
6. Analyze the corrected time and auxiliary space; run both failure cases and passing controls.

The signatures shown are the suggested repair signatures. Shared bounds, read-only rules, and non-null assumptions apply. Each task has a completion target and delayed hints, but no repair code. Run a suspected infinite loop in a separate process with a timeout, not in the main test runner.

### D1. A search that stops or stalls

**Difficulty:** Medium. **Objective:** distinguish a faulty loop guard from a faulty update. **Prerequisites:** F1 and progress measures.

Required contract: sorted int array, duplicates allowed; return any matching target index or `-1`. Empty input is valid; matches are not guaranteed.

```java
public static int d1(int[] a, int target) {
    int left = 0, right = a.length - 1;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (a[mid] == target) return mid;
        if (a[mid] < target) left = mid;
        else right = mid - 1;
    }
    return -1;
}
```

| Input | Required behavior |
| --- | --- |
| `([1,2], 2)` | Return `1` and terminate |
| `([7], 7)` | Return `0` |
| `([], 7)` | Return `-1` |
| `([1,2], 1)` | Return `0` |

**Repair targets:** `O(log(n+1))` time, `O(1)` auxiliary. **Complete when:** you explain why fixing only one line may leave another defect, prove progress on two candidates, and pass the controls plus new absent/duplicate tests.

<details>
<summary>Hint 1</summary>

Inspect the states with one candidate and with two candidates.

</details>
<details>
<summary>Hint 2</summary>

A closed interval still contains a candidate when its endpoints are equal. A rejected midpoint must not remain the same unresolved lower endpoint forever.

</details>
<details>
<summary>Hint 3</summary>

Repair the guard and the update together using one interval convention. Run the two-element case with a process timeout before repairing it.

</details>

- [ ] **D1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D2. The wrong boundary and sentinel

**Difficulty:** Medium. **Objective:** separate strictness from absence conventions. **Prerequisites:** B1.

Required contract: for a sorted int array with duplicates allowed, return the first index with value **>= target**, or **n** if none. Empty input returns 0. This is an insertion boundary, not an exact-match index.

```java
public static int d2(int[] a, int target) {
    int left = 0, right = a.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (a[mid] <= target) left = mid + 1;
        else right = mid;
    }
    return left == a.length ? -1 : left;
}
```

| Input | Required output |
| --- | --- |
| `([2,2,3], 2)` | `0` |
| `([1,3], 5)` | `2` |
| `([], 0)` | `0` |
| `([1,3], 0)` | `0` |

**Repair targets:** `O(log(n+1))` time, `O(1)` auxiliary. **Complete when:** both independent return/comparison defects are explained and all-equal, missing-between, below-all, and above-all cases pass. Explain why n may be the correct answer despite being an invalid element index.

<details>
<summary>Hint 1</summary>

Read the contract's equality sign and absence rule separately.

</details>
<details>
<summary>Hint 2</summary>

What fact does the current comparison establish about the discarded prefix?

</details>
<details>
<summary>Hint 3</summary>

A boundary API and an exact-search API need not share a missing-answer sentinel.

</details>

- [ ] **D2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D3. A supposed last occurrence

**Difficulty:** Medium. **Objective:** repair both overflow and false membership. **Prerequisites:** B2-B3.

Required contract: sorted int array, duplicates allowed; return the **last inclusive index equal to target**, or `-1` if absent. Empty input is valid; target can be any int.

```java
public static int d3(int[] a, int target) {
    int wanted = target + 1;
    int left = 0, right = a.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (a[mid] < wanted) left = mid + 1;
        else right = mid;
    }
    return left - 1;
}
```

| Input | Required output |
| --- | --- |
| `([1,3], 2)` | `-1` |
| `([MAX], MAX)` | `0` |
| `([2,2], 2)` | `1` |
| `([], 2)` | `-1` |

**Repair targets:** `O(log(n+1))` time, `O(1)` auxiliary. **Complete when:** the repair handles both maximum-valued targets and absent targets, with a justified equality guard and no invalid dereference. Follow-up: which valid predecessor question does a nearby index answer even when target is absent?

<details>
<summary>Hint 1</summary>

A neighboring boundary does not itself prove that target occurs.

</details>
<details>
<summary>Hint 2</summary>

Consider the mathematical meaning of target + 1 and whether int can represent it.

</details>
<details>
<summary>Hint 3</summary>

Express strictness with a comparison, then validate the candidate element before reporting an occurrence.

</details>

- [ ] **D3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D4. Discarded rotated candidates

**Difficulty:** Medium. **Objective:** audit the meaning of a sorted-half test. **Prerequisites:** P1.

Required contract: a rotated strictly increasing int array, with **no duplicates**; return target's unique index or `-1`. Empty and unrotated arrays are valid.

```java
public static int d4(int[] a, int target) {
    int left = 0, right = a.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (a[mid] == target) return mid;
        if (a[left] <= a[mid]) {
            if (a[left] <= target && target < a[mid]) left = mid + 1;
            else right = mid - 1;
        } else {
            if (a[mid] < target && target <= a[right]) left = mid + 1;
            else right = mid - 1;
        }
    }
    return -1;
}
```

| Input | Required output |
| --- | --- |
| `([4,5,6,1,2,3], 4)` | `0` |
| `([4,5,6,1,2,3], 6)` | `2` |
| `([2,1], 1)` | `1` |
| `([], 4)` | `-1` |

**Repair targets:** `O(log(n+1))` time, `O(1)` auxiliary. **Complete when:** each branch's retained region is explained and every rotation of small distinct arrays passes for present and absent targets. A midpoint-hit control alone is insufficient.

<details>
<summary>Hint 1</summary>

The sorted-half classification can be correct while the chosen update is wrong.

</details>
<details>
<summary>Hint 2</summary>

When the target lies in a certified sorted half, which half must remain?

</details>
<details>
<summary>Hint 3</summary>

Trace the first failing row and name the discarded indices before reading the next iteration.

</details>

- [ ] **D4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D5. Virtual positions with unsafe boundaries

**Difficulty:** Medium. **Objective:** repair empty dimensions, interval conventions, and domain width. **Prerequisites:** P2.

Required contract: rectangular globally row-major sorted int matrix, duplicates and shared rows allowed; return whether target occurs. Empty outer input and zero-column matrices return false. Target is any int. Use the shared separate row/column size bounds; the virtual product can exceed int.

```java
public static boolean d5(int[][] matrix, int target) {
    int rows = matrix.length;
    int cols = rows == 0 ? 0 : matrix[0].length;
    int left = 0, right = rows * cols;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        int value = matrix[mid / cols][mid % cols];
        if (value == target) return true;
        if (value < target) left = mid + 1;
        else right = mid - 1;
    }
    return false;
}
```

| Input | Required output |
| --- | --- |
| `([], 1)` / `([[],[]], 1)` | `false` / `false` |
| `([[1]], 2)` | `false` |
| `([[1]], 1)` | `true` |
| 50,000 references to one 50,000-zero row; target 0 | `true` |

**Repair targets:** `O(log(N+1))` time for N virtual elements, `O(1)` auxiliary. **Complete when:** every table case runs without an exception, the wide-domain case finds zero, and the invariant excludes sentinel dereferences. Follow-up: why does converting the final int product to long occur too late?

<details>
<summary>Hint 1</summary>

Dimensions, boundary positions, and element positions have different roles.

</details>
<details>
<summary>Hint 2</summary>

Audit the product's type, the status of N as an endpoint, and division when the column count is zero independently.

</details>
<details>
<summary>Hint 3</summary>

Choose a consistent interval and widen an operand before multiplying. Only convert a valid virtual element position to coordinates.

</details>

- [ ] **D5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D6. A production estimate with two numeric defects

**Difficulty:** Medium. **Objective:** distinguish invalid bounds from overflowing feasibility counts. **Prerequisites:** A2.

Required contract is A2 restricted to **nonempty** periods with each p in `1..10^9` and required in `1..10^9`. Periods may be unsorted and repeated. Return the earliest nonnegative completion time for at least required units. An answer always exists and may require long.

```java
public static long d6(int[] periods, long required) {
    long left = 0, right = required;
    while (left < right) {
        long mid = left + (right - left) / 2;
        int made = 0;
        for (int period : periods) {
            made += mid / period;
        }
        if (made >= required) right = mid;
        else left = mid + 1;
    }
    return left;
}
```

| Input | Required output |
| --- | --- |
| `([5], 3)` | `15` |
| `(repeat(1,10), 1000000000)` | `100000000` |
| `([1], 3)` | `3` |

**Repair targets:** `O(n * (1+log R))` time with justified candidate-count R; `O(1)` auxiliary. **Complete when:** you reproduce failures caused by each defect, prove the repaired high endpoint feasible, and ensure the checker remains safe at large candidate times. Explain the implicit narrowing performed by compound assignment. Follow-up: is changing made to long enough for every valid A2 helper input?

<details>
<summary>Hint 1</summary>

The requested number of units is not generally a feasible completion time.

</details>
<details>
<summary>Hint 2</summary>

Inspect the type used to store the sum, even though each division uses a long operand.

</details>
<details>
<summary>Hint 3</summary>

Build the bound from a feasible production scenario. Accumulate only as much information as the yes/no threshold test requires.

</details>

- [ ] **D6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D7. Searching arbitrary flags

**Difficulty:** Medium. **Objective:** recognize when boundary repair cannot justify binary search. **Prerequisites:** B4 and linear traversal.

Required contract: given a boolean array in **arbitrary order**, return its first true index or `-1` if no true value exists. Empty input and any repetition of values are allowed. There is no monotonicity guarantee.

```java
public static int d7(boolean[] flags) {
    int left = 0, right = flags.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (flags[mid]) right = mid;
        else left = mid + 1;
    }
    return left == flags.length ? -1 : left;
}
```

| Input | Required output |
| --- | --- |
| `[false,true,false,false]` | `1` |
| `[true,false,true]` | `0` |
| `[false,false,true]` | `2` |
| `[]` / `[false,false]` | `-1` / `-1` |

**Repair targets:** `O(n)` worst-case time, `O(1)` auxiliary, preserving the arbitrary-order contract. **Complete when:** a counterexample identifies an unjustified discarded region, you explain why changing a comparison alone cannot fix the assumption, and a correct alternative passes exhaustive short boolean arrays. Follow-up: what additional precondition would make the shown boundary-search idea valid?

<details>
<summary>Hint 1</summary>

Ask what a false value at mid tells you about earlier positions under this contract.

</details>
<details>
<summary>Hint 2</summary>

Construct two arrays that agree at the inspected positions but have different first true indices.

</details>
<details>
<summary>Hint 3</summary>

Preserve the original input contract. If no ordering supports elimination, a scan may be the appropriate repair.

</details>

- [ ] **D7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 12. Mock Interview Practice

Use these prompts with existing exercise IDs, not as additional exercises or duplicate logs. A typical Medium mock may take about 35-45 minutes including clarification, code, tests, and explanation; adjust to the target interview format. A faster attempt does not compensate for an unsupported proof.

| Session | Selection | Communication evidence |
| --- | --- | --- |
| Mechanics | Cold F2 plus a B1 or B2 variant with changed data | State the interval, explain equality, trace two candidates, and justify the sentinel |
| Core reasoning | Cold P1 or A2 | Start with a baseline; derive the sorted-half or feasibility argument; prove every update |
| Transfer | An unattempted X problem with title/hints hidden | Compare alternatives before choosing; implement; test; include preprocessing/predicate costs |
| Repair | One unattempted D problem | Show a concrete failure, explain the cause, repair the invariant, and test independent defects |
| Retention | A different unfamiliar boundary/answer task in a later session | Demonstrate transfer without relying on the previous problem's code |

Before coding, give a short explanation of why your technique applies, what the answer domain is, and whether an answer is guaranteed. During coding, narrate why a boundary moves rather than reading statements aloud. After coding, test a risk-focused case and explain total time and auxiliary space.

Ask a peer or use these follow-ups after a concrete attempt:

- What exactly is still possible at the top of this loop?
- Why does this midpoint remain or leave the candidate set?
- Which measure decreases even when only two candidates remain?
- What does the loop exit prove about a missing answer?
- What if all values are equal, or the answer is at the last legal boundary?
- What precondition permits discarding this entire half?
- How do comparator, linked access, copying, or feasibility costs affect the bound?
- What numeric intermediate can overflow before its result is assigned?
- Which alternative costs less for one query, and which improves repeated queries?
- What counterexample defeats the proposed equality or arbitrary-flag predicate?

If you recognize a memorized solution, use that attempt as retention evidence and choose a genuinely unfamiliar variant for readiness. Record the mock result on the exercised problem's existing record; put only a compact assessment reference in the roadmap's readiness evidence when a gate is assessed.

## 13. Revision and Spaced Repetition

Follow the [roadmap review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): try reviews about **1, 3, 7, and 14 days after the learning attempt**, then widen gaps when retention holds. These are flexible suggestions, not deadlines; missed dates do not reset progress.

A cold re-solve starts with only the statement, a blank editor, and closed hints, old code, notes, and solutions. Reconstruct the baseline, algorithm choice, invariant, implementation, complexity, and edge tests. Compare with the previous attempt only after finishing. Update the existing attempt record with the new result, time, lesson, and next review date. Keep a short result history in that same record if useful.

Choose reviews in this order:

1. S attempts, including unfinished work.
2. H attempts.
3. Incorrect boundary or monotonicity reasoning.
4. Unresolved absence, duplicate, termination, or overflow mistakes.
5. Independent work whose review date is due.

For N1-N6, update only the existing roadmap problem Log. Do not create a parallel revision table. A recurrence of the same error should trigger a targeted F/B/A or D drill, followed by a different variant. Reproducing familiar code is useful retention evidence but cannot alone earn R.

## 14. Completion and Readiness

These milestones describe evidence; they are not a second topic dashboard. Update T/I/P/R only in the [roadmap](./DSA_Interview_Roadmap.md#four-independent-progress-gates), using its [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).

| Milestone | Evidence required |
| --- | --- |
| Level 1 fundamentals | F1-F3 from a blank editor; both interval conventions explained; empty/singleton/two-element traces; midpoint arithmetic justified |
| Level 2 core implementations | Cold exact search from F1/F2; B1-B4; P1; a separate predicate and minimum-feasible search from A2; maximum-feasible reasoning from A3; tests and invariant/cost explanations |
| Ordered-structure breadth | P2 and B3 demonstrate matrix order and timestamp predecessor reasoning even when their platform problems are Additional |
| Essential platform practice | N1, N3, and N4 independently solved or cold re-solved, tested, and explained; this supports P |
| Additional practice | N2 and N5 used for breadth or gaps after Essentials; they are A, not promoted to E |
| Optional advanced investigation | N6 is C and optional; an unfinished Hard does not block ordinary readiness |
| Interview readiness | Repeated independent unfamiliar boundary/answer variants across separate sessions, plus technique selection and clear communication under the common rubric |

**T** requires explaining the essential theory without notes. **I** requires the roadmap's core implementations from scratch with tests and proofs; the Level 2 evidence above supports this, but checkbox totals are not a substitute. **P** requires independent Essential practice. **R** additionally requires unfamiliar transfer and repeatability.

Before claiming Binary Search readiness, demonstrate all of the following in assessed work: recognize an applicable order or monotone condition; reject unsuitable binary-search proposals; select valid bounds and absence conventions; derive updates; handle duplicates and missing answers; prove progress; prevent numeric overflow; analyze preprocessing and predicate work; implement Java 21 correctly; and explain correctness and alternatives aloud. Use at least one unfamiliar boundary variant and one unfamiliar answer-search variant across separate sessions, with a mixed-selection task to establish that topic labels are unnecessary.

Use C1-C7 and D1-D7 to diagnose evidence gaps rather than imposing an arbitrary completion quota. A failed cold attempt or a misunderstood invariant calls for review. Keep the assessment date and brief unfamiliar-problem evidence at the [roadmap's Binary Search readiness paragraph](./DSA_Interview_Roadmap.md#4-binary-search). Do not infer Interview Ready from a completed workbook or an accepted submission alone.

## 15. References and Delayed Complexity Checks

### Repository references

| Reference | Use after an attempt |
| --- | --- |
| [Roadmap](./DSA_Interview_Roadmap.md#4-binary-search) | Coverage, priorities, and canonical platform attempt logs |
| [Binary Search concepts](./Binary_Search_Essential_Concepts.md) | Primary theory and worked conceptual explanations |
| [Search-space correctness](./Binary_Search_Essential_Concepts.md#10-search-space-invariants-and-correctness) | Initialization, preservation, progress, termination |
| [Detailed search complexity](./Binary_Search_Essential_Concepts.md#11-detailed-complexity-analysis) | Predicate, access, copy, and numeric-domain costs |
| [Alternatives to binary search](./Binary_Search_Essential_Concepts.md#12-binary-search-versus-alternative-techniques) | Technique selection and preprocessing tradeoffs |
| [Java Arrays](./Java_Array.md) | Arrays, copies, matrices, sorting, and search APIs |
| [Java Strings](./Java_String.md) | Comparison and materialized-string costs |
| [Lists](./Java_List_ArrayList_LinkedList.md) | ArrayList access versus LinkedList traversal |
| [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) | Key lookup and first-index summaries |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Independent variables, auxiliary space, recursion, library work |
| [Arrays & Hashing exercises](./Arrays_and_Hashing/Arrays_and_Hashing_Exercises.md) | Cold practice selecting hash state |
| [Two Pointers concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md) | Ordered pair reasoning and movement proofs |
| [Two Pointers exercises](./Two_Pointers/Two_Pointers_Exercises.md) | Alternative linear methods |
| [Stack concepts](./Stack/Stack_Essential_Concepts.md) | Explicit versus recursive state |
| [Testing concepts](./Testing_Essential_Concepts.md) | Contract-based edge tests and failure diagnosis |

Java 21 API contracts were checked through Context7 against Oracle documentation. [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) documents sorted-input requirements and the negative insertion-point encoding for binary search; duplicate hits do not identify a particular occurrence. [Collections.binarySearch](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html#binarySearch(java.util.List,T)) distinguishes random-access lists from large sequential-access lists. [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html) documents constant-time indexed access. Consult [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) for comparison and joining semantics. Complexity derivations below count the operations and materialized data in the snippets, rather than assuming every library call costs O(1).

### Delayed complexity checks

Open only after recording your own C1-C7 derivations. These are cost checks, not completed implementations for the coding exercises.

<details>
<summary>C1 cost check</summary>

Let n and m be the independent array lengths. Worst-case time is O(n+m+1), with constant auxiliary space and scalar output. An early first-position hit in both arrays takes constant time, but does not improve the worst case. Duplicates do not increase the contribution beyond one per array. Sorting for a single membership query adds unnecessary preprocessing; repeated queries could justify a different design.

</details>

<details>
<summary>C2 cost check</summary>

Both versions perform O(1+log(n+1)) work in the worst case because only one shrinking half is visited. An immediate hit takes constant time. The iterative version uses O(1) auxiliary space; the recursive version uses O(1+log(n+1)) live frames. Both return a scalar. Copying subarrays would change both work and space; it is absent here. The checksum addition does not create a second recursive branch.

</details>

<details>
<summary>C3 cost check</summary>

Let n be values.length and q be queries.length. The clone costs O(n), sorting costs O(n log(n+1)) under the chosen primitive-sort model, and q searches cost O(q(1+log(n+1))). Include constant setup. Auxiliary space is O(n + S_sort(n) + 1), with scalar output. The snippet still clones and sorts for q=0. Avoid assuming a Java sorting implementation has constant workspace without establishing that assumption. A set could trade sorted order for expected O(n+q) membership work with O(u) stored keys; input preservation still matters.

</details>

<details>
<summary>C4 cost check</summary>

For n list elements and q queries, the ArrayList version takes O(1+q(1+log(n+1))) time and O(1) peak auxiliary search state, with constant-time Integer comparisons. This relies on efficient positional access. For a large non-RandomAccess list, the documented Collections implementation performs O(n) link traversals and O(log n) comparisons per search, giving O(1+q(n+1)) with constant-time comparisons. A naive handwritten LinkedList search repeatedly calling get(mid) can instead take O(n log n) per query. These are different access strategies, not contradictory comparison counts.

</details>

<details>
<summary>C5 cost check</summary>

Let n=needs.length and R=limit+1 mathematically. For limit>0, O(log R) probes each scan n elements; a valid upper bound is O(1+n log R). When limit=0, the loop does no scans and returns in constant time. Auxiliary space and output are O(1). No preprocessing to find limit occurs in this snippet: it is supplied with a guaranteed precondition. Early return from a revised checker may save some work, but a worst-case scan can still examine every need.

</details>

<details>
<summary>C6 cost check</summary>

Let m be the number of rows, q the target count, and n_i the length of row i. Time is O(1 + m + q * sum over i of (1+log(n_i+1))). Peak auxiliary space is O(1), with scalar output. The outer m work persists when q=0. Empty rows still incur a call for each target. A rectangular simplification with common row length n is O(1+m+mq(1+log(n+1))); using that form without a rectangular assumption hides the input structure.

</details>

<details>
<summary>C7 cost check</summary>

For n>0, the copied reference counts sum to O(n), since interval sizes shrink geometrically. Joining scans those references and materializes up to O(nL) characters in total; comparisons cost up to O(L log(n+1)). Thus O(nL) is a worst-case upper bound for nonempty input, and O(1+nL) covers empty input. Peak live temporary space is O(nL) in the worst case, including references and joined text; scalar output is O(1). The total-character constraint may give a tighter bound for a particular input, but does not justify treating joining as constant time.

The arrays copy references, not the original String objects' contents. Newly joined text does require storage. Peak live memory and cumulative allocation are different quantities even though both have O(nL) upper bounds here. The exact checksum can be preserved by summing lengths over the same selected ranges without creating copies or joined strings: this gives O(1+n+L log(n+1)) time and O(1) auxiliary space. This improvement preserves the diagnostic output; simply returning an insertion position would change the task.

</details>

### Validation scope

Authoring checks verified 155 table cases with independent scan, enumeration, counting, and exact-integer oracles. All 31 Java blocks compiled for Java 21; 80 runtime checks covered the analysis examples, every deliberate defect, and numeric edge cases. D1's nontermination was reproduced in an isolated process with a timeout. D6 intentionally produces a compiler warning for its lossy compound assignment; the other snippets passed a separate warnings-as-errors compilation. All 56 local links and anchors resolved, and all 20 pre-existing files in DSA_Guide retained their original content hashes. Temporary validation programs remained outside the repository.

These checks do not mark learner progress or prove a future learner implementation correct. Keep exercising your own implementation against independent small-case oracles and the stated contracts.




