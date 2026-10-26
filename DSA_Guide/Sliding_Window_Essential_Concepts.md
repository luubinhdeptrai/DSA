# Sliding Window — Essential Concepts (Java 21)

## Table of Contents

- [1. Introduction and Learning Objectives](#1-introduction-and-learning-objectives)
- [2. Sliding Window Fundamentals](#2-sliding-window-fundamentals)
- [3. Window Boundaries and Invariants](#3-window-boundaries-and-invariants)
- [4. Fixed-Size Rolling Windows](#4-fixed-size-rolling-windows)
- [5. Fixed-Size Frequency Matching](#5-fixed-size-frequency-matching)
- [6. Variable Windows and Longest Valid Ranges](#6-variable-windows-and-longest-valid-ranges)
- [7. Shortest Valid and Covering Windows](#7-shortest-valid-and-covering-windows)
- [8. Frequency Counting and Character Models](#8-frequency-counting-and-character-models)
- [9. Amortized Complexity and Complete Costs](#9-amortized-complexity-and-complete-costs)
- [10. When Sliding Window Is Inappropriate](#10-when-sliding-window-is-inappropriate)
- [11. Prefix Sum and HashMap Alternatives](#11-prefix-sum-and-hashmap-alternatives)
- [12. Monotonic Deque: Optional Advanced](#12-monotonic-deque-optional-advanced)
- [13. Java 21 Implementation Decisions](#13-java-21-implementation-decisions)
- [14. Comparing Techniques](#14-comparing-techniques)
- [15. Common Mistakes and Edge Cases](#15-common-mistakes-and-edge-cases)
- [16. Pattern Recognition Practice and Cheat Sheet](#16-pattern-recognition-practice-and-cheat-sheet)
- [17. Conceptual Assessment and Answer Key](#17-conceptual-assessment-and-answer-key)
- [18. Key Takeaways and Interview Explanation](#18-key-takeaways-and-interview-explanation)
- [19. References, Validation, and Next Steps](#19-references-validation-and-next-steps)

## 1. Introduction and Learning Objectives

A sliding window is a contiguous part of an input whose boundaries move while you maintain useful information about its contents. Instead of rebuilding a sum or a frequency table for every overlapping range, update the state when an element enters or leaves.

The optimization has two separate obligations: **maintain the state correctly**, and **justify which candidate ranges need to be considered**. Fast updates alone do not justify skipping ranges. This distinction becomes especially important for variable windows and negative-number sum problems.

The [roadmap's Sliding Window topic](./DSA_Interview_Roadmap.md#5-sliding-window) is the scope authority. Its prerequisites are Two Pointers, counting maps, and amortized scans. Your earlier Binary Search work supplies a useful habit: explain every elimination. Window algorithms use a different mechanism, however; their moving endpoints typically advance through the input rather than halve a candidate interval.

After studying this guide, you should be able to:

1. Describe an active window, its boundaries, and its maintained state.
2. Derive fixed-size rolling sums, averages, and frequency matching.
3. Distinguish longest-valid and shortest-valid search strategies.
4. Prove when a prefix can be removed without losing a better answer.
5. Maintain multiplicities, distinct counts, and coverage requirements.
6. Count total endpoint movements and include state-maintenance costs.
7. Reject an unsuitable sum window and explain a prefix-based alternative.
8. Implement safe Java 21 loops with an explicit character model.
9. Explain the optional monotonic deque using expiry and dominance.

### 1.1 Reuse earlier material

| Existing reference | Reuse here | New window reasoning |
| --- | --- | --- |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Independent size variables, auxiliary space, expected and amortized costs | Count both endpoint travel and update/validation work |
| [Java Arrays](./Java_Array.md) and [Java Strings](./Java_String.md) | Indexing, copies, half-open ranges, UTF-16 | Represent candidates using indices without repeated allocation |
| [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) | Counts versus membership, hash assumptions | Undo outgoing contributions and remove exhausted keys |
| [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Histograms and prefix sums | Maintain a histogram for a changing interval |
| [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md) | Justified movements and progress | Endpoints enclose one contiguous input region |
| [Stack](./Stack/Stack_Essential_Concepts.md) | Dominance and aggregate push/pop counting | A deque also removes expired candidates from the front |
| [Binary Search](./Binary_Search_Essential_Concepts.md) | Preconditions, invariants, and monotonicity | Windows need a safe extension/removal rule, not an assumed binary-search transition |

### 1.2 Core scope and optional extensions

Fixed-size sums/counts, distinct-character windows, duplicate restrictions, endpoint invariants, amortized analysis, and counterexamples are core. Shortest-threshold reasoning develops the opposite update order. Shortest multiset coverage and the monotonic deque are taught later as advanced extensions; their named platform problems remain optional C problems.

The roadmap's platform priorities are preserved here. E/A/C is the roadmap's priority; Easy/Medium/Hard is the platform's difficulty.

| Priority | Problem and official link | Difficulty | Underlying idea |
| --- | --- | --- | --- |
| E | [Best Time to Buy and Sell Stock](https://leetcode.com/problems/best-time-to-buy-and-sell-stock/) | Easy | Earlier minimum / two-position scan |
| E | [Longest Substring Without Repeating Characters](https://leetcode.com/problems/longest-substring-without-repeating-characters/) | Medium | Longest valid duplicate-restricted range |
| E | [Permutation in String](https://leetcode.com/problems/permutation-in-string/) | Medium | Fixed-size frequency distribution |
| A | [Longest Repeating Character Replacement](https://leetcode.com/problems/longest-repeating-character-replacement/) | Medium | Replacement budget and frequencies |
| C | [Minimum Window Substring](https://leetcode.com/problems/minimum-window-substring/) | Hard | Shortest range covering repeated requirements |
| C | [Sliding Window Maximum](https://leetcode.com/problems/sliding-window-maximum/) | Hard | Unexpired, undominated deque candidates |

This is a concepts guide with original teaching implementations, not a platform answer bank. Keep detailed attempts in the existing roadmap problem Logs. T means explain theory; I means implement core drills from a blank editor; P means independent Essential practice; R requires unfamiliar transfer under the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category). Reading this file does not earn a gate.

### 1.3 Shared contracts for the Java examples

All inputs are non-null and read-only. Arrays and strings have length at most **100,000**; input values can be any signed int unless a method explicitly requires nonnegative values. Arrays need not be sorted. Duplicates are allowed. No concurrent mutation occurs. Arithmetic is ordinary Java integer arithmetic, so each method's bounds must establish safety.

All implementations use **half-open active windows `[left,right)`**, where right is the boundary just after the last included item. A `for` loop starting right at 1 adds the item at `right-1`. This makes the length `right-left` and allows right to equal input length without dereferencing it.

Every Java block below is a class-member block. Put them together in a class named `WindowExamples` with `import java.util.*;`. The snippets require only standard Java 21. Helpers are shared rather than redefined.

```java
public record Window(int from, int to) {
    public int length() {
        return to - from;
    }
}
```

A successful Window is `[from,to)`. When a method promises an empty optimum, it returns `Window(0,0)`. Methods searching for a nonempty feasible range return `Window(-1,-1)` if none exists; inspect from before using the range. The record does not validate arbitrary externally constructed ranges. Longest/shortest teaching methods break equal-length ties by **earliest starting position**.

Complexity excludes input and required returned output from auxiliary storage, but includes copies, maps, deque state, and call frames. Empty input takes constant setup work even when a conventional table abbreviates a cost to O(n). Hash-based linear bounds are **expected** under suitable hashing and bounded key-operation costs; they are not unconditional worst-case promises.

**Self-check Q1:** What extra proof does a variable window need beyond “the sum/count is updated correctly”?

## 2. Sliding Window Fundamentals

### 2.1 Contiguity is the defining constraint

A subarray or substring uses consecutive positions in their original order. In `[2,1,5,1,3,2]`, positions 1 through 3 form `[1,5,1]`. Choosing positions 0, 2, and 5 gives `[2,5,2]`, which is a subsequence, not a contiguous window.

Sorting usually changes which values were neighbors. If the question concerns the original contiguous input, sorting to make a pointer rule convenient can change the problem.

A window need not store its elements in a new collection. Left and right identify them in the original array; sum/count state is additional information about that range.

### 2.2 A fixed window moves by reusing overlap

For k=3:

```text
index:    0  1  2  3  4  5
values:   2  1  5  1  3  2

[0,3):   [2  1  5]          sum 8
[1,4):      [1  5  1]       sum 7
[2,5):         [5  1  3]    sum 9
[3,6):            [1  3  2] sum 6

8 - outgoing 2 + incoming 1 = 7
7 - outgoing 1 + incoming 3 = 9
9 - outgoing 5 + incoming 2 = 6
```

Adjacent windows share k-1 items. Re-adding those items is repeated work. Updating one outgoing and one incoming contribution avoids it. Averages share the same numerator: divide each current sum by k, using floating-point division if a fractional result is required.

### 2.3 Expansion and shrinking are separate actions

For a variable range:

```text
Before expansion:       [left ........ right)
Add input[right]:       [left .............. right+1)

Before shrinking:       [left .............. right)
Remove input[left]:          [left+1 ....... right)
```

Expansion changes both the boundary and the state. Shrinking must undo the outgoing item **before its identity is lost**, then advance left. A decrement to left would reintroduce old work; the standard linear algorithms derive why left never needs to move backward.

Sliding Window is a specialization of Two Pointers with maintained interval state. A read/write compaction algorithm has two indices but its write index represents output length, not a window start. A running minimum may solve a two-position task without maintaining every item between the positions. The operation required by the problem determines the invariant.

### 2.4 Start from the baseline

For a fixed length k, scanning each of n-k+1 candidate windows costs `O((n-k+1)k)` when `1 <= k <= n`. For variable ranges, there are `n(n+1)/2` nonempty intervals. Enumerating them with a rolling inner sum takes O(n²); recomputing every interval sum from scratch can take O(n³).

Do not apply a window because the statement says “substring.” Ask which repeated work can be reused and which candidates can safely be skipped. Fixed-size movement is justified by enumerating every legal start. Variable-size movement needs a property of validity or objective quality.

**Self-check Q2:** Why can sorting invalidate a contiguous-range question even if it preserves all values?

## 3. Window Boundaries and Invariants

### 3.1 Half-open and inclusive conventions

| Convention | Elements | Length | Empty representation | Last included element |
| --- | --- | --- | --- | --- |
| Half-open | `[left,right)` | `right-left` | `left==right` | `right-1` when nonempty |
| Inclusive | `[left,right]` | `right-left+1` | Often `left>right` | `right` |

Both work. Our code uses half-open ranges throughout. If an inclusive-loop explanation adds `input[r]`, its equivalent exclusive end is r+1; do not mix its length formula with a half-open state.

Valid half-open bounds satisfy `0 <= left <= right <= n`. Boundaries include n; element indices do not. For example, `[2,5)` contains positions 2,3,4 and has length 3. A returned `Window(2,5)` maps directly to `s.substring(2,5)` for a UTF-16-unit method.

### 3.2 Three layers of an invariant

An invariant states what is true at a **specified point** of every iteration. “The window is valid” is incomplete if adding an incoming item temporarily makes it invalid.

1. **Boundary invariant:** what positions are included?
2. **State invariant:** does sum/count/deque state describe those positions exactly, or a stated projection of them?
3. **Selection invariant:** what has been proved about earlier candidates and the saved best?

For longest-at-most-K, counts match the current interval even immediately after expansion. Validity may temporarily fail. After the repair loop, counts still match, the range is valid, and shorter left endpoints already rejected cannot produce a better valid ending at this right boundary.

For shortest-threshold, the state is exact after each boundary operation. The inner loop intentionally visits valid windows, records them, and then shrinks until validity is lost. It would be incorrect to demand that its final window stay valid.

### 3.3 Initialization, preservation, and completion

Start from an empty window with zero sum/counts. This initializes the state without inventing a first element. On expansion, add exactly the incoming contribution. On shrink, subtract/decrement exactly the outgoing contribution, removing a map key when its live count reaches zero if size means distinctness.

Then connect the state to the answer:

- A fixed window is recorded only after reaching exactly k items.
- A longest-valid window is recorded **after** restoring validity.
- A shortest-valid window is recorded **while it is valid**, before removing the next item.

Prove termination separately. Right increases at most n times; left increases at most n times; every successful inner iteration advances left or removes a deque entry that cannot return. A loop that merely recomputes validity without changing the relevant state can run forever.

### 3.4 Why bad updates are not harmless

| Incorrect operation | Concrete witness | Violated fact and correction |
| --- | --- | --- |
| Move left but leave its value in the sum | `[2,1,5,1]`, k=3: adding the last 1 gives 9 instead of next-window sum 7 | Sum must describe the new interval; remove outgoing 2 |
| Increment distinct on every incoming occurrence | `[4,4]` with K=1 | There is one kind, not two; increment a separate distinct counter only on a zero-to-one transition |
| Remove a map key when one of several copies leaves | Window `[4,4,7]` loses its first 4 | A 4 remains; decrement rather than delete unless count becomes zero |
| Record the expanded longest window before repair | `[1,2,3]` with K=2 | Length 3 is invalid; record only after repair |
| Shrink a shortest valid window before saving it | `[5]`, threshold 5 | The only candidate is lost; record before removal |

A boundary proof is about **all valid inputs**. A trace helps reveal errors but cannot replace the assumption that makes the movement safe.

**Self-check Q3:** At what exact point is validity guaranteed in a longest-window loop?
**Self-check Q4:** Why can a shortest-window loop deliberately finish an iteration with an invalid window?

## 4. Fixed-Size Rolling Windows

### 4.1 Pattern A: all length-k sums

**Problem:** given an arbitrary int array and positive k, return the sum of each length-k contiguous range, in increasing start order. If k exceeds the input length, return an empty long array. k<=0 is rejected. Negative values and duplicates are valid: the method enumerates every fixed-size range, so it needs no sum-monotonicity assumption.

The baseline recomputes k additions for every start. The repeated work is the overlap. Maintain one long sum, add the next value, and remove a value if the range would otherwise exceed k.

```java
public static long[] fixedWindowSums(int[] values, int k) {
    if (k <= 0) throw new IllegalArgumentException("k must be positive");
    if (k > values.length) return new long[0];

    long[] result = new long[values.length - k + 1];
    long sum = 0;
    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        if (right > k) sum -= values[right - k - 1];
        if (right >= k) result[right - k] = sum;
    }
    return result;
}
```

After trimming at exclusive end right, the represented interval is `[max(0,right-k),right)`. Before the first full window, it is a partial prefix and produces no output.

### 4.2 Detailed dry run

Input `[2,1,5,1,3,2]`, k=3. The table shows state **after** trimming.

| Step | Left | Right exclusive | Added | Removed | Window state | Emitted sums |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 1 | 2 | — | `[2]`, sum=2, partial | `[]` |
| 2 | 0 | 2 | 1 | — | `[2,1]`, sum=3, partial | `[]` |
| 3 | 0 | 3 | 5 | — | `[2,1,5]`, sum=8 | `[8]` |
| 4 | 1 | 4 | 1 | 2 | `[1,5,1]`, sum=7 | `[8,7]` |
| 5 | 2 | 5 | 3 | 1 | `[5,1,3]`, sum=9 | `[8,7,9]` |
| 6 | 3 | 6 | 2 | 5 | `[1,3,2]`, sum=6 | `[8,7,9,6]` |

Averages are `[8/3.0,7/3.0,3.0,2.0]`. Dividing the long sum by an int without casting performs integer division and loses the fractional part.

### 4.3 Correctness

**Initialization:** sum=0 represents the empty prefix. **Expansion:** adding values[right-1] includes exactly the new last element. **Preservation after removal:** if right>k, the old leftmost element is values[right-k-1]; subtracting it leaves exactly the last k elements. Otherwise the state is the entire partial prefix.

**Answer timing:** at right>=k, result[right-k] receives exactly the sum for start right-k. **No skipped candidate:** right takes each boundary k,k+1,...,n, producing every legal start 0 through n-k once. **Termination:** the finite right loop ends at n. The complete output is therefore correct.

Time is O(n) when k<=n, with O(1) auxiliary state and O(n-k+1) required output. The immediate k>n exit is O(1). A single rolling maximum sum could use a scalar result, but must initialize from an actual full window rather than zero if all sums can be negative.

### 4.4 Averages and edge cases

This wrapper illustrates a simple output conversion:

```java
public static double[] fixedWindowAverages(int[] values, int k) {
    long[] sums = fixedWindowSums(values, k);
    double[] averages = new double[sums.length];
    for (int i = 0; i < sums.length; i++) {
        averages[i] = sums[i] / (double) k;
    }
    return averages;
}
```

Time is O(n) for ordinary valid windows, but this wrapper has **O(number of windows) auxiliary storage** for the intermediate sums array, in addition to its required output. A direct rolling-average implementation can avoid that intermediate array and use O(1) auxiliary state. Double results are approximate numerical values; do not use exact floating-point equality for fractional test results.

| Input; k | Sums |
| --- | --- |
| `[]; 1` | `[]` |
| `[7]; 1` | `[7]` |
| `[2,1]; 3` | `[]` |
| `[-4,-2,-7]; 2` | `[-6,-9]` |
| `[5,5,5]; 2` | `[10,10]` |
| `[2,1,5]; 3` | `[8]` |
| `[MAX,MAX]; 2` | `[4294967294]` |

Under the shared bounds, at most 100,000 int contributions fit safely in long. Merely declaring an int sum's eventual destination as long would not fix earlier overflow. A fixed sum window is unsuitable for maximum queries because removing the old maximum does not identify the next maximum; [Section 12](#12-monotonic-deque-optional-advanced) supplies a different state.

**Self-check Q5:** Why do negative values not invalidate this fixed-size sum algorithm?
**Self-check Q6:** Why is the averages wrapper's auxiliary space larger than the sums method's?

## 5. Fixed-Size Frequency Matching

### 5.1 Pattern C: count matching inventories

**Original teaching problem:** count the length-m substrings of text whose character multiset equals pattern's multiset. Both strings contain only lowercase English letters `a..z`. Repeated requirements count separately. Empty pattern matches at each of n+1 boundaries; a nonempty pattern longer than text has zero matches. These conventions are explicit rather than borrowed from a platform problem.

Permutation/anagram matching ignores order **inside one candidate window**, but the candidate itself must still be contiguous. A set loses multiplicity: `"aab"` and `"abb"` use the same distinct letters but have different inventories.

The baseline counts each length-m window from scratch, costing O((n-m+1)m) for ordinary m, plus histogram initialization. One improvement updates incoming/outgoing counts and compares two 26-element arrays after every move: O(m+26n), which is O(n+m) for this fixed alphabet. For a variable-size alphabet Σ, repeated full comparisons can instead cost O(nΣ).

To avoid a full comparison at every step, maintain:

- `difference[c] = countInWindow[c] - countInPattern[c]`;
- `nonZero` = number of buckets whose difference is not zero.

A match has exactly m items and nonZero=0. Updating one bucket can change this summary in constant time: subtract its old nonzero contribution, change the bucket, and add its new contribution.

```java
private static int changeDifference(
        int[] difference, int bucket, int delta, int nonZero) {
    if (difference[bucket] != 0) nonZero--;
    difference[bucket] += delta;
    if (difference[bucket] != 0) nonZero++;
    return nonZero;
}

public static int countAnagramWindows(String text, String pattern) {
    int n = text.length(), m = pattern.length();
    if (m == 0) return n + 1;
    if (m > n) return 0;

    int[] difference = new int[26];
    int nonZero = 0;
    for (int i = 0; i < m; i++) {
        nonZero = changeDifference(
                difference, pattern.charAt(i) - 'a', -1, nonZero);
    }

    int matches = 0;
    for (int right = 1; right <= n; right++) {
        nonZero = changeDifference(
                difference, text.charAt(right - 1) - 'a', 1, nonZero);
        if (right > m) {
            nonZero = changeDifference(
                    difference, text.charAt(right - m - 1) - 'a',
                    -1, nonZero);
        }
        if (right >= m && nonZero == 0) matches++;
    }
    return matches;
}
```

The helper is private: its bucket must be valid, delta is +1 or -1, and nonZero must describe the supplied difference array before the call. The lowercase precondition is not validated by an extra scan. Applying this code to uppercase letters or emoji violates its contract.

### 5.2 Frequency dry run

Text `"cabaabbac"`, pattern `"aab"`. Required counts are a:2,b:1. Counts below describe the trimmed active window; unlisted counts are zero.

| Right exclusive | Added | Removed | Active text | Window counts | nonZero | Match count |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | c | — | c, partial | c:1 | 3 | 0 |
| 2 | a | — | ca, partial | a:1,c:1 | 3 | 0 |
| 3 | b | — | cab | a:1,b:1,c:1 | 2 | 0 |
| 4 | a | c | aba | a:2,b:1 | 0 | 1 |
| 5 | a | a | baa | a:2,b:1 | 0 | 2 |
| 6 | b | b | aab | a:2,b:1 | 0 | 3 |
| 7 | b | a | abb | a:1,b:2 | 2 | 3 |
| 8 | a | a | bba | a:1,b:2 | 2 | 3 |
| 9 | c | b | bac | a:1,b:1,c:1 | 2 | 3 |

There can be a nonzero difference during the temporary add-before-remove state even when both adjacent full windows match. Only test after trimming to exactly m.

### 5.3 Correctness, cost, and limits

**Initialization:** negative pattern counts represent empty-window minus pattern counts; the helper tracks the exact number of nonzero buckets. **Expansion/removal:** changing the entering or leaving character changes precisely its bucket, preserving the difference invariant. The fixed-window length invariant is the one from Section 4 with m replacing k.

**Answer timing:** only a full m-length range can match. Zero difference in every bucket is equivalent to equal multiplicities, so a counted window is exactly an anagram window. Every legal start is visited once; none is skipped. Right's finite traversal proves termination.

Time is O(n+m) under the fixed-alphabet model, auxiliary space is O(26)=O(1), and output is a scalar count. Early empty-pattern/too-long-pattern returns take constant time. A generalized map version needs storage for participating keys and expected hashing costs. Frequencies alone are unsuitable for finding a literal ordered pattern: `"ab"` and `"ba"` have equal counts but different order.

Edge examples: `("aaaa","aa") -> 3`; `("abb","aab") -> 0`; `("","a") -> 0`; `("ab","") -> 3`; `("","") -> 1`. For existence instead of counting, a first match can stop the scan; derive that platform variant independently.

**Self-check Q7:** Why does equal distinct-character membership not imply an anagram match?
**Self-check Q8:** Which two facts must hold when incrementing the match count?

## 6. Variable Windows and Longest Valid Ranges

### 6.1 Derive the shrinking rule from the condition

For a longest-valid problem, ask whether **removing elements preserves validity**. At-most-K distinct values, no duplicates, and at-most-L copies of any character have this property: a subwindow of a valid window is valid. Equivalently, once a range is invalid, extending it to the right cannot fix that violation under these specific constraints.

That equivalence justifies a forward-only left boundary. If an earlier start has too many distinct values at the current right end, it cannot become useful at a future right end merely by adding more items. Discard it permanently.

The strategy is:

1. Add the incoming item and advance the exclusive right boundary.
2. While the current range violates the condition, remove the leftmost item and advance left.
3. Record the repaired valid range if it improves the best length.

This is often called expand-then-repair. It is a reasoning pattern, not a universal template: [Section 10](#10-when-sliding-window-is-inappropriate) shows conditions for which removal is not justified.

### 6.2 Pattern B: longest range with at most K distinct values

**Problem:** return the longest range of an arbitrary int array containing at most k distinct values. k>=0; negative k is rejected. Empty input or k=0 returns (0,0). Any nonempty array with k>=1 has a nonempty answer. Tie: earliest start.

A baseline fixes each start, expands an endpoint, and builds counts, costing up to O(n²) expected time. Its repeated work is reconstructing overlapping histograms and revisiting starts already shown to be invalid. Keep one live histogram and move each endpoint only forward.

```java
private static void removeIntCount(Map<Integer, Integer> counts, int value) {
    // Precondition: value has a positive count.
    int remaining = counts.get(value) - 1;
    if (remaining == 0) counts.remove(value);
    else counts.put(value, remaining);
}

public static Window longestAtMostKDistinct(int[] values, int k) {
    if (k < 0) throw new IllegalArgumentException("k must be nonnegative");
    Map<Integer, Integer> counts = new HashMap<>();
    int left = 0;
    Window best = new Window(0, 0);

    for (int right = 1; right <= values.length; right++) {
        int incoming = values[right - 1];
        counts.put(incoming, counts.getOrDefault(incoming, 0) + 1);
        while (counts.size() > k) {
            removeIntCount(counts, values[left]);
            left++;
        }
        if (right - left > best.length()) {
            best = new Window(left, right);
        }
    }
    return best;
}
```

Map keys represent exactly the distinct live values because zero counts are removed. The state invariant is exact even while the inner loop is repairing validity.

### 6.3 Detailed distinct-window dry run

Input `[4,1,4,2,2,1,1]`, k=2. The best column uses half-open coordinates.

| Right | Added | Left before | Shrinking actions | Window after repair; counts | Best |
| --- | --- | --- | --- | --- | --- |
| 1 | 4 | 0 | None | `[0,1)`; 4:1 | `[0,1)`, length 1 |
| 2 | 1 | 0 | None | `[0,2)`; 4:1,1:1 | `[0,2)`, length 2 |
| 3 | 4 | 0 | None | `[0,3)`; 4:2,1:1 | `[0,3)`, length 3 |
| 4 | 2 | 0 | Remove 4; still 3 kinds. Remove 1; now 2 kinds | `[2,4)`; 4:1,2:1 | `[0,3)` |
| 5 | 2 | 2 | None | `[2,5)`; 4:1,2:2 | `[0,3)`; earlier tie |
| 6 | 1 | 2 | Remove 4; now 2 kinds | `[3,6)`; 2:2,1:1 | `[0,3)`; earlier tie |
| 7 | 1 | 3 | None | `[3,7)`; 2:2,1:2 | `[3,7)`, length 4 |

Using `if` instead of `while` at right=4 would remove only one 4, leaving three kinds. Recording that state would use an invalid window.

### 6.4 Correctness and cost

**Initialization:** empty counts represent [0,0); best is the allowed empty range. **Expansion:** the new value's count is incremented, preserving exactness. **Shrinking:** removing the actual leftmost value preserves counts and makes left advance.

**Validity:** removing a prefix cannot increase the number of distinct values. The repair must eventually reach a valid range, even the empty range for k=0. **No optimal candidate skipped:** any start discarded while invalid is also invalid for all later ends, because extension cannot remove an existing kind. At a fixed right boundary, the retained left is the smallest start not already ruled out; its valid window is at least as long as any other valid range ending there. Previously valid discarded windows cannot improve on an earlier, longer-or-equal saved candidate when revisited as shorter suffixes.

**Answer timing and ties:** update after repair; strict length improvement keeps the earlier optimum. **Termination:** right advances n times; left never retreats and never exceeds right. The best saved range is therefore a longest valid range globally.

Expected time is O(n), with O(min(u,k+1)+1) peak auxiliary map state for u distinct input values: an expansion can temporarily introduce the (k+1)th kind. A simple O(u+1) bound is also valid. Output is one record. Negative values do not affect this frequency condition.

Examples: `([],2) -> (0,0)`; `([8,8,8],1) -> (0,3)`; `([1,2],0) -> (0,0)`; `([1,2,3],5) -> (0,3)`; `([-1,2,-1,3],2) -> (0,3)`.

For an at-most-K-distinct **character** window, the same invariant uses `Map<Character,Integer>` and reads incoming/outgoing values with `charAt`; keep the `counts.size() > k` repair condition. For example, `"aaabbc"` with K=2 has earliest longest range (0,5), `"aaabb"`. Do not substitute a per-character copy limit of K: `"aaaa"` has one distinct char and is entirely valid for K=1, even though a one-copy-per-char constraint would allow only length 1. The optional code-point adapter in Section 8 applies the distinct-kind algorithm in a different character model.

### 6.5 Duplicate restrictions and character-frequency limits

For strings, a different valid condition is “each UTF-16 char value appears at most limit times.” With limit=1 this forbids repeated char values. With limit>1 it is a frequency-budget problem. Limit zero permits only an empty range; negative limit is rejected.

```java
public static Window longestWithCopyLimit(String text, int limit) {
    if (limit < 0) throw new IllegalArgumentException("limit must be nonnegative");
    Map<Character, Integer> counts = new HashMap<>();
    int left = 0;
    Window best = new Window(0, 0);

    for (int right = 1; right <= text.length(); right++) {
        char incoming = text.charAt(right - 1);
        counts.put(incoming, counts.getOrDefault(incoming, 0) + 1);
        while (counts.getOrDefault(incoming, 0) > limit) {
            char outgoing = text.charAt(left++);
            int remaining = counts.get(outgoing) - 1;
            if (remaining == 0) counts.remove(outgoing);
            else counts.put(outgoing, remaining);
        }
        if (right - left > best.length()) best = new Window(left, right);
    }
    return best;
}
```

Only the incoming char can newly violate the limit: all other counts were already valid and did not increase. That is why the while condition may inspect that single key. Removing other outgoing characters on the way to its earliest excess occurrence still requires decrementing their counts.

For `"abbaca"` with limit=1:

| Right | Incoming | Left before | Removals | Valid window after repair | Best |
| --- | --- | --- | --- | --- | --- |
| 1 | a | 0 | None | `[0,1)` = a | `[0,1)` |
| 2 | b | 0 | None | `[0,2)` = ab | `[0,2)` |
| 3 | b | 0 | a, then b | `[2,3)` = b | `[0,2)` |
| 4 | a | 2 | None | `[2,4)` = ba | `[0,2)` |
| 5 | c | 2 | None | `[2,5)` = bac | `[2,5)` |
| 6 | a | 2 | b, then a | `[4,6)` = ca | `[2,5)` |

The proof mirrors the distinct-window proof, with “an excessive multiplicity cannot disappear by extension” as the elimination fact. Counts remain exact; repair restores the copy limit; every start discarded for excess remains unusable at later ends. Both pointers progress, and recording after repair finds the longest valid range. Expected time is O(n); auxiliary state is O(min(n,65536)+1) for char keys, with one-record output. Actual map state may be much smaller.

Examples: `("",1) -> (0,0)`; `("aaaa",1) -> (0,1)`; `("aaaa",2) -> (0,2)`; `("aaab",2) -> (1,4)`; `("abc",0) -> (0,0)`. The character model is deliberate; [Section 8](#8-frequency-counting-and-character-models) explains its Unicode limitations.

### 6.6 Replacement-budget reasoning: an Additional extension

Suppose a window may change at most b letters so that all its letters become equal. For an **exact current histogram**, the required changes are:

`window length - largest current character frequency`.

This is a different condition from “at most b distinct characters.” For `"AABBC"` and budget 1, a longest feasible length is 3: `"AAB"` or `"ABB"` needs one change, while every length-4 candidate needs two.

The condition is preserved by deleting an item: length falls by one, while the largest frequency falls by at most one. Therefore required changes cannot increase on deletion. It supports longest-window repair. For a fixed 26-letter alphabet, recomputing the exact largest count over 26 buckets during repair still gives O(26n)=O(n) work. With an input-dependent alphabet, scanning all keys repeatedly must be charged separately.

Some specialized length-only solutions use a historical maximum frequency that never decreases. That number can exceed the maximum in the current range. For example, after `"AAAA"` followed by B with budget zero, a repaired length-4 range `"AAAB"` can coexist with historical maximum 4 even though its true maximum is 3. Such an optimization needs a separate proof about the **best attainable length**, and does not support the claim that every current window is valid. Do not mix that state with the exact-valid-window invariant used above or return a claimed valid witness without justification. Derive the Additional platform problem independently.

**Self-check Q9:** Why is shrinking by just one item insufficient for an at-most-K repair?
**Self-check Q10:** Why can the copy-limit implementation check only the incoming character for excess?
**Self-check Q11:** Does a historical maximum frequency necessarily describe the current window?

## 7. Shortest Valid and Covering Windows

### 7.1 The answer timing reverses

For a shortest-valid problem, the useful condition often has the opposite shape: expanding an invalid range can make it valid, while removing a prefix can eventually make it invalid again.

1. Add the incoming item.
2. **While valid**, record the current candidate.
3. Remove the leftmost item and repeat while validity remains.

Recording after that loop would inspect an invalid range. Recording only once before the loop could miss a shorter valid suffix. The validity condition itself determines the safe movement.

### 7.2 Shortest nonnegative range reaching a threshold

**Problem:** values are nonnegative ints and threshold is a positive long. Return the shortest nonempty range whose sum is >= threshold; earliest start breaks ties. Empty input or insufficient total gives (-1,-1). Threshold<=0 is rejected so that empty ranges are never valid in this method.

For a nonnegative array, adding items cannot lower the sum and removing items cannot raise it. A baseline enumerates starts and ends, using a running inner sum, for O(n²) work. Reuse one rolling sum and discard starts once their valid candidate has been recorded.

```java
public static Window shortestAtLeast(int[] values, long threshold) {
    if (threshold <= 0) {
        throw new IllegalArgumentException("threshold must be positive");
    }
    int left = 0;
    long sum = 0;
    Window best = new Window(-1, -1);

    for (int right = 1; right <= values.length; right++) {
        sum += values[right - 1];
        while (sum >= threshold) {
            if (best.from() < 0 || right - left < best.length()) {
                best = new Window(left, right);
            }
            sum -= values[left++];
        }
    }
    return best;
}
```

The nonnegative-value precondition is not checked by a preliminary scan. The shared int-array bounds make the long sum safe; changing values to arbitrary longs would require a new overflow policy.

### 7.3 Threshold dry run

Input `[2,1,5,1,3,2]`, threshold=6. State sum always describes [left,right), including during shrink.

| Right | Added | Left before | Valid candidates recorded; removals | Window after loop | Best |
| --- | --- | --- | --- | --- | --- |
| 1 | 2 | 0 | None | `[0,1)`, sum 2 | Missing |
| 2 | 1 | 0 | None | `[0,2)`, sum 3 | Missing |
| 3 | 5 | 0 | `[0,3)` length 3; remove 2. `[1,3)` length 2; remove 1 | `[2,3)`, sum 5 | `[1,3)` |
| 4 | 1 | 2 | `[2,4)` length 2, tie; remove 5 | `[3,4)`, sum 1 | `[1,3)` |
| 5 | 3 | 3 | None | `[3,5)`, sum 4 | `[1,3)` |
| 6 | 2 | 3 | `[3,6)` length 3; remove 1 | `[4,6)`, sum 5 | `[1,3)` |

The final live range is invalid by design, while best is valid.

### 7.4 Correctness, zero values, and limits

**Initialization:** sum=0 is the empty range, which is invalid for positive threshold. **Expansion:** add exactly the incoming value. **Shrinking:** each recorded range is valid; removing its first value preserves the exact sum.

**Why discard a start:** once [left,right) is recorded, every future range with that same start has length at least as large. It cannot improve a minimum-length answer. The algorithm may safely advance left. **Why stop shrinking when invalid:** with nonnegative values, removing more cannot increase the sum, so no shorter suffix at this right end can recover validity. This second implication fails with arbitrary negative values.

Right examines every ending boundary. For each, all potentially improving valid starts are recorded until the first invalid suffix. Progress of left guarantees termination even if the outgoing value is zero: the sum need not strictly change for the interval to shrink. Positive threshold prevents an empty interval from keeping the while condition true.

Time is O(n); auxiliary space is O(1); output is one record. Examples: `([],1) -> (-1,-1)`; `([0,0],1) -> (-1,-1)`; `([0,0,5,0],5) -> (2,3)`; `([2,3],5) -> (0,2)`; `([2,3],6) -> (-1,-1)`; `([MAX,MAX],4294967294L) -> (0,2)`.

### 7.5 Pattern D: shortest multiset coverage — advanced transfer

The roadmap's Minimum Window Substring is optional C. The following original example uses integer event types instead of providing that platform's string-returning implementation.

**Problem:** given arbitrary int arrays events and required, return the shortest range containing every required event with its multiplicity. Unneeded event types may appear inside the range. Repeated requirements matter. Empty required returns (0,0); impossible nonempty coverage returns (-1,-1). Tie: earliest start.

For required `[7,7,2]`, one 7 plus one 2 does not cover the requirement. Need three required **occurrences** across two distinct required **kinds**.

A baseline tries every start, extends with fresh counts until covered, and compares lengths. It can cost O(n²+m) expected work even with incremental counts per start. Reuse the counts across both endpoints.

Let need map each required kind to its target count. Let have store positive current counts for **required kinds only**; irrelevant events are deliberately omitted. Let formed count required kinds whose current count is at least their required count. Coverage means `formed == need.size()`.

```java
public static Window smallestCoverage(int[] events, int[] required) {
    if (required.length == 0) return new Window(0, 0);
    Map<Integer, Integer> need = new HashMap<>();
    for (int value : required) {
        need.put(value, need.getOrDefault(value, 0) + 1);
    }
    Map<Integer, Integer> have = new HashMap<>();
    int formed = 0, left = 0;
    Window best = new Window(-1, -1);

    for (int right = 1; right <= events.length; right++) {
        int incoming = events[right - 1];
        Integer requiredCount = need.get(incoming);
        if (requiredCount != null) {
            int now = have.getOrDefault(incoming, 0) + 1;
            have.put(incoming, now);
            if (now == requiredCount.intValue()) formed++;
        }
        while (formed == need.size()) {
            if (best.from() < 0 || right - left < best.length()) {
                best = new Window(left, right);
            }
            int outgoing = events[left++];
            Integer needed = need.get(outgoing);
            if (needed != null) {
                int before = have.get(outgoing);
                if (before == needed.intValue()) formed--;
                if (before == 1) have.remove(outgoing);
                else have.put(outgoing, before - 1);
            }
        }
    }
    return best;
}
```

### 7.6 Coverage dry run

Events `[7,2,7,4,2,7]`; required `[7,7,2]`. Need is 7:2,2:1, so two kinds must be formed. Have omits type 4.

| Right | Added | Have after add | Formed after add | Shrinking and validity | Best |
| --- | --- | --- | --- | --- | --- |
| 1 | 7 | 7:1 | 0 | Invalid; no shrink | Missing |
| 2 | 2 | 7:1,2:1 | 1 | Invalid; no shrink | Missing |
| 3 | 7 | 7:2,2:1 | 2 | Record `[0,3)`. Remove 7: have 7:1,2:1; formed=1; left=1 | `[0,3)` |
| 4 | 4 | 7:1,2:1 | 1 | Irrelevant type; still invalid | `[0,3)` |
| 5 | 2 | 7:1,2:2 | 1 | Extra 2 does not form a new kind | `[0,3)` |
| 6 | 7 | 7:2,2:2 | 2 | Record `[1,6)`. Remove 2: still covered, left=2. Record `[2,6)`. Remove 7: formed=1, left=3 | `[0,3)` |

The count transition matters. Adding a third 7 after need[7]=2 does not increase formed again. Removing a surplus 2 from count 2 to count 1 does not decrease formed. Removing a 7 from 2 to 1 does.

### 7.7 Correctness and cost of coverage

**Initialization:** need is fixed; have and formed describe empty coverage. **Expansion:** an incoming required kind gains one count; formed changes only when crossing from insufficient to sufficient. Irrelevant values change the interval length, but not coverage state. **Shrinking:** a required kind loses sufficiency only when its count crosses below its requirement. Have remains the exact required-kind projection of the interval.

**Answer timing:** all candidates saved inside the loop are covered. Every removed start has already produced its shortest covered candidate at the earliest currently available end; a later end at that same start cannot improve its length. Once coverage is lost, removing more cannot supply missing occurrences. Thus no shorter covered suffix at this end is missed. Both endpoints progress, so the algorithm terminates and best is globally shortest.

For n events, m required occurrences, and d distinct required kinds, expected time is O(n+m), auxiliary space O(d+1), and output one record. Have is restricted to required kinds; counting every unrelated event would instead require state depending on the live input alphabet.

Examples: `([9,1,2,1,3,2],[1,1,2]) -> (1,4)`; `([7,2],[7,7]) -> (-1,-1)`; `([],[1]) -> (-1,-1)`; `([1,2],[]) -> (0,0)`; `([5,5,5],[5,5]) -> (0,2)`. A coverage histogram is unsuitable when the required elements must appear in a prescribed sequence rather than in any order.

**Self-check Q12:** Why must a shortest-window loop record before removing its leftmost item?
**Self-check Q13:** Why can zeros require several shrink iterations without threatening termination?
**Self-check Q14:** For need={7:2,2:1}, what are the required occurrence total and the required-kind total?

## 8. Frequency Counting and Character Models

### 8.1 Counts, kinds, and requirements are different quantities

For an active window `"aaba"`:

| Quantity | Value | Meaning |
| --- | --- | --- |
| Frequency of a | 3 | Three occurrences of this char value |
| Frequency of b | 1 | One occurrence of this char value |
| Total occurrences | 4 | Sum of live counts; also window length in this model |
| Distinct kinds | 2 | Number of positive-count keys |
| Required kinds for `"aab"` | 2 | a and b |
| Required occurrence total for `"aab"` | 3 | Two a occurrences and one b |
| Satisfied required kinds | 2 | Both targets have been reached |
| Matched required occurrences, capped per kind | 3 | Extra a does not create an extra requirement |

A set can represent distinct membership, but a set alone cannot remove one occurrence correctly if another copy remains. A map records multiplicities.

```java
public static int[] charFrequencySummary(String text) {
    Map<Character, Integer> counts = new HashMap<>();
    for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        counts.put(c, counts.getOrDefault(c, 0) + 1);
    }
    return new int[]{text.length(), counts.size()};
}
```

Output is `[total UTF-16 code units, distinct char values]`. Examples: `"banana" -> [6,3]`; `"aab" -> [3,2]`; `"" -> [0,0]`. Expected time is O(n), auxiliary space O(u+1) for u distinct char values, and output has two ints. This builds a whole-input histogram for demonstration; the window implementations perform the same updates only for active contents.

### 8.2 Entering and leaving a window

For a map with positive counts and no null values:

| Event | Operation | Distinct-kind effect |
| --- | --- | --- |
| A previously absent key enters | Read default 0; put 1 | +1 |
| Another occurrence enters | Put old+1 | No change |
| One of several occurrences leaves | Put old-1 | No change |
| Last occurrence leaves | Remove the key | -1 |

`getOrDefault` reads; it does not insert. `put` performs the mutation. `remove` removes a mapping. `size` counts mappings, not occurrences and not “keys whose numeric value is positive.” If you keep `{a:0,b:1}`, size is 2 even though only b is live. These [HashMap contracts](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) explain why zero cleanup is part of the distinct-window invariant.

For frequency arrays, zero slots remain allocated, so use a separate distinct counter if needed: increment it on 0→1, decrement it on 1→0. Do not increment on every occurrence. A `long[]` may be needed when aggregating counts from a stream or many batches beyond int range; the one-input examples here fit int. Sum values into long when the value total, rather than the occurrence count, can exceed int.

### 8.3 Track satisfaction by threshold crossings

For requirement need[x]=2:

| Change in have[x] | Satisfied-kind transition |
| --- | --- |
| 0→1 | Still insufficient |
| 1→2 | formed increases once |
| 2→3 | Still one satisfied kind |
| 3→2 | Still satisfied |
| 2→1 | formed decreases once |

An alternative summary is **missing required occurrences**: initialize missing to the requirement length, decrement it when an incoming item fills a still-unfilled occurrence, and increment it when removal creates a deficit. Coverage is then missing=0. This summary ranges over occurrences, while formed ranges over kinds. Both work if updates and comparisons use the same definition.

Repeated full-map equality or summing every count after each endpoint move can destroy linearity. Maintain a scalar summary such as distinct, formed, missing, or nonZero when each changed bucket can update it locally. For a verified 26-slot alphabet, a full 26-slot scan is also a constant-factor option; do not silently extend that argument to a growing alphabet.

### 8.4 Choose a frequency representation

| Representation | Appropriate domain | Update cost | Auxiliary state | Limitation |
| --- | --- | --- | --- | --- |
| `int[26]` | Verified lowercase English a..z | O(1) | O(26) | Uppercase, punctuation, and arbitrary text violate the indexing assumption |
| `int[128]` | Verified ASCII char values 0..127 | O(1) | O(128) | A Java String is not automatically ASCII |
| `int[65536]` | All UTF-16 char values | O(1) | O(65536) | Counts code units, including surrogate halves |
| `HashMap<Character,Integer>` | Sparse char-value domain | Expected O(1) | O(u+1) | A char need not be a complete Unicode character |
| `HashMap<Integer,Integer>` | Arbitrary int values or decoded code points | Expected O(1) | O(u+1) | Code-point indices may differ from String indices |

A fixed alphabet is constant relative to input length, but memory constants can still matter. Allocating 65,536 counters is not the same practical choice as allocating 26. For generic-alphabet analysis use Σ or u explicitly instead of calling every histogram O(1).

### 8.5 UTF-16 units, code points, and perceived characters

Java `String.length()` counts UTF-16 code units and `charAt(i)` returns one such unit. Supplementary Unicode symbols use a surrogate pair. The [String API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) defines indices in this unit.

For `"\uD83D\uDE00\uD83D\uDE01"`, displaying two different emoji:

- UTF-16 length is 4.
- Code-point count is 2.
- The two emoji share the same high-surrogate char value.

Consequently, longestWithCopyLimit(text,1) returns `(1,4)`, length 3 **code units**. Its result is correct under its stated char-value contract but can begin inside a surrogate pair. It is unsuitable when “character” means a whole Unicode code point.

This optional adapter reuses the arbitrary-int distinct-window algorithm for **well-formed Unicode text**:

```java
public static Window longestAtMostKCodePoints(String text, int k) {
    if (k < 0) throw new IllegalArgumentException("k must be nonnegative");
    int[] points = text.codePoints().toArray();
    return longestAtMostKDistinct(points, k);
}
```

Its returned boundaries are **code-point-array positions**, not String UTF-16 indices. For the two-emoji text and k=2, it returns (0,2). Do not pass those boundaries directly to substring and expect the same range. Mapping the selected code-point boundaries back to UTF-16 offsets requires additional conversion work or an offset table.

Let n be UTF-16 length and p code-point count. This simple adapter takes expected O(n+p) time and O(p+min(u,k+1)+1) auxiliary space, including the decoded array. The stream combines valid surrogate pairs; handling malformed text is a separate input-policy question. Code points still do not equal user-perceived grapheme clusters: `"e\u0301"` has two code points but can display as one accented unit. Full grapheme segmentation and normalization are beyond the core roadmap; state such requirements before choosing a char-based algorithm.

**Self-check Q15:** Why must a distinct-count map remove zero-frequency keys?
**Self-check Q16:** Does using Character keys make an algorithm automatically Unicode-code-point aware?

## 9. Amortized Complexity and Complete Costs

### 9.1 Count total travel instead of multiplying loop shapes

Consider the longest-window shape: a right loop contains a left-moving while loop. If right advances n times and left **never retreats**, then left can advance at most n times over the entire algorithm. One right step may cause many removals; earlier steps cause fewer. The total is bounded by the number of items that can leave.

For the distinct dry run, right advances 7 times. Left advances twice at right=4 and once at right=6, for 3 total removals. It does not restart at zero for each right boundary. Pointer work is O(n+n)=O(n), not O(n²).

For shortest-threshold and shortest-covering loops, a large right step may record and remove many valid prefixes. Each removed position is still removed only once. The same aggregate argument applies even though the while condition is “valid” rather than “invalid.”

A useful accounting method charges each removal to that element's earlier insertion. There are at most n insertions and n removals. A potential argument uses the live window length as stored credit: adding creates credit; removing spends it. This is deterministic aggregate/amortized reasoning, not a claim about random inputs.

### 9.2 Loop-condition work also counts

There is at most one final unsuccessful while check per outer iteration, plus one successful check per removal. Therefore condition-evaluation count is O(n) too. But if **each evaluation scans the entire window**, O(n) evaluations may still cost O(n²).

For example, on an all-distinct string with a permissive constraint, rebuilding a set from the current range at every right end processes lengths 1+2+...+n. Pointer motion remains linear; repeated validation is quadratic. Likewise, extracting a growing substring and scanning it can repeatedly copy and process its contents.

The honest formula is:

`total time = endpoint work + sum of update costs + sum of validation costs + preprocessing + output construction`.

### 9.3 Complete cost table

Here n is input length, m is pattern/requirement length, u is the distinct relevant input alphabet, d is distinct required kinds, k is a fixed window length, and w=n-k+1 for `1<=k<=n`.

| Method or baseline | Time | Auxiliary space | Required output |
| --- | --- | --- | --- |
| Recompute each fixed-window sum | O(wk) | O(1) | O(w) sums if returned |
| Enumerate all variable intervals with a running inner sum | O(n²) | O(1) | Depends on requested output |
| fixedWindowSums | O(n), or O(1) if k>n | O(1) | O(w), or empty |
| fixedWindowAverages wrapper | O(n), or O(1) if k>n | O(w) intermediate sums | O(w) doubles |
| countAnagramWindows | O(n+m), with constant-time early exits | O(26) | Scalar |
| longestAtMostKDistinct | Expected O(n) | O(min(u,k+1)+1) | One Window |
| longestWithCopyLimit | Expected O(n) | O(min(n,65536)+1) | One Window |
| shortestAtLeast, nonnegative input | O(n) | O(1) | One Window |
| smallestCoverage | Expected O(n+m) | O(d+1) | One Window |
| charFrequencySummary | Expected O(n) | O(u+1) | Two ints |
| Prefix-map exact-sum counting, Section 11 | Expected O(n) | O(n+1) | Scalar long |
| Monotonic deque, Section 12 | O(n) total | O(k) | O(w) indices |

Include constant setup for empty inputs. Hash-based expected bounds assume suitable distribution and constant-cost operations on the shown primitive-wrapper keys. Arbitrary expensive custom keys would add their own hashing/equality costs. HashMap resizing has amortized costs; it does not make every individual insertion worst-case O(1).

The code creates some short-lived records and boxed counts. Peak auxiliary storage is distinct from total allocated objects. The table counts **peak live algorithmic state**; creating a new best Window many times does not require storing all earlier best records. Unreachable objects may await garbage collection, which is an implementation/runtime issue rather than a retained algorithmic history.

### 9.4 The deque has two removal reasons, one lifetime

In a monotonic deque, each index is inserted once and removed at most once: either from the back because it is dominated, or from the front because it expires. The two categories do not each remove the same occurrence again. This gives O(n) abstract deque operations.

ArrayDeque endpoint operations are themselves amortized constant time. These two arguments compose to O(n) total work. A single iteration may still remove O(k) candidates. [Stack's aggregate analysis](./Stack/Stack_Essential_Concepts.md#13-deriving-time-and-space-complexity) provides the related push-once/pop-once proof; [the complexity reference](./Complexity/Complexity_Analysis_and_Foundations.md#13-amortized-complexity) explains amortization.

**Self-check Q17:** Why is “there is a while inside a for” insufficient to conclude quadratic time?
**Self-check Q18:** Give a way to keep pointer movement O(n) while making total time O(n²).

## 10. When Sliding Window Is Inappropriate

### 10.1 Ask what one boundary movement proves

Window suitability depends on the **condition**, not just the data type or sign of the values.

| Condition | Useful property | Consequence |
| --- | --- | --- |
| At most K distinct | Removing cannot introduce a new kind; extending cannot repair excess kinds | Repair invalid windows for a longest objective |
| At most L copies of each value | Removing cannot increase a count | Repair invalid multiplicity |
| Sum >= positive threshold with nonnegative values | Extending cannot reduce the sum; removing cannot raise it | Record and shrink valid windows for a shortest objective |
| Multiset coverage | Extending cannot remove required occurrences; removing cannot create missing ones | Record covered windows, then shrink |
| Exactly a target sum with arbitrary signed values | Neither direction consistently controls sum | A generic one-window rule is unjustified |

A problem can have useful monotonicity even when there is no globally sorted input. Conversely, a contiguous range does not guarantee an adequate monotonicity rule. Binary Search requires its own ordered decision domain; it is not an automatic replacement for every failed window.

### 10.2 A real exact-sum failure

This intentionally incorrect method claims to detect a nonempty exact-sum range in an **arbitrary signed int array**, with positive target. It borrows a shrinking rule that only has a suitable sum-direction argument under additional assumptions.

```java
public static boolean buggyExactSumWindow(int[] values, long target) {
    int left = 0;
    long sum = 0;
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

On `[4,-1]` with target 3:

| Right | Operation | State after repair | Consequence |
| --- | --- | --- | --- |
| 1 | Add 4; sum=4>3, so remove 4 | left=1, sum=0 | Start 0 is discarded |
| 2 | Add -1 | left=1, sum=-1 | Returns false |
| Correct candidate | `[0,2)` has sum `4+(-1)=3` | Nonempty exact match exists | The eliminated start was necessary |

An overly large prefix can become suitable after a later negative value. The observation sum>target did not license discarding its start.

### 10.3 A shortest-threshold failure

Copy the Section 7 shortest-sum movement rule **without its nonnegative precondition** onto `[1,-1,5]`, threshold 5. At the final end, sum=5 records length 3. Removing the first 1 gives sum=4, so the loop stops. But removing the next value, -1, would **increase** the sum to 5 and expose the one-element range [5].

The valid answer is `[2,3)`, length 1. The rule returned a length-3 answer because “once invalid, every shorter suffix is invalid” was false. This example tests the missing assumption; it is outside shortestAtLeast's documented nonnegative contract.

Likewise, a naive longest-sum-at-most-1 repair discards 5 immediately on `[5,-4]`. It can then find only length 1, although the whole length-2 range sums to 1. A future extension repaired the violation, so permanently discarding the start was unsafe.

### 10.4 Negative values do not invalidate all windows

The fixed-window sums in Section 4 work with negative numbers because every candidate is still visited. Distinct-value and frequency-copy constraints depend on multiplicities, so signed integer keys cause no special problem. The deque maximum also compares negative values normally.

What fails is a specific argument such as “removing the first item always reduces the sum.” Replace it with a proof for the actual condition, or choose another method.

Other reasons to reject a generic window:

- The output concerns subsequences or arbitrary subsets rather than contiguous input.
- You need many independent static range queries; prefix preprocessing may fit better.
- You need all exact-sum counts, including many zero-valued extensions; one saved valid range per end is insufficient.
- You need a maximum after deletions, but the state stores only the previous maximum.
- You sorted the data even though original adjacency is part of the answer.
- Validity is an arbitrary predicate for which neither extension nor removal supports safe elimination.

**Self-check Q19:** What precise implication fails on [1,-1,5] for shortest-threshold shrinking?
**Self-check Q20:** Give a signed-input window task that remains valid, and explain why.

## 11. Prefix Sum and HashMap Alternatives

### 11.1 Range differences preserve information a single window discards

Define `P[0]=0` and `P[r]` as the sum of values[0..r). Then the sum of [left,right) is `P[right]-P[left]`. This identity holds with negative values. It does not require sum monotonicity.

For static range-sum queries, build a long prefix array once in O(n) time/storage, then answer each query in O(1). Reuse the detailed [prefix-sum explanation](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries) rather than rebuilding a separate theory here.

For exact target counting:

`P[right]-P[left]=target` means `P[left]=P[right]-target`.

Keep **all earlier prefix frequencies**, not just one current window start. Several earlier starts can work for the same end.

### 11.2 Original example: count exact-sum ranges

**Contract:** arbitrary signed int array, nonempty subarrays only, and long target in `[-10^15,10^15]`. Return a long count. Empty input returns 0. Duplicates and negative values are valid. The target restriction and shared input bounds ensure prefix-target fits long; full-range long inputs would need a separate checked-arithmetic policy.

```java
public static long countExactSumRanges(int[] values, long target) {
    if (target < -1_000_000_000_000_000L ||
            target > 1_000_000_000_000_000L) {
        throw new IllegalArgumentException("target outside supported range");
    }
    Map<Long, Long> earlierPrefixes = new HashMap<>();
    earlierPrefixes.put(0L, 1L);
    long prefix = 0, answer = 0;
    for (int value : values) {
        prefix += value;
        answer += earlierPrefixes.getOrDefault(prefix - target, 0L);
        earlierPrefixes.put(prefix,
                earlierPrefixes.getOrDefault(prefix, 0L) + 1L);
    }
    return answer;
}
```

Before querying at right, the map contains frequencies of prefixes at positions `0..right-1`. Query **before inserting the current prefix** so that target zero does not count an empty [right,right) interval. The initial zero prefix allows valid ranges beginning at index zero.

This map describes earlier boundaries permanently; its keys are not “values currently inside one sliding window.” Do not apply outgoing-count cleanup to this different invariant.

### 11.3 Prefix dry run

Input `[2,-1,2,-2,1]`, target=2.

| Right | Added | Prefix | Needed earlier prefix | Earlier frequency | Running answer |
| --- | --- | --- | --- | --- | --- |
| 1 | 2 | 2 | 0 | 1 | 1 |
| 2 | -1 | 1 | -1 | 0 | 1 |
| 3 | 2 | 3 | 1 | 1 | 2 |
| 4 | -2 | 1 | -1 | 0 | 2 |
| 5 | 1 | 2 | 0 | 1 | 3 |

The three ranges are `[0,1)`, `[2,3)`, and `[0,5)`. The two prefixes equal to 1 are both retained even though they arise at different positions.

**Correctness:** every matching earlier boundary gives exactly one nonempty range ending here; every exact-sum range has such a boundary by the prefix identity. Querying before insertion uses only earlier positions. Summing those counts over all right endpoints counts every range exactly once. The finite scan terminates.

Expected time is O(n), auxiliary space O(n+1), output a scalar long. The count can be n(n+1)/2, which is **5,000,050,000** for n=100,000 and an all-zero array with target zero. Both answer and prefix multiplicities use long. Examples: `([4,-1],3) -> 1`; `([1,-1,1],1) -> 3`; `([0,0],0) -> 3`; `([],0) -> 0`.

### 11.4 Choose the prefix state for the objective

| Objective | Useful earlier-prefix state |
| --- | --- |
| Does an exact-sum range exist? | Set of earlier prefix values |
| Count all exact-sum ranges | Map from prefix value to frequency |
| Longest exact-sum range | Map from prefix value to earliest position |
| Many specified static sum queries | Full prefix array |
| Shortest threshold with arbitrary signed values | Needs additional order/dominance reasoning; a frequency map alone is insufficient |

The last task can use an advanced monotonic deque over prefixes, but it is a different algorithm from the fixed-window maximum deque below. It is outside the core scope here. “Use prefix sums” identifies a representation; you must still derive the operation that solves the actual output objective.

**Self-check Q21:** Why is prefix zero inserted before scanning, and why is each current prefix inserted after its query?
**Self-check Q22:** Why does exact-sum counting need frequencies instead of only a set?

## 12. Monotonic Deque: Optional Advanced

### 12.1 Pattern E: moving maximum candidates

For every length-k window, scanning all k values for its maximum costs O((n-k+1)k). A rolling sum supports undo by subtraction. A maximum does not: after the maximum leaves, the scalar old maximum says nothing about the next best candidate.

Store candidate **indices** in a deque. Indices identify both value and expiry position. Keep them increasing from front to back and their values strictly decreasing. The front then identifies the current maximum.

There are two removal reasons:

1. **Expiry at the front:** the index is before the current window start.
2. **Dominance at the back:** the incoming value is at least as large and is newer, so the older candidate cannot win a future query while both could matter.

This extends [monotonic-stack reasoning](./Stack/Stack_Essential_Concepts.md#8-monotonic-stack-fundamentals). A deque needs front removal as time expires old candidates, as well as back removal for dominance.

### 12.2 Original implementation with an explicit tie rule

**Problem:** return the **original index of the rightmost maximum** in each length-k range. Values are arbitrary ints, including negatives and duplicates. k must be positive; k>n gives an empty output. The index output and tie policy differ from a generic maximum-value-only exercise.

```java
public static int[] maxWindowIndices(int[] values, int k) {
    if (k <= 0) throw new IllegalArgumentException("k must be positive");
    if (k > values.length) return new int[0];
    int[] result = new int[values.length - k + 1];
    Deque<Integer> candidates = new ArrayDeque<>();

    for (int right = 1; right <= values.length; right++) {
        int incomingIndex = right - 1;
        int left = right - k;
        while (!candidates.isEmpty() && candidates.peekFirst() < left) {
            candidates.pollFirst();
        }
        while (!candidates.isEmpty() &&
                values[candidates.peekLast()] <= values[incomingIndex]) {
            candidates.pollLast();
        }
        candidates.offerLast(incomingIndex);
        if (right >= k) result[right - k] = candidates.peekFirst();
    }
    return result;
}
```

Before a full window exists, left may be negative as a **prospective** start; the actual active prefix is [0,right). No negative element index is read. Once right>=k, left is the actual start and the deque contains only its candidates.

Using <= in back pruning removes an older equal maximum, enforcing the rightmost-index tie rule. A strict < comparison would retain older equals and require a different tie interpretation. Values alone are insufficient for this implementation because position determines expiry and the requested answer.

### 12.3 Detailed deque dry run

Input `[6,2,2,5,1,5,3]`, k=3. Deque notation is front→back, `index:value`.

| Right | Incoming | Expired from front | Dominated from back | Deque after insertion | Emitted index |
| --- | --- | --- | --- | --- | --- |
| 1 | 0:6 | None | None | `[0:6]` | Partial |
| 2 | 1:2 | None | None | `[0:6,1:2]` | Partial |
| 3 | 2:2 | None | 1:2, older equal | `[0:6,2:2]` | 0 |
| 4 | 3:5 | 0:6 | 2:2 | `[3:5]` | 3 |
| 5 | 4:1 | None | None | `[3:5,4:1]` | 3 |
| 6 | 5:5 | None | 4:1, then 3:5 | `[5:5]` | 5 |
| 7 | 6:3 | None | None | `[5:5,6:3]` | 5 |

Output is `[0,3,3,5,5]`, corresponding to values `[6,5,5,5,5]`. Equal 5 values at indices 3 and 5 illustrate the tie policy.

### 12.4 Correctness

**Initialization:** the empty deque represents the empty processed prefix. **Expiry:** indices are increasing, so all expired candidates form a removable front segment. Index equal to left is still inside [left,right), so expire only indices **< left**.

**Dominance:** if an older candidate has value <= the incoming value, every future window that could still use that older index also contains the newer index until the older one expires. The newer value is no worse, is preferred on ties, and expires later. Removing the older index cannot lose a winning answer.

**Preservation:** after pruning and appending, indices increase, values strictly decrease, and all stored indices are unexpired. Every unexpired omitted position has a surviving dominance chain to a no-worse newer candidate. Therefore the front is a valid rightmost maximum for the current window.

**Answer timing:** emit only for right>=k, after expiry and dominance processing. Every legal full window is visited once. Each inner iteration removes an index, and right always advances, so termination follows.

Time is O(n) total by the insert-once/remove-once argument; auxiliary space is O(k); required output is O(n-k+1). A strictly decreasing input can keep k candidates, so the space bound is real. Endpoint operations on [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) are amortized constant time. Arbitrary value removal or contains scans are not part of the algorithm.

Examples: `([5,5,1],2) -> [1,1]`; `([-3,-1,-2],2) -> [1,1]`; `([9,1,2],3) -> [0]`; `([7],1) -> [0]`; `([],1) -> []`; `([1,2],3) -> []`. Window minimum uses the opposite dominance comparison, with a separately chosen tie rule. Arbitrary range updates or queries in unrelated order need different structures.

**Self-check Q23:** Why can a newer equal value dominate an older one under this output contract?
**Self-check Q24:** Why is expiring index equal to the current left boundary an off-by-one error?

## 13. Java 21 Implementation Decisions

### 13.1 Boundaries and text allocation

| Operation | Window application | Pitfall |
| --- | --- | --- |
| `array.length` | Exclusive array boundary | It is a field, unlike String.length() |
| `String.length()` | Number of UTF-16 units | It is not necessarily code-point count |
| `String.charAt(i)` | Incoming/outgoing char value | Valid only for `0<=i<length()` |
| `substring(from,to)` | Materialize a final UTF-16 range | to is exclusive; repeated copies can dominate runtime |
| `int[]` | Bounded-domain frequencies | Establish the alphabet before direct indexing |
| `long[]` | Returned sums or wide aggregate state | Widen before an overflowing operation |
| `StringBuilder` | Build a requested output text or report | It is not needed to hold a window already identified by indices |

Keep the best boundaries during scanning and extract text **once** if a String is required. In the usual Java 21 cost model, materializing a length-L substring costs O(L) time/storage; exact allocation shortcuts are implementation details, not a reason to omit repeated copying from an analysis. Copying best text on every improvement can accumulate quadratic output-construction work, even if the final answer is short enough to fit memory.

StringBuilder helps append newly constructed output. Repeatedly deleting its first character can shift remaining contents, so it does not automatically make a moving window efficient. For a range in an existing immutable String, two indices are usually simpler.

### 13.2 Map updates and boxing

`Map<Character,Integer>` and `Map<Integer,Integer>` box primitive keys and counts. Their APIs are convenient for unknown/sparse domains, but a verified frequency array avoids hashing and boxing. No null count is inserted by our code, so unboxing existing counts is safe when the invariant establishes membership.

Use `getOrDefault(key,0)` for a possibly absent count and `put` to store the changed count. Outgoing removal must use the same character/value model as incoming addition. If code adds a full code point but removes one UTF-16 char, the two updates do not undo each other.

Avoid `==` between two boxed Integer references when you mean numeric equality. Reference caching can make small tests misleading. Our coverage implementation converts requirements with intValue before comparison. Prefer primitive local variables or equals where appropriate.

Map.size is O(1) for the used implementation, but iterating through every key/value to test validity is not. Iteration can depend on both map size and capacity; maintain a small summary rather than repeatedly scanning a large map. See [the Maps reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md#expected-and-worst-case-cost).

### 13.3 Deque endpoint operations

The optional maximum implementation uses `Deque<Integer> candidates = new ArrayDeque<>();`.

| API | Meaning here | Empty behavior |
| --- | --- | --- |
| `offerLast(index)` | Append a new candidate at the back | Inserts; ArrayDeque grows as needed |
| `peekFirst()` | Inspect the winning/oldest candidate | null if empty |
| `pollFirst()` | Remove an expired front candidate | null if empty |
| `peekLast()` | Inspect a candidate that may be dominated | null if empty |
| `pollLast()` | Remove that dominated back candidate | null if empty |

ArrayDeque rejects null elements. Guard emptiness before a peek result is unboxed to int or used as an array index; unboxing null throws NullPointerException. After insertion, at least the incoming index remains, so the emitted front is non-null.

Endpoint operations are amortized O(1). `contains`, `remove(Object)`, and occurrence searches can require a linear scan. The algorithm needs neither arbitrary removal nor repeated membership searches.

### 13.4 Numeric bounds and empty-input policy

A sum of 100,000 full-range int values fits long. A total pair/range count can exceed int even when every input index fits int. Averages require floating-point division if fractions matter.

The expression `long bad = Integer.MAX_VALUE + Integer.MAX_VALUE;` still adds as int and produces -2. `(long) Integer.MAX_VALUE + Integer.MAX_VALUE` correctly yields 4294967294. For an incoming/outgoing difference, `(long) incoming - outgoing` widens before subtraction; `(long) (incoming-outgoing)` widens after possible damage.

Our rolling sums add and subtract separately from a long accumulator; they do not compute an overflowing int difference first. Prefix-target subtraction is safe because its stated domain is bounded. A method accepting arbitrary long values needs a checked, saturating, or wider-arithmetic policy derived from its output contract.

Empty behavior is an API decision:

- Fixed positive k with no full range returns an empty output, including empty input.
- Longest-at-most constraints allow (0,0).
- Shortest positive-threshold/nonempty-cover searches use (-1,-1) if impossible.
- Empty multiset requirement is already covered at (0,0).
- Empty pattern matches n+1 boundary positions in our counting example.
- Invalid k<=0 is rejected by fixed-window/deque methods; k=0 is allowed as a distinct-kind limit.

Do not borrow one method's absence rule for a different task.

**Self-check Q25:** Why can assigning an int sum to long still overflow, and where must widening occur?
**Self-check Q26:** Why is deque peek safe at answer emission but guarded in the pruning conditions?

## 14. Comparing Techniques

### 14.1 Algorithm selection by operations and assumptions

| Comparison | Useful choice and advantage | Assumptions and limitations | Typical task |
| --- | --- | --- | --- |
| Sliding Window vs general Two Pointers | Window maintains information about a contiguous input interval; other pointer methods may compare ends or compact output | Both need a justified move; two indices alone do not identify a window | Substring constraints vs sorted-pair search |
| Sliding Window vs prefix array | Window updates one evolving interval with little state; prefix array answers unrelated range sums quickly after preprocessing | Window needs safe movement; prefix array needs O(n) storage and a stable input snapshot | One best range vs many specified sum queries |
| Sliding Window vs prefix HashMap | Window can give O(1) or small live state; prefix map retains many earlier starts and handles signed exact sums | Prefix state must match existence/count/length objective and needs O(n) storage | Distinct substring vs signed exact-sum count |
| Fixed vs variable window | Fixed size enumerates all starts; variable size skips starts using a validity proof | Negative values do not harm fixed sums; variable sum direction needs assumptions | Rolling average vs longest valid range |
| Longest vs shortest valid | Longest repairs invalid state, then records; shortest records valid state, then contracts | These rules depend on their respective deletion/extension properties | At most K kinds vs enough required counts |
| Frequency array vs map | Array gives direct deterministic updates; map uses sparse keys without a small-alphabet requirement | Arrays need a verified domain; maps add expected hashing and object overhead | Lowercase pattern vs arbitrary event IDs |
| Window vs brute force | Reuses overlap and often avoids revisiting starts | Brute force has fewer proof assumptions and is a valuable small-input oracle | Derive and cross-check a new method |
| Scalar rolling state vs monotonic deque | Sum is undoable; deque retains replacement maximum candidates | Old maximum alone cannot be undone; deque needs a dominance and expiry invariant | Rolling total vs moving maximum |
| Sliding Window vs Binary Search on answer | A direct window may solve the objective in one pass; feasibility search repeatedly tests candidate answers | Binary Search needs monotone feasibility in answer space and includes checker cost | Shortest nonnegative threshold vs other bounded-answer tasks |

Sliding Window and a monotonic deque are not mutually exclusive categories: the deque is one way to maintain a window's state. Likewise, a HashMap may be part of a window or part of a prefix algorithm; the container name does not determine the algorithm.

### 14.2 Stock profit is a running-minimum scan

The roadmap includes Best Time to Buy and Sell Stock in this category, but its clean invariant can be “smallest price **strictly before** the candidate selling day.” You do not need to add/remove every item between two positions or impose a generic shrink loop.

For original prices `[9,5,7,4,10]`:

| Selling index | Price | Earlier minimum | Candidate difference | Best nonnegative difference so far |
| --- | --- | --- | --- | --- |
| 0 | 9 | None | No earlier buy | 0 |
| 1 | 5 | 9 | -4 | 0 |
| 2 | 7 | 5 | 2 | 2 |
| 3 | 4 | 5 | -1 | 2 |
| 4 | 10 | 4 | 6 | 6 |

After checking a selling day, its price can contribute to the earlier minimum for later days. This is O(n) time/O(1) auxiliary state, with the time-order constraint supplied by scan order. If the transaction rules change, derive the state again. Use this conceptual example to prepare for independent platform practice.

### 14.3 Avoid sorting away the question

For values `[3,1,2]`, the original length-2 sums are 4 and 3. After sorting, they are 3 and 5. A window on the sorted data answers a different adjacency question. Sorting may be useful for arbitrary-pair questions, but is not a transparent preprocessing step for original subarrays.

**Self-check Q27:** Why is the earlier-minimum scan sufficient for the single-transaction stock objective?

## 15. Common Mistakes and Edge Cases

### 15.1 Failure witnesses and repairs

Use a witness to connect a bug to an invariant. Changing a comparison until examples pass is weaker than explaining the missing fact.

| Mistake | Failing example or misleading state | Correction |
| --- | --- | --- |
| Mix inclusive and exclusive length formulas | [0,2) on `"ab"` has length 2; adding one reports 3 | Use right-left for half-open windows |
| Forget outgoing sum/count | `[2,1,5,1]`, k=3: next sum becomes 9 instead of 7 | Undo the outgoing contribution before treating the state as the new interval |
| Keep zero keys while using map.size | `[1,2]`, K=1: {1:0,2:1} still has size 2, causing over-shrinking | Delete a key at zero, or maintain a correct independent distinct count |
| Delete a key with copies remaining | `[4,4,7]` loses one 4; removing key 4 hides the other | Decrement first; delete only when zero |
| Repair with if when several removals are needed | `[4,1,4,2]`, K=2: removing one 4 leaves three kinds | Use while and preserve exact counts each time |
| Record longest before restoring validity | `[1,2,3]`, K=2: length 3 is invalid | Record after repair |
| Remove before recording shortest | `[5]`, threshold 5: removing 5 loses the only answer | Save the valid candidate before contraction |
| Omit progress in a repair loop | `"aa"`, copy limit 1, while excess but neither count nor left changes | Every successful repair step must change the state and advance the relevant boundary |
| Count occurrences as satisfied kinds | Need `"aab"`; window `"aa"` has two occurrences but no b | Form a kind only when its required threshold is reached |
| Decrease formed when only surplus leaves | Need a:2; have a changes 3→2 | Decrease formed only on sufficient→insufficient transition |
| Use a 26-slot array without checking the domain | `'A'-'a'` is -32 | State lowercase-only input or choose a suitable map/domain |
| Confuse anagram equality with ordered matching | `"ba"` matches `"ab"` frequencies but is not that literal pattern | Preserve order if the statement requires it |
| Assume a historical maximum is the current maximum | `"AAAB"` after earlier `"AAAA"` can have historical maximum 4, actual maximum 3 | Use exact current frequencies for an exact-valid-window proof; treat stale summaries as a separate optimization |
| Infer O(n²) from nested syntax alone | A valid repair scan can remove each of n items once in total | Count movement and removal lifetimes over the whole scan |
| Claim O(n) while rescanning every live window | Increasing distinct array with K=n: rebuild sets of sizes 1..n | Update one live state and charge each validation operation |
| Use signed sums with an unjustified shrink rule | `[4,-1]` target 3, or `[1,-1,5]` threshold 5 | Supply the monotonicity proof or use an appropriate prefix-based method |
| Count only one exact-sum range per right end | `[0,0]` with target 0 has 3 nonempty ranges | Count matching earlier-prefix frequencies |
| Expire an index equal to left | `[9,1,2]`, k=3: index 0 is still valid and is the maximum | Expire index<left for [left,right) |
| Lose duplicate identity in a value-only deque | `[5,5,1]`, k=2: an older equal 5 is pruned, then an expiry-by-value may remove the newer 5 | Store indices and expire by position |
| Overflow an int sum | `[MAX,MAX]` sum becomes -2 in int | Use long accumulation and widen before intermediate arithmetic |
| Materialize every candidate String | A growing valid length-n range causes copied lengths 1..n | Keep boundaries and materialize only the requested final result |
| Sort before a question about original adjacency | `[3,1,2]` gives different length-2 sums after sorting | Preserve input order unless the contract permits redefining adjacency |

The intentionally buggy exact-sum code has a real algorithmic defect; it is not an alternative production implementation. The remaining witnesses isolate reasoning errors without introducing another exercise workbook.

### 15.2 A compact edge-case matrix

| Risk | Example and expected behavior |
| --- | --- |
| Empty fixed input | fixedWindowSums([],1) → [] |
| Fixed k exceeds n | fixedWindowSums([2,1],3) → [] |
| Invalid fixed k | k<=0 throws IllegalArgumentException |
| One full window | fixedWindowSums([2,1,5],3) → [8] |
| All-negative fixed sums | [-4,-2,-7], k=2 → [-6,-9] |
| Zero distinct budget | longestAtMostKDistinct([1,2],0) → (0,0) |
| All equal | longestAtMostKDistinct([8,8,8],1) → (0,3) |
| Shortest singleton | shortestAtLeast([0,0,5,0],5) → (2,3) |
| Impossible threshold | shortestAtLeast([2,3],6) → (-1,-1) |
| Repeated cover requirement | smallestCoverage([7,2],[7,7]) → (-1,-1) |
| Empty cover requirement | smallestCoverage([1,2],[]) → (0,0) |
| Empty anagram pattern | countAnagramWindows("ab","") → 3 |
| Pattern longer than text | countAnagramWindows("a","aa") → 0 |
| Rightmost maximum tie | maxWindowIndices([5,5,1],2) → [1,1] |
| Wide sum | fixedWindowSums([MAX,MAX],2) → [4294967294] |
| Wide range count | countExactSumRanges(100000 zeros,0) → 5000050000 |

Test invalid-input rejection separately from valid-input correctness. Negative values are valid for fixed sums, frequency windows, the deque, and prefix counting; they are excluded from the shortestAtLeast contract.

## 16. Pattern Recognition Practice and Cheat Sheet

### 16.1 Recognition cues are questions, not proof

| Statement cue | Candidate technique | Proof question before coding |
| --- | --- | --- |
| Exactly k contiguous items | Fixed rolling state | Can the outgoing contribution be removed efficiently? |
| Longest range with at most a count/kind budget | Variable longest window | Can extension ever repair a start I intend to discard? |
| A substring with the same inventory as a pattern | Fixed frequency window | Is multiplicity preserved, and is order irrelevant? |
| Shortest range containing required counts | Shortest covering window | Can removal create coverage, or only preserve/lose it? |
| Maximum/minimum in each moving fixed range | Monotonic deque, optional | Why is an older candidate permanently dominated or expired? |
| Exact signed sum, possibly many answers | Prefix map | Do I need membership, multiplicity, or earliest positions? |
| Many static sum queries | Prefix array | Is preprocessing worthwhile, and will the input remain unchanged? |
| Earlier buy then later sell | Running minimum / two-position scan | Does saved state contain only earlier prices? |

### 16.2 Small conceptual scenarios

Cover the cheat sheet and answer before opening the [recognition key](#17-conceptual-assessment-and-answer-key). For each, identify the output, a baseline, whether contiguity exists, whether size is fixed, and what justifies movement. These are discussion prompts rather than additional implementation assignments.

| ID | Scenario |
| --- | --- |
| R1 | Report the average of every five consecutive signed sensor readings |
| R2 | Find the longest contiguous event sequence containing at most three event types |
| R3 | Count substrings with the same letter inventory as `"aabc"` in lowercase text |
| R4 | Find the shortest run of events containing two arrivals of type 7 and one of type 2 |
| R5 | Count exact-sum subarrays in a signed array that may contain zero |
| R6 | Return each moving length-50 maximum, breaking value ties toward the newest index |
| R7 | Answer 100,000 specified range-sum queries over one unchanged array |
| R8 | Choose any three positions, not necessarily adjacent, whose values meet a sum condition |
| R9 | In chronological prices, maximize one later-minus-earlier difference |
| R10 | Find the shortest signed-array range whose sum reaches a threshold |

For R10, identify why the ordinary nonnegative-window proof fails; recognizing the need for additional reasoning is sufficient here. Do not force every scenario into a window.

**Self-check Q28:** Which evidence distinguishes a justified window algorithm from a guess based on the word “substring”?

## 17. Conceptual Assessment and Answer Key

Use Q1-Q28 at the ends of earlier sections as a no-notes oral assessment. Explain a concrete example before giving a general statement. For any unclear answer, return to its section and then test a changed example.

<details>
<summary>Concise answers to Q1-Q14</summary>

| Question | Answer |
| --- | --- |
| Q1 | Prove that boundary movement does not discard a potentially better feasible candidate |
| Q2 | Sorting changes adjacency, so original contiguous ranges are not preserved |
| Q3 | After the invalidity-repair loop; counts remain exact during temporary invalidity |
| Q4 | It has recorded valid candidates and stops once further contraction cannot stay valid |
| Q5 | Fixed movement visits every length-k range; it does not use sum direction to skip starts |
| Q6 | It retains a complete intermediate long[] in addition to its double[] output |
| Q7 | Membership loses multiplicity; aab and abb use the same kinds but different counts |
| Q8 | The active length is m and every frequency difference is zero |
| Q9 | One outgoing occurrence may leave its kind present, or multiple kinds/occurrences may need removal |
| Q10 | Only the incoming character's count increased; every other count was already within its limit |
| Q11 | No; it may be larger than the current maximum, so a separate proof is needed |
| Q12 | The current valid candidate might be the optimum, and removal may destroy it |
| Q13 | Even if sum stays constant, left strictly advances and cannot pass right while positive-threshold validity holds |
| Q14 | Three required occurrences, two required kinds |

</details>

<details>
<summary>Concise answers to Q15-Q28</summary>

| Question | Answer |
| --- | --- |
| Q15 | size counts mappings including zero-valued entries; it equals distinct live kinds only after zero cleanup |
| Q16 | No; Character represents one UTF-16 code unit, so a surrogate pair is two keys |
| Q17 | The inner loop shares a forward-only boundary; total advances, not maximum work per outer step multiplied blindly, determine the bound |
| Q18 | Rebuild a histogram or copy/scan the growing range at every end; sizes can sum to Θ(n²) |
| Q19 | After sum falls below the threshold, removing a negative value can restore validity; shorter suffixes are not all invalid |
| Q20 | Fixed-length signed sums, signed-value distinct counts, or moving maxima; their proof does not depend on sums increasing with extension |
| Q21 | Initial zero counts starts at 0; querying before insertion uses only earlier boundaries and excludes empty intervals |
| Q22 | Several earlier equal prefixes produce different matching starts for one end |
| Q23 | It is no smaller, expires later, and wins the required rightmost tie |
| Q24 | left is included in [left,right), so its index has not expired |
| Q25 | Operand types determine arithmetic before assignment; cast an operand before addition/subtraction/multiplication |
| Q26 | Pruning may inspect an empty deque; after insertion the incoming index survives and supplies a front |
| Q27 | For each selling day, the smallest earlier price maximizes that day's difference; scanning all selling days covers the optimum |
| Q28 | An exact state invariant plus a proof that extension/removal rules retain every necessary candidate, with a progress and cost argument |

</details>

<details>
<summary>Recognition answers R1-R10</summary>

| Scenario | Appropriate direction and key assumption |
| --- | --- |
| R1 | Fixed rolling sum then divide by 5; signed values do not affect enumeration |
| R2 | Longest at-most-three-distinct window; extension cannot repair excess kinds |
| R3 | Fixed frequency matching of length 4; duplicate a counts twice |
| R4 | Shortest covering window; record before removing required occurrences |
| R5 | Prefix frequencies; a one-window signed-sum rule lacks monotonicity |
| R6 | Monotonic deque of indices; remove expired and older no-better candidates |
| R7 | Prefix array; independent queries need not follow one moving interval |
| R8 | The output concerns noncontiguous choices, so a standard contiguous window does not directly represent candidates; compare suitable pair/anchor/hashing methods |
| R9 | Running earlier minimum; no generic frequency or shrink loop is necessary |
| R10 | Ordinary nonnegative-threshold shrinking is unjustified; consider prefix order/dominance reasoning, with more advanced techniques outside this core guide |

</details>

These answers are short checks, not substitutes for deriving the implementations and correctness arguments. A memorized answer without a valid changed example is incomplete understanding.

## 18. Key Takeaways and Interview Explanation

A reliable window method has a precise contract, an exact maintained state, a justified movement rule, and an explicit answer-update point. Fixed windows enumerate every legal start. Longest-valid windows typically repair invalidity before recording. Shortest-valid windows typically record valid candidates while contracting. Each rule depends on a property of the actual condition.

Frequency state represents occurrences, distinct kinds, or satisfied requirements according to its definition. These quantities must not be interchanged. Alphabet assumptions determine whether a frequency array is valid, and the character model determines what indices and lengths mean.

Linear endpoint travel is only part of a complexity proof. Include per-update work, validity scans, allocation, preprocessing, output, hashing assumptions, and optional deque operations. Negative values require examining the predicate; they are neither a universal prohibition nor a universal permission.

For an interview, explain the reasoning in this order:

1. “The output is a contiguous range with this exact validity/tie/absence contract.”
2. “The baseline repeats this work and costs this much.”
3. “My state describes [left,right) in these terms.”
4. “This extension/removal property justifies discarding this start or keeping this candidate.”
5. “I record the answer here because the state is valid and the objective is longest/shortest.”
6. “The endpoints or candidate lifetimes give this total operation count; these are the state and output costs.”
7. “These edge tests target the assumptions, and this changed input would require another method.”

Use your own words and the problem's actual invariant rather than reciting this structure.

## 19. References, Validation, and Next Steps

### 19.1 Repository references

- [Roadmap: Sliding Window](./DSA_Interview_Roadmap.md#5-sliding-window) — required concepts, implementation drills, priorities, and canonical attempt Logs.
- [Roadmap: learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) and [progress gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) — separate theory, implementation, practice, and readiness.
- [Roadmap: spaced review](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition) — approximate 1/3/7/14-day cold reviews, adjusted to retention.
- [Java Arrays](./Java_Array.md) and [copying arrays](./Java_Array.md#copying-arrays) — indexing, mutation, and allocation.
- [Java Strings](./Java_String.md) — UTF-16, substring, comparison, and StringBuilder costs.
- [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) — counts, membership, and hash assumptions.
- [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) — histograms and prefix representations.
- [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements) — movement and termination proofs.
- [Stack](./Stack/Stack_Essential_Concepts.md#8-monotonic-stack-fundamentals) — dominance and candidate lifetimes.
- [Binary Search](./Binary_Search_Essential_Concepts.md#10-search-space-invariants-and-correctness) — preconditions and justified elimination.
- [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) — complete time and auxiliary-space analysis.
- [Testing concepts](./Testing_Essential_Concepts.md) — contracts, boundary cases, and independent oracles.

### 19.2 Official Java 21 sources

API details were checked using Context7 against Oracle's Java 21 documentation; direct pages were consulted where retrieved excerpts were too broad.

| Source | Relevant contract |
| --- | --- |
| [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) | UTF-16 indices, length, charAt, substring, and code-point APIs |
| [CharSequence](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/CharSequence.html) | codePoints combines valid surrogate pairs |
| [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) | Mapping operations and hashing-dependent costs |
| [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html) | getOrDefault, put, remove, and size semantics |
| [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) | Endpoint operations, null policy, and amortized costs |
| [Deque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html) | Front/back peek and poll behavior |

Algorithmic invariants, counterexamples, and cost derivations are specific to the implementations and contracts in this guide. The [official NeetCode roadmap](https://neetcode.io/roadmap) supplies category context; this repository's roadmap remains authoritative for local E/A/C choices.

### 19.3 Validation scope

All **13 Java blocks** compiled together with `javac --release 21 -Xlint:all -Werror`. A temporary Java runner passed **1,276,729 checks**, including exhaustive tests over 19,531 signed arrays, 1,093 lowercase texts and 40 patterns, 1,000 seeded randomized trials, Unicode/code-unit examples, and 100,000-element edge cases. Independent brute-force oracles checked rolling sums, averages, distinct/copy limits, shortest nonnegative thresholds, repeated coverage, anagram counts, exact signed-sum counts, and rightmost maximum indices. Invalid-parameter rejection was checked separately.

A separate document-based audit reconstructed all **57 rows across nine main dry-run tables**. Additional witnesses verified zero-key retention, wrong deque expiry, signed-sum shrinking, overflow, alphabet mismatch, and stale frequency summaries. The deliberately buggy exact-sum method's false negative was reproduced; its inclusion is intentional. The signed shortest-threshold counterexample was checked explicitly as a violation of that method's precondition.

All **54 local links and section anchors** resolved. Content hashes confirmed all **21 pre-existing files in DSA_Guide** were unchanged, and this guide was the only added file there. Validation programs and compiled classes stayed in a temporary directory outside the repository.

These checks cover the stated examples and bounded input domains; they do not prove future modifications correct or mark learning progress. Code-point conversion is demonstrated, while full grapheme segmentation/normalization and the signed shortest-threshold algorithm are outside the core scope. Hash-based costs remain qualified by their stated assumptions; actual JVM allocation/timing was not benchmarked.

### 19.4 Next steps using the existing roadmap

Read one pattern, trace its state, close the guide, and implement a changed example from a blank editor. Begin with the roadmap's fixed-sum and distinct-character drills. Then attempt the three E problems independently, use the A problem for a frequency-budget gap, and investigate C problems after core reasoning is reliable.

Use the existing problem Log for I/H/S results and next review dates. On a cold review, hide old code and notes, derive the baseline and invariant again, test, and explain costs aloud. Reviews around 1,3,7,14 days are adjustable memory aids. Prioritize unfinished/S work, then H work and unresolved boundary errors; keep one record per problem.

For readiness, derive an unfamiliar substring variant without topic hints, explain why shrinking is safe, handle repeated requirements/impossible coverage/minimal ranges, and identify a case that needs another algorithm. Demonstrate this repeatedly across separate sessions under the common rubric. Optional Hard completion is not required for ordinary readiness.




