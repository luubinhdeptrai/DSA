# Java `List`, `ArrayList`, and `LinkedList`: A Practical Reference

This guide uses Java 21. Examples assume `import java.util.*;` unless a complete class includes its imports. Short code blocks are method-body examples; each block stands on its own. Complexity uses `n` for list size and `m` for another collection's size. A method's availability through `List` does **not** mean it has the same performance for every implementation.

# 1. Collections Framework and `List`

The Java Collections Framework supplies interfaces, implementations, and utility algorithms for groups of objects.

```text
Collection<E> (interface)
    └── List<E> (interface: ordered, indexed sequence)
          ├── ArrayList<E>  (resizable-array implementation)
          └── LinkedList<E> (doubly linked implementation; also Deque<E>)
```

| Name | What it is | Example role |
| --- | --- | --- |
| `Collection<E>` | Base interface for many groups of elements | Common `add`, `remove`, `size` contracts |
| `Collections` | Utility class with static algorithms | `Collections.reverse(list)` |
| `List<E>` | Interface for an ordered, zero-indexed sequence | API parameter/return type |
| `ArrayList<E>` | Resizable-array class implementing `List` | Default mutable list in many cases |
| `LinkedList<E>` | Doubly linked class implementing `List` and `Deque` | End operations and sequential traversal |

`CopyOnWriteArrayList` is another specialized implementation for certain read-heavy concurrent use cases; it is outside this introductory guide. `List` is an **interface**, so `new List<>()` is invalid: instantiate a class or use a factory such as `List.of(...)`.

`<E>` stands for the element type. `List<String> names;` declares a variable that can refer to a list of strings, but does not create one. Lists preserve positional order and normally allow duplicates. Whether `null` is allowed depends on the implementation or factory: `ArrayList` and `LinkedList` allow it; `List.of` does not. Indexes begin at zero.

```java
List<String> names = new ArrayList<>();
names.add("Ada");
names.add("Ada"); // duplicates are allowed
String first = names.get(0);
```

## Interface type versus implementation type

```java
List<String> names = new ArrayList<>(); // usual choice
ArrayList<String> specific = new ArrayList<>(); // also valid
```

Declaring the variable or public method as `List<String>` lets callers and implementations change between `ArrayList`, `LinkedList`, or another list without changing code that only needs the `List` contract. This is the practical meaning of **program to the interface, not the implementation**: it improves flexibility and maintainability in backend service APIs and tests. Use `ArrayList<String>` as the declared type when you specifically need `ensureCapacity()`, `trimToSize()`, or another class-specific operation. Choosing an interface does **not** make a mutable list immutable or thread-safe.

## Creating lists: mutable, fixed size, and unmodifiable

```java
List<Integer> growable = new ArrayList<>();
List<Integer> reserved = new ArrayList<>(10);       // size is still 0
List<Integer> fixed = Arrays.asList(1, 2, 3);      // fixed-size view
List<Integer> unmodifiable = List.of(1, 2, 3);    // Java 9+
List<Integer> editableCopy = new ArrayList<>(List.of(1, 2, 3));
```

| Feature | `List.of(...)` | `Arrays.asList(...)` | `new ArrayList<>()` |
| --- | --- | --- | --- |
| Can replace element with `set`? | No | Yes | Yes |
| Can add? | No | No | Yes |
| Can remove? | No | No | Yes |
| Allows `null`? | No | Yes | Yes |
| Size changeable? | No | No | Yes |
| Backed by caller's array? | No | Yes, if an array is passed | No |
| Typical use | Small read-only value list | Bridge object array to `List` | Mutable working list |
| Java version | 9+ | Long-standing | Long-standing |

`List.of` is **unmodifiable**: `set`, `add`, and `remove` throw `UnsupportedOperationException`; construction with a null element throws `NullPointerException`. Its elements can themselves be mutable objects. `Arrays.asList(array)` is a fixed-size **view backed by the same object array**: `set` and array element assignments are visible from both sides, but size-changing methods throw. `new ArrayList<>(source)` makes a resizable, independent **container**, though its element references are shallow-copied. `List.copyOf(source)` (Java 10+) makes an unmodifiable, null-rejecting list; it is also shallow and may reuse an already unmodifiable source.

> **Common mistake:** `Arrays.asList(intArray)` produces a one-element `List<int[]>`, not a `List<Integer>`. Primitive-array conversion is shown below.

# 2. Core `List` methods

The following table rates methods for DSA, interviews, general Java, and backend work. Unless noted, mutation requires a modifiable list. For complexity, `A` means `ArrayList`, `L` means `LinkedList`; `k` is a copied/changed range. Search uses element equality and can be affected by expensive `equals` implementations.

