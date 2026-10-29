# Heap / Priority Queue — Exercises (Java 21)

Practice deriving the root's meaning, proving what retained state represents, and deciding when priority access is worth maintaining. This workbook complements the [concepts guide](./Heap_Priority_Queue_Essential_Concepts.md); it does not supply exercise solutions.

## Contents

- [1. Purpose and working conventions](#1-purpose-and-working-conventions)
- [2. Problem-solving workflow](#2-problem-solving-workflow)
- [3. Level 1 — Heap fundamentals](#3-level-1--heap-fundamentals)
- [4. Level 2 — Java PriorityQueue](#4-level-2--java-priorityqueue)
- [5. Level 3 — Core heap patterns](#5-level-3--core-heap-patterns)
- [6. Level 4 — Curated practice](#6-level-4--curated-practice)
- [7. Level 5 — Mixed interview challenges](#7-level-5--mixed-interview-challenges)
- [8. Complexity laboratory](#8-complexity-laboratory)
- [9. Debugging laboratory](#9-debugging-laboratory)
- [10. Optional two-heap median workshop](#10-optional-two-heap-median-workshop)
- [11. Mock interviews](#11-mock-interviews)
- [12. Revision and spaced repetition](#12-revision-and-spaced-repetition)
- [13. Completion and readiness](#13-completion-and-readiness)
- [14. References and author validation](#14-references-and-author-validation)

## 1. Purpose and working conventions

The [Phase 3 Heap section](./DSA_Interview_Roadmap.md#9-heap--priority-queue) determines scope and curated priorities. Follow its [learning loop](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy): understand, implement, practice, explain, review, validate. Owning completed notes is not evidence of mastery.

There are **five progressive levels** and **49 original exercises**: **19 coding exercises**, **8 traces**, **2 Java reasoning drills**, **7 complexity investigations**, **10 debugging challenges**, **1 optional median workshop**, and **2 mock interviews**. The **six curated problems** are separate. The median workshop has no required full implementation and does not increase the coding count.

### Prerequisite diagnostic

Before Level 1, draw a complete tree, explain active size versus array capacity, and use indexes without reading beyond a half-open range. Before Level 2, explain references, equality, immutable records, and comparator signs. Before Level 3, independently maintain a processed-prefix invariant and distinguish auxiliary space from output.

| Reuse | Demonstrate before relying on it |
| --- | --- |
| [Trees: shape properties](./Trees/Trees_Essential_Concepts.md#3-binary-tree-types-and-properties) | Completeness differs from full/perfect shape and BST ordering |
| [Java Arrays](./Java_Array.md#3-indexes-length-and-traversal), [copies](./Java_Array.md#copying-arrays) | Valid indexes, shallow references, fresh outputs, permitted mutation |
| [Collections/List](./Java_List_ArrayList_LinkedList.md#1-collections-framework-and-list) | Interface versus ordering policy; duplicates and object references |
| [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md#frequency-counting), [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) | Counts differ from membership; equality differs from priority |
| [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model) | State meaning, fixed-domain counting, expected hash costs |
| [Linked List merging](./Linked_List/Linked_List_Essential_Concepts.md#11-merging-sorted-chains) | Choose from exposed sorted heads; preserve input ownership |
| [Binary Search invariants](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) | State what has been eliminated and what remains possible |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#148-priorityqueue) | Amortized growth, operation counts, capacity, and peak live storage |

### Shared contracts

These rules are part of **every original coding exercise**, with local overrides stated explicitly.

- Java 21 standard library, single-threaded calls, feasible allocations. Arrays contain 0..100000 entries unless a smaller bound is stated. Input integers may include negatives, zero, duplicates, MIN_VALUE, and MAX_VALUE unless a local domain says otherwise.
- A required null array, row, record, or operation is rejected with **NullPointerException (NPE)**. A numeric parameter outside an explicitly validated range is rejected with **IllegalArgumentException (IAE)**. Validate before mutating caller-owned state. For several invalid fields, any applicable stated exception is acceptable unless an order is specified.
- Upper size limits and properties explicitly labelled **preconditions** are trusted; do not add a linear validation pass to a logarithmic drill. Sorted-source preconditions are not rescanned. All other stated domain checks are required.
- Inputs remain unchanged except F2/F3, whose permitted active-prefix mutations are explicit. New arrays/lists are fresh containers; immutable records may be shared. Aliases in read-only inputs are legal. Calls are repeatable and instances are independent.
- Duplicates are occurrences, not distinct values. Each exercise states its tie and output-order policies. No implied FIFO order among comparator ties. Finite batch inputs have no hidden updates; online objects have append-only calls and no deletions.
- Static selection allows **0<=k<=n**, including k=1 and k=n, unless stated otherwise. Out-of-range k throws IAE. Stream rank instead requires positive k and can report insufficient observations. A k=0 selection is not a kth-rank query.
- A heap display written as a sorted multiset is only a display. It is **not** an assertion about PriorityQueue iteration or its backing array. Exact primitive repair traces are labelled **array order**.
- Empty complete-tree node-height is 0; nonempty height is floor(log2 n)+1. Depth and sift distances count edges.
- Auxiliary space excludes the supplied input and required returned output, but includes temporary copies, heaps, maps, frames, and sorting workspace. Report persistent stream state separately without pretending it is free. Report backing capacity if it exceeds current occupancy.

### Java starter convention

Create your own scratch file outside this guide. Use `import java.util.*;` and a class named `HeapWorkbook`. **Every Java fence below is a member of that class**; combine them in document order. Types are intentionally distinct. All starter methods throw UnsupportedOperationException until implemented; these are placeholders, not intended invalid-input behavior. Do not copy implementations from the concepts guide during a cold attempt.

Analysis/debugging methods are executable specimens, not starter solutions. Some analysis wrappers call named TODO helpers whose contracts are specified; implement those prerequisites or use a test fixture when measuring wrapper behavior. No full custom collection library is required.

### One record per exercise

Every exercise has an unchecked record. Fill **last attempt date, result, minutes, next review, and one concrete mistake/lesson**. Codes: **I** independently solved and explained; **H** conceptual hint used; **S** substantial solution review or unfinished; **—** not attempted. A completed attempt with H/S is still not independent mastery.

The roadmap owns topic-level [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates). For curated problems, use the workbook record as a compact pointer to the roadmap's canonical Log after attempting; keep dated history and review scheduling in that one roadmap entry. Do not maintain two full histories. Original-exercise histories stay beside their exercise. Nothing is pre-completed.

## 2. Problem-solving workflow

Before opening hints, write the output contract and a simple baseline. Answer these questions in order:

1. Does the workload require repeated next-priority access, only k retained candidates, online answers, or an ordered-stream frontier? Could scanning, sorting, counting, a map, or binary search suffice?
2. What must the **next removal** mean? Choose the comparator from that meaning, not from a word such as “largest” in the title.
3. What exactly does each container represent? Write a multiset/processed-prefix invariant, not only “the queue is a heap.”
4. State initialization, preservation for each branch, progress/termination, and the final relationship to the requested result.
5. Explain ties, stable priorities, identity, empty state, valid k, numeric bounds, and whether input/output order matters.
6. Count preprocessing, allocations, offers/polls, equality searches, output sorting/materialization, and idle intervals. Define n, k, m and any extra variables before using them.

**Every coding completion** requires: compile, run all listed tests, add at least two adversarial cases, verify permitted mutation/ownership, compare small random inputs against an independent simple oracle, explain correctness aloud, and derive total time and auxiliary/persistent/output space. A passing example is not a proof. Prefer sorted-copy or scan oracles over reusing the same heap algorithm.

For bounded selection, show why discarded candidates cannot later become necessary in the stated append-only model. Prove size never exceeds k if that is the contract; an offer-then-trim method temporarily reaches k+1 and must not be described otherwise. For simulations, describe a decreasing measure or a finite event counter. For scheduling, prove legality separately from any optimality claim.

| Roadmap objective | Evidence here |
| --- | --- |
| Array shape, indexes, order, height | T1–T3, F1–F2 |
| Local downward repair and linear build | T4, F3, C2 |
| Java APIs, stable priorities, safe comparisons | J1–J2, Q1–Q2, D3–D4, D7–D8 |
| Bounded selection and ties | P1–P3, T5, C3 |
| Online kth rank | P4, T6, D5–D6 |
| Changing-priority simulations | P5–P6, X5, C6 |
| Ordered frontiers | P7–P8, T7, C5 |
| Availability and cooldown | P9, T8, D10; Additional practice |
| Curated independent practice | L1–L6, exact E/E/E/A/A/C order |
| Recognition and alternatives | X1–X5, M1–M2 |
| Optional median partition | O1, optional L6 |

Use O(log(k+1)) where k=1 is allowed. Fixed primitive comparison costs are assumed unless C is supplied. Java PriorityQueue growth makes insertion bounds amortized over ordinary growth; one resize may copy a linear number of references. Pre-sizing can remove that resize from updates but costs allocation up front.


## 3. Level 1 — Heap fundamentals

Read [array relationships](./Heap_Priority_Queue_Essential_Concepts.md#3-complete-trees-and-array-indexes), [sift-up](./Heap_Priority_Queue_Essential_Concepts.md#5-sift-up-and-insertion), and [sift-down](./Heap_Priority_Queue_Essential_Concepts.md#6-sift-down-and-root-removal), then close the implementation examples.

### T1 — Shape, edges, and ordering

**Trace · Easy.** For active array [3,8,5,11,9,7], draw index:value nodes, list each parent/left/right index, identify every leaf, and classify min-heap, max-heap, and strict BST ordering independently. State node-height and maximum edge depth. Capacity is ten; positions 6..9 contain arbitrary stale values and must not enter the drawing.

```text
Shape to label (six occupied positions):
             [0]
           /     \
         [1]     [2]
        /   \    /
      [3]  [4] [5]
```

Test your rules on [], [4], [4,4,4], [9,7,8,1], and [1,4,2,0]. Name a violating edge rather than saying “looks wrong.” Then draw a root with a right child but no left child: explain why a valid comparison alone cannot make that shape a binary heap.

**Checkpoints:** The main array is a min-heap, not a max-heap or strict BST; height 3, maximum depth 2, leaves at 3,4,5. Empty/singleton arrays satisfy both numeric order predicates; [4,4,4] satisfies both; [9,7,8,1] is max-only; [1,4,2,0] is neither. The root has no parent even though Java evaluates (0-1)/2 as 0.

**Completion:** supply the full index table and explain why sibling order is irrelevant. No code is required.

- [ ] **T1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T2 — Insert along an ancestor route

**Trace · Easy.** Start with min-heap [4,9,6,12,10,8], append 2, and use swap-based sift-up: swap only for a strict parent>child violation. Repeat on max-heap [20,14,18,3,7] by appending 17 and reversing the order test. In a separate run from that same original max-heap, append 22 so that upward repair is required. Draw before/after trees.

Record initial array, appended leaf index, every parent comparison, each swap, final array, and why all other edges remain ordered. Additional fixtures: insert into empty; append an equal root value; insert MIN into a one-element [MAX] min-heap.

**Checkpoints:** min result [2,9,4,12,10,8,6] with two swaps; max result for 17 is [20,14,18,3,7,17] with zero swaps; max result for 22 is [22,14,20,3,7,18] with two swaps. Equal values do not require a swap. Singleton [MAX] plus MIN becomes [MIN,MAX].

**Completion:** give exact intermediate arrays and a strictly decreasing progress measure.

- [ ] **T2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T3 — Remove the root without a gap

**Trace · Medium.** Poll once from min-heap [1,4,2,7,6,5,3,9,8]. Save the root, move the last occupied value to root, decrement active size, then sift down using the smaller existing child; choose left on equal child keys and stop on parent<=child.

```text
Initial active tree for T3:
             1
           /   \
          4     2
         / \   / \
        7   6 5   3
       / \
      9   8
```

Use this worksheet, adding rows as needed:

| Stage | Active size | Moving index | Existing children | Chosen comparison | Swap | Exact active array |
| --- | --- | --- | --- | --- | --- | --- |
| Initial | 9 | — | — | — | — | [1,4,2,7,6,5,3,9,8] |
| Replacement | — | — | — | — | — | — |
| Repair | — | — | — | — | — | — |

Also poll [1,2,3,4,5], [7], and an empty Java PriorityQueue. Explain which case has one left child but no right child. The manual model has no root to remove when size=0; state the API policy before executing a removal.

**Checkpoints:** first extraction returns 1, leaves size 8 and [2,4,3,7,6,5,8,9]. Second fixture returns 1 and leaves [2,4,3,5]. Singleton leaves []; Java empty poll returns null.

**Completion:** comparisons, movements, exact final arrays, multiset preservation, and termination. Explain why a fixed “always left” choice is insufficient.

<details>
<summary>Hint 1</summary>

The replacement can violate order only on a downward route.

</details>

<details>
<summary>Hint 2</summary>

A parent must precede both children, not merely the first inspected child.

</details>

<details>
<summary>Hint 3</summary>

Check right<size before reading it; decrease active size before repair.

</details>

- [ ] **T3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T4 — Build from the bottom

**Trace · Medium.** Apply bottom-up min-heap construction to [10,3,8,2,7,1,6,4]. Internal indexes run backward from n/2-1. Each local sift chooses the smaller child, left on ties, and stops at <=. Draw the initial tree and final tree; record the array after every swap and after each internal-node call.

```text
Initial complete tree for T4 (ordering not yet repaired):
             10
           /    \
          3      8
         / \    / \
        2   7  1   6
       /
      4
```

Repeat with [], [5], [3,2,1], and [2,2,2,2]. Give a decreasing input family on which repeated insertion makes many upward moves. Explain why leaves are already heaps and why “n nodes each cost log n” is a loose bound for bottom-up construction.

**Checkpoints:** main last internal index 3; resulting exact array [1,2,6,3,7,8,10,4]. For []/[5], no internal-node calls. [3,2,1] becomes [1,2,3]; equal input stays equal. These are checks, not the missing comparison trace.

**Completion:** derive a linear sum using counts of nodes able to descend at least j levels; distinguish n=0/1. If using a different correct build algorithm, validate heap order and multiset instead of demanding this exact layout, and label that change.

<details>
<summary>Hint 1</summary>

Leaves cost no downward movement.

</details>

<details>
<summary>Hint 2</summary>

Group possible work by distance from the leaves.

</details>

<details>
<summary>Hint 3</summary>

Compare the sum of floor(n/2^j), j>=1, with a geometric series.

</details>

- [ ] **T4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.
### F1 — Audit an active heap prefix

**Coding · Easy.** Separate shape-by-prefix from numeric edge order.

**Prerequisites:** T1, array bounds, direct comparisons.

**Problem, input, and output:** Given backing array a, active size, and minOrder, return whether every active parent-child edge obeys min order when minOrder is true and max order otherwise. Ignore a[size..a.length).

**Contract and constraints:** Shared contracts apply. Validate 0<=size<=a.length. No mutation; return one boolean. Empty and singleton prefixes are valid under either order; no sorting requirement.

```java
public static boolean isHeap(int[] a, int size, boolean minOrder) {
    throw new UnsupportedOperationException("TODO F1");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [3,8,5,11,9,7,-100], size=6, min=true | true |
| same array, size=7, min=true | false |
| [9,7,8,1],4,false | true |
| [1,4,2,0],4,true | false |
| [],0,true | true |
| [4,4,4],3,false | true |
| [MIN,MAX],2,true | true |
| [1],2,true | IAE |
| null,0,true | NPE |

**Target:** O(size+1) worst-case time, O(1) auxiliary; early rejection may be faster.

**Completion:** Identify a first violated edge in a failing trace; prove all edges are covered once. Test both directions and untouched inactive capacity. Apply the shared validation and explanation checklist in Section 2.

- [ ] **F1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### F2 — Repair one appended leaf

**Coding · Easy.** Implement a small sift-up drill without building a collection class.

**Prerequisites:** T2, F1, active-prefix ownership.

**Problem, input, and output:** a[0..size-1) is already a min-heap; a[size-1] is the newly appended value. Repair in place by strict parent>child swaps and return its final index.

**Contract and constraints:** Require nonnull a and validate 1<=size<=a.length. The prior prefix being a heap is a trusted precondition. Only active slots may change. Equal keys stop movement. The exact swap policy makes the final array and returned index deterministic.

```java
public static int repairAppend(int[] a, int size) {
    throw new UnsupportedOperationException("TODO F2");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [4,9,6,12,10,8,2,99],7 | index 0; array [2,9,4,12,10,8,6,99] |
| [2,2],2 | index 1; [2,2] |
| [7],1 | index 0; [7] |
| [MAX,MIN],2 | index 0; [MIN,MAX] |
| [],0 | IAE |
| [1],2 | IAE |
| null,1 | NPE |

**Target:** O(log(size+1)) worst-case time, O(1) auxiliary. Do not scan the trusted prior heap.

**Completion:** Explain the one potentially invalid route and verify the untouched suffix byte-for-byte; do not call PriorityQueue. Apply the shared validation and explanation checklist in Section 2.

- [ ] **F2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### F3 — Construct a heap in caller storage

**Coding · Medium.** Implement local downward repair and a linear bottom-up build.

**Prerequisites:** T3–T4, F1, complete-tree height.

**Problem, input, and output:** Convert a[0..size) into a min-heap in place using the exact T4 algorithm. Provide a local down helper that assumes its child subtrees are already heaps. Return no value.

**Contract and constraints:** Validate nonnull and 0<=size<=a.length in buildMin. Internal down calls trust 0<=i<size and child-subtree order; down has no public validation duty. Ignore and preserve inactive slots. Keep duplicates and the entire active multiset. Size=0/1 is legal. No PriorityQueue or sorting.

```java
public static void buildMin(int[] a, int size) {
    throw new UnsupportedOperationException("TODO F3 build");
}
static void down(int[] a, int size, int i) {
    throw new UnsupportedOperationException("TODO F3 local repair");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [10,3,8,2,7,1,6,4,99],8 | [1,2,6,3,7,8,10,4,99] |
| [3,2,1],3 | [1,2,3] |
| [5],1 | [5] |
| [],0 | [] |
| [2,2,2,2],4 | [2,2,2,2] |
| [0,MIN,MAX],3 | [MIN,0,MAX] |
| [1],-1 | IAE |
| null,0 | NPE |

**Target:** O(size+1) worst-case total time, O(1) auxiliary with iterative local repair.

**Completion:** Prove that children have been repaired before their parent. Also directly test down on [8,5,2] and on a node with only a left child; explain why this helper alone cannot repair an arbitrary ancestor violation. Apply the shared validation and explanation checklist in Section 2.

<details>
<summary>Hint 1</summary>

Start where a child can actually exist.

</details>

<details>
<summary>Hint 2</summary>

The backward loop establishes an invariant about already processed subtrees.

</details>

<details>
<summary>Hint 3</summary>

Guard child arithmetic by proving the current node is internal; use the smaller existing child.

</details>

- [ ] **F3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.



## 4. Level 2 — Java PriorityQueue

Use the [Java API and stable-key discussion](./Heap_Priority_Queue_Essential_Concepts.md#8-java-21-priorityqueue) and [comparator safety](./Heap_Priority_Queue_Essential_Concepts.md#9-comparators-and-numeric-safety) as references after an initial attempt. Natural integer order puts the smallest value first; reverse order changes the priority, not the shape invariant.

### Q1 — Observe the queue contract

**Reasoning · Easy.** Run this specimen after predicting every scalar result. The input is exactly the offers shown. The returned list permits null; replacing it with List.of is not equivalent.

```java
static List<Object> queueObservation() {
    PriorityQueue<Integer> q = new PriorityQueue<>();
    List<Object> log = new ArrayList<>();
    log.add(q.peek());
    log.add(q.poll());
    q.offer(6); q.offer(2); q.offer(6);
    log.add(q.peek());
    log.add(q.remove(Integer.valueOf(6)));
    log.add(q.size());
    log.add(q.contains(6));
    log.add(q.poll());
    log.add(q.poll());
    log.add(q.poll());
    return log;
}
```

Explain poll versus peek, remove() versus remove(Object), duplicate occurrences, equality-based searches, and why initial capacity is not a maximum size. On a fresh queue, separately test element(), remove(), offer(null), and `int x = q.peek();`. Do not let the first thrown exception prevent the remaining tests.

For the separate offers [1,5,2], inspect iteration and then drain a copy. State which observation is API-guaranteed and which is implementation-dependent. Reject a test that insists on one particular iterator sequence.

**Checks:** the scalar log must be [null,null,2,true,2,true,2,6,null]. Empty throwing forms give NoSuchElementException; offering null and unboxing empty peek give NPE. Draining the numeric example gives [1,2,5]; no exact iterator order is required.

**Completion:** make five small independent assertions and explain why a PriorityQueue is not a sorted List. Compare an iterator visit with the next-removal contract.

- [ ] **Q1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### Q2 — Read a comparator as a policy

**Reasoning · Medium.** Determine the required sign for each pair before executing any code: (MIN,1), (MAX,-1), (Long.MIN_VALUE,1L), and (Long.MAX_VALUE,-1L). Test ascending and descending order. Audit these dangerous expressions:

- Integer ascending: `a-b`.
- Long ascending: `(int)(a-b)`.
- Descending int key: `Integer.compare(-a,-b)`.
- Severity descending, time ascending: reverse the entire chained comparator.
- Distance: `(long)(x*x+y*y)`, with int coordinates.

Construct an actual failing pair for **each** expression, state the violated policy, and replace it in your own scratch comparator using Integer.compare, Long.compare, comparingInt, or comparingLong. Check sign reversal for swapped arguments and transitivity across triples. Do not demand compare returns exactly -1 or 1; only its sign is the contract.

**Tests/checks:** ascending MIN before 1 and MAX after -1; analogous long endpoints; descending MAX before MIN. For equal severity, time 2 must precede time 9. The point (50000,0) has squared distance 2500000000, and (10^9,10^9) has 2000000000000000000. Explain why even widening operands is insufficient for two unrestricted int MIN coordinates.

**Completion:** give five witnesses, safe comparator expressions, a domain proof for the distance sum, and one triple test. Distinguish comparator ties, equals, and arrival stability.

<details>
<summary>Hint 1</summary>

The comparator sign must match the mathematical relation even at numeric endpoints.

</details>

<details>
<summary>Hint 2</summary>

Reversing a chain reverses every tie key already in that chain.

</details>

<details>
<summary>Hint 3</summary>

Promote before arithmetic and prove the largest possible sum; do not repair overflow after it occurs.

</details>

- [ ] **Q2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.
### J1 — Replay a priority queue command tape

**Coding · Easy.** Use offers, observations, removals, duplicates, and empty state through a precise API.

**Prerequisites:** Q1, natural/reverse ordering, lists permitting null.

**Problem, input, and output:** Replay commands on a fresh queue. maxFirst selects numeric descending priority when true, ascending otherwise. Emit one Object per command: OFFER emits true; PEEK/POLL emit Integer or null; REMOVE emits whether one equal value was removed; SIZE emits the current int size.

**Contract and constraints:** The tape is finite and has at most 100000 commands. Validate the array, each command and each kind as nonnull. value is used only for OFFER/REMOVE. Return a fresh List<Object> in command order that permits null. Inputs unchanged; duplicates retained; no keys change. REMOVE means remove(Object), never an index. No invalid enum strings exist because Kind is an enum.

```java
enum Kind { OFFER, PEEK, POLL, REMOVE, SIZE }
record Command(Kind kind, int value) {}
public static List<Object> replay(Command[] tape, boolean maxFirst) {
    throw new UnsupportedOperationException("TODO J1");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| min: PEEK, OFFER(5), OFFER(2), OFFER(5), SIZE, REMOVE(5), POLL, POLL, POLL | [null,true,true,true,3,true,2,5,null] |
| max: OFFER(-4), OFFER(0), OFFER(-4), PEEK, REMOVE(9), POLL, SIZE | [true,true,true,0,false,0,2] |
| empty tape, either order | [] |
| min: OFFER(MAX), OFFER(MIN), POLL, POLL | [true,true,MIN,MAX] |
| null tape | NPE |
| [null command] | NPE |
| Command(null,0) | NPE |

**Target:** With m commands, r REMOVE commands, and maximum occupancy M: O(m log(M+1)+r*M+1) time as a safe bound, O(M+1) auxiliary backing storage, O(m) returned log. Explain tighter costs for only PEEK/SIZE and amortized growth.

**Completion:** Use both queue directions; log boxed values safely; check remaining duplicate after REMOVE. Explain linear arbitrary removal. Apply the shared validation and explanation checklist in Section 2.

- [ ] **J1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### J2 — Dispatch immutable tickets

**Coding · Medium.** Translate a multi-field priority policy into safe comparator composition.

**Prerequisites:** Q2, records, comparator chaining.

**Problem, input, and output:** Return all ticket IDs in dispatch order: greater severity first, then smaller submittedAt, then smaller id. Use a PriorityQueue in this drill; later compare a one-off sorted copy.

**Contract and constraints:** Ticket[] and elements nonnull. IDs are unique (trusted precondition); every int ID/severity and every long timestamp are valid, including negatives/endpoints. Duplicated severity/time is legal. Records immutable, priorities fixed, finite batch, deterministic total order. Input unchanged. Return fresh int[]. No k parameter.

```java
record Ticket(int id, int severity, long submittedAt) {}
public static int[] dispatch(Ticket[] tickets) {
    throw new UnsupportedOperationException("TODO J2");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| Tickets (id,severity,time): (8,4,9),(3,7,5),(2,7,5),(1,7,2) | [1,2,3,8] |
| (2,MIN,0),(1,MAX,0) | [1,2] |
| (4,1,Long.MAX),(5,1,Long.MIN) | [5,4] |
| (MAX,0,0),(MIN,0,0) | [MIN,MAX] |
| [] | [] |
| (7,0,0) | [7] |
| [null] | NPE |
| null | NPE |

**Target:** O(n log(n+1)+1) total time with constant-cost primitive keys; O(n+1) auxiliary queue, O(n) output. Include insertion-based construction and draining.

**Completion:** Prove the comparator policy and verify all tie levels independently; do not mutate keys while queued. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** If only severity descends, where should reversed() apply? If dispatch order is the whole output once, what makes sorting competitive?

<details>
<summary>Hint 1</summary>

Write the desired relation between two tickets in English first.

</details>

<details>
<summary>Hint 2</summary>

A tie in the first key delegates to the next key without reversing its policy.

</details>

<details>
<summary>Hint 3</summary>

Use comparingInt/comparingLong and tie chaining; test endpoints before trusting a subtraction.

</details>

- [ ] **J2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.



## 5. Level 3 — Core heap patterns

Attempt P1 before opening the [bounded-selection theory](./Heap_Priority_Queue_Essential_Concepts.md#10-bounded-selection-and-top-k). P2 tests whether you can reverse the **invariant**, not just a comparator. P3 adds identity and ties; P4 changes state lifetime.

### T5 — Account for every retained occurrence

**Trace · Medium.** For arrivals [7,-1,12,7,3,15,12] and k=3, retain the largest three occurrences. Decide the root meaning yourself. Use the convention that equal-threshold incoming values may be rejected because only numeric multiplicities matter. Never hold more than k entries.

| New value | Retained before, sorted display | Decision | Retained after | Threshold if full |
| --- | --- | --- | --- | --- |
| 7 | [] | — | — | — |
| -1 | — | — | — | — |
| 12 | — | — | — | — |
| 7 | — | — | — | — |
| 3 | — | — | — | — |
| 15 | — | — | — | — |
| 12 | — | — | — | — |

**Checks:** full-prefix thresholds are -1,7,7,7,12 starting at the third arrival; final retained multiset is [12,12,15], descending output [15,12,12]. Re-run k=1 and k=7; outputs must be [15] and [15,12,12,7,7,3,-1]. The table is not PriorityQueue iteration.

**Completion:** show a rejection, replacement, duplicate, and unchanged threshold; prove a discarded candidate cannot become needed later while the stream is append-only.

<details>
<summary>Hint 1</summary>

Think about the weakest currently retained winner.

</details>

<details>
<summary>Hint 2</summary>

The root is useful only if it identifies the candidate that should leave when a better value arrives.

</details>

<details>
<summary>Hint 3</summary>

Handle unfilled slots separately, then compare one new value to the full-set boundary.

</details>

- [ ] **T5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.
### P1 — Return the largest k occurrences

**Coding · Medium.** Derive bounded selection, its threshold, and ordered extraction.

**Prerequisites:** T5, J1, min/max policies, multiset invariants.

**Problem, input, and output:** Return the k largest numeric occurrences from values in nonincreasing order. Implement with a retained set never larger than k; compare against sorting a copy as a test oracle.

**Contract and constraints:** Shared integer and static-selection contracts apply: 0<=k<=n validated, null NPE, duplicates count, negatives/extremes legal, deterministic numeric output, input unchanged. k=0 returns a fresh empty array immediately after null/k checks. Priorities are fixed in this finite batch.

```java
public static int[] topKLargest(int[] values, int k) {
    throw new UnsupportedOperationException("TODO P1");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [7,-1,12,7,3,15,12],3 | [15,12,12] |
| [7,-1,12],1 | [12] |
| [3,1,2],3 | [3,2,1] |
| [5,5,4],2 | [5,5] |
| [MIN,0,MAX],2 | [MAX,0] |
| [],0 | [] |
| [9],0 | [] |
| [1],2 or k=-1 | IAE |
| null,0 | NPE |

**Target:** For 1<=k<=n, O(n log(k+1)) time including ordered output, O(k) auxiliary, O(k) output. k=0: O(1) time/working state. No growth above k is necessary after pre-sizing.

**Completion:** Explain direction, retained-prefix invariant, root meaning, discard safety, termination, and why k=1 still scans. Verify initial capacity does not enforce the size bound. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** When k=n, what practical advantages might sorting have? How would distinct-rank semantics change state?

<details>
<summary>Hint 1</summary>

Full sorting establishes more order than the retained set needs.

</details>

<details>
<summary>Hint 2</summary>

Expose the retained element least deserving to survive the next improvement.

</details>

<details>
<summary>Hint 3</summary>

Decide output placement separately from candidate retention; queue iteration cannot promise descending output.

</details>

- [ ] **P1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P2 — Return the smallest k occurrences

**Coding · Medium.** Transfer a selection proof to its opposite direction.

**Prerequisites:** P1 derivation, reverse comparators.

**Problem, input, and output:** Return the k smallest numeric occurrences in nondecreasing order with no more than k retained candidates. Derive the mirror invariant without editing P1 mechanically.

**Contract and constraints:** Exactly P1’s validation, immutability, integer domain, duplicate and k rules. Output ascending; no identities distinguish equal ints. k=0 immediate fresh empty output.

```java
public static int[] topKSmallest(int[] values, int k) {
    throw new UnsupportedOperationException("TODO P2");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [7,-1,12,7,3,15,12],3 | [-1,3,7] |
| [3,1,2],1 | [1] |
| [3,1,2],3 | [1,2,3] |
| [2,2,2],2 | [2,2] |
| [MIN,MAX,0],2 | [MIN,0] |
| [],0 | [] |
| [1],-1 | IAE |
| null,0 | NPE |

**Target:** Same parameterized time/space targets as P1, including the k=0 exception.

**Completion:** State the mirror rejection rule in words and test increasing, decreasing, and all-equal arrays. Keep k=1/k=n tests. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** Which comparisons must reverse besides the comparator, and which output-order decisions must change?

<details>
<summary>Hint 1</summary>

A new smaller value can force a previously retained large candidate out.

</details>

<details>
<summary>Hint 2</summary>

Retain the smallest min(k,t) occurrences after t values.

</details>

<details>
<summary>Hint 3</summary>

Check replacement direction and output placement independently; changing only one can still fail.

</details>

- [ ] **P2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P3 — Select nearby survey markers

**Coding · Medium.** Combine bounded selection, stable identity, deterministic ties, and safe squared distances.

**Prerequisites:** P1/P2, J2, long arithmetic.

**Problem, input, and output:** Select k markers nearest the fixed origin by Euclidean distance. Rank by squared distance ascending, then id ascending, and return selected IDs in that same best-first order.

**Contract and constraints:** Marker[] and records nonnull. IDs unique as a precondition; coordinates are ints but validate each coordinate in [-1000000000,1000000000]. Validate all records/coordinates even for k=0. Validate 0<=k<=n. Duplicate coordinates/distances legal; smaller ID wins ties including at the cutoff. Immutable records/input unchanged; no moving priorities. Use no square root. The maximum squared sum is 2*10^18, which fits long; prove promotion occurs before multiplication.

```java
record Marker(int id, int x, int y) {}
public static int[] nearestMarkers(Marker[] markers, int k) {
    throw new UnsupportedOperationException("TODO P3");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| (id,x,y): (8,3,4),(2,0,5),(6,1,1),(1,-3,-4), k=3 | [6,1,2] |
| same markers, k=1 | [6] |
| (2,50000,0),(1,49999,0), k=2 | [1,2] |
| (2,1000000000,1000000000),(1,0,0), k=1 | [1] |
| (5,0,0),(3,0,0), k=2 | [3,5] |
| [],0 | [] |
| (1,MIN,0), k=0 | IAE |
| [null],0 | NPE |
| [],1 | IAE |

**Target:** O(n log(k+1)+n+1) time, O(k+1) auxiliary, O(k) output. Full validation keeps k=0 at O(n+1), unlike P1/P2. Constant-time distance/comparator work under the stated domain.

**Completion:** Use the entire distance/id ranking at the rejection boundary. Test equal distances with different IDs and explain why reverse ranking must reverse the tie key too when exposing the weakest candidate. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** If any tied subset were allowed, which assertions would change from exact IDs to membership and distance-boundary properties?

<details>
<summary>Hint 1</summary>

The best-first ranking and the next-rejection ranking have opposite purposes.

</details>

<details>
<summary>Hint 2</summary>

A distant marker or a larger ID at equal distance is a weaker retained candidate.

</details>

<details>
<summary>Hint 3</summary>

Represent stable keys without overflow; compare long distances safely and extract output in the required order.

</details>

- [ ] **P3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P4 — Maintain a rank across arrivals

**Coding · Medium.** Keep bounded persistent state and distinguish insufficient observations from a valid answer.

**Prerequisites:** P1, append-only retained-set proof.

**Problem, input, and output:** Implement OnlineRank. Constructor fixes k. add accepts one int occurrence and returns the kth-largest observation so far, or OptionalInt.empty if fewer than k observations exist. current observes the same answer without inserting or consuming state.

**Contract and constraints:** Validate 1<=k<=100000. Online, append-only calls; at most 100000 arrivals per instance is a precondition. Any int, duplicates and negatives legal. No input history may be stored beyond the necessary bounded candidates; no deletion/expiration. current before any add is absent. Instances independent. Equal answers are not a signal to deduplicate.

```java
static final class OnlineRank {
    OnlineRank(int k) {
        throw new UnsupportedOperationException("TODO P4 constructor");
    }
    OptionalInt add(int value) {
        throw new UnsupportedOperationException("TODO P4 add");
    }
    OptionalInt current() {
        throw new UnsupportedOperationException("TODO P4 current");
    }
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| k=3; add 4,9,1,9,-2,12,9 | [absent,absent,1,4,4,9,9] |
| k=1; add -3,-7,0,MAX | [-3,-3,0,MAX] |
| k=2; add 5,5,4 | [absent,5,5] |
| k=4; add 8,2 | [absent,absent] |
| k=2; add MIN,MAX | [absent,MIN] |
| k=2; no adds; current twice | absent, absent |
| construct k=0 or 100001 | IAE |

**Target:** O(log(k+1)) heap work per add, amortized with ordinary backing growth; O(1) current. With default growth, O(min(k,t)+1) persistent storage after t arrivals; pre-sizing k instead uses O(k) from construction. State the allocation choice and any single-resize cost.

**Completion:** After every prefix compare to a sorted-history test oracle; test current repeatedly and interleave two instances. Explain why current must not poll. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** Would the retained-set proof still work if old values could expire? Give a counterexample rather than silently adding deletion.

<details>
<summary>Hint 1</summary>

A root after two observations cannot represent the third rank.

</details>

<details>
<summary>Hint 2</summary>

Retain precisely the best min(k,t) occurrences; state lifetime is the main change from a batch.

</details>

<details>
<summary>Hint 3</summary>

Use an optional for the warm-up phase and separate read-only observation from update.

</details>

- [ ] **P4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T6 — A rank table with a warm-up period

**Trace · Medium.** Trace P4 with k=3 and arrivals [4,9,1,9,-2,12,9]. Fill the table for **each** arrival. Insert two current() calls after the third arrival and show state is unchanged.

| New value | Heap before, sorted display | Operation | Heap after | Current answer |
| --- | --- | --- | --- | --- |
| 4 | [] | — | — | — |
| 9 | — | — | — | — |
| 1 | — | — | — | — |
| 9 | — | — | — | — |
| -2 | — | — | — | — |
| 12 | — | — | — | — |
| 9 | — | — | — | — |

**Checks:** answers match P4’s first test; final multiset [9,9,12]. A k=1 stream of -3,-7 has answers -3,-3. Explain the difference between an absent rank and an ordinary value such as MIN.

**Completion:** show bounded occupancy and root meaning at every prefix, then explain total costs for m arrivals and why answer-history output would add O(m) memory.

<details>
<summary>Hint 1</summary>

Distinguish the warm-up phase from a full retained set.

</details>

<details>
<summary>Hint 2</summary>

A smaller arrival cannot improve a full largest-k multiset.

</details>

<details>
<summary>Hint 3</summary>

Keep any recorded answer history in the test harness, not in the online object.

</details>

- [ ] **T6 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.


### P5 — Run a batch-reduction machine

**Coding · Medium.** Maintain the current extreme after a priority changes.

**Prerequisites:** J1, stable keys, finite simulation counters.

**Problem, input, and output:** For each of rounds operations, select one largest load x and replace it by x-floor(x/3). Return the sum of all final loads. If the input is empty, no operations occur and the sum is zero.

**Contract and constraints:** Validate 0<=rounds<=100000 and every load in 0..1000000000; all validation occurs even when rounds=0. n<=100000 is a precondition. Equal largest loads are interchangeable; numeric final sum is deterministic. Do not mutate loads. A replacement priority changes only after extraction. Loads 0,1,2 may remain unchanged, so termination follows the finite rounds counter. Result is long; initial and final totals are <=10^14.

```java
public static long remainingLoad(int[] loads, int rounds) {
    throw new UnsupportedOperationException("TODO P5");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [20,11,5],3 | 23 |
| [20,11,5],0 | 36 |
| [3,3],2 | 4 |
| [0,1,2],9 | 3 |
| [],5 | 0 |
| [1000000000,1000000000],1 | 1666666667 |
| [-1],0 | IAE |
| [1],-1 | IAE |
| null,0 | NPE |

**Target:** O((n+m) log(n+1)+1) time with insertion-based construction, where m is actual rounds (0 when empty); O(n+1) auxiliary, scalar output. Compare one-step scanning and repeated full sorting.

**Completion:** Trace selected value, transformed value, current multiset and total. Prove that every step follows the specified simulation, without claiming it optimizes a different objective. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** Why would changing a queued object field be unsafe? Why can “repeat until the total decreases” fail?

<details>
<summary>Hint 1</summary>

The next largest value can change after a single reduction.

</details>

<details>
<summary>Hint 2</summary>

The live multiset must equal the current machine state, including zero loads.

</details>

<details>
<summary>Hint 3</summary>

Extract, transform using safe arithmetic, and reinsert; keep the round counter independent of numeric progress.

</details>

- [ ] **P5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P6 — Combine the two lightest bundles

**Coding · Medium.** Analyze a shrinking priority simulation and an accumulated cost.

**Prerequisites:** J1, long values, multiset accounting.

**Problem, input, and output:** Repeatedly remove the two smallest current weights, combine them into their sum, add that sum to total cost, and reinsert the combined weight. Stop with at most one bundle. Return total cost, not the final bundle weight.

**Contract and constraints:** Finite batch; n<=10000 is a precondition. Validate weights in 0..1000000; null NPE. Duplicates/zero allowed, negatives rejected. All ties interchangeable. Inputs unchanged; merged priorities represented by new long values. Empty/singleton return zero. No minimization proof is required because the two-smallest policy is prescribed. For numeric safety, total input mass <=10^10 and at most n-1 rounds each add at most that mass, so cost<10^14.

```java
public static long bundleCost(int[] weights) {
    throw new UnsupportedOperationException("TODO P6");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [4,7,2,6] | 37 |
| [1,1,1,1] | 8 |
| [0,0,5] | 5 |
| [9] | 0 |
| [] | 0 |
| [1000000,1000000,1000000] | 5000000 |
| [-1,2] | IAE |
| [1000001] | IAE |
| null | NPE |

**Target:** O(n log(n+1)+1) time, O(n+1) auxiliary, scalar output. Count two polls and one offer per round and derive the n-1 round bound.

**Completion:** Show cost and current weights separately. Verify all occurrences are conserved through replacement and explain the shrinking measure. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** If a different pair policy were requested, would the same direction remain correct? Why is the final total mass not the accumulated processing cost?

<details>
<summary>Hint 1</summary>

A newly combined bundle participates in the next priority decision.

</details>

<details>
<summary>Hint 2</summary>

Each round lowers the number of current bundles by one.

</details>

<details>
<summary>Hint 3</summary>

Promote both merged weights and the running cost to long; stop before attempting a second poll from a singleton.

</details>

- [ ] **P6 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P7 — Join two ordered sensor buffers

**Coding · Easy.** Recognize a direct frontier problem that needs no heap.

**Prerequisites:** Array pointers, sorted-chain merge reasoning.

**Problem, input, and output:** Return a fresh nondecreasing array containing every occurrence from sorted arrays left and right. Do not use a heap or sort the concatenation.

**Contract and constraints:** Nonnull arrays individually sorted nondecreasing as trusted preconditions; combined length<=100000. Full int domain and duplicates allowed. Inputs unchanged, including when the same array is passed twice; that supplies two occurrence streams. Output values deterministic; provenance ties may prefer left but equal ints are indistinguishable.

```java
public static int[] mergeTwo(int[] left, int[] right) {
    throw new UnsupportedOperationException("TODO P7");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [-3,2,2],[-2,2,8] | [-3,-2,2,2,2,8] |
| [],[1] | [1] |
| [],[] | [] |
| [MIN],[MAX] | [MIN,MAX] |
| [1,1],[1,1] (also same reference) | [1,1,1,1] |
| null,[] | NPE |

**Target:** O(p+q+1) time, O(1) auxiliary beyond O(p+q) required output. Do not allocate a copy of each source.

**Completion:** State the committed-output-prefix invariant; verify tails and empty sides. Explain why a general heap is unnecessary here. Apply the shared validation and explanation checklist in Section 2.

- [ ] **P7 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### P8 — Join many ordered sensor buffers

**Coding · Medium.** Track one frontier entry per source with identity and exhaustion.

**Prerequisites:** P7, custom comparator, immutable entries, sorted-source assumptions.

**Problem, input, and output:** Return a fresh nondecreasing array containing all values in sorted sources. Maintain one frontier entry for each nonexhausted source, carrying value, source index, and offset. For trace determinism, choose smaller source index then smaller offset when values tie.

**Contract and constraints:** Nonnull outer and every row; k=sources.length<=100000 and N=sum of row lengths<=100000 are preconditions. Rows sorted nondecreasing, trusted; no sortedness scan. Empty rows/outer allowed; duplicate rows by reference are separate streams. Full int values and duplicates retained. Input arrays unchanged. Output order required; no extra full copy or sort of N values.

```java
record Head(int value, int source, int offset) {}
public static int[] mergeMany(int[][] sources) {
    throw new UnsupportedOperationException("TODO P8");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [[-4,2,8],[],[-4,5],[0,2]] | [-4,-4,0,2,2,5,8] |
| [[],[],[]] | [] |
| [] | [] |
| [[1,1],[1],[1,1]] | [1,1,1,1,1] |
| [[MIN,MAX],[0]] | [MIN,0,MAX] |
| [[2,3],[2,3]] (also alias rows) | [2,2,3,3] |
| [[],null] | NPE |
| null | NPE |

**Target:** O(k+N log(a+1)+1) time where a is the number of initially nonempty sources; O(a+1) auxiliary, O(N) output. Keep the k header-scan term even when N=0. Count cumulative cursor allocations separately from peak live frontier size.

**Completion:** Prove one source head represents all unread values in that source. Compare a scan of all heads, repeatedly merging into an expanding result, balanced pairwise merging, and sorting all elements. Explain how array offsets become node references in Linked List’s optional Merge k Sorted Lists. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** With one huge source and 99999 empty sources, why is O(N log k) alone an incomplete description? What breaks if a row is unsorted?

<details>
<summary>Hint 1</summary>

Only each source’s first unread value can be its next contribution.

</details>

<details>
<summary>Hint 2</summary>

Every nonexhausted source must have exactly one representative.

</details>

<details>
<summary>Hint 3</summary>

After extraction advance only that entry’s source; do not lose source identity when values tie.

</details>

- [ ] **P8 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T7 — Follow the source, not just the value

**Trace · Medium.** Use P8’s sources [[-4,2,8],[],[-4,5],[0,2]]. Draw a row for every output with frontier before, chosen value@source:offset, successor if any, exhausted sources, and output prefix. Display frontiers in comparator order, explicitly not iterator order.

**Checks:** source choices are [0,2,3,0,3,2,0]; values are [-4,-4,0,2,2,5,8]. Initially three frontier entries exist; after the final output none remains. Test [[1],[1]]: both occurrences must appear, with source 0 chosen first under this trace policy. [] and [[],[]] emit nothing.

**Completion:** account for each source offset exactly once and explain why advancing every source sharing the same value would lose data.

<details>
<summary>Hint 1</summary>

Equal values can have different successors.

</details>

<details>
<summary>Hint 2</summary>

Keep one representative per unread source, not one representative per distinct value.

</details>

<details>
<summary>Hint 3</summary>

Write source and offset in the trace before changing either.

</details>

- [ ] **T7 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.
### P9 — Measure a cooldown dispatcher

**Coding · Medium; Additional-scope.** Separate currently available priorities from temporarily ineligible work.

**Prerequisites:** Frequency counting, J2, queues, long time arithmetic.

**Problem, input, and output:** All task occurrences are present initially and may be reordered. One processor executes one unit task in [t,t+1). At each decision, among eligible labels choose greatest remaining frequency, alphabetically smaller label on ties. After running label X at t, X may next start at t+cooldown+1. If no task is eligible, idle until the earliest eligibility time. Return the elapsed length produced by this specified policy, not a materialized timeline.

**Contract and constraints:** Input char[] may contain only A..Z, validate every label; duplicates are tasks. Validate 0<=cooldown<=1000000000. n<=100000. Empty returns 0 after validation. Input unchanged; available priorities change only when a task is extracted/executed. No release times, unequal durations, preemption, multiple processors, or per-label cooldowns. Result long; (n-1)*(cooldown+1)+1 bounds a serial schedule for nonempty input and fits long. Do not claim this implementation proves optimality for more general scheduling.

```java
public static long dispatcherLength(char[] tasks, int cooldown) {
    throw new UnsupportedOperationException("TODO P9");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| "AAABB",2 | 7 |
| "AAB",1 | 3 |
| "AAA",2 | 7 |
| "AABBC",2 | 5 |
| "AAABBB",2 | 8 |
| "AAAA",0 | 4 |
| "AA",1000000000 | 1000000002 |
| "",4 | 0 |
| "Aa",0 | IAE |
| "A",-1 | IAE |
| null,0 | NPE |

**Target:** O(n log(u+1)+u+1) time with direct idle jumps, O(u+1) scheduling state plus the fixed 26-count array, scalar output. u<=26 is distinct labels. Explain the lower bound Ω(T) if a full length-T timeline were required.

**Completion:** Count tasks once, track cooling labels separately, release all ready entries before selection, and justify FIFO release ordering under a common cooldown. Independently validate legality on small schedules. Compare a proved counting approach for the related minimum-length problem, without assuming a formula applies to changed policies. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** At t=0 and cooldown=2, is the next legal start 2 or 3? If different tasks have different delays, is a FIFO release queue still justified?

<details>
<summary>Hint 1</summary>

Priority among eligible tasks and future eligibility are separate pieces of information.

</details>

<details>
<summary>Hint 2</summary>

Execution times increase; adding one common fixed delay preserves release-time order.

</details>

<details>
<summary>Hint 3</summary>

Use long time and jump across an idle run when only the length is needed.

</details>

- [ ] **P9 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### T8 — Distinguish release, run, and idle

**Trace · Medium; Additional-scope.** Trace P9 on "AAABB" with cooldown=2. Record time before release, labels released, available priorities, chosen task or idle jump, cooling entries with readyAt, and time after the event. Also explain why a high remaining count cannot override a cooldown.

**Checks:** compressed run events are (0,A),(1,B),(3,A),(4,B),(6,A); elapsed length 7. Idle intervals are [2,3) and [5,6). With "AA", cooldown=0, starts 0 and 1 are legal; with cooldown=2, starts 0 and 3 are legal and length is 4. Verify a cooldown boundary by counting the intervening whole intervals.

**Completion:** give the complete state table and a legality proof. For this "AAABB" fixture, obtain an independent lower bound from the three A occurrences and check attainment. That does not establish a universal rule for variants with release times or different durations.

<details>
<summary>Hint 1</summary>

Mark the first legal reuse time when a label is executed.

</details>

<details>
<summary>Hint 2</summary>

The available set must exclude every label with readyAt>time.

</details>

<details>
<summary>Hint 3</summary>

An idle jump changes time, not remaining counts; process releases before the next choice.

</details>

- [ ] **T8 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.


## 6. Level 4 — Curated practice

These are the **six exact roadmap entries, in roadmap order**. Priority E/A/C is the repository’s learning tier; Easy/Medium/Hard is LeetCode difficulty. Official pages supply current platform signatures and constraints, which can differ from the original contracts above. Open the statement with solution/editorial tabs closed.

Each record below has the same six fields as original exercises. Once attempted, point it to the corresponding [roadmap Heap Log](./DSA_Interview_Roadmap.md#9-heap--priority-queue), which remains the single curated history. Check an attempt only after tests and a postmortem; I/H/S still controls what that completion demonstrates.

### L1 — Kth Largest Element in a Stream

**Priority E · LeetCode Easy.** [Official problem 703](https://leetcode.com/problems/kth-largest-element-in-a-stream/).

**Objective:** preserve a fixed-rank retained-set invariant as observations arrive. **Prerequisites:** P1, P4, T6; verify platform initialization and rank-availability assumptions. **Assessed skills:** choosing the threshold direction, handling duplicate occurrences, preserving state across calls, explaining bounded memory and update cost.

**Attempt deliverable:** clarify constructor versus update behavior, trace a tied arrival and a new maximum, run an independent sorted-prefix oracle, and explain why discarded values stay irrelevant. Check the platform contract before importing P4’s optional-answer behavior.

- [ ] **L1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### L2 — Last Stone Weight

**Priority E · LeetCode Easy.** [Official problem 1046](https://leetcode.com/problems/last-stone-weight/).

**Objective:** process the current highest priorities through a shrinking simulation. **Prerequisites:** J1, P5/P6, singleton/empty distinctions. **Assessed skills:** extraction order, transform/reinsert logic, equal-priority handling, termination and event counting.

**Attempt deliverable:** write the transformation from the official statement in your own words, test equal weights and a one-item ending, and prove a bound on rounds. Do not substitute P5’s transformation or P6’s accumulated cost.

- [ ] **L2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### L3 — K Closest Points to Origin

**Priority E · LeetCode Medium.** [Official problem 973](https://leetcode.com/problems/k-closest-points-to-origin/).

**Objective:** retain nearby candidates while avoiding needless complete ordering. **Prerequisites:** P1–P3, comparator/distance safety. **Assessed skills:** rejection boundary, squared-distance range proof, output properties and mutation assumptions.

**Attempt deliverable:** determine what output order and tie choices the platform allows, then validate count, membership, and the selected-distance boundary. A test must not reject a valid permutation merely because it differs from one example. Do not add P3’s ID policy unless the statement needs it.

- [ ] **L3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### L4 — Kth Largest Element in an Array

**Priority A · LeetCode Medium.** [Official problem 215](https://leetcode.com/problems/kth-largest-element-in-an-array/).

**Objective:** adapt retained candidates to one static rank. **Prerequisites:** P1/P4, valid k and occurrence semantics. **Assessed skills:** static versus online cost, root interpretation, sorting baseline, bounded versus full heap.

**Attempt deliverable:** include k=1, k=n, negatives, and repeated values; justify total construction and selection costs. **Quickselect is an optional comparison**, not a prerequisite: discuss expected versus worst-case cost, input mutation/copying, and the distinction between partitioning and sorting.

- [ ] **L4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### L5 — Task Scheduler

**Priority A · LeetCode Medium.** [Official problem 621](https://leetcode.com/problems/task-scheduler/).

**Objective:** reason about eligibility, cooldown gaps, idle time, and minimum completion length. **Prerequisites:** P9/T8, frequency counts, a clear time model. **Assessed skills:** legal state transitions, event-time accounting, and an optimality justification.

**Attempt deliverable:** choose **heap plus cooldown state** or a **mathematically justified counting approach**. For counting, establish a lower bound and why it can be attained; naming a formula is insufficient. For simulation, distinguish legal schedule generation from a proof that its length is minimal. Compare total elapsed time with the number of task executions.

- [ ] **L5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### L6 — Find Median from Data Stream

**Priority C · LeetCode Hard · OPTIONAL.** [Official problem 295](https://leetcode.com/problems/find-median-from-data-stream/).

**Objective:** preserve both cross-half order and size balance. **Prerequisites:** reliable one-heap invariants and optional O1. **Assessed skills:** rebalancing, odd/even ranks, read-only median, long-before-add arithmetic, persistent storage.

**Attempt deliverable:** test both endpoint averages and duplicates across the partition. Explain why all observations remain represented. This problem and the optional workshop do **not** block ordinary T/I/P/R readiness.

- [ ] **L6 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

## 7. Level 5 — Mixed interview challenges

Hide topic notes and hints initially. The titles do not identify an algorithm. Before coding, compare Heap, sorting, HashMap, binary search, scanning, and any relevant earlier technique. Optional Quickselect may be discussed for a static rank, but no challenge requires implementing it.

For **each X exercise**, answer: Is priority access repeated? Is only k state needed? Is input online? Is full sorted output needed? Can queued keys change? Is preprocessing reusable? Does heap overhead earn its cost? Some tasks intentionally reward a different choice.
### X1 — Export the complete audit batch

**Coding · Easy.** Choose a method suited to full ordered output.

**Prerequisites:** Arrays, sorting comparators, index identity.

**Problem, input, and output:** Return all original indexes ordered by their corresponding amount ascending; for equal amounts, smaller original index comes first. This is a one-off export of the complete batch, with no later inserts or rank queries.

**Contract and constraints:** Nonnull int[] amounts, n<=100000; full int domain, duplicates/negatives allowed. All n records required; no k. Input unchanged. Output fresh int[n] with each original index exactly once. Priorities static and complete tie order deterministic.

```java
public static int[] auditOrder(int[] amounts) {
    throw new UnsupportedOperationException("TODO X1");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [5,-2,5,0] | [1,3,0,2] |
| [2,2,2] | [0,1,2] |
| [3,2,1] | [2,1,0] |
| [MIN,MAX] | [0,1] |
| [] | [] |
| [8] | [0] |
| null | NPE |

**Target:** O(n log(n+1)+1) time; at most O(n+1) auxiliary including index/key structures and any sort workspace, O(n) output. Explain why the entire ordering is needed.

**Completion:** Test index-permutation validity as well as key/tie order; defend your choice against a heap that must eventually drain everything. Apply the shared validation and explanation checklist in Section 2.

- [ ] **X1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### X2 — Summarize recurring incident codes

**Coding · Medium.** Combine a frequency summary with a carefully defined cutoff.

**Prerequisites:** Frequency maps, occurrence versus distinct-key semantics, comparator ties.

**Problem, input, and output:** Return k distinct incident codes ranked by frequency descending, then numeric code ascending. Input values are individual incident occurrences; output contains code values once each.

**Contract and constraints:** Validate 0<=k<=u where u is the number of distinct input codes; even k=0 must reject null. n<=100000, full int values and repeats legal. Finite immutable input; frequencies fixed after counting. Deterministic ties including at cutoff. Output fresh int[k]. k=1 and k=u valid. Do not confuse k<=n with k<=u.

```java
public static int[] recurringCodes(int[] events, int k) {
    throw new UnsupportedOperationException("TODO X2");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [4,2,4,3,2,4,3,1],2 | [4,2] |
| same events,4 | [4,2,3,1] |
| [7,7,7],1 | [7] |
| [MIN,MAX],1 | [MIN] |
| [],0 | [] |
| [5,5],2 | IAE |
| [1],-1 | IAE |
| null,0 | NPE |

**Target:** Expected O(n+u log(k+1)+1) time and O(u+k+1) auxiliary under ordinary integer hashing and growth; O(k) output. Explain the k=0 behavior and the cost of obtaining u. A sorted-distinct-key baseline is acceptable first but must be compared to the target.

**Completion:** Separate frequency aggregation from ranking, justify tie rejection using both keys, and explain which state scales with u rather than k. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** Would a fixed domain of 0..15 change your representation? What if incidents were online and old counts could increase while records remained queued?

<details>
<summary>Hint 1</summary>

The raw number of occurrences and the number of ranked candidates are different quantities.

</details>

<details>
<summary>Hint 2</summary>

Once counts are complete, each distinct code has a stable compound rank.

</details>

<details>
<summary>Hint 3</summary>

Keep only the required ranked candidates if u is large; never rank through a changing external count map without an update strategy.

</details>

- [ ] **X2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### X3 — Find the next published reading

**Coding · Medium.** Exploit already ordered data for many independent lookups.

**Prerequisites:** Binary-search boundaries and exclusive endpoints.

**Problem, input, and output:** For each query x, return the index of the first reading strictly greater than x in sorted readings, or readings.length if none exists. Preserve query order.

**Contract and constraints:** Both arrays nonnull; n readings and q queries each<=100000. Readings are already nondecreasing (trusted), queries arbitrary order. Duplicate/negative/full int values valid. No updates; no preprocessing/index allocation required. Inputs unchanged. Output fresh int[q]. Returned n is a boundary, not a valid element index.

```java
public static int[] nextReading(int[] readings, int[] queries) {
    throw new UnsupportedOperationException("TODO X3");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| readings=[-2,1,1,8], queries=[1,0,8,-3] | [3,1,4,0] |
| [],[0,1] | [0,0] |
| [MIN,MAX],[MIN,MAX] | [1,2] |
| [2,2,2],[1,2] | [0,3] |
| [1],[] | [] |
| null,[] | NPE |
| [],null | NPE |

**Target:** O(q log(n+1)+q+1) time, O(1) auxiliary beyond O(q) output. Do not scan readings to validate a trusted sortedness precondition.

**Completion:** State a boundary invariant and prove strictness handles duplicates. Compare scanning per query and building a heap that would discard data needed by later queries. Apply the shared validation and explanation checklist in Section 2.

<details>
<summary>Hint 1</summary>

Each query asks for a position in an existing total order.

</details>

<details>
<summary>Hint 2</summary>

The predicate “reading>x” changes from false to true at most once.

</details>

<details>
<summary>Hint 3</summary>

Keep the answer boundary allowed to equal n and avoid dereferencing it.

</details>

- [ ] **X3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### X4 — Measure the batch spread

**Coding · Easy.** Avoid maintaining an ordering that the answer does not require.

**Prerequisites:** Array scanning, safe subtraction, OptionalLong.

**Problem, input, and output:** Return maximum value minus minimum value as OptionalLong, or absent for an empty array. The batch is read once and has no later queries.

**Contract and constraints:** Nonnull array, n<=100000, full int values and duplicates legal. A singleton has spread 0. Return exact nonnegative long; MAX-MIN is 4294967295. No mutation or k; ties have no identity significance.

```java
public static OptionalLong spread(int[] values) {
    throw new UnsupportedOperationException("TODO X4");
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| [7,-5,2,7] | 12 |
| [9] | 0 |
| [4,4,4] | 0 |
| [MIN,MAX] | 4294967295 |
| [] | absent |
| null | NPE |

**Target:** O(n+1) time and O(1) auxiliary; scalar optional output.

**Completion:** Explain why one or two heaps add no useful operation here. Widen before subtracting and test permutation invariance. Apply the shared validation and explanation checklist in Section 2.

- [ ] **X4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### X5 — Allocate incoming work packets

**Coding · Medium.** Recognize repeated selection with deterministic identity and changing priorities.

**Prerequisites:** Stable queued entries, safe compound ranking, online state ownership.

**Problem, input, and output:** A service owns w worker ledgers, initially total 0, IDs 0..w-1. On add(duration), choose the worker with the smallest accumulated assigned duration, smaller ID on ties; add duration to that ledger and return its ID. This models cumulative assignments, not elapsed execution or task completion. totals returns a fresh long[] in ID order.

**Contract and constraints:** Validate 1<=w<=100000 and duration in 0..1000000000; invalid adds leave all state unchanged. At most 100000 adds per instance is a precondition, so each total<=10^14. Online, no removals/completions; equal totals and zero duration are legal. Independent instances. A worker’s priority changes after selection; it must not be mutated while still ordered under its old key. All w ledgers exist even if no packets arrive.

```java
static final class PacketAllocator {
    PacketAllocator(int workers) {
        throw new UnsupportedOperationException("TODO X5 constructor");
    }
    int add(int duration) {
        throw new UnsupportedOperationException("TODO X5 add");
    }
    long[] totals() {
        throw new UnsupportedOperationException("TODO X5 totals");
    }
}
```

**Examples and tests:** Each row is an independent call or a fresh instance unless marked as one sequence. `MIN` and `MAX` mean the int endpoints; `absent` means an empty optional.

| Input / operations | Expected result |
| --- | --- |
| w=3; durations [5,2,4,1,0,8] | IDs [0,1,2,1,1,1]; totals [5,11,4] |
| w=2; [0,0,2,1] | IDs [0,0,0,1]; totals [2,1] |
| w=1; [1000000000,1000000000,1000000000] | IDs [0,0,0]; totals [3000000000] |
| w=3; [] | IDs []; totals [0,0,0] |
| w=0 | IAE |
| w=2; [-1] | IAE; totals stay [0,0] |

**Target:** O(w log(w+1)+1) construction is acceptable; O(log(w+1)) per add after initialization with bounded occupancy; O(w) per totals snapshot. O(w) persistent state, O(w) snapshot output, O(1) explicit update locals. No stale-history accumulation.

**Completion:** Use a complete total/id ordering and compare small random tapes against a linear scan of all ledgers. Mutate one returned snapshot and verify the object remains unchanged. Apply the shared validation and explanation checklist in Section 2.

**Interviewer follow-up:** What changes if completion events subtract work later? If w=1, what simplification is possible?

<details>
<summary>Hint 1</summary>

Only one ledger changes after the next worker is selected.

</details>

<details>
<summary>Hint 2</summary>

The selected worker is the first in the total/id ordering; every other entry keeps its rank key.

</details>

<details>
<summary>Hint 3</summary>

Remove the selected entry before changing its priority and reintroduce its new state.

</details>

- [ ] **X5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.



## 8. Complexity laboratory

Read the [cost model](./Heap_Priority_Queue_Essential_Concepts.md#16-complete-complexity-analysis) only after deriving your answer. These are **analysis exercises**, not alternate implementations to memorize.

For **each C exercise**, submit all eight items: input-size variables; operations; execution counts; cost per operation; total time; peak auxiliary/persistent/output space; assumptions; a better approach or a justified reason none is needed. Include initialization and copies. Explain empty/k=1 boundaries where allowed. Distinguish a worst-case bound from a tight count for one fixture.

Conventions: n=input values, k=retained candidates or supplied streams as locally defined, m=updates/rounds; N=total merged values, a=nonempty streams, u=distinct labels, p=peeks, r=arbitrary removals. Primitive comparisons/equality are O(1) unless C/E costs are explicitly requested. Preconditions in these specimens are trusted and do not add validation passes.

### C1 — Insert, inspect, and drain

**Analysis · Easy.** Nonnull nonempty values, n<=100000; 0<=peeks<=100000. Read-only input; long checksum fits. Analyze both total heap work and the possible cost of one growth-triggering offer.

```java
static long cOperations(int[] values, int peeks) {
    PriorityQueue<Integer> q = new PriorityQueue<>();
    for (int value : values) q.offer(value);
    long checksum = 0;
    for (int i = 0; i < peeks; i++) checksum += q.peek();
    while (!q.isEmpty()) checksum += q.poll();
    return checksum;
}
```

**Tests:** ([3,1,2],4) returns 10; ([5],0) returns 5; ([-2,4],3) returns -4. Add an increasing and a decreasing insertion order with the same multiset. Explain why repeated peek does not consume entries. After draining, does capacity necessarily become zero?

- [ ] **C1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C2 — Charge build work by height

**Analysis · Medium.** The next wrapper invokes F3’s iterative down contract on all internal positions; this method’s return counts calls, not comparisons. Input a is nonnull, n<=100000; it is deliberately mutated. Compare against repeated offers of a decreasing sequence.

```java
static int cBuildCalls(int[] a) {
    int calls = 0;
    for (int i = a.length / 2 - 1; i >= 0; i--) {
        down(a, a.length, i);
        calls++;
    }
    return calls;
}
```

**Tests:** lengths 0,1,2,8 yield call counts 0,0,1,4; length-8 T4 input must end with T4’s heap. Instrument downward levels and key comparisons separately. Bound the sum of possible subtree edge-heights using floor(n/2^j). Compare copied construction with in-place construction, including output ownership.

**Completion:** explain why counting calls alone cannot distinguish O(n) from O(n log n), then derive the tighter bound.

<details>
<summary>Hint 1</summary>

Different calls start at very different heights.

</details>

<details>
<summary>Hint 2</summary>

Count how many nodes can descend one level, two levels, and so on.

</details>

<details>
<summary>Hint 3</summary>

Separate the linear copy, if present, from the sum of local repair work.

</details>

- [ ] **C2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C3 — Full storage versus bounded storage

**Analysis · Medium.** Here 0<=k<=n; input nonnull. boxed is an ordinary ArrayList, not a PriorityQueue. Analyze the **OpenJDK 21** collection-construction path and contrast it with filling a comparator-based queue through n offers. Then compare this full-heap approach with P1/P2’s bounded candidate processing, including required sorted output.

```java
static long cFullSelection(int[] values, int k) {
    List<Integer> boxed = new ArrayList<>(values.length);
    for (int value : values) boxed.add(value);
    PriorityQueue<Integer> all = new PriorityQueue<>(boxed);
    long checksum = 0;
    for (int i = 0; i < k; i++) checksum += all.poll();
    return checksum;
}
```

**Tests:** ([9,1,4,1],2) returns 2; k=0 returns 0 but still builds storage; k=4 returns 15; ([],0) returns 0. Explain why k=1 does not make reading n inputs free. Give symbolic costs for n, k=1, k near n, and optional Quickselect’s different output/mutation guarantees.

**Completion:** count both the boxing list and heap backing array. A scalar result does not make the algorithm constant-space.

<details>
<summary>Hint 1</summary>

The constructor receives a collection whose numeric order is natural.

</details>

<details>
<summary>Hint 2</summary>

Separate full construction from k root removals; do not charge n offers unless they occur.

</details>

<details>
<summary>Hint 3</summary>

Compare maximum live state n versus k and include the ordered-output work required by the other contract.

</details>

- [ ] **C3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C4 — Pay for preparation before querying

**Analysis · Medium.** Nonnull nonempty values, n<=100000; 0<=queries<=100000. The clone is a primitive-array copy. Discuss whether either preprocessing step can be removed under this one-minimum repeated-query workload.

```java
static long cPreparedMinimum(int[] values, int queries) {
    int[] copy = values.clone();
    Arrays.sort(copy);
    PriorityQueue<Integer> q = new PriorityQueue<>();
    for (int value : copy) q.offer(value);
    long checksum = 0;
    for (int i = 0; i < queries; i++) checksum += q.peek();
    return checksum;
}
```

**Tests:** ([8,-1,3],5) returns -5; ([2],0) returns 0; original input must remain unchanged. Analyze the actually sorted offer order rather than automatically claiming every offer travels to root. Include sorting, allocation, and unused preprocessing when queries=0.

<details>
<summary>Hint 1</summary>

The output needs one extreme repeated, not a changing priority order.

</details>

<details>
<summary>Hint 2</summary>

An ascending insertion order has a useful relation to its existing parents.

</details>

<details>
<summary>Hint 3</summary>

Total time still includes sorting even if subsequent heap insertions need little repair.

</details>

- [ ] **C4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C5 — Count frontiers, copies, and output

**Analysis · Medium.** mergeMany has P8’s contract and target; source headers k, total N, initially nonempty a. The two rows in the second call intentionally alias the same sorted array and count as separate occurrence streams. Require 2N<=100000. The wrapper returns a scalar but retains intermediate arrays during its calls.

```java
static int cMergeExpansion(int[][] sources) {
    int[] first = mergeMany(sources);
    int[] second = mergeMany(new int[][]{first, first});
    return second.length;
}
```

**Tests:** [[1,4],[],[2]] returns 6; [[],[],[]] returns 0; [[5]] returns 2. Explain initialization with many empty source headers, the changing frontier, cumulative entry allocations versus live entries, and why intermediate arrays count as auxiliary to this scalar-returning wrapper.

Compare three baselines for a single merge: repeatedly append a new source through pairwise merging (account for source lengths/order), a balanced merge tree, and sorting all N values. State assumptions instead of asserting sequential merging is always exactly Θ(Nk).

<details>
<summary>Hint 1</summary>

An array returned by a helper can be temporary work in its caller.

</details>

<details>
<summary>Hint 2</summary>

The second call has at most two active sources even if the first has many.

</details>

<details>
<summary>Hint 3</summary>

Write N as a sum of source lengths; account for how often each value is copied in an unbalanced merge sequence.

</details>

- [ ] **C5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C6 — Online updates and a separate simulation

**Analysis · Medium.** Use P4/P5 contracts. k positive, incoming nonnull, at most 100000 arrivals; loads nonnull and valid. Distinguish t=current stream observations, m=incoming length, n=loads.length, and b=rounds. Record which methods change state and which only observe it.

```java
static long cUpdateBatch(int k, int[] incoming, int[] loads, int rounds) {
    OnlineRank rank = new OnlineRank(k);
    long observed = 0;
    for (int value : incoming) {
        rank.add(value);
        if (rank.current().isPresent()) observed++;
    }
    return observed + remainingLoad(loads, rounds);
}
```

**Tests:** k=3, incoming=[4,9,1,9], loads=[20,11,5], rounds=3 returns 25; k=5, incoming=[], loads=[], rounds=8 returns 0. Also analyze m<k, n=0, and b=0 separately. Would storing every stream answer add working state or required output here? Why does “each operation is log n” mix unrelated sizes?

<details>
<summary>Hint 1</summary>

Streaming candidates and simulation loads occupy two different state spaces.

</details>

<details>
<summary>Hint 2</summary>

Only prefixes long enough to have a kth observation contribute to observed.

</details>

<details>
<summary>Hint 3</summary>

Add the costs of the two phases and identify their simultaneously live storage; do not multiply unrelated loops.

</details>

- [ ] **C6 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### C7 — Search, equality, and arbitrary removal

**Analysis · Medium.** Nonnull values/targets, each length<=100000; full ints. Track q’s shrinking size during successful removals. remove(Integer.valueOf(x)) means equality-based removal of one occurrence, not an indexed extraction.

```java
static int cFindAndRemove(int[] values, int[] targets) {
    PriorityQueue<Integer> q = new PriorityQueue<>();
    for (int value : values) q.offer(value);
    int removed = 0;
    for (int target : targets) {
        if (q.contains(target) && q.remove(Integer.valueOf(target))) {
            removed++;
        }
    }
    return removed;
}
```

**Tests:** values=[4,4,7], targets=[4,4,4,7] returns 3; values=[1,2], targets=[9,9] returns 0; empty values returns 0. Count the redundant successful contains-plus-remove search. Generalize one operation to comparator cost C and equality cost E. If only counted membership/removal matters and no extreme is ever requested, propose a better structure.

**Completion:** produce a worst-case family of absent targets that forces scanning; do not infer logarithmic arbitrary lookup from logarithmic poll.

<details>
<summary>Hint 1</summary>

PriorityQueue only organizes enough order to expose its head.

</details>

<details>
<summary>Hint 2</summary>

An equality match may be anywhere, and missing values may force a full search.

</details>

<details>
<summary>Hint 3</summary>

Ask whether the workload ever uses the priority root before paying to maintain it.

</details>

- [ ] **C7 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### Delayed count checks

<details>
<summary>Open after writing the seven derivations</summary>

These are operation-accounting checkpoints, not full answers. C1 performs n offers, peeks peeks, and n successful polls. C2 calls down floor(n/2) times; the potential downward-level sum is less than n. C3 constructs all n entries even at k=0 and polls exactly k times. C4 prepares once and never removes a root. C5’s helper outputs have lengths N and 2N. C6 counts max(0,m-k+1) valid-rank prefixes. C7 removes at most min(n,targets.length) occurrences but may scan unsuccessfully many more times.

A derivation missing allocated copies, comparator/equality cost assumptions, or the distinction between helper output and caller auxiliary memory remains incomplete.

</details>


## 9. Debugging laboratory

Every specimen below has a real reproducible defect under its stated contract. They compile as Java 21; compilation is not correctness. Inputs are valid under the stated preconditions unless empty behavior itself is the defect. The expected values describe the **intended corrected behavior**, not a claim that the bad code passes. Derive the exact observed failure yourself.

Use [pitfalls](./Heap_Priority_Queue_Essential_Concepts.md#18-java-pitfalls-and-counterexamples) only after producing a concrete witness. A correction can change the data structure if that better satisfies the contract. No complete corrected implementations are supplied.
### D1 — A plausible parent formula

**Debugging · Medium.** Intended contract: F2’s exact min-heap append repair, active size>=1; prior size-1 prefix is a valid heap.

```java
static void dParent(int[] a, int size) {
    int i = size - 1;
    while (i > 0) {
        int parent = i / 2;
        if (a[parent] <= a[i]) break;
        int saved = a[parent]; a[parent] = a[i]; a[i] = saved;
        i = parent;
    }
}
```

**Tests and required behavior:** [1,8,2,9,5], size=5 must become [1,5,2,9,8]. Check the edge at indexes 1 and 4. Controls: [2,1],2 becomes [1,2]; [7],1 stays [7].

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

Distinguish zero-based indexing from a one-based formula.

</details>

<details>
<summary>Hint 2</summary>

An even child index can expose a parent mismatch that an odd index hides.

</details>

<details>
<summary>Hint 3</summary>

Derive parent from the two child positions before editing the arithmetic.

</details>

- [ ] **D1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D2 — Only one child is considered

**Debugging · Medium.** Intended contract: local min sift-down with ordered child subtrees, as in F3. Ties choose left; valid nonempty active prefix.

```java
static void dLeftOnly(int[] a, int size, int i) {
    while (i < size / 2) {
        int child = 2 * i + 1;
        if (a[i] <= a[child]) break;
        int saved = a[i]; a[i] = a[child]; a[child] = saved;
        i = child;
    }
}
```

**Tests and required behavior:** [9,4,2],3,0 must become [2,4,9]. The bad output still violates an edge. Controls: [5,2],2,0 becomes [2,5]; a leaf [7] stays unchanged. Add a right-child-absent case and an equal-child case.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

A node’s two children compete to establish its next correct value.

</details>

<details>
<summary>Hint 2</summary>

Restoring one edge can leave the other invalid.

</details>

<details>
<summary>Hint 3</summary>

Select among existing children before comparing the parent to the chosen child.

</details>

- [ ] **D2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D3 — Priority arithmetic changes the order

**Debugging · Medium.** Intended contract: return the minimum of a nonempty int array without changing it. The helper also claims ascending long comparison.

```java
static int dNumericMinimum(int[] values) {
    PriorityQueue<Integer> q = new PriorityQueue<>((a, b) -> a - b);
    for (int value : values) q.offer(value);
    return q.peek();
}
static int dLongOrder(long a, long b) {
    return (int) (a - b);
}
```

**Tests and required behavior:** [MIN,1] must return MIN; [MAX,-1] must return -1. A control [3,1,2] returns 1. dLongOrder(0L,4294967296L) must be negative, not a tie; dLongOrder(Long.MIN_VALUE,1L) must be negative. Do not assert every faulty comparator invocation produces a visible queue failure; test its sign directly too.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

A subtraction result is a numeric difference, not a reliable comparison sign under overflow.

</details>

<details>
<summary>Hint 2</summary>

A long difference can lose information when narrowed even without overflowing long.

</details>

<details>
<summary>Hint 3</summary>

Compare primitives with safe APIs; validate antisymmetry and transitivity at endpoints.

</details>

- [ ] **D3 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D4 — A square is widened too late

**Debugging · Medium.** Intended contract: select the nearer x-axis coordinate among first and second, with first winning a distance tie. Both ints are restricted to ±10^9. The return is the chosen original coordinate.

```java
static long dSquare(int x) {
    return (long) (x * x);
}
static int dNearer(int first, int second) {
    return dSquare(first) <= dSquare(second) ? first : second;
}
```

**Tests and required behavior:** dNearer(50000,1) must return 1, because the true squares are 2500000000 and 1. dNearer(-3,4) must return -3; dNearer(-5,5) must return -5. Extend your corrected test to a two-coordinate sum under P3’s domain and explain its long bound.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

Casting the result does not change the arithmetic type used earlier.

</details>

<details>
<summary>Hint 2</summary>

The comparison is only meaningful when its distance keys are mathematically correct.

</details>

<details>
<summary>Hint 3</summary>

Widen an operand before multiplying, then separately prove the sum of squares fits.

</details>

- [ ] **D4 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D5 — The wrong candidates survive

**Debugging · Medium.** Intended contract: kth-largest occurrence for nonnull finite values with 1<=k<=n; values may repeat. Input unchanged.

```java
static int dWrongRank(int[] values, int k) {
    PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.reverseOrder());
    for (int value : values) {
        q.offer(value);
        if (q.size() > k) q.poll();
    }
    return q.peek();
}
```

**Tests and required behavior:** [8,3,10,1],k=2 must return 8; [5,5,4],k=2 must return 5; [4],k=1 returns 4. Explain what set the bad code actually retains. A temporary k+1 occupancy is allowed for this specimen, but your reported invariant must say when the size bound holds.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

Ask which candidate is expelled when the queue becomes too large.

</details>

<details>
<summary>Hint 2</summary>

The root for answering the rank must also define the weakest retained winner.

</details>

<details>
<summary>Hint 3</summary>

Check the desired retained set before changing comparator direction.

</details>

- [ ] **D5 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D6 — A capacity hint becomes a false bound

**Debugging · Medium.** Intended contract: after each arrival, return kth-largest occurrence or absent before k observations. Nonnull values and positive k. This finite wrapper returns answer history for testing.

```java
static List<OptionalInt> dUnboundedStream(int[] values, int k) {
    PriorityQueue<Integer> q = new PriorityQueue<>(k);
    List<OptionalInt> answers = new ArrayList<>();
    for (int value : values) {
        q.offer(value);
        answers.add(q.size() < k ? OptionalInt.empty() : OptionalInt.of(q.peek()));
    }
    return answers;
}
```

**Tests and required behavior:** values=[4,9,1,10],k=2 must give [absent,4,4,9]. [5,5,4],k=2 must give [absent,5,5]. k=1 with [-3,0] must give [-3,0]. Add a size assertion after every update; no entry should be removed merely because it duplicates another numeric value.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

Initial capacity reserves space; it does not reject future insertions.

</details>

<details>
<summary>Hint 2</summary>

Growing the queue beyond k changes the rank represented by its minimum.

</details>

<details>
<summary>Hint 3</summary>

Enforce retained-set membership on every arrival and observe without consuming the answer.

</details>

- [ ] **D6 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D7 — A traversal is treated as a sorted result

**Debugging · Medium.** Intended contract: return a fresh ascending list of every numeric occurrence from values. No mutation.

```java
static List<Integer> dIteratorOrder(int[] values) {
    PriorityQueue<Integer> q = new PriorityQueue<>();
    for (int value : values) q.offer(value);
    return new ArrayList<>(q);
}
```

**Tests and required behavior:** [1,5,2] must return [1,2,5], and [2,2,1] must return [1,2,2]. On the checked OpenJDK 21 implementation the first bad result is [1,5,2]; that observation is a reproducible witness, not a portable iterator-order promise. The portable corrected test asserts sortedness plus multiset equality. Empty returns [].

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

The queue only guarantees its next removal is first in priority.

</details>

<details>
<summary>Hint 2</summary>

Iteration exposes all entries without performing repeated ordered removal.

</details>

<details>
<summary>Hint 3</summary>

Obtain ordered output explicitly and count its cost; preserve the source if the contract requires it.

</details>

- [ ] **D7 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D8 — A waiting entry changes its own key

**Debugging · Medium.** Intended behavior: after second is reprioritized to 0, inspection should select second. Understand why this code does not implement that requested update.

```java
static final class MutableEntry {
    final int id;
    int priority;
    MutableEntry(int id, int priority) { this.id = id; this.priority = priority; }
}
static int dChangedKey() {
    PriorityQueue<MutableEntry> q = new PriorityQueue<>(
            Comparator.comparingInt(e -> e.priority));
    MutableEntry first = new MutableEntry(1, 3);
    MutableEntry second = new MutableEntry(2, 8);
    q.offer(first); q.offer(second);
    second.priority = 0;
    return q.peek().id;
}
```

**Tests and required behavior:** Intended return 2; checked OpenJDK 21 bad return 1. Construct a control that performs the same ranking before either object is offered and obtains 2. Then repair the update protocol and test two equal-priority records with distinct IDs. Explain why merely copying an invalid PriorityQueue is not a guaranteed rebuild.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

A referenced object can change without any queue operation running.

</details>

<details>
<summary>Hint 2</summary>

The representation assumed each queued key stayed stable.

</details>

<details>
<summary>Hint 3</summary>

Remove the intended entry before changing its key, then restore it through a queue operation; count the equality search.

</details>

- [ ] **D8 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D9 — A drain ignores its remaining size

**Debugging · Medium.** Intended contract: return every value ascending as a fresh list; any nonnull input, including empty and singleton. This specimen confuses the loop boundary and empty boxed results.

```java
static List<Integer> dOverPoll(int[] values) {
    PriorityQueue<Integer> q = new PriorityQueue<>();
    for (int value : values) q.offer(value);
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i <= values.length; i++) {
        int value = q.poll();
        result.add(value);
    }
    return result;
}
```

**Tests and required behavior:** [] must return []; [7] must return [7]; [3,1,1] must return [1,1,3]. Every bad call reaches an empty poll and unboxes null. Test the corrected empty case directly; replacing int with Integer alone would still append an unwanted null.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

The queue contains exactly as many occurrences as were offered.

</details>

<details>
<summary>Hint 2</summary>

A null return and an integer zero are different states.

</details>

<details>
<summary>Hint 3</summary>

Use a progress condition that agrees with the number of remaining entries, not an inclusive extra iteration.

</details>

- [ ] **D9 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### D10 — A cooldown ends one interval early

**Debugging · Medium.** Intended contract: length for count identical unit tasks with a common cooldown, all initially available. count>=0 and cooldown>=0, each<=100000; long arithmetic fits.

```java
static long dSingleLabelLength(int count, int cooldown) {
    long time = 0;
    long nextReady = 0;
    for (int i = 0; i < count; i++) {
        time = Math.max(time, nextReady);
        nextReady = time + cooldown;
        time++;
    }
    return time;
}
```

**Tests and required behavior:** count=3,cooldown=2 must return 7; count=2,cooldown=1 must return 3. Controls: (0,4)->0, (1,9)->1, (3,0)->3. Draw occupied intervals and legal next starts; verify the boundary against P9’s definition rather than patching the final total.

**Repair task:** identify the defect; construct a failing input; explain the violated invariant; correct your scratch implementation; test the fix against the witness, a control, and a boundary; derive time and space. Do not merely special-case the example. Keep the published specimen unchanged.

<details>
<summary>Hint 1</summary>

Cooldown counts intervening intervals, excluding the execution just performed.

</details>

<details>
<summary>Hint 2</summary>

Separate the task’s start time from the instant its own execution finishes.

</details>

<details>
<summary>Hint 3</summary>

Derive readyAt from the interval [t,t+1) before changing the time update.

</details>

- [ ] **D10 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.



## 10. Optional two-heap median workshop

This section is **OPTIONAL**, supporting L6’s C-tier Hard challenge. It is not required for ordinary readiness. Refer to the [optional median theory](./Heap_Priority_Queue_Essential_Concepts.md#15-two-heap-median-maintenance-optional) after trying the invariant exercise.

### O1 — Test both halves of a partition

**Reasoning and trace · Medium/advanced; optional.** No full median class implementation is required. Model a lower-half max-heap and an upper-half min-heap. At every observation boundary, require every lower value<=every upper value and either equal sizes or lower having exactly one extra entry. Each observation appears in exactly one half.

For arrivals [6,2,9,1,2,10], choose an insertion/rebalancing policy, describe it in words, and trace both half multisets, their roots, each transfer, and the median. Display lower descending and upper ascending as **multisets**, not internal arrays. Give an argument that the policy preserves order as well as sizes; explain which boundary value may cross when a half is too large.

| Arrival | Lower display | Upper display | Size condition | Order condition | Median |
| --- | --- | --- | --- | --- | --- |
| 6 | — | — | — | — | — |
| 2 | — | — | — | — | — |
| 9 | — | — | — | — | — |
| 1 | — | — | — | — | — |
| 2 | — | — | — | — | — |
| 10 | — | — | — | — | — |

**Checks:** prefix medians [6.0,4.0,6.0,4.0,2.0,4.0]; final lower [2,2,1], upper [6,9,10]. These outputs do not specify one mandatory internal heap layout or insertion policy. Empty median is absent for this workshop; [5] gives 5.0; [2,2] gives 2.0; [MAX,MAX] gives 2147483647.0; [MIN,MAX] gives -0.5.

Audit a proposed state lower=[9,8], upper=[1,2]: sizes match but it is not a valid ordered partition. Audit lower=[1,0,-1], upper=[2]: order holds but the size convention fails. Explain why either condition alone is insufficient.

**Completion:** handle odd/even sizes, ties on both sides, read-only retrieval, one-value insertion and rebalancing. Derive update/query costs including amortized capacity growth and O(t) persistent storage after t values. Show why widening must precede adding the two roots and why integer division would lose a half-unit. Compare with sorting each observed prefix. Do not copy a complete median implementation into this workbook.

<details>
<summary>Hint 1</summary>

The median lies at the boundary between ordered halves only when their sizes represent the middle ranks.

</details>

<details>
<summary>Hint 2</summary>

Moving an extreme boundary entry can repair size without reversing cross-half order.

</details>

<details>
<summary>Hint 3</summary>

Calculate an even-count average from a widened sum and a floating-point divisor; test both int endpoints.

</details>

- [ ] **O1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

## 11. Mock interviews

These are communication and design assessments, not two additional coding templates. Use a partner or record yourself. Start with a 35–45 minute practice slot and adjust to the actual interview format. Reuse a blank scratch file if the interviewer asks you to implement an already specified exercise; do not count its duplicate attempt as a new problem.

### M1 — Defend a retention contract

**Mock · Medium.** Begin with the P1 task but keep topic tags and notes hidden. The interviewer reveals [6,6,2,1], k=2 only after you state assumptions. Explain a baseline, a more selective design, the root meaning, the exact retained multiset after each prefix, and why rejection is safe. Then respond to these changes **before editing code**:

1. The caller now means distinct values rather than occurrences.
2. The caller wants any order, not descending output.
3. Yesterday’s observations can expire.
4. Records replace ints; score ties must favor the earliest arrival.
5. A single smallest batch is requested once, with k=1.

**Tests/checks:** original result [6,6]; distinct reinterpretation [6,2]; k=0 gives []; invalid k rejected. For expiration, use history [10,9,1], k=2, then remove 10: the required new rank is 1, which a permanently discarded candidate may no longer be able to supply. Do not claim the append-only proof covers this change.

**Completion:** give a 90-second correctness explanation, an operation-count cost derivation, a numeric safety test, and a reasoned alternative for at least one change. A changed contract may require new state; explaining that boundary is success. Record whether any hint was needed.

<details>
<summary>Hint 1</summary>

Name precisely what information the old contract allowed you to forget.

</details>

<details>
<summary>Hint 2</summary>

A new requirement can invalidate a discard proof even when the heap remains structurally valid.

</details>

<details>
<summary>Hint 3</summary>

Write a three-value counterexample for the changed workload before proposing a larger data structure.

</details>

- [ ] **M1 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

### M2 — Review three service workloads

**Mock · Medium.** An interviewer gives three workloads without data-structure tags: (a) repeated exact integer ID lookup and frequency deletion, (b) interleaving thousands of already chronological immutable files, and (c) exporting every unsorted record in chronological order once. Choose operations and structures for each, explaining preprocessing, mutable-key risks, total output, and the meaning of any root.

Then examine an employee’s claim: “Our next job has the greatest count, so it may run immediately despite having run in the previous interval.” Clarify the time model, separate legality from rank, and propose the state categories needed. Compare a length-only report with a complete timeline.

**Tests/checks:** (a) counts {7:2,9:1}, deleting one 7 leaves count 1; a priority tie cannot deduplicate it. (b) files [1,4],[],[1,3] must yield [1,1,3,4]; no source disappears because its current key ties another. (c) [4,1,3] must yield [1,3,4]. A repeated label at starts 0 and 2 is illegal with cooldown=2; starts 0 and 3 are legal.

**Completion:** defend one heap use and at least one alternative, then derive costs with independent input variables. Explain why a heap is not a lookup index or an optimality proof. End by identifying two tests that could falsify your proposed invariant. No memorized complete model answer is supplied.

<details>
<summary>Hint 1</summary>

Start from what the caller asks repeatedly, not from the word “priority.”

</details>

<details>
<summary>Hint 2</summary>

Already sorted sources expose useful boundaries; unsorted batches and equality lookups offer different information.

</details>

<details>
<summary>Hint 3</summary>

Tie each container to one required operation and give it an explicit memory bound.

</details>

- [ ] **M2 attempt** — Last: —; result: —; minutes: —; next review: —; mistake/lesson: —.

## 12. Revision and spaced repetition

Follow the roadmap’s [flexible review plan](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): approximately **1, 3, 7, and 14 days** after an attempt, adjusted for difficulty and retention. Dates belong in the existing exercise record or canonical curated Log; do not create another review ledger.

A **cold re-solve** starts from the statement and a blank editor with prior code, hints, answer checkpoints, and theory closed. Restate the contract, derive the invariant, implement, run tests, and explain costs before comparing with the previous attempt. For a trace, redraw the tree and recompute every movement. For a debugging task, first reproduce the failure, then repair without reading the previous correction. Change at least one tie or boundary fixture to prevent visual memorization.

Review in this order: substantial-review/unfinished S attempts; hint-assisted H attempts; wrong heap direction; incorrect retained-set proof; comparator and overflow errors; incomplete complexity accounting; unresolved empty/tie/cooldown cases. Record what the failed reasoning step was, not merely “careless.”

An independent re-solve can update the latest result to I, while the same record retains a short dated history. Curated history remains in the roadmap. A changed variant or unfamiliar mock on a separate session provides stronger R evidence than repeatedly reproducing the same code. If retention fails, reopen the relevant gate in the roadmap manually; this workbook does not update it.

## 13. Completion and readiness

These are evidence milestones, not a second progress dashboard. Use the exercise records for work details and the [common rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) for final assessment.

| Milestone | Demonstration and relevant evidence | Gate connection |
| --- | --- | --- |
| 1. Heap fundamentals | T1/F1: complete shape, min/max order, active size, indexes, height, distinction from BST/sorting | T |
| 2. Manual repairs | T2/T3/F2: explain every comparison/swap, one-child and singleton cases, and repair-route invariants | T + I |
| 3. Bottom-up construction | T4/F3/C2: derive the internal-node order and linear summed-height bound | T + I |
| 4. Java queue usage | Q1/J1: min/max creation, offer/poll/peek, null, duplicates, equality removal and unsorted iteration | I |
| 5. Comparator correctness | Q2/J2/P3/D3/D4/D8: safe primitive comparisons, full tie order, stable keys and distance bounds | T + I |
| 6. Bounded selection | P1–P3/T5: root as rejection threshold, discard proof, size bound, ordered output, k=1/k=n | I |
| 7. Streaming | P4/T6: warm-up, new maxima, smaller/tied arrivals, read-only queries, persistent memory | I |
| 8. Repeated priorities | P5/P6: transform only extracted state, finite event count, long totals | I |
| 9. Ordered merging | P7/P8/T7/C5: frontier proof, exhaustion, empty sources, ownership, a/k/N costs | I |
| 10. Essential practice | Independently solve or cold re-solve L1, L2, L3 with explanation and tests | P |
| 11. Additional breadth | L4/L5 as useful targeted practice; demonstrate static rank and availability concepts via core work even before completing every A problem | Supports T/I/P/R; A completion is not a blanket requirement |
| 12. Optional extension | O1/L6: both median invariants and safe averaging if pursued | Optional C; never blocks ordinary readiness |
| 13. Interview transfer | X exercises and M1/M2 with hidden tags, changed fixtures, and independent success across separate sessions | R |

Before requesting **Heap / Priority Queue Interview Ready**, demonstrate all of the following without notes:

- Explain complete array shape, parent/child bounds, heap order versus total sorting/BSTs, and why operations follow short routes.
- Hand-trace insertion and removal, including equal children, one child, singleton, and empty policy; derive bottom-up O(n) instead of repeating it.
- Use Java PriorityQueue independently; write safe comparators; distinguish equality, priority ties, arrival stability, and mutable keys.
- Choose a direction for an unseen selection/stream task, prove the retained-candidate invariant, and justify forgetting rejected values.
- Handle duplicates, k=1/k=n, invalid inputs where required, output order, numeric endpoints, and ownership.
- Derive total selection, merging, simulation, and streaming costs including construction, output, comparison costs, capacity growth, and persistent memory.
- Explain when sorting, scanning, hashing, binary search, or a direct two-source merge is preferable; treat Quickselect only as an optional comparison.
- Explain availability versus priority, a legal cooldown boundary, and why elapsed idle time matters without making all Additional platform completions mandatory.
- Complete unfamiliar variants independently across separate sessions and communicate a correctness argument during a mock.

Apply the gates precisely: explanations support **T**; tested blank-editor drills support **I**; independent Essential platform solves support **P**; repeated unfamiliar transfer plus the communication rubric support **R**. An exercise checkbox, a compilation result, a high problem count, or this prepared workbook earns no gate automatically. Record dated R evidence once in the [roadmap Heap section](./DSA_Interview_Roadmap.md#9-heap--priority-queue).

## 14. References and author validation

### Repository references

The actual paths below were discovered in this workspace. The Heap concepts document remains the primary theoretical reference; links to earlier topics cover prerequisites rather than duplicating their theory.

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Heap scope and curated list](./DSA_Interview_Roadmap.md#9-heap--priority-queue), [study strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), and [readiness](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Heap / Priority Queue concepts](./Heap_Priority_Queue_Essential_Concepts.md): [linear heapify](./Heap_Priority_Queue_Essential_Concepts.md#7-bottom-up-heapify), [streaming](./Heap_Priority_Queue_Essential_Concepts.md#11-streaming-rank), [simulation](./Heap_Priority_Queue_Essential_Concepts.md#12-repeated-priority-simulation), [merging](./Heap_Priority_Queue_Essential_Concepts.md#13-merging-ordered-streams), [scheduling](./Heap_Priority_Queue_Essential_Concepts.md#14-scheduling-and-availability), and [alternatives](./Heap_Priority_Queue_Essential_Concepts.md#17-choosing-an-alternative).
- [Trees](./Trees/Trees_Essential_Concepts.md), [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md), [Binary Search](./Binary_Search/Binary_Search_Essential_Concepts.md), and [Linked List](./Linked_List/Linked_List_Essential_Concepts.md).
- [Java Arrays](./Java_Array.md), [Collections/List](./Java_List_ArrayList_LinkedList.md), [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), and [Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), particularly [PriorityQueue](./Complexity/Complexity_Analysis_and_Foundations.md#148-priorityqueue).
- The roadmap’s [Linked List category](./DSA_Interview_Roadmap.md#6-linked-list) contains the optional [Merge k Sorted Lists](https://leetcode.com/problems/merge-k-sorted-lists/) bridge. It is not a seventh Heap curated entry and does not require a solution here.

### Java and platform sources

Java 21 API details were checked through Context7. The [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html) supports the natural/comparator ordering, duplicate, empty, equality-search, and iteration contracts used in the drills. The [Queue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Queue.html) distinguishes null-returning from throwing operations. The [Comparator API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html) specifies sign consistency and safe key composition.

Construction and capacity observations are identified as [OpenJDK 21 implementation details](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/PriorityQueue.java), rather than portable promises of a particular internal layout or growth factor. The six official problem pages linked in Level 4 were checked **2026-10-29** for titles and difficulty; roadmap E/A/C classifications remain authoritative. No full platform statements or solutions are reproduced.

### Author validation

Completed **2026-10-30**. The workbook contains **14 sections**, **49 original exercises** (including **19 coding exercises**), **six curated problems** in exact E/E/E/A/A/C order, **117 optional hints**, and **55 unchecked attempt records**.

All **37 Java blocks** were assembled and compiled as written using Java 21 with `--release 21 -Xlint:all -Werror`. Runtime checks passed **123,454 assertions**, covering **1,174 small array cases**, primitive and multi-field comparator policies, distance arithmetic, empty API behavior, the executable complexity specimens, and **36 debugging witness/control checks** across all ten defective challenges. A 100000-element construction/drain check observed 99990 downward build levels, while decreasing repeated insertion used 1468946 comparisons; these are logical operation counts, not hardware benchmarks.

For analysis wrappers that depend on unimplemented starters, only their prerequisite helpers were supplied by separate test fixtures: local sift-down, merging, stream rank, and batch reduction. The published wrapper and buggy code were retained unchanged. The sorted-history/scan fixtures validate wrapper outputs; their internal performance is not evidence for the target complexity of the learner’s future implementation.

Independent sorting, scanning, frequency-counting, wide-integer, and availability oracles verified **all 149 coding test rows**, plus **30 trace and prose checkpoints**. A separate audit parsed the rendered table outputs and checked **three ASCII tree diagrams**. All **64 relative links**, including heading anchors, resolve. SHA-256 comparison confirmed all **35 pre-existing guide files** remained unchanged; **Heap_Priority_Queue_Exercises.md** is the only added file in DSA_Guide. No missing dependency or unresolved validation issue remains.

These checks validate the workbook’s contracts, expected results, executable analysis specimens, and deliberately faulty witnesses. **Coding starters remain TODOs.** Compilation of a starter is not evidence that a learner’s future implementation passes its tests. Independent test oracles were kept outside the repository, and no solutions file was added.
