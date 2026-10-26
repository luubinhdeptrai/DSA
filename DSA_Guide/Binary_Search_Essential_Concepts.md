# Binary Search — Essential Concepts (Java 21)

> Binary search is a proof-guided way to discard candidates. Before moving a boundary, explain why the answer cannot be in the part being removed.

- [1. Introduction and Learning Objectives](#1-introduction-and-learning-objectives)
- [2. Binary Search Fundamentals](#2-binary-search-fundamentals)
- [3. Search Intervals and Loop Invariants](#3-search-intervals-and-loop-invariants)
- [4. Exact-Match Binary Search](#4-exact-match-binary-search)
- [5. Boundary Search](#5-boundary-search)
- [6. Binary Search in Rotated Sorted Arrays](#6-binary-search-in-rotated-sorted-arrays)
- [7. Binary Search on Answer](#7-binary-search-on-answer)
- [8. Binary Search in Other Ordered Structures](#8-binary-search-in-other-ordered-structures)
- [9. Java 21 Implementation Details](#9-java-21-implementation-details)
- [10. Search-Space Invariants and Correctness](#10-search-space-invariants-and-correctness)
- [11. Detailed Complexity Analysis](#11-detailed-complexity-analysis)
- [12. Binary Search versus Alternative Techniques](#12-binary-search-versus-alternative-techniques)
- [13. Common Mistakes and Edge Cases](#13-common-mistakes-and-edge-cases)
- [14. Pattern Recognition Practice](#14-pattern-recognition-practice)
- [15. Conceptual Interview Questions and Answer Key](#15-conceptual-interview-questions-and-answer-key)
- [16. Key Takeaways](#16-key-takeaways)
- [17. References and Next Steps](#17-references-and-next-steps)

## 1. Introduction and Learning Objectives

This guide develops the [Binary Search category](./DSA_Interview_Roadmap.md#4-binary-search) in Phase 2 of the local NeetCode-based roadmap. The roadmap is authoritative for required knowledge, problem priorities, and readiness. Prior experience with Koko or a familiar search template is useful background; independently deriving a fresh boundary search is stronger evidence of understanding.

By the end, you should be able to define an ordered candidate space, prove a safe elimination rule, choose an interval convention, write Java 21, and explain the final boundary. Learn exact matches, duplicate ranges, first/last predicate boundaries, distinct-value rotations, and minimum/maximum feasible answers. Also recognize when sorting, hashing, a scan, or two pointers is more suitable.

### Reuse prerequisites rather than relearn them here

| Existing material | Reuse | Check before continuing |
| --- | --- | --- |
| [Phase 0 foundations](./DSA_Interview_Roadmap.md#phase-0-foundations) | Loops, integer arithmetic, recursion, and invariants | Explain why a loop progresses and whether an expression can overflow |
| [Java Arrays](./Java_Array.md) | Indexing, copies, sorting, rectangular/jagged matrices | Distinguish an element index from an exclusive endpoint |
| [Java Strings](./Java_String.md) | Indexed access and comparison costs | Explain why comparing two strings may cost more than O(1) |
| [Arrays & Hashing concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Sufficient state, lookup alternatives, total algorithm costs | Explain whether an equality lookup needs order at all |
| [Two Pointers concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md) | Safe elimination, sorted order, and termination | Justify discarding candidates instead of merely describing movement |
| [Two Pointers exercises](./Two_Pointers/Two_Pointers_Exercises.md) | Blank-editor practice, explicit contracts, and independent tests | Separate a passing sample from a correctness argument |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Halving loops, recursion depth, auxiliary space, library work | Count comparisons separately from the cost of each comparison |

The Stack references now live in the `Stack` subdirectory. [Explicit versus recursive stacks](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack) can help with recursive-search space analysis; no stack algorithm is a prerequisite for the search loop.

### Scope and roadmap coverage

| Roadmap objective | Main sections |
| --- | --- |
| Derive exact and first-true/lower-bound searches; use one consistent interval | 2–5, 10 |
| Prove progress on empty, singleton, two-element, and duplicate ranges | 3–5, 13 |
| Find first/last occurrences without assuming a library hit is the first | 5, 9 |
| Identify the sorted half of a rotation and justify the selected half | 6 |
| Construct monotone feasibility and valid answer bounds | 7 |
| Include predicate work and preprocessing in total complexity | 7, 9, 11–12 |
| Implement exact search, lower/upper bounds, and a separate first-feasible predicate | 4–5, 7 |

Duplicate-heavy rotated searching and the roadmap's Hard partition problem are **optional extensions**. Recursive exact search is included to understand the call-stack tradeoff; iterative search is the usual constant-auxiliary default. Matrix and timestamp examples support the roadmap's Additional problems without implementing a complete key-value store or a platform answer collection.

The [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain separate: explanation supports T; independently rebuilt drills support I; independent/cold Essential solves support P; unfamiliar transfer across separate sessions supports R. This document earns none automatically and creates no duplicate progress log.

### Code contracts and cost model

Combine the `java` blocks in this guide inside a scratch `class BinarySearchExamples`, with `import java.util.*;` and `import java.util.function.*;` at file scope. The blocks contain static methods; later examples reuse earlier helpers. The single deliberately broken method in Section 13 is clearly labelled and must not be used as a search implementation.

Unless stated otherwise, inputs are non-null, arrays have at most 100,000 entries, execution is single-threaded, and memory allocation is feasible. Inputs remain unchanged. A sorted array means **nondecreasing numerical order**; duplicates are permitted except in the distinct-rotation method. Sorting/rotation/rectangular-shape requirements are preconditions, not checked by a full scan inside each search. Validating them would add real work. Null is outside the educational contracts; empty inputs are supported as specified. Predicate callbacks must be non-null, deterministic during the search, and monotone in the stated direction.

Indices use int; wider numerical answer domains use long. Fixed-width arithmetic and valid array access cost O(1), provided the intended arithmetic is represented correctly. Auxiliary space excludes input and required returned output; report output separately. A callback's own workspace or a preprocessing copy still belongs in the complete algorithm's cost. Use O(log(n+1)) when accounting for n=0; O(log n) is the conventional nonempty asymptotic description. O(n) can similarly be read as O(n+1) at the empty boundary.

## 2. Binary Search Fundamentals

Imagine guessing a whole number from 1 to 100 while being told “too low” or “too high.” Guessing near the middle is valuable because either response removes about half the possibilities. The response must be trustworthy: if “too low” did not rule out all smaller guesses, discarding them would be unjustified.

In a sorted array, comparing the middle value with the target provides exactly that evidence. If `a[mid] < target`, every value at an index at most mid is also below the target. If `a[mid] > target`, every value at an index at least mid is above it. Equality supplies a match, but does not by itself identify the first or last duplicate.

### A concrete elimination

```text
indices:   0   1   2    3    4    5    6
values:   [2,  5,  8,  12,  16,  23,  38]
           L           M              R
target: 23

12 < 23, so indices 0..3 cannot match.

values:   [2,  5,  8,  12,  16,  23,  38]
                             L    M    R
23 == 23, so return index 5.
```

The algorithm discards positions because of a proven order implication. Merely seeing a middle value smaller than the target is insufficient on unsorted data: in `[3,1,2]`, the target 3 lies to the left of the middle value 1.

### What must be searchable?

Binary search needs a candidate space with an order and a rule that safely removes a large contiguous portion of it. The space might be array indices, flattened matrix indices, integer capacities, timestamps, or a virtual sequence that is never materialized. It does not require the input values themselves to be a sorted array in every application.

A common generalization is a **monotonic predicate**: a yes/no question whose answers change direction at most once over the ordered domain. For example:

```text
candidate index: 0 1 2 3 4 5
predicate:       F F F T T T    first true = 3

candidate size:  1 2 3 4 5 6
feasible:        T T T F F F    last true size = 3
```

Here monotonic refers to the boolean results, not necessarily the original input array. “Feasible” must be defined precisely. A false→true predicate locates a minimum feasible candidate; a true→false predicate can locate a maximum feasible candidate. Other formulations are possible if you re-derive the invariant.

### Why logarithmic shrinkage matters

If at most roughly half the candidate positions survive each comparison, after k iterations at most about `n / 2^k` remain. Reaching a constant-size region requires k proportional to log₂ n. With a conventional exact search, the worst-case number of probes for n>0 is at most `floor(log₂ n)+1`. Boundary searches have the same logarithmic order, with small rounding differences.

A million candidate positions need roughly twenty probes. That says nothing yet about the cost of evaluating one probe: checking a proposed capacity might scan a million input records. Logarithmically many decisions do not imply logarithmic total runtime.

**Self-check Q1:** Why is “the middle value is too small” a safe reason to remove the entire left portion only under an appropriate ordering guarantee?

**Self-check Q2:** Can binary search apply to an unsorted array of workloads? What, rather than the workloads, would have to be monotonic?

## 3. Search Intervals and Loop Invariants

A **loop invariant** is a statement true before every iteration. It says what has already been established and what the remaining region means. A useful search invariant must be stronger than “left and right are valid”: it must explain why the desired answer has not been discarded.

There are two related interval meanings in this guide:

- **Exact search:** if a match exists and has not been returned, at least one matching position is still inside the candidate interval.
- **Boundary search:** all positions before left are known to be on one side of a threshold, and all positions from right onward are known to be on the other. The unresolved **element positions** occupy `[left,right)`; the answer is a **boundary** that can equal right or n.

The second distinction matters. A first-true boundary may survive at right even when right is not an unresolved element to inspect. Do not dereference an exclusive endpoint to “check the final answer” without a bounds check.

### Closed intervals for exact search

`[left,right]` contains both endpoint indices. Start with left=0 and right=n−1. The interval is empty when left>right, including `[0,-1]` for an empty array. While left<=right, mid is a valid candidate.

After comparing `a[mid]` with target:

| Observation | Eliminated indices | Remaining interval | Why mid is removed |
| --- | --- | --- | --- |
| Equal | Search finishes | Return mid | A valid match is found |
| Less | left through mid | `[mid+1,right]` | Mid itself is too small |
| Greater | mid through right | `[left,mid-1]` | Mid itself is too large |

Use `mid = left + (right-left)/2` under these nonnegative active index bounds. Every non-returning update removes mid and at least one position. The mathematical candidate count `right-left+1` strictly decreases until it is zero.

### Half-open intervals

`[left,right)` includes left and excludes right. Start with left=0 and right=n. Empty means left==right. While left<right, the floor midpoint satisfies left<=mid<right, so it is a valid element index even when right=n.

For **exact search**, a too-small midpoint gives left=mid+1 and a too-large midpoint gives right=mid. The new exclusive endpoint excludes mid; subtracting one again would exclude an additional unchecked position.

For **first-true boundary search**, a true midpoint may be the desired boundary, so right=mid preserves that boundary as a possible final value. A false midpoint cannot be the first true and gives left=mid+1. This uses the same numerical updates as some exact-search branches but a different invariant and return meaning.

| Decision to make | Closed exact search | Half-open exact search | Half-open first-true search |
| --- | --- | --- | --- |
| Initial bounds | 0, n−1 | 0, n | 0, n |
| Continue while | left<=right | left<right | left<right |
| Midpoint | Floor of remaining index range | Floor, always below right | Floor, always below right |
| Too small / false | left=mid+1 | left=mid+1 | left=mid+1 |
| Too large / true | right=mid−1 | right=mid | right=mid |
| Equality | Return any hit | Return any hit | Retain equality if it satisfies predicate |
| Empty/end state | left>right | left==right | left==right is the boundary |
| Missing result | −1 | −1/false by contract | Often n, not −1 |

### Why more than one valid template exists

An algorithm can retain the midpoint as a possible boundary, remove it as a disproved element, or remember a best answer in a separate variable. These choices lead to different loop guards and updates. The variable names alone do not determine a template's correctness. Decide what the interval means, what outside it is known, and how the return value is encoded.

For a half-open interval of length `right-left`, right=mid and left=mid+1 both strictly reduce that nonnegative length. For a closed **candidate-answer** interval used later, left<right and right=mid retain a possible answer at mid; a false midpoint gives left=mid+1. The region is closed despite resembling half-open boundary code. Read the contract and invariant instead of classifying solely by syntax.

**Self-check Q3:** Why is right=n legal in a half-open search but not an index you may read?

**Self-check Q4:** On a two-candidate interval, why can left=mid fail to make progress with a floor midpoint?

## 4. Exact-Match Binary Search

### Start with a correct linear baseline

The following baseline accepts an array in any order and returns the first index equal to target, or −1. Its worst-case time is O(n), auxiliary space O(1), and output scalar. It is often the best choice for one lookup in unsorted input.

```java
static int linearIndex(int[] values, int target) {
    for (int i = 0; i < values.length; i++) {
        if (values[i] == target) return i;
    }
    return -1;
}
```

Sorted order allows stronger elimination. Instead of testing each position, inspect the midpoint, use order to discard a whole side, and repeat on the surviving region. Sorting solely to perform one lookup adds preprocessing and may change the meaning of returned indices; the following searches **require sorted input and do not sort it**.

### Iterative closed-interval implementation

**Contract:** nondecreasing int array, any int target. Return any matching original index, or −1. With duplicates, the returned index need not be first. Preserve input.

```java
static int exactIndex(int[] values, int target) {
    int left = 0;
    int right = values.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (values[mid] == target) return mid;
        if (values[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return -1;
}
```

**Invariant:** if the array contains the target and no match has been returned, a matching index remains within `[left,right]`. Initially all indices are included. If the midpoint value is too small, sorted order proves every index through mid too small; the opposite comparison proves every index from mid onward too large. Thus the updates preserve a possible match. Each update strictly shrinks the interval. At empty termination, no matching index can remain, so −1 is correct. Equality returns a valid index immediately.

For `[2,5,8,12,16,23,38]`, target 23:

| Iteration | Left | Right | Mid | Comparison | Action | New search space |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 6 | 3 | 12<23 | left=4 | `[4,6]` |
| 2 | 4 | 6 | 5 | 23=23 | Return 5 | Finished |

Same array, target 15:

| Iteration | Left | Right | Mid | Comparison | Action | New search space |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 6 | 3 | 12<15 | left=4 | `[4,6]` |
| 2 | 4 | 6 | 5 | 23>15 | right=4 | `[4,4]` |
| 3 | 4 | 4 | 4 | 16>15 | right=3 | `[4,3]`, empty |
| End | 4 | 3 | — | left>right | Return −1 | No candidate |

### A consistent half-open implementation

This method returns a boolean rather than an index, but uses the same exact-match condition. Its active region is `[left,right)` throughout.

```java
static boolean containsHalfOpen(int[] values, int target) {
    int left = 0;
    int right = values.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (values[mid] == target) return true;
        if (values[mid] < target) left = mid + 1;
        else right = mid;
    }
    return false;
}
```

For target 38 in the seven-element example, intervals are `[0,7)` → `[4,7)` → `[6,7)`, with midpoint values 12, 23, 38; return true on the third probe. If a greater midpoint causes right=mid, mid is excluded because right is exclusive. A closed-search update right=mid−1 would remove an extra position in this implementation.

### Recursive implementation without subarray copies

A recursive call receives a smaller closed interval in the **same** backing array. It searches only one half after a mismatch; it does not recursively explore both halves.

```java
static int exactIndexRecursive(int[] values, int target) {
    return exactInRange(values, target, 0, values.length - 1);
}

private static int exactInRange(int[] values, int target, int left, int right) {
    if (left > right) return -1;
    int mid = left + (right - left) / 2;
    if (values[mid] == target) return mid;
    if (values[mid] < target) {
        return exactInRange(values, target, mid + 1, right);
    }
    return exactInRange(values, target, left, mid - 1);
}
```

The contract and elimination proof match the iterative method. A missing search for 15 makes calls on `[0,6]`, `[4,6]`, `[4,4]`, and `[4,3]`; the last returns −1 and the result propagates back. There is one active call per halving level, plus the final empty call when needed. Each frame stores constant state, so worst-case auxiliary space is O(log(n+1)), while time is O(log(n+1)). Java does not guarantee tail-call elimination. Iterative search keeps only one set of boundaries, using O(1) auxiliary space. Copying subarrays at each recursion level would add different costs and is unnecessary.

| Feature | Iterative | Recursive |
| --- | --- | --- |
| Worst-case time | O(log(n+1)) | O(log(n+1)) |
| Peak auxiliary space | O(1) | O(log(n+1)) frames |
| State transition | Assign new boundaries | Pass new boundaries |
| Empty case | Loop does not execute | Base case returns immediately |
| Typical use | Default interview implementation | Demonstrating the recurrence and call-stack reasoning |

### Minimal and extreme cases

| Sorted input and target | Closed-search trace | Result |
| --- | --- | --- |
| `[], 7` | Start left=0, right=−1; no probe | −1 |
| `[5], 5` | mid=0, equality | 0 |
| `[5], 2` | mid=0, right=−1 | −1 |
| `[2,5], 5` | mid=0 → left=1; mid=1, equality | 1 |
| `[2,5], 1` | mid=0 → right=−1 | −1 |
| `[2,5], 9` | mid=0 → left=1; mid=1 → left=2 | −1 |
| `[4,4,4,4,4], 4` | Initial mid=2, equality | 2 for this code; not the first |
| `[MIN,MAX], MAX` | mid=0 → left=1; mid=1, equality | 1 |

MIN/MAX mean `Integer.MIN_VALUE`/`Integer.MAX_VALUE`. Compare values directly; subtracting target from a value can overflow. For a general exact-search contract, validate any hit by bounds and equality rather than insisting that every valid implementation return the same duplicate index.

**Self-check Q5:** Why does exactIndex return a correct match on an all-equal array without guaranteeing its first occurrence?

**Self-check Q6:** Why is recursive search O(log n) auxiliary space even though each frame has only a few variables?

## 5. Boundary Search

An exact search stops when it finds equality. To find the first equality, an equal midpoint must remain a possible answer while you investigate earlier positions. To find the end of an equal range, equality must instead belong to the discarded prefix. Expressing these decisions as threshold boundaries removes much of the ambiguity.

### 5.1 Lower bound: first value greater than or equal to target

**Definition:** `lowerBound(values,target)` returns the smallest index p with `values[p] >= target`, or n if none exists. It is a position in `[0,n]`, not a promise that the target occurs. It also gives an insertion position **before existing equals**.

On a nondecreasing array, the predicate `values[i] >= target` is false→true. Maintain:

```text
[0,left)       [left,right)        [right,n)
 known < t      unresolved         known >= t
                  boundary p is in [left,right]
```

If the midpoint is below target, every position through mid is below it, so left=mid+1. Otherwise mid might be the first qualifying position; right=mid preserves it as a possible **boundary**, and sorted order proves all positions from mid onward qualify.

```java
static int lowerBound(int[] values, int target) {
    int left = 0;
    int right = values.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (values[mid] < target) left = mid + 1;
        else right = mid;
    }
    return left;
}
```

For `[1,3,3,3,8]`, target 3:

| Iteration | Left | Right, exclusive | Mid | Predicate `a[mid]>=3` | Action | Unresolved elements |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 5 | 2 | True | right=2 | `[0,2)` |
| 2 | 0 | 2 | 1 | True | right=1 | `[0,1)` |
| 3 | 0 | 1 | 0 | False | left=1 | `[1,1)`, empty |
| End | 1 | 1 | — | — | Return 1 | Boundary is 1 |

At termination left=right=p. Every earlier element is below target, and every element at or after p is at least target. That proves p is the first such position, or n if there is no qualifying element. The boundary is retained even when the unresolved element interval becomes empty.

### 5.2 Upper bound: first value strictly greater than target

**Definition:** `upperBound(values,target)` returns the smallest p with `values[p] > target`, or n. It is the insertion position **after existing equals**. The prefix invariant is now `values[0..left) <= target`, and the suffix invariant is `values[right..n) > target`.

Equality belongs in the prefix, so it must advance left. Do not implement upper bound by adding 1 to target; the maximum int has no larger int successor.

```java
static int upperBound(int[] values, int target) {
    int left = 0;
    int right = values.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (values[mid] <= target) left = mid + 1;
        else right = mid;
    }
    return left;
}
```

For the same array and target:

| Iteration | Left | Right, exclusive | Mid | Predicate `a[mid]>3` | Action | Unresolved elements |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 5 | 2 | False | left=3 | `[3,5)` |
| 2 | 3 | 5 | 4 | True | right=4 | `[3,4)` |
| 3 | 3 | 4 | 3 | False | left=4 | `[4,4)`, empty |
| End | 4 | 4 | — | — | Return 4 | Boundary is 4 |

Initialization is vacuous because both known regions start empty. Each branch extends one known region using sorted order, preserves the threshold between left and right, and reduces right−left. At termination the known regions meet, proving the result. This is the lower-bound proof with a different predicate, not a separate collection of arbitrary update rules.

| Question | Lower bound | Upper bound |
| --- | --- | --- |
| First qualifying relation | `>= target` | `> target` |
| Known prefix relation | `< target` | `<= target` |
| Equality moves | right toward mid | left past mid |
| Existing equal values | Insert before them | Insert after them |
| No qualifying value | n | n |
| Empty array | 0 | 0 |
| Time / auxiliary space | O(log(n+1)) / O(1) | O(log(n+1)) / O(1) |

### 5.3 First/last occurrences and the entire equal range

Let L=lowerBound(target) and U=upperBound(target). Every occurrence of target lies in `[L,U)`, and every position in that interval equals target. The occurrence count is U−L. If absent, L=U is still a meaningful insertion boundary.

For a **first occurrence**, check L<n and equality at L; otherwise return −1. For a **last occurrence**, inspect U−1 only if it is nonnegative, then verify equality. That equality check matters: the predecessor of an insertion point may be smaller than target.

```java
static int firstOccurrence(int[] values, int target) {
    int position = lowerBound(values, target);
    return position < values.length && values[position] == target ? position : -1;
}

static int lastOccurrence(int[] values, int target) {
    int position = upperBound(values, target) - 1;
    return position >= 0 && values[position] == target ? position : -1;
}

static int[] equalRange(int[] values, int target) {
    return new int[]{lowerBound(values, target), upperBound(values, target)};
}
```

The lower/upper traces above are the complete searches for first/last occurrence of 3: L=1 passes the first-occurrence equality check; U=4 gives U−1=3 and passes the last-occurrence check. Return first=1, last=3, equal range `[1,4)`, count 3. Two logarithmic searches still take O(log(n+1)) time. These wrappers use O(1) auxiliary space; equalRange returns a fixed two-int output.

| Array | Target | L | U | First | Last | Equal range |
| --- | --- | --- | --- | --- | --- | --- |
| `[1,3,3,3,8]` | 0 | 0 | 0 | −1 | −1 | `[0,0)` |
| `[1,3,3,3,8]` | 3 | 1 | 4 | 1 | 3 | `[1,4)` |
| `[1,3,3,3,8]` | 5 | 4 | 4 | −1 | −1 | `[4,4)` |
| `[1,3,3,3,8]` | 8 | 4 | 5 | 4 | 4 | `[4,5)` |
| `[1,3,3,3,8]` | 9 | 5 | 5 | −1 | −1 | `[5,5)` |
| `[]` | 3 | 0 | 0 | −1 | −1 | `[0,0)` |
| `[4,4,4]` | 4 | 0 | 3 | 0 | 2 | `[0,3)` |

For a missing 5 in `[1,3,3,3,8]`, lower-bound intervals are `[0,5)` → `[3,5)` → `[3,4)` → `[4,4)`, with midpoint values 3, 8, 3. Boundary 4 points to 8, so firstOccurrence returns −1. Upper-bound follows the same path; position 3 holds 3, so lastOccurrence also returns −1. No match is fabricated from an insertion point.

### 5.4 First true over an implicit index domain

An array is only one way to evaluate a predicate. The following method searches indices `0..n−1`, with `n>=0`, for the first true value in a false→true sequence. It returns n if all are false, and never calls the predicate at n. A Java `IntPredicate` is simply an object/function whose `test(int)` returns boolean; lambda syntax such as `i -> i >= 3` supplies one.

```java
static int firstTrue(int n, IntPredicate predicate) {
    int left = 0;
    int right = n;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (predicate.test(mid)) right = mid;
        else left = mid + 1;
    }
    return left;
}
```

**Invariant:** indices below left are false, indices at or above right but below n are true, and the first-true boundary is in `[left,right]`. On false, monotonicity proves all positions through mid false; on true, it proves all later positions true. The interval shrinks, and when the regions meet the boundary is known. The absent value n is an answer encoding, not an additional domain element that needs evaluation.

For n=5 and predicate values `F F F T T`:

| Iteration | Left | Right | Mid | Predicate | Action | New interval |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 5 | 2 | False | left=3 | `[3,5)` |
| 2 | 3 | 5 | 4 | True | right=4 | `[3,4)` |
| 3 | 3 | 4 | 3 | True | right=3 | `[3,3)` |
| End | 3 | 3 | — | — | Return 3 | First true |

### 5.5 Last true over a true→false domain

Now assume predicate values have the form `T T ... T F F ... F`. Search for the **first false**, then return the preceding index. Return −1 if no true exists. This changes the predicate orientation relative to firstTrue; saying only “find last true” without stating the truth order is incomplete.

```java
static int lastTrue(int n, IntPredicate predicate) {
    int left = 0;
    int right = n;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (predicate.test(mid)) left = mid + 1;
        else right = mid;
    }
    return left - 1;
}
```

**Invariant:** all indices below left are true, all indices from right through n−1 are false, and the first-false boundary lies in `[left,right]`. True at mid places the last true at or after mid, so keep track of it through the growing known prefix and move left past mid. False at mid places the first false at or before mid, so right=mid. Strict shrinkage ends at the first false; subtracting one returns the last true, or −1 for an empty true prefix.

For n=5 and predicate values `T T T F F`:

| Iteration | Left | Right | Mid | Predicate | Action | New interval |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 5 | 2 | True | left=3 | `[3,5)` |
| 2 | 3 | 5 | 4 | False | right=4 | `[3,4)` |
| 3 | 3 | 4 | 3 | False | right=3 | `[3,3)` |
| End | 3 | 3 | — | — | Return 2 | Last true |

| Domain | firstTrue on false→true data | lastTrue on true→false data |
| --- | --- | --- |
| n=0 | 0; predicate is never called | −1; predicate is never called |
| n=1, false | 1 after one probe | −1 after one probe |
| n=1, true | 0 after one probe | 0 after one probe |
| All false, n>0 | n | −1 |
| All true, n>0 | 0 | n−1 |

Each method makes O(log(n+1)) predicate calls and uses O(1) search state. With predicate time Tcheck and workspace Scheck, total time is O((1+log(n+1))·Tcheck) when charging a possibly nonconstant call cost, and peak auxiliary space is O(1+Scheck). More precisely, add loop overhead and actual call costs; an empty domain makes no calls. A predicate capturing an existing array does not copy it, but a predicate allocating a new array per call adds that workspace and allocation work.

**Self-check Q7:** Why can a lower-bound result equal n, and why must an occurrence search check equality separately?

**Self-check Q8:** How do truth orientation and the absent-answer encoding differ between firstTrue and lastTrue?

## 6. Binary Search in Rotated Sorted Arrays

Rotating a strictly increasing array moves a prefix to the end while preserving each part's internal order. For example, `[3,6,9,11,14,18,22]` can become `[14,18,22,3,6,9,11]`. The result is not globally sorted, so ordinary binary search's “everything left is smaller” argument no longer holds.

### Derive which part can be trusted

A linear scan is a correct O(n)-time, O(1)-space baseline. For a rotation of **distinct** values, the remaining region contains at most one rotation break. Splitting it at mid means at least one side is sorted. If `values[left] <= values[mid]`, the left half `[left,mid]` is sorted; otherwise the right half `[mid,right]` is sorted.

Check equality at mid first. In a sorted left half, the target can remain there only if `values[left] <= target < values[mid]`; if that holds, keep the part before mid, otherwise eliminate the left half. When the right half is sorted, its corresponding test is `values[mid] < target <= values[right]`. Distinctness and the rotation property justify using these value endpoints to select a side. Choosing a side only from `values[mid] < target` is insufficient.

**Contract:** input is empty or a rotation of a strictly increasing int array; return the target index or −1. No duplicate values. An unrotated array is permitted.

```java
static int rotatedIndexDistinct(int[] values, int target) {
    int left = 0;
    int right = values.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (values[mid] == target) return mid;
        if (values[left] <= values[mid]) {
            if (values[left] <= target && target < values[mid]) right = mid - 1;
            else left = mid + 1;
        } else {
            if (values[mid] < target && target <= values[right]) left = mid + 1;
            else right = mid - 1;
        }
    }
    return -1;
}
```

For `[14,18,22,3,6,9,11]`, target 6:

| Iteration | Left | Right | Mid | Established order | Target decision | New space |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 6 | 3 | Right `[3,6]` has values 3 through 11 in order | 3<6<=11; retain right after mid | `[4,6]` |
| 2 | 4 | 6 | 5 | Left `[4,5]` has values 6 through 9 in order | 6<=6<9; retain left before mid | `[4,4]` |
| 3 | 4 | 4 | 4 | Midpoint equals 6 | Return 4 | Finished |

For target 18, the first midpoint 3 has value 3; the sorted right part cannot contain 18, so retain `[0,2]`. Midpoint 1 then matches. For absent target 7, intervals become `[4,6]`, `[4,4]`, then `[5,4]`; return −1. Test `[]`, singleton hit/miss, `[2,1]` with either target, unrotated data, and targets at either side of the break.

**Correctness:** initially any target lies in the whole interval. Before every iteration, if it exists and was not returned, it is still inside `[left,right]`; the interval remains a contiguous segment of the original rotation. Distinctness identifies a sorted side. Its endpoint test proves either that the target must be in that side or that the side can contain no target. After handling mid's equality, removing mid and the excluded side preserves the invariant. Every iteration keeps at most roughly half the positions, so termination takes O(log(n+1)) probes. Empty termination proves absence. Auxiliary space is O(1).

### Optional duplicate-heavy extension

Distinctness is a meaningful precondition. In `[1,0,1,1,1]`, left, mid, and right can all hold 1 even though the left half crosses the break. Treating `a[left] <= a[mid]` as sufficient evidence of sortedness can discard the 0.

An optional duplicate-aware design checks equality with target first; when `a[left]==a[mid]==a[right]` and that value is not the target, those equal endpoints can be excluded safely. However, this may remove only a constant number of positions. All-equal input with an absent target can require O(n) work. The method above deliberately retains the simpler distinct-input contract; do not claim logarithmic worst-case time for all duplicate variants.

Finding a rotation's minimum or searching with duplicates is useful extension work after the required invariant is reliable. The roadmap's optional median partition challenge requires its own cross-array boundary reasoning; it is not implied by understanding this rotated search.

**Self-check Q9:** Why must you identify a sorted half before deciding whether target belongs in it?

**Self-check Q10:** What information is missing when left, mid, and right have equal values in a duplicate-heavy rotation?

## 7. Binary Search on Answer

Instead of asking “which array position contains target?”, ask “what is the smallest capacity that can satisfy this requirement?” Candidate answers are ordered integers. The input may be unsorted because the monotonic property belongs to **feasibility as the candidate changes**.

Start by separating two questions:

1. For a fixed candidate x, can we correctly decide feasibility?
2. Why does that decision have a single transition as x increases?

A fast search around an incorrect or non-monotonic checker remains incorrect. Prove the checker and monotonicity before discussing midpoint updates.

```text
Enumerate candidate answers with a correct checker
                         |
                         v
Prove feasible candidates form one ordered region
                         |
                         v
Specify the desired boundary and valid initial bounds
                         |
                         v
Use a shrinking interval that retains that boundary
                         |
                         v
Prove the final answer and include every check's cost
```

### 7.1 A reusable minimum-feasible search

**Contract:** `0 <= low <= high <= Long.MAX_VALUE`; feasible is false→true on the inclusive domain `[low,high]`. High must be feasible. Return the smallest feasible candidate in that domain. Invalid numerical bounds or an infeasible high throw `IllegalArgumentException`; monotonicity itself remains a caller-proved precondition. This differs from firstTrue over indices: here the function promises an actual feasible answer, not a sentinel for absence.

```java
static long firstFeasibleLong(long low, long high, LongPredicate feasible) {
    if (low < 0 || low > high) throw new IllegalArgumentException("invalid bounds");
    if (!feasible.test(high)) throw new IllegalArgumentException("upper bound infeasible");
    long left = low;
    long right = high;
    while (left < right) {
        long mid = left + (right - left) / 2;
        if (feasible.test(mid)) right = mid;
        else left = mid + 1;
    }
    return left;
}
```

**Invariant:** the minimum feasible answer in the original domain is in the **closed candidate interval** `[left,right]`, and right is feasible. Left need not be infeasible. A feasible mid could be the minimum, so retain it with right=mid. An infeasible mid and every smaller candidate are impossible, so left=mid+1. When left<right, floor mid is below right; either update strictly decreases right−left. At left=right the remaining candidate is feasible and no smaller candidate in the original domain is feasible.

Because the bounds are nonnegative longs, right−left fits long. Even if high is Long.MAX_VALUE, mid+1 is safe: that branch executes only while mid<right. The mathematical range size R=high−low+1 can equal 2^63 when low=0 and high=Long.MAX_VALUE, so **do not materialize R in a signed long just to count candidates**. The search itself does not need that expression.

The high check adds one predicate call. The loop needs O(log R) calls for mathematical cardinality R, with constant work for a singleton domain. Total time is O((1+log R)·Tcheck), plus any preprocessing. Search state is O(1); include the predicate's own workspace separately.

### 7.2 Original example: minimum uniform grant

**Problem:** each department has a nonnegative int need. Give every department the same nonnegative integer grant g. Any remaining need is paid from a shared reserve. Find the smallest g for which total remaining need is at most reserve. Reserve is a nonnegative long. Unused grant is allowed and does not replenish reserve. Empty departments require grant 0. This is an educational interface, independent of a judge problem.

For fixed g, department i needs `max(0, needs[i]-g)` from reserve. Increasing g never increases any deficit; therefore if g is feasible, every larger grant is feasible. Conversely, decreasing an infeasible grant cannot help. This proves false→true monotonicity without assuming the needs array is sorted.

A baseline tries g=0,1,2,... and scans all needs each time: O(nR) worst-case time for R candidate grants, O(1) auxiliary space. The repeated work is checking neighboring candidates even though one failed check can rule out all smaller grants.

**Bounds:** low=0 includes every possible minimum. High=max(needs), or 0 when empty, makes every deficit zero and is always feasible. A tighter lower bound might be derivable, but it is unnecessary for correctness. The high value must be established from the problem, not guessed from a convenient constant.

```java
static boolean reserveCovers(int[] needs, long grant, long reserve) {
    long used = 0;
    for (int need : needs) {
        if (need > grant) {
            long extra = (long) need - grant;
            if (extra > reserve - used) return false;
            used += extra;
        }
    }
    return true;
}

static long minimumUniformGrant(int[] needs, long reserve) {
    if (reserve < 0) throw new IllegalArgumentException("negative reserve");
    long maximum = 0;
    for (int need : needs) maximum = Math.max(maximum, need);
    return firstFeasibleLong(0, maximum, grant -> reserveCovers(needs, grant, reserve));
}
```

Both methods require nonnegative needs; reserveCovers also requires nonnegative grant/reserve. The helper's running invariant is `0 <= used <= reserve`: used equals the processed deficit sum while the candidate is still possible. Comparing extra with `reserve-used` before adding prevents an overflow-prone addition from corrupting the decision. Under the shared array bounds an ordinary long sum would already fit, but this budget-aware comparison is useful when generalizing the domain.

For needs `[2,7,9]`, reserve 6:

```text
grant:       0  1  2  3  4  5  6  7  8  9
deficit:    18 15 12 10  8  6  4  2  1  0
feasible:    F  F  F  F  F  T  T  T  T  T
minimum:                    5
```

The initial upper-bound check confirms grant 9 is feasible. The loop then proceeds:

| Iteration | Left | Right, inclusive | Mid | Total deficit | Feasible? | New candidate interval |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 9 | 4 | 8 | No | `[5,9]` |
| 2 | 5 | 9 | 7 | 2 | Yes | `[5,7]` |
| 3 | 5 | 7 | 6 | 4 | Yes | `[5,6]` |
| 4 | 5 | 6 | 5 | 6 | Yes | `[5,5]` |
| End | 5 | 5 | — | — | Retained candidate is feasible | Return 5 |

The displayed total deficit is mathematical; the checker may stop early once the budget is exceeded. To prove minimality independently, grant 5 uses exactly 6 reserve units, while grant 4 needs 8. Reserve 0 requires grant 9. Reserve at least 18 allows grant 0. Empty needs and all-zero needs both return 0. One need MAX with reserve 0 returns MAX; `[MAX,MAX]` with reserve MAX requires grant 1073741824.

**Full cost:** finding maximum takes O(n). Each feasibility scan costs O(n) worst case, with early exits sometimes cheaper. R=maxNeed+1 possible grants gives O(n·(1+log R)) total time, including preprocessing and the high check, and O(1) auxiliary space. No sorted copy, map, or returned collection is needed. State these variables instead of claiming “O(log n).”

### 7.3 Original example: maximum uniform piece length

**Problem:** nonnegative int lengths describe independent material strips. A strip can produce `floor(length/x)` pieces of positive integer length x; leftover material from different strips cannot be combined. For a positive long requiredPieces, return the largest feasible x, or 0 if no positive length can supply enough pieces. Empty/all-zero lengths return 0. Negative lengths are outside the contract; requiredPieces<=0 is rejected by the search method.

For larger x, every strip supplies no more pieces. Feasibility is **true→false** over positive candidate lengths. A baseline tests every x from 1 through maxLength, scanning the strips each time. Binary search instead retains the unresolved candidates and remembers the greatest feasible one already found.

```java
static boolean canSupplyPieces(int[] lengths, long pieceLength, long requiredPieces) {
    long made = 0;
    for (int length : lengths) {
        long pieces = length / pieceLength;
        if (pieces >= requiredPieces - made) return true;
        made += pieces;
    }
    return false;
}

static long maximumPieceLength(int[] lengths, long requiredPieces) {
    if (requiredPieces <= 0) throw new IllegalArgumentException("nonpositive requirement");
    long maximum = 0;
    for (int length : lengths) maximum = Math.max(maximum, length);
    long left = 1;
    long right = maximum;
    long answer = 0;
    while (left <= right) {
        long mid = left + (right - left) / 2;
        if (canSupplyPieces(lengths, mid, requiredPieces)) {
            answer = mid;
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return answer;
}
```

The checker requires pieceLength>=1 and requiredPieces>=1. Before each iteration, made is the exact processed piece count and is less than requiredPieces. Compare the next contribution against the remaining requirement, returning early once enough pieces exist. This avoids accumulating beyond what the decision needs.

The search uses a **closed unresolved interval** `[left,right]` and separate answer. All discarded candidates below left are feasible; answer is the greatest among those checked/established so far, with 0 representing no positive feasible candidate. Candidates above right are infeasible. A true mid establishes every smaller positive length feasible and allows searching strictly above mid; false establishes mid and larger candidates infeasible. Each update removes mid, so the loop shrinks even for two candidates. At empty termination no unclassified candidate can improve answer.

For lengths `[5,8,10]`, requiredPieces=6:

| Iteration | Left | Right | Mid | Pieces | Feasible? | Action and new interval |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 10 | 5 | 1+1+2=4 | No | right=4; `[1,4]` |
| 2 | 1 | 4 | 2 | 2+4+5=11 | Yes | answer=2, left=3; `[3,4]` |
| 3 | 3 | 4 | 3 | 1+2+3=6 | Yes | answer=3, left=4; `[4,4]` |
| 4 | 4 | 4 | 4 | 1+2+2=5 | No | right=3; empty |
| End | 4 | 3 | — | — | — | Return 3 |

With the same strips and requirement 24, even length 1 supplies only 23 pieces, so return 0. `[0,0]` also has no positive candidate. For `[MAX,MAX]` and requirement 2, return MAX. The use of long boundaries makes left=MAX+1 representable after the last feasible int-sized candidate.

Let M=maxLength. Maximum preprocessing costs O(n); searching positive lengths costs O(n·(1+log(M+1))) overall and O(1) auxiliary space. If M=0, no predicate is called. The checker does not allocate an array of pieces. A much larger numerical requiredPieces changes feasibility, not the number of candidate lengths.

### 7.4 Koko as a conceptual reference

[Koko Eating Bananas](https://leetcode.com/problems/koko-eating-bananas/) is Essential in the roadmap. Use it to practice the same derivation independently: candidate integer speed k, hours for a pile p equal to `ceil(p/k)`, total hours compared with the allowed budget. Increasing speed cannot increase total hours, so “finish within budget” is false→true. With nonempty positive piles and at least one hour available per pile, 1 through maxPile contains a feasible minimum. If a problem variant allows fewer hours than nonempty piles, that feasibility guarantee fails and must be handled explicitly.

For the conceptual example piles `[4,9,10]` and 8 hours, speeds 1, 2, 3, 4 need 23, 12, 9, 7 hours respectively, so the minimum speed is 4. Do not use “uses exactly 8 hours” as the predicate: exact equality can be skipped and does not describe the feasible suffix. Explain the ceiling and sum before deriving the search. Section 9 gives a safe integer ceiling helper; no complete platform solution is supplied here.

### Minimum versus maximum feasible answers

| Choice | Minimum grant example | Maximum piece example |
| --- | --- | --- |
| Predicate orientation | False→true as grant rises | True→false as length rises |
| Desired boundary | First true | Last true |
| Initial guarantee | Upper bound is feasible | Positive range may be entirely infeasible |
| Interval meaning | Closed set containing the answer | Closed unresolved candidates plus saved answer |
| True midpoint | Keep mid as possible minimum | Save mid; investigate larger lengths |
| False midpoint | Remove mid and smaller grants | Remove mid and larger lengths |
| No feasible candidate | Excluded by contract; high check rejects | Return 0 |

Do not transplant an update rule between these searches. Derive it from the meaning of true and the boundary requested.

**Self-check Q11:** Why does knowing one candidate is feasible not prove that binary search is applicable?

**Self-check Q12:** What must you prove about the initial bounds before using firstFeasibleLong, and what costs remain outside its loop?

## 8. Binary Search in Other Ordered Structures

### 8.1 A globally sorted rectangular matrix

Suppose each row is nondecreasing and every row's last value is **at most the next row's first value**. Reading row by row forms a nondecreasing virtual one-dimensional sequence. The educational contract allows duplicates; some platform contracts are stricter.

For row count r and common column count c, virtual index k maps to `row=k/c`, `column=k%c`. The inverse is `(long)row*c+column`. Mapping does not require copying the matrix. Java's `int[][]` can be jagged, so rectangular shape is a real precondition.

```java
static boolean containsGlobalMatrix(int[][] matrix, int target) {
    int rows = matrix.length;
    if (rows == 0) return false;
    int columns = matrix[0].length;
    if (columns == 0) return false;
    long left = 0;
    long right = (long) rows * columns;
    while (left < right) {
        long mid = left + (right - left) / 2;
        int row = (int) (mid / columns);
        int column = (int) (mid % columns);
        int value = matrix[row][column];
        if (value == target) return true;
        if (value < target) left = mid + 1;
        else right = mid;
    }
    return false;
}
```

All rows must be non-null with equal length. Empty outer matrices and zero-column rectangular matrices return false before division. The code does not scan to validate shape or order; adding such validation would affect total cost. The multiplication is promoted **before** it occurs, so a virtual count greater than int range is not accidentally truncated. A valid mid yields row<rows and column<columns before casts to int.

For `[[1,4,7],[9,13,18]]`, target 13:

| Iteration | Left | Right | Mid | Row, column | Value | Action |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 6 | 3 | 1, 0 | 9 | left=4 |
| 2 | 4 | 6 | 5 | 1, 2 | 18 | right=5 |
| 3 | 4 | 5 | 4 | 1, 1 | 13 | Return true |

For absent target 8, midpoint values are 9, 4, 7 and intervals become `[0,3)`, `[2,3)`, `[3,3)`; return false. The exact-search invariant applies to virtual indices because their values are globally ordered. Progress is measured by right−left. With N=r·c cells, time is O(log(N+1)) and auxiliary space O(1), under constant-time row/column access and trusted preconditions.

Being sorted only within rows and columns is **insufficient** for this flattening. `[[1,9],[2,10]]` has sorted rows and columns, but virtual sequence `[1,9,2,10]` is not sorted; the method can miss 9. That is a different matrix-search contract, requiring its own elimination argument rather than this implementation.

### 8.2 Ordered timestamps and predecessor queries

A version query often asks for the most recent record at or before a requested time. In nondecreasing timestamps, that is the last position with `time<=query`, equivalently **upperBound(query)−1**. Exact search alone is inadequate when the query lies between stored times.

**Contract:** sorted long timestamps, possibly repeated; return the index of the last timestamp at most query, or −1. When times tie, the later stored occurrence wins. Query and timestamps may use the full signed long range because only comparisons, not timestamp subtraction, are used.

```java
static int latestAtOrBefore(long[] timestamps, long query) {
    int left = 0;
    int right = timestamps.length;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (timestamps[mid] <= query) left = mid + 1;
        else right = mid;
    }
    return left - 1;
}
```

For `[10,20,20,35]`, query 20: `[0,4)` has mid=2 with time 20, so left=3; `[3,4)` has mid=3 with time 35, so right=3. The upper boundary is 3; return 2. Query 25 follows the same path and also returns 2. Query 9 has no predecessor and returns −1; query 40 returns 3; empty input returns −1 without reading an element.

The invariant is “all positions below left have time<=query; all positions from right onward have time>query.” It initializes vacuously, is preserved by sortedness, and ends at the first-too-late position. Subtracting one selects the latest allowed position. Time is O(log(m+1)) for m versions, auxiliary space O(1), and output one index.

For a per-key version store, first select that key's ordered version list, then apply the predecessor reasoning. Expected hashing lookup plus O(log(m+1)) search is a possible query cost, with storage and insertion costs analyzed separately. Appending out-of-order timestamps breaks the assumption; inserting into an ArrayList can require O(m) shifts. This guide does not implement the roadmap's full Time Based Key-Value Store class.

### 8.3 Predecessor and successor relationships

For an ascending array, let L=lowerBound(query), U=upperBound(query):

| Requested neighbor | Index expression | Missing boundary |
| --- | --- | --- |
| Greatest value strictly below query | L−1 | −1 |
| Greatest value at most query | U−1 | −1 |
| Smallest value at least query | L | n |
| Smallest value strictly above query | U | n |

These are positions, not value sentinels. Check 0<=index<n before reading. For `[2,5,5,11]` and query 5, the four indices are 0, 2, 1, 3 respectively. Weak predecessor and weak successor can deliberately choose opposite ends of a duplicate run.

### 8.4 Implicit and virtual spaces

A candidate need not be stored in memory. firstTrue might query a deterministic formula, while firstFeasibleLong tests a capacity. The matrix method accesses a virtual sequence by index. Each still needs a finite or otherwise justified initial range, a safe decision rule, and an honest evaluation cost.

If an implicit candidate computation scans n entries, a search with logarithmically many candidate probes can cost O(n log R). If the candidate source lacks cheap random access, retrieving the midpoint can dominate. Infinite/unbounded searches, floating-point precision searches, interpolation search, and advanced data structures are outside the mandatory scope here; do not assume a finite-array proof covers them automatically.

**Self-check Q13:** Why do sorted rows and sorted columns not necessarily justify flattened binary search?

**Self-check Q14:** Why is a timestamp predecessor query naturally an upper boundary followed by subtraction rather than an exact-match lookup?

## 9. Java 21 Implementation Details

### 9.1 Indices, answer values, and midpoint arithmetic

Array indices and an array's exclusive endpoint fit int. Numerical candidate answers may require long, even when every input element is int. Use long **before** a sum, product, or subtraction whose mathematical result exceeds int range. A long destination does not change the evaluation type of an int expression; see [the existing arithmetic discussion](./Two_Pointers/Two_Pointers_Essential_Concepts.md#113-arithmetic-must-be-wide-before-the-operation).

| Expression or choice | Risk | Reasoned choice |
| --- | --- | --- |
| `(left+right)/2` with large ints | Addition can overflow | `left+(right-left)/2` under valid nonnegative bounds |
| `long mid = (left+right)/2` with int operands | Overflow already happened | Widen operands first, or use the safe difference form |
| Difference midpoint over arbitrary signed long endpoints | `right-left` itself may overflow | Restrict the candidate domain as in firstFeasibleLong, or derive arithmetic for the full signed domain separately |
| `int total` for many pile hours | Sum can exceed int | Accumulate in long or stop safely at the budget |
| `long count = rows*columns` with int dimensions | Product can overflow as int | `(long)rows*columns` |
| `upperBound(target+1)` via lower bound | MAX+1 wraps to MIN | Compare with `> target` directly |
| Floor midpoint with an update `left=mid` | Two candidates can stall | Remove mid when disproved, or derive a compatible upper-midpoint invariant |

For left=1,500,000,000 and right=2,000,000,000, the overflowing sum midpoint is −397,483,648; the difference form gives 1,750,000,000. The latter is valid because 0<=left<=right<=MAX, so the difference and final sum fit.

If a different, correctly proved maximum-boundary algorithm retains mid with left=mid, it generally needs an **upper midpoint** when left<right. Under nonnegative long bounds, compute distance=right−left and use `left + distance/2 + distance%2`. This avoids both a floor-midpoint stall and the possible overflow in `distance+1`. This guide's maximumPieceLength instead saves an answer and removes mid, so it uses the ordinary floor midpoint consistently.

### 9.2 Integer ceiling division

For nonnegative amount and positive unit, ordinary division floors the mathematical quotient. To count units needed, add one exactly when there is a remainder:

```java
static long ceilDivNonnegative(long amount, long unit) {
    if (amount < 0 || unit <= 0) throw new IllegalArgumentException("invalid division domain");
    return amount / unit + (amount % unit == 0 ? 0 : 1);
}
```

The helper takes O(1) time/space and supports the entire nonnegative long amount domain. The quotient-plus-remainder adjustment cannot overflow: a quotient of Long.MAX_VALUE requires unit=1 and then the remainder is zero. `ceilDivNonnegative(0,3)=0`, `(10,3)=4`, `(9,3)=3`, and `(Long.MAX_VALUE,2)=4611686018427387904`.

The familiar `(amount+unit-1)/unit` is valid only if the addition is representable. With int amount=MAX and unit=MAX, the numerator wraps to −3, producing 0 instead of 1. Casting that already-overflowed numerator afterward does not repair it. Floating-point conversion can also lose precision: `9007199254740993L` is not represented exactly as double, so using a double quotient and Math.ceil can corrupt an otherwise exact integer calculation.

Java 21 also provides `Math.ceilDiv(long,long)`, rounding toward positive infinity. It handles this positive domain directly. For arbitrary signed inputs, its rounding differs from ordinary truncating division; the special Long.MIN_VALUE/−1 overflow is outside this helper's domain. Do not assume this positive-quantity explanation validates every signed ceiling formula.

### 9.3 Overflow-safe feasibility accumulation

Derive a numerical range before choosing a type. With at most 100,000 int-sized nonnegative terms, a sum may reach roughly 2.15×10^14: safe in long, unsafe in int. Two MAX-sized piles alone need 4,294,967,294 hours at speed 1; an int sum wraps to −2 and can turn “too slow” into a false feasible result.

If only a budget comparison is needed, preserve `0<=used<=budget` and test `contribution > budget-used` before adding. For an “at least required” count, preserve made<required and test `contribution >= required-made` before adding. These are the invariants used in Section 7. The individual contribution must also be computed safely. Early exit improves some calls but does not remove the O(n) worst-case check cost.

### 9.4 Arrays.binarySearch contracts

The Java 21 [Arrays API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) requires the searched array/range to be sorted in the order used for comparison. On unsorted data, the result is unspecified by that contract. It does not sort or validate the entire input first.

- A **nonnegative result** is a matching index. Test `result>=0`, not `result>0`; index 0 is a valid hit.
- A **negative result** encodes `-(insertionPoint)-1`. Decode on a miss with `-(result+1)`. The insertion point is where the absent key belongs in that sorted range, including its exclusive end.
- With duplicates, there is **no guarantee which matching index is returned**. This is not a lower-bound or upper-bound API.
- Range overloads use `[fromIndex,toIndex)`. Returned indices and insertion points are positions in the original array, not offsets from fromIndex.
- Object-array comparator overloads require the **same ordering** used to sort. A null comparator means natural ordering. Primitive int arrays have no comparator overload.

```java
static int[] librarySearchReport() {
    int[] sorted = {2, 4, 6};
    int found = Arrays.binarySearch(sorted, 4);
    int missing = Arrays.binarySearch(sorted, 5);
    int insertion = -(missing + 1);
    int[] duplicates = {1, 3, 3, 3, 8};
    int rangeMissing = Arrays.binarySearch(duplicates, 1, 4, 4);
    return new int[]{found, missing, insertion, rangeMissing, -(rangeMissing + 1)};
}
```

The report is `[1,-3,2,-5,4]`. The range contains indices 1,2,3 only; missing 4 belongs at absolute position 4, its exclusive endpoint. For `[2,4,6]`, missing 1 returns −1/insertion 0, missing 9 returns −4/insertion 3, and an empty array returns −1/insertion 0. Do not decode a successful result as an insertion point.

### 9.5 Comparators and indexed collections

Sorting and searching must agree on what “before” means. The following object array is in descending order; the comparator expresses that order for both hit and miss:

```java
static int[] comparatorSearchReport() {
    Integer[] descending = {9, 5, 1};
    Comparator<Integer> order = Comparator.reverseOrder();
    int hit = Arrays.binarySearch(descending, 5, order);
    int miss = Arrays.binarySearch(descending, 7, order);
    return new int[]{hit, miss, -(miss + 1)};
}
```

The result is `[1,-2,1]`: 7 belongs between 9 and 5 at index 1. For custom numerical records use `Comparator.comparingInt`/`Integer.compare`, not subtraction that may overflow. Null elements require an explicit ordering policy if supported at all; the examples contain none. For strings, account for character work in comparisons: with maximum compared length L, O(log n) comparisons can require O(L log n) time. See [Java String costs](./Java_String.md#8-complexity-expectations-for-dsa).

Arrays and ArrayList support constant-time positional access, so ordinary indexed binary search takes logarithmic time for constant-cost comparisons. Repeated `LinkedList.get(mid)` walks links: O(log n) probes can have O(n log n) worst-case traversal work. An absent target near the middle of a large list can keep many midpoint accesses far from either end.

The [Collections.binarySearch API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html) distinguishes random-access lists from large non-RandomAccess lists. Its iterator-based path for the latter uses O(n) link traversals and O(log n) comparisons, rather than repeated independent get calls. Neither case establishes logarithmic total traversal time for a linked representation. Copying once to an array costs O(n) time/space and may make repeated queries worthwhile; it is not free preprocessing. Reuse the [list complexity reference](./Java_List_ArrayList_LinkedList.md#arraylist-complexity).

**Self-check Q15:** Under what bounds is the difference midpoint safe, and why does changing only the destination type to long not fix an overflowing int expression?

**Self-check Q16:** What can you infer from a negative Arrays.binarySearch result, and what can you not infer from a duplicate hit?

## 10. Search-Space Invariants and Correctness

An implementation is reviewable when each boundary update has an explicit reason. Use five steps: initialization, preservation, progress, termination, and final interpretation. Saying “the range halves” addresses runtime and termination, but not whether it halves toward the correct answer.

### A reusable derivation process

1. **Name the answer.** An arbitrary matching index, first index with value>=target, latest allowed timestamp, or minimum feasible capacity are different contracts.
2. **Define the ordered domain.** State its endpoints and whether they are elements, boundary positions, or candidate answer values.
3. **Prove the deciding property.** Sorted array, sorted half, false→true feasibility, or true→false feasibility; do not infer monotonicity from a few samples.
4. **Write the invariant before code.** Explain every known prefix/suffix, possible answer interval, and saved answer.
5. **Derive each branch.** State exactly what the midpoint observation rules out and whether mid itself remains possible.
6. **Choose a decreasing measure.** Interval length or width must strictly shrink on every non-returning path.
7. **Interpret the terminal state.** An empty exact interval means absence; a meeting boundary may equal n; a minimum-feasible singleton is an actual feasible answer.

### Pattern-specific proof obligations

| Pattern | Initialization | Preservation | Progress and final result |
| --- | --- | --- | --- |
| Closed exact search | Entire array is a candidate | Order removes only positions unable to equal target | Mid is removed on mismatch; empty interval means −1 |
| Lower/upper bound | Known prefix/suffix initially empty | Predicate truth expands the correct known region | right−left decreases; meeting point is the unique transition |
| First/last occurrence | Use a proven lower/upper boundary | Bounds/equality distinguish a real occurrence from an insertion neighbor | Fixed extra checks preserve logarithmic search cost |
| Rotated distinct search | A possible target is in the whole rotation | Sorted-half test proves which side can contain it | Mid is removed after mismatch; a half-sized region remains |
| Minimum feasible answer | The domain includes an optimum; right is feasible | False discards a prefix; true retains mid as possible optimum | Width decreases; singleton is feasible and minimal |
| Maximum feasible answer | Full positive domain unresolved; answer=0 | True saves a feasible candidate; false removes an impossible suffix | Mid is removed; no unresolved candidate can improve answer |
| Timestamp predecessor | Sorted times give a true prefix for `time<=query` | Keep the first-too-late boundary | Return the preceding allowed position or −1 |

### Why candidate retention changes updates

In exact search, a midpoint unequal to target is disproved and must be removed. In lower bound, a midpoint value equal to target could be the first occurrence and therefore remains a possible **boundary** while earlier elements are examined. In firstFeasibleLong, a feasible mid could be the minimum feasible **candidate value** and remains inside the closed interval. These distinctions explain why `right=mid-1` and `right=mid` can both be correct in different algorithms.

For progress with a floor midpoint, if left<right then mid<right. Setting right=mid reduces right; setting left=mid+1 increases left. If instead a branch sets left=mid, a two-value region can remain unchanged. A correct template must handle that branch by a different update or a justified upper midpoint, not by hoping a later iteration will move.

### Correctness of a feasibility checker is a separate proof

The outer invariant assumes the checker's result is accurate. For reserveCovers, processed deficits are summed until they exceed a budget; every omitted deficit is nonnegative, so early false is safe. For canSupplyPieces, contributions are independent strip counts; after enough pieces exist, unprocessed strips cannot revoke them, so early true is safe. Both monotonicity arguments follow from termwise inequalities as the candidate changes.

If a checker uses a greedy decision, approximation, lossy floating-point arithmetic, or overflowed sums, its correctness must be established separately. Binary search does not turn an unreliable checker into a reliable optimizer. The original examples deliberately use transparent additive checks so the two proofs remain visible.

**Self-check Q17:** Why is strict interval shrinkage insufficient by itself to prove the returned answer correct?

**Self-check Q18:** In a minimum-feasible closed interval, why must a true midpoint be retained while a false one is removed?

## 11. Detailed Complexity Analysis

Let n be array/input length, q the number of queries, R the **mathematical number of candidate answers**, Tcheck the worst-case time of one check, Scheck its peak workspace, and Ssort(n) the chosen sort's workspace. These variables describe different resources; R is not necessarily n.

### Derive the logarithmic term

For a search with at most half the candidates remaining after a probe, k probes reduce n candidates to roughly n/2^k. Solving n/2^k<=1 gives k>=log₂ n. More formally, an exact-search worst-case recurrence is `T(n)=T(floor(n/2))+O(1)` with constant-size base cases. It yields O(log(n+1)) time. Boundary searches may use a slightly different rounded subproblem size but still reduce geometrically.

Recursive exact search has one branch per level, not two simultaneously explored branches. Its depth is logarithmic and each frame has constant state, giving O(log(n+1)) auxiliary space. Copying a half-array at each level instead would copy n/2+n/4+... elements, adding O(n) work and storage effects; passing index bounds avoids that cost.

### Count the whole operation

| Operation under stated assumptions | Time | Auxiliary space | Qualification |
| --- | --- | --- | --- |
| Linear search | O(n) worst case; O(1) on immediate hit | O(1) | No sortedness required |
| Iterative exact search | O(log(n+1)) worst case | O(1) | Sorted input, constant-time access/comparison |
| Recursive exact search | O(log(n+1)) worst case | O(log(n+1)) | Call frames included; no copied subarrays |
| Lower/upper bound; occurrence wrappers | O(log(n+1)) | O(1) | A fixed number of logarithmic searches |
| Distinct rotated search | O(log(n+1)) | O(1) | Valid rotation and distinctness |
| Optional duplicate-aware rotated search | O(n) worst case | O(1) for an iterative design | Ambiguity can defeat halving |
| Scalar-domain binary search | O(1+log R) checks | O(1) search state | Constant-cost decision model only |
| Binary search with a check | O((1+log R)·Tcheck), plus preprocessing | O(1+Scheck) | Sequential checks do not keep every workspace live |
| O(n) scanning predicate | O(n·(1+log R)) including a linear preprocessing pass | O(1) if checker scans with scalars | Early exits do not improve the worst-case bound automatically |
| Globally sorted matrix, N cells | O(log(N+1)) | O(1) | No flattening copy; trusted order/shape |
| Timestamp predecessor, m versions | O(log(m+1)) | O(1) | Array/ArrayList-style indexed access |
| Copy and sort, then one search | O(n log(n+1)) | O(n+Ssort(n)) | Copy preserves input but not original-index identity by itself |
| Sort once, then q searches | O(n log(n+1)+q log(n+1)) | Ssort(n), plus any retained copy/index | Data and ordering remain stable |
| Expensive comparison cost C | O(C log(n+1)) as an upper bound | Include comparator workspace | O(log n) comparisons are not O(log n) time when C grows |

The output of these searches is scalar or fixed-size except where a demonstration returns a small array; output space is O(1). If a real application returns every matching element, add Ω(k) time and O(k) output for k matches. Lower/upper bounds locate the range efficiently but do not eliminate the cost of emitting it.

### Examples of complete bounds

- Searching one target in an already sorted million-entry int array needs O(log n) time and O(1) auxiliary space iteratively.
- Copying and sorting an unsorted array before that same search costs O(n log n) time overall and includes the copy plus sorting workspace. A direct O(n) scan can be better for one query.
- Checking n departments for each of O(log R) grant candidates costs O(n log R), with an additional O(n) maximum-finding pass already absorbed when R is nontrivial.
- A predicate taking O(n log n) because it sorts on every call yields O(n log n·(1+log R)), not O(n log R). Consider moving candidate-independent work outside, if correctness permits.
- Materializing a virtual matrix as an N-element array adds O(N) time/space. It can dominate the subsequent logarithmic search.

General Java sorting workspace is not automatically O(1) just because the array is mutable. Keep Ssort(n) explicit or justify a bound for the actual implementation. Refer to [Java library operation costs](./Complexity/Complexity_Analysis_and_Foundations.md#15-java-standard-library-operation-complexities) and [the interview analysis process](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) rather than repeating general Big-O theory here.

**Self-check Q19:** If candidate feasibility costs O(n), why does halving the candidate range produce O(n log R) rather than O(log n)?

**Self-check Q20:** What changes in the time and space analysis when an unsorted read-only array must be copied and sorted before searching?

## 12. Binary Search versus Alternative Techniques

Choose the technique from the query, existing order, access model, mutation rules, and number of future queries. Binary search is efficient when its prerequisites already hold or are worth establishing.

| Comparison | First option | Second option | Decision to explain |
| --- | --- | --- | --- |
| Linear versus binary search | Linear works on arbitrary order, O(n) time, O(1) space | Binary uses order, O(log n) time with cheap access | Is safe elimination available without preprocessing? |
| Exact versus boundary search | Stop at any equality; missing result often −1 | Locate a threshold, possibly n; equality may remain a candidate | Is the requested answer any occurrence, first/last, or an insertion position? |
| Positions versus answers | Search a stored/virtual ordered sequence | Search numerical candidates with a feasibility checker | What is ordered, what does one probe cost, and how are bounds proved? |
| Binary search versus HashMap lookup | Ordered data supports boundaries and neighbors without a new hash index | HashMap gives expected O(1) exact-key retrieval after construction, using O(n) storage | Is equality enough, or is an ordered predecessor/successor required? |
| Sort plus search versus direct scan | Sorting costs O(n log n), changes order, may use workspace/copies | One scan costs O(n), preserves positions without sorting | Are repeated queries valuable enough to amortize preprocessing? |
| Repeated binary searches versus two pointers | q independent sorted lookups cost O(q log n) | Two ordered sequences can sometimes be processed together in O(n+q) | Can query order be exploited without changing the required answers? |
| Boundary search versus monotonic stack | A monotone predicate locates one threshold in an ordered domain | A stack can resolve next-greater positions in an unsorted stream in O(n) | Is there a searchable suffix order, or deferred work for many positions? |

### Exact lookup is not the same as ordered lookup

A map from ID to record is natural for “does this exact ID exist?” A sorted sequence is natural for “what is the latest ID/time at most q?” A HashMap has no sorted-neighbor guarantee. TreeMap/TreeSet offer ordered navigation, but building or maintaining those structures has its own costs; they are not required to implement the static boundary drills here. Reuse [the existing map reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) when the application needs dynamic ordered updates.

If one unsorted array will receive many exact lookups, constructing a hash index can cost expected O(n) time and O(n) storage, followed by expected O(1) lookups. If preserving sorted order and supporting range queries matter, a sorted copy plus binary search may be a better fit. Include key hashing/comparison costs and the semantics of duplicates in the choice.

### Sorting can change the question

Sorting `[40,10,30]` yields `[10,30,40]`. The sorted index of 10 is 0; its original index is 1. A binary search can be correct on the sorted data and still produce the wrong answer if the caller requested original positions. Sorting records with original indices retains provenance at an added storage cost. If order encodes timestamps, adjacency, distance, or a subsequence, sorting may change the mathematical problem altogether.

For a sorted pair-sum question, scanning two endpoints can examine candidates in O(n), while performing one binary search per first index costs O(n log n). For an unsorted nearest-greater-to-the-right question, ordinary binary search over each suffix is invalid because the suffix need not be sorted and the nearest-answer predicate need not be monotonic by position. [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md) and [Stack technique selection](./Stack/Stack_Essential_Concepts.md#15-choosing-between-stacks-and-alternatives) supply the appropriate comparison reasoning.

**Self-check Q21:** Why can an O(n) scan beat sorting plus O(log n) search for one query, even though binary search itself is faster?

**Self-check Q22:** Which capability does sorted boundary search provide that a plain HashMap exact lookup does not?

## 13. Common Mistakes and Edge Cases

Use a small counterexample to challenge every assumption. The examples below describe deliberately incorrect choices, not alternative templates to copy.

| Incorrect assumption or update | Small witness | Why it fails | Correct reasoning |
| --- | --- | --- | --- |
| Sum midpoint always fits | int left=1500000000, right=2000000000 | Their sum overflows before division | Use a difference midpoint under a domain where the difference fits |
| Long destination repairs int overflow | `long x = MAX + MAX` using int operands | The int sum wraps to −2 first | Promote an operand before adding |
| Closed exact search can advance with left=mid | `[1,2]`, target 2 | Floor mid can remain 0 forever | Remove a disproved midpoint with mid+1 |
| Half-open exact search can use right=mid−1 | `[1,2]`, target 1 | First mid=1 is too large; right=0 skips unchecked index 0 | An exclusive right=mid already removes mid |
| Half-open search can blindly use left<=right | Empty array, left=right=0 | It can attempt an out-of-range access | The guard must match the interval and updates |
| Equality gives the first occurrence | `[4,4,4,4,4]`, target 4 | exactIndex returns midpoint 2 | Retain equality while searching the left boundary |
| A lower bound is automatically a match | `[2,5]`, target 3 | Boundary 1 points to 5 | Check bounds and equality before returning an occurrence |
| Upper bound can search target+1 in int | `[MAX]`, target MAX | Increment wraps to MIN | Use a strict comparison directly |
| A midpoint comparison is enough without sortedness | `[3,1,2]`, target 3 | The correct target can be discarded on the left | Establish order or use a scan/other technique |
| A rotated array is globally sorted | `[4,5,1,2,3]`, target 4 | Ordinary exact search can discard the target | Prove which half is sorted and test that half's endpoints |
| Equal endpoints identify the sorted half | `[1,0,1,1,1]`, target 0 | The left part crosses the break despite equal endpoints | Distinctness is essential to the simple logarithmic method |
| Any yes/no condition supports binary search | x=1..4, predicate `x*x==4`: F,T,F,F | A false→true boundary search can skip the only true candidate | Prove a single transition, not isolated satisfactions |
| A convenient numerical high is feasible | Need `[9]`, reserve 0, guessed high=8 | An unguarded candidate loop can return an infeasible value | Derive a guaranteed high; firstFeasibleLong explicitly rejects this one |
| Exact resource equality is the optimization predicate | Needs `[2,7,9]`, “deficit exactly 8” | Only grant 4 satisfies it; larger feasible grants become false | Use the inequality that defines acceptable solutions |
| Ceiling numerator cannot overflow | int amount=MAX, unit=MAX | Wrapped numerator gives 0 rather than 1 | Divide plus remainder adjustment, or valid Math.ceilDiv usage |
| Feasibility total fits int because each term does | Two MAX-sized pile hours at speed 1 | Total wraps to −2 | Use a proven long bound or budget-aware accumulation |
| O(log R) probes means O(log n) total time | n-entry checker at every probe | The repeated scans still execute | Multiply probe count by check cost and include preprocessing |
| Library hit means first duplicate | An array with repeated equal keys | The API makes no such guarantee | Use lower/upper bounds when positions within a run matter |
| Every successful library index is positive | `[2]`, key 2 | Valid index 0 is rejected by `result>0` | Test `result>=0` |

### An executable missing-progress counterexample

This method intentionally has the closed-search left update wrong. On `[1,2]` and target 2, left=0, right=1, mid=0 repeats unchanged. A counter increment or continued reevaluation cannot repair the missing progress.

```java
static int buggyNoProgress(int[] values, int target) {
    int left = 0;
    int right = values.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (values[mid] == target) return mid;
        if (values[mid] < target) left = mid; // Deliberate defect.
        else right = mid - 1;
    }
    return -1;
}
```

Trace it by hand or run it only in an isolated process with a timeout. Compare its invariant and decreasing measure with exactIndex. The correction follows from having disproved mid itself; it is not an arbitrary “plus one” to memorize. Another common stall comes from using right=mid in a closed exact search on `[2]`, target 1: right never moves. That same assignment is correct in lowerBound because its interval meaning and loop guard differ.

### Edge-case matrix for your own implementations

| Case family | What it tests | Expected property |
| --- | --- | --- |
| Empty array | Guard before any access | Exact −1, lower/upper 0, first/last occurrence −1 |
| One element, hit and both miss directions | Final candidate handling | Equality works; either miss removes the only candidate |
| Two elements, both hits and a missing in-between target | Floor midpoint and progress | Every non-returning branch changes a boundary |
| All equal | Strictness and duplicate-run endpoints | lower equal=0, upper equal=n |
| Target below/above every value | Boundary sentinels | Insertion boundary 0 or n, never read as an invalid index |
| Negative values and MIN/MAX | Direct comparisons and arithmetic | No subtraction comparator or target+1 overflow |
| Unrotated, one-position rotation, pivot-adjacent hit | Sorted-half classification | Same target-preservation invariant holds |
| All-false/all-true predicate | Missing versus endpoint answer | firstTrue n/0; lastTrue −1/n−1 |
| Feasible minimum at low/high | Candidate-bound inclusivity | The search does not exclude a legitimate endpoint |
| Infeasible maximum-piece request | Optional absence in that contract | Return 0; do not invent a positive feasible length |
| Large terms with a small resource budget | Early rejection and accumulation | Overflow cannot turn excessive use into feasible use |
| Maximum-length sorted input | Probe count and recursion depth | Halving continues; no copied subarrays are needed |

For small test arrays, compare exact hits and boundaries with a linear oracle. Generate nondecreasing arrays with duplicates, rotate distinct arrays at every pivot, and test targets before, inside, between, and after stored values. For answer search, enumerate small candidate domains to verify both feasibility and minimality/maximality. Tests expose mistakes; the invariant and monotonicity proof explain correctness for the full contract.

**Self-check Q23:** Why does replacing one boundary update without revisiting the interval convention often introduce another bug?

**Self-check Q24:** How can an overflowing feasibility sum invalidate the outer search even when the outer midpoint and updates are correct?

## 14. Pattern Recognition Practice

Read these scenarios before opening the answer key. For each, decide whether binary search applies, name the domain, state the predicate/truth orientation and desired boundary, and give an invariant. If another technique fits better, explain why. These are conceptual diagnostics, not a second exercise workbook.

| ID | Scenario | Question to resolve |
| --- | --- | --- |
| R1 | A sorted catalogue has repeated prices; find the first price at least a budget | Is equality part of the kept suffix or discarded prefix? |
| R2 | A per-device history has ordered timestamps; return the last event no later than a query | What should an exact miss mean? |
| R3 | A proposed capacity passes a correct O(n) checker; passing at c guarantees passing at every larger capacity | What bound must be established before searching for the minimum? |
| R4 | An unsorted ID array receives one equality query; original index must be returned | Is sorting justified, and what index would it produce? |
| R5 | For every position in an unsorted sequence, find the next position with a higher value | Is the predicate monotonic by suffix index? |
| R6 | A rectangular matrix is ordered within rows and columns, but successive row ranges overlap | Does row-major flattening preserve order? |
| R7 | Two sorted lists contain q query thresholds and n values; find each query's lower boundary | Should you use independent searches or exploit both orders? |
| R8 | A service returns true only when candidate x uses exactly the available resource, with false on either side | Does a single acceptable candidate imply monotonicity? |

A useful recognition checklist is: **ordered domain → safe elimination property → requested boundary → valid bounds → shrinking invariant → complete cost**. If any link is missing, pause the implementation and resolve it. “The statement asks for a minimum” is a clue to investigate feasibility, not sufficient evidence for binary search.

**Self-check Q25:** Why is the word “minimum” alone insufficient to justify binary search on answer?

**Self-check Q26:** When might multiple sorted queries be processed more efficiently by two advancing positions than by q independent binary searches?

## 15. Conceptual Interview Questions and Answer Key

Try explaining a fresh problem aloud using this sequence: contract, simple baseline, ordered domain, monotonicity/safe elimination, bounds, invariant, implementation, counterexample tests, and full complexity. A listener should be able to tell why every discarded candidate is impossible or unnecessary.

Useful interviewer follow-ups include:

1. What does the interval represent, and which endpoint may be a non-element boundary?
2. Why is the answer still present after this branch? Why is mid retained or removed?
3. What happens with no match, all equal values, or only two candidates?
4. Can you derive the opposite strictness without looking at a template?
5. What proves the checker is both correct and monotonic?
6. Why does your initial range contain an answer? What if none exists?
7. Which expression could overflow before assignment to long?
8. How would indexed access, sorting, string comparisons, or predicate workspace change the cost?

<details>
<summary>Self-assessment answer key — open after answering Q1–Q26</summary>

| ID | Concise reasoning check |
| --- | --- |
| Q1 | Sorted order extends one midpoint comparison to all positions on the discarded side; arbitrary order does not. |
| Q2 | Yes, if candidate answers have a monotonic feasibility predicate. The workload values themselves need not be sorted. |
| Q3 | Right marks the first excluded position. The guard ensures mid<right, so no access at n occurs. |
| Q4 | With two candidates the floor midpoint is the lower one; assigning left=mid can reproduce the same state. |
| Q5 | Equality proves that position matches; the algorithm stops without establishing that all earlier positions differ. |
| Q6 | Logarithmically many constant-size frames coexist before returns unwind; one frame's size is not the whole stack. |
| Q7 | No value may qualify, leaving the boundary at n. A qualifying >=value need not equal the target. |
| Q8 | firstTrue expects false→true and returns n on absence; lastTrue expects true→false and returns −1 on absence. |
| Q9 | Rotation breaks global ordering. A sorted-half endpoint test is the evidence that a whole side may be retained/discarded. |
| Q10 | Equal values can hide the rotation break, so endpoint comparisons do not establish which side is ordered. |
| Q11 | One feasible point says nothing about intervening or larger candidates; a single-transition proof is still required. |
| Q12 | The domain must include the minimum and high must be feasible. Add bound-building/preprocessing and all predicate work. |
| Q13 | Row ranges can overlap, placing a large previous-row value before a smaller next-row value in flattened order. |
| Q14 | The desired event may precede an absent query time. The first timestamp>query minus one returns the latest allowed occurrence. |
| Q15 | Ordinary nonnegative bounds ensure right−left and the final midpoint fit. Int operands still evaluate as int before assignment. |
| Q16 | A miss encodes an insertion position in the searched range. A duplicate hit identifies some matching position, not necessarily an endpoint. |
| Q17 | An algorithm can shrink toward a wrong region. Preservation must additionally show that the desired answer is not discarded. |
| Q18 | True mid may be the optimum; false mid and its smaller candidates cannot be, by monotonicity. |
| Q19 | There are O(log R) calls and each can perform O(n) work; their costs multiply, with preprocessing added. |
| Q20 | Include O(n) copying, O(n log n) sorting, sorting workspace, and changed index meaning before the logarithmic search. |
| Q21 | Establishing sorted order costs more than a single linear traversal; it becomes useful for additional operations/queries. |
| Q22 | Threshold boundaries and ordered predecessor/successor positions; plain hashing supplies exact-key association without sorted-neighbor guarantees. |
| Q23 | Updates express what stays possible under a particular guard and interval. Mixing them can skip candidates or stop shrinking. |
| Q24 | The outer proof trusts predicate results. An overflowed total can classify an infeasible candidate as feasible and discard the real optimum. |
| Q25 | Minima can arise from non-monotonic conditions or unsearchable domains. A correct monotone predicate and valid bounds are necessary. |
| Q26 | If both sequences are ordered and all results are needed, a monotone scan can use O(n+q) work instead of O(q log n). |

</details>

<details>
<summary>Recognition checks — open after attempting R1–R8</summary>

| ID | Direction and invariant to justify |
| --- | --- |
| R1 | Lower bound over catalogue indices: prices before left are below the budget; the suffix from right is at least it. |
| R2 | Upper boundary on `time>query`, then subtract one; check missing predecessor before reading. |
| R3 | Minimum-feasible answer search after proving a feasible high and a domain containing the optimum. Total cost includes O(n) per check. |
| R4 | A direct scan preserves original positions in O(n). Sorting adds work and changes indices unless provenance is retained. |
| R5 | The unsorted suffix is not a binary-search domain for this query. A monotonic stack can resolve pending occurrences in linear total work. |
| R6 | The flattening implementation does not apply. Establish a different matrix-specific elimination rule or use a valid baseline. |
| R7 | Independent lower bounds cost O(q log n); ordered queries may permit an O(n+q) scan. Compare actual sizes and query needs. |
| R8 | Equality alone can give false→true→false. Use a proven monotonic acceptable-condition inequality if the problem supports one, otherwise choose another method. |

</details>

## 16. Key Takeaways

Binary search is safe elimination over an ordered domain. The order may come from sorted values, a sorted half of a rotation, or monotone answers to a feasibility question. State the exact result first: any match, first/last occurrence, insertion boundary, predecessor, minimum feasible value, or maximum feasible value.

Choose one invariant and interval convention per implementation. Derive updates from whether mid is disproved or remains a possible answer. Prove strict progress on two candidates and explain the terminal state, including n/−1/0 sentinels where the contract uses them. Empty and all-equal inputs are useful tests of the reasoning, not exceptional afterthoughts.

In Java, make arithmetic safe where it is evaluated. Check the range of midpoint differences, ceiling calculations, totals, and products. Use Arrays.binarySearch for its documented exact-hit/insertion-encoding contract; derive lower/upper bounds when duplicate endpoints matter. Count data access, comparisons, preprocessing, predicate work, recursive frames, and copied data in the full complexity.

An interview explanation should make four facts clear: **what remains possible, why the next elimination is safe, why progress cannot stop, and what the final boundary means**. Rebuilding that argument for a changed strictness or unfamiliar feasibility checker is the skill to retain.

## 17. References and Next Steps

### Existing repository references

- [Binary Search roadmap](./DSA_Interview_Roadmap.md#4-binary-search), [Phase 2 objectives](./DSA_Interview_Roadmap.md#phase-2-search-windows-and-pointer-structures), and [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) define scope and practice expectations.
- [Array copying](./Java_Array.md#copying-arrays) and [Java Arrays](./Java_Array.md) cover input preservation, indexing, matrices, and library overloads.
- [Java String comparison](./Java_String.md#2-comparing-and-searching) and [String costs](./Java_String.md#8-complexity-expectations-for-dsa) explain nonconstant character work.
- [List complexity](./Java_List_ArrayList_LinkedList.md#arraylist-complexity) explains constant-time ArrayList access and the different linked representation.
- [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md) develops safe elimination; [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) supports choosing equality lookup and accounting for preprocessing.
- [Space complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity), and [Java library operation costs](./Complexity/Complexity_Analysis_and_Foundations.md#15-java-standard-library-operation-complexities) supply the underlying cost framework.

### Official Java 21 references

The API behavior in this guide was checked using Context7 against official Java 21 documentation:

- [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html): sorted-range requirements, comparator overloads, duplicate-hit guarantees, and insertion-point encoding.
- [Collections](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html): binary search on random-access versus linked representations.
- [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html) and [RandomAccess](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/RandomAccess.html): positional access and the access-model distinction.
- [Math](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html): integer ceiling division and its arithmetic contract.
- [IntPredicate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/IntPredicate.html) and [LongPredicate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/LongPredicate.html): the callback interfaces used by the boundary helpers.

### Roadmap problems, in their existing order

The local roadmap supplies the following titles, difficulty labels, and priorities. **E/A/C are local learning priorities; Easy/Medium/Hard are platform difficulties.** The canonical links provide the exact judge contracts. Use the explanations above to derive a solution independently rather than copying a nearby method without adapting its assumptions.

| Priority | Problem | Difficulty | Learning focus |
| --- | --- | --- | --- |
| E — Essential | [Binary Search](https://leetcode.com/problems/binary-search/) | Easy | Candidate interval and exact-hit reasoning |
| A — Additional | [Search a 2D Matrix](https://leetcode.com/problems/search-a-2d-matrix/) | Medium | Required global order and virtual indices |
| E — Essential | [Search in Rotated Sorted Array](https://leetcode.com/problems/search-in-rotated-sorted-array/) | Medium | Distinct-input sorted-half elimination |
| E — Essential | [Koko Eating Bananas](https://leetcode.com/problems/koko-eating-bananas/) | Medium | Monotone feasibility, ceiling division, and total checker cost |
| A — Additional | [Time Based Key-Value Store](https://leetcode.com/problems/time-based-key-value-store/) | Medium | Per-key ordered versions and predecessor search |
| C — Optional advanced challenge | [Median of Two Sorted Arrays](https://leetcode.com/problems/median-of-two-sorted-arrays/) | Hard | Separate cross-array partition reasoning; optional |

The optional Hard and duplicate-heavy rotation extension do not block ordinary Binary Search readiness. Additional platform classifications do not remove the need to understand the relevant essential boundary concepts.

### Practice and assessment sequence

Close the guide and implement exact search, lower bound, and upper bound from a blank editor. Trace empty arrays, singleton/two-element ranges, all-equal values, and targets before/after the stored range. Verify each method against a linear oracle on small sorted inputs. Then construct a separate first-feasible checker, prove monotonicity and bounds, and account for its cost before writing the outer loop.

Proceed through the roadmap's assigned problems, keeping attempt dates, I/H/S assistance results, lessons, and review dates in their **existing roadmap rows**. Consider cold reviews after roughly 1, 3, 7, and 14 days, adjusted to retention, following the [revision policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition). Keep notes and prior code closed during a cold solve. S includes substantial solution review or an unfinished attempt; H means a conceptual hint was needed; a fully tested and explained independent solve can be I.

For R, derive an unfamiliar boundary or answer search across separate sessions and satisfy the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category). Explain the monotonic property, valid bounds, invariant, termination, missing-answer behavior, duplicates, overflow, and predicate cost. Record assessment evidence once at the roadmap's Binary Search readiness paragraph; reading or compiling this document is not a readiness assessment.

### Validation and limits

All **19 Java blocks**, containing **24 methods** including the private recursive helper and the deliberately broken demonstration, compiled together using Temurin JDK 21 with `javac --release 21 -Xlint:all -Werror`. A temporary Java harness outside the repository passed **437,085 checks**. It compared exact/occurrence/boundary results with linear scans, tested every transition in small monotone predicate domains, and checked the full nonnegative long answer domain without allocating it.

Coverage included 792 exhaustive nondecreasing arrays over values −2..2 of lengths 0–7, 500 seeded arbitrary-int sorted arrays, maximum-length arrays, 1,365 small nonnegative input arrays with exhaustive resource budgets/requirements, 821 generated distinct-rotation cases plus extreme-value variations, 36 small matrix shapes, and seeded full-range timestamp cases. Integer ceiling division was checked against BigInteger arithmetic on 1,500 seeded cases. Additional checks covered duplicate runs, missing answers, infeasible bounds, long endpoints, int overflow witnesses, range/comparator library overloads, input preservation, and the original grant/piece/Koko numerical examples. The deliberate missing-progress loop was reproduced in a separate process and terminated after a timeout.

An independent document audit verified **15 tabulated exact/boundary result cases**, all **31 probe rows across ten dry-run tables**, **50 local Markdown links and heading anchors**, all 26 self-check answer IDs, and the six curated entries against the local roadmap. The final code blocks match the compiled source. All **19 pre-existing files inside DSA_Guide** retained their original bytes; only this document was added, with validation artifacts outside the repository.

The examples rely on their stated ordering, distinctness, rectangular-shape, and monotonicity preconditions. Full input validation is not hidden inside logarithmic cost claims. Optional duplicate-aware rotation and median-partition algorithms are discussed as extensions rather than implemented; sorting workspace remains explicit where implementation-dependent. No judge submission or learner-readiness result is claimed by these checks.