| Priority | Method | Return Type | Purpose | Typical Complexity | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `add(E e)` | `boolean` | Append | A amortized O(1); L O(1) | `list.add(7)` | Collect results | Accumulate DTOs |
| ⭐⭐⭐⭐ | `add(int i, E e)` | `void` | Insert, shift later positions | A O(n); L O(n) indexed | `list.add(0, 7)` | Ordered edits | Insert item |
| ⭐⭐⭐ | `addAll(c)` / `addAll(i,c)` | `boolean` | Append/insert many | End O(m) amortized A / O(m) L; indexed O(n+m) | `list.addAll(other)` | Merge groups | Batch assembly |
| ⭐⭐⭐⭐⭐ | `get(int i)` | `E` | Read index | A O(1); L O(n) | `list.get(0)` | Indexed algorithms | Read item |
| ⭐⭐⭐⭐ | `set(int i, E e)` | `E` old value | Replace at index | A O(1); L O(n) | `list.set(0, 9)` | Update answer | Update value |
| ⭐⭐⭐⭐⭐ | `remove(int i)` | `E` removed | Remove index | A O(n), last O(1); L O(n), ends O(1) | `list.remove(0)` | Delete position | Edit list |
| ⭐⭐⭐⭐ | `remove(Object o)` | `boolean` | Remove first equal value | O(n) search, plus shifting for A | `list.remove(Integer.valueOf(7))` | Delete value | Remove match |
| ⭐⭐ | `removeAll(c)` | `boolean` | Remove members found in `c` | Depends on membership; often O(n·m) | `list.removeAll(blocked)` | Set-like cleanup | Prune values |
| ⭐⭐⭐⭐ | `removeIf(predicate)` | `boolean` | Remove matches safely | Usually O(n) plus predicate cost | `list.removeIf(x -> x < 0)` | Filter in place | Drop invalid rows |
| ⭐⭐⭐⭐ | `clear()` | `void` | Remove every element | O(n) typical | `list.clear()` | Reset state | Reset buffer |
| ⭐⭐⭐⭐⭐ | `size()` | `int` | Element count | O(1) | `list.size()` | Loop bounds | Pagination/count |
| ⭐⭐⭐⭐ | `isEmpty()` | `boolean` | Test size zero | O(1) | `list.isEmpty()` | Edge cases | Empty result |
| ⭐⭐⭐⭐ | `contains(o)` | `boolean` | Membership test | O(n) | `list.contains(7)` | Small membership | Validation |
| ⭐⭐ | `containsAll(c)` | `boolean` | Every supplied value present? | Often O(n·m) | `list.containsAll(required)` | Occasional | Validation |
| ⭐⭐⭐⭐ | `indexOf(o)` | `int` | First position or `-1` | O(n) | `list.indexOf(7)` | Locate value | Find item |
| ⭐⭐ | `lastIndexOf(o)` | `int` | Last position or `-1` | O(n) | `list.lastIndexOf(7)` | Duplicate lookup | Occasional |
| ⭐⭐⭐ | `subList(from,to)` | `List<E>` view | Select `[from,to)` | View creation usually O(1); later costs vary | `list.subList(1, 3)` | Work on range | Slice data |
| ⭐⭐⭐⭐ | `sort(comparator)` | `void` | Sort in place | O(n log n) typical | `list.sort(Comparator.naturalOrder())` | Ordering | Order DTOs |
| ⭐⭐ | `replaceAll(operator)` | `void` | Replace each element | O(n) plus operator cost | `list.replaceAll(x -> x + 1)` | Transform | Normalize data |
| ⭐⭐⭐ | `toArray()` / `toArray(T[])` | `Object[]` / `T[]` | Make array copy | O(n) | `list.toArray(new String[0])` | Interop | API bridge |
| ⭐⭐⭐⭐ | `iterator()` | `Iterator<E>` | Sequential cursor | O(1) creation, O(n) traversal | `list.iterator()` | Safe removal | Traversal |
| ⭐⭐⭐ | `listIterator()` / `(i)` | `ListIterator<E>` | Bidirectional cursor | O(1) start A; O(n) to index L | `list.listIterator()` | Sequential edits | Occasional |
| ⭐⭐⭐ | `forEach(action)` | `void` | Visit elements | O(n) plus action | `list.forEach(System.out::println)` | Simple visit | Logging/processing |
| ⭐⭐⭐⭐ | `equals(o)` | `boolean` | Same ordered elements | O(n) worst case | `a.equals(b)` | Check answers | Compare results |
| ⭐⭐ | `hashCode()` | `int` | Ordered content hash | O(n) typical | `list.hashCode()` | Rare directly | Map/set behavior |

`subList` is a **backed view**, not an independent copy: changing it changes the parent list. Structural changes to the parent outside the view can make later view behavior undefined. Copy it with `new ArrayList<>(list.subList(from, to))` when independent storage is needed.

