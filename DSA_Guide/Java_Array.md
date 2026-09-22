# Java Arrays: A Practical Reference

Java arrays are the basic fixed-size containers behind many DSA problems and useful backend tasks. The examples below assume `import java.util.Arrays;` where `Arrays` is used. Short code blocks are intended for a method body; complete classes are shown where several methods work together.

# 1. What an array is

An array is an object that holds a fixed number of elements of one declared type. Its positions are **zero-indexed**: an array of length 3 has indexes `0`, `1`, and `2`. An `int[]` stores `int` values; a `String[]` stores references that can point to `String` objects (or be `null`).

Arrays are useful when the element count is known or managed separately, and when fast indexed access matters. `a[i]` reads or writes a position in **O(1)** time because the runtime can locate it from the array reference and index. Think of a one-dimensional array as a logically contiguous sequence of slots: primitive values are stored in the array object, while an object array stores references. Avoid assuming that the objects *referenced* by an array occupy adjacent memory, or that a Java program can rely on a particular physical memory layout.

```java
int[] scores = {80, 90, 100};
int first = scores[0]; // 80
scores[1] = 95;        // [80, 95, 100]
```

The length cannot change after creation. To hold more elements, create a new array (or use an `ArrayList`).

# 2. Declare, create, and initialize

```java
int[] nums;       // preferred: [] clearly belongs to the type
int otherNums[];  // legal, but less clear, especially in multi-variable declarations
String[] names;
```

The conventional modern style is `int[] nums`. Beware that `int[] a, b;` declares **two arrays**, whereas `int a[], b;` declares an array and an `int`.

```java
int[] nums = new int[5]; // indexes 0 through 4, initially all 0
int[] empty = new int[0]; // valid; empty.length is 0
```

`new` creates an array object. `5` is the number of elements, and `nums` stores a **reference** to the array. Conceptually, the object is allocated in managed memory, often described as the heap; Java does not promise a particular physical placement. A zero-length array is valid but has no accessible elements. A negative size compiles if it is a runtime value, then throws `NegativeArraySizeException` when executed.

```java
int requestedSize = -1;
// new int[requestedSize] would throw NegativeArraySizeException at runtime.
```

Three common initialization styles:

```java
int[] knownValues = {1, 2, 3};             // concise at declaration
int[] explicitValues = new int[]{1, 2, 3}; // useful in an argument or assignment
int[] enteredLater = new int[3];           // size known, values supplied later
enteredLater[0] = 10;
enteredLater[1] = 20;
enteredLater[2] = 30;
```

The shorthand `{1, 2, 3}` works in a declaration, but a later assignment needs `new int[]{1, 2, 3}`. A computed length, such as `new int[n]`, must be nonnegative.

## Default element values

`new T[n]` initializes **every element**. This differs from uninitialized local variables, which must be assigned before use.

| Element type | Default in a newly created array |
| --- | --- |
| `byte` | `0` |
| `short` | `0` |
| `int` | `0` |
| `long` | `0L` |
| `float` | `0.0f` |
| `double` | `0.0d` |
| `char` | `'\u0000'` (NUL, usually invisible) |
| `boolean` | `false` |
| `String` or any other object reference | `null` |
| Custom object such as `Person` | `null` |

```java
String[] strs = new String[3]; // [null, null, null]
strs[0] = "hello";
strs[1] = "bye";
strs[1] = null;             // valid: reference elements may be null
```

An array of `Person` does not automatically create `Person` objects; each slot begins as `null`. An `int[]` cannot contain `null`, while `Integer[]` can.

# 3. Indexes, `length`, and traversal

Valid indexes satisfy `0 <= i && i < nums.length`. An index below `0` or at least `nums.length` throws `ArrayIndexOutOfBoundsException` at runtime.

```java
int[] nums = {10, 20, 30};
int first = nums[0];
nums[1] = 100;
int count = nums.length; // 3
// nums[3] would throw ArrayIndexOutOfBoundsException.
```

> **Important:** `length` is a field of every array, so use `nums.length`. `nums.length()` is **incorrect Java**. A `String` instead uses `length()`, and a `List` uses `size()`.

Use an indexed `for` when you need the index, want to replace elements, or coordinate multiple arrays. Use an enhanced `for` for read-only visits to values. A `while` is useful when the stopping condition changes during the scan.

