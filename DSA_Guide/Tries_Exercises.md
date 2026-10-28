# Tries — Exercises (Java 21)

Build the structure, explain what each path means, and test the distinction between a prefix and a complete word. This workbook develops independent implementation, debugging, analysis, and interview selection.

## Contents

- [1. Introduction and contracts](#1-introduction-and-contracts)
- [2. Problem-solving workflow](#2-problem-solving-workflow)
- [3. Level 1: Trie fundamentals](#3-level-1-trie-fundamentals)
- [4. Level 2: Core operations](#4-level-2-core-operations)
- [5. Child-storage design workshop](#5-child-storage-design-workshop)
- [6. Level 3: Wildcard DFS](#6-level-3-wildcard-dfs)
- [7. Level 4: Curated platform practice](#7-level-4-curated-platform-practice)
- [8. Level 5: Unfamiliar interview challenges](#8-level-5-unfamiliar-interview-challenges)
- [9. Complexity investigations](#9-complexity-investigations)
- [10. Debugging laboratory](#10-debugging-laboratory)
- [11. Board-search reasoning: optional](#11-board-search-reasoning-optional)
- [12. Interview communication](#12-interview-communication)
- [13. Cold review and spaced repetition](#13-cold-review-and-spaced-repetition)
- [14. Completion and readiness evidence](#14-completion-and-readiness-evidence)
- [15. References and author validation](#15-references-and-author-validation)

## 1. Introduction and contracts

This practical companion to [Tries Essential Concepts](./Tries_Essential_Concepts.md) follows the authoritative [Phase 3 Tries roadmap](./DSA_Interview_Roadmap.md#8-tries). It contains **five levels**, **17 original coding exercises** (11 core drills and 6 unfamiliar challenges), **5 tracing exercises**, **3 representation investigations**, **6 complexity investigations**, **9 debugging challenges**, and **1 optional board investigation**: **41 original exercises**, plus **3 curated LeetCode problems**. Related methods under one ID count once.

Attempt from a blank editor with the statement visible and theory implementations hidden. Read hints only after recording a concrete failed approach. Local Easy/Medium labels describe practice difficulty; they are separate from the roadmap's E/C priorities and platform difficulty. Building these documents has not established mastery.

| Prerequisite demonstration | Reference |
| --- | --- |
| Explain reference slots, independent objects, and field mutation | [Trees: references](./Trees/Trees_Essential_Concepts.md#4-java-treenode-references-and-structure), [Java Arrays](./Java_Array.md) |
| Specify one recursive call and distinguish calls from live frames | [Recursion: contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness), [complete space](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) |
| Explain UTF-16 indexing and the cost of copying strings | [Java Strings](./Java_String.md#indexes-length-and-traversal) |
| Distinguish membership, counts, and key-to-value information | [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model), [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) |
| Separate work, temporary storage, and persistent index growth | [Complexity foundations](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), [trie costs](./Tries_Essential_Concepts.md#11-time-storage-and-branching-complexity) |

### Shared contract profiles

These profiles are part of every coding specification. An explicit local override takes precedence. Constraints are preconditions unless rejection is explicitly required.

**A — lowercase array dictionary.** Keys contain only a..z, case-sensitive, with no normalization. Uppercase, digits, accents, emoji, literal dots, and other characters are invalid. Null String arguments throw NullPointerException; invalid characters throw IllegalArgumentException. Validate the **entire** argument before traversal or mutation, including an invalid suffix behind an absent edge. An invalid insertion must leave all nodes, links, and terminal flags unchanged. This guarantee concerns specified validation failures, not resource exhaustion.

Empty words are allowed. Insertion is idempotent for membership: repeated insertion changes no identities or counts. Exact empty search is true iff the empty word was inserted. Prefix search means **path existence**: the empty prefix is true even on a fresh dictionary because its root exists. Wildcards are unsupported unless profile W is named.

**M — raw UTF-16 map dictionary.** Any sequence of Java char code units is permitted, including uppercase, digits, punctuation, and surrogate code units. No normalization, case folding, or surrogate validation occurs. Null is rejected with NullPointerException. Empty and duplicate policies match A. Dot is a literal character; no wildcard API is supplied for M. A supplementary Unicode code point uses two edges. This is not an advanced Unicode/grapheme implementation.

**W — whole-word patterns over A.** Stored words follow A. Patterns allow a..z and dot only; null throws NullPointerException and every other symbol throws IllegalArgumentException before searching. Dot consumes **exactly one lowercase English letter**. It never consumes zero letters, multiple letters, or an arbitrary regex expression. The whole stored word must have the same length as the pattern. Empty pattern matches iff root is terminal. Consecutive and all-dot patterns are legal unless W1 explicitly restricts the number of dots.

All trie roots are nonnull, finite ordinary insertion-only roots: each nonroot node has one incoming edge, no cycles or shared child identities, and each child container belongs to its own node. Existing inputs are read-only except documented insertion. Do not use static mutable roots, cursors, or result flags. Queries allocate no trie nodes and cannot mark or erase words. Independent wrapper instances own independent roots.

Ordinary recursive exercises use maximum edge depth **H<=64** and **N<=200000**. Assume sufficient stack resources for these modest tests; there is no portable Java safe-depth threshold. Explicit iterative drills may use **L<=100000** and **N<=200001**. Batch total characters are at most 200000. Counts fit int, but aggregate counts use long when requested. Container arguments are nonnull unless their rejection is explicitly tested. No deletion, compression, concurrency, or generic graph validation is required.

Output order is irrelevant for scalars/records. Node results must be existing identities. Ordered arrays/lists follow their local stated order. Several implementations can satisfy a contract; observable results and mutation rules must agree.

| Symbol | Meaning |
| --- | --- |
| L / P | Word or pattern length / prefix length in the stated symbol model |
| S / m | Total characters / number of accepted insertion requests, including duplicates and empty requests |
| N / H | Allocated nodes including root / deepest edge depth |
| b / sigma | Maximum actual child degree / array slots per node, sigma=26 |
| delta | New nodes attached by an insertion; root is not included |
| V / B | Entered nonnull query states / entered nonbase wildcard branching states |
| Z | Required output size, measured in characters or elements as stated |

The concepts guide refines the roadmap's alphabet bound using actual degree b and separate slot count sigma. Keep this distinction when using the roadmap's wildcard shorthand.

Auxiliary space here excludes the existing input and required returned output. It includes recursion, work collections, copies, and transient library allocation. **Persistent trie growth is reported separately and still consumes memory.** Include constant setup for empty inputs; O(L+1) is the precise convention. Hash operations need expected/amortized qualifications.

### Java scratch setup

Use only the Java 21 standard library. All Java blocks in this workbook can be placed as members of one scratch class named TriesWorkbook with import java.util.*; at file scope. TODO methods intentionally throw UnsupportedOperationException. They compile before implementation but are not solved exercises.

First try defining the node classes yourself for F1 and O5; then use this common scaffold:

```java
static final class ArrayNode {
    final ArrayNode[] children = new ArrayNode[26];
    boolean isWord;
}
static final class MapNode {
    final Map<Character, MapNode> children = new HashMap<>();
    boolean isWord;
}
record Inventory(int nodes, int edges, int words, int maxDepth, long slots) {}
```

The reference arrays allocate slots, not child objects. Do not add a static child array. The root's depth is zero, so a fresh root has maxDepth=0.

### Fixture and test notation

D denotes words ["app","apple","apply","bat"]. Q denotes ["cab","cane","cape","dog"]. E denotes a fresh root with no stored word. A dictionary shown in a test means construct it from the listed words in that order. D/Q are setups, never implicit global mutable fixtures.

Expected errors are strings NPE and IAE for NullPointerException and IllegalArgumentException. Strings in JSON cells translate directly to Java literals; \uD83D\uDE00 denotes two UTF-16 units. "a^k" in prose means k repetitions, not a literal caret. Rebuild a fresh setup for each row unless a sequence is shown.

A structural check records each prefix's node identity, terminal flag, and outgoing references. Count nodes independently by enumerating distinct string prefixes in the fixture; do not trust the learner's insertion or inventory method as the only oracle. For no-mutation checks compare identities/flags/edges before and after, not merely successful membership answers. You may use test-only identity maps; that test storage is not the operation's auxiliary-space budget.

### Attempt records

Every exercise and curated problem has one initially unchecked record. Check it after an attempt, tests, and a postmortem; a check does not mean independent success. Use **I** independently solved with correct explanation/tests, **H** conceptual hint, **S** substantial solution review or unfinished attempt, **—** not attempted. Record actual minutes and dates, never prefill success.

Original-exercise histories live beneath their IDs. Curated records link to the roadmap's existing problem Log; keep the detailed history there and use the workbook record as a pointer rather than maintaining two histories. Topic-level [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) and dated readiness evidence remain only in the roadmap.

## 2. Problem-solving workflow

1. Clarify the alphabet, empty/duplicate policy, exact versus prefix versus pattern semantics, and whether the workload needs an index at all.
2. Draw a small dictionary before coding. Label full prefixes and terminal flags, not just the final letters.
3. State the invariant: before position i, which consumed prefix does current represent? For DFS, what does one node/position call promise?
4. Describe a baseline. Explain which repeated work an index avoids and what storage it adds.
5. Derive transitions, the endpoint/base interpretation, and a decreasing measure. Explain why existing words survive insertion and why missing paths reject queries.
6. Implement from the signature. Reuse tested earlier drills if permitted, but rederive the core once from a blank editor for the I gate.
7. Test structure and behavior. Include a short word inside a longer word, both insertion orders, a missing edge, duplicates, empty input, and the applicable rejection policy.
8. Define L,S,m,N,b,sigma,V. Separate character scans, child checks, node allocations, stack depth, output, and persistent storage.
9. Explain an unfamiliar follow-up and record assistance honestly.

| Learning objective | Evidence to produce |
| --- | --- |
| Nodes, labels, shared paths, terminal flags | F1–F3, T1 |
| Correct preserved insertion and validation | O1, O4, T2, D1/D2/D5/D6 |
| Exact versus prefix interpretation | O2/O3, T3, D3/D4 |
| Array/map domain and memory decisions | O5, R1–R3, C4 |
| One-dot then arbitrary-dot DFS | W1/W2, T4/T5, D7–D9 |
| Distinct matching words versus insertion multiplicity | W3 |
| Complete time/storage reasoning | C1–C6 |
| Independent selection and transfer | X1–X6, interview follow-ups |
| Platform practice | P208, P211; P212 is optional |
| Board visitation and restoration | B1, after Backtracking |

Do not consult theory implementations during an independent attempt. Afterward use [structural invariants](./Tries_Essential_Concepts.md#4-structural-and-dictionary-invariants) and [recursive correctness](./Tries_Essential_Concepts.md#10-recursive-contracts-and-correctness) to identify the reasoning gap without copying an entire solution.

## 3. Level 1: Trie fundamentals

Draw before implementing. Use the [prefix-path model](./Tries_Essential_Concepts.md#2-prefix-paths-and-shared-structure); keep its completed example hidden until your own drawing is finished.

### F1. Build a shared-prefix dictionary by hand

**Difficulty:** Easy. **Objective:** Connect distinct objects, share only equal prefixes, and mark complete words.

**Prerequisites:** Java references, array defaults, A.

Define ArrayNode yourself, then draw D and manually construct exactly its representation without calling an insertion routine. A star marks terminal state, never an edge label. Return a fresh root each time.

**Input:** No arguments; the required dictionary is D.

**Output:** An A root representing exactly app, apple, apply, bat.

**Constraints and policies:** A; construction allocates nodes. All unspecified child slots remain null. No node or child array may be shared between separate calls.

```java
static ArrayNode buildD() {
    throw new UnsupportedOperationException("TODO F1");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"call":"buildD"}` | `{"nodes":10,"edges":9,"words":4,"maxDepth":5,"slots":260}` |
| `{"prefixes":["","a","ap","app","appl","apple","apply","b","ba","bat"]}` | `{"terminal":[false,false,false,true,false,true,true,false,false,true]}` |
| `{"calls":2}` | `"disjoint node and child-array identities; identical logical dictionaries"` |

**Test tasks:** Confirm the a node and ba node differ even though their last label is a. Verify app is terminal with a child and appl is nonterminal with two children. Mutating one returned root must not affect another.

**Target time:** O(1) for this fixed fixture (generalize your explanation to O(sigma*N)).

**Target auxiliary space:** O(1) explicit locals.

**Persistent storage / output:** Required output is ten nodes and ten 26-slot child arrays.

**Completion criteria:** Submit your pre-code drawing, exact prefix set, flags, allocation counts, and identity checks. Explain why a 26-slot array does not allocate 26 child nodes.

**Interviewer follow-up:** Which single flag would change if ap were additionally stored?



- [ ] **F1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### F2. Measure a dictionary structure

**Difficulty:** Medium. **Objective:** Inspect nodes independently of the insertion requests that produced them.

**Prerequisites:** F1, finite-tree traversal, recursive contracts.

Return an Inventory of a valid A trie: reachable nodes including root, nonnull edges, terminal nodes including a terminal root, maximum edge depth, and total allocated child-reference slots. Inspect actual structure; no history or stored whole-word list is provided.

**Input:** Nonnull root, A, N<=200000 and H<=64.

**Output:** Inventory(nodes,edges,words,maxDepth,slots).

**Constraints and policies:** Read-only; empty dictionary still has one root. Duplicates have no multiplicity. No result ordering applies. Slots is long; all other fields fit int.

```java
static Inventory inventory(ArrayNode root) {
    throw new UnsupportedOperationException("TODO F2");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":[]}` | `[1,0,0,0,26]` |
| `{"words":[""]}` | `[1,0,1,0,26]` |
| `{"words":["apple","app","apple"]}` | `[6,5,2,5,156]` |
| `{"words":["app","apple","apply","bat"]}` | `[10,9,4,5,260]` |
| `{"words":["a","z",""]}` | `[3,2,3,1,78]` |

**Test tasks:** Add a 64-edge chain and all 26 one-letter words. Check edges=nodes-1 and slots=26*nodes independently; all flags/edges must remain unchanged.

**Target time:** O(sigma*N), or O(N) for sigma=26.

**Target auxiliary space:** O(H+1) with one current DFS route.

**Persistent storage / output:** No index growth; constant-size returned record.

**Completion criteria:** Explain why words, leaves, edges, nodes, and allocated slots are different counts. Verify the root and a terminal internal node.

<details>
<summary>Hint 1</summary>

Which facts belong to the current node before inspecting any children?

</details>

<details>
<summary>Hint 2</summary>

The child contribution must use the child root as its own depth zero or carry an explicit depth consistently.

</details>

<details>
<summary>Hint 3</summary>

Combine scalar child reports; avoid storing every complete prefix String.

</details>

- [ ] **F2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### F3. Return the existing endpoint

**Difficulty:** Easy. **Objective:** Separate path traversal from the interpretation of a terminal flag.

**Prerequisites:** F1, A validation, reference identity.

Follow a fully validated literal text and return its existing endpoint, or null if a needed child is absent. A nonterminal endpoint is still a valid endpoint.

**Input:** Nonnull A root and text, with L<=100000.

**Output:** An existing ArrayNode identity or null.

**Constraints and policies:** A rejection and full-validation policy. Iterative, read-only, no node/array creation. Empty text returns the supplied root.

```java
static ArrayNode endpoint(ArrayNode root, String text) {
    throw new UnsupportedOperationException("TODO F3");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["apple"],"text":"app"}` | `"identity for prefix app, isWord=false"` |
| `{"words":["apple"],"text":"apple"}` | `"identity for prefix apple, isWord=true"` |
| `{"words":["apple"],"text":"az"}` | `null` |
| `{"words":[],"text":""}` | `"supplied root identity"` |
| `{"words":[],"text":"missing#"}` | `"IAE"` |
| `{"words":[],"text":null}` | `"NPE"` |

**Test tasks:** Test a 100000-letter single path without recursion. Save node identities before a failed query; none may be allocated or replaced.

**Target time:** O(L+1), including full prevalidation even on an early miss.

**Target auxiliary space:** O(1).

**Persistent storage / output:** No growth; a single reference is returned.

**Completion criteria:** State the loop invariant at position i and justify both early absence and empty-root behavior.

**Interviewer follow-up:** Why does this method alone not answer exact-word membership?



- [ ] **F3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### T1. Slots, references, and prefix identity

**Difficulty:** Easy. **Objective:** Read a drawing as an object graph.

**Prerequisites:** F1.

On your completed F1 drawing, assign object IDs to all nodes. For prefixes a, ap, app, appl, apple, apply, b, ba, bat, label incoming character indexes and terminal state. Draw only the fragment below as a starting shape, then finish the other branches:

```text
(root: "")
+-- a
|   +-- p
|       +-- p [terminal?]
|           +-- l [terminal?]
|               +-- e [terminal?]
+-- b ...
```

**Test tasks:** Use D, a fresh root, and a root storing only "". Checkpoint for D: 10 node objects, 9 nonnull edges, 4 terminal flags, 260 slots. Fresh root has no children; empty insertion adds no node.

**Investigation:** Explain new ArrayNode(), local current=child, and current.children[j]=child as three different operations. Identify the effect of terminal versus child changes. Check two separate node arrays are different identities.

**Completion criteria:** Produce a complete drawing and label a versus ba as different prefixes. Explain final on the array field versus mutability of its elements.



- [ ] **T1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

## 4. Level 2: Core operations

Revisit [insertion](./Tries_Essential_Concepts.md#5-deriving-insertion), [exact search](./Tries_Essential_Concepts.md#6-deriving-exact-word-search), and [prefix lookup](./Tries_Essential_Concepts.md#7-prefix-search-and-the-public-api) only after your attempt. O1–O3 form the core blank-editor implementation drill.

### O1. Insert and report structural growth

**Difficulty:** Medium. **Objective:** Preserve shared paths and expose allocation separately from membership.

**Prerequisites:** F1/F3, A, loop invariants.

Insert one word into the existing root and return the number of newly attached nodes, excluding root. Reuse every existing node. Mark only the complete word endpoint, retaining all previous terminal flags.

**Input:** A root and word; L<=100000.

**Output:** int delta, the number of new nodes.

**Constraints and policies:** A; permitted mutation is adding absent edges and setting the endpoint terminal. A duplicate returns zero. A new shorter word can also return zero. Full validation precedes mutation.

```java
static int insertAdded(ArrayNode root, String word) {
    throw new UnsupportedOperationException("TODO O1");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"initial":[],"insert":["app","apple","apply","bat","apple","ap",""]}` | `{"added":[3,2,1,3,0,0,0],"nodes":10,"words":6}` |
| `{"initial":["apple"],"insert":["app","apple"]}` | `{"added":[0,0],"nodes":6,"words":2}` |
| `{"initial":["a"],"insert":["z"]}` | `{"added":[1],"nodes":3,"words":2}` |
| `{"initial":[],"insert":["ab#"]}` | `"IAE; exact structural snapshot unchanged"` |
| `{"initial":["app"],"insert":[null]}` | `"NPE; exact structural snapshot unchanged"` |

**Test tasks:** Draw both apple→app and app→apple insertion orders. For each character list existing versus new child, prefix after moving, and final marker update. Check duplicate insertion preserves every object identity. Test empty insertion twice and a 100000-letter path.

**Target time:** O(L+1) for fixed sigma; O(L+sigma*delta+1) with allocation explicit.

**Target auxiliary space:** O(1) iterative locals.

**Persistent storage / output:** delta nodes and sigma*delta reference slots persist.

**Completion criteria:** Give initialization/preservation/termination reasoning. Verify earlier words remain searchable and every new nonroot path belongs to an accepted word.

<details>
<summary>Hint 1</summary>

A prefix can already exist without its endpoint being a stored word.

</details>

<details>
<summary>Hint 2</summary>

An existing edge owns all words below it; insertion must preserve that identity.

</details>

<details>
<summary>Hint 3</summary>

Validate the full String first, then count only absent edges that you attach.

</details>

- [ ] **O1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### O2. Decide exact membership

**Difficulty:** Easy. **Objective:** Use terminal state after literal traversal.

**Prerequisites:** F3, O1.

Return whether exactly word was successfully inserted. You may reuse your tested endpoint helper. Do not infer membership from a path or a leaf alone.

**Input:** A root and word; L<=100000.

**Output:** boolean exact membership.

**Constraints and policies:** A, read-only, empty membership follows root.isWord, no wildcard syntax.

```java
static boolean contains(ArrayNode root, String word) {
    throw new UnsupportedOperationException("TODO O2");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["app","apple","apply","bat"],"queries":["app","ap","appl","apple","application","bat",""]}` | `[true,false,false,true,false,true,false]` |
| `{"words":[],"queries":["","a"]}` | `[false,false]` |
| `{"words":["","apple","app","app"],"queries":["","app","ap"]}` | `[true,true,false]` |
| `{"words":[],"queries":["missing#"]}` | `"IAE"` |
| `{"words":[],"queries":[null]}` | `"NPE"` |

**Test tasks:** Query before insertion, after one insertion, and after duplicate insertion. Compare snapshots before and after every query. Include a missing first edge and a missing final edge.

**Target time:** O(L+1) with validation.

**Target auxiliary space:** O(1).

**Persistent storage / output:** No growth; boolean output.

**Completion criteria:** Explain why app can be terminal with descendants and why appl is rejected despite existing.

**Interviewer follow-up:** Would checking children.isEmpty() express the intended contract?



- [ ] **O2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### O3. Decide prefix-path existence

**Difficulty:** Easy. **Objective:** Reuse traversal while changing endpoint interpretation.

**Prerequisites:** F3, O2.

Return whether the complete prefix route exists. Follow the workbook root-path policy, including the empty prefix on a fresh root.

**Input:** A root and prefix; P<=100000.

**Output:** boolean path existence.

**Constraints and policies:** A, read-only. A terminal marker is not required. Invalid suffixes must still be rejected after full validation.

```java
static boolean starts(ArrayNode root, String prefix) {
    throw new UnsupportedOperationException("TODO O3");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["app","apple","apply","bat"],"queries":["","a","ap","appl","apple","applea","b","ba","az"]}` | `[true,true,true,true,true,false,true,true,false]` |
| `{"words":[],"queries":["","a"]}` | `[true,false]` |
| `{"words":[""],"queries":["","a"]}` | `[true,false]` |
| `{"words":[],"queries":["z#"]}` | `"IAE"` |
| `{"words":[],"queries":[null]}` | `"NPE"` |

**Test tasks:** Pair every query with exact search from O2 and justify any difference. Repeating a query must not create the missing prefix.

**Target time:** O(P+1) with validation.

**Target auxiliary space:** O(1).

**Persistent storage / output:** No growth; boolean output.

**Completion criteria:** Explain why the chosen empty-prefix answer differs from an existential stored-word policy on an empty dictionary.

**Interviewer follow-up:** Does prefix existence require enumerating every matching word?



- [ ] **O3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### O4. Validate an entire batch before accepting it

**Difficulty:** Medium. **Objective:** Extend the no-partial-change guarantee across multiple words.

**Prerequisites:** O1, validation boundaries.

Insert every member of a batch into one root, but reject the entire batch without any structural change if the array is null, any entry is null, or any character is invalid. Return total newly attached nodes. Preserve batch order for identifying the first error: scan entries from left to right, and characters from left to right.

**Input:** A root and String[] words; m<=20000, sum lengths<=200000, each L<=100000.

**Output:** long total delta across a successful batch.

**Constraints and policies:** A. Null array/entry -> NPE, first invalid character -> IAE. Rejection is transactional only for these validation errors. Input array/Strings unchanged. Empty batch returns zero. Reuse O1 after establishing the batch guarantee.

```java
static long insertBatch(ArrayNode root, String[] words) {
    throw new UnsupportedOperationException("TODO O4");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"initial":[],"batch":["app","apple","apply","bat"]}` | `{"added":9,"nodes":10,"words":4}` |
| `{"initial":["apple"],"batch":["app","","apple"]}` | `{"added":0,"nodes":6,"words":3}` |
| `{"initial":[],"batch":[]}` | `{"added":0,"nodes":1,"words":0}` |
| `{"initial":["bat"],"batch":["app","ab#"]}` | `"IAE; exact structural snapshot unchanged"` |
| `{"initial":["bat"],"batch":["app",null]}` | `"NPE; exact structural snapshot unchanged"` |
| `{"initial":[],"batch":null}` | `"NPE; exact structural snapshot unchanged"` |
| `{"initial":[],"batch":["#",null]}` | `"IAE; exact structural snapshot unchanged"` |

**Test tasks:** Include 20000 empty strings, all duplicates, and a bad final character in the final word. Confirm neither new prefixes nor new terminal flags survive rejection.

**Target time:** O(Sbatch+m+1) with fixed sigma.

**Target auxiliary space:** O(1) explicit locals, no full clone.

**Persistent storage / output:** Only nodes for a successful batch persist; no temporary trie copy is needed.

**Completion criteria:** State when mutation becomes permissible and why a per-word validating insert alone cannot guarantee whole-batch rejection.

<details>
<summary>Hint 1</summary>

A later bad word can invalidate earlier otherwise valid insertions.

</details>

<details>
<summary>Hint 2</summary>

Separate validation of the entire request from the mutation phase.

</details>

<details>
<summary>Hint 3</summary>

Two scans of total input are still linear; the second may call your existing insertion routine.

</details>

- [ ] **O4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### O5. Support literal UTF-16 keys with map children

**Difficulty:** Medium. **Objective:** Transfer the same path/terminal contracts to another symbol representation.

**Prerequisites:** O1–O3, Java HashMap, M.

Implement a fresh-instance dictionary with insert, search, and startsWith under M. Store only nonnull MapNode children. Define MapNode yourself before using the common scaffold. No wildcard method is requested.

**Input:** Successive nonnull raw Java Strings, each length<=100000; total characters<=200000.

**Output:** insert returns void; the two query methods return booleans.

**Constraints and policies:** M; insertion mutates only this instance. Queries read-only. No ordering promise is needed. Arbitrary char sequences, including lone surrogates, are permitted; dot is literal.

```java
static final class CharDictionary {
    private final MapNode root = new MapNode();
    public void insert(String word) {
        throw new UnsupportedOperationException("TODO O5 insert");
    }
    public boolean search(String word) {
        throw new UnsupportedOperationException("TODO O5 search");
    }
    public boolean startsWith(String prefix) {
        throw new UnsupportedOperationException("TODO O5 prefix");
    }
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["A1","a.b","café"],"exact":["A1","a1","a.b","a","café"],"prefix":["A","a.",""]}` | `{"exact":[true,false,true,false,true],"prefix":[true,true,true]}` |
| `{"words":["😀"],"exact":["\ud83d","😀"],"prefix":["\ud83d"]}` | `{"exact":[false,true],"prefix":[true]}` |
| `{"words":["","\ud83d",""],"exact":["","\ud83d"],"prefix":[""]}` | `{"exact":[true,true],"prefix":[true]}` |
| `{"words":[],"exact":[""],"prefix":[""]}` | `{"exact":[false],"prefix":[true]}` |
| `{"operation":"each String method","argument":null}` | `"NPE"` |

**Test tasks:** Interpret backslash-u values as their indicated code units, not six literal characters. Insert a composed accent and query a decomposed spelling to test no normalization. Use two independent instances and duplicate insertions; snapshot map edges and flags.

**Target time:** insert: expected amortized O(L+1); queries: expected O(L+1) worst traversal, with early stopping.

**Target auxiliary space:** O(1) query/traversal locals; an insertion may additionally use O(dmax+1) transient rehash storage, where dmax is the largest resized child-map degree.

**Persistent storage / output:** O(N) under ordinary default growth, including nodes/maps, N-1 entries, and capacities; new structure persists after insertion. The fixed raw-char alphabet bounds dmax, but the allocation still matters in practice.

**Completion criteria:** Defend Character value equality, absent get semantics, case sensitivity, and the difference between code units and code points.

<details>
<summary>Hint 1</summary>

Changing child storage should not change what a terminal marker means.

</details>

<details>
<summary>Hint 2</summary>

A missing mapping means no edge because this implementation never stores null values.

</details>

<details>
<summary>Hint 3</summary>

Use one original String with charAt positions; reuse a mapped child before considering allocation.

</details>

- [ ] **O5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### T2. Account for insertion one character at a time

**Difficulty:** Medium. **Objective:** Observe preserved identities and zero-growth new words.

**Prerequisites:** O1/O4.

Start fresh and insert ["cane","cab","can","cane","","dog"]. Before coding a trace printer, draw each resulting trie. For every character record position, slot, prefix before, existing child identity or new child identity, and prefix after. Record the final flag separately.

**Test tasks:** Checkpoint arrays in forward order: deltas=[4,1,0,0,0,3], totals=[5,6,6,6,6,9], distinct terminal counts=[1,2,3,3,4,5]. Invalid suffix "ca#" after the complete history throws and preserves the exact snapshot.

**Investigation:** Give newly allocated nodes and total nodes after each insertion, terminal-count changes, and the final prefix set. Repeat with reversed first three words. Explain which intermediate paths are temporarily not prefixes of an accepted word until insertion completes.

**Completion criteria:** Submit initial/final diagrams, all character rows, and an explanation of both shorter-after-longer and longer-after-shorter orders.

<details>
<summary>Hint 1</summary>

Count missing prefixes, not the length of every newly accepted word.

</details>

<details>
<summary>Hint 2</summary>

The can path exists after cane but its terminal marker is a separate fact.

</details>

<details>
<summary>Hint 3</summary>

Keep a prefix-to-object-ID table; only add a row when a missing edge is attached.

</details>

- [ ] **T2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### T3. Explain paired exact and prefix answers

**Difficulty:** Easy. **Objective:** Trace endpoints without conflating flags with paths.

**Prerequisites:** O2/O3.

On Q, query ["ca","cab","can","cane","cap","cape","caper","do","dog",""]. Record validation length, visited prefixes, first absent edge if any, endpoint flag, exact result, and prefix result.

**Test tasks:** Expected exact vector=[false,true,false,true,false,true,false,false,true,false]; expected prefix vector=[true,true,true,true,true,true,false,true,true,true]. On E, empty exact is false and empty prefix is true. On E, "z#" throws IAE for both APIs even though z is absent.

**Completion criteria:** Provide a route/flag explanation for every difference and explain why early traversal failure does not skip full validation.



- [ ] **T3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.


## 5. Child-storage design workshop

Use [array versus map representation](./Tries_Essential_Concepts.md#8-array-children-and-map-children). A byte estimate needs an actual JVM layout; reference-slot and entry counts alone are not byte measurements.

### R1. Choose storage for five alphabets

**Difficulty:** Medium. **Objective:** Derive the encoding and memory tradeoff from actual requirements.

**Prerequisites:** A, M, O5, String indexing.

For each workload choose a 26-slot array, a different explicitly indexed array, a Character map, or a different symbol model. State what is accepted, rejected, or normalized before making the choice.

| Workload | Requirement |
| --- | --- |
| Lowercase dictionary | Frequent literal and dot queries; many sparse paths |
| Inventory identifiers | A..Z plus 0..9, case-sensitive |
| Sensor codes | Only x, y, z, and dash; repeated prefixes |
| Raw Java text | Exact/prefix operations on any String code-unit sequence |
| Displayed symbols | One wildcard must consume one Unicode code point, including emoji |


**Test tasks:** Use labels a,z,A,0,- and U+1F600. The 26-slot mapping must reject A and 0. For raw-char indexing the emoji spans two edges; the code-point requirement spans one symbol and needs a changed traversal model. Advanced Unicode coding is not required.

**Investigation:** Compare sparse/dense memory, direct versus expected lookup, array initialization, bucket/entry overhead, iteration order, and the validity of c-'a'. Explain why changing only the container does not settle wildcard character semantics.

**Completion criteria:** Give a defensible decision for every row and a workload change that would reverse one decision.

<details>
<summary>Hint 1</summary>

A character encoding must be injective over the accepted alphabet.

</details>

<details>
<summary>Hint 2</summary>

The number of possible symbols differs from the number of children actually present at one prefix.

</details>

<details>
<summary>Hint 3</summary>

Write the symbol-to-slot rule and validation policy before comparing arrays with maps.

</details>

- [ ] **R1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### R2. Audit storage claims without guessing bytes

**Difficulty:** Medium. **Objective:** Separate nodes, reference slots, mappings, capacity, and total insertion length.

**Prerequisites:** F2, HashMap growth.

Compare array and ordinary default-growth map tries for D, a single 64-letter word, all 26 one-letter words, and 1000 repeated insertions of apple. Assume no deletion, retained whole-word copies, or deliberate map over-sizing.

**Test tasks:** D: N=10, slots=260, entries=9. Single length-64 word: N=65, slots=1690, entries=64. All letters: N=27, slots=702, entries=26. Repeated apple: N=6, slots=156, entries=5. Include the root object/map even when no edge exists.

**Investigation:** For each dictionary compute N, N-1 edges, terminal count, and 26N array slots. List which Java objects each representation needs. Explain whether a dense root makes every descendant dense. Challenge the claims "a map stores only its entries" and "1000 inserts require 1000 distinct paths."

**Completion criteria:** Give symbolic storage with capacities, expected/amortized qualifications, and a measurement plan if exact heap bytes are requested.

<details>
<summary>Hint 1</summary>

Build a set of distinct nonempty prefixes on paper.

</details>

<details>
<summary>Hint 2</summary>

Every nonroot node has exactly one incoming mapping or nonnull array slot.

</details>

<details>
<summary>Hint 3</summary>

A HashMap has a container and backing capacity in addition to entries; ordinary growth is an explicit assumption.

</details>

- [ ] **R2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### R3. Select a dictionary service from its workload

**Difficulty:** Medium. **Objective:** Avoid choosing a trie merely because inputs are strings.

**Prerequisites:** Sets, maps, sorted lookup, basic trie operations.

Compare six workload cards: (a) only exact blocked-ID membership; (b) exact ID-to-count retrieval; (c) 10 prefix questions over 12 static words; (d) a pre-sorted static catalog with repeated prefix existence; (e) many dynamic incremental prefix queries; (f) repeated whole-word dot patterns. Choose among HashSet, HashMap, scanning, sorted array, and trie. No coding is required.

**Test tasks:** Use an empty dictionary, long common prefixes, and fresh String query objects. Exact membership must not accept a stored prefix; a fresh key can require character hashing. A sorted array remains usable for prefix ranges without allocating a node per prefix.

**Investigation:** For each card state whether prefixes, shared prefixes, exact membership, or wildcards matter; justify build cost, query cost, update cost, and memory. More than one choice may be defensible if workload assumptions are explicit.

**Completion criteria:** Defend two cases where a trie is unnecessary and one workload where its extra structure is justified. Do not call whole-String hashing/comparison unconditionally constant time.

<details>
<summary>Hint 1</summary>

Start from the questions the service actually asks.

</details>

<details>
<summary>Hint 2</summary>

Preprocessing should be amortized over the expected future workload.

</details>

<details>
<summary>Hint 3</summary>

Count character work inside hashing, comparisons, and startsWith, not just container operations.

</details>

- [ ] **R3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

## 6. Level 3: Wildcard DFS

The core progression is one wildcard, arbitrary consecutive wildcards, then an aggregate result. Use [wildcard semantics](./Tries_Essential_Concepts.md#9-wildcard-search-using-dfs) and [recursive contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) after attempting your own derivation. Keep all queries read-only and pass positions rather than suffix copies.

### W1. Match a pattern with at most one dot

**Difficulty:** Medium. **Objective:** Introduce branching without losing whole-word semantics.

**Prerequisites:** O1–O3, recursion basics, W.

Return whether any stored word matches pattern when zero or one dot is allowed. Reject a pattern containing two or more dots with IAE, even if the first literal edge is absent. Use an indexed helper and define its promise before implementing.

**Input:** A root with H<=64 and a pattern of length<=64.

**Output:** boolean whole-word match.

**Constraints and policies:** W, with at most one dot; full prevalidation includes counting dots. Empty and literal-only patterns remain legal.

```java
static boolean oneDot(ArrayNode root, String pattern) {
    throw new UnsupportedOperationException("TODO W1");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["cab","cane","cape","dog"],"patterns":["cab",".ab","c.b","ca.","ca.e","...","c..","z.."]}` | `[true,true,true,true,true,"IAE","IAE","IAE"]` |
| `{"words":["cab","cane","cape","dog"],"patterns":[".","ca","cat","caz.","dog.",""]}` | `[false,false,false,false,false,false]` |
| `{"words":[""],"patterns":["","."]}` | `[true,false]` |
| `{"words":[],"patterns":["z#",null]}` | `["IAE","NPE"]` |

**Test tasks:** Test wildcard first/middle/last, multiple possible matching children, a missing literal after a candidate, and a prefix-only endpoint. All 26 one-letter words with "." must succeed. Snapshot every query.

**Target time:** O(L+V+sigma*B+1); with one dot and b>=1 a loose state bound is O((L+1)*b), capped by N.

**Target auxiliary space:** O(min(L,H)+1) frames.

**Persistent storage / output:** No growth; boolean output. Handle b=0 separately as root-only exploration.

**Completion criteria:** Explain exact-child selection, the terminal base, sibling independence, safe early success, and why one failed child cannot reject all candidates.

<details>
<summary>Hint 1</summary>

Before the dot there is only one literal route to consider.

</details>

<details>
<summary>Hint 2</summary>

Every candidate consumes one pattern position regardless of whether its continuation succeeds.

</details>

<details>
<summary>Hint 3</summary>

Do not put a failure return inside the first eligible candidate branch.

</details>

- [ ] **W1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### W2. Match arbitrary consecutive dots

**Difficulty:** Medium. **Objective:** Implement complete whole-word wildcard DFS.

**Prerequisites:** W1, recursive soundness/completeness.

Extend matching to any number of dots, including adjacent dots and an all-dot pattern. Implement from a fresh editor rather than adding special cases for particular dot positions. You may reuse validation only after changing its dot-count policy.

**Input:** A root with H<=64 and pattern length<=64.

**Output:** boolean existence of a matching stored word.

**Constraints and policies:** W with no cap on dots except pattern length. Visit nonnull children in a..z order for reproducible traces; return on the first success. No mutable global cursor or stale result field.

```java
static boolean wildcardSearch(ArrayNode root, String pattern) {
    throw new UnsupportedOperationException("TODO W2");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["cab","cane","cape","dog"],"patterns":["c..e","c..z","....","...","..",".....","d..",".a..","c...",""]}` | `[true,false,true,true,false,false,true,true,true,false]` |
| `{"words":["","a","ab","abc","ab"],"patterns":["",".","..","...","....","a..","..c","b."]}` | `[true,true,true,true,false,true,true,false]` |
| `{"words":[],"patterns":["",".","..."]}` | `[false,false,false]` |
| `{"words":["cane"],"patterns":["ca","c.","c..","c...","c*",null]}` | `[false,false,false,true,"IAE","NPE"]` |

**Test tasks:** Trace a failed cab branch followed by cane success for Q/"c..e". Query an impossible suffix next to expose stale state. Test 64 dots against a matching length-64 word, plus many shallow branches. Do not use a length-100000 recursive stress test.

**Target time:** O(L+V+sigma*B+1), fixed sigma gives O(L+V+1); V<=min(N,sum of b^i for i=0..L).

**Target auxiliary space:** O(min(L,H)+1) frames.

**Persistent storage / output:** No growth; one original String and index state, no suffix copies.

**Completion criteria:** Prove terminal exhaustion, soundness, completeness, and termination by remaining positions. Explain why a visited set is unnecessary for this ordinary trie and why stack depth differs from total calls.

<details>
<summary>Hint 1</summary>

The complete state includes the current node and the number of consumed pattern positions.

</details>

<details>
<summary>Hint 2</summary>

A wildcard is an existence question over all eligible child continuations.

</details>

<details>
<summary>Hint 3</summary>

Keep one String; pass the next index by value to every child and skip null slots before calling.

</details>

- [ ] **W2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### W3. Count distinct matching words

**Difficulty:** Medium. **Objective:** Change existential combination to a complete aggregate without counting duplicates.

**Prerequisites:** W2, scalar subtree aggregation.

Return the number of distinct stored words matching a W pattern. Duplicate insertions do not increase the result. Account for all eligible matches, including empty membership, while preserving the dictionary.

**Input:** A root with N<=200000 and H<=64; W pattern length<=64.

**Output:** long number of matching terminal endpoints.

**Constraints and policies:** W. Visit children a..z; output is a scalar, with no ordering requirement. Read-only and no full list of matching words.

```java
static long countMatches(ArrayNode root, String pattern) {
    throw new UnsupportedOperationException("TODO W3");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["cab","cane","cape","dog","cane"],"patterns":["c..e","....","...","c..","....z",""]}` | `[2,2,2,1,0,0]` |
| `{"words":["","a","ab","abc","a"],"patterns":["",".","..","...","...."]}` | `[1,1,1,1,0]` |
| `{"words":["a","b","c","z"],"patterns":[".",".."]}` | `[4,0]` |
| `{"words":[],"patterns":["","."]}` | `[0,0]` |
| `{"words":["a"],"patterns":["*",null]}` | `["IAE","NPE"]` |

**Test tasks:** All 26 one-letter words produce 26 for ".". Compare your result against a deduplicated word-list matcher that tests length and each position independently. Snapshot flags and child references.

**Target time:** O(L+V+sigma*B+1), now V counts all eligible states because no success short-circuit is allowed.

**Target auxiliary space:** O(min(L,H)+1) frames.

**Persistent storage / output:** No growth; constant-size long output.

**Completion criteria:** Explain why each stored matching word contributes once, why duplicates do not contribute again, and why W2 cannot merely return a boolean converted to 0/1.

<details>
<summary>Hint 1</summary>

Each complete word has exactly one terminal endpoint.

</details>

<details>
<summary>Hint 2</summary>

The exhausted-pattern case contributes a count rather than an existence flag.

</details>

<details>
<summary>Hint 3</summary>

Accumulate child contributions without returning after the first positive child.

</details>

- [ ] **W3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### T4. A failed branch before a successful branch

**Difficulty:** Medium. **Objective:** Trace DFS entries, returns, slot scans, and short-circuiting separately.

**Prerequisites:** W2.

Use Q and pattern "c..e". Draw the trie from Q, then complete this partial recursion diagram and a chronological enter/return table. Use a..z child order, no calls on null children, and stop at the first successful terminal exhaustion.

```text
("", i=0)
  -> ("c", i=1)
       -> ("ca", i=2)
            -> ("cab", i=3) -> ?
            -> (?, i=3)    -> ...
            -> ("cap", i=3) -> entered or skipped?
```

**Test tasks:** Behavior checkpoints: "c..e" is true; "c..z" is false. The success enters 6 nonnull helper states, peaks at 5 frames, and examines 15 wildcard slots. The failure enters 6 states, peaks at 4 frames, and examines 52 wildcard slots. Derive the calendars yourself.

**Investigation:** Record prefix, i, live helper depth, relevant character, returned boolean, and whether each child is actually entered. Count literal lookups and wildcard-slot checks separately. Then trace "c..z" and compare.

**Completion criteria:** Show why cab failure leaves the earlier suspended frames available, why cap is skipped only after success, and why sequential branches do not add their full depths.

<details>
<summary>Hint 1</summary>

The final e is literal; a terminal cab node does not have a following e child.

</details>

<details>
<summary>Hint 2</summary>

The wildcard at prefix c reaches only a, while the wildcard at ca can reach b,n,p.

</details>

<details>
<summary>Hint 3</summary>

Slot scans include absent slots before a tried candidate; null slots are not helper calls.

</details>

- [ ] **T4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### T5. Separate geometric exploration from path depth

**Difficulty:** Medium. **Objective:** Construct a counterexample to universal linear wildcard time.

**Prerequisites:** W2, geometric sums.

Store ["aax","abx","bax","bbx"] and query "..". Draw all consumed-prefix states and annotate terminal status. Then query "..." using the same exploration order. Finally generalize to all binary words of length k, each followed by x.

**Test tasks:** For the concrete fixture N=11 and S=12. ".." is false with 7 calls, 3 frames, 78 slot checks. "..." is true with 4 calls, 4 frames, 26 slot checks. For k=0 the stored word is x and the empty pattern fails at root.

**Investigation:** For both concrete queries count calls, maximum live frames, and array-slot checks. Explain why the last x edge exists but is not followed by the two-dot query. Give the family state count and compare it with N.

**Completion criteria:** Supply the drawn call tree and a growth argument with b>=2. Address b=1 and root-only b=0 separately; do not substitute 1^L to erase the L-length path.

<details>
<summary>Hint 1</summary>

The two-dot pattern exhausts at nonterminal depth-two nodes.

</details>

<details>
<summary>Hint 2</summary>

A failed existence query may need every eligible candidate; an early success does not.

</details>

<details>
<summary>Hint 3</summary>

Sum the number of reachable states at depths zero through the consumed pattern length.

</details>

- [ ] **T5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

## 7. Level 4: Curated platform practice

These are exactly the [roadmap Tries practice entries](./DSA_Interview_Roadmap.md#8-tries), in their original order: **E, E, C**. There is **no Additional (A) Trie tier**. E/C describe local learning priority; Medium/Hard describe platform difficulty. Open the official statement for its current constraints; do not silently impose this workbook's empty/validation policies on a judge with a different domain.

### P208. 208. Implement Trie (Prefix Tree)

**Official problem:** [208. Implement Trie (Prefix Tree)](https://leetcode.com/problems/implement-trie-prefix-tree/). **Priority:** E — Essential. **Difficulty:** Medium.

**Learning objective:** Shared-prefix insertion, exact membership, and prefix lookup.

**Prerequisites:** F1–F3, O1–O3; array/map and terminal invariants.

**Attempt task:** Test shorter/longer keys in both insertion orders, duplicate insertion, missing paths, and prefix-only endpoints. Explain the platform's nonempty input domain separately. Write Java from a blank editor, test it, and explain correctness and costs. No solution or full platform statement is reproduced here.

- [ ] **P208 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —. Detailed history: [existing roadmap Log](./DSA_Interview_Roadmap.md#8-tries).

### P211. 211. Design Add and Search Words Data Structure

**Official problem:** [211. Design Add and Search Words Data Structure](https://leetcode.com/problems/design-add-and-search-words-data-structure/). **Priority:** E — Essential. **Difficulty:** Medium.

**Learning objective:** Whole-word literal/dot matching with correct branch and base behavior.

**Prerequisites:** O1, W1/W2, recursive contracts and branch costs.

**Attempt task:** Trace an unsuccessful candidate before success and an all-candidates failure. Recheck the platform's dot-count and length constraints; W2 deliberately allows more general patterns. Write Java from a blank editor, test it, and explain correctness and costs. No solution or full platform statement is reproduced here.

- [ ] **P211 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —. Detailed history: [existing roadmap Log](./DSA_Interview_Roadmap.md#8-tries).

### P212. 212. Word Search II

**Official problem:** [212. Word Search II](https://leetcode.com/problems/word-search-ii/). **Priority:** C — Optional advanced challenge. **Difficulty:** Hard.

**Learning objective:** Prefix pruning across board paths with correct Backtracking state.

**Prerequisites:** Both Tries and [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking) must be comfortable before attempting this problem.

**Attempt task:** Defer until the prerequisite is reliable. Examine cell reuse, boundaries, restoration, terminal nodes with descendants, and duplicate output. This problem is not required for ordinary Tries readiness. Write Java from a blank editor, test it, and explain correctness and costs. No solution or full platform statement is reproduced here.

- [ ] **P212 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —. Detailed history: [existing roadmap Log](./DSA_Interview_Roadmap.md#8-tries).


## 8. Level 5: Unfamiliar interview challenges

Hide the learning-objective map and hints before starting. For every challenge compare trie, HashSet, HashMap, sorted-array, and scanning approaches as applicable. State whether prefix queries, shared prefixes, exact membership, or wildcard matching actually matter. Justify construction against query volume and memory. A title does not identify the intended technique. More than one implementation may meet a contract.

### X1. Find the deepest applicable rule

**Difficulty:** Medium. **Objective:** Select state for repeated longest accepted-prefix queries.

**Prerequisites:** Core dictionary operations and independent selection.

A service registers lowercase rule keys. Given a request key, return the length of the longest registered key that is a prefix of it. A registered empty key is a valid length-zero fallback; if no registered key applies, return OptionalInt.empty(). Support many registrations and queries without scanning every registered key on each request.

**Input:** Successive A keys; register duplicates idempotently. Individual length<=100000, total registered characters<=200000.

**Output:** register returns void; longestRule returns OptionalInt with a prefix length or empty.

**Constraints and policies:** A rejection/empty rules for registration. A literal query may extend beyond every stored key. No wildcards. Query is iterative and read-only; result length is unique even if duplicate registration occurred.

```java
static final class RuleBook {
    public void register(String key) {
        throw new UnsupportedOperationException("TODO X1 register");
    }
    public OptionalInt longestRule(String request) {
        throw new UnsupportedOperationException("TODO X1 query");
    }
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"keys":["api","app","apple"],"queries":["apples","application","apricot","app",""]}` | `[5,3,null,3,null]` |
| `{"keys":["","a","ab"],"queries":["abcd","az","z",""]}` | `[2,1,0,0]` |
| `{"keys":[],"queries":["","a"]}` | `[null,null]` |
| `{"keys":["app","app"],"queries":["apple","app"]}` | `[3,3]` |
| `{"keys":["a"],"queries":["z#",null]}` | `["IAE","NPE"]` |

**Test tasks:** JSON null means OptionalInt.empty(), not a Java null return. If a longer route exists but its endpoint is nonterminal, return the last applicable registered length. Test 100000-character literal queries and separate instances.

**Target time:** O(L+1) per registration/query under fixed alphabet, after an index is established.

**Target auxiliary space:** O(1) iterative query state.

**Persistent storage / output:** State across requests must be counted; aim for O(S+1) persistent structural storage.

**Completion criteria:** State the meaning of the best candidate after each consumed character; distinguish absent from a valid zero-length rule. Defend the chosen representation.

**Interviewer follow-up:** If only five rules and one request ever exist, would your design change?

<details>
<summary>Hint 1</summary>

A full request need not itself be a registered key.

</details>

<details>
<summary>Hint 2</summary>

A later missing continuation cannot invalidate an earlier applicable complete key.

</details>

<details>
<summary>Hint 3</summary>

Maintain the best accepted length while consuming the request; treat root membership as a candidate only if registered.

</details>

- [ ] **X1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### X2. Locate the first repeated admission

**Difficulty:** Medium. **Objective:** Choose only the information needed for exact repetition detection.

**Prerequisites:** Arrays & Hashing, String equality.

Process admission labels left to right. Return the smallest index j whose full label equals any earlier label. Labels sharing only a prefix are different. Return -1 if no repeated label exists. This is one pass over a batch, with no subsequent prefix or wildcard queries.

**Input:** String[] labels, n<=100000, total UTF-16 units T<=1000000.

**Output:** int index of the first repeated occurrence, or -1.

**Constraints and policies:** Raw UTF-16 equality, case-sensitive, no normalization; empty labels and duplicates allowed, dot literal. Null array/entry -> NPE. Do not modify input. All entries must be null-checked even if an earlier duplicate would otherwise finish the query.

```java
static int firstRepeatedAdmission(String[] labels) {
    throw new UnsupportedOperationException("TODO X2");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"labels":["app","apple","app","apple"]}` | `2` |
| `{"labels":["A","a","A"]}` | `2` |
| `{"labels":["","x",""]}` | `2` |
| `{"labels":["a","ab","abc"]}` | `-1` |
| `{"labels":[]}` | `-1` |
| `{"labels":["a","a",null]}` | `"NPE"` |
| `{"labels":null}` | `"NPE"` |

**Test tasks:** Use separate String objects with equal contents and long shared prefixes. Test a duplicate only at the last position. Explain the input-wide null validation before an early return.

**Target time:** Expected O(T+n+1) including character hashing/equality and validation.

**Target auxiliary space:** O(u+1) stored references/entries for u distinct labels encountered; count any copies if you choose to create them.

**Persistent storage / output:** No persistent index after return; scalar output.

**Completion criteria:** Compare the chosen representation with a trie and explain why exact content equality, not object identity, determines repetition.

<details>
<summary>Hint 1</summary>

The future question asks whether a whole label has appeared, not how it can be extended.

</details>

<details>
<summary>Hint 2</summary>

Before processing position j, retain membership for exactly the earlier labels.

</details>

<details>
<summary>Hint 3</summary>

A membership container can store original immutable String references; count their hash/equality work.

</details>

- [ ] **X2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### X3. Reconcile request totals

**Difficulty:** Medium. **Objective:** Select a key-to-aggregate representation and use a wide result type.

**Prerequisites:** HashMap, long arithmetic, full-key equality.

Each event has a label and nonnegative units. For each requested label return the total units across events with exactly that label, preserving request order. Missing labels return zero. There are no prefix or wildcard requests.

**Input:** String[] labels and int[] units of equal length n<=100000; String[] requested with q<=100000; each units value is 0..1000000000.

**Output:** Fresh long[q], in requested order.

**Constraints and policies:** Raw UTF-16, case-sensitive, no normalization; empty labels/requests allowed, literal dots. Duplicate events add units and duplicate requests repeat the answer. Null arrays/entries -> NPE; unequal lengths or negative units -> IAE. If multiple independent violations occur, any applicable specified exception is accepted. Inputs read-only.

```java
static long[] requestTotals(String[] labels, int[] units, String[] requested) {
    throw new UnsupportedOperationException("TODO X3");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"labels":["app","apple","app"],"units":[2,7,5],"requested":["app","ap","apple","app"]}` | `[7,0,7,7]` |
| `{"labels":["","A","a","A"],"units":[3,0,4,9],"requested":["","A","a","z"]}` | `[3,9,4,0]` |
| `{"labels":["x","x","x"],"units":[1000000000,1000000000,1000000000],"requested":["x"]}` | `[3000000000]` |
| `{"labels":[],"units":[],"requested":["x",""]}` | `[0,0]` |
| `{"labels":["x"],"units":[],"requested":[]}` | `"IAE"` |
| `{"labels":["x"],"units":[-1],"requested":[]}` | `"IAE"` |
| `{"labels":[null],"units":[1],"requested":[]}` | `"NPE"` |

**Test tasks:** Test an empty request array, null array, null request entry, all-zero events, and fresh equal String objects. Do not merge app with apple or A with a.

**Target time:** Expected O(T+n+q+1), where T is total event/request character length and hash/equality work is counted.

**Target auxiliary space:** O(u+1) aggregate entries for u distinct event labels, excluding the required output.

**Persistent storage / output:** No persistent index after return; O(q) required output. Maximum possible total is 10^14, within long.

**Completion criteria:** Explain what a membership-only set would lose, why counts need long, and why prefix structure does not help these exact-key requests.

<details>
<summary>Hint 1</summary>

An event contributes to only one full label.

</details>

<details>
<summary>Hint 2</summary>

The aggregate for a key must equal the sum of all processed matching events.

</details>

<details>
<summary>Hint 3</summary>

Widen before accumulating; return results in request order, not map iteration order.

</details>

- [ ] **X3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### X4. Search a frozen catalog

**Difficulty:** Medium. **Objective:** Exploit an existing ordering while accounting for String comparison costs.

**Prerequisites:** Binary Search, lexicographic ordering.

The caller supplies a pre-sorted array of lowercase catalog entries. Decide whether at least one entry begins with prefix. There are repeated queries, and the caller forbids copying, sorting again, or constructing a persistent index.

**Input:** String[] sorted and a prefix. Entries are guaranteed nonnull, lowercase, and sorted by String.compareTo, with duplicates allowed; n<=100000, each length<=100000.

**Output:** boolean existential prefix match.

**Constraints and policies:** Entries may be empty. The array itself is nonnull by precondition. Validate prefix under A (null -> NPE, invalid -> IAE), but do not scan entries to revalidate the promised sorting/domain. Read-only. **Local empty override:** empty prefix is true iff the catalog contains at least one entry; an empty catalog returns false. No wildcards.

```java
static boolean catalogHasPrefix(String[] sorted, String prefix) {
    throw new UnsupportedOperationException("TODO X4");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"sorted":["app","apple","bat"],"prefix":"ap"}` | `true` |
| `{"sorted":["app","apple","bat"],"prefix":"az"}` | `false` |
| `{"sorted":["app","apple","bat"],"prefix":"b"}` | `true` |
| `{"sorted":["app","apple","bat"],"prefix":"applea"}` | `false` |
| `{"sorted":[],"prefix":""}` | `false` |
| `{"sorted":[""],"prefix":""}` | `true` |
| `{"sorted":[""],"prefix":"a"}` | `false` |
| `{"sorted":["a","a","ab"],"prefix":"a"}` | `true` |
| `{"sorted":[],"prefix":"z#"}` | `"IAE"` |

**Test tasks:** Test prefixes before the first and after the last entry, a prefix equal to a full entry, long common prefixes, a singleton, and null prefix. Trace your interval invariant on an empty array.

**Target time:** O((M+1)*log(n+1)+P+1) safe character-work bound, M=max(P, longest catalog entry length); logarithmically many comparisons.

**Target auxiliary space:** O(1) iterative state.

**Persistent storage / output:** No index growth or copied array; boolean output.

**Completion criteria:** Justify the ordering property used, the candidate you inspect after the search, and the chosen empty-policy override.

<details>
<summary>Hint 1</summary>

Entries with a common prefix occupy a contiguous region under this ordering.

</details>

<details>
<summary>Hint 2</summary>

A boundary candidate can establish whether the region exists.

</details>

<details>
<summary>Hint 3</summary>

Count String.compareTo character inspections as well as the number of binary-search iterations.

</details>

- [ ] **X4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### X5. Count visible labels once

**Difficulty:** Easy. **Objective:** Recognize when a direct scan fits a one-off small workload.

**Prerequisites:** String.startsWith and basic loops.

A screen receives at most 50 labels and one prefix filter, then discards the batch. Count records whose label begins with the filter. Duplicate label records count separately. No future queries or updates will use an index.

**Input:** String[] labels, n<=50, each length<=64, and prefix length<=64.

**Output:** int matching record count.

**Constraints and policies:** Raw UTF-16, case-sensitive, no normalization; empty labels permitted and empty prefix matches all records. Dot literal, no wildcards. Null array/entry/prefix -> NPE. Inputs read-only; no result ordering issue.

```java
static int countVisible(String[] labels, String prefix) {
    throw new UnsupportedOperationException("TODO X5");
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"labels":["app","apple","app","bat"],"prefix":"app"}` | `3` |
| `{"labels":["A1","a1","A2"],"prefix":"A"}` | `2` |
| `{"labels":["","a","a"],"prefix":""}` | `3` |
| `{"labels":[""],"prefix":"a"}` | `0` |
| `{"labels":[],"prefix":""}` | `0` |
| `{"labels":["a.b","axb"],"prefix":"a."}` | `1` |
| `{"labels":[null],"prefix":"a"}` | `"NPE"` |

**Test tasks:** Test null array/prefix, a filter longer than every label, all matches, and no matches. The same exact text occurring twice must contribute twice.

**Target time:** O(n*(P+1)+1) safe upper bound using startsWith.

**Target auxiliary space:** O(1).

**Persistent storage / output:** No index or output collection; scalar result.

**Completion criteria:** Explain why building a trie or deduplicating labels changes the cost or requested meaning. Present a workload change that would justify preprocessing.

**Interviewer follow-up:** What changes if a million prefix filters arrive for the same labels?



- [ ] **X5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### X6. Detect a one-letter transcription

**Difficulty:** Medium. **Objective:** Transfer indexed search to a constrained error budget.

**Prerequisites:** W2, exact whole-word semantics.

Register lowercase reference labels. For a literal query, return true iff some registered label has the same length and differs at **exactly one** character position. Exact equality alone is insufficient. Multiple matching labels still produce one boolean.

**Input:** Successive A registrations and queries of length<=64; N<=200000, H<=64.

**Output:** add returns void; hasOneChange returns boolean.

**Constraints and policies:** A; no wildcard syntax, so a query dot is rejected. Empty words may be registered, but no empty query has exactly one mismatch. Duplicates idempotent. Read-only queries; fresh independent instances.

```java
static final class TranscriptionBook {
    public void add(String label) {
        throw new UnsupportedOperationException("TODO X6 add");
    }
    public boolean hasOneChange(String query) {
        throw new UnsupportedOperationException("TODO X6 query");
    }
}
```

**Examples and required tests.** Each row starts fresh unless it explicitly describes a sequence. Values below use JSON notation; escapes denote the corresponding Java String values.

| Input / setup | Expected result |
| --- | --- |
| `{"words":["cat","cot","dog"],"queries":["cat","cut","cats","dig","fog","zzz",""]}` | `[true,true,false,true,true,false,false]` |
| `{"words":["cat"],"queries":["cat","bat","cab","cot","dog"]}` | `[false,true,true,true,false]` |
| `{"words":["","a","a"],"queries":["","a","b","aa"]}` | `[false,false,true,false]` |
| `{"words":[],"queries":["a",""]}` | `[false,false]` |
| `{"words":["a"],"queries":[".",null]}` | `["IAE","NPE"]` |

**Test tasks:** Include a difference at first/middle/last positions, two mismatches, a different length, and exact equality with a separate one-change neighbor. Explain why cat succeeds in the first fixture but fails in the second.

**Target time:** An indexed solution should use O(L+V+sigma*B+1), where B counts states considering alternative letters and V<=N; define your own valid bounds for another approach.

**Target auxiliary space:** O(min(L,H)+1) active search frames for indexed DFS.

**Persistent storage / output:** Account for O(S+1) persistent fixed-alphabet structure if selected; no query mutation/output list.

**Completion criteria:** Define the full recursive state and remaining allowance, prove exact-one rather than at-most-one acceptance, and compare against scanning all same-length words.

<details>
<summary>Hint 1</summary>

A matching prefix can have used zero or one changes so far.

</details>

<details>
<summary>Hint 2</summary>

At the endpoint, the used-change count and terminal marker must both meet the promise.

</details>

<details>
<summary>Hint 3</summary>

Once the allowance is spent, further characters have only one eligible label; do not accept a second mismatch.

</details>

- [ ] **X6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.


## 9. Complexity investigations

Analyze the Java that actually runs. C1–C3 call the earlier learner methods under their completed contracts; they are compilable clients, not provided solutions for those methods. You can fill your own stubs before running them. C4–C6 are complete analysis subjects, not replacement implementations of the coding exercises.

For **every** C exercise answer: (1) what is the input size; (2) how many characters are processed; (3) how many trie nodes may be visited; (4) what time bound follows; (5) what temporary/stack space is used; (6) what persistent trie storage exists or grows; (7) which representation, validation, hash, copying, or short-circuit assumptions matter; and (8) why a wildcard operation can cost more than a literal operation. Mark a question not applicable only with a reason.

Use L,S,m,N,b and sigma from Section 1. Define additional parameters explicitly. Distinguish full validation from early-stopping traversal and peak live storage from cumulative allocation. Derive answers before opening the delayed numerical checkpoints at the end of this section. No asymptotic answer key is placed beside the questions.

### C1. One insertion and child-array initialization

**Difficulty:** Medium. **Objective:** Account for persistent growth without hiding it behind constant local state.

**Prerequisites:** O1, allocation costs.

Assume insertAdded meets O1, with full validation and 26-slot arrays at every new node. Analyze this client for an absent word, a duplicate, a shorter newly terminal word, and empty input.

```java
static long cNewSlots(ArrayNode root, String word) {
    int delta = insertAdded(root, word);
    return 26L * delta;
}
```

**Test tasks:** Fresh root with apple; then app; then apple again; then empty. Independently draw before/after states. Add z to a root containing a. Check rejected "ab#" preserves all structure.

**Investigation:** Give a delta-sensitive bound with variable sigma, then specialize to 26. Count validation and traversal separately. Explain whether zero returned slots means no work. Account for the already existing root.

**Completion criteria:** Complete all eight analysis questions and report local space, persistent delta, total stored nodes, and result space separately.

<details>
<summary>Hint 1</summary>

The number of characters visited and number of new objects need not agree.

</details>

<details>
<summary>Hint 2</summary>

Every new child array has sigma initialized reference slots, including null ones.

</details>

<details>
<summary>Hint 3</summary>

Use delta for growth and L for input processing; simplify only after stating sigma is fixed.

</details>

- [ ] **C1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### C2. Many insertions and empty requests

**Difficulty:** Medium. **Objective:** Derive build cost from requests and characters rather than distinct words alone.

**Prerequisites:** C1, aggregate costs.

All words are valid nonnull A keys here. The client has no whole-batch error contract. Analyze a fresh build and a batch added to an existing dictionary.

```java
static long cBuildTape(ArrayNode root, String[] words) {
    long created = 0;
    for (String word : words) {
        created += insertAdded(root, word);
    }
    return created;
}
```

**Test tasks:** D, ["apple","apple","app",""], an empty batch, and 1000 empty words. Check both allocation totals and accepted distinct-word counts with a prefix-set oracle.

**Investigation:** Compare all-distinct words, heavily shared prefixes, repeated apple, and m empty words. State N versus S+1 for a fresh build; for an existing root use an initial N0 and new-character bound. Explain why m matters when S=0.

**Completion criteria:** Give tight enough bounds to distinguish request overhead from structural growth. Explain why duplicate requests still consume character-processing work.

<details>
<summary>Hint 1</summary>

A zero-character request still invokes the operation.

</details>

<details>
<summary>Hint 2</summary>

The returned sum counts new attached nodes, not successful requests.

</details>

<details>
<summary>Hint 3</summary>

For a fresh build, enumerate the union of all nonempty prefixes before substituting an upper bound in S.

</details>

- [ ] **C2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### C3. Repeated literal and prefix queries

**Difficulty:** Medium. **Objective:** Include validation and repeated traversals in a batch analysis.

**Prerequisites:** O2/O3, independent input sizes.

Assume contains and starts meet their full-validation contracts. Each query is a valid literal key. Let q be the number of queries and Q be their total character length.

```java
static int cQueryPairs(ArrayNode root, String[] queries) {
    int successes = 0;
    for (String query : queries) {
        if (contains(root, query)) successes++;
        if (starts(root, query)) successes++;
    }
    return successes;
}
```

**Test tasks:** D with ["app","ap","az",""], then E with the same queries, then a single long path. Replace the final valid query by "z#" to reason about validation, without counting it as an ordinary successful batch.

**Investigation:** Compare a shared endpoint traversal with these two public calls. Do constants change asymptotic growth? Analyze all misses at the first edge with long valid suffixes, all empty queries, and a chain matching every position. Does N multiply this cost?

**Completion criteria:** Distinguish number of calls, full scanned characters, followed edges, and persistent growth. Discuss what changes if domain validity becomes a precondition instead.

<details>
<summary>Hint 1</summary>

The entire query is scanned by validation even if route traversal stops early.

</details>

<details>
<summary>Hint 2</summary>

The batch contains two separate public operations per query.

</details>

<details>
<summary>Hint 3</summary>

Use Q and q; do not assume every query has the maximum length unless giving a deliberately looser bound.

</details>

- [ ] **C3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### C4. Direct lookup versus child enumeration

**Difficulty:** Medium. **Objective:** Differentiate lookup, actual degree, slots, and map capacity.

**Prerequisites:** O5, R2, HashMap documentation.

Array nodes use A; map nodes use M. For cChildReads, c is guaranteed lowercase. Each count refers only to immediate children. Analyze each method and then its use at every visited wildcard state.

```java
static int cChildReads(ArrayNode array, MapNode map, char c) {
    return (array.children[c - 'a'] == null ? 0 : 1)
            + (map.children.get(c) == null ? 0 : 1);
}
static int cArrayChildren(ArrayNode node) {
    int count = 0;
    for (ArrayNode child : node.children) {
        if (child != null) count++;
    }
    return count;
}
static int cMapChildren(MapNode node) {
    int count = 0;
    for (MapNode child : node.children.values()) {
        if (child != null) count++;
    }
    return count;
}
```

**Test tasks:** Nodes with zero, one, two, and 26 children. D root and its appl endpoint. For cChildReads test both present, neither present, and only one representation containing c. Logical child counts agree for equal edges regardless of layout.

**Investigation:** State expected lookup assumptions and map resize/amortization separately. Compare array scans with capacity+size map iteration. Separately create a standalone HashMap<Character,MapNode> with large initial capacity and one entry, then count its values with the same loop. This comparison does not require replacing the scaffold node's final default-growth map.

**Completion criteria:** Explain why "map iteration is always O(actual degree)" needs a capacity qualification, and why unspecified map order affects traces but not existential correctness.

<details>
<summary>Hint 1</summary>

One lookup and an entire child-container iteration are different operations.

</details>

<details>
<summary>Hint 2</summary>

A fixed array visits absent slots too; a hash view may scan empty buckets.

</details>

<details>
<summary>Hint 3</summary>

Keep each node capacity c_v explicit before applying an ordinary-growth assumption.

</details>

- [ ] **C4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### C5. A branching contour and a batch of patterns

**Difficulty:** Medium. **Objective:** Bound explored structure while separating total work from live depth.

**Prerequisites:** W2, T5, recursive costs.

cContour counts all nodes reached at depths zero through remaining, ignoring terminal state and pattern symbols. Remaining is 0..64. cPatternBatch delegates to your completed W2. Explain why the contour provides a useful structural bound but is not itself a word-matching algorithm.

```java
static int cContour(ArrayNode node, int remaining) {
    int count = 1;
    if (remaining == 0) return count;
    for (ArrayNode child : node.children) {
        if (child != null) count += cContour(child, remaining - 1);
    }
    return count;
}
static int cPatternBatch(ArrayNode root, String[] patterns) {
    int matches = 0;
    for (String pattern : patterns) {
        if (wildcardSearch(root, pattern)) matches++;
    }
    return matches;
}
```

**Test tasks:** ["aax","abx","bax","bbx"] with remaining=0,1,2,3,4 and patterns ["..","...","...."]. Q with patterns ["c..e","c..z","...."]. Compare with a single 64-edge path and a fresh root.

**Investigation:** Derive contour calls and slot checks on a full binary prefix tree capped at depth L. Bound by both geometry and N. For W2 add validation and branching-slot costs; compare a fail-all pattern and a first-branch success. Account for b=0 and b=1. Does a batch keep all queries' stacks live together?

**Completion criteria:** Give separate formulas for entered states, maximum frames, and scanned slots; include B and sigma for wildcard work. Reject the claim that L pattern positions imply only L total work.

<details>
<summary>Hint 1</summary>

Branch work adds across calls; peak live stack follows one route.

</details>

<details>
<summary>Hint 2</summary>

Geometric sums count a full possible contour, while N limits the actual available structure.

</details>

<details>
<summary>Hint 3</summary>

Use per-query V and B when summing a batch; early success can visit much less than the contour.

</details>

- [ ] **C5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### C6. Retained suffixes and cumulative copies

**Difficulty:** Medium. **Objective:** Analyze String copying separately from recursion depth.

**Prerequisites:** Java String.substring, recursion memory.

The input is a nonnull lowercase String of length<=64. This analysis subject keeps each nonempty suffix in held until its deeper call returns; held is an initially empty ArrayList supplied by the wrapper. Use the usual OpenJDK 21 proper-suffix copying model. It returns a numeric checksum, not trie membership.

```java
static long cSuffixWeight(String text) {
    return cSuffixWeightFrom(text, new ArrayList<>());
}
static long cSuffixWeightFrom(String suffix, List<String> held) {
    if (suffix.isEmpty()) return 0;
    held.add(suffix);
    long rest = cSuffixWeightFrom(suffix.substring(1), held);
    long answer = rest + held.get(held.size() - 1).length();
    held.remove(held.size() - 1);
    return answer;
}
```

**Test tasks:** Use "", "a", "abcd", and lengths 0..64. The checksum is a testable scalar; also inspect held after the helper returns. Analyze a hypothetical branch at every pattern position separately: copies can multiply across visited branches.

**Investigation:** Count helper calls, maximum helper frames, copied suffix content, retained suffix content excluding the original input, and list entries at peak. Explain why the list makes retention explicit. Describe an index-based alternative without implementing a solution here.

**Completion criteria:** Separate time, frames, peak live auxiliary data, cumulative copied characters, and persistent trie storage (if any). State which copying claim is implementation-dependent.

<details>
<summary>Hint 1</summary>

Suffix lengths decrease by one; the empty suffix does not add a held entry.

</details>

<details>
<summary>Hint 2</summary>

The suspended callers keep all nonempty suffixes accessible in the list.

</details>

<details>
<summary>Hint 3</summary>

Sum lengths of proper nonempty suffixes for copied content; the original input belongs to input storage.

</details>

- [ ] **C6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### Delayed numerical checkpoints

Open only after completing the analysis. These values verify small executions; you must still derive the asymptotic results and assumptions.

<details>
<summary>C1–C6 execution checks</summary>

- C1: successive apple/app/apple/empty calls return 130,0,0,0 new slots.
- C2: D creates 9 nodes; ["apple","apple","app",""] creates 5; an empty batch or 1000 empty strings creates 0.
- C3: D with ["app","ap","az",""] returns 4; E with the same list returns 1.
- C4: D root has 2 children; appl has 2; a leaf has 0. cChildReads returns 0,1,2 according to presence in the two supplied nodes.
- C5: the binary-prefix fixture returns [1,3,7,11,11] for remaining 0..4. Its three-pattern batch has 1 success; the Q batch has 2. The contour at remaining=2 enters 7 states and scans 78 slots; at remaining=3 it enters 11 and scans 182.
- C6: checksum for lengths 0,1,4 is 0,1,10. Length 4 makes 5 helper calls, retains 4 nonempty suffix references at peak, copies 6 proper-suffix units, and restores held to empty.

</details>


## 10. Debugging laboratory

Every program below is deliberately flawed under its stated intended contract. Do not copy it into a working dictionary unchanged. For **every D exercise**: identify each defect; construct a minimal failing input; trace actual behavior; name the violated invariant; repair the implementation in your own scratch file; test the repair and a nonfailing control; and analyze time, auxiliary space, and persistent storage. Supplied witnesses establish that a defect is real, but you must find at least one additional witness.

Unless rejection is explicitly the subject, these low-level snippets receive nonnull roots and already validated lowercase inputs (or W patterns). No null-root or malformed graph tests are required. D7–D9 start at position=0, scan child arrays in a..z order, and use only modest recursive depths. A corrected solution is not included.

### D1. A path appears but a word disappears

**Difficulty:** Easy. **Objective:** Distinguish allocated paths from accepted words.

**Prerequisites:** O1/O2.

Intended behavior: insert a valid A word while preserving earlier words, including empty insertion.

```java
static void dUnmarked(ArrayNode root, String word) {
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int j = word.charAt(i) - 'a';
        if (current.children[j] == null) current.children[j] = new ArrayNode();
        current = current.children[j];
    }
}
```

**Test tasks:** On E, inserting app leaves a present app route but exact membership false; correct behavior requires true. Empty insertion also leaves root nonterminal. Verify node counts can look correct despite this failure.

**Completion criteria:** Complete the seven debugging obligations. Test app/apple in both orders and a duplicate after repair.



- [ ] **D1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D2. Boundary letters reveal an encoding error

**Difficulty:** Easy. **Objective:** Check a character-to-slot mapping against both alphabet endpoints.

**Prerequisites:** F1, O1.

Intended behavior: insert validated lowercase words using slot c-'a'.

```java
static void dShifted(ArrayNode root, String word) {
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int j = word.charAt(i) - 'a' + 1;
        if (current.children[j] == null) current.children[j] = new ArrayNode();
        current = current.children[j];
    }
    current.isWord = true;
}
```

**Test tasks:** On E, inserting a marks the b route under the intended encoding; correct a search is false and b search true. Inserting z throws ArrayIndexOutOfBoundsException for index 26. A repaired method must accept both a and z.

**Completion criteria:** Complete the seven obligations and explain why passing interior-letter tests alone cannot validate an encoding.



- [ ] **D2 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D3. A missing route interrupts a query

**Difficulty:** Easy. **Objective:** Reject absent paths without dereferencing null.

**Prerequisites:** O2.

Intended behavior: exact search for validated literal words; no query mutation.

```java
static boolean dUnchecked(ArrayNode root, String word) {
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        current = current.children[word.charAt(i) - 'a'];
    }
    return current.isWord;
}
```

**Test tasks:** E/"a" throws NPE rather than returning false. With only app, "az" and "apple" throw. An existing app query succeeds, so include both positive and negative routes.

**Completion criteria:** Complete the seven obligations; trace the distinction between a missing last child and a missing child before another loop iteration.



- [ ] **D3 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D4. Two similar APIs disagree for the wrong reason

**Difficulty:** Medium. **Objective:** Audit endpoint interpretation in paired methods.

**Prerequisites:** F3, O2/O3.

Assume endpoint meets F3 exactly. These methods intend exact membership and path-prefix existence respectively.

```java
static boolean dExact(ArrayNode root, String word) {
    return endpoint(root, word) != null;
}
static boolean dPrefix(ArrayNode root, String prefix) {
    ArrayNode found = endpoint(root, prefix);
    return found != null && found.isWord;
}
```

**Test tasks:** With only apple, dExact("app") returns true instead of false; dPrefix("app") returns false instead of true. On E, empty exact incorrectly returns true and empty prefix incorrectly returns false.

**Completion criteria:** Complete the seven obligations, keeping traversal shared while deriving each public postcondition separately.

<details>
<summary>Hint 1</summary>

A route and an inserted whole word are different facts.

</details>

<details>
<summary>Hint 2</summary>

Check the final interpretation after all input symbols have been consumed.

</details>

<details>
<summary>Hint 3</summary>

Test one nonterminal existing endpoint and the root before changing the traversal code.

</details>

- [ ] **D4 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D5. A later update changes earlier answers

**Difficulty:** Medium. **Objective:** Protect identities and empty-word behavior during insertion.

**Prerequisites:** O1.

Intended behavior: preserve all accepted A words and make duplicate insertion idempotent.

```java
static void dReplace(ArrayNode root, String word) {
    if (word.isEmpty()) return;
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int j = word.charAt(i) - 'a';
        current.children[j] = new ArrayNode();
        current = current.children[j];
    }
    current.isWord = true;
}
```

**Test tasks:** Fresh app→apple loses app membership; apple→app loses apple. Repeating apple replaces identities even though final boolean membership is unchanged. Empty insertion never marks root. Detect both logical and structural failures.

**Completion criteria:** Complete the seven obligations and compare before/after snapshots for a duplicate and a shorter-word insertion.

<details>
<summary>Hint 1</summary>

A linked child may already own many accepted words.

</details>

<details>
<summary>Hint 2</summary>

The endpoint of an empty route is still a real node.

</details>

<details>
<summary>Hint 3</summary>

Distinguish deciding whether to allocate from deciding which endpoint marker to update.

</details>

- [ ] **D5 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D6. A rejected request leaves observable state

**Difficulty:** Medium. **Objective:** Enforce the documented validation boundary.

**Prerequisites:** O1/O4.

Intended behavior: reject null or non-lowercase input under A without any changed link or flag.

```java
static void dLateValidation(ArrayNode root, String word) {
    Objects.requireNonNull(word);
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        char c = word.charAt(i);
        if (c < 'a' || c > 'z') throw new IllegalArgumentException("invalid");
        int j = c - 'a';
        if (current.children[j] == null) current.children[j] = new ArrayNode();
        current = current.children[j];
    }
    current.isWord = true;
}
```

**Test tasks:** On E, "ab#" throws IAE but leaves a and ab reachable. starts("ab") has changed from false to true despite rejection. Null and a first-character "#" reject without mutation, so those tests alone miss the defect.

**Completion criteria:** Complete the seven obligations. Repair without cloning the whole dictionary, then test an invalid suffix after both an existing and a newly created prefix.

<details>
<summary>Hint 1</summary>

A thrown exception does not undo earlier field writes.

</details>

<details>
<summary>Hint 2</summary>

No observable change is allowed before the full input is accepted.

</details>

<details>
<summary>Hint 3</summary>

Compare the point of validation with the first allocation, and separate their phases.

</details>

- [ ] **D6 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D7. Exhaustion accepts an unfinished key

**Difficulty:** Medium. **Objective:** Check the recursive base against whole-word meaning.

**Prerequisites:** W2.

Intended behavior: match the whole W pattern to a stored whole word.

```java
static boolean dExhausted(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) return true;
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode next = node.children[c - 'a'];
        return next != null && dExhausted(next, pattern, position + 1);
    }
    for (ArrayNode next : node.children) {
        if (next != null && dExhausted(next, pattern, position + 1)) return true;
    }
    return false;
}
```

**Test tasks:** With only cane, "ca" and "c." return true incorrectly. E/empty also returns true incorrectly. A full cane pattern still returns true and a missing literal route can still return false.

**Completion criteria:** Complete the seven obligations and test a terminal internal prefix as a positive control after repair.

<details>
<summary>Hint 1</summary>

A complete consumed route need not be a stored key.

</details>

<details>
<summary>Hint 2</summary>

State exactly what no remaining pattern positions means.

</details>

<details>
<summary>Hint 3</summary>

Trace the current node flag at the base before changing any branching logic.

</details>

- [ ] **D7 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D8. The first candidate decides everything

**Difficulty:** Medium. **Objective:** Preserve completeness after a failed wildcard continuation.

**Prerequisites:** W2, T4.

Intended behavior: return existence over every eligible continuation, stopping early only when justified.

```java
static boolean dFirst(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) return node.isWord;
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode next = node.children[c - 'a'];
        return next != null && dFirst(next, pattern, position + 1);
    }
    for (ArrayNode next : node.children) {
        if (next != null) return dFirst(next, pattern, position + 1);
    }
    return false;
}
```

**Test tasks:** Q/"c..e" returns false after cab fails, missing cane and cape. Q/"c.." returns true because its first candidate cab works. Q/"c..z" is correctly false but does not prove completeness.

**Completion criteria:** Complete the seven obligations, drawing the exact unvisited subtree in the false-negative case.

<details>
<summary>Hint 1</summary>

An existence result is true if any candidate works.

</details>

<details>
<summary>Hint 2</summary>

One failed branch disproves only the words below that child.

</details>

<details>
<summary>Hint 3</summary>

Check the location of the failure decision relative to the whole candidate loop.

</details>

- [ ] **D8 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

### D9. A wildcard advances beyond its contract

**Difficulty:** Medium. **Objective:** Maintain one consumed symbol per child edge and a reachable base.

**Prerequisites:** W2, termination measures.

Intended behavior: dot consumes exactly one lowercase letter. Valid inputs and initial position=0 are guaranteed.

```java
static boolean dSkip(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) return node.isWord;
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode next = node.children[c - 'a'];
        return next != null && dSkip(next, pattern, position + 1);
    }
    for (ArrayNode next : node.children) {
        if (next != null && dSkip(next, pattern, position + 2)) return true;
    }
    return false;
}
```

**Test tasks:** With only a, ".." returns true even though a has length one. The same dictionary with "." throws StringIndexOutOfBoundsException after position jumps beyond length. Literal "a" remains a positive control.

**Completion criteria:** Complete the seven obligations. Explain why merely changing the base to position>=length can hide the exception while preserving incorrect matching semantics.

<details>
<summary>Hint 1</summary>

Compare consumed pattern positions with the number of followed edges.

</details>

<details>
<summary>Hint 2</summary>

The base depends on an invariant that the index never passes the end.

</details>

<details>
<summary>Hint 3</summary>

Fix the state transition to match the defined symbol consumption rather than masking an out-of-range index.

</details>

- [ ] **D9 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.


## 11. Board-search reasoning: optional

This section and P212 are **optional advanced material**. Study [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking) first; attempt full Word Search II only when both Tries and Backtracking are comfortable. None of this section is required for ordinary core Tries readiness. The [conceptual preview](./Tries_Essential_Concepts.md#13-trie-guided-board-search-optional-advanced-material) supplies background, not a full solution.

### B1. Audit legal routes and restored state

**Difficulty:** Medium. **Objective:** Separate dictionary prefixes, current board routes, and unique output.

**Prerequisites:** Core Tries plus Backtracking; optional.

Reason on the following board and dictionary. Use orthogonal neighbors only, no cell reuse within one word path, lowercase letters, and distinct output words. Dictionary duplicates are allowed but count once. The board and dictionary must be unchanged after a query; repeated queries must produce the same word set. Empty dictionary words are excluded here.

```text
       col 0 1
row 0      a b
row 1      b a

dictionary = ["ab","aba","abab","ababa","aa","b","ab"]
```

Draw the dictionary before exploring routes. No complete board-search implementation is required.

**Test tasks:** Expected unique set is {ab,aba,abab,b}, in no required order. aa is impossible because equal letters are diagonal; ababa cannot use five distinct cells on a four-cell board. A one-cell board a with dictionary ["a","aa"] finds only a. An empty dictionary finds nothing. Multiple routes for ab must not duplicate output.

**Investigation:** Show one route per found word, one pruned prefix, a boundary rejection, and a cell-reuse rejection. Explain which visited marks are local to a path and must be restored. Explain why finding aba does not end exploration of longer words. Compare result-set deduplication with clearing terminal flags when the dictionary must remain reusable.

**Completion criteria:** Answer every conceptual question, supply legal coordinate paths, and justify prefix pruning without claiming it guarantees polynomial time. Name board size, maximum route length, visited storage, recursion, trie storage, and output separately.

<details>
<summary>Hint 1</summary>

A terminal dictionary node can still have descendants.

</details>

<details>
<summary>Hint 2</summary>

Board visitation describes the current route, not every route explored so far.

</details>

<details>
<summary>Hint 3</summary>

Any optimization that edits terminal flags must be reconciled with the repeatable read-only query contract.

</details>

- [ ] **B1 attempt** — Last: —; Result: —; Minutes: —; Next review: —; Main mistake / lesson: —.

## 12. Interview communication

Use the follow-ups below with an already selected exercise; they are prompts for its existing attempt record, not new exercises or a second mock tracker. In a roughly 35–45-minute mock, spend a few minutes clarifying and proposing a baseline, then implement, test, and explain costs. Adjust timing to the actual interview format and difficulty.

| Selected task | Explain aloud | Mock-interviewer follow-up |
| --- | --- | --- |
| F1/O1 | Node meaning, shared identities, terminal independence, insertion invariant | Can a distinct newly stored word allocate zero nodes? |
| O2/O3 | Why missing edges reject; why endpoint interpretation differs | What is the empty-prefix answer in an empty dictionary, and which contract made it so? |
| O4 | The boundary between validation and mutation | Why is validating one word at a time insufficient for this batch promise? |
| O5/R1 | Domain, array/map layout, char versus code point | What changes if dot must match one emoji rather than one UTF-16 unit? |
| W2/T4 | Recursive promise, one-symbol consumption, branch combination, termination | Why can you stop after one success but not after one failure? |
| W3 | Unique terminal endpoints and aggregate results | Does duplicate insertion change this count? When would you need different metadata? |
| C5 | Total explored states, slot work, and maximum simultaneous frames | Show a failing all-dot family that defeats a universal O(L) claim. |
| X1/X6 | Baseline, workload, maintained state, correctness, edge tests | What input would falsify your stated invariant? |
| X2/X3/X5 | Exact membership/count or one-off workload | What benefits would justify paying for prefix nodes here? |
| X4 | Ordered candidate selection and character comparison costs | Does O(log n) comparisons mean O(log n) character work? |

A strong explanation connects the chosen state to the required answer, gives initialization/preservation/end reasoning or a recursive induction, identifies progress, and derives costs from actual work. Do not memorize model answers. If the interviewer changes the empty, duplicate, alphabet, mutation, or ordering policy, revise the contract before editing code.

## 13. Cold review and spaced repetition

Follow the roadmap's [flexible review approach](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): approximately **1, 3, 7, and 14 days** after learning, adjusted to retention. A missed date does not restart the course.

Open only the statement and starter signature. Hide theory code, hints, previous attempts, and technique labels. Reconstruct the baseline, selection, state, proof, and costs; implement and test from scratch. Compare afterward and update the existing ID's record with the actual date, I/H/S result, minutes, next review, and the main lesson. A remembered solution is not an unfamiliar transfer demonstration.

Prioritize unfinished/S attempts, then hint-assisted attempts, incorrect structure or terminal invariants, wildcard branch/base/index errors, complexity mistakes, and unresolved edge cases. A short isolated drill can repair the prerequisite before retrying the larger task. After repeated independent cold success, increase the gap and use a different unseen variant.

Keep one history per original exercise beneath its existing record. Curated history stays in its [roadmap problem Log](./DSA_Interview_Roadmap.md#8-tries); the workbook pointer refers there. Do not copy rows into a revision ledger or maintain a second category dashboard.

## 14. Completion and readiness evidence

These milestones describe practical evidence, not an automatic award of T/I/P/R. Keep the actual category gate decisions and dates in the [roadmap](./DSA_Interview_Roadmap.md#four-independent-progress-gates).

- [ ] **Fundamentals:** explain node/edge/prefix/terminal meaning and child-slot defaults without notes; demonstrate F1–F3 and T1.
- [ ] **Manual construction:** draw a new shared-prefix dictionary before coding, then verify identities, node counts, flags, and child slots.
- [ ] **Insertion:** complete O1/O4 cold, preserve earlier words and duplicate identities, and reject invalid requests without structural change.
- [ ] **Exact search:** implement O2 independently, including a nonterminal prefix, missing child, and empty membership.
- [ ] **Prefix search:** implement O3 independently and explain its distinction from exact and existential empty-prefix policies.
- [ ] **Representation:** implement O5 and defend R1–R3 for lowercase, custom alphabets, and broader text; state UTF-16 limitations.
- [ ] **Wildcard DFS:** derive W1/W2 and W3, trace both unsuccessful and successful continuations, and explain the consumed-prefix invariant and termination.
- [ ] **Complexity:** complete C1–C6; separate L/S/m/N/b/sigma, validation, branching, depth, copies, output, and persistent storage.
- [ ] **Essential practice:** independently solve or cold re-solve P208 and P211 with tests, reasoning, and honest assistance records.
- [ ] **Unfamiliar transfer:** solve changed X-style variants without hints, including at least one appropriate non-trie selection, across separate sessions.
- [ ] **Interview readiness:** repeat the complete common rubric with an unseen dictionary variant; communicate assumptions, baseline, design, correctness, Java choices, edge cases, and costs.

For **T**, explain the core concepts without consulting text. For **I**, implement the core insertion/exact/prefix and wildcard drills from a blank editor and test them. For **P**, use independent or cold Essential-problem evidence. For **R**, apply the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) and the Tries-specific demonstration on unfamiliar variants in separate sessions.

An exercise checkbox records an attempt, not mastery. Hint-assisted success remains H until a later independent attempt. If a cold review exposes a conceptual gap, follow the roadmap's Reviewing rules and reconsider the affected gate. **P212 and B1 are optional and never prerequisites for ordinary Tries readiness.**

## 15. References and author validation

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Tries](./DSA_Interview_Roadmap.md#8-tries), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category), and [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking).
- [Tries Essential Concepts](./Tries_Essential_Concepts.md): [invariants](./Tries_Essential_Concepts.md#4-structural-and-dictionary-invariants), [array/map storage](./Tries_Essential_Concepts.md#8-array-children-and-map-children), [wildcard correctness](./Tries_Essential_Concepts.md#10-recursive-contracts-and-correctness), and [cost analysis](./Tries_Essential_Concepts.md#11-time-storage-and-branching-complexity).
- [Trees Essential Concepts](./Trees/Trees_Essential_Concepts.md).
- [Recursion Essential Concepts](./Recursion/Recursion_Essential_Concepts.md): [contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness), [branching](./Recursion/Recursion_Essential_Concepts.md#9-multiple-branch-recursion), and [complete space](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space).
- [Java Strings](./Java_String.md), [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md), and [Java Arrays](./Java_Array.md).
- [Arrays & Hashing Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md).

### Primary references

Java API details were checked with the Context7 version-21 Oracle documentation collection. Unrelated retrieved classes were not used to justify String or HashMap claims.

- [Java 21 String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html): UTF-16 indexing, String comparison, and prefix operations.
- [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html): expected lookup assumptions, unspecified ordering, and capacity-sensitive iteration.
- [OpenJDK 21 String](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/String.java) and [StringLatin1](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/StringLatin1.java): implementation context for copying proper nonempty suffixes.
- The official LeetCode links under P208/P211/P212 identify the current titles/difficulties, checked **2026-10-29**. The local roadmap determines E/C priorities. The workbook does not reproduce full platform statements or solutions.

### Author validation

Completed **2026-10-29**. All **33 Java blocks**, containing **41 methods** plus the shared node/record and wrapper declarations, were assembled and compiled with Java 21 using --release 21 -Xlint:all -Werror. The original starter methods remain TODOs; no completed learner implementations or separate solutions document were added.

All **94 published coding-test rows** were checked against independent specification oracles. These used distinct-prefix sets, direct whole-word/position comparisons, pairwise repetition checks, event summation, linear catalog scans, and legal board-route enumeration. The published JSON cells were parsed back from this document and compared with the checked cases. The checks also covered insertion counts, terminal counts, raw UTF-16 cases, empty-policy differences, exact-one-change matching, long aggregate totals, and the optional board's expected set.

Runtime checks passed **95,948 assertions** across **148 dictionary cases**, including all 128 subsets of the seven a/b words of lengths zero through two and 20 deterministic insertion histories. There were **82,568 literal/prefix/wildcard query comparisons** against independent word-set oracles, **56 directly generated published Java cases**, and **161,051 observed wildcard helper entries**. Snapshots checked child identities, terminal flags, unique routes, duplicate preservation, and query immutability.

The analysis clients were executed using test-only fixture helpers that satisfy their declared dependencies. This validates the clients and expected behavior; it does not claim that an unfinished learner method has passed tests. Instrumentation checked **11 recorded trace/contour metrics**, including the c..e/c..z call counts and slot scans, binary-prefix failure/success, and contour counts. Suffix lengths 0..64 verified returned checksums, calls, retained-list restoration, and logical copy counts under the stated copying model. Array and raw-char map fixture traversal also handled a **100000-character iterative path**. These checks do not establish a portable recursive depth limit or JVM heap-byte measurements.

All **nine debugging subjects** were executed with **30 failing-witness/control checks**, reproducing the documented missing-marker, shifted-index, null-child, endpoint-interpretation, overwritten-path, partial-validation, and wildcard base/branch/advancement defects. No corrected solutions are supplied in the workbook.

The content audit verified **41 original IDs**, **17 coding specifications**, **93 optional hints**, **44 initially unchecked attempt records**, the three exact E/E/C roadmap entries, code fences, and **68 local links** including heading anchors. SHA-256 comparison confirmed all **33 pre-existing guide files** remained unchanged. Only **Tries_Exercises.md** was created in DSA_Guide; validation artifacts were kept outside the repository. No missing dependency or unresolved validation issue remains.