## `add`, `get`, `set`, and `remove`

```java
List<Integer> nums = new ArrayList<>();
boolean added = nums.add(10); // true for ArrayList
nums.add(20);
nums.add(1, 15);             // [10, 15, 20]; valid insertion index 0..size
int value = nums.get(1);     // 15; valid read index 0..size-1
int old = nums.set(1, 99);   // old=15; [10, 99, 20], size unchanged
int removed = nums.remove(1); // removed=99; [10, 20]
```

`add(E)` returns whether the list changed; `add(index,E)` returns `void`. In an `ArrayList`, indexed insertion shifts later references, hence O(n); in a `LinkedList`, it must first locate the index, usually O(n). Invalid indexes throw `IndexOutOfBoundsException`. `set` **replaces** and returns the previous element; `add(index, value)` **inserts** and increases size. Calling `set(0, x)` on an empty list fails.

**`ArrayList.get(i)` is O(1); `LinkedList.get(i)` is O(n).** The former indexes a backing array. The latter walks links from the nearer end to reach a node. Avoid repeatedly calling `get(i)` in a `LinkedList` loop.

## The two `remove` overloads

```java
List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
Integer byIndex = nums.remove(1);               // removes 20 at index 1
boolean byValue = nums.remove(Integer.valueOf(30)); // removes value 30
```

With `List<Integer>`, the `int` argument in `remove(1)` selects `remove(int index)`, **not** removal of the integer value 1. `remove(Object)` removes the first equal element and returns `true` if one was found. `Integer.valueOf(1)` selects that overload. This is a common interview trap.

## Search and size syntax

`contains`, `indexOf`, and `lastIndexOf` scan until a match; they are O(n) for both implementations. The index methods return `-1` if absent. `List.size()` returns the number of elements and is O(1) for `ArrayList` and `LinkedList`.

| Type | Syntax |
| --- | --- |
| Array | `array.length` |
| String | `string.length()` |
| List | `list.size()` |

## Traversal and iterators

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}
for (Integer value : list) {
    System.out.println(value);
}
Iterator<Integer> iterator = list.iterator();
while (iterator.hasNext()) {
    Integer value = iterator.next();
    System.out.println(value);
}
list.forEach(System.out::println);
```

Use an indexed loop when positions matter **and** indexed access is fast (usually `ArrayList`). Enhanced `for` and `forEach` are concise for read-only traversal. Use an `Iterator<E>` when sequential traversal needs supported removal; a `ListIterator<E>` additionally moves backward and can insert or replace elements. Iteration over a `LinkedList` is O(n) total; `get(i)` for every `i` can be O(n²).

```text
Before next(): cursor | [A] [B] [C]
After next():  returned A; cursor [A] | [B] [C]
```

`hasNext()` checks availability. `next()` returns the next element **and advances** the cursor, so there is no `iterator++`. Calling `next()` when exhausted throws `NoSuchElementException`. `iterator.remove()` removes the last element returned by `next()`; call it at most once per `next()`.

```java
List<Integer> values = new ArrayList<>(List.of(3, -1, 4, -2));
Iterator<Integer> it = values.iterator();
while (it.hasNext()) {
    if (it.next() < 0) it.remove(); // [3, 4]
}
values.removeIf(x -> x % 2 == 0); // [3]
```

Structural changes through `list.remove(...)` during enhanced `for` iteration can trigger `ConcurrentModificationException` in these implementations. Fail-fast detection is best-effort, not a correctness mechanism. Use the iterator's own `remove` when cursor control matters, or `removeIf` for a simple predicate. Replacing an existing element is a different operation from structural add/remove, but follow the iterator's documented rules when editing during traversal.

> **Runtime mistake (valid Java, unsafe iteration):** In `for (Integer value : list) { if (value < 0) list.remove(value); }`, the enhanced `for` uses an iterator while `list.remove` structurally changes the list outside that iterator. The safe alternatives are shown above.

## Sorting, reversing, min, and max

```java
List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
Collections.sort(list);                         // [1, 2, 3]
list.sort(Comparator.reverseOrder());          // [3, 2, 1]
list.sort(Comparator.naturalOrder());          // [1, 2, 3]
Collections.sort(list, Collections.reverseOrder()); // [3, 2, 1]
Collections.reverse(list);                     // [1, 2, 3]
int smallest = Collections.min(list);          // 1
int largest = Collections.max(list);           // 3
```

`list.sort(comparator)` is the clear modern style; `Collections.sort` remains valid and delegates to `List.sort` in modern Java. Natural ordering uses `null` as comparator (or `Comparator.naturalOrder()`); reverse ordering uses `Comparator.reverseOrder()`. Sorting is approximately O(n log n), mutates the list, and requires replacement support. `Collections.reverse` is O(n); `min` and `max` scan in O(n) and throw `NoSuchElementException` on an empty list. An unmodifiable `List.of(...)` cannot be sorted or reversed in place; copy it first.

## Array and list conversions

```java
String[] names = {"Ada", "Lin"};
List<String> backedView = Arrays.asList(names);
List<String> growable = new ArrayList<>(backedView);
String[] back = growable.toArray(new String[0]);