```java
int[] nums = {4, 5, 6};
for (int i = 0; i < nums.length; i++) {
    nums[i] *= 2;
}
for (int num : nums) {
    System.out.println(num);
}
int i = 0;
while (i < nums.length && nums[i] != 10) {
    i++;
}
```

In `for (int num : nums)`, `num` is a copy of the primitive value; assigning to `num` does **not** replace `nums[i]`. With an object array, the loop variable receives a copy of each reference: you can mutate a referenced mutable object, but reassigning the loop variable does not replace the array slot.

# 4. Primitive arrays, object arrays, and references

| Array | What each slot contains | Default slot value |
| --- | --- | --- |
| `int[] numbers` | An `int` value | `0` |
| `String[] names` | A `String` reference | `null` |
| `Person[] people` | A `Person` reference | `null` |

```java
int[] a = {1, 2, 3};
int[] b = a;  // copies the reference, not the elements
b[0] = 100;
System.out.println(a[0]); // 100
```

```text
a ─┐
   ├──> one int[] object: [100, 2, 3]
b ─┘
```

Both variables point to the same array, so a change through either is visible through both. Reassigning `b` to a different array would not change which array `a` refers to. Passing an array to a method similarly copies its reference; the method can edit its elements.

## Copying arrays

| Technique | Result | Important detail |
| --- | --- | --- |
| `array.clone()` | New array of the same length/type | One-level copy; `int[]` values copied, object references shared |
| `Arrays.copyOf(a, newLength)` | New array with chosen length | Truncates or pads with default values |
| `Arrays.copyOfRange(a, from, to)` | New array for `[from, to)` | End exclusive; may pad if `to` exceeds length |
| `System.arraycopy(src, srcPos, dst, dstPos, count)` | Copies into an existing destination | No new array; overlapping regions handled correctly |
| Manual loop | New or existing destination | Flexible for transformations while copying |

```java
int[] source = {1, 2, 3};
int[] cloned = source.clone();
int[] grown = Arrays.copyOf(source, 5);       // [1, 2, 3, 0, 0]
int[] middle = Arrays.copyOfRange(source, 1, 3); // [2, 3]
int[] destination = new int[source.length];
System.arraycopy(source, 0, destination, 0, source.length);
int[] manual = new int[source.length];
for (int i = 0; i < source.length; i++) {
    manual[i] = source[i];
}
```

For `Person[]`, all these techniques copy **references**, not the `Person` objects. Likewise, `matrix.clone()` copies only the outer array; its rows are still shared. To isolate a matrix's rows, copy each row separately. Copying the objects inside rows requires an object-specific deep-copy strategy.

```java
int[][] matrix = {{1, 2}, {3, 4}};
int[][] rowCopies = new int[matrix.length][];
for (int row = 0; row < matrix.length; row++) {
    rowCopies[row] = matrix[row].clone();
}
```

# 5. Comparing and printing

`a == b` asks whether two references identify the **same array object**. For separate arrays with the same elements, use `Arrays.equals(a, b)`. For nested arrays, `Arrays.deepEquals(a, b)` descends into nested arrays; ordinary `Arrays.equals` compares nested array references as elements.

```java
int[] a = {1, 2};
int[] b = {1, 2};
boolean sameObject = a == b;        // false
boolean sameValues = Arrays.equals(a, b); // true
int[][] x = {{1, 2}};
int[][] y = {{1, 2}};
boolean nestedSame = Arrays.deepEquals(x, y); // true
```

`System.out.println(nums)` normally prints an array type/identity representation such as `[I@...`, not its elements. Use `Arrays.toString` for one-dimensional arrays and `Arrays.deepToString` for nested arrays.

```java
int[] nums = {1, 2, 3};
int[][] grid = {{1, 2}, {3, 4}};
System.out.println(Arrays.toString(nums));     // [1, 2, 3]
System.out.println(Arrays.deepToString(grid)); // [[1, 2], [3, 4]]
```

# 6. `java.util.Arrays`: methods worth knowing

Import the utility class with `import java.util.Arrays;`. These are **static utility methods**, called as `Arrays.sort(a)`, rather than methods on an array. In the table, `n` is the array length, `k` is a copied or filled range length, and `d` is the first mismatch position (or the shorter length). Complexity is the usual sequential work; parallel running time depends on input size and available processors. For object sorting/comparison, comparator cost also matters. The star rating reflects DSA, interviews, general Java, and backend usefulness together.

