# Java DSA Interview Guide

> An interview-first roadmap and reference for learning how to turn **problem clues → constraints → patterns → data structures → algorithms → correct code**.

All implementations and language-specific examples use **Java 17-compatible code**, with Java-native collections, object references, and immutable strings. Blocks labeled `text` are intentionally language-independent pseudocode. Learn the invariants and decisions; a template is a starting shape, not a solution to memorize.

---

## Table of Contents

1. [How to Use This Guide](#1-how-to-use-this-guide)
   - [Java for DSA Interviews — Essential Reference](#java-for-dsa-interviews-essential-reference)
2. [DSA Interview Priority Map](#2-dsa-interview-priority-map)
3. [Complexity Analysis and Foundations](#3-complexity-analysis-and-foundations)
4. [Arrays & Strings](#4-arrays-strings)
5. [Hashing](#5-hashing)
6. [Two Pointers](#6-two-pointers)
7. [Sliding Window](#7-sliding-window)
8. [Linked Lists](#8-linked-lists)
9. [Stacks, Queues & Deques](#9-stacks-queues-deques)
10. [Binary Search](#10-binary-search)
11. [Sorting](#11-sorting)
12. [Trees](#12-trees)
13. [Heaps / Priority Queues](#13-heaps-priority-queues)
14. [Graphs](#14-graphs)
15. [Recursion & Backtracking](#15-recursion-backtracking)
16. [Greedy Algorithms](#16-greedy-algorithms)
17. [Intervals](#17-intervals)
18. [Dynamic Programming](#18-dynamic-programming)
19. [Tries](#19-tries)
20. [Specialized / Advanced Topics](#20-specialized-advanced-topics)
21. [DSA Pattern Recognition](#21-dsa-pattern-recognition)
22. [Interview Problem-Solving Framework](#22-interview-problem-solving-framework)
23. [Common Interview Mistakes](#23-common-interview-mistakes)
24. [Code Templates](#24-code-templates)
25. [How to Learn DSA Effectively](#25-how-to-learn-dsa-effectively)
26. [Mistake Log System](#26-mistake-log-system)
27. [Learning Roadmap](#27-learning-roadmap)
28. [Mastery Checklists](#28-mastery-checklists)
29. [DSA Interview Cheat Sheet](#29-dsa-interview-cheat-sheet)

### Deep links for the longest reference sections

- **Java:** [essential reference](#java-for-dsa-interviews-essential-reference)
- **Trees:** [recursive DFS](#122-recursive-dfs-traversals), [iterative DFS](#123-iterative-dfs-traversals), [BFS](#124-tree-bfs-level-order-traversal), [BST](#126-binary-search-trees), [LCA](#127-lowest-common-ancestor)
- **Graphs:** [representations](#141-graph-representations), [DFS](#142-graph-dfs), [BFS](#143-graph-bfs-and-unweighted-shortest-paths), [topological sort](#148-topological-sorting), [DSU](#149-union-find-disjoint-set-union-dsu), [Dijkstra](#1412-dijkstras-algorithm)
- **Dynamic programming:** [recognition](#182-how-to-recognize-dp), [design process](#183-the-six-part-dp-design-process), [worked evolution](#186-worked-evolution-from-brute-force-to-optimized-dp), [knapsack](#1810-knapsack-style-dp), [subsequences](#1811-subsequence-dp)
- **Templates:** [binary search](#2410-binary-search-exact-match), [BFS](#2415-graph-bfs), [topological sort](#2418-topological-sort-kahns-algorithm), [Union-Find](#2419-union-find-disjoint-set-union), [backtracking](#2422-backtracking-choose-explore-unchoose), [DP](#2424-dynamic-programming-memoization-and-tabulation)
- **Roadmap:** [Phase 1](#phase-1-foundations), [Phase 3](#phase-3-core-interview-patterns), [Phase 4](#phase-4-trees-and-graphs), [Phase 6](#phase-6-dynamic-programming), [Phase 8](#phase-8-interview-practice)

---

## 1. How to Use This Guide

This document works in two modes:

- **Course mode:** follow the phases in [Learning Roadmap](#27-learning-roadmap). Within each phase, learn Tier 1 material before Tier 2, and defer Tier 3/4.
- **Reference mode:** use the Table of Contents, [Pattern Recognition](#21-dsa-pattern-recognition), [Code Templates](#24-code-templates), and final [Cheat Sheet](#29-dsa-interview-cheat-sheet).

<a id="java-for-dsa-interviews-essential-reference"></a>
### Java for DSA Interviews — Essential Reference

Use Java to express the invariant clearly. This guide targets **Java 17-compatible source**, including records where named immutable fields help. The examples also work on Java 21. Select a supported Java version on your coding platform; if records are unavailable, use a small class with final fields and a constructor. No algorithm depends on a record-specific trick.

**Snippet convention:** Algorithm blocks contain members to put inside a `class Solution`; API demonstrations contain statements to put inside a method. Add `import java.util.*;` once. Helper types declared earlier in the same topic are identified where needed; adapt judge-provided `ListNode`/`TreeNode` field names and signatures instead of redefining them. Each block is an independent example, not a file to concatenate with every other block. `text` fences are deliberately language-independent reasoning or pseudocode.

**Input contract:** Unless a snippet says otherwise, arrays, strings, collections, and their required elements are non-null; dimensions and indices satisfy the stated problem contract. A null node is the normal empty linked-list/tree representation. Empty sequences are handled or explicitly ruled out. Agree on invalid-input behavior before adding checks; do not silently treat an invalid input as a valid empty answer.

**Complexity convention:** Indexed `List` parameters assume constant-time access, as with `ArrayList`; using `LinkedList.get(i)` repeatedly can change the bound. Graph examples use an array-backed outer adjacency list. Numeric operations assume the stated fixed-width result bounds. Report auxiliary memory separately from required output, and include copies, table initialization, and recursion frames.

#### Memory, understanding, and lookup budget

| Must know from memory | Must understand and reconstruct | Safe to look up occasionally |
|---|---|---|
| Array indexing and `.length`; String `length()`, `charAt()`, `equals()`; list `add/get/set/remove/size` | Aliasing, shallow copying, mutation, object identity vs logical equality | Less common `NavigableMap` operations beyond their purpose |
| `HashMap` lookup/default/update/iteration; `HashSet` membership | Stable equality and hashing; why a map stores counts, indices, or states | Specialized collection constructors and capacity tuning |
| `ArrayDeque` as stack/queue; `PriorityQueue` min/max direction | LIFO/FIFO/heap invariants and expected/amortized costs | Exact sorting implementation internals and rare APIs |
| `Arrays.sort`, `List.sort`, safe comparators; `StringBuilder` | Overflow bounds, UTF-16 assumptions, recursion depth, DP state sufficiency | Unicode normalization/grapheme libraries when a real contract needs them |

Make the first column automatic with tiny blank-page drills. Reconstruct algorithms from their invariants; do not memorize complete problem solutions. This reference can be read in pieces alongside the relevant topic.

#### Arrays and ArrayList

```java
int[] values = {4, 1, 7};
int[] counts = new int[26];            // primitive elements default to 0
int size = values.length;
int first = values[0];                // require length > 0
values[1] = 9;
for (int value : values) {
    // value is a copy; assigning value does not update the array
}
Arrays.fill(counts, -1);
int[] copy = Arrays.copyOf(values, values.length);
Arrays.sort(copy);                    // ascending; values is unchanged

List<Integer> numbers = new ArrayList<>();
numbers.add(4);                       // amortized O(1) append
numbers.add(7);
int number = numbers.get(0);          // unboxes Integer
numbers.set(0, 9);                    // replaces an existing element
numbers.remove(0);                    // removes index 0; shifts the suffix
numbers.remove(Integer.valueOf(7));   // removes first matching value
int listSize = numbers.size();
```

An `int[]` has fixed length, unboxed values, and `O(1)` indexing; use it for known-size input, counts, visited state, and DP. `ArrayList<Integer>` grows and stores object references to boxed numbers; use it for variable-size results. Its `get/set/size` are `O(1)`, append amortized `O(1)`, and insertion/removal at an arbitrary index `O(n)` in the worst case. Removing the final element by index is `O(1)`; `remove(Object)` must search. Initial capacity does **not** create elements: `new ArrayList<>(n)` still has size zero. [ArrayList API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html)

Array allocation/initialization and full `Arrays.fill` are `O(n)`; copying into length `m` is `O(m)` including initialization. `Integer[]` initially contains null references, so unboxing an unfilled cell fails. `new int[rows][cols]` creates distinct zero-filled rows; a Java 2D array may also be jagged, so rectangular algorithms must state that assumption.

`List<T>`, `Set<T>`, `Map<K,V>`, `Queue<T>`, and `Deque<T>` describe needed operations; concrete classes choose storage and performance. Generic type arguments are reference types: `List<Integer>` is valid, `List<int>` is not. Enhanced `for` over objects copies each reference; mutating the referenced object is visible, but reassigning the loop variable does not replace a list element.

> 🌐 **Java Backend Relevance — HIGH:** Collection interfaces and generics make method contracts clearer. Choose the implementation from required operations; prefer primitive arrays for dense numeric state and meaningful object types for application data.

#### Strings, StringBuilder, and character assumptions

**`String` is immutable in Java.** A method such as `substring` returns a string; it never edits the original.

```java
String text = "algorithm";
int length = text.length();
char first = text.charAt(0);
String piece = text.substring(2, 6);   // "gori": [begin, end)
boolean same = text.equals("algorithm");
int order = "cat".compareTo("dog");   // negative, zero, or positive
char[] letters = text.toCharArray();
letters[0] = 'A';                     // text stays "algorithm"
String changed = new String(letters);

StringBuilder builder = new StringBuilder();
for (char letter : letters) {
    builder.append(letter);
}
builder.append('s');
builder.setCharAt(0, 'a');
String result = builder.toString();
```

`length()` and `charAt()` are constant time in the implementations used here. Budget `O(k)` time and space for a copied proper substring of length `k`, `O(n)` for `toCharArray()`, and up to `O(min(n,m))` character comparisons for equality/lexicographic comparison (length checks can reject equality earlier). Full/empty substrings may be optimized. Building a length-`n` output with repeated `result += nextCharacter` in a loop can copy growing prefixes for `O(n²)` total work. Repeated builder append takes amortized time proportional to total appended content; one final `toString()` copies `O(n)` content. Front/middle insertion or deletion still shifts content. Avoid taking a substring or converting the builder to a string inside each window iteration. [String API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html), [StringBuilder API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html)

`char` is a **16-bit UTF-16 code unit**, not always a complete Unicode code point. `"😀".length()` is 2. A frequency array of length 26 requires lowercase English letters; size 128 requires ASCII. For code-point processing, `s.codePoints().toArray()` gives an `int[]` at `O(n)` time/space. Code points are still not always whole user-perceived characters; normalization and grapheme boundaries require a separate contract. Do not complicate an explicitly ASCII interview task. [Character API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html)

> 🌐 **Java Backend Relevance — HIGH:** Immutability, efficient text construction, and explicit character assumptions transfer directly to parsing, validation, identifiers, and text transformations.

#### HashMap, HashSet, equality, and iteration

```java
Map<String, Integer> frequency = new HashMap<>();
frequency.put("cat", frequency.getOrDefault("cat", 0) + 1);
Integer count = frequency.get("dog");  // null if absent here
boolean known = frequency.containsKey("dog");
frequency.remove("dog");
for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
    String word = entry.getKey();
    int occurrences = entry.getValue();
}

Set<Integer> seen = new HashSet<>();
boolean added = seen.add(42);          // false if already present
boolean present = seen.contains(42);
seen.remove(42);

Map<String, List<String>> groups = new HashMap<>();
groups.computeIfAbsent("act", key -> new ArrayList<>()).add("cat");
```

Use `entrySet()` when both key and value are needed, `keySet()` for keys, and `values()` for values. Views are backed by the map. `get` never inserts; `put` inserts or replaces. `getOrDefault` supplies a default for an **absent** key, not for a present null value. For interview count maps, keep stored values non-null. `computeIfAbsent` creates a value when absent or mapped to null; its callback should not structurally modify the same map.

Expected lookup/removal is `O(1)` for well-distributed constant-cost hashes/equality; insertion is expected amortized `O(1)`, with occasional linear resizing. There is no general worst-case constant-time guarantee. Current implementations can treeify collision bins, but do not promise every arbitrary custom-key operation becomes `O(log n)`. Iteration costs `O(size + capacity)` for these hash tables, so extreme over-allocation hurts. A new string key's hashing/equality may cost `O(key length)`; include that work. No sorted or insertion-order guarantee exists. [HashMap API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html), [HashSet API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html)

`==` on object references tests identity; `.equals()` tests the type's logical equality. Use `Objects.equals(a, b)` if either might be null. Never compare boxed `Integer` values with `==`; caching can make this appear to work for some numbers. When overriding `equals`, also override `hashCode`: equal keys must have equal hashes; equal hashes need not imply equality. Never mutate fields used by equality or hashing while a key is stored.

Arrays use identity equality by default. Use `Arrays.equals` for primitive contents and `Arrays.deepEquals` for suitable nested arrays; neither changes a `HashMap<int[], ...>` into a content-keyed map. Prefer an immutable encoded key or a class with consistent content-based equality/hash behavior. A record with primitive/String components is often simplest.

> 🌐 **Java Backend Relevance — HIGH:** Equality and hashing determine correct deduplication, grouping, map lookup, and cache keys. Shared mutable keys can make entries effectively unreachable even though they are still stored.

#### TreeMap and TreeSet when order is required

```java
TreeMap<Integer, String> events = new TreeMap<>();
events.put(10, "start");
events.put(20, "finish");
Integer floor = events.floorKey(15);    // greatest key <= 15: 10
Integer ceiling = events.ceilingKey(15); // smallest key >= 15: 20
Integer lower = events.lowerKey(10);   // strictly smaller: null here
Integer higher = events.higherKey(10); // strictly larger: 20
TreeSet<Integer> times = new TreeSet<>();
times.add(10);
times.add(20);
Integer previous = times.floor(15);
```

Use ordered trees for sorted iteration, predecessor/successor, or range queries. Basic insert/remove/lookup costs `O(log n)`; these navigation operations also follow tree height. Navigation can return null; check before unboxing. Iterating `k` matching entries adds output-sensitive work. A comparison of zero identifies the same key in a tree collection, so ordering should be consistent with logical equality; otherwise distinct records may collapse unexpectedly. Know this use case from memory; look up rare navigation names when needed. [TreeMap API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html), [TreeSet API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeSet.html)

#### Stack, queue, and deque with ArrayDeque

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(3);                        // addFirst
int newest = stack.peek();            // known nonempty here
int removed = stack.pop();            // removeFirst, returns removed value

Queue<Integer> queue = new ArrayDeque<>();
queue.offer(3);                       // add at tail
Integer oldest = queue.peek();        // head, or null if empty
Integer polled = queue.poll();        // remove head, or null if empty

Deque<Integer> deque = new ArrayDeque<>();
deque.addFirst(1);
deque.addLast(2);
Integer front = deque.peekFirst();
Integer back = deque.peekLast();
deque.pollFirst();
deque.pollLast();
```

Use `Deque<Integer> stack = new ArrayDeque<>();` for stack behavior instead of legacy `java.util.Stack`. `ArrayDeque` usually has less allocation overhead than a linked-node queue. End insertions are amortized `O(1)` (a growth operation can cost `O(n)`); end reads/removals are `O(1)`. Searching/removing a particular value is `O(n)`. It forbids null elements. `pop`/`removeFirst` throw `NoSuchElementException` when empty; `poll`/`peek` return null, which still causes `NullPointerException` if unboxed. Guard empty state, and never enqueue null tree children. [ArrayDeque API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html)

#### PriorityQueue: a min-heap by default

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
minHeap.offer(5);
minHeap.offer(2);
int smallest = minHeap.peek();        // 2; known nonempty
minHeap.poll();
maxHeap.offer(5);
maxHeap.offer(2);
int largest = maxHeap.poll();         // 5
```

`offer`/`poll` are `O(log n)` heap operations, `peek` is `O(1)`, and `contains`/`remove(Object)` are `O(n)`. Growth can add occasional allocation work to insertion. There is no indexed decrease-key API; Dijkstra normally inserts an updated immutable state and later skips stale entries. Heap iteration is **not sorted**; drain a copy if sorted extraction without mutation is required. Null elements are forbidden, and equal-priority order is unspecified unless you add a tie-breaker. Do not mutate the priority of an element already in the heap. [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)

#### Sorting and safe comparators

```java
int[] values = {7, 1, 4};
Arrays.sort(values);                  // primitive ascending sort
List<Integer> numbers = new ArrayList<>(List.of(7, 1, 4));
numbers.sort(Comparator.reverseOrder());
Collections.sort(numbers);           // also valid; List.sort is direct

int[][] intervals = {{2, 4}, {1, 3}, {1, 5}};
Arrays.sort(intervals, (a, b) -> {
    int byStart = Integer.compare(a[0], b[0]);
    return byStart != 0 ? byStart : Integer.compare(b[1], a[1]);
});                                   // start ascending, end descending
```

A comparator returns **negative / zero / positive**, not a boolean. It must be transitive, sign-symmetric, and consistent on ties. Use `Integer.compare`, `Long.compare`, `Comparator.comparingInt`, or `comparingLong`; `(a, b) -> a - b` can overflow and reverse the ordering. `reversed()` on an entire comparator reverses all keys; reverse only the tie comparator when that is the intent. Comparator overloads apply to object arrays (including `int[][]`), not `int[]`; sort a primitive array ascending then reverse it if needed.

Object-array sorting and `List.sort` are stable; allow `O(n)` temporary reference storage in worst-case analysis. Primitive `Arrays.sort(int[])` is a different implementation; its documented JDK implementation has `O(n log n)` time and implementation-dependent scratch storage. Budget conservatively rather than inferring `O(1)` space from mutation. Key comparisons of cost `C` make comparison sorting `O(C n log n)`. [Arrays API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), [List API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html), [Comparator API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html)

`Arrays.binarySearch` returns an arbitrary matching index, or `-(insertionPoint)-1` if absent; it does not promise the first duplicate. Reconstruct [lower and upper bounds](#102-lower-bound-and-upper-bound) when the task needs boundaries.

> 🌐 **Java Backend Relevance — HIGH:** Explicit comparators make multi-field ordering and tie behavior reviewable. Stable ordering and overflow-safe comparisons matter when sorting application records as well as intervals.

#### Primitive types, wrappers, and numeric safety

| Type | Interview use | Rule |
|---|---|---|
| `int` | Indices, bounded counts, input values | Signed 32-bit; range `-2^31` through `2^31-1` |
| `long` | Sums, products, distances, answer counts | Signed 64-bit; still finite |
| `double` | Averages or approximate real calculations | Binary floating point; not exact for arbitrary large integers |
| `char` | A UTF-16 code unit | Unsigned 16-bit; alphabet assumptions matter |
| `Integer`, `Long` | Values in generic collections, nullable results | Boxing adds references/possible objects; null unboxing throws |

```java
int value = 1_000_000;
long product = (long) value * value;   // promote BEFORE multiplication
long sum = 0L;
int maxInt = Integer.MAX_VALUE;
long maxLong = Long.MAX_VALUE;
double average = 5 / 2.0;             // 2.5; 5 / 2 would be integer 2
long positiveA = 10L, positiveB = 3L;
long ceiling = positiveA / positiveB + (positiveA % positiveB == 0 ? 0 : 1);
```

Integer arithmetic can silently overflow; assigning `a * b` to `long` after an `int` multiplication is too late. Narrowing casts can lose data. Division truncates toward zero; division by integer zero throws `ArithmeticException`. For positive modulus `m`, `Math.floorMod(x, m)` avoids negative residues and the overflow risk of adding `m` to a large remainder. `Math.abs(Integer.MIN_VALUE)` is still negative; promote first when needed. Never add a cost to an infinity sentinel without proving the state is reachable and the finite sum fits. Use checked arithmetic such as `Math.addExact` when the contract calls for reporting overflow; it does not extend the range.

Java masks shift distances: `int` uses the low 5 bits and `long` the low 6. Require `0 <= bit < 32` or `< 64`; use `1L << bit` for long masks. `>>` sign-extends; `>>>` fills with zeros. These rules matter in bitmask bounds, not in every array solution.

> 🌐 **Java Backend Relevance — HIGH:** Nullable wrappers, unboxing, and integer promotion affect totals, counters, and optional data. State the numeric range and null contract instead of relying on accidental behavior.

#### References, mutation, and copying

**Java is always pass-by-value. For objects, the value being copied is the object reference.** A parameter can mutate the referenced object, but reassigning that parameter does not reassign the caller's variable.

```java
static void mutateThenReassign(int[] values) {
    values[0] = 99;                    // caller sees this, require nonempty
    values = new int[] {1, 2};        // only the local reference changes
}

static void demonstrateAliasing() {
    int[] original = {3, 4};
    int[] alias = original;            // same array, O(1)
    int[] copy = original.clone();     // independent primitive values, O(n)
    mutateThenReassign(alias);         // original now {99, 4}; copy still {3, 4}
}
```

| Operation | What gets copied | What remains shared |
|---|---|---|
| `int b = a` | Primitive value | Nothing |
| `int[] b = a` / object parameter | Reference, `O(1)` | The same array/object |
| `a.clone()` / `Arrays.copyOf` on `int[]` | New array of primitive values | Nothing mutable inside its elements |
| Copy `TreeNode[]` or `new ArrayList<>(nodes)` | New container of references | Node objects |
| `grid.clone()` for `int[][]` | Outer array of row references | Rows; clone each row for an independent grid |
| `new ArrayList<>(path)` in backtracking | A snapshot of the list's references | Mutable element objects; boxed integers are safe immutable elements |

For tree/list methods, changing `node.next` or `node.left` mutates an object. Assigning `head = head.next` changes only a local variable; return the new head and let the caller assign it. A `final` reference prevents reassignment, not mutation of the object. Garbage collection reclaims unreachable objects; an unreachable cycle is collectible, but retaining references in a cache/list can retain the whole reachable structure.

**Collection-copy traps:** `Arrays.asList(objectArray)` is a fixed-size view backed by that array: `set` writes through, structural add/remove fails. `Arrays.asList(new int[] {1, 2})` has **one `int[]` element**, not two boxed integers. `List.of(...)` is unmodifiable and rejects nulls; contained mutable objects are still mutable. `new ArrayList<>(existing)` creates a growable shallow copy. These are different contracts, not interchangeable spellings.

> 🌐 **Java Backend Relevance — HIGH:** Mutation ownership and defensive copying prevent callers from changing shared data accidentally. A final field or unmodifiable collection does not make an entire object graph immutable.

#### Small classes and records

```java
record Coordinate(int row, int col) {}
record Job(int start, int end) {}

static void orderJobs(List<Job> jobs) {
    jobs.sort(Comparator.comparingInt(Job::start)
            .thenComparing(Comparator.comparingInt(Job::end).reversed()));
}
```

A record supplies a constructor, accessors such as `row()`, and component-based `equals/hashCode`. Components are final; referenced mutable objects are not deep-copied. In particular, a record containing an `int[]` inherits that array component's identity equality, not content equality. Use mutable classes for list/tree nodes. An interview state `new int[] {row, col}` is quick and practical; a `Coordinate` gives named fields and appropriate value equality for a map key. Choose one representation and document its field order.

> 🌐 **Java Backend Relevance — HIGH:** Small named data types clarify meaning and equality. Records work well for immutable values with suitable components; mutable linked nodes serve a different purpose.

#### Recursion, iteration, nulls, and safe traversal

Java's call stack is finite, and there is no portable safe recursion-depth number. A chain of `O(n)` recursive calls can throw `StackOverflowError`; Java does not guarantee tail-call elimination. Recursive DFS is clear for moderate tree depth and backtracking. Prefer iterative BFS/DFS for unbounded chains, and include explicit stack/queue memory in the analysis. Catching `StackOverflowError` is not a substitute for choosing a suitable traversal.

Do not structurally modify a collection through a separate API while its fail-fast iterator is in progress. Use the iterator's supported `remove()`, use `removeIf`, or collect changes for a later pass. Fail-fast behavior is best-effort bug detection, not a correctness or synchronization guarantee.

```java
List<Integer> values = new ArrayList<>(List.of(-2, 3, -1));
Iterator<Integer> iterator = values.iterator();
while (iterator.hasNext()) {
    if (iterator.next() < 0) {
        iterator.remove();
    }
}
```

> 🌐 **Java Backend Relevance — MEDIUM:** `ArrayList`, `HashMap`, `ArrayDeque`, and `PriorityQueue` are not thread-safe for concurrent mutation. Interview-local state is normally single-threaded; shared application state needs a deliberate synchronization or concurrent-collection contract, including compound operations.

**Five-minute Java recall drill:** Build a count map, drain a min-heap, use a deque both ways, sort two fields without subtraction, and predict the aliasing example. Explain one empty/null case and one operation cost. If an API error appears in practice, record its general rule in the [mistake log](#26-mistake-log-system).

### Priority legend

#### 🔴 Tier 1 — Must Master

Extremely common and foundational. Recognize it quickly, explain it, implement it unaided, and solve standard variations.

#### 🟠 Tier 2 — Very Important

Frequently tested. Understand it well and solve standard problems confidently, but do not let its hardest variants displace Tier 1 practice.

#### 🟡 Tier 3 — Nice to Know

Useful but less frequent. Understand the concept and solve basic versions after the core is solid.

#### ⚪ Tier 4 — Low Priority / Specialized

Rare in general SWE interviews. Study deeply only for algorithm-heavy roles, competitive programming, or evidence that a target company expects it.

### What to study next

Start with [the roadmap](#27-learning-roadmap), not a cover-to-cover read of the reference. In your next session: retrieve one due problem cold, learn one missing invariant, attempt one problem, then write a mistake rule and the next revisit date. Keep only one new pattern active until its mechanics are clear; interleave already learned patterns.

| Tier | First-pass practice budget per major pattern | Memory expectation | Advancement evidence |
|---|---|---|---|
| 🔴 Tier 1 | 3–4 distinct problems plus delayed revisits | Common APIs and core traversal/search shape unaided | Recognize, explain, implement, test, and analyze a variation cold |
| 🟠 Tier 2 | 2–4 distinct problems plus delayed revisits | Reconstruct standard forms from invariant/state | Solve canonical and one variation with no full-solution hint |
| 🟡 Tier 3 | 1–2 selected problems after core gates | Explain idea; reconstruct a basic form if selected | Recognize applicability and trade-offs; consult reference for details |
| ⚪ Tier 4 | 0 by default; 1–2 only with target evidence | Recognition is normally sufficient | Explain why a core alternative is inadequate before investing |

Counts are starting budgets, not quotas or separate charges for every table row. Shared problems count once; a cold revisit is another attempt at an existing problem. Add a new variation only when it addresses a demonstrated gap. Section-level checklists are depth targets for the appropriate tier, not reasons to postpone mixed practice until every box is checked.

### The study loop

For each pattern:

1. **Understand:** explain what problem it removes and its key invariant.
2. **Implement:** write the core operation without copying.
3. **Solve:** do one simple problem, then two or three standard variations.
4. **Struggle productively:** explore before taking a hint, but use a time box.
5. **Review:** compare approaches and record the missed clue or bug.
6. **Re-solve:** return after a delay with no solution visible.
7. **Generalize:** state which changed constraints would make the technique fail.

### How much depth is enough?

For Tier 1, derive and adapt; for Tier 2, handle common forms and explain trade-offs; for Tier 3, recognize it and implement a basic version only when selected; for Tier 4, recognition is normally sufficient. “I watched a video” and “I once accepted a solution” are exposure, not mastery.

### A note on representative problems

Problem names in this guide refer to well-known archetypes. Use LeetCode, NeetCode, HackerRank, CodeSignal, a textbook, or any equivalent source. The learning target is the transferable pattern stated beside the problem—not the platform or a memorized answer.

### Maintenance contract

When extending this guide, integrate new material into the relevant topic instead of appending a duplicate explanation. Preserve useful notes, keep priority labels consistent, update the Table of Contents when major sections change, and cross-reference an existing explanation when only a variation is new.

---

## 2. DSA Interview Priority Map

Priorities reflect typical general Software Engineering coding interviews. A company, level, or role can shift them; use company-specific evidence only after building the common core.

The tiers are curriculum judgments about broad transfer and prerequisite value, not measured company-frequency statistics. “Frequency” below is qualitative; use observed target-role questions to adjust emphasis after mastering the common core.

| Topic or major subtopic | Priority | Frequency | Required depth | Practice priority | Why this priority |
|---|---|---:|---|---|---|
| Big-O, time, and space analysis | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | Every solution is evaluated through correctness and resource cost. |
| Recursion fundamentals | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | It is the natural model for trees, DFS, backtracking, and memoization. |
| Amortized analysis | 🟠 Tier 2 — Very Important | Medium | Working | Medium | It explains dynamic arrays, hash tables, and stack patterns accurately. |
| Interview-useful math | 🟠 Tier 2 — Very Important | Medium | Working | Medium | Logs, arithmetic, modulo, and overflow reasoning support many core algorithms; advanced number theory does not. |
| Bit manipulation basics | 🟡 Tier 3 — Nice to Know | Medium/low | Basic | Medium/low | XOR and masks appear, but far less than core collection patterns. |
| Arrays and strings | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | They are the dominant input form and substrate for many patterns. |
| Prefix sums | 🔴 Tier 1 — Must Master | High | Deep | Very high | They turn repeated contiguous-range work into constant-time queries. |
| Difference arrays | 🟡 Tier 3 — Nice to Know | Low/medium | Basic | Low | Powerful for batch range updates, but less common than prefix sums. |
| Matrix / grid traversal | 🔴 Tier 1 — Must Master | High | Deep | Very high | It tests indexing, traversal, BFS/DFS, and state modeling across a frequent interview input shape. |
| Hash maps and sets | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | Expected linear-time solutions often depend on fast membership or association. |
| Linked lists | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | Reference rewiring is a classic correctness and communication test. |
| Fast/slow pointers and reversal | 🟠 Tier 2 — Very Important | Medium | Strong | High | These cover most high-value linked-list variations. |
| Basic stack use / nested delimiters | 🔴 Tier 1 — Must Master | High | Strong | High | LIFO operations and delimiter invariants underpin traversal and parsing; advanced stack patterns have their own tiers. |
| Monotonic stacks | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | A recurring linear-time answer to next greater/smaller and span questions. |
| Basic FIFO queue use | 🔴 Tier 1 — Must Master | High | Strong | High | FIFO discovery order is required for BFS correctness; double-ended and monotonic variants are learned separately. |
| Monotonic queues | 🟡 Tier 3 — Nice to Know | Low/medium | Basic | Medium/low | Useful for window extrema, but specialized compared with ordinary windows. |
| Two pointers | 🔴 Tier 1 — Must Master | High | Deep | Very high | A broadly reusable way to exploit order or compact data in place. |
| Sliding window | 🔴 Tier 1 — Must Master | High | Deep | Very high | The standard family for contiguous ranges with maintainable constraints. |
| Binary search and bounds | 🔴 Tier 1 — Must Master | High | Deep | Very high | Frequent, deceptively error-prone, and applicable beyond exact lookup. |
| Binary search on answer | 🟠 Tier 2 — Very Important | Medium | Strong | High | Converts monotone feasibility into efficient optimization. |
| Sorting as a problem-solving tool | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | Sorting exposes order and enables intervals, greedy methods, and pointers. |
| Hand-implementing elementary sorts | 🟡 Tier 3 — Nice to Know | Low | Basic | Low | Concepts matter, but production/library sorting is normally used. |
| Binary trees and traversals | 🔴 Tier 1 — Must Master | Very high | Deep | Very high | Trees heavily test recursion, state, and structural reasoning. |
| Binary search trees | 🔴 Tier 1 — Must Master | Medium/high | Deep | High | Ordered-tree invariants produce common search and validation questions. |
| Tree construction and serialization | 🟠 Tier 2 — Very Important | Medium | Strong | Medium/high | Valuable recurring variations, but secondary to traversal and subtree reasoning. |
| Heaps / priority queues | 🟠 Tier 2 — Very Important | High | Strong | High | The default for repeated extrema, Top-K, scheduling, and streaming. |
| Graph BFS and DFS | 🔴 Tier 1 — Must Master | High | Deep | Very high | Core tools for reachability, components, grids, and paths. |
| Topological sorting | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | The standard model for dependencies and directed acyclic order. |
| Union-Find / DSU | 🟠 Tier 2 — Very Important | Medium | Strong | Medium/high | Cleanly handles evolving connectivity and redundant-edge questions. |
| Dijkstra | 🟠 Tier 2 — Very Important | Medium | Strong | Medium/high | It is the principal interview shortest-path algorithm with nonnegative weights. |
| Minimum spanning trees | 🟡 Tier 3 — Nice to Know | Low/medium | Basic | Low/medium | It appears in cost-to-connect problems, but less than reachability and shortest paths. |
| Backtracking | 🟠 Tier 2 — Very Important | High | Strong | High | Required for generating possibilities and constraint search. |
| Greedy reasoning | 🟠 Tier 2 — Very Important | High | Strong | High | Many optimal solutions are short but require a defensible local-choice argument. |
| Intervals | 🔴 Tier 1 — Must Master | High | Deep | Very high | Sorting plus boundary reasoning forms a compact, frequent interview family. |
| Sweep line | 🟠 Tier 2 — Very Important | Medium | Strong | Medium | Useful for event-counting variants after standard intervals are mastered. |
| Dynamic programming fundamentals | 🟠 Tier 2 — Very Important | High | Deep core, selective advanced | High | Common at many companies and central to optimization, but the advanced space is too broad to master uniformly. |
| 1D, grid, and basic knapsack DP | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | These are the most transferable DP state patterns. |
| Interval and tree DP | 🟡 Tier 3 — Nice to Know | Low/medium | Basic/selective | Low/medium | Useful for harder loops, but not the best early return for general interviews. |
| Bitmask DP | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | Low | Exponential subset-state DP is uncommon in ordinary SWE interviews. |
| Tries | 🟡 Tier 3 — Nice to Know | Medium/low | Working | Medium | They are ideal for prefix dictionaries and some word searches, but narrow. |
| Fenwick / segment trees | ⚪ Tier 4 — Low Priority / Specialized | Low | Awareness | Low | Dynamic range-query structures are uncommon outside algorithm-heavy screens. |
| KMP exact string matching | 🟡 Tier 3 — Nice to Know | Low | Basic | Low | Its failure-function idea is a plausible follow-up, but built-ins often suffice. |
| Rabin–Karp / rolling hash | 🟡 Tier 3 — Nice to Know | Low/medium | Basic | Low | Occasionally useful for substring matching, with collision trade-offs. |
| Manacher's algorithm | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | Low | Linear-time palindrome radii are rarely expected in standard SWE interviews. |
| Bellman–Ford / Floyd–Warshall | 🟡 Tier 3 — Nice to Know | Low | Basic | Low | Negative-edge and small all-pairs variants occasionally appear after core shortest paths. |
| Advanced graph algorithms | ⚪ Tier 4 — Low Priority / Specialized | Low | Awareness | Low | Flow, SCC, bridges, and similar algorithms are role/company specific. |
| Computational geometry | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | Low | It is specialized outside graphics, spatial, and algorithmic roles. |

### Core data-structure and algorithm trade-offs

Choose from required operations and invariants, not from familiarity.

| Need | First consideration | Why |
|---|---|---|
| Fast membership | Hash set | Expected `O(1)` lookup without storing a separate value |
| Key → value/count/index | Hash map | Expected `O(1)` association and update |
| Sorted keys / predecessor / range iteration | Balanced tree map/set | `O(log n)` operations while preserving order |
| Indexed contiguous data | Array | `O(1)` random access and strong locality |
| LIFO / nesting / unresolved candidates | Stack | The most recent item must be resolved first |
| FIFO / levels / arrival order | Queue | The earliest discovered item must be processed first |
| Repeated highest/lowest priority | Heap | `O(log n)` updates and `O(1)` access to one extreme |
| Prefix lookup | Trie | A path represents a shared prefix |
| Evolving undirected connectivity | Union-Find | Near-constant amortized merge and connectivity query |

| Choice | Prefer the first when | Prefer the second when | Main trade-off |
|---|---|---|---|
| **Array vs linked list** | You need indexing, cache-friendly scans, or simple storage. | You already have a node and need local `O(1)` link changes. | Arrays shift on middle insertion/deletion; lists require `O(n)` search and extra references. |
| **Hash map vs tree map** | Exact lookup speed matters and order does not. | Sorted traversal, lower bounds, predecessor/successor, or worst-case `O(log n)` matters. | Hashing is expected `O(1)` and unordered; balanced trees are ordered with `O(log n)` operations. |
| **Stack vs queue** | Work is nested, reversible, or last-in-first-out. | Work is layered, arrival-ordered, or first-in-first-out. | The removal order changes traversal and often correctness. |
| **BFS vs DFS** | You need minimum unweighted edges or explicit levels. | You need subtree/postorder state, reachability, or lower memory on very wide graphs. | Both traverse in `O(V+E)`; frontier/call-stack shape and path guarantees differ. |
| **Heap vs sorting** | Data changes/streams, only repeated extremes or small Top-K matter. | You need the complete order or one offline scan after ordering. | Heap Top-K can be `O(n log k)`; sorting is simpler and gives all order in `O(n log n)`. |
| **Greedy vs dynamic programming** | A local choice can be proven safe by exchange or stays-ahead reasoning. | Choices interact and repeated states must preserve alternatives. | Greedy stores little and is often faster; DP is broader but needs a correct state and more resources. |
| **Recursion vs iteration** | The structure is naturally recursive and depth is safe. | Stack limits, explicit traversal control, or low call overhead matters. | Both may use `O(depth)` state; recursion hides it in the call stack. |
| **Memoization vs tabulation** | Only reachable states should be evaluated and recurrence clarity matters. | Evaluation order is known and call-stack overhead should be avoided. | Memoization follows demand; tabulation offers predictable iteration and easier space compression. |
| **Adjacency list vs adjacency matrix** | The graph is sparse or neighbor iteration is common. | The graph is dense or constant-time arbitrary edge tests dominate. | Lists use `O(V+E)` space; matrices use `O(V²)` space and scan all possible neighbors. |

### How to allocate study time

A reasonable default is **65% Tier 1**, **30% Tier 2**, and **5% Tier 3/4** until mock interviews reveal a specific weakness. Priority controls depth, not permission: you may encounter a rare topic, but mastering common reasoning produces a much higher return.

---

## 3. Complexity Analysis and Foundations

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** A vocabulary and reasoning toolkit for describing how an algorithm's work and memory grow as its input grows.
- **Why it exists:** Two correct programs can behave very differently at scale. Complexity lets us compare their scalability without depending on a particular machine.
- **Why it matters in interviews:** Candidates are normally expected to propose a baseline, derive its cost, and justify an optimization. A correct solution with unexplained or unsuitable complexity is often incomplete.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Loops, functions, arrays, and basic algebra.
- **Common use cases:** Comparing approaches, checking a solution against constraints, analyzing recursion, and explaining time–space trade-offs.
- **Common problem patterns:** Nested iteration, divide-and-conquer, traversal, maintaining auxiliary state, and repeated subproblems.
- **Recognition clues:** The prompt gives a large input bound, asks for an “efficient” solution, forbids extra storage, or asks for complexity explicitly.
- **Required depth:** Derive—not guess—the dominant time and auxiliary-space terms for ordinary interview code. Know common operation costs and explain amortized cost. Formal proofs are unnecessary.

> **Why this priority?** Complexity analysis is part of almost every coding interview, and it guides every later choice of data structure and algorithm. It is foundational rather than a standalone niche.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Big-O and growth rates | 🔴 Tier 1 — Must Master | Compare common classes and discard constants/lower terms correctly |
| Deriving time from loops and traversals | 🔴 Tier 1 — Must Master | Count work by input-dependent iterations |
| Auxiliary vs input/output space | 🔴 Tier 1 — Must Master | Include containers and recursion stack |
| Recursion, base cases, and call-stack cost | 🔴 Tier 1 — Must Master | Trace calls and identify repeated work |
| Constraints-to-complexity reasoning | 🔴 Tier 1 — Must Master | Use bounds to reject infeasible approaches |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Amortized analysis | 🟠 Tier 2 — Very Important | Explain dynamic-array append and similar occasional expensive operations |
| Simple recurrence reasoning | 🟠 Tier 2 — Very Important | Recognize linear, branching, and divide-by-two recursion |
| Useful algorithmic mathematics | 🟠 Tier 2 — Very Important | Sums, logs, modular arithmetic, gcd, overflow awareness |
| Bit manipulation basics | 🟡 Tier 3 — Nice to Know | Read/test/set/toggle bits and recognize XOR tricks |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Formal asymptotic proofs and the Master Theorem in full | ⚪ Tier 4 — Low Priority / Specialized | Awareness; derive only standard interview recurrences |
| Advanced amortized methods | ⚪ Tier 4 — Low Priority / Specialized | Not needed for ordinary interviews |
| Number theory beyond gcd/modular basics | ⚪ Tier 4 — Low Priority / Specialized | Study only for a role or company that emphasizes algorithms |

<a id="31-big-o-time-and-space"></a>
### 3.1 Big-O, Time, and Space — 🔴 Tier 1 — Must Master

#### Core intuition

Let `n` describe the growing part of the input. Big-O gives an upper bound on the growth of resource use, usually with the tightest useful class in interview discussion. It answers “what happens as `n` becomes large?” rather than “how many milliseconds on my laptop?”

- `3n + 20` becomes `O(n)`: constants do not change the growth class.
- `n² + 50n` becomes `O(n²)`: the dominant term wins for large `n`.
- Independent inputs stay separate. Traversing arrays of lengths `n` and `m` is `O(n + m)`, not automatically `O(n)`.
- Sequential blocks add; nested dependent work usually multiplies.

Use **worst-case** complexity unless the interviewer or data-structure guarantee calls for average or amortized analysis. For example, hash lookup is commonly described as expected `O(1)`, not an unconditional worst-case guarantee.

#### Growth-rate quick reference

| Class | Typical source | Practical interpretation |
|---|---|---|
| `O(1)` | Array index, a fixed number of operations | Does not grow with input size |
| `O(log n)` | Halving a search space | Excellent; binary search |
| `O(n)` | One full traversal | Usually necessary when every item may matter |
| `O(n log n)` | Efficient comparison sorting; divide-and-conquer | Usually acceptable for large interview inputs |
| `O(n²)` | Comparing every pair; full nested grid over `n × n` | Often too slow once `n` reaches tens of thousands |
| `O(n³)` | Three independent choices | Usually only viable for small `n` |
| `O(2^n)` | Include/exclude branching | Only small `n`; common backtracking baseline |
| `O(n!)` | Enumerating permutations | Only very small `n` |

Approximate constraint heuristics—not laws, because compilers, platforms, and operation costs differ:

| Input size | Complexity worth considering |
|---:|---|
| `n ≤ 10–12` | `O(n!)` may be intended |
| `n ≤ 20–25` | `O(2^n)` may be intended |
| `n ≤ 100` | `O(n³)` may be possible |
| `n ≤ 2,000` | `O(n²)` may be possible |
| `n ≤ 100,000–1,000,000` | Usually `O(n log n)` or `O(n)` |
| Huge numeric range, few elements | Look for `O(log range)`, hashing, or sparse representation |

#### Derive time from code

Do not count indentation alone. Count how many times the body executes.

```text
for i from 0 through n-1 do             (n iterations)
    perform constant work               (O(1))
end for                                  total O(n)

for i from 0 through n-1 do
    for j from 0 through n-1 do          (n iterations for each i)
        perform constant work
    end for
end for                                  total O(n²)

for i from 0 through n-1 do
    for j from i+1 through n-1 do        ((n-1)+(n-2)+...+1 iterations)
        compare positions i and j
    end for
end for                                  total O(n²), despite the triangular shape

i = 1
while i < n do
    i = 2 * i
end while                                number of doublings is O(log n)
```

Two nested pointers can still be linear. If `right` advances `n` times and `left` advances at most `n` times over the **entire** algorithm, the total is `O(n)`, not `O(n²)`. This is the key accounting argument behind sliding windows.

If an operation inside the loop is not constant, include it. Sorting inside an `n`-iteration loop has worst-case `O(n · k log k)` time when each iteration uses an `O(k log k)` comparison sort on `k` items; adaptive sorts may do less work on easier inputs.

#### Derive space correctly

Distinguish:

- **Input space:** Memory already occupied by the provided input; normally excluded from auxiliary-space claims.
- **Output space:** Sometimes reported separately because the required output cannot be avoided.
- **Auxiliary space:** Extra working memory created by the algorithm.
- **Call-stack space:** Recursive frames count even when no explicit array or map is created.

Examples:

| Algorithm shape | Time | Auxiliary space |
|---|---:|---:|
| Scan array and keep two counters | `O(n)` | `O(1)` |
| Copy or hash every element | `O(n)` | `O(n)` |
| Recursive linked-list traversal | `O(n)` | `O(n)` stack |
| Binary-tree DFS | `O(n)` | `O(h)`, typically `O(log n)`, worst `O(n)` |
| Sort in place | Depends on library/algorithm | Do not claim `O(1)` without knowing its implementation |

#### Recurring whole-algorithm examples

| Example | How to derive time | Time | Auxiliary-space idea |
|---|---|---:|---|
| BFS/DFS with an adjacency list | Each vertex is discovered once; across all vertices, every stored edge is examined once (twice for a stored undirected edge, still a constant factor). | `O(V + E)` | `O(V)` for visited/frontier, excluding the graph |
| Keep Top-K with a size-`k` heap | Each of `n` items performs at most one `O(log k)` heap update. | `O(n log k)` | `O(k)` |
| Sort then scan | One comparison sort dominates one linear pass. | `O(n log n) + O(n) = O(n log n)` | Depends on sorting implementation, plus output |
| DP over an `n × target` table | There are `n × target` distinct states and constant work per state. | `O(n × target)` | `O(n × target)`, or less only after proving safe compression |
| Backtracking over all subsets | A binary take/skip tree has about `2^n` leaves; copying each length-`n` output can add a factor of `n`. | `O(2^n)` search, up to `O(n·2^n)` with output copies | `O(n)` path/stack excluding output |

#### Recognition and interview use

Ask these questions before optimizing:

1. What does `n` mean? Are there two dimensions or two independent inputs?
2. How many times can each element, edge, or state be processed?
3. What is the cost of operations called inside the loop?
4. Does recursion branch, shrink by one, or shrink by a factor?
5. Which extra structures and stack frames exist at peak usage?
6. Do the stated constraints permit this class?

#### Common mistakes, edge cases, and trade-offs

- Calling any nested loop `O(n²)` without checking whether the pointers move monotonically.
- Calling a hash-based solution worst-case `O(1)` per operation; say **expected `O(1)`** when precision matters.
- Omitting recursion-stack space.
- Treating an `n × m` matrix as `n²`; its size is `nm` unless it is known to be square.
- Claiming `O(1)` space after sorting without knowing whether the sort allocates memory.
- Counting a required output array/list as auxiliary storage without stating the convention.
- Optimizing away useful memory prematurely. An `O(n)` map that reduces `O(n²)` time to `O(n)` is frequently the right interview trade-off.

<a id="32-amortized-complexity"></a>
### 3.2 Amortized Complexity — 🟠 Tier 2 — Very Important

#### Intuition and mechanics

An operation can occasionally be expensive while a long sequence remains cheap on average. A dynamic array usually appends in `O(1)`. When capacity is exhausted, it allocates a larger block and copies existing elements, an `O(n)` event. Because capacity normally grows geometrically, those copies happen rarely; `n` appends cost `O(n)` total, so each append is **amortized `O(1)`**.

This differs from:

- **Average-case analysis:** Assumes a distribution of inputs.
- **Amortized analysis:** Guarantees the average per operation over any sufficiently long operation sequence under the data structure's rules.

Typical interview examples include dynamic-array append and monotonic-stack algorithms: an element may trigger several pops in one iteration, but each element is pushed and popped at most once, so total work is `O(n)`.

**Mistake to avoid:** Calling an individual resize `O(1)`. The individual event is `O(n)`; the sequence gives amortized `O(1)` append.

<a id="33-recursion-and-iteration"></a>
### 3.3 Recursion and Iteration — 🔴 Tier 1 — Must Master

#### Core model

Every recursive solution needs:

1. A state: what this call is responsible for.
2. Progress: how the next call becomes smaller or closer to completion.
3. A base case: when recursion stops.
4. A way to combine or propagate results.

```text
function solve(state)
    if state is terminal then
        return base_value
    end if
    result = solve(smaller_state)
    return combine(current_choice, result)
end function
```

#### Reasoning about recursive cost

- One call on `n-1`: depth and often time are `O(n)`.
- One call on `n/2`: depth is `O(log n)`.
- Two calls on `n-1` without memoization: often exponential because the recursion tree branches.
- DFS that visits each node once: `O(number of nodes + edges)` for a graph, even though it is recursive.

#### Recursion vs iteration

| Prefer recursion when | Prefer iteration when |
|---|---|
| The structure is recursive: trees, divide-and-conquer, backtracking | A simple loop expresses the state clearly |
| Backtracking requires natural choose/recurse/unchoose flow | Input depth may exhaust the finite Java call stack and throw `StackOverflowError` |
| Recursive clarity outweighs stack overhead | Constant auxiliary space is important and achievable |

Both can express many of the same algorithms. Recursion uses an implicit call stack; iteration may use explicit state or an explicit stack. Do not rewrite elegant tree DFS iteratively merely to claim superiority, but do discuss deep-tree stack risks.

**Common failures:** Missing or overly broad base cases, no progress, mutating shared state without undoing it, returning from only one branch, recomputing the same state, and confusing recursion depth with total number of calls.

<a id="34-mathematics-useful-in-interviews"></a>
### 3.4 Mathematics Useful in Interviews — 🟠 Tier 2 — Very Important

Focus on practical tools:

| Tool | Why it matters | Typical use | Cost |
|---|---|---|---:|
| Arithmetic-series sum `1+…+n = n(n+1)/2` | Explains triangular nested loops | Pair counts, missing number | `O(1)` formula |
| Logarithms | Count repeated halving/doubling | Binary search, balanced trees | Usually `O(log n)` steps |
| Remainder/modulo | Wrap indices or track residue classes | Circular arrays, divisible subarrays | `O(1)` per operation |
| `gcd` via Euclid | Reduce ratios; cycle/step reasoning | Fraction normalization | `O(log min(a,b))` |
| Integer division and ceiling division | Bound groups/pages | Search-on-answer feasibility | For positive values: `a / b + (a % b == 0 ? 0 : 1)` |
| Overflow awareness | Prevent silent wrong answers | Midpoints, sums, products | Use wider type or rearrange safely |

In Java, integer division truncates toward zero and a negative dividend can produce a negative remainder. For positive `m`, use `Math.floorMod(x, m)` when mathematical nonnegative modulo is required. Promote before multiplication, for example `(long) n * (n + 1L) / 2`, and prove that the final result fits.

<a id="35-bit-manipulation-basics"></a>
### 3.5 Bit Manipulation Basics — 🟡 Tier 3 — Nice to Know

Bits are compact Boolean flags. They matter occasionally in interviews but should not displace arrays, hashing, trees, graphs, or DP.

| Operation | Meaning |
|---|---|
| `x & (1 << i)` | Test bit `i` |
| `x \| (1 << i)` | Set bit `i` |
| `x & ~(1 << i)` | Clear bit `i` |
| `x ^ (1 << i)` | Toggle bit `i` |
| `x & (x - 1)` | Remove the lowest set bit |
| `x > 0 && (x & (x - 1)) == 0` | Test power of two |

XOR is associative and `a ^ a = 0`, so paired values cancel. This supports “one value appears once, all others twice.” Be careful: a clever XOR solution usually relies on strict occurrence assumptions and is less general than a frequency map.

- **Time:** Usually `O(1)` per machine-word operation; scanning values is `O(n)`.
- **Space:** Usually `O(1)`.
- **Trade-off:** Compact and fast, but easy to make unreadable. Explain the invariant instead of presenting it as magic.
- **Optional / Specialized:** Bitmask enumeration and bitmask DP are advanced topics covered later, not prerequisites here.

### Selected foundation practice ladder

Use four short exercises, not an extra platform checklist: **mechanics** — count work in a scan and triangular loop; **canonical** — analyze and implement array reversal; **variation** — contrast recursive and iterative stack space; **mixed** — compare duplicate detection by brute force, sorting, and hashing. **Cold revisit:** re-derive all three duplicate-detection costs after several days. Fibonacci's full DP progression belongs in Section 18; do not block basic arrays on DP mastery.

> ⭐ **Canonical Interview Problem:** Reverse String / array reversal. Connect the symmetric-position invariant to the actual Java mutations and space cost.

### Representative Foundation Exercises

#### Beginner

- **Analyze small code fragments:** Derive time and space for independent loops, nested loops, and halving loops. Learn to count executions rather than recognize shapes.
- **Reverse an array iteratively and recursively:** Compare `O(1)` explicit auxiliary space with `O(n)` call-stack space.
- **Power of Two:** Practice a small, explainable bit invariant; also solve it by repeated division to compare approaches.

#### Core Interview

- **Find the Duplicate by pair comparison, sorting, and hashing:** Practice moving from `O(n²)` to `O(n log n)` to expected `O(n)` and stating mutation/space trade-offs.
- **Fibonacci: recursion → memoization → iteration:** Observe exponential repeated work, linear state caching, and constant-space optimization.
- **Dynamic-array growth thought experiment:** Explain why a sequence of appends is amortized linear overall.

#### Advanced

- **Count work in a monotonic-stack trace:** Justify `O(n)` through push/pop accounting despite an inner `while`.
- **Analyze balanced vs skewed tree recursion:** Separate node count `n` from height `h` and distinguish total time from peak stack space.

### Foundation Mastery Checklist

- [ ] I can explain `O(1)`, `O(log n)`, `O(n)`, `O(n log n)`, `O(n²)`, `O(2^n)`, and `O(n!)` without notes.
- [ ] I can derive complexity for sequential loops, nested loops, two monotonic pointers, recursion, sorting, hash operations, heaps, graph traversal, and DP-state iteration.
- [ ] I distinguish worst-case, expected, and amortized complexity.
- [ ] I include containers and recursive stack frames in auxiliary-space analysis.
- [ ] I use input constraints to reject approaches before coding.
- [ ] I can explain why a dynamic-array append is amortized `O(1)`.
- [ ] I can state the state, progress, base case, and return meaning of a recursive function.
- [ ] I know the practical math and bit operations above, without forcing them onto unrelated problems.

---

<a id="4-arrays-strings"></a>
## 4. Arrays & Strings

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** Java uses fixed-length primitive arrays such as `int[]`, resizable lists such as `ArrayList<Integer>`, and immutable `String` values. Choose the representation from the operations required.
- **Why it exists:** Indexed sequential storage gives fast access and efficient traversal, making it the default representation for many problems.
- **Why it matters in interviews:** Arrays and strings are the most common input forms and the surface on which hashing, two pointers, windows, binary search, sorting, greedy reasoning, and DP are practiced.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Loops, indexing, complexity analysis, and Java references, mutation, and copying.
- **Common use cases:** Scans, aggregation, in-place transformation, contiguous ranges, frequency counting, and matrices/grids.
- **Common problem patterns:** Running state, prefix aggregates, write pointers, marking, rotation, range updates, and row/column traversal.
- **Recognition clues:** Indexed sequence, contiguous subarray/substring, range query, “in place,” “preserve order,” duplicates, or a 2D grid.
- **Required depth:** Deep. Trace indices safely, derive invariants, choose between copying and in-place work, and connect arrays to the patterns in the next sections.

> **Why this priority?** Most interview techniques either consume arrays/strings or use arrays internally. Weak indexing and boundary reasoning causes failures even when the higher-level algorithm is correct.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Traversal and running state | 🔴 Tier 1 — Must Master | One-pass invariants, min/max/count, adjacent comparisons |
| In-place operations and read/write pointers | 🔴 Tier 1 — Must Master | Mutation, compaction, reversal, order preservation |
| Prefix sums | 🔴 Tier 1 — Must Master | Range sum and prefix-frequency transformations |
| Frequency counting | 🔴 Tier 1 — Must Master | Fixed alphabet arrays vs hash maps |
| Strings and character processing | 🔴 Tier 1 — Must Master | Mutability, building output efficiently, normalization |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Matrix/grid traversal | 🔴 Tier 1 — Must Master | Boundaries, direction offsets, visited/state marking |
| Multi-dimensional prefix sums | 🟡 Tier 3 — Nice to Know | Basic rectangle-sum idea |
| Difference arrays | 🟡 Tier 3 — Nice to Know | Many offline range updates |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Advanced string matching | 🟡 Tier 3 — Nice to Know | KMP/Rabin–Karp awareness; addressed in advanced topics |
| High-dimensional prefix structures | ⚪ Tier 4 — Low Priority / Specialized | Rare in general interviews |

<a id="41-traversal-and-running-invariants"></a>
### 4.1 Traversal and Running Invariants — 🔴 Tier 1 — Must Master

#### Intuition and use

A scan is not merely “loop through the array.” Define what is true before or after each index. Examples:

- `best` is the best answer among positions already processed.
- `running_sum` equals the sum through the current index.
- `write` is the next position where a kept value belongs.
- `seen` describes exactly the values in the processed prefix.

That sentence is the **loop invariant**. It guides initialization, updates, and the returned result.

```java
static OptionalInt maximumValue(int[] values) {
    if (values.length == 0) {
        return OptionalInt.empty();
    }
    int best = values[0];
    for (int i = 1; i < values.length; i++) {
        best = Math.max(best, values[i]);
    }
    return OptionalInt.of(best);
}
```

- **When to use:** Every element may affect a small running summary.
- **Recognition:** Asked for a count, extreme, total, trend, or transformation with no need to revisit arbitrary prior positions.
- **Time:** `O(n)`; reading all items is often a lower bound.
- **Space:** `O(1)` if only fixed state is maintained; output storage may be additional.
- **Empty-input semantics:** This version returns `OptionalInt.empty()` for an empty array. If the contract guarantees nonempty input, a plain `int` return is simpler. `OptionalInt` is an occasional lookup API; agree on absence semantics before coding.
- **Edge cases:** Empty input, one element, all negative values, duplicates, and values at numeric limits.
- **Alternative:** Sorting can expose order but usually costs `O(n log n)` and may mutate input.

<a id="42-in-place-operations"></a>
### 4.2 In-Place Operations — 🔴 Tier 1 — Must Master

“In place” usually means `O(1)` auxiliary storage, not literally zero variables. Common techniques:

- **Swap symmetric positions:** Reversal and partitioning.
- **Read/write compaction:** Read every element; write only retained values to the front.
- **Encode state in the input:** Negate, mark, or reuse cells only when input constraints make the encoding unambiguous.
- **Cycle movement:** Rotate or permute without an extra full array; useful but more error-prone.

Read/write compaction sketch:

```text
write = 0
for read from 0 through n-1 do
    if a[read] should be kept then
        a[write] = a[read]
        write = write + 1
    end if
end for
return write                              (the valid prefix length)
```

- **Recognition:** “Modify the array,” “in place,” “remove duplicates,” “move zeros,” or “use constant extra space.”
- **Time:** Usually `O(n)`.
- **Space:** `O(1)` auxiliary.
- **Invariant:** The half-open prefix `a[0..write)` contains exactly the kept values from the processed input, in the required order.
- **Common mistake:** Overwriting unread data. A forward compaction is safe when `write ≤ read`; other transformations may require traversing backward.
- **Trade-off:** Mutation saves memory but can surprise callers, destroy original information, and complicate debugging. State it explicitly.

#### Java compaction and reversal

The compaction method retains nonzero values and returns a valid prefix length; it does not promise zero-filled trailing cells. For Move Zeroes, fill the remaining suffix with zeros after compaction. Both methods mutate the supplied array and use `O(1)` auxiliary space.

```java
static int compactNonzero(int[] values) {
    int write = 0;
    for (int read = 0; read < values.length; read++) {
        if (values[read] != 0) {
            values[write++] = values[read];
        }
    }
    return write;
}

static void reverseArray(int[] values) {
    for (int left = 0, right = values.length - 1; left < right; left++, right--) {
        int temporary = values[left];
        values[left] = values[right];
        values[right] = temporary;
    }
}
```

<a id="43-prefix-sums"></a>
### 4.3 Prefix Sums — 🔴 Tier 1 — Must Master

#### Intuition

Precompute cumulative information so a range can be answered by subtracting two prefixes. With an exclusive prefix array:

```java
static long[] buildPrefix(int[] values) {
    long[] prefix = new long[values.length + 1];
    for (int i = 0; i < values.length; i++) {
        prefix[i + 1] = prefix[i] + values[i];
    }
    return prefix;
}

// Require 0 <= left <= right < number of original values.
static long inclusiveRangeSum(long[] prefix, int left, int right) {
    return prefix[right + 1] - prefix[left];
}
```

The leading zero makes boundaries uniform: `prefix[i]` is the sum of the first `i` values.

- **When to use:** Many static range-sum queries; subarray conditions transformable into relations between two prefixes; cumulative balance problems.
- **Recognition:** “Sum/count between `l` and `r`,” many queries over unchanged data, contiguous subarrays, or target-sum subarrays.
- **Build:** `O(n)` time and `O(n)` space; each range query is `O(1)`.
- **Running-prefix variant:** For a single pass, keep only current prefix plus a map/set of prior prefixes.
- **Alternative:** A direct range scan is `O(length)` per query; a Fenwick/segment tree is appropriate only when values also change frequently.

For a subarray `l..r`, `sum(l..r) = prefix[r+1] - prefix[l]`. Therefore a subarray ending now has target `k` if an earlier prefix equals `current_prefix - k`. This algebra is the bridge from range sums to hash-map counting.

**Mistakes and edge cases:** Mixing inclusive/exclusive definitions, forgetting the initial zero, integer overflow, assuming nonnegative values when negative values invalidate a sliding-window approach, and storing only a set when the number of occurrences matters.

<a id="44-difference-arrays"></a>
### 4.4 Difference Arrays — 🟡 Tier 3 — Nice to Know

A difference array stores changes between adjacent positions. To add `delta` to every index in inclusive range `[l, r]`:

```text
diff[l] = diff[l] + delta
if r + 1 < n then
    diff[r + 1] = diff[r + 1] - delta
end if
```

One final prefix sum reconstructs all values.

- **Use:** Many **offline** range updates, followed by final values or a scan.
- **Recognition:** Repeated “add to every value in interval” operations; no need to query arbitrary intermediate states.
- **Time:** `O(n + q)` for `q` updates, rather than `O(nq)` worst case.
- **Space:** `O(n)` unless the input can safely hold differences.
- **Trade-off:** Excellent for batch updates; unsuitable when each update must be followed immediately by arbitrary online queries.
- **Typical mistake:** Placing the negative boundary at `r` instead of `r + 1` for inclusive ranges.

<a id="45-frequency-counting"></a>
### 4.5 Frequency Counting — 🔴 Tier 1 — Must Master

Frequency counting compresses a sequence into `value → count`.

- Use a fixed array when the domain is small and known, such as lowercase English letters. It has low overhead and deterministic indexing.
- Use a hash map for large, sparse, negative, string, or otherwise unconstrained keys.
- Use sorting plus a scan when mutation is allowed, sorted output helps, or constant extra structure is preferred.

| Approach | Time | Extra space | Best when |
|---|---:|---:|---|
| Fixed count array | `O(n + alphabet)` | `O(alphabet)` | Small known key range |
| Hash map | Expected `O(n)` | `O(k)` distinct keys | General keys and fast lookup |
| Sort then count runs | `O(n log n)` | Sort-dependent | Ordering is useful or hashing is restricted |

Typical patterns: anagrams, majority/frequency, duplicates, bucket by count, and window validity. Define whether case, Unicode normalization, whitespace, and punctuation matter before processing strings.

<a id="46-matrix-and-grid-problems"></a>
### 4.6 Matrix and Grid Problems — 🔴 Tier 1 — Must Master

A matrix is usually `rows × cols`; traversal is `O(rows · cols)`, not automatically `O(n²)`. Grid problems range from simple iteration to graph search; DFS/BFS details belong in the graph section, but safe representation starts here.

```java
// Require a valid (row, col) in a rows-by-cols rectangle.
static List<int[]> validNeighbors(int row, int col, int rows, int cols) {
    int[] directions = {1, 0, -1, 0, 1};
    List<int[]> neighbors = new ArrayList<>(4);
    for (int direction = 0; direction < 4; direction++) {
        int nextRow = row + directions[direction];
        int nextCol = col + directions[direction + 1];
        if (0 <= nextRow && nextRow < rows && 0 <= nextCol && nextCol < cols) {
            neighbors.add(new int[] {nextRow, nextCol});
        }
    }
    return neighbors;
}
```

- **Recognition:** Cell neighbors, islands/regions, shortest moves, row/column constraints, rotation, spiral traversal.
- **State choices:** Separate `visited` set/array, in-place marking, or a state matrix. In-place marking is memory-efficient but mutates input.
- **Time:** A full traversal is `O(rows · cols)`; graph traversal remains linear in cells plus neighbor edges.
- **Space:** `O(1)` for pure traversal; up to `O(rows · cols)` for visited/frontier/recursion.
- **Mistakes:** Swapping row and column bounds, assuming nonempty matrix, revisiting cells, marking visited too late, and modifying a cell before its original value has been used.

For repeated immutable rectangle-sum queries, a 2D prefix sum gives `O(1)` queries after `O(rows · cols)` preprocessing. Memorize the inclusion–exclusion idea, not a brittle formula: take the large prefix, subtract the two outside strips, add back their overlap.

### Arrays and Strings: Recognition and Trade-offs

| Problem clue | First approaches to consider | Key question |
|---|---|---|
| One aggregate over all elements | Linear scan | What invariant summarizes the processed prefix? |
| Modify while preserving kept order | Read/write pointers | Can unread data be overwritten? |
| Static range sums/counts | Prefix sums | Are queries numerous enough to justify preprocessing? |
| Many offline range increments | Difference array | Are intermediate online answers unnecessary? |
| Small fixed alphabet | Count array | Is the domain truly bounded and normalized? |
| Contiguous segment with a condition | Window or prefix relation | Are values nonnegative/monotone, or can negatives occur? |
| Grid neighbors/regions | Direction offsets + DFS/BFS | What is a node, neighbor, and visited state? |

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Scan / prefix | Running Sum of 1D Array | Product of Array Except Self | Range Sum Query — Immutable | Subarray Sum Equals K | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| In-place / grid | Move Zeroes | Set Matrix Zeroes | Rotate Image | Spiral Matrix | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Product of Array Except Self; Set Matrix Zeroes. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Running Sum of 1D Array:** Learn exclusive vs inclusive prefix definitions; be able to do it with and without an output array.
- **Move Zeroes:** Practice the read/write invariant and stable in-place compaction.
- **Valid Anagram:** Choose between a fixed frequency array, map, and sorting; state assumptions about the alphabet.
- **Richest Customer Wealth / row maximum:** Practice `rows × cols` reasoning and empty-shape assumptions.

#### Core Interview

- **Product of Array Except Self:** Learn left/right prefix products and how to reduce extra arrays to output storage; do not use division unless constraints explicitly allow it.
- **Subarray Sum Equals K:** Convert a subarray equation into previous-prefix lookup; learn why counts, not just membership, are required.
- **Set Matrix Zeroes:** Practice using first row/column as markers and managing marker collisions.
- **Rotate Image:** Decompose a 2D transformation into transpose plus reversal, and reason about in-place indices.
- **Spiral Matrix:** Learn explicit boundaries and termination when a layer has one row or one column.

#### Advanced

- **Range Addition / flight bookings type:** Learn the difference-array boundary technique and offline-update trade-off.
- **Maximum-size subarray with a balance condition:** Encode a running balance and store the earliest index for maximum length.
- **2D Region Sum Query:** Apply inclusion–exclusion carefully after 2D prefix preprocessing.

### Common Mistakes and Interview Tips

- `String` is immutable. Use `char[]` for indexed mutation or `StringBuilder` for incremental output. Repeated concatenation, front insertion, or middle deletion can be quadratic; use the [Java reference](#java-for-dsa-interviews-essential-reference) for copying and Unicode rules.
- State whether the input may be mutated before using an in-place approach.
- Name index semantics: “`right` is inclusive” or “the window is `[left, right)`.”
- Do not use a sliding window for arbitrary negative values unless a monotonic property still holds; prefix sums plus hashing may be correct.
- For grid code, check `grid.length == 0` before reading `grid[0]`, then calculate `rows` and `cols` once. For rectangular algorithms, assume non-null rows of equal length; otherwise use each row's length.
- Manually test empty, one element, all equal, duplicates, negative values, already transformed input, first/last range, one row, and one column.

### Arrays and Strings Mastery Checklist

- [ ] I can define and maintain a loop invariant during a one-pass scan.
- [ ] I can implement reversal and stable read/write compaction from scratch.
- [ ] I can build an exclusive prefix array and derive a range query without guessing indices.
- [ ] I recognize when prefix sums plus hashing are safer than sliding windows.
- [ ] I can explain and implement an offline difference-array update.
- [ ] I choose deliberately among fixed frequency arrays, hash maps, and sorting.
- [ ] I traverse rectangular matrices safely and analyze `O(rows · cols)` time.
- [ ] I can discuss mutation, output-space, string-building, and overflow trade-offs.
- [ ] I can solve beginner problems reliably and standard medium prefix/in-place/matrix problems without a full solution hint.

---

## 5. Hashing

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** Hashing maps a key to a bucket so a hash map can store key–value associations and a hash set can store unique keys with expected constant-time access.
- **Why it exists:** It trades additional memory and loss of natural ordering for much faster membership, lookup, counting, and grouping than repeated scans.
- **Why it matters in interviews:** A hash map or set is often the decisive optimization from `O(n²)` to expected `O(n)`.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Arrays/strings, key equality, and expected vs worst-case complexity.
- **Common use cases:** Membership, complements, deduplication, counts, grouping, caching, index lookup, and prefix-state lookup.
- **Common problem patterns:** “Have I seen this?”, “how many?”, value-to-index mapping, canonical signatures, and storing the first/last occurrence.
- **Recognition clues:** Fast lookup, duplicates, pairs, counts, grouping, matching, or repeated work keyed by a state.
- **Required depth:** Deep usage and invariants; know high-level collision and hashability concepts, but implementing a production hash table is usually unnecessary.

> **Why this priority?** Hashing is one of the most frequent interview tools and unlocks linear-time solutions across arrays, strings, graphs, prefix sums, and dynamic programming.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Hash map lookup and insertion | 🔴 Tier 1 — Must Master | Value/index maps, default handling, update timing |
| Hash set membership and deduplication | 🔴 Tier 1 — Must Master | Seen-state invariants |
| Frequency tables | 🔴 Tier 1 — Must Master | Counts, grouping, matching, window state |
| One-pass complement lookup | 🔴 Tier 1 — Must Master | Check-before-insert reasoning |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Canonical keys for grouping | 🟠 Tier 2 — Very Important | Sorted signatures and count signatures |
| Prefix state + hash map | 🟠 Tier 2 — Very Important | Count or longest-range variants |
| Hash-table internals | 🟡 Tier 3 — Nice to Know | Collisions, load factor, resizing at a conceptual level |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Implementing a robust hash table | ⚪ Tier 4 — Low Priority / Specialized | Only if explicitly requested |
| Custom rolling hashes | ⚪ Tier 4 — Low Priority / Specialized | Collision-sensitive string algorithms; covered later |

<a id="51-hash-map-and-hash-set-fundamentals"></a>
### 5.1 Hash Map and Hash Set Fundamentals — 🔴 Tier 1 — Must Master

#### Core intuition and mechanics

A hash function converts a key into an integer-like code, which selects a storage bucket. Different keys may collide, so the table must resolve collisions and still compare keys for equality. Resizing keeps the load factor manageable.

This yields typical interview costs:

| Operation | Hash map / set expected | Worst case | Notes |
|---|---:|---:|---|
| Lookup | `O(1)` | `O(n)` | Depends on hashing, collisions, and implementation |
| Insert/update | `O(1)` amortized expected | `O(n)` | A resize can be expensive occasionally |
| Delete | `O(1)` expected | Do not assume constant worst case | `HashMap`/`HashSet` provide no sorted-order guarantee |
| Iterate all entries | `O(k + capacity)` | `O(k + capacity)` | `k` stored keys; avoid excessive preallocation |

Hash keys must have stable equality and a compatible hash. Java does not prevent you from mutating a stored key; changing equality/hash fields breaks lookup assumptions. Primitive wrappers and strings are useful immutable keys. Use records with suitable immutable components or an unambiguous encoded signature for compound state; raw arrays have identity equality. Costs above assume constant-cost hashing/equality; include string-key length and resize work as described in the [Java reference](#java-for-dsa-interviews-essential-reference).

#### Set pattern: “Have I seen this?”

```java
static boolean containsDuplicate(int[] values) {
    Set<Integer> seen = new HashSet<>();
    for (int value : values) {
        if (!seen.add(value)) {
            return true;
        }
    }
    return false;
}
```

The crucial design choice is **when** to insert. Checking before insertion prevents the current item from matching itself. In other problems, pre-populating all values or removing the current value may be appropriate; state the invariant.

#### Map pattern: retain the information future positions need

```java
// Return zero-based indices, or an empty array when no answer exists.
static int[] findPairIndices(int[] values, long target) {
    if (target < 2L * Integer.MIN_VALUE || target > 2L * Integer.MAX_VALUE) {
        return new int[0];
    }
    Map<Long, Integer> position = new HashMap<>();
    for (int i = 0; i < values.length; i++) {
        long needed = target - values[i];
        Integer previousIndex = position.get(needed);
        if (previousIndex != null) {
            return new int[] {previousIndex, i};
        }
        position.put((long) values[i], i);
    }
    return new int[0];
}
```

Here `position` contains eligible indices strictly before `i`. Storing an index rather than only membership is driven by the required output.

<a id="52-frequency-tables-and-lookup-techniques"></a>
### 5.2 Frequency Tables and Lookup Techniques — 🔴 Tier 1 — Must Master

Choose what the map value means:

- `value → count`: frequency, multiset equality, or window validity.
- `value → first index`: longest distance/range; do not overwrite the earliest occurrence.
- `value → latest index`: most recent conflict or boundary.
- `key → List<Item>`: grouping.
- `state → number of prior occurrences`: count ranges/pairs.
- `state → best result so far`: memoization or DP.

For counting subarrays with sum `k`:

```text
count_by_prefix initially maps 0 to 1
prefix = 0
answer = 0
for each value in values do
    prefix = prefix + value
    answer = answer + lookup(count_by_prefix, prefix - k, default 0)
    increment count_by_prefix[prefix]
end for
```

The initial mapping from prefix `0` to count `1` represents an empty prefix, allowing a valid subarray that begins at index `0`. In Java, seed a `Map<Long, Long>` with `countByPrefix.put(0L, 1L);`. Update **after** querying so an empty current subarray is not accidentally counted when inappropriate.

#### Java prefix-frequency implementation

This handles negative values and zero targets. Both the accumulated sum and answer count use `long`. For any Java `int[]`, prefix sums fit `long`; an arbitrary extreme target is rejected before subtraction could overflow. Time is expected `O(n)` and auxiliary space `O(n)`.

```java
static long countSubarraysWithSum(int[] values, long target) {
    long minimumPossible = (long) values.length * Integer.MIN_VALUE;
    long maximumPossible = (long) values.length * Integer.MAX_VALUE;
    if (target < minimumPossible || target > maximumPossible) {
        return 0L;
    }
    Map<Long, Long> countByPrefix = new HashMap<>();
    countByPrefix.put(0L, 1L);
    long prefix = 0L;
    long answer = 0L;
    for (int value : values) {
        prefix += value;
        answer += countByPrefix.getOrDefault(prefix - target, 0L);
        countByPrefix.put(prefix, countByPrefix.getOrDefault(prefix, 0L) + 1L);
    }
    return answer;
}
```

#### Canonical grouping keys — 🟠 Tier 2 — Very Important

To group objects that are equivalent under a transformation, map each object to a canonical signature:

- Sort characters: `O(L log L)` per string of length `L`.
- Count a fixed alphabet and use a stable signature: encode all counts with delimiters into a `String`, or use an immutable content-key class with matching `equals/hashCode`. An `int[]` itself is not a content-based hash key. Building the signature costs `O(L + alphabet)`.
- Normalize signs or divide by gcd for ratios, being careful with zero.

The signature must satisfy: equivalent objects have the same key, and non-equivalent objects should not accidentally share it.

### How to Recognize Hashing

| Clue | Map/set design | Frequent trap |
|---|---|---|
| Find a pair satisfying a relation | Store previously seen complement-relevant values | Matching an item with itself |
| Detect duplicates | Set of seen values | Returning before/after the required distance condition |
| Count occurrences | Map value to count | Deleting or decrementing incorrectly |
| Longest span with same state | State to earliest index | Overwriting the earliest index |
| Number of spans with same relation | State to occurrence count | Using a set when multiplicity matters |
| Group equivalent items | Canonical signature to group | Mutable or ambiguous signature |
| Cache repeated states | Full state to result | Omitting a state variable from the key |

### Trade-offs, Alternatives, Edge Cases

| Hashing | Alternative | Prefer the alternative when |
|---|---|---|
| Expected `O(n)` and `O(n)` space | Sort + scan in `O(n log n)` | Ordered output helps, input may be mutated, or deterministic ordering matters |
| Set membership | Boolean/count array | Key universe is small, dense, and known |
| Map lookup | Binary search in sorted data | Data is already sorted and extra memory should be minimized |
| Hash map | Ordered tree map | Sorted iteration, predecessor/successor, or worst-case logarithmic guarantees are required |

Check empty input, duplicate keys, zero/negative values, Unicode/case normalization, large numeric values, and whether key iteration order is relevant. Never rely on arbitrary hash iteration order for correctness.

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Lookup / signatures | Contains Duplicate | Two Sum | Group Anagrams | Longest Consecutive Sequence | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Prefix state | Running Sum of 1D Array | Subarray Sum Equals K | Contiguous Array (earliest balance index) | Binary Subarrays With Sum | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Two Sum; Subarray Sum Equals K. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Contains Duplicate:** Learn the seen-set invariant and compare set space with sort-and-scan mutation.
- **Two Sum:** Learn one-pass complement lookup and check-before-insert timing.
- **Ransom Note / character construction:** Practice frequency decrements and early failure.

#### Core Interview

- **Group Anagrams:** Design a canonical signature and compare sorted-string keys with fixed-count-array signatures.
- **Longest Consecutive Sequence:** Use set membership but start only at sequence beginnings; learn how a nested-looking loop remains `O(n)` overall.
- **Subarray Sum Equals K:** Connect prefix equations to a count map, including the empty prefix.
- **Longest Substring Without Repeating Characters:** Use a map of latest indices inside a window; understand why `left` never moves backward.

#### Advanced

- **Minimum Window Substring:** Maintain required and satisfied frequency state without rescanning the window.
- **Isomorphic Strings / pattern bijection:** Enforce mapping consistency in both directions.
- **Max Points on a Line (only after core mastery):** Normalize slope keys carefully; learn why hash-key representation can be harder than the high-level algorithm.

### Common Mistakes and Interview Tips

- Using membership when the result needs counts, indices, or grouped values.
- Overwriting an earliest index required for a maximum-length answer.
- Reading a missing key without a default or membership check.
- Using array identity as a content signature, forgetting a matching `hashCode` override, or mutating a key's equality fields while it is stored.
- Assuming hash operations are unconditional worst-case `O(1)`.
- Forgetting that a map can have up to `O(n)` distinct keys even when each value is small.
- Decrementing counts but leaving logic that treats zero-count keys as present.
- Creating a key that omits relevant state, especially in memoization.

In an interview, say what each key and value represent: “After processing index `i`, this `HashMap` stores the count of every prefix sum through `i`.” That explanation is more valuable than saying only “I use a hash table.”

### Hashing Mastery Checklist

- [ ] I can choose between a set, a map, a fixed frequency array, and sorting.
- [ ] I state expected/amortized and worst-case qualifications accurately.
- [ ] I implement one-pass complement lookup without matching an element to itself.
- [ ] I design map values deliberately: count, first index, latest index, group, or cached result.
- [ ] I can derive prefix-state lookup for count and longest-length variants.
- [ ] I can create a stable canonical grouping key and choose an immutable encoded key or a type with consistent Java equality and hashing deliberately.
- [ ] I explain hashability, collisions, load factor, and resizing at a high level.
- [ ] I solve standard medium hashing problems and explain their time–space trade-offs.

---

## 6. Two Pointers

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** Two indices move through one or more sequences while an invariant eliminates work that brute force would repeat.
- **Why it exists:** Many pair, comparison, compaction, and partition problems have structure that lets pointer movement rule out many candidates at once.
- **Why it matters in interviews:** It commonly turns pair enumeration from `O(n²)` into `O(n)` after sorting or exploits already sorted input directly.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Arrays/strings, invariants, sorting trade-offs, and index boundaries.
- **Common use cases:** Pair sums, palindrome checks, merging, compaction, deduplication, partitioning, cycle detection, and linked-list distance.
- **Common problem patterns:** Opposite directions, same direction, parallel sequences, and slow/fast movement.
- **Recognition clues:** Sorted input, pairs, symmetric comparison, remove/move in place, merge two ordered sequences, or compare from both ends.
- **Required depth:** Deep. Explain why each pointer movement is safe, not merely that “two pointers works.”

> **Why this priority?** Two pointers is both frequent and foundational for sliding windows, linked lists, partitioning, and sorted-array reasoning. The invariant is highly transferable.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Opposite-direction pointers | 🔴 Tier 1 — Must Master | Sorted pair reasoning, palindrome, container boundaries |
| Same-direction read/write pointers | 🔴 Tier 1 — Must Master | Stable in-place compaction and deduplication |
| Parallel pointers over two inputs | 🔴 Tier 1 — Must Master | Merge/intersection in linear time |
| Movement invariants and termination | 🔴 Tier 1 — Must Master | Prove discarded choices cannot be answers |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Three-way partitioning | 🟠 Tier 2 — Very Important | Maintain `<`, `unknown`, and `>` regions |
| Fast/slow pointers | 🟠 Tier 2 — Very Important | Linked-list cycles/middle; developed in Section 8 |
| Sorting + two pointers | 🟠 Tier 2 — Very Important | Preserve indices if required; account for sort cost |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Multi-pointer geometric tricks | 🟡 Tier 3 — Nice to Know | Learn only after standard invariants are comfortable |
| Quickselect-style partition internals | 🟡 Tier 3 — Nice to Know | Useful for selection; not a first-line two-pointer topic |

<a id="61-opposite-direction-pointers"></a>
### 6.1 Opposite-Direction Pointers — 🔴 Tier 1 — Must Master

#### Intuition and safe movement

For sorted pair sum, begin with the smallest and largest remaining values:

```text
left = 0
right = n - 1
while left < right do
    total = a[left] + a[right]
    if total equals target then
        return the pair
    else if total is less than target then
        left = left + 1
    else
        right = right - 1
    end if
end while
```

If the sum is too small, pairing the current smallest with any value no larger than `a[right]` cannot reach the target, so moving `left` safely discards that value. A symmetric argument justifies moving `right` when the sum is too large. This monotonic elimination—not the mere presence of two variables—is the technique.

- **When to use:** Sorted pair relationship, symmetric comparison, shrinking a candidate interval.
- **Time:** `O(n)` after sorting; `O(n log n)` total if sorting is required.
- **Space:** `O(1)` pointer state, excluding sorting and output.
- **Alternatives:** Hashing finds an unsorted pair in expected `O(n)` time and `O(n)` space; sorting + pointers offers order and lower explicit memory but may mutate input or lose indices.

For palindrome-like comparisons, define normalization. Skipping punctuation/case on demand can avoid building another string, but increases boundary complexity.

#### Java sorted-pair implementation

Require ascending input; return zero-based indices or an empty array. Sorting beforehand changes original indices, so preserve them if the contract requires them.

```java
static int[] sortedPairIndices(int[] values, long target) {
    int left = 0;
    int right = values.length - 1;
    while (left < right) {
        long total = (long) values[left] + values[right];
        if (total == target) {
            return new int[] {left, right};
        }
        if (total < target) {
            left++;
        } else {
            right--;
        }
    }
    return new int[0];
}
```

<a id="62-same-direction-and-parallel-pointers"></a>
### 6.2 Same-Direction and Parallel Pointers — 🔴 Tier 1 — Must Master

#### Read/write compaction

`read` inspects every value; `write` marks the next output position. State whether the valid output is a prefix and whether relative order must remain stable.

For sorted deduplication, the first value is kept, and each new distinct value is written after the last kept value. Handle empty input before initializing `write` around index `1`.

#### Merge-style pointers

For sorted sequences `a` and `b`, compare their current values, consume whichever is smaller, then append the remaining suffix after one input is exhausted.

- **Time:** `O(n + m)` because each pointer only advances.
- **Space:** `O(n + m)` for a new merged result, or `O(1)` auxiliary if writing backward into preallocated capacity.
- **Recognition:** Two sorted inputs, intersection, union, merge, synchronized events, or compare sequences.
- **Mistake:** Forgetting leftover elements or advancing both pointers when only one item has been consumed.

#### Java read/write deduplication

Require ascending input; the returned length identifies the valid prefix. The array length itself cannot change. Time `O(n)`, auxiliary space `O(1)`.

```java
static int deduplicateSorted(int[] values) {
    int write = 0;
    for (int read = 0; read < values.length; read++) {
        if (write == 0 || values[read] != values[write - 1]) {
            values[write++] = values[read];
        }
    }
    return write;
}
```

<a id="63-partitioning"></a>
### 6.3 Partitioning — 🟠 Tier 2 — Very Important

Partitioning maintains regions with explicit meanings. In the Dutch-national-flag form:

```text
[0, low)       values less than pivot
[low, current) values equal to pivot
[current, high] unknown
(high, n)       values greater than pivot
```

Each action must shrink the unknown region. When swapping a high value from `current` with `high`, do **not** advance `current` until the incoming unknown value has been inspected.

- **Use:** Three categories, colors, pivot partition, segregating values.
- **Time:** `O(n)`.
- **Space:** `O(1)` auxiliary.
- **Trade-off:** In-place and fast, but often unstable—the relative order within groups may change. A stable output is simpler with extra storage.

### How to Recognize Two Pointers

| Clue | Pointer form | Invariant to articulate |
|---|---|---|
| Sorted array + target pair | Opposite ends | Outside the interval is proven impossible/processed |
| Palindrome/symmetry | Opposite ends | Processed outer pairs match |
| Remove/move values in place | Read/write | Valid prefix contains exactly kept items |
| Merge/intersection of sorted inputs | One pointer per input | Output contains all consumed items in correct order |
| Three categories | Low/current/high | Three classified regions plus shrinking unknown region |
| Cycle/middle of linked list | Slow/fast | Relative speeds encode structure, not array ordering |

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Opposite ends | Valid Palindrome | Two Sum II | Container With Most Water | 3Sum | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Read/write / merge | Move Zeroes | Remove Duplicates from Sorted Array | Merge Sorted Array | Sort Colors | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Two Sum II; Remove Duplicates from Sorted Array. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Valid Palindrome:** Practice opposite-end movement and character normalization boundaries.
- **Remove Duplicates from Sorted Array:** Practice the read/write invariant and returned valid-prefix length.
- **Merge Sorted Array:** Learn backward merging to avoid overwriting unread values.

#### Core Interview

- **Two Sum II (sorted input):** Justify which pointer moves from monotonic order.
- **3Sum:** Combine sorting, a fixed first value, two pointers, and duplicate skipping; learn output uniqueness.
- **Container With Most Water:** Learn why moving the shorter boundary is the only movement that can improve the limiting height.
- **Sort Colors:** Maintain three explicit regions and avoid skipping swapped-in unknown values.

#### Advanced

- **Trapping Rain Water (two-pointer form):** Maintain left/right maxima and process the side whose bound is determined; compare with prefix arrays and a monotonic stack.
- **Four Sum / k-sum reduction:** Generalize sorting plus duplicate-aware recursion/two pointers, while controlling complexity and overflow.

### Common Mistakes, Edge Cases, and Interview Tips

- Moving a pointer without proving what candidates are discarded.
- Using sorted reasoning on unsorted data.
- Sorting when original indices or order are required and failing to preserve them.
- Looping with `left <= right` when the task requires two distinct elements; or using `<` when a single midpoint remains valid.
- Skipping duplicates too early or accessing beyond the array while skipping.
- Advancing both merge pointers after consuming only one value.
- Calling all same-direction pointer problems sliding windows. A window represents a contiguous active range; read/write compaction does not necessarily do so.

### Two Pointers Mastery Checklist

- [ ] I can derive and verbalize a safe pointer movement from sorted order.
- [ ] I implement opposite-end, read/write, and two-input merge patterns from scratch.
- [ ] I account for sorting time, mutation, stability, and original-index requirements.
- [ ] I handle duplicate skipping without boundary errors.
- [ ] I can define all regions in a three-way partition.
- [ ] I recognize when hashing is preferable to sorting plus pointers and vice versa.
- [ ] I solve standard medium pair/partition problems and explain why total pointer movement is linear.

---

## 7. Sliding Window

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** A sliding window maintains information about a contiguous range as its boundaries move, updating state incrementally rather than recomputing each range.
- **Why it exists:** Many subarray and substring questions examine overlapping ranges. Reusing state can reduce `O(nk)` or `O(n²)` work to `O(n)`.
- **Why it matters in interviews:** Fixed and variable windows are among the most common array/string patterns, especially for medium problems.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Arrays/strings, two monotonic pointers, hashing/frequency tables, and loop invariants.
- **Common use cases:** Window sum/average, longest or shortest valid contiguous segment, distinct-character constraints, and permutation/anagram matching.
- **Common problem patterns:** Fixed-size update, expand-then-shrink, count-map validity, and monotonic-deque extrema.
- **Recognition clues:** “Contiguous subarray/substring,” “longest/shortest,” fixed length `k`, or a condition maintainable as items enter and leave.
- **Required depth:** Deep for fixed, variable, and frequency windows. Monotonic deque is useful but can follow later.

> **Why this priority?** Sliding windows recur frequently and test the exact skills interviewers value: recognizing exploitable structure, maintaining an invariant, and avoiding repeated work.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Fixed-size windows | 🔴 Tier 1 — Must Master | Add entering/remove leaving item and place answer update correctly |
| Variable-size expand/shrink | 🔴 Tier 1 — Must Master | Monotonic feasibility and valid-window invariants |
| Frequency-based string windows | 🔴 Tier 1 — Must Master | Counts, distinct/required/satisfied bookkeeping |
| Longest vs shortest answer timing | 🔴 Tier 1 — Must Master | Know whether to update before, during, or after shrinking |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Last-seen-index window | 🟠 Tier 2 — Very Important | Jump the left boundary without moving backward |
| Monotonic deque window extrema | 🟡 Tier 3 — Nice to Know | Remove expired and dominated indices; revisited in queues |
| Window + replacement budget | 🟠 Tier 2 — Very Important | Maintain a conservative maximum-frequency invariant |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Multiple nested windows / exact-count transforms | 🟡 Tier 3 — Nice to Know | Learn “exactly `k` = at most `k` − at most `k-1`” after core mastery |
| Non-monotonic window constraints | ⚪ Tier 4 — Low Priority / Specialized | Often need prefix sums, trees, or deques instead of a standard window |

<a id="71-fixed-size-window"></a>
### 7.1 Fixed-Size Window — 🔴 Tier 1 — Must Master

#### Intuition and implementation sketch

Compute the first range once, then remove the outgoing value and add the incoming value:

```text
state = contribution of first k items
answer = evaluate(state)
for right from k through n-1 do
    remove(a[right-k])
    add(a[right])
    answer = combine(answer, evaluate(state))
end for
```

- **When to use:** Every candidate has exactly length `k`.
- **Recognition:** “Maximum/average/count in every subarray of size `k`,” fixed-length substring, or anagram/permutation of known length.
- **Time:** Usually `O(n)` if updates are `O(1)`; a balanced structure may make it `O(n log k)`.
- **Space:** `O(1)`, `O(alphabet)`, or `O(k)` depending on state.
- **Edge cases:** `k = 0` if permitted, `k = 1`, `k = n`, `k > n`, and negative values when initializing a maximum.
- **Mistake:** Recomputing the whole window or removing `a[right-k+1]` instead of the actual outgoing index.

#### Java fixed-window sum

Require `1 <= k <= values.length`; invalid sizes throw an exception instead of inventing an empty maximum. Initialize from the first complete window so all-negative input works. Time `O(n)`, auxiliary space `O(1)`.

```java
static long maximumWindowSum(int[] values, int k) {
    if (k <= 0 || k > values.length) {
        throw new IllegalArgumentException("Window size must be in [1, n]");
    }
    long sum = 0L;
    for (int i = 0; i < k; i++) {
        sum += values[i];
    }
    long best = sum;
    for (int right = k; right < values.length; right++) {
        sum -= values[right - k];
        sum += values[right];
        best = Math.max(best, sum);
    }
    return best;
}
```

<a id="72-variable-size-window"></a>
### 7.2 Variable-Size Window — 🔴 Tier 1 — Must Master

#### The expand/shrink invariant

The right pointer expands the range. While the constraint is violated—or while it remains valid for a minimum-window problem—the left pointer shrinks it.

```text
left = 0
for right from 0 through n-1 do
    add(a[right])
    while window violates constraint do
        remove(a[left])
        left = left + 1
    end while
    update longest valid answer using [left, right]
end for
```

This shape works when validity changes **monotonically enough** as the window expands/shrinks. For example, with nonnegative numbers, removing values cannot increase a sum. With arbitrary negative numbers, a sum-based window may not have that property.

For a **shortest valid** window, update the answer while the window is valid, immediately before each shrink:

```text
add right item
while window is valid do
    answer = min(answer, right-left+1)
    remove left item
    left = left + 1
end while
```

Every item enters once and leaves once, so the total is `O(n)` even though a `while` loop is nested inside a `for` loop.

<a id="73-frequency-based-windows"></a>
### 7.3 Frequency-Based Windows — 🔴 Tier 1 — Must Master

The hard part is not moving pointers; it is designing a constant-time validity test.

Useful summaries include:

- `distinct`: number of keys with positive count in the window.
- `required`: number of target keys whose required count must be met.
- `satisfied`: number of target keys currently meeting their required count.
- `duplicates`: number of excess items or repeated keys.
- `max_frequency`: largest count of one value in a replacement-budget window.

Update a summary only when crossing a meaningful boundary. For minimum cover, `satisfied` increases when a count becomes **exactly** the required count and decreases when removal makes it fall below.

```text
need = frequency(target)
required = number of keys in need
satisfied = 0

when adding x do
    window[x] = window[x] + 1
    if x is a key in need and window[x] equals need[x] then
        satisfied = satisfied + 1
    end if

when removing x do
    if x is a key in need and window[x] equals need[x] then
        satisfied = satisfied - 1
    end if
    window[x] = window[x] - 1
```

Ordering matters during removal: detect the transition from satisfied to unsatisfied before decrementing (or write the equivalent condition after decrementing consistently).

#### Last-seen alternative — 🟠 Tier 2 — Very Important

For “longest substring without duplicates,” store each character's latest index and jump:

```text
left = max(left, last_seen[character] + 1)
```

The `max` prevents `left` from moving backward when the previous occurrence is already outside the current window.

#### Java longest-window implementation

This version counts distinct **UTF-16 code units**. It is suitable for ASCII/BMP-unit contracts; convert to code points first if the problem requires them. Zero-count keys are removed so `frequency.size()` means the number of distinct units in the current window. For `k <= 0`, the valid length is zero. Expected time `O(n)`; working space `O(min(n, k + 1, alphabet))`, including the transient entering key.

```java
static int longestAtMostKDistinct(String text, int k) {
    if (k <= 0) {
        return 0;
    }
    Map<Character, Integer> frequency = new HashMap<>();
    int left = 0;
    int best = 0;
    for (int right = 0; right < text.length(); right++) {
        char entering = text.charAt(right);
        frequency.put(entering, frequency.getOrDefault(entering, 0) + 1);
        while (frequency.size() > k) {
            char leaving = text.charAt(left++);
            int remaining = frequency.get(leaving) - 1;
            if (remaining == 0) {
                frequency.remove(leaving);
            } else {
                frequency.put(leaving, remaining);
            }
        }
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

### When Sliding Window Does Not Apply

- The range is not contiguous.
- Adding/removing an item cannot update state efficiently.
- Validity is non-monotonic under pointer movement.
- Arbitrary negative numbers break a simple sum threshold invariant.
- The problem asks about all subsequences rather than subarrays/substrings.
- The relevant relation is between prefix states; hashing prefix sums may be a better model.

Alternatives include prefix sums + hashing, binary search on answer, monotonic stacks/deques, DP, or sorting + two pointers.

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Fixed / frequency | Maximum Average Subarray I | Permutation in String | Find All Anagrams in a String | Minimum Window Substring | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Variable bounds | Longest Substring Without Repeating Characters | Minimum Size Subarray Sum | Longest Substring with At Most K Distinct Characters | Longest Repeating Character Replacement | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Permutation in String; Minimum Size Subarray Sum. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Maximum Average Subarray I:** Learn a fixed-size sum update and window boundary arithmetic.
- **Find All Anagrams in a String:** Maintain fixed-window frequencies and compare meaningful state rather than rebuilding counts.
- **Longest Substring Without Repeating Characters:** Learn shrink-until-valid and the last-seen jump alternative.

#### Core Interview

- **Minimum Size Subarray Sum:** Learn shortest-valid timing and understand why the common solution assumes positive/nonnegative values.
- **Permutation in String:** Maintain a fixed-length frequency match efficiently.
- **Longest Repeating Character Replacement:** Maintain window length minus dominant frequency as a replacement budget; explain why a stale high `max_frequency` can remain safe for the maximum-length objective.
- **Minimum Window Substring:** Track required counts and satisfied keys; shrink to minimality without rescanning.

#### Advanced

- **Subarrays with K Different Integers:** Transform exactly `k` into at-most counts and subtract.
- **Sliding Window Maximum:** Maintain a deque of useful indices; compare `O(n)` deque with `O(n log k)` heap/tree approaches.
- **Shortest Subarray with Sum at Least K with negatives:** Learn why an ordinary window fails and how prefix sums plus a monotonic deque restores structure.

### Common Mistakes, Edge Cases, and Trade-offs

- Applying the pattern merely because the problem says “subarray.”
- Updating a longest answer before restoring validity, or updating a shortest answer only after shrinking past validity.
- Moving `left` backward in a last-seen solution.
- Confusing a distinct-key count with total required frequency.
- Forgetting to remove or decrement outgoing values, including zero-count cleanup when logic depends on map size.
- Claiming `O(n²)` because of nested loops despite monotonic boundaries.
- Using `if` where repeated shrinking requires `while`.
- Ignoring empty target, empty input, `k > n`, all-identical input, or a valid window at the final index.

State the invariant aloud: “After the `while`, `[left, right]` is valid, and `left` is the smallest boundary reached by removing all current violations.” For a minimum window, specify the different invariant precisely.

### Sliding Window Mastery Checklist

- [ ] I distinguish fixed-size and variable-size windows immediately.
- [ ] I define the window boundaries as inclusive or half-open and keep them consistent.
- [ ] I can prove total pointer movement is `O(n)`.
- [ ] I know where to update longest and shortest answers.
- [ ] I maintain count-map validity through boundary transitions rather than rescanning.
- [ ] I can explain when negative values invalidate an ordinary sum window.
- [ ] I compare windows with prefix sums + hashing and select based on monotonicity.
- [ ] I solve standard medium frequency-window problems and can re-derive the state after a delay.

---

## 8. Linked Lists

**Priority:** 🟠 Tier 2 — Very Important

### Topic Overview

- **What it is:** A linked list stores elements in nodes connected by object references rather than at contiguous indices. Singly linked nodes point forward; doubly linked nodes point both forward and backward.
- **Why it exists:** Links permit local insertion/deletion without shifting later elements and let structures be assembled from independently allocated nodes.
- **Why it matters in interviews:** Linked-list tasks test reference safety, mutation, invariants, and reasoning without random access. Reversal, merge, cycle, and dummy-node patterns are frequent classics.
- **Interview priority:** 🟠 Tier 2 — Very Important.
- **Prerequisites:** Java object references, `null` handling, loops/recursion, and object identity vs value equality.
- **Common use cases:** Queues, adjacency chains, LRU-cache internals, ordered merging, and sequences with frequent node-level updates.
- **Common problem patterns:** Reverse links, splice nodes, merge lists, dummy head, fast/slow pointers, and cycle detection.
- **Recognition clues:** Input is a `ListNode`, random access is absent, nodes must be rearranged in place, or the task asks for cycle/middle/intersection.
- **Required depth:** Confidently implement singly linked reversal, merge, middle, and cycle detection; understand doubly linked splicing for designs such as LRU cache.

> **Why this priority?** Linked lists appear less often than arrays and hashing, but their core patterns are canonical interview material and expose reference-rewiring errors clearly. Advanced list tricks have much lower transfer value.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Singly linked traversal and mutation | 🟠 Tier 2 — Very Important | Preserve references before rewiring |
| Iterative reversal | 🟠 Tier 2 — Very Important | Implement from scratch and state the reversed-prefix invariant |
| Dummy/sentinel node | 🟠 Tier 2 — Very Important | Uniform head insertion/deletion and merge logic |
| Fast/slow pointers | 🟠 Tier 2 — Very Important | Middle and cycle detection |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Recursive reversal | 🟡 Tier 3 — Nice to Know | Understand call-stack trade-off and link direction |
| Doubly linked lists | 🟡 Tier 3 — Nice to Know | Constant-time removal given a node; LRU design |
| Sublist and k-group rewiring | 🟡 Tier 3 — Nice to Know | Practice only after whole-list reversal is automatic |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Exotic multilevel/XOR lists | ⚪ Tier 4 — Low Priority / Specialized | Not useful for standard interviews |
| Custom concurrent linked structures | ⚪ Tier 4 — Low Priority / Specialized | Role-specific concurrency knowledge |

<a id="81-singly-linked-lists-and-reference-safety"></a>
### 8.1 Singly Linked Lists and Reference Safety — 🟠 Tier 2 — Very Important

A node conceptually contains `value` and `next`. Access by position is `O(n)` because links must be followed; insertion or deletion is `O(1)` **only when the relevant node/predecessor is already known**.

| Operation | Singly linked list | Dynamic array | Important qualification |
|---|---:|---:|---|
| Access index `i` | `O(i)` | `O(1)` | No random access in a list |
| Search by value | `O(n)` | `O(n)` | Unless another index exists |
| Insert/delete after known node | `O(1)` | Usually `O(n)` shift | Finding the node may be `O(n)` |
| Append | `O(1)` with tail, else `O(n)` | Amortized `O(1)` | Keep tail consistent |

Before changing `current.next`, save any node that still needs to be reached. Draw a three-node picture or label `previous`, `current`, and `next_node`; reference rewiring becomes far less error-prone when the preserved path is explicit.

#### Dummy/sentinel nodes

A dummy node precedes the real head. It makes deletion/insertion at the head look like the same operation as elsewhere:

```text
dummy.next points to head
previous points to dummy
...
return dummy.next as the new head
```

- **Use:** Remove conditionally, merge lists, build a result, reverse a subrange.
- **Cost:** `O(1)` extra space.
- **Trade-off:** One extra node greatly simplifies branching; remember not to return the dummy itself.

<a id="82-reversal"></a>
### 8.2 Reversal — 🟠 Tier 2 — Very Important

#### Core intuition

At every step, the processed prefix is reversed and headed by `previous`; `current` begins the unreversed suffix.

```java
static class ListNode {
    int value;
    ListNode next;

    ListNode(int value) {
        this.value = value;
    }
}

// Require an acyclic list. The caller assigns head = reverseList(head).
static ListNode reverseList(ListNode head) {
    ListNode previous = null;
    ListNode current = head;
    while (current != null) {
        ListNode nextNode = current.next; // preserve the suffix
        current.next = previous;
        previous = current;
        current = nextNode;
    }
    return previous;
}
```

- **Time:** `O(n)`; each node is processed once.
- **Space:** `O(1)` iterative; `O(n)` call stack for a recursive version on a length-`n` list.
- **Recognition:** “Reverse,” reorder halves, palindrome list, or a larger problem needs temporary direction changes.
- **Common mistake:** Reassigning `current.next` before saving the original next reference, losing the remaining list.
- **Edge cases:** Empty list, one node, two nodes, and whether the caller expects the input structure to be restored after a temporary reversal.

For recursive reversal, define the return value: the new head of the reversed suffix. After recursion returns, set `head.next.next = head`, then set `head.next = null` to prevent a cycle. Iteration is usually safer when list length is large or uncontrolled.

<a id="83-fast-and-slow-pointers"></a>
### 8.3 Fast and Slow Pointers — 🟠 Tier 2 — Very Important

#### Finding a middle

Move `slow` one node and `fast` two nodes. When `fast` reaches the end, `slow` is near the midpoint.

```text
while fast exists and fast.next exists do
    slow = slow.next
    fast = fast.next.next
end while
```

For even length, this common initialization returns the second middle. If the task needs the first middle or the node before the second half, adjust initialization/condition intentionally and test lengths `0, 1, 2, 3, 4`.

#### Floyd cycle detection

If a cycle exists, fast eventually laps slow inside it; if `fast` reaches `null`, no cycle exists.

- **Detection:** `O(n)` time and `O(1)` space.
- **Cycle entry:** After a meeting, place one pointer at the head, move both one step at a time, and their next meeting is the cycle entry.
- **Alternative:** Store node identities in a set for expected `O(n)` time and `O(n)` space; simpler but uses memory.

Compare **node identity**, not node values. Repeated values do not imply a cycle or intersection.

The technique also supports “kth from end”: place pointers `k` nodes apart, then move both until the lead reaches the end. Clarify whether `k` is one-based and what should happen when `k` exceeds the length.

#### Java fast/slow implementations

These use `ListNode` from [reversal](#82-reversal). `middleNode` requires an acyclic list and returns the second middle for even length, or null for empty input. `hasCycle` uses identity (`==`) deliberately. Both run in `O(n)` time and `O(1)` auxiliary space.

```java
static ListNode middleNode(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}

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

<a id="84-doubly-linked-lists"></a>
### 8.4 Doubly Linked Lists — 🟡 Tier 3 — Nice to Know

A doubly linked node has `prev` and `next`. Given a node, it can be removed in `O(1)` by reconnecting both neighbors. With dummy head and tail sentinels, every real node has two neighbors, eliminating endpoint branches.

This is important for an **LRU cache** design:

- `HashMap<Key, Node>`: key → node reference for expected `O(1)` lookup.
- Doubly linked list: recency order and `O(1)` detach/append.

Every splice must update four logical connections consistently. When moving a node, detach it fully before attaching it elsewhere. Keep map membership, list membership, capacity, and tail/head meaning synchronized.

- **Time:** Insert/remove known node `O(1)`; search without a map remains `O(n)`.
- **Space:** Two links per node plus any index.
- **Trade-off:** Easier bidirectional removal than singly lists, but more memory and more invariants to maintain.

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Rewire / identity | Middle of the Linked List | Reverse Linked List | Linked List Cycle | Reorder List | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Reverse Linked List. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Reverse Linked List:** Learn the preserve–rewire–advance sequence and reversed-prefix invariant.
- **Merge Two Sorted Lists:** Use a dummy head and a moving tail; remember the remaining suffix.
- **Middle of the Linked List:** Trace fast/slow behavior for both even and odd lengths.

#### Core Interview

- **Linked List Cycle / Cycle II:** Separate cycle detection from locating the entry and compare with a visited set.
- **Remove Nth Node From End:** Use a dummy plus a deliberate pointer gap; clarify indexing.
- **Reorder List:** Combine middle, reversal, and alternating merge while preventing accidental cycles.
- **Palindrome Linked List:** Find middle, reverse half, compare, and discuss whether to restore the list.
- **Intersection of Two Linked Lists:** Align path lengths through pointer switching and compare identities.

#### Advanced

- **Reverse Nodes in k-Group:** Manage group boundaries and reconnect reversed blocks; solve only after basic rewiring is fluent.
- **Copy List with Random Pointer:** Compare a node-to-copy map with the interleaving technique and discuss mutation.
- **LRU Cache:** Combine a hash map with a sentinel-based doubly linked list; focus on synchronized invariants rather than memorized methods.

### Common Mistakes, Edge Cases, and Interview Tips

- Losing the rest of the list before saving `next`.
- Returning the old head after reversal.
- Dereferencing `fast.next` before confirming `fast != null`.
- Confusing equal node values with identical node objects.
- Creating a cycle by failing to terminate a reversed or merged tail.
- Forgetting that finding a predecessor can make deletion `O(n)`.
- Mishandling head deletion instead of using a dummy.
- Assuming the second half begins at the same place for odd and even lengths without tracing examples.

In an interview, draw nodes and arrows, state which node each reference denotes, and trace one rewiring operation. “`previous` heads the fully reversed prefix; `current` heads the untouched suffix” is a strong correctness explanation.

### Linked Lists Mastery Checklist

- [ ] I know linked-list operation costs and their “known node” qualifications.
- [ ] I reverse a singly linked list iteratively without notes and without losing nodes.
- [ ] I use a dummy node to simplify head-sensitive operations.
- [ ] I find the required middle and trace even/odd cases deliberately.
- [ ] I detect a cycle and explain why node identity matters.
- [ ] I can merge sorted lists and safely reconnect sublists.
- [ ] I understand the hash map + doubly linked list design of an LRU cache.
- [ ] I analyze recursive stack space and discuss mutation/restoration trade-offs.

---

<a id="9-stacks-queues-deques"></a>
## 9. Stacks, Queues & Deques

**Priority:** 🟠 Tier 2 — Very Important

### Topic Overview

- **What it is:** A stack removes the most recently added item (LIFO). A queue removes the earliest added item (FIFO). A deque supports insertion/removal at both ends.
- **Why it exists:** Restricted access order encodes pending work, nested contexts, chronological frontiers, and a useful candidate frontier without arbitrary searching.
- **Why it matters in interviews:** Stacks drive delimiter parsing, DFS, expression evaluation, and next-greater/smaller patterns. Queues drive BFS; deques support efficient window extrema.
- **Interview priority:** 🟠 Tier 2 — Very Important overall. Basic LIFO reasoning, parentheses, and BFS queue usage are Tier 1 skills; monotonic variants occur somewhat less often.
- **Prerequisites:** Arrays/linked lists, complexity, invariants, and basic tree/graph concepts for traversal usage.
- **Common use cases:** Undo/nesting, parser state, iterative traversal, BFS levels, task scheduling, next greater/smaller, and window maximum/minimum.
- **Common problem patterns:** Match opener/closer, evaluate postfix/infix, store unresolved indices, process by layers, or discard dominated candidates.
- **Recognition clues:** Nested structure, “most recent unmatched,” next greater/smaller, FIFO order, minimum steps in an unweighted state space, or extrema in every window.
- **Required depth:** Implement ordinary stack/queue operations confidently, understand queue-based BFS, and derive monotonic stack logic. Learn monotonic deque after core patterns.

> **Why this priority?** Stack/queue access order is fundamental and appears inside trees and graphs, while specialized monotonic structures are frequent enough for strong preparation but not as universal as arrays, hashing, or binary search.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Stack fundamentals | 🔴 Tier 1 — Must Master | LIFO state, push/pop/peek, iterative traversal |
| Parentheses and delimiter matching | 🔴 Tier 1 — Must Master | Most-recent unmatched opener invariant |
| Queue fundamentals and BFS usage | 🔴 Tier 1 — Must Master | FIFO frontier, mark-on-enqueue, level boundaries |
| Correct Java collection | 🔴 Tier 1 — Must Master | Use `ArrayDeque` through `Deque`/`Queue`; avoid linear `ArrayList.remove(0)` |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Monotonic stack | 🟠 Tier 2 — Very Important | Unresolved indices, pop condition, nearest boundary |
| Expression evaluation | 🟠 Tier 2 — Very Important | Operand order and precedence/associativity |
| Deque fundamentals | 🟠 Tier 2 — Very Important | Both-end operations and 0–1 frontier intuition |
| Monotonic queue/deque | 🟡 Tier 3 — Nice to Know | Window extrema and domination invariant |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Full parser construction | ⚪ Tier 4 — Low Priority / Specialized | Basic calculator patterns are enough for most roles |
| Lock-free or concurrent queues | ⚪ Tier 4 — Low Priority / Specialized | Systems-specific, not ordinary DSA interview scope |

<a id="91-stack-fundamentals-and-parentheses"></a>
### 9.1 Stack Fundamentals and Parentheses — 🔴 Tier 1 — Must Master

`Deque<Integer> stack = new ArrayDeque<>();` supports `push`, `pop`, and `peek`; end insertion is amortized `O(1)`, and end read/removal is `O(1)`. It is ideal when the newest unresolved item must be handled first.

#### Delimiter invariant

When scanning a bracket string, the stack contains exactly the unmatched opening brackets in encounter order; the top is the only opener the next closing bracket can legally match.

```text
for each token in input do
    if token is an opener then
        push token
    else
        if stack is empty or its top does not match token then
            return invalid
        end if
        pop
    end if
end for
return valid exactly when the stack is empty
```

- **Time:** `O(n)`.
- **Space:** `O(n)` worst case for all openers.
- **Edge cases:** Empty input, a closing token first, mismatched types, leftover openers, and non-bracket tokens if present.
- **Alternative:** A counter works for one bracket type only when nesting types do not need distinction; it cannot validate arbitrary mixed delimiters.

Stacks also replace recursion explicitly. An iterative DFS stack gives control over order and avoids exhausting Java's finite call stack, but may require storing extra per-frame state for postorder processing.

#### Java delimiter validation

The contract accepts only `()[]{}`; any other character returns false, and the empty string is valid.

```java
static boolean validParentheses(String text) {
    Deque<Character> stack = new ArrayDeque<>();
    for (int i = 0; i < text.length(); i++) {
        char token = text.charAt(i);
        if (token == '(' || token == '[' || token == '{') {
            stack.push(token);
        } else {
            char expected;
            if (token == ')') {
                expected = '(';
            } else if (token == ']') {
                expected = '[';
            } else if (token == '}') {
                expected = '{';
            } else {
                return false;
            }
            if (stack.isEmpty() || stack.pop() != expected) {
                return false;
            }
        }
    }
    return stack.isEmpty();
}
```

<a id="92-expression-problems"></a>
### 9.2 Expression Problems — 🟠 Tier 2 — Very Important

Common forms:

- **Postfix / Reverse Polish Notation:** Push operands; on an operator, pop the right operand first, then the left, evaluate `left op right`, and push the result.
- **Infix calculators:** Use an operator stack plus an operand stack, or accumulate sign/term state for a restricted grammar.
- **Decode/nested expressions:** Push prior context when entering a nested group and restore it on closing.

Important details:

- Subtraction and division are not commutative; pop order matters.
- Define integer division behavior, especially for negatives.
- Operator precedence determines when pending operators are applied.
- Exponentiation is commonly right-associative; most binary arithmetic operators are left-associative.
- Unary minus and whitespace require deliberate tokenization rather than accidental character assumptions.

**Complexity:** A well-designed single pass is typically `O(n)` time and `O(n)` stack space. Repeated front insertion, middle erasure, or rebuilding temporary strings can raise the cost in Java; count decoded output length as well as encoded input length.

<a id="93-monotonic-stack"></a>
### 9.3 Monotonic Stack — 🟠 Tier 2 — Very Important

#### Core intuition

A monotonic stack stores candidates in increasing or decreasing value order. When a new value makes candidates obsolete or resolves their question, pop them. Store **indices** when distance, width, duplicate handling, or expiration matters.

Next-greater sketch:

```text
stack = indices whose next greater value is unresolved
for i from 0 through n-1 do
    while stack is not empty and a[i] > a[stack.top] do
        j = pop the stack
        answer[j] = i or a[i]
    end while
    push i onto the stack
end for
```

Each index is pushed once and popped at most once:

- **Time:** `O(n)` amortized total.
- **Space:** `O(n)`.

#### Recognition clues

- Next or previous greater/smaller element.
- First boundary on each side where a monotonic property breaks.
- Span/width until a warmer/larger/smaller value.
- Largest rectangle, visibility, stock span, or removing digits to maintain order.

#### Design questions

1. Does the stack increase or decrease from bottom to top?
2. Does equality pop (`>=`) or remain (`>`)? Duplicate semantics determine this.
3. What question does a pop resolve?
4. Are unresolved items answered by a default after the scan?
5. Do I need values or indices?

For largest-rectangle problems, a sentinel height can force all remaining bars to pop. State whether boundaries are inclusive and why width is `right_boundary - left_boundary - 1`.

#### Java next-greater indices

Equal values do not resolve a strictly-greater query. A result of `-1` means no greater value occurs to the right. Empty input returns an empty result. The primitive stack avoids boxing; its size never exceeds `n`. Time `O(n)`, auxiliary space `O(n)`, plus `O(n)` output.

```java
static int[] nextGreaterIndices(int[] values) {
    int[] answer = new int[values.length];
    Arrays.fill(answer, -1);
    int[] stack = new int[values.length];
    int size = 0;
    for (int i = 0; i < values.length; i++) {
        while (size > 0 && values[i] > values[stack[size - 1]]) {
            answer[stack[--size]] = i;
        }
        stack[size++] = i;
    }
    return answer;
}
```

<a id="94-queue-fundamentals-and-bfs-usage"></a>
### 9.4 Queue Fundamentals and BFS Usage — 🔴 Tier 1 — Must Master

A queue processes items in discovery order. Use `Queue<T> queue = new ArrayDeque<>();`: `offer` at the tail is amortized `O(1)` and `poll` at the head is `O(1)`. Repeated `ArrayList.remove(0)` shifts remaining elements and costs `O(n)` per removal.

#### BFS queue invariant

The queue contains discovered but not yet processed states, in nondecreasing distance from the source. Mark a state visited **when enqueuing**, not when dequeuing, so it is not inserted repeatedly by multiple parents.

- **Unweighted shortest path:** The first time a node is discovered, BFS has found a shortest number of edges from the source.
- **Level processing:** Capture the current queue length before processing that layer; nodes enqueued during the layer belong to the next one.
- **Time:** `O(V + E)` with adjacency lists, or `O(rows · cols)` for a grid with constant-degree neighbors.
- **Space:** `O(V)` worst-case for visited plus frontier.

DFS may find a path but not necessarily the shortest unweighted path. Weighted edges require a different algorithm unless all weights fit a special case such as 0–1 BFS.

#### Deque — 🟠 Tier 2 — Very Important

`ArrayDeque` offers amortized `O(1)` insertion and `O(1)` removal at both ends. Use it for ordinary queues, palindrome-style processing when mutation is acceptable, 0–1 BFS awareness, and monotonic candidate queues.

<a id="95-monotonic-deque"></a>
### 9.5 Monotonic Deque — 🟡 Tier 3 — Nice to Know

For the maximum in each length-`k` window, keep indices in decreasing value order:

1. Remove indices from the front if they have expired outside the window.
2. Remove indices from the back while their values are no better than the new value; they are dominated because the new value is at least as large and expires later.
3. Append the new index.
4. The front is the current maximum once the first full window forms.

- **Time:** `O(n)` total because each index enters and leaves at most once.
- **Space:** `O(k)`.
- **Alternative:** A heap is often simpler, typically `O(n log n)` with lazy stale entries or `O(n log k)` with deletions managed appropriately.
- **Mistakes:** Storing values when expiration needs indices, removing dominated elements from the wrong end, emitting before the window is full, and mixing expiration with domination rules.

#### Java window maxima

Require `1 <= k <= n`. Equal older candidates can be removed because the newer equal value expires later. Expiration happens before appending, so the deque has at most `k` indices. Output takes `O(n-k+1)` space in addition to the `O(k)` deque.

```java
static int[] slidingWindowMaximum(int[] values, int k) {
    if (k <= 0 || k > values.length) {
        throw new IllegalArgumentException("Window size must be in [1, n]");
    }
    int[] answer = new int[values.length - k + 1];
    Deque<Integer> candidates = new ArrayDeque<>();
    for (int right = 0; right < values.length; right++) {
        while (!candidates.isEmpty() && candidates.peekFirst() <= right - k) {
            candidates.pollFirst();
        }
        while (!candidates.isEmpty() && values[candidates.peekLast()] <= values[right]) {
            candidates.pollLast();
        }
        candidates.addLast(right);
        if (right >= k - 1) {
            answer[right - k + 1] = values[candidates.peekFirst()];
        }
    }
    return answer;
}
```

### Stack vs Queue vs Deque

| Need | Choose | Why |
|---|---|---|
| Most recent unresolved context | Stack | LIFO mirrors nesting/undo |
| Earliest discovered work | Queue | FIFO preserves BFS distance order |
| Both ends | Deque | Flexible frontier/window maintenance |
| Next greater/smaller | Monotonic stack | Resolves prior candidates when a boundary arrives |
| Max/min in every window | Monotonic deque | Keeps only unexpired, undominated candidates |
| Highest numeric priority | Heap, not queue | Extraction follows priority rather than arrival time |

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| LIFO / monotonic | Valid Parentheses | Daily Temperatures | Min Stack | Largest Rectangle in Histogram (later) | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| FIFO / levels | Number of Recent Calls | Binary Tree Level Order Traversal | Implement Queue Using Stacks | Rotting Oranges (after graphs) | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Deque (Tier 3; select later) | Trace expiry on a length-3 window | Sliding Window Maximum | Sliding Window Minimum archetype | Shortest Subarray with Sum at Least K (optional hard) | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Daily Temperatures; Binary Tree Level Order Traversal; Sliding Window Maximum. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Valid Parentheses:** Learn the unmatched-opener invariant and final empty-stack check.
- **Implement Queue Using Stacks:** Practice amortized transfer reasoning rather than moving every item on every operation.
- **Number of Recent Calls / simple bounded queue:** Practice expiring old items from the front.

#### Core Interview

- **Min Stack:** Store synchronized minima or value/min pairs and preserve duplicate minima.
- **Evaluate Reverse Polish Notation:** Practice operand order and integer division rules.
- **Daily Temperatures:** Learn a decreasing stack of unresolved indices and amortized linear proof.
- **Rotting Oranges:** Apply multi-source BFS, enqueue all sources at distance zero, and process layers.
- **Binary Tree Level Order Traversal:** Use a queue-length snapshot to separate levels.

#### Advanced

- **Largest Rectangle in Histogram:** Derive nearest-smaller boundaries and sentinel flushing; compare one-pass and precomputed-boundary forms.
- **Basic Calculator:** Manage precedence, nested contexts, unary signs, and tokenization carefully.
- **Sliding Window Maximum:** Maintain unexpired, undominated indices in a deque.

### Common Mistakes, Edge Cases, and Interview Tips

- Popping or peeking without checking emptiness.
- Failing to validate that a delimiter stack is empty at the end.
- Reversing operands for subtraction/division.
- Using `ArrayList.remove(0)` as a queue and paying `O(n)` per removal.
- Marking BFS nodes only when dequeued and inserting duplicates.
- Mixing level count with a queue size that grows during the level.
- Choosing the wrong monotonic direction or wrong strict/non-strict comparison for duplicates.
- Claiming a monotonic structure is linear without explaining push/pop accounting.

Before coding a monotonic stack/deque, narrate one pop: “This old index can never be the answer after the new index because…” If that statement is unclear, the invariant is not ready.

### Stacks, Queues, and Deques Mastery Checklist

- [ ] I choose LIFO, FIFO, double-ended, or priority order deliberately.
- [ ] I validate mixed parentheses and explain why the stack is necessary.
- [ ] I evaluate postfix expressions with correct operand order.
- [ ] I use `ArrayDeque` for amortized `O(1)` FIFO operations instead of removing index zero from an `ArrayList`.
- [ ] I perform BFS with mark-on-enqueue and correct level boundaries.
- [ ] I derive a monotonic stack's direction, pop condition, stored information, and default result.
- [ ] I prove monotonic stack/deque total work through per-element accounting.
- [ ] I understand the deque invariant for window extrema without treating it as a memorized trick.

---

## 10. Binary Search

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** Binary search repeatedly discards half of a monotonic search space while preserving an invariant that the answer, if it exists, remains inside the candidate range.
- **Why it exists:** Ordered or monotonic structure lets us avoid examining every candidate.
- **Why it matters in interviews:** Exact lookup is only the beginning. Interviews frequently ask for first/last positions, insertion boundaries, rotated arrays, or the smallest feasible answer.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Indexing, sorted order, predicates, integer division, and invariant-based reasoning.
- **Common use cases:** Lookup in sorted data, lower/upper bounds, boundary finding, implicit sorted domains, and optimization through feasibility checks.
- **Common problem patterns:** Exact match, first true/last false, first/last occurrence, peak/rotation, and binary search on answer.
- **Recognition clues:** Sorted input; a yes/no condition changes only once; “minimum capacity,” “maximum possible,” “first time,” or a huge numeric answer domain.
- **Required depth:** Implement a consistent boundary convention without assistance, derive variants rather than patching them, and prove the search-on-answer predicate is monotonic.

> **Why this priority?** Binary search is common, efficient, and deceptively easy to get almost right. Boundary discipline is a strong signal of coding precision and generalizes to many optimization problems.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Exact search in sorted data | 🔴 Tier 1 — Must Master | Loop invariant, midpoint, termination |
| Lower bound / first valid position | 🔴 Tier 1 — Must Master | First-true framework and insertion position |
| Upper bound / last occurrence derivation | 🔴 Tier 1 — Must Master | Boundary composition, duplicates |
| Boundary-condition handling | 🔴 Tier 1 — Must Master | Empty input, one item, absent target, endpoints |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Binary search on answer | 🟠 Tier 2 — Very Important | Monotonic feasibility and tight bounds |
| Rotated sorted arrays | 🟠 Tier 2 — Very Important | Identify sorted half; account for duplicates |
| Peak/unimodal search | 🟠 Tier 2 — Very Important | Compare local slope without out-of-bounds access |
| Search in conceptual matrices | 🟠 Tier 2 — Very Important | Map a flat index or exploit row/column ordering |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Floating-point answer search | 🟡 Tier 3 — Nice to Know | Fixed iterations/tolerance and precision caveats |
| Parallel binary search and parametric-search theory | ⚪ Tier 4 — Low Priority / Specialized | Competitive-programming or highly algorithmic roles |

<a id="101-exact-binary-search"></a>
### 10.1 Exact Binary Search — 🔴 Tier 1 — Must Master

#### Core intuition

Maintain a range known to contain every still-possible target position. Inspect its midpoint; sorted order proves which part can be discarded.

Two correct conventions are common. Pick one and keep all its conditions consistent.

**Closed interval `[left, right]`:**

```text
left = 0
right = n - 1
while left <= right do
    mid = left + floor((right - left) / 2)
    if a[mid] equals target then
        return mid
    else if a[mid] is less than target then
        left = mid + 1
    else
        right = mid - 1
    end if
end while
return NOT_FOUND
```

**Half-open interval `[left, right)`:** Often natural for lower-bound searches, with `right = n` and loop `left < right`.

- **Time:** `O(log n)` comparisons.
- **Space:** `O(1)` iterative; `O(log n)` recursive stack for a recursive implementation.
- **When to use:** Random-access sequence sorted under the same comparison relation used by the search.
- **Alternative:** A hash map offers expected `O(1)` repeated exact lookup but needs extra space and loses order operations; a linear scan works on unsorted input in `O(n)`.

In Java, use `left + (right - left) / 2`; with nonnegative indices, integer division supplies the needed floor while avoiding the overflow risk of `(left + right) / 2`. More importantly, each update must remove `mid` or otherwise strictly shrink the range; `left = mid` in the wrong convention can loop forever.

#### Java exact search

Require ascending input. Return any matching zero-based index, or `-1` when absent; empty input is naturally handled.

```java
static int binarySearch(int[] values, int target) {
    int left = 0;
    int right = values.length - 1;
    while (left <= right) {
        int middle = left + (right - left) / 2;
        if (values[middle] == target) {
            return middle;
        }
        if (values[middle] < target) {
            left = middle + 1;
        } else {
            right = middle - 1;
        }
    }
    return -1;
}
```

<a id="102-lower-bound-and-upper-bound"></a>
### 10.2 Lower Bound and Upper Bound — 🔴 Tier 1 — Must Master

#### First-true model

Many variants become one question: find the first index where a monotonic predicate is true.

For lower bound, `predicate(i)` is `a[i] >= target`:

```text
left = 0
right = n                         (the candidate boundary may be n)
while left < right do
    mid = left + floor((right - left) / 2)
    if a[mid] >= target then
        right = mid               (mid may be the first valid index)
    else
        left = mid + 1
    end if
end while
return left
```

The result is in `[0, n]`; `n` means no element satisfies the predicate.

- **Lower bound:** First index with value `>= target`; also the insertion point before equal values.
- **Upper bound:** First index with value `> target`; insertion point after equal values.
- **First occurrence:** Lower bound, then verify the result is in range and equals target.
- **Last occurrence:** Upper bound minus one, then verify.
- **Count in sorted array:** `upperBound(values, target) - lowerBound(values, target)`.

The invariant for the half-open first-true search is:

- All indices before `left` are known false.
- All indices at or after `right` are known true, with conceptual sentinel boundaries permitted.
- The first true position remains in `[left, right]`.

This boundary search is safer than finding one occurrence and scanning outward, which can degrade to `O(n)` with many duplicates.

#### Java lower and upper bounds

Require ascending input. Both return a boundary in `[0, n]`, including zero on empty input. Never index the returned `n`. Derive upper bound by changing the predicate, without computing `target + 1`, which could overflow.

```java
static int lowerBound(int[] values, int target) {
    int left = 0;
    int right = values.length;
    while (left < right) {
        int middle = left + (right - left) / 2;
        if (values[middle] >= target) {
            right = middle;
        } else {
            left = middle + 1;
        }
    }
    return left;
}

static int upperBound(int[] values, int target) {
    int left = 0;
    int right = values.length;
    while (left < right) {
        int middle = left + (right - left) / 2;
        if (values[middle] > target) {
            right = middle;
        } else {
            left = middle + 1;
        }
    }
    return left;
}
```

<a id="103-binary-search-on-answer"></a>
### 10.3 Binary Search on Answer — 🟠 Tier 2 — Very Important

#### Recognition and systematic process

The answer itself may be numeric and ordered even when no sorted array is given. Define a feasibility predicate such as:

> `feasible(x)`: Can the task be completed with capacity/time/distance at most `x`?

If `feasible(x)` is false below some boundary and true from that boundary onward, search for the first true value.

1. **Define the candidate answer** precisely.
2. **Choose guaranteed bounds** that bracket the answer.
3. **Write a feasibility check** without performing another optimization inside it.
4. **Prove monotonicity:** why feasibility at `x` implies feasibility for every larger (or smaller) relevant value.
5. **Choose first true or last true**, then apply a consistent binary-search convention.

```text
low = smallest possible answer
high = largest guaranteed feasible answer
while low < high do
    mid = low + floor((high - low) / 2)
    if feasible(mid) then
        high = mid
    else
        low = mid + 1
    end if
end while
return low
```

- **Time:** `O(C · log R)`, where `C` is the feasibility-check cost and `R` is the size of the answer range.
- **Space:** The feasibility check's auxiliary space, often `O(1)`.
- **Examples:** Minimum ship capacity, minimum eating speed, split-array maximum sum, maximize minimum distance.

#### Common failure: unproven monotonicity

Binary search is invalid if feasibility can alternate true/false as `x` changes. Test the logic verbally: “If capacity `x` works, why must capacity `x+1` work?” Also ensure the greedy feasibility check is itself correct; binary search cannot repair a flawed predicate.

#### Java first-true search

Require `0 <= low <= high`, a monotone false-then-true predicate, and a guaranteed feasible `high`. This closed answer range returns the first feasible value, including `high`; if no feasible value may exist, test the upper endpoint first and define absence explicitly. The parameter uses `java.util.function.LongPredicate`; concrete interviews can call a named feasibility helper instead. The stated nonnegative range makes `high - low` safe.

```java
static long firstTrue(long low, long high, java.util.function.LongPredicate feasible) {
    while (low < high) {
        long middle = low + (high - low) / 2;
        if (feasible.test(middle)) {
            high = middle;
        } else {
            low = middle + 1;
        }
    }
    return low;
}
```

<a id="104-rotated-peak-and-matrix-variants"></a>
### 10.4 Rotated, Peak, and Matrix Variants — 🟠 Tier 2 — Very Important

#### Rotated sorted array

With distinct values, at least one half around `mid` is sorted. Determine the sorted half, test whether the target lies inside its value range, and discard the other half. Duplicates can make the sorted half ambiguous; shrinking equal endpoints may be necessary and can cause `O(n)` worst-case behavior.

#### Peak / slope search

Comparing `a[mid]` with `a[mid+1]` reveals whether a peak lies to the right or at/before `mid` under the problem's structural guarantee. Use a range where `mid+1` is always valid, such as `left < right` with `right = n-1`.

#### Matrix search

- If each row's last element is less than the next row's first, treat the matrix as a flat sorted array: in Java with nonnegative indices, `row = mid / cols` and `col = mid % cols`.
- If rows and columns are independently sorted, staircase search from a corner may be `O(rows + cols)`; a flattened binary search would be invalid without global row-to-row ordering.

Always identify exactly what ordering guarantee the prompt provides.

### Binary Search Recognition and Boundary Audit

| Question | Why it matters |
|---|---|
| What is monotonic or sorted? | Establishes permission to discard half |
| Am I finding any match, first true, or last true? | Determines retained midpoint and returned boundary |
| Is the candidate interval closed or half-open? | Determines initialization and loop condition |
| Can the valid result be `n` or another sentinel? | Prevents out-of-range verification |
| Does each branch strictly shrink the interval? | Guarantees termination |
| Are duplicates allowed? | Changes rotated/first-last reasoning |
| What does the predicate cost? | Gives total `O(C log R)` complexity |

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Bounds | Binary Search | Search Insert Position | Find First and Last Position in Sorted Array | Search in Rotated Sorted Array | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Answer search | First Bad Version | Koko Eating Bananas | Capacity to Ship Packages Within D Days | Split Array Largest Sum | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Search Insert Position; Koko Eating Bananas. Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Binary Search:** Implement exact lookup under one boundary convention and test empty/singleton arrays.
- **Search Insert Position:** Learn lower bound and the valid sentinel result `n`.
- **First Bad Version:** Model first true without direct array access; minimize predicate calls.

#### Core Interview

- **Find First and Last Position in Sorted Array:** Compose lower/upper bounds and verify absent targets.
- **Search in Rotated Sorted Array:** Identify the sorted half and maintain target inclusion boundaries.
- **Find Minimum in Rotated Sorted Array:** Search a boundary using comparison with the right endpoint.
- **Koko Eating Bananas / minimum rate:** Define monotonic feasibility, integer ceiling, and search bounds.
- **Search a 2D Matrix:** Map a virtual flat index only when the global ordering guarantee supports it.

#### Advanced

- **Capacity to Ship Packages Within D Days:** Build a linear greedy feasibility check and search the smallest capacity.
- **Split Array Largest Sum:** Recognize answer search and validate the greedy group-count predicate for nonnegative values.
- **Median of Two Sorted Arrays:** Partition by value order in `O(log min(n,m))`; a valuable advanced boundary exercise, but lower priority than ordinary bounds.

### Common Mistakes, Edge Cases, and Trade-offs

- Mixing `[left, right]` initialization with `[left, right)` loop/update rules.
- Returning immediately on equality when the first/last occurrence is required.
- Failing to verify the lower-bound result before indexing.
- Overflow in midpoint, candidate sums, or feasibility arithmetic.
- Infinite loops because `mid` remains in an unchanged interval.
- Assuming a rotated-array half is identifiable the same way with duplicates.
- Searching an answer before proving predicate monotonicity or selecting guaranteed bounds.
- Forgetting total time is feasibility cost times number of binary-search iterations.

During the interview, state the invariant before code: “I am finding the first feasible capacity; all values below `left` are infeasible, and `high` is guaranteed feasible.” This prevents boundary repair by trial and error.

### Binary Search Mastery Checklist

- [ ] I implement exact binary search with one internally consistent interval convention.
- [ ] I derive lower bound, upper bound, first occurrence, last occurrence, and count of duplicates.
- [ ] I test empty, singleton, absent, all-equal, first, last, and sentinel-`n` cases.
- [ ] I can define first-true/last-true invariants and prove termination.
- [ ] I recognize answer search, choose valid bounds, and prove predicate monotonicity.
- [ ] I include feasibility-check cost in total complexity.
- [ ] I solve standard rotated and matrix variants only from the provided ordering guarantee.
- [ ] I explain when hashing, a linear scan, or staircase search is preferable.

---

## 11. Sorting

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** Sorting arranges records according to a comparison key or ordering rule.
- **Why it exists:** Order exposes adjacency, monotonicity, grouping, and greedy structure that may be hidden in unsorted input.
- **Why it matters in interviews:** Sorting is a high-value preprocessing strategy behind two pointers, intervals, greedy algorithms, deduplication, binary search, and sweep-line reasoning.
- **Interview priority:** 🔴 Tier 1 — Must Master as a problem-solving tool. Hand-implementing every sort is not Tier 1.
- **Prerequisites:** Arrays, comparisons, complexity, mutation, and comparator semantics.
- **Common use cases:** Group equal values, process events chronologically, choose by a key, enable binary search/two pointers, and establish greedy order.
- **Common problem patterns:** Sort + scan, sort + two pointers, sort intervals by start/end, custom comparator, and order statistics.
- **Recognition clues:** Relative order is irrelevant, the input can be reordered, nearest/adjacent values matter, or a greedy choice becomes valid after ordering.
- **Required depth:** Know when sorting simplifies a problem, account for `O(n log n)` and mutation/space, write safe keys/comparators, and understand merge sort/quicksort at a conceptual implementation level.

> **Why this priority?** Sorting is used constantly, but the interview return comes mainly from recognizing what order unlocks and using library sorting correctly. Memorizing obscure sorting implementations offers much less value.

### Focus First

| Subtopic | Priority | Target depth |
|---|---|---|
| Sorting as preprocessing | 🔴 Tier 1 — Must Master | Explain what adjacency/monotonicity/order unlocks |
| Comparator/key-based sorting | 🔴 Tier 1 — Must Master | Lexicographic keys, tie-breaking, valid comparator rules |
| Sort + scan / two pointers / greedy | 🔴 Tier 1 — Must Master | Include sort cost and input-order trade-offs |
| Java sorting behavior | 🔴 Tier 1 — Must Master | Know `Arrays.sort`/`List.sort`, comparator signs, half-open ranges, mutation, and stability |

### Learn Later

| Subtopic | Priority | Target depth |
|---|---|---|
| Merge sort | 🟠 Tier 2 — Very Important | Divide/merge intuition, `O(n log n)`, extra memory, linked-list fit |
| Quicksort and partitioning | 🟠 Tier 2 — Very Important | Pivot partition, average vs worst case, recursion depth |
| Heap sort and selection relationship | 🟡 Tier 3 — Nice to Know | Conceptual comparison; heaps are developed separately |
| Stability and in-place terminology | 🟡 Tier 3 — Nice to Know | Know when equal-key order matters |
| Counting/bucket sorting | 🟡 Tier 3 — Nice to Know | Use when key range or frequency structure is bounded |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Bubble and selection sort implementation | ⚪ Tier 4 — Low Priority / Specialized | Awareness only; poor default choices |
| Radix sort internals, sorting networks, external sort | ⚪ Tier 4 — Low Priority / Specialized | Role/company-specific |
| Library-sort internals | ⚪ Tier 4 — Low Priority / Specialized | Know relevant API guarantees and document implementation-dependent space |

<a id="111-sorting-as-an-interview-strategy"></a>
### 11.1 Sorting as an Interview Strategy — 🔴 Tier 1 — Must Master

Sorting spends `O(n log n)` to create useful structure:

- Equal values become adjacent → duplicates, grouping, frequency runs.
- Small/large extremes become accessible → greedy choices and pair bounds.
- Events become chronological → interval scheduling and sweep lines.
- Monotonic order enables two pointers and binary search.
- A complex identity relation may become a canonical sequence.

Always ask what information sorting destroys:

- Original indices or order.
- Stable relative order among equal keys, if the sort is unstable.
- The untouched input, if sorting in place.

If indices are required, sort records such as `record IndexedValue(int value, int index) {}` or a small class, or use hashing instead. If the input must not be mutated, copy it deliberately (`int[] ordered = input.clone();`) and include `O(n)` extra time/space for that copy.

#### Typical complexity

- Comparison sorting has a general `Ω(n log n)` lower bound in the comparison model.
- Object-array `Arrays.sort` and `List.sort` are stable; primitive-array overloads use different implementations. See the [Java sorting reference](#java-for-dsa-interviews-essential-reference) for API and memory distinctions.
- A Java comparator may be invoked `O(n log n)` times. If deriving a key is expensive, precompute/decorate records rather than recomputing an `O(L)` key on every comparison.
- A subsequent linear scan leaves total time `O(n log n + n) = O(n log n)`.

<a id="112-comparator-and-key-based-sorting"></a>
### 11.2 Comparator and Key-Based Sorting — 🔴 Tier 1 — Must Master

Sort custom records with `Comparator` key extractors or a short lambda. Records do not automatically implement natural ordering; specify the comparison and tie-breaks that the algorithm needs.

```java
record IntervalKey(int start, int end) {}

static void orderRecords(List<IntervalKey> records) {
    records.sort(Comparator.comparingInt(IntervalKey::start)
            .thenComparing(Comparator.comparingInt(IntervalKey::end).reversed()));
}
```

Tie-breaking is algorithmic, not cosmetic. For example, sorting intervals by ending time supports maximum non-overlapping selection; sorting by starting time supports merging.

A comparator must define a consistent ordering:

- Return a negative integer when the first argument precedes the second, zero for an ordering tie, and a positive integer when it follows.
- Keep comparison transitive, sign-symmetric, and consistent for values that compare as zero.
- Use `Integer.compare` or `Long.compare`; subtraction can silently overflow and violate ordering.
- Handle equal keys deliberately.

For “largest concatenated number,” compare `a+b` with `b+a`; numeric or ordinary lexicographic order alone is insufficient. After sorting, normalize an all-zero result.

<a id="113-stability-and-in-place-behavior"></a>
### 11.3 Stability and In-Place Behavior — 🟡 Tier 3 — Nice to Know

A **stable** sort preserves the input relative order of records whose keys compare equal. Stability matters when:

- A previous ordering should survive ties in a later sort.
- Equal-key records have meaningful chronological/input order.
- Multi-pass sorting relies on earlier lower-priority keys.

It does not matter when all keys are unique or an explicit full tie-break key determines the desired order.

“In place” and “stable” are independent properties. `Arrays.sort` mutates its array or specified half-open range; object-array sorting and `List.sort` preserve ties. They may allocate linear auxiliary buffers; primitive-array sorting has implementation-dependent scratch space. State only guarantees relevant to the interview.

### 11.4 Which Sorting Algorithms to Understand

| Algorithm | Priority | Time | Extra space | Stable? | Interview depth |
|---|---|---:|---:|---|---|
| Java object `Arrays.sort` / `List.sort` | 🔴 Tier 1 — Must Master | `O(n log n)` with constant-time comparison | `O(n)` worst-case buffer | Yes | Use confidently; know comparator, range, and mutation |
| Java primitive `Arrays.sort(int[])` | 🔴 Tier 1 — Must Master | `O(n log n)` documented JDK implementation | Implementation-dependent; budget `O(n)` conservatively | No record ties to preserve | Ascending primitive values; no comparator overload |
| Merge sort | 🟠 Tier 2 — Very Important | `O(n log n)` worst case | `O(n)` for arrays; list merge can use link rewiring | Yes in standard form | Explain split/merge; implement if asked |
| Quicksort | 🟠 Tier 2 — Very Important | Average `O(n log n)`, worst `O(n²)` | Average `O(log n)` stack, worst `O(n)` | Usually no | Explain pivot/partition and randomization; basic implementation |
| Insertion sort | 🟡 Tier 3 — Nice to Know | Worst `O(n²)`, best `O(n)` on already sorted with standard form | `O(1)` | Yes | Understand small/nearly sorted use |
| Heap sort | 🟡 Tier 3 — Nice to Know | `O(n log n)` worst case | `O(1)` auxiliary in array form | No | Conceptual; prioritize heap operations |
| Counting sort / frequency array | 🟡 Tier 3 — Nice to Know | `O(n + R)` for range `R` | `O(R)` for counts; stable record output also needs `O(n)` | Depends on form | Recognize bounded key range/frequencies |
| Selection sort | ⚪ Tier 4 — Low Priority / Specialized | `O(n²)` | `O(1)` | Usually no | Awareness only |
| Bubble sort | ⚪ Tier 4 — Low Priority / Specialized | `O(n²)` | `O(1)` | Yes in standard form | Awareness only |
| Radix sort | ⚪ Tier 4 — Low Priority / Specialized | Digit/radix-dependent | Radix-dependent | Can be | Conceptual only for normal SWE interviews |

#### Merge sort intuition

Recursively sort each half, then merge two sorted halves in linear time. There are `O(log n)` levels and `O(n)` merge work per level, giving `O(n log n)`. It offers predictable worst-case time and stability, but array merging uses extra memory.

#### Quicksort intuition

Choose a pivot, partition values around it, then recursively sort partitions. Balanced partitions give `O(n log n)`; consistently extreme pivots give `O(n²)`. Randomization or robust pivot selection makes pathological imbalance less likely, but does not turn the theoretical worst case into `O(n log n)` for ordinary quicksort.

Do not memorize partition code without defining regions and the pivot's final meaning. Different Lomuto/Hoare schemes have different return values and recursive boundaries.

<a id="115-non-comparison-sorting"></a>
### 11.5 Non-Comparison Sorting — 🟡 Tier 3 — Nice to Know

The `Ω(n log n)` comparison lower bound does not apply when keys have exploitable structure. If integer values lie in a small range, frequency counting can sort in `O(n + R)` time with `O(R)` extra space. This is worse than comparison sorting when `R` is enormous relative to `n`.

Buckets can also organize values by frequency or a bounded measure. Define bucket ordering and memory use; “bucket sort is linear” is incomplete without assumptions about key distribution/range.

### Sorting Recognition and Trade-offs

| Need | Sorting approach | Competing approach / trade-off |
|---|---|---|
| Find duplicates | Sort + adjacent scan | Set is expected `O(n)` but uses `O(n)` storage |
| Pair sum | Sort + two pointers | Hash map preserves linear expected time and can retain indices |
| Merge intervals | Sort by start then scan | Without order, overlap decisions are difficult |
| Top `k` | Full sort `O(n log n)` | Heap `O(n log k)` or selection average `O(n)` |
| Repeated lookup | Sort once + binary searches | Hash map gives expected `O(1)` exact lookup but not order queries |
| Small bounded integers | Counting sort/frequency array | Comparison sort avoids `O(R)` memory for large sparse range |
| Need original order | Sort decorated records or copy | Avoid sorting if order itself is part of correctness |

### Selected practice ladder

Start with 3–4 distinct problems for the selected pattern; reuse overlaps across chapters. Work left to right only when the previous invariant can be explained unaided. The bank below provides substitutions and targeted follow-ups, not additional quotas. For a Tier 3 row, one mechanics/canonical pair is enough unless later evidence justifies the harder columns.

| Pattern | 1. Mechanics | 2. Canonical | 3. Variation | 4. Mixed pattern | 5. Cold revisit |
|---|---|---|---|---|---|
| Ordering as a tool | Valid Anagram (sort version) | Merge Intervals | Largest Number | 3Sum | Canonical after 2–3 days; variation after 1–2 weeks without hints |
| Sort mechanics (Tier 2; select later) | Merge two sorted arrays | Sort an Array (merge sort) | Sort List | Count Inversions archetype | Canonical after 2–3 days; variation after 1–2 weeks without hints |

> ⭐ **Canonical Interview Problem:** Merge Intervals; Sort an Array (merge sort). Explain the invariant before opening the implementation.

### Representative problem bank — choose as needed

#### Beginner

- **Merge Sorted Array:** Exploit existing order rather than sorting the combined data again.
- **Valid Anagram (sorting version):** Compare simple `O(n log n)` sorting with `O(n)` frequency counting and discuss alphabet assumptions.
- **Squares of a Sorted Array:** Preserve sorted output with two pointers; learn not to sort again unnecessarily.

#### Core Interview

- **Merge Intervals:** Sort by start and maintain the merged result's last interval.
- **3Sum:** Sort to enable duplicate-aware two-pointer search and deterministic output.
- **Meeting Rooms / Non-overlapping Intervals:** Select the correct sort key—start or end—based on the objective.
- **Largest Number:** Build a domain-specific comparator from concatenation order and handle all zeros.
- **Sort List:** Apply merge sort where linked-list splitting/merging fits the representation.

#### Advanced

- **Count Inversions / Count Smaller After Self:** Augment merge sort to count cross-half relationships instead of enumerating pairs.
- **Quickselect for kth largest:** Reuse partitioning to avoid fully sorting; analyze average `O(n)` and worst `O(n²)`.
- **Maximum Gap with buckets:** Use numeric range structure; learn that linear claims depend on precise assumptions.

### Common Mistakes, Edge Cases, and Interview Tips

- Saying “sort it” without explaining what sorted order makes possible.
- Forgetting the sort dominates a subsequent linear scan.
- Mutating input without permission or losing original indices.
- Choosing the wrong interval key or omitting a necessary tie-breaker.
- Writing an inconsistent comparator or using overflow-prone subtraction.
- Assuming stable or in-place behavior without knowing the API guarantee.
- Claiming quicksort is always `O(n log n)` or counting sort is unconditionally `O(n)`.
- Hand-implementing a sort when a standard library sort is allowed and the interview is testing the surrounding algorithm.

In an interview, say: “Sorting by end time makes the earliest-finishing compatible choice visible,” or “Sorting groups equal values so one scan detects duplicates.” This ties the preprocessing cost to a correctness benefit.

### Sorting Mastery Checklist

- [ ] I recognize when sorting reveals adjacency, monotonicity, chronological order, or a greedy choice.
- [ ] I include sorting time, internal/copy space, mutation, and lost-index trade-offs.
- [ ] I write safe key functions and deliberate tie-breakers.
- [ ] I know when stability matters and do not assume it blindly.
- [ ] I explain merge sort and quicksort, including their time/space and worst-case differences.
- [ ] I know which simple sorts are awareness-only and why library sort is usually preferred.
- [ ] I recognize bounded-range opportunities for counting/bucket methods without ignoring `R`.
- [ ] I compare full sorting with hashing, heaps, binary search, and selection appropriately.

---

## 12. Trees

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Tree questions combine recursion, traversal, state design, and object-reference reasoning in a form that appears constantly in general Software Engineering interviews. A candidate should be able to traverse a binary tree, choose DFS or BFS, state an invariant, and analyze `O(n)` time without prompting. Specialized balanced-tree internals are far less important.

### Topic Overview

- **What it is:** A tree is a connected, acyclic graph. In a rooted tree, every node except the root has one parent. A binary tree gives each node at most two children.
- **Why it exists:** Trees represent hierarchy and support divide-and-conquer reasoning: solve the same smaller problem for each child, then combine the results.
- **Why it matters in interviews:** Trees test recursion, queues, stacks, careful base cases, and the ability to define what a function returns. Many seemingly different questions are one of a few traversal patterns.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Big-O, recursion and call stacks, stack/queue operations, Java object references, and basic hashing.
- **Common use cases:** File systems, syntax trees, organization hierarchies, search indexes, decision trees, and nested UI data.
- **Common problem patterns:** Root-to-leaf aggregation, subtree summaries, level-order processing, path constraints, BST ordering, ancestor queries, tree construction, and encode/decode.
- **How to recognize problems that require it:** The input contains nodes with child links, parent-child relationships, hierarchy, ancestry, levels, subtrees, or a connected graph with `n - 1` edges.
- **How deeply to understand it:** Deeply understand binary-tree DFS/BFS, BST invariants, height/depth, and recursive return-value design. Be comfortable with LCA, reconstruction, and serialization. Only know balanced-tree implementation details conceptually unless a role demands them.

### Focus First

- DFS: preorder, inorder, and postorder, especially recursive traversal.
- BFS/level-order traversal with a queue.
- The two core recursive designs: **pass state downward** and **return a subtree summary upward**.
- Height, depth, balance, diameter, path-sum, and BST-bound invariants.
- Null-node and single-node edge cases.

### Learn Later

- Iterative traversal details, lowest common ancestor, reconstruction from traversals, and serialization.
- Morris traversal, threaded traversal, and parent-reference variations after ordinary traversals are automatic.

### Optional / Specialized

- **AVL/red-black tree rotations — ⚪ Tier 4 — Low Priority / Specialized.** Standard libraries provide balanced ordered maps/sets; implementing rotations is rare in general interviews.
- **B-trees/B+ trees — ⚪ Tier 4 — Low Priority / Specialized.** Important for databases and storage-system interviews, but not normal coding rounds.
- **Morris traversal — 🟡 Tier 3 — Nice to Know.** It achieves `O(1)` auxiliary space by temporarily threading the tree, but mutates links and is rarely the clearest interview solution.

**Study target:** Reconstruct DFS/BFS and BST validation from memory; understand subtree contracts rather than memorizing solutions. Complete the core practice ladder below, then choose construction/LCA/codec variations. Balanced-tree internals are reference knowledge. Recursion foundations in [Section 3](#3-complexity-analysis-and-foundations) come before this topic.

### 12.1 Binary-Tree Fundamentals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Nearly every tree problem depends on precise vocabulary and a correct null-safe node representation.

```java
static class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) {
        this.val = val;
    }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
```

**Java convention:** Tree snippets share this `TreeNode` class and use object references. A platform may supply its own equivalent node class. All inputs are non-null unless the contract explicitly uses `null` for an absent tree/node. Member snippets can be placed inside a solution class with `import java.util.*;`; `static` is a convenience for local practice.

> 🌐 **Java Backend Relevance — HIGH:** Java is always pass-by-value: passing a node copies its reference. Mutating `node.left` changes the shared object; assigning a different object to the parameter only changes that local variable. Use identity comparisons when two references must denote the same node.

Key distinctions:

- **Root:** the only node with no parent.
- **Leaf:** a node with no children.
- **Depth of a node:** number of edges from the root to that node; root depth is `0`.
- **Height of a node:** number of edges on the longest downward path from that node to a leaf. Some platforms count nodes instead; state your convention.
- **Subtree:** a node and all of its descendants.
- **Balanced binary tree:** commonly, every node's left/right subtree heights differ by at most `1`.
- **Full, complete, and perfect** mean different things. A complete tree fills every level except possibly the last, which fills left to right; heaps rely on this shape.

**Complexity baseline:** A traversal that visits each node once costs `O(n)` time. Recursive DFS uses `O(h)` call-stack space, where `h` is tree height: `O(log n)` for a balanced tree and `O(n)` for a skewed tree. A BFS queue can hold `O(w)` nodes, where `w` is maximum width, and `O(n)` in the worst case.

### 12.2 Recursive DFS Traversals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Recursive DFS is the default language of binary-tree interviews. Traversal order tells you when the current node is processed relative to its children.

#### Intuition and how it works

At each node there are three moments: **before** visiting children, **between** the left and right child, and **after** both children. Those moments produce preorder, inorder, and postorder.

| Traversal | Order | Best mental cue | Common uses |
|---|---|---|---|
| Preorder | node, left, right | Process parent before descendants | Copy/serialize tree, propagate path state |
| Inorder | left, node, right | Process node between subtrees | Sorted values in a BST |
| Postorder | left, right, node | Children report before parent | Height, balance, diameter, delete/evaluate tree |

```java
static List<Integer> preorder(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    preorderDfs(root, result);
    return result;
}

static void preorderDfs(TreeNode node, List<Integer> result) {
    if (node == null) return;
    result.add(node.val);
    preorderDfs(node.left, result);
    preorderDfs(node.right, result);
}

static List<Integer> inorder(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    inorderDfs(root, result);
    return result;
}

static void inorderDfs(TreeNode node, List<Integer> result) {
    if (node == null) return;
    inorderDfs(node.left, result);
    result.add(node.val);
    inorderDfs(node.right, result);
}

static List<Integer> postorder(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    postorderDfs(root, result);
    return result;
}

static void postorderDfs(TreeNode node, List<Integer> result) {
    if (node == null) return;
    postorderDfs(node.left, result);
    postorderDfs(node.right, result);
    result.add(node.val);
}
```

**When to use / recognition clues:** Use DFS when the answer depends on complete subtrees, root-to-leaf paths, ancestors, or aggregating child results. Words such as *subtree*, *path*, *descendant*, *height*, *balanced*, or *ancestor* are strong clues.

**Complexity:** All three visit `n` nodes: `O(n)` time and `O(h)` auxiliary stack space, excluding output.

#### The most useful recursive contract

Before coding, finish this sentence: **`dfs(node)` returns ...** For example, in a balance check it returns the subtree height, or a sentinel saying the subtree is already unbalanced.

```java
static boolean isBalanced(TreeNode root) {
    return heightOrFail(root) != -1;
}

static int heightOrFail(TreeNode node) {
    if (node == null) return 0;
    int left = heightOrFail(node.left);
    if (left == -1) return -1;
    int right = heightOrFail(node.right);
    if (right == -1 || Math.abs(left - right) > 1) return -1;
    return 1 + Math.max(left, right);
}
```

This single postorder traversal is `O(n)`. Recomputing height separately at every node can degrade to `O(n^2)` on a skewed tree.

**Common mistakes:** Writing recursion before defining its return meaning; forgetting the `null` case; mixing node-count and edge-count height; using shared global state unnecessarily; recomputing a subtree; and forgetting that a skewed tree makes recursion depth `O(n)`.

**Edge cases:** Empty tree, one node, one-sided tree, duplicate values, negative values, and a path where the optimal answer does not include the root.

**Alternatives and trade-offs:** Recursive DFS is compact and mirrors the structure. Iterative DFS avoids exhausting the finite Java call stack (a deep traversal can throw `StackOverflowError`) but makes postorder and carried state more explicit. Both have the same asymptotic worst-case auxiliary space.

### 12.3 Iterative DFS Traversals

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Interviewers often request an iterative variant or use input deep enough to make recursion unsafe. Preorder and inorder should be comfortable; iterative postorder is useful but less frequently required.

```java
static List<Integer> preorderIterative(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) return result;
    Deque<TreeNode> pending = new ArrayDeque<>();
    pending.push(root);
    while (!pending.isEmpty()) {
        TreeNode node = pending.pop();
        result.add(node.val);
        // Push right first so left is processed first.
        if (node.right != null) pending.push(node.right);
        if (node.left != null) pending.push(node.left);
    }
    return result;
}

static List<Integer> inorderIterative(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    Deque<TreeNode> pending = new ArrayDeque<>();
    TreeNode current = root;
    while (current != null || !pending.isEmpty()) {
        while (current != null) {
            pending.push(current);
            current = current.left;
        }
        current = pending.pop();
        result.add(current.val);
        current = current.right;
    }
    return result;
}

record TreeFrame(TreeNode node, boolean expanded) {}

static List<Integer> postorderIterative(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) return result;
    Deque<TreeFrame> pending = new ArrayDeque<>();
    pending.push(new TreeFrame(root, false));
    while (!pending.isEmpty()) {
        TreeFrame frame = pending.pop();
        TreeNode node = frame.node();
        if (frame.expanded()) {
            result.add(node.val);
        } else {
            pending.push(new TreeFrame(node, true));
            if (node.right != null) pending.push(new TreeFrame(node.right, false));
            if (node.left != null) pending.push(new TreeFrame(node.left, false));
        }
    }
    return result;
}
```

**How it works:** The stack stores unfinished work. A `(node, expanded)` marker preserves the return-to-parent moment that recursion normally supplies.

**Complexity:** `O(n)` time and `O(h)` live auxiliary stack for each binary-tree traversal shown, excluding `O(n)` output. A skewed tree has `h = O(n)`. Postorder creates `O(n)` short-lived frame objects overall, even though only `O(h)` frames are live at once.

**Mistakes:** Pushing preorder children in the wrong order, losing the current node reference in inorder, processing postorder before its children, and forgetting the `root == null` case.

<a id="124-tree-bfs-level-order-traversal"></a>
### 12.4 Tree BFS / Level-Order Traversal

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** BFS is the direct solution for levels, nearest depth, right/left views, and minimum edge distance in an unweighted tree.

```java
static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) return levels;
    Queue<TreeNode> pending = new ArrayDeque<>();
    pending.offer(root);
    while (!pending.isEmpty()) {
        int levelSize = pending.size();
        List<Integer> level = new ArrayList<>(levelSize);
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = pending.poll();
            level.add(node.val);
            if (node.left != null) pending.offer(node.left);
            if (node.right != null) pending.offer(node.right);
        }
        levels.add(level);
    }
    return levels;
}
```

**Recognition:** The problem says *level*, *row*, *nearest*, *minimum depth*, *view from a side*, or asks for nodes in increasing distance from the root.

**Complexity:** `O(n)` time; `O(w)` queue space where `w` is maximum tree width.

**Trade-off:** BFS exposes levels naturally but can hold a very wide level. DFS is often smaller on a wide balanced tree and is better when the result is a subtree summary.

**Common mistakes and edge cases:** Using `ArrayList.remove(0)` instead of an `ArrayDeque` queue, not snapshotting the queue length before a level, trying to enqueue `null` (`ArrayDeque` rejects it), or returning one empty level for an empty tree.

### 12.5 Height, Depth, Balance, Diameter, and Paths

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** These are canonical examples of subtree-summary recursion and are among the highest-transfer tree patterns.

```java
static int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}
```

For diameter, each child returns a height. At a node, the best path *through* the node is `left_height + right_height`; the recursion returns only one extendable branch upward.

```java
static int diameterOfBinaryTree(TreeNode root) {
    int[] best = {0}; // Per-call mutable holder; no state leaks between calls.
    diameterHeight(root, best);
    return best[0]; // Number of edges.
}

static int diameterHeight(TreeNode node, int[] best) {
    if (node == null) return 0;
    int left = diameterHeight(node.left, best);
    int right = diameterHeight(node.right, best);
    best[0] = Math.max(best[0], left + right);
    return 1 + Math.max(left, right);
}
```

**Recognition:** The problem asks for a longest path, maximum contribution, balance, or an answer that may pass through a node and combine both children.

**Core distinction:** The value returned to the parent and the globally best complete answer may be different. A parent can extend only one downward branch, while a complete path at the current node can combine two branches.

**Complexity:** Proper one-pass designs are `O(n)` time and `O(h)` stack space.

**Mistakes:** Returning a two-branch path to a parent, assuming the best path passes through the root, initializing a maximum to `0` when all values can be negative, and confusing depth with height.

### 12.6 Binary Search Trees

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** BST questions test invariant reasoning and ordered search. Validation, search, kth-smallest, and ancestor variants are common even though hand-building a balanced BST is not.

#### Core intuition and invariant

For every node, **all** values in its left subtree must lie on the permitted lower side and **all** values in its right subtree on the permitted upper side. It is not enough to compare a node only with its immediate children.

```java
static boolean isValidBst(TreeNode root) {
    return validBst(root, Long.MIN_VALUE, Long.MAX_VALUE);
}

static boolean validBst(TreeNode node, long low, long high) {
    if (node == null) return true;
    if (node.val <= low || node.val >= high) return false;
    return validBst(node.left, low, node.val)
            && validBst(node.right, node.val, high);
}
```

An inorder traversal of a strict BST is strictly increasing. Bounds validation is usually more explicit about the invariant; inorder validation is convenient for kth-smallest and sorted iteration.

```java
static TreeNode searchBst(TreeNode root, int target) {
    TreeNode current = root;
    while (current != null) {
        if (target == current.val) return current;
        current = target < current.val ? current.left : current.right;
    }
    return null;
}
```

**When to use / recognition:** Ordered binary tree; searching by comparison; predecessor/successor; range query; kth smallest; or the input explicitly promises a BST.

**Complexity:** Search/insert/delete take `O(h)`: `O(log n)` if balanced, worst-case `O(n)` if skewed. Traversal is `O(n)`. Do not claim `O(log n)` without a balance guarantee.

**Duplicates:** The problem must define whether duplicates are forbidden, counted, or always placed on one side. Adjust `<`/`<=` and bounds deliberately.

**Alternatives/trade-offs:** `HashMap` has expected `O(1)` exact lookup but no sorted order. `TreeMap` gives `O(log n)` search and ordered operations. Sorting once can be simpler for static data.

**Common mistakes:** Local-only validation, assuming balance, using a stale previous-inorder value, mishandling numeric bounds, and mutating tree links during a read-only query.

### 12.7 Lowest Common Ancestor

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** LCA is a recurring ancestor/path pattern and an excellent test of recursive meaning, but appears less often than basic traversal.

For a general binary tree, let `dfs(node)` return a target if found in the subtree. If left and right both return non-null, the current node is their lowest meeting point.

```java
static TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    if (left != null && right != null) return root;
    return left != null ? left : right;
}
```

**Assumption:** Both non-null target references belong to the tree; `p == q` is allowed. If existence is not guaranteed, verify each distinct target exists before accepting the result, or return match information with the subtree result. Here `==` intentionally checks node identity, not equal values.

For a BST, ordering avoids searching both sides: if both values are smaller go left, if both are larger go right, otherwise the current node is the split point. Time is `O(h)` instead of a general `O(n)` traversal.

**Mistakes:** Comparing values when node identity matters, not clarifying whether a node can be its own ancestor, ignoring missing targets, or applying the BST shortcut to a normal binary tree.

### 12.8 Constructing Trees from Traversals

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Reconstruction tests whether you understand traversal boundaries and divide-and-conquer. Standard versions occur regularly; duplicate-heavy or exotic traversal pairs are less important.

Preorder reveals the root first. Inorder tells how many nodes belong to the left and right subtrees. A value-to-inorder-index map prevents a linear scan in each recursive call.

```java
static TreeNode buildTree(int[] preorder, int[] inorder) {
    if (preorder.length != inorder.length) {
        throw new IllegalArgumentException("traversal lengths must match");
    }
    Map<Integer, Integer> inorderIndex = new HashMap<>();
    for (int i = 0; i < inorder.length; i++) {
        if (inorderIndex.put(inorder[i], i) != null) {
            throw new IllegalArgumentException("values must be unique");
        }
    }
    int[] next = {0};
    return buildSubtree(preorder, inorderIndex, next, 0, inorder.length - 1);
}

static TreeNode buildSubtree(int[] preorder, Map<Integer, Integer> inorderIndex,
                             int[] next, int left, int right) {
    if (left > right) return null;
    if (next[0] >= preorder.length) {
        throw new IllegalArgumentException("inconsistent traversals");
    }
    int value = preorder[next[0]++];
    Integer split = inorderIndex.get(value);
    if (split == null || split < left || split > right) {
        throw new IllegalArgumentException("inconsistent traversals");
    }
    TreeNode root = new TreeNode(value);
    root.left = buildSubtree(preorder, inorderIndex, next, left, split - 1);
    root.right = buildSubtree(preorder, inorderIndex, next, split + 1, right);
    return root;
}
```

**Complexity:** Expected `O(n)` time using `HashMap`, with `O(n)` map and `O(h)` recursion stack, excluding the `O(n)` returned tree. Without the map, worst-case time is `O(n^2)`.

**Recognition:** Two traversals, unique values, reconstruct original hierarchy, or sorted array to height-balanced BST.

**Edge cases/mistakes:** Empty inputs, inconsistent traversal lengths, duplicates that make a single index map insufficient, off-by-one subrange boundaries, building right before left when consuming preorder, and copying array subranges instead of passing indices.

**Alternative:** Postorder + inorder also uniquely reconstructs a tree with unique values; consume postorder from the end and build right before left.

### 12.9 Serialization and Deserialization

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Encode/decode questions test whether structure—not just values—is preserved. The pattern is common enough to practice once or twice, especially for platform/infrastructure roles.

Preorder plus an explicit null marker uniquely represents a binary tree.

```java
static String serialize(TreeNode root) {
    StringBuilder output = new StringBuilder();
    serializeDfs(root, output);
    return output.toString();
}

static void serializeDfs(TreeNode node, StringBuilder output) {
    if (node == null) {
        output.append("#,");
        return;
    }
    output.append(node.val).append(',');
    serializeDfs(node.left, output);
    serializeDfs(node.right, output);
}

static TreeNode deserialize(String data) {
    // Keep empty tokens so malformed delimiters are rejected.
    String[] tokens = data.split(",", -1);
    if (!tokens[tokens.length - 1].isEmpty()) {
        throw new IllegalArgumentException("encoding must end with a comma");
    }
    int[] next = {0};
    TreeNode root = deserializeNode(tokens, next);
    if (next[0] != tokens.length - 1) {
        throw new IllegalArgumentException("extra tree tokens");
    }
    return root;
}

static TreeNode deserializeNode(String[] tokens, int[] next) {
    if (next[0] >= tokens.length - 1) {
        throw new IllegalArgumentException("truncated tree encoding");
    }
    String token = tokens[next[0]++];
    if (token.equals("#")) return null;
    TreeNode node = new TreeNode(Integer.parseInt(token));
    node.left = deserializeNode(tokens, next);
    node.right = deserializeNode(tokens, next);
    return node;
}
```

**Complexity:** `O(n)` time and `O(n)` encoded output for fixed-width `int` values. Encoding uses a `StringBuilder` plus `O(h)` recursion; decoding uses `O(n)` extra token/string storage from `split`, plus `O(h)` recursion and the returned tree. An index-based streaming parser can avoid the token array but is a later refinement.

**Trade-offs:** The preorder format is compact and easy to parse recursively; the decoder above splits tokens for simplicity. BFS serialization is also valid and visually corresponds to level order, but may need trailing-null normalization. A BST can sometimes be encoded without null markers using ordering constraints, at the cost of a more specialized decoder.

**Mistakes/edge cases:** Omitting null markers, unsafe delimiter parsing, negative/multi-digit values, empty tree, recursive depth limits, and requiring globally unique node values when the format should not.

### Tree Pattern Map

| Problem clue | First approach | State/invariant to define |
|---|---|---|
| Values by level, nearest leaf, side view | BFS | Queue contains current frontier |
| Height, balance, subtree property | Postorder DFS | What each subtree returns |
| Root-to-leaf path | Preorder DFS/backtracking | State carried down and undone |
| Sorted output from BST | Inorder DFS | Left values before node before right |
| Kth smallest in BST | Inorder + counter | Number of visited ordered nodes |
| Two targets and ancestors | LCA DFS | Which targets a subtree contains |
| Longest path through nodes | Postorder + global best | Extendable branch versus complete path |
| Create/copy/encode hierarchy | Preorder or BFS | Explicit representation of null/children |

### Representative Tree Problems

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Subtree summaries | Maximum Depth of Binary Tree | ⭐ **Canonical Interview Problem:** Diameter of Binary Tree | Balanced Binary Tree | Binary Tree Maximum Path Sum (later) | Diameter: distinguish returned branch from full path |
| Traversal/frontiers | Inorder Traversal, recursive then iterative | ⭐ **Canonical Interview Problem:** Binary Tree Level Order Traversal | Right Side View (standard, not advanced) | All Nodes Distance K (after graphs) | Level Order without a queue-size bug |
| Ordering/structure | Search in a BST | ⭐ **Canonical Interview Problem:** Validate Binary Search Tree | Kth Smallest in a BST | LCA / Construct from Preorder and Inorder | Validate a tree that violates an ancestor bound |

Choose one row at a time; the cold revisit repeats a selected problem without notes after 2–7 days, then again after several weeks. Additional focused drills: **Same Tree** for structural null cases, **Invert Binary Tree** for mutation, **Serialize and Deserialize Binary Tree** for structure preservation (1–2 attempts). **Recover BST** and **Vertical Order Traversal** are later extensions, not gates before graph study.

### Common Tree Mistakes and Interview Tips

- Draw a three-node example and an empty/single-node tree before coding.
- State whether height/path length counts nodes or edges.
- Say what `dfs(node)` returns and what side effects it has.
- Do not claim recursive space is `O(1)`; the call stack counts.
- Confirm whether values are unique and whether target nodes are guaranteed to exist.
- For an `ArrayList<Integer>` path, use `add(value)` before recursion and `remove(path.size() - 1)` after it. Store answers with `new ArrayList<>(path)` so later mutation cannot change earlier answers.
- Test a skewed tree. It exposes false `O(log n)` claims and recursion-depth risks.

### Tree Mastery Checklist

I have mastered interview trees when I can:

- [ ] Explain preorder, inorder, postorder, and level order without notes.
- [ ] Implement recursive DFS, iterative preorder/inorder, and BFS from scratch.
- [ ] Choose traversal order from when information becomes available.
- [ ] Define a clear recursive return contract before writing code.
- [ ] Derive `O(n)` time and `O(h)`/`O(w)` auxiliary space accurately.
- [ ] Solve depth, balance, diameter, path-sum, and subtree-comparison variants.
- [ ] Validate a BST with inherited bounds and explain why local checks fail.
- [ ] Solve standard LCA and tree-construction questions.
- [ ] Serialize and deserialize while preserving null structure.
- [ ] Explain recursion-versus-iteration and BFS-versus-DFS trade-offs.

---

<a id="13-heaps-priority-queues"></a>
## 13. Heaps / Priority Queues

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Heaps are the standard tool when an interview asks for the next smallest/largest item, a dynamic top `k`, or repeated selection from several ordered sources. The API and common patterns matter much more than implementing a heap from scratch.

### Topic Overview

- **What it is:** A heap is a complete binary tree, usually stored in an array, whose parent has priority over its children. A priority queue is the abstract operation set commonly implemented by a heap.
- **Why it exists:** It maintains access to an extreme-priority element while allowing efficient insertions and removals.
- **Why it matters in interviews:** It turns repeated `min`/`max` scans from `O(n)` each into `O(log n)` updates and supports top-K, scheduling, merging, and shortest-path algorithms.
- **Interview priority:** 🟠 Tier 2 — Very Important.
- **Prerequisites:** Arrays, comparator/order rules, Big-O, and basic tree indexing.
- **Common use cases:** Task scheduling, event simulation, top-K queries, streaming statistics, Dijkstra, and merging sorted sequences.
- **Common problem patterns:** Keep the best `k`; repeatedly choose a current minimum; process jobs by priority; maintain two halves of a stream.
- **How to recognize problems that require it:** The input changes while you repeatedly need the smallest/largest, the `k` best, or the next item across sorted sources. Words such as *top K*, *kth*, *priority*, *closest*, *next available*, and *stream* are clues.
- **How deeply to understand it:** Fluently use `PriorityQueue`, choose its default min-heap versus `Comparator.reverseOrder()` for a max-heap, design records/classes and comparators for entries, and analyze heap size. Know array indexing and heapify conceptually; implement sift operations only if explicitly requested.

### Focus First

- Min-heap versus max-heap and how to choose the corresponding `PriorityQueue` comparator.
- `offer`, `peek`, and `poll` complexity.
- Fixed-size heap for top-K and kth-element questions.
- K-way merge and named priority-entry records with explicit comparators.

### Learn Later

- Two-heaps running median, lazy deletion, and heap-based schedulers.
- `O(n)` bottom-up heap construction and why it is not `O(n log n)`.

### Optional / Specialized

- **Indexed heaps / decrease-key implementation — 🟡 Tier 3 — Nice to Know.** Useful conceptually for graph algorithms; most interview code inserts a fresh immutable entry and skips stale entries.
- **Binomial/Fibonacci/other meldable heaps — ⚪ Tier 4 — Low Priority / Specialized.** Theoretical or specialized; not useful enough for normal interview preparation.

### 13.1 Heap Fundamentals and Operations

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** All heap patterns depend on correctly understanding what is—and is not—ordered.

**Study target:** Reproduce min/max declarations and top-K from memory; understand the root invariant and derive k-way merge. Practice 4–6 selected problems plus cold revisits. Two-heaps/lazy-deletion implementations can wait; heap internals are lookup material unless requested.

For a zero-indexed array:

- Parent of index `i > 0`: `(i - 1) / 2` with integer division; the root has no parent
- Left child: `2 * i + 1`
- Right child: `2 * i + 2`

A min-heap guarantees only that each parent is `<=` its children, so the root is globally smallest. The rest of the array is **not sorted**.

| Operation | Binary heap time | Notes |
|---|---:|---|
| `peek()` min/max | `O(1)` | Read the root |
| Insert | `O(log n)` | Append, then sift up |
| Remove root | `O(log n)` | Move last item to root, then sift down |
| Heapify `n` existing items | `O(n)` | Bottom-up construction |
| Search arbitrary value | `O(n)` | Heap order does not support general search |

```java
static void heapOperationsExample() {
    // Collection construction uses bottom-up heap construction in OpenJDK.
    PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(7, 2, 5));
    minHeap.offer(3);                 // O(log n)
    int smallest = minHeap.peek();    // O(1); nonempty before unboxing
    int removed = minHeap.poll();     // O(log n); returns the removed element

    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
    maxHeap.offer(7);
    int largest = maxHeap.peek();
    maxHeap.poll();
}
```

> 🌐 **Java Backend Relevance — HIGH:** Comparator correctness also matters in application sorting and scheduling. Keep priority fields immutable while entries are stored. `PriorityQueue` is not thread-safe; sharing it across worker threads needs a separate concurrency design.

**How it works:** Sift-up repairs the one possibly broken path from a new leaf to the root. Sift-down repairs the path from a replaced root to a leaf. Only one root-to-leaf path changes, hence `O(log n)`.

**Common mistakes:** Reading heap iteration as sorted order; comparing the wrong entry field; forgetting Java defaults to a min-heap; unboxing a `null` returned by `peek()`/`poll()` on an empty heap; changing an entry's priority while it is stored; and assuming `remove(Object)` or `contains` is logarithmic (both are `O(n)`). Use a safe comparator such as `Comparator.comparingLong`, not subtraction.

**Edge cases:** Empty heap before unboxing `peek()`/`poll()`, equal priorities, `null` entries (forbidden), `k = 0`, and `k > n`. Comparator ties are allowed; add tie-breakers only when output order requires them. Records do not automatically implement `Comparable`.

**Trade-offs:** A heap gives only the next extreme, not full order. `TreeSet`/`TreeMap` support predecessor and range operations. Sorting gives all items in order but makes repeated dynamic insertions costly.

### 13.2 Top-K and Kth-Element Pattern

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Top-K is one of the most common direct heap signals and transfers to frequency, distance, score, and streaming problems.

To keep the `k` largest values, maintain a **min-heap of size at most `k`**. The root is the weakest member of the current winners.

```java
static List<Integer> kLargest(int[] values, int k) {
    List<Integer> result = new ArrayList<>();
    if (k <= 0) return result;
    PriorityQueue<Integer> winners = new PriorityQueue<>();
    for (int value : values) {
        winners.offer(value);
        if (winners.size() > k) winners.poll();
    }
    while (!winners.isEmpty()) result.add(winners.poll());
    return result; // Largest min(k, n) values in ascending removal order.
}
```

**Invariant:** After processing any prefix, the heap contains the largest `min(k, prefix_length)` items in that prefix.

**Complexity:** `O(n log(k + 1))` time and `O(min(n, k))` heap/output space for positive `k`; conventionally written `O(n log k)` for `k >= 2`. The `k <= 0` branch is constant time. Sorting all values costs `O(n log n)` time, often simpler if ordered output is also needed. Quickselect has expected `O(n)` time for a single kth statistic but is harder to implement robustly and does not naturally support a stream.

**Direction rule:**

- Keep `k` largest → min-heap of winners; discard the smallest winner.
- Keep `k` smallest → max-heap of winners; discard the largest winner.

For **Top K Frequent**, first build a frequency map. Heap entries can be `(frequency, value)`. Bucket sorting can achieve `O(n)` because frequencies lie from `1` to `n`, but a heap generalizes naturally and costs `O(m log k)` for `m` distinct values.

### 13.3 K-Way Merge

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** This reusable pattern appears in merging sorted lists/arrays, external data streams, and sorted matrix questions.

Keep only the next unconsumed candidate from each of `k` sorted sources. Pop the smallest candidate, output it, then push its successor from the same source.

```java
record MergeEntry(int value, int source, int index) {}

static List<Integer> mergeSortedArrays(int[][] arrays) {
    PriorityQueue<MergeEntry> frontier = new PriorityQueue<>(
            Comparator.comparingInt(MergeEntry::value)
                    .thenComparingInt(MergeEntry::source)
                    .thenComparingInt(MergeEntry::index));
    for (int source = 0; source < arrays.length; source++) {
        if (arrays[source].length > 0) {
            frontier.offer(new MergeEntry(arrays[source][0], source, 0));
        }
    }
    List<Integer> merged = new ArrayList<>();
    while (!frontier.isEmpty()) {
        MergeEntry entry = frontier.poll();
        merged.add(entry.value());
        int next = entry.index() + 1;
        if (next < arrays[entry.source()].length) {
            frontier.offer(new MergeEntry(arrays[entry.source()][next], entry.source(), next));
        }
    }
    return merged;
}
```

**Complexity:** For `N` total items across `k` source arrays, time is `O(k + N log(k + 1))`, including inspecting empty sources; heap space is `O(k)`, excluding `O(N)` output. With nonempty sources and `k >= 2`, the usual shorthand is `O(N log k)`.

**Recognition:** Multiple individually sorted inputs; need global sorted order or the kth global item; cannot concatenate and sort due to scale or streaming.

**Mistakes:** Inserting every item rather than one frontier per source, losing source identity, failing on empty sources, or forgetting to provide a comparator for record entries, or omitting a tie-breaker when a deterministic output order is required.

### 13.4 Two Heaps / Running Median

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority:** It is a classic streaming design and demonstrates balancing invariants, though it appears less often than ordinary top-K.

Maintain:

- `lower`: a max-heap for the smaller half;
- `upper`: a min-heap for the larger half;
- size difference at most one; and every `lower` value `<=` every `upper` value.

```java
static class MedianFinder {
    private final PriorityQueue<Integer> lower = new PriorityQueue<>(Comparator.reverseOrder());
    private final PriorityQueue<Integer> upper = new PriorityQueue<>();

    void add(int value) {
        lower.offer(value);
        upper.offer(lower.poll());
        if (upper.size() > lower.size()) lower.offer(upper.poll());
    }

    double median() {
        if (lower.isEmpty()) throw new IllegalStateException("median of empty stream");
        if (lower.size() > upper.size()) return lower.peek();
        long sum = (long) lower.peek() + upper.peek();
        return sum / 2.0;
    }
}
```

**Complexity:** `O(log n)` per insertion, `O(1)` median query, and `O(n)` storage.

**Edge cases/mistakes:** Querying before insertion, overflow while averaging two integers in fixed-width languages, forgetting a rebalance step, or maintaining sizes without maintaining cross-half order.

**Alternative:** Java has no standard multiset with rank queries. A `TreeMap<Integer, Integer>` can track counts, but maintaining the median requires extra bookkeeping. Two heaps are the clearer standard interview approach; sorting once is best when all data arrives before queries.

### Heap Pattern Map

| Problem clue | Heap design | Why |
|---|---|---|
| Kth largest / keep best `k` | Min-heap of size `k` | Root is weakest winner |
| Kth smallest / keep smallest `k` | Max-heap of size `k` | Root is weakest winner |
| Repeatedly take current minimum | Min-heap of all available choices | Efficient next choice |
| Merge `k` sorted sources | Min-heap with one frontier/source | Heap never exceeds `k` |
| Median of a stream | Max-heap lower half + min-heap upper half | Fast balanced middle |
| Schedule by earliest finish/time | Min-heap keyed by end/time | Releases resources in order |
| Dynamic shortest tentative distance | Min-heap `(distance, node)` | Extract nearest unsettled state |

### Representative Heap Problems

| Stage | Selected problem | Transfer target |
|---|---|---|
| Mechanics | Last Stone Weight | Max-heap direction and repeated extraction |
| Canonical | ⭐ **Canonical Interview Problem:** Kth Largest Element in an Array | A size-`k` min-heap retains the winners |
| Variation | Top K Frequent Elements; optionally K Closest Points | Hashing/priority-key design; compare buckets or sorting |
| Mixed pattern | Merge K Sorted Lists, then Meeting Rooms II | One frontier per source; release resources by earliest end |
| Cold revisit | Kth Largest with duplicate/extreme values, then K-Way Merge | Explain the root invariant before writing the comparator |

Later choices: **Find Median from Data Stream** (Tier 3), **Kth Smallest in a Sorted Matrix** (heap versus answer search), **Smallest Range Covering Elements from K Lists** (maintain current maximum). **Sliding Window Median** and **IPO** are optional advanced mixed practice; do not block core progress on them.

### Common Heap Mistakes and Interview Tips

- Say what the root means and what the heap invariant preserves.
- Include heap size in the complexity: `O(n log k)`, not simply `O(n log n)`.
- Clarify whether output itself must be sorted; a heap's internal array is not sorted.
- Prefer immutable record/class entries and explicit comparator fields. Add a stable integer ID as a tie-breaker if ties must have deterministic order.
- When graph code pushes duplicate entries instead of decreasing keys, skip a popped entry if it is stale.
- If `k` is close to `n` and ordered output is needed, sorting may be clearer with similar practical cost.

### Heap Mastery Checklist

I have mastered interview heaps when I can:

- [ ] Explain the heap invariant and why the entire array is not sorted.
- [ ] Declare Java min-heaps and max-heaps correctly from memory.
- [ ] State `peek`, `offer`, `poll`, arbitrary-removal, and heap-construction complexities.
- [ ] Choose the correct heap direction for top-K.
- [ ] Derive and implement `O(n log k)` top-K and `O(N log k)` k-way merge.
- [ ] Design safe record/class comparator keys and tie-breakers.
- [ ] **Later (Tier 3):** Implement and explain the two-heaps median invariant.
- [ ] Compare heap, full sorting, bucket sorting, and quickselect trade-offs.

---

## 14. Graphs

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Graph reasoning appears directly in network/dependency questions and indirectly in grids, word transformations, scheduling, and state-space search. BFS and DFS are must-master patterns; several Tier 2 algorithms build directly on them.

### Topic Overview

- **What it is:** A graph is a set of vertices (nodes) connected by edges. Edges may be directed or undirected, weighted or unweighted, and the graph may be cyclic or disconnected.
- **Why it exists:** Graphs model arbitrary relationships that are not limited to a linear order or parent-child hierarchy.
- **Why it matters in interviews:** They test modeling, traversal, visited-state management, and choosing an algorithm from edge semantics. Many interview problems hide a graph behind a grid, set of transformations, or prerequisites.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Hash maps/sets, queues, stacks/recursion, trees, heaps for weighted paths, and Big-O.
- **Common use cases:** Social connections, computer networks, routes, dependency systems, course prerequisites, account merging, grids, and state transitions.
- **Common problem patterns:** Reachability, connected components, cycle detection, shortest path, dependency ordering, connectivity under merges, coloring, and region traversal.
- **How to recognize problems that require it:** The question describes entities plus relationships or legal transitions. Ask: “Can each state be a node and each allowed move/relationship be an edge?”
- **How deeply to understand it:** Deep mastery of representations, BFS/DFS, components, grid traversal, and unweighted shortest paths. Strong working knowledge of cycle detection, topological sort, DSU, bipartite checking, and Dijkstra. Conceptual/basic implementation knowledge of MST; specialized graph algorithms can wait.

### Focus First

- Model the nodes, edges, direction, weights, and state identity correctly.
- Adjacency-list representation.
- DFS/BFS with a `boolean[]` or `HashSet` for visited state and correct visited timing.
- Connected components and grid-as-graph traversal.
- BFS for shortest paths in unweighted graphs.

### Learn Later

- Directed/undirected cycle detection, topological sorting, Union-Find, multi-source BFS, bipartite coloring, and Dijkstra.
- Minimum spanning trees after shortest paths and DSU are comfortable.

### Optional / Specialized

- **Floyd-Warshall and Bellman-Ford — 🟡 Tier 3 — Nice to Know.** Know what problem each solves; implement only if the target role/company favors graph-heavy questions.
- **Strongly connected components, bridges, and articulation points — ⚪ Tier 4 — Low Priority / Specialized.** Valuable for advanced algorithmic interviews but uncommon in general SWE rounds.
- **Max flow/min cut, matching, Euler tours, and LCA preprocessing — ⚪ Tier 4 — Low Priority / Specialized.** Study only for role-specific or competitive-programming preparation.

### 14.1 Graph Representations

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** A correct algorithm on the wrong representation can exceed the constraints or silently model edge direction incorrectly.

**Java contracts:** Graph snippets use a nonnegative vertex count and IDs `0..V-1`; each listed neighbor, source, and target must be valid. A source-based query therefore requires a nonempty graph. Whole-graph operations support zero vertices. Adjacency outer lists use `ArrayList`, giving constant-time `get(node)`; arbitrary labels can be mapped to integer IDs.

**Study target:** Reconstruct DFS/BFS and component loops first (4–6 problems); then add topological order, DSU, and Dijkstra (3–5 problems). Understand modeling and visited state; look up advanced graph algorithms when needed.

#### Adjacency list

```java
record WeightedEdge(int to, long weight) {}
record WeightedArc(int from, int to, long weight) {}

static List<List<Integer>> emptyGraph(int n) {
    List<List<Integer>> graph = new ArrayList<>(n);
    for (int node = 0; node < n; node++) graph.add(new ArrayList<>());
    return graph;
}

static List<List<Integer>> buildUndirectedGraph(int n, int[][] edges) {
    List<List<Integer>> graph = emptyGraph(n);
    for (int[] edge : edges) {
        graph.get(edge[0]).add(edge[1]);
        graph.get(edge[1]).add(edge[0]);
    }
    return graph;
}

static List<List<WeightedEdge>> buildDirectedWeightedGraph(int n, List<WeightedArc> edges) {
    List<List<WeightedEdge>> graph = new ArrayList<>(n);
    for (int node = 0; node < n; node++) graph.add(new ArrayList<>());
    for (WeightedArc edge : edges) {
        graph.get(edge.from()).add(new WeightedEdge(edge.to(), edge.weight()));
    }
    return graph;
}
```

- Space: `O(V + E)`.
- Iterating all neighbors during a full traversal: `O(V + E)`.
- Best default for sparse interview graphs.

#### Adjacency matrix

An `V x V` matrix stores whether or what edge connects each pair.

- Space: `O(V^2)`.
- Test a particular edge: `O(1)`.
- Iterate one node's potential neighbors: `O(V)`.
- Useful for dense/small graphs, direct edge queries, or matrix-based DP; usually wasteful for sparse input.

#### Edge list

An `int[][]` of unweighted endpoints or a `List` of weighted-edge records is compact and ideal for algorithms that process edges globally, such as Kruskal and Bellman-Ford. It is poor for repeatedly asking for one vertex's neighbors unless converted.

**Recognition and modeling questions:**

1. Are edges directed?
2. Are weights present, and can they be negative?
3. Can parallel edges or self-loops occur?
4. Are node labels contiguous integers or arbitrary strings?
5. Can the graph be disconnected?
6. Does the state need more than a location, such as `(row, col, keys_mask)`?

**Common mistakes:** Adding only one direction for an undirected edge; accidentally adding a reverse directed edge; allocating based on edge count instead of node count; omitting isolated nodes; confusing an edge list with an adjacency list; and collapsing distinct states that share a location.

### 14.2 Graph DFS

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** DFS is the standard reachability/component/substructure tool and is the foundation for cycle and ordering algorithms.

#### Intuition and how it works

DFS follows one path as far as possible, then backtracks. A visited array (or a `HashSet` for non-contiguous labels) means each graph state is processed at most once and prevents infinite loops in cyclic graphs.

```java
static boolean[] dfsRecursive(List<List<Integer>> graph, int start) {
    boolean[] visited = new boolean[graph.size()];
    visitRecursive(graph, start, visited);
    return visited;
}

static void visitRecursive(List<List<Integer>> graph, int node, boolean[] visited) {
    visited[node] = true;
    for (int neighbor : graph.get(node)) {
        if (!visited[neighbor]) visitRecursive(graph, neighbor, visited);
    }
}

static boolean[] dfsIterative(List<List<Integer>> graph, int start) {
    boolean[] visited = new boolean[graph.size()];
    visitIterative(graph, start, visited);
    return visited;
}

static void visitIterative(List<List<Integer>> graph, int start, boolean[] visited) {
    Deque<Integer> pending = new ArrayDeque<>();
    visited[start] = true;
    pending.push(start);
    while (!pending.isEmpty()) {
        int node = pending.pop();
        for (int neighbor : graph.get(node)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                pending.push(neighbor);
            }
        }
    }
}
```

**When to use / recognition:** Reachability, components, exploring a region, detecting structural properties, exhaustive graph paths, and when shortest distance is not the requirement.

**Complexity:** With an adjacency list, `O(V + E)` time and `O(V)` visited/stack space. For a directed graph, each edge is considered once; for an undirected adjacency list, each appears twice but is still `O(E)`.

**Visited timing:** Mark when discovered/pushed, not later when popped, unless the algorithm deliberately permits duplicate work. This prevents a node from being scheduled repeatedly.

**Alternatives/trade-offs:** Recursive DFS is concise but can overflow on deep graphs. Iterative DFS controls memory explicitly. BFS finds minimum edge count; DFS generally does not.

**Mistakes/edge cases:** Forgetting visited; marking the wrong composite state; traversing only from node `0` when the graph may be disconnected; recursion depth; and modifying the adjacency list during traversal.

### 14.3 Graph BFS and Unweighted Shortest Paths

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** BFS is the correct and common answer for shortest path measured in number of edges or equal-cost moves.

#### Intuition and invariant

BFS explores a frontier in nondecreasing distance from the source. When a node is first discovered in an unweighted graph, no later path can reach it with fewer edges.

```java
record PathResult(int distance, List<Integer> path) {}

static PathResult shortestUnweightedPath(List<List<Integer>> graph, int start, int target) {
    int[] distance = new int[graph.size()];
    int[] parent = new int[graph.size()];
    Arrays.fill(distance, -1);
    Arrays.fill(parent, -1);
    Queue<Integer> pending = new ArrayDeque<>();
    distance[start] = 0;
    pending.offer(start);
    while (!pending.isEmpty()) {
        int node = pending.poll();
        if (node == target) {
            List<Integer> path = new ArrayList<>();
            for (int current = target; current != -1; current = parent[current]) {
                path.add(current);
            }
            Collections.reverse(path);
            return new PathResult(distance[target], path);
        }
        for (int neighbor : graph.get(node)) {
            if (distance[neighbor] == -1) {
                distance[neighbor] = distance[node] + 1;
                parent[neighbor] = node;
                pending.offer(neighbor);
            }
        }
    }
    return new PathResult(-1, List.of());
}
```

**When to use / recognition:** Fewest moves, minimum number of transformations, closest target, shortest unweighted route, or all edges have identical cost.

**Complexity:** `O(V + E)` time and `O(V)` space.

**Why discovery-time marking matters:** If two frontier nodes can reach the same neighbor, marking only on dequeue may enqueue that neighbor multiple times and obscure which parent gives the shortest path.

**Trade-offs:** DFS can find *a* path but not generally a shortest one. Dijkstra generalizes BFS to nonnegative unequal weights. Bidirectional BFS can reduce explored states when start and target are known and reverse transitions are easy.

**Common mistakes:** Using DFS for shortest unweighted paths; marking after dequeue; counting nodes when the question counts edges; stopping when a target is generated without ensuring the algorithm's discovery guarantee; and storing full paths in every queue item instead of parent links.

### 14.4 Multi-Source BFS

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Many grid and nearest-source problems become simple once all valid sources enter the queue at distance zero.

Initialize the queue with every source simultaneously. Conceptually, add a virtual super-source connected by zero setup cost to all sources. BFS then computes distance to the nearest source.

```java
static int[] nearestSourceDistance(List<List<Integer>> graph, int[] sources) {
    int[] distance = new int[graph.size()];
    Arrays.fill(distance, -1);
    Queue<Integer> pending = new ArrayDeque<>();
    for (int source : sources) {
        if (distance[source] == -1) {
            distance[source] = 0;
            pending.offer(source);
        }
    }
    while (!pending.isEmpty()) {
        int node = pending.poll();
        for (int neighbor : graph.get(node)) {
            if (distance[neighbor] == -1) {
                distance[neighbor] = distance[node] + 1;
                pending.offer(neighbor);
            }
        }
    }
    return distance;
}
```

**Recognition:** “Distance to nearest gate/zero/hospital,” simultaneous spread/infection/fire, or minimum time until every reachable cell changes.

**Complexity:** `O(V + E + S)` for a source list of length `S`, including duplicate entries. With distinct sources, `S <= V`, giving the usual `O(V + E)` bound. Traversal is not multiplied by the number of sources; auxiliary distance/queue space is `O(V)`.

**Mistakes:** Running a separate BFS from every cell/source, failing to enqueue all initial sources before traversal, and confusing simultaneous time steps with sequential source processing.

### 14.5 Connected Components

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Component counting is a core reuse of traversal and appears in graphs, grids, accounts, and connectivity stories.

```java
static int countComponents(int n, int[][] edges) {
    List<List<Integer>> graph = buildUndirectedGraph(n, edges);
    boolean[] visited = new boolean[n];
    int components = 0;
    for (int node = 0; node < n; node++) {
        if (!visited[node]) {
            components++;
            visitIterative(graph, node, visited); // Helper from 14.2.
        }
    }
    return components;
}
```

**Invariant:** Each outer-loop traversal consumes exactly one previously unseen component.

**Complexity:** `O(V + E)` time and `O(V + E)` total representation plus `O(V)` traversal state.

**Alternatives:** DFS/BFS is best when adjacency already exists or component contents matter. DSU is strong when processing connection operations and only connectivity/group counts matter.

**Mistakes:** Starting just once; not including isolated vertices; incrementing per node rather than per new traversal; and using directed reachability as though it were undirected connectivity.

### 14.6 Grid as a Graph

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Grid problems are among the most common disguises of BFS/DFS, and they demand careful boundary and mutation reasoning.

Each traversable cell is a node. Legal moves define edges—usually four directions, sometimes eight or problem-specific moves.

```java
static int countIslands(char[][] grid) {
    if (grid.length == 0 || grid[0].length == 0) return 0;
    int rows = grid.length;
    int cols = grid[0].length;
    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    Deque<int[]> pending = new ArrayDeque<>();
    int islands = 0;
    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            if (grid[row][col] != '1') continue;
            islands++;
            grid[row][col] = '0'; // Explicitly mutates the caller's grid.
            pending.push(new int[] {row, col});
            while (!pending.isEmpty()) {
                int[] cell = pending.pop();
                for (int[] direction : directions) {
                    int nextRow = cell[0] + direction[0];
                    int nextCol = cell[1] + direction[1];
                    if (nextRow >= 0 && nextRow < rows && nextCol >= 0 && nextCol < cols
                            && grid[nextRow][nextCol] == '1') {
                        grid[nextRow][nextCol] = '0';
                        pending.push(new int[] {nextRow, nextCol});
                    }
                }
            }
        }
    }
    return islands;
}
```

**Complexity:** For an `R x C` grid, `O(RC)` time and up to `O(RC)` DFS/BFS state. Each cell and constant number of neighbor edges is considered at most once.

**Recognition:** Regions, islands, connected pixels, spreading, nearest cell, shortest moves, maze, or board-state transitions.

> 🌐 **Java Backend Relevance — MEDIUM:** `int[] {row, col}` is compact interview state; a `record Coordinate(int row, int col)` names fields clearly in an application API. Arrays use identity equality, so a record is also a safer value key in a `HashSet` when state needs content equality.

**State modeling:** Sometimes `(r, c)` is insufficient. If future moves depend on collected keys, remaining obstacle eliminations, direction, or time parity, those fields belong in the visited state.

**Mutation trade-off:** Marking the grid itself saves separate storage but destroys input. Ask whether mutation is permitted. A separate `boolean[][] visited` preserves input at `O(RC)` extra space.

**Common mistakes/edge cases:** Swapping rows and columns; assuming rectangular input without checking the contract; wrong four-versus-eight-direction rule; marking on pop; revisiting start; empty grid; and treating a changed resource count as the same state.

### 14.7 Cycle Detection

#### Undirected graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is a standard extension of DFS/BFS and directly tests whether the candidate distinguishes a parent edge from a real back edge.

During DFS, encountering an already visited neighbor indicates a cycle **unless that neighbor is the node we just came from**.

```java
static boolean hasUndirectedCycle(List<List<Integer>> graph) {
    boolean[] visited = new boolean[graph.size()];
    for (int node = 0; node < graph.size(); node++) {
        if (!visited[node] && undirectedCycleDfs(graph, node, -1, visited)) return true;
    }
    return false;
}

static boolean undirectedCycleDfs(List<List<Integer>> graph, int node, int parent,
                                  boolean[] visited) {
    visited[node] = true;
    for (int neighbor : graph.get(node)) {
        if (!visited[neighbor]) {
            if (undirectedCycleDfs(graph, neighbor, node, visited)) return true;
        } else if (neighbor != parent) {
            return true;
        }
    }
    return false;
}
```

**Complexity:** `O(V + E)` time and `O(V)` traversal space.

**Contract:** The parent-based DFS shown assumes a simple undirected graph. If parallel edges must count as a two-edge cycle, track edge IDs so only the exact reverse parent edge is skipped, or process the edge list with DSU.

**Alternative:** DSU detects whether an undirected edge connects vertices already in the same set. DFS is better if adjacency and the actual cycle/path matter.

**Mistakes:** Treating the parent edge as a cycle, missing disconnected components, and assuming the simple parent rule handles all parallel-edge conventions without clarification.

#### Directed graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Directed cycle detection underlies prerequisite validation and topological sorting.

A visited node is not automatically a cycle. A cycle exists when DFS reaches a node that is still in the **current recursion path**. Use three states: `0 = unvisited`, `1 = visiting`, `2 = finished`.

```java
static boolean hasDirectedCycle(List<List<Integer>> graph) {
    int[] state = new int[graph.size()];
    for (int node = 0; node < graph.size(); node++) {
        if (state[node] == 0 && directedCycleDfs(graph, node, state)) return true;
    }
    return false;
}

static boolean directedCycleDfs(List<List<Integer>> graph, int node, int[] state) {
    if (state[node] == 1) return true;
    if (state[node] == 2) return false;
    state[node] = 1;
    for (int neighbor : graph.get(node)) {
        if (directedCycleDfs(graph, neighbor, state)) return true;
    }
    state[node] = 2;
    return false;
}
```

**Complexity:** `O(V + E)` time and `O(V)` state/recursion.

**Alternative:** Kahn's topological-sort algorithm detects a directed cycle if fewer than `V` nodes can be processed.

**Mistakes:** Using only one visited boolean; forgetting to mark a node finished; carrying a recursion-path `HashSet` between independent completed paths; and reversing prerequisite edge direction accidentally.

### 14.8 Topological Sorting

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Dependency scheduling is a common interview story. A candidate should recognize that a valid linear order exists only for a directed acyclic graph (DAG).

#### Kahn's algorithm: indegree BFS

`indegree[v]` counts unresolved prerequisites entering `v`. Start with every zero-indegree node, remove it, and decrement the indegree of its dependents.

```java
static List<Integer> topologicalOrder(int n, int[][] edges) {
    List<List<Integer>> graph = emptyGraph(n); // Helper from 14.1.
    int[] indegree = new int[n];
    for (int[] edge : edges) {
        int prerequisite = edge[0];
        int course = edge[1];
        graph.get(prerequisite).add(course);
        indegree[course]++;
    }
    Queue<Integer> ready = new ArrayDeque<>();
    for (int node = 0; node < n; node++) {
        if (indegree[node] == 0) ready.offer(node);
    }
    List<Integer> order = new ArrayList<>();
    while (!ready.isEmpty()) {
        int node = ready.poll();
        order.add(node);
        for (int neighbor : graph.get(node)) {
            if (--indegree[neighbor] == 0) ready.offer(neighbor);
        }
    }
    return order.size() == n ? order : List.of();
}
```

**Invariant:** Every emitted node has no remaining incoming edge from an unprocessed node.

**Complexity:** `O(V + E)` time and `O(V + E)` space including adjacency.

**Recognition:** Prerequisites, build order, dependencies, alien alphabet constraints, or ordering tasks while respecting “before” relationships.

**DFS alternative:** Append each node after exploring all outgoing edges (postorder), then reverse the result; use visiting/finished states to reject cycles. Kahn is often easier when cycle detection and indegree-based availability are central.

**Trade-offs:** Multiple valid orders may exist. A normal queue returns any valid order; a min-heap returns the lexicographically smallest available order at `O((V + E) log V)` rather than linear time.

**Common mistakes/edge cases:** Reversing edge direction; omitting nodes with no edges; not verifying `order.size() == V`; decrementing indegree more than once; assuming uniqueness; self-loop; and duplicate constraints that inflate indegrees unless consistently represented.

<a id="149-union-find-disjoint-set-union-dsu"></a>
### 14.9 Union-Find / Disjoint Set Union (DSU)

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** DSU is the cleanest structure for incremental undirected connectivity, redundant edges, account grouping, and Kruskal. It is frequent enough to implement confidently after BFS/DFS.

#### Intuition and how it works

Each component has a representative root. `find(x)` returns it. `unite(a, b)` merges two components. Path compression flattens find paths; union by size/rank attaches the smaller tree below the larger.

```java
static class DSU {
    private final int[] parent;
    private final int[] size;
    private int components;

    DSU(int n) {
        parent = new int[n];
        size = new int[n];
        Arrays.fill(size, 1);
        for (int node = 0; node < n; node++) parent[node] = node;
        components = n;
    }

    int find(int node) {
        while (parent[node] != node) {
            parent[node] = parent[parent[node]]; // Path halving.
            node = parent[node];
        }
        return node;
    }

    boolean unite(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int temporary = rootA;
            rootA = rootB;
            rootB = temporary;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        components--;
        return true;
    }

    int components() {
        return components;
    }
}
```

**Complexity:** Construction takes `O(V)` time and space. With both optimizations, subsequent operations take `O(alpha(V))` amortized time each, where inverse Ackermann `alpha` grows so slowly it is below `5` for practical inputs.

**Recognition:** Repeatedly add undirected connections; ask whether two items belong to the same group; count groups; detect a redundant edge; merge accounts sharing identifiers; or choose non-cycling edges for an MST.

**Invariant:** `parent[root] == root`; sizes/ranks are meaningful at roots; two nodes are connected exactly when their roots match.

**Alternatives/trade-offs:** DFS/BFS handles static connectivity and can list paths/component members directly. DSU answers merge/connectivity efficiently but does not support general edge deletion or reveal an actual route.

**Common mistakes/edge cases:** Comparing immediate parents instead of roots; updating size on the child root; forgetting path compression's assignment; decrementing component count for a no-op union; arbitrary string labels without mapping; and trying to use ordinary DSU for directed reachability.

### 14.10 Bipartite Graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Two-coloring is a standard constraint pattern and a useful cycle-property test; it appears moderately often.

A graph is bipartite if its vertices can be colored with two colors so every edge connects opposite colors. Equivalently, an undirected graph is bipartite iff it has no odd-length cycle.

```java
static boolean isBipartite(List<List<Integer>> graph) {
    int[] color = new int[graph.size()];
    Arrays.fill(color, -1);
    Queue<Integer> pending = new ArrayDeque<>();
    for (int start = 0; start < graph.size(); start++) {
        if (color[start] != -1) continue;
        color[start] = 0;
        pending.offer(start);
        while (!pending.isEmpty()) {
            int node = pending.poll();
            for (int neighbor : graph.get(node)) {
                if (color[neighbor] == -1) {
                    color[neighbor] = 1 - color[node];
                    pending.offer(neighbor);
                } else if (color[neighbor] == color[node]) {
                    return false;
                }
            }
        }
    }
    return true;
}
```

**Complexity:** `O(V + E)` time and `O(V)` state.

**Recognition:** Divide people/items into two incompatible groups, alternate labels along connections, possible odd cycle, or “no two connected nodes in the same group.”

**Mistakes:** Traversing only one component; using an uninitialized color as a real color; forgetting self-loops immediately violate bipartiteness; and confusing general graph coloring with the special two-color case.

### 14.11 Shortest-Path Algorithm Selection

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Choosing BFS, 0–1 BFS, Dijkstra, or a negative-edge algorithm from the weight model prevents one of the most common graph-category errors.

| Edge costs / graph property | First algorithm | Typical time |
|---|---|---:|
| All edges equal / unweighted | BFS | `O(V + E)` |
| Weights are only `0` or `1` | 0-1 BFS with `ArrayDeque` | `O(V + E)` |
| Nonnegative weights | Dijkstra | `O(V + E log(E + 1))` with duplicate heap entries; commonly `O((V + E) log V)` for simple graphs |
| Negative edges, no reachable negative cycle | Bellman-Ford | `O(VE)` |
| All-pairs, small/dense graph | Floyd-Warshall | `O(V^3)` |
| DAG with any edge weights | Topological relaxation | `O(V + E)` |

Do not choose from the word *shortest* alone. Inspect the edge-cost model first.

### 14.12 Dijkstra's Algorithm

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is the standard nonnegative-weight shortest-path algorithm and a common heap/graph interview topic. Negative-weight variants are much less common.

#### Intuition and invariant

Maintain the best tentative distance known for every node. Repeatedly pop the smallest distance from a min-heap. With nonnegative edges, that popped non-stale distance cannot later be improved by traveling through a farther node.

```java
record DistanceState(long distance, int node) {}

static long[] dijkstra(List<List<WeightedEdge>> graph, int source) {
    final long infinity = Long.MAX_VALUE;
    for (List<WeightedEdge> edges : graph) {
        for (WeightedEdge edge : edges) {
            if (edge.weight() < 0) {
                throw new IllegalArgumentException("Dijkstra requires nonnegative weights");
            }
        }
    }
    long[] distance = new long[graph.size()];
    Arrays.fill(distance, infinity);
    PriorityQueue<DistanceState> frontier = new PriorityQueue<>(
            Comparator.comparingLong(DistanceState::distance));
    distance[source] = 0;
    frontier.offer(new DistanceState(0, source));
    while (!frontier.isEmpty()) {
        DistanceState current = frontier.poll();
        long dist = current.distance();
        int node = current.node();
        if (dist != distance[node]) continue; // Stale entry.
        for (WeightedEdge edge : graph.get(node)) {
            if (dist > infinity - edge.weight()) continue;
            long candidate = dist + edge.weight();
            if (candidate < distance[edge.to()]) {
                distance[edge.to()] = candidate;
                frontier.offer(new DistanceState(candidate, edge.to()));
            }
        }
    }
    return distance;
}
```

**Relaxation:** For edge `u -> v` with weight `w`, if `dist[u] + w < dist[v]`, update `dist[v]` and record `u` as parent if a path must be reconstructed.

**Complexity:** `O(V + E log(E + 1))` time including distance initialization and input-edge validation. There are at most `O(E)` successful relaxations/heap entries, so auxiliary space is `O(V + E)` for distances and heap, excluding the graph. On simple graphs the familiar looser bound is `O((V + E) log V)`.

**Numeric contract:** All weights are nonnegative, and every finite shortest distance must be less than `Long.MAX_VALUE`, reserved for unreachable nodes. The guarded addition skips overflowing candidates. If the problem permits larger answers, this representation needs to change; `int` is not an acceptable shortcut.

**Recognition:** Minimum total travel time/cost/risk where every transition cost is nonnegative and costs differ.

**Early exit:** When a target is popped with its current non-stale best distance, its distance is final, so a single-target search may stop.

**Why negative edges break it:** A node considered final could later be improved through a negative edge from a currently farther node, invalidating the greedy pop invariant.

**Alternatives/trade-offs:** BFS is simpler/faster for equal weights. Bellman-Ford allows negative weights at much higher cost. A* can explore fewer nodes with a valid heuristic but is more specialized.

**Common mistakes/edge cases:** Using Dijkstra with negative weights; marking visited on push (a better path may arrive before pop); not skipping stale entries; reversing `(neighbor, weight)`; integer overflow; disconnected nodes remaining infinity; and confusing shortest path with MST.

### 14.13 Minimum Spanning Trees

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority:** MST is useful in connection-cost problems and appears at algorithm-focused companies, but much less often than traversal, topological sort, or Dijkstra. Understand and implement one standard algorithm after higher-priority graph skills.

An MST connects all vertices in a connected, weighted, undirected graph with minimum total edge weight and exactly `V - 1` selected edges. It minimizes the **total network cost**; it does not guarantee the shortest route between every pair.

#### Kruskal's algorithm

Sort edges by weight. Add an edge if DSU says it connects two different components.

```java
record MstEdge(long weight, int u, int v) {}

static OptionalLong kruskalMst(int n, List<MstEdge> edges) {
    if (n <= 1) return OptionalLong.of(0);
    List<MstEdge> sorted = new ArrayList<>(edges);
    sorted.sort(Comparator.comparingLong(MstEdge::weight));
    DSU dsu = new DSU(n); // Definition in 14.9.
    long total = 0;
    int used = 0;
    for (MstEdge edge : sorted) {
        if (dsu.unite(edge.u(), edge.v())) {
            total = Math.addExact(total, edge.weight());
            if (++used == n - 1) break;
        }
    }
    return used == n - 1 ? OptionalLong.of(total) : OptionalLong.empty();
}
```

**Complexity:** `O(V + E log(E + 1))` time including DSU initialization; `O(V + E)` auxiliary space for DSU, the copied list of references, and object-list sorting. Immutable `MstEdge` objects can be shared with the caller safely. Every running total of selected edge weights must fit `long`; `Math.addExact` reports intermediate overflow. With signed weights, a final total that fits does not guarantee that every partial total fits.

#### Prim's algorithm

Start from one node and repeatedly use a min-heap to choose the cheapest edge crossing from the built tree to an unvisited vertex. With an adjacency list and a lazy binary heap: `O(V + E log(E + 1))` time and `O(V + E)` space; for a connected simple graph, the common time shorthand is `O(E log V)`.

**Recognition:** Connect all locations/devices with minimum total installation cost; allowed pairwise connection prices; output `V - 1` links.

**Trade-offs:** Kruskal is natural with an edge list and sparse graph; Prim is natural with adjacency and growing one component. If the graph is disconnected, the result is a minimum spanning forest unless the problem demands failure.

**Common mistakes:** Applying MST to a directed graph without a specialized formulation; confusing it with shortest paths; not checking connectivity; adding cycles; and assuming the MST is unique when weights tie.

### 14.14 Advanced Shortest Paths and Graph Algorithms

- **0-1 BFS — 🟡 Tier 3 — Nice to Know.** For edge weights exactly `0` or `1`, push zero-cost transitions to the front and one-cost transitions to the back of a `ArrayDeque`. It runs in `O(V + E)` and is worth recognizing.
- **Bellman-Ford — 🟡 Tier 3 — Nice to Know.** Repeatedly relax every edge `V - 1` times; one more improvement reveals a reachable negative cycle. `O(VE)` time, `O(V)` space. Know when Dijkstra is invalid.
- **Floyd-Warshall — 🟡 Tier 3 — Nice to Know.** Dynamic programming over allowed intermediate vertices for all-pairs paths. `O(V^3)` time and `O(V^2)` space; practical only for small graphs.
- **Strongly connected components — ⚪ Tier 4 — Low Priority / Specialized.** Kosaraju/Tarjan condense a directed graph into mutually reachable groups; uncommon in standard SWE interviews.
- **Bridges/articulation points — ⚪ Tier 4 — Low Priority / Specialized.** Low-link DFS identifies single points/edges of failure; more likely in advanced graph rounds.
- **Maximum flow/matching — ⚪ Tier 4 — Low Priority / Specialized.** Powerful but too specialized for a general interview roadmap.

### Graph Pattern Map

| Problem clue | Likely approach | Critical question/mistake |
|---|---|---|
| Can A reach B? | DFS or BFS | What exactly is one state? |
| Number of groups/regions | Outer loop + DFS/BFS | Include isolated nodes/cells |
| Fewest equal-cost moves | BFS | Mark visited on discovery |
| Nearest among many sources | Multi-source BFS | Enqueue every source at distance 0 |
| Weighted cheapest path, costs nonnegative | Dijkstra | Skip stale heap entries |
| Prerequisites / valid order | Topological sort | Edge direction and cycle check |
| Incremental undirected connectivity | DSU | Compare roots, not parents |
| Divide into two conflicting groups | Bipartite BFS/DFS coloring | Traverse every component |
| Redundant undirected edge | DSU or cycle DFS | A failed union reveals cycle |
| Minimum cost to connect everything | MST | Not the same as shortest paths |
| Grid regions | DFS/BFS | Bounds, directions, state mutation |

### Representative Graph Problems

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Reachability/components | Find if Path Exists; Flood Fill | ⭐ **Canonical Interview Problem:** Number of Islands | Connected Components; Graph Valid Tree | Clone Graph (identity map); Pacific Atlantic (reverse search) | Islands using the other traversal and a clear mutation contract |
| Shortest equal-cost moves | Shortest Path in Binary Matrix | ⭐ **Canonical Interview Problem:** Rotting Oranges | Walls and Gates / Word Ladder | Shortest Path to Get All Keys (later composite state) | Multi-source BFS with all sources enqueued first |
| Dependencies/connectivity | Course Schedule | ⭐ **Canonical Interview Problem:** Course Schedule II | Is Graph Bipartite?; Redundant Connection (DSU) | Alien Dictionary (later invalid-prefix/duplicate-edge rules) | Reconstruct Kahn + DSU, then solve one unlabelled problem |
| Nonnegative weighted paths | Trace a three-node weighted graph | ⭐ **Canonical Interview Problem:** Network Delay Time | Path With Minimum Effort | Cheapest Flights Within K Stops (state/algorithm changes) | Dijkstra with stale entries and one unreachable node |

Select one variation per family initially. **Min Cost to Connect All Points** is a later Tier 3 MST exercise. For Word Ladder, include neighbor-generation cost in complexity; avoid building every possible pairwise edge blindly.

### Common Graph Mistakes and Interview Tips

- Before choosing an algorithm, say: nodes, edges, direction, weight rules, and state definition.
- Build a tiny example with a cycle and a disconnected node.
- Mark discovered states at the right moment; ordinary BFS/DFS typically marks on enqueue/push.
- Do not say all traversals are `O(V^2)`; with adjacency lists they are `O(V + E)`.
- Count the graph representation in space if you construct it from an edge list.
- Separate **path existence**, **shortest path**, **minimum total network**, and **dependency order**; they are different goals.
- If mutation/resource affects future moves, make it part of state rather than using only location as visited.
- Ask whether negative weights exist before proposing Dijkstra.
- For recursion on an arbitrary graph, mention stack-depth risks and offer iterative traversal.

### Graph Mastery Checklist

I have mastered interview graphs when I can:

- [ ] Convert a relationship story, grid, or state-transition problem into nodes and edges.
- [ ] Choose and build an adjacency list with correct direction and weights.
- [ ] Implement recursive/iterative DFS and queue-based BFS from scratch.
- [ ] Explain visited timing and derive `O(V + E)` traversal complexity.
- [ ] Solve components, grid regions, reachability, and unweighted shortest paths.
- [ ] Implement multi-source BFS and reconstruct a shortest path with parents.
- [ ] Detect cycles in undirected and directed graphs and explain why the states differ.
- [ ] Produce a topological order with Kahn's algorithm and reject cycles.
- [ ] Implement DSU with path compression and union by size/rank.
- [ ] Check bipartiteness across every component.
- [ ] Select BFS versus Dijkstra from edge costs and implement Dijkstra correctly.
- [ ] Recognize MST versus shortest path; **later (Tier 3)** implement basic Kruskal or Prim.
- [ ] Identify when composite state is necessary.

---

<a id="15-recursion-backtracking"></a>
## 15. Recursion & Backtracking

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Recursion is essential for trees and DFS, while backtracking is the standard tool for generating combinations and searching constrained choice spaces. Backtracking is frequent but usually appears in fewer rounds than arrays, hashing, trees, and graph traversal.

### Topic Overview

- **What it is:** Recursion solves a problem through smaller instances of itself. Backtracking performs depth-first search over choices, building a partial candidate and undoing a choice when returning.
- **Why it exists:** It expresses hierarchical and combinatorial search naturally and avoids writing deeply nested loops for an unknown number of decisions.
- **Why it matters in interviews:** It tests base cases, state management, search-tree reasoning, complexity bounds, and pruning.
- **Interview priority:** 🟠 Tier 2 — Very Important. Recursion fundamentals themselves are 🔴 Tier 1 because trees/graphs depend on them; combinatorial backtracking is Tier 2.
- **Prerequisites:** Function call stack, arrays/sets, Big-O, DFS, and careful mutation.
- **Common use cases:** Tree traversal, subsets, permutations, combinations, board search, constraint satisfaction, and path enumeration.
- **Common problem patterns:** Choose/skip; choose one unused item; fill one position; split a string; search a board; assign values while constraints hold.
- **How to recognize problems that require it:** The problem asks for all arrangements/selections/partitions/paths, or asks whether any assignment satisfies constraints, and the constraints are small enough for exponential search.
- **How deeply to understand it:** Deeply understand recursive contracts, call-stack space, choice-tree modeling, append/recurse/pop, duplicate control, and safe pruning. Recognize exponential lower bounds and avoid pretending pruning changes worst-case complexity without proof.

### Focus First

- Base case, recursive progress, and a precise function contract.
- Drawing a recursion tree for a tiny input.
- The backtracking skeleton: **choose → explore → unchoose**.
- Subsets, permutations, combinations, and duplicate handling.
- Separating path state from answer snapshots.

### Learn Later

- Constraint-specific pruning, board search, palindrome partitioning, and memoizing failed states.
- Bit masks as a compact replacement for a used-element array after ordinary state is clear.

### Optional / Specialized

- **Exact cover / Dancing Links — ⚪ Tier 4 — Low Priority / Specialized.** Elegant for certain constraint systems but not useful enough for normal SWE interviews.
- **Highly optimized branch-and-bound — ⚪ Tier 4 — Low Priority / Specialized.** Role- and problem-specific; first master clear pruning and state design.

**Study target:** Remember choose–explore–unchoose and snapshot syntax; understand state, reuse, duplicate rules, and pruning proofs. Solve one mechanics exercise, subsets, combinations, permutations, and two chosen variations; revisit cold. Board-search optimizations and bitmask refinements are later reference material.

### 15.1 Recursion Fundamentals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Recursion is the foundation of tree DFS, graph DFS, divide-and-conquer, memoization, and backtracking.

Every correct recursive function needs:

1. **Contract:** What does `f(state)` return or accomplish?
2. **Base case:** Which smallest/terminal state stops recursion?
3. **Progress:** How does every call move toward a base case?
4. **Combination:** How are subproblem results assembled?

```java
static long factorial(int n) {
    if (n < 0 || n > 20) {
        throw new IllegalArgumentException("long factorial requires 0 <= n <= 20");
    }
    if (n <= 1) return 1;         // Base case.
    return n * factorial(n - 1); // Progress and combination.
}
```

The recurrence has `O(n)` time and `O(n)` call-stack space; this fixed-width demonstration accepts only `0..20` because `21!` overflows `long`. Java does not guarantee tail-call elimination. Deep recursive input may throw `StackOverflowError`; a tail-recursive formulation must not be analyzed as constant-space.

#### Deriving recursive complexity

- One call makes one call on `n - 1`: `T(n) = T(n - 1) + O(1) = O(n)`.
- One call makes two calls on `n - 1`: roughly `T(n) = 2T(n - 1) + O(1) = O(2^n)`.
- Balanced divide-and-conquer with two half-size calls plus linear combining: `T(n) = 2T(n/2) + O(n) = O(n log n)`.
- DFS on a tree/graph is not automatically exponential: visited state or disjoint subtrees often ensure every node is handled once, giving `O(V + E)` or `O(n)`.

**Recursion versus iteration:** Recursion often matches structural problems and is easier to prove. Iteration avoids call-stack limits and can expose control/state explicitly. Both may use the same asymptotic auxiliary memory—an explicit stack is still a stack.

**Common mistakes/edge cases:** Missing base case; input does not shrink; base case after an invalid access; accidental shared mutable state; forgetting call-stack space; and stack overflow on deep input.

### 15.2 Recursion Trees

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** A recursion tree makes both correctness and exponential complexity visible and prevents mysterious template memorization.

For subsets of `[a, b, c]`, level `i` decides whether to include item `i`:

```text
                         []
                    /          \
                take a       skip a
               /     \        /     \
           take b  skip b  take b  skip b
             ...      ...      ...      ...
```

- **Node:** one partial solution/state.
- **Edge:** one decision.
- **Depth:** number of decisions made.
- **Leaf:** complete candidate or terminal failure.
- **Branching factor:** number of available choices.

There are `2^n` leaves for binary choose/skip decisions. Copying each length-up-to-`n` output makes enumerating all subsets `O(n * 2^n)` time including output, not merely `O(2^n)`.

**How to use it:** Draw two or three levels. Label what changes on each edge and what state must be restored when returning. Count the leaves to estimate unavoidable output size.

### 15.3 General Backtracking Template

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Nearly every standard combination/permutation/constraint-search solution is a specialization of this control flow.

```text
backtrack(state):
    if complete(state):
        record an independent snapshot of state
        return
    for each valid choice from state:
        apply choice
        backtrack(updated state)
        undo choice
```

Before writing it, identify:

- **State:** the minimum information that determines future choices.
- **Choices:** what decisions are possible here.
- **Constraints:** what makes a partial candidate invalid.
- **Goal/base case:** when to record or return success.
- **Restoration:** exactly what mutation each recursive call must undo.

**Copy versus mutate:** Copying state for each call simplifies restoration but costs time/space. In-place mutation plus undo is efficient and common. `result.add(path)` copies only a reference to the same mutable list, so later undo steps would corrupt every recorded answer. Use `result.add(new ArrayList<>(path))`. This copies the list structure; its `Integer` elements are safely shared because they are immutable.

> 🌐 **Java Backend Relevance — HIGH:** `new ArrayList<>(source)` is a shallow structural copy, and `final List<?>` only prevents reassigning the reference. Neither makes mutable elements immutable. Distinguishing aliases, snapshots, and immutable values prevents shared-state bugs in application data transformations.

**When to stop early:** If the problem asks only whether a solution exists, return `true` immediately on success instead of enumerating all answers.

### 15.4 Subsets: Choose or Skip

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Subsets are the simplest decision tree and teach output-sensitive complexity, path snapshots, and index progress.

```java
static List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    subsetsDfs(nums, 0, new ArrayList<>(), result);
    return result;
}

static void subsetsDfs(int[] nums, int index, List<Integer> path,
                        List<List<Integer>> result) {
    if (index == nums.length) {
        result.add(new ArrayList<>(path)); // Copy the current snapshot.
        return;
    }
    path.add(nums[index]);
    subsetsDfs(nums, index + 1, path, result);
    path.remove(path.size() - 1); // Remove by int index, not Integer value.
    subsetsDfs(nums, index + 1, path, result);
}
```

An equally useful form records `path` at every node and loops choices from a `start` index. That form generalizes naturally to combinations.

**Recognition:** All selections, subsequences where relative order is retained, choose any number of items, or powerset.

**Complexity:** `2^n` subsets. `O(n * 2^n)` time including copying output; `O(n)` recursion/path auxiliary space, excluding `O(n * 2^n)` output.

**Mistakes/edge cases:** Storing the same mutable `path` reference instead of an independent list snapshot; failing to advance the index; treating subsequences as substrings; and producing duplicates when input contains repeated values.

### 15.5 Combinations: Start-Index Search

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Many “choose `k`” and sum-construction questions depend on controlling which future candidates remain eligible.

```java
static List<List<Integer>> combine(int n, int k) {
    List<List<Integer>> result = new ArrayList<>();
    if (n < 0 || k < 0 || k > n) return result;
    combinationsDfs(n, k, 1, new ArrayList<>(), result);
    return result;
}

static void combinationsDfs(int n, int k, int start, List<Integer> path,
                             List<List<Integer>> result) {
    if (path.size() == k) {
        result.add(new ArrayList<>(path));
        return;
    }
    int needed = k - path.size();
    int lastStart = n - needed + 1;
    for (int value = start; value <= lastStart; value++) {
        path.add(value);
        combinationsDfs(n, k, value + 1, path, result);
        path.remove(path.size() - 1);
    }
}
```

**Invariant:** `start` prevents both reusing earlier items and generating the same combination in different orders. The upper bound prunes branches that cannot collect enough remaining elements.

**Complexity:** `C(n, k)` outputs of length `k`; total time is `O((k + 1) * C(n, k))`, including the `k = 0` case returning one empty combination. Auxiliary path/stack space is `O(k)`, excluding output. As usual for enumeration, constraints must keep the output feasible.

**Reuse variants:**

- Each candidate used once → recurse with `i + 1`.
- Same candidate reusable → recurse with `i`.
- Order matters → do not use a monotonic start index; usually use permutation state or DP counting depending on the question.

**Mistakes:** Advancing incorrectly, confusing combinations with permutations, not sorting before value-based pruning, and using `break` on an unsorted candidate list.

### 15.6 Permutations: Used-Choice Search

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Permutations test state restoration and the distinction between position choices and monotonic combinations.

```java
static List<List<Integer>> permutations(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    permutationsDfs(nums, new boolean[nums.length], new ArrayList<>(), result);
    return result;
}

static void permutationsDfs(int[] nums, boolean[] used, List<Integer> path,
                             List<List<Integer>> result) {
    if (path.size() == nums.length) {
        result.add(new ArrayList<>(path));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true;
        path.add(nums[i]);
        permutationsDfs(nums, used, path, result);
        path.remove(path.size() - 1);
        used[i] = false;
    }
}
```

**Recognition:** All orderings/arrangements; fill each position with an unused item; route orders; assignments where sequence matters.

**Complexity:** `n!` outputs and `O(n * n!)` time including copies; `O(n)` auxiliary path/used/stack, excluding output.

**Alternative:** Swap the current position with each later position, recurse, then swap back. It uses the input array as path state but mutates it.

**Mistakes:** Tracking values instead of indices when duplicates/identity matter; forgetting to reset `used`; using a start index and accidentally generating combinations; and not defining whether duplicate-value permutations should be unique.

### 15.7 Duplicate Handling

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Duplicate output is one of the most common backtracking bugs, especially in combination and permutation follow-ups.

Sort first so equal choices are adjacent. For combinations/subsets, skip equal values at the **same recursion level**:

```java
static List<List<Integer>> uniqueSubsets(int[] nums) {
    int[] sorted = Arrays.copyOf(nums, nums.length);
    Arrays.sort(sorted);
    List<List<Integer>> result = new ArrayList<>();
    uniqueSubsetsDfs(sorted, 0, new ArrayList<>(), result);
    return result;
}

static void uniqueSubsetsDfs(int[] nums, int start, List<Integer> path,
                             List<List<Integer>> result) {
    result.add(new ArrayList<>(path));
    for (int i = start; i < nums.length; i++) {
        if (i > start && nums[i] == nums[i - 1]) continue;
        path.add(nums[i]);
        uniqueSubsetsDfs(nums, i + 1, path, result);
        path.remove(path.size() - 1);
    }
}
```

Why `i > start` rather than `i > 0`? Equal values may both appear in one valid candidate at different depths; only repeated sibling choices produce duplicate branches.

**Complexity:** Worst-case `O(n log n + n * 2^n)` time including sorting and answer snapshots; duplicates may reduce the output. The copied array, path, and recursion require `O(n)` auxiliary space, excluding output.

For unique permutations after sorting, skip `nums[i]` when it equals `nums[i - 1]` **and the previous equal item has not been used in the current path**. This chooses a consistent order among equal siblings.

**Alternative:** Store completed snapshots in a `HashSet<List<Integer>>`. List equality and hashing compare elements, but hashing each path costs `O(path length)` and duplicate branches are still explored. Never mutate a list while it is a set key. A `HashSet<int[]>` would use array identity and would not deduplicate equal contents.

### 15.8 Constraint Search and Pruning

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Standard board and partition questions become feasible only when impossible partial candidates are rejected early.

Pruning means proving that an unfinished branch cannot produce a required or better answer.

Common safe pruning rules:

- **Constraint violation:** repeated column/diagonal in N-Queens; reused cell in word search.
- **Monotonic bound:** sorted positive candidates exceed the remaining target, so later candidates also fail.
- **Insufficient remaining choices:** fewer items remain than positions needed.
- **Best-case bound:** even an optimistic continuation cannot beat the current best.
- **Memoized failure:** the same future-determining state was already proven unsolvable.

```java
static List<List<Integer>> combinationSum(int[] candidates, int target) {
    int[] sorted = Arrays.copyOf(candidates, candidates.length);
    Arrays.sort(sorted);
    for (int value : sorted) {
        if (value <= 0) throw new IllegalArgumentException("candidates must be positive");
    }
    List<List<Integer>> result = new ArrayList<>();
    if (target >= 0) combinationSumDfs(sorted, 0, target, new ArrayList<>(), result);
    return result;
}

static void combinationSumDfs(int[] candidates, int start, int remaining,
                               List<Integer> path, List<List<Integer>> result) {
    if (remaining == 0) {
        result.add(new ArrayList<>(path));
        return;
    }
    for (int i = start; i < candidates.length; i++) {
        if (i > start && candidates[i] == candidates[i - 1]) continue;
        int value = candidates[i];
        if (value > remaining) break;
        path.add(value);
        combinationSumDfs(candidates, i, remaining - value, path, result);
        path.remove(path.size() - 1);
    }
}
```

**Termination contract:** Candidates must be positive; zero or negative reusable values invalidate the decreasing-remaining argument. This implementation validates that rule and skips equal sibling values. A negative target returns no answers; target zero returns one empty answer. If the minimum candidate is `m > 0`, recursion depth is at most `target / m`. Sorting/copying costs `O(n log n)` time and `O(n)` auxiliary space before the output-sensitive search.

**Do not overclaim complexity:** Pruning can drastically improve real inputs but worst-case search may remain exponential.

**Backtracking versus DP:** Backtracking is natural when actual candidates/paths must be generated or constraints depend on a partial construction. DP is natural when many paths reach the same state and only a count/best/possible result is needed. They can combine: memoize failed backtracking states, but ensure the cache key includes every field affecting the future.

### 15.9 Board Search

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Word-search and placement problems are common constraint-search examples that test marking and restoration.

For a word in a grid, `dfs(r, c, i)` asks whether the suffix starting at `word[i]` can be matched from `(r, c)`. Temporarily mark the cell, explore legal neighbors, then restore it before returning.

**Typical complexity:** Word Search can be bounded by `O(RC * 3^L)` after the first character for word length `L` because the path cannot immediately reuse the previous cell; it is often conservatively stated `O(RC * 4^L)`. Auxiliary recursion is `O(L)`.

**Common mistakes/edge cases:** Not restoring a cell on every return path; globally marking a cell when it may be reused in a different starting path; checking bounds after indexing; empty word semantics; and using ordinary visited `(r, c)` across paths rather than path-local state.

### Backtracking Pattern Map

| Output/goal | State | Choice control | Typical bound |
|---|---|---|---:|
| Every subset | `index`, path | Include or exclude | `2^n` leaves |
| Choose `k` items | `start`, path | Later indices only | `C(n,k)` outputs |
| Every ordering | path, `used` | Any unused index | `n!` outputs |
| Sum using candidates | `start`, remaining | `i` or `i+1` for reuse rule | Exponential in general |
| Partition a string | start index, pieces | Choose next cut | Up to exponential |
| Search a grid path | cell, matched index, path marks | Legal neighbor | Exponential in path length |
| Constraint assignment | next variable + occupied sets | Valid value | Branching factor^depth, pruned |

### Representative Recursion & Backtracking Problems

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Choose/skip and combinations | Trace factorial; Combinations | ⭐ **Canonical Interview Problem:** Subsets | Subsets II / Combination Sum II | Palindrome Partitioning (cuts + validation) | Subsets II: explain same-level duplicate skipping |
| Ordered choices | Letter Combinations of a Phone Number | ⭐ **Canonical Interview Problem:** Permutations | Permutations II | Generate Parentheses (constraint pruning) | Permutations: reconstruct used-state restoration |
| Path-local constraints | Trace one grid word by hand | ⭐ **Canonical Interview Problem:** Word Search | Combination Sum (reuse and positive bounds) | N-Queens (later occupancy constraints) | Word Search, checking every early return restores the board |

Choose one variation per row first. **Restore IP Addresses** develops remaining-length pruning. **Sudoku Solver** and **Partition to K Equal Sum Subsets** are optional advanced search; learn symmetry/memoization only after ordinary restoration and pruning are reliable. Naive Fibonacci is a tracing/overlap example, not a recommended computation strategy.

### Common Backtracking Mistakes and Interview Tips

- Draw the decision tree for two or three items and label each argument.
- State whether the question needs one solution, all solutions, a count, or the best solution; this changes early stopping and may suggest DP.
- Append a copy of mutable path state to results.
- Place undo logic so it executes after every explored branch; be careful with early returns.
- Clarify whether items can be reused and whether input/output duplicates are allowed.
- Sort only if it preserves semantics and supports duplicate skipping or monotonic pruning.
- Explain complexity using choices, depth, and output size—not merely “recursive, so exponential.”
- Never cite a pruning rule unless you can explain why no valid answer is lost.

### Recursion & Backtracking Mastery Checklist

I have mastered this topic when I can:

- [ ] Define a recursive contract, base case, progress step, and combination.
- [ ] Trace the call stack and include it in space complexity.
- [ ] Draw a recursion tree and estimate branching/depth/output size.
- [ ] Implement choose–explore–unchoose without shared-state bugs.
- [ ] Generate subsets, combinations, and permutations from scratch.
- [ ] Adjust index rules for single-use versus reusable choices.
- [ ] Handle duplicate inputs without relying only on a set of completed results.
- [ ] Design and justify safe pruning conditions.
- [ ] Solve standard grid/word-search and partition backtracking problems.
- [ ] Explain when DP/memoization is preferable to pure enumeration.

---

## 16. Greedy Algorithms

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Greedy ideas occur frequently in intervals, scheduling, reachability, and resource allocation. They can produce very simple optimal solutions—but only with a valid invariant or exchange argument. The interview skill is recognizing and justifying the choice, not memorizing “sort and pick.”

### Topic Overview

- **What it is:** A greedy algorithm commits to a locally best-looking choice and does not revisit it.
- **Why it exists:** When a problem has the right structure, a safe local choice reduces the remaining problem and yields a globally optimal answer with less state than DP or search.
- **Why it matters in interviews:** Greedy solutions are often the intended optimization after brute force or DP, and interviewers expect a concise correctness explanation.
- **Interview priority:** 🟠 Tier 2 — Very Important.
- **Prerequisites:** Sorting, comparators, and loop invariants; heaps for heap-assisted variants. Learn the proof ideas here before interval scheduling in Section 17; DP awareness helps compare alternatives later.
- **Common use cases:** Scheduling, selecting non-overlapping items, reachability frontiers, assigning resources, minimizing removals, partitioning, and repeatedly taking the best available choice.
- **Common problem patterns:** Sort by finish time; maintain farthest reachable point; assign smallest sufficient resource; merge/use cheapest available option; delay commitment until necessary.
- **How to recognize problems that require it:** The goal asks for a global minimum/maximum, choices can be ordered, and one choice seems to leave an equal-or-better remainder for every future decision. A clean exchange argument or dominance invariant is the real signal.
- **How deeply to understand it:** Master standard interval and frontier greedy patterns and be able to defend correctness. Do not treat “optimization” or “sorted input” as proof that greedy works.

### Focus First

- Identify the candidate local choice and state the invariant it preserves.
- Sorting + greedy, especially earliest-finish scheduling.
- Farthest-reachable/frontier patterns.
- Greedy-versus-DP diagnosis and counterexample construction.

### Learn Later

- Heap-assisted greedy, matching resources to demands, and delayed decisions.
- Formal exchange and staying-ahead proof styles.

### Optional / Specialized

- **Matroid theory — ⚪ Tier 4 — Low Priority / Specialized.** It explains broad classes of correct greedy algorithms but is unnecessary for typical coding interviews.
- **Approximation algorithms — ⚪ Tier 4 — Low Priority / Specialized.** Useful academically and for some systems/optimization roles, but rarely part of general SWE coding rounds.

**Study target:** Reconstruct frontier scans and sort keys; understand and explain why a choice is safe. Solve 4–6 representative problems across frontier, matching, and scheduling, then mix with DP counterexamples. Formal matroid theory and specialized scheduling are lookup material.

### 16.1 Greedy Reasoning: Choice, Invariant, Proof

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** A greedy implementation is often easy; deciding that the local choice is safe is the entire interview challenge.

A sound greedy explanation has four parts:

1. **Choice:** What exactly is selected now?
2. **Ordering:** Why are candidates processed in this order?
3. **Invariant:** What does the maintained state mean after each step?
4. **Safety/proof:** Why can any optimal solution be transformed to use this choice without becoming worse?

#### Exchange argument

Suppose a candidate interval `G` finishes earliest. Take any optimal schedule whose first interval is `O`. Replacing `O` with `G` cannot reduce room for later intervals because `G` ends no later. Therefore, some optimal solution begins with `G`; repeat on the remainder.

#### Staying-ahead argument

Show that after every prefix/decision count, the greedy solution's state is at least as favorable as any competitor's—for example, it reaches at least as far using no more jumps.

#### Cut/safe-edge argument

Common in MST: the cheapest edge crossing a suitable partition can be chosen without sacrificing optimality.

**Useful counterexample habit:** If the rule “take the locally largest value” seems plausible, test a three-choice example where that selection blocks two moderately valuable compatible choices. If a small counterexample exists, use DP/search or a different greedy order.

### 16.2 Sorting + Greedy

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Sorting reveals an order in which a small amount of state is enough to make safe choices. This is the dominant interview greedy pattern.

Possible sort keys have very different meanings:

- **Earliest finishing time:** leaves the most future room; maximizes count of non-overlapping intervals.
- **Start time:** exposes overlaps for merging; does not by itself maximize scheduled count.
- **Demand/size:** supports matching smallest sufficient resource to smallest unmet need.
- **Deadline:** often supports scheduling feasibility or lateness reasoning.
- **Difference/ratio:** may be useful in particular cost models, but requires a proof; ratios do not solve 0/1 knapsack.

Sorting generally makes total time `O(n log n)` even if the scan is `O(n)`. Space depends on whether sort is in-place and on language implementation.

**Mistakes:** Sorting by the most obvious field rather than the field that makes the invariant safe; losing original indices when output requires them; integer overflow from subtraction comparators; and claiming the scan's `O(n)` while omitting sort cost.

### 16.3 Interval Scheduling / Maximum Non-Overlapping Selection

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is the canonical provably correct greedy problem and directly powers interval-removal variants.

The canonical implementation, earliest-finish invariant, endpoint semantics, complexity, minimum-removals reduction, and weighted-DP contrast are taught once in [Non-Overlapping Selection and Minimum Removals](#175-non-overlapping-selection-and-minimum-removals). Here, retain the greedy lesson: choosing the compatible interval that finishes earliest leaves at least as much room as any alternative and can be justified with an exchange argument.

### 16.4 Reachability Frontier

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** A single dominance value often replaces a quadratic search or DP in jump/reachability questions.

```java
static boolean canJump(int[] nums) {
    long farthest = 0;
    for (int i = 0; i < nums.length; i++) {
        if (i > farthest) return false;
        farthest = Math.max(farthest, (long) i + nums[i]);
    }
    return true; // Convention: empty input needs no moves.
}
```

**Contract:** Jump lengths are nonnegative. The empty-input convention is explicit in the code; some platforms guarantee at least one element.

**Invariant:** Every index up to `farthest` is reachable through some processed choice. If the scan reaches an index beyond the frontier, no earlier choice can cross the gap.

**Complexity:** `O(n)` time and `O(1)` space.

For minimum jumps, process a current reachable layer similarly to BFS: track the farthest next-layer endpoint and increase the jump count when finishing the current range.

**Recognition:** Choices cover a contiguous reachable prefix/range, and among partial solutions only the farthest frontier matters.

**Mistakes:** Choosing the largest immediate jump rather than the best resulting frontier; stepping from an unreachable index; and applying the pattern when reachable states are not a contiguous dominated region.

### 16.5 Resource Matching

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Matching the smallest sufficient resource to the smallest remaining demand is a reusable sort-and-scan idea in assignment questions.

Sort demands and resources. If the smallest resource cannot satisfy the smallest demand, it cannot satisfy any larger demand, so discard it. If it can, use it there; saving that small resource cannot enable more matches than consuming it now.

```java
static int maxAssignments(int[] demands, int[] resources) {
    int[] sortedDemands = Arrays.copyOf(demands, demands.length);
    int[] sortedResources = Arrays.copyOf(resources, resources.length);
    Arrays.sort(sortedDemands);
    Arrays.sort(sortedResources);
    int demandIndex = 0;
    for (int resource : sortedResources) {
        if (demandIndex < sortedDemands.length && resource >= sortedDemands[demandIndex]) {
            demandIndex++;
        }
    }
    return demandIndex;
}
```

**Complexity:** `O(n log n + m log m)` time and `O(n + m)` auxiliary space for the two explicit primitive-array copies, which preserve caller input. Sorting caller arrays directly can remove those copies if mutation is allowed.

**Recognition:** Each resource can serve at most one demand, any sufficiently large resource works, and the goal is maximum number served rather than maximum weighted value.

### 16.6 Heap-Assisted Greedy

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Some problems reveal choices in one sorted order but require selecting the best currently eligible candidate by another key.

General shape:

1. Sort events/items by when they become available.
2. Advance through that order, pushing eligible candidates into a heap.
3. Pop the locally best eligible candidate.
4. Repeat while maintaining feasibility.

Examples include choosing the most profitable affordable project, assigning meeting rooms by earliest release, scheduling tasks by deadline/duration, and minimizing refueling stops by taking the largest fuel among passed stations only when necessary.

**Complexity:** Usually `O(n log n)` from sorting plus one heap push/pop per item, with `O(n)` space.

**Trade-off:** The heap is not proof of greediness; explain why the best currently eligible choice is safe or why delayed commitment lets you exchange a previous choice.

### 16.7 Greedy Versus Dynamic Programming

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Incorrectly forcing a greedy rule onto a DP problem is a major interview failure mode.

| Question | Greedy is promising when... | DP is promising when... |
|---|---|---|
| Does one local choice dominate others? | It leaves a remainder at least as flexible | Choices trade off incompatible future benefits |
| Do we need to reconsider earlier choices? | No; an exchange argument makes commitment safe | Yes; optimal choice depends on state/context |
| Do subproblems repeat? | Not essential | Many paths reach the same state |
| Can a tiny counterexample break the rule? | No after systematic testing/proof | Yes |
| Typical example | Unweighted interval scheduling | Weighted interval scheduling / 0-1 knapsack |

Coin change illustrates the danger: choosing the largest coin first works for some currency systems but not arbitrary denominations. Coins `[1, 3, 4]`, target `6`: greedy uses `4 + 1 + 1`, while optimum is `3 + 3`.

**Interview strategy:** Offer the greedy candidate, test it aloud with adversarial examples, then either justify an invariant/exchange or switch to DP. This demonstrates judgment, not hesitation.

### Common Greedy Patterns

| Clue | Candidate rule | Proof/invariant to seek | Warning |
|---|---|---|---|
| Max number of compatible intervals | Earliest finish | Leaves maximum future room | Weights invalidate it |
| Cover/reach a line | Extend farthest among currently reachable choices | Current prefix is covered | Ensure no gap |
| Assign resources to demands | Smallest sufficient resource | Saves larger resources | Values/weights may change goal |
| Choices become eligible over time | Sort + heap best eligible | Only eligible items can be selected | Define selection key carefully |
| Remove overlaps | Keep interval with earlier end | Dominates later-ending overlap | Endpoint rules matter |
| Unlimited stock transactions | Take every positive adjacent gain | Gains telescope across rises | Fees/cooldowns require new state |
| Build minimum-cost connection | MST safe edge | Cut property | Not a shortest-path problem |

### Representative Greedy Problems

| Stage | Selected problem | Transfer target |
|---|---|---|
| Mechanics | Assign Cookies | Smallest sufficient resource and exchange proof |
| Canonical | ⭐ **Canonical Interview Problem:** Jump Game | Farthest reachable prefix; never expand an unreachable index |
| Variation | Jump Game II; Non-overlapping Intervals | Reachable layers; earliest-finish scheduling |
| Mixed pattern | Partition Labels or Boats to Save People | Last-occurrence hashing or two-pointer dominance |
| Cold revisit | Jump Game plus a weighted-interval/coin-change counterexample | Reconstruct the invariant and recognize when greedy fails |

Optional focused drills: **Stock II** for summing positive gains, **Lemonade Change** for flexible inventory, **Gas Station** for candidate elimination, **Task Scheduler** for counting versus simulation. Later: **Minimum Refueling Stops**, **Course Schedule III**, **Candy**, and **Remove Duplicate Letters** for heap replacement, directional constraints, and monotonic-stack greedy.

### Common Greedy Mistakes and Interview Tips

- Do not say “greedy is faster” as a correctness argument.
- Name the exact local choice and why it leaves no worse future.
- Search for a counterexample using three or four items before committing.
- Distinguish maximizing **count** from maximizing **weight/value**.
- Include sorting and heap operations in complexity.
- Clarify endpoint and tie behavior; a different tie-breaker can change feasibility.
- If the choice needs to be revised later, consider delayed-decision greedy, heap replacement, or DP.
- Explain why discarded information is dominated and can never matter again.

### Greedy Mastery Checklist

I have mastered interview greedy algorithms when I can:

- [ ] Recognize common sorting, frontier, assignment, and heap-assisted patterns.
- [ ] State a loop invariant for a proposed greedy solution.
- [ ] Give an exchange, staying-ahead, or safe-choice argument in plain language.
- [ ] Construct small counterexamples to reject invalid greedy rules.
- [ ] Solve standard interval scheduling and reachability-frontier problems.
- [ ] Distinguish unweighted interval scheduling from weighted interval DP.
- [ ] Compare greedy, DP, backtracking, and heap alternatives.
- [ ] Include sorting/heap costs and handle ties/endpoints explicitly.

---

## 17. Intervals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Interval questions are common, compact interview problems that combine sorting, boundary semantics, greedy reasoning, heaps, and sweep lines. Merge, insert, overlap removal, and meeting-room variants should be routine.

### Topic Overview

- **What it is:** An interval represents a continuous range with a start and end, such as `[start, end]` or `[start, end)`.
- **Why it exists:** Many scheduling, coverage, timeline, and resource-allocation problems concern ranges rather than individual points.
- **Why it matters in interviews:** A small set of transferable patterns solves a large family of realistic problems, but correctness depends on sorting key and endpoint semantics.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Sorting, comparators, arrays, and basic prefix/difference ideas. Merge/insert come first; use the proof ideas from Section 16 for selection and heaps from Section 13 for room allocation.
- **Common use cases:** Calendar conflicts, CPU/job scheduling, reservation systems, range coverage, employee availability, and event concurrency.
- **Common problem patterns:** Merge ranges, insert one range, detect overlap, select compatible ranges, count concurrent intervals, allocate rooms, and cover a target range.
- **How to recognize problems that require it:** The input consists of starts/ends, times, ranges, meetings, coverage, bookings, or “active at the same time” events.
- **How deeply to understand it:** Deep mastery of start-sorted merging, endpoint definitions, overlap conditions, earliest-finish greedy, and meeting-room concurrency. Understand heap and sweep-line formulations and be able to compare them. Advanced computational-geometry sweep lines are optional.

### Focus First

- Define whether intervals are closed `[s, e]`, open, or half-open `[s, e)`.
- Sort by start for merging and overlap scans.
- Merge Intervals and Insert Interval.
- Earliest-end greedy for maximum compatible set/minimum removals.
- Meeting Rooms I/II with heap or sorted endpoints.

### Learn Later

- Sweep-line event aggregation, interval intersections, coverage gaps, and line-covering greedy.
- Preserving original indices and returning room assignments rather than only a count.

### Optional / Specialized

- **Coordinate compression with difference/Fenwick/segment trees — 🟡 Tier 3 — Nice to Know.** Useful for many large-coordinate range updates/queries, but not needed for ordinary interval scans.
- **Computational-geometry sweep line — ⚪ Tier 4 — Low Priority / Specialized.** Segment intersections and geometric event structures are rare in general SWE interviews.
- **Interval trees — ⚪ Tier 4 — Low Priority / Specialized.** Useful for dynamic overlap queries, but library/data-system design knowledge is more relevant than implementing one in typical coding rounds.

**Study target:** Reproduce merge/insert from memory and explain endpoint tests; then derive selection and concurrency from their invariants. Solve 5–7 problems across these families and revisit the canonical problems cold. Dynamic interval structures and geometry sweeps are reference knowledge.

### 17.1 Endpoint Semantics and Overlap

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Most interval bugs are not algorithmic—they come from an unstated endpoint rule.

Common models:

- **Closed** `[start, end]`: both endpoints are included. `[1, 2]` and `[2, 3]` overlap at `2`.
- **Half-open** `[start, end)`: start included, end excluded. `[1, 2)` and `[2, 3)` do not overlap. This model is common in programming and scheduling.
- **Open** `(start, end)`: endpoints excluded; less common in coding interviews.

For two start-sorted intervals `current` and `next`:

- Half-open overlap: `next.start < current.end`.
- Half-open compatible/touching: `next.start >= current.end`.
- Closed overlap: `next.start <= current.end`.
- Closed disjoint: `next.start > current.end`.

For arbitrary intervals `[a, b]` and `[c, d]`, closed intersection exists if `max(a, c) <= min(b, d)`; for half-open intervals, use `<`.

**Interview habit:** State the convention before coding. If the statement says a meeting ending at 10 allows another at 10, use half-open-like compatibility.

**Edge cases:** Zero-length intervals, reversed endpoints (validate or normalize only if contract allows), negative coordinates, duplicate intervals, contained intervals, touching endpoints, and integer extremes.

### 17.2 Merge Overlapping Intervals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** This is the base pattern for start-sorted interval scans and one of the most common interview questions.

#### Intuition and invariant

Sort by start. Once intervals are in start order, a new interval can overlap only the last merged interval; all earlier merged intervals end before it.

```java
record Interval(long start, long end) {
    Interval {
        if (start > end) throw new IllegalArgumentException("start must not exceed end");
    }
}

static List<Interval> mergeIntervals(List<Interval> intervals) {
    List<Interval> sorted = new ArrayList<>(intervals);
    sorted.sort(Comparator.comparingLong(Interval::start).thenComparingLong(Interval::end));
    List<Interval> merged = new ArrayList<>();
    for (Interval interval : sorted) {
        if (merged.isEmpty() || interval.start() > merged.get(merged.size() - 1).end()) {
            merged.add(interval);
        } else {
            Interval last = merged.get(merged.size() - 1);
            merged.set(merged.size() - 1,
                    new Interval(last.start(), Math.max(last.end(), interval.end())));
        }
    }
    return merged;
}
```

**Java representation:** Interval snippets share the immutable `Interval(long start, long end)` record above. The constructor rejects reversed endpoints. Merge/insert/intersection use closed intervals; scheduling/removal/concurrency use half-open intervals and ignore empty `[s, s)` ranges. Inputs contain non-null records; index-based scans assume constant-time list access. An `int[][]` is also practical on coding platforms, but an outer-array copy still aliases every endpoint row.

> 🌐 **Java Backend Relevance — HIGH:** Immutable records give range fields names and value equality. A copied list can safely share these interval objects; mutable `long[]` rows would need deliberate copying before endpoint mutation.

**Invariant:** `merged` contains the fully merged union of all processed intervals, in sorted disjoint order.

**Complexity:** `O(n log n)` time; `O(n)` auxiliary space for the copied list and object-list sort, excluding `O(n)` output. Copying a list copies its element references; immutable `Interval` records make this safe without deep copying.

**When to use / recognition:** Combine bookings/ranges, compute union, remove redundant covered pieces, or normalize ranges.

**Why `max(end)` matters:** When `[1, 10]` contains `[2, 3]`, assigning end to `3` would shrink the union incorrectly.

**Alternatives/trade-offs:** If intervals arrive already sorted, scan in `O(n)`. For small bounded integer coordinates, a difference array can represent coverage. For online insert/query, an ordered structure may be needed.

**Common mistakes:** Forgetting to sort; sorting by end; using the wrong `<`/`<=`; shrinking on containment; mutating caller-owned intervals unexpectedly; and sharing mutable endpoint arrays unintentionally. Java passes a copied reference; this implementation explicitly creates a new list and replaces immutable records when merging, preserving the caller's data.

### 17.3 Insert an Interval

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** It tests whether a candidate can exploit an existing sorted, non-overlapping invariant instead of re-sorting unnecessarily.

For sorted disjoint intervals:

1. Append intervals strictly before the new one.
2. Merge all overlapping intervals into the new range.
3. Append everything strictly after it.

```java
static List<Interval> insertInterval(List<Interval> intervals, Interval added) {
    List<Interval> result = new ArrayList<>();
    int index = 0;
    long start = added.start();
    long end = added.end();
    while (index < intervals.size() && intervals.get(index).end() < start) {
        result.add(intervals.get(index++));
    }
    while (index < intervals.size() && intervals.get(index).start() <= end) {
        start = Math.min(start, intervals.get(index).start());
        end = Math.max(end, intervals.get(index).end());
        index++;
    }
    result.add(new Interval(start, end));
    while (index < intervals.size()) result.add(intervals.get(index++));
    return result;
}
```

This code uses closed-overlap semantics. Adjust strictness for half-open rules.

**Complexity:** `O(n)` time and `O(1)` auxiliary state excluding `O(n)` output, assuming an `ArrayList` or another list with constant-time indexed access. No sort is needed because the precondition provides order.

**Mistakes/edge cases:** Ignoring the sorted/disjoint promise; dropping intervals before/after the merge block; mutating a shared endpoint array unexpectedly; empty input; new interval before/after all others; and using inconsistent endpoint tests in the three phases.

**Alternative:** Append the new interval and call merge for `O(n log n)`. It is simpler but fails to exploit the useful precondition; mention it as brute force, then optimize.

### 17.4 Interval Intersection

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is a clean two-pointer range pattern and a common follow-up when both interval lists are already sorted/disjoint.

```java
static List<Interval> intervalIntersections(List<Interval> first, List<Interval> second) {
    List<Interval> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < first.size() && j < second.size()) {
        Interval a = first.get(i);
        Interval b = second.get(j);
        long start = Math.max(a.start(), b.start());
        long end = Math.min(a.end(), b.end());
        if (start <= end) result.add(new Interval(start, end)); // Closed intervals.
        if (a.end() < b.end()) i++;
        else j++;
    }
    return result;
}
```

**Complexity:** `O(n + m)` time and `O(1)` auxiliary space excluding output, assuming constant-time indexed access for both lists.

**Invariant:** Any future intersection must involve the interval whose end extends farther; discard the one ending first.

**Mistakes:** Advancing by start rather than end; failing to advance both-or-one safely on equal ends; wrong endpoint condition; and using nested loops for sorted lists.

### 17.5 Non-Overlapping Selection and Minimum Removals

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** This is the highest-value interval greedy pattern and contrasts directly with merging.

To maximize the number of mutually compatible intervals, sort by **end** and repeatedly keep the next interval whose start is compatible. To minimize removals, return `n - kept`.

```java
static int minRemovalsForNonOverlap(List<Interval> intervals) {
    // Empty half-open intervals occupy no time and never need removal.
    List<Interval> sorted = new ArrayList<>();
    for (Interval interval : intervals) {
        if (interval.start() < interval.end()) sorted.add(interval);
    }
    sorted.sort(Comparator.comparingLong(Interval::end).thenComparingLong(Interval::start));
    long previousEnd = Long.MIN_VALUE;
    int kept = 0;
    for (Interval interval : sorted) {
        if (interval.start() >= previousEnd) {
            kept++;
            previousEnd = interval.end();
        }
    }
    return sorted.size() - kept;
}
```

**Complexity:** `O(n log n)` time and `O(n)` auxiliary space for the copied/filtered list and object-list sort.

**Recognition:** Schedule the most jobs/meetings, remove fewest overlaps, choose maximum compatible subset.

**Why not sort by start:** An early-starting interval may end very late and block many short later intervals. Earliest finish leaves the most room.

**Alternative scan:** Sort by start; whenever two intervals overlap, conceptually remove the one with the larger end. This maintains the same earliest-ending survivor.

**Weighted warning:** If intervals have profit/weight and the goal maximizes total weight, use weighted interval scheduling DP with binary search for the previous compatible interval.

### 17.6 Meeting Rooms I: Can One Resource Handle All Intervals?

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** It is the simplest overlap-detection question and establishes the start-sorted neighbor property.

```java
static boolean canAttendAll(List<Interval> meetings) {
    List<Interval> sorted = new ArrayList<>(meetings);
    sorted.sort(Comparator.comparingLong(Interval::start));
    long previousEnd = Long.MIN_VALUE;
    for (Interval meeting : sorted) {
        if (meeting.start() == meeting.end()) continue; // Empty [s, s).
        if (meeting.start() < previousEnd) return false;
        previousEnd = meeting.end();
    }
    return true;
}
```

Assuming ending and starting at the same time is allowed, use `<`; otherwise use `<=`.

**Complexity:** `O(n log n)` time and `O(n)` auxiliary space for the copied list and object-list sort.

**Why the scan suffices:** Before processing the next nonempty meeting, all previously accepted meetings are disjoint and `previousEnd` is their latest occupied end. A start before that end is a conflict; otherwise the invariant extends to the new meeting.

### 17.7 Meeting Rooms II: Minimum Concurrent Resources

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** This common question connects intervals to both heaps and sweep lines and tests interpretation of maximum concurrency.

#### Min-heap of room end times

Sort meetings by start. The heap contains end times of rooms currently in use. Release all rooms that have ended before the next start, then allocate the meeting.

```java
static int minMeetingRooms(List<Interval> meetings) {
    List<Interval> sorted = new ArrayList<>(meetings);
    sorted.sort(Comparator.comparingLong(Interval::start));
    PriorityQueue<Long> activeEnds = new PriorityQueue<>();
    int maximum = 0;
    for (Interval meeting : sorted) {
        if (meeting.start() == meeting.end()) continue;
        while (!activeEnds.isEmpty() && activeEnds.peek() <= meeting.start()) {
            activeEnds.poll();
        }
        activeEnds.offer(meeting.end());
        maximum = Math.max(maximum, activeEnds.size());
    }
    return maximum;
}
```

**Invariant:** Before insertion, the heap holds exactly the meetings still active at `start`; after insertion, its size is current concurrency.

**Complexity:** `O(n log n)` time and `O(n)` auxiliary space for the copied/sorted list and active heap. The heap may be smaller when concurrency is low, but the input copy still has size `n`.

If only the minimum room count is needed and each new meeting can reuse at most one just-freed room in a start-sorted scan, a common compact variant pops at most one then returns final heap size. Popping all ended meetings and tracking `maximum` makes the active-set invariant explicit and generalizes better.

#### Sorted starts and ends

Sort all starts and all ends separately. If next start is before the next end, one more room becomes active; otherwise a room frees first.

```java
static int minMeetingRoomsTwoArrays(List<Interval> meetings) {
    long[] starts = new long[meetings.size()];
    long[] ends = new long[meetings.size()];
    int count = 0;
    for (Interval meeting : meetings) {
        if (meeting.start() < meeting.end()) {
            starts[count] = meeting.start();
            ends[count++] = meeting.end();
        }
    }
    Arrays.sort(starts, 0, count);
    Arrays.sort(ends, 0, count);
    int startIndex = 0;
    int endIndex = 0;
    int active = 0;
    int maximum = 0;
    while (startIndex < count) {
        if (starts[startIndex] < ends[endIndex]) {
            active++;
            maximum = Math.max(maximum, active);
            startIndex++;
        } else {
            active--;
            endIndex++;
        }
    }
    return maximum;
}
```

**Complexity of two arrays:** `O(n log n)` time and `O(n)` auxiliary space for the primitive endpoint arrays; empty half-open meetings are excluded from the sorted prefixes.

**Trade-offs:** The heap can retain room end information and be extended to assign room IDs. Sorted endpoints/sweep line is often simpler when only maximum concurrency is needed.

**Common mistakes/edge cases:** Wrong tie rule; returning final active count rather than maximum in a general sweep; assuming meetings are already sorted; an empty input list; zero-length meetings; and popping latest rather than earliest end.

### 17.8 Sweep Line / Event Counting

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Sweep line generalizes meeting concurrency and handles coverage/event aggregation. Basic one-dimensional sweeps are useful; geometric versions are specialized.

Convert each interval into events:

- At a start: `+1` active.
- At an end: `-1` active.

Sort events, update a running count, and track the desired maximum or covered duration.

```java
record IntervalEvent(long coordinate, int delta) {}

static int maximumOverlapHalfOpen(List<Interval> intervals) {
    List<IntervalEvent> events = new ArrayList<>();
    for (Interval interval : intervals) {
        if (interval.start() == interval.end()) continue;
        events.add(new IntervalEvent(interval.start(), 1));
        events.add(new IntervalEvent(interval.end(), -1));
    }
    events.sort(Comparator.comparingLong(IntervalEvent::coordinate)
            .thenComparingInt(IntervalEvent::delta)); // End (-1) before start (+1).
    int active = 0;
    int maximum = 0;
    for (IntervalEvent event : events) {
        active += event.delta();
        maximum = Math.max(maximum, active);
    }
    return maximum;
}
```

**Tie semantics are algorithmic:**

- Half-open scheduling `[s, e)`: process end before start at the same time.
- Closed overlap `[s, e]`: process start before end at the same coordinate if touching counts concurrently.

For half-open concurrency or covered duration, aggregating deltas at a coordinate is valid. For closed-interval peak overlap, do not combine starts and ends before measuring the point: apply starts, measure the peak, then apply ends. For covered length, accumulate `(coordinate - previous_coordinate)` using the active count *before* applying events at the new coordinate.

**Complexity:** `O(n log n)` time for sorting `2n` events and `O(n)` event space. If coordinates are small bounded integers, a difference array plus prefix sum can reduce scanning to `O(n + U)` for universe size `U`.

**Recognition:** Maximum simultaneous users/bookings, peak load, time periods covered by at least `k` intervals, skyline-like event changes, or sum of range effects.

**Alternatives/trade-offs:** Heap keeps identities/details of active intervals; sweep events efficiently compute aggregate counts. Coordinate compression helps large sparse coordinates when array-based range aggregation is otherwise useful.

**Common mistakes:** Unspecified tie order; updating maximum before/after the wrong delta; processing identical coordinates individually with inconsistent semantics; forgetting that sorted-event space is `O(n)`; and using a difference array over enormous raw coordinates.

### 17.9 Covering a Range with Intervals

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority:** Farthest-extension greedy appears in video stitching and minimum-tap questions, but is less frequent than merging and meeting rooms.

To cover a target beginning at `0`, among all intervals whose start is at or before the current covered endpoint, choose the one that extends farthest. If no such interval extends coverage, a gap makes coverage impossible.

**Invariant:** After each selection count, the greedy method reaches at least as far as any solution using the same number of intervals, because it considers every currently eligible interval and chooses maximum end.

**Complexity:** Sorting followed by the scan takes `O(n log n)` time; copying and sorting a Java list of records uses `O(n)` auxiliary space. Already sorted input needs only an `O(n)` scan with constant auxiliary state. Bounded integer starts sometimes allow a Jump-Game-style `O(n + U)` preprocessing/scan.

**Mistakes:** Selecting merely the earliest-starting interval; committing before seeing all intervals that begin within current coverage; not detecting lack of progress; and confusing minimum number of intervals with maximum non-overlapping selection.

### Interval Pattern Map

| Goal/clue | Sort/order | Core state | Typical approach |
|---|---|---|---|
| Union/merge ranges | Start ascending | Last merged end | Merge scan |
| Insert into sorted disjoint ranges | Existing start order | Before / merge / after phases | Linear scan |
| Intersections of two sorted lists | Both start ordered | Two pointers, earlier end advances | Two pointers |
| Detect any conflict | Start ascending | Previous/max end | Neighbor scan |
| Max compatible count | End ascending | End of last kept | Greedy |
| Min removals for no overlap | End ascending | Number kept | `n - kept` |
| Minimum rooms/resources | Start order + end heap | Active end times | Heap |
| Maximum concurrency only | Sorted events/endpoints | Running active count | Sweep line |
| Minimum intervals to cover target | Start ascending | Current and farthest reach | Frontier greedy |
| Maximum weighted compatible value | End ascending | Best value through prefix | DP + binary search |

### Representative Interval Problems

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Union/ordered ranges | Meeting Rooms (endpoint tests) | ⭐ **Canonical Interview Problem:** Merge Intervals | Insert Interval / Interval List Intersections | Employee Free Time (k-way merge) | Merge touching, nested, and chained intervals |
| Selection | Hand-schedule three intervals | ⭐ **Canonical Interview Problem:** Non-overlapping Intervals | Minimum Arrows to Burst Balloons | Maximum Profit in Job Scheduling (later DP contrast) | Explain why sort by end differs from sort by start |
| Concurrency | Draw start/end events | ⭐ **Canonical Interview Problem:** Meeting Rooms II | Solve once with heap, once with endpoints | My Calendar I (`TreeMap` dynamic neighbors) | Handle equal endpoints and empty half-open ranges |

Select 5–7 problems across the rows, counting alternate implementations as variations rather than new submissions. **Video Stitching / Minimum Taps** add coverage-frontier greedy. **The Skyline Problem** and **Range Module** are optional advanced event/ordered-map work.

### Common Interval Mistakes and Interview Tips

- State endpoint semantics before deriving an overlap condition.
- Say why you sort by **start** (merge/expose) or **end** (leave future room); they are not interchangeable.
- Include sorting in time complexity.
- Test disjoint, touching, identical, nested, and chain-overlap inputs.
- Preserve `max(current_end, next_end)` when one interval contains another.
- Ask whether input is already sorted/disjoint and exploit that promise.
- Distinguish union, maximum compatible subset, maximum concurrency, and range coverage; each uses a different invariant.
- If values/weights are attached, question whether the ordinary interval greedy proof still holds.
- Preserve original indices if the required output refers to input ordering or assignments.

### Interval Mastery Checklist

I have mastered interview intervals when I can:

- [ ] Explain closed versus half-open endpoint rules and choose `<` versus `<=` correctly.
- [ ] Implement Merge Intervals and Insert Interval without notes.
- [ ] Find intersections of two sorted interval lists in linear time.
- [ ] Detect conflicts and solve minimum-overlap removals with the correct sort key.
- [ ] Prove earliest-finish scheduling with a simple exchange argument.
- [ ] Compute minimum meeting rooms using both a heap and a sweep/two-array method.
- [ ] Design event tie-breaking deliberately.
- [ ] Compare heap, event sweep, difference array, and ordered-structure approaches.
- [ ] Recognize weighted interval scheduling as DP rather than ordinary greedy.
- [ ] Handle empty, touching, nested, duplicate, and zero-length intervals.

## 18. Dynamic Programming

**Priority:** 🟠 Tier 2 — Very Important

> Dynamic programming (DP) is not one algorithm. It is a way to organize a search when different decision paths repeatedly reach the same smaller states.

### Topic Overview

- **What it is:** A problem-solving method that defines reusable subproblems, stores their answers, and combines them to solve a larger problem.
- **Why it exists:** Plain recursion often recomputes the same subproblem exponentially many times. DP turns that repeated work into one computation per distinct state.
- **Why it matters in interviews:** DP is a common separator on medium and hard interviews. More importantly, it tests whether you can model a problem precisely rather than recall a named algorithm.
- **Interview priority:** **🟠 Tier 2 — Very Important.** DP is less universal than arrays, hashing, trees, or graph traversal, but it appears often enough that a candidate should confidently solve standard 1D, grid, knapsack, and subsequence forms. The enormous DP topic should not displace Tier 1 fundamentals.
- **Prerequisites:** Recursion, Big-O analysis, arrays and matrices, tree/graph DFS, and the ability to state what a function returns.
- **Common use cases:** Counting ways, deciding feasibility, minimizing cost, maximizing score, choosing non-conflicting items, matching sequences, and optimizing a path through a grid or tree.
- **Common problem patterns:** Take/skip, choose among previous states, prefix-versus-prefix, remaining capacity, cell-from-neighbors, interval split, and include/exclude a tree node.
- **How to recognize it:** The problem asks for a count, possibility, minimum, or maximum; choices create repeated smaller problems; and a small set of variables completely describes what remains.
- **How deeply to understand it:** Be able to derive a state and transition aloud, write both memoized and tabulated standard solutions, choose a safe evaluation order, analyze the number of states and work per state, and optimize space only after the full recurrence is correct.

#### Why this priority level was assigned

DP questions are frequent, but the return on studying increasingly exotic variants drops quickly. Standard DP deserves serious practice; digit DP, profile DP, and highly dimensional state compression are specialized. Interview preparation should therefore go deep on a compact core rather than treat every recurrence as equally important.

**Study contract:** Reconstruct dense-array memoization, tabulation, and the standard core recurrences from a blank editor; understand state sufficiency and dependency order. Look up uncommon state encodings and advanced DP. The concrete implementations live here; [Code Templates](#24-code-templates) is the retrieval index.

### DP Priority Map

| Subtopic | Priority | Why this priority was assigned | Required depth |
|---|---|---|---|
| State, transition, and base cases | 🟠 Tier 2 — Very Important | These are the language of every DP solution; without them, memorized templates fail on variations. | Derive and explain from scratch |
| Memoization and tabulation | 🟠 Tier 2 — Very Important | Both are common and expose different correctness and implementation issues. | Implement both confidently |
| 1D DP and take/skip | 🟠 Tier 2 — Very Important | The most approachable and common DP family. | Deep |
| 2D and grid DP | 🟠 Tier 2 — Very Important | Frequently tests dependency order and boundary handling. | Deep |
| 0/1 knapsack-style DP | 🟠 Tier 2 — Very Important | Many selection, subset, and target problems reduce to it even when no “knapsack” is mentioned. | Medium–deep |
| Subsequence DP | 🟠 Tier 2 — Very Important | LCS/LIS-style states recur in sequence and string interviews. | Medium–deep |
| Space optimization | 🟠 Tier 2 — Very Important | A common follow-up and a test of whether dependencies are truly understood. | Standard 1D/2-row cases |
| Minimum Coin Change | 🟠 Tier 2 — Very Important | A compact, transferable test of unlimited reuse and impossible-state handling. | Derive and implement from memory |
| Unbounded counting variations | 🟡 Tier 3 — Nice to Know | Useful after minimum coins; loop nesting changes what is counted. | One variation, then reference |
| Interval DP | 🟡 Tier 3 — Nice to Know | Appears in harder interviews; the gap/length ordering is worth recognizing. | Concept plus one or two problems |
| DP on trees | 🟡 Tier 3 — Nice to Know | Useful in advanced tree interviews but ordinary DFS questions are much more common. | Basic two-state forms |
| Bitmask, digit, profile, and high-dimensional DP | ⚪ Tier 4 — Low Priority / Specialized | Rare in general SWE interviews and expensive to master. | Awareness unless role-specific |

### Focus First

1. Write a sentence that defines each state exactly.
2. Derive transitions from the choices available at that state.
3. Identify base cases and a valid evaluation order.
4. Convert a small recursive solution into memoization and tabulation.
5. Master 1D take/skip, grid, 0/1 subset, minimum Coin Change, LCS, and LIS patterns.
6. Compute complexity as **number of reachable states × work per state**.

### Learn Later

- Reconstructing an actual optimal choice, not just its value.
- Two-row and one-row space compression.
- Unbounded change-counting variations after minimum Coin Change.
- Interval DP and simple DP on trees.

### Optional / Specialized

- Bitmask DP, digit DP, profile DP, rerooting DP, convex-hull optimization, and other recurrence optimizations.
- DP with more than two or three independent dimensions unless the target company is known for algorithm-heavy interviews.

### 18.1 Core Intuition: Replace a Repeated Search with a State Graph

**Priority:** 🟠 Tier 2 — Very Important

Suppose a recursive search asks, “What is the best answer starting at index `i`?” Different earlier choices may arrive at the same `i`. If everything relevant about the future is captured by `i`, then recomputing the suffix is wasteful.

DP treats each distinct state as a node in a directed acyclic dependency graph:

```text
current state
   ├── choice A ──> smaller state
   └── choice B ──> smaller state

solve each distinct state once, then reuse its answer
```

Two properties usually make DP appropriate:

- **Optimal substructure:** An answer for a larger state can be built from correct answers to smaller states.
- **Overlapping subproblems:** The same smaller state is reached from more than one decision path.

Overlap explains the speedup, but state modeling is the real skill. A cache cannot repair an incomplete state. If the future also depends on remaining capacity, previous choice, or transaction count, that information must be represented too.

#### A state must be sufficient and minimal

- **Sufficient:** Two calls with the same state must have the same set of legal futures and therefore the same answer.
- **Minimal:** Do not store information that cannot affect future decisions; unnecessary dimensions inflate time and space.

For example, in 0/1 knapsack, `(item_index, remaining_capacity)` is sufficient. `item_index` alone is not, because two paths at the same item may have different remaining capacity.

### 18.2 How to Recognize DP

**Priority:** 🟠 Tier 2 — Very Important

#### Strong clues

| Problem language | Likely DP interpretation |
|---|---|
| “Number of ways” | Sum counts from predecessor states |
| “Is it possible?” | Boolean OR over choices |
| “Minimum/maximum cost, score, or operations” | Min/max over choices plus current contribution |
| “Choose items under a limit” | Knapsack-style state |
| “Subsequence” or matching two sequences | Prefix/index-pair state |
| “Path through a grid with restricted moves” | Cell or coordinate state |
| “Cannot choose adjacent/conflicting items” | Take/skip state |
| “Split an interval/sequence optimally” | Interval boundaries and a split point |
| “Same recursive arguments recur” | Memoization candidate |

#### Constraint clues

- A direct search has branching factor 2 or 3 and depth `n`, suggesting `O(2^n)` or worse.
- `n` is small enough for `O(n²)` but not exponential.
- A target, capacity, or sum is modest enough to be a DP dimension. This often produces **pseudo-polynomial** time such as `O(n × target)`.
- The problem requests only a value/count/feasibility result rather than all explicit solutions.

#### Signals that do **not** prove DP

- **Optimization alone:** A greedy proof may exist.
- **Recursion alone:** Tree DFS may visit every node once and have no overlapping subproblems.
- **Contiguous data:** Sliding window or prefix sums may be simpler.
- **All combinations must be returned:** Backtracking is necessary because output size itself may be exponential.
- **Shortest path:** BFS or Dijkstra is usually the clearer model; these algorithms can be viewed through DP ideas, but use the standard graph tool in an interview.

#### DP versus nearby approaches

| Situation | Prefer | Reason |
|---|---|---|
| Local choice can be proved globally safe | Greedy | Usually simpler and faster |
| Need every solution, arrangement, or path | Backtracking | DP normally aggregates rather than enumerates |
| Need one count/best value and states repeat | DP | Reuse collapses the search |
| Dependencies form an arbitrary graph with cycles | Graph algorithm | A plain DP evaluation order may not exist |
| Only a contiguous window changes monotonically | Sliding window | Avoid an unnecessary state table |

### 18.3 The Six-Part DP Design Process

**Priority:** 🟠 Tier 2 — Very Important

Use this process before writing code.

#### 1. Define the state in one sentence

Examples:

- `dp[i]` = maximum money obtainable from houses `0..i`.
- `dp[r][c]` = minimum cost to reach cell `(r, c)`.
- `dp[i][cap]` = maximum value using items from index `i` onward with `cap` capacity left.
- `dp[i][j]` = LCS length between prefixes `a[0..i)` and `b[0..j)`.

If the sentence is ambiguous about “up to,” “ending at,” “using exactly,” or “at most,” the implementation will probably be ambiguous too.

#### 2. List the choices and derive the transition

Write the decision in ordinary language first. For house robbery:

- Skip house `i` → solve the suffix from `i + 1`.
- Take house `i` → gain `nums[i]`, then solve from `i + 2`.

Therefore:

```text
best(i) = max(best(i + 1), nums[i] + best(i + 2))
```

Common aggregation operators:

| Objective | Transition aggregation |
|---|---|
| Count ways | Add (`+`) |
| Is any choice feasible? | Boolean OR |
| Must all conditions hold? | Boolean AND where appropriate |
| Minimum cost | `min(...)` |
| Maximum score | `max(...)` |

#### 3. Establish base cases

Base cases are the smallest valid states, not emergency patches added after testing.

- Empty suffix may have value `0` or one valid construction, depending on the question.
- An impossible state should use an impossible sentinel, such as `+∞`, `-∞`, or `false`, not always `0`.
- For counting, “one way to choose nothing” is often `1`; confusing this with zero is a common bug.

#### 4. Choose an evaluation order

- **Top-down:** Recursion discovers only reachable states; the call graph determines order.
- **Bottom-up:** Every dependency must already be computed. If `dp[i]` depends on `dp[i - 1]`, iterate forward. If it depends on `dp[i + 1]`, iterate backward.
- For interval DP, shorter intervals generally come before longer intervals.

#### 5. Identify the requested answer

The answer may be `dp[n]`, `dp[0]`, `max(dp)`, a sum of terminal states, or a reconstructed path. Do not assume the final cell is automatically the answer.

#### 6. Validate complexity and correctness

```text
time  = number of reachable states × work per state
space = stored states + recursion stack + reconstruction data
```

Then give a short correctness argument: every legal first choice is considered, each transition uses correct smaller results, and the min/max/sum/OR combines them according to the objective.

### 18.4 Correctness Intuition: Why the Recurrence Works

**Priority:** 🟠 Tier 2 — Very Important

A concise interview proof often uses induction over the dependency order.

1. **State meaning:** State exactly what `dp[s]` represents.
2. **Base:** Show the value is correct for the smallest state(s).
3. **Inductive assumption:** Assume every smaller dependency already has the correct answer.
4. **Exhaustiveness:** Show the transition covers every legal next or last choice.
5. **Optimality or counting:** Because dependencies are correct, taking `min`/`max` chooses the best legal choice, adding counts includes all disjoint cases, or OR detects any feasible case.
6. **Conclusion:** The requested state has the desired answer.

For a take/skip problem, every valid solution either takes the current item or skips it—never neither as a third unmodeled category. That mutually exclusive, collectively exhaustive split is the core correctness insight.

### 18.5 Memoization, Tabulation, and Space Optimization

**Priority:** 🟠 Tier 2 — Very Important

#### Memoization (top-down)

**Priority:** 🟠 Tier 2 — Very Important

Memoization preserves the recursive reasoning and caches each state on first evaluation.

```text
solve(state):
    if state is a base case: return its base value
    if state is cached: return cached value
    answer = identity for this objective
    for each legal choice:
        combine its contribution and solve(next state) into answer
    cache answer for state
    return answer
```

- **Use when:** The recurrence is easy to express recursively, only a subset of possible states is reachable, or iteration order is awkward.
- **Time:** `O(number of reachable states × work per state)`.
- **Space:** Cache plus recursion depth.
- **Common mistakes:** Caching too late, mutating data that participates in a key/hash, omitting a state variable from the key, and forgetting that recursion stack counts as auxiliary space.
- **Java choice:** Use a primitive array when states are dense integer indices. A `boolean[] computed` separates “uncomputed” from valid zero/negative answers; a safe sentinel is also fine. Use `HashMap<State, Value>` for sparse states, with immutable keys and correct equality/hashing.
- **Trade-off:** Recursion can throw `StackOverflowError`; Java does not promise tail-call elimination. Prefer tabulation for a deep linear dependency chain. The concrete [House Robber evolution](#186-worked-evolution-from-brute-force-to-optimized-dp) shows both execution strategies.

#### Tabulation (bottom-up)

**Priority:** 🟠 Tier 2 — Very Important

Tabulation explicitly fills states in a dependency-safe order.

```text
initialize the table, including base and impossible states
for state in dependency-safe order:
    for each legal choice:
        combine the contribution and solved dependency into table[state]
return the state or aggregate requested by the problem
```

- **Use when:** Most states are reachable, recursion depth is risky, or a compact iterative solution is straightforward.
- **Time:** Usually the full table size times work per state.
- **Space:** Full table unless compressed.
- **Common mistakes:** Wrong iteration direction, uninitialized impossible states behaving like valid zero states, and inconsistent indexing offsets.
- **Trade-off:** Predictable and often faster in practice, but it may compute unreachable states and can obscure the original choices.

#### Space optimization

**Priority:** 🟠 Tier 2 — Very Important

Only retain layers that future states can still read. If row `r` depends only on row `r - 1`, two rows—or sometimes one carefully updated row—are sufficient.

Do this in three steps:

1. Write the correct full recurrence and table.
2. Mark exactly which earlier states each update reads.
3. Compress storage and choose an update direction that does not overwrite needed values.

Space optimization can destroy reconstruction information and makes loop-direction bugs easier. Treat it as a follow-up, not the starting point.

#### Comparison

| Question | Memoization | Tabulation |
|---|---|---|
| Closest to recurrence | Yes | Sometimes |
| Computes only reachable states | Yes | Usually no |
| Uses call stack | Yes | No |
| Evaluation order | Implicit | Must be designed |
| Space compression | Less direct | Often natural |
| Easy reconstruction | Possible | Often easier with a full table |
| Interview starting point | Excellent for deriving | Excellent when pattern is familiar |

### 18.6 Worked Evolution from Brute Force to Optimized DP

**Priority:** 🟠 Tier 2 — Very Important

Use this systematic conversion path: **Brute Force → Recursion → Memoization → Tabulation → Optimization**.

#### Problem: maximum sum with no adjacent choices

Given nonnegative `int[] nums`, choose elements with no two adjacent and maximize their sum. Inputs are non-null; the empty input returns zero. Use `long` for the accumulated answer. The classic story is “House Robber,” but the transferable pattern is **take or skip under a local conflict**.

Example:

```text
nums = [2, 7, 9, 3, 1]
best choice = 2 + 9 + 1 = 12
```

#### Stage 1 — Brute-force subsets

Enumerate all `2^n` subsets, reject those containing adjacent indices, and retain the best sum.

- **Time:** `O(n × 2^n)` if each subset is validated in `O(n)`.
- **Space:** `O(n)` for the chosen subset.
- **Value:** It clarifies the complete search space and gives a correctness oracle for tiny tests.
- **Problem:** It explores many invalid subsets and repeats structurally identical suffix work.

#### Stage 2 — Express the decision as recursion

Let `best(i)` mean “maximum sum obtainable from index `i` through the end.” At each index, every valid solution either skips `i` or takes it and skips `i + 1`.

```java
static long robRecursive(int[] nums) {
    return robRecursiveFrom(nums, 0);
}

private static long robRecursiveFrom(int[] nums, int index) {
    if (index >= nums.length) return 0;
    long skip = robRecursiveFrom(nums, index + 1);
    long take = nums[index] + robRecursiveFrom(nums, index + 2);
    return Math.max(skip, take);
}
```

- **Time:** `O(2^n)` in the worst case.
- **Space:** `O(n)` recursion depth.
- **Improvement:** Invalid adjacent selections are never generated.
- **Remaining issue:** States such as `best(3)` are computed from multiple branches.

The recurrence tree exposes overlap:

```text
best(0)
├── best(1)
│   ├── best(2)
│   └── best(3)
└── best(2)       <- repeated
    ├── best(3)   <- repeated
    └── best(4)
```

#### Stage 3 — Memoize each index

```java
static long robMemo(int[] nums) {
    // -1 cannot be a valid answer when values are nonnegative.
    long[] memo = new long[nums.length];
    Arrays.fill(memo, -1);
    return robMemoFrom(nums, 0, memo);
}

private static long robMemoFrom(int[] nums, int index, long[] memo) {
    if (index >= nums.length) return 0;
    if (memo[index] != -1) return memo[index];
    long skip = robMemoFrom(nums, index + 1, memo);
    long take = nums[index] + robMemoFrom(nums, index + 2, memo);
    memo[index] = Math.max(skip, take);
    return memo[index];
}
```

- **States:** `n` meaningful indices.
- **Work per state:** `O(1)`.
- **Time:** `O(n)`.
- **Space:** `O(n)` cache plus `O(n)` recursion stack, still `O(n)` total.

#### Stage 4 — Re-express the recurrence as prefix tabulation

Define `dp[i]` as the maximum sum using the first `i` elements. Then:

```text
dp[i] = max(
    dp[i - 1],                 # skip element i - 1
    dp[i - 2] + nums[i - 1]    # take it
)
```

```java
static long robTable(int[] nums) {
    int n = nums.length;
    long[] dp = new long[n + 1];
    if (n > 0) dp[1] = nums[0];
    for (int i = 2; i <= n; i++) {
        dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i - 1]);
    }
    return dp[n];
}
```

- **Time:** `O(n)`.
- **Space:** `O(n)`.
- **Edge cases:** The extra prefix slot makes the empty input return `0` naturally.

For `[2, 7, 9, 3, 1]`, the table is:

| `i` elements considered | 0 | 1 | 2 | 3 | 4 | 5 |
|---:|---:|---:|---:|---:|---:|---:|
| `dp[i]` | 0 | 2 | 7 | 11 | 11 | 12 |

#### Stage 5 — Keep only dependencies that remain live

Each update reads only `dp[i - 1]` and `dp[i - 2]`.

```java
static long rob(int[] nums) {
    long twoBack = 0;
    long oneBack = 0;
    for (int value : nums) {
        long current = Math.max(oneBack, twoBack + value);
        twoBack = oneBack;
        oneBack = current;
    }
    return oneBack;
}
```

- **Time:** `O(n)`.
- **Auxiliary space:** `O(1)`.
- **Trade-off:** This version returns the optimal value but no longer preserves enough information to reconstruct which indices were selected.

#### What should transfer to a new problem

Do not memorize the variable names `twoBack` and `oneBack`. Retain this reasoning chain:

1. What choices partition all valid solutions?
2. What smaller state follows each choice?
3. Which states repeat?
4. In what order can dependencies be evaluated?
5. Which old states remain necessary after each update?

### 18.7 Complexity Analysis for DP

**Priority:** 🟠 Tier 2 — Very Important

The safest formula is:

```text
time = number of distinct states × transitions tried per state × work per transition
space = stored states + recursion stack + output/reconstruction storage
```

Examples:

| DP | State count | Work/state | Time | Typical space |
|---|---:|---:|---:|---:|
| House Robber | `n` | `1` | `O(n)` | `O(n)` or `O(1)` |
| Grid path | `rows × cols` | `1` | `O(rows × cols)` | `O(rows × cols)` or `O(cols)` |
| 0/1 Knapsack | `n × capacity` | `1` | `O(n × capacity)` | `O(n × capacity)` or `O(capacity)` |
| LCS | `m × n` | `1` | `O(mn)` | `O(mn)` or `O(min(m,n))` |
| LIS, basic DP | `n` | scan up to `n` predecessors | `O(n²)` | `O(n)` |
| Interval split DP | `n²` intervals | up to `n` splits | `O(n³)` | `O(n²)` |
| Tree include/exclude | `n` nodes × constant states | degree across traversal | `O(n)` | `O(n)` stack worst case |

`O(n × target)` is pseudo-polynomial: it is polynomial in the numeric target value, not in the number of bits required to encode that value. If `target` can be a billion, this DP is not practical.

### 18.8 1D DP: Linear Decisions and Take/Skip

**Priority:** 🟠 Tier 2 — Very Important

#### Intuition and recognition

Use 1D DP when a left-to-right or right-to-left sequence of states is sufficient and each state depends on a small set of earlier or later positions.

Typical clues:

- “Ways to reach step `n`” with allowed jumps.
- “Minimum cost to reach the end.”
- “Maximum sum with adjacent choices forbidden.”
- “Can this prefix be segmented?”
- “Best answer ending at index `i`” or “using the first `i` values.”

Common state meanings are not interchangeable:

- `dp[i]` = answer **for prefix** `0..i`.
- `dp[i]` = answer **ending exactly at** `i`.
- `dp[i]` = answer **starting from** `i`.

The requested answer might be the last state, the maximum of all ending states, or a combination of terminal states.

#### Example: minimum cost to climb beyond the last step

If you can move one or two steps and pay the cost of each step you land on, define `dp[i]` as the minimum cost to stand on step `i`.

```java
static long minCostClimbingStairs(int[] cost) {
    long twoBack = 0;
    long oneBack = 0;
    for (int stepCost : cost) {
        long current = stepCost + Math.min(twoBack, oneBack);
        twoBack = oneBack;
        oneBack = current;
    }
    return Math.min(twoBack, oneBack);
}
```

- **Time:** `O(n)`.
- **Auxiliary space:** `O(1)`; a full table would use `O(n)`.
- **Contract:** Non-null, nonnegative costs; start at index 0 or 1 and move one or two positions. The top is index `cost.length`. This extension returns zero for empty or one-element input because you can start at or beyond the top; confirm the platform contract (often `n ≥ 2`).
- **Alternative:** Memoized recursion is often easier to derive but uses `O(n)` stack space.

#### Common 1D variations

| Pattern | State idea | Transition idea |
|---|---|---|
| Count ways | Ways to reach position `i` | Sum ways from legal predecessors |
| Minimum steps/cost | Minimum cost to reach `i` | Minimum predecessor cost plus current cost |
| Take/skip | Best value using first `i` choices | Max of skipping versus taking plus compatible prefix |
| Word segmentation | Whether prefix `s[0..i)` is buildable | Try a prior split `j` and test substring `s[j..i)` |
| Best ending here | Best value for a structure ending at `i` | Extend compatible earlier endings |

#### Common mistakes and trade-offs

- Returning `dp[dp.length - 1]` when the answer is the maximum over all ending states; maintain a running maximum.
- Mixing an “index in the input” state with a “prefix length” state, causing an off-by-one error.
- Initializing a minimum-cost table with zero; unreachable states should normally start at infinity.
- Compressing to variables before checking which old value each variable represents.
- Assuming `O(n)` is automatic: if each `dp[i]` scans all prior indices, time is `O(n²)`.

### 18.9 2D and Grid DP

**Priority:** 🟠 Tier 2 — Very Important

#### Intuition and recognition

A 2D table is useful when two independent coordinates describe a subproblem:

- Current row and column in a grid.
- Prefix length from each of two strings.
- Item index and remaining capacity.
- Two positions or two boundaries.

For a grid with moves only right and down, cells form a DAG. A cell depends on the cell above and to the left; row-major order is therefore safe.

#### Example: minimum path sum

Use boundary cases directly instead of adding costs to an infinity sentinel. The input is read-only by convention; Java does not enforce that through an array parameter.

```java
static long minPathSum(int[][] grid) {
    if (grid.length == 0 || grid[0].length == 0) return 0;
    int cols = grid[0].length;
    long[] dp = new long[cols];
    for (int row = 0; row < grid.length; row++) {
        for (int col = 0; col < cols; col++) {
            if (row == 0 && col == 0) {
                dp[col] = grid[row][col];
            } else if (row == 0) {
                dp[col] = dp[col - 1] + grid[row][col];
            } else if (col == 0) {
                dp[col] += grid[row][col];
            } else {
                dp[col] = Math.min(dp[col], dp[col - 1]) + grid[row][col];
            }
        }
    }
    return dp[cols - 1];
}
```

- **State:** During row `r`, `dp[c]` becomes the minimum cost to reach `(r, c)`.
- **Time:** `O(rows × cols)`.
- **Auxiliary space:** `O(cols)` rather than `O(rows × cols)`.
- **Why one row works:** Before update, `dp[c]` is the value from above; after updating `c - 1`, `dp[c - 1]` is the value from the left.
- **Contract:** Non-null rectangular grid with no obstacles; empty dimensions return zero. Both start and end costs count. The code handles negative cell values because moves are acyclic. For obstacles, introduce and guard an unreachable state instead of applying this recurrence unchanged.

#### Grid DP versus graph traversal

- Restricted acyclic moves, aggregated counts, or min/max path value → DP is natural.
- Four-direction movement with possible cycles → use BFS, Dijkstra, or DFS plus visited state.
- Shortest path in an unweighted grid → BFS, not a hand-built grid DP, unless movement makes a clear DAG.

#### Common mistakes and trade-offs

- Using a zero sentinel for blocked or unreachable cells.
- Overwriting a one-row table in the wrong direction.
- Counting paths through an obstacle because its previous value was not reset.
- Forgetting that diagonal movement changes the dependencies.
- Assuming negative weights are harmless: a restricted DAG grid remains valid, but arbitrary cyclic movement can create a fundamentally different shortest-path problem.

### 18.10 Knapsack-Style DP

**Priority:** 🟠 Tier 2 — Very Important

#### 0/1 knapsack

**Priority:** 🟠 Tier 2 — Very Important

Each item can be selected at most once. With weight `w` and value `v`:

```text
best(i, cap) = max(
    best(i + 1, cap),                    # skip item i
    v[i] + best(i + 1, cap - w[i])       # take if it fits
)
```

The direct table has `n × capacity` states and `O(1)` work per state.

##### One-dimensional implementation

Contract: non-null equal-length arrays, nonnegative weights, and optional empty selection. Values may be negative; skipping them is allowed. Capacity must be small enough for the table. The method does not mutate inputs.

```java
static long knapsack01(int[] weights, int[] values, int capacity) {
    if (weights.length != values.length || capacity < 0
            || capacity == Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Invalid knapsack input");
    }
    long[] dp = new long[capacity + 1];
    for (int item = 0; item < weights.length; item++) {
        int weight = weights[item];
        if (weight < 0) throw new IllegalArgumentException("Negative weight");
        // Descending order prevents this item from being reused.
        for (int cap = capacity; cap >= weight; cap--) {
            dp[cap] = Math.max(dp[cap], dp[cap - weight] + values[item]);
        }
    }
    return dp[capacity];
}
```

- **State:** `dp[cap]` is the best value for capacity at most `cap` after processed items.
- **Time:** `O((n + 1) × (capacity + 1))`, including initialization, zero capacity, and zero-weight items; conventionally `O(n × capacity)` for positive sizes.
- **Auxiliary space:** `O(capacity + 1)`.
- **Critical detail:** Capacity moves downward. Moving upward would read a value already updated by the current item and accidentally allow unlimited copies.

##### Boolean subset sum

```java
static boolean canMakeSum(int[] nums, int target) {
    if (target < 0) return false;
    if (target == Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Target is too large for this table");
    }
    boolean[] possible = new boolean[target + 1];
    possible[0] = true;
    for (int value : nums) {
        if (value < 0) throw new IllegalArgumentException("Negative value");
        for (int total = target; total >= value; total--) {
            possible[total] = possible[total] || possible[total - value];
        }
    }
    return possible[target];
}
```

- **Time:** `O((n + 1) × (target + 1))`, including initialization; conventionally `O(n × target)` for positive sizes.
- **Auxiliary space:** `O(target + 1)`.
- **Edge cases:** Target zero is normally feasible via the empty subset; zero-valued items need care in counting versions; negative values invalidate this simple index-by-sum model.

#### Unbounded knapsack

**Priority:** 🟠 Tier 2 for minimum Coin Change; 🟡 Tier 3 for further counting variations

Each item may be selected repeatedly. The one-dimensional capacity loop usually moves **upward**, allowing the current item’s newly updated state to be reused:

```java
static int minCoins(int[] coins, int amount) {
    if (amount < 0) return -1;
    if (amount == Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Amount is too large for this table");
    }
    int unreachable = amount + 1; // Any feasible answer uses at most amount coins.
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, unreachable);
    dp[0] = 0;
    for (int coin : coins) {
        if (coin <= 0) throw new IllegalArgumentException("Nonpositive coin");
        for (int total = coin; total <= amount; total++) {
            if (dp[total - coin] != unreachable) {
                dp[total] = Math.min(dp[total], dp[total - coin] + 1);
            }
        }
    }
    return dp[amount] == unreachable ? -1 : dp[amount];
}
```

- **Time:** `O((numberOfCoins + 1) × (amount + 1))` including validation and initialization; conventionally `O(numberOfCoins × amount)` for positive sizes.
- **Auxiliary space:** `O(amount + 1)`. The amount must be practical for a dense table; rejecting arithmetic overflow is not a memory-capacity guarantee.

#### Loop order changes the meaning

This is a frequent interview trap:

| Goal | Outer loop | Inner loop | Direction / consequence |
|---|---|---|---|
| 0/1 selection | Items | Capacity | Descending; use each item at most once |
| Unlimited selection | Items | Capacity | Ascending; current item may be reused |
| Count unordered coin combinations | Coins | Totals | Each multiset counted once |
| Count ordered sequences | Totals | Choices | Different orders can count separately |

Always state what a partially filled `dp` array means after each outer-loop iteration. That invariant determines loop order more reliably than memorizing arrows.

#### Recognition, alternatives, and trade-offs

- **Clues:** Select items, each once or unlimited times; capacity/target/budget; exact or at-most sum; count/feasibility/minimum/maximum objective.
- **Alternative:** A `HashSet<Long>` of reachable sums can help when reachable states are sparse but sums are large. Snapshot the previous set or build a new set for each 0/1 item; modifying it during iteration is invalid and can also reuse the same item.
- **Meet-in-the-middle:** May be better when item count is around 30–40 but values/target are huge.
- **Greedy warning:** Choosing the largest value/weight ratio solves fractional knapsack, not general 0/1 knapsack. Standard coin systems can hide the fact that greedy coin choice is not universally correct.
- **Pseudo-polynomial warning:** Check the numeric capacity before proposing `O(n × capacity)`.

### 18.11 Subsequence DP

**Priority:** 🟠 Tier 2 — Very Important

#### Subsequence versus substring/subarray

- A **subsequence** preserves order but may skip elements.
- A **substring/subarray** must be contiguous.

This distinction changes the approach. Sliding window often helps contiguous segments; subsequence questions frequently need DP, greedy reasoning, or binary search.

#### Longest Common Subsequence (LCS): prefix-versus-prefix

Define `dp[i][j]` as the LCS length of prefixes `a[0..i)` and `b[0..j)`.

```java
static int lcsLength(String a, String b) {
    // Assigning a String reference does not copy its characters.
    String rows = a.length() >= b.length() ? a : b;
    String cols = a.length() >= b.length() ? b : a;
    int[] previous = new int[cols.length() + 1];
    int[] current = new int[cols.length() + 1];
    for (int i = 1; i <= rows.length(); i++) {
        current[0] = 0;
        for (int j = 1; j <= cols.length(); j++) {
            if (rows.charAt(i - 1) == cols.charAt(j - 1)) {
                current[j] = previous[j - 1] + 1;
            } else {
                current[j] = Math.max(previous[j], current[j - 1]);
            }
        }
        int[] swap = previous;
        previous = current;
        current = swap;
    }
    return previous[cols.length()];
}
```

- **Transition:** Matching final characters can extend a smaller match; otherwise at least one final character is excluded.
- **Time:** `O(mn)` for nonempty strings, where `m = a.length()` and `n = b.length()`; initialization/loop overhead gives `O((m + 1)(n + 1))` including empty inputs.
- **Auxiliary space:** `O(min(m, n) + 1)`. Two independently allocated arrays are reused; swapping their references does not copy their contents.
- **Trade-off:** Two-row compression returns only the length. Reconstructing a sequence is easiest with the full `O(mn)` table or a more advanced reconstruction method.
- **Common mistake:** Reading the current row where the recurrence needs the previous diagonal, or accidentally making both row variables refer to the same array.
- **Character contract:** This solution compares UTF-16 `char` units. For code-point subsequences, convert with `s.codePoints().toArray()` and account for those extra arrays.

LCS modeling also appears in edit distance, deletions needed to equalize strings, and sequence alignment. The transition differs, but the index-pair state is shared.

#### Longest Increasing Subsequence (LIS): best ending at each index

Define `dp[i]` as the LIS length ending exactly at index `i`:

```java
static int lisLengthQuadratic(int[] nums) {
    int[] dp = new int[nums.length];
    Arrays.fill(dp, 1);
    int best = 0;
    for (int i = 0; i < nums.length; i++) {
        for (int j = 0; j < i; j++) {
            if (nums[j] < nums[i]) {
                dp[i] = Math.max(dp[i], dp[j] + 1);
            }
        }
        best = Math.max(best, dp[i]);
    }
    return best;
}
```

- **Time:** `O(n²)`.
- **Space:** `O(n)`.
- **Why answer is `max(dp)`:** The optimal subsequence may end before the last input element.
- **Edge case:** Replace `<` with `<=` only if the definition asks for non-decreasing rather than strictly increasing.

The advanced `O(n log n)` method stores the smallest possible tail for each length and uses lower-bound binary search. It is excellent to know after the `O(n²)` state is understood, but the `tails` array is not itself necessarily an actual LIS.

```java
static int lisLength(int[] nums) {
    int[] tails = new int[nums.length];
    int size = 0;
    for (int value : nums) {
        int left = 0;
        int right = size;
        // First tail >= value: equal values must not extend a strict LIS.
        while (left < right) {
            int middle = left + (right - left) / 2;
            if (tails[middle] < value) left = middle + 1;
            else right = middle;
        }
        tails[left] = value;
        if (left == size) size++;
    }
    return size;
}
```

- **Time:** `O(n log n)`.
- **Space:** `O(n)`.
- **Alternative:** Use the quadratic DP when reconstruction or explanation simplicity matters and constraints allow it.

#### Common subsequence mistakes

- Confusing “ending at `i`” with “best within prefix `i`.”
- Forgetting the empty-prefix row/column in two-string DP.
- Updating a compressed row in a direction that overwrites diagonal data.
- Treating duplicates incorrectly in strict versus non-strict LIS.
- Reconstructing from a space-compressed table without retaining parents or decisions.

### 18.12 State Compression and Extra State

**Priority:** 🟠 Tier 2 — Very Important

Sometimes the current index is not sufficient. Add the smallest state that distinguishes legal futures.

Examples:

| Problem restriction | Possible extra state |
|---|---|
| At most `k` transactions | Transactions remaining |
| Cannot repeat the previous action | Previous action or whether it is allowed |
| Stock holding rules | Holding/not holding and cooldown status |
| Match two sequences | Second sequence index |
| Use a limited resource | Remaining capacity/budget |
| Paths with exactly `k` special moves | Special moves remaining |

Before adding a dimension, ask whether it can be derived from existing state. For example, a day index and transaction count may imply which action parity is next, making a redundant “buy/sell” flag unnecessary in some formulations.

Every added dimension multiplies the state space. If state is `(i, j, k)` with ranges `n`, `m`, and `k`, storage is typically `O(nmk)`, not `O(n + m + k)`.

### 18.13 Interval DP

**Priority:** 🟡 Tier 3 — Nice to Know

#### Intuition and recognition

Define a state for a contiguous interval, usually `dp[left][right]`. A transition then:

- removes or chooses one endpoint;
- splits at a middle position; or
- chooses which operation happens first or last inside the interval.

Strong clues include:

- “Best way to parenthesize or split this range.”
- “Choose an element whose removal joins its neighbors.”
- “Minimum cost to combine a sequence.”
- “Longest palindromic subsequence within `left..right`.”

Shorter intervals must usually be solved before longer intervals. The common complexity is `O(n²)` states and either `O(1)` or `O(n)` work per state.

#### Example: choose the final action in each interval

Contract: nonnegative balloon values, practical `n` for an `O(n²)` table, and all products/totals fit in `long`. Empty input returns zero. In the “Burst Balloons” model, choosing the first balloon is awkward because its neighbors change. Choose the **last** balloon burst inside an interval; then its two outside neighbors are fixed and the left/right subintervals are independent.

```java
static long maxCoins(int[] nums) {
    int n = nums.length + 2;
    long[] values = new long[n];
    values[0] = values[n - 1] = 1;
    for (int i = 0; i < nums.length; i++) values[i + 1] = nums[i];
    // dp[left][right] covers the open interval (left, right).
    long[][] dp = new long[n][n];
    for (int width = 2; width < n; width++) {
        for (int left = 0; left + width < n; left++) {
            int right = left + width;
            for (int last = left + 1; last < right; last++) {
                long gain = values[left] * values[last] * values[right];
                dp[left][right] = Math.max(dp[left][right],
                        dp[left][last] + gain + dp[last][right]);
            }
        }
    }
    return dp[0][n - 1];
}
```

- **Time:** `O(n³)`.
- **Space:** `O(n²)`.
- **Correctness intuition:** Every complete strategy has exactly one final burst in `(left, right)`. Trying every possible final balloon covers every strategy, and once it is fixed, actions in the two subintervals do not interfere.
- **Common mistakes:** Iterating long intervals before short dependencies, mixing inclusive and exclusive boundaries, and attempting to model changing neighbors with only the original index.
- **Alternative:** Memoized recursion on `(left, right)` expresses the same split and may be easier to derive.

Other representative interval states include longest palindromic subsequence (`O(n²)`) and matrix-chain multiplication (`O(n³)`). Understanding one endpoint recurrence and one split recurrence is sufficient for most general interview preparation.

### 18.14 DP on Trees

**Priority:** 🟡 Tier 3 — Nice to Know

#### Intuition and recognition

Tree DP is usually a postorder DFS where each node returns a small summary of its subtree. Parent decisions combine child summaries.

Typical clues:

- A choice at a node restricts choices at its children.
- Need a minimum/maximum/count over a tree, not merely traversal output.
- Subtree results are independent once the parent state is fixed.

The parent relationship prevents revisiting in a tree; memoization by node is not what makes this DP. The DP aspect is the multiple values returned for different parent-child conditions.

#### Example: include/exclude a node

For a tree where directly connected nodes cannot both be selected, return two values:

- `skip`: best subtree value if this node is not selected.
- `take`: best subtree value if this node is selected.

```java
// Uses the TreeNode type from section 12: int val; TreeNode left, right.
record RobState(long skip, long take) {}
private static final RobState EMPTY_ROB_STATE = new RobState(0, 0);

static long maxNonAdjacentTreeSum(TreeNode root) {
    RobState result = treeRobState(root);
    return Math.max(result.skip(), result.take());
}

private static RobState treeRobState(TreeNode node) {
    if (node == null) return EMPTY_ROB_STATE;
    RobState left = treeRobState(node.left);
    RobState right = treeRobState(node.right);
    long take = node.val + left.skip() + right.skip();
    long skip = Math.max(left.skip(), left.take())
            + Math.max(right.skip(), right.take());
    return new RobState(skip, take);
}
```

- **Time:** `O(n)` because each node is processed once.
- **Auxiliary space:** `O(h)` live recursion and subtree summaries, worst-case `O(n)` on a skewed tree; `O(n)` records are allocated across the traversal. Iterative postorder avoids call-stack overflow but needs its own state storage.
- **Correctness intuition:** Taking a node forces every child into its skip state. Skipping it lets each child independently choose its better state.
- **Edge cases:** Empty tree, negative values (is choosing nothing allowed?), and a skewed tree that can overflow the call stack.
- **Alternative:** A map keyed by a node reference plus the parent-selection flag can memoize a different formulation, but adds hashing/state overhead. The postorder summary visits each node once and needs no map.

> 🌐 **Java Backend Relevance — MEDIUM:** `RobState` is an immutable record whose named accessors make a small result easier to read. A short `long[]` is possible in an interview; records help when result fields have distinct meanings. Immutability of a record is shallow when a component itself is mutable.

More advanced forms—rerooting, many states per node, or DP on arbitrary tree decompositions—are **⚪ Tier 4 — Low Priority / Specialized** for general SWE interviews.

### 18.15 Value, Feasibility, Counting, and Reconstruction

**Priority:** 🟠 Tier 2 — Very Important

The same state graph may support different questions, but the identity values and combining operation change.

| Question type | Typical stored value | Base/identity concern | Combine choices |
|---|---|---|---|
| Feasibility | Boolean | Empty construction is often `true` | OR |
| Count | Integer | Empty construction is often one way | Sum |
| Minimum | Number | Unreachable should be `+∞` | Min |
| Maximum | Number | Impossible may be `-∞`, not zero | Max |
| Construct one solution | Parent/choice plus value | Preserve predecessor | Follow parents backward |
| Return all solutions | Usually backtracking | Output may be exponential | Enumerate, perhaps with DP pruning |

#### Reconstructing an optimal solution

If an interviewer asks which choices form the optimum, keep either:

- a `parent[state]` index/reference or `choice[state]` value while filling the table; or
- the full value table and walk backward by checking which transition could have produced the current value.

Reconstruction usually adds `O(number of states)` storage even when the value-only DP could be compressed. Say this trade-off explicitly.

#### Counting safely

- Determine whether order matters: `[1, 2]` versus `[2, 1]` may be one combination or two sequences.
- Determine whether duplicates are distinct items.
- If the problem requests modulo arithmetic, apply the modulus during transitions.
- Java `int` and `long` silently wrap on overflow. Use `long` when its bound is sufficient, apply the requested modulus during transitions, or use `java.math.BigInteger` when an exact unbounded count is required. Widen operands before multiplication; a later cast cannot repair an overflowed `int` expression.

### 18.16 Representative DP Problems

Start with **10–12 distinct core problems**, expanding only when a mistake or transfer gap justifies another. A ladder is a progression, not a requirement to complete every row at once. A cold revisit is the same problem solved without hints after a delay; it does not count as a new problem. See [How to Learn DSA Effectively](#25-how-to-learn-dsa-effectively) for the shared review schedule.

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Linear / take-skip | Climbing Stairs | ⭐ **Canonical Interview Problem:** House Robber | House Robber II: handle the circular boundary | Delete and Earn: group values, then take/skip | House Robber; derive all stages and justify live dependencies |
| Grid | Unique Paths | ⭐ **Canonical Interview Problem:** Minimum Path Sum | Unique Paths II: reset blocked cells | Longest Increasing Path in a Matrix (later): grid DFS plus memoization | Minimum Path Sum with one row, one column, and negative cells |
| 0/1 subset | Small boolean Subset Sum | ⭐ **Canonical Interview Problem:** Partition Equal Subset Sum | Target Sum: count assignments and inspect zero/parity cases | Last Stone Weight II: reduce minimization to a partition | Partition; explain descending capacity without a memorized rule |
| Unlimited reuse | Hand-trace minimum coins for `[1, 3, 4]`, amount `6` | ⭐ **Canonical Interview Problem:** Coin Change | Coin Change II (Tier 3): unordered counts | Word Break: prefix DP plus dictionary membership | Coin Change with impossible target and no coin of value 1 |
| Two sequences | LCS table for `"ab"` and `"ac"` | ⭐ **Canonical Interview Problem:** Longest Common Subsequence | Edit Distance: operation-based transitions | Delete Operation for Two Strings: relate deletions to LCS | LCS; reconstruct row dependencies before compression |
| Increasing subsequence | Quadratic LIS on a tiny array | ⭐ **Canonical Interview Problem:** Longest Increasing Subsequence | Non-decreasing variant: change the bound condition | Russian Doll Envelopes (later): sorting/tie policy plus LIS | LIS; explain why `tails` is sufficient and is not the actual sequence |

**Java cost check for Word Break:** A naive scan of all splits with `dictionary.contains(s.substring(j, i))` can take `O(n³)` time: `O(n²)` candidates each copy/hash up to `O(n)` UTF-16 units. Limit candidate lengths to dictionary word lengths, or compare words against positions without creating substrings; explain the resulting cost for the chosen implementation.

**Optional expansion after the core:** Decode Ways (zero/boundary counting), Stock with Cooldown (small state machine), Distinct Subsequences (counting and overflow). For Tier 3 interval/tree DP, do Longest Palindromic Subsequence then Burst Balloons, or House Robber III after tree postorder. Regular Expression Matching remains an optional advanced exercise.

For each attempted problem, derive the state before coding, trace one tiny state graph, justify transitions and order, and record the rule behind any mistake. On a revisit, remove the topic label and explain why a competing greedy, window, or graph method does or does not apply.

### 18.17 Common DP Mistakes and Prevention

| Mistake | Symptom | Prevention |
|---|---|---|
| Vague state definition | Transitions mix prefixes, suffixes, or exact/at-most meanings | Write one precise sentence before code |
| Missing state variable | Memo returns a result that is valid for one path but not another | Ask whether equal cache keys always have identical legal futures |
| Redundant state | Memory/time explodes | Ask whether a dimension is derivable from the others |
| Incorrect base case | Empty or size-one tests fail; counts are all zero | Derive the smallest mathematical subproblem first |
| Treating impossible as zero | Invalid path wins a min/max transition | Use `+∞`, `-∞`, or `false` as appropriate |
| Wrong table order | Reads default or current-layer values | Draw dependency arrows before loops |
| Wrong 1D update direction | A 0/1 item is reused or valid reuse is blocked | State the per-iteration invariant; then choose direction |
| Counting combinations as permutations | Counts are too large | Decide whether order matters and choose loop nesting accordingly |
| Blind space compression | Values change after in-place update | Keep the full table first and label old versus new dependencies |
| Forgetting recursion stack | Claims `O(1)` extra space for memoization | Count cache and maximum call depth |
| Incorrect complexity | Says `O(n)` despite an inner predecessor/split loop | Count states and transitions per state separately |
| Greedy assumption without proof | Passes examples but misses a counterexample | Compare local choice with take/skip recurrence; require an exchange argument for greedy |
| Integer overflow | Count becomes negative or wraps | Estimate maximum count; use wider integers/modulo when required |
| Accidental mutation | A reference to a row is reused or modified unexpectedly | Allocate independent rows and be explicit about references versus copies |
| Reconstructing after compression | Optimal value exists but choices are lost | Retain parent choices or full table when output needs a witness |

#### Edge-case checklist

- Empty input and one element.
- Target/capacity zero.
- Impossible target.
- All values zero; all negative if permitted.
- Duplicate values or duplicate choices.
- One-row/one-column grid and blocked start/end.
- Strict versus non-strict ordering.
- Exact versus at-most capacity.
- Whether the empty choice is permitted.
- Counts large enough to overflow.
- Recursion depth on maximum input.

### 18.18 Interview Communication for DP

A strong explanation can be concise:

> “Brute force makes two choices at each index, so it is exponential. The future depends only on the index and remaining capacity, and those states repeat. Let `dp(i, cap)` be the best value from items `i..n-1` with `cap` remaining. I either skip item `i`, or take it if it fits and move to `i+1` with reduced capacity. The base case is `i == n`. There are `n × capacity` states and constant work per state, so time and space are `O(n × capacity)`. I’ll start top-down, then discuss one-row compression.”

Before coding, explicitly give:

1. State meaning.
2. Choices and recurrence.
3. Base cases.
4. Evaluation order or memo key.
5. Answer location.
6. Time and space.

During testing, trace a tiny example through states rather than only checking the final output.

### 18.19 DP Mastery Checklist

I have mastered the interview-relevant DP core when I can:

- [ ] Explain optimal substructure and overlapping subproblems without claiming every recursive problem needs DP.
- [ ] Define a sufficient, minimal state in one precise sentence.
- [ ] Derive choices, transition, base cases, and answer state before coding.
- [ ] Explain correctness using exhaustive choices and correct smaller states.
- [ ] Turn exponential recursion into memoization and compute its state-space complexity.
- [ ] Convert a familiar memoized recurrence to a dependency-safe table.
- [ ] Implement 1D take/skip, grid, 0/1 subset, coin, LCS, and basic LIS patterns without copying.
- [ ] Explain why capacity loop direction differs for 0/1 and unbounded selection.
- [ ] Distinguish feasibility, count, min, and max initialization.
- [ ] Analyze time as states times transitions and include recursion stack in space.
- [ ] Space-optimize a one-dimensional or row-based recurrence without overwriting dependencies.
- [ ] Recognize when greedy, sliding window, BFS, or backtracking is more appropriate.
- [ ] Solve standard medium DP problems and explain trade-offs aloud.
- [ ] Re-solve representative problems after a delay and adapt the state when a constraint changes.

## 19. Tries

**Priority:** 🟡 Tier 3 — Nice to Know

### Topic Overview

- **What it is:** A trie (prefix tree) stores strings character by character. Strings with the same prefix share the same path from the root.
- **Why it exists:** A `HashSet<String>` answers “is this complete word present?” well, but does not naturally answer “does any stored word begin with this prefix?” A trie supports both queries incrementally.
- **Why it matters in interviews:** Tries appear in autocomplete, dictionary, wildcard, prefix-replacement, and board word-search problems. They also test whether a candidate can combine a data structure with DFS/backtracking.
- **Interview priority:** **🟡 Tier 3 — Nice to Know.** Tries are useful and recognizable but much less frequent than hashing, trees, graphs, and core sequence patterns. Implement the standard form and understand trie-guided pruning; do not overinvest in compressed or persistent variants.
- **Prerequisites:** Hash maps or fixed arrays, strings, tree traversal, recursion, and backtracking for board search.
- **Common use cases:** Exact lookup, prefix existence, autocomplete candidates, longest matching prefix, wildcard dictionary search, and pruning many simultaneous string searches.
- **Common problem patterns:** Implement a dictionary, replace words by roots, search with `.` wildcards, find several words in a grid, or compute prefix scores.
- **How to recognize it:** Many strings are queried by prefix, searches proceed character-by-character, or a brute-force search repeats the same prefix checks across a large dictionary.
- **How deeply to understand it:** Implement insert, exact search, and prefix search; explain child-storage trade-offs; augment nodes with useful metadata; and combine a trie with backtracking for multiword search.

**Study contract:** Implement the three basic operations from memory after 3–4 representative problems. Understand prefix pruning; look up deletion and specialized trie variants. The following snippets use non-null inputs and UTF-16 units unless a narrower alphabet is stated.

### Why This Priority Was Assigned

A `HashSet<String>` is simpler for exact-word membership, and a sorted `String[]` plus binary search can answer some offline prefix queries. A trie earns its space cost only when prefixes are first-class operations or when shared-prefix pruning saves repeated work. This narrower applicability makes it Tier 3 for general SWE interviews.

### Focus First

- Node children, end-of-word marker, and root sentinel.
- `insert`, `search`, and `startsWith` in `O(L)` time for a length-`L` key.
- The difference between a word being present and merely being a valid prefix.
- Trie-guided DFS for searching many dictionary words at once.
- Choosing hash-map children versus a fixed alphabet array.

### Learn Later

- Wildcard search by branching over children.
- Storing counts, word IDs, scores, or the complete word at terminal nodes.
- Deleting a word and pruning nodes that are no longer shared.
- Autocomplete traversal and ranking metadata.

### Optional / Specialized

- Radix/Patricia tries, ternary search trees, persistent tries, binary XOR tries, Aho–Corasick automata, suffix tries, and production-grade Unicode normalization.

### 19.1 Core Intuition and Invariant

**Priority:** 🟡 Tier 3 — Nice to Know

Each root-to-node path spells a prefix. A node can be both:

- the end of a complete stored word; and
- the parent of longer stored words.

For example, storing `app` and `apple` requires the node for the second `p` to be terminal while still having a child `l`. Never infer “complete word” from “node has no children.”

```text
(root)
  └─ a
     └─ p
        └─ p  [word: app]
           └─ l
              └─ e  [word: apple]
```

The central invariant is:

> After consuming the first `i` characters of a query, `node` represents exactly that prefix. If the next child is absent, no stored word has the requested prefix.

### 19.2 Standard Trie Implementation

**Priority:** 🟡 Tier 3 — Nice to Know

```java
static final class TrieNode {
    final Map<Character, TrieNode> children = new HashMap<>();
    boolean isWord;
    String word; // Optional terminal payload, used only by board search below.
}

static final class Trie {
    private final TrieNode root = new TrieNode();

    private TrieNode findNode(String text) {
        TrieNode node = root;
        for (int i = 0; i < text.length(); i++) {
            node = node.children.get(text.charAt(i));
            if (node == null) return null;
        }
        return node;
    }

    void insert(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            node = node.children.computeIfAbsent(ch, key -> new TrieNode());
        }
        node.isWord = true;
    }

    boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isWord;
    }

    boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }
}
```

#### What changes from problem to problem

- The alphabet and child representation.
- Terminal metadata: `isWord`, count, index, score, or stored full word.
- Whether duplicates increment a frequency.
- Whether search allows wildcards, substitutions, or approximate matches.
- Whether deletion and cleanup are required.

#### Complexity

Let `L` be the key length and `S` the total number of characters inserted.

| Operation | Expected time with hash-map children | Extra space |
|---|---:|---:|
| Insert | `O(L)` | Up to `O(L)` new nodes |
| Exact search | `O(L)` | `O(1)` iterative |
| Prefix search | `O(L)` | `O(1)` iterative |
| Build from all words | `O(S)` | `O(S)` nodes worst case |

Character hash-map operations are expected `O(1)`. Fixed-size child arrays also give `O(1)` indexing, but the constant memory cost is paid at every node.

The table uses nonempty-key shorthand. An empty-key operation is `O(1)`; building from `W` words takes `O(S + W)` time if empty words are allowed, with the root taking constant space even when `S = 0`.

#### Child-storage trade-offs

| Representation | Prefer when | Advantages | Costs |
|---|---|---|---|
| `HashMap<Character, TrieNode>` | Alphabet is large or nodes are sparse | Stores only actual edges | Per-node map/entry overhead and boxing; expected lookup |
| `TrieNode[26]` | Lowercase English alphabet is guaranteed | Direct `children[ch - 'a']` indexing; no character boxing | Pays for 26 reference slots at every node |
| Sorted edge array/list | Nodes have very few children and memory matters | Can reduce per-edge overhead | Insertion shifts elements; search scans or uses binary search |

Use `TrieNode[26]` when the prompt guarantees lowercase English letters; the map implementation above supports arbitrary UTF-16 units with the same prefix invariant. Java creates and garbage-collects nodes automatically; `get` returns a reference or `null`. `computeIfAbsent` installs one child for a missing key.

> 🌐 **Java Backend Relevance — HIGH:** A map stores references to the same mutable node objects. A `final` map field prevents reassignment of the field; it does not freeze the map or its nodes. This distinction matters in shared caches and mutable object graphs.

### 19.3 Prefix Search, Counts, and Deletion

**Priority:** 🟡 Tier 3 — Nice to Know

#### Prefix search

`startsWith(prefix)` succeeds as soon as every prefix character is consumed; it does not require `isWord` at the final node. Exact search does.

To list completions, first locate the prefix node, then DFS below it. If there may be many results, output size dominates runtime; returning `k` characters of results cannot be faster than `Ω(k)`.

#### Useful node augmentations

- `passCount`: how many inserted words pass through the node.
- `endCount`: how many copies terminate at the node.
- `word`: store the full word at terminal nodes for easy board-search output.
- `topSuggestions`: cached ranked completions for an autocomplete system.

Augmentation improves a particular query but adds update and memory costs. Store only metadata the problem requests.

#### Deletion

To delete safely:

1. Walk the path and confirm the complete word exists.
2. Clear or decrement its terminal marker/count.
3. Moving backward, remove a node only if it has no children, is not terminal for another word, and has no remaining pass count.

Simply deleting every node on the path would corrupt shared prefixes such as deleting `app` when `apple` remains.

### 19.4 Wildcard Search

**Priority:** 🟡 Tier 3 — Nice to Know

If `.` matches any one character, a normal character follows one child while `.` branches over every child.

This helper belongs alongside the trie implementation. A dictionary API can call `wildcardSearch(root, pattern)` internally; keep the mutable root private rather than exposing it to callers.

```java
// Uses TrieNode from section 19.2; call with the dictionary's root node.
static boolean wildcardSearch(TrieNode root, String pattern) {
    return wildcardFrom(root, pattern, 0);
}

private static boolean wildcardFrom(TrieNode node, String pattern, int index) {
    if (index == pattern.length()) return node.isWord;
    char ch = pattern.charAt(index);
    if (ch != '.') {
        TrieNode child = node.children.get(ch);
        return child != null && wildcardFrom(child, pattern, index + 1);
    }
    for (TrieNode child : node.children.values()) {
        if (wildcardFrom(child, pattern, index + 1)) return true;
    }
    return false;
}
```

- **Expected time:** `O(L)` without wildcards for hash-map children.
- **Worst-case time:** Exponential in the number of wildcard positions, bounded by nodes reachable at the required depths.
- **Space:** `O(L)` recursion depth.
- **Common mistake:** Returning true for a prefix after the pattern is consumed without checking `isWord`.
- **Contract:** `.` is the wildcard token and cannot mean a literal period here. Empty patterns succeed only for a stored empty word. Depth `L` can still overflow the Java call stack for unusually long keys.

### 19.5 Trie + Backtracking for Word Search

**Priority:** 🟡 Tier 3 — Nice to Know

#### Why one trie beats one search per word

If the task asks whether one word exists in a board, ordinary DFS/backtracking is enough. If it asks for **all dictionary words**, searching once per word repeats exploration for shared prefixes. A trie lets one board traversal pursue all words with the current prefix and stop immediately when no dictionary word can continue.

```java
// Uses TrieNode from section 19.2. Contract: rectangular lowercase-English
// board, nonempty lowercase-English dictionary words; no cell reuse in a path.
static List<String> findWords(char[][] board, String[] words) {
    List<String> found = new ArrayList<>();
    if (board.length == 0 || board[0].length == 0 || words.length == 0) {
        return found;
    }
    TrieNode root = new TrieNode();
    for (String word : words) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            node = node.children.computeIfAbsent(word.charAt(i), key -> new TrieNode());
        }
        node.isWord = true;
        node.word = word; // Copies the reference; String is immutable.
    }
    int[] rowDelta = {1, -1, 0, 0};
    int[] colDelta = {0, 0, 1, -1};
    for (int row = 0; row < board.length; row++) {
        for (int col = 0; col < board[0].length; col++) {
            findWordsFrom(board, row, col, root, rowDelta, colDelta, found);
        }
    }
    return found;
}

private static void findWordsFrom(char[][] board, int row, int col,
        TrieNode parent, int[] rowDelta, int[] colDelta, List<String> found) {
    char ch = board[row][col];
    TrieNode node = parent.children.get(ch);
    if (node == null) return;
    if (node.word != null) {
        found.add(node.word);
        node.word = null; // Suppress duplicate output in this disposable trie.
        node.isWord = false;
    }
    board[row][col] = '#'; // Marker is outside the declared alphabet.
    for (int direction = 0; direction < 4; direction++) {
        int nextRow = row + rowDelta[direction];
        int nextCol = col + colDelta[direction];
        if (0 <= nextRow && nextRow < board.length
                && 0 <= nextCol && nextCol < board[0].length
                && board[nextRow][nextCol] != '#') {
            findWordsFrom(board, nextRow, nextCol, node, rowDelta, colDelta, found);
        }
    }
    board[row][col] = ch;
    if (node.children.isEmpty() && !node.isWord) {
        parent.children.remove(ch);
    }
}
```

#### Complexity and trade-offs

- Building the trie takes expected `O(S)` time and `O(S)` node/edge space for `S` total dictionary UTF-16 units. Terminal payloads reference the original immutable strings rather than copying them.
- Let `B = rows × cols`, `L` be the longest word, and `D = min(L, B)`. A loose expected board-search bound is `O(B × 4 × 3^(D-1))` for nonempty words, with constant expected child lookup: four first directions and at most three onward directions because the path cannot immediately reuse the prior cell. Prefix failures and pruning often reduce actual work. Include `O(S)` construction and `O(K)` output references for `K` distinct found words.
- Recursion path space is `O(D)`; temporarily modifying the board avoids a separate `boolean[][] visited`. The board is restored on normal return. If mutation is forbidden, allocate visited state; if exceptional exits must restore shared input, use `try/finally` around the temporary change.
- Mutating the trie to suppress duplicates/prune dead branches is efficient only if that trie is disposable. Do not do it if later queries must reuse the original dictionary.

#### Common mistakes

- Failing to restore the board after DFS.
- Reusing a cell within one path.
- Returning immediately after finding a word even though longer words may share that terminal prefix.
- Emitting the same word multiple times from different paths.
- Starting a DFS for characters not present under the root.
- Building a trie for a single-word search, where it adds unnecessary complexity.

### 19.6 Alternatives and When Not to Use a Trie

**Priority:** 🟡 Tier 3 — Nice to Know

| Need | Usually prefer | Why |
|---|---|---|
| Exact membership only | `HashSet<String>` | Simpler, compact, expected `O(L)` hashing/lookup |
| Offline prefix range in sorted words | Sorted `String[]` + binary search | Avoids node overhead; contiguous lexical range |
| One word in a board | Plain backtracking | No benefit from shared dictionary prefixes |
| Many words or repeated prefix queries | Trie | Shares prefix work |
| Many patterns searched inside one long text | KMP/Aho–Corasick depending count | Search direction and workload differ |
| Space-constrained static dictionary | Compressed representation | Ordinary trie object overhead may be large |

A trie’s asymptotic lookup is `O(L)`, just like hashing a length-`L` string. Its advantage is prefix navigation and shared structure, not a magical sublinear exact lookup.

### 19.7 Representative Trie Problems

Budget **3–4 distinct problems** after hashing and backtracking; one cold revisit is more useful than several near-duplicate dictionary exercises.

| Step | Problem | Lesson to retain |
|---|---|---|
| Mechanics | Insert `app` and `apple`; test exact, prefix, duplicate, and empty-word queries | Terminal marker differs from prefix existence |
| Canonical | ⭐ **Canonical Interview Problem:** Implement Trie / Prefix Tree | Reconstruct insert/search/startsWith from the path invariant |
| Variation | Design Add and Search Words | A literal follows one edge; `.` explores children and still requires a terminal marker |
| Mixed pattern | ⭐ **Canonical Interview Problem:** Word Search II | Combine trie pruning with grid backtracking, restoration, and duplicate suppression |
| Cold revisit | Implement Trie, then re-explain Word Search II after a delay | Compare the trie with exact hashing and a separate DFS per word |

Replace Words is a useful extra query variation if shortest-prefix lookup is still unfamiliar. Autocomplete ranking, binary XOR tries, and reversed stream queries are optional target-specific extensions. For Longest Common Prefix over one batch, first explain why a direct scan is usually enough.

### 19.8 Trie Edge Cases and Interview Tips

- Define behavior for the empty string: inserting it normally marks the root terminal.
- Decide whether input is case-sensitive and what alphabet is valid.
- Duplicate insertion may be idempotent or may increase a count.
- Unicode “character” handling and normalization are production concerns; clarify them rather than silently assuming ASCII.
- Explain memory as total created nodes and child-container overhead, not merely number of words.
- For a board search, state whether diagonals are allowed and whether cells can be reused.
- Match child storage to the alphabet: a fixed array for guaranteed lowercase English, a map for a sparse or broad alphabet. `Character` keys model UTF-16 code units; use `Integer` keys and code-point iteration if complete Unicode code points are required.

### 19.9 Trie Mastery Checklist

I have mastered interview-level tries when I can:

- [ ] Explain why a terminal flag is separate from the existence of children.
- [ ] Implement insert, exact search, and prefix search from scratch.
- [ ] Analyze each operation as `O(L)` and total node space as `O(S)` worst case.
- [ ] Compare a trie with a `HashSet<String>` and sorted `String[]` for prefix workloads.
- [ ] Choose `HashMap` versus fixed-array children and explain the memory trade-off.
- [ ] Add wildcard search using DFS without accepting a prefix as a complete word.
- [ ] Combine a trie with board backtracking and restore visited cells correctly.
- [ ] Explain how prefix pruning helps and why its worst case can still be exponential.
- [ ] Avoid using a trie when exact membership or a single search has a simpler solution.

<a id="20-specialized-advanced-topics"></a>
## 20. Specialized / Advanced Topics

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

### Topic Overview

- **What it is:** A collection of techniques that solve narrower or more demanding problems: dynamic range-query trees, specialized string matching, uncommon graph algorithms, high-dimensional DP, and geometry.
- **Why it exists:** Core structures do not efficiently support every workload. These techniques exploit additional constraints such as associative range operations, fixed patterns, negative edges, small bitmaskable sets, or geometric orientation.
- **Why it matters in interviews:** Awareness helps identify an unusual prompt and prevents forcing an unsuitable core technique. Deep implementation is usually valuable only for algorithm-heavy companies or teams.
- **Interview priority:** **⚪ Tier 4 — Low Priority / Specialized.** As a category, these topics have low frequency in general SWE interviews and high preparation cost. A few components—especially KMP, rolling hash, MST, and negative-edge shortest-path awareness—are **🟡 Tier 3 — Nice to Know**.
- **Prerequisites:** Strong Tier 1/Tier 2 fundamentals, binary search, heaps, graph traversal, greedy reasoning, recursion, DP, bit operations, and modular arithmetic where hashing is involved.
- **Common use cases:** Online range queries/updates, linear-time pattern matching, multi-pattern matching, negative-weight shortest paths, network flow, exponential-state optimization over a tiny set, and geometric intersection or hull problems.
- **Common problem patterns:** Many interleaved updates and range queries; search a pattern inside a long text; detect string borders; choose subsets when `n ≤ 20`; find strongly connected regions; or reason about point orientation.
- **How to recognize it:** Constraints explicitly rule out the simpler tool, the operation algebra matches a specialized structure, or the prompt names/strongly suggests the technique.
- **How deeply to understand it:** First know what problem each technique solves, its complexity, and the simpler alternative. Implement only the Tier 3 techniques relevant to target companies; study Tier 4 implementations on demand.

**Study contract:** Memorize purpose and headline complexity only for techniques relevant to your target. Understand the trigger and simpler alternative; look up implementations of Tier 4 techniques. Do not add them to the daily template recall set.

### Why This Priority Was Assigned

General SWE interviews overwhelmingly reward fluency in arrays, hashing, two pointers, sliding windows, search, trees, graphs, heaps, and standard DP. Spending weeks on segment-tree lazy propagation or suffix arrays while those fundamentals remain shaky is a poor trade. Specialized topics become rational when job signals—company question history, role description, interviewer guidance, or contest-style screening—show that they are in scope.

### Focus First

- Recognize when prefix sums, sorting, hashing, a heap, BFS/DFS, Dijkstra, or ordinary DP already solves the problem.
- Know the capability and headline complexity of each advanced technique.
- Learn KMP/rolling-hash intuition and basic negative-edge/MST distinctions if core topics are already strong.
- Use constraints to decide whether a specialized algorithm is necessary.

### Learn Later

- Fenwick tree mechanics before segment trees if only sums and point updates are needed.
- KMP prefix/failure-function implementation.
- Rabin–Karp rolling hash with explicit collision handling.
- Kruskal/Prim, Bellman–Ford, and Floyd–Warshall at a conceptual plus standard-implementation level.

### Optional / Specialized

- Lazy segment trees, persistent data structures, suffix arrays/trees/automata, Aho–Corasick, Manacher, max flow/min cut, strongly connected components, bridge/articulation algorithms, digit/profile/bitmask DP, and computational geometry beyond elementary orientation.

### 20.1 Advanced Topic Priority Matrix

| Topic | Priority | General SWE frequency | Required depth | Why this priority was assigned |
|---|---|---:|---|---|
| Fenwick Tree (Binary Indexed Tree) | ⚪ Tier 4 — Low Priority / Specialized | Low | Awareness; basic implementation only if target-relevant | Narrower than prefix sums and less flexible than segment trees, though relatively easy |
| Segment Tree | ⚪ Tier 4 — Low Priority / Specialized | Low | Know purpose and complexity | Powerful but implementation-heavy; uncommon outside algorithmic screens |
| KMP | 🟡 Tier 3 — Nice to Know | Low–medium | Understand prefix function; implement after core topics | Linear exact matching is a plausible string follow-up, but built-ins or simpler methods often suffice |
| Rabin–Karp / rolling hash | 🟡 Tier 3 — Nice to Know | Low–medium | Understand rolling update and collisions | Useful for many-window comparisons and duplicate-substring variants, but correctness needs collision care |
| Manacher’s algorithm | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness only | Linear palindrome radii rarely justify memorizing a delicate implementation |
| Bellman–Ford / Floyd–Warshall | 🟡 Tier 3 — Nice to Know | Low | Recognize when needed; basic recurrence | Negative edges or all-pairs paths occasionally appear, but Dijkstra/BFS dominate |
| MST (Kruskal / Prim) | 🟡 Tier 3 — Nice to Know | Low–medium | Standard form after DSU/heaps | Appears in network-connection questions, but less often than traversal and shortest paths |
| SCC, bridges, articulation points, Euler tours | ⚪ Tier 4 — Low Priority / Specialized | Low | Awareness unless company-specific | Valuable graph theory with limited general-interview frequency |
| Max flow / min cut | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | High learning cost and rare outside specialized matching/network tasks |
| Bitmask DP | ⚪ Tier 4 — Low Priority / Specialized | Very low | Recognize `n ≤ 20`; one basic example if needed | Exponential despite optimization and uncommon in ordinary interviews |
| Digit DP / profile DP / DP optimizations | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | Competitive-programming territory for most candidates |
| Advanced string indexes (suffix array/tree/automaton) | ⚪ Tier 4 — Low Priority / Specialized | Very low | Awareness | Complex, specialized, and often replaced by libraries in production |
| Computational geometry | ⚪ Tier 4 — Low Priority / Specialized | Very low | Basic coordinate math only | Rare in general SWE interviews and full of precision/degeneracy issues |

### 20.2 Range Queries: Prefix Sum, Fenwick Tree, or Segment Tree?

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

Start from the operation workload, not the fanciest structure.

| Workload | Best first consideration | Build | Query | Update |
|---|---|---:|---:|---:|
| Static range sums | Prefix sum | `O(n)` | `O(1)` | Rebuild / `O(n)` |
| Many offline range additions, final values only | Difference array | `O(n + q)` total | Final pass | `O(1)` per recorded update |
| Point updates + prefix/range sums | Fenwick tree | `O(n)` or `O(n log n)` | `O(log n)` | `O(log n)` |
| General associative range query + point updates | Segment tree | `O(n)` | `O(log n)` | `O(log n)` |
| Range updates + range queries | Lazy segment tree | `O(n)` | `O(log n)` | `O(log n)` worst-case for standard lazy range-add/aggregate operations |

#### Fenwick Tree (Binary Indexed Tree)

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

A Fenwick tree compactly stores partial aggregates. With one-based indexing, `i & -i` gives the size of the block represented at index `i`.

```java
static final class FenwickTree {
    private final long[] tree; // Internal indexing is one-based.

    FenwickTree(int size) {
        if (size < 0 || size == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid size");
        }
        tree = new long[size + 1];
    }

    int size() {
        return tree.length - 1;
    }

    void add(int index, long delta) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();
        // long protects the upward index step from signed-int overflow.
        for (long i = (long) index + 1; i < tree.length; i += i & -i) {
            tree[(int) i] += delta;
        }
    }

    long prefixSum(int index) {
        if (index < -1 || index >= size()) throw new IndexOutOfBoundsException();
        long total = 0;
        for (int i = index + 1; i > 0; i -= i & -i) total += tree[i];
        return total;
    }

    long rangeSum(int left, int right) { // Inclusive [left, right].
        if (left < 0 || left > right || right >= size()) {
            throw new IndexOutOfBoundsException("Invalid range");
        }
        return prefixSum(right) - prefixSum(left - 1);
    }
}
```

- **Time:** `O(log n)` for point update and prefix/range sum.
- **Space:** `O(n)`.
- **When to use:** Interleaved updates and cumulative/range-sum queries; also frequency tables for order-statistic-style counting after coordinate compression.
- **Common mistakes:** Mixing zero- and one-based indices, calling `i += i & -i` at `i = 0`, and assuming every associative operation has the inverse needed to derive the interval `[left, right]` from two prefix queries.
- **Trade-off:** Smaller and simpler than a segment tree for sums, but less flexible. This constructor initializes an all-zero tree in `O(n)`; inserting `n` initial values through `add` costs `O(n log n)`. `prefixSum(-1)` is the empty prefix; sums and updates must fit in `long`.

#### Segment Tree

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

A segment tree stores an aggregate for each interval in a binary decomposition of the array. Parent nodes combine child results using an associative operation such as sum, minimum, maximum, or greatest common divisor.

- **Build:** `O(n)`.
- **Point update:** `O(log n)`.
- **Range query:** `O(log n)` for standard associative aggregates.
- **Space:** `O(n)` asymptotically, commonly about `2n` iteratively or up to `4n` recursively.
- **Use when:** Data changes between queries and prefix subtraction is invalid or insufficient—for example, range minimum with point updates.
- **Learn only when required:** Lazy propagation supports range updates, but its invariants and composition rules are easy to get wrong in an interview.
- **Common mistakes:** Incorrect half-open/inclusive interval conventions, wrong neutral element, failing to recompute ancestors, and composing lazy updates in the wrong order.

##### Practical decision rule

1. No updates → prefix sums or preprocessing.
2. Sum/frequency with point updates → Fenwick tree.
3. Need min/max/GCD or richer aggregate with updates → segment tree.
4. Only a few operations → even direct `O(n)` work may be simpler and fast enough.

### 20.3 Advanced String Algorithms

**Priority:** 🟡 Tier 3 — Nice to Know

#### KMP (Knuth–Morris–Pratt)

**Priority:** 🟡 Tier 3 — Nice to Know

##### Intuition

When a mismatch occurs after matching part of the pattern, some suffix of what matched may also be a prefix of the pattern. The prefix-function/LPS table tells how far the pattern can shift without rechecking text characters known to match.

`lps[i]` is the length of the longest **proper** prefix of `pattern[0..i+1)` that is also a suffix.

```java
static int kmpFind(String text, String pattern) {
    if (pattern.isEmpty()) return 0;
    int[] lps = new int[pattern.length()];
    int length = 0;
    for (int i = 1; i < pattern.length();) {
        if (pattern.charAt(i) == pattern.charAt(length)) {
            lps[i++] = ++length;
        } else if (length > 0) {
            length = lps[length - 1];
        } else {
            i++;
        }
    }
    int textIndex = 0;
    int patternIndex = 0;
    while (textIndex < text.length()) {
        if (text.charAt(textIndex) == pattern.charAt(patternIndex)) {
            textIndex++;
            patternIndex++;
            if (patternIndex == pattern.length()) return textIndex - patternIndex;
        } else if (patternIndex > 0) {
            patternIndex = lps[patternIndex - 1];
        } else {
            textIndex++;
        }
    }
    return -1;
}
```

- **Time:** `O(text length + pattern length)`.
- **Space:** `O(pattern length)`.
- **Correctness intuition:** On fallback, KMP retains exactly the longest prefix already known to match the suffix before the mismatch; no possible earlier match start is skipped.
- **Common mistakes:** Treating the whole string as a proper prefix, resetting `j` to zero instead of following failure links, and advancing the text index during a fallback when the current character has not been resolved.
- **Java contract:** `String` arguments are non-null; matching/indexing uses UTF-16 units, as `String.indexOf` does. Empty pattern returns zero.
- **Alternatives:** `text.indexOf(pattern)` for practical code, naive `O(nm)` when constraints are small, or rolling hash when comparing many equal-length windows.

#### Rabin–Karp and rolling hash

**Priority:** 🟡 Tier 3 — Nice to Know

Represent a window as a polynomial hash. When the window shifts, remove the outgoing character’s weighted contribution, multiply/shift, and add the incoming character in `O(1)`.

- **Expected time for single-pattern search:** `O(n + m)` with a good hash and few collisions.
- **Worst-case time:** `O(nm)` if many candidate hashes collide and each is verified.
- **Space:** `O(1)` for one rolling window, or `O(n)` when storing many hashes/prefix hashes.
- **Use when:** Comparing many fixed-length substrings, detecting duplicates, or pairing with binary search on substring length.
- **Correctness requirement:** Equal hashes are candidates, not proof of equal strings. Verify character equality for an exact answer. Two independent hashes reduce collision probability but still give a probabilistic answer without verification.
- **Java pitfalls:** `%` can leave a negative remainder; normalize it when the hash requires `[0, modulus)`. Promote before multiplication and bound the `long` product. Hashing a newly created `substring` scans/copies its units, so it is not a constant-time rolling update.

#### Manacher’s algorithm

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

Manacher computes every odd/even palindrome radius in `O(n)` time and `O(n)` space by reusing mirror information inside the rightmost known palindrome.

- **Why low priority:** The implementation is delicate, the invariant is easy to forget, and expanding around centers solves longest palindromic substring in `O(n²)` time with `O(1)` space—often adequate for interview constraints.
- **Study deeply only if:** The role is algorithm-heavy, linear time is explicitly required, or palindrome queries are a known target-company theme.
- **Common mistakes:** Confusing radius conventions, transformed-string indices, even/odd centers, and output boundary conversion.

#### Other advanced string structures

| Technique | Priority | What it solves | General-interview guidance |
|---|---|---|---|
| Z algorithm | ⚪ Tier 4 — Low Priority / Specialized | Prefix-match length at every position | Know it exists; KMP coverage is usually enough |
| Aho–Corasick | ⚪ Tier 4 — Low Priority / Specialized | Many patterns in one text | Trie + failure links; specialized |
| Suffix array | ⚪ Tier 4 — Low Priority / Specialized | Sorted suffix queries, repeated substrings | High implementation cost; awareness only |
| Suffix tree/automaton | ⚪ Tier 4 — Low Priority / Specialized | Rich substring queries | Almost never expected in general SWE interviews |

### 20.4 Advanced Graph Algorithms

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

Graph BFS/DFS, topological sorting, Union-Find, and Dijkstra belong in the main graph curriculum. The techniques here solve less common variants.

#### Selective priority map

| Technique | Priority | Trigger | Complexity | What to know |
|---|---|---|---:|---|
| Kruskal MST | 🟡 Tier 3 — Nice to Know | Connect all vertices with minimum total edge cost | `O(E log E)` | Sort edges; add one if DSU says it joins components |
| Prim MST | 🟡 Tier 3 — Nice to Know | Same MST goal, grow from a vertex | `O((V + E) log(E + 1))` with lazy heap; `O(E log V)` for a connected simple graph | Cheapest crossing edge; discard stale entries |
| Bellman–Ford | 🟡 Tier 3 — Nice to Know | Negative edges or negative-cycle detection | `O(VE)` | Relax every edge `V-1` times; one more pass detects reachable negative cycle |
| Floyd–Warshall | 🟡 Tier 3 — Nice to Know | Dense, small graph; all-pairs paths | `O(V³)` time, `O(V²)` space | Intermediate-vertex DP: `dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])` |
| 0–1 BFS | 🟡 Tier 3 — Nice to Know | Edge weights only 0 or 1 | `O(V+E)` | `ArrayDeque`: zero-cost edge to front, one-cost edge to back |
| Strongly connected components | ⚪ Tier 4 — Low Priority / Specialized | Mutual reachability in directed graph | `O(V+E)` | Know Tarjan/Kosaraju purpose; implement only if target-relevant |
| Bridges / articulation points | ⚪ Tier 4 — Low Priority / Specialized | Single failure disconnects graph | `O(V+E)` | DFS discovery/low-link concept |
| Eulerian path / Hierholzer | ⚪ Tier 4 — Low Priority / Specialized | Use every edge exactly once | `O(E)` | Degree conditions and postorder edge consumption |
| Max flow / min cut | ⚪ Tier 4 — Low Priority / Specialized | Capacitated routing, some matchings | Algorithm-dependent | Awareness unless explicitly in scope |

#### Essential distinctions

- **Shortest path** minimizes distance between vertices; an **MST** minimizes total edge weight needed to connect all vertices. An MST path is not generally a shortest path.
- Dijkstra requires non-negative edge weights. Bellman–Ford permits negative edges and can detect a reachable negative cycle.
- Floyd–Warshall is attractive only when `V` is small enough for cubic time and many source-target pairs matter.
- A topological-order relaxation can solve shortest paths in a DAG even with negative edges, more efficiently than Bellman–Ford.

#### Common mistakes

- Applying Dijkstra to negative edges.
- Using MST when the prompt asks for one source-to-target shortest route.
- Forgetting disconnected graphs produce a minimum spanning **forest**, not one spanning tree.
- Failing to guard infinity before adding an edge in Bellman–Ford/Floyd–Warshall.
- Treating an undirected parent edge as a cycle during DFS.

### 20.5 Advanced Dynamic Programming

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

#### Bitmask DP

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

A mask encodes which members of a small set have already been used. A common state is:

```text
dp[mask][last] = best cost for visiting exactly mask and ending at last
```

For a traveling-salesperson-style recurrence, try each unvisited next vertex.

- **States:** Often `O(2^n × n)`.
- **Transitions:** Up to `O(n)` each.
- **Time:** Often `O(2^n × n²)`.
- **Space:** Often `O(2^n × n)`.
- **Recognition:** Very small `n`, need to remember an arbitrary chosen subset, and order/assignment matters.
- **Alternative:** Backtracking with pruning may be easier and faster on typical instances; greedy may solve special metric/structure variants but not the general problem.
- **Java pitfalls:** Parenthesize `(mask & (1 << bit)) != 0`; `1 << n` uses an `int` and Java masks the shift distance modulo 32. `1L << n` uses a `long` and masks modulo 64, but does not make an exponential table feasible. Estimate cells and bytes before allocating; use primitive arrays when dense state is justified.

#### Other specialized DP families

| Family | Priority | Purpose | Guidance |
|---|---|---|---|
| Digit DP | ⚪ Tier 4 — Low Priority / Specialized | Count numbers up to a bound satisfying digit constraints | State often includes position, tight flag, and other properties; contest-oriented |
| Profile DP | ⚪ Tier 4 — Low Priority / Specialized | Tile/navigate a narrow grid using row/column masks | Only when one grid dimension is very small |
| Rerooting DP | ⚪ Tier 4 — Low Priority / Specialized | Compute an answer as if every tree node were root | Learn only for advanced tree-heavy screening |
| Probability DP | ⚪ Tier 4 — Low Priority / Specialized | Aggregate probabilities over states | Requires careful event/state modeling and numeric precision |
| DP optimizations | ⚪ Tier 4 — Low Priority / Specialized | Reduce expensive transitions using monotonicity/convexity | Divide-and-conquer, Knuth, convex hull trick are rarely general-interview material |

The right baseline is to recognize that a DP exists and estimate its dimensions before reaching for an optimization. An elegant optimized recurrence is useless if its state meaning is wrong.

### 20.6 Computational Geometry

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

#### Minimum useful toolkit

- Squared Euclidean distance to avoid unnecessary square roots.
- Rectangle overlap and interval reasoning.
- Cross product/orientation of three points.
- Awareness of integer overflow when multiplying coordinates.
- Clear policy for collinear points, touching boundaries, and floating-point tolerance.

For points `A`, `B`, `C`, the 2D cross product

```text
(B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x)
```

is positive for one turn direction, negative for the other, and zero when collinear (subject to exactness/precision of the numeric type).

#### Keep specialized

- Convex hull, rotating calipers, line sweep with event structures, closest pair of points, and robust segment-arrangement algorithms.
- Learn them only for graphics, mapping, robotics, games, quantitative, or explicitly algorithmic roles.

#### Common mistakes

- Comparing floating-point values for exact equality.
- Ignoring collinear overlap or endpoint-touch cases.
- Using slopes and dividing by zero when cross products avoid division.
- Overflowing products before storing them in a wider type. In Java, cast before subtraction too: `((long) bx - ax) * ((long) cy - ay)`. Even `long` is insufficient for every product of unrestricted `int` coordinate differences; derive bounds or use `java.math.BigInteger` for exact larger arithmetic.
- Mixing screen coordinates (often y increases downward) with Cartesian orientation assumptions.

### 20.7 Selective Representative Problems

The default budget is **zero new specialized implementations** until the core is reliable and a target requirement justifies one. Then choose one ladder and spend roughly **2–3 problems**, including a later cold revisit; do not study every row as a course.

| Family | Mechanics | Canonical | Variation | Mixed pattern | Cold revisit |
|---|---|---|---|---|---|
| Dynamic range sums | Trace low-bit blocks and `add` by hand | ⭐ **Canonical Interview Problem:** Range Sum Query — Mutable | Point assignment: convert new value to a delta | Count Smaller Numbers After Self: compression plus frequency prefixes (advanced) | Rebuild the Fenwick invariant and test boundary indices |
| Exact string matching | Build LPS for a repeated-prefix pattern | ⭐ **Canonical Interview Problem:** Find the Index of the First Occurrence in a String, implementing KMP | Report every match, including overlapping matches | Repeated String Match: repetition bounds plus substring search | Rebuild LPS and explain why the text index never retreats |
| Graph extensions | Trace relaxation and crossing-edge choices | ⭐ **Canonical Interview Problem:** Min Cost to Connect All Points | Connect a disconnected graph: detect a spanning forest | Cheapest Flights Within K Stops: separate previous/current distance layers | Classify MST versus shortest path before choosing an algorithm |

Pick further work only by a concrete constraint: range minimum with updates → segment tree; many equal-length substring comparisons → rolling hash with a collision policy; small arbitrary subsets → bitmask DP. Lazy range updates, strict linear palindrome search, critical connections, and convex hulls remain specialization exercises. Their existence is reference knowledge, not a core mastery gate.

### 20.8 Advanced-Topic Study Decision

Before investing deeply, ask:

1. Are all 🔴 Tier 1 — Must Master topics reliable under interview time pressure?
2. Can I solve standard 🟠 Tier 2 — Very Important problems without pattern hints?
3. Does my target company, role, or screening format show evidence that this topic appears?
4. Does a simpler method already meet the constraints?
5. Will one hour here improve my interview odds more than a mock interview or re-solving a weak core pattern?

If any of questions 1–3 has answer “no,” defer deep study. A “yes” to question 4 usually means use the simpler method.

### 20.9 Specialized Topics Mastery Checklist

For general SWE interviews, “mastery” here mostly means informed triage:

- [ ] I can choose prefix sums, a Fenwick tree, or a segment tree based on update/query needs.
- [ ] I know Fenwick and segment-tree headline complexities and memory trade-offs.
- [ ] I can explain what KMP’s LPS table means and why search is linear.
- [ ] I understand rolling-hash collisions and do not claim deterministic equality without verification.
- [ ] I know why Dijkstra fails with negative edges and when Bellman–Ford/Floyd–Warshall applies.
- [ ] I distinguish shortest path from minimum spanning tree.
- [ ] I recognize `n ≤ 20` plus arbitrary chosen subsets as a possible bitmask-DP clue.
- [ ] I know Manacher, suffix structures, max flow, digit/profile DP, and advanced geometry exist without prioritizing them prematurely.
- [ ] I can justify using a specialized technique from the constraints rather than from novelty.

## 21. DSA Pattern Recognition

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority level was assigned:** General interviews reward selecting and adapting an approach on an unfamiliar prompt. Fast, evidence-based mapping from clues and constraints to candidate patterns is the central transferable skill this entire guide is designed to build.

Pattern recognition is disciplined hypothesis generation, not keyword matching. A clue suggests candidates; constraints and invariants decide whether a candidate is valid.

### Pattern-recognition cheat sheet

| Problem clues | Priority | Likely data structure | Likely algorithm / pattern | Example problem types | Common mistake |
|---|---|---|---|---|---|
| Fast membership, “seen before,” duplicates | 🔴 Tier 1 — Must Master | Hash set | One-pass lookup | Contains duplicate, longest consecutive run | Forgetting that worst-case hashing or memory may matter |
| Associate a key with a count/index/object | 🔴 Tier 1 — Must Master | Hash map | Frequency/index table | Anagram groups, first unique item | Overwriting an index that should be preserved |
| Pair or complement reaches a target | 🔴 Tier 1 — Must Master | Hash map, or sorted array | Complement lookup; sort + two pointers | Two sum, pair difference | Returning values when indices are required |
| Counts or multiset equality | 🔴 Tier 1 — Must Master | Frequency array/map | Count then compare; maintain deficit | Anagrams, character replacement | Failing to remove zero-count entries when map equality matters |
| Contiguous range, repeated range sum | 🔴 Tier 1 — Must Master | Array/map | Prefix sum | Range sum, subarray sum equals `k` | Using a sliding window when negative values break monotonicity |
| Many offline range additions | 🟡 Tier 3 — Nice to Know | Difference array | Mark boundaries, then prefix | Flight bookings, capacity changes | Updating the wrong closing boundary |
| Longest/shortest contiguous range satisfying a maintainable constraint | 🔴 Tier 1 — Must Master | Map/set/deque | Variable sliding window | Longest unique substring, minimum covering window | Shrinking only once when the invariant needs a `while` loop |
| Statistic over every fixed-length contiguous range | 🔴 Tier 1 for ordinary windows; 🟡 Tier 3 for monotonic deque | Frequency map/deque | Fixed window; monotonic deque for extrema | Maximum average, window maximum | Recomputing the entire window each step |
| Sorted sequence plus pair/triple condition | 🔴 Tier 1 — Must Master | Array | Opposite-direction two pointers | Two-sum sorted, 3Sum, closest pair | Skipping duplicates incorrectly |
| Remove/compact/partition in place | 🔴 Tier 1 — Must Master | Array | Read/write or slow/fast pointers | Remove duplicates, move zeroes | Losing unread values during writes |
| Sorted input and exact target/boundary | 🔴 Tier 1 — Must Master | Array | Binary search / lower bound | First occurrence, insertion position | Mixing closed and half-open interval rules |
| Minimize/maximize a numeric answer; feasibility changes only once | 🟠 Tier 2 — Very Important | Usually array + helper | Binary search on answer | Shipping capacity, eating speed | Searching without proving monotonic feasibility |
| Next greater/smaller, span until obstruction | 🟠 Tier 2 — Very Important | Monotonic stack | Maintain unresolved candidates | Daily temperatures, histogram area | Storing values when indices/distances are needed |
| Nested scopes, matching delimiters, undo order | 🔴 Tier 1 — Must Master | Stack | Push open state, pop on closure | Valid parentheses, decode string | Popping an empty stack or ignoring final leftovers |
| Repeated minimum/maximum or highest-priority item | 🟠 Tier 2 — Very Important | Heap | Push/pop priority queue | Task scheduling, merge streams | Assuming heap iteration is sorted |
| Top `k` while `k` is much smaller than `n` | 🟠 Tier 2 — Very Important | Size-`k` heap | Retain best `k` | Top frequent items, kth largest | Choosing min- versus max-heap incorrectly |
| Merge `k` sorted sources | 🟠 Tier 2 — Very Important | Heap | Keep one frontier per source | Merge lists, smallest range | Omitting the comparator for custom objects; equal priorities need a tie-breaker only when the contract requires one |
| Running median or balanced lower/upper halves | 🟡 Tier 3 — Nice to Know | Two heaps | Max-heap + min-heap rebalance | Median from data stream | Rebalancing sizes without maintaining ordering |
| Overlapping ranges or schedules | 🔴 Tier 1 — Must Master | Sorted interval list | Sort then merge/scan | Merge intervals, insert interval | Treating touching endpoints incorrectly for the stated semantics |
| Maximum compatible activities | 🟠 Tier 2 — Very Important | Sorted intervals | Greedy by earliest finish | Non-overlapping intervals | Sorting by start because it feels natural |
| Number of simultaneous events/resources | 🟠 Tier 2 — Very Important | Heap or event list | End-time heap; sweep line | Meeting rooms, maximum overlap | Processing equal-time starts/ends in the wrong order |
| Hierarchical structure, subtree result | 🔴 Tier 1 — Must Master | Tree + call stack | DFS / postorder | Height, diameter, balanced tree | Returning global information instead of the subtree state parent needs |
| Tree level, nearest node, minimum edges | 🔴 Tier 1 — Must Master | Queue | BFS by level | Level order, nearest target | Mixing levels or marking visited too late |
| Ordered binary tree | 🔴 Tier 1 — Must Master | BST | Use lower/upper bounds or inorder order | Validate BST, kth smallest | Comparing only a node with its parent |
| Ancestor or path through a tree | 🟠 Tier 2 — Very Important | Tree / parent map | Recursive state; LCA | Lowest common ancestor, path sum | Confusing node identity with duplicate values |
| Grid regions or islands | 🔴 Tier 1 — Must Master | Grid + queue/stack | Flood-fill BFS/DFS | Number of islands, surrounded regions | Marking visited after enqueue and duplicating work |
| Shortest path in an unweighted graph | 🔴 Tier 1 — Must Master | Adjacency list + queue | BFS | Word ladder, minimum moves | Using DFS and accepting the first found path |
| Reachability or connected components | 🔴 Tier 1 — Must Master | Adjacency list/set | DFS/BFS | Provinces, graph clone | Forgetting disconnected starting nodes |
| Prerequisites or dependency order | 🟠 Tier 2 — Very Important | Directed graph + indegrees | Topological sort | Course schedule, build order | Treating a directed graph as undirected |
| Incremental undirected connectivity | 🟠 Tier 2 — Very Important | Union-Find | Union by size/rank + compression | Redundant connection, accounts merge | Applying DSU when actual paths or direction are required |
| Cheapest paths with nonnegative weights | 🟠 Tier 2 — Very Important | Weighted graph + heap | Dijkstra | Network delay, cheapest route | Marking a node final on insertion rather than best extraction |
| Connect all vertices with minimum total edge cost | 🟡 Tier 3 — Nice to Know | Edges + DSU, or heap | Kruskal or Prim | Connect cities/points | Confusing MST cost with shortest-path cost |
| Divide vertices into two incompatible groups | 🟠 Tier 2 — Very Important | Graph + color array | BFS/DFS two-coloring | Possible bipartition | Checking only one connected component |
| Generate every subset/permutation/placement | 🟠 Tier 2 — Very Important | Path + used state | Backtracking | Subsets, permutations, N-Queens | Forgetting to undo state or copy a completed path |
| Small input such as `n ≈ 15–20`, choices per item | 🟠 Tier 2 — Very Important | Recursion/mask | Backtracking or bitmask enumeration | Assignments, subset constraints | Forcing a polynomial method where exponential search is intended |
| Repeated subproblems plus optimal substructure | 🟠 Tier 2 — Very Important | Memo/table | Dynamic programming | Robber, edit distance, coin change | Defining state with too little information |
| Count ways / minimum cost over a decision sequence | 🟠 Tier 2 — Very Important | Memo/table | DP after deriving recurrence | Decode ways, climbing stairs | Coding a table before defining what each cell means |
| Local choice appears to leave a smaller same-form problem | 🟠 Tier 2 — Very Important | Often sorted list | Greedy—only with exchange/stays-ahead reasoning | Jump reachability, scheduling | Trusting intuition without a correctness argument |
| Prefix dictionary, many prefix queries | 🟡 Tier 3 — Nice to Know | Trie | Character-by-character descent | Autocomplete, replace words | Omitting end-of-word state |
| Pattern inside text | 🟡 Tier 3 — Nice to Know | String / hash | Built-in search, KMP, rolling hash as needed | Find substring, repeated pattern | Reaching for advanced matching when constraints do not require it |
| Linked structure cycle or midpoint | 🟠 Tier 2 — Very Important | Linked-list node references | Floyd fast/slow pointers | Cycle detection, middle node | Reading `fast.next` without guarding `fast != null` |
| Recently used key with O(1) updates | 🟠 Tier 2 — Very Important | Map + doubly linked list | LRU design | LRU cache | Updating the map but not list links consistently |
| Static order statistic without needing all sorted output | 🟡 Tier 3 — Nice to Know | Array | Quickselect or heap | Kth largest | Claiming worst-case linear time for ordinary quickselect |

### A recognition funnel

Ask these questions in order:

1. **What is the shape?** Sequence, contiguous range, linked nodes, hierarchy, arbitrary network, intervals, or a stream?
2. **What must be returned?** Existence, count, exact object, ordering, minimum/maximum, path, or all possibilities?
3. **What do constraints rule out?** Estimate whether quadratic or exponential work is plausible.
4. **Which property can be exploited?** Sortedness, bounded values, monotonicity, repeated states, locality, tree structure, or nonnegative edge weights?
5. **What state must survive while scanning?** Counts, best prefix, active window, unresolved indices, frontier, visited nodes, or DP states?
6. **What invariant proves progress?** Each pointer moves once; every node is finalized once; the window is valid after shrinking; `dp[i]` has a precise meaning.

### Constraint-to-complexity heuristics

These are rough interview clues, not contractual limits; Java allocation, boxing, key-comparison cost, output size, memory limits, and runtime limits matter. A table with `2^25` states can exhaust memory even when enumerating that many lightweight states might be feasible.

| Input scale | First complexity to investigate | Typical families |
|---:|---|---|
| `n ≤ 10` | `O(n!)` may be possible | Permutations, exhaustive ordering |
| `n ≤ 20–25` | `O(2^n)` may be possible | Subsets, bitmask search/DP |
| `n ≤ 500` | `O(n²)` may be possible | 2D DP, pair enumeration |
| `n ≤ 10^4` | Usually `O(n log n)` or `O(n)` | Sorting, heaps, maps |
| `n ≥ 10^5` | Usually `O(n log n)` or `O(n)` | Linear scans, efficient graph traversal |
| Huge answer range but cheap feasibility test | `O(f(n) log range)` | Binary search on answer |

### Clues that are easy to confuse

- **Contiguous does not automatically mean sliding window.** A window needs a condition that can be repaired predictably as pointers move. With arbitrary negative numbers, prefix sums plus hashing often replace it.
- **Optimization does not automatically mean DP.** Try greedy only if a local choice can be justified; use DP when alternative decisions must be remembered.
- **Shortest path does not automatically mean BFS.** BFS is for unweighted/equal-weight edges; Dijkstra handles nonnegative weights.
- **Sorted does not automatically mean binary search.** Pair relationships often favor two pointers; binary search requires a monotone predicate or ordered lookup target.
- **Connectivity does not automatically mean DSU.** DFS/BFS is better when paths, traversal order, or directed edges matter. DSU excels at undirected merge/query operations.
- **Top-K does not automatically require a heap.** Sorting may be simpler when `k` is large or sorted output is required; quickselect can find a static kth boundary.

---

## 22. Interview Problem-Solving Framework

**Priority:** 🔴 Tier 1 — Must Master

This workflow is itself an interview skill. It gives the interviewer visible evidence of how you handle ambiguity, correctness, trade-offs, and feedback.

> **Problem statement → clarify contract → examples → constraints → brute force → bottleneck → recognition clues → candidate pattern/data structure → invariant/state → algorithm → complexity → implementation → edge cases → testing → follow-up optimization**

The steps below group that sequence into a practical conversation. Return to the contract or invariant whenever a counterexample invalidates an assumption.

### Step 1 — Understand the problem

Identify the input, required output, constraints, and semantics before proposing an algorithm.

- Restate the task in one sentence.
- Ask only questions that change the solution: Can input be empty? Are values unique? Is the graph directed? Are weights negative? Does “overlap” include touching endpoints? Must the input remain unchanged?
- Work one small example and confirm what should be returned—not merely printed.
- Name dangerous cases: duplicates, negative values, disconnected nodes, overflow, and invalid input if relevant.

Useful communication:

> “I’ll restate it to make sure I have the contract right: given ..., return .... The constraints suggest ..., and I’m assuming ....”

Do not silently invent convenient assumptions. If the interviewer does not answer, state a reasonable assumption and explain how another interpretation would change the solution.

### Step 2 — Start with brute force

Describe the simplest correct approach. Brute force provides:

- a correctness baseline;
- a source of test results for an optimized version;
- the repeated work that reveals the optimization.

Do not spend five minutes coding a clearly unacceptable brute force. Explain it crisply: “Enumerating every pair is `O(n²)` time and `O(1)` auxiliary space.”

### Step 3 — Analyze complexity against constraints

Derive rather than guess:

- What work happens per element, state, node, or edge?
- Can an element be revisited many times, or only a constant number?
- What is the recursion depth and what data is retained?
- Does a library operation hide sorting, copying, slicing, or linear deletion?

Then compare the result with the input scale. An optimization is necessary only when correctness, constraints, or interviewer expectations require it.

### Step 4 — Look for structure and patterns

Use the recognition funnel from [DSA Pattern Recognition](#21-dsa-pattern-recognition):

- lookup or counts → hashing;
- sorted order → two pointers or binary search;
- contiguous region → window or prefix sum;
- repeated extrema → heap;
- nested or unresolved items → stack;
- hierarchy/network → tree or graph traversal;
- dependencies → topological sort;
- all configurations → backtracking;
- repeated decision states → DP;
- locally safe choice → greedy, with proof.

Say why a pattern fits. “This is a sliding window” is weaker than “when the count exceeds the limit, advancing the left boundary is the only way to restore validity, so both pointers move monotonically.”

### Step 5 — Optimize one bottleneck

Name the duplicated work or expensive operation and replace it deliberately:

- repeated scans → maintained count, prefix, or deque;
- repeated lookup → map/set;
- repeated sorting of candidates → heap;
- repeated recursive states → memoization;
- exploring irrelevant branches → pruning;
- scanning all possible answers → binary search if feasibility is monotone.

Recheck trade-offs. An `O(n)` map solution may use `O(n)` space; an in-place sorted solution may be `O(n log n)` but preserve less memory.

### Step 6 — Explain before coding

Give a compact contract for the solution:

1. the data structure and what it stores;
2. the invariant;
3. the update order;
4. why the result is complete and correct;
5. time and space complexity.

Example:

> “I’ll scan once and store each previously seen value’s index. For value `x`, I check whether `target - x` is already present before inserting `x`, which prevents using one position twice. Expected time is `O(n)` and extra space is `O(n)`.”

This short explanation frequently exposes a missing condition before it becomes a bug.

### Step 7 — Implement cleanly

- Use names that encode roles: `left`, `right`, `indegree`, `remaining`, `best`.
- Keep one boundary convention throughout.
- Choose primitive arrays for dense numeric state; name object fields when an array entry would hide its meaning.
- State null, empty-input, and mutation assumptions. Promote arithmetic to `long` before a potentially overflowing operation.
- Separate a feasibility predicate or DFS helper when it clarifies state.
- Avoid premature micro-optimization and clever one-liners.
- Comment invariants or non-obvious choices, not syntax.
- If you notice a bug, state it calmly, repair it, and continue.

### Step 8 — Test manually

Trace the code—not the intended algorithm—on:

1. a normal representative case;
2. empty/minimum input if allowed;
3. one element or one node;
4. duplicates/all equal values;
5. already sorted/reverse sorted data;
6. boundary values, negatives, and overflow risks;
7. no solution and multiple possible solutions;
8. skewed trees, disconnected graphs, or cycles when applicable.

During a trace, write the important state (`left`, `right`, map, queue, `dp[i]`) after each meaningful update. Confirm termination as well as the returned value.

### Step 9 — Close the answer

Restate complexity and trade-offs, then mention one meaningful alternative:

> “This is `O(n)` expected time and `O(n)` extra space with `HashMap`. Sorting would cost `O(n log n)`, mutate or copy the input, and complicate original indices.”

If time remains, improve naming and cover one untested edge rather than introducing an unrelated optimization.

### When stuck

Use progressively stronger prompts on yourself:

1. What would brute force enumerate?
2. What work repeats?
3. What information would make one repeated operation constant or logarithmic?
4. Can sorting create useful order?
5. Can I state a smaller subproblem?
6. Which constraint have I not used?
7. Can I solve a tiny version by hand and name the changing state?

In a real interview, share these observations. Silence hides useful reasoning and prevents the interviewer from redirecting you.

### Framework mastery checklist

- [ ] I clarify semantics that affect the algorithm without interrogating every trivial detail.
- [ ] I can state a correct brute force and its complexity quickly.
- [ ] I explain the optimized invariant before coding.
- [ ] I derive time and auxiliary space, including recursion and output space.
- [ ] I trace edge cases through actual code.
- [ ] I respond to hints by incorporating them and explaining the revised reasoning.

---

## 23. Common Interview Mistakes

**Priority:** 🔴 Tier 1 — Must Master

Avoiding predictable errors often improves performance more than learning one more specialized algorithm. Record repeated failures using the categories in the [mistake log](#26-mistake-log-system); use the [Java reference](#java-for-dsa-interviews-essential-reference) for API examples.

| Trap | Why it fails | Prevention |
|---|---|---|
| Off-by-one errors | A loop misses or repeats a boundary element. | Write whether ranges are inclusive or half-open; test sizes `0`, `1`, and `2`. |
| Mixed binary-search conventions | Updates no longer guarantee progress or the returned boundary has no meaning. | State the interval invariant and final postcondition; trace absent targets and duplicates. |
| Forgetting duplicates | Counts, original indices, ties, or generated results become wrong. | Clarify whether identity, value, and multiplicity matter; skip duplicates at the correct recursion depth. |
| Using one element twice | A complement lookup sees the current position. | Query previously stored positions before inserting the current one. |
| Incompatible mutation during iteration | Structural changes during enhanced `for` traversal may cause `ConcurrentModificationException`; detection is best-effort. | Use the iterator's supported `remove`, `removeIf`, a separate change list, or an index-based algorithm whose shifting rules you understand. |
| Quadratic queue simulation | `ArrayList.remove(0)` shifts the remaining elements. | Use `ArrayDeque` with `offer`/`poll`, or a head index over suitable storage. |
| Infinite loop | A branch changes neither boundary nor state. | Verify that every iteration advances, returns, or shrinks a finite candidate set. |
| Incorrect recursion base case | Work stops too soon, never stops, or returns the wrong identity. | Define the function contract first; test the smallest legal state. |
| Missing backtracking undo | One branch contaminates another. | Match each choice with restoration after exploration. |
| Shared mutable result/path | `answers.add(path)` stores the same list reference repeatedly. | Snapshot a completed path with `new ArrayList<>(path)`; this copies references to its elements, so mutable elements may need their own copies. |
| Forgetting visited state | A graph traversal loops or performs duplicate work. | Normally mark a state on enqueue/push; use the visited key that represents the full search state. |
| Losing directed edges or disconnected vertices | Cycles, reachability, and indegrees become wrong. | Encode direction explicitly and start component scans from every unvisited vertex. |
| Wrong shortest-path algorithm | The first DFS route need not be shortest; fewest edges need not mean cheapest weight. | BFS for equal/unweighted edges; Dijkstra for nonnegative weights. |
| Finalizing Dijkstra on insertion | A later relaxation can find a cheaper route. | Skip stale heap entries and expand the cheapest current distance. |
| Incorrect grid boundaries | Java throws `ArrayIndexOutOfBoundsException`; empty or ragged rows also invalidate assumptions. | Check bounds before indexing, distinguish rows and columns, and state the rectangular-grid contract. |
| Losing tree return state | The parent cannot combine child results. | State what one recursive call returns; distinguish carried state from returned subtree state. |
| Confusing node identity with value | Different nodes may contain equal values. | Use `==` for node identity when identifying a particular node; use value comparisons for ordering. |
| Integer overflow | Java integer arithmetic can wrap silently. | Widen before arithmetic: `(long) a * b`; choose bounds/sentinels and do not add blindly to `Long.MAX_VALUE`. |
| Unsafe midpoint arithmetic | `left + right`, or an unconstrained `right - left`, may overflow. | Use `left + (right - left) / 2` when the bounds make subtraction safe; otherwise derive a method for the actual domain. |
| Object/String equality with `==` | It compares object references rather than logical contents. | Use `.equals()` for logical equality, or `Objects.equals(a, b)` when null is allowed. Do not rely on String interning or boxed-number caching. |
| Inconsistent custom-key equality | Equal keys may occupy different hash buckets; mutable key fields can make entries unreachable by lookup. | Keep equality fields stable and implement compatible `equals()`/`hashCode()`; arrays use identity equality by default. |
| Assuming `String` is mutable | `replace`, `substring`, and concatenation return values; they do not modify the original string. | Assign the returned value or choose `char[]`/`StringBuilder` when mutation is required. |
| Repeated string concatenation | Building a growing immutable string in a loop can copy a quadratic total number of characters. | Use a `StringBuilder` and account for the final string copy. |
| Confusing length APIs | Arrays, strings, and collections expose different members. | Recall `array.length`, `text.length()`, and `list.size()`. |
| Null unboxing or absent map value | Assigning a missing `Integer` to `int` throws `NullPointerException`. | Use an appropriate default for absent keys, or explicitly test null; `getOrDefault` does not replace an explicitly stored null. |
| Primitive/wrapper confusion | `List<int>` is illegal; boxed values add allocation/indirection and `==` is unreliable for logical equality. | Use `int[]` for dense numeric state and `List<Integer>` when collection behavior is needed. |
| Overloaded list removal | On `List<Integer>`, `remove(1)` removes index `1`. | Use `remove(Integer.valueOf(1))` to remove the first matching value. |
| Accidental reference aliasing | Assigning an array/list variable copies a reference; cloning a two-dimensional array copies only the outer array. | Decide whether you need a new container, copied rows, or copied mutable elements. |
| Pass-by-value misconception | Reassigning a parameter does not replace the caller's variable, although mutating its referenced object is visible. | Return a new head/result and assign it at the call site; draw variables separately from objects. |
| Fixed-size or unmodifiable list | `Arrays.asList` is backed by the source array and rejects size changes; `List.of` rejects mutation and null elements. | Use `new ArrayList<>(...)` when you need a resizable list. `Arrays.asList(intArray)` is a one-element list containing that array, not a list of boxed integers. |
| Wrong empty-container behavior | `poll`/`peek` can return null; `pop`/`removeFirst` can throw on empty; null unboxing can then fail. | Make empty-result behavior explicit and guard before access. `ArrayDeque` and `PriorityQueue` reject null elements. |
| Wrong comparator | Subtraction may overflow; inconsistent comparisons corrupt ordering assumptions. | Use `Integer.compare`, `Long.compare`, or `Comparator.comparingInt(...).thenComparingInt(...)`; return zero for equal keys and maintain transitivity. |
| Wrong heap direction | Java `PriorityQueue` exposes the minimum by default. | State which item must be removed next; use `Comparator.reverseOrder()` for a natural-order max-heap. |
| Mutating a stored priority | A heap does not automatically reorganize after an element's comparison fields change. | Use immutable entries and remove/reinsert or use the algorithm's stale-entry strategy. |
| Assuming hash or heap iteration is sorted | Neither promises sorted iteration. | Use `TreeMap`/`TreeSet`, sort explicitly, or repeatedly poll a heap when ordered removal is required. |
| Deep recursive traversal | A skewed tree or long graph path can cause `StackOverflowError`. | Use iterative DFS/BFS when depth is uncontrolled; Java has no portable safe recursion-depth constant. |
| Wrong initial state | Java requires local-variable initialization, but array/field defaults may not match the algorithm's identity or unreachable state. | Initialize from the contract: first valid value, neutral element, or justified sentinel; use a separate computed flag when zero is a valid result. |
| Treating `char` as every Unicode character | A Java `char` is one UTF-16 code unit; some code points require two units. | State ASCII/lowercase/code-unit assumptions; use code-point iteration when the contract requires it, and clarify normalization separately. |
| Recomputing window state | The intended linear scan becomes quadratic. | Maintain counts/sums incrementally; justify why each boundary moves at most `n` times. |
| Sum window with arbitrary negatives | Expanding and shrinking no longer changes sums predictably. | Use prefix-state lookup or another method justified by constraints. |
| Wrong interval endpoint semantics | Touching intervals and equal-time events are processed incorrectly. | Clarify closed versus half-open intervals before choosing comparisons. |
| Greedy without a proof idea | An attractive local choice may block the optimum. | Seek a counterexample, then give an exchange or stays-ahead argument. |
| Incomplete or oversized DP state | Different futures collapse together, or irrelevant history explodes state count. | Define a sufficient state in a sentence; keep only history needed for future decisions. |
| Wrong DP update direction | A 0/1 item is reused, or an unlimited item is used only once. | Derive capacity direction from the dependency: backward for compressed 0/1, forward for the usual unbounded recurrence. |
| Hidden copying or omitted space | Slices, snapshots, sorting buffers, and recursion invalidate a stated bound. | Count their actual costs; pass indices to avoid unnecessary slices; separate auxiliary space from required output. |
| Coding before agreement | A correct program solves the wrong interpretation. | Restate the contract, examples, and invariant before typing. |
| Testing only the intended algorithm | A sound idea hides a wrong comparison or update in the code. | Trace actual statements on a minimal adversarial input and confirm termination. |

The operation contracts above follow the [Java collections APIs](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html). The guide's snippets remain compatible with Java 17.

### A five-point pre-submit scan

1. **Contract:** right return type, indices versus values, null/empty behavior, mutation allowed?
2. **Boundaries:** endpoints, last element, neighbor checks, overflow?
3. **Multiplicity:** duplicates, ties, object equality versus identity?
4. **State:** initialized, updated in the right order, snapshotted/restored correctly?
5. **Evidence:** trace actual code; include hidden Java operation costs, recursion, and output in analysis?

---

## 24. Code Templates

**Priority:** 🔴 Tier 1 for reconstructing common control flow; each algorithm keeps its own topic priority.

This is a **retrieval index to the canonical Java implementations in the topic sections**. Keep one maintained implementation per algorithm and use these cards to reconstruct it from a blank page before following the link. The topic implementation defines exact argument, mutation, empty-result, and numeric contracts; do not silently transfer one contract to a different problem.

Before adapting any template, fill in four blanks:

1. **State:** what does each variable/container mean?
2. **Invariant:** what is true before and after every iteration/call?
3. **Change:** which condition adds, removes, branches, or terminates?
4. **Result:** where is the answer valid and when is it updated?

For imports, node conventions, equality, and collection syntax, use the [Java essential reference](#java-for-dsa-interviews-essential-reference). A template is a control-flow shape supported by a proof, not a complete solution to memorize.

### 24.1 Array traversal

**🔴 Tier 1. Canonical Java:** [§4.1 traversal and running invariants](#41-traversal-and-running-invariants).

- **Reconstruct:** initialize a summary from the identity or first valid item, scan once, and update it. For a maximum, `best` describes the processed prefix.
- **Adapt/check:** empty-result contract, all-negative values, count versus sum versus extreme, and `long` for a potentially large sum. A method call copies the array reference, not the array.
- **Cost:** `O(n)` time and `O(1)` auxiliary state for one summary.

### 24.2 Frequency counting

**🔴 Tier 1. Canonical Java:** [§5.2 frequency tables and lookup](#52-frequency-tables-and-lookup-techniques).

- **Reconstruct:** count each processed value with `HashMap.getOrDefault` and `put`; a fixed `int[]` is simpler for a small known alphabet.
- **Adapt/check:** membership versus multiplicity, key equality, case/alphabet rules, and whether zero counts must be removed. Map size counts keys, including keys mapped to zero.
- **Cost:** expected `O(n)` time and `O(d)` map space for `d` distinct constant-cost keys; initializing a count array costs `O(alphabet)`.

### 24.3 Stack

**🔴 Tier 1. Canonical Java:** [§9.1 delimiters](#91-stack-fundamentals-and-parentheses).

- **Reconstruct:** use `Deque<Character>` backed by `ArrayDeque`; the stack contains exactly the unmatched opening delimiters from the processed prefix.
- **Adapt/check:** define non-delimiter input policy, guard before popping, verify matching type, and reject leftover open delimiters. In expression problems, preserve operand order.
- **Cost:** `O(n)` time and up to `O(n)` auxiliary space.

### 24.4 Queue and deque

**🔴 Tier 1 for FIFO; 🟡 Tier 3 for monotonic windows. Canonical Java:** [§14.3 BFS queue implementation](#143-graph-bfs-and-unweighted-shortest-paths), [§9.5 sliding-window maximum](#95-monotonic-deque); [§9.4 queue invariant](#94-queue-fundamentals-and-bfs-usage).

- **Reconstruct FIFO:** `offer` at the tail, `poll` from the head; mark graph states discovered before enqueuing. For levels, snapshot queue size before processing that layer.
- **Reconstruct monotonic deque:** expire old indices at the front, remove dominated indices at the back, append the new index, then emit the front after a full window exists.
- **Adapt/check:** `1 <= k <= n`, indices versus values, strictness for ties, and output versus auxiliary space. `ArrayDeque` rejects null.
- **Cost:** end insertions are amortized `O(1)`; sliding maxima take `O(n)` total time and `O(k)` auxiliary space because each index enters and leaves once.

### 24.5 Prefix sum and prefix-state lookup

**🔴 Tier 1. Canonical Java:** [§4.3 prefix sums](#43-prefix-sums), [§5.2 subarray-sum counting](#52-frequency-tables-and-lookup-techniques).

- **Reconstruct:** `prefix[i]` is the sum of the first `i` values; `[left, right)` sums to `prefix[right] - prefix[left]`. For inclusive `right`, use `prefix[right + 1]`.
- **Counting invariant:** the map holds frequencies of earlier prefixes. Seed zero with one occurrence; query `currentPrefix - target` before recording the current prefix.
- **Adapt/check:** `long` sums and answer count, initial zero, endpoint convention, frequency versus first/latest index. This relationship still works with negative values.
- **Cost:** prefix-array build `O(n)` time/space and `O(1)` queries; prefix-map counting expected `O(n)` time and `O(n)` auxiliary space.

### 24.6 Opposite-direction two pointers

**🔴 Tier 1. Canonical Java:** [§6.1 sorted two-sum scan](#61-opposite-direction-pointers).

- **Reconstruct:** compare endpoint sum with the target; sorted order proves which endpoint cannot work and may be discarded.
- **Adapt/check:** sorted-input requirement, distinct indices, duplicate policy, return values versus original indices, and widening before addition. Sorting changes positions and may mutate input.
- **Cost:** `O(n)` scan and `O(1)` auxiliary state; include sorting/copying if you introduce it.

### 24.7 Same-direction read/write pointers

**🔴 Tier 1. Canonical Java:** [§4.2 in-place compaction](#42-in-place-operations), [§6.2 read/write and merge pointers](#62-same-direction-and-parallel-pointers).

- **Reconstruct:** `read` inspects input; `write` is the next kept position. The prefix `[0, write)` contains the finalized result and forward compaction maintains `write <= read`.
- **Adapt/check:** keep predicate, stable relative order, empty input, and returned valid length. A Java array keeps its original capacity; trailing entries are outside the logical result.
- **Cost:** `O(n)` time and `O(1)` auxiliary space.

### 24.8 Fixed-size sliding window

**🔴 Tier 1. Canonical Java:** [§7.1 fixed-size window](#71-fixed-size-window).

- **Reconstruct:** build the first `k`-element aggregate, then add the entering value and remove the leaving value before updating the answer.
- **Adapt/check:** legal `k`, all-negative values, outgoing index, `long` sum, and whether the aggregate supports efficient removal. Extrema need a different maintained structure.
- **Cost:** `O(n)` time and `O(1)` auxiliary space for a sum.

### 24.9 Variable-size sliding window

**🔴 Tier 1. Canonical Java:** [§7.3 frequency-based windows](#73-frequency-based-windows).

- **Reconstruct:** expand right, update counts, then shrink left in a `while` loop until validity is restored. For “at most `k` distinct,” remove zero-count keys before using `map.size()`.
- **Adapt/check:** state the repair property, define when the answer updates, and distinguish longest-valid from shortest-covering windows. Arbitrary negative sums do not obey the usual sum-window repair rule.
- **Cost:** expected `O(n)` with hash state because both boundaries advance at most `n` times; auxiliary space is the retained distinct-key state, at most `O(n)`.

### 24.10 Binary search: exact match

**🔴 Tier 1. Canonical Java:** [§10.1 exact binary search](#101-exact-binary-search).

- **Reconstruct:** choose one range convention; if the target exists it remains in the candidate range. Every nonreturning comparison strictly shrinks that range.
- **Adapt/check:** empty input, absent target, duplicate semantics, returned sentinel, and safe midpoint arithmetic. An arbitrary exact match does not promise the first duplicate.
- **Cost:** `O(log n)` time and `O(1)` auxiliary space over indexed data.

### 24.11 Binary search: lower bound, upper bound, and first true

**🔴 Tier 1 for array bounds; 🟠 Tier 2 for answer search. Canonical Java:** [§10.2 lower/upper bounds](#102-lower-bound-and-upper-bound), [§10.3 binary search on answer](#103-binary-search-on-answer).

- **Reconstruct:** find the first true boundary. Lower bound uses `value >= target`; upper bound uses `value > target`. Every discarded left position is false and every discarded right position is true; the insertion boundary can equal `n`.
- **Adapt/check:** prove predicate monotonicity, justify numeric bounds, distinguish sentinel from a valid index, and handle no feasible candidate. For nonnegative `long` bounds with `low <= high`, `high - low` is safe.
- **Cost:** `O(log n)` indexed comparisons; answer search is `O(feasibilityCost * log(domainSize + 1))`.

### 24.12 Linked-list reversal and fast/slow pointers

**🟠 Tier 2. Canonical Java:** [§8.2 reversal](#82-reversal), [§8.3 fast/slow pointers](#83-fast-and-slow-pointers).

- **Reconstruct reversal:** save the next node, redirect the current link to the reversed prefix, then advance. Return the new head and assign it at the caller.
- **Reconstruct fast/slow:** guard `fast != null && fast.next != null`, move at different speeds, and compare node identity with `==` for a meeting.
- **Adapt/check:** empty list, midpoint convention for even length, cyclic-input assumptions, and reconnecting a reversed segment. Changing `node.next` mutates a shared object; reassigning a parameter changes only its local copy of the reference.
- **Cost:** `O(n)` time and `O(1)` auxiliary space for reversal, middle, or cycle detection.

### 24.13 Monotonic stack

**🟠 Tier 2. Canonical Java:** [§9.3 monotonic stack](#93-monotonic-stack).

- **Reconstruct:** keep unresolved indices in monotone value order. When the new value breaks the rule, pop indices and finalize their answers.
- **Adapt/check:** increasing/decreasing order, strictly greater versus greater-or-equal, answer at pop versus current index, distances versus values, and circular traversal.
- **Cost:** `O(n)` total time and `O(n)` auxiliary space; the nested loop is linear because each index enters and leaves once.

### 24.14 Tree DFS and BFS

**🔴 Tier 1. Canonical Java:** [§12.2 recursive DFS and subtree contracts](#122-recursive-dfs-traversals), [§12.3 iterative DFS](#123-iterative-dfs-traversals), [§12.4 level order](#124-tree-bfs-level-order-traversal).

- **Reconstruct DFS:** define what one call returns, handle null, obtain child results, then combine at the correct traversal moment.
- **Reconstruct BFS:** enqueue a non-null root, snapshot level size, process exactly that layer, and enqueue only non-null children.
- **Adapt/check:** node/edge height convention, carried path state versus returned state, identity versus value, and recursion depth.
- **Cost:** `O(n)` time, `O(h)` DFS stack or `O(w)` BFS frontier, excluding returned traversal output. Use iterative traversal when depth may exhaust Java's call stack.

### 24.15 Graph BFS

**🔴 Tier 1. Canonical Java:** [§14.3 BFS and unweighted shortest paths](#143-graph-bfs-and-unweighted-shortest-paths), [§14.4 multi-source BFS](#144-multi-source-bfs).

- **Reconstruct:** initialize source distance, mark on enqueue, then set each undiscovered neighbor's distance to current distance plus one. Queue order is nondecreasing edge distance.
- **Adapt/check:** direction, source/target labels, full visited-state key, unreachable result, and parent storage if a path must be reconstructed. Multi-source BFS starts all sources at distance zero.
- **Cost:** `O(V + E)` time and `O(V)` auxiliary space excluding the adjacency list.

### 24.16 Grid traversal

**🔴 Tier 1. Canonical Java:** [§14.6 grid as a graph](#146-grid-as-a-graph).

- **Reconstruct:** generate neighbors from direction offsets, check bounds and eligibility before indexing/enqueuing, and mark at discovery. Each queued cell is already known valid and discovered.
- **Adapt/check:** rectangular or ragged grid, empty rows, four/eight directions, in-place visited marking versus separate storage, and whether coordinates are output. `new int[] {row, col}` is convenient state; allocate a distinct entry for each queued coordinate.
- **Cost:** `O(rows * cols)` time and up to `O(rows * cols)` auxiliary space for a rectangular grid.

### 24.17 Iterative graph DFS

**🔴 Tier 1. Canonical Java:** [§14.2 graph DFS](#142-graph-dfs).

- **Reconstruct:** use an explicit stack, mark discovered vertices before pushing, and process each reachable vertex once. For components, start again from every unvisited vertex.
- **Adapt/check:** an ordinary reachability stack need not reproduce recursive DFS entry/exit order. Use explicit frames or an appropriate postorder method when exit state, directed recursion-path membership, or exact DFS order matters.
- **Cost:** `O(V + E)` time and `O(V)` auxiliary space for the marked-on-push reachability version, excluding the graph.

### 24.18 Topological sort (Kahn's algorithm)

**🟠 Tier 2. Canonical Java:** [§14.8 topological sorting](#148-topological-sorting).

- **Reconstruct:** build edges from prerequisite to dependent, count indegrees, enqueue every zero-indegree vertex, emit one vertex, and decrement its outgoing neighbors. The queue contains currently unblocked vertices.
- **Adapt/check:** isolated vertices, duplicate-edge policy, requested order versus feasibility, and cycle detection when processed count is less than `V`. Multiple valid orders are normal.
- **Cost:** `O(V + E)` time and `O(V)` auxiliary space beyond the graph and returned order.

<a id="2419-union-find-disjoint-set-union"></a>
### 24.19 Union-Find / Disjoint Set Union

**🟠 Tier 2. Canonical Java:** [§14.9 DSU](#149-union-find-disjoint-set-union-dsu).

- **Reconstruct:** initialize each vertex as its own root; find representatives with compression/path halving; attach the smaller component to the larger; decrement component count only on a successful merge.
- **Adapt/check:** valid labels, root-only size/rank metadata, and same-component union. `parent[x]` need not be the representative until you follow the chain. DSU does not answer directed reachability or reconstruct a graph path.
- **Cost:** `O(n)` initialization/space; `O(alpha(n))` amortized per operation with compression and union by size/rank.

### 24.20 Heap: retain Top-K

**🟠 Tier 2. Canonical Java:** [§13.2 Top-K](#132-top-k-and-kth-element-pattern), [§13.3 k-way merge](#133-k-way-merge).

- **Reconstruct Top-K:** keep a size-`k` min-heap for the largest values; its root is the weakest retained winner. Replace that root only with a better candidate.
- **Reconstruct merge:** keep one frontier entry per sorted source; after polling an entry, offer only the next item from that source.
- **Adapt/check:** `k <= 0`, `k > n`, kth value versus all retained values, output order, immutable priority fields, and safe comparators. A tie-breaker is required only for a defined secondary order; custom entries still need a comparison rule.
- **Cost:** Top-K `O(n log(m + 1))` time and `O(m)` auxiliary space for `m = min(k, n)` and positive `k`; k-way merge `O(N log(k + 1))` time and `O(k)` frontier space for `N` items. Polling retained items produces ordered output at an additional `O(m log(m + 1))` cost, within the Top-K bound.

### 24.21 Dijkstra's shortest path

**🟠 Tier 2. Canonical Java:** [§14.12 Dijkstra](#1412-dijkstras-algorithm).

- **Reconstruct:** maintain `long[] distance`, put the source at distance zero in a min-heap, discard stale popped entries, and relax outgoing nonnegative edges.
- **Adapt/check:** nonnegative weights, edge direction, representable distance range and infinity sentinel, unreachable vertices, and early target exit only on a non-stale extraction. A finite answer equal to a reserved infinity sentinel needs a different contract.
- **Cost:** with duplicate entries, `O(V + E log(E + 1))` time including initialization/edge checks; `O(V + E)` auxiliary space because the heap may contain `O(E)` entries. For simple graphs this is commonly expressed as `O((V + E) log(V + 1))`.

### 24.22 Backtracking: choose, explore, unchoose

**🟠 Tier 2. Canonical Java:** [§15.3 backtracking contract](#153-general-backtracking-template), [§15.4 subsets](#154-subsets-choose-or-skip), [§15.6 permutations](#156-permutations-used-choice-search).

- **Reconstruct:** `path` contains exactly the choices on the current branch; choose, recurse with the correct next state, and undo. Save completed answers with a new list snapshot.
- **Adapt/check:** completion condition, reuse versus advance, start index versus used array, duplicate skipping at one depth, and safe pruning. A shallow list snapshot is enough for immutable `Integer` elements, not arbitrary mutable objects.
- **Cost:** derive the actual search tree. Enumerating all subsets has `O(n * 2^n)` time/output space and `O(n)` auxiliary path/stack space; other choice trees differ.

### 24.23 Greedy frontier

**🟠 Tier 2. Canonical Java:** [§16.4 reachability frontier](#164-reachability-frontier), [§16.1 greedy proof ideas](#161-greedy-reasoning-choice-invariant-proof).

- **Reconstruct:** retain the farthest reachable index. Before using a position's jump, prove the position is reachable; all indices through the frontier are reachable under the nonnegative-jump contract.
- **Adapt/check:** empty-input policy, nonnegative jumps, widening/capping reach arithmetic, and whether the frontier truly dominates discarded histories.
- **Cost:** `O(n)` time and `O(1)` auxiliary space. A different greedy rule needs its own safe-choice argument.

### 24.24 Dynamic programming: memoization and tabulation

**🟠 Tier 2. Canonical Java:** [§18.6 full take/skip evolution](#186-worked-evolution-from-brute-force-to-optimized-dp), [§18.3 DP design](#183-the-six-part-dp-design-process).

- **Reconstruct:** define state, choices, transition, base cases, evaluation order, and requested answer. For nonadjacent score with skipping allowed, a suffix state chooses between skipping `i` and taking `value[i]` plus the result from `i + 2`.
- **Adapt/check:** whether empty selection is allowed, a separate computed flag when zero/negative results are legitimate, `long` scores, complete memo keys, and dependencies before compressing storage. Keep parent/choice information if reconstruction is requested.
- **Cost:** states times transition work; this example takes `O(n)` time, `O(n)` memo plus recursion space, or `O(1)` rolling state with iteration.

### 24.25 Trie

**🟡 Tier 3. Canonical Java:** [§19.2 trie implementation](#192-standard-trie-implementation), [§19.3 prefix variants](#193-prefix-search-counts-and-deletion).

- **Reconstruct:** descend through one edge per alphabet symbol, create missing nodes only on insertion, and mark the terminal node separately from its prefix path.
- **Adapt/check:** fixed alphabet versus mapped children, lowercase/code-unit/code-point contract, empty word, duplicate insertion, exact versus prefix search, and terminal counts when deletion is needed.
- **Cost:** `O(L)` per key with fixed-alphabet constant-time edges; mapped edges give expected `O(L)` under constant-cost key hashing. Space depends on distinct prefix nodes and child-storage representation.

### 24.26 Interval merge

**🔴 Tier 1. Canonical Java:** [§17.2 interval merge](#172-merge-overlapping-intervals), [§17.1 endpoint semantics](#171-endpoint-semantics-and-overlap).

- **Reconstruct:** sort by start; extend only the latest merged interval while overlap continues. Completed output is sorted, disjoint, and covers all processed input.
- **Adapt/check:** closed versus half-open boundaries, nested/equal intervals, valid start/end ordering, comparator overflow, and mutation policy. Sorting an outer `int[][]` array rearranges row references; copying only the outer array does not isolate row mutation.
- **Cost:** `O(n log n)` time with Java object-array sorting; account for `O(n)` worst-case sorting/copy workspace and `O(n)` output separately as used by the canonical implementation.

### Template practice rule

For each Tier 1 pattern, reconstruct the invariant and Java control flow unaided, then change one constraint and explain the adaptation. For Tier 2, reconstruct common forms with the same care. Tier 3 requires a selected basic implementation; Tier 4 usually requires recognition only. Look up rare APIs after the retrieval attempt and record the general rule when an API mistake changes correctness. Follow the [practice ladder and review schedule](#25-how-to-learn-dsa-effectively) rather than repeatedly typing unchanged templates.

---

## 25. How to Learn DSA Effectively

The goal is durable retrieval and transfer to unfamiliar problems. Use this cycle:

> **Learn → Implement → Solve → Struggle → Review → Re-solve → Generalize**

### Start the next study session

Use this order when the guide feels too large:

1. **Due review:** take one due mistake-log item cold, with its notes hidden.
2. **Repair or advance:** if its invariant or Java rule fails again, drill that exact gap; otherwise choose the first unmet prerequisite gate in the [roadmap](#27-learning-roadmap).
3. **One ladder step:** solve the next mechanics, canonical, variation, or mixed problem for that pattern. Do not start several new topics at once.
4. **Close the loop:** explain the rule aloud, record hint level and the first error, and schedule a cold revisit.

For a 60-minute session, a starting allocation is 10 minutes of recall, 30 minutes of deliberate solving, 15 minutes of correction/testing, and 5 minutes of logging. Adjust to the problem; an unfinished attempt can still produce a useful invariant, counterexample, and next step. If an interview is close, replace new breadth with the highest recurring Tier 1/2 weakness and mixed practice.

### 25.1 What each step contributes

| Step | What to do | Why it matters | Evidence that the step worked |
|---|---|---|---|
| Learn | Understand the motivating bottleneck, invariant, and complexity. Trace one small example. | Facts without a mental model disappear quickly. | You can explain why the method works without code. |
| Implement | Reconstruct the core operation in Java. | Converts recognition into executable detail. | During learning you need only occasional API references; common operations later work unaided. |
| Solve | Start with a learning problem, then a standard variation. | Builds the clue-to-pattern connection. | You choose the technique for a reason, not from the tag. |
| Struggle | Explore examples, brute force, and state before seeking help. | Retrieval effort strengthens memory and exposes gaps. | Your notes contain attempted reasoning, not just copied code. |
| Review | Compare with a strong solution and classify the difference. | Turns failure into a reusable rule. | You can name the missed clue or broken invariant. |
| Re-solve | Return later with the solution closed. | Tests durable recall rather than short-term imitation. | You solve and explain from a blank page. |
| Generalize | Change a constraint and predict the needed approach. | Builds transfer to unseen questions. | You can say when the pattern stops working. |

### 25.2 Learn concepts actively

For a new technique, make a compact note containing:

- the problem it solves;
- the invariant or state definition;
- recognition clues and false friends;
- operation complexities;
- one skeletal example;
- two common failure modes;
- one comparison with an alternative.

Then close the note and recreate it from memory. Drawing pointer movement, a recursion tree, BFS layers, or a DP table is useful when it makes state transitions concrete.

Keep three kinds of knowledge separate:

| Retrieve from memory | Understand and derive | Safe to look up occasionally |
|---|---|---|
| Common array/String/list/map/set/deque/heap operations; sorting/comparator syntax; equality, null, arithmetic, and common costs | Why an invariant permits an update; recursion contracts; graph modeling; greedy proof; DP state/transition and dependency order | Rare `NavigableMap` methods; specialized APIs; selected Tier 3/4 algorithms |

Memorize a small API vocabulary and a few boundary conventions. Reconstruct algorithms from their invariants. Never treat memorizing a complete problem solution as the goal. The [Java reference](#java-for-dsa-interviews-essential-reference) supplies the detailed memory/understanding/lookup split.

### 25.3 Use a deliberate problem ladder

For each high-value pattern:

1. **Mechanics problem:** isolates the data structure or loop.
2. **Canonical problem:** uses the standard invariant.
3. **Variation:** changes output, constraints, or update timing.
4. **Mixed problem:** combines it with another pattern.
5. **Cold revisit:** no topic label and no notes.

Label the anchor problem **⭐ Canonical Interview Problem** in your tracker. For example, a hashing ladder is: build a frequency table → Two Sum → Group Anagrams (a changed key) → Subarray Sum Equals K (prefix sums plus hashing) → a cold, untagged revisit. The mixed problem belongs after its other prerequisite is learned.

Use these as initial doses **per pattern**, not quotas per broad chapter:

| Priority | Initial distinct problems | Retrieval target |
|---|---:|---|
| 🔴 Tier 1 | 3–4 across the ladder; add a distinct variation if needed | Reconstruct common forms, solve an untagged variation, and pass at least two delayed attempts |
| 🟠 Tier 2 | 3–4 across the ladder | Reconstruct standard forms and pass two delayed attempts; expand when a variation exposes a gap |
| 🟡 Tier 3 | 1–2 chosen basics | Explain when it applies and implement a basic form if selected for study |
| ⚪ Tier 4 | 0 unless a target requires it | Recognition and alternatives; deepen only from evidence |

Cold revisits are additional attempts, not new-problem quotas. A mechanics exercise may be brief if the Java operation is already automatic. Increase volume only when a distinct transfer gap remains; move on when evidence meets the gate, while keeping reviews scheduled.

### 25.4 Struggle and use hints correctly

Time-box based on your stage and interview length. For a standard problem, a learner might explore independently for roughly 20–30 minutes; shorten that during breadth-building and extend it during mock practice. Productive struggle produces examples, a brute force, or hypotheses. Repeating the same thought is not productive.

Use the smallest hint that unblocks the next reasoning step:

1. restate the constraints;
2. reveal the broad pattern;
3. reveal the invariant/state;
4. reveal pseudocode;
5. inspect full code only last.

After any hint, close it and complete the reasoning yourself. Record the clue you missed.

### 25.5 Learn from a solution without memorizing it

When reading an editorial or another person's code:

1. identify why your approach failed;
2. explain the new invariant in plain language;
3. derive its complexity;
4. close the source;
5. implement from a blank file;
6. test it with an adversarial case;
7. write one variation that would break or alter the method;
8. re-solve later.

Do not copy code line by line. Syntax familiarity can imitate competence while leaving the state transition unexplained.

### 25.6 Review with active recall and spaced repetition

Review based on weakness rather than a rigid calendar. A useful starting cadence is:

- same day: explain the insight and repair the code;
- 2–3 days: re-solve from scratch; an outline is a lighter review, not implementation evidence;
- 1 week: solve cold;
- 2–4 weeks: solve a variation or include it in a mixed set;
- before interviews: prioritize items still marked uncertain in the mistake log.

Successful cold recalls can be spaced farther apart; failures return sooner. Ask “What clue points to this pattern?” before asking “What was the code?” Record whether you independently recognized, explained, implemented, tested, and analyzed it. An immediate reconstruction after reading a solution is repair; it does not pass a delayed mastery gate.

### 25.7 Debug systematically

When a solution fails:

1. minimize the failing input;
2. state expected versus actual output;
3. trace only variables belonging to the invariant;
4. locate the first state divergence;
5. classify the cause—model, boundary, update order, API, or complexity;
6. add the case to a small regression set;
7. fix the rule, not only that input.

For recursion, log call state and return value. For windows, log both boundaries and counts. For graphs, log discovery time. For DP, write the sentence represented by the wrong cell.

### 25.8 Practice explanation and writing code without IDE assistance

- Explain approaches aloud before typing.
- Periodically code in a plain editor without autocomplete, running only after a manual trace.
- Retrieve the common Java array, String, map, set, `ArrayDeque`, `PriorityQueue`, sorting, and comparator APIs without autocomplete. Look up infrequent APIs after an honest recall attempt.
- Practice correcting a bug while narrating calmly.
- Do not ban the IDE entirely; use it for feedback during learning, then reduce assistance to test recall.

### 25.9 Mock interviews

Begin short mixed sessions once you have two or three usable patterns; start full mocks after core easy problems and some standard mediums are reliable. Phase 8 increases mock frequency, but is not the first time you mix topics. Run mocks under realistic time and communication constraints:

- five minutes to clarify and outline;
- most time on reasoning and implementation;
- final minutes for trace, complexity, and alternatives.

Review communication separately from algorithm choice and coding accuracy. A useful scorecard tracks: contract clarity, brute-force baseline, pattern justification, invariant, code correctness, testing, complexity, and response to hints.

### 25.10 What not to do

| Inefficient habit | Why it underperforms | Better alternative |
|---|---|---|
| Solve hundreds of random problems | Repeats familiar patterns while leaving gaps invisible. | Use a curated pattern ladder and tag the lesson after each problem. |
| Memorize complete solutions | Small wording changes break recall. | Memorize questions/invariants, then derive code. |
| Study advanced algorithms early | Consumes time with low interview return. | Master Tier 1, then common Tier 2, then specialize from evidence. |
| Spend hours stuck with no new hypothesis | Rehearses confusion rather than reasoning. | Time-box, take a minimal hint, and document the missing clue. |
| Read the solution immediately | Removes retrieval practice. | Produce a brute force, example, and at least one optimization hypothesis first. |
| Only watch tutorials | Recognition feels fluent while implementation remains weak. | Pause, predict, trace, and code from a blank file. |
| Ignore complexity | You cannot justify suitability or find the bottleneck. | Derive time and auxiliary space after every approach. |
| Never revisit accepted problems | Short-term context fades and creates false mastery. | Schedule cold re-solves and variations. |
| Obsess over acceptance streaks | Optimizes a platform metric instead of interview skill. | Track independent solves, explanations, and missed clues. |
| Always filter by topic | Supplies the pattern clue artificially. | Mix topics once mechanics are learned. |
| Rewrite only familiar templates | Trains typing, not adaptation. | Change constraints and explain which lines/invariants must change. |
| Use hints without recording them | The same recognition failure repeats. | Log hint level and the exact clue it supplied. |
| Avoid easy problems | Leaves mechanics slow and error-prone. | Use easies to automate fundamentals, then move quickly to standard mediums. |
| Chase only hard problems | Feedback is sparse and advanced tricks dominate. | Build broad medium-level reliability before selective hard problems. |

---

## 26. Mistake Log System

A mistake log is a spaced-repetition queue built from your own weaknesses. Keep one row per meaningful failed attempt—not every typo.

### Recommended schema

| Field | What to record |
|---|---|
| Problem | Name/link or a platform-neutral description |
| Topic | Broad area such as graph or DP |
| Pattern | Specific technique such as multi-source BFS |
| Difficulty | Your experienced difficulty, not only the platform label |
| My original approach | Brief algorithm and intended complexity |
| Why it failed | First incorrect assumption, invariant, or implementation step |
| Correct insight | The smallest idea that unlocks the solution |
| Better approach | Short outline, not copied full code |
| Time complexity | Derived final time |
| Space complexity | Auxiliary space, plus output if relevant |
| Mistake category | Pattern recognition, conceptual, invariant/state, boundary, implementation, Java/API, complexity, testing, or communication |
| What clue I missed | Exact phrase, constraint, or structural fact |
| Hint level used | No hint, pattern, invariant, pseudocode, or full solution |
| Date solved | First correct implementation date |
| Date to review | Next active-recall date |
| Could I solve it again without help? | No / uncertain / yes, with dated recognition, implementation, explanation, and testing evidence |
| General rule and regression case | One reusable correction plus the smallest input exposing it |

Copyable entry:

```text
Problem: [name or link]

- Topic / pattern:
- Difficulty to me:
- Original approach:
- Why it failed:
- Correct insight:
- Better approach:
- Time / auxiliary space:
- Mistake category:
- General rule / smallest failing case:
- Clue I missed:
- Hint level used:
- Date solved:
- Next review:
- Cold re-solve result:
- One variation:
```

### Mistake categories and responses

| Category | Example | Corrective drill |
|---|---|---|
| Pattern recognition | Missed monotone feasibility or used a window with arbitrary negative sums | Compare five untagged prompts; name a supporting clue and a disqualifying constraint for each candidate |
| Conceptual | Believed the first DFS route was the shortest, or assumed an attractive greedy rule was always optimal | Build a minimal counterexample; explain the correct theorem or safe-choice argument |
| Invariant/state | DP omitted remaining capacity; a window answer was updated before validity was restored | Write state contracts and trace the first divergent update on three tiny cases |
| Boundary | Lower bound failed on empty input or returned `n` was indexed | Trace sizes `0`, `1`, `2`, duplicates, and absent targets using one range convention |
| Implementation | Lost a linked-list node or forgot the backtracking undo | Trace the assignments and branch restoration before rewriting the method |
| Java/API | Used `==` for logical String equality, or unboxed a missing map value | Write the general semantic rule, then a minimal snippet/case that distinguishes correct from incorrect behavior |
| Complexity | Repeated substring copies or `ArrayList.remove(0)` made a scan quadratic | Annotate hidden allocation, copying, shifting, key, comparator, and recursion costs |
| Testing | Only traced a normal example; missed zero, duplicate, no-solution, or cyclic input | Derive one test from each precondition, boundary, and invariant rather than from the sample list alone |
| Communication | Began coding without a contract or stayed silent while stuck | Record a two-minute outline: contract, baseline, bottleneck, invariant, complexity, and first test |

Choose the root cause as the primary category; use one secondary category only if it adds a distinct repair. A boundary bug caused by a misunderstood window state primarily needs invariant practice, not more random edge cases.

**Worked Java/API entry**

- **Problem:** group or compare text values created from input.
- **Failure:** two separately created strings with contents `cat` compared false because I used `==`.
- **General rule:** `==` compares object identity; `.equals()` compares String contents. When null is legal, use `Objects.equals(a, b)`. Interned literals can hide this error in tests.
- **Smallest regression:** two `new String("cat")` objects are equal by contents; two different strings are not; include a null case only if the contract allows null.
- **Repair and review:** explain identity versus equality without code, write a small comparison from a blank editor, then revisit in a map-key or grouping problem after a delay.

Bad note: “I forgot line 8.” Useful note: “I used identity comparison for logical String equality; test separately allocated equal strings.” Record the reusable rule rather than merely a forgotten method name.

### Revision workflow

1. Review entries due today before selecting new random problems.
2. Hide the solution and reconstruct the clue, invariant, and complexity.
3. Re-solve failures; for successful recalls, solve a variation or explain one.
4. If the same category repeats, pause problem volume and drill that micro-skill.
5. Promote an item to “yes” only after a delayed cold solve with a clear explanation.
6. Archive mastered entries but sample them periodically in mixed mocks.

The log should become shorter in explanation and richer in generalizable rules over time. “Forgot line 8” is not useful; “updated the answer before restoring the window invariant” is.

---

## 27. Learning Roadmap

Progress by mastery gates rather than calendar time. Section numbers are stable reference addresses; this roadmap supplies the **study order**, which deliberately introduces sorting before comparator-dependent topics and greedy/interval reasoning before DP. Continue reviewing earlier patterns throughout later phases.

**What to study next:** select the first phase whose prerequisite or mastery gate you cannot demonstrate. Inside it, take one due review, then the next unlearned core pattern in the listed order. Use the [problem ladder](#253-use-a-deliberate-problem-ladder) and [mistake log](#26-mistake-log-system); an accepted submission alone does not advance a gate. If you already know a phase, pass a cold diagnostic and move forward instead of rereading every page.

<a id="phase-1-foundations"></a>
### Phase 1 — Foundations

**Learning objectives:** write simple Java methods, reason about resource costs, and explain object/reference behavior that affects algorithms.

- **Prerequisites:** basic conditionals and loops; learn the small Java essentials alongside the first exercises if needed.
- **Study order:** [Java reference](#java-for-dsa-interviews-essential-reference) memory items → [§3 foundations](#3-complexity-analysis-and-foundations) → [§4 array/String traversal and matrices](#4-arrays-strings).
- **🔴 Tier 1 — Must Master:** arrays, String immutability/equality, primitive arithmetic and `long` promotion, pass-by-value/reference mutation, complexity, recursion contracts/base cases, traversal, and simple tests.
- **🟠 Tier 2 — Very Important:** amortized analysis, recursion-tree reasoning, logarithms, gcd, and basic modular arithmetic.
- **🟡 Tier 3 — Nice to Know:** bit operations and masks; **⚪ Tier 4:** advanced number theory and formal complexity proofs.
- **Recommended practice:** derive costs of short loops; scan/reverse an array; compare String contents; trace a recursive sum and an aliased array; handle empty/non-square matrices.
- **Mastery gate:** write a small Java method from a blank file, explain mutation versus parameter reassignment, promote before overflowing arithmetic, and derive time/auxiliary/output costs from the actual operations.

<a id="phase-2-core-data-structures"></a>
### Phase 2 — Core Data Structures

**Learning objectives:** choose storage from operations and manipulate collections/node links correctly.

- **Prerequisites:** Phase 1; basic classes and object references for linked nodes.
- **Study order:** [§5 hashing](#5-hashing) → basic [§9 stack/queue/deque](#9-stacks-queues-deques) → [§8 linked lists](#8-linked-lists). Learn `ArrayList` versus primitive arrays as needed.
- **🔴 Tier 1 — Must Master:** key→index/count/group, `HashMap`/`HashSet`, ordinary LIFO/FIFO operations with `ArrayDeque`, delimiter validation, and equality/null rules.
- **🟠 Tier 2 — Very Important:** linked-list reversal/merge, dummy nodes, fast/slow pointers, and node identity. Leave monotonic-stack variations for Phase 3.
- **🟡 Tier 3 — Nice to Know:** detailed doubly linked-list mechanics and occasional ordered-map neighbor APIs. The map plus list LRU design is a **Tier 2** combination to revisit after both components are familiar.
- **⚪ Tier 4 — Low Priority / Specialized:** implementing hash-table internals beyond collision/equality awareness.
- **Recommended practice:** frequency counter → Two Sum → grouping; bracket validation; queue simulation; reversal/middle/cycle exercises. Begin one short **untagged mixed exercise** each week as soon as two or three patterns are usable.
- **Mastery gate:** select list/map/set/deque for a stated operation, retrieve common APIs without autocomplete, explain expected/amortized costs, avoid null-unboxing and identity-equality bugs, and rewire three nodes without losing a link.

<a id="phase-3-core-interview-patterns"></a>
### Phase 3 — Core Interview Patterns

**Learning objectives:** turn order, contiguity, and dominance into justified scans; learn the greedy/DP distinction before DP.

- **Prerequisites:** Phases 1–2, especially arrays, hashing, and collection mutation.
- **Study order:** [§11 library sorting and safe comparators](#11-sorting) → prefix sums → [§6 two pointers](#6-two-pointers) → [§7 windows](#7-sliding-window) → [§10 binary search/bounds](#10-binary-search) → monotonic stack → [§13 heap/Top-K](#13-heaps-priority-queues) → [§16 greedy proof and frontier](#16-greedy-algorithms) plus [§17 intervals](#17-intervals). Study merge/conflicts before selection/resources; study heap basics before heap scheduling.
- **🔴 Tier 1 — Must Master:** sorting as a tool, safe comparators, prefix sums, pointers, fixed/variable windows, exact/boundary search, and interval merge/insert/conflict detection.
- **🟠 Tier 2 — Very Important:** monotonic stack, answer search, heaps/Top-K, greedy safe-choice arguments, interval selection, meeting-room resources, and event counting.
- **🟡 Tier 3 — Nice to Know:** difference arrays, monotonic deque, quickselect, running median; **⚪ Tier 4:** dynamic range-query trees.
- **Recommended practice:** use the five-step ladder for each pattern. Mix prefix sums with hashing, sorting with pointers, and heaps with interval resources. For a greedy proposal, find a counterexample or explain the exchange/stays-ahead proof.
- **Mastery gate:** distinguish window from prefix lookup, scan from binary search, and heap from sorting; reconstruct a safe comparator and lower bound; define endpoint semantics; justify a greedy frontier and explain why some choices instead require remembering alternatives.

<a id="phase-4-trees-and-graphs"></a>
### Phase 4 — Trees and Graphs

**Learning objectives:** model structure, traverse once, return useful subtree state, and choose path/connectivity algorithms.

- **Prerequisites:** recursion, stack/queue, hashing, and heap basics. Revisit only the missing prerequisite instead of restarting a whole phase.
- **Study order:** [§12 tree DFS/BFS and BST](#12-trees) → [§14 graph representation, DFS/BFS, components and grids](#14-graphs) → cycle detection/topological order → multi-source BFS/bipartite checks → DSU → Dijkstra. Add tree construction/serialization after traversal is reliable.
- **🔴 Tier 1 — Must Master:** tree traversal and subtree contracts, BST search/validation, adjacency lists, graph BFS/DFS, components, grid traversal, and unweighted shortest paths.
- **🟠 Tier 2 — Very Important:** LCA, tree construction/serialization, directed/undirected cycles, topological sort, multi-source BFS, bipartite checks, DSU, and Dijkstra.
- **🟡 Tier 3 — Nice to Know:** MST and basic Bellman–Ford/Floyd–Warshall; **⚪ Tier 4:** SCC, max flow, and other advanced graph families unless targeted.
- **Recommended practice:** draw every graph and label direction/weight assumptions; compare DFS with BFS on a shared input; solve one subtree-return, level, dependency, connectivity, and weighted-path problem. Continue mixed exercises from earlier phases.
- **Mastery gate:** derive `O(V + E)`, mark visited at discovery, cover disconnected components, explain BFS versus Dijkstra, return exactly the subtree state the parent needs, and replace deep recursion with an appropriate iterative traversal.

<a id="phase-5-recursion-and-backtracking"></a>
### Phase 5 — Recursion and Backtracking

**Learning objectives:** represent a choice tree, isolate branch state, prune safely, and count search/output costs.

- **Prerequisites:** recursion contracts and DFS; mutable list/reference behavior.
- **Study order:** [§15 recursion-tree reasoning](#15-recursion-backtracking) → subsets → combinations → permutations → duplicate handling → constraint/board search.
- **🔴 Tier 1 — Must Master:** existing recursion and mutation fundamentals; **🟠 Tier 2:** common enumeration families, choose/explore/unchoose, snapshotting results, and safe pruning.
- **🟡 Tier 3 — Nice to Know:** bitmask enumeration and advanced pruning heuristics; **⚪ Tier 4:** highly optimized combinatorial search.
- **Recommended practice:** draw tiny choice trees; implement each basic family from a blank page; add duplicate constraints and one board-search variation; cold revisit with topic labels hidden.
- **Mastery gate:** define path/state/completion, restore state after each branch, explain shallow versus deeper snapshots, and count unavoidable output separately from auxiliary memory.

<a id="phase-6-dynamic-programming"></a>
### Phase 6 — Dynamic Programming

**Learning objectives:** derive a state graph from decisions, reuse repeated states, choose evaluation order, and compress only proven dependencies.

- **Prerequisites:** recursion/backtracking, array/matrix indexing, complexity, and the greedy-versus-remembering-alternatives distinction from Phase 3.
- **Study order:** [§18 recognition and six-part design](#18-dynamic-programming) → worked take/skip recursion/memo/table evolution → 1D and grid DP → basic 0/1 knapsack and minimum Coin Change → common subsequence DP → selected counting/reconstruction/space optimization.
- **🟠 Tier 2 — Very Important:** core state design, memoization/tabulation, 1D/grid DP, basic 0/1 knapsack, minimum Coin Change and its reusable-item recurrence, common subsequences, and safe space reduction. These need strong standard-form fluency; the breadth of DP does not make every advanced family mandatory.
- **🟡 Tier 3 — Nice to Know:** further unbounded counting variants, interval/tree DP, and extensive reconstruction variants; **⚪ Tier 4:** bitmask DP and specialized optimization families.
- **Recommended practice:** derive brute-force choices, state, and transition first. Use memoization to expose repeated states, convert representative examples to tables, and optimize only after correctness tests. Mix feasibility, counting, and optimization; explain why loop order changes 0/1 reuse or combination/permutation counting.
- **Mastery gate:** define all six design decisions, include every future-relevant dimension, derive states times transition work, convert a standard recurrence to a valid iteration order, and recognize a simpler greedy or graph solution when available.

<a id="phase-7-advanced-interview-patterns"></a>
### Phase 7 — Advanced Interview Patterns

**Learning objectives:** repair remaining common weaknesses and select useful breadth without making rare material a barrier to interviews.

- **Prerequisites:** reliable Tier 1 patterns and working common Tier 2 coverage. You may begin interview practice before completing this phase.
- **First:** consolidate weak interval/greedy/heap/graph/DP combinations from mixed attempts and mocks.
- **🟡 Tier 3 — Nice to Know:** [§19 trie](#19-tries), running median, rolling hash, basic KMP, MST, and selected interval/tree DP. Pick only topics with a clear gap or target benefit.
- **⚪ Tier 4 — Low Priority / Specialized:** [§20 range-query trees, advanced graph/DP and geometry](#20-specialized-advanced-topics), Manacher, and specialized algorithm optimizations. Basic KMP remains Tier 3; deep specialization is optional.
- **Recommended practice:** one canonical application and one recognition/variation exercise for a selected Tier 3 topic; compare the simpler alternative and memory trade-off.
- **Mastery gate:** recognize the selected structure, implement its chosen basic form, and explain when its extra complexity is justified. Recognition alone is sufficient for unselected Tier 4 material.

<a id="phase-8-interview-practice"></a>
### Phase 8 — Interview Practice

**Learning objectives:** integrate recognition, explanation, implementation, testing, and recovery under realistic time pressure.

- **Prerequisites:** short mixed practice has already begun in Phase 2; full mocks become useful after core easy and standard medium problems are workable. Increase emphasis as Tier 1 and common Tier 2 gates improve.
- **Core work:** mixed unseen prompts, the [interview framework](#22-interview-problem-solving-framework), verbal invariants, Java fluency, manual testing, and honest complexity.
- **Recommended practice:** alternate timed solo attempts, peer/mentor mocks, and untimed repair; include debugging, a changed constraint, or one follow-up optimization. Hide topic tags and keep the final minutes for tracing and complexity.
- **Specialization:** company-specific topics should follow credible target evidence; last-minute obscure-topic cramming is not a readiness requirement.
- **Mastery gate:** in several separated mixed sessions, choose and justify an approach, produce correct Java, test actual code, explain costs, and use a hint constructively. Use the [full readiness gate](#full-interview-readiness-gate), not a single successful mock.

### Ongoing weekly balance

A useful steady-state starting mix is **40% new/weak patterns, 30% delayed re-solves and mistake-log review, 20% mixed timed practice, and 10% explanation/API/complexity drills**. Early on, mixed practice is short and limited to learned patterns; near interviews, shift more time toward mocks. Repeated errors call for concept or Java-rule repair before adding volume. Passive watching does not count as an independent retrieval attempt.

---

## 28. Mastery Checklists

Use observable evidence. Mark an item only after a delayed, unaided attempt—not immediately after reading a solution.

### Mastery levels

| Level | Evidence |
|---|---|
| Exposed | I recognize the term after being told the topic. |
| Working | I solve a canonical problem with light hints and can trace the algorithm. |
| Interview-ready | I recognize an untagged variation, justify the choice, implement it, test it, and explain complexity unaided. |
| Robust | I handle follow-ups, compare alternatives, and recover from bugs under time pressure. |

Tier 1 aims for interview-ready or robust. Tier 2 aims for interview-ready on standard forms. Tier 3 usually aims for working. Tier 4 usually requires only awareness.

### Java interview fluency — 🔴 Tier 1 — Must Master

- [ ] I use arrays, String, `ArrayList`, `HashMap`, `HashSet`, `ArrayDeque`, `PriorityQueue`, sorting, and safe comparators with minimal tooling.
- [ ] I explain `array.length`, `text.length()`, and `list.size()`; I choose primitive arrays versus boxed collections deliberately.
- [ ] I distinguish logical equality from object identity and keep hash-key equality fields stable.
- [ ] I explain pass-by-value, caller-visible mutation, parameter reassignment, aliasing, and shallow snapshots.
- [ ] I handle null/empty contracts, missing map values, overflow before casting, default min-heap order, and collection mutability limits.
- [ ] I account for String copying, builder output, list shifting, sorting buffers, amortized growth, and recursion depth.
- [ ] I state an alphabet/Unicode assumption when indexing characters and know when recursion should become iterative.

### Foundations — 🔴 Tier 1 — Must Master

- [ ] I derive time from sequential, nested, shrinking, and amortized loops.
- [ ] I include recursion depth and retained data in space analysis.
- [ ] I distinguish worst-case, average/expected, amortized, auxiliary, and output costs.
- [ ] I define a recursive function's contract and base case before implementation.
- [ ] I estimate feasible complexity from constraints without treating heuristics as laws.

### Arrays, strings, and matrices — 🔴 Tier 1 — Must Master

- [ ] I traverse without boundary errors and choose mutation versus a new output deliberately.
- [ ] I use prefix sums for static ranges and prefix-state maps for exact subarray relationships.
- [ ] I explain when negative values invalidate a sum-based sliding window.
- [ ] I map a grid to neighbors and handle empty/non-square grids.
- [ ] I recognize when a difference array is useful, even if it is lower priority.

### Hashing — 🔴 Tier 1 — Must Master

- [ ] I choose set, key→index, key→count, or key→group based on required information.
- [ ] I write complement and frequency scans in one pass when appropriate.
- [ ] I handle duplicates and missing keys, preserve the first/latest occurrence when required, and do not assume `HashMap` iteration order.
- [ ] I state expected `O(1)` operations and `O(n)` extra space honestly.
- [ ] I compare hashing with sorting when order, memory, or worst-case guarantees matter.

### Two pointers and sliding windows — 🔴 Tier 1 — Must Master

- [ ] I identify the ordering or repair property that allows a boundary to move safely.
- [ ] I implement opposite-direction and read/write pointers.
- [ ] I maintain fixed and variable windows incrementally.
- [ ] I state the window invariant and know exactly when to update the answer.
- [ ] I reject a window when its condition is not monotone under pointer movement.

### Linked lists — 🟠 Tier 2 — Very Important

- [ ] I reverse, merge, split, and reconnect lists without losing nodes.
- [ ] I use a dummy node to simplify head changes.
- [ ] I derive midpoint/cycle behavior with fast and slow pointers.
- [ ] I distinguish node identity from node value.
- [ ] I test empty, one-node, two-node, and cyclic inputs.

### Stacks, queues, and deques

**Priorities:** basic LIFO/FIFO use is **🔴 Tier 1**; expression/monotonic-stack patterns are **🟠 Tier 2**; monotonic deque is **🟡 Tier 3**.

- [ ] I select LIFO for nesting/unresolved work and FIFO for layers/arrival order.
- [ ] I implement parentheses parsing and graph/tree BFS with safe empty checks.
- [ ] I derive a monotonic-stack comparison and strictness for a new variation.
- [ ] I explain why each index is pushed/popped at most once.
- [ ] I recognize monotonic deque as a later optimization for window extrema.

### Binary search and sorting — 🔴 Tier 1 — Must Master

- [ ] I implement exact search and lower bound with consistent intervals.
- [ ] I describe the returned boundary after termination.
- [ ] I prove a feasibility predicate is monotone before searching an answer.
- [ ] I include predicate cost in total complexity.
- [ ] I use comparator sorting, understand stability, and know when sorting enables a simpler scan.
- [ ] I explain merge sort/quicksort trade-offs and distinguish primitive-array sorting from stable object/list sorting and its buffer costs.

### Trees — 🔴 Tier 1 — Must Master

- [ ] I write recursive DFS and iterative DFS/BFS from scratch.
- [ ] I choose preorder, inorder, postorder, or level order based on when state is needed.
- [ ] I define the information each subtree returns for height, balance, diameter, or path problems.
- [ ] I use full ancestor bounds for BST validation.
- [ ] I solve standard LCA and explain assumptions about node presence/identity.
- [ ] I understand construction and serialization even if they are not my first practice priority.

### Heaps / priority queues — 🟠 Tier 2 — Very Important

- [ ] I choose min- or max-orientation from the item that must be removed next.
- [ ] I solve Top-K and k-way merge patterns.
- [ ] I compare size-`k` heap processing, sorting, and static quickselect; I handle `k = 1` and `k > n` in both contract and complexity.
- [ ] I handle ties and know that heap storage is not globally sorted.
- [ ] I explain the two-heap running-median invariant at least conceptually.

### Graphs

**Priorities:** Traversal is **🔴 Tier 1 — Must Master**; common extensions are **🟠 Tier 2 — Very Important**.

- [ ] I build adjacency lists with correct direction and labels.
- [ ] I implement BFS/DFS, components, grid traversal, and unweighted shortest paths.
- [ ] I choose and correctly implement directed/undirected cycle detection.
- [ ] I implement Kahn topological sort and detect a cycle from processed count.
- [ ] I use DSU for undirected connectivity and Dijkstra for nonnegative weights.
- [ ] I distinguish shortest path, MST, reachability, and topological ordering.

### Recursion and backtracking — 🟠 Tier 2 — Very Important

- [ ] I draw the choice tree and state the meaning of `path` and the index/used set.
- [ ] I generate subsets, permutations, and combinations without copied code.
- [ ] I handle duplicate candidates at the correct recursion level.
- [ ] I restore mutable state and snapshot completed answers.
- [ ] I justify pruning and include unavoidable output size in complexity.

### Greedy and intervals

**Priorities:** Greedy reasoning is **🟠 Tier 2 — Very Important**; standard interval patterns are **🔴 Tier 1 — Must Master**.

- [ ] I identify a candidate local rule and try to disprove it with small counterexamples.
- [ ] I give an exchange or stays-ahead correctness argument for accepted greedy choices.
- [ ] I merge, insert, and count overlapping intervals after defining endpoint semantics.
- [ ] I solve meeting-room/resource variants with a heap or events.
- [ ] I switch to DP when local choices require remembering alternatives.

### Dynamic programming — 🟠 Tier 2 — Very Important

- [ ] I start with choices and a recursive state rather than a table shape.
- [ ] I define state, transition, base cases, and final answer as sentences.
- [ ] I memoize every future-relevant dimension and count distinct states × work per state.
- [ ] I convert standard 1D/2D recurrences to a valid tabulation order.
- [ ] I distinguish 0/1 from unbounded knapsack update direction.
- [ ] I optimize space only after proving which earlier states remain necessary.
- [ ] I solve representative 1D, grid, knapsack, and subsequence problems; I recognize interval/tree DP as later variants.

### Tries and specialized topics

**Priorities:** Tries are **🟡 Tier 3 — Nice to Know**; specialized topics are generally **⚪ Tier 4 — Low Priority / Specialized**.

- [ ] I implement basic trie insert, exact search, and prefix search, including end-of-word markers.
- [ ] I compare a trie with a hash set/sorted list based on prefix workload and memory.
- [ ] I recognize rolling hash, Fenwick/segment trees, KMP, MST, bitmask DP, and advanced graph algorithms by use case.
- [ ] I only deepen a specialized topic when target evidence or repeated problems justify the time.

### Full interview-readiness gate

- [ ] On mixed, untagged problems, I form two plausible approaches and choose using constraints.
- [ ] I communicate the contract, brute force, invariant, and complexity before coding.
- [ ] I produce clean Java 17-compatible code with minimal tooling and can explain the relevant Java semantics.
- [ ] I test normal, minimal, duplicate, boundary, and no-solution cases.
- [ ] I can absorb a hint, revise the model, and continue without defensiveness.
- [ ] My mistake log shows fewer repeated categories, at least two delayed successful attempts for weak core patterns, and transfer to untagged variations.

---

## 29. DSA Interview Cheat Sheet

Use this for retrieval before practice. Detailed syntax belongs in the [Java reference](#java-for-dsa-interviews-essential-reference), pattern discriminators in [§21](#21-dsa-pattern-recognition), and canonical implementation links in [§24](#24-code-templates).

### Complexity growth

| Complexity | Typical example | Interpretation |
|---|---|---|
| `O(1)` | Array index; expected constant-cost hash lookup | Cost does not grow with element count under the stated assumptions |
| `O(log n)` | Binary search, balanced-tree update | Repeatedly discard a constant fraction |
| `O(n)` | One flat scan | Account for any hidden operation inside the scan |
| `O(n log n)` | Comparison sorting, repeated heap updates | Common bound when order is needed |
| `O(n^2)` | Enumerating all two-index choices, 2D DP | Check input scale and transition cost |
| `O(2^n)` | Binary-choice search | Small `n`; output copying can add an `n` factor |
| `O(n!)` | Permutation search | Very small `n`; include output cost |

**Derive:** sequential blocks add; nested dependent work is a sum; recursion uses the number of calls and each call's own work; adjacency-list traversal is `O(V + E)`; DP is states times work per state. Peak live state determines auxiliary space. Separate output from auxiliary storage and qualify expected, amortized, and worst-case claims.

### Common operation costs

Unless specified otherwise, primitive/key/comparator work is constant. String and custom-object hashing/comparison may scale with key length.

| Java structure / operation | Time | Qualification |
|---|---:|---|
| `int[]` index/update | `O(1)` | New/copy/fill of `n` entries is `O(n)` time; a new array uses `O(n)` space |
| `ArrayList.get/set` | `O(1)` | Indexed access does not imply cheap middle removal |
| `ArrayList.add(value)` | Amortized `O(1)` | One resize can take `O(n)`; indexed insertion/removal can shift `O(n)` entries |
| `HashMap`/`HashSet` lookup/update | Expected `O(1)`; growth amortized | Good hash dispersion and constant-cost keys assumed; no sorted order; iteration costs size plus backing capacity |
| `TreeMap`/`TreeSet` lookup/update/bounds | `O(log n)` | Maintains comparator order; comparator/equality choices affect key uniqueness |
| `ArrayDeque` end operations | Insertion amortized `O(1)`; peek/removal `O(1)` | Rejects null; arbitrary search/removal is `O(n)` |
| `PriorityQueue.peek` | `O(1)` | Minimum under its comparator; empty returns null |
| `PriorityQueue.offer/poll` | `O(log n)` (usual amortized insertion analysis) | `contains`/`remove(Object)` are `O(n)`; polling is ordered, iteration is not |
| `String.length/charAt` | `O(1)` | Indices count UTF-16 code units; strings are immutable |
| Proper `String.substring` of length `k` | `O(k)` time/space | Modern Java copies the selected contents; whole/empty slices may reuse existing values |
| `String.equals/compareTo` | Up to `O(min(n, m))` character work | Equality can stop early on a length mismatch; comparisons can stop at the first difference |
| `StringBuilder.append` | Amortized proportional to appended content | Building `L` characters is `O(L)` total; `toString()` copies the final contents |
| `Arrays.sort(int[])` | `O(n log n)` for the documented Java implementation | Mutates the primitive array; auxiliary memory is implementation-dependent, so do not infer `O(1)` from the API |
| Object-array `Arrays.sort`, `List.sort`, `Collections.sort` | `O(n log n)` worst-case comparisons | Stable; comparator cost matters; budget `O(n)` worst-case auxiliary buffer for the ordinary object/array-list paths |
| Custom linked-node link update | `O(1)` once required references are known | Finding a node/predecessor can cost `O(n)`; `LinkedList.get(i)` requires traversal |
| Trie insert/search | `O(L)` fixed alphabet; expected with hash children | Memory depends on created prefixes and child representation |
| DSU merge/find | `O(alpha(n))` amortized | Requires path compression/halving and union by size/rank; initialization is `O(n)` |

API details: [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html), [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html), [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html), [PriorityQueue](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html), [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), and [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html). These are implementation/API qualifications, not permission to ignore the algorithm's own copies or output.

### Choose a data structure

| Need | Consider | Ask before committing |
|---|---|---|
| Fast membership | `HashSet` | Is multiplicity, logical key equality, or sorted order needed? |
| Key → value/count/index/group | `HashMap` | Which occurrence/state must survive? |
| Dense indexed numeric state | Primitive array | Fixed size? Mutation allowed? Could `long` be needed? |
| Resizable indexed sequence | `ArrayList` | Are primitive boxing and shifting costs acceptable? |
| Text read/build/mutate | `String` / `StringBuilder` / `char[]` | What is the alphabet, and which operations copy? |
| LIFO / nested work | `Deque<T>` with `ArrayDeque` | What exactly does each entry represent? |
| FIFO / level order | `Queue<T>` or `Deque<T>` with `ArrayDeque` | When is the state marked discovered? |
| Both ends / window extrema | `ArrayDeque` | Which entries expire and which are dominated? |
| Repeated min/max | `PriorityQueue` | Which item belongs at the root? |
| Sorted keys and neighbors | `TreeMap`, `TreeSet`, or sorted array | Are updates frequent, or is one sort enough? |
| Prefix lookup | Trie | Does prefix workload justify node memory? |
| Undirected connectivity merges | DSU | Are direction or actual paths also required? |
| Hierarchy / arbitrary relations | Tree / graph adjacency list | What state flows between nodes? Are edges directed/weighted? |

### Clue → pattern

| Clue | First candidates |
|---|---|
| Complement, membership, frequency | Hash map/set |
| Sorted condition involving two or three values | Two pointers |
| Sorted boundary or monotone predicate | Binary search |
| Contiguous, incrementally maintainable validity | Sliding window |
| Contiguous exact sum with negatives | Prefix sum + map |
| Repeated static range query | Prefix sum; changing values may need a specialized structure |
| Next greater/smaller | Monotonic stack |
| Top/kth/repeated extreme | Heap, sorting, or quickselect |
| Overlapping schedules | Sort intervals; heap or event sweep for resource counts |
| Tree subtree property | DFS/postorder |
| Levels or unweighted shortest path | BFS |
| Components/connectivity | DFS/BFS; DSU for repeated undirected merges |
| Dependencies | Topological sort |
| Nonnegative weighted shortest path | Dijkstra |
| All configurations | Backtracking |
| Repeated decision state | DP |
| Locally safe dominant choice | Greedy with a correctness argument |
| Prefix dictionary | Trie |

### Key algorithm costs

| Algorithm | Time | Auxiliary space, excluding input/output |
|---|---:|---:|
| Two pointers / sliding window | Usually `O(n)`; hash-backed state expected | `O(1)` to `O(n)` state |
| Iterative binary search | `O(log n)` constant-cost indexed comparisons | `O(1)` |
| Binary search on answer | Feasibility cost times `O(log(domainSize + 1))` | Feasibility helper's state |
| Tree traversal | `O(n)` | `O(h)` DFS stack or `O(w)` BFS frontier |
| Graph BFS/DFS, adjacency list | `O(V + E)` | `O(V)` for the usual marked-on-discovery traversal |
| Kahn topological order | `O(V + E)` | `O(V)` excluding graph and returned order |
| Dijkstra, duplicate-entry heap | `O(V + E log(E + 1))`; commonly `O((V + E) log(V + 1))` on simple graphs | `O(V + E)`; heap may retain `O(E)` entries |
| Top-K with positive `k`, `m = min(k, n)` | `O(n log(m + 1))` | `O(m)` |
| K-way merge of `N` items | `O(N log(k + 1))` | `O(k)` frontier |
| Enumerate all subsets | `O(n * 2^n)` including snapshots | `O(n)` path/stack; `O(n * 2^n)` output is additional |
| DP | Distinct states times transition work | Stored states plus recursion when used |

### Boundary conventions worth stating

- Half-open `[left, right)` has length `right - left`; Java substring and many array range APIs use an exclusive end.
- Inclusive `[left, right]` has length `right - left + 1`.
- Binary-search insertion boundaries may equal `n`; do not index that sentinel.
- Intervals: clarify whether `[a, b]` overlaps `[b, c]`; scheduling often uses half-open `[start, end)`.
- Tree height: state nodes versus edges; graph labels: state zero/one-based and isolated-node handling.
- Object identity and logical equality are different contracts; choose deliberately.

### Interview workflow: U-B-A-P-E-C-T

1. **Understand:** contract → examples → constraints; state null/empty/mutation assumptions.
2. **Brute force:** give a correct baseline and identify its bottleneck.
3. **Analyze:** derive time/space from actual operations and compare with constraints.
4. **Pattern:** clues → candidates → invariant/state; reject invalid assumptions.
5. **Explain:** algorithm, correctness idea, update order, and complexity.
6. **Code:** clear Java, safe arithmetic, consistent boundaries, and progress.
7. **Test:** trace actual code on normal and adversarial cases; then discuss a meaningful follow-up optimization.

### Pre-code questions

- What exactly does my helper/window/table entry represent?
- Why is it safe to discard this element/state/branch?
- Does every loop or recursion make progress?
- Could duplicates, negatives, direction, or weights invalidate the idea?
- What is the simplest counterexample to my greedy rule?
- Which Java operations dominate time, and what remains live simultaneously?

### High-frequency edge cases

- empty input and `null` root/head when allowed;
- one/two elements, duplicates, and all-equal values;
- separately allocated objects with equal logical values;
- negative/zero/large values and overflow before assignment to `long`;
- target absent, `k = 0/1/n`, `k > n`, or multiple answers;
- sorted/reverse-sorted data, skewed trees, and deep recursion;
- disconnected graph, self-loop, parallel edges, and cycles;
- empty/non-square/ragged grids according to the contract;
- touching/nested/zero-length intervals;
- shared array rows or result lists accidentally aliased.

### Final reminders

- Correctness before cleverness; state the invariant before typing.
- Expected hash speed does not imply sorted order or free key hashing.
- Java is pass-by-value; object references can still point to shared mutable state.
- `String` is immutable, `PriorityQueue` is a min-heap by default, and `ArrayDeque` rejects null.
- BFS requires equal/unweighted edges; Dijkstra requires nonnegative weights.
- A heap exposes one extreme; its iteration order is not sorted.
- Greedy needs a safe-choice argument; DP needs a complete state.
- Count recursion, copies, sorting buffers, and output accurately.
- Re-solve after a delay and test transfer; acceptance is not mastery.

---

> **North star:** Given an unseen problem, use its contract and constraints to form candidate patterns, choose a structure whose operations support the invariant, derive complexity, implement clearly, and test the actual code.