int[] primitives = {1, 2, 3};
List<Integer> boxed = Arrays.stream(primitives).boxed().toList(); // unmodifiable
int[] unboxed = boxed.stream().mapToInt(Integer::intValue).toArray();
```

`int[]` contains primitive values; `List<Integer>` contains references to wrapper objects. Boxing/unboxing adds work and may use more memory; unboxing a null `Integer` throws `NullPointerException`. `Stream.toList()` returns an unmodifiable list (Java 16+); wrap it in `new ArrayList<>(...)` if mutation is needed. For performance-sensitive numeric DSA, prefer `int[]` when its fixed size fits the task.

# 3. `ArrayList`: the usual mutable list

`ArrayList<E>` implements `List<E>` using a dynamically resized **array of references**. It preserves insertion order, allows duplicates and `null`, and offers fast random access. The objects referred to by its elements do not have to live next to each other.

```text
size = 3, capacity >= 3
backing slots: [A] [B] [C] [spare] [spare] ...
                0   1   2
```

`size()` counts actual elements. **Capacity** is the available backing-array slot count; it is an internal detail at least as large as size. `new ArrayList<>(100)` creates an **empty list** with room for about 100 references: `size()` is `0`, and `get(0)` still throws. A negative initial capacity throws `IllegalArgumentException`.

```java
ArrayList<Integer> list = new ArrayList<>(100);
int count = list.size(); // 0, not 100
list.add(5);             // now size 1
```

When the backing array fills, the list allocates a larger array and copies references. The exact growth factor is an implementation detail. Most appends just write one spare slot, so `add(e)` is **amortized O(1)**: an occasional O(n) resize is spread across many cheap appends. Think of paying a little extra across each append to cover rare copying. Inserting at the front or middle shifts elements, so it is O(n) even when there is spare capacity.

Specify an initial capacity when a reasonable element-count estimate is known, such as collecting an expected batch of database rows; this can reduce reallocations. Do not reserve a huge capacity without a reason, because spare slots consume memory. `new ArrayList<>()` is a good default when size is unknown.

## ArrayList-specific methods

| Priority | Method | What it does | Practical use |
| --- | --- | --- | --- |
| ⭐⭐ | `ensureCapacity(n)` | Reserves room for at least `n` elements | Known large batch; avoids some growth copies |
| ⭐ | `trimToSize()` | Shrinks capacity toward current size | Rare memory tuning after final build |
| ⭐ | `clone()` | Returns a shallow copy as `Object` | Rare; `new ArrayList<>(source)` is clearer |

`clone()` copies the list container and element references, not the referenced objects. Capacity is not the logical size and should not be used as an index bound.

## ArrayList complexity

| Operation | Complexity | Why |
| --- | ---: | --- |
| `get(index)` | O(1) | Direct backing-array index |
| `set(index)` | O(1) | Replace one slot |
| `add(element)` | Amortized O(1); O(n) on resize | Usually spare slot; occasional copy |
| `add(index, element)` | O(n) | Shift suffix right |
| Remove last | O(1) | Clear last slot and reduce size |
| `remove(index)` | O(n) generally | Shift suffix left |
| `contains()` | O(n) | Linear scan |
| `indexOf()` | O(n) | Linear scan |
| Iterate all | O(n) | Visit each element |
| Sort | O(n log n) | Comparison sorting; comparator cost extra |

`ArrayList` is usually good for frequent reads, indexed DSA algorithms, appends, and full scans. Its compact reference array generally gives better locality and lower overhead than one node object per element. It is less suited to frequent front or middle edits, but replacing it blindly with `LinkedList` does not guarantee better speed: finding linked positions, allocations, and cache misses can dominate.

# 4. `LinkedList`: linked nodes and deque operations

`LinkedList<E>` implements both `List<E>` and `Deque<E>`. Its conceptual structure is a **doubly linked list**: each node holds an element plus references to previous and next nodes.

```text
head                              tail
 ↓                                 ↓