| Priority | Method | Return Type | Purpose | Time Complexity | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `sort(a)` / `sort(a, from, to)` | `void` | Sort primitive or naturally ordered object array; range is `[from,to)` | Usually O(n log n) | `Arrays.sort(nums)` | Prepare for two pointers/search | Order small in-memory data |
| ⭐⭐⭐⭐ | `sort(a, comparator)` | `void` | Sort object array by chosen order | O(n log n) | `Arrays.sort(words, String.CASE_INSENSITIVE_ORDER)` | Interval/custom-order tasks | Sort DTOs or records |
| ⭐⭐⭐⭐ | `binarySearch(a, key)` / range or comparator overload | `int` | Find key in matching sorted order | O(log n) | `Arrays.binarySearch(nums, 7)` | Fast lookup | Lookup in sorted in-memory table |
| ⭐⭐⭐⭐⭐ | `equals(a, b)` | `boolean` | Compare one-dimensional elements | O(n) worst case | `Arrays.equals(a, b)` | Check expected output | Compare byte arrays or values |
| ⭐⭐⭐ | `deepEquals(a, b)` | `boolean` | Compare nested object arrays recursively | O(total visited elements) | `Arrays.deepEquals(x, y)` | Matrix test comparison | Compare nested data |
| ⭐⭐⭐⭐ | `fill(a, value)` / range overload | `void` | Set every selected slot | O(k) | `Arrays.fill(nums, -1)` | Initialize sentinel/frequency arrays | Reset buffers |
| ⭐⭐⭐⭐⭐ | `copyOf(a, newLength)` | Same array type | Copy, truncate, or pad | O(newLength) | `Arrays.copyOf(nums, 5)` | Preserve state or resize manually | Defensive copy |
| ⭐⭐⭐⭐ | `copyOfRange(a, from, to)` | Same array type | Copy `[from,to)` | O(to - from) | `Arrays.copyOfRange(nums, 1, 3)` | Extract subarray | Copy selected records |
| ⭐⭐⭐⭐⭐ | `toString(a)` | `String` | Render one-dimensional array | O(n) | `Arrays.toString(nums)` | Debug output | Logs/diagnostics |
| ⭐⭐⭐ | `deepToString(a)` | `String` | Render nested object arrays | O(total visited elements) | `Arrays.deepToString(grid)` | Inspect matrix | Logs/diagnostics |
| ⭐⭐⭐⭐ | `asList(T... a)` | `List<T>` | Fixed-size list backed by object array | O(1) view creation | `Arrays.asList(names)` | Quick list bridge | Adapt array to `List` API |
| ⭐⭐⭐ | `stream(a)` / range overload | `IntStream`, `LongStream`, `DoubleStream`, or `Stream<T>` | Create stream over supported arrays | O(1) to create; terminal work varies | `Arrays.stream(nums).sum()` | Occasional sum/conversion | Pipeline integration |
| ⭐⭐ | `compare(a, b)` | `int` | Lexicographic comparison | O(d) | `Arrays.compare(a, b)` | Lexicographic ordering | Compare sequences |
| ⭐⭐ | `mismatch(a, b)` | `int` | First differing index, or `-1` | O(d) | `Arrays.mismatch(a, b)` | Locate difference | Diagnose changed data |
| ⭐⭐ | `setAll(a, generator)` | `void` | Generate each element from its index | O(n) | `Arrays.setAll(nums, i -> i * 2)` | Initialize formula-based arrays | Build lookup tables |
| ⭐⭐ | `parallelSort(a)` | `void` | Parallel sorting, including range overloads | O(n log n) work; runtime varies | `Arrays.parallelSort(nums)` | Rare in interviews | Large in-memory batch data |
| ⭐ | `parallelSetAll(a, generator)` | `void` | Parallel index-based generation | O(n) work; runtime varies | `Arrays.parallelSetAll(nums, i -> i)` | Rare | Large independent computations |
| ⭐ | `parallelPrefix(a, op)` | `void` | Replace elements with inclusive cumulative results | O(n) work; runtime varies | `Arrays.parallelPrefix(nums, Integer::sum)` | Usually write prefix loops | Large associative scans |

Important overload and behavior details:

