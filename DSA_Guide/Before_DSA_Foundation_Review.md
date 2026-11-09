# Before DSA: Java & Math Foundation Review

**Audience:** Java 21 learner preparing to study the `Complexity` topic and then `Arrays_and_Hashing`  
**Purpose:** A focused prerequisite refresher and readiness check—not a replacement for the existing DSA topic guides.  
**Where to save in your repository:** `DSA_Guide/Before_DSA_Foundation_Review.md` (alongside your roadmap, if it is at the root of `DSA_Guide`).

> **How to use this file:** Read Sections 1–7, answer the questions in Section 8 *without looking at Section 9*, then complete the coding drills in Section 10. Revisit only the sections where your answers or code are incorrect. You do **not** need to master Trees, recursion-heavy algorithms, Heaps, or Backtracking before beginning Complexity.

## 1. What is actually required before the first topic?

Your first formal study topic should be **Complexity**, followed by **Arrays & Hashing**. To begin Complexity, you mainly need basic Java fluency and a few mathematical ideas:

| Priority | Review item | What you should be able to do |
| --- | --- | --- |
| **Must know now** | Java variables, methods, loops, conditionals | Read a short method and count what work it performs. |
| **Must know now** | `int`, `long`, arithmetic, overflow | Reason about numeric ranges and avoid unsafe intermediate calculations. |
| **Must know now** | Arrays and indexing | Traverse with correct boundaries; understand empty and singleton arrays. |
| **Must know now** | Mathematical growth | Recognize doubling, halving, and `1 + 2 + ... + n`. |
| **Must know now** | Objects, references, and mutation | Tell the difference between changing an object and reassigning a local reference. |
| **Helpful now** | Strings and standard collections | Recognize that library calls perform work; know the main use of a list, map, set, and deque. |
| **Preview only** | Big O, loop invariants, recursive complexity | Recognize the vocabulary; learn the full derivations in your `Complexity` and `Recursion` materials. |

**Suggested approach:** Do not spend a week memorizing every Java API. A single review session and one coding session are enough to *diagnose* missing foundations; use the existing topic references only to fix those gaps.

## 2. Java essentials that affect DSA

### 2.1 Primitive types and arithmetic

```java
int x = 7;
int y = 2;
System.out.println(x / y);        // 3: integer division truncates toward zero
System.out.println(x % y);        // 1: remainder
System.out.println(7.0 / 2);      // 3.5: floating-point division
```

- `int` is a 32-bit signed integer: `-2^31` through `2^31 - 1`.
- `long` is a 64-bit signed integer. Use it for potentially large counts, sums, products, and distances, but still check its limits.
- An `int` expression can overflow **before** it is assigned to a `long`.

```java
int a = 1_500_000_000;
int b = 2;
long wrong = a * b;          // Multiplication occurs as int; overflows first
long right = (long) a * b;   // First operand is long; result is 3_000_000_000L
```

**Important:** `(long) (a * b)` still casts the already-overflowed result; it is not equivalent to `(long) a * b`. Also avoid using `a - b` as a comparator for unrestricted integers—subtraction can overflow.

### 2.2 Methods, parameters, and return values

```java
static int square(int value) {
    return value * value;
}
```

`value` is a **local parameter**. Java passes parameters **by value**. If the parameter is an object reference, the *reference value* is copied: the method can mutate the referenced object, but assigning a new object to its local reference variable does not reassign the caller's variable.

### 2.3 Loops and boundaries

```java
int[] nums = {4, 8, 12};
for (int i = 0; i < nums.length; i++) {
    System.out.println(nums[i]);
}
```

- Array indices are `0` through `nums.length - 1`.
- The common index interval is **half-open**: `[start, end)` includes `start` and excludes `end`.
- `i < nums.length` works for an empty array; `i <= nums.length` attempts an invalid final index.
- If an algorithm starts at index `1`, ask whether it handles a zero-length or one-element array.

### 2.4 Arrays, copies, and aliasing

```java
int[] original = {10, 20};
int[] alias = original;
alias[0] = 99;
// original is now [99, 20]

int[] copy = java.util.Arrays.copyOf(original, original.length);
copy[0] = 7;
// original remains [99, 20]
```