[prev:null | A | next] ⇄ [B] ⇄ [prev | C | next:null]
```

LinkedList permits duplicates and `null`. Finding `get(5000)` requires walking nodes from the nearer end; it is still O(n) in the list size. Once a specific node/location is reached, relinking neighbors can be O(1), but the public **indexed** add/remove methods generally pay O(n) to find it. End operations use known head/tail links and are O(1).

> **Java 21 note:** `List` gained first/last operations through `SequencedCollection`, so `ArrayList` also exposes names such as `addFirst`. For `ArrayList`, front insertion still shifts O(n) elements. `LinkedList` additionally implements `Deque` and offers `offer`, `peek`, and `poll` variants.

## LinkedList end methods, ranked

| Priority | Method | Return Type | On empty list | Use |
| --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐ | `addFirst(e)` | `void` | Adds element | Prepend, deque |
| ⭐⭐⭐⭐ | `addLast(e)` | `void` | Adds element | Append, queue |
| ⭐⭐⭐ | `getFirst()` | `E` | Throws `NoSuchElementException` | Read head |
| ⭐⭐⭐ | `getLast()` | `E` | Throws `NoSuchElementException` | Read tail |
| ⭐⭐⭐⭐ | `removeFirst()` | `E` | Throws `NoSuchElementException` | Pop head |
| ⭐⭐⭐⭐ | `removeLast()` | `E` | Throws `NoSuchElementException` | Pop tail |
| ⭐⭐⭐ | `peekFirst()` | `E` or `null` | Returns `null` | Optional head read |
| ⭐⭐⭐ | `peekLast()` | `E` or `null` | Returns `null` | Optional tail read |
| ⭐⭐⭐ | `pollFirst()` | `E` or `null` | Returns `null` | Optional head removal |
| ⭐⭐⭐ | `pollLast()` | `E` or `null` | Returns `null` | Optional tail removal |
| ⭐⭐ | `offerFirst(e)` | `boolean` | Inserts, normally `true` here | Deque-style prepend |
| ⭐⭐⭐ | `offerLast(e)` | `boolean` | Inserts, normally `true` here | Queue-style append |

All these head/tail operations are O(1) on `LinkedList`. `getFirst`/`removeFirst` signal an empty list with an exception; `peekFirst`/`pollFirst` return `null`. The same distinction applies at the last end. Because `LinkedList` permits actual `null` elements, a null returned by `peek`/`poll` can be ambiguous; avoid null queue elements when using those sentinel methods. For ordinary queue/deque workloads, `ArrayDeque` is often a better default because it avoids per-element linked nodes, though that is a separate topic.

```java
LinkedList<Integer> deque = new LinkedList<>();
deque.addFirst(2);
deque.addLast(3);
Integer head = deque.peekFirst();  // 2
Integer tail = deque.pollLast();   // 3
Integer removed = deque.removeFirst(); // 2
Integer empty = deque.pollFirst(); // null
```

## LinkedList complexity

| Operation | Complexity | Why |
| --- | ---: | --- |
| `get(index)` | O(n) | Traverse from nearer end |
| `set(index)` | O(n) | Find node, then replace value |
| Add first | O(1) | Link at head |
| Add last | O(1) | Link at tail |
| Remove first | O(1) | Unlink head |
| Remove last | O(1) | Unlink tail |
| Add by index | O(n) generally; O(1) at ends | Locate position, then link |
| Remove by index | O(n) generally; O(1) at ends | Locate node, then unlink |
| Search | O(n) | Follow links until match |
| Iterate all | O(n) | Advance node by node |

The O(1) claim for linked insertion/removal applies **after the location is already known**, such as through a positioned `ListIterator` or at the ends. It does not turn `list.add(n/2, x)` into O(1).

# 5. Choosing `ArrayList` or `LinkedList`

| Feature | `ArrayList` | `LinkedList` |
| --- | --- | --- |
| Internal structure | Resizable array of references | Doubly linked nodes |
| `get(index)` | O(1) | O(n) |
| `set(index)` | O(1) | O(n) |
| Add at end | Amortized O(1) | O(1) |
| Add at beginning | O(n) shift | O(1) link |
| Insert in middle by index | O(n) shift | O(n) search, O(1) link |
| Remove at beginning | O(n) shift | O(1) unlink |
| Remove in middle by index | O(n) shift | O(n) search, O(1) unlink |
| Memory overhead | Backing array plus spare capacity | Node object and links per element |
| Cache locality | Usually better for sequential slots | Usually poorer due to separate nodes |
| Iteration performance | Usually fast O(n) | O(n), often larger constant costs |
| DSA usefulness | Excellent for indexed/dynamic result work | Useful for end operations or iterator edits |
| Backend usefulness | Common default result container | Specialized cases; deque alternatives exist |
| Typical recommendation | Start here for mutable `List` | Choose for a measured or clear end/iterator use case |

`ArrayList` is commonly preferred because most code reads, appends, and iterates more than it inserts at an arbitrary middle position. Its array gives cheap indexing and often good locality. `LinkedList` can relink quickly **after finding a node**, but indexed access, node allocations, memory overhead, and cache misses often erase that theoretical advantage. Both are mutable and are not synchronized for concurrent modification.

## Six practical scenarios

| Scenario | Likely choice | Why and tradeoff |
| --- | --- | --- |
| Frequent `list.get(i)` | `ArrayList` | O(1) access versus O(n) node traversal |
| Mostly append and iterate | `ArrayList` | Amortized O(1) append and efficient scans |
| Frequent operations at both ends | `LinkedList` if a list is also needed; otherwise consider `ArrayDeque` | Linked ends are O(1); `ArrayList` front edits shift O(n) |
| LeetCode indexed algorithm | `ArrayList` or primitive array | Random access is central; `int[]` avoids boxing if size is fixed |
| Queue/deque behavior | Usually `ArrayDeque`; `LinkedList` when its `List`/null behavior is specifically needed | Both support end operations; `ArrayDeque` often has lower overhead and disallows null |
| Backend service returning DTOs | Return `List<DTO>`, often build with `ArrayList` | Expose a flexible interface; choose mutability/ownership intentionally |

> **Performance note:** Big-O describes growth, not elapsed time for one input. Benchmark representative workloads before choosing `LinkedList` for speed.

# 6. DSA and backend usage

## DSA uses and nested lists

`ArrayList` fits dynamic result collection, adjacency lists, backtracking snapshots, groups, intervals, sorting, and returning multiple answers. `List<Integer>`, `List<String>`, and `List<List<Integer>>` describe element types; each inner list is itself an object.

```java
List<List<Integer>> result = new ArrayList<>();
List<Integer> row = new ArrayList<>();
row.add(1);
row.add(2);
result.add(row);
int value = result.get(0).get(1); // 2