- `sort` changes the supplied array. Primitive arrays are sorted in ascending numerical order; object arrays need natural ordering or a comparator. Object-array sorting is stable. `boolean[]` has no `sort` overload. Range endpoints use **from inclusive, to exclusive**.
- A comparator overload works for object arrays, not `int[]`; to sort numbers descending, use `Integer[]` with a comparator or sort an `int[]` ascending and reverse it. Do not implement a comparator as `a - b`, which can overflow; use `Integer.compare(a, b)`.
- `binarySearch` requires the searched range to be sorted **using the same order/comparator**. If absent, it returns `-(insertionPoint) - 1`; a nonnegative result is a found index. For duplicate keys, the particular matching index is unspecified. Searching an unsorted array gives an undefined result.
- `copyOf` and `copyOfRange` create new arrays and pad extra positions with the element type's default. `copyOfRange` permits `to` beyond the source length but rejects invalid `from`/reversed ranges.
- `deepEquals` and `deepToString` are for arrays whose elements are themselves arrays, such as `int[][]`. `Arrays.equals` and `Arrays.toString` only handle the outer level.
- `Arrays.stream` has overloads for `int[]`, `long[]`, `double[]`, and object arrays, **not** every primitive type; it does not mutate the source by itself.
- `setAll`, `parallelSetAll`, and `parallelPrefix` likewise support `int[]`, `long[]`, `double[]`, and object arrays. The `setAll` generators receive an index; `parallelPrefix` instead combines values with a binary operator. `fill`, by contrast, also supports other primitive arrays such as `boolean[]`.
- `compare` returns a negative number, zero, or a positive number for lexicographic order; do not assume the negative/positive result is exactly `-1`/`1`. `mismatch` returns the first differing index, `-1` for equal arrays, or the shorter length when one is a prefix of the other.
- `Arrays.fill` on an object array writes the **same reference** to every selected slot; it does not create a distinct object per slot.
- `parallelSort`, `parallelSetAll`, and `parallelPrefix` have overhead. `parallelSetAll` generators should be safe to run concurrently. `parallelPrefix` requires an associative operation; addition is a common example. For normal interview problems, simple loops are easier to explain.

```java
int[] sorted = {2, 4, 6};
int found = Arrays.binarySearch(sorted, 4);   // 1
int missing = Arrays.binarySearch(sorted, 5); // -3: insertion point 2
int insertionPoint = -missing - 1;             // 2
```

The `Arrays` table covers the practical method families, not every overload. See the [Java SE 21 `Arrays` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) for exact signatures.

# 7. Arrays and lists are different

An array has indexed access, the `length` field, and an inherited `clone()` method, but it has **no** `add()`, `remove()`, `contains()`, `size()`, `get()`, or `set()` methods. Those names belong to collection APIs such as `List`, `ArrayList`, and `Set`. Use `array[i]` and `array.length`; use `list.get(i)` and `list.size()`.

## `Arrays.asList()` caveats

```java
String[] arr = {"A", "B", "C"};
java.util.List<String> list = Arrays.asList(arr);
list.set(0, "X"); // allowed: arr[0] is now "X"
arr[1] = "Y";     // list.get(1) is now "Y"
// list.add("D") would throw UnsupportedOperationException at runtime.
```

The returned list has a **fixed size** and is backed by the same array. You may replace elements with `set`, but `add`/`remove` cannot change its size. For a resizable independent list, use `new ArrayList<>(Arrays.asList(arr))`.

```java
int[] nums = {1, 2, 3};
java.util.List<int[]> oneElement = Arrays.asList(nums);
// oneElement.size() is 1; its only element is the entire int[].
```

`int[]` is one reference-type argument to the generic varargs method; Java does not box its three elements into `Integer` objects. This is **not** a `List<Integer>`.

## Common conversions

```java
String[] arr = {"A", "B"};
java.util.List<String> fixedView = Arrays.asList(arr); // backed by arr
java.util.List<String> resizable = new java.util.ArrayList<>(fixedView);
String[] back = resizable.toArray(new String[0]);

int[] primitives = {1, 2, 3};
java.util.List<Integer> boxed = Arrays.stream(primitives).boxed().toList();
int[] unboxed = boxed.stream().mapToInt(Integer::intValue).toArray();
```

`Stream.toList()` returns an unmodifiable list. Use `new ArrayList<>(...)` if it must grow. Boxing creates `Integer` objects; for performance-sensitive numeric work, prefer `int[]`. The stream examples are conversion idioms, not a requirement to learn the Stream API here.

