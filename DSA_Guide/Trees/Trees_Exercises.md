# Trees — Exercises (Java 21)

Build a tree, state what one call promises, and test whether that promise survives a changed shape. This workbook develops independent implementation and interview reasoning; document ownership does not establish mastery.

## Contents

- [1. Introduction and conventions](#1-introduction-and-conventions)
- [2. Problem-solving workflow](#2-problem-solving-workflow)
- [3. Level 1: Tree fundamentals](#3-level-1-tree-fundamentals)
- [4. Level 2: DFS implementation and tracing](#4-level-2-dfs-implementation-and-tracing)
- [5. Level 2: BFS and level information](#5-level-2-bfs-and-level-information)
- [6. Level 3: Subtree reports and structural changes](#6-level-3-subtree-reports-and-structural-changes)
- [7. Level 3: Ordered queries and inherited bounds](#7-level-3-ordered-queries-and-inherited-bounds)
- [8. Level 3: Paths and reconstruction](#8-level-3-paths-and-reconstruction)
- [9. Level 4: Curated platform practice](#9-level-4-curated-platform-practice)
- [10. Level 5: Unfamiliar interview challenges](#10-level-5-unfamiliar-interview-challenges)
- [11. Complexity investigations](#11-complexity-investigations)
- [12. Debugging laboratory](#12-debugging-laboratory)
- [13. Interview communication](#13-interview-communication)
- [14. Cold review and spaced repetition](#14-cold-review-and-spaced-repetition)
- [15. Completion and readiness evidence](#15-completion-and-readiness-evidence)
- [16. References and author validation](#16-references-and-author-validation)

## 1. Introduction and conventions

This is the practical companion to [Trees Essential Concepts](./Trees_Essential_Concepts.md). The [Phase 3 Trees roadmap](./DSA_Interview_Roadmap.md#7-trees) determines scope and E/A/C priorities. There are **five levels**, **18 core coding drills**, **6 unfamiliar coding challenges**, **6 tracing/reasoning exercises**, **7 complexity investigations**, and **10 debugging challenges**: **47 original exercises**, plus **8 curated platform problems**. Several drills contain related methods under one ID; count the ID once.

Start with a blank editor and the statement. Hide hints and the theory's implementations during an independent attempt. Medium is a local exercise difficulty, separate from roadmap priority and platform difficulty. Preparation of earlier documents is not evidence of prerequisite mastery.

| If you cannot yet… | Revisit |
| --- | --- |
| Define a child call's promise and null base | [Recursive contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) |
| Distinguish calls from live frames | [Recursive space](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space), [complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) |
| Explain saved work and LIFO | [Explicit versus recursive stacks](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack) |
| Predict aliases and link mutation | [Node identity](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity) |
| Defend interval bounds and ordered elimination | [Binary Search invariants](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) |
| Separate output lists and interpret map lookups | [Java Lists](./Java_List_ArrayList_LinkedList.md#7-references-copying-equality-and-null), [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) |

### Shared contract profiles

These profiles are part of **every exercise specification**. Local overrides take precedence.

**G — ordinary tree:** finite rooted binary tree, no cycles and no shared child identities, **0..200 nodes** in ordinary exercises. A nonroot object has exactly one incoming child edge. Null root means empty. Values may be any int, including duplicates, negatives, MIN, and MAX. No BST or balance guarantee exists. Existing nodes are read-only; no new TreeNode objects are permitted unless a construction or mutation contract says otherwise. Working records/containers are allowed within the space target. Queries returning nodes return existing identities.

**S — strict BST:** G with unique keys and strict ordering of entire subtrees. No balance guarantee. S methods may rely on validity without first checking it. Validation exercises accept G and determine whether S holds. Empty is a valid strict BST. Equal values in different nodes are legal under G and invalidate S.

**L — large iterative domain:** G with **0..100000 nodes**, only where explicitly assigned. Full-depth chain tests use iterative code. Recursive exercises use at most 200 nodes in ordinary tests; no universal JVM safe-depth guarantee is assumed. Invalid graph-like inputs are outside these contracts and are not ordinary tests.

All array/list/map arguments are nonnull unless explicitly allowed. Preconditions are assumed; rejection is required only when specified. No generic cycle checker is required. Every constructed output must also be an ordinary tree.

| Quantity | Convention |
| --- | --- |
| n | Number of distinct reachable nonnull nodes |
| h | Height in **nodes**; empty 0, leaf 1 |
| Node depth | **Edges from the supplied root**; root 0 |
| Path length / diameter | **Edges**, unless a sum or node count is explicitly requested |
| w | Maximum actual nonnull nodes on one level; empty 0 |
| MIN / MAX | Integer.MIN_VALUE / Integer.MAX_VALUE |
| Auxiliary space | Excludes existing input and required output; includes frames, frontiers, maps, and intermediate allocations |
| Empty-case cost | Add constant setup: O(n+1), O(h+1), O(w+1); nonempty shorthand omits +1 |

Counts/lengths fit int. Sum in long: even 100000 int values have magnitude below 2.15×10^14. Widen before potentially overflowing arithmetic. Structural answers do not depend on value signs.

Return fresh mutable lists on every request, including empty results. Nested level lists are distinct containers. Node equality means identity when an identity is requested. No request-specific state may survive in static fields. Mutating exercises grant exclusive link ownership; the caller stores the returned root. Unless stated, output ordering is irrelevant for scalar/record results and explicitly defined for lists/trees.

Use only Java 21 standard-library types. Java blocks are members of a scratch `TreesWorkbook` class with `import java.util.*;` above it. Records are nested members. TODO methods intentionally throw UnsupportedOperationException. Analysis subjects and deliberately flawed programs are supplied for investigation; no corrected coding solutions are included.

F1 asks you to write the node class yourself first. The compile scaffold for later exercises is:

```java
static final class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) {
        this.val = val;
    }
}
```

### Fixture language: structure first

Letters identify **objects**, not values. Each test uses fresh instances unless a repeated-call test says otherwise. `E` means null; `S(v)` means a singleton. `L(v1,...,vk)` is a left-child-only chain; `R(v1,...,vk)` is a right-child-only chain, starting at v1. An empty chain means E. `Fork(v,l,r)` has root v and leaf children l/r.

**Tree K**, for implementation tests:

```text
                 A(8)
                /    \
             B(3)    C(12)
                \    /   \
                D(6) E(10) F(14)
```

Exact links: A.left=B; A.right=C; B.right=D; C.left=E; C.right=F; all others null. K is a strict BST and height-balanced, but not complete, full, or perfect. n=6, h=3, w=3; leaves D/E/F; five edges. Depths: A=0, B/C=1, D/E/F=2. K differs from the theory's Tree A.

**Tree U**, for cold traces:

```text
                 P(4)
                /    \
             Q(-2)   R(4)
             /         \
           S(9)        T(0)
```

P/R are distinct despite equal values. U is balanced but not a strict BST; n=5, h=3, w=2. All omitted links are null.

**Tree V**, for ancestor ordering:

```text
                 A(8)
                /    \
             B(3)    C(12)
                     /   \
                   D(6)  E(14)
```

Every immediate comparison passes. D belongs to A's right subtree; investigate the inherited constraint.

**Tree W**, for queue width:

```text
                   A(0)
                 /      \
              B(0)      C(0)
             /   \      /   \
           D(0) E(0)  F(0) G(0)
          / \   / \
        H(0) I(0) J(0) K(0)
```

n=11, h=4; level widths 1,2,4,4. Repeated values do not merge identities.

Compact **BFS serialization** `[v0,v1,null,...]` consumes a root, then two child tokens for each **nonnull parent** in FIFO order. Null consumes a child slot but creates no node and is never enqueued. Trailing absent child tokens mean null; trailing null tokens may be omitted. `[]` is empty. This is **not** heap-index encoding: null placeholders have no descendant slots. `[1,null,2,3]` means 1.right=2 and 2.left=3. All supplied serializations use this convention.

Structural outputs omit trailing nulls. You need not implement a parser: build fixtures manually. Also verify identities/child links when required; equal traversal values alone do not establish structural correctness.

### Roadmap objectives mapped to evidence

| Objective | Exercises |
| --- | --- |
| Types, references, height/depth | F1–F3, T1 |
| Recursive DFS | V1–V3, T2 |
| Iterative DFS and saved state | V4–V6, T3, C6 |
| BFS layers and width | B1/B2, T4, X1, C2 |
| Size, leaves, height, balance, diameter | F2, A1, X2, C1/C3 |
| Mutation and ownership | A2, D10; LC2 supplies inversion practice |
| Ordered lookup, extremes, rank | O1, V2/V5, LC6 |
| Ancestor bounds | O2, D6/D7, X3 |
| Root-to-leaf and extendable/internal paths | P1/P2, T6, A1; LC8 optional |
| Reconstruction and disjoint ranges | R1, T5, X4, C5, D8 |
| Unfamiliar selection and simpler approaches | X1–X6, mock follow-ups |
| Empty/skewed costs and repeated scans | C1–C7, debugging |

T/I/P/R are the [roadmap's independent topic gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates). I/H/S in an attempt log are **results**: I = independent correct solution, explanation, and tests; H = conceptual hint; S = substantial solution review or unfinished; — = not attempted. A checked task means an attempt, tests, and postmortem were completed; it does not automatically mean independent success.

Original exercises use their single local record. For curated problems the roadmap's existing Log remains canonical; the workbook includes a linked record form. Fill the roadmap Log with the form's fields and leave the local form as a pointer, or deliberately choose the local record and leave a pointer in the roadmap when studying. Do not maintain two histories. This workbook creates no dashboard and awards no progress.

## 2. Problem-solving workflow

Before coding an important recursive exercise, write six lines:

1. **Promise:** what this call returns/changes for exactly its argument subtree.
2. **Base:** what null returns, and why.
3. **Left:** the left child's promised result, units, and ownership.
4. **Right:** the right child's promised result.
5. **Combine:** how the current node uses them; distinguish returned state from an internal best.
6. **Proof:** base correctness, valid smaller children, correct combination, and strict progress.

For traversal helpers, describe preservation of an existing output prefix. For F3/O2/P1/X3, state inherited context separately. For A2, specify postconditions on **links and identities**. Memorized code without a defensible contract does not meet completion.

For iteration, explain each saved entry, what is processed, what remains, and why every iteration progresses. For all six DFS drills explain visit order, null behavior, recording time, coverage, termination, time, and stack space. For BFS additionally explain FIFO, level-size snapshots, left-before-right discovery, null rejection, and maximum frontier.

Use clarify → draw → correct baseline → improve → implement → test → analyze. Compare at least two approaches on unfamiliar exercises. Test empty/minimal, asymmetric, duplicate/negative/extreme, and repeated-call cases where valid. Record expected/actual values and the property tested. Snapshot every input node's val/left/right for read-only methods. Check fresh identities for constructors and exact retained links for mutators.

ArrayDeque uses `offerLast/pollFirst` for FIFO and `push/pop` for LIFO. It rejects null entries; endpoint operations have amortized constant cost. [Java 21 ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html)

**Shared completion standard:** pass all assigned tests, add a risk-focused test, honor ownership, explain correctness and costs, and fill the attempt record. Tracing/analysis/debugging exercises submit the requested table/proof/repair rather than treating compilation as completion.

## 3. Level 1: Tree fundamentals

Repair gaps with [terminology](./Trees_Essential_Concepts.md#2-tree-fundamentals-and-terminology), [shape types](./Trees_Essential_Concepts.md#3-binary-tree-types-and-properties), and [references](./Trees_Essential_Concepts.md#4-java-treenode-references-and-structure).

### F1. Build and inspect a linked hierarchy

**Difficulty:** Easy. **Learning objective:** Create objects and child links deliberately; distinguish shape from values.

**Prerequisites:** Fields, constructors, references, root/leaf terminology.

**Problem and contract:** Write TreeNode from memory, then construct exactly Tree K without a serialization helper. Draw it from your completed links. Identify root, leaves, edge depths, subtree heights, and its BST/balanced/complete/full/perfect properties.

**Input / output:** No parameters; return the newly allocated root. Each call must succeed and return six fresh nodes.

**Constraints and ownership:** Fixed nonempty six-node output, unique values, strict BST, balanced. Allocate exactly six TreeNodes per call, share none across calls, and leave every unspecified child null. Shape and left/right order matter; labels A–F name positions, not stored fields.

```java
static TreeNode buildK() {
    throw new UnsupportedOperationException("TODO F1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| One call | [8,3,12,null,6,10,14]; n=6, h=3, w=3; leaves 6,10,14 |
| Two calls | Same shape/values; disjoint node identity sets |
| Inspect B and C | B: depth 1, height 2; C: depth 1, height 2 |
| Inspect all child slots | Five nonnull edges, seven null child slots; no aliasing |

**Target costs:** O(1) time and auxiliary space for this fixed fixture; six required output nodes. Generalizing construction to n explicit nodes takes O(n) time/output.

**Completion criteria:** Verify every edge and null slot, not only preorder. Explain why K is balanced but not complete. Explain how to represent an empty tree separately; it is not this constructor’s output.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### F2. A basic subtree inventory

**Difficulty:** Easy. **Learning objective:** Separate size, leaf count, and node-height.

**Prerequisites:** F1; null bases and recursive contracts.

**Problem and contract:** Return the size, number of leaves, and node-height of the supplied subtree in one report. A leaf has both children null. Define the six-line contract before implementing. This is the small inventory used later as a prerequisite, not a balance or diameter solution.

**Input / output:** TreeNode root → Inventory(size, leaves, height). Empty → (0,0,0).

**Constraints and ownership:** G. Read-only, no new TreeNodes; fixed-size result records allowed. Counts concern identities, not unique values. Output order is record-field order.

```java
record Inventory(int size, int leaves, int height) {}

static Inventory inventory(TreeNode root) {
    throw new UnsupportedOperationException("TODO F2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | (0,0,0) |
| S(-5) | (1,1,1) |
| L(4,4) | (2,1,2) |
| K | (6,3,3) |
| U | (5,2,3) |
| [1,2,3,4] | (4,2,3) |
| R(MIN,MAX,MIN) | (3,1,3) |
| L(1,...,200) | (200,1,200) |

**Target costs:** O(n) time, O(h) auxiliary frames/live fixed-size reports, O(1) required output. Do not rescan each subtree separately.

**Completion criteria:** Explain why height chooses a route while size includes both sides; count leaves correctly when only one child exists. State total calls separately from maximum depth.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### F3. Locate a particular object

**Difficulty:** Medium. **Learning objective:** Propagate depth while matching node identity.

**Prerequisites:** F1/F2; reference equality; recursive or explicit DFS state.

**Problem and contract:** Return the edge depth of the exact target object relative to root. An equal-valued object is not a match. Return -1 when target is null or is not reachable. Choose a traversal and explain its inherited depth context.

**Input / output:** TreeNode root, TreeNode target → int depth or -1.

**Constraints and ownership:** G. Target may be null or an external, unreachable node. Read-only; no new nodes. No BST/balance assumption. Existing identity, not value, selects the answer; no output sequence.

```java
static int objectDepth(TreeNode root, TreeNode target) {
    throw new UnsupportedOperationException("TODO F3");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| K, target A | 0 |
| K, target D | 2 |
| K, new TreeNode(6) | -1 |
| U, target R | 1 |
| E, null | -1 |
| S(7), that root | 0 |
| R(7,7), second node | 1 |
| L(1,...,200), last node | 199 |

**Target costs:** O(n) worst-case time and O(h) auxiliary using DFS; output O(1). A BFS solution is correct with O(w) auxiliary but must report that different bound.

**Completion criteria:** Explain absence, duplicate values, and why local reassignment cannot mark the caller’s nodes. Trace a miss and a match; preserve every link.

<details>
<summary>Hint 1</summary>

Value equality cannot identify one object in a duplicate-valued tree.

</details>

<details>
<summary>Hint 2</summary>

Attach a root-relative depth to the subtree or pending node being examined.

</details>

<details>
<summary>Hint 3</summary>

A successful child result must survive combination; use a distinct absence value rather than confusing it with depth zero.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T1. Reference slots and structural ownership

**Difficulty:** Easy. **Objective:** distinguish reassignment from mutation. **Prerequisites:** F1 and Java reference values. **Task:** predict every variable target and child link after each statement, without running it first. **Input:** a fresh K and an alias to its D node. **Output:** a written identity/link table and final serialization. **Contract:** the supplied subject intentionally mutates B.right; the original fixture is ordinary, unique-valued, and balanced. No cycle or shared child is introduced. The discarded allocation is not an output node. Length units are irrelevant.

```java
static TreeNode tReference(TreeNode root) {
    TreeNode saved = root.left.right;
    TreeNode local = root.left;
    local = new TreeNode(99);
    root.left.right = null;
    return saved;
}
```

| Statement just executed | local | saved | B.right | Objects reachable from root |
| --- | --- | --- | --- | --- |
| saved assignment | Not declared | ? | ? | ? |
| local assignment | ? | ? | ? | ? |
| local rebinding | ? | ? | ? | ? |
| field assignment | ? | ? | ? | ? |

Example checkpoint: final root serialization is `[8,3,12,null,null,10,14]`; returned identity is original D(6), still visible through the external alias. Explain why D is detached from the root but not necessarily collectible. **Edge checks:** B is not lost; D has no children; caller root stays A. Null or another shape is outside this subject’s precondition. **Target:** O(1) time/auxiliary, one temporary allocation, existing-reference output. **Completion:** fill all cells, draw before/after, justify each assignment, then verify with Java. Method signature is the supplied subject; do not replace it with a “fixed” method.
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 4. Level 2: DFS implementation and tracing

See [recursive DFS](./Trees_Essential_Concepts.md#6-preorder-inorder-and-postorder-dfs) and [iterative DFS](./Trees_Essential_Concepts.md#7-iterative-dfs) after an attempt. V1–V6 are six separate implementations, not wrappers that call one another. Do not implement a requested traversal by sorting values. Every result retains duplicate occurrences and leaves input unchanged.

### V1. Recursive preorder

**Difficulty:** Easy. **Learning objective:** Derive root, left subtree, right subtree visit timing and account for saved work.

**Prerequisites:** F1/F2; recursive contracts.

**Problem and contract:** Return values in root, left subtree, right subtree order. Use recursive child calls and one output container per public request; define an append-helper contract that preserves its existing prefix. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** G. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> recursivePreorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [8,3,6,12,10,14] |
| U | [4,-2,9,4,0] |
| L(1,2,3) | [1,2,3] |
| R(1,2,3) | [1,2,3] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [0,MIN,MAX] |

**Target costs:** O(n) aggregate time; O(h) auxiliary recursive frames; O(n) returned values. 

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Trace null entries/returns and explain why two child calls do not imply exponential work.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### V2. Recursive inorder

**Difficulty:** Easy. **Learning objective:** Derive left subtree, root, right subtree visit timing and account for saved work.

**Prerequisites:** F1/F2; recursive contracts.

**Problem and contract:** Return values in left subtree, root, right subtree order. Use recursive child calls and one output container per public request; define an append-helper contract that preserves its existing prefix. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** G. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> recursiveInorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [3,6,8,10,12,14] |
| U | [9,-2,4,4,0] |
| L(1,2,3) | [3,2,1] |
| R(1,2,3) | [1,2,3] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [MIN,0,MAX] |

**Target costs:** O(n) aggregate time; O(h) auxiliary recursive frames; O(n) returned values. 

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Trace null entries/returns and explain why two child calls do not imply exponential work.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### V3. Recursive postorder

**Difficulty:** Easy. **Learning objective:** Derive left subtree, right subtree, root visit timing and account for saved work.

**Prerequisites:** F1/F2; recursive contracts.

**Problem and contract:** Return values in left subtree, right subtree, root order. Use recursive child calls and one output container per public request; define an append-helper contract that preserves its existing prefix. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** G. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> recursivePostorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V3");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [6,3,10,14,12,8] |
| U | [9,-2,0,4,4] |
| L(1,2,3) | [3,2,1] |
| R(1,2,3) | [3,2,1] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [MIN,MAX,0] |

**Target costs:** O(n) aggregate time; O(h) auxiliary recursive frames; O(n) returned values. 

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Trace null entries/returns and explain why two child calls do not imply exponential work.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### V4. Iterative preorder

**Difficulty:** Medium. **Learning objective:** Derive root, left subtree, right subtree visit timing and account for saved work.

**Prerequisites:** V1; ArrayDeque LIFO.

**Problem and contract:** Return values in root, left subtree, right subtree order. Use ArrayDeque and no recursive calls. Derive what each pending stack entry means. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** L; ordinary hand tests remain small. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> iterativePreorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V4");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [8,3,6,12,10,14] |
| U | [4,-2,9,4,0] |
| L(1,2,3) | [1,2,3] |
| R(1,2,3) | [1,2,3] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [0,MIN,MAX] |

**Target costs:** O(n) aggregate time; O(h) auxiliary explicit state; O(n) returned values. 

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Also test iterative construction of a 100000-node left and right chain: result length 100000; correct first/last values; no recursive helper hidden in setup or checks.

<details>
<summary>Hint 1</summary>

The next pending child is chosen by LIFO.

</details>

<details>
<summary>Hint 2</summary>

Decide which sibling must wait while the other subtree finishes.

</details>

<details>
<summary>Hint 3</summary>

Only push nonnull children; work backward from the required next pop to the push order.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### V5. Iterative inorder

**Difficulty:** Medium. **Learning objective:** Derive left subtree, root, right subtree visit timing and account for saved work.

**Prerequisites:** V2/V4; pending ancestor state.

**Problem and contract:** Return values in left subtree, root, right subtree order. Use ArrayDeque and no recursive calls. Derive what each pending stack entry means. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** L; ordinary hand tests remain small. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> iterativeInorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V5");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [3,6,8,10,12,14] |
| U | [9,-2,4,4,0] |
| L(1,2,3) | [3,2,1] |
| R(1,2,3) | [1,2,3] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [MIN,0,MAX] |

**Target costs:** O(n) aggregate time; O(h) auxiliary explicit state; O(n) returned values. 

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Also test iterative construction of a 100000-node left and right chain: result length 100000; correct first/last values; no recursive helper hidden in setup or checks.

<details>
<summary>Hint 1</summary>

A node’s own visit must wait for its left subtree.

</details>

<details>
<summary>Hint 2</summary>

The pending ancestors and a current subtree reference represent different unfinished work.

</details>

<details>
<summary>Hint 3</summary>

Your loop must continue when either a current subtree exists or an ancestor is waiting.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### V6. Iterative postorder with explicit completion state

**Difficulty:** Medium. **Learning objective:** Derive left subtree, right subtree, root visit timing and account for saved work.

**Prerequisites:** V3/V5; suspended work and explicit records.

**Problem and contract:** Return values in left subtree, right subtree, root order. Use ArrayDeque and no recursive calls. Use explicit completion markers/stages, or an equally justified O(h) design. A two-stack O(n)-auxiliary answer is only a baseline. Explain all traversal obligations in Section 2.

**Input / output:** TreeNode root → fresh mutable List<Integer>. Empty → []. Ordered output must contain one entry per identity.

**Constraints and ownership:** L; ordinary hand tests remain small. Read-only, arbitrary values/shape, no new TreeNodes. Output O(n); no persistent accumulator, sorting, subtree list concatenation, or calls to another V solution.

```java
static List<Integer> iterativePostorder(TreeNode root) {
    throw new UnsupportedOperationException("TODO V6");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MIN) | [MIN] |
| K | [6,3,10,14,12,8] |
| U | [9,-2,0,4,4] |
| L(1,2,3) | [3,2,1] |
| R(1,2,3) | [3,2,1] |
| Fork(-1,-1,-1) | [-1,-1,-1] |
| [0,MIN,MAX] | [MIN,MAX,0] |

**Target costs:** O(n) aggregate time; O(h) auxiliary explicit state; O(n) returned values. Distinguish cumulative marker allocation from simultaneously live markers.

**Completion criteria:** Verify ordering by identities as well as values on U, compare to a hand traversal, and show output freshness across calls. Also test iterative construction of a 100000-node left and right chain: result length 100000; correct first/last values; no recursive helper hidden in setup or checks.

<details>
<summary>Hint 1</summary>

A first encounter does not yet mean that both children have completed.

</details>

<details>
<summary>Hint 2</summary>

Save whether a node is waiting for child expansion or its final visit.

</details>

<details>
<summary>Hint 3</summary>

Schedule a later completion entry below child obligations; explain the order of both children.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T2. Predict three call calendars

**Difficulty:** Medium. **Objective:** separate invocation order from visit order. **Prerequisites:** V1–V3. **Input:** fresh U, then E and S(-2). **Output:** three visit sequences and a full enter/return calendar, including null bases, for left-first recursion. **Contract:** G, read-only, no new nodes; a helper appends to an existing list. The signatures below are tracing templates, not finished implementations:

```java
static void tPre(TreeNode node, List<Integer> out) {
    throw new UnsupportedOperationException("TODO: write the traced body");
}
static void tIn(TreeNode node, List<Integer> out) {
    throw new UnsupportedOperationException("TODO: write the traced body");
}
static void tPost(TreeNode node, List<Integer> out) {
    throw new UnsupportedOperationException("TODO: write the traced body");
}
```

Predict on paper before executing your own bodies. Use this row format repeatedly:

| Event | Current identity or named null slot | Live helper depth | Enter/return/visit | Output prefix |
| --- | --- | --- | --- | --- |
| 1 | P | 1 | Enter | [] |
| 2 onward | ? | ? | ? | ? |

Checkpoint example: a singleton appends its one value in all three orders, but at different points relative to its two null child calls. Empty appends nothing and preserves any existing prefix such as [99]. For U, compare your completed sequences to the V test tables **only after predicting them**. Count helpers and maximum live frames, not the wrapper. **Target:** O(n) trace work/storage, O(h) execution frames excluding the written trace. **Completion:** show exactly one visit per identity, all return events, and why P/R remain separate. No answer calendar is supplied.

<details>
<summary>Hint 1</summary>

Changing visit timing need not change the structural calls.

</details>

<details>
<summary>Hint 2</summary>

After a child returns, its caller resumes at the next statement.

</details>

<details>
<summary>Hint 3</summary>

Label null slots by parent and side so distinct base calls do not collapse into one row.

</details>
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T3. Compare explicit stack states

**Difficulty:** Medium. **Objective:** explain a continuation without invoking recursion. **Prerequisites:** V4–V6. **Input:** U, L(1,2,3), and R(1,2,3). **Output:** complete stack-state tables for each iterative traversal. **Constraints:** G, read-only; entries hold existing nodes plus fixed-size stage information. Order matters; empty input produces no pop. Use your V4/V5/V6 signatures as the executable subjects.

Display stacks **bottom→top**, next pop at the right:

```text
initial pending root: [P]
right child waiting while left starts: [R,Q]
an expanded marker: P! means “record P after its scheduled children”
```

| Action | Current reference / popped entry | Stack bottom→top afterward | Output | Peak entries so far |
| --- | --- | --- | --- | --- |
| Initialization | P | [P] for preorder/marker postorder | [] | 1 |
| Continue | ? | ? | ? | ? |

For inorder initialize current=P and stack=[] instead. Record **every** push and pop, not only visits. Example checkpoint: on R(1,2,3), iterative inorder outputs [1,2,3]; a marker-based postorder outputs [3,2,1]. Explain why their peak entries can differ despite the same h. Include E and a singleton to check initialization. **Target:** O(n) operations and written rows, O(h) execution state for these designs, O(n) output. **Completion:** identify the saved-state invariant and justify exact measured peaks; never infer them from the word DFS.

<details>
<summary>Hint 1</summary>

An ancestor awaiting its visit differs from an unexpanded subtree root.

</details>

<details>
<summary>Hint 2</summary>

For postorder, a saved completion entry must survive until both children finish.

</details>

<details>
<summary>Hint 3</summary>

Keep stack display orientation separate from ArrayDeque iteration order, which starts at its front.

</details>
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 5. Level 2: BFS and level information

Use [BFS concepts](./Trees_Essential_Concepts.md#8-bfs-and-level-order-traversal) for gap repair. B1 collects values; B2 emits only compact per-level information. Do not solve B2 by first materializing B1's entire output.

### B1. Independent level lists

**Difficulty:** Medium. **Learning objective:** Implement a FIFO layer boundary and preserve output ownership.

**Prerequisites:** F1; ArrayDeque endpoints; fresh ArrayList containers.

**Problem and contract:** Return one value list per occupied level, root level first and left-to-right within each level. Use ArrayDeque. Describe why each queue-size snapshot belongs to exactly one level and why null children never enter the queue.

**Input / output:** TreeNode root → List<List<Integer>>. Empty → a fresh empty outer list.

**Constraints and ownership:** L. Input read-only; no new nodes. Arbitrary shape/values, duplicates retained. Both outer list and each inner list are fresh mutable containers; distinct levels must not alias.

```java
static List<List<Integer>> levels(TreeNode root) {
    throw new UnsupportedOperationException("TODO B1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(-5) | [[-5]] |
| K | [[8],[3,12],[6,10,14]] |
| U | [[4],[-2,4],[9,0]] |
| R(1,2,3) | [[1],[2],[3]] |
| [1,2,3,4] | [[1],[2,3],[4]] |
| Fork(0,MIN,MAX) | [[0],[MIN,MAX]] |
| Fork(7,7,7) | [[7],[7,7]] |

**Target costs:** O(n) time, O(w) auxiliary queue, O(n) output including level containers. Queue entries may mix a suffix/prefix of adjacent levels; peak is not necessarily exactly w.

**Completion criteria:** Add an inner-list identity check, mutate one output row and show the other rows stay unchanged, then call again and show fresh output. Run a 100000-node chain iteratively and verify 100000 singleton levels.

<details>
<summary>Hint 1</summary>

Nodes discovered behind a current level belong to the next level.

</details>

<details>
<summary>Hint 2</summary>

Fix the number of removals for this layer before appending children.

</details>

<details>
<summary>Hint 3</summary>

Allocate the current output row inside the level iteration; never enqueue null placeholders.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### B2. A compact level ledger

**Difficulty:** Medium. **Learning objective:** Compute width and signed per-level aggregates without retaining every value.

**Prerequisites:** B1; long arithmetic and record outputs.

**Problem and contract:** For each occupied level, return its edge depth, node count, and sum of values. Also return the greatest count across levels as maxWidth. Use ArrayDeque, and calculate only the requested ledger rather than a whole value traversal.

**Input / output:** TreeNode root → LevelLedger(rows, maxWidth), with each LevelRow(depth,count,sum). Empty → ([],0).

**Constraints and ownership:** L. Read-only, no new nodes, arbitrary signed/duplicate values. Rows ordered by increasing depth; rows list fresh/mutable. Each row is immutable; sums are long. Width counts real nodes, not null slots.

```java
record LevelRow(int depth, int count, long sum) {}
record LevelLedger(List<LevelRow> rows, int maxWidth) {}

static LevelLedger levelLedger(TreeNode root) {
    throw new UnsupportedOperationException("TODO B2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | ([],0) |
| S(MIN) | ([(0,1,-2147483648)],1) |
| K | ([(0,1,8),(1,2,15),(2,3,30)],3) |
| U | ([(0,1,4),(1,2,2),(2,2,9)],2) |
| Fork(MAX,MAX,MAX) | ([(0,1,2147483647),(1,2,4294967294)],2) |
| L(-1,-2,-3) | ([(0,1,-1),(1,1,-2),(2,1,-3)],1) |
| W | ([(0,1,0),(1,2,0),(2,4,0),(3,4,0)],4) |

**Target costs:** O(n) time; O(w) auxiliary queue; O(h) required ledger output. Do not count all accumulated rows as queue memory or call total storage O(w).

**Completion criteria:** Explain why maximum queue occupancy is not the returned maxWidth. Compare a long chain with a perfect tree. Test negative totals, overflow-risk sums, and repeated requests.

<details>
<summary>Hint 1</summary>

A layer boundary supplies both a count and a natural grouping for its sum.

</details>

<details>
<summary>Hint 2</summary>

Keep completed ledger rows separate from the current queue invariant.

</details>

<details>
<summary>Hint 3</summary>

Use a long accumulator initialized for each level; take width from the frozen layer size.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T4. Watch the queue cross a level boundary

**Difficulty:** Medium. **Objective:** distinguish level width from instantaneous queue size. **Prerequisites:** B1/B2. **Input:** fresh W, then K and a singleton. **Output:** a row after each removal and its child insertions, showing frozen level size, index within the level, and queue **front→back**. Use B1/B2 signatures as your executable subjects; input is G/read-only and no new nodes are needed.

```text
W at depth 2: queue = [D,E,F,G]
front / next removed is the leftmost entry
children are appended at the right
```

| Removed identity | Frozen size | Queue after adding children | Current-level nodes remaining | Next-level nodes discovered |
| --- | --- | --- | --- | --- |
| D | 4 | ? | ? | ? |
| E | 4 | ? | ? | ? |
| F | 4 | ? | ? | ? |
| G | 4 | ? | ? | ? |

Also trace from the root to this boundary. Example checkpoint: W's level ledger is [1,2,4,4] and maxWidth=4; your queue peak must be measured separately. K's layers are [[8],[3,12],[6,10,14]]. Empty starts with no queued node; singleton has one removal and no child insertion. **Target:** O(n) trace operations, O(w) working queue, O(n) written trace/output. **Completion:** derive a constant-multiple-of-w upper bound without asserting peak≤w; explain what breaks if the loop compares its index with the changing queue.size().

<details>
<summary>Hint 1</summary>

While processing a level, some of its nodes coexist with discovered children.

</details>

<details>
<summary>Hint 2</summary>

Separate the remaining current-level suffix from the next-level prefix.

</details>

<details>
<summary>Hint 3</summary>

Count these two groups after processing D and E; use the frozen size to determine the level boundary.

</details>
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 6. Level 3: Subtree reports and structural changes

Write the six-line contract before Java. Revisit [aggregation](./Trees_Essential_Concepts.md#9-recursive-contracts-and-subtree-aggregation) and [height/balance/diameter](./Trees_Essential_Concepts.md#10-height-balance-and-diameter) only after an attempt.

### A1. One report for structural decisions

**Difficulty:** Medium. **Learning objective:** Combine fixed-size child reports once per node.

**Prerequisites:** F2, V3; path versus subtree meaning.

**Problem and contract:** Return height, size, leaves, whether every node is height-balanced, and edge diameter. Balance means child heights differ by at most one at every node. Diameter is the longest simple node-to-node path anywhere, including entirely below the root. Compute all fields in one pass; do not call a separate height scan at every node.

**Input / output:** TreeNode root → Structure(height,size,leaves,balanced,diameter). Empty → (0,0,0,true,0).

**Constraints and ownership:** G. Read-only, no new nodes; report records allowed. Values do not affect structural results. Height is nodes, diameter edges; scalar fields have no encounter order. Empty/singleton diameter is 0.

```java
record Structure(int height, int size, int leaves,
                 boolean balanced, int diameter) {}

static Structure structure(TreeNode root) {
    throw new UnsupportedOperationException("TODO A1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | (0,0,0,true,0) |
| S(-8) | (1,1,1,true,0) |
| L(1,2) | (2,2,1,true,1) |
| R(-1,-2,-3) | (3,3,1,false,2) |
| K | (3,6,3,true,4) |
| [1,2,3,4,5,6,7] | (3,7,4,true,4) |
| [0,1,null,2,3,4,null,null,5,6,null,null,7] | (5,8,2,false,6) |
| L(1,...,200) | (200,200,1,false,199) |

**Target costs:** O(n) time; O(h) auxiliary frames and simultaneously retained fixed-size reports; O(1) returned report. C3 investigates a repeated-scan baseline separately.

**Completion criteria:** Prove every field, distinguish extendable height from complete diameter, and explain why balance at the root alone is insufficient. For the eight-node sparse test, draw both three-edge arms below node 1; the best path avoids root 0.

<details>
<summary>Hint 1</summary>

The parent needs enough child information to avoid reopening a completed subtree.

</details>

<details>
<summary>Hint 2</summary>

An internal best path can be in either child or connect their downward arms through the current node.

</details>

<details>
<summary>Hint 3</summary>

Return a fixed-size compound result; combine child balance flags as well as child heights.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### A2. Remove only the leaves present at entry

**Difficulty:** Medium. **Learning objective:** Mutate structure under a precise one-round contract.

**Prerequisites:** F1/F2, returned roots, link ownership.

**Problem and contract:** Remove every node that was a leaf at method entry and whose value is below cutoff. This is exactly one simultaneous round: a parent newly exposed as a leaf must remain even if its value is below cutoff. Return the surviving root or null. Preserve values and every retained node identity.

**Input / output:** TreeNode root, int cutoff → surviving TreeNode root. If the original root is a qualifying leaf, return null.

**Constraints and ownership:** G with exclusive permission to change child links. No new TreeNodes. Original leaves already have null children; removed leaves remain detached. Preserve the relative shape of retained nodes and every unaffected edge. Duplicates allowed; no BST/balance assumption.

```java
static TreeNode removeEntryLeaves(TreeNode root, int cutoff) {
    throw new UnsupportedOperationException("TODO A2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E, cutoff 0 | [] |
| S(-1), cutoff 0 | [] |
| S(0), cutoff 0 | [0] |
| K, cutoff 11 | [8,3,12,null,null,null,14] |
| L(-5,-4,-3), cutoff 0 | [-5,-4] |
| Fork(7,7,7), cutoff 8 | [7] |
| Fork(0,MIN,MAX), cutoff MAX | [0,null,MAX] |
| Two consecutive calls on L(-5,-4,-3), cutoff 0 | First [-5,-4]; second [-5] |

**Target costs:** O(n) time, O(h) auxiliary for recursion, no fresh output nodes. A one-round result is generally not a fixed point.

**Completion criteria:** Mark original leaves on a diagram before calling. Compare identity sets and each retained child link afterward. Explain why the second call may remove more nodes and why assigning only a local parameter is insufficient.

<details>
<summary>Hint 1</summary>

The word “original” refers to structure before children are changed.

</details>

<details>
<summary>Hint 2</summary>

Distinguish the entry-time leaf predicate from the exit-time child links.

</details>

<details>
<summary>Hint 3</summary>

A parent must attach returned child roots, while its own removal decision must use the specified round semantics.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 7. Level 3: Ordered queries and inherited bounds

Use [BST queries](./Trees_Essential_Concepts.md#11-binary-search-trees-and-ordered-queries) and [ancestor bounds](./Trees_Essential_Concepts.md#12-bst-validation-with-ancestor-bounds). Strict ordering concerns every descendant in a side, not only the child root. “BST” supplies no height guarantee.

### O1. Lookup and endpoint identities

**Difficulty:** Easy. **Learning objective:** Use the ordering guarantee to discard impossible subtrees.

**Prerequisites:** V2/V5; Binary Search invariants; identity.

**Problem and contract:** Implement lookup by key, minimum-node query, and maximum-node query iteratively. Return existing objects. A missing key or empty-tree endpoint returns null. Do not materialize inorder or validate the supplied BST first.

**Input / output:** findKey(root,key), lowestKey(root), highestKey(root) → TreeNode or null. Test triples below are (found,min,max).

**Constraints and ownership:** S; L-size inputs are permitted when they satisfy S. Input read-only; no allocations of data nodes. Unique keys; arbitrary height. Identity matters; no output ordering beyond the test triple.

```java
static TreeNode findKey(TreeNode root, int key) {
    throw new UnsupportedOperationException("TODO O1 lookup");
}
static TreeNode lowestKey(TreeNode root) {
    throw new UnsupportedOperationException("TODO O1 minimum");
}
static TreeNode highestKey(TreeNode root) {
    throw new UnsupportedOperationException("TODO O1 maximum");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E, key 3 | (null,null,null) |
| K, key 6 | (D,B,F) |
| K, key 9 | (null,B,F) |
| S(MIN), key MIN | (root,root,root) |
| [0,MIN,MAX], key MAX | (right,left,right) |
| R(1,2,3), key 2 | (second,root,last) |
| L(3,2,1), key 4 | (null,last,root) |

**Target costs:** O(h) time per query, O(1) auxiliary, existing-reference output. Three independent queries still take O(h) aggregate asymptotically. Balanced inputs allow O(log n); a chain permits O(n).

**Completion criteria:** Explain every discarded subtree, verify returned references with ==, and test a 100000-node increasing right chain. Give the inorder key sequence for K and identify ranks 1,3,6 without building a rank algorithm yet.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### O2. Return the first ordering witness

**Difficulty:** Medium. **Learning objective:** Carry all relevant ancestor constraints and identify a concrete violation.

**Prerequisites:** V1; strict BST definition; safe numeric bounds.

**Problem and contract:** For an arbitrary tree, return the first node in root-left-right preorder that violates the strict open interval imposed by its proper ancestors. If no node violates a constraint, return null. When a node itself violates, it is the witness before any of its descendants. This is a diagnostic variant of validation.

**Input / output:** TreeNode root → existing offending TreeNode or null. Null means no violation, including empty.

**Constraints and ownership:** G, so duplicate values are valid inputs but can be violations. No mutation/new nodes. Compare all int keys, including MIN/MAX, without overflowing adjusted bounds. Read-only ordered witness, not an arbitrary failing node.

```java
static TreeNode firstOrderingViolation(TreeNode root) {
    throw new UnsupportedOperationException("TODO O2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | null |
| K | null |
| V | D(6) |
| [8,3,12,null,9] | left.right (9) |
| Fork(7,7,8) | left (7) |
| S(MIN) | null |
| [0,MIN,MAX] | null |
| [8,9,7] | left (9), before right (7) |

**Target costs:** O(n) worst-case time, O(h) auxiliary; early witness may stop sooner. Output is one existing reference. No hashing assumption is needed.

**Completion criteria:** Write the inherited interval at each node in V, explain why immediate comparisons accept it, and test duplicates/exclusive endpoints. Explain how witness absence relates to boolean validation without including the platform solution here.

<details>
<summary>Hint 1</summary>

Each ancestor may constrain a descendant even when its parent-child comparison passes.

</details>

<details>
<summary>Hint 2</summary>

A subtree receives an allowed interval; going left or right narrows one endpoint and preserves the other.

</details>

<details>
<summary>Hint 3</summary>

Use bounds wider than int or explicit missing bounds, and propagate the earliest nonnull witness before exploring later preorder work.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 8. Level 3: Paths and reconstruction

See [path information](./Trees_Essential_Concepts.md#13-tree-paths-and-returned-state) and [reconstruction contracts](./Trees_Essential_Concepts.md#14-reconstruction-from-preorder-and-inorder). A path contract must state endpoints, allowed branching, units, and absence. Do not apply positive-only pruning to signed data.

### P1. Count matching terminal routes

**Difficulty:** Medium. **Learning objective:** Keep an inherited prefix separate from completed root-to-leaf paths.

**Prerequisites:** F2; signed long sums; recursive context.

**Problem and contract:** Count distinct root-to-leaf paths whose sum equals target. A path ends only at an actual leaf, not at null or an internal matching prefix. Distinct leaves define distinct paths even if their value sequences are identical.

**Input / output:** TreeNode root, long target → int count. Empty always returns 0, including target 0.

**Constraints and ownership:** G. Read-only, no new nodes, duplicate and negative values permitted. Target may be any long. Sum is over node values, not edge lengths; ordering of counted paths is irrelevant.

```java
static int matchingLeafRoutes(TreeNode root, long target) {
    throw new UnsupportedOperationException("TODO P1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E, target 0 | 0 |
| S(-5), target -5 | 1 |
| K, target 17 | 1 |
| K, target 8 | 0 |
| Fork(1,2,2), target 3 | 2 |
| R(5,-8,3), target 0 | 1 |
| R(MAX,MAX), target 4294967294 | 1 |
| S(MIN), target Long.MIN_VALUE | 0 |
| L(1,...,200), target 20100 | 1 |

**Target costs:** O(n) time, O(h) auxiliary, scalar output. Count all matching leaves; first-success stopping is insufficient.

**Completion criteria:** State what each frame’s inherited prefix means, avoid arithmetic on an arbitrary long target that could overflow, and explain why a negative intermediate total does not justify pruning.

<details>
<summary>Hint 1</summary>

A missing child is not a terminal node.

</details>

<details>
<summary>Hint 2</summary>

Keep the sum of proper ancestors as inherited state and count completed leaves.

</details>

<details>
<summary>Hint 3</summary>

All actual path totals fit long under the size bound; compare a safely accumulated total to target instead of repeatedly subtracting from an arbitrary long target.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### P2. A contribution that can be extended

**Difficulty:** Medium. **Learning objective:** Return one downward branch without accidentally returning a fork.

**Prerequisites:** P1, A1; absence versus a negative answer.

**Problem and contract:** Find the greatest sum of a nonempty downward path that starts at the supplied root. It may stop at any descendant, including the root itself, and may follow at most one child at each step. Return absence for empty. This is not the maximum arbitrary node-to-node path problem.

**Input / output:** TreeNode root → OptionalLong; empty → OptionalLong.empty(). Tests write present(x) for a value.

**Constraints and ownership:** G; read-only, no new nodes. Any signed int values/duplicates. Path sum uses long; no required sequence or tie-breaking because only the sum is returned. Root must be included in every nonempty answer.

```java
static OptionalLong bestRootContribution(TreeNode root) {
    throw new UnsupportedOperationException("TODO P2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | empty |
| S(-8) | present(-8) |
| Fork(-3,4,6) | present(3) |
| Fork(-5,-2,-9) | present(-5) |
| K | present(34) |
| R(5,-8,10) | present(7) |
| R(MAX,MAX) | present(4294967294) |
| R(MIN,MAX) | present(-1) |

**Target costs:** O(n) time, O(h) auxiliary, O(1) output. Fixed-size child contributions suffice; no path-list copying.

**Completion criteria:** Explain what a parent may legally extend and why 4→-3→6 is not a valid downward answer for its root. Preserve all-negative behavior. Contrast with A1 diameter and optional LC8 without implementing the Hard problem.

<details>
<summary>Hint 1</summary>

A downward path cannot use both child branches.

</details>

<details>
<summary>Hint 2</summary>

Stopping at the current node is legal; skipping the required root is not.

</details>

<details>
<summary>Hint 3</summary>

Represent empty separately from negative sums and choose at most one child contribution for extension.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### R1. Plan one reconstruction partition

**Difficulty:** Medium. **Learning objective:** Derive disjoint traversal ranges without writing a full reconstruction solution.

**Prerequisites:** V1/V2; half-open ranges; HashMap index lookup.

**Problem and contract:** Given valid unique-value preorder/inorder arrays for one ordinary tree, a prepared value→inorder-index map, and a valid subtree segment, return the root key, pivot, and both child preorder starts and inorder half-open ranges. Return null for an empty segment. Do not allocate any TreeNode or recursively construct the tree in this drill.

**Input / output:** pre, preStart, inLo, inHi, index → Split(rootKey,pivot,leftPre,leftLo,leftHi,rightPre,rightLo,rightHi) or null. Segment length is inHi-inLo; its preorder segment starts at preStart.

**Constraints and ownership:** Arrays describe a guaranteed valid pair with unique arbitrary int keys, length 0..200; no BST/balance guarantee. Map is exact, built once separately from inorder, and read-only. Supplied segment is valid; empty preStart may equal pre.length. No invalid/duplicate pair is an execution input. All inputs preserved; fixed-size output.

```java
record Split(int rootKey, int pivot,
             int leftPre, int leftLo, int leftHi,
             int rightPre, int rightLo, int rightHi) {}

static Split planPartition(int[] pre, int preStart, int inLo, int inHi,
                           Map<Integer, Integer> index) {
    throw new UnsupportedOperationException("TODO R1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| K arrays; (preStart,inLo,inHi)=(0,0,6) | (8,2,1,0,2,3,3,6) |
| K arrays; (1,0,2) | (3,0,2,0,0,2,1,2) |
| K arrays; (3,3,6) | (12,4,4,3,4,5,5,6) |
| K arrays; (6,6,6) | null |
| pre=[MIN], in=[MIN]; (0,0,1) | (MIN,0,1,0,0,1,1,1) |
| pre=[], in=[]; (0,0,0) | null |
| pre=[3,2,1], in=[1,2,3]; (0,0,3) | (3,2,1,0,2,3,3,3) |

**Target costs:** Expected O(1) per plan with a prepared HashMap, O(1) auxiliary/output. Building the map once costs expected O(n) time and O(n) auxiliary; full indexed reconstruction is a separate task.

**Completion criteria:** Derive child starts from subtree sizes, prove disjoint/exhaustive ranges and strict shrinking, then manually assemble K in T5. Explain why duplicate keys invalidate a single-index map; do not silently impose BST sorting.

K arrays are pre=[8,3,6,12,10,14], in=[3,6,8,10,12,14]. Map indices are zero-based. Empty child starts are still recorded even though no element may be read there.

<details>
<summary>Hint 1</summary>

The subtree root is first in its preorder segment.

</details>

<details>
<summary>Hint 2</summary>

Its unique inorder location separates the left and right node sets.

</details>

<details>
<summary>Hint 3</summary>

Count how many preorder entries the entire left subtree consumes before locating the right start; check the empty segment before indexing.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T5. Rebuild a small shape on paper

**Difficulty:** Medium. **Objective:** recover structure from disjoint ranges and explain uniqueness. **Prerequisites:** R1. **Input:** preorder=[5,2,4,9,7], inorder=[2,4,5,7,9]; unique values, valid pair, n=5. **Output:** drawn tree, exact child-link description, postorder, and complete range-entry table including empty segments. Read-only arrays; draw fresh nodes, no reuse. No BST assumption is needed even though this example happens to satisfy one. Use the hypothetical signature below only as a trace interface; full platform implementation belongs to LC7.

```java
static TreeNode tRebuild(int[] preorder, int[] inorder) {
    throw new UnsupportedOperationException("TODO: trace before implementation");
}
```

| preStart | Inorder [lo,hi) | Root/pivot or empty | Left segment | Right segment |
| --- | --- | --- | --- | --- |
| 0 | [0,5) | ? | ? | ? |
| Continue until every empty range | ? | ? | ? | ? |

Example shape checkpoint, opened only after drawing: canonical serialization is `[5,2,9,null,4,7]`. Derive all three traversals yourself. Repeat with two empty arrays and a singleton; do not read an empty range. As a separate **reasoning question**, draw two different shapes with preorder=[7,7] and inorder=[7,7]. This duplicate pair is outside R1's execution domain: explain ambiguity, not a required rejection implementation. Also show why preorder=[1,2] alone is insufficient.

**Target:** expected O(n) complete range processing with a once-built index map; O(n) map plus O(h) execution frames, O(n) fresh output/drawing. **Completion:** no skipped or overlapping inorder positions, correct boundaries, unique identity per drawn node, termination argument, and a clear uniqueness assumption.

<details>
<summary>Hint 1</summary>

One traversal locates the root; the other splits its descendants.

</details>

<details>
<summary>Hint 2</summary>

A child’s preorder segment length equals its inorder segment length.

</details>

<details>
<summary>Hint 3</summary>

List empty ranges explicitly; they attach null and must return before any array read.

</details>
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### T6. Decide what a parent can use

**Difficulty:** Medium. **Objective:** separate a returned contribution from a complete internal path. **Prerequisites:** A1/P2. **Input:** the two trees below. **Output:** a table of candidate path endpoints, sum, edge length, and whether each is root-to-leaf, root-to-node downward, or arbitrary node-to-node. No mutation/new nodes; duplicates allowed generally, no BST/balance assumption. This is a paper reasoning task; P2 and A1 are its executable check interfaces.

```text
      A(-3)                 X(-5)
      /   \                 /   \
    B(4) C(6)             Y(-2) Z(-9)
```

Example: B→A→C has sum 7 and length 2 edges. Is it legal to attach a hypothetical parent above A to that entire path? Compare it with the best downward path beginning at A. For the second tree, explain why a nonempty path is not automatically allowed to have sum zero. The root-start requirement and arbitrary-endpoint requirement are different.

| Candidate | Sum | Edges | Root-to-leaf? | Downward from root? | Can a parent extend it as one branch? |
| --- | --- | --- | --- | --- | --- |
| A alone | ? | ? | ? | ? | ? |
| A→B | ? | ? | ? | ? | ? |
| A→C | ? | ? | ? | ? | ? |
| B→A→C | 7 | 2 | ? | ? | ? |
| X alone; Y alone; Y→X→Z | ? | ? | ? | ? | ? |

Include E (no nonempty path) and S(MIN) (one nonempty path with sum MIN, zero edges). **Target:** O(1) for these fixed diagrams; a full derived scan should be justified in n/h separately. **Completion:** state exactly what one subtree can contribute upward; explain why an internal two-arm best can be retained as a separate answer but not passed up as a branch. No full Maximum Path Sum implementation is required.

<details>
<summary>Hint 1</summary>

Adding a parent edge to an existing two-arm path can create a fork.

</details>

<details>
<summary>Hint 2</summary>

A returned branch must start at the subtree root and use at most one child branch.

</details>

<details>
<summary>Hint 3</summary>

Write separate meanings for “extendable from here” and “best anywhere inside”; do not use an empty zero as a nonempty all-negative answer.

</details>
- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 9. Level 4: Curated platform practice

The following **eight problems preserve the exact roadmap order and priorities**. E/A/C is learning priority; Easy/Medium/Hard is LeetCode difficulty. Read each official statement for its own domain, signature, and mutation/output expectations; workbook contracts may be broader. These short learning objectives do not reproduce platform statements or reveal solutions. Keep tags/editorials closed during an independent attempt.

Each unchecked record is a form linked to the [canonical Trees Log](./DSA_Interview_Roadmap.md#7-trees). Store date, result, minutes, next review, and lesson there; leave the local form as a pointer. Do not duplicate histories. E problems support P only after independent or successful cold re-solves with explanations/tests; Additional problems support targeted breadth. The Hard challenge never blocks ordinary Trees readiness.

### LC1. 104. Maximum Depth of Binary Tree

**Official problem:** [104. Maximum Depth of Binary Tree](https://leetcode.com/problems/maximum-depth-of-binary-tree/)

**Roadmap priority:** E — Essential. **LeetCode difficulty:** Easy.

**Learning objective:** Define a node-height contract and a direct empty result.

**Prerequisites:** F2, recursive bases and child returns.

**Attempt evidence:** Clarify whether “depth” counts nodes; compare a singleton with a chain. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC2. 226. Invert Binary Tree

**Official problem:** [226. Invert Binary Tree](https://leetcode.com/problems/invert-binary-tree/)

**Roadmap priority:** E — Essential. **LeetCode difficulty:** Easy.

**Learning objective:** Transform all child links while preserving the intended node identities.

**Prerequisites:** F1, A2, returned roots and ownership.

**Attempt evidence:** Explain saved references, a double-inversion check, and recursion space. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC3. 543. Diameter of Binary Tree

**Official problem:** [543. Diameter of Binary Tree](https://leetcode.com/problems/diameter-of-binary-tree/)

**Roadmap priority:** E — Essential. **LeetCode difficulty:** Easy.

**Learning objective:** Distinguish a downward height from an internal edge-path answer.

**Prerequisites:** A1, T6, C3.

**Attempt evidence:** Test a best path that avoids the original root and justify one-pass work. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC4. 102. Binary Tree Level Order Traversal

**Official problem:** [102. Binary Tree Level Order Traversal](https://leetcode.com/problems/binary-tree-level-order-traversal/)

**Roadmap priority:** E — Essential. **LeetCode difficulty:** Medium.

**Learning objective:** Maintain the layer boundary and independent nested output containers.

**Prerequisites:** B1/B2, T4, ArrayDeque.

**Attempt evidence:** Explain queue width versus output size and why null is not enqueued. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC5. 98. Validate Binary Search Tree

**Official problem:** [98. Validate Binary Search Tree](https://leetcode.com/problems/validate-binary-search-tree/)

**Roadmap priority:** E — Essential. **LeetCode difficulty:** Medium.

**Learning objective:** Enforce ancestor constraints for all permitted int keys.

**Prerequisites:** O2, D6/D7, strict duplicate policy.

**Attempt evidence:** Use a deep violation and valid extreme keys; explain short-circuiting. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC6. 230. Kth Smallest Element in a BST

**Official problem:** [230. Kth Smallest Element in a BST](https://leetcode.com/problems/kth-smallest-element-in-a-bst/)

**Roadmap priority:** A — Additional. **LeetCode difficulty:** Medium.

**Learning objective:** Connect inorder order with rank and justify early stopping.

**Prerequisites:** V5, O1, 1-based rank interpretation.

**Attempt evidence:** On K, ranks 1,3,6 correspond to 3,8,14. Derive O(h+k) for a valid k when your implementation supports it; do not assume stored subtree sizes. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC7. 105. Construct Binary Tree from Preorder and Inorder Traversal

**Official problem:** [105. Construct Binary Tree from Preorder and Inorder Traversal](https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/)

**Roadmap priority:** A — Additional. **LeetCode difficulty:** Medium.

**Learning objective:** Recover structure using forced roots, disjoint ranges, and a prepared index map.

**Prerequisites:** R1/T5, map costs, fresh-node ownership.

**Attempt evidence:** Verify both traversal sequences and exact child positions; explain uniqueness and copied-array costs. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

### LC8. 124. Binary Tree Maximum Path Sum

**Official problem:** [124. Binary Tree Maximum Path Sum](https://leetcode.com/problems/binary-tree-maximum-path-sum/)

**Roadmap priority:** C — Optional advanced challenge. **LeetCode difficulty:** Hard.

**Learning objective:** Separate a parent-extendable branch from a best complete nonempty path.

**Prerequisites:** A1/P2/T6; signed values and state lifetime.

**Attempt evidence:** Investigate all-negative data and repeated public calls. Full implementation is optional; no Tries, Heap, Backtracking, or Graph prerequisite is imposed. Explain correctness, time, auxiliary space, output, and applicable edge cases before recording success.

- [ ] Attempt completed and checked. [Canonical Log](./DSA_Interview_Roadmap.md#7-trees): last attempt —; result —; minutes —; next review —; main mistake/lesson —.

## 10. Level 5: Unfamiliar interview challenges

Attempt statements with earlier sections and hints closed. Titles do not identify the intended pattern. For each, write a baseline and at least two candidate approaches before choosing. Explain required state, the recursive promise if applicable, whether shared state is necessary, and why an alternative is unsuitable or more expensive. A prior exposure means the problem is practice rather than unseen R evidence; ask a peer to change the contract for a later mock.

Constraints and cost targets are part of the interview specification, but leave hints closed until after a serious attempt. No complete solutions or pattern answer key are included.

### X1. First service station

**Difficulty:** Medium. **Learning objective:** Select a traversal from depth and tie-breaking requirements.

**Prerequisites:** Core tree traversal and leaf definitions; pattern selection left to you.

**Problem and contract:** A leaf is a service station when its value is at least threshold. Return a qualifying station at minimum edge depth from root. If several qualify at that depth, return the leftmost in structural level order. Return null when none exists.

**Input / output:** TreeNode root, int threshold → existing TreeNode or null.

**Constraints and ownership:** L. Arbitrary shape/duplicates, no BST guarantee, read-only, no new nodes. Identity and tie order matter. Support a 100000-node chain without recursive stack growth.

```java
static TreeNode firstStation(TreeNode root, int threshold) {
    throw new UnsupportedOperationException("TODO X1");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E, threshold 0 | null |
| S(-4), threshold -4 | root |
| K, threshold 10 | E(10) |
| K, threshold 15 | null |
| U, threshold 0 | S(9) |
| [0,5,1,null,null,9], threshold 5 | left (5), ahead of deeper 9 |
| Fork(0,7,7), threshold 7 | left leaf |
| R(1,...,100000), threshold 100000 | last node at depth 99999 |

**Target costs:** O(n) worst-case time, O(w) auxiliary, existing-reference output. Explain any early stop using the requested order.

**Completion criteria:** Justify minimality and tie-breaking, handle an internal node meeting the threshold but not being a station, and compare a deep chain with a wide last level.

<details>
<summary>Hint 1</summary>

The required minimum is about depth, not the greatest value.

</details>

<details>
<summary>Hint 2</summary>

Choose a frontier order that rules out all shallower candidates before accepting an answer.

</details>

<details>
<summary>Hint 3</summary>

Preserve left-before-right discovery and test leafhood with both child slots.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### X2. Inspection credits

**Difficulty:** Medium. **Learning objective:** Derive sufficient returned information from a new counting rule.

**Prerequisites:** Subtree definitions, signed arithmetic, independent contract design.

**Problem and contract:** A node earns one credit if its own value equals the sum of values of all its proper descendants. A leaf has no proper descendants, so that sum is zero. Return total credits across the tree. Each identity can earn at most one credit.

**Input / output:** TreeNode root → int credits; empty → 0.

**Constraints and ownership:** G, read-only, no new nodes; signed values and duplicates valid, no ordering/balance assumption. Descendant sums use long. Output scalar; no path-length quantity involved.

```java
static int inspectionCredits(TreeNode root) {
    throw new UnsupportedOperationException("TODO X2");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | 0 |
| S(0) | 1 |
| S(-5) | 0 |
| Fork(5,2,3) | 1 |
| Fork(0,0,0) | 3 |
| K | 0 |
| U | 1 |
| Fork(MIN,MIN,0) | 2 |
| R(0,0,0) | 3 |

**Target costs:** O(n) time, O(h) auxiliary, scalar output. Rewalking descendants separately for each node fails the time target on a chain.

**Completion criteria:** Write a sufficient child-result contract, prove credit counting without overlap, and compare against an independent small-tree baseline that enumerates each descendant set.

<details>
<summary>Hint 1</summary>

The decision for one node needs a descendant total, while the final answer also needs how many child decisions succeeded.

</details>

<details>
<summary>Hint 2</summary>

A child can return more than one fixed-size fact.

</details>

<details>
<summary>Hint 3</summary>

Distinguish the sum including the current node from the sum used for its own equality test.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### X3. Clearance notices

**Difficulty:** Medium. **Learning objective:** Maintain the right ancestor information with strict comparisons.

**Prerequisites:** Root routes, identity output, traversal state.

**Problem and contract:** Issue a notice for every node whose value is strictly smaller than every proper ancestor on its root route. The root always qualifies. Return qualifying existing nodes in root-left-right preorder. Equal to any minimum ancestor is not smaller.

**Input / output:** TreeNode root → fresh mutable List<TreeNode>; empty → [].

**Constraints and ownership:** G, read-only, no new nodes; duplicates and extreme ints allowed, no BST/balance assumption. Output identity/order matter. Parent comparison alone may be insufficient.

```java
static List<TreeNode> clearanceNotices(TreeNode root) {
    throw new UnsupportedOperationException("TODO X3");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E | [] |
| S(MAX) | [root] |
| K | [A,B] |
| U | [P,Q,T] |
| R(5,7,6) | [first] |
| L(5,3,3,2) | [first,second,fourth] |
| Fork(MIN,MIN,MAX) | [root] |
| R(MAX,MIN) | [first,second] |

**Target costs:** O(n) time, O(h) auxiliary, O(k) output where k notices are issued. Do not copy complete ancestor lists at every node.

**Completion criteria:** Explain the minimum sufficient inherited state, root handling without excluding MAX, and why sibling routes do not contaminate each other. Compare to rescanning ancestors.

<details>
<summary>Hint 1</summary>

Earlier ancestors still matter after the immediate parent changes.

</details>

<details>
<summary>Hint 2</summary>

Summarize the relevant part of the root route instead of storing every value.

</details>

<details>
<summary>Hint 3</summary>

Evaluate the current node against proper ancestors before extending the context for children; handle the root explicitly or with a safe wider bound.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### X4. Restore an archived layout

**Difficulty:** Medium. **Learning objective:** Adapt structural reconstruction to changed traversal information.

**Prerequisites:** Traversal meanings, range contracts, map lookup, new-node ownership.

**Problem and contract:** An archive stores inorder and postorder sequences of the same uniquely labeled ordinary tree. Recreate that exact shape with fresh nodes. Both arrays are guaranteed consistent, equally sized, and individually unique; empty arrays represent empty. Derive the range interface yourself.

**Input / output:** int[] inorder, int[] postorder → fresh TreeNode root or null.

**Constraints and ownership:** 0..200 nodes, arbitrary distinct int keys, no BST/balance guarantee. Arrays read-only. Allocate exactly n output nodes, reuse none from another call; no input trees exist. Invalid or duplicate pairs are outside execution domain, but discuss their ambiguity separately.

```java
static TreeNode restoreLayout(int[] inorder, int[] postorder) {
    throw new UnsupportedOperationException("TODO X4");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| in=[], post=[] | [] |
| in=[MIN], post=[MIN] | [MIN] |
| in=[3,6,8,10,12,14], post=[6,3,10,14,12,8] | [8,3,12,null,6,10,14] |
| in=[1,2,3], post=[3,2,1] | [1,null,2,null,3] |
| in=[1,2,3], post=[1,2,3] | [3,2,null,1] |
| in=[9,4,-2], post=[9,-2,4] | [4,9,-2] |
| in=[MIN,0,MAX], post=[MIN,MAX,0] | [0,MIN,MAX] |

**Target costs:** Expected O(n) time using a once-built HashMap; O(n) map plus O(h) stack auxiliary; O(n) required fresh nodes. No repeated linear pivot scans or copied subarrays in the target design.

**Completion criteria:** Validate exact shape and both supplied traversals, a fresh identity set on every call, and unchanged arrays. Explain which traversal endpoint identifies a subtree root and derive child ranges without copying R1’s preorder arithmetic.

<details>
<summary>Hint 1</summary>

The archive order places a subtree root at a different end from preorder.

</details>

<details>
<summary>Hint 2</summary>

The root’s inorder position still partitions exactly the same two child sets.

</details>

<details>
<summary>Hint 3</summary>

Choose either explicit index ranges or a well-scoped cursor, then justify child processing order and disjoint segment sizes.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### X5. Review a completed export

**Difficulty:** Easy. **Learning objective:** Recognize when building a tree is unnecessary.

**Prerequisites:** Array scans, OptionalInt, numeric range analysis.

**Problem and contract:** A tree system has already exported its values into an int array. The review needs only the minimum value, maximum value, and their mathematical difference. Ignore the former tree shape; no root or edges are supplied. Return absence for both endpoints on an empty export, with span zero.

**Input / output:** int[] values → ExportRange(OptionalInt min, OptionalInt max, long span).

**Constraints and ownership:** Non-null array length 0..100000, arbitrary ints/duplicates, read-only. No ordering guarantee. Do not construct TreeNodes, sort, or use an auxiliary collection. No identity/path/level output is requested.

```java
record ExportRange(OptionalInt min, OptionalInt max, long span) {}

static ExportRange reviewExport(int[] values) {
    throw new UnsupportedOperationException("TODO X5");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| [] | (empty,empty,0) |
| [-3] | (-3,-3,0) |
| [8,3,12,6,10,14] | (3,14,11) |
| [-5,-2,-9] | (-9,-2,7) |
| [7,7,7] | (7,7,0) |
| [MIN,MAX] | (MIN,MAX,4294967295) |

**Target costs:** O(n) time, O(1) auxiliary and output. Explain why hierarchy reconstruction would add irrelevant work and may be impossible from the supplied data.

**Completion criteria:** State why no tree-specific algorithm is needed, preserve the array, and widen before computing max-min. Explain why [0] differs from empty.

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### X6. A fitting reservation

**Difficulty:** Medium. **Learning objective:** Choose an ordered route rather than scanning an entire hierarchy.

**Prerequisites:** Strict BST contract and candidate invariants; no balancing implementation.

**Problem and contract:** Return the existing node with the greatest key at most budget, or null if no key qualifies. Budget need not occur in the tree. Return a node identity, not an insertion point or arithmetic predecessor.

**Input / output:** TreeNode root, int budget → TreeNode or null.

**Constraints and ownership:** S with up to 100000 nodes. Read-only, no new nodes or per-node metadata. Unique keys; arbitrary height, including a full chain. All int budgets/keys allowed. Output scalar identity.

```java
static TreeNode fittingReservation(TreeNode root, int budget) {
    throw new UnsupportedOperationException("TODO X6");
}
```

**Examples and required tests** (fresh fixtures unless stated):

| Input | Expected result |
| --- | --- |
| E, budget 0 | null |
| K, budget 8 | A(8) |
| K, budget 9 | A(8) |
| K, budget 2 | null |
| K, budget MAX | F(14) |
| [0,MIN,MAX], budget -1 | left (MIN) |
| S(MIN), budget MIN | root |
| R(1,...,100000), budget 99999 | node with key 99999 |

**Target costs:** O(h) time, O(1) auxiliary; no recursion for the large domain. Explain the worst-case n-height bound.

**Completion criteria:** State what the retained candidate means, justify each discarded subtree, verify == identity, and compare with an O(n) scan and with incorrect budget-1 arithmetic.

<details>
<summary>Hint 1</summary>

The answer can exist even when the budget key does not.

</details>

<details>
<summary>Hint 2</summary>

Retain the best qualifying key found while narrowing where an improvement could remain.

</details>

<details>
<summary>Hint 3</summary>

Use comparisons directly; MIN/MAX make arithmetic adjustments to the query unnecessary and unsafe.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 11. Complexity investigations

Use [tree cost conventions](./Trees_Essential_Concepts.md#15-time-height-width-and-complete-space), [space foundations](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), and [recursive analysis](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) after deriving your own answer.

For **every C exercise**, submit: output; nonnull-node processing count; helper invocations including null bases; maximum simultaneous family frames; other live objects; total allocation/copy work; returned output storage; and justified time/auxiliary bounds in n,h,w. State best/worst shape where relevant. Count wrappers separately. For loops, report zero recursive-family calls and measure the explicit frontier instead.

Use G unless overridden, preserve all inputs, and allocate no new TreeNodes except C5's required output. Scalar results have no order; list order follows the supplied subject. All subjects are Java 21 members. **Target complexity is the result to derive**, not a supplied answer beside the question. Give a tight bound where possible and explain WHY. A big-O label without work/space reasoning does not pass. Ordinary runs use n≤200; growing-n statements describe the algorithmic model, not a promise of safe deep Java recursion.

### C1. Two calls do not imply exponential work

**Difficulty:** Medium. **Objective:** Count disjoint subtrees and deepest live frames. **Prerequisites:** F2 and recursive contracts.

**Contract / input / output:** Ordinary G tree with values restricted to -1000..1000; return the long sum of squared node values. Empty returns 0. No mutation/new nodes; signed duplicates count separately.

```java
static long cSquares(TreeNode node) {
    if (node == null) return 0L;
    long left = cSquares(node.left);
    long right = cSquares(node.right);
    return (long) node.val * node.val + left + right;
}
```

**Output checkpoints and edge tests:** E→0; S(-3)→9; K→549; U→117; L(1,2,3,4)→30. For shape comparison use a 7-node chain and a 7-node perfect tree with all values 1; both return 7.

**Investigation and completion:** Derive exact helper-call and maximum-frame formulas. Show whether the two child calls process overlapping identity sets. Compare the two seven-node shapes and explain why their outputs/call totals need not distinguish their memory needs. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

Separate node work from null-base work.

</details>

<details>
<summary>Hint 2</summary>

Each ordinary node has exactly two child slots, including missing ones.

</details>

<details>
<summary>Hint 3</summary>

Time adds both child executions; peak live depth follows one active ancestry.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C2. Queue occupancy and value totals

**Difficulty:** Medium. **Objective:** Analyze width-dependent memory independently of a scalar answer. **Prerequisites:** B1/T4 and amortized deque operations.

**Contract / input / output:** G tree; return sum of all values as long, empty 0. Queue stores only nonnull existing nodes, read-only. No output list is required.

```java
static long cQueueTotal(TreeNode root) {
    if (root == null) return 0L;
    Deque<TreeNode> q = new ArrayDeque<>();
    q.offerLast(root);
    long total = 0L;
    while (!q.isEmpty()) {
        TreeNode x = q.pollFirst();
        total += x.val;
        if (x.left != null) q.offerLast(x.left);
        if (x.right != null) q.offerLast(x.right);
    }
    return total;
}
```

**Output checkpoints and edge tests:** E→0; S(MIN)→-2147483648; K→53; U→15; W→0; Fork(MAX,MAX,MAX)→6442450941.

**Investigation and completion:** Count enqueues/removals, derive total work, and measure exact queue peaks for K, W, and a chain. Explain a width-based upper bound when two levels coexist. Contrast scalar output with B1’s nested lists. A single resize is not worst-case constant time, so state the aggregate API assumption. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

Every nonroot node enters through its unique parent edge.

</details>

<details>
<summary>Hint 2</summary>

A queue can contain parts of two adjacent levels.

</details>

<details>
<summary>Hint 3</summary>

Trace W after D and E before treating maximum level width as an exact queue capacity.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C3. A fresh scan at every root

**Difficulty:** Medium. **Objective:** Expose repeated subtree work without confusing it with simultaneous depth. **Prerequisites:** F2/A1 and summations.

**Contract / input / output:** G tree; cNestedTotal returns the sum of every node’s subtree total, including that subtree root. Each helper cSum returns a long value sum. Negative values/duplicates allowed; no mutation/new nodes.

```java
static long cSum(TreeNode node) {
    if (node == null) return 0L;
    return node.val + cSum(node.left) + cSum(node.right);
}
static long cNestedTotal(TreeNode node) {
    if (node == null) return 0L;
    long here = cSum(node);
    long left = cNestedTotal(node.left);
    long right = cNestedTotal(node.right);
    return here + left + right;
}
```

**Output checkpoints and edge tests:** E→0; S(5)→5; K→128; U→35; R(1,2,3,4)→30; Fork(-1,-2,-3)→-11.

**Investigation and completion:** For n-node chains derive both families’ exact call counts, then their combined maximum live frames. Sum scan work by depth for perfect trees. Explain why repeated height scans in a diameter baseline have the same shape-dependent issue. Propose a fixed-size return contract that removes rescans, but do not provide its completed code here. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

Count how many ancestor-rooted scans contain a given node.

</details>

<details>
<summary>Hint 2</summary>

A chain has subtree sizes n,n-1,...,1.

</details>

<details>
<summary>Hint 3</summary>

For space, a scan runs above waiting outer frames; old completed scan stacks do not coexist forever.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C4. Lists returned by every child

**Difficulty:** Medium. **Objective:** Separate intermediate copying, final output, and peak live storage. **Prerequisites:** V traversals, ArrayList costs, recursive lifetime.

**Contract / input / output:** G tree; return all node edge-depths relative to its supplied root in preorder. Values do not affect output. Every call creates a fresh list, including null calls; no mutation/new nodes.

```java
static List<Integer> cDepthLists(TreeNode node) {
    if (node == null) return new ArrayList<>();
    List<Integer> left = cDepthLists(node.left);
    List<Integer> right = cDepthLists(node.right);
    List<Integer> out = new ArrayList<>();
    out.add(0);
    for (int d : left) out.add(d + 1);
    for (int d : right) out.add(d + 1);
    return out;
}
```

**Output checkpoints and edge tests:** E→[]; S(9)→[0]; K→[0,1,2,1,2,2]; U→[0,1,2,1,2]; L(1,2,3,4)→[0,1,2,3].

**Investigation and completion:** Count total appended entries and list allocations on chains and perfect trees. Analyze peak simultaneously live element slots, not the sum of all allocations ever made. Child lists are intermediate output for their calls but auxiliary relative to the public result. Assume materialized ArrayLists with geometrically bounded capacity; do not claim an exact byte count. Explain how a different shared-output contract could avoid repeatedly copying descendant results. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

A descendant’s depth is copied and incremented at each ancestor.

</details>

<details>
<summary>Hint 2</summary>

Returned sibling lists represent disjoint completed subtrees while another branch runs.

</details>

<details>
<summary>Hint 3</summary>

Draw the live child lists and newly growing parent list at one instant; compare that with allocation accumulated over the entire run.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C5. Index preparation and reconstruction work

**Difficulty:** Medium. **Objective:** Account for preprocessing, lookup assumptions, stack, and fresh output. **Prerequisites:** R1/T5, HashMap.

**Contract / input / output:** Non-null, equal-length valid unique preorder/inorder arrays, length 0..200, arbitrary keys and shape. Wrapper prepares one map; the intentionally unimplemented helper has the work specification below. Inputs remain unchanged; n fresh output nodes are required, null for empty.

```java
static TreeNode cIndexedReconstruction(int[] pre, int[] in) {
    Map<Integer, Integer> index = new HashMap<>();
    for (int i = 0; i < in.length; i++) index.put(in[i], i);
    return cIndexedRange(pre, 0, 0, in.length, index);
}
static TreeNode cIndexedRange(int[] pre, int start, int lo, int hi,
                              Map<Integer, Integer> index) {
    throw new UnsupportedOperationException("Analysis boundary; no solution supplied");
}
```

**Output checkpoints and edge tests:** If the helper meets its specification: empty arrays→null; K arrays→K shape; pre=[2,1], in=[1,2]→[2,1]. These are contract checkpoints, not claimed executions of a TODO.

**Investigation and completion:** Assume each nonempty range performs one map get, constant arithmetic, allocates one node, and invokes both disjoint child ranges; each empty range returns directly. Derive whole-wrapper expected time, helper invocation count, stack, map storage, and output. State the hashing assumption. Compare linear pivot lookup and repeated subarray copying on a left chain. Explain why allocating the map inside every helper changes the model. Do not implement the helper as part of this analysis exercise. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

Count map construction once before counting helper work.

</details>

<details>
<summary>Hint 2</summary>

Child ranges partition the remaining positions, even when sizes are unequal.

</details>

<details>
<summary>Hint 3</summary>

The index map is auxiliary; the n fresh nodes are required output; skewed recursive depth still counts.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C6. Explicit entries on different shapes

**Difficulty:** Medium. **Objective:** Measure the chosen DFS frontier instead of assuming it equals h. **Prerequisites:** V4/T3.

**Contract / input / output:** G tree; return the long sum of value×edge-depth for all nodes. Record entries hold existing nodes and depth. Empty returns 0; no input mutation or new TreeNodes.

```java
record DepthEntry(TreeNode node, int depth) {}

static long cWeightedDepth(TreeNode root) {
    if (root == null) return 0L;
    Deque<DepthEntry> stack = new ArrayDeque<>();
    stack.push(new DepthEntry(root, 0));
    long total = 0L;
    while (!stack.isEmpty()) {
        DepthEntry e = stack.pop();
        TreeNode x = e.node();
        total += (long) x.val * e.depth();
        if (x.right != null) stack.push(new DepthEntry(x.right, e.depth() + 1));
        if (x.left != null) stack.push(new DepthEntry(x.left, e.depth() + 1));
    }
    return total;
}
```

**Output checkpoints and edge tests:** E→0; S(MAX)→0; K→75; U→20; L(1,2,3,4)→20; Fork(0,MIN,MAX)→-1.

**Investigation and completion:** Compare exact stack peaks on left/right chains, K, and a perfect tree. Explain an O(h)-style bound for this particular scheduling policy and why a chain may use less. Count total record creation separately from peak live records. Compare V6 markers and B1 output storage. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

The stack retains pending sibling work, not every ancestor automatically.

</details>

<details>
<summary>Hint 2</summary>

A one-child pop can replace itself with one child entry.

</details>

<details>
<summary>Hint 3</summary>

For a branching shape, count pending siblings along the current descent.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### C7. Copying an entire route

**Difficulty:** Medium. **Objective:** Include retained ancestor-path copies in auxiliary space. **Prerequisites:** Recursion lifetime and shallow list copying.

**Contract / input / output:** G tree and initially empty prefix list; cRouteCopies returns the sum over nodes of their root-to-node path node-count. Prefix is never mutated. Each nonnull call copies all ancestor entries and adds its own value; no new TreeNodes.

```java
static int cRouteCopies(TreeNode node, List<Integer> prefix) {
    if (node == null) return 0;
    List<Integer> route = new ArrayList<>(prefix);
    route.add(node.val);
    int left = cRouteCopies(node.left, route);
    int right = cRouteCopies(node.right, route);
    return left + right + route.size();
}
```

**Output checkpoints and edge tests:** E with []→0; S(7) with []→1; K with []→14; U with []→11; R(1,2,3,4) with []→10.

**Investigation and completion:** Analyze chain and perfect-tree time, total copied entries, and maximum simultaneously retained route content. The caller uses route after children return: include it in the source-level materialized-list model. Distinguish this from C4’s child-output lifetime and from sharing one primitive depth value. Count list containers and frames separately; explain why copied values need no duplicate TreeNode objects. Apply all C-section reporting obligations; explain the empty case separately.

<details>
<summary>Hint 1</summary>

One copy contains all proper ancestors, then the current value is appended.

</details>

<details>
<summary>Hint 2</summary>

Suspended callers still need their own route after the child returns.

</details>

<details>
<summary>Hint 3</summary>

Sum route lengths along one active chain for live content; sum them across all nodes for total work.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 12. Debugging laboratory

Each D subject below is **intentionally flawed** against its stated contract. All compile; compilation does not establish correctness. Work on a copy, leaving the subject available for before/after comparison. No corrected implementation or complete solution key is supplied.

For **each D exercise**, identify the defect(s), construct a failing tree, predict and then reproduce actual versus expected behavior, name the violated contract/invariant, repair the code, add a regression test and an ordinary control case, verify ownership/repeated calls, and analyze repaired time/auxiliary/output space. A crash is an observable result, not an excuse to omit the expected behavior. Test only the stated domain; all inputs here are ordinary finite trees or valid traversal pairs.

The supplied method is the Java signature to repair. Each challenge is Medium; write the six-line contract or iterative invariant first. Target bounds refer to the **repaired** implementation. G applies unless overridden; ordinary tests are small and require no stack-overflow experiment.

### D1. A height that loses information

**Difficulty:** Medium. **Learning objective:** Respect null bases and child-return meaning. **Prerequisites:** F2/A1.

**Intended contract / input / output:** G tree→node-height, empty 0, leaf 1. Read-only; no new nodes; duplicates/negative values do not affect height.

```java
static int dHeight(TreeNode node) {
    if (node.left == null && node.right == null) return 1;
    int left = node.left == null ? 0 : dHeight(node.left);
    int right = node.right == null ? 0 : dHeight(node.right);
    return 1 + left;
}
```

**Required witnesses and expected correct results:** E→0; S(7)→1; R(1,2,3)→3; L(1,2,3)→3; K→3. Address both the empty call and asymmetric children. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(h) auxiliary, scalar output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

The first dereference has its own precondition.

</details>

<details>
<summary>Hint 2</summary>

Computing a child result does not ensure the parent uses it.

</details>

<details>
<summary>Hint 3</summary>

Test mirrored chains and explain which one exposes the combination error.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D2. The next child is not the next visit

**Difficulty:** Medium. **Learning objective:** Derive push order from LIFO. **Prerequisites:** V1/V4.

**Intended contract / input / output:** G tree→fresh preorder value list root-left-right, empty []. Read-only, no new nodes, duplicates preserved.

```java
static List<Integer> dPreorder(TreeNode root) {
    List<Integer> out = new ArrayList<>();
    if (root == null) return out;
    Deque<TreeNode> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
        TreeNode x = stack.pop();
        out.add(x.val);
        if (x.left != null) stack.push(x.left);
        if (x.right != null) stack.push(x.right);
    }
    return out;
}
```

**Required witnesses and expected correct results:** Fork(1,2,3)→[1,2,3]; K→[8,3,6,12,10,14]; E→[]; S(-1)→[-1]. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(h) auxiliary, O(n) output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

Record the top immediately after both pushes.

</details>

<details>
<summary>Hint 2</summary>

The required visitation order and push order need not match.

</details>

<details>
<summary>Hint 3</summary>

A chain cannot reveal a sibling-order error; test two different children.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D3. A moving level boundary

**Difficulty:** Medium. **Learning objective:** Maintain a fixed layer obligation. **Prerequisites:** B1/T4.

**Intended contract / input / output:** G tree→fresh nested level lists, root first, left-to-right; empty []. Read-only, no new nodes.

```java
static List<List<Integer>> dMovingLevels(TreeNode root) {
    List<List<Integer>> out = new ArrayList<>();
    if (root == null) return out;
    Deque<TreeNode> q = new ArrayDeque<>();
    q.offerLast(root);
    while (!q.isEmpty()) {
        List<Integer> row = new ArrayList<>();
        for (int i = 0; i < q.size(); i++) {
            TreeNode x = q.pollFirst();
            row.add(x.val);
            if (x.left != null) q.offerLast(x.left);
            if (x.right != null) q.offerLast(x.right);
        }
        out.add(row);
    }
    return out;
}
```

**Required witnesses and expected correct results:** Fork(1,2,3)→[[1],[2,3]]; K→[[8],[3,12],[6,10,14]]; R(1,2,3)→[[1],[2],[3]]; E→[]. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(w) auxiliary, O(n) output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

The condition is re-evaluated after both removals and insertions.

</details>

<details>
<summary>Hint 2</summary>

Current-layer obligations must not change when next-layer nodes are discovered.

</details>

<details>
<summary>Hint 3</summary>

Trace the root with two children and compare the next i with the new queue size.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D4. Missing nodes and shared rows

**Difficulty:** Medium. **Learning objective:** Respect container null rules and output identity. **Prerequisites:** B1, Java Lists references.

**Intended contract / input / output:** G tree→fresh independent level lists, empty []. Preserve input, no new nodes. Two defects may require different witnesses.

```java
static List<List<Integer>> dSharedLevels(TreeNode root) {
    List<List<Integer>> out = new ArrayList<>();
    List<Integer> row = new ArrayList<>();
    Deque<TreeNode> q = new ArrayDeque<>();
    q.offerLast(root);
    while (!q.isEmpty()) {
        int count = q.size();
        for (int i = 0; i < count; i++) {
            TreeNode x = q.pollFirst();
            row.add(x.val);
            if (x.left != null) q.offerLast(x.left);
            if (x.right != null) q.offerLast(x.right);
        }
        out.add(row);
    }
    return out;
}
```

**Required witnesses and expected correct results:** E→[] without exception; L(1,2)→[[1],[2]] with distinct inner lists; S(7)→[[7]]. After completion, changing result row 0 must not change row 1. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(w) auxiliary, O(n) output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

An empty tree and a null queue entry are different representations.

</details>

<details>
<summary>Hint 2</summary>

Adding a mutable list stores its reference, not frozen contents.

</details>

<details>
<summary>Hint 3</summary>

Moving clear() around cannot create independent row identities.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D5. Yesterday’s longest route

**Difficulty:** Medium. **Learning objective:** Separate units and per-call state lifetime. **Prerequisites:** A1, edge diameter.

**Intended contract / input / output:** G tree→edge diameter, empty/singleton 0. Read-only, no new nodes. Calls occur sequentially on one loaded class.

```java
static int dBest;
static int dDiameter(TreeNode root) {
    dDiameterHeight(root);
    return dBest;
}
static int dDiameterHeight(TreeNode node) {
    if (node == null) return 0;
    int left = dDiameterHeight(node.left);
    int right = dDiameterHeight(node.right);
    dBest = Math.max(dBest, left + right + 1);
    return 1 + Math.max(left, right);
}
```

**Required witnesses and expected correct results:** With dBest initially 0: S(7)→0. Separately reset dBest=0 for diagnosis, then call K, S(7), E in order; correct independent results are 4,0,0. Diagnose both unit and lifetime problems; fixing just one is insufficient. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(h) auxiliary, scalar output; repeated public calls independent.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

Count the edges in a singleton path.

</details>

<details>
<summary>Hint 2</summary>

A correct child height and a complete path can use different units.

</details>

<details>
<summary>Hint 3</summary>

Inspect which variables still exist after the public method returns; initialization must belong to the request.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D6. Locally ordered, globally wrong

**Difficulty:** Medium. **Learning objective:** Find a missing ancestor constraint. **Prerequisites:** O2.

**Intended contract / input / output:** G tree→boolean strict-BST validity; empty true, duplicates invalid. Read-only, no new nodes, all int keys.

```java
static boolean dLocalOrder(TreeNode node) {
    if (node == null) return true;
    if (node.left != null && node.left.val >= node.val) return false;
    if (node.right != null && node.right.val <= node.val) return false;
    return dLocalOrder(node.left) && dLocalOrder(node.right);
}
```

**Required witnesses and expected correct results:** V→false; [8,3,12,null,9]→false; K→true; Fork(7,7,8)→false; E→true. Predict the flawed result yourself before executing.

**Target after repair:** O(n) worst-case time, O(h) auxiliary, scalar output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

A child can satisfy its parent while violating an older ancestor.

</details>

<details>
<summary>Hint 2</summary>

Write all bounds at V’s D node.

</details>

<details>
<summary>Hint 3</summary>

Repair the information carried between calls, not just the number of nearby comparisons.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D7. Legal endpoints disappear

**Difficulty:** Medium. **Learning objective:** Use bounds that represent the full key domain. **Prerequisites:** O2; Java numeric types.

**Intended contract / input / output:** G tree→strict-BST validity, including MIN/MAX keys. Empty true; duplicates invalid; read-only, no new nodes.

```java
static boolean dNarrowBounds(TreeNode root) {
    return dNarrowWithin(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
}
static boolean dNarrowWithin(TreeNode node, int low, int high) {
    if (node == null) return true;
    if (node.val <= low || node.val >= high) return false;
    return dNarrowWithin(node.left, low, node.val)
            && dNarrowWithin(node.right, node.val, high);
}
```

**Required witnesses and expected correct results:** S(MIN)→true; S(MAX)→true; [0,MIN,MAX]→true; Fork(7,7,8)→false; V→false. Predict the flawed result yourself before executing.

**Target after repair:** O(n) worst-case time, O(h) auxiliary, scalar output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

Are the initial exclusive endpoints allowed stored keys?

</details>

<details>
<summary>Hint 2</summary>

The interval representation must surround, not consume, valid extreme values.

</details>

<details>
<summary>Hint 3</summary>

Do not repair this with int key±1 arithmetic; reason about wider or explicitly absent bounds.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D8. A misplaced archive cursor

**Difficulty:** Medium. **Learning objective:** Derive child segment boundaries and detect reused positions. **Prerequisites:** R1/T5.

**Intended contract / input / output:** Valid unique preorder/inorder arrays length 0..200→fresh exact tree, null for empty. Arrays read-only; no BST/balance guarantee. The wrapper passes a correct inorder index map.

```java
static TreeNode dRebuild(int[] pre, int[] in) {
    Map<Integer, Integer> index = new HashMap<>();
    for (int i = 0; i < in.length; i++) index.put(in[i], i);
    return dBuild(pre, 0, 0, in.length, index);
}
static TreeNode dBuild(int[] pre, int start, int lo, int hi,
                       Map<Integer, Integer> index) {
    if (lo == hi) return null;
    int value = pre[start];
    int pivot = index.get(value);
    if (pivot < lo || pivot >= hi) throw new IllegalArgumentException("range");
    TreeNode node = new TreeNode(value);
    node.left = dBuild(pre, start + 1, lo, pivot, index);
    node.right = dBuild(pre, start + (pivot - lo), pivot + 1, hi, index);
    return node;
}
```

**Required witnesses and expected correct results:** pre=[2,1,3], in=[1,2,3]→[2,1,3] without exception; pre=[1,2], in=[1,2]→[1,null,2]; pre=[2,1], in=[1,2]→[2,1]; empty→null. Predict the flawed result yourself before executing.

**Target after repair:** Expected O(n) time, O(n) map plus O(h) stack auxiliary, O(n) fresh output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

Every nonempty segment consumes its own root in addition to the left subtree.

</details>

<details>
<summary>Hint 2</summary>

Count preorder positions before the right subtree begins.

</details>

<details>
<summary>Hint 3</summary>

A valid input triggering the pivot-range guard can expose a bad derived start; do not remove the guard to hide it.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D9. A fork disguised as a contribution

**Difficulty:** Medium. **Learning objective:** Honor downward-path and numeric contracts. **Prerequisites:** P2/T6.

**Intended contract / input / output:** Nonempty G tree→greatest long sum of a downward path starting at root, allowed to stop early. Wrapper rejects empty input by precondition; helper uses zero for absent extension. Read-only, no new nodes.

```java
static long dContribution(TreeNode node) {
    if (node == null) return 0L;
    long left = dContribution(node.left);
    long right = dContribution(node.right);
    return node.val + Math.max(0L, left) + Math.max(0L, right);
}
```

**Required witnesses and expected correct results:** Fork(-3,4,6)→3; K→34; Fork(-5,-2,-9)→-5; R(MAX,MAX)→4294967294. A passing all-negative case does not establish the path contract. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(h) auxiliary, scalar output.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

A downward path cannot split into both child branches.

</details>

<details>
<summary>Hint 2</summary>

Distinguish a valid internal two-arm candidate from a branch returned to a parent.

</details>

<details>
<summary>Hint 3</summary>

Preserve the option to stop at the current node, including all-negative data.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

### D10. One round becomes many

**Difficulty:** Medium. **Learning objective:** Test entry-time structure versus changed links. **Prerequisites:** A2; identity-sensitive mutation.

**Intended contract / input / output:** Exactly A2: remove only entry-time leaves below cutoff, preserving all other node identities/values. Return surviving root; empty→null. G with exclusive link mutation, no new nodes.

```java
static TreeNode dPrune(TreeNode node, int cutoff) {
    if (node == null) return null;
    node.left = dPrune(node.left, cutoff);
    node.right = dPrune(node.right, cutoff);
    if (node.left == null && node.right == null && node.val < cutoff) return null;
    return node;
}
```

**Required witnesses and expected correct results:** L(-5,-4,-3), cutoff 0→[-5,-4]; Fork(7,7,7), cutoff 8→[7]; S(-1), cutoff 0→[]; K, cutoff 11→[8,3,12,null,null,null,14]. Verify retained identities, not only values. Predict the flawed result yourself before executing.

**Target after repair:** O(n) time, O(h) auxiliary, no fresh output nodes.

**Completion:** complete all seven debugging obligations above and explain why a plausible ordinary case can miss this defect.

<details>
<summary>Hint 1</summary>

The specification refers to leaves before any rewiring.

</details>

<details>
<summary>Hint 2</summary>

A parent can become a leaf during the child calls.

</details>

<details>
<summary>Hint 3</summary>

Record the relevant entry-time fact before changing children, then apply the promised single round.

</details>

- [ ] Attempt completed and checked. Last attempt: —; result: —; minutes: —; next review: —; main mistake/lesson: —.

## 13. Interview communication

These are oral follow-ups attached to existing attempts, not extra numbered exercises or duplicate tracking rows. Explain from a fresh diagram, not by quoting K's known outputs. Do not read a model answer; ask a peer to challenge one assumption.

| Selected attempt | Explain aloud | Mock interviewer follow-up |
| --- | --- | --- |
| F1/F3 | References, identity, height/depth, empty/absent policy | “Another object has the same value. Why is that not your target?” |
| V1–V3 | Helper promise, null effect, exact recording point, complete coverage | “Where does this suspended caller resume?” |
| V4–V6 | Meaning of every saved entry and why the next pop is correct | “What extra information does postorder need?” |
| B1/B2 | FIFO, frozen layer size, child ordering, output ownership | “Can your queue exceed the size of one level?” |
| A1 | Each returned field, child facts, units, and induction argument | “What if the longest path stays entirely in the left subtree?” |
| A2 | Entry-time leaves, returned roots, identities retained | “Would a second call remove more? Is that a bug?” |
| O1/O2 | Ordered elimination versus full validation, ancestor bounds | “What happens with a valid MIN key or a chain?” |
| P1/P2 | Valid path endpoints, signs, extendable state | “Can a branch passed upward contain two child arms?” |
| R1/X4 | Root choice, subtree sizes, range progress, uniqueness | “Which assumption fails with duplicate labels?” |
| X1–X6 | Baseline, selected approach, sufficient state, rejected alternative | “Which constraint made you choose this method?” |
| C3/C4/C7 | Total work versus simultaneously live state | “Which allocations remain reachable during the next child call?” |

For a 35–45 minute mock, allow time to clarify and test rather than timing only typing. Suggested sequence: restate domain and output; draw a minimal witness; offer a correct baseline; derive the improvement; code in coherent steps; test an asymmetric and boundary case; derive costs; discuss changed assumptions. Timing is adjustable and is not a pass/fail deadline.

On a new structural-query mock, require both a precise recursive return contract and an iterative traversal design. A correct recursive implementation alone does not show that you can represent pending work explicitly. On another session, change the requested information or input depth limit so selection must be reconsidered.

## 14. Cold review and spaced repetition

Follow the roadmap's [review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): approximately **1, 3, 7, and 14 days** after learning, adjusted to actual retention. Earlier review after failure is useful; missed dates do not require restarting. There is no second revision ledger.

1. Open the statement only; close previous code, hints, theory implementations, and solution discussions.
2. Rebuild the contract, diagram, baseline, and improvement from a blank editor.
3. Implement, test an edge case that defeated the earlier attempt, and explain correctness/costs aloud.
4. Compare afterward; classify the gap as recognition, contract, visit order, bounds, ownership, arithmetic, or complexity.
5. Update the exercise's existing record with date, I/H/S, total minutes, next review, and the most useful lesson. Curated problems use their selected canonical record, preferably the roadmap Log.
6. If the approach is remembered, use the cold solve as retention evidence and choose a changed/unseen problem for transfer evidence.

Prioritize S outcomes, then H outcomes, incorrect recursive contracts, traversal-order mistakes, forgotten BST ancestors/extreme bounds, tree complexity errors, and unresolved edge cases. A conceptual hint makes the result H even if the final code is correct and fast. S includes unfinished work; do not convert a copied implementation into I by retyping it immediately.

Use one concise history in the same record, for example `date H → later date I; minutes ...; due ...; lesson ...`. This is a format, not prefilled progress. A failed cold review can invalidate affected roadmap gates; assess the demonstrated skill rather than treating every minor independently fixed typo as loss of all progress.

## 15. Completion and readiness evidence

These are demonstration criteria, not a duplicate dashboard. Record actual T/I/P/R decisions only in the [Trees roadmap section](./DSA_Interview_Roadmap.md#7-trees), using its [common rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category). All milestones begin unchecked.

- [ ] **Fundamentals:** build custom nodes without a parser, draw all links, identify leaves, distinguish height/depth/diameter units, and explain identity, emptiness, and shape/order properties (F1–F3, T1).
- [ ] **Recursive DFS:** implement all three orders from a blank editor, define append contracts/null bases, show coverage and termination, and trace returns (V1–V3, T2).
- [ ] **Iterative DFS:** implement preorder, inorder, and a suitable postorder; explain saved continuations and actual stack peaks, including skewed inputs (V4–V6, T3).
- [ ] **BFS:** use ArrayDeque with frozen layers, no null entries, independent output rows, signed sums, and defensible O(w) working memory (B1/B2, T4).
- [ ] **Subtree aggregation:** derive child reports, combine size/leaves/height/balance/diameter in one pass, and explain why repeated scans can be quadratic (A1, X2, C3).
- [ ] **Mutation:** preserve intended identities and all unaffected links under a stated round/ownership contract (A2 and LC2).
- [ ] **BST reasoning:** use ordered lookup and min/max, validate ancestors rather than local pairs, and handle duplicates/extreme keys correctly (O1/O2, D6/D7).
- [ ] **Paths and reconstruction:** distinguish legal endpoints and extendable contributions; derive disjoint reconstruction ranges and uniqueness assumptions (P1/P2, R1, T5/T6).
- [ ] **Essential practice:** independently solve or cold re-solve all five E problems, explain/test them, and retain correct complexity reasoning (LC1–LC5). A checked H/S attempt is not sufficient for P.
- [ ] **Additional breadth:** use LC6/LC7 for inorder rank and complete preorder/inorder reconstruction when needed. Their A tier remains unchanged; core ordered/range concepts still require a demonstration even if those exact submissions are deferred.
- [ ] **Optional advanced exploration:** if choosing LC8, explain nonempty signed paths and returned-branch versus internal-best state. This checkbox may remain unchecked without blocking ordinary readiness.
- [ ] **Debugging:** reproduce and repair every chosen defect with a counterexample, invariant, structural/identity check, repeated-call check, and cost explanation (D1–D10).
- [ ] **Trees interview readiness:** across separate sessions, independently solve unfamiliar variants, derive a recursive return contract and iterative traversal, justify ancestor constraints, analyze h/w and repeated scans, test empty/skewed/signed/extreme cases, and communicate correctness clearly under mock follow-ups.

**T:** explain core concepts and Java choices without notes. **I:** blank-editor core drills with tests and invariant/cost explanations. **P:** independent/cold Essential solves. **R:** repeatable unfamiliar transfer under the common rubric, including recognition, Java, complexity, edge cases, independence, and communication. No exercise count alone earns a gate. Additional and optional work do not substitute for the core demonstrations.

Do not require Tries, Heap, Backtracking, or Graph algorithms for these milestones. Trees prepares those later branches, each with its own representations and practice.

## 16. References and author validation

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Trees scope/drills/priorities](./DSA_Interview_Roadmap.md#7-trees), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Trees Essential Concepts](./Trees_Essential_Concepts.md): primary theoretical reference, including terminology, traversal, contracts, ownership, bounds, path state, reconstruction, and full costs.
- [Recursion Essential Concepts](./Recursion/Recursion_Essential_Concepts.md), especially [contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) and [explicit state](./Recursion/Recursion_Essential_Concepts.md#14-recursion-iteration-and-explicit-stacks).
- [Stack Essential Concepts](./Stack/Stack_Essential_Concepts.md).
- [Linked List Essential Concepts](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity).
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md).
- [Java Lists](./Java_List_ArrayList_LinkedList.md) and [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md).

These are actual discovered paths. The roadmap supplies learning priorities; official LeetCode pages linked in Section 9 supply platform metadata and task-specific contracts. Titles/numbers/difficulties were checked on **2026-10-28**. E/A/C remain the local roadmap's classifications, including the optional Hard status.

### Java 21 primary documentation

Context7's version-21 Oracle API collection was used for relevant collection contracts; unrelated retrieved snippets were disregarded. Direct Oracle documentation also confirmed deque behavior.

- [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html): null prohibition, FIFO/LIFO endpoints, amortized endpoint work.
- [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html): amortized append and mutable list behavior.
- [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html): basic lookup/update costs under appropriate hash dispersion; absent get and previous-value put semantics.

Expected hash bounds are assumptions, not unconditional worst-case constant guarantees. Asymptotic live-object analysis is separate from exact JVM heap bytes and garbage-collection timing.

### Author validation

Completed **2026-10-28**. All **45 Java blocks**, containing **53 methods** plus shared types, were assembled and compiled with Java 21 using `--release 21 -Xlint:all -Werror`. Learner TODOs and C5's abstract reconstruction boundary intentionally remain unimplemented; compilation does not certify a learner solution.

All **184 coding test rows** were checked against independent fixture/oracle calculations, with **2,262 assertions**. Checks used structural path ordering, ancestor inspection, pairwise path distances, entry-time leaf filtering, and reconstruction by traversal priorities. Structural examples were checked for exact child positions, not only one value sequence. Large-chain identity/depth checkpoints used iterative fixture construction and inspection; this is not a claim that the learner's unfinished methods were executed.

Executable Java analysis subjects passed **82,374 assertions** across **1,884 tree cases**, including all **626 binary-tree shapes with up to seven nodes** under three value assignments, plus targeted fixtures and 200-node chains. Instrumentation checked **318,054 logical invocations** and recorded **45 cost/frontier measurements**. It verified call totals, maximum frames, copying/appending work, retained route entries, read-only inputs, the width-four/queue-six witness, and contrasting chain/perfect-tree costs. These source-level live-storage checks are not exact JVM byte measurements.

All **ten deliberately flawed subjects** were exercised with **36 witness/control checks**, reproducing null failures, traversal order errors, moving level boundaries, output aliasing, wrong diameter units and stale state, missing ancestor bounds, rejected extreme keys, reconstruction range failures, illegal branch contributions, and cascading removal. T1's reference mutation was executed separately. Published analysis output checkpoints were also run against the original uninstrumented snippets. No corrected solutions are included in the workbook.

All **69 local links** resolve to existing files and heading anchors. The final structural audit checked exercise counts, all 55 unchecked attempt records, all 117 progressive hints, code fences, and test-table consistency. SHA-256 comparison confirmed that all **31 pre-existing guide files** remained unchanged. Only **Trees_Exercises.md** was created in the repository; validation artifacts stayed outside DSA_Guide. No prerequisite dependency or correctness/link issue remains unresolved.
