# Complexity Analysis and Foundations

> **Goal:** Learn to analyze the time and space complexity of Java solutions well enough to explain them confidently in LeetCode and software-engineering interviews.
>
> **Assumptions:** Java 17+, standard JDK collections, and ordinary RAM-model analysis unless stated otherwise.
>
> **Teaching cost model:** To keep examples focused on algorithm structure, printing or consuming one fixed-width primitive value is treated as `O(1)`. Real console I/O, number-to-text conversion, buffering, and output length have costs of their own; include them when output behavior is part of the problem.

## How to use this guide

The labels used throughout the guide are:

- **🔥 MUST KNOW** — essential for LeetCode and technical interviews.
- **⭐ IMPORTANT** — frequently useful when solving or explaining problems.
- **💡 NICE TO KNOW** — useful depth, but not required for a beginner.
- **🌐 JAVA/BACKEND NOTE** — a short connection to Java backend engineering.

A good study order is:

1. Learn growth rates and the meaning of `O(...)`.
2. Practice analyzing loops.
3. Learn space complexity.
4. Learn the complexity of Java collections and library methods.
5. Practice recursion.
6. Use input constraints to choose an algorithm.
7. Practice explaining complexity out loud.

---

## 1. Why Complexity Analysis Matters

### 1.1 What is an algorithm?

**🔥 MUST KNOW**

An **algorithm** is a finite sequence of steps for solving a problem.

For example, suppose you want to determine whether an array contains a target value.

```java
static boolean contains(int[] nums, int target) {
    for (int num : nums) {
        if (num == target) {
            return true;
        }
    }
    return false;
}
```

The algorithm is:

1. Visit each element.
2. Compare it with `target`.
3. Return `true` if a match is found.
4. Otherwise return `false`.

The important question is not only:

> Does this algorithm work?

You should also ask:

> How much time and memory will it require as the input becomes larger?

That is the purpose of **complexity analysis**.

---

### 1.2 What does algorithm efficiency mean?

An algorithm is usually evaluated using two major resources:

- **Time** — how the number of operations grows.
- **Space** — how much additional memory is needed.

Suppose two algorithms solve the same problem:

- Algorithm A checks every pair of elements.
- Algorithm B stores previously seen values in a `HashSet`.

For a large input, Algorithm B may use more memory but perform dramatically fewer operations.

This introduces one of the most important ideas in DSA:

> **Time and space are often traded against each other.**

---

### 1.3 Runtime vs time complexity

**🔥 MUST KNOW**

These are related but different concepts.

#### Runtime

Runtime is the **actual measured execution time**.

For example:

```text
12 milliseconds
0.8 seconds
3 seconds
```

Runtime depends on many things:

- CPU
- JVM implementation
- JIT compilation
- machine load
- cache behavior
- data distribution
- garbage collection
- implementation details

#### Time complexity

Time complexity describes **how the amount of work grows when input size grows**.

For example:

```text
O(1)
O(log n)
O(n)
O(n log n)
O(n²)
```

If an algorithm is `O(n)`, doubling the input usually means the amount of relevant work grows roughly proportionally.

If an algorithm is `O(n²)`, doubling `n` can make the amount of work roughly four times larger.

So:

> **Runtime is a measurement. Complexity is a growth model.**

---

### 1.4 Memory usage vs space complexity

The same distinction applies to memory.

Actual memory usage might be:

```text
12 MB
180 MB
```

Space complexity describes how memory growth depends on input size:

```text
O(1)
O(n)
O(n²)
```

Example:

```java
static int sum(int[] nums) {
    int total = 0;

    for (int num : nums) {
        total += num;
    }

    return total;
}
```

Ignoring the input array itself, this algorithm only creates a few variables.

Auxiliary space:

```text
O(1)
```

Now compare it with:

```java
static int[] copy(int[] nums) {
    int[] result = new int[nums.length];

    for (int i = 0; i < nums.length; i++) {
        result[i] = nums[i];
    }

    return result;
}
```

The new array grows with `n`.

Auxiliary space:

```text
O(n)
```

---

## 2. Input Size and Growth

### 2.1 What does `n` mean?

**🔥 MUST KNOW**

In complexity analysis, `n` usually represents **input size**.

Examples:

```java
int[] nums
```

If `nums.length == n`, then `n` is the number of elements.

For a string:

```java
String text
```

`n` might be:

```java
text.length()
```

For a graph:

- `V` may represent vertices.
- `E` may represent edges.

For two separate arrays:

```java
int[] a; // length n
int[] b; // length m
```

you should often keep both variables:

```text
n = a.length
m = b.length
```

Do **not** automatically replace both with `n`.

---

### 2.2 Why growth matters more than exact operation counts

Consider:

```java
for (int i = 0; i < n; i++) {
    System.out.println(i);
}
```

The body runs `n` times.

Now:

```java
for (int i = 0; i < n; i++) {
    System.out.println(i);
    System.out.println(i);
}
```

The second loop performs approximately twice as many print operations.

You could describe the work as:

```text
2n
```

But complexity focuses on growth:

```text
O(2n) = O(n)
```

Both algorithms grow linearly.

---

### 2.3 Actual execution time is not asymptotic complexity

An `O(n)` solution can be slower than an `O(n²)` solution for very small `n`.

Why?

Because Big O hides:

- constants
- hardware details
- implementation overhead
- JVM optimizations
- cache behavior

However, as `n` becomes large, the **growth rate** usually matters much more.

That is why complexity analysis is valuable for scalable algorithms.

> **🌐 JAVA/BACKEND NOTE**
>
> Backend systems face the same issue. An endpoint that scans every row may seem fast with 100 records but become unusable with millions of records. Complexity thinking helps you reason about scalability before production traffic exposes the problem.

---

## 3. Big O Notation

### 3.1 What Big O means

**🔥 MUST KNOW**

Big O describes an **asymptotic upper bound** on growth.

Informally, developers often use it to say:

> “At large input sizes, the amount of work does not grow faster than this order, ignoring constants and lower-order terms.”

In interviews, people frequently use “Big O” as shorthand for the main complexity of a solution, often emphasizing the worst case.

Strictly speaking:

> **Big O itself does not mean “worst case.”**

It describes a bound. A best-case, average-case, or worst-case function can each be described with Big O.

---

### 3.2 Common growth rates

**🔥 MUST KNOW**

From generally better scaling to worse scaling:

```text
O(1)
O(log n)
O(n)
O(n log n)
O(n²)
O(n³)
O(2^n)
O(n!)
```

A useful mental picture:

```text
Excellent                   Usually dangerous at large n
   ↓                                     ↓
O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2^n) < O(n!)
```

---

### 3.3 Comparison table

| Complexity | Name | Typical example | Relative scaling |
|---|---|---|---|
| `O(1)` | Constant | Array access | Excellent |
| `O(log n)` | Logarithmic | Binary search | Excellent |
| `O(n)` | Linear | Scan an array | Very good |
| `O(n log n)` | Linearithmic | Merge sort | Good |
| `O(n²)` | Quadratic | Compare every pair | Can become expensive |
| `O(n³)` | Cubic | Three full nested loops | Expensive |
| `O(2^n)` | Exponential | Subset-style brute force | Very expensive |
| `O(n!)` | Factorial | Generate all permutations | Extremely expensive |

“Efficient” always depends on the input constraints and constants.

---

### 3.4 `O(1)` — constant time

The amount of work does not grow with `n`.

```java
static int first(int[] nums) {
    return nums[0];
}
```

Array indexing is constant time.

```text
n = 10        → roughly one indexed access
n = 1,000,000 → roughly one indexed access
```

Common examples:

- Array access by index
- `ArrayList.get(index)`
- `HashMap.get(key)` on average
- `ArrayDeque.peekFirst()`

`O(1)` does **not** necessarily mean exactly one CPU instruction. It means the operation count is bounded independently of `n`.

---

### 3.5 `O(log n)` — logarithmic time

**🔥 MUST KNOW**

A logarithmic algorithm repeatedly reduces the remaining problem by a multiplicative factor.

Example:

```java
static int binarySearch(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] == target) {
            return mid;
        }

        if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;
}
```

Each iteration discards approximately half of the remaining array.

```text
1,024
512
256
128
64
32
16
8
4
2
1
```

Only about 10 halvings are needed for 1,024 elements.

Common appearances:

- Binary search
- Balanced search trees
- Heap insertion/removal
- Repeated division by 2

---

### 3.6 `O(n)` — linear time

The work grows proportionally with input size.

```java
static int max(int[] nums) {
    int best = nums[0];

    for (int num : nums) {
        best = Math.max(best, num);
    }

    return best;
}
```

Every element must be examined.

```text
n = 10      → ~10 visits
n = 1,000   → ~1,000 visits
n = 100,000 → ~100,000 visits
```

Common appearances:

- Array scan
- Linked-list traversal
- Counting
- Building a frequency map

---

### 3.7 `O(n log n)` — linearithmic time

This often appears when:

1. There are `log n` levels of work.
2. Each level performs `O(n)` total work.

Classic example:

```text
Merge sort
```

A Java library example is sorting an array:

```java
static void sort(int[] nums) {
    Arrays.sort(nums);
}
```

For Java 17+ interview analysis, treat this call as `O(n log n)` time. Section 15 explains why exact algorithms and auxiliary-space details depend on the overload and JDK implementation.

At each recursive level, all `n` elements participate in merging.

There are approximately `log n` levels.

Therefore:

```text
n work per level × log n levels = O(n log n)
```

Other common examples:

- Heap sort
- Many efficient comparison-based sorting algorithms
- Divide-and-conquer algorithms

---

### 3.8 `O(n²)` — quadratic time

A common source is a pair of loops that each traverse `n` elements.

```java
static void printPairs(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = 0; j < nums.length; j++) {
            System.out.println(nums[i] + ", " + nums[j]);
        }
    }
}
```

