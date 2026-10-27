# Linked List — Exercises (Java 21)

## 1. Introduction

This workbook complements [Linked List — Essential Concepts](./Linked_List_Essential_Concepts.md). The [roadmap's Linked List section](./DSA_Interview_Roadmap.md#6-linked-list) determines learning objectives, E/A/C priorities, and readiness. Use this workbook to derive and test node operations from their contracts, then select methods on unfamiliar statements.

There are **42 original exercises**: 24 implementation exercises, 8 complexity investigations, and 10 debugging challenges. There are also **8 curated platform problems**. Several related methods under one ID form one exercise and share one attempt record.

| Level or practice group | IDs | Purpose |
| --- | --- | --- |
| Level 1: fundamentals | F1–F4 | Build nodes, inspect identities, edit neighbors, maintain endpoints |
| Level 2: node manipulation | M1–M2, R1–R3 | Reconnect, extract, reverse, and delete safely |
| Level 2: coordinated references | P1–P3, G1–G4 | Midpoints, cycles, gaps, merge, split, reorder, and copy |
| Level 2: doubly linked state | L1–L3 | Local sentinel operations, map consistency, complete LRU behavior |
| Level 3: curated practice | N1–N8 | Exact roadmap E/A/C selections |
| Level 4: unfamiliar challenges | X1–X5 | Choose an algorithm from the required behavior |
| Supporting investigations | C1–C8, D1–D10 | Derive total costs and repair reproducible structural defects |

Prerequisites are Java references, arrays, maps/sets, complexity analysis, Two Pointers, and the concepts guide. No core challenge requires heaps, trees, graphs, or dynamic programming. The cycle-entry part of P2 is optional; N8 is an optional Hard challenge whose heap-based approach waits for Heap / Priority Queue. Core LRU reasoning is required even though the platform LRU problem has Additional priority.

### How to use the workbook

Read the contract and examples with hints closed. Draw identities before writing code, propose a correct baseline, and then justify any improvement. For Medium exercises, three collapsed hints move from an observation to a state clue and then an implementation clue. Reading one changes the attempt to H. The workbook supplies signatures and diagnostic subjects; it does not supply completed coding-task or platform solutions.

Reuse the [Java List reference](./Java_List_ArrayList_LinkedList.md), [Map reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), [Two Pointers proof method](./Two_Pointers/Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements), and [complexity process](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) when a concrete gap appears. Avoid rereading entire prerequisite documents before making an attempt.

### Attempt records

Every exercise has an unchecked record: last date, result, minutes including explanation/testing, next review, and main lesson. Checking a box records a completed attempt and postmortem; it does not imply independent success.

| Result | Meaning |
| --- | --- |
| I | Independently derived, implemented or analyzed, explained, and tested |
| H | Needed a conceptual hint |
| S | Needed substantial solution review, or could not finish |
| — | Not attempted |

For N1–N8, keep the detailed date/result/minutes/review/lesson history in that problem's **existing roadmap Log**. The workbook's checkbox and record pointer identify that one record; do not maintain a second history. Topic-level [T/I/P/R](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remains in the roadmap. All original-exercise records live here, with review dates on the same lines.

### Navigation

- [Workflow, contracts, and types](#2-problem-solving-workflow)
- [Level 1](#3-level-1-fundamentals)
- [Node manipulation](#4-level-2-node-manipulation)
- [Reversal and deletion](#5-reversal-and-dummy-node-drills)
- [Fast/slow and fixed gaps](#6-fastslow-and-fixed-gap-drills)
- [Merging and composition](#7-merging-splitting-reordering-and-copying)
- [Doubly linked lists and LRU](#8-doubly-linked-list-and-lru-drills)
- [Curated problems](#9-level-3-curated-platform-problems)
- [Unfamiliar challenges](#10-level-4-unfamiliar-interview-challenges)
- [Complexity investigations](#11-complexity-analysis-exercises)
- [Debugging](#12-debugging-challenges)
- [Mock interviews](#13-mock-interview-practice)
- [Review](#14-revision-and-spaced-repetition)
- [Milestones](#15-completion-and-readiness)
- [References and delayed cost checks](#16-references-and-delayed-complexity-checks)

## 2. Problem-Solving Workflow

1. Restate the required output, including identity, order, mutation, and allocation rules.
2. Label distinct objects A, B, C even when their values match.
3. Draw the initial head/tail/local references and all relevant next/prev/random edges.
4. Describe a correct straightforward approach and its costs.
5. Identify the repeated search or extra storage you want to avoid.
6. Choose a method and state the meaning of every reference at a specific loop boundary.
7. Before replacing an edge, identify where its old target remains reachable.
8. Prove initialization, preservation, progress, and the final result.
9. Implement in Java 21.
10. Test values **and identities**, null boundaries, detached nodes, and restored links.
11. Derive total costs, including searches, validation, recursion, maps, allocations, and output.
12. Explain which changed assumption would invalidate your proof.

For every structural mutation in F3–F4, M1–M2, R1–R3, P3, G1–G4, and L1–L3, submit a **reference sheet**: references before the operation; their node identities; fields that change; retained continuations; possible lost nodes/cycles; the invariant; and why the final structure meets the contract. X1–X5 first require independent method selection; then submit the corresponding proof, using this sheet when links change.

Use a trace table such as:

| Step | Local references before | Saved continuation | Field changed | Reachable chains afterward |
| --- | --- | --- | --- | --- |

A moving cursor proves progress only when it approaches a stated stopping condition. It does not by itself prove that every node survives or that the result order is correct. See [reference semantics](./Linked_List_Essential_Concepts.md#3-java-references-and-node-identity) and [patterns and invariants](./Linked_List_Essential_Concepts.md#15-deriving-patterns-and-proving-invariants).

### Shared contracts: part of every original exercise

Unless the exercise explicitly overrides a rule:

- A list is an ordinary finite, acyclic, null-terminated custom-node chain. Empty input is allowed and represented by null. Input arrays/maps/operation collections themselves are nonnull.
- Total distinct input nodes are at most 100,000. Values use the full signed int range; negatives and duplicates are allowed. Lists are **not assumed sorted** except where stated. MIN/MAX mean Integer.MIN_VALUE/MAX_VALUE.
- Input values and keys remain unchanged, except owner insertion or cache value updates explicitly requested. Output identities refer to the same input objects unless the exercise explicitly requests fresh nodes.
- Structural mutation permission and allocation requirements are stated in each exercise. A single returned record is O(1) output storage. A constant number of temporary sentinels is allowed only when stated; it does not permit replacement data nodes.
- No concurrent editing occurs. Destructive inputs have exclusive structural ownership; multiple input chains are node-disjoint unless sharing is explicitly permitted.
- Recursion is forbidden for the target implementation unless explicitly allowed or required. This makes an O(1) auxiliary target include the call stack.
- A promise such as sortedness, disjointness, or membership need not be verified by an extra scan. Invalid parameters with an explicit exception contract **must** be rejected; all such rejections leave original input links/values unchanged.
- Auxiliary space excludes existing input and required returned output but includes working maps, reference arrays, copies, and recursive frames. Explain both auxiliary and output storage.
- Hashing bounds are expected under suitable dispersion and constant-size key operations; insertions may need amortized analysis. Count constant setup for empty cases even when a table abbreviates O(n).
- n denotes the input chain length, m a second length, q query/operation count, c cache capacity, and μ/λ the noncycle-prefix/cycle lengths. Exercise-specific scalar parameters may have other names.

### Identity notation and fixtures

`[A:7,B:7,C:-2]` means A(7) → B(7) → C(−2) → null. A and B are different objects. `[A,B]` in an output gives identity order; values remain as specified in the input. `[]` means a null head, or an empty array/list where the output type requires one. Every table row starts with fresh independent fixtures unless it explicitly gives an operation sequence.

`last->B` changes the last next edge to the existing B object, creating a cycle. `random=[C,null,A]` lists the random targets of A,B,C in that order. A prime, such as A', labels a fresh copy corresponding to A; it is not a field you add to the class.

For doubly linked state, `[A:4/40,B:7/70]` means node A has key 4/value 40 and node B has key 7/value 70. Persistent sentinel identities are H and T:

```text
H <-> A <-> B <-> T
      MRU   LRU

H.prev = null; T.next = null
empty: H.next = T and T.prev = H
```

Map displays show exact key-to-node identities, not iteration order. A returned `null` cache value means a miss; stored −1 is valid data.

### Shared Java 21 types

Place all member snippets inside a class such as `Practice`, with `import java.util.*;` before the class. Recreate the node definitions from memory for F1, then use these consistent types. They deliberately have identity equality and mutable links.

```java
static final class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}
static final class RandomNode {
    int val;
    RandomNode next;
    RandomNode random;
    RandomNode(int val) { this.val = val; }
}
static final class DNode {
    final int key;
    int value;
    DNode prev;
    DNode next;
    DNode(int key, int value) { this.key = key; this.value = value; }
}
public record Inspection(int count, ListNode firstMatch, ListNode last) {}
public record Endpoints(ListNode head, ListNode tail) {}
public record Cut(ListNode remaining, ListNode extracted) {}
public record MiddlePair(ListNode first, ListNode second) {}
public record Removal(ListNode head, ListNode removed) {}
public record Split(ListNode left, ListNode right) {}
public record Rail(DNode front, DNode back) {}
```

A starter's UnsupportedOperationException means unfinished work. C snippets are analysis subjects that may call these unfinished methods under explicitly stated contracts; D snippets are deliberately flawed repair subjects. Neither group supplies a completed solution to the implementation exercises.

### Structural validation requirements

Use a bounded traversal or identity-based visited set in test code. Never run an unguarded printer on cyclic or suspect output. For an in-place result, compare the observed identity sequence against an independently constructed expected permutation/subsequence; verify values, null termination, and any detached victim links. Keep external fixture references so you can inspect removed nodes.

For deep copies, verify one fresh node per original, disjoint identity sets, translated next/random edges, and original links unchanged. For doubly linked structures, traverse both directions and verify every adjacent pair. For LRU, check that each map entry refers to its one linked node and every real node has a map entry, with the correct capacity and recency order.

Test harnesses may use extra storage even when the algorithm's target is O(1). A reference array is a useful independent oracle for order. Do not count test infrastructure as the candidate algorithm's auxiliary storage. Use [the testing guide](./Testing_Essential_Concepts.md) for broader testing principles.

## 3. Level 1: Fundamentals

### F1. Build a fresh chain

**Difficulty:** Easy. **Objective:** Create custom nodes and connect a null-terminated sequence without aliasing equal values. **Prerequisites:** Java constructors; [node types](./Linked_List_Essential_Concepts.md#4-singly-and-doubly-linked-nodes).

Given a nonnull int[] of length n, return a chain containing those values in array order. Allocate exactly n fresh data nodes. Return null for an empty array. The array is read-only; every result node must be a distinct object, including repeated values. No cycle or sortedness assumption applies to the array. Recursion is forbidden; use O(1) working references beyond the output.

```java
public static ListNode buildChain(int[] values) {
    // TODO: Construct the requested fresh identities and links.
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `values=[3,3,-1]` | `values=[3,3,-1]; fresh nodes=3` |
| `values=[]` | `head=[]; fresh nodes=0` |
| `values=[0]` | `values=[0]; fresh nodes=1` |
| `values=[MIN,MAX]` | `values=[MIN,MAX]; fresh nodes=2` |

**Targets:** O(n+1) time, O(1) auxiliary, O(n) fresh returned nodes.

**Edge checks:** Empty/singleton input, duplicates, negative/extreme values; the final next must be null.

**Complete when:** Create the class yourself, pass every example, prove that each allocation contributes exactly one output position, and explain why assigning another reference to a node does not clone it. **Mock follow-up:** If you keep only a head reference and rescan for every append, what total work results?

- [ ] **F1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F2. Inspect a chain without changing it

**Difficulty:** Easy. **Objective:** Combine traversal, counting, first-match search, and tail observation while preserving identities. **Prerequisites:** F1; [basic operations](./Linked_List_Essential_Concepts.md#5-basic-operations-and-reachability).

Given head and an int target, return Inspection(count, firstMatch, last). firstMatch is the earliest node whose value equals target, or null; last is the final node, or null for empty input. The unsorted acyclic chain may contain duplicates. Do not mutate or allocate data nodes. Return (0,null,null) for empty input. No recursion.

```java
public static Inspection inspectChain(ListNode head, int target) {
    // TODO: Return observations about the original objects.
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:4,B:9,C:4]; target=4` | `count=3; first=A; last=C` |
| `chain=[A:4,B:9,C:4]; target=8` | `count=3; first=null; last=C` |
| `chain=[]; target=0` | `count=0; first=null; last=null` |
| `chain=[A:-2]; target=-2` | `count=1; first=A; last=A` |
| `chain=[A:7,B:7]; target=7` | `count=2; first=A; last=B` |

**Targets:** O(n+1) time, O(1) auxiliary and one constant-size output record.

**Edge checks:** Empty, matching singleton, missing target, duplicate matches, two equal-valued nodes.

**Complete when:** Verify all original next fields are unchanged and firstMatch uses the earliest identity rather than a newly allocated equal-valued node. **Mock follow-up:** Would assigning current=current.next change head.next?

- [ ] **F2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F3. Edit after a known node

**Difficulty:** Easy. **Objective:** Preserve the old successor during insertion and detach a removed neighbor. **Prerequisites:** F1–F2 and the reference sheet.

Implement insertAfter and removeAfter. A nonnull predecessor belongs to an unsorted acyclic chain. Insertion allocates one fresh X with the requested value immediately after it and returns X. Removal returns its old successor after bypassing it and setting the removed node's next to null; if no successor exists, return null with no change. A null predecessor throws IllegalArgumentException. Preserve all other identities and values. No recursion.

```java
public static ListNode insertAfter(ListNode predecessor, int value) {
    throw new UnsupportedOperationException();
}
public static ListNode removeAfter(ListNode predecessor) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `op=insert; chain=[A:2,B:2]; after=A; value=-1` | `chain=[A,X,B]; returned=X; X.val=-1` |
| `op=insert; chain=[A:2]; after=A; value=5` | `chain=[A,X]; returned=X; X.val=5` |
| `op=remove; chain=[A:2,B:2,C:5]; after=A` | `chain=[A,C]; returned=B; B.next=null` |
| `op=remove; chain=[A:2,B:2]; after=A` | `chain=[A]; returned=B; B.next=null` |
| `op=remove; chain=[A:2]; after=A` | `chain=[A]; returned=null` |
| `op=insert/remove; predecessor=null` | `throw IllegalArgumentException` |

**Targets:** O(1) time and auxiliary per operation; insertion adds one data node, removal adds none.

**Edge checks:** Tail insertion, middle/tail removal, no successor, null predecessor, duplicate values.

**Complete when:** Trace every changed edge, show the saved continuation, and validate detached victims through external fixture references. **Mock follow-up:** How would a separate owner tail field need to change after editing the old tail?

- [ ] **F3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### F4. Maintain head and tail ownership

**Difficulty:** Medium. **Objective:** Keep endpoint metadata consistent across insertion and deletion. **Prerequisites:** F1–F3; [Java reference ownership](./Linked_List_Essential_Concepts.md#3-java-references-and-node-identity).

Complete ChainOwner. A new owner starts empty. addFirst/addLast allocate exactly one new node; removeFirst/removeLast return the removed original node, detached with next=null, or null on empty state. Maintain head, tail, and size after every call. Values may repeat; links are acyclic and unsorted. The owner exclusively owns its nodes. No arrays, maps, recursion, or replacement nodes. In traces, F(v)/L(v) mean addFirst/addLast and RF/RL mean removeFirst/removeLast. Newly allocated identities are A,B,C,... in allocation order within each row.

```java
static final class ChainOwner {
    ListNode head;
    ListNode tail;
    int size;
    void addFirst(int value) { throw new UnsupportedOperationException(); }
    void addLast(int value) { throw new UnsupportedOperationException(); }
    ListNode removeFirst() { throw new UnsupportedOperationException(); }
    ListNode removeLast() { throw new UnsupportedOperationException(); }
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `ops=RF,RL` | `removed=[null,null]; chain=[]; head=null; tail=null; size=0` |
| `ops=L(5),L(5),F(-1)` | `removed=[]; chain=[C,A,B]; head=C; tail=B; size=3` |
| `ops=L(5),L(5),F(-1),RF,RL` | `removed=[C,B]; chain=[A]; head=A; tail=A; size=1` |
| `ops=F(0),RL` | `removed=[A]; chain=[]; head=null; tail=null; size=0` |
| `ops=L(1),RF,L(2)` | `removed=[A]; chain=[B]; head=B; tail=B; size=1` |

**Targets:** O(1) time for addFirst, addLast, and removeFirst; O(n+1) for removeLast on a singly linked chain. O(1) auxiliary per operation; maintained nodes O(size).

**Edge checks:** Empty reads/removals, the only node, two equal-valued nodes, deletion followed by append.

**Complete when:** Show head==null iff tail==null iff size==0, and otherwise tail.next==null. Check size against an independent traversal after every operation. **Mock follow-up:** Why does holding tail not reveal its predecessor?

<details>
<summary>Hint 1</summary>

Separate the empty and nonempty endpoint relationships before considering an interior traversal.

</details>

<details>
<summary>Hint 2</summary>

An operation that removes the only node changes both endpoints, even if it is named for only one end.

</details>

<details>
<summary>Hint 3</summary>

Keep a local handle to a victim before detaching it; derive how the owner fields should describe the surviving chain.

</details>

- [ ] **F4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 4. Level 2: Node Manipulation

Reconnect retained fragments before attempting more complex extraction. A lost edge can be repaired when a suitable reference survives; arbitrary lost objects cannot be recovered from their former values alone.

### M1. Reconnect retained fragments

**Difficulty:** Easy. **Objective:** Join two retained chains and distinguish a missing edge from a missing reference. **Prerequisites:** F2–F3; [reachability](./Linked_List_Essential_Concepts.md#5-basic-operations-and-reachability).

Given first and second, return their concatenation in that order, reusing all nodes. Both chains are unsorted, acyclic, and node-disjoint; either may be empty. The second head may be a saved reference to a suffix accidentally disconnected earlier. You have no tail metadata. Change only the link needed to join the chains; allocate no data or sentinel nodes and use no recursion. If both are empty return null. Explain separately why a suffix cannot generally be recovered when every reference to it has been lost.

```java
public static ListNode reconnect(ListNode first, ListNode second) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `first=[A:1,B:1]; second=[C:-2,D:3]` | `head=[A,B,C,D]` |
| `first=[]; second=[C:2]` | `head=[C]` |
| `first=[A:2]; second=[]` | `head=[A]` |
| `first=[]; second=[]` | `head=[]` |
| `first=[A:4]; second=[B:4]` | `head=[A,B]` |

**Targets:** O(n+1) worst-case time for n nodes in first, O(1) auxiliary; no new nodes. Explain any empty-input shortcut.

**Edge checks:** Both empty, either empty, singleton fragments, equal values on separate identities.

**Complete when:** Show every input identity appears once and null-termination remains valid. Draw a broken A→B / saved C→D fixture and its repair. **Mock follow-up:** How would supplying a trusted first-tail handle change the cost?

- [ ] **M1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### M2. Extract a half-open segment

**Difficulty:** Medium. **Objective:** Preserve both surviving continuations while transferring a range into a separate chain. **Prerequisites:** F3, M1, and [sentinels](./Linked_List_Essential_Concepts.md#6-dummy-and-sentinel-nodes).

Given head and indices from,to, return Cut(remaining,extracted). Remove original positions [from,to) from the unsorted acyclic chain, preserving order in both result chains. Reuse every input node exactly once across the two disjoint results. Both tails must end at null. Empty ranges return the unchanged head and extracted=null. Require 0<=from<=to<=n; otherwise throw IllegalArgumentException before any input link changes. Up to one temporary dummy is allowed, but no new data nodes or recursion.

```java
public static Cut extractRange(ListNode head, int from, int to) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:2,D:3,E:4]; from=1; to=4` | `remaining=[A,E]; extracted=[B,C,D]` |
| `chain=[A:1,B:2,C:3]; from=0; to=2` | `remaining=[C]; extracted=[A,B]` |
| `chain=[A:1,B:2,C:3]; from=1; to=3` | `remaining=[A]; extracted=[B,C]` |
| `chain=[A:1,B:2]; from=0; to=2` | `remaining=[]; extracted=[A,B]` |
| `chain=[A:1,B:2]; from=1; to=1` | `remaining=[A,B]; extracted=[]` |
| `chain=[]; from=0; to=0` | `remaining=[]; extracted=[]` |
| `chain=[A:1]; from=0; to=2` | `throw IllegalArgumentException` |
| `chain=[A:1]; from=1; to=0` | `throw IllegalArgumentException` |

**Targets:** O(n+1) worst-case time including required validation, O(1) auxiliary and one output record.

**Edge checks:** Head/tail/whole-chain extraction, empty interval, empty chain, negative or reversed/out-of-range bounds.

**Complete when:** Check disjoint output identity sets whose union is the input set; inspect both tail links and prove rejection performs no partial edit. **Mock follow-up:** Why is keeping the extracted head alone insufficient to reconnect the remaining chain?

<details>
<summary>Hint 1</summary>

Think of three consecutive regions: retained prefix, extracted segment, retained suffix.

</details>

<details>
<summary>Hint 2</summary>

Identify the incoming edge before the segment and the outgoing edge after it, including absent boundary regions.

</details>

<details>
<summary>Hint 3</summary>

Decide which bounds must be established before your first destructive write; save both result entry references.

</details>

- [ ] **M2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 5. Reversal and Dummy-Node Drills

Use [the reversal and sentinel theory](./Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal) after an attempted derivation. Keep iterative and recursive space claims distinct.

### R1. Reverse and report both endpoints

**Difficulty:** Medium. **Objective:** Derive iterative reversal with explicit ownership of the new head and tail. **Prerequisites:** F3, M1; [iterative reversal](./Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal).

Reverse an unsorted acyclic chain in place and return Endpoints(newHead,newTail). Reuse all nodes, preserve values, and allocate no data/sentinel nodes. Empty input returns (null,null); a singleton is both endpoints. Recursion and growing containers are forbidden. Draw the reversed and unprocessed portions after each iteration on a three-node input.

```java
public static Endpoints reverseWithEnds(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:4,B:4,C:-1]` | `head=[C,B,A]; tail=A` |
| `chain=[]` | `head=[]; tail=null` |
| `chain=[A:0]` | `head=[A]; tail=A` |
| `chain=[A:1,B:2]` | `head=[B,A]; tail=A` |
| `chain=[A:1,B:2,C:3,D:4]` | `head=[D,C,B,A]; tail=A` |

**Targets:** O(n+1) time, O(1) auxiliary and one constant-size result record.

**Edge checks:** Empty, singleton, two nodes, odd/even lengths, duplicate values.

**Complete when:** Prove that the two portions partition the original identities and that the returned tail.next is null. Verify exact identity reversal, not only reversed values. **Mock follow-up:** If the caller ignores the returned head, which original node does its variable still reference?

<details>
<summary>Hint 1</summary>

A processed prefix and an unprocessed suffix can together account for every original node.

</details>

<details>
<summary>Hint 2</summary>

After replacing one next field, the untouched suffix still needs an entry reference.

</details>

<details>
<summary>Hint 3</summary>

Audit the order in which you save a continuation, change a link, and advance local references.

</details>

- [ ] **R1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### R2. Reverse through a recursive contract

**Difficulty:** Medium. **Objective:** Explain the smaller-chain promise, unwinding, and stack-space cost. **Prerequisites:** R1, recursion foundations, and [recursive reversal](./Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal).

Return the reversed head of an acyclic unsorted chain using recursion. For this learning exercise, n<=200; values may repeat and head may be null. Reuse every node and preserve values; allocate no data or sentinel nodes. Empty input returns null. You may use O(n) stack frames; an O(1) auxiliary claim would be incorrect. Draw calls and returned chains for a three-node case.

```java
public static ListNode reverseRecursively(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:2,B:2,C:9]` | `head=[C,B,A]` |
| `chain=[]` | `head=[]` |
| `chain=[A:-1]` | `head=[A]` |
| `chain=[A:4,B:5]` | `head=[B,A]` |

**Targets:** O(n+1) time, O(n+1) auxiliary stack, no new data nodes.

**Edge checks:** Null before any dereference, singleton base case, two-node backward/forward links, duplicate identities.

**Complete when:** State the recursive postcondition, identify the live frames, and show that each completed frame leaves a null-terminated result without an accidental cycle. **Mock follow-up:** Why is an iterative version preferable for a 100,000-node chain?

<details>
<summary>Hint 1</summary>

Specify what reversing the suffix should return before writing the caller frame.

</details>

<details>
<summary>Hint 2</summary>

The caller still has a relationship to its original immediate successor after the smaller call returns.

</details>

<details>
<summary>Hint 3</summary>

Inspect both the new backward connection and the old forward connection during unwinding.

</details>

- [ ] **R2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### R3. Remove every selected value

**Difficulty:** Medium. **Objective:** Use uniform predecessor reasoning across repeated head and interior deletions. **Prerequisites:** F3 and [dummy nodes](./Linked_List_Essential_Concepts.md#6-dummy-and-sentinel-nodes).

Remove every node whose value equals target from an unsorted acyclic chain. Return the surviving head in original order. Reuse surviving nodes; set next=null on every removed node, which the test retains through external references. Empty input or removing all nodes returns null. Up to one dummy is allowed; no new data nodes, arrays/maps, or recursion. Do not change values.

```java
public static ListNode removeAll(ListNode head, int target) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:2,B:2,C:3,D:2,E:4]; target=2` | `head=[C,E]; detached=[A,B,D]` |
| `chain=[A:7,B:7]; target=7` | `head=[]; detached=[A,B]` |
| `chain=[A:1,B:2]; target=9` | `head=[A,B]; detached=[]` |
| `chain=[]; target=0` | `head=[]; detached=[]` |
| `chain=[A:-1,B:0,C:-1]; target=-1` | `head=[B]; detached=[A,C]` |

**Targets:** O(n+1) time and O(1) auxiliary, including the optional dummy.

**Edge checks:** Consecutive matches at the head, consecutive interior matches, tail match, no match, all matches.

**Complete when:** Prove that every original node is inspected and that removal does not cause the following candidate to be skipped; verify every victim's next independently. **Mock follow-up:** When should a predecessor advance, and when should it retain its position?

<details>
<summary>Hint 1</summary>

Head matches and interior matches should satisfy the same survivor rule.

</details>

<details>
<summary>Hint 2</summary>

Define which candidate is first unclassified relative to the predecessor.

</details>

<details>
<summary>Hint 3</summary>

After removing a candidate, check whether its replacement at that position has been inspected yet.

</details>

- [ ] **R3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 6. Fast/Slow and Fixed-Gap Drills

Explain speed for midpoint/cycle tasks and distance for fixed-gap tasks. These are different invariants even though each uses multiple references.

### P1. Report both middle conventions

**Difficulty:** Medium. **Objective:** Derive null-safe midpoint guards and distinguish even-length choices. **Prerequisites:** F2; [midpoints](./Linked_List_Essential_Concepts.md#8-finding-the-middle-with-fast-and-slow-references).

Return MiddlePair(first,second) for an unsorted acyclic chain. On odd n both fields are the same middle identity; on even n>0 they are the earlier and later middle identities. Empty input returns (null,null). Do not mutate or allocate nodes. Derive coordinated references without counting the full length first, indexing, containers, or recursion.

```java
public static MiddlePair middlePair(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[]` | `first=null; second=null` |
| `chain=[A:7]` | `first=A; second=A` |
| `chain=[A:7,B:7]` | `first=A; second=B` |
| `chain=[A:1,B:2,C:3]` | `first=B; second=B` |
| `chain=[A:1,B:2,C:3,D:4]` | `first=B; second=C` |
| `chain=[A:1,B:2,C:3,D:4,E:5]` | `first=C; second=C` |

**Targets:** O(n+1) time, O(1) auxiliary and one result record.

**Edge checks:** Lengths 0–5, equal-valued central nodes, safe evaluation of every next dereference.

**Complete when:** Explain positions after t iterations, the loop guard, the ending parity information, and why every original edge is unchanged. **Mock follow-up:** Which convention gives a left half that is at least as long as the right half?

<details>
<summary>Hint 1</summary>

Even length creates a pair of central positions, so one arbitrary middle answer is insufficient.

</details>

<details>
<summary>Hint 2</summary>

Track the relationship between distances traveled and retain enough neighboring information for both outputs.

</details>

<details>
<summary>Hint 3</summary>

Trace lengths 0,1,2 before committing to a guard or initialization.

</details>

- [ ] **P1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### P2. Inspect cyclic reachability

**Difficulty:** Medium. **Objective:** Distinguish repeated identities from repeated values and prove Floyd's meeting argument. **Prerequisites:** P1; [cycle detection](./Linked_List_Essential_Concepts.md#9-cycle-detection-and-an-optional-entry-extension).

detectCycle returns whether following next from head revisits a node. Input may be null, acyclic, or have one reachable cycle; there are at most 100,000 distinct reachable nodes and values may repeat. Do not mutate or allocate nodes, use recursion, or use a visited container in the target implementation. Return false for null. First describe a HashSet baseline, then meet O(1) auxiliary space. The separate cycleEntryOptional method is an optional extension: return the entry identity or null; it does not block completion of the core boolean task.

```java
public static boolean detectCycle(ListNode head) {
    throw new UnsupportedOperationException();
}
public static ListNode cycleEntryOptional(ListNode head) {
    // Optional extension; leave unfinished until the core proof is reliable.
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[]` | `cycle=false; optional entry=null` |
| `chain=[A:7]` | `cycle=false; optional entry=null` |
| `chain=[A:7,B:7,C:7]` | `cycle=false; optional entry=null` |
| `chain=[A:1]; last->A` | `cycle=true; optional entry=A` |
| `chain=[A:1,B:2]; last->A` | `cycle=true; optional entry=A` |
| `chain=[A:1,B:2,C:3,D:4,E:5]; last->C` | `cycle=true; optional entry=C` |

**Targets:** O(n+1) time for acyclic input, O(μ+λ+1) for cyclic input, O(1) auxiliary for both target methods.

**Edge checks:** Empty, finite singleton, self-cycle, cycle at head, cycle after a prefix, duplicates without a cycle.

**Complete when:** Explain one-step/two-step movement, safe guards, why null proves termination, and why initial equality is not cycle evidence. Use a bounded test observer. **Mock follow-up:** Why need the first meeting identity not be the entry?

<details>
<summary>Hint 1</summary>

A repeated value does not identify a repeated structural position.

</details>

<details>
<summary>Hint 2</summary>

Once both references are inside a cycle, analyze their relative offset modulo its length.

</details>

<details>
<summary>Hint 3</summary>

Separate initialization from positive movement; for the optional extension, derive the reset condition from traveled distances before coding it.

</details>

- [ ] **P2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### P3. Select and remove by distance from the tail

**Difficulty:** Medium. **Objective:** Maintain a fixed gap and distinguish a target from its predecessor. **Prerequisites:** F3, R3; [fixed-gap reasoning](./Linked_List_Essential_Concepts.md#10-fixed-gap-references-and-tail-relative-removal).

Implement nthNode and eraseNth on an acyclic unsorted chain. k is one-based from the tail. nthNode returns the existing target without mutation. eraseNth returns Removal(newHead,removed), detaching the target with next=null and reusing every survivor. Require 1<=k<=n; otherwise throw IllegalArgumentException before changing input links, including on an empty chain. Use a fixed-gap traversal rather than a full length-count pass. No recursion or growing containers; removal may allocate one dummy.

```java
public static ListNode nthNode(ListNode head, int k) {
    throw new UnsupportedOperationException();
}
public static Removal eraseNth(ListNode head, int k) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:2,D:3,E:4]; k=2` | `selected=D; head after removal=[A,B,C,E]; removed=D` |
| `chain=[A:1,B:2,C:3]; k=3` | `selected=A; head after removal=[B,C]; removed=A` |
| `chain=[A:1,B:2,C:3]; k=1` | `selected=C; head after removal=[A,B]; removed=C` |
| `chain=[A:7]; k=1` | `selected=A; head after removal=[]; removed=A` |
| `chain=[A:7,B:7]; k=2` | `selected=A; head after removal=[B]; removed=A` |
| `chain=[]; k=1` | `throw IllegalArgumentException` |
| `chain=[A:1,B:2]; k=0` | `throw IllegalArgumentException` |
| `chain=[A:1,B:2]; k=3` | `throw IllegalArgumentException` |

**Targets:** O(n+1) time per method, O(1) auxiliary, one output record for removal.

**Edge checks:** Head/tail/only-node removal, k<=0, k>n, equal values; use fresh fixtures for the two calls or call selection before removal.

**Complete when:** State the initial gap, what counts as a link, the stopping position, and the final predecessor/target relationship; validate the returned victim identity. **Mock follow-up:** Why cannot the same stopping condition be copied between two differently initialized gap conventions?

<details>
<summary>Hint 1</summary>

A tail-relative position can be represented by a distance from a leading reference.

</details>

<details>
<summary>Hint 2</summary>

Selection needs the target; unlinking usually needs the node before it.

</details>

<details>
<summary>Hint 3</summary>

Choose the starting position and stopping test together, then test k=1 and k=n before other cases.

</details>

- [ ] **P3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 7. Merging, Splitting, Reordering, and Copying

Treat each helper as a contract about identities, ownership, and links. Composition is safe only when the next phase receives the structure it assumes.

### G1. Merge with stable identity ties

**Difficulty:** Medium. **Objective:** Extend a sorted result while retaining both source continuations. **Prerequisites:** M1, R3; [sorted merging](./Linked_List_Essential_Concepts.md#11-merging-sorted-chains).

Merge two nondecreasing, acyclic, node-disjoint chains. Reuse all input nodes, preserving order within each source and taking nodes from first before equal-valued nodes from second. Either input may be null. Return null only when both are empty. Input links may be consumed; values must not change. Allow one dummy, no new data nodes, containers, or recursion.

```java
public static ListNode stableMerge(ListNode first, ListNode second) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `first=[A:2,B:5,C:5]; second=[X:1,Y:5,Z:8]` | `head=[X,A,B,C,Y,Z]` |
| `first=[]; second=[X:-1,Y:2]` | `head=[X,Y]` |
| `first=[A:0]; second=[]` | `head=[A]` |
| `first=[]; second=[]` | `head=[]` |
| `first=[A:7,B:7]; second=[X:7]` | `head=[A,B,X]` |
| `first=[A:MIN,B:MAX]; second=[X:MIN,Y:MAX]` | `head=[A,X,B,Y]` |

**Targets:** O(n+m+1) worst-case time, O(1) auxiliary, no new data nodes.

**Edge checks:** Either/both empty, unequal lengths, all ties, one source exhausted early, extreme values.

**Complete when:** Validate the exact stable identity order, the union of source identity sets, and null termination. Explain why a remaining suffix can be attached without visiting it again. **Mock follow-up:** What fails if the inputs share their final node?

<details>
<summary>Hint 1</summary>

Each source already exposes its smallest remaining value at its current head.

</details>

<details>
<summary>Hint 2</summary>

Define a committed output prefix ending at a result tail, separate from the remaining sources.

</details>

<details>
<summary>Hint 3</summary>

Retain the chosen node's successor before it becomes part of the result; make the tie policy explicit.

</details>

- [ ] **G1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### G2. Split into balanced independent halves

**Difficulty:** Medium. **Objective:** Turn a midpoint observation into two disjoint mutable chains. **Prerequisites:** P1, M2; [splitting](./Linked_List_Essential_Concepts.md#12-splitting-and-reordering).

Split an unsorted acyclic chain into consecutive left and right halves with ceil(n/2) and floor(n/2) nodes. Return Split(left,right), preserving original identities and order. Both tails must end at null. Empty input returns (null,null); a singleton belongs entirely to left. Mutate only necessary next links, allocate no data/sentinel nodes, and use no containers or recursion.

```java
public static Split splitBalanced(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[]` | `left=[]; right=[]` |
| `chain=[A:7]` | `left=[A]; right=[]` |
| `chain=[A:7,B:7]` | `left=[A]; right=[B]` |
| `chain=[A:1,B:2,C:3]` | `left=[A,B]; right=[C]` |
| `chain=[A:1,B:2,C:3,D:4]` | `left=[A,B]; right=[C,D]` |
| `chain=[A:1,B:2,C:3,D:4,E:5]` | `left=[A,B,C]; right=[D,E]` |

**Targets:** O(n+1) time, O(1) auxiliary and one result record.

**Edge checks:** Empty and singleton cases, odd/even parity, equal boundary values.

**Complete when:** Show the output identity sets are disjoint and cover the input, then explain which reference must survive the cut. **Mock follow-up:** Why is returning two heads without changing a link usually not a true split?

<details>
<summary>Hint 1</summary>

Decide which half receives the extra node before choosing a midpoint convention.

</details>

<details>
<summary>Hint 2</summary>

The second half needs its own saved entry reference before the boundary link changes.

</details>

<details>
<summary>Hint 3</summary>

Verify both output tails independently rather than only counting the first half.

</details>

- [ ] **G2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### G3. Weave the outside positions inward

**Difficulty:** Medium. **Objective:** Compose previously proved operations without aliasing the remaining halves. **Prerequisites:** R1, P1, G2; [composition](./Linked_List_Essential_Concepts.md#12-splitting-and-reordering).

Rearrange original positions 0..n-1 into 0,n-1,1,n-2,... . Reuse all nodes of the unsorted acyclic input and preserve values. Return the resulting head, or null for empty input. The final chain must be acyclic. No new data/sentinel nodes, arrays/maps/stacks, or recursion. A constant number of result records from earlier helpers is allowed. State contracts for each phase before combining them.

```java
public static ListNode foldChain(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3,D:4,E:5,F:6]` | `head=[A,F,B,E,C,D]` |
| `chain=[A:1,B:2,C:3,D:4,E:5]` | `head=[A,E,B,D,C]` |
| `chain=[A:7,B:7,C:7]` | `head=[A,C,B]` |
| `chain=[A:1,B:2]` | `head=[A,B]` |
| `chain=[A:1]` | `head=[A]` |
| `chain=[]` | `head=[]` |

**Targets:** O(n+1) total time and O(1) auxiliary with iterative helpers.

**Edge checks:** Lengths 0–6, odd leftover node, duplicates, old tail becoming an interior node.

**Complete when:** Prove each original identity appears once, explain the ownership of remaining portions during weaving, and sum the phase costs. **Mock follow-up:** How would a recursive helper change the total auxiliary-space claim?

<details>
<summary>Hint 1</summary>

The required order alternates an early original position and a late original position.

</details>

<details>
<summary>Hint 2</summary>

Any independently processed portions must have disjoint identity sets.

</details>

<details>
<summary>Hint 3</summary>

During an alternating connection, both remaining continuations need to survive the next pair of field writes.

</details>

- [ ] **G3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### G4. Copy values and cross-references

**Difficulty:** Medium. **Objective:** Preserve graph relationships under an identity-to-identity correspondence. **Prerequisites:** F1, HashMap basics; [random-reference copying](./Linked_List_Essential_Concepts.md#13-copying-a-chain-with-random-references).

Deep-copy a RandomNode chain whose next edges are acyclic and whose random edges are null or point within that next chain. Values can repeat; the chain is not sorted. Allocate exactly one fresh result node per original and preserve both edge kinds under the correspondence. No returned edge may reach an original node; original values/links remain unchanged. Empty input returns null. Use a node-identity mapping for the core solution; no recursion or temporary input mutation.

```java
public static RandomNode copyRandom(RandomNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:7,B:7,C:2]; random=[C,A,C]` | `copy=[A',B',C']; random=[C',A',C']` |
| `chain=[]; random=[]` | `copy=[]; random=[]` |
| `chain=[A:1]; random=[null]` | `copy=[A']; random=[null]` |
| `chain=[A:1]; random=[A]` | `copy=[A']; random=[A']` |
| `chain=[A:-1,B:4,C:4]; random=[B,B,B]` | `copy=[A',B',C']; random=[B',B',B']` |

**Targets:** Expected O(n+1) time, O(n+1) auxiliary mapping, O(n) new returned nodes.

**Edge checks:** Empty, null/self/forward/backward random edges, shared random targets, duplicate values.

**Complete when:** Check disjoint identity sets, exact value correspondence, translated edges, and unchanged originals. Explain why this node class works as an identity key. **Mock follow-up:** If a supplied node class overrides equals by value, which mapping semantics are required?

<details>
<summary>Hint 1</summary>

Two originals with equal values may need different outgoing relationships.

</details>

<details>
<summary>Hint 2</summary>

Every original identity needs one unambiguous corresponding copy identity.

</details>

<details>
<summary>Hint 3</summary>

Consider when a forward random target's copy becomes available, rather than assuming it was already visited.

</details>

- [ ] **G4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 8. Doubly Linked List and LRU Drills

Complete local rail operations before composing the cache. Order is newest near H and oldest near T. Every real node's key is unique; equal payload values are allowed. The shared next/prev validation applies after each operation.

### L1. Create and edit a sentinel rail

**Difficulty:** Medium. **Objective:** Maintain both directions during constant-time local node operations. **Prerequisites:** F3; [doubly linked invariants](./Linked_List_Essential_Concepts.md#14-lru-cache-with-a-map-and-doubly-linked-list).

createRail allocates exactly two fresh sentinels H,T and returns Rail(H,T) in the empty configuration. detachReal unlinks a supplied real member and leaves its prev/next null. insertNewest inserts a supplied detached real node immediately after H, preserving other nodes' relative order. Sentinels and nonnull membership/detachment arguments are promised valid; no membership scan is required. All lists are acyclic in each direction, keys are unique, and values may repeat. Apart from createRail's two sentinels, allocate no nodes or containers. No recursion; do not change keys/values.

```java
public static Rail createRail() {
    throw new UnsupportedOperationException();
}
public static void detachReal(DNode node) {
    throw new UnsupportedOperationException();
}
public static void insertNewest(DNode front, DNode detached) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `op=create` | `order=[]; H.next=T; T.prev=H` |
| `op=detach; order=[A:4/40,B:7/70,C:9/90]; node=B` | `order=[A,C]; detached=B` |
| `op=detach; order=[A:4/40]; node=A` | `order=[]; detached=A` |
| `op=insert; order=[A:4/40,B:7/70]; node=X:9/90` | `order=[X,A,B]` |
| `op=insert; order=[]; node=X:0/-1` | `order=[X]` |

**Targets:** O(1) time and auxiliary per operation. createRail adds two sentinel nodes and one record; the other methods reuse existing nodes.

**Edge checks:** Empty insertion, only-node removal, middle removal, key 0/value −1, duplicate values on distinct nodes.

**Complete when:** Traverse forward/backward after every change, inspect both outer sentinel null links, and verify every detached node has two null links. **Mock follow-up:** Why is changing only predecessor.next insufficient?

<details>
<summary>Hint 1</summary>

Each adjacency is represented by two fields that must agree.

</details>

<details>
<summary>Hint 2</summary>

A real node has two valid neighbors even at either end because sentinels are present.

</details>

<details>
<summary>Hint 3</summary>

Save the relevant neighbors before overwriting fields, then check both directional paths.

</details>

- [ ] **L1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### L2. Synchronize lookup and recency movement

**Difficulty:** Medium. **Objective:** Use key-to-node handles while preserving a map/list bijection. **Prerequisites:** L1, HashMap get/remove semantics.

Input is a valid H-to-T sentinel rail and a Map<Integer,DNode> containing exactly its real nodes keyed by their unique keys. touch(index,H,key) returns true on a hit and moves that existing node immediately after H; a miss returns false with no changes. evictOldest(index,T) removes and returns the oldest real node immediately before T, detached in both directions, or null if empty. Update the map consistently. Values may repeat; no allocation, recursion, or linear key search is permitted. Helpers may assume the supplied representation is initially valid.

```java
public static boolean touch(Map<Integer, DNode> index, DNode front, int key) {
    throw new UnsupportedOperationException();
}
public static DNode evictOldest(Map<Integer, DNode> index, DNode back) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `op=touch; order=[A:4/40,B:7/70]; key=7` | `returned=true; order=[B,A]; map={4:A,7:B}` |
| `op=touch; order=[A:4/40,B:7/70]; key=4` | `returned=true; order=[A,B]; map={4:A,7:B}` |
| `op=touch; order=[A:4/40]; key=9` | `returned=false; order=[A]; map={4:A}` |
| `op=evict; order=[A:4/40,B:7/70]` | `returned=B; order=[A]; map={4:A}; detached=B` |
| `op=evict; order=[A:0/-1]` | `returned=A; order=[]; map={}; detached=A` |
| `op=evict; order=[]` | `returned=null; order=[]; map={}` |
| `op=touch; order=[]; key=1` | `returned=false; order=[]; map={}` |

**Targets:** Expected O(1) time and O(1) auxiliary per operation under hashing assumptions. Existing representation storage is O(r+1) for r real nodes.

**Edge checks:** Hit on MRU/LRU, miss, empty eviction, only node, key distinct from value.

**Complete when:** Verify exact map values by identity, forward/backward consistency, no sentinel mappings, and preserved identity on repeated touches. **Mock follow-up:** What happens if eviction removes the right node but uses its value as the map key?

<details>
<summary>Hint 1</summary>

The map answers which object to move; it should not create a second representation of that key.

</details>

<details>
<summary>Hint 2</summary>

After each operation, the map's image must equal the set of real linked identities.

</details>

<details>
<summary>Hint 3</summary>

Separate local unlink/reinsert correctness from the decision about which key mapping changes.

</details>

- [ ] **L2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### L3. Complete a bounded recent-readings cache

**Difficulty:** Medium. **Objective:** Compose lookup, movement, updates, and eviction into a complete LRU contract. **Prerequisites:** L1–L2; [LRU reasoning](./Linked_List_Essential_Concepts.md#14-lru-cache-with-a-map-and-doubly-linked-list).

Complete PracticeCache using HashMap plus the explicit doubly linked rail. Capacity must be in 0..100,000; reject other capacities with IllegalArgumentException. get returns Integer null on a miss, otherwise the stored value and refreshes recency. put inserts or updates and makes the key most recent; an update must reuse the existing node identity. Evict the least-recent key when needed, detaching its node. Capacity zero retains nothing. Keys/values use all int values, including 0 and −1. New distinct keys may allocate one data node; initialize two sentinels. No LinkedHashMap substitute, recursion, or whole-list search. P(k,v)/G(k) denote put/get; reads lists only get returns.

```java
static final class PracticeCache {
    final int capacity;
    final Map<Integer, DNode> index = new HashMap<>();
    final DNode front = new DNode(0, 0);
    final DNode back = new DNode(0, 0);
    PracticeCache(int capacity) { throw new UnsupportedOperationException(); }
    Integer get(int key) { throw new UnsupportedOperationException(); }
    void put(int key, int value) { throw new UnsupportedOperationException(); }
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `capacity=2; ops=P(4,40),P(7,70),G(4),P(9,90),G(7)` | `reads=[40,null]; order=[9,4]; map={4:40,9:90}` |
| `capacity=2; ops=P(4,40),P(7,70),P(4,44),P(9,90),G(4)` | `reads=[44]; order=[4,9]; map={4:44,9:90}` |
| `capacity=1; ops=P(1,5),P(1,6),G(1),P(2,8),G(1)` | `reads=[6,null]; order=[2]; map={2:8}` |
| `capacity=0; ops=P(0,-1),G(0)` | `reads=[null]; order=[]; map={}` |
| `capacity=2; ops=P(0,-1),G(0),G(0)` | `reads=[-1,-1]; order=[0]; map={0:-1}` |
| `capacity=1; ops=G(3)` | `reads=[null]; order=[]; map={}` |
| `capacity=-1; ops=none` | `throw IllegalArgumentException` |

**Targets:** Expected O(1) get, expected amortized O(1) put; O(1) local auxiliary per call and O(c+1) maintained storage. Explain resize/collision assumptions.

**Edge checks:** Zero/one capacity, repeated hits, miss, existing-key update, eviction, real key 0 and value −1; also reject capacity 100001.

**Complete when:** Compare operation traces with an independent array-based recency model; validate representation invariants after every operation and capture node identity before/after an update. **Mock follow-up:** Why does a successful read count as a structural state change even if its returned value is unchanged?

<details>
<summary>Hint 1</summary>

State the recency policy for every operation before implementing the representation.

</details>

<details>
<summary>Hint 2</summary>

An existing key already owns one real node; its value and position can change without increasing the entry count.

</details>

<details>
<summary>Hint 3</summary>

Check empty, one-entry, and capacity-zero states separately, then compose the already tested local helpers.

</details>

- [ ] **L3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 9. Level 3: Curated Platform Problems

These eight selections preserve the local roadmap's exact order, priorities, and difficulties. **E is Essential, A is Additional, and C is optional advanced**; priority is distinct from LeetCode difficulty. Open the canonical statement for its current constraints and supplied node signature. No full statement or solution is reproduced here.

### N1. Reverse Linked List

**Priority:** E — Essential. **LeetCode difficulty:** Easy. **Canonical problem:** [Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/).

**Prerequisites:** R1. **Learning objective:** Reconstruct the processed-prefix/unprocessed-suffix invariant; explain the caller's new-head assignment.

**Attempt requirements:** Check null, singleton, two nodes, duplicate identities, and the original head becoming the tail. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** What does ignoring the return value do to the caller's reachable chain?

- [ ] **N1** — Canonical attempt record: the Reverse Linked List row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N2. Merge Two Sorted Lists

**Priority:** E — Essential. **LeetCode difficulty:** Easy. **Canonical problem:** [Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/).

**Prerequisites:** G1. **Learning objective:** Derive the smaller-head choice and result-tail role from sortedness.

**Attempt requirements:** Test either/both empty, unequal lengths, and duplicate values; state the actual platform mutation assumptions. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** Which ownership assumption prevents reusing one identity twice?

- [ ] **N2** — Canonical attempt record: the Merge Two Sorted Lists row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N3. Linked List Cycle

**Priority:** E — Essential. **LeetCode difficulty:** Easy. **Canonical problem:** [Linked List Cycle](https://leetcode.com/problems/linked-list-cycle/).

**Prerequisites:** P2. **Learning objective:** Explain detection by identity and relative speed without a visited container.

**Attempt requirements:** Test finite duplicate values, self-cycle, head entry, and entry after a prefix with a bounded observer. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** Why does initial equality not prove a cycle?

- [ ] **N3** — Canonical attempt record: the Linked List Cycle row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N4. Remove Nth Node From End of List

**Priority:** E — Essential. **LeetCode difficulty:** Medium. **Canonical problem:** [Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/).

**Prerequisites:** P3. **Learning objective:** Derive the gap and stopping condition, including the predecessor of the head.

**Attempt requirements:** Apply the platform's stated validity guarantees; test removing head, tail, and the only node. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** How does a dummy change the boundary proof?

- [ ] **N4** — Canonical attempt record: the Remove Nth Node From End of List row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N5. Reorder List

**Priority:** A — Additional. **LeetCode difficulty:** Medium. **Canonical problem:** [Reorder List](https://leetcode.com/problems/reorder-list/).

**Prerequisites:** G2–G3. **Learning objective:** Compose separately correct phases while preserving ownership and identity.

**Attempt requirements:** Check odd/even lengths, duplicates, no lost identities, and no accidental cycle. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** Which phase establishes that the two remaining portions are disjoint?

- [ ] **N5** — Canonical attempt record: the Reorder List row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N6. Copy List with Random Pointer

**Priority:** A — Additional. **LeetCode difficulty:** Medium. **Canonical problem:** [Copy List with Random Pointer](https://leetcode.com/problems/copy-list-with-random-pointer/).

**Prerequisites:** G4. **Learning objective:** Choose an identity correspondence and translate all allowed edges.

**Attempt requirements:** Check null/self/forward/backward/shared random targets and disjoint original/copy identities. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** How would value-based equality on the node class change your mapping choice?

- [ ] **N6** — Canonical attempt record: the Copy List with Random Pointer row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N7. LRU Cache

**Priority:** A — Additional. **LeetCode difficulty:** Medium. **Canonical problem:** [LRU Cache](https://leetcode.com/problems/lru-cache/).

**Prerequisites:** L1–L3. **Learning objective:** Implement the requested interface while explaining lookup, recency, and eviction.

**Attempt requirements:** Use the platform's capacity and miss-value contract; test hits, misses, updates, eviction, and representation consistency. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** Why does expected constant-time lookup alone not establish expected constant-time cache operations?

- [ ] **N7** — Canonical attempt record: the LRU Cache row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

### N8. Merge k Sorted Lists

**Priority:** C — Optional advanced challenge. **LeetCode difficulty:** Hard. **Canonical problem:** [Merge k Sorted Lists](https://leetcode.com/problems/merge-k-sorted-lists/).

**Prerequisites:** G1; later Heap / Priority Queue for the heap-based approach. **Learning objective:** Optional extension: compare a correct repeated-merge baseline with a method justified by later prerequisites.

**Attempt requirements:** Handle empty inputs and unequal lengths; define total node count and number of lists separately. Derive the baseline, explain the invariant, implement without solution access, and account for time/auxiliary/output storage. **Mock follow-up:** How does the chosen strategy determine which input-size variables appear in the bound?

The heap-based approach depends on [Heap / Priority Queue](./DSA_Interview_Roadmap.md#9-heap--priority-queue). Defer it until that topic. This Hard problem does not block ordinary Linked List readiness.

- [ ] **N8** — Canonical attempt record: the Merge k Sorted Lists row's existing [roadmap Log](./DSA_Interview_Roadmap.md#6-linked-list). Track Last / Result / Minutes / Next review / Lesson there; no attempt is recorded here yet.

## 10. Level 4: Unfamiliar Interview Challenges

Choose a baseline and a method before opening hints or targeted theory. Explain why a plausible alternative is inappropriate or more expensive. Titles deliberately omit the intended technique; these mandatory challenges need only earlier roadmap concepts.

### X1. Shared checkpoint

**Difficulty:** Medium. **Objective:** Determine whether two routes reach the same underlying object. **Prerequisites:** Level 1 and the shared contract; choose further prerequisites after your first attempt.

Two unsorted acyclic chains may share a suffix. Return the first node identity reachable from both heads, or null if they share none. This explicitly permits shared nodes; equal values alone are not sharing. Either head may be null. Preserve every original value/link and allocate no data nodes. Derive O(n+m+1) time with O(1) auxiliary and no recursion. In fixtures, 'second enters B' means its final next edge points to the existing B of first; an empty second prefix means second itself is B.

```java
public static ListNode sharedCheckpoint(ListNode first, ListNode second) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `first=[A:1,B:7,C:9]; second=[X:2]; second enters B` | `returned=B` |
| `first=[A:7]; second=[X:7]` | `returned=null` |
| `first=[A:1,B:2]; second=[]; second enters A` | `returned=A` |
| `first=[A:1,B:2,C:3]; second=[]; second enters C` | `returned=C` |
| `first=[]; second=[X:1]` | `returned=null` |
| `first=[]; second=[]` | `returned=null` |

**Targets:** O(n+m+1) time and O(1) auxiliary; no new nodes.

**Edge checks:** Different prefix lengths, one head inside the other route, identical heads, disjoint equal values, empty head.

**Complete when:** Explain your chosen method and a plausible alternative, return the earliest shared identity, and prove all links remain unchanged. **Mock follow-up:** Which part of your argument would need revision if cycles were allowed?

<details>
<summary>Hint 1</summary>

Once two acyclic successor routes reach one common object, their remaining suffix is the same.

</details>

<details>
<summary>Hint 2</summary>

Unequal amounts of private prefix can hide the shared position during an unadjusted joint walk.

</details>

<details>
<summary>Hint 3</summary>

Find a way to compare positions with equal remaining distance, or justify an equivalent bounded traversal.

</details>

- [ ] **X1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X2. Mirror audit

**Difficulty:** Medium. **Objective:** Answer a structural query while restoring the caller-visible representation. **Prerequisites:** Core Level 2 skills; choose a method before opening theory or hints.

Return whether the values of an unsorted acyclic chain read the same forward and backward. Empty and singleton chains return true; duplicates and negative values are valid. Temporary rewiring is allowed under exclusive ownership, but every original next link and value must be exactly restored before returning, including false results. Allocate no data nodes or growing containers, use no recursion, and meet O(n) time/O(1) auxiliary.

```java
public static boolean mirrorAudit(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:7,B:2,C:2,D:7]` | `returned=true; original links restored` |
| `chain=[A:1,B:2,C:3]` | `returned=false; original links restored` |
| `chain=[A:-1,B:4,C:-1]` | `returned=true; original links restored` |
| `chain=[A:1,B:2]` | `returned=false; original links restored` |
| `chain=[A:7]` | `returned=true; original links restored` |
| `chain=[]` | `returned=true; original links restored` |

**Targets:** O(n+1) time, O(1) auxiliary; restoration work is included.

**Edge checks:** Odd/even parity, first comparison mismatch, late mismatch, all equal, empty/singleton.

**Complete when:** Show a correctness argument for the boolean and a separate restoration argument; compare an identity-to-next snapshot before and after both outcomes. **Mock follow-up:** What is dangerous about an immediate false return inside a temporarily mutating comparison?

<details>
<summary>Hint 1</summary>

A comparison result does not finish the contract until the original structure is restored.

</details>

<details>
<summary>Hint 2</summary>

Keep enough information to undo any temporary transformation without rediscovering lost boundaries.

</details>

<details>
<summary>Hint 3</summary>

Arrange control flow so both successful and unsuccessful comparisons pass through the required restoration work.

</details>

- [ ] **X2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X3. Sensor archive requests

**Difficulty:** Medium. **Objective:** Choose a representation for repeated indexed queries rather than defaulting to link traversal. **Prerequisites:** F2, array indexing, and complete-cost analysis.

Given a read-only unsorted acyclic chain and an int[] indices, return an int[] of the values at those zero-based positions in query order, retaining repeated queries. Let q=indices.length<=100,000. Each index must satisfy 0<=index<n; otherwise throw IllegalArgumentException. Empty queries return an empty array, including with an empty head; any query on an empty chain is invalid. Values may repeat. Do not change or allocate list nodes; arrays/collections are allowed, recursion is not. Choose a structure that meets the total target.

```java
public static int[] archiveLookups(ListNode head, int[] indices) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:4,B:-1,C:4]; indices=[2,0,1,2]` | `returned=[4,4,-1,4]` |
| `chain=[]; indices=[]` | `returned=[]` |
| `chain=[A:9]; indices=[]` | `returned=[]` |
| `chain=[A:9]; indices=[0,0]` | `returned=[9,9]` |
| `chain=[]; indices=[0]` | `throw IllegalArgumentException` |
| `chain=[A:1]; indices=[-1]` | `throw IllegalArgumentException` |
| `chain=[A:1]; indices=[1]` | `throw IllegalArgumentException` |

**Targets:** O(n+q+1) time, O(n+1) auxiliary allowed, plus O(q) returned output.

**Edge checks:** Empty queries, empty chain, repeated positions, first/last position, invalid negative/too-large index.

**Complete when:** Explain why the original representation alone may be a poor query index; include conversion/storage costs and verify the original structure is unchanged. **Mock follow-up:** How does the best tradeoff change if there is exactly one query?

<details>
<summary>Hint 1</summary>

The workload contains many positional requests, not structural edits.

</details>

<details>
<summary>Hint 2</summary>

A one-time representation change can avoid rediscovering the same positions repeatedly.

</details>

<details>
<summary>Hint 3</summary>

Account for the cost of get(i) on the specific collection you choose; a familiar List interface does not promise constant indexing.

</details>

- [ ] **X3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X4. Arrival schedule

**Difficulty:** Medium. **Objective:** Preserve relative order while arranging existing identities under a new rule. **Prerequisites:** Level 1 operations and core structural invariants; select the method independently.

Given an unsorted acyclic chain and an int cutoff, move all nodes with val<cutoff before all nodes with val>=cutoff. Preserve the original relative order within each group. Reuse every original node exactly once, do not change values, and return the new head or null for empty input. Up to two temporary dummies are allowed; no new data nodes, growing containers, or recursion. The final chain must terminate at null.

```java
public static ListNode arrangeArrivals(ListNode head, int cutoff) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:3,B:1,C:3,D:0]; cutoff=3` | `head=[B,D,A,C]` |
| `chain=[A:1,B:1]; cutoff=2` | `head=[A,B]` |
| `chain=[A:3,B:4]; cutoff=3` | `head=[A,B]` |
| `chain=[A:-2,B:5,C:-2,D:0]; cutoff=0` | `head=[A,C,B,D]` |
| `chain=[A:MAX,B:MIN]; cutoff=0` | `head=[B,A]` |
| `chain=[]; cutoff=0` | `head=[]` |

**Targets:** O(n+1) time and O(1) auxiliary, including permitted sentinels.

**Edge checks:** All/none below cutoff, duplicates spanning the boundary value, extreme ints, empty/singleton.

**Complete when:** Compare against stable filtering of an original identity array; prove there are no old cross-links that introduce an extra node or cycle. **Mock follow-up:** Would swapping node values satisfy the identity and stability contract?

<details>
<summary>Hint 1</summary>

Membership in a group and ordering within that group are different obligations.

</details>

<details>
<summary>Hint 2</summary>

Record enough boundary state to extend each group's order without rescanning it.

</details>

<details>
<summary>Hint 3</summary>

Before joining final portions, inspect whether any surviving old next edge still crosses the intended boundary.

</details>

- [ ] **X4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### X5. First revisit report

**Difficulty:** Medium. **Objective:** Select state that distinguishes a new object from a repeated visit. **Prerequisites:** Reference identity, collection costs, and termination reasoning.

Starting at head, follow next until null or the first repeated node identity. Return WalkReport(distinct,repeated), where distinct counts different visited objects and repeated is the first identity seen again, or null if traversal terminates. Input may contain a cycle and duplicate values; it has at most 100,000 distinct reachable nodes. Empty input returns (0,null). Do not mutate or allocate data nodes; working collections are permitted and recursion is forbidden. Choose and justify the stored information before coding.

```java
public record WalkReport(int distinct, ListNode repeated) {}
public static WalkReport firstRevisit(ListNode head) {
    throw new UnsupportedOperationException();
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:2,B:2,C:2]` | `distinct=3; repeated=null` |
| `chain=[A:1,B:2,C:3]; last->B` | `distinct=3; repeated=B` |
| `chain=[A:1]; last->A` | `distinct=1; repeated=A` |
| `chain=[A:1,B:2]; last->A` | `distinct=2; repeated=A` |
| `chain=[]` | `distinct=0; repeated=null` |

**Targets:** Expected O(n+1) time with O(n+1) auxiliary allowed, where n is distinct reachable nodes; state a stronger bound if you derive one.

**Edge checks:** Finite duplicates, self-cycle, head entry, interior entry, empty input.

**Complete when:** Use a bounded observer, preserve every edge, and explain why your membership/equality model matches objects rather than values. **Mock follow-up:** Could a constant-space alternative return both fields, and what extra reasoning would it need?

<details>
<summary>Hint 1</summary>

The stopping event is a repeated object, not a repeated integer.

</details>

<details>
<summary>Hint 2</summary>

Your retained state must answer whether this exact identity was already counted.

</details>

<details>
<summary>Hint 3</summary>

Check whether a membership insertion reports a new identity, and handle the null endpoint separately.

</details>

- [ ] **X5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 11. Complexity Analysis Exercises

For each C snippet, answer: (1) What are the input-size variables? (2) What is total time? (3) What is peak auxiliary space? (4) How many nodes/positions are visited? (5) Which new nodes or other objects are allocated? (6) What recursive frames are live? (7) Which map/library operations occur? (8) Which assumptions affect the bounds? (9) Can work or storage be improved without changing the contract?

Do not open the delayed cost checks first. Calls to earlier unfinished starters are analyzed under the explicitly stated correct-helper contracts. To execute those diagnostic snippets, supply your own verified helpers in a temporary test class. This workbook does not fill in those implementation solutions. C1 and C4 run independently of such helpers.

### C1. Two scans and one scalar

**Difficulty:** Easy. **Objective:** Count full traversals without multiplying sequential work. **Prerequisites:** F2 and the nine analysis questions below.

Analyze the complete diagnostic below on an unsorted acyclic chain, including empty input and duplicate/negative values. It returns the sum of node values plus the node count as long. Input is read-only; no new nodes or recursion are used. Derive time and auxiliary space before checking the delayed key.

```java
public static long c1(ListNode head) {
    long total = 0;
    for (ListNode p = head; p != null; p = p.next) {
        total += p.val;
    }
    int count = 0;
    for (ListNode p = head; p != null; p = p.next) {
        count++;
    }
    return total + count;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:2,B:-1,C:2]` | `returned=6` |
| `chain=[]` | `returned=0` |
| `chain=[A:0]` | `returned=1` |
| `chain=[A:MAX,B:MAX]` | `returned=4294967296` |

**Targets:** Derive the bounds first; compare with C1 in the [delayed cost checks](#delayed-cost-checks).

**Edge checks:** Zero iterations, full int arithmetic range, two traversals of the same identities.

**Complete when:** Answer all nine analysis questions, explain the number of visits and numeric safety, and distinguish a constant-factor improvement from a different asymptotic class. **Mock follow-up:** Would combining the loops change the asymptotic time?

- [ ] **C1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C2. Repeated orientation passes

**Difficulty:** Medium. **Objective:** Account for repeated helper calls, result records, and reused nodes. **Prerequisites:** R1, C1, and the analysis checklist.

For analysis, reverseWithEnds is a correct iterative R1 implementation meeting its contract. The diagnostic performs rounds in 0..1000 and returns PassResult(visits,head). The unsorted acyclic input is destructively reused; visits counts only the explicit counting loop, not the helper's visits. Empty and singleton inputs are allowed. Track final identity order, cumulative allocations of endpoint records, and peak auxiliary storage. No recursion.

```java
public record PassResult(long visits, ListNode head) {}
public static PassResult c2(ListNode head, int rounds) {
    long visits = 0;
    for (int pass = 0; pass < rounds; pass++) {
        Endpoints reversed = reverseWithEnds(head);
        head = reversed.head();
        for (ListNode p = head; p != null; p = p.next) {
            visits++;
        }
    }
    return new PassResult(visits, head);
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3]; rounds=2` | `visits=6; head=[A,B,C]` |
| `chain=[A:1,B:2,C:3]; rounds=1` | `visits=3; head=[C,B,A]` |
| `chain=[A:1,B:2]; rounds=0` | `visits=0; head=[A,B]` |
| `chain=[]; rounds=3` | `visits=0; head=[]` |
| `chain=[A:7]; rounds=4` | `visits=4; head=[A]` |

**Targets:** Derive time in both n and rounds, peak auxiliary, and output; check C2 only after answering.

**Edge checks:** Zero rounds, empty chains with positive rounds, odd/even round parity.

**Complete when:** Distinguish the returned visits counter from all executed work and total allocated records from simultaneously needed records. **Mock follow-up:** Can a direct mathematical result replace repeated work if only the final order and counter are required?

<details>
<summary>Hint 1</summary>

The counter excludes work done inside a helper.

</details>

<details>
<summary>Hint 2</summary>

A returned record holds references to reused nodes; it does not copy the chain.

</details>

<details>
<summary>Hint 3</summary>

Analyze one pass, the number of passes, and the maximum number of records that must stay live.

</details>

- [ ] **C2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C3. A merged chain followed by measurement

**Difficulty:** Medium. **Objective:** Combine two independent input sizes and include an output scan. **Prerequisites:** G1 and the analysis checklist.

Assume stableMerge is a correct iterative G1 implementation. Inputs are sorted, acyclic, node-disjoint, and may be empty. This diagnostic consumes their links and returns ChainMeasure(head,size). It preserves values and uses no recursion or new data nodes. Analyze helper work and the subsequent full traversal separately.

```java
public record ChainMeasure(ListNode head, int size) {}
public static ChainMeasure c3(ListNode first, ListNode second) {
    ListNode merged = stableMerge(first, second);
    int size = 0;
    for (ListNode p = merged; p != null; p = p.next) {
        size++;
    }
    return new ChainMeasure(merged, size);
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `first=[A:1,B:3]; second=[X:2,Y:3]` | `head=[A,X,B,Y]; size=4` |
| `first=[]; second=[X:7]` | `head=[X]; size=1` |
| `first=[]; second=[]` | `head=[]; size=0` |
| `first=[A:7,B:7]; second=[X:7]` | `head=[A,B,X]; size=3` |

**Targets:** Derive bounds using n and m; compare with delayed C3.

**Edge checks:** Empty input shortcut inside merge versus nonempty counting work, stable ties, unequal lengths.

**Complete when:** Explain why O(1) suffix attachment does not eliminate the diagnostic's later traversal of that suffix. **Mock follow-up:** If an input owner cached its old tail, what metadata might be stale afterward?

<details>
<summary>Hint 1</summary>

The helper can attach an existing suffix without copying it.

</details>

<details>
<summary>Hint 2</summary>

The outer diagnostic still visits every node in the returned chain.

</details>

<details>
<summary>Hint 3</summary>

Keep input storage, the helper dummy, and the returned measurement record in separate categories.

</details>

- [ ] **C3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C4. The cost behind indexed reads

**Difficulty:** Medium. **Objective:** Analyze library access behavior rather than the apparent loop shape. **Prerequisites:** [Java LinkedList costs](./Java_List_ArrayList_LinkedList.md#linkedlist-complexity) and the analysis checklist.

The input is a nonnull java.util.LinkedList<Integer> of length 0..100,000 with nonnull elements. It is read-only and may be unsorted with duplicates/negative/extreme ints. The return is the long sum. The existing library nodes are input storage; no custom nodes are involved. No recursion. Explain why this diagnostic is intentionally a library-list example rather than a replacement for structural drills.

```java
public static long c4(LinkedList<Integer> values) {
    long sum = 0;
    for (int i = 0; i < values.size(); i++) {
        sum += values.get(i);
    }
    return sum;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `values=[2,-1,2]` | `returned=3` |
| `values=[]` | `returned=0` |
| `values=[7]` | `returned=7` |
| `values=[MAX,MAX]` | `returned=4294967294` |

**Targets:** Derive the traversal sum and auxiliary storage before opening delayed C4.

**Edge checks:** Empty/singleton, nearest-end traversal, primitive unboxing, wide summation.

**Complete when:** Express the indexed traversal distances across the whole loop and propose an API-level improvement with the same output. **Mock follow-up:** Would changing the declared type to List<Integer> make get(i) constant time?

<details>
<summary>Hint 1</summary>

The complexity of a loop body depends on the runtime collection implementation.

</details>

<details>
<summary>Hint 2</summary>

Each indexed read starts a search for its position rather than retaining the previous cursor.

</details>

<details>
<summary>Hint 3</summary>

Use the distance from the nearer end when summing the work; compare with one sequential iterator.

</details>

- [ ] **C4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C5. Guarded half-speed checksum

**Difficulty:** Medium. **Objective:** Combine cycle detection with a different-speed traversal and distinguish phases. **Prerequisites:** P1–P2 and the analysis checklist.

For analysis, detectCycle is a correct constant-auxiliary P2 implementation. Input may contain a reachable cycle; values can repeat. If cyclic, return ScanResult(true,0) immediately. Otherwise accumulate values at the slow reference after each one-step/two-step movement. The output is a diagnostic checksum, not a middle-node result. Read-only; no data-node allocation or recursion.

```java
public record ScanResult(boolean cyclic, long checksum) {}
public static ScanResult c5(ListNode head) {
    if (detectCycle(head)) {
        return new ScanResult(true, 0);
    }
    ListNode slow = head;
    ListNode fast = head;
    long checksum = 0;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        checksum += slow.val;
    }
    return new ScanResult(false, checksum);
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3,D:4,E:5]` | `cyclic=false; checksum=5` |
| `chain=[A:9,B:8,C:7,D:6]` | `cyclic=false; checksum=15` |
| `chain=[A:-1,B:2]` | `cyclic=false; checksum=2` |
| `chain=[A:7]` | `cyclic=false; checksum=0` |
| `chain=[]` | `cyclic=false; checksum=0` |
| `chain=[A:1,B:2,C:3]; last->B` | `cyclic=true; checksum=0` |

**Targets:** Derive bounds separately for acyclic n and cyclic μ,λ; compare with delayed C5.

**Edge checks:** Empty/singleton, odd/even counts, cycle after a prefix, negative values.

**Complete when:** Count the checksum terms independently of the detection phase and explain why the second traversal cannot hang under the helper contract. **Mock follow-up:** Would replacing the detection helper with a visited-set version change the space bound?

<details>
<summary>Hint 1</summary>

A successful cycle guard changes which later code executes.

</details>

<details>
<summary>Hint 2</summary>

On finite input, the walkers' speeds bound the number of checksum additions.

</details>

<details>
<summary>Hint 3</summary>

Add sequential phase costs; do not multiply a detection traversal by the later traversal.

</details>

- [ ] **C5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C6. A recursive result and a later scan

**Difficulty:** Medium. **Objective:** Distinguish active call-stack depth from work after the helper returns. **Prerequisites:** R2, C3, and the analysis checklist.

Assume reverseRecursively is the correct recursive R2 implementation, with n<=200 and null allowed. The unsorted acyclic input is reused destructively and no new data nodes are created. Return the reversed head and its length. Determine where peak space occurs, including the recursive helper even though its code is outside this snippet.

```java
public static ChainMeasure c6(ListNode head) {
    ListNode reversed = reverseRecursively(head);
    int size = 0;
    for (ListNode p = reversed; p != null; p = p.next) {
        size++;
    }
    return new ChainMeasure(reversed, size);
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:2]` | `head=[C,B,A]; size=3` |
| `chain=[A:7,B:7]` | `head=[B,A]; size=2` |
| `chain=[A:-1]` | `head=[A]; size=1` |
| `chain=[]` | `head=[]; size=0` |

**Targets:** Derive total time and peak stack/auxiliary storage; compare with delayed C6.

**Edge checks:** Null base case, singleton, duplicates, all frames live before unwinding.

**Complete when:** Explain why the later loop does not keep the earlier recursive frames active, while those earlier frames still count toward peak space. **Mock follow-up:** What changes if the helper is replaced by R1's iterative implementation?

<details>
<summary>Hint 1</summary>

A helper's local variables can remain live in many simultaneous calls.

</details>

<details>
<summary>Hint 2</summary>

The maximum simultaneous storage matters even if it is released before the next phase.

</details>

<details>
<summary>Hint 3</summary>

Draw the deepest call-stack state and compare it with the later counting state.

</details>

- [ ] **C6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C7. Several independent replicas

**Difficulty:** Medium. **Objective:** Separate working-map storage, allocated nodes, and returned reference slots. **Prerequisites:** G4 and the analysis checklist.

Assume copyRandom is the correct map-based G4 implementation with a fresh mapping per call. copies is promised in 0..20. The next chain is acyclic and all random targets stay inside it or are null. Return an ArrayList of all copied nodes in batch order, each batch in next-chain order. Each batch is disjoint from originals and from other batches; its next/random links remain within that batch. Original links are read-only, duplicates are allowed, and no recursion is used. Analyze live working memory separately from cumulative allocation.

```java
public static List<RandomNode> c7(RandomNode head, int copies) {
    List<RandomNode> all = new ArrayList<>();
    for (int batch = 0; batch < copies; batch++) {
        RandomNode replica = copyRandom(head);
        for (RandomNode p = replica; p != null; p = p.next) {
            all.add(p);
        }
    }
    return all;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:7,B:7]; random=[B,A]; copies=2` | `list=[A',B',A'',B'']; fresh nodes=4` |
| `chain=[A:1]; random=[A]; copies=2` | `list=[A',A'']; fresh nodes=2` |
| `chain=[]; random=[]; copies=3` | `list=[]; fresh nodes=0` |
| `chain=[A:1,B:2]; random=[null,A]; copies=0` | `list=[]; fresh nodes=0` |

**Targets:** Derive time in n and copies, peak auxiliary, returned storage, and cumulative node allocations; compare with delayed C7.

**Edge checks:** Zero copies, empty source with positive copies, duplicate values, self/shared random targets.

**Complete when:** Verify the stated batches, explain the lifetime of each helper map, and account for ArrayList growth and returned reference slots. **Mock follow-up:** Why is a list containing the same replica twice not equivalent?

<details>
<summary>Hint 1</summary>

Repeated output batches remain reachable even when a temporary helper map is no longer needed.

</details>

<details>
<summary>Hint 2</summary>

A reference slot in the returned ArrayList and the node it refers to are different allocated objects.

</details>

<details>
<summary>Hint 3</summary>

Count one copy pass and one append pass per batch; then distinguish peak non-output state from retained output.

</details>

- [ ] **C7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### C8. Cache misses over a request stream

**Difficulty:** Medium. **Objective:** Analyze amortized hash work and bounded retained state over many operations. **Prerequisites:** L3 and the analysis checklist.

Assume PracticeCache is a correct L3 implementation. Given valid capacity c and nonnull int[] keys of length q<=100,000, create an empty cache. Each request calls get; on a miss store key as its value. Return int[]{misses,retainedSize}. Repeated/negative keys are allowed, and stored −1 is not a miss. Input keys are read-only. The helper allocates/evicts nodes under L3's rules; no recursion.

```java
public static int[] c8(int capacity, int[] keys) {
    PracticeCache cache = new PracticeCache(capacity);
    int misses = 0;
    for (int key : keys) {
        if (cache.get(key) == null) {
            misses++;
            cache.put(key, key);
        }
    }
    return new int[] {misses, cache.index.size()};
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `capacity=2; keys=[1,2,1,3,2]` | `returned=[4,2]` |
| `capacity=1; keys=[7,7,8,7]` | `returned=[3,1]` |
| `capacity=0; keys=[0,0,0]` | `returned=[3,0]` |
| `capacity=2; keys=[]` | `returned=[0,0]` |
| `capacity=2; keys=[0,-1,0]` | `returned=[2,2]` |

**Targets:** Derive expected total time, retained cache storage, and output; compare with delayed C8.

**Edge checks:** Capacity zero/one, repeated hits, evicted keys requested again, no requests, key −1.

**Complete when:** Distinguish cumulative allocations from peak retained entries, explain resizing assumptions, and trace hit-driven recency changes. **Mock follow-up:** Could retained node count stay small while the stream causes many allocations?

<details>
<summary>Hint 1</summary>

A request can refresh order even when it allocates nothing.

</details>

<details>
<summary>Hint 2</summary>

The cache retains at most capacity entries, but each later miss may allocate a replacement.

</details>

<details>
<summary>Hint 3</summary>

Analyze the operation sequence as a whole under the stated hash assumptions; state a bound that also handles c=0 and q=0.

</details>

- [ ] **C8** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

## 12. Debugging Challenges

Every snippet below has a deliberate, reproducible defect. Tables specify the **required corrected behavior**. The completion protocol for every D exercise is: identify each defect; construct a failing fixture; draw the incorrect reference structure; explain the failure; repair the implementation; state the restored invariant; test both failures and controls; and derive total time/auxiliary space.

Do not treat a convenient value display as structural success. Bound all observations of suspect output, and isolate any suspected nonterminating execution with a timeout. Several subjects contain independent defects, so a single changed line may not restore the entire contract. C/D code belongs only in scratch practice, not in your completed implementation library.

### D1. The disappearing suffix

**Difficulty:** Medium. **Objective:** Diagnose lost reachability and a wrong returned entry reference. **Prerequisites:** R1 and the debugging protocol below.

Required behavior: reverse an unsorted acyclic chain in place and return its new head, preserving every node/value and ending at null. Empty input returns null. No data-node allocation, recursion, or growing storage is allowed. The supplied implementation is deliberately wrong; the table shows required results, not its actual results.

```java
public static ListNode d1(ListNode head) {
    ListNode previous = null;
    ListNode current = head;
    while (current != null) {
        current.next = previous;
        previous = current;
        current = current.next;
    }
    return head;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3]` | `head=[C,B,A]` |
| `chain=[A:7,B:7]` | `head=[B,A]` |
| `chain=[A:-1]` | `head=[A]` |
| `chain=[]` | `head=[]` |

**Targets:** Repair target: O(n+1) time, O(1) auxiliary.

**Edge checks:** Two nodes isolate reachability; empty/singleton are controls.

**Complete when:** Follow the debugging protocol, test both traversal and return behavior, and show why fixing only one of them can leave the contract broken. **Mock follow-up:** Which reference still reaches B immediately after the first original link is overwritten?

<details>
<summary>Hint 1</summary>

A field assignment changes what a later read of that field observes.

</details>

<details>
<summary>Hint 2</summary>

The old successor and the new predecessor belong to different portions.

</details>

<details>
<summary>Hint 3</summary>

Audit both the state transition inside the loop and the reference returned after termination.

</details>

- [ ] **D1** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D2. Deletion boundary audit

**Difficulty:** Medium. **Objective:** Check the returned data boundary and the state of a removed object. **Prerequisites:** F3, R3, and dummy-node reasoning.

Required behavior: remove only the first target-valued node, return the surviving head, and detach the removed node by setting next=null. An absent target leaves the original chain unchanged; empty input returns null. Inputs are unsorted, acyclic, and may contain duplicates. Reuse all survivors, allow one dummy, forbid recursion. External fixtures retain removed identities for inspection.

```java
public static ListNode d2(ListNode head, int target) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode p = dummy;
    while (p.next != null && p.next.val != target) {
        p = p.next;
    }
    if (p.next != null) {
        p.next = p.next.next;
    }
    return dummy;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:2,B:3,C:2]; target=2` | `head=[B,C]; detached=[A]` |
| `chain=[A:1,B:2,C:3]; target=2` | `head=[A,C]; detached=[B]` |
| `chain=[A:1,B:2]; target=9` | `head=[A,B]; detached=[]` |
| `chain=[]; target=0` | `head=[]; detached=[]` |
| `chain=[A:7]; target=7` | `head=[]; detached=[A]` |

**Targets:** Repair target: O(n+1) worst-case time, O(1) auxiliary.

**Edge checks:** Head/interior/only-node deletion, no match, empty input.

**Complete when:** Validate the returned identity set and the removed object's fields; demonstrate whether a return-line-only repair is sufficient. **Mock follow-up:** Why can an extra zero-valued node be a bug even if zero is a valid input value?

<details>
<summary>Hint 1</summary>

The helper node has a role outside the data sequence.

</details>

<details>
<summary>Hint 2</summary>

Bypassing a victim and detaching that victim are separate observable requirements.

</details>

<details>
<summary>Hint 3</summary>

Keep a handle to every object whose postcondition you must establish after changing its incoming edge.

</details>

- [ ] **D2** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D3. Earlier-middle inspection

**Difficulty:** Medium. **Objective:** Separate null safety from the requested even-length convention. **Prerequisites:** P1 and safe short-circuit conditions.

Return the earlier middle identity of an acyclic unsorted chain, or null for empty input. For odd length return the unique middle. Values and links are read-only; no data allocation, growing storage, or recursion. Diagnose the given implementation on both parity classes, then repair its contract.

```java
public static ListNode d3(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[]` | `returned=null` |
| `chain=[A:7]` | `returned=A` |
| `chain=[A:7,B:7]` | `returned=A` |
| `chain=[A:1,B:2,C:3]` | `returned=B` |
| `chain=[A:1,B:2,C:3,D:4]` | `returned=B` |

**Targets:** Repair target: O(n+1) time, O(1) auxiliary.

**Edge checks:** Empty, singleton, both even and odd lengths, duplicate central values.

**Complete when:** Construct a null-safety failure and test whether merely adding fast!=null gives the required earlier middle. **Mock follow-up:** What does the final state of fast tell you about the convention you implemented?

<details>
<summary>Hint 1</summary>

The loop condition itself dereferences a reference.

</details>

<details>
<summary>Hint 2</summary>

A safe loop can still return the wrong one of two middle identities.

</details>

<details>
<summary>Hint 3</summary>

Trace the first two even lengths after each proposed guard change.

</details>

- [ ] **D3** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D4. A suspicious revisit signal

**Difficulty:** Medium. **Objective:** Separate initial coincidence, value equality, and structural identity. **Prerequisites:** P2 and the cycle meeting proof.

Return true exactly when next traversal reaches a cycle. Empty and acyclic inputs return false; cyclic inputs may have an entry anywhere. Duplicate values are allowed. Preserve all links/values and meet constant auxiliary space without recursion. The table gives correct boolean results.

```java
public static boolean d4(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        if (slow.val == fast.val) {
            return true;
        }
        slow = slow.next;
        fast = fast.next.next;
    }
    return false;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2]` | `returned=false` |
| `chain=[A:5,B:7,C:7,D:9]` | `returned=false` |
| `chain=[A:1]; last->A` | `returned=true` |
| `chain=[A:1,B:2,C:3]; last->B` | `returned=true` |
| `chain=[A:7]` | `returned=false` |
| `chain=[]` | `returned=false` |

**Targets:** Repair target: O(n+1) for finite chains or O(μ+λ+1) for cyclic ones; O(1) auxiliary.

**Edge checks:** Finite duplicates, initial state on a two-node chain, self-cycle, interior cycle, null.

**Complete when:** Provide separate witnesses for an initialization error and for a value-based equality error after initialization is corrected. **Mock follow-up:** Does moving the comparison alone make value equality a reliable cycle test?

<details>
<summary>Hint 1</summary>

Two walkers initially coincide on every nonempty input, not only cyclic ones.

</details>

<details>
<summary>Hint 2</summary>

Two different objects can store the same integer.

</details>

<details>
<summary>Hint 3</summary>

State exactly when a comparison becomes meaningful and which equality operation expresses that event.

</details>

- [ ] **D4** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D5. Tail-relative removal audit

**Difficulty:** Medium. **Objective:** Repair a gap convention and reject invalid requests without mutation. **Prerequisites:** P3 and predecessor-based deletion.

Return the chain with the one-based kth node from the end removed and detached. Reuse all survivors, preserve values, and allow one dummy but no recursion/growing storage. Input is acyclic and unsorted. Reject k<=0 or k>n with IllegalArgumentException before any original link changes. The implementation below violates this contract.

```java
public static ListNode d5(ListNode head, int k) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode ahead = dummy;
    for (int step = 0; step < k; step++) {
        ahead = ahead.next;
    }
    ListNode behind = dummy;
    while (ahead != null) {
        ahead = ahead.next;
        behind = behind.next;
    }
    ListNode removed = behind.next;
    behind.next = removed.next;
    removed.next = null;
    return dummy.next;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3]; k=2` | `head=[A,C]; detached=B` |
| `chain=[A:1,B:2,C:3]; k=3` | `head=[B,C]; detached=A` |
| `chain=[A:1,B:2,C:3]; k=1` | `head=[A,B]; detached=C` |
| `chain=[A:7]; k=1` | `head=[]; detached=A` |
| `chain=[]; k=1` | `throw IllegalArgumentException` |
| `chain=[A:1,B:2]; k=0` | `throw IllegalArgumentException` |
| `chain=[A:1,B:2]; k=3` | `throw IllegalArgumentException` |

**Targets:** Repair target: O(n+1) time, O(1) auxiliary.

**Edge checks:** Head/tail/middle/only-node removal; invalid requests that currently throw the wrong exception or alter the wrong link.

**Complete when:** Draw the exact final ahead/behind positions, test rejection preserves the original chain, and confirm the victim's identity and detachment. **Mock follow-up:** How can a wrong gap produce a plausible-looking list while deleting the wrong node?

<details>
<summary>Hint 1</summary>

The stopped behind reference must identify a predecessor, not merely a nearby position.

</details>

<details>
<summary>Hint 2</summary>

The initial gap and the stopping endpoint jointly determine the final index.

</details>

<details>
<summary>Hint 3</summary>

Validate the lead walk before making destructive changes and test both k=1 and k=n.

</details>

- [ ] **D5** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D6. Alternating-chain composition audit

**Difficulty:** Medium. **Objective:** Find an ownership failure between individually correct helper calls. **Prerequisites:** P1, R1, G3; assume middlePair and reverseWithEnds are correct.

Required result is the original identity order 0,n-1,1,n-2,..., null-terminated and acyclic. Input is an unsorted acyclic chain, possibly empty; reuse nodes and preserve values with O(1) auxiliary and no recursion. Treat the helpers as correct under their own contracts. Test the returned graph with bounded traversal; its values may initially look right.

```java
public static ListNode d6(ListNode head) {
    if (head == null || head.next == null) {
        return head;
    }
    ListNode middle = middlePair(head).first();
    ListNode right = reverseWithEnds(middle.next).head();
    ListNode left = head;
    while (right != null) {
        ListNode nextLeft = left.next;
        ListNode nextRight = right.next;
        left.next = right;
        right.next = nextLeft;
        left = nextLeft;
        right = nextRight;
    }
    return head;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:1,B:2,C:3,D:4]` | `head=[A,D,B,C]; acyclic=true` |
| `chain=[A:1,B:2,C:3]` | `head=[A,C,B]; acyclic=true` |
| `chain=[A:7,B:7]` | `head=[A,B]; acyclic=true` |
| `chain=[A:7]` | `head=[A]; acyclic=true` |
| `chain=[]` | `head=[]; acyclic=true` |

**Targets:** Repair target: O(n+1) total time and O(1) auxiliary.

**Edge checks:** Two nodes, odd/even chains, duplicates, empty/singleton controls.

**Complete when:** Find the first edge creating repeated reachability, identify the missing phase precondition, and verify every original node exactly once after repair. **Mock follow-up:** Can two correct functions compose incorrectly when they are given overlapping state?

<details>
<summary>Hint 1</summary>

Two entry references do not necessarily describe independent chains.

</details>

<details>
<summary>Hint 2</summary>

Check whether the left portion can still reach nodes being used by the right portion.

</details>

<details>
<summary>Hint 3</summary>

Establish each phase's structural ownership before it begins, then inspect the final tail edge.

</details>

- [ ] **D6** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D7. Replica correspondence audit

**Difficulty:** Medium. **Objective:** Distinguish a correct value display from a true deep copy. **Prerequisites:** G4 and identity-key semantics.

Required behavior is G4's deep copy: one fresh node per original, values and both edge kinds translated, no original identities in the returned graph, originals unchanged. The next chain is acyclic and random targets stay inside it or are null. Duplicates are allowed; null head returns null. No recursion. The supplied snippet has more than one independent defect.

```java
public static RandomNode d7(RandomNode head) {
    if (head == null) {
        return null;
    }
    Map<Integer, RandomNode> byValue = new HashMap<>();
    for (RandomNode p = head; p != null; p = p.next) {
        byValue.put(p.val, new RandomNode(p.val));
    }
    for (RandomNode p = head; p != null; p = p.next) {
        RandomNode copy = byValue.get(p.val);
        copy.next = p.next == null ? null : byValue.get(p.next.val);
        copy.random = p.random;
    }
    return byValue.get(head.val);
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[A:7,B:7]; random=[null,null]` | `copy=[A',B']; random=[null,null]` |
| `chain=[A:1,B:2]; random=[B,A]` | `copy=[A',B']; random=[B',A']` |
| `chain=[A:1]; random=[A]` | `copy=[A']; random=[A']` |
| `chain=[A:1,B:2]; random=[null,null]` | `copy=[A',B']; random=[null,null]` |
| `chain=[]; random=[]` | `copy=[]; random=[]` |

**Targets:** Repair target: expected O(n+1) time, O(n+1) auxiliary mapping, O(n) fresh returned nodes.

**Edge checks:** Duplicate original values, self/cross random references, unique-value/null-random control, empty input.

**Complete when:** Use separate failing inputs to isolate identity collapse and original-object sharing; verify both are fixed without mutating originals. **Mock follow-up:** Why might comparing only a copied value array accept this implementation?

<details>
<summary>Hint 1</summary>

A map key must distinguish every original object that deserves its own copy.

</details>

<details>
<summary>Hint 2</summary>

A fresh node can still contain an edge into the old graph.

</details>

<details>
<summary>Hint 3</summary>

Audit the mapping domain and the translation of each outgoing edge independently.

</details>

- [ ] **D7** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D8. Recency movement in both directions

**Difficulty:** Medium. **Objective:** Repair a local operation that may look correct during forward traversal. **Prerequisites:** L1–L2 and bidirectional adjacency.

Given valid nonempty sentinel state and a supplied real member node, move that same node immediately after H, preserving all other order, keys, and values. No map changes are needed because identities remain the same. Do not allocate nodes or use recursion. Every prev/next adjacency and both sentinel boundaries must be correct afterward. The code is intentionally flawed.

```java
public static void d8(DNode front, DNode node) {
    node.prev.next = node.next;
    node.prev = front;
    node.next = front.next;
    front.next.prev = node;
    front.next = node;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `order=[A:4/40,B:7/70,C:9/90]; node=B` | `order=[B,A,C]; reverse=[C,A,B]` |
| `order=[A:4/40,B:7/70]; node=B` | `order=[B,A]; reverse=[A,B]` |
| `order=[A:4/40,B:7/70]; node=A` | `order=[A,B]; reverse=[B,A]` |
| `order=[A:0/-1]; node=A` | `order=[A]; reverse=[A]` |

**Targets:** Repair target: O(1) time and auxiliary.

**Edge checks:** Middle node, oldest node, already-newest control, only-node control.

**Complete when:** Identify the stale adjacency and show it through a backward trace; confirm no extra node or lost mapping is hidden by a correct forward order. **Mock follow-up:** Why should a structural validator inspect more than head.next traversal?

<details>
<summary>Hint 1</summary>

The old location has two neighbors.

</details>

<details>
<summary>Hint 2</summary>

Changing the predecessor's forward edge does not automatically change the successor's backward edge.

</details>

<details>
<summary>Hint 3</summary>

Check the old neighborhood before reasoning about the node's new front position.

</details>

- [ ] **D8** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D9. Eviction bookkeeping audit

**Difficulty:** Medium. **Objective:** Keep removal from the rail synchronized with the lookup representation. **Prerequisites:** L2–L3 and HashMap key semantics.

Given a valid map and sentinel rail, evict and return the oldest real node, detached in both directions; return null for empty state. Remove exactly its key mapping. Preserve the other nodes, their order, keys, and values. Values may repeat and need not equal keys. Input state is otherwise valid; no allocation or recursion. The table states required postconditions.

```java
public static DNode d9(Map<Integer, DNode> index, DNode back) {
    DNode victim = back.prev;
    if (victim.prev == null) {
        return null;
    }
    victim.prev.next = back;
    back.prev = victim.prev;
    victim.prev = null;
    victim.next = null;
    index.remove(victim.value);
    return victim;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `order=[A:4/9,B:9/4]` | `returned=B; order=[A]; map={4:A}; detached=B` |
| `order=[A:5/99]` | `returned=A; order=[]; map={}; detached=A` |
| `order=[A:5/5]` | `returned=A; order=[]; map={}; detached=A` |
| `order=[]` | `returned=null; order=[]; map={}` |

**Targets:** Repair target: expected O(1) time and O(1) auxiliary.

**Edge checks:** Value equal to another key, value absent as a key, key==value control, empty rail.

**Complete when:** Check the map's exact key-to-identity pairs after unlinking and explain a future operation that would fail because of stale state. **Mock follow-up:** Why can a key==value test conceal the defect?

<details>
<summary>Hint 1</summary>

List removal and map removal describe the same logical entry using different access mechanisms.

</details>

<details>
<summary>Hint 2</summary>

A node's lookup identity is not necessarily its stored payload.

</details>

<details>
<summary>Hint 3</summary>

Inspect the argument type and meaning expected by Map.remove, not only whether the call compiles.

</details>

- [ ] **D9** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —

### D10. Recursive frame audit

**Difficulty:** Medium. **Objective:** Repair a missing minimum-input case and an incomplete unwinding postcondition. **Prerequisites:** R2 and bounded structural tests.

Required behavior: recursively reverse an unsorted acyclic chain of at most 200 nodes, returning null for empty input and reusing all original identities without changing values. The result must be null-terminated and acyclic. No data-node allocation. The snippet may throw on an allowed input or return a cyclic structure; never print its result without a bound.

```java
public static ListNode d10(ListNode head) {
    if (head.next == null) {
        return head;
    }
    ListNode newHead = d10(head.next);
    head.next.next = head;
    return newHead;
}
```

| Input / setup | Expected observable result |
| --- | --- |
| `chain=[]` | `head=[]` |
| `chain=[A:7]` | `head=[A]` |
| `chain=[A:1,B:2]` | `head=[B,A]; acyclic=true` |
| `chain=[A:1,B:2,C:3]` | `head=[C,B,A]; acyclic=true` |
| `chain=[A:7,B:7]` | `head=[B,A]; acyclic=true` |

**Targets:** Repair target: O(n+1) time, O(n+1) auxiliary stack.

**Edge checks:** Null, singleton control, two nodes, deeper unwinding, duplicate values.

**Complete when:** Construct independent witnesses for base-case safety and link termination; explain the returned-chain contract of every completed frame. **Mock follow-up:** Why is making the empty input safe insufficient to establish recursive correctness?

<details>
<summary>Hint 1</summary>

The smallest allowed input may not have an object whose field can be read.

</details>

<details>
<summary>Hint 2</summary>

Adding a backward edge can leave an old forward edge in place.

</details>

<details>
<summary>Hint 3</summary>

Check the entire completed-frame postcondition, including the tail, rather than only the returned head.

</details>

- [ ] **D10** — Last: —; Result: —; Minutes: —; Next review: —; Lesson: —


## 13. Mock Interview Practice

Use an existing exercise's record for the attempt; these formats do not create extra exercises or a second log. In a cold mock, hide the topic headings, hints, prior code, and the concepts guide.

| Format | Suggested existing task | Communication focus |
| --- | --- | --- |
| Fundamentals diagnostic, roughly 15–25 minutes | F3 or M1 | References versus objects, ownership, empty boundaries, local edit versus search cost |
| Core structural mock, roughly 30–45 minutes | M2, P3, or G3 | Baseline, invariant, retained continuations, null/head/tail handling, structural tests |
| Design mock, roughly 35–50 minutes | L3 | Required operations, map/list bijection, recency policy, capacity, expected/amortized costs |
| Unfamiliar transfer, roughly 30–45 minutes | One unseen X exercise | Independent method selection and a reason to reject a plausible alternative |

These are flexible pacing diagnostics, not deadlines or readiness guarantees. A conceptual hint makes the result H regardless of speed; substantial help or an unfinished attempt is S.

Ask a partner to use questions such as:

- What does each reference identify at this exact loop boundary?
- Which old edge will the next assignment replace, and what still reaches its target?
- Is this a shared object or merely an equal value?
- What proves no identity is silently lost or represented twice?
- Where could a cycle first be created?
- What changes when the removed node is head, tail, or the only node?
- Which precondition is promised, and which invalid input must your method reject?
- What is the termination measure?
- Does your space claim include frames, helper maps, temporary sentinels, and returned nodes?
- Why is your data structure suitable for this workload?
- What would change if original links were read-only or if inputs could share nodes?
- Can your test distinguish correct values from incorrect identities?

Do not read prepared model answers. After the attempt, compare the explanation with the invariant and edge-case obligations, record one concrete gap, and choose a later unfamiliar variant to test transfer.

## 14. Revision and Spaced Repetition

Follow the roadmap's [flexible review strategy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition). A starting schedule is about **1, 3, 7, and 14 days** after learning, adjusted according to actual retention. Missing a date does not require restarting.

A cold re-solve means opening only the statement and permitted type declarations. Recreate the baseline, identity diagram, invariant, implementation, tests, and complexity without consulting previous code. For a debugging exercise, first predict the failure again; then repair from the required contract rather than recalling a changed line.

Prioritize substantial-review or unfinished attempts (S), hint-assisted attempts (H), lost references or accidental cycles, failed head/tail/null cases, and unresolved complexity errors. Next revisit overdue independent work. Delay comfortable repetitions and use unfamiliar variants when a solution feels memorized.

Keep the next review date and short history on the same original-exercise record. For curated problems, use the existing roadmap Log. Do not copy dates into a separate revision table. If a cold review exposes a conceptual gap, use the roadmap's Reviewing status and reconsider the affected gate; an isolated syntax slip independently corrected does not erase all prior evidence.

## 15. Completion and Readiness

| Milestone | Evidence required | Relationship to roadmap gates |
| --- | --- | --- |
| Level 1 complete | Recreate node types; implement F1–F4 without copied code; distinguish aliases, values, and identities; maintain endpoints and explain costs | Supports the fundamentals needed for T and I |
| Core manipulation reliable | Independently complete M/R/P/G mechanisms; prove reversal, sentinel deletion, gap placement, cycle detection, merge, and composition; validate structural edge cases | Supports I; optional P2 entry recovery is excluded from the core requirement |
| Doubly linked/LRU mechanics reliable | Complete L1–L3 incrementally; defend both directions, lookup identity, update reuse, eviction, and capacity | Core design knowledge and implementation evidence, even though N7 is Additional |
| Essential practice complete | Independently solve or cold re-solve N1–N4, test the platform contracts, and explain why the techniques apply | Supports P; checked assisted attempts alone do not earn it |
| Move to Additional practice | Essential reasoning and blank-editor core drills are reliable; use N5–N7 to target composition, copying, or cache gaps | Extends evidence; does not replace missing fundamentals |
| Optional advanced work | P2 entry proof is comfortable; attempt N8's heap-based approach after Heap / Priority Queue | Optional breadth; not required for ordinary Linked List readiness |
| Linked List interview ready | Repeated independent success on unfamiliar variants across separate sessions, plus the common rubric and category demonstration | Supports R; not earned by document ownership or a single memorized answer |

Before R, independently manipulate custom nodes, explain Java reference assignment and identity, implement iterative reversal and dummy-node edits, merge sorted chains, detect cycles, use fixed gaps, and handle head/tail/null boundaries. Select a method from an unfamiliar statement, explain its invariant and costs, test the actual structure, and communicate the reference changes during a mock.

Use the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) and [Linked List category evidence](./DSA_Interview_Roadmap.md#6-linked-list) as the authority. T, I, P, and R assess different abilities; do not collapse them into a count of completed exercises.

### Objective-to-exercise coverage

| Required objective | Evidence in this workbook |
| --- | --- |
| Custom nodes, null, identity, construction/traversal/search | F1–F2; F3–F4; D2–D4 |
| Known-node insertion/deletion, head/tail ownership | F3–F4; M2; D2, D5 |
| Connect chains and restore retained broken fragments | M1; reference sheet; M2 continuation handling |
| Iterative and recursive reversal | R1–R2; C2, C6; D1, D10 |
| Dummy/sentinel operations | R3; P3; G1; L1; D2 |
| First/second middle and null guards | P1; G2; C5; D3 |
| Cycle detection and optional entry | P2; X5; C5; D4 |
| Fixed gap and tail-relative selection/removal | P3; D5 |
| Stable sorted merge | G1; C3; N2 |
| Split/reverse/weave composition | G2–G3; X2; D6; N5 |
| Deep copy and identity mapping | G4; C7; D7; N6 |
| Doubly linked operations, lookup handles, LRU | L1–L3; C8; D8–D9; N7 |
| Independent method choice and alternatives | X1–X5, including the indexed-query workload |
| Total costs and hidden library work | C1–C8; all coding targets and repair explanations |
| Unfamiliar correctness/communication | Reference sheets, X tasks, mock follow-ups, and readiness evidence |

## 16. References and Delayed Complexity Checks

### Local theory

- [Roadmap: Linked List](./DSA_Interview_Roadmap.md#6-linked-list), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [T/I/P/R](./DSA_Interview_Roadmap.md#four-independent-progress-gates).
- [Linked List — Essential Concepts](./Linked_List_Essential_Concepts.md).
- [Java List / ArrayList / LinkedList](./Java_List_ArrayList_LinkedList.md).
- [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Two Pointers — Essential Concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md).
- [Testing Essential Concepts](./Testing_Essential_Concepts.md).

These use discovered paths. In particular, Two Pointers and Complexity are in subdirectories; the companion Linked List concepts guide is at the repository root.

Library-specific assumptions were checked against Java 21 documentation through Context7: [LinkedList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedList.html) indexed access walks from the nearer end, and [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) removes mappings by key. Expected hash costs depend on distribution; insertion may incur resizing. The [IdentityHashMap documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/IdentityHashMap.html) describes explicit identity equality when a supplied node type has incompatible value equality.

### Delayed cost checks

Open these only after writing the nine requested analysis answers. They check costs and allocation categories, not completed implementation solutions. Your own explanation must still connect the bound to actual node visits and helper contracts.

<details>
<summary>C1 cost check</summary>

Two full scans perform 2n node visits, so time is Θ(n+1), auxiliary O(1), scalar output O(1), and no new nodes. The count is at most 100,000 and the long sum safely covers that many int values. A single scan can reduce constant work without changing the asymptotic bound.

</details>

<details>
<summary>C2 cost check</summary>

Let r=rounds. Each positive round reverses n nodes and then explicitly counts n nodes; the round loop still does work when n=0. Time is Θ(r(n+1)+1). Peak auxiliary is O(1) with iterative R1; the final PassResult is O(1) output. No data nodes are allocated. Endpoint records can be allocated across r calls without all remaining live simultaneously. The visits field equals r*n, not the total number of reference operations. For this diagnostic output, reasoning about parity and counting once can avoid repeated traversals, but a new implementation still needs its own proof.

</details>

<details>
<summary>C3 cost check</summary>

The helper has O(n+m+1) time and O(1) auxiliary under its contract. The following scan visits all n+m nodes, including an attached remainder. Total time is Θ(n+m+1), auxiliary O(1), and the returned record is constant-size. The result nodes reuse input storage. Stable tie order does not require new data nodes.

</details>

<details>
<summary>C4 cost check</summary>

Each get(i) traverses from the nearer end. Summing distances min(i,n−1−i), plus constant work per request, gives Θ(n²+1) time over all indices. Auxiliary space is O(1); the library list already exists as input. Sequential iterator/enhanced-for traversal can compute the same sum in O(n+1) time. Changing only the declared interface type does not change the implementation's indexed-access cost.

</details>

<details>
<summary>C5 cost check</summary>

With the assumed Floyd helper, finite input takes O(n+1) detection plus a linear bounded half-speed scan; total O(n+1) time and O(1) auxiliary. Cyclic input returns after O(μ+λ+1) detection with O(1) auxiliary. The checksum scan adds values at indices 1 through floor(n/2) on finite input. One constant-size result record is output. A set-based helper changes auxiliary space to O(n+1) even though the visible loop uses only two references.

</details>

<details>
<summary>C6 cost check</summary>

Recursive reversal and the later scan contribute O(n+1) each, giving Θ(n+1) total time. The recursive chain has O(n+1) active frames at peak; it is not constant auxiliary space. Those frames have returned before the later scan, so the phases do not multiply or require simultaneous copies of the whole stack. No new data nodes are allocated and the result record is O(1).

</details>

<details>
<summary>C7 cost check</summary>

Let r=copies. Expected time is O(r(n+1)+1), including helper mapping, output traversal, and amortized ArrayList growth. Exactly r*n fresh data nodes are returned indirectly through the list, along with r*n reference slots: O(r*n+1) output storage. For r>0, peak auxiliary mapping is O(n+1); maps from completed calls need not remain reachable. If r=0, working and output setup are O(1). Cumulative allocation can be larger than peak non-output live state. Reusing one batch would violate independence.

</details>

<details>
<summary>C8 cost check</summary>

Over q requests, the expected amortized total is O(q+1) under the cache's hash assumptions. Peak cache storage is O(min(c,q)+1), including sentinels and transient growth; returned int[2] is O(1). With positive capacity, repeated eviction can allocate many nodes over time while retaining only a bounded number. Capacity zero retains no real nodes, but get calls and fixed setup still take work. The cache is temporary to this diagnostic, so its maintained storage counts as auxiliary to c8.

</details>

### Authoring validation

All **225 table examples** were parsed from this document and checked with independent array, identity, arithmetic, and recency models. All **43 Java blocks** compiled together for Java 21 with `--release 21 -Xlint:all -Werror`.

A temporary Java runner passed **373 runtime assertions** covering all **36 complexity-example cases** and **50 debugging-example cases**, plus API and partial-repair checks. It reproduced a real defect in **every D1–D10 subject**. Five additional checks confirmed independent defects remaining after partial repairs in D1, D2, D3, D4, and D10. Observations of suspect output were bounded; checks inspected original identities, null termination, copied random edges, detached nodes, and both linked directions where applicable.

For C/D subjects that call an earlier unfinished starter, the temporary runner supplied verified helper implementations adapted from the concepts guide. The workbook itself retains all implementation stubs. Analysis subjects were checked under their explicitly stated helper contracts.

Structural checks verified **42 original IDs, eight curated problems, 50 unchecked attempt records, 111 progressive hints, and eight delayed cost checks**. The curated order, E/A/C classifications, and difficulties match the local roadmap. All **67 local links and anchors** resolved.

Content hashes confirmed that all **24 pre-existing files** retained their initial contents. This task created only this workbook inside DSA_Guide; temporary authoring, compilation, and validation files remained outside the repository.

The coding stubs remain intentionally incomplete. Authoring checks do not validate a future learner implementation or automatically earn any progress gate. Use the explicit contracts and independent structural tests when completing the exercises.

