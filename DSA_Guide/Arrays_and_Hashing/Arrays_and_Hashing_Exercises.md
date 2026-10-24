# Arrays & Hashing — Exercises (Java 21)

Build the habit of deriving useful state, explaining why it is correct, and testing what could break it. This workbook contains **28 original exercises** (16 implementation tasks, six complexity investigations, and six debugging tasks), plus **seven roadmap problems**. All attempt records start empty.

## Contents

- [1. Introduction and Coverage](#1-introduction-and-coverage)
- [2. Practice Rules and Java Setup](#2-practice-rules-and-java-setup)
- [3. Level 1: Java and Data Structure Warm-ups](#3-level-1-java-and-data-structure-warm-ups)
- [4. Level 2: Core Algorithmic Patterns](#4-level-2-core-algorithmic-patterns)
- [5. Level 3: NeetCode and LeetCode Practice](#5-level-3-neetcode-and-leetcode-practice)
- [6. Level 4: Mixed Interview Challenges](#6-level-4-mixed-interview-challenges)
- [7. Complexity Analysis Exercises](#7-complexity-analysis-exercises)
- [8. Debugging Challenges](#8-debugging-challenges)
- [9. Revision and Spaced Repetition](#9-revision-and-spaced-repetition)
- [10. Completion and Readiness Checklist](#10-completion-and-readiness-checklist)
- [11. Learning References and Validation](#11-learning-references-and-validation)

## 1. Introduction and Coverage

The [Arrays & Hashing roadmap section](./DSA_Interview_Roadmap.md#1-arrays--hashing) defines the scope and Essential/Additional priorities. [Essential Concepts](./Arrays_and_Hashing_Essential_Concepts.md) is the primary teaching reference. This workbook supplies independent practice and evidence for the roadmap's [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates).

Prerequisites are Core Java, array/string traversal, collections, equality, and basic complexity. Use [Arrays](./Java_Array.md), [Strings](./Java_String.md), [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md), and [Complexity](./Complexity/Complexity_Analysis_and_Foundations.md) to repair a specific gap. Close worked implementations before attempting a drill.

Original difficulty labels describe progression within this workbook; they are not platform ratings. W = warm-up, F = fundamental drill, L = linked platform problem, X = unfamiliar challenge, C = complexity, D = debugging. Some early drills intentionally isolate a mechanism demonstrated in the theory guide. Derive them from a blank editor instead of reproducing that guide's code.

| Roadmap objective | Practice evidence | Teaching reference |
| --- | --- | --- |
| Traverse/transform with a processed-prefix invariant | W1, W4; C1 | [Traversal](./Arrays_and_Hashing_Essential_Concepts.md#3-traversal-transformation-and-processed-prefixes) |
| Choose membership, counts, indices, or a bounded alphabet | W2–W5; L1–L2 | [Seen values](./Arrays_and_Hashing_Essential_Concepts.md#4-seen-value-tracking), [counts](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting) |
| Integer and string frequency implementations | W2, W5 | [Frequency counting](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting) |
| Complement lookup and distinct-index timing | F1, L3; D1 | [Complement lookup](./Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup) |
| Unambiguous signatures, equality, and grouping | F2, L4; D3–D4 | [Canonical signatures](./Arrays_and_Hashing_Essential_Concepts.md#7-canonical-signatures-and-grouping) |
| Prefix sums and consistent range boundaries | F3; C5, D2 | [Prefix sums](./Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries) |
| Two-pass products, zeros, and output-buffer reuse | F4, L5; D5 for arithmetic | [Prefix/suffix aggregation](./Arrays_and_Hashing_Essential_Concepts.md#9-prefixsuffix-products-and-aggregation) |
| Consecutive values and unique sequence heads | F5, L6; D6 | [Consecutive runs](./Arrays_and_Hashing_Essential_Concepts.md#10-consecutive-runs-and-sequence-head-expansion) |
| Compare sorting, hashing, and bounded buckets | F6–F7, L7; C2–C3, C6 | [Approach comparison](./Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets) |
| Include key construction, copying, and library costs | C1–C6 | [Cost model](./Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model), [Java pitfalls](./Arrays_and_Hashing_Essential_Concepts.md#12-java-specific-pitfalls) |
| Select and defend a technique on unseen requirements | X1–X4, across separate sessions | Open each challenge's references only after attempting it |

Use Level 1 as a diagnostic, then Level 2 to repair implementation gaps. Attempt Essential problems in the roadmap's order; interleave the related C/D tasks after writing your own implementation. Additional problems broaden practice after Essential competence, or earlier when a specific gap requires them. Save Level 4 for closed-note assessment. No heap, sliding-window, graph, or dynamic-programming mastery is required here.

## 2. Practice Rules and Java Setup

For every F, L, or X task, leave brief evidence of these ten steps in your own scratch notes:

1. Restate the required output and what counts as equal or distinct.
2. Identify assumptions: bounds, mutation, ordering, duplicates, and arithmetic.
3. Describe a correct brute-force method before optimizing.
4. Derive its time and auxiliary space.
5. Name the repeated work or memory bottleneck.
6. Derive a better representation or schedule; compare one alternative.
7. State the invariant, initialization, preservation, and termination argument, or an equivalent correctness proof.
8. Implement in Java 21 from a blank editor.
9. Run the listed tests and at least two tests you designed; check mutation and output properties.
10. Derive the final costs, including output and Java library work, and explain the solution aloud.

For W tasks, a short baseline, invariant, and cost explanation is enough. Start C/D tasks without the related theory open. Suggested practice windows are 10–20 minutes for W, 20–40 for F, and 30–45 for X; adjust while learning. These are pacing aids, not completion deadlines.

**Hint policy.** First record a concrete attempt and the point of confusion. Open Hint 1, retry, then escalate only if needed. Opening a helpful hint or consulting a worked implementation makes that attempt H or S; speed does not restore independence. Level 4 titles and visible prerequisites intentionally omit the intended pattern. A theory link can itself be a hint.

**Results:** I = independently solved, tested, and explained; H = needed a hint; S = needed substantial solution review or could not finish. A checked attempt means it was attempted, tested to the extent possible, and reviewed; it does not by itself mean mastery. For an unfinished attempt, record S and the failing test or missing reasoning step. An I result also needs a correctness and complexity explanation. Memorized familiar code alone does not establish transfer.

Every exercise has one local record: latest date, I/H/S, minutes, next review, and lesson. Append short history on that same line when useful. For the seven platform problems, use their workbook records for detailed attempts; when you later maintain the roadmap, its corresponding Log can point to that record rather than duplicate dates. The roadmap remains the authoritative topic-level dashboard. This workbook does not change any roadmap status.

### Shared contracts and complexity conventions

- All original tasks are single-threaded. Inputs, rows, strings, and lists are non-null. Allocations must be feasible. Unless a task says otherwise, arrays have at most 100,000 entries, integer values may span the full `int` range, and inputs must remain unchanged.
- Only W1 deliberately mutates input. Grouping tasks return original **indices**, retaining repeated occurrences. Within each group indices must increase; outer group order is unrestricted. Validate partitions by membership, with no missing or duplicated index, rather than comparing one arbitrary outer ordering.
- Maps are compared by entries, not iteration order. Primitive arrays are compared by contents; for nested arrays use deep content comparison. `MIN` and `MAX` in test tables mean `Integer.MIN_VALUE` and `Integer.MAX_VALUE`.
- A range `[from,to)` includes `from` and excludes `to`; `from == to` is empty. Unless explicitly stated, only valid inputs are required; null handling and rejection of out-of-contract values are not extra exercises.
- Complexity targets describe successful executions, using fixed-width arithmetic. Hashing bounds are **expected**, with amortized growth and suitable hash distribution; they are not unconditional worst-case guarantees. Define n, q, u, N, L, or R as appropriate. For empty inputs, read O(n) as O(n+1) when a constant operation is necessary.
- **Auxiliary space excludes required output**, which must be reported separately. A returned frequency map still occupies memory. Growing returned maps/lists can temporarily allocate replacement backing storage; account for that separately if claiming a constant auxiliary bound. Retained preprocessing tables are working space when the public output is query answers. Distinguish peak live storage from cumulative allocation.
- For sorting, include copies and library workspace. You may name the latter `S_sort(n)` if you have not justified a tighter bound for the selected implementation. For strings, include construction, first hashing, and content comparisons. The [theory cost model](./Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model) supplies the detailed conventions.

### Starter template

Use a scratch directory of your choice; no external testing framework is needed. Each exercise signature below is shown as a Java declaration comment. Replace the sample stub with the selected signature and your own body. The only completed routines later in the workbook are code to **analyze** or deliberately buggy code to **repair**, not solutions to the implementation tasks.

```java
import java.util.*;

class WorkbookPractice {
    public static int clampNegatives(int[] values) {
        // TODO: derive the invariant, then implement W1.
        throw new UnsupportedOperationException("Implement independently");
    }

    // F5 uses inclusive endpoints. This is a data type, not an algorithm.
    record Span(int start, int end) {}

    public static void main(String[] args) {
        // TODO: add explicit checks for the selected exercise.
        // Throw AssertionError on a mismatch, or use Java assertions with -ea.
    }
}
```

Compile with `javac --release 21 -Xlint:all WorkbookPractice.java` and run with `java -ea WorkbookPractice`. Keep test inputs independent when mutation is allowed. For grouping, normalize a copy of the output only in your test harness; sorting the test representation does not change the solution's ordering contract or costs.

## 3. Level 1: Java and Data Structure Warm-ups

### W1. Clamp invalid readings

**Difficulty:** warm-up. **Objective:** distinguish visiting a value from replacing an array slot. **Prerequisites:** [traversal and invariants](./Arrays_and_Hashing_Essential_Concepts.md#3-traversal-transformation-and-processed-prefixes).

Replace every negative element of `values` with zero **in the same array**. Return how many positions changed. Keep length and order; an original zero is not a change. Input may be empty, with any int values. Output is a scalar count plus the mutated array; do not allocate a replacement array.

```java
// public static int clampNegatives(int[] values)
```

| Input values | Expected return | Expected array afterward |
| --- | --- | --- |
| `[4,-2,0,-7,5]` | `2` | `[4,0,0,0,5]` |
| `[]` | `0` | `[]` |
| `[-1]` | `1` | `[0]` |
| `[0,2,2]` | `0` | `[0,2,2]` |
| `[MIN,MAX]` | `1` | `[0,MAX]` |

In the first example, only indices 1 and 3 change; zero at index 2 was already valid. **Target:** O(n) time, O(1) auxiliary space, scalar output. **Completion:** pass the table, preserve the original array object, explain the processed-prefix meaning, and explain why assigning an enhanced-for loop variable would not satisfy the contract.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### W2. Exact inventories

**Difficulty:** warm-up. **Objective:** preserve multiplicities using maps and correct key equality. **Prerequisites:** [frequency counting](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting), [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md).

Implement two small versions of one inventory operation: return each distinct input value and its occurrence count. One takes integers; one takes strings. String equality is exact, case-sensitive content equality, including empty strings; do not trim or normalize. For the string version, N ≤ 100,000 and total UTF-16 length L ≤ 1,000,000. Output maps contain only observed keys with positive counts. Equal-content strings created as separate objects must share one entry. Input order and objects stay unchanged.

```java
// public static Map<Integer, Integer> integerInventory(int[] values)
// public static Map<String, Integer> wordInventory(String[] words)
```

| Version and input | Expected map entries |
| --- | --- |
| integers `[4,4,-1,4,-1]` | `{4:3,-1:2}` |
| integers `[]` | `{}` |
| integers `[MIN,0,MAX,MIN]` | `{MIN:2,0:1,MAX:1}` |
| words `["tea","Tea","tea",""]` | `{"tea":2,"Tea":1,"":1}` |
| words `["",""]` | `{"":2}` |
| words `[]` | `{}` |

For the first row, three occurrences of 4 and two of -1 account for all five positions. Also construct two separate `new String("tea")` objects and expect one entry with count 2. **Target:** expected O(n) for integers; expected O(L+N) for words including hashing/equality. Output uses O(u) map entries referencing input keys. Scalar explicit state suffices beyond the output, but allow O(u) peak temporary collection growth. **Completion:** pass tests; sum counts to recover input length using `entrySet()`; explain why a set cannot replace counts and why `==` cannot define word equality.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### W3. Shared identifiers

**Difficulty:** warm-up. **Objective:** separate membership from occurrence count. **Prerequisites:** [seen-value tracking](./Arrays_and_Hashing_Essential_Concepts.md#4-seen-value-tracking).

Given arrays `left` and `right`, return the number of **distinct integer values** occurring in both. Repeated copies on either side count once. Either array may be empty; each length is at most 100,000. Return a scalar, preserving both arrays.

```java
// public static int sharedIdentifierCount(int[] left, int[] right)
```

| left | right | Expected count |
| --- | --- | --- |
| `[3,3,1,-2]` | `[3,3,-2,8]` | `2` |
| `[]` | `[1]` | `0` |
| `[5,5]` | `[5,5,5]` | `1` |
| `[1,2]` | `[3,4]` | `0` |
| `[MIN,0,MAX]` | `[MAX,MIN]` | `2` |

The first shared values are 3 and -2, regardless of how many times they appear. **Target:** expected O(n+m) time and O(n+m) auxiliary space or better. **Completion:** pass tests, identify where repeated values could be overcounted, state what your set represents, and compare a sorting alternative including copies.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### W4. First-position directory

**Difficulty:** warm-up plus. **Objective:** choose an index payload instead of membership alone. **Prerequisites:** [processed-prefix state](./Arrays_and_Hashing_Essential_Concepts.md#3-traversal-transformation-and-processed-prefixes), [map choices](./Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets).

Given `values` and `queries`, return an int array with one answer per query: the smallest index containing that value, or -1 if absent. Each input has length at most 100,000. Duplicate queries repeat the same answer. Preserve input order and contents; index zero is a valid success.

```java
// public static int[] firstPositions(int[] values, int[] queries)
```

| values | queries | Expected output |
| --- | --- | --- |
| `[8,3,8,-1]` | `[8,-1,4,8]` | `[0,3,-1,0]` |
| `[]` | `[0,MAX]` | `[-1,-1]` |
| `[2,2]` | `[]` | `[]` |
| `[MIN,MAX,MIN]` | `[MAX,MIN]` | `[1,0]` |
| `[7]` | `[7,6]` | `[0,-1]` |

The first 8 stays at index 0 even after index 2 is visited. **Target:** expected O(n+q) time; O(u) auxiliary directory, O(q) output. **Completion:** pass tests; explain why blindly overwriting an entry changes the problem to last-position lookup; demonstrate how missing keys differ from stored index zero.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### W5. Independent letter inventories

**Difficulty:** warm-up plus. **Objective:** use a fixed alphabet without stale state or shared mutable rows. **Prerequisites:** [bounded counts](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting), [array copying](./Java_Array.md#copying-arrays).

For each word, return a 26-element count row in alphabet order. N ≤ 100,000, total length L ≤ 1,000,000; all characters are lowercase English `a`–`z`. Empty words are valid. Output has N distinct row objects: changing one returned row later must not change another. There is no normalization or Unicode extension in this exercise.

```java
// public static int[][] letterInventories(String[] words)
```

For compact tests, `{a:2,b:1}` means a full length-26 row with those counts and zeros elsewhere; `{}` means 26 zeros. These are arrays, not maps.

| words | Expected rows |
| --- | --- |
| `["cabba","b",""]` | `[{a:2,b:2,c:1},{b:1},{}]` |
| `[]` | `[]` |
| `["z"]` | `[{z:1}]` |
| `["aa","aa"]` | `[{a:2},{a:2}]`, separate row objects |
| `["",""]` | `[{},{}]`, separate row objects |

The first word has two a's, two b's, and one c. **Target:** O(L+26N) time, O(26N) output, O(1) or O(26) auxiliary space depending on buffer strategy. **Completion:** check every row length/count sum; mutate one row in a test and verify the others stay intact; explain when `Arrays.fill` is needed and why 26 slots would fail for a broader alphabet.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

## 4. Level 2: Core Algorithmic Patterns

The pattern names are visible here because these are implementation drills. Complete the ten-step workflow before looking at hints. F5–F7 demonstrate required concepts even though the associated platform problems are Additional.

### F1. Count compatible index pairs

**Difficulty:** foundational. **Objective:** derive complement lookup with multiplicity and correct update timing. **Prerequisites:** W2, [complement lookup](./Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup).

Return the number of index pairs `(i,j)` with `i < j` and mathematical sum `values[i] + values[j] == target`. Different index pairs count separately even when values repeat. Empty and singleton inputs return zero. n ≤ 100,000; values and target are arbitrary ints. Return `long`; preserve input. Compute sums/complements without int wraparound.

```java
// public static long compatiblePairCount(int[] values, int target)
```

| values | target | Expected count |
| --- | --- | --- |
| `[4,7,4,2,5]` | `9` | `3` |
| `[]` | `0` | `0` |
| `[4]` | `8` | `0` |
| `[4,4,4]` | `8` | `3` |
| `[0,0,0,0]` | `0` | `6` |
| `[-3,1,2,4]` | `1` | `1` |
| `[1,2,3]` | `99` | `0` |
| `[MIN,MAX,1,0]` | `-1` | `1` |
| `[MAX,MAX]` | `-2` | `0` |
| 100,000 zeros | `0` | `4999950000` |

The first pairs are indices (0,4), (1,3), and (2,4). The last count exceeds int; two MAX values do not mathematically sum to -2. **Target:** expected O(n) time, O(u) auxiliary space, scalar output. **Completion:** compare with direct pair enumeration on small arrays; explain each pair is counted once, self-matches are excluded, and equal values at distinct positions remain legal.

<details>
<summary>Hint 1 — conceptual direction</summary>

For a fixed later index, what exact value must an earlier partner have? Does knowing that the value exists tell you how many index pairs it creates?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Store counts for the processed prefix. Decide whether that prefix includes the current element when you query it.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Promote before subtracting the current value from the target. Long keys can represent an out-of-int complement that simply has no stored match; the total also needs long.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F2. Group small cargo manifests

**Difficulty:** foundational plus. **Objective:** build a complete, immutable inventory signature. **Prerequisites:** W2/W5, [canonical grouping](./Arrays_and_Hashing_Essential_Concepts.md#7-canonical-signatures-and-grouping).

Each row of `manifests` lists cargo category codes from -2 through 2. Two rows belong together exactly when their counts of **every** category match; order inside a manifest does not matter. Return groups of row indices. N ≤ 100,000; total entries L ≤ 1,000,000; empty rows are allowed. Follow the shared grouping-order contract and preserve all rows. Do not use a raw array or a hash value alone as a content signature.

```java
// public static List<List<Integer>> groupManifests(int[][] manifests)
```

| manifests | One valid output |
| --- | --- |
| `[[0,-2,0],[0,0,-2],[-2,0],[],[],[2]]` | `[[0,1],[2],[3,4],[5]]` |
| `[]` | `[]` |
| `[[1],[1],[1,1]]` | `[[0,1],[2]]` |
| `[[-2,2],[2,-2],[0,0]]` | `[[0,1],[2]]` |
| one row with 1 copy of -2 and 11 copies of -1; another with 11 copies of -2 and 1 copy of -1 | `[[0],[1]]` |

Rows 0 and 1 in the first test contain two zeros and one -2; row 2 has only one zero. The final test detects ambiguous decimal concatenation. **Target:** expected O(L+N) with the fixed five-code domain, including signature work; O(N) output indices. Allow O(N) auxiliary signatures/map structures and O(N) temporary output growth. **Completion:** validate every index occurs once; prove both directions of signature equivalence; explain why collisions in `hashCode()` differ from two unequal inventories receiving the exact same key.

<details>
<summary>Hint 1 — conceptual direction</summary>

Which information survives every permitted rearrangement, while still distinguishing one copy from two copies?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

A five-field inventory describes the relevant information. Map an immutable encoding to the accumulating group.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Keep fields in a fixed category order, including zeros, with unambiguous boundaries. `computeIfAbsent` can create a stored group when needed.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F3. Build a static range-sum service

**Difficulty:** foundational. **Objective:** distinguish element indices from prefix boundaries. **Prerequisites:** [prefix sums and ranges](./Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries).

Implement a builder returning n+1 boundary totals for an unchanged int array, and a query returning the sum of `[from,to)`. The empty-prefix entry is zero. Queries satisfy `0 <= from <= to <= n`; the passed table is an unmodified builder result. n ≤ 100,000; full int values are allowed. Empty input has one boundary and supports only `[0,0)`. Both methods preserve their inputs.

```java
// public static long[] buildRangeIndex(int[] values)
// public static long rangeTotal(long[] prefix, int from, int to)
```

| values | Expected built table | Queries → expected answers |
| --- | --- | --- |
| `[3,-2,5,1]` | `[0,3,1,6,7]` | `[1,3) → 3; [0,4) → 7; [2,2) → 0; [3,4) → 1` |
| `[]` | `[0]` | `[0,0) → 0` |
| `[-5]` | `[0,-5]` | `[0,1) → -5; [1,1) → 0` |
| `[0,0]` | `[0,0,0]` | `[0,2) → 0` |
| `[MAX,MAX,MIN]` | `[0,2147483647,4294967294,2147483646]` | `[0,2) → 4294967294; [1,3) → -1` |

The first query contains -2 and 5. Its right endpoint is a boundary, not an included element. **Target:** O(n) build time and O(n) returned table, O(1) auxiliary beyond it; O(1) query time/space. For q queries, report O(n+q) overall time and O(n) retained index storage. **Completion:** compare every valid range on small arrays with direct summation, explain why negatives are allowed, and demonstrate that later mutation of the source would make a snapshot stale.

<details>
<summary>Hint 1 — conceptual direction</summary>

Represent the total before any element as well as the total after each element. What can the difference between two boundary readings describe?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Define one table entry as the sum of exactly the first b elements. Express the queried interval as the difference of two such prefixes.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Allocate n+1 long slots. Derive the write index from the boundary definition; test the query ending at n and the empty query before testing interior ranges.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F4. Products after temporary removals

**Difficulty:** foundational plus. **Objective:** combine reusable left/right aggregates without division. **Prerequisites:** [prefix/suffix products](./Arrays_and_Hashing_Essential_Concepts.md#9-prefixsuffix-products-and-aggregation).

Given `values` and q half-open ranges in `removedRanges`, return one product per query of all values **outside** that range. Queries are independent removals from the original array. Removing nothing retains everything; removing everything gives the empty product 1. Constraints: `0 <= n <= 18`, every value in [-9,9], `0 <= q <= 100,000`, and each query row contains exactly `[from,to]` with valid boundaries. No division. Return long values; preserve both inputs. The bound `9^18 = 150094635296999121` ensures all products of subsets, including intermediates, fit long.

```java
// public static long[] retainedProducts(int[] values, int[][] removedRanges)
```

| values | removedRanges | Expected output |
| --- | --- | --- |
| `[2,0,-3,4]` | `[[1,2],[1,3],[0,4],[2,2]]` | `[-24,8,1,0]` |
| `[]` | `[[0,0]]` | `[1]` |
| `[0]` | `[[0,1],[0,0]]` | `[1,0]` |
| `[0,0,5]` | `[[0,1],[0,2],[0,3]]` | `[0,5,1]` |
| `[-2,3,-4]` | `[[1,2],[0,1],[2,3]]` | `[8,-12,-6]` |
| `[2,3]` | `[]` | `[]` |
| 18 copies of 9 | `[[0,0],[0,18]]` | `[150094635296999121,1]` |

In the first output, removing only zero leaves `2 × -3 × 4 = -24`; removing zero and -3 leaves 8. **Target:** O(n+q) time, O(n) auxiliary index, O(q) output. Treat n parametrically even though the arithmetic domain caps it. **Completion:** derive each identity and boundary; test all ranges on short inputs; contrast one-query direct scanning with preprocessing; explain why the stated arithmetic domain is stronger than merely asserting final answers fit.

<details>
<summary>Hint 1 — conceptual direction</summary>

A removed contiguous block leaves at most two regions. Which calculations are shared by different queries?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Prepare a product for each left boundary and each right boundary. The empty side needs the multiplicative identity.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Use n+1 slots for each side and long arithmetic. Work out which two entries correspond to `[from,to)`; verify a range touching each end.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F5. Report occupied number runs

**Difficulty:** foundational plus. **Objective:** bound total expansion work and handle integer endpoints. **Prerequisites:** W3, [consecutive runs](./Arrays_and_Hashing_Essential_Concepts.md#10-consecutive-runs-and-sequence-head-expansion).

Return each maximal run of present consecutive **values** as a `Span(start,end)` with inclusive endpoints. Original positions do not matter; duplicates do not lengthen or duplicate runs. n ≤ 100,000, full int domain. Output spans may be in any order, but must partition the distinct input values into maximal runs. Preserve input. MIN and MAX are not consecutive to each other.

```java
// public static List<Span> occupiedRuns(int[] values)
```

Here `[a,b]` in output denotes `Span(a,b)`, not a half-open range.

| values | One valid output |
| --- | --- |
| `[3,8,2,3,1,-2,-1]` | `[[-2,-1],[1,3],[8,8]]` |
| `[]` | `[]` |
| `[5,5,5]` | `[[5,5]]` |
| `[4,3,2,1]` | `[[1,4]]` |
| `[MIN,MIN+1,MAX-1,MAX]` | `[[MIN,MIN+1],[MAX-1,MAX]]` |
| `[0,-1,1,0]` | `[[-1,1]]` |

The first case contains three disjoint maximal value runs; it is not asking for contiguous positions. Add a stress input containing 10,000 zeros and one occurrence of each value 1 through 9,999: expect only `Span(0,9999)`. **Target:** expected O(n) time, O(u) auxiliary membership, O(r) output for r runs. **Completion:** validate coverage/maximality/uniqueness; count total successor advances across all runs; handle boundary arithmetic and the repeated-head stress test.

<details>
<summary>Hint 1 — conceptual direction</summary>

Which member can uniquely identify the beginning of a maximal run? Starting again from every member repeats work.

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Visit distinct values, and expand only when no predecessor is present. Prove that different expansions cannot share a value.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Check integer endpoints before evaluating predecessor/successor expressions, or use a wider representation consistently. Visiting the original array can repeat the same head.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F6. Reconstruct ordered scores

**Difficulty:** foundational. **Objective:** use a feasible domain to replace comparison sorting. **Prerequisites:** W2, [bounded buckets](./Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets).

Return a new ascending array with exactly the input multiplicities. The caller supplies inclusive `low` and `high`; every value lies between them. `1 <= R = (long)high - low + 1 <= 10,000`, and n ≤ 100,000. Low/high can be near either int endpoint. Preserve input; do not call a sorting library. Empty input returns an empty result.

```java
// public static int[] orderedScoreCopy(int[] values, int low, int high)
```

| values | low, high | Expected output |
| --- | --- | --- |
| `[3,-1,3,0]` | `-1,3` | `[-1,0,3,3]` |
| `[]` | `-2,2` | `[]` |
| `[2,2,2]` | `2,2` | `[2,2,2]` |
| `[-1,0,1]` | `-1,1` | `[-1,0,1]` |
| `[1,0,-1]` | `-1,1` | `[-1,0,1]` |
| `[MIN+1,MIN,MIN+1]` | `MIN,MIN+1` | `[MIN,MIN+1,MIN+1]` |
| `[MAX,MAX-1]` | `MAX-1,MAX` | `[MAX-1,MAX]` |

Four input occurrences in the first row produce four output positions, including both 3s. **Target:** O(n+R) time, O(R) auxiliary counters, O(n) output. **Completion:** check sortedness and multiset equality; explain why total inner emission work is n, and why a range spanning most ints would invalidate this method's feasibility.

<details>
<summary>Hint 1 — conceptual direction</summary>

The domain tells you every possible value before reading the input. How could each possible value record its number of copies?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Count by an offset from low, then visit categories in numeric order.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Compute the domain width in long before narrowing. Iterate bucket indices rather than incrementing an int value past MAX.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### F7. Summarize multiplicity levels

**Difficulty:** foundational plus. **Objective:** distinguish buckets keyed by values from buckets keyed by frequencies. **Prerequisites:** W2, [frequency buckets](./Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets).

Return an int array `profile` of length n+1. For each `f >= 1`, `profile[f]` is the number of **distinct values** appearing exactly f times in the input. `profile[0]` is always zero; values that never appear are not counted. n ≤ 100,000 with any int values. Preserve input. Empty input returns `[0]`.

```java
// public static int[] multiplicityProfile(int[] values)
```

| values | Expected profile |
| --- | --- |
| `[4,4,4,7,7,9]` | `[0,1,1,1,0,0,0]` |
| `[]` | `[0]` |
| `[8]` | `[0,1]` |
| `[2,2,2,2]` | `[0,0,0,0,1]` |
| `[-1,0,1]` | `[0,3,0,0]` |
| `[MIN,MAX,MIN,MAX]` | `[0,0,2,0,0]` |

In the first case, one distinct value occurs once, one twice, and one three times. **Target:** expected O(n) time, O(u) auxiliary state, O(n) output. **Completion:** prove `sum(profile[f]) = u` and `sum(f * profile[f]) = n`; explain what extra payload would be needed to return the most frequent values themselves. That extension prepares L7 without requiring a heap.

<details>
<summary>Hint 1 — conceptual direction</summary>

An input value has a frequency; several different values may have the same frequency. Which of those levels does the output count?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

First determine the count of each distinct value, then classify each distinct value once by its final count.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

The largest possible frequency is n, so index n must exist. Traversing completed map entries avoids counting the same distinct value repeatedly.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

## 5. Level 3: NeetCode and LeetCode Practice

These seven titles, canonical URLs, difficulties, order, and E/A labels match the [repository roadmap](./DSA_Interview_Roadmap.md#1-arrays--hashing). E/A are the roadmap's priorities, not NeetCode difficulty labels. Read each linked original for its current statement, constraints, and judge interface; the notes below are original learning objectives, not replacements for those statements. Only test cases valid under that problem's constraints should be submitted. In particular, an original drill's empty-input or full-int contract need not match LeetCode's contract.

Use the ten-step workflow and record one attempt below. Official hints/editorials count as assistance. For familiar problems, change a requirement in scratch practice and re-derive it before claiming transfer. No solution code is supplied here.

### L1. Contains Duplicate

**E — Essential · Easy.** [Open Contains Duplicate](https://leetcode.com/problems/contains-duplicate/).

**Objective/prerequisites:** use membership correctly; W3 and [seen values](./Arrays_and_Hashing_Essential_Concepts.md#4-seen-value-tracking). Compare pairwise search, sorting, and a hash-based scan. Defend when early exit is valid. Test a late duplicate, all equal values, and no duplicate within the official constraints. Explain mutation/copy costs for a sorting alternative. **Completion:** accepted Java, independently explained invariant and expected time/auxiliary space, plus a cold re-solve if assistance was used.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L2. Valid Anagram

**E — Essential · Easy.** [Open Valid Anagram](https://leetcode.com/problems/valid-anagram/).

**Objective/prerequisites:** determine equality of inventories; W2/W5 and [frequency counting](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting). Explain why character membership alone loses information. Test repeated letters with unequal counts, unequal lengths, and reordered equal inventories. Justify the alphabet and compare sorting with counting, including initialization. **Completion:** accepted Java and an independent equivalence argument; discuss how a larger character domain would change representation and cost.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L3. Two Sum

**E — Essential · Easy.** [Open Two Sum](https://leetcode.com/problems/two-sum/).

**Objective/prerequisites:** translate an algebraic relation into useful stored information; W4/F1 and [complement lookup](./Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup). The output here is indices, unlike F1's count. Explain distinct-index enforcement, duplicate values, and why query/update timing depends on your state definition. Test equal-valued partners, negative values, and a partner found late, within the official contract. **Completion:** accepted Java, valid original indices, numerical-range reasoning, and a clear brute-force-to-optimized derivation.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L4. Group Anagrams

**E — Essential · Medium.** [Open Group Anagrams](https://leetcode.com/problems/group-anagrams/).

**Objective/prerequisites:** design a key whose equality exactly matches the intended relation; F2 and [grouping](./Arrays_and_Hashing_Essential_Concepts.md#7-canonical-signatures-and-grouping). Compare sorted-character and count signatures. Test multiple groups, repeated identical strings, and empty words if allowed by the official constraints. Validate groups as a partition with multiplicity; do not demand one unspecified ordering. **Completion:** accepted Java, both directions of key correctness, and analysis in N words and L total characters including construction/hash/equality and output.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L5. Product of Array Except Self

**E — Essential · Medium.** [Open Product of Array Except Self](https://leetcode.com/problems/product-of-array-except-self/).

**Objective/prerequisites:** adapt reusable aggregation to one output per position; F4 and [output-buffer reuse](./Arrays_and_Hashing_Essential_Concepts.md#96-constant-auxiliary-space-for-per-position-outputs). Derive the two-pass method without division, then meet O(n) time and O(1) auxiliary space excluding the returned array. Test no zero, one zero, two zeros, negative factors, and the minimum permitted length. Use the official numeric guarantees rather than assuming arbitrary products fit. **Completion:** accepted Java, separate left/right invariants, no hidden O(n) work array, and an honest output-space count.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L6. Longest Consecutive Sequence

**A — Additional · Medium.** [Open Longest Consecutive Sequence](https://leetcode.com/problems/longest-consecutive-sequence/).

**Objective/prerequisites:** demonstrate a global linear-work argument; F5 and [sequence-head expansion](./Arrays_and_Hashing_Essential_Concepts.md#10-consecutive-runs-and-sequence-head-expansion). Contrast value adjacency with input-position adjacency. Test duplicate heads, scattered runs, and a single long run. **Completion:** accepted Java meeting the required time bound under the hashing model; explain why each distinct value participates in only bounded expansion work and compare sorting. F5 can establish the core concept before this Additional problem is completed.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### L7. Top K Frequent Elements

**A — Additional · Medium.** [Open Top K Frequent Elements](https://leetcode.com/problems/top-k-frequent-elements/).

**Objective/prerequisites:** turn occurrence counts into selection; F7 and [bucket reasoning](./Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets). Begin with a sorting baseline, then consider buckets indexed by possible frequencies to meet expected O(n) time with O(n) auxiliary storage and O(k) output. Unlike F7, retain the identities of the values. Test k=1, k equal to the number of distinct values, and tied frequencies consistent with the official guarantees. Validate membership and required cardinality without demanding an unspecified order. **Completion:** accepted Java and explanation of count construction, bucket traversal, and total stored entries. Heap-based O(n+u log k) comparison is a **later extension after Phase 3**, not a prerequisite or gate here.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

## 6. Level 4: Mixed Interview Challenges

These four tasks use only this category's knowledge. Work with the statement and tests visible, but hints and references closed. The prerequisite is completion of the core drills and independent Essential practice; no specific pattern is named in a challenge's visible title or learning objective. An interviewer can select one without showing this section's surrounding tasks. Do not use a challenge as unseen evidence after reading its hints or practicing its implementation; choose a fresh variant on a later day.

### X1. Dispatch desk

**Difficulty:** interview transfer, moderate. **Objective:** derive state from consumable resources and ordered requests.

Each integer in `stock` represents one available unit with that identifier. Process `orders` in order. An order succeeds exactly when an unused matching unit remains; success consumes one unit, failure consumes nothing. Return one boolean per order. Each array length is at most 100,000, with any int identifiers. Preserve both arrays. Empty orders produce an empty result; empty stock cannot satisfy any order.

```java
// public static boolean[] dispatch(int[] stock, int[] orders)
```

| stock | orders | Expected output |
| --- | --- | --- |
| `[5,5,2]` | `[5,2,5,5,2]` | `[true,true,true,false,false]` |
| `[]` | `[7,7]` | `[false,false]` |
| `[1]` | `[]` | `[]` |
| `[0,0]` | `[0,0,0]` | `[true,true,false]` |
| `[MIN,MAX,MIN]` | `[MIN,MIN,MAX,MIN]` | `[true,true,true,false]` |
| `[3,3]` | `[2,3,2,3]` | `[false,true,false,true]` |

In the first example, the two units of 5 serve the first and third orders; no unit remains for the fourth. **Target:** expected O(n+m) time, O(u) auxiliary space for u distinct stock identifiers, O(m) output. **Completion:** pass the table and an independent slow simulation on small inputs; prove successful orders never exceed available copies, and explain why failure does not change future availability. Write the ten-step interview explanation before assigning I.

<details>
<summary>Hint 1 — conceptual direction</summary>

What must you remember after the first successful request for an identifier that originally had several copies?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Represent remaining availability by identifier. The invariant should describe unused units before each order.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Query a remaining count and reduce it only after success. Missing identifiers and a stored zero both mean no unit is available.

</details>

<details>
<summary>Prerequisite details and theory — open after attempting</summary>

Revisit W2 and [frequency counting](./Arrays_and_Hashing_Essential_Concepts.md#6-frequency-counting). Counts here describe remaining stock, not the number of processed orders.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### X2. Renamed labels

**Difficulty:** interview transfer, demanding. **Objective:** formalize an equivalence relation without losing positional information.

Two lowercase words are equivalent if one can be transformed into the other by consistently renaming its letters: each source letter maps to exactly one destination letter, and different source letters must map to different destination letters. Length and positions are preserved. Renaming is chosen independently for each compared pair. Return groups of input indices under this relation, with the shared grouping-order contract. N ≤ 100,000, total length L ≤ 1,000,000, alphabet `a`–`z`, empty words allowed. Preserve input.

```java
// public static List<List<Integer>> renamedLabelGroups(String[] words)
```

| words | One valid output |
| --- | --- |
| `["paper","title","apple","","","foo","egg","bar"]` | `[[0,1],[2],[3,4],[5,6],[7]]` |
| `[]` | `[]` |
| `["ab","aa","cd","zz"]` | `[[0,2],[1,3]]` |
| `["abba","noon","abab","cdcd"]` | `[[0,1],[2,3]]` |
| `["a","z","","aa"]` | `[[0,1],[2],[3]]` |

For `paper` → `title`, p maps to t, a to i, e to l, and r to e. `apple` has a different arrangement of repeated positions. `ab` cannot become `aa` because that would merge two distinct letters. **Target:** expected O(L+N) time including key work; O(N) returned indices/group containers and O(L+N) auxiliary storage allowed. **Completion:** give a two-direction proof for your representation; reject equal-histogram words with different repetition layouts; validate a partition retaining empty and repeated words. Compare against direct pairwise equivalence checks on small inputs.

<details>
<summary>Hint 1 — conceptual direction</summary>

The literal letter names can change, but which positions share a letter cannot change. Would a simple character inventory preserve that fact?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Give each newly encountered letter a deterministic local identity based on first occurrence. Equivalent words should then have identical identity sequences.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Reset the local letter mapping for each word. Encode identity numbers with clear boundaries, especially when they exceed 9, and group by immutable content keys.

</details>

<details>
<summary>Prerequisite details and theory — open after attempting</summary>

F2 and [canonical grouping](./Arrays_and_Hashing_Essential_Concepts.md#7-canonical-signatures-and-grouping) provide the method of defining equivalence before constructing a key. This relation preserves positional equality, not character multiplicities alone.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### X3. Balanced handoff

**Difficulty:** interview transfer, moderate. **Objective:** choose a representation that supports every possible boundary with little repeated work.

A handoff boundary b divides `adjustments` into `[0,b)` and `[b,n)`. Return **all** boundaries whose two mathematical sums are equal, in ascending order. Both empty sides are allowed, so b ranges from 0 through n. n ≤ 100,000; adjustments may be any int. Preserve input. Return an empty int array when no boundary qualifies.

```java
// public static int[] handoffBoundaries(int[] adjustments)
```

| adjustments | Expected output |
| --- | --- |
| `[2,-1,1,2]` | `[1,3]` |
| `[]` | `[0]` |
| `[0,0,0]` | `[0,1,2,3]` |
| `[1]` | `[]` |
| `[-1,1,0]` | `[0,2,3]` |
| `[MAX,MAX]` | `[1]` |
| `[MIN,MAX,1]` | `[0,3]` |

In the first example, the left and right sums are both 2 at boundaries 1 and 3; boundary 2 has sums 1 and 3. For empty input, the single boundary splits two empty sums. **Target:** O(n) time, O(1) auxiliary space beyond an exactly sized returned int array; O(b) output for b qualifying boundaries. Multiple linear passes are allowed. **Completion:** independently scan both sides for every boundary in a small-input checker; include all boundary positions; explain the numerical range and how to size output without retaining a separate growing list.

<details>
<summary>Hint 1 — conceptual direction</summary>

What changes when the dividing boundary moves across one element? Can you reuse information from its previous position?

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Relate one side's running total to the total of the whole array. Start with boundary zero rather than the first element.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

Use long totals. A counting pass can determine the exact number of answers before a final filling pass; both passes can repeat the same boundary predicate.

</details>

<details>
<summary>Prerequisite details and theory — open after attempting</summary>

F3 and [prefix boundaries](./Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries) support this variant. A stored table is not necessary when boundaries are visited in order.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### X4. Paired maintenance

**Difficulty:** interview transfer, demanding. **Objective:** adapt an existing idea to a changed exclusion rule and memory budget.

For every adjacent position pair `(i,i+1)`, independently remove those two values and return the product of all remaining values. Output position i corresponds to that pair, so output length is n-1. Constraints: `2 <= n <= 18`, values in [-9,9], empty product 1. Use long arithmetic; division is forbidden. Preserve input. The same `9^18` bound as F4 covers every intermediate product.

```java
// public static long[] maintenanceOutputs(int[] values)
```

| values | Expected output |
| --- | --- |
| `[2,0,-3,4]` | `[-12,8,0]` |
| `[5,-2]` | `[1]` |
| `[0,0,5]` | `[5,0]` |
| `[0,2,0,3]` | `[0,0,0]` |
| `[-2,3,-4]` | `[-4,-2]` |
| `[1,1,1,1]` | `[1,1,1]` |
| 18 copies of 9 | 17 copies of `1853020188851841` |

In the first example, removing positions 1 and 2 leaves 2 and 4, whose product is 8. **Target:** O(n) time, O(1) auxiliary space excluding O(n) output; use n as the work-size parameter despite the arithmetic cap. **Completion:** compare with direct multiplication outside each pair, explain both endpoint pairs and zero behavior, and justify every update's timing. A correct solution with extra arrays is a useful intermediate result; complete the stated storage target before claiming the challenge is solved.

<details>
<summary>Hint 1 — conceptual direction</summary>

Which portions remain on either side of a removed pair? Compare neighboring answers to identify repeated work.

</details>

<details>
<summary>Hint 2 — algorithmic direction</summary>

Temporarily store one side's contribution in each output position. Carry the other side in a scalar while traversing in reverse.

</details>

<details>
<summary>Hint 3 — implementation direction</summary>

For answer i, the retained right side begins at i+2. Write down the scalar's meaning before updating it; trace n=2 and n=3 to catch accidental inclusion of a removed element.

</details>

<details>
<summary>Prerequisite details and theory — open after attempting</summary>

F4/L5 and [constant auxiliary aggregation](./Arrays_and_Hashing_Essential_Concepts.md#96-constant-auxiliary-space-for-per-position-outputs) explain the storage technique. The pair exclusion requires a new boundary argument.

</details>

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

## 7. Complexity Analysis Exercises

These supplied snippets are questions to analyze, not implementations to copy into another exercise. Put each static method inside a class with `import java.util.*;`. For every C task, record **time, auxiliary space, output space, a derivation, and whether/how it can be improved without changing behavior**. Define variables and expected/worst-case assumptions. State peak live memory separately from total allocation. Confirm the small behavioral example before reasoning about growth. Completion requires a written argument and a justified comparison; a stopwatch alone is insufficient.

Use [Complexity](./Complexity/Complexity_Analysis_and_Foundations.md) and the [cost model](./Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model) only after your first analysis. The discussion checkpoints are collapsed at the end of this section, away from the questions; they contain analysis, not optimized Java solutions.

### C1. Every earlier comparison

**Difficulty:** analysis foundation. **Objective:** count a triangular iteration domain. **Prerequisites:** loops and fixed-width arithmetic. n ≤ 100,000, arbitrary ints, no mutation. The method counts equal index pairs.

```java
static long earlierEqualPairs(int[] values) {
    long matches = 0;
    for (int i = 0; i < values.length; i++) {
        for (int j = 0; j < i; j++) {
            if (values[i] == values[j]) matches++;
        }
    }
    return matches;
}
```

`[2,2,3,2] → 3`; `[] → 0`; four equal values → 6. Explain whether an all-distinct array changes the loop count. Derive the total comparisons before using Big O. Propose a different state that retains the same multiplicity semantics and discuss its memory cost.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### C2. Two kinds of lookup

**Difficulty:** analysis intermediate. **Objective:** distinguish key lookup from a search through map values. **Prerequisites:** W2/W3 and the [Maps reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md). n,m ≤ 100,000, arbitrary int inputs, no mutation. Return two counts: queries equal to an observed value; queries equal to at least one observed value's frequency. Duplicate queries count repeatedly.

```java
static long[] auditQueries(int[] values, int[] queries) {
    Map<Integer, Integer> counts = new HashMap<>();
    Set<Integer> observed = new HashSet<>();
    for (int value : values) {
        counts.put(value, counts.getOrDefault(value, 0) + 1);
        observed.add(value);
    }
    long present = 0;
    long frequencyExists = 0;
    for (int query : queries) {
        if (observed.contains(query)) present++;
        if (counts.containsValue(query)) frequencyExists++;
    }
    return new long[]{present, frequencyExists};
}
```

`values=[4,4,7], queries=[4,2,1,9] → [1,2]`; empty values with any queries → `[0,0]`. Analyze in n, m, distinct count u, and capacity c where relevant. Why does one apparently similar lookup have a different cost? Can a preprocessing step improve many queries? Is both the map and the observed-value set necessary?

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### C3. Sorting fresh prefixes

**Difficulty:** analysis intermediate. **Objective:** include copying, sorting, and transient storage. **Prerequisites:** [array copies/sorting](./Java_Array.md). n ≤ 10,000, values in [-100,100], no mutation. For each nonempty prefix, the method adds its sorted middle value at index `length/2`.

```java
static long prefixMiddleChecksum(int[] values) {
    long checksum = 0;
    for (int end = 1; end <= values.length; end++) {
        int[] copy = Arrays.copyOfRange(values, 0, end);
        Arrays.sort(copy);
        checksum += copy[copy.length / 2];
    }
    return checksum;
}
```

`[3,1,2] → 8` from middle values 3, 3, and 2; `[] → 0`; `[-1,-1] → -2`. Derive a safe sorting-based upper bound, and separate all allocated array slots over time from simultaneously needed storage. Express sort workspace as `S_sort(n)` unless you justify a bound. Does sorting once preserve these **original-prefix** answers? Under this exercise's small domain, propose an improvement using only current-category knowledge; no heap is required.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### C4. Retained text snapshots

**Difficulty:** analysis intermediate. **Objective:** count copied and hashed characters, including retained keys. **Prerequisites:** [Strings](./Java_String.md). Text is lowercase English, length n ≤ 5,000. Return a map from every nonempty prefix to its length; do not mutate input.

```java
static Map<String, Integer> textSnapshots(String text) {
    Map<String, Integer> result = new HashMap<>();
    String prefix = "";
    for (int i = 0; i < text.length(); i++) {
        prefix = prefix + text.charAt(i);
        result.put(prefix, i + 1);
    }
    return result;
}
```

`"aba" → {"a":1,"ab":2,"aba":3}`; `"" → {}`. Count the lengths of all retained keys. Distinguish required output from transient strings and map growth. Would replacing concatenation with `StringBuilder` followed by `toString()` on every iteration change the asymptotic cost of this output contract? What if the contract required only the final string instead? Explain which improvement changes the task and which preserves it.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### C5. Cheap queries, many candidates

**Difficulty:** analysis intermediate. **Objective:** distinguish preprocessing, one query, and enumerating all ranges. **Prerequisites:** F3. The argument is a valid n+1-entry prefix table of an int array with n ≤ 100,000. The original prefix construction is outside the method; no mutation. The result counts nonempty zero-sum ranges.

```java
static long zeroRangeScan(long[] prefix) {
    long count = 0;
    for (int from = 0; from < prefix.length - 1; from++) {
        for (int to = from + 1; to < prefix.length; to++) {
            if (prefix[to] - prefix[from] == 0) count++;
        }
    }
    return count;
}
```

Table `[0,1,0,0]` for input `[1,-1,0]` gives 3; `[0]` gives 0; `[0,0,0]` gives 3. Analyze the method alone, then the whole process including one prefix construction and its storage. Explain why constant query time does not make the nested enumeration linear. **Optional extension only:** propose a better way to count equal boundary totals; implementing target-sum subarray counting is not a new roadmap requirement.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### C6. Membership depends on the receiver

**Difficulty:** analysis foundation plus. **Objective:** identify the real collection performing a lookup. **Prerequisites:** W3 and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md). n,m ≤ 100,000. `allowed` is specifically an `ArrayList<Integer>` without nulls; input values are arbitrary ints. Return how many distinct input values occur in that list; mutate neither input.

```java
static int allowedDistinctCount(int[] values, ArrayList<Integer> allowed) {
    Set<Integer> distinct = new HashSet<>();
    for (int value : values) distinct.add(value);
    int count = 0;
    for (int value : distinct) {
        if (allowed.contains(value)) count++;
    }
    return count;
}
```

`values=[3,3,1], allowed=[1,1,9] → 1`; either input empty → 0. Analyze the cost of building and iterating the set, then identify which object receives `contains`. Explain how converting the allowed collection could change time and memory, and when that preprocessing may not be worthwhile.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### Complexity discussion checkpoints

<details>
<summary>Open only after writing all six analyses</summary>

| ID | Expected reasoning checkpoints |
| --- | --- |
| C1 | n(n−1)/2 comparisons regardless of equality outcomes: Θ(n²+1) time, O(1) auxiliary/scalar output. Counts of earlier equal values permit expected O(n) time with O(u) working state. |
| C2 | Construction expected O(n). Key membership is expected constant; searching values can inspect u entries plus capacity. With ordinary capacity, a safe expected-model upper bound is O(n+m(u+1)); O(u) auxiliary and constant output. A set of final frequencies supports expected O(n+m) overall. The map's keys already encode observed membership. Early matches may make particular queries cheaper. |
| C3 | Sum copying O(k) and sorting O(k log(k+1)) for k=1..n: O(n² log(n+1)+1) upper bound. Copy allocation volume is Θ(n²); peak auxiliary is O(n+S_sort(n)), not that volume. Output is scalar. One global sort loses prefix membership. Maintain counts over the 201 allowed values and scan those counts for each prefix's middle rank: O(nR), R=201, with O(R) state. |
| C4 | All prefix lengths sum to n(n+1)/2. Under the ordinary materialized-String model, expected time and required output characters are Θ(n²+1); key hashing is included. Explicit transient text is O(n); allowing temporary map growth also gives O(n) auxiliary beyond output. A builder cannot remove quadratic materialization of all keys. A final-string-only contract has different output requirements. |
| C5 | There are n(n+1)/2 nonempty ranges: Θ(n²+1) time, O(1) auxiliary within the method, scalar output. Building once adds O(n) time and O(n) retained table storage to the application. Optional equal-prefix counting can avoid enumerating ranges; justify multiplicities and the empty boundary. |
| C6 | Default-growth set construction expected O(n), traversal O(u+c), each list membership O(m) worst case: expected-model upper bound O(n+u+c+um+1). With c=O(u+1), simplify to O(n+u(m+1)+1); O(u) auxiliary and scalar output. An additional allowed-value hash set permits expected O(n+m) time with O(u+v) state for v distinct allowed values. |

These are bounds under stated models, not timing predictions. Hash degradation requires additional analysis; Java implementation details do not make arbitrary key work constant. In C3, the sorting upper bound need not be tight on every dataset. In C4, a different compact output representation would be a different API contract.

</details>

## 8. Debugging Challenges

Every snippet below compiles as Java 21 but violates its stated contract for at least one valid input. For each D task: identify the defect, explain its cause, construct a failing input **before running**, record expected versus actual behavior, repair your own copy, run regression tests, and analyze corrected time/auxiliary/output space. Do not stop at editing a line until the invariant is clear. Correct alternatives are welcome; meeting the stated contract and target matters more than matching a preferred implementation.

Each example table states **required behavior**, not what the buggy code necessarily returns. Full corrections are deliberately omitted. [Java pitfalls](./Arrays_and_Hashing_Essential_Concepts.md#12-java-specific-pitfalls) is the shared prerequisite; each task adds a specific reference. Completion requires all five diagnosis/repair steps and at least one new passing and one new failing-before-repair test.

### D1. The eager partner registry

**Difficulty:** debugging foundation. **Objective:** align state timing with distinct-index requirements. **Prerequisite:** [complements](./Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup). Intended contract: return whether two **different** positions sum to target. n ≤ 100,000; values and target in [-1,000,000,1,000,000], so this task isolates state timing rather than overflow. No mutation. Corrected target: expected O(n) time, O(u) auxiliary space, scalar output.

```java
static boolean buggyPairExists(int[] values, int target) {
    Map<Integer, Integer> indexByValue = new HashMap<>();
    for (int i = 0; i < values.length; i++) {
        indexByValue.put(values[i], i);
        if (indexByValue.containsKey(target - values[i])) return true;
    }
    return false;
}
```

| values | target | Required output |
| --- | --- | --- |
| `[4]` | `8` | `false` |
| `[4,4]` | `8` | `true` |
| `[1,3]` | `4` | `true` |
| `[]` | `0` | `false` |
| `[-2,5]` | `10` | `false` |

One occurrence of 4 cannot occupy both positions; two occurrences can. Explain the map's meaning at the exact point of the query. Merely forbidding equal values is not a valid repair.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### D2. Inclusive request, boundary table

**Difficulty:** debugging foundation. **Objective:** translate a caller's endpoint convention. **Prerequisite:** [prefix boundaries](./Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries). A valid prefix table comes from F3 with original length `1 <= n <= 100,000`. This API receives **inclusive element indices** satisfying `0 <= left <= right < n`; it must return that element range's sum. No empty ranges are accepted here. Preserve input. Corrected target: O(1) time/auxiliary, scalar output.

```java
static long buggyInclusiveTotal(long[] prefix, int left, int right) {
    return prefix[right] - prefix[left];
}
```

| prefix | left, right | Required output |
| --- | --- | --- |
| `[0,3,1,6,7]` | `1,2` | `3` |
| `[0,-5]` | `0,0` | `-5` |
| `[0,3,1,6,7]` | `0,3` | `7` |
| `[0,2147483647,4294967294]` | `0,1` | `4294967294` |

The first request includes both -2 and 5. Draw element indices and boundary indices separately; changing the prefix builder's contract is not a repair for this API.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### D3. Content keys that split equal inventories

**Difficulty:** debugging intermediate. **Objective:** distinguish object identity from inventory equality. **Prerequisite:** [grouping keys](./Arrays_and_Hashing_Essential_Concepts.md#7-canonical-signatures-and-grouping). Intended output: the number of distinct lowercase-letter inventories among words, ignoring character order but preserving multiplicity. N ≤ 100,000, total length L ≤ 1,000,000; `a`–`z`, empty words allowed, no mutation. Corrected target: expected O(L+N) time, O(N) auxiliary storage for fixed-alphabet signatures, scalar output.

```java
static int buggyInventoryKinds(String[] words) {
    Set<int[]> keys = new HashSet<>();
    for (String word : words) {
        int[] counts = new int[26];
        for (int i = 0; i < word.length(); i++) {
            counts[word.charAt(i) - 'a']++;
        }
        keys.add(counts);
    }
    return keys.size();
}
```

| words | Required output |
| --- | --- |
| `["ab","ba"]` | `1` |
| `["ab","abb"]` | `2` |
| `["",""]` | `1` |
| `[]` | `0` |
| `["a","a","b"]` | `2` |

Both arrays for `ab` and `ba` hold the same counts; determine which equality the set actually uses. Explain why using only `Arrays.hashCode(counts)` would not be a general collision-safe correction.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### D4. A compact but ambiguous label

**Difficulty:** debugging intermediate. **Objective:** prove an encoding preserves all its fields. **Prerequisite:** [count signatures](./Arrays_and_Hashing_Essential_Concepts.md#73-count-signatures-without-ambiguity). Input is one word containing only `a` and `b`, length ≤ 1,000,000. Required key format is the decimal a-count, a colon, then the decimal b-count, with no leading zeros except zero itself. This defines exact outputs as well as unambiguous equality. No mutation. Corrected target: O(k+s) time, O(s) output and O(s) auxiliary allowance, where s is the encoded key length.

```java
static String buggyTwoLetterKey(String word) {
    int a = 0;
    int b = 0;
    for (int i = 0; i < word.length(); i++) {
        if (word.charAt(i) == 'a') a++;
        else b++;
    }
    return "" + a + b;
}
```

| word | Required output |
| --- | --- |
| `"abbbbbbbbbbb"` (one a, eleven b's) | `"1:11"` |
| `"aaaaaaaaaaab"` (eleven a's, one b) | `"11:1"` |
| `""` | `"0:0"` |
| `"abba"` | `"2:2"` |

Demonstrate that the first two inventories produce the same buggy key, not merely the same hash. Explain why the fixed separator is meaningful for this numeric domain. Generalize your explanation to more count fields without writing a full grouping solution.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### D5. Wide destination, narrow calculation

**Difficulty:** debugging foundation. **Objective:** reason about the type of an arithmetic expression before assignment. **Prerequisite:** [numeric pitfalls](./Arrays_and_Hashing_Essential_Concepts.md#12-java-specific-pitfalls). Given any two int values, return their exact mathematical sum as long. No mutation. Corrected target: O(1) time/auxiliary, scalar output.

```java
static long buggyWideSum(int left, int right) {
    return (long) (left + right);
}
```

| left | right | Required output |
| --- | --- | --- |
| `MAX` | `1` | `2147483648` |
| `MIN` | `-1` | `-2147483649` |
| `MAX` | `MAX` | `4294967294` |
| `MIN` | `MAX` | `-1` |
| `-3` | `3` | `0` |

Trace the evaluation type and result before the cast. Explain why this two-int contract always fits long, while multiplying arbitrarily many ints would require a separate numeric contract.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

### D6. Duplicate starting reports

**Difficulty:** debugging intermediate. **Objective:** distinguish membership state from the outer traversal's multiplicity. **Prerequisite:** [consecutive-run reasoning](./Arrays_and_Hashing_Essential_Concepts.md#10-consecutive-runs-and-sequence-head-expansion). Return the start value of every maximal consecutive-value run **exactly once**, in any order. n ≤ 100,000, full int domain, no mutation. Corrected target: expected O(n) time, O(u) auxiliary space, O(r) output for r runs.

```java
static List<Integer> buggyRunStarts(int[] values) {
    Set<Integer> present = new HashSet<>();
    for (int value : values) present.add(value);
    List<Integer> starts = new ArrayList<>();
    for (int value : values) {
        if (value == Integer.MIN_VALUE || !present.contains(value - 1)) {
            starts.add(value);
        }
    }
    return starts;
}
```

| values | One valid required output |
| --- | --- |
| `[1,1,2,5]` | `[1,5]` |
| `[2,2,2]` | `[2]` |
| `[3,2,1]` | `[1]` |
| `[]` | `[]` |
| `[MIN,MIN,MAX]` | `[MIN,MAX]` |

The two copies of 1 belong to the same value run. The integer-boundary guard is intentional and correct; explain the separate defect that remains. If this routine expanded a whole run after reporting a start, explain the extra performance risk as well.

- [ ] Attempt tested and reviewed. **Log:** date —; result —; minutes —; due —; lesson —.

## 9. Revision and Spaced Repetition

Follow the roadmap's [review process](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition). Start with approximately **1, 3, 7, and 14 days after the learning attempt**, adjusting to recall and difficulty. These are suggestions, not deadlines; missing a day does not require restarting. Move H/S work earlier and comfortable independent work later. Keep the next due date and short history on the existing exercise record; do not make a second revision ledger.

For a review, open only the statement and contracts. Hide previous code, hints, theory, and the complexity checkpoints. Reconstruct the baseline, bottleneck, improved state, invariant, and cost before coding. Choose one ordinary example and a risk-focused test. Only after finishing, compare with the old attempt and classify the new result. Record the missing idea, such as “lost multiplicity” or “mixed element indices and boundaries,” instead of “need more practice.”

Prioritize unfinished S, repeated H, failed unfamiliar attempts, then overdue I reviews. After H/S, explain what assistance unlocked and rebuild the prerequisite if needed; an immediate reproduction demonstrates understanding but needs a later cold I before Essential practice is independently established. If familiar code feels memorized, use a changed requirement or a fresh unseen variant to test recognition.

Do not retroactively relabel an assisted attempt I. Preserve a brief sequence on the same log, for example `date H → later-date I; due next-date; lesson ...`, using actual dates when you attempt it. No examples here are recorded learner progress.

## 10. Completion and Readiness Checklist

The following conditions describe evidence to assess, not automatic promotions. Keep the actual topic-level status in the [roadmap dashboard](./DSA_Interview_Roadmap.md#2-progress-dashboard). These checkboxes are a local verification checklist, without duplicate counts or a second T/I/P/R dashboard.

- [ ] **Fundamental drills:** W1–W5 and F1–F7 were implemented from a blank editor, tested, and explained. Assisted work was followed by cold independent reproduction. I can explain mutation, multiplicity, key equality, range boundaries, product identities, unique sequence heads, and bucket bounds. This supports the roadmap's Implementation gate.
- [ ] **Theory:** without notes, I can choose set/map/array state, state an invariant, explain expected hashing and string-key costs, and compare sorting/hashing/buckets. I can explain why the relevant algorithm works, including its assumptions. Reading or owning the concepts file does not earn this evidence.
- [ ] **Essential practice:** all five E problems have independent solves or cold independent re-solves with edge-case tests and reasoning. A submitted/accepted solution alone is insufficient. This supports Practice; an H/S history is compatible with eventual completion after later I evidence.
- [ ] **Additional transition:** Essential patterns are reliable, or I can name the specific gap L6/L7 will address. I have demonstrated sequence-head and bucket reasoning in F5/F7 even if I postpone those Additional platform tasks. Heap mastery is not required; Additional completion does not replace missing Essential evidence.
- [ ] **Complexity and debugging:** I can explain C1–C6 and diagnose/repair D1–D6, including real failure witnesses. I account for library work, numerical bounds, output memory, and total work across nested loops.
- [ ] **Review after failure:** every unresolved H/S has a next action and due date on its own record. I can state whether the gap was recognition, state, correctness, Java, tests, or complexity. Conceptual failure during a cold review triggers reassessment of affected roadmap gates; isolated independently corrected syntax errors do not erase all prior evidence.
- [ ] **Ready to attempt unfamiliar tasks:** core implementations and Essential reasoning are reliable enough to close references. I can work through the ten-step protocol without being told a pattern. If hints are needed on X1–X4, I record assistance and reserve a fresh variant for later assessment.
- [ ] **Arrays & Hashing readiness:** across at least two separate sessions, I have independently solved unfamiliar tasks, including an unseen lookup/grouping variant and an unseen prefix/aggregation variant. I can defend the stored state/key, expected bound, a sorting alternative, duplicate-index behavior, and applicable zero/overflow cases. I communicate the derivation, proof, Java choices, and tests. This supplies evidence for the roadmap's common and topic-specific Readiness rubric; record the assessment date and exercise references at the roadmap's existing readiness paragraph when you choose to update it.

Distinguish four achievements: **running code** passes selected tests; **independent derivation** discovers the approach without help; **correctness understanding** explains why all valid inputs are covered; **transfer** adapts to unfamiliar requirements. Tests support all four but do not establish the last three by themselves. Finishing this category does not establish readiness for every branch of the full roadmap.

## 11. Learning References and Validation

Use the concept-specific links beside a drill only when studying or reviewing. Keep challenge references closed during assessment.

- [Roadmap scope and seven selected problems](./DSA_Interview_Roadmap.md#1-arrays--hashing), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Arrays & Hashing — Essential Concepts](./Arrays_and_Hashing_Essential_Concepts.md): primary source for definitions, derivations, worked examples, and invariants.
- [Java Arrays](./Java_Array.md), [Java Strings](./Java_String.md), [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md), and [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): repository prerequisites and API references.
- [Official NeetCode Roadmap](https://neetcode.io/roadmap): curriculum behind the repository selection. The workbook's seven problem URLs, labels, and difficulties were checked against the local roadmap; original exercise difficulties are editorial labels.
- [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) and [HashSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html): hash-distribution assumptions and capacity-sensitive traversal.
- [Java 21 Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html): key equality, lookup, and group-creation contracts. [Java 21 Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html): copying, content comparison, and sorting; the primitive sorting implementation note supplies an O(n log n) time bound, not a universal O(1) workspace promise.
- [Java 21 String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) and [StringBuilder](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html): immutable text, content operations, and construction costs.

Java API references were checked using Context7 and Oracle's Java 21 documentation on **24 October 2026**. The problem statements, examples, and teaching prompts for original exercises are authored for this workbook. Platform statements are linked rather than reproduced.

The starter class, all 18 suggested method signatures instantiated as placeholder stubs, and all 12 supplied analysis/debugging methods compiled with Temurin JDK 21.0.12 using `javac --release 21 -Xlint:all -Werror`. A temporary Java harness outside the repository passed **156 checks**, including 136 table-derived and companion checks, the complexity snippets' behavioral examples, separate equal-content String objects, duplicate-head stress, integer boundaries, and exact product limits. Independent direct-scan, sorting, and pairwise-equivalence models checked the expected results. All six deliberately flawed methods produced a contract violation on at least one listed valid input; 28 debugging-table probes were run. These checks validate the specifications and supplied snippets, not unwritten learner implementations.

All **83 local Markdown links**, including heading anchors, were checked; the seven platform entries were matched to the roadmap's URLs, priorities, and difficulties. Attempt records remain unchecked. No existing repository file was modified.

The intended limits remain non-null inputs, feasible allocation, each stated alphabet/domain, fixed-width arithmetic, and expected hash behavior. Test runs can verify cases and defects; complexity and general correctness still require the requested arguments. No LeetCode submission or learner attempt is implied by generating or checking this file.