Operations:

```text
n × n = n²
```

If `n` increases 10 times, `n²` grows about 100 times.

Typical examples:

- Comparing every pair
- Simple duplicate detection without hashing
- Bubble sort in typical/worst analysis
- Selection sort

---

### 3.9 `O(n³)` — cubic time

Example:

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        for (int k = 0; k < n; k++) {
            // O(1)
        }
    }
}
```

Total:

```text
n × n × n = O(n³)
```

This can still be acceptable for small `n`, but it becomes expensive quickly.

---

### 3.10 `O(2^n)` — exponential time

A common pattern is making two recursive branches at each level.

```java
static int f(int n) {
    if (n <= 1) {
        return 1;
    }

    return f(n - 1) + f(n - 1);
}
```

Very roughly, the number of calls doubles as recursion depth increases.

```text
depth 0: 1
depth 1: 2
depth 2: 4
depth 3: 8
...
```

This leads to exponential growth.

Common appearances:

- Brute-force subset exploration
- Naive recursion with branching
- Some backtracking problems

Memoization can sometimes transform an exponential algorithm into polynomial time.

---

### 3.11 `O(n!)` — factorial time

This often appears when generating all permutations.

For `n` distinct elements:

```text
number of permutations = n!
```

This Java routine explores every ordering while only counting them:

```java
static long countOrders(int[] nums, int index) {
    if (index == nums.length) {
        return 1;
    }

    long count = 0;

    for (int i = index; i < nums.length; i++) {
        swap(nums, index, i);
        count += countOrders(nums, index + 1);
        swap(nums, index, i);
    }

    return count;
}

static void swap(int[] nums, int left, int right) {
    int temporary = nums[left];
    nums[left] = nums[right];
    nums[right] = temporary;
}
```

The search tree contains `Θ(n!)` nodes, so counting takes `Θ(n!)` time and `Θ(n)` recursion-stack space. If you **materialize** every permutation by copying all `n` elements at each leaf, the output itself requires `Θ(n × n!)` time and space. This output-cost distinction is interview-important.

Example growth:

```text
5!  = 120
8!  = 40,320
10! = 3,628,800
12! = 479,001,600
```

Factorial algorithms are normally practical only for very small `n`.

---

### 3.12 Growth comparison

Approximate operation-count intuition:

| `n` | `log₂ n` | `n` | `n log₂ n` | `n²` | `2^n` |
|---:|---:|---:|---:|---:|---:|
| 10 | ~3.3 | 10 | ~33 | 100 | 1,024 |
| 20 | ~4.3 | 20 | ~86 | 400 | 1,048,576 |
| 100 | ~6.6 | 100 | ~664 | 10,000 | astronomically large |
| 1,000 | ~10 | 1,000 | ~10,000 | 1,000,000 | impossible in ordinary brute force |

The exact values are less important than the growth pattern.

---

## 4. Big Omega and Big Theta

### 4.1 Three asymptotic notations

**⭐ IMPORTANT**

```text
O(...)  → upper bound
Ω(...)  → lower bound
Θ(...)  → tight bound
```

Suppose an algorithm always performs exactly `3n + 7` relevant operations.

As `n` grows:

```text
3n + 7
```

grows linearly.

We can say:

```text
O(n)
Ω(n)
Θ(n)
```

The most precise of these is:

```text
Θ(n)
```

because the function grows both no faster and no slower than a constant multiple of `n`.

---

### 4.2 Big O — upper bound

If a function is `O(n²)`, it means that for sufficiently large `n`, its growth is bounded above by a constant multiple of `n²`.

A linear algorithm is technically also:

```text
O(n²)
O(n³)
O(2^n)
```

because those are valid upper bounds.

But developers normally give the **tightest useful bound**.

So for a linear scan, say:

```text
O(n)
```

not:

```text
O(n²)
```

---

### 4.3 Big Omega — lower bound

`Ω(n)` says the algorithm needs at least linear-order work asymptotically.

For an algorithm that always visits every element:

```text
Ω(n)
```

is a valid lower bound.

---

### 4.4 Big Theta — tight bound

`Θ(n)` means both:

```text
O(n)
```

and:

```text
Ω(n)
```

hold.

So the growth is tightly linear.

---

### 4.5 Why interviews discuss Big O much more often

Coding interviews usually care about whether your algorithm will scale under an upper-bound scenario.

So interviewers commonly ask:

> “What is the Big O?”

Often what they practically want is:

- the dominant time complexity
- usually the worst-case complexity
- plus auxiliary space complexity

Still, remember:

> **Big O and worst case are not mathematically identical concepts.**

---

## 5. Best, Average, and Worst Case

### 5.1 Linear search

**🔥 MUST KNOW**

```java
static int linearSearch(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] == target) {
            return i;
        }
    }

    return -1;
}
```

#### Best case

The target is the first element.

```text
O(1)
```

#### Worst case

The target is last or absent.

```text
O(n)
```

#### Average case

Under ordinary assumptions, you inspect a fraction of the array proportional to `n`.

```text
O(n)
```

---

### 5.2 Binary search

For a sorted array:

- Best case: target is the middle element → `O(1)`
- Average case: `O(log n)`
- Worst case: `O(log n)`

---

### 5.3 `HashMap` lookup

For a standard `HashMap`:

```java
map.get(key);
```

Typical expected/average behavior:

```text
O(1)
```

Pathological collision behavior can be worse.

In modern Java, sufficiently large collision buckets may be treeified, improving operations within those tree bins to roughly `O(log n)` under the relevant conditions. However, you should not describe `HashMap` lookup as *guaranteed* `O(1)`.

A safe interview statement is:

> `HashMap.get()` is expected/average `O(1)`, with worse behavior possible under heavy collisions.

---

### 5.4 Which case do interviewers normally care about?

Usually:

```text
Worst-case time complexity
```

because it provides a useful upper-bound guarantee.

However, for hash-based structures and amortized operations, interviewers often expect you to mention:

```text
Average / expected
Worst case
Amortized
```

when those distinctions matter.

---

## 6. Complexity Growth Rates

### 6.1 Dominant growth is what matters

Suppose an algorithm performs:

```text
n² + 100n + 500
```

operations.

For small `n`, all terms matter.

For very large `n`, the `n²` term dominates.

Therefore:

```text
O(n² + 100n + 500) = O(n²)
```

---

### 6.2 Why constants are dropped

Examples:

```text
O(2n)      → O(n)
O(100n)    → O(n)
O(n + 10)  → O(n)
O(3n²)     → O(n²)
```

Why?

Big O describes **growth class**, not exact instruction count.

Doubling the constant does not change the fact that the algorithm is linear.

---

### 6.3 Why lower-order terms are dropped

```text
O(n² + n) = O(n²)
```

As `n` becomes large:

```text
n²
```

dominates:

```text
n
```

Similarly:

```text
O(n³ + n² + n + 1) = O(n³)
```

---

### 6.4 Complexity hierarchy worth memorizing

**🔥 MUST KNOW**

```text
O(1)
O(log n)
O(n)
O(n log n)
O(n²)
O(n³)
O(2^n)
O(n!)
```

This ordering helps you quickly judge whether an optimization is meaningful.

---

## 7. Rules for Calculating Complexity

### 7.1 Constant operations

```java
int x = arr[0];
int y = 10;
boolean found = set.contains(value);
```

Array access is `O(1)`.

The `HashSet.contains` example is **expected `O(1)`**, not a strict unconditional guarantee.

---

### 7.2 Sequential operations are added

**🔥 MUST KNOW**

```java
methodA();
methodB();
```

Suppose:

```text
methodA() = O(n)
methodB() = O(n)
```

Then:

```text
O(n) + O(n)
= O(2n)
= O(n)
```

Example:

```java
for (int x : nums) {
    // O(1)
}

for (int x : nums) {
    // O(1)
}
```

This is:

```text
O(n + n) = O(n)
```

It is **not** `O(n²)` because the loops are consecutive, not nested.

---

### 7.3 Nested independent work is often multiplied

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        // O(1)
    }
}
```

Outer loop:

```text
n iterations
```

For each outer iteration, inner loop:

```text
n iterations
```

Total:

```text
n × n = n²
```

So:

```text
O(n²)
```

---

### 7.4 Independent input variables must remain independent

Suppose:

```java
static void process(int[] a, int[] b) {
    for (int x : a) {
        System.out.println(x);
    }

    for (int y : b) {
        System.out.println(y);
    }
}
```

Let:

```text
n = a.length
m = b.length
```

Then:

```text
O(n + m)
```

Do not simplify this to `O(n)` unless you have a stated relationship such as `m = n`.

The same rule applies to any number of independent inputs:

```java
static long sumAll(int[] a, int[] b, int[] c) {
    long total = 0;

    for (int value : a) {
        total += value;
    }
    for (int value : b) {
        total += value;
    }
    for (int value : c) {
        total += value;
    }

    return total;
}
```

If `n = a.length`, `m = b.length`, and `k = c.length`, the time is `O(n + m + k)`. None of the three variables may be discarded without a stated relationship. If the three traversals were fully nested instead, the time would be `O(n × m × k)`.

---

### 7.5 Nested loops over independent variables

```java
for (int x : a) {
    for (int y : b) {
        System.out.println(x + y);
    }
}
```

Outer loop:

```text
n
```

Inner loop:

```text
m
```

Total:

```text
O(n × m)
```

---

### 7.6 Conditionals usually use the dominant branch

```java
if (condition) {
    linearWork(nums);      // O(n)
} else {
    quadraticWork(nums);   // O(n²)
}
```

For a worst-case complexity discussion:

```text
O(n²)
```

You do not add both branches because only one branch executes.

---

### 7.7 Function calls contribute their own complexity

```java
for (int i = 0; i < n; i++) {
    list.contains(target);
}
```

