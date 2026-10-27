# Linked List — Essential Concepts (Java 21)

> Before changing a link, identify which node you would lose access to and which reference will keep it reachable.

## Table of Contents

- [1. Introduction and Roadmap Scope](#1-introduction-and-roadmap-scope)
- [2. Linked List Fundamentals](#2-linked-list-fundamentals)
- [3. Java References and Node Identity](#3-java-references-and-node-identity)
- [4. Singly and Doubly Linked Nodes](#4-singly-and-doubly-linked-nodes)
- [5. Basic Operations and Reachability](#5-basic-operations-and-reachability)
- [6. Dummy and Sentinel Nodes](#6-dummy-and-sentinel-nodes)
- [7. Iterative and Recursive Reversal](#7-iterative-and-recursive-reversal)
- [8. Finding the Middle with Fast and Slow References](#8-finding-the-middle-with-fast-and-slow-references)
- [9. Cycle Detection and an Optional Entry Extension](#9-cycle-detection-and-an-optional-entry-extension)
- [10. Fixed-Gap References and Tail-Relative Removal](#10-fixed-gap-references-and-tail-relative-removal)
- [11. Merging Sorted Chains](#11-merging-sorted-chains)
- [12. Splitting and Reordering](#12-splitting-and-reordering)
- [13. Copying a Chain with Random References](#13-copying-a-chain-with-random-references)
- [14. LRU Cache with a Map and Doubly Linked List](#14-lru-cache-with-a-map-and-doubly-linked-list)
- [15. Deriving Patterns and Proving Invariants](#15-deriving-patterns-and-proving-invariants)
- [16. Deriving Time and Space Costs](#16-deriving-time-and-space-costs)
- [17. Java Pitfalls and Failing Examples](#17-java-pitfalls-and-failing-examples)
- [18. Comparing Structures and Approaches](#18-comparing-structures-and-approaches)
- [19. Pattern Recognition and Decision Practice](#19-pattern-recognition-and-decision-practice)
- [20. Conceptual Assessment and Answer Key](#20-conceptual-assessment-and-answer-key)
- [21. Key Takeaways and Interview Explanation](#21-key-takeaways-and-interview-explanation)
- [22. References, Validation, and Next Steps](#22-references-validation-and-next-steps)

## 1. Introduction and Roadmap Scope

This document develops the [Linked List category](./DSA_Interview_Roadmap.md#6-linked-list) in **Phase 2 — Search, Windows, and Pointer Structures**. The local roadmap determines the required objectives and E/A/C practice priorities. The aim is to reconstruct algorithms from their structural promises: which nodes exist, which links change, and why every required node remains reachable.

You have already met `java.util.LinkedList` as a collection. Interview problems usually expose custom nodes instead. The new work is reasoning about object identity, aliases, head changes, link order, and ownership. Reading code is useful; drawing the references and rebuilding the mechanism without notes is the implementation evidence.

### Prerequisites to reuse

| Existing material | Reuse here | New work in this guide |
| --- | --- | --- |
| [Phase 0 foundations](./DSA_Interview_Roadmap.md#phase-0-foundations) | Null checks, recursion, invariants, input constraints | State a structural invariant before changing links |
| [Arrays & Hashing](./DSA_Interview_Roadmap.md#1-arrays--hashing) and its [concept guide](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Membership, mappings, expected hash costs | Store node identities or key-to-node handles |
| [Two Pointers](./DSA_Interview_Roadmap.md#2-two-pointers) and its [proof method](./Two_Pointers/Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements) | Explain coordinated movement and termination | Follow links instead of using arbitrary indexed access |
| [Java Arrays](./Java_Array.md) | Reference elements, aliasing, copies | Distinguish copying a reference from copying a node |
| [Java List reference](./Java_List_ArrayList_LinkedList.md) | Library list behavior and costs | Work with custom nodes whose links are directly accessible |
| [Java Map reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Set reference](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) | Equality contracts, map/set choices | Preserve node identity and synchronize two structures |
| [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Loop bounds, recursion, auxiliary space | Count link traversals, stack frames, map entries, and newly allocated nodes |
| [Testing concepts](./Testing_Essential_Concepts.md) | Independent expected results and boundary tests | Test node identities, reachability, and absence of unintended cycles |

Review a prerequisite when a trace exposes a gap. Prior completion of Stack, Binary Search, or Sliding Window is useful background, but these are separate branches rather than additional mathematical prerequisites for every linked-list task.

### Coverage and priorities

| Roadmap objective | Main sections | Independent evidence |
| --- | --- | --- |
| Define a node; distinguish identity and equal values | 2–4 | Draw two equal-valued objects and their aliases |
| Use a sentinel for deletion and merging | 5–6, 10–11 | Explain the same predecessor rule at the head and in the middle |
| Save next before rewiring | 5, 7, 12 | Reverse without losing the unprocessed suffix |
| Use fast/slow references for midpoint and cycles | 8–9 | Explain even-length conventions and why meeting is inevitable |
| Maintain a fixed gap for tail-relative positions | 10 | Derive the gap and stopping condition together |
| Compose midpoint, reversal, and merge | 11–12 | Preserve every original node exactly once without a cycle |
| Combine a map and doubly linked list for LRU | 14 | Explain lookup, movement, eviction, and cross-structure consistency |
| Complete the custom-node and deliberate-cycle drills | 4–12, 17 | Rebuild and test from a blank editor, including boundary inputs |
| Meet the common and category readiness rubrics | 19–22 | Transfer the reasoning to an unfamiliar variant |

Basic doubly linked structure, recursive reversal, random-reference copying, and explicit LRU mechanics are covered because they explain the requested interview mechanisms. **Cycle-entry recovery and the interleaving copy technique are optional extensions.** Heap-based merging of k lists is a deferred challenge after Heap / Priority Queue. Core LRU reasoning remains an essential knowledge objective even though its named platform problem is Additional.

The following table preserves the roadmap's exact practice order and classifications. These are curriculum priorities from the local roadmap, not official NeetCode tier labels.

| Priority | Problem | Difficulty | Focus |
| --- | --- | --- | --- |
| E — Essential | [Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/) | Easy | Save the successor and reverse links |
| E — Essential | [Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/) | Easy | Result tail, sentinel, and sorted prefixes |
| E — Essential | [Linked List Cycle](https://leetcode.com/problems/linked-list-cycle/) | Easy | Relative movement and identity |
| E — Essential | [Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/) | Medium | Fixed gap and predecessor |
| A — Additional | [Reorder List](https://leetcode.com/problems/reorder-list/) | Medium | Split, reverse, weave |
| A — Additional | [Copy List with Random Pointer](https://leetcode.com/problems/copy-list-with-random-pointer/) | Medium | Old-to-new identity mapping |
| A — Additional | [LRU Cache](https://leetcode.com/problems/lru-cache/) | Medium | Map plus movable doubly linked nodes |
| C — Optional advanced challenge | [Merge k Sorted Lists](https://leetcode.com/problems/merge-k-sorted-lists/) | Hard | Defer heap-based merging until Heap / Priority Queue |

The implementations below are teaching mechanisms with explicit local contracts and original traces. They are not judge-specific `Solution` classes or an answer bank. Re-derive each platform interface from its current statement.

Use the existing [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates): theory explanation supports T; reliable blank-editor drills support I; independent Essential solves or cold re-solves support P; unfamiliar transfer across separate sessions supports R. Owning this file earns no gate.

## 2. Linked List Fundamentals

### 2.1 A sequence represented by relationships

A linked list represents order through references between nodes. A singly linked node stores a value and a reference to its successor. One externally held reference identifies the first node. Following successors discovers the sequence.

```text
head
 |
 v
A:[10 | next] ---> B:[20 | next] ---> C:[30 | null]
                                      ^
                                      |
                                     tail
```

A, B, and C are **identity labels**, chosen for the drawing. They are not memory addresses and are not stored by the implementations. The field `A.next` refers to B; `B.next` refers to C; `C.next` contains `null`. The optional `tail` variable also refers to C. It does not create another node.

Why represent order this way? If you already hold a suitable node or predecessor, you can insert or remove a neighboring node by changing a constant number of links. You can also move an existing node without shifting a whole suffix of values. This is valuable in algorithms with frequent edits at known positions or direct handles from another structure.

The tradeoff is access. There is no `head[7]` operation: to reach position 7, follow seven successors from the head. A singly linked list also cannot move backward through `next`. A tail reference gives direct access to the final node; it does not reveal its predecessor.

### 2.2 Logical structure and managed memory

The arrows describe Java object references. Java manages object storage and reclamation; source code does not expose pointer arithmetic or promise particular physical addresses for these nodes. Objects that are neighbors in the list need not have adjacent physical storage. Moving a link changes the logical chain, not the physical position of an object.

An empty ordinary list has `head == null`. A singleton has `head` referring to one node whose `next == null`. A tail field, if maintained, must agree with the final reachable node and must be null exactly when the head is null.

A list does not necessarily know its size. Counting reachable nodes costs O(n), unless an owning container maintains a size field consistently after every edit. That metadata makes `size()` cheap; it does not make arbitrary indexed access cheap.

### 2.3 Arrays, ArrayList, and custom linked lists

| Property | `int[]` / reference array | `ArrayList<E>` | Custom singly linked list |
| --- | --- | --- | --- |
| Order represented by | Logical indexed slots | Backing array of references | Successor references |
| Size changes | Requires a different array | Managed resizing | Allocate/unlink nodes |
| Read arbitrary index | O(1) | O(1) | O(n) worst case |
| Insert at beginning | Shift/copy when preserving sequence | O(n) shifts | O(1) with head |
| Append | Needs spare-slot convention or new array | Amortized O(1) | O(1) with tail; O(n) by scanning |
| Edit after a known node | Typically shift suffix | Typically shift suffix | O(1) local rewiring |
| Full traversal | O(n) | O(n) | O(n) |
| Extra structural storage | Array object and slots | Backing array and spare capacity | Node object and link per element |

An object array and an ArrayList store references to elements; copying those slots does not deep-copy element objects. A linked list also connects existing objects by references. The important distinction is how the order is represented and which operations the representation makes cheap.

Arrays and ArrayList often have practical locality advantages for scanning, while separate linked nodes incur allocation and reference-following costs. Treat those as implementation/performance considerations, not as promises of exact object layout or universal benchmark results. See the [existing list comparison](./Java_List_ArrayList_LinkedList.md#5-choosing-arraylist-or-linkedlist).

**Self-check Q1:** Why does having a tail reference fail to make singly linked tail deletion O(1)?

**Self-check Q2:** If two adjacent values are equal, how many nodes exist?

## 3. Java References and Node Identity

This section uses the `ListNode` declaration introduced in Section 4. The Java rules are straightforward: a reference identifies an object or is null; assignment copies a reference value; several variables can therefore refer to one mutable object. These semantics are described in [JLS 4.3.1](https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.1).

### 3.1 Objects and the variables that refer to them

Consider these statements, in this order:

```java
static ListNode referenceExample() {
    ListNode first = new ListNode(10);  // object A
    ListNode second = new ListNode(20); // object B
    first.next = second;
    ListNode alias = first;
    alias.val = 11;
    second = new ListNode(30);          // object C
    return first;
}
```

The reference changes are:

| Statement completed | `first` | `second` | `alias` | Object links/state |
| --- | --- | --- | --- | --- |
| First allocation | A | Not declared yet | Not declared yet | A has value 10 and next null |
| Second allocation | A | B | Not declared yet | B has value 20 and next null |
| `first.next = second` | A | B | Not declared yet | A.next is B |
| `alias = first` | A | B | A | No new node |
| `alias.val = 11` | A | B | A | A.val becomes 11 |
| Reassign `second` | A | C | A | A.next is still B; C has value 30 |

```text
first ----+
          +---> A:[11] ---> B:[20] ---> null
alias ----+

second --------> C:[30] ---> null
```

Assigning `second` later does not change `A.next`. That field stored the reference value that `second` held when the assignment occurred, not a live connection to the variable named `second`. The returned chain is A(11) → B(20). The unrelated allocation C is not part of that chain.

### 3.2 Moving a local reference versus changing a link

Suppose `head` and `current` both initially refer to A in A → B → C. Each row below is independent and starts from that state.

| Statement | What changes | What stays the same |
| --- | --- | --- |
| `current = current.next` | Local variable current now refers to B | A.next is still B; the chain from head is unchanged |
| `current.next = anotherNode` | The next field of the object current refers to | Other local variables retain their reference values |
| `current = null` | Local variable current no longer refers to a node | Existing nodes and their links are unchanged |
| `head.next = null` | A.next becomes null | Any other variable already referring to B still refers to B |

A variable assignment changes one reference slot. A field assignment changes a reference stored **inside an object**. If several aliases reach that object, they observe the same changed field.

The receiver must exist. `current = current.next` and `current.next = anotherNode` both dereference current, so both fail with `NullPointerException` if current is null. Assigning `current = null` is legal.

### 3.3 Java passes reference values by value

At a method call, Java initializes fresh parameter variables from the supplied argument values. When an argument is a reference, the copied value refers to the same object; the caller's variable itself is not passed as a replaceable slot. See [JLS 15.12.4.5](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.5).

```java
static void rebindOnly(ListNode node) {
    node = new ListNode(99);
}

static void changeFirstValue(ListNode node, int value) {
    if (node != null) {
        node.val = value;
    }
}

static ListNode dropFirstReference(ListNode head) {
    return head == null ? null : head.next;
}
```

Starting with caller variable `head` → A(10) → B(20):

| Call | Caller after the call |
| --- | --- |
| `rebindOnly(head)` | head still refers to A; the replacement was local |
| `changeFirstValue(head, 12)` | head still refers to A, whose value is now 12 |
| `dropFirstReference(head)` with result ignored | head still refers to A |
| `head = dropFirstReference(head)` | head now refers to B; A.next itself was not changed |

Returning a new head lets the caller explicitly store it. Another valid interface is an owning container whose `head` field the method updates. A one-element array wrapper can also expose a mutable slot, but it usually obscures a simple interview contract.

Saying “Java passes objects by reference” hides the distinction that matters here. Java passes values, including reference values. C/C++ pointer syntax is unnecessary for these algorithms; Java supplies object access and reference equality without direct pointer arithmetic.

### 3.4 Identity, values, aliases, and lifetime

For two distinct objects A(7) and B(7):

- `a == b` is false: they are different nodes.
- `a.val == b.val` is true: their integer values match.
- For our plain classes, inherited `equals` also uses identity. Another class could override it to use different semantics.
- `a == null` asks whether a identifies no object.

Cycle detection, intersection, and copying need structural identity. Repeated value 7 is not proof of revisiting a node. Two different lists may even share a suffix: A → C and B → C refer to the same C object. Rewiring C through one head changes what the other head can reach.

Losing reachability **from the current head** is different from making an object unreachable from all live references. Garbage collection can reclaim objects that are no longer reachable from the running program, including unreachable cycles; setting a local variable to null is neither immediate deallocation nor a guarantee that an object is collectible. Exact collection timing is outside these algorithm contracts.

Detaching a removed node with `removed.next = null` can make its isolated status explicit, but an external alias observes that mutation too. This guide states when deletion detaches a node.

**Self-check Q3:** After `alias = first`, how many nodes did that assignment create?

**Self-check Q4:** Why does `node = new ListNode(99)` inside a method not replace the caller's head?

**Self-check Q5:** If A.next is B and B.next is C, what changes when a local reference moves from A to B?

## 4. Singly and Doubly Linked Nodes

### 4.1 Shared teaching types and compilation convention

All Java blocks in this document are **members of one teaching class**, for example `LinkedExamples`. Place them inside that class in document order. Use `import java.util.*;` before the outer class. The static nested classes keep the examples together; a judge may instead supply a top-level node with different field names. No preview features or external dependencies are needed.

```java
static final class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
        this.val = val;
    }
}
```

`val` is an integer field. `next` is a reference field of the same node type. A new node's reference field defaults to null. The constructor sets the supplied value; `this` identifies the object being constructed. Both fields are mutable. The class deliberately declares no value-based `equals` or `hashCode`.

We use an ordinary class because the links must change. A record's component fields are final and its generated equality is based on components; it is not a drop-in representation for this mutable identity model. Final on the **class** above prevents subclassing; it does not freeze its fields.

```java
static ListNode sampleChain() {
    ListNode a = new ListNode(10);
    ListNode b = new ListNode(20);
    ListNode c = new ListNode(30);
    a.next = b;
    b.next = c;
    return a;
}
```

This method allocates exactly three data nodes and returns a reference to the first. No separate container is required. A variable named `head` is just a conventional entry reference.

### 4.2 Doubly linked structure

```java
static final class DoublyNode {
    int val;
    DoublyNode prev;
    DoublyNode next;

    DoublyNode(int val) {
        this.val = val;
    }
}
```

```text
head                                      tail
 |                                         |
 v                                         v
A:[prev=null,10,next=B] <-> B:[20] <-> C:[prev=B,30,next=null]
```

For adjacent reachable nodes x and y, the intended relationship is `x.next == y` **and** `y.prev == x`. Updating only one direction leaves inconsistent structure. In a null-terminated list, head.prev and tail.next are null. With sentinels, the boundary rules are different and must be stated explicitly.

Doubly linked nodes support constant local unlinking when the node itself is known because both neighbors are available. Singly linked removal generally needs the predecessor. Do not import the “copy the next value into this node” trick into an identity-sensitive contract: it changes which object represents a value, cannot remove a tail this way, and can violate external handles.

### 4.3 Shared contracts for the algorithms

Unless a section explicitly changes the contract:

1. The input is a finite, acyclic, null-terminated singly linked chain. `null` is a valid empty head.
2. Node values are arbitrary `int` values. Values need not be distinct.
3. Destructive algorithms may change links. They preserve values and the specified surviving node identities. The caller stores the returned head when the interface returns one.
4. Inputs to an in-place merge are sorted and **node-disjoint**, and the caller permits their links to be consumed.
5. Mutations occur with exclusive structural ownership: no concurrent editing and no unaccounted shared suffix. External aliases, if present, observe link changes.
6. Cycle routines explicitly accept a reachable cycle. Random-copy and LRU sections define their own structure contracts.
7. Complexity counts constant-size references and int operations as O(1). With n nodes, assume n fits an int; numeric aggregation, if added, may need long.
8. Basic routines do not first validate acyclicity, sortedness, membership, or disjointness. Such checks add work or state. State a precondition rather than pretending these properties were verified for free.

A method that works on ordinary chains is not automatically safe on a cycle. An unbounded `while (node != null)` printer can hang on deliberate cyclic input. For debugging, impose a node limit or track seen **identities**; test a cycle without passing it through an ordinary length or reversal routine.

### 4.4 Custom nodes and the library LinkedList

| Question | Custom `ListNode` chain | `java.util.LinkedList<E>` |
| --- | --- | --- |
| Can algorithm code edit `next` directly? | Yes | Internal nodes are not exposed through the public API |
| What does an element represent? | A field on a node | An E stored inside a library-managed node |
| Can random references or shared suffixes be represented directly? | Yes, with suitable types/contracts | Not by accessing its hidden links |
| Can it satisfy a judge expecting `ListNode`? | Match the supplied type | A collection object has a different interface |
| Is indexed access constant time? | No | No; indexed operations walk from the nearer end |

Storing custom nodes **as elements** of a library LinkedList does not make that collection's internal links equal to your nodes' `next` fields. It creates two different relationships.

Java 21 documents the library class as a doubly linked List/Deque with traversal for indexed operations. See the [official LinkedList API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedList.html) and the [local reference](./Java_List_ArrayList_LinkedList.md#4-linkedlist-linked-nodes-and-deque-operations).

**Self-check Q6:** Which bidirectional relationship must hold after inserting B between A and C?

## 5. Basic Operations and Reachability

Begin every edit with a small drawing. Identify the incoming link being replaced and save any successor needed later. Then update the caller's head or the owner's endpoint metadata if necessary.

### 5.1 Traversal, counting, and search

Start a local cursor at head. After visiting its node, follow next. Counting and searching use the same structural walk; neither changes a link.

```java
static int countNodes(ListNode head) {
    int count = 0;
    for (ListNode current = head; current != null; current = current.next) {
        count++;
    }
    return count;
}

static ListNode findFirst(ListNode head, int target) {
    for (ListNode current = head; current != null; current = current.next) {
        if (current.val == target) {
            return current;
        }
    }
    return null;
}
```

For A(4) → B(9) → C(4), counting proceeds:

| Visited node | Count afterward | Next cursor |
| --- | --- | --- |
| A(4) | 1 | B |
| B(9) | 2 | C |
| C(4) | 3 | null |

Searching for 4 returns **A**, not an arbitrary equal-valued node; searching for 9 visits A and B; searching for 8 visits all three and returns null. The invariant is that the cursor begins the unvisited suffix, and the count or search result concerns exactly the visited prefix. Following next shortens that suffix until null or a match.

Counting is O(n) time and O(1) auxiliary space. Search is O(n) worst-case time, O(1) auxiliary; an immediate hit is O(1). Both handle an empty head without dereferencing it. Their output is a scalar or a reference to an existing node, not a copied list.

### 5.2 Insertion at the head

The new node must point to the **old** head before becoming the returned head.

```java
static ListNode prepend(ListNode head, int value) {
    ListNode added = new ListNode(value);
    added.next = head;
    return added;
}
```

```text
Before: head -> A(10) -> B(20) -> null
1. Allocate X(5).
2. X.next = A.
3. Caller stores head = X.
After:  head -> X(5) -> A(10) -> B(20) -> null
```

The old chain is reachable through X.next, so its order and identities remain intact. For an empty input, X.next is null. Time O(1), O(1) working references, and one new node in the result. A separately maintained tail stays unchanged unless the original chain was empty, when both endpoints become X.

### 5.3 Tail insertion with and without a tail reference

Without a tail, find the last node whose next is null. Only the final link edit is constant time.

```java
static ListNode appendByScan(ListNode head, int value) {
    ListNode added = new ListNode(value);
    if (head == null) {
        return added;
    }
    ListNode last = head;
    while (last.next != null) {
        last = last.next;
    }
    last.next = added;
    return head;
}
```

For A → B → null, last starts at A, advances to B, stops, and B.next becomes X. The invariant is that last is a reachable original node and the original chain has not yet changed. The loop ends at its unique tail. The input head is retained unless the chain was empty. O(n) time, O(1) working state, one new result node.

If an owner maintains both endpoints, no search is needed:

```java
static final class TailChain {
    ListNode head;
    ListNode tail;

    void append(int value) {
        ListNode added = new ListNode(value);
        if (tail == null) {
            head = added;
        } else {
            tail.next = added;
        }
        tail = added;
    }
}
```

| Append on a new TailChain | Head afterward | Tail afterward | Chain |
| --- | --- | --- | --- |
| 10 | A | A | A(10) |
| 20 | A | B | A(10) → B(20) |
| 30 | A | C | A(10) → B(20) → C(30) |

The owner invariant is `head == null` exactly when `tail == null`; otherwise tail is the last node reachable from head and tail.next is null. Append preserves it in both branches. Each append is O(1) time and working space, with one new node. Appending n items costs O(n); repeating appendByScan on a growing chain costs 0+1+...+(n−1) = O(n²).

This minimal owner demonstrates append only. Do not call unrelated bare-head mutators on its head and expect its tail metadata to repair itself. A full container must update head, tail, and any size field on every operation.

### 5.4 Insertion after a known node

The contract requires a nonnull node already belonging to the intended chain.

```java
static ListNode insertAfter(ListNode node, int value) {
    if (node == null) {
        throw new IllegalArgumentException("node must be nonnull");
    }
    ListNode added = new ListNode(value);
    added.next = node.next;
    node.next = added;
    return added;
}
```

For A → C, preserve C by setting X.next = C, then A.next = X. The result is A → X → C. Reversing these assignments carelessly, as in `node.next = added; added.next = node.next;`, makes X point to itself and loses C from this head.

For a tail A, the old next is null and X becomes the tail. O(1) time and working references, one newly allocated result node. If the location must first be found by value or index, add O(n) search time. An owning tail reference must change when insertion follows the old tail.

### 5.5 Deletion at the head and after a known predecessor

These deletion methods detach the removed node, leaving its next null.

```java
static ListNode removeHead(ListNode head) {
    if (head == null) {
        return null;
    }
    ListNode newHead = head.next;
    head.next = null;
    return newHead;
}

static ListNode deleteAfter(ListNode predecessor) {
    if (predecessor == null) {
        throw new IllegalArgumentException("predecessor must be nonnull");
    }
    ListNode removed = predecessor.next;
    if (removed == null) {
        return null;
    }
    predecessor.next = removed.next;
    removed.next = null;
    return removed;
}
```

| Operation on a fresh A → B → C | Saved reference | Link updates | Result |
| --- | --- | --- | --- |
| removeHead(A) | newHead = B | A.next = null | Return B → C; A detached |
| deleteAfter(A) | removed = B | A.next = C; B.next = null | Chain A → C; return B |
| deleteAfter(B) | removed = C | B.next = null; C.next = null | Chain A → B; return C |
| deleteAfter(C) | removed = null | None | Return null; original chain unchanged |

For a singleton, removeHead returns null. The owner of a tail field must set it to null after removing the only node; when deleting after a predecessor removes the tail, that predecessor becomes the new tail.

The correct order saves the survivor or victim before overwriting the only convenient incoming link. These methods take O(1) time and auxiliary space and allocate no nodes. They do not search for the predecessor. A removed object still exists if another reference reaches it; detachment is a structural operation, not a request to manually free memory.

### 5.6 Removing the first node with a chosen value

The simplest interface returns the possibly changed head. This version intentionally exposes the head special case before Section 6 removes it.

```java
static ListNode removeFirstValueNoDummy(ListNode head, int target) {
    if (head == null) {
        return null;
    }
    if (head.val == target) {
        return removeHead(head);
    }
    ListNode predecessor = head;
    while (predecessor.next != null && predecessor.next.val != target) {
        predecessor = predecessor.next;
    }
    if (predecessor.next != null) {
        deleteAfter(predecessor);
    }
    return head;
}
```

For A(8) → B(3) → C(3), target 3: predecessor starts at A; A.next already matches, so B is bypassed and detached. Result A(8) → C(3). Target 8 removes A; target 5 scans to C and leaves the chain unchanged. Only the **first** matching node is removed.

The invariant is that every data node before predecessor.next has been checked and is not a matching node eligible for removal. Initially the head is already checked. Moving predecessor checks one more candidate; stopping identifies the first match or the end. O(n) worst-case time, O(1) auxiliary space.

**Self-check Q7:** Which reference must be saved before detaching the head?

**Self-check Q8:** Why can inserting after a known node be O(1) while inserting at a requested index is O(n)?

## 6. Dummy and Sentinel Nodes

### 6.1 Give the head a predecessor

Head deletion is awkward because the caller's head variable is an incoming reference outside the chain. An ordinary internal node has a predecessor whose next can be updated. A dummy node supplies that predecessor uniformly.

```text
Without dummy: head -> A -> B -> null
With dummy:    dummy D -> A -> B -> null
                         ^
                         |
                    original head

Delete A by changing D.next, just as deleting B changes A.next.
Returned data head is D.next.
```

The dummy is not an input data node. Its value has no semantic role; using zero does not reserve zero as a forbidden data value. Identify it by its role/reference, not by comparing values.

```java
static ListNode removeFirstValue(ListNode head, int target) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode predecessor = dummy;
    while (predecessor.next != null && predecessor.next.val != target) {
        predecessor = predecessor.next;
    }
    if (predecessor.next != null) {
        deleteAfter(predecessor);
    }
    return dummy.next;
}
```

On A(8) → B(3) → C(3):

| Target | Search begins at | Predecessor at removal/termination | Returned chain |
| --- | --- | --- | --- |
| 8 | D | D | B → C |
| 3 | D | A | A → C |
| 5 | D | C, no successor | A → B → C |
| Any target on empty input | D | D, no successor | null |

The invariant now starts before **all** data nodes: the nodes already passed are nonmatching, and predecessor.next is the first unexamined candidate. Head and interior deletion follow the same rule. The removed node is detached by deleteAfter. Time remains O(n); the single dummy and fixed references use O(1) auxiliary space.

### 6.2 Local dummy versus persistent sentinels

| Choice | Boundary handling | Storage | Typical use |
| --- | --- | --- | --- |
| No dummy | Explicit empty/head branches | O(1) references | Tiny operations such as prepend |
| One temporary dummy | Always have a predecessor | One O(1) auxiliary node per call | Removal and merging |
| Two persistent sentinels | Every real doubly linked node has two neighbors | Two nodes per owner | LRU recency list |

A sentinel simplifies structural boundary cases; it does not justify dereferencing arbitrary null references. For example, a fast reference can still run beyond the chain while establishing a gap.

Returning dummy instead of dummy.next inserts an unwanted data position and exposes the sentinel. On empty input it incorrectly returns one node. Dummy nodes also do not solve shared-input aliasing or guarantee sortedness.

**Self-check Q9:** Why does allocating one dummy node still count as O(1) auxiliary space?

**Self-check Q10:** What makes a dummy node different from an ordinary node whose value happens to be zero?

## 7. Iterative and Recursive Reversal

The task reverses **links between existing nodes**. Reversing only their values can produce the same displayed numbers but violate the identity contract. A simple copy-based baseline reads the values and allocates a new reversed chain in O(n) time with O(n) newly allocated output nodes. An array or stack of original node handles can also guide rewiring using O(n) auxiliary storage. Neither is needed when we can preserve the remaining suffix with one saved reference.

### 7.1 Derive iterative reversal from two portions

Suppose the original chain is A → B → C → null. Maintain:

- `previous`: the head of the already reversed prefix.
- `current`: the head of the untouched suffix.
- `savedNext`: a temporary reference preserving the suffix after current.

Initially previous is null and current is A. Transfer one node from the suffix to the reversed prefix.

```java
static ListNode reverseIterative(ListNode head) {
    ListNode previous = null;
    ListNode current = head;
    while (current != null) {
        ListNode savedNext = current.next;
        current.next = previous;
        previous = current;
        current = savedNext;
    }
    return previous;
}
```

One iteration, before local references advance:

```text
Before:
previous -> reversed prefix
current  -> X -> savedNext -> untouched remainder

After current.next = previous:
current  -> X -> reversed prefix
savedNext -> untouched remainder

Then previous = current; current = savedNext.
```

For A(10) → B(20) → C(30), each row records the completed transfer; the Current column names the node processed during that row.

| Step | Previous before | Current | Saved Next | Link Changed | Resulting Chain / remaining suffix |
| --- | --- | --- | --- | --- | --- |
| 1 | null | A | B | A.next = null | previous: A → null; current: B → C |
| 2 | A | B | C | B.next = A | previous: B → A → null; current: C |
| 3 | B | C | null | C.next = B | previous: C → B → A → null; current: null |

The original head A becomes the tail because its link is changed to null on the first iteration. The final processed node C becomes the new head. The caller must use `head = reverseIterative(head)` when replacing its entry reference; an ignored return leaves the caller pointing to the old head, now the tail.

### 7.2 Prove the invariant

At each loop start:

1. The original processed prefix is represented in reverse order from previous.
2. The original unprocessed suffix is reachable from current in original order.
3. These two chains contain disjoint sets of original nodes, and their union is every original node.
4. The reversed prefix is null-terminated.

**Initialization:** the processed prefix is empty and current reaches the complete input. All four claims hold.

**Preservation:** savedNext captures current's successor before any change. current.next attaches that one node before the reversed prefix. It cannot form a cycle because current belongs to the disjoint unprocessed suffix. Moving previous and current restores the partition with one more processed node.

**Progress:** the number of unprocessed nodes decreases by one. On a finite acyclic input it reaches zero.

**Termination:** current is null, so previous reaches every original node exactly once, in reverse order, ending at the original head. Values have never changed.

Each node is processed once with a constant number of reference operations: O(n) time, O(1) auxiliary space, no new output nodes. Empty input returns null; a singleton keeps the same identity and null next; two nodes exchange head/tail roles.

### 7.3 Why saving next is indispensable

If you execute `current.next = previous` and then try `current = current.next`, you follow the newly reversed edge. On the first iteration previous is null, so the cursor becomes null and the original B → C suffix is no longer reachable through the algorithm's saved state. The correct “next to process” is the old successor, which must be retained before rewiring.

Another error is beginning previous at head while current also starts at head. The first change makes head.next point to head, producing a self-cycle. Initialization is part of the proof.

### 7.4 Recursive reversal as a smaller-chain contract

Ask the recursive call to reverse the suffix beginning at head.next and return its new head. Once that smaller task finishes, the old immediate successor has become the tail of the reversed suffix. Attach head after it and terminate head's next.

```java
static ListNode reverseRecursive(ListNode head) {
    if (head == null || head.next == null) {
        return head;
    }
    ListNode newHead = reverseRecursive(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
}
```

For A → B → C:

```text
Calls: reverse(A) -> reverse(B) -> reverse(C)
Base:  reverse(C) returns C.

Unwind B:
  C.next = B
  B.next = null
  return C, representing C -> B -> null

Unwind A:
  B.next = A
  A.next = null
  return C, representing C -> B -> A -> null
```

| Active frame | Suffix already reversed | Attachment | Required break | Returned head |
| --- | --- | --- | --- | --- |
| C | Base singleton C | None | C.next already null | C |
| B | C | C.next = B | B.next = null | C |
| A | C → B | B.next = A | A.next = null | C |

Why does head.next still identify the old successor? The recursive call rewires the suffix nodes; it does not reassign the caller frame's head.next field. That old successor becomes the suffix's tail. Both assignments are required: between the attachment and the break there can briefly be a backward/forward pair forming a cycle. The completed frame removes the old forward link before returning.

The induction is the same partition idea expressed through a recursive contract: the suffix is correctly reversed by the smaller call; attach exactly one preceding node; terminate the new tail. The call count and maximum live frame count are both O(n), so time is O(n) and auxiliary **stack space is O(n)**. The returned head is one reference to reused nodes.

There is no Java guarantee that a long recursive list reversal will fit the available stack. Prefer the iterative form for long chains or a constant-auxiliary-space requirement. Empty and singleton inputs finish at the base case.

| Aspect | Iterative | Recursive |
| --- | --- | --- |
| State carrying unfinished work | previous/current/savedNext | Active frames and returned suffix head |
| Time | O(n) | O(n) |
| Auxiliary space | O(1) | O(n) call stack |
| New data nodes | None | None |
| Main structural risk | Losing old next | Forgetting to terminate the old forward link |
| Typical interview default | Useful for space constraints | Useful for explaining recursive contracts |

**Self-check Q11:** Why does overwriting current.next before saving it lose useful information?

**Self-check Q12:** Why is the recursive version not O(1) auxiliary space?

**Self-check Q13:** What would happen on a two-node list if `head.next = null` were omitted during recursive unwinding?

## 8. Finding the Middle with Fast and Slow References

### 8.1 Derive the speed relationship

A direct baseline counts n nodes, then walks to a chosen middle index. That is O(n) time and O(1) auxiliary space with two passes. A reference array gives indexed access but uses O(n) extra storage.

One pass can encode the length relationship: after t iterations, slow has moved t links and fast has moved 2t. When fast reaches the end, slow is near halfway. Unlike an array pointer, fast does not jump by address arithmetic; `fast.next.next` follows two actual links.

### 8.2 Second-middle and first-middle conventions

For A → B → C → D, there are two central nodes, B and C. State which is required.

```java
static ListNode secondMiddle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}

static ListNode firstMiddle(ListNode head) {
    if (head == null) {
        return null;
    }
    ListNode slow = head;
    ListNode fast = head;
    while (fast.next != null && fast.next.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}
```

The first guard permits the final two-link movement that takes fast past an even-length chain. The second stops when fast is already at the last or next-to-last node. Short-circuit `&&` ensures that a later dereference is evaluated only after its prerequisite succeeds.

For **secondMiddle** on A → B → C → D → E:

| Iteration completed | Slow | Fast | Observation |
| --- | --- | --- | --- |
| 0 | A | A | Both at the first node |
| 1 | B | C | One step versus two |
| 2 | C | E | Fast.next is null; return C |

For an even chain A → B → C → D:

| Method | Iteration completed | Slow | Fast | Observation |
| --- | --- | --- | --- | --- |
| secondMiddle | 0 | A | A | Continue |
| secondMiddle | 1 | B | C | Continue |
| secondMiddle | 2 | C | null | Return C |
| firstMiddle | 0 | A | A | Continue |
| firstMiddle | 1 | B | C | C.next.next is null; return B |

| Length | firstMiddle index | secondMiddle index |
| --- | --- | --- |
| 0 | No node | No node |
| 1 | 0 | 0 |
| 2 | 0 | 1 |
| 3 | 1 | 1 |
| 4 | 1 | 2 |
| 5 | 2 | 2 |

For a nonempty length n, indices are floor((n−1)/2) and floor(n/2), respectively. The movement invariant explains these formulas and each guard fixes the stopping convention. Each method takes O(n) time and O(1) auxiliary space and leaves every link unchanged.

The finite-chain precondition matters: these middle routines do not stop on a reachable cycle. First establish that the input is acyclic when the statement does not guarantee it. For later reordering, the first-middle convention ensures the left half has at least as many nodes as the right half.

**Self-check Q14:** What returns on a two-node chain under each convention, and why?

## 9. Cycle Detection and an Optional Entry Extension

A reachable cycle means following next revisits the **same node object**. For A(5) → B(5) → null, equal values do not form a cycle. For A → B → C → B, the repeated B identity does.

Use μ for the number of nodes before the cycle entry and λ for the cycle length. Then μ+λ is the number of distinct reachable nodes in a cyclic chain.

### 9.1 A direct visited-set baseline

Remember each visited node and detect an identity encountered twice.

```java
static boolean hasCycleWithSet(ListNode head) {
    Set<ListNode> seen = new HashSet<>();
    for (ListNode current = head; current != null; current = current.next) {
        if (!seen.add(current)) {
            return true;
        }
    }
    return false;
}
```

This HashSet is correct because our final ListNode class inherits identity equality/hash behavior. If a supplied node type uses value-based equality, use an identity-backed set instead; otherwise A(5) and B(5) could be mistaken for a revisit. A suitable identity-backed construction is `Collections.newSetFromMap(new IdentityHashMap<ListNode, Boolean>())`.

For n distinct reachable nodes, this approach takes expected O(n) time and O(n) auxiliary storage under suitable hashing. It is simple and can also identify the first repeated node. It never mutates the chain.

### 9.2 Floyd's Tortoise and Hare algorithm

A faster walker on a circular track eventually catches a slower one. Before the cycle, they follow the same entry path; inside the cycle, the relative position advances by one modulo λ per iteration. One of the next λ relative positions must be zero.

```java
static boolean hasCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) {
            return true;
        }
    }
    return false;
}
```

The initial equality is not evidence: both start at head even on an acyclic chain. Compare after movement.

For A → B → C → D → E → C:

```text
A -> B -> C -> D -> E
          ^         |
          +---------+
mu = 2, lambda = 3
```

| Iteration | Slow | Fast | Observation |
| --- | --- | --- | --- |
| 0 | A | A | Initialization only |
| 1 | B | C | Different identities |
| 2 | C | E | Slow enters the cycle |
| 3 | D | D | Meeting proves a revisit |

For A → B → C → D → null, the positions after movement are (B,C), then (C,null). The guard stops and returns false. A one-node self-loop meets after one iteration and returns true. Empty and null-terminated singleton inputs return false.

**Why a meeting implies a cycle:** after positive t iterations, the walkers have taken t and 2t links. If both reach the same identity after different path lengths, a segment of the deterministic successor walk repeats. In an acyclic chain a node has a unique position from head, so that equality cannot occur after positive movement.

**Why null proves absence:** if a successor walk reaches null, it cannot also contain a reachable cycle earlier. A cycle has no outgoing next edge to null along that same walk. The guard checks both fast and fast.next before taking two links.

**Why a cycle produces a meeting:** once both are inside, the difference in their traveled positions increases by one each iteration modulo λ. Thus the gap hits zero within at most λ more iterations. Together with entry travel, this is O(μ+λ) time. Acyclic inputs take O(n) time. Auxiliary space is O(1), with no new nodes.

### 9.3 Optional extension: recover the cycle entry

Once the references meet, move one reference back to head. Advance both one step until they meet again; that identity is the entry. The first meeting need not be the entry.

```java
static ListNode cycleEntry(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) {
            ListNode fromHead = head;
            while (fromHead != slow) {
                fromHead = fromHead.next;
                slow = slow.next;
            }
            return fromHead;
        }
    }
    return null;
}
```

At the first meeting, slow has moved t steps and fast 2t. Their extra distance t is a multiple of λ. Slow's cycle offset is (t−μ) modulo λ. After another μ steps, its offset is t modulo λ, which is zero. A new walker from head reaches the entry in those same μ steps. Before that point, the new walker is outside the cycle, so they cannot meet earlier. If μ=0, the first meeting is already at head and the second loop performs no steps.

In the trace above, the first meeting is D. Reset one walker to A: after one joint step they are B and E; after two they are C and C. C is the entry. Total time remains O(μ+λ), space O(1).

The proof uses the specific one-step/two-step Floyd setup. Arbitrarily changing speeds while reusing the same entry-recovery rule can invalidate the argument. Learn this extension after the basic meeting proof.

**Self-check Q15:** Why is comparing slow.val and fast.val incorrect?

**Self-check Q16:** Why must the ordinary Floyd implementation avoid treating initial equality as a detected cycle?

**Self-check Q17:** Does Floyd's first meeting always identify the entry?

## 10. Fixed-Gap References and Tail-Relative Removal

Fast/slow detection uses different speeds. Tail-relative positions use a **fixed gap**, maintained by moving both references at the same speed after initialization. Derive the initial gap and the end condition together.

### 10.1 Find the Nth node from the end

Contract: n is one-based, so n=1 means the tail. If n<=0 or n exceeds the chain length, throw IllegalArgumentException. The chain is acyclic. Invalid input leaves its links unchanged.

A two-pass baseline counts the length L, then walks to zero-based index L−n. That is already O(L) time and O(1) auxiliary space. A fixed gap avoids separately counting all nodes.

```java
static ListNode nthFromEnd(ListNode head, int n) {
    if (n <= 0) {
        throw new IllegalArgumentException("n must be positive");
    }
    ListNode ahead = head;
    for (int step = 0; step < n; step++) {
        if (ahead == null) {
            throw new IllegalArgumentException("n exceeds length");
        }
        ahead = ahead.next;
    }
    ListNode behind = head;
    while (ahead != null) {
        ahead = ahead.next;
        behind = behind.next;
    }
    return behind;
}
```

For A → B → C → D → E, n=2:

| Stage | Behind | Ahead | Distance in next links |
| --- | --- | --- | --- |
| After two lead steps | A | C | 2 |
| Joint step 1 | B | D | 2 |
| Joint step 2 | C | E | 2 |
| Joint step 3 | D | null | 2, treating null as the position after E |

Behind points to D, the second-from-last node. Joint movement preserves the gap because both advance one link. If null has conceptual index L, a gap n places behind at L−n. The explicit lead-loop check rejects n>L before any mutation, and n=L leaves behind at head.

### 10.2 Removal needs the target's predecessor

To unlink the Nth node from the end, arrange for behind to finish **before** it. A dummy supplies a predecessor even when the target is head.

```java
static ListNode removeNthFromEnd(ListNode head, int n) {
    if (n <= 0) {
        throw new IllegalArgumentException("n must be positive");
    }
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode ahead = dummy;
    for (int step = 0; step < n; step++) {
        if (ahead.next == null) {
            throw new IllegalArgumentException("n exceeds length");
        }
        ahead = ahead.next;
    }
    ListNode behind = dummy;
    while (ahead.next != null) {
        ahead = ahead.next;
        behind = behind.next;
    }
    deleteAfter(behind);
    return dummy.next;
}
```

Here ahead and behind begin at dummy, ahead is advanced n links, and joint movement stops when ahead is the **tail**, not null. At that point behind is the target's predecessor. An alternative valid convention advances n+1 from dummy and stops ahead at null; mixing half of each convention gives an off-by-one error.

For A → B → C → D → E, n=2:

| Stage | Behind | Ahead | Observation |
| --- | --- | --- | --- |
| After lead setup | Dummy | B | Gap is two links |
| Joint step 1 | A | C | Gap preserved |
| Joint step 2 | B | D | Gap preserved |
| Joint step 3 | C | E | Ahead is tail; target is C.next = D |
| Unlink | C | E | C.next becomes E; D.next becomes null |

Boundary cases:

| Input | n | Correct result |
| --- | --- | --- |
| A → B → C | 3 | B → C; A detached |
| A → B → C | 1 | A → B; C detached |
| A | 1 | null; A detached |
| A → B | 2 | B |
| A → B | 1 | A |
| null | 1 | Throw; no input links changed |
| A → B | 0 or −1 | Throw |
| A → B | 3 | Throw before any input link changes |

**Invariant:** after setup, ahead is n links after behind. The joint loop preserves the distance and does not change the chain. For length L, final ahead index is L−1 and final behind index is L−n−1, with dummy assigned conceptual index −1. Therefore behind.next is the node at index L−n. deleteAfter removes exactly that node, preserving all others.

Each method traverses O(L) links with O(1) auxiliary space; removal allocates one dummy. They do not need O(n) auxiliary storage for the gap. For too-large n, they throw after reaching the end rather than blindly performing n dereferences. Time on valid calls is O(L), not O(L²).

**Self-check Q18:** Why does removal stop ahead at the tail while nthFromEnd stops it at null?

**Self-check Q19:** How is removing the head handled without a separate head-removal branch?

## 11. Merging Sorted Chains

### 11.1 Clarify the result before choosing an algorithm

Inputs a and b are individually sorted in nondecreasing order, finite, acyclic, and node-disjoint. The result must contain each input node exactly once, sorted by value. Equal values are retained. We choose a stable tie rule: take the node from a when the current values tie; relative order within each source is preserved.

A straightforward value-copy method can repeatedly choose the smaller current value and allocate a new result node. It takes O(n+m) time and n+m new output nodes while leaving the source links intact. Concatenating into an array and sorting generally adds O((n+m) log(n+m)) comparison work and ignores the already sorted inputs.

If destructive consumption is permitted, reuse the original nodes and retain only a result tail.

### 11.2 Derive the result-tail invariant

The smallest remaining value must be at one of the two source heads. Append the smaller head to the result; save its successor by advancing that source reference. A dummy gives the empty result an initial tail.

```java
static ListNode mergeSorted(ListNode a, ListNode b) {
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;
    while (a != null && b != null) {
        if (a.val <= b.val) {
            ListNode chosen = a;
            a = a.next;
            tail.next = chosen;
        } else {
            ListNode chosen = b;
            b = b.next;
            tail.next = chosen;
        }
        tail = tail.next;
    }
    tail.next = a != null ? a : b;
    return dummy.next;
}
```

The result's **committed prefix ends at tail**. During the loop, tail.next may still refer to a remaining source node through its old link. Do not incorrectly claim that every intermediate result is null-terminated. The next iteration replaces that edge when needed. Both remaining source heads are explicitly retained.

Example: a is A(2) → B(5) → C(5); b is X(1) → Y(5) → Z(8).

| Step | Current source heads before choice | Chosen identity | Committed result through tail | Remaining a / b |
| --- | --- | --- | --- | --- |
| 1 | A(2), X(1) | X | X(1) | A→B→C / Y→Z |
| 2 | A(2), Y(5) | A | X→A | B→C / Y→Z |
| 3 | B(5), Y(5) | B | X→A→B | C / Y→Z |
| 4 | C(5), Y(5) | C | X→A→B→C | null / Y→Z |
| Attach remainder | null, Y(5) | Y→Z | X→A→B→C→Y→Z | Both represented |

The committed prefix is sorted and consists of exactly the consumed prefixes of the original sources, with no identity repeated. Every committed value is no greater than any remaining source value. Choosing the smaller head preserves those properties; advancing that source preserves access to its suffix. Each iteration consumes one node, so the loop terminates when at least one source is empty. Its other suffix is already sorted and can be attached with one assignment.

Worst-case time is O(n+m), O(1) auxiliary space including the dummy, and no new data nodes. Attaching a remainder is O(1); it does not traverse that suffix. If one input is empty, the other is returned through dummy.next; both empty returns null. MIN_VALUE and MAX_VALUE are safe because the code compares directly rather than subtracting values.

### 11.3 Mutation, sharing, and stability

“Reusing nodes” does not preserve the input lists as independent chains. For example, X.next originally refers to Y but later refers to A. A caller retaining an old input head can observe its newly merged suffix.

Node-disjointness is essential. Suppose a and b both refer to the same singleton A. Picking it from a leaves b still at A; attaching b through tail=A would set A.next=A. A shared-suffix input can produce the same problem later. Detect sharing first or use a contract/algorithm designed for it; a sentinel alone cannot fix aliasing.

If the inputs must remain unchanged or be independently mutable afterward, allocate new nodes. Copying is a different ownership promise, not merely slower syntax.

**Self-check Q20:** Why is it safe to append the remaining suffix without comparing each of its nodes again?

**Self-check Q21:** Why can merging a list with itself create a cycle?

## 12. Splitting and Reordering

Reordering illustrates composition. For a sequence of node identities L0,L1,...,L(n−1), the required order here is L0,L(n−1),L1,L(n−2),... . Values are not changed; duplicates remain distinct nodes.

A reference-array baseline records all nodes and reconnects them by alternating indices. It is O(n) time with O(n) auxiliary storage. To obtain O(1) auxiliary space, give each phase a small, explicit contract.

### 12.1 Four phases

1. Find the first middle.
2. Split after it, creating two **disjoint** null-terminated chains.
3. Reverse the second chain.
4. Weave one node from the reversed right chain after each left node.

For seven nodes:

```text
Original: A -> B -> C -> D -> E -> F -> G -> null

First middle D; split D.next = null:
Left:     A -> B -> C -> D -> null
Right:    E -> F -> G -> null

Reverse right:
Left:     A -> B -> C -> D -> null
Right:    G -> F -> E -> null

Weave:
A -> G -> B -> F -> C -> E -> D -> null
```

The first-middle convention makes left length ceil(n/2) and right length floor(n/2). Thus each right node has a left partner, and one unpaired left node may remain at the end.

### 12.2 Implementation and saved continuations

```java
static ListNode weaveOutsideIn(ListNode head) {
    if (head == null || head.next == null) {
        return head;
    }
    ListNode middle = firstMiddle(head);
    ListNode right = middle.next;
    middle.next = null;
    right = reverseIterative(right);
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

Both continuations are saved before either link is replaced. The loop guard is right != null because the right chain is never longer than the left chain. On an even-length input, left can become null only on the final weave step when right also becomes null.

Trace after splitting and reversing A → B → C → D → E → F:

| Step | Left before | Right before | Saved nextLeft / nextRight | Links written | Committed prefix / remaining |
| --- | --- | --- | --- | --- | --- |
| 1 | A | F | B / E | A.next=F; F.next=B | A→F / left B→C, right E→D |
| 2 | B | E | C / D | B.next=E; E.next=C | A→F→B→E / left C, right D |
| 3 | C | D | null / null | C.next=D; D.next=null | A→F→B→E→C→D / none |

**Invariant:** the committed prefix has the required alternating identities. The remaining left and right suffixes are disjoint and reachable through their local references; left has at least as many nodes as right. Each step consumes one from each side and preserves the next suffixes. No node is repeated because the split established disjointness. When right is exhausted, any remaining left node already has the correct final null-terminated position.

The split is not cosmetic. On A → B → C → D, forgetting to break B.next can leave the old left chain reaching into the right half. Weaving may then reconnect a node that remains reachable through the other half and create a self-link or cycle.

### 12.3 Boundaries and total cost

| Original chain | Result identity order |
| --- | --- |
| null | null |
| A | A |
| A → B | A → B |
| A → B → C | A → C → B |
| A → B → C → D | A → D → B → C |
| A → B → C → D → E | A → E → B → D → C |

Midpoint O(n), reversal O(n), and weave O(n) are **sequential** phases, so their costs add to O(n). The implementation uses O(1) auxiliary space and no new data nodes. Using recursive reversal inside this composition would change auxiliary space to O(n).

An alias to the old tail now refers to an interior node. A wrapper that caches tail must update its metadata; this bare-head method promises the rearranged chain, not a repaired external container.

**Self-check Q22:** What structural fact does splitting establish that reversal and weaving need?

**Self-check Q23:** Why is the complete three-scan composition still O(n)?

## 13. Copying a Chain with Random References

### 13.1 Define the graph being copied

Each node contains a value, an ordinary next reference, and an additional random reference. Here the next chain is finite and acyclic; random may be null or refer to **any node in that same chain**, including itself or an earlier node. Repeated values are allowed.

```java
static final class RandomNode {
    int val;
    RandomNode next;
    RandomNode random;

    RandomNode(int val) {
        this.val = val;
    }
}
```

A shallow copy of the head reference shares every object. Copying only the values and next chain creates new objects but fails to reproduce the random relationships. Giving a copy the original node's random reference also shares original structure and therefore fails the deep-copy contract.

**Deep-copy contract:** allocate exactly one new node for each original next-chain node; preserve values and the next/random relationships under a one-to-one mapping; no returned next or random edge may refer to an original node. Leave the original structure unchanged.

### 13.2 Derive the old-to-new mapping

A straightforward alternative finds each random target's next-chain index by rescanning, then locates its copy. That can cost O(n²) while using only a few working references beyond the output chain. A map avoids repeatedly rediscovering the correspondence.

Use two passes. First create all nodes, storing original identity → copy identity. Then every target is available, even a forward random reference.

```java
static RandomNode copyRandomChain(RandomNode head) {
    Map<RandomNode, RandomNode> copies = new HashMap<>();
    for (RandomNode current = head; current != null; current = current.next) {
        copies.put(current, new RandomNode(current.val));
    }
    for (RandomNode current = head; current != null; current = current.next) {
        RandomNode copy = copies.get(current);
        copy.next = copies.get(current.next);
        copy.random = copies.get(current.random);
    }
    return copies.get(head);
}
```

Our final RandomNode class has inherited identity equality, so HashMap keys distinguish different same-valued nodes. No mapping for null is inserted; lookup of an absent null key returns null, correctly reproducing null edges and an empty head. This use is intentional.

To explicitly demand reference equality when integrating with a different node class, use `new IdentityHashMap<>()` for the map. It compares keys with == rather than a potentially overridden equals. It is specialized for identity-sensitive work, not a general replacement for value-key maps. See the [Java 21 IdentityHashMap documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/IdentityHashMap.html).

### 13.3 Trace identity relationships

Original next chain A(7) → B(7) → C(2), with random edges A→C, B→A, C→C:

```text
Original next: A(7)  -> B(7)  -> C(2)  -> null
Copy next:    A'(7) -> B'(7) -> C'(2) -> null

Original random: A->C,  B->A,  C->C
Copy random:     A'->C', B'->A', C'->C'
```

| Pass | Original processed | Mapping or edges established |
| --- | --- | --- |
| Allocate | A | A → A' |
| Allocate | B | B → B', distinct from A' despite equal value |
| Allocate | C | C → C' |
| Wire | A | A'.next=B'; A'.random=C' |
| Wire | B | B'.next=C'; B'.random=A' |
| Wire | C | C'.next=null; C'.random=C' |

**Pass-one invariant:** each visited original has exactly one fresh copy with equal value, and no original link changes. After pass one, every allowed nonnull target has a mapping.

**Pass-two invariant:** each visited original has its copy's outgoing edges translated through that mapping. Future wiring changes only copy objects. At termination, all original next/random relationships are represented without an original object in the result.

A map keyed by value would collapse A and B, losing the one-to-one mapping. If random references may point **outside** the next chain or next itself may cycle, this two-pass contract is insufficient: model a graph, traverse all required reachable identities with a visited mapping, and clarify the intended copy boundary. Do not silently treat unknown targets as null.

Expected time is O(n) under suitable hash behavior, auxiliary map space O(n), and newly allocated returned-node storage O(n). There are no recursive frames here. Reporting “O(1) space because the map becomes garbage afterward” is incorrect; peak live working storage matters. A null head returns null; a singleton with random null or random to itself copies correctly.

### 13.4 Optional extension: interleaving

When temporary mutation and exclusive ownership are allowed, insert each copy after its original:

```text
A -> A' -> B -> B' -> C -> C' -> null
```

An original's copy is now available through original.next; a nonnull random target's copy is available through original.random.next. After wiring copy random edges, separate the alternating chain into original and copied chains, restoring **every original next link**, including the original tail's null.

This can achieve O(n) time and O(1) auxiliary space excluding the n returned nodes. It is more delicate: failure to restore links leaves the input changed; observers must not see the temporary interleaving; an exception partway through does not automatically restore the structure. It does not suit a read-only-input requirement. The map-based implementation is the core learning approach here; deriving the interleaving implementation is optional.

**Self-check Q24:** Why is value → copy an invalid mapping when duplicate values exist?

**Self-check Q25:** What must a deep-copy test check beyond the displayed values?

## 14. LRU Cache with a Map and Doubly Linked List

### 14.1 Derive the structure from required operations

Least Recently Used evicts the retained entry whose latest successful read or write occurred furthest in the past. This teaching cache stores int sensor keys and int readings. A successful read refreshes recency; a miss does not. Both inserting and updating through write make that key most recent.

Clarify the workload: lookup a key, update its value, move it to the most-recent position, and evict the least-recent key when capacity is exceeded.

- A plain HashMap finds entries quickly but has no recency sequence. Adding timestamps still needs a way to find the minimum timestamp efficiently; scanning all retained entries costs O(c).
- A doubly linked list maintains recency and unlinks a **known node** in O(1), but searching for a key costs O(c).
- Combine key → node lookup with direct node movement.

A map combined with an array-based recency order still pays O(c) for shifts or arbitrary removal. A singly linked recency list lacks a known node's predecessor unless extra bookkeeping supplies it. The doubly linked list exposes both neighbors directly.

### 14.2 Orientation and invariants

Choose front as the most-recent side and back as the least-recent side:

```text
front sentinel <-> newest <-> ... <-> oldest <-> back sentinel
                    ^                  ^
                  MRU                LRU

byKey: key -> exact real node already present in this chain
```

At public-operation boundaries:

1. front.next.prev is front, back.prev.next is back, and adjacent nodes agree in both directions.
2. front.prev and back.next are null. Empty means front.next==back and back.prev==front.
3. Each map entry points to exactly one real list node with that key; every real node has exactly one map entry.
4. Sentinels are never stored in the map or returned as data.
5. The real-node order is the order of latest successful accesses, newest first.
6. The number of retained entries is at most capacity.

Local helper operations can temporarily break a link relationship while completing their assignments. The invariant is promised after the helper/public operation completes in this single-threaded teaching model, not after each individual write.

### 14.3 Local unlink and attach operations

For a known real node X with neighbors P and Q:

```text
Before detach: P <-> X <-> Q
After detach:  P <------> Q     X.prev=null, X.next=null

Attach after front F:
Before: F <-> R
After:  F <-> X <-> R
```

Detaching writes P.next=Q and Q.prev=P. Attaching saves R, initializes X's links to F and R, then makes both neighbors refer to X. Persistent sentinels ensure P and Q exist even for the oldest/newest/only real node.

### 14.4 Explicit implementation

Contract: capacity is any nonnegative int; a negative capacity throws. Capacity zero retains nothing. Keys and readings may be any int. `read` returns an Integer, with null for a miss, so stored reading −1 is unambiguous. No method accepts null values. This class is a sequential algorithm example.

```java
static final class RecentReadings {
    private static final class Entry {
        final int key;
        int reading;
        Entry prev;
        Entry next;

        Entry(int key, int reading) {
            this.key = key;
            this.reading = reading;
        }
    }

    private final int capacity;
    private final Map<Integer, Entry> byKey = new HashMap<>();
    private final Entry front = new Entry(0, 0);
    private final Entry back = new Entry(0, 0);

    RecentReadings(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be nonnegative");
        }
        this.capacity = capacity;
        front.next = back;
        back.prev = front;
    }

    Integer read(int key) {
        Entry entry = byKey.get(key);
        if (entry == null) {
            return null;
        }
        moveToFront(entry);
        return entry.reading;
    }

    void write(int key, int reading) {
        Entry entry = byKey.get(key);
        if (entry != null) {
            entry.reading = reading;
            moveToFront(entry);
            return;
        }
        if (capacity == 0) {
            return;
        }
        Entry added = new Entry(key, reading);
        byKey.put(key, added);
        attachAfterFront(added);
        if (byKey.size() > capacity) {
            Entry victim = back.prev;
            detach(victim);
            byKey.remove(victim.key);
        }
    }

    private void moveToFront(Entry entry) {
        detach(entry);
        attachAfterFront(entry);
    }

    private void detach(Entry entry) {
        Entry before = entry.prev;
        Entry after = entry.next;
        before.next = after;
        after.prev = before;
        entry.prev = null;
        entry.next = null;
    }

    private void attachAfterFront(Entry entry) {
        Entry oldFirst = front.next;
        entry.prev = front;
        entry.next = oldFirst;
        front.next = entry;
        oldFirst.prev = entry;
    }
}
```

A hit uses the node handle from the map; it does not search the recency list. Updating an existing key preserves its node identity rather than creating a duplicate. A new key creates one node, puts it in both structures, then removes the oldest if necessary. The just-added node cannot be confused with the back sentinel because capacity>0 on this path and at least one real node exists.

Reading the current MRU detaches and reattaches the same node; its final position is unchanged. For a one-entry cache, both neighbors are sentinels, so the same helper code works. The sentinel key zero is harmless: real key zero is represented by a different Entry stored in the map.

### 14.5 Complete operation trace

Capacity c=2. List order is **MRU → LRU**. E4 denotes the real Entry object for key 4, not its reading. Map entries below are shown sorted by key only for legibility; HashMap does not provide that display order.

| Operation | Return | Map State after operation | List Order after operation | Evicted Key |
| --- | --- | --- | --- | --- |
| write(4,40) | — | {4→E4(40)} | 4 | None |
| write(7,70) | — | {4→E4(40), 7→E7(70)} | 7,4 | None |
| read(4) | 40 | {4→E4(40), 7→E7(70)} | 4,7 | None |
| write(9,90) | — | {4→E4(40), 9→E9(90)} | 9,4 | 7 |
| write(4,44) | — | {4→E4(44), 9→E9(90)} | 4,9 | None |
| read(7) | null | {4→E4(44), 9→E9(90)} | 4,9 | None |
| write(2,20) | — | {2→E2(20), 4→E4(44)} | 2,4 | 9 |
| read(4) | 44 | {2→E2(20), 4→E4(44)} | 4,2 | None |

Additional boundaries:

| Scenario | Expected behavior |
| --- | --- |
| c=0; write(1,5); read(1) | Miss; no real node retained |
| c=1; write(1,5); write(1,6); read(1) | Return 6; no eviction caused by updating the same key |
| c=1; write(1,5); write(2,6); read(1) | Miss; key 1 was evicted |
| c=2; write(0,−1); read(0) | Return −1, distinct from a null miss |
| Negative capacity | Throw at construction |

### 14.6 Correctness and costs

**Base state:** the sentinels link directly to one another, the map is empty, and the capacity bound holds.

**Read miss:** neither structure changes. **Read hit:** lookup identifies the unique retained node; detach preserves all other relative order; attach makes that node newest. The map still points to the same node and all values remain correct.

**Existing-key write:** update only that node's reading and refresh recency; size does not increase. **New-key write:** create a unique key/node pair at the newest position. If size exceeds capacity, back.prev is the least-recent real node by the ordering invariant. Removing it from both structures restores the capacity bound and the bijection.

Link helpers perform O(1) work. Hash operations are expected O(1) under suitable dispersion, and insertion costs are amortized across table growth. Thus read is expected O(1), and write is expected amortized O(1), **not a strict worst-case guarantee for every call**. A map resize or heavy collisions can cost more. Over q operations, expected total work is O(q), with O(c+1) retained state including sentinels and at most one transient extra entry during insertion. An individual public call uses O(1) extra local working state beyond the cache it maintains.

If eviction removes only the list node, a future map lookup can return a detached stale node. If it removes only the map entry, an unreachable-by-key real node remains in the list and may later be selected again. The data structures form one representation invariant.

### 14.7 Java comparison: access-order LinkedHashMap

Java 21 supports `new LinkedHashMap<Integer, Integer>(16, 0.75f, true)`. In access-order mode, successful get and put operations update an entry's position; normal encounter order runs **least recent to most recent**, opposite the displayed orientation of our explicit cache. The [official API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html) specifies which methods generate an access. In particular, containsKey alone is not a recency-refreshing get.

`removeEldestEntry` can impose a size rule after insertion of a new entry. See the [existing local access-order example](./Java_Map_HashMap_TreeMap_LinkedHashMap.md#access-order-and-eldest-entry-hook). Access-order traversal must account for reads that change order.

This library is useful when permitted, but the explicit implementation explains the mechanism requested by the roadmap. Production concerns such as expiration, persistence, and concurrent cache architecture are outside this lesson.

**Self-check Q26:** Why does a map of keys to values alone fail to provide the required recency operations?

**Self-check Q27:** Which two structures must change on eviction, and what fails if only one changes?

**Self-check Q28:** Why should updating an existing key avoid allocating another linked node?

## 15. Deriving Patterns and Proving Invariants

The roadmap names three patterns. Their common principle is to represent the exact unfinished work and preserve access to it. A recognizable problem title is only a clue; the contract decides which mechanism is valid.

### 15.1 Pattern A: Rewire plus sentinel

**Definition:** edit a chain by changing a small number of next links, using a sentinel when the head needs an ordinary predecessor.

**Recognition clues:** remove a selected node, reverse a chain or segment, splice two chains, merge sorted nodes, or compose a reordering. The result often reuses original objects.

**Start simply:** collect values or node handles in an array, or allocate an independent output chain. This clarifies the desired order before optimizing storage.

**Optimization:** save each continuation in a local reference and relink existing nodes. A dummy removes head-only branches for deletion or result construction. Reversal itself needs a reversed-prefix boundary rather than a dummy; the family name does not mean every member needs every ingredient.

**Invariant:** processed nodes satisfy the output rule, unprocessed nodes remain reachable, and identities are neither duplicated nor silently discarded except for explicitly removed nodes. For merge, formulate the committed prefix through tail rather than claiming an intermediate null-terminated result.

Sections 6, 7, 11, and 12 give implementations and detailed traces. For reversal, previous and current partition the original nodes; for deletion, predecessor.next is the first unchecked candidate; for merge, the smaller remaining head extends the sorted committed prefix.

**Costs:** usually O(n) or O(n+m) traversal with O(1) auxiliary state for iterative rewiring. Count searches and whole compositions separately from their constant-size local writes.

**Common failures:** overwrite next before retaining it; return dummy; omit a head update; merge aliased sources; forget to split. **Unsuitable when:** original links must remain unchanged, input ownership is shared, indexed access dominates, or the required identity/output contract demands a new copy. Choose copying or a different representation when needed.

### 15.2 Pattern B: Fast/slow or fixed-gap references

**Definition:** coordinate two walkers so their relative positions reveal a midpoint, a revisit, or a tail-relative location.

**Recognition clues:** “middle,” “cycle,” “one pass,” “Nth from the end,” and “no length field.” One pass is a useful goal, not permission to guess a gap.

**Start simply:** count then traverse for midpoint or Nth-from-end; use a visited set for cycles. These baselines are already correct under their stated contracts.

**Optimization:** encode information in the walkers' movement relationship. One-versus-two speeds reveal halfway progress and cycle meetings; equal-speed movement preserves a prepared gap.

**Invariants:** for midpoint, distances traveled are t and 2t; for Floyd, relative cycle offset increases by one modulo λ; for tail-relative removal, ahead remains n links after behind. Each uses its own guard and stopping interpretation.

Sections 8–10 supply code and traces. For removal, dummy supplies a predecessor and the tail stopping condition identifies the correct outgoing link.

**Costs:** O(n) time/O(1) auxiliary state, or O(μ+λ) for cyclic reachability. **Common failures:** compare values, check initial equality as a cycle, use unsafe fast.next.next, or combine a gap from one convention with termination from another. **Unsuitable when:** a walk does not follow one deterministic successor, random-access queries dominate, or mutation/traversal contracts differ. A branching graph generally needs graph traversal, not this list proof.

### 15.3 Pattern C: Map plus movable nodes

**Definition:** use a lookup table to locate node identities/correspondences while linked structure records relationships or an order.

**Recognition clues:** repeated key lookup plus arbitrary recency movement; a deep copy with cross-references; a requirement to preserve sharing between copies of nodes.

**Start simply:** scan for a key or random target on every operation. That can cost O(c) per cache access or O(n²) for copying.

**Optimization:** store the missing correspondence. LRU stores key → existing movable node; copying stores original identity → newly allocated copy. Both use a map, but their **stored meanings and invariants differ**.

Sections 13–14 give complete teaching implementations and traces. The copy invariant is a one-to-one correspondence preserving every edge. LRU maintains a bijection between map entries and real doubly linked nodes, plus recency order and a capacity bound.

**Costs:** expected O(n) time, O(n) map space, and O(n) returned nodes for map-based copying; expected amortized O(1) cache operations with O(c+1) maintained storage. **Common failures:** value-keyed copies, stale map entries, duplicate cache nodes, one-direction-only unlinking, or reversed MRU/LRU interpretation. **Unsuitable when:** an identity map is replaced with incompatible value equality, hashing bounds are unacceptable, input references escape the copy boundary, or a simple library/array representation better matches the workload.

### 15.4 A repeatable derivation process

1. Clarify the result: values, identities, order, mutation permission, and missing/invalid cases.
2. Draw a normal chain and a smallest boundary chain.
3. Write a correct baseline and identify what it repeatedly searches or stores.
4. Name the local references and what each means at loop start.
5. Before each field write, state which old edge is being replaced and where its target remains reachable.
6. Prove initialization, preservation, progress, and termination.
7. Count node visits, link updates, allocations, map work, and recursive frames.
8. Test identities and structure before trusting printed values.
9. Change one assumption and ask whether the proof still applies.

For example, change “node-disjoint lists” to “lists may share a suffix.” The ordinary in-place merge proof loses its disjointness claim. Change “may mutate” to “read-only,” and the rewire-based optimization no longer meets the contract even if it produces the right sequence once.

**Self-check Q29:** What extra fact must a proof establish beyond “every iteration moves a reference”?

## 16. Deriving Time and Space Costs

### 16.1 Define the size before the bound

Let n be the number of nodes in an ordinary chain, m the size of a second independent chain, c the cache capacity, r the currently retained cache entries, and q the number of cache operations. For a cycle, μ and λ describe the noncycle prefix and cycle; there are μ+λ distinct reachable nodes.

A local rewrite such as `predecessor.next = removed.next` is O(1). Finding predecessor may require O(n). Distinguish the cost of the edit from the cost of locating its operands.

| Operation / implementation | Time | Auxiliary working space | New result / maintained storage |
| --- | --- | --- | --- |
| Traverse or count | O(n) | O(1) | Scalar/reference output |
| Search by value | O(n) worst case | O(1) | Existing node reference |
| Prepend | O(1) | O(1) | One added data node |
| Append by scan | O(n) | O(1) | One added data node |
| Append with maintained tail | O(1) | O(1) | One added data node |
| Insert after known node | O(1) | O(1) | One added data node |
| Delete head / after known predecessor | O(1) | O(1) | No new nodes |
| Delete first by value | O(n) worst case | O(1), including optional dummy | No new data nodes |
| Delete singly linked tail without predecessor | O(n) generally | O(1) | No new nodes |
| Unlink known doubly linked real node | O(1) | O(1) | No new nodes |
| Iterative reversal | O(n) | O(1) | Reused input nodes |
| Recursive reversal | O(n) | O(n) stack | Reused input nodes |
| Middle or fixed-gap traversal | O(n) | O(1) | Existing node / one removal |
| Merge two sorted disjoint chains | O(n+m) worst case | O(1) | Reused n+m nodes |
| Split, reverse, weave | O(n) | O(1) with iterative reversal | Reused input nodes |
| HashSet cycle detection | Expected O(n) | O(n) set | Boolean |
| Floyd detection / entry | O(n) acyclic; O(μ+λ) cyclic | O(1) | Boolean / existing node |
| Map-based random copy | Expected O(n) | O(n) map | O(n) fresh returned nodes |
| Optional interleaving copy | O(n) | O(1), excluding output | O(n) fresh returned nodes |
| Cache read | Expected O(1) | O(1) locals per call | O(r+1) cache state |
| Cache write | Expected amortized O(1) | O(1) locals per call | O(r+1), r<=c at operation boundaries |

Empty/small inputs take constant work. Bounds such as O(n) use the usual n>=1 convention; use O(n+1) when explicitly modeling an empty input as well. Similarly, allocated cache state is O(c+1), so capacity zero still includes the owner, map object, and sentinels.

### 16.2 Count work across the entire execution

For reversal, each original node becomes current once. For merge, each comparison consumes a node from one source; at most n+m−1 comparisons occur when both inputs are nonempty, followed by one suffix attachment. For weaving, each right-half node is processed once.

Two references do not imply O(n²): fast can follow two links per iteration while slow follows one, and total iterations remain linear. Several independent linear phases add. Repeatedly restarting traversal is different: n appends by scanning, or reading every `LinkedList.get(i)` in an indexed loop, accumulates quadratic traversal.

Recursive reversal satisfies T(n)=T(n−1)+O(1). There are O(n) calls, and a maximum O(n) are active before unwinding. Total calls determine time; maximum simultaneous frames determine stack space. Java does not promise tail-call elimination, and this reversal also performs essential work after its recursive call.

### 16.3 Input, auxiliary storage, and output

An existing n-node input uses O(n) space whether you reverse it or do nothing. A reversal with four local references still uses O(1) **additional** working storage. Referencing an existing suffix does not duplicate its nodes.

A single dummy is O(1) auxiliary storage. A new dummy per **recursive level**, however, could accumulate O(n) simultaneously; “dummy” is not itself a complexity guarantee.

Copying n nodes needs O(n) output storage. The map-based copy separately needs O(n) auxiliary map storage. If an interviewer counts all extra allocated memory together, its total additional allocation remains O(n); stating both components makes the convention unambiguous. Interleaving saves the map, not the output allocation.

LRU owns its retained nodes and lookup table. Its **data structure storage** grows with capacity; constant local variables per get/put do not make the whole cache O(1) space. Resizing may temporarily retain old/new table storage, still O(c+1) asymptotically.

### 16.4 Hashing and practical costs

Integer keys and identity-based node keys have constant-size equality/hash operations in this model, but good dispersion remains a condition for expected hash bounds. Map insertion may trigger a resize. Do not turn expected/amortized language into a worst-case promise.

Traversing nodes to stringify the entire list takes O(n) plus output construction; doing it inside each step of a linear algorithm can make the diagnostic run O(n²). Drawing/tracing helps learning, but account for it if it becomes part of measured program behavior.

Use the [complexity derivation process](./Complexity/Complexity_Analysis_and_Foundations.md#19-interview-complexity-analysis-process) to explain costs from execution, not from the number of loops or the name of a technique.

**Self-check Q30:** How can a reversal be O(1) auxiliary space when its result contains n nodes?

## 17. Java Pitfalls and Failing Examples

### 17.1 Failures worth tracing by identity

The fragments in the next table describe deliberate mistakes; they are not additional implementations to use.

| Mistake | Small failing case | What fails | Correct reasoning |
| --- | --- | --- | --- |
| Overwrite next before saving it | A→B→C; set A.next=null, then follow A.next | Cursor loses B→C | Preserve old successor first |
| Use current as its own previous | A→B; initialize both to A | A.next=A | Reversed prefix starts empty |
| Forget recursive tail termination | A→B; set B.next=A but keep A.next=B | Two-node cycle | Break old forward link before returning |
| Ignore returned new head | Reverse A→B but retain caller head=A | Caller reaches only old head/tail | Store the returned reference |
| Return dummy | Delete the only node A | Result incorrectly contains D | Return dummy.next |
| Assume a null fast receiver is safe | Empty input; evaluate fast.next | NullPointerException | Check fast before fast.next |
| Check initial Floyd equality | A→null; slow==fast before moving | False positive | Compare after positive movement |
| Compare values for cycle detection | A(7)→B(7)→null | False positive if equal values mean “seen” | Compare identities |
| Mix fixed-gap conventions | A→B→C, n=1; move lead to null with a predecessor-style gap | Wrong target/predecessor | Derive gap, origin, and stopping condition together |
| Forget tail metadata | TailChain A→B, delete B externally | Cached tail still B; next append can be lost from head | Maintain all owner fields together |
| Merge overlapping inputs | a=A, b=A | A.next can become A | Require disjoint input nodes or use another algorithm |
| Skip split before weaving | A→B→C→D | Both “halves” can still reach common nodes | Terminate left half before reversing right |
| Update only one doubly linked direction | P↔X↔Q; only P.next=Q | Q.prev still points to X | Update both neighbor directions |
| Evict only from the list | Capacity 1; write 1, then 2 | Map still returns evicted 1 | Remove map entry and node together |
| Allocate on existing-key update | Capacity 2; write 1 twice | Duplicate list nodes for one key | Change existing node and move it |
| Map random-copy nodes by value | A(7)→B(7), different random edges | Two originals collapse to one key | Use original identity as key |
| Put original random target in copy | A.random=B; A'.random=B | “Copy” still shares original object | Translate edge through old-to-new mapping |
| Use indexed library traversal | For each i, call linked.get(i) | Total traversal becomes O(n²) | Use an iterator/enhanced for for sequential access |
| Treat removed as immediately destroyed | Keep alias B, then unlink B | Alias still reaches a live object | Unlinking and garbage collection are different |
| Claim recursion uses only a few references | Reverse a long chain recursively | Ignores active frames | Include O(n) stack space |

### 17.2 Mutation and identity tests

For an in-place algorithm, save the original identity sequence before invoking it. Afterward:

1. Follow next with an identity-based visited set or a strict bound.
2. Fail if a node repeats unexpectedly; do not let the test hang.
3. Compare the observed identity sequence with the required permutation or surviving subsequence.
4. Confirm the tail's next is null and removed nodes are detached when promised.
5. Confirm values on surviving nodes were not changed.
6. For methods with a read-only contract, confirm the original outgoing links are unchanged.

A displayed `[7,7]` is insufficient: two equal-valued nodes could have been swapped, duplicated, copied, or replaced. For a stable merge, test source-relative identity order on ties. For reverse, applying it twice should restore identity order, but that property alone does not prove that a single application produced the correct reverse.

For random-copy tests, maintain a correspondence between original and copy nodes, verify values, translate next/random targets, and check the two identity sets are disjoint. Mutate a copy value and confirm no original changes. Also compare every original link with a saved snapshot.

For LRU tests, check outputs against a simple reference model **and** check internal structure: forward/backward consistency, one node per key, no sentinels in the map, correct MRU/LRU order, and retained size within capacity. A cache can return correct readings for a short trace while already containing a broken backward edge.

### 17.3 Edge-case matrix

| Family | Cases to propose |
| --- | --- |
| Chain basics | Empty, singleton, two nodes, all equal values |
| Insertion/deletion | Head, middle, tail, absent value, repeated target, only node |
| Reversal | Odd/even length, repeated values, old head becomes tail |
| Midpoint | Lengths 0 through 5 under both conventions |
| Cycle detection | Empty, finite singleton, self-loop, entry at head, entry after a prefix, duplicate values without a cycle |
| Fixed gap | n=1, n=length, n<=0, n>length |
| Merge | Both empty, one empty, alternating sources, one exhausted early, all ties, extreme int values |
| Reordering | Lengths 0 through 6 and an odd chain with a leftover left node |
| Random copy | Empty, null randoms, forward/backward targets, self-reference, shared random target, duplicate values |
| LRU | Capacity 0/1, hit/miss, updating a key, repeated MRU access, eviction, key 0, reading −1 |

### 17.4 Additional Java choices

A `final ListNode head` variable cannot be rebound, but its referenced object's mutable fields can still change. Final does not recursively freeze a chain.

Do not add recursive equals/hashCode/toString over next/random casually. Cycles can cause nontermination or stack overflow, and mutable link-based hash codes can invalidate map/set membership. Identity labels in diagrams are diagnostic names; hash codes are not unique addresses.

Use direct comparison or Integer.compare for sorting decisions involving int values. Subtracting two extreme values can overflow. If an added task sums values, widen before arithmetic and check the maximum supported size/value product.

Node arrays and `new ArrayList<>(nodeReferences)` copy references, not the node graph. Library clone/copy operations must be checked for their actual depth. Replacing a custom-node problem with a collection of values can erase identity and sharing information the problem requires.

**Self-check Q31:** Can final on a head variable prevent changing head.next?

## 18. Comparing Structures and Approaches

### 18.1 Singly versus doubly linked structure

| Property | Singly linked | Doubly linked |
| --- | --- | --- |
| Links per node | next | prev and next |
| Forward traversal | O(n) | O(n) |
| Backward traversal | Not directly supported | Supported |
| Delete known node | Need its predecessor in general | O(1) local unlink if neighbors/endpoints are known |
| Remove tail with maintained tail | O(n) to find predecessor | O(1) with tail |
| Link consistency | One direction | Both directions must agree |
| Typical use here | Reversal, merge, cycle/middle/gap tasks | LRU recency movement |

“Known node” does not imply “known predecessor” in a singly linked chain. A doubly linked representation spends additional per-node storage to make that predecessor directly available.

### 18.2 Floyd versus a visited set

| Property | Floyd | Identity-preserving visited set |
| --- | --- | --- |
| Time | O(n) or O(μ+λ) | Expected O(n) for distinct reachable nodes |
| Auxiliary space | O(1) | O(n) |
| Core proof | Relative speed around a deterministic cycle | A repeated identity is a revisit |
| Find cycle entry | Additional reset-and-walk phase | First repeated identity |
| Useful diagnostic history | Only a few references | Can retain visited nodes/order with more storage |
| Main pitfall | Initialization, guard, meeting interpretation | Accidentally using value equality |

A visited set is often the clearest baseline. Floyd removes that storage when the structure has exactly one successor per node and only a few structural facts are required.

### 18.3 In-place versus copying

| Requirement | In-place rewiring | Allocate a new chain |
| --- | --- | --- |
| Preserve original links | Generally no | Yes, with a correct copy algorithm |
| Preserve input node identities in result | Yes | No; new identities are created |
| Additional returned nodes | None for reversal/merge/reorder | O(n), or O(n+m) for merging two lists |
| Working space | Often O(1), iterative | Can be O(1) for simple copying; maps add O(n) |
| Shared aliases | Observe mutations | Original aliases remain attached to originals |
| Independent result mutation | Result and input identities coincide | Possible when the copy is deep enough |

“Space efficient” is not a sufficient reason to violate a read-only contract. Conversely, allocating fresh nodes is incorrect when a task requires rearranging the original identities.

### 18.4 HashMap, linked recency order, and their combination

| Design | Lookup key | Move known entry to newest | Find/evict oldest | Limitation |
| --- | --- | --- | --- | --- |
| Plain HashMap of values | Expected O(1) | No maintained order | Requires additional state/work | No recency sequence by itself |
| Doubly linked list only | O(c) search | O(1) after locating node | O(1) at known boundary | Lookup dominates |
| Map plus doubly linked nodes | Expected O(1) | O(1) links | O(1) links plus expected map removal | Must maintain joint invariant |
| Access-order LinkedHashMap | Expected basic hash costs | Handled by access-order methods | Eldest policy / ordered entry operations | May hide required interview mechanics |

### 18.5 When another representation is preferable

Use an array or ArrayList for frequent indexed reads, sorting, binary search, or compact scan-oriented data. Use an ordinary iterator when editing a library list rather than trying to access its hidden nodes. For a basic FIFO queue or stack, the [roadmap's Java toolkit](./DSA_Interview_Roadmap.md#java-21-interview-toolkit) generally uses ArrayDeque; a custom linked list is justified when node-level control is the point.

A required random-reference graph is more than a list of values. A general graph with multiple outgoing edges needs a graph representation and traversal. A requirement for worst-case logarithmic ordered key queries suggests tree structures rather than a plain hash table plus list.

**Self-check Q32:** Which workload would make an array-based list a better default despite linked lists' cheap local edits?

## 19. Pattern Recognition and Decision Practice

Before reading the suggestions, choose an approach for each scenario and explain its precondition, invariant, mutation permission, and costs. These are conceptual decisions, not a new problem tracker.

| ID | Scenario | Explain your choice |
| --- | --- | --- |
| R1 | Find the first node containing a requested value in a finite chain | What information justifies stopping? |
| R2 | Remove the first matching node, which might be head | Which incoming link must change? |
| R3 | Report the later middle of a finite chain without a size field | Which even-length convention and guard? |
| R4 | An instrument can accidentally point next to an earlier node; decide whether this happened with constant extra storage | Which notion of equality matters? |
| R5 | Diagnose a malformed chain and retain which identities were visited | Why might extra memory be worthwhile? |
| R6 | Remove the fourth-from-last node when length is unknown | Where do the references begin and stop? |
| R7 | Merge two sorted chains while preserving every source node's identity | What must be true about shared nodes? |
| R8 | Alternate the earliest and latest original nodes using constant auxiliary storage | How will you make two safe halves? |
| R9 | Clone a read-only next/random structure with duplicate values | What must the mapping keys represent? |
| R10 | Maintain recent sensor readings with expected constant-time lookup and eviction | What does each representation contribute? |
| R11 | Store a million readings and repeatedly request positions chosen by index | Is a linked representation appropriate? |
| R12 | Two list heads might converge on the same node; determine whether they share structure | Why are value comparisons insufficient, and why does merging them first change the question? |

<details>
<summary>Recognition suggestions</summary>

- **R1:** Traverse with a local reference. The visited prefix contains no match; the first match is therefore the requested identity. O(n) worst case/O(1) auxiliary.
- **R2:** A dummy supplies a predecessor for every candidate, including head. Keep predecessor.next as the first unchecked node; bypass only the first match.
- **R3:** Use the secondMiddle convention. After t rounds the walkers have moved t and 2t links; the fast guard permits the even-chain pass to null.
- **R4:** Floyd detects repeated identity with O(1) auxiliary state. Equal readings on different nodes are irrelevant.
- **R5:** An identity-preserving visited set/map is a useful diagnostic baseline. Expected O(n) time/O(n) space can buy retained visit history rather than just a boolean.
- **R6:** With a dummy, advance ahead four links, then move both until ahead is the tail. Behind is the predecessor; validate that four nodes exist first.
- **R7:** Sorted merge with a result tail and sentinel; require disjoint source identities and permission to consume their links. Stable ties can prefer the first source.
- **R8:** Find the first middle, split, reverse the second chain iteratively, and weave with both continuations saved. Left must not be shorter.
- **R9:** Use original identity → new identity mapping, then translate all edges. The read-only contract rules out temporary interleaving unless permission changes.
- **R10:** Map key → doubly linked node, with sentinels and a bijection invariant. The map avoids search; links avoid shifting or rescanning recency.
- **R11:** An array or ArrayList gives O(1) indexed access. Repeated linked indexed access can dominate even if append is cheap.
- **R12:** Treat this as identity-based shared-structure detection, with acyclicity stated first. A visited-identity set is a clear baseline; length alignment is a possible constant-space extension. Do not mutate either chain just to detect sharing. This extension is not a new required platform problem.

</details>

### A compact decision sheet

| Need | Candidate mechanism | First condition to check |
| --- | --- | --- |
| Read a value or count nodes | Traversal | Is next guaranteed to terminate? |
| Delete a possibly first node | Predecessor plus dummy | What is the deletion/absence contract? |
| Reverse order of existing nodes | previous/current/savedNext | Is mutation permitted? |
| Middle | Fast/slow | First or second middle? |
| Reachable cycle | Floyd or visited identities | Compare identities; avoid an unbounded printer |
| Tail-relative node | Fixed gap | One-based n, origin, stopping position |
| Sorted combination | Result-tail merge | Sorted, disjoint, consumable inputs |
| Outside-in order | Split, reverse, weave | Separate halves before composing |
| Deep copy of cross-links | Old-to-new map | Identity semantics and copy boundary |
| Key lookup plus recency | Map plus doubly linked nodes | Map/list bijection and MRU/LRU orientation |
| Many arbitrary indexed reads | Array-based representation | Count traversal costs before choosing links |

Changing the statement can change the best answer. A read-only reorder might return values in a new array instead of rewiring. A diagnostic might prefer a set over Floyd. A tail-relative query repeated many times on unchanged data might justify a prebuilt index. Always optimize for the stated operations and constraints.

## 20. Conceptual Assessment and Answer Key

Try the Q1–Q32 questions at their original sections with the relevant code hidden. Explain each answer with a two-to-five-node drawing. The key checks the reasoning; it is not a substitute for implementing the roadmap drills.

<details>
<summary>Q1–Q10: structure, references, and sentinels</summary>

| Question | Concise answer |
| --- | --- |
| Q1 | A singly linked tail does not expose its predecessor; finding that incoming link generally requires a head traversal. |
| Q2 | Two equal-valued positions are two nodes unless the diagram explicitly revisits one identity. Equal values alone say nothing about aliasing. |
| Q3 | None. The assignment copies a reference, so alias and first reach the same object. |
| Q4 | The parameter is a separate variable initialized with the caller's reference value. Rebinding it does not rebind the caller's slot. |
| Q5 | Only the local variable's reference changes. A.next remains B and the chain from head is unchanged. |
| Q6 | A.next==B, B.prev==A, B.next==C, and C.prev==B, with outer links/endpoints preserved. |
| Q7 | Save head.next as the prospective new head before setting the removed head's next to null. |
| Q8 | A known node supplies the edit location directly. A requested index must first be reached by following links. |
| Q9 | Exactly one extra node and a fixed number of references are used, independent of input length. |
| Q10 | The dummy is excluded from the data contract and identified by role/identity; zero-valued data nodes remain ordinary nodes. |

</details>

<details>
<summary>Q11–Q23: reversal, movement, and composition</summary>

| Question | Concise answer |
| --- | --- |
| Q11 | The old successor is replaced by the reversed edge. Without a saved reference the untouched suffix may no longer be reachable by the algorithm. |
| Q12 | Each active recursive call retains its frame until the suffix returns; depth is linear in the chain length. |
| Q13 | A.next remains B while B.next becomes A, creating a two-node cycle. |
| Q14 | firstMiddle returns index 0; secondMiddle returns index 1. Their fast guards stop before versus after the final possible two-link movement. |
| Q15 | Distinct nodes may hold equal values; a cycle requires revisiting the same identity. |
| Q16 | Both references start at head on acyclic inputs too. Equality becomes evidence only after positive movement. |
| Q17 | No. The first meeting can be inside the cycle away from entry; entry recovery needs the additional phase/proof. |
| Q18 | nthFromEnd wants the target itself at distance n behind null. Removal wants its predecessor at distance n behind the tail, starting from dummy. |
| Q19 | Dummy remains the predecessor when n equals length; changing dummy.next bypasses the original head. |
| Q20 | The remaining source is sorted, and every committed value is no larger than its first value. Its internal order needs no further choices. |
| Q21 | The two source references can identify one object; later attachment can set that object's next to itself. Disjointness was part of the proof. |
| Q22 | The halves become disjoint null-terminated chains. Neither remaining cursor silently aliases nodes in the other half. |
| Q23 | Each phase visits at most a linear number of nodes; sequential O(n)+O(n)+O(n) work is O(n). |

</details>

<details>
<summary>Q24–Q32: copying, cache design, proofs, and costs</summary>

| Question | Concise answer |
| --- | --- |
| Q24 | Different same-valued originals need different copies and possibly different edges; a value key collapses them. |
| Q25 | One fresh copy per original, equal values, translated next/random relationships, no edge into originals, and original links unchanged. |
| Q26 | HashMap does not encode the least-to-most-recent order. Finding the oldest from timestamps alone can require a scan. |
| Q27 | Remove the real list node and its key mapping. Otherwise stale lookup state or an unmatched retained node violates the bijection. |
| Q28 | The old node already represents that key. A second node creates duplicate recency positions and breaks one-key/one-node consistency. |
| Q29 | Movement proves progress toward termination. Correctness also needs preservation of the required order, identities, reachability, and result contract. |
| Q30 | Existing result nodes are input storage being reused. A fixed number of additional references is O(1) auxiliary storage. |
| Q31 | No. Final prevents rebinding that variable, not mutation of the referenced object's fields. |
| Q32 | Frequent indexed access, sorting, binary search, or dense scan-oriented storage generally favors arrays/ArrayList. |

</details>

For an oral interview, add these follow-ups: What changes if inputs may share nodes? What if mutation is prohibited? What if n is invalid? What exactly is returned for an empty input? Which complexity claim changes if recursive reversal replaces iterative reversal? A memorized loop that cannot answer these is not yet reliable knowledge.

## 21. Key Takeaways and Interview Explanation

A node variable stores a reference; a node object stores fields. Rebinding the variable and mutating a field have different effects. Identity survives changes to values and links, so structural algorithms must reason about the same objects, not merely the same displayed numbers.

Cheap local rewiring depends on already having the right node handles. Finding those handles can still cost a traversal. Dummy nodes unify predecessors; saved continuations preserve unfinished work; fast/slow or fixed-gap relationships encode position information; maps store the correspondence that linked structure alone cannot find efficiently.

A useful interview explanation follows this sequence:

1. “The input is a finite acyclic chain; the result reuses its nodes, and mutation is permitted.”
2. “A simple baseline records node handles. I can replace that storage with these local references.”
3. “At loop start these references represent the processed portion and the unprocessed suffix.”
4. “Before overwriting this link, I save its old target so all required nodes remain reachable.”
5. “Each step consumes one unprocessed node, preserving order and identity. Here is why termination implies the requested result.”
6. “The traversal is linear; this iterative version uses constant auxiliary state and allocates no data nodes.”
7. “I will test empty, singleton, two-node, duplicate-valued, and relevant head/tail cases by identity.”

Adapt the explanation for cycles, two-source merging, copying, or a cache. A cycle input changes the termination proof; a copy changes allocation and identity goals; LRU requires a joint representation invariant rather than a simple processed prefix.

### Readiness evidence

Use the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) together with the category's [specific evidence requirement](./DSA_Interview_Roadmap.md#6-linked-list). From a blank editor, complete the custom-node construction, reversal, dummy deletion, sorted merge, and deliberate-cycle drills. Explain the fast/slow meeting argument, fixed-gap boundary choice, and map-plus-node LRU design.

Then solve an unfamiliar deletion/reversal/composition variant with tags hidden. Draw the links, preserve all required identities, justify null/head/tail handling, test for accidental cycles, and derive the costs. Repeat across separate sessions. Familiar curated answers support practice evidence; transfer to unfamiliar requirements supports readiness.

The [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) remains the source for I/H/S attempt results. Keep problem history and due dates on the existing roadmap rows. A flexible [review schedule](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition) is about 1, 3, 7, and 14 days, adjusted to actual retention. This document creates no second progress ledger and records no earned gates.

## 22. References, Validation, and Next Steps

### Repository references

- [Roadmap: Linked List objectives, drills, practice, and readiness](./DSA_Interview_Roadmap.md#6-linked-list).
- [Roadmap: progress gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) and [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy).
- [Java List, ArrayList, and LinkedList](./Java_List_ArrayList_LinkedList.md), especially [references and copying](./Java_List_ArrayList_LinkedList.md#7-references-copying-equality-and-null).
- [Java Map reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Set reference](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Java Arrays](./Java_Array.md) and [Arrays & Hashing concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).
- [Two Pointers concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md).
- [Testing Essential Concepts](./Testing_Essential_Concepts.md).

These paths were discovered in this repository. The Two Pointers, Arrays & Hashing, and Complexity guides are in subdirectories; the new guide links to their actual locations.

### Primary references for language and library details

- [Java Language Specification 21: objects and references](https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.1).
- [Java Language Specification 21: method invocation and parameter variables](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.5).
- [Java 21 Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html): inherited equality and hashing contracts.
- [Java 21 LinkedList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedList.html): doubly linked structure and indexed traversal.
- [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html): basic operation assumptions and table behavior.
- [Java 21 IdentityHashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/IdentityHashMap.html): explicit identity-based key comparison.
- [Java 21 LinkedHashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html): access order and eldest-entry policy.
- [Official NeetCode roadmap](https://neetcode.io/roadmap): external organizational reference; this repository's roadmap remains authoritative for local E/A/C priorities.

Java collection behavior was checked against version-specific official documentation through Context7. The linked NeetCode page uses dynamic content and was not used to invent or replace the local category objectives. Platform constraints and supplied node classes should be read at practice time; the eight links above identify the roadmap's selected problems.

### Validation scope

All **26 Java blocks** were extracted directly from this document, assembled under the stated teaching-class convention, and compiled using Java 21 with `--release 21 -Xlint:all -Werror`. A temporary identity-aware runner passed **9,775,870 runtime assertions**:

| Area | Validation performed |
| --- | --- |
| Basic operations, reversal, midpoint, fixed gap, and reordering | 3,281 input arrays: exhaustive lengths 0–7 over {−1,0,1}, plus an extreme-int case; checked returned identities, order, preserved values, invalid arguments, and promised detachment |
| Sorted merge | 7,057 pairs, including every pair of nondecreasing arrays of lengths 0–6 over {−1,0,1}, plus an extreme-int pair; checked exact stable identity order |
| Cycles | 1,313 structures: finite lengths 0–256 and all μ=0–32, λ=1–32 combinations; compared both detectors and entry recovery, and verified unchanged links |
| Random-reference copying | 11,553 cases: exhaustive small value/random-target assignments through length 4, plus 1,000 seeded cases up to length 50; checked disjoint copies, translated edges, and original-link preservation |
| LRU cache | 78,432 short operation sequences across capacities 0–3, plus the documented trace and longer seeded runs; 429,100 operations checked against an independent array-based recency model and both link directions |
| Large iterative case | A 100,000-node chain checked for counting, both midpoint conventions, a full-length gap, reversal identity order, and restoration by a second reversal |

Independent simulations also verified **89 rows across 19 dry-run and boundary tables**. Structural checks confirmed 22 numbered sections, 32 self-checks with answers, 12 recognition scenarios with explanations, and all eight curated priorities/difficulties matching the local roadmap. All **56 local links and anchors** resolved.

Content hashes confirmed that all **23 pre-existing files** retained their original contents at their discovered current paths. This task added only this guide; temporary compilation and validation files remained outside the repository.

The tests exercise the stated contracts and representative large iterative behavior; they do not make a long recursive chain safe for every JVM stack configuration. Most structural cases deliberately include duplicate values so identity errors cannot hide behind distinct numbers. Shared-node merges and cyclic inputs to ordinary finite-chain routines remain excluded by their explicit preconditions. The optional interleaving-copy discussion is a derivation, not an executable implementation validated here. No learner progress or T/I/P/R gate was automatically earned.

### Next steps

Read Sections 2–7 first and draw reference changes by hand. Close the document and rebuild the basic mechanisms. Continue with midpoint, cycle, and fixed-gap reasoning, then combine the mechanisms in merging and reordering. Study copying and LRU after identity and local link edits feel reliable.

Attempt the four Essential problems in the roadmap's order and record assistance honestly. Use Additional problems to develop composition, copying, and cache mechanics; the core concepts still need explanation even if a particular platform attempt is deferred. Keep Merge k Sorted Lists as an optional challenge until the Heap / Priority Queue prerequisite is available.

If testing reveals a lost node, start with the last overwritten link and find the reference that should have preserved its target. If testing reveals a cycle, identify the first edge that reconnects to an already represented identity. Repair that reasoning before memorizing a different line of code.

