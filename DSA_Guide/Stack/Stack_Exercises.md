# Stack — Exercises (Java 21)

- [1. Introduction](#1-introduction)
- [2. Problem-Solving Workflow](#2-problem-solving-workflow)
- [3. Level 1 — Stack Fundamentals](#3-level-1--stack-fundamentals)
- [4. Level 2 — Core Stack Patterns](#4-level-2--core-stack-patterns)
- [5. Monotonic Stack Practice](#5-monotonic-stack-practice)
- [6. Level 3 — NeetCode / LeetCode Practice](#6-level-3--neetcode--leetcode-practice)
- [7. Level 4 — Mixed Interview Challenges](#7-level-4--mixed-interview-challenges)
- [8. Complexity Analysis Exercises](#8-complexity-analysis-exercises)
- [9. Debugging Challenges](#9-debugging-challenges)
- [10. Mock Interview Questions](#10-mock-interview-questions)
- [11. Revision and Spaced Repetition](#11-revision-and-spaced-repetition)
- [12. Completion and Readiness Checklist](#12-completion-and-readiness-checklist)
- [13. References and Delayed Checks](#13-references-and-delayed-checks)

## 1. Introduction

Use this workbook to turn [Stack theory](./Stack_Essential_Concepts.md) into independent Java implementations and interview explanations. The [Stack roadmap](./DSA_Interview_Roadmap.md#3-stack) determines required scope and priorities. Read a relevant concept, close it, and reconstruct the reasoning. Copying a working example is not an independent solve.

There are **31 original exercises**: **18 coding exercises**, **six complexity exercises**, and **seven debugging challenges**. The **five curated platform problems** retain the roadmap's order and E/A/C classifications. Mock sessions reuse existing exercises; they are not extra assignments or attempt logs.

| Level | Exercises | Evidence to develop |
| --- | --- | --- |
| 1 — Fundamentals | F1–F4 | Predict LIFO, use endpoints, traverse without damage, handle empty state |
| 2 — Core patterns | P1–P3 and M1–M6 | Derive matching, evaluation, augmentation, monotonic order, and boundaries |
| 3 — Curated practice | N1–N5 | Solve the exact judge contract independently |
| 4 — Mixed challenges | X1–X5 | Choose an algorithm with tags and hints hidden |

M6 and N5 are **optional advanced work**. Neither blocks ordinary readiness. Postfix reasoning is core even though its platform problem N3 is Additional. Complexity and debugging exercises support the four levels rather than forming additional levels.

| Roadmap skill | Primary evidence |
| --- | --- |
| LIFO, Java deque operations, explicit versus recursive stack | F1–F4, C1, C4 |
| Matching and nested validation | P1, N1, D1 |
| Postfix operand order and expression shape | P2, N3, D2 |
| Minimum metadata and restoration after pop | P3, N2, D6 |
| Indices versus values and unresolved entries | M2–M5, N4, D3–D5 |
| Equality and monotonic increasing/decreasing orders | M1–M5, D3, D6 |
| Total push/pop work and peak storage | C1–C6, M2–M5, D7 |
| Unfamiliar selection, proof, and communication | X1–X5 and mock sessions |

### Shared contracts

Unless narrowed below, inputs and their elements are non-null, lengths/operation counts are at most **100,000**, allocations are feasible, and execution is single-threaded. Arrays may contain any int and duplicates. Indices are zero-based; intervals are half-open. Every input array is **read-only**, including nested rows, unless a specific contract says otherwise. Do not sort a read-only array in place. Invalid values outside stated preconditions need not be checked; explicitly specified rejection cases are part of the tested contract.

Strings use ASCII code units 0–127. Delimiter exercises recognize only `()[]{}` and ignore other code units; quotes/comments have no special meaning. `MIN`/`MAX` mean the int extremes. `NONE` means **`Long.MIN_VALUE`** in long-valued test results; it is distinct from every valid int value. Table shorthand must be expanded to Java literals in a test class. An empty array is `[]`; a record is displayed by its fields in declaration order.

**Cost convention:** auxiliary space excludes the required returned output, which must be reported separately. Working stacks, copied input, intermediate arrays, and temporary builders count as auxiliary. For an ArrayDeque retained across operations, report peak allocated storage in terms of the historical peak size, not just its current size. Read O(n) as O(n+1) at n=0. Hash-table bounds are expected/amortized under ordinary fixed-size integer-key hashing. Arithmetic on int/long is fixed-width; widening must happen before an overflowing operation.

### One attempt record per item

Each unchecked line has completion, last date, result, minutes, next review date, and lesson fields. Check it after an attempt has been tested and a postmortem written; a checked box alone does not mean independent success.

- **I:** independently correct, tested, and explained without hints.
- **H:** a conceptual hint was needed, including opening a theory link during an assessed attempt.
- **S:** substantial solution/code review was needed, or the attempt remained unfinished.

Update the same line after a review and retain brief useful history there. The [roadmap's T/I/P/R gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain the only topic dashboard. For N1–N5, keep the detailed date/result/time/due/lesson record in the **existing roadmap problem row**; the workbook checkbox and Log reference point there. Do not maintain duplicate dates or results in both places. Nothing in this document automatically earns a gate.

## 2. Problem-Solving Workflow

Use this sequence for each meaningful problem. For a small F drill, a short trace and a few sentences are sufficient.

1. Restate the output and what counts as the same answer.
2. Identify constraints, mutation rules, arithmetic limits, and invalid-input behavior.
3. Propose a simple correct approach, even if slow.
4. Derive its time and auxiliary space.
5. Identify repeated work, deferred obligations, or state that must survive an undo.
6. Decide whether a stack helps; compare at least one plausible alternative.
7. Derive an improvement from the problem's order or state requirements. Use another technique when it fits better.
8. State the invariant: what each stored item means, why a push/pop is safe, and what is already correct.
9. Implement in Java 21 from a blank editor.
10. Run the supplied cases, add a counterexample to a tempting wrong approach, and check input preservation.
11. Explain correctness, termination, total time, peak auxiliary storage, and output size; update the attempt line honestly.

Before a pop, explain whether it **finishes an answer**, **discards a dominated candidate**, or **undoes an operation**. Those reasons require different proofs. For monotonic work, specify scan direction, bottom-to-top value order, and strictness before writing a comparison. Consult [the invariant reference](./Stack_Essential_Concepts.md#12-correctness-and-stack-invariants) only after attempting your own explanation.

### Java and testing conventions

Place the coding stubs inside a scratch class named `Practice`, with `import java.util.*;`. Nested records/classes belong inside that class. Replace the placeholders yourself. Analysis and debugging snippets later are executable subjects for inspection, not implementations of the coding exercises. Use only the standard library; no test dependency is required.

For an ArrayDeque stack, `push`/`pop` operate at the first end; ordinary iteration is first-to-last. `peek()` returns null on empty, while `pop()` throws `NoSuchElementException`. ArrayDeque rejects null. Use these facts deliberately instead of mixing queue and stack endpoints. See [the Java implementation reference](./Stack_Essential_Concepts.md#3-stack-operations-and-java-implementations).

For arrays, compare contents with `Arrays.equals` or `Arrays.deepEquals`; compare record array fields separately because record equality does not make arrays content-equal. For M2–M5 and X5, write a slow independent nested-scan oracle for small random inputs. For P3, a plain list with a scan for min/sum is a useful test model. Test maximum-sized structured inputs using known formulas rather than a quadratic oracle.

This optional smoke harness checks one M2 contract and input preservation without supplying the algorithm:

```java
public final class PracticeSmoke {
    public static void main(String[] args) {
        int[] input = {2, 2, 3};
        int[] before = input.clone();
        Practice.NeighborResult answer = Practice.nextGreater(input);
        if (!java.util.Arrays.equals(answer.indices(), new int[]{2, 2, -1})
                || !java.util.Arrays.equals(answer.values(),
                    new long[]{3, 3, Long.MIN_VALUE})
                || !java.util.Arrays.equals(input, before)) {
            throw new AssertionError("M2 contract failed");
        }
    }
}
```

Run a smoke harness only after implementing the selected stub; the placeholder intentionally throws. Test-side clones do not count as algorithm workspace. Stack traces in this workbook are written **bottom to top, left to right**, even though an ArrayDeque printed after pushes appears top first.

## 3. Level 1 — Stack Fundamentals

### F1. Execute a command tape

**Difficulty:** Easy. **Objective:** implement LIFO operations and defined empty behavior. **Prerequisites:** loops, records, arrays, [stack fundamentals](./Stack_Essential_Concepts.md#2-stack-fundamentals-and-lifo).

**Problem and contract:** Start with an empty stack. Input is an `Op[]`; each operation has a case-sensitive name and an int value. `push` adds its value and produces no reply. `pop` removes/returns the top, `peek` returns it without removal, and `size` returns the current size; these three commands append one long reply each. Ignore the value field except for `push`. Empty `pop`/`peek` append NONE and leave state unchanged. Any other name throws `IllegalArgumentException`. Return replies in command order and final remaining values in **top-first** order. Use ArrayDeque for the working stack.

```java
public record Op(String name, int value) {}
public record TapeResult(long[] replies, int[] remaining) {}
public static TapeResult executeTape(Op[] operations) {
    throw new UnsupportedOperationException("TODO F1");
}
```

**Target:** O(q) amortized time and O(p) auxiliary space for q commands and peak depth p; O(r+s) output for r replies and final size s. A temporary reply buffer also counts as auxiliary if copied; pre-count replies to retain the O(p) target.

| Input commands, in order | Expected `(replies, remaining)` |
| --- | --- |
| `[]` | `([],[])` |
| `pop, peek, size` | `([NONE,NONE,0],[])` |
| `push 4, push -1, peek, pop, size` | `([-1,-1,1],[4])` |
| `push 0, push 0, pop, peek` | `([0,0],[0])` |
| `push MIN, push MAX, pop, peek` | `([MAX,MIN],[MIN])` |
| `push 1, push 2, push 3` | `([],[3,2,1])` |
| `Push 1` | `IllegalArgumentException` |

**Maximum test:** 100,000 pushes of values 0 through 99,999 produce no replies and remaining values 99,999 down to 0. Also try 100,000 empty pops: every reply is NONE, depth stays zero. **Complete when:** both tests and the table pass, every peek preserves size/order, and you can trace every operation of the third row. Explain why int −1 cannot serve as an empty marker here.

- [ ] F1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F2. Inspect without consuming

**Difficulty:** Easy. **Objective:** separate traversal order from destructive removal. **Prerequisites:** Deque iteration and array allocation.

**Problem and contract:** Input is a `Deque<Integer>` backed by an ArrayDeque, containing no nulls and already used as a stack with its top at the first end. Return a new int array in top-to-bottom order. Preserve the exact deque contents and order; do not pop and restore, clone the deque, or use a second collection. The caller does not mutate it during traversal.

```java
public static int[] inspectTopFirst(Deque<Integer> stack) {
    throw new UnsupportedOperationException("TODO F2");
}
```

**Target:** O(n) time, O(1) auxiliary space, O(n) returned output. The iterator's fixed state is auxiliary.

| Input stack shown bottom-to-top | Expected array |
| --- | --- |
| `[]` | `[]` |
| `[7]` | `[7]` |
| `[1,2,3]` | `[3,2,1]` |
| `[3,2,1]` | `[1,2,3]` |
| `[0,0,-1]` | `[-1,0,0]` |
| `[MIN,MAX]` | `[MAX,MIN]` |

**Maximum test:** push 100,000 equal values; every returned entry equals that value and the deque still contains 100,000 entries. **Complete when:** snapshot twice, compare both arrays, then pop the original deque and confirm the same order. Explain how using `descendingIterator()` would change the returned order. Revisit [collection endpoints](./Java_List_ArrayList_LinkedList.md#linkedlist-end-methods-ranked) if needed.

- [ ] F2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F3. Transfer a selected top segment

**Difficulty:** Easy. **Objective:** reason about two consecutive LIFO effects. **Prerequisites:** F1 and F2.

**Problem and contract:** Input `bottomToTop` describes a source stack; the destination starts empty. Perform exactly k repetitions of removing the source top and pushing that value onto the destination. Require `0 <= k <= n`. Return both final stacks as arrays in **bottom-to-top** order, leaving input unchanged. For this drill simulate with two ArrayDeques; afterward describe how direct array indexing could avoid working stacks. There is no return-to-source phase.

```java
public record TransferResult(int[] source, int[] destination) {}
public static TransferResult transferTop(int[] bottomToTop, int k) {
    throw new UnsupportedOperationException("TODO F3");
}
```

**Target for the simulation:** O(n) amortized time, O(n) auxiliary space, O(n) total returned output.

| Input `(bottomToTop, k)` | Expected `(source, destination)` |
| --- | --- |
| `([],0)` | `([],[])` |
| `([7],0)` | `([7],[])` |
| `([7],1)` | `([],[7])` |
| `([1,2,3,4],2)` | `([1,2],[4,3])` |
| `([3,2,1],3)` | `([],[1,2,3])` |
| `([0,-1,-1],2)` | `([0],[-1,-1])` |

**Maximum test:** n=100,000, `bottomToTop[i]=i`, k=n: source empty and destination `[99999,...,0]`. **Complete when:** show both stacks after each move in the fourth row, verify multiset preservation, and explain why the destination top is the last transferred value.

- [ ] F3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### F4. Undo a running total

**Difficulty:** Easy. **Objective:** restore state using the value actually removed. **Prerequisites:** F1, long arithmetic.

**Problem and contract:** Process an int command array. A nonzero command adds that signed value as a new active entry; zero undoes the most recent still-active entry. Undo on empty is a no-op. Return a long array containing the sum of active entries after every command. Zero is reserved as a command and cannot be an active entry. The total fits long under the shared bounds.

```java
public static long[] undoTotals(int[] commands) {
    throw new UnsupportedOperationException("TODO F4");
}
```

**Target:** O(n) amortized time, O(p) auxiliary space for peak active depth p, O(n) output. Avoid rescanning all active entries for each total.

| Input | Expected totals |
| --- | --- |
| `[]` | `[]` |
| `[0,0]` | `[0,0]` |
| `[5]` | `[5]` |
| `[5,-2,0,0,0]` | `[5,3,5,0,0]` |
| `[2,2,0,0]` | `[2,4,2,0]` |
| `[MAX,MAX,0]` | `[2147483647,4294967294,2147483647]` |
| `[MIN,0]` | `[-2147483648,0]` |

**Maximum test:** 100,000 commands equal to MAX give total `(i+1L)*MAX` at each position. **Complete when:** contrast O(n²) rescanning with your update cost, trace negative-value undo, and explain why subtracting the newly exposed top is wrong.

- [ ] F4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 4. Level 2 — Core Stack Patterns

### P1. Locate the first structural error

**Difficulty:** Medium. **Objective:** validate nesting and distinguish an irreparable prefix from incomplete input. **Prerequisites:** F1, [matching theory](./Stack_Essential_Concepts.md#5-matching-and-nested-structures).

**Problem and contract:** Given ASCII text, return the index of the **first closing delimiter** that has no pending opener or has the wrong opener type. If no such closer occurs but openers remain at the end, return `text.length()`. Return −1 only when all delimiters are correctly nested and closed. Ignore all non-delimiters. An invalid delimiter sequence is normal input and returns an error index; it does not throw. A leftover opener's own index is not the requested result.

```java
public static int firstStructureError(String text) {
    throw new UnsupportedOperationException("TODO P1");
}
```

**Target:** O(n) amortized time, O(d) auxiliary space for maximum pending depth d, scalar output.

| Input | Expected |
| --- | --- |
| `""` | `-1` |
| `"abc"` | `-1` |
| `"x{[a](b)}"` | `-1` |
| `"([)]"` | `2` |
| `"]"` | `0` |
| `"(()"` | `3` |
| `"[]}"` | `2` |
| `"(("` | `2` |

**Maximum tests:** 50,000 `(` followed by 50,000 `)` returns −1; 100,000 `(` returns 100,000. **Complete when:** draw the stack after each delimiter in the third and fourth rows, justify early rejection and the end check, and explain why three independent counters cannot establish nesting. Follow-up: when would a single counter be sufficient?

<details>
<summary>Hint 1</summary>

A closer must discharge the most recently opened unfinished obligation.

</details>
<details>
<summary>Hint 2</summary>

State what the stored openers mean after a valid prefix. At end-of-input, distinguish an empty state from pending obligations.

</details>
<details>
<summary>Hint 3</summary>

Check emptiness before reading the top; compare the expected opener type, not just the number of entries. Keep the original string index for an error result.

</details>

- [ ] P1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P2. Evaluate checked postfix arithmetic

**Difficulty:** Medium. **Objective:** derive operand order, enforce expression shape, and handle arithmetic deliberately. **Prerequisites:** F1, decimal parsing, [postfix theory](./Stack_Essential_Concepts.md#6-expression-evaluation-and-rpn).

**Problem and contract:** Input is a non-null array of non-null ASCII tokens, each at most 11 code units. A token is either one of `+`, `-`, `*`, `/` or a signed decimal int accepted by `Integer.parseInt` without trimming. Evaluate binary postfix notation using **long intermediates and result**, with division truncating toward zero. Reject malformed/out-of-int-range numeric tokens with `NumberFormatException`; reject empty input, operand underflow, or extra final operands with `IllegalArgumentException`. Throw `ArithmeticException` for division by zero or a result outside long range, including `Long.MIN_VALUE / -1`. Evaluate left-to-right and report the first encountered failure. Numeric errors are tested as rejection cases within this contract.

```java
public static long checkedPostfix(String[] tokens) {
    throw new UnsupportedOperationException("TODO P2");
}
```

**Target:** O(n) time with the explicit bounded token length, O(d) auxiliary space for peak available operands d, scalar output. If that length bound were removed, express parsing time using total token characters L.

| Tokens | Expected |
| --- | --- |
| `["12","5","-","3","*","2","/"]` | `10` |
| `["3","8","-"]` | `-5` |
| `["7","-3","/"]` | `-2` |
| `["+42"]` | `42` |
| `["-2147483648","-1","/"]` | `2147483648` |
| `[]` or `["1","2"]` or `["1","+"]` | `IllegalArgumentException` |
| `["2147483648"]` or `[" 2"]` | `NumberFormatException` |
| `["2","0","/"]` | `ArithmeticException` |
| `["2147483647","2147483647","*","3","*"]` | `ArithmeticException` |

**Boundary tests:** the valid sequence `0` followed by 49,999 repetitions of `1 +` has 99,999 tokens and returns 49,999. A valid binary postfix expression has an odd token count; a 100,000-token array of `"1"` must reject for extra operands. To test overflowing division, build Long.MIN_VALUE with `-2147483648 -2147483648 * -2 *`, then append `-1 /`; it must throw.

**Complete when:** trace every operand state for the first row, prove why exactly one final value is necessary, test freshly allocated operator strings rather than only interned literals, and explain why int literals do not guarantee safe long products. Follow-up: which two operations expose reversed operand order?

<details>
<summary>Hint 1</summary>

Each available value represents an entire completed subexpression.

</details>
<details>
<summary>Hint 2</summary>

The more recently completed expression is the right operand. A binary combination consumes two values and produces one.

</details>
<details>
<summary>Hint 3</summary>

Check shape before removal. Compare token contents with `equals`; use checked long arithmetic and handle the exceptional division case.

</details>

- [ ] P2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### P3. Preserve minimum and total through undo

**Difficulty:** Medium. **Objective:** maintain constant-time aggregate queries after pops, including equal minima. **Prerequisites:** F4, [augmented state](./Stack_Essential_Concepts.md#7-augmented-state-and-aggregate-queries).

**Problem and contract:** Implement the class below, initially empty. Push accepts any int; pop returns/removes the top; peek returns it unchanged; min returns the least active value; sum returns the long sum of active values. Empty sum is 0, size is 0, and isEmpty is true. Empty pop/peek/min throw `NoSuchElementException` and preserve state. At most 100,000 public operations occur in a test sequence, so long sum cannot overflow. No scanning for each aggregate query.

```java
public static final class MinSumStack {
    public void push(int value) { throw new UnsupportedOperationException("TODO P3"); }
    public int pop() { throw new UnsupportedOperationException("TODO P3"); }
    public int peek() { throw new UnsupportedOperationException("TODO P3"); }
    public int min() { throw new UnsupportedOperationException("TODO P3"); }
    public long sum() { throw new UnsupportedOperationException("TODO P3"); }
    public int size() { throw new UnsupportedOperationException("TODO P3"); }
    public boolean isEmpty() { throw new UnsupportedOperationException("TODO P3"); }
}
```

**Target:** amortized O(1) push, O(1) other operations, O(q) total amortized time for q operations, O(p) auxiliary storage for historical peak size p. Each returned query is scalar.

| Operations from a fresh instance | Required observations |
| --- | --- |
| none | size 0, sum 0, empty true; pop/peek/min each throw |
| `push 5` | peek 5, min 5, sum 5, size 1 |
| `push 5, push 2, push 2, pop` | pop returns 2; min 2, sum 7, size 2 |
| `push 5, push 2, push 2, pop, pop` | second pop returns 2; min 5, sum 5 |
| `push -2, push 0, push -3, pop` | pop returns −3; min −2, sum −2 |
| `push MIN, push MAX` | min MIN, sum −1; after pop: min MIN, sum −2147483648 |
| `push MAX, push MAX` | min MAX, sum 4294967294 |
| `push 7, pop` | pop 7; state is empty again |

**Maximum test:** 99,999 pushes of MAX followed by sum (100,000 operations) returns 214746217216353. Use separate sequences of increasing values, decreasing values, and repeated minima followed by enough pops to expose an older minimum. **Complete when:** explain the metadata invariant at every depth, compare with a list-scan model on random operations, and test an empty exception followed by a successful push. Follow-up: what does a single scalar minimum forget?

<details>
<summary>Hint 1</summary>

Popping should recover the previous aggregate, not rediscover it by scanning.

</details>
<details>
<summary>Hint 2</summary>

Think about what metadata belongs to each logical depth. A separate minimum structure also works if its multiplicities stay synchronized.

</details>
<details>
<summary>Hint 3</summary>

Use long for sum updates. If recording only new minima, decide explicitly what happens when a pushed value equals the current minimum.

</details>

- [ ] P3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 5. Monotonic Stack Practice

This section continues **Level 2**, with one record per exercise. Begin with the vocabulary in M1, then derive unresolved-state and candidate-state algorithms in M2/M3. M4 changes direction, M5 changes equality and output, and optional M6 combines boundaries. Use [monotonic fundamentals](./Stack_Essential_Concepts.md#8-monotonic-stack-fundamentals) and [strictness](./Stack_Essential_Concepts.md#9-increasing-decreasing-and-equal-values) for repair after an attempt.

For M2–M5, first write a nested-scan baseline. In a manual trace record position, value, stack before, popped entries and their meaning, answer writes, and stack after. Do not infer order from the stack's Java printout. For the final algorithm, separately bound pushes, successful pops, failed loop checks, peak depth, and backing-array growth. A nested while is not by itself a quadratic-time proof.

### M1. Classify bottom-to-top order

**Difficulty:** Easy. **Objective:** use strict and non-strict monotonic vocabulary accurately. **Prerequisites:** adjacent comparisons and F2.

**Problem and contract:** Input is an arbitrary int array representing a stack from bottom to top. Return four booleans in this order: strictly increasing, nondecreasing, strictly decreasing, nonincreasing. Each property quantifies over every adjacent pair. An empty or singleton sequence satisfies all four properties. This is an order diagnostic; do not rearrange values or construct a monotonic stack.

```java
public static boolean[] classifyOrder(int[] bottomToTop) {
    throw new UnsupportedOperationException("TODO M1");
}
```

**Target:** O(n) time, O(1) auxiliary space, four output booleans.

| Input | Expected |
| --- | --- |
| `[]` | `[true,true,true,true]` |
| `[8]` | `[true,true,true,true]` |
| `[1,3,7]` | `[true,true,false,false]` |
| `[1,3,3,7]` | `[false,true,false,false]` |
| `[7,3,1]` | `[false,false,true,true]` |
| `[7,3,3,1]` | `[false,false,false,true]` |
| `[0,0,0]` | `[false,true,false,true]` |
| `[-1,2,0]` | `[false,false,false,false]` |

**Maximum test:** 100,000 zeros gives `[false,true,false,true]`. Also test `[MIN,MAX]`; compare values directly rather than subtracting them. **Complete when:** explain every true flag in the empty and equal cases, and classify your later M2/M3 traces using the same direction convention.

- [ ] M1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### M2. Find first greater positions and values

**Difficulty:** Medium. **Objective:** track unresolved occurrences and distinguish a nearest answer from a suffix maximum. **Prerequisites:** M1 and the roadmap's next-greater drill.

**Problem and contract:** For every index i, find the smallest j>i with `values[j] > values[i]`. Return a `NeighborResult` with two arrays of length n: indices[i]=j and values[i]=the int at j widened to long. If no j exists, write −1 and NONE respectively. Repeated equal values are separate occurrences. Implement a left-to-right unresolved-entry algorithm; keep an index result even though a value result is also required. Declare `NeighborResult` once; M3 reuses it.

```java
public record NeighborResult(int[] indices, long[] values) {}
public static NeighborResult nextGreater(int[] values) {
    throw new UnsupportedOperationException("TODO M2");
}
```

**Target:** O(n) amortized time, O(n) worst-case auxiliary space, O(n) output.

| Input | Expected `(indices, values)` |
| --- | --- |
| `[]` | `([],[])` |
| `[7]` | `([-1],[NONE])` |
| `[4,2,2,5,3]` | `([3,3,3,-1,-1],[5,5,5,NONE,NONE])` |
| `[2,2,3]` | `([2,2,-1],[3,3,NONE])` |
| `[2,3,9]` | `([1,2,-1],[3,9,NONE])` |
| `[3,2,1]` | `([-1,-1,-1],[NONE,NONE,NONE])` |
| `[0,0,0]` | `([-1,-1,-1],[NONE,NONE,NONE])` |
| `[-2,-1,-3]` | `([1,-1,-1],[-1,NONE,NONE])` |
| `[MIN,MAX]` | `([1,-1],[MAX,NONE])` |

**Maximum tests:** for n=100,000 and `values[i]=i`, every i<n−1 has index i+1 and value i+1; the final entry is absent. For `values[i]=n-i`, every answer is absent and a forward unresolved stack can reach depth n.

**Complete when:** manually trace `[4,2,2,5,3]`, identify each unresolved entry before processing 5, prove the recorded j is first, and explain why a blocking top also blocks deeper entries. Compare with the nested-scan oracle on small arbitrary arrays. Follow-up: why must two equal positions retain distinct identities?

<details>
<summary>Hint 1</summary>

Ask which earlier questions the newly arrived value can finish.

</details>
<details>
<summary>Hint 2</summary>

Choose an order that places eligible unresolved entries at the top. An answer is written to the position whose question has just finished.

</details>
<details>
<summary>Hint 3</summary>

Indices preserve occurrence identity and let you retrieve values from the unchanged array. Initialize both absent-result representations deliberately.

</details>

- [ ] M2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### M3. Find first smaller positions and values

**Difficulty:** Medium. **Objective:** justify candidate pruning and its equality rule. **Prerequisites:** M1, M2, and the `NeighborResult` record declared there.

**Problem and contract:** For each i return the smallest j>i with `values[j] < values[i]`, along with its long value, using the same two-array format and −1/NONE absence markers as M2. For this drill implement a **right-to-left candidate algorithm**. Explain why removing a candidate cannot hide the nearest qualifying answer for a future position to its left. Keep duplicates and negative values within the contract.

```java
public static NeighborResult nextSmaller(int[] values) {
    throw new UnsupportedOperationException("TODO M3");
}
```

**Target:** O(n) amortized time, O(n) worst-case auxiliary space, O(n) output.

| Input | Expected `(indices, values)` |
| --- | --- |
| `[]` | `([],[])` |
| `[7]` | `([-1],[NONE])` |
| `[5,3,3,4,2]` | `([1,4,4,4,-1],[3,2,2,2,NONE])` |
| `[1,2,3]` | `([-1,-1,-1],[NONE,NONE,NONE])` |
| `[3,2,1]` | `([1,2,-1],[2,1,NONE])` |
| `[2,2,2]` | `([-1,-1,-1],[NONE,NONE,NONE])` |
| `[0,-1,-1]` | `([1,-1,-1],[-1,NONE,NONE])` |
| `[MAX,MIN]` | `([1,-1],[MIN,NONE])` |

**Maximum test:** n=100,000, `values[i]=n-i`: index i+1 and value n-i-1 for i<n−1, then absence. Increasing input gives all absence. **Complete when:** trace `[5,3,3,4,2]` from the right, explain equal-candidate removal, and contrast the meaning of a pop here with M2. Count each candidate's lifetime instead of multiplying n by a worst-case inner-loop length.

<details>
<summary>Hint 1</summary>

A farther candidate that is no better than a nearer one may never be needed again.

</details>
<details>
<summary>Hint 2</summary>

The current index asks a question after unusable candidates are removed. The remaining nearest useful candidate must be accessible at the top.

</details>
<details>
<summary>Hint 3</summary>

Decide whether an equal value can answer a strict-smaller query. Explain both why it fails now and why a nearer replacement is safe for later queries.

</details>

- [ ] M3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### M4. Recover both previous boundaries

**Difficulty:** Medium. **Objective:** transfer nearest-boundary reasoning to earlier positions. **Prerequisites:** M2 and M3.

**Problem and contract:** Return two int arrays of length n. `lower[i]` is the largest j<i with `values[j] < values[i]`; `higher[i]` is the largest j<i with `values[j] > values[i]`. Each absent answer is −1. Both comparisons are strict. You may use separate passes or two independent structures, but each output must refer to an original input index. Do not substitute the earliest or most extreme earlier value.

```java
public record PreviousBounds(int[] lower, int[] higher) {}
public static PreviousBounds previousBounds(int[] values) {
    throw new UnsupportedOperationException("TODO M4");
}
```

**Target:** O(n) amortized time, O(n) auxiliary space, O(n) output.

| Input | Expected `(lower, higher)` |
| --- | --- |
| `[]` | `([],[])` |
| `[7]` | `([-1],[-1])` |
| `[4,2,2,5,3]` | `([-1,-1,-1,2,2],[-1,0,0,-1,3])` |
| `[1,2,3]` | `([-1,0,1],[-1,-1,-1])` |
| `[3,2,1]` | `([-1,-1,-1],[-1,0,1])` |
| `[0,0,0]` | `([-1,-1,-1],[-1,-1,-1])` |
| `[-1,0,-2]` | `([-1,0,-1],[-1,-1,1])` |

**Maximum test:** 100,000 equal ints yields −1 in both arrays; for strictly increasing input, lower[i]=i−1 and every higher entry is −1. **Complete when:** explain each boundary of the third row, test both passes against scans, and give an example where keeping a farther equal candidate would obscure the required nearest answer. Follow-up: what changes if higher becomes greater-or-equal?

<details>
<summary>Hint 1</summary>

“Previous” changes where candidates are available; “nearest” still excludes farther substitutes.

</details>
<details>
<summary>Hint 2</summary>

Derive a candidate invariant for each strict relation separately. One ordering need not answer both queries.

</details>
<details>
<summary>Hint 3</summary>

Write an answer before making the current index available to later positions. Otherwise self-matching becomes possible.

</details>

- [ ] M4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### M5. Measure the wait for a non-lower value

**Difficulty:** Medium. **Objective:** change strictness without losing occurrence identity, then convert indices to distances. **Prerequisites:** M2 and M4.

**Problem and contract:** For each i, find the smallest j>i with `values[j] >= values[i]`. Return distance j−i, or **0** if none exists. The output has length n. Equal values qualify; zero is unambiguous because every real distance is positive. Input order is arbitrary and values may be negative.

```java
public static int[] waitsForNonLower(int[] values) {
    throw new UnsupportedOperationException("TODO M5");
}
```

**Target:** O(n) amortized time, O(n) auxiliary space, O(n) output.

| Input | Expected distances |
| --- | --- |
| `[]` | `[]` |
| `[7]` | `[0]` |
| `[2,2,3,1]` | `[1,1,0,0]` |
| `[3,1,2,3]` | `[3,1,1,0]` |
| `[1,2,3]` | `[1,1,0]` |
| `[3,2,1]` | `[0,0,0]` |
| `[0,0,0]` | `[1,1,0]` |
| `[-1,-2,-1]` | `[2,1,0]` |
| `[MIN,MAX]` | `[1,0]` |

**Maximum test:** 100,000 equal values yields 1 at every position except the final 0. **Complete when:** derive the equality condition from this statement, trace `[3,1,2,3]`, and explain why stored values alone cannot produce distances. As a verbal follow-up, predict the all-equal result if the relation became strict; do not create another exercise record.

<details>
<summary>Hint 1</summary>

An equal arrival ends the wait under this contract.

</details>
<details>
<summary>Hint 2</summary>

Use the first qualifying position to answer each unresolved occurrence, preserving enough information to measure a gap.

</details>
<details>
<summary>Hint 3</summary>

Subtract the earlier index from the answering index. Separate the unresolved default from a legitimate zero input value.

</details>

- [ ] M5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### M6. Return maximal support intervals — optional

**Difficulty:** Challenging, optional. **Objective:** combine two boundary facts and handle equal-height plateaus. **Prerequisites:** M3, M4, [optional boundary theory](./Stack_Essential_Concepts.md#11-optional-advanced-monotonic-boundaries).

**Problem and contract:** Input is a nonnegative int array of unit-width column heights. For every i, return the unique widest interval `[left,right)` containing i such that every height inside it is at least `heights[i]`. Return an n-by-2 int array in input-index order. Equal columns belong to the same support region when no lower column separates them. This asks for each interval, not a single maximum rectangle and not a count of distinct intervals.

```java
public static int[][] supportIntervals(int[] heights) {
    throw new UnsupportedOperationException("TODO M6");
}
```

**Target:** O(n) amortized time, O(n) auxiliary space, O(n) output. Any optional area calculation must multiply using long before overflow can occur.

| Input | Expected intervals |
| --- | --- |
| `[]` | `[]` |
| `[7]` | `[[0,1]]` |
| `[2,1,4,4,2]` | `[[0,1],[0,5],[2,4],[2,4],[2,5]]` |
| `[3,3]` | `[[0,2],[0,2]]` |
| `[0,0]` | `[[0,2],[0,2]]` |
| `[1,2,3]` | `[[0,3],[1,3],[2,3]]` |
| `[3,2,1]` | `[[0,1],[0,2],[0,3]]` |
| `[MAX,MAX]` | `[[0,2],[0,2]]` |

**Maximum test:** 100,000 equal heights gives `[0,100000)` for every index. **Complete when:** compare against direct outward expansion on small arrays; for each result prove containment, feasibility, and inability to extend one position farther. Trace the equal 4s in the third row. Follow-up: why are duplicate intervals acceptable here, whereas counting distinct contributions may need a different tie policy?

<details>
<summary>Hint 1</summary>

A support interval stops at a height that is too low, not at an equal height.

</details>
<details>
<summary>Hint 2</summary>

Determine the nearest blocking position independently on each side, then translate those exclusions into an interval.

</details>
<details>
<summary>Hint 3</summary>

Check the difference between an absent right boundary and an exclusive endpoint n. A blocking left index itself is excluded.

</details>

- [ ] M6 optional attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 6. Level 3 — NeetCode / LeetCode Practice

The following entries follow the [roadmap's exact order](./DSA_Interview_Roadmap.md#3-stack). **E/A/C describe local learning priority; Easy/Medium/Hard are platform difficulty.** Read the canonical statement for its current constraints, judge interface, and result requirements. The original drills above use educational interfaces that may differ from the judge. No full platform statement or solution is reproduced here.

For every N entry, a complete attempt includes a baseline, an invariant, Java implementation, edge tests, and a cost explanation. In its existing roadmap Log use `date; I/H/S; minutes; due; lesson`, retaining brief review history there. The reference below is the workbook's attempt record; do not copy those fields here as well.

### N1. Valid Parentheses

**Priority E — Essential. Difficulty Easy.** [Canonical problem](https://leetcode.com/problems/valid-parentheses/).

Practice nesting validity, early closers, mismatched types, and leftover openings after P1. Explain why checking only final counts is insufficient. Read the judge's alphabet and length constraints before carrying over the workbook's empty-text or ignored-character tests. Completion requires an independent or cold solve with the latest unresolved-opener invariant.

- [ ] N1 attempt — date/result/minutes/due/lesson Log: [existing roadmap Stack row](./DSA_Interview_Roadmap.md#3-stack), “Valid Parentheses”.

### N2. Min Stack

**Priority E — Essential. Difficulty Medium.** [Canonical problem](https://leetcode.com/problems/min-stack/).

Practice minimum queries that remain correct after removals. Adapt P3 reasoning to the exact required interface; confirm the judge's guarantees about nonempty operations. Explain repeated minima and restoration of old state. Completion includes a sequence exposing a previous minimum and a proof of per-operation costs.

- [ ] N2 attempt — date/result/minutes/due/lesson Log: [existing roadmap Stack row](./DSA_Interview_Roadmap.md#3-stack), “Min Stack”.

### N3. Evaluate Reverse Polish Notation

**Priority A — Additional. Difficulty Medium.** [Canonical problem](https://leetcode.com/problems/evaluate-reverse-polish-notation/).

Practice binary expression reduction and left/right operand order after P2. Check the platform's arithmetic and valid-input guarantees instead of assuming the checked-long teaching contract. Include subtraction, negative division, and more than one pending expression in your explanation. This platform problem is Additional; the ability to evaluate postfix independently remains a core implementation requirement.

- [ ] N3 attempt — date/result/minutes/due/lesson Log: [existing roadmap Stack row](./DSA_Interview_Roadmap.md#3-stack), “Evaluate Reverse Polish Notation”.

### N4. Daily Temperatures

**Priority E — Essential. Difficulty Medium.** [Canonical problem](https://leetcode.com/problems/daily-temperatures/).

Practice unresolved positions, the exact warmer relation, and distances. Explain ties, an unresolved suffix, and why each answer is the first qualifying day. Contrast a single expensive iteration with linear total work. Completion requires independent derivation from the statement, not changing a comparator in memorized code without a proof.

- [ ] N4 attempt — date/result/minutes/due/lesson Log: [existing roadmap Stack row](./DSA_Interview_Roadmap.md#3-stack), “Daily Temperatures”.

### N5. Largest Rectangle in Histogram

**Priority C — Optional advanced challenge. Difficulty Hard.** [Canonical problem](https://leetcode.com/problems/largest-rectangle-in-histogram/).

After M6, investigate height-limited intervals, widths, equal heights, and remaining state at end-of-input. Explain which boundary convention your chosen algorithm uses. Keep area arithmetic consistent with platform constraints. This is an optional investigation and **does not block P, R, or progression to ordinary internship preparation**.

- [ ] N5 optional attempt — date/result/minutes/due/lesson Log: [existing roadmap Stack row](./DSA_Interview_Roadmap.md#3-stack), “Largest Rectangle in Histogram”.

## 7. Level 4 — Mixed Interview Challenges

Use only the statements, contracts, and tests on a first attempt. Keep hints and pattern references closed. These tasks draw from arrays, hashing, two pointers, scans, and the current chapter; their position in this workbook does not determine the right algorithm. **Shared prerequisites:** earlier roadmap foundations and Java arrays/strings/collections. No later roadmap topic is required.

For every X task, completion additionally requires a written selection argument: compare at least two plausible techniques, identify the information each stores, and explain why the chosen one meets the contract and bounds. A recognized title or a memorized implementation is insufficient evidence of R.

### X1. Badge cleanup

**Difficulty:** Medium. **Objective:** derive a process that accounts for newly adjacent data. **Prerequisites:** shared Level 4 prerequisites.

**Problem and contract:** A badge consists of ASCII text. Repeatedly remove the **leftmost adjacent pair of identical code units** until no such pair remains. Return the final string in surviving order. Comparison is case-sensitive and punctuation counts. A removal can create a new removable pair across the gap. The specified leftmost process defines the output; no freedom to reorder or choose unrelated occurrences exists.

```java
public static String cleanBadge(String text) {
    throw new UnsupportedOperationException("TODO X1");
}
```

**Target:** O(n) amortized time, O(n) auxiliary space, O(k) returned string for k survivors. Explain the relationship between your computation and the stated repeated-removal process.

| Input | Expected |
| --- | --- |
| `""` | `""` |
| `"a"` | `"a"` |
| `"abbaca"` | `"ca"` |
| `"azxxzy"` | `"ay"` |
| `"aaaa"` | `""` |
| `"aba"` | `"aba"` |
| `"aA"` | `"aA"` |
| `"a!!a"` | `""` |

**Maximum tests:** 100,000 `a` characters returns empty; 50,000 repetitions of `ab` remain unchanged. **Complete when:** trace the changing adjacency in `azxxzy`, explain why a frequency table loses required information, and compare short cases against literal leftmost deletion. Follow-up: why is repeatedly editing the middle of a string a poor baseline at large n?

<details>
<summary>Hint 1</summary>

After processing a prefix, only its surviving right edge can interact with the next code unit.

</details>
<details>
<summary>Hint 2</summary>

Maintain the fully reduced prefix. Decide whether a new code unit extends it or cancels its last survivor.

</details>
<details>
<summary>Hint 3</summary>

Use an explicit logical length or an endpoint structure; avoid making a new string after every deletion. Account for temporary storage when producing the final string.

</details>

- [ ] X1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X2. Repeated ticket

**Difficulty:** Medium. **Objective:** retain the right evidence for the earliest violation. **Prerequisites:** shared Level 4 prerequisites.

**Problem and contract:** Each int in an unsorted array is a ticket ID. Return `[i,j]` for the **smallest j** such that the same ID appeared earlier, and the **smallest i<j** with that ID. If no ID repeats, return `[-1,-1]`. IDs have no numerical ordering significance. Preserve the original array and return original indices.

```java
public static int[] repeatedTicket(int[] ids) {
    throw new UnsupportedOperationException("TODO X2");
}
```

**Target:** expected O(n) time, O(u) auxiliary space for u distinct IDs seen, two output ints. State the hashing assumption if your solution uses it.

| Input | Expected |
| --- | --- |
| `[]` | `[-1,-1]` |
| `[7]` | `[-1,-1]` |
| `[7,2,7,2]` | `[0,2]` |
| `[3,1,2,1,3]` | `[1,3]` |
| `[0,0,0]` | `[0,1]` |
| `[-2,0,-1]` | `[-1,-1]` |
| `[MAX,MIN,MAX]` | `[0,2]` |

**Maximum test:** values 0 through 99,998 followed by 0 yields `[0,99999]`. A length-100,000 strictly decreasing sequence of distinct IDs has no answer. **Complete when:** justify both tie rules and explain why sorting or only inspecting the most recent active value can break the requirement. Follow-up: could a set alone recover both returned indices?

<details>
<summary>Hint 1</summary>

The stopping condition concerns any earlier equal ID, not just an adjacent one.

</details>
<details>
<summary>Hint 2</summary>

Process candidate second indices in order and retain the first position for each previously seen ID.

</details>
<details>
<summary>Hint 3</summary>

Look up before recording the current occurrence. Do not overwrite an earlier first index.

</details>

- [ ] X2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X3. Two-parcel allowance

**Difficulty:** Medium. **Objective:** use an input ordering guarantee and validate a nonunique witness. **Prerequisites:** shared Level 4 prerequisites.

**Problem and contract:** Input weights are nonnegative ints in nondecreasing order. Choose two **distinct** indices i<j with maximum possible sum at most `limit`, where `0 <= limit <= 4294967294L`. Return any maximizing pair `[i,j]`; return an empty int array if none is feasible. Duplicates are distinct parcels. Input is read-only. Compare sums in long arithmetic.

```java
public static int[] parcelPair(int[] weights, long limit) {
    throw new UnsupportedOperationException("TODO X3");
}
```

**Target:** O(n) time, O(1) auxiliary space, at most two output ints. Tied witnesses need not have a particular order beyond i<j.

| Input `(weights, limit)` | Required result property |
| --- | --- |
| `([],5)` or `([2],4)` | `[]` |
| `([1,2,2,4],5)` | `[0,3]`, sum 5 |
| `([1,1,3,3],4)` | i in `{0,1}`, j in `{2,3}`, sum 4 |
| `([2,4,8],5)` | `[]` |
| `([2,4,8],11)` | `[0,2]`, sum 10 |
| `([0,0,1],0)` | `[0,1]`, sum 0 |
| `([MAX,MAX],4294967294)` | `[0,1]`, sum 4294967294 |

**Maximum test:** 100,000 ones and limit 2: any distinct valid pair is optimal. **Complete when:** validate indices, feasibility, and optimality against all pairs on small inputs; test claimed absence too. Explain why each rejected group of candidates cannot improve the answer and why no input sorting or copied array is necessary. Follow-up: what evidence is lost if the nondecreasing precondition is removed?

<details>
<summary>Hint 1</summary>

Sorted weights let one comparison rule out more than one candidate pair.

</details>
<details>
<summary>Hint 2</summary>

Consider the remaining range's smallest and largest weights. A feasible pair may establish a best-so-far result while allowing one endpoint to advance.

</details>
<details>
<summary>Hint 3</summary>

Keep the two indices distinct, widen before addition, and preserve the best witness when a later comparison is worse.

</details>

- [ ] X3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X4. Remaining record holders

**Difficulty:** Medium. **Objective:** identify the smallest sufficient summary of a suffix. **Prerequisites:** shared Level 4 prerequisites.

**Problem and contract:** Return a boolean array whose entry i is true exactly when `values[i]` is strictly greater than **every** value at a later index. The last position is true because there are no later values. An equal value to the right disqualifies an earlier position. This asks about the entire suffix, not its first qualifying element.

```java
public static boolean[] remainingRecords(int[] values) {
    throw new UnsupportedOperationException("TODO X4");
}
```

**Target:** O(n) time, O(1) auxiliary space, O(n) output.

| Input | Expected |
| --- | --- |
| `[]` | `[]` |
| `[MIN]` | `[true]` |
| `[5,3,4,4,2]` | `[true,false,false,true,true]` |
| `[1,2,3]` | `[false,false,true]` |
| `[3,2,1]` | `[true,true,true]` |
| `[0,0,0]` | `[false,false,true]` |
| `[-1,-2,-3]` | `[true,true,true]` |
| `[MAX,MAX]` | `[false,true]` |

**Maximum test:** 100,000 equal values gives only the final true; strictly decreasing input gives all true. **Complete when:** justify the sufficient state, avoid inventing an int smaller than MIN, and compare with direct suffix checks. Follow-up: what extra information would a nearest-position query require?

<details>
<summary>Hint 1</summary>

Only the greatest later value can prevent the current position from satisfying this condition.

</details>
<details>
<summary>Hint 2</summary>

Choose a traversal direction in which the needed suffix summary is already available.

</details>
<details>
<summary>Hint 3</summary>

Treat the empty suffix explicitly, then update the summary after evaluating the current position. Equality must fail the strict comparison.

</details>

- [ ] X4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### X5. Round-trip inspection

**Difficulty:** Medium. **Objective:** preserve nearest-answer semantics when traversal wraps. **Prerequisites:** shared Level 4 prerequisites.

**Problem and contract:** Values lie on a circular route in their array order. From each position i inspect positions `(i+1)%n`, `(i+2)%n`, and so on, through distance n−1 only. Return the smallest positive distance to a **strictly lower** value; return 0 if none exists. Do not count the starting position after a full lap. For n=0 return an empty array without computing modulo zero. Negative and equal values are allowed.

```java
public static int[] inspectionDistances(int[] values) {
    throw new UnsupportedOperationException("TODO X5");
}
```

**Target:** O(n) amortized time, O(n) auxiliary space, O(n) output; the route need not be physically copied.

| Input | Expected |
| --- | --- |
| `[]` | `[]` |
| `[7]` | `[0]` |
| `[4,2,3]` | `[1,0,2]` |
| `[1,2,3]` | `[0,2,1]` |
| `[3,2,1]` | `[1,1,0]` |
| `[2,2,1]` | `[2,1,0]` |
| `[0,0,0]` | `[0,0,0]` |
| `[-1,-2,0]` | `[1,0,1]` |
| `[MIN,MAX]` | `[0,1]` |

**Maximum test:** n=100,000, `values[i]=i`: output[0]=0 and output[i]=n−i for every i>0. **Complete when:** trace a wraparound answer and an unresolved minimum, compare with a circular nested scan, and prove that extra traversal does not overwrite a nearer answer or permit self-matching. Explain the number of times each original occurrence is inserted/removed. Follow-up: what bounds the total scan even when every answer is absent?

<details>
<summary>Hint 1</summary>

Every eligible answer appears within one additional lap; endless traversal adds no new evidence.

</details>
<details>
<summary>Hint 2</summary>

Consider resolving pending original positions while visiting a bounded virtual continuation of the array. Decide whether positions need to become pending more than once.

</details>
<details>
<summary>Hint 3</summary>

Separate virtual traversal position from original index when computing distance. Strict inequality rules out equal self-matches, but you still need a proof of the distance bound.

</details>

- [ ] X5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 8. Complexity Analysis Exercises

Analyze the exact snippets below before opening the delayed checks in Section 13. Put them inside a scratch class with `import java.util.*;`. They return scalars so output space is O(1); working collections still count. Inputs are read-only. These are cost-analysis tasks, not requests to reproduce an algorithm from a template.

For **every C exercise**, submit: (1) time, distinguishing worst-case and amortized costs; (2) peak auxiliary space; (3) a derivation; (4) necessary assumptions, including library operations and arithmetic; and (5) an improvement or an explanation of why the current bound is appropriate. Also state maximum active stack size, whether capacity may remain after pops, and any relevant total allocation volume. The **target time/space analysis and improvement checks are delayed** in Section 13 so the prompt does not give away the answer. Complete only after predicting the small output examples and defending your cost bounds.

### C1. Build, then drain

**Difficulty:** Easy. **Objective:** distinguish one endpoint operation from a whole sequence. **Prerequisites:** F1 and amortized complexity. **Input/output:** integer `0 <= n <= 100000`; return the sum of all removed values. Empty input size returns 0; the sum fits long.

```java
static long c1(int n) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (int i = 0; i < n; i++) stack.push(i);
    long total = 0;
    while (!stack.isEmpty()) total += stack.pop();
    return total;
}
```

**Output checks:** n=0 → 0; n=1 → 0; n=4 → 6; n=100000 → 4999950000. Count pushes and pops separately. Is every push worst-case constant time? Does an empty final stack imply constant peak space? Discuss an improved method if only this scalar sum is required.

- [ ] C1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C2. Drain at checkpoints

**Difficulty:** Easy. **Objective:** analyze repeated inner loops using total progress. **Prerequisites:** F4 and C1. **Input/output:** arbitrary int array; nonzero entries become pending values, zero drains all pending values into a long total. Return the sum of values drained, excluding any final undrained entries. This arithmetic fits long.

```java
static long c2(int[] values) {
    Deque<Integer> stack = new ArrayDeque<>();
    long total = 0;
    for (int value : values) {
        if (value != 0) {
            stack.push(value);
        } else {
            while (!stack.isEmpty()) total += stack.pop();
        }
    }
    return total;
}
```

**Output checks:** `[]` → 0; `[2,3,0,4,0]` → 9; `[2,0,4]` → 2; `[0,0]` → 0; `[-2,3,0]` → 1. Compare one long batch, many singleton batches, and no checkpoint. Define p as the longest pending batch. Would clearing a collection release its backing capacity immediately? Could a pending subtotal preserve this output contract?

- [ ] C2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C3. Count monotonic removals

**Difficulty:** Medium. **Objective:** prove total work despite an expensive individual iteration. **Prerequisites:** M2 and C2. **Input/output:** arbitrary int array; return the number of indices removed by the shown scan. Do not change its strict comparison.

```java
static int c3(int[] values) {
    Deque<Integer> pending = new ArrayDeque<>();
    int removals = 0;
    for (int i = 0; i < values.length; i++) {
        while (!pending.isEmpty() && values[pending.peek()] < values[i]) {
            pending.pop();
            removals++;
        }
        pending.push(i);
    }
    return removals;
}
```

**Output checks:** `[]` → 0; `[3,2,1,4]` → 3; `[2,2,2]` → 0; `[1,2,3]` → 2; `[3,2,1]` → 0. For n=100000, a decreasing prefix followed by one larger value produces a large single drain. Count successful and failed while checks; distinguish the abstract operation count from ArrayDeque growth. Give a worst-space family and discuss whether a scalar answer alone permits a simpler characterization.

<details>
<summary>Hint 1</summary>

An index that leaves this structure is never inserted again.

</details>
<details>
<summary>Hint 2</summary>

Charge each successful inner iteration to the removed index, and each final failed check to an outer iteration.

</details>
<details>
<summary>Hint 3</summary>

Try all-equal and decreasing inputs for maximum pending depth. Do not use only the final iteration's depth to estimate peak storage.

</details>

- [ ] C3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C4. Recursive frames and explicit entries

**Difficulty:** Medium. **Objective:** distinguish total calls, live depth, and necessary storage. **Prerequisites:** C1 and [recursive-stack theory](./Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack). **Input/output:** an int array; call the recursive method with i=0, or a valid suffix index `0 <= i <= n`. Both methods sum their selected values into long. For a full-array comparison use i=0.

```java
static long c4Recursive(int[] values, int i) {
    if (i == values.length) return 0;
    return (long) values[i] + c4Recursive(values, i + 1);
}

static long c4Explicit(int[] values) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (int value : values) stack.push(value);
    long total = 0;
    while (!stack.isEmpty()) total += stack.pop();
    return total;
}
```

**Output checks:** full `[]` → 0; full `[2,-1,4]` → 5; recursive `[2,-1,4]` with i=1 → 3; i=3 → 0. Analyze general n assuming sufficient call-stack capacity, but run only small recursive examples; a JVM may throw `StackOverflowError` on a long valid chain, and no universal safe depth is promised. Is the addition pending across the recursive call? Does replacing recursion with a collection alone improve asymptotic space? Propose an alternative that needs neither form of pending state.

<details>
<summary>Hint 1</summary>

Count frames that coexist at the deepest call, not just variables declared in one invocation.

</details>
<details>
<summary>Hint 2</summary>

The explicit method also retains all values before summing; moving storage to the heap does not erase it.

</details>
<details>
<summary>Hint 3</summary>

For a sum, ask whether any future input is needed before an already-read value can contribute to the result.

</details>

- [ ] C4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C5. A search hidden inside each push

**Difficulty:** Medium. **Objective:** include collection lookup costs. **Prerequisites:** F1 and earlier hashing concepts. **Input/output:** arbitrary int array; return its number of distinct values.

```java
static int c5(int[] values) {
    Deque<Integer> seen = new ArrayDeque<>();
    for (int value : values) {
        if (!seen.contains(value)) seen.push(value);
    }
    return seen.size();
}
```

**Output checks:** `[]` → 0; `[1,2,1,3]` → 3; `[0,0,0]` → 1; `[MIN,MAX]` → 2. Let u be the number of distinct values. Give both an all-distinct worst-case family and an all-equal family. Explain why a bound on pushes alone is insufficient. What alternative provides the natural expected-time improvement, and what assumption does it introduce?

<details>
<summary>Hint 1</summary>

Finding an arbitrary value differs from looking at the first endpoint.

</details>
<details>
<summary>Hint 2</summary>

On each unseen value, the search may inspect every value already retained.

</details>
<details>
<summary>Hint 3</summary>

Express the work as a sum of search lengths. Check the documented cost of ArrayDeque.contains before choosing a bound.

</details>

- [ ] C5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### C6. Temporary snapshots

**Difficulty:** Medium. **Objective:** separate peak live storage from cumulative allocation and copying. **Prerequisites:** C1 and array/list copying costs. **Input/output:** arbitrary int array; return the total number of elements across the temporary snapshots made below. Snapshots are not retained or returned.

```java
static long c6(int[] values) {
    Deque<Integer> stack = new ArrayDeque<>();
    long copied = 0;
    for (int value : values) {
        stack.push(value);
        List<Integer> snapshot = new ArrayList<>(stack);
        copied += snapshot.size();
    }
    return copied;
}
```

**Output checks:** `[]` → 0; `[7]` → 1; `[2,2,2]` → 6; any length-100000 input → 5000050000. Analyze source-level work without assuming the JVM removes allocations. Use logical reachable peak memory rather than unreclaimed garbage accumulated under an unspecified collector. Would retaining every snapshot change the space bound? Can the scalar contract be computed without inspecting element values?

<details>
<summary>Hint 1</summary>

Each snapshot copies a prefix-sized collection; the snapshots are not all the same size.

</details>
<details>
<summary>Hint 2</summary>

Sum copy sizes for time and allocation volume, but ask which snapshots remain reachable simultaneously for peak live space.

</details>
<details>
<summary>Hint 3</summary>

The result depends only on input length. If deriving a formula, widen before multiplying.

</details>

- [ ] C6 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 9. Debugging Challenges

Every snippet deliberately contains a reproducible defect and compiles in Java 21 inside a class with `import java.util.*;`. The table for each gives **required correct behavior**, not the buggy output. Run failing cases to discover the actual behavior. There is no completed repair here.

For **every D exercise**, submit: a minimal failing input, observed versus required behavior, a precise defect explanation, repaired Java code, the corrected invariant, termination reasoning, and time/auxiliary-space analysis. Preserve the stated contract. Include passing controls and regression tests for all identified defects. A change that fixes only one witness is not sufficient. The target bounds below apply to the repaired algorithm. Shared inputs are non-null and read-only; empty or malformed cases are included only when the individual contract defines them.

### D1. A validator that trusts every closer

**Difficulty:** Medium. **Objective:** detect both unsafe removal and lost type information. **Prerequisites:** P1. **Input/output:** a string containing only `()[]`; return whether it is correctly nested and complete. Invalid nesting must return false, never throw. Empty string is valid.

```java
static boolean d1(String text) {
    Deque<Character> stack = new ArrayDeque<>();
    for (char c : text.toCharArray()) {
        if (c == '(' || c == '[') stack.push(c);
        else stack.pop();
    }
    return stack.isEmpty();
}
```

| Input | Required result |
| --- | --- |
| `"]"` | `false` |
| `"([)]"` | `false` |
| `"([])"` | `true` |
| `"(("` | `false` |
| `""` | `true` |

**Repair target:** O(n) amortized time, O(d) auxiliary space for maximum depth d. Notice the full character copy when assessing whether a repair meets that space target. **Complete when:** each independent defect has a regression test, and a 100,000-character flat sequence `()()...()` is processed without a full copied input buffer. Follow-up: does checking only stack size establish the grammar?

<details>
<summary>Hint 1</summary>

A closing symbol carries a type requirement as well as a request to finish something.

</details>
<details>
<summary>Hint 2</summary>

Inspect what must be true before removal and what must be compared afterward. The invalid-prefix case needs a defined return value.

</details>
<details>
<summary>Hint 3</summary>

Test an early closer separately from equal counts in the wrong order. Use indexed string traversal if targeting depth-based auxiliary space.

</details>

- [ ] D1 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D2. Plausible arithmetic, wrong expression

**Difficulty:** Medium. **Objective:** expose bugs hidden by commutative operations. **Prerequisites:** P2. **Input/output:** a valid binary postfix expression over int literals and `+`, `-`, `/`; return its long value. Tokens have length at most 11, every correct intermediate fits long, and every correct divisor is nonzero. Malformed expressions are outside this drill's precondition.

```java
static long d2(String[] tokens) {
    Deque<Long> values = new ArrayDeque<>();
    for (String token : tokens) {
        if (token.equals("+") || token.equals("-") || token.equals("/")) {
            long left = values.pop();
            long right = values.pop();
            values.push(switch (token) {
                case "+" -> left + right;
                case "-" -> left - right;
                default -> left / right;
            });
        } else values.push((long) Integer.parseInt(token));
    }
    return values.pop();
}
```

| Tokens | Required result |
| --- | --- |
| `["8","3","-"]` | `5` |
| `["8","2","/"]` | `4` |
| `["0","2","/"]` | `0` |
| `["2","3","+"]` | `5` |
| `["7","-3","/"]` | `-2` |

**Repair target:** O(n) time, O(d) auxiliary space for peak operands. **Complete when:** label the two subexpressions before each pop, test negative division, and explain why a valid expression can trigger an exception in the buggy code. Keep validation for invalid syntax separate from the stated repair.

<details>
<summary>Hint 1</summary>

Variable names do not establish an operand's position in the expression.

</details>
<details>
<summary>Hint 2</summary>

The last completed subexpression is the right operand of the next binary operation.

</details>
<details>
<summary>Hint 3</summary>

Trace subtraction before addition. Use separate variables so a pop order is not concealed inside an arithmetic expression.

</details>

- [ ] D2 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D3. Equality answers too soon

**Difficulty:** Medium. **Objective:** derive a comparison from strict answer semantics. **Prerequisites:** M2. **Input/output:** arbitrary int array; return first later **strictly greater indices**, or −1 for absence.

```java
static int[] d3(int[] values) {
    int[] answer = new int[values.length];
    Arrays.fill(answer, -1);
    Deque<Integer> pending = new ArrayDeque<>();
    for (int i = 0; i < values.length; i++) {
        while (!pending.isEmpty() && values[pending.peek()] <= values[i]) {
            answer[pending.pop()] = i;
        }
        pending.push(i);
    }
    return answer;
}
```

| Input | Required result |
| --- | --- |
| `[2,2,3]` | `[2,2,-1]` |
| `[2,2]` | `[-1,-1]` |
| `[1,2]` | `[1,-1]` |
| `[]` | `[]` |

**Repair target:** O(n) amortized time, O(n) auxiliary space, O(n) output. **Complete when:** explain the unresolved invariant and why the equal occurrence cannot finish an earlier query. Test 100,000 equal values and both monotone directions. Follow-up: would the same comparison change be correct in a right-to-left candidate algorithm?

<details>
<summary>Hint 1</summary>

Read the relation in the contract literally.

</details>
<details>
<summary>Hint 2</summary>

Here a pop commits an answer; it is not merely pruning a candidate.

</details>
<details>
<summary>Hint 3</summary>

Inspect the result written when the second 2 arrives, before the 3 has been seen.

</details>

- [ ] D3 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D4. Losing the identity of a removed entry

**Difficulty:** Medium. **Objective:** repair state representation and a use of the wrong post-pop state. **Prerequisites:** M2 and M5. **Input/output:** arbitrary int array; return the distance to the first later strictly greater value, or 0 when absent. Values are not restricted to valid array indices.

```java
static int[] d4(int[] values) {
    int[] answer = new int[values.length];
    Deque<Integer> pending = new ArrayDeque<>();
    for (int i = 0; i < values.length; i++) {
        while (!pending.isEmpty() && values[pending.peek()] < values[i]) {
            int previous = pending.pop();
            answer[previous] = i - pending.peek();
        }
        pending.push(values[i]);
    }
    return answer;
}
```

| Input | Required result |
| --- | --- |
| `[9,8]` | `[0,0]` |
| `[0,1]` | `[1,0]` |
| `[4,2,5]` | `[2,1,0]` |
| `[-1,0]` | `[1,0]` |
| `[]` | `[]` |

**Repair target:** O(n) amortized time, O(n) auxiliary space, O(n) output. **Complete when:** show why repairing only the insertion representation still leaves a failure, and explain which identity owns each answer after removal. Follow-up: what if no entry remains after the pop?

<details>
<summary>Hint 1</summary>

The stored quantity must support every later use made of it.

</details>
<details>
<summary>Hint 2</summary>

Track the removed item separately from the newly exposed item; they represent different positions.

</details>
<details>
<summary>Hint 3</summary>

Use a large value in a short input to test representation, then a two-item increasing input to test the post-pop access.

</details>

- [ ] D4 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D5. Distances and unresolved output

**Difficulty:** Medium. **Objective:** separate distance arithmetic from missing-answer initialization. **Prerequisites:** M2 and M5. **Input/output:** arbitrary int array; return first strictly greater distance j−i, but use **−1** when absent in this exercise. No real distance is zero.

```java
static int[] d5(int[] values) {
    int[] answer = new int[values.length];
    Deque<Integer> pending = new ArrayDeque<>();
    for (int i = 0; i < values.length; i++) {
        while (!pending.isEmpty() && values[pending.peek()] < values[i]) {
            int earlier = pending.pop();
            answer[earlier] = i - earlier + 1;
        }
        pending.push(i);
    }
    return answer;
}
```

| Input | Required result |
| --- | --- |
| `[1,2]` | `[1,-1]` |
| `[2,1]` | `[-1,-1]` |
| `[3,1,2,4]` | `[3,1,1,-1]` |
| `[5]` | `[-1]` |
| `[]` | `[]` |

**Repair target:** O(n) amortized time, O(n) auxiliary space, O(n) output. **Complete when:** fix and independently test both failures. Explain the distinction between distance and the number of endpoints in an inclusive segment. Test a long unresolved suffix, not only increasing inputs.

<details>
<summary>Hint 1</summary>

An adjacent answer and no answer must produce different values under this contract.

</details>
<details>
<summary>Hint 2</summary>

Analyze what happens to an output cell that is never assigned inside the loop.

</details>
<details>
<summary>Hint 3</summary>

Check j−i for adjacent positions numerically, then compare the default array value with the specified absence marker.

</details>

- [ ] D5 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D6. A minimum disappears too early

**Difficulty:** Medium. **Objective:** restore synchronization when minimum values repeat. **Prerequisites:** P3. **Input/output:** initially empty mutable object; push stores an int, pop removes/returns the top, min returns the least active value. Empty pop/min throw `NoSuchElementException`. Each successful operation has scalar output or none.

```java
static final class BrokenMinimum {
    private final Deque<Integer> values = new ArrayDeque<>();
    private final Deque<Integer> minima = new ArrayDeque<>();
    void push(int value) {
        values.push(value);
        if (minima.isEmpty() || value < minima.peek()) minima.push(value);
    }
    int pop() {
        int value = values.pop();
        if (value == minima.peek()) minima.pop();
        return value;
    }
    int min() { return minima.element(); }
}
```

| Operations from fresh state | Required observation |
| --- | --- |
| `push 5, push 2, push 2, pop, min` | pop 2, min 2 |
| `push 2, push 2, pop, min` | pop 2, min 2 |
| `push 5, push 2, pop, min` | pop 2, min 5 |
| empty `pop` or `min` | `NoSuchElementException` |

**Repair target:** amortized O(1) push, O(1) pop/min, O(p) auxiliary storage for peak size p. **Complete when:** test repeated minima, an older minimum after several pops, and reuse after becoming empty. State the exact relationship between the two structures. Follow-up: how would a multiplicity representation differ from per-depth metadata?

<details>
<summary>Hint 1</summary>

Two equal minimum values can have different lifetimes.

</details>
<details>
<summary>Hint 2</summary>

Removing one witness must not remove evidence for another still-active equal witness.

</details>
<details>
<summary>Hint 3</summary>

Examine both the condition for recording a minimum and the condition for removing its record. They must describe compatible multiplicities.

</details>

- [ ] D6 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

### D7. A loop without progress

**Difficulty:** Medium. **Objective:** connect a termination proof to actual mutations. **Prerequisites:** M2 and C3. **Input/output:** arbitrary int array; for each value, remove all smaller top values from the pending stack, then push the value. Return the total number of removals. Equality does not remove an entry. This is the same scalar observation as C3, implemented with values instead of indices.

```java
static int d7(int[] values) {
    Deque<Integer> pending = new ArrayDeque<>();
    int removals = 0;
    for (int value : values) {
        while (!pending.isEmpty() && pending.peek() < value) {
            removals++;
        }
        pending.push(value);
    }
    return removals;
}
```

| Input | Required result |
| --- | --- |
| `[1,2]` | `1` |
| `[3,2,1,4]` | `3` |
| `[2,2]` | `0` |
| `[]` | `0` |

**Repair target:** O(n) amortized time, O(n) auxiliary space, scalar output. Run the buggy increasing case only in an isolated process with a short timeout, or trace it by hand; its while loop can run forever. **Complete when:** identify a decreasing measure for the inner loop, show where the code fails to change that measure, and validate the repair against C3 on small arrays. Follow-up: why does eventual advancement of the for-loop variable not prove termination of its body?

<details>
<summary>Hint 1</summary>

A counter changing is not enough if the loop condition never reads that counter.

</details>
<details>
<summary>Hint 2</summary>

Compare the pending state before and after one successful while iteration.

</details>
<details>
<summary>Hint 3</summary>

The counted event must actually happen to the data structure. Re-evaluate the condition after that event, rather than relying on a future outer iteration.

</details>

- [ ] D7 attempt — date: —; result I/H/S: —; minutes: —; due: —; lesson: —

## 10. Mock Interview Questions

Use a fresh task or a partner-selected variant, with topic tags, hints, old code, and theory closed. These sessions reuse the existing exercise records; do not create another revision ledger. Suggested windows are flexible practice aids, following the [roadmap strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy): roughly 15–25 minutes for an Easy and 30–45 minutes for a Medium, with explanation and testing included. Learning sessions may take longer.

| Session | Task selection | Evidence to collect |
| --- | --- | --- |
| Foundation rehearsal | One F drill plus C1 or C4 | Predict endpoints/emptiness; distinguish explicit entries from recursive frames |
| Core reasoning | One of P1–P3 selected without advance notice | Explain state, baseline, invariant, invalid behavior, and aggregate cost |
| Monotonic reasoning | One of M2–M5 plus a tie case | Derive comparisons, prove first/nearest, bound total pushes/pops |
| Mixed selection | An unseen X task with its hints hidden | Compare plausible algorithms before coding and respect read-only input |
| Repair discussion | One D task chosen by another person | Construct a minimal witness, repair all defects, and give a regression test |

Use short follow-ups during the appropriate session:

1. What does one stored entry represent? What information would be lost by storing only its value?
2. What fact makes the newest pending entry the next one to inspect?
3. Why is this pop safe? Is it resolving an answer or eliminating a candidate?
4. Why can no nearer valid answer have been skipped?
5. What changes on equal values, and why does scan direction matter?
6. What decreases during the inner loop? What advances during the outer loop?
7. How many times can one occurrence enter and leave? How much can one iteration cost?
8. What is the maximum live depth? Does the backing array shrink whenever an entry leaves?
9. Which edge case defeats your baseline or a tempting wrong solution?
10. Which simpler summary or earlier technique would meet this contract with less memory?
11. Where could int arithmetic overflow before assignment to long?
12. What would change if the input could not be retained, or if the requested answer were an index instead of a value?

Answer in your own words; no model speech is supplied. An interviewer may change strictness, the absence marker, scan direction, or whether a witness versus a value is required. Re-derive the affected invariant and tests rather than patching code from memory. If all X tasks are already familiar, have a partner supply a fresh variant for readiness assessment; familiar cold solves remain useful for retention.

## 11. Revision and Spaced Repetition

Follow the [existing review policy](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): consider reviews about **1, 3, 7, and 14 days after the learning attempt**, adjusting to retention and workload. These are approximate offsets, not deadlines. Missing a date does not erase prior learning. Prioritize S, repeated H, failed mocks, and fragile prerequisite skills; then review overdue independent solves.

For a cold re-solve, open only the statement and contract, start a blank editor, and keep hints, old code, and the concepts guide closed. Reconstruct a baseline, derive the improvement, state the invariant, implement, and run both ordinary and risk-focused tests. Explain costs before comparing with the earlier attempt. If you needed help, record H or S even if the final code passes.

Update the **same attempt line** with the latest date, assistance code, minutes, next due date, and one diagnostic lesson. For N items, update the existing roadmap Log that the workbook references. Retain short history there if useful; do not copy the task into a separate review table. Lessons such as “treated equality as strictly greater” or “subtracted the exposed top after pop” are more actionable than “careless mistake.”

A cold familiar solve can establish retention and P evidence. R additionally needs unfamiliar transfer across separate sessions. If review exposes a concept failure, use the roadmap's Reviewing status and reconsider affected gates manually. A minor syntax error that you catch yourself is different from needing the invariant supplied.

## 12. Completion and Readiness Checklist

Use these criteria to assess evidence, then update only the [roadmap dashboard](./DSA_Interview_Roadmap.md#2-progress-dashboard). This table is a rubric, not a second set of gate checkboxes.

| Milestone | Required evidence | Gate relationship |
| --- | --- | --- |
| Foundations complete | F1–F4 independently implemented and tested; explain LIFO, traversal direction, empty behavior, and running-state restoration; C1/C4 reasoning distinguishes endpoint work and live frames | Supports I prerequisites; reading alone supports neither I nor P |
| Theory understood | Explain the essential concepts without notes: matching, postfix operand order, auxiliary minima, monotonic state/strictness, indices, and total push/pop work | T under the roadmap's definition |
| Core implementations reliable | P1–P3 and M1–M5 built from a blank editor, with invariants and supplied boundary tests; M2 specifically demonstrates next-greater indices; postfix remains required even if N3 is deferred | I; optional M6 is excluded |
| Essential platform practice complete | Independent or cold independent solutions to N1, N2, N4, with exact platform contracts, explanations, meaningful tests, and complexity derivations | P; a successful submission or checked attempt alone is insufficient |
| Ready for Additional practice | Essential patterns are reliable, or a specific gap calls for focused practice; attempt N3 to strengthen expression reasoning | Additional breadth does not replace missing Essential evidence |
| Ready to investigate optional Hard | Can derive M3/M4 boundaries, justify equality, compute widths, and explain M6's interval contract; choose N5 if useful for the target role | Optional extension; never a prerequisite for ordinary P/R |
| Interview readiness demonstrated | Independently select and derive solutions to unfamiliar variants in separate sessions; include a deferred-work task and a task where another technique is preferable; satisfy every dimension below | R with date/evidence recorded once at the roadmap Stack readiness paragraph |

Apply the [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category): concepts, recognition, Java implementation, complexity, edge cases, independence, and communication. For Stack specifically, show what each entry represents, why pushes/pops preserve correctness, why a first answer is not skipped, why loops terminate, how duplicates affect comparisons, and why total work is linear where claimed. Propose tests for empty state, unresolved suffixes, ties, operand order, and overflow as applicable.

Completing every optional task is unnecessary. Conversely, completing a checklist without being able to solve and explain a fresh problem does not establish readiness. Use C and D tasks to diagnose gaps; an unrepairable core defect or unsupported complexity claim needs attention before claiming reliable I/R evidence. No gate or exercise is pre-completed here.

## 13. References and Delayed Checks

### Focused reference map

| Need | Existing material |
| --- | --- |
| Scope, learning sequence, and assessment | [Roadmap Stack](./DSA_Interview_Roadmap.md#3-stack), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy) |
| Endpoint semantics and implementation choices | [Stack Java implementations](./Stack_Essential_Concepts.md#3-stack-operations-and-java-implementations), [LinkedList endpoints](./Java_List_ArrayList_LinkedList.md#linkedlist-end-methods-ranked) |
| Nesting, evaluation, and metadata | [Matching](./Stack_Essential_Concepts.md#5-matching-and-nested-structures), [RPN](./Stack_Essential_Concepts.md#6-expression-evaluation-and-rpn), [augmentation](./Stack_Essential_Concepts.md#7-augmented-state-and-aggregate-queries) |
| Monotonic invariants and boundaries | [Next greater/smaller](./Stack_Essential_Concepts.md#10-next-greater-and-next-smaller-patterns), [equality](./Stack_Essential_Concepts.md#9-increasing-decreasing-and-equal-values), [optional boundaries](./Stack_Essential_Concepts.md#11-optional-advanced-monotonic-boundaries) |
| Time, peak space, and amortization | [Stack cost derivation](./Stack_Essential_Concepts.md#13-deriving-time-and-space-complexity), [space complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity), [recursion](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity), [amortization](./Complexity/Complexity_Analysis_and_Foundations.md#13-amortized-complexity) |
| Array preservation and comparison | [Array copying](./Java_Array.md#copying-arrays), [Java Arrays](./Java_Array.md) |
| Text and allocation costs | [Java String costs](./Java_String.md#8-complexity-expectations-for-dsa), [collection-operation costs](./Complexity/Complexity_Analysis_and_Foundations.md#15-java-standard-library-operation-complexities) |
| Choosing among already learned approaches | [Arrays & Hashing](./Arrays_and_Hashing/Arrays_and_Hashing_Essential_Concepts.md), [Two Pointers](./Two_Pointers/Two_Pointers_Essential_Concepts.md), [Stack versus alternatives](./Stack_Essential_Concepts.md#15-choosing-between-stacks-and-alternatives) |
| Risk-focused tests | [Test design](./Testing_Essential_Concepts.md#designing-test-cases-before-writing-code), [Stack edge cases](./Stack_Essential_Concepts.md#14-java-pitfalls-and-edge-cases) |

Java API behavior was checked with Context7 against the official Java 21 [Deque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html) and [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) documentation. These document endpoint semantics, iteration order, null rejection, amortized ordinary operations, and linear searches such as contains. For P2, consult [Integer parsing](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html), [Math checked arithmetic](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html), and [integer division rules](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.2). The concepts guide remains the primary explanation; these API references settle precise library contracts.

### Delayed complexity checks

Open only after writing your derivation. These concise cost checks contain no completed coding-task solutions or debugging repairs. Match assumptions as well as Big-O notation.

<details>
<summary>C1 check</summary>

The shown code takes Θ(n+1) total amortized time: n pushes and n pops, plus loop overhead. Peak auxiliary space is Θ(n+1), even though the final logical stack is empty. A growing push can be linear individually; this does not change the sequence bound. The scalar sum depends only on n and admits O(1) arithmetic using a widened triangular-number expression, with O(1) space under the given bounds.

</details>
<details>
<summary>C2 check</summary>

Time is Θ(n+1) amortized: every nonzero occurrence is pushed once and popped at most once. Peak storage is O(p+1), where p is the largest undrained batch; the deque can retain that capacity after a drain. A single zero can trigger Θ(p) work. A pending subtotal plus a committed total achieves O(n) time and O(1) auxiliary space because this output does not require retaining individual values.

</details>
<details>
<summary>C3 check</summary>

Time is Θ(n+1) amortized, not Θ(n²). There are n pushes, at most n pops, and at most one final failed condition check per outer iteration, with constant-cost index/value comparisons. ArrayDeque growth has its own amortized bound. Peak auxiliary space is O(n), attained by decreasing or equal values. A single final larger arrival can remove n−1 entries. For this scalar observation, each removed occurrence is exactly one that has some later strictly greater value; a suffix summary can count those in O(n) time and O(1) auxiliary space. This simplification does not return nearest indices.

</details>
<details>
<summary>C4 check</summary>

For k=n−i, recursion makes k+1 calls and has k+1 simultaneously live frames at its deepest point: Θ(k+1) time and space, assuming sufficient stack capacity. Java does not promise tail-call elimination, and this expression has a pending addition. The explicit full-array version uses Θ(n+1) amortized time and Θ(n+1) auxiliary space. An ordinary running sum needs O(n) time and O(1) space; this contract does not require deferred work. Large recursive tests may fail due to JVM stack limits rather than arithmetic.

</details>
<details>
<summary>C5 check</summary>

With u distinct values, searches give O(n(u+1)) time and O(u+1) auxiliary space. The all-distinct family performs a growing sequence of unsuccessful linear searches, giving Θ(n²) worst-case time. The all-equal family takes Θ(n) time and constant space. A HashSet offers expected/amortized O(n) total time and O(u) storage for fixed-size integer keys under ordinary hashing assumptions. ArrayDeque's constant-time endpoint operations do not make contains constant time.

</details>
<details>
<summary>C6 check</summary>

The source-level copies cost 1+2+...+n = Θ(n²) time and cumulative copied slots/allocation. Peak logically reachable auxiliary storage is O(n): the stack and a bounded number of prefix-sized temporary arrays/lists, not all snapshots retained together. Garbage-collector timing is separate. Retaining every snapshot would require Θ(n²) storage. Under the actual scalar contract, the sum depends only on n and can be computed in O(1) time/space with long arithmetic; reporting snapshots themselves would impose their output cost.

</details>

### Workbook validation and limits

All **32 Java blocks** compiled with Temurin JDK 21 using `javac --release 21 -Xlint:all -Werror`, with the documented wrappers/imports. Independent test models checked **175 tabulated examples**, including exception outcomes, record fields, and nonunique pair witnesses. Temporary Java programs reproduced real failures in **all seven debugging snippets**, including both independent D4 defects and D7's nontermination in a process that was terminated after a timeout.

Runtime checks also covered the analysis examples, 300 seeded analysis inputs, 3,280 small arrays and corresponding cancellation strings, ASCII duplicate pairs, checked arithmetic, and structured inputs of length 100,000. Nested scans and direct leftmost deletion served as independent small-input oracles. Large boundary cases used known formulas and linear reference computations. C6's maximum-size scalar count was verified without allocating its quadratic volume of temporary snapshots; recursive checks stayed small because JVM stack capacity is environment-dependent.

All **60 local links and heading anchors** were verified. The five curated entries match the roadmap's titles, order, priorities, and difficulties. All **18 pre-existing files inside DSA_Guide** retained their original bytes; only this workbook was added, with validation files kept outside the repository. The coding stubs intentionally remain unfinished for the learner. These checks establish consistency of the workbook and its test expectations; independent implementation, explanation, and readiness evidence still come from your attempts.