If `list` is an `ArrayList` of size `n`, `contains()` is linear.

Outer loop:

```text
O(n)
```

`contains()` each time:

```text
O(n)
```

Total:

```text
O(n²)
```

This is why hidden library-method complexity matters.

---

## 8. Loop Complexity Patterns

### 8.1 Constant loop

```java
for (int i = 0; i < 100; i++) {
    // O(1)
}
```

The loop always runs 100 times regardless of `n`.

Therefore:

```text
O(1)
```

Big O cares about growth with input size.

---

### 8.2 Linear loop

```java
for (int i = 0; i < n; i++) {
    // O(1)
}
```

Runs exactly `n` times.

```text
O(n)
```

---

### 8.3 Loop increasing by 2

```java
for (int i = 0; i < n; i += 2) {
    // O(1)
}
```

Number of iterations:

```text
n / 2
```

Therefore:

```text
O(n / 2)
= O(n)
```

The factor `1/2` is ignored.

---

### 8.4 Loop increasing multiplicatively

```java
for (int i = 1; i < n; i *= 2) {
    // O(1)
}
```

Values:

```text
1
2
4
8
16
...
```

After `k` iterations:

```text
i = 2^k
```

Stop when approximately:

```text
2^k >= n
```

Therefore:

```text
k ≈ log₂ n
```

Complexity:

```text
O(log n)
```

---

### 8.5 Decreasing logarithmic loop

```java
int x = n;

while (x > 1) {
    x /= 2;
}
```

Values might be:

```text
1024 → 512 → 256 → ... → 1
```

Number of iterations:

```text
O(log n)
```

---

### 8.6 Consecutive loops

```java
for (int i = 0; i < n; i++) {
    // O(1)
}

for (int j = 0; j < n; j++) {
    // O(1)
}
```

Total:

```text
n + n
= 2n
= O(n)
```

---

### 8.7 Standard nested loops

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        // O(1)
    }
}
```

Total:

```text
n × n = O(n²)
```

---

### 8.8 Dependent nested loop

**🔥 MUST KNOW**

```java
for (int i = 0; i < n; i++) {
    for (int j = i; j < n; j++) {
        // O(1)
    }
}
```

Do not just say “two loops, therefore `O(n²)`.”

Derive it.

When:

```text
i = 0 → inner loop runs n times
i = 1 → inner loop runs n - 1 times
i = 2 → inner loop runs n - 2 times
...
i = n - 1 → inner loop runs 1 time
```

Total:

```text
n + (n - 1) + ... + 1
```

This sum is:

```text
n(n + 1) / 2
```

Therefore:

```text
O(n²)
```

The answer happens to be quadratic, but the reasoning is what matters.

---

### 8.9 Dependent loop that is not quadratic

Consider:

```java
for (int i = 1; i < n; i *= 2) {
    for (int j = 0; j < i; j++) {
        // O(1)
    }
}
```

Inner-loop work is:

```text
1 + 2 + 4 + 8 + ... < 2n
```

Therefore total complexity is:

```text
O(n)
```

Even though there are nested loops.

This is a classic interview trap:

> **Not every nested loop is `O(n²)`.**

---

### 8.10 Another logarithmic nested pattern

```java
for (int i = 0; i < n; i++) {
    for (int j = 1; j < n; j *= 2) {
        // O(1)
    }
}
```

Outer loop:

```text
O(n)
```

Inner loop:

```text
O(log n)
```

Total:

```text
O(n log n)
```

---

## 9. Understanding `O(log n)`

### 9.1 Intuition

**🔥 MUST KNOW**

A logarithm answers a question like:

> How many times can I multiply or divide by a fixed factor before reaching a target?

For powers of 2:

```text
1 → 2 → 4 → 8 → 16 → 32 → 64
```

To reach `n`, the number of doublings is approximately:

```text
log₂ n
```

Likewise, repeatedly halving:

```text
64 → 32 → 16 → 8 → 4 → 2 → 1
```

takes approximately:

```text
log₂ 64 = 6
```

steps.

---

### 9.2 Why binary search is `O(log n)`

Suppose there are 1,000,000 sorted elements.

Each comparison removes about half:

```text
1,000,000
500,000
250,000
125,000
...
```

After around 20 halvings, only about one candidate remains because:

```text
2^20 ≈ 1,048,576
```

So binary search is dramatically better than scanning all one million elements.

---

### 9.3 Balanced binary search trees

A balanced tree has height roughly:

```text
O(log n)
```

Operations that follow one root-to-leaf path, such as search/insert/remove, therefore typically take:

```text
O(log n)
```

Examples in Java:

- `TreeMap`
- `TreeSet`

These are implemented using a Red-Black tree.

---

### 9.4 Heap operations

A binary heap has height:

```text
O(log n)
```

When inserting or removing an element, it may move along the heap height.

Therefore:

```text
PriorityQueue.offer() → O(log n)
PriorityQueue.poll()  → O(log n)
```

---

### 9.5 Logarithm base usually does not matter in Big O

You may see:

```text
log₂ n
log₁₀ n
ln n
```

Changing logarithm base only introduces a constant factor.

Therefore, asymptotically:

```text
O(log₂ n) = O(log₁₀ n) = O(log n)
```

---

## 10. Understanding `O(n log n)`

### 10.1 Merge-sort intuition

**🔥 MUST KNOW**

Imagine repeatedly dividing an array in half:

```text
n
n/2 + n/2
n/4 + n/4 + n/4 + n/4
...
```

The number of levels is:

```text
O(log n)
```

At each level, merging all pieces requires total work proportional to:

```text
O(n)
```

Therefore:

```text
O(n) × O(log n)
= O(n log n)
```

---

### 10.2 Why efficient comparison sorting often appears here

**💡 NICE TO KNOW**

Comparison sorting asks questions like:

```text
Is a < b?
```

Algorithms such as merge sort and heap sort achieve:

```text
O(n log n)
```

For general comparison-based sorting, `Ω(n log n)` comparisons are required in the worst case in the comparison model.

You do not need the proof for most interviews, but it helps explain why `n log n` is a very common sorting target.

---

### 10.3 Divide-and-conquer pattern

A common recurrence is:

```text
T(n) = 2T(n / 2) + O(n)
```

Interpretation:

- solve two half-sized problems
- perform linear work to combine them

This gives:

```text
O(n log n)
```

Merge sort is the classic example.

---

## 11. Space Complexity

### 11.1 Auxiliary space vs total space

**🔥 MUST KNOW**

#### Input space

Memory already occupied by the provided input.

Example:

```java
int[] nums
```

The input itself uses `O(n)` storage.

#### Auxiliary space

Additional memory created by the algorithm.

Most interview questions mean **auxiliary space** when they ask:

> “What is the space complexity?”

Always state your convention if necessary.

#### Total space

```text
input space + auxiliary space
```

---

### 11.2 `O(1)` auxiliary space

```java
static int sum(int[] nums) {
    int total = 0;

    for (int num : nums) {
        total += num;
    }

    return total;
}
```

Variables:

```text
total
num
```

do not grow with `n`.

Therefore auxiliary space:

```text
O(1)
```

---

### 11.3 `O(n)` auxiliary space

```java
static int[] copy(int[] nums) {
    int[] result = new int[nums.length];
    System.arraycopy(nums, 0, result, 0, nums.length);
    return result;
}
```

`result` contains `n` integers.

Therefore:

```text
O(n)
```

auxiliary space.

---

### 11.4 Collections consume space too

```java
Set<Integer> seen = new HashSet<>();

for (int num : nums) {
    seen.add(num);
}
```

In the worst case, the set may contain all `n` elements.

Auxiliary space:

```text
O(n)
```

---

### 11.5 In-place algorithms

An algorithm is commonly called **in-place** when it uses only a small amount of additional memory relative to input size, typically:

```text
O(1)
```

or sometimes `O(log n)` stack space depending on convention.

Example:

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

Time:

```text
O(n)
```

Auxiliary space:

```text
O(1)
```

---

### 11.6 Recursive call stack

Every active recursive call generally consumes stack space.

```java
static void printDown(int n) {
    if (n == 0) {
        return;
    }

    printDown(n - 1);
}
```

Maximum recursion depth:

```text
n
```

Therefore stack space:

```text
O(n)
```

even though each individual call uses only constant local memory.

---

### 11.7 Time-space trade-off

Suppose duplicate detection uses nested loops:

```java
static boolean hasDuplicateSlow(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] == nums[j]) {
                return true;
            }
        }
    }

    return false;
}
```

Time:

```text
O(n²)
```

Space:

```text
O(1)
```

Using a set:

```java
static boolean hasDuplicateFast(int[] nums) {
    Set<Integer> seen = new HashSet<>();

    for (int num : nums) {
        if (!seen.add(num)) {
            return true;
        }
    }

    return false;
}
```

Expected time:

```text
O(n)
```

Space:

```text
O(n)
```

You spend memory to gain speed.

> **🌐 JAVA/BACKEND NOTE**
>
> This trade-off appears constantly in backend systems: caching, indexes, precomputed lookup maps, materialized views, and in-memory batching all spend memory/storage to reduce repeated computation or I/O.

---

## 12. Recursive Complexity

### 12.1 What to inspect

**🔥 MUST KNOW**

For recursion, ask:

1. How many recursive calls are created?
2. How much smaller is the input?
3. How much non-recursive work happens per call?
4. What is the maximum recursion depth?
5. Do branches overlap and repeat work?

---

### 12.2 Linear recursion

```java
static void print(int n) {
    if (n == 0) {
        return;
    }

    System.out.println(n);
    print(n - 1);
}
```

Calls:

```text
print(n)
print(n - 1)
print(n - 2)
...
print(1)
print(0)
```

Number of calls:

```text
O(n)
```

Work per call:

```text
O(1)
```

Time:

```text
O(n)
```

Call-stack depth:

```text
O(n)
```

Space:

```text
O(n)
```

Recurrence:

```text
T(n) = T(n - 1) + O(1)
```

---

### 12.3 Recursion that halves the input

```java
static void halve(int n) {
    if (n <= 1) {
        return;
    }

    halve(n / 2);
}
```

Sequence:

```text
n
n/2
n/4
n/8
...
1
```

Number of calls:

```text
O(log n)
```

Time:

```text
O(log n)
```

Stack:

```text
O(log n)
```

Recurrence:

```text
T(n) = T(n / 2) + O(1)
```

---

### 12.4 Two recursive branches

```java
static int bad(int n) {
    if (n <= 0) {
        return 1;
    }

    return bad(n - 1) + bad(n - 1);
}
```

At each level, one call creates two calls.

Very roughly:

```text
1 + 2 + 4 + 8 + ...
```

Time:

```text
O(2^n)
```

Maximum stack depth:

```text
O(n)
```

Important distinction:

> The recursion tree may contain exponentially many calls while the maximum number of simultaneously active calls is only linear.

---

### 12.5 Recursive binary search

```java
static int binarySearch(
        int[] nums,
        int target,
        int left,
        int right
) {
    if (left > right) {
        return -1;
    }

    int mid = left + (right - left) / 2;

    if (nums[mid] == target) {
        return mid;
    }

    if (nums[mid] < target) {
        return binarySearch(nums, target, mid + 1, right);
    }

    return binarySearch(nums, target, left, mid - 1);
}
```

Each call keeps only half.

Time:

```text
O(log n)
```

Recursive stack:

```text
O(log n)
```

An iterative binary search reduces auxiliary space to:

```text
O(1)
```

---

### 12.6 Merge-sort recurrence

A simplified recurrence:

```text
T(n) = 2T(n / 2) + O(n)
```

Meaning:

- recursively sort left half
- recursively sort right half
- merge both halves in linear time

Result:

```text
O(n log n)
```

You do not need the Master Theorem yet to understand this.

---

## 13. Amortized Complexity

### 13.1 Why amortized analysis exists

**⭐ IMPORTANT**

Some operations are normally cheap but occasionally expensive.

Instead of judging one unlucky operation in isolation, amortized analysis asks:

> What is the average cost per operation across a long sequence of operations?

This is different from probability-based average-case analysis.

---

### 13.2 `ArrayList.add()` example

```java
List<Integer> values = new ArrayList<>();

