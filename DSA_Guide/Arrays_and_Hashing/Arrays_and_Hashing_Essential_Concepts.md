# Arrays & Hashing — Essential Concepts (Java 21)

> Learn to choose useful state, prove what it represents, and reuse work. A remembered solution is useful only when you can explain its assumptions and adapt it to a new problem.

## Table of Contents

- [1. Purpose, Scope, and Learning Route](#1-purpose-scope-and-learning-route)
- [2. Foundations Refresher and Cost Model](#2-foundations-refresher-and-cost-model)
- [3. Traversal, Transformation, and Processed Prefixes](#3-traversal-transformation-and-processed-prefixes)
- [4. Seen-Value Tracking](#4-seen-value-tracking)
- [5. Complement Lookup](#5-complement-lookup)
- [6. Frequency Counting](#6-frequency-counting)
- [7. Canonical Signatures and Grouping](#7-canonical-signatures-and-grouping)
- [8. Prefix Sums and Range Queries](#8-prefix-sums-and-range-queries)
- [9. Prefix/Suffix Products and Aggregation](#9-prefixsuffix-products-and-aggregation)
- [10. Consecutive Runs and Sequence-Head Expansion](#10-consecutive-runs-and-sequence-head-expansion)
- [11. Comparing Approaches and Bounded Buckets](#11-comparing-approaches-and-bounded-buckets)
- [12. Java-Specific Pitfalls](#12-java-specific-pitfalls)
- [13. Pattern Recognition Cheat Sheet](#13-pattern-recognition-cheat-sheet)
- [14. Interview Self-Assessment and Short Answer Key](#14-interview-self-assessment-and-short-answer-key)
- [15. Key Takeaways, References, and Next Steps](#15-key-takeaways-references-and-next-steps)

## 1. Purpose, Scope, and Learning Route

This guide develops the reasoning behind the Arrays & Hashing category in [the repository roadmap](./DSA_Interview_Roadmap.md#1-arrays--hashing). It assumes familiarity with Java syntax, arrays, strings, collections, and basic Big O. It adds the missing bridge between knowing `HashMap` methods and independently deriving an algorithm.

The roadmap is authoritative for scope and priorities. The following map accounts for its essential objectives and the supporting fundamentals requested for this guide.

| Objective | Where to learn it | Demonstration of understanding |
| --- | --- | --- |
| Array traversal and transformation | [Section 3](#3-traversal-transformation-and-processed-prefixes) | Explain which positions are read, written, and still untouched |
| Loop invariants and processed-prefix reasoning | [Sections 3–6](#3-traversal-transformation-and-processed-prefixes) | State the meaning of stored state before an iteration |
| Hashing for algorithm design | [Sections 2 and 4](#2-foundations-refresher-and-cost-model) | Separate logical equality from bucket collisions and operation costs |
| Membership and duplicate detection | [Section 4](#4-seen-value-tracking) | Replace earlier-value rescans with a seen set |
| Lookup and complements | [Section 5](#5-complement-lookup) | Derive the required prior value and justify query/update timing |
| Frequency arrays and maps | [Section 6](#6-frequency-counting) | Preserve multiplicity and justify the key domain |
| Canonical signatures and grouping | [Section 7](#7-canonical-signatures-and-grouping) | Prove that equal keys mean exactly the intended equivalence |
| Prefix sums and range queries | [Section 8](#8-prefix-sums-and-range-queries) | Derive a query from the definition of a prefix |
| Prefix/suffix products and aggregation | [Section 9](#9-prefixsuffix-products-and-aggregation) | Combine disjoint sides and separate output from working storage |
| Consecutive sequence detection | [Section 10](#10-consecutive-runs-and-sequence-head-expansion) | Explain why each distinct value is expanded only once |
| Sorting, hashing, and bucket counting | [Section 11](#11-comparing-approaches-and-bounded-buckets) | Compare assumptions, order, numeric range, and memory |
| Time-space tradeoffs and selection | [Sections 11–13](#11-comparing-approaches-and-bounded-buckets) | Select a method from the actual required operations |

### Reuse the references; deepen the reasoning

| Existing resource | Knowledge reused here | What this guide develops further |
| --- | --- | --- |
| [Java Arrays](./Java_Array.md) | Indexing, traversal, mutation, copies, `Arrays` utilities | Safe transformation, invariants, and positional aggregation |
| [Java Strings](./Java_String.md) | Immutability, UTF-16 indexing, `StringBuilder`, copying | Alphabet assumptions, canonical keys, and signature construction costs |
| [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md) | Counts, lookup, grouping APIs, equality, order | Choosing the map's meaning, update timing, and correctness |
| [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) | Membership, uniqueness, hashing contracts | Which information a set loses and how to avoid repeated expansion |
| [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md) | Expected/amortized costs, memory, library work, input constraints | Deriving complete costs from actual operations and stored state |

Study in order through Section 10, then use the comparison and recognition sections to choose methods on unfamiliar statements. For each pattern: explain the baseline, identify repeated work, define better state, trace it, then code it from a blank editor.

The Java examples use small teaching variants: arrival flags, earlier-match counts, grouping by distinct letters, and products outside a block. They teach transferable mechanisms without providing a completed solution bank for the roadmap's exact practice problems. The full essential practice list remains in the roadmap. No separate exercise document is assumed to exist.

**Progress:** reading and explaining this material can support the roadmap's T gate. Reimplementing and testing the mechanisms supports I. Independent practice supports P; unfamiliar-problem transfer and a clear mock explanation support R. Update those records in the existing roadmap, not in a duplicate tracker here.

## 2. Foundations Refresher and Cost Model

### 2.1 Choose state by the question it must answer

Imagine processing deliveries. A notebook can record that a parcel arrived, how many arrived, or where the first one appeared. These are different summaries of the same history.

| State | Question it answers | Information it loses | Java strategy |
| --- | --- | --- | --- |
| Set of values | Has this value appeared? | Counts, original positions, and encounter order in a `HashSet` | `HashSet<Integer>` |
| Value → index | Where did a useful occurrence appear? | Other occurrences unless explicitly retained | `HashMap<Integer,Integer>` |
| Value → count | How many occurrences appeared? | Individual occurrence positions | `HashMap<Integer,Integer>` |
| Fixed-domain count array | How many occurrences of this small-domain value? | Values outside the chosen encoding | `int[]` with a justified index mapping |
| Prefix aggregate | What is the aggregate before this boundary? | Individual values may need separate input storage | Usually `long[]` for sums/products |

A map is not automatically better than a set. Store only the information needed to answer future questions. If you need one earlier index, retaining every occurrence adds memory without helping; if you need the number of pairs, storing only one index loses necessary information.

### 2.2 What hashing contributes

An array finds a slot directly from an index. A hash table first computes a bucket from a key's hash, then distinguishes candidate keys by equality. Think of finding a book by a labelled shelf: the shelf narrows the search, but you still check the title.

```text
key -> hashCode -> candidate bucket -> equals checks -> matching key/value
```

Two unequal keys can share a hash or bucket. That is a **hash collision**, which the table handles. Two different logical categories that you accidentally encode as the **same key** are a signature-design bug; hashing cannot repair that loss of information.

Equal keys must have equal hash codes. Different keys need not have different hashes. This contract comes from the [Java 21 Object specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html). `HashMap` and `HashSet` provide expected constant-time basic operations when hashes are suitably dispersed; the [HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) and [HashSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html) documentation states that condition.

### 2.3 Variables and operation costs

Definitions are local to each section, but these names recur:

- `n`: number of input integer elements; `u`: number of distinct integer values.
- `N`: number of strings; `L`: their total length; `k`: one string's length.
- `sigma`: alphabet/domain size; `R`: width of an integer bucket range.
- `q`: number of queries; `c`: hash-table capacity; `S`: characters in a constructed signature.

We use a fixed-width integer/RAM cost model: operations on `int` and `long` take O(1). Arbitrary-precision arithmetic has different costs and is outside this guide. For simplicity, all O(n) bounds can be read as O(n+1) on an empty input.

| Operation | Cost model used here | Qualification |
| --- | --- | --- |
| Array access / integer arithmetic | O(1) | Valid index; arithmetic must fit its intended type |
| Initialize/fill/copy an array of length r | O(r) | Allocation initializes slots; copying is work |
| Integer-key hash lookup/update | Expected O(1); insertion amortized over growth | Poor collision behavior or a resize can make one call slower |
| Scan a `HashMap`/`HashSet` | O(u+c) | Default growth from empty keeps capacity proportional to stored entries, up to a constant minimum |
| Build/hash/compare a length-S string key | Include O(S) character work as a safe bound per key operation | Cached hashes can help an existing object; a fresh signature still has construction/first-hash costs |
| Append to `ArrayList` | Amortized O(1) per reference | An individual resize may copy O(size) references |
| Build S characters with `StringBuilder` | O(S) total amortized building work, plus O(S) final copy | Peak buffer space matters too |
| Sort n primitive integers with Java 21 `Arrays.sort` | O(n log(n+1)) | Mutates input; do not assume every primitive-sort implementation path uses O(1) auxiliary memory |

**Expected and amortized are different qualifications.** Expected hashing assumes a useful hash distribution. Amortization spreads occasional resizing over a sequence of operations; it is not a probability statement. The sorting bound is supported by the Java 21 [Arrays API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html).

When a section gives expected O(n) hashing time, a conservative pathological upper bound is O(n²) if O(n) operations each inspect O(n) candidates. This is a safe general bound, not a claim that it is tight for every Java 21 key type: tree-shaped bins and comparable keys can improve particular collision cases. Add nonconstant key hashing/equality work separately. Do not call hashing unconditionally O(1), or assume every collision makes an algorithm quadratic.

### 2.4 Input, output, auxiliary space, and allocations

Here **auxiliary space excludes the required returned output**. We report that output separately. Total live storage is input + output + auxiliary space under this convention. If an interviewer counts output as auxiliary, add it explicitly; the underlying memory use has not changed. The existing complexity reference discusses both output conventions.

Examples:

- An n-element returned array plus two scalar accumulators has O(n) output and O(1) auxiliary space, not O(1) total storage.
- A seen set used to construct an output array is auxiliary state; it is not part of the returned array.
- A returned frequency map is itself output, even though it acts as the accumulator while being built. Its O(u) storage still exists.
- A returned prefix index occupies O(n) storage across queries. Calling it a preprocessing output does not make the application's memory usage constant.

Boxing integers, allocating hash nodes, list capacity, string buffers, and new keys affect actual memory and allocation volume. A map with u entries uses O(u) live structural storage with ordinary capacity; it does not store primitive values as compactly as `int[]`. Distinguish **peak live storage** from the cumulative bytes allocated over the whole execution. A growing returned collection can briefly retain old and new backing storage; constant explicit local state does not necessarily mean constant peak auxiliary space inside its library operations. The collection-building analyses below state that distinction.

### 2.5 Code conventions

Each `java` block below contains static methods or nested records for one class, with `import java.util.*;` at file scope. Combine them inside `class ArrayHashingExamples { ... }`; some later methods deliberately call earlier helpers. They require non-null inputs, single-threaded use, and feasible array allocations. Additional domain/mutation assumptions are stated at the method's section. No dependencies are needed beyond Java 21.

**Self-check F1:** Why can `HashSet.contains` be expected O(1) while a whole set traversal costs O(u+c)?

**Self-check F2:** Why does returning an n-element array prevent an O(1) total-memory claim even if auxiliary space is O(1)?

## 3. Traversal, Transformation, and Processed Prefixes

### 3.1 The problem and baseline

Suppose a sensor array contains valid nonnegative readings and negative invalid readings. Keep the valid readings in their original order at the front of the same array, and return the valid prefix length. The array cannot physically shrink.

Think of checking tickets: one position inspects the next ticket, while another marks the next place for an accepted ticket.

A straightforward method copies valid values to a separate list or array. That already takes O(n) time, but needs O(n) extra storage. Another tempting method deletes each invalid element by shifting the suffix left; with many invalid values, repeated shifts can take O(n²).

The improvement here is primarily **space**, not beating a linear scan's time. Recognition clues include stable filtering, a logical output length, and permission to mutate the input. It is unsuitable when the original array must remain unchanged or the predicate depends on values that would be overwritten without additional care.

### 3.2 Derive two positions from their meanings

1. `read` identifies the next original value to inspect.
2. `write` is the number of accepted values among the already inspected elements.
3. If the value is accepted, place it at `write` and advance `write`.
4. Advance `read` regardless of acceptance.
5. Because `write <= read`, writes never destroy a future unread element.

An **invariant** is a fact that remains true at a chosen point in every iteration. Here, before processing `read`:

> `values[0, write)` contains exactly the accepted original values from `[0, read)`, in their original order; the unread region has not been changed.

```java
static int keepNonnegativePrefix(int[] values) {
    int write = 0;
    for (int read = 0; read < values.length; read++) {
        int current = values[read];
        if (current >= 0) {
            values[write] = current;
            write++;
        }
    }
    return write;
}
```

### 3.3 Dry run

Input: `[4, -2, 0, -7, 5]`.

| read | Current value | write before | Decision | Valid prefix after |
| --- | --- | --- | --- | --- |
| 0 | 4 | 0 | Copy to index 0 | `[4]` |
| 1 | -2 | 1 | Skip | `[4]` |
| 2 | 0 | 1 | Copy to index 1 | `[4, 0]` |
| 3 | -7 | 2 | Skip | `[4, 0]` |
| 4 | 5 | 2 | Copy to index 2 | `[4, 0, 5]` |

Return 3. The physical array happens to be `[4, 0, 5, -7, 5]`; only `[0, 3)` is the answer. Trailing values are unspecified leftovers.

### 3.4 Why it works

- **Initialization:** no values have been read, and the empty output prefix is correct.
- **Preservation:** a rejected value contributes nothing; an accepted value is appended after all earlier accepted values. No unread position is overwritten.
- **Termination:** after n reads, the invariant covers the entire original input. `write` is exactly the accepted count.

This processed-prefix reasoning generalizes: state should summarize what has been processed and remain sufficient for the next step. In Sections 4–6, the summary becomes a set, a complement-count map, or frequency counts rather than an output prefix.

### 3.5 Complexity, mistakes, and boundaries

Let n be the array length. The loop performs n constant-time reads and at most n writes: O(n) time. Two indices and one value use O(1) auxiliary space. There is no new array output, just a returned scalar and a mutated input prefix. Including the existing input, total storage is O(n).

Do not increment `write` for rejected values, read beyond `length - 1`, or treat trailing elements as valid. Empty input returns 0; all-negative input returns 0; all-accepted input returns n; zero is accepted under this predicate. An enhanced-for variable reassignment would not replace array slots—see [Java Arrays](./Java_Array.md).

**Self-check T1:** What breaks if you advance `write` on every iteration?

**Self-check T2:** Why does `write <= read` protect unread values, and why is the original order preserved?

## 4. Seen-Value Tracking

### 4.1 Recognize the question that needs only membership

For each arriving integer code, mark whether this is its first arrival. Example: `[6, 2, 6, 6]` should produce `[true, true, false, false]`.

A visitor register need only tell you whether the visitor has already checked in. It does not need every earlier visit to answer that question.

Consider this pattern for repeated values, uniqueness, previously visited states, or intersection checks. Required assumption: value equality is the notion of sameness. It is unsuitable when you need counts, earlier indices, sorted neighbors, or a rule about distance between occurrences; a plain set discards that information.

### 4.2 Baseline, bottleneck, and derivation

For each index i, scan indices `[0, i)` for an equal value. If all values are distinct, the number of comparisons is:

```text
0 + 1 + 2 + ... + (n - 1) = n(n - 1)/2
```

The same history is searched repeatedly. Replace those scans with a set containing exactly the values from that history. `add(value)` reports whether the set changed, so one call can both decide novelty and update state.

```java
static boolean[] firstArrivalFlags(int[] codes) {
    Set<Integer> seen = new HashSet<>();
    boolean[] first = new boolean[codes.length];
    for (int i = 0; i < codes.length; i++) {
        first[i] = seen.add(codes[i]);
    }
    return first;
}
```

### 4.3 Dry run and invariant

The sets shown here are logical contents; their displayed order is not a promise about `HashSet` iteration.

| i | Code | Set before | `add` result / output | Set after |
| --- | --- | --- | --- | --- |
| 0 | 6 | `{}` | true | `{6}` |
| 1 | 2 | `{6}` | true | `{6, 2}` |
| 2 | 6 | `{6, 2}` | false | `{6, 2}` |
| 3 | 6 | `{6, 2}` | false | `{6, 2}` |

Before iteration i, `seen` contains exactly the values in `codes[0, i)`. Initially both are empty. If the current value is absent, it is a first arrival and adding it extends the summary correctly. If it is present, an earlier equal value exists, so false is correct; membership already represents the extended prefix. At termination every output position is correct.

### 4.4 Complexity and alternatives

Let n be arrivals and u the number of distinct codes. Output allocation is O(n); there are n expected-amortized constant-time additions. Expected total time is O(n), auxiliary set storage O(u), output storage O(n), and total storage including input O(n+u). A conservative pathological hashing bound is O(n²), with the qualifications in Section 2.

Boxing and table entries add per-key overhead. The set retains u logical elements, not n separate entries for repeated values. Temporary boxing/allocation volume can still depend on n; do not equate number of allocation attempts with live entry count.

Alternatives:

- Sort a copy and scan equal neighbors for a yes/no duplicate question: O(n log n) time and O(n) copy storage. Sorting alone does not recover the original arrival flags; original indices would also be needed.
- Use a boolean presence array for a small dense integer range: deterministic O(n+R) time and O(R) state, but only with a feasible range encoding.
- Keep the quadratic scan when memory is extremely constrained and n is small enough; do not assume a hash set is mandatory.

### 4.5 Mistakes and edge cases

Calling `contains` only after adding loses the before/after distinction: the current value will now be present even on its first arrival. A set also cannot tell whether code 6 appeared twice or ten times. Empty input gives an empty result; repeated identical values give one true then false values. Negative codes and integer extremes are ordinary `Integer` keys.

**Self-check S1:** What information permits a set to replace the earlier-prefix scan?

**Self-check S2:** Why does this state fail if the question changes to “how many earlier arrivals had this code”?

## 5. Complement Lookup

### 5.1 Derive the value you need

Suppose an arriving amount must be paired with an **earlier** amount to reach a target. Report how many earlier amounts could pair with each arrival. This is an online counting variant, not the roadmap problem's pair-of-indices interface.

Imagine matching a new payment with earlier vouchers: instead of checking every voucher, ask for the denomination that would complete the target.

From `earlier + current = target`, derive:

```text
neededEarlier = target - current
```

Recognition clues: pair relationships, a current value plus a missing counterpart, online arrival order, or “has a compatible value already appeared?” The relation must let you compute a small set of required keys. If the complement can take many unrelated values, ordered search or another representation may be needed.

### 5.2 Baseline and optimized state

The brute-force count for index i scans all j < i and checks the sum. The worst-case work is O(n²). The repeated work is discovering how many earlier occurrences have the required value.

Derive the state from the required output:

1. A set would tell only whether at least one compatible value exists.
2. A value-to-index map would retain only one occurrence.
3. A **value-to-count map** answers how many compatible earlier occurrences exist.
4. Query that map before inserting the current occurrence, because “earlier” excludes the current index.

```java
static long[] priorMatchesPerArrival(int[] values, int target) {
    Map<Long, Integer> earlierCounts = new HashMap<>();
    long[] matches = new long[values.length];
    for (int i = 0; i < values.length; i++) {
        long current = values[i];
        long needed = (long) target - current;
        matches[i] = earlierCounts.getOrDefault(needed, 0);
        earlierCounts.put(current,
                earlierCounts.getOrDefault(current, 0) + 1);
    }
    return matches;
}
```

`long` keys let the computed complement be represented even when it lies outside the int range. Since both input values and target are int, this subtraction fits long. A complement outside the int domain simply cannot match a stored input value. A count fits int because it is at most the input array length; a total over many pairs would need a long accumulator. The long output leaves room for such extensions.

### 5.3 Detailed dry run

Input `[4, 7, 4, 2, 5]`, target 9.

| i | Current | Needed | Map before | Decision / matches[i] | Map after |
| --- | --- | --- | --- | --- | --- |
| 0 | 4 | 5 | `{}` | No earlier 5; 0 | `{4:1}` |
| 1 | 7 | 2 | `{4:1}` | No earlier 2; 0 | `{4:1, 7:1}` |
| 2 | 4 | 5 | `{4:1, 7:1}` | No earlier 5; 0 | `{4:2, 7:1}` |
| 3 | 2 | 7 | `{4:2, 7:1}` | One earlier 7; 1 | `{4:2, 7:1, 2:1}` |
| 4 | 5 | 4 | `{4:2, 7:1, 2:1}` | Two earlier 4s; 2 | `{4:2, 7:1, 2:1, 5:1}` |

Output: `[0, 0, 0, 1, 2]`. The final 5 forms separate valid pairs with indices 0 and 2. Equal values can represent different usable occurrences; different indices matter.

### 5.4 Correctness and update timing

Before iteration i, the map counts exactly the occurrences in `[0, i)`. It is initially empty. Its `needed` entry therefore counts exactly the earlier partners for i. Recording that count is correct, and then inserting the current occurrence makes the map correct for `[0, i+1)`. At termination every arrival has its exact earlier-partner count.

Counterexample to inserting first: input `[4]`, target 8. If you insert 4 before querying 4, the current index incorrectly matches itself. With `[4, 4]`, the second 4 may legitimately match the first; avoiding equal values entirely would reject that valid pair.

“Always query before insertion” is not a universal map rule. It follows from this invariant about **earlier** values. A two-pass map containing all positions needs a different mechanism to enforce distinct indices.

### 5.5 Complexity, alternatives, and limits

Let n be input length and u distinct input values. Allocation costs O(n); each of n iterations performs a constant number of expected O(1) integer-key map operations. Expected time is O(n), with O(u) auxiliary map storage and O(n) output. Total storage including input is O(n+u). A conservative pathological hashing upper bound is O(n²).

For a single pair-existence query, sorting plus two pointers is an O(n log n) alternative, or O(n) scanning time if the input is already sorted. It changes the handling of original positions and does not directly provide the same per-arrival counts. For very small bounded domains, a count array can replace the map. Distinct-pair counting and counting all index pairs also have different duplicate rules—clarify the required output first.

Handle empty input, no partners, repeated partners, self-complements, negative values, and extreme ints. Do not compute `target - values[i]` in int and cast afterward; overflow would have happened already.

**Self-check C1:** What changes in the stored state when the output changes from existence to one index to number of earlier partners?

**Self-check C2:** Why is `[4, 4]` with target 8 valid but `[4]` is not?

## 6. Frequency Counting

### 6.1 Counts preserve information that membership discards

A warehouse inventory records how many units of each item exist. Knowing that both warehouses stock apples and pears does not imply equal inventories: one might contain two apples and one pear, the other one apple and two pears.

Use frequencies when the statement involves multiplicity, availability, repeated occurrences, rearrangement, inventory equality, or “at most k copies.” A set is insufficient whenever repeated copies affect the answer. If positions or occurrence order matter, a histogram alone is insufficient too.

A brute-force approach selects each distinct value and scans the entire input to count it. For n elements and u distinct values, that can cost O(nu), or O(n²) when u grows with n. The optimization is to count **all values in one scan**, updating the bucket for the current value.

### 6.2 Choose a map or a direct index

For arbitrary int values, use a map. Before each update, the current count is the number of occurrences in the processed prefix; a missing count means zero.

```java
static Map<Integer, Integer> valueCounts(int[] values) {
    Map<Integer, Integer> counts = new HashMap<>();
    for (int value : values) {
        counts.put(value, counts.getOrDefault(value, 0) + 1);
    }
    return counts;
}

static long reconstructedTotal(Map<Integer, Integer> counts) {
    long total = 0;
    for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
        total += (long) entry.getKey() * entry.getValue();
    }
    return total;
}
```

`reconstructedTotal` assumes its argument is a frequency map produced from an int array, with non-null keys and positive non-null counts. Each key contributes value × occurrence count. Casting before multiplication protects the int multiplication. Under this provenance assumption, the sum fits long: at most an int-array-length number of int values are represented. Arbitrary externally supplied maps would need their own numeric-range contract.

`entrySet()` is appropriate because both key and count are needed. It avoids a second lookup for every key and makes no promise about traversal order. `getOrDefault(value, 0)` reads a fallback; the following `put` actually stores the incremented count. These semantics are specified by the [Java 21 Map API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html).

When the domain is exactly lowercase English `a`–`z`, a character can directly select one of 26 counters:

```java
static int[] lowercaseCounts(String text) {
    int[] counts = new int[26];
    for (int i = 0; i < text.length(); i++) {
        char letter = text.charAt(i);
        if (letter < 'a' || letter > 'z') {
            throw new IllegalArgumentException("lowercase a-z required");
        }
        counts[letter - 'a']++;
    }
    return counts;
}
```

Subtracting `'a'` is an encoding, not a general character-counting trick. Uppercase letters, punctuation, accented characters, and arbitrary Unicode text violate this domain. A `HashMap<Character,Integer>` can count UTF-16 code units, but whole Unicode code points require a code-point-aware representation. Do not silently normalize text unless the problem permits it.

### 6.3 Dry runs

Integer input `[4, 4, -1, 4, -1]`:

| Processed value | Its old count | Its new count | Map after processing |
| --- | --- | --- | --- |
| 4 | 0 | 1 | `{4:1}` |
| 4 | 1 | 2 | `{4:2}` |
| -1 | 0 | 1 | `{4:2, -1:1}` |
| 4 | 2 | 3 | `{4:3, -1:1}` |
| -1 | 1 | 2 | `{4:3, -1:2}` |

The reconstructed total is `4 × 3 + (-1) × 2 = 10`, the same as adding the original values.

For `"cabba"`, only the relevant counter slots are shown:

| Processed prefix | a | b | c |
| --- | --- | --- | --- |
| empty | 0 | 0 | 0 |
| c | 0 | 0 | 1 |
| ca | 1 | 0 | 1 |
| cab | 1 | 1 | 1 |
| cabb | 1 | 2 | 1 |
| cabba | 2 | 2 | 1 |

### 6.4 Correctness and complexity

For both counters, the invariant is: **each bucket equals the number of occurrences of its value in the processed prefix**. Empty-prefix counts are zero. One new occurrence increases exactly its own bucket by one; all other buckets remain correct. After the scan, the prefix is the whole input. The weighted reconstruction is correct because the buckets partition the original occurrences into disjoint groups.

For `valueCounts`, let n be input length and u distinct values. There are n iterations, each doing two expected O(1) key operations: expected O(n) time. A conservative pathological bound is O(n²). The returned map occupies O(u) output storage, with O(1) explicit local state outside it. Counting temporary hash-table growth buffers separately gives a conservative O(u) peak auxiliary bound during construction; total additional live storage, including the output map, is still O(u). If this map is a helper inside another solution, its O(u) storage generally becomes that solution's working state—do not make it disappear from the overall analysis.

For `reconstructedTotal`, let c be the passed `HashMap`'s capacity. Traversing entries costs O(u+c), with O(1) auxiliary space and a scalar output. For the map built above using default growth, c = O(u+1), so this is O(u+1). The general method accepts `Map`; another implementation's iteration cost must be analyzed accordingly. Integer arithmetic per entry is O(1).

For `lowercaseCounts`, let k be string length and sigma = 26. Initializing counters takes O(sigma), scanning takes O(k), and the result uses O(sigma) output with O(1) auxiliary space beyond it. Time is O(k+sigma), normally simplified to O(k+1) and constant result size for this fixed alphabet. Parameterizing the alphabet would require retaining sigma in the bound.

### 6.5 Alternatives, mistakes, and self-checks

Sorting creates equal-value runs, which can be counted without hashing after an O(n log n) sort; it is attractive if sorted order is also needed or the input is already sorted. A count array is faster to index and avoids boxing but can waste enormous space on sparse IDs. A map stores only observed values but has object/hash overhead.

Common mistakes: counting only membership, leaving stale counts when reusing an array, assigning null counts and then unboxing them, indexing a negative value directly, or comparing two count arrays with `==`. Compare array contents with `Arrays.equals`; for sigma slots this is O(sigma), with early exit possible. New primitive arrays start at zero; use `Arrays.fill(counts, 0)` when deliberately reusing an old one.

**Self-check H1:** Why do `"aab"` and `"abb"` have the same character set but different inventories?

**Self-check H2:** If the alphabet grows with the input, which “constant-space” claims must change?

## 7. Canonical Signatures and Grouping

### 7.1 First define what “equivalent” means

Suppose objects should share a group when some irrelevant detail differs. A canonical signature is a stable representative of the details that **do** matter. Think of a recipe inventory: ingredient order may be irrelevant, while ingredient quantities remain essential.

Recognition clues include grouping by rearrangement, identical feature sets, normalized forms, or a structural shape. Required assumption: the intended equivalence can be represented without losing relevant information. Similar-looking grouping tasks can require different keys:

| Equivalence rule | Suitable key | Counterexample to a weaker key |
| --- | --- | --- |
| Same lowercase character multiplicities | Entire 26-count vector, serialized unambiguously, or sorted characters | `ab` and `abb` have the same distinct letters but different counts |
| Same set of distinct lowercase letters | Ordered presence signature | This deliberately groups `ab` and `abb` together |
| Same length only | Length | It deliberately groups unrelated words of the same size |
| Exact spelling | Original immutable string | A frequency key would incorrectly merge different spellings |

A key is not justified because it passes examples. It needs both directions:

1. Equivalent inputs always produce equal keys, so a valid group is not split.
2. Equal keys imply equivalent inputs, so unrelated groups are not merged.

### 7.2 Baseline and optimization insight

A baseline assigns each new item by comparing it with a representative from every existing group. With N strings and G groups, that can require O(NG) equivalence tests; each test may itself inspect characters or build counts. Repeatedly comparing the same relevant features is the bottleneck.

Instead:

1. Decide which features completely characterize the equivalence.
2. Construct those features once for the current item.
3. Encode them in an immutable, content-equal key.
4. Map that key to the destination list and append the item.

The key is the optimization. `computeIfAbsent` is only the final API convenience.

### 7.3 Count signatures without ambiguity

The following helper constructs a lowercase multiset signature. It leaves the roadmap's complete frequency-based grouping solution for your independent implementation.

```java
static String lowercaseMultisetKey(String text) {
    int[] counts = lowercaseCounts(text);
    StringBuilder key = new StringBuilder();
    for (int count : counts) {
        key.append(count).append('#');
    }
    return key.toString();
}
```

Why the delimiter? If two adjacent count fields are `[1, 11]` or `[11, 1]`, plain decimal concatenation produces `111` for both. `1#11#` and `11#1#` are distinguishable. Retain zero fields and a fixed alphabet order so each count has a known position. The full signature has 26 fields; shortened examples show only the relevant leading fields.

For example, `"abb"` has counts `[1,2,0,...]`, so its key begins `1#2#0#...`. `"bab"` has the same key. `"ab"` begins `1#1#0#...`, so it is kept separate.

The helper is correct because `lowercaseCounts` preserves every character multiplicity, and the fixed-order delimiter encoding preserves every count. Equal signatures therefore mean equal inventories, and equal inventories produce equal signatures.

Let k be string length, sigma = 26, and S the serialized key length. Counting takes O(k+sigma); encoding, hashing if later used, and the final string copy each require O(S) work as a safe model. The helper takes O(k+sigma+S) time, O(sigma+S) auxiliary space for counters/builder, and O(S) returned-string storage. With fixed sigma and int counters, the fields are bounded; more generally, S is O(sigma·log(k+2)), which also gives O(k+1) time for fixed sigma. Do not call variable-length key construction O(1) merely because map lookup is expected constant in the number of entries.

An alternative is sorting the characters of a copy. For a word of length k, use an O(k log(k+1)) sorting bound plus O(k) copying/key work and O(k) newly allocated character/key storage. Exact costs depend on the primitive sorting path. This is simple and handles a domain that can be sorted, but it retains duplicate characters and must use the intended character model.

Across N multiset-grouping inputs with total character length L, counting and serializing costs O(L+N·sigma+sum S_i); include each key's hashing/equality work too. With fixed sigma, expected grouping time is O(L+N), including empty strings, under the stated hashing assumptions. Sorting each word instead gives a bound O(N+L+sum k_i log(k_i+1)); it sorts the characters within each word, not merely the N words. These are derivations to use when implementing the full practice solution yourself.

### 7.4 A complete grouping demonstration with a different rule

For this example, group labels by their **set of distinct lowercase letters**, ignoring multiplicity. This differs deliberately from anagram grouping. `"abb"`, `"ba"`, and `"baba"` belong together under this rule.

```java
static Map<String, List<String>> groupByDistinctLetters(String[] labels) {
    Map<String, List<String>> groups = new HashMap<>();
    boolean[] present = new boolean[26];
    for (String label : labels) {
        Arrays.fill(present, false);
        for (int i = 0; i < label.length(); i++) {
            char letter = label.charAt(i);
            if (letter < 'a' || letter > 'z') {
                throw new IllegalArgumentException("lowercase a-z required");
            }
            present[letter - 'a'] = true;
        }
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < present.length; i++) {
            if (present[i]) {
                key.append((char) ('a' + i));
            }
        }
        String signature = key.toString();
        groups.computeIfAbsent(signature, ignored -> new ArrayList<>())
                .add(label);
    }
    return groups;
}
```

We reset and reuse the boolean array, scan letters, then emit present letters in alphabet order. No delimiter is needed here: each emitted lowercase character represents itself and appears at most once. The signature is immutable because `toString()` creates a String independent of later builder changes. The [StringBuilder contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html) also explains why using the builder itself as a content-based key would be wrong.

`computeIfAbsent` creates a list only when the key has no non-null list. Appending then uses that stored list. `getOrDefault(key, new ArrayList<>()).add(label)` is not equivalent: on an absent key, it modifies a temporary list without inserting it into the map. No null mappings are used here.

### 7.5 Grouping dry run and correctness

Input: `["abb", "ba", "ac", "baba", ""]`.

| Label | Present letters | Canonical key | Group state after append |
| --- | --- | --- | --- |
| abb | a, b | `ab` | `ab → [abb]` |
| ba | a, b | `ab` | `ab → [abb, ba]` |
| ac | a, c | `ac` | `ab → [abb, ba]`; `ac → [ac]` |
| baba | a, b | `ab` | `ab → [abb, ba, baba]`; `ac → [ac]` |
| empty string | none | empty key | Previous groups plus empty key → `[empty string]` |

Before each label, the map groups exactly the previously processed labels by their distinct-letter sets. The boolean scan correctly computes the current set; alphabet-order emission gives the same key if and only if that set is the same. Appending places the label in exactly the correct group and preserves all existing memberships. Thus all labels are grouped correctly at termination.

The order of groups is unspecified because the outer map is a `HashMap`. The order inside each `ArrayList` follows input processing. If group encounter order is required, choose `LinkedHashMap` or explicitly order the result, and account for that decision.

### 7.6 Complexity, unsuitable keys, and self-checks

For the complete demonstration, let N be number of labels, L total characters, G number of groups, and sigma = 26:

1. All input character scans together cost O(L).
2. Each reset and alphabet-order signature scan costs O(sigma), for O(N·sigma).
3. Each new key has at most sigma characters; construction, hashing, and equal-key comparison are included in that term under expected hash behavior.
4. N appends take O(N) total amortized work across the group lists.

Expected total time is O(L+N·sigma), or O(L+N) for the fixed alphabet. A conservative collision upper bound is O(L+N²·sigma). Required output holds N references, G map/list structures, and at most G·sigma key characters: O(N+G·sigma), plus constant container overhead. Input strings are shared, not copied into group lists. Explicit working state beyond output is O(sigma) for the reusable presence array and the current builder/key. Counting temporary map/list resize buffers separately gives a conservative O(N+G+sigma) peak auxiliary bound; total additional live storage including output remains O(N+G·sigma+sigma). Over the whole execution, constructing N candidate signatures can allocate O(N·sigma) character storage cumulatively even though only one candidate at a time and G retained keys need be live in the abstract analysis.

An empty input has no groups; empty labels share an empty signature. Repeated identical labels are retained as separate list entries. Case folding, punctuation removal, and Unicode normalization would change the equivalence and are not silently performed.

Do not use a raw `int[]` as a content key: equal arrays created separately are different keys by default. Calling `Arrays.hashCode(counts)` and using just that int is not a collision-free canonical key either. Use a complete immutable encoding, or a carefully implemented immutable value wrapper with content equality/hash and defensive copying.

**Self-check G1:** Which of `ab` and `abb` should share a key under anagram grouping, and under distinct-letter grouping?

**Self-check G2:** Why can both a delimiter bug and a real hash collision occur, yet only the delimiter bug merges different logical groups incorrectly?

## 8. Prefix Sums and Range Queries

### 8.1 Turn repeated interval work into a boundary lookup

Suppose many questions ask for the sum of readings between two positions in an unchanged array. Think of a meter showing total consumption at each boundary: consumption between two readings is the later cumulative reading minus the earlier one.

The brute force scans each requested interval. For q queries with lengths d1, d2, ..., dq, time is O(d1+d2+...+dq), up to O(qn). If many intervals overlap, the same elements are added repeatedly.

Derive a reusable summary:

1. Let `prefix[b]` mean the sum of the first b elements, namely `[0, b)`.
2. The empty prefix is `prefix[0] = 0`.
3. Extending by one element gives `prefix[i+1] = prefix[i] + values[i]`.
4. The range `[from, to)` is the longer prefix with the earlier prefix removed: `prefix[to] - prefix[from]`.

Recognition clues: many static contiguous-range queries, overlapping subarray aggregates, or repeatedly recomputed earlier totals. Required assumptions: addition/subtraction represent the intended values and the input snapshot remains unchanged. This index is less attractive for one short query or frequent updates; dynamic range structures are later-topic additions, not prerequisites here.

### 8.2 Java implementation

```java
static long[] prefixSums(int[] values) {
    int size = Math.addExact(values.length, 1);
    long[] prefix = new long[size];
    for (int i = 0; i < values.length; i++) {
        prefix[i + 1] = prefix[i] + values[i];
    }
    return prefix;
}

static long rangeSum(long[] prefix, int from, int to) {
    if (from < 0 || from > to || to >= prefix.length) {
        throw new IndexOutOfBoundsException("invalid half-open range");
    }
    return prefix[to] - prefix[from];
}
```

`rangeSum` assumes `prefix` was produced by the builder and has not been altered. Its last valid boundary is the original input length, not the last input index. `Math.addExact` rejects a length increment that cannot fit int; actual allocation must still fit available memory. Long arithmetic safely accommodates sums of an int-array-length number of int elements. Changing the API to long input values would require a new overflow policy.

### 8.3 Dry run: boundaries are not elements

Input: `[3, -2, 5, 1]`.

```text
boundary:     0     1     2     3     4
elements:        3    -2     5     1
prefix:       0     3     1     6     7
```

| i | prefix[i] before extension | values[i] | prefix[i+1] |
| --- | --- | --- | --- |
| 0 | 0 | 3 | 3 |
| 1 | 3 | -2 | 1 |
| 2 | 1 | 5 | 6 |
| 3 | 6 | 1 | 7 |

| Query | Boundary subtraction | Result |
| --- | --- | --- |
| `[1, 3)` → elements -2, 5 | prefix[3] - prefix[1] = 6 - 3 | 3 |
| `[0, 4)` → all elements | prefix[4] - prefix[0] = 7 - 0 | 7 |
| `[2, 2)` → empty range | prefix[2] - prefix[2] | 0 |

If a statement uses inclusive element indices `[left, right]`, translate to boundaries `[left, right+1)`. Do not mix formulas from different conventions.

### 8.4 Correctness and cost derivation

Before the construction iteration at i, `prefix[i]` is the sum of exactly `[0, i)`, and all earlier entries are correct. This holds for i = 0 because the sum of no elements is zero. Adding `values[i]` extends the prefix by exactly one element, preserving the definition. The whole table is correct at termination.

For a query, decompose the longer prefix:

```text
sum([0, to)) = sum([0, from)) + sum([from, to))
```

Subtracting removes exactly the unwanted earlier elements. Negative values do not invalidate this identity; prefix sums need not be increasing.

Let n be input length. Allocating/initializing n+1 long slots is O(n+1), and the builder performs n constant-time additions, so preprocessing is O(n+1). It returns O(n+1) table storage and uses O(1) auxiliary space beyond that returned table. Each range query performs constant-time checks, two reads, and one subtraction: O(1) time, O(1) auxiliary, scalar output. Building once for q queries gives O(n+q+1) total time, O(n+1) retained index storage, and optionally O(q) output if you save all answers.

The original input is O(n); the application's total live memory includes it and the prefix index. If the table is called auxiliary state of the overall query service, report O(n) auxiliary for that application. Do not rebuild the table for every query.

### 8.5 Limits, alternatives, and a useful connection

For one short query, scan the range directly and avoid preprocessing. If the array changes after construction, the index describes the old values; a simple update may require changing many later prefixes. Sorting is not an alternative for these positional queries because it changes which elements occupy each interval.

Prefix sums do not automatically make every subarray problem linear: enumerating all pairs of boundaries still creates O(n²) candidate ranges. They make each candidate's sum cheap.

**Optional cross-pattern connection:** if a task asks for how many earlier boundaries form a target-sum subarray ending at the current boundary, rearrange `currentPrefix - earlierPrefix = target`. You need counts of `currentPrefix - target`. The empty prefix must be represented, and query/update timing again matters. This extension combines Sections 5, 6, and 8; implementing it is optional depth, not an added roadmap requirement.

Do not substitute an ordinary positive-sum sliding window when arbitrary negative numbers are allowed: extending a window may lower its sum, and shrinking may raise it. Similar wording about subarrays does not imply the same algorithm.

**Self-check P1:** Why does an n-element array have n+1 prefix boundaries, and what formula handles an empty range?

**Self-check P2:** Why does a correct prefix table survive negative values but become stale after input mutation?

## 9. Prefix/Suffix Products and Aggregation

### 9.1 Derive the two sides before choosing arrays

Suppose a contiguous block is temporarily removed from a production line, and you need the product of all remaining multipliers. Think of preparing summaries for everything to the left of the block and everything to its right.

For one query, simply scan the values outside the block: O(n) time, O(1) auxiliary space. For many removals on the same static input, repeatedly multiplying the same sides is wasteful. For every boundary, precompute the left product and the right product once.

Recognition clues: “all elements except a position/block,” repeated prefix/suffix aggregates, exclusion without disturbing order, or a value that combines independent left and right contributions. Assumptions: the aggregate can be combined associatively, a useful identity exists for an empty side, arithmetic fits the chosen representation, and the input is static during queries.

For multiplication, the identity is **1**, because `1 × x = x`. Define:

```text
before[b] = product of values[0, b)
after[b]  = product of values[b, n)

before[0] = 1
after[n]  = 1

product outside [from, to) = before[from] * after[to]
```

This combines disjoint regions; no removed element is used. For a single excluded position i, the right boundary is i+1. Derive that boundary yourself rather than memorizing an array-offset formula.

### 9.2 Why a total product divided by the removed value is fragile

Division requires an invertible contribution. Zero cannot be divided out, and integer division is not a general inverse unless all necessary arithmetic is exact. A total product may overflow even if some requested exclusions would fit. A “divide total by each value” shortcut also fails when the problem disallows division.

Prefix/suffix multiplication does not divide, so zeros and negative values follow the same multiplication rules. It still needs an overflow policy; removing division does not make arithmetic unlimited.

### 9.3 Java implementation for products outside a block

The record packages two returned preprocessing arrays. It is not deeply immutable: do not modify its arrays between construction and queries. As with prefix sums, callers must preserve the index's meaning.

```java
record SideProducts(long[] before, long[] after) {}

static SideProducts sideProducts(int[] values) {
    int n = values.length;
    int size = Math.addExact(n, 1);
    long[] before = new long[size];
    long[] after = new long[size];
    before[0] = 1L;
    for (int i = 0; i < n; i++) {
        before[i + 1] = Math.multiplyExact(before[i], values[i]);
    }
    after[n] = 1L;
    for (int i = n - 1; i >= 0; i--) {
        after[i] = Math.multiplyExact(values[i], after[i + 1]);
    }
    return new SideProducts(before, after);
}

static long productOutside(SideProducts index, int from, int to) {
    int n = index.before().length - 1;
    if (from < 0 || from > to || to > n) {
        throw new IndexOutOfBoundsException("invalid half-open range");
    }
    return Math.multiplyExact(index.before()[from], index.after()[to]);
}
```

`productOutside` assumes the record came from `sideProducts`. `Math.multiplyExact` throws `ArithmeticException` instead of silently wrapping an overflowing long product; this is the declared numeric contract, supported by the [Java 21 Math API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html). The preprocessing contract requires **all constructed prefixes/suffixes**, and any requested combined result, to fit long. That is stronger than requiring only selected final answers to fit. A later zero does not prevent an earlier prefix from overflowing. Arbitrary-precision products would require a different representation and bit-cost analysis.

### 9.4 Dry run with zero and a negative multiplier

Input `[2, 0, -3, 4]`:

| Boundary b | before[b], built left to right | after[b], built right to left |
| --- | --- | --- |
| 0 | 1, empty left side | 0 = 2 × 0 × -3 × 4 |
| 1 | 2 | 0 = 0 × -3 × 4 |
| 2 | 0 = 2 × 0 | -12 = -3 × 4 |
| 3 | 0 = 2 × 0 × -3 | 4 |
| 4 | 0 = 2 × 0 × -3 × 4 | 1, empty right side |

Construction directions:

```text
before: 1 --*2--> 2 --*0--> 0 --*-3--> 0 --*4--> 0
after:  0 <--*2-- 0 <--*0-- -12 <--*-3-- 4 <--*4-- 1
```

| Removed block | Remaining parts | Query | Result |
| --- | --- | --- | --- |
| `[1, 2)` → only the zero | `[2]` and `[-3, 4]` | before[1] × after[2] | -24 |
| `[1, 3)` → zero and -3 | `[2]` and `[4]` | before[1] × after[3] | 8 |
| `[0, 4)` → everything | Two empty sides | before[0] × after[4] | 1 |
| `[2, 2)` → nothing | The whole input remains | before[2] × after[2] | 0 |

### 9.5 Correctness and query complexity

For the forward loop, before computing index i+1, `before[i]` is the exact product of `[0, i)`. Identity 1 initializes the empty prefix; multiplying by `values[i]` adds exactly the next element. For the backward loop, `after[i+1]` is the exact product of `[i+1, n)`; multiplying the current value constructs `[i, n)`. These statements prove both tables by induction over their traversal directions.

The retained regions outside `[from, to)` are precisely `[0, from)` and `[to, n)`. Combining their products uses every retained value exactly once and no removed value. Empty regions work because their product is 1. Checked multiplication either returns the exact representable result or reports overflow.

Let n be input length. Initializing two n+1 arrays and executing two n-step passes costs O(n+1) time. The returned preprocessing index uses O(n+1) storage; auxiliary space beyond it is O(1). A query checks bounds, reads two values, and multiplies: O(1) time, O(1) auxiliary, scalar output. For q queries, total time is O(n+q+1), with O(n+1) retained index storage. Checked arithmetic is O(1) per operation in this fixed-width model; early exceptions can terminate a failing call sooner.

### 9.6 Constant auxiliary space for per-position outputs

Two full tables are useful for arbitrary later queries, but they are not mandatory for a one-time per-position output. Often the output array itself can temporarily store every left-side summary while a single scalar accumulates the right side.

To demonstrate the technique without completing the roadmap's product problem, compute the **maximum value at any other position**. At least two values are required.

```java
static int[] maximumAtOtherPositions(int[] values) {
    if (values.length < 2) {
        throw new IllegalArgumentException("at least two values required");
    }
    int[] result = new int[values.length];
    int leftMax = Integer.MIN_VALUE;
    for (int i = 0; i < values.length; i++) {
        result[i] = leftMax;
        leftMax = Math.max(leftMax, values[i]);
    }
    int rightMax = Integer.MIN_VALUE;
    for (int i = values.length - 1; i >= 0; i--) {
        result[i] = Math.max(result[i], rightMax);
        rightMax = Math.max(rightMax, values[i]);
    }
    return result;
}
```

The identity for maximum over int values is `Integer.MIN_VALUE`: combining it with any valid int leaves that int unchanged. Because at least one other position exists, each final result corresponds to a real value, even if all values equal that minimum.

For input `[5, 1, 7, 3]`, write `M` for `Integer.MIN_VALUE`:

| Forward index | Left maximum stored in result[i] | leftMax after including current |
| --- | --- | --- |
| 0 | M | 5 |
| 1 | 5 | 5 |
| 2 | 5 | 7 |
| 3 | 7 | 7 |

Temporary output: `[M, 5, 5, 7]`.

| Backward index | rightMax before current | Combine with stored left maximum | Final result[i] |
| --- | --- | --- | --- |
| 3 | M | max(7, M) | 7 |
| 2 | 3 | max(5, 3) | 5 |
| 1 | 7 | max(5, 7) | 7 |
| 0 | 7 | max(M, 7) | 7 |

Final output: `[7, 7, 5, 7]`. The current input value is folded into the running summary **after** producing its own output, so the current position never contributes to itself.

Correctness follows from two invariants: the forward pass stores the maximum strictly left of i; during the backward pass, `rightMax` is the maximum strictly right of i. Combining them gives the maximum outside exactly that position, and updating the right scalar prepares the next earlier position.

For n elements, output initialization plus two constant-work passes is O(n) time. The two running maxima and indices are O(1) auxiliary space; output is O(n). Input is unchanged and occupies O(n), so total storage remains O(n). There is no hidden suffix array.

### 9.7 Transfer the storage idea and check the assumptions

For multiplicative per-position output, derive the same left-summary/right-scalar schedule using identity 1 and multiplication instead of maximum. Account for zeros and numeric bounds before writing the Java solution yourself. One zero makes only exclusions that remove that zero potentially nonzero; with at least two zeros, excluding one position still leaves a zero. Negative signs require no separate division rule.

Do not say “prefix/suffix always needs O(n) auxiliary space.” It depends on what must be retained:

| Requirement | Storage choice | Cost beyond required output |
| --- | --- | --- |
| One exclusion query | Scan the retained sides directly | O(1) auxiliary, O(n) time |
| Many arbitrary block-exclusion queries | Retain before/after tables | O(n) retained index, O(1) per query |
| One per-position result array | Store left summaries in output; carry one right summary | Often O(1) auxiliary beyond O(n) output |
| No associative combine or no useful identity | The simple recipe may not apply | Derive a different state rather than forcing the template |

Associative does not mean commutative. If order matters, combine the left region before the right region. String concatenation is associative but not constant-cost; a linear number of concatenations can still copy quadratic total characters. These scalar bounds apply to the fixed-width aggregate operations analyzed above.

Common mistakes: use 0 as product identity, include the current item before writing an exclusive summary, divide through zeros, claim output allocation is constant space, or compute an unnecessary final full product that overflows even though every needed partial product fits. A numerical contract must cover the intermediates actually computed.

**Self-check A1:** Why does an empty sum use 0 while an empty product uses 1?

**Self-check A2:** How does the backward pass avoid an entire suffix array for per-position output, and why must it update the scalar after writing the current answer?

## 10. Consecutive Runs and Sequence-Head Expansion

### 10.1 Separate value adjacency from input positions

Suppose unordered integer readings describe numbered houses that exist on a street. Report each maximal run of existing consecutive numbers. `[3, 8, 2, 3, 1, -2, -1]` contains runs `[-2, -1]`, `[1, 3]`, and `[8, 8]`. Input positions and duplicates do not change which numbered houses exist.

A consecutive **value run** is different from a contiguous subarray and from an increasing subsequence that preserves input order. Recognize this pattern when integer adjacency means x followed by x+1, input order is irrelevant, and duplicates should not extend a run. It is unsuitable if positions, occurrence counts, or an arbitrary non-unit successor relation define the problem instead.

### 10.2 Baseline and the repeated-work trap

Sorting and scanning adjacent distinct values works in O(n log n) time, or O(n) time if input is already sorted. But suppose you want a hash-based method for unordered data.

Putting values in a set makes neighbor lookup cheap. That alone is not enough: if you expand a run starting from **every** member, a run of u consecutive values creates:

```text
u + (u - 1) + ... + 1 = O(u²) neighbor checks
```

You keep walking the same suffixes. The optimization is to start only at a house with no existing predecessor—every street segment has exactly one first house.

### 10.3 Derivation and implementation

1. Deduplicate the input into a set; multiplicity is irrelevant here.
2. For each distinct value x, ask whether x-1 exists.
3. If it does, this value is inside a run that will be handled from an earlier head; skip expansion.
4. Otherwise x is the unique head of its run. Advance while the next integer exists.
5. Report the maximal interval. Iterate the **set**, not repeated input occurrences of the same head.

```java
record Run(int start, int end) {}

static List<Run> consecutiveRuns(int[] values) {
    Set<Integer> existing = new HashSet<>();
    for (int value : values) {
        existing.add(value);
    }
    List<Run> runs = new ArrayList<>();
    for (int start : existing) {
        if (start != Integer.MIN_VALUE && existing.contains(start - 1)) {
            continue;
        }
        int end = start;
        while (end != Integer.MAX_VALUE && existing.contains(end + 1)) {
            end++;
        }
        runs.add(new Run(start, end));
    }
    return runs;
}
```

Returned run order is unspecified; no sorting is performed. The integer-limit guards prevent wraparound from treating `Integer.MAX_VALUE` and `Integer.MIN_VALUE` as adjacent. If you need a run length, derive it as `(long) end - start + 1` when the range can demand a wider type. The exact longest-run practice interface remains for your independent implementation.

### 10.4 Dry run

For `[3, 8, 2, 3, 1, -2, -1]`, the distinct set is `{-2, -1, 1, 2, 3, 8}`. The rows use ascending order for readability, not because `HashSet` promises it.

| Candidate | Predecessor exists? | Expansion / decision | Output contribution |
| --- | --- | --- | --- |
| -2 | No -3 | -2 → -1; 0 missing, stop | Run(-2, -1) |
| -1 | Yes, -2 | Skip | None |
| 1 | No 0 | 1 → 2 → 3; 4 missing, stop | Run(1, 3) |
| 2 | Yes, 1 | Skip | None |
| 3 | Yes, 2 | Skip | None |
| 8 | No 7 | 9 missing, stop | Run(8, 8) |

Duplicate 3 is inserted only once. Empty input returns no runs; a singleton becomes a one-element run.

### 10.5 Correctness: coverage, maximality, and uniqueness

During construction, after each input element the set contains exactly the distinct values seen so far. Thus the completed set represents all value membership correctly.

For any head, before each expansion step, every value from `start` through `end` is present and consecutive. Moving to `end+1` only when it exists preserves that property. When expansion stops, there is no representable next member, so the right boundary is maximal. The missing predecessor proves the left boundary is maximal too.

Every value belongs to exactly one maximal run. That finite run has one head, and the outer set traversal visits that head once. Non-head values do not expand. Therefore every run is produced, no invalid run is produced, and no run is duplicated, regardless of set iteration order.

### 10.6 Count total work instead of multiplying loop syntax

Let n be input length, u distinct values, r runs, and c set capacity.

1. Building the set performs n expected-amortized O(1) additions: expected O(n).
2. The outer set traversal costs O(u+c), with one predecessor check per distinct value.
3. Across all expansions, advancing from one value to its successor happens at most u-r times because runs are disjoint.
4. At most one terminating failed successor check occurs per run, except when the maximum-int guard ends it directly.
5. Appending r output records takes O(r) total amortized list work.

With default capacity growth, c = O(u+1), so expected total time is O(n+u+r+1), simplifying to O(n+1). The `while` is nested syntactically but performs only O(u) total successful advances. With pathological membership costs, a conservative upper bound is O(n²+1), not an unconditional linear guarantee.

Auxiliary set storage is O(u+1); output is O(r+1), including run records/list backing storage. Input is O(n). The extra live structures therefore use O(u+r+1). Integer boxing and table nodes add constant-size overhead per retained value; each record stores two primitive int endpoints.

### 10.7 Alternatives, pitfalls, and self-checks

Sorting is a good alternative when order is also needed, hashing memory is undesirable, or input is already sorted. Skip duplicate values while scanning sorted neighbors. If preserving the input, include the copy; even when mutating, do not promise that Java library sorting has universal O(1) auxiliary space.

Two subtle quadratic traps are expanding from every value and iterating the original array when the smallest value has many duplicates. With many copies of the same head and a long run, the second approach repeats the same complete expansion. Iterating unique values fixes both repetition and output duplication.

**Self-check R1:** Why does a run with u members require only O(u) total expansion work in this algorithm, even though a `while` appears inside a `for`?

**Self-check R2:** Why do both integer-limit guards and deduplication matter for correctness or cost?

## 11. Comparing Approaches and Bounded Buckets

### 11.1 A bucket is a directly addressable category

Suppose scores are known to lie between min and max, and you need them in ascending order. Rather than repeatedly comparing scores, allocate one mailbox for each possible score and count how many belong there. The mailbox index is `score - min`.

This is **counting sort for a bounded integer domain**, a supporting demonstration of the roadmap's bucket-counting comparison. It is not an extra category or a required custom replacement for Java's sorting library.

Baseline options include repeatedly selecting the smallest remaining value, which can take O(n²), or comparison sorting in O(n log n). The optimization uses additional information: only R = max - min + 1 values are possible. Count once, then emit buckets in numeric order. There are no pairwise comparisons between input elements.

Consider it when R is small enough for both time and memory, especially with many repeated bounded values. It is unsuitable for sparse IDs spanning most of the int range, unknown ordering domains, or records whose attached payloads must be retained without a more complete stable counting-sort design.

```java
static int[] countingSortedCopy(int[] values, int min, int max) {
    if (min > max) {
        throw new IllegalArgumentException("min must not exceed max");
    }
    int width = Math.toIntExact((long) max - min + 1);
    int[] counts = new int[width];
    for (int value : values) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("value outside domain");
        }
        counts[value - min]++;
    }
    int[] result = new int[values.length];
    int write = 0;
    for (int offset = 0; offset < width; offset++) {
        int copies = counts[offset];
        for (int copy = 0; copy < copies; copy++) {
            result[write++] = min + offset;
        }
    }
    return result;
}
```

The subtraction for domain width is performed in long before narrowing. `Math.toIntExact` rejects a width that does not fit int; even a representable width may be far too large for practical memory. The caller must choose a feasible domain. Given a valid width and validated input value, `value - min` is a valid int index in `[0, width)`.

### 11.2 Dry run, correctness, and total loop work

Input `[3, -1, 3, 0]`, domain -1 through 3: R = 5.

| Numeric value | -1 | 0 | 1 | 2 | 3 |
| --- | --- | --- | --- | --- | --- |
| Bucket index | 0 | 1 | 2 | 3 | 4 |
| Final count | 1 | 1 | 0 | 0 | 2 |
| Emitted copies | -1 | 0 | none | none | 3, 3 |

Output: `[-1, 0, 3, 3]`.

The counting invariant from Section 6 proves each bucket's frequency. During emission, before a bucket is processed, the output prefix contains exactly all input occurrences in earlier buckets, in ascending order. Emitting precisely this bucket's copies extends that sorted prefix correctly. After the final bucket, all and only the original occurrences have been emitted.

Let n be input length and R the domain width. Initializing counts is O(R), counting is O(n), initializing output is O(n), and traversing buckets is O(R). Although emission has a nested loop, the total number of copies written is the sum of all counts, n. Total time is O(n+R), auxiliary counter storage O(R), output O(n), and total live storage including input O(n+R). There is no hashing assumption. Integer comparisons, index arithmetic, and each write are O(1).

Negative values work because of the offset. Empty input returns an empty result after allocating the declared domain counters. This is a useful reminder that O(n) is inaccurate when n = 0 but R is large. The algorithm preserves multiplicity and leaves the input unchanged, but it reconstructs values rather than preserving the identities/order of equal attached records.

### 11.3 HashMap versus HashSet versus frequency arrays

| Choice | Required operation / retained state | Typical basic operation | Storage | Assumptions and tradeoff |
| --- | --- | --- | --- | --- |
| `HashSet` | Membership of observed values | Expected O(1) | O(u) | Loses counts and positions; no iteration-order guarantee |
| `HashMap` | Value → index/count/group | Expected O(1) structural lookup/update | O(u) entries plus stored payloads | Key hashing/equality and payload construction may cost more |
| Frequency array | Counts in a known indexed domain | Deterministic O(1) after initialization | O(R) or O(sigma) | Must allocate/reset the entire range; can waste space |
| Sorted array | Order, adjacent duplicates, binary search | O(log n) search; O(n) scan | Input/copy plus library workspace | Sorting costs O(n log n), changes positions, and preserves more order information |

Use `TreeMap`/`TreeSet` when sorted keys or nearest-neighbor navigation are required; typical basic key operations cost O(log u), plus comparison costs. They are useful alternatives already covered in the existing references, not a prerequisite to implement manually here.

### 11.4 Hashing versus sorting versus bucket counting

| Method | Typical total time for processing n integers | Memory to account for | Prefer when |
| --- | --- | --- | --- |
| Pairwise scanning | O(n²) | O(1) working state, plus output | n is tiny or extra memory is constrained |
| Hash summary | Expected O(n), with pathological caveats | O(u) table and any payload/output | Need reusable lookup, counts, or grouping without numeric order |
| Sort then scan | O(n log(n+1)) | O(n) if copying; also library sort workspace | Need order, adjacent runs, or sorted two-pointer reasoning |
| Bounded count buckets | O(n+R) | O(R) counters plus output | R is feasible relative to n and memory budget |

Count initialization, not just input visits. For 20 IDs scattered between -1,000,000,000 and 1,000,000,000, a bucket for every possible ID is impractical; a map can retain at most 20 observed entries. For millions of scores in a small fixed range, direct counters may be an excellent fit.

Sorting is not universally worse because O(n log n) grows faster than expected O(n): it may satisfy extra ordering requirements, avoid hashing assumptions, and perform well in practice. Likewise, hash storage can be worthwhile for repeated queries but unnecessary for a single scan. Choose from the full contract, not from a preferred container.

### 11.5 Repeated scans versus reusable summaries

| Repeated work | Reusable state | Cost change and condition |
| --- | --- | --- |
| Scan earlier values at every arrival | Seen set or prior-value map | O(n²) → expected O(n); choose membership/count/index payload correctly |
| Recount each observed value | Frequency map/array | O(nu) → expected O(n), or deterministic O(n+R) |
| Scan q static intervals | Prefix sums | Up to O(qn) → O(n+q); no arbitrary input mutation |
| Multiply retained regions for q exclusions | Before/after products | O(qn) → O(n+q); intermediate products must fit |
| Expand from every member of a run | Set + head-only expansion | Expected O(n+u²) → expected O(n+u), with each distinct head processed once |

Preprocessing is an investment: for one small query it may cost more than the direct scan. Its value grows when state can answer many later questions without rebuilding.

### 11.6 Two meanings of “bucket”

Value buckets are indexed by the input value's bounded offset. **Frequency buckets** are indexed by how often a value occurs. With n input items, a positive frequency lies between 1 and n even if the original values are huge IDs.

After constructing a frequency map, values can be placed into buckets indexed by count, then buckets can be scanned from high count to low count. This is the idea to investigate later for the roadmap's Additional top-frequency practice. Initialization/scanning costs O(n), placing u values costs O(u), and counting costs expected O(n); a direct bucket representation uses O(n+u) state. Derive the required tie handling and output yourself. This is different from allocating a bucket for every possible original ID.

**Self-check B1:** Why can O(n+R) be much worse than O(n log n) for sparse large IDs?

**Self-check B2:** Why does bucket emission cost O(n+R), rather than O(nR), despite the nested loops?

## 12. Java-Specific Pitfalls

The examples in this table are deliberately labelled bug fragments or design errors, not recommended implementations. The full method implementations above use their stated domains and numeric contracts.

| Mistake | Concrete failure | Better reasoning |
| --- | --- | --- |
| Reuse the current index | Insert 4 before searching for target 8 in a one-element input | Query a map representing only earlier indices, then insert |
| Always ban equal-valued partners | Reject `[4, 4]` for target 8 | Equal values at different indices may be legitimate partners |
| Store one index when counting all partners | Two earlier 4s should contribute 2, not 1 | Choose counts when multiplicity matters |
| Compare wrapper references | Use `a == b` on two `Integer` references | Use unboxed values or `equals`; wrapper caching can hide the error on small values |
| Compare arrays by identity | Separate arrays with the same counts fail `a.equals(b)` | Use `Arrays.equals(a, b)` for contents |
| Use raw arrays as content keys | `Map<int[], ...>` keeps separately allocated equal-content arrays as distinct keys | Encode immutable contents or implement a value wrapper correctly |
| Treat an int hash as a complete signature | Different count vectors can have the same `Arrays.hashCode` | Keep the complete key; let the map handle hash collisions with equality |
| Omit count delimiters/positions | `[1,11]` and `[11,1]` become the same decimal text | Fixed field order with unambiguous separators and retained zero fields |
| Use mutable content keys | Insert a list as key, then change its elements | Keep equality/hash-relevant state immutable, or remove before changing/reinsert |
| Expect content equality from `StringBuilder` | Two builders containing `ab` are not equal by text | Convert to immutable String keys; builder equality is identity-based |
| Believe `final int[]` or a record deeply freezes an array | Mutate an array element through another alias | `final` freezes the reference, and records retain component semantics; copy/wrap correctly |
| Assume `HashMap` remembers input order | Tests depend on group iteration matching first occurrence | Use `LinkedHashMap` for that contract or order the output explicitly |
| Treat `getOrDefault` as insertion | Append to a fallback list but never store it | Use `computeIfAbsent` for stored group creation |
| Store null counts | Unboxing `getOrDefault`'s present null throws | Use non-null counts; a fallback applies only to absent mappings |
| Misapply a frequency alphabet | Index `A - 'a'` or an emoji into 26 slots | Validate the domain; choose a suitable character model |
| Mix inclusive indices with prefix boundaries | `prefix[right] - prefix[left]` omits the inclusive right endpoint | For inclusive `[left,right]`, use `prefix[right+1] - prefix[left]` |
| Cast after overflow | `(long) (a * b)` has already multiplied as int | Promote first, as `(long) a * b`, then still check long range |
| Compute a complement as int | `target - current` wraps at an extreme | Promote before subtraction or prove the domain excludes overflow |
| Divide through zero | Total product is zero but an excluded-zero result may be nonzero | Combine the two retained sides without division |
| Expand every consecutive value | The same suffix is walked repeatedly | Expand only from unique sequence heads |
| Wrap integer neighbors | MAX_VALUE + 1 becomes MIN_VALUE | Guard integer bounds or use a wider neighbor representation |
| Hide copying inside “one operation” | New substrings or growing immutable concatenations repeat character copies | Count copied lengths; use indices/builder where appropriate |

An array key's default hash/equality is identity-based: changing its elements does not magically make it a content key. A mutable **content-based** key, such as a list used as a key, can change its hash/equality and break future lookup. These are different problems. A record containing an array does not automatically supply deep array-content equality.

For immutable keys, content equality still costs work. Building a signature, hashing a fresh String, and comparing equal-length String contents may all inspect characters. Cached hashes do not eliminate allocation and scanning of each new signature. Likewise, `substring` normally copies its selected characters in modern Java; repeated growing string concatenation can copy 1+2+...+S characters overall while retaining only O(S) peak live text. Review [Java Strings](./Java_String.md) for API details.

**Self-check J1:** Why is an array key unsuitable for content grouping even when you promise never to mutate it?

**Self-check J2:** Why does casting an overflowed expression to long fail to repair its value?

## 13. Pattern Recognition Cheat Sheet

### 13.1 Ask what future work the current state must support

Before choosing an API, ask:

1. Does original position/order matter, or only values and their multiplicities?
2. Is the question about existence, one witness, a count, a whole group, or a range aggregate?
3. Must the state represent the whole processed prefix, a fixed static snapshot, or something narrower?
4. Is there a small domain, useful sorted order, or a computable counterpart?
5. Can disjoint summaries be combined, and does the combine have an identity or an inverse?
6. What could make the proposed representation lose necessary information?

| Statement characteristics | Candidate pattern | State / invariant | Assumption to verify | Consider an alternative when |
| --- | --- | --- | --- | --- |
| “Already appeared,” duplicate, unique membership | Seen-value tracking | Set equals distinct values in processed prefix | Value equality defines sameness | Need multiplicity, indices, sorted neighbors, or a bounded recent history |
| Pair relation with a derivable missing amount | Complement lookup | Prior value → useful index/count | Distinct-index timing and arithmetic range | A sorted scan fits better or compatible partners form a broad range |
| “How many,” inventory, available copies | Frequency counting | Bucket equals processed occurrence count | Correct domain and multiplicity semantics | Order/positions, rather than counts, determine validity |
| Group despite irrelevant order/features | Canonical grouping | Key is equivalent if and only if intended category is equivalent | Encoding is immutable and unambiguous | Comparing a few tiny items is cheaper or no compact complete key is available |
| Many static contiguous sums | Prefix sums | prefix[b] = aggregate of `[0,b)` | Snapshot is static; subtraction valid | One query, frequent updates, or a noninvertible aggregate |
| Exclude a position/block; combine two sides | Prefix/suffix aggregation | Left/right summaries cover disjoint retained regions | Associative combine, identity, safe intermediates | A direct scan or invertible total is simpler |
| Unordered consecutive integer values | Sequence-head expansion | Only missing-predecessor values launch disjoint runs | Value adjacency, duplicates irrelevant | Original-position contiguity or ordered subsequences are required |
| Small dense numeric domain | Direct buckets | Offset maps each possible value to one bucket | Feasible R in time and memory | IDs are sparse across a huge range |

### 13.2 Similar words, different algorithms

- **“Duplicate”** may mean any repeated value, a repeat within a recent window, or repeated content across objects. A whole-prefix integer set solves only the first directly; the others need a narrower state or correct content equality.
- **“Pair sum”** may ask for one pair, all index pairs, or distinct value pairs. These require different payloads and duplicate rules even when the complement equation is identical.
- **“Anagram” versus “same letters”** depends on whether multiplicity matters. Presence bits can be correct for one and wrong for the other.
- **“Consecutive”** can mean adjacent numeric values, contiguous array positions, or ordered subsequence elements. Clarify that meaning before choosing a set.
- **“Range aggregate”** does not imply subtraction works. Sums have an inverse; maxima do not. Products with zeros are another warning against an unjustified inverse.
- **“Linear scan”** does not guarantee O(n): a scan that creates an increasingly long signature or calls a linear membership operation each iteration may be quadratic.

**Self-check M1:** Which question would you ask before replacing a value-to-count map with a set?

**Self-check M2:** How could a correct algorithm for all previously seen values become incorrect when the statement changes to “the last w arrivals”?

## 14. Interview Self-Assessment and Short Answer Key

### 14.1 Explain a solution in a reusable order

Use this short speaking structure on an unfamiliar problem:

> “The baseline repeats ____. I will store ____ because the next iteration needs ____. Before iteration i, the state means ____. The update preserves that because ____. At completion ____. The cost is ____ under these assumptions, and this edge case would break an alternative approach.”

Then write the Java code, trace a normal case and a risk-focused boundary case, and explain the memory convention. Compare at least one alternative, such as sorting or a bounded counter array. Do not declare an approach optimal without discussing the model and constraints.

Three transfer checks, without coding a full workbook:

1. A task changes from returning one compatible earlier item to the number of compatible earlier items. Explain the payload change, query timing, duplicates, and possible total-count range.
2. Two strings have the same distinct characters but different multiplicities. Propose separate canonical keys for two possible equivalence rules and explain both directions of each key's correctness.
3. You can answer a single static interval query by scanning it. Explain what changes when there are many queries, when the data changes, and when the aggregate changes from sum to maximum or product.

These explanations should emerge from state and assumptions, not from recognizing a familiar problem title. Repeated success on unfamiliar variants is the roadmap's readiness evidence; familiarity with these examples is not enough.

### 14.2 Short answer key

Try the questions at the end of each teaching section before opening these answers. They are reasoning checkpoints, not scripts to memorize.

| Checks | Key ideas |
| --- | --- |
| F1 / F2 | One membership probe selects a bucket; iteration must visit the table's buckets and entries. Output storage remains real even when excluded from auxiliary space. |
| T1 / T2 | Advancing write for rejected values leaves holes in the claimed valid prefix. `write <= read` prevents overwriting future input; appending accepted values in read order preserves stability. |
| S1 / S2 | The next decision needs only whether an earlier equal value exists. A set collapses repeated occurrences, so it cannot answer how many occurred. |
| C1 / C2 | Store membership for existence, a useful index for a witness, and counts for all earlier partners. Two equal values at distinct indices are valid; one occurrence cannot pair with itself. |
| H1 / H2 | The sets are equal but the a/b counts differ. A growing domain requires O(sigma) or O(R) storage/initialization rather than an unconditional constant-space claim. |
| G1 / G2 | `ab` and `abb` differ as multisets but agree as distinct-letter sets. A bad encoding makes keys truly equal; a hash collision leaves unequal keys distinguishable by equality. |
| P1 / P2 | There is one boundary before any element plus one after each element; an empty interval subtracts the same prefix from itself. The sum identity allows negative values, while mutations change what later prefixes should contain. |
| A1 / A2 | Identities leave an aggregate unchanged when one region is empty. The output retains left summaries and one scalar represents the right side; folding current input in too early includes the excluded position. |
| R1 / R2 | Disjoint runs are expanded only from their unique heads, so each distinct value participates in at most one expansion. Bounds prevent wraparound; deduplication prevents repeated heads and duplicate output. |
| B1 / B2 | R can dwarf n and consume prohibitive memory. The inner writes sum to the total input count n, not R writes per element per bucket. |
| J1 / J2 | Raw array equality identifies an object, not equal contents. Narrow arithmetic has already lost high bits before the later cast; promote operands before operating. |
| M1 / M2 | Ask whether counts or positions affect the answer. A whole-prefix set retains old values after they leave a recent window; the state must support expiration and possibly counts. |

### 14.3 From understanding to evidence

Explain a pattern without notes, reimplement its mechanism in a scratch environment, and compare against a simple baseline on small inputs. Then attempt the roadmap's problems independently, recording I/H/S results and a review date on their existing rows. A correct implementation of the wrong state does not earn readiness; neither does a fast familiar re-solve without transfer.

No additional per-problem tracker or progress checklist is maintained in this guide. The authoritative [dashboard and gates](./DSA_Interview_Roadmap.md#2-progress-dashboard), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and [revision process](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition) remain in the roadmap.

## 15. Key Takeaways, References, and Next Steps

The central habit is to derive the **meaning** of state before writing its update. Membership, counts, indices, canonical keys, prefixes, and sequence heads solve different repeated-work problems. Their usefulness depends on what information may safely be discarded.

Preserve an explicit invariant, define boundaries before using formulas, and explain why each occurrence or distinct value is processed only a bounded number of times. Count key construction, output, preprocessing, and Java library work. State expected hashing assumptions and numeric limits instead of hiding them inside an O(n) label.

### Repository references

- [Arrays & Hashing roadmap requirements and practice](./DSA_Interview_Roadmap.md#1-arrays--hashing).
- [Phase 0 foundations](./DSA_Interview_Roadmap.md#phase-0-foundations) and [Phase 1 context](./DSA_Interview_Roadmap.md#phase-1-array-reasoning-and-linear-patterns).
- [Java Arrays](./Java_Array.md), [Java Strings](./Java_String.md), [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md), and [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md) for detailed API/reference behavior.
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), especially sections 7, 11, 13–15, 18–20, for deeper analysis practice.

### External primary references

Java contracts were checked on **24 October 2026**, using Context7's Oracle Java SE 21 documentation and the official pages below. The roadmap supplies the local learning requirements; the educational variants, derivations, and cost analyses in this guide are original. The bounded counting-sort demonstration and optional target-subarray connection support the required concepts; they do not add mandatory NeetCode practice assignments.

- [NeetCode Roadmap](https://neetcode.io/roadmap): primary curriculum behind the repository roadmap.
- [Java 21 HashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) and [HashSet](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html): performance assumptions, ordering, and iteration costs.
- [Java 21 Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html) and [Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html): lookup/group-creation semantics and equality/hash contracts.
- [Java 21 Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html): array equality/hash helpers, sorting, and filling.
- [Java 21 String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) and [StringBuilder](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html): immutable text, content equality, and key construction.
- [Java 21 Math](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html): checked arithmetic and narrowing helpers.

### Validation and limits

All 12 Java blocks, containing 15 methods and two nested records, were compiled together with Temurin JDK 21.0.12 using `javac --release 21 -Xlint:all -Werror`. A temporary harness outside the repository passed 66,446 checks: the documented dry runs, 1,000 seeded short integer-array cases against direct-scan/sorting baselines, 300 seeded string-group cases, integer extremes, duplicate-head stress, invalid bounds/domains, and checked-product overflow. The methods intended to preserve input were also checked for accidental mutation.

The checks validate examples and contracts; the operation counts and invariants establish the stated complexity/correctness arguments. Examples deliberately require non-null inputs, feasible allocations, lowercase `a`–`z` where stated, and the declared checked-long product range. Hashing bounds retain their distribution assumptions. No claim is made that a huge representable bucket domain fits memory or that long handles arbitrary products.

**Next step:** close this guide and explain one processed-prefix invariant, one complement update, and one prefix boundary formula. Rebuild a small mechanism from memory, test an adversarial case, then attempt an Essential Arrays & Hashing problem from the roadmap without the examples open. Use its practice/review record to distinguish recognition from recall; return to the specific concept that blocked you.