> **Backend note:** Arrays are mutable even when their reference is `final`. If a class needs to keep ownership of a `byte[]` received from a caller or returned from a getter, a defensive `Arrays.copyOf` can prevent unexpected external changes. `Arrays.equals` compares byte contents; `==` compares array identity.

# 8. Multidimensional and jagged arrays

Java's `int[][]` is an **array of `int[]` references**, not one rectangular primitive block. `new int[3][4]` creates an outer array with three rows, each of length four. `matrix.length` is the row count; `matrix[r].length` is that row's column count.

```java
int[][] matrix = new int[3][4];
matrix[1][2] = 7;
for (int row = 0; row < matrix.length; row++) {
    for (int col = 0; col < matrix[row].length; col++) {
        System.out.println(matrix[row][col]);
    }
}
```

```java
int[][] values = {
    {1, 2, 3},
    {4, 5, 6}
};
int bottomLeft = values[1][0]; // 4
```

Rows can have different lengths; this is a **jagged array**. With `new int[3][]`, row slots initially contain `null`, so allocate them before indexing.

```java
int[][] jagged = new int[3][];
jagged[0] = new int[2];
jagged[1] = new int[5];
jagged[2] = new int[1];
```

Always use `matrix[row].length` in a row traversal. A row can be `null` if it has not been assigned.

# 9. Common array operations

The methods below are valid together as one class. Unless stated otherwise, a scan uses O(1) extra space. For `max`, `min`, and `average`, an empty array needs a policy; these methods reject it. The sum uses `long` to reduce overflow risk, though sufficiently large sums can still overflow `long`.

```java
import java.util.Arrays;

class ArrayRecipes {
    static int max(int[] a) {                         // O(n)
        if (a.length == 0) throw new IllegalArgumentException("empty array");
        int best = a[0];
        for (int value : a) best = Math.max(best, value);
        return best;
    }

    static int min(int[] a) {                         // O(n)
        if (a.length == 0) throw new IllegalArgumentException("empty array");
        int best = a[0];
        for (int value : a) best = Math.min(best, value);
        return best;
    }

    static long sum(int[] a) {                        // O(n)
        long total = 0;
        for (int value : a) total += value;
        return total;
    }

    static double average(int[] a) {                  // O(n)
        if (a.length == 0) throw new IllegalArgumentException("empty array");
        return (double) sum(a) / a.length;
    }

    static void swap(int[] a, int i, int j) {         // O(1)
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    static void reverse(int[] a) {                    // O(n)
        for (int left = 0, right = a.length - 1; left < right; left++, right--) {
            swap(a, left, right);
        }
    }

    static int count(int[] a, int target) {           // O(n)
        int matches = 0;
        for (int value : a) if (value == target) matches++;
        return matches;
    }

    static int indexOf(int[] a, int target) {         // O(n)
        for (int i = 0; i < a.length; i++) {
            if (a[i] == target) return i;
        }
        return -1;
    }

    static int[] merge(int[] a, int[] b) {            // O(a.length + b.length)
        int[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    static void rotateRightByOne(int[] a) {          // O(n)
        if (a.length < 2) return;
        int last = a[a.length - 1];
        for (int i = a.length - 1; i > 0; i--) a[i] = a[i - 1];
        a[0] = last;
    }

    static void rotateRight(int[] a, int k) {        // O(n) time, O(1) extra space
        if (a.length == 0) return;
        k = Math.floorMod(k, a.length);
        reverseRange(a, 0, a.length - 1);
        reverseRange(a, 0, k - 1);
        reverseRange(a, k, a.length - 1);
    }

    private static void reverseRange(int[] a, int left, int right) {
        while (left < right) swap(a, left++, right--);
    }

    static int[] uniqueSorted(int[] a) {             // O(n log n) overall
        int[] copy = a.clone();
        Arrays.sort(copy);
        if (copy.length == 0) return copy;
        int write = 1;
        for (int read = 1; read < copy.length; read++) {
            if (copy[read] != copy[write - 1]) copy[write++] = copy[read];
        }
        return Arrays.copyOf(copy, write);
    }
}
```

`uniqueSorted` removes duplicates by sorting a copy, then compacting distinct values; it **changes order** to sorted order and uses O(n) extra space. If input is already sorted, the compaction alone is O(n). A `HashSet` can preserve only uniqueness, with expected O(n) time and O(n) space; preserving encounter order requires a suitable ordered set or output buffer. A fixed array cannot actually shrink, so return a shorter copy or track a logical count.