values.add(10);
values.add(20);
values.add(30);
```

An `ArrayList` stores elements in an internal array.

Most append operations place the new element into an available slot.

Typical cost:

```text
O(1)
```

But eventually the internal array fills.

Then Java must approximately:

1. allocate a larger array
2. copy existing elements
3. append the new element

That resize can cost:

```text
O(n)
```

for that particular operation.

Yet resizing does not happen on every append.

Across many appends, the total resizing cost is spread across all operations.

Therefore:

```text
ArrayList.add(element)
```

is normally described as:

```text
amortized O(1)
```

---

### 13.3 Worst-case operation vs amortized operation

```text
Single unlucky append:
O(n)

Many appends considered together:
amortized O(1) per append
```

This distinction is interview-important.

> **🌐 JAVA/BACKEND NOTE**
>
> Capacity planning matters in real systems too. For large known batch sizes, pre-sizing structures such as `new ArrayList<>(expectedSize)` can reduce reallocations and copying.

---

## 14. Java Data Structure Complexities

### 14.1 Arrays

**🔥 MUST KNOW**

| Operation | Complexity | Why |
|---|---:|---|
| Access `arr[i]` | `O(1)` | Direct index |
| Update `arr[i] = x` | `O(1)` | Direct index |
| Search unsorted | `O(n)` | May inspect all elements |
| Simulated ordered insert | `O(n)` | Shift/copy elements into available or new storage |
| Simulated ordered delete | `O(n)` | Shift elements to close the gap |

Java arrays have fixed length: they expose no size-changing `insert` or `delete` operation. The table describes the work needed to preserve order when you shift elements within an array or create a replacement array.

---

### 14.2 `ArrayList`

| Operation | Typical complexity | Notes |
|---|---:|---|
| `get(i)` | `O(1)` | Backed by array |
| `set(i, x)` | `O(1)` | Direct indexed replacement |
| `add(x)` | amortized `O(1)` | Resize may make one append `O(n)` |
| `add(i, x)` | `O(n)` | Shift elements |
| `remove(i)` | `O(n)` in general | Shift following elements; removing the last is `O(1)` |
| `remove(Object)` | `O(n)` | Search, then possibly shift elements |
| `contains(x)` | `O(n)` | Linear scan |
| `indexOf(x)` | `O(n)` | Linear scan |

> **🌐 JAVA/BACKEND NOTE**
>
> `ArrayList` is the normal default list for many application workloads because indexed access is fast and iteration is cache-friendly.

---

### 14.3 `LinkedList`

Java's `LinkedList` is a doubly linked list.

| Operation | Complexity | Notes |
|---|---:|---|
| `get(i)` | `O(n)` | Must traverse nodes |
| `addFirst(x)` | `O(1)` | Update endpoint links |
| `addLast(x)` | `O(1)` | Update endpoint links |
| `removeFirst()` | `O(1)` | Endpoint removal |
| `removeLast()` | `O(1)` | Endpoint removal |
| `add(i, x)` | `O(n)` | Finding index dominates |
| `remove(i)` | `O(n)` | Finding index dominates |
| `contains(x)` | `O(n)` | Traversal |

A common mistake is saying:

> “Insertion into a linked list is always `O(1)`.”

Insertion or removal is `O(1)` **when a `ListIterator` is already positioned correctly**. Ordinary indexed methods must first locate that position, which may require `O(n)` traversal. Java callers do not receive `LinkedList`'s private internal node objects.

---

### 14.4 `HashMap`

**🔥 MUST KNOW**

| Operation | Expected/Average | Worst-case discussion |
|---|---:|---|
| `put(k, v)` | amortized `O(1)` | A resize or severe collisions can make one call slower |
| `get(k)` | `O(1)` | Worse under severe collisions |
| `containsKey(k)` | `O(1)` | Same lookup behavior |
| `remove(k)` | `O(1)` | Same lookup behavior |

Modern Java can treeify sufficiently large collision bins under particular conditions, making operations within those bins roughly logarithmic. Still, interviews normally phrase the complexity as:

```text
Average/expected: O(1)
Worst/pathological: worse than O(1)
```

If a simple worst-case answer is required, `O(n)` is a safe general upper-bound description for pathological hashing scenarios.

These bounds normally assume that computing the key's `hashCode()` and comparing keys with `equals()` take `O(1)`. If those methods inspect data of length `k`, include that cost in the analysis.

> **🌐 JAVA/BACKEND NOTE**
>
> `HashMap` appears everywhere in Java services: caches, grouping, request processing, lookup tables, aggregation, DTO transformation, and indexing.

---

### 14.5 `HashSet`

`HashSet` is backed by hash-table machinery similar to `HashMap`.

| Operation | Expected/Average | Worst-case discussion |
|---|---:|---|
| `add(x)` | amortized `O(1)` | A resize or pathological collisions can make one call slower |
| `contains(x)` | `O(1)` | Worse with pathological collisions |
| `remove(x)` | `O(1)` | Worse with pathological collisions |

Use a set when you primarily need membership, uniqueness, or fast duplicate detection.

---

### 14.6 `TreeMap`

Java `TreeMap` uses a Red-Black tree.

| Operation | Complexity |
|---|---:|
| `put(k, v)` | `O(log n)` |
| `get(k)` | `O(log n)` |
| `containsKey(k)` | `O(log n)` |
| `remove(k)` | `O(log n)` |
| first/last/floor/ceiling navigation | `O(log n)` |
| Full ordered iteration | `O(n)` |

Main advantage over `HashMap`:

```text
keys remain ordered
```

These operation bounds assume each key comparison is `O(1)`. Include comparator cost when comparing keys itself takes non-constant work.

---

### 14.7 `TreeSet`

`TreeSet` is tree-based.

| Operation | Complexity |
|---|---:|
| `add(x)` | `O(log n)` |
| `contains(x)` | `O(log n)` |
| `remove(x)` | `O(log n)` |

As with `TreeMap`, these bounds assume each natural-order or comparator comparison is `O(1)`.

Use it when you need both:

- uniqueness
- sorted order / navigational operations

---

### 14.8 `PriorityQueue`

Java `PriorityQueue` is a heap.

The bounds below assume that each natural-order or comparator comparison takes `O(1)`.

| Operation | Complexity |
|---|---:|
| `offer(x)` / `add(x)` | amortized `O(log n)`; one growth can be `O(n)` |
| `peek()` | `O(1)` |
| `poll()` | `O(log n)` |
| `contains(x)` | `O(n)` |
| `remove(Object)` | `O(n)` |

Important:

```text
PriorityQueue is not a fully sorted list.
```

It efficiently exposes the highest-priority element.

The queue uses a resizable backing array. The documented heap work for `offer` is `O(log n)`, but an individual growth-triggering insertion can additionally copy `O(n)` references. Across many insertions, that resize cost is amortized. Iterating a `PriorityQueue` does **not** visit elements in priority order.

---

### 14.9 `ArrayDeque`

**⭐ IMPORTANT**

| Operation | Complexity |
|---|---:|
| `addFirst(x)` | amortized `O(1)` |
| `addLast(x)` | amortized `O(1)` |
| `removeFirst()` | `O(1)` |
| `removeLast()` | `O(1)` |
| `peekFirst()` | `O(1)` |
| `peekLast()` | `O(1)` |
| `contains(x)` | `O(n)` |
| `removeFirstOccurrence(x)` | `O(n)` |

For stack/queue behavior, `ArrayDeque` is usually preferred over legacy `Stack`.

---

### 14.10 Stack concept

Stack operations are conceptually:

```text
push → amortized O(1) with ArrayDeque
pop  → O(1)
peek → O(1)
```

In modern Java DSA code, commonly use:

```java
Deque<Integer> stack = new ArrayDeque<>();
```

Example:

```java
stack.push(10);
int top = stack.peek();
int value = stack.pop();
```

`java.util.Stack` is a legacy class extending `Vector`; it is usually not the preferred choice for new code.

---

### 14.11 Summary table

| Structure | Access | Search | Insert/Add | Delete/Remove |
|---|---:|---:|---:|---:|
| Array | `O(1)` | `O(n)` | `O(n)` arbitrary position | `O(n)` arbitrary position |
| `ArrayList` | `O(1)` | `O(n)` | append amortized `O(1)`, indexed `O(n)` | indexed `O(n)` |
| `LinkedList` | `O(n)` indexed | `O(n)` | endpoints `O(1)`, indexed `O(n)` | endpoints `O(1)`, indexed `O(n)` |
| `HashMap` | — | expected `O(1)` by key | expected amortized `O(1)` | expected `O(1)` |
| `HashSet` | — | expected `O(1)` | expected amortized `O(1)` | expected `O(1)` |
| `TreeMap` | — | `O(log n)` by key | `O(log n)` | `O(log n)` |
| `TreeSet` | — | `O(log n)` | `O(log n)` | `O(log n)` |
| `PriorityQueue` | top `O(1)` | `O(n)` general | amortized `O(log n)`, one growth `O(n)` | top `O(log n)` |
| `ArrayDeque` | ends `O(1)` | `O(n)` | ends amortized `O(1)` | ends `O(1)` |

---

## 15. Java Standard-Library Operation Complexities

### 15.1 `String.length()`

```java
int n = text.length();
```

Time:

```text
O(1)
```

Java `String` stores its length information, so it does not need to scan all characters.

---

### 15.2 `String.charAt(index)`

```java
char c = text.charAt(i);
```

Time:

```text
O(1)
```

---

### 15.3 `String.substring(...)`

For a general substring in modern Java versions, creating the result copies the requested range into a new string representation.

If substring length is `k`:

```text
Time:  O(k)
Space: O(k)
```

Do not assume substring is `O(1)` based on old Java implementation behavior.

There are implementation shortcuts for special cases: requesting the whole string can return the original object, and requesting an empty range can return a shared empty string. Those cases may be `O(1)`; use `O(k)` for the normal length-`k` case.

---

### 15.4 `StringBuilder.append()`

Appending one character is typically:

```text
amortized O(1)
```

because the internal buffer occasionally resizes.

Appending a string of length `k` requires copying those characters:

```text
O(k)
```

If the final result contains `L` characters, building it using `StringBuilder` is normally:

```text
O(L)
```

overall.

---

### 15.5 Repeated immutable-string concatenation

**🔥 MUST KNOW**

Consider:

```java
String result = "";

