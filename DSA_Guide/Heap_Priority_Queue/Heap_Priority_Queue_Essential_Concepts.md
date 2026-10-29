# Heap / Priority Queue — Essential Concepts (Java 21)

A heap keeps the next priority decision cheap. Learn to derive its shape and order invariants, then choose what the root should expose: the next item to process, the weakest retained candidate, or the boundary between two halves.

## Contents

- [1. Scope and prerequisites](#1-scope-and-prerequisites)
- [2. Heaps and priority queues](#2-heaps-and-priority-queues)
- [3. Complete trees and array indexes](#3-complete-trees-and-array-indexes)
- [4. Min-heap and max-heap invariants](#4-min-heap-and-max-heap-invariants)
- [5. Sift-up and insertion](#5-sift-up-and-insertion)
- [6. Sift-down and root removal](#6-sift-down-and-root-removal)
- [7. Bottom-up heapify](#7-bottom-up-heapify)
- [8. Java 21 PriorityQueue](#8-java-21-priorityqueue)
- [9. Comparators and numeric safety](#9-comparators-and-numeric-safety)
- [10. Bounded selection and top-k](#10-bounded-selection-and-top-k)
- [11. Streaming rank](#11-streaming-rank)
- [12. Repeated-priority simulation](#12-repeated-priority-simulation)
- [13. Merging ordered streams](#13-merging-ordered-streams)
- [14. Scheduling and availability](#14-scheduling-and-availability)
- [15. Two-heap median maintenance: optional](#15-two-heap-median-maintenance-optional)
- [16. Complete complexity analysis](#16-complete-complexity-analysis)
- [17. Choosing an alternative](#17-choosing-an-alternative)
- [18. Java pitfalls and counterexamples](#18-java-pitfalls-and-counterexamples)
- [19. Pattern recognition and derivation](#19-pattern-recognition-and-derivation)
- [20. Self-assessment answer key](#20-self-assessment-answer-key)
- [21. Key takeaways and readiness](#21-key-takeaways-and-readiness)
- [22. References and next steps](#22-references-and-next-steps)

## 1. Scope and prerequisites

Heap / Priority Queue belongs to **Phase 3 — Hierarchies and Search Spaces** in the [DSA Interview Roadmap](./DSA_Interview_Roadmap.md#9-heap--priority-queue). The roadmap determines scope, priorities, prerequisite order, and readiness. Trees prepares this branch; prior Tries study is useful background but is not a dependency.

The core is complete-tree shape, array layout, heap order, sift-up/down, linear heap construction, Java priority queues, bounded selection, online rank, repeated-priority simulation, and ordered-stream frontiers. Scheduling receives focused treatment consistent with its Additional practice priority. Two-heap median maintenance is **optional advanced material**. Indexed heaps, arbitrary decrease-key, meldable heaps, and concurrent priority queues are later extensions.

Preparing references does not establish mastery. Before proceeding, demonstrate the relevant prerequisite without notes:

| Demonstrate | Reuse this reference | What heaps add |
| --- | --- | --- |
| Index bounds, array copies, and mutation | [Java Arrays](./Java_Array.md#3-indexes-length-and-traversal), [copying](./Java_Array.md#copying-arrays) | Encode a complete tree with positions instead of links |
| Interfaces versus implementations, ordering, and object references | [Java Collections/List](./Java_List_ArrayList_LinkedList.md#1-collections-framework-and-list) | A Queue whose next element follows priority |
| Counts versus membership and processed-prefix invariants | [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model) | Maintain only the candidates needed for future decisions |
| Tree shape versus BST ordering; node-height versus edge-depth | [Trees](./Trees/Trees_Essential_Concepts.md#3-binary-tree-types-and-properties) | Completeness gives logarithmic repair routes |
| Loop initialization, preservation, progress, and final meaning | [Binary Search invariants](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) | Prove heap repair and retained-set correctness |
| Merge sorted inputs from their exposed heads | [Linked List merging](./Linked_List/Linked_List_Essential_Concepts.md#11-merging-sorted-chains) | Extend two heads to k frontier candidates |
| Frequencies, key lookup, and ordered navigation | [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) | Priority extraction does not replace equality lookup |
| Worst-case, amortized, comparator, output, and live-storage costs | [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#14-java-data-structure-complexities) | Include repair, growth, initialization, and extraction |

### Code and state conventions

All Java blocks are members of a scratch `HeapExamples` class with `import java.util.*;` at file scope. Combine them in order; some classes call helpers defined elsewhere in the document. No external dependencies are required. All use single-threaded calls and feasible allocations.

Unless stated otherwise, arrays are nonnull, lengths are 0..100000, and values may be any int including duplicates, negatives, MIN_VALUE, and MAX_VALUE. Null inputs explicitly checked by code throw NullPointerException. Invalid numeric parameters explicitly checked by code throw IllegalArgumentException. Additional domain restrictions are stated beside each algorithm. General preconditions such as sorted source rows are assumed, rather than silently revalidated. Inputs remain unchanged except the explicitly in-place sift/heapify helpers and the owned custom-heap state.

The educational custom heap has **fixed capacity**, making its per-operation bounds easy to see. Java PriorityQueue instead grows automatically. The custom empty peek/poll methods throw NoSuchElementException; Java's peek/poll return null. These are deliberate different API contracts.

**State displays:** custom-heap tables show the exact active array in index order. Tables about Java PriorityQueue patterns show a **sorted display of the retained multiset or frontier** for readability, with the next removal first unless labeled otherwise. Those displays are not iterator output or promises about Java's internal array. Duplicate entries are retained.

For a nonempty complete heap, node-height is floor(log2 n)+1 and maximum edge depth is floor(log2 n), matching the [Trees height convention](./Trees/Trees_Essential_Concepts.md#2-tree-fundamentals-and-terminology). Empty node-height is zero. Work uses constant-time primitive comparisons unless a comparator cost C is explicitly included.

Auxiliary space excludes existing inputs and required output but includes working heaps, temporary arrays, frames, and library growth. A heap retained between streaming calls is persistent state and must still be reported. Distinguish peak live storage from all allocations made over time.

| Learning objective | Sections |
| --- | --- |
| Shape, order, root meaning, array relationships | 2–4 |
| Sift-up/down and custom primitive mechanics | 5–6 |
| Bottom-up heap construction and its proof | 7 |
| PriorityQueue, ordering, ties, numeric safety | 8–9 |
| Bounded selection and streaming rank | 10–11 |
| Changing priorities and merging frontiers | 12–13 |
| Available versus cooling tasks | 14 |
| Optional online median | 15 |
| Costs, alternatives, pitfalls, transfer, readiness | 16–22 |

Keep topic-level T/I/P/R gates, curated histories, and readiness evidence in the [existing roadmap](./DSA_Interview_Roadmap.md#four-independent-progress-gates). This guide adds conceptual checks, not a second dashboard or exercise workbook.

## 2. Heaps and priority queues

### 2.1 Ask which item should be handled next

A FIFO queue serves the oldest waiting item. A priority queue serves the item that ranks first under a chosen ordering: earliest deadline, least cost, greatest score, or another explicit key. In a support desk, a newly arrived urgent request can precede an older routine request.

A **priority queue is an abstract set of operations**: insert, inspect the highest-priority item, and remove it. A **heap is a data structure** that can implement those operations efficiently. Sorted arrays, unsorted arrays, and ordered trees can also implement a priority queue with different tradeoffs. Here “heap” means a binary heap, not the JVM's general object-allocation heap.

| Concept | Describes | Does not by itself specify |
| --- | --- | --- |
| Priority queue | Which operations and priority behavior a client needs | A binary-tree implementation or FIFO order |
| Binary heap | Complete shape plus parent/child priority order | A fully sorted traversal or arbitrary-key search |
| Java PriorityQueue | A library priority queue backed by a priority heap | Stable ties, sorted iteration, or indexed decrease-key |

### 2.2 A small min-heap

```text
             2
           /   \
          5     3
         / \
        9   7

active array: [2,5,3,9,7]
```

Every parent is no greater than its children: 2<=5, 2<=3, 5<=9, and 5<=7. Following any root-to-node route gives a nondecreasing sequence, so root 2 is a minimum. The left child 5 can exceed the right child 3; siblings do not need sorted order.

The array itself is not sorted: 5 precedes 3, and 9 precedes 7. A heap stores just enough order to expose an extreme cheaply. It does not pay to organize every pair.

A max-heap reverses each comparison. Its root is a maximum. Under a general comparator, “highest priority” means the element that the comparator ranks first; numeric size alone may not express that policy.

| Need | Root to expose | Natural numeric heap |
| --- | --- | --- |
| Process the smallest available cost | Smallest value | Min-heap |
| Process the largest workload | Largest value | Max-heap |
| Retain the k largest values | Smallest among retained winners | Min-heap |
| Retain the k smallest values | Largest among retained winners | Max-heap |

The last two rows concern what must be **discarded next**. They are why choosing a heap solely from the word “largest” often produces the wrong direction.

**Self-check Q1:** How do a priority queue and a binary heap differ?

**Self-check Q2:** Why does the displayed min-heap not guarantee sorted siblings or a sorted array?

## 3. Complete trees and array indexes

### 3.1 Shape makes array positions meaningful

A complete binary tree fills every level before the last, then fills the last left to right without gaps. Its occupied breadth-first positions are exactly 0 through n−1. That contiguous shape allows an array to encode the relationships without allocating TreeNode objects.

```text
Tree with index:value labels:
             0:2
           /     \
        1:5       2:3
       /   \
    3:9    4:7

index:       0  1  2  3  4
value:       2  5  3  9  7
parent:      -  0  0  1  1
left child:  1  3  -  -  -
right child: 2  4  -  -  -
```

For zero-based position i:

| Relationship | Formula | Required condition |
| --- | --- | --- |
| Parent | (i−1)/2 using integer division | i>0 |
| Left child | 2i+1 | The resulting index is less than active size |
| Right child | 2i+2 | The resulting index is less than active size |
| Internal positions | 0 through n/2−1 | n>=2 |
| Leaf positions | n/2 through n−1 | Nonempty occupied positions |

The root has no parent. In Java, (0−1)/2 evaluates to 0 because integer division truncates toward zero; that arithmetic result does not create a root parent. Always guard i>0 before using the parent relationship.

The first non-leaf encountered when walking backward is the last internal position, n/2−1. For n>=2 it is also the parent of the last element, floor((n−2)/2). For n=0 or n=1, n/2−1 is −1, so bottom-up construction performs no sift-down calls.

### 3.2 Active size differs from capacity

A backing array can have more allocated positions than occupied heap values. The heap is `a[0..size)`, not the whole array. Stale primitive values beyond size do not belong to the data structure; zero is an ordinary valid value, not an empty marker.

Appending at index size adds the next legal complete-tree position. Removing the last occupied position preserves completeness. Root extraction combines these: replace root with the final occupied value, shorten the active prefix, then repair priority order.

A node-based heap is possible, but locating the next insertion/removal position requires additional structure or index reasoning. Array layout already encodes it. Objects stored in a Java PriorityQueue are referenced by its backing array; the heap's shape does not require a separate linked node per entry.

### 3.3 Height and safe arithmetic

For nonempty n, completeness gives

```text
2^(h-1) <= n <= 2^h - 1       h is node-height
h = floor(log2 n) + 1
maximum edge depth = floor(log2 n)
```

Moving up or down one route therefore takes at most logarithmically many edges. The guarantee comes from completeness, unlike an arbitrary BST that can form a chain.

Blindly computing 2*i+2 for every int index can overflow. The sift-down implementation first tests `i<size/2`, proving a left child exists. Under 0<=size<=Integer.MAX_VALUE, this guard bounds 2*i+1 by size−1; computing right=left+1 remains representable even when right==size and is absent. Test right<size before reading it. Sift-up uses (i−1)/2 only for i>0.

If you must calculate a hypothetical child without that proof, compute with long, compare against size, and only then narrow to int. No formula bypasses actual Java allocation limits.

**Self-check Q3:** Why does completeness eliminate gaps in the occupied array?

**Self-check Q4:** What is the last internal index for sizes 0, 1, 2, and 5?

**Self-check Q5:** Why test for an internal node before computing child indexes?

## 4. Min-heap and max-heap invariants

The stable representation invariant combines **shape**, **order**, and **contents**:

1. Occupied positions are exactly a[0..size), forming a complete tree.
2. Every parent/child edge follows the priority comparison.
3. The occupied multiset is exactly the accepted values minus extracted values, including duplicates.

For a numeric min-heap, a[parent]<=a[child]. For a numeric max-heap, a[parent]>=a[child]. Transitivity along each route establishes that the root is an extreme. Without a valid transitive comparator, locally plausible edges do not supply a reliable global priority meaning.

| Ordering | Whole-subtree consequence | Sibling requirement |
| --- | --- | --- |
| Min-heap | Every ancestor is no greater than its descendants | None between siblings |
| Max-heap | Every ancestor is no less than its descendants | None between siblings |
| Strict BST | Entire left subtree smaller, entire right subtree larger | Derived from key partitions, not heap comparisons |

In the example [2,5,3,9,7], the left child 5 violates a BST's left-smaller rule but obeys min-heap order. A balanced BST with root 5 and left child 2 can be a valid search tree but is not a min-heap. Sorted ascending arrays happen to satisfy min-heap order; heap order alone does not imply sorting.

Ties are legal: [2,2,3] is a min-heap. The root is **a** minimum, and duplicate values occupy separate entries. Equal priority does not imply FIFO order or deduplication. If records need a deterministic tie rule, make that rule part of the comparator.

Appending one new leaf preserves all earlier edges; only its parent edge can initially be wrong. Root replacement after extraction preserves the remaining lower subtrees; only the replacement's downward relationship can initially be wrong. Sift-up/down exploit these localized defects rather than rebuilding all order.

The stable invariant applies at public-operation boundaries. During a repair, the code can temporarily violate an edge; its loop invariant must identify the one active route and the ordered regions already established.

**Self-check Q6:** Why does heap order imply an extreme root but not fast arbitrary membership?

**Self-check Q7:** Which edges can first become invalid after append and after root replacement?

## 5. Sift-up and insertion

### 5.1 Repair the new leaf's ancestor route

Append the value at the next occupied position to preserve completeness. If its parent already precedes it, stop. Otherwise swap the two values and consider the new parent relationship. No unrelated subtree requires examination.

The following helpers mutate the supplied active array. They assume valid indexes and that the old active prefix was a heap; they perform no allocation or argument validation.

```java
static void swap(int[] a, int i, int j) {
    int saved = a[i];
    a[i] = a[j];
    a[j] = saved;
}

static void siftUpMin(int[] a, int i) {
    while (i > 0) {
        int parent = (i - 1) / 2;
        if (a[parent] <= a[i]) {
            break;
        }
        swap(a, parent, i);
        i = parent;
    }
}
```

### 5.2 Min-heap insertion trace

Start with [2,5,3,9,7] and insert 1. Array order below is exact.

| Step | Current index | Comparison / action | Active array |
| --- | --- | --- | --- |
| Before insertion | — | Existing heap | [2,5,3,9,7] |
| Append | 5 | Put 1 at next leaf position | [2,5,3,9,7,1] |
| First repair | 5 | Parent index 2 holds 3>1; swap | [2,5,1,9,7,3] |
| Second repair | 2 | Parent index 0 holds 2>1; swap | [1,5,2,9,7,3] |
| Stop | 0 | Root has no parent | [1,5,2,9,7,3] |

```text
After append:         After first swap:    After second swap:
       2                     2                    1
      / \                   / \                  / \
     5   3                 5   1                5   2
    / \ /                 / \ /                / \ /
   9  7 1                9  7 3               9  7 3
```

### 5.3 Correctness and cost

Before each iteration, the only possible ordering violation is between the current moving value and its parent; the child subtrees and all unrelated edges are ordered. If the parent is too large, moving it down preserves its relationship with the children below the moving position: those children were already no smaller than the displaced parent under the prior heap order. The smaller inserted value moves upward, so only its new parent edge can remain wrong.

At root, or when the parent comparison passes, every edge is ordered. Swaps preserve the multiset, and append established shape. Each swap strictly reduces index depth, so the loop terminates.

Worst-case repair time is O(log(n+1)), best case O(1), and auxiliary space O(1). A fixed-capacity append adds no growth cost. A resizable backing array may add a linear copy on one insertion; that separate issue appears in Section 8.

Equal values do not need to swap for heap correctness. Empty insertion places the first element at root and performs no parent comparison. Negative values and int extremes are safe because comparisons use <= rather than subtraction.

### 5.4 The same derivation for a max-heap

Change the comparison that defines an already ordered edge:

```java
static void siftUpMax(int[] a, int i) {
    while (i > 0) {
        int parent = (i - 1) / 2;
        if (a[parent] >= a[i]) {
            break;
        }
        swap(a, parent, i);
        i = parent;
    }
}
```

For [30,18,24,9,12], append 27 at index 5:

| Step | Decision | Active array |
| --- | --- | --- |
| Append | 27 becomes left child of 24 | [30,18,24,9,12,27] |
| Swap upward | 27>24 at parent 2 | [30,18,27,9,12,24] |
| Stop | Parent 30>=27 | [30,18,27,9,12,24] |

```text
After append:             After upward swap:
        30                         30
       /  \                       /  \
      18  24                     18  27
     / \  /                     / \  /
    9  12 27                   9  12 24
```

The largest numeric value must reach the root under max-heap order. This is a reversal of order, not a different shape algorithm.

**Self-check Q8:** Why is it sufficient to inspect ancestors of the appended value?

**Self-check Q9:** What changes for equal priorities and for max-heap insertion?

## 6. Sift-down and root removal

### 6.1 Replace the root without leaving a hole

Save the root answer. Remove the final occupied element, move that value to root if anything remains, then sift it down. Deleting root without replacement would leave an array gap and destroy complete shape.

For a min-heap, choose the **smaller existing child**. If the parent is already <= that child, it is <= both children. Otherwise swap with the smaller child. Choosing the larger child can immediately leave the smaller child above a too-large parent.

```java
static void siftDownMin(int[] a, int size, int i) {
    while (i < size / 2) {
        int left = 2 * i + 1;
        int right = left + 1;
        int smaller = left;
        if (right < size && a[right] < a[left]) {
            smaller = right;
        }
        if (a[i] <= a[smaller]) {
            break;
        }
        swap(a, i, smaller);
        i = smaller;
    }
}
```

Preconditions: 1<=size<=a.length, 0<=i<size, and both child subtrees of i are min-heaps. The entire subtree rooted at i becomes a min-heap. Values outside that subtree and beyond active size remain untouched. A leaf call does no work. When children tie, this implementation chooses the left one; another consistent tied choice can also be correct.

### 6.2 Root extraction with two downward repairs

Start from a min-heap whose active array is [2,5,3,9,7,8,6,12,10]. Save 2 and move last value 10 to root. Size decreases from nine to eight.

| Step | Active size | Decision | Active array |
| --- | --- | --- | --- |
| Initial heap | 9 | Root answer is 2 | [2,5,3,9,7,8,6,12,10] |
| Move last to root | 8 | Remove final occupied position | [10,5,3,9,7,8,6,12] |
| Compare children | 8 | Children 5 and 3; choose index 2 | [10,5,3,9,7,8,6,12] |
| First swap | 8 | 10>3 | [3,5,10,9,7,8,6,12] |
| Compare next children | 8 | Children 8 and 6; choose index 6 | [3,5,10,9,7,8,6,12] |
| Second swap | 8 | 10>6 | [3,5,6,9,7,8,10,12] |
| Stop | 8 | Index 6 is a leaf | [3,5,6,9,7,8,10,12] |

```text
Before removal:              After restoration:
          2                           3
        /   \                       /   \
       5     3                     5     6
      / \   / \                   / \   / \
     9   7 8   6                 9   7 8  10
    / \                         /
   12 10                       12
```

A one-child case occurs when polling [1,2,3,4,5]:

| Step | Decision | Active array |
| --- | --- | --- |
| Replace root | Save 1, move last 5, size=4 | [5,2,3,4] |
| Choose smaller child | Swap with index 1 holding 2 | [2,5,3,4] |
| Only left child exists | At index 1, child 3 holds 4; no right child 4 | [2,4,3,5] |
| Stop | Leaf reached | [2,4,3,5] |

```text
After root replacement:    After restoration:
        5                         2
       / \                       / \
      2   3                     4   3
     /                         /
    4                         5
```

### 6.3 Why selecting the smaller child is sufficient

At each step, both child subtrees are ordered, and only the moving value's relationship to their roots may be wrong. The smaller child is <= its sibling and <= every value below its own ordered subtree. Moving it up establishes an ordered parent position. The displaced value can now violate only edges at its new lower position.

If the parent is <= the smaller child, every edge below the current position is valid and no swap is needed. Each swap increases depth and remains inside a complete finite tree, so it terminates within O(log(n+1)) levels. Constant temporary values give O(1) auxiliary space.

For a max-heap, choose the **larger** child and stop when parent>=that child. Reversing only the parent stopping comparison while still choosing the smaller child is incorrect.

### 6.4 A small fixed-capacity educational heap

This class ties the helpers to a usable owned active array. It is intentionally smaller than a general collection framework.

```java
static final class IntMinHeap {
    private final int[] heap;
    private int size;

    IntMinHeap(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("negative capacity");
        }
        heap = new int[capacity];
    }

    int size() {
        return size;
    }

    boolean isEmpty() {
        return size == 0;
    }

    void offer(int value) {
        if (size == heap.length) {
            throw new IllegalStateException("fixed capacity reached");
        }
        heap[size] = value;
        siftUpMin(heap, size);
        size++;
    }

    int peek() {
        if (size == 0) {
            throw new NoSuchElementException("empty heap");
        }
        return heap[0];
    }

    int poll() {
        if (size == 0) {
            throw new NoSuchElementException("empty heap");
        }
        int answer = heap[0];
        int last = heap[--size];
        if (size > 0) {
            heap[0] = last;
            siftDownMin(heap, size, 0);
        }
        return answer;
    }

    int[] snapshot() {
        return Arrays.copyOf(heap, size);
    }
}
```

Construction allocates/initializes capacity slots: O(capacity+1) time and storage. The backing array is the heap's persistent representation. offer/poll use O(log(n+1)) worst-case time and O(1) additional working space; peek/size/isEmpty use O(1). snapshot is O(n+1) time with a new O(n) output array, and does not expose the mutable owned array.

On a singleton poll, --size becomes zero and no repair runs. Capacity zero is legal but accepts no insertion. Full insertion throws before changing the heap. Stale int values beyond size retain no object references and are ignored; a generic reference-based implementation should clear removed slots to avoid unnecessary object retention.

The custom class's exceptions are chosen for its primitive API. Do not project that behavior onto Java PriorityQueue.poll(), whose empty result is null.

**Self-check Q10:** Why can choosing the larger child break min-heap repair?

**Self-check Q11:** Which cases require special attention for empty, singleton, and one-child removal?

## 7. Bottom-up heapify

### 7.1 Repair parents after their child subtrees

Repeated insertion is one way to build a heap, but it can move many incoming values all the way toward root. A decreasing input sequence realizes Θ(n log(n+1)) min-heap insertion work.

Bottom-up heap construction instead starts with the array already holding all n values. Leaves are one-node heaps. Traverse internal positions backward, applying sift-down only after both child subtrees have been repaired.

```java
static void heapifyMin(int[] a) {
    Objects.requireNonNull(a, "a");
    for (int i = a.length / 2 - 1; i >= 0; i--) {
        siftDownMin(a, a.length, i);
    }
}

static IntMinHeap heapFrom(int[] values) {
    Objects.requireNonNull(values, "values");
    IntMinHeap result = new IntMinHeap(values.length);
    System.arraycopy(values, 0, result.heap, 0, values.length);
    result.size = values.length;
    for (int i = result.size / 2 - 1; i >= 0; i--) {
        siftDownMin(result.heap, result.size, i);
    }
    return result;
}
```

heapifyMin mutates its input in place. heapFrom copies the input into a fresh owned heap and does not modify the caller's array. Its fixed capacity equals the input length. Both handle empty and singleton arrays without any internal-node loop iterations.

“Heapify” can mean the local sift-down repair or the entire bottom-up build in different sources. This guide names the local operation siftDownMin and the full build heapifyMin.

### 7.2 Exact build trace

Input [9,4,7,1,6,3,8], n=7, last internal index 2:

| Step | Position being repaired | Decision | Exact array |
| --- | --- | --- | --- |
| Start | — | Leaves 3..6 are already heaps | [9,4,7,1,6,3,8] |
| Repair i=2 | 2 | 7 swaps with child 3 at index 5 | [9,4,3,1,6,7,8] |
| Repair i=1 | 1 | 4 swaps with child 1 at index 3 | [9,1,3,4,6,7,8] |
| Begin i=0 | 0 | 9 swaps with child 1 at index 1 | [1,9,3,4,6,7,8] |
| Continue same repair | 1 | 9 swaps with child 4 at index 3 | [1,4,3,9,6,7,8] |
| Complete | — | Entire tree is a min-heap | [1,4,3,9,6,7,8] |

```text
Before:                    After:
          9                         1
        /   \                     /   \
       4     7                   4     3
      / \   / \                 / \   / \
     1   6 3   8               9   6 7   8
```

The final array is a heap, not a sorted array. Its different possible valid layouts do not affect the extracted nondecreasing sequence.

### 7.3 Why the build is linear

Multiplying n nodes by the maximum root height gives a correct but loose O(n log n) upper bound. It charges every leaf for work that only a root-like node can do.

In a complete tree, the number of nodes whose subtree has edge-height at least j is floor(n/2^j). Summing those possible downward levels over all nodes gives

```text
sum of node subtree-heights
  = sum over j>=1 of number of nodes with height at least j
  = sum over j>=1 of floor(n / 2^j)
  < n
```

Most nodes can move zero or one level, very few can move many. Constant work per attempted level plus O(n) loop/setup work yields **O(n)** worst-case build time. Equivalently, on a perfect tree the familiar weighted sum is n*(1/4 + 2/8 + 3/16 + ...)=O(n). General complete trees obey the same linear bound.

After processing i backward, every subtree rooted at a position >=i is a heap: its children have larger indexes, so they were already repaired or were leaves. sift-down establishes the subtree at i without damaging the established subtrees. After i=0, the whole array meets heap order. Contents and complete shape never change.

heapifyMin has O(1) auxiliary space. heapFrom additionally requires O(n) **returned/persistent heap storage** and O(n) copy/initialization time. If construction uses a recursive sift-down instead, include O(log(n+1)) stack; the iterative helpers here avoid it.

**Self-check Q12:** Why begin at n/2−1 rather than the final element?

**Self-check Q13:** Why does bottom-up construction take O(n) while repeated insertion can take Θ(n log n)?


## 8. Java 21 PriorityQueue

### 8.1 Queue is an interface; priority is an ordering policy

`Queue<E>` specifies insertion, inspection, and extraction. It does not require FIFO ordering. `PriorityQueue<E>` chooses the least element under its natural or supplied order. For integers, natural order is ascending; reverse order exposes the largest number. [Queue contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Queue.html)

```java
static List<List<Integer>> minMaxDemo() {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    PriorityQueue<Integer> maxHeap =
            new PriorityQueue<>(Comparator.reverseOrder());
    for (int value : new int[]{4, 1, 7, 1}) {
        minHeap.offer(value);
        maxHeap.offer(value);
    }
    return List.of(drainInPriorityOrder(minHeap),
                   drainInPriorityOrder(maxHeap));
}

static List<Integer> drainInPriorityOrder(PriorityQueue<Integer> source) {
    PriorityQueue<Integer> copy = new PriorityQueue<>(source);
    List<Integer> result = new ArrayList<>(copy.size());
    while (!copy.isEmpty()) {
        result.add(copy.poll());
    }
    return result;
}
```

minMaxDemo returns [[1,1,4,7],[7,4,1,1]]. The duplicate 1 appears twice. Each source queue remains unchanged because the drain uses a fresh queue copy with the same comparator. Objects themselves are not deep-copied. Preconditions: source is nonnull and still valid under an unchanged ordering.

A call through `Queue<Integer> q = new PriorityQueue<>();` still uses the implementation's ordering. An interface reference changes the exposed API, not the actual container behavior.

Natural ordering requires mutually comparable elements. A record with neither Comparable nor a supplied comparator can cause ClassCastException when offered. Job and Point examples therefore supply their own ordering explicitly.

### 8.2 Operations and empty behavior

| Operation | Meaning | Empty result / relevant failure | Heap work |
| --- | --- | --- | --- |
| offer(x) / add(x) | Insert x | Null rejected; invalid comparability can fail | O(log(n+1)) |
| peek() | Inspect head without removal | null | O(1) |
| poll() | Remove head | null | O(log(n+1)) |
| element() | Inspect head, throwing form | NoSuchElementException | O(1) |
| remove() | Remove head, throwing form | NoSuchElementException | O(log(n+1)) |
| size() / isEmpty() | Count / emptiness | 0 / true | O(1) |
| contains(x) | Equality-based membership | false if absent | O(n) equality checks |
| remove(Object x) | Remove one equal entry if present | false if absent | O(n) search plus repair |

For this unbounded implementation, a successful offer returns true; it does not impose a maximum queue size. Null elements are forbidden, so a null peek/poll result unambiguously means empty. Assigning that result directly to an int triggers unboxing and can throw NPE. Keep an Integer or check emptiness when the contract permits an empty queue.

The API documents logarithmic enqueue/dequeue heap work. A growth-triggering offer additionally copies a backing array and can take O(n) individual time; over a sequence, geometric growth is amortized. A pre-sized capacity avoids growth until exceeded. This distinction matches the repository's [PriorityQueue complexity discussion](./Complexity/Complexity_Analysis_and_Foundations.md#148-priorityqueue). [Java 21 PriorityQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)

The O(n) membership/removal bounds count equality checks. If user-defined equals costs E, include that cost. PriorityQueue uses equals for these searches, not comparator equality. Multiple records with compare(a,b)==0 may remain distinct entries.

With equality cost E and comparison cost C, use O(n*E+1) for membership and O(n*E+C*log(n+1)+1) for arbitrary removal including repair.

PriorityQueue provides no public decrease-key handle or constant-time arbitrary deletion operation.

### 8.3 Iteration, ties, and sorted output

Only the head is immediately available in priority order. Iterator, enhanced-for, toArray, and toString do not promise sorted traversal. For the valid min-heap layout [1,5,2], index-order inspection is visibly different from sorted [1,2,5]. Never write a test that requires Java's iterator to match a particular heap array.

Repeated polling gives comparator order **while consuming that queue**. To preserve it, poll a copy as in drainInPriorityOrder, or sort a copied collection using the intended comparator. Those choices add copying/output costs: draining n values takes O(n log(n+1)+1) time, O(n) queue-copy space, and O(n) output.

Equal-priority items are not stable by insertion order. If FIFO among ties matters, add a monotonic sequence key to the comparator and define its range. It is a policy you implement, not an automatic heap feature.

### 8.4 Construction costs and capacity

| Construction route | Ordering | Cost to account for |
| --- | --- | --- |
| Empty default queue, then n offers | Natural | O(n log(n+1)+1) heap work and backing growth |
| new PriorityQueue<>(Comparator.reverseOrder()), then offers | Reverse natural | Same bound; comparator supplied at construction |
| new PriorityQueue<>(ordinaryCollection) | Natural | OpenJDK 21 uses copy plus bottom-up heapify: O(n+1) with constant comparisons |
| Copy an existing valid PriorityQueue | Same ordering as source | OpenJDK 21 can copy its valid heap representation in O(n+1) |
| Comparator constructor, then addAll(customRecords) | Supplied comparator | Treat as repeated insertion, O(n log(n+1)+1) |

A SortedSet source also supplies its ordering. A plain List does not carry a comparator merely because it happens to be sorted. There is no public PriorityQueue constructor taking both an arbitrary Collection and a comparator. Collection-constructor behavior and internal build details are distinct: use the explicit bottom-up algorithm to understand the linear proof, and identify the library implementation when citing its construction path. [OpenJDK 21 implementation](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/PriorityQueue.java)

`new PriorityQueue<>(k)` chooses initial capacity, not “keep only k.” Capacity must be positive. The caller must enforce bounded selection. Primitive int[] does not become a List<Integer> through Arrays.asList(values); that produces a single int[] element. Offer the ints in a loop or perform an intentional boxed conversion, counting its allocations.

### 8.5 Queued priority must remain stable

The queue repairs itself when its operations move elements. It does not watch fields inside queued objects or external state read by a comparator.

```java
static final class MutableTask {
    final String name;
    int priority;

    MutableTask(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}

static List<String> mutablePriorityWitness() {
    PriorityQueue<MutableTask> queue = new PriorityQueue<>(
            Comparator.comparingInt(task -> task.priority));
    MutableTask first = new MutableTask("first", 2);
    MutableTask second = new MutableTask("second", 7);
    queue.offer(first);
    queue.offer(second);
    second.priority = 0; // Deliberate misuse: queued key changes.
    return List.of(queue.peek().name,
                   queue.poll().name,
                   queue.poll().name);
}
```

On the checked OpenJDK 21 implementation the result is ["first","first","second"], even though second's changed priority is numerically smaller. The stable-key representation invariant was violated; a logical reprioritization did not trigger sift-up.

Safe approaches depend on the workload:

- For an infrequent update, remove the intended entry **before** changing its key, then offer the updated entry. Searching with remove(Object) is O(n); reinsertion is logarithmic.
- Prefer immutable entries. For frequent event updates, append immutable versioned entries and discard stale versions when polled, using an external current-version map. Bound stale-entry storage and cleanup work; memory need not stay proportional to live tasks.
- Rebuild using all current entries and an explicit valid construction path. Copying an already invalid PriorityQueue is not a promised reheapification repair.
- If arbitrary-key updates dominate, use an indexed heap or another structure designed for them; that is beyond this category's basic implementation.

Mutating external ranking data used by a comparator has the same issue as mutating a field. The comparison must remain stable for each queued entry until it is removed.

**Self-check Q14:** What do peek and poll do on an empty Java PriorityQueue, and how does unboxing change the outcome?

**Self-check Q15:** Why are remove() and remove(Object) different operations with different costs?

**Self-check Q16:** Why does iterating a queue not provide sorted output, and how can you preserve the source while obtaining it?

**Self-check Q17:** Why does a changed queued priority require an explicit update strategy?

## 9. Comparators and numeric safety

### 9.1 Read the sign, not its magnitude

compare(a,b)<0 means a precedes b; zero means equal priority under this ordering; positive means b precedes a. PriorityQueue exposes the comparator's least element, even when that ordering puts the greatest numeric score first.

A comparator must be consistent in direction and transitive. Swapping its arguments must reverse the comparison sign. If a precedes b and b precedes c, the order cannot also place c before a. Equal-priority elements must compare with other values consistently. Returning zero for a tie is legal; FIFO tie behavior needs another key. Comparator equality need not equal Object.equals for a priority queue, which retains duplicates; ordered sets/maps have uniqueness consequences that need additional care. [Comparator contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html)

```java
static int safeIntOrder(int a, int b) {
    return Integer.compare(a, b);
}

static int safeLongOrder(long a, long b) {
    return Long.compare(a, b);
}

record Job(int severity, long arrival, int id) {}

static Comparator<Job> jobOrder() {
    return Comparator.comparingInt(Job::severity)
            .reversed()
            .thenComparingLong(Job::arrival)
            .thenComparingInt(Job::id);
}

static List<Integer> jobOrderDemo() {
    PriorityQueue<Job> jobs = new PriorityQueue<>(jobOrder());
    jobs.offer(new Job(2, 9, 1));
    jobs.offer(new Job(5, 4, 2));
    jobs.offer(new Job(5, 2, 3));
    jobs.offer(new Job(5, 2, 4));
    List<Integer> ids = new ArrayList<>();
    while (!jobs.isEmpty()) {
        ids.add(jobs.poll().id());
    }
    return ids;
}
```

jobOrderDemo returns [3,4,2,1]: greater severity first, then earlier arrival, then smaller unique ID. Reversed applies only to the severity comparator because it appears before the chained tie keys. Reversing the entire final chain would also reverse arrival and ID order.

Records keep the compared primitive fields immutable. Unique IDs make all relevant ties deterministic here; if duplicate identical records are offered, both entries remain.

### 9.2 Why subtraction fails

For a=MIN_VALUE and b=1, a−b wraps to MAX_VALUE, a positive result, incorrectly ranking the smaller a after b. The comparator's sign is corrupted.

Similarly, for a=MAX_VALUE and b=−1, a−b wraps to MIN_VALUE. Even long subtraction can overflow, and narrowing a long difference to int loses information. Use Integer.compare or Long.compare; for extraction-based ordering use comparingInt/comparingLong and explicit tie keys.

Direct relational checks such as value>threshold are safe for int values. Writing value−threshold>0 introduces an overflow that was unnecessary. Negating an int key to simulate reverse order also fails at MIN_VALUE; Comparator.reverseOrder() avoids that trap.

### 9.3 Squared distance needs a range proof

For 2D Euclidean distance from a fixed origin, comparing x²+y² is equivalent to comparing its nonnegative square root. Avoiding sqrt is simpler, but only if arithmetic preserves the actual squared values.

Here every coordinate is restricted to **−10^9..10^9**. Each square is at most 10^18 and the sum at most 2×10^18, below Long.MAX_VALUE. This is an explicit educational domain, broader than some platform examples.

```java
record Point(int x, int y, int id) {}

static long squaredOrigin(Point point) {
    Objects.requireNonNull(point, "point");
    long x = point.x();
    long y = point.y();
    if (x < -1_000_000_000L || x > 1_000_000_000L
            || y < -1_000_000_000L || y > 1_000_000_000L) {
        throw new IllegalArgumentException("coordinate out of range");
    }
    return x * x + y * y;
}

static Comparator<Point> closestOrder() {
    return Comparator.comparingLong(HeapExamples::squaredOrigin)
            .thenComparingInt(Point::id);
}

static List<Integer> pointOrderDemo() {
    PriorityQueue<Point> points = new PriorityQueue<>(closestOrder());
    points.offer(new Point(3, 4, 3));
    points.offer(new Point(0, 5, 2));
    points.offer(new Point(1, 1, 1));
    List<Integer> ids = new ArrayList<>();
    while (!points.isEmpty()) {
        ids.add(points.poll().id());
    }
    return ids;
}
```

The result is [1,2,3], with squared distances 2,25,25. Tie-breaking by ID is an educational policy; a statement that permits any tied subset need not demand that policy.

Widening x into long **before** multiplication prevents int overflow. `(long)(x*x+y*y)` is too late if the expression was evaluated as int. For arbitrary full-range int coordinates, each square individually fits long, but the two-square sum may not: MIN_VALUE in both coordinates sums to 2^63, outside signed long. The helper rejects that larger domain rather than claiming long is universally sufficient.

For distances between two bounded points, subtract in long before squaring. Re-prove the bound on differences; it is not the same bound as distance to a fixed origin. If the domain can exceed long capacity, choose exact wider arithmetic or a problem-specific safe comparison. Do not silently accept wrapped results.

The comparator recomputes a constant amount of arithmetic here. For expensive priorities, precompute a stable key in an immutable entry and count preprocessing/storage. A comparator that scans long strings or performs a graph search is not O(1) merely because the heap invokes it once.

**Self-check Q18:** How can subtraction reverse a comparator result for valid int inputs?

**Self-check Q19:** Where does reversed() belong if only the primary key should descend?

**Self-check Q20:** Why is widening before squaring necessary but not sufficient for every coordinate domain?


## 10. Bounded selection and top-k

### 10.1 Keep the weakest current winner at the root

Suppose scores arrive as [11,4,17,9,17,2] and only the largest three values matter. Sorting all six works, but maintains much more order than selecting three requires.

Retain at most k candidates in a **min-heap**. Its root is the weakest retained winner. A new value can affect the best k only if there is an unfilled slot or it exceeds that threshold. If it is no larger, discard it.

The invariant after every processed input prefix is:

> The heap contains the largest min(k,t) values among the t processed values, as a multiset.

Duplicates count as occurrences. “Third largest” is not “third distinct largest” unless the problem explicitly requires distinctness.

### 10.2 Derive the update and output policies

- Before k candidates exist, retain the incoming value.
- Once full, compare against the smallest retained winner.
- If better, remove that minimum and insert the new value.
- If tied or worse, leave the retained **value multiset** unchanged.
- For sorted output, extract in priority order and place the minima from the end of a result array.

This implementation keeps size<=k at every insertion by removing before offering a replacement. An offer-then-trim implementation briefly holds k+1 and still uses O(k+1) storage, but its code and capacity analysis must reflect that.

```java
static int[] largestDescending(int[] values, int k) {
    Objects.requireNonNull(values, "values");
    if (k < 0 || k > values.length) {
        throw new IllegalArgumentException("k outside 0..n");
    }
    if (k == 0) {
        return new int[0];
    }
    PriorityQueue<Integer> keep = new PriorityQueue<>(k);
    for (int value : values) {
        if (keep.size() < k) {
            keep.offer(value);
        } else if (value > keep.peek()) {
            keep.poll();
            keep.offer(value);
        }
    }
    int[] result = new int[k];
    for (int i = k - 1; i >= 0; i--) {
        result[i] = keep.poll();
    }
    return result;
}
```

Contract: null input -> NPE; k outside 0..n -> IAE; input unchanged; duplicate occurrences retained; output is a fresh array in nonincreasing order. k=0 is allowed even when n=0 and returns immediately. There is no valid kth-rank query at k=0; this is a selection API, not a kth API.

### 10.3 Dry run with ties

Displays below list retained values from smallest to largest to expose the threshold.

| Incoming | Retained before | Decision | Retained after | Threshold when full |
| --- | --- | --- | --- | --- |
| 11 | [] | Fill first slot | [11] | — |
| 4 | [11] | Fill second slot | [4,11] | — |
| 17 | [4,11] | Fill third slot | [4,11,17] | 4 |
| 9 | [4,11,17] | Replace 4 | [9,11,17] | 9 |
| 17 | [9,11,17] | Replace 9 | [11,17,17] | 11 |
| 2 | [11,17,17] | Reject below threshold | [11,17,17] | 11 |
| Output | [11,17,17] | Poll 11,17,17 into positions 2,1,0 | [] | Result [17,17,11] |

A value equal to the threshold need not replace another equal value when only numbers matter. For records with identities, a deterministic tied selection policy requires comparing the complete ranking key. If “earlier arrival wins ties,” rejecting an equally ranked later record can be correct only under that explicit policy.

### 10.4 Retained-set correctness

Initialization satisfies the invariant with no processed values. While fewer than k exist, retaining every seen value is necessary and sufficient.

Once full, let r be the smallest retained value. If x<=r, all k retained values are >=x, so some valid largest-k multiset remains unchanged. If x>r, the new largest k are obtained by replacing one occurrence of the old weakest winner with x. No discarded value can become a better candidate merely because a stronger value arrived. Heap order provides r; the processed-prefix argument proves why keeping only those k values is enough.

At termination, the retained multiset is the requested largest k. Polling extracts it in ascending order, so filling the result backward produces descending output. Each loop advances through finite input or reduces heap size, proving termination.

### 10.5 The mirror: k smallest values

Use a max-heap to expose the largest retained value, which is the current rejection threshold for the smallest k:

```java
static int[] smallestAscending(int[] values, int k) {
    Objects.requireNonNull(values, "values");
    if (k < 0 || k > values.length) {
        throw new IllegalArgumentException("k outside 0..n");
    }
    if (k == 0) {
        return new int[0];
    }
    PriorityQueue<Integer> keep =
            new PriorityQueue<>(k, Comparator.reverseOrder());
    for (int value : values) {
        if (keep.size() < k) {
            keep.offer(value);
        } else if (value < keep.peek()) {
            keep.poll();
            keep.offer(value);
        }
    }
    int[] result = new int[k];
    for (int i = k - 1; i >= 0; i--) {
        result[i] = keep.poll();
    }
    return result;
}
```

On [11,4,17,9,17,2], k=3, the result is [2,4,9]. Preconditions, invalid-k policy, mutation, and duplicate rules match largestDescending, with the output direction reversed accordingly.

For closest points, rank by distance and retain k candidates in the reverse of the desired best-first ordering: the farthest retained candidate must be at root. Use the [safe distance key](#9-comparators-and-numeric-safety) and state the tie policy. A numeric min-heap of distances would evict the closest points and keep the wrong set.

### 10.6 Total cost and alternatives

For 1<=k<=n, selection costs O(n log(k+1)) time and O(k) working heap storage. Initial capacity allocation is O(k), and sorted extraction costs O(k log(k+1)); both are absorbed because k<=n. The required result occupies O(k) additional output. Comparator cost C changes heap comparison work to O(C*n log(k+1)), plus key preprocessing and output construction.

The code's pre-sizing and size bound avoid backing-array growth beyond k during processing. k=1 reduces to a maximum/minimum scan with heap overhead; direct scanning is simpler. k=n produces a full ordering using O(n log(n+1)) time; sorting may be clearer and faster in practice.

| Approach | Time model | Storage / tradeoff |
| --- | --- | --- |
| Sort all, then select | O(n log(n+1)) plus output | Full order; copying preserves input but adds O(n); library workspace must be included |
| Bounded heap | O(n log(k+1)) for k>=1 | O(k) candidates, suitable online; sorted output adds extraction |
| Full heap, then poll k | O(n+k log(n+1)) with a linear build | O(n) stored values; repeated-offer construction instead costs O(n log n) |
| Optional Quickselect | Expected O(n), classic worst O(n²) | Usually mutates input; returns a partition/rank, not sorted output or an online index |
| Scan for one extreme | O(n+1) | O(1) working state, simplest for k=1 |

For k=0, the supplied methods perform O(1) work after the null/parameter checks and return an empty array. An alternative “up to k” API could allow k>n and return everything, but these static functions deliberately reject it. Streaming code below instead reports when insufficient values have arrived.

**Self-check Q21:** Why does selecting the k largest use a min-heap?

**Self-check Q22:** Why can a rejected value be forgotten permanently in an append-only stream?

**Self-check Q23:** Which costs are missing if someone reports O(n log k) but ignores k=1, initialization, and required sorted output?

## 11. Streaming rank

### 11.1 An online answer has a different lifetime

A static algorithm sees a complete array and can sort it once. An online algorithm must answer after each arrival without knowing future values. Re-sorting all observations after every arrival can repeatedly process the entire history.

To maintain the kth largest occurrence, retain only the largest k observed values in a min-heap. Its root is the kth largest **only after k values are retained**. Before then there is no kth value; the educational API uses OptionalInt.empty rather than pretending the minimum of a short prefix is the kth.

```java
static final class RankStream {
    private final int k;
    private final PriorityQueue<Integer> keep = new PriorityQueue<>();

    RankStream(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
    }

    OptionalInt accept(int value) {
        if (keep.size() < k) {
            keep.offer(value);
        } else if (value > keep.peek()) {
            keep.poll();
            keep.offer(value);
        }
        return keep.size() == k
                ? OptionalInt.of(keep.peek())
                : OptionalInt.empty();
    }
}
```

Every accepted value is an int occurrence, including duplicates. The stream is append-only; deletion/expiration changes the retained-set proof and is outside this class. Independent instances own independent state. Each return is a scalar optional value, not an exposed heap reference.

### 11.2 Operation-by-operation rank trace

k=3; values arrive [6,1,9,4,12,12,0].

| Arrival | Retained before | Update | Retained after | kth largest |
| --- | --- | --- | --- | --- |
| 6 | [] | Fill | [6] | Absent |
| 1 | [6] | Fill | [1,6] | Absent |
| 9 | [1,6] | Fill | [1,6,9] | 1 |
| 4 | [1,6,9] | Replace 1 | [4,6,9] | 4 |
| 12 | [4,6,9] | Replace 4 | [6,9,12] | 6 |
| 12 | [6,9,12] | Replace 6 | [9,12,12] | 9 |
| 0 | [9,12,12] | Reject | [9,12,12] | 9 |

The retained-set proof is the same as static bounded selection; the difference is that the heap persists across calls and is not drained to form a final output. The root becomes a useful answer after every update.

With t observations, persistent storage is O(min(k,t)+1). Heap comparison work per update is O(log(k+1)) worst-case, and a rejected value costs O(1). Because this implementation grows from default capacity, a growth-triggering offer may copy O(min(k,t)) references; update time is O(log(k+1)) **amortized** across normal growth. Pre-sizing k can avoid that growth at the cost of O(k) up-front allocation even before k observations.

Processing m initial or later arrivals costs O(m log(k+1)+1) total, including amortized growth. Returning all m rank answers would add O(m) output storage; this class stores no answer history. Maintaining an entire sorted history is appropriate if future requirements demand arbitrary ranks or deletions, but it spends more state than this one-rank task needs.

**Self-check Q24:** Why is the heap root not yet a kth-largest answer after only k−1 observations?

**Self-check Q25:** What changes in memory lifetime and update analysis between a static top-k function and RankStream?

## 12. Repeated-priority simulation

### 12.1 Extract, transform, and reinsert

When an operation changes one item's priority, the next extreme can change immediately. Sorting once cannot maintain that evolving decision. Re-sorting the whole set after every step also does unnecessary work when only one item changed.

Original educational model: nonnegative workloads represent remaining batches. In each round, choose a largest workload and reduce it to ceil(load/2), reinserting the result. Perform at most the requested rounds; with no entries, return zero. This is a defined simulation, not a claim that this greedy choice optimizes every workload objective.

```java
static long reduceLargest(int[] loads, int rounds) {
    Objects.requireNonNull(loads, "loads");
    if (rounds < 0) {
        throw new IllegalArgumentException("negative rounds");
    }
    for (int load : loads) {
        if (load < 0 || load > 1_000_000_000) {
            throw new IllegalArgumentException("load outside range");
        }
    }
    PriorityQueue<Integer> work =
            new PriorityQueue<>(Comparator.reverseOrder());
    long total = 0;
    for (int load : loads) {
        work.offer(load);
        total += load;
    }
    for (int step = 0; step < rounds && !work.isEmpty(); step++) {
        int largest = work.poll();
        int reduced = largest / 2 + largest % 2;
        total -= (long) largest - reduced;
        work.offer(reduced);
    }
    return total;
}
```

Additional domain: loads length<=100000 and rounds<=100000. Each load lies in 0..10^9, so total<=10^14 fits long. Validation happens before building the local heap; the input array is unchanged. Division/remainder computes the ceiling without adding one to a potentially extreme key. Zero remains zero; one remains one. A fixed round counter ensures termination even if all keys stop changing.

### 12.2 Original simulation trace

loads=[19,10,7], rounds=4. Heap display is descending.

| Round | Before | Extract | Reinsert | After | Total |
| --- | --- | --- | --- | --- | --- |
| Initial | — | — | 19,10,7 | [19,10,7] | 36 |
| 1 | [19,10,7] | 19 | 10 | [10,10,7] | 27 |
| 2 | [10,10,7] | 10 | 5 | [10,7,5] | 22 |
| 3 | [10,7,5] | 10 | 5 | [7,5,5] | 17 |
| 4 | [7,5,5] | 7 | 4 | [5,5,4] | 14 |

The heap multiset always equals the current simulation state, and total equals its sum. Poll chooses an allowed largest item by max-heap order; replacing exactly that occurrence by its specified transformation preserves both invariants. Other equal maxima are interchangeable in this numeric model.

This code builds with n offers, so total time is O((n+m) log(n+1)+1), where m is the number of rounds actually executed. Local working heap storage is O(n); the scalar result is O(1). The heap may allocate during initialization but does not exceed its initial populated size during the poll-then-offer rounds. A custom linear max-heap build could reduce initialization to O(n).

Compare with sorting the n current values each round: O(m*n log(n+1)) in the straightforward repeated-sort baseline. Choosing one maximum by a fresh scan each round costs O(m*n) and can be entirely reasonable for tiny n or very few rounds.

### 12.3 Combining items and selecting events

[Last Stone Weight](https://leetcode.com/problems/last-stone-weight/) is an Essential conceptual reference for processing the largest current items, but its particular transform differs from the halving model above. Always define the update rule and count its effect on heap size.

If each round removes two items and reinserts at most one, size decreases by at least one, giving at most n−1 such rounds. More generally, with n initial inserts and m rounds each performing O(1) heap operations, the safe bound is O((n+m) log(M+1)), where M is the maximum live heap size; include initialization separately if it uses linear construction.

For events, the key might be earliest scheduled time rather than largest load. Polling an event can generate future immutable events. A heap exposes the next event efficiently, but correctness also requires processing times in the right model and proving that event generation terminates. “Uses a heap” is not a termination proof.

Prefer direct counting when priorities occupy a tiny fixed domain and counts suffice; prefer a single scan when only one extreme decision is needed. Use a balanced ordered map if predecessor/range queries or counted keys are central.

**Self-check Q26:** Why can a fixed initially sorted array become invalid after one transformation?

**Self-check Q27:** Which quantity bounds the number of rounds when two items are removed and at most one is reinserted?


## 13. Merging ordered streams

### 13.1 One exposed candidate per nonempty source

A straightforward approach concatenates all values and sorts them, ignoring the order already present in each source. Another baseline scans all k current heads before every output, costing O(N*k) comparisons.

If each source is nondecreasing, its first unread value is no greater than any later unread value in that source. Therefore the global smallest unread value must be one of the current heads. Keep those heads in a min-heap, together with enough source information to advance only the chosen source.

The frontier invariant is:

> Every nonexhausted source contributes exactly its first unread value, and the output is the sorted committed prefix of all consumed values.

### 13.2 Immutable source-aware entries

```java
record Cursor(int value, int source, int offset) {}

static int[] mergeSorted(int[][] streams) {
    Objects.requireNonNull(streams, "streams");
    long total = 0;
    for (int[] stream : streams) {
        Objects.requireNonNull(stream, "stream");
        total += stream.length;
    }
    if (total > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("output too large");
    }
    Comparator<Cursor> order = Comparator.comparingInt(Cursor::value)
            .thenComparingInt(Cursor::source)
            .thenComparingInt(Cursor::offset);
    PriorityQueue<Cursor> frontier = new PriorityQueue<>(order);
    for (int source = 0; source < streams.length; source++) {
        if (streams[source].length > 0) {
            frontier.offer(new Cursor(streams[source][0], source, 0));
        }
    }
    int[] result = new int[(int) total];
    int used = 0;
    while (!frontier.isEmpty()) {
        Cursor first = frontier.poll();
        result[used++] = first.value();
        int nextOffset = first.offset() + 1;
        int[] source = streams[first.source()];
        if (nextOffset < source.length) {
            frontier.offer(new Cursor(source[nextOffset],
                    first.source(), nextOffset));
        }
    }
    return result;
}
```

Contract: outer/inner arrays nonnull or NPE; each row already sorted nondecreasing; total elements N<=100000 and number of rows k<=100000; input arrays unchanged. Empty sources and empty outer array are allowed. Duplicate values are all retained. Output is a fresh nondecreasing array. Source/offset tie keys make the conceptual provenance deterministic, although equal output integers are indistinguishable.

The code does not scan rows to revalidate sortedness. If unsorted inputs are possible, validate or define a different problem; the frontier proof otherwise fails. The length sum uses long and checks representability before narrowing for allocation.

### 13.3 Original frontier trace

Sources: S0=[1,6,9], S1=[2,6], S2=[0,7], S3=[].

Here v@Sj means the current value v from source j. Frontier displays are comparator-sorted displays, not internal arrays.

| Step | Frontier before | Extract | New frontier candidate | Frontier after | Output so far |
| --- | --- | --- | --- | --- | --- |
| 1 | [0@S2,1@S0,2@S1] | 0@S2 | 7@S2 | [1@S0,2@S1,7@S2] | [0] |
| 2 | [1@S0,2@S1,7@S2] | 1@S0 | 6@S0 | [2@S1,6@S0,7@S2] | [0,1] |
| 3 | [2@S1,6@S0,7@S2] | 2@S1 | 6@S1 | [6@S0,6@S1,7@S2] | [0,1,2] |
| 4 | [6@S0,6@S1,7@S2] | 6@S0 | 9@S0 | [6@S1,7@S2,9@S0] | [0,1,2,6] |
| 5 | [6@S1,7@S2,9@S0] | 6@S1 | Source exhausted | [7@S2,9@S0] | [0,1,2,6,6] |
| 6 | [7@S2,9@S0] | 7@S2 | Source exhausted | [9@S0] | [0,1,2,6,6,7] |
| 7 | [9@S0] | 9@S0 | Source exhausted | [] | [0,1,2,6,6,7,9] |

No candidate is inserted from S3. Advancing only the extracted source keeps all other heads available. Equal sixes are output twice, with S0 selected first under the stated tie rule.

### 13.4 Correctness, termination, and storage

Each source head is its own smallest unread value, so the least frontier entry is a global minimum. Appending it preserves sorted output. Removing that source's head and inserting its successor restores one candidate per nonexhausted source. No value is skipped or duplicated because the offset advances once for each emitted value. Every iteration emits one of N values, proving termination.

Let a be the number of initially nonempty sources, a<=min(k,N) when N>0. This implementation scans k source headers, initializes with a offers, and performs N polls plus at most N successor offers. Total time is

```text
O(k + N log(a+1) + 1)
```

The initialization term a log(a+1) is absorbed by N log(a+1). When all sources are nonempty, this is the usual O(N log(k+1)) shorthand, including the O(k) scan because k<=N. With many empty sources, keep the O(k) header cost. If a=1, merging is linear in N; the +1 inside the logarithm avoids a misleading log(1)=0 bound.

Peak auxiliary heap/cursor storage is O(a+1), and required output is O(N). Cursor objects are created over the run, but only O(a) are live in the frontier at once; cumulative allocation is O(N), distinct from peak live space. An iterator/consumer output API could avoid the result array when the caller does not require materialized output.

This generalizes the [two-chain merge invariant](./Linked_List/Linked_List_Essential_Concepts.md#11-merging-sorted-chains). For the roadmap's optional [Merge k Sorted Lists](https://leetcode.com/problems/merge-k-sorted-lists/), the frontier carries node references and source successors rather than array offsets. Node ownership, shared suffixes, and destructive versus copied output still need a separate contract. The array example does not grant permission to rewire input lists.

For just two sorted arrays/lists, a direct two-pointer merge is O(N) with less overhead. For unsorted sources, one-frontier-per-source is not justified; sorting or another approach is needed.

**Self-check Q28:** Why can one frontier candidate represent all remaining values in one sorted source?

**Self-check Q29:** Why must the source and successor position accompany a candidate?

**Self-check Q30:** Why can O(N log k) omit important work when many input sources are empty?

## 14. Scheduling and availability

### 14.1 Priority is not eligibility

A task can have high priority but be temporarily unavailable. A scheduling simulation must distinguish:

- **Available tasks:** eligible to run now; a heap chooses among them.
- **Cooling tasks:** unfinished tasks with a future eligibility time.
- **Current time:** the start of the next unit interval.
- **Idle intervals:** time that must pass when no task is currently eligible.

This section uses unit tasks, one processor, labels A..Z, all occurrences available initially, one common cooldown, and no precedence constraints. Count remaining occurrences by label. A 26-slot frequency array supplies worst-case O(n) preprocessing; a frequency map generalizes the labels with its hashing/key-cost assumptions. Reuse [frequency counting](./Java_Map_HashMap_TreeMap_LinkedHashMap.md#frequency-counting). A max-heap can rank available labels by remaining frequency, with a deterministic label tie rule for examples.

The heap cannot by itself remember that a label was just used. Place its remaining count in cooldown state instead of immediately returning it to the available heap.

### 14.2 Define time precisely

A task run at integer time t occupies [t,t+1). With cooldown c>=0, the same label can next start at

```text
readyAt = t + c + 1
```

For c=2 and execution at t=0, starts at 1 or 2 are forbidden; 3 is allowed. Using t+c would admit a task one interval too early. Use long times and prove bounds on task count and cooldown before doing arithmetic.

With a common fixed cooldown and one unit execution per time step, enqueued readyAt values increase with execution times, so a FIFO cooldown queue is sufficient. With task-specific delays or out-of-order event generation, use release-time ordering, often a separate min-heap. A queue choice depends on the release-time invariant.

A conceptual simulation repeatedly releases **all** entries with readyAt<=time, chooses one eligible heap entry, executes it, and queues its remaining count if positive. If the available heap is empty but cooldown is nonempty, time can jump directly to the earliest readyAt; those skipped intervals are idle. The heap ranks available candidates; it does not turn unavailable tasks into legal choices.

### 14.3 Original schedule with an idle interval

Tasks A,A,A,B,B,C, cooldown=2. Tie order is larger remaining count first, then alphabetic label. Cooldown displays are after the step.

| Time | Released at start | Available before choosing | Action | Cooling after step |
| --- | --- | --- | --- | --- |
| 0 | None | [A:3,B:2,C:1] | Run A; remaining A:2 | [A:2 ready@3] |
| 1 | None | [B:2,C:1] | Run B; remaining B:1 | [A:2 ready@3,B:1 ready@4] |
| 2 | None | [C:1] | Run C; complete | [A:2 ready@3,B:1 ready@4] |
| 3 | A:2 | [A:2] | Run A; remaining A:1 | [B:1 ready@4,A:1 ready@6] |
| 4 | B:1 | [B:1] | Run B; complete | [A:1 ready@6] |
| 5 | None | [] | Idle; advance to 6 | [A:1 ready@6] |
| 6 | A:1 | [A:1] | Run A; complete | [] |

Timeline: [A,B,C,A,B,idle,A], elapsed length 7. A starts at 0,3,6 and B at 1,4, meeting the cooldown exactly where required.

Three A occurrences with two intervening intervals require at least 7 total intervals, while executing six tasks requires at least 6. The shown schedule attains the stronger lower bound, so this particular instance is optimal. This is a correctness/optimality argument for the example; merely choosing a max-heap is not an optimality proof for every scheduling variant.

### 14.4 Costs and alternatives

If n unit-task executions use u distinct labels, an implementation with a heap plus cooldown queue can perform O(n) heap operations at size<=u, after O(n) frequency preprocessing and O(u log(u+1)) insertion-based initialization. With direct idle jumps, the resulting bound is O(n log(u+1)+u+1), with O(u) scheduling state under the common-cooldown model.

A simulator that visits every idle interval costs at least Ω(T), where T is total elapsed time, which can greatly exceed n. Returning a complete timeline also costs Ω(T) output work/storage even if the internal algorithm jumps over idle runs. Returning only the elapsed length or a compressed schedule has different output costs.

[Task Scheduler](https://leetcode.com/problems/task-scheduler/) is **Additional**, and also admits a counting-based approach under its specific assumptions. Reason from the maximum frequency, the number of labels tied at that frequency, and the separation slots; establish both a lower bound and attainability before using a formula. No ready-time heap is needed if a proved count answers the entire question.

Release times, unequal durations, deadlines, multiple processors, and task-specific cooldowns define different scheduling problems. A heap is useful only after clarifying the actual eligibility and objective rules. This guide introduces the state distinction rather than providing a complete platform scheduler implementation.

**Self-check Q31:** Why must availability/cooldown state be separate from priority?

**Self-check Q32:** Why can a time-jumping simulator be faster than one that emits every idle interval?

## 15. Two-heap median maintenance: optional

This is **OPTIONAL ADVANCED MATERIAL**, corresponding to the roadmap's C-tier Hard problem. It does not block ordinary Heap interview readiness. First establish reliable one-heap invariants and bounded selection.

### 15.1 Two boundaries identify the middle

For an append-only stream of ints, retain all values split into two halves:

```text
lower half: max-heap                    upper half: min-heap
largest lower value at root            smallest upper value at root
              L        <=        U

size(lower) == size(upper)
or size(lower) == size(upper) + 1
```

Maintain both:

1. **Ordering:** every lower-half value is <= every upper-half value.
2. **Balance:** lower has the same number as upper, or exactly one more.

If lower is larger, its maximum is the middle element. If sizes tie, the middle pair is lower.max and upper.min. Either a size invariant alone or an ordering invariant alone is insufficient.

### 15.2 Insertion and rebalancing

```java
static final class GrowingMedian {
    private final PriorityQueue<Integer> lower =
            new PriorityQueue<>(Comparator.reverseOrder());
    private final PriorityQueue<Integer> upper = new PriorityQueue<>();

    void accept(int value) {
        if (lower.isEmpty() || value <= lower.peek()) {
            lower.offer(value);
        } else {
            upper.offer(value);
        }
        if (lower.size() > upper.size() + 1) {
            upper.offer(lower.poll());
        } else if (upper.size() > lower.size()) {
            lower.offer(upper.poll());
        }
    }

    OptionalDouble median() {
        if (lower.isEmpty()) {
            return OptionalDouble.empty();
        }
        if (lower.size() > upper.size()) {
            return OptionalDouble.of(lower.peek());
        }
        long sum = (long) lower.peek() + upper.peek();
        return OptionalDouble.of(sum / 2.0);
    }
}
```

Contract: append-only int occurrences, including duplicates and extremes; at most 100000 observations in these examples; independent instances; no deletion. Empty median is absent. The median query is read-only.

Choosing the half by the current lower maximum preserves cross-half ordering. If insertion makes one half too large, moving its appropriate boundary element restores balance while preserving ordering: lower's maximum belongs before every remaining upper value, and upper's minimum belongs after every remaining lower value. One new value can create imbalance of at most two under the chosen size convention, so at most one transfer is needed.

### 15.3 Original median trace

Each half is shown in its removal order: lower descending and upper ascending.

| Incoming | Lower after rebalance | Upper after rebalance | Median |
| --- | --- | --- | --- |
| 8 | [8] | [] | 8.0 |
| 2 | [2] | [8] | 5.0 |
| 10 | [8,2] | [10] | 8.0 |
| 4 | [4,2] | [8,10] | 6.0 |
| 4 | [4,4,2] | [8,10] | 4.0 |
| −2 | [4,2,−2] | [4,8,10] | 4.0 |

Equal fours may legitimately appear on both sides of the boundary. The ordering condition is <=, not strict <.

Insertion uses O(log(n+1)) heap work, amortized across library backing growth; median retrieval uses O(1) arithmetic and root inspection. Persistent storage is O(n): unlike kth-largest tracking, the two heaps collectively retain every observation. Growth can add a linear copy to an individual insertion, and output history is separate.

Promote before adding the two int roots: `(long)lower.peek()+upper.peek()`. With two MAX_VALUE inputs, int addition would wrap to −2 and a naive /2.0 would incorrectly report −1.0. Here the long sum fits and the correct median is 2147483647.0. MIN_VALUE and MAX_VALUE average to −0.5. Dividing by integer 2 would also lose a half-unit for an odd sum.

A single unsorted heap does not expose the central rank in O(1). Sorting a small static array can be simpler; a balanced order-statistic tree supports richer rank/deletion operations but requires additional structure.

| Approach to a changing median | Immediately available information | Cost / limitation |
| --- | --- | --- |
| One ordinary min-heap or max-heap | One extreme | Its root does not identify the central rank |
| Two ordered, balanced heaps | The one or two middle boundary values | O(n) persistent storage, logarithmic heap updates |
| Sort each observed prefix | Every rank | Repeated full ordering costs O(t log(t+1)) for a prefix of length t |

**Self-check Q33:** Why do two-heap medians require both balance and cross-half order?

**Self-check Q34:** Why is this stream's persistent storage O(n) rather than O(k)?


## 16. Complete complexity analysis

### 16.1 Define the quantities for the actual algorithm

| Symbol | Meaning |
| --- | --- |
| n | Current heap size or input item count, as stated locally |
| k | Selection target or number of supplied streams |
| a | Initially nonempty streams; frontier size never exceeds a |
| m | Number of simulation rounds, events, or stream updates |
| N | Total merged elements |
| u | Number of distinct task labels |
| C | Cost of one comparator evaluation |
| T | Total elapsed schedule length, including idle intervals |
| cap | Allocated backing-array capacity |
| M | Maximum live heap size over an algorithm |

Do not silently reuse k as both a selection size and an unrelated stream count in one derivation. Include +1 setup at empty boundaries. All bounds assume fixed-width primitive arithmetic unless a wider numeric model is explicitly selected.

### 16.2 Operation and algorithm table

| Operation / algorithm | Time | Auxiliary work storage | Representation / output to report |
| --- | --- | --- | --- |
| Complete heap height | Node-height floor(log2 n)+1 for n>0 | — | Shape property, not BST order |
| Iterative sift-up/down | O(C log(n+1)+1) worst case | O(1) | Existing array is input/state |
| Custom fixed-capacity offer/poll | O(log(n+1)+1), primitive comparisons | O(1) | Owned array has cap slots |
| Custom peek/size/isEmpty | O(1) | O(1) | No output collection |
| In-place bottom-up heapify | O(n+1) for constant comparisons | O(1) | Mutates existing array |
| heapFrom copying constructor helper | O(n+1) | O(1) beyond returned heap | O(n) returned/persistent array |
| PriorityQueue offer | Logarithmic heap work; amortized O(C log(n+1)+1) with growth | A growth may transiently allocate/copy O(n) | Backing capacity persists |
| PriorityQueue poll | O(C log(n+1)+1) | O(1) ordinary repair | May retain previously allocated capacity |
| PriorityQueue contains/remove(Object) | O(n) equality search with constant equals, plus repair for removal | O(1) ordinary search/repair | Not indexed deletion |
| Bounded top-k, k>=1 | O(n log(k+1)) including this code's output extraction | O(k) working heap | O(k) required returned array |
| RankStream update | O(log(k+1)) heap work; amortized with default growth | O(1) locals plus possible growth copy | O(min(k,t)+1) persistent candidates after t arrivals |
| Repeated simulation in Section 12 | O((n+m) log(n+1)+1) | O(n) working heap | Scalar result; input unchanged |
| Ordered-stream merge | O(k+N log(a+1)+1) | O(a+1) frontier | O(N) required result |
| Common-cooldown simulation with jumps | O(n log(u+1)+u+1) under the stated state model | O(u+1) | A full timeline adds O(T) output |
| Optional median insertion/query | Amortized O(log(n+1)) / O(1) | O(1) explicit update/query locals; growth can allocate | O(n) persistent heap state |

The library's backing array matters independently of the logical repair. An individual offer that grows capacity can take O(n+C log(n+1)) time; reporting only logarithmic worst-case latency would hide the copy. Pre-sizing changes initialization cost, not the fundamental heap invariant.

For a general PriorityQueue, storage is O(cap), not necessarily O(current size) after a large queue has been drained. The checked OpenJDK implementation does not shrink its backing array on each poll. With ordinary growth from empty, allocated capacity is O(M+1), where M is the previous peak occupancy. The bounded and median examples have more specific monotone/bounded size histories that justify their stated storage.

When C is nonconstant, multiply the number of comparisons by C or sum their actual costs. Building stable cached keys can move that work into preprocessing, which must be counted rather than forgotten.

### 16.3 Derive total work from operations

A useful analysis sequence is:

1. Count preprocessing: validation, frequency counting, copies, key calculations, and heap construction.
2. Bound the maximum live heap size.
3. Count offers, polls, comparisons, and equality searches.
4. Apply the correct size-sensitive cost to each operation.
5. Add output sorting/extraction, materialization, and idle traversal.
6. Report temporary, persistent, and returned storage separately.

“Every operation is logarithmic” does not establish the total until the number of operations and heap-size bound are known. A shrinking two-item simulation can have O(n) rounds; an event generator may create many more events than input items.

The space convention does not erase memory. A heap returned by heapFrom is output; a heap local to largestDescending is auxiliary; RankStream's heap is persistent service state. All three occupy allocated arrays.

**Self-check Q35:** Why can one Java PriorityQueue offer exceed logarithmic time even when its sift-up is logarithmic?

**Self-check Q36:** How do working heap storage, persistent state, allocated capacity, and returned output differ?

## 17. Choosing an alternative

Use the required operations, update pattern, key domain, and output contract rather than choosing a container from a problem tag.

| Requirement | Useful starting point | Why / tradeoff |
| --- | --- | --- |
| One maximum/minimum | Simple scan | O(n+1) time, O(1) working state |
| All values in full order | Sorting | O(n log n) comparison work, contiguous traversal afterward |
| Repeated next extreme with updates | Heap | Logarithmic local repair; no full sort |
| Online best k or kth rank | Bounded heap | Retain only necessary candidates; not arbitrary rank/deletion |
| Exact key→payload lookup | HashMap | Expected key access, not a maintained numeric extreme |
| Predecessor, successor, sorted key ranges | Balanced ordered map/set | O(log n) comparisons and ordered navigation; counts needed for duplicate keys |
| Find a key in a sorted array | Binary search | O(log n) comparisons, no priority-queue preprocessing |
| One static order statistic | Optional Quickselect | Expected linear selection; mutation/worst case/output order need care |
| Merge two sorted sequences | Two pointers / two heads | Linear direct merge, no heap necessary |
| Merge many sorted sequences | Heap frontier | One candidate per active source |
| Tiny bounded priority domain | Frequency/bucket counts | Scans/counting can avoid comparison-heap overhead |
| Static cooldown length only | Proved counting formula where applicable | No event history if counts fully determine the answer |
| Online median | Optional two heaps | Central boundary in O(1), with all observations retained |
| Complete array heap versus linked-node structure | Choose shape representation separately from priority | Array indexes encode relatives; linked nodes need explicit references and a way to locate the next complete-tree position |

A heap and a HashMap solve different access questions. A map can supply frequencies or locate current versions while a heap supplies priority order. Combining them requires a state invariant linking the two, not a claim that either automatically provides the other's operations.

A heap and a balanced BST also differ. The heap optimizes root extraction with a compact complete layout. A balanced search tree maintains key partitions useful for membership/range/neighbor operations. Arbitrary BSTs may be unbalanced; logarithmic ordered operations need a balancing guarantee. A TreeMap stores one mapping per comparator-equal key, so use a frequency value if duplicate numeric priorities represent multiple items.

Sorted arrays provide constant-time access to an extreme at an appropriate endpoint and efficient ordered traversal, but maintaining arbitrary dynamic insertions can require O(n) shifts. An unsorted array provides cheap append but needs O(n) scanning to select each extreme. A binary heap occupies the middle ground.

Quickselect is an optional future comparison. Randomized/simple versions have expected O(n) selection and O(n²) worst-case behavior; stronger worst-case algorithms exist but are beyond this guide. Do not claim every library selection routine has the same guarantees. After selecting a partition, sorting its k winners still costs O(k log k) if required.

A heap's better asymptotic selection bound does not guarantee better measured performance on small inputs or k near n. Sorting has locality and implementation advantages. Explain both feasibility and why the added structure is justified by the workload.

**Self-check Q37:** Give one task where scanning, sorting, hashing, and binary search each beat the need for a heap.

## 18. Java pitfalls and counterexamples

### 18.1 A subtraction comparator is observably wrong

The following is a deliberately faulty comparison under the intended ascending numeric order:

```java
static int badIntComparison(int a, int b) {
    return a - b;
}
```

badIntComparison(MIN_VALUE,1) returns a positive value instead of a negative one. badIntComparison(MAX_VALUE,-1) returns a negative value instead of a positive one. These are genuine valid-input failures. Use Integer.compare(a,b), not a larger collection wrapper around the same flawed rule.

### 18.2 A late cast cannot recover a square

```java
static long badSquaredDistance(int x, int y) {
    return (long) (x * x + y * y);
}
```

At x=50000,y=0, the correct value is 2500000000, but the method returns −1794967296 because int multiplication already overflowed. Widen operands before multiplication and prove the sum bound, as squaredOrigin does.

### 18.3 A wrong threshold heap keeps the opposite candidates

This flawed routine intends to return the kth largest occurrence. Preconditions here are nonnull input and 1<=k<=values.length.

```java
static int wrongKthThreshold(int[] values, int k) {
    PriorityQueue<Integer> keep =
            new PriorityQueue<>(Comparator.reverseOrder());
    for (int value : values) {
        keep.offer(value);
        if (keep.size() > k) {
            keep.poll();
        }
    }
    return keep.peek();
}
```

For [10,9,1,0], k=2, it returns 1 rather than 9. It discards the current largest whenever over capacity, retaining the smallest two instead. The defect is the relationship between the requested retained set and the eviction threshold. For the k largest, root must be the weakest retained winner.

### 18.4 A fixed left-child choice can fail

The next snippet deliberately ignores the right child, though both child subtrees satisfy min-heap order.

```java
static void wrongSiftDown(int[] a, int size, int i) {
    while (i < size / 2) {
        int left = 2 * i + 1;
        if (a[i] <= a[left]) {
            break;
        }
        swap(a, i, left);
        i = left;
    }
}
```

With active [8,5,2], size=3, i=0, it produces [5,8,2], whose root 5 still exceeds child 2. Correct sift-down must choose the smaller existing child, not simply the first child. A one-child fixture alone would not reveal this defect.

### 18.5 Other mistakes to test deliberately

| Incorrect assumption | Failure-focused example | Required interpretation / repair |
| --- | --- | --- |
| Default PriorityQueue is a max-heap | Insert 4,1,7; peek gives 1 | Supply reverse ordering for largest-first processing |
| Heap array or iteration is sorted | Valid min-heap [1,5,2] has unsorted array order | Poll, or sort a copy using the intended comparator |
| Equal priority implies FIFO | Several records tie under a comparator | Add an arrival-sequence tie key if stability is required |
| A comparator tie deduplicates a queue | Offer two distinct equal-priority records | Both remain; equals-based removal and comparator priority are different |
| Initial capacity k bounds occupancy | Create capacity 2, offer 1,2,3 | Size becomes 3; enforce the selection invariant yourself |
| kth largest means distinct rank | Values [5,5,4], k=2 | Occurrence rank is 5; distinct rank would be 4 under a changed problem |
| Every positive k is valid for an array | n=2,k=3 | Static selection contract rejects it; a stream can instead report insufficient observations |
| k=1 can be analyzed with log(1)=0 work | Must still inspect input candidates | Use log(k+1) and include scans/setup |
| poll() always returns an int | Empty queue returns null | Check or keep Integer before unboxing |
| remove(7) is an indexed/logarithmic deletion | Search for numeric 7 in a priority queue | remove(Object) searches equality in O(n); no indexed key API |
| Mutating an entry updates its position | mutablePriorityWitness changes second's key | Remove/update/reinsert or use a documented immutable-version strategy |
| Copying an invalid queue repairs it | A queue contains changed keys | A valid-heap copy path can preserve invalid order; rebuild explicitly |
| Root has parent index 0 | (0−1)/2 evaluates to 0 in Java | Guard i>0; root has no parent |
| Every internal node has two children | n=4, node 1 has only child 3 | Check right<size before reading |
| Active size equals backing capacity | A fixed-capacity heap has empty slots | Ignore positions >=size |
| Every node costs root-height during build | Half the nodes are leaves | Sum actual possible sift heights; bottom-up build is linear |
| Returning arbitrary retained order is always allowed | Caller requires descending scores | Extract/sort and count output work |
| A single heap exposes median at root | Values [1,2,100] in a min-heap | Minimum is not the central rank; use an appropriate rank structure |
| A heap enforces cooldown by itself | A has greatest count immediately after running | Move unfinished unavailable entries to separate release state |

All standalone faulty snippets above compile, but their output/invariants are deliberately wrong. The validation section distinguishes those witness checks from the correct implementations.

**Self-check Q38:** Which tests distinguish a wrong heap direction from a correct retained-set algorithm?

**Self-check Q39:** Why are comparator ties, object equality, and FIFO stability separate concepts?

**Self-check Q40:** Why do invalid heap assumptions often survive tests with only one element or one child?

## 19. Pattern recognition and derivation

### 19.1 Five transferable patterns

| Pattern | Recognition clue | Root meaning and invariant | Main risk | Prefer an alternative when |
| --- | --- | --- | --- | --- |
| A: Bounded selection | Need only the best k | Root is weakest retained winner; processed-prefix best-k multiset | Wrong direction or unbounded storage | Full order is required, or k=1 |
| B: Streaming rank | A fixed rank after each arrival | Retain largest k; answer only after k exist | Returning a short-prefix minimum as kth | Arbitrary ranks/deletions are required |
| C: Repeated-priority simulation | The next step acts on the current extreme | Heap multiset equals current simulation state | Unproved update/termination or ignored event count | One decision or a tiny bounded domain suffices |
| D: Ordered-stream frontier | Many inputs are already sorted | One first-unread item per nonexhausted source | Missing source tracking or unsorted inputs | Only two sources need a direct merge |
| E: Two balanced heaps — optional | Online median | Balanced sizes and all lower<=all upper | Correct sizes with wrong partition, overflow in average | Static sorting or richer rank structures fit better |

For an unfamiliar problem, first state the output and workload. Ask whether it repeatedly needs an extreme, receives online values, retains only the best k, or combines sorted frontiers. Then choose the element that must be removed next. Write the retained-set/frontier/state invariant before code, and justify why rejected information is unnecessary.

If priorities change, define how entries are updated. If availability changes, separate eligibility from ranking. If all values must be sorted, account for the full ordering cost. If memory is constrained, bound the actual retained state, stale entries, capacity, and output rather than just the number of local variables.

### 19.2 Recognition scenarios

Choose an approach and justify preprocessing, updates, memory, and output before opening the concise checkpoints in Section 20.

| Scenario | Requirement to interpret |
| --- | --- |
| R1 | Find one greatest value in a one-off unsorted batch |
| R2 | Produce every transaction in sorted amount order |
| R3 | Report the fifth-largest occurrence after every arrival |
| R4 | Keep the best twenty records from millions of arrivals |
| R5 | Retrieve a payload by exact identifier |
| R6 | Repeated predecessor and key-range queries with updates |
| R7 | Find a key in an existing sorted array |
| R8 | Combine twelve sorted logs into one ordered output |
| R9 | Queued task priorities can be revised repeatedly |
| R10 | Maintain the median after every integer arrival |
| R11 | Find the most frequent values from a tiny bounded integer domain |
| R12 | Run unit tasks with unavailable periods after use |

A problem can combine patterns: frequencies plus bounded selection, a priority heap plus release queue, or a version map plus immutable priority entries. State the meaning of every combined container and how an update preserves their relationship.

## 20. Self-assessment answer key

Answer Q1–Q40 aloud without notes, draw the requested structure, and derive one cost rather than naming a remembered bound. These concise checkpoints support theory assessment; independent implementation and unfamiliar transfer need separate evidence.

<details>
<summary>Q1–Q13: shape and repair</summary>

| Question | Concise answer |
| --- | --- |
| Q1 | Priority queue is an operation/ordering abstraction; binary heap is a complete ordered representation that implements it. |
| Q2 | Only parent-child route order is constrained; unrelated branches and array positions need not be globally sorted. |
| Q3 | Left-to-right complete filling occupies exactly the first n breadth-first positions. |
| Q4 | −1,−1,0,1 respectively, using n/2−1. |
| Q5 | The guard proves a child exists and keeps int index arithmetic within representable bounds. |
| Q6 | Route transitivity makes root extreme; an arbitrary key may occur anywhere else, requiring a scan. |
| Q7 | Append may break only the new leaf's parent edge; root replacement may break only the moving replacement's downward edges. |
| Q8 | All old edges remain ordered and the new leaf has no child subtree to repair. |
| Q9 | Tied values need not swap; max-heap comparisons reverse the priority direction while shape stays unchanged. |
| Q10 | Moving the larger child up can leave it greater than its smaller sibling. |
| Q11 | Empty has no root; singleton removal needs no repair; a lone left child must not trigger a right-child read. |
| Q12 | Leaves already satisfy heap order; only internal positions can need downward repair. |
| Q13 | Summed possible downward heights are linear; repeated insertion can repeatedly climb growing logarithmic routes. |

</details>

<details>
<summary>Q14–Q25: Java ordering and bounded state</summary>

| Question | Concise answer |
| --- | --- |
| Q14 | Both return null on empty; unboxing null into int throws NPE. |
| Q15 | remove() extracts head with heap repair; remove(Object) first searches for an equal value, which is linear. |
| Q16 | Heap storage is only partially ordered; poll a comparator-preserving copy or sort a copied collection. |
| Q17 | Field changes do not invoke queue repair; use explicit removal/reinsertion, rebuilding, or a controlled version strategy. |
| Q18 | Overflow can flip the sign; compare primitives directly or use Integer.compare/Long.compare. |
| Q19 | Reverse the primary comparator before chaining ascending tie keys. |
| Q20 | Widening prevents int multiplication overflow; the mathematical sum must still fit long under the stated domain. |
| Q21 | Its minimum is the weakest retained winner and the next candidate to evict. |
| Q22 | In append-only best-k selection, a value already dominated by k retained winners cannot become necessary when more values arrive. |
| Q23 | k=1 still needs scanning; allocation/build and sorted extraction/output must be counted, using a nonzero logarithmic convention. |
| Q24 | No kth occurrence exists yet; the current minimum has a different rank. |
| Q25 | Static heaps are local and drained/discarded; a stream retains its candidates across calls and may grow its backing storage. |

</details>

<details>
<summary>Q26–Q40: simulation, frontiers, costs, and mistakes</summary>

| Question | Concise answer |
| --- | --- |
| Q26 | Updating a selected key changes its order relative to others; the next extreme can be different. |
| Q27 | Each such round reduces live item count by at least one, yielding at most n−1 rounds. |
| Q28 | Every later value in a sorted source is at least its first unread value. |
| Q29 | Extraction must advance exactly the source whose head was removed. |
| Q30 | Scanning k supplied headers still costs O(k), even if N is zero or only one source is nonempty. |
| Q31 | A high-ranked task may be legally unavailable; eligibility needs its own release-time state. |
| Q32 | A jump avoids visiting every idle instant; materializing the full timeline still requires its full output work. |
| Q33 | Balance locates the rank positions only if all lower values also precede all upper values. |
| Q34 | Computing future medians retains both halves of every observation, not just a fixed tail of k winners. |
| Q35 | Backing-array growth can allocate and copy O(n) references in that call. |
| Q36 | Working heaps are temporary, streaming heaps persist, capacity may exceed occupancy, and output remains additional required data. |
| Q37 | One extreme: scan; full order: sort; exact payload: hash map; lookup in existing sorted input: binary search. |
| Q38 | Test mixed extremes with k=1, k=n, middle k, ties, and compare the retained multiset/rank against sorting. |
| Q39 | Priority equality does not identify the same object or promise arrival order; each is a separate policy/API relation. |
| Q40 | Such fixtures never force competing-child selection, threshold replacement, or meaningful tie/ordering decisions. |

</details>

<details>
<summary>Recognition checkpoints R1–R12</summary>

R1: scan. R2: sort unless an existing sorted-stream merge applies. R3: bounded min-heap after enough observations. R4: bounded selection with a complete ranking/tie key. R5: HashMap rather than priority structure. R6: balanced ordered map/set with an explicit duplicate policy. R7: binary search. R8: source-aware min-heap frontier. R9: explicit key-update strategy; consider immutable versions or an indexed structure and bound stale state. R10: optional two balanced heaps. R11: frequency/bucket counting can exploit the small domain; compare bounded heaps for a broader domain. R12: priority among available tasks plus release/cooldown state, or a proved counting approach when only the model-specific total is needed.

</details>

## 21. Key takeaways and readiness

Complete shape supplies short repair routes; heap order supplies an extreme root. Array positions encode structure, not BST partitions or full sorting. Sift-up/down fix a localized violation, and bottom-up heapify is linear because most nodes cannot move far.

For a new problem, identify the root's job: next item, weakest winner, source frontier, or median boundary. Then state the invariant that makes it useful. Comparator validity, stable queued keys, numeric ranges, duplicate semantics, and output order are part of correctness.

Use the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) and the Heap-specific roadmap demonstration:

- Explain complete shape, array boundaries, min/max order, and the distinction from BSTs without notes.
- Hand-execute both insertion directions, root removal, a one-child case, and bottom-up construction.
- Implement the core small mechanics and size-k selection from a blank editor; test ties, empty/singleton cases, k=1, and k=n.
- Choose a heap direction for an unfamiliar best-k or stream task and prove the retained-set invariant.
- Use PriorityQueue with safe comparator composition, stable keys, valid null/empty handling, and explicit output-order requirements.
- Explain merging-frontier correctness and why sorted-source assumptions matter.
- Derive linear heapify and the total costs of selection, simulation, streaming, and merging, including backing growth, comparator work, and output.
- Defend scanning, sorting, hashing, binary search, or ordered trees when a heap adds no useful operation.
- Repeat independent success on changed variants across separate sessions.

Explaining concepts supports **T**; tested blank-editor core drills support **I**; independent or cold Essential solves support **P**; unfamiliar transfer and communication support **R**. Reading a prepared guide earns none automatically. The optional median technique and Hard problem do not block ordinary readiness.

Keep attempt history and dated evidence in the [roadmap Heap section](./DSA_Interview_Roadmap.md#9-heap--priority-queue), following its understand→implement→practice→explain→review→validate loop and flexible [1/3/7/14-day reviews](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition). No progress is prefilled here.

## 22. References and next steps

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Heap / Priority Queue](./DSA_Interview_Roadmap.md#9-heap--priority-queue), [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates).
- [Trees Essential Concepts](./Trees/Trees_Essential_Concepts.md).
- [Java Arrays](./Java_Array.md), [Collections/List](./Java_List_ArrayList_LinkedList.md), [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Arrays & Hashing Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md).
- [Linked List Essential Concepts](./Linked_List/Linked_List_Essential_Concepts.md), especially [two-source merging](./Linked_List/Linked_List_Essential_Concepts.md#11-merging-sorted-chains).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), including [PriorityQueue costs](./Complexity/Complexity_Analysis_and_Foundations.md#148-priorityqueue).

### Java 21 primary sources

The version-21 Oracle documentation was checked through Context7. API contracts and implementation details are distinguished where growth and construction are discussed.

- [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html).
- [Queue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Queue.html).
- [Comparator API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html).
- [OpenJDK 21 PriorityQueue source](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/PriorityQueue.java): backing growth, collection construction, and repair paths. These are implementation details, not a promised fixed capacity-growth factor for every JVM.

### Curated practice in exact roadmap order

| Priority | Official problem | Difficulty | Learning objective |
| --- | --- | --- | --- |
| E | [703. Kth Largest Element in a Stream](https://leetcode.com/problems/kth-largest-element-in-a-stream/) | Easy | Bounded retained-set invariant across arrivals |
| E | [1046. Last Stone Weight](https://leetcode.com/problems/last-stone-weight/) | Easy | Largest-first extraction and changing priorities |
| E | [973. K Closest Points to Origin](https://leetcode.com/problems/k-closest-points-to-origin/) | Medium | Selection threshold, numeric distance range, ties |
| A | [215. Kth Largest Element in an Array](https://leetcode.com/problems/kth-largest-element-in-an-array/) | Medium | Static rank, sorting and optional Quickselect comparison |
| A | [621. Task Scheduler](https://leetcode.com/problems/task-scheduler/) | Medium | Availability/cooldown simulation or proved counting |
| C | [295. Find Median from Data Stream](https://leetcode.com/problems/find-median-from-data-stream/) | Hard | Optional two-heap order and size invariants |

E/A/C are the repository's learning priorities; platform difficulty is separate. The official titles/numbers/difficulties were checked **2026-10-29**. Platform constraints and signatures may differ from the deliberately explicit contracts in these original teaching examples.

Start with a drawn array heap and the core implementation drills. Attempt Essential problems independently, use Additional practice to close gaps, and leave median maintenance optional. The separate Linked List C problem Merge k Sorted Lists becomes relevant after this frontier lesson; it is not an extra Heap practice entry.

### Author validation

Completed **2026-10-29**. All **19 Java blocks**, containing **33 methods** plus the custom heap, stream classes, and immutable record types, were assembled and compiled with Java 21 using `--release 21 -Xlint:all -Werror`.

Runtime validation passed **1,127,401 assertions**. It covered **1,178 array cases**, including every array of lengths 0..6 over {-1,0,1}, explicit int extremes/ties, and deterministic randomized arrays. Independent sorting oracles checked insertion/extraction, min/max order, active-prefix boundaries, input preservation, **20,084 selection cases**, **195,320 streaming-prefix checks**, and **8,864 median-prefix checks**. Median tests checked both partition order and size balance, not only the returned value.

Further checks covered **8,424 ordered-stream merge cases**, **2,387 repeated-priority simulations**, and **18,666 comparator checks**. Exact wide-arithmetic oracles checked distance keys and primitive comparison signs; the chained comparator was tested for direction and transitivity. Empty/null/rejection behavior, duplicate occurrences, independent instances, fixed capacity, copy ownership, and **100000-element** heap/stream cases were exercised. The deliberately faulty snippets and mutable-priority example reproduced their documented failures, with **seven witness/control checks**.

Instrumentation of the published algorithms passed **9,238 additional assertions**, including the linear summed-height bound on **3,076 heapify inputs** and the ancestor-height sum for decreasing repeated insertions. **60 recorded Java events** supported direct comparisons of the published states. **64 rows across 11 dry-run tables**, plus **seven heap diagrams containing 13 tree views**, passed a further **239 trace/diagram assertions**. Scheduling states were checked by a separate availability simulator; exact state search independently confirmed elapsed length seven for the example. These are logical-operation and state checks, not hardware timing or exact JVM heap-byte measurements.

The content audit checked all **22 sections**, **40 questions with answer entries**, **12 recognition scenarios with checkpoints**, and the **six exact roadmap practice entries** with E/E/E/A/A/C priorities. All **57 local links**, including heading anchors, were verified. SHA-256 comparison confirmed all **34 pre-existing guide files** remained unchanged. Only **Heap_Priority_Queue_Essential_Concepts.md** was created in DSA_Guide; validation artifacts stayed outside the repository. No prerequisite reference was missing, and no validation issue remains unresolved.