An `int[]` holds primitive values; `Arrays.copyOf` creates a distinct primitive array. For a 2D array, however, cloning only the outer array **does not** copy each nested row:

```java
int[][] grid = {{1, 2}, {3, 4}};
int[][] shallow = grid.clone();
shallow[0][0] = 50;
// grid[0][0] is also 50: both outer arrays refer to the same first row
```

Remember three different questions: **Did we copy the outer container? Did we copy the contained objects? Did we mutate shared state?**

## 3. Strings and Java collections: a small toolkit

### 3.1 Strings

```java
String first = new String("abc");
String second = new String("abc");
System.out.println(first.equals(second)); // true: same contents
System.out.println(first == second);      // false: different objects in this example
```

- `String` is immutable: operations that change text produce another `String`.
- `s.length()` gives the number of UTF-16 **code units**, not always the number of Unicode characters a person perceives.
- `s.charAt(i)` retrieves one UTF-16 code unit.
- A `new int[26]` frequency table works for characters guaranteed to be `'a'` through `'z'`, **not** arbitrary text.
- Prefer `StringBuilder` for repeatedly appending many pieces of text. `toString()` gives a `String` result.
- Repeated `substring()` and String concatenation can create additional work and allocations; analyze them when they appear in a loop.

### 3.2 Choose a structure by the required operation

| Structure | Typical reason to choose it | Important caveat |
| --- | --- | --- |
| `int[]` | Fixed-size, fast indexed access | Length cannot change. |
| `ArrayList<Integer>` | Resizable sequence, fast indexed access | Inserting near the front shifts elements. |
| `HashSet<Integer>` | Unique values and expected fast membership tests | No sorted or stable iteration order guaranteed. |
| `HashMap<Integer, Integer>` | Value-to-count or value-to-index association | Lookup is *expected* O(1), not an unconditional worst-case guarantee. |
| `ArrayDeque<Integer>` | Stack or FIFO queue | Rejects `null`; learn its empty-state behavior. |
| `PriorityQueue<Integer>` | Repeated access to smallest-priority element | Not fully sorted when iterated; learn in the Heap topic. |

Example frequency map:

```java
java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
for (int value : new int[]{3, 3, 5}) {
    counts.put(value, counts.getOrDefault(value, 0) + 1);
}
// {3=2, 5=1}; iteration order is unspecified
```

`getOrDefault(key, 0)` **returns** a fallback when absent; it does not insert the key by itself.

## 4. Math you need for complexity analysis

### 4.1 Powers of two and logarithms

| k | 2^k |
| ---: | ---: |
| 0 | 1 |
| 1 | 2 |
| 2 | 4 |
| 3 | 8 |
| 4 | 16 |
| 5 | 32 |
| 10 | 1,024 |

`log₂(n)` answers: **How many times can I divide n by 2 until it is around 1?** For n = 32, five halvings take us from 32 to 1. In Big O, a constant change in logarithm base does not change the asymptotic class.

### 4.2 A common summation

`1 + 2 + ... + n = n(n + 1) / 2`.

Why it matters: a nested loop that visits 1 item, then 2, then 3, ..., then n performs a **triangular** amount of work, which grows proportionally to `n²`.

### 4.3 Powers and combinations

- If each of `n` positions has two independent choices, there are `2^n` possible sequences of choices.
- If a problem tries all orderings of `n` distinct items, there are `n!` possible permutations.
- Do not confuse these **number-of-possibilities** counts with the exact running time of every algorithm: work per possibility and pruning also matter.

### 4.4 Rough growth intuition

For `n = 100,000`:

- A single scan does about `100,000` iterations.
- Two nested full scans do about `10,000,000,000` iterations.
- Repeated halving takes around `17` iterations.

These are illustrations, **not** guarantees of runtime. Real performance also depends on work per iteration, memory, language/runtime, and time limits.

## 5. Complexity preview (your first formal topic)

You only need to understand the basic intuition here; the `Complexity` guide will teach the proofs and harder cases.