for (String value : values) {
    result += value;
}
```

Strings are immutable.

Each concatenation can create a new string and copy the previous content.

Let `L` be the total number of characters in the final result. If you append one character at a time, then `L = n`, and the copied work resembles:

```text
1 + 2 + 3 + ... + n
```

which is:

```text
O(L²)
```

More generally, repeated immutable concatenation costs the sum of all intermediate prefix lengths. `O(L²)` is the common worst case for many small appends. Peak live/result space is normally `O(L)`, even though the cumulative amount allocated over the whole loop can be `O(L²)`.

Use:

```java
StringBuilder builder = new StringBuilder();

for (String value : values) {
    builder.append(value);
}

String result = builder.toString();
```

This normally reduces total building cost to `O(L)`: amortized appends plus the final `toString()` copy. Space for the builder and final result is also `O(L)`.

> **🌐 JAVA/BACKEND NOTE**
>
> String building occurs often when generating logs, CSV, SQL fragments, templates, serializers, and text responses. Prefer `StringBuilder` when performing many incremental concatenations.

---

### 15.6 `Arrays.sort()`

Complexity depends on the array type and JDK implementation. The guarantees below describe Java 17/21 behavior; always separate interview-level analysis from implementation details that could change in another JDK.

For Java 17:

#### Primitive arrays

```java
int[] nums;
Arrays.sort(nums);
```

The JDK uses specialized primitive sorting implementations. Java 17/21 documentation describes them as offering:

```text
O(n log n) time on all data sets
```

Auxiliary space depends on the primitive type, input, and implementation path. Do not promise universal `O(1)` space: modern JDK implementations may allocate a linear-size temporary buffer on some paths.

#### Object arrays

```java
Integer[] nums;
Arrays.sort(nums);
```

Object sorting is stable and uses comparison-based algorithms with worst-case order:

```text
Time:  O(n log n)
Space: O(n) temporary references in the worst case
```

The time bound assumes `compareTo()` or the supplied comparator takes `O(1)` per comparison. If one comparison costs `O(k)`, the comparison work can become `O(k × n log n)`.

For LeetCode/interview discussion, unless an implementation detail is central, saying:

```text
sorting n elements → O(n log n)
```

is normally appropriate.

---

### 15.7 `Collections.sort()`

```java
Collections.sort(list);
```

For ordinary comparison sorting of `n` list elements, reason as:

```text
Time:  O(n log n)
Space: O(n) for standard JDK implementations
```

The sort is stable and `Collections.sort(list)` delegates to `list.sort(...)`. These bounds assume each comparison takes `O(1)`.

Important hidden cost:

If your comparator itself performs expensive work, total complexity includes that work.

Example:

```java
list.sort((a, b) -> Integer.compare(
        expensiveScore(a),
        expensiveScore(b)
));
```

If `expensiveScore()` is `O(k)`, then comparator calls are not `O(1)`.

---

### 15.8 `Arrays.copyOf()`

```java
int[] copy = Arrays.copyOf(nums, nums.length);
```

Copies `n` elements.

```text
Time:  O(n)
Space: O(n)
```

In general, `Arrays.copyOf(original, newLength)` takes `O(newLength)` time and space: it copies `min(original.length, newLength)` values and initializes any remaining positions in the newly allocated array. For object arrays, the references are copied; the referenced objects are not deeply cloned.

---

### 15.9 `System.arraycopy()`

```java
System.arraycopy(src, 0, dst, 0, k);
```

Copies `k` elements.

```text
Time:            O(k)
Auxiliary space: O(1)
```

The destination array must already exist; `arraycopy` does not allocate it. Overlapping source and destination ranges are supported. The JVM may optimize the copy intrinsically, but that changes constants rather than its linear dependence on `k`.

---

### 15.10 Hidden complexity checklist

When analyzing Java code, do not assume a method call is `O(1)` just because it occupies one line.

Ask:

```text
What does this method actually do?
```

Examples:

```text
list.contains(x)       → O(n)
list.indexOf(x)        → O(n)
Arrays.copyOf(...)     → O(n)
Arrays.sort(...)       → about O(n log n)
substring(...)         → O(k)
StringBuilder.toString → O(n) to create the resulting String
```

---

## 16. Input Constraints and Algorithm Selection

### 16.1 Why constraints matter

**🔥 MUST KNOW**

A solution is not “fast” or “slow” in isolation.

It is fast or slow **relative to the allowed input size and execution environment**.

A quadratic algorithm may be perfectly acceptable for:

```text
n = 200
```

but disastrous for:

```text
n = 1,000,000
```

---

### 16.2 Rough interview heuristics

These are **not universal rules**.

They are mental starting points.

| Approximate `n` | Complexities that may be plausible |
|---:|---|
| `n ≤ 10` | `O(n!)`, `O(2^n)`, polynomial |
| `n ≤ 20` | often `O(2^n)` with careful constants |
| `n ≤ 100` | `O(n³)` may be plausible |
| `n ≤ 1,000` | `O(n²)` may be plausible |
| `n ≤ 10,000` | `O(n²)` is often risky; `O(n log n)` safer |
| `n ≤ 100,000` | usually target `O(n log n)` or `O(n)` |
| `n ≤ 1,000,000` | usually target `O(n)` or `O(n log n)` with care |
| huge searchable domain | `O(log n)` may be ideal |

Actual feasibility depends on:

- language
- constant factors
- number of test cases
- memory limit
- data structure overhead
- expensive operations inside loops
- time limit
- JIT/runtime behavior

---

### 16.3 Example reasoning

Suppose:

```text
n ≤ 100,000
```

A pairwise algorithm:

```text
O(n²)
```

could imply approximately:

```text
10^10
```

pair checks.

That is generally too much.

So you should look for patterns such as:

- hashing
- sorting
- two pointers
- sliding window
- binary search
- prefix sums

that can reduce the solution toward:

```text
O(n)
O(n log n)
```

---

### 16.4 Constraint clues for brute force

If:

```text
n ≤ 20
```

the problem may intentionally permit subset enumeration:

```text
O(2^n)
```

If:

```text
n ≤ 10
```

permutation backtracking:

```text
O(n!) search states
```

may be intended. Materializing every length-`n` permutation adds the cost of copying each result, producing `Θ(n × n!)` time and output space.

This is one reason experienced problem solvers read constraints **before coding**.

---

## 17. Step-by-Step Java Complexity Analysis

### Example 1 — `O(1)`

```java
static int second(int[] nums) {
    return nums[1];
}
```

#### 1. What repeats?

Nothing.

#### 2. How many times?

One array access.

#### 3. How are operations combined?

Only constant work.

#### 4. What can be removed?

Nothing meaningful.

#### 5–6. Final time and space

```text
Time:  O(1)
Space: O(1)
```

---

### Example 2 — `O(n)`

```java
static int sum(int[] nums) {
    int total = 0;

    for (int num : nums) {
        total += num;
    }

    return total;
}
```

#### 1. What repeats?

```text
total += num
```

runs once per element.

#### 2. How many times?

```text
n
```

#### 3. How are operations combined?

There is one pass with constant work per element, so the work is `n × O(1)`.

#### 4. What can be removed?

There are no constants or lower-order terms to remove.

#### 5–6. Final time and space

```text
Time:  O(n)
Space: O(1)
```

---

### Example 3 — `O(log n)`

```java
static int countHalves(int n) {
    int count = 0;

    while (n > 1) {
        n /= 2;
        count++;
    }

    return count;
}
```

#### 1. What repeats?

The comparison, integer division, assignment, and increment repeat. Each iteration does `O(1)` work.

#### 2. How many times?

Each iteration halves `n`.

After `k` iterations:

```text
n / 2^k ≈ 1
```

So:

```text
k ≈ log₂ n
```

#### 3. How are operations combined?

`O(1)` work per iteration multiplied by `O(log₂ n)` iterations gives `O(log₂ n)`.

#### 4. What can be removed?

The logarithm base contributes only a constant factor, so `O(log₂ n)` becomes `O(log n)`.

#### 5–6. Final time and space

```text
Time:  O(log n)
Space: O(1)
```

---

### Example 4 — `O(n²)`

```java
static int countEqualPairs(int[] nums) {
    int count = 0;

    for (int i = 0; i < nums.length; i++) {
        for (int j = 0; j < nums.length; j++) {
            if (nums[i] == nums[j]) {
                count++;
            }
        }
    }

    return count;
}
```

#### 1–2. What repeats, and how many times?

The outer loop runs:

```text
n
```

The inner loop runs this many times for each outer iteration:

```text
n
```

#### 3. How are operations combined?

The loops are nested, so their counts multiply:

```text
n × n = n²
```

#### 4. What can be removed?

There are no lower-order terms. The product is already `n²`.

#### 5–6. Final time and space

```text
Time:  O(n²)
Space: O(1)
```

---

### Example 5 — `O(n log n)`

```java
static void process(int[] nums) {
    for (int num : nums) {
        int x = nums.length;

        while (x > 1) {
            x /= 2;
        }
    }
}
```

#### 1–2. What repeats, and how many times?

The outer loop visits all `n` elements:

```text
O(n)
```

For each visit, the inner loop halves `x` until it reaches `1`:

```text
O(log n)
```

#### 3. How are operations combined?

The logarithmic work occurs inside every outer iteration, so multiply:

```text
O(n log n)
```

#### 4. What can be removed?

No lower-order term remains. The logarithm base is ignored as a constant factor.

#### 5–6. Final time and space

```text
Time:  O(n log n)
Space: O(1)
```

---

### Example 6 — `O(n + m)`

```java
static void printBoth(int[] a, int[] b) {
    for (int x : a) {
        System.out.println(x);
    }

    for (int y : b) {
        System.out.println(y);
    }
}
```

#### 1–2. What repeats, and how many times?

The first loop performs constant work `n` times:

```text
O(n)
```

The second loop performs constant work `m` times:

```text
O(m)
```

#### 3. How are operations combined?

The loops are consecutive, so add their costs:

```text
O(n + m)
```

#### 4. What can be removed?

Neither independent variable may be removed unless a relationship between `n` and `m` is given.

#### 5–6. Final time and space

```text
Time:  O(n + m)
Space: O(1)
```

ignoring output system buffering.

---

### Example 7 — `O(n × m)`

```java
static int countMatches(int[] a, int[] b) {
    int count = 0;

    for (int x : a) {
        for (int y : b) {
            if (x == y) {
                count++;
            }
        }
    }

    return count;
}
```

#### 1–2. What repeats, and how many times?

The outer loop runs `n` times:

```text
n
```

For each outer iteration, the inner loop runs `m` times:

```text
m
```

#### 3. How are operations combined?

The loops are nested, so multiply their counts:

```text
O(nm)
```

#### 4. What can be removed?

Neither independent variable may be removed unless their relationship is known.

#### 5–6. Final time and space

```text
Time:  O(nm)
Space: O(1)
```

---

### Example 8 — Hidden `O(n²)`

```java
static boolean repeatedContains(List<Integer> values) {
    for (int x : values) {
        if (values.contains(x + 1)) {
            return true;
        }
    }

    return false;
}
```

Suppose `values` is an `ArrayList` with `n` elements.

#### 1–2. What repeats, and how many times?

The enhanced-for loop can visit all `n` elements:

```text
O(n)
```

Each `ArrayList.contains()` call can scan all `n` elements:

```text
O(n)
```

#### 3. How are operations combined?

The linear lookup occurs inside the linear traversal, so multiply in the worst case:

```text
O(n²)
```

#### 4. What can be removed?

No lower-order term remains; `n × n` is `n²`.

#### 5–6. Final worst-case time and space

```text
Time:  O(n²)
Space: O(1)
```

---

### Example 9 — `O(2^n)` recursion

```java
static int branches(int n) {
    if (n == 0) {
        return 1;
    }

    return branches(n - 1) + branches(n - 1);
}
```

#### 1. What repeats?

Each non-base call performs constant local work and creates two recursive calls.

#### 2. How many times?

The recursion tree doubles at each level and has depth `n`, so its node count is:

```text
Θ(2^n)
```

#### 3. How are operations combined?

Constant work across all tree nodes gives `Θ(2^n)` total work.

#### 4. What can be removed?

There are no lower-order terms that change the exponential result.

#### 5–6. Final time and space

```text
Time:  Θ(2^n)
Space: O(n) maximum active call stack
```

The number of calls is exponential, but calls from both branches are not all simultaneously active; maximum recursion depth is only linear.

---

### Example 10 — Sorting plus scan

```java
static boolean hasAdjacentDuplicate(int[] nums) {
    Arrays.sort(nums);

    for (int i = 1; i < nums.length; i++) {
        if (nums[i] == nums[i - 1]) {
            return true;
        }
    }

    return false;
}
```

#### 1–2. What repeats, and how many times?

Sorting processes `n` elements in `O(n log n)` time:

```text
O(n log n)
```

The following loop performs at most `n - 1` comparisons:

```text
O(n)
```

#### 3. How are operations combined?

Sorting and scanning are consecutive phases, so add them:

```text
O(n log n + n)
```

#### 4. What can be removed?

Drop the dominated linear term:

```text
O(n log n)
```

#### 5–6. Final time and space

```text
Time:  O(n log n)
Space: O(n) conservative worst-case auxiliary bound for Java 17/21
```

Some primitive-sort implementation paths use less space. In an interview, state the sorting-space assumption rather than blindly claiming `O(1)`.

---

## 18. Common Complexity Analysis Mistakes

### Mistake 1 — “Every nested loop is `O(n²)`”

Wrong.

```java
for (int i = 0; i < n; i++) {
    for (int j = 1; j < n; j *= 2) {
        // ...
    }
}
```

is:

```text
O(n log n)
```

**How to avoid it:** analyze each loop’s iteration count.

---

### Mistake 2 — Adding when you should multiply

Nested work often multiplies.

```text
outer: O(n)
inner per outer iteration: O(m)