For an arbitrary right rotation by `k`, the three reversals above take O(n) time and O(1) extra space. `Math.floorMod` also handles negative `k` consistently; the zero-length guard prevents division by zero.

# 10. Time complexity at a glance

These costs assume an array of `n` elements and, for insertion/deletion, a logical element count plus enough capacity or a replacement array. Java arrays do not directly grow or delete slots.

| Operation | Time complexity | Why |
| --- | ---: | --- |
| Access by index | O(1) | Direct indexed location plus bounds check |
| Modify by index | O(1) | One indexed write |
| Search unsorted array | O(n) | May inspect every element |
| Search sorted array with binary search | O(log n) | Halves the candidate interval each step |
| Insert at beginning | O(n) | Shift/copy existing elements right |
| Insert at end if fixed array requires copy | O(n) | Allocate and copy old elements |
| Delete from middle | O(n) | Shift later elements left or copy them |
| Traverse | O(n) | Visit each element |
| Sort | Usually O(n log n) | Comparison-based library sorting; details depend on type/implementation |
| Copy | O(n) | Each copied element is read and written |

Appending into a spare slot with a separate logical count is O(1); it is the need to **grow** a full array that makes appending O(n). Deleting the last logical element can be O(1) if only the count changes. Remember to clear an unused reference slot if you want its object to be eligible for garbage collection.

# 11. Array versus `ArrayList`

| Aspect | Array, e.g. `int[]` | `ArrayList<Integer>` |
| --- | --- | --- |
| Size | Fixed after creation | Grows and shrinks automatically |
| Performance | Direct primitive storage and indexed access | Indexed access O(1); resizing and boxing can add cost |
| Primitive support | Yes | No; uses wrappers such as `Integer` |
| Memory | Compact for primitive values | Backing reference array, wrapper objects, spare capacity |
| Methods | Indexing, `length`, `clone`; utilities in `Arrays` | `add`, `remove`, `get`, `set`, `size`, etc. |
| Resizing | Allocate/copy a new array | Managed internally, usually amortized O(1) append |
| Access | `a[i]`: O(1) | `list.get(i)`: O(1) |
| Insert/delete | Shift/copy: O(n) | Middle insertion/deletion: O(n) |
| DSA usage | Frequent: input, indices, matrices, numeric buffers | Useful when result size changes |
| Backend usage | Good for fixed data, binary buffers, primitive work | Good for variable-size collections and APIs |

Choose `int[]` for fixed-size numeric data, tight loops, or array-based interview inputs. Choose `ArrayList<Integer>` when the number of items changes and list operations matter. The wrapper cost may matter for large numeric workloads; avoid assuming one is always faster without measuring the actual task.

# 12. Arrays in DSA

Arrays make positions explicit and support O(1) indexed access, so many techniques build on them:

| Pattern | How arrays fit |
| --- | --- |
| Linear scan | Visit each index once for counts, extrema, or validation |
| Two pointers | Move two indexes through one sorted array or two arrays |
| Sliding window | Maintain a contiguous interval with moving boundaries |
| Prefix sum | Store cumulative totals for fast range-sum queries |
| Binary search | Repeatedly probe the middle of a sorted array or answer range |
| Sorting | Rearrange elements to expose order, duplicates, or pair structure |
| Frequency counting | Use values as indexes when the domain is small and known |
| Hashing alongside arrays | Map values to positions/counts while scanning |
| Kadane's algorithm | Track the best subarray ending at each position using a scan |
| Matrix traversal | Work over rows and columns, respecting each row's length |

> **DSA note:** Before coding, ask whether you need original order, whether the data is sorted, whether duplicate values exist, and whether the array may be empty. These determine the right technique and edge cases.

# 13. Common mistakes and interview checks

| Mistake | Correct approach |
| --- | --- |
| `i <= nums.length` in a normal forward scan | Use `i < nums.length`; last index is `length - 1` |
| Calling `nums.length()` | Use field `nums.length` |
| Using `a == b` for contents | Use `Arrays.equals`, or `deepEquals` for nested arrays |
| Assuming an array grows on assignment | Allocate a larger array or use `ArrayList` |
| Editing an alias unexpectedly | Copy before mutation when independent data is required |
| Confusing inclusive and exclusive ends | Most range APIs here use `[from, to)` |
| Binary-searching unsorted data | Sort first and search with the matching order |
| Treating `Arrays.asList(int[])` as `List<Integer>` | Box elements explicitly |
| Treating `int[]` and `Integer[]` as interchangeable | Primitive array stores values; wrapper array stores nullable references |
| Dereferencing object-array slots before filling them | Check/create each object; default is `null` |
| Taking max/average of empty array without a policy | Handle empty input explicitly |

