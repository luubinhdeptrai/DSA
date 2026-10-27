# Recursion — Essential Concepts (Java 21)

> A recursive call is a smaller problem with a precise promise. The caller must preserve enough state to use that promise when the call returns.

## Table of Contents

- [1. Introduction and Roadmap Scope](#1-introduction-and-roadmap-scope)
- [2. Understanding Recursion Intuitively](#2-understanding-recursion-intuitively)
- [3. Base Cases, Recursive Cases, and Termination](#3-base-cases-recursive-cases-and-termination)
- [4. Java Call Stack and Stack Frames](#4-java-call-stack-and-stack-frames)
- [5. Calls, Returns, and Execution Order](#5-calls-returns-and-execution-order)
- [6. Recursive Contracts and Correctness](#6-recursive-contracts-and-correctness)
- [7. Linear Recursion over Numbers and Sequences](#7-linear-recursion-over-numbers-and-sequences)
- [8. Tail Recursion and Accumulators](#8-tail-recursion-and-accumulators)
- [9. Multiple-Branch Recursion](#9-multiple-branch-recursion)
- [10. Divide and Conquer](#10-divide-and-conquer)
- [11. Recursion Trees and Recurrence Relations](#11-recursion-trees-and-recurrence-relations)
- [12. Time, Depth, and Complete Auxiliary Space](#12-time-depth-and-complete-auxiliary-space)
- [13. Java References and Recursive State](#13-java-references-and-recursive-state)
- [14. Recursion, Iteration, and Explicit Stacks](#14-recursion-iteration-and-explicit-stacks)
- [15. Trees, DFS, Backtracking, and DP Previews](#15-trees-dfs-backtracking-and-dp-previews)
- [16. Common Mistakes and Java Pitfalls](#16-common-mistakes-and-java-pitfalls)
- [17. Recognition and a Derivation Cheat Sheet](#17-recognition-and-a-derivation-cheat-sheet)
- [18. Conceptual Assessment and Answer Key](#18-conceptual-assessment-and-answer-key)
- [19. Key Takeaways and Foundational Readiness](#19-key-takeaways-and-foundational-readiness)
- [20. References, Validation, and Next Steps](#20-references-validation-and-next-steps)

## 1. Introduction and Roadmap Scope

Recursion Fundamentals is **supplementary Phase 0 preparation** in the [Java DSA Interview Roadmap](./DSA_Interview_Roadmap.md#phase-0-foundations). It supports later categories; it is not another official NeetCode category. This guide explains how to derive, execute, prove, and analyze recursive Java methods.

The roadmap asks you to trace parameters, base cases, returned values, and local versus shared state; distinguish total calls from maximum stack depth; and implement recursive array sum and a small branching choice enumerator. It also introduces merge sort and requires call-stack costs in complexity analysis. These are the core objectives here.

You need ordinary Java methods, conditionals, array/string bounds, references, and basic Big O. The existence of a study document does not demonstrate mastery. If you cannot predict a small call trace, use that as a diagnostic and rebuild the missing concept.

### Reuse existing references

| Available reference | Reuse | Dedicated emphasis here |
| --- | --- | --- |
| [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) | Existing recurrence examples and recursive-space vocabulary | Derive calls, depth, per-call work, and peak live memory separately |
| [Java Arrays](./Java_Array.md#4-primitive-arrays-object-arrays-and-references) | Reference assignment and copies | Pass one array with changing indices instead of copying suffixes |
| [Java Strings](./Java_String.md#8-complexity-expectations-for-dsa) | UTF-16 indexing, immutability, substring costs | State the character model and count hidden recursive copying |
| [Java List reference](./Java_List_ArrayList_LinkedList.md#7-references-copying-equality-and-null) | Aliases, mutable collections, shallow copies | Decide which recursive state is local and which objects are shared |
| [Stack concepts](./Stack/Stack_Essential_Concepts.md#4-explicit-stack-versus-recursive-call-stack) | LIFO and explicit versus call stacks | Explain pending work and preserve it when converting to iteration |
| [Binary Search concepts](./Binary_Search/Binary_Search_Essential_Concepts.md#4-exact-match-binary-search) | Ordered elimination and interval conventions | Trace a half-open recursive search without subarray copies |
| [Linked List concepts](./Linked_List/Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal) | Custom nodes and reversal invariants | Interpret node references and recursive return contracts |

The Stack, Binary Search, and Linked List guides are in subdirectories. This guide uses those discovered paths.

### Core versus optional depth

**Core:** base cases, termination measures, frames, return flow, contracts, linear/tail/branching recursion, simple divide and conquer, recursion trees, complete memory analysis, Java references, and iterative alternatives.

**Optional:** the Master Theorem as a recognition aid and tighter exponential-growth facts beyond the elementary bounds shown here. Trees, graph DFS, Backtracking, and Dynamic Programming are **previews**, with links to their actual roadmap dependencies rather than full algorithms or new practice requirements.

There is no new Recursion dashboard row to maintain. Use the roadmap's [Phase 0 drills](./DSA_Interview_Roadmap.md#phase-0-foundations), [learning strategy](./DSA_Interview_Roadmap.md#3-learning-and-validation-strategy), and existing [T/I/P/R meanings](./DSA_Interview_Roadmap.md#four-independent-progress-gates). Reading supports explanation; blank-editor implementation and unfamiliar transfer require separate evidence.

### Code and counting conventions

All Java blocks are members of one class, such as `RecursionExamples`. Add `import java.util.*;` before that class. Methods use Java 21 without external dependencies or preview features.

Unless stated otherwise:

- Arrays and strings are nonnull, with lengths in 0..100,000. Integer elements may be negative, duplicated, or use the full int range. Inputs are read-only except explicitly mutating examples.
- Strings are sequences of **UTF-16 code units**. A char-based count does not claim to count Unicode code points or user-perceived characters.
- Invalid numeric arguments explicitly rejected by a public method produce IllegalArgumentException. Internal range helpers receive valid bounds from their wrapper.
- Scalar arithmetic uses the stated fixed-width types. For the stated array bound, summing int values in long is safe. Factorial and branching examples have narrower explicit domains.
- Recursive examples assume enough runtime stack to complete. A mathematically valid finite input can still be too deep for a particular JVM. Use small linear traces/tests and iterative alternatives for large depth.
- **C** means total recursive-method/helper invocations, including base calls. **D** means the maximum number of simultaneously live invocations in that recursive family, including its root and base.
- A root-to-leaf path with D frames has D−1 call edges. A nonrecursive wrapper adds a constant number of frames, stated separately where useful. Other test/caller frames are excluded.
- “Live” or “active” in a teaching diagram means not yet returned, including suspended callers. Only one frame is current and executing at a time in ordinary synchronous execution.
- Auxiliary space excludes existing input and required returned output. It includes call frames, temporary arrays/strings, explicit stacks, maps, and other retained working objects. Count output storage separately.
- Per-call constant work assumes fixed-width operations and constant-time indexed reads. Include actual copy/concatenation/library costs when those assumptions do not apply.

These conventions make exact small traces comparable; they do not fix a physical JVM frame size or promise a universal safe recursion limit.

## 2. Understanding Recursion Intuitively

### 2.1 Delegate a smaller version of the same task

Imagine a stack of envelopes. To discover how many there are, remove the top one, ask the same question about the remaining stack, then add one to that smaller answer. An empty stack answers immediately: zero.

This is useful intuition because it identifies three obligations:

1. A case you can answer directly.
2. A smaller instance whose meaning is the same.
3. A way to use that smaller answer.

A common conceptual shape is:

```text
Solve(problem):
    if the problem is simple enough:
        return its direct answer
    smallerAnswer = Solve(a smaller instance)
    return a result built from smallerAnswer
```

This is a reasoning model, not a universal template. Some calls produce several subresults, mutate state, emit output, or continue in tail position. You must derive the actual contract.

### 2.2 Sum the first n positive integers

Let S(n) mean 1+2+...+n for nonnegative n, with S(0)=0. Remove the final term n: the remaining problem is S(n−1). Therefore S(n)=n+S(n−1).

```java
static long sumTo(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be nonnegative");
    }
    if (n == 0) {
        return 0;
    }
    return n + sumTo(n - 1);
}
```

For n=3:

```text
sumTo(3) needs 3 + sumTo(2)
sumTo(2) needs 2 + sumTo(1)
sumTo(1) needs 1 + sumTo(0)
sumTo(0) returns 0

Then: 1 + 0 = 1
      2 + 1 = 3
      3 + 3 = 6
```

Calling the same method does not restart the existing invocation or erase its n. It creates a new invocation whose parameter has a different value. The old invocation waits with its unfinished addition.

The return type is long. In `n + sumTo(n-1)`, the recursive result is long, so addition is long arithmetic. Even the mathematical sum through Integer.MAX_VALUE fits long; this numeric fact does not make that enormous recursive depth practical. A simple loop or a carefully widened formula is better for large n.

Short code can hide substantial execution. This method makes n+1 recursive-family calls and needs n+1 live frames at its deepest point: Θ(n+1) time and auxiliary stack. An iterative running total needs Θ(n+1) time and O(1) auxiliary space.

**Self-check Q1:** Which exact smaller problem does sumTo(3) ask another invocation to solve?

**Self-check Q2:** Why does a one-line recursive return not imply constant execution time or space?

## 3. Base Cases, Recursive Cases, and Termination

### 3.1 Base cases must be correct and reachable

A base case is an input whose answer is known directly. It is not merely a condition that stops the program. Returning the wrong answer there can corrupt every result built from it.

For sumTo, the empty sum is zero. For factorial, the empty product is one. For a maximum query, an empty input needs an explicit contract; zero is not a valid universal answer because all values may be negative.

A recursive case reduces a valid problem to one or more valid smaller problems. The reduction must eventually reach a base case along **every** path.

| Fault | Small example | Why the reasoning fails |
| --- | --- | --- |
| Missing base case | Read a[i] and call with i+1 forever | Eventually an invalid read can throw; no direct empty-suffix answer exists |
| Incorrect base value | Define factorial(0)=0 | Multiplication propagates zero instead of the correct product |
| No progress | f(1) calls f(1) | The same unmet obligation is recreated |
| Wrong direction | A nonnegative n with base n=0 calls f(n+1) | n is moving away from the proposed stopping measure |
| Invalid initial domain | A decrement-to-zero method accepts n=−1 without a suitable rule | Repeated decrement does not approach zero within the intended domain |
| Skipped boundary | Only stop at n=0 but subtract 2 from odd positive n | The sequence can pass the base without reaching it |

Machine integer wraparound is not a termination argument. It changes values outside the intended mathematical reasoning, may violate the domain, and usually arrives far after an infeasible amount of work or stack growth.

### 3.2 A decreasing measure

Choose a nonnegative integer that becomes strictly smaller in every recursive step:

| Problem | Measure | Decrease |
| --- | --- | --- |
| sumTo(n), n>=0 | n | n−1 |
| Suffix processing at index i | length−i | One fewer remaining position |
| Search in [lo,hi) | hi−lo | A strictly smaller interval |
| Acyclic list traversal | Number of remaining successor nodes | Follow next |
| Binary choices with r decisions left | r | Each child has r−1 |
| Balanced split of a range of length >=2 | Range length | Each child has length at most ceil(n/2)<n |

A measure proves termination only when it has a lower bound, decreases, and remains in the valid domain. For branching recursion, checking only one child is insufficient. For linked traversal, acyclicity is necessary for “remaining nodes” to decrease to zero; following next around a cycle does not satisfy that claim.

Correctness and termination are different. Returning zero immediately terminates but does not compute every requested sum. A correct mathematical recurrence can also fail operationally if the Java implementation does not reach its base.

### 3.3 Direct and indirect recursion

Direct recursion calls the same method. Indirect recursion follows a cycle of methods, such as A calling B and B calling A. The same termination obligations apply to the entire call cycle.

```java
static boolean evenByCalls(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be nonnegative");
    }
    if (n == 0) {
        return true;
    }
    return oddByCalls(n - 1);
}

static boolean oddByCalls(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be nonnegative");
    }
    if (n == 0) {
        return false;
    }
    return evenByCalls(n - 1);
}
```

`evenByCalls(3)` → `oddByCalls(2)` → `evenByCalls(1)` → `oddByCalls(0)` returns false, which propagates unchanged through all callers. The measure n decreases regardless of the method name. Counting both methods as one recursive family gives C=D=n+1, Θ(n+1) time, and Θ(n+1) stack under ordinary Java analysis. The remainder test n%2==0 is the simpler O(1) alternative; the example teaches mutual calls, not an efficient parity implementation.

**Self-check Q3:** Why does finding a base case in the source not establish termination?

**Self-check Q4:** What additional input property makes a linked-list length a valid decreasing measure?

## 4. Java Call Stack and Stack Frames

### 4.1 What a frame preserves

A logical invocation frame preserves information needed to complete one method call: its parameters, local state, partial results, and a continuation describing where work resumes. Each invocation has its own local-variable slots. Objects referenced by those slots can still be shared.

The JVM specification describes per-thread stacks and method frames, including local-variable and operand-stack state. These are abstract execution rules; implementation layout and optimizations are not fixed by the diagrams here. See [JVMS 21: frames](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.6).

In ordinary synchronous recursion, the caller suspends its execution while the callee runs. It resumes after that call completes normally, using the returned value, or unwinds abruptly if an exception/error propagates. Recursion does not run both frames in parallel.

### 4.2 sumTo(3) at maximum depth

```text
Top / current frame
+----------------------------------+
| sumTo(0): n=0; next action return 0|
+----------------------------------+
| sumTo(1): n=1; waiting for child  |
| continuation: return 1 + child    |
+----------------------------------+
| sumTo(2): n=2; waiting for child  |
| continuation: return 2 + child    |
+----------------------------------+
| sumTo(3): n=3; waiting for child  |
| continuation: return 3 + child    |
+----------------------------------+
Bottom / earliest recursive call
```

There are four live invocations, but only the top one is current. Each frame's n is independent. The deepest n=0 does not overwrite the suspended n=3. A base case is still an actual call; it has a frame until it returns.

| Depth in this recursive family | Active method | Local state | Next action |
| --- | --- | --- | --- |
| 1 | sumTo(3) | n=3 | Wait for sumTo(2), then add 3 |
| 2 | sumTo(2) | n=2 | Wait for sumTo(1), then add 2 |
| 3 | sumTo(1) | n=1 | Wait for sumTo(0), then add 1 |
| 4 | sumTo(0) | n=0 | Return 0 directly |

After sumTo(0) returns, its frame is no longer needed. sumTo(1) becomes current, receives zero, computes one, and returns. Frames complete in last-in-first-out order, which is why the [Stack guide's LIFO model](./Stack/Stack_Essential_Concepts.md#2-stack-fundamentals-and-lifo) is relevant.

### 4.3 Three phases

- **Calling/descent:** create smaller obligations and preserve pending work.
- **Base-case execution:** answer the smallest obligation directly.
- **Returning/unwinding:** resume suspended callers and finish their remaining work.

A frame's return does not restart its caller at the first line. It resumes at the continuation after the call. Returning also does not destroy a referenced array/node that remains reachable elsewhere; frame lifetime and object lifetime are different questions.

### 4.4 Finite recursion can still be too deep

A correct decreasing measure guarantees a finite number of mathematical calls, not unlimited machine resources. Deep nesting can require more stack than the runtime permits. Java defines [StackOverflowError](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StackOverflowError.html) for excessively deep recursion. It is an Error, not an ordinary Exception subclass.

There is no portable fixed safe depth. It depends on runtime configuration, the methods' state, other live callers, and implementation choices. Increasing available stack would not fix a missing progress rule or change a linear-stack algorithm into a constant-space one.

Treat an overflow as a reason to inspect termination and depth, then choose an appropriate iterative or bounded-depth design. It is not a normal recursive base case or a mechanism for finding the answer. Later, explicit stacks can represent pending work without growing Java invocation depth, but their storage still counts.

**Self-check Q5:** At the deepest point of sumTo(3), which frames are live and which one is current?

**Self-check Q6:** Why can terminating recursion still produce StackOverflowError on a valid input?

## 5. Calls, Returns, and Execution Order

### 5.1 Return values flow one call at a time

The recursive expression is not solved by instantly expanding an infinite formula. It is ordinary method execution:

| Call | Arguments | Base case? | Recursive call | Return value |
| --- | --- | --- | --- | --- |
| sumTo(3) | n=3 | No | sumTo(2) | 6 |
| sumTo(2) | n=2 | No | sumTo(1) | 3 |
| sumTo(1) | n=1 | No | sumTo(0) | 1 |
| sumTo(0) | n=0 | Yes | None | 0 |

This table lists calls in descent order and their eventual results. The actual **completion order** is the reverse:

| Completed call | Child result received | Work performed now | Value returned to caller |
| --- | --- | --- | --- |
| sumTo(0) | None | Base answer | 0 |
| sumTo(1) | 0 | 1+0 | 1 |
| sumTo(2) | 1 | 2+1 | 3 |
| sumTo(3) | 3 | 3+3 | 6 |

The call site uses the returned value. If a method calls sumTo(n−1) and then simply returns n, it has performed recursion but discarded the smaller answer.

### 5.2 Work before and after a recursive call

A small event recorder makes the order visible. Domain: 0<=n<=30. It returns a fresh list of diagnostic strings; no input is mutated.

```java
static List<String> callReturnTrace(int n) {
    if (n < 0 || n > 30) {
        throw new IllegalArgumentException("trace depth must be in 0..30");
    }
    List<String> events = new ArrayList<>();
    traceFrom(n, events);
    return events;
}

private static void traceFrom(int n, List<String> events) {
    events.add("enter " + n);
    if (n == 0) {
        events.add("base");
    } else {
        traceFrom(n - 1, events);
    }
    events.add("exit " + n);
}
```

For n=2:

| Step | Event recorded | Live traceFrom frames when recorded |
| --- | --- | --- |
| 1 | enter 2 | 1 |
| 2 | enter 1 | 2 |
| 3 | enter 0 | 3 |
| 4 | base | 3 |
| 5 | exit 0 | 3 |
| 6 | exit 1 | 2 |
| 7 | exit 2 | 1 |

An exit event is appended **before** that invocation returns, so its frame is still live at that event. There are n+1 enter events, one base event, and n+1 exit events: 2n+3 strings. Helper calls and maximum helper frames both equal n+1; the wrapper adds one frame. Time, returned diagnostic output, and stack are each O(n+1) under the bounded fixed-width text model. ArrayList appends have amortized rather than guaranteed per-append constant cost.

Printing instead of recording would keep the same call/return order, but console work can dominate runtime. Use event traces as a learning tool; do not silently include repeated full-state printing in an algorithm's claimed constant work per call.

### 5.3 Multiple calls execute in sequence

For ordinary Java code that computes a left child result, then a right child result, the entire left call returns before the right call starts. The parent keeps the left result while executing the right call. This is why a branching call tree does not imply that all its frames coexist.

Argument and expression evaluation also matter. In `n + sumTo(n-1)`, the current n contributes to the pending expression; the smaller call receives a separately computed argument value. Trace actual Java execution, not an imagined concurrent expansion of every branch.

**Self-check Q7:** Why are the exit events for n=2 recorded in order 0,1,2?

**Self-check Q8:** If a method has two recursive call statements, why might the two child stacks not coexist?

## 6. Recursive Contracts and Correctness

### 6.1 Make the promise precise

A recursive contract defines valid inputs, the returned meaning, permitted mutations, and relevant exceptional cases.

For sumTo:

- **Precondition:** n is a nonnegative int; stack resources are sufficient.
- **Result:** the long value 1+...+n, with zero for n=0.
- **Mutation:** none.
- **Rejection:** a negative n produces IllegalArgumentException.

“Returns the answer” is too vague to guide implementation. “sumFrom(a,i) returns the sum of a[i..length)” gives a precise subproblem and tells the caller what it may combine.

A useful derivation sequence is:

```text
Problem
  -> smallest directly solvable case
  -> recursive promise
  -> valid smaller instances
  -> relationship between their answers
  -> combination and mutation rules
  -> termination measure
  -> Java implementation
  -> call/return trace
  -> correctness and complete costs
```

### 6.2 Trust a smaller promise for a reason

When deriving sumTo(n), temporarily treat sumTo(n−1) as satisfying its stated contract. This is not blind faith in arbitrary code. The base and the strictly smaller argument let you build a ladder of justification:

1. S(0)=0 is correct directly.
2. If the method correctly returns S(k), adding k+1 correctly returns S(k+1).
3. The implementation always decreases n until the directly correct case.

Therefore correctness extends from the smallest case through every valid finite n. This is an induction-style argument expressed as a method contract.

Proving termination separately matters. A hypothetical call to sumTo(n) from itself could promise the desired result, but there would be no smaller case on which to base the argument.

### 6.3 Recursive invariants are not only loop invariants

For an index helper, an invariant may be `0<=i<=length` and “this invocation is responsible exactly for the suffix beginning at i.” For an accumulator, the invariant relates already processed work to the remaining answer. For a mutating list routine, it must also describe which nodes remain reachable and what the returned head represents.

Prove the promise at the **return boundary**, not at every intermediate statement. Some implementations temporarily change structure before restoring it. You must account for those intermediate states, but the completed-call postcondition is the object used in the induction.

| Proof obligation | Question to answer |
| --- | --- |
| Domain | Are all child arguments valid? |
| Base correctness | Does the direct answer match the mathematical/structural definition? |
| Progress | What strictly decreases on every path? |
| Combination | Does the caller turn correct child results into its promised result? |
| State | Which objects are shared or changed, and what must be restored? |
| Termination interpretation | Why does reaching the base finish every remaining obligation? |

A small trace is evidence and a debugging aid, not a proof for all inputs. A proof without a correct Java guard can still describe a different program. Use both.

**Self-check Q9:** Why can you rely on a smaller recursive call's contract while deriving the current call?

**Self-check Q10:** What fails if the recursive call's result is correct but the caller combines it incorrectly?

## 7. Linear Recursion over Numbers and Sequences

“Linear recursion” here means at most one recursive child per invocation. The examples below remove one item and therefore also take linear time. A one-child method that halves its input can instead take logarithmic time; shape and running time are separate descriptions.

### 7.1 Factorial and numeric domains

Factorial multiplies the integers 1 through n, with 0!=1. Removing the final factor gives n!=n·(n−1)!. A recursive call promises the product through n−1; multiplying by n adds exactly the missing factor.

This implementation accepts **0..20** and rejects other ints. It returns long: 20! is 2,432,902,008,176,640,000, while 21! exceeds Long.MAX_VALUE. With int, only factorials through 12 fit. Choosing a larger return type does not remove the need to bound the domain.

```java
static long factorialLong(int n) {
    if (n < 0 || n > 20) {
        throw new IllegalArgumentException("n must be in 0..20");
    }
    if (n == 0) {
        return 1;
    }
    return n * factorialLong(n - 1);
}
```

The recursive result is long, so multiplication is performed in long arithmetic. Every intermediate result fits under the bound.

| Call | Base case? | Smaller result | Combination | Returned value |
| --- | --- | --- | --- | --- |
| factorialLong(4) | No | 6 | 4·6 | 24 |
| factorialLong(3) | No | 2 | 3·2 | 6 |
| factorialLong(2) | No | 1 | 2·1 | 2 |
| factorialLong(1) | No | 1 | 1·1 | 1 |
| factorialLong(0) | Yes | None | Empty product | 1 |

The completion order runs upward from n=0. Base correctness and the product relationship establish correctness by induction; n decreases to zero. There are n+1 calls and maximum frames, constant work per call, Θ(n+1) time and stack, and scalar output. An iterative product preserves the same mathematical relationship with one accumulator and O(1) auxiliary space; Section 8 gives the comparison.

The growth analysis describes the algorithm's dependence on n; the fixed-width numeric domain limits which values this implementation accepts. Do not remove the upper guard and assume larger products remain exact.

### 7.2 Recurse over indices in one array

An array problem need not create a smaller array object. A suffix is identified by the original array reference and a starting index.

| Method | Recursive promise | Direct case |
| --- | --- | --- |
| arraySumFrom(a,i) | Sum a[i..length) as long | Empty suffix returns 0 |
| countAboveFrom(a,i,t) | Number of suffix elements strictly greater than t | Empty suffix returns 0 |
| maxLinearFrom(a,i) | Maximum in the nonempty suffix a[i..length) | The last element is its own maximum |

The public sum/count methods allow an empty array. Maximum rejects empty input because its result is an int value, with no missing-result representation. Values are arbitrary ints, and every method leaves the array unchanged.

```java
static long arraySum(int[] values) {
    return arraySumFrom(values, 0);
}

private static long arraySumFrom(int[] values, int from) {
    if (from == values.length) {
        return 0;
    }
    return (long) values[from] + arraySumFrom(values, from + 1);
}

static int countAbove(int[] values, int threshold) {
    return countAboveFrom(values, 0, threshold);
}

private static int countAboveFrom(int[] values, int from, int threshold) {
    if (from == values.length) {
        return 0;
    }
    int rest = countAboveFrom(values, from + 1, threshold);
    return rest + (values[from] > threshold ? 1 : 0);
}

static int maxLinear(int[] values) {
    if (values.length == 0) {
        throw new IllegalArgumentException("maximum requires a nonempty array");
    }
    return maxLinearFrom(values, 0);
}

private static int maxLinearFrom(int[] values, int from) {
    if (from == values.length - 1) {
        return values[from];
    }
    int restMaximum = maxLinearFrom(values, from + 1);
    return Math.max(values[from], restMaximum);
}
```

No suffix array is allocated. Each helper receives a copied reference to the **same array** and a new int index.

For values=[4,−1,7], the sum's calling chain uses indices 0→1→2→3. At index 3, the remaining range is empty. Return flow is:

| from | Remaining values | Child answer | Current contribution | Returned sum |
| --- | --- | --- | --- | --- |
| 3 | [] | None | Base | 0 |
| 2 | [7] | 0 | 7 | 7 |
| 1 | [−1,7] | 7 | −1 | 6 |
| 0 | [4,−1,7] | 6 | 4 | 10 |

Counting values strictly above 3 uses the same suffix structure:

| from | Value or base | Suffix count received | Current match | Returned count |
| --- | --- | --- | --- | --- |
| 3 | Empty suffix | None | None | 0 |
| 2 | 7 | 0 | 1 | 1 |
| 1 | −1 | 1 | 0 | 1 |
| 0 | 4 | 1 | 1 | 2 |

Maximum uses a nonempty direct case:

| from | Value | Suffix maximum received | Returned maximum |
| --- | --- | --- | --- |
| 2 | 7 | None | 7 |
| 1 | −1 | 7 | 7 |
| 0 | 4 | 7 | 7 |

For each method, a correct child answer summarizes exactly the suffix after the current position. Adding the current value, adding its match indicator, or comparing it with the suffix maximum gives the promised full suffix answer. The measure length−from decreases; maximum stops at length−from=1.

For n elements, sum/count have C=D=n+1 helper calls/frames; nonempty maximum has C=D=n. Each uses Θ(n+1) time, Θ(n+1) stack, O(1) scalar output, and no new arrays. Public wrappers add one frame. An iterative scan has the same Θ(n+1) time and O(1) auxiliary space.

| Operation | Input | Result |
| --- | --- | --- |
| arraySum | [] | 0 |
| arraySum | [MAX,MAX] | 4294967294 |
| countAbove, threshold=3 | [4,−1,7] | 2 |
| countAbove, threshold=0 | [] | 0 |
| maxLinear | [−8,−3] | −3 |
| maxLinear | [MIN] | MIN |
| maxLinear | [] | IllegalArgumentException |

Initializing a maximum to zero would incorrectly accept a value absent from an all-negative array. A correct base must match the result's meaning.

### 7.3 Process a string without copying it

To count a chosen UTF-16 code unit, keep the string unchanged and move an index. Contract: nonnull text, case-sensitive exact char equality, empty result zero. The method counts code units, not code points or grapheme clusters.

```java
static int countUnit(String text, char wanted) {
    return countUnitFrom(text, 0, wanted);
}

private static int countUnitFrom(String text, int from, char wanted) {
    if (from == text.length()) {
        return 0;
    }
    int rest = countUnitFrom(text, from + 1, wanted);
    return rest + (text.charAt(from) == wanted ? 1 : 0);
}
```

The helper promises a count in text[from..length). The empty suffix has zero matches; otherwise the suffix count plus the current match indicator gives the answer. The remaining code-unit count decreases each call.

For text="aba", wanted='a':

| from on return | Current unit/base | Child count | Returned count |
| --- | --- | --- | --- |
| 3 | Empty | None | 0 |
| 2 | a | 0 | 1 |
| 1 | b | 1 | 1 |
| 0 | a | 1 | 2 |

There are n+1 helper calls and frames for n code units. Time and auxiliary stack are Θ(n+1); scalar output and non-frame working state are O(1). No new substring is created. A loop achieves the same count in Θ(n+1) time/O(1) auxiliary space.

For the Java string literal `"\uD83D\uDE00"`, length is 2 even though it represents one supplementary code point. Counting `'\uD83D'` returns 1. Counting an actual Unicode code point would require a different traversal contract. See [the string reference](./Java_String.md#indexes-length-and-traversal).

### 7.4 Follow an acyclic linked structure

A node's next reference can identify the smaller suffix directly:

```java
static final class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

static int countLinked(ListNode head) {
    if (head == null) {
        return 0;
    }
    return 1 + countLinked(head.next);
}
```

Contract: an acyclic, null-terminated chain with at most 100,000 nodes; null is empty. Count existing node identities regardless of duplicate values. Do not change links/values or allocate data nodes.

```text
A(7) -> B(7) -> C(-2) -> null

countLinked(A) waits for 1 + countLinked(B)
countLinked(B) waits for 1 + countLinked(C)
countLinked(C) waits for 1 + countLinked(null)

Return: null->0, C->1, B->2, A->3
```

The null suffix contributes zero; adding the current node counts each successor position once. Acyclicity supplies progress toward null. C=D=n+1, Θ(n+1) time/stack, scalar output, and no new nodes. An iterative cursor uses O(1) auxiliary space.

If C.next were B, this contract would be violated: no call reaches null. A recursive counter is not cycle detection. Structural mutation such as recursive reversal needs a stronger contract about rewired links and the returned head; use [the existing linked-list derivation](./Linked_List/Linked_List_Essential_Concepts.md#7-iterative-and-recursive-reversal) rather than reusing a counter's proof.

**Self-check Q11:** Why is factorial's zero base one rather than zero?

**Self-check Q12:** Why do sum/count allow an empty array while maxLinear rejects it?

**Self-check Q13:** What does countUnit count when a displayed symbol occupies two UTF-16 code units?

**Self-check Q14:** What part of countLinked's termination argument fails on a cycle?

## 8. Tail Recursion and Accumulators

### 8.1 Move pending work into an argument

In factorialLong(n), multiplication remains after the smaller factorial returns. That call is not in tail position.

An accumulator can hold the product already processed. Let factorialState(remaining,accumulator) return accumulator·remaining!. The next state has remaining−1 and accumulator·remaining.

```java
static long factorialTail(int n) {
    if (n < 0 || n > 20) {
        throw new IllegalArgumentException("n must be in 0..20");
    }
    return factorialState(n, 1);
}

private static long factorialState(int remaining, long accumulator) {
    if (remaining == 0) {
        return accumulator;
    }
    return factorialState(remaining - 1, accumulator * remaining);
}

static long factorialLoop(int n) {
    if (n < 0 || n > 20) {
        throw new IllegalArgumentException("n must be in 0..20");
    }
    long product = 1;
    for (int factor = 2; factor <= n; factor++) {
        product *= factor;
    }
    return product;
}
```

The multiplication that updates the accumulator happens **before** the recursive call. After that call returns, factorialState only returns the same value; it has no remaining combination work. This is tail recursion.

For factorialTail(4):

| Helper call | remaining | accumulator | Next state / direct return |
| --- | --- | --- | --- |
| 1 | 4 | 1 | (3,4) |
| 2 | 3 | 4 | (2,12) |
| 3 | 2 | 12 | (1,24) |
| 4 | 1 | 24 | (0,24) |
| 5 | 0 | 24 | Return 24 |

Every waiting helper subsequently returns 24 unchanged. By contrast, standard factorial returns 1,1,2,6,24 while unwinding from its zero base.

### 8.2 Accumulator invariant and correctness

For the original argument N, maintain:

```text
accumulator * remaining! = N!
```

Initially 1·N!=N!. Updating to accumulator·remaining and remaining−1 preserves the equation because remaining!=(remaining)·(remaining−1)!. At remaining=0, multiplying by 0!=1 changes nothing, so accumulator=N!. The measure remaining decreases and the 0..20 wrapper bound keeps products in long.

The helper itself is private because arbitrary accumulator values would require a different numeric-overflow contract. The wrapper establishes the valid initial state.

### 8.3 Java does not promise constant stack for tail calls

Tail position describes the algorithm's continuation. It does not promise that a Java implementation eliminates the caller frame. Portable Java 21 reasoning must account for the full recursive depth.

| Version | Combination timing | Time | Maximum relevant frames | Auxiliary space |
| --- | --- | --- | --- | --- |
| factorialLong | Multiply after child returns | Θ(n+1) | n+1 recursive calls | Θ(n+1) stack |
| factorialTail | Update accumulator before child | Θ(n+1) | n+1 helper calls, plus wrapper | Θ(n+1) stack |
| factorialLoop | Update one product in a loop | Θ(n+1) | O(1) method depth | O(1) |

Java SE does not guarantee tail-call optimization. Do not change the tail version's space bound to O(1) because other languages or a particular compiler may optimize tail calls. The [JVM frame model](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.6) gives no portable constant-space guarantee for this source-level rewrite.

Tail-recursive state is often straightforward to turn into loop variables: keep the same state meaning and replace the child call with updates. Preserve the dependency between updates; computing a new argument from a value already overwritten can change the algorithm.

A return expression such as `return n * f(n-1);` is not tail recursion just because it uses the keyword return. Multiplication still follows the child. Conversely, the branch returns in recursive binary search are tail-position calls, but their Java stack remains logarithmic in the worst case.

**Self-check Q15:** Why is the accumulator multiplication not work that remains after the recursive call?

**Self-check Q16:** Which part of the tail-factorial argument is mathematical, and which space conclusion is Java-specific?

## 9. Multiple-Branch Recursion

### 9.1 A decision tree with no overlapping full states

For b binary choices, each decision can be 0 or 1. A root with b decisions remaining has two children with b−1 decisions remaining. Store the chosen prefix as a bounded integer code.

This small Phase 0 enumerator returns numeric codes for all b-bit words, including leading zeros. Domain **0..12** bounds output and integer arithmetic. At b=0 there is one empty word, represented by code 0, so the result is [0], not an empty list.

```java
static List<Integer> choiceCodes(int bits) {
    if (bits < 0 || bits > 12) {
        throw new IllegalArgumentException("bits must be in 0..12");
    }
    List<Integer> output = new ArrayList<>();
    emitCodes(bits, 0, output);
    return output;
}

private static void emitCodes(int remaining, int prefix, List<Integer> output) {
    if (remaining == 0) {
        output.add(prefix);
        return;
    }
    emitCodes(remaining - 1, prefix * 2, output);
    emitCodes(remaining - 1, prefix * 2 + 1, output);
}
```

The helper promises to append all completions of its prefix, with the 0-child's completions before the 1-child's. At zero remaining decisions, prefix is a complete code. Otherwise, completions split into two disjoint groups according to the next decision. Both children decrease remaining, so all paths finish.

For bits=2:

```text
emitCodes(2,0)
+-- emitCodes(1,0)
|   +-- emitCodes(0,0) -> append 0   [00]
|   +-- emitCodes(0,1) -> append 1   [01]
+-- emitCodes(1,1)
    +-- emitCodes(0,2) -> append 2   [10]
    +-- emitCodes(0,3) -> append 3   [11]
```

| Helper entry order | remaining | prefix | Action at this node |
| --- | --- | --- | --- |
| 1 | 2 | 0 | Explore two children |
| 2 | 1 | 0 | Explore two children |
| 3 | 0 | 0 | Append 0 |
| 4 | 0 | 1 | Append 1 |
| 5 | 1 | 1 | Explore two children |
| 6 | 0 | 2 | Append 2 |
| 7 | 0 | 3 | Append 3 |

There are seven helper calls, four leaves, and a maximum of three helper frames. The first complete child subtree finishes before the second begins. Prefix is a primitive local value per invocation; output is one intentionally shared collection.

In general there are 2^b leaves and 1+2+...+2^b=2^(b+1)−1 helper calls. The maximum helper depth is b+1, plus the wrapper. Time is Θ(2^b) with amortized output appends, auxiliary stack O(b+1), and returned output O(2^b). Each emitted value has bounded fixed-width size. Emitting copied strings of length b instead would add output construction/storage proportional to b·2^b.

ArrayList's aggregate append bound is documented in the [Java 21 API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html). An occasional resize does not make every append a separate linear scan.

### 9.2 Branching does not automatically mean repeated subproblems

In choiceCodes, the full state is (remaining,prefix). Different branches represent different prefixes even when the number of remaining decisions matches. Branching alone does not establish overlap.

Naive Fibonacci does repeat the **same** full state. Let F(0)=0, F(1)=1, and F(n)=F(n−1)+F(n−2).

For this deliberately simple teaching implementation, accept **0..30**. This limit keeps experiments manageable; it is not the maximum representable Fibonacci index for long. Repeated evaluation becomes expensive well before numeric range is exhausted.

```java
static long fibNaive(int n) {
    if (n < 0 || n > 30) {
        throw new IllegalArgumentException("teaching domain is 0..30");
    }
    if (n <= 1) {
        return n;
    }
    long left = fibNaive(n - 1);
    long right = fibNaive(n - 2);
    return left + right;
}
```

The recursive contract is F(n); both smaller arguments are valid for n>=2. The base definitions are direct, the recurrence combines the two required predecessor values, and n decreases on both paths.

### 9.3 fibNaive(4): nine calls, four live frames at peak

```text
fib(4) = 3
+-- fib(3) = 2
|   +-- fib(2) = 1
|   |   +-- fib(1) = 1
|   |   +-- fib(0) = 0
|   +-- fib(1) = 1
+-- fib(2) = 1           same n=2 as an earlier call
    +-- fib(1) = 1
    +-- fib(0) = 0
```

| Call instance | Argument n | Child results in execution order | Returned value |
| --- | --- | --- | --- |
| Root | 4 | 2,1 | 3 |
| Root's left child | 3 | 1,1 | 2 |
| Left-left child | 2 | 1,0 | 1 |
| Left-left-left leaf | 1 | None | 1 |
| Left-left-right leaf | 0 | None | 0 |
| Left-right leaf | 1 | None | 1 |
| Root's right child | 2 | 1,0 | 1 |
| Right-left leaf | 1 | None | 1 |
| Right-right leaf | 0 | None | 0 |

The table lists call instances in entry order with their eventual results. When the first fib(1) runs, the live chain is fib(4)→fib(3)→fib(2)→fib(1): four frames. The root's right subtree has not been entered. It is not another simultaneously allocated tree of frames.

| n | F(n) | Total calls C(n) | Maximum frames D(n) |
| --- | --- | --- | --- |
| 0 | 0 | 1 | 1 |
| 1 | 1 | 1 | 1 |
| 2 | 1 | 3 | 2 |
| 3 | 2 | 5 | 3 |
| 4 | 3 | 9 | 4 |
| 5 | 5 | 15 | 5 |
| 6 | 8 | 25 | 6 |

C(0)=C(1)=1 and C(n)=1+C(n−1)+C(n−2). In fact C(n)=2F(n+1)−1. D(n)=n for n>=1, with D(0)=1.

The actual time recurrence has **n−1 and n−2**, not two identical n−1 children. Time is exponential; O(2^n) is a valid loose upper bound. The tighter Θ(φ^n), with φ=(1+sqrt(5))/2, is optional mathematical depth. You can understand the repeated work without deriving that closed form.

The stack uses O(n+1), because a single sequential branch reaches linear depth and every frame has constant local state. Exponential total calls do not imply exponential simultaneous stack. Caching repeated n states motivates the later DP preview; a full memoized Fibonacci implementation is outside this foundational example.

**Self-check Q17:** Why does the bits=2 enumerator have seven calls but at most three helper frames?

**Self-check Q18:** What is the full state that prevents choiceCodes' sibling prefixes from being the same subproblem?

**Self-check Q19:** Why is 2T(n−1)+O(1) not the exact recurrence for the Fibonacci code?

## 10. Divide and Conquer

A decomposition can reduce the input much faster than removing one item. The key questions remain: what do children promise, which children are needed, and what combination work occurs?

Binary search keeps one half; stricter terminology calls it **decrease and conquer**. It is included here to contrast one halving child with two halving children. Balanced maximum and merge sort solve both halves, but their different combination costs produce different time bounds.

### 10.1 Recursive binary search without copies

Contract: a nonnull int array sorted in nondecreasing order; return an index containing target, or −1 if absent. Any matching index is acceptable when duplicates exist. Empty input returns −1. Input is unchanged; sortedness is a precondition, not rechecked on each call.

The helper promises to search only [lo,hi). An empty interval is directly absent. Otherwise a comparison decides whether to return mid or retain one strictly smaller interval.

```java
static int binarySearchRec(int[] values, int target) {
    return searchRange(values, target, 0, values.length);
}

private static int searchRange(int[] values, int target, int lo, int hi) {
    if (lo == hi) {
        return -1;
    }
    int mid = lo + (hi - lo) / 2;
    if (values[mid] == target) {
        return mid;
    }
    if (values[mid] < target) {
        return searchRange(values, target, mid + 1, hi);
    }
    return searchRange(values, target, lo, mid);
}
```

The array reference is shared; the range shrinks. Returning the child's result propagates the selected answer unchanged.

For values=[1,4,7,9,12,15,18], target=7:

| Call | lo | hi | mid | values[mid] | Next action / result |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | 7 | 3 | 9 | Search [0,3) |
| 2 | 0 | 3 | 1 | 4 | Search [2,3) |
| 3 | 2 | 3 | 2 | 7 | Return 2 |

For target=8:

| Call | lo | hi | mid | values[mid] | Next action / result |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | 7 | 3 | 9 | Search [0,3) |
| 2 | 0 | 3 | 1 | 4 | Search [2,3) |
| 3 | 2 | 3 | 2 | 7 | Search [3,3) |
| 4 | 3 | 3 | Not evaluated | Not evaluated | Return −1 |

Sortedness proves that the discarded values cannot match. If values[mid]<target, every position through mid can be excluded; otherwise the greater mid and its right side can be excluded. The retained interval length decreases. The empty base therefore proves absence only after all possible positions have been safely excluded.

Worst-case time and stack are O(log(n+1)+1), best-case time O(1). Total recursive calls equal depth along this one-child search. For n>=1, a worst-case miss uses floor(log2(n))+2 helper calls including its final empty call; a successful lookup may stop sooner. No subarrays or result arrays are allocated. An iterative search has the same worst-case time and O(1) auxiliary space.

For full binary-search interval reasoning, reuse the [dedicated guide](./Binary_Search/Binary_Search_Essential_Concepts.md#3-search-intervals-and-loop-invariants).

### 10.2 Balanced maximum: two children, linear total work

Contract: a nonempty, read-only int array. Return its maximum, including all-negative/extreme inputs. Empty input throws IllegalArgumentException. Split a nonempty range into two nonempty halves until one element remains.

```java
static int maxByHalves(int[] values) {
    if (values.length == 0) {
        throw new IllegalArgumentException("maximum requires a nonempty array");
    }
    return maxRange(values, 0, values.length);
}

private static int maxRange(int[] values, int lo, int hi) {
    if (hi - lo == 1) {
        return values[lo];
    }
    int mid = lo + (hi - lo) / 2;
    int leftMaximum = maxRange(values, lo, mid);
    int rightMaximum = maxRange(values, mid, hi);
    return Math.max(leftMaximum, rightMaximum);
}
```

The maximum of two disjoint nonempty halves is the maximum of their maxima. That recursive contract supplies the combination rule; length one supplies the base. Child lengths are strictly smaller, so termination follows.

For [−4,7,7,2]:

```text
max[0,4) = 7
+-- max[0,2) = 7
|   +-- max[0,1) = -4
|   +-- max[1,2) = 7
+-- max[2,4) = 7
    +-- max[2,3) = 7
    +-- max[3,4) = 2
```

| Completed range | Child results | Returned maximum |
| --- | --- | --- |
| [0,1) | None | −4 |
| [1,2) | None | 7 |
| [0,2) | −4,7 | 7 |
| [2,3) | None | 7 |
| [3,4) | None | 2 |
| [2,4) | 7,2 | 7 |
| [0,4) | 7,7 | 7 |

There are n leaves and n−1 internal nodes: C=2n−1 for n>=1. Constant work at each node gives Θ(n) time, while the longest balanced path has D=ceil(log2(n))+1 frames and Θ(log(n+1)) stack. No new arrays are created. The left subtree's frames have returned before the right subtree runs; the parent retains only leftMaximum.

A simple iterative maximum still uses less space, O(1), with Θ(n) time. This example shows that **two recursive calls do not automatically imply exponential time or n log n time**. Input shrinkage and per-call work decide the result.

### 10.3 Merge sort: linear combination at each level

Merge sort divides the array into two ranges, sorts both, and combines two sorted ranges into one sorted range. A length-zero/one range is already sorted. Unlike maximum's constant-size comparison, merging a range of length k takes Θ(k) work.

This teaching implementation sorts the original int[] in nondecreasing order. Empty/singleton arrays are allowed; negative and duplicate/extreme values are valid. Allocate one shared temporary array of length n and no subarray copies.

```java
static void mergeSort(int[] values) {
    int[] buffer = new int[values.length];
    mergeSortRange(values, buffer, 0, values.length);
}

private static void mergeSortRange(int[] values, int[] buffer, int lo, int hi) {
    if (hi - lo < 2) {
        return;
    }
    int mid = lo + (hi - lo) / 2;
    mergeSortRange(values, buffer, lo, mid);
    mergeSortRange(values, buffer, mid, hi);

    int left = lo;
    int right = mid;
    int write = lo;
    while (left < mid && right < hi) {
        if (values[left] <= values[right]) {
            buffer[write++] = values[left++];
        } else {
            buffer[write++] = values[right++];
        }
    }
    while (left < mid) {
        buffer[write++] = values[left++];
    }
    while (right < hi) {
        buffer[write++] = values[right++];
    }
    for (int i = lo; i < hi; i++) {
        values[i] = buffer[i];
    }
}
```

For values=[6,1,4,1], nontrivial ranges complete as follows:

| Completed range | Sorted child ranges | Merged range | Entire values array afterward |
| --- | --- | --- | --- |
| [0,2) | [6] and [1] | [1,6] | [1,6,4,1] |
| [2,4) | [4] and [1] | [1,4] | [1,6,1,4] |
| [0,4) | [1,6] and [1,4] | [1,1,4,6] | [1,1,4,6] |

**Recursive contract:** after mergeSortRange returns, values[lo..hi) is a sorted permutation of its original contents and positions outside the range are unchanged.

**Correctness:** singleton/empty ranges satisfy the contract. The two smaller calls establish sorted child ranges. During merging, buffer[lo..write) is the sorted merge of the consumed child prefixes; each comparison selects a smallest remaining value. The remaining suffix of either child is already sorted. Copying the completed buffer range back establishes the parent contract. Input is not overwritten while unread child values are still needed. Preferring the left on ties provides the usual stable merge choice.

**Termination:** each recursive range of size at least two has smaller nonempty children. **Time:** T(n)=T(floor(n/2))+T(ceil(n/2))+Θ(n), giving Θ(n log n) for n>=2. Empty/singleton cases take constant work; the one buffer allocation contributes O(n+1) setup.

**Depth and storage:** D=ceil(log2(n))+1 for nonempty n, or one helper frame for empty input. The recursive stack is O(log(n+1)+1), but the buffer is O(n+1), so total auxiliary space is **O(n+1)**. The mutated input is not a separately allocated output array. For n>=1 there are 2n−1 recursive range calls, yet total time is n log n because internal calls perform nonconstant merge work.

Balanced maximum and merge sort can have the same call-tree shape and different time complexities. The difference is the work performed at each node.

**Self-check Q20:** Why does the binary-search method not recursively search both halves after a mismatch?

**Self-check Q21:** Why does balanced maximum use 2n−1 calls but only logarithmic simultaneous depth?

**Self-check Q22:** Why is O(log n) an incomplete auxiliary-space answer for this merge sort?

## 11. Recursion Trees and Recurrence Relations

A recurrence describes a particular implementation, not a label attached to every recursive method. Start by identifying the child sizes and the work outside those children.

### 11.1 Separate result recurrences from cost recurrences

For factorial, the **answer** satisfies F(n)=n·F(n−1). Its running time does not: multiplying two fixed-width values is constant work, so T(n)=T(n−1)+Θ(1).

For Fibonacci, the answer satisfies F(n)=F(n−1)+F(n−2), while total calls satisfy C(n)=1+C(n−1)+C(n−2). The added one counts the current call. Always specify what your recurrence measures.

| Symbol | What it measures | Base convention |
| --- | --- | --- |
| Result, such as S(n) or F(n) | Mathematical value returned | Problem-specific value |
| C(n) | Total invocations over the execution | A direct base invocation counts as 1 |
| T(n) | Work performed, including all calls | Direct base takes Θ(1) |
| D(n) | Maximum simultaneous recursive-family frames | A direct base has depth 1 |
| S_aux(n) | Peak additional live working storage | Include relevant frames/objects |

The same mathematical result can have several algorithms and several cost recurrences. A loop or formula for a sum does not inherit the recursive method's stack behavior.

### 11.2 Derive by following levels or a single path

For a decreasing chain, the input sizes are n,n−1,...,0. Constant work at n+1 calls sums to Θ(n+1). All calls are nested before the base returns, so depth is n+1.

For a halving chain, sizes are roughly n,n/2,n/4,...,1. After k halvings, the size is about n/2^k. Reaching one requires k around log2(n). With constant work at each call, both total work and depth are logarithmic.

For a complete binary decision tree with b remaining decisions:

```text
Depth in call edges    Calls at that level
0                      1
1                      2
2                      4
...
b                      2^b

Total = 2^(b+1)-1
Longest live chain = b+1 frames
```

The number of calls doubles at each level, yet the current execution follows one nested path. All completed siblings contribute to total time without remaining on the stack.

For balanced maximum on n leaves, there are n−1 internal calls and constant work per call, giving Θ(n). For merge sort, each level's nontrivial ranges collectively cover at most n positions; linear merging across a full level costs Θ(n). There are Θ(log n) such levels, so time is Θ(n log n). Counting only the tree's Θ(n) nodes would miss the merge work.

### 11.3 Representative recurrences and their assumptions

These are cost models, with base costs constant and integer rounding handled by floors/ceilings in actual code.

| Implementation structure | Representative time recurrence | Time for growing positive n | Maximum frame depth |
| --- | --- | --- | --- |
| Remove one item, one child, constant work | T(n)=T(n−1)+Θ(1) | Θ(n) | Θ(n) |
| Retain one half, constant work | T(n)=T(n/2)+Θ(1) | Θ(log n) worst case | Θ(log n) |
| Two children each with n−1 decisions | T(n)=2T(n−1)+Θ(1) | Θ(2^n) | Θ(n) |
| Two balanced halves, constant combination | T(n)=2T(n/2)+Θ(1) | Θ(n) | Θ(log n) |
| Two balanced halves, linear combination | T(n)=2T(n/2)+Θ(n) | Θ(n log n) | Θ(log n), excluding buffers |
| Naive Fibonacci | T(n)=T(n−1)+T(n−2)+Θ(1) | Exponential; O(2^n) is an upper bound | Θ(n) |
| Decreasing copied suffix, linear local copying | T(n)=T(n−1)+Θ(n) | Θ(n²) | Θ(n), plus copied storage |

The table assumes sequential children. Frame depth does not include other heap objects automatically. Empty inputs need constant setup and a base interpretation; use forms such as O(n+1) or O(log(n+1)+1) when zero is included explicitly.

### 11.4 Optional: where the Master Theorem fits

For standard balanced recurrences of the form `T(n)=aT(n/b)+f(n)` with fixed branching a, shrink factor b>1, and suitable regularity conditions, the Master Theorem summarizes comparisons between leaf growth and per-level work.

The already derived examples illustrate its use: one halving child with constant work gives logarithmic time; two halving children with constant combination give linear time; two halving children with linear combination give n log n.

This is an optional recognition aid, not a requirement to memorize theorem cases before writing correct recursion. The general theorem has conditions; a matching-looking expression alone is insufficient. It does not directly cover Fibonacci's unequal n−1/n−2 reduction, nor does a running-time theorem determine auxiliary space. The level/path arguments above are enough for the core examples.

**Self-check Q23:** Why is the recurrence for factorial's returned value different from its running-time recurrence?

**Self-check Q24:** Why can two algorithms with the same recursion-tree shape have different running times?

## 12. Time, Depth, and Complete Auxiliary Space

### 12.1 Ask four different questions

1. How many invocations execute over the whole run?
2. How much nonrecursive work does each invocation perform?
3. How many frames coexist at the deepest point?
4. What additional objects remain live while those frames exist?

With constant work per call, time is proportional to total calls. With linear copying or merging, add that work across calls. With fixed-size local frame state and no additional working objects, stack storage is proportional to maximum live depth. A reference to an n-element array does not copy that array into every frame.

### 12.2 Summary for the actual examples

Nonempty n is assumed where a maximum is required. Public wrappers add only constant frames. Output is separate from auxiliary space.

| Example | Total recursive-family calls | Maximum family frames | Time | Other working storage / output |
| --- | --- | --- | --- | --- |
| sumTo / standard factorial | n+1 | n+1 | Θ(n+1) | No growing non-frame state; scalar output |
| Array sum / countAbove | n+1 | n+1 | Θ(n+1) | Shared input array; scalar output |
| maxLinear | n | n | Θ(n) | Shared input array; scalar output |
| countUnit / countLinked | n+1 | n+1 | Θ(n+1) | No copied input; scalar output |
| Tail-factorial helper | n+1 | n+1 | Θ(n+1) | Accumulator is fixed-width; wrapper adds one frame |
| choiceCodes, b choices | 2^(b+1)−1 | b+1 | Θ(2^b) | O(2^b) returned list; O(b+1) stack |
| fibNaive | 2F(n+1)−1 | max(1,n) | Exponential | O(n+1) stack; scalar output |
| binarySearchRec | One invocation per visited interval | O(log(n+1)+1) | O(log(n+1)+1) worst case | No subarray copies; scalar output |
| maxByHalves | 2n−1 | ceil(log2(n))+1 | Θ(n) | O(log(n+1)) stack; scalar output |
| mergeSort | 2n−1 for n>=1 | ceil(log2(n))+1 for n>=1 | Θ(n log n), n>=2 | One O(n+1) buffer plus logarithmic stack |
| Simple iterative running total | No recursive calls | O(1) method depth | Θ(n+1) | O(1) auxiliary |

For merge sort, empty input still makes one helper invocation and allocates a length-zero buffer object. It does not perform an n log n amount of work at n=0. These small-case distinctions refine the convention rather than changing the growing-input classification.

### 12.3 Sequential branches use a maximum, not a sum of entire stacks

For a balanced method with constant local state and scalar child answers:

```text
stackSpace(n)
    = constant parent frame
      + max(stackSpace(leftSize), stackSpace(rightSize))
```

The left child's frames have returned when the right child begins. Time adds both child executions, while peak stack depth follows the longer live path.

If the left child returns a large allocated object that the parent retains while evaluating the right child, add that retained object to the space calculation. If every active frame retains a new buffer, add those buffer lengths along the path. “Sequential children” does not erase data that remains reachable.

The JVM can optimize execution, but source-level algorithm analysis should not assume an unpromised optimization removes required stack or allocations.

### 12.4 A correct answer with unnecessarily expensive suffix copies

Compare countUnit's index approach with the following deliberately inefficient implementation. For this cost example, use nonnull **ASCII strings** and small traces; the returned meaning is still the count of an exact char. Input is unchanged.

```java
static int countWithSubstringCopies(String text, char wanted) {
    if (text.isEmpty()) {
        return 0;
    }
    int rest = countWithSubstringCopies(text.substring(1), wanted);
    return rest + (text.charAt(0) == wanted ? 1 : 0);
}
```

For text="abaa", wanted='a', the calls receive "abaa","baa","aa","a","". Their returned counts are 3,2,2,1,0, respectively, when listed in entry order.

In the usual OpenJDK 21 copying implementation, proper nonempty suffixes receive copied content. The [String implementation](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/String.java) selects a new suffix, and the [Latin-1 helper](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/StringLatin1.java) copies its backing range. This is an implementation-specific cost model, not an API-wide promise that every String operation allocates.

| Caller text | Child text | Units copied for that child | Count eventually returned by caller |
| --- | --- | --- | --- |
| abaa | baa | 3 | 3 |
| baa | aa | 2 | 2 |
| aa | a | 1 | 2 |
| a | Empty | 0 | 1 |
| Empty | None | 0 | 0 |

The copy work sums to (n−1)+(n−2)+...+1=Θ(n²). There are still only n+1 calls and n+1 frames. Constant work per call was the assumption that failed.

Each suspended invocation uses its own text again after the child returns. Under the ordinary materialized-string/source-level model, these retained suffix contents can occupy Θ(n²) additional live storage, plus O(n) frames. This is not merely a statement about cumulative allocations. Passing one original String with indices avoids those suffix objects, giving O(n) stack for countUnit and O(1) non-frame working state.

This analysis does not prescribe exact object bytes or rely on a particular compact-string byte width. Empty/full-range special cases and implementation optimizations must be distinguished from the repeated proper-suffix copies used here.

### 12.5 Peak memory and cumulative allocation are different

A balanced algorithm that allocates subarrays of decreasing half sizes can allocate O(n log n) elements across all levels yet retain only O(n) along one active path, because n+n/2+n/4+...=O(n). Exact space depends on which sibling arrays/results remain reachable.

A linear-depth method retaining sizes n,n−1,...,1 can instead have Θ(n²) simultaneously live content. A method that reuses one global buffer, as this merge sort does, has O(n) buffer storage rather than one full buffer per call.

Use the [complete-space discussion](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity) to classify input, working memory, and output. “The objects become garbage later” does not remove their peak-live cost; “the method returns an array” does not mean all intermediate copies are output.

**Self-check Q25:** Under what assumptions may you calculate auxiliary stack as depth times constant frame size?

**Self-check Q26:** How can O(n) calls produce O(n²) time and even O(n²) retained working content?

## 13. Java References and Recursive State

### 13.1 Parameters are local slots initialized by value

Java passes values, including reference values. A new invocation receives a fresh parameter variable initialized from the argument. Reassigning that parameter does not reassign the caller's variable; mutating a shared referenced object can be visible to the caller. See [JLS 21: method invocation](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.5).

Primitive state such as an index or prefix is copied as a primitive value. An array/node/list parameter copies a reference to the same object unless the program explicitly constructs a different object.

```java
static void changePrimitive(int value) {
    value = 99;
}

static void rebindArray(int[] values) {
    values = new int[] {99};
}

static void rebindNode(ListNode node) {
    node = new ListNode(99);
}

static void fillRecursively(int[] values, int replacement) {
    fillFrom(values, 0, replacement);
}

private static void fillFrom(int[] values, int from, int replacement) {
    if (from == values.length) {
        return;
    }
    values[from] = replacement;
    fillFrom(values, from + 1, replacement);
}
```

The first three methods demonstrate local rebinding. fillRecursively explicitly permits mutation: after a normal return, every position contains replacement. Any int replacement is valid; no arithmetic on element values occurs. Empty input does nothing.

Starting with caller x=7 and values referring to array R=[1,2]:

| Operation | Caller-visible result |
| --- | --- |
| changePrimitive(x) | x remains 7 |
| rebindArray(values) | values still refers to R=[1,2] |
| fillRecursively(values,9) | R becomes [9,9] |
| rebindNode(head), where head=A(5) | head still refers to the original A(5) |

rebindArray creates a different array reachable through its local parameter; it does not replace the caller's reference slot. rebindNode similarly creates a different node without replacing caller head.

### 13.2 Independent indices, shared object

During fillFrom(R,0,9) → fillFrom(R,1,9) → fillFrom(R,2,9):

```text
Frame from=0: values -----+
Frame from=1: values -----+----> one array object R
Frame from=2: values -----+       [9,9] at the base

Each frame has its own from.
All values reference slots identify the same R.
```

The helper's contract is to replace the suffix beginning at from. The empty suffix requires no mutation. One assignment handles the current position and the recursive child handles the remaining suffix; length−from decreases. Time and stack are Θ(n+1), no additional arrays are created by the filling algorithm, and output is the changed input.

This is a tail-position recursive call, but it still has linear Java stack requirements. A loop can perform the same assignments with O(1) auxiliary storage. If a mutating execution fails partway through, a normal-return postcondition does not imply the earlier writes were automatically undone.

### 13.3 Shared accumulators require an ownership decision

choiceCodes creates one output list per public invocation and shares it intentionally among all helper calls. Each helper appends a disjoint group of results. Prefix is a primitive value, so siblings do not need to undo one another's prefix assignments.

This differs from a mutable path object reused across branches. If you save the **same path reference** twice and then edit that object, both saved entries observe the edit. A snapshot of the container, such as new ArrayList<>(path), copies its elements' references; it is not automatically a deep copy of mutable elements. Use the [existing copying reference](./Java_List_ArrayList_LinkedList.md#7-references-copying-equality-and-null).

State stored in static fields or instance fields also persists outside one recursive frame. It can accidentally survive a complete public call and contaminate the next call. Prefer a returned answer or state owned by one public invocation when that matches the contract. Section 16 gives a concrete repeated-call failure.

### 13.4 Returning a reference does not clone its object

A recursive method returning a child node's reference returns an existing object unless it explicitly allocated a new one. Caller and callee can therefore share that node. Rewiring next changes a shared object's field; assigning a local head changes only a local slot.

This is why recursive linked-list reversal returns a new entry reference and must terminate the old forward link. Its output reuses original identities and its recursion consumes stack; it is not a deep copy. Reuse [the linked-list explanation](./Linked_List/Linked_List_Essential_Concepts.md#3-java-references-and-node-identity) for the full reference model.

**Self-check Q27:** Why does changePrimitive fail to replace caller x while fillRecursively changes caller-visible array contents?

**Self-check Q28:** During recursive array traversal, which state is copied per call and which state can be shared?

**Self-check Q29:** Why does adding the same mutable path to output twice not create two independent snapshots?

## 14. Recursion, Iteration, and Explicit Stacks

### 14.1 A loop can preserve a recursive state meaning

sumTo needs only the accumulated total and the next remaining number if we are free to perform additions directly. Contract: nonnegative int n; return the same long mathematical sum; reject negative input.

```java
static long sumToLoop(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be nonnegative");
    }
    long total = 0;
    int remaining = n;
    while (remaining > 0) {
        total += remaining;
        remaining--;
    }
    return total;
}
```

Invariant: total + sumTo(remaining), interpreted mathematically, equals the original requested sum. The loop transfers one term into total each step. At remaining=0 the invariant gives the answer. No recursive call is actually executed in this proof expression.

Time remains Θ(n+1), but auxiliary space is O(1). The decreasing loop counter also avoids an overflowing increment past Integer.MAX_VALUE.

### 14.2 An explicit stack can represent pending additions

To simulate the recursive order, preserve the terms waiting to be added, then pop them during a manual unwinding phase.

```java
static long sumWithExplicitStack(int n) {
    if (n < 0) {
        throw new IllegalArgumentException("n must be nonnegative");
    }
    Deque<Integer> pending = new ArrayDeque<>();
    for (int value = n; value > 0; value--) {
        pending.push(value);
    }
    long result = 0;
    while (!pending.isEmpty()) {
        result += pending.pop();
    }
    return result;
}
```

For n=3, stack displays are bottom-to-top:

| Action completed | Pending stack | result |
| --- | --- | --- |
| push 3 | [3] | 0 |
| push 2 | [3,2] | 0 |
| push 1 | [3,2,1] | 0 |
| pop 1, add | [3,2] | 1 |
| pop 2, add | [3] | 3 |
| pop 3, add | [] | 6 |

The initial result zero represents the recursive zero base. The stack stores n positive terms; it does not need a separate marker for that known base answer. Every term is pushed and popped once, giving Θ(n+1) aggregate time and O(n+1) auxiliary storage, with O(1) Java method depth.

ArrayDeque rejects null; this code pushes nonnull boxed ints. Endpoint operations have amortized constant cost, so occasional growth is included in the aggregate bound. See the [Java 21 ArrayDeque API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html).

For this simple sum, the running-total loop needs less storage. The explicit-stack version teaches how pending work can be represented manually. Removing recursive calls does not automatically remove the memory needed by the algorithm.

### 14.3 More complex continuations need more than a parameter

A recursive frame may need to remember which child is next, a left-child result, and work to perform afterward. An explicit equivalent might store a conceptual entry such as:

```text
Frame(problem, stage, partialResult)
```

Pushing only a node or range is insufficient if the return-time combination requires additional state. When converting branching recursion, preserve execution order and the meaning of each saved entry. The later Trees guide develops traversal-specific forms; no full traversal implementation is required here.

| Aspect | Recursive implementation | Iterative running state | Iteration with an explicit stack |
| --- | --- | --- | --- |
| Pending work | Invocation frames | Only what the loop retains | Chosen entry objects/values |
| Java call depth | Depends on recursion depth | O(1) | O(1) |
| Auxiliary storage | Frames plus other allocations | Can be O(1) when sufficient | Peak entries plus their payloads |
| Hierarchical structure | Often mirrors the definition | May need careful state design | Can encode traversal stages explicitly |
| Failure mode for excessive state | StackOverflowError may occur | Depends on retained allocations | Explicit storage can still exhaust memory |
| Time | Depends on calls and per-call work | Depends on actual loop work | Includes pushes/pops and processing |
| Clarity | Strong when contracts match substructure | Strong for straightforward scans | Useful when depth control and saved state matter |

No approach is always faster or clearer. Method calls, boxing, allocation, JIT behavior, and locality affect constants. Compare the actual algorithms first, then measure a representative implementation if those constants matter. A short recursive method and a long iterative method may perform the same work—or entirely different work.

**Self-check Q30:** Why does an explicit-stack rewrite not automatically have O(1) auxiliary space?

**Self-check Q31:** What information can be lost by replacing a recursive call with only a pushed parameter?

## 15. Trees, DFS, Backtracking, and DP Previews

These are connections, not complete category lessons. Keep the roadmap's actual dependencies and later practice priorities.

### 15.1 Trees: a recursive structure

A subtree is itself a tree. A helper can promise an answer about one subtree, then the parent can combine child answers or pass constraints downward. This naturally supports size, height, structural transformations, and other queries.

The important preparation is a precise returned meaning, an empty/small structure contract, and the distinction between information inherited from a parent and information returned by a child. A tree's height h controls recursive depth; h is not always logarithmic. A skewed tree can have h proportional to its node count.

Continue with the roadmap's [Trees category](./DSA_Interview_Roadmap.md#7-trees), after its Binary Search and Linked List prerequisites plus these foundations. Reading this preview does not establish tree-traversal implementation evidence.

### 15.2 DFS: saved exploration state

Recursive depth-first exploration suspends a parent while following one child or neighbor, then resumes with the next choice. An explicit stack can preserve an equivalent exploration state.

Graphs can contain cycles and shared neighbors, so a null/leaf base alone does not prevent repeated exploration. The full graph lesson defines visitation and representation rules. Moving to a neighbor is not automatically a decreasing measure when the same vertices can be revisited.

The [Graphs category](./DSA_Interview_Roadmap.md#13-graphs) follows the roadmap's Backtracking/DFS preparation and includes iterative alternatives for deep traversals. Do not assume a recursive graph walk uses only logarithmic stack.

### 15.3 Backtracking: restore branch-local choices

Backtracking explores choices while maintaining a partial candidate. When that candidate is mutable shared state, a sibling branch must receive the intended restored state:

```text
make a choice
  -> explore the smaller search state
  -> restore the choice-dependent shared state
  -> try another choice
```

The immutable primitive prefix in choiceCodes needed no undo. A shared mutable path, board, or visited structure may need explicit restoration. The correct restoration policy depends on whether state is path-local or global.

See [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking) after Trees. Validity rules, pruning, duplicate handling, and full path enumeration belong there. They are not consequences of merely having two recursive calls.

### 15.4 DP: reuse answers to repeated complete states

fibNaive computes the same F(2) more than once. Its full state is just n, so the same n means the same pure answer. Remembering an already computed answer can avoid repeating the corresponding subtree.

This motivates memoization. It does not automatically remove recursion depth, and caching requires a complete state key with a consistent returned meaning. Two calls with equal remaining depth but different required prefixes are not necessarily equivalent states. A method with visible mutation also cannot be memoized by ignoring required effects.

The roadmap places [1-D DP](./DSA_Interview_Roadmap.md#14-1-d-dynamic-programming) after Backtracking and [2-D DP](./DSA_Interview_Roadmap.md#16-2-d-dynamic-programming) after the Graphs/1-D DP preparation. State design, transitions, tabulation, and storage optimization will be separate lessons.

**Self-check Q32:** Why does discovering recursion in a problem not automatically establish that memoization is appropriate?

## 16. Common Mistakes and Java Pitfalls

### 16.1 Small executable failures

The following methods are deliberately incorrect for the indicated tasks. They compile so that small failures can be studied; they are not implementations to adopt.

For the bad factorial/ignored-sum examples, use nonnegative small n, at most 20. badMissingBase starts at index zero of a small nonnull array. badNoProgress(0) returns directly; a positive argument never approaches that base. Test nontermination/stack exhaustion only in an isolated process with a timeout.

```java
static long badFactorialZero(int n) {
    if (n == 0) {
        return 0; // deliberately wrong empty product
    }
    return n * badFactorialZero(n - 1);
}

static long badIgnoredSum(int n) {
    if (n == 0) {
        return 0;
    }
    badIgnoredSum(n - 1);
    return n; // deliberately discards the child answer
}

static long badMissingBase(int[] values, int from) {
    return (long) values[from] + badMissingBase(values, from + 1);
}

static long badNoProgress(int n) {
    if (n == 0) {
        return 0;
    }
    return badNoProgress(n);
}

static long staleTotal;

static long badSharedSum(int[] values) {
    badSharedFrom(values, 0);
    return staleTotal;
}

private static void badSharedFrom(int[] values, int from) {
    if (from == values.length) {
        return;
    }
    staleTotal += values[from];
    badSharedFrom(values, from + 1);
}
```

badSharedSum is intended to return the sum for **this call**, but its static accumulator starts at zero only when initialized and persists afterward. In a fresh state, calling it on [1,2] returns 3; then calling it on [4] returns 7 instead of 4. A returned suffix sum or state freshly owned by the public invocation avoids this contamination.

### 16.2 Failure examples and the reasoning to repair

| Mistake | Small witness | What goes wrong | Correct reasoning |
| --- | --- | --- | --- |
| Wrong base value | badFactorialZero(3) | Returns 0 instead of 6 | Empty product is 1 |
| Ignored return | badIgnoredSum(3) | Returns 3 instead of 6 | The child's answer must be combined |
| Missing empty-suffix base | badMissingBase([2],0) | ArrayIndexOutOfBoundsException | Stop before reading index length |
| No progress | badNoProgress(1) | Same argument keeps nesting until resource failure | A valid measure must decrease |
| Wrong-direction argument | Base n=0, call f(n+1) from n=1 | Moves away from the proposed nonnegative measure | Prove progress in the intended domain |
| Postfix-decrement argument | f(1) calls f(n--) | Child receives old n=1; changing the parent's local slot does not help the child | Pass the intended smaller value |
| Wrong array boundary | Return 0 at i==length−1 for a sum | [7] incorrectly contributes 0 | Empty suffix begins at length |
| Invalid maximum identity | Use 0 as the empty maximum | [−8,−3] can incorrectly return 0 | Define empty rejection or a suitable missing representation |
| Nonshrinking search interval | On [4], target 5, retain [mid,hi) | [0,1) can be repeated unchanged | Exclude a checked mismatch consistently |
| Persistent shared accumulator | badSharedSum([1,2]), then ([4]) | Returns 3, then 7 | Scope mutable state to its intended operation |
| Shared path mistaken for copies | Save path=[1], edit it to [2], save same reference again | Both saved entries show [2] | Preserve independent snapshots when required |
| Reference copy mistaken for object copy | Two array parameters identify one array | Mutation through either is visible | Distinguish local reference slots from object identity |
| Tail call assumed constant space | factorialTail(4) | Five helper frames still need accounting | Java does not promise tail-call elimination |
| Calls mistaken for frames | fibNaive(4) | Nine total calls are mistaken for nine simultaneous frames | Maximum live depth is four |
| Two children assumed exponential | Balanced maximum on 8 values | Mislabels a 15-call tree as exponential in n | Both child sizes halve; depth is four |
| Integer width ignored | Accumulate 13! in int | Wraps to 1932053504 instead of 6227020800 | Choose a valid type/domain before multiplication |
| Per-call copying ignored | Suffix copies for "abaa" | Copies 3+2+1 units despite only five calls | Add local work and retained objects across calls |

These are different defects. Adding a null/base guard does not fix a wrong recurrence. Reducing an argument does not repair an incorrect base answer. Replacing recursion with a loop does not make a flawed state transition correct.

### 16.3 Stack overflow is a symptom to diagnose

First check whether every path reaches its base. If it does, compute the worst valid depth. A linear recursive implementation may simply be unsuitable for the largest valid input.

Do not catch StackOverflowError as a substitute for a base case or return a partial result as though the contract were met. A test harness may isolate and recognize the error to demonstrate a defect; that is different from using it as normal algorithm control flow.

The number of frames at failure is not a portable measurement of an algorithm's safe input bound. Logical depth analysis and small deterministic traces are more transferable than memorizing one JVM's observed limit.

### 16.4 Overflow can happen before assignment to long

A long result variable cannot repair an int expression that already overflowed. For a mathematical triangular sum, `(long) n * (n + 1L) / 2` widens before multiplication and addition. `(long) (n * (n + 1))` is too late.

The sum through 65,536 is 2,147,516,416, larger than Integer.MAX_VALUE. Verify numeric boundaries with a loop or an independent wide-arithmetic oracle rather than deliberately making a 65,537-frame recursive call. The factorial methods reject 21; removing that guard would permit a product too large for long.

The Fibonacci teaching bound is 30, so its results fit long easily. Do not confuse “we bounded the experiment” with “exponential work is efficient for all representable results.”

### 16.5 A compact edge checklist

For a recursive implementation, check empty/minimum input; singleton input; ordinary input; negative or otherwise invalid arguments; duplicate/negative/extreme values where meaningful; both recursive branches; the last reachable position; repeated public calls; and shared mutable state.

For a node walk, establish acyclicity or choose a suitable visited-state algorithm. For strings, state the character model. For copying, count allocation and aliasing. For sorted search, verify interval semantics and whether sortedness is promised. For a mutating method, define successful-return structure and any required restoration.

**Self-check Q33:** Why is StackOverflowError not an acceptable substitute for a mathematical base case?

**Self-check Q34:** Why can assigning an arithmetic expression to a long variable still leave an overflow bug?

## 17. Recognition and a Derivation Cheat Sheet

Look for a smaller instance with the same meaning, a hierarchy, repeated decomposition, mathematical recurrence, or natural branching choices whose answers can be combined. Then ask whether recursion is a good implementation under the depth, mutation, and performance constraints.

| Scenario | Decision to make |
| --- | --- |
| R1: Sum a long flat array | Is a recursive frame per element useful compared with a loop? |
| R2: Return a nonempty array's maximum with recursion required | How can you meet the requirement without a linear-depth chain? |
| R3: Find a value in a sorted read-only array | How many smaller ranges actually need searching? |
| R4: Combine maxima from two disjoint halves | Does branching imply exponential work? |
| R5: Sort using two sorted child ranges | What work belongs at each parent, and where is temporary storage? |
| R6: Emit every bounded binary choice code | How many outputs already have to be produced? |
| R7: Recompute the same Fibonacci index | Is the entire relevant state equal, and can its answer be reused? |
| R8: Follow nodes that might revisit an identity | Does a null base alone prove termination? |
| R9: Explore choices using a shared mutable path | What must the next sibling see, and what output must be copied? |
| R10: A method calls itself with unchanged arguments | Where is progress, regardless of how short the code looks? |

<details>
<summary>Recognition guidance</summary>

- **R1:** A straightforward loop computes the same aggregate in O(n) time/O(1) auxiliary. Linear recursion is useful for learning the contract but can exhaust stack on large arrays.
- **R2:** A balanced decomposition can use O(log n) stack and O(n) time; a simple loop would still use O(1) space if recursion were not required.
- **R3:** Sortedness lets one mismatch eliminate a half. Search only the possible half; iteration avoids the recursive stack.
- **R4:** The actual sizes halve. There are 2n−1 calls for singleton leaves, so constant combination work gives linear total time.
- **R5:** Both halves must be sorted, then merging is linear in the parent range. Count the shared buffer separately from logarithmic depth.
- **R6:** There are 2^b distinct fixed-width outputs. Exponential enumeration here reflects the output requirement, not necessarily an avoidable inefficiency.
- **R7:** Pure F(n) depends only on n, so repeated states motivate memoization. The later DP lesson explains correct caching and evaluation order.
- **R8:** A cycle can avoid null forever. Clarify structure assumptions or use the identity/visit techniques in the Linked List/Graph categories.
- **R9:** Sibling exploration must receive restored path state; saved outputs may need snapshots. Full restoration policies belong to Backtracking.
- **R10:** A recursive contract cannot justify itself at the same size. Establish a valid reduction before choosing recursion or iteration.

</details>

### A practical derivation card

1. **Contract:** What precisely is returned or changed? Which inputs are valid?
2. **Base:** Which smallest instances have direct answers?
3. **Reduction:** Which valid smaller instances determine this answer?
4. **State:** What must a frame keep, and what objects are shared?
5. **Combination:** What happens before and after each child?
6. **Termination:** What decreases on every path?
7. **Correctness:** Why do correct child results establish the parent promise?
8. **Costs:** Count all invocations, per-call work, maximum live frames, other live objects, and output.
9. **Alternatives:** Can a loop, index range, shared buffer, or later memoization improve the relevant constraint?
10. **Tests:** Trace a base, an ordinary case, a boundary, and a case that challenges an assumption.

Recursion is a technique for expressing an algorithm. It is not evidence that the algorithm is optimal. Binary search, a repeated-subproblem computation, and an exhaustive enumerator use recursion for different reasons and have different lower bounds.

## 18. Conceptual Assessment and Answer Key

Answer the Q1–Q34 prompts at their original sections before opening the key. Use a small call tree or live-frame drawing when the question concerns execution. The purpose is to diagnose reasoning, not to create another exercise workbook or progress ledger.

<details>
<summary>Q1–Q10: fundamentals, frames, and contracts</summary>

| Question | Concise answer |
| --- | --- |
| Q1 | It asks for the sum through 2; the current invocation retains the obligation to add 3. |
| Q2 | A short source expression can trigger many invocations and retain many suspended callers. |
| Q3 | The base must be correct, and every valid recursive path must reach it through valid smaller arguments. |
| Q4 | The chain must be finite and acyclic, so following next eventually reaches null. |
| Q5 | sumTo(3), sumTo(2), sumTo(1), and sumTo(0) are live; only sumTo(0) is current. |
| Q6 | Finite depth can still exceed the runtime's available stack; mathematical termination does not imply unlimited resources. |
| Q7 | The deepest invocation completes first; each waiting caller records its exit only after its child returns. |
| Q8 | Ordinary synchronous execution completes one child before starting the next; only the parent and current child path coexist. |
| Q9 | Correct direct cases and a strictly smaller recursive argument support induction; this is not a license to assume same-sized self-calls terminate. |
| Q10 | The current method violates its own result contract even if its child fulfilled a smaller contract. |

</details>

<details>
<summary>Q11–Q22: linear, tail, branching, and divided work</summary>

| Question | Concise answer |
| --- | --- |
| Q11 | The product of no factors is the multiplicative identity 1; a zero would propagate through every product. |
| Q12 | Empty sum/count have direct identities of zero; an empty set has no int maximum under this interface. |
| Q13 | It counts each matching char/code unit. It does not automatically count whole code points or displayed symbols. |
| Q14 | There is no decreasing number of remaining successor positions ending at null; previously visited identities can recur. |
| Q15 | It is evaluated to form the child's argument before the call; afterward the helper only returns the child's result. |
| Q16 | The accumulator-times-remaining-factorial invariant proves the result; Java's lack of a guaranteed tail-call optimization means stack depth must still be counted. |
| Q17 | A full depth-two binary tree has 1+2+4 nodes, but a current root-to-leaf path has only three frames. |
| Q18 | The state includes remaining decisions and the chosen prefix, not merely remaining depth. |
| Q19 | Its children have different sizes n−1 and n−2; the recurrence must represent those actual calls. |
| Q20 | Sortedness proves one half cannot contain the target after a mismatch. Searching the other half would discard the main optimization. |
| Q21 | All n leaves and n−1 internal calls execute, but balanced sequential children share only a logarithmic live path. |
| Q22 | The shared temporary array uses O(n) additional storage; logarithmic stack describes only one component. |

</details>

<details>
<summary>Q23–Q34: costs, Java state, and transfer</summary>

| Question | Concise answer |
| --- | --- |
| Q23 | Multiplication affects the returned numeric value, but one fixed-width multiplication is constant local work in the cost recurrence. |
| Q24 | Per-call work can differ: scalar comparison for maximum versus linear-range merging for merge sort. |
| Q25 | Each live invocation has constant-size frame state, and other retained allocations are counted separately rather than hidden behind references. |
| Q26 | A call may copy a suffix of length proportional to its remaining input, and many copied suffixes can remain live in suspended callers. |
| Q27 | The primitive parameter is a separate value slot; array parameters copy a reference to the same mutable array object. |
| Q28 | Indices and reference slots are local to each invocation; the referenced array object can be shared by all of them. |
| Q29 | Both entries identify one object, so later mutation is visible through both. Independent snapshots require separate appropriate copies. |
| Q30 | The explicit container may hold a growing number of pending entries even while Java invocation depth stays constant. |
| Q31 | The resume stage, completed child result, or work needed after a child can be omitted if only the original argument is saved. |
| Q32 | Memoization requires repeated equivalent complete states and reusable answers, not merely the presence of recursive calls. |
| Q33 | Resource exhaustion provides no correct direct answer and can occur on a terminating but overly deep computation. |
| Q34 | Java evaluates an expression using its operand types before assignment; widening an already-overflowed int value cannot recover the intended value. |

</details>

For an oral explanation, also state the domain, base definition, reduction, pending work, and depth convention. If you can quote O(2^n) but cannot distinguish Fibonacci's repeated states from the enumerator's required outputs, revisit the actual call trees.

## 19. Key Takeaways and Foundational Readiness

A recursive call creates a smaller obligation under a contract. The base answers a valid minimal instance, the reduction establishes progress, and the caller combines a correct child result into its own promised result. Java preserves the suspended caller's local state until execution resumes.

Total calls measure the entire execution tree. Maximum depth measures one simultaneously live chain. Per-call merging/copying and retained objects determine whether either count is sufficient to explain time or auxiliary space. Tail position alone does not remove Java stack requirements.

Before treating the Phase 0 recursion foundations as reliable, demonstrate the following without copied code:

- Trace descent, base execution, and unwinding for a linear method.
- Rebuild array sum and the small branching choice enumerator; state their exact result contracts.
- Explain factorial's numeric domain and compare ordinary, tail-recursive, and iterative versions.
- Draw Fibonacci's repeated calls and count live frames separately.
- Derive one halving recurrence and one balanced two-child recurrence.
- Include buffers, copied data, output, and recursive frames in space analysis.
- Distinguish per-call primitives/reference slots from shared mutable objects.
- Identify a missing progress rule, a wrong base, an ignored result, and state leaking across public calls.
- Choose a simple loop or explicit stack when depth or the task's structure justifies it.

These are evidence expectations, not prechecked progress. Record Phase 0 work in the roadmap's [existing foundation checklist](./DSA_Interview_Roadmap.md#phase-0-foundations). Its [common readiness rubric](./DSA_Interview_Roadmap.md#common-readiness-rubric-required-for-every-category) later applies to each actual category. There is no extra official Recursion category or duplicated dashboard here.

Use the roadmap's learning loop: understand, implement, practice, explain, review, validate. A familiar cold re-solve supports retention; an unfamiliar changed requirement tests transfer. Follow the [flexible review guidance](./DSA_Interview_Roadmap.md#revision-and-spaced-repetition), roughly 1,3,7,14 days when useful, adjusted to your results.

Proceed to Trees/Backtracking when their prerequisites and these contracts are reliable. Later graph and DP work needs additional structure-specific reasoning. Document creation alone does not earn T, I, P, or R for any topic.

## 20. References, Validation, and Next Steps

### Existing repository materials

- [DSA Interview Roadmap](./DSA_Interview_Roadmap.md), especially [Phase 0](./DSA_Interview_Roadmap.md#phase-0-foundations), [Trees](./DSA_Interview_Roadmap.md#7-trees), [Backtracking](./DSA_Interview_Roadmap.md#10-backtracking), [Graphs](./DSA_Interview_Roadmap.md#13-graphs), and [1-D DP](./DSA_Interview_Roadmap.md#14-1-d-dynamic-programming).
- [Complexity Analysis and Foundations](./Complexity/Complexity_Analysis_and_Foundations.md), including [recursive complexity](./Complexity/Complexity_Analysis_and_Foundations.md#12-recursive-complexity) and [space complexity](./Complexity/Complexity_Analysis_and_Foundations.md#11-space-complexity).
- [Java Arrays](./Java_Array.md), [Java Strings](./Java_String.md), and [Java List / ArrayList / LinkedList](./Java_List_ArrayList_LinkedList.md).
- [Stack Essential Concepts](./Stack/Stack_Essential_Concepts.md).
- [Binary Search Essential Concepts](./Binary_Search/Binary_Search_Essential_Concepts.md).
- [Linked List Essential Concepts](./Linked_List/Linked_List_Essential_Concepts.md).
- [Testing Essential Concepts](./Testing_Essential_Concepts.md).

All directly requested supporting references were available. Their existing Java fundamentals are reused rather than reproduced wholesale. Later-category previews link to the authoritative roadmap sections.

### Primary Java references

- [JVMS 21: Java Virtual Machine stacks](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.5.2) and [frames](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.6).
- [JLS 21: method invocation and fresh parameter variables](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.5).
- [Java 21 StackOverflowError](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StackOverflowError.html).
- [Java 21 ArrayDeque](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayDeque.html) and [ArrayList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html).
- [OpenJDK 21 String](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/String.java) and [StringLatin1](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/StringLatin1.java) for the stated implementation-specific proper-substring copying model.
- [Official NeetCode roadmap](https://neetcode.io/roadmap) as the external organizational reference; the local roadmap governs this guide's scope and prerequisites.

Context7 was used for Java 21 library lookup. Results that did not answer the specific API question were checked directly against the linked official Java 21 documentation. JVM/language rules and the substring-copying implementation were verified from their primary sources.

### Validation scope

All **18 Java blocks**, containing **41 methods**, were extracted directly from the document and compiled together for Java 21 using `--release 21 -Xlint:all -Werror`. A temporary runner passed **237,487 assertions**.

Runtime checks covered **1,302 array cases** and **1,099 string cases**, including exhaustive arrays of lengths 0–6 over {−2,0,3}, exhaustive strings of lengths 0–6 over {a,b,c}, random/extreme-int arrays, empty inputs, and UTF-16 surrogate/code-unit cases. Independent loop, BigInteger, and sorting oracles checked numeric results, ranges, mutation contracts, and output order.

Logical instrumentation checked **852,259 recursive-family invocations** across the tested runs, including call counts and maximum depth for linear, mutual, tail, branching, halving, and balanced recursion. Power-of-two merge ranges also confirmed the stated per-level work counts. A **100,000-element** input exercised balanced maximum and merge sort; linear recursive tests stayed small. Allocation-space bounds were derived from the source and the stated lifetime/copying model, rather than inferred from measured JVM heap bytes.

The intentionally nonprogressing method produced the expected StackOverflowError in an isolated JVM process with a timeout. Finite defective examples reproduced wrong base values, discarded results, missing-boundary exceptions, and state contamination across calls.

**106 rows across 21 trace/example tables** were checked against recorded Java call/return events or independent boundary/state models. Seventeen recorded execution runs also supported the linked-node and mutual-recursion diagrams. All **62 local links and anchors** resolved; the 20 sections, 34 self-checks with answers, and 10 recognition scenarios were checked structurally.

Content hashes confirmed that all **25 pre-existing files** retained their initial contents. This task added only Recursion_Essential_Concepts.md inside DSA_Guide. Temporary source files, compiled classes, instrumentation, and validation results stayed outside the repository. No progress gate was automatically earned.

The frame diagrams model logical execution rather than a byte-level JVM layout. Complexity follows the stated fixed-width and allocation models; tests do not establish a universal safe recursion depth or make a valid deep input fit every runtime.

### Suggested next session

Draw sumTo(3), factorialTail(4), and fibNaive(4) with notes closed. Label returned values and current versus suspended frames. Recreate arraySum and choiceCodes from their contracts, then compare a large flat scan with its iterative alternative. Revisit whichever proof, state, or cost explanation you could not reconstruct.

Use these foundations in the later roadmap categories, retaining their own T/I/P/R evidence and canonical practice records.