total: O(nm)
```

---

### Mistake 3 — Multiplying when you should add

Consecutive loops add.

```java
for (...) { } // O(n)
for (...) { } // O(n)
```

Total:

```text
O(n + n) = O(n)
```

not `O(n²)`.

---

### Mistake 4 — Ignoring independent variables

```text
O(n + m)
```

is not automatically:

```text
O(n)
```

unless you know how `m` relates to `n`.

---

### Mistake 5 — Confusing actual runtime with Big O

A measured runtime such as:

```text
15 ms
```

does not tell you the asymptotic growth class.

---

### Mistake 6 — Forgetting recursion stack space

A recursive algorithm may use no explicit array or collection and still require:

```text
O(n)
```

stack space.

---

### Mistake 7 — Assuming `HashMap` is guaranteed `O(1)`

Better wording:

```text
Expected/average O(1)
```

with worse collision behavior possible.

---

### Mistake 8 — Saying `ArrayList.add()` is always strict `O(1)`

Appending is:

```text
amortized O(1)
```

A resize operation may be:

```text
O(n)
```

---

### Mistake 9 — Ignoring hidden library calls

One line of Java can represent substantial work.

```java
Arrays.sort(nums);
```

is not `O(1)`.

---

### Mistake 10 — Treating `O(2n)` as different from `O(n)`

Big O drops constant factors.

```text
O(2n) = O(n)
```

---

### Mistake 11 — Assuming two `O(n)` loops imply `O(n²)`

Only if one is repeatedly executed inside the other.

Consecutive linear loops remain:

```text
O(n)
```

---

### Mistake 12 — Reporting only time complexity

Interviewers commonly expect both:

```text
Time complexity
Auxiliary space complexity
```

---

### Mistake 13 — Forgetting output space

If your algorithm must return `n` items, the output itself requires `O(n)` storage.

Sometimes interviewers exclude required output space from auxiliary-space analysis; state your assumption.

---

### Mistake 14 — Analyzing syntax instead of behavior

This:

```java
map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
```

looks compact, but complexity depends on:

- hash lookup
- object creation
- list append
- possible resizing
- total number of inserted values

Always analyze operations, not line count.

---

## 19. Interview Complexity Analysis Process

When asked:

> “What is the time and space complexity of your solution?”

Use this process.

### Step 1 — Identify input size

Say what your variables mean.

Example:

```text
Let n be nums.length.
```

If there are two independent inputs:

```text
Let n = a.length and m = b.length.
```

---

### Step 2 — Identify repeated operations

Look for:

- loops
- recursion
- repeated searches
- sorting
- collection operations
- string copying

---

### Step 3 — Analyze each loop

Ask:

```text
How many iterations?
```

Watch for:

```text
i++
i += 2
i *= 2
i /= 2
```

---

### Step 4 — Analyze nesting

If an inner operation runs for every outer iteration, combine the counts.

Examples:

```text
n × n = n²
n × log n = n log n
n × m = nm
```

---

### Step 5 — Check library-method complexity

Examples:

```text
ArrayList.contains → O(n)
HashSet.contains   → expected O(1)
Arrays.sort        → roughly O(n log n)
substring(k chars) → O(k)
```

---

### Step 6 — Analyze recursion

Determine:

- number of branches
- input reduction
- work per call
- maximum call depth

---

### Step 7 — Combine sequential phases

Example:

```text
sorting + scan
O(n log n) + O(n)
```

---

### Step 8 — Drop constants and dominated terms

```text
O(2n)       → O(n)
O(n² + n)   → O(n²)
O(n log n+n)→ O(n log n)
```

---

### Step 9 — Analyze auxiliary memory

Check:

- arrays
- lists
- maps
- sets
- queues
- recursion stack
- copied strings

---

### Step 10 — State the result clearly

A strong interview answer sounds like:

> “Let `n` be the number of elements. I scan the array once, and each `HashSet` membership operation is expected `O(1)`, so the expected time is `O(n)`. The set may store up to `n` elements, so the auxiliary space is `O(n)`.”

That is clearer than simply saying:

```text
O(n), O(n)
```

---

## 20. Optimizing Algorithm Complexity

### 20.1 `O(n²)` → expected `O(n)` using `HashSet`

#### Before

```java
static boolean hasDuplicate(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] == nums[j]) {
                return true;
            }
        }
    }

    return false;
}
```

Time:

```text
O(n²)
```

Space:

```text
O(1)
```

#### After

```java
static boolean hasDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();

    for (int num : nums) {
        if (!seen.add(num)) {
            return true;
        }
    }

    return false;
}
```

Expected time:

```text
O(n)
```

Space:

```text
O(n)
```

Trade-off:

```text
more memory → less time
```

---

### 20.2 Repeated linear lookup → preprocessing with `HashMap`

Suppose many queries ask for a user's score.

Repeated scanning:

```text
q queries × O(n) scan
= O(qn)
```

Preprocess:

```java
Map<Integer, Integer> scoreById = new HashMap<>();

