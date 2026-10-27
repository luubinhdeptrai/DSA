# Recursion — Exercises (Java 21)

This workbook turns [Recursion Essential Concepts](./Recursion_Essential_Concepts.md) into independent execution, implementation, debugging, and interview practice. It is **supplementary Phase 0 preparation**, not a nineteenth NeetCode category. The [roadmap](./DSA_Interview_Roadmap.md) remains authoritative for dependencies, priorities, and T/I/P/R evidence.

## Table of Contents

- [1. Scope, Prerequisites, and Practice Conventions](#1-scope-prerequisites-and-practice-conventions)
- [2. Recursive Problem-Solving Workflow](#2-recursive-problem-solving-workflow)
- [3. Level 1 — Understanding Recursive Execution](#3-level-1--understanding-recursive-execution)
- [4. Level 2 — Fundamental Implementations](#4-level-2--fundamental-implementations)
- [5. Level 3 — Intermediate Recursion](#5-level-3--intermediate-recursion)
- [6. Level 4 — Recurrences and Complete Costs](#6-level-4--recurrences-and-complete-costs)
- [7. Base Cases and Termination Reviews](#7-base-cases-and-termination-reviews)
- [8. Debugging Recursive Algorithms](#8-debugging-recursive-algorithms)
- [9. Recursion, Iteration, and Explicit Stack Decisions](#9-recursion-iteration-and-explicit-stack-decisions)
- [10. Level 5 — Unfamiliar Interview Problems](#10-level-5--unfamiliar-interview-problems)
- [11. Supplementary External Practice](#11-supplementary-external-practice)
- [12. Interview Communication and Mock Sessions](#12-interview-communication-and-mock-sessions)
- [13. Revision and Spaced Repetition](#13-revision-and-spaced-repetition)
- [14. Completion and Readiness Evidence](#14-completion-and-readiness-evidence)
- [15. Delayed Execution Checkpoints](#15-delayed-execution-checkpoints)
- [16. References and Validation](#16-references-and-validation)

## 1. Scope, Prerequisites, and Practice Conventions

### A differentiated practice set

There are **45 original exercises**: **18 coding exercises**, **6 tracing exercises**, **7 complexity investigations**, **3 termination reviews**, **8 debugging challenges**, and **3 algorithm-selection discussions**. The coding exercises are F1–F8, M1–M6, and X1–X4. Three external suggestions are optional and are not counted as originals. Debug repairs are part of their respective debugging attempts, not additional coding assignments.

| Level | Exercises | Evidence to produce |
| --- | --- | --- |
| 1: execution | T1–T6 | Handwritten calls, frame state, returns, and reference diagrams |
| 2: fundamentals | F1–F8 | Small recursive methods derived from contracts |
| 3: patterns | M1–M6 | Accumulators, halving, branching, decomposition, and structural mutation |
| 4: analysis and repair | C1–C7, N1–N3, D1–D8, J1–J3 | Defensible recurrences, complete space, termination, bug witnesses, and choices |
| 5: transfer | X1–X4 | A suitable design derived from an unfamiliar statement |

Start with T1 and a blank trace. Existing documents do not establish prerequisite mastery. If you cannot explain array bounds, a suffix, a node reference, or a half-open interval, use the relevant reference and return to the exercise. A hint-assisted attempt is useful evidence of a gap, not an independent solve.

### Concept-to-exercise map

| Foundation in the concepts guide | Practice |
| --- | --- |
| [Contracts and correctness](./Recursion_Essential_Concepts.md#6-recursive-contracts-and-correctness) | F3, F4, F7, M4, M6; the six-part proof packet |
| [Frames and execution](./Recursion_Essential_Concepts.md#4-java-call-stack-and-stack-frames) | T1–T6 |
| [Base cases and progress](./Recursion_Essential_Concepts.md#3-base-cases-recursive-cases-and-termination) | F1–F8, N1–N3, D1–D4 |
| [Linear recursion](./Recursion_Essential_Concepts.md#7-linear-recursion-over-numbers-and-sequences) | F1–F8 |
| [Tail recursion](./Recursion_Essential_Concepts.md#8-tail-recursion-and-accumulators) | T5, M1, J1 |
| [Branching and repeated states](./Recursion_Essential_Concepts.md#9-multiple-branch-recursion) | T2, M5, C3, C7 |
| [Divide and conquer](./Recursion_Essential_Concepts.md#10-divide-and-conquer) | T6, M2–M4, C4, X2 |
| [Recurrences](./Recursion_Essential_Concepts.md#11-recursion-trees-and-recurrence-relations) | C1–C7, D7 |
| [Complete auxiliary space](./Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space) | C4–C6, M4–M5, X4 |
| [Reference values and mutation](./Recursion_Essential_Concepts.md#13-java-references-and-recursive-state) | T3–T4, F6, M6, D5–D6 |
| [Iteration and explicit stacks](./Recursion_Essential_Concepts.md#14-recursion-iteration-and-explicit-stacks) | M1, J1–J3, X1, X3–X4 |

Review only demonstrated gaps using [Java Arrays](./Java_Array.md#3-indexes-length-and-traversal), [Java Strings](./Java_String.md#indexes-length-and-traversal), [Stack](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack), [Binary Search intervals](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants), and [Linked List references](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity).

### Shared contracts: part of every exercise

Read these together with each exercise's specific contract.

- Arrays and strings are nonnull. Null arrays/strings are outside the contract; no prescribed validation or exception is required. A null list head means an empty list.
- Length limits, sortedness, acyclicity, and stated element bounds are preconditions unless an exercise explicitly requires rejection. Do not add full-input validation to every recursive frame and then omit it from complexity.
- Scalar arguments and range endpoints explicitly marked **reject** must produce `IllegalArgumentException`. For mutating exercises, reject invalid endpoints **before any mutation**. Exception message text is unrestricted.
- `MIN` and `MAX` mean `Integer.MIN_VALUE` and `Integer.MAX_VALUE`. Ordinary int elements may be negative, zero, repeated, or extreme unless a narrower range is stated. Widen operands before arithmetic that could overflow int.
- A Java `char` is a **UTF-16 code unit**. String exercises use exact case-sensitive code-unit equality; they do not promise Unicode code-point or grapheme behavior.
- Helpers are permitted for every coding exercise. Helpers may assume the public method established its contract. No external dependencies, streams replacing the requested traversal, or calls to ready-made solutions.
- Read-only input means values, links, and ordering must be unchanged after execution. New working arrays/containers are forbidden unless explicitly permitted. Recursion frames and a fixed number of scalar locals are permitted; returned records are permitted where declared.
- Required linear-depth exercises use at most 200 elements/steps, numeric digit recursion at most ten digits, and factorial at most 20. These are modest teaching limits, **not a portable guarantee of safe JVM depth**. Test on an ordinary Java 21 runtime; reason about larger-depth risks without deliberately running huge recursive chains.
- Complexity describes how work grows with the named size within the algorithmic model, even where fixed-width arithmetic or a teaching limit bounds executable inputs. Use fixed-width scalar arithmetic as O(1); do not infer that bounded examples make all algorithms equally useful.
- `C` counts **all invocations in the recursive family**, including the root and base invocations. `D` counts maximum simultaneous logical frames in that family, including the current frame and suspended callers. If a wrapper is outside the family, report its extra frame separately. Edge depth is D−1.
- Children execute sequentially. Analyze source-level logical frames without assuming tail-call elimination. Java 21 does **not guarantee** tail-call optimization.
- Auxiliary space excludes pre-existing input and the required returned output, but includes recursive frames, temporary copies, explicit stack entries, and intermediate objects. Report output storage separately. Peak live storage and cumulative allocation are different quantities.

Unless specified otherwise, a coding submission needs the [workflow packet](#2-recursive-problem-solving-workflow), all supplied tests, at least two additional tests, a clean repeated-call test, and a cost explanation. Compiling a starter does not implement its TODO.

### Java 21 compilation convention

Place the following shared declarations and any exercise's Java block inside one `Practice` class. Add `import java.util.*;` above the class if using lists/deques. All blocks use distinct names and can also coexist. Their starter exceptions intentionally prevent accidental claims of completion.

```java
static final class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

record Extremes(int min, int max) {}
record Digits(int count, int sum) {}
record Summary(long sum, int evenCount) {}
record Ledger(long total, long lowestPrefix, int firstNegativeIndex) {}
```

For example, compile your own scratch `Practice.java` with `javac --release 21 Practice.java`, then run a test entry point. Use standard Java assertions with `-ea` or explicit checks that throw `AssertionError`; a program printing plausible values is insufficient validation. No scratch files need to be added to this repository.

List examples name identities: `A(4) -> B(4) -> null` contains two distinct nodes. Check reference identity and each saved node's `next` where a contract concerns links. Compare arrays by contents and strings by `equals`, not `==`.

### Attempt records and honest results

Each original exercise has one unchecked record. Check it only after completing an attempt, running/reviewing the required evidence, and writing a postmortem. A checked record can still have H or S.

`I = independently solved; H = needed a conceptual hint; S = needed substantial solution review or could not finish; — = not attempted.` Time includes explanation and testing. Keep later attempts on the same record, for example `Last: 2026-11-02 H → 2026-11-05 I`; this is an illustration, not entered progress. Do not create another revision ledger.

The [roadmap's four gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remain separate from attempt result I. There is no Recursion dashboard row or invented E/A/C classification here.

## 2. Recursive Problem-Solving Workflow

Before coding, write the **six-part recursive contract packet**:

1. **Promise:** exactly what a call returns or changes for its valid arguments.
2. **Base guarantee:** why the direct result satisfies that promise, including empty/minimal inputs.
3. **Smaller instance:** exact child arguments and why they remain valid.
4. **Combination:** how child results or effects establish the current promise.
5. **Termination:** a nonnegative integer measure that strictly decreases along every recursive edge.
6. **Correctness:** base plus the smaller-instance assumption implies the full postcondition, including state ownership.

Then write Java, trace one ordinary case and one boundary, and derive **separate** result, call-count, time, and depth recurrences where useful. Do not multiply time by the mathematical returned value. For a mutating algorithm, record the state immediately before and after each child and after the full call.

Use this blank planning card:

| Obligation | Your derivation |
| --- | --- |
| Input domain, rejection, numeric bounds | |
| Returned meaning / permitted mutation | |
| Recursive helper contract | |
| Directly solved case | |
| Child arguments and decreasing measure | |
| Pending work after each child | |
| Correctness argument | |
| C, D, local work, time | |
| Frames, other live objects, output | |
| Iterative alternative and tradeoff | |

For M2–M4 and X2, additionally name **divide, child subproblems, and combine**. For F3, F4, F7, M4, and M6, write all six proof items **before** opening an editor. For every implementation, explain the base, relationship, representative trace, time, D, and auxiliary stack aloud.

For tracing, predict first and run the code only after recording a complete attempt. For debugging, reproduce the defect before editing. For complexity, count actual statements and allocations rather than matching a familiar method name to a memorized Big O.

## 3. Level 1 — Understanding Recursive Execution

No implementation is required in T1–T6. For each, identify the first call, every invocation's arguments and locals, base execution, call and completion orders, eventual result/effects, C and D. Distinguish **unassigned** locals from locals holding zero. A return event occurs while that frame still exists.

Use a separate row for an entry, child-call suspension, resumption, mutation, or return. Add as many rows as necessary; the blank rows are not a promised event count.

| Step | Active call / invocation ID | Arguments and locals | Live frame chain; depth | Action | Return value |
| --- | --- | --- | --- | --- | --- |
| 1 | Root | Fill in | Root; 1 | Enter | — |
| 2 | | | | | |
| 3 | | | | | |
| … | | | | | |

Also draw the stack at the deepest point. T2/T6 need a call tree: label **invocation instances**, so two calls with equal arguments still receive different IDs.

### T1 — The waiting addition

**Difficulty:** Easy. **Objective:** Track a local value while a child runs.

**Prerequisites:** Frames, local variables, return flow.

Trace `t1(3)` and then `t1(0)`; domain 0..10. Record `held` and `child` separately in each frame. Do any local assignments change a suspended parent's locals? Explain the direct case and prove progress. Input is scalar, no mutation or allocated working objects.

```java
static int t1(int n) {
    if (n == 0) return 2;
    int held = n + 1;
    int child = t1(n - 1);
    return held + child;
}
```

**Deliverable:** complete the common trace table, compute C/D and the root value, and distinguish the moment `child` becomes assigned from the moment the call starts.

- [ ] **T1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### T2 — Two answers waiting to combine

**Difficulty:** Medium. **Objective:** Separate a branching call tree from the simultaneously live stack.

**Prerequisites:** T1; sequential expression evaluation.

Trace `t2(3)` and the bases `t2(0)`, `t2(1)`; domain 0..8. Enumerate every call instance and eventual return. At the first entry into the right child of the root, identify which left-child frames still exist and which left result the root retains. No mutable input or growing allocations.

```java
static int t2(int n) {
    if (n <= 1) return n + 1;
    int left = t2(n - 1);
    int right = t2(n - 2);
    return 2 * left + right;
}
```

Complete this tree and mark entry order versus completion order:

```text
root: t2(3)
+-- first child: t2(?)
|   +-- ...
|   +-- ...
+-- second child: t2(?)
```

**Deliverable:** C, D, root value, the live chain at each base, and a recurrence reflecting the two actual child sizes.

| Invocation ID | n | Entry order | left after return | right after return | Completion order / result |
| --- | --- | --- | --- | --- | --- |
| Root | 3 | 1 | | | |
| | | | | | |
| … | | | | | |

<details>
<summary>Hint 1</summary>

A returned child contributes a value without keeping all its frames alive.

</details>

<details>
<summary>Hint 2</summary>

The two bases are different inputs; label both instead of expanding either.

</details>

<details>
<summary>Hint 3</summary>

In a table, assign `left` only after the entire first subtree finishes, then evaluate `right`.

</details>

- [ ] **T2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### T3 — One array, several suspended callers

**Difficulty:** Medium. **Objective:** Trace mutation alongside return-value aggregation.

**Prerequisites:** T1; array aliases and indices.

Use array R=`[2,-1,3]`, call `t3(R,0)`, and also test `t3([],0)`. Valid initial arrays have length 0..8 and elements −10..10; initial index is zero. Record R after every assignment and at the deepest base. Which values are saved in independent `before` slots? Which reads observe the current shared array? The code intentionally mutates R and allocates no arrays.

```java
static int t3(int[] a, int i) {
    if (i == a.length) return 0;
    int before = a[i];
    a[i] = before + i;
    int rest = t3(a, i + 1);
    a[i] += rest;
    return before + rest;
}
```

**Deliverable:** a final scalar result, final R contents, mutation order, and C/D. Add a `Shared R` column to the common table; a reference copied into a frame is not an array clone.

| Step | Current i | Saved before | Returned rest | Shared R now | Action / return |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | Unassigned | Unassigned | [2,-1,3] | Enter root |
| | | | | | |
| … | | | | | |

<details>
<summary>Hint 1</summary>

A local primitive can preserve a value even after the source array changes.

</details>

<details>
<summary>Hint 2</summary>

The child handles later indices, then the current frame resumes its second assignment.

</details>

<details>
<summary>Hint 3</summary>

Keep two columns: the saved `before` value and the array slot's current value.

</details>

- [ ] **T3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### T4 — Which caller observes the change?

**Difficulty:** Medium. **Objective:** Distinguish primitive updates, object mutation, and local reference reassignment.

**Prerequisites:** T3; ListNode identity.

Caller variables are `x=3`, `a=R=[7]`, and `node=A(5)` with A.next=null. Trace `t4(x,a,node,2)`. Name every newly allocated array and node, including those reachable only through parameters. Predict caller x, R contents, A.val/A.next, and caller reference identities after return. Also trace remaining=0. The trace domain is remaining 0..3, one-element arrays, values 0..10 initially; all operations fit int. Distinguish reassigning a local reference from mutating its referent in your own explanation.

```java
static void t4(int x, int[] a, ListNode node, int remaining) {
    if (remaining == 0) return;
    x += 10;
    a[0] += 1;
    node.val += 2;
    a = new int[] {x};
    node = new ListNode(x);
    t4(x, a, node, remaining - 1);
}
```

**Deliverable:** frame-to-object arrows before and after each reassignment, object contents after return, allocation count, C/D, and a precise statement of Java pass-by-value. Do not equate the name `a` with one permanent object.

| Frame / caller | x slot | a refers to | node refers to | Referent contents at this step |
| --- | --- | --- | --- | --- |
| Caller before call | 3 | R | A | R=[7], A.val=5, A.next=null |
| Root after first mutation | | | | |
| Root after reassignment | | | | |
| … | | | | |

<details>
<summary>Hint 1</summary>

Draw variables as slots and arrays/nodes as separately named objects.

</details>

<details>
<summary>Hint 2</summary>

Each call gets its own parameter slots; inspect which object each slot holds at each statement.

</details>

<details>
<summary>Hint 3</summary>

After a reassignment, continue the same trace with a new arrow rather than renaming the old object.

</details>

- [ ] **T4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### T5 — A result carried into the next call

**Difficulty:** Easy. **Objective:** Recognize tail position and accumulator state.

**Prerequisites:** T1; arithmetic order.

Trace `t5(3,0)` and `t5(0,7)`; n is 0..10 and the initial accumulator is 0..10. List arguments on descent and every returned value on ascent. Identify work done before the recursive call versus work still pending after it. No mutation or allocation.

```java
static int t5(int n, int accumulator) {
    if (n == 0) return accumulator;
    return t5(n - 1, accumulator + 2 * n - 1);
}
```

**Deliverable:** C/D, result, tail-position classification, and a proposed meaning for the accumulator. Does tail position permit claiming O(1) Java stack? Justify using the guide, not a timing experiment.

- [ ] **T5 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### T6 — The range journal

**Difficulty:** Medium. **Objective:** Trace disjoint subproblems, ordered events, and a noncommutative combination.

**Prerequisites:** T2; half-open ranges; list append.

Start with `a=[8,3,2]`, an empty events list, and call `t6(a,0,3,events)`. Also consider `([],0,0)` and `([9],0,1)` with fresh event lists. Valid arrays have length 0..8 and values −10..10, and bounds denote a valid half-open range. Input a is read-only; events is intentionally appended to. Predict exact event order, returned value, and C/D; reversing child order can change more than the drawing.

```java
static int t6(int[] a, int lo, int hi, List<String> events) {
    if (lo == hi) {
        events.add("empty");
        return 0;
    }
    if (hi - lo == 1) {
        events.add("value " + a[lo]);
        return a[lo];
    }
    int mid = lo + (hi - lo) / 2;
    events.add("open");
    int left = t6(a, lo, mid, events);
    int right = t6(a, mid, hi, events);
    events.add("close");
    return left - right;
}
```

**Deliverable:** draw ranges on the call tree, annotate each completed child's value, and separate returned diagnostic output storage from stack space. Explain why an empty child is or is not reached from a nonempty input.

<details>
<summary>Hint 1</summary>

For odd lengths, calculate the midpoint rather than drawing equal-size children.

</details>

<details>
<summary>Hint 2</summary>

A parent waits for both scalar results but retains only one completed child's scalar while the other runs.

</details>

<details>
<summary>Hint 3</summary>

Append the closing event after both child traces; perform subtraction in the code's stated order.

</details>

- [ ] **T6 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 4. Level 2 — Fundamental Implementations

Implement these from a blank editor after writing a contract. Every exercise changes a different obligation: numeric identity, overflow, a selected suffix, a two-field summary, string units, visible mutation, decimal representation, or paired boundaries. Passing supplied cases alone is not a proof.

### F1 — Total through a boundary

**Difficulty:** Easy. **Objective:** Derive a scalar recurrence with an empty sum.

**Prerequisites:** T1; nonnegative decreasing measures.

**Problem / input:** receive int n. **Output:** the exact long sum of integers 1 through n; n=0 denotes an empty sum.

**Contract:** accept 0..200; reject every other int with IllegalArgumentException. Recursion is required, with at most one recursive child per nonbase call. Helpers are allowed; no loops, closed-form formula, arrays, or containers in the submitted calculation. No input mutation. The bounded result fits long.

**Target:** O(n+1) time and O(n+1) auxiliary stack; O(1) non-frame state. Derive C and D for your base convention.

```java
public static long sumToN(int n) {
    throw new UnsupportedOperationException("TODO F1");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `n=0` | `0` |
| `n=1` | `1` |
| `n=5` | `15` |
| `n=200` | `20100` |
| `n=-1` | `IllegalArgumentException` |
| `n=201` | `IllegalArgumentException` |

**Complete when:** the six-part packet explains the empty sum, both rejected boundaries are checked, and a trace of n=4 shows pending additions. **Follow-up:** if the numeric domain expanded, which limitation would become relevant before long overflow?

- [ ] **F1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F2 — Products and their numeric limit

**Difficulty:** Easy. **Objective:** Choose the correct empty product and justify a numeric domain.

**Prerequisites:** F1; long arithmetic.

**Problem / input:** receive int n. **Output:** exact n! as long, with 0!=1.

**Contract:** accept 0..20; reject all other ints. Recursion is required; helpers allowed; no loops, BigInteger, arrays, or containers in the implementation. Input is scalar and unchanged. Every intermediate product must be computed using a type that preserves the valid result.

**Target:** O(n+1) time and O(n+1) stack, O(1) non-frame state. State why allowing 21 would require a changed numeric contract.

```java
public static long factorial(int n) {
    throw new UnsupportedOperationException("TODO F2");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `n=0` | `1` |
| `n=1` | `1` |
| `n=5` | `120` |
| `n=12` | `479001600` |
| `n=13` | `6227020800` |
| `n=20` | `2432902008176640000` |
| `n=-1 or n=21` | `IllegalArgumentException` |

**Complete when:** your trace distinguishes a product's mathematical recurrence from its running-time recurrence, and the 12/13 and 20/21 boundaries are explained. **Follow-up:** why does assigning an overflowing int multiplication to a long variable fail to recover the result?

- [ ] **F2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F3 — A precise suffix promise

**Difficulty:** Easy. **Objective:** Write a recursive contract before array-sum code.

**Prerequisites:** T1; half-open suffixes; long addition.

**Problem / input:** a nonnull int array values of length n=0..200 and a starting index from. **Output:** the long sum of values[from..n).

**Contract:** accept 0<=from<=n; reject other from values. Elements may be any int. Recursion is required; helpers allowed; no loops or allocated arrays/containers. Read-only input. The absolute sum is at most 200·2147483648 and fits long. Empty suffix returns zero, including from=n on a nonempty array.

**Target:** with k=n−from, O(k+1) time and O(k+1) auxiliary stack, O(1) non-frame state. Before coding, write the complete six-part contract packet with this exact suffix meaning.

```java
public static long suffixTotal(int[] values, int from) {
    throw new UnsupportedOperationException("TODO F3");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `[], from=0` | `0` |
| `[4,-1,7], from=0` | `10` |
| `[4,-1,7], from=1` | `6` |
| `[4,-1,7], from=3` | `0` |
| `[MAX,MAX], from=0` | `4294967294` |
| `[MIN,MIN], from=0` | `-4294967296` |
| `200 copies of MAX, from=0` | `429496729400` |
| `[5], from=-1 or 2` | `IllegalArgumentException` |

**Complete when:** you prove that no index before from is read, reject invalid bounds, preserve the array, and count the empty-suffix frame. **Follow-up:** why does passing the same reference repeatedly not allocate k copies of the input?

- [ ] **F3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F4 — Two extremes in one range

**Difficulty:** Medium. **Objective:** Combine a fixed-size child summary without inventing an invalid numeric identity.

**Prerequisites:** F3; records; minimum and maximum.

**Problem / input:** values has length 0..200 and arbitrary int elements; lo/hi describe a half-open range. **Output:** Extremes(min,max) containing the least and greatest values in that range.

**Contract:** require 0<=lo<hi<=values.length; reject all other bounds, including an empty range. Recursion is required and must remove one position per level. Helpers and fixed-size Extremes objects are allowed; no loops, additional arrays, collections, or input mutation. No arithmetic difference between min/max is needed, so extreme ints remain valid.

**Target:** k=hi−lo, O(k) time and O(k) auxiliary space including frames; output O(1). Do not run two separate traversals. Write the six-part packet before code, including a base that works for all-negative ranges.

```java
public static Extremes extremes(int[] values, int lo, int hi) {
    throw new UnsupportedOperationException("TODO F4");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `[-8,-3,-3], lo=0, hi=3` | `Extremes(-8,-3)` |
| `[9,4,-7,3], lo=1, hi=3` | `Extremes(-7,4)` |
| `[0], lo=0, hi=1` | `Extremes(0,0)` |
| `[MAX,MIN], lo=0, hi=2` | `Extremes(MIN,MAX)` |
| `200 copies of -5, lo=0, hi=200` | `Extremes(-5,-5)` |
| `[], lo=0, hi=0` | `IllegalArgumentException` |
| `[1,2], bounds (-1,1), (1,1), (2,1), or (0,3)` | `IllegalArgumentException` |

**Complete when:** one child summary supports both answers, the chosen base does not silently assume nonnegative elements, and you distinguish output from intermediate records. **Follow-up:** can the scalar maximum alone summarize enough information for this return contract?

<details>
<summary>Hint 1</summary>

The smallest nonempty range already determines both result fields.

</details>

<details>
<summary>Hint 2</summary>

A child can promise the extremes of the suffix after the current element.

</details>

<details>
<summary>Hint 3</summary>

Combine one returned record with one input value; choose the record fields independently.

</details>

- [ ] **F4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F5 — Matching units in a suffix

**Difficulty:** Easy. **Objective:** Count exact string matches without copied substrings.

**Prerequisites:** F3; UTF-16 code-unit indexing.

**Problem / input:** nonnull String text of 0..200 UTF-16 units, char wanted, and int from. **Output:** the number of positions at or after from whose char equals wanted.

**Contract:** accept 0<=from<=text.length(); reject other from values. Empty suffix returns zero. Any char, including a surrogate or '\u0000', is allowed. Recursion is required; helpers permitted; no loops, substring, toCharArray, new arrays, collections, or mutation. Counts are at most 200 and fit int.

**Target:** k=text.length()−from, O(k+1) time and stack, O(1) non-frame state.

```java
public static int countUnitFrom(String text, char wanted, int from) {
    throw new UnsupportedOperationException("TODO F5");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `text="", wanted='a', from=0` | `0` |
| `text="abAa", wanted='a', from=0` | `2` |
| `text="abAa", wanted='a', from=1` | `1` |
| `text="abAa", wanted='z', from=0` | `0` |
| `text="\uD83D\uDE00", wanted='\uD83D', from=0` | `1` |
| `text="\uD83D\uDE00", wanted='\uD83D', from=2` | `0` |
| `200 copies of 'a', wanted='a', from=0` | `200` |
| `text="a", from=-1 or 2` | `IllegalArgumentException` |

**Complete when:** code-unit semantics, case sensitivity, the empty suffix, and the no-copy rule are tested. Trace a mismatch between two matches. **Follow-up:** what would change if the task counted displayed symbols?

- [ ] **F5 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F6 — Update only the selected positions

**Difficulty:** Easy. **Objective:** Prove a mutating suffix/range contract.

**Prerequisites:** T3; half-open bounds.

**Problem / input:** int[] values of length 0..200, bounds lo/hi, and any int replacement. **Output:** void; after return exactly the selected positions values[lo..hi) equal replacement, and every other position retains its original value.

**Contract:** accept 0<=lo<=hi<=length; reject other bounds before any write. Empty ranges are valid. Recursion is required; helpers permitted; no loops, new arrays/containers, or replacement of the caller's array. This is an in-place element update, but recursive stack storage still counts. No arithmetic on element values is necessary, so MIN/MAX are safe.

**Target:** k=hi−lo, O(k+1) time and O(k+1) stack; O(1) other auxiliary state.

```java
public static void fillRange(int[] values, int lo, int hi, int replacement) {
    throw new UnsupportedOperationException("TODO F6");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `[], lo=0, hi=0, replacement=9` | `[]` |
| `[1,2,3], lo=1, hi=3, replacement=9` | `[1,9,9]` |
| `[1,2,3], lo=1, hi=1, replacement=9` | `[1,2,3]` |
| `[MAX], lo=0, hi=1, replacement=MIN` | `[MIN]` |
| `[4,4], lo=0, hi=2, replacement=0` | `[0,0]` |
| `200 zeros, lo=0, hi=200, replacement=-1` | `200 copies of -1` |
| `[1,2], bounds (-1,1), (2,1), or (0,3)` | `IllegalArgumentException; [1,2] unchanged` |

**Complete when:** aliases observe the same changed array, out-of-range positions remain unchanged, and invalid bounds produce no partial writes. **Follow-up:** is 'in-place mutation' sufficient evidence for O(1) total auxiliary space?

- [ ] **F6 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F7 — A decimal inspection

**Difficulty:** Medium. **Objective:** Define recursion over numeric representation and handle Integer.MIN_VALUE.

**Prerequisites:** Integer division/remainder; widening before absolute value.

**Problem / input:** any int value, positive, negative, or zero. **Output:** Digits(count,sum) for the ordinary decimal digits of its magnitude, excluding the sign and leading zeros. Zero has one digit and digit sum zero.

**Contract:** all ints are valid, so no invalid scalar needs rejection. Recursion is required; helpers and fixed-size Digits records are permitted. No loops, strings, arrays, collections, or input mutation. Divide/remainder by ten are allowed. Choose a helper magnitude type that represents the magnitude of MIN; the result fields fit int.

**Target:** for d decimal digits, O(d) time and O(d) stack, O(d) total auxiliary space if intermediate records are retained; returned output O(1). d<=10. Write the complete contract packet before coding.

```java
public static Digits inspectDigits(int value) {
    throw new UnsupportedOperationException("TODO F7");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `value=0` | `Digits(1,0)` |
| `value=7` | `Digits(1,7)` |
| `value=-1` | `Digits(1,1)` |
| `value=10020` | `Digits(5,3)` |
| `value=-506` | `Digits(3,11)` |
| `value=MAX` | `Digits(10,46)` |
| `value=MIN` | `Digits(10,47)` |

**Complete when:** the zero representation is counted once, the sign is excluded, MIN is correct, and your termination measure applies to the chosen magnitude type. **Follow-up:** why is taking int absolute value first and then widening too late?

<details>
<summary>Hint 1</summary>

The sign does not contribute a digit, but its magnitude may require a wider type.

</details>

<details>
<summary>Hint 2</summary>

A one-digit magnitude is directly inspectable; state what a child promises for the remaining higher digits.

</details>

<details>
<summary>Hint 3</summary>

Promote before negation/absolute value, and combine the current remainder with one child summary.

</details>

- [ ] **F7 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### F8 — A mirrored unit sequence

**Difficulty:** Medium. **Objective:** Use two moving boundaries and meaningful early termination.

**Prerequisites:** Two Pointers basics; F5's code-unit contract.

**Problem / input:** a nonnull String text of 0..200 UTF-16 units. **Output:** true exactly when its code-unit sequence reads identically forward and backward.

**Contract:** empty/singleton strings are true. Every char is valid; equality is case-sensitive. Recursion is required; helpers allowed; no loops, copied strings/arrays, collections, or mutation. No numeric aggregation can overflow. Do not normalize case or Unicode.

**Target:** O(n+1) worst-case time and O(n+1) worst-case stack, O(1) non-frame state; state any better early-exit behavior. Derive the actual decrement in unresolved units per recursive step.

```java
public static boolean mirroredUnits(String text) {
    throw new UnsupportedOperationException("TODO F8");
}
```


**Representative tests** (also use two tests of your own):

| Input | Expected result / post-state |
| --- | --- |
| `text=""` | `true` |
| `text="x"` | `true` |
| `text="aba"` | `true` |
| `text="abba"` | `true` |
| `text="abca"` | `false` |
| `text="Aa"` | `false` |
| `text="\uD83D\uDE00"` | `false` |
| `text="\u0000a\u0000"` | `true` |
| `200 copies of 'z'` | `true` |

**Complete when:** odd/even lengths and both base shapes are handled, a mismatch does not require inspecting every inner position, and UTF-16 behavior is explicit. **Follow-up:** would a loop simplify the same proof or change the auxiliary-space bound?

<details>
<summary>Hint 1</summary>

A mismatch at the ends decides the entire question.

</details>

<details>
<summary>Hint 2</summary>

When the ends agree, define the remaining interval precisely.

</details>

<details>
<summary>Hint 3</summary>

Choose a base that covers both crossing boundaries and a single remaining unit.

</details>

- [ ] **F8 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 5. Level 3 — Intermediate Recursion

These exercises change the recursive state or call-tree shape. They do not require Trees, Graphs, Backtracking search machinery, or dynamic programming. Use the conceptual branching enumerator without treating it as completion of the later Backtracking category.

### M1 — Move the pending work into state

**Difficulty:** Medium. **Objective:** Convert a known recursive contract into tail recursion and iteration.

**Prerequisites:** F3; T5; accumulator invariants.

**Problem / input:** reuse F3's nonnull int[] values of length 0..200 and from. **Output:** implement two methods returning the same long suffix sum as F3: one tail-recursive, one iterative.

**Contract:** reject from outside 0..length. Input is read-only. Helpers are permitted. In suffixTotalTail, use a helper whose recursive call is in tail position and whose state includes a long accumulator; no loops. In suffixTotalLoop, use a loop and no recursive calls. Neither version may allocate arrays/containers. All valid sums fit long.

**Target:** k=length−from. Both take O(k+1) time. Analyze Java tail-recursive stack as O(k+1), while the iterative method must use O(1) auxiliary space. No promise of tail-call optimization. This is a conversion exercise, not a second independent array-sum problem.

```java
public static long suffixTotalTail(int[] values, int from) {
    throw new UnsupportedOperationException("TODO M1 tail");
}

public static long suffixTotalLoop(int[] values, int from) {
    throw new UnsupportedOperationException("TODO M1 loop");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[], from=0 (both methods)` | `0` |
| `[4,-1,7], from=0 (both)` | `10` |
| `[4,-1,7], from=1 (both)` | `6` |
| `[4,-1,7], from=3 (both)` | `0` |
| `[MAX,MAX], from=0 (both)` | `4294967294` |
| `[MIN,MIN], from=0 (both)` | `-4294967296` |
| `200 copies of MAX, from=0 (both)` | `429496729400` |
| `[5], from=-1 or 2 (both)` | `IllegalArgumentException` |

**Complete when:** you relate processed state, remaining suffix, and original result in one invariant; trace both forms side by side; identify the variables preserved by the loop conversion; and compare their D. **Follow-up:** for a million-element scan, which form would you select and why? Do not run the deep recursive version.

<details>
<summary>Hint 1</summary>

Pending additions can become already-completed work carried into the child.

</details>

<details>
<summary>Hint 2</summary>

The helper's promise must mention both the remaining suffix and the accumulator.

</details>

<details>
<summary>Hint 3</summary>

Translate the helper's arguments into loop state; preserve which old values each update uses.

</details>

- [ ] **M1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### M2 — Find a value within ordered data

**Difficulty:** Medium. **Objective:** Derive a recursive search interval and prove discarded positions cannot match.

**Prerequisites:** Binary Search interval reasoning; scalar return propagation.

**Problem / input:** nonnull values, sorted nondecreasing, length n=0..100000; target is any int. **Output:** any index whose value equals target, or −1 when absent.

**Contract:** duplicates are permitted; first/last occurrence is not required. Sortedness and length are preconditions, not a validation scan. Empty input is absent. Recursion is required; helpers allowed and must use half-open bounds. No subarray copies, collections, loops replacing search, or input mutation. Compute a midpoint without risky endpoint addition. Element extremes require comparisons, not subtraction.

**Target:** O(log(n+1)+1) worst-case time and stack, O(1) non-frame state. Specify divide/retained subproblem/combine and why only one child is necessary.

```java
public static int locate(int[] values, int target) {
    throw new UnsupportedOperationException("TODO M2");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[], target=3` | `-1` |
| `[4], target=4` | `0` |
| `[4], target=5` | `-1` |
| `[-3,0,4,4,9], target=4` | `2 or 3` |
| `[-3,0,4,4,9], target=5` | `-1` |
| `[MIN,0,MAX], target=MIN` | `0` |
| `[MIN,0,MAX], target=MAX` | `2` |
| `values[i]=i for 0<=i<100000, target=99999` | `99999` |
| `same array, target=-1` | `-1` |

**Complete when:** every nonreturning step strictly shrinks the candidate interval, all allowed matches are accepted by tests, and best-case early success is distinguished from worst-case miss. **Follow-up:** how would validating sortedness on every recursive call alter the claimed cost?

<details>
<summary>Hint 1</summary>

A comparison can disprove a whole region only because the order precondition holds.

</details>

<details>
<summary>Hint 2</summary>

Give the empty half-open interval a precise absent result.

</details>

<details>
<summary>Hint 3</summary>

A checked mismatch must be excluded consistently; trace one-element misses on both sides.

</details>

- [ ] **M2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### M3 — A maximum from disjoint pieces

**Difficulty:** Medium. **Objective:** Contrast two balanced children with a linear chain.

**Prerequisites:** F4; half-open ranges; balanced decomposition.

**Problem / input:** nonnull int[] values, length n=0..100000, arbitrary int elements. **Output:** maximum element as int.

**Contract:** reject an empty array. Recursion is required: partition each nonsingleton range into two near-equal, disjoint, nonempty ranges and combine their answers. Helpers allowed; no loops replacing traversal, arrays, collections, or input mutation. Do not sort. No risky arithmetic on values is needed.

**Target:** O(n) time and O(log(n+1)) auxiliary stack for nonempty input; O(1) non-frame locals per invocation. Derive exact C and maximum D, including non-power-of-two lengths. State divide, both child promises, combine, recurrence, and correctness.

```java
public static int maximumFromPieces(int[] values) {
    throw new UnsupportedOperationException("TODO M3");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[-8,-3,-3]` | `-3` |
| `[MIN]` | `MIN` |
| `[2,9,1,9,4]` | `9` |
| `[MAX,MIN]` | `MAX` |
| `100000 copies of -7` | `-7` |
| `[]` | `IllegalArgumentException` |

**Complete when:** your tree for five elements has exactly one leaf per input position; you explain why two calls do not imply exponential time; and you compare with an iterative maximum scan. **Follow-up:** why can a balanced recursive algorithm have more total calls but fewer live frames than F4?

<details>
<summary>Hint 1</summary>

The maximum of a union of disjoint nonempty ranges depends on one summary from each.

</details>

<details>
<summary>Hint 2</summary>

Use a singleton as a directly solved range, then check both child sizes are smaller.

</details>

<details>
<summary>Hint 3</summary>

Pass bounds over one shared array; a split need not allocate two arrays.

</details>

- [ ] **M3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### M4 — A two-field range report

**Difficulty:** Medium. **Objective:** Combine independent summaries and account for intermediate returned objects.

**Prerequisites:** M3; F3; fixed-size records.

**Problem / input:** nonnull int[] values of length 0..100000, and half-open lo/hi. **Output:** Summary(sum,evenCount) for precisely values[lo..hi), where sum is long and evenCount counts values divisible by two, including zero and negative evens.

**Contract:** accept 0<=lo<=hi<=length; reject other bounds. Empty range yields Summary(0,0). Recursion is required: split nonsingleton ranges into two near-equal disjoint ranges and combine fixed-size summaries. Helpers/records allowed; no new arrays, collections, loops replacing traversal, or mutation. The largest absolute valid sum is at most 100000·2147483648, fitting long; count fits int.

**Target:** k=hi−lo, O(k+1) time, O(log(k+1)+1) peak auxiliary space including frames and intermediate records; returned output O(1). Also derive cumulative record allocation for your design. Write all six proof items before code, then divide/children/combine and cost recurrences.

```java
public static Summary reportRange(int[] values, int lo, int hi) {
    throw new UnsupportedOperationException("TODO M4");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[], lo=0, hi=0` | `Summary(0,0)` |
| `[2,-3,4,0], lo=0, hi=4` | `Summary(3,3)` |
| `[2,-3,4,0], lo=1, hi=3` | `Summary(1,1)` |
| `[7], lo=0, hi=1` | `Summary(7,0)` |
| `[MIN,MAX], lo=0, hi=2` | `Summary(-1,1)` |
| `100000 copies of MAX, lo=0, hi=100000` | `Summary(214748364700000,0)` |
| `[1,2], lo=1, hi=1` | `Summary(0,0)` |
| `[1,2], bounds (-1,1), (2,1), or (0,3)` | `IllegalArgumentException` |

**Complete when:** the empty identity works in both fields, odd lengths cover every element once, and your live-object diagram explains why cumulative allocations need not equal peak retained records. **Follow-up:** what changes if each child returns a copy of its entire range instead?

<details>
<summary>Hint 1</summary>

The two result fields can each be combined from the corresponding child fields.

</details>

<details>
<summary>Hint 2</summary>

State the empty summary before deciding whether your helper ever reaches an empty child.

</details>

<details>
<summary>Hint 3</summary>

Keep child records constant-size and release them conceptually when their parent finishes; account for the first child's retained result during the second call.

</details>

- [ ] **M4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### M5 — Codes for three-position switches

**Difficulty:** Medium. **Objective:** Build a small exhaustive choice enumerator and distinguish full states.

**Prerequisites:** T2; primitive prefix state; List<Integer> output.

**Problem / input:** int digits in 0..8. Each of digits ordered switches has one of three positions: 0, 1, or 2. Interpret a completed setting as a base-three integer, including leading zeros. **Output:** a fresh List<Integer> containing every code in increasing order, once each. Zero switches have one empty setting represented by code 0.

**Contract:** reject digits outside 0..8. Recursion is required: choose one next switch per edge and explore its three positions in order. A loop over the three immediate choices is allowed; directly looping over all completed codes is not. Helpers and one returned ArrayList are allowed; no other arrays/containers. No mutable input; no state shared across public calls. The largest code is 6560 and fits int.

**Target:** O(3^digits) time, O(digits+1) auxiliary stack excluding O(3^digits) returned output. Draw the full call tree for digits=2, count leaves/C/D, and derive C separately from output count.

```java
public static List<Integer> switchCodes(int digits) {
    throw new UnsupportedOperationException("TODO M5");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `digits=0` | `[0]` |
| `digits=1` | `[0,1,2]` |
| `digits=2` | `[0,1,2,3,4,5,6,7,8]` |
| `digits=3` | `27 codes: every integer 0 through 26, in order` |
| `digits=8` | `6561 codes: every integer 0 through 6560, in order` |
| `digits=-1 or 9` | `IllegalArgumentException` |
| `call digits=1 twice` | `two distinct lists, each [0,1,2]` |

**Complete when:** you establish exhaustive coverage with no duplicates, explain the zero-switch result, and compare the full state (remaining,prefix) against C7's repeated state. **Follow-up:** if output became digit strings, which cost would acquire a factor proportional to digits?

<details>
<summary>Hint 1</summary>

Leading zero choices still describe different paths until the fixed number of positions is filled.

</details>

<details>
<summary>Hint 2</summary>

A child needs to know both how many choices remain and the selected prefix.

</details>

<details>
<summary>Hint 3</summary>

Make the output list belong to one public call; append only complete settings and preserve child order.

</details>

- [ ] **M5 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### M6 — Remove flagged nodes and release their links

**Difficulty:** Medium. **Objective:** Derive a recursive structural postcondition with identity and detachment.

**Prerequisites:** Linked List references; F6 mutation reasoning.

**Problem / input:** head of an acyclic null-terminated chain with 0..200 nodes, arbitrary int values, and int flagged. **Output:** head of the original nodes whose value differs from flagged, in their original order.

**Contract:** null is empty. The caller grants exclusive ownership of links. Every removed node must have next=null after return, including nodes removed from the middle or from a consecutive run. Retained nodes keep their values and identities; the result is acyclic and null-terminated. Recursion is required; helpers allowed; no new nodes, arrays, containers, or value swaps. Link mutation is required. No numeric aggregation or invalid scalar rejection is needed; cyclic input is outside the contract.

**Target:** O(n+1) time and O(n+1) recursive stack, O(1) other state per frame. This rewires existing nodes but is not O(1) total auxiliary space. Write a six-part contract before code, including the postcondition for removed identities.

```java
public static ListNode removeFlagged(ListNode head, int flagged) {
    throw new UnsupportedOperationException("TODO M6");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `head=null, flagged=2` | `null` |
| `A(2), flagged=2` | `null; A.next=null` |
| `A(1), flagged=2` | `A -> null` |
| `A(2)->B(1)->C(2)->D(3)->E(2), flagged=2` | `B -> D -> null; A.next=C.next=E.next=null` |
| `A(2)->B(2)->C(2), flagged=2` | `null; every original next=null` |
| `A(MIN)->B(MAX)->C(MIN), flagged=MIN` | `B -> null; A.next=C.next=null` |
| `200 nodes with value 7, flagged=8` | `same 200 identities in the same order` |

**Complete when:** tests retain handles to every original node, verify removed links individually, verify the returned identity sequence, and use the returned head. **Follow-up:** why does only reassigning a local head fail to change the caller's head variable?

<details>
<summary>Hint 1</summary>

Ask a smaller suffix to return its retained chain under the full detachment contract.

</details>

<details>
<summary>Hint 2</summary>

The current node either joins that returned chain or is excluded from it.

</details>

<details>
<summary>Hint 3</summary>

Preserve access to the child suffix before changing links; inspect the removed node's next as well as the returned chain.

</details>

- [ ] **M6 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 6. Level 4 — Recurrences and Complete Costs

These Java blocks are **analysis subjects**, not starter solutions to the coding exercises. Each method can be run on its small valid domain after the paper attempt. Unless stated otherwise, scalar bounds are preconditions, not required runtime guards. No concurrent execution or hidden memoization occurs.

Answer all ten questions for **each C exercise**:

1. What measures input size? Distinguish numeric magnitude, number of elements, and digit count.
2. What is the actual time recurrence, including bases and integer rounding?
3. How many recursive-family calls execute? Give a recurrence and exact small-input counts.
4. What work occurs outside child calls, including library work and initialization?
5. What is the time bound, and why is it tight or only an upper bound?
6. What is maximum D, including base frames?
7. What is auxiliary **stack** space under the frame model?
8. What additional memory is allocated per call, and what remains live?
9. What is total peak auxiliary space? Report output separately.
10. Why do your recurrence, level/path argument, and Java code agree?

Copy this card for each analysis; there are no complexity answers beside the subjects.

| Quantity | Your derivation | Small-input evidence |
| --- | --- | --- |
| Input size and base | | |
| Result recurrence / value | | |
| C recurrence and count | | |
| Local work; T recurrence; time | | |
| D recurrence and maximum | | |
| Stack-only space | | |
| Per-call allocation; cumulative allocation | | |
| Peak non-frame storage; total auxiliary | | |
| Required output storage | | |

Your final set should distinguish a one-step chain, a halving chain, two n−1 children, two halves with linear local work, copied suffixes, and unequal predecessor children. Match these structures to the code before writing a formula.

### C1 — A chain of constant-size returns

**Difficulty:** Easy. **Objective:** Separate the returned answer from execution cost.

**Prerequisites:** T1; recurrence base costs.

Analyze n=0..200; no invalid-input handling is required for this analysis subject. It is read-only scalar computation with no growing allocations. Hand-trace n=0, 1, and 4. Define separate recurrences for the returned value and C, then complete the ten-question cost card.

```java
static long c1(int n) {
    if (n == 0) return 0;
    long child = c1(n - 1);
    return child + 3;
}
```

**Evidence:** exact C/D for the three traces; a sum of local work; and a proof that the numeric result is not a multiplier in the time recurrence.

- [ ] **C1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C2 — A shrinking integer

**Difficulty:** Medium. **Objective:** Derive a logarithmic chain with integer rounding and explicit bases.

**Prerequisites:** C1; integer division.

Analyze n=0..100000; argument bounds are preconditions. Trace n=0, 1, 2, 7, and 8. Determine the returned value and complete the cost card. Do not treat integer n/2 as exact real division at every step.

```java
static int c2(int n) {
    if (n <= 1) return 1;
    return 1 + c2(n / 2);
}
```

**Evidence:** an exact formula for positive n's C/D under this base convention, a separate statement for n=0, and an explanation of why one recursive child does not necessarily mean linear time.

<details>
<summary>Hint 1</summary>

Count how many times a positive integer can be halved before reaching one.

</details>

<details>
<summary>Hint 2</summary>

The base call itself still contributes a frame and a return.

</details>

<details>
<summary>Hint 3</summary>

List the values for 7 and 8 before expressing the answer with floor(log2(n)).

</details>

- [ ] **C2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C3 — Two calls with the same remaining count

**Difficulty:** Medium. **Objective:** Explain exponential work with a linear live stack.

**Prerequisites:** T2; powers of two.

Analyze n=0..16. All returned values fit long; keep experiments within this teaching domain. Draw the full tree for n=3, and trace n=0 and 1. Both child statements must be represented in the recurrence for this actual code; do not silently replace the program with a formula.

```java
static long c3(int n) {
    if (n == 0) return 1;
    long left = c3(n - 1);
    long right = c3(n - 1);
    return left + right;
}
```

**Evidence:** exact returned value, leaf count, C and D for n=3; a general call-count recurrence; all ten cost answers; and a paragraph explaining why total calls do not all coexist. Compare the complete state here with M5's (remaining,prefix).

<details>
<summary>Hint 1</summary>

A level counts all work at that distance from the root; a live stack follows one path.

</details>

<details>
<summary>Hint 2</summary>

A zero-size child is still a call, and an internal call creates two equally sized children.

</details>

<details>
<summary>Hint 3</summary>

Time adds both children; peak depth uses the larger child depth plus the parent.

</details>

- [ ] **C3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C4 — A buffer kept until both reports return

**Difficulty:** Medium. **Objective:** Account for linear local work, balanced children, and retained per-call arrays.

**Prerequisites:** M3; peak versus cumulative allocation.

Analyze n=0..256. Arrays are internal working buffers, not required output. Trace n=0, 1, 2, 4, and 8; use powers of two first, then n=5. Array creation/initialization and each loop visit count in the source-level cost model. Each scratch buffer is read after both children return, so include it among the parent's retained working objects.

```java
static long c4(int n) {
    if (n <= 1) return n;
    int[] scratch = new int[n];
    for (int i = 0; i < n; i++) scratch[i] = i + 1;
    long left = c4(n / 2);
    long right = c4(n - n / 2);
    long checksum = 0;
    for (int value : scratch) checksum += value;
    return left + right + checksum;
}
```

**Evidence:** all ten answers; for n=8, C, D, cumulative allocated int slots, and peak simultaneously retained int slots; a recurrence with floors/ceilings for odd n. Explain why counting only calls misses some work.

**Optional merge-sort reasoning, within this same attempt:** inspect the [concepts guide's divide-and-conquer section](./Recursion_Essential_Concepts.md#10-divide-and-conquer). Compare its one shared buffer with c4's per-call buffers. Trace the merges for [6,1,4,1] and state divide/sort/combine, correctness, time, stack, and total auxiliary space. No full merge-sort implementation is required here.

<details>
<summary>Hint 1</summary>

Local work includes both loops and the buffer allocation, not only the final addition.

</details>

<details>
<summary>Hint 2</summary>

For total allocation, add across the whole tree; for peak memory, inspect what remains reachable on one active descent.

</details>

<details>
<summary>Hint 3</summary>

Write n+n/2+n/4+… for a power-of-two active path, and separately total the buffers at each tree level.

</details>

- [ ] **C4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C5 — A shorter array at every call

**Difficulty:** Medium. **Objective:** Measure copied data and identify when linear call count is misleading.

**Prerequisites:** F3; array copying; C4's space distinctions.

Analyze nonnull int[] a with length n=0..64 and arbitrary int values. It is read-only; the scalar result fits long. Trace [], [5], and [2,-1,4]. Arrays.copyOfRange creates a new tail array, including a length-zero array at the final nonempty step. Analyze materialized arrays and source-level liveness; do not rely on optimization removing them.

```java
static long c5(int[] a) {
    if (a.length == 0) return 0;
    int[] tail = Arrays.copyOfRange(a, 1, a.length);
    long rest = c5(tail);
    return rest + a[0];
}
```

**Evidence:** all ten answers; for n=4, list every copied length and every retained array at the deepest point. Exclude the original input from auxiliary storage, but include array objects as well as their element storage asymptotically. Describe an index-based alternative without writing a full solution.

<details>
<summary>Hint 1</summary>

A smaller argument does not imply constant work was needed to create it.

</details>

<details>
<summary>Hint 2</summary>

The parent needs its own a again after the child returns.

</details>

<details>
<summary>Hint 3</summary>

Sum the lengths of the proper suffix copies; count frames separately and compare the two growth rates.

</details>

- [ ] **C5 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C6 — A text checksum with hidden work

**Difficulty:** Medium. **Objective:** Include implementation-specific substring copying in time and live storage.

**Prerequisites:** F5; C5; String immutability.

Analyze nonnull ASCII text of length n=0..64. XOR is bitwise exclusive-or on char values promoted to int, not exponentiation. The returned int checksum never exceeds the unsigned char range. Use the ordinary OpenJDK 21 materialized proper-suffix copying model described in the [concepts guide](./Recursion_Essential_Concepts.md#12-time-depth-and-complete-auxiliary-space), not an assumption that all JDKs/API operations allocate identically. Empty-string reuse is allowed. Trace "", "a", "abca", and "aaaa".

```java
static int c6(String text) {
    if (text.isEmpty()) return 0;
    int rest = c6(text.substring(1));
    return rest ^ text.charAt(0);
}
```

**Evidence:** all ten answers; exact copied content lengths for "abca"; a peak-live argument using the parent's post-child charAt read; and what changes when the original text and an index identify the remaining input. This is an algorithmic storage model, not a measurement of exact JVM heap bytes.

<details>
<summary>Hint 1</summary>

An immutable object can still be copied to construct another immutable object.

</details>

<details>
<summary>Hint 2</summary>

Determine how much content each proper suffix contains, and when its parent is last used.

</details>

<details>
<summary>Hint 3</summary>

Treat empty/full-range special cases separately from growing proper-suffix copy costs.

</details>

- [ ] **C6 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### C7 — Repeated predecessor questions

**Difficulty:** Medium. **Objective:** Identify overlapping full states in naive Fibonacci without prescribing a DP implementation.

**Prerequisites:** T2; C3; recurrence definitions.

Analyze n=0..20, a practical teaching bound. F(0)=0, F(1)=1, and later values are the sum of the previous two. Draw the entire call tree for n=5 and mark every repeated argument. Use n=0 and n=1 as separate bases. Return values fit long in this domain; the limit is also intended to keep repeated-work experiments small.

```java
static long c7(int n) {
    if (n <= 1) return n;
    long left = c7(n - 1);
    long right = c7(n - 2);
    return left + right;
}
```

**Evidence:** all ten cost answers; exact C and D for n=0..6; the actual unequal-child recurrence; and an explanation of exponential work versus linear peak stack. A justified O(2^n) upper bound is acceptable; an optional tighter bound must be identified as tighter. State what a reusable state key would contain, but no memoization or tabulation implementation is required.

<details>
<summary>Hint 1</summary>

Equal full arguments identify repeated pure questions here; remaining depth alone was not M5's whole state.

</details>

<details>
<summary>Hint 2</summary>

The two child sizes are n−1 and n−2, not two copies of n−1.

</details>

<details>
<summary>Hint 3</summary>

Derive C(0), C(1), then C(n)=1 plus both child counts; derive D with a maximum instead of a sum.

</details>

- [ ] **C7 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 7. Base Cases and Termination Reviews

These are Level 4 reviews of genuinely defective claims or implementations. For each, identify a failing input, trace the first calls, propose a correction, prove its termination and correctness, and analyze costs. A decreasing measure must stay in a well-founded domain; a machine exception is not a correct base answer.

For a suspected nonterminating recursion, a short paper prefix is sufficient. If you execute it, use a **separate JVM process with a timeout** and catch/report the expected resource failure only in that disposable harness. Do not make an application depend on catching StackOverflowError.

### N1 — Does every permitted input reach the stop?

**Difficulty:** Medium. **Objective:** Separate a decreasing expression from a valid nonnegative termination measure.

**Prerequisites:** Base cases; integer domains.

A proposed method promises ceil(n/2) for every n in 0..20, and promises IllegalArgumentException for negative n. Review the implementation below. First predict behavior for n=0,1,2,3 and −1 without running it. Identify which advertised inputs reach a mathematically valid base. Repair both the domain handling and the progress/base relationship; helpers are allowed, no arrays or mutation. Derive the repaired time/depth bounds.

```java
static int n1(int n) {
    if (n == 0) return 0;
    return 1 + n1(n - 2);
}
```

**Deliverable:** a smallest witness, the first six argument values for that witness, a valid decreasing nonnegative measure for your repair, and correct results for both parity classes. Distinguish an infinite mathematical call sequence from Java eventually exhausting its stack. Do not treat int wraparound as the proof.

<details>
<summary>Hint 1</summary>

Strictly decreasing integers need a lower bound that the execution respects.

</details>

<details>
<summary>Hint 2</summary>

Check whether the advertised stopping value is reachable from both even and odd starting values.

</details>

<details>
<summary>Hint 3</summary>

Specify the boundary cases before adjusting the child argument or the stopping condition.

</details>

- [ ] **N1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### N2 — Review two proposed progress rules

**Difficulty:** Medium. **Objective:** Find a wrong direction and an argument-evaluation trap.

**Prerequisites:** T1; postfix decrement semantics.

Each method claims to return n for valid n=0..20 by counting steps to zero. No invalid-input handling is required for this review. Analyze both methods on n=0 and n=1; write the next five child arguments, and distinguish changes to a parent's local slot from values passed into the child. Give a corrected child rule and its termination proof, without using a loop to evade the recursive review.

```java
static int n2Away(int n) {
    if (n == 0) return 0;
    return 1 + n2Away(n + 1);
}

static int n2Post(int n) {
    if (n == 0) return 0;
    return 1 + n2Post(n--);
}
```

**Deliverable:** a defect explanation for each method, small terminating cases for the repaired version, C/D for your chosen base, and why changing JVM stack size would not repair either argument rule.

<details>
<summary>Hint 1</summary>

The proposed measure must move toward the stopping region on the child's state.

</details>

<details>
<summary>Hint 2</summary>

A postfix operation evaluates to the old value even though it changes the current local variable.

</details>

<details>
<summary>Hint 3</summary>

Write the value of the argument expression before drawing the next frame.

</details>

- [ ] **N2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### N3 — The promised list domain

**Difficulty:** Medium. **Objective:** Identify a missing structural precondition instead of assuming that following next makes progress.

**Prerequisites:** Linked List identity/cycle basics; recursion contracts.

A reviewer advertises n3 as counting distinct reachable nodes for any finite next-linked structure of at most 20 distinct nodes, including cycles. Inspect that claim. Compare null, A(7)->B(7)->null, and A(7)->B(7)->A. Draw node identities, not merely values. The method mutates no nodes and allocates no nodes/containers.

```java
static int n3(ListNode node) {
    if (node == null) return 0;
    return 1 + n3(node.next);
}
```

**Deliverable:** demonstrate why the advertised domain and code disagree. Propose two contract-level repairs: restrict to acyclic chains, or validate/reject cycles before using a recursive acyclic counter. For the latter, outline a known cycle check and state its additional cost; a full new cycle-detection implementation is not required. Explain why duplicate values alone do not invalidate acyclic termination.

<details>
<summary>Hint 1</summary>

Finitely many distinct objects do not imply finitely many next-edge traversals.

</details>

<details>
<summary>Hint 2</summary>

A decreasing remaining-node count requires that the same identities are not revisited.

</details>

<details>
<summary>Hint 3</summary>

Write the advertised promise and the smaller-call promise side by side; identify the assumption needed for them to be compatible.

</details>

- [ ] **N3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 8. Debugging Recursive Algorithms

Each D subject has a real defect against its stated functional, structural, or resource contract. The blocks compile, which does not establish correctness. For **every** challenge produce this eight-step packet:

1. Identify the defect, including the violated promise.
2. Construct a minimal failing input independently.
3. Trace the incorrect execution or retained state.
4. Explain why that execution fails the contract.
5. Correct the code without weakening the contract.
6. Prove the correction terminates on the valid domain.
7. Verify the supplied boundaries plus two additional regression tests.
8. Derive time, C/D, and complete auxiliary space after repair.

Do not open the delayed checkpoints until a witness and trace are recorded. No complete fixes are provided here.

### D1 — A product with a suspicious boundary

**Difficulty:** Easy. **Objective:** Validate a directly returned answer against the promised operation.

**Prerequisites:** F2; empty-product meaning.

**Intended contract:** for n=0..12, return n! as long; no input mutation or allocation. Bounds are preconditions. Use cases 0,1,4,12, but first construct your own smallest failing case. Repair the method recursively without changing the contract.

```java
static long d1(int n) {
    if (n == 0) return 0;
    return n * d1(n - 1);
}
```

**Completion evidence:** the eight-step debugging packet, all listed cases after repair, and separate result/time recurrences. Explain how a base result propagates through callers.

- [ ] **D1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D2 — The suffix boundary review

**Difficulty:** Medium. **Objective:** Repair an incorrect index boundary and test the empty case.

**Prerequisites:** F3; valid array indices.

**Intended contract:** initial call d2(a,0), with nonnull a of length 0..200 and arbitrary int values, returns its exact long sum. Input is read-only; no allocation. The internal index should identify an unresolved suffix. Test [], [7], [2,5], and [-3,0,4] after first constructing your own witness.

```java
static long d2(int[] a, int i) {
    if (i == a.length - 1) return 0;
    return (long) a[i] + d2(a, i + 1);
}
```

**Completion evidence:** show both a wrong returned value and an exception-producing valid input in the original. Repair the base/guard order, then defend the six-part recursive contract and cost.

<details>
<summary>Hint 1</summary>

The empty suffix and the last element are different subproblems.

</details>

<details>
<summary>Hint 2</summary>

Check whether the guard fires before reading index zero of an empty array.

</details>

<details>
<summary>Hint 3</summary>

Write the sum represented by a[i..length) at the exact boundary where it has no terms.

</details>

- [ ] **D2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D3 — The missing contribution

**Difficulty:** Easy. **Objective:** Check whether a correct child answer is actually used.

**Prerequisites:** F1; return propagation.

**Intended contract:** for n=0..200, return 1+...+n as long; n=0 returns zero. Bounds are preconditions. No mutation or allocation. Test 0,1,2,4 after creating a failing witness; repair recursively.

```java
static long d3(int n) {
    if (n == 0) return 0;
    d3(n - 1);
    return n;
}
```

**Completion evidence:** record the child's returned value and the parent's eventual result in different columns, identify the broken relationship, and show why making recursive calls alone does not establish correctness.

- [ ] **D3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D4 — A lookup that revisits a range

**Difficulty:** Medium. **Objective:** Validate half-open search progress, not just in-bounds accesses.

**Prerequisites:** M2; search invariants.

**Intended contract:** d4(a,target,0,a.length) searches a sorted nondecreasing nonnull array of length 0..200, returning any matching index or −1. Values/target are arbitrary ints, duplicates allowed, input unchanged, no copies. For direct helper tests require 0<=lo<=hi<=length. Construct a minimal miss that distinguishes valid bounds from strict progress; then test [], [4] with targets 3/4/5, and [1,4,7] with target 6.

```java
static int d4(int[] a, int target, int lo, int hi) {
    if (lo == hi) return -1;
    int mid = lo + (hi - lo) / 2;
    if (a[mid] == target) return mid;
    if (a[mid] < target) return d4(a, target, mid, hi);
    return d4(a, target, lo, mid);
}
```

**Completion evidence:** repeated interval witness, a corrected exclusion rule, proof that every retained interval is smaller, and worst-case time/stack after repair. Use the isolated-process policy for an original execution that does not reach its base.

<details>
<summary>Hint 1</summary>

A midpoint can be a valid index while its containing interval fails to shrink.

</details>

<details>
<summary>Hint 2</summary>

A mismatching midpoint cannot be the desired result under the exact-search contract.

</details>

<details>
<summary>Hint 3</summary>

Evaluate the smaller-than-target branch on a one-element range and write both old and new bounds.

</details>

- [ ] **D4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D5 — Results that remember another request

**Difficulty:** Medium. **Objective:** Find state that survives beyond one public call.

**Prerequisites:** T4; ownership of accumulators.

**Intended contract:** each d5(a) independently returns the long sum of its nonnull input of length 0..200; no input mutation, no dependence on earlier calls. Elements are arbitrary ints. In a fresh process, test [1,2], then [4], then [], without resetting fields between public calls. Also test a fresh first call on [].

```java
static long d5Total;

static long d5(int[] a) {
    d5From(a, 0);
    return d5Total;
}

private static void d5From(int[] a, int i) {
    if (i == a.length) return;
    d5Total += a[i];
    d5From(a, i + 1);
}
```

**Completion evidence:** the first call where the contract fails, a state-lifetime explanation, and a repair that scopes state to one operation or returns the answer. Include two consecutive identical calls, negative values, and empty input in regression tests. Discuss why resetting a shared field is less composable than avoiding it.

<details>
<summary>Hint 1</summary>

A static field and a recursive local variable have different lifetimes.

</details>

<details>
<summary>Hint 2</summary>

The contract describes this input, not the concatenation of all previous inputs.

</details>

<details>
<summary>Hint 3</summary>

Track the accumulator at each public method's entry, not only at each helper's entry.

</details>

- [ ] **D5 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D6 — The returned head is not the whole structure

**Difficulty:** Medium. **Objective:** Check link postconditions after recursive structural mutation.

**Prerequisites:** Linked List reversal contract; identity and cycles.

**Intended contract:** reverse the links of an exclusively owned acyclic chain of 0..200 nodes, return its new head, preserve every original identity/value exactly once, allocate no nodes/containers, and leave a null-terminated result. Test null, A(1), A(1)->B(2), and A(1)->B(1)->C(3). Save all node handles before the call. Recursion and link mutation are required for the repair.

```java
static ListNode d6(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode result = d6(head.next);
    head.next.next = head;
    return result;
}
```

**Completion evidence:** a minimal structural witness, every saved next field before/after, and a correction establishing the full contract. Inspect at most n+1 traversed identities when examining the original result; do not let a cyclic output hang the test. Explain why value-only output checks can miss the defect.

<details>
<summary>Hint 1</summary>

A correct returned head does not guarantee a correct tail or every intermediate link.

</details>

<details>
<summary>Hint 2</summary>

The old head's role changes when the suffix has been reversed.

</details>

<details>
<summary>Hint 3</summary>

Draw both directions between two original adjacent nodes after the assignment, then inspect the required termination of the output chain.

</details>

- [ ] **D6 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D7 — Correct values under a linear-work promise

**Difficulty:** Medium. **Objective:** Detect accidental recomputation as a violated resource contract.

**Prerequisites:** M3; C recurrences; repeated subproblems.

**Intended contract:** for a nonempty range [lo,hi) in a read-only int array of length 1..32, return its maximum in O(k) time, k=hi−lo, with balanced recursive decomposition and no copied arrays. Numeric results alone are insufficient. Add temporary call/depth counters outside the submitted API and compare equal-valued arrays of lengths 1,2,4,8,16. Do not change the subject before counting it.

```java
static int d7(int[] a, int lo, int hi) {
    if (hi - lo == 1) return a[lo];
    int mid = lo + (hi - lo) / 2;
    int left = d7(a, lo, mid);
    int right = d7(a, mid, hi);
    if (left >= right) return d7(a, lo, mid);
    return d7(a, mid, hi);
}
```

**Completion evidence:** prove that the returned maximum can be numerically correct, show a recurrence contradicting the linear-time promise for powers of two, and repair repeated work while preserving the result. Also analyze D and stack: extra work need not increase their order.

<details>
<summary>Hint 1</summary>

Count executed call expressions after the comparison as well as before it.

</details>

<details>
<summary>Hint 2</summary>

For equal half sizes, which already-answered child question is asked again?

</details>

<details>
<summary>Hint 3</summary>

A local variable can be the complete answer to a subproblem; inspect whether the final branch uses that value.

</details>

- [ ] **D7 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### D8 — A wide result with narrow intermediates

**Difficulty:** Medium. **Objective:** Locate where integer overflow occurs before a cast.

**Prerequisites:** F2; Java numeric promotion.

**Intended contract:** for n=0..20, d8(n) returns exact n! as long, without BigInteger or allocated containers. Bounds are preconditions for this snippet. Test 0,12,13,20 and construct the earliest overflow witness. The helper is part of the subject and must be included in the repair; do not change only the outer method's declared result type.

```java
static long d8(int n) {
    if (n == 0) return 1L;
    return (long) (n * d8IntProduct(n - 1));
}

private static int d8IntProduct(int n) {
    if (n == 0) return 1;
    return n * d8IntProduct(n - 1);
}
```

**Completion evidence:** exact mathematical versus actual values for the first failing n, operand types at every multiplication, a valid 0..20 repair, and the limit that would require rejecting 21. Explain why termination and a long return type are insufficient for numeric correctness.

<details>
<summary>Hint 1</summary>

The type of an assignment target does not retroactively change how its right-hand expression was evaluated.

</details>

<details>
<summary>Hint 2</summary>

Inspect both the helper's return type and the operands inside the parentheses.

</details>

<details>
<summary>Hint 3</summary>

Promote before the vulnerable multiplication, and ensure earlier helper computations were also exact.

</details>

- [ ] **D8 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 9. Recursion, Iteration, and Explicit Stack Decisions

These three Level 4 discussion exercises test selection rather than coding speed. Answer with assumptions and a concrete state model. Recursion, a loop, and a loop with an explicit stack are separate alternatives with different storage needs. No approach is universally superior.

### J1 — A large maintenance pass

**Difficulty:** Easy. **Objective:** Select a practical control structure when depth is large.

**Prerequisites:** F6; M1; Java stack limitations.

A service must replace every entry in an int array containing up to 100000 values, preserving the array object's identity and using O(1) auxiliary space. Compare a recursive index traversal, an accumulator-style/tail rewrite, and a direct loop. No implementation is required. A response must discuss readability, time, peak frames, other auxiliary memory, input depth, StackOverflowError risk, and ease of proof.

**Deliverable:** choose a design, justify the rejection of alternatives under this contract, and name the invariant/state that survives the conversion. Compare with a small bounded teaching input rather than claiming recursion is always inappropriate.

- [ ] **J1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### J2 — The structure of the requested answer

**Difficulty:** Medium. **Objective:** Decide when a recursive decomposition improves reasoning and when a scan suffices.

**Prerequisites:** M3–M4; recursive contracts.

Compare two read-only array tasks: (a) find the maximum of 100000 values; (b) produce a report whose specification recursively splits an interval and combines a left and right report in a fixed, order-sensitive way. For (b), assume child reports are fixed-size and the split is balanced. No implementation is required. Discuss recursion, direct iteration, and an explicit stack for both.

**Deliverable:** one recommended design per setting, what smaller subproblem exists, what pending state each design needs, and time/stack/total-space implications. Address all seven considerations listed in J1. Explain what additional specification you would request if the report combination were not defined.

<details>
<summary>Hint 1</summary>

A data structure stored as an array does not alone determine the best control structure.

</details>

<details>
<summary>Hint 2</summary>

Ask whether the result needs the hierarchy of intermediate reports or only a single running summary.

</details>

<details>
<summary>Hint 3</summary>

For the ordered combination, account for the completed left report while the right report is being computed.

</details>

- [ ] **J2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### J3 — Preserve the suspended computation

**Difficulty:** Medium. **Objective:** Design an explicit-stack simulation without losing continuation state.

**Prerequisites:** T2 or T6; Stack LIFO; Deque.

Choose T2 or T6 and sketch an iterative simulation that preserves the exact call order, return-time combination, and visible events. Pseudocode and state diagrams are enough; this is not a new coding exercise. Recursion is prohibited in the sketch. An explicit stack of custom fixed-size frames is permitted. State what each entry stores and when it is removed.

**Deliverable:** simulate the same small input as the selected T exercise, matching its result/event order; show stages, partial child answers, and peak entries. Explain why storing only the initial argument may be insufficient and why constant Java call depth does not imply constant total space. Compare readability, depth risk, and ease of reasoning.

<details>
<summary>Hint 1</summary>

A suspended caller remembers both where it was and what has already returned.

</details>

<details>
<summary>Hint 2</summary>

Give an entry a stage: before the first child, after it, or ready to combine.

</details>

<details>
<summary>Hint 3</summary>

Test the transition after the first child's completion; the parent must still know its left result and next action.

</details>

- [ ] **J3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 10. Level 5 — Unfamiliar Interview Problems

Keep hints and earlier implementations closed. Before coding, decide whether the problem is naturally recursive, define a smaller subproblem if one exists, determine a base, look for repeated complete states, and compare recursion, iteration, and explicit storage against the constraints. Some contracts favor a loop; do not force recursion merely because of the workbook title.

Use the target costs as resource constraints, not as a substitute for a derivation. An independence attempt includes explanation and risk-focused tests as well as code.

### X1 — Delivery ledger

**Difficulty:** Medium. **Objective:** Select a method under a large-input storage budget and maintain several related results.

**Prerequisites:** Long sums; arrays; earlier control-structure comparisons.

**Problem / input:** a nonnull int[] changes of length 0..100000. Each signed change is added to a running balance initially zero. **Output:** Ledger(total,lowestPrefix,firstNegativeIndex): the ending balance; the smallest balance reached, including the initial zero balance; and the earliest zero-based index after whose change the balance is negative, or −1 if this never occurs.

**Contract:** any int change is valid, including negative/extreme values. Empty input returns Ledger(0,0,-1). Helpers permitted; no input mutation, arrays/collections, or persistent fields. Return one fixed-size record. Recursion is optional; you must justify your choice. All balances fit long under the stated length bound.

**Resource target:** O(n+1) time and O(1) auxiliary space. A proposal that merely assumes a runtime will optimize deep tail recursion does not meet the portable Java requirement.

```java
public static Ledger deliveryLedger(int[] changes) {
    throw new UnsupportedOperationException("TODO X1");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[]` | `Ledger(0,0,-1)` |
| `[5]` | `Ledger(5,0,-1)` |
| `[-4,6,-3]` | `Ledger(-1,-4,0)` |
| `[3,-5,4,-1]` | `Ledger(1,-2,1)` |
| `[0,0,0]` | `Ledger(0,0,-1)` |
| `[MAX,MAX,MIN]` | `Ledger(2147483646,0,-1)` |
| `[MIN,MAX]` | `Ledger(-1,-2147483648,0)` |
| `100000 copies of MAX` | `Ledger(214748364700000,0,-1)` |

**Complete when:** you derive a baseline, decide whether recursion is suitable, explain the state invariant, distinguish the earliest negative index from the position of the minimum, and meet the storage budget. **Follow-up:** if a balance later recovers, which already-recorded evidence must remain unchanged?

<details>
<summary>Hint 1</summary>

All three answers concern the same sequence of prefixes.

</details>

<details>
<summary>Hint 2</summary>

A prefix summary must retain both a current value and historical information that cannot be reconstructed from the final total.

</details>

<details>
<summary>Hint 3</summary>

Before choosing recursive state, compare its live-frame growth with the required auxiliary-space budget.

</details>

- [ ] **X1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### X2 — Parcel marks

**Difficulty:** Medium. **Objective:** Derive a control structure from an ordered hierarchical definition.

**Prerequisites:** Half-open ranges; fixed-size summaries; long arithmetic.

**Problem / input:** a nonnull int[] readings of length n=0..100000, each in −1000..1000. A parcel containing no readings has mark 0. A parcel containing one reading has that reading as its mark. A larger parcel is divided after floor(k/2) readings, preserving order; its mark is the left parcel's mark plus twice the right parcel's mark. **Output:** the mark of the whole parcel as long.

**Contract:** length/value bounds are preconditions. Empty input is valid. Recursion or iteration is allowed; justify the choice. Helpers allowed. Input is read-only; no copied ranges. An explicit stack of fixed-size frames is allowed if choosing iteration; no other working arrays/containers. For a nonempty input, the loose magnitude bound n·2^ceil(log2(n))·1000 is below 1.4·10^13 here, so long is sufficient.

**Target:** O(n+1) time and O(log(n+1)+1) auxiliary space, with O(1) returned output. State divide, child promises, combine, recurrence, termination, and correctness before coding.

```java
public static long parcelMark(int[] readings) {
    throw new UnsupportedOperationException("TODO X2");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `[]` | `0` |
| `[7]` | `7` |
| `[3,4]` | `11` |
| `[8,3,2]` | `22` |
| `[1,2,3,4]` | `27` |
| `[-2,5,-1]` | `4` |
| `[0,0,0,0,0]` | `0` |
| `65536 copies of 1` | `43046721` |
| `100000 zeros` | `0` |

**Complete when:** odd sizes split exactly as specified, left/right order is preserved, and a proof connects your returned value to the parcel definition. **Follow-up:** why can flattening the definition into an ordinary unweighted sum change the answer?

<details>
<summary>Hint 1</summary>

The grouping rule is part of the requested result, not just a suggested implementation.

</details>

<details>
<summary>Hint 2</summary>

A report for a range should promise that range's parcel mark under the same rule.

</details>

<details>
<summary>Hint 3</summary>

With iteration, store enough state to combine the two returned marks in order; with recursion, count only one live descent at a time.

</details>

- [ ] **X2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### X3 — Endpoint report

**Difficulty:** Medium. **Objective:** Choose between return-time work and direct indexing while preserving linked data.

**Prerequisites:** Acyclic list traversal; arrays; returned output versus auxiliary memory.

**Problem / input:** an acyclic null-terminated list with 0..200 nodes and arbitrary int values. **Output:** a fresh int[] containing node values from tail to head. Distinct equal-valued nodes still contribute separate entries.

**Contract:** null returns a fresh empty array. All original next fields and values must remain unchanged; do not temporarily reverse the list. Recursion or iteration is allowed and must be justified. Helpers and one output array of exactly n elements are allowed; no extra arrays, collections, or new nodes. You may traverse the input more than once if the total work meets the target. No numeric aggregation can overflow.

**Target:** O(n+1) time; report your actual auxiliary bound excluding the O(n+1) returned array object/content. An O(n+1)-stack recursive design or an O(1)-auxiliary iterative design may be accepted with a correct explanation; neither is automatically preferable for every changed requirement.

```java
public static int[] endpointReport(ListNode head) {
    throw new UnsupportedOperationException("TODO X3");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `head=null` | `fresh []` |
| `A(7)` | `[7]` |
| `A(2)->B(-1)->C(4)` | `[4,-1,2]` |
| `A(5)->B(5)` | `[5,5]` |
| `A(MIN)->B(MAX)` | `[MAX,MIN]` |
| `200 nodes with values 0 through 199` | `[199,198,...,0]` |
| `two calls on A(3)->B(8)` | `distinct arrays [8,3]; original A->B->null unchanged` |

**Complete when:** you verify both output values and every original link/value, explain how length and output position are determined, and compare pending return-time work with loop state. **Follow-up:** what would a single-pass restriction change about your design?

<details>
<summary>Hint 1</summary>

The output array has random-access positions even though the input nodes do not.

</details>

<details>
<summary>Hint 2</summary>

Determine what you must know to place a head-side value at its final output position.

</details>

<details>
<summary>Hint 3</summary>

Compare recording work until return with obtaining the size and filling known array positions; count both passes if you use them.

</details>

- [ ] **X3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

### X4 — Inspection itinerary

**Difficulty:** Medium. **Objective:** Represent nested obligations explicitly while preserving an observable event order.

**Prerequisites:** J3; half-open ranges; Deque stack operations.

**Problem / input:** an int n in 0..64 denotes positions [0,n). Inspecting a range [lo,hi) emits the string `Elo:hi` on entry, with lo/hi replaced by their decimal values. If its length is greater than one, inspect [lo,mid) and then [mid,hi), where mid=lo+(hi−lo)/2. Finally emit `Xlo:hi` on exit. Empty and singleton ranges emit only their own entry/exit pair. **Output:** a fresh List<String> of the exact itinerary for [0,n).

**Contract:** reject n outside 0..64. The execution interface prohibits Java recursion; implement an iterative simulation using an explicit stack. Helpers, a fresh output ArrayList, and a Deque of fixed-size frames/events are allowed; no other arrays/containers or shared persistent state. Event formatting contains no spaces. All indices and counts fit int. Do not hardcode cases or sort generated events.

**Target:** O(n+1) aggregate time with bounded-size event strings, O(log(n+1)+1) auxiliary pending entries, and O(n+1) returned output. Explain stack order, continuation state, and why Java call depth stays constant.

```java
public static List<String> inspectionItinerary(int n) {
    throw new UnsupportedOperationException("TODO X4");
}
```


**Representative tests:**

| Input | Expected result / post-state |
| --- | --- |
| `n=0` | `[E0:0,X0:0]` |
| `n=1` | `[E0:1,X0:1]` |
| `n=2` | `[E0:2,E0:1,X0:1,E1:2,X1:2,X0:2]` |
| `n=3` | `[E0:3,E0:1,X0:1,E1:3,E1:2,X1:2,E2:3,X2:3,X1:3,X0:3]` |
| `n=64` | `254 events; first E0:64, last X0:64; each position has one singleton entry/exit pair` |
| `n=-1 or n=65` | `IllegalArgumentException` |
| `two calls with n=1` | `distinct lists with the same two events` |

**Complete when:** every exit occurs after its children, the left child is fully processed before the right, empty input is handled, and instrumentation confirms peak pending entries rather than equating space with total events. **Follow-up:** what changes if events must be consumed one at a time instead of returned in a list?

<details>
<summary>Hint 1</summary>

The next observable event is constrained by both range hierarchy and entry/exit timing.

</details>

<details>
<summary>Hint 2</summary>

An explicit entry can store a stage or represent an event that must happen after descendants.

</details>

<details>
<summary>Hint 3</summary>

LIFO reverses the order of newly pushed obligations; test n=2 before relying on a larger trace.

</details>

- [ ] **X4 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.


## 11. Supplementary External Practice

These are **optional supplementary suggestions**, not an official NeetCode Recursion category and not newly assigned E/A/C tiers. Original exercises are the primary practice. The official pages were checked for problem number, title, difficulty, and link on **2026-10-27**.

All three use already encountered structures. Their platform contracts differ from workbook contracts: reread the current page, including input lengths and the platform's node/signature conventions. A recursive method that works on a small workbook chain is not automatically safe for every platform-depth input.

| ID | Verified problem | Difficulty | Recursion-focused assessment |
| --- | --- | --- | --- |
| EX1 | [206. Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/) | Easy | Define the returned-head and final-tail contract; compare recursive and iterative links/space |
| EX2 | [21. Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/) | Easy | Define a smaller merged suffix and explain link ownership; compare recursive versus iterative depth |
| EX3 | [704. Binary Search](https://leetcode.com/problems/binary-search/) | Easy | Defend a recursive interval and compare it with a loop; follow the platform's uniqueness/nonempty assumptions |

For each, derive the algorithm independently, test boundaries, explain termination and C/D, and justify whether recursion is appropriate. No platform solutions or full statements are reproduced.

These rows record the **supplementary assessment attempt**. If you also track the same solve in the roadmap's existing category Log, keep the detailed history in one place and reference that record from the other; do not maintain two independent histories. Preserve the roadmap's existing category assignments.

- [ ] **EX1 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.
- [ ] **EX2 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.
- [ ] **EX3 attempt** — Last: —; Result: —; Time: —; Next review: —; Mistake/lesson: —.

## 12. Interview Communication and Mock Sessions

Use these questions as follow-ups to existing exercise attempts, not as a second counted exercise set. Answers should refer to your actual code and contract. Do not recite a general recursion template.

| Selected exercise | Explain in 60–90 seconds | Interviewer follow-up |
| --- | --- | --- |
| F3 | Exact suffix promise, empty base, child argument, returned contribution, termination | What would change if the start index were invalid? |
| F4 | Why the base handles negative/extreme values; what both result fields mean | Could initializing both fields to zero silently pass some tests? |
| F7 | Representation of zero and MIN; a decreasing magnitude | Which expression must be widened before arithmetic? |
| M1 | Accumulator invariant; what the loop retains; portable Java stack | Why is the tail version still vulnerable at large depth? |
| M2 | Candidate interval, discarded region proof, empty result | Is a valid midpoint enough to prove progress? |
| M4 | Divide/child promises/combine; intermediate records | How do cumulative allocation and peak memory differ? |
| M5 | Exhaustiveness, uniqueness, output size, C versus D | Do equal remaining depths mean equal full states? |
| M6 | Retained identities, detached removed nodes, returned head | Which saved node handles must a strong test inspect? |
| C4 | Work per internal call and memory along an active path | Why can two similar call trees have different time costs? |
| C7 | Actual child sizes and repeated states | Why does exponential work not imply exponential stack? |
| X1 | Why your control structure fits the resource budget | What survives when a negative balance later becomes positive? |
| X4 | Pending obligations, stages, observable order | Can an explicit stack use nonconstant space with constant Java call depth? |

A useful **execution mock** takes one T exercise and one N/D exercise: trace before running, identify a failed assumption, and propose a defensible repair. An **implementation mock** uses one F/M exercise from a blank editor with all six proof obligations. A **transfer mock** uses an unseen X problem or a genuinely changed requirement supplied by an interviewer; hide category labels and hints.

For a Medium implementation/transfer task, approximately 30–45 minutes including clarification, code, tests, and explanation is a useful practice window. Adjust to your learning stage and target format. A hint changes the attempt to H regardless of speed; unfinished work belongs under S. Do not invent an I outcome because the timer ended.

At the end, evaluate:

- Was the valid domain explicit, including null/empty and numeric bounds?
- Did every child satisfy the same contract on a smaller valid instance?
- Did the argument cover both termination and returned/mutated correctness?
- Were total calls, maximum frames, and nonconstant per-call work separated?
- Were retained arrays/strings/records and returned output classified correctly?
- Did tests target the likely failure, rather than only the displayed example?
- Could the candidate explain why an iterative or explicit-stack alternative is preferable or unnecessary?

Record evidence on that exercise's existing attempt row. Use the roadmap's [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) for broader assessment; this workbook does not create an extra dashboard.

## 13. Revision and Spaced Repetition

Follow the roadmap's [flexible review approach](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition): roughly **1, 3, 7, and 14 days** after a learning attempt, then adjust. Review sooner after a failed assumption or a hint; extend the interval after repeated cold success. Missing a date does not restart all progress.

Prioritize S, repeated H, failed base/progress reasoning, incorrect call-stack traces, wrong recurrences, and unresolved Java reference mistakes. Repair the smallest demonstrated gap, then revisit the exercise.

A cold re-solve means:

1. Close prior code, hints, traces, and the concepts guide.
2. Read only the statement and contracts.
3. Restate the recursive promise, derive the base/reduction, and implement or analyze anew.
4. Run tests **after** making your predictions; explain any correction independently.
5. Recheck costs and state ownership, then update the same attempt row's date/result/time/review/lesson fields.

For a familiar problem, a clean cold re-solve shows retention. It does not by itself demonstrate transfer. Use an unseen mixed requirement in a separate session to assess recognition. A previously opened X exercise is no longer unseen merely because its name has been forgotten.

Do not copy exercises into a separate revision log. Search the current attempt rows for H, S, or a review date. Link to one canonical external-problem history rather than maintaining parallel platform records.

## 14. Completion and Readiness Evidence

These milestones are evaluation criteria, not automatic progress and not another category dashboard. Associate evidence with the existing attempt rows and the roadmap's [Phase 0 checklist](./DSA_Interview_Roadmap.md#phase-0-foundations).

| Milestone | Evidence required without notes or hints |
| --- | --- |
| Understand execution | Explain which frame is current and which are suspended; identify independent locals versus shared referents |
| Trace calls independently | Complete one linear and one branching trace, including exact small-input C/D and return order |
| Implement fundamentals | Rebuild F3 and representative numeric, string, mutation, and digit drills from a blank editor; test bases and bounds |
| Define contracts | State all six proof items for an unseen suffix/range problem before code |
| Prove termination | Supply a valid decreasing measure on every path; identify the gaps in N1–N3 |
| Analyze recursive costs | Derive a chain, halving, full branching, and balanced-linear-combine recurrence; include C5/C6 copying |
| Debug real defects | Construct and explain witnesses for base, progress, return, shared-state, reference, recomputation, and overflow failures |
| Convert control structure | Complete M1 and explain J3/X4 state preservation with accurate auxiliary-space costs |
| Transfer to unfamiliar work | Choose and defend a method on unseen foundational variants in separate sessions, without forcing recursion |
| Prepare for later structures | Explain child-result contracts, empty bases, branching depth, mutable-state ownership, and deep-stack alternatives |

The [T/I/P/R philosophy](./DSA_Interview_Roadmap.md#four-independent-progress-gates) remains useful: explaining theory, implementing core drills, solving/cold re-solving, and transferring to unfamiliar tasks are different evidence. Do not award a gate from a document, a compiler exit code, or memorized solutions. The roadmap's official category gates retain their existing meanings and practice requirements; these supplementary drills do not replace them.

Before beginning **Trees**, be able to identify bases, define contracts, trace calls/returns, explain frames, implement linear recursion, reason about branching, derive simple recurrences, distinguish C from D, include stack/copies, recognize depth risk, debug code, and solve an unfamiliar foundational variant independently. Also satisfy the actual [Trees prerequisites](./DSA_Interview_Roadmap.md#7-trees): Binary Search, Linked List, and queue/recursion foundations. A tree's height is not always logarithmic.

**Backtracking** comes after Trees in the [roadmap](./DSA_Interview_Roadmap.md#10-backtracking). M5 prepares branching and per-call primitive state; it does not establish choose/explore/undo, pruning, duplicate control, or mutable path restoration. Those require their own later lessons. Graph and DP prerequisites also remain in place. No mandatory workbook exercise requires those advanced categories.

Use the common rubric across separate sessions. If a cold review exposes a conceptual failure, record the gap and repair it before relying on that prerequisite. Minor syntax corrections caught independently are different evidence from a missing contract or a hint-dependent design.

## 15. Delayed Execution Checkpoints

**Stop here until you have recorded your own attempt.** These are limited numeric/structural checks, not complete traces, implementations, repairs, or proof answers. They help detect an incorrect prediction; they do not establish that every required explanation is correct. Running the supplied subjects after an attempt is another way to check exact execution.

<details>
<summary>Tracing checkpoints — selected observable facts only</summary>

| Exercise / call | Checkpoint |
| --- | --- |
| T1: t1(3) | Root result 11; the base returns 2 |
| T2: t2(3) | Root result 12; C=5, D=3 |
| T3: t3([2,-1,3],0) | Root result 4; final shared array [4,3,5] |
| T4: initial x=3, R=[7], A(5), remaining=2 | Caller x remains 3; R is [8]; original A.val=7 and A.next=null; caller references still identify R/A |
| T5: t5(3,0) | Result 9; t5(0,7) returns 7 |
| T6: [8,3,2], [0,3) | Result 7; events [open,value 8,open,value 3,value 2,close,close] |

Reconstruct the missing frame/local state and order yourself. A correct last number does not verify the trace.

</details>

<details>
<summary>Cost subjects — small-run checks, without general cost answers</summary>

| Subject | Checkpoint |
| --- | --- |
| C1: n=4 | Result 12, C=5 |
| C2: n=8 | Result 4, C=D=4 |
| C3: n=3 | Result 8, C=15, D=4 |
| C4: n=8 | Result 76; C=15, D=4; 24 allocated int slots cumulatively, 14 retained simultaneously under the source model |
| C5: [2,-1,4] | Result 5; proper-tail lengths 2,1,0 |
| C6: "abca" | Result 1; nonempty proper-suffix lengths 3,2,1 |
| C7: n=5 | Result 5, C=15, D=5 |

The C4 slot counts exclude fixed-size array headers and frames; include those separately in total asymptotic space. These observations do not replace a recurrence derivation.

</details>

<details>
<summary>Selected failure witnesses — open only after constructing your own</summary>

| Subject | Original behavior demonstrating a defect |
| --- | --- |
| N1: n=1 | Arguments start 1,−1,−3,−5; the advertised base is skipped |
| N2: n2Away(1), n2Post(1) | First child receives 2 and 1, respectively |
| N3: A->B->A | Argument identities repeat; null is never reached |
| D1: n=0 | Returns 0 instead of the required 1 |
| D2: [] and [7] | Empty input throws ArrayIndexOutOfBoundsException; singleton returns 0 instead of 7 |
| D3: n=4 | Returns 4 instead of 10 |
| D4: [4], target=5, [0,1) | The same interval is revisited until stack exhaustion in an ordinary isolated JVM run |
| D5: fresh process, [1,2] then [4] then [] | Returns 3,7,7, instead of independent sums 3,4,0 |
| D6: A(1)->B(2) | Returned B reaches A and then B again; output is cyclic |
| D7: eight equal elements | Result is correct, but 40 recursive calls execute; increasing powers of two exposes the failed linear-work promise |
| D8: n=13 | Returns 1932053504 instead of 6227020800 |

A correction still needs a proof and regression tests. Fixing only the displayed witness can leave other valid inputs incorrect.

</details>

## 16. References and Validation

### Repository references

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md): [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations), [learning and validation](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), [four gates](./DSA_Interview_Roadmap.md#four-independent-progress-gates), [readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category).
- [Recursion Essential Concepts](./Recursion_Essential_Concepts.md): the primary theory reference; return there after an attempt identifies a specific gap.
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md): [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) and [space complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity).
- [Java Arrays](./Java_Array.md#4-primitive-arrays-object-arrays-and-references) and [Java Strings](./Java_String.md#indexes-length-and-traversal): bounds, references, UTF-16 units, and copying.
- [Java Lists](./Java_List_ArrayList_LinkedList.md): returned collections and copying.
- [Stack Essential Concepts](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack): explicit containers versus recursive frames.
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants): interval meaning and strict progress.
- [Linked List Essential Concepts](./Linked_List/Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal): returned-head contracts, link ownership, and stack cost.

### Java and external sources

Java 21 Deque method contracts were checked through Context7; the official ArrayDeque class reference was checked directly for its null restriction and aggregate operation costs. For an explicit stack, push/pop operate at the head, pop on an empty deque throws, and ArrayDeque rejects null entries. Its usual endpoint operations are amortized constant time. See [Deque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Deque.html) and [ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html).

The recursion guide's [JVM frame discussion](./Recursion_Essential_Concepts.md#4-java-call-stack-and-stack-frames) and [Java reference discussion](./Recursion_Essential_Concepts.md#13-java-references-and-recursive-state) provide the execution model used here. They link to the Java 21 language/JVM specifications. This workbook never assumes a fixed safe recursion depth or guaranteed tail-call elimination.

C6 uses the materialized proper-suffix copying model in OpenJDK 21, supported by [String](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/String.java) and [StringLatin1](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/StringLatin1.java). This is an implementation-specific model, not an API promise about exact allocation bytes.

The official LeetCode pages linked in Section 11 support the external problem titles, numbers, and Easy difficulty labels. Their statements are not copied into this workbook.

### Author validation

Completed **2026-10-28**. All **43 Java blocks** were assembled with the shared declarations and compiled using Java 21 with `--release 21 -Xlint:all -Werror`. The 19 starter methods represent 18 coding exercises; they compile as TODO stubs and have not been mistaken for completed learner implementations.

All **135 coding-example rows**, expanded into **148 cases**, were checked against independent mathematical or iterative reference calculations. Checks included rejection boundaries, long numeric results, UTF-16 units, unchanged input, retained/detached node identities, fresh outputs, and event order. The executable tracing, complexity, and defective-code subjects passed **11,783 runtime assertions** across **1,093 exhaustively generated small array cases** and other bounded cases. Instrumentation checked **418,260 logical invocations**, recorded **14 selected executions**, and compared call/depth and modeled allocation totals with independent recurrences. Allocation-unit checks describe the stated source model, not measured JVM heap bytes.

Six deliberately failing recursion scenarios were executed in isolated JVMs with timeouts and produced the expected StackOverflowError. Wrong values, array-bound failure, persistent state, cyclic output, recomputation, and overflow were also reproduced. The **61 local links** were checked against actual files and heading anchors. SHA-256 comparison confirmed that all **26 pre-existing files** in the guide directory remained unchanged; independently added files were left untouched. No complete coding solutions or separate solutions file were added to the repository.


