# Stack — Essential Concepts (Java 21)

> A stack preserves work that cannot be finished yet. Its top is the most recent unresolved item, which is useful only when that item should be handled next.

## Table of Contents

- [1. Purpose, Prerequisites, and Roadmap Scope](#1-purpose-prerequisites-and-roadmap-scope)
- [2. Stack Fundamentals and LIFO](#2-stack-fundamentals-and-lifo)
- [3. Stack Operations and Java Implementations](#3-stack-operations-and-java-implementations)
- [4. Explicit Stack versus Recursive Call Stack](#4-explicit-stack-versus-recursive-call-stack)
- [5. Matching and Nested Structures](#5-matching-and-nested-structures)
- [6. Expression Evaluation and RPN](#6-expression-evaluation-and-rpn)
- [7. Augmented State and Aggregate Queries](#7-augmented-state-and-aggregate-queries)
- [8. Monotonic Stack Fundamentals](#8-monotonic-stack-fundamentals)
- [9. Increasing, Decreasing, and Equal Values](#9-increasing-decreasing-and-equal-values)
- [10. Next Greater and Next Smaller Patterns](#10-next-greater-and-next-smaller-patterns)
- [11. Optional Advanced Monotonic Boundaries](#11-optional-advanced-monotonic-boundaries)
- [12. Correctness and Stack Invariants](#12-correctness-and-stack-invariants)
- [13. Deriving Time and Space Complexity](#13-deriving-time-and-space-complexity)
- [14. Java Pitfalls and Edge Cases](#14-java-pitfalls-and-edge-cases)
- [15. Choosing between Stacks and Alternatives](#15-choosing-between-stacks-and-alternatives)
- [16. Pattern Recognition Practice](#16-pattern-recognition-practice)
- [17. Conceptual Assessment and Answer Key](#17-conceptual-assessment-and-answer-key)
- [18. Key Takeaways and Interview Explanation](#18-key-takeaways-and-interview-explanation)
- [19. References, Validation, and Next Steps](#19-references-validation-and-next-steps)

## 1. Purpose, Prerequisites, and Roadmap Scope

This guide develops the [Stack category](./DSA_Interview_Roadmap.md#3-stack) of the local NeetCode-based roadmap. Learn to choose useful state, derive safe pushes and pops, prove correctness, and write Java 21 from a blank editor. Knowing an API or recognizing a familiar title does not establish independent problem-solving ability.

The roadmap is authoritative for scope, priorities, and readiness. This guide follows its [Phase 0 foundations](./DSA_Interview_Roadmap.md#phase-0-foundations), [Phase 1 objectives](./DSA_Interview_Roadmap.md#phase-1-array-reasoning-and-linear-patterns), and [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy). Stack can be studied alongside Two Pointers; neither pattern is a universal replacement for the other.

### Reuse existing knowledge and close specific gaps

| Existing reference | Reuse here | Check before continuing |
| --- | --- | --- |
| [Java Arrays](./Java_Array.md) | Indexing, mutation, copies, and logical prefixes | Explain valid positions in an array stack of size s |
| [Java Strings](./Java_String.md) | Indexed traversal, content equality, parsing, and copying costs | Distinguish a numeric token from an operator; state the character model |
| [Lists](./Java_List_ArrayList_LinkedList.md) | Interfaces, generics, wrappers, and deque endpoints | Explain why `get(i)` on a linked list may add a traversal |
| [Complexity](./Complexity/Complexity_Analysis_and_Foundations.md) | Recursion depth, auxiliary space, amortization, and library costs | Distinguish total calls from live frames and amortized from worst-case work |
| [Arrays & Hashing concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) | Processed-prefix invariants and choosing sufficient state | Explain what information a set or count loses about order |
| [Two Pointers concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md) | Safe elimination, termination, and total progress | Explain why discarded work cannot become necessary again |

The discovered Two Pointers documents live in the `Two_Pointers` directory. No missing conceptual prerequisite is assumed from a filename: if an entry in the last column is difficult, revisit that reference and test the skill. You do not need trees, graphs, Sliding Window, or a production collections implementation to understand the core material here.

### Coverage and practice priorities

| Required concept | Main sections | Independent evidence |
| --- | --- | --- |
| LIFO, operations, implementation choices | 2–3 | Predict an operation sequence, including empty behavior |
| Explicit stack versus call stack | 4, 13 | Count frames separately from total calls |
| Matching, early closers, leftover openers | 5 | Implement and explain a delimiter validator |
| Postfix evaluation and operand order | 6 | Evaluate subtraction/division correctly and reject malformed input |
| Auxiliary minimum invariant | 7 | Restore the aggregate after popping repeated minima |
| Monotonic indices, equality, next greater/smaller | 8–10 | Derive the comparison from the exact relation and scan direction |
| Amortized linear work and maximum stack size | 12–13 | Bound total pushes/pops and peak unresolved state |
| Advanced histogram boundaries | 11 | Optional extension; not a core readiness requirement |

Preserve the roadmap's sequence below. E/A/C are its curriculum tiers, not platform labels. Follow each original statement for its current judge interface and constraints; the teaching interfaces in this guide deliberately differ.

| Priority | Problem | Difficulty | Focus |
| --- | --- | --- | --- |
| E — Essential | [Valid Parentheses](https://leetcode.com/problems/valid-parentheses/) | Easy | Matching nested obligations |
| E — Essential | [Min Stack](https://leetcode.com/problems/min-stack/) | Medium | Aggregate metadata that survives undo |
| A — Additional | [Evaluate Reverse Polish Notation](https://leetcode.com/problems/evaluate-reverse-polish-notation/) | Medium | Operand order and reductions |
| E — Essential | [Daily Temperatures](https://leetcode.com/problems/daily-temperatures/) | Medium | Unresolved indices and answer distances |
| C — Optional advanced challenge | [Largest Rectangle in Histogram](https://leetcode.com/problems/largest-rectangle-in-histogram/) | Hard | Maximal monotonic boundaries |

Postfix reasoning remains a **core concept** even though its representative platform problem is Additional. The Hard problem and the advanced boundary example are optional. Reading supports T; blank-editor drills support I; independent Essential solves or cold re-solves support P; unfamiliar transfer supports R. Keep progress and review dates in the existing roadmap records, rather than creating another dashboard here.

### Code and cost conventions

All `java` blocks below are complete members of a single scratch class named `StackExamples`. Put these imports above that class: `java.util.ArrayDeque`, `java.util.Arrays`, `java.util.Deque`, and `java.util.NoSuchElementException`. Combine the blocks in document order; nested classes and records belong inside `StackExamples`. No dependency or build framework is needed.

Inputs are non-null, arrays/tokens are read-only unless explicitly described as stack storage, allocations are feasible, and examples are single-threaded. Sequence lengths are at most 100,000 unless a section says otherwise. Primitive arithmetic has constant cost, but a constant-cost operation can still overflow. Most array examples accept arbitrary ints and duplicates; heights must be nonnegative where specified. Returned indices are zero-based; −1 means no matching index.

Auxiliary space excludes required returned output, following the earlier concepts guides; report that output separately. A stack holding d unresolved entries uses O(d+1) working space. Copies used only for preprocessing are auxiliary. For a long-lived resizable stack, capacity can reflect an earlier peak rather than current size; do not infer allocated memory solely from `size()` after many pops.

## 2. Stack Fundamentals and LIFO

A **stack** is an abstract data type that gives access to the most recently added remaining item. Its rule is **LIFO: last in, first out**. Think of a pile of plates: add at the top, inspect the top, remove from the top. The implementation may use an array or nodes; LIFO describes behavior, not physical memory layout.

```text
Initial             push(40)            pop() returns 40
 TOP                  TOP                 TOP
  |                    |                   |
 [30]                 [40]                [30]
 [20]                 [30]                [20]
 [10]                 [20]                [10]
                      [10]

peek() on the initial stack returns 30 and changes nothing.
isEmpty() asks whether there are any entries; size() counts them.
```

| Operation | Meaning | Changes stored entries? |
| --- | --- | --- |
| push(x) | Make x the newest top | Yes: one added |
| pop() | Remove and return the top | Yes: one removed |
| peek() | Read the top without removing it | No |
| isEmpty() | Test whether size is zero | No |
| size() | Return the number of active entries | No |

After pushing 10, 20, 30, three pops return 30, 20, 10. Repeated peeks return 30 until the stack changes. A pop on an empty stack needs an explicit contract: throw, return an optional result, or use a sentinel. Do not use an arbitrary int such as −1 as an empty sentinel when −1 is valid stored data.

### Why the most recent item matters

An opening parenthesis may need a closer later. When another opening appears, it starts an inner obligation that must finish before the outer one. Postfix operands also wait until an operator can combine the most recent completed expressions. A stack is useful when unresolved work has this reverse-of-creation order.

A stack is less suitable when the oldest item must be processed first, the globally smallest priority matters, or arbitrary previously seen values need lookup. Choose the ordering rule from the problem before choosing a container.

| Property | Stack | FIFO queue |
| --- | --- | --- |
| Next item removed | Most recently added remaining item | Earliest added remaining item |
| Sequence after adding 10,20,30 | Removes 30,20,10 | Removes 10,20,30 |
| Typical purpose | Nesting, deferred reductions, undo | Arrival-order work or breadth-first processing |
| Deque operations | `push`, `pop`, `peek` at the first end | `offer` at the last end; `poll`/`peek` at the first |

The same deque can implement either behavior. Mixing stack insertion with queue assumptions changes the order silently.

**Self-check F1:** Why does counting opening and closing brackets fail on `([)]` even though the counts balance?

**Self-check F2:** What must `peek` preserve that `pop` changes?

## 3. Stack Operations and Java Implementations

### 3.1 Use a Deque interface and an ArrayDeque implementation

The usual declaration is `Deque<Integer> stack = new ArrayDeque<>();`. `Deque` is the interface; `ArrayDeque` is a resizable-array implementation. Java's [Deque documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html) recommends it over legacy `Stack`. Use one end consistently for stack behavior.

| Call on an ArrayDeque | First-end equivalent | On empty / special condition |
| --- | --- | --- |
| `push(x)` | `addFirst(x)` | Adds x; null is rejected |
| `pop()` | `removeFirst()` | Throws `NoSuchElementException` if empty |
| `peek()` | `peekFirst()` | Returns null if empty |
| `element()` | `getFirst()` | Throws `NoSuchElementException` if empty |
| `poll()` | `pollFirst()` | Removes first, or returns null if empty |
| `isEmpty()`, `size()` | State queries | true / zero on empty |

Legacy `Stack.peek()` throws `EmptyStackException` on empty; **Deque.peek() returns null**. Similar names do not guarantee identical empty behavior. See the [Stack API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Stack.html) for that distinction.

```java
static int[] lifoExample() {
    Deque<Integer> stack = new ArrayDeque<>();
    stack.push(10);
    stack.push(20);
    stack.push(30);
    int before = stack.peek();
    int removed = stack.pop();
    return new int[]{before, removed, stack.peek(), stack.size()};
}
```

The result is `[30,30,20,2]`: peek sees 30, pop removes 30, the new top is 20, and two entries remain. This fixed-size demonstration uses O(1) time/space and O(1) output. Generalizing to q operations requires counting q, not declaring every stack program constant time.

`Deque<int>` is invalid because generic type arguments are reference types. `Integer` is a wrapper: pushing an int boxes it, and assigning a peek/pop result to int unboxes it. Guard `isEmpty()` before unboxing a possibly null `peek()`. `Integer` reference `==` is not a general value comparison; unbox to primitives or use content equality. The [Lists wrapper discussion](./Java_List_ArrayList_LinkedList.md#generics-wrappers-and-primitive-arrays) covers the same distinction.

### 3.2 Compare implementation choices

| Choice | Useful properties | Tradeoff / appropriate use |
| --- | --- | --- |
| `Deque<E>` | Stack/queue/deque contract | Interface, not something to instantiate; costs depend on implementation |
| `ArrayDeque<E>` | Resizable array; no per-entry linked node | Good ordinary default; no null elements; not thread-safe |
| `LinkedList<E>` as `Deque<E>` | Linked endpoint operations; also a List | Per-node overhead; permits null, making null-returning queries ambiguous |
| Legacy `Stack<E>` | Vector-based stack API | Broader legacy collection surface; prefer Deque for new ordinary stack code |
| Primitive array stack | No boxing; explicit capacity/index invariant | Capacity management becomes your responsibility |
| Singly linked custom stack | Direct head operations | Node allocation/reference overhead; educational when learning representation |

The [ArrayDeque API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) documents amortized constant-time ordinary operations, growth as needed, null rejection, and linear exceptions such as `contains` and occurrence removal. A growth-triggering push can take O(s) at size s; a sequence of endpoint pushes/pops still has O(q) total amortized work. `peek`, `size`, and `isEmpty` inspect constant state in this implementation. Do not add a linear `contains` call per iteration and still claim an O(n) algorithm.

ArrayDeque is a useful default because its array-backed representation avoids a linked node per entry and its Deque interface expresses the needed endpoint operations. Legacy Stack inherits Vector's broader collection interface; the Java API recommends the Deque alternative and describes ArrayDeque as likely faster for ordinary stack use. The examples need only single-threaded endpoint access.

`LinkedList`'s null policy and endpoint versus indexed costs are covered in the [existing deque reference](./Java_List_ArrayList_LinkedList.md#linkedlist-end-methods-ranked). An `ArrayList` can also act as a stack at its last end; repeated front insertion/removal shifts elements and can make a scan quadratic. The interface name alone never establishes an operation's cost.

### 3.3 Educational fixed-capacity array stack

Store active entries in `[0,size)` from bottom to top. The top is at `size−1`, and `size` is the next free slot. A zero-capacity stack is valid but cannot accept a push. Full push throws `IllegalStateException`; empty pop/peek throw `NoSuchElementException`. Any int, including −1 and both extremes, is valid data.

```java
static final class FixedIntStack {
    private final int[] elements;
    private int size;

    FixedIntStack(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("negative capacity");
        elements = new int[capacity];
    }

    void push(int value) {
        if (size == elements.length) throw new IllegalStateException("stack full");
        elements[size++] = value;
    }

    int pop() {
        if (isEmpty()) throw new NoSuchElementException("stack empty");
        return elements[--size];
    }

    int peek() {
        if (isEmpty()) throw new NoSuchElementException("stack empty");
        return elements[size - 1];
    }

    boolean isEmpty() {
        return size == 0;
    }

    int size() {
        return size;
    }
}
```

**Invariant:** `0 <= size <= capacity`, and active entries in `[0,size)` are exactly the logical stack. Push writes the next free slot, then includes it. Pop decreases size, then reads the former top. Stale ints beyond size are not active entries. `peek` changes neither size nor contents.

Each operation has O(1) worst-case algorithmic work, excluding allocation/runtime effects. Construction initializes capacity C in O(C+1) time and allocates O(C+1) storage even if the current size is zero. Growing an array manually could support amortized O(1) pushes with a geometric capacity rule, but that library-building extension is unnecessary here. An object-array implementation should clear removed references to avoid retaining obsolete objects; primitive ints do not retain object references.

### 3.4 Educational linked-node stack

Make the head node the top. A push creates a node before the old head; a pop makes the old head's successor the new head. Only next links are required for stack operations.

```java
static final class LinkedIntStack {
    private static final class Node {
        final int value;
        final Node next;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node head;
    private int size;

    void push(int value) {
        head = new Node(value, head);
        size++;
    }

    int pop() {
        if (isEmpty()) throw new NoSuchElementException("stack empty");
        int value = head.value;
        head = head.next;
        size--;
        return value;
    }

    int peek() {
        if (isEmpty()) throw new NoSuchElementException("stack empty");
        return head.value;
    }

    boolean isEmpty() {
        return head == null;
    }

    int size() {
        return size;
    }
}
```

**Invariant:** following next links from head yields the logical stack from top to bottom, with exactly size nodes. Changing only the head preserves the rest of that chain. Each operation performs O(1) pointer/primitive work in the usual allocation model. Reachable node storage is O(s+1) for s active entries; garbage collection timing is outside that logical live-space bound. Keep the active size within the guide's stated limit.

```text
Before push(30): head -> [20] -> [10] -> null
After push(30):  head -> [30] -> [20] -> [10] -> null
After pop():    head -> [20] -> [10] -> null   returns 30
```

**Self-check J1:** Why can `int top = stack.peek()` fail on an empty `ArrayDeque<Integer>` even though peek itself does not throw for emptiness?

**Self-check J2:** Why does the fixed array stack use O(C) storage rather than O(current size), and why is an occasional expensive resizable push not evidence against amortized O(1)?

## 4. Explicit Stack versus Recursive Call Stack

An **explicit stack** is a data structure your program creates and manages. The **call stack** tracks active method invocations: parameters, local state, and where execution resumes after a call returns. Ordinary recursive calls return in LIFO order because the most recently invoked active method must finish first.

| Aspect | Explicit stack | Recursive call stack |
| --- | --- | --- |
| Who represents pending work? | Your chosen entry type | Active invocation frames |
| Who pushes/pops? | Your code | Method call/return machinery |
| What must be saved? | Enough state to resume the algorithm | Parameters, locals, and continuation information |
| Memory bound | Maximum live explicit entries and their sizes | Maximum simultaneous frame depth and frame sizes |
| Failure if too deep/large | Allocation/capacity failure is possible | `StackOverflowError` is possible |
| Cost caveat | Explicit state can omit useful information accidentally | Total calls are not the same as maximum live depth |

### A simple explicit reversal

The contract is to return a reversed copy of an arbitrary int array, preserving the input. This example isolates LIFO; direct reverse indexing is simpler for this particular task and uses less auxiliary memory.

```java
static int[] reversedCopyWithStack(int[] values) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (int value : values) {
        stack.push(value);
    }
    int[] reversed = new int[values.length];
    for (int i = 0; i < reversed.length; i++) {
        reversed[i] = stack.pop();
    }
    return reversed;
}
```

For `[4,-1,4]`, pushes leave bottom-to-top `[4,-1,4]`; pops produce `[4,-1,4]`. A less symmetric `[4,-1,7]` produces `[7,-1,4]`. Exactly n pushes and n pops take O(n) total amortized time. The stack peaks at n entries, so auxiliary space is O(n); returned output is another O(n). Empty input returns an empty array. LIFO is correct here, but correctness alone does not make the stack the best approach.

### A recursive chain and its pending additions

For a valid `0 <= from <= values.length`, this method sums the suffix using long arithmetic. Large inputs can exceed the JVM's available recursion depth; use only small traces/tests for this teaching example. An iterative running total is the practical O(1)-auxiliary alternative.

```java
static long recursiveSuffixSum(int[] values, int from) {
    if (from == values.length) {
        return 0;
    }
    return (long) values[from] + recursiveSuffixSum(values, from + 1);
}
```

Trace `[4,-1,7]`, from 0:

```text
sum(0): 4 + waiting for sum(1)
  sum(1): -1 + waiting for sum(2)
    sum(2): 7 + waiting for sum(3)
      sum(3): returns 0
    returns 7
  returns 6
returns 10
```

For k suffix entries there are k+1 calls, O(k+1) time, and O(k+1) simultaneous frames at the deepest point. The input remains unchanged, output is scalar, and the sum fits long under the guide's length bound. Java does not promise tail-call elimination; rewriting a recurrence does not automatically remove its stack space.

A branching recursion can make Θ(2^d) total calls yet use only O(d) live frames if branches execute sequentially and each frame holds constant state. Conversely, a linear chain can have only O(n) calls and O(n) live depth. See [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) for the existing derivations. Converting recursion to iteration must preserve any needed continuation/state, not merely push a parameter and hope the ordering is sufficient.

**Self-check RC1:** Why does a small amount of local data in each recursive call not imply O(1) auxiliary space?

**Self-check RC2:** If a recursive procedure explores two branches one after another, why need both entire recursion trees not be live at once?

## 5. Matching and Nested Structures

### 5.1 Derive the latest unresolved opener

**Recognition:** nested delimiters, opening/closing obligations, or a rule that the inner construct must finish before its enclosing construct. A matching closer must pair with the **latest unresolved opening**, not simply any earlier opener of its type.

**Original teaching contract:** Given ASCII text, return an int array of the same length. At each closing `)`, `]`, or `}`, store its matching opening index. Other positions contain −1. Ignore non-delimiter code units. Require correctly nested `()`, `[]`, `{}`; throw `IllegalArgumentException` on an early closer, a type mismatch, or leftover openers. Preserve the string. This is delimiter matching, not a Java parser: quotes, comments, and escapes have no special semantics.

A straightforward approach keeps delimiter tokens with original indices, repeatedly finds an adjacent matching pair, records it, and deletes it. Each deletion can require a linear search/shift; at most n/2 deletions yield O(n²) worst-case time and O(n) working storage. Balanced nesting always has an innermost adjacent pair, but repeatedly rediscovering it wastes work.

Instead, scan once. Opening a construct postpones its match. Opening another construct makes that new obligation the next one that must close. A stack stores precisely that unresolved nesting path:

1. Push an opening symbol together with its position.
2. Ignore a non-delimiter.
3. On a closer, reject if there is no pending opener.
4. Pop the latest opener and require the corresponding type.
5. Record its original index for that closer.
6. At the end, reject if any opener remains.

```java
record Opening(char symbol, int index) {}

static int[] closingPartnerIndices(String text) {
    int[] partners = new int[text.length()];
    Arrays.fill(partners, -1);
    Deque<Opening> unmatched = new ArrayDeque<>();
    for (int i = 0; i < text.length(); i++) {
        char symbol = text.charAt(i);
        if (symbol == '(' || symbol == '[' || symbol == '{') {
            unmatched.push(new Opening(symbol, i));
            continue;
        }
        char requiredOpening = switch (symbol) {
            case ')' -> '(';
            case ']' -> '[';
            case '}' -> '{';
            default -> '\0';
        };
        if (requiredOpening == '\0') continue;
        if (unmatched.isEmpty()) {
            throw new IllegalArgumentException("closing without opening at " + i);
        }
        Opening opening = unmatched.pop();
        if (opening.symbol() != requiredOpening) {
            throw new IllegalArgumentException("mismatched delimiter at " + i);
        }
        partners[i] = opening.index();
    }
    if (!unmatched.isEmpty()) {
        throw new IllegalArgumentException("unclosed delimiter");
    }
    return partners;
}
```

The record groups two related facts; it does not change the algorithm. The small switch simply maps a closer to its required opener, with a non-delimiter marker otherwise.

### 5.2 Dry run and prefix meaning

Stacks in this guide's tables are displayed **bottom to top, left to right**. A pair such as `{@1` means the opening `{` at index 1. This teaching order is the reverse of an ArrayDeque's usual first-to-last printed order when using push/pop.

Text `x{[a](b)}` has positions 0 through 8:

| Index | Character | Stack before | Action | Stack after | Prefix status / recorded result |
| --- | --- | --- | --- | --- | --- |
| 0 | `x` | `[]` | Ignore | `[]` | Balanced so far |
| 1 | `{` | `[]` | Push `{@1` | `[{@1]` | Extendable; one pending opener |
| 2 | `[` | `[{@1]` | Push `[@2` | `[{@1, [@2]` | Extendable; nested obligation |
| 3 | `a` | `[{@1, [@2]` | Ignore | `[{@1, [@2]` | Extendable |
| 4 | `]` | `[{@1, [@2]` | Pop matching `[@2` | `[{@1]` | `partners[4]=2` |
| 5 | `(` | `[{@1]` | Push `(@5` | `[{@1, (@5]` | Extendable |
| 6 | `b` | `[{@1, (@5]` | Ignore | `[{@1, (@5]` | Extendable |
| 7 | `)` | `[{@1, (@5]` | Pop matching `(@5` | `[{@1]` | `partners[7]=5` |
| 8 | `}` | `[{@1]` | Pop matching `{@1` | `[]` | `partners[8]=1`; balanced |

Return `[-1,-1,-1,-1,2,-1,-1,5,1]`. A prefix with pending openings is not complete yet, but can still be extended into valid text. An early or mismatched closer makes the prefix irreparable without changing what was already read.

For `([)]`, the closer at index 2 requests `(` while the latest unresolved opener is `[`: reject immediately. An empty stack at that point is not the only kind of error. For `(()`, the scan ends with a pending `(`: reject then. Empty text returns `[]`; ordinary `abc` returns `[-1,-1,-1]`; `[]` returns `[-1,0]`; `]` rejects before a pop.

### 5.3 Invariant, correctness, and cost

**Invariant before index i:** the stack contains exactly the unmatched opening delimiters of `text[0..i)` in nesting order; all closers already processed have matched the required latest opener. Recorded partner indices are correct.

Initially the prefix and stack are empty. An opener appends a new inner obligation. A closer must remove the top: matching an older opener would cross the still-open inner construct. The type check preserves the delimiter grammar. Ignoring an ordinary symbol changes no obligation. At termination, an empty stack means every opener was matched, while any leftover opening violates completeness. Each iteration advances i, so the scan terminates.

Let n be code units and d the maximum nesting depth. There is one push per opener and one pop per processed closer; total stack operations are O(n), maximum stack size d. Time is O(n) amortized, auxiliary space O(d+1), returned output O(n). The output array allocation/fill itself costs O(n), even for text without delimiters. Each Opening holds constant-size state.

Common errors are counting types independently, accepting leftover openers, dereferencing an empty stack, or assuming ignored quotes make this a source-code parser. If only one bracket type needs a boolean validity result, a counter with a nonnegative-prefix check can use O(1) auxiliary space. It cannot recover all partner indices or distinguish arbitrary mixed-type nesting by itself.

**Self-check M1:** Why is `([)]` rejected before the entire text is read?

**Self-check M2:** What does maximum nesting depth measure that total number of openings does not?

## 6. Expression Evaluation and RPN

### 6.1 Understand the notation and operand order

In infix notation an operator sits between operands: `(12−5)*3`. In **postfix**, also called **Reverse Polish Notation (RPN)**, operands come first: `12 5 - 3 *`. The token order already describes when each operation is ready; no precedence parser is needed for this input form.

**Recognition:** tokens represent values or operations, and an operation combines the most recently completed subexpressions. RPN can be understood as repeatedly reducing a suffix of completed expressions to one value.

**Teaching contract:** tokens are non-null ASCII strings. A numeric token is a signed decimal int accepted by `Integer.parseInt`; an operator is exactly `+`, `-`, `*`, or `/`. Intermediate/results use long. Reject too few operands, empty input, and extra final operands with `IllegalArgumentException`; malformed/out-of-range numbers cause `NumberFormatException`, which extends `IllegalArgumentException`. Reject long overflow and division by zero with `ArithmeticException`. Preserve the token array. Division truncates toward zero, rather than rounding down.

A simple evaluator repeatedly searches a mutable token/value list for the next reducible operator and replaces its two preceding values and itself with one result. Linear scans/shifts repeated O(n) times can take O(n²) time, with O(n) working storage. The repeated search is unnecessary: when an operator arrives during a left-to-right scan, its two completed operands must already be the two newest remaining values.

Derive the operand stack:

1. Parse a numeric token and push its value.
2. For a binary operator, require at least two values.
3. Pop **right first**, then **left**.
4. Compute `left operator right`, then push the result.
5. At the end, require exactly one result.

The most recent expression appears on the right in the original postfix combination. Reversing pops silently changes subtraction/division while leaving some addition/multiplication tests passing.

```java
static long evaluatePostfix(String[] tokens) {
    Deque<Long> operands = new ArrayDeque<>();
    for (String token : tokens) {
        boolean operator = token.equals("+") || token.equals("-")
                || token.equals("*") || token.equals("/");
        if (!operator) {
            operands.push((long) Integer.parseInt(token));
            continue;
        }
        if (operands.size() < 2) {
            throw new IllegalArgumentException("operator needs two operands");
        }
        long right = operands.pop();
        long left = operands.pop();
        long result;
        if (token.equals("+")) {
            result = Math.addExact(left, right);
        } else if (token.equals("-")) {
            result = Math.subtractExact(left, right);
        } else if (token.equals("*")) {
            result = Math.multiplyExact(left, right);
        } else {
            if (right == 0 || (left == Long.MIN_VALUE && right == -1)) {
                throw new ArithmeticException("invalid or overflowing division");
            }
            result = left / right;
        }
        operands.push(result);
    }
    if (operands.size() != 1) {
        throw new IllegalArgumentException("expression must produce one result");
    }
    return operands.pop();
}
```

Exact arithmetic is a deliberate teaching contract. A long destination alone cannot prevent overflow inside a long expression. Java's ordinary `Long.MIN_VALUE / -1` is the special overflowing quotient that wraps rather than throwing; the guard rejects it explicitly. `Math` also offers `divideExact` in Java 21. See [checked arithmetic](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html), [decimal parsing](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html), and the [division specification](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.2).

### 6.2 Dry run

Tokens: `12 5 - 3 * 2 /`.

| Token | Stack before, bottom to top | Operation | Stack after |
| --- | --- | --- | --- |
| `12` | `[]` | Parse and push | `[12]` |
| `5` | `[12]` | Parse and push | `[12,5]` |
| `-` | `[12,5]` | right=5, left=12; 12−5=7 | `[7]` |
| `3` | `[7]` | Parse and push | `[7,3]` |
| `*` | `[7,3]` | right=3, left=7; 7*3=21 | `[21]` |
| `2` | `[21]` | Parse and push | `[21,2]` |
| `/` | `[21,2]` | right=2, left=21; 21/2=10 | `[10]` |

Return 10. `7 -3 /` returns −2; `-7 3 /` also returns −2. `3 8 -` returns −5. The token `-3` is one number, while `-` alone is an operator. `+42` is a valid numeric token. Whitespace inside a token is not silently trimmed.

### 6.3 Invariant, correctness, and cost

**Invariant:** the stack contains values of the completed postfix subexpressions that have not yet been combined, in their original order. A literal completes one expression. An operator combines the last two completed expressions in the required left/right order and replaces them with the combined value. It never consumes an earlier unrelated value. Induction over tokens proves each reduction correct. Exactly one value at the end means one full expression was formed; underflow or extra values violates the grammar.

For n tokens and L total token code units, processing/parsing takes O(n+L) time in the fixed-width arithmetic model. This simplifies to O(n) **when token lengths are bounded**. An int value range alone does not guarantee that: `Integer.parseInt` can accept a long spelling with many leading zeros. Each valid token causes one push; each valid operator causes two pops, plus one final pop. Maximum stack size d is the largest number of simultaneously available uncombined values, at most n. Auxiliary space for a successful evaluation is O(d+1); output is scalar. Arithmetic exceptions stop invalid computations rather than returning wrapped answers.

Test `[]`, `['+']`, and `['1','2']` as malformed expression shapes; `['2','0','/']` as invalid arithmetic; `['2147483648']` as an out-of-int-range literal; and `['-2147483648','-1','/']` as a valid result 2147483648 under this guide's long contract. In real Java source, token strings use double quotes. A very large intermediate product can still overflow long even though every literal is an int.

Do not apply this evaluator directly to infix text, unary operators, variables, or arbitrary functions: they require additional grammar/state. Parsing an infix language with precedence is an optional extension, not a prerequisite for the roadmap's postfix drill.

**Self-check E1:** Why do `8 3 -` and `3 8 -` produce different results even though the same two values are popped?

**Self-check E2:** Why is nonempty final state insufficient to establish that the entire postfix expression was valid?

## 7. Augmented State and Aggregate Queries

### 7.1 Derive metadata that can be undone

An ordinary stack remembers values and their order. Suppose it must also answer the minimum after any number of pushes and pops. Scanning s active values for every query costs O(s), so q queries interleaved with q pushes can require O(q²) total time. One scalar minimum answers pushes easily but loses the previous minimum when its current witness is popped.

**Recognition:** normal LIFO updates plus an aggregate query that must remain fast after undo. Store enough metadata **at each depth** to restore the earlier state automatically.

**Original teaching interface:** `RangeStack` supports ordinary stack operations plus `min()`, `max()`, and long `span() = max−min`. Any int, including duplicate extrema, is allowed. All top/aggregate queries and pop throw `NoSuchElementException` on empty. Push is always allowed subject to feasible memory and the size bound. This generalizes the minimum invariant rather than copying the assigned judge's exact class interface.

Each level stores `(value, minimum through this level, maximum through this level)`. Derive an update from the old top:

```text
newMinimum = min(oldMinimum, newValue)
newMaximum = max(oldMaximum, newValue)

After pop, the exposed older level already knows the older aggregates.
```

```java
static final class RangeStack {
    private record Level(int value, int minimum, int maximum) {}

    private final Deque<Level> levels = new ArrayDeque<>();

    void push(int value) {
        int minimum = value;
        int maximum = value;
        if (!levels.isEmpty()) {
            minimum = Math.min(value, levels.peek().minimum());
            maximum = Math.max(value, levels.peek().maximum());
        }
        levels.push(new Level(value, minimum, maximum));
    }

    int pop() {
        return levels.pop().value();
    }

    int peek() {
        return levels.element().value();
    }

    int min() {
        return levels.element().minimum();
    }

    int max() {
        return levels.element().maximum();
    }

    long span() {
        return (long) max() - min();
    }

    boolean isEmpty() {
        return levels.isEmpty();
    }

    int size() {
        return levels.size();
    }
}
```

### 7.2 Dry run with repeated minima

Each tuple is `(value,min,max)`; levels are bottom to top.

| Operation | Levels after operation | Top | Min | Max | Span |
| --- | --- | --- | --- | --- | --- |
| push 5 | `[(5,5,5)]` | 5 | 5 | 5 | 0 |
| push 2 | `[(5,5,5),(2,2,5)]` | 2 | 2 | 5 | 3 |
| push 2 | `[(5,5,5),(2,2,5),(2,2,5)]` | 2 | 2 | 5 | 3 |
| push 7 | `[(5,5,5),(2,2,5),(2,2,5),(7,2,7)]` | 7 | 2 | 7 | 5 |
| pop returns 7 | `[(5,5,5),(2,2,5),(2,2,5)]` | 2 | 2 | 5 | 3 |
| pop returns 2 | `[(5,5,5),(2,2,5)]` | 2 | 2 | 5 | 3 |
| pop returns 2 | `[(5,5,5)]` | 5 | 5 | 5 | 0 |

The second pop of 2 does not lose the remaining 2's minimum information. Push MIN then MAX and span must be 4294967295, which is why subtraction is widened before it executes.

### 7.3 Invariant, correctness, and alternatives

**Invariant:** every level's metadata equals the minimum and maximum of all values at or below that level. The first level satisfies this trivially. Appending a value changes the aggregate by exactly the min/max update above. Removing the top leaves every earlier value and metadata pair unchanged, so the newly exposed aggregate is already correct. Empty queries have an explicit exception contract.

Each metadata computation/query is O(1); push is amortized O(1) because the ArrayDeque can grow. Pop and top queries do constant endpoint work; q operations have O(q) total amortized time. There is one Level per active value and peak auxiliary storage O(p+1) for peak size p, including the backing capacity. This long-lived deque need not return to its original allocation after many pops. A single minimum scan needs O(s) time but no per-depth metadata; augmentation trades linear stored state for constant-time aggregate queries.

A separate value stack plus auxiliary minimum stack is another design. If it records only new minima, ties need a rule: either push an equal minimum too (`<=`), or keep a multiplicity count. Recording only strictly smaller minima and removing the auxiliary minimum whenever an equal value is popped loses information on `[5,2,2]`. Storing the full per-depth aggregate avoids that synchronization issue.

Augmentation works when the desired aggregate has a constant-size summary and a constant-time update, such as a running minimum or maximum under LIFO undo. It does not automatically give a constant-time median, arbitrary middle deletion, or a minimum over independently changing non-LIFO elements. Those require different structures or more expensive state updates.

**Self-check A1:** Why is a single current minimum insufficient after popping the only minimum value?

**Self-check A2:** What invariant makes repeated equal minima safe in the per-depth representation?

## 8. Monotonic Stack Fundamentals

### 8.1 From repeated future searches to unresolved entries

**Monotonic** means values follow one direction of order. A monotonic stack maintains its stored values in an increasing or decreasing order, with a deliberate choice about equality. In this guide, that order is always stated **from bottom to top**. Stored indices themselves are also ordered by traversal, but value order and index order are different facts.

Consider: for every position, find the first later position with a strictly greater value. The input need not be sorted. A straightforward algorithm scans rightward separately for each index until it finds an answer. On a decreasing array it examines every later pair, taking Θ(n²) time, with O(1) auxiliary space and O(n) output.

The repeated work comes from asking the future the same question many times. Turn the direction around: when a new value arrives, **which earlier unresolved positions does it answer?** Positions whose values are smaller can now be resolved, provided no earlier arriving value already qualified. The newest pending entry is inspected first; a useful ordering lets eligible entries form a removable suffix of the stack.

```text
Earlier unresolved values, bottom -> top:  [8, 6, 4, 4, 2]
New value:                                 5

2, then 4, then 4 receive their first greater answer now.
6 remains unresolved, and 8 remains behind it.
Push 5 as a new unresolved position:       [8, 6, 5]
```

Each new position is pushed because its own future answer is still unknown. An earlier entry is popped because this new position has **finished its answer**, not because stacks somehow prefer smaller storage. The stack does not contain every processed element; it contains the unresolved ones needed for future decisions.

### 8.2 Two distinct reasons to pop

| Design | Meaning of an entry | Why pop? | Where is an answer written? |
| --- | --- | --- | --- |
| Forward unresolved stack | An earlier position still awaiting a future answer | Current position is its first qualifying answer | Into the popped earlier position's result |
| Candidate stack scanned from the far side | A processed position that might answer a later query | A nearer candidate dominates it or it cannot satisfy the current relation | Into the current queried position's result after pruning |

Both designs can be correct for next-element problems. Their invariants and equality conditions differ. Do not memorize one while condition and apply it to every scan direction.

### 8.3 What monotonic order contributes

For the forward strictly-greater problem, pending values are nonincreasing from bottom to top. The smallest pending values are at the top. If the top is smaller than the current value, resolve it; there may be more eligible entries just below. If the top is at least the current value, all deeper values are at least that top, so none qualifies for a strictly-greater answer from the current value.

That last implication is what makes checking only the top sufficient. Without the maintained order, a blocking top could hide an eligible entry below it. The invariant must establish why that cannot happen.

```text
Physical stack view                 Table view
TOP [index 4 : value 2]              bottom -> top
    [index 3 : value 4]              [0:8, 1:6, 2:4, 3:4, 4:2]
    [index 2 : value 4]
    [index 1 : value 6]
    [index 0 : value 8] BOTTOM
```

### 8.4 What to store

| Stored state | Sufficient when | Insufficient when |
| --- | --- | --- |
| Values only | Only the next/previous qualifying value is required and identity does not matter | Need positions, distances, widths, or different answers for equal occurrences |
| Indices into the original array | Need value access plus position/identity | Original array becomes unavailable or is modified incompatibly |
| Value/index record | Need both facts without relying on a stable backing sequence | Extra fields are unnecessary for a simple index-backed scan |
| Value plus aggregate metadata | Need undoable summaries such as minima | Need arbitrary random lookup rather than stack-top operations |

Index i lets you recover `values[i]`, write `answer[i]`, compute a gap, and distinguish two equal occurrences. It is usually the clearest representation for the roadmap's next-greater drill and Daily Temperatures-style reasoning. Preserve the input array when indices depend on its values.

**Recognition clues:** nearest next/previous greater/smaller, waiting until a threshold is exceeded, or boundaries defined by the first blocking value. The exact relation and the word **first/nearest** matter. A request for the maximum value anywhere to the right is instead naturally a suffix-maximum problem; a request to count every greater value has more information than one nearest witness.

**Self-check N1:** In the forward design, what remains unknown for an entry while it stays in the stack?

**Self-check N2:** Why is an index stack necessary when the answer is a distance rather than merely a value?

## 9. Increasing, Decreasing, and Equal Values

### 9.1 State whether equality is permitted

“Increasing stack” is ambiguous unless you say strictly increasing or nondecreasing. These are orders of the **values represented by entries**, from bottom to top:

| Order | Example | Equality permitted? |
| --- | --- | --- |
| Strictly increasing | `[1,3,7]` | No |
| Nondecreasing | `[1,3,3,7]` | Yes |
| Strictly decreasing | `[7,3,1]` | No |
| Nonincreasing | `[7,3,3,1]` | Yes |

The desired answer relation determines which equality behavior is correct. Equal values may need separate later answers; alternatively, a nearer equal candidate may dominate an older one. Equality handling is a correctness decision, not a cosmetic optimization.

### 9.2 Forward resolution table

Here `current` is the new value and `topValue` belongs to an earlier unresolved entry. Pop only when **current qualifies as that entry's answer**.

| Required next relation | Pop while | Pending value order after pushing current |
| --- | --- | --- |
| Strictly greater | `topValue < current` | Nonincreasing |
| Greater or equal | `topValue <= current` | Strictly decreasing |
| Strictly smaller | `topValue > current` | Nondecreasing |
| Smaller or equal | `topValue >= current` | Strictly increasing |

For `[2,2,3]`, strict next-greater indices are `[2,2,-1]`. The second 2 must not resolve the first 2; both wait for 3. Under greater-or-equal semantics, answers become `[1,2,-1]`. Replacing `<` with `<=` changes the problem being solved.

### 9.3 Right-to-left candidate pruning table

Here the current position asks for its next qualifying position to the right. The stack already contains candidates from that right side. Pop entries that cannot be the answer and are dominated by the current, nearer position for future queries to the left.

| Required next relation | Prune while | Candidate value order after pushing current |
| --- | --- | --- |
| Strictly greater | `topValue <= current` | Strictly decreasing |
| Greater or equal | `topValue < current` | Nonincreasing |
| Strictly smaller | `topValue >= current` | Strictly increasing |
| Smaller or equal | `topValue > current` | Nondecreasing |

For strictly greater, an equal candidate to the right cannot answer current. For future positions farther left, current is at least as useful a value and is nearer, so the farther equal candidate can be removed. That is why equality is popped here even though the forward strict-greater design retains equal unresolved entries.

These tables are checks on a derivation, not a substitute for one. First name the unresolved/candidate state, scan direction, and requested inequality. Then justify which entries are removed and where the answer is written.

### 9.4 Normal versus monotonic stack

| Property | Ordinary stack | Monotonic stack |
| --- | --- | --- |
| Required value order | None | An explicit monotone value relation |
| Meaning of pop | Defined by the problem's LIFO operation | Resolves a pending answer or removes a dominated candidate |
| Can one input pop many entries? | Depends on the algorithm | Yes, while restoring the invariant |
| Always keeps all inserted values? | Until the program removes them | Usually keeps only unresolved/useful entries |
| Typical proof | Matching, evaluation, undo | Matching plus an ordering/dominance argument |

**Self-check D1:** Why can `<` be correct in one strict next-greater algorithm while `<=` is correct in another?

**Self-check D2:** What does an all-equal input reveal about the intended strictness of the answer relation?

## 10. Next Greater and Next Smaller Patterns

### 10.1 First later strictly higher value: forward resolution

**Teaching contract:** For each position i in a read-only arbitrary int array, return the smallest j with `j > i` and `values[j] > values[i]`, or −1 if none exists. Duplicates and both int extremes are allowed. Output is indices in original input order, not a sorted value list.

The nested-scan baseline is O(n²) worst-case time, O(1) auxiliary space, and O(n) output. Derive the stack algorithm from the repeated future searches described in Section 8:

1. Initialize every answer to −1, the default for unresolved positions.
2. Scan new positions from left to right.
3. While current is strictly higher than the pending top, pop that index and write current as its answer.
4. Push current because its own future answer is not known yet.
5. Leave entries still pending at the end as −1.

```java
static int[] nextHigherIndices(int[] values) {
    int[] next = new int[values.length];
    Arrays.fill(next, -1);
    Deque<Integer> pending = new ArrayDeque<>();
    for (int i = 0; i < values.length; i++) {
        while (!pending.isEmpty() && values[pending.peek()] < values[i]) {
            int resolved = pending.pop();
            next[resolved] = i;
        }
        pending.push(i);
    }
    return next;
}
```

Dry run for `[4,2,2,5,3]`; each stack entry is `index:value`:

| Step | Index | Current value | Stack before, bottom to top | Actions | Stack after | Results written |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 4 | `[]` | Push 0 | `[0:4]` | None |
| 2 | 1 | 2 | `[0:4]` | Push 1 | `[0:4,1:2]` | None |
| 3 | 2 | 2 | `[0:4,1:2]` | Equal is not greater; push 2 | `[0:4,1:2,2:2]` | None |
| 4 | 3 | 5 | `[0:4,1:2,2:2]` | Pop 2,1,0; then push 3 | `[3:5]` | `next[2]=3`, `next[1]=3`, `next[0]=3` |
| 5 | 4 | 3 | `[3:5]` | Push 4 | `[3:5,4:3]` | None |
| End | — | — | `[3:5,4:3]` | Leave unresolved answers at −1 | Same | Return `[3,3,3,-1,-1]` |

**Invariant before i:** pending indices are increasing from bottom to top; their values are nonincreasing; each has no qualifying answer among processed positions after it. Every removed index already has its correct first later higher answer.

If the top value is smaller than current, current qualifies. It is the first qualifier because that index remained unresolved through every earlier scanned position. Once popped, the same proof applies to the new top. If the top blocks the pop, all deeper values are at least as large as that top, so current cannot qualify for them. Pushing current restores nonincreasing value order and starts its own unresolved obligation. At the end, unresolved entries have no higher value to their right because every right-side position has been processed.

There are n pushes and at most n pops, so total stack work is O(n), including a while loop that can pop many entries in one iteration. Maximum size can be n, as on decreasing or all-equal input. With ArrayDeque endpoint costs, total time is O(n) amortized, auxiliary space O(n), and output O(n). No sorting or input mutation occurs.

| Input | Expected next-higher indices | What it checks |
| --- | --- | --- |
| `[]` | `[]` | No access on empty |
| `[7]` | `[-1]` | No future position |
| `[2,2,2]` | `[-1,-1,-1]` | Equality does not resolve strict greater |
| `[1,2,3]` | `[1,2,-1]` | Immediate future answers |
| `[3,2,1]` | `[-1,-1,-1]` | Unresolved suffix / maximum stack |
| `[-2,-1,-3]` | `[1,-1,-1]` | Negative values |
| `[MIN,MAX]` | `[1,-1]` | Comparison requires no risky subtraction |

For a Daily Temperatures-style **distance**, a found index j gives `j−i`; an unresolved answer may use 0 according to that task's contract. Storing only values cannot recover this distance. Do not replace “nearest higher” with “largest value to the right”: on `[2,3,9]`, the first answer is index 1, not index 2.

### 10.2 First later strictly lower value: candidate pruning

**Teaching contract:** The same read-only int-array domain, but return the smallest j with `j > i` and `values[j] < values[i]`, or −1. Output remains in original position order. We intentionally use a different direction to show that an invariant, not a remembered comparison, determines the method.

The brute force is again O(n²) worst-case time, O(1) auxiliary space, O(n) output. Scan from right to left so the right-side candidates are already available. A candidate with value at least current cannot answer current's strictly-lower query. It can also be removed for future queries to the left: current is nearer and no larger, so whenever that old candidate could qualify, current would be at least as good and nearer.

```java
static int[] nextLowerIndices(int[] values) {
    int[] next = new int[values.length];
    Deque<Integer> candidates = new ArrayDeque<>();
    for (int i = values.length - 1; i >= 0; i--) {
        while (!candidates.isEmpty() && values[candidates.peek()] >= values[i]) {
            candidates.pop();
        }
        next[i] = candidates.isEmpty() ? -1 : candidates.peek();
        candidates.push(i);
    }
    return next;
}
```

Dry run for `[5,3,3,4,2]`:

| Step | Index | Current value | Stack before, bottom to top | Actions | Stack after | Result for current |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 4 | 2 | `[]` | No candidate; push 4 | `[4:2]` | `next[4]=-1` |
| 2 | 3 | 4 | `[4:2]` | Top qualifies; push 3 | `[4:2,3:4]` | `next[3]=4` |
| 3 | 2 | 3 | `[4:2,3:4]` | Prune 3; top 4 qualifies; push 2 | `[4:2,2:3]` | `next[2]=4` |
| 4 | 1 | 3 | `[4:2,2:3]` | Prune equal 2; top 4 qualifies; push 1 | `[4:2,1:3]` | `next[1]=4` |
| 5 | 0 | 5 | `[4:2,1:3]` | Top 1 qualifies; push 0 | `[4:2,1:3,0:5]` | `next[0]=1` |

Return `[1,4,4,4,-1]`. The equal 3 at index 2 is not a lower answer for index 1.

**Invariant:** the stack is an undominated representation of processed right-side candidates. Indices decrease from bottom to top, so its top is the nearest retained position; values increase strictly from bottom to top. Every removed candidate has a nearer candidate with a no-greater value that can replace it for future strictly-lower queries.

After pruning, a remaining top is strictly lower and is the nearest useful candidate. A nearer processed position omitted from the stack cannot be a better answer: its dominating replacements are nearer still and no larger. If no candidate remains, no lower position exists in the processed suffix. Pushing current maintains strict increase because all remaining values are below it. The reverse loop terminates after exactly n indices and does not dereference −1.

There are n pushes and at most n pops, O(n) total amortized time, O(n) auxiliary space, and O(n) output. The maximum stack can be n; `[3,2,1]` builds it during right-to-left traversal. All-equal input returns all −1 and retains only one candidate at a time. `[1,2,3]` returns all −1; `[3,2,1]` returns `[1,2,-1]`; `[]` returns `[]`; `[MAX,MIN]` returns `[1,-1]`.

### 10.3 Boundaries of the technique

Both examples require the **first positional witness** satisfying a monotone value comparison. They are unsuitable as-is for arbitrary predicates such as “first future value with the same hash,” global frequency queries, kth greater values, or comparisons based on a changing external state. A stack pop needs either a completed-answer proof or a dominance proof; neither follows merely from the words “next element.”

Common mistakes include using `if` instead of `while` when one current value resolves several entries, resolving equals under a strict relation, writing the answer at current instead of the popped index in the forward design, leaving default zeros where −1 is required, and sorting away original positions. In the candidate design, confusing the top's index with its value can silently corrupt comparisons.

**Self-check G1:** Why is index 3 the first higher answer for all three popped positions in the first dry run?

**Self-check G2:** In the right-to-left lower-candidate scan, why can a farther equal-valued candidate be discarded permanently?

## 11. Optional Advanced Monotonic Boundaries

This section extends next/previous smaller reasoning to the roadmap's **C — optional** histogram challenge. Basic monotonic scans are core; mastering histogram rectangles is not required for ordinary readiness. The code returns an original **per-position support profile**, rather than the assigned problem's single maximum.

### 11.1 Derive maximal support at one fixed height

**Teaching contract:** Heights are nonnegative ints at unit-width positions, in original order. For each i, return the area of the widest contiguous interval containing i on which every height is at least `heights[i]`, using exactly that height for the area. Preserve the input; return a long array in input order. Equal bars do not block each other. Empty input returns an empty profile.

A direct baseline expands left and right separately for each bar until it meets a strictly shorter bar. All-equal heights force O(n²) work. The returned profile uses O(n) output; the direct expansion needs O(1) auxiliary state. The repeated searches are for nearest **strictly smaller boundaries**, which earlier monotonic reasoning can compute in linear total work.

Let L be the previous strictly smaller index, or −1 if absent. Let R be the next strictly smaller index, or n if absent. The maximal interval at this height is `(L,R)`, meaning indices `L+1` through `R−1`, with width `R−L−1`. The blocking positions themselves must not be included.

```text
heights:       [ 2, 1, 4, 4, 2 ]
indices:         0  1  2  3  4
for i = 2:          L  [=====] R
                    1    4,4   2

The equal 4 at index 3 is support, not a blocking boundary.
L=1, R=4, width=2, height=4, area=8.
```

### 11.2 Previous smaller and the support profile

The previous-smaller helper scans left to right. Its candidates have strictly increasing values bottom to top. A top at least current cannot answer current's strictly-smaller query and is dominated for future queries by current, which is nearer and no larger. This mirrors the next-smaller candidate proof with the direction reversed.

```java
static int[] previousLowerIndices(int[] values) {
    int[] previous = new int[values.length];
    Deque<Integer> candidates = new ArrayDeque<>();
    for (int i = 0; i < values.length; i++) {
        while (!candidates.isEmpty() && values[candidates.peek()] >= values[i]) {
            candidates.pop();
        }
        previous[i] = candidates.isEmpty() ? -1 : candidates.peek();
        candidates.push(i);
    }
    return previous;
}

static long[] levelSupportAreas(int[] heights) {
    int[] previous = previousLowerIndices(heights);
    int[] next = nextLowerIndices(heights);
    long[] areas = new long[heights.length];
    for (int i = 0; i < heights.length; i++) {
        int rightBoundary = next[i] == -1 ? heights.length : next[i];
        int width = rightBoundary - previous[i] - 1;
        areas[i] = (long) heights[i] * width;
    }
    return areas;
}
```

`previousLowerIndices` itself accepts arbitrary ints and duplicates. The profile method has the narrower nonnegative-height contract. The two helpers' returned arrays become **auxiliary** when used inside the profile calculation; only the long profile is required final output.

### 11.3 Detailed boundary dry run

Previous-boundary scan for `[2,1,4,4,2]`:

| Index | Current | Candidate stack before | Actions | Previous smaller | Stack after |
| --- | --- | --- | --- | --- | --- |
| 0 | 2 | `[]` | No candidate; push 0 | −1 | `[0:2]` |
| 1 | 1 | `[0:2]` | Pop 0; push 1 | −1 | `[1:1]` |
| 2 | 4 | `[1:1]` | Top qualifies; push 2 | 1 | `[1:1,2:4]` |
| 3 | 4 | `[1:1,2:4]` | Pop equal 2; push 3 | 1 | `[1:1,3:4]` |
| 4 | 2 | `[1:1,3:4]` | Pop 3; push 4 | 1 | `[1:1,4:2]` |

The right-to-left helper produces next-smaller indices `[1,-1,4,4,-1]`. Combine the boundaries:

| i | Height | L | R, using n if absent | Width | Area |
| --- | --- | --- | --- | --- | --- |
| 0 | 2 | −1 | 1 | 1 | 2 |
| 1 | 1 | −1 | 5 | 5 | 5 |
| 2 | 4 | 1 | 4 | 2 | 8 |
| 3 | 4 | 1 | 4 | 2 | 8 |
| 4 | 2 | 1 | 5 | 3 | 6 |

Return `[2,5,8,8,6]`. On `[3,3]`, both bars span width 2 and the profile is `[6,6]`. On `[0,0]`, it is `[0,0]`; on `[7]`, `[7]`; on `[]`, `[]`. `[MAX,MAX]` gives `[4294967294,4294967294]`, requiring widening before multiplication.

### 11.4 Correctness, duplicate policy, and limits

The boundary helpers find the closest strictly shorter bars by their candidate invariants. No bar inside `(L,R)` is shorter than the selected height, so the whole interval supports it. If a blocking boundary exists, extending beyond it would include a shorter bar and violate the fixed-height requirement. Thus the computed interval is both valid and maximal for that bar.

Both sides use **strictly smaller** boundaries, so equal heights are removed from candidate stacks and do not stop a span. Multiple bars can therefore report the same span/area. That is correct for this profile and for taking a maximum afterward: duplicate witnesses do not alter a maximum. For a different task that sums contributions and must assign each interval to exactly one equal minimum, equal elements need a unique ownership convention, often one strict boundary and one non-strict boundary. Do not transfer that asymmetric rule here without checking the output definition.

Each helper pushes n indices and pops at most n. Two boundary scans plus one area pass take O(n) total amortized time. The two boundary arrays and helper stack use O(n) auxiliary space; the returned profile is O(n) output. This is not an O(1)-space algorithm merely because area calculation uses a few scalar locals.

The profile introduces the reasoning behind histogram rectangles. To connect it to a largest-rectangle question, explain why some minimum-height bar witnesses any candidate rectangle and why extending its fixed-height support cannot decrease area for nonnegative heights. Derive that argument yourself before attempting the optional platform problem. A direct single-stack area-at-pop formulation is a further storage/implementation variant, not a prerequisite here.

Common mistakes are counting a blocking bar in the width, using 0 as the absent-left sentinel, treating equal bars as strictly smaller, multiplying in int before assigning to long, and forgetting intervals that extend to an input edge. This method assumes unit widths, nonnegative heights, and contiguous original positions; nonuniform coordinates need a different width calculation. Sorting heights would destroy the geometry.

**Self-check B1:** Why is width `R−L−1`, and what do the −1/n sentinels represent?

**Self-check B2:** Why are duplicate support areas harmless here, while a contribution-counting problem may need asymmetric equality rules?

## 12. Correctness and Stack Invariants

### 12.1 A reusable derivation workflow

```text
State the problem and exact output
  -> Clarify ordering, ties, mutation, and arithmetic constraints
  -> Build a straightforward correct approach
  -> Identify repeated work or unresolved information
  -> Ask whether the next needed item is the most recent unresolved one
  -> Introduce the smallest sufficient stack entry
  -> Derive push/pop rules and a precise invariant
  -> Prove initialization, preservation, and the final result
  -> Prove termination and count time/space
```

Do not start with “this is a stack problem.” Start with what a simple solution would keep rediscovering. An invariant should connect the stored entries to the problem, not just assert that a data structure is sorted or nonempty.

### 12.2 What each invariant must establish

| Pattern | Meaning of stored state | Essential preservation argument | Final condition |
| --- | --- | --- | --- |
| Delimiter matching | Exactly the unmatched openings, in nesting order | The next closer must match the latest unresolved opener's type | No unmatched openings remain |
| Postfix evaluation | Values of uncombined completed subexpressions, in order | Combine the last two in left/right order | Exactly one value remains |
| Augmented stack | Logical values plus correct aggregate at every depth | Append updated metadata; pop reveals an already valid older level | Top metadata answers the current stack query |
| Forward next greater | Unresolved earlier indices with nonincreasing values | Current resolves the eligible suffix, and no earlier position qualified | Unresolved entries have no answer |
| Next/previous candidate scan | Undominated candidates with strict value order | A removed candidate has a nearer no-worse replacement | Top is the nearest qualifying retained witness, or none exists |
| Fixed-height boundaries | Nearest strictly shorter positions on each side | Monotonic pruning preserves nearest useful blockers | Open interval between blockers is maximal support |

For each algorithm, ask:

1. **Initialization:** why does empty state represent the processed empty prefix/suffix correctly?
2. **Preservation:** why is each push useful, and what justifies every pop or answer assignment?
3. **Coverage and exclusion:** are all required answers found, and are false/duplicate answers excluded as the contract requires?
4. **Termination:** which index advances or finite stack shrinks?
5. **Postcondition:** why does the invariant at the end imply exactly the requested output?

Counting pushes and pops proves a work bound, not correctness. For example, the wrong strictness can still push/pop each index at most once and compute the wrong answers in O(n). A type-mismatched bracket validator can likewise be linear and incorrect. A termination proof and a correctness proof answer different questions.

### 12.3 Prove a pop according to its role

For **resolution**, show current qualifies and no earlier processed candidate qualified. For **pruning**, show the discarded candidate cannot help the current query and has a nearer replacement whenever it could help a future query. For **grammar reduction**, show the top entries are exactly the subexpressions or obligations that must be consumed next. For **undo**, show the older state still contains the right information when revealed.

The [Two Pointers movement proof](./Two_Pointers/Two_Pointers_Essential_Concepts.md#9-a-method-for-proving-pointer-movements) asks what can safely be discarded from a candidate interval. Stack reasoning asks the same safety question about stored entries, but can retain many unresolved positions rather than only two boundaries.

**Self-check V1:** Why does “every element is popped once” fail to prove that a next-greater result is the *nearest* greater result?

**Self-check V2:** Which extra claim distinguishes a valid candidate-pruning pop from simply deleting a value that fails the current comparison?

## 13. Deriving Time and Space Complexity

### 13.1 Aggregate push/pop counting

For a monotonic scan of n input positions:

- Each index is pushed exactly once.
- Once an index is popped, that same occurrence is never pushed again.
- Therefore successful pop iterations across the **entire scan** total at most n.
- Each outer iteration also performs only bounded non-pop work and at most one final failed pop-condition check.

Total abstract stack work is O(n). The nested while loop does not start a fresh independent n-item scan for every position. In the first next-greater dry run, one iteration pops three entries, while earlier iterations only pushed them. The expensive iteration spends accumulated unresolved work; it does not create a new copy of that work.

Contrast this with n independent queries that each rebuild a stack from the entire input: each individual build may be linear, but the complete program is O(n²). Count the scope in which entries are pushed once. The [existing amortization reference](./Complexity/Complexity_Analysis_and_Foundations.md#13-amortized-complexity) develops the same distinction for resizable collections.

### 13.2 Amortized does not mean every iteration is cheap

For `[9,8,7,6,10]`, the final forward next-greater iteration pops four entries. Its worst-case work is O(n), even though the scan's amortized work per input position is O(1). This bound does not rely on random inputs.

A simple potential argument makes the accounting explicit. Let potential be the number of pending entries. A push costs one abstract operation and increases potential by one; a pop costs one and decreases potential by one. Saved potential pays for later pops. The total change over a scan cannot finance more pops than there have been pushes.

ArrayDeque resizing adds a separate amortized argument beneath the algorithmic one. Its endpoint-operation sequence has amortized constant cost, so O(n) abstract endpoint operations take O(n) total time. A single iteration can perform many pops and a push can occasionally grow storage; neither fact makes the complete scan quadratic.

### 13.3 Complete cost table

Let n be sequence length, k a recursive suffix length, d peak pending depth, q operations on an augmented stack, p its peak size, and C a fixed array capacity.

| Example / algorithm | Total work | Peak stack state | Auxiliary space | Returned output |
| --- | --- | --- | --- | --- |
| FixedIntStack | O(C+1) construction; O(1) per operation | At most C active ints | O(C+1) representation | Scalars |
| LinkedIntStack | O(1) pointer/allocation-model work per operation | s active nodes | O(s+1) reachable representation | Scalars |
| Reversed copy using a stack | n pushes and n pops: O(n) amortized | n entries | O(n) | O(n) array |
| Recursive suffix sum | k+1 calls: O(k+1) | k+1 frames | O(k+1) | Scalar |
| Closing partner indices | At most n pushes/pops plus n output initialization: O(n) amortized | d nested openings | O(d+1) | O(n) indices |
| Postfix evaluation | O(n+L), including token parsing | d uncombined expression values | O(d+1) | Scalar long |
| RangeStack sequence | O(q) total amortized | p levels at peak | O(p+1) representation/capacity | Scalars |
| Next higher / next lower | n pushes, at most n pops: O(n) amortized | At most n indices | O(n) | O(n) indices |
| Previous lower | n pushes, at most n pops: O(n) amortized | At most n indices | O(n) | O(n) indices |
| Optional support profile | Two boundary scans and one area pass: O(n) amortized | At most n indices in either helper | O(n), including helper arrays | O(n) long profile |

Successful fixed-width postfix evaluations take O(n+L); under an explicit bounded-token-length assumption, L is O(n). Invalid inputs stop at the first detected error rather than promising a result. JVM/runtime allocation details are not a claim of constant wall-clock latency for each data-structure operation.

### 13.4 Costs hidden behind familiar calls

`Arrays.fill` and array allocation initialize n positions. Copying a stack into a list or array is linear in its size. `contains`, arbitrary occurrence removal, sorting, parsing, and converting an entire string/collection add work beyond top operations. Debug printing a stack of size s at every input step can add O(n²) output/formatting work even when the underlying algorithm is linear.

For an index stack, storing n boxed Integers remains O(n) space; boxing constants do not make the stack O(1). If a known n bound matters in a performance-sensitive implementation, a primitive `int[]` plus a top/size variable can represent the same monotonic invariant without boxing. Allocating that n-sized buffer upfront is O(n) space even when the actual pending depth stays small.

Recursion uses live frames, not all calls ever made. Similarly, auxiliary space is peak live working data, not the sum of allocations over time. Input and returned output remain real memory even when the chosen auxiliary-space convention excludes them. Keep these quantities separate when comparing implementations.

**Self-check C1:** How can the final iteration take O(n) while the whole scan still takes O(n)?

**Self-check C2:** Why does the optional profile have O(n) auxiliary space even if a boundary helper's result would count as output when called by itself?

## 14. Java Pitfalls and Edge Cases

### 14.1 A failure should have a concrete witness

| Mistake | Witness / consequence | Prevention |
| --- | --- | --- |
| Pop before checking available state | `]` in matching; `+` in postfix | Guard emptiness or required operand count |
| Unbox nullable Deque.peek | Empty `Deque<Integer>` produces null, then unboxing fails | Check emptiness or use an intentional throwing query |
| Push null into ArrayDeque | Throws `NullPointerException` | Do not use null as stored data/sentinel |
| Compare only delimiter counts | `([)]` has balanced counts but wrong nesting | Match the latest unresolved opener's type |
| Ignore leftover openings | `(()` has no early bad closer but is incomplete | Check final stack state |
| Reverse operand order | `3 8 -` should return −5, not 5 | Pop right, then left, and compute left op right |
| Treat any nonempty operand stack as success | `1 2` leaves two expressions | Require exactly one final value |
| Compare operator Strings with `==` | A separately created `new String("+")` need not share identity | Use `.equals` or a String switch |
| Save only values for a positional answer | Equal values at different indices need distinct distances | Store original indices or value/index records |
| Resolve equal values as strictly greater | `[2,2,3]` would give a false answer at index 1 for the first 2 | Derive strictness from the contract |
| Use `if` when several entries become resolved | `[4,2,2,5]` needs three answers at the final position | Restore the invariant with repeated pops |
| Leave default zero for absent indices | Singleton next-greater array would claim index 0 | Initialize absent results to −1, or assign every result explicitly |
| Lose a repeated auxiliary minimum | Push 5,2,2 then pop one 2 | Store per-depth metadata or correct equal-minimum multiplicity |
| Add/subtract/multiply in int before widening | MIN/MAX span and large rectangle areas | Promote an operand before arithmetic |
| Assume long never overflows | Multiplying several valid int literals can exceed long | Check bounds or use the stated exact-arithmetic contract |
| Include a blocking histogram bar | L=1,R=4 supports width 2, not 3 | Use the open boundary interval |
| Confuse index and array length | Empty reverse scan starts at −1 | Test the index before any access |
| Make a linear scan do linear library work each step | Searching/copying/printing the entire stack repeatedly | Count the actual operations inside each iteration |

### 14.2 A compact testing matrix

Apply a case only when it belongs to the method's domain. Negative ints are valid for next-element scans and aggregate stacks, but heights in the optional area profile must be nonnegative. Malformed delimiters/postfix shapes are deliberately rejected by those teaching methods.

| Case family | Example | Required check |
| --- | --- | --- |
| Empty state | Empty manual/RangeStack; empty next-element input | Correct exceptions/queries; empty output array |
| Empty grammar input | Empty delimiter text; empty token array | Empty partner output is valid; empty expression is rejected |
| Singleton | `[7]` next-element; push one minimum | No future answer; exact top/aggregate restoration after pop |
| Two elements | `[2,2]`, `[1,2]`, `[2,1]` | Strict equality, immediate greater, immediate lower |
| All equal | `[3,3,3]` | No strict next answers; equal bars do not block support |
| Monotone inputs | Increasing and decreasing sequences | Correct output and actual peak stack size |
| Many resolutions at once | `[9,8,7,6,10]` | Every eligible pending index is resolved |
| Unresolved suffix | `[4,2,2,5,3]` | Final 5 and 3 retain −1 next-higher answers |
| Wrong nesting | `([)]`, `]`, `(()` | Type mismatch, early closer, and leftover opening detected |
| Arithmetic order | `8 3 -`, `8 3 /`, `7 -3 /` | 5, 2, and −2 respectively |
| Numeric bounds | MIN then MAX in RangeStack | Span 4294967295; no narrow subtraction |
| Height bounds | `[MAX,MAX]` | Both support areas 4294967294 |
| Input preservation | Save arrays/tokens before calls | Contents unchanged by all read-only teaching algorithms |

Use direct enumeration for short next-element arrays and direct left/right expansion for the optional profile. These independent baselines test the requested nearest/maximal property. For the aggregate stack, compare each query after random pushes/pops with a simple list scan. Test exception behavior separately from correct-value tests.

The `Opening` and `RangeStack.Level` records hold primitives, so their components have ordinary value semantics. A record containing an array would not automatically give deep array equality; use `Arrays.equals` for the returned primitive arrays. Array reference `==` does not compare contents. Review [array copying and equality](./Java_Array.md#copying-arrays) rather than duplicating the full reference here.

**Self-check J3:** Why can replacing all top queries with `peek()` introduce a failure when code later unboxes them?

**Self-check J4:** Which two different problems can the input `[2,2,3]` distinguish when testing a monotonic stack?

## 15. Choosing between Stacks and Alternatives

Choose the required access/update order first. Useful questions are: what information is unresolved, what future event resolves it, and why can the most recent item be handled before older ones?

| Required task | Natural candidate | Why / limitation |
| --- | --- | --- |
| Mixed nested delimiters and partner positions | Stack of opener identities/indices | Latest unresolved opener must close first |
| Boolean validity for one bracket type only | Prefix-balance counter | Count plus nonnegative-prefix check is enough; types/partner identities are absent |
| Postfix combination of completed expressions | Operand stack | The newest two expressions are the next operands |
| Fast aggregate after LIFO undo | Augmented stack | Save summary at each depth |
| First next/previous greater/smaller by original position | Monotonic stack | Resolve pending positions or prune dominated candidates |
| Maximum value anywhere in every suffix | Suffix maximum scan | Nearest witness and LIFO removal are unnecessary |
| Membership, counts, or complement lookup among earlier values | HashSet/HashMap | Lookup history matters, not a top-only ordering |
| Pair target in sorted input | Two Pointers | Ordered endpoint elimination can use O(1) state |
| Reverse an array in place | Two Pointers | A stack works but adds unnecessary O(n) working storage |
| Process earliest arrival first | FIFO queue | LIFO would choose the wrong next item |
| Process globally highest/lowest priority | Priority queue, a later roadmap topic | Neither insertion order nor nesting establishes priority |
| Queries over a changing contiguous window | A separately derived window/deque design | Monotonic stack alone does not remove items by window expiry correctly |

A list is a representation choice, not a substitute for this reasoning. A hash map can locate a matching type somewhere in history but does not preserve the nested obligation of `([)]`. A simple stack can retain all values but does not automatically support arbitrary search or expiration. Two Pointers can reduce stored state when a pair of boundaries summarizes the unresolved region, but next-element answers may require retaining many separate unresolved positions.

The [Arrays & Hashing recognition guide](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md#13-pattern-recognition-cheat-sheet) and [Two Pointers technique comparison](./Two_Pointers/Two_Pointers_Essential_Concepts.md#12-choosing-between-techniques) provide the earlier alternatives. Window algorithms and priority queues are later branches; mentioning them here does not make them prerequisites for the Stack category.

**Self-check T1:** Why is “the problem asks about the future” insufficient to choose a monotonic stack over a suffix summary?

**Self-check T2:** Which property makes a single-type boolean bracket question simpler than mixed delimiter matching with partner indices?

## 16. Pattern Recognition Practice

For each scenario, propose a baseline, name the minimal useful state, and explain the operation order. If you choose a stack, state what a push means, what a pop proves, whether indices are required, and which ties count. If you reject a stack, give a reason tied to the contract. Answers are delayed until Section 17.

| ID | Unfamiliar scenario |
| --- | --- |
| R1 | A configuration fragment uses three types of nested delimiters. Return the original opening position for each closer; quoted text has no special grammar here. |
| R2 | A stream of task scores needs the first strictly higher later score's position gap for every original entry. Equal scores are not sufficient. |
| R3 | A history accepts append and undo-last; report the smallest current score after each operation, including repeated smallest scores. |
| R4 | For every measurement, return the maximum measurement anywhere to its right, regardless of how near it is. |
| R5 | Given sorted prices, decide whether two distinct positions have a specified total while preserving the input. |
| R6 | A log asks how many times each arbitrary ID has occurred, and can query any ID rather than the latest one. |
| R7 | A tiny calculator receives complete numeric tokens followed by binary operators in postfix order. |
| R8 | For each unit-width support column, find how far its height can extend through neighboring columns of at least that height. |

Vary one assumption before declaring mastery. Change strictly higher to higher-or-equal, values to distances, boolean validity to partner indices, or LIFO undo to deleting any position. Explain which invariant survives and which must be redesigned. A remembered while comparison is not a transferable answer.

## 17. Conceptual Assessment and Answer Key

First answer the section self-checks with references closed. Then rehearse these interview prompts in your own words:

1. Derive a delimiter algorithm from a reduction baseline. Explain the earliest point at which a bad prefix is unrecoverable.
2. Explain the operand stack as completed expressions, not merely numbers. Prove the pop order for a noncommutative operator.
3. Derive constant-time minimum queries after undo. Show why repeated equal minima need deliberate state.
4. State every clause of the next-higher invariant and use it to prove the answer is the first qualifier.
5. Explain why a candidate can be removed permanently even if it might satisfy a query farther away.
6. Count total pushes/pops and maximum live state. Contrast that analysis with one iteration's worst case and with recursive calls/depth.

Keep conceptual understanding, implementation, and unfamiliar recognition separate. Use the answer key as feedback after a concrete attempt, then explain again without reading it.

<details>
<summary>Concise self-check answers — open after attempting</summary>

| Checks | Core idea |
| --- | --- |
| F1 / F2 | Balanced counts lose nesting order; a closer must match the latest unresolved opener. Peek preserves both the entry and size; pop removes one. |
| J1 / J2 | Empty Deque.peek returns null, and unboxing null fails. Fixed capacity is allocated regardless of size; geometric growth spreads occasional copies over many pushes. |
| RC1 / RC2 | Constant local state per frame still accumulates over live depth. Sequential branches can release one subtree's frames before exploring the next. |
| M1 / M2 | The top unmatched `[` cannot be closed by `)`, so the prefix is already invalid. Maximum nesting is simultaneous unresolved depth, not the total number of openings seen. |
| E1 / E2 | First pop is the right operand, so reversing operands changes subtraction/division. Two leftover values represent two uncombined expressions, not one result. |
| A1 / A2 | Popping the minimum can expose a larger older minimum that one scalar forgot. Each stored level preserves its own prefix aggregate, including equal values. |
| N1 / N2 | A pending entry has no first qualifying future witness yet. Distance needs original positions; equal values alone do not identify them. |
| D1 / D2 | Forward resolution pops when current answers an older entry; reverse candidate pruning pops when a nearer entry makes a candidate useless. Equal inputs reveal whether equality qualifies or must be skipped. |
| G1 / G2 | Earlier arrivals did not qualify, or those entries would already have been resolved. A nearer equal candidate is just as small and nearer for all later left-side queries. |
| B1 / B2 | Boundaries themselves are excluded; −1/n are positions just outside the input. Repeated witnesses do not change a maximum/profile, but counting contributions needs unique ownership. |
| V1 / V2 | A work count says nothing about first-qualifier correctness. Pruning also needs a proof that no future query loses a needed better candidate. |
| C1 / C2 | One late iteration can spend entries accumulated earlier; total pops remain at most n. A helper result used only for a larger computation is working state of that larger computation. |
| J3 / J4 | Peek's null sentinel becomes an unboxing failure if unchecked. `[2,2,3]` separates strict greater from greater-or-equal and exposes wrong equal handling. |
| T1 / T2 | “Future” can mean a whole-suffix aggregate rather than the nearest witness. One bracket type and a boolean result need less identity/type information than mixed matching with positions. |

| Scenario | Suitable direction and proof obligation |
| --- | --- |
| R1 | Stack of opening types/indices; the inner unresolved construct must close first. |
| R2 | Monotonic unresolved indices; resolve only on a strict increase and compute position differences. |
| R3 | Augmented stack; save/restore the minimum at each depth or maintain correct minimum multiplicities. |
| R4 | Suffix maximum; nearest-position elimination is not the requested result. |
| R5 | Sorted Two Pointers; justify discarding all pairs involving an impossible endpoint. |
| R6 | Frequency map; arbitrary-key counts are not top-only LIFO work. |
| R7 | Operand stack; reduce the last two completed expressions in left/right order. |
| R8 | Optional nearest strictly-smaller boundaries; prove the interval is maximal and handle equal heights consistently. |

The six interview prompts correspond to Sections 5, 6, 7, 10, 10–11, and 13. A satisfactory response supplies its own contract, proof, and cost argument rather than just a technique name.

</details>

## 18. Key Takeaways and Interview Explanation

A stack is useful because the most recent unresolved entry is the right one to handle next. Matching uses nested obligations; postfix evaluation combines completed expressions; augmentation stores history summaries; monotonic scans retain unresolved indices or undominated candidates. Each pattern gives a different meaning to push, pop, equality, and final state.

Use this explanation structure on an unfamiliar problem:

> “The baseline repeats ___ and costs ___. The information that must wait is ___. An entry stores ___ because the output needs ___. Before each step, the stack represents ___. I can pop when ___ because ___. Equal values mean ___ under this contract. At the end ___. Each occurrence is pushed ___ times and popped ___ times, so total time is ___. The largest live stack has ___ entries; including other working data and returned output, space is ___.”

Then challenge the explanation. If the output changes from a value to an index, if equality starts qualifying, or if operations cease to be LIFO, the representation or algorithm may have to change. A clear reason for choosing another technique is evidence of understanding too.

## 19. References, Validation, and Next Steps

### Repository and primary references

- [Stack requirements and practice priorities](./DSA_Interview_Roadmap.md#3-stack), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category), and [revision process](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition).
- [Java Arrays](./Java_Array.md), [Java Strings](./Java_String.md), and [Lists/deque endpoints](./Java_List_ArrayList_LinkedList.md#linkedlist-end-methods-ranked).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), especially [recursive costs](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) and [amortization](./Complexity/Complexity_Analysis_and_Foundations.md#13-amortized-complexity).
- [Arrays & Hashing concepts](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md) and [Two Pointers concepts](./Two_Pointers/Two_Pointers_Essential_Concepts.md) for choosing state, invariants, and elimination arguments.
- [Official NeetCode Roadmap](https://neetcode.io/roadmap) is the curriculum behind the local roadmap. The local document supplies this guide's exact practice titles, difficulties, and E/A/C priorities; the public page's dynamic rendering is not used to infer additional assignments.
- Official Java 21 [Deque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html), [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html), [Stack](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Stack.html), and [LinkedList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedList.html) document the collection contracts. These were checked using Context7 and official documentation.
- Java 21 [Integer parsing](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html), [Math exact operations](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html), and [integer division rules](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.2) support the postfix example's explicit numeric contract.

### Validation and limits

All **11 Java blocks** compiled together with Temurin JDK 21 using `javac --release 21 -Xlint:all -Werror`. A temporary harness outside the repository passed **59,903 checks**. These included the documented examples and exception contracts, 2,500 paired manual-stack operations, 2,000 augmented-stack operations, 700 seeded array pairs, all 1,093 sequences of lengths 0–6 over values 0–2 and their shifted negative variants, 6,492 delimiter cases, and 1,800 generated postfix expressions. A separate 10,000-code-unit nesting case exercised the explicit delimiter stack without recursive parsing.

Independent nested scans checked nearest greater/smaller positions; direct expansion checked support areas; list scans checked minimum/maximum metadata; adjacent-pair reduction checked delimiter matching; and BigInteger-based list reductions checked postfix results and overflow rejection. Tests covered all 128 ASCII code units in delimiter contexts, input preservation, repeated extrema, absent answers, integer extremes, long arithmetic overflow, division order, malformed expression shapes, and long leading-zero token spellings. Recursive examples were tested only at small depths, consistent with their stated purpose.

All **49 local Markdown links**, including heading anchors, were verified. The five practice entries match the roadmap's order, titles, difficulties, and E/A/C priorities. All 17 pre-existing workspace files were preserved; only this guide was added. These checks validate the teaching examples, not learner readiness or a platform submission. The invariants and operation counts remain the general correctness and complexity arguments.

The examples are educational interfaces, not a complete answer bank for the assigned platform problems. The ASCII delimiter model does not parse quotes/comments, the postfix evaluator accepts int literals with checked long intermediate arithmetic, and histogram support assumes nonnegative unit-width heights. Recursive suffix sum is intended for small demonstrations; an arbitrary large recursive chain can exhaust the JVM call stack. These contracts remain part of correctness.

### Next steps using the existing roadmap

Close the guide and predict a deque operation trace, including empty/null behavior. Rebuild the roadmap's bracket and postfix drills from a blank editor, then implement next-greater indices and compare them with a nested scan on all-equal, increasing, decreasing, and seeded short arrays. Rebuild the minimum invariant and demonstrate repeated equal minima after pops. Explain each invariant and total operation count without notes before using those drills as I evidence.

Attempt the assigned problems in roadmap order. Record I/H/S assistance results and review dates on their existing rows; use about 1, 3, 7, and 14 days as flexible cold-review intervals, prioritizing S and repeated H. The three Essential problems support P only through independent solves or cold re-solves, not remembered code. Postfix reasoning still needs a demonstration even when its Additional platform problem is deferred.

To assess R, use an unseen deferred-work variant with references and tags hidden. Explain why a stack fits, choose indices or values deliberately, handle ties and unresolved suffixes, test empty state and operand order as applicable, and prove total push/pop work. Repeat unfamiliar success across separate sessions using the roadmap's common rubric. The optional Hard and advanced boundaries never block ordinary Stack readiness. No progress gate is earned merely because this document exists or its examples compile.