for (User user : users) {
    scoreById.put(user.id(), user.score());
}
```

Build:

```text
expected O(n)
```

Each query:

```text
expected O(1)
```

Total for `q` queries:

```text
expected O(n + q)
```

This pattern is extremely common.

---

### 20.3 `O(n)` search → `O(log n)` binary search

If data is already sorted:

```text
linear search → O(n)
binary search → O(log n)
```

However, if you must sort first:

```text
sort once: O(n log n)
search:    O(log n)
```

For one search, sorting may not be worthwhile.

For many searches, it may be.

---

### 20.4 Repeated sorting → sort once

Bad pattern:

```java
for (Query query : queries) {
    Arrays.sort(nums);
    // answer query
}
```

If there are `q` queries:

```text
O(q × n log n)
```

If data does not change, sort once:

```java
Arrays.sort(nums);

for (Query query : queries) {
    // answer using sorted nums
}
```

Total:

```text
O(n log n + queryProcessing)
```

---

### 20.5 Repeated immutable concatenation → `StringBuilder`

Before:

```java
String result = "";

for (String word : words) {
    result += word;
}
```

Potentially quadratic in total output length.

After:

```java
StringBuilder builder = new StringBuilder();

for (String word : words) {
    builder.append(word);
}

String result = builder.toString();
```

Roughly linear in the total number of appended characters.

---

### 20.6 Sorting `O(n log n)` instead of pairwise `O(n²)`

Sometimes sorting creates structure that eliminates nested comparisons.

Example strategy:

```text
1. Sort values.
2. Use two pointers.
```

Common final complexity:

```text
O(n log n)
```

where sorting dominates an `O(n)` two-pointer pass.

---

### 20.7 Optimization questions to ask

**🔥 MUST KNOW**

When you see an expensive solution, ask:

- Can I use a `HashMap` or `HashSet`?
- Can I sort once?
- Can I binary-search?
- Can I use two pointers?
- Can I use a sliding window?
- Can I precompute prefix information?
- Am I repeating the same computation?
- Can memoization remove overlapping recursive work?
- Is extra memory acceptable?

---

## 21. Practice Exercises

Do not look at the solutions until you have written your own analysis.

### Level 1 — Beginner

#### Exercise 1

```java
for (int i = 0; i < n; i++) {
    System.out.println(i);
}
```

Find time and auxiliary space complexity.

#### Exercise 2

```java
for (int i = 0; i < n; i += 5) {
    System.out.println(i);
}
```

Find time and auxiliary space complexity.

#### Exercise 3

```java
for (int i = 1; i < n; i *= 3) {
    System.out.println(i);
}
```

Find time and auxiliary space complexity.

#### Exercise 4

```java
int x = nums[0];
int y = nums[nums.length - 1];
return x + y;
```

Find time and auxiliary space complexity.

---

### Level 2 — Intermediate

#### Exercise 5

```java
for (int i = 0; i < n; i++) {
    // O(1)
}

for (int j = 0; j < n; j++) {
    // O(1)
}
```

Find time complexity.

#### Exercise 6

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        // O(1)
    }
}
```

Find time complexity.

#### Exercise 7

```java
for (int i = 0; i < n; i++) {
    for (int j = i; j < n; j++) {
        // O(1)
    }
}
```

Find time complexity.

#### Exercise 8

```java
for (int i = 0; i < n; i++) {
    list.contains(i);
}
```

Assume `list` is an `ArrayList` of size `n`.

Find time complexity.

#### Exercise 9

```java
Arrays.copyOf(nums, nums.length);
```

Find time and additional space complexity.

---

### Level 3 — Interview

#### Exercise 10

```java
static boolean containsDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();

    for (int num : nums) {
        if (!seen.add(num)) {
            return true;
        }
    }

    return false;
}
```

Find expected time and auxiliary space complexity.

#### Exercise 11

```java
static int find(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] == target) {
            return mid;
        }

        if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;
}
```

Find time and auxiliary space complexity.

#### Exercise 12

```java
static void process(int[] nums) {
    Arrays.sort(nums);

    for (int num : nums) {
        System.out.println(num);
    }
}
```

Find overall time complexity.

#### Exercise 13

```java
static Map<Integer, Integer> frequency(int[] nums) {
    Map<Integer, Integer> freq = new HashMap<>();

    for (int num : nums) {
        freq.merge(num, 1, Integer::sum);
    }

    return freq;
}
```

Find expected time and auxiliary space complexity.

---

### Level 4 — Challenge

#### Exercise 14

```java
for (int i = 1; i < n; i *= 2) {
    for (int j = 0; j < i; j++) {
        // O(1)
    }
}
```

Find time complexity.

#### Exercise 15

```java
static void f(int n) {
    if (n <= 1) {
        return;
    }

    f(n / 2);
}
```

Find time and stack-space complexity.

#### Exercise 16

```java
static int f(int n) {
    if (n == 0) {
        return 1;
    }

    return f(n - 1) + f(n - 1);
}
```

Find time and stack-space complexity.

#### Exercise 17

```java
String result = "";

for (int i = 0; i < n; i++) {
    result += "x";
}
```

Analyze total time and additional space behavior.

#### Exercise 18

```java
for (int i = 0; i < n; i++) {
    int j = n;

    while (j > 1) {
        j /= 2;
    }
}
```

Find time complexity.

---

## 22. Solutions and Explanations

### Solution 1

Loop runs `n` times.

```text
Time:  O(n)
Space: O(1)
```

---

### Solution 2

Iterations:

```text
n / 5
```

Drop the constant:

```text
Time:  O(n)
Space: O(1)
```

---

### Solution 3

Values grow:

```text
1, 3, 9, 27, ...
```

Number of multiplications until reaching `n`:

```text
O(log₃ n)
```

Logarithm base is ignored asymptotically:

```text
Time:  O(log n)
Space: O(1)
```

---

### Solution 4

Two array accesses and arithmetic.

```text
Time:  O(1)
Space: O(1)
```

---

### Solution 5

Two consecutive linear loops:

```text
O(n) + O(n)
= O(2n)
= O(n)
```

```text
Time:  O(n)
Space: O(1)
```

---

### Solution 6

Outer loop:

```text
n
```

Inner loop:

```text
m
```

Nested:

```text
Time:  O(nm)
Space: O(1)
```

---

### Solution 7

Total inner iterations:

```text
n + (n - 1) + ... + 1
= n(n + 1) / 2
```

Dominant term:

```text
n²
```

Final:

```text
Time:  O(n²)
Space: O(1)
```

---

### Solution 8

Outer loop:

```text
O(n)
```

`ArrayList.contains`:

```text
O(n)
```

Repeated for each outer iteration:

```text
O(n²)
```

Auxiliary space:

```text
O(1)
```

---

### Solution 9

`Arrays.copyOf()` copies `n` elements and creates a new array.

```text
Time:  O(n)
Space: O(n)
```

---

### Solution 10

The array is scanned once.

Each `HashSet.add()` is expected amortized `O(1)`.

Expected time:

```text
O(n)
```

The set can store up to `n` elements:

```text
Space: O(n)
```

Mention that hash collisions can worsen individual operations.

---

### Solution 11

Each binary-search iteration removes about half of the search range.

```text
Time:  O(log n)
Space: O(1)
```

because this implementation is iterative.

---

### Solution 12

Sorting dominates:

```text
O(n log n)
```

The print loop is:

```text
O(n)
```

Total:

```text
O(n log n + n)
= O(n log n)
```

For this primitive-array call, auxiliary sorting space is implementation-dependent. With Java 17/21 implementations, a conservative worst-case answer is `O(n)` because some sorting paths may allocate a linear temporary buffer. If an interviewer asks you to treat the chosen sort as in-place, state that assumption explicitly.

---

### Solution 13