List<Integer> path = new ArrayList<>(List.of(4, 5));
result.add(new ArrayList<>(path)); // snapshot container for backtracking
```

The outer list contains references to inner lists. `result.add(row)` shares `row`: later `row.add(3)` changes what `result.get(0)` sees. For backtracking, add `new ArrayList<>(path)` when each saved path must have independent list structure. The elements inside a copied list remain shared references.

```java
List<List<Integer>> graph = new ArrayList<>();
int vertices = 3;
for (int i = 0; i < vertices; i++) {
    graph.add(new ArrayList<>());
}
graph.get(0).add(2); // an edge from 0 to 2, in a simple adjacency-list model
```

`new ArrayList<>(map.values())` is useful when a map's values need to become a list: it creates an independent list container in the map's value iteration order. The value objects themselves are not copied. This can help when collecting grouped answers or preparing a return value.

```java
Map<String, Integer> counts = new LinkedHashMap<>();
counts.put("A", 1);
counts.put("B", 2);
List<Integer> countValues = new ArrayList<>(counts.values()); // [1, 2]
```

## Backend uses and sorting objects

Backend APIs commonly use `List<Entity>` for repository query results, `List<DTO>` for service or REST response data, `List<String>` for names/tags, and `List<Long>` for IDs. A method accepting or returning `List<User>` depends on the interface contract rather than one storage class. Decide whether callers may edit the returned list; copy or return an unmodifiable list when ownership needs a boundary.

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

record User(String name) {}

class UserServiceExample {
    static List<User> orderedUsers(List<User> input) {
        List<User> users = new ArrayList<>(input); // avoid sorting caller's list
        users.sort(Comparator.comparing(User::name));
        users.sort(Comparator.comparing(User::name).reversed()); // descending example
        return users;
    }
}
```

`Comparator.comparing(User::name)` orders by a field; `.reversed()` reverses that order. The example demonstrates both forms; a real method would choose one. Comparator details belong in their own topic.

## Filtering

```java
List<Integer> mutable = new ArrayList<>(List.of(1, -2, 3));
mutable.removeIf(x -> x < 0); // mutates: [1, 3]
List<Integer> filtered = mutable.stream().filter(x -> x > 1).toList();
```

`removeIf` modifies a mutable list. The stream form creates a result; `Stream.toList()` is unmodifiable in modern Java. A `List` has **no direct `filter()` method**: `filter` belongs to a stream. Learn streams separately when needed.

## Generics, wrappers, and primitive arrays

```java
List<Integer> numbers = new ArrayList<>();
List<String> labels = new ArrayList<>();
numbers.add(5);      // int 5 is autoboxed to Integer
int first = numbers.get(0); // Integer is unboxed to int
```

Generics provide compile-time type checking: a `List<Integer>` normally cannot receive a `String`. Generic type arguments must be reference types, so **`List<int>` is intentionally incorrect Java**; use `List<Integer>`. Autoboxing wraps a primitive and unboxing extracts it. A null `Integer` causes `NullPointerException` when unboxed.