| Code shape | Basic intuition | Typical class, when each step is O(1) |
| --- | --- | --- |
| One full scan | Visit n items once | O(n) |
| Two independent full scans | Visit n, then n | O(n) |
| Two nested full scans | n operations for each of n items | O(n²) |
| Repeated halving | Candidate size is divided by 2 | O(log n) |
| One pass over each of two independent inputs | Visit n items and m items | O(n + m) |
| Two nested loops over different inputs | Pair each of n with each of m | O(nm) |

**Time complexity:** How the number of basic operations grows with input size.  
**Auxiliary space:** Additional memory used by the algorithm, usually excluding input and returned output storage (state your convention).  
**Worst-case vs expected:** A typical `HashMap` operation is expected O(1); that is not a blanket worst-case guarantee.  
**Amortized:** Some operations are occasionally expensive, but their average cost across a long operation sequence can be small (e.g., resizing a dynamic array).

### 5.1 Example: linear scan

```java
static int findFirst(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] == target) return i;
    }
    return -1;
}
```

- Worst-case time: **O(n)**.
- Auxiliary space: **O(1)**.
- Important cases: empty array, target at first index, target absent.

### 5.2 Example: halving

```java
static int countHalvings(int n) { // precondition: n > 0
    int count = 0;
    while (n > 1) {
        n /= 2;
        count++;
    }
    return count;
}
```

- Time: **O(log n)** for positive n.
- Auxiliary space: **O(1)**.

### 5.3 A gentle introduction to loop invariants

An **invariant** is a statement that remains true at a chosen point of every iteration.

For a left-to-right array sum, an invariant is: **Before iteration i, `sum` equals the sum of elements with indices `0` through `i-1`.**

- Initialization: at i = 0, the processed prefix is empty, and sum = 0.
- Preservation: adding `nums[i]` makes the invariant true for the next i.
- Termination: after i = n, the processed prefix is the whole array.

This way of thinking later helps you derive Two Pointers, Binary Search, Sliding Window, and Greedy algorithms.

## 6. Interview habits to establish now

Before writing code, state:

1. **Input contract:** What are the types, sizes, valid values, and ordering assumptions?
2. **Output contract:** What exactly must be returned for no result or multiple valid results?
3. **Baseline:** What simple approach would work, even if slow?
4. **Constraints:** Is the baseline plausible for the largest input?
5. **Invariant:** What stays true while the algorithm runs?
6. **Correctness:** Why can the algorithm not miss a required answer?
7. **Complexity:** Count loops **and** important API operations, allocations, and recursive calls.
8. **Tests:** Include empty/singleton inputs when permitted, duplicates, negative values, boundaries, and overflow.

**Avoid one unproductive habit:** Do not read a complete AI solution before attempting to reason through the problem. Start with a simple idea and a small hand-trace, even if you cannot finish.

## 7. A focused review checklist

Mark these only after you can demonstrate them without opening an answer:

- [ ] Explain integer division, remainder, and safe `long` arithmetic.
- [ ] Traverse an empty, singleton, and normal array correctly.
- [ ] Explain `i < length` versus `i <= length`.
- [ ] Explain reference aliasing and shallow 2D-array copying.
- [ ] Compare Strings by content and state a 26-letter frequency-array assumption.
- [ ] Choose correctly among array, list, set, map, and deque for simple operations.
- [ ] Explain a power of two, a logarithm, and `1 + 2 + ... + n`.
- [ ] Recognize one-pass, nested, and halving loop patterns.
- [ ] State one processed-prefix loop invariant.
- [ ] Write a short Java program from a blank editor and test an edge case.

## 8. Diagnostic: answer before reading the key

**Instructions:** Answer Q1–Q16 without looking at Section 9. Write your responses in a separate note or below each question, and explain *why*, not just the final value.

**Q1.** What is printed?

```java
System.out.println(9 / 4);
System.out.println(9 % 4);
```

**Q2.** What is the difference between `(long) a * b` and `(long) (a * b)` when `a` and `b` are `int`?

**Q3.** For an array of length 5, what are its valid indices? Why is `i <= nums.length` usually wrong for forward traversal?

**Q4.** What does this print?

```java
int[] a = {1, 2};
int[] b = a;
b[1] = 9;
System.out.println(a[1]);
```

**Q5.** Does `grid.clone()` deep-copy a 2D primitive array? Explain.

