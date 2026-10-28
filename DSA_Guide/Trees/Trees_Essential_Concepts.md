# Trees — Essential Concepts (Java 21)

A tree algorithm becomes easier to derive when each call has a precise job: solve one subtree, preserve the information needed by its caller, and combine the children's answers under an explicit contract.

## Table of Contents

- [1. Introduction and Roadmap Scope](#1-introduction-and-roadmap-scope)
- [2. Tree Fundamentals and Terminology](#2-tree-fundamentals-and-terminology)
- [3. Binary Tree Types and Properties](#3-binary-tree-types-and-properties)
- [4. Java TreeNode References and Structure](#4-java-treenode-references-and-structure)
- [5. Recursive Tree Thinking](#5-recursive-tree-thinking)
- [6. Preorder, Inorder, and Postorder DFS](#6-preorder-inorder-and-postorder-dfs)
- [7. Iterative DFS](#7-iterative-dfs)
- [8. BFS and Level-Order Traversal](#8-bfs-and-level-order-traversal)
- [9. Recursive Contracts and Subtree Aggregation](#9-recursive-contracts-and-subtree-aggregation)
- [10. Height, Balance, and Diameter](#10-height-balance-and-diameter)
- [11. Binary Search Trees and Ordered Queries](#11-binary-search-trees-and-ordered-queries)
- [12. BST Validation with Ancestor Bounds](#12-bst-validation-with-ancestor-bounds)
- [13. Tree Paths and Returned State](#13-tree-paths-and-returned-state)
- [14. Reconstruction from Preorder and Inorder](#14-reconstruction-from-preorder-and-inorder)
- [15. Time, Height, Width, and Complete Space](#15-time-height-width-and-complete-space)
- [16. Java Pitfalls and Demonstrable Failures](#16-java-pitfalls-and-demonstrable-failures)
- [17. Trees and Alternative Representations](#17-trees-and-alternative-representations)
- [18. Pattern Recognition and Derivation](#18-pattern-recognition-and-derivation)
- [19. Conceptual Assessment and Answer Key](#19-conceptual-assessment-and-answer-key)
- [20. Key Takeaways and Readiness Evidence](#20-key-takeaways-and-readiness-evidence)
- [21. References, Validation, and Next Steps](#21-references-validation-and-next-steps)

## 1. Introduction and Roadmap Scope

Trees is an official category in **Phase 3 — Hierarchies and Search Spaces** of the [DSA Interview Roadmap](./DSA_Interview_Roadmap.md#7-trees). Its purpose is to develop structural reasoning: define a subtree's answer, decide what must come from children or ancestors, and select a traversal that makes that information available.

The roadmap places **Binary Search and Linked List before Trees**, supplemented by Phase 0 recursion and queue fundamentals. Existing files are references, not proof that those skills are reliable. Start by checking that you can explain a recursive contract, trace suspended callers, distinguish node identity from value, and maintain an ordered search invariant.

### Reuse prerequisites when a gap appears

| Prerequisite | Existing reference | Demonstration before relying on it |
| --- | --- | --- |
| Recursive calls, bases, progress, returned state | [Recursion](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) | Trace a small array sum and explain the child's exact promise |
| Frames, output, retained allocations | [Recursive costs](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) and [Complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) | Distinguish total calls from simultaneous depth |
| Explicit LIFO storage | [Stack](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack) | Predict push/pop order and state what a saved entry means |
| Java node references | [Linked List](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity) | Explain local reassignment versus changing a shared node's field |
| Ordered elimination and bounds | [Binary Search](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants) | Justify which values remain possible after a comparison |
| Arrays, lists, and maps | [Arrays](./Java_Array.md), [Lists](./Java_List_ArrayList_LinkedList.md), [Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) | Explain copying, fresh mutable output containers, and expected hash lookup cost |

### Coverage and priorities

Core knowledge includes tree types and terminology; Java nodes; recursive and iterative preorder/inorder/postorder; BFS layers; height, size, leaf count, maximum, balance, and diameter contracts; strict BST ordering and ancestor-bound validation; path information; reconstruction from preorder/inorder; and complete complexity analysis.

Predecessor/successor and inorder rank are useful ordered-query extensions. The roadmap lists Kth Smallest and the reconstruction platform problem as Additional practice, but its essential concepts still include ordered reasoning and reconstruction. **Maximum Path Sum is optional advanced practice**: this guide prepares the returned-branch versus complete-path distinction without developing a full Hard-problem solution. AVL/Red-Black implementation, B-trees, deletion cases, Morris traversal, serialization protocols, and graph algorithms are outside this document's core scope.

The roadmap's [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) stay separate. Reading can support theory; blank-editor implementations, independent/cold solves, and unfamiliar transfer need their own evidence. Keep problem histories and readiness evidence in the roadmap; no progress is awarded by this file.

### Contracts shared by the implementations

Unless a section explicitly says otherwise:

- Input is a finite **ordinary rooted binary tree** with 0..100000 distinct reachable nodes. There are no cycles and no child object shared by two incoming child references. A null root represents the empty tree.
- Node values are arbitrary ints, including negatives, duplicates, MIN, and MAX. **BST methods instead assume or test a strict BST**, so duplicate keys are invalid there. MIN/MAX denote Integer.MIN_VALUE/Integer.MAX_VALUE.
- Read-only algorithms preserve every value and child reference. `invertTree` explicitly mutates links under exclusive ownership; reconstruction allocates fresh output nodes. Query methods returning TreeNode return an existing identity, not a clone.
- Structural preconditions and the size bound are assumed; ordinary traversal does not first run a cycle/alias validator. Invalid graph-like input has no promised result. Null is a valid structural base, not an integer-valued data node.
- Recursion requires sufficient available stack. Use small/depth-bounded recursive tests; the 100000-node mathematical domain is **not a safe-depth guarantee**. Deep chains call for the iterative alternatives or an explicit-frame aggregation. Java does not guarantee tail-call elimination.
- Numeric counts/heights/edge diameters fit int under the node bound. Any sum of node values fits long; promote before addition where necessary. No algorithm relies on int wraparound.
- Every returned list is a fresh mutable container owned by that call. Nested BFS levels are separate lists. A shared output list inside one traversal is deliberate; sharing it between independent requests is not.
- Auxiliary space excludes the input and required output, but includes stack frames, explicit frontier entries, maps, and intermediate records. Output storage is stated separately.

The height/width conventions in the next section apply throughout. For empty cases, write O(h+1), O(w+1), or O(n+1) when needed to include constant setup; the usual O(h), O(w), and O(n) shorthand below assumes nonempty inputs.

## 2. Tree Fundamentals and Terminology

### 2.1 A hierarchy with one route from the root

A tree represents nested relationships: a directory contains subdirectories, a syntax expression contains smaller expressions, or an organization contains smaller reporting groups. In a rooted tree, one node is designated the **root**, and edges are viewed from parents toward children.

For an ordinary rooted tree, every nonroot node has exactly one parent; every node is reachable from the root; and there is no cycle. Consequently, there is exactly one root-to-node route. Ignoring directions, a nonempty tree with n nodes has n−1 edges. Equal values do not merge different nodes.

This guide repeatedly uses **Tree A**. Letters name identities; numbers are stored values.

```text
                  A(10)
                 /     \
              B(5)     C(15)
             /   \         \
          D(2)   E(7)      F(20)
```

| Term | Meaning | Tree A |
| --- | --- | --- |
| Root | Node with no parent in this rooted structure | A |
| Parent / child | Endpoints of an immediate downward edge | B is D's parent; D is B's child |
| Siblings | Distinct nodes with the same parent | B/C; D/E |
| Leaf | Node with no children | D, E, F |
| Internal node | Node with at least one child | A, B, C |
| Ancestor / descendant | A node above/below another on the root route | A is an ancestor of F; F is A's descendant |
| Subtree rooted at x | x and all descendants reachable below x | B's subtree contains B,D,E |
| Empty subtree | A missing child position, represented by null | C.left |
| Path | A sequence of adjacent nodes with no repeated node | D→B→A→C→F |
| Width at a depth | Number of actual nodes at that depth | Depth 2 has D,E,F: width 3 |

A subtree is itself a tree. That closure is the reason recursive contracts work naturally: a method accepting a tree root can accept either child root under the same structural assumptions.

### 2.2 Height and depth: state the units

Use these conventions consistently:

- **Node depth** is measured in **edges from the root**. The root has depth 0.
- **Subtree height h** is measured in **nodes on the longest downward route to a leaf**. Empty height is 0; leaf height is 1.
- **Path length and diameter** are measured in **edges**, unless explicitly labeled as a sum or a node count.
- **w** is the maximum number of actual nodes at a single depth, not the number of conceptual null slots.

| Node | Edge depth from A | Subtree height in nodes |
| --- | --- | --- |
| A | 0 | 3 |
| B | 1 | 2 |
| C | 1 | 2 |
| D | 2 | 1 |
| E | 2 | 1 |
| F | 2 | 1 |

Tree A has n=6, h=3, w=3. Its maximum edge depth is 2. The number of occupied levels equals its node-height 3.

Another common convention measures height in edges: a leaf has height 0, and empty height is often defined as −1. For a nonempty tree, edge-height = node-height−1. Many interview APIs call node-height **maximum depth**, counting nodes with a singleton result of 1. Read the problem's units; do not silently mix that convention with a node's root-relative edge depth.

For any path containing k nodes, its edge length is k−1. Thus D→B→A→C→F contains five nodes and four edges. An empty tree has no root depth; our height and diameter methods return zero by contract.

### 2.3 How trees differ from familiar sequences

An array gives positions and fast indexed access, but relationships must be encoded separately. A singly linked list gives one next link and a linear suffix. A binary tree gives two named child positions and branching substructures.

Following a tree child reference does not automatically halve the remaining node count. A child can contain n−1 nodes. Recursion fits the structure, but logarithmic depth requires a structural guarantee.

A general tree can have more than two children, commonly represented by a list of children. The binary-tree methods here are specifically for left/right references, not a universal implementation for arbitrary child counts.

**Self-check Q1:** In Tree A, distinguish the depth of C, the height of C's subtree, and the length of D→F.

**Self-check Q2:** Why does an ordinary rooted tree have one root-to-node route, and what assumption fails if two parents share the same child object?

## 3. Binary Tree Types and Properties

### 3.1 Ordering and shape answer different questions

A **binary tree** has at most two children per node, with distinct left and right positions. A **binary search tree** adds an ordering invariant; it does not add a balance guarantee.

| Type / property | Definition | What it does not guarantee |
| --- | --- | --- |
| General tree | Arbitrary allowed number of children | Left/right positions |
| Binary tree | At most one left and one right child | Sorted values or small height |
| Strict BST | Every left-subtree key is smaller; every right-subtree key is larger; both subtrees obey the rule | Balanced shape |
| Height-balanced binary tree, here | At every node, child node-heights differ by at most one | Complete left-to-right filling or BST order |
| Unbalanced tree | Violates the stated balance condition somewhere | A particular ordering |
| Complete binary tree | Every level except possibly the last is full; the last is filled left to right without gaps | Two children at every internal node |
| Full binary tree | Every node has either zero or two children | Equal leaf depths or logarithmic height |
| Perfect binary tree | Every internal node has two children and all leaves have the same depth | Any ordering on values |
| Skewed tree | Each internal node has only one child, forming a chain | Logarithmic height |

“Balanced” can mean different formal guarantees. Here, the local height-difference condition is explicit and implies logarithmic height. Other balanced search structures use different invariants. A small difference at the root alone is insufficient: **every** subtree must satisfy the condition.

### 3.2 Shape gallery

```text
General tree:       Complete, not full:     Full, not complete:
      A                    A                       A
    / | \                /   \                   /   \
   B  C  D              B     C                 B     C
                       /                             / \
                      D                             D   E

Perfect:           Balanced, not complete:  Skewed strict BST:
      4                    10                       1
    /   \                 /  \                       \
   2     6               5    15                      2
  / \   / \             / \     \                      \
 1   3 5   7           2   7     20                      3
```

In the full-but-not-complete example, B is a leaf while C has two children: the last occupied level has gaps on the left. In the balanced-but-not-complete example, the missing left child of 15 precedes its right child 20.

The displayed perfect tree is also a strict BST because of its values, but perfection alone would still hold if its values were rearranged. Completeness/fullness concern child positions, whereas the BST rule concerns keys.

### 3.3 Height consequences

For a nonempty binary tree under node-height h:

```text
h <= n <= 2^h - 1
h >= ceil(log2(n+1))
```

The inequalities allow height n for a chain. They do not prove h=O(log n).

A perfect tree has n=2^h−1 and bottom width 2^(h−1). A complete tree has h=floor(log2(n))+1. Under the stated height-balanced condition, h=O(log n), although subtrees need not contain exactly equal node counts. A full tree can still have linear height: repeatedly give an internal node one leaf child and one continuing internal child.

Why does the balance condition control height? To reach height h while keeping each difference at most one, a smallest such tree needs child heights h−1 and at least h−2. For h>=2, its minimum node count follows M(h)=1+M(h−1)+M(h−2), with M(0)=0 and M(1)=1. That minimum grows exponentially with h, so supporting height h requires enough nodes to make h logarithmic in n. The guarantee comes from this recursive condition at every node, not from requiring exactly equal halves.

| Shape family | Node-height | Typical widest level | Implication |
| --- | --- | --- | --- |
| Perfect / complete | Θ(log(n+1)) | Can be Θ(n) | Shallow DFS; potentially large BFS frontier |
| Skewed chain | n | 1 | Deep recursive DFS; tiny BFS queue |
| Height-balanced | O(log(n+1)) | Depends on shape; can be large | Search/traversal depth is logarithmic |
| Arbitrary binary tree | Between minimum height and n | Between 1 and O(n) | Analyze actual h and w |

Every BST is binary. A binary tree can violate ordering, and a valid BST can be a chain. Treat “binary,” “search,” “balanced,” and “complete” as separate claims.

**Self-check Q3:** Can a full binary tree have Θ(n) height? Describe a shape.

**Self-check Q4:** Why is Tree A balanced but not complete, and why does its BST validity require a separate check?

## 4. Java TreeNode References and Structure

### 4.1 A node contains values and reference slots

All Java blocks in this guide are class members that can coexist inside a scratch `TreeExamples` class with `import java.util.*;` above it. Records shown later are also nested class members. There are no external dependencies.

```java
static final class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) {
        this.val = val;
    }
}

static TreeNode sampleTree() {
    TreeNode a = new TreeNode(10);
    TreeNode b = new TreeNode(5);
    TreeNode c = new TreeNode(15);
    TreeNode d = new TreeNode(2);
    TreeNode e = new TreeNode(7);
    TreeNode f = new TreeNode(20);

    a.left = b;
    a.right = c;
    b.left = d;
    b.right = e;
    c.right = f;
    return a;
}
```

A TreeNode object stores an int and two reference fields. Newly created left/right fields are null. Assigning `a.left=b` copies the reference to B into A's left field; it does not embed a second copy of B.

```text
caller root ──> A { val=10, left ──> B, right ──> C }
                                  / \           \
                                 D   E           F
```

The diagram represents object relationships, not exposed memory addresses. Java offers references and object access; these examples use no raw pointer arithmetic.

### 4.2 Identity is separate from value

Two separately allocated nodes with value 7 are different identities. `x==y` tests whether references identify the same object; `x.val==y.val` tests equality of stored ints. This plain class inherits identity-based equals/hashCode.

This matters when an API asks about a **specific node**. A value lookup is sufficient only when the contract identifies a node by a unique key, as in our strict BST methods. In an ordinary tree, duplicate values are valid; finding value 7 may find the wrong identity.

Two variables can refer to the same node. Such aliases observe the same field changes. The [Linked List reference model](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity) applies unchanged; the tree simply has two child fields.

### 4.3 Local reassignment and structural mutation

```java
static void rebindRootOnly(TreeNode node) {
    node = new TreeNode(99);
}

static void replaceLeftChild(TreeNode parent, TreeNode replacement) {
    if (parent == null) {
        throw new IllegalArgumentException("parent must be nonnull");
    }
    parent.left = replacement;
}
```

Calling rebindRootOnly(root) changes only a local parameter slot. Calling replaceLeftChild(root,newChild) changes a field of the original root object and therefore changes the reachable structure. Passing null as replacement removes that child connection; it does not instantly delete the detached subtree if another reference still reaches it.

To change the caller's root variable, return the intended entry reference and let the caller assign it. Java passes reference **values** into fresh parameter variables; it does not pass the caller's variable as a replaceable slot.

### 4.4 Structural preconditions are real assumptions

These are not ordinary trees:

```text
Shared child:                       Cycle:
       A                              A
      / \                            /
     B   C                          B
      \ /                            \
       D                              A  (same object)
```

Sharing D gives it two parent routes. A normal recursive traversal can process D twice and any descendants multiple times; mutation through B is visible through C. A cycle can prevent a null base from ever being reached. A self-loop `a.left=a` has the same problem.

Even `a.left=d; a.right=d` violates the unique-parent-route assumption, though there is only one distinct parent object. Two incoming child **edges** reach the same identity.

If untrusted data may contain these structures, validate identity reachability first or use an appropriate graph algorithm with a visitation policy. That requires additional working state and a different contract. The tree traversals below deliberately rely on ordinary-tree input; “follow a child” is a smaller subtree only under that assumption.

**Self-check Q5:** What changes after rebinding a parameter versus assigning parent.left, and what can an external alias observe?

**Self-check Q6:** Why do repeated node values not imply a cycle, while repeated node identities on one downward route do?

## 5. Recursive Tree Thinking

### 5.1 A node plus two smaller trees

A nonempty binary tree consists of a root value, a left subtree, and a right subtree. Either child subtree may be empty. A natural contract therefore speaks about **the subtree rooted at the argument**, not about the entire caller's original tree.

For node-height, the promise is:

> heightNodes(node) returns the number of nodes on the longest downward route from node to a leaf; null returns zero.

If the children correctly report heights L and R, the longest route from the current node uses that node plus the longer child route. The result is 1+max(L,R). It does not add both heights because a downward route chooses one child.

```java
static int heightNodes(TreeNode node) {
    if (node == null) {
        return 0;
    }
    int leftHeight = heightNodes(node.left);
    int rightHeight = heightNodes(node.right);
    return 1 + Math.max(leftHeight, rightHeight);
}
```

The empty base is correct directly. For a nonempty subtree, correct child answers plus one root establish the promised height. Each child subtree has fewer nodes than the current subtree, so all paths terminate on ordinary finite trees.

Do not justify this as “recursion works because it calls itself.” The contract, correct base, valid smaller children, and combination rule supply the argument.

### 5.2 What a suspended frame retains

At Tree A's deepest left descent:

```text
current top: heightNodes(null under D)   -> return 0
             heightNodes(D)             -> waiting for its left height
             heightNodes(B)             -> waiting for its left height
bottom:      heightNodes(A)             -> waiting for its left height
```

A, B, and D have independent local-variable slots, while their node references lead into the same input structure. D's completion does not remove D from the tree; it only completes that invocation. Once B's left child returns, B retains that scalar height while evaluating its right subtree.

The null call is an actual invocation. If h counts nonnull nodes on the deepest route, this helper has at most h+1 simultaneous family frames, including a null base. That is O(h+1), not O(n) for every shape and not O(log n) for every tree. Total calls and depth are analyzed separately in Section 15.

### 5.3 A derivation card for every tree method

| Obligation | Question |
| --- | --- |
| Domain | Is this an ordinary binary tree, strict BST, or another structure? |
| Promise | What exactly does one call return/change for its own subtree? |
| Base | What does null mean for this operation? |
| Children | What facts does each smaller call provide? |
| Combine | How do those facts establish the current answer? |
| Progress | Why are child subtrees genuinely smaller? |
| Ownership | Is input read-only; are new nodes/output objects created; can state escape? |
| Costs | Count calls, per-node work, h-dependent frames, w-dependent frontier, and output separately |

A correct null value is operation-specific. Height/size/leaf count use zero; “this is a valid BST” uses true; “there exists a root-to-leaf path” uses false. A maximum needs an explicit representation for absence. Reusing the same null return mechanically changes the meaning of the function.

### 5.4 Structural transformation: a returned root

A simple inversion illustrates a mutating recursive contract: return the same root identity after swapping left and right **throughout its subtree**. All original values/identities are preserved, and no nodes are allocated. The caller grants exclusive ownership of the links.

```java
static TreeNode invertTree(TreeNode node) {
    if (node == null) {
        return null;
    }
    TreeNode invertedLeft = invertTree(node.left);
    TreeNode invertedRight = invertTree(node.right);
    node.left = invertedRight;
    node.right = invertedLeft;
    return node;
}
```

Both children are inverted before their returned roots are placed in swapped positions. Saving both references prevents losing a subtree during rewiring. The base preserves an empty subtree. If both child postconditions hold, swapping those two inverted subtrees establishes the current postcondition.

Tree A becomes:

```text
                  A(10)
                 /     \
              C(15)    B(5)
              /        /   \
           F(20)     E(7)  D(2)
```

The result is still an ordinary binary tree but generally no longer a strict BST under the same left-smaller rule. External aliases observe the changed child fields. Inverting twice restores all original links, a useful identity-sensitive test.

Time is Θ(n+1), auxiliary stack O(h+1), and there are no new data nodes. Rewiring in the original objects does not make recursive stack storage O(1).

**Self-check Q7:** Why does height combine child answers with max, while subtree size will add them?

**Self-check Q8:** What must a recursive transformation promise about returned identities and child links, beyond returning the right root value?


## 6. Preorder, Inorder, and Postorder DFS

### 6.1 Visit timing is the decision

**Depth-first search (DFS)** follows one child subtree through to completion before continuing with the other. A **visit** is the application action for a node, such as appending its value. Entering a frame is not automatically the visit.

For a left-first recursive traversal, the structural calls are the same. The application action can occur at three different points:

```text
enter node
  [preorder visit]
  finish left subtree
  [inorder visit]
  finish right subtree
  [postorder visit]
return
```

| Order | Visit rule | Intuition / applications |
| --- | --- | --- |
| Preorder | Root → Left → Right | Process context before descendants; start a copied hierarchy; record rooted structure with sufficient shape information |
| Inorder | Left → Root → Right | Put the root between its left/right groups; enumerate a strict BST in increasing order |
| Postorder | Left → Right → Root | Finish dependencies before combining child results; height, balance, diameter, structural transformations |

Preorder **values alone** do not generally encode an arbitrary tree's shape. A copying/serialization scheme needs additional shape information or other assumptions; Section 14 explains the ambiguity.

### 6.2 Preorder: announce the node, then explore

**Contract:** append exactly this subtree's values in root-left-right order to the supplied output, preserving the output's existing prefix and the input tree. An empty subtree appends nothing. The public method owns a fresh output list.

```java
static List<Integer> preorder(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    preorderFrom(root, output);
    return output;
}

private static void preorderFrom(TreeNode node, List<Integer> output) {
    if (node == null) {
        return;
    }
    output.add(node.val);
    preorderFrom(node.left, output);
    preorderFrom(node.right, output);
}
```

The root is recorded before either descendant group. If the left helper correctly appends the left group's preorder and the right helper correctly appends the right group's preorder, the final append sequence is exactly root+left+right. No subtree's values are copied into a separate intermediate list.

Tree A produces:

```text
[10, 5, 2, 7, 15, 20]
```

### 6.3 Inorder: finish everything to the left first

**Contract:** append exactly this subtree's values in left-root-right order, with the same prefix-preservation and read-only guarantees. Null appends nothing.

```java
static List<Integer> inorder(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    inorderFrom(root, output);
    return output;
}

private static void inorderFrom(TreeNode node, List<Integer> output) {
    if (node == null) {
        return;
    }
    inorderFrom(node.left, output);
    output.add(node.val);
    inorderFrom(node.right, output);
}
```

The left child's promise supplies all left values before the root. The right child's promise supplies all right values afterward. In a strict BST, every left value is less than the root and every right value greater; recursively sorted child sequences therefore concatenate into a **strictly increasing** sequence. This reasoning uses the full subtree invariant, not merely comparisons with immediate children.

For an arbitrary binary tree, the same code still gives inorder traversal, but its output need not be sorted. Tree A happens to be a strict BST:

```text
[2, 5, 7, 10, 15, 20]
```

### 6.4 Postorder: finish both obligations before the node

**Contract:** append exactly this subtree's values in left-right-root order, preserving the previous output prefix and all input links/values. Null appends nothing.

```java
static List<Integer> postorder(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    postorderFrom(root, output);
    return output;
}

private static void postorderFrom(TreeNode node, List<Integer> output) {
    if (node == null) {
        return;
    }
    postorderFrom(node.left, output);
    postorderFrom(node.right, output);
    output.add(node.val);
}
```

Correct child append sequences precede the current value, establishing left+right+root. For aggregation, replace “append node” with “combine the two answers” while preserving that dependency order.

Tree A produces:

```text
[2, 7, 5, 20, 15, 10]
```

All three helper families terminate because child subtrees have fewer nodes. Every nonnull node is visited once. Duplicated values are recorded once per node identity, not deduplicated.

### 6.5 The shared call/return calendar

This is the **complete helper call/return schedule** on Tree A, omitting wrapper frames and output append events. Depth counts helper frames, including null bases. Each “return null-call” means a void helper returns immediately; no value is appended for null.

| Event | Current invocation | Helper depth | Control action |
| --- | --- | --- | --- |
| 1 | A | 1 | Enter A |
| 2 | B | 2 | Enter A.left |
| 3 | D | 3 | Enter B.left |
| 4 | D.left=null | 4 | Enter null base |
| 5 | D.left=null | 4 | Return from null base |
| 6 | D.right=null | 4 | Enter null base |
| 7 | D.right=null | 4 | Return from null base |
| 8 | D | 3 | Return from D |
| 9 | E | 3 | Enter B.right |
| 10 | E.left=null | 4 | Enter null base |
| 11 | E.left=null | 4 | Return from null base |
| 12 | E.right=null | 4 | Enter null base |
| 13 | E.right=null | 4 | Return from null base |
| 14 | E | 3 | Return from E |
| 15 | B | 2 | Return from B |
| 16 | C | 2 | Enter A.right |
| 17 | C.left=null | 3 | Enter null base |
| 18 | C.left=null | 3 | Return from null base |
| 19 | F | 3 | Enter C.right |
| 20 | F.left=null | 4 | Enter null base |
| 21 | F.left=null | 4 | Return from null base |
| 22 | F.right=null | 4 | Enter null base |
| 23 | F.right=null | 4 | Return from null base |
| 24 | F | 3 | Return from F |
| 25 | C | 2 | Return from C |
| 26 | A | 1 | Return from A |

A return event is recorded while that invocation is still live; its frame is removed afterward. There are thirteen helper invocations: six nonnull and seven null. Maximum helper frames are four, even though the entire execution contains thirteen calls. A public wrapper adds one waiting frame.

At event 4:

```text
top / current
  visit(null under D) -> direct return
  visit(D)           -> waiting for left call, then continue
  visit(B)           -> waiting for left call, then continue
  visit(A)           -> waiting for left call, then continue
bottom
```

The right subtree C has not been called. It is not another simultaneously allocated stack branch.

### 6.6 Dry runs: the same calls, different application actions

For **preorder**, append immediately after each nonnull entry:

| Visit | Calendar position | Current node | Live nonnull ancestor chain | Output afterward |
| --- | --- | --- | --- | --- |
| 1 | After event 1 | A | A | [10] |
| 2 | After event 2 | B | A→B | [10,5] |
| 3 | After event 3 | D | A→B→D | [10,5,2] |
| 4 | After event 9 | E | A→B→E | [10,5,2,7] |
| 5 | After event 16 | C | A→C | [10,5,2,7,15] |
| 6 | After event 19 | F | A→C→F | [10,5,2,7,15,20] |

For **inorder**, append after the left helper returns:

| Visit | Calendar position | Current node | Just completed left subtree | Output afterward |
| --- | --- | --- | --- | --- |
| 1 | After event 5 | D | Empty | [2] |
| 2 | After event 8 | B | D | [2,5] |
| 3 | After event 11 | E | Empty | [2,5,7] |
| 4 | After event 15 | A | B's subtree | [2,5,7,10] |
| 5 | After event 18 | C | Empty | [2,5,7,10,15] |
| 6 | After event 21 | F | Empty | [2,5,7,10,15,20] |

For **postorder**, append after both children and immediately before returning from the current nonnull frame:

| Visit | Calendar position | Current node | Completed child subtrees | Output afterward |
| --- | --- | --- | --- | --- |
| 1 | Before event 8 | D | Empty / empty | [2] |
| 2 | Before event 14 | E | Empty / empty | [2,7] |
| 3 | Before event 15 | B | D / E | [2,7,5] |
| 4 | Before event 24 | F | Empty / empty | [2,7,5,20] |
| 5 | Before event 25 | C | Empty / F | [2,7,5,20,15] |
| 6 | Before event 26 | A | B's subtree / C's subtree | [2,7,5,20,15,10] |

The three visit tables are not three different structural call trees. They place work at different continuations in the same left-first call schedule. A frame resumes where it stopped; it does not restart its method from the top after the child returns.

### 6.7 Costs and an important implementation choice

For each traversal, total helper calls are 2n+1: each nonnull node makes two child calls, including missing-child bases. The maximum helper depth is h+1 when n>0, or one null frame when empty. Appending n fixed-width values takes O(n) aggregate time with ArrayList's amortized append bound.

Therefore time is Θ(n+1), auxiliary stack O(h+1), and returned output O(n+1). Locals contain references and fixed-size scalars, not a copy of the subtree in every frame.

An alternative that recursively builds child lists and repeatedly concatenates/copies them may do more work. On a chain, repeatedly copying the full accumulated suffix can make time quadratic. The shared per-call output list above appends each visited value once and belongs to only that public request.

**Self-check Q9:** Why can all three traversals have the same recursive calls but different output orders?

**Self-check Q10:** Is inorder output sorted for any binary tree? State the ordering assumption and duplicate policy required for the strict-increasing claim.

## 7. Iterative DFS

The call stack implicitly remembers what to do next. An iterative algorithm represents the required pending state explicitly. The [Stack guide](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack) explains the shared LIFO principle.

Use `Deque<TreeNode> stack=new ArrayDeque<>();` with push/pop at the front. **ArrayDeque rejects null entries**; push only actual child nodes. A pop on an empty deque throws, so every pop below is justified by a nonempty-stack condition. Tables display stacks **bottom-to-top**, making the rightmost entry the next to pop.

### 7.1 Iterative preorder: right waits below left

After visiting a node, the left subtree must run before the right. Because the stack is LIFO, push the right child first and the left child second.

**Invariant:** the stack contains pending roots whose subtrees remain to be traversed; its top is the next preorder node. For each pending right subtree, the necessary earlier left-side work is above it.

```java
static List<Integer> preorderIterative(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    if (root == null) {
        return output;
    }
    Deque<TreeNode> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
        TreeNode node = stack.pop();
        output.add(node.val);
        if (node.right != null) {
            stack.push(node.right);
        }
        if (node.left != null) {
            stack.push(node.left);
        }
    }
    return output;
}
```

Every popped root is visited before its descendants. Right-first pushes make the next pop a left child whenever one exists. Applying that rule at each node preserves preorder; each node is pushed/popped once.

| Step | Popped node | Pushes in execution order | Stack afterward, bottom→top | Output |
| --- | --- | --- | --- | --- |
| Initial | — | A | [A] | [] |
| 1 | A | C, B | [C,B] | [10] |
| 2 | B | E, D | [C,E,D] | [10,5] |
| 3 | D | None | [C,E] | [10,5,2] |
| 4 | E | None | [C] | [10,5,2,7] |
| 5 | C | F | [F] | [10,5,2,7,15] |
| 6 | F | None | [] | [10,5,2,7,15,20] |

Time is Θ(n+1), explicit auxiliary stack O(h+1), output O(n+1). The O(h) bound comes from at most a constant number of pending sibling obligations per ancestor, not from storing every descendant at once. A one-child chain uses only one pending root in this particular version even though recursive depth would be linear.

### 7.2 Iterative inorder: save ancestors while going left

An inorder visit must wait until the left subtree is finished. Save the ancestors whose visits remain pending, descend left, then pop the closest waiting ancestor. After its visit, start the same process in its right subtree.

**Invariant:** stacked nodes are waiting for their own inorder visit, in ancestor order. The current reference identifies the next subtree whose left descent must be expanded. When current is null, the stack's top is the next unvisited inorder node.

```java
static List<Integer> inorderIterative(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    Deque<TreeNode> stack = new ArrayDeque<>();
    TreeNode current = root;
    while (current != null || !stack.isEmpty()) {
        while (current != null) {
            stack.push(current);
            current = current.left;
        }
        TreeNode node = stack.pop();
        output.add(node.val);
        current = node.right;
    }
    return output;
}
```

A node is pushed once while finding the next leftmost unresolved position, and popped once after its left side is done. Switching to the popped node's right child ensures its right values follow it. If a right child is null, the next waiting ancestor is used.

| Step | Action | current afterward | Stack bottom→top | Output |
| --- | --- | --- | --- | --- |
| 1 | Push A, go left | B | [A] | [] |
| 2 | Push B, go left | D | [A,B] | [] |
| 3 | Push D, go left | null | [A,B,D] | [] |
| 4 | Pop/visit D, go right | null | [A,B] | [2] |
| 5 | Pop/visit B, go right | E | [A] | [2,5] |
| 6 | Push E, go left | null | [A,E] | [2,5] |
| 7 | Pop/visit E, go right | null | [A] | [2,5,7] |
| 8 | Pop/visit A, go right | C | [] | [2,5,7,10] |
| 9 | Push C, go left | null | [C] | [2,5,7,10] |
| 10 | Pop/visit C, go right | F | [] | [2,5,7,10,15] |
| 11 | Push F, go left | null | [F] | [2,5,7,10,15] |
| 12 | Pop/visit F, go right | null | [] | [2,5,7,10,15,20] |

The outer guard needs **current!=null OR stack nonempty**. Checking only current would stop before visiting saved ancestors. Checking only stack would fail on the initial empty stack with a nonnull root.

Time is Θ(n+1), auxiliary stack O(h+1), output O(n+1). A left chain fills the stack with h nodes; a right chain can keep only one. Both are within the O(h) upper bound.

### 7.3 Iterative postorder: encode the continuation

A node must wait for **both** children before its visit. A simple explicit representation stores (node,expanded). A first encounter schedules a later completion visit below its children; the second encounter records the value.

**Invariant:** an unexpanded entry means “schedule this subtree's work”; an expanded entry means “record this node after the child work already scheduled above it.” The saved flag represents where a recursive frame would resume.

```java
record PostVisit(TreeNode node, boolean expanded) {}

static List<Integer> postorderIterative(TreeNode root) {
    List<Integer> output = new ArrayList<>();
    if (root == null) {
        return output;
    }
    Deque<PostVisit> stack = new ArrayDeque<>();
    stack.push(new PostVisit(root, false));
    while (!stack.isEmpty()) {
        PostVisit entry = stack.pop();
        TreeNode node = entry.node();
        if (entry.expanded()) {
            output.add(node.val);
            continue;
        }
        stack.push(new PostVisit(node, true));
        if (node.right != null) {
            stack.push(new PostVisit(node.right, false));
        }
        if (node.left != null) {
            stack.push(new PostVisit(node.left, false));
        }
    }
    return output;
}
```

Every PostVisit entry is nonnull, and this algorithm also guarantees its node field is nonnull. ArrayDeque checks entries themselves, not whether arbitrary fields inside an entry are null.

The expanded marker is pushed **before** child obligations, so it stays below them. Right is pushed before left. Thus left completes, then right, then the saved parent marker is popped.

In this table, A! means an expanded entry for A; a bare letter is unexpanded.

| Step | Popped entry | Action | Stack bottom→top afterward | Output |
| --- | --- | --- | --- | --- |
| Initial | — | Push A | [A] | [] |
| 1 | A | Schedule A!, C, B | [A!,C,B] | [] |
| 2 | B | Schedule B!, E, D | [A!,C,B!,E,D] | [] |
| 3 | D | Schedule D! | [A!,C,B!,E,D!] | [] |
| 4 | D! | Visit 2 | [A!,C,B!,E] | [2] |
| 5 | E | Schedule E! | [A!,C,B!,E!] | [2] |
| 6 | E! | Visit 7 | [A!,C,B!] | [2,7] |
| 7 | B! | Visit 5 | [A!,C] | [2,7,5] |
| 8 | C | Schedule C!, F | [A!,C!,F] | [2,7,5] |
| 9 | F | Schedule F! | [A!,C!,F!] | [2,7,5] |
| 10 | F! | Visit 20 | [A!,C!] | [2,7,5,20] |
| 11 | C! | Visit 15 | [A!] | [2,7,5,20,15] |
| 12 | A! | Visit 10 | [] | [2,7,5,20,15,10] |

There are two entries processed per node, giving Θ(n+1) time with amortized deque operations. Only O(h+1) markers/pending siblings coexist; Tree A peaks at five entries. Total entry allocation across the run is O(n), but peak algorithmic live entry storage is O(h), excluding output. Reclaimed objects need not disappear from the JVM heap immediately; peak live working storage is the asymptotic model here.

A two-stack postorder variant is also valid but can retain O(n) nodes. Do not claim that every algorithm called “iterative DFS” automatically uses O(h) space. The bound depends on the chosen state representation.

### 7.4 Recursion and explicit state

| Aspect | Recursive DFS | These explicit-stack DFS versions |
| --- | --- | --- |
| Saved continuation | JVM invocation state | Pending roots, ancestors, or expanded markers |
| Java call depth | O(h+1) | O(1) |
| Algorithmic auxiliary space | O(h+1) frames | O(h+1) entries as an upper bound |
| Deep-chain behavior | Can exceed available call stack | Avoids recursive depth; explicit storage still costs memory |
| Clarity | Often closely matches a subtree contract | Requires correct push/stage rules |
| Mutation | Not inherent | Not inherent |

Iteration removes recursive invocation growth; it does not guarantee constant total storage. For iterative **aggregation**, a visited marker alone is not enough if child results must also be retained. Add stage/result fields or another justified storage design; storing all answers in a node→result map would use O(n) auxiliary space.

**Self-check Q11:** Why does preorder push right before left, and what changes if the pushes are reversed?

**Self-check Q12:** What does an expanded postorder entry remember that a bare node reference does not?


## 8. BFS and Level-Order Traversal

### 8.1 Why a queue produces layers

**Breadth-first search (BFS)** processes nodes in increasing edge depth from the root. A FIFO queue preserves discovery order: nodes discovered earlier at a shallower depth are removed before children appended later.

Use `offerLast` to append at the back and `pollFirst` to remove from the front. `size` reads the current entry count; `isEmpty` checks whether there is pending work. ArrayDeque rejects null, so missing children are skipped rather than enqueued as placeholders.

At the start of one layer, snapshot the queue size. Exactly that many nodes belong to the current layer; newly appended children belong to the next one. Reading the queue's changing size as the loop bound would mix layers.

**Contract:** return a fresh list of fresh value lists, one per occupied level, left-to-right within each level. A null root returns an empty outer list. Input is read-only.

```java
static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) {
        return levels;
    }
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.offerLast(root);
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        List<Integer> level = new ArrayList<>(levelSize);
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.pollFirst();
            level.add(node.val);
            if (node.left != null) {
                queue.offerLast(node.left);
            }
            if (node.right != null) {
                queue.offerLast(node.right);
            }
        }
        levels.add(level);
    }
    return levels;
}
```

**Invariant:** at a layer boundary, the queue holds precisely the next level's nodes in left-to-right order. During processing, it holds the unprocessed suffix of that level followed by the discovered prefix of the next. The fixed levelSize removes exactly the current level. Appending each node's left then right child preserves next-level order.

pollFirst can return null on an empty deque, but this loop never polls an empty queue: the first layer starts nonempty, and the fixed number of current-level nodes remains available regardless of appended children. This is a control-flow guarantee, not a claim that pollFirst's API never returns null.

### 8.2 Queue dry run on Tree A

Queue displays are **front-to-back**, so the leftmost entry is removed next.

```text
start: [A]
after A: [B,C]
after B: [C,D,E]       current level still includes C
after C: [D,E,F]       next level is now complete
after D: [E,F]
after E: [F]
after F: []
```

| Step | Fixed size for this level | Removed node | Queue after enqueueing children | Current level | Completed levels |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | A | [B,C] | [10] | [[10]] |
| 2 | 2 | B | [C,D,E] | [5] | [[10]] |
| 3 | 2 | C | [D,E,F] | [5,15] | [[10],[5,15]] |
| 4 | 3 | D | [E,F] | [2] | [[10],[5,15]] |
| 5 | 3 | E | [F] | [2,7] | [[10],[5,15]] |
| 6 | 3 | F | [] | [2,7,20] | [[10],[5,15],[2,7,20]] |

Each node is enqueued and dequeued once. With amortized deque endpoints and ArrayList appends, aggregate time is Θ(n+1). Queue auxiliary space is O(w+1); required output is O(n+1), including the level containers. The current level list is part of the required output, not an extra full-tree working copy.

### 8.3 Queue size need not equal level width at every instant

Suppose a level has four nodes P,Q,R,S and only P/Q have children:

```text
current level:       P     Q     R     S
                   / \   / \
next level:       p1 p2 q1 q2

queue at boundary:       [P,Q,R,S]                 size 4
after processing P:      [Q,R,S,p1,p2]             size 5
after processing Q:      [R,S,p1,p2,q1,q2]          size 6
```

A binary tree can have maximum level width w=4 yet a queue peak of 6 here. The queue mixes two adjacent levels. Their combined sizes are at most 2w, establishing O(w), without claiming an exact cap of w. Tree A's particular peak is three.

On a chain, w=1 and the queue remains tiny. On a perfect tree, w=Θ(n), so BFS can require linear working storage. “BFS is iterative” does not mean constant auxiliary space.

### 8.4 Shallowest information: stop at the first leaf

BFS is especially natural when the required answer concerns the first depth at which a condition is reached. For **minimum root-to-leaf node-depth**, a leaf must have **both** children missing. A single absent child is not a root-to-leaf path ending at that missing position.

```java
static int minimumLeafDepth(TreeNode root) {
    if (root == null) {
        return 0;
    }
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.offerLast(root);
    int depthNodes = 1;
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.pollFirst();
            if (node.left == null && node.right == null) {
                return depthNodes;
            }
            if (node.left != null) {
                queue.offerLast(node.left);
            }
            if (node.right != null) {
                queue.offerLast(node.right);
            }
        }
        depthNodes++;
    }
    return 0;
}
```

The first removed leaf has minimum depth because all earlier levels have already been checked. Tree A returns 3; a singleton returns 1; a two-node chain returns 2. Null returns 0 by contract.

Worst-case time is O(n+1), queue space O(w+1), and output scalar. Early stopping may inspect fewer than n nodes, but its cost includes any children already enqueued before that stop. DFS can also compute the answer correctly; BFS makes the increasing-depth guarantee explicit.

For right-side views or per-level summaries, reuse the layer invariant with the appropriate action. BFS is not a substitute for a bottom-up child-result contract when a parent's answer needs completed descendants.

**Self-check Q13:** Why must levelSize be captured before processing the level?

**Self-check Q14:** How can a queue exceed the maximum size of one level yet still use O(w) auxiliary space?

## 9. Recursive Contracts and Subtree Aggregation

### 9.1 Ask what information the parent actually needs

When a parent needs facts about both children, postorder timing is natural: obtain left facts, obtain right facts, then combine. No result field should be justified merely because it appeared in a familiar template.

| Requested fact | Null-subtree meaning | Left/right information | Current-node combination |
| --- | --- | --- | --- |
| Size | 0 nodes | Child sizes | 1+leftSize+rightSize |
| Node-height | 0 | Child heights | 1+max(leftHeight,rightHeight) |
| Leaf count | 0 | Child leaf counts | If current is a leaf, 1; otherwise leftLeaves+rightLeaves |
| Maximum value | No value present | Child maxima when present | Maximum of current value and present child maxima |
| Height-balanced? | True, height 0 | Child heights and balanced flags | Both balanced and height difference at most 1 |
| Diameter in edges | 0 | Child heights and child diameters | Best of a path through current and either child's internal best |

The null answers follow the operation's meaning. Zero is not a valid universal maximum for arbitrary int values: a tree containing only −8 would otherwise “find” a value absent from the tree.

### 9.2 A compound result avoids shared global fields

The following original scan returns several fixed-size facts at once. The internal null record uses max=MIN as a neutral value for integer maximum, **paired with size=0 to mean absence**. A nonempty subtree whose true maximum is MIN is distinguished by its nonzero size. The public maximum query returns OptionalInt to expose absence safely.

```java
record Facts(int size, int height, int leaves, int max) {}

static Facts subtreeFacts(TreeNode node) {
    if (node == null) {
        return new Facts(0, 0, 0, Integer.MIN_VALUE);
    }
    Facts left = subtreeFacts(node.left);
    Facts right = subtreeFacts(node.right);
    int size = 1 + left.size() + right.size();
    int height = 1 + Math.max(left.height(), right.height());
    int leaves = node.left == null && node.right == null
            ? 1 : left.leaves() + right.leaves();
    int maximum = Math.max(node.val, Math.max(left.max(), right.max()));
    return new Facts(size, height, leaves, maximum);
}

static OptionalInt maximumValue(TreeNode root) {
    Facts facts = subtreeFacts(root);
    return facts.size() == 0
            ? OptionalInt.empty() : OptionalInt.of(facts.max());
}
```

**Promise:** subtreeFacts(node) describes exactly the reachable nodes in that subtree; it changes no node. Null facts describe no nodes, height zero, and no leaves. The max field's empty interpretation is internal and must be read together with size.

**Correctness by fields:**

- Child node sets are disjoint, so adding their sizes plus the current node counts every node once.
- A longest downward route chooses the taller child and includes the current node.
- A current leaf contributes one leaf. A nonleaf contributes no new leaf identity; its leaves are exactly those in its child subtrees.
- Every value is either the current value or a child's value. Taking those maxima gives the full maximum; MIN is a neutral max operand for any int.

The conjunction of these field arguments establishes the record's whole contract. Each child is smaller, proving termination. A record makes the method's returned meaning explicit and avoids stale size/height/max fields surviving across requests.

### 9.3 Bottom-up facts on Tree A

Rows are ordered by actual nonnull completion, after both child facts are available. Each null child supplies Facts(0,0,0,MIN).

| Completed node | Left size / right size | Returned size | Returned height | Returned leaves | Returned max |
| --- | --- | --- | --- | --- | --- |
| D(2) | 0 / 0 | 1 | 1 | 1 | 2 |
| E(7) | 0 / 0 | 1 | 1 | 1 | 7 |
| B(5) | 1 / 1 | 3 | 2 | 2 | 7 |
| F(20) | 0 / 0 | 1 | 1 | 1 | 20 |
| C(15) | 0 / 1 | 2 | 2 | 1 | 20 |
| A(10) | 3 / 2 | 6 | 3 | 3 | 20 |

| Input | Facts | Public maximum |
| --- | --- | --- |
| null | (0,0,0,MIN) | Empty OptionalInt |
| Single node −8 | (1,1,1,−8) | Present −8 |
| Single node MIN | (1,1,1,MIN) | Present MIN |
| Tree A | (6,3,3,20) | Present 20 |

With constant work per call, this is Θ(n+1) time. There are O(h+1) frames and O(h+1) simultaneously retained fixed-size result records under the source-level model: completed left results wait while right children run. Total record allocations across the execution are O(n+1), which is different from peak live working records. The final returned record/OptionalInt has O(1) output size.

If only one fact is needed, a smaller scalar contract such as heightNodes may be clearer and allocate fewer objects. Compound results are useful when several fields support the current computation, not a requirement to compute every possible statistic for every query.

### 9.4 Returned information versus shared answers

| State style | Meaning | Benefits | Risks / costs |
| --- | --- | --- | --- |
| Scalar return | One exact subtree fact | Compact contract | May not carry enough information for a richer parent decision |
| Compound return | Several related subtree facts | Explicit ownership and composable proofs | Fixed-size records allocate unless optimized; include live intermediates |
| Per-invocation mutable accumulator | Shared object created by one public call | Convenient for output collection or a cross-subtree best | Must define mutation order/ownership and what the helper still returns |
| Static / persistent field | State outlives one call | Sometimes used in short judge code | Stale results, reentrancy/concurrency problems, hidden dependencies |

A method may legitimately return height while updating a separate best diameter. Those are different promises and both need documentation. Returning both in one record is another design, shown next. Do not treat the field's global best as though it were the branch value returned to a parent.

**Self-check Q15:** What does the null maximum field mean in Facts, and how is a real node with value MIN distinguished?

**Self-check Q16:** Why can a scan allocate O(n) result records in total while retaining only O(h) of them simultaneously?

## 10. Height, Balance, and Diameter

### 10.1 Height is a downward fact; depth is inherited context

heightNodes(node) asks how far the subtree extends **below** node. The node's edge depth asks how far it lies **from the original root**. Moving the same subtree under a different parent changes depths but preserves its internal heights.

A height helper returns child-derived information upward. A depth-aware traversal instead passes the current depth down, incrementing it at each edge. Both directions of information are useful, but their contracts are different.

### 10.2 Height balance needs local and recursive checks

Checking just the root's height difference ignores unbalanced descendants. Return the child height and whether **every** node in that subtree satisfies the local condition.

```java
record Balance(int height, boolean balanced) {}

static Balance balanceReport(TreeNode node) {
    if (node == null) {
        return new Balance(0, true);
    }
    Balance left = balanceReport(node.left);
    Balance right = balanceReport(node.right);
    int height = 1 + Math.max(left.height(), right.height());
    boolean balanced = left.balanced() && right.balanced()
            && Math.abs(left.height() - right.height()) <= 1;
    return new Balance(height, balanced);
}

static boolean isHeightBalanced(TreeNode root) {
    return balanceReport(root).balanced();
}
```

The bound on node count keeps height subtraction within int. The base is a balanced empty tree of height zero. If both children are balanced and the current height difference is at most one, every node in the full subtree meets the condition; if any of those tests fails, the full subtree is unbalanced.

| Node completed | Left height | Right height | Returned height | Local difference | Whole subtree balanced? |
| --- | --- | --- | --- | --- | --- |
| D | 0 | 0 | 1 | 0 | true |
| E | 0 | 0 | 1 | 0 | true |
| B | 1 | 1 | 2 | 0 | true |
| F | 0 | 0 | 1 | 0 | true |
| C | 0 | 1 | 2 | 1 | true |
| A | 2 | 2 | 3 | 0 | true |

A three-node one-sided chain returns height 3 and false; its root child heights are 2 and 0. Two equally tall children do not establish balance if one child's own balanced flag is false.

This version always obtains both reports and computes each subtree once: Θ(n+1) time, O(h+1) stack and peak fixed-size working records, O(1) output. A sentinel-return variant can stop earlier, but must preserve an unambiguous contract separating “height” from “unbalanced.”

### 10.3 Diameter: a path may use both child branches

A root-to-leaf route starts at the root and follows children. A root-to-node route may stop at an internal node. A general path between two nodes may climb to their shared ancestor and descend another branch.

The **diameter** is the greatest edge length of any simple path. It need not pass through the original root. Under our empty/singleton convention, both have diameter zero.

At a current node, a longest path in its subtree falls into one of three cases:

1. It lies entirely inside the left subtree.
2. It lies entirely inside the right subtree.
3. It passes through the current node, using at most one downward branch on either side.

For case 3, child node-heights L and R give **L+R edges**. A left child route containing L nodes is L edges away from the current node: one connecting edge plus L−1 edges within that child route. An absent side has contribution zero.

The parent needs two pieces of information: the subtree's height for possible extension, and its best internal diameter. Return both.

```java
record Shape(int heightNodes, int diameterEdges) {}

static Shape analyzeShape(TreeNode node) {
    if (node == null) {
        return new Shape(0, 0);
    }
    Shape left = analyzeShape(node.left);
    Shape right = analyzeShape(node.right);
    int height = 1 + Math.max(left.heightNodes(), right.heightNodes());
    int throughCurrent = left.heightNodes() + right.heightNodes();
    int diameter = Math.max(throughCurrent,
            Math.max(left.diameterEdges(), right.diameterEdges()));
    return new Shape(height, diameter);
}
```

**Promise:** analyzeShape returns the longest downward node-height and longest internal edge-path for exactly this subtree. Neither field is a shared global. Empty supplies (0,0). The height argument proves the first field; the exhaustive three-case path classification proves the second. Smaller child subtrees establish termination.

### 10.4 Diameter dry run

Child pairs are (node-height,edge-diameter).

| Completed node | Left pair | Right pair | Through-current edges | Returned height | Returned diameter |
| --- | --- | --- | --- | --- | --- |
| D | (0,0) | (0,0) | 0 | 1 | 0 |
| E | (0,0) | (0,0) | 0 | 1 | 0 |
| B | (1,0) | (1,0) | 2 | 2 | 2 |
| F | (0,0) | (0,0) | 0 | 1 | 0 |
| C | (0,0) | (1,0) | 1 | 2 | 1 |
| A | (2,2) | (2,1) | 4 | 3 | 4 |

Tree A's diameter can be D→B→A→C→F, four edges. Its height is three nodes. Returning four as the subtree height would corrupt a parent's use of the result.

A diameter can remain below the root. Consider a root with one child whose own left and right arms are each three edges long. That child's internal diameter is six edges, while the longest path using the original root is only four. Taking only the root's through-current candidate would miss the answer.

Time is Θ(n+1), auxiliary stack/live fixed-size reports O(h+1), output O(1). This fulfills the roadmap's height-and-diameter-in-one-pass drill.

### 10.5 Recomputing heights changes the running time

A straightforward baseline computes a through-current candidate by rescanning both child heights, then separately computes child diameters. It is correct but repeats work.

```java
static int diameterWithRepeatedHeights(TreeNode node) {
    if (node == null) {
        return 0;
    }
    int through = heightNodes(node.left) + heightNodes(node.right);
    int leftDiameter = diameterWithRepeatedHeights(node.left);
    int rightDiameter = diameterWithRepeatedHeights(node.right);
    return Math.max(through, Math.max(leftDiameter, rightDiameter));
}
```

On an n-node chain, successive diameter frames rescan child subtrees of roughly n−1,n−2,...,0 nodes. Total height work sums to Θ(n²), although a single height scan is linear. Peak stack remains O(h+1): a height scan runs while some diameter frames are suspended, but it does not keep every scan's entire stack simultaneously.

| Design | Child work | Time | Auxiliary space | Returned information |
| --- | --- | --- | --- | --- |
| Repeated height scans | Revisit descendants at many ancestors | O(n²) worst case; Θ(n²) on a chain | O(h+1) | Diameter only |
| Single postorder Shape | Reuse each child's fixed-size report | Θ(n+1) | O(h+1) | Height and diameter |
| Persistent global best | Depends on helper design | Can be linear when computed once per node | O(h+1) | Height plus separate mutable answer |

For a perfect tree, repeated scans instead sum to Θ(n log n). The shape and actual repeated work matter. Do not label every method with nested recursion “quadratic,” or every tree method “n log n,” without deriving the work.

**Self-check Q17:** Why is the through-current diameter L+R when child heights are measured in nodes?

**Self-check Q18:** Why can a subtree's best internal path not always be returned unchanged as its extendable height?


## 11. Binary Search Trees and Ordered Queries

### 11.1 The invariant describes entire subtrees

In this guide, a **strict BST** satisfies:

- Every key in a node's left subtree is strictly less than that node's key.
- Every key in its right subtree is strictly greater.
- Both child subtrees satisfy the same property.

Duplicate keys are therefore forbidden. Ordinary binary trees elsewhere in the guide still allow duplicate values. A different BST API may store duplicates using counts, allow them on a specific side, or order richer records by a comparator. Those policies require corresponding search/validation contracts; do not mix them with strict comparisons accidentally.

A BST does not sort its memory layout or make nodes indexable. It supplies an ordering proof for eliminating a whole child subtree.

| Property | Arbitrary binary tree | Strict BST |
| --- | --- | --- |
| Child slots | Left and right | Left and right |
| Values | Any order, duplicates permitted | Entire-subtree strict ordering, unique keys |
| Find arbitrary value | May require all nodes | Follow one ordered route |
| Inorder | A structural order | Strictly increasing keys |
| Height | Can be n | Can still be n |
| Search time | O(n) worst case | O(h+1), logarithmic only with a height guarantee |

### 11.2 Search by eliminating an impossible subtree

If target is less than the current key, every key in the right subtree is too large. Search only the left. If greater, search only the right. Equality returns the existing node identity.

```java
static TreeNode searchBst(TreeNode root, int target) {
    TreeNode current = root;
    while (current != null) {
        if (current.val == target) {
            return current;
        }
        current = target < current.val ? current.left : current.right;
    }
    return null;
}
```

**Invariant:** if a matching key exists and has not been returned, it is in the subtree rooted at current. The ordering rule justifies every discarded region. On a miss, current eventually becomes null, proving absence. No node values/links are changed, and no nodes are allocated.

| Tree A query | Inspected keys | Result |
| --- | --- | --- |
| target=7 | 10→5→7 | Existing E |
| target=20 | 10→15→20 | Existing F |
| target=12 | 10→15→null-left | null |
| empty tree, any target | None | null |

Time is O(h+1), auxiliary space O(1). A recursive search can use the same invariant but retains O(h+1) Java frames; tail position does not guarantee their removal.

Balanced BST search is O(log(n+1)); searching a chain can be O(n). Sorted insertion into an ordinary unbalanced BST can create such a chain. Balance is supplied by a separate maintenance invariant, not by the word “search.”

### 11.3 Minimum, maximum, predecessor, and successor

The smallest key in a strict BST is at its leftmost node: any left child is smaller, and without a left child no smaller key exists in that subtree. The rightmost argument is symmetric.

Predecessor/successor queries here concern a **key boundary**, whether or not that key is present:

- predecessor: greatest stored key strictly less than key;
- successor: least stored key strictly greater than key.

Return null when no candidate exists. This differs from asking for the previous/next identity under some duplicate-node policy.

```java
static TreeNode minimumBstNode(TreeNode root) {
    if (root == null) {
        return null;
    }
    while (root.left != null) {
        root = root.left;
    }
    return root;
}

static TreeNode maximumBstNode(TreeNode root) {
    if (root == null) {
        return null;
    }
    while (root.right != null) {
        root = root.right;
    }
    return root;
}

static TreeNode predecessorKey(TreeNode root, int key) {
    TreeNode candidate = null;
    while (root != null) {
        if (root.val < key) {
            candidate = root;
            root = root.right;
        } else {
            root = root.left;
        }
    }
    return candidate;
}

static TreeNode successorKey(TreeNode root, int key) {
    TreeNode candidate = null;
    while (root != null) {
        if (root.val > key) {
            candidate = root;
            root = root.left;
        } else {
            root = root.right;
        }
    }
    return candidate;
}
```

For predecessor, candidate is the greatest qualifying key found so far. When current is below key, its left subtree cannot improve on current, but its right subtree might; retain current and move right. Otherwise current/right are not qualifying, so move left. The successor argument reverses the inequalities/directions.

Tree A has minimum D(2), maximum F(20). For key=10, predecessor is E(7) and successor C(15). For absent key=9, predecessor is E(7) and successor A(10). The predecessor of 2 and successor of 20 are null.

Each query follows one route: O(h+1) time, O(1) auxiliary space, no input mutation. Compare this boundary reasoning with [Binary Search predecessor/successor concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#8-binary-search-in-other-ordered-structures). A BST route chooses child subtrees rather than array index intervals.

### 11.4 Inorder rank: a useful Additional practice skill

In a strict BST, the first inorder visit is rank 1, the second rank 2, and so on. An explicit inorder stack can stop at the kth visit without materializing all n keys.

Contract: root is a strict BST; return its kth smallest key with **1-based k**. Reject k<=0 or k>n with IllegalArgumentException. Null with positive k is out of rank. Input is read-only.

```java
static int kthSmallest(TreeNode root, int k) {
    if (k <= 0) {
        throw new IllegalArgumentException("k must be positive");
    }
    Deque<TreeNode> stack = new ArrayDeque<>();
    TreeNode current = root;
    int visited = 0;
    while (current != null || !stack.isEmpty()) {
        while (current != null) {
            stack.push(current);
            current = current.left;
        }
        TreeNode node = stack.pop();
        visited++;
        if (visited == k) {
            return node.val;
        }
        current = node.right;
    }
    throw new IllegalArgumentException("k exceeds tree size");
}
```

The inorder invariant supplies increasing keys; a visit count supplies rank. Tree A has kth values 2,5,7,10,15,20 for k=1..6. k=0 and k=7 reject.

For valid k, time is O(h+k): at most k popped nodes and at most h additional pending nodes have been pushed. Auxiliary space is O(h+1), output scalar. Rejecting k>n scans the tree in O(n+1). A node with a unique key is not automatically annotated with its rank.

**Optional extension:** maintaining subtree-size fields can support rank/select along one root route in O(h), but updates must preserve those sizes, and logarithmic time still needs a height guarantee. Do not assume Java TreeMap exposes a general rank/select API or your custom TreeNode has such metadata.

**Self-check Q19:** Why can searchBst discard one entire child subtree, and why does this not prove logarithmic time?

**Self-check Q20:** Why can kthSmallest use O(h+k) time instead of always building a full O(n) inorder output?

## 12. BST Validation with Ancestor Bounds

### 12.1 Immediate-child checks miss ancestor violations

This tree passes every available **immediate** left-smaller/right-larger comparison:

```text
                 A(10)
                /     \
             B(5)     C(15)
                      /   \
                   D(6)   E(20)
```

B<10, C>10, D<15, and E>15 all hold. Nevertheless D is in 10's **right subtree** and must also be greater than 10. Its value 6 violates that inherited condition.

Checking only local child pairs forgets constraints from earlier ancestors. Its inorder output [5,10,6,15,20] also exposes the violation.

### 12.2 Carry the allowed open interval downward

**Contract:** withinBst(node,low,high) returns true exactly when every key in this subtree forms a strict BST **and lies inside the inherited open interval (low,high)**.

If current value v is outside the interval, reject immediately. Otherwise:

- The left child inherits (low,v).
- The right child inherits (v,high).

The ancestor constraint is retained on the side not replaced by v. Null contains no violating keys and is valid.

```java
static boolean isStrictBst(TreeNode root) {
    return withinBst(root, Long.MIN_VALUE, Long.MAX_VALUE);
}

private static boolean withinBst(TreeNode node, long low, long high) {
    if (node == null) {
        return true;
    }
    long value = node.val;
    if (value <= low || value >= high) {
        return false;
    }
    return withinBst(node.left, low, value)
            && withinBst(node.right, value, high);
}
```

Long limits safely surround **all int keys**, including MIN and MAX. The comparisons use open endpoints, so no ±1 arithmetic is needed. Starting with int MIN/MAX as exclusive bounds would reject valid nodes with those exact keys; computing val±1 in int can overflow.

**Correctness:** every key must satisfy all ancestor restrictions. The current check enforces its inherited interval; each child's stricter interval enforces both the new parent constraint and the still-relevant ancestor bound. If both recursive promises hold, every node in the full subtree has the correct ordering. If any fails, the whole tree is invalid. Child subtrees are smaller, proving termination.

### 12.3 Dry run: inherited bounds expose the counterexample

Here −∞/+∞ are readable notation for the chosen long limits, not stored floating-point infinities.

| Check order, nonnull nodes | Key | Inherited open interval | Decision / child intervals |
| --- | --- | --- | --- |
| 1 | 10 | (−∞,+∞) | Pass; left (−∞,10), right (10,+∞) |
| 2 | 5 | (−∞,10) | Pass; both null children valid |
| 3 | 15 | (10,+∞) | Pass; left (10,15), right (15,+∞) |
| 4 | 6 | (10,15) | Fail: not greater than 10 |

Because Java && short-circuits, the 20 subtree is not checked after 6 causes failure. A trace should represent that actual control flow, not an unconditional full traversal.

On valid Tree A:

| Key | Inherited interval | Result of own key check |
| --- | --- | --- |
| 10 | (−∞,+∞) | Pass |
| 5 | (−∞,10) | Pass |
| 2 | (−∞,5) | Pass |
| 7 | (5,10) | Pass |
| 15 | (10,+∞) | Pass |
| 20 | (15,+∞) | Pass |

The full method returns true. A duplicate equal to any restrictive bound is rejected, even if it is not the immediate child of that ancestor.

| Input | Strict validation result |
| --- | --- |
| null | true |
| Single MIN or single MAX | true |
| MIN root with MAX as right child | true |
| 10 root with another 10 below either side | false |
| Counterexample with right-subtree 6 | false |
| Tree A | true |

Worst-case time is O(n+1), auxiliary stack O(h+1), output scalar. Early failure may do less work; successful validation visits all nodes. No hashing is used, so no expected-time hash assumption is needed.

### 12.4 Inorder validation is another view of the invariant

A strict BST's inorder values must be strictly increasing. Conversely, for an ordinary binary tree, a strictly increasing inorder sequence ensures each root lies between every value in its left/right subtrees; applying this recursively establishes strict BST order.

An iterative validator can retain only the previous visited value and reject a nonincrease. A nullable previous value or a hasPrevious flag handles the first key safely; initializing previous=MIN and immediately requiring greater would reject a first MIN. Materializing the entire traversal is unnecessary.

Inherited bounds often make the **reason** for invalidity clearer and generalize naturally to ancestor-dependent rules. Inorder streaming is useful when you already have the traversal machinery and need to avoid recursive depth. Match the duplicate policy in either approach.

**Self-check Q21:** Why does checking only each parent-child pair accept the counterexample?

**Self-check Q22:** Why are long open bounds safer than int MIN/MAX or arithmetic adjustments to the key?

## 13. Tree Paths and Returned State

### 13.1 Define which paths count

A root-to-node path may end at an internal node. A root-to-leaf path must end at a node with no children. A general path between two nodes may pass through their common ancestor and is not required to include the original root.

In an ordinary tree, the unique-root-route property prevents ambiguous inherited prefix state. In a shared-child graph, the same node could receive different prefixes through different routes, requiring a different contract.

For Tree A:

| Root-to-leaf path | Node values | Sum as long | Edge length |
| --- | --- | --- | --- |
| A→B→D | 10,5,2 | 17 | 2 |
| A→B→E | 10,5,7 | 22 | 2 |
| A→C→F | 10,15,20 | 45 | 2 |

The prefix A alone sums to 10 but is not a root-to-leaf path. Target 10 therefore does not succeed merely because the root matches.

### 13.2 Inherited prefix state: a root-to-leaf total

**Contract:** return true iff at least one actual root-to-leaf path sums to target. Empty input has no such path and returns false, including target=0. Any long target is permitted. Input is read-only.

Carry a long prefix total rather than subtracting arbitrary targets; the node-count/value bounds guarantee every accumulated path sum fits long, even if target itself is Long.MIN_VALUE or Long.MAX_VALUE.

```java
static boolean hasRootLeafTotal(TreeNode root, long target) {
    return rootLeafFrom(root, 0L, target);
}

private static boolean rootLeafFrom(TreeNode node, long prefix, long target) {
    if (node == null) {
        return false;
    }
    long total = prefix + node.val;
    if (node.left == null && node.right == null) {
        return total == target;
    }
    return rootLeafFrom(node.left, total, target)
            || rootLeafFrom(node.right, total, target);
}
```

**Helper promise:** prefix is the sum of the ancestors before node; return whether a leaf below node completes that prefix to target. Adding node.val establishes the prefix for either child. A leaf is the only place a completed candidate is compared. An absent child cannot supply a fictitious ending.

| Input / target | Result | Reason |
| --- | --- | --- |
| Tree A / 22 | true | A→B→E |
| Tree A / 10 | false | Root is not a leaf; none of the leaf totals is 10 |
| Tree A / 45 | true | A→C→F |
| null / 0 | false | No path exists |
| Single MIN / −2147483648L | true | One actual leaf path |
| Root −4 with child 2 / −2 | true | Negative prefix is valid |

Smaller child subtrees ensure termination. Worst-case time is O(n+1), stack O(h+1); || may stop after the first success. A negative prefix does not justify pruning under arbitrary signed values: later values can increase or decrease the sum.

The inherited state is copied as a primitive into each invocation. Updating one frame's total does not overwrite a sibling's prefix. The same distinction supports ancestor maximum constraints, depth labels, and other root-route properties.

### 13.3 Extendable state versus a complete internal answer

Diameter already required two different result meanings:

- **Extendable contribution:** one downward branch starting at the current node, usable by a parent.
- **Complete internal answer:** a path whose endpoints may lie in two different child subtrees.

A simple path cannot fork. If the parent tries to attach to a child result that already uses both of that child's branches, the shared child would have three incident path edges. That is not one simple path.

```text
           parent
              |
           current
           /     \
      left arm   right arm

usable by parent: current + at most one arm
complete candidate: left arm + current + right arm
```

For Tree A's root, the longest extendable downward route contains three nodes, while its best internal diameter contains four edges. Units differ, and so do the shape constraints. Even if both used the same units, they would still be different promises.

A compound report can return both facts. A helper with a per-invocation shared answer can instead return only the extendable fact and update the internal best separately. Document the distinction whichever implementation style you select; avoid persistent fields unless their reset/lifetime is explicit.

### 13.4 Optional bridge to Maximum Path Sum

A value-weighted path introduces decisions that edge diameter does not have. With current value −3 and leaf children 4 and 6, an extendable route starting at current can have sum 3 by choosing the 6 side; a complete path 4→−3→6 has sum 7. The complete path cannot be passed upward unchanged as one downward branch.

Negative branches may be worse than omitting that branch, while the required answer is usually a **nonempty** path. An all-negative tree must therefore still choose an actual node rather than invent an empty zero-sum answer. Under this guide's broader int-value domain, use long for path totals.

This is preparation for the roadmap's optional Hard problem, not a full implementation. First be reliable with height/diameter, complete versus extendable state, signed numeric bounds, and state ownership.

**Self-check Q23:** Why does an absent child not count as a leaf ending in hasRootLeafTotal?

**Self-check Q24:** Why would returning a two-arm complete path as a parent's branch violate a simple-path contract?


## 14. Reconstruction from Preorder and Inorder

### 14.1 Two traversals contribute different information

For a nonempty subtree:

- Its **preorder** segment starts with its root.
- The root's position in its **inorder** segment separates all left-subtree values from all right-subtree values.
- The left segment's length tells us how many following preorder entries belong to the left subtree.

With unique values, the root has one unambiguous inorder position. Once the two child ranges are determined, apply the same promise to each. Reconstruction does not require the values to form a BST; inorder is used for **structural partitioning**, not sorted lookup.

Use this original example, **Tree B**:

```text
                  12
                 /  \
                4    19
               / \   /
              2   8 16

preorder = [12,4,2,8,19,16]
inorder  = [2,4,8,12,16,19]
```

Root 12 divides inorder after three left values. Therefore the next three preorder values [4,2,8] describe the left subtree, and [19,16] describe the right.

### 14.2 A contract based on ranges

Let buildRange(pre,preStart,inLo,inHi,index) return a fresh tree whose inorder values occupy **[inLo,inHi)** and whose preorder segment starts at preStart with the same length. Empty length returns null without reading pre[preStart].

For a nonempty range:

```text
rootValue = pre[preStart]
pivot = index[rootValue]
leftSize = pivot - inLo

left:
    preorder starts at preStart+1
    inorder range [inLo,pivot)

right:
    preorder starts at preStart+1+leftSize
    inorder range [pivot+1,inHi)
```

Ranges avoid creating new subarrays. A map locates each root's inorder position in expected constant time instead of rescanning its range.

### 14.3 Java implementation and rejection rules

Contract: preorder/inorder are nonnull int arrays of length 0..100000. Return a fresh ordinary binary tree realizing both traversals; empty arrays return null. Reject unequal lengths, duplicate inorder values, missing values, duplicate preorder uses, or structurally inconsistent orderings with IllegalArgumentException. Inputs are read-only. Values may be arbitrary ints; no BST assumption applies.

```java
static TreeNode buildFromPreIn(int[] preorderValues, int[] inorderValues) {
    if (preorderValues.length != inorderValues.length) {
        throw new IllegalArgumentException("traversal lengths differ");
    }
    Map<Integer, Integer> index = new HashMap<>();
    for (int i = 0; i < inorderValues.length; i++) {
        if (index.put(inorderValues[i], i) != null) {
            throw new IllegalArgumentException("inorder values must be unique");
        }
    }
    return buildRange(preorderValues, 0, 0, inorderValues.length, index);
}

private static TreeNode buildRange(
        int[] preorderValues, int preStart,
        int inLo, int inHi, Map<Integer, Integer> index) {
    if (inLo == inHi) {
        return null;
    }
    int value = preorderValues[preStart];
    Integer pivotBox = index.get(value);
    if (pivotBox == null || pivotBox < inLo || pivotBox >= inHi) {
        throw new IllegalArgumentException("inconsistent traversal ranges");
    }
    int pivot = pivotBox;
    int leftSize = pivot - inLo;
    TreeNode node = new TreeNode(value);
    node.left = buildRange(preorderValues, preStart + 1,
            inLo, pivot, index);
    node.right = buildRange(preorderValues, preStart + 1 + leftSize,
            pivot + 1, inHi, index);
    return node;
}
```

The wrapper establishes equal total lengths. Every nonempty valid range consumes one root and partitions the remaining length between children, so derived preorder segments stay within the original array. A base may have preStart==length; it returns before indexing.

A lookup must be checked **before unboxing**: a missing value maps to null. A present pivot outside the current inorder range means the proposed root belongs elsewhere and the traversal pair is inconsistent.

Why does this also reject duplicate preorder values? Each accepted pivot is assigned to exactly one root, and all descendant/sibling inorder ranges exclude that consumed position. Reusing that value later necessarily proposes its old pivot in a range that no longer contains it. No persistent preorder cursor or extra duplicate-set is needed.

If an invalid pair fails after allocating some output nodes, it still leaves both input arrays unchanged; no partially built tree is returned. Those allocations are temporary working objects on the failing path.

### 14.4 Full range-entry dry run for Tree B

The map is {2→0,4→1,8→2,12→3,16→4,19→5}. The following table lists helper entries in actual order, including empty bases.

| Call | preStart | Inorder range | Root value / base | Pivot | Child preorder starts, left/right |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | [0,6) | 12 | 3 | 1 / 4 |
| 2 | 1 | [0,3) | 4 | 1 | 2 / 3 |
| 3 | 2 | [0,1) | 2 | 0 | 3 / 3 |
| 4 | 3 | [0,0) | Empty | — | — |
| 5 | 3 | [1,1) | Empty | — | — |
| 6 | 3 | [2,3) | 8 | 2 | 4 / 4 |
| 7 | 4 | [2,2) | Empty | — | — |
| 8 | 4 | [3,3) | Empty | — | — |
| 9 | 4 | [4,6) | 19 | 5 | 5 / 6 |
| 10 | 5 | [4,5) | 16 | 4 | 6 / 6 |
| 11 | 6 | [4,4) | Empty | — | — |
| 12 | 6 | [5,5) | Empty | — | — |
| 13 | 6 | [6,6) | Empty | — | — |

When call 3 returns, node 2 has null children. Node 8 then completes, letting node 4 attach both fresh children. Node 16 completes before 19 attaches it on the left and records a null right child. Finally 12 attaches the two completed subtree roots.

The reconstructed traversals are:

```text
preorder:  [12,4,2,8,19,16]
inorder:   [2,4,8,12,16,19]
postorder: [2,8,4,16,19,12]
```

Helper calls=13, maximum helper frames=4 for this height-three tree, plus the public wrapper.

### 14.5 Why the reconstruction is correct and unique

The first preorder value must be the root. Unique inorder values give its only possible pivot, forcing exactly which values belong on each side. Their counts force the child preorder segments. Assuming recursive children satisfy their range contracts, attaching them under this root yields both required traversals.

The base represents an empty segment. Every accepted child range is shorter, proving termination. Repeating the forced-root/forced-partition argument establishes uniqueness under the unique-value assumption.

Preorder alone does not generally determine an arbitrary binary tree:

```text
preorder [1,2] can mean:

    1              1
   /                \
  2                  2
```

Their inorder outputs differ. Inorder alone is also insufficient. Duplicate values remove the simple value→single-index guarantee; preorder=[7,7] and inorder=[7,7] can describe a left-child or right-child shape. The code deliberately rejects that domain. More specialized reconstruction under other assumptions requires a different specification.

### 14.6 Expected costs and copying tradeoffs

| Design | Root lookup / input representation | Time | Auxiliary space, excluding fresh output |
| --- | --- | --- | --- |
| Linear lookup in each inorder range | Scan current range; pass indices | O(n²) worst case | O(h+1) |
| Value→index map | Expected O(1) lookup; pass indices | Expected O(n+1) | O(n+1) map plus O(h+1) stack |
| Repeated subarray copies | Lookup may be fast, but copied ranges add work/storage | Shape-dependent; can become quadratic | Include retained copies as well as stack/map |

For a left-skewed tree with ascending inorder and descending preorder, linear lookup finds each root near the end of its remaining range: n+(n−1)+...+1 work. An index map removes those rescans. Properly dispersed fixed-width HashMap keys support the expected lookup bound; do not claim every hashing implementation gives guaranteed constant time.

The output itself contains n fresh nodes, O(n+1) storage. The map is **auxiliary**, not output. A balanced output has O(log n) recursive depth; a skewed output can need O(n) frames even though total expected work remains linear.

**Self-check Q25:** Which traversal identifies the root, and how is the right subtree's preorder start derived?

**Self-check Q26:** Why do unique values and range checks matter, and why does preorder alone leave shape ambiguity?

## 15. Time, Height, Width, and Complete Space

### 15.1 Keep five quantities separate

Define n as distinct reachable nonnull nodes, h as node-height, and w as maximum actual level width. Also distinguish:

1. Total invocations/node processing over the whole run.
2. Nonrecursive work at each invocation.
3. Maximum simultaneous Java frames or explicit entries.
4. Other retained working objects, such as maps/reports.
5. Required output storage.

A reference to a subtree is one reference, not a copy of all its nodes. Conversely, a map containing one entry per node does not vanish from the space bound because its values are small.

For a full recursive traversal with two child calls at every nonnull node:

```text
C(null) = 1
C(node) = 1 + C(left) + C(right) = 2n+1

D(null) = 1
D(node) = 1 + max(D(left),D(right)) = h+1

T(node) = T(left) + T(right) + Θ(1)
```

The actual work recurrence uses actual child sizes. Only special balanced cases justify replacing them with two halves. Because the child node sets are disjoint, summing constant work across all nodes gives Θ(n+1), independent of how unbalanced the tree is.

Space follows a longest live path, so sequential child stack depths combine with **max**, not the sum of both full stacks. Add the parent frame and any completed-child result it retains.

### 15.2 Algorithm cost table

| Actual implementation | Time | Auxiliary space | Required output |
| --- | --- | --- | --- |
| Recursive preorder/inorder/postorder | Θ(n+1) | O(h+1) frames | O(n+1) value list |
| Iterative preorder/inorder here | Θ(n+1), aggregate deque/list work | O(h+1) explicit entries; may be smaller on some shapes | O(n+1) |
| Expanded-marker postorder | Θ(n+1) | O(h+1) live entries; O(n) cumulative entry creation | O(n+1) |
| levelOrder | Θ(n+1) | O(w+1) queue | O(n+1) nested lists |
| minimumLeafDepth | O(n+1) worst case; can stop early | O(w+1) queue | O(1) |
| heightNodes | Θ(n+1) | O(h+1) stack | O(1) |
| subtreeFacts / balanceReport / analyzeShape | Θ(n+1) | O(h+1) frames and fixed-size retained records | O(1) |
| invertTree | Θ(n+1) | O(h+1) stack | Changed input; no fresh data nodes |
| diameterWithRepeatedHeights | O(n²) worst case; Θ(n²) on a chain | O(h+1) combined live frames | O(1) |
| Iterative BST search/extremes/neighbors | O(h+1) | O(1) | Existing node reference / null |
| isStrictBst | O(n+1) worst case | O(h+1) stack | O(1) |
| kthSmallest, valid k | O(h+k) | O(h+1) stack | O(1) |
| hasRootLeafTotal | O(n+1) worst case | O(h+1) stack | O(1) |
| buildFromPreIn | Expected O(n+1) | O(n+1) index map plus O(h+1) stack | O(n+1) fresh nodes |

Pure fixed-work postorder aggregation is deterministic linear time in this model. Reconstruction is qualified as **expected** because it uses hashing. ArrayDeque endpoints and ArrayList appends contribute amortized aggregate bounds; a single resize is not a constant-time worst-case insertion.

### 15.3 Deep versus wide shapes

| Shape | n / h / w relationship | Recursive DFS | Explicit DFS versions | BFS queue |
| --- | --- | --- | --- | --- |
| Chain | h=n, w=1 | Θ(n) frames | Preorder can hold one pending root; inorder depends on left/right orientation; marker postorder can need Θ(n) entries | O(1) |
| Perfect | h=Θ(log n), w=Θ(n) | O(log n) frames | O(log n) pending entries | Θ(n) entries |
| Arbitrary | h and w depend on shape | O(h+1) | O(h+1) upper bound for these designs | O(w+1) |

Output may dominate storage regardless of frontier size. A chain's level-order output has n separate one-element lists even though its working queue holds only one node. A scalar postorder answer does not require an O(n) result list, but its stack still counts.

For an explicit aggregation, storing a result for every node in a map is an O(n) design even if its traversal stack is small. If the goal is O(h) working state, a frame must carry just the staged child results it needs along the current exploration.

### 15.4 Explain repeated work with a concrete count

On an n-node one-sided chain, diameterWithRepeatedHeights makes 2n+1 calls in its own family and n(n+1) calls to heightNodes, counting null height bases. The total logical invocation count is n²+3n+1. That exact example exposes why “each diameter call does only a few lines” misses the cost of its height helper.

| Chain nodes n | Diameter-family calls | Height-family calls | Total calls | True diameter, edges |
| --- | --- | --- | --- | --- |
| 1 | 3 | 2 | 5 | 0 |
| 4 | 9 | 20 | 29 | 3 |
| 8 | 17 | 72 | 89 | 7 |
| 16 | 33 | 272 | 305 | 15 |

The single-pass analyzeShape uses just 2n+1 helper calls. Both versions can have O(h) maximum live frames; total time and simultaneous storage answer different questions.

### 15.5 A practical complexity explanation

In an interview, say what a call promises, how many times nodes are processed, what work combines child results, how tall/wide the structure can be, and which allocations are additional. State worst-case shapes explicitly.

“DFS is O(log n) space” is incomplete without a logarithmic-height assumption. “BFS is O(n) space” is a valid worst-case bound but hides the more informative O(w). “Two recursive calls means exponential” ignores that child subtrees can partition disjoint input. “It is in-place” describes mutation/output allocation and does not automatically exclude recursive frames.

Use [space complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity) and [recursive cost analysis](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) for the prerequisite conventions rather than resetting those meanings for trees.

**Self-check Q27:** Why do complete traversals make 2n+1 calls but need only h+1 helper frames at once?

**Self-check Q28:** For a perfect tree versus a chain, which of recursive DFS or BFS can have the smaller working frontier, and how can output change total storage?

## 16. Java Pitfalls and Demonstrable Failures

The following blocks are **deliberately incorrect subjects**, each against its stated intended contract. They are included to make failures concrete, not as recommended implementations. Test witnesses are small; ordinary traversal preconditions still apply except where an invalid graph is explicitly discussed.

### 16.1 A plausible local BST test is insufficient

Intended contract: validate the whole strict BST.

```java
static boolean badLocalBst(TreeNode node) {
    if (node == null) {
        return true;
    }
    if (node.left != null && node.left.val >= node.val) {
        return false;
    }
    if (node.right != null && node.right.val <= node.val) {
        return false;
    }
    return badLocalBst(node.left) && badLocalBst(node.right);
}
```

The 10/5/15/6/20 counterexample returns true here and false with isStrictBst. The missing information is the inherited interval; adding more immediate child checks does not recover ancestor constraints.

### 16.2 Narrow exclusive endpoints reject valid keys

Intended contract: accept all valid strict BSTs with arbitrary int keys.

```java
static boolean badIntBstBounds(TreeNode root) {
    return badIntWithin(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
}

private static boolean badIntWithin(TreeNode node, int low, int high) {
    if (node == null) {
        return true;
    }
    if (node.val <= low || node.val >= high) {
        return false;
    }
    return badIntWithin(node.left, low, node.val)
            && badIntWithin(node.right, node.val, high);
}
```

A single node MIN or MAX incorrectly returns false. Long surrounding endpoints fix this for int data without modifying key values. Another common fault, computing MAX+1 or MIN−1 in int, wraps before any later assignment/cast can recover it.

### 16.3 Shared BFS lists change previously recorded levels

Intended contract: return an independent list for each level.

```java
static List<List<Integer>> badSharedLevels(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) {
        return levels;
    }
    List<Integer> shared = new ArrayList<>();
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.offerLast(root);
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.pollFirst();
            shared.add(node.val);
            if (node.left != null) queue.offerLast(node.left);
            if (node.right != null) queue.offerLast(node.right);
        }
        levels.add(shared);
    }
    return levels;
}
```

For root 1 with left child 2, the result is [[1,2],[1,2]], and the two entries reference the **same** list. Calling shared.clear() at the next iteration would mutate earlier output too. levelOrder creates a fresh list for every level. Returning a list is returning a reference to an object, not freezing its contents.

### 16.4 A missing child is not a leaf path

Intended contract: minimum root-to-leaf depth in nodes.

```java
static int badMinimumDepth(TreeNode node) {
    if (node == null) {
        return 0;
    }
    return 1 + Math.min(badMinimumDepth(node.left),
            badMinimumDepth(node.right));
}
```

A two-node one-child chain returns 1 instead of 2 because the absent side wins the minimum. The base's numeric zero is valid for an empty whole tree, but combining it as a valid nonempty leaf route changes the contract. minimumLeafDepth avoids that mistake by stopping only at actual leaves.

### 16.5 A best answer that outlives its request

Intended contract: each badPersistentDiameter(root) independently returns that tree's diameter.

```java
static int staleDiameter;

static int badPersistentDiameter(TreeNode root) {
    badDiameterHeight(root);
    return staleDiameter;
}

private static int badDiameterHeight(TreeNode node) {
    if (node == null) {
        return 0;
    }
    int left = badDiameterHeight(node.left);
    int right = badDiameterHeight(node.right);
    staleDiameter = Math.max(staleDiameter, left + right);
    return 1 + Math.max(left, right);
}
```

Starting with staleDiameter=0, calling on Tree A returns 4. A subsequent singleton incorrectly returns 4 instead of 0; empty input also inherits 4. The helper's height calculation is valid, but the public request's answer ownership is not. The Shape return has no such cross-call field dependency. A field-based design must at least initialize its state per request and account for overlapping/reentrant use.

### 16.6 Small null and identity failures

Intended contracts: a root-value query must deliberately handle empty input; queue initialization must accept empty input safely; a same-node check must compare identities.

```java
static int badRootValue(TreeNode root) {
    return root.val;
}

static void badEnqueueRoot(TreeNode root) {
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.offerLast(root);
}

static boolean badSameNode(TreeNode a, TreeNode b) {
    return a != null && b != null && a.val == b.val;
}
```

badRootValue(null) throws NullPointerException. Decide whether the real API should reject empty input explicitly or return an optional value. badEnqueueRoot(null) throws NullPointerException because null is not an allowed ArrayDeque entry; a missing tree is handled before queue initialization. badSameNode accepts two distinct nodes with equal values as though they were one identity; it also mishandles two null handles if null identity equality is part of the contract.

### 16.7 More failure-focused checks

| Mistake | Small witness / consequence | Correct reasoning |
| --- | --- | --- |
| Wrong null answer | Height(null)=1 makes a singleton height 2 | Match the declared height units and empty base |
| Confuse height with size | 1+leftHeight+rightHeight gives 3 on a root with two leaf children, though height is 2 | A downward route chooses one branch |
| Wrong helper return meaning/type | Returning a boolean balance flag loses the child height needed by the parent; an int return cannot express both without a stated encoding | Use a precise compound report or documented sentinel |
| Wrong preorder push order | Root 1 with children 2/3 visits [1,3,2] if left is pushed first | LIFO requires right-before-left scheduling |
| Dynamic layer loop bound | After removing the root and enqueueing two children, queue.size changes during the layer | Snapshot the current level's node count |
| Accidental tree mutation | Assigning node.left=null during a supposedly read-only scan detaches a subtree | State and test link ownership; keep an identity snapshot |
| Aliased child / cycle | A.left=D and A.right=D double-process one identity; A.left=A cannot reach a null base | Ordinary-tree shape is a precondition, not implied by the class |
| Depth unit error | A singleton has edge depth 0 but maximum node-depth/height 1 | Name both quantity and units |
| Diameter unit error | Two linked nodes have diameter 1 edge, not 2 | Child node-heights combine into edge distance |
| Stale previous inorder value | Validating a second tree can be compared against the first tree's last key | Keep previous state local to one request |
| Empty path invented | An all-negative weighted tree incorrectly returns zero | Enforce the nonempty-path requirement |
| Too-deep recursion | A valid long chain can exhaust call stack despite strict progress | Use an iterative traversal or explicit staged aggregation |
| Unsafe sum arithmetic | Two MAX-valued nodes sum to 4294967294, outside int | Add in long; widening an already overflowed int is too late |

A compilation error catches incompatible Java types; it does not catch compatible ints representing the wrong units, a wrong base value, or incorrect shared-state lifetime. Tests should examine input links/identities and repeated public calls in addition to numeric outputs.

**Self-check Q29:** Why does clearing a shared BFS list not repair independent level output?

**Self-check Q30:** Which failures in this section can pass ordinary positive, nonempty, single-call examples?


## 17. Trees and Alternative Representations

A tree is a relationship model; a linked TreeNode representation is one way to store it. Choose representation and algorithm from the operations required.

| Need | Useful representation | Tradeoff |
| --- | --- | --- |
| Fast access by position | Array / ArrayList | O(1) index access; hierarchy needs an encoding |
| Simple linear succession | Custom linked list | One next route; no inherent branching |
| Hierarchy with explicit child links | TreeNode objects | Natural subtree access and local link changes; no direct ith-node access |
| Fixed complete binary shape | Array with child index arithmetic | Compact layout; sparse/skewed shapes can waste conceptual positions |
| Ordered dynamic key lookup | A balanced search-tree map/set implementation | Ordering and logarithmic guarantees come from its maintained invariant |
| Exact key lookup without order | HashMap | Expected basic lookup cost under hashing assumptions; not an ordered rank structure |
| Cycles or shared reachable identities | Graph representation and visitation rules | Ordinary tree recursion alone is insufficient |

For a zero-based complete-tree array, children are at 2i+1 and 2i+2 when those indices are within length. This encoding is useful for complete shapes such as heaps, but does **not** imply BST ordering. With large index arithmetic, choose a type/bounds check that does not overflow. A skewed conceptual layout can require exponentially large position indices relative to actual nodes; explicit links avoid allocating absent positions.

Java TreeMap is a library sorted-map abstraction. Its internal nodes and balancing policy are not the same interface as this custom TreeNode. Knowing how to call TreeMap does not demonstrate binary-tree traversal, and implementing arbitrary custom nodes does not automatically reproduce TreeMap's guarantees. Reuse [the Maps reference](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) for that abstraction.

### DFS versus BFS is about information and frontier shape

| Question | DFS approach | BFS approach |
| --- | --- | --- |
| Need child reports before parent | Postorder naturally supplies them | Requires delayed processing or stored dependency results |
| Need increasing root-relative depth | Possible with extra depth bookkeeping and full comparison | Layer invariant directly gives this order |
| Need ordered BST keys | Inorder exploits subtree order | Ordinary BFS does not sort keys |
| Very deep narrow input | Recursion can fail; explicit DFS is available | Queue may stay constant-size |
| Very wide shallow input | DFS can keep a small h-dependent frontier | Queue can grow with w |
| Need every traversal value | Output is O(n) regardless of frontier | Output is O(n) regardless of frontier |

A weighted shortest-path problem on a general graph has additional assumptions; tree BFS's shallowest **edge-depth** guarantee does not automatically solve arbitrary weighted distances. Keep later Graphs/Advanced Graphs material separate.

### Recursive state versus global state

Returning local subtree facts often makes correctness and repeated calls easier to reason about. Sharing one fresh output list inside a traversal is also a good fit when the effect is intentionally “append these values.” A persistent mutable best/previous field can hide required initialization.

The design question is **who owns the state and what one invocation promises**, not whether every mutable variable is inherently wrong. A shared accumulator must not be confused with a per-frame primitive, and a returned compound object must not silently contain references to mutable scratch storage that later changes.

**Self-check Q31:** Why does a complete-tree array layout not make the values a BST?

**Self-check Q32:** When is a shared mutable output list appropriate, and when can sharing corrupt independent results?

## 18. Pattern Recognition and Derivation

### 18.1 Five recurring patterns

| Pattern | Recognition clues | State / invariant | Derivation and Java example | Costs and main trap | When it is not the right starting point |
| --- | --- | --- | --- | --- | --- |
| A: postorder aggregation | Parent answer depends on completed child facts | Each return summarizes exactly one subtree | Define child reports, direct null facts, then combine; [subtreeFacts](#9-recursive-contracts-and-subtree-aggregation), [analyzeShape](#10-height-balance-and-diameter) | O(n) fixed-work visits, O(h) live state; repeated rescans can be quadratic | A query can discard a whole ordered region or needs only the shallowest event |
| B: BFS layers | Depth, level output, first matching shallow node | Queue boundary contains one level; during it, current suffix precedes next prefix | Snapshot size and enqueue nonnull children; [levelOrder / minimumLeafDepth](#8-bfs-and-level-order-traversal) | O(n) worst-case work, O(w) queue; dynamic size and null placeholders break invariants | Parent requires completed child aggregates |
| C: inherited state | Legality or result depends on ancestors/root prefix | Call receives constraints/context for its root route | Restrict intervals or extend a prefix; [isStrictBst](#12-bst-validation-with-ancestor-bounds), [hasRootLeafTotal](#13-tree-paths-and-returned-state) | O(n) worst case, O(h) recursion; forgetting older constraints or inventing a missing-child path | Needed information is purely a completed-child summary |
| D: indexed reconstruction | Traversal sequences identify root and subtree groups | Preorder start and inorder range describe the same nodes | Use root pivot and left length; [buildFromPreIn](#14-reconstruction-from-preorder-and-inorder) | Expected O(n) map work, O(n+h) auxiliary; duplicates, inconsistent ranges, and copies matter | Values do not uniquely identify pivots or the supplied traversals are insufficient |
| E: path contributions | Subtree best may use more branches than a parent can extend | Return one extendable branch, also preserve a complete internal best | Separate height from diameter in [Shape](#10-height-balance-and-diameter); compare [path state](#13-tree-paths-and-returned-state) | O(n) with fixed-size reports, O(h) live state; returning a fork upward breaks path semantics | Task asks about arbitrary collections of nodes rather than one path |

Every row represents a derivation, not an instruction to memorize a named template. State exact result units and invalid/empty semantics before choosing an implementation.

### 18.2 A decision sequence for an unfamiliar statement

1. Confirm the input really is an ordinary tree and whether ordering/duplicates are constrained.
2. State the requested object: scalar, value list, existing identity, mutated links, fresh tree, or path.
3. Ask whether the current decision needs **ancestor context**, **child answers**, **level order**, or **ordered elimination**.
4. Define the helper promise before deciding which locals/fields exist.
5. Supply a direct empty/minimal case and check all-negative/extreme-key cases where relevant.
6. Prove each child call's arguments/state are valid and progress occurs.
7. Count actual work and peak frontier; then choose recursion or explicit state for the allowed depth.
8. Test both structure and values. A displayed sequence can hide aliasing, dropped nodes, or stale state.

Some problems combine patterns: BST validation inherits bounds while recursively combining booleans; diameter returns a postorder report with path semantics; reconstruction passes index bounds while returning fresh structures. Classification helps organize obligations but does not replace them.

### 18.3 Recognition mini-scenarios

Try these before opening the concise key in Section 19. They are short design checks, not a separate workbook or platform assignment list.

| Scenario | Decide what information/state is needed |
| --- | --- |
| R1 | Find the first level containing a leaf with a marked value |
| R2 | Report the number of leaves in every child subtree before deciding at a parent |
| R3 | A descendant key must obey constraints imposed several ancestors earlier |
| R4 | Recover a uniquely labeled hierarchy from preorder and inorder arrays |
| R5 | Find the longest path that may join two child branches |
| R6 | Produce ordered keys from a valid strict BST without copying all keys first |
| R7 | Traverse a 100000-node narrow chain while avoiding recursive depth |
| R8 | A purported “tree” has a shared child and a cycle |

For R7, justify memory using the chosen explicit stack/queue design, not merely the word “iterative.” For R8, repair the domain/visitation contract before applying ordinary-tree complexity claims.

**Self-check Q33:** What distinguishes information inherited from ancestors from information returned by child subtrees?

**Self-check Q34:** Why does selecting a familiar traversal still leave work to define the helper contract and complete space bound?

## 19. Conceptual Assessment and Answer Key

Attempt the numbered self-checks from memory first. The concise key supplies checkpoints; it does not replace the diagrams, executable traces, and correctness arguments.

<details>
<summary>Q1–Q8: structure, units, references, and recursive promises</summary>

| Question | Concise answer |
| --- | --- |
| Q1 | C has edge depth 1 and node-height 2; D→F has four edges. These measure different routes and units. |
| Q2 | Reachability plus no cycles and one incoming parent edge per nonroot node gives one root route. Shared D breaks unique incoming reachability. |
| Q3 | Yes. Repeatedly attach one leaf and one continuing internal child; every internal node still has two children while height grows linearly. |
| Q4 | Child heights differ by at most one everywhere, but a gap at C.left precedes C.right. Ordering is a separate value constraint. |
| Q5 | Rebinding changes one local slot. Field assignment changes a shared object's structure, visible through aliases. |
| Q6 | Equal values can belong to different objects. Revisiting one identity on a downward route closes a cycle. |
| Q7 | A downward route uses one child; a size count includes both disjoint child node sets and the root. |
| Q8 | Specify which existing identities remain, which links change, whether new nodes appear, and what object the returned reference identifies. |

</details>

<details>
<summary>Q9–Q18: traversals, queues, aggregation, and paths</summary>

| Question | Concise answer |
| --- | --- |
| Q9 | They place their visit before left, between children, or after right, while keeping the same left-first structural call schedule. |
| Q10 | No. Strict subtree ordering with unique keys gives strictly increasing inorder; other duplicate policies require their own guarantees. |
| Q11 | The last pushed child runs first. Reversing the pushes gives root-right-left traversal. |
| Q12 | It remembers a deferred parent visit after scheduled child work; the node identity alone does not specify that stage. |
| Q13 | Appended children belong to the next level, so a changing size can pull them into the current loop. |
| Q14 | Unprocessed current nodes coexist with some next-level nodes. Their combined count is bounded by a constant multiple of w. |
| Q15 | It is a neutral internal max operand paired with size=0 for absence. A real MIN maximum has nonzero size and a present OptionalInt. |
| Q16 | Finished child frames/temporary reports stop being needed; only records on the active ancestry and retained child results coexist. |
| Q17 | A child route of L nodes is L edges from the current node, including the connecting edge; the two sides add. |
| Q18 | An internal best may join two branches, while a downward contribution can use only one; units may differ as well. |

</details>

<details>
<summary>Q19–Q28: ordered constraints, reconstruction, and costs</summary>

| Question | Concise answer |
| --- | --- |
| Q19 | Subtree ordering disproves all keys on one side. A route can still have n nodes in a skewed BST. |
| Q20 | Stop after k inorder pops; beyond those, at most O(h) ancestors remain pending, so not every key must be emitted. |
| Q21 | They forget the lower bound from root 10 when inspecting 6 under 15. The local pair 6<15 is not sufficient. |
| Q22 | Long limits strictly surround every int key; no int key±1 adjustment can overflow or exclude a legitimate extreme. |
| Q23 | Null is not a node with no children. A successful path must terminate at an actual leaf. |
| Q24 | Attaching the parent to an already two-arm fork creates three incident path edges at that child, violating one simple path. |
| Q25 | Preorder starts with the root; root's inorder pivot gives leftSize, so the right preorder starts at preStart+1+leftSize. |
| Q26 | Unique values give one pivot; range checks reject a pivot that belongs elsewhere. A preorder sequence does not identify which child slots are missing. |
| Q27 | Calls count all child positions over time, including null bases. Depth counts one active root-to-null route, h+1 frames. |
| Q28 | Perfect trees favor h-dependent DFS working storage; chains can favor a constant-width BFS queue. Returned traversal output can still occupy O(n). |

</details>

<details>
<summary>Q29–Q34 and recognition checkpoints</summary>

| Question | Concise answer |
| --- | --- |
| Q29 | All rows still reference the same object, so clearing/editing it changes earlier rows. Create independent level lists. |
| Q30 | Examples include exclusive int bounds, stale request state, absent-child minimum, aliasing, and wrong units; tests must target those assumptions. |
| Q31 | Array index relationships encode shape, not comparisons among stored values. A heap/complete layout is not automatically a BST. |
| Q32 | Sharing within one request is appropriate for deliberate appending; reusing a mutable container across independent rows/requests can change previous results. |
| Q33 | Inherited state describes the ancestor route before processing a node; returned state summarizes completed work inside its subtree. |
| Q34 | A traversal order alone does not specify result meaning, mutation, extra retained objects, output, or depth guarantees. |

R1: BFS layers with a leaf/value condition. R2: postorder child facts. R3: inherited bounds. R4: indexed reconstruction under unique-value assumptions. R5: extendable branch plus internal best. R6: streaming inorder, optionally stopping by rank. R7: choose an iterative route/frontier representation and justify its actual peak entries. R8: ordinary-tree assumptions fail; define a graph/identity visitation policy or reject malformed input.

</details>

For an oral self-assessment, explain a fresh shape rather than quoting Tree A's outputs. Useful interviewer follow-ups include: “What if the root is null?”, “What if keys duplicate?”, “What if the tree is a chain?”, “What survives across repeated calls?”, and “Which returned child information is sufficient for your parent?”

## 20. Key Takeaways and Readiness Evidence

A tree algorithm begins with its structural assumptions and helper promise. Child subtrees provide returned facts; ancestors provide inherited context; a queue preserves levels; an explicit stack preserves chosen continuations. Tree shape determines h/w, not the number of child fields or the presence of recursion.

Use these demonstrations with the roadmap's [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category):

- Explain root/leaf/subtree, node-height, edge depth, and edge diameter on a newly drawn tree.
- Build a custom-node tree from a blank editor and distinguish duplicate values from node identity.
- Derive all three recursive DFS orders and verify their outputs by hand.
- Implement iterative inorder, preorder, staged postorder, and level-order BFS; state the saved-state invariant.
- Derive a subtree report and prove its null base/child combination, including all-negative maximum values.
- Compute height and diameter in one postorder pass without confusing extendable and internal path facts.
- Show why a local BST test fails, then validate with inherited bounds and extreme int keys.
- Explain reconstruction ranges, unique-value assumptions, expected map cost, and fresh output.
- Analyze a chain and a wide tree using n, h, w, including copied data/records/output where relevant.
- Detect shared output lists, stale fields, accidental mutation, and depth limitations on changed examples.
- Derive an unfamiliar structural query independently and compare a recursive and iterative design.

These are evidence expectations, not prechecked progress. Keep concept/drill status, problem attempts, and dated readiness evidence in the [existing Trees roadmap section](./DSA_Interview_Roadmap.md#7-trees). A remembered solution may support a cold retention check but cannot establish unfamiliar transfer.

Use the roadmap's understand→implement→practice→explain→review→validate cycle. Record I/H/S honestly on its existing problem rows and use roughly 1/3/7/14-day reviews when helpful, adjusted to retention. No separate dashboard or revision ledger is introduced here.

Trees prepares the roadmap's Tries, Heap / Priority Queue, and Backtracking branches. Those branches still require their own representations, invariants, and practice. Understanding child contracts does not by itself establish graph-cycle handling, heap operations, mutable-path restoration, or DP state design.

## 21. References, Validation, and Next Steps

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations), [Trees](./DSA_Interview_Roadmap.md#7-trees), [learning and validation](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [independent gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Recursion Essential Concepts](./Recursion/Recursion_Essential_Concepts.md): [contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness), [complete costs](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space), [explicit state](./Recursion/Recursion_Essential_Concepts.md#14-recursion-iteration-and-explicit-stacks).
- [Stack Essential Concepts](./Stack/Stack_Essential_Concepts.md).
- [Linked List Essential Concepts](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity).
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): [space](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity) and [recursion](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity).
- [Java Arrays](./Java_Array.md), [Java Lists](./Java_List_ArrayList_LinkedList.md), and [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md).

Use actual discovered paths above; the recursion references are currently inside the Recursion subdirectory. Existing older documents may have their own path history; this guide's links are validated independently.

### Java 21 primary sources

Java 21 library contracts were checked using Context7, selecting its version-21 official API collection. Only relevant ArrayDeque, ArrayList, and HashMap material was used; unrelated retrieval snippets were disregarded.

- [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html): null rejection, deque behavior, and amortized endpoint costs.
- [Deque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html): LIFO/FIFO method meanings.
- [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html): aggregate append cost.
- [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html): basic-operation cost assumptions and missing-key behavior.
- [OptionalInt](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/OptionalInt.html): explicit presence/absence for a primitive result.
- [JLS 21 method invocation](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.5): fresh parameter values, including reference values.
- [JVMS 21 frames](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.6): local variables and invocation frames.

The [official NeetCode roadmap](https://neetcode.io/roadmap) is the repository roadmap's organizational source. Its interactive page did not expose the dependency diagram in the text retrieval used here; the local roadmap remained authoritative for this task's scope and prerequisites.

### Existing roadmap practice, in its original order

The following titles/numbers/difficulties were checked on official LeetCode pages on **2026-10-28**. E/A/C are copied from the local roadmap's priorities, not invented platform labels. Keep attempts on the roadmap's existing rows; no duplicate tracker is needed.

| Roadmap tier | Problem | Difficulty | Concept to demonstrate |
| --- | --- | --- | --- |
| E | [104. Maximum Depth of Binary Tree](https://leetcode.com/problems/maximum-depth-of-binary-tree/) | Easy | A precise node-height contract |
| E | [226. Invert Binary Tree](https://leetcode.com/problems/invert-binary-tree/) | Easy | Structural mutation and returned roots |
| E | [543. Diameter of Binary Tree](https://leetcode.com/problems/diameter-of-binary-tree/) | Easy | Height versus internal edge path |
| E | [102. Binary Tree Level Order Traversal](https://leetcode.com/problems/binary-tree-level-order-traversal/) | Medium | Layer boundary/FIFO invariant |
| E | [98. Validate Binary Search Tree](https://leetcode.com/problems/validate-binary-search-tree/) | Medium | Ancestor bounds and strict keys |
| A | [230. Kth Smallest Element in a BST](https://leetcode.com/problems/kth-smallest-element-in-a-bst/) | Medium | Inorder rank and early stopping |
| A | [105. Construct Binary Tree from Preorder and Inorder Traversal](https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/) | Medium | Forced root/pivot/range relationships |
| C | [124. Binary Tree Maximum Path Sum](https://leetcode.com/problems/binary-tree-maximum-path-sum/) | Hard | Optional weighted contribution reasoning |

Do not count document ownership as completion of any problem or gate. Platform constraints may be narrower/different than this guide's examples; reread them before coding.

### Author validation

Completed **2026-10-28**. All **28 Java blocks**, containing **44 methods** plus the shared node/record types, were assembled and compiled using Java 21 with `--release 21 -Xlint:all -Werror`.

The executable validation passed **713,304 assertions** across **2,508 tree cases**, including all **626 ordinary binary-tree shapes with up to seven nodes** under multiple value assignments, plus targeted negative/extreme-key, duplicate, empty, narrow, and wide inputs. Independent oracles used structural path ordering, level processing, and pairwise path distances to check traversals, aggregates, balance, diameter, and leaf totals. Reconstruction checks covered fresh identities, unchanged arrays, valid traversal pairs, inconsistent permutations, duplicate uses, and rejection boundaries. Mutation checks inspected every original child link and confirmed that two inversions restore the structure; repeated-call tests reproduced the stale-state and output-alias defects.

Instrumentation checked **555,420 logical invocations** and recorded **14 selected executions**. **156 rows across 21 dry-run tables** were compared with Java call/return events, explicit stack/queue states, and independent value/boundary models. The repeated-height call counts and the width-four/queue-six example were verified. Iterative traversals/BFS and ordered queries handled a **100,000-node chain**; balanced 100,000-node inputs exercised aggregation and reconstruction at logarithmic depth. A one-node cycle reproduced StackOverflowError only in an isolated JVM with a timeout. These checks do not imply that deep recursive chains are safe or that algorithmic live storage equals exact JVM heap bytes.

All **67 local links** were checked against actual files and heading anchors. SHA-256 comparison confirmed all **30 pre-existing guide files** remained unchanged. Only Trees_Essential_Concepts.md was created in the repository; validation artifacts stayed outside it. No supporting prerequisite document was missing, and no unresolved correctness or link issue remained.


