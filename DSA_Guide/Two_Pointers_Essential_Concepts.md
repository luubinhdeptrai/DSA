# Two Pointers — Essential Concepts (Java 21)

> A pointer move is a claim: the work you leave behind is already finished, impossible, or unable to improve the answer. Learn to justify that claim before writing the update.

## Table of Contents

- [1. Purpose, Prerequisites, and Roadmap Scope](#1-purpose-prerequisites-and-roadmap-scope)
- [2. Two Pointers Fundamentals](#2-two-pointers-fundamentals)
- [3. Opposite-End Pointers](#3-opposite-end-pointers)
- [4. Same-Direction Read/Write Pointers](#4-same-direction-readwrite-pointers)
- [5. Coordinated Scans Across Two Sequences](#5-coordinated-scans-across-two-sequences)
- [6. Fixed Anchor Plus Two Pointers](#6-fixed-anchor-plus-two-pointers)
- [7. Dominated-Boundary Elimination](#7-dominated-boundary-elimination)
- [8. Optional Advanced Boundary-Maximum Reasoning](#8-optional-advanced-boundary-maximum-reasoning)
- [9. A Method for Proving Pointer Movements](#9-a-method-for-proving-pointer-movements)
- [10. Deriving Time and Space Complexity](#10-deriving-time-and-space-complexity)
- [11. Java 21 Implementation Decisions](#11-java-21-implementation-decisions)
- [12. Choosing Between Techniques](#12-choosing-between-techniques)
- [13. Pattern Recognition Practice](#13-pattern-recognition-practice)
- [14. Common Mistakes and Edge Cases](#14-common-mistakes-and-edge-cases)
- [15. Conceptual Self-Assessment and Answer Key](#15-conceptual-self-assessment-and-answer-key)
- [16. Key Takeaways and Interview Explanation](#16-key-takeaways-and-interview-explanation)
- [17. References, Validation, and Next Steps](#17-references-validation-and-next-steps)

## 1. Purpose, Prerequisites, and Roadmap Scope

This guide teaches the [Two Pointers category](./DSA_Interview_Roadmap.md#2-two-pointers) in the repository roadmap. The roadmap defines scope and practice priorities. The goal is to derive an algorithm, explain each movement, implement it safely in Java 21, and adapt it when a requirement changes.

Read [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations) as prerequisites and [Phase 1](./DSA_Interview_Roadmap.md#phase-1-array-reasoning-and-linear-patterns) for context. You should already understand array/string bounds, sorted order, hashing, basic Big O, and a processed-prefix invariant. A pointer here is an **index or position**, not a Java memory address.

| Roadmap requirement | Where this guide develops it | Evidence to produce independently |
| --- | --- | --- |
| Distinguish opposite-end, read/write, and two-sequence scans | Sections 2–5 | Explain what each position represents and what remains unresolved |
| Prove a move cannot discard a valid or better answer | Sections 3, 5, 7, 9 | Give the inequality, obligation, or dominance argument |
| Explain sorting and original positions | Sections 3, 6, 11–12 | Separate sorted positions from original indices and count preprocessing |
| Skip duplicates at the correct pair/triple level | Sections 4 and 6 | Distinguish value-unique output from index combinations |
| Separate mutation, logical length, and allocated output | Sections 4–5 and 10–11 | State which positions are valid and which arrays exist |
| Implement sorted pair search and stable chosen-value removal | Sections 3–4 | Rebuild the mechanisms from a blank editor and test them |
| Check filtered palindromes without a cleaned copy | Section 3 | Handle skipped symbols, case, empty results, and crossing bounds |
| Understand anchor scans and limiting boundaries | Sections 6–7 | Explain quadratic anchor work and linear boundary elimination |
| Explore two boundary maxima, optionally | Section 8 | Explain why one position's water depth can be finalized |
| Apply the common and topic-specific readiness rubric | Sections 13–17 | Select a method on an unfamiliar statement with references closed |

### Reuse existing material

| Reference | Reuse | New emphasis here |
| --- | --- | --- |
| [Java Arrays](./Java_Array.md) | Indexing, mutation, copying, sorting, printing | Safe writes, ordered elimination, logical output length |
| [Java Strings](./Java_String.md) | Immutability, UTF-16, indexed access, conversion costs | Filtered comparison and the character model |
| [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Loop sums, auxiliary space, hidden library costs | Total pointer travel and repeated per-anchor scans |
| [Arrays & Hashing — Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Processed prefixes, complements, counts, output conventions | Replace a stored lookup history with an ordering or partition proof when possible |

The Arrays & Hashing guide resides in the `Arrays_and_Hashing` subdirectory; links here use that discovered path.

### Preserve the practice priorities

These entries match the local roadmap; the labels are its curriculum choices. Read the linked original for current constraints and the required judge interface.

| Priority | Problem | Difficulty | Learning purpose |
| --- | --- | --- | --- |
| E — Essential | [Valid Palindrome](https://leetcode.com/problems/valid-palindrome/) | Easy | Filtered opposite-end comparison |
| E — Essential | [Two Sum II - Input Array Is Sorted](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/) | Medium | Sorted pair elimination |
| E — Essential | [3Sum](https://leetcode.com/problems/3sum/) | Medium | Fixed anchors and duplicate suppression |
| A — Additional | [Container With Most Water](https://leetcode.com/problems/container-with-most-water/) | Medium | Dominated-boundary elimination |
| C — Optional advanced challenge | [Trapping Rain Water](https://leetcode.com/problems/trapping-rain-water/) | Hard | Two boundary maxima |

The code uses teaching interfaces: boolean pair existence, a filtered string range, logical array prefixes, merged output, a threshold triple count, a boundary-score witness, and a per-position water profile. In particular, the triple-count routine is **not** a unique-triplet enumeration solution. Derive the roadmap's exact practice interfaces yourself.

Reading and explaining can support T; blank-editor implementation supports I; independent Essential solves or cold re-solves support P; unfamiliar transfer supports R. Use the [existing learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) and roadmap records. This guide adds no second progress dashboard or review ledger.

## 2. Two Pointers Fundamentals

### 2.1 The idea: coordinated positions with a reason to advance

Imagine checking a row of books. You might compare the two end covers, inspect books with one hand while packing accepted books with the other, or combine the smallest remaining books from two ordered stacks. Each activity uses positions for different purposes.

Two Pointers coordinates positions so that each step finishes useful work or removes a justified part of the candidate space. Merely writing two index variables does not establish a useful algorithm. The saving comes from **avoiding work you can prove is unnecessary**.

```text
Opposite ends:      [ left ->  unresolved  <- right ]
Read/write:        [ accepted | write ... read -> unread ]
Two sequences:     A: [ consumed | first -> remaining ]
                   B: [ consumed | second -> remaining ]
Fixed anchor:      [ anchor | left -> ... <- right ]
```

| Pattern | Positions mean | Progress measure | Typical justification |
| --- | --- | --- | --- |
| Opposite-end comparison | Next unmatched left/right symbols | Remaining interval shrinks | Matching ends complete an obligation |
| Sorted pair search | Smallest/largest unresolved candidate positions | `right - left` decreases | Sorted inequalities eliminate a row or column of pairs |
| Read/write | Next unread input; next output slot | `read` advances | Accepted-prefix invariant and `write <= read` |
| Coordinated sequences | First unconsumed item in each input | Total unconsumed items decreases | The next required item must be at one of the heads |
| Fixed anchor + pair scan | One chosen item plus a remaining pair | Inner interval shrinks for each anchor | Fixing one dimension makes ordered elimination possible |
| Dominated boundary | Two candidate boundaries | Candidate interval shrinks | Discarded boundary cannot beat an already evaluated score |

Read/write positions may coincide and never need to cross. Positions in separate arrays cannot meaningfully be compared to each other: `first < second` says nothing about which sequence is exhausted. Converging pair search requires distinct positions, so it normally uses `left < right`. A per-position settlement algorithm may legitimately process `left == right`. The contract determines the loop condition.

### 2.2 Start with a baseline, not a preferred pattern

Ask what a simple correct solution would inspect. A pair search has n(n−1)/2 index pairs. Stable filtering can copy accepted values into another array. A merge can concatenate and sort. A water-depth calculation can scan both sides of every position.

Then ask:

1. What repeated work or extra storage is unnecessary?
2. Does ordering constrain how the answer changes?
3. Can a region be ruled out, or an output position be finalized?
4. Which invariant will still hold after moving?
5. What quantity decreases so the loop must end?

Sometimes the benefit is O(n²) to O(n) time; sometimes it is O(n) extra storage to O(1), with the same linear time. Sometimes sorting costs O(n log n) first. An outer anchor loop can make a pointer method O(n²). Use the actual work, not the pattern name, to derive complexity.

### 2.3 Shared Java contracts

All methods below are static members of one class. Put `import java.util.Arrays;` above that class. Nested records belong inside it too. Combine the complete `java` blocks in document order; no external dependency is needed.

Inputs are non-null, used in one thread, and allocations are feasible. Domain assumptions such as nondecreasing order, ASCII text, or nonnegative capacities are preconditions, not silently checked by every example. Only the methods explicitly described as mutating may change input. Numeric operations use a fixed-width model; int/long work is O(1).

Auxiliary space here **excludes required returned output**, which is reported separately, following the Arrays & Hashing guide. Copies made only for preprocessing are auxiliary. A linear returned array plus constant local state has O(1) auxiliary space and O(n) output; it still needs O(n) additional memory overall. Read an O(n) bound as O(n+1) when including empty-input constant work.

**Self-check F1:** What additional fact, beyond “I have two indices,” makes the method useful?

**Self-check F2:** Why can two pointer variables inside a loop still lead to quadratic time?

## 3. Opposite-End Pointers

### 3.1 Sorted pair search: derive the movement

**Problem.** In a nondecreasing int array, decide whether two **distinct positions** have a mathematical sum equal to a long target. A boolean answer needs no particular pair ordering. Duplicates and negatives are valid; empty/singleton arrays return false.

The baseline checks all i < j: O(n²) time and O(1) auxiliary space. Sorting is already supplied in this problem. The useful observation is that order lets one comparison rule out many pairs.

```text
values = [ -3,  -1,   2,   4,   6 ]    target = 5
            L                       R   sum = 3, too small

For this L, every remaining partner <= 6.
Even its largest possible sum is too small: discard L entirely.
```

Suppose the current candidates lie between left and right:

- If `a[left] + a[right] < target`, then for every `k <= right`, `a[left] + a[k] <= a[left] + a[right] < target`. No remaining pair using left can work. Advance left.
- If the sum is too large, then for every `k >= left`, `a[k] + a[right] >= a[left] + a[right] > target`. No remaining pair using right can work. Decrease right.
- If equal, the current two distinct indices prove existence.

The claims only concern unresolved candidates; previously removed candidates have already been proved impossible. “Move left to increase the sum” is useful intuition, but **eliminating all partners for that endpoint** is the correctness proof. Values may repeat, so a move need not strictly change the sum.

```java
static boolean sortedPairExists(int[] values, long target) {
    int left = 0;
    int right = values.length - 1;
    while (left < right) {
        long sum = (long) values[left] + values[right];
        if (sum == target) {
            return true;
        }
        if (sum < target) {
            left++;
        } else {
            right--;
        }
    }
    return false;
}
```

No sorting occurs inside the method; the input must already be nondecreasing. Casting one operand before addition prevents int overflow. Every sum of two ints fits long, even if the target lies outside that sum range.

### 3.2 Dry runs: success, failure, and crossing

Success on `[-3,-1,2,4,6]`, target 5:

| Step | left, right before | Endpoint values | Sum | Decision | Positions after |
| --- | --- | --- | --- | --- | --- |
| 1 | 0, 4 | -3, 6 | 3 | Discard index 0 | 1, 4 |
| 2 | 1, 4 | -1, 6 | 5 | Found distinct positions | Return true |

Failure on `[1,2,4,8]`, target 7:

| Step | left, right before | Sum | Safe elimination | Positions after |
| --- | --- | --- | --- | --- |
| 1 | 0, 3 | 9 | Every remaining pair using 8 is too large | 0, 2 |
| 2 | 0, 2 | 5 | Every remaining pair using 1 is too small | 1, 2 |
| 3 | 1, 2 | 6 | Every remaining pair using 2 is too small | 2, 2 |
| Stop | 2, 2 | Not evaluated | Only one position remains | Return false |

The guard stops when pointers meet; it need not wait until they cross. A loop that advances both positions after a match, such as palindrome checking, can cross directly. `[3,3]` with target 6 is true; `[3]` is false. Equal **values** are legal, reusing one **index** is not.

### 3.3 Correctness, cost, and limits

**Invariant:** if a valid pair exists and has not been returned, some valid pair lies entirely inside `[left,right]`. Initially that interval contains every position. Each inequality above removes only an impossible endpoint, preserving the invariant. Every non-returning iteration reduces `right-left` by one; there are at most n−1 such steps for n ≥ 1. If the pointers meet, no two distinct positions remain, so false is correct. Time is O(n), auxiliary space O(1), output a boolean; the input is unchanged.

Recognition clues are an ordered sequence, a pair relation monotone in each selected value, and a question such as existence or a carefully defined count. This exact routine is not an all-pairs enumerator. Enumerating index pairs may require quadratic output; counting pairs with duplicates requires a separate multiplicity argument.

The method is invalid on an arbitrary unsorted array. `[3,2,4]`, target 6 starts with sum 7 and discards 4, then fails, although 2+4 works. On `[1,2,4,8]`, target 10, moving right after a too-small sum 9 loses the valid 2+8 pair. For target 6, moving left after a too-large sum 9 also loses 2+4. Do not infer a movement rule solely from the desired numerical direction without proving what is discarded.

If sorting is needed, total time becomes O(n log(n+1)) plus O(n) scan; memory includes any copy and sort workspace. Sorting changes positions. If an unsorted problem requires original indices, consider hashing or value/index records; a sorted position is not an original index. [Section 11](#11-java-21-implementation-decisions) develops that tradeoff.

**Self-check P1:** State the inequality that rules out every remaining partner for left when the sum is too small.

**Self-check P2:** Why does a negative number or an equal neighboring value not invalidate the proof, while unsorted order does?

### 3.4 Filtered palindrome checking: finish mirrored obligations

**Problem.** Check a valid half-open string range `[from,to)` as a case-insensitive palindrome after ignoring non-alphanumeric symbols. The selected range contains only ASCII code units; empty ranges and ranges with no retained symbols are palindromes. The surrounding string is not examined. This teaches the roadmap drill with a range interface and no cleaned copy.

A straightforward baseline builds a filtered lowercase string, then compares it with its reversal: O(k) time and O(k) auxiliary space for k selected code units. It is already linear. The improvement is avoiding the temporary representation. Repeatedly stripping ends with new substrings could instead copy O(k²) total characters.

Read the text like checking a word written on a folded strip: the first meaningful symbol must match the last, then the next must match the next-last. Ignored punctuation contributes no obligation. Unlike pair sums, this method needs **no sorted order**.

1. Place left at from and right at to−1.
2. Skip ignored symbols while the positions are distinct.
3. If no distinct symbols remain, succeed.
4. Compare the two meaningful symbols after ASCII lowercasing; a mismatch cannot be repaired by moving inward.
5. After a match, both symbols' obligations are complete, so advance both ends.

```java
static boolean mirroredAsciiRange(String text, int from, int to) {
    if (from < 0 || from > to || to > text.length()) {
        throw new IndexOutOfBoundsException("invalid half-open range");
    }
    int left = from;
    int right = to - 1;
    while (left < right) {
        while (left < right
                && !Character.isLetterOrDigit(text.charAt(left))) {
            left++;
        }
        while (left < right
                && !Character.isLetterOrDigit(text.charAt(right))) {
            right--;
        }
        if (left >= right) {
            break;
        }
        char leftSymbol = Character.toLowerCase(text.charAt(left));
        char rightSymbol = Character.toLowerCase(text.charAt(right));
        if (leftSymbol != rightSymbol) {
            return false;
        }
        left++;
        right--;
    }
    return true;
}
```

The guard occurs before each dereference. The method checks bounds, but trusts the ASCII precondition. The [Java 21 Character API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html) has broader Unicode classification rules; using its char overloads does not establish a full-Unicode palindrome contract.

Dry run for `"A, b-a"`, range `[0,6)`:

```text
index:  0 1 2 3 4 5
text:   A , _ b - a       _ denotes the space
        L         R
```

| Step | Positions before | Skips/comparison | Positions after | Meaning |
| --- | --- | --- | --- | --- |
| 1 | 0, 5 | `A` and `a` match after lowercasing | 1, 4 | Outer meaningful pair finished |
| 2 | 1, 4 | Left skips comma and space to 3; right skips hyphen to 3 | 3, 3 | Only center `b` remains |
| Stop | 3, 3 | No pair remains | Return true | Retained sequence is `aba` |

For `"aa"`, the matching pair moves `(0,1)` to `(1,0)`: crossing is safe because the next guard fails. `"ab"` fails at the first comparison. `"--"` skips to meeting and succeeds. `"xA-aY"` with `[1,4)` succeeds for `A-a`, while `[0,5)` fails. Empty `[0,0)` never accesses the negative initial right index.

**Invariant:** previously consumed meaningful symbols have matched their mirror partners; all remaining comparison obligations lie between left and right. Skipping an ignored symbol removes no meaningful item. Matching ends removes one required pair. A mismatch disproves the definition immediately. At meeting/crossing at most one meaningful symbol remains, which matches itself.

For k selected code units, each position advances in only one direction and crosses each code unit at most once. The two inner skip loops plus the outer loop have O(k) total work, not O(k²). Case conversion/classification is constant work for this stated domain. Auxiliary space is O(1), output boolean, and the string remains unchanged.

Common mistakes are accessing after the pointers cross, forgetting to advance after a match, removing meaningful mismatches instead of returning false, or allocating substrings while claiming constant space. Do not use the method for “can delete one symbol” without changing the state and proof. General Unicode, grapheme clusters, normalization, and language-sensitive case folding need a separately specified character model; see [Section 11](#11-java-21-implementation-decisions).

**Self-check M1:** Why may punctuation be skipped, while unequal retained symbols cannot simply be skipped?

**Self-check M2:** How do several nested skip loops still perform only linear work?

## 4. Same-Direction Read/Write Pointers

### 4.1 Stable removal: two roles, one array

Read/write pointers divide the array into a valid output prefix, already inspected input, and unread input. Think of inspecting tickets at one position and packing accepted tickets into the next free slot. `read` always advances; `write` advances only when an item belongs in the output.

**Problem.** Remove every occurrence of `removedValue` from an int array in place, preserving the order of retained values. Return the logical retained length. The physical array length is unchanged; elements after that logical length are unspecified. Empty arrays, arbitrary int values, and all-removed/all-retained cases are valid.

The baseline copies retained values to another array: O(n) time and O(n) extra storage. Repeatedly shifting a suffix after each removal can take O(n²). We can match the first baseline's time while using constant auxiliary space because the output never gets ahead of unread input.

```text
before processing read:
[ correct retained prefix | inspected leftovers | unread input ]
0                       write                 read             n

write <= read: a write cannot destroy a future unread value.
```

```java
static int removeValueStable(int[] values, int removedValue) {
    int write = 0;
    for (int read = 0; read < values.length; read++) {
        int current = values[read];
        if (current != removedValue) {
            values[write] = current;
            write++;
        }
    }
    return write;
}
```

Dry run for `[2,5,2,-1,0,2]`, remove 2:

| read | Current | write before | Action | write after | Entire physical array after action |
| --- | --- | --- | --- | --- | --- |
| 0 | 2 | 0 | Reject | 0 | `[2,5,2,-1,0,2]` |
| 1 | 5 | 0 | Write index 0 | 1 | `[5,5,2,-1,0,2]` |
| 2 | 2 | 1 | Reject | 1 | `[5,5,2,-1,0,2]` |
| 3 | -1 | 1 | Write index 1 | 2 | `[5,-1,2,-1,0,2]` |
| 4 | 0 | 2 | Write index 2 | 3 | `[5,-1,0,-1,0,2]` |
| 5 | 2 | 3 | Reject | 3 | `[5,-1,0,-1,0,2]` |

Return 3; the answer is only `[5,-1,0]`. Repeated or removed values in the suffix are irrelevant. No new array was returned, and clearing the suffix is not required.

**Invariant before read:** `[0,write)` contains exactly the retained original values from `[0,read)`, in original order, and future unread positions are intact. Initially both prefixes are empty. Rejection leaves the retained prefix unchanged. Acceptance appends the current value; `write <= read` protects future input. Each iteration then advances read. At n, all original elements have been considered, so write is the required length.

There are n reads and at most n writes: O(n) time, O(1) auxiliary space, scalar output plus mutated input. Stability follows from appending in read order. With `[2,2]` removing 2, return 0; with `[]`, return 0; with `[0,-1]` removing 2, return 2 and preserve those values.

Recognition clues: stable filtering, allowed mutation, and an output no longer than the input. Avoid this exact method when the original must remain available, when writes could exceed reads, or when a filtering rule depends on overwritten earlier values without retained state. If stability is unnecessary, swapping rejected items with the end can be another design, with a different invariant and output order.

The [Arrays & Hashing processed-prefix example](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#3-traversal-transformation-and-processed-prefixes) used the same mechanism with a nonnegative predicate. The new point is recognizing it as the same-direction Two Pointers family rather than a new unrelated trick.

### 4.2 Remove adjacent duplicate runs from sorted input

**Problem.** In a nondecreasing array, keep one copy of every distinct value in an ascending logical prefix. Return its length; the tail is unspecified. Sorting is a precondition, and this method mutates the input.

Repeated deletion/shifting can be quadratic. A set can detect distinct values with O(u) state but is unnecessary here: sorted duplicates are contiguous, so only the last accepted value is needed.

```java
static int uniqueSortedPrefix(int[] values) {
    int write = 0;
    for (int read = 0; read < values.length; read++) {
        if (write == 0 || values[read] != values[write - 1]) {
            values[write] = values[read];
            write++;
        }
    }
    return write;
}
```

For `[-2,-2,0,0,0,5]`:

| read | Current | Accepted prefix before | Decision | Accepted prefix after |
| --- | --- | --- | --- | --- |
| 0 | -2 | `[]` | First distinct value | `[-2]` |
| 1 | -2 | `[-2]` | Same as last accepted | `[-2]` |
| 2 | 0 | `[-2]` | Append at index 1 | `[-2,0]` |
| 3 | 0 | `[-2,0]` | Skip | `[-2,0]` |
| 4 | 0 | `[-2,0]` | Skip | `[-2,0]` |
| 5 | 5 | `[-2,0]` | Append at index 2 | `[-2,0,5]` |

Return 3; the physical result is `[-2,0,5,0,0,5]`. For `[7,7,7]`, return 1 with prefix `[7]`; for `[]`, return 0 without reading `values[-1]`.

The processed-prefix invariant now says the output holds each distinct value of the original processed prefix exactly once. Because all copies of a sorted value are adjacent, a current value equal to the last accepted one adds nothing, while a different value starts a new run. The same `write <= read` argument protects unread input. Time is O(n), auxiliary space O(1), output a logical prefix plus scalar length.

On unsorted `[1,2,1]`, this code returns length 3: it only suppresses neighboring equal runs, not arbitrary repetitions. If multiplicities matter, removing them changes the task. For “keep at most two copies,” the acceptance invariant must be redesigned; do not reuse “one distinct value” logic unchanged.

**Self-check W1:** Why is `write <= read` sufficient to protect future input but not, by itself, sufficient to prove correct filtering?

**Self-check W2:** What property lets deduplication remember only the last accepted value, and what fails without it?

## 5. Coordinated Scans Across Two Sequences

### 5.1 Merge by finishing the next output position

**Problem.** Given two nondecreasing int arrays, return one ascending array containing every occurrence from both. Preserve both inputs; equal values appear as many times as they occur in total. Empty inputs are valid. Their combined length must be representable and feasible to allocate.

The baseline concatenates and sorts: O((n+m) log(n+m+1)) time, output/copy storage, and sort workspace. This ignores that each source already has a known smallest unconsumed value. Scan those heads instead.

```text
first:   [ 1 | 4 4 ]       firstIndex  = 1
second:  [ 2 | 4 7 ]       secondIndex = 1
output:  [ 1 2 | ... ]     next item is one of the two visible 4s
```

The smallest remaining item in a sorted sequence must be at its head. Therefore the smallest item across both remaining sequences must be the smaller head. Emit it and advance only its source. When one source is exhausted, every item of the other can be copied in order.

```java
static int[] mergeSorted(int[] first, int[] second) {
    int totalLength = Math.addExact(first.length, second.length);
    int[] merged = new int[totalLength];
    int firstIndex = 0;
    int secondIndex = 0;
    int write = 0;
    while (firstIndex < first.length && secondIndex < second.length) {
        if (first[firstIndex] <= second[secondIndex]) {
            merged[write] = first[firstIndex];
            firstIndex++;
        } else {
            merged[write] = second[secondIndex];
            secondIndex++;
        }
        write++;
    }
    while (firstIndex < first.length) {
        merged[write] = first[firstIndex];
        firstIndex++;
        write++;
    }
    while (secondIndex < second.length) {
        merged[write] = second[secondIndex];
        secondIndex++;
        write++;
    }
    return merged;
}
```

There are two source pointers and one destination position; “Two Pointers” does not prohibit an output index. Choosing first on equality gives a defined source tie preference. For primitive ints, equal occurrences have no observable identity; a corresponding record merge could preserve that source order explicitly.

### 5.2 Dry run and correctness

Merge `[1,4,4]` and `[2,4,7]`:

| Step | firstIndex, secondIndex before | Available heads | Decision | Source indices after | Output prefix |
| --- | --- | --- | --- | --- | --- |
| 1 | 0, 0 | 1, 2 | Emit first 1 | 1, 0 | `[1]` |
| 2 | 1, 0 | 4, 2 | Emit second 2 | 1, 1 | `[1,2]` |
| 3 | 1, 1 | 4, 4 | Emit first 4 | 2, 1 | `[1,2,4]` |
| 4 | 2, 1 | 4, 4 | Emit first 4 | 3, 1 | `[1,2,4,4]` |
| 5 | 3, 1 | First exhausted | Copy second 4 | 3, 2 | `[1,2,4,4,4]` |
| 6 | 3, 2 | Second 7 remains | Copy second 7 | 3, 3 | `[1,2,4,4,4,7]` |

**Invariant:** the output prefix is a sorted merge of exactly the consumed source prefixes, and its values are no larger than the unconsumed heads. Initially all prefixes are empty. Emitting the smaller head is safe because no hidden later item can be smaller; it extends the correct merge. Each emission consumes exactly one input occurrence, with `write = firstIndex + secondIndex`. Once both inputs are exhausted, every occurrence has been output once.

Each source pointer advances at most its source length. Every iteration, including the tail loops, emits one of n+m items. Time is O(n+m), output O(n+m), auxiliary space O(1) beyond that preallocated output. The loops are sequential phases of the same bounded travel, not multiplied costs. `Math.addExact` reports a combined length that cannot fit int; feasible memory is still a separate precondition.

### 5.3 Related scans and when ordering is essential

This family applies when each sequence's next candidate carries enough information to decide what to consume. For sorted multiset intersection: on unequal heads discard the smaller head, which cannot match any later value in the other sequence; on equality emit once and advance both. That yields min(countA,countB) copies. A set intersection that emits each distinct value once needs additional duplicate skipping. The output contract changes the updates.

For subsequence comparison, the positions instead mean “next required symbol” and “next available symbol.” They need not be sorted: matching consumes both, while a nonmatch consumes only the available sequence. That movement follows order-preserving matching, not numeric inequalities. It is a related two-sequence pattern, not the merge rule applied blindly.

Avoid sorted-head elimination for unordered intersection without preprocessing. Avoid indexed traversal on a `LinkedList` when each `get(i)` can itself traverse; arrays give the constant-time access assumed above. A merge into unused capacity of an existing first array also needs a different write-direction argument to avoid overwriting unread values. A fresh output array makes aliasing straightforward here.

Edge cases: merging two empty arrays returns `[]`; `[2,2]` with `[2]` returns `[2,2,2]`; `[-3,0]` with `[-2,4]` returns `[-3,-2,0,4]`. Missing a tail loop loses valid values even if the main loop is correct. Advancing both heads after unequal comparison loses an unconsumed value.

**Self-check S1:** Why can an item behind a source head never be the next smaller output item?

**Self-check S2:** Why does `while (firstIndex < secondIndex)` have no useful exhaustion meaning here?

## 6. Fixed Anchor Plus Two Pointers

### 6.1 Reduce one dimension before using order

Triples appear to require three loops: choose i < j < k and inspect every sum. There are n(n−1)(n−2)/6 index triples, so direct enumeration takes O(n³) time and O(1) auxiliary space for a scalar count. A pointer scan does not somehow move three independent choices in linear time. It first **fixes one choice**, then exploits order for the other two.

```text
sorted values: [ anchor | left -> remaining pair interval <- right ]
                  fixed    pair positions are strictly after anchor
```

For exact equality, fixing value a changes the pair requirement to `leftValue + rightValue = target - a`. The sorted-pair proof then applies within that anchor's suffix. For a threshold, sorted order can sometimes count a whole block of acceptable pairs at once.

Recognition clues: a fixed-size combination, a sortable value relation, a cubic baseline, and a pair subproblem remaining after one value is fixed. Do not sort if original adjacency or positional order is essential. The scan is not sufficient for arbitrary three-item predicates with no monotone relation.

### 6.2 Original teaching example: count index triples below a limit

**Contract.** Count original index combinations i < j < k whose mathematical sum is **strictly less than** `limit`. Distinct index choices count separately, even when values repeat. Preserve input by sorting a copy. Values are any ints, limit is long, and n ≤ 100,000, so the maximum number of triples fits long. This is a count of index combinations, not unique value triples.

Sorting reorders occurrences, but a permutation gives a one-to-one correspondence between subsets of three occurrences before and after sorting. Their sums do not change. That preserves the count, although sorted indices could not be reported as original indices.

For one fixed anchor:

1. Start left at anchor+1 and right at n−1.
2. If even the sum with the largest remaining right value is below the limit, then every partner from left+1 through right also works with this anchor and left. Add `right-left` combinations and advance left.
3. Otherwise, this smallest remaining left value already makes the right endpoint's sum too large or equal. Any larger left is no better for that right, so decrease right.
4. Repeat for every anchor that leaves two later positions.

```java
static long countIndexTriplesBelow(int[] values, long limit) {
    int[] sorted = values.clone();
    Arrays.sort(sorted);
    long count = 0;
    for (int anchor = 0; anchor < sorted.length - 2; anchor++) {
        int left = anchor + 1;
        int right = sorted.length - 1;
        while (left < right) {
            long sum = (long) sorted[anchor] + sorted[left] + sorted[right];
            if (sum < limit) {
                count += right - left;
                left++;
            } else {
                right--;
            }
        }
    }
    return count;
}
```

The first cast makes the entire three-term sum long. The count is also long: arithmetic safety for the sum and for the number of answers are separate obligations.

### 6.3 Dry run and counting proof

Input `[-2,0,1,3]`, limit 3; the sorted copy is identical:

| anchor | left, right before | Sum | Conclusion | Count added | Running count | Movement |
| --- | --- | --- | --- | --- | --- | --- |
| 0: -2 | 1, 3 | 1 | Both partners at indices 2 and 3 work with left 1 | 2 | 2 | left becomes 2 |
| 0: -2 | 2, 3 | 2 | Partner 3 works with left 2 | 1 | 3 | left becomes 3; stop this anchor |
| 1: 0 | 2, 3 | 4 | Right 3 cannot work with any remaining left | 0 | 3 | right becomes 2; stop |

The three accepted value triples are `(-2,0,1)`, `(-2,0,3)`, and `(-2,1,3)`. Input `[0,0,0,0]` with limit 1 returns 4, one for each choice of three indices; limit 0 returns 0 because the inequality is strict. Fewer than three elements return 0. `[-4,-2,-1]` with limit -6 returns 1 because the only sum is -7.

**Inner invariant:** for the fixed anchor, pairs already removed from the candidate interval have either been counted exactly once or proved invalid; all unresolved suffix pairs lie between left and right. A qualifying sum permits counting the entire row of `right-left` partners by monotonicity, then discarding that left. A nonqualifying sum permits discarding the right because even its smallest remaining partner fails. These disjoint removals prevent double counting.

Every sorted index triple has a unique smallest index, its anchor. The outer loop visits every possible anchor, so combining the inner counts covers each index triple exactly once. This is also why equal anchors must **not** be skipped in this counting task.

### 6.4 Unique value triplets: a different duplicate contract

The roadmap's 3Sum practice asks for unique value results, not the index-combination count above. Preserve the distinction before adapting the mechanism:

| Requested result | Meaning of duplicates | Correct direction |
| --- | --- | --- |
| One witness or existence | Stop when a valid witness is found | No enumeration-specific deduplication is needed |
| Number of index triples | Equal values at different positions are separate choices | Keep multiplicities; do not skip repeated anchors |
| Unique value triples | Repeated occurrence choices may describe the same value triple | Suppress repeated anchor values and repeated partner-value results |

For unique exact-sum triples after sorting:

- Process the first occurrence of each **anchor value**; a repeated equal anchor would generate the same value triples from a suffix with no new value possibilities.
- Restrict partner indices to positions strictly after the anchor. Equal values remain allowed when there are enough occurrences.
- A too-small/too-large sum permits the usual ordered elimination for that anchor. Duplicate skipping in these branches is optional if justified; simple one-step motion is already correct.
- After recording an exact match, consume both matched partner positions, then skip further copies of those matched **partner values** inside the remaining interval. For fixed anchor and left value, only one right value can satisfy the exact equation, so repeated copies yield no new value triple.
- Compare a repeated anchor with the **previous anchor value**. Do not skip the first occurrence just because the next value is equal; it may be needed in a triple using two equal values.

Walkthrough for sorted `[-2,-2,0,2,2]`, target 0:

| Stage | Anchor / partner positions | Observation | Effect |
| --- | --- | --- | --- |
| First anchor | anchor 0; left 1, right 4 | Sum -2 is too small | Advance left to 2 |
| Match | anchor 0; left 2, right 4 | Values `(-2,0,2)` sum to 0 | Record that value triple once |
| Consume match | left 3, right 3 after one move each | No distinct pair remains | Stop this anchor |
| Repeated anchor | anchor 1 has same value -2 as anchor 0 | No new anchor-value result | Skip it |
| Next anchor | anchor 2; left 3, right 4 | `0+2+2` is too large | right becomes 3; stop |

One unique value triple remains. By contrast, there are four index combinations producing `(-2,0,2)`: two choices of -2 times two choices of 2. On `[-1,-1,2]`, both -1 occurrences are needed in the only triple. Skipping the initial left merely because it equals its anchor would erase a valid result. On four zeros, unique exact-sum output has one value triple while index counting has four.

### 6.5 Complexity and limits

For an anchor at position a, the inner interval has at most n−a−1 elements and shrinks monotonically: O(n−a) work. Across all anchors:

```text
(n-1) + (n-2) + ... + 2 = O(n²)
```

The example also clones in O(n) time/space and sorts in O(n log(n+1)) time. Total time is O(n²+n log(n+1)+1), normally simplified to O(n²) for n ≥ 2. Auxiliary space is O(n + S_sort(n)) for the copied array and the chosen sort's workspace; the pointer scan itself uses O(1). Output is a scalar long. Do not describe the complete method as constant auxiliary space because its pointers are scalar.

For unique value enumeration, include returned triples: O(t) result storage and at least Ω(t) emission work for t results. A list may also allocate temporary growth buffers. Sorting, output, and pointer state are separate costs. The teaching method's n bound protects the triple count; if n's permitted domain changed radically, even long counts would need reassessment.

**Self-check A1:** Why are repeated anchors necessary in the count example but redundant in unique-value enumeration?

**Self-check A2:** Why does a linear inner scan reset for each anchor produce quadratic total work?

## 7. Dominated-Boundary Elimination

### 7.1 The limiting side, not sorted values

**Problem.** At each unit-spaced position is a nonnegative int capacity. Choose two distinct positions. Their score is the distance between them multiplied by the smaller capacity. Return any maximizing pair plus its long score. With fewer than two positions, return `SpanScore(-1,-1,0)` to represent no pair. Preserve input; tied optima may return any valid witness.

Think of two supports with a horizontal cover between them: the shorter support limits useful height. The widest pair begins at the ends. A narrower pair can improve the score only by obtaining a sufficiently better limiting height.

The direct baseline evaluates all O(n²) pairs with constant work and O(1) auxiliary state. The optimization uses a **dominance bound**, not sorted order. Capacities remain in original positions because distance is part of the objective; sorting them would change the task.

```text
left capacity <= right capacity

current score = leftCapacity * (right-left)
any pair (left,k), left < k < right:
    min(leftCapacity, capacity[k]) <= leftCapacity
    k-left < right-left

Therefore its score cannot exceed the current score.
```

Evaluate the current pair first. If the left capacity is no taller, every narrower remaining pair using that same left endpoint is dominated by the score already seen. Discard left. Otherwise use the symmetric proof to discard right. Equal capacities allow discarding either one; the code chooses left.

```java
record SpanScore(int left, int right, long score) {}

static SpanScore strongestSpan(int[] capacities) {
    if (capacities.length < 2) {
        return new SpanScore(-1, -1, 0L);
    }
    int left = 0;
    int right = capacities.length - 1;
    int bestLeft = left;
    int bestRight = right;
    long bestScore = -1L;
    while (left < right) {
        long score = (long) Math.min(capacities[left], capacities[right])
                * (right - left);
        if (score > bestScore) {
            bestScore = score;
            bestLeft = left;
            bestRight = right;
        }
        if (capacities[left] <= capacities[right]) {
            left++;
        } else {
            right--;
        }
    }
    return new SpanScore(bestLeft, bestRight, bestScore);
}
```

The cast occurs before multiplication. With nonnegative int capacities and an int-indexed array, the product is bounded by roughly `(2^31−1)²`, which fits long. `bestScore = -1` ensures even a zero-score first pair becomes a valid witness. This would require a new proof for negative capacities, irregular coordinates, or another objective.

### 7.2 Dry run and preservation of an optimum

For `[2,7,3,6,4]`:

| Step | left, right | Capacities | Width | Score | Best after evaluation | Movement and reason |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0, 4 | 2, 4 | 4 | 8 | `(0,4,8)` | Discard left 0; its narrower pairs cannot beat 8 |
| 2 | 1, 4 | 7, 4 | 3 | 12 | `(1,4,12)` | Discard right 4, the limiting capacity |
| 3 | 1, 3 | 7, 6 | 2 | 12 | `(1,4,12)` | Discard right 3; tied score need not replace witness |
| 4 | 1, 2 | 7, 3 | 1 | 3 | `(1,4,12)` | Discard right 2; positions meet at 1 |

The code returns `(1,4,12)`. `(1,3,12)` would also satisfy the stated contract. Demanding the lexicographically smallest maximizing pair would be an extra requirement; this scan's tie policy does not promise it.

**Invariant:** the best recorded score is at least every score of an eliminated pair; any pair that could improve it is still inside `[left,right]`. Initially nothing is eliminated. After evaluating a current pair, the limiting-side inequality proves every unresolved pair using the discarded endpoint is bounded by that recorded candidate. Thus removing it preserves all opportunities to improve the best. The interval shrinks each step. At meeting, no unevaluated improving pair remains, so the recorded score is optimal.

The usual phrase “move the shorter side to seek a taller one” suggests what might help. The dominance inequality proves why no improvement is lost. The next height need not be taller and the next score may fall; neither invalidates the invariant.

### 7.3 Cost, counterexamples, and edge cases

At most n−1 evaluations occur, each with constant work: O(n) time, O(1) auxiliary space, and one constant-size returned record. Input is unchanged. No sort or auxiliary table is needed. When a maximum-scoring witness has several possibilities, compare its score with a baseline and verify its indices/score relation, rather than requiring one arbitrary pair.

Moving the taller side has no equivalent guarantee. On `[1,4,3,2]`, the outer score is 3. Wrongly keeping the left capacity 1 and shrinking right visits scores 2 and 1, missing the score 4 at indices (1,3). Moving both endpoints at once can likewise discard the only useful combination before evaluating it; a separate proof would be necessary.

`[0,0,0]` returns some distinct pair with score 0. `[5,5,5]` has score 10 at (0,2); the widest equal-height span wins. For two values, exactly one pair is evaluated. Empty/singleton input uses the sentinel. `[MAX,0,MAX]` gives score 4294967294, illustrating why int multiplication is insufficient.

Use this pattern only when you can bound **all** remaining candidates involving the removed boundary by something already evaluated. For an objective based on a sum, an irregular penalty, or interior values, the shorter-boundary argument may be false. A picture resembling an area problem is not a proof.

**Self-check D1:** Why must the current score be evaluated before the endpoint is discarded?

**Self-check D2:** Why does moving a pointer safely not imply the next score must improve?

## 8. Optional Advanced Boundary-Maximum Reasoning

This section supports the roadmap's **C — optional** Trapping Rain Water challenge. It does not block ordinary Two Pointers readiness. First understand the two-sided bound and the linear preprocessing solution; derive the advanced constant-space implementation later. Water held above individual positions is a different objective from the one pair score in Section 7.

### 8.1 A position needs a wall on both sides

Let nonnegative heights describe unit-width columns. At position i, the highest available wall at or left of i and the highest at or right of i bound the water level. Include the current column in both maxima:

```text
leftMax[i]  = max(height[0..i])
rightMax[i] = max(height[i..n-1])
depth[i]    = min(leftMax[i], rightMax[i]) - height[i]

height: [3,0,2,0,4]
depth:  [0,3,1,3,0]    total depth over unit widths = 7
```

Depth is nonnegative because both inclusive maxima are at least the current height. A low column needs containment on **both** sides; a high left wall alone cannot establish its level. Endpoints have depth zero.

The simple baseline rescans left and right for every column: O(n²) time and O(1) auxiliary space beyond an O(n) returned profile. It repeats the same maxima. Build a left-maximum table once and carry a right maximum in a reverse pass to avoid those rescans.

```java
static long[] waterDepthProfile(int[] heights) {
    int n = heights.length;
    int[] leftMax = new int[n];
    int highest = 0;
    for (int i = 0; i < n; i++) {
        highest = Math.max(highest, heights[i]);
        leftMax[i] = highest;
    }
    long[] depth = new long[n];
    int rightMax = 0;
    for (int i = n - 1; i >= 0; i--) {
        rightMax = Math.max(rightMax, heights[i]);
        depth[i] = (long) Math.min(leftMax[i], rightMax) - heights[i];
    }
    return depth;
}
```

This educational output is a per-position profile, not the platform's total-volume interface. Inputs are unchanged. Initial maxima zero rely on nonnegative heights. Empty input returns an empty profile without dereferencing any array element.

### 8.2 Profile dry run and correctness

For `[3,0,2,0,4]`, the forward table is `[3,3,3,3,4]`. The backward pass is:

| i | Height | leftMax[i] | rightMax after including i | Settled depth[i] |
| --- | --- | --- | --- | --- |
| 4 | 4 | 4 | 4 | 0 |
| 3 | 0 | 3 | 4 | 3 |
| 2 | 2 | 3 | 4 | 1 |
| 1 | 0 | 3 | 4 | 3 |
| 0 | 3 | 3 | 4 | 0 |

The forward invariant says leftMax[i] stores the highest column in exactly `[0,i]`. Each update includes one new column, preserving that meaning. In the reverse pass, after updating rightMax, it is the maximum of exactly `[i,n)`. Combining those established boundaries gives the exact two-sided limiting level at i. Each position is assigned once.

Both passes and allocations cost O(n) time. Auxiliary leftMax storage is O(n), returned depth storage O(n), with constant scalar state. If the result were only a total accumulated in long, the leftMax table would still be O(n) auxiliary; changing output does not remove the table. A total across an int-array-length number of nonnegative int-bounded depths fits long, while int totals can overflow.

### 8.3 Why a boundary maximum can settle one side early

To reduce retained working memory for a total, maintain an unresolved interval `[left,right]`, plus maxima of already processed prefixes/suffixes. Start both maxima at zero for nonnegative heights. No water for a processed position remains unresolved.

Suppose the known left maximum is no greater than the known right maximum. For the next left column:

- If that column is below the known left maximum, a right wall at least as high as that left maximum already exists. Unknown interior columns cannot lower that wall. The water depth is therefore fixed by the known left maximum, regardless of how much taller the unseen interior might be.
- If the column raises the left maximum, it holds zero water at itself. Updating the maximum before advancing is safe even if that new maximum exceeds the formerly known right maximum.

The symmetric argument settles the right side when its known maximum is smaller. This gives a different invariant from the sorted pair proof and the Section 7 dominance proof. Do not mix “compare current heights” and “compare processed maxima” implementations without deriving what each variable includes.

Illustrative settlement order for the same heights, with maxima initially 0:

| Step | left, right before | Known left/right maxima before | Side settled | Depth | Maxima and interval after |
| --- | --- | --- | --- | --- | --- |
| 1 | 0, 4 | 0, 0 | Left column 3 raises its maximum | 0 | 3, 0; interval [1,4] |
| 2 | 1, 4 | 3, 0 | Right column 4 raises its maximum | 0 | 3, 4; interval [1,3] |
| 3 | 1, 3 | 3, 4 | Left column 0 has both needed bounds | 3 | 3, 4; interval [2,3] |
| 4 | 2, 3 | 3, 4 | Left column 2 is below the left bound | 1 | 3, 4; interval [3,3] |
| 5 | 3, 3 | 3, 4 | Final column 0 | 3 | 3, 4; interval empty |

Each settlement moves one position, so this formulation can achieve O(n) time and O(1) auxiliary space for a scalar total. The profile implementation above deliberately exposes the maxima definitions first; translating settlement into Java is optional advanced practice. An unresolved single position is still a column to settle, unlike a pair-existence loop that requires two distinct positions.

### 8.4 Edge cases and unsuitable assumptions

Empty, singleton, all-equal, nondecreasing, and nonincreasing height sequences hold no water. `[2,0,2]` has profile `[0,2,0]`; `[0,2,0]` has `[0,0,0]`. Replacing two-sided containment with “maximum seen on the left minus height” would wrongly assign water to a descending open edge. Negative heights, variable widths, open boundaries with another physical model, and arbitrary updates require new contracts and analysis.

Common mistakes: use the larger boundary maximum as the water level, subtract before establishing the maxima, reuse the Section 7 shorter-height movement without its own invariant, or claim constant auxiliary memory while retaining a prefix table. Index bounds and water-level reasoning are separate correctness obligations.

**Self-check B1:** Why does a known taller wall on the other side make a lower side's depth final?

**Self-check B2:** Why does the profile code still use linear auxiliary space even though its reverse pass carries only one right maximum?

## 9. A Method for Proving Pointer Movements

An invariant is a precise statement about the state at a particular point in a loop. Keep it readable: “all possible answers are still inside this interval,” “the output prefix is exactly the accepted processed input,” or “the emitted merge consists of exactly the consumed source prefixes.” Naming it is only the start; explain how each branch preserves it.

Use the following proof sequence whenever deriving a pointer method:

1. **Define the output and remaining work.** Existence, counting, enumeration, and maximization need different promises.
2. **Initialize the invariant.** Why do the starting positions represent the entire unresolved task?
3. **Justify the chosen action.** Supply the sorted inequality, completed matching obligation, safe-write relationship, or dominance bound.
4. **Prove preservation.** Explain exactly why the discarded region contains nothing necessary, or why the finalized prefix is correct.
5. **Prove progress.** Name a nonnegative integer measure that decreases, such as interval length or total unread items.
6. **Interpret termination.** Meeting positions, an exhausted source, or a completed read scan imply different conclusions. Connect the stopping state to the output contract.

| Pattern | What is preserved | Why moving is safe | Termination means |
| --- | --- | --- | --- |
| Sorted pair | Any remaining valid pair lies inside the interval | Endpoint cannot work with any unresolved partner | No distinct candidate pair remains |
| Filtered palindrome | Consumed meaningful ends match | Ignored symbols are irrelevant; matched pair is finished | At most one meaningful symbol remains |
| Stable compaction | Accepted prefix equals retained processed input | Write never destroys unread input; accepted order is preserved | Every original value was classified |
| Sorted merge | Emitted prefix is the sorted merge of consumed prefixes | Smaller head is globally smallest remaining value | Both sources consumed |
| Anchor threshold count | Removed pairs are counted once or rejected | Sorted order classifies a whole row or endpoint | Every suffix pair classified for that anchor |
| Maximum boundary score | No eliminated candidate can improve recorded best | Limiting-boundary candidates are dominated | Best covers every possible improving pair |
| Advanced water settlement | Finalized depths cannot change | Both required bounds are sufficient for the chosen side | Every column settled |

For a proposed optimization, actively try to break its assumptions. Shuffle a sorted array; change “unique values” to “index combinations”; remove the permission to mutate; require original indices; add a negative height; change an equality to a threshold. A counterexample identifies which part of the proof must change.

Correctness and termination are distinct. A pointer that always moves may terminate with the wrong answer. Conversely, a logically sensible condition without an update may never terminate. For pair search, `right-left` decreases; for merge, `(n-firstIndex)+(m-secondIndex)` decreases; for filtering, `n-read` decreases. None proves correctness alone.

**Self-check V1:** What does a progress measure establish that a correctness invariant does not?

**Self-check V2:** Why is “moving left might increase the sum” weaker than the sorted endpoint-elimination proof?

## 10. Deriving Time and Space Complexity

### 10.1 Count travel within the correct scope

Let n be one input length, m a second input length, k a selected string-range length, t the number of returned triples, and `S_sort(n)` the sorting implementation's peak auxiliary workspace. Use the [complexity reference](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) for the full analysis process.

| Algorithm | Counted work | Time | Auxiliary space | Output |
| --- | --- | --- | --- | --- |
| Sorted pair existence, already sorted | At most n−1 endpoint eliminations | O(n) | O(1) | Boolean |
| Filtered range palindrome | At most k left/right advances combined up to constant factors | O(k) | O(1) | Boolean |
| Stable removal / sorted deduplication | n reads, at most n writes | O(n) | O(1) | Scalar length and mutated input prefix |
| Merge two sorted arrays | n+m emissions and advances | O(n+m) | O(1) | O(n+m) array |
| Threshold triple count, copied input | O(n) clone, sort, sum of per-anchor scan lengths | O(n²+n log(n+1)+1) | O(n+S_sort(n)) | Scalar long |
| Boundary-score witness | At most n−1 pair evaluations | O(n) | O(1) | One record |
| Water-depth profile | Two n-step passes and two n-length arrays | O(n) | O(n) | O(n) profile |
| Optional water total by settlement | Each column settled once | O(n) | O(1) | Scalar long |

The palindrome skip loops share monotonically advancing pointers with the outer loop. They do not restart a scan from the beginning, so their travel adds to O(k). The anchor method **does** reset its pair interval for a new anchor, so its scan lengths add to O(n²). Merge tail loops consume items the main loop did not consume; they do not create another multiplicative dimension.

### 10.2 Include preprocessing and output honestly

For a pair search on unsorted input, sorting then scanning costs O(n log(n+1))+O(n). If preserving input by cloning, add O(n) auxiliary storage and copying time. The scalar pointer state remains O(1), but the complete operation's storage has changed. Java 21 primitive sorting should not be described as universally constant-space; use `S_sort(n)` or a justified implementation-specific bound.

A merge's O(n+m) array is required output. It is excluded from auxiliary space under this guide's convention but remains real memory. If a helper's returned array is used only to compute another final result, it generally becomes auxiliary state of that larger algorithm. A mutable array does not shrink when a method returns a smaller logical length. `Arrays.copyOf(values, length)` materializes a result prefix with O(length) additional time and storage.

For t enumerated triples, writing t results needs Ω(t) time and O(t) output storage even if finding candidates is efficient. Distinct-index pairs can have Θ(n²) output on repeated values; an existence algorithm's linear bound cannot be reused for listing them. Scalar counts avoid materializing all matches but must fit the count type.

### 10.3 Separate data access from pointer arithmetic

Array indexing and `String.charAt` provide the constant-time access used in these analyses. Copying a substring, calling `toCharArray`, creating a cleaned string, sorting, or repeatedly accessing a linked list by index adds real work. A linear number of pointer updates does not establish linear total time if each update performs a growing copy or a linear lookup.

For example, a palindrome implementation that calls `substring(left,right+1)` each iteration can copy k+(k−2)+(k−4)+... code units: O(k²) total. Its peak temporary text may be O(k), which differs from cumulative allocation volume. State the actual API calls before simplifying costs.

**Self-check C1:** Why does merged output occupy O(n+m) memory even when auxiliary space is O(1)?

**Self-check C2:** What must you add to a pair scan's cost when the input must first be copied and sorted?

## 11. Java 21 Implementation Decisions

### 11.1 Indices, bounds, and mutation

Use `values.length` for arrays and `text.length()` for strings. Decide whether a position is an element index, a boundary, or a logical output length before writing a condition. `[from,to)` has length `to-from`; its final possible element is `to-1`. The initial right index can therefore be -1 for an empty range, provided the loop guard prevents access.

For distinct pair candidates, `left < right` enforces different positions. `left <= right` would allow `[3]` to appear to solve target 6. For reverse array loops, an int index can safely reach -1 and terminate; do not dereference it. In a skip loop, evaluate a bounds guard **before** array/string access using short-circuit `&&`.

Array parameters are references. `int[] work = values` aliases the same array; sorting work sorts the caller's data. Use `values.clone()` or `Arrays.copyOf` when a private copy is part of the contract. For nested arrays, an outer clone shares rows. The [array-copy reference](./Java_Array.md#copying-arrays) supplies the details without needing to repeat Java reference semantics here.

Only `removeValueStable` and `uniqueSortedPrefix` mutate inputs in this guide. The latter needs sorted input but does not sort it. `countIndexTriplesBelow` sorts a private copy. Other examples read their inputs and return scalars, a record, or new arrays. A returned prefix length authorizes reading `[0,length)` as the result, not the full original array.

### 11.2 Sorted positions are not original positions

Suppose original values are `[40,10,30]` and the target is 40. The matching original positions are 1 and 2. Sorting produces `[10,30,40]`, whose matching sorted positions are 0 and 1. Returning those as original indices would be wrong even though the values are right.

Choose from the actual contract:

- If values/existence are sufficient and sorting is allowed, a sorted copy can work.
- If original indices are required, hashing may directly retain them; see [complement lookup](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#5-complement-lookup).
- Sorting records containing both value and original index preserves provenance, with O(n) records/storage and object-sort workspace. Do not recover positions by one naive “first index of value” lookup when duplicates require different occurrences.
- If array order encodes distance, contiguity, or subsequence order, sorting can change the mathematical problem itself. The capacity-score example must preserve original geometry.

The [Java 21 Arrays API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) specifies ascending primitive sorting and documents its O(n log n) implementation time. Its implementation notes do not justify a universal O(1) workspace claim. Primitive `int[]` has no comparator overload. For object records, use `Comparator.comparingInt` or `Integer.compare`; comparator subtraction can overflow and violate ordering.

### 11.3 Arithmetic must be wide before the operation

The relevant question is the expression's type when it is evaluated, not the destination variable's type:

| Expression | What happens | Safer choice when the range requires long |
| --- | --- | --- |
| `long sum = a + b;` with int a,b | Addition occurs as int before assignment | `(long) a + b` |
| `long sum = (long) (a + b + c);` | Overflow may occur before the cast | `(long) a + b + c` |
| `long score = height * width;` with int operands | Multiplication occurs as int | `(long) height * width` |
| `long needed = target - value;` with int operands | Subtraction may wrap before assignment | `(long) target - value` |
| `compare(a,b)` implemented as `a-b` | Sign can be corrupted by overflow | `Integer.compare(a,b)` |

`MAX + MAX` has exact sum 4294967294; three MIN values sum to -6442450944. Both fit long. A container score with heights `[MAX,0,MAX]` is 4294967294. Duplicate index counts can outgrow int even when every individual value is tiny. These bounds are why the examples use long sums, scores, and counts.

Long is finite. The triple-count contract caps n so its combinatorial count fits; a different count/output domain would require a new bound. Checking arithmetic does not repair an invalid algorithm, and casting an already overflowed expression cannot recover lost bits.

### 11.4 Strings: choose the character model explicitly

The [Java 21 String API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) defines `length` and indices in UTF-16 code units. A supplementary code point occupies two char positions. Therefore one `charAt` result is not always a whole character.

The range-palindrome method's ASCII precondition makes individual char indexing, `Character.isLetterOrDigit(char)`, and simple lowercase comparison appropriate. It does not validate ASCII itself. Do not apply that contract unchanged to arbitrary user-language text: two surrogate halves, combining sequences, normalization, and full case folding need explicit semantics.

`Character` int overloads support supplementary code points; char overloads cannot represent them. Simple `Character.toLowerCase` is not a promise of full locale-sensitive, contextual, or multi-code-point case mapping. These distinctions are stated in the [Java 21 Character API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html). They are domain decisions, not a reason to add Unicode machinery to a problem that explicitly permits only ASCII.

If a different problem defines equality by Unicode code points, you could materialize a code-point array, adding O(n) time/storage, or carefully advance string positions by code-point width from both ends. Neither automatically solves grapheme-cluster or normalization semantics. State what “same character” means first.

Strings are immutable; use `char[]` when the task requires actual character mutation and count the conversion's O(n) storage. Use indexed access when only reading. Modern substring extraction generally copies the selected content, except implementation shortcuts such as the whole/empty range; avoid building one on every pointer step. See [String complexity](./Java_String.md#8-complexity-expectations-for-dsa).

### 11.5 Inputs and outputs in tests

For a mutating routine, retain a separate original copy in the **test harness** if comparing the result with a baseline. Harness storage does not become the algorithm's auxiliary memory. Verify only the valid prefix for removal/deduplication; separately verify input identity and logical length. For pair/score witnesses, check index bounds, distinctness, and the objective value. Do not impose an unstated tie order.

Use `Arrays.equals` for primitive arrays, `Arrays.deepEquals` for nested arrays, and `Arrays.toString`/`deepToString` to inspect results. `==` checks array identity. Records compare their components; the `SpanScore` primitive components have value semantics, but a record containing arrays would not automatically provide deep array-content equality.

**Self-check J1:** Why is `(long)(a+b)` insufficient for arbitrary int operands?

**Self-check J2:** What changes in correctness and memory when a char-based palindrome problem is redefined over Unicode code points?

## 12. Choosing Between Techniques

Start with required output, ordering, mutation permission, input size, and available operations. A technique that needs sorting is useful only if ordering does not destroy required information and its preprocessing costs are acceptable.

### 12.1 Two Pointers versus brute force and hashing

| Situation | Simple baseline | Pointer option | Hashing option / decision |
| --- | --- | --- | --- |
| Pair existence in sorted data | O(n²), O(1) auxiliary | O(n), O(1), ordered elimination | Expected O(n) with O(u) state; often unnecessary if sorted |
| Pair existence in unsorted data | O(n²), O(1) | Sort then scan: O(n log(n+1)), copy/workspace as needed | Expected O(n), O(u), preserves original order and can retain indices |
| All index pairs satisfying equality | O(n²) checks plus output | Must handle multiplicity/output; not the existence routine | Maps may locate matches, but quadratic output can remain |
| Filtered palindrome | Cleaned/reversed copies: O(n) time/space | O(n) time, O(1) auxiliary under stated text model | Frequency counts lose positional symmetry |
| Stable removal | Copy accepted values: O(n) time/space | O(n) time, O(1) auxiliary with allowed mutation | Membership can define a predicate, but does not itself perform compaction |
| Sorted merge | Concatenate/sort: O((n+m) log(n+m+1)) | O(n+m) time and required output storage | A set loses multiplicities; a map adds work and does not directly preserve order |

Hashing's expected bound depends on key work/distribution, while the ordered pointer scans above use deterministic inequalities after their preconditions hold. Sorting may be worthwhile when several later operations need the order; for one tiny query, even a quadratic baseline may be adequate. Discuss the whole contract rather than declaring one method universally best.

### 12.2 Opposite direction, same direction, and Sliding Window

| Family | What the positions represent | What is maintained | Main proof question |
| --- | --- | --- | --- |
| Opposite ends | Unresolved boundaries / mirrored partners | Candidate interval or unmatched obligations | What lets an endpoint be discarded? |
| Same-direction read/write | Next source value / next accepted destination | Correct output prefix | Why are writes safe and output stable? |
| Coordinated sources | First unconsumed position in each sequence | Consumed prefixes and next required output/match | Which source can advance without losing needed data? |
| Sliding Window | Ends of a contiguous region in original input | Aggregate/count state for that region | How do adding/removing boundary items preserve validity? |

Sliding Window is a related specialization with moving boundaries and maintained state over a contiguous region. Not every same-direction scan is a window: a compaction write index is an output length, not the start of a contiguous input region. Not every subarray question has a valid window monotonicity rule. For arbitrary negative values and exact-sum counting, prefix/hash reasoning may be needed instead. Detailed window algorithms belong to the [later Sliding Window category](./DSA_Interview_Roadmap.md#5-sliding-window).

### 12.3 Sort first or preserve order; mutate or allocate

| Choice | Benefit | Cost or risk | Suitable when |
| --- | --- | --- | --- |
| Sort the supplied array | Enables ordered elimination; no explicit preservation copy | Mutates input, loses original positions, sort workspace remains | Ordering is allowed and original arrangement is irrelevant |
| Sort a private copy | Enables the same reasoning while preserving input | O(n) copy plus sorting work/workspace | Values/counts matter but caller owns original arrangement |
| Decorate with original indices, then sort | Retains provenance | O(n) records and object sorting costs | Original-index witnesses are required and sorting is otherwise useful |
| Preserve input and use hashing | Direct lookup without reorder | Expected costs, O(u) state, key overhead | Unsorted original-index search or reusable counts |
| Stable in-place compaction | Constant working space, original retained order | Destroys parts of original input; logical length contract | Mutation allowed, output no longer than input |
| Fresh output array | Input remains available; simple write ownership | Required output allocation and initialization | Output contract permits or requires a separate result |

For same-array merging or expanding transformations, `write <= read` may not hold. A backward fill can sometimes protect unread values when extra capacity exists, but it requires its own invariant. Do not generalize forward stable filtering to every in-place operation.

**Self-check T1:** Why might hashing be preferable when an unsorted pair problem requires original indices?

**Self-check T2:** Why is a read/write compaction scan not automatically a sliding window?

## 13. Pattern Recognition Practice

Before opening the answer key, name a candidate technique, the required assumption, and the proof obligation. These are short reasoning scenarios, not a new exercise workbook.

| ID | Scenario |
| --- | --- |
| R1 | A nondecreasing list of account amounts asks whether two distinct positions reach a target. |
| R2 | The same amounts are unsorted; the answer must return original indices and input must stay unchanged. |
| R3 | A mutable array must remove one chosen value while preserving all retained values' order. |
| R4 | Two sorted event-ID arrays must become one sorted array retaining every occurrence. |
| R5 | A string must read the same from either end after a specified ASCII filtering rule. |
| R6 | A cubic baseline searches three values in any order for a target; results must be unique value triples. |
| R7 | Boundary heights in their original unit-spaced positions define a width-times-limiting-height score. |
| R8 | Count all contiguous subarrays with target sum, allowing negative elements. |
| R9 | Count copies of arbitrary IDs; input is unsorted and its domain spans most ints. |
| R10 | Find the longest original contiguous region satisfying a count condition as its boundaries expand/shrink. |

Do not select by keywords alone. R1's inequality needs sorted order; R3's write safety needs nonexpanding output; R6's duplicate policy depends on the result definition. R8 mentions contiguous ranges but does not supply the monotonicity needed for an ordinary positive-sum window. R9 is naturally about counts, not sorted endpoint elimination.

An unfamiliar problem is not mastered because its title resembles one of these rows. Demonstrate an actual movement proof or choose another method. Revisit the [Arrays & Hashing recognition questions](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#13-pattern-recognition-cheat-sheet) when the required state is membership, counts, or prefix totals instead of ordered boundaries.

## 14. Common Mistakes and Edge Cases

### 14.1 Bugs, witnesses, and repairs in reasoning

The fragments below describe **incorrect choices**, not alternative valid algorithms. Their witnesses satisfy the relevant teaching contracts unless the row explicitly demonstrates a missing precondition.

| Mistake | Witness / consequence | Why it fails; how to prevent it |
| --- | --- | --- |
| Pair search uses `left <= right` without excluding self-pairs | `[3]`, target 6 can match itself | The task requires distinct positions; use the candidate-pair invariant and `left < right` |
| Sum equals target but an enumeration loop never advances | `[1,2]`, target 3 repeats the same state forever | Every non-returning branch must make progress; define match consumption and duplicate policy |
| Decrease right after a too-small sorted sum | `[1,2,4,8]`, target 10 loses 2+8 | No impossibility proof exists for right; discard the endpoint ruled out by the inequality |
| Increase left after a too-large sum | Same array, target 6 loses 2+4 | Discard right instead under sorted order |
| Apply sorted pair motion to unsorted data | `[3,2,4]`, target 6 returns false | The ordering premise is false; sort appropriately or use another method |
| Skip the first anchor if it equals the next value | `[-1,-1,2]`, target 0 loses its only unique triple | Keep the first equal anchor; suppress repeated anchors relative to a previous processed anchor |
| Skip a partner merely because it equals the anchor | `[-1,-1,2]` again | Equal values can occupy distinct needed indices; skip duplicate results at the correct stage |
| Skip repeated anchors in index-combination counting | Four zeros, limit 1 should count 4 | Value deduplication erases distinct occurrence choices |
| Increment write for rejected values | `[2,5]`, removing 2 leaves a hole in the claimed prefix | Write counts accepted values only; read advances over every value |
| Compare deduplication against an invalid previous slot | Empty input or write 0 | Guard the first accepted value before reading `values[write-1]` |
| Treat tail values as part of a compacted result | `[2,5,2]`, removal returns 1 but suffix still contains leftovers | Read only the logical prefix; array length is unchanged |
| Move both merge sources after unequal heads | `[1,4]` and `[2]` can lose 2 | Only the source whose item was emitted has been consumed |
| Omit a merge tail | `[1]` and `[2,3]` loses remaining second items | Exhaustion of one source is not exhaustion of the output |
| Move the taller capacity boundary | `[1,4,3,2]` misses score 4 | The discarded endpoint is not dominated; use the limiting-side proof |
| Use int arithmetic then cast | `[MAX,MAX]`, target 4294967294 | The sum wraps first; promote an operand before addition |
| Return sorted positions as original indices | Original `[40,10,30]`, target 40 | A permutation changes index meaning; preserve provenance or use hashing |
| Alias input while claiming to sort a copy | `work = values; Arrays.sort(work)` | Both references identify one array; actually clone when required |
| Check bounds after dereferencing | `text.charAt(left)` before testing whether left crossed | Access can be invalid on empty/fully skipped ranges; short-circuit guards come first |
| Claim every sort + pointer method is O(1) space | The triple example allocates a clone | Include copies, sort workspace, output, and library operations |

The pair, anchor, and score examples show why arbitrary motion is wrong. A different algorithm may use a different update sequence correctly if it preserves a clearly stated invariant. Do not call it buggy merely because it looks different.

### 14.2 A compact edge-case matrix

`MIN`/`MAX` below mean Java int limits. Only apply cases valid for a method's stated domain; capacities/heights must remain nonnegative, pair/dedup inputs must be sorted, and the palindrome range must be ASCII.

| Case | Example | Expected behavior to check |
| --- | --- | --- |
| Empty | pair `[]`; palindrome empty range; merge `[] + []` | false; true; empty result respectively |
| Singleton | pair `[3]`; dedup `[3]`; score `[3]` | false; logical length 1; no-pair sentinel |
| All equal | dedup `[7,7,7]`; triples four zeros with limit 1 | One retained value; four index combinations |
| Duplicate partners | pair `[3,3]`, target 6 | true with two distinct indices |
| Negative sorted values | pair `[-5,-2,4]`, target -1 | true from -5+4 |
| Missing target | pair `[1,2,4,8]`, target 7 | false when pointers meet |
| Full removal | `[2,2]`, remove 2 | Logical length 0; physical tail unspecified |
| Fully filtered text | `"!?-"`, whole range | true, with no invalid access |
| Mixed-case center | `"A, b-a"` | true; center imposes no pair obligation |
| Match with direct crossing | `"aa"` | true after left/right cross |
| One source empty | merge `[] + [-1,2]` | `[-1,2]` in a fresh result |
| Reverse-sorted data | triples `[3,1,0,-2]`, limit 3 | Copy-sort count 3, original unchanged |
| Extreme sum | pair `[MIN,MAX]`, target -1 | true without overflow |
| Extreme score | capacities `[MAX,0,MAX]` | Score 4294967294 |
| No containment | heights `[0,2,0]` | Water profile `[0,0,0]` |
| Equal optimal scores | capacities `[2,7,3,6,4]` | Score 12; any valid maximizing witness |

For a nonmutating method, assert input contents remain unchanged. For logical-prefix routines, compare the returned prefix with a direct filtered/distinct baseline, and check the returned length is within `[0,n]`. For a sorted search, test a valid duplicate pair and a singleton separately so you do not accidentally reject equal values while trying to reject self-use.

**Self-check E1:** Why is checking only the full physical array after compaction an invalid test oracle?

**Self-check E2:** Why should a test for a maximizing witness validate score and indices instead of one arbitrary tied pair?

## 15. Conceptual Self-Assessment and Answer Key

First answer the self-checks at the ends of the teaching sections. Then rehearse these six interview questions without notes:

1. **Q1:** A sorted pair sum is too small. State exactly which pairs are eliminated and prove they cannot match. What changes if the array is unsorted?
2. **Q2:** Explain stable removal using initialization, preservation, and termination. Why does an accepted write not corrupt future reads?
3. **Q3:** Change a triple task from unique value output to counting index combinations. Which duplicate policies change, and why?
4. **Q4:** For a capacity boundary score, explain why keeping a shorter endpoint and narrowing the other side cannot improve the current score. Why does that not require sorted capacities?
5. **Q5:** Analyze a copied-and-sorted anchor scan, including output and the library's workspace. Why is it not O(n) merely because the inner scan is linear?
6. **Q6:** A palindrome task changes from ASCII code units to Unicode code points. Which assumptions and operations must be reconsidered before choosing an implementation?

<details>
<summary>Compact answers — open after attempting the questions</summary>

| Checks | Core answer |
| --- | --- |
| F1 / F2 | A relation or invariant lets each move finish work or eliminate candidates safely. Pointer count says nothing about repeated outer scans, expensive access, or output size. |
| P1 / P2 | For every unresolved k, `a[left]+a[k] <= a[left]+a[right] < target`; discard left. Nondecreasing order supports this for negative/duplicate values too; unsorted data does not. |
| M1 / M2 | Filtering removes symbols explicitly excluded by the definition. A retained mismatch disproves mirrored equality. Nested skips share monotone positions, so each code unit is passed only a bounded number of times. |
| W1 / W2 | Safe writes protect input but the acceptance rule must still produce exactly the desired prefix. Sorted duplicates are contiguous; without order, the last accepted value does not summarize all previously seen values. |
| S1 / S2 | Each sorted head is its source's minimum remaining item, so the global next item is a head. Source indices belong to different arrays; test each against its own length. |
| A1 / A2 | Index combinations retain occurrence choices; unique value output suppresses redundant value results. A new anchor starts a fresh suffix scan, so lengths add quadratically. |
| D1 / D2 | Dominance bounds use the current evaluated candidate; it must be included in best first. A move preserves possible improvement overall, not monotone improvement of successive scores. |
| B1 / B2 | A sufficiently high wall on the other side guarantees the lower known boundary limits the current depth. The profile still stores n left maxima, regardless of the scalar right scan. |
| V1 / V2 | A decreasing finite measure establishes termination, not that the answer is correct. “May increase” predicts direction; eliminating every impossible partner proves safety. |
| C1 / C2 | Required output storage is real even when excluded from auxiliary space. Add clone time/storage, sorting time/workspace, and any output costs to the scan. |
| J1 / J2 | Narrow arithmetic has already lost information before the cast. Code-point semantics need code-point-aware advancement or representation, an explicit filtering/case policy, and a revised memory analysis. |
| T1 / T2 | A map can preserve original positions without sorting, with expected costs and extra state. Compaction's write is an output position, not a window's input boundary. |
| E1 / E2 | Tail contents are unspecified and may contain leftovers; only length and valid prefix define correctness. Any allowed tied optimum is valid, so enforce the stated property rather than invent tie ordering. |

| Recognition scenario | Suitable direction and proof obligation |
| --- | --- |
| R1 | Sorted opposite-end pair search; prove endpoint elimination using ordered sums. |
| R2 | Consider a value-to-index hash map, or value/index decoration plus sorting; retain original provenance and distinct indices. Sorting bare values then returning positions is invalid. |
| R3 | Read/write compaction; preserve accepted processed input and protect unread positions. |
| R4 | Coordinated sorted merge; the next global minimum is a remaining head; retain multiplicity. |
| R5 | Filtered opposite-end comparison under the specified character model; compare all retained mirrored symbols. |
| R6 | Fix an anchor and scan its sorted suffix; justify both pair motion and duplicate suppression at the anchor/partner levels. |
| R7 | Dominated-boundary elimination if the exact score admits the limiting-side bound; preserve original coordinates. |
| R8 | Consider prefix totals and occurrence counts, not an unjustified monotone sum window; negatives break the usual direction inference. |
| R9 | A frequency map is appropriate; sparse full-int IDs do not justify a direct range array. Two Pointers offers no automatic advantage. |
| R10 | Sliding Window may fit if state and validity permit safe expansion/shrinking; establish that property rather than relying on “contiguous.” |

Q1–Q6 correspond respectively to Sections 3, 4, 6, 7, 10, and 11. A satisfactory answer contains the proof and assumptions, not only the technique name or a memorized code fragment.

</details>

## 16. Key Takeaways and Interview Explanation

Two Pointers is a family of reasoning patterns. Sorted elimination removes impossible pairs. Mirrored comparison finishes obligations. Read/write preserves a valid output prefix. Coordinated scans choose the next required item. Anchors reduce a larger combination problem to repeated pair scans. Dominance bounds eliminate endpoints that cannot beat an evaluated candidate. Optional water settlement relies on sufficient two-sided maxima.

Use this short explanation when discussing a new problem:

> “The baseline costs ___ because it repeats ___. The useful assumption is ___. My positions represent ___. Before each iteration, ___. I can advance ___ because ___, so no required answer is lost. The progress measure is ___. At termination ___. Including preprocessing, output, and Java operations, time is ___ and auxiliary space is ___.”

Test that explanation by changing one assumption. If order is unsorted, indices must be preserved, duplicates have a different meaning, text semantics change, or mutation is disallowed, revisit the design. The same-looking loop can require a different invariant or a different technique.

## 17. References, Validation, and Next Steps

### Repository and official references

- [Two Pointers requirements and practice](./DSA_Interview_Roadmap.md#2-two-pointers), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category), and [revision process](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition).
- [Java Arrays](./Java_Array.md), [Java Strings](./Java_String.md), [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), and [Arrays & Hashing — Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).
- [Official NeetCode Roadmap](https://neetcode.io/roadmap): primary curriculum behind the repository roadmap. Scope, priorities, problem titles, and difficulty labels in this document are taken from the inspected local roadmap; no new platform problems are required.
- [Java 21 Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), [String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html), and [Character](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html): contracts for sorting/copying and text operations. These were checked with Context7 and official Oracle documentation on **24 October 2026**. Algorithm derivations and teaching variants here are original explanations.

### Validation and limits

All **eight Java blocks**, containing eight methods and one nested record, compiled together with Temurin JDK 21.0.12 using `javac --release 21 -Xlint:all -Werror`. A temporary harness outside the repository passed **87,620 checks**. It compared the implementations with direct pair/triple enumeration, independent ASCII filtering/reversal, stable filtering, distinct sorting, concatenate-and-sort merging, exhaustive boundary scoring, and direct per-column wall scans.

Checks included the documented dry-run outputs, 700 seeded short array cases, every valid range in 350 seeded ASCII strings, all 5,461 height sequences of lengths 0–6 with values 0–3, integer extremes, invalid palindrome range bounds, and the declared empty/singleton/duplicate cases. Nonmutating methods were checked for input preservation; logical-prefix methods were checked only against their valid prefix contracts. Maximizing witnesses were checked for valid indices and scores, allowing any tied optimum. The optional constant-space water settlement discussion was traced and reasoned through; the executable water example is the explicit profile implementation.

All **42 local Markdown links**, including heading anchors, were checked. The five practice entries match the inspected roadmap's URLs, priorities, and difficulties. Existing repository files were left unchanged; only this guide was created.

All numerical, ordering, character-domain, mutation, and feasible-allocation assumptions stated in the sections remain part of the contracts. In particular, the filtered range method assumes ASCII, ordered scans assume nondecreasing input, boundary examples assume nonnegative capacities/heights, and the triple count uses its declared n bound. Tests support correctness; the invariants and operation counts explain general correctness and complexity. No learner practice result or LeetCode submission is implied by these checks.

### Next steps using the existing roadmap

Close this guide, state one sorted inequality and one read/write invariant, then rebuild the roadmap's sorted pair, stable removal, and filtered-palindrome drills from a blank editor. Test an ordinary case, a boundary case, and a counterexample to a tempting wrong movement. Use the small comparison questions above to distinguish understanding from recall.

Attempt the three Essential problems in roadmap order, recording I/H/S outcomes and review dates on their **existing roadmap rows**. Use about 1, 3, 7, and 14 days as flexible review intervals; prioritize assisted or failed work. Do not check gates solely because these examples run. Core implementation and independent practice support I/P; R requires repeatable independent transfer on unfamiliar sorted/filtered variants, duplicate-result reasoning, output order, crossing bounds, and defensible complexity. Continue to Additional boundary-score practice when it addresses a gap; the optional Hard does not block core readiness.