> **Common mistake — intentionally incorrect Java:** `nums.length()` is not a method call supported by arrays. `nums.add(4)` is also invalid because arrays have no `add` method.

## Short conceptual interview questions

| Question | Short answer |
| --- | --- |
| Are Java arrays objects? | Yes. An array variable holds a reference to an array object. |
| Can an array contain `null`? | A reference-type array can; its elements initially are `null`. The array variable itself can also be `null`. |
| Can a primitive array contain `null`? | No; `int[]` elements are `int` values. |
| Can an array change size? | No. Make a new array to change capacity. |
| What is the default value of an `int[]`? | If asking about **elements** of `new int[n]`, each is `0`. An uninitialized local `int[]` variable has no usable value; a field defaults to `null`. |
| `int[]` versus `Integer[]`? | Primitive values versus nullable `Integer` references; the latter may involve boxing and more memory. |
| What happens at an invalid index? | `ArrayIndexOutOfBoundsException`. |
| `==` versus `Arrays.equals`? | Same object identity versus one-dimensional element equality. |
| Why is indexed access O(1)? | The runtime computes an element location directly from the reference and index. |
| Does `clone()` deeply copy an object array? | No. Its elements remain references to the same objects. |
| Does `Arrays.binarySearch` return the first duplicate? | No; any matching index may be returned. |

# 14. LeetCode syntax cheat sheet

Keep these imports and statements handy. In a problem method, assume `n` is nonnegative and use one declaration per alternative creation style.

```java
import java.util.Arrays;

class ArrayCheatSheet {
    static void demo(int n) {
        int[] nums = new int[n];
        int[] values = {1, 2, 3};
        int length = values.length;
        int first = values[0];

        for (int i = 0; i < values.length; i++) {
            System.out.println(values[i]);
        }
        for (int value : values) {
            System.out.println(value);
        }

        Arrays.sort(values);
        Arrays.fill(nums, -1);
        int[] copy = Arrays.copyOf(values, values.length);
        int[] slice = Arrays.copyOfRange(values, 0, 2);
        boolean equal = Arrays.equals(values, copy);
        int index = Arrays.binarySearch(values, 2); // values must be sorted
        System.out.println(Arrays.toString(values));

        int[][] matrix = new int[2][3];
        int rows = matrix.length;
        int cols = matrix[0].length;
    }
}
```

# What Should I Memorize?

### ⭐⭐⭐⭐⭐ Must Know

- Declaration and creation (`int[] a = new int[n]`, `{...}`), defaults, zero-based bounds, `a.length`, and indexed loops.
- Primitive values versus object references, `null`, aliasing, and copy versus shared reference.
- `Arrays.sort`, `Arrays.equals`, `Arrays.copyOf`, `Arrays.toString`; O(1) access and O(n) scans/copies.

### ⭐⭐⭐⭐ Very Important

- `Arrays.binarySearch` precondition and negative insertion-point result; `Arrays.fill`, `copyOfRange`, and `System.arraycopy`.
- Two-dimensional arrays as arrays of rows; use `matrix[row].length`.
- Array versus `ArrayList`, fixed-size `Arrays.asList` view, and common list conversions.
- Common DSA patterns: two pointers, sliding window, prefix sums, and frequency arrays.

### ⭐⭐⭐ Useful

- `clone` and its shallow-copy behavior; `deepEquals`/`deepToString` for nested arrays.
- `Arrays.stream` for simple numeric conversions, custom object-array sorting, and rotation/deduplication strategies.

### ⭐⭐ Low Priority for Now

- `Arrays.compare`, `mismatch`, `setAll`, and `parallelSort` until a real problem calls for them.

### ⭐ Rarely Needed for Now

- `Arrays.parallelSetAll` and `parallelPrefix`; learn their contracts when handling large parallel array workloads.

For exact method contracts, consult the [Java SE 21 `Arrays` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html) and [`System.arraycopy` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html#arraycopy(java.lang.Object,int,java.lang.Object,int,int)).
