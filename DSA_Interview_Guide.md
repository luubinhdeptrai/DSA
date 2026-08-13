# DSA Interview Guide

> An interview-first roadmap and reference for learning how to turn **problem clues → constraints → patterns → data structures → algorithms → correct code**.

All implementations and language-specific examples use **C++17 as the sole implementation language**. Blocks labeled `text` are intentionally language-independent pseudocode. Learn the invariants and decisions; a template is a starting shape, not a solution to memorize.

---

## Table of Contents

1. [How to Use This Guide](#1-how-to-use-this-guide)
   - [C++ for DSA Interviews — Essential STL Reference](#c-for-dsa-interviews-essential-stl-reference)
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

- **C++17:** [essential STL reference](#c-for-dsa-interviews-essential-stl-reference)
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

### C++ for DSA Interviews — Essential STL Reference

Use the Standard Library to express the algorithm clearly. Interview preparation should make these operations automatic, but syntax must remain subordinate to the invariant.

**Snippet convention:** Examples omit repetitive headers and assume **C++17**, the needed standard headers, and `using namespace std;`. `<bits/stdc++.h>` is convenient on many competitive-programming and interview judges, but it is non-standard and less portable; use normal headers when portability or production-style code matters.

#### Sequences: `vector` and `string`

```cpp
vector<int> nums{4, 1, 7};
nums.push_back(9);                    // amortized O(1)
int first = nums.front();             // require !nums.empty()
nums.pop_back();                      // require !nums.empty()

string text = "algorithm";
text[0] = 'A';                        // std::string is mutable
text += "s";                         // append; reserve first if size is predictable
string piece = text.substr(2, 4);     // copies four characters: O(4)
```

- `vector<T>` provides `O(1)` indexing, amortized `O(1)` `push_back`, and `O(n)` middle insertion/erasure because later elements shift.
- `string` is a mutable character sequence in C++17. Repeated `+=` is normally amortized linear in total output; `reserve` can avoid reallocations when the final size is predictable.
- C++ has no built-in vector-slice syntax. Iterator-range construction and `string::substr` create copies, so include their cost.
- `size()` returns unsigned `size_t`. Guard emptiness and convert deliberately: `int right = static_cast<int>(nums.size()) - 1;`. Do not evaluate `nums.size() - 1` first on an empty vector.

#### Hash tables and ordered maps/sets

```cpp
unordered_map<string, int> frequency;
++frequency["cat"];                    // operator[] inserts a missing key with value 0
if (auto it = frequency.find("dog"); it != frequency.end()) {
    int count = it->second;
}

unordered_set<int> seen;
bool inserted = seen.insert(42).second;
bool present = seen.find(42) != seen.end();

map<int, string> ordered;              // O(log n), keys stay sorted
set<int> unique_sorted;
```

- `unordered_map`/`unordered_set`: expected `O(1)` lookup, insertion, and erasure; no sorted-order guarantee.
- `map`/`set`: worst-case `O(log n)` operations plus sorted iteration and bound queries.
- `operator[]` on a map inserts a default value. Use `find`, `contains` only in C++20 (not C++17), or `at` when insertion is not intended.
- Keys are effectively immutable while stored. C++17 provides standard hashes for common scalar/string types, but not a general hash for `pair` or `tuple`; use `map<pair<...>, ...>`, encode a safe key, or define a custom hasher.

#### Stack, queue, deque, and priority queue

```cpp
stack<int> st;
st.push(3); int newest = st.top(); st.pop();

queue<int> q;
q.push(3); int oldest = q.front(); q.pop();

deque<int> dq;
dq.push_front(1); dq.push_back(2);
dq.pop_front(); dq.pop_back();

priority_queue<int> max_heap;
priority_queue<int, vector<int>, greater<int>> min_heap;
max_heap.push(5); int largest = max_heap.top(); max_heap.pop();
```

All listed end operations are `O(1)` except priority-queue `push`/`pop`, which are `O(log n)`; heap `top` is `O(1)`. Check `empty()` before `top`, `front`, `back`, or `pop`—calling them on an empty container is invalid.

#### Sorting, bounds, and comparators

```cpp
sort(nums.begin(), nums.end());
stable_sort(nums.begin(), nums.end());

auto first_ge = lower_bound(nums.begin(), nums.end(), 7); // first value >= 7
auto first_gt = upper_bound(nums.begin(), nums.end(), 7); // first value > 7
int index = static_cast<int>(first_ge - nums.begin());

struct Interval { int start; int end; };
vector<Interval> intervals;
sort(intervals.begin(), intervals.end(), [](const Interval& a, const Interval& b) {
    if (a.start != b.start) return a.start < b.start;
    return a.end > b.end;              // tie: descending end
});
```

- `sort` is `O(n log n)` worst-case in C++17 and is not stable; `stable_sort` preserves equal-key order and may use extra memory.
- `lower_bound`/`upper_bound` require a range sorted under a compatible ordering and take `O(log n)` comparisons on random-access iterators.
- A comparator must define a **strict weak ordering**. Return `false` for equal elements; use `<`, not `<=`, and never compare integers by overflow-prone subtraction.
- Lambdas use `[&]` to capture surrounding values by reference and `[=]` by value. Prefer explicit captures when lifetime or mutation could be unclear.

#### `pair`, `tuple`, and structured bindings

```cpp
pair<int, int> edge{2, 5};
auto [u, v] = edge;                    // copies the fields
auto& [stored_u, stored_v] = edge;     // aliases the fields

tuple<int, long long, string> state{3, 12LL, "open"};
auto [index_value, cost, label] = state;
```

Pairs and tuples make compact records and lexicographic ordered-map keys. Prefer a named `struct` when fields have domain meaning or a comparator becomes hard to read.

#### References, copies, numeric safety, and pointers

```cpp
long long sum_values(const vector<int>& nums) { // read-only, no vector copy
    long long total = 0;
    for (int value : nums) total += value;
    return total;
}

void reverse_in_place(vector<int>& nums) {      // caller-visible mutation
    reverse(nums.begin(), nums.end());
}

long long infinity = numeric_limits<long long>::max();
long long product = 1LL * 1'000'000 * 1'000'000;
struct ListNode;                        // forward declaration for the pointer example
ListNode* head = nullptr;
```

- Pass large read-only inputs as `const T&`, mutable inputs as `T&`, and by value only when a deliberate copy/ownership transfer is useful. A copied `vector` or `string` costs linear time and space.
- Use `long long` for sums, distances, products, and answer-search bounds that may exceed `int`; promote before arithmetic with `1LL * value`.
- `numeric_limits<T>::max()`/`lowest()` give type-correct sentinels. Avoid adding to a maximum sentinel, which can overflow.
- Use `nullptr`, not `0` or `NULL`, for pointer absence. Interview list/tree node pointers are normally borrowed; delete nodes only when ownership is explicitly part of the task.
- Integer division truncates toward zero. For positive `a, b`, overflow-safe ceiling division is `a / b + (a % b != 0)`.

#### Recursion, invalidation, and high-frequency C++ traps

- C++ specifies no portable recursion-depth limit, but the native call stack is finite. A skewed tree or graph with depth `O(n)` can overflow it; mention an iterative stack when depth is uncontrolled.
- A `vector` reallocation invalidates all iterators, pointers, and references to its elements. Insertion/erasure without reallocation still invalidates positions at or after the change.
- Rehashing an `unordered_map`/`unordered_set` invalidates iterators. Do not insert unpredictably while iterating; call `reserve` when appropriate or separate traversal from mutation.
- When erasing during iterator traversal, use the returned iterator when supported: `it = values.erase(it);`.
- `for (const auto& item : container)` avoids copies; use `auto&` only when mutation is intended.
- `std::ctype` functions such as `tolower` should receive `static_cast<unsigned char>(ch)` to avoid undefined behavior for negative signed `char` values.
- Other common traps: accidental map insertion through `operator[]`, signed/unsigned comparisons, dangling references after container growth, reading heap storage as sorted, using a non-strict comparator, and forgetting that `sort` mutates its range.

### Priority legend

#### 🔴 Tier 1 — Must Master

Extremely common and foundational. Recognize it quickly, explain it, implement it unaided, and solve standard variations.

#### 🟠 Tier 2 — Very Important

Frequently tested. Understand it well and solve standard problems confidently, but do not let its hardest variants displace Tier 1 practice.

#### 🟡 Tier 3 — Nice to Know

Useful but less frequent. Understand the concept and solve basic versions after the core is solid.

#### ⚪ Tier 4 — Low Priority / Specialized

Rare in general SWE interviews. Study deeply only for algorithm-heavy roles, competitive programming, or evidence that a target company expects it.

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

For Tier 1, be able to derive and adapt; for Tier 2, handle common forms and explain trade-offs; for Tier 3, know when it applies and implement a basic version; for Tier 4, recognition is normally sufficient. “I watched a video” and “I once accepted a solution” are exposure, not mastery.

### A note on representative problems

Problem names in this guide refer to well-known archetypes. Use LeetCode, NeetCode, HackerRank, CodeSignal, a textbook, or any equivalent source. The learning target is the transferable pattern stated beside the problem—not the platform or a memorized answer.

### Maintenance contract

When extending this guide, integrate new material into the relevant topic instead of appending a duplicate explanation. Preserve useful notes, keep priority labels consistent, update the Table of Contents when major sections change, and cross-reference an existing explanation when only a variation is new.

---

## 2. DSA Interview Priority Map

Priorities reflect typical general Software Engineering coding interviews. A company, level, or role can shift them; use company-specific evidence only after building the common core.

As a sanity check, current interview curricula from [HackerRank](https://www.hackerrank.com/interview/interview-preparation-kit) and [LeetCode](https://leetcode.com/discuss/post/2580423/new-data-structures-and-algorithms-conte-8ov1/) emphasize the same broad core: arrays/strings, hashing, sorting/search, stacks/queues, trees/graphs, heaps, greedy, backtracking, and DP. The tiers below apply this guide's own transfer-value and required-depth judgment; they do not copy a platform's percentages.

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
| Linked lists | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | Pointer manipulation is a classic correctness and communication test. |
| Fast/slow pointers and reversal | 🟠 Tier 2 — Very Important | Medium | Strong | High | These cover most high-value linked-list variations. |
| Stacks | 🟠 Tier 2 — Very Important | High | Strong | High | Essential for nested structure, DFS simulation, and unresolved-candidate problems, but narrower than the core array/hash patterns. |
| Monotonic stacks | 🟠 Tier 2 — Very Important | Medium/high | Strong | High | A recurring linear-time answer to next greater/smaller and span questions. |
| Queues and deques | 🟠 Tier 2 — Very Important | High | Strong | High | Queues are fundamental to BFS; deques support both ends efficiently. |
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
| **Array vs linked list** | You need indexing, cache-friendly scans, or simple storage. | You already have a node and need local `O(1)` link changes. | Arrays shift on middle insertion/deletion; lists require `O(n)` search and extra pointers. |
| **Hash map vs tree map** | Exact lookup speed matters and order does not. | Sorted traversal, lower bounds, predecessor/successor, or worst-case `O(log n)` matters. | Hashing is expected `O(1)` and unordered; balanced trees are ordered with `O(log n)` operations. |
| **Stack vs queue** | Work is nested, reversible, or last-in-first-out. | Work is layered, arrival-ordered, or first-in-first-out. | The removal order changes traversal and often correctness. |
| **BFS vs DFS** | You need minimum unweighted edges or explicit levels. | You need subtree/postorder state, reachability, or lower memory on very wide graphs. | Both traverse in `O(V+E)`; frontier/call-stack shape and path guarantees differ. |
| **Heap vs sorting** | Data changes/streams, only repeated extremes or small Top-K matter. | You need the complete order or one offline scan after ordering. | Heap Top-K can be `O(n log k)`; sorting is simpler and gives all order in `O(n log n)`. |
| **Greedy vs dynamic programming** | A local choice can be proven safe by exchange or stays-ahead reasoning. | Choices interact and repeated states must preserve alternatives. | Greedy stores little and is often faster; DP is broader but needs a correct state and more resources. |
| **Recursion vs iteration** | The structure is naturally recursive and depth is safe. | Stack limits, explicit traversal control, or low call overhead matters. | Both may use `O(depth)` state; recursion hides it in the call stack. |
| **Memoization vs tabulation** | Only reachable states should be evaluated and recurrence clarity matters. | Evaluation order is known and call-stack overhead should be avoided. | Memoization follows demand; tabulation offers predictable iteration and easier space compression. |
| **Adjacency list vs adjacency matrix** | The graph is sparse or neighbor iteration is common. | The graph is dense or constant-time arbitrary edge tests dominate. | Lists use `O(V+E)` space; matrices use `O(V²)` space and scan all possible neighbors. |

### How to allocate study time

A reasonable default is roughly **60–70% Tier 1**, **25–35% Tier 2**, and **at most 5–10% Tier 3/4** until mock interviews reveal a specific weakness. Priority controls depth, not permission: you may encounter a rare topic, but mastering common reasoning produces a much higher return.

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

If an operation inside the loop is not constant, include it. Sorting inside an `n`-iteration loop costs at least `O(n · k log k)` when each sort handles `k` items.

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
| Balanced-tree DFS | `O(n)` | `O(h)`, typically `O(log n)`, worst `O(n)` |
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
- Counting a required output vector as auxiliary storage without stating the convention.
- Optimizing away useful memory prematurely. An `O(n)` map that reduces `O(n²)` time to `O(n)` is frequently the right interview trade-off.

### 3.2 Amortized Complexity — 🟠 Tier 2 — Very Important

#### Intuition and mechanics

An operation can occasionally be expensive while a long sequence remains cheap on average. A dynamic array usually appends in `O(1)`. When capacity is exhausted, it allocates a larger block and copies existing elements, an `O(n)` event. Because capacity normally grows geometrically, those copies happen rarely; `n` appends cost `O(n)` total, so each append is **amortized `O(1)`**.

This differs from:

- **Average-case analysis:** Assumes a distribution of inputs.
- **Amortized analysis:** Guarantees the average per operation over any sufficiently long operation sequence under the data structure's rules.

Typical interview examples include dynamic-array append and monotonic-stack algorithms: an element may trigger several pops in one iteration, but each element is pushed and popped at most once, so total work is `O(n)`.

**Mistake to avoid:** Calling an individual resize `O(1)`. The individual event is `O(n)`; the sequence gives amortized `O(1)` append.

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
| Backtracking requires natural choose/recurse/unchoose flow | Input depth may exhaust the finite native call stack and cause stack overflow |
| Recursive clarity outweighs stack overhead | Constant auxiliary space is important and achievable |

Both can express many of the same algorithms. Recursion uses an implicit call stack; iteration may use explicit state or an explicit stack. Do not rewrite elegant tree DFS iteratively merely to claim superiority, but do discuss deep-tree stack risks.

**Common failures:** Missing or overly broad base cases, no progress, mutating shared state without undoing it, returning from only one branch, recomputing the same state, and confusing recursion depth with total number of calls.

### 3.4 Mathematics Useful in Interviews — 🟠 Tier 2 — Very Important

Focus on practical tools:

| Tool | Why it matters | Typical use | Cost |
|---|---|---|---:|
| Arithmetic-series sum `1+…+n = n(n+1)/2` | Explains triangular nested loops | Pair counts, missing number | `O(1)` formula |
| Logarithms | Count repeated halving/doubling | Binary search, balanced trees | Usually `O(log n)` steps |
| Remainder/modulo | Wrap indices or track residue classes | Circular arrays, divisible subarrays | `O(1)` per operation |
| `gcd` via Euclid | Reduce ratios; cycle/step reasoning | Fraction normalization | `O(log min(a,b))` |
| Integer division and ceiling division | Bound groups/pages | Search-on-answer feasibility | For positive values: `a / b + (a % b != 0)` |
| Overflow awareness | Prevent silent wrong answers | Midpoints, sums, products | Use wider type or rearrange safely |

In C++17, integer division truncates toward zero and a negative dividend can produce a negative remainder. For positive `m`, normalize a mathematical modulo when needed with `((x % m) + m) % m`.

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

## 4. Arrays & Strings

**Priority:** 🔴 Tier 1 — Must Master

### Topic Overview

- **What it is:** In C++17, `vector<T>` is the default resizable indexed sequence and `string` is a mutable byte/character sequence.
- **Why it exists:** Indexed sequential storage gives fast access and efficient traversal, making it the default representation for many problems.
- **Why it matters in interviews:** Arrays and strings are the most common input forms and the surface on which hashing, two pointers, windows, binary search, sorting, greedy reasoning, and DP are practiced.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Loops, indexing, complexity analysis, and C++ value/reference semantics.
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
| Matrix/grid traversal | 🔴 Tier 1 — Must Master | Boundaries, direction vectors, visited/state marking |
| Multi-dimensional prefix sums | 🟡 Tier 3 — Nice to Know | Basic rectangle-sum idea |
| Difference arrays | 🟡 Tier 3 — Nice to Know | Many offline range updates |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Advanced string matching | 🟡 Tier 3 — Nice to Know | KMP/Rabin–Karp awareness; addressed in advanced topics |
| High-dimensional prefix structures | ⚪ Tier 4 — Low Priority / Specialized | Rare in general interviews |

### 4.1 Traversal and Running Invariants — 🔴 Tier 1 — Must Master

#### Intuition and use

A scan is not merely “loop through the array.” Define what is true before or after each index. Examples:

- `best` is the best answer among positions already processed.
- `running_sum` equals the sum through the current index.
- `write` is the next position where a kept value belongs.
- `seen` describes exactly the values in the processed prefix.

That sentence is the **loop invariant**. It guides initialization, updates, and the returned result.

```cpp
optional<int> maximum_value(const vector<int>& values) {
    if (values.empty()) return nullopt;

    int best = values[0];
    for (int i = 1; i < static_cast<int>(values.size()); ++i) {
        best = max(best, values[i]);
    }
    return best;
}
```

- **When to use:** Every element may affect a small running summary.
- **Recognition:** Asked for a count, extreme, total, trend, or transformation with no need to revisit arbitrary prior positions.
- **Time:** `O(n)`; reading all items is often a lower bound.
- **Space:** `O(1)` if only fixed state is maintained; output storage may be additional.
- **Empty-input semantics:** This version returns `nullopt`. If the problem guarantees a nonempty vector, returning `int` directly is simpler; otherwise agree on `optional`, a documented sentinel, or an exception before coding.
- **Edge cases:** Empty input, one element, all negative values, duplicates, and values at numeric limits.
- **Alternative:** Sorting can expose order but usually costs `O(n log n)` and may mutate input.

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

### 4.3 Prefix Sums — 🔴 Tier 1 — Must Master

#### Intuition

Precompute cumulative information so a range can be answered by subtracting two prefixes. With an exclusive prefix array:

```cpp
vector<long long> build_prefix(const vector<int>& values) {
    vector<long long> prefix(values.size() + 1, 0);
    for (int i = 0; i < static_cast<int>(values.size()); ++i) {
        prefix[i + 1] = prefix[i] + values[i];
    }
    return prefix;
}

long long inclusive_range_sum(const vector<long long>& prefix,
                              int left, int right) {
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

### 4.6 Matrix and Grid Problems — 🔴 Tier 1 — Must Master

A matrix is usually `rows × cols`; traversal is `O(rows · cols)`, not automatically `O(n²)`. Grid problems range from simple iteration to graph search; DFS/BFS details belong in the graph section, but safe representation starts here.

```cpp
vector<pair<int, int>> valid_neighbors(int row, int col,
                                       int rows, int cols) {
    constexpr array<pair<int, int>, 4> directions{{
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    }};

    vector<pair<int, int>> neighbors;
    for (auto [dr, dc] : directions) {
        int next_row = row + dr;
        int next_col = col + dc;
        if (0 <= next_row && next_row < rows &&
            0 <= next_col && next_col < cols) {
            neighbors.emplace_back(next_row, next_col);
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
| Grid neighbors/regions | Direction vectors + DFS/BFS | What is a node, neighbor, and visited state? |

### Representative Problems

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

- `string` is mutable in C++17. Prefer appending at the end, call `reserve` when the final size is predictable, and remember that repeated front insertion or middle erasure can be quadratic. Also clarify whether input is ASCII bytes or requires real Unicode processing; `string` does not decode Unicode code points for you.
- State whether the input may be mutated before using an in-place approach.
- Name index semantics: “`right` is inclusive” or “the window is `[left, right)`.”
- Do not use a sliding window for arbitrary negative values unless a monotonic property still holds; prefix sums plus hashing may be correct.
- For grid code, guard `grid.empty()` before reading `grid[0]`, then calculate `rows` and `cols` once.
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
| Canonical keys for grouping | 🟠 Tier 2 — Very Important | Sorted signatures and count tuples |
| Prefix state + hash map | 🟠 Tier 2 — Very Important | Count or longest-range variants |
| Hash-table internals | 🟡 Tier 3 — Nice to Know | Collisions, load factor, resizing at a conceptual level |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Implementing a robust hash table | ⚪ Tier 4 — Low Priority / Specialized | Only if explicitly requested |
| Custom rolling hashes | ⚪ Tier 4 — Low Priority / Specialized | Collision-sensitive string algorithms; covered later |

### 5.1 Hash Map and Hash Set Fundamentals — 🔴 Tier 1 — Must Master

#### Core intuition and mechanics

A hash function converts a key into an integer-like code, which selects a storage bucket. Different keys may collide, so the table must resolve collisions and still compare keys for equality. Resizing keeps the load factor manageable.

This yields typical interview costs:

| Operation | Hash map / set expected | Worst case | Notes |
|---|---:|---:|---|
| Lookup | `O(1)` | `O(n)` | Depends on hashing, collisions, and implementation |
| Insert/update | `O(1)` amortized expected | `O(n)` | A resize can be expensive occasionally |
| Delete | `O(1)` expected | `O(n)` | `unordered_map`/`unordered_set` provide no sorted-order guarantee |
| Iterate all entries | `O(k)` | `O(k)` | `k` stored keys |

Hash keys must have stable equality and a compatible hash. C++ associative containers expose stored keys as `const`; do not try to mutate them in place. In C++17, scalar and string keys have standard hashes, but `pair`, `tuple`, and fixed count arrays need a custom hasher for `unordered_map`/`unordered_set`, an encoded string key, or an ordered `map`/`set` alternative.

#### Set pattern: “Have I seen this?”

```cpp
bool contains_duplicate(const vector<int>& values) {
    unordered_set<int> seen;
    seen.reserve(values.size());

    for (int value : values) {
        if (!seen.insert(value).second) {
            return true;
        }
    }
    return false;
}
```

The crucial design choice is **when** to insert. Checking before insertion prevents the current item from matching itself. In other problems, pre-populating all values or removing the current value may be appropriate; state the invariant.

#### Map pattern: retain the information future positions need

```cpp
optional<pair<int, int>> find_pair_indices(const vector<int>& values,
                                           long long target) {
    unordered_map<long long, int> position;
    for (int i = 0; i < static_cast<int>(values.size()); ++i) {
        long long needed = target - values[i];
        auto it = position.find(needed);
        if (it != position.end()) {
            return pair<int, int>{it->second, i};
        }
        position[values[i]] = i;
    }
    return nullopt;
}
```

Here `position` contains eligible indices strictly before `i`. Storing an index rather than only membership is driven by the required output.

### 5.2 Frequency Tables and Lookup Techniques — 🔴 Tier 1 — Must Master

Choose what the map value means:

- `value → count`: frequency, multiset equality, or window validity.
- `value → first index`: longest distance/range; do not overwrite the earliest occurrence.
- `value → latest index`: most recent conflict or boundary.
- `key → vector<Item>`: grouping.
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

The initial mapping from prefix `0` to count `1` represents an empty prefix, allowing a valid subarray that begins at index `0`. In C++ this can be initialized as `unordered_map<long long, long long> count_by_prefix{{0, 1}};`. Update **after** querying so an empty current subarray is not accidentally counted when inappropriate.

#### Canonical grouping keys — 🟠 Tier 2 — Very Important

To group objects that are equivalent under a transformation, map each object to a canonical signature:

- Sort characters: `O(L log L)` per string of length `L`.
- Count a fixed alphabet and use a stable signature: encode the counts into a string, use `map<array<int, 26>, ...>`, or provide a custom hash for `array<int, 26>`. Building the signature costs `O(L + alphabet)`.
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

### Representative Problems

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
- Trying to use a compound key without a valid C++17 hash/equality definition, or deriving a key from data that later changes without rebuilding the key.
- Assuming hash operations are unconditional worst-case `O(1)`.
- Forgetting that a map can have up to `O(n)` distinct keys even when each value is small.
- Decrementing counts but leaving logic that treats zero-count keys as present.
- Creating a key that omits relevant state, especially in memoization.

In an interview, say what each key and value represent: “After processing index `i`, this `unordered_map` stores the count of every prefix sum through `i`.” That explanation is more valuable than saying only “I use a hash table.”

### Hashing Mastery Checklist

- [ ] I can choose between a set, a map, a fixed frequency array, and sorting.
- [ ] I state expected/amortized and worst-case qualifications accurately.
- [ ] I implement one-pass complement lookup without matching an element to itself.
- [ ] I design map values deliberately: count, first index, latest index, group, or cached result.
- [ ] I can derive prefix-state lookup for count and longest-length variants.
- [ ] I can create a stable canonical grouping key and choose an encoded key, ordered map, or custom C++ hasher deliberately.
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

### 6.1 Opposite-Direction Pointers — 🔴 Tier 1 — Must Master

#### Intuition and safe movement

For sorted pair sum, begin with the smallest and largest remaining values:

```text
left = 0
right = n - 1
while left < right do
    total = a[left] + a[right]
    if total equals target then
        report the pair
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

### Representative Problems

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
| Monotonic deque window extrema | 🟠 Tier 2 — Very Important | Remove expired and dominated indices; revisited in queues |
| Window + replacement budget | 🟠 Tier 2 — Very Important | Maintain a conservative maximum-frequency invariant |

### Optional / Specialized

| Subtopic | Priority | Target depth |
|---|---|---|
| Multiple nested windows / exact-count transforms | 🟡 Tier 3 — Nice to Know | Learn “exactly `k` = at most `k` − at most `k-1`” after core mastery |
| Non-monotonic window constraints | ⚪ Tier 4 — Low Priority / Specialized | Often need prefix sums, trees, or deques instead of a standard window |

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

### When Sliding Window Does Not Apply

- The range is not contiguous.
- Adding/removing an item cannot update state efficiently.
- Validity is non-monotonic under pointer movement.
- Arbitrary negative numbers break a simple sum threshold invariant.
- The problem asks about all subsequences rather than subarrays/substrings.
- The relevant relation is between prefix states; hashing prefix sums may be a better model.

Alternatives include prefix sums + hashing, binary search on answer, monotonic stacks/deques, DP, or sorting + two pointers.

### Representative Problems

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

- **What it is:** A linked list stores elements in nodes connected by pointers rather than at contiguous indices. Singly linked nodes point forward; doubly linked nodes point both forward and backward.
- **Why it exists:** Links permit local insertion/deletion without shifting later elements and let structures be assembled from independently allocated nodes.
- **Why it matters in interviews:** Linked-list tasks test pointer safety, mutation, invariants, and reasoning without random access. Reversal, merge, cycle, and dummy-node patterns are frequent classics.
- **Interview priority:** 🟠 Tier 2 — Very Important.
- **Prerequisites:** C++ pointers, `nullptr` handling, loops/recursion, and pointer identity vs value equality.
- **Common use cases:** Queues, adjacency chains, LRU-cache internals, ordered merging, and sequences with frequent node-level updates.
- **Common problem patterns:** Reverse links, splice nodes, merge lists, dummy head, fast/slow pointers, and cycle detection.
- **Recognition clues:** Input is a `ListNode`, random access is absent, nodes must be rearranged in place, or the task asks for cycle/middle/intersection.
- **Required depth:** Confidently implement singly linked reversal, merge, middle, and cycle detection; understand doubly linked splicing for designs such as LRU cache.

> **Why this priority?** Linked lists appear less often than arrays and hashing, but their core patterns are canonical interview material and expose pointer errors clearly. Advanced list tricks have much lower transfer value.

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
| Implementing list allocators/intrusive lists | ⚪ Tier 4 — Low Priority / Specialized | Role-specific systems knowledge |

### 8.1 Singly Linked Lists and Pointer Safety — 🟠 Tier 2 — Very Important

A node conceptually contains `value` and `next`. Access by position is `O(n)` because links must be followed; insertion or deletion is `O(1)` **only when the relevant node/predecessor is already known**.

| Operation | Singly linked list | Dynamic array | Important qualification |
|---|---:|---:|---|
| Access index `i` | `O(i)` | `O(1)` | No random access in a list |
| Search by value | `O(n)` | `O(n)` | Unless another index exists |
| Insert/delete after known node | `O(1)` | Usually `O(n)` shift | Finding the node may be `O(n)` |
| Append | `O(1)` with tail, else `O(n)` | Amortized `O(1)` | Keep tail consistent |

Before changing `current->next`, save any node that still needs to be reached. Draw a three-node picture or label `previous`, `current`, and `next_node`; pointer manipulation becomes far less error-prone when the preserved path is explicit.

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

### 8.2 Reversal — 🟠 Tier 2 — Very Important

#### Core intuition

At every step, the processed prefix is reversed and headed by `previous`; `current` begins the unreversed suffix.

```cpp
struct ListNode {
    int value;
    ListNode* next;
};

ListNode* reverse_list(ListNode* head) {
    ListNode* previous = nullptr;
    ListNode* current = head;

    while (current != nullptr) {
        ListNode* next_node = current->next; // preserve the suffix
        current->next = previous;            // reverse one edge
        previous = current;
        current = next_node;
    }
    return previous;
}
```

- **Time:** `O(n)`; each node is processed once.
- **Space:** `O(1)` iterative; `O(n)` call stack for a recursive version on a length-`n` list.
- **Recognition:** “Reverse,” reorder halves, palindrome list, or a larger problem needs temporary direction changes.
- **Common mistake:** Reassigning `current->next` before saving the original next pointer, losing the remaining list.
- **Edge cases:** Empty list, one node, two nodes, and whether the caller expects the input structure to be restored after a temporary reversal.

For recursive reversal, define the return value: the new head of the reversed suffix. After recursion returns, set `head->next->next = head`, then set `head->next = nullptr` to prevent a cycle. Iteration is usually safer when list length is large or uncontrolled.

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

If a cycle exists, fast eventually laps slow inside it; if `fast` reaches `nullptr`, no cycle exists.

- **Detection:** `O(n)` time and `O(1)` space.
- **Cycle entry:** After a meeting, place one pointer at the head, move both one step at a time, and their next meeting is the cycle entry.
- **Alternative:** Store node identities in a set for expected `O(n)` time and `O(n)` space; simpler but uses memory.

Compare **node identity**, not node values. Repeated values do not imply a cycle or intersection.

The technique also supports “kth from end”: place pointers `k` nodes apart, then move both until the lead reaches the end. Clarify whether `k` is one-based and what should happen when `k` exceeds the length.

### 8.4 Doubly Linked Lists — 🟡 Tier 3 — Nice to Know

A doubly linked node has `prev` and `next`. Given a node, it can be removed in `O(1)` by reconnecting both neighbors. With dummy head and tail sentinels, every real node has two neighbors, eliminating endpoint branches.

This is important for an **LRU cache** design:

- `unordered_map<Key, Node*>`: key → node pointer for expected `O(1)` lookup.
- Doubly linked list: recency order and `O(1)` detach/append.

Every splice must update four logical connections consistently. When moving a node, detach it fully before attaching it elsewhere. Keep map membership, list membership, capacity, and tail/head meaning synchronized.

- **Time:** Insert/remove known node `O(1)`; search without a map remains `O(n)`.
- **Space:** Two links per node plus any index.
- **Trade-off:** Easier bidirectional removal than singly lists, but more memory and more invariants to maintain.

### Representative Problems

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
- Dereferencing `fast->next` before confirming `fast != nullptr`.
- Confusing equal node values with identical node objects.
- Creating a cycle by failing to terminate a reversed or merged tail.
- Forgetting that finding a predecessor can make deletion `O(n)`.
- Mishandling head deletion instead of using a dummy.
- Assuming the second half begins at the same place for odd and even lengths without tracing examples.

In an interview, draw nodes and arrows, state what each pointer owns, and trace one rewiring operation. “`previous` heads the fully reversed prefix; `current` heads the untouched suffix” is a strong correctness explanation.

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
| Correct C++ container adaptor | 🔴 Tier 1 — Must Master | Use `stack`, `queue`, or `deque`; avoid linear vector front erasure |

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

### 9.1 Stack Fundamentals and Parentheses — 🔴 Tier 1 — Must Master

`std::stack` supports `push`, `pop`, and `top` in `O(1)` with its standard underlying containers (a `deque` by default). It is ideal when the newest unresolved item must be handled first.

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

Stacks also replace recursion explicitly. An iterative DFS stack gives control over order and avoids exhausting C++'s finite native call stack, but may require storing extra per-frame state for postorder processing.

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

**Complexity:** A well-designed single pass is typically `O(n)` time and `O(n)` stack space. Repeated front insertion, middle erasure, or rebuilding temporary strings can raise the cost in C++.

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

### 9.4 Queue Fundamentals and BFS Usage — 🔴 Tier 1 — Must Master

A queue processes items in discovery order. In C++17, use `queue<T>` or `deque<T>` for `O(1)` end operations; repeatedly calling `vector::erase(vector.begin())` shifts the remaining elements and costs `O(n)`.

#### BFS queue invariant

The queue contains discovered but not yet processed states, in nondecreasing distance from the source. Mark a state visited **when enqueuing**, not when dequeuing, so it is not inserted repeatedly by multiple parents.

- **Unweighted shortest path:** The first time a node is discovered, BFS has found a shortest number of edges from the source.
- **Level processing:** Capture the current queue length before processing that layer; nodes enqueued during the layer belong to the next one.
- **Time:** `O(V + E)` with adjacency lists, or `O(rows · cols)` for a grid with constant-degree neighbors.
- **Space:** `O(V)` worst-case for visited plus frontier.

DFS may find a path but not necessarily the shortest unweighted path. Weighted edges require a different algorithm unless all weights fit a special case such as 0–1 BFS.

#### Deque — 🟠 Tier 2 — Very Important

A deque offers `O(1)` insertion/removal at both ends. Use it for ordinary queues, palindrome-style processing when mutation is acceptable, 0–1 BFS awareness, and monotonic candidate queues.

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

### Stack vs Queue vs Deque

| Need | Choose | Why |
|---|---|---|
| Most recent unresolved context | Stack | LIFO mirrors nesting/undo |
| Earliest discovered work | Queue | FIFO preserves BFS distance order |
| Both ends | Deque | Flexible frontier/window maintenance |
| Next greater/smaller | Monotonic stack | Resolves prior candidates when a boundary arrives |
| Max/min in every window | Monotonic deque | Keeps only unexpired, undominated candidates |
| Highest numeric priority | Heap, not queue | Extraction follows priority rather than arrival time |

### Representative Problems

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
- Using `vector::erase(begin())` as a queue and paying `O(n)` per removal.
- Marking BFS nodes only when dequeued and inserting duplicates.
- Mixing level count with a queue size that grows during the level.
- Choosing the wrong monotonic direction or wrong strict/non-strict comparison for duplicates.
- Claiming a monotonic structure is linear without explaining push/pop accounting.

Before coding a monotonic stack/deque, narrate one pop: “This old index can never be the answer after the new index because…” If that statement is unclear, the invariant is not ready.

### Stacks, Queues, and Deques Mastery Checklist

- [ ] I choose LIFO, FIFO, double-ended, or priority order deliberately.
- [ ] I validate mixed parentheses and explain why the stack is necessary.
- [ ] I evaluate postfix expressions with correct operand order.
- [ ] I use `queue`/`deque` for `O(1)` FIFO operations instead of erasing the front of a `vector`.
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

In C++17, use `left + (right - left) / 2`; with nonnegative indices, integer division supplies the needed floor while avoiding the overflow risk of `(left + right) / 2`. More importantly, each update must remove `mid` or otherwise strictly shrink the range; `left = mid` in the wrong convention can loop forever.

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
- **Count in sorted array:** `upper_bound(target) - lower_bound(target)`.

The invariant for the half-open first-true search is:

- All indices before `left` are known false.
- All indices at or after `right` are known true, with conceptual sentinel boundaries permitted.
- The first true position remains in `[left, right]`.

This boundary search is safer than finding one occurrence and scanning outward, which can degrade to `O(n)` with many duplicates.

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

### 10.4 Rotated, Peak, and Matrix Variants — 🟠 Tier 2 — Very Important

#### Rotated sorted array

With distinct values, at least one half around `mid` is sorted. Determine the sorted half, test whether the target lies inside its value range, and discard the other half. Duplicates can make the sorted half ambiguous; shrinking equal endpoints may be necessary and can cause `O(n)` worst-case behavior.

#### Peak / slope search

Comparing `a[mid]` with `a[mid+1]` reveals whether a peak lies to the right or at/before `mid` under the problem's structural guarantee. Use a range where `mid+1` is always valid, such as `left < right` with `right = n-1`.

#### Matrix search

- If each row's last element is less than the next row's first, treat the matrix as a flat sorted array: in C++ with nonnegative indices, `row = mid / cols` and `col = mid % cols`.
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

### Representative Problems

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
| C++17 sorting behavior | 🔴 Tier 1 — Must Master | Know `sort`/`stable_sort`, iterator ranges, complexity, and stability |

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
| Library-sort internals (often introsort) | ⚪ Tier 4 — Low Priority / Specialized | Know the C++ standard's guarantees, not a particular library implementation |

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

If indices are required, sort records such as `pair<Value, int>{value, original_index}` or a named struct, or use hashing instead. If the input must not be mutated, copy it deliberately (`auto ordered = input;`) and include `O(n)` extra time/space for that copy.

#### Typical complexity

- Comparison sorting has a general `Ω(n log n)` lower bound in the comparison model.
- C++17 `std::sort` guarantees `O(n log n)` comparisons in the worst case and does not preserve equal-element order.
- A C++ comparator may be invoked `O(n log n)` times. If deriving a key is expensive, precompute/decorate records rather than recomputing an `O(L)` key on every comparison.
- A subsequent linear scan leaves total time `O(n log n + n) = O(n log n)`.

### 11.2 Comparator and Key-Based Sorting — 🔴 Tier 1 — Must Master

In C++17, sorting custom records normally uses a lambda comparator. Keep it short, use explicit tie-breaks, and rely on `pair`/`tuple` lexicographic order only when that order exactly matches the problem.

```cpp
struct Record {
    int start;
    int end;
};

void order_records(vector<Record>& records) {
    sort(records.begin(), records.end(),
         [](const Record& a, const Record& b) {
             if (a.start != b.start) return a.start < b.start;
             return a.end > b.end; // tie: descending end
         });
}
```

Tie-breaking is algorithmic, not cosmetic. For example, sorting intervals by ending time supports maximum non-overlapping selection; sorting by starting time supports merging.

A comparator must define a consistent ordering:

- Return `true` exactly when the first argument must precede the second.
- Define a strict weak ordering: equal elements compare false in both directions, and the ordering is transitive. Use `<`, never `<=`.
- Do not subtract integers in a C++ comparator: signed overflow is undefined behavior, and `<` expresses the order directly.
- Handle equal keys deliberately.

For “largest concatenated number,” compare `a+b` with `b+a`; numeric or ordinary lexicographic order alone is insufficient. After sorting, normalize an all-zero result.

### 11.3 Stability and In-Place Behavior — 🟡 Tier 3 — Nice to Know

A **stable** sort preserves the input relative order of records whose keys compare equal. Stability matters when:

- A previous ordering should survive ties in a later sort.
- Equal-key records have meaningful chronological/input order.
- Multi-pass sorting relies on earlier lower-priority keys.

It does not matter when all keys are unique or an explicit full tie-break key determines the desired order.

“In place” and “stable” are independent properties. Both `sort` and `stable_sort` mutate the iterator range; `sort` is not stable, while `stable_sort` may allocate a linear buffer and has weaker comparison complexity when sufficient extra memory is unavailable. State only guarantees relevant to the interview.

### 11.4 Which Sorting Algorithms to Understand

| Algorithm | Priority | Time | Extra space | Stable? | Interview depth |
|---|---|---:|---:|---|---|
| C++17 `std::sort` | 🔴 Tier 1 — Must Master | `O(n log n)` worst case | Implementation-dependent; commonly `O(log n)` stack | No | Use confidently; know iterator range, comparator, and mutation |
| Merge sort | 🟠 Tier 2 — Very Important | `O(n log n)` worst case | `O(n)` for arrays; list merge can use link rewiring | Yes in standard form | Explain split/merge; implement if asked |
| Quicksort | 🟠 Tier 2 — Very Important | Average `O(n log n)`, worst `O(n²)` | Average `O(log n)` stack, worst `O(n)` | Usually no | Explain pivot/partition and randomization; basic implementation |
| Insertion sort | 🟡 Tier 3 — Nice to Know | Worst `O(n²)`, best `O(n)` on already sorted with standard form | `O(1)` | Yes | Understand small/nearly sorted use |
| Heap sort | 🟡 Tier 3 — Nice to Know | `O(n log n)` worst case | `O(1)` auxiliary in array form | No | Conceptual; prioritize heap operations |
| Counting/bucket sort | 🟡 Tier 3 — Nice to Know | `O(n + R)` for range `R` | `O(R)` | Depends on form | Recognize bounded key range/frequencies |
| Selection sort | ⚪ Tier 4 — Low Priority / Specialized | `O(n²)` | `O(1)` | Usually no | Awareness only |
| Bubble sort | ⚪ Tier 4 — Low Priority / Specialized | `O(n²)` | `O(1)` | Yes in standard form | Awareness only |
| Radix sort | ⚪ Tier 4 — Low Priority / Specialized | Digit/radix-dependent | Radix-dependent | Can be | Conceptual only for normal SWE interviews |

#### Merge sort intuition

Recursively sort each half, then merge two sorted halves in linear time. There are `O(log n)` levels and `O(n)` merge work per level, giving `O(n log n)`. It offers predictable worst-case time and stability, but array merging uses extra memory.

#### Quicksort intuition

Choose a pivot, partition values around it, then recursively sort partitions. Balanced partitions give `O(n log n)`; consistently extreme pivots give `O(n²)`. Randomization or robust pivot selection makes pathological imbalance less likely, but does not turn the theoretical worst case into `O(n log n)` for ordinary quicksort.

Do not memorize partition code without defining regions and the pivot's final meaning. Different Lomuto/Hoare schemes have different return values and recursive boundaries.

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

### Representative Problems

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

## 12. Trees

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Tree questions combine recursion, traversal, state design, and pointer reasoning in a form that appears constantly in general Software Engineering interviews. A candidate should be able to traverse a binary tree, choose DFS or BFS, state an invariant, and analyze `O(n)` time without prompting. Specialized balanced-tree internals are far less important.

### Topic Overview

- **What it is:** A tree is a connected, acyclic graph. In a rooted tree, every node except the root has one parent. A binary tree gives each node at most two children.
- **Why it exists:** Trees represent hierarchy and support divide-and-conquer reasoning: solve the same smaller problem for each child, then combine the results.
- **Why it matters in interviews:** Trees test recursion, queues, stacks, careful base cases, and the ability to define what a function returns. Many seemingly different questions are one of a few traversal patterns.
- **Interview priority:** 🔴 Tier 1 — Must Master.
- **Prerequisites:** Big-O, recursion and call stacks, stack/queue operations, references or pointers, and basic hashing.
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
- Morris traversal, threaded traversal, and parent-pointer variations after ordinary traversals are automatic.

### Optional / Specialized

- **AVL/red-black tree rotations — ⚪ Tier 4 — Low Priority / Specialized.** Standard libraries provide balanced ordered maps/sets; implementing rotations is rare in general interviews.
- **B-trees/B+ trees — ⚪ Tier 4 — Low Priority / Specialized.** Important for databases and storage-system interviews, but not normal coding rounds.
- **Morris traversal — 🟡 Tier 3 — Nice to Know.** It achieves `O(1)` auxiliary space by temporarily threading the tree, but mutates links and is rarely the clearest interview solution.

### 12.1 Binary-Tree Fundamentals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Nearly every tree problem depends on precise vocabulary and a correct null-safe node representation.

```cpp
struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;

    explicit TreeNode(int value, TreeNode* left_child = nullptr,
                      TreeNode* right_child = nullptr)
        : val(value), left(left_child), right(right_child) {}
};
```

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

```cpp
void preorderDfs(const TreeNode* node, std::vector<int>& result) {
    if (node == nullptr) return;
    result.push_back(node->val);
    preorderDfs(node->left, result);
    preorderDfs(node->right, result);
}

std::vector<int> preorder(const TreeNode* root) {
    std::vector<int> result;
    preorderDfs(root, result);
    return result;
}

void inorderDfs(const TreeNode* node, std::vector<int>& result) {
    if (node == nullptr) return;
    inorderDfs(node->left, result);
    result.push_back(node->val);
    inorderDfs(node->right, result);
}

std::vector<int> inorder(const TreeNode* root) {
    std::vector<int> result;
    inorderDfs(root, result);
    return result;
}

void postorderDfs(const TreeNode* node, std::vector<int>& result) {
    if (node == nullptr) return;
    postorderDfs(node->left, result);
    postorderDfs(node->right, result);
    result.push_back(node->val);
}

std::vector<int> postorder(const TreeNode* root) {
    std::vector<int> result;
    postorderDfs(root, result);
    return result;
}
```

**When to use / recognition clues:** Use DFS when the answer depends on complete subtrees, root-to-leaf paths, ancestors, or aggregating child results. Words such as *subtree*, *path*, *descendant*, *height*, *balanced*, or *ancestor* are strong clues.

**Complexity:** All three visit `n` nodes: `O(n)` time and `O(h)` auxiliary stack space, excluding output.

#### The most useful recursive contract

Before coding, finish this sentence: **`dfs(node)` returns ...** For example, in a balance check it returns the subtree height, or a sentinel saying the subtree is already unbalanced.

```cpp
bool isBalanced(const TreeNode* root) {
    std::function<int(const TreeNode*)> heightOrFail =
        [&](const TreeNode* node) -> int {
        if (node == nullptr) return 0;

        const int left = heightOrFail(node->left);
        if (left == -1) return -1;
        const int right = heightOrFail(node->right);
        if (right == -1 || std::abs(left - right) > 1) return -1;

        return 1 + std::max(left, right);
    };

    return heightOrFail(root) != -1;
}
```

This single postorder traversal is `O(n)`. Recomputing height separately at every node can degrade to `O(n^2)` on a skewed tree.

**Common mistakes:** Writing recursion before defining its return meaning; forgetting the `nullptr` case; mixing node-count and edge-count height; using shared global state unnecessarily; recomputing a subtree; and forgetting that a skewed tree makes recursion depth `O(n)`.

**Edge cases:** Empty tree, one node, one-sided tree, duplicate values, negative values, and a path where the optimal answer does not include the root.

**Alternatives and trade-offs:** Recursive DFS is compact and mirrors the structure. Iterative DFS avoids exhausting the finite native call stack but makes postorder and carried state more explicit. Both have the same asymptotic worst-case auxiliary space.

### 12.3 Iterative DFS Traversals

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Interviewers often request an iterative variant or use input deep enough to make recursion unsafe. Preorder and inorder should be comfortable; iterative postorder is useful but less frequently required.

```cpp
std::vector<int> preorderIterative(const TreeNode* root) {
    if (root == nullptr) return {};
    std::vector<int> result;
    std::stack<const TreeNode*> pending;
    pending.push(root);

    while (!pending.empty()) {
        const TreeNode* node = pending.top();
        pending.pop();
        result.push_back(node->val);
        // Push right first so left is processed first.
        if (node->right != nullptr) pending.push(node->right);
        if (node->left != nullptr) pending.push(node->left);
    }
    return result;
}

std::vector<int> inorderIterative(const TreeNode* root) {
    std::vector<int> result;
    std::stack<const TreeNode*> pending;
    const TreeNode* current = root;

    while (current != nullptr || !pending.empty()) {
        while (current != nullptr) {
            pending.push(current);
            current = current->left;
        }
        current = pending.top();
        pending.pop();
        result.push_back(current->val);
        current = current->right;
    }
    return result;
}

std::vector<int> postorderIterative(const TreeNode* root) {
    if (root == nullptr) return {};
    std::vector<int> result;
    std::stack<std::pair<const TreeNode*, bool>> pending;
    pending.push({root, false});

    while (!pending.empty()) {
        auto [node, expanded] = pending.top();
        pending.pop();
        if (node == nullptr) continue;
        if (expanded) {
            result.push_back(node->val);
        } else {
            pending.push({node, true});
            pending.push({node->right, false});
            pending.push({node->left, false});
        }
    }
    return result;
}
```

**How it works:** The stack stores unfinished work. A `(node, expanded)` marker preserves the return-to-parent moment that recursion normally supplies.

**Complexity:** `O(n)` time and `O(h)` stack for inorder on a typical tree; the explicit preorder/postorder stack can be `O(n)` in the worst shape.

**Mistakes:** Pushing preorder children in the wrong order, losing the current pointer in inorder, processing postorder before its children, and forgetting the `root == nullptr` case.

### 12.4 Tree BFS / Level-Order Traversal

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** BFS is the direct solution for levels, nearest depth, right/left views, and minimum edge distance in an unweighted tree.

```cpp
std::vector<std::vector<int>> levelOrder(const TreeNode* root) {
    if (root == nullptr) return {};

    std::vector<std::vector<int>> levels;
    std::queue<const TreeNode*> pending;
    pending.push(root);

    while (!pending.empty()) {
        const int level_size = static_cast<int>(pending.size());
        std::vector<int> level;
        level.reserve(level_size);
        for (int i = 0; i < level_size; ++i) {
            const TreeNode* node = pending.front();
            pending.pop();
            level.push_back(node->val);
            if (node->left != nullptr) pending.push(node->left);
            if (node->right != nullptr) pending.push(node->right);
        }
        levels.push_back(std::move(level));
    }
    return levels;
}
```

**Recognition:** The problem says *level*, *row*, *nearest*, *minimum depth*, *view from a side*, or asks for nodes in increasing distance from the root.

**Complexity:** `O(n)` time; `O(w)` queue space where `w` is maximum tree width.

**Trade-off:** BFS exposes levels naturally but can hold a very wide level. DFS is often smaller on a wide balanced tree and is better when the result is a subtree summary.

**Common mistakes and edge cases:** Using a `vector` and erasing its first element instead of `std::queue`, not snapshotting the queue length before a level, enqueuing null pointers unnecessarily, or returning one empty level for an empty tree.

### 12.5 Height, Depth, Balance, Diameter, and Paths

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** These are canonical examples of subtree-summary recursion and are among the highest-transfer tree patterns.

```cpp
int maxDepth(const TreeNode* root) {
    if (root == nullptr) return 0;
    return 1 + std::max(maxDepth(root->left), maxDepth(root->right));
}
```

For diameter, each child returns a height. At a node, the best path *through* the node is `left_height + right_height`; the recursion returns only one extendable branch upward.

```cpp
int diameterOfBinaryTree(const TreeNode* root) {
    int best = 0;
    std::function<int(const TreeNode*)> height =
        [&](const TreeNode* node) -> int {
        if (node == nullptr) return 0;
        const int left = height(node->left);
        const int right = height(node->right);
        best = std::max(best, left + right);
        return 1 + std::max(left, right);
    };

    height(root);
    return best;  // Number of edges.
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

```cpp
bool isValidBst(const TreeNode* root) {
    std::function<bool(const TreeNode*, long long, long long)> valid =
        [&](const TreeNode* node, long long low, long long high) {
        if (node == nullptr) return true;
        if (!(low < node->val && node->val < high)) return false;
        return valid(node->left, low, node->val) &&
               valid(node->right, node->val, high);
    };

    return valid(root, std::numeric_limits<long long>::lowest(),
                 std::numeric_limits<long long>::max());
}
```

An inorder traversal of a strict BST is strictly increasing. Bounds validation is usually more explicit about the invariant; inorder validation is convenient for kth-smallest and sorted iteration.

```cpp
const TreeNode* searchBst(const TreeNode* root, int target) {
    const TreeNode* current = root;
    while (current != nullptr) {
        if (target == current->val) return current;
        current = target < current->val ? current->left : current->right;
    }
    return nullptr;
}
```

**When to use / recognition:** Ordered binary tree; searching by comparison; predecessor/successor; range query; kth smallest; or the input explicitly promises a BST.

**Complexity:** Search/insert/delete take `O(h)`: average `O(log n)` if balanced, worst-case `O(n)` if skewed. Traversal is `O(n)`. Do not claim `O(log n)` without a balance guarantee.

**Duplicates:** The problem must define whether duplicates are forbidden, counted, or always placed on one side. Adjust `<`/`<=` and bounds deliberately.

**Alternatives/trade-offs:** `unordered_map` has expected `O(1)` exact lookup but no sorted order. `map` gives `O(log n)` search and ordered operations. Sorting once can be simpler for static data.

**Common mistakes:** Local-only validation, assuming balance, using a stale previous-inorder value, mishandling numeric bounds, and mutating tree links during a read-only query.

### 12.7 Lowest Common Ancestor

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** LCA is a recurring ancestor/path pattern and an excellent test of recursive meaning, but appears less often than basic traversal.

For a general binary tree, let `dfs(node)` return a target if found in the subtree. If left and right both return non-null, the current node is their lowest meeting point.

```cpp
TreeNode* lowestCommonAncestor(TreeNode* root, const TreeNode* p,
                               const TreeNode* q) {
    if (root == nullptr || root == p || root == q) return root;

    TreeNode* left = lowestCommonAncestor(root->left, p, q);
    TreeNode* right = lowestCommonAncestor(root->right, p, q);
    if (left != nullptr && right != nullptr) return root;
    return left != nullptr ? left : right;
}
```

**Assumption:** This common template assumes both target nodes exist. If existence is not guaranteed, also count matches and return an answer only after finding two.

For a BST, ordering avoids searching both sides: if both values are smaller go left, if both are larger go right, otherwise the current node is the split point. Time is `O(h)` instead of a general `O(n)` traversal.

**Mistakes:** Comparing values when node identity matters, not clarifying whether a node can be its own ancestor, ignoring missing targets, or applying the BST shortcut to a normal binary tree.

### 12.8 Constructing Trees from Traversals

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Reconstruction tests whether you understand traversal boundaries and divide-and-conquer. Standard versions occur regularly; duplicate-heavy or exotic traversal pairs are less important.

Preorder reveals the root first. Inorder tells how many nodes belong to the left and right subtrees. A value-to-inorder-index map prevents a linear scan in each recursive call.

```cpp
TreeNode* buildTree(const std::vector<int>& preorder,
                    const std::vector<int>& inorder) {
    if (preorder.size() != inorder.size()) {
        throw std::invalid_argument("traversal lengths must match");
    }

    std::unordered_map<int, int> inorder_index;
    for (int i = 0; i < static_cast<int>(inorder.size()); ++i) {
        inorder_index[inorder[i]] = i;
    }

    int preorder_index = 0;
    std::function<TreeNode*(int, int)> build = [&](int left, int right) {
        if (left > right) return static_cast<TreeNode*>(nullptr);
        if (preorder_index >= static_cast<int>(preorder.size())) {
            throw std::invalid_argument("inconsistent traversals");
        }

        const int root_value = preorder[preorder_index++];
        const auto found = inorder_index.find(root_value);
        if (found == inorder_index.end() || found->second < left ||
            found->second > right) {
            throw std::invalid_argument("inconsistent or duplicate traversals");
        }

        TreeNode* root = new TreeNode(root_value);
        const int split = found->second;
        root->left = build(left, split - 1);
        root->right = build(split + 1, right);
        return root;
    };

    return build(0, static_cast<int>(inorder.size()) - 1);
}
```

**Complexity:** `O(n)` time and `O(n)` map plus `O(h)` recursion stack. Without the map, worst-case time is `O(n^2)`.

**Recognition:** Two traversals, unique values, reconstruct original hierarchy, or sorted array to height-balanced BST.

**Edge cases/mistakes:** Empty inputs, inconsistent traversal lengths, duplicates that make a single index map insufficient, off-by-one subrange boundaries, building right before left when consuming preorder, and copying vector subranges instead of passing indices.

**Alternative:** Postorder + inorder also uniquely reconstructs a tree with unique values; consume postorder from the end and build right before left.

### 12.9 Serialization and Deserialization

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Encode/decode questions test whether structure—not just values—is preserved. The pattern is common enough to practice once or twice, especially for platform/infrastructure roles.

Preorder plus an explicit null marker uniquely represents a binary tree.

```cpp
std::string serialize(const TreeNode* root) {
    std::ostringstream out;
    std::function<void(const TreeNode*)> dfs = [&](const TreeNode* node) {
        if (node == nullptr) {
            out << "#,";
            return;
        }
        out << node->val << ',';
        dfs(node->left);
        dfs(node->right);
    };
    dfs(root);
    return out.str();
}

TreeNode* deserialize(const std::string& data) {
    std::istringstream in(data);
    std::function<TreeNode*()> build = [&]() -> TreeNode* {
        std::string token;
        if (!std::getline(in, token, ',')) {
            throw std::invalid_argument("truncated tree encoding");
        }
        if (token == "#") return nullptr;

        TreeNode* node = new TreeNode(std::stoi(token));
        node->left = build();
        node->right = build();
        return node;
    };
    return build();
}
```

**Complexity:** `O(n)` time and `O(n)` output; `O(h)` recursion stack. The serialized representation uses one token per node/null child, still `O(n)`.

**Trade-offs:** Preorder streaming is compact and easy to parse recursively. BFS serialization is also valid and visually corresponds to level order, but may need trailing-null normalization. A BST can sometimes be encoded without null markers using ordering constraints, at the cost of a more specialized decoder.

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

#### Beginner

- **Maximum Depth of Binary Tree:** Teaches the base `nullptr -> 0` case and returning a subtree summary. Learn to state `depth(node) = 1 + max(...)`.
- **Invert Binary Tree:** Teaches visit-and-transform recursion and makes mutation order explicit.
- **Binary Tree Inorder Traversal:** Teaches recursive and iterative stack mechanics; for a BST, connect traversal order to sorted output.
- **Same Tree:** Teaches synchronized traversal and structural base cases, not just comparing value lists.

#### Core Interview

- **Binary Tree Level Order Traversal:** Teaches queue frontiers and level-size snapshots.
- **Validate Binary Search Tree:** Teaches ancestor bounds and why parent-only comparisons fail.
- **Lowest Common Ancestor of a Binary Tree:** Teaches a recursive result with three meanings: neither, one target, or both.
- **Diameter of Binary Tree:** Teaches the difference between an extendable child result and a complete global result.
- **Binary Tree Maximum Path Sum:** Extends diameter reasoning to weights and negative branches; learn to clamp an unusable branch at zero.
- **Construct Binary Tree from Preorder and Inorder:** Teaches traversal roles, index boundaries, and eliminating repeated scans.
- **Serialize and Deserialize Binary Tree:** Teaches preservation of structure using null markers.
- **Kth Smallest Element in a BST:** Teaches exploiting inorder ordering instead of sorting all values blindly.

#### Advanced

- **Binary Tree Right Side View (DFS and BFS):** Teaches that the same level invariant can be expressed through either traversal.
- **Recover Binary Search Tree:** Teaches detecting inversions in inorder order; useful after BST validation is solid.
- **All Nodes Distance K in Binary Tree:** Teaches converting parent-child hierarchy into an undirected graph and avoiding revisits.
- **Vertical Order Traversal:** Teaches coordinate state, grouping, and precise tie-breaking; read the specification carefully.

### Common Tree Mistakes and Interview Tips

- Draw a three-node example and an empty/single-node tree before coding.
- State whether height/path length counts nodes or edges.
- Say what `dfs(node)` returns and what side effects it has.
- Do not claim recursive space is `O(1)`; the call stack counts.
- Confirm whether values are unique and whether target nodes are guaranteed to exist.
- For path state stored in a `vector`, `push_back` before recursion and `pop_back` after it; copying the vector is simpler but more expensive.
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
- **How deeply to understand it:** Fluently use `std::priority_queue`, choose its default max-heap versus a `std::greater` min-heap, design `std::pair`/`std::tuple` ordering or a custom comparator, and analyze heap size. Know array indexing and heapify conceptually; implement sift operations only if explicitly requested.

### Focus First

- Min-heap versus max-heap and how to choose the corresponding `std::priority_queue` comparator.
- `push`, `top`, and `pop` complexity.
- Fixed-size heap for top-K and kth-element questions.
- K-way merge and priority-entry design with `std::pair` or `std::tuple`.

### Learn Later

- Two-heaps running median, lazy deletion, and heap-based schedulers.
- `O(n)` bottom-up heap construction and why it is not `O(n log n)`.

### Optional / Specialized

- **Indexed heaps / decrease-key implementation — 🟡 Tier 3 — Nice to Know.** Useful conceptually for graph algorithms; most interview code pushes a fresh pair and skips stale entries.
- **Binomial/Fibonacci/pairing heaps — ⚪ Tier 4 — Low Priority / Specialized.** Theoretical or specialized; not useful enough for normal interview preparation.

### 13.1 Heap Fundamentals and Operations

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** All heap patterns depend on correctly understanding what is—and is not—ordered.

For a zero-indexed array:

- Parent of index `i`: `(i - 1) / 2` with integer division
- Left child: `2 * i + 1`
- Right child: `2 * i + 2`

A min-heap guarantees only that each parent is `<=` its children, so the root is globally smallest. The rest of the array is **not sorted**.

| Operation | Binary heap time | Notes |
|---|---:|---|
| `top()` min/max | `O(1)` | Read the root |
| Insert | `O(log n)` | Append, then sift up |
| Remove root | `O(log n)` | Move last item to root, then sift down |
| Heapify `n` existing items | `O(n)` | Bottom-up construction |
| Search arbitrary value | `O(n)` | Heap order does not support general search |

```cpp
std::vector<int> values{7, 2, 5};

// Range construction performs bottom-up heap construction.
std::priority_queue<int, std::vector<int>, std::greater<int>> min_heap(
    values.begin(), values.end());
min_heap.push(3);                 // O(log n)
int smallest = min_heap.top();    // O(1)
min_heap.pop();                   // O(log n); pop() returns void

// std::priority_queue is a max-heap by default.
std::priority_queue<int> max_heap;
max_heap.push(7);
int largest = max_heap.top();
max_heap.pop();
```

**How it works:** Sift-up repairs the one possibly broken path from a new leaf to the root. Sift-down repairs the path from a replaced root to a leaf. Only one root-to-leaf path changes, hence `O(log n)`.

**Common mistakes:** Treating the underlying container as sorted; reversing `std::pair`/`std::tuple` fields; using the default max-heap when a min-heap is required; forgetting that `pop()` returns `void`; mutating an item already inside the heap; and assuming arbitrary deletion is `O(log n)` without an index map/lazy-deletion plan.

**Edge cases:** Empty heap before `top()`/`pop()`, equal priorities, payload types that lack an ordering after tied `std::tuple` fields, `k = 0`, and `k > n`.

**Trade-offs:** A heap gives only the next extreme, not full order. `std::set`/`std::map` support predecessor and range operations. Sorting gives all items in order but makes repeated dynamic insertions costly.

### 13.2 Top-K and Kth-Element Pattern

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Top-K is one of the most common direct heap signals and transfers to frequency, distance, score, and streaming problems.

To keep the `k` largest values, maintain a **min-heap of size at most `k`**. The root is the weakest member of the current winners.

```cpp
std::vector<int> kLargest(const std::vector<int>& values, int k) {
    if (k <= 0) return {};

    std::priority_queue<int, std::vector<int>, std::greater<int>> winners;
    for (int value : values) {
        winners.push(value);
        if (static_cast<int>(winners.size()) > k) winners.pop();
    }

    std::vector<int> result;
    result.reserve(winners.size());
    while (!winners.empty()) {
        result.push_back(winners.top());
        winners.pop();
    }
    return result;  // Contains the k largest in ascending pop order.
}
```

**Invariant:** After processing any prefix, the heap contains the largest `min(k, prefix_length)` items in that prefix.

**Complexity:** `O(n log k)` time and `O(k)` extra space. Sorting all values costs `O(n log n)` time, often simpler if ordered output is also needed. Quickselect has expected `O(n)` time for a single kth statistic but is harder to implement robustly and does not naturally support a stream.

**Direction rule:**

- Keep `k` largest → min-heap of winners; discard the smallest winner.
- Keep `k` smallest → max-heap of winners; discard the largest winner.

For **Top K Frequent**, first build a frequency map. Heap entries can be `(frequency, value)`. Bucket sorting can achieve `O(n)` because frequencies lie from `1` to `n`, but a heap generalizes naturally and costs `O(m log k)` for `m` distinct values.

### 13.3 K-Way Merge

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** This reusable pattern appears in merging sorted lists/arrays, external data streams, and sorted matrix questions.

Keep only the next unconsumed candidate from each of `k` sorted sources. Pop the smallest candidate, output it, then push its successor from the same source.

```cpp
std::vector<int> mergeSortedArrays(
    const std::vector<std::vector<int>>& arrays) {
    using Entry = std::tuple<int, int, int>;  // value, array index, element index
    std::priority_queue<Entry, std::vector<Entry>, std::greater<Entry>> frontier;

    for (int array_index = 0;
         array_index < static_cast<int>(arrays.size()); ++array_index) {
        if (!arrays[array_index].empty()) {
            frontier.push({arrays[array_index][0], array_index, 0});
        }
    }

    std::vector<int> merged;
    while (!frontier.empty()) {
        auto [value, array_index, element_index] = frontier.top();
        frontier.pop();
        merged.push_back(value);

        const int next_index = element_index + 1;
        if (next_index < static_cast<int>(arrays[array_index].size())) {
            frontier.push(
                {arrays[array_index][next_index], array_index, next_index});
        }
    }
    return merged;
}
```

**Complexity:** If there are `N` total items and at most `k` sources, time is `O(N log k)` and heap space is `O(k)`, excluding output.

**Recognition:** Multiple individually sorted inputs; need global sorted order or the kth global item; cannot concatenate and sort due to scale or streaming.

**Mistakes:** Inserting every item rather than one frontier per source, losing source identity, failing on empty sources, or omitting a deterministic tie-breaker when payload objects are not comparable.

### 13.4 Two Heaps / Running Median

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority:** It is a classic streaming design and demonstrates balancing invariants, though it appears less often than ordinary top-K.

Maintain:

- `lower`: a max-heap for the smaller half;
- `upper`: a min-heap for the larger half;
- size difference at most one; and every `lower` value `<=` every `upper` value.

```cpp
class MedianFinder {
    std::priority_queue<int> lower_;  // Max-heap: smaller half.
    std::priority_queue<int, std::vector<int>, std::greater<int>> upper_;

public:
    void add(int value) {
        lower_.push(value);
        upper_.push(lower_.top());
        lower_.pop();

        if (upper_.size() > lower_.size()) {
            lower_.push(upper_.top());
            upper_.pop();
        }
    }

    double median() const {
        if (lower_.empty()) throw std::logic_error("median of empty stream");
        if (lower_.size() > upper_.size()) return lower_.top();

        const long long sum = static_cast<long long>(lower_.top()) + upper_.top();
        return sum / 2.0;
    }
};
```

**Complexity:** `O(log n)` per insertion, `O(1)` median query, and `O(n)` storage.

**Edge cases/mistakes:** Querying before insertion, overflow while averaging two integers in fixed-width languages, forgetting a rebalance step, or maintaining sizes without maintaining cross-half order.

**Alternative:** `std::multiset` supports insertion and median maintenance but needs careful iterator management because it has no direct rank lookup. Sorting after every insertion is too slow; sorting once is best when all data arrives before queries.

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

#### Beginner

- **Kth Largest Element in an Array:** Teaches a size-`k` min-heap and comparison with sorting/quickselect.
- **Last Stone Weight:** Teaches max-heap simulation and repeated extraction.
- **K Closest Points to Origin:** Teaches priority keys and keeping only `k` candidates.

#### Core Interview

- **Top K Frequent Elements:** Teaches combining a hash-frequency table with a heap or frequency buckets.
- **Merge K Sorted Lists:** Teaches one frontier per source, tie-breaking, and `O(N log k)` analysis.
- **Find Median from Data Stream:** Teaches two balanced heaps and explicit invariants.
- **Task Scheduler / Meeting Rooms II:** Teaches heap-based resource release; also compare with counting or sweep-line alternatives.
- **Kth Smallest Element in a Sorted Matrix:** Teaches either k-way row merge or binary search on value, depending on constraints.

#### Advanced

- **Sliding Window Median:** Teaches lazy deletion because heaps do not support arbitrary removal efficiently.
- **Smallest Range Covering Elements from K Lists:** Extends k-way merge while tracking the current maximum.
- **IPO / maximize capital:** Teaches two heaps or sort + heap to expose currently feasible choices.

### Common Heap Mistakes and Interview Tips

- Say what the root means and what the heap invariant preserves.
- Include heap size in the complexity: `O(n log k)`, not simply `O(n log n)`.
- Clarify whether output itself must be sorted; a heap's internal array is not sorted.
- Prefer `tuple` entries with stable primitive tie-breakers, such as `(priority, unique_id, payload)`.
- When graph code pushes duplicate entries instead of decreasing keys, skip a popped entry if it is stale.
- If `k` is close to `n` and ordered output is needed, sorting may be clearer with similar practical cost.

### Heap Mastery Checklist

I have mastered interview heaps when I can:

- [ ] Explain the heap invariant and why the entire array is not sorted.
- [ ] Use C++ min-heap and max-heap `priority_queue` declarations correctly from memory.
- [ ] State `top`, `push`, `pop`, and heap-construction complexities.
- [ ] Choose the correct heap direction for top-K.
- [ ] Derive and implement `O(n log k)` top-K and `O(N log k)` k-way merge.
- [ ] Design safe tuple/comparator keys and tie-breakers.
- [ ] Implement and explain the two-heaps median invariant.
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
- DFS/BFS with a `vector<bool>` or `unordered_set` for visited state and correct visited timing.
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

#### Adjacency list

```cpp
using Graph = std::vector<std::vector<int>>;
using WeightedGraph =
    std::vector<std::vector<std::pair<int, long long>>>;  // neighbor, weight

Graph buildUndirectedGraph(int n,
                           const std::vector<std::pair<int, int>>& edges) {
    Graph graph(n);
    for (const auto& [a, b] : edges) {
        graph[a].push_back(b);
        graph[b].push_back(a);
    }
    return graph;
}

WeightedGraph buildDirectedWeightedGraph(
    int n, const std::vector<std::tuple<int, int, long long>>& edges) {
    WeightedGraph graph(n);
    for (const auto& [source, target, weight] : edges) {
        graph[source].push_back({target, weight});
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

A `vector` of `(u, v)` or `(u, v, w)` edges is compact and ideal for algorithms that process edges globally, such as Kruskal and Bellman-Ford. It is poor for repeatedly asking for one vertex's neighbors unless converted.

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

DFS follows one path as far as possible, then backtracks. A visited array (or an `unordered_set` for non-contiguous labels) means each graph state is processed at most once and prevents infinite loops in cyclic graphs.

```cpp
std::vector<bool> dfsRecursive(const Graph& graph, int start) {
    std::vector<bool> visited(graph.size(), false);
    std::function<void(int)> dfs = [&](int node) {
        visited[node] = true;
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) dfs(neighbor);
        }
    };
    dfs(start);
    return visited;
}

std::vector<bool> dfsIterative(const Graph& graph, int start) {
    std::vector<bool> visited(graph.size(), false);
    std::stack<int> pending;
    visited[start] = true;
    pending.push(start);

    while (!pending.empty()) {
        const int node = pending.top();
        pending.pop();
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                pending.push(neighbor);
            }
        }
    }
    return visited;
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

```cpp
std::pair<int, std::vector<int>> shortestUnweightedPath(
    const Graph& graph, int start, int target) {
    std::vector<int> distance(graph.size(), -1);
    std::vector<int> parent(graph.size(), -1);
    std::queue<int> pending;
    distance[start] = 0;
    pending.push(start);

    while (!pending.empty()) {
        const int node = pending.front();
        pending.pop();
        if (node == target) {
            std::vector<int> path;
            for (int current = target; current != -1; current = parent[current]) {
                path.push_back(current);
            }
            std::reverse(path.begin(), path.end());
            return {distance[target], path};
        }

        for (int neighbor : graph[node]) {
            if (distance[neighbor] == -1) {  // Also acts as visited.
                distance[neighbor] = distance[node] + 1;
                parent[neighbor] = node;
                pending.push(neighbor);
            }
        }
    }
    return {-1, {}};
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

```cpp
std::vector<int> nearestSourceDistance(const Graph& graph,
                                       const std::vector<int>& sources) {
    std::vector<int> distance(graph.size(), -1);
    std::queue<int> pending;
    for (int source : sources) {
        if (distance[source] == -1) {
            distance[source] = 0;
            pending.push(source);
        }
    }

    while (!pending.empty()) {
        const int node = pending.front();
        pending.pop();
        for (int neighbor : graph[node]) {
            if (distance[neighbor] == -1) {
                distance[neighbor] = distance[node] + 1;
                pending.push(neighbor);
            }
        }
    }
    return distance;
}
```

**Recognition:** “Distance to nearest gate/zero/hospital,” simultaneous spread/infection/fire, or minimum time until every reachable cell changes.

**Complexity:** Still `O(V + E)`, not multiplied by the number of sources.

**Mistakes:** Running a separate BFS from every cell/source, failing to enqueue all initial sources before traversal, and confusing simultaneous time steps with sequential source processing.

### 14.5 Connected Components

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Component counting is a core reuse of traversal and appears in graphs, grids, accounts, and connectivity stories.

```cpp
int countComponents(int n, const std::vector<std::pair<int, int>>& edges) {
    const Graph graph = buildUndirectedGraph(n, edges);
    std::vector<bool> visited(n, false);
    int components = 0;

    for (int node = 0; node < n; ++node) {
        if (visited[node]) continue;
        ++components;
        std::stack<int> pending;
        pending.push(node);
        visited[node] = true;

        while (!pending.empty()) {
            const int current = pending.top();
            pending.pop();
            for (int neighbor : graph[current]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    pending.push(neighbor);
                }
            }
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

```cpp
int countIslands(std::vector<std::vector<char>>& grid) {
    if (grid.empty() || grid.front().empty()) return 0;
    const int rows = static_cast<int>(grid.size());
    const int cols = static_cast<int>(grid.front().size());
    const std::array<std::pair<int, int>, 4> directions{{
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    }};

    auto flood_fill = [&](int start_row, int start_col) {
        std::stack<std::pair<int, int>> pending;
        pending.push({start_row, start_col});
        grid[start_row][start_col] = '0';  // Explicitly mutates the input.

        while (!pending.empty()) {
            auto [row, col] = pending.top();
            pending.pop();
            for (const auto& [dr, dc] : directions) {
                const int next_row = row + dr;
                const int next_col = col + dc;
                if (0 <= next_row && next_row < rows &&
                    0 <= next_col && next_col < cols &&
                    grid[next_row][next_col] == '1') {
                    grid[next_row][next_col] = '0';
                    pending.push({next_row, next_col});
                }
            }
        }
    };

    int islands = 0;
    for (int row = 0; row < rows; ++row) {
        for (int col = 0; col < cols; ++col) {
            if (grid[row][col] == '1') {
                ++islands;
                flood_fill(row, col);
            }
        }
    }
    return islands;
}
```

**Complexity:** For an `R x C` grid, `O(RC)` time and up to `O(RC)` DFS/BFS state. Each cell and constant number of neighbor edges is considered at most once.

**Recognition:** Regions, islands, connected pixels, spreading, nearest cell, shortest moves, maze, or board-state transitions.

**State modeling:** Sometimes `(r, c)` is insufficient. If future moves depend on collected keys, remaining obstacle eliminations, direction, or time parity, those fields belong in the visited state.

**Mutation trade-off:** Marking the grid itself saves separate storage but destroys input. Ask whether mutation is permitted. A separate `vector<vector<bool>> visited` preserves input at `O(RC)` extra space.

**Common mistakes/edge cases:** Swapping rows and columns; assuming rectangular input without checking the contract; wrong four-versus-eight-direction rule; marking on pop; revisiting start; empty grid; and treating a changed resource count as the same state.

### 14.7 Cycle Detection

#### Undirected graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is a standard extension of DFS/BFS and directly tests whether the candidate distinguishes a parent edge from a real back edge.

During DFS, encountering an already visited neighbor indicates a cycle **unless that neighbor is the node we just came from**.

```cpp
bool hasUndirectedCycle(const Graph& graph) {
    std::vector<bool> visited(graph.size(), false);
    std::function<bool(int, int)> dfs = [&](int node, int parent) {
        visited[node] = true;
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                if (dfs(neighbor, node)) return true;
            } else if (neighbor != parent) {
                return true;
            }
        }
        return false;
    };

    for (int node = 0; node < static_cast<int>(graph.size()); ++node) {
        if (!visited[node] && dfs(node, -1)) return true;
    }
    return false;
}
```

**Complexity:** `O(V + E)` time and `O(V)` traversal space.

**Alternative:** DSU detects whether an undirected edge connects vertices already in the same set. DFS is better if adjacency and the actual cycle/path matter.

**Mistakes:** Treating the parent edge as a cycle, missing disconnected components, and assuming the simple parent rule handles all parallel-edge conventions without clarification.

#### Directed graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Directed cycle detection underlies prerequisite validation and topological sorting.

A visited node is not automatically a cycle. A cycle exists when DFS reaches a node that is still in the **current recursion path**. Use three states: `0 = unvisited`, `1 = visiting`, `2 = finished`.

```cpp
bool hasDirectedCycle(const Graph& graph) {
    std::vector<int> state(graph.size(), 0);
    std::function<bool(int)> dfs = [&](int node) {
        if (state[node] == 1) return true;
        if (state[node] == 2) return false;

        state[node] = 1;
        for (int neighbor : graph[node]) {
            if (dfs(neighbor)) return true;
        }
        state[node] = 2;
        return false;
    };

    for (int node = 0; node < static_cast<int>(graph.size()); ++node) {
        if (state[node] == 0 && dfs(node)) return true;
    }
    return false;
}
```

**Complexity:** `O(V + E)` time and `O(V)` state/recursion.

**Alternative:** Kahn's topological-sort algorithm detects a directed cycle if fewer than `V` nodes can be processed.

**Mistakes:** Using only one visited boolean; forgetting to mark a node finished; carrying a recursion-path `unordered_set` between independent completed paths; and reversing prerequisite edge direction accidentally.

### 14.8 Topological Sorting

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Dependency scheduling is a common interview story. A candidate should recognize that a valid linear order exists only for a directed acyclic graph (DAG).

#### Kahn's algorithm: indegree BFS

`indegree[v]` counts unresolved prerequisites entering `v`. Start with every zero-indegree node, remove it, and decrement the indegree of its dependents.

```cpp
std::vector<int> topologicalOrder(
    int n, const std::vector<std::pair<int, int>>& edges) {
    Graph graph(n);
    std::vector<int> indegree(n, 0);
    for (const auto& [prerequisite, course] : edges) {
        graph[prerequisite].push_back(course);
        ++indegree[course];
    }

    std::queue<int> ready;
    for (int node = 0; node < n; ++node) {
        if (indegree[node] == 0) ready.push(node);
    }

    std::vector<int> order;
    while (!ready.empty()) {
        const int node = ready.front();
        ready.pop();
        order.push_back(node);
        for (int neighbor : graph[node]) {
            if (--indegree[neighbor] == 0) ready.push(neighbor);
        }
    }
    return static_cast<int>(order.size()) == n ? order : std::vector<int>{};
}
```

**Invariant:** Every emitted node has no remaining incoming edge from an unprocessed node.

**Complexity:** `O(V + E)` time and `O(V + E)` space including adjacency.

**Recognition:** Prerequisites, build order, dependencies, alien alphabet constraints, or ordering tasks while respecting “before” relationships.

**DFS alternative:** Append each node after exploring all outgoing edges (postorder), then reverse the result; use visiting/finished states to reject cycles. Kahn is often easier when cycle detection and indegree-based availability are central.

**Trade-offs:** Multiple valid orders may exist. A normal queue returns any valid order; a min-heap returns the lexicographically smallest available order at `O((V + E) log V)` rather than linear time.

**Common mistakes/edge cases:** Reversing edge direction; omitting nodes with no edges; not verifying `order.size() == V`; decrementing indegree more than once; assuming uniqueness; self-loop; and duplicate constraints that inflate indegrees unless consistently represented.

### 14.9 Union-Find / Disjoint Set Union (DSU)

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** DSU is the cleanest structure for incremental undirected connectivity, redundant edges, account grouping, and Kruskal. It is frequent enough to implement confidently after BFS/DFS.

#### Intuition and how it works

Each component has a representative root. `find(x)` returns it. `unite(a, b)` merges two components. Path compression flattens find paths; union by size/rank attaches the smaller tree below the larger.

```cpp
class DSU {
    std::vector<int> parent_;
    std::vector<int> size_;
    int components_;

public:
    explicit DSU(int n) : parent_(n), size_(n, 1), components_(n) {
        std::iota(parent_.begin(), parent_.end(), 0);
    }

    int find(int x) {
        if (parent_[x] != x) parent_[x] = find(parent_[x]);
        return parent_[x];
    }

    bool unite(int a, int b) {
        int root_a = find(a);
        int root_b = find(b);
        if (root_a == root_b) return false;
        if (size_[root_a] < size_[root_b]) std::swap(root_a, root_b);
        parent_[root_b] = root_a;
        size_[root_a] += size_[root_b];
        --components_;
        return true;
    }

    int components() const { return components_; }
};
```

**Complexity:** With both optimizations, a sequence of operations takes nearly constant amortized time: `O(alpha(V))` per operation, where inverse Ackermann `alpha` grows so slowly it is below `5` for practical inputs. Space is `O(V)`.

**Recognition:** Repeatedly add undirected connections; ask whether two items belong to the same group; count groups; detect a redundant edge; merge accounts sharing identifiers; or choose non-cycling edges for an MST.

**Invariant:** `parent[root] == root`; sizes/ranks are meaningful at roots; two nodes are connected exactly when their roots match.

**Alternatives/trade-offs:** DFS/BFS handles static connectivity and can list paths/component members directly. DSU answers merge/connectivity efficiently but does not support general edge deletion or reveal an actual route.

**Common mistakes/edge cases:** Comparing immediate parents instead of roots; updating size on the child root; forgetting path compression's assignment; decrementing component count for a no-op union; arbitrary string labels without mapping; and trying to use ordinary DSU for directed reachability.

### 14.10 Bipartite Graphs

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Two-coloring is a standard constraint pattern and a useful cycle-property test; it appears moderately often.

A graph is bipartite if its vertices can be colored with two colors so every edge connects opposite colors. Equivalently, an undirected graph is bipartite iff it has no odd-length cycle.

```cpp
bool isBipartite(const Graph& graph) {
    std::vector<int> color(graph.size(), -1);
    for (int start = 0; start < static_cast<int>(graph.size()); ++start) {
        if (color[start] != -1) continue;
        color[start] = 0;
        std::queue<int> pending;
        pending.push(start);

        while (!pending.empty()) {
            const int node = pending.front();
            pending.pop();
            for (int neighbor : graph[node]) {
                if (color[neighbor] == -1) {
                    color[neighbor] = 1 - color[node];
                    pending.push(neighbor);
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
| Weights are only `0` or `1` | 0-1 BFS with `std::deque` | `O(V + E)` |
| Nonnegative weights | Dijkstra | Duplicate-entry heap: `O((V + E) log E)` general, commonly `O((V + E) log V)` for simple graphs |
| Negative edges, no reachable negative cycle | Bellman-Ford | `O(VE)` |
| All-pairs, small/dense graph | Floyd-Warshall | `O(V^3)` |
| DAG with any edge weights | Topological relaxation | `O(V + E)` |

Do not choose from the word *shortest* alone. Inspect the edge-cost model first.

### 14.12 Dijkstra's Algorithm

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is the standard nonnegative-weight shortest-path algorithm and a common heap/graph interview topic. Negative-weight variants are much less common.

#### Intuition and invariant

Maintain the best tentative distance known for every node. Repeatedly pop the smallest distance from a min-heap. With nonnegative edges, that popped non-stale distance cannot later be improved by traveling through a farther node.

```cpp
std::vector<long long> dijkstra(const WeightedGraph& graph, int source) {
    const long long INF = std::numeric_limits<long long>::max() / 4;
    std::vector<long long> distance(graph.size(), INF);
    using State = std::pair<long long, int>;  // distance, node
    std::priority_queue<State, std::vector<State>, std::greater<State>> frontier;
    distance[source] = 0;
    frontier.push({0, source});

    while (!frontier.empty()) {
        const auto [dist, node] = frontier.top();
        frontier.pop();
        if (dist != distance[node]) continue;  // Stale entry.

        for (const auto& [neighbor, weight] : graph[node]) {
            if (weight < 0) {
                throw std::invalid_argument("Dijkstra requires nonnegative weights");
            }
            if (dist > INF - weight) continue;  // Guard addition overflow.
            const long long candidate = dist + weight;
            if (candidate < distance[neighbor]) {
                distance[neighbor] = candidate;
                frontier.push({candidate, neighbor});
            }
        }
    }
    return distance;
}
```

**Relaxation:** For edge `u -> v` with weight `w`, if `dist[u] + w < dist[v]`, update `dist[v]` and record `u` as parent if a path must be reconstructed.

**Complexity:** With an adjacency list and duplicate-entry binary heap, the fully general bound is `O((V + E) log E)` time because the heap can hold `O(E)` entries, plus `O(V + E)` graph/distance storage. For a simple graph, `E ≤ V²`, so `log E = O(log V)` and the familiar bound is `O((V + E) log V)` (often summarized as `O(E log V)` for connected graphs).

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

```cpp
struct Edge {
    long long weight;
    int u;
    int v;
};

std::optional<long long> kruskalMst(int n, std::vector<Edge> edges) {
    if (n == 0) return 0LL;
    std::sort(edges.begin(), edges.end(),
              [](const Edge& a, const Edge& b) { return a.weight < b.weight; });

    DSU dsu(n);
    long long total = 0;
    int used = 0;
    for (const Edge& edge : edges) {
        if (dsu.unite(edge.u, edge.v)) {
            total += edge.weight;
            if (++used == n - 1) break;
        }
    }
    if (used != n - 1) return std::nullopt;
    return total;
}
```

**Complexity:** `O(E log E)` time dominated by sorting; DSU operations add nearly linear time. Space is `O(V)` excluding the edge list/sort implementation.

#### Prim's algorithm

Start from one node and repeatedly use a min-heap to choose the cheapest edge crossing from the built tree to an unvisited vertex. With an adjacency list and binary heap: `O(E log V)` time and `O(V + E)` space.

**Recognition:** Connect all locations/devices with minimum total installation cost; allowed pairwise connection prices; output `V - 1` links.

**Trade-offs:** Kruskal is natural with an edge list and sparse graph; Prim is natural with adjacency and growing one component. If the graph is disconnected, the result is a minimum spanning forest unless the problem demands failure.

**Common mistakes:** Applying MST to a directed graph without a specialized formulation; confusing it with shortest paths; not checking connectivity; adding cycles; and assuming the MST is unique when weights tie.

### 14.14 Advanced Shortest Paths and Graph Algorithms

- **0-1 BFS — 🟡 Tier 3 — Nice to Know.** For edge weights exactly `0` or `1`, push zero-cost transitions to the front and one-cost transitions to the back of a `std::deque`. It runs in `O(V + E)` and is worth recognizing.
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

#### Beginner

- **Find if Path Exists in Graph:** Teaches adjacency construction, visited state, and reachability.
- **Flood Fill:** Teaches a grid as a graph, preserving the original color, and marking on discovery.
- **Number of Islands:** Teaches an outer component loop and mutation-versus-visited choices.
- **Clone Graph:** Teaches old-node-to-new-node mapping and cycles; the map simultaneously prevents repeated cloning.

#### Core Interview

- **Rotting Oranges / Walls and Gates:** Teaches multi-source BFS and simultaneous time layers.
- **Word Ladder:** Teaches implicit graph modeling and BFS for minimum transformations; avoid quadratic neighbor construction where constraints demand pattern indexing.
- **Course Schedule I/II:** Teaches directed cycle detection and topological order.
- **Number of Connected Components:** Teaches DFS/BFS versus DSU selection.
- **Redundant Connection:** Teaches failed union as an undirected cycle signal.
- **Graph Valid Tree:** Teaches that a tree requires connectivity plus no cycle (equivalently, connected and `E = V - 1`).
- **Is Graph Bipartite?:** Teaches two-coloring across disconnected components.
- **Network Delay Time:** Teaches Dijkstra, relaxation, stale entries, and unreachable results.
- **Pacific Atlantic Water Flow:** Teaches reversing edges/search direction and launching multi-source traversal from boundaries.

#### Advanced

- **Cheapest Flights Within K Stops:** Teaches that node alone may not define state; compare bounded Bellman-Ford, BFS-by-layers, and careful heap state.
- **Path With Minimum Effort:** Teaches minimax path cost and either Dijkstra or binary-search-on-threshold plus reachability.
- **Alien Dictionary:** Teaches deriving precedence edges, invalid prefix cases, duplicate-edge handling, and topological order.
- **Min Cost to Connect All Points:** Teaches MST selection and dense-graph trade-offs.
- **Shortest Path to Get All Keys:** Teaches BFS on composite state `(row, col, key_mask)`.

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
- [ ] Explain MST versus shortest path and implement basic Kruskal or Prim.
- [ ] Identify when composite state is necessary.

---

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

### 15.1 Recursion Fundamentals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Recursion is the foundation of tree DFS, graph DFS, divide-and-conquer, memoization, and backtracking.

Every correct recursive function needs:

1. **Contract:** What does `f(state)` return or accomplish?
2. **Base case:** Which smallest/terminal state stops recursion?
3. **Progress:** How does every call move toward a base case?
4. **Combination:** How are subproblem results assembled?

```cpp
long long factorial(int n) {
    if (n < 0) throw std::invalid_argument("factorial requires n >= 0");
    if (n <= 1) return 1;                 // Base case.
    return n * factorial(n - 1);          // Progress and combination.
}
```

This example takes `O(n)` time and `O(n)` call-stack space. C++ does not guarantee tail-call optimization, so even a tail-recursive formulation must not be analyzed as constant-space.

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

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Nearly every standard combination/permutation/constraint-search solution is a specialization of this control flow.

```cpp
template <class State, class Choices, class IsComplete, class Record,
          class ValidChoices, class Apply, class Undo>
void backtrack(State& state, const Choices& choices, IsComplete& is_complete,
               Record& record, ValidChoices& valid_choices,
               Apply& apply, Undo& undo) {
    if (is_complete(state)) {
        record(state);  // Record a snapshot, not a reference to mutable state.
        return;
    }

    for (const auto& choice : valid_choices(state, choices)) {
        apply(choice, state);  // Choose.
        backtrack(state, choices, is_complete, record, valid_choices,
                  apply, undo);  // Explore.
        undo(choice, state);   // Unchoose.
    }
}
```

Before writing it, identify:

- **State:** the minimum information that determines future choices.
- **Choices:** what decisions are possible here.
- **Constraints:** what makes a partial candidate invalid.
- **Goal/base case:** when to record or return success.
- **Restoration:** exactly what mutation each recursive call must undo.

**Copy versus mutate:** Copying state for each call simplifies restoration but costs time/space. In-place mutation plus undo is efficient and common. In C++, `answers.push_back(path)` copies a `vector`; do not store a pointer or `reference_wrapper` to the one mutable path unless that lifetime and aliasing are intentional.

**When to stop early:** If the problem asks only whether a solution exists, return `true` immediately on success instead of enumerating all answers.

### 15.4 Subsets: Choose or Skip

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Subsets are the simplest decision tree and teach output-sensitive complexity, path snapshots, and index progress.

```cpp
std::vector<std::vector<int>> subsets(const std::vector<int>& nums) {
    std::vector<std::vector<int>> result;
    std::vector<int> path;

    std::function<void(int)> dfs = [&](int index) {
        if (index == static_cast<int>(nums.size())) {
            result.push_back(path);  // Copies the current snapshot.
            return;
        }

        path.push_back(nums[index]);  // Include.
        dfs(index + 1);
        path.pop_back();

        dfs(index + 1);               // Exclude.
    };

    dfs(0);
    return result;
}
```

An equally useful form records `path` at every node and loops choices from a `start` index. That form generalizes naturally to combinations.

**Recognition:** All selections, subsequences where relative order is retained, choose any number of items, or powerset.

**Complexity:** `2^n` subsets. `O(n * 2^n)` time including copying output; `O(n)` recursion/path auxiliary space, excluding `O(n * 2^n)` output.

**Mistakes/edge cases:** Storing a pointer/reference to the mutable `path` instead of copying it; failing to advance the index; treating subsequences as substrings; and producing duplicates when input contains repeated values.

### 15.5 Combinations: Start-Index Search

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Many “choose `k`” and sum-construction questions depend on controlling which future candidates remain eligible.

```cpp
std::vector<std::vector<int>> combine(int n, int k) {
    if (k < 0 || k > n) return {};
    std::vector<std::vector<int>> result;
    std::vector<int> path;

    std::function<void(int)> dfs = [&](int start) {
        if (static_cast<int>(path.size()) == k) {
            result.push_back(path);
            return;
        }

        const int needed = k - static_cast<int>(path.size());
        const int last_start = n - needed + 1;
        for (int value = start; value <= last_start; ++value) {
            path.push_back(value);
            dfs(value + 1);
            path.pop_back();
        }
    };

    dfs(1);
    return result;
}
```

**Invariant:** `start` prevents both reusing earlier items and generating the same combination in different orders. The upper bound prunes branches that cannot collect enough remaining elements.

**Complexity:** There are `C(n, k)` outputs, each length `k`, so at least `O(k * C(n, k))` output work; auxiliary depth is `O(k)`.

**Reuse variants:**

- Each candidate used once → recurse with `i + 1`.
- Same candidate reusable → recurse with `i`.
- Order matters → do not use a monotonic start index; usually use permutation state or DP counting depending on the question.

**Mistakes:** Advancing incorrectly, confusing combinations with permutations, not sorting before value-based pruning, and using `break` on an unsorted candidate list.

### 15.6 Permutations: Used-Choice Search

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Permutations test state restoration and the distinction between position choices and monotonic combinations.

```cpp
std::vector<std::vector<int>> permutations(const std::vector<int>& nums) {
    std::vector<std::vector<int>> result;
    std::vector<int> path;
    std::vector<char> used(nums.size(), false);

    std::function<void()> dfs = [&]() {
        if (path.size() == nums.size()) {
            result.push_back(path);
            return;
        }

        for (int i = 0; i < static_cast<int>(nums.size()); ++i) {
            if (used[i]) continue;
            used[i] = true;
            path.push_back(nums[i]);
            dfs();
            path.pop_back();
            used[i] = false;
        }
    };

    dfs();
    return result;
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

```cpp
std::vector<std::vector<int>> uniqueSubsets(std::vector<int> nums) {
    std::sort(nums.begin(), nums.end());  // Sort the local copy.
    std::vector<std::vector<int>> result;
    std::vector<int> path;

    std::function<void(int)> dfs = [&](int start) {
        result.push_back(path);
        for (int i = start; i < static_cast<int>(nums.size()); ++i) {
            if (i > start && nums[i] == nums[i - 1]) continue;
            path.push_back(nums[i]);
            dfs(i + 1);
            path.pop_back();
        }
    };

    dfs(0);
    return result;
}
```

Why `i > start` rather than `i > 0`? Equal values may both appear in one valid candidate at different depths; only repeated sibling choices produce duplicate branches.

For unique permutations after sorting, skip `nums[i]` when it equals `nums[i - 1]` **and the previous equal item has not been used in the current path**. This chooses a consistent order among equal siblings.

**Alternative:** Put completed vectors into a `set<vector<int>>`; simpler but explores duplicate branches, adds logarithmic insertion work, and obscures the desired invariant.

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

```cpp
std::vector<std::vector<int>> combinationSum(std::vector<int> candidates,
                                             int target) {
    std::sort(candidates.begin(), candidates.end());
    std::vector<std::vector<int>> result;
    std::vector<int> path;

    std::function<void(int, int)> dfs = [&](int start, int remaining) {
        if (remaining == 0) {
            result.push_back(path);
            return;
        }

        for (int i = start; i < static_cast<int>(candidates.size()); ++i) {
            const int value = candidates[i];
            if (value > remaining) break;  // Positive, sorted candidates.
            path.push_back(value);
            dfs(i, remaining - value);     // Reuse is allowed.
            path.pop_back();
        }
    };

    if (target >= 0) dfs(0, target);
    return result;
}
```

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

#### Beginner

- **Fibonacci / factorial (for tracing only):** Teaches base cases and call-stack tracing; do not use naive Fibonacci as a production solution.
- **Subsets:** Teaches a binary decision tree, snapshots, and output-sensitive complexity.
- **Combinations:** Teaches `start` index and remaining-choice pruning.
- **Permutations:** Teaches `used` state and restoration.

#### Core Interview

- **Combination Sum / Combination Sum II:** Teaches reuse rules, sorted pruning, and same-level duplicate skipping.
- **Subsets II / Permutations II:** Teaches precise duplicate-branch control.
- **Word Search:** Teaches path-local visited state and restoration in a grid.
- **Palindrome Partitioning:** Teaches selecting cuts, validating a piece, and later optimizing repeated palindrome checks.
- **Generate Parentheses:** Teaches pruning by an invariant: never close more groups than have been opened.
- **Letter Combinations of a Phone Number:** Teaches one decision per input position and variable branching.

#### Advanced

- **N-Queens:** Teaches constant-time conflict sets for columns and diagonals instead of rescanning the board.
- **Sudoku Solver:** Teaches choosing constrained variables, occupancy sets, and aggressive pruning.
- **Restore IP Addresses:** Teaches segment-length/value constraints and remaining-length pruning.
- **Partition to K Equal Sum Subsets:** Teaches symmetry pruning, bitmask/memoized state, and the boundary between backtracking and DP.

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
- [ ] Handle duplicate inputs without relying only on a `std::set` of completed results.
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
- **Prerequisites:** Sorting, comparison functions, loops/invariants, heaps, intervals, and basic DP awareness.
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

### 16.1 Greedy Reasoning: Choice, Invariant, Proof

**Priority:** 🔴 Tier 1 — Must Master

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

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** Sorting reveals an order in which a small amount of state is enough to make safe choices. This is the dominant interview greedy pattern.

Possible sort keys have very different meanings:

- **Earliest finishing time:** leaves the most future room; maximizes count of non-overlapping intervals.
- **Start time:** exposes overlaps for merging; does not by itself maximize scheduled count.
- **Demand/size:** supports matching smallest sufficient resource to smallest unmet need.
- **Deadline:** often supports scheduling feasibility or lateness reasoning.
- **Difference/ratio:** may be useful in particular cost models, but requires a proof; ratios do not solve 0/1 knapsack.

Sorting generally makes total time `O(n log n)` even if the scan is `O(n)`. Space depends on whether sort is in-place and on language implementation.

**Mistakes:** Sorting by the most obvious field rather than the field that makes the invariant safe; losing original indices when output requires them; comparator overflow in languages using subtraction; and claiming the scan's `O(n)` while omitting sort cost.

### 16.3 Interval Scheduling / Maximum Non-Overlapping Selection

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is the canonical provably correct greedy problem and directly powers interval-removal variants.

The canonical implementation, earliest-finish invariant, endpoint semantics, complexity, minimum-removals reduction, and weighted-DP contrast are taught once in [Non-Overlapping Selection and Minimum Removals](#175-non-overlapping-selection-and-minimum-removals). Here, retain the greedy lesson: choosing the compatible interval that finishes earliest leaves at least as much room as any alternative and can be justified with an exchange argument.

### 16.4 Reachability Frontier

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** A single dominance value often replaces a quadratic search or DP in jump/reachability questions.

```cpp
bool canJump(const std::vector<int>& nums) {
    long long farthest = 0;
    for (int i = 0; i < static_cast<int>(nums.size()); ++i) {
        if (i > farthest) return false;
        farthest = std::max(farthest, static_cast<long long>(i) + nums[i]);
    }
    return true;
}
```

**Invariant:** Every index up to `farthest` is reachable through some processed choice. If the scan reaches an index beyond the frontier, no earlier choice can cross the gap.

**Complexity:** `O(n)` time and `O(1)` space.

For minimum jumps, process a current reachable layer similarly to BFS: track the farthest next-layer endpoint and increase the jump count when finishing the current range.

**Recognition:** Choices cover a contiguous reachable prefix/range, and among partial solutions only the farthest frontier matters.

**Mistakes:** Choosing the largest immediate jump rather than the best resulting frontier; stepping from an unreachable index; and applying the pattern when reachable states are not a contiguous dominated region.

### 16.5 Resource Matching

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Matching the smallest sufficient resource to the smallest remaining demand is a reusable sort-and-scan idea in assignment questions.

Sort demands and resources. If the smallest resource cannot satisfy the smallest demand, it cannot satisfy any larger demand, so discard it. If it can, use it there; saving that small resource cannot enable more matches than consuming it now.

```cpp
int maxAssignments(std::vector<int> demands, std::vector<int> resources) {
    std::sort(demands.begin(), demands.end());
    std::sort(resources.begin(), resources.end());
    std::size_t demand_index = 0;
    std::size_t resource_index = 0;
    int matches = 0;

    while (demand_index < demands.size() &&
           resource_index < resources.size()) {
        if (resources[resource_index] >= demands[demand_index]) {
            ++matches;
            ++demand_index;
        }
        ++resource_index;
    }
    return matches;
}
```

**Complexity:** `O(n log n + m log m)` time and sorting-dependent space.

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

**Priority:** 🔴 Tier 1 — Must Master

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

#### Beginner

- **Assign Cookies:** Teaches sorted two-pointer resource matching and an exchange argument.
- **Best Time to Buy and Sell Stock II:** Teaches decomposing every upward run into positive daily gains; distinguish from one-transaction stock.
- **Lemonade Change:** Teaches maintaining the most flexible change inventory and choosing high-denomination change first.

#### Core Interview

- **Jump Game:** Teaches a farthest-reachable invariant.
- **Jump Game II:** Teaches BFS-like greedy layers over reachable ranges.
- **Non-overlapping Intervals:** Teaches earliest-finish scheduling and minimizing removals.
- **Gas Station:** Teaches total feasibility plus resetting a failed candidate start; understand why a negative prefix invalidates every start within it.
- **Partition Labels:** Teaches last-occurrence boundaries and closing a segment only when all included characters end within it.
- **Task Scheduler:** Teaches frequency bottlenecks; compare closed-form/counting and heap simulation.
- **Boats to Save People:** Teaches sorting and pairing the heaviest person with the lightest feasible partner.

#### Advanced

- **Minimum Number of Refueling Stops:** Teaches delayed decisions with a max-heap of previously passed stations.
- **Course Schedule III:** Teaches sorting by deadline and replacing the longest selected duration when necessary.
- **Candy:** Teaches satisfying directional local constraints with two passes.
- **Remove Duplicate Letters:** Teaches greedy lexicographic choice with a monotonic stack, future occurrence knowledge, and membership state.

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
- **Prerequisites:** Sorting, comparators, arrays, greedy invariants, heaps, and basic prefix/difference ideas.
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

```cpp
using Interval = std::pair<long long, long long>;  // start, end

std::vector<Interval> mergeIntervals(std::vector<Interval> intervals) {
    if (intervals.empty()) return {};
    std::sort(intervals.begin(), intervals.end());  // Sorts the local copy.

    std::vector<Interval> merged;
    merged.push_back(intervals.front());
    for (std::size_t i = 1; i < intervals.size(); ++i) {
        const auto [start, end] = intervals[i];
        Interval& last = merged.back();
        if (start <= last.second) {  // Closed-interval convention.
            last.second = std::max(last.second, end);
        } else {
            merged.push_back({start, end});
        }
    }
    return merged;
}
```

**Invariant:** `merged` contains the fully merged union of all processed intervals, in sorted disjoint order.

**Complexity:** `O(n log n)` time due to sorting and `O(n)` output. Auxiliary space is sorting/language dependent, excluding output.

**When to use / recognition:** Combine bookings/ranges, compute union, remove redundant covered pieces, or normalize ranges.

**Why `max(end)` matters:** When `[1, 10]` contains `[2, 3]`, assigning end to `3` would shrink the union incorrectly.

**Alternatives/trade-offs:** If intervals arrive already sorted, scan in `O(n)`. For small bounded integer coordinates, a difference array can represent coverage. For online insert/query, an ordered structure may be needed.

**Common mistakes:** Forgetting to sort; sorting by end; using the wrong `<`/`<=`; shrinking on containment; mutating caller-owned intervals unexpectedly; and returning references into a local or later-mutated container. Passing the input by value above makes the sorting mutation explicit and local.

### 17.3 Insert an Interval

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** It tests whether a candidate can exploit an existing sorted, non-overlapping invariant instead of re-sorting unnecessarily.

For sorted disjoint intervals:

1. Append intervals strictly before the new one.
2. Merge all overlapping intervals into the new range.
3. Append everything strictly after it.

```cpp
std::vector<Interval> insertInterval(const std::vector<Interval>& intervals,
                                     Interval new_interval) {
    std::vector<Interval> result;
    result.reserve(intervals.size() + 1);
    std::size_t i = 0;
    auto [start, end] = new_interval;

    while (i < intervals.size() && intervals[i].second < start) {
        result.push_back(intervals[i++]);
    }

    while (i < intervals.size() && intervals[i].first <= end) {
        start = std::min(start, intervals[i].first);
        end = std::max(end, intervals[i].second);
        ++i;
    }
    result.push_back({start, end});

    result.insert(result.end(), intervals.begin() + i, intervals.end());
    return result;
}
```

This code uses closed-overlap semantics. Adjust strictness for half-open rules.

**Complexity:** `O(n)` time and `O(n)` output space; no sort is needed because the precondition provides order.

**Mistakes/edge cases:** Ignoring the sorted/disjoint promise; dropping intervals before/after the merge block; mutating `new_interval`; empty input; new interval before/after all others; and using inconsistent endpoint tests in the three phases.

**Alternative:** Append the new interval and call merge for `O(n log n)`. It is simpler but fails to exploit the useful precondition; mention it as brute force, then optimize.

### 17.4 Interval Intersection

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** It is a clean two-pointer range pattern and a common follow-up when both interval lists are already sorted/disjoint.

```cpp
std::vector<Interval> intervalIntersections(
    const std::vector<Interval>& first,
    const std::vector<Interval>& second) {
    std::vector<Interval> result;
    std::size_t i = 0;
    std::size_t j = 0;

    while (i < first.size() && j < second.size()) {
        const long long start = std::max(first[i].first, second[j].first);
        const long long end = std::min(first[i].second, second[j].second);
        if (start <= end) result.push_back({start, end});  // Closed intervals.

        // The interval ending first cannot overlap any later part of the other.
        if (first[i].second < second[j].second) {
            ++i;
        } else {
            ++j;
        }
    }
    return result;
}
```

**Complexity:** `O(n + m)` time and `O(1)` auxiliary space excluding output.

**Invariant:** Any future intersection must involve the interval whose end extends farther; discard the one ending first.

**Mistakes:** Advancing by start rather than end; failing to advance both-or-one safely on equal ends; wrong endpoint condition; and using nested loops for sorted lists.

### 17.5 Non-Overlapping Selection and Minimum Removals

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** This is the highest-value interval greedy pattern and contrasts directly with merging.

To maximize the number of mutually compatible intervals, sort by **end** and repeatedly keep the next interval whose start is compatible. To minimize removals, return `n - kept`.

```cpp
int minRemovalsForNonOverlap(std::vector<Interval> intervals) {
    if (intervals.empty()) return 0;
    std::sort(intervals.begin(), intervals.end(),
              [](const Interval& a, const Interval& b) {
                  if (a.second != b.second) return a.second < b.second;
                  return a.first < b.first;
              });

    int kept = 1;
    long long previous_end = intervals.front().second;
    for (std::size_t i = 1; i < intervals.size(); ++i) {
        const auto [start, end] = intervals[i];
        if (start >= previous_end) {  // Touching is allowed here.
            ++kept;
            previous_end = end;
        }
    }
    return static_cast<int>(intervals.size()) - kept;
}
```

**Complexity:** `O(n log n)` time, sorting-dependent auxiliary space.

**Recognition:** Schedule the most jobs/meetings, remove fewest overlaps, choose maximum compatible subset.

**Why not sort by start:** An early-starting interval may end very late and block many short later intervals. Earliest finish leaves the most room.

**Alternative scan:** Sort by start; whenever two intervals overlap, conceptually remove the one with the larger end. This maintains the same earliest-ending survivor.

**Weighted warning:** If intervals have profit/weight and the goal maximizes total weight, use weighted interval scheduling DP with binary search for the previous compatible interval.

### 17.6 Meeting Rooms I: Can One Resource Handle All Intervals?

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** It is the simplest overlap-detection question and establishes the start-sorted neighbor property.

```cpp
bool canAttendAll(std::vector<Interval> meetings) {
    std::sort(meetings.begin(), meetings.end());
    for (std::size_t i = 1; i < meetings.size(); ++i) {
        if (meetings[i].first < meetings[i - 1].second) return false;
    }
    return true;
}
```

Assuming ending and starting at the same time is allowed, use `<`; otherwise use `<=`.

**Complexity:** `O(n log n)` time and sorting-dependent space.

**Why adjacent comparison suffices:** With start order, if the current meeting does not overlap the previous meeting with the relevant ending frontier, it cannot overlap an even earlier interval whose effective occupied end is no later. For a pure conflict check against immediate previous, any nested earlier interval that extends farther would already have conflicted with the previous sequence; alternatively maintain `max_end` for a visibly direct invariant.

### 17.7 Meeting Rooms II: Minimum Concurrent Resources

**Priority:** 🔴 Tier 1 — Must Master

**Why this priority:** This common question connects intervals to both heaps and sweep lines and tests interpretation of maximum concurrency.

#### Min-heap of room end times

Sort meetings by start. The heap contains end times of rooms currently in use. Release all rooms that have ended before the next start, then allocate the meeting.

```cpp
int minMeetingRooms(std::vector<Interval> meetings) {
    std::vector<Interval> valid;
    valid.reserve(meetings.size());
    for (const auto& [start, end] : meetings) {
        if (start > end) {
            throw std::invalid_argument("meeting start must not exceed end");
        }
        if (start < end) valid.push_back({start, end});  // Ignore empty [s, s).
    }
    std::sort(valid.begin(), valid.end());

    std::priority_queue<long long, std::vector<long long>,
                        std::greater<long long>> active_ends;
    int maximum = 0;
    for (const auto& [start, end] : valid) {
        while (!active_ends.empty() && active_ends.top() <= start) {
            active_ends.pop();
        }
        active_ends.push(end);
        maximum = std::max(maximum, static_cast<int>(active_ends.size()));
    }
    return maximum;
}
```

**Invariant:** Before insertion, the heap holds exactly the meetings still active at `start`; after insertion, its size is current concurrency.

**Complexity:** `O(n log n)` time and `O(n)` heap space.

If only the minimum room count is needed and each new meeting can reuse at most one just-freed room in a start-sorted scan, a common compact variant pops at most one then returns final heap size. Popping all ended meetings and tracking `maximum` makes the active-set invariant explicit and generalizes better.

#### Sorted starts and ends

Sort all starts and all ends separately. If next start is before the next end, one more room becomes active; otherwise a room frees first.

```cpp
int minMeetingRoomsTwoArrays(const std::vector<Interval>& meetings) {
    std::vector<long long> starts;
    std::vector<long long> ends;
    starts.reserve(meetings.size());
    ends.reserve(meetings.size());

    for (const auto& [start, end] : meetings) {
        if (start > end) {
            throw std::invalid_argument("meeting start must not exceed end");
        }
        if (start < end) {  // Empty half-open intervals need no room.
            starts.push_back(start);
            ends.push_back(end);
        }
    }
    std::sort(starts.begin(), starts.end());
    std::sort(ends.begin(), ends.end());

    std::size_t start_index = 0;
    std::size_t end_index = 0;
    int active = 0;
    int maximum = 0;
    while (start_index < starts.size()) {
        if (starts[start_index] < ends[end_index]) {
            ++active;
            maximum = std::max(maximum, active);
            ++start_index;
        } else {
            --active;
            ++end_index;
        }
    }
    return maximum;
}
```

**Trade-offs:** The heap can retain room end information and be extended to assign room IDs. Sorted endpoints/sweep line is often simpler when only maximum concurrency is needed.

**Common mistakes/edge cases:** Wrong tie rule; returning final active count rather than maximum in a general sweep; assuming meetings are already sorted; an empty `vector`; zero-length meetings; and popping latest rather than earliest end.

### 17.8 Sweep Line / Event Counting

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority:** Sweep line generalizes meeting concurrency and handles coverage/event aggregation. Basic one-dimensional sweeps are useful; geometric versions are specialized.

Convert each interval into events:

- At a start: `+1` active.
- At an end: `-1` active.

Sort events, update a running count, and track the desired maximum or covered duration.

```cpp
int maximumOverlapHalfOpen(const std::vector<Interval>& intervals) {
    std::vector<std::pair<long long, int>> events;  // coordinate, delta
    events.reserve(2 * intervals.size());
    for (const auto& [start, end] : intervals) {
        if (start > end) {
            throw std::invalid_argument("interval start must not exceed end");
        }
        if (start == end) continue;
        events.push_back({start, +1});
        events.push_back({end, -1});
    }

    // Lexicographic pair ordering processes -1 before +1 at a tie.
    std::sort(events.begin(), events.end());
    int active = 0;
    int maximum = 0;
    for (const auto& [coordinate, delta] : events) {
        (void)coordinate;
        active += delta;
        maximum = std::max(maximum, active);
    }
    return maximum;
}
```

**Tie semantics are algorithmic:**

- Half-open scheduling `[s, e)`: process end before start at the same time.
- Closed overlap `[s, e]`: process start before end at the same coordinate if touching counts concurrently.

For many events at the same coordinate, aggregating their deltas first can make the intended semantics clearer. For covered length, accumulate `(coordinate - previous_coordinate)` using the active count *before* applying events at the new coordinate.

**Complexity:** `O(n log n)` time for sorting `2n` events and `O(n)` event space. If coordinates are small bounded integers, a difference array plus prefix sum can reduce scanning to `O(n + U)` for universe size `U`.

**Recognition:** Maximum simultaneous users/bookings, peak load, time periods covered by at least `k` intervals, skyline-like event changes, or sum of range effects.

**Alternatives/trade-offs:** Heap keeps identities/details of active intervals; sweep events efficiently compute aggregate counts. Coordinate compression helps large sparse coordinates when array-based range aggregation is otherwise useful.

**Common mistakes:** Unspecified tie order; updating maximum before/after the wrong delta; processing identical coordinates individually with inconsistent semantics; forgetting that sorted-event space is `O(n)`; and using a difference array over enormous raw coordinates.

### 17.9 Covering a Range with Intervals

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority:** Farthest-extension greedy appears in video stitching and minimum-tap questions, but is less frequent than merging and meeting rooms.

To cover a target beginning at `0`, among all intervals whose start is at or before the current covered endpoint, choose the one that extends farthest. If no such interval extends coverage, a gap makes coverage impossible.

**Invariant:** After each selection count, the greedy method reaches at least as far as any solution using the same number of intervals, because it considers every currently eligible interval and chooses maximum end.

**Complexity:** With intervals sorted by start, `O(n log n)` time and sorting-dependent space; bounded integer starts sometimes allow a Jump-Game-style `O(n + U)` preprocessing/scan.

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

#### Beginner

- **Merge Intervals:** Teaches start sorting, containment, and maintaining the last merged range.
- **Meeting Rooms:** Teaches overlap semantics and adjacent checks after sorting.
- **Interval List Intersections:** Teaches two pointers and advancing the interval with earlier end.

#### Core Interview

- **Insert Interval:** Teaches exploiting sorted/disjoint input for a three-phase `O(n)` scan.
- **Non-overlapping Intervals:** Teaches earliest-finish greedy and minimum-removal conversion.
- **Meeting Rooms II:** Teaches maximum concurrency through a heap or sweep line.
- **Minimum Number of Arrows to Burst Balloons:** Teaches choosing a point at the earliest interval end to cover as many overlapping intervals as safely possible.
- **Employee Free Time:** Teaches merging across multiple schedules, potentially via flatten-sort or k-way heap merge.
- **My Calendar I:** Teaches dynamic conflict checks and raises the trade-off between a simple sorted `vector` and an ordered `map`/tree.

#### Advanced

- **Video Stitching / Minimum Taps to Water a Garden:** Teaches farthest-extension coverage greedy.
- **Maximum Profit in Job Scheduling:** Teaches weighted interval scheduling with sort + binary search + DP; this is a deliberate non-greedy contrast.
- **The Skyline Problem:** Teaches event sorting, active-height multiset/heap, lazy deletion, and careful ties.
- **Range Module / dynamic interval union:** Teaches ordered interval maintenance; implementation details depend strongly on available ordered-map libraries.

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
| Unbounded knapsack | 🟡 Tier 3 — Nice to Know | Useful for coin and reuse-allowed problems, but less frequent than the 0/1 form. | Standard forms |
| Interval DP | 🟡 Tier 3 — Nice to Know | Appears in harder interviews; the gap/length ordering is worth recognizing. | Concept plus one or two problems |
| DP on trees | 🟡 Tier 3 — Nice to Know | Useful in advanced tree interviews but ordinary DFS questions are much more common. | Basic two-state forms |
| Bitmask, digit, profile, and high-dimensional DP | ⚪ Tier 4 — Low Priority / Specialized | Rare in general SWE interviews and expensive to master. | Awareness unless role-specific |

### Focus First

1. Write a sentence that defines each state exactly.
2. Derive transitions from the choices available at that state.
3. Identify base cases and a valid evaluation order.
4. Convert a small recursive solution into memoization and tabulation.
5. Master 1D take/skip, grid, 0/1 subset, LCS, and LIS patterns.
6. Compute complexity as **number of reachable states × work per state**.

### Learn Later

- Reconstructing an actual optimal choice, not just its value.
- Two-row and one-row space compression.
- Unbounded knapsack and change-counting variations.
- Interval DP and simple DP on trees.

### Optional / Specialized

- Bitmask DP, digit DP, profile DP, rerooting DP, convex-hull optimization, and other recurrence optimizations.
- DP with more than two or three independent dimensions unless the target company is known for algorithm-heavy interviews.

### 18.1 Core Intuition: Replace a Repeated Search with a State Graph

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** Seeing repeated decision states is the foundation of interview DP; without this model, memoization becomes mechanical caching.

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

**Why this priority level was assigned:** Recognition determines whether a candidate reaches DP at all and avoids forcing it onto greedy, graph, or window problems.

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

**Why this priority level was assigned:** State, choices, transitions, bases, order, and answer location are the reusable reasoning process behind standard DP questions.

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

**Why this priority level was assigned:** Interviewers expect a candidate to justify that the recurrence covers every legal choice, not merely present a familiar table.

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

**Why this priority level was assigned:** These are the standard execution strategies and a frequent optimization follow-up.

#### Memoization (top-down)

**Priority:** 🟠 Tier 2 — Very Important

Memoization preserves the recursive reasoning and caches each state on first evaluation.

```cpp
// Schematic C++17 template: State must have equality and a Hash function.
template <class State, class Value, class Hash, class IsBase,
          class BaseValue, class LegalChoices, class NextState,
          class Contribution, class Combine>
Value solveWithMemo(const State& initial_state, Hash hash,
                    IsBase is_base, BaseValue base_value,
                    LegalChoices legal_choices, NextState next_state,
                    Contribution contribution, Combine combine) {
    std::unordered_map<State, Value, Hash> memo(0, hash);

    std::function<Value(const State&)> dp = [&](const State& state) -> Value {
        if (is_base(state)) return base_value(state);
        if (const auto found = memo.find(state); found != memo.end()) {
            return found->second;
        }

        std::vector<Value> candidates;
        for (const auto& choice : legal_choices(state)) {
            candidates.push_back(contribution(choice) +
                                 dp(next_state(state, choice)));
        }
        const Value answer = combine(candidates);
        memo.emplace(state, answer);
        return answer;
    };

    return dp(initial_state);
}
```

- **Use when:** The recurrence is easy to express recursively, only a subset of possible states is reachable, or iteration order is awkward.
- **Time:** `O(number of reachable states × work per state)`.
- **Space:** Cache plus recursion depth.
- **Common mistakes:** Caching too late, mutating data that participates in a key/hash, omitting a state variable from the key, and forgetting that recursion stack counts as auxiliary space.
- **Trade-off:** Clear and close to brute force, but native stack depth and function-call overhead can matter.

#### Tabulation (bottom-up)

**Priority:** 🟠 Tier 2 — Very Important

Tabulation explicitly fills states in a dependency-safe order.

```cpp
// Generic bottom-up shape for integer-indexed states. The callables encode
// the problem-specific base cases, order, choices, transition, and objective.
template <class Data, class MakeTable, class StateOrder, class LegalChoices,
          class NextState, class Contribution, class Combine>
auto solveWithTable(const Data& data, MakeTable make_table,
                    StateOrder state_order, LegalChoices legal_choices,
                    NextState next_state, Contribution contribution,
                    Combine combine) {
    auto dp = make_table(data);
    using Value = typename decltype(dp)::value_type;

    for (int state : state_order(data)) {
        std::vector<Value> candidates;
        for (const auto& choice : legal_choices(data, state)) {
            candidates.push_back(contribution(choice) +
                                 dp[next_state(state, choice)]);
        }
        dp[state] = combine(candidates);
    }
    return dp;
}
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

**Why this priority level was assigned:** Deriving each stage from the previous one builds transferable problem-solving ability instead of solution memorization.

Use this systematic conversion path: **Brute Force → Recursion → Memoization → Tabulation → Optimization**.

#### Problem: maximum sum with no adjacent choices

Given non-negative values in `nums`, choose elements with no two adjacent and maximize their sum. The classic story is “House Robber,” but the transferable pattern is **take or skip under a local conflict**.

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

```cpp
long long robRecursive(const std::vector<long long>& nums) {
    std::function<long long(int)> best = [&](int index) -> long long {
        if (index >= static_cast<int>(nums.size())) return 0;
        const long long skip = best(index + 1);
        const long long take = nums[index] + best(index + 2);
        return std::max(skip, take);
    };
    return best(0);
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

```cpp
long long robMemo(const std::vector<long long>& nums) {
    // -1 is safe here because the problem states that values are non-negative.
    std::vector<long long> memo(nums.size(), -1);
    std::function<long long(int)> best = [&](int index) -> long long {
        if (index >= static_cast<int>(nums.size())) return 0;
        if (memo[index] != -1) return memo[index];
        return memo[index] = std::max(best(index + 1),
                                      nums[index] + best(index + 2));
    };
    return best(0);
}
```

- **States:** `n` meaningful indices.
- **Work per state:** `O(1)`.
- **Time:** `O(n)`.
- **Space:** `O(n)` cache plus `O(n)` recursion stack, still `O(n)` total.

#### Stage 4 — Reverse the recurrence into tabulation

Define `dp[i]` as the maximum sum using the first `i` elements. Then:

```text
dp[i] = max(
    dp[i - 1],                 # skip element i - 1
    dp[i - 2] + nums[i - 1]    # take it
)
```

```cpp
long long robTable(const std::vector<long long>& nums) {
    const int n = static_cast<int>(nums.size());
    std::vector<long long> dp(n + 1, 0);
    if (n >= 1) dp[1] = nums[0];

    for (int i = 2; i <= n; ++i) {
        dp[i] = std::max(dp[i - 1], dp[i - 2] + nums[i - 1]);
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

```cpp
long long rob(const std::vector<long long>& nums) {
    long long two_back = 0;
    long long one_back = 0;
    for (long long value : nums) {
        const long long current = std::max(one_back, two_back + value);
        two_back = one_back;
        one_back = current;
    }
    return one_back;
}
```

- **Time:** `O(n)`.
- **Auxiliary space:** `O(1)`.
- **Trade-off:** This version returns the optimal value but no longer preserves enough information to reconstruct which indices were selected.

#### What should transfer to a new problem

Do not memorize `two_back` and `one_back`. Retain this reasoning chain:

1. What choices partition all valid solutions?
2. What smaller state follows each choice?
3. Which states repeat?
4. In what order can dependencies be evaluated?
5. Which old states remain necessary after each update?

### 18.7 Complexity Analysis for DP

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** DP tables can hide large state spaces, so counting states and transition work is part of a complete interview solution.

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

**Why this priority level was assigned:** Many interview DPs collapse to one index plus a small amount of state. They teach recurrence design without the bookkeeping of a matrix and are the best bridge from recursion to DP.

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

```cpp
long long minCostClimbingStairs(const std::vector<int>& cost) {
    // Starting before step 0 or step 1 costs nothing.
    long long two_back = 0;
    long long one_back = 0;

    for (int step_cost : cost) {
        const long long current = step_cost + std::min(two_back, one_back);
        two_back = one_back;
        one_back = current;
    }
    // The top can be reached from either of the final two steps.
    return std::min(two_back, one_back);
}
```

- **Time:** `O(n)`.
- **Auxiliary space:** `O(1)`; a full table would use `O(n)`.
- **Edge cases:** Empty and one-element inputs depend on the exact problem's definition of legal starting positions.
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

- Returning `dp.back()` when the answer is `*max_element(dp.begin(), dp.end())`.
- Mixing an “index in the input” state with a “prefix length” state, causing an off-by-one error.
- Initializing a minimum-cost table with zero; unreachable states should normally start at infinity.
- Compressing to variables before checking which old value each variable represents.
- Assuming `O(n)` is automatic: if each `dp[i]` scans all prior indices, time is `O(n²)`.

### 18.9 2D and Grid DP

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** Grid and two-index states are common, visually intuitive, and test whether dependencies, obstacles, boundaries, and space compression are handled correctly.

#### Intuition and recognition

A 2D table is useful when two independent coordinates describe a subproblem:

- Current row and column in a grid.
- Prefix length from each of two strings.
- Item index and remaining capacity.
- Two positions or two boundaries.

For a grid with moves only right and down, cells form a DAG. A cell depends on the cell above and to the left; row-major order is therefore safe.

#### Example: minimum path sum

```cpp
long long minPathSum(const std::vector<std::vector<int>>& grid) {
    if (grid.empty() || grid.front().empty()) return 0;
    const int rows = static_cast<int>(grid.size());
    const int cols = static_cast<int>(grid.front().size());
    const long long INF = std::numeric_limits<long long>::max() / 4;
    std::vector<long long> dp(cols, INF);
    dp[0] = 0;

    for (int row = 0; row < rows; ++row) {
        for (int col = 0; col < cols; ++col) {
            const long long from_above = dp[col];
            const long long from_left = col > 0 ? dp[col - 1] : INF;
            dp[col] = grid[row][col] + std::min(from_above, from_left);
        }
    }
    return dp.back();
}
```

- **State:** During row `r`, `dp[c]` becomes the minimum cost to reach `(r, c)`.
- **Time:** `O(rows × cols)`.
- **Auxiliary space:** `O(cols)` rather than `O(rows × cols)`.
- **Why one row works:** Before update, `dp[c]` is the value from above; after updating `c - 1`, `dp[c - 1]` is the value from the left.
- **Edge cases:** Empty grid, one row, one column, obstacles, and whether start/end cost counts.

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

**Why this priority level was assigned:** The 0/1 take-or-skip recurrence underlies many subset and resource problems; its unbounded variant is lower priority.

#### 0/1 knapsack

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** The exact backpack story is not always common, but its take/skip-with-capacity recurrence underlies subset sum, partition, target feasibility, and many resource-allocation questions.

Each item can be selected at most once. With weight `w` and value `v`:

```text
best(i, cap) = max(
    best(i + 1, cap),                    # skip item i
    v[i] + best(i + 1, cap - w[i])       # take if it fits
)
```

The direct table has `n × capacity` states and `O(1)` work per state.

##### One-dimensional implementation

```cpp
long long knapsack01(const std::vector<int>& weights,
                     const std::vector<long long>& values, int capacity) {
    if (weights.size() != values.size() || capacity < 0) {
        throw std::invalid_argument("invalid knapsack input");
    }
    std::vector<long long> dp(capacity + 1, 0);

    for (std::size_t item = 0; item < weights.size(); ++item) {
        const int weight = weights[item];
        if (weight < 0) throw std::invalid_argument("weights must be nonnegative");
        // Descending order prevents this item from being reused.
        for (int cap = capacity; cap >= weight; --cap) {
            dp[cap] = std::max(dp[cap], dp[cap - weight] + values[item]);
        }
    }
    return dp[capacity];
}
```

- **State:** `dp[cap]` is the best value for capacity at most `cap` after processed items.
- **Time:** `O(n × capacity)`.
- **Auxiliary space:** `O(capacity)`.
- **Critical detail:** Capacity moves downward. Moving upward would read a value already updated by the current item and accidentally allow unlimited copies.

##### Boolean subset sum

```cpp
bool canMakeSum(const std::vector<int>& nums, int target) {
    if (target < 0) return false;
    std::vector<char> possible(target + 1, false);
    possible[0] = true;

    for (int value : nums) {
        if (value < 0) {
            throw std::invalid_argument("this DP requires nonnegative values");
        }
        for (int total = target; total >= value; --total) {
            possible[total] = possible[total] || possible[total - value];
        }
    }
    return possible[target];
}
```

- **Time:** `O(n × target)`.
- **Space:** `O(target)`.
- **Edge cases:** Target zero is normally feasible via the empty subset; zero-valued items need care in counting versions; negative values invalidate this simple index-by-sum model.

#### Unbounded knapsack

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** Reuse-allowed decisions appear in coin change and cutting problems, but this family is less frequent than core array/tree/graph patterns and 0/1 DP.

Each item may be selected repeatedly. The one-dimensional capacity loop usually moves **upward**, allowing the current item’s newly updated state to be reused:

```cpp
int minCoins(const std::vector<int>& coins, int amount) {
    if (amount < 0) return -1;
    const int INF = amount + 1;  // Any feasible answer uses at most amount 1-coins.
    std::vector<int> dp(amount + 1, INF);
    dp[0] = 0;

    for (int coin : coins) {
        if (coin <= 0) throw std::invalid_argument("coin values must be positive");
        for (int total = coin; total <= amount; ++total) {
            dp[total] = std::min(dp[total], dp[total - coin] + 1);
        }
    }
    return dp[amount] == INF ? -1 : dp[amount];
}
```

- **Time:** `O(number_of_coins × amount)`.
- **Space:** `O(amount)`.

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
- **Alternative:** An `unordered_set<int>` of reachable sums can be clearer for sparse targets, though worst-case state count can still be large.
- **Meet-in-the-middle:** May be better when item count is around 30–40 but values/target are huge.
- **Greedy warning:** Choosing the largest value/weight ratio solves fractional knapsack, not general 0/1 knapsack. Standard coin systems can hide the fact that greedy coin choice is not universally correct.
- **Pseudo-polynomial warning:** Check the numeric capacity before proposing `O(n × capacity)`.

### 18.11 Subsequence DP

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** Matching prefixes and building best subsequences are recurring interview patterns. LCS and LIS supply reusable state models without requiring every specialized string algorithm.

#### Subsequence versus substring/subarray

- A **subsequence** preserves order but may skip elements.
- A **substring/subarray** must be contiguous.

This distinction changes the approach. Sliding window often helps contiguous segments; subsequence questions frequently need DP, greedy reasoning, or binary search.

#### Longest Common Subsequence (LCS): prefix-versus-prefix

Define `dp[i][j]` as the LCS length of prefixes `a[0..i)` and `b[0..j)`.

```cpp
int lcsLength(const std::string& a, const std::string& b) {
    // Put the shorter string on the columns to minimize auxiliary space.
    const std::string* rows = &a;
    const std::string* cols = &b;
    if (cols->size() > rows->size()) std::swap(rows, cols);

    std::vector<int> previous(cols->size() + 1, 0);
    for (std::size_t i = 1; i <= rows->size(); ++i) {
        std::vector<int> current(cols->size() + 1, 0);
        for (std::size_t j = 1; j <= cols->size(); ++j) {
            if ((*rows)[i - 1] == (*cols)[j - 1]) {
                current[j] = previous[j - 1] + 1;
            } else {
                current[j] = std::max(previous[j], current[j - 1]);
            }
        }
        previous.swap(current);
    }
    return previous.back();
}
```

- **Transition:** Matching final characters can extend a smaller match; otherwise at least one final character is excluded.
- **Time:** `O(mn)`, where `m = a.size()` and `n = b.size()`.
- **Space:** `O(min(m, n))` because the shorter string is placed on the columns.
- **Trade-off:** Two-row compression returns only the length. Reconstructing a sequence is easiest with the full `O(mn)` table or a more advanced reconstruction method.
- **Common mistake:** Using the matching branch plus the nonmatching maximum at the same time and thereby double-counting.

LCS modeling also appears in edit distance, deletions needed to equalize strings, and sequence alignment. The transition differs, but the index-pair state is shared.

#### Longest Increasing Subsequence (LIS): best ending at each index

Define `dp[i]` as the LIS length ending exactly at index `i`:

```cpp
int lisLengthQuadratic(const std::vector<int>& nums) {
    if (nums.empty()) return 0;
    std::vector<int> dp(nums.size(), 1);
    for (int i = 0; i < static_cast<int>(nums.size()); ++i) {
        for (int j = 0; j < i; ++j) {
            if (nums[j] < nums[i]) {
                dp[i] = std::max(dp[i], dp[j] + 1);
            }
        }
    }
    return *std::max_element(dp.begin(), dp.end());
}
```

- **Time:** `O(n²)`.
- **Space:** `O(n)`.
- **Why answer is `max(dp)`:** The optimal subsequence may end before the last input element.
- **Edge case:** Replace `<` with `<=` only if the definition asks for non-decreasing rather than strictly increasing.

The advanced `O(n log n)` method stores the smallest possible tail for each length and uses lower-bound binary search. It is excellent to know after the `O(n²)` state is understood, but the `tails` array is not itself necessarily an actual LIS.

```cpp
int lisLength(const std::vector<int>& nums) {
    std::vector<int> tails;
    for (int value : nums) {
        auto position = std::lower_bound(tails.begin(), tails.end(), value);
        if (position == tails.end()) {
            tails.push_back(value);
        } else {
            *position = value;
        }
    }
    return static_cast<int>(tails.size());
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

**Why this priority level was assigned:** Adding only future-relevant state and safely removing dead dimensions are common tests of whether the recurrence is understood.

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

**Why this priority level was assigned:** Interval DP is a recognizable pattern in difficult interviews, but it appears far less often than linear, grid, knapsack, or subsequence DP. Learn the state shape and evaluation order; do not make it an early priority.

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

In the “Burst Balloons” model, choosing the first balloon is awkward because its neighbors change. Choose the **last** balloon burst inside an interval; then its two outside neighbors are fixed and the left/right subintervals are independent.

```cpp
long long maxCoins(const std::vector<int>& nums) {
    std::vector<long long> values;
    values.reserve(nums.size() + 2);
    values.push_back(1);
    values.insert(values.end(), nums.begin(), nums.end());
    values.push_back(1);

    const int n = static_cast<int>(values.size());
    // dp[left][right] covers the open interval (left, right).
    std::vector<std::vector<long long>> dp(
        n, std::vector<long long>(n, 0));

    for (int width = 2; width < n; ++width) {
        for (int left = 0; left + width < n; ++left) {
            const int right = left + width;
            for (int last = left + 1; last < right; ++last) {
                const long long gain =
                    values[left] * values[last] * values[right];
                dp[left][right] = std::max(
                    dp[left][right],
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

**Why this priority level was assigned:** Simple tree DP elegantly handles parent-child choice conflicts, but it is much less common than ordinary tree DFS/BFS. It is best studied after postorder traversal and core DP are comfortable.

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

```cpp
long long maxNonAdjacentTreeSum(const TreeNode* root) {
    std::function<std::pair<long long, long long>(const TreeNode*)> dfs =
        [&](const TreeNode* node) -> std::pair<long long, long long> {
        if (node == nullptr) return {0, 0};  // skip, take

        const auto [left_skip, left_take] = dfs(node->left);
        const auto [right_skip, right_take] = dfs(node->right);
        const long long take = static_cast<long long>(node->val) +
                               left_skip + right_skip;
        const long long skip = std::max(left_skip, left_take) +
                               std::max(right_skip, right_take);
        return {skip, take};
    };

    const auto [skip_root, take_root] = dfs(root);
    return std::max(skip_root, take_root);
}
```

- **Time:** `O(n)` because each node is processed once.
- **Space:** `O(h)` recursion stack, where `h` is tree height; worst-case `O(n)`.
- **Correctness intuition:** Taking a node forces every child into its skip state. Skipping it lets each child independently choose its better state.
- **Edge cases:** Empty tree, negative values (is choosing nothing allowed?), and a skewed tree that can overflow the call stack.
- **Alternative:** An `unordered_map` keyed by `(node pointer, parent_taken)` works but adds hashing/state overhead and usually communicates the postorder idea less cleanly.

More advanced forms—rerooting, many states per node, or DP on arbitrary tree decompositions—are **⚪ Tier 4 — Low Priority / Specialized** for general SWE interviews.

### 18.15 Value, Feasibility, Counting, and Reconstruction

**Priority:** 🟠 Tier 2 — Very Important

**Why this priority level was assigned:** Changing the objective changes identities and aggregation even when the state graph is similar; this is a common source of interview bugs.

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

- a `parent[state]` or `choice[state]` pointer while filling the table; or
- the full value table and walk backward by checking which transition could have produced the current value.

Reconstruction usually adds `O(number of states)` storage even when the value-only DP could be compressed. Say this trade-off explicitly.

#### Counting safely

- Determine whether order matters: `[1, 2]` versus `[2, 1]` may be one combination or two sequences.
- Determine whether duplicates are distinct items.
- If the problem requests modulo arithmetic, apply the modulus during transitions.
- In fixed-width languages, counts may overflow even when the final input size looks modest.

### 18.16 Representative DP Problems

These are representative **problem types**, not a checklist to memorize. Equivalent problems from LeetCode, NeetCode, HackerRank, CodeSignal, books, or mock interviews teach the same patterns.

#### Beginner

| Problem type / well-known example | Pattern it teaches | Why it is worth solving | What to learn |
|---|---|---|---|
| Climbing Stairs | Count ways in 1D | Clean recurrence with tiny state | State meaning, base cases, Fibonacci-like compression |
| Min Cost Climbing Stairs | Minimum-cost 1D DP | Separates cost to reach a state from cost of leaving it | Terminal-state handling and `min` aggregation |
| House Robber | Take/skip | Canonical local-conflict recurrence | Derive recursion, memoize, tabulate, compress |
| Unique Paths | Grid counting | Makes dependency order visible | Empty-border initialization and row compression |
| Maximum Subarray | Best ending at index | Shows that not every DP needs a table | Decide between extending and restarting; compare with prefix-sum thinking |

#### Core Interview

| Problem type / well-known example | Pattern it teaches | Why it is worth solving | What to learn |
|---|---|---|---|
| Coin Change (minimum coins) | Unbounded minimum DP | Tests impossible-state initialization | Distinguish `+∞` from zero and verify target feasibility |
| Coin Change II | Count unordered combinations | Same ingredients, different loop meaning | Why loop order affects counting |
| Partition Equal Subset Sum | 0/1 subset feasibility | Turns a partition question into target sum | Descending updates and pseudo-polynomial limits |
| Word Break | Prefix segmentation | A 1D state with variable-length predecessors | State for prefix `s[0..i)`, `unordered_set` lookup, and practical pruning by word length |
| Decode Ways | Conditional count DP | Base cases and validity dominate the problem | Handle zero and two-character boundaries precisely |
| Longest Increasing Subsequence | Best ending plus optimized tails | Offers a standard optimization follow-up | `O(n²)` DP first; then explain `O(n log n)` tails invariant |
| Longest Common Subsequence | Prefix pair | Foundational two-sequence recurrence | Empty-prefix row/column and match/mismatch cases |
| Edit Distance | Prefix pair with three operations | Forces precise transition semantics | Insert/delete/replace as movement in the table |
| Target Sum | Counting assignments | Shows transformation and alternative state choices | Offset sums or reduction to subset counting; inspect negatives and parity |
| Stock with Cooldown | Small state machine DP | Demonstrates extra state without giant tables | Define holding/sold/rest states and legal transitions |

#### Advanced

| Problem type / well-known example | Pattern it teaches | Why it is worth solving after the core | What to learn |
|---|---|---|---|
| Distinct Subsequences | Two-sequence counting | Tests counting bases and overflow | Match creates use/skip branches |
| Longest Palindromic Subsequence | Endpoint interval DP | Introduces interval length order gently | Endpoint equality and inner interval dependencies |
| Burst Balloons | Interval split / choose last | A strong test of reframing decisions | Fixed boundaries, `O(n³)` split recurrence |
| House Robber III | Tree include/exclude | Connects DFS summaries to DP state | Return multiple subtree states |
| Regular Expression Matching | Boolean two-index DP | Many edge cases and transitions | Only attempt after core DP; define wildcard semantics rigorously |

#### A productive way to practice each problem

1. State the brute force and estimate its complexity.
2. Draw a small recursion tree and circle repeated argument tuples.
3. Write the state sentence and recurrence without code.
4. Implement memoization.
5. Convert to tabulation only after the recursive version is understood.
6. Explain evaluation order and complexity.
7. Re-solve later from a blank editor; change a constraint and predict how the state changes.

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
- **Why it exists:** An `unordered_set<string>` answers “is this complete word present?” well, but does not naturally answer “does any stored word begin with this prefix?” A trie supports both queries incrementally.
- **Why it matters in interviews:** Tries appear in autocomplete, dictionary, wildcard, prefix-replacement, and board word-search problems. They also test whether a candidate can combine a data structure with DFS/backtracking.
- **Interview priority:** **🟡 Tier 3 — Nice to Know.** Tries are useful and recognizable but much less frequent than hashing, trees, graphs, and core sequence patterns. Implement the standard form and understand trie-guided pruning; do not overinvest in compressed or persistent variants.
- **Prerequisites:** Hash maps or fixed arrays, strings, tree traversal, recursion, and backtracking for board search.
- **Common use cases:** Exact lookup, prefix existence, autocomplete candidates, longest matching prefix, wildcard dictionary search, and pruning many simultaneous string searches.
- **Common problem patterns:** Implement a dictionary, replace words by roots, search with `.` wildcards, find several words in a grid, or compute prefix scores.
- **How to recognize it:** Many strings are queried by prefix, searches proceed character-by-character, or a brute-force search repeats the same prefix checks across a large dictionary.
- **How deeply to understand it:** Implement insert, exact search, and prefix search; explain child-storage trade-offs; augment nodes with useful metadata; and combine a trie with backtracking for multiword search.

### Why This Priority Was Assigned

An `unordered_set<string>` is simpler for exact-word membership, and a sorted `vector<string>` plus binary search can answer some offline prefix queries. A trie earns its space cost only when prefixes are first-class operations or when shared-prefix pruning saves repeated work. This narrower applicability makes it Tier 3 for general SWE interviews.

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

**Why this priority level was assigned:** Shared-prefix paths and terminal markers are the essential trie model, though tries are narrower than core hash/tree patterns.

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

**Why this priority level was assigned:** A basic implementation is worth reconstructing for prefix questions, but sophisticated variants have low general-interview return.

```cpp
struct TrieNode {
    std::unordered_map<char, std::unique_ptr<TrieNode>> children;
    bool is_word = false;
    std::optional<std::string> word;  // Optional terminal payload.
};

class Trie {
    TrieNode root_;

    const TrieNode* findNode(const std::string& text) const {
        const TrieNode* node = &root_;
        for (char ch : text) {
            const auto found = node->children.find(ch);
            if (found == node->children.end()) return nullptr;
            node = found->second.get();
        }
        return node;
    }

public:
    void insert(const std::string& word) {
        TrieNode* node = &root_;
        for (char ch : word) {
            auto& child = node->children[ch];
            if (!child) child = std::make_unique<TrieNode>();
            node = child.get();
        }
        node->is_word = true;
    }

    bool search(const std::string& word) const {
        const TrieNode* node = findNode(word);
        return node != nullptr && node->is_word;
    }

    bool startsWith(const std::string& prefix) const {
        return findNode(prefix) != nullptr;
    }

    const TrieNode& root() const { return root_; }
};
```

#### What changes from problem to problem

- The alphabet and child representation.
- Terminal metadata: `is_word`, count, index, score, or stored full word.
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

#### Child-storage trade-offs

| Representation | Prefer when | Advantages | Costs |
|---|---|---|---|
| `unordered_map` | Alphabet is large or nodes are sparse | Simple and memory proportional to actual children | Hash/allocation overhead; expected rather than strict constant lookup |
| Fixed array of size 26 | Alphabet is known, small, and dense | Fast direct indexing and predictable behavior | Often wastes many child slots |
| Sorted `vector<pair<char, child>>` | Nodes have very few children and memory matters | Compact | Search may be linear or require binary search |

For interview code, `unordered_map<char, unique_ptr<TrieNode>>` children are usually the clearest unless the prompt fixes lowercase English letters and emphasizes the lower constant factor of an `array<unique_ptr<TrieNode>, 26>`.

### 19.3 Prefix Search, Counts, and Deletion

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** Prefix queries and small node augmentations are useful standard extensions; deletion is secondary.

#### Prefix search

`startsWith(prefix)` succeeds as soon as every prefix character is consumed; it does not require `is_word` at the final node. Exact search does.

To list completions, first locate the prefix node, then DFS below it. If there may be many results, output size dominates runtime; returning `k` characters of results cannot be faster than `Ω(k)`.

#### Useful node augmentations

- `pass_count`: how many inserted words pass through the node.
- `end_count`: how many copies terminate at the node.
- `word`: store the full word at terminal nodes for easy board-search output.
- `top_suggestions`: cached ranked completions for an autocomplete system.

Augmentation improves a particular query but adds update and memory costs. Store only metadata the problem requests.

#### Deletion

To delete safely:

1. Walk the path and confirm the complete word exists.
2. Clear or decrement its terminal marker/count.
3. Moving backward, remove a node only if it has no children, is not terminal for another word, and has no remaining pass count.

Simply deleting every node on the path would corrupt shared prefixes such as deleting `app` when `apple` remains.

### 19.4 Wildcard Search

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** Wildcards test trie-guided branching but appear less often than exact and prefix search.

If `.` matches any one character, a normal character follows one child while `.` branches over every child.

```cpp
bool wildcardSearch(const TrieNode& root, const std::string& pattern) {
    std::function<bool(const TrieNode*, std::size_t)> dfs =
        [&](const TrieNode* node, std::size_t index) {
        if (index == pattern.size()) return node->is_word;

        const char ch = pattern[index];
        if (ch != '.') {
            const auto found = node->children.find(ch);
            return found != node->children.end() &&
                   dfs(found->second.get(), index + 1);
        }

        for (const auto& [next_char, child] : node->children) {
            (void)next_char;
            if (dfs(child.get(), index + 1)) return true;
        }
        return false;
    };

    return dfs(&root, 0);
}
```

- **Typical time:** `O(L)` without wildcards.
- **Worst-case time:** Exponential in the number of wildcard positions, bounded by nodes reachable at the required depths.
- **Space:** `O(L)` recursion depth.
- **Common mistake:** Returning true for a prefix after the pattern is consumed without checking `is_word`.
- **Alternative:** A regular expression engine or finite automaton may be appropriate in production, but a trie DFS is the expected interview model for a small dictionary API.

### 19.5 Trie + Backtracking for Word Search

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** This is the most valuable combined trie pattern, but it belongs after core grid backtracking and hashing.

#### Why one trie beats one search per word

If the task asks whether one word exists in a board, ordinary DFS/backtracking is enough. If it asks for **all dictionary words**, searching once per word repeats exploration for shared prefixes. A trie lets one board traversal pursue all words with the current prefix and stop immediately when no dictionary word can continue.

```cpp
std::vector<std::string> findWords(
    std::vector<std::vector<char>>& board,
    const std::vector<std::string>& words) {
    if (board.empty() || board.front().empty() || words.empty()) return {};

    TrieNode root;
    for (const std::string& word : words) {
        TrieNode* node = &root;
        for (char ch : word) {
            auto& child = node->children[ch];
            if (!child) child = std::make_unique<TrieNode>();
            node = child.get();
        }
        node->is_word = true;
        node->word = word;
    }

    const int rows = static_cast<int>(board.size());
    const int cols = static_cast<int>(board.front().size());
    const std::array<std::pair<int, int>, 4> directions{{
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    }};
    std::vector<std::string> found_words;

    std::function<void(int, int, TrieNode*)> dfs =
        [&](int row, int col, TrieNode* parent) {
        const char ch = board[row][col];
        const auto child_it = parent->children.find(ch);
        if (child_it == parent->children.end()) return;
        TrieNode* node = child_it->second.get();

        if (node->word.has_value()) {
            found_words.push_back(*node->word);
            node->word.reset();       // Suppress duplicate output.
            node->is_word = false;
        }

        board[row][col] = '#';        // Temporary, path-local mutation.
        for (const auto& [dr, dc] : directions) {
            const int next_row = row + dr;
            const int next_col = col + dc;
            if (0 <= next_row && next_row < rows &&
                0 <= next_col && next_col < cols &&
                board[next_row][next_col] != '#') {
                dfs(next_row, next_col, node);
            }
        }
        board[row][col] = ch;

        // Optional pruning; use only when this trie is disposable.
        if (node->children.empty() && !node->is_word) {
            parent->children.erase(ch);
        }
    };

    for (int row = 0; row < rows; ++row) {
        for (int col = 0; col < cols; ++col) {
            dfs(row, col, &root);
        }
    }
    return found_words;
}
```

#### Complexity and trade-offs

- Building the trie takes `O(S)` time and space for `S` total dictionary characters.
- Let `B = rows × cols` and `L` be the longest word. A loose board-search bound is `O(B × 4 × 3^(L-1))`: four first moves and at most three thereafter because the current path cannot immediately reuse the prior cell. Trie prefix failures and pruning often reduce actual work substantially.
- Recursion path space is `O(L)`; temporarily modifying the board avoids a separate `vector<vector<bool>> visited`.
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

**Why this priority level was assigned:** Knowing when an `unordered_set` or sorted `vector` is simpler prevents paying the trie's substantial memory cost unnecessarily.

| Need | Usually prefer | Why |
|---|---|---|
| Exact membership only | `unordered_set<string>` | Simpler, compact, expected `O(L)` hashing/lookup |
| Offline prefix range in sorted words | Sorted `vector<string>` + binary search | Avoids node overhead; contiguous lexical range |
| One word in a board | Plain backtracking | No benefit from shared dictionary prefixes |
| Many words or repeated prefix queries | Trie | Shares prefix work |
| Many patterns searched inside one long text | KMP/Aho–Corasick depending count | Search direction and workload differ |
| Space-constrained static dictionary | Compressed representation | Ordinary trie object overhead may be large |

A trie’s asymptotic lookup is `O(L)`, just like hashing a length-`L` string. Its advantage is prefix navigation and shared structure, not a magical sublinear exact lookup.

### 19.7 Representative Trie Problems

#### Beginner

| Problem type / well-known example | Pattern it teaches | Why solve it | Lesson to retain |
|---|---|---|---|
| Implement Trie / Prefix Tree | Core operations | Establishes the path invariant | Terminal marker versus prefix node |
| Longest Common Prefix via trie or direct scan | Prefix traversal | Encourages comparison with simpler alternatives | A trie is valid but often unnecessary for one batch |

#### Core Interview

| Problem type / well-known example | Pattern it teaches | Why solve it | Lesson to retain |
|---|---|---|---|
| Replace Words | Shortest stored prefix | Simple trie query embedded in text processing | Stop at the first terminal prefix |
| Design Add and Search Words | Wildcard branching | Combines trie nodes with DFS | Fixed character follows one edge; wildcard explores all |
| Word Search II | Trie-guided backtracking | Canonical multi-pattern pruning problem | Share prefixes, restore board, deduplicate output |

#### Advanced

| Problem type / well-known example | Pattern it teaches | Why solve it later | Lesson to retain |
|---|---|---|---|
| Autocomplete system | Metadata and ranking | Adds system-design trade-offs | Cache versus update cost and result-size complexity |
| Maximum XOR pair using a binary trie | Bitwise trie | Specialized but elegant | Prefer the opposite bit greedily at each position |
| Stream of characters matching suffix words | Reverse trie / automaton thinking | Changes query direction | Data structure orientation should match query flow |

### 19.8 Trie Edge Cases and Interview Tips

- Define behavior for the empty string: inserting it normally marks the root terminal.
- Decide whether input is case-sensitive and what alphabet is valid.
- Duplicate insertion may be idempotent or may increase a count.
- Unicode “character” handling and normalization are production concerns; clarify them rather than silently assuming ASCII.
- Explain memory as total created nodes and child-container overhead, not merely number of words.
- For a board search, state whether diagonals are allowed and whether cells can be reused.
- Start with `unordered_map` children for readable interview code; optimize representation only when constraints justify it.

### 19.9 Trie Mastery Checklist

I have mastered interview-level tries when I can:

- [ ] Explain why a terminal flag is separate from the existence of children.
- [ ] Implement insert, exact search, and prefix search from scratch.
- [ ] Analyze each operation as `O(L)` and total node space as `O(S)` worst case.
- [ ] Compare a trie with an `unordered_set` and sorted `vector` for prefix workloads.
- [ ] Choose `unordered_map` versus fixed-array children and explain the memory trade-off.
- [ ] Add wildcard search using DFS without accepting a prefix as a complete word.
- [ ] Combine a trie with board backtracking and restore visited cells correctly.
- [ ] Explain how prefix pruning helps and why its worst case can still be exponential.
- [ ] Avoid using a trie when exact membership or a single search has a simpler solution.

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

**Why this priority level was assigned:** Static prefix sums cover the common case. Dynamic query structures become necessary only when many updates are interleaved with queries, a relatively uncommon general-interview workload.

Start from the operation workload, not the fanciest structure.

| Workload | Best first consideration | Build | Query | Update |
|---|---|---:|---:|---:|
| Static range sums | Prefix sum | `O(n)` | `O(1)` | Rebuild / `O(n)` |
| Many offline range additions, final values only | Difference array | `O(n + q)` total | Final pass | `O(1)` per recorded update |
| Point updates + prefix/range sums | Fenwick tree | `O(n)` or `O(n log n)` | `O(log n)` | `O(log n)` |
| General associative range query + point updates | Segment tree | `O(n)` | `O(log n)` | `O(log n)` |
| Range updates + range queries | Lazy segment tree | `O(n)` | `O(log n)` | `O(log n)` amortized per operation |

#### Fenwick Tree (Binary Indexed Tree)

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

**Why this priority level was assigned:** Fenwick trees are concise and useful for dynamic prefix aggregates, but ordinary interview problems much more often use static prefix sums or hashing.

A Fenwick tree compactly stores partial aggregates. With one-based indexing, `i & -i` gives the size of the block represented at index `i`.

```cpp
class FenwickTree {
    std::vector<long long> tree_;  // Internal indexing is one-based.

public:
    explicit FenwickTree(int size) {
        if (size < 0) throw std::invalid_argument("size must be nonnegative");
        tree_.assign(static_cast<std::size_t>(size) + 1, 0);
    }

    int size() const { return static_cast<int>(tree_.size()) - 1; }

    void add(int index, long long delta) {
        if (index < 0 || index >= size()) {
            throw std::out_of_range("Fenwick index out of range");
        }
        for (int i = index + 1; i < static_cast<int>(tree_.size());
             i += i & -i) {
            tree_[i] += delta;
        }
    }

    long long prefixSum(int index) const {
        if (index < 0) return 0;
        if (index >= size()) {
            throw std::out_of_range("Fenwick index out of range");
        }
        long long total = 0;
        for (int i = index + 1; i > 0; i -= i & -i) {
            total += tree_[i];
        }
        return total;
    }

    long long rangeSum(int left, int right) const {  // Inclusive [left, right].
        if (left < 0 || left > right || right >= size()) {
            throw std::out_of_range("invalid Fenwick range");
        }
        return prefixSum(right) - prefixSum(left - 1);
    }
};
```

- **Time:** `O(log n)` for point update and prefix/range sum.
- **Space:** `O(n)`.
- **When to use:** Interleaved updates and cumulative/range-sum queries; also frequency tables for order-statistic-style counting after coordinate compression.
- **Common mistakes:** Mixing zero- and one-based indices, calling `i += i & -i` at `i = 0`, and assuming every associative operation has the inverse needed to derive the interval `[left, right]` from two prefix queries.
- **Trade-off:** Smaller and simpler than a segment tree for sums, but less flexible.

#### Segment Tree

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

**Why this priority level was assigned:** Segment trees support broad dynamic range operations, but their implementation cost and low general-SWE frequency make deep study a poor early investment.

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

**Why this priority level was assigned:** Linear pattern matching and rolling hashes occasionally appear as string-focused follow-ups, while most advanced indexes remain rare. Learn the ideas behind KMP and rolling hash, then stop unless the role is string/search heavy.

#### KMP (Knuth–Morris–Pratt)

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** KMP is the standard deterministic linear-time exact-pattern matcher and teaches useful border/prefix reasoning. Exact substring search is not a dominant general interview topic, so it belongs after core patterns.

##### Intuition

When a mismatch occurs after matching part of the pattern, some suffix of what matched may also be a prefix of the pattern. The prefix-function/LPS table tells how far the pattern can shift without rechecking text characters known to match.

`lps[i]` is the length of the longest **proper** prefix of `pattern[0..i+1)` that is also a suffix.

```cpp
int kmpFind(const std::string& text, const std::string& pattern) {
    if (pattern.empty()) return 0;

    std::vector<int> lps(pattern.size(), 0);
    int length = 0;
    for (int i = 1; i < static_cast<int>(pattern.size());) {
        if (pattern[i] == pattern[length]) {
            lps[i++] = ++length;
        } else if (length > 0) {
            length = lps[length - 1];
        } else {
            ++i;
        }
    }

    int text_index = 0;
    int pattern_index = 0;
    while (text_index < static_cast<int>(text.size())) {
        if (text[text_index] == pattern[pattern_index]) {
            ++text_index;
            ++pattern_index;
            if (pattern_index == static_cast<int>(pattern.size())) {
                return text_index - pattern_index;
            }
        } else if (pattern_index > 0) {
            pattern_index = lps[pattern_index - 1];
        } else {
            ++text_index;
        }
    }
    return -1;
}
```

- **Time:** `O(text length + pattern length)`.
- **Space:** `O(pattern length)`.
- **Correctness intuition:** On fallback, KMP retains exactly the longest prefix already known to match the suffix before the mismatch; no possible earlier match start is skipped.
- **Common mistakes:** Treating the whole string as a proper prefix, resetting `j` to zero instead of following failure links, and advancing the text index during a fallback when the current character has not been resolved.
- **Alternatives:** Built-in substring search for practical code, naive `O(nm)` when constraints are small, or rolling hash when comparing many equal-length windows.

#### Rabin–Karp and rolling hash

**Priority:** 🟡 Tier 3 — Nice to Know

**Why this priority level was assigned:** Rolling hash is versatile for repeated substring comparisons and binary-search-on-length problems, but hash collisions complicate a supposedly exact interview solution.

Represent a window as a polynomial hash. When the window shifts, remove the outgoing character’s weighted contribution, multiply/shift, and add the incoming character in `O(1)`.

- **Expected time for single-pattern search:** `O(n + m)` with a good hash and few collisions.
- **Worst-case time:** `O(nm)` if many candidate hashes collide and each is verified.
- **Space:** `O(1)` for one rolling window, or `O(n)` when storing many hashes/prefix hashes.
- **Use when:** Comparing many fixed-length substrings, detecting duplicates, or pairing with binary search on substring length.
- **Correctness requirement:** Equal hashes are candidates, not mathematical proof of equal strings. Verify the substring, use two independent hashes, or explicitly discuss collision risk.
- **Common mistakes:** Incorrect removal power, negative modulo behavior across languages, overflow assumptions, and claiming collision-free `O(n)` without qualification.

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

**Why this priority level was assigned:** Standard traversal, topological sorting, Union-Find, and Dijkstra cover most interview graphs. A few extensions are Tier 3, but the broader advanced graph toolbox is rarely required for general SWE hiring.

Graph BFS/DFS, topological sorting, Union-Find, and Dijkstra belong in the main graph curriculum. The techniques here solve less common variants.

#### Selective priority map

| Technique | Priority | Trigger | Complexity | What to know |
|---|---|---|---:|---|
| Kruskal MST | 🟡 Tier 3 — Nice to Know | Connect all vertices with minimum total edge cost | `O(E log E)` | Sort edges; add one if DSU says it joins components |
| Prim MST | 🟡 Tier 3 — Nice to Know | Same MST goal, grow from a vertex | `O(E log V)` with heap | Cheapest crossing edge; stale heap entries |
| Bellman–Ford | 🟡 Tier 3 — Nice to Know | Negative edges or negative-cycle detection | `O(VE)` | Relax every edge `V-1` times; one more pass detects reachable negative cycle |
| Floyd–Warshall | 🟡 Tier 3 — Nice to Know | Dense, small graph; all-pairs paths | `O(V³)` time, `O(V²)` space | Intermediate-vertex DP: `dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])` |
| 0–1 BFS | 🟡 Tier 3 — Nice to Know | Edge weights only 0 or 1 | `O(V+E)` | `std::deque`: zero-cost edge to front, one-cost edge to back |
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

**Why this priority level was assigned:** Advanced DP families have large, delicate state spaces and are disproportionately common in competitions rather than general SWE interviews. Study them only after standard DP is reliable and target evidence justifies the cost.

#### Bitmask DP

**Priority:** ⚪ Tier 4 — Low Priority / Specialized

**Why this priority level was assigned:** Bitmask DP can turn a factorial search into `O(2^n × poly(n))`, but that remains exponential and usually applies only when `n` is around 15–22. Such constraints are rare in general SWE interviews.

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
- **Common mistakes:** Operator precedence in bit tests, confusing bit position with mask value, allocating an infeasible `2^n` table, and failing to exploit symmetry.

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

**Why this priority level was assigned:** Geometry problems are uncommon in general SWE interviews, have many degeneracies, and often depend on precision conventions. Basic coordinate reasoning is useful; a full geometry toolkit is not a high-return investment.

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
- Overflowing products before storing them in a wider type.
- Mixing screen coordinates (often y increases downward) with Cartesian orientation assumptions.

### 20.7 Selective Representative Problems

Do these only after the core interview curriculum is reliable.

#### Beginner exposure

| Problem type | Technique | What it teaches |
|---|---|---|
| Mutable Range Sum Query | Fenwick tree | One-based low-bit updates and prefix subtraction |
| Implement substring search without a built-in | KMP | LPS fallback invariant |
| Connect Points with Minimum Cost | Prim or Kruskal MST | Distinguish global connection cost from shortest paths |
| Cheapest Flights with a stop limit | Bounded Bellman–Ford / layered state | Why copies/layers prevent using too many edges in one round |

#### Core only for algorithm-heavy targets

| Problem type | Technique | What it teaches |
|---|---|---|
| Range minimum with point updates | Segment tree | Associative combine and neutral element |
| Repeated DNA / duplicate fixed-length substrings | Rolling hash | Window hashing and collision policy |
| Network delay with a negative-edge variant | Bellman–Ford | Relaxation and algorithm-selection constraints |
| Small traveling salesperson / assignment | Bitmask DP | Subset state and feasibility of `2^n` |

#### Advanced specialization

| Problem type | Technique | What it teaches |
|---|---|---|
| Range addition plus range queries | Lazy segment tree | Deferred updates and composition invariants |
| Longest palindromic substring in strict linear time | Manacher | Mirror-radius invariant |
| Critical connections in a network | Bridge-finding DFS | Discovery time and low-link values |
| Convex hull of points | Monotonic-chain geometry | Orientation, sorting, and collinear policy |

### 20.8 Advanced-Topic Study Decision

Before investing deeply, ask:

1. Are all 🔴 Tier 1 — Must Master topics reliable under interview time pressure?
2. Can I solve standard 🟠 Tier 2 — Very Important problems without pattern hints?
3. Does my target company, role, or screening format show evidence that this topic appears?
4. Does a simpler method already meet the constraints?
5. Will one hour here improve my interview odds more than a mock interview or re-solving a weak core pattern?

If the answer to questions 1–3 is no, defer the specialized topic.

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
| Statistic over every fixed-length contiguous range | 🔴 Tier 1 — Must Master | Frequency map/deque | Fixed window; monotonic deque for extrema | Maximum average, window maximum | Recomputing the entire window each step |
| Sorted sequence plus pair/triple condition | 🔴 Tier 1 — Must Master | Array | Opposite-direction two pointers | Two-sum sorted, 3Sum, closest pair | Skipping duplicates incorrectly |
| Remove/compact/partition in place | 🔴 Tier 1 — Must Master | Array | Read/write or slow/fast pointers | Remove duplicates, move zeroes | Losing unread values during writes |
| Sorted input and exact target/boundary | 🔴 Tier 1 — Must Master | Array | Binary search / lower bound | First occurrence, insertion position | Mixing closed and half-open interval rules |
| Minimize/maximize a numeric answer; feasibility changes only once | 🟠 Tier 2 — Very Important | Usually array + helper | Binary search on answer | Shipping capacity, eating speed | Searching without proving monotonic feasibility |
| Next greater/smaller, span until obstruction | 🟠 Tier 2 — Very Important | Monotonic stack | Maintain unresolved candidates | Daily temperatures, histogram area | Storing values when indices/distances are needed |
| Nested scopes, matching delimiters, undo order | 🔴 Tier 1 — Must Master | Stack | Push open state, pop on closure | Valid parentheses, decode string | Popping an empty stack or ignoring final leftovers |
| Repeated minimum/maximum or highest-priority item | 🟠 Tier 2 — Very Important | Heap | Push/pop priority queue | Task scheduling, merge streams | Assuming heap iteration is sorted |
| Top `k` while `k` is much smaller than `n` | 🟠 Tier 2 — Very Important | Size-`k` heap | Retain best `k` | Top frequent items, kth largest | Choosing min- versus max-heap incorrectly |
| Merge `k` sorted sources | 🟠 Tier 2 — Very Important | Heap | Keep one frontier per source | Merge lists, smallest range | Failing to include a tie-breaker for incomparable objects |
| Running median or balanced lower/upper halves | 🟡 Tier 3 — Nice to Know | Two heaps | Max-heap + min-heap rebalance | Median from data stream | Rebalancing sizes without maintaining ordering |
| Overlapping ranges or schedules | 🟠 Tier 2 — Very Important | Sorted interval list | Sort then merge/scan | Merge intervals, insert interval | Treating touching endpoints incorrectly for the stated semantics |
| Maximum compatible activities | 🟠 Tier 2 — Very Important | Sorted intervals | Greedy by earliest finish | Non-overlapping intervals | Sorting by start because it feels natural |
| Number of simultaneous events/resources | 🟠 Tier 2 — Very Important | Heap or event list | End-time heap; sweep line | Meeting rooms, maximum overlap | Processing equal-time starts/ends in the wrong order |
| Hierarchical structure, subtree result | 🔴 Tier 1 — Must Master | Tree + call stack | DFS / postorder | Height, diameter, balanced tree | Returning global information instead of the subtree state parent needs |
| Tree level, nearest node, minimum edges | 🔴 Tier 1 — Must Master | Queue | BFS by level | Level order, nearest target | Mixing levels or marking visited too late |
| Ordered binary tree | 🟠 Tier 2 — Very Important | BST | Use lower/upper bounds or inorder order | Validate BST, kth smallest | Comparing only a node with its parent |
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
| Linked structure cycle or midpoint | 🟠 Tier 2 — Very Important | Linked-list pointers | Floyd fast/slow pointers | Cycle detection, middle node | Dereferencing `fast->next` without a guard |
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

These are rough interview clues, not contractual limits; language and constant factors matter.

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

> “This is `O(n)` expected time and `O(n)` extra space with `std::unordered_map`. Sorting would cost `O(n log n)`, mutate or copy the input, and complicate original indices.”

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

Avoiding predictable errors often improves performance more than learning one more specialized algorithm.

| Trap | Why it fails | Prevention |
|---|---|---|
| Off-by-one errors | A loop misses or repeats a boundary element. | Write whether ranges are inclusive or half-open; test sizes `0`, `1`, and `2`. |
| Mixed binary-search conventions | Update rules no longer guarantee progress. | Pick `[lo, hi]` or `[lo, hi)` and use its matching loop, midpoint, and updates. |
| Wrong bound after binary search | The loop terminates correctly but `lo` has no defined meaning. | State the predicate and postcondition: “`lo` is the first true index.” |
| Forgetting duplicates | Counts, index choices, or generated results become wrong. | Ask whether identity, value, and multiplicity matter; sort and skip at the correct recursion depth. |
| Using one element twice | A complement lookup sees the current item. | Check before insert when distinct positions are required. |
| Modifying a container while iterating | Reallocation or erasure invalidates C++ iterators, pointers, or references. | Follow that container's invalidation rules; use the iterator returned by `erase`, collect changes, or use a controlled index. |
| Quadratic front deletion | Calling `vector.erase(vector.begin())` shifts all remaining items. | Use `std::queue`, `std::deque`, or a head index. |
| Infinite pointer loop | A branch changes neither boundary. | Verify that every iteration advances, returns, or shrinks a finite state. |
| Incorrect recursion base case | Work stops too soon, never stops, or returns the wrong identity. | Define the function contract first; test the smallest legal state. |
| Missing backtracking undo | One branch contaminates another. | Use choose → explore → unchoose symmetrically; copy only at completed answers. |
| Shared mutable result/path | Stored pointers/references all observe the same changing path. | Save a value snapshot such as `answers.push_back(path)`, not a pointer or reference to the live path. |
| Forgetting visited nodes | Graph traversal loops or repeats exponential work. | Decide when a node becomes discovered; normally mark on enqueue/push. |
| Treating a directed graph as undirected | Cycles, reachability, and indegrees are wrong. | Encode edge direction explicitly and test a one-way example. |
| DFS for shortest unweighted path | First depth-first path need not be shortest. | Use BFS layers for equal-weight edges. |
| BFS for weighted shortest path | Fewest edges may cost more. | Use Dijkstra for nonnegative weights; reconsider if negative edges exist. |
| Finalizing Dijkstra too early | A later relaxation may produce a cheaper route. | Ignore stale heap entries and finalize the best popped distance. |
| Incorrect grid boundaries | In C++, an unchecked negative or oversized index can produce undefined behavior. | Check signed coordinates before converting/indexing; distinguish rows from columns. |
| Losing tree return state | Parent cannot combine subtree results. | Ask exactly what one recursive call promises to return. |
| Confusing depth and height | Edge/node conventions produce wrong answers. | Define them explicitly; for example, empty height `0`, leaf height `1`. |
| Integer overflow | Sums, products, or midpoint calculations overflow. | Use wider types; compute `lo + (hi - lo) / 2` where the bounds make subtraction safe. |
| Signed/unsigned mismatch | Comparing `int` with `size_t`, or subtracting from `size()`, can wrap or warn unexpectedly. | Check emptiness first; keep one index type within a loop or convert deliberately with `static_cast<int>`. |
| Reading an empty container | `front()`, `back()`, `top()`, or indexing has no valid element and may cause undefined behavior. | Establish non-emptiness before access; make the empty-result contract explicit. |
| Accidental large copy | Passing a `vector`, `string`, map, or set by value adds hidden linear work and storage. | Use `const T&` for read-only input and `T&` for intentional caller-visible mutation; copy only when the algorithm needs ownership. |
| Invalid iterator use | Dereferencing `end()` or keeping an iterator across reallocation/rehash produces undefined behavior. | Compare with `end()` before dereferencing and know the chosen container's invalidation rules. |
| Uninitialized scalar | A counter, pointer, or best-so-far value starts indeterminate and corrupts the invariant. | Initialize every scalar from the problem's identity, first valid value, or a justified sentinel. |
| Incorrect custom comparator | Using `<=`, inconsistent tie handling, or overflow-prone subtraction violates strict weak ordering. | Return true only when the first item must precede the second; use lexicographic comparisons and explicit ties. |
| Default heap direction | `std::priority_queue<T>` returns the maximum, but the algorithm needs the minimum. | State the required root and use `std::priority_queue<T, vector<T>, greater<T>>` for a min-heap. |
| Assignment inside a condition | Writing `=` where `==` was intended mutates state and tests the assigned value. | Read conditions aloud and enable compiler warnings in practice; keep assignments outside conditions unless deliberate. |
| Assuming hash operations are ordered | Output or tie behavior becomes nondeterministic. | Sort explicitly or use an ordered structure when order is required. |
| Assuming heap contents are globally sorted | Reading the backing array gives the wrong sequence. | Only the root is guaranteed extreme; pop repeatedly if order is required. |
| Recomputing window state | An intended linear scan becomes `O(n²)`. | Maintain counts/sum incrementally as boundaries move. |
| Sliding window with negative values | Expanding/shrinking no longer changes the sum predictably. | Use prefix sums + map, or another method justified by constraints. |
| Incorrect interval endpoint semantics | Touching meetings are merged or separated incorrectly. | Clarify closed vs half-open intervals and order equal-time events accordingly. |
| Greedy without a proof idea | A locally attractive choice blocks the optimum. | Give an exchange or stays-ahead argument; otherwise try DP/search. |
| DP state lacks information | Different futures collapse into one table entry. | Phrase `dp[state]` as a complete sentence and test two histories mapping to it. |
| DP state stores irrelevant history | State count explodes. | Retain only information needed to choose and evaluate future decisions. |
| Wrong DP update direction | A 0/1 item is reused, or an unlimited item is used only once. | For compressed knapsack, iterate capacity backward for 0/1 and forward for unbounded. |
| Memoization key omits mutable state | Cached results are reused for nonequivalent subproblems. | Include every future-relevant dimension or redesign state. |
| Calling output storage “auxiliary space” | Complexity explanation becomes ambiguous. | State auxiliary space and required output space separately. |
| Ignoring recursion stack | Claimed space is too low. | Include maximum call depth even if no explicit container is used. |
| Hidden subrange copying | A clean-looking loop or recursion becomes slower and larger. | Know language costs; pass indices or iterator pairs instead of repeatedly copying ranges. |
| Premature advanced algorithm | Complexity and bug risk rise without need. | Match the simplest correct method to the actual constraints. |
| Coding before agreement | A correct implementation solves the wrong interpretation. | Restate contract and example before writing code. |

### A five-point pre-submit scan

Before saying “done,” inspect:

1. **Contract:** right return type, indices versus values, mutation allowed?
2. **Boundaries:** empty input, loop endpoints, last element, neighbor checks?
3. **Multiplicity:** duplicates, ties, multiple paths/answers?
4. **State:** initialized, updated in correct order, and restored when branching?
5. **Complexity:** hidden nested work, recursion depth, and data-structure costs?

---

## 24. Code Templates

**Priority:** 🔴 Tier 1 — Must Master

Why this priority: Reconstructing common control flow is important interview practice, but each template inherits the depth of its underlying topic—Tier 1 patterns still require unaided implementation, while lower-tier templates require proportionally less fluency.

These templates expose common control flow. Before adapting one, fill in four blanks:

1. **State:** what does each variable/container mean?
2. **Invariant:** what is true before and after every iteration/call?
3. **Change:** which condition adds, removes, branches, or terminates?
4. **Result:** where is the answer valid and when is it updated?

Do not memorize placeholder code. Rebuild it from the invariant, then compare.

The snippets below target **C++17** and assume the standard headers for the types they use. Standard-library names are qualified with `std::`. Read-only collections are passed by `const&`; a template that mutates or copies input says so explicitly. Integer sums and path costs use `long long` where overflow from `int` is plausible.

### 24.1 Array traversal

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Scans an indexed sequence once while maintaining a small summary. This example returns no value for an empty input rather than inventing a sentinel.
**Use when:** every item may affect a count, total, extreme, or simple transformation.  
**Invariant:** after index `i`, `best` is the maximum over the processed prefix `[0, i]`.  
**Complexity:** `O(n)` time and `O(1)` auxiliary space.

```cpp
std::optional<int> maximum_value(const std::vector<int>& values) {
    if (values.empty()) {
        return std::nullopt;
    }

    int best = values.front();
    for (std::size_t i = 1; i < values.size(); ++i) {
        best = std::max(best, values[i]);
    }
    return best;
}
```

**Change per problem:** the maintained summary, update condition, and empty-input contract.  
**Common mistakes:** indexing before checking emptiness, copying a large collection into the function, using `int` for a potentially large sum, or hiding linear work inside the loop.

### 24.2 Frequency counting

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Builds `value → occurrence count` for later membership, multiplicity, grouping, or equality checks.  
**Use when:** duplicates or the number of occurrences affects the answer.  
**Invariant:** after processing a prefix, `frequency[x]` equals the number of times `x` appears in that prefix.  
**Complexity:** expected `O(n)` time and `O(k)` space for `k` distinct values.

```cpp
std::unordered_map<int, long long> frequency_table(
        const std::vector<int>& values) {
    std::unordered_map<int, long long> frequency;
    for (int value : values) {
        ++frequency[value];
    }
    return frequency;
}
```

For a small fixed alphabet, prefer `std::array<int, ALPHABET_SIZE>` to avoid hashing overhead.

**Change per problem:** key type, fixed array versus hash map, whether zero counts are erased, and whether only membership is needed.  
**Common mistakes:** using `operator[]` during a read-only lookup and accidentally inserting a key, overlooking case/normalization rules, or using a set when multiplicity matters.

### 24.3 Stack

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Keeps the most recent unresolved item at the top. This canonical version validates nested delimiters.  
**Use when:** work is LIFO—nested scopes, undo, parsing, or an explicit DFS frontier.  
**Invariant:** the stack contains exactly the unmatched opening delimiters from the processed prefix.  
**Complexity:** `O(n)` time and `O(n)` space in the worst case.

```cpp
bool valid_parentheses(const std::string& text) {
    std::stack<char> open;

    for (char token : text) {
        if (token == '(' || token == '[' || token == '{') {
            open.push(token);
            continue;
        }

        if (token != ')' && token != ']' && token != '}') {
            continue;  // Change this policy if non-delimiter input is invalid.
        }
        if (open.empty()) {
            return false;
        }

        char expected = token == ')' ? '(' : (token == ']' ? '[' : '{');
        if (open.top() != expected) {
            return false;
        }
        open.pop();
    }

    return open.empty();
}
```

**Change per problem:** stack entry type, what resolves an entry, and whether processing occurs on push, top, or pop.  
**Common mistakes:** reading `top()` before checking `empty()`, reversing operand order in expression evaluation, or assuming the stack is globally sorted.

### 24.4 Queue and deque

**Priority:** 🔴 Tier 1 — Must Master for queues; 🟡 Tier 3 — Nice to Know for monotonic deques

**What it does:** A queue preserves FIFO discovery order; a deque additionally permits constant-time work at both ends.  
**Use when:** tasks must run in arrival order, layers must remain ordered, or candidates expire from the front and are dominated from the back.  
**Invariant:** `pending.front()` is the earliest enqueued item not yet processed.  
**Complexity:** The FIFO demonstration is `O(n)` time and `O(n)` space. The monotonic-deque example below is `O(n)` time because every index enters and leaves once, with `O(k)` auxiliary space.

```cpp
std::vector<int> process_in_fifo_order(const std::vector<int>& items) {
    std::queue<int> pending;
    for (int item : items) {
        pending.push(item);
    }

    std::vector<int> order;
    order.reserve(items.size());
    while (!pending.empty()) {
        order.push_back(pending.front());
        pending.pop();
    }
    return order;
}
```

Use `std::deque<T>` when the algorithm genuinely needs `push_front`, `pop_front`, `push_back`, and `pop_back`; ordinary BFS usually needs only `std::queue<T>`.

```cpp
std::vector<int> max_each_window(
        const std::vector<int>& nums,
        std::size_t k) {
    if (k == 0 || k > nums.size()) {
        return {};
    }

    std::deque<std::size_t> candidates;
    std::vector<int> answer;
    answer.reserve(nums.size() - k + 1);

    for (std::size_t right = 0; right < nums.size(); ++right) {
        while (!candidates.empty() && right >= k &&
               candidates.front() <= right - k) {
            candidates.pop_front();
        }
        while (!candidates.empty() &&
               nums[candidates.back()] <= nums[right]) {
            candidates.pop_back();
        }
        candidates.push_back(right);

        if (right + 1 >= k) {
            answer.push_back(nums[candidates.front()]);
        }
    }

    return answer;
}
```

**Change per problem:** queued state, discovery timing, level boundaries, and whether both ends are required.  
**Common mistakes:** calling `front()` on an empty container, using `vector.erase(vector.begin())` as a queue, or marking graph states only after dequeueing and creating duplicates.

### 24.5 Prefix sum and prefix-state lookup

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Precomputes cumulative sums or stores prior cumulative states so a range relationship becomes one subtraction or lookup.  
**Use when:** many static range-sum queries, or a subarray can be recognized from two prefix states.  
**Invariant:** `prefix[i]` is the sum of the first `i` values, so range `[left, right)` sums to `prefix[right] - prefix[left]`.  
**Complexity:** build `O(n)` time/space; each range query `O(1)`.

```cpp
std::vector<long long> build_prefix(const std::vector<int>& nums) {
    std::vector<long long> prefix(nums.size() + 1, 0);
    for (std::size_t i = 0; i < nums.size(); ++i) {
        prefix[i + 1] = prefix[i] + nums[i];
    }
    return prefix;
}

long long range_sum(
        const std::vector<long long>& prefix,
        std::size_t left,
        std::size_t right) {
    // Sum of the original values in [left, right); caller supplies valid bounds.
    return prefix[right] - prefix[left];
}
```

For “number of subarrays with sum `target`,” store earlier prefix frequencies:

```cpp
long long count_subarrays_with_sum(
        const std::vector<int>& nums,
        long long target) {
    std::unordered_map<long long, long long> seen{{0, 1}};
    long long prefix = 0;
    long long answer = 0;

    for (int value : nums) {
        prefix += value;
        auto it = seen.find(prefix - target);
        if (it != seen.end()) {
            answer += it->second;
        }
        ++seen[prefix];
    }
    return answer;
}
```

**Change per problem:** the aggregated value and the relationship between current and earlier prefixes.  
**Common mistakes:** omitting initial prefix `0`, mixing inclusive/exclusive endpoints, and storing only one index when frequencies are needed.

### 24.6 Opposite-direction two pointers

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Uses ordered comparisons to discard one endpoint at a time without missing a valid pair.  
**Use when:** sorted order makes a comparison tell you which boundary cannot participate.  
**Invariant:** discarded elements cannot form a better/valid answer under the problem's ordering rule.  
**Complexity:** usually `O(n)` after any sorting; sorting makes total time `O(n log n)`.

```cpp
std::optional<std::pair<std::size_t, std::size_t>> pair_sum_sorted(
        const std::vector<int>& nums,
        long long target) {
    if (nums.size() < 2) {
        return std::nullopt;
    }

    std::size_t left = 0;
    std::size_t right = nums.size() - 1;
    while (left < right) {
        long long total = static_cast<long long>(nums[left]) + nums[right];
        if (total == target) {
            return std::pair<std::size_t, std::size_t>{left, right};
        }
        if (total < target) {
            ++left;
        } else {
            --right;
        }
    }
    return std::nullopt;
}
```

**Change per problem:** comparison, answer update, duplicate handling, and whether the array may be sorted.  
**Common mistakes:** using it without exploitable order, moving the wrong boundary, or returning sorted positions when original indices are required.

### 24.7 Same-direction read/write pointers

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Compacts selected values into a finalized prefix without allocating another result vector.  
**Use when:** filtering or compacting an array in place.  
**Invariant:** `nums[0..write)` contains the finalized kept values from the processed prefix.  
**Complexity:** `O(n)` time and `O(1)` auxiliary space.

```cpp
template <typename Predicate>
std::size_t keep_if(std::vector<int>& nums, Predicate should_keep) {
    std::size_t write = 0;
    for (std::size_t read = 0; read < nums.size(); ++read) {
        if (should_keep(nums[read])) {
            nums[write] = nums[read];
            ++write;
        }
    }
    return write;  // The valid result occupies nums[0..write).
}
```

**Change per problem:** keep condition and whether relative order must remain stable.  
**Common mistakes:** confusing returned length with last valid index or overwriting data still needed later.

### 24.8 Fixed-size sliding window

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Updates an aggregate by adding the entering value and removing the leaving value.  
**Use when:** evaluate every contiguous block of exactly `k` elements.  
**Invariant:** after removing the outgoing value, `window` describes exactly the current `k` elements.  
**Complexity:** `O(n)` time and `O(1)` space for a sum; frequency state may use `O(k)`.

```cpp
std::optional<long long> max_sum_window(
        const std::vector<int>& nums,
        std::size_t k) {
    if (k == 0 || k > nums.size()) {
        return std::nullopt;
    }

    long long window = 0;
    for (std::size_t i = 0; i < k; ++i) {
        window += nums[i];
    }

    long long best = window;
    for (std::size_t right = k; right < nums.size(); ++right) {
        window += nums[right];
        window -= nums[right - k];
        best = std::max(best, window);
    }
    return best;
}
```

**Change per problem:** maintained aggregate and answer type.  
**Common mistakes:** failing to validate `k`, removing the wrong index, or rescanning the whole window.

### 24.9 Variable-size sliding window

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Expands a right boundary and shrinks the left boundary until a maintainable validity rule is restored.  
**Use when:** a contiguous window becomes valid again by repeatedly moving `left`.  
**Invariant:** after the inner loop, the current window satisfies the required condition.  
**Complexity:** usually `O(n)` time because each boundary advances at most `n` times; state is often `O(alphabet)` or `O(n)`.

```cpp
std::size_t longest_window_with_at_most_k_distinct(
        const std::vector<int>& items,
        std::size_t k) {
    if (k == 0) {
        return 0;
    }

    std::unordered_map<int, int> frequency;
    std::size_t left = 0;
    std::size_t best = 0;

    for (std::size_t right = 0; right < items.size(); ++right) {
        ++frequency[items[right]];

        while (frequency.size() > k) {
            int outgoing = items[left++];
            auto it = frequency.find(outgoing);
            if (--it->second == 0) {
                frequency.erase(it);
            }
        }

        best = std::max(best, right - left + 1);
    }
    return best;
}
```

**Change per problem:** state, invalid predicate, and whether the answer updates before or after shrinking.  
**Common mistakes:** `if` instead of `while`, incorrect removal order, and applying the template when negative values destroy monotonic repair.

### 24.10 Binary search: exact match

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Repeatedly halves a sorted candidate range and returns an index only when an exact value is found.  
**Use when:** search a sorted random-access collection.  
**Invariant:** if the target exists, it remains inside half-open range `[left, right)`.  
**Complexity:** `O(log n)` time, `O(1)` space.

```cpp
std::optional<std::size_t> binary_search_exact(
        const std::vector<int>& nums,
        int target) {
    std::size_t left = 0;
    std::size_t right = nums.size();

    while (left < right) {
        std::size_t mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            return mid;
        }
        if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }
    return std::nullopt;
}
```

**Change per problem:** comparison key; exact-match search does not by itself guarantee first/last duplicate.  
**Common mistakes:** using `left < right` with inclusive updates or setting a boundary to `mid` without proving progress.

### 24.11 Binary search: lower bound, upper bound, and first true

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Finds a transition boundary: first value `>= target`, first value `> target`, or first candidate satisfying a monotone predicate.  
**Use when:** a predicate over ordered candidates is false, then true; find the first true position.  
**Invariant:** a boundary answer lies in half-open search interval `[left, right)`; `right` may be the sentinel `values.size()`.  
**Complexity:** `O(log n)` predicate calls.

```cpp
std::size_t lower_bound_index(
        const std::vector<int>& values,
        int target) {
    std::size_t left = 0;
    std::size_t right = values.size();
    while (left < right) {
        std::size_t mid = left + (right - left) / 2;
        if (values[mid] >= target) {
            right = mid;
        } else {
            left = mid + 1;
        }
    }
    return left;
}

std::size_t upper_bound_index(
        const std::vector<int>& values,
        int target) {
    std::size_t left = 0;
    std::size_t right = values.size();
    while (left < right) {
        std::size_t mid = left + (right - left) / 2;
        if (values[mid] > target) {
            right = mid;
        } else {
            left = mid + 1;
        }
    }
    return left;
}

std::optional<long long> first_true_nonnegative(
        long long low,
        long long high,
        const std::function<bool(long long)>& feasible) {
    if (low < 0 || low > high || !feasible(high)) {
        return std::nullopt;
    }

    while (low < high) {
        long long mid = low + (high - low) / 2;
        if (feasible(mid)) {
            high = mid;
        } else {
            low = mid + 1;
        }
    }
    return low;
}
```

This version deliberately uses a nonnegative answer domain, so `high - low` cannot overflow. Adapt the midpoint logic explicitly if a problem truly needs the full signed-integer domain.

**Change per problem:** domain bounds and monotone feasibility predicate. Test the returned candidate if no feasible value is guaranteed.  
**Common mistakes:** guessing bounds, reversing predicate direction, and multiplying by predicate cost incorrectly; total time is `O(feasibility_cost × log(domain_size))`.

### 24.12 Linked-list reversal and fast/slow pointers

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Rewires links in place or advances pointers at different speeds to expose a midpoint or cycle.  
**Use when:** links must be redirected in place, or a cycle/midpoint must be detected without extra storage.  
**Complexity:** `O(n)` time, `O(1)` auxiliary space.

```cpp
struct ListNode {
    int val;
    ListNode* next;

    explicit ListNode(int value, ListNode* next_node = nullptr)
        : val(value), next(next_node) {}
};

ListNode* reverse_list(ListNode* head) {
    ListNode* previous = nullptr;
    ListNode* current = head;
    while (current != nullptr) {
        ListNode* following = current->next;  // Save before overwriting.
        current->next = previous;
        previous = current;
        current = following;
    }
    return previous;
}

bool has_cycle(const ListNode* head) {
    const ListNode* slow = head;
    const ListNode* fast = head;
    while (fast != nullptr && fast->next != nullptr) {
        slow = slow->next;
        fast = fast->next->next;
        if (slow == fast) {
            return true;
        }
    }
    return false;
}

const ListNode* middle_node(const ListNode* head) {
    const ListNode* slow = head;
    const ListNode* fast = head;
    while (fast != nullptr && fast->next != nullptr) {
        slow = slow->next;
        fast = fast->next->next;
    }
    return slow;  // For even length, returns the second middle.
}
```

**Change per problem:** reversal segment boundaries, midpoint convention, or post-meeting cycle logic.  
**Common mistakes:** losing the next node, failing to reconnect a reversed segment, and unsafe `fast->next->next` access.

### 24.13 Monotonic stack

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Retains unresolved indices in monotone value order and resolves them when a breaking value arrives.  
**Use when:** each item needs the next prior/later item that breaks a monotone condition.  
**Invariant:** indices in the stack have unresolved answers and their values remain monotone.  
**Complexity:** `O(n)` time—each index enters and leaves once—and `O(n)` space.

```cpp
std::vector<int> next_greater_distance(const std::vector<int>& nums) {
    std::vector<int> answer(nums.size(), 0);
    std::vector<std::size_t> unresolved;  // Values decrease along the stack.

    for (std::size_t i = 0; i < nums.size(); ++i) {
        while (!unresolved.empty() && nums[unresolved.back()] < nums[i]) {
            std::size_t j = unresolved.back();
            unresolved.pop_back();
            answer[j] = static_cast<int>(i - j);
        }
        unresolved.push_back(i);
    }
    return answer;
}
```

**Change per problem:** increasing/decreasing direction, strict versus non-strict comparison, answer on pop versus current item, and circular traversal.  
**Common mistakes:** storing values instead of indices and mishandling equal values.

### 24.14 Tree DFS and BFS

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** DFS returns a subtree summary; BFS groups nodes by distance from the root.  
**Use when:** the parent answer combines child results.  
**Invariant:** `dfs(node)` has one explicitly stated return contract.  
**Complexity:** both traversals take `O(n)` time. Recursive DFS uses `O(h)` call-stack space for height `h`; BFS uses `O(w)` frontier space for maximum width `w`, excluding returned output.

```cpp
struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;

    explicit TreeNode(int value, TreeNode* left_child = nullptr,
                      TreeNode* right_child = nullptr)
        : val(value), left(left_child), right(right_child) {}
};

int tree_height(const TreeNode* root) {
    if (root == nullptr) {
        return 0;
    }
    return 1 + std::max(tree_height(root->left), tree_height(root->right));
}

std::vector<std::vector<int>> level_order(const TreeNode* root) {
    if (root == nullptr) {
        return {};
    }

    std::vector<std::vector<int>> levels;
    std::queue<const TreeNode*> pending;
    pending.push(root);

    while (!pending.empty()) {
        std::size_t level_size = pending.size();
        std::vector<int> level;
        level.reserve(level_size);

        for (std::size_t i = 0; i < level_size; ++i) {
            const TreeNode* node = pending.front();
            pending.pop();
            level.push_back(node->val);
            if (node->left != nullptr) {
                pending.push(node->left);
            }
            if (node->right != nullptr) {
                pending.push(node->right);
            }
        }
        levels.push_back(std::move(level));
    }
    return levels;
}
```

**Change per problem:** traversal moment (pre/in/post), returned state, carried path data, and whether levels or a flat order are required.  
**Common mistakes:** computing a value but not returning it, mixing node- and edge-count height, dereferencing `nullptr`, or failing to snapshot a BFS level size.

### 24.15 Graph BFS

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Explores an unweighted graph in nondecreasing edge distance from the source.  
**Use when:** exploring by minimum number of unweighted edges or processing one level at a time.  
**Invariant:** queue items are discovered but not fully processed; discovery distance is minimal.  
**Complexity:** graph `O(V + E)` time and `O(V)` space.

```cpp
std::optional<int> shortest_unweighted(
        const std::vector<std::vector<int>>& graph,
        int start,
        int target) {
    const int n = static_cast<int>(graph.size());
    if (start < 0 || start >= n || target < 0 || target >= n) {
        return std::nullopt;
    }

    std::vector<int> distance(n, -1);
    std::queue<int> pending;
    distance[start] = 0;
    pending.push(start);

    while (!pending.empty()) {
        int node = pending.front();
        pending.pop();
        if (node == target) {
            return distance[node];
        }

        for (int neighbor : graph[node]) {
            if (distance[neighbor] == -1) {
                distance[neighbor] = distance[node] + 1;  // Mark on enqueue.
                pending.push(neighbor);
            }
        }
    }
    return std::nullopt;
}
```

For level processing, capture `std::size_t level_size = pending.size()` and process exactly that many nodes before incrementing the level.  
**Change per problem:** neighbor generation, visited key, stored state, and success condition.  
**Common mistakes:** marking on dequeue, using `vector.erase(vector.begin())` for FIFO work, and reusing one visited set across logically different searches.

### 24.16 Grid traversal

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Treats each eligible cell as a graph node and generates neighbors from direction offsets.  
**Use when:** a matrix is an implicit graph.  
**Invariant:** every enqueued cell is in bounds, eligible, and already marked discovered.  
**Complexity:** `O(rows × cols)` time and up to `O(rows × cols)` space.

```cpp
std::vector<std::pair<int, int>> flood_fill(
        const std::vector<std::vector<int>>& grid,
        int start_row,
        int start_col,
        int allowed_value) {
    if (grid.empty() || grid.front().empty()) {
        return {};
    }

    const int rows = static_cast<int>(grid.size());
    const int cols = static_cast<int>(grid.front().size());
    if (start_row < 0 || start_row >= rows ||
        start_col < 0 || start_col >= cols ||
        grid[start_row][start_col] != allowed_value) {
        return {};
    }

    const std::array<std::pair<int, int>, 4> directions{{
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    }};
    std::vector<std::vector<bool>> visited(
        rows, std::vector<bool>(cols, false));
    std::queue<std::pair<int, int>> pending;
    std::vector<std::pair<int, int>> reached;

    visited[start_row][start_col] = true;
    pending.push({start_row, start_col});

    while (!pending.empty()) {
        auto [row, col] = pending.front();
        pending.pop();
        reached.push_back({row, col});

        for (auto [dr, dc] : directions) {
            int next_row = row + dr;
            int next_col = col + dc;
            if (next_row >= 0 && next_row < rows &&
                next_col >= 0 && next_col < cols &&
                !visited[next_row][next_col] &&
                grid[next_row][next_col] == allowed_value) {
                visited[next_row][next_col] = true;
                pending.push({next_row, next_col});
            }
        }
    }
    return reached;
}
```

**Change per problem:** directions, allowed-cell predicate, multi-source initialization, and whether the grid itself can mark visited.  
**Common mistakes:** failing to handle an empty grid, assuming a rectangular grid without a contract, swapping dimensions, and marking after rather than before enqueue.

### 24.17 Iterative graph DFS

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Uses an explicit stack to explore one branch deeply while visiting each reachable node once.  
**Use when:** reachability/components do not require shortest paths and recursion depth may be unsafe.  
**Complexity:** `O(V + E)` time and `O(V)` space.

```cpp
std::vector<int> dfs_iterative(
        const std::vector<std::vector<int>>& graph,
        int start) {
    const int n = static_cast<int>(graph.size());
    if (start < 0 || start >= n) {
        return {};
    }

    std::vector<bool> visited(n, false);
    std::vector<int> stack{start};
    std::vector<int> order;
    visited[start] = true;

    while (!stack.empty()) {
        int node = stack.back();
        stack.pop_back();
        order.push_back(node);

        // Reverse iteration preserves left-to-right adjacency order in this DFS.
        for (auto it = graph[node].rbegin(); it != graph[node].rend(); ++it) {
            int neighbor = *it;
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                stack.push_back(neighbor);
            }
        }
    }
    return order;
}
```

**Change per problem:** neighbor construction and work performed on entry/exit. Recursive DFS is often clearer when postorder exit state matters.  
**Common mistakes:** assuming traversal order when adjacency order is unspecified and forgetting to start a traversal from every vertex for components.

### 24.18 Topological sort (Kahn's algorithm)

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Repeatedly emits zero-indegree nodes, producing a dependency order or detecting a directed cycle.  
**Use when:** directed prerequisites must be ordered or a directed cycle detected.  
**Invariant:** the queue contains currently unblocked vertices with indegree zero.  
**Complexity:** `O(V + E)` time and `O(V + E)` storage including the graph.

```cpp
std::optional<std::vector<int>> topological_order(
        int num_nodes,
        const std::vector<std::pair<int, int>>& edges) {
    if (num_nodes < 0) {
        return std::nullopt;
    }

    std::vector<std::vector<int>> graph(num_nodes);
    std::vector<int> indegree(num_nodes, 0);
    for (const auto& [before, after] : edges) {
        if (before < 0 || before >= num_nodes ||
            after < 0 || after >= num_nodes) {
            return std::nullopt;
        }
        graph[before].push_back(after);
        ++indegree[after];
    }

    std::queue<int> ready;
    for (int node = 0; node < num_nodes; ++node) {
        if (indegree[node] == 0) {
            ready.push(node);
        }
    }

    std::vector<int> order;
    order.reserve(num_nodes);
    while (!ready.empty()) {
        int node = ready.front();
        ready.pop();
        order.push_back(node);
        for (int neighbor : graph[node]) {
            if (--indegree[neighbor] == 0) {
                ready.push(neighbor);
            }
        }
    }

    if (static_cast<int>(order.size()) != num_nodes) {
        return std::nullopt;  // Directed cycle.
    }
    return order;
}
```

**Change per problem:** edge orientation, node labels, and whether any valid order or only feasibility is required.  
**Common mistakes:** reversing prerequisite edges, using an out-of-range vertex label without validation, and forgetting that multiple valid orders can exist.

### 24.19 Union-Find / Disjoint Set Union

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Maintains component representatives under repeated undirected merges.  
**Use when:** repeatedly merge undirected groups and ask whether two items are connected.  
**Invariant:** each root represents one component; `size[root]` is meaningful only at roots.  
**Complexity:** `O(α(n))` amortized per operation, effectively constant; `O(n)` space.

```cpp
class UnionFind {
public:
    explicit UnionFind(int n) {
        if (n < 0) {
            throw std::invalid_argument("size must be nonnegative");
        }
        parent_.resize(n);
        size_.assign(n, 1);
        components_ = n;
        std::iota(parent_.begin(), parent_.end(), 0);
    }

    int find(int x) {
        while (x != parent_[x]) {
            parent_[x] = parent_[parent_[x]];  // Path halving.
            x = parent_[x];
        }
        return x;
    }

    bool unite(int a, int b) {
        int root_a = find(a);
        int root_b = find(b);
        if (root_a == root_b) {
            return false;
        }
        if (size_[root_a] < size_[root_b]) {
            std::swap(root_a, root_b);
        }
        parent_[root_b] = root_a;
        size_[root_a] += size_[root_b];
        --components_;
        return true;
    }

    int components() const {
        return components_;
    }

private:
    std::vector<int> parent_;
    std::vector<int> size_;
    int components_ = 0;
};
```

**Change per problem:** label-to-index mapping and metadata stored per component.  
**Common mistakes:** reading `parent[x]` as the root without calling `find`, or using DSU for directed reachability.

### 24.20 Heap: retain Top-K

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Keeps a min-heap containing only the strongest `k` values seen so far.  
**Use when:** only the best `k` items matter and the full order does not.  
**Invariant:** the min-heap contains the best `k` items seen; its root is the weakest retained item.  
**Complexity:** `O(n log k)` time and `O(k)` space.

This contract returns all `n` values when `k > n`.

```cpp
std::vector<int> k_largest(
        const std::vector<int>& nums,
        std::size_t k) {
    if (k == 0) {
        return {};
    }

    std::priority_queue<int, std::vector<int>, std::greater<int>> winners;
    for (int value : nums) {
        if (winners.size() < k) {
            winners.push(value);
        } else if (value > winners.top()) {
            winners.pop();
            winners.push(value);
        }
    }

    std::vector<int> result;
    result.reserve(winners.size());
    while (!winners.empty()) {
        result.push_back(winners.top());
        winners.pop();
    }
    return result;  // Members are correct; this happens to be ascending.
}
```

For k-way merge, store entries such as `std::tuple{value, source_id, index}` and push the next item only from the source just popped.
**Change per problem:** priority key, heap orientation, tie-breaker, and retained payload.  
**Common mistakes:** expecting sorted heap iteration, mishandling `k > n`, and retaining `n-k` items by accident.

### 24.21 Dijkstra's shortest path

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Repeatedly expands the cheapest non-stale tentative distance and relaxes outgoing edges.  
**Use when:** single-source shortest paths have nonnegative edge weights.  
**Invariant:** when a non-stale `(distance, node)` is popped, it is the cheapest known route to expand.  
**Complexity:** `O((V + E) log E)` with duplicate heap entries in a general graph, simplifying to `O((V + E) log V)` for simple graphs; `O(V + E)` graph/distance storage and up to `O(E)` heap entries.

```cpp
std::vector<long long> dijkstra(
        const std::vector<std::vector<std::pair<int, long long>>>& graph,
        int source) {
    const int n = static_cast<int>(graph.size());
    const long long INF = std::numeric_limits<long long>::max();
    if (source < 0 || source >= n) {
        throw std::out_of_range("source vertex is outside the graph");
    }
    for (const auto& edges : graph) {
        for (const auto& [neighbor, weight] : edges) {
            if (neighbor < 0 || neighbor >= n) {
                throw std::out_of_range("edge endpoint is outside the graph");
            }
            if (weight < 0) {
                throw std::invalid_argument(
                    "Dijkstra requires nonnegative weights");
            }
        }
    }

    using State = std::pair<long long, int>;  // (distance, node)
    std::priority_queue<State, std::vector<State>, std::greater<State>> heap;
    std::vector<long long> distance(n, INF);
    distance[source] = 0;
    heap.push({0, source});

    while (!heap.empty()) {
        auto [dist, node] = heap.top();
        heap.pop();
        if (dist != distance[node]) {
            continue;  // Stale entry.
        }

        for (const auto& [neighbor, weight] : graph[node]) {
            if (weight > INF - dist) {
                continue;  // Prevent overflow; this path exceeds the chosen INF.
            }
            long long candidate = dist + weight;
            if (candidate < distance[neighbor]) {
                distance[neighbor] = candidate;
                heap.push({candidate, neighbor});
            }
        }
    }
    return distance;
}
```

**Change per problem:** graph construction, source(s), early target exit, and extra path state.  
**Common mistakes:** negative weights, reversing directed edges, or treating the first inserted distance as final.

### 24.22 Backtracking: choose, explore, unchoose

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Traverses a decision tree while restoring branch-local state after every recursive call.  
**Use when:** enumerate candidates subject to constraints.  
**Invariant:** `path` represents exactly the choices on the current recursion branch.  
**Complexity:** for this subset template, `O(n · 2^n)` time including output snapshots, `O(n)` auxiliary recursion/path space, and `O(n · 2^n)` output space. Other searches depend on branching, depth, pruning, and output.

```cpp
std::vector<std::vector<int>> subsets(const std::vector<int>& nums) {
    std::vector<std::vector<int>> answers;
    std::vector<int> path;

    std::function<void(std::size_t)> backtrack = [&](std::size_t start) {
        answers.push_back(path);  // Value copy: snapshot the current candidate.

        for (std::size_t i = start; i < nums.size(); ++i) {
            path.push_back(nums[i]);  // Choose.
            backtrack(i + 1);         // Explore; next-state rule varies.
            path.pop_back();          // Unchoose.
        }
    };

    backtrack(0);
    return answers;
}
```

**Change per problem:** choice set, completion, next index, used-state, duplicate skipping, and pruning.  
**Common mistakes:** advancing past `i` when reuse is allowed, saving a pointer/reference to the live path instead of a value snapshot, and skipping duplicates across different depths.

### 24.23 Greedy frontier

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Retains one dominance summary—here, the farthest reachable index—rather than remembering every path.  
**Use when:** a local choice can be proved safe and all processed choices collapse to one equally good or better frontier.  
**Invariant:** every index through `farthest` is reachable using choices from the processed prefix.  
**Complexity:** `O(n)` time and `O(1)` auxiliary space.

```cpp
bool can_reach_end(const std::vector<int>& jumps) {
    std::size_t farthest = 0;

    for (std::size_t i = 0; i < jumps.size(); ++i) {
        if (i > farthest) {
            return false;
        }
        if (jumps[i] < 0) {
            throw std::invalid_argument("jump lengths must be nonnegative");
        }

        std::size_t jump = static_cast<std::size_t>(jumps[i]);
        std::size_t reach = jump > jumps.size() - i
            ? jumps.size()
            : i + jump;
        farthest = std::max(farthest, reach);
        if (!jumps.empty() && farthest >= jumps.size() - 1) {
            return true;
        }
    }
    return jumps.empty();
}
```

**Change per problem:** the dominance summary, safe-choice rule, sorting key if ordering is required, and proof technique.  
**Common mistakes:** choosing a locally attractive value without a safe-choice argument, updating from an unreachable state, or forcing greedy onto a weighted choice problem that needs DP.

### 24.24 Dynamic programming: memoization and tabulation

**Priority:** 🟠 Tier 2 — Very Important

**What it does:** Evaluates each distinct decision state once, either lazily through recursion or explicitly in dependency order.  
**Use when:** a recurrence revisits the same fully described state. Define the state before choosing either form.  
**Example contract:** `solve(i)` is the maximum score obtainable from suffix starting at `i`.

```cpp
long long best_score_memo(const std::vector<int>& values) {
    std::vector<std::optional<long long>> memo(values.size());

    std::function<long long(std::size_t)> solve = [&](std::size_t i) {
        if (i >= values.size()) {
            return 0LL;
        }
        if (memo[i].has_value()) {
            return *memo[i];
        }

        long long skip = solve(i + 1);
        long long take = static_cast<long long>(values[i]) + solve(i + 2);
        memo[i] = std::max(skip, take);
        return *memo[i];
    };

    return solve(0);
}
```

Equivalent bottom-up form:

```cpp
long long best_score_table(const std::vector<int>& values) {
    long long next_one = 0;  // dp[i + 1]
    long long next_two = 0;  // dp[i + 2]

    for (std::size_t end = values.size(); end > 0; --end) {
        std::size_t i = end - 1;
        long long current = std::max(
            next_one,
            static_cast<long long>(values[i]) + next_two);
        next_two = next_one;
        next_one = current;
    }
    return next_one;
}
```

**Complexity:** number of distinct states × work per state; here `O(n)` time and `O(n)` memo space or `O(1)` table-state space.  
**Change per problem:** state meaning, choices, transition, base cases, evaluation order, and reconstructing decisions.  
**Common mistakes:** caching an incomplete state, claiming `O(1)` space while using recursion, and optimizing storage before validating the recurrence.

### 24.25 Trie

**Priority:** 🟡 Tier 3 — Nice to Know

**What it does:** Stores shared string prefixes as root-to-node paths and marks complete words separately.  
**Use when:** many operations share string prefixes.  
**Invariant:** a path from the root spells a prefix; `is_word` distinguishes a complete key from a prefix.  
**Complexity:** insert/search `O(L)` for word length `L`; storage is proportional to created prefix nodes.

```cpp
class Trie {
private:
    struct Node {
        std::unordered_map<char, std::unique_ptr<Node>> children;
        bool is_word = false;
    };

    Node root_;

    const Node* walk(std::string_view text) const {
        const Node* node = &root_;
        for (char character : text) {
            auto it = node->children.find(character);
            if (it == node->children.end()) {
                return nullptr;
            }
            node = it->second.get();
        }
        return node;
    }

public:
    void insert(std::string_view word) {
        Node* node = &root_;
        for (char character : word) {
            auto& child = node->children[character];
            if (child == nullptr) {
                child = std::make_unique<Node>();
            }
            node = child.get();
        }
        node->is_word = true;
    }

    bool contains(std::string_view word) const {
        const Node* node = walk(word);
        return node != nullptr && node->is_word;
    }

    bool starts_with(std::string_view prefix) const {
        return walk(prefix) != nullptr;
    }
};
```

**Change per problem:** alphabet representation, counts, stored payload, deletion, and wildcard traversal.  
**Common mistakes:** treating any prefix as a complete word and overlooking the trie's substantial constant-factor memory.

### 24.26 Interval merge

**Priority:** 🔴 Tier 1 — Must Master

**What it does:** Sorts by start and extends only the last merged interval while overlap continues.  
**Use when:** combine all overlapping intervals.  
**Invariant:** output intervals are sorted, disjoint, and cover all processed input intervals.  
**Complexity:** `O(n log n)` time, `O(n)` auxiliary space for the explicit input-preserving working copy, and `O(n)` output space.

```cpp
using Interval = std::array<long long, 2>;  // {start, end}

std::vector<Interval> merge_intervals(
        const std::vector<Interval>& intervals) {
    if (intervals.empty()) {
        return {};
    }

    std::vector<Interval> ordered = intervals;  // Explicit copy: preserve input.
    std::sort(ordered.begin(), ordered.end(),
              [](const Interval& a, const Interval& b) {
                  return a[0] < b[0] || (a[0] == b[0] && a[1] < b[1]);
              });

    std::vector<Interval> merged;
    merged.reserve(ordered.size());
    merged.push_back(ordered.front());

    for (std::size_t i = 1; i < ordered.size(); ++i) {
        long long start = ordered[i][0];
        long long end = ordered[i][1];
        if (start <= merged.back()[1]) {  // Closed intervals; adjust semantics.
            merged.back()[1] = std::max(merged.back()[1], end);
        } else {
            merged.push_back({start, end});
        }
    }
    return merged;
}
```

**Change per problem:** overlap definition, mutation policy, and whether the answer is a count, schedule, or merged ranges.  
**Common mistakes:** sorting by the wrong key and assuming closed-interval semantics without clarification.

### Template practice rule

For each Tier 1 template, write it on blank paper or a plain editor, state the invariant aloud, and adapt it to at least two variations. For Tier 2 templates, be able to reconstruct the core and look up only infrequent C++ standard-library API details. If you cannot explain why every update preserves the invariant, the template is not yet learned.

---

## 25. How to Learn DSA Effectively

The goal is durable retrieval and transfer to unfamiliar problems. Use this cycle:

> **Learn → Implement → Solve → Struggle → Review → Re-solve → Generalize**

### 25.1 What each step contributes

| Step | What to do | Why it matters | Evidence that the step worked |
|---|---|---|---|
| Learn | Understand the motivating bottleneck, invariant, and complexity. Trace one small example. | Facts without a mental model disappear quickly. | You can explain why the method works without code. |
| Implement | Reconstruct the core operation in your interview language. | Converts recognition into executable detail. | You can write it with only API references. |
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

### 25.3 Use a deliberate problem ladder

For each high-value pattern:

1. **Mechanics problem:** isolates the data structure or loop.
2. **Canonical problem:** uses the standard invariant.
3. **Variation:** changes output, constraints, or update timing.
4. **Mixed problem:** combines it with another pattern.
5. **Cold revisit:** no topic label and no notes.

Three deeply reviewed problems often teach more than fifteen random accepted submissions. Increase volume only when analysis quality remains high.

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
- 2–3 days: re-solve or outline from scratch;
- 1 week: solve cold;
- 2–4 weeks: solve a variation or include it in a mixed set;
- before interviews: prioritize items still marked uncertain in the mistake log.

Successful cold recalls can be spaced farther apart; failures return sooner. Ask “What clue points to this pattern?” before asking “What was the code?”

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
- Know the C++17 standard-library interfaces for maps, sets, queues, heaps, sorting, and custom comparators.
- Practice correcting a bug while narrating calmly.
- Do not ban the IDE entirely; use it for feedback during learning, then reduce assistance to test recall.

### 25.9 Mock interviews

Begin after you can solve core easy problems and some standard mediums. Run mocks under realistic time and communication constraints:

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
| Mistake category | Recognition, modeling, correctness, boundary, implementation, complexity, communication, or language/API |
| What clue I missed | Exact phrase, constraint, or structural fact |
| Hint level used | No hint, pattern, invariant, pseudocode, or full solution |
| Date solved | First correct implementation date |
| Date to review | Next active-recall date |
| Could I solve it again without help? | No / uncertain / yes, with evidence |

Copyable entry:

```markdown
## [Problem name]

- Topic / pattern:
- Difficulty to me:
- Original approach:
- Why it failed:
- Correct insight:
- Better approach:
- Time / auxiliary space:
- Mistake category:
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
| Recognition | Missed that feasibility is monotone | Sort five mixed prompts by likely pattern and justify each |
| Modeling | DP state omitted remaining capacity | Write state contracts without code for three related problems |
| Correctness | Greedy choice had no safe-choice argument | Find a counterexample, then articulate a proof idea for the correct rule |
| Boundary | Lower bound failed on empty input | Test the same template on sizes `0`, `1`, `2`, duplicates, and absent targets |
| Implementation | Linked-list node was lost | Trace pointer assignments on three nodes before coding |
| Complexity | Repeated subrange copies made recursion quadratic | Annotate cost of every nonconstant operation |
| Communication | Began coding without explaining state | Record a two-minute spoken outline before the next solve |
| Language/API | Used an array as a slow queue | Drill the standard queue/heap/map APIs in your interview language |

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

Progress by mastery gates rather than calendar time. A part-time learner often spends roughly 12–20 weeks on a first pass, but prior coding experience and interview date matter more than that estimate. Continue reviewing old phases while adding new ones.

### Phase 1 — Foundations

**Learning objectives:** reason about resource costs; manipulate loops and recursion; connect constraints to feasible complexity; write and test simple functions.

- **Prerequisites:** basic C++ syntax, functions, vectors/strings, conditionals, and loops.
- **🔴 Tier 1 — Must Master:** Big-O time/space, loop analysis, recursion contracts/base cases, array/string traversal.
- **🟠 Tier 2 — Very Important:** amortized dynamic-array/hash behavior, recursion trees at an intuitive level.
- **🟠 Tier 2 — Very Important:** logarithms, modular arithmetic basics, gcd, and overflow-safe arithmetic.
- **🟡 Tier 3 — Nice to Know:** bit operations (`&`, `|`, `^`, shifts).
- **⚪ Tier 4 — Low Priority / Specialized:** formal proofs, advanced number theory.
- **Recommended practice:** annotate short code snippets with complexity; implement iterative and recursive factorial/sum; trace call stacks; solve basic traversal, frequency, reversal, and matrix problems.
- **Mastery gate:** derive common loop/recursion costs, explain auxiliary versus output space, and identify whether `O(n²)` is plausible from constraints.

### Phase 2 — Core Data Structures

**Learning objectives:** choose storage based on operations and implement safe pointer/container manipulation.

- **Prerequisites:** Phase 1; familiarity with classes/references for linked structures.
- **🔴 Tier 1 — Must Master:** arrays/strings, hash map/set, stack, queue/deque, frequency tables.
- **🟠 Tier 2 — Very Important:** linked-list reversal, dummy nodes, fast/slow pointers, cycle detection.
- **🟡 Tier 3 — Nice to Know:** doubly linked lists and LRU structure; `std::map`/`std::set` ordered operations.
- **⚪ Tier 4 — Low Priority / Specialized:** custom hash-table internals beyond collisions/load factor awareness.
- **Recommended practice:** build a frequency counter, bracket validator, queue-based simulation, linked-list reversal/merge/cycle exercises, and compare operation tables without notes.
- **Mastery gate:** justify structure choice, state expected operation costs, manipulate a three-node list without losing links, and avoid front erasure from a `std::vector`.

### Phase 3 — Core Interview Patterns

**Learning objectives:** turn ordering and contiguity clues into linear or logarithmic scans.

- **Prerequisites:** Phases 1–2, especially arrays and hashing.
- **🔴 Tier 1 — Must Master:** prefix sums, two pointers, fixed/variable sliding window, binary search/bounds, sorting as a tool, and standard interval patterns.
- **🟠 Tier 2 — Very Important:** monotonic stack, binary search on answer, heaps/Top-K, and basic sweep-line event counting.
- **🟡 Tier 3 — Nice to Know:** difference arrays, monotonic deque, and quickselect.
- **⚪ Tier 4 — Low Priority / Specialized:** dynamic range-query trees.
- **Recommended practice:** use a ladder for each pattern: one mechanic, two canonical variations, one mixed problem, then a cold mixed set without topic labels.
- **Mastery gate:** distinguish window from prefix sum, pointer scan from binary search, and heap from full sorting; implement lower bound with a stated postcondition; explain each invariant.

### Phase 4 — Trees and Graphs

**Learning objectives:** model nodes/edges, traverse without repetition, return useful recursive state, and select shortest-path/connectivity methods.

- **Prerequisites:** recursion, stack, queue, hashing, heap basics.
- **🔴 Tier 1 — Must Master:** binary-tree recursive DFS/BFS, height/depth, BST invariants, graph adjacency lists, DFS/BFS, components, unweighted shortest paths, and grid-as-graph.
- **🟠 Tier 2 — Very Important:** iterative tree traversal, LCA, tree construction/serialization, directed/undirected cycle detection, topological sort, bipartite checking, multi-source BFS, DSU, and Dijkstra.
- **🟡 Tier 3 — Nice to Know:** MST and Bellman–Ford/Floyd–Warshall awareness.
- **⚪ Tier 4 — Low Priority / Specialized:** strongly connected components, max flow, and other advanced graph algorithms unless targeted.
- **Recommended practice:** draw each graph; implement adjacency construction; solve paired DFS/BFS versions; practice one subtree-return problem, one level problem, one dependency problem, one DSU problem, and one weighted shortest path.
- **Mastery gate:** derive `O(V+E)`, mark visited at the right moment, traverse disconnected components, explain why BFS/Dijkstra applies, and define a tree helper's return contract before coding.

### Phase 5 — Recursion and Backtracking

**Learning objectives:** represent a choice tree, maintain branch state, eliminate invalid branches, and estimate exponential work.

- **Prerequisites:** recursive contracts and DFS; comfort with mutable collections.
- **🔴 Tier 1 — Must Master:** recursion mechanics already learned in Phase 1.
- **🟠 Tier 2 — Very Important:** subsets, permutations, combinations, duplicate handling, constraint search, choose/explore/unchoose, pruning.
- **🟡 Tier 3 — Nice to Know:** bitmask enumeration and advanced pruning/order heuristics.
- **⚪ Tier 4 — Low Priority / Specialized:** highly optimized combinatorial search.
- **Recommended practice:** draw recursion trees for tiny inputs; implement subset/permutation/combination families from blank pages; add duplicate rules and one constraint-placement problem.
- **Mastery gate:** state path meaning and completion rule, restore state correctly, account for output size, and explain why a prune cannot remove a valid solution.

### Phase 6 — Dynamic Programming

**Learning objectives:** derive state and recurrence from brute-force decisions, cache repeated states, choose an evaluation order, and optimize only valid dimensions.

- **Prerequisites:** recursion/backtracking, array/matrix indexing, complexity analysis.
- **🔴 Tier 1 — Must Master:** no separate advanced DP family is promoted to Tier 1; recursion/state fundamentals remain mandatory.
- **🟠 Tier 2 — Very Important:** recognition, state contract, transition, base cases, memoization, tabulation, 1D DP, grid DP, basic 0/1 knapsack, common subsequence DP, and safe space optimization.
- **🟡 Tier 3 — Nice to Know:** unbounded knapsack, interval DP, tree DP, and reconstruction of an optimal solution.
- **⚪ Tier 4 — Low Priority / Specialized:** bitmask DP, contest-style DP optimizations, and high-dimensional exotic states.
- **Recommended practice:** for every problem write brute-force choices first; draw the recursion DAG; implement memoization; convert to a table; optimize space only after tests pass. Mix counting, feasibility, and optimization objectives.
- **Mastery gate:** define each state as a sentence, derive rather than copy transitions, calculate states × transition work, convert a standard memo solution to tabulation, and recognize when greedy or graph traversal is simpler.

### Phase 7 — Advanced Interview Patterns

**Learning objectives:** round out useful breadth without stealing time from core reliability.

- **Prerequisites:** mastery gates through Phase 6.
- **🔴 Tier 1 — Must Master:** continue mixed review of all Tier 1 topics.
- **🟠 Tier 2 — Very Important:** consolidate interval/greedy/heap/DSU/Dijkstra variants based on weaknesses.
- **🟡 Tier 3 — Nice to Know:** trie, running median, rolling hash, MST, and selective interval/tree DP.
- **⚪ Tier 4 — Low Priority / Specialized:** Fenwick/segment trees, KMP in depth, Manacher, advanced graph algorithms, computational geometry.
- **Recommended practice:** choose topics from actual target-company patterns or repeated mock gaps. Learn one canonical application and one recognition exercise for each selected Tier 3 topic.
- **Mastery gate:** recognize specialized structures, implement chosen Tier 3 basics, and consciously decline low-return depth without feeling that the roadmap is incomplete.

### Phase 8 — Interview Practice

**Learning objectives:** integrate recognition, communication, implementation, testing, and recovery under time pressure.

- **Prerequisites:** Tier 1 mastery and working coverage of common Tier 2 topics.
- **🔴 Tier 1 — Must Master:** mixed unseen problems, verbal framework, complexity, edge-case testing.
- **🟠 Tier 2 — Very Important:** mock interviews, timed pairs, follow-up optimization, adapting to changing requirements.
- **🟡 Tier 3 — Nice to Know:** company-specific patterns supported by credible recent evidence.
- **⚪ Tier 4 — Low Priority / Specialized:** last-minute obscure-topic cramming.
- **Recommended practice:** alternate timed solo sessions, peer/mentor mocks, and untimed weakness repair. Re-solve mistake-log items; practice without topic labels; include debugging and follow-up questions.
- **Mastery gate:** consistently reach a correct approach, explain an invariant, produce mostly correct code, test it, and respond constructively to hints within realistic interview time.

### Ongoing weekly balance

A useful steady-state mix is:

- **40% new or weak patterns**;
- **30% delayed re-solves and mistake-log review**;
- **20% mixed timed practice**;
- **10% explanation, template/API, and complexity drills**.

Shift toward mocks near interviews and toward concept repair when the same error category repeats. Do not count passive watching as practice time.

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
- [ ] I handle duplicates, missing keys, and insertion order correctly.
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

### Stacks, queues, and deques — 🟠 Tier 2 — Very Important

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
- [ ] I can explain merge sort and quicksort trade-offs without needing to hand-code every sort.

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
- [ ] I compare `O(n log k)` heap processing with `O(n log n)` sorting and static quickselect.
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
- [ ] I produce clean, interview-friendly C++17 with minimal tooling.
- [ ] I test normal, minimal, duplicate, boundary, and no-solution cases.
- [ ] I can absorb a hint, revise the model, and continue without defensiveness.
- [ ] My mistake log shows fewer repeated categories and successful delayed re-solves.

---

## 29. DSA Interview Cheat Sheet

### Complexity growth

| Complexity | Typical example | Interview interpretation |
|---|---|---|
| `O(1)` | Array index, hash lookup expected | Independent of input size |
| `O(log n)` | Binary search, balanced-tree operation | Repeatedly discard a constant fraction |
| `O(n)` | One scan, tree/graph vertices plus edges | Usually ideal for large flat input |
| `O(n log n)` | Comparison sort, `n` heap operations | Common acceptable bound for large input |
| `O(n²)` | All pairs, 2D DP | Plausible for hundreds or low thousands, not usually `10^5` |
| `O(2^n)` | Subsets, binary choices | Only for small `n`; pruning may help constants |
| `O(n!)` | All permutations | Only for very small `n` |

**Derive quickly:** sequential blocks add and keep the dominant term; nested dependent work usually sums; recursion is number of calls × work per call; graph traversal is `O(V+E)` with adjacency lists; DP is states × transitions; sorting contributes `O(n log n)` unless a stronger bound dominates.

### Common operation costs

| Structure / operation | Typical time | Important qualification |
|---|---:|---|
| `std::vector` index/update | `O(1)` | Middle insert/erase is `O(n)` and may invalidate iterators/references |
| `std::vector::push_back` | `O(1)` amortized | A reallocation is individually `O(n)` |
| `std::unordered_map` / `std::unordered_set` lookup/insert/erase | `O(1)` expected | Worst case can degrade; rehashing invalidates iterators |
| Linked-list known-node insert/delete | `O(1)` | Finding the node is `O(n)` |
| `std::stack` push/pop/top | `O(1)` | Check `empty()` before `top()` or `pop()` |
| `std::queue` / `std::deque` end operations | `O(1)` | Do not emulate FIFO with front erasure from a vector |
| `std::priority_queue::top` | `O(1)` | Only the root is guaranteed extreme |
| `std::priority_queue` push/pop | `O(log n)` | `std::make_heap` over a full range is `O(n)` |
| `std::map` / `std::set` | `O(log n)` | Maintains key order; unlike unordered containers |
| Trie insert/search | `O(L)` | `L` is key length; memory constants are high |
| Union-Find operation | `O(α(n))` amortized | With compression and union by size/rank |

### Choose a data structure

| Need | Consider | Ask before committing |
|---|---|---|
| Fast membership | `std::unordered_set` | Is ordering or multiplicity needed? |
| Key → value/count/index | `std::unordered_map` | Which occurrence must be stored? |
| Contiguous indexed data | `std::vector` / `std::string` | May I mutate it? |
| LIFO / nested scopes | `std::stack` | What does each stack entry represent? |
| FIFO / level order | `std::queue` | When is an item marked discovered? |
| Both ends / window extrema | `std::deque` | Must it remain monotone? |
| Repeated min/max | `std::priority_queue` | Which item should be at the root? |
| Sorted keys and ordered queries | `std::map`, `std::set`, or sorted vector | Are updates frequent? |
| Prefix lookup | Trie | Does prefix volume justify memory? |
| Undirected connectivity merges | Union-Find | Are paths/directions also required? |
| Hierarchy | Tree | What state flows from child to parent? |
| General relationships | Graph adjacency list | Directed? weighted? disconnected? |

### Clue → pattern

| Clue | First candidates |
|---|---|
| Complement, membership, frequency | Hash map/set |
| Sorted pair/triple | Two pointers |
| Sorted boundary or monotone predicate | Binary search |
| Contiguous, maintainable validity | Sliding window |
| Contiguous exact sum with negatives | Prefix sum + map |
| Repeated range query | Prefix sum; dynamic queries may need specialized tree |
| Next greater/smaller | Monotonic stack |
| Top/Kth/repeated extreme | Heap, sorting, or quickselect |
| Overlapping schedules | Sort intervals; heap or sweep events |
| Tree subtree property | DFS/postorder |
| Tree/graph levels or unweighted shortest path | BFS |
| Connectivity/components | DFS/BFS; DSU for repeated undirected unions |
| Dependencies | Topological sort |
| Nonnegative weighted shortest path | Dijkstra |
| All combinations | Backtracking |
| Repeated decision state | Dynamic programming |
| Prefix dictionary | Trie |

### Key algorithm costs

| Algorithm | Time | Auxiliary space |
|---|---:|---:|
| Two pointers / sliding window | Usually `O(n)` | `O(1)` to `O(n)` state |
| Binary search | `O(log n)` | `O(1)` iterative |
| Comparison sorting | `O(n log n)` typical/guaranteed by algorithm | Implementation dependent |
| Tree traversal | `O(n)` | `O(h)` DFS or `O(w)` BFS |
| Graph BFS/DFS, adjacency list | `O(V+E)` | `O(V)` excluding graph |
| Topological sort | `O(V+E)` | `O(V)` excluding graph |
| Dijkstra, duplicate-entry binary heap | `O((V+E) log E)` general; `O((V+E) log V)` for simple graphs | `O(V+E)` including graph; heap can hold `O(E)` entries |
| Top-K size-`k` heap | `O(n log k)` | `O(k)` |
| Backtracking | Output/search dependent, often exponential | Path + recursion + output |
| DP | states × work per state | Number of stored states |

### Boundary conventions worth stating

- Half-open indexed range `[left, right)` has length `right - left`; C++ iterator ranges use the same convention.
- Inclusive window: `[left, right]` has length `right - left + 1`.
- Binary search: never mix `[left, right]` and `[left, right)` update rules.
- Intervals: clarify whether `[a,b]` overlaps `[b,c]`; scheduling often behaves like half-open `[start,end)`.
- Tree height: say whether measured in nodes or edges.
- Graph labels: confirm zero/one-based and whether isolated nodes are listed.

### Interview workflow: U-B-A-P-E-C-T

1. **Understand:** restate contract, constraints, assumptions, example.
2. **Brute force:** establish a correct baseline.
3. **Analyze:** derive time and space; compare with constraints.
4. **Pattern:** exploit lookup, order, contiguity, structure, monotonicity, or repeated state.
5. **Explain:** data structure, invariant, algorithm, correctness idea, complexity.
6. **Code:** simple names, consistent boundaries, forward progress.
7. **Test:** normal, minimum, duplicates, boundaries, no solution, structure-specific hazards.

### Pre-code questions

- What exactly does my helper/window/table entry represent?
- Why is it safe to discard this element/state/branch?
- Does every loop or recursion make progress?
- Could duplicates, negative values, direction, or weights invalidate the idea?
- What is the simplest counterexample to my greedy rule?
- Which operations dominate time? What remains simultaneously in memory?

### High-frequency edge cases

- empty input and `nullptr` root/head;
- one or two elements/nodes;
- duplicates and all-equal values;
- negative, zero, and very large values;
- target absent or multiple valid answers;
- already sorted and reverse sorted;
- skewed tree and maximum recursion depth;
- disconnected graph, self-loop, parallel edge, and cycle;
- empty row/grid and non-square dimensions;
- touching, nested, or zero-length intervals.

### Final reminders

- Correctness before cleverness; brute force before optimization.
- Explain the invariant, not just the pattern name.
- Expected hash `O(1)` is not ordered behavior.
- BFS gives shortest paths only for equal/unweighted edges.
- Dijkstra requires nonnegative weights.
- A heap is partially ordered, not a sorted list.
- Greedy needs a safe-choice argument; DP needs a complete state.
- Count recursion stack in space.
- Re-solve missed problems; acceptance is not mastery.

---

> **North star:** Given an unseen problem, use its contract and constraints to form candidate patterns, choose a structure whose operations support the invariant, derive complexity, implement clearly, and test the actual code.