| Aspect | `int[]` | `List<Integer>` |
| --- | --- | --- |
| Storage | Primitive values | References to wrapper objects |
| Size | Fixed | Resizable when implementation permits |
| Memory | Usually compact | Backing references, wrappers, spare capacity |
| Access | `a[i]`, O(1) | `list.get(i)`: A O(1), L O(n) |
| Methods | Array `length`; utilities in `Arrays` | `add`, `remove`, `contains`, `size`, etc. |
| DSA fit | Large numeric/fixed-size input, tight loops | Dynamic outputs and nested structures |
| Convenience | Simple, minimal overhead | Flexible collection API |

Choose `int[]` for fixed-size or performance-sensitive numeric data; choose `ArrayList<Integer>` when growth and list methods outweigh boxing cost.

# 7. References, copying, equality, and null

## Aliases and shallow copies

```java
List<Integer> a = new ArrayList<>(List.of(1, 2));
List<Integer> alias = a;
alias.add(3);
System.out.println(a); // [1, 2, 3]

List<Integer> copy = new ArrayList<>(a);
copy.add(4);
System.out.println(a);    // [1, 2, 3]
System.out.println(copy); // [1, 2, 3, 4]
```

```text
a ─────┐
       ├──> one mutable list [1, 2, 3]
alias ─┘
copy ─────> separate list container [1, 2, 3, 4]
```

`alias = a` copies only the reference. `new ArrayList<>(a)` creates a new list container but **shallow-copies** element references. If elements are mutable objects, editing an object through either list can be visible in both. `List.copyOf(a)` returns an unmodifiable, null-rejecting list snapshot of element references; it is not a deep copy. It may reuse the input if it is already an appropriate unmodifiable list.

## Equality and null

`List.equals` compares **size, element contents, and order**, independent of whether one list is an `ArrayList` and the other a `LinkedList`.

```java
List<String> left = new ArrayList<>(List.of("A", "B"));
List<String> right = new LinkedList<>(List.of("A", "B"));
boolean same = left.equals(right); // true
boolean differentOrder = left.equals(List.of("B", "A")); // false

left.add(null);  // ArrayList allows null
right.add(null); // LinkedList allows null
// List.of("A", null) would throw NullPointerException at runtime.
```

The `List` interface does not guarantee that every implementation accepts null. `ArrayList`/`LinkedList` do; `List.of` and `List.copyOf` reject it. A null list reference is different from a list containing null. Null elements can complicate comparison, sorting, unboxing, and deque `peek`/`poll` results.

# 8. Common mistakes and interview answers

| Mistake | Correct understanding |
| --- | --- |
| Writing `list.length` | Use `list.size()`; array uses `length`, string uses `length()` |
| Calling `get(size)` | Last valid index is `size() - 1`; invalid index throws `IndexOutOfBoundsException` |
| Treating `set(i,x)` as insertion | `set` replaces; `add(i,x)` inserts |
| Using `nums.remove(1)` to remove integer value 1 | It removes index 1; use `remove(Integer.valueOf(1))` |
| Removing through list inside enhanced `for` | Use iterator `remove` or `removeIf` |
| Assuming `LinkedList.get(i)` is O(1) | It traverses nodes: O(n) |
| Assuming linked indexed insertion is O(1) | Finding the indexed node costs O(n) |
| Using raw `List`/`ArrayList` types | Use `List<Integer> list = new ArrayList<>()` for type safety |
| Resizing an `Arrays.asList` view | Fixed size; copy into `ArrayList` |
| Editing a `List.of` list | Unmodifiable; copy into `ArrayList` |
| Treating initial capacity as elements | `new ArrayList<>(100)` has size 0 |
| Indexed loop over `LinkedList` | Repeated `get(i)` can make O(n²) total |

> **Intentionally incorrect Java:** `list.length` and `new List<Integer>()` do not compile. Raw `List list = new ArrayList();` compiles but discards generic type safety and should generally be avoided.

## Short interview questions

| Question | Short answer |
| --- | --- |
| `List` versus `ArrayList`? | Interface contract versus one resizable-array implementation. |
| Is `List` a class or interface? | Interface; instantiate an implementation or use a factory. |
| Why declare `List` variable? | Callers depend on shared operations, so implementation can change. |
| `ArrayList` versus `LinkedList`? | Backing array with O(1) indexing versus linked nodes with O(n) indexing and O(1) end links. |
| How does `ArrayList` grow? | It allocates a larger backing array and copies references when needed; exact growth policy is unspecified. |
| How does `LinkedList` work internally? | Each node links to its previous and next neighbors; head and tail are known. |
| Why is `ArrayList.get` O(1)? | It reads a backing-array slot by index. |
| Why is `LinkedList.get` O(n)? | It must traverse links to the indexed node. |
| What is amortized O(1) append? | Occasional O(n) resizes average out over many O(1) appends. |
| Size versus capacity? | Actual element count versus backing-slot count. |
| Duplicates and null? | Both classes allow duplicates and null; `List.of` rejects null. |
| `add` versus `set`? | Insert/append and grow versus replace existing element. |
| Two `remove` overloads? | `remove(int)` removes index; `remove(Object)` removes first equal value. |
| What is `ConcurrentModificationException`? | A best-effort fail-fast signal from iterators after unsupported structural modification. |
| Why is `ArrayList` often default? | Fast indexing, efficient scans, and low overhead for common append/read workloads. |
| Is `LinkedList` always better for insertions? | No; indexed search and node overhead can dominate. |
| `Arrays.asList` versus `List.of`? | Fixed-size backed view allowing `set`/null versus unmodifiable null-rejecting list. |
| Why not `List<int>`? | Generics require reference types; use `Integer`, with boxing. |