**Q6.** Which expression compares the contents of two Strings: `a == b` or `a.equals(b)`?

**Q7.** You need to know whether an integer has appeared before. Which structure is a natural default: `ArrayList`, `HashSet`, or `PriorityQueue`? Why?

**Q8.** You need the frequency of each integer. Which structure is a natural default? State the expected operation-cost caveat.

**Q9.** How many times can 64 be halved before it reaches 1?

**Q10.** What is `1 + 2 + ... + 100`? What is the growth class of the general sum through n?

**Q11.** Estimate the Big-O time of this method:

```java
for (int i = 0; i < n; i++) {
    doConstantWork();
}
```

**Q12.** Estimate the Big-O time of this method:

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        doConstantWork();
    }
}
```

**Q13.** What is the time complexity of this loop, assuming `n >= 1`?

```java
for (int k = n; k > 1; k /= 2) {
    doConstantWork();
}
```

**Q14.** For a single left-to-right summation loop, write an invariant describing `sum` before processing index `i`.

**Q15.** Which code is correct if `s` is guaranteed to contain only lowercase English letters `'a'`–`'z'`?

```java
int index = s.charAt(i) - 'a';
// Can index safely address int[26] under this assumption?
```

Would this also be correct for arbitrary Unicode text?

**Q16.** A brute-force algorithm uses two complete nested loops over an input of size `n = 100,000`. Approximately how many loop-body executions occur? Would you trust it as a default first implementation for a time-limited interview problem?

## 9. Diagnostic answer key (open only after attempting)

<details>
<summary><strong>Reveal Q1–Q16 answers and explanations</strong></summary>

**A1.** `2` and `1`. Integer division truncates toward zero: `9 / 4` gives 2, and `9 % 4` gives remainder 1.

**A2.** `(long) a * b` converts before multiplication, so the product is computed using `long`. `(long) (a * b)` multiplies using `int` first and may overflow before the cast.

**A3.** Indices `0, 1, 2, 3, 4`. Index 5 is out of bounds. The safe forward condition is usually `i < nums.length`.

**A4.** `9`. Both variables refer to the same array.

**A5.** No. It copies only the outer array; row arrays are shared. A true independent copy must copy each row.

**A6.** `a.equals(b)` compares contents for non-null String references. `==` compares whether the references point to the same object.

**A7.** `HashSet` for expected fast membership checks and uniqueness. It does not provide a sorted traversal order.

**A8.** `HashMap<Integer, Integer>`, mapping each value to a count. Operations are *expected* O(1), not guaranteed worst-case O(1).

**A9.** Six: 64 → 32 → 16 → 8 → 4 → 2 → 1. Thus `log₂ 64 = 6`.

**A10.** `100 * 101 / 2 = 5,050`. In general the growth is **Θ(n²)** (therefore also O(n²)).

**A11.** O(n), assuming `doConstantWork()` is O(1).

**A12.** O(n²), assuming constant work in the body; there are n² body executions.

**A13.** O(log n). Each iteration divides the remaining positive value by two.

**A14.** Before iteration i, `sum` equals the total of elements at indices `[0, i)`. At `i = 0`, that range is empty.

**A15.** Yes, for guaranteed lowercase English letters, indices are 0–25. No, it is not safe for arbitrary Unicode text (and `charAt` processes UTF-16 code units).

**A16.** About `100,000² = 10,000,000,000` body executions. Usually implausible under common interview time limits; still state the work per iteration and actual constraints rather than relying on a universal threshold.

</details>

## 10. Coding drills and reference answers

**First attempt each task in a blank Java file.** Open the reference solution only after testing your own implementation.

### Drill A — Sum an array safely

**Task:** Return the sum of an `int[]` as a `long`. Assume the array is non-null; an empty array has sum 0.

```java
static long sum(int[] nums) {
    // TODO
    throw new UnsupportedOperationException();
}
```

Tests: `[] → 0`, `[4, -1, 7] → 10`, `[Integer.MAX_VALUE, Integer.MAX_VALUE] → 4_294_967_294`.

<details>
<summary>Reference solution A</summary>

```java
static long sum(int[] nums) {
    long total = 0;
    for (int value : nums) total += value;
    return total;
}
```

One scan: O(n) time and O(1) auxiliary space. The accumulator is already `long` before addition.

</details>

### Drill B — Reverse an array in place

**Task:** Reverse a non-null `int[]` without allocating a second array.

```java
static void reverse(int[] nums) {
    // TODO
}
```

Tests: `[] → []`, `[5] → [5]`, `[1,2,3,4] → [4,3,2,1]`.

<details>
<summary>Reference solution B</summary>

```java
static void reverse(int[] nums) {
    int left = 0;
    int right = nums.length - 1;
    while (left < right) {
        int temp = nums[left];
        nums[left] = nums[right];
        nums[right] = temp;
        left++;
        right--;
    }
}
```

O(n) time, O(1) auxiliary space. Invariant: items outside `[left, right]` are already in their final reversed positions.

</details>

### Drill C — Count distinct integers

**Task:** Return the number of different integer values in an array. Do not sort or mutate the input.

```java
static int distinctCount(int[] nums) {
    // TODO
    throw new UnsupportedOperationException();
}
```

Tests: `[] → 0`, `[2, 2, 4, 2, 4] → 2`, `[-1, 0, -1] → 2`.

<details>
<summary>Reference solution C</summary>

```java
static int distinctCount(int[] nums) {
    java.util.Set<Integer> seen = new java.util.HashSet<>();
    for (int value : nums) seen.add(value);
    return seen.size();
}
```

Expected O(n) time and O(n) auxiliary space, under expected constant-time hashing.

</details>

### Drill D — Build a frequency map

**Task:** Map each integer to its number of occurrences.

```java
static java.util.Map<Integer, Integer> frequencies(int[] nums) {
    // TODO
    throw new UnsupportedOperationException();
}
```

Tests: `[] → {}`, `[3,3,5] → {3=2,5=1}` (map iteration order is irrelevant).

<details>
<summary>Reference solution D</summary>

```java
static java.util.Map<Integer, Integer> frequencies(int[] nums) {
    java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
    for (int value : nums) {
        counts.put(value, counts.getOrDefault(value, 0) + 1);
    }
    return counts;
}
```

Expected O(n) time; O(k) auxiliary map storage where k is the number of distinct values (at most n). Counts fit `int` when the Java input array length is within `int` bounds.

</details>

## 11. Readiness decision and study workflow

### When can you start Complexity?

Start when you can:

- [ ] Explain the main Java arithmetic and reference questions correctly (Q1–Q6).
- [ ] Answer the basic math and loop questions (Q9–Q13).
- [ ] Explain a processed-prefix invariant (Q14), even informally.
- [ ] Complete **at least two** coding drills without copying the solution.

You **do not** need perfect scores. If a specific gap remains, review its reference section while learning Complexity; do not delay all DSA study for every minor API detail.

### How to study each topic after this review

1. **Learn:** Read one section of `<Topic>_Essential_Concepts.md` and restate its idea without notes.
2. **Trace:** Walk through a small example and write the state/invariant.
3. **Implement:** Rebuild its key code from a blank editor; test a normal and an edge case.
4. **Practice:** Attempt an Essential exercise before opening hints or solutions.
5. **Explain:** Justify correctness, complexity, and why an alternative would be worse.
6. **Review:** Record whether you solved it Independently (`I`), with Hints (`H`), or after Studying a solution (`S`); revisit after roughly 1, 3, 7, and 14 days when useful.

Do not require every optional Hard problem before continuing to a dependent topic. Do require enough understanding to explain and reproduce a representative prerequisite algorithm.

## 12. References in your DSA guide

If you save this file at the root of `DSA_Guide`, these are the intended references:

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md)
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md)
- [Java Arrays](./Java_Array.md)
- [Java Strings](./Java_String.md)
- [Java Lists](./Java_List_ArrayList_LinkedList.md)
- [Java Maps](./Java_Map_HashMap_TreeMap_LinkedHashMap.md)
- [Java Sets](./Java_Set_HashSet_TreeSet_LinkedHashSet.md)

**Next action:** Complete Sections 8 and 10, then begin the existing `Complexity` materials. After Complexity, start `Arrays_and_Hashing`.
