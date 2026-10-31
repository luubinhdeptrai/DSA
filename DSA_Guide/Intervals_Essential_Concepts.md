# Intervals — Essential Concepts (Java 21)

An interval algorithm begins with a statement about which points belong to a range. Sorting, scanning, Greedy selection, and heaps become reliable only after that statement is precise.

## Contents

- [1. Scope, prerequisites, and learning objectives](#1-scope-prerequisites-and-learning-objectives)
- [2. What an interval represents](#2-what-an-interval-represents)
- [3. Endpoint conventions and empty ranges](#3-endpoint-conventions-and-empty-ranges)
- [4. Deriving overlap and disjointness](#4-deriving-overlap-and-disjointness)
- [5. Sorting intervals in Java](#5-sorting-intervals-in-java)
- [6. Merge intervals with a covered frontier](#6-merge-intervals-with-a-covered-frontier)
- [7. Insert through three ordered regions](#7-insert-through-three-ordered-regions)
- [8. Select compatible activities](#8-select-compatible-activities)
- [9. Prove the earliest-finish choice](#9-prove-the-earliest-finish-choice)
- [10. Detect meeting conflicts](#10-detect-meeting-conflicts)
- [11. Sweep through endpoint events](#11-sweep-through-endpoint-events)
- [12. Track active bookings with a min-heap](#12-track-active-bookings-with-a-min-heap)
- [13. Offline interval queries — optional advanced](#13-offline-interval-queries--optional-advanced)
- [14. Arrays, references, and ownership](#14-arrays-references-and-ownership)
- [15. Time and complete auxiliary space](#15-time-and-complete-auxiliary-space)
- [16. Mistakes and reproducible counterexamples](#16-mistakes-and-reproducible-counterexamples)
- [17. Choose among interval and alternative methods](#17-choose-among-interval-and-alternative-methods)
- [18. Recognition and derivation](#18-recognition-and-derivation)
- [19. Conceptual assessment and answer key](#19-conceptual-assessment-and-answer-key)
- [20. Readiness and retained understanding](#20-readiness-and-retained-understanding)
- [21. References, curated practice, and validation](#21-references-curated-practice-and-validation)

## 1. Scope, prerequisites, and learning objectives

Intervals is category 11 in **Phase 4 — Connectivity, Choices, and Reused States** of the [DSA Interview Roadmap](./DSA_Interview_Roadmap.md#11-intervals). The roadmap is authoritative for topic scope, curated E/A/C priorities, implementation drills, and readiness. Its [learning loop](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) is Understand → Implement → Practice → Explain → Review → Validate. Prepared notes are a starting resource, not proof of practical mastery.

Core skills are endpoint semantics, sort-and-merge frontiers, three-region insertion, earliest-finish selection with a correctness proof, conflict detection, and active-interval counting by events or a heap. Scheduling concepts are taught here without a Premium subscription. Offline shortest-covering-interval queries are **optional advanced**; the guide supplies preparation/cleanup helpers and reasoning, not a full Hard-problem solution.

### Reuse prerequisites and test your starting point

| Existing reference | Reuse | Quick demonstration |
| --- | --- | --- |
| [Phase 0 foundations](./DSA_Interview_Roadmap.md#phase-0-foundations) | Sorting, invariants, costs, numeric bounds | Trace one comparison-based sort and explain which property the sorted order gives |
| [Java Arrays](./Java_Array.md#8-multidimensional-and-jagged-arrays) | int[][] rows and array references | Explain outer-array order versus values inside one row |
| [Java Lists](./Java_List_ArrayList_LinkedList.md) | ArrayList, element references, conversions | Explain why a list of int[] does not copy its rows |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#156-arrayssort) | Sorting time/storage, output convention | Include a copied input and library sort workspace in an analysis |
| [Heap concepts](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#8-java-21-priorityqueue) | Head order, offer/poll/peek, capacity | Choose a min-heap when the earliest ending booking must be released |
| [Heap comparators](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#9-comparators-and-numeric-safety) | Comparison signs, overflow, stable keys | Give a failing subtraction-comparator pair |
| [Binary Search](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) | Candidate regions and monotone boundaries | Distinguish a half-open index region from a booking's time interval |
| [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets) | Sorting versus buckets/hashing | Explain why a bucket per timestamp can be infeasible on a sparse domain |

If one demonstration is uncertain, repair that prerequisite rather than assuming document ownership establishes mastery. No full custom heap, sorting library, advanced Graph algorithm, or DP implementation is required for this chapter.

### Coverage map

| Roadmap requirement | Dedicated treatment |
| --- | --- |
| Define endpoint inclusion and touching behavior | Sections 2–4, with empty-range guards and mixed-bound reasoning |
| Sort by start and maintain a merge frontier | Sections 5–6 |
| Insert into sorted/disjoint data | Section 7 |
| Keep most compatible intervals / minimize removals | Sections 8–9, including exchange proof and counterexamples |
| Sweep or heap with endpoint-sensitive ties | Sections 10–12 |
| Java row copying, safe comparators, output conversion | Sections 5, 14–16 |
| Merge drill: nested/equal-start/disjoint/touching/points | Section 6 traces and boundary table |
| Booking drill: maximum concurrency with end/start ties | Sections 11–12, matching original examples |
| Unfamiliar recognition and defensible costs | Sections 15, 17–20 |
| Optional offline queries | Section 13; not an ordinary readiness requirement |

### Code and contract conventions

All Java fences are **members of one scratch class named IntervalsExamples** with `import java.util.*;`. Combine them in document order. A record's body contains its own instance members as shown. No external dependency is needed.

- Educational calls are single-threaded with feasible allocations. Arrays have at most **100000 interval rows** as a size precondition. Coordinates may be any int, including negatives and MIN_VALUE/MAX_VALUE; time is an ordered numerical axis, not a date/time-zone API.
- Required null arrays/rows/objects cause **NullPointerException (NPE)**. An interval row must have **exactly two entries** and start<=end; violations cause **IllegalArgumentException (IAE)**. Invalid input is rejected, not silently reversed or sorted within a row. Local APIs can impose a stricter domain.
- All ordinary algorithm APIs preserve input outer order, row contents, and row identities. Read-only input rows may alias each other. Returned int[][] arrays and each returned row are fresh containers, with no shared mutable rows between answers or input.
- Closed-range algorithms accept start==end as one point. Booking APIs accept empty half-open rows but ignore them after full validation. Activity selection instead requires start<end and **rejects zero-duration rows**, keeping the optimization domain explicit.
- No hidden cancellation, weighted reward, travel time, cleanup gap, recurrence, or resource capacity exists. State such a rule before adapting an algorithm.
- All O(n) bounds include constant empty-input setup; use n+1/log(n+1) when writing a formula that must cover n=0. Auxiliary space excludes input and required returned output but includes copies, conversion buffers, heaps, and sort workspace.
- `List<int[]>` holds array references. Output construction/copying is real work even when its returned storage is excluded from auxiliary space.

The common validation and copying helpers are intentionally small. They let the algorithm explanations focus on the invariant without leaving shape/null policies implicit.

```java
private static void validateInterval(int[] interval) {
    Objects.requireNonNull(interval, "interval");
    if (interval.length != 2 || interval[0] > interval[1]) {
        throw new IllegalArgumentException("expected [start,end] with start <= end");
    }
}

private static void validateAll(int[][] intervals) {
    Objects.requireNonNull(intervals, "intervals");
    for (int[] row : intervals) {
        validateInterval(row);
    }
}

static int[][] copyIntervals(int[][] intervals) {
    validateAll(intervals);
    int[][] copy = new int[intervals.length][];
    for (int i = 0; i < intervals.length; i++) {
        copy[i] = intervals[i].clone();
    }
    return copy;
}

private static int[][] nonemptyBookings(int[][] bookings) {
    validateAll(bookings);
    int count = 0;
    for (int[] row : bookings) {
        if (row[0] < row[1]) {
            count++;
        }
    }
    int[][] copy = new int[count][];
    int at = 0;
    for (int[] row : bookings) {
        if (row[0] < row[1]) {
            copy[at++] = row.clone();
        }
    }
    return copy;
}
```

copyIntervals gives independent rows even when two input slots refer to the same row. nonemptyBookings validates **all** rows before filtering; an invalid later row cannot be hidden behind an empty first booking. Both take O(n+1) time for two-entry rows. The returned copy is O(n) storage when used by another algorithm.

## 2. What an interval represents

An interval describes an uninterrupted part of an ordered axis. A classroom reservation might occupy times from 9 to 11. A machine's service period, a measurement tolerance, or a covered numerical range can use the same two-boundary representation.

The numbers alone do not state whether the exact beginning or ending point belongs. A booking ending at 11 may release the classroom for another booking beginning at 11. A closed measurement range can instead include that shared boundary in both ranges.

Original overlap example, **closed ranges**:

```text
time:  1   2   3   4   5   6
A:     [===========]           [1,4]
B:             [===========]   [3,6]
shared:        [===]           [3,4]
```

A and B overlap because at least one point belongs to both; here an entire segment does. “One interval begins earlier” alone does not tell you whether they overlap.

If A=[1,4] and B=[4,6], only point 4 is shared under closed semantics. If they are A=[1,4) and B=[4,6), point 4 belongs only to B. The intervals **touch at a boundary** in either description, but the overlap answer differs.

### Three different interval questions

| Question | Required result | Why the state differs |
| --- | --- | --- |
| Which locations are covered by at least one range? | A union representation | Combine ranges without remembering their original identities |
| How many activities can one resource perform? | Maximum compatible subset/count | Discard conflicting choices according to a proved optimization rule |
| How many resources are needed for all bookings? | Maximum simultaneous occupancy | Preserve multiplicity: two identical bookings can require two resources |

Merging identical bookings into one range destroys the count of simultaneous demand. Keeping one “best” activity does not represent the union of all covered time. Match the operation to the output before choosing code.

### Real-valued time versus integer positions

Unless explicitly stated otherwise, intervals here are subsets of the **real-number line**, with integer endpoints used for storage. Thus (3,4) is nonempty: 3.5 belongs. A closed [3,3] contains one point. An application covering only integer IDs has a different domain: [1,2] and [3,4] have no missing integer between them, although they leave a real-valued gap.

This distinction affects length and possible coalescing. Duration of [a,b) is b-a; the number of integer points in a closed [a,b] is b-a+1. Do not add 1 to meeting durations merely because endpoints are integers.

**Self-check Q1:** Why can merging reservations give the correct covered timeline but the wrong room count?

**Self-check Q2:** Why can an open interval with consecutive integer endpoints still be nonempty in this guide?

## 3. Endpoint conventions and empty ranges

### 3.1 Membership is the definition

| Convention | Membership test for a point t | Example members | At equal endpoints |
| --- | --- | --- | --- |
| Closed [s,e] | s<=t<=e | [1,3] includes 1,2,3 and intermediate real points | [3,3] is the singleton {3} |
| Half-open [s,e) | s<=t<e | [1,3) includes 1 and 2, excludes 3 | [3,3) is empty |
| Open (s,e) | s<t<e | (1,3) excludes both 1 and 3, includes 2 | (3,3) is empty |
| Other half-open (s,e] | s<t<=e | (1,3] excludes 1 and includes 3 | (3,3] is empty |

We use [s,e) for bookings. Start-inclusive/end-exclusive ranges let consecutive stays or classroom reservations share a checkout/end time without both occupying the resource at that instant. That is a modeling choice, not a rule every scheduling system must use.

```text
closed:     1 [-------] 3 [-------] 5
                 A=[1,3]   B=[3,5]
            A and B both contain 3.

half-open:  1 [-------) 3 [-------) 5
                 A=[1,3)  B=[3,5)
            A releases at 3; B begins at 3.

open:       1 (-------) 3 (-------) 5
                 A=(1,3)  B=(3,5)
            neither contains 3.
```

The shared drawing position is not proof of shared membership. Read the bracket/parenthesis and the contract.

### 3.2 Empty ranges need their own reasoning

A zero-duration half-open booking [3,3) consumes no time and overlaps nothing. The booking APIs validate it and then remove it from active counting and conflict checks. An isolated empty booking needs zero rooms. By contrast, a closed [3,3] can overlap a closed [1,5] at point 3.

A frequently copied condition is:

`a.start < b.end && b.start < a.end`

For **nonempty** half-open intervals, it correctly recognizes overlap. Applied to empty A=[3,3) and B=[1,5), it returns true even though A contains no point. Add explicit nonemptiness checks or derive a predicate that incorporates the intersection's width. Section 4 uses max(start)<min(end), which yields false for this empty case.

Do not generate end/start events for ignored empty bookings. End-before-start on [3,3) alone would temporarily produce active=-1. The final peak might accidentally remain zero, but “active counts bookings currently present” is no longer a valid invariant.

For activity selection, this guide rejects empty intervals rather than counting them as selectable zero-time objects. A mathematical optimization that includes empty sets could keep all of them; that would be a different contract. State the policy before deriving a count or removals.

### 3.3 Touching, overlap, and coalescing are separate concepts

| Pair | Do they overlap? | Can their union be written as one same-kind interval? |
| --- | --- | --- |
| [1,3] and [3,5] | Yes, at 3 | Yes: [1,5] |
| [1,3) and [3,5) | No | Yes: [1,5) |
| (1,3) and (3,5) | No | No: (1,5) would incorrectly add 3 |
| [1,3) and (3,5] | No | No single interval can include both sides without addressing the missing 3 |
| [1,3] and (3,5] | No shared point | Yes: [1,5] |

An API that merges **overlapping half-open components** can leave [1,3),[3,5) separate. An API that normalizes their covered union into coalesced half-open runs may combine them. Both can represent the same set; their required output shape differs. “Touching intervals never overlap” does not by itself decide a union-normalization policy.

For integer-only closed ranges, joining [1,2] and [3,4] may be allowed by an explicitly discrete coverage contract. Do not silently change the real-line rule to end+1>=nextStart: it changes semantics and end+1 can overflow at MAX_VALUE.

**Self-check Q3:** What does [3,3) contain, and how should a room-counting API treat it?

**Self-check Q4:** Why does the usual two-inequality half-open overlap test need care with empty intervals?

**Self-check Q5:** Why can two nonoverlapping half-open ranges still be coalesced?

**Self-check Q6:** Is [1,3] interchangeable with [1,3) merely because both endpoints are stored as ints?

## 4. Deriving overlap and disjointness

To find common points, take the later start and earlier end:

```text
left  = max(a.start, b.start)
right = min(a.end,   b.end)

left > right: no common candidate point
left < right: positive-width common interior
left == right: inspect membership of that exact point
```

For closed intervals, equality is enough because both ranges include every boundary. For uniformly half-open or uniformly open ranges, equality does not yield a common point; an intersection must have positive width. Endpoints stored as ints still describe real interior points.

### 4.1 Uniform closed and half-open predicates

```java
static boolean overlapsClosed(int[] a, int[] b) {
    validateInterval(a);
    validateInterval(b);
    return Math.max(a[0], b[0]) <= Math.min(a[1], b[1]);
}

static boolean overlapsHalfOpen(int[] a, int[] b) {
    validateInterval(a);
    validateInterval(b);
    return Math.max(a[0], b[0]) < Math.min(a[1], b[1]);
}
```

These methods are symmetric, input-preserving, O(1) time/space, and handle valid zero-length rows deliberately. The second predicate also describes whether two uniformly open real intervals have a nonempty intersection; it does not make their endpoint membership identical.

For **nonempty** intervals, the alternative “neither lies entirely before the other” gives:

| Model | Entirely before | Overlap |
| --- | --- | --- |
| Closed | a.end<b.start or b.end<a.start | a.start<=b.end and b.start<=a.end |
| Half-open | a.end<=b.start or b.end<=a.start | a.start<b.end and b.start<a.end |

The half-open row assumes both intervals are nonempty. The max/min predicate above is a convenient way to include empty-intersection reasoning.

### 4.2 Cases with diagrams

Here # marks the shared closed portion; Ø means empty. Each row states its model. Diagrams are schematic, not unit-scaled.

| Case and convention | Diagram | Intersection / overlap result |
| --- | --- | --- |
| Partial, closed [1,4],[3,6] | A=[1----4], B=[3----6], shared=[3#4] | [3,4], true |
| Containment, closed [1,8],[3,5] | A=[1--------8], B=..[3--5].. | [3,5], true |
| Identical, closed [2,6],[2,6] | A=B=[2------6] | [2,6], true |
| Disjoint, closed [1,2],[4,7] | [1--2] ..gap.. [4---7] | Ø, false |
| Touching, closed [1,3],[3,5] | [1----3]#[3----5] | {3}, true |
| Touching, half-open [1,3),[3,5) | [1----3)[3----5) | Ø, false |
| Equal start, closed [2,4],[2,7] | [2--4] inside [2------7] | [2,4], true |
| Equal end, closed [1,6],[4,6] | [1------6] contains [4--6] | [4,6], true |
| Point, closed [3,3],[1,5] | [1----{3}----5] | {3}, true |
| Point outside, closed [6,6],[1,5] | [1----5] .. {6} | Ø, false |
| Empty, half-open [3,3),[1,5) | [1----5), A=Ø at 3 | Ø, false |
| Touching, open (1,3),(3,5) | (1----3)(3----5), 3 missing | Ø, false |

A containment test alone misses partial overlap. Comparing only starts misses disjointness. Symmetry is a useful test property: overlap(a,b) must equal overlap(b,a).

### 4.3 Mixed endpoints: inspect the equality point

For mixed open/closed boundaries, use explicit inclusion flags. This small value model is a boundary-reasoning helper, not a different implementation of every later algorithm.

```java
record Range(int start, int end, boolean includeStart, boolean includeEnd) {
    Range {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
    }

    boolean isEmpty() {
        return start == end && !(includeStart && includeEnd);
    }

    boolean contains(int point) {
        boolean afterStart = point > start || (point == start && includeStart);
        boolean beforeEnd = point < end || (point == end && includeEnd);
        return afterStart && beforeEnd;
    }
}

static boolean overlapsRanges(Range a, Range b) {
    Objects.requireNonNull(a, "a");
    Objects.requireNonNull(b, "b");
    if (a.isEmpty() || b.isEmpty()) {
        return false;
    }
    int left = Math.max(a.start(), b.start());
    int right = Math.min(a.end(), b.end());
    return left < right
            || (left == right && a.contains(left) && b.contains(left));
}
```

For a positive-width intersection, every interior point lies in both valid nonempty ranges, regardless of endpoint flags. At equality, both must contain that exact point. Examples:

| A | B | Result and reason |
| --- | --- | --- |
| [1,3] | [3,5) | true; both contain 3 |
| [1,3) | [3,5] | false; A excludes 3 |
| [1,3] | (3,5] | false; B excludes 3 |
| (3,3) | [1,5] | false; A is empty |
| [3,3] | [3,3] | true; same point |
| (3,4) | (3,4) | true; positive real interior |

contains accepts an integer point because the helper needs to inspect stored endpoints; overlapsRanges still reasons about real interiors. Do not interpret (3,4) as empty just because no integer lies strictly between its endpoints.

**Self-check Q7:** Why does equality in max(start)<=min(end) work for closed intervals but not every mixed-bound pair?

**Self-check Q8:** Why is checking containment insufficient to determine overlap?


## 5. Sorting intervals in Java

### 5.1 Choose the key from the question

Unsorted ranges can hide the next useful boundary. Sorting imposes a direction in which some earlier decisions become final.

| Order | Useful question | What becomes easy | What it does not guarantee |
| --- | --- | --- | --- |
| Start ascending | Merge coverage; detect conflicts; admit active bookings | No unprocessed interval starts before the current one | Ends need not be sorted |
| End ascending | Keep the largest compatible activity count | The first available choice leaves the earliest release boundary | It does not directly maintain a merge frontier |
| Start ascending, end ascending tie | Deterministic merge/booking traces | Equal starts have a reproducible order | The end tie key is usually not required for correctness |
| End ascending, start ascending tie | Deterministic earliest-finish trace | Tied finishes use a clear order | A tie rule is not the Greedy proof |
| Descending key | Reverse sweeps or explicitly reversed objectives | Sometimes fits a backwards invariant | Reversing a sort is not automatically a valid algorithm |

For merging, the essential key is start ascending. Either end order works with an update that retains the maximum frontier end. For earliest-finish count selection on strictly positive-duration activities, any compatible earliest-finishing tie can be used; our secondary start key makes examples deterministic.

### 5.2 Why int[][] accepts a Comparator

int[][] is an object array whose elements are **int[] row references**. Therefore Arrays.sort can compare rows using extracted endpoints. The comparator does not sort the two endpoint values within a row.

A primitive int[] instead contains int values and has no Comparator overload. Boxing into Integer[] would change representation and allocation costs; interval sorting does not require that.

```java
static final Comparator<int[]> BY_START =
        Comparator.comparingInt((int[] a) -> a[0])
                .thenComparingInt(a -> a[1]);

static final Comparator<int[]> BY_END =
        Comparator.comparingInt((int[] a) -> a[1])
                .thenComparingInt(a -> a[0]);

static int[][] sortedByStart(int[][] intervals) {
    int[][] copy = copyIntervals(intervals);
    Arrays.sort(copy, BY_START);
    return copy;
}

static int[][] sortedByEnd(int[][] intervals) {
    int[][] copy = copyIntervals(intervals);
    Arrays.sort(copy, BY_END);
    return copy;
}
```

The shorter expression `Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]))` is also valid when the surrounding type supplies int[] inference. It sorts the caller's outer array directly. The methods above first make independent row copies because their contract preserves input and provides independent output rows.

Java 21 object-array sorting is stable and uses temporary reference storage in the documented implementation; stability preserves order only among rows that compare equal under the **entire comparator**. It is not a reason to depend on unspecified input order among meaningful ties. These methods take O(n log(n+1)+1) worst-case time and O(n+1) additional storage under the stated copying/sort model. [Arrays API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), [Comparator API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html)

### 5.3 A sorting trace

All rows in this table represent **closed ranges**. Sorting changes processing order, not membership.

Input:
`[[8,10],[1,4],[2,3],[4,7],[12,12],[8,9],[14,16]]`

| Sorted position | Row | Reason for position |
| --- | --- | --- |
| 0 | [1,4] | Smallest start |
| 1 | [2,3] | Next start, despite its smaller end |
| 2 | [4,7] | Starts at 4 |
| 3 | [8,9] | First start-8 row under the end tie key |
| 4 | [8,10] | Same start, larger end |
| 5 | [12,12] | Single-point closed interval |
| 6 | [14,16] | Largest start |

Notice [1,4] precedes [2,3] by start even though 4>3. An algorithm must not assume both columns become sorted together.

### 5.4 Safe numeric ordering

Comparators return a **sign**, not a distance. Use Integer.compare or comparingInt rather than endpoint subtraction. For a.start=MIN_VALUE and b.start=1, a.start-b.start wraps to MAX_VALUE and has the wrong sign. Casting the wrapped int result afterward does not repair it.

Direct relational checks such as nextStart<=currentEnd are safe for all int endpoints. For duration or inclusive integer size, widen **before** subtraction:

```java
static long duration(int[] interval) {
    validateInterval(interval);
    return (long) interval[1] - interval[0];
}

static long closedIntegerSize(int[] interval) {
    validateInterval(interval);
    return (long) interval[1] - interval[0] + 1;
}
```

For [MIN_VALUE,MAX_VALUE], duration is 4294967295 and closedIntegerSize is 4294967296. Both exceed int. The first is a real-axis length; the second counts integer points. A singleton [3,3] has duration zero but integer size one. The cast protects arithmetic, not a confused definition.

**Self-check Q9:** Why can int[][] use a Comparator while primitive int[] cannot?

**Self-check Q10:** Why does sorting by start not imply nondecreasing ends?

## 6. Merge intervals with a covered frontier

### 6.1 Define the requested output

**Educational API mergeClosed:** arbitrary valid closed ranges [s,e], including duplicate and point ranges. Return their union as start-sorted, pairwise nonoverlapping closed ranges. Touching closed ranges merge because they share a point. Negative/extreme endpoints are legal. Preserve all input rows and return independent output rows. Empty input returns an empty array.

This is a coverage question. Original row identities and how many inputs cover a point are intentionally lost.

A straightforward baseline repeatedly finds an overlapping pair, replaces it with their union, and searches again until no pair remains. One repeated all-pairs search can become much more expensive than a single quadratic pass; a naive implementation that rescans O(n^2) pairs after O(n) merges can take O(n^3) time. Merely checking each original pair once is insufficient to construct growing connected coverage correctly without further bookkeeping.

Sorting by start gives a simpler invariant and a bounded scan.

### 6.2 Derive the frontier

After sorting, let the last output range be [s,e] and the next input range [a,b].

- If a<=e, the next range overlaps the frontier. Their union is [s,max(e,b)]. Keep max so a nested range cannot shrink coverage.
- If a>e, a real gap separates them. Every later range starts at least at a, so none can reach back across that gap. The old frontier is final; begin a new range.

The current start never needs to move right. Start sorting ensured it was already the smallest start in its merged component.

```text
Unsorted ranges
    -> sort by start
    -> maintain the current covered frontier
    -> extend with max end when overlap is proved
    -> finalize when the next start is beyond the frontier
    -> return the ordered union
```

### 6.3 Java implementation

```java
static int[][] mergeClosed(int[][] intervals) {
    int[][] ordered = sortedByStart(intervals);
    List<int[]> merged = new ArrayList<>();
    for (int[] next : ordered) {
        if (merged.isEmpty()
                || next[0] > merged.get(merged.size() - 1)[1]) {
            merged.add(new int[]{next[0], next[1]});
        } else {
            int[] last = merged.get(merged.size() - 1);
            last[1] = Math.max(last[1], next[1]);
        }
    }
    return merged.toArray(int[][]::new);
}
```

The output frontier is a newly allocated row; changing it does not mutate ordered or the caller. List-to-array conversion copies row references into a new outer array, not row contents. That is safe here because those rows were created for this output and no two outputs share one row.

### 6.4 Full dry run

**Convention: closed.** Use the seven sorted rows from Section 5.

| Step | Next input | Frontier before | Condition | Action | Result after step |
| --- | --- | --- | --- | --- | --- |
| 1 | [1,4] | None | First row | Start component | [[1,4]] |
| 2 | [2,3] | [1,4] | 2<=4 | Nested; max(4,3)=4 | [[1,4]] |
| 3 | [4,7] | [1,4] | 4<=4 | Shared endpoint; extend to 7 | [[1,7]] |
| 4 | [8,9] | [1,7] | 8>7 | Finalize old component | [[1,7],[8,9]] |
| 5 | [8,10] | [8,9] | 8<=9 | Equal start; extend to 10 | [[1,7],[8,10]] |
| 6 | [12,12] | [8,10] | 12>10 | Start point component | [[1,7],[8,10],[12,12]] |
| 7 | [14,16] | [12,12] | 14>12 | Start component | [[1,7],[8,10],[12,12],[14,16]] |

Schematic frontier evolution:

```text
[1----4] contains [2--3]
[1----4] touches [4----7]     -> [1----------7]
                                         gap
[8--9] plus [8----10]        -> [8----10]

Final: [1,7]  gap  [8,10]  gap  {12}  gap  [14,16]
```

### 6.5 Invariant and correctness

**Invariant after each processed row:** merged is ordered, its rows are pairwise nonoverlapping, and its union equals exactly the union of the processed input prefix. All rows except the last are final; the last is the only component a later row might extend.

Initialization holds with no ranges. On overlap, replacing only the last end by max preserves exactly the union of two overlapping closed intervals. It cannot remove nested coverage or introduce a gap. Earlier components are separated from the last and cannot be affected.

On a gap, every unprocessed start is at least next.start. Thus no later interval can bridge backward to an earlier finalized component; adding the next range preserves order and separation. At termination every input row has been processed, so the invariant gives the requested union.

This is why the scan does not compare the next range to **every** previous input. The finalized components are unreachable under the sorted-start invariant, and the last component summarizes all relevant earlier coverage.

### 6.6 Half-open merging requires an output policy

The following optional companion illustrates endpoint reasoning without changing the main closed-range contract.

**mergeHalfOpen:** validate all rows, discard empty [t,t) ranges, and return nonempty half-open ranges. With coalesceTouching=false, combine only actual overlaps. With true, also join adjacent runs to obtain a more compact union representation. Input/returned-row ownership matches mergeClosed.

```java
static int[][] mergeHalfOpen(int[][] intervals, boolean coalesceTouching) {
    int[][] ordered = nonemptyBookings(intervals);
    Arrays.sort(ordered, BY_START);
    List<int[]> merged = new ArrayList<>();
    for (int[] next : ordered) {
        if (merged.isEmpty()) {
            merged.add(new int[]{next[0], next[1]});
            continue;
        }
        int[] last = merged.get(merged.size() - 1);
        boolean join = coalesceTouching
                ? next[0] <= last[1] : next[0] < last[1];
        if (join) {
            last[1] = Math.max(last[1], next[1]);
        } else {
            merged.add(new int[]{next[0], next[1]});
        }
    }
    return merged.toArray(int[][]::new);
}
```

| Input endpoints | Contract | Exact output |
| --- | --- | --- |
| [[1,3],[3,5]] | Closed | [[1,5]] |
| [[1,3],[3,5]] | Half-open, overlap only | [[1,3],[3,5]] |
| [[1,3],[3,5]] | Half-open, coalesce touching | [[1,5]] |
| [[3,3],[1,5]] | Half-open, either policy | [[1,5]] |
| [[3,3]] | Closed | [[3,3]] |
| [[3,3]] | Half-open, either policy | [] |
| [[2,9],[2,4],[4,6]] | Closed | [[2,9]] |
| [[-5,-2],[-1,1]] | Closed | [[-5,-2],[-1,1]] |
| [] | Either model | [] |

The half-open implementation maintains the corresponding overlap-only or coalesced frontier invariant after empty filtering. It does not implement arbitrary mixed open/closed unions, where boundary inclusion must be retained as data.

### 6.7 Costs and alternative cases

For n rows and m output ranges, validation/copying is O(n+1), sorting O(n log(n+1)+1), scanning O(n), and output construction O(m). Total worst-case time is O(n log(n+1)+1).

Auxiliary space is O(n+m+1)=O(n+1): private row copies, sort workspace, and the temporary ArrayList reference buffer/conversion coexist. Returned outer/inner arrays occupy O(m+1) output. The scalar frontier itself is constant-size, but this full preserving implementation is not O(1) auxiliary.

If input is already sorted by start, a suitably contracted merge scan can avoid sorting and run in O(n+1) time. Preserve or explicitly permit mutation; a sorted precondition does not by itself grant ownership of row contents.

**Self-check Q11:** Why must a merge update use max(oldEnd,nextEnd) rather than assign nextEnd?

**Self-check Q12:** What proves a finalized merged component can never be extended by an unprocessed interval?

## 7. Insert through three ordered regions

### 7.1 A stronger input contract removes sorting

**insertClosed:** existing ranges are valid **closed** ranges, already sorted and pairwise nonoverlapping. Therefore each previous end is strictly less than the next start. The method checks this condition and rejects an invalid order/overlap with IAE. The inserted interval is another valid closed range; points are allowed. Return the normalized union with fresh rows, preserving all inputs.

The input has already done the ordering work. Sorting again would replace an O(n) task with unnecessary O(n log n) preparation.

Think of the input as three consecutive regions:

```text
A: entirely before new range
B: overlaps the growing inserted frontier
C: entirely after that frontier

emit A unchanged -> combine B into one range -> emit C unchanged
```

“Unchanged” describes values in the output; the preserving implementation still copies each output row.

### 7.2 Derive the boundaries

Let the candidate frontier initially be [s,e].

1. Region A ends before s: existing.end<s. Those rows cannot overlap the insertion.
2. Region B starts no later than the growing e: existing.start<=e. Extend s with min and e with max as each overlapping row is consumed.
3. The remaining rows start after e. Because existing rows are ordered/disjoint, all belong to C.

At the first B row, not belonging to A guarantees its end>=s, while start<=e gives overlap. For later B rows the frontier already covers the previous consumed range; the ordered disjoint structure makes the same test sufficient. Expanding the frontier left does not reach an A row because A and the first B row were already separated in the valid existing collection.

### 7.3 Java implementation with exact output allocation

This version first locates the three regions, then allocates the exact output size and copies rows. It avoids an O(n) temporary List buffer as well as sorting.

```java
static int[][] insertClosed(int[][] existing, int[] inserted) {
    validateAll(existing);
    validateInterval(inserted);
    for (int i = 1; i < existing.length; i++) {
        if (existing[i - 1][1] >= existing[i][0]) {
            throw new IllegalArgumentException("closed rows must be sorted and disjoint");
        }
    }

    int left = 0;
    int start = inserted[0];
    int end = inserted[1];
    while (left < existing.length && existing[left][1] < start) {
        left++;
    }

    int right = left;
    while (right < existing.length && existing[right][0] <= end) {
        start = Math.min(start, existing[right][0]);
        end = Math.max(end, existing[right][1]);
        right++;
    }

    int[][] result = new int[left + 1 + existing.length - right][];
    int at = 0;
    for (int i = 0; i < left; i++) {
        result[at++] = existing[i].clone();
    }
    result[at++] = new int[]{start, end};
    for (int i = right; i < existing.length; i++) {
        result[at++] = existing[i].clone();
    }
    return result;
}
```

No subtraction of endpoints or end+1 is needed. The validation compares values directly. inserted may alias a row of existing when the existing collection itself is valid; the method only reads it and creates a separate merged output row.

### 7.4 Detailed trace

**Convention: closed.**

existing=`[[0,1],[3,4],[6,8],[10,13],[16,18]]`, inserted=`[4,11]`.

| Step | Existing row | Frontier before | Region / comparison | Action | Frontier / copied prefix |
| --- | --- | --- | --- | --- | --- |
| 1 | [0,1] | [4,11] | A: 1<4 | Preserve before region | A=[[0,1]] |
| 2 | [3,4] | [4,11] | Not A; 3<=11 | Include touching closed range | [3,11] |
| 3 | [6,8] | [3,11] | B: 6<=11 | Contained in frontier | [3,11] |
| 4 | [10,13] | [3,11] | B: 10<=11 | Extend end | [3,13] |
| 5 | [16,18] | [3,13] | C: 16>13 | Stop combining | Suffix=[[16,18]] |
| 6 | Allocate/copy | [3,13] | left=1, right=4 | 1 before + 1 merged + 1 after | [[0,1],[3,13],[16,18]] |

The scan found boundaries, then materialized three fresh output rows. Input order and all five original rows remain intact.

| Boundary case | Existing / inserted | Output or rejection |
| --- | --- | --- |
| Empty existing | [] / [2,2] | [[2,2]] |
| Before all | [[3,5],[8,10]] / [0,1] | [[0,1],[3,5],[8,10]] |
| After all | [[3,5],[8,10]] / [12,14] | [[3,5],[8,10],[12,14]] |
| Nested new range | [[1,9]] / [3,4] | [[1,9]] |
| New covers all | [[1,2],[4,6]] / [0,8] | [[0,8]] |
| Touch both sides | [[1,3],[5,7]] / [3,5] | [[1,7]] |
| Existing touches under closed model | [[1,3],[3,5]] / [8,9] | IAE; precondition is already disjoint closed input |
| Existing out of order | [[8,9],[1,2]] / [4,5] | IAE |
| New reversed | [[1,2]] / [5,4] | IAE |

### 7.5 Correctness and costs

A contains exactly the intervals proved disjoint to the left. The combining loop maintains the exact union of inserted and consumed B rows as one closed frontier. On exit, no remaining start is within that frontier; sorted order puts every remaining row to its right. Emitting A, the frontier, and C therefore preserves precisely all covered points, preserves order, and leaves no overlap between output rows.

Every index moves forward and every row is validated/copied at most a constant number of times. Time is **O(n+1)**; the code scans some rows more than once across its validation, boundary-finding, and materialization phases, still a constant number of linear passes. Additional non-output storage is **O(1)**. Returned outer array and rows are O(m+1), m<=n+1.

Under this guide's convention, result is required output even while it is being filled. There is no hidden list-to-array reference buffer or sorted copy in this implementation. If an interviewer includes output in “extra space,” report O(n+1) total additional storage instead.

### 7.6 Derive changed endpoint rules

For **nonempty half-open** insertion with an overlap-only output policy:

| Region | Closed | Half-open overlap-only |
| --- | --- | --- |
| Entirely before | existing.end<new.start | existing.end<=new.start |
| Combine while start reaches frontier | existing.start<=frontier.end | existing.start<frontier.end |
| Entirely after | existing.start>frontier.end | existing.start>=frontier.end |

A half-open insertion API must also specify what happens to empty existing rows and an empty insertion, whether touching runs should be coalesced, and what “disjoint” means. Replacing one <= with < inside the closed implementation while leaving all other assumptions unchanged is not a complete adaptation.

**Self-check Q13:** Which input property makes sorting unnecessary for insertion?

**Self-check Q14:** Why can the right boundary of the overlap region grow while scanning?

## 8. Select compatible activities

### 8.1 Optimize the count, not the union

Suppose one machine can perform one activity at a time. Activity [s,e) occupies the machine starting at s and releases it at e. Touching activities are compatible. We want the maximum number of activities, with every activity having equal reward one.

Merging conflicts would create a coverage range, not choose which tasks to keep. A selection algorithm must deliberately retain some activities and reject others.

**maxCompatibleActivities:** valid int[][] rows, additionally require start<end. Zero-duration activities cause IAE; negative times are fine. Each row is a separate selectable activity, including equal-valued rows. Return only the maximum count. Inputs are preserved. Empty input returns zero. **minimumActivityRemovals** returns n minus that count under the same domain and compatibility rule.

### 8.2 Why “earliest start” fails

**Convention: nonempty half-open activities.**

```text
long activity:   [0----------------------12)
short sequence:   [1--3)[3--5)[5--7)[7--9)
```

Choosing [0,12) first leaves room for none of the four short activities. Choosing by earliest finish retains all four. Starting earlier can mean occupying the resource much longer.

The key question is: **which first choice leaves every later possibility at least as available?** Finishing earlier leaves a superset of the feasible future start times. That suggests earliest finish, and Section 9 proves it.

### 8.3 Derive the scan

Sort activities by end. Maintain the end of the last selected activity. For each next candidate:

- If none has been selected, select it.
- Otherwise select it when candidate.start>=lastEnd.
- If it conflicts, skip it; do not move lastEnd to the skipped activity's end.

A boolean “have selected” avoids a fabricated initial timestamp such as -1. Negative and MIN_VALUE starts are valid; a sentinel inside the endpoint domain can incorrectly reject the first activity.

```java
private static int[][] strictActivities(int[][] activities) {
    int[][] copy = copyIntervals(activities);
    for (int[] row : copy) {
        if (row[0] == row[1]) {
            throw new IllegalArgumentException("activities require start < end");
        }
    }
    return copy;
}

static int maxCompatibleActivities(int[][] activities) {
    int[][] ordered = strictActivities(activities);
    Arrays.sort(ordered, BY_END);
    boolean haveSelected = false;
    int lastEnd = 0;
    int kept = 0;
    for (int[] current : ordered) {
        if (!haveSelected || current[0] >= lastEnd) {
            kept++;
            lastEnd = current[1];
            haveSelected = true;
        }
    }
    return kept;
}

static int minimumActivityRemovals(int[][] activities) {
    int kept = maxCompatibleActivities(activities);
    return activities.length - kept;
}
```

This educational API uses a half-open compatibility interpretation. A platform statement may write [start,end] while explicitly defining touching as nonconflicting. Follow its **stated compatibility**, not bracket typography alone. The official Non-overlapping Intervals problem explicitly permits touching and requires start<end. [Official statement](https://leetcode.com/problems/non-overlapping-intervals/)

### 8.4 Selection trace

**Convention: half-open, strictly positive duration.**

Input: `[[0,12],[1,3],[3,5],[5,7],[7,9],[2,6]]`.

| End-sorted candidate | Last selected end before | Compatible? | Action | Selected count / end |
| --- | --- | --- | --- | --- |
| [1,3) | None | First | Keep | 1 / 3 |
| [3,5) | 3 | 3>=3 | Keep touching successor | 2 / 5 |
| [2,6) | 5 | 2<5 | Skip; preserve end 5 | 2 / 5 |
| [5,7) | 5 | 5>=5 | Keep | 3 / 7 |
| [7,9) | 7 | 7>=7 | Keep | 4 / 9 |
| [0,12) | 9 | 0<9 | Skip | 4 / 9 |

Maximum kept=4; minimum removals=6-4=2.

| Input | Kept / removals |
| --- | --- |
| [] | 0 / 0 |
| [[2,5]] | 1 / 0 |
| [[1,3],[3,5]] | 2 / 0 |
| [[1,4],[1,4],[1,4]] | 1 / 2 |
| [[-5,-2],[-2,0],[0,3]] | 3 / 0 |
| [[3,3]] | IAE under the activity contract |

### 8.5 Invariant and cost

The selected prefix is compatible. lastEnd is its final finish boundary; it changes only when a candidate is accepted. Among maximum-length selections for a scanned prefix, an earliest possible last finish preserves the best future opportunities. The exchange proof explains why the Greedy decisions can participate in a globally optimal selection.

Validation/copy O(n), end sort O(n log(n+1)), scan O(n): total O(n log(n+1)+1) time. The returned count/removal count is O(1) output. The private copy and object-sort workspace are O(n+1) auxiliary; the scan alone has constant state. If actual chosen activities are requested, retain their IDs/rows and count the output; do not imply this scalar API already returns a schedule.

For strictly positive closed activities that share endpoints, compatibility would be next.start>lastEnd. That is a different tie rule with the same style of exchange proof. Empty or mixed-bound activities require their own contract.

**Self-check Q15:** Why is merging conflicts not a solution to maximum-count activity selection?

**Self-check Q16:** Why must skipping an activity leave lastEnd unchanged?

## 9. Prove the earliest-finish choice

### 9.1 What an exchange argument does

A Greedy rule is a hypothesis until it is shown compatible with an optimal result. An **exchange argument** begins with an optimal selection, changes its first decision to the proposed Greedy choice without reducing its size, and then repeats the argument on what remains.

Assumptions here: fixed independent nonempty half-open activities, one resource, equal reward, no setup/travel times, and compatibility start>=previousEnd. We optimize the number of selected activities.

### 9.2 Exchange the first activity

Let G be an activity with minimum end among currently eligible activities. Take an optimal selection O and let O1 be its first activity in chronological order.

1. Because G finishes earliest, end(G)<=end(O1).
2. Every activity following O1 begins at or after end(O1).
3. Therefore every such follower also begins at or after end(G).
4. Replace O1 by G, keeping all followers. The selection remains compatible and has the same number of activities.

If G already is O1, no change is needed. Otherwise G cannot be one of O's later activities under this strictly positive-duration chronological model: its earliest finish is no later than O1's, so it cannot start after O1 while also finish no later. The replacement does not create a repeated row identity.

```text
optimal first:       [----- O1 -----)   [ follower ) [ follower )
greedy replacement: [-- G --)           [ follower ) [ follower )
                                      same later activities fit
because finish(G) <= finish(O1)
```

Thus **some** optimal solution begins with G. After selecting G, the remaining question is the same maximum-count problem restricted to activities starting at/after end(G). Apply the argument again. Finite selection and removal of processed candidates ensure termination. The end-sorted scan implements those repeated safe choices.

The proof need not claim G belongs to every optimal solution or is the only best first choice. It proves a Greedy-compatible optimum exists.

### 9.3 A staying-ahead view

After the same number of selected activities, the Greedy schedule can finish no later than an optimal competitor's corresponding activity. Earlier release cannot make a feasible later start unavailable. This is another way to express the dominance used by the exchange proof.

The crucial order is release time, not activity length by itself or how early an activity starts. The input may contain negative times, equal finishes, or nested activities; the end comparison remains valid under the stated domain.

### 9.4 Counterexamples to tempting alternatives

| Rule | Half-open counterexample | Wrong outcome versus optimum |
| --- | --- | --- |
| Choose earliest start | [0,12), [1,3), [3,5), [5,7), [7,9) | Long first gives 1; four consecutive activities fit |
| Choose a globally shortest duration, then discard all conflicts | [0,5), [4,6), [5,10) | The unique shortest [4,6) blocks both long neighbors; optimum keeps [0,5),[5,10) |
| Choose the latest finish first and allow only later successors | [0,2), [2,4), [0,10) | [0,10) gives 1; two short activities fit |
| Keep earliest finish to maximize total reward | [0,2) reward 1, [2,4) reward 1, [0,4) reward 10 | Count-optimal two have reward 2; reward optimum is 10 |

The shortest-duration counterexample concerns selecting the shortest interval **anywhere** and rejecting all conflicts, not the valid reverse-time analogue of earliest finish with a separate backward proof. State the proposed algorithm fully before evaluating it.

Weighted interval scheduling, several alternative objectives, and additional setup constraints may need different states/proofs and sometimes later DP. You can recognize the limitation without implementing those extensions now.

### 9.5 Why minimum removals is the complement

Any retained compatible set of size k requires removing n-k activities. Maximizing k therefore minimizes removals. Conversely, a removal set leaving a compatible collection of size n-r establishes r removals; an optimum cannot beat the complement of the maximum compatible count.

This equivalence assumes each row counts once and all removals have equal cost. It does not transform a weighted removal-cost problem into unweighted counting.

**Self-check Q17:** Which inequality makes replacing an optimal first activity with G safe?

**Self-check Q18:** Why does the proof fail as a justification for maximizing arbitrary rewards?


## 10. Detect meeting conflicts

### 10.1 A yes/no question about all bookings

**canAttendAll:** each row is a half-open booking [s,e). Validate every row, ignore empty [t,t) bookings, preserve inputs, and return whether all remaining bookings can share one resource. Equal positive-duration bookings conflict; end/start ties do not. Empty input or only empty bookings returns true.

A direct baseline checks every pair for overlap: O(n^2) time, constant scan state if no copies are needed. That can be reasonable for tiny input, but sorting starts exposes a linear conflict scan.

### 10.2 Why adjacent checks suffice

Sort nonempty bookings by start. If every adjacent pair satisfies previous.end<=current.start, the whole ordered collection is compatible: each earlier booking has ended before its immediate successor begins, and every still-later start is at least that successor's start.

Conversely, if an earlier range reaches into any later booking, it also reaches the start of its immediate successor unless an even earlier adjacent conflict was already found. Hence at least one adjacent pair detects a conflict; there is no need to test every nonadjacent pair.

This is an **early-return conflict detector**. If you wanted to keep scanning and describe all covered regions, a previous-row end would not replace a maximum merged frontier. For [1,10),[2,3),[4,5), the detector already finds the first conflict; a coverage algorithm must remember 10.

```java
static boolean canAttendAll(int[][] bookings) {
    int[][] ordered = nonemptyBookings(bookings);
    Arrays.sort(ordered, BY_START);
    for (int i = 1; i < ordered.length; i++) {
        if (ordered[i][0] < ordered[i - 1][1]) {
            return false;
        }
    }
    return true;
}
```

### 10.3 Dry run and boundaries

**Convention: half-open.** Input `[[6,8],[1,4],[4,7],[5,5]]`. Empty [5,5) is validated then ignored. Ordered nonempty rows are [1,4),[4,7),[6,8).

| Next pair | Comparison | Meaning | Result so far |
| --- | --- | --- | --- |
| [1,4),[4,7) | 4<4 is false | First releases exactly when second begins | No conflict |
| [4,7),[6,8) | 6<7 is true | Both active during [6,7) | Return false |

| Input endpoints under half-open semantics | Answer |
| --- | --- |
| [] | true |
| [[3,3]] | true; empty booking ignored |
| [[1,3],[3,5]] | true |
| [[1,5],[3,3]] | true; empty booking does not conflict |
| [[1,5],[1,2]] | false; equal start |
| [[2,6],[2,6]] | false; multiplicity is real |
| [[5,2]] | IAE |
| [[1,2],null] | NPE, even if another pair could establish a quick answer |

Validation/copying and scanning are O(n+1); sorting is O(n log(n+1)+1), giving that total worst-case time. The preserving implementation uses O(n+1) auxiliary copy/sort storage, with O(1) output. Already sorted valid nonempty input admits a linear scan; include validation if the API promises it.

The roadmap lists **Meeting Rooms as A — Additional, Easy, LeetCode Premium**. The complete concept and educational API above require no Premium access. A yes/no conflict answer cannot tell whether peak demand is 2, 3, or 100; use counting when the output asks for resources.

**Self-check Q19:** Why are adjacent checks sufficient here but not a replacement for every merge-frontier operation?

**Self-check Q20:** How do an empty booking and two equal nonempty bookings affect conflict detection?

## 11. Sweep through endpoint events

### 11.1 Replace intervals by changes

Imagine moving a vertical line from earlier to later time. At a booking's start, demand increases by one. At its end, demand decreases by one. Between event coordinates, no booking changes membership, so the active count stays constant.

An event is **a change**, not an interval. Sorting events lets the algorithm process only coordinates where something happens, even when timestamps range across the entire int domain.

Original booking example used throughout the sweep/heap sections:

```text
A: [1-------4)
B:   [2-----------6)
C:       [4-5)
D:       [4-----------7)
E:             [6-------8)
F:                 [8-------10)

At t=4: A has ended; B,C,D are active -> 3 bookings.
At t=6: B has ended and E begins; D,E are active -> 2.
```

The input order may be arbitrary. A zero-duration booking [5,5) can also be present in the input; it is ignored after validation. We never turn it into demand events.

### 11.2 Equal-time ordering follows membership

For **nonempty half-open** [s,e), a booking ending at t is absent at t, while one starting at t is present. Process end events (-1) before start events (+1) at a tied timestamp.

For **closed** [s,e], an interval ending at t still occupies point t. Process starts before ends to measure occupancy at that point. A closed point [t,t] contributes a start and end at t and must count during that instant.

| Convention | Equal-time order | Touching [1,3] / [3,5] interpreted in that model |
| --- | --- | --- |
| Half-open [s,e) | End, then start | Peak 1 |
| Closed [s,e] | Start, then end | Peak 2 at t=3 |
| Uniform open (s,e) | Neither endpoint is present; reason about interiors and empties explicitly | Do not reuse a closed event rule without a proof |
| Mixed endpoints | Separate inclusion at the coordinate from occupancy just before/after it | One global start/end tie rule may be insufficient |

For uniform open nonempty ranges, endpoint occupancy and the open gaps between coordinates can be modeled explicitly; this guide's executable sweeps cover closed and left-closed/right-open intervals. A generic mixed-bound sweep may need phases for ends that exclude t, starts that include t, a point-at-t query, and transitions to the right of t. Retain boundary metadata rather than pretending bracket flags disappeared.

### 11.3 Java event model and sweep

Event deltas constructed by these wrappers are always +1/-1. peakFromEvents owns its event array and may sort it. It is a private helper; it is not an API for arbitrary deltas or malformed event sequences.

```java
record Event(int time, int delta) {}

private static int peakFromEvents(Event[] events, boolean closed) {
    Comparator<Event> order = Comparator.comparingInt(Event::time);
    if (closed) {
        order = order.thenComparing(
                Comparator.comparingInt(Event::delta).reversed());
    } else {
        order = order.thenComparingInt(Event::delta);
    }
    Arrays.sort(events, order);
    int active = 0;
    int peak = 0;
    for (Event event : events) {
        active += event.delta();
        peak = Math.max(peak, active);
    }
    return peak;
}

static int peakBookingsSweep(int[][] bookings) {
    int[][] nonempty = nonemptyBookings(bookings);
    Event[] events = new Event[2 * nonempty.length];
    int at = 0;
    for (int[] row : nonempty) {
        events[at++] = new Event(row[0], 1);
        events[at++] = new Event(row[1], -1);
    }
    return peakFromEvents(events, false);
}

static int peakClosedRanges(int[][] intervals) {
    validateAll(intervals);
    Event[] events = new Event[2 * intervals.length];
    int at = 0;
    for (int[] row : intervals) {
        events[at++] = new Event(row[0], 1);
        events[at++] = new Event(row[1], -1);
    }
    return peakFromEvents(events, true);
}
```

The int active count fits because n<=100000. The 2*n event length also fits under that size precondition. Endpoints themselves are never incremented or subtracted in a comparator, so MIN_VALUE/MAX_VALUE work.

### 11.4 Event-by-event trace

**Convention: half-open.** Use A–F above; events of the same sign at one time are interchangeable. The table reports changes, not a promise about input booking identity among equal events.

| Time | Event | Active before | Active after | Maximum |
| --- | --- | --- | --- | --- |
| 1 | Start +1 | 0 | 1 | 1 |
| 2 | Start +1 | 1 | 2 | 2 |
| 4 | End -1 | 2 | 1 | 2 |
| 4 | Start +1 | 1 | 2 | 2 |
| 4 | Start +1 | 2 | 3 | 3 |
| 5 | End -1 | 3 | 2 | 3 |
| 6 | End -1 | 2 | 1 | 3 |
| 6 | Start +1 | 1 | 2 | 3 |
| 7 | End -1 | 2 | 1 | 3 |
| 8 | End -1 | 1 | 0 | 3 |
| 8 | Start +1 | 0 | 1 | 3 |
| 10 | End -1 | 1 | 0 | 3 |

Peak=3. A and the arrivals at 4 do not coexist under half-open semantics. Processing starts first would fabricate a temporary count 4 at time 4 for these bookings.

### 11.5 State invariant and why intermediate ties are safe

After all events at time t, the half-open active count equals the number of input bookings containing t. During processing of several tied events, some ending bookings may not yet have been removed or some starts may not yet have been added; do not claim every intermediate row already represents the final physical state at t.

End-before-start first decreases from the count immediately to the left, then increases toward the count at t. Those intermediate counts cannot exceed the larger of the legitimate before/after counts. Hence updating peak after each correctly ordered event is safe.

For closed ranges, after processing starts at t and **before processing ends at t**, active equals occupancy at point t. The start-first phase exposes this true point peak; after ends, the count describes occupancy immediately to the right. For a closed [3,3], +1 then -1 correctly exposes occupancy one at 3.

An alternative half-open implementation can group all equal times, subtract all ends, add all starts, then update peak. A single **net delta per time** works for half-open counting after empty filtering, but loses the point-only peak needed for general closed counting. For touching closed ranges, the net change at their shared endpoint can be zero while true point occupancy increases.

### 11.6 Complexity and alternatives

There are 2p events for p nonempty bookings, p<=n. Validation/filtering/allocation is O(n+1); event sorting O(p log(p+1)+1); scanning O(p). Total O(n log(n+1)+1) time and O(n+1) auxiliary space, with O(1) scalar output. Closed counting uses n ranges including points and the same asymptotic bounds.

A dense difference array can replace sorting if the coordinate domain is small and its initialization/scan cost is acceptable. It costs O(n+U) time and O(U) storage for domain width U, not O(n) independent of coordinate range. Coordinate compression retains ordered event positions without allocating every missing timestamp.

If starts and ends are separately sorted, two coordinated pointers can also count half-open occupancy, taking the end branch first on equality and filtering empty ranges. End/start pairing is irrelevant to the count after event construction, but it matters for validation and interpreting original bookings. Sections 15 and 17 compare the full costs.

**Self-check Q21:** Why must an end precede a start at the same time for half-open bookings?

**Self-check Q22:** Why can grouping only a net delta miss a closed-interval peak?

## 12. Track active bookings with a min-heap

### 12.1 Keep the next release boundary visible

An event sweep globally sorts both starts and ends. Another approach sorts bookings by start and stores end times of currently active processed bookings. Before admitting a new booking beginning at s, remove every active end e<=s. Those bookings have released their resource under half-open semantics.

A **min-heap** exposes the earliest end. If the smallest end is still greater than s, every other stored end is also greater; none has finished. A max-heap would hide shorter expired bookings behind a long-running one.

### 12.2 Java implementation

```java
static int peakBookingsHeap(int[][] bookings) {
    int[][] ordered = nonemptyBookings(bookings);
    Arrays.sort(ordered, BY_START);
    PriorityQueue<Integer> endTimes = new PriorityQueue<>();
    int peak = 0;
    for (int[] current : ordered) {
        int start = current[0];
        while (!endTimes.isEmpty() && endTimes.peek() <= start) {
            endTimes.poll();
        }
        endTimes.offer(current[1]);
        peak = Math.max(peak, endTimes.size());
    }
    return peak;
}
```

The natural Integer order gives the smallest end at the head. peek inspects it without removing; poll removes it; offer inserts the new end; isEmpty guards inspection/unboxing on an empty queue. Duplicate end values remain separate heap elements because they represent separate bookings. Heap iteration or toString is not guaranteed to be sorted. [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)

Use a **while**, not one poll, for the claim that the heap represents the true current active set. Several resources may have become free since the preceding arrival.

### 12.3 Heap trace

**Convention: nonempty half-open bookings.** Use the same six A–F bookings. Sort by start then end. Every heap display below is an **ascending multiset view**, not Java iterator order or a backing-array layout.

| Step | Current booking | Heap before cleanup | Expired ends removed | Heap after offer | Current maximum |
| --- | --- | --- | --- | --- | --- |
| 1 | [1,4) | [] | [] | [4] | 1 |
| 2 | [2,6) | [4] | [] | [4,6] | 2 |
| 3 | [4,5) | [4,6] | [4] | [5,6] | 2 |
| 4 | [4,7) | [5,6] | [] | [5,6,7] | 3 |
| 5 | [6,8) | [5,6,7] | [5,6] | [7,8] | 3 |
| 6 | [8,10) | [7,8] | [7,8] | [10] | 3 |

At step 5, removing only one expired end would leave another inactive booking. At step 6, the final active heap has size one, although the historical maximum is three. The return value must be peak, not the final active size.

### 12.4 Exact invariant and correctness

Before each offer, after cleanup, the heap contains exactly the end times of **previously processed nonempty bookings active at current.start**, with multiplicity. After offer, it includes the current booking too. Unprocessed bookings with the same start may not yet be present; later iterations at that start add them, revealing the full simultaneous demand.

Sorted starts ensure no future iteration goes backward in time. Every previously admitted booking with end<=start is expired; repeatedly removing the minimum removes all and only such ends. Every end left is greater than start, so its booking contains that point. The current end is also greater than start because empty bookings were filtered.

Each loop records a legitimate active count. Maximum half-open occupancy can increase only at a start, and the iteration processing the last arrival at a tied start observes all bookings active there. Therefore the maximum heap size over offers equals the maximum simultaneous booking count.

### 12.5 Why the peak is the minimum resource requirement

If k bookings overlap at some time, each needs a different resource; k is a lower bound on any feasible allocation. Conversely, process starts in order and reuse any released resource. At an arrival with k still-active previous bookings, k resources are occupied; use a free resource if one exists, otherwise allocate one more. This creates a valid assignment using at most the largest active count observed.

Thus maximum concurrency is both a lower bound and an achievable resource count under the no-setup, identical-resource model. The code returns the number; it does not return resource IDs. Producing an assignment requires keeping identities/free-resource state and returning that extra output.

For closed ranges, an end equal to the next start is **not** released yet; a true active-end heap would remove ends strictly less than start and retain point intervals. The half-open empty-filtering wrapper must also change. Do not change the comparison while still dropping closed points.

### 12.6 Three different meanings a heap can have

| Question / invariant | Cleanup policy | Meaning of heap size |
| --- | --- | --- |
| True active processed bookings | Remove **all** ends<=current.start, then offer current end | Current active count; keep historical maximum |
| One latest end per allocated room | At most one earliest available room is reused for each arriving booking; replace that room's end | Number of allocated rooms; may include rooms currently idle |
| Best compatible subset for one resource | Earliest-finish Greedy scan, usually no active heap needed | Selected count is a different objective |

The second invariant can support a correct room-count algorithm using a single poll when one room is available, then an offer. Its heap need not equal the current active set. Calling such a valid allocated-room algorithm wrong solely because it polls once confuses two contracts. Calling its heap “all active bookings” would still be inaccurate.

For our **true active-set** implementation, all expired ends must leave. A heap with stale entries cannot be used as a current concurrency count, and final active size is not historical peak.

### 12.7 Costs and comparison with events

Let k be the largest active heap size, k<=p<=n. There are p offers and at most p polls in the whole execution; the inner while loop does not cause p polls at every arrival. Heap comparison work is O(p log(k+1)+1), and backing growth is amortized over insertions. Individual growth can copy a backing array; the complete batch bound still holds.

Total time is O(n+p log(p+1)+p log(k+1)+1), which is O(n log(n+1)+1). Auxiliary space is O(n+k+1)=O(n+1): the preserving sorted copy and sort workspace plus heap capacity proportional to its peak occupancy. The heap alone is O(k+1), but it is not the only working object. Returned peak is O(1).

| Sweep line | Active-end heap |
| --- | --- |
| Sorts endpoint events globally | Sorts bookings by start, releases ends dynamically |
| Occupancy follows event deltas | Occupancy follows retained active ends |
| Tie convention is in event ordering | Tie convention is in the expiration comparison |
| O(n) event objects/reference storage | O(n) preserving sort storage plus O(k) heap |
| Convenient for a whole timeline of changes | Convenient when earliest release and resource metadata matter |
| Neither automatically provides an assignment | Add booking/resource identities if an assignment is required |

**Self-check Q23:** Why must this active-set heap remove all expired ends before counting?

**Self-check Q24:** Why can a single-poll allocated-room algorithm still be correct under a different invariant?

## 13. Offline interval queries — optional advanced

**OPTIONAL ADVANCED.** This section motivates the roadmap's C-tier Hard **Minimum Interval to Include Each Query**. It does not block ordinary Intervals readiness. It presents invariants, a small trace, and partial helpers, not a complete platform solver.

### 13.1 Change processing order when queries are known

Suppose closed integer ranges are given, and each query x asks for the **smallest number of integer points** in a range containing x. If none contains x, return -1. The original query order must be restored. Repeated query values are separate output positions.

A simple baseline scans every interval for each query: O(n*q+n+q+1) time including required output setup; it may be adequate for tiny inputs.

When all queries are known in advance, process them in nondecreasing value order. Sort ranges by start. As x moves right, newly eligible ranges have start<=x; a range with end<x is permanently expired for all later queries.

Eligibility by start does not identify the smallest interval. Maintain candidates in a min-heap keyed by **inclusive length**, not by end time. This heap's objective differs from the active-end heap in Section 12.

### 13.2 Preserve original query positions

```java
record Query(int value, int originalIndex) {}
record Candidate(int end, long size) {}

static final Comparator<Candidate> BY_SIZE =
        Comparator.comparingLong(Candidate::size)
                .thenComparingInt(Candidate::end);

static Query[] offlineQueryOrder(int[] queries) {
    Objects.requireNonNull(queries, "queries");
    Query[] ordered = new Query[queries.length];
    for (int i = 0; i < queries.length; i++) {
        ordered[i] = new Query(queries[i], i);
    }
    Arrays.sort(ordered, Comparator.comparingInt(Query::value)
            .thenComparingInt(Query::originalIndex));
    return ordered;
}
```

The q<=100000 size precondition applies to this optional query array too. Values can be any int. The input queries are unchanged; Query records are immutable and newly constructed. Cost is O(q log(q+1)+1) time, O(q+1) records/output plus O(q+1) worst-case sort workspace. When used inside a query solver, this ordered array is intermediate auxiliary state, not the solver's required answers.

The interval's size is `(long) end-start+1`. Under the full int endpoint domain it may exceed int, so a general educational answer array would be long[]. The platform's own bounds/signature may be narrower.

### 13.3 Lazy removal and the correct heap invariant

At query x:

- All ranges with start<=x have been admitted exactly once.
- No not-yet-admitted range can contain x.
- Any candidate whose end<x is invalid now and forever later.
- Remove expired **heap heads** until the head is valid or the heap is empty.
- A valid head has minimum size among all valid admitted candidates; write its size to the query's original output index.

This partial helper performs only the head cleanup:

```java
private static void discardExpired(PriorityQueue<Candidate> candidates, int query) {
    while (!candidates.isEmpty() && candidates.peek().end() < query) {
        candidates.poll();
    }
}
```

Preconditions: candidates is nonnull, ordered by BY_SIZE, and its immutable entries were constructed from validated closed ranges already eligible by start. Each Candidate.size is positive and correctly computed from its source. No queued key is mutated.

**Important difference from the active-end heap:** this heap is ordered by size, not end. Expired candidates can remain buried behind a valid smaller candidate. The invariant is not “all stored candidates currently cover x.” Only the exposed head is validated before answering.

Why is head-only cleanup sufficient? If the smallest stored candidate is expired, removing it cannot lose a valid answer. Once the head is valid, every other stored candidate has size at least as large. Any expired non-head entry cannot improve the answer, and if it later reaches the head it will be removed then. Do not scan/delete arbitrary heap entries at every query; that defeats the intended efficiency.

For example, at x=4 a valid [4,4] of size 1 can sit above expired [0,2] of size 3. The expired range may remain stored without corrupting the valid answer 1. This does **not** justify using heap size as a current coverage count.

### 13.4 Original offline trace

Closed integer ranges: `[[0,5],[2,3],[4,9],[8,8]]`.
Queries in original order: `[8,3,6,3,-1]`.

Display candidates as (size,end), sorted **only for explanation**, not as a promise about heap iteration.

| Processed query / original index | Newly admitted by start | Expired heads removed | Candidate head after cleanup | Answer at original index |
| --- | --- | --- | --- | --- |
| -1 / 4 | None | None | Empty | answer[4]=-1 |
| 3 / 1 | [0,5] size 6; [2,3] size 2 | None | (2,3) | answer[1]=2 |
| 3 / 3 | None | None; end==query remains valid | (2,3) | answer[3]=2 |
| 6 / 2 | [4,9] size 6 | (2,3), then (6,5) | (6,9) | answer[2]=6 |
| 8 / 0 | [8,8] size 1 | None | (1,8) | answer[0]=1 |

Restored output order: `[1,2,6,2,-1]`. Query 3 occurs twice and must populate both original indexes. At x=8, the closed point [8,8] is valid; removing end<=query would incorrectly discard it.

The guide deliberately omits the complete admission/query/output loop. Derive it yourself only when investigating the optional challenge.

### 13.5 Bounds and limits

Sorting costs O(n log(n+1)+q log(q+1)+1). Each interval is admitted at most once and removed at most once, so total heap work is O(n log(n+1)+1), plus O(q+1) head inspection/output work. A useful total upper bound is **O(n log(n+1)+q log(q+1)+n+q+1)**; the coarser **O((n+q) log(n+q+1)+1)** is also valid.

It is not O(n+q) merely because interval/query pointers advance once; priority-queue operations and initial sorting remain. With already ordered inputs, monotone admission still combines with heap work.

General auxiliary storage is O(n+q+1) for a preserving interval copy/sort, ordered query records, heap, and library workspace. Required answer storage is O(q+1). Restoring output order is indexed assignment, not another search for the matching original query.

Online queries arriving in arbitrary order cannot necessarily use this one-way sweep without buffering/reordering. Dynamic insertion/removal or different coverage statistics require another data structure and proof; they are beyond ordinary scope here.

**Self-check Q25:** Why must an offline algorithm retain original query indexes?

**Self-check Q26:** Why is it acceptable for the size-ordered candidate heap to contain expired non-head entries?


## 14. Arrays, references, and ownership

### 14.1 An interval row is a mutable object

For int[][] intervals, the outer array stores row references. A row stores the two primitive endpoint values. Sorting the outer array moves references; assigning row[1] changes the endpoint inside one referenced object.

```text
original outer slots          row objects
slot 0 ---------------------> A = [8,10]
slot 1 ---------------------> B = [1,4]

shallow = original.clone()
slot 0 ---------------------> same A
slot 1 ---------------------> same B
```

Sorting shallow can reorder its slots to B,A without reordering original's slots A,B. But shallow[0][1]=6 edits B, so original[1] also sees [1,6]. An outer clone protects **outer order**, not mutable row contents.

After cloning every row, the new rows contain copied primitive endpoint values, so row edits are independent. With two-int rows this costs O(n) time and storage. If the row elements themselves were mutable objects, one more layer of copying might be needed; primitive endpoints stop that problem here.

### 14.2 Executable ownership trace

This demonstration intentionally edits only locally created arrays, so no external caller input is involved.

```java
static List<String> copyingTrace() {
    int[][] original = {{8, 10}, {1, 4}};
    int[][] shallow = original.clone();
    Arrays.sort(shallow, BY_START);
    String afterSortOnly = Arrays.deepToString(original);

    shallow[0][1] = 6;
    int[][] deep = copyIntervals(original);
    Arrays.sort(deep, BY_START);
    deep[0][1] = 99;

    return List.of(afterSortOnly,
            Arrays.deepToString(original),
            Arrays.deepToString(shallow),
            Arrays.deepToString(deep),
            Boolean.toString(shallow[0] == original[1]),
            Boolean.toString(deep[0] == original[1]));
}
```

| Observation | Exact value |
| --- | --- |
| original after sorting shallow only | [[8, 10], [1, 4]] |
| original after the shallow row edit and deep row edit | [[8, 10], [1, 6]] |
| shallow after its row edit | [[1, 6], [8, 10]] |
| independent deep rows after its own edit | [[1, 99], [8, 10]] |
| shallow[0]==original[1] | true |
| deep[0]==original[1] | false |

Arrays.toString on an int[][] does not print nested numeric contents usefully; use Arrays.deepToString for a value display. Arrays.deepEquals compares nested values; == checks object identity. A mutation test should check both values and row identity when the contract promises preservation.

### 14.3 List<int[]> and conversions

List<int[]> is useful when the number of merged components is unknown. Its add stores a row reference. toArray(int[][]::new) allocates a new outer array and copies those references; it does not clone rows.

That is safe for mergeClosed because each stored row was freshly created for that result. If you add input rows directly, the returned array still exposes caller-owned mutable rows even after toArray. Copying the outer result alone cannot supply the advertised independent-output contract.

```java
static int[][] intervalListRoundTrip(int[][] intervals) {
    int[][] independentRows = copyIntervals(intervals);
    List<int[]> rows = new ArrayList<>();
    Collections.addAll(rows, independentRows);
    return rows.toArray(int[][]::new);
}
```

This method preserves interval **order**, not sorting or merging. It demonstrates the ownership boundary before conversion. Its returned rows are fresh; the intermediate independentRows outer array and list backing buffer add O(n+1) temporary references. Total time and auxiliary storage are O(n+1), with O(n+1) output.

Arrays.asList(intervals) creates a fixed-size list backed by that outer object array. Replacing a list element can affect the original outer slot, and editing a row changes the shared row. Size-changing add/remove is not supported by that view. By contrast, Arrays.asList(aPrimitiveIntArray) is a one-element List<int[]> rather than a list of boxed endpoint values. Reuse the [existing List reference](./Java_List_ArrayList_LinkedList.md) for these API distinctions.

### 14.4 Choose ownership deliberately

| Design | What may change | Copy/storage obligation |
| --- | --- | --- |
| Sort caller int[][] directly | Caller outer row order | Explicit permission needed; include object-sort workspace |
| Sort an outer clone and only read rows | Private outer order; shared rows read-only | O(n) reference copy; no protection if later code edits a row |
| Clone all rows before sorting/editing | Private order and endpoint contents | O(n) independent row storage |
| Produce fresh output rows without sorting input | Output only | Can be enough for already ordered insertion |
| Return caller rows in a new outer container | Output edits can affect input | Document sharing; it does not meet this guide's independent-output contract |
| Immutable interval records | Values cannot change after construction | Sorting still reorders a container; allocate/store references as required |

An immutable value representation can simplify ownership, but does not eliminate sorting/output storage. Array-based examples remain common in interviews; explain their mutation rules rather than relying on syntax familiarity.

**Self-check Q27:** Which changes are prevented by intervals.clone(), and which remain visible?

**Self-check Q28:** Why does toArray not automatically make a List<int[]> result independent of input?

## 15. Time and complete auxiliary space

### 15.1 Define the workload

Let **n** be input interval count, **p** the count of nonempty half-open bookings (p<=n), **m** output union ranges, **q** query count, and **k** peak active heap occupancy. Endpoints and counts use fixed-width int/long operations with the range checks stated earlier.

Separate:

1. validation and copying;
2. sorting and comparator cost;
3. scan/event/heap operations;
4. result materialization and conversions;
5. maximum simultaneously live input, auxiliary objects, and output.

All comparators here inspect a constant number of primitive fields, so comparison cost is O(1). A comparator that parses strings or performs expensive lookup would need that extra factor.

### 15.2 Bounds for the actual code

| API / technique | Total time | Peak auxiliary space, excluding returned output | Required output |
| --- | --- | --- | --- |
| Uniform or mixed-bound overlap predicates | O(1) | O(1) | Boolean O(1) |
| copyIntervals | O(n+1) | O(1) non-output loop state | O(n+1) copied rows/outer array |
| sortedByStart / sortedByEnd | O(n log(n+1)+1) | O(n+1) worst-case object-sort workspace | O(n+1) independent sorted copy |
| mergeClosed / mergeHalfOpen | O(n log(n+1)+1) | O(n+1) private copy, sorting, list/conversion buffers | O(m+1) |
| insertClosed, exact-allocation version | O(n+1) | O(1) | O(m+1), m<=n+1 |
| maxCompatibleActivities / removals | O(n log(n+1)+1) | O(n+1) copied rows and sorting | O(1) count |
| canAttendAll | O(n log(n+1)+1) | O(n+1) copied nonempty rows and sorting | O(1) boolean |
| peakBookingsSweep / peakClosedRanges | O(n log(n+1)+1) | O(n+1) events, optional booking copy, sort workspace | O(1) peak |
| peakBookingsHeap | O(n+p log(p+1)+p log(k+1)+1) | O(n+k+1)=O(n+1) | O(1) peak |
| Already ordered merge/conflict scan, with an appropriate contract | O(n+1) | Depends on copy/output construction; scalar scan can be O(1) | Union O(m+1) or boolean O(1) |
| Optional offline query algorithm described in Section 13 | O(n log(n+1)+q log(q+1)+n+q+1) | O(n+q+1) | O(q+1) answers |
| Small-domain event buckets | O(n+U+1), U domain width | O(U+1) | Depends on requested count/timeline |

A standalone copy returned to its caller is output. The same copy kept inside mergeClosed or maxCompatibleActivities is auxiliary relative to that API. Space categories follow the **public contract**, not the name of a helper.

### 15.3 Java sorting is not universally constant-space

For int[][] with a Comparator, object-array sorting is the relevant Java API. Its documented Java 21 implementation uses temporary references and has O(n) worst-case workspace. Stable adaptive sorting may do less work on ordered runs, but that does not establish O(1) worst-case space or linear worst-case time for arbitrary inputs. [Arrays implementation note](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html)

Even sorting directly in a caller array does not erase the library's temporary workspace. “In place” is sometimes used loosely to mean “reorders the provided array”; it is not a proof of constant total auxiliary memory.

Primitive sorts use different implementation paths. Do not infer their storage from the object-array algorithm or promise that every Java primitive sort uses O(1) auxiliary space. These examples deliberately make conservative implementation-aware allowances.

### 15.4 Output can grow while still being output

insertClosed allocates its final result once and fills it; under our convention that is O(m) output with constant additional state. It does not use an intermediate result list. mergeClosed uses an ArrayList buffer and later allocates an outer array; while the returned row arrays are output, the temporary list backing references also exist and count as auxiliary.

A grown list or priority queue may briefly retain old and new backing arrays during resizing. Capacity is tied to peak occupancy, not necessarily current size after removals. A heap that becomes empty does not necessarily release its backing capacity immediately. Such details do not change the O(n) bounds here, but they invalidate a careless “only two variables, therefore O(1) space” explanation.

### 15.5 Count all operations, not loop syntax

The heap's inner cleanup while can poll at most once per previously offered booking across the entire run. Therefore total polls are O(p), not O(p) for each of p arrivals. Each costs logarithmic heap work, so total heap work is O(p log(k+1)+1).

In insertion, validation and several forward passes still sum to O(n), not O(n^2). In repeated all-pairs merging, repeated rescans can really multiply costs. Describe what is charged to each row/event, not merely how many loops appear.

Two workloads need separate parameters: a naive interval/query baseline is O(n*q+n+q+1), not O(n^2) unless q is constrained to scale like n. Likewise, O(n+q) pointer advances do not eliminate O(n log n) heap work or sorting.

**Self-check Q29:** Why can a scalar-returning scheduling method still require O(n) auxiliary storage?

**Self-check Q30:** Why is the heap cleanup loop not quadratic in the number of bookings?

## 16. Mistakes and reproducible counterexamples

The small faulty methods below are deliberate counterexamples. They are not alternative production APIs. Their stated inputs satisfy the local preconditions; null/shape validation is omitted when it is not the defect under discussion.

### 16.1 Treat an empty booking as an overlap

Preconditions: valid ordered endpoint rows, interpreted as half-open; empty rows allowed. Intended result is true iff the represented sets overlap.

```java
static boolean badHalfOpenOverlap(int[] a, int[] b) {
    return a[0] < b[1] && b[0] < a[1];
}
```

For A=[3,3), B=[1,5), this returns true; the correct result is false because A is empty. The rule is valid for nonempty half-open ranges, but the wider domain invalidates the shortcut. The Section 4 max/min predicate handles the empty intersection.

### 16.2 Shrink the merged frontier

This fragment's preconditions are a valid closed frontier ending at oldEnd and an overlapping next row. Intended result is the new frontier end.

```java
static int badExtendedEnd(int oldEnd, int[] next) {
    return next[1];
}
```

oldEnd=10 and next=[3,5] produce 5, losing already covered points through 10. The union cannot become smaller when another range is added. Use the maximum end; nested and equal-start tests expose this immediately.

### 16.3 Subtract comparator keys

Preconditions: two valid interval rows.

```java
static int badStartComparison(int[] a, int[] b) {
    return a[0] - b[0];
}
```

For a=[MIN_VALUE,MIN_VALUE], b=[1,1], this returns MAX_VALUE, a positive result, ranking a after b. Integer.compare(MIN_VALUE,1) is negative. A few small positive examples do not verify comparator correctness. Test both signs, extreme endpoints, transitivity, and ties.

### 16.4 Use closed event ties for half-open bookings

Intended result: maximum simultaneous nonempty half-open bookings, with the same validation/empty policy as peakBookingsSweep. The following wrapper deliberately chooses the wrong tie convention.

```java
static int badStartFirstPeak(int[][] bookings) {
    int[][] nonempty = nonemptyBookings(bookings);
    Event[] events = new Event[2 * nonempty.length];
    int at = 0;
    for (int[] row : nonempty) {
        events[at++] = new Event(row[0], 1);
        events[at++] = new Event(row[1], -1);
    }
    return peakFromEvents(events, true);
}
```

For [[1,3],[3,5]], it returns 2 instead of 1. The same start-first order is correct for **closed** occupancy, so the defect is a mismatch between the chosen contract and the tie policy.

### 16.5 Return current active size instead of historical peak

Intended contract: peak half-open concurrency, not activity remaining at the last start.

```java
static int badFinalActiveSize(int[][] bookings) {
    int[][] ordered = nonemptyBookings(bookings);
    Arrays.sort(ordered, BY_START);
    PriorityQueue<Integer> ends = new PriorityQueue<>();
    for (int[] current : ordered) {
        while (!ends.isEmpty() && ends.peek() <= current[0]) {
            ends.poll();
        }
        ends.offer(current[1]);
    }
    return ends.size();
}
```

On the six-booking Section 12 example, the method returns 1 although the true maximum is 3. The heap's current-state invariant is correct; the returned statistic is wrong. Record the maximum after each admission. This is different from the valid allocated-room invariant discussed earlier.

### 16.6 Hide expired bookings under a max-heap root

Intended contract: the same peak half-open concurrency. This code changes the ordering while retaining a minimum-based release test.

```java
static int badMaxHeapPeak(int[][] bookings) {
    int[][] ordered = nonemptyBookings(bookings);
    Arrays.sort(ordered, BY_START);
    PriorityQueue<Integer> ends =
            new PriorityQueue<>(Comparator.reverseOrder());
    int peak = 0;
    for (int[] current : ordered) {
        while (!ends.isEmpty() && ends.peek() <= current[0]) {
            ends.poll();
        }
        ends.offer(current[1]);
        peak = Math.max(peak, ends.size());
    }
    return peak;
}
```

Input [[1,2],[1,10],[3,4]] has true peak 2. At start 3, end 2 is expired but hidden under maximum end 10. The code counts three heap entries and returns 3. A min-heap is required by the proof that if the head has not expired, no stored end has expired.

### 16.7 Widen only after overflow

Preconditions: a valid closed integer range; intended result is its number of integer points.

```java
static long badInclusiveSize(int[] interval) {
    return (long) (interval[1] - interval[0] + 1);
}
```

For [MIN_VALUE,MAX_VALUE], the int expression wraps to 0 before conversion; correct size is 4294967296. Promote an operand before subtraction, as closedIntegerSize does.

### 16.8 Other high-impact failures

| Incorrect assumption | Failing example | Correct reasoning |
| --- | --- | --- |
| Touching always overlaps | Half-open [1,3),[3,5) | Derive membership and tie behavior |
| Touching never overlaps | Closed [1,3],[3,5] | Both contain point 3 |
| Zero endpoint difference means empty for every model | Closed [4,4] | Singleton closed points are valid |
| Sort by end, then use the ordinary start-frontier merge scan | Closed [[1,10],[2,3]] becomes [2,3],[1,10] | A frontier initialized at 2 cannot recover covered start 1 without a different algorithm |
| Keep the immediately preceding input end for coverage | [1,10],[2,3],[4,5] | The covered frontier remains 10 after the nested row |
| Put closed touching rows in insertion's before region with <= | Existing [1,3], insert [3,5] | Shared point requires a merged [1,5] |
| Assign inserted end to each consumed end | Existing [[1,3],[6,8]], insert [2,10] | Shrinking to 3 loses later coverage and overlap decisions |
| Take earliest start to maximize count | [0,12), four short consecutive activities | Earliest finish preserves the needed future opportunities |
| Use strict > for touching-compatible selection | [1,3),[3,5) | Equality is compatible under the half-open model |
| Initialize lastEnd=-1 without a selected flag | First activity [-5,-2) | Negative starts are legal |
| Treat a plausible Greedy rule as proved | Shortest [4,6) blocks [0,5),[5,10) | Supply an exchange/dominance argument or a counterexample |
| Leave expired ends in a claimed active heap | End 2 still present at start 3 | Size counts stale demand unless every expired end is removed |
| Poll once and still call the heap the active set | At step 5 of Section 12, ends 5 and 6 have both expired | Either remove all, or state a different allocated-room invariant |
| Read PriorityQueue iteration as sorted | Multiple live ends | Display a sorted copy for explanation, count its cost if part of an algorithm |
| Return shared input rows in a new outer array | Edit one returned merged endpoint | New outer storage does not imply independent rows |
| Assume outer clone protects row edits | copyingTrace | Row identities are still shared |
| Ignore initial sorting in cost | A linear scan after Arrays.sort | Add preparation, copy, sorting, and scan |
| Delete closed offline candidates at end<=query | [8,8], query 8 | Closed coverage permits equality; expiration is end<query |
| Use a size-heap's length as active coverage count | Valid size-1 head above an expired size-3 entry | Lazy invalid non-head entries can remain stored |

Each correction follows from an invariant or domain rule, not a remembered punctuation change.

### 16.9 A focused testing sequence

Start with empty input, a singleton, two overlapping rows, and two disjoint rows. Then test nested/equal-start/equal-end/touching pairs, identical ranges, closed points, empty half-open rows, negative times, and int endpoints. Compare shuffled input against the same set in sorted order where input order is irrelevant.

For merge, verify sorted disjoint output, exact covered union, idempotence, and fresh rows. For insertion, compare against merging existing plus inserted in a test oracle, and validate the stricter existing-order contract. For Greedy count, enumerate all subsets of small activities and compare the maximum compatible size. For concurrency, compare heap, event sweep, and direct occupancy at endpoints/interior samples.

After each preserving call, compare every input value, row identity, and outer order with its entry snapshot. Edit a returned row and check that input and other output rows are unchanged. Repeat the call to detect unexpected stored state. A strong test oracle should use a simpler independent method, not just copy the same algorithm.

**Self-check Q31:** How can a correct current-state heap still return the wrong statistic?

**Self-check Q32:** Which tests expose numeric and reference bugs that ordinary positive endpoints can hide?

## 17. Choose among interval and alternative methods

“An input contains ranges” is not enough to select one algorithm. Determine what must be preserved and which question the output answers.

### 17.1 Compare the core operations

| Pair of techniques | Prefer the first when | Prefer the second when |
| --- | --- | --- |
| Merge versus insert | Arbitrary order/overlap requires normalization | One new range enters already sorted/disjoint data |
| Merge versus selection | Every covered point must remain represented | Conflicting activities may be discarded to optimize count |
| Conflict detection versus maximum concurrency | Only whether one resource is sufficient matters | Need the exact number of simultaneous resources |
| Start sort versus end sort | Need a left-to-right coverage/admission frontier | Need a safe earliest-release selection decision |
| Event sweep versus active heap | Timeline changes are the main abstraction | Next release or active metadata should be available |
| Sorting/scan versus repeated pair checks | Inputs can be numerous and ordering simplifies future work | Input is tiny, one pair is queried, or extra preparation would dominate |
| Defensive copies versus direct mutation | Input must be preserved or outputs independent | Caller explicitly permits modifications and the full storage tradeoff is acceptable |

### 17.2 Other previously studied techniques

- **One pair:** an overlap predicate is O(1); sorting two rows adds no useful structure.
- **Already sorted, disjoint collection and one membership query:** binary search for the last start<=query, then test the matching end with the correct inclusion rule. This can be O(log(n+1)+1) without rescanning all rows. The ordering/disjointness contract must be trusted or validated separately.
- **Sum of numerical array values between positions:** a prefix sum answers an aggregate, not interval-union or overlap selection. Endpoints here may represent indexes, but the requested operation is different.
- **Repeated exact equality lookup of interval IDs:** a map/set can be useful; it does not provide endpoint ordering or geometric overlap by itself. An int[] key also uses identity equality unless converted to a content key.
- **Small bounded coordinate domain:** buckets/difference counts may beat sorting if O(U) initialization and memory are acceptable. Sparse full-range int coordinates make a bucket per timestamp inappropriate.
- **Already ordered start/end sequences:** a two-pointer event count avoids a heap; equality still needs the endpoint convention.
- **List all compatible subsets:** output can be exponential and Backtracking may fit. Returning only the maximum count of unweighted one-resource activities admits the proved Greedy shortcut instead.
- **Weighted scheduling, dynamic coverage, many changing queries:** investigate richer structures or later DP after defining the objective/state. Those topics are extensions, not hidden prerequisites here.

The Binary Search guide uses half-open **index regions** to prove candidate elimination. A booking's half-open **time range** uses the same boundary notation, but different membership facts. Transferring an invariant requires matching its meaning, not matching a loop's appearance.

### 17.3 Clarify resource models

Maximum concurrency gives identical resources needed when bookings have fixed times and no setup constraints. It does not directly solve assigning named rooms with capacities, minimizing travel, maximizing weighted profit, or choosing new start times. Additional constraints change feasibility and may invalidate the exchange argument or the resource-count proof.

A cleaning gap g>=0 can sometimes be modeled by a later release time end+g. Under full int endpoints, compute the release in long and use a consistent long-based comparator/heap domain. Do not add g in int and then widen. Any such modification needs its own bound and semantics.

**Self-check Q33:** Which condition makes binary search a useful alternative for interval membership?

**Self-check Q34:** Why does maximum concurrency not automatically solve weighted activity selection?

## 18. Recognition and derivation

### 18.1 Pattern cheat sheet

| Pattern | Recognition clues and preconditions | Invariant / derivation | Implementation and costs | Common failure / alternative |
| --- | --- | --- | --- | --- |
| A — Sort + merge frontier | All covered locations required; arbitrary ranges | Processed-prefix union summarized by finalized components and one extendable end | mergeClosed/mergeHalfOpen, Section 6; O(n log n), preserving O(n) auxiliary plus output | Shrinking nested coverage; use an ordered scan when already sorted |
| B — Three-region insertion | One new range; existing data sorted/disjoint under stated model | Before region final; overlap frontier expands; after region separated | insertClosed, Section 7; O(n), O(1) auxiliary plus exact output | Wrong touching boundary; use general merging if existing rows violate assumptions |
| C — Earliest-finish selection | Maximum count, equal rewards, one resource, explicit compatibility | Exchange earliest finish into an optimum; maintain last selected end | Section 8; O(n log n), O(n) preserving copy/sort | Earliest start/shortest anywhere lacks this proof; weighted goals need different reasoning |
| D — Active-end heap | All bookings retained; count resources or expose next release | Exactly processed bookings active at current start after cleanup | peakBookingsHeap, Section 12; sort plus O(n log(k+1)), O(n+k) preserving storage | Stale ends, wrong heap direction; events are simpler for a pure timeline |
| E — Sweep events | Need simultaneous demand or timeline changes | Correct signed changes, endpoint-sensitive tie phase, historical maximum | Section 11; O(n log n), O(n) auxiliary | Wrong tie order/empty events; bounded-domain buckets can replace sorting |
| F — Offline queries, optional | All queries available; reorder privately and restore indexes | Admit by start, lazily purge invalid size-heap heads, answer minimum valid key | Partial helpers in Section 13; O(n log n+q log q+n+q), O(n+q) auxiliary | End equality, lost query order, false “all heap entries active” claim; scan is fine for tiny workloads |

Conventional table bounds assume nonempty growing inputs. The detailed sections provide n=0 conventions, validation, output storage, and code-specific caveats. Pattern F intentionally supplies only helpers and a trace; a complete Hard solution is outside this guide.

### 18.2 A derivation card for an unfamiliar statement

1. What does one interval include? Is the axis real, discrete integer, array index, or a named time model?
2. Can a range be empty or a closed point? What is invalid input?
3. Does touching mean conflict, overlap, adjacency to coalesce, or immediate resource reuse?
4. Is the output a union, yes/no answer, maximum subset, peak demand, assignment, or query result?
5. Is input already ordered/disjoint? May it be reordered or mutated? Must output rows be independent?
6. Which boundary lets earlier work become final: next start, chosen finish, event time, or earliest active end?
7. State the processed-prefix/active-state invariant before writing comparisons.
8. Explain correctness by union preservation, exchange, occupancy counting, or a sufficient monotone query state.
9. Count copies/sorting/scans/heap work/output separately. Give a touching-endpoint counterexample to a plausible alternative.

### 18.3 Recognition scenarios

Choose a method before opening the checkpoints in Section 19. These short questions assess selection of state, not a quota of platform solves.

| ID | Scenario |
| --- | --- |
| R1 | A sensor reports unordered closed coverage ranges; return all covered real locations as disjoint components |
| R2 | One new closed maintenance range enters an already sorted, pairwise disjoint schedule |
| R3 | One machine can keep as many fixed positive-duration unweighted tasks as possible; touching is compatible |
| R4 | Determine whether all half-open classroom bookings fit in one room |
| R5 | Return the smallest number of identical rooms needed for all bookings |
| R6 | Record every timestamp where nonempty half-open demand changes and its new count |
| R7 | Test whether one coordinate lies in an already sorted/disjoint interval collection |
| R8 | Known-in-advance closed-range queries ask for the smallest covering integer interval; restore original query order |
| R9 | Return the highest total reward from incompatible weighted activities |
| R10 | Compare one empty half-open row against an enclosing nonempty row |

For R5, give both sweep and heap approaches and explain the tie rule in each. For R8, identify which entries may remain lazily invalid in the heap. For R9, explain why a count-optimal Greedy selection need not maximize reward; implementing its later solution is not required.

**Self-check Q35:** Which output distinction decides between union, selection, and resource counting?

**Self-check Q36:** Why should a changed input/mutation assumption be stated before adapting a familiar implementation?


## 19. Conceptual assessment and answer key

Answer the 36 questions embedded in Sections 2–18 before opening these checkpoints. Add the following four interview questions; they assess reasoning rather than code recall.

**Self-check Q37:** Give an exchange argument for earliest finish without claiming it is the only possible optimum.

**Self-check Q38:** Explain a case where O(n) output storage is excluded from auxiliary space and a similar-looking O(n) buffer is included.

**Self-check Q39:** How would you validate the merge, Greedy, and concurrency APIs using independent small-input oracles?

**Self-check Q40:** Which evidence beyond reading this document would support ordinary Intervals interview readiness?

### Concise answer key

<details>
<summary>Q1–Q8: representation, endpoints, and overlap</summary>

| Question | Checkpoint |
| --- | --- |
| Q1 | Union drops how many distinct bookings cover the same point; resource demand must retain that multiplicity. |
| Q2 | Stored endpoints are integers, but the default axis is real; a value such as 3.5 can belong to (3,4). |
| Q3 | It is empty and consumes no resource. Validate it, then ignore it in the booking APIs. |
| Q4 | The two inequalities assume nonempty operands; [3,3) versus [1,5) satisfies them while having empty intersection. |
| Q5 | Their union can be one continuous half-open range even though their shared boundary belongs only to the later range. |
| Q6 | Closed includes the end; half-open excludes it. That changes point membership, conflicts, and event/expiration rules. |
| Q7 | Equality leaves one candidate point; mixed endpoints overlap there only if both contain it and neither range is empty. |
| Q8 | Partial intersections need not contain either entire interval, such as [1,4] and [3,6]. |

</details>

<details>
<summary>Q9–Q16: sorting, merging, insertion, and selection</summary>

| Question | Checkpoint |
| --- | --- |
| Q9 | int[][] elements are int[] references, so object-array sorting can compare them; primitive int[] has no comparator overload. |
| Q10 | A later-starting interval can be nested and end earlier. The merge frontier must retain the maximum covered end. |
| Q11 | Assigning a smaller nested end removes coverage that was already present; union only expands. |
| Q12 | Once the next start is beyond its end, every unprocessed start is at least as far right, so none can reach back across that gap. |
| Q13 | Existing ranges are already sorted and disjoint under the specified endpoint model; their before/overlap/after regions are consecutive. |
| Q14 | Every consumed overlap can extend the merged end; test the current frontier while advancing, and preserve the maximum. |
| Q15 | Merging represents all covered time, whereas selection intentionally rejects activities to optimize a count. |
| Q16 | A skipped candidate was not selected, so its end is not the resource's release boundary in the retained schedule. |

</details>

<details>
<summary>Q17–Q24: Greedy proof and occupancy</summary>

| Question | Checkpoint |
| --- | --- |
| Q17 | end(G)<=end(O1), so followers starting at/after end(O1) still fit after G. |
| Q18 | Exchanging one activity preserves count but can change reward; an earlier finish need not have enough value. |
| Q19 | With no adjacent conflicts the entire start-ordered schedule is compatible. A coverage algorithm must instead summarize maximum reached end and cannot discard nested coverage. |
| Q20 | Empty half-open bookings contribute no conflict; two identical positive-duration bookings represent two simultaneous uses and conflict. |
| Q21 | The ending booking is absent at that timestamp and the starting booking is present; start-first would fabricate overlap. |
| Q22 | Closed occupancy can spike only at one shared endpoint even when the net change there is zero. |
| Q23 | Its stated size is current active multiplicity; any expired retained entry overcounts that state. |
| Q24 | One end per allocated room is a different state: a free room is reused once for the new booking, and stored idle-room entries need not represent active bookings. |

</details>

<details>
<summary>Q25–Q32: offline queries, ownership, and cost</summary>

| Question | Checkpoint |
| --- | --- |
| Q25 | Processing order changes privately; each answer still belongs at its original query position, including duplicates. |
| Q26 | A valid size-minimum head is no worse than any non-head candidate. Invalid buried entries are removed if/when exposed later; heap size is not a coverage count. |
| Q27 | An outer clone permits independent outer order, but shared row endpoint edits remain visible in both arrays. |
| Q28 | Conversion copies row references; independence depends on allocating/copying rows before storing them. |
| Q29 | Preserving copies and object-sort workspace are intermediate memory even when the final result is one number. |
| Q30 | Each offered booking can be polled at most once; charge total removals to total admissions, then include logarithmic heap work. |
| Q31 | Returning the final current size discards an earlier maximum; track peak over all admissions. |
| Q32 | Use MIN/MAX and negative endpoints for overflow/sentinels, aliased rows for shallow copies, mutate returned rows, and repeat calls after success/failure. |

</details>

<details>
<summary>Q33–Q40: choosing methods, proving, and validating</summary>

| Question | Checkpoint |
| --- | --- |
| Q33 | Already sorted/disjoint ranges let a monotone start search find the only possible covering region; endpoint inclusion still decides membership. |
| Q34 | Concurrency counts fixed simultaneous demand, while weighted selection chooses which activities to keep for a different objective. |
| Q35 | Preserve all coverage for union; discard conflicts to optimize a subset; preserve booking multiplicity to count resources. |
| Q36 | An implementation's proof and storage depend on those assumptions; silently changing them can invalidate both. |
| Q37 | Take an optimum's first eligible activity, replace it by earliest finish, preserve every follower and count, then recurse on the remaining compatible problem. Some optimum is Greedy-compatible. |
| Q38 | insertClosed's exact returned rows/outer array are output; a temporary list backing buffer or private sorted copy is intermediate auxiliary state. |
| Q39 | Compare merge with overlap-connected components or sampled membership, selection with all small subsets, and concurrency with direct occupancy at critical coordinates; verify ownership and invalid inputs too. |
| Q40 | Tested blank-editor drills, independent Essential solves/cold re-solves, unfamiliar variants across sessions, and the roadmap's correctness/cost/communication rubric. |

</details>

<details>
<summary>Recognition checkpoints R1–R10</summary>

R1: sort by start and merge a closed frontier. R2: three-region insertion. R3: earliest-finish selection with the unweighted compatibility proof. R4: sorted adjacent conflict checks after empty filtering. R5: sweep events or a min-heap of active ends; half-open ends precede starts, and heap expiration uses <=start. R6: grouped event sweep with demand after each coordinate. R7: binary search under the already ordered/disjoint membership contract. R8: optional offline sorted queries with original indexes and a size-ordered heap using lazy expired-head removal. R9: count-based Greedy is insufficient; reason about the weighted objective and later methods. R10: empty means no overlap; use an empty-aware predicate.

These are method-selection checkpoints, not full model implementations or evidence that a memorized category label establishes readiness.

</details>

## 20. Readiness and retained understanding

An interval method is a compact proof about boundaries: which points belong, which earlier ranges are final, which choices preserve an optimum, or which processed bookings remain active. Keep that meaning attached to each comparison.

The [roadmap's independent gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain the only topic dashboard:

- **T — Theory:** explain endpoint membership, empty/point cases, overlap derivation, sorting keys, frontiers, event ties, heap meaning, and the exchange argument without notes.
- **I — Implementation:** from a blank editor implement closed merging and one half-open concurrency method. Test nested, equal-start, disjoint, touching, single-point, and empty-booking cases. Explain copies and input/output ownership. Also derive the three-region and earliest-finish scans reliably.
- **P — Practice:** independently solve or cold re-solve the three Essential platform problems with Java, proof, cost analysis, and adversarial tests. A previous solution review requires a later independent attempt.
- **R — Readiness:** pass the [common rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) on unfamiliar scheduling/range variants across separate sessions. Derive endpoint and tie rules; choose union versus selection versus active-count state; explain sorting and mutation costs.

The two A-tier Premium problems are optional paid practice, not a subscription prerequisite. Their conflict/concurrency concepts still need a demonstration through the free educational drills. The C-tier offline Hard problem is optional advanced and does not block ordinary readiness.

### Evidence to demonstrate aloud

| Skill | Practical demonstration |
| --- | --- |
| Endpoint contract | Explain a touching pair whose answer changes between closed and half-open models |
| Empty versus point | Treat [3,3) as no demand and [3,3] as one closed point |
| Covered frontier | Trace nested/equal-start intervals without losing earlier coverage |
| Three regions | Explain why insertion avoids sorting and how output remains independent |
| Greedy choice | Exchange an earliest finish into an optimum and reject a tempting counterexample rule |
| Events | Order end/start ties from membership, including closed point peaks |
| Heap | State exactly what its entries mean; release all expired ends for a true active-set count |
| Java | Explain row aliases, safe comparator signs, widening before arithmetic, and conversion buffers |
| Complexity | Separate preparation, scan/heap work, sorting workspace, and returned output |
| Transfer | Re-derive a changed statement instead of quoting the original comparison operators |

Use the roadmap's **I/H/S/—** attempt codes and canonical Logs. Do not automatically mark progress because this document compiles. Follow its [flexible review intervals](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition), approximately 1, 3, 7, and 14 days: hide notes/code, restate the contract, derive the invariant, implement, and test before looking back.

A useful cold review changes one assumption: closed to half-open, union to maximum count, scalar peak to assignment, mutable to read-only input, or sorted to arbitrary order. State the new contract and identify which proof/cost changes. Familiar re-solves support P; unfamiliar transfer and repeatable communication support R. Keep dated evidence once in the roadmap's [Intervals section](./DSA_Interview_Roadmap.md#11-intervals), without adding a duplicate review ledger here.

## 21. References, curated practice, and validation

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Intervals](./DSA_Interview_Roadmap.md#11-intervals), [Greedy](./DSA_Interview_Roadmap.md#12-greedy), [Heap / Priority Queue](./DSA_Interview_Roadmap.md#9-heap--priority-queue), [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Java Arrays](./Java_Array.md): [references and copies](./Java_Array.md#4-primitive-arrays-object-arrays-and-references), [copying arrays](./Java_Array.md#copying-arrays), and [multidimensional arrays](./Java_Array.md#8-multidimensional-and-jagged-arrays).
- [Java List, ArrayList, and LinkedList](./Java_List_ArrayList_LinkedList.md): mutable containers, fixed-size views, nested references, and conversions.
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): [space](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), [sorting](./Complexity/Complexity_Analysis_and_Foundations.md#156-arrayssort), and [PriorityQueue costs](./Complexity/Complexity_Analysis_and_Foundations.md#148-priorityqueue).
- [Heap / Priority Queue Essential Concepts](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md): [Java operations](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#8-java-21-priorityqueue), [comparators](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#9-comparators-and-numeric-safety), and [complexity](./Heap_Priority_Queue/Heap_Priority_Queue_Essential_Concepts.md#16-complete-complexity-analysis).
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md): [search intervals and invariants](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants).
- [Arrays & Hashing Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md): [storage convention](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#24-input-output-auxiliary-space-and-allocations), [prefix sums](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#8-prefix-sums-and-range-queries), and [sorting/bucket tradeoffs](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#11-comparing-approaches-and-bounded-buckets).
- [Backtracking Essential Concepts](./Backtracking_Essential_Concepts.md): compare exhaustive choice enumeration with a proved Greedy reduction; a complete list of solutions has different output costs.

### Primary Java sources

Version-21 API documentation was consulted through Context7: [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), [Comparator](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html), [PriorityQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html), and [List.toArray](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html). The API-supported facts are kept separate from algorithm proofs and the guide's explicit output/mutation contracts.

### Curated roadmap practice, in exact order

| Priority | Official problem | Difficulty | Learning objective |
| --- | --- | --- | --- |
| E | [56. Merge Intervals](https://leetcode.com/problems/merge-intervals/) | Medium | Derive sorted coverage and preserve nested endpoints |
| E | [57. Insert Interval](https://leetcode.com/problems/insert-interval/) | Medium | Use existing sorted/disjoint structure and three regions |
| E | [435. Non-overlapping Intervals](https://leetcode.com/problems/non-overlapping-intervals/) | Medium | Relate maximum compatible count to removals; prove earliest finish |
| A | [Meeting Rooms](https://leetcode.com/problems/meeting-rooms/) | Easy; Premium | Detect adjacent sorted conflicts under the problem's compatibility rule |
| A | [Meeting Rooms II](https://leetcode.com/problems/meeting-rooms-ii/) | Medium; Premium | Count concurrent resource demand and justify heap/event ties |
| C | [1851. Minimum Interval to Include Each Query](https://leetcode.com/problems/minimum-interval-to-include-each-query/) | Hard; optional advanced | Offline ordering, indexed answers, and size-heap candidate validity |

E/A/C are repository learning priorities, not platform difficulties or official NeetCode tier labels. Keep attempts and review dates in the existing roadmap Logs. No new progress is recorded automatically.

Public titles/difficulties and relevant endpoint rules were checked **2026-10-31**. The Meeting Rooms pages expose a subscription gate; their Easy/Medium labels above follow the repository roadmap rather than an inspected paid statement. The [interactive NeetCode roadmap](https://neetcode.io/roadmap) did not expose readable graph text during checking; this guide follows the local roadmap instead of inferring new dependency edges.

### Author validation

Completed **2026-10-31**. All **23 Java blocks**, containing **35 methods** plus the displayed value records/constructor, were assembled in one scratch class and compiled with Java 21 using `--release 21 -Xlint:all -Werror`.

Runtime validation passed **988,024 assertions**. A doubled-coordinate membership oracle checked **3,969 mixed-endpoint pairs**, including open, closed, half-open, empty, singleton, and extreme-boundary cases. Independent overlap-connected-component oracles and direct occupancy counts checked **12,112 interval collections**, including exhaustive ordered arrays of lengths 0..4 over a small endpoint domain, seeded full-int-range fixtures, and aliased input rows. Tests compared closed merging, both half-open coalescing policies, conflict detection, event counting, and heap counting; they verified input order/content/row identity, output independence, and merge idempotence.

An exhaustive subset oracle checked **9,996 activity-selection cases**, and independent union components checked **342 insertions**, including an inserted row aliased to an existing row. Further checks covered comparator signs, widened duration/inclusive-size arithmetic, query-index preservation, lazy candidate cleanup, list conversion, **83 invalid-input cases**, and **19 witness/control assertions** for the seven executable faulty methods. A 100000-booking boundary fixture checked touching semantics, count selection, and merging at scale. No hardware timing or universal capacity limit was inferred.

Instrumentation of the published algorithms captured **50 state observations in eight trace families**. A separate **120-check audit** matched **57 rows across nine sorting, merge, insertion, selection, conflict, sweep, heap, offline-query, and copying tables** to actual states. **60 additional published boundary examples** were extracted from six Markdown tables and executed with **67 Java assertions**. Optional offline validation exercised only the published preparation/cleanup helpers and the small manually staged example; the guide contains no complete Hard-problem implementation.

The content audit checked all **21 sections**, **40 questions with answer entries**, **10 recognition scenarios**, and the **six curated problems in exact E/E/E/A/A/C order**. All **63 local links**, including heading anchors, resolve. SHA-256 checks confirmed all **40 files present at task start** remained unchanged. Only **Intervals_Essential_Concepts.md** was created by this task inside DSA_Guide; compilation, oracle, and instrumentation artifacts stayed in a temporary directory. All requested supporting references were available. No unresolved validation issue remains.

