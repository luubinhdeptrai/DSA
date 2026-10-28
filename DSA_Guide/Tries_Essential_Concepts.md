# Tries — Essential Concepts (Java 21)

A trie shares the paths for common string prefixes. Keep its central distinction explicit: **a path says that a prefix exists; a terminal flag says that this exact word was inserted**.

## Contents

- [1. Scope and prerequisites](#1-scope-and-prerequisites)
- [2. Prefix paths and shared structure](#2-prefix-paths-and-shared-structure)
- [3. Java nodes and input policies](#3-java-nodes-and-input-policies)
- [4. Structural and dictionary invariants](#4-structural-and-dictionary-invariants)
- [5. Deriving insertion](#5-deriving-insertion)
- [6. Deriving exact-word search](#6-deriving-exact-word-search)
- [7. Prefix search and the public API](#7-prefix-search-and-the-public-api)
- [8. Array children and map children](#8-array-children-and-map-children)
- [9. Wildcard search using DFS](#9-wildcard-search-using-dfs)
- [10. Recursive contracts and correctness](#10-recursive-contracts-and-correctness)
- [11. Time, storage, and branching complexity](#11-time-storage-and-branching-complexity)
- [12. Choosing a trie or an alternative](#12-choosing-a-trie-or-an-alternative)
- [13. Trie-guided board search: optional advanced material](#13-trie-guided-board-search-optional-advanced-material)
- [14. Java details and ownership](#14-java-details-and-ownership)
- [15. Mistakes and counterexamples](#15-mistakes-and-counterexamples)
- [16. Pattern recognition and derivation](#16-pattern-recognition-and-derivation)
- [17. Self-assessment and answer key](#17-self-assessment-and-answer-key)
- [18. Key takeaways and readiness](#18-key-takeaways-and-readiness)
- [19. References and validation](#19-references-and-validation)

## 1. Scope and prerequisites

Tries is an official category in **Phase 3 — Hierarchies and Search Spaces** of the [DSA Interview Roadmap](./DSA_Interview_Roadmap.md#8-tries). That roadmap determines scope, prerequisites, priorities, and progress. It places Tries after Trees; Backtracking is an additional prerequisite for optional board search.

Core knowledge includes insertion, exact membership, prefix lookup, terminal markers, array/map representation, wildcard branching, correctness, and complete cost analysis. Board search receives a conceptual preview rather than a full Hard-problem implementation. Deletion, compressed/radix tries, suffix structures, ranked autocomplete, and concurrent indexes are extensions.

The objective is to derive unfamiliar dictionary operations from node meanings. Explain shared prefixes, successful and missing paths, wildcard state, character assumptions, and actual memory/work. Preparation of prerequisite documents is not evidence of mastery.

| Demonstrate first | Reference | Used here for |
| --- | --- | --- |
| Arrays contain initially null object references | [Java Arrays](./Java_Array.md#2-declare-create-and-initialize) | Child slots and character indexes |
| String positions are UTF-16 code units | [Java Strings](./Java_String.md#indexes-length-and-traversal) | Edge/wildcard character model |
| Missing map get and expected key costs | [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) | Sparse child maps |
| Membership versus counts; meaningful stored state | [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#2-foundations-refresher-and-cost-model) | Selecting an index |
| References, identity, and unique root routes | [Trees](./Trees/Trees_Essential_Concepts.md#4-java-treenode-references-and-structure) | Labeled trees with more than two children |
| Exact recursive promises and sequential branches | [Recursion](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) | Wildcard DFS |
| Work, live frames, allocation, and output | [Complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), [recursive costs](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) | Honest cost analysis |

The [T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain separate: explanation supports T, tested blank-editor implementation supports I, independent/cold Essential solves support P, and unfamiliar transfer across sessions supports R. Keep histories and readiness evidence in the existing roadmap. This guide adds no progress dashboard.

| Roadmap objective | Sections |
| --- | --- |
| Edges as characters, nodes as prefixes, terminal flags | 2–4 |
| Insert/search/startsWith, duplicate/empty policies | 5–7 |
| Arrays versus maps, character assumptions | 3 and 8 |
| Wildcard branching, contracts, costs | 9–11 |
| HashSet and other alternatives | 12 and 16 |
| Optional board prefixes and state restoration | 13 |
| Java pitfalls and independent readiness | 14–18 |

## 2. Prefix paths and shared structure

### 2.1 Why a prefix tree exists

Exact membership asks whether one complete string is stored. Prefix existence asks whether a route begins with a shorter sequence. A dictionary containing app, apple, apply, and bat can share the beginning of its three app words:

```text
(root: "")
+-- a                         prefix "a"
|   +-- p                     prefix "ap"
|       +-- p*                stored "app"
|           +-- l             prefix "appl"
|               +-- e*        stored "apple"
|               +-- y*        stored "apply"
+-- b
    +-- a                     prefix "ba"
        +-- t*                stored "bat"

* means isWord == true; it is notation, not an edge.
```

The root represents the empty prefix. Each edge appends one character; the entire root route identifies a node's prefix. The two a edges reach different nodes, for a and ba. All app words instead reuse the same a→p→p route.

An ordinary, uncompressed **trie**, or **prefix tree**, uses one character per edge. A node need not store the entire prefix String: its route already supplies that meaning. Shared structure exposes relationships among keys without repeatedly comparing their full strings.

### 2.2 Words and prefixes are different facts

app is terminal and has a child. appl has children but is not stored. bat is a terminal leaf. A terminal node can therefore be internal. Under this insertion-only implementation every nonroot leaf is terminal, but terminal does not imply leaf.

| Query on these four words | Result | Reason |
| --- | --- | --- |
| search("app") | true | Its endpoint is terminal |
| search("ap") | false | Present path, nonterminal endpoint |
| startsWith("ap") | true | Entire prefix path exists |
| search("apple") | true | Terminal endpoint |
| startsWith("app") | true | A word can also be a prefix |
| startsWith("az") | false | No z below a |
| search("application") | false | No i below appl |

### 2.3 Count what is actually shared

The words have 16 total character occurrences but only nine distinct nonempty prefixes. Thus N=10 nodes including root, nine nonnull edges, and four terminal flags. Separate unshared paths under one root would use 17 nodes.

With a 26-slot array at **every** node, those ten nodes allocate **260 reference slots**. A null slot is allocated storage but is not a child object. Sharing prefixes reduces structural paths; it does not establish that this Java structure occupies fewer bytes than four Strings.

A trie chooses the edge for the next character position. A BST compares ordered keys and chooses a subtree. These are different invariants. A single length-L word gives a trie route of L edges without any balancing requirement.

**Self-check Q1:** What prefix does the second a represent?

**Self-check Q2:** Why do search("ap") and startsWith("ap") differ?

**Self-check Q3:** Why is terminal different from leaf?

## 3. Java nodes and input policies

### 3.1 One slot per lowercase English letter

All Java blocks are members of one scratch `TrieExamples` class with `import java.util.*;` at file scope. Nested classes, helpers, demonstrations, and deliberately faulty subjects can coexist. No external dependency is required.

```java
static final class ArrayNode {
    final ArrayNode[] children = new ArrayNode[26];
    boolean isWord;
}
```

Each node owns a new array. `final` prevents rebinding that field; elements remain mutable. Reference slots start null, and the boolean starts false. Creating the array does **not** allocate 26 child nodes. [Array defaults](./Java_Array.md#default-element-values)

```text
index = c - 'a'
'a' -> 0
'p' -> 15
'z' -> 25
'A' -> -32   invalid
'{' -> 26    invalid
```

The subtraction produces an int whose validity follows from the lowercase-English domain. It is not a universal character encoding. A newly attached child has its own empty array and false terminal flag. Moving a local current reference to that child does not copy the subtree.

### 3.2 Explicit API policies

| Concern | Array trie / wildcard index | Map trie in Section 8 |
| --- | --- | --- |
| Literal keys | Only a..z, case-sensitive | Any sequence of UTF-16 char values, case-sensitive |
| Null String | NullPointerException | NullPointerException |
| Other literal characters | IllegalArgumentException | Accepted |
| Normalization | None | None |
| Empty word | Allowed; marks root | Same |
| Duplicate insertion | Idempotent membership, no extra count | Same |
| Exact empty query | True iff empty word inserted | Same |
| Empty prefix | Always true: root path exists | Same |
| Pattern syntax | Dot matches exactly one lowercase letter | No wildcard API in map example |
| Queries | Read-only, allocate no trie nodes | Same |

Here startsWith means **path existence**, including the empty root route. Consequently `startsWith("")` is true even before any word is stored. If an application instead requires “at least one stored word has this prefix,” that empty-dictionary case needs a different answer and supporting state. Do not silently mix policies. Read platform constraints separately.

An empty trie has a nonnull root and no terminal markers. Low-level helper roots below are nonnull valid roots made by this implementation. Clients normally use the wrapper. Calls are single-threaded; no deletion, external rewiring, cycles, or shared child objects are permitted.

### 3.3 Validate before mutation

```java
static void requireLowercase(String text) {
    Objects.requireNonNull(text, "text");
    for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (c < 'a' || c > 'z') {
            throw new IllegalArgumentException("only a..z are allowed");
        }
    }
}
```

Checking the whole input first prevents `insert("ab#")` from allocating a/ab and only then rejecting #. Such partial mutation could change prefix answers even if no terminal word was added. Prevalidation preserves structure for the specified invalid-input exceptions.

Queries also validate first: an invalid suffix is rejected even if an earlier edge is missing. Thus public method time includes a full input scan. With a judge-guaranteed domain, validation can instead be a stated precondition; analyze the actual code selected.

**Self-check Q4:** How many child objects does new ArrayNode() create?

**Self-check Q5:** Why check the domain before indexing?

**Self-check Q6:** How can a failed insertion alter prefix behavior if validation happens too late?

## 4. Structural and dictionary invariants

These are stable invariants at completed public-operation boundaries. During insertion, the loop can temporarily build prefixes of the new word before its endpoint is marked; the loop invariant tracks that in-progress route separately.

1. Root denotes the empty prefix.
2. Child slot j appends `(char)('a'+j)` to the parent's prefix.
3. Every nonroot object has one incoming child edge, so its root route uniquely identifies its prefix.
4. isWord is true exactly for successfully inserted complete paths, including an optional empty word.
5. Insertion never replaces an existing edge or clears a terminal flag; earlier words remain stored.
6. In this insertion-only structure, every nonroot path is a prefix of an accepted word. Queries cannot create paths.

Inserting apple does not insert a, ap, app, and appl as separate words. It establishes those paths and marks only the final endpoint:

```text
After only apple:
"" -> a -> ap -> app -> appl -> apple*

After additionally app:
"" -> a -> ap -> app* -> appl -> apple*
                 ^
           one flag changes
```

For a traversal loop, the invariant is: **before processing position i, current denotes exactly the first i characters**. Initialization uses root and i=0. Following/creating the next edge preserves the invariant. At i=L, current identifies the complete input endpoint. Whether that endpoint is accepted depends on the operation.

This reuses [processed-prefix reasoning](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#3-traversal-transformation-and-processed-prefixes), with prefix paths rather than a processed array segment.

**Self-check Q7:** Does inserting apple automatically store app?

**Self-check Q8:** Why is comparison with a newly allocated node not a word-membership test?

## 5. Deriving insertion

### 5.1 Establish a path without losing earlier paths

Start at root. For each character, reuse its child if present; otherwise allocate and attach a child. Reuse is required because the existing child already denotes that prefix and may own terminal flags/longer words. After the last character, mark the endpoint.

```java
static void insertWord(ArrayNode root, String word) {
    requireLowercase(word);
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int index = word.charAt(i) - 'a';
        if (current.children[index] == null) {
            current.children[index] = new ArrayNode();
        }
        current = current.children[index];
    }
    current.isWord = true;
}
```

The root is a nonnull valid root as specified above. Empty input skips the loop and marks root. Each operation validates before changing flags or links.

### 5.2 First insertion: app

| Position | Character / slot | Prefix before | Child exists? | Action | Prefix after |
| --- | --- | --- | --- | --- | --- |
| 0 | a / 0 | "" | no | Allocate | a |
| 1 | p / 15 | a | no | Allocate | ap |
| 2 | p / 15 | ap | no | Allocate | app |
| End | — | app | — | Set terminal true | app* |

```text
(root)
+-- a
    +-- p
        +-- p*
N=4; new nodes=3.
```

### 5.3 Longer word sharing app: apple

| Position | Character / slot | Prefix before | Child exists? | Action | Prefix after |
| --- | --- | --- | --- | --- | --- |
| 0 | a / 0 | "" | yes | Reuse | a |
| 1 | p / 15 | a | yes | Reuse | ap |
| 2 | p / 15 | ap | yes | Reuse; preserve terminal | app* |
| 3 | l / 11 | app | no | Allocate | appl |
| 4 | e / 4 | appl | no | Allocate | apple |
| End | — | apple | — | Set terminal true | apple* |

```text
(root)
+-- a
    +-- p
        +-- p*
            +-- l
                +-- e*
N=6; this insertion creates 2 nodes.
```

### 5.4 A new sibling branch: apply

| Position | Character / slot | Prefix before | Child exists? | Action | Prefix after |
| --- | --- | --- | --- | --- | --- |
| 0 | a / 0 | "" | yes | Reuse | a |
| 1 | p / 15 | a | yes | Reuse | ap |
| 2 | p / 15 | ap | yes | Reuse | app* |
| 3 | l / 11 | app | yes | Reuse | appl |
| 4 | y / 24 | appl | no | Allocate | apply |
| End | — | apply | — | Set terminal true | apply* |

```text
(root)
+-- a
    +-- p
        +-- p*
            +-- l
                +-- e*
                +-- y*
N=7; this insertion creates 1 node.
```

bat subsequently adds b/ba/bat, giving the Section 2 dictionary. Existing e/y branches stay intact.

### 5.5 Duplicate, shorter, and empty insertions

Each row continues from the previous row:

| Insertion | New nodes | Total nodes | Newly stored word |
| --- | --- | --- | --- |
| app into fresh root | 3 | 4 | app |
| apple | 2 | 6 | apple |
| apply | 1 | 7 | apply |
| bat | 3 | 10 | bat |
| apple again | 0 | 10 | none |
| ap | 0 | 10 | ap |
| "" | 0 | 10 | empty word |

If apple is inserted first, inserting app later also adds zero nodes and marks the existing internal endpoint. Duplicate insertion is idempotent for membership. A multiset needs an explicit terminal count; a boolean cannot track how often a word arrived.

### 5.6 Correctness and costs

Initialization identifies the empty prefix. Every iteration selects the unique edge for the next character, creating it only when absent. This preserves existing links and flags and establishes the next prefix. At termination the endpoint denotes exactly the whole word; marking it establishes membership. The counter increases to L, proving termination.

For fixed 26-slot nodes, validation/traversal/allocation take **Θ(L+1)** time. Iterative local working space is O(1). Persistent index growth is **Δ nodes and 26Δ reference slots**, where 0≤Δ≤L; one insertion can add O(L) stored space. A duplicate has Δ=0.

For variable alphabet size σ, new arrays must initialize σ slots: use O(L+σΔ) time and O(σΔ+Δ) structural growth. The ordinary O(L) claim treats σ=26 as constant. “O(1) local space” never means “no index growth.”

**Self-check Q9:** Why reuse an existing child?

**Self-check Q10:** How can a distinct new word allocate zero nodes?

**Self-check Q11:** Which memory is excluded when saying insertion has constant local working space?

## 6. Deriving exact-word search

### 6.1 Find the path, then read its terminal flag

Search follows existing edges and never creates them. A missing edge disproves the requested full route: a stored word would have established it during insertion. A present route proves only a prefix; exact search also needs its terminal flag.

```java
private static ArrayNode followValidated(ArrayNode root, String text) {
    ArrayNode current = root;
    for (int i = 0; i < text.length(); i++) {
        int index = text.charAt(i) - 'a';
        current = current.children[index];
        if (current == null) {
            return null;
        }
    }
    return current;
}

static boolean containsWord(ArrayNode root, String word) {
    requireLowercase(word);
    ArrayNode endpoint = followValidated(root, word);
    return endpoint != null && endpoint.isWord;
}
```

The private route helper assumes validated lowercase text and a valid nonnull root. A missing child returns before the next dereference. Empty text returns root directly.

### 6.2 Exact-search dry runs

Use the original four-word dictionary, before adding ap or the empty word:

| Query | Traversal character positions | Endpoint / failure | Terminal? | Result |
| --- | --- | --- | --- | --- |
| app | 0:a, 1:p, 2:p | app | true | true |
| apple | 0:a, 1:p, 2:p, 3:l, 4:e | apple | true | true |
| ap | 0:a, 1:p | ap | false | false |
| application | 0:a, 1:p, 2:p, 3:l, 4:i | Missing i below appl | — | false |
| az | 0:a, 1:z | Missing z below a | — | false |
| "" | No edges | Root | false | false |

```text
application:
root -> a -> ap -> app* -> appl
                             \
                              i absent -> false
```

application has eleven characters. Route finding stops at position 4, but prevalidation already scanned the whole input. An early missing edge reduces traversal work, not the chosen public API's complete validation scan.

### 6.3 Correctness and cost

The prefix invariant identifies the only possible query endpoint. Missing-edge rejection follows from insertion's path invariant. A present endpoint is accepted only when its terminal flag records this complete path, preventing false positives such as ap.

Time is Θ(L+1) with prevalidation, O(1) auxiliary space, and scalar output. Under a guaranteed domain, omit validation and the route can stop early; worst-case remains O(L+1). No nodes or substrings are allocated. Empty exact search is true precisely after root is marked by an empty insertion.

**Self-check Q12:** What decides membership after every edge has been followed?

**Self-check Q13:** Why can application fail early while total method work still includes eleven characters?

## 7. Prefix search and the public API

Prefix lookup needs a present endpoint and ignores its terminal flag. This shares route finding with exact search while interpreting the result differently.

```java
static boolean hasPrefix(ArrayNode root, String prefix) {
    requireLowercase(prefix);
    return followValidated(root, prefix) != null;
}

static final class LowercaseTrie {
    private final ArrayNode root = new ArrayNode();

    public void insert(String word) {
        insertWord(root, word);
    }

    public boolean search(String word) {
        return containsWord(root, word);
    }

    public boolean startsWith(String prefix) {
        return hasPrefix(root, prefix);
    }
}
```

The wrapper owns a fresh root for each instance. Clients receive dictionary operations rather than mutable nodes. Wrapping adds constant call overhead; it does not change the earlier contracts.

| Prefix query on app/apple/apply/bat | Endpoint | Endpoint terminal? | Result |
| --- | --- | --- | --- |
| ap | ap | false | true |
| app | app | true | true |
| appl | appl | false | true |
| apple | apple | true | true |
| applea | Missing child below apple | — | false |
| b | b | false | true |
| "" | Root | false | true |

### 7.1 Executable demonstration

```java
static List<Boolean> basicDemo() {
    LowercaseTrie trie = new LowercaseTrie();
    trie.insert("app");
    trie.insert("apple");
    trie.insert("apply");
    trie.insert("bat");
    return List.of(
            trie.search("app"),
            trie.search("ap"),
            trie.startsWith("ap"),
            trie.search("application"),
            trie.search(""),
            trie.startsWith(""));
}
```

Result: `[true,false,true,false,false,true]`. List.of is an immutable demonstration result; the trie operations themselves return booleans.

### 7.2 Correctness, empty prefixes, and limitations

A nonnull endpoint means the full prefix route exists by the common traversal invariant. In this insertion-only structure every nonempty route belongs to an accepted word. Root existence is the explicit empty-prefix special case, including on a fresh dictionary.

For prefix length P, time is Θ(P+1) with validation and auxiliary space O(1). No terminal check is needed. This boolean API does not enumerate completions, rank suggestions, or count matching words. Those features need additional traversal/metadata and must include output costs.

**Self-check Q14:** How can shared traversal support different exact/prefix answers?

**Self-check Q15:** What is startsWith("") on a fresh trie here?

**Self-check Q16:** Does O(P) prefix existence imply O(P) time to print all completions?

## 8. Array children and map children

### 8.1 Choose a representation from the domain and operations

Array slots are direct character encodings. A map instead associates each present edge label with its child:

```text
array node for prefix "appl":
slot 4  -> apple
slot 24 -> apply
24 other slots -> null

map node for prefix "appl":
'e' -> apple
'y' -> apply
```

Both represent the same logical outgoing edges. A map's absence stands for a missing edge; no null child values are stored, so get returning null is unambiguous in this implementation.

| Concern | Fixed child array | HashMap child storage |
| --- | --- | --- |
| Domain | Explicit indexable alphabet, here a..z | Key type determines symbol model |
| Literal child lookup | O(1) direct index | Expected O(1) with appropriate hashing |
| New node | Allocates σ reference slots | Allocates node/map object; entries/buckets as needed |
| Sparse nodes | Still pay for null slots | Store present edges, with map/entry overhead |
| Dense small alphabet | Predictable layout and scans | Hash/bucket/boxing overhead may dominate |
| Wildcard iteration | Scan σ slots, skip nulls | Iterate present mappings, accounting for map capacity |
| Encounter order | Slot order gives alphabet order | HashMap order unspecified |
| Failure mode | Invalid index without domain checks | Character model/equality/normalization can still be wrong |

Neither is universally more memory-efficient. Twenty-six null-capable references per node can be costly on long narrow paths. A HashMap at every tiny node also has substantial object and bucket/entry overhead. Measure representative data if exact memory matters; asymptotic node counts are not byte measurements.

### 8.2 A complete UTF-16 char-key variant

The following variant changes the symbol domain, while preserving the path and terminal invariants. Every String is interpreted as a sequence of UTF-16 **code units**. It performs no case folding, normalization, or surrogate validation. A surrogate pair occupies two edges. A lone surrogate is accepted as one code unit; this is a raw String-key index, not a guarantee of human-character semantics.

```java
static final class MapNode {
    final Map<Character, MapNode> children = new HashMap<>();
    boolean isWord;
}

static final class CharMapTrie {
    private final MapNode root = new MapNode();

    public void insert(String word) {
        Objects.requireNonNull(word, "word");
        MapNode current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            MapNode next = current.children.get(c);
            if (next == null) {
                next = new MapNode();
                current.children.put(c, next);
            }
            current = next;
        }
        current.isWord = true;
    }

    public boolean search(String word) {
        Objects.requireNonNull(word, "word");
        MapNode endpoint = follow(word);
        return endpoint != null && endpoint.isWord;
    }

    public boolean startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        return follow(prefix) != null;
    }

    private MapNode follow(String text) {
        MapNode current = root;
        for (int i = 0; i < text.length(); i++) {
            current = current.children.get(text.charAt(i));
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
```

get/put make reuse and allocation explicit. computeIfAbsent can express similar logic, but a callback is not necessary to understand the algorithm. No query method puts a mapping or creates a node.

The map uses Character value equality, so equal char keys reuse an edge regardless of boxing-object identity. A HashMap can generally store null keys/values, but this code never inserts either: primitive char boxing supplies nonnull keys and every mapped node is nonnull. [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html)

### 8.3 UTF-16, code points, and visible characters

Java String.length and charAt count/index code units. A supplementary code point such as U+1F600 occupies two chars. Thus a Character-key trie can index an emoji exactly as a two-edge string, but a wildcard defined to consume **one char** would match only half of that code point.

```java
static List<Boolean> charMapDemo() {
    CharMapTrie trie = new CharMapTrie();
    trie.insert("caf\u00E9");
    trie.insert("\uD83D\uDE00");
    trie.insert("a.b");
    return List.of(
            trie.search("caf\u00E9"),
            trie.search("Caf\u00E9"),
            trie.startsWith("caf"),
            trie.search("\uD83D"),
            trie.startsWith("\uD83D"),
            trie.search("a.b"));
}
```

Result: `[true,false,true,false,true,true]`. The high-surrogate-only prefix succeeds under this raw code-unit policy, while that prefix is not itself a stored word. The literal dot in a.b has no wildcard role in this map API.

For a **code-point** trie, use integer keys and advance a String offset by Character.charCount(codePointAt(offset)); alternatively iterate a codePoints stream. Define whether malformed surrogate sequences are rejected. Code points still do not necessarily equal user-perceived grapheme clusters, and visually similar text can have different encoded sequences. Those broader text policies belong in the application's key contract.

Keep the models distinct: the array implementation's lowercase-English letters each occupy one char and one code point; the map example indexes raw chars; the wildcard implementation below operates only on the lowercase model. [Java 21 String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html)

### 8.4 Complexity qualifications

Map queries have expected O(L+1) worst-case traversal time under the hash assumptions, O(1) working space, and may stop early on a missing edge. They only null-check, so they do not perform the array implementation's full alphabet-validation scan.

Map insertion is expected amortized O(L+1) over construction with ordinary hash dispersion/growth. A single child-map resize can cost more than one constant-time update. Persistent storage includes nodes, one map object per node, N−1 mappings, and backing capacities. Do not call get/put unconditionally worst-case O(1), or count only the number of child entries and forget maps/buckets.

**Self-check Q17:** Why can sparse maps and fixed arrays each be expensive on different data?

**Self-check Q18:** Does Map<Character,...> automatically mean one edge per Unicode code point?

**Self-check Q19:** Why is get returning null safe as “no edge” in this code despite HashMap supporting null values generally?

## 9. Wildcard search using DFS

### 9.1 Define the query before choosing recursion

The pattern alphabet is a..z plus dot. **Dot matches exactly one lowercase letter**. It is not “zero or more letters,” not a regex engine, and not an escape for a literal dot. Pattern matching concerns an entire stored word of the same length.

For the requested illustrative dictionary bad/dad/mad:

| Pattern | Result | Explanation |
| --- | --- | --- |
| bad | true | Exact stored word |
| .ad | true | One of b/d/m, then a,d |
| b.. | true | b followed by any two existing letters |
| ..d | true | Any two letters, then d |
| pad | false | No initial p |
| . | false | One-character prefixes are not stored one-letter words |
| .... | false | No stored four-letter word |

Consecutive dots consume consecutive characters; none can consume zero letters. A pattern ending at a nonterminal prefix must fail.

### 9.2 A straightforward baseline

Keep all distinct words in a HashSet/List, scan those with matching length, and compare each position: a literal must equal the word's letter, while dot accepts that one position. With m words, this takes O(m(L+1)) as a simple upper bound, using O(1) per-comparison working state besides the dictionary.

That baseline can be sufficient for a tiny dictionary or very few queries. A trie can avoid repeatedly scanning words whose prefixes already fail. At a literal, there is at most one child worth examining. At dot, any existing child may work; distinguish “one failed candidate” from “all candidates failed.”

### 9.3 Derive the recursive state

Use `(node, pattern, position)`. Promise: **the remaining pattern suffix starting at position matches some path from node ending at a terminal node**. The already consumed positions correspond to the root route of node.

- At position==length, accept exactly when node.isWord is true.
- A literal selects its one child, if present.
- Dot tries each existing child at position+1 and succeeds on the first successful continuation.
- After every eligible continuation fails, return false.

Passing an index retains the original String and avoids repeated suffix allocation. Calls never receive a null node: a missing literal is rejected before calling, and wildcard branches skip null children.

### 9.4 Implementation

```java
static void requirePattern(String pattern) {
    Objects.requireNonNull(pattern, "pattern");
    for (int i = 0; i < pattern.length(); i++) {
        char c = pattern.charAt(i);
        if (c != '.' && (c < 'a' || c > 'z')) {
            throw new IllegalArgumentException("only a..z and dot are allowed");
        }
    }
}

static boolean matchFrom(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) {
        return node.isWord;
    }
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode child = node.children[c - 'a'];
        return child != null && matchFrom(child, pattern, position + 1);
    }
    for (int index = 0; index < 26; index++) {
        ArrayNode child = node.children[index];
        if (child != null && matchFrom(child, pattern, position + 1)) {
            return true;
        }
    }
    return false;
}

static final class PatternIndex {
    private final ArrayNode root = new ArrayNode();

    public void add(String word) {
        insertWord(root, word);
    }

    public boolean matches(String pattern) {
        requirePattern(pattern);
        return matchFrom(root, pattern, 0);
    }
}
```

The root is privately owned and insertion is reused. Full pattern validation precedes DFS, so invalid symbols are rejected consistently even behind a missing prefix. Empty patterns examine only root.isWord. Empty insertion can therefore make matches("") true, but never makes matches(".") true by itself.

Array order makes candidate exploration deterministic, a through z. That order affects the trace and early-success work, not the boolean meaning. A map implementation may examine children in a different order; it still must continue after failed branches.

### 9.5 Original trace: a failed branch before success

Use stored words **map, mint, mop, sun** and pattern **m..t**. This example is separate from the app dictionary.

```text
(root)
+-- m
|   +-- a
|   |   +-- p*
|   +-- i
|   |   +-- n
|   |       +-- t*
|   +-- o
|       +-- p*
+-- s
    +-- u
        +-- n*
```

After literal m, the first dot can try a/i/o. The a branch reaches map, but the final required t edge is absent. That failure must return to the earlier choice so i can lead to mint.

| Helper depth | Position | Prefix | Pattern item | Available relevant children | Action / eventual result |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | "" | m | m | Follow m; eventually true |
| 2 | 1 | m | . | a,i,o | Try a; later i succeeds; skip o |
| 3 | 2 | ma | . | p | Try p; branch eventually false |
| 4 | 3 | map | t | No t | Return false |
| 3 | 2 | mi | . | n | Try n; eventually true |
| 4 | 3 | min | t | t | Follow t |
| 5 | 4 | mint | End | — | Terminal true |

The table lists helper **entries** in actual order, not all possible prefixes. The complete enter/return calendar is:

| Event | Action | Prefix | Position | Live helper depth | Result on return |
| --- | --- | --- | --- | --- | --- |
| 1 | Enter | "" | 0 | 1 | — |
| 2 | Enter | m | 1 | 2 | — |
| 3 | Enter | ma | 2 | 3 | — |
| 4 | Enter | map | 3 | 4 | — |
| 5 | Return | map | 3 | 4 | false |
| 6 | Return | ma | 2 | 3 | false |
| 7 | Enter | mi | 2 | 3 | — |
| 8 | Enter | min | 3 | 4 | — |
| 9 | Enter | mint | 4 | 5 | — |
| 10 | Return | mint | 4 | 5 | true |
| 11 | Return | min | 3 | 4 | true |
| 12 | Return | mi | 2 | 3 | true |
| 13 | Return | m | 1 | 2 | true |
| 14 | Return | "" | 0 | 1 | true |

There are seven helper invocations, at most five live helper frames, and no calls for null slots. The wrapper adds one waiting frame. Once mint succeeds, no mo or s route is explored.

The fixed-array loop still examines null **slots**. ma scans all 26 slots because its p continuation fails. m examines slots a through i (9); mi examines a through n (14). Total wildcard-slot checks are **26+9+14=49**, distinct from seven node invocations. Literal edge lookups occur at root, map, and min.

### 9.6 Multiple wildcard positions and failure

On the same dictionary:

| Pattern | Result | Matching word / stopping reason | Helper entries |
| --- | --- | --- | --- |
| m..t | true | mint after map branch fails | 7 |
| m.. | true | map is the first successful length-three word | 4 |
| m..z | false | No final z below map/min/mop | 8 |
| .... | true | mint is a stored length-four word | 7 |
| mi | false | mi is only a prefix | 3 |
| "" | false | Root is not terminal | 1 |

The all-dot pattern .... also tries map first; at its fourth position map has no children, then the i branch reaches mint. Matching is a DFS over the trie constrained by pattern positions, not one linear march through the pattern.

**Self-check Q20:** Why does failure below ma not justify rejecting m..t?

**Self-check Q21:** Why must the wildcard base inspect isWord?

**Self-check Q22:** Does dot consume a Unicode code point in every possible trie implementation?

## 10. Recursive contracts and correctness

### 10.1 State the promise at each call

For matchFrom(node,pattern,i), node is nonnull and its root route matches pattern positions [0,i). The call returns true iff some terminal descendant extends that route to match positions [i,L). Full-word matching is essential: a route that stops before or after L is not a match.

| Obligation | Justification |
| --- | --- |
| Initialization | Root's empty route matches the empty consumed prefix |
| Base | With no symbols left, only a terminal current node is a complete stored word |
| Literal preservation | The unique named child consumes exactly this literal; no other label can match it |
| Wildcard preservation | Every existing child label is eligible and consumes exactly one position |
| Soundness | A true child result gives a matching terminal completion for the parent |
| Completeness | If a matching completion exists, its next edge is either the literal child or one of the wildcard candidates |
| Failure meaning | No literal child, or all eligible wildcard continuations fail |
| Termination | Every recursive call increases i; L-i strictly decreases |

This is an intuitive induction on remaining pattern length. The base is directly correct. Assuming shorter-suffix calls meet the promise, the literal or existential wildcard combination meets the current promise. Early success is safe because the task asks whether **any** completion exists; early failure after just one candidate is unsafe.

### 10.2 Information flow and state ownership

The helper returns a boolean for its own remaining-query question. Position and node are local parameter values. There is no global cursor or persistent found flag to reset between requests. An unsuccessful branch cannot consume input for its sibling because each receives its own i+1.

The trie itself is read-only during matching. No mutable path String is required for boolean existence. If later enumerating words, a path buffer and output copies become separate state with their own restoration/ownership obligations.

No visited set is needed for this ordinary trie. Each node has one root route, and at depth d its query position is fixed at d. Separate branches do not converge on the same node. Memoization is therefore unnecessary for these particular (node,position) states. A compressed/DAG index or a different pattern language could change that reasoning.

### 10.3 Total calls and maximum depth differ

All children run sequentially. The failing ma subtree has returned before mi starts. The call tree may branch widely, while the live call stack follows one current root route and its suspended callers.

Maximum helper depth is at most min(L,H)+1, where H is the deepest trie edge depth. This is O(L+1), not a guarantee that Java can safely recurse for every mathematically valid L. Even a literal-only query uses recursive matchFrom here. Exact/prefix APIs are iterative and can handle long paths without recursive depth.

For large recursive queries, an explicit frame can store node, query position, and next child index, simulating DFS with O(L+1) live frames. A version that pushes all pending children has a representation-dependent frontier cost. “Iterative” alone does not prove constant auxiliary memory. [Explicit state](./Recursion/Recursion_Essential_Concepts.md#14-recursion-iteration-and-explicit-stacks)

**Self-check Q23:** What does L-i measure, and why does it prove termination?

**Self-check Q24:** Why can many explored branches coexist with only O(L) recursive frames?

**Self-check Q25:** Why is no visited set needed for this wildcard algorithm?

## 11. Time, storage, and branching complexity

### 11.1 Define the quantities before using a bound

| Symbol | Meaning |
| --- | --- |
| L | Word/query length in the chosen edge symbols; here lowercase chars or raw UTF-16 chars |
| P | Prefix query length when distinguished from a full word |
| S | Total character occurrences across accepted insertion requests; duplicates may contribute repeatedly |
| m | Number of accepted insertion requests, including empty/duplicate words |
| W | Number of distinct stored words, including an inserted empty word |
| N | Actual trie nodes, including root |
| σ | Representable alphabet slots per array node; 26 in the implementation |
| b | Maximum number of actual children of a node; b≤σ for the array trie |
| H | Maximum trie edge depth |
| Δ | New nodes created by one insertion |
| V | Nonnull helper states actually entered during one wildcard query |
| B | Entered wildcard states with position<L |

Count actual objects and work rather than substituting W, N, and S for each other. A million duplicate insertions can leave N unchanged; inserting the empty word sets a flag without adding an edge.

For insertion-only tries built this way:

```text
N = 1 + number of distinct nonempty prefixes
N <= S + 1
nonnull edges = N - 1
W <= N
0 <= Δ <= L
```

When S=0 the root still exists. Total fixed-array storage is Θ(Nσ) slots plus Θ(N) node/array/flag overhead, or Θ(N) for σ=26. This is O(S+1), not a literal zero-storage O(S) at the empty case.

### 11.2 Operations and persistent storage

| Operation | Time model | Local auxiliary state | Persistent growth / result |
| --- | --- | --- | --- |
| Array insertion with prevalidation | Θ(L+1) for fixed σ; O(L+σΔ) explicitly | O(1) | Δ nodes, σΔ slots |
| Array exact search | Θ(L+1) with validation | O(1) | Boolean, no index growth |
| Array prefix search | Θ(P+1) with validation | O(1) | Boolean |
| Char-map insertion | Expected amortized O(L+1) over ordinary growth | O(1) explicit locals | Δ nodes/maps and new edges/buckets |
| Char-map exact/prefix search | Expected O(L+1) worst-case traversal; can stop early | O(1) | Boolean |
| Array wildcard search | O(L+V+σB), refined below | O(min(L,H)+1) frames | Boolean, no trie growth |

Validation/loop setup accounts for the +1 empty case. Index growth is stored data, but it is still extra allocated memory for insertion. Report it separately instead of hiding it behind the local-space bound.

For m insertions, even m empty words require m calls. Build time for fixed arrays is O(S+m+1), and stored space O(S+1). The shorthand O(S) assumes a nonempty-word convention or separately accounts for request overhead.

Map storage is Θ(N) node/map objects plus N−1 mappings and the sum of backing capacities. With ordinary default growth, no removals, and no extravagant pre-sizing, capacities remain proportional to degree plus a constant minimum, giving O(N) structural storage. Constants and allocation layout differ substantially from arrays. Iterating a HashMap view costs capacity+size, not automatically exactly the number of actual children. This matters if adapting wildcard DFS to oversized child maps.

### 11.3 Bound wildcard work by explored structure first

Each entered literal state performs constant work. Each base checks a flag. A wildcard state scans up to σ slots, with recursive work counted in descendant states. Full prevalidation adds Θ(L+1).

Therefore the implementation's direct bound is:

```text
time = O(L + V + σB + 1)
stack = O(min(L,H) + 1)
V <= N
```

For fixed σ=26, this simplifies to O(L+V+1). It does **not** simplify to O(L) for every query: V can be much larger than query depth. Treating alphabet size as variable requires retaining the slot-scan factor even when actual degree b is small.

A shape-based bound uses the number of nodes reachable at each queried depth:

```text
V <= min(N, 1 + b + b^2 + ... + b^L)
```

For b≥2 the geometric sum is O(b^L), giving **O(L+min(N,b^L)+1)** time with the fixed-alphabet assumption. Since N≤S+1, this explains the roadmap's O(min(S+1,b^L)) visited-node shorthand, plus validation/path handling and the actual child-iteration costs.

For b=1, there is at most one route: V≤min(N,L+1). For b=0 only root can be entered. Do not use a b≥2 exponential formula to erase these boundary cases.

For b≥1 and r dots, at most b^r full choice sequences are possible; a safe looser state bound is O((L+1)b^r), capped by N. The positions of dots and existing prefixes matter, and many branches can stop much earlier. These are upper bounds, not claims that every wildcard query reaches them.

### 11.4 A small witness that defeats the linear claim

Insert every length-three a/b string followed by x:

```text
aaax, aabx, abax, abbx, baax, babx, bbax, bbbx

root:                     ""
depth 1:                 a    b
depth 2:               aa ab ba bb
depth 3:          aaa aab aba abb baa bab bba bbb
depth 4:          each has a terminal x child
```

Query **...**. Every depth-three node is nonterminal, so no branch succeeds. The helper enters 1+2+4+8=**15 states**, scans 26 slots at each of seven wildcard states (**182 slot checks**), and peaks at four helper frames. L=3, N=23, S=32, W=8, b=2.

For the family with all b-ary prefixes of length L followed by one extra letter, a length-L all-dot query can explore the entire prefix tree and fail only at nonterminal depth-L nodes. The state count grows geometrically even though stack depth is linear.

Contrast query **....** on this concrete dictionary: it succeeds on aaax after **five helper calls**, with **27 wildcard-slot checks** (a,a,a,x at indices 0,0,0,23). Short-circuiting helps this success; it does not improve the worst-case failure bound.

### 11.5 Output and copying are separate costs

Boolean search has constant output size. Enumerating completions requires visiting relevant descendants and constructing results. If Z characters are emitted, output construction/storage alone costs Ω(Z), even if the prefix endpoint is found in O(P).

Repeated proper suffix substring construction on modern OpenJDK copying implementations can add Θ(L²) cumulative copied content along a length-L path. Index-based traversal avoids those suffix objects. Analyze live retained copies separately from cumulative allocation; merely counting recursive frames does not account for copied data. [String copying](./Java_String.md#substring-inclusive-start-exclusive-end)

**Self-check Q26:** Why is N bounded by S+1, and why may that bound be loose?

**Self-check Q27:** How do allocated slots, nonnull children, and stored words differ?

**Self-check Q28:** What assumptions are needed to simplify wildcard work to O(L+V)?

**Self-check Q29:** Why does an early successful wildcard query not establish a linear worst-case bound?

## 12. Choosing a trie or an alternative

Choose from the required operations, dictionary size, update frequency, key domain, and memory budget. A trie is useful when partial keys matter repeatedly; string input by itself is not a sufficient reason.

| Need | Suitable starting point | Cost/tradeoff to explain |
| --- | --- | --- |
| Only exact membership | HashSet<String> | Simpler; expected hash-table work plus String hashing/equality |
| Exact key→payload lookup | HashMap<String,Value> | Stores associated values without custom prefix nodes |
| Repeated prefix existence and incremental character traversal | Trie | Reuses prefix paths; pays node/child-container overhead |
| Infrequent prefix queries on a small dictionary | Scan words with startsWith | Simple O(m(P+1)) upper bound |
| Mostly static words and ordered/prefix queries | Sorted String array/list | Sort once; binary-search a prefix boundary |
| Dynamic ordered keys/neighbors | Balanced sorted map/set | Whole-key comparisons; no custom trie needed |
| Whole-word wildcard matching | Baseline word scan or trie branching | Decide from query volume, branching, memory |
| Many board paths against a dictionary | Optional trie-guided backtracking | Prefix pruning helps; path-local visitation still required |

### Exact lookup does not have free character processing

HashSet.contains is expected constant **table** work under hash dispersion, but a fresh length-L String key may require O(L) hashing and equality work. Cached hashes, identical object references, and early mismatches can reduce a particular lookup. Do not claim unconditional constant whole-word cost or claim a trie always outperforms a set.

A trie performs character-by-character lookup and can reject a missing early edge. This guide's validating array API still scans the whole input first; a guaranteed-domain version may exploit that early stop more directly.

### Sorted arrays can support prefix queries

Under the same ordinary lexicographic ordering and character policy, strings sharing a prefix form a contiguous region. Find the first key at least the prefix, then test that candidate with startsWith. Empty dictionaries and the chosen empty-prefix policy need explicit handling.

Binary search uses O(log m) **string comparisons**, not necessarily O(log m) character work. A safe bound with comparisons inspecting at most M units is O(M log(m+1)+P). Sorting m words can involve O(m log m) comparisons, each potentially scanning a long common prefix. Likewise, BST lookup takes O(h) key comparisons, each with its own string cost; logarithmic h requires a balancing guarantee.

### What extra trie features would require

A terminal marker can become a payload reference for word→value association. Counting completions needs subtree metadata or a scan. Autocomplete enumeration needs an output policy, traversal order, and path construction; top-ranked suggestions need further ranking information. None follows automatically from a boolean startsWith API.

**Self-check Q30:** When is HashSet the clearer choice?

**Self-check Q31:** Why are logarithmically many String comparisons not automatically logarithmic character work?

## 13. Trie-guided board search: optional advanced material

This section is **OPTIONAL ADVANCED MATERIAL**. The roadmap's Word Search II is a C-tier Hard challenge, deferred until both Tries and [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking) are comfortable. Core insertion/search/prefix/wildcard understanding does not require implementing it.

Assume a rectangular lowercase board, four orthogonal neighbors, nonempty dictionary words, and no cell reuse within one word path. Dictionary wildcard dots are not part of this board model.

### 13.1 Why prefixes can prune board paths

```text
       column 0 1 2
row 0         c a t
row 1         r r e
row 2         s o n

dictionary: cat, car, cart, tar, care
```

cat follows (0,0)→(0,1)→(0,2). car follows (0,0)→(0,1)→(1,1). care continues to (1,2). tar follows (0,2)→(0,1)→(1,1). cart is absent: no legal next t follows the car route; the top-right t is diagonal from (1,1).

Instead of starting a separate full board search for every word, build a dictionary trie and move a trie reference as board letters are consumed. A missing child proves no dictionary word extends the current spelling, so that entire board-path continuation can stop.

| Board route prefix | Trie observation | Action |
| --- | --- | --- |
| c | Present | Continue |
| ca | Present | Continue |
| car | Terminal and has e/t continuations in dictionary | Record car; still explore longer words |
| care | Terminal | Record care |
| c→r via (0,0)→(1,0) | No cr prefix | Prune |

The actual found set is `{cat,car,tar,care}`, with output order defined by an application's chosen contract.

### 13.2 Trie state and visited state answer different questions

- Trie node: which dictionary prefix has this route spelled?
- Position: which board cell is currently used?
- Path-local visited state: which cells cannot be reused in this one route?
- Output deduplication: which dictionary word identities have already been reported?

Backtracking follows choose→explore→undo: mark a chosen cell, explore legal neighbors whose letters continue the trie, then restore that mark before exploring another route. A global “visited once forever” rule would incorrectly suppress independent paths starting elsewhere or using the same cell later.

Reaching a terminal node does not justify stopping all continuation: car and care may both be present. Multiple board routes can spell the same word, so use a result set or per-word found flag if unique words are required. Clearing terminal flags is another possible optimization **only when dictionary mutation/one-shot ownership is explicitly allowed**; otherwise later searches would lose membership.

### 13.3 Conservative costs and limits

Let R×C be board cells, D=min(maximum dictionary word length,R·C), S total dictionary characters, and Z output characters. A conservative bound is O(S+R·C·4^D+Z), consistent with the roadmap's looser maximum-word-length form. Prefix pruning can greatly reduce actual exploration but does not establish polynomial worst-case search.

Trie storage is O(S+1) for the fixed alphabet. Recursion/path state is O(D); a separate visited matrix adds O(R·C). In-place board marking can avoid that matrix under an allowed mutation/restoration policy. Output requires its own space, and copied path strings add work. Tighter non-revisiting neighbor bounds are possible, but they still depend on route depth.

This preview deliberately supplies no complete board-search implementation. First be able to explain prefix pruning, local visitation, restoration on every continuing branch, deduplication, and terminal nodes with longer descendants.

**Self-check Q32:** Why cannot trie membership replace path-local visited cells?

**Self-check Q33:** Why continue after finding car if care is also stored?

**Self-check Q34:** When can clearing a terminal flag to deduplicate results violate the API?

## 14. Java details and ownership

### 14.1 References are not prefix copies

The child array holds references. A local assignment such as current=child changes one local slot; writing current.children[index]=new ArrayNode() changes shared structure. The root route carries prefix meaning implicitly. Two nodes ending in the same edge label can represent different prefixes, and two independent trie instances must not share roots/child arrays accidentally.

Keep the root and child arrays encapsulated in a public-facing wrapper. A final array field still permits element mutation; it does not make the trie immutable or thread-safe. Insertion-only single-threaded code is sufficient for this guide. Concurrent update/query behavior would require its own ownership/synchronization contract.

### 14.2 Character and String operations

charAt reads a code unit without copying the String. toCharArray creates a separate array, adding O(L) allocation. Neither automatically changes the symbol model to code points. For comparison, the following traversal counts code-point steps rather than char steps:

```java
static int countCodePointEdges(String text) {
    Objects.requireNonNull(text, "text");
    int count = 0;
    for (int offset = 0; offset < text.length();) {
        int codePoint = text.codePointAt(offset);
        offset += Character.charCount(codePoint);
        count++;
    }
    return count;
}
```

For `"a\uD83D\uDE00z"`, String.length is 4 while this method returns 3. It illustrates iteration, not a complete code-point trie or a validator for malformed Unicode. Do not change only key types while leaving insertion and query advancement inconsistent.

The array trie rejects uppercase, accents, digits, spaces, emoji, and literal dots. If an application wants case-insensitive keys, specify normalization and apply the same policy to insertion and queries; silently lowercasing arbitrary text changes the API and can involve language-specific decisions. Raw char-map keys are flexible, but still require an application-level equality policy.

### 14.3 Avoid suffix allocation in recursive search

matchFrom carries the original String and an integer position. Constructing substring(i+1) in every call can copy progressively shorter suffixes. On one full length-L path, lengths L−1,L−2,...,1 sum to Θ(L²) copied content on the usual modern OpenJDK copying paths.

Branching can multiply those copies across explored routes. The O(L) call-stack bound does not remove that work. This is why query position is a useful part of a recursive contract, not merely a coding style preference. The [String reference](./Java_String.md#substring-inclusive-start-exclusive-end) and [Recursion discussion](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) provide the prerequisite copying/lifetime model.

### 14.4 Empty input, errors, and allocation

A root exists even with no accepted words. Empty insertion sets one flag; duplicate empty insertion changes nothing further. Empty queries do not dereference a nonexistent “first character.” Null input deliberately throws rather than meaning the empty word.

Prevalidating insertion performs a second character scan before path construction, but two linear passes remain linear. It protects the documented invalid-input behavior and avoids unnecessary temporary arrays. Runtime allocation failures are not modeled as transactional updates; the promised no-partial-change behavior here concerns the defined null/alphabet validation errors.

Recursive wildcard tests should stay at modest depth. A long literal word can be inserted and searched iteratively without implying its recursive wildcard query is stack-safe. No portable safe recursion threshold is promised. [Trees depth/explicit state](./Trees/Trees_Essential_Concepts.md#7-iterative-dfs)

**Self-check Q35:** Why does final children not make a node immutable?

**Self-check Q36:** Why pass an index rather than recursive suffix Strings?

## 15. Mistakes and counterexamples

The Java subjects below are **deliberately incorrect or inefficient**, each under its stated intended behavior. They are not alternatives to the validated implementations. Small witnesses make each failure reproducible.

### 15.1 Replacing a shared child destroys earlier words

Intended behavior: insertion preserves existing memberships. Assume valid lowercase word input and a valid nonnull root.

```java
static void badOverwriteInsert(ArrayNode root, String word) {
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int index = word.charAt(i) - 'a';
        current.children[index] = new ArrayNode();
        current = current.children[index];
    }
    current.isWord = true;
}
```

On one fresh root, badOverwriteInsert(app) followed by badOverwriteInsert(apple) loses app's terminal marker. In the reverse order, the shorter insertion loses apple's longer path. Both words should remain. Allocation must happen only for a missing child; replacing a pointer replaces access to an entire subtree.

### 15.2 Establishing a path without marking a word

Intended behavior: inserting app makes exact app membership true.

```java
static void badUnmarkedInsert(ArrayNode root, String word) {
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        int index = word.charAt(i) - 'a';
        if (current.children[index] == null) {
            current.children[index] = new ArrayNode();
        }
        current = current.children[index];
    }
    // Incorrectly omits the terminal update.
}
```

After this app insertion, hasPrefix(app) is true while containsWord(app) is false. Empty insertion also fails because root is never marked. Paths and accepted words are separate invariants.

The opposite confusion occurs in an exact query:

```java
static boolean badPathAsWord(ArrayNode root, String word) {
    requireLowercase(word);
    return followValidated(root, word) != null;
}
```

With only apple stored, this accepts ap and app as complete words; it even accepts the empty word before insertion. Exact queries must inspect the terminal endpoint.

### 15.3 Returning a failure before trying other wildcard candidates

Intended behavior: match a whole validated lowercase/dot pattern from the supplied node and position.

```java
static boolean badFirstBranch(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) {
        return node.isWord;
    }
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode child = node.children[c - 'a'];
        return child != null && badFirstBranch(child, pattern, position + 1);
    }
    for (ArrayNode child : node.children) {
        if (child != null) {
            return badFirstBranch(child, pattern, position + 1);
        }
    }
    return false;
}
```

On map/mint/mop/sun, m..t returns false after the ma branch fails, missing mint. A child's true result can finish an existence query; its false result cannot rule out untried siblings.

### 15.4 Accepting any exhausted pattern

Intended behavior: the complete pattern must end at a complete word.

```java
static boolean badEndMarker(ArrayNode node, String pattern, int position) {
    if (position == pattern.length()) {
        return true;
    }
    char c = pattern.charAt(position);
    if (c != '.') {
        ArrayNode child = node.children[c - 'a'];
        return child != null && badEndMarker(child, pattern, position + 1);
    }
    for (ArrayNode child : node.children) {
        if (child != null && badEndMarker(child, pattern, position + 1)) {
            return true;
        }
    }
    return false;
}
```

With only mint stored, mi and .. wrongly return true at the nonterminal mi endpoint. Empty pattern also wrongly succeeds on a fresh root. The base must interpret terminal-word meaning, not merely that a route was consumed.

### 15.5 Checking the alphabet after changing the path

Intended behavior: reject invalid words with no structural change.

```java
static void badPartialValidation(ArrayNode root, String word) {
    Objects.requireNonNull(word, "word");
    ArrayNode current = root;
    for (int i = 0; i < word.length(); i++) {
        char c = word.charAt(i);
        if (c < 'a' || c > 'z') {
            throw new IllegalArgumentException("invalid letter");
        }
        int index = c - 'a';
        if (current.children[index] == null) {
            current.children[index] = new ArrayNode();
        }
        current = current.children[index];
    }
    current.isWord = true;
}
```

badPartialValidation(ab#) throws, but on a fresh root leaves a and ab allocated. hasPrefix(ab) changes from false to true despite no accepted insertion. Prevalidate before mutation, as in insertWord.

### 15.6 More failure-focused cases

| Incorrect assumption | Witness / consequence | Correct interpretation |
| --- | --- | --- |
| Any char fits 26 slots | A maps to -32; { maps to 26 | Validate a..z before indexing; map/code-point domains need different encodings |
| isWord means no children | app has a child and is stored | Terminal and children are independent |
| New nodes should begin terminal | Inserting apple would incorrectly store every prefix | New flags start false; mark only endpoint |
| Empty means null | Empty insertion must mark root, not throw | Null and empty String have distinct contracts |
| Dot can match zero symbols | Stored bad should not match b.d. | Each dot consumes exactly one edge |
| Existing-word lookup needs node identity comparison | Comparing a found node to new ArrayNode() always fails | Membership uses route plus flag; identity tests are useful for preservation, not key equality |
| Lowercasing one side is harmless | Insert App and query app under a case-sensitive contract | Normalize both sides only under an explicitly changed key policy |
| Every wildcard query is O(L) | Length-three ... visits 15 nodes in the binary-prefix witness | Account for branching and slot iteration |
| HashMap iteration is sorted | Candidate traces differ across map layouts | Boolean correctness cannot depend on map order |
| Global mutable query cursor is convenient | One failed branch advances past characters needed by siblings | Pass position per call |
| Repeated calls can reuse a stale found flag | A prior success can make a later impossible pattern succeed | Keep result state local to one request |
| Every visited board cell is unavailable forever | A cell needed by another independent path is suppressed | Use path-local marks and restore |
| Clear terminal flags without an ownership policy | A later dictionary query loses a word | Keep queries read-only or explicitly grant one-shot mutation |

### 15.7 A semantically correct but costly suffix version

This subject assumes validated lowercase suffix input and a valid nonnull node. It is intentionally inefficient, not logically incorrect:

```java
static boolean costlySuffixExact(ArrayNode node, String suffix) {
    if (suffix.isEmpty()) {
        return node.isWord;
    }
    ArrayNode child = node.children[suffix.charAt(0) - 'a'];
    return child != null && costlySuffixExact(child, suffix.substring(1));
}
```

On a stored app path, the suffix arguments are app, pp, p, and empty. Proper suffix content copied is 2+1+0=3 units on the copying model. A full length-L path accumulates L(L−1)/2 units. The index-based exact helper follows the same path without those suffix objects. This count is cumulative copied content; it is not a claim that every historical suffix remains live simultaneously.

### 15.8 A compact boundary matrix

All rows start from the stated fresh index unless an operation sequence is shown:

| Setup / operation | Expected result |
| --- | --- |
| Fresh LowercaseTrie, search("") | false |
| Fresh LowercaseTrie, startsWith("") | true |
| Insert ""; search(""); startsWith("") | true; true |
| Insert apple; search(app); startsWith(app) | false; true |
| Insert apple, then app; search both | true; true |
| Insert app, then apple; search both | true; true |
| Insert apple twice | Same membership and identities; no new nodes second time |
| Fresh trie, insert a and z; search each | true; true; slots 0 and 25 |
| Insert ab# into validating array API | IllegalArgumentException; root structure unchanged |
| search("A"), startsWith("a#") | IllegalArgumentException for each |
| PatternIndex.add("a.b") | IllegalArgumentException |
| PatternIndex.matches("a*") | IllegalArgumentException |
| Null argument to any public String operation | NullPointerException |
| PatternIndex with only empty word, matches(".") | false |
| CharMapTrie insert a.b; search a.b | true, literal dot |

**Self-check Q37:** Why can returning true early be safe for wildcard existence while returning false after one branch is unsafe?

**Self-check Q38:** Which failures can pass tests containing only exact nonempty words with no shared prefixes?

## 16. Pattern recognition and derivation

### Pattern A: shared-prefix index — core

Recognition clues are repeated word/prefix queries or incremental character-by-character dictionary traversal. Confirm the symbol domain, empty policy, duplicate meaning, and whether outputs are booleans, counts, or suggestions.

The invariant is one node per represented prefix, with terminal state separate. Typical operations validate, follow/create edges, and interpret endpoints. Exact/prefix operations are O(L) under fixed direct child access; map costs require expected/amortized qualifications. Storage follows distinct prefixes and child representation.

Main traps are overwriting shared children, confusing path existence with membership, and assuming 26 slots fit arbitrary text. Prefer HashSet for exact membership alone; a sorted array can be sufficient for a mostly static dictionary with occasional prefix queries.

### Pattern B: wildcard prefix search — core

Clues are whole-word patterns containing one-symbol wildcards. State what a symbol means and whether the wildcard consumes exactly one, zero-or-more, or another unit. This guide handles only the first.

The invariant is that the node route matches the consumed query prefix. DFS carries node/position, chooses one literal child or tries all eligible wildcard children, and accepts only terminal exhaustion. Cost depends on explored structure, child scans, query validation, and O(L) maximum frames. A brute-force word scan remains a sound baseline and may be simpler for small workloads.

Main traps are failing to retry siblings, accepting nonterminal endpoints, creating suffix copies, and describing all wildcard searches as linear. Different regex/glob semantics need different state and proofs.

### Pattern C: trie-guided board search — optional advanced

Clues are many dictionary words sought along board paths with prefix-pruning opportunities. Assumptions include adjacency rules, path-local cell reuse restrictions, dictionary symbol model, and duplicate-output policy.

The invariant combines a valid trie prefix with a legal current board route. The algorithm concept is trie + DFS + choose/explore/undo. Space includes the trie, active route/frames, visited representation, and output. Time can remain exponential in route depth despite pruning.

Main traps are global visited marks, missing restoration, stopping at a word that has longer descendants, and mutating a reusable dictionary to deduplicate. For one short target word, direct board search can be simpler than building a dictionary trie.

### Recognition mini-scenarios

Choose a representation and explain its assumptions before reading the concise key in Section 17:

| Scenario | Decision to justify |
| --- | --- |
| R1 | A service checks only whether a complete identifier belongs to a fixed set |
| R2 | A live dictionary repeatedly tests whether typed prefixes can be continued |
| R3 | Query m..t asks for a complete stored word |
| R4 | A raw-text index must treat an emoji as one wildcard symbol |
| R5 | A sorted, rarely updated word list needs occasional prefix-existence queries |
| R6 | Many dictionary words must be found on a board without cell reuse |
| R7 | An API must return every completion of a very short prefix |
| R8 | One 100000-letter word must support insertion and exact lookup |

**Self-check Q39:** What requirements would justify choosing a trie over exact-key hashing?

**Self-check Q40:** What additional costs arise when a query returns matching words rather than a boolean?

## 17. Self-assessment and answer key

Answer the numbered questions without notes. These are conceptual checks, not another exercise workbook. A concise key is a checkpoint; it does not replace the derivations, diagrams, or tested implementation.

<details>
<summary>Q1–Q11: structure, policies, and insertion</summary>

| Question | Concise answer |
| --- | --- |
| Q1 | ba; its meaning comes from the entire root route. |
| Q2 | ap has a route but no terminal flag in the four-word dictionary. |
| Q3 | Terminal records membership; children record possible extensions. app has both. |
| Q4 | Zero child objects; it creates one node and one array of 26 null references. |
| Q5 | c-'a' stays within 0..25 only for the chosen alphabet. |
| Q6 | Valid prefix nodes can be allocated before a later invalid symbol throws, changing startsWith answers. |
| Q7 | No; only apple is marked unless app was separately inserted. |
| Q8 | A fresh object is a different identity; route/terminal state determines membership. |
| Q9 | It already owns that prefix and possibly many earlier words below it. |
| Q10 | A previously nonterminal internal prefix such as ap can simply be marked. |
| Q11 | Persistent nodes/child containers added to the index; those still consume stored memory. |

</details>

<details>
<summary>Q12–Q25: searches, representation, and recursive contracts</summary>

| Question | Concise answer |
| --- | --- |
| Q12 | The endpoint's isWord flag. |
| Q13 | Full prevalidation precedes the early-stopping route finder. |
| Q14 | Exact search checks the endpoint flag; prefix search checks only endpoint existence. |
| Q15 | True under this guide's explicit root-path policy. |
| Q16 | No; descendants and emitted character data must be processed. |
| Q17 | Arrays pay for absent slots; maps pay object, bucket, entry, and boxing overhead. Density/domain determine tradeoffs. |
| Q18 | No. Character keys are UTF-16 code units; supplementary code points occupy two. |
| Q19 | This implementation never stores null child values, so null get cannot denote a present child. |
| Q20 | Another eligible child, i, may complete the pattern; ma disproves only its own continuation. |
| Q21 | Exhausting a pattern at a prefix does not prove a complete stored word. |
| Q22 | No; wildcard units depend on the stated character model. Here it consumes one lowercase letter. |
| Q23 | Remaining pattern positions; each child call reduces it by one. |
| Q24 | Siblings execute sequentially; only one active route and its suspended ancestors coexist. |
| Q25 | Each node has one route/position, and calls progress downward without convergence or cycles. |

</details>

<details>
<summary>Q26–Q40: costs, optional board search, and selection</summary>

| Question | Concise answer |
| --- | --- |
| Q26 | At most one new node per inserted character plus root. Reused prefixes and duplicates reduce actual nodes. |
| Q27 | Slots include null storage; nonnull child edges link nodes; terminal nodes identify distinct words. |
| Q28 | Fixed σ=26, constant-size state operations, and V counting all entered nodes; validation adds L. |
| Q29 | An unsuccessful query can explore every eligible continuation even when a successful query stops early. |
| Q30 | Exact membership without repeated prefix/incremental structure requirements. |
| Q31 | Each comparison may scan a long shared character prefix. |
| Q32 | The trie tracks allowed spellings, not which board cells this route already used. |
| Q33 | A terminal prefix can have terminal descendants. |
| Q34 | When later queries expect the same reusable dictionary membership. |
| Q35 | final fixes the array reference; its entries and terminal flag remain mutable. |
| Q36 | One String and index avoid repeated suffix content copying and allocation. |
| Q37 | One witness proves existence; one failed candidate does not disprove all alternatives. |
| Q38 | Missing terminal/prefix distinctions, overwriting shared paths, wildcard sibling/base errors, and empty/invalid-input policies can remain hidden. |
| Q39 | Repeated prefix/incremental or constrained character queries whose benefits justify the representation's memory cost. |
| Q40 | Traversing matched descendants, constructing/copying result text, output storage, ordering, and possibly deduplication. |

</details>

<details>
<summary>Recognition checkpoints R1–R8</summary>

R1: start with HashSet for exact membership. R2: a shared-prefix index fits repeated prefix traversal. R3: whole-word wildcard DFS with terminal exhaustion. R4: choose a code-point-aware model and advance accordingly; raw char keys alone are insufficient for that wildcard unit. R5: sorted lower-bound lookup plus a prefix check may suffice. R6: optional trie-guided backtracking with path-local visitation/restoration. R7: prefix lookup is only setup; include descendant/output costs. R8: iterative insertion/exact traversal avoids recursive depth, while 26 slots per long-path node still consume memory.

</details>

## 18. Key takeaways and readiness

A trie indexes **prefix relationships**. Root routes define prefixes; terminal state records complete keys. Insertion extends only missing edges. Exact and prefix queries share traversal but interpret endpoints differently. Wildcard matching branches only where allowed and returns existence over eligible continuations. Character domain and child representation determine both correctness and cost.

Use the roadmap's [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) with these demonstrations:

- Draw a new shared-prefix dictionary and identify words, nonterminal prefixes, actual nodes, and allocated slots.
- Implement insert/search/startsWith from a blank editor and explain loop initialization, preservation, termination, and final interpretation.
- Test shorter/longer insertion in both orders, duplicates, missing edges, null/invalid inputs, and the chosen empty-word/prefix policies.
- Compare array and map children for the actual alphabet and explain char versus code-point behavior.
- Derive wildcard state/base/branch combination and trace a failure before a successful sibling.
- Explain O(L) exact traversal, possible exponential wildcard exploration, O(L) maximum frames, and persistent memory growth.
- Select hashing, sorting, or a trie for an unfamiliar dictionary variant and defend the choice.
- Repeat independent success across separate sessions; a remembered implementation alone does not establish R.

Keep concept/drill checks, platform histories, and dated readiness evidence in the [existing Tries roadmap section](./DSA_Interview_Roadmap.md#8-tries). Follow its understand→implement→practice→explain→review→validate loop. Record I/H/S honestly and use flexible [1/3/7/14-day reviews](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition) when useful. No gate is awarded by reading this guide.

The roadmap's practice order and priorities remain:

| Priority | Official problem | Difficulty | Learning objective |
| --- | --- | --- | --- |
| E | [208. Implement Trie (Prefix Tree)](https://leetcode.com/problems/implement-trie-prefix-tree/) | Medium | Shared paths, terminal membership, and prefix lookup |
| E | [211. Design Add and Search Words Data Structure](https://leetcode.com/problems/design-add-and-search-words-data-structure/) | Medium | Literal versus wildcard continuation and correct base cases |
| C | [212. Word Search II](https://leetcode.com/problems/word-search-ii/) | Hard | Optional prefix pruning plus board backtracking |

E/C are the local roadmap's learning priorities; Medium/Hard are platform difficulties. The optional Hard problem does not block ordinary Tries readiness and stays deferred until Backtracking is comfortable. Platform constraints may differ from this guide's deliberately explicit empty/validation policies and general dot-count model.

## 19. References and validation

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Tries](./DSA_Interview_Roadmap.md#8-tries), [Trees](./DSA_Interview_Roadmap.md#7-trees), [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [independent gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates).
- [Java Arrays](./Java_Array.md) and [Java Strings](./Java_String.md).
- [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) and [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md).
- [Arrays & Hashing Essential Concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md).
- [Recursion Essential Concepts](./Recursion/Recursion_Essential_Concepts.md): [contracts](./Recursion/Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness), [branching](./Recursion/Recursion_Essential_Concepts.md#9-multiple-branch-recursion), and [complete space](./Recursion/Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space).
- [Trees Essential Concepts](./Trees/Trees_Essential_Concepts.md): the current file is inside Trees.
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md).

Only actual discovered paths are used here. Older documents may have their own moved-file links; this guide's links are validated independently.

### Primary references

Java contracts were checked with Context7's version-21 Oracle API collection and direct primary documentation. Relevant String/HashMap information was used; unrelated retrieved classes were disregarded.

- [Java 21 String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html): UTF-16 positions and code-point APIs.
- [Java 21 Character](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html): code units, supplementary code points, and charCount.
- [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html): lookup/update assumptions, null behavior, unspecified order, and iteration capacity costs.
- [Java 21 language specification: default values](https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.12.5): initial reference/boolean values.
- [OpenJDK 21 String implementation](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/String.java) and [Latin-1 helper](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/StringLatin1.java): implementation context for proper suffix copying, rather than an API-wide allocation guarantee.

The official LeetCode titles, numbers, and difficulties linked in Section 18 were checked on **2026-10-28**. The [official NeetCode roadmap](https://neetcode.io/roadmap) is the repository roadmap's organizational reference; its interactive page did not expose curriculum content in this text retrieval. The local roadmap remained authoritative for this task.

### Author validation

Completed **2026-10-28**. All **17 Java blocks**, containing **26 methods** plus the node/wrapper classes, were assembled and compiled with Java 21 using `--release 21 -Xlint:all -Werror`.

Runtime validation passed **644,654 assertions** across **158 dictionary cases** and **191,136 query checks**. Cases included all **128 subsets** of the binary-word universe of lengths zero through two, plus 30 deterministic randomized insertion histories. Independent word-set oracles checked exact membership, prefix policy, and position-by-position wildcard matching. Structural checks verified all represented prefixes, terminal flags, unique routes, node bounds, duplicate identity preservation, separate instances, and read-only queries.

Instrumentation checked **141,325 wildcard helper invocations** and recorded **15 pattern metrics**. **92 rows across 12 dry-run/boundary tables** and **five character-edge diagrams** were compared with actual Java insertion/follow/DFS events and structural snapshots; that comparison passed **431 assertions**. Separate snapshot checkpoints passed another **139 assertions**. The original failed-branch trace, complete enter/return calendar, 49 slot checks, width of the binary-prefix witness, and its 15-call/182-slot failure and five-call/27-slot success were verified. The optional fixed board's word set was checked by independent enumeration of legal non-reusing routes.

Tests covered null/alphabet rejection, unchanged structure after rejected insertions, empty words/prefixes, duplicate insertion, raw UTF-16 surrogate behavior, case sensitivity, literal map dots, and the code-point iteration illustration. **53 exception checks** and **21 flawed/inefficient-subject witness/control checks** reproduced the documented mistakes. Suffix-copy counts were checked for lengths 0..200. Both iterative trie variants handled a **100,000-character path**; no deep recursive wildcard call was required. These tests do not establish a portable safe Java recursion depth or exact JVM heap-byte costs.

All **57 local links** were checked against existing files and heading anchors. The audit also checked section/question/answer coverage, code fences, and exact roadmap practice priorities. SHA-256 comparison confirmed all **32 pre-existing guide files** remained unchanged. Only **Tries_Essential_Concepts.md** was created in the repository; all validation artifacts stayed outside DSA_Guide. No prerequisite reference was missing, and no correctness/link issue remains unresolved.