# 9. Master complexity table

This table assumes ordinary indexed APIs. `n` is current size. Comparison sorting costs can also depend on comparator work.

| Operation | `ArrayList` | `LinkedList` |
| --- | ---: | ---: |
| Access by index | O(1) | O(n) |
| Update by index | O(1) | O(n) |
| Search | O(n) | O(n) |
| Add at end | Amortized O(1), resize O(n) | O(1) |
| Add at beginning | O(n) | O(1) |
| Insert by index | O(n) | O(n) usually; O(1) at ends |
| Remove last | O(1) | O(1) |
| Remove first | O(n) | O(1) |
| Remove by index | O(n) usually; O(1) last | O(n) usually; O(1) at ends |
| `contains()` | O(n) | O(n) |
| Iterate all | O(n) | O(n) |
| Sort | O(n log n) typical | O(n log n) typical, with array copy in default sort |

`LinkedList` traversal may begin from the closer end, but remains O(n) in general. Once a `ListIterator` is already at a linked position, supported local insertion/removal is O(1); locating that position by index is O(n). `ArrayList` append is amortized because a resize copy occurs only occasionally. Neither structure gives constant-time arbitrary value search.

# 10. DSA syntax cheat sheet

The example is a compilable class; keep only the operations needed for a problem. `List` and `ArrayList` operations are the usual default, while `LinkedList` end operations apply when a linked deque is chosen.

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

class ListCheatSheet {
    static void demo() {
        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(20);
        list.add(1, 15);
        int first = list.get(0);
        list.set(0, 11);
        list.remove(1);                    // index 1
        list.remove(Integer.valueOf(20)); // value 20
        int size = list.size();
        boolean empty = list.isEmpty();
        boolean has11 = list.contains(11);
        int position = list.indexOf(11);
        Collections.sort(list);
        list.sort(Comparator.naturalOrder());
        list.sort(Comparator.reverseOrder());

        List<List<Integer>> result = new ArrayList<>();
        result.add(new ArrayList<>(list));

        LinkedList<Integer> deque = new LinkedList<>();
        deque.addFirst(1);
        deque.addLast(2);
        int fromFront = deque.removeFirst();
        int fromBack = deque.removeLast();
    }
}
```

# What Should I Memorize?

### ⭐⭐⭐⭐⭐ Must Know

- `List` is an interface; `ArrayList` and `LinkedList` are implementations. Usually declare `List<E>` and instantiate `ArrayList<>`.
- `add`, `get`, `set`, `remove`, `size`, `isEmpty`, and zero-based index bounds.
- `ArrayList.get` O(1), `LinkedList.get` O(n); `ArrayList` append amortized O(1).
- `remove(int)` versus `remove(Object)` for `List<Integer>`.

### ⭐⭐⭐⭐ Very Important

- Mutable `ArrayList` versus fixed-size `Arrays.asList` versus unmodifiable `List.of`; null rules.
- Enhanced `for`, iterator removal, `removeIf`, `contains`, `indexOf`, and `list.sort`.
- Linked-list end operations O(1), indexed operations O(n), and why `ArrayList` is often the default.
- List aliases and shallow copies; nested-list snapshots in backtracking.

### ⭐⭐⭐ Useful

- `addAll`, `subList` backed view, `toArray`, `Collections.reverse/min/max`, list equality, and `List.copyOf`.
- `LinkedList` deque-style `peek`/`poll` versus exception-throwing `get`/`remove`.
- Capacity planning when approximate batch size is known.

### ⭐⭐ Low Priority for Now

- `containsAll`, `removeAll`, `replaceAll`, `listIterator`, `ensureCapacity`, `trimToSize`, and direct `LinkedList` use for queues where `ArrayDeque` fits.

### ⭐ Rarely Needed for Now

- `ArrayList.clone()` and list `hashCode()` calls in everyday DSA work.

References: [Java SE 21 `List`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html), [`ArrayList`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html), [`LinkedList`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedList.html), [`Arrays.asList`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html), and [`Collections`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html).