One scan:

```text
n iterations
```

Each `HashMap` update is expected:

```text
O(1)
```

Expected time:

```text
O(n)
```

In the worst case, all elements may be distinct, so the map stores `n` keys:

```text
Space: O(n)
```

---

### Solution 14

Outer values:

```text
1, 2, 4, 8, ...
```

But the inner-loop work is:

```text
1 + 2 + 4 + 8 + ...
```

The geometric sum is less than approximately `2n`.

Therefore:

```text
Time:  O(n)
Space: O(1)
```

This is an important example showing that nested loops are not automatically quadratic.

---

### Solution 15

Each recursive call halves the input.

Call sequence length:

```text
O(log n)
```

Therefore:

```text
Time:  O(log n)
Space: O(log n)
```

The space comes from recursive stack depth.

---

### Solution 16

Each call creates two calls with input `n - 1`.

Time:

```text
O(2^n)
```

Maximum active recursion depth:

```text
O(n)
```

Therefore:

```text
Time:  O(2^n)
Space: O(n)
```

---

### Solution 17

Each `String` is immutable.

At iteration `i`, creating the new string may copy approximately `i` characters.

Total copied work:

```text
1 + 2 + 3 + ... + n
= O(n²)
```

So:

```text
Time:      O(n²)
Peak space: O(n)
```

The final string contains `n` characters, so required output storage is `O(n)`. Temporary allocation volume across the loop is `O(n²)`, but space complexity normally measures the maximum simultaneously live memory, not every byte allocated over time.

Prefer `StringBuilder`.

---

### Solution 18

Outer loop:

```text
O(n)
```

Inner loop repeatedly divides by two:

```text
O(log n)
```

Total:

```text
O(n log n)
```

Space:

```text
O(1)
```

---

## 23. Mini Interview Questions

### 1. Why do we ignore constants in Big O?

Because Big O focuses on asymptotic growth. `2n`, `10n`, and `100n` all grow linearly, so they belong to the same `O(n)` class.

---

### 2. Is `O(2n)` different from `O(n)`?

Asymptotically, no.

```text
O(2n) = O(n)
```

The factor 2 may matter in real performance, but not in Big-O growth class.

---

### 3. Is `O(n + m)` the same as `O(n)`?

Not necessarily.

If `n` and `m` describe independent inputs, keep:

```text
O(n + m)
```

Only simplify if a relationship between them is known.

---

### 4. Why is binary search `O(log n)`?

Because every comparison discards approximately half the remaining search range.

---

### 5. Why is merge sort `O(n log n)`?

There are approximately `log n` division levels, and each level performs `O(n)` total merging work.

```text
O(n) × O(log n)
= O(n log n)
```

---

### 6. Why is `ArrayList.add()` amortized `O(1)`?

Most appends are constant time. Occasional resizing costs `O(n)`, but those expensive resizes are infrequent enough that the average cost per append across many appends remains constant-order.

---

### 7. Is `HashMap.get()` always `O(1)`?

No.

It is normally described as expected/average `O(1)`, but collisions can make lookup slower. Modern Java can treeify some large collision bins, but constant time is not an unconditional worst-case guarantee.

---

### 8. What is the space complexity of recursion?

It depends on maximum recursion depth.

Examples:

```text
depth n     → O(n) stack
depth log n → O(log n) stack
```

---

### 9. When can `O(n²)` still be acceptable?

When `n` is small enough, constants are reasonable, and the execution limits permit it.

For example, `n ≈ 100` or sometimes `n ≈ 1,000` may allow quadratic work, depending heavily on what happens inside the loop and the environment.

---

### 10. Can using more space make an algorithm faster?

Yes.

Example:

```text
Nested-loop duplicate search:
Time O(n²), Space O(1)

HashSet duplicate search:
Expected Time O(n), Space O(n)
```

---

### 11. Is Big O always worst case?

No.

Big O is an upper-bound notation. Developers often use it to discuss worst-case complexity, but those ideas are not identical.

---

### 12. Why are two consecutive `O(n)` loops still `O(n)`?

Because:

```text
O(n) + O(n)
= O(2n)
= O(n)
```

They do not multiply unless one is executed inside the other.

---

### 13. What is amortized complexity?

It describes the average cost per operation across a sequence of operations, even when occasional individual operations are expensive.

---

### 14. What is auxiliary space?

Memory created by the algorithm beyond the memory required for the original input.

---

### 15. What is the first thing you should do when analyzing complexity?

Define the input-size variables.

Example:

```text
Let n = nums.length.
```

Without knowing what `n` represents, the complexity statement is ambiguous.

---

## 24. Complexity Analysis Cheat Sheet

### 🔥 MUST KNOW — Growth ranking

```text
Best scaling
↓
O(1)
O(log n)
O(n)
O(n log n)
O(n²)
O(n³)
O(2^n)
O(n!)
↑
Worst scaling
```

---

### Loop patterns

```java
// O(n)
for (int i = 0; i < n; i++)

// O(n)
for (int i = 0; i < n; i += 5)

// O(log n)
for (int i = 1; i < n; i *= 2)

// O(log n)
while (n > 1) {
    n /= 2;
}
```

Nested:

```text
O(n) inside O(n)       → O(n²)
O(log n) inside O(n)   → O(n log n)
O(m) inside O(n)       → O(nm)
```

But always inspect dependencies; not every nested loop multiplies into `n²`.

---

### Combination rules

Sequential phases:

```text
O(a) + O(b)
```

Nested/repeated phases:

```text
O(a) × O(b)
```

Drop constants:

```text
O(2n) → O(n)
```

Drop lower-order terms:

```text
O(n² + n + 1) → O(n²)
```

Independent variables stay independent:

```text
O(n + m)
O(nm)
```

---

### Common Java collection operations

| Structure / operation | Complexity |
|---|---:|
| Array access | `O(1)` |
| Array search | `O(n)` |
| `ArrayList.get()` | `O(1)` |
| `ArrayList.add()` | amortized `O(1)` |
| `ArrayList.add(index, x)` | `O(n)` |
| `ArrayList.contains()` | `O(n)` |
| `LinkedList.get(index)` | `O(n)` |
| `LinkedList.addFirst/addLast` | `O(1)` |
| `HashMap.get` | expected `O(1)` |
| `HashMap.put` | expected amortized `O(1)` |
| `HashSet.contains` | expected `O(1)` |
| `HashSet.add` | expected amortized `O(1)` |
| `TreeMap` operations | `O(log n)` |
| `TreeSet` operations | `O(log n)` |
| `PriorityQueue.peek()` | `O(1)` |
| `PriorityQueue.offer()` | amortized `O(log n)`; one growth can be `O(n)` |
| `PriorityQueue.poll()` | `O(log n)` |
| `ArrayDeque` end operations | amortized/typical `O(1)` |

---

### Sorting

For interview reasoning:

```text
General efficient comparison sorting:
O(n log n)
```

Common Java call:

```java
Arrays.sort(nums);
```

Always consider the array type, JDK implementation, and whether the interview expects implementation-specific details.

---

### Binary search

Requirement:

```text
Data must be sorted or otherwise monotonic/searchable by the binary-search condition.
```

Complexity:

```text
Iterative:
Time  O(log n)
Space O(1)

Recursive:
Time  O(log n)
Space O(log n)
```

---

### Recursion reminders

Ask:

```text
1. How many branches?
2. How much smaller is each subproblem?
3. Work per call?
4. Maximum depth?
5. Repeated subproblems?
```

Patterns:

```text
T(n) = T(n - 1) + O(1)       → O(n)
T(n) = T(n / 2) + O(1)       → O(log n)
T(n) = 2T(n / 2) + O(n)      → O(n log n)
Two n-1 branches without memo → often O(2^n)
```

---

### Space-complexity reminders

Check:

```text
new arrays
ArrayList
HashMap
HashSet
queue / deque
recursive stack
copied strings
returned output
```

Examples:

```text
few variables         → O(1)
array of size n       → O(n)
HashMap with n keys   → O(n)
recursion depth n     → O(n)
recursion depth log n → O(log n)
```

---

### Java-specific traps

```text
ArrayList.contains()    is O(n), not O(1)
HashMap lookup          is expected O(1), not guaranteed strict O(1)
HashMap put             is expected amortized O(1); resizing can be O(n)
ArrayList.add()         is amortized O(1), not always strict O(1)
String.substring(k)     is O(k) in modern Java
Arrays.copyOf(n)        is O(n)
System.arraycopy(k)     is O(k)
Repeated String +=      may become O(n²)
StringBuilder           is preferred for repeated concatenation
```

---

### Input-size heuristic

Very rough starting point:

```text
n ≤ 10        → factorial may be possible
n ≤ 20        → exponential may be possible
n ≤ 100       → cubic may be possible
n ≤ 1,000     → quadratic may be possible
n ≤ 100,000   → usually seek O(n log n) or O(n)
n ≤ 1,000,000 → usually seek near-linear
```

Never treat this as a universal rule.

---

### 10-second interview checklist

Before giving your final answer, mentally ask:

```text
1. What is n?
2. How many times does each operation run?
3. Are loops consecutive or nested?
4. Are there independent variables?
5. Are any library calls non-constant?
6. Is there recursion?
7. What term dominates?
8. What extra memory grows with input?
9. Is any complexity average or amortized?
10. Can I explain why in one sentence?
```

---

### Final mental model

Do not memorize complexity as a disconnected table.

For every piece of code, ask:

```text
What work repeats?
How many times?
What gets smaller or larger each step?
Which operations are hidden inside method calls?
How much extra memory grows with the input?
```

If you can answer those questions consistently, complexity analysis becomes a reasoning skill rather than a memorization task.

**The interview-ready habit is:**

```text
Define input size
→ count repeated work
→ combine growth
→ simplify
→ analyze memory
→ state assumptions
→ explain the result clearly
```
