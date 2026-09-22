# Java `Set`, `HashSet`, `LinkedHashSet`, and `TreeSet`

A practical reference for DSA, interviews, and backend development. Examples use Java 21 and assume `import java.util.*;` unless a complete class shows its imports. Short Java blocks are method-body examples. In complexity tables, `n` is the current set size and `m` is another collection's size; costs for hashing and comparison assume those user-defined operations are reasonably cheap.

# 1. Set fundamentals

## Where sets fit

```text
Collection<E> (interface)
    └── Set<E> (interface: unique elements)
          ├── HashSet<E>       (hashing; no iteration-order guarantee)
          ├── LinkedHashSet<E> (hashing; insertion order)
          └── TreeSet<E>       (sorted tree)

Set<E> → SortedSet<E> → NavigableSet<E> → TreeSet<E>
```

`TreeSet` implements `NavigableSet`, which extends `SortedSet`, which extends `Set`. These interfaces add sorted-range and nearest-value operations. `LinkedHashSet` extends `HashSet` as a class but adds a predictable encounter order. The diagram shows the useful conceptual choices rather than every inheritance detail.

## What `Set<E>` means

`Set<E>` is an **interface** for a collection with no duplicate elements. `<E>` is the element type; `Set<Integer>` means a set of `Integer` objects. An interface gives the operations, while an implementation supplies storage and performance.

```java
Set<Integer> numbers = new HashSet<>();
numbers.add(10);
numbers.add(20);
numbers.add(10); // still only two elements
```

`Set` is the interface type, `Integer` is the element type, `numbers` is a reference, and `new HashSet<>()` creates the mutable implementation. The diamond `<>` lets Java infer `Integer`. Sets are useful for membership tests, uniqueness, and avoiding repeated values.

The **Set interface does not promise iteration order**; the chosen implementation may. It has no positional index and no `get(index)` or `set(index, value)`. Whether `null` is accepted depends on the implementation. `HashSet` and `LinkedHashSet` allow one `null`; a natural-order `TreeSet` does not. The no-duplicate rule means at most one element equal to another already present; how equality is decided differs for `TreeSet` (explained below).

## `Set` versus `List`

| Feature | `List` | `Set` |
| --- | --- | --- |
| Duplicates | Allowed | Rejected |
| Ordering | Positional order | Depends on implementation |
| Index access | Yes, zero-based | No |
| `get(index)` | Yes | No |
| Main purpose | Sequence where position and repetition matter | Unique membership or ordered unique values |
| DSA use | Inputs, indexed algorithms, answer lists | Seen/visited values, deduplication |
| Backend use | Ordered result collections | Unique IDs, roles, tags |

Use a `Set` when duplicate values would be a bug or when fast membership checks matter more than positions. Use a `List` when order by position, duplicates, or indexed access matters. A set can be traversed with enhanced `for`, an `Iterator`, or `forEach`; **`set.get(0)` is intentionally incorrect Java** because there is no index-based Set method.

# 2. Essential `Set` operations

This table rates operations for LeetCode, interviews, general Java, and backend work. `H` = `HashSet`, `L` = `LinkedHashSet`, `T` = `TreeSet`. `H/L` costs are expected with well-distributed hashes; poor hashes/collisions can make a single operation slower. `T` costs use tree order. Bulk-method cost also depends on the argument collection.

| Priority | Method | Return Type | Purpose | Typical Complexity | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `add(e)` | `boolean` | Insert if absent | H/L expected O(1); T O(log n) | `set.add(7)` | Mark seen | Unique ID |
| ⭐⭐⭐ | `addAll(c)` | `boolean` | Union into receiver | H/L expected O(m); T O(m log(n+m)) | `a.addAll(b)` | Merge unique values | Merge roles |
| ⭐⭐⭐⭐⭐ | `remove(o)` | `boolean` | Delete if present | H/L expected O(1); T O(log n) | `set.remove(7)` | Update seen set | Revoke role |
| ⭐⭐⭐ | `removeAll(c)` | `boolean` | Difference from receiver | Depends on `c`/implementation | `a.removeAll(b)` | Set difference | Remove blocked IDs |
| ⭐⭐⭐ | `retainAll(c)` | `boolean` | Keep common elements | Depends on `c`/implementation | `a.retainAll(b)` | Intersection | Shared permissions |
| ⭐⭐⭐ | `removeIf(p)` | `boolean` | Remove matching elements | Usually a traversal plus predicate cost | `set.removeIf(x -> x < 0)` | Filter set | Prune stale values |
| ⭐⭐⭐⭐⭐ | `contains(o)` | `boolean` | Membership test | H/L expected O(1); T O(log n) | `set.contains(7)` | Seen lookup | Permission check |
| ⭐⭐⭐ | `containsAll(c)` | `boolean` | Does receiver include every item in `c`? | H/L expected O(m); T O(m log n) | `a.containsAll(b)` | Subset test | Required roles |
| ⭐⭐⭐⭐⭐ | `size()` | `int` | Count unique elements | O(1) | `set.size()` | Count distinct | Unique count |
| ⭐⭐⭐⭐ | `isEmpty()` | `boolean` | Test zero elements | O(1) | `set.isEmpty()` | Edge case | Empty result |
| ⭐⭐⭐⭐ | `clear()` | `void` | Remove all elements | Implementation/capacity dependent | `set.clear()` | Reset state | Reset cache-like set |
| ⭐⭐⭐⭐ | `iterator()` | `Iterator<E>` | Sequential traversal | Creation cheap; full pass below | `set.iterator()` | Visit unique values | Process values |
| ⭐⭐⭐ | `forEach(action)` | `void` | Visit elements | H roughly O(n+capacity); L/T O(n) | `set.forEach(System.out::println)` | Simple visit | Process IDs |
| ⭐⭐⭐ | `toArray()` / `(T[])` | `Object[]` / `T[]` | Copy to array | Full traversal + output | `set.toArray(new Integer[0])` | Interop | API conversion |
| ⭐⭐⭐⭐ | `equals(o)` | `boolean` | Same members, ignoring order | Depends on membership costs | `a.equals(b)` | Check result | Compare role sets |
| ⭐⭐ | `hashCode()` | `int` | Order-independent set hash | Full traversal | `set.hashCode()` | Rare directly | Set as map key |

`add(e)` returns `true` only when the set changes. `remove(o)` returns `true` only when a matching element was removed. `addAll`, `removeAll`, and `retainAll` mutate their receiver and return whether it changed; copy the receiver first if the originals must remain intact. On an unmodifiable set, mutators throw `UnsupportedOperationException`.

For bulk operations, a hash-set argument can make repeated membership checks expected O(1), while a list argument may require an O(m) scan for each of n elements (O(n·m) total). Implementations may also choose different strategies based on relative collection sizes, so the table avoids a single unconditional bound.

```java
Set<Integer> set = new HashSet<>();
boolean first = set.add(10);     // true
boolean duplicate = set.add(10); // false; size remains 1
boolean present = set.contains(10); // true
boolean removed = set.remove(10);   // true
boolean absent = set.remove(10);    // false
```

`contains` is especially important in DSA: it often replaces a repeated linear scan with expected constant-time membership checks. For a `TreeSet`, `contains` is O(log n) and buys sorted/navigable behavior. None of these costs includes unusually expensive element `hashCode`, `equals`, or comparator code.

| Type | Size syntax |
| --- | --- |
| Array | `array.length` |
| String | `string.length()` |
| List | `list.size()` |
| Set | `set.size()` |

## Traversing without indexes

```java
Set<Integer> set = new LinkedHashSet<>(List.of(3, 1, 2));
for (Integer value : set) {
    System.out.println(value);
}
Iterator<Integer> iterator = set.iterator();
while (iterator.hasNext()) {
    Integer value = iterator.next();
    System.out.println(value);
}
set.forEach(System.out::println);
```

Enhanced `for` is the most common read-only traversal. Use an iterator when you need its supported `remove()` operation while traversing; `next()` returns an element and advances the cursor. `forEach` is concise for a simple action. These methods follow the implementation's iteration order: insertion order for `LinkedHashSet`, sorted order for `TreeSet`, and **no promised order** for `HashSet`.

# 3. `HashSet`: fast expected membership

`HashSet<E>` implements `Set` using a hash table (backed by a `HashMap` in Java 21). It rejects duplicates, allows one `null`, and gives **no predictable iteration order**. It is the usual first choice for duplicate detection, visited values, and membership tests when ordering is irrelevant.

```java
Set<Integer> values = new HashSet<>();
Set<Integer> withCapacity = new HashSet<>(1000);
```

The constructor argument is **initial capacity**, not initial element count or size. `withCapacity.size()` is still zero. An appropriate capacity can reduce rehashing for a known large workload, but the default constructor is normally enough. A negative capacity is invalid.

## Hashing, equality, and collisions

```text
element x
   ↓ hashCode()
hash value → bucket choice
   ↓ inspect candidates already in that bucket
equals() decides whether x matches an existing element
```

Conceptually, `add(x)` computes a hash, chooses a bucket, and checks candidate elements. If an equal element is already there, it returns `false`; otherwise it inserts x and returns `true`. `contains` and `remove` follow a similar lookup path. `null` is handled specially by the implementation.

A **hash collision** means distinct objects map to the same hash/bucket. For example, A and B might both land in bucket 5. That alone does **not** make them duplicates: `equals` still distinguishes them. A good hash distribution keeps bucket work small.

> **Essential contract:** If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` must be true. Equal objects with different hashes can be placed or searched in different buckets, breaking expected set behavior. The reverse is **not** required: unequal objects may share a hash.

Custom objects in `HashSet` (and `HashMap` keys) need equality and hashing that describe the same identity. Java records provide value-based `equals` and `hashCode` for their components:

```java
record User(long id) {}
Set<User> users = new HashSet<>();
boolean added = users.add(new User(7));
boolean duplicate = users.add(new User(7)); // false
```

If a regular class inherits `Object.equals`/`hashCode`, two separately constructed instances are usually distinct even if their fields look the same. Override **both** when value equality is intended. Hashes need not be unique; they must remain consistent with equality.

## Cost, capacity, and load factor

| Operation | Expected average | Conservative worst case | Practical note |
| --- | ---: | ---: | --- |
| `add()` | O(1) amortized | O(n) | Resize/rehash or poor collisions can cost more |
| `contains()` | O(1) | O(n) | Hash/equals quality matters |
| `remove()` | O(1) | O(n) | Same lookup caveat |
| Iterate all | O(n + capacity) | O(n + capacity) | Empty buckets may also be scanned |

The conservative O(n) worst-case bound avoids promising constant-time performance. Modern Java may use tree-shaped buckets in some collision cases, improving those cases, but that is an implementation detail and not a reason to write poor hashes. Individual resizes take O(n); average append/insert remains expected constant time over many inserts with suitable hashing.

**Capacity** is the bucket-array size; **load factor** controls how full the table gets before growth. On a threshold crossing, the table grows and redistributes entries (**rehashing**). The Java 21 default `HashSet` constructor uses capacity 16 and load factor 0.75; treat these as documented defaults for that constructor, not tuning values to memorize. Larger capacity can reduce collisions/resizes but uses more memory and can make iteration slower because `HashSet` iteration depends on capacity as well as size.

# 4. `LinkedHashSet`: unique values in insertion order

`LinkedHashSet<E>` combines hashing with a linked encounter-order structure. It implements `Set`, rejects duplicates, allows one `null`, and iterates in **insertion order**. Normal `add` of an existing element does not move it to the end. It uses more memory than `HashSet` for its extra links, and basic lookup is still expected O(1) with well-distributed hashes.

```java
Set<Integer> set = new LinkedHashSet<>();
set.add(3);
set.add(1);
set.add(2);
set.add(1); // duplicate; no new element or reordering
for (Integer value : set) {
    System.out.println(value); // 3, then 1, then 2
}
```

| Feature | `HashSet` | `LinkedHashSet` |
| --- | --- | --- |
| Duplicates | No | No |
| Ordering | Unspecified | Insertion order |
| Lookup | Expected O(1) | Expected O(1) |
| Memory | Hash table | Hash table plus order links |
| Typical performance | Slightly less bookkeeping | Slightly more bookkeeping; iteration O(n) |
| DSA usefulness | Seen/membership | Stable-order deduplication |
| Backend usefulness | Unique IDs/roles | Predictable result order |
| Typical use | Order irrelevant | Preserve input encounter order |

Unlike `HashSet`, iterating a `LinkedHashSet` is O(n) regardless of backing capacity. Preserve input order when the user or an API consumer should see first occurrences in their original sequence.

```java
List<Integer> nums = List.of(3, 1, 3, 2, 1);
Set<Integer> unique = new LinkedHashSet<>(nums); // iteration: 3, 1, 2
List<Integer> orderedUnique = new ArrayList<>(unique); // [3, 1, 2]
```

> **Backend note:** A predictable Java iteration order helps produce stable results, but use explicit ordering in persistence queries and API contracts when order is part of the requirement; a set alone does not define database result order.

# 5. `TreeSet`: sorted unique values and neighbors

`TreeSet<E>` implements `NavigableSet<E>` (and thus `SortedSet<E>` and `Set<E>`). Java 21 backs it with a balanced search tree. Iteration follows **sorted order**, not insertion order. `add`, `contains`, and `remove` take O(log n) comparison steps; this is the tradeoff for automatic order and nearest-value queries.

```java
NavigableSet<Integer> sorted = new TreeSet<>();
sorted.add(5);
sorted.add(1);
sorted.add(3);
System.out.println(sorted); // [1, 3, 5]
```

`TreeSet<Integer>` uses `Integer`'s natural order; `TreeSet<String>` uses `String`'s lexicographic natural order. Natural ordering comes from `Comparable.compareTo`. A custom `Comparator` supplies a different order:

```java
NavigableSet<Integer> descending = new TreeSet<>(Comparator.reverseOrder());
descending.addAll(List.of(5, 1, 3));
System.out.println(descending); // [5, 3, 1]
```

Use `TreeSet<E>` or `NavigableSet<E>` as the variable type when you need `floor`, `ceiling`, or other navigation methods; a variable declared merely `Set<E>` does not expose them. Navigation follows the set's comparator order, which may differ from normal numerical order for a descending comparator.

> **Important:** A `TreeSet` treats two elements as duplicates when its comparator (or `compareTo`) returns **0**. For the general `Set` equality contract to behave as expected, that ordering should be consistent with `equals`. A comparator that compares users only by name, for example, can collapse two unequal users with the same name into one set entry.

## Navigation methods, ranked

The examples assume `NavigableSet<Integer> s = new TreeSet<>(List.of(10, 20, 30, 40));`. For a range method, the returned set is a **backed view**, so edits through it can affect `s`.

| Priority | Method | Return Type | Purpose | Complexity | Example | DSA Use |
| --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐ | `first()` | `E` | Smallest in set order; throws if empty | O(log n) typical | `s.first()` | Minimum |
| ⭐⭐⭐ | `last()` | `E` | Largest in set order; throws if empty | O(log n) typical | `s.last()` | Maximum |
| ⭐⭐⭐⭐ | `lower(x)` | `E` or `null` | Greatest value strictly below x | O(log n) | `s.lower(20)` | Predecessor |
| ⭐⭐⭐⭐ | `higher(x)` | `E` or `null` | Least value strictly above x | O(log n) | `s.higher(20)` | Successor |
| ⭐⭐⭐⭐ | `floor(x)` | `E` or `null` | Greatest value at or below x | O(log n) | `s.floor(20)` | Nearest ≤ x |
| ⭐⭐⭐⭐ | `ceiling(x)` | `E` or `null` | Least value at or above x | O(log n) | `s.ceiling(20)` | Nearest ≥ x |
| ⭐⭐ | `pollFirst()` | `E` or `null` | Remove/return first, or null if empty | O(log n) | `s.pollFirst()` | Consume minimum |
| ⭐⭐ | `pollLast()` | `E` or `null` | Remove/return last, or null if empty | O(log n) | `s.pollLast()` | Consume maximum |
| ⭐⭐ | `headSet(to)` | `SortedSet<E>` view | Values strictly below `to` | View creation cheap; query O(log n) | `s.headSet(30)` | Bounded values |
| ⭐⭐ | `tailSet(from)` | `SortedSet<E>` view | Values at/above `from` | View creation cheap; query O(log n) | `s.tailSet(20)` | Bounded values |
| ⭐⭐ | `subSet(from,to)` | `SortedSet<E>` view | Values in `[from,to)` | View creation cheap; query O(log n) | `s.subSet(20, 40)` | Range queries |
| ⭐⭐ | `descendingSet()` | `NavigableSet<E>` view | Reverse-order view | View creation cheap; traversal O(n) | `s.descendingSet()` | Descending output |

```java
NavigableSet<Integer> s = new TreeSet<>(List.of(10, 20, 30, 40));
Integer lower = s.lower(20);     // 10: strictly less
Integer floor = s.floor(20);     // 20: less or equal
Integer higher = s.higher(20);   // 30: strictly greater
Integer ceiling = s.ceiling(20); // 20: greater or equal
Integer none = s.lower(10);      // null: no smaller value
SortedSet<Integer> head = s.headSet(30);    // [10, 20]
SortedSet<Integer> tail = s.tailSet(20);    // [20, 30, 40]
SortedSet<Integer> middle = s.subSet(20, 40); // [20, 30]
NavigableSet<Integer> reverseView = s.descendingSet(); // [40, 30, 20, 10]
```

The one-argument range methods use **exclusive** `headSet(to)` and **inclusive** `tailSet(from)`; `subSet(from,to)` uses `[from,to)`. Overloads on `NavigableSet` can specify inclusive/exclusive ends explicitly. These are views, not copies; use `new TreeSet<>(view)` for an independent set. `first()`/`last()` throw `NoSuchElementException` on empty input; `lower`/`floor`/`higher`/`ceiling` and `pollFirst`/`pollLast` return `null` when no result exists.

## `null` and TreeSet complexity

A natural-order `TreeSet` rejects `null` because it cannot compare it to ordinary values. A **custom comparator that explicitly supports null** can permit it, so “TreeSet never allows null” is too broad. In normal DSA/backend usage, keep `TreeSet` elements non-null. Both `HashSet` and `LinkedHashSet` permit one null.

```java
NavigableSet<String> nullable = new TreeSet<>(
    Comparator.nullsFirst(Comparator.naturalOrder())
);
nullable.add(null); // supported by this comparator
nullable.add("A");
```

| Operation | Typical tree complexity |
| --- | ---: |
| `add()` | O(log n) |
| `remove()` | O(log n) |
| `contains()` | O(log n) |
| `first()` | O(log n) typical |
| `last()` | O(log n) typical |
| `floor()` | O(log n) |
| `ceiling()` | O(log n) |
| Iterate all | O(n) |

The tree remains balanced as values are inserted and removed, so searches follow only a logarithmic number of levels. `first`/`last` costs describe the current tree implementation; the API explicitly guarantees logarithmic cost for `add`, `remove`, and `contains`. A comparison function with expensive work adds its own cost.

# 6. Choosing among the three implementations

| Feature | `HashSet` | `LinkedHashSet` | `TreeSet` |
| --- | --- | --- | --- |
| Duplicate elements | No | No | No; comparator result 0 defines same entry |
| Ordering | Unspecified | Insertion order | Sorted comparator order |
| Sorted? | No | No | Yes |
| Allows `null`? | Yes, one | Yes, one | Not with natural order; comparator may allow |
| `add()` | Expected O(1) | Expected O(1) | O(log n) |
| `contains()` | Expected O(1) | Expected O(1) | O(log n) |
| `remove()` | Expected O(1) | Expected O(1) | O(log n) |
| Memory | Hash table | Hash table plus order links | Tree nodes and links |
| Best for lookup | Yes, when order irrelevant | Yes, when order also needed | When sorted neighbors/ranges also needed |
| Best for preserving insertion order | No | Yes | No |
| Best for sorted elements | No | No | Yes |
| DSA usefulness | Seen/visited/dedup | Stable-order dedup | Ordered values/predecessor/successor |
| Backend usefulness | Unique IDs/roles | Ordered unique API values | Sorted unique schedules/values |

The three sets solve different ordering needs. Hash-based sets have expected fast membership but can degrade with poor hashing; a tree pays O(log n) to maintain order. `LinkedHashSet` adds ordering links and normally a little overhead. `TreeSet` needs comparable elements or a compatible comparator.

| Scenario | Choice | Reason |
| --- | --- | --- |
| Fast duplicate detection | `HashSet` | Expected O(1) `add` and `contains`; order irrelevant |
| Unique values in input order | `LinkedHashSet` | Keeps first occurrence order automatically |
| Automatically sorted elements | `TreeSet` | Maintains order as values arrive |
| `floor()` / `ceiling()` nearest values | `TreeSet` | Navigation is built into `NavigableSet` |
| Simple membership checking | `HashSet` | Avoid tree cost when order is unnecessary |
| Backend unique IDs/roles | Usually `HashSet`; `LinkedHashSet` if response order matters | Decide whether encounter order is part of the contract |
| Sorted unique timestamps/numbers | `TreeSet` | Unique sorted range and neighbor queries |

# 7. Common set operations

Constructing a set from a list removes duplicates. `HashSet` gives no order promise; `LinkedHashSet` preserves first occurrence order.

```java
List<Integer> list = List.of(1, 2, 2, 3, 1);
Set<Integer> unique = new HashSet<>(list);           // members {1, 2, 3}
Set<Integer> ordered = new LinkedHashSet<>(list);    // iteration 1, 2, 3
```

Union, intersection, and difference are implemented by **mutating the receiver**. Copy first when the original sets should remain unchanged.

```java
Set<Integer> a = Set.of(1, 2, 3);
Set<Integer> b = Set.of(3, 4);

Set<Integer> union = new HashSet<>(a);
union.addAll(b);                    // A ∪ B = {1, 2, 3, 4}

Set<Integer> intersection = new HashSet<>(a);
intersection.retainAll(b);         // A ∩ B = {3}

Set<Integer> difference = new HashSet<>(a);
difference.removeAll(b);           // A - B = {1, 2}

boolean bIsSubsetOfA = a.containsAll(b); // false: 4 is absent from A
boolean commonIsSubsetOfA = a.containsAll(intersection); // true
```

`a.containsAll(b)` asks **whether every element of b is in a**: it tests `b ⊆ a`, not the reverse. Bulk-operation cost depends on the argument type, relative sizes, and implementation; do not assume each mathematical operation is always O(n). `Set.equals` compares membership regardless of iteration order, so `{1,2}` in a `HashSet` equals the same members in a `TreeSet` when their element equality/order is consistent. `Set.of(...)` is an unmodifiable, null-rejecting factory and rejects duplicate arguments; copying it into a mutable set is fine.

# 8. Sets in DSA

| Pattern | Why a set helps |
| --- | --- |
| Duplicate detection | `add` returns false on a repeated value |
| Membership checking | Expected O(1) hash lookup replaces repeated scans |
| Seen/visited tracking | Prevent processing an item twice |
| Finding unique values | Set stores one representative per equality class |
| Cycle detection support | A repeated state/node can reveal revisiting |
| Sliding window | Track distinct items inside the current window; remove outgoing items |
| Two Sum-style problems | Test whether a complement was seen earlier |
| Longest consecutive sequence | Test neighboring integers efficiently |
| Graph traversal | Mark visited node IDs/references in BFS/DFS |
| Deduplication | Collapse repeated input values |
| Intersection of arrays | Test membership of one array while scanning another |
| Unique characters | Track observed `char` values in a string problem |

These are relationships to Set operations, not full algorithm lessons. In most such problems, `HashSet` is the first choice unless sorted or encounter order is required.

```java
import java.util.HashSet;
import java.util.Set;

class SetRecipes {
    static boolean hasDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            if (!seen.add(num)) return true;
        }
        return false;
    }

    static boolean hasDuplicateChar(String text) {
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < text.length(); i++) {
            if (!seen.add(text.charAt(i))) return true;
        }
        return false;
    }
}
```

For `n` numbers, duplicate detection is expected O(n) time and O(n) extra space, versus O(n²) time for checking every pair in a nested loop. `Set<Character>` tracks UTF-16 `char` code units; one visible Unicode character may use more than one code unit. A sliding-window solution can add an incoming character and remove an outgoing one; a graph traversal can use `Set<Integer> visited` or `Set<Node> visited` to avoid revisiting. For custom nodes, stable equality/hash behavior matters.

# 9. Sets in backend code

Common types include `Set<String>` for tags, `Set<Long>` for IDs, `Set<Role>` for roles, `Set<Permission>` for permissions, and `Set<Tag>` for domain labels. Sets can deduplicate request inputs, store a user's unique permissions, and prevent duplicate values while assembling a response. Choose `LinkedHashSet` if encounter order matters to clients and `TreeSet` if a sorted result or nearest-value query is required.

```java
record Role(String name) {}
Set<Role> roles = new HashSet<>();
roles.add(new Role("ADMIN"));
roles.add(new Role("ADMIN")); // one role by record value equality
Set<Long> requestedIds = new HashSet<>(List.of(4L, 4L, 9L));
```

> **JPA/Hibernate note:** Entity relationships may use `Set<Role>` or `Set<Tag>` when uniqueness is meaningful. In-memory Set uniqueness does **not** replace a database unique constraint, and entity equality/hash rules should remain stable while an entity is in a hash set. Persistence mapping details belong in a separate topic.

# 10. Custom objects and mutable hashed fields

For a custom value object, a record is a compact way to get consistent `equals` and `hashCode`. A `TreeSet` also needs an ordering: either the type implements `Comparable`, or you supply a `Comparator`.

```java
record User(long id) {}
Set<User> hashed = new HashSet<>();
hashed.add(new User(7));
hashed.add(new User(7)); // rejected as equal

NavigableSet<User> ordered = new TreeSet<>(Comparator.comparingLong(User::id));
ordered.add(new User(7));
ordered.add(new User(8));
```

The comparator above orders and deduplicates by `id`. It is consistent with the record's equality because the record has only `id`. If the record also had `name` but the comparator still used only `id`, two records with different names could compare as 0 and one would be rejected by `TreeSet` despite `equals` saying they differ.

> **Important backend/interview issue:** Do not change a field used by `equals`/`hashCode` while an object is inside a `HashSet`. The object may remain in a bucket chosen using its *old* hash; `contains` or `remove` using its new state may fail. The general `Set` contract also warns against changing equality-relevant fields while an element is present.

```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class MutableUser {
    int id;
    MutableUser(int id) { this.id = id; }
    @Override public boolean equals(Object other) {
        return other instanceof MutableUser u && id == u.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

class MutableSetExample {
    static void demonstrateRisk() {
        MutableUser user = new MutableUser(1);
        Set<MutableUser> users = new HashSet<>();
        users.add(user);
        user.id = 2; // unsafe: equality/hash identity changed after insertion
        // users.contains(user) is no longer reliable for this changed key.
    }
}
```

Prefer immutable identity fields, or remove the element **before** changing them and reinsert afterward. For generated database IDs, decide equality policy carefully before using entities in hash collections.

# 11. Common mistakes

| Mistake | Correct understanding |
| --- | --- |
| Expecting `set.get(0)` | **Intentionally invalid Java**: sets have no index access; iterate instead |
| Assuming `HashSet` preserves insertion order | Its iteration order is unspecified and may change |
| Assuming `TreeSet` preserves insertion order | It iterates in comparator/natural sorted order |
| Assuming `LinkedHashSet` sorts values | It preserves insertion order, not numeric/alphabetic order |
| Expecting size to grow after a duplicate `add` | Duplicate `add` returns `false`; size stays the same |
| Overriding `equals` without compatible `hashCode` | Equal objects must have equal hashes for hashing to work |
| Modifying fields used by hash/equality after insertion | Lookup/removal behavior can break; prefer stable identity |
| Assuming `HashSet` is **guaranteed** O(1) | O(1) is expected with suitable hashing; pathological cases cost more |
| Confusing `HashSet` with `HashMap` | A Set stores unique elements; a Map stores key–value associations |
| Using `TreeSet` for plain membership only | `HashSet` is simpler and usually faster when order is irrelevant |
| Forgetting TreeSet ordering requirements | Elements need compatible natural ordering or a comparator |
| Assuming every Set accepts `null` | H/L do; natural-order TreeSet does not |
| Relying on observed `HashSet` output order | Tests/data changes can alter it; choose another implementation for order |
| Using Set when duplicates matter | Keep a `List` or counts when repetition is meaningful |

Also distinguish **set-level** `equals`/`hashCode` from **element-level** methods. Calling `set1.equals(set2)` checks same members regardless of order; implementing `User.equals`/`User.hashCode` determines how hash sets recognize equal `User` elements. Both levels have different purposes.

# 12. Short interview questions

| Question | Short answer |
| --- | --- |
| What is `Set`? | An interface for a collection with no duplicate elements. |
| Why no duplicates? | `add` rejects an element considered equal to one already present. |
| Is a `Set` ordered? | The interface gives no general iteration order; implementations differ. |
| `Set` versus `List`? | Unique membership without indexes versus positional sequence that may repeat. |
| `HashSet` versus `TreeSet`? | Expected fast hashing with unspecified order versus sorted tree with O(log n) operations. |
| `HashSet` versus `LinkedHashSet`? | Unspecified order versus insertion order, both hash-based. |
| All three in one sentence? | HashSet: unordered hash; LinkedHashSet: insertion-ordered hash; TreeSet: sorted tree. |
| How does `HashSet` work? | It hashes to a bucket, then checks candidates for equality. |
| How does it detect duplicates? | Matching hash/bucket candidates are compared with `equals`. |
| What is a collision? | Unequal values land in the same hash/bucket area. |
| Why both `equals` and `hashCode`? | Equal objects must hash alike so lookup reaches their candidate bucket. |
| Same hash means equal? | No. A collision still needs `equals` to distinguish values. |
| Can `HashSet` contain null? | Yes, at most one. |
| Can `TreeSet` contain null? | Natural order: no; a null-aware comparator may allow it. |
| Average `HashSet.contains` cost? | Expected O(1) with a suitable hash distribution. |
| Why is `TreeSet` O(log n)? | Its balanced tree limits search depth to logarithmic growth. |
| Does `HashSet` preserve insertion order? | No. |
| Does `LinkedHashSet` preserve insertion order? | Yes, for normal insertion and iteration. |
| Does `TreeSet` sort automatically? | Yes, under natural order or its comparator. |
| When choose `TreeSet`? | When sorted iteration, ranges, or nearest values are required. |
| `floor(x)` versus `lower(x)`? | Greatest `<= x` versus greatest `< x`. |
| `ceiling(x)` versus `higher(x)`? | Least `>= x` versus least `> x`. |
| Why avoid mutable hashed elements? | Changing hash/equality fields can make existing entries hard to find/remove. |

# 13. Master complexity comparison

`HashSet`/`LinkedHashSet` entries below are **expected** costs for well-distributed hashes, not worst-case guarantees. `n` is element count and `c` is `HashSet` bucket capacity. For finding an extreme in an unsorted set, you must scan; Java's `Collections.min/max` can do that if elements are comparable.

| Operation | `HashSet` | `LinkedHashSet` | `TreeSet` |
| --- | ---: | ---: | ---: |
| `add()` | Expected O(1) amortized | Expected O(1) amortized | O(log n) |
| `contains()` | Expected O(1) | Expected O(1) | O(log n) |
| `remove()` | Expected O(1) | Expected O(1) | O(log n) |
| Iterate all | O(n + c) | O(n) | O(n), sorted |
| Find smallest | O(n + c) scan | O(n) scan | `first()` O(log n) typical |
| Find largest | O(n + c) scan | O(n) scan | `last()` O(log n) typical |
| Sorted traversal | Sort copied values: O(n log n) plus iteration | Sort copied values: O(n log n) | O(n), already sorted |
| `floor()` | N/A | N/A | O(log n) |
| `ceiling()` | N/A | N/A | O(log n) |

Worst-case hash behavior may be O(n) for an operation under pathological collisions or unusual custom keys; modern Java collision handling can improve many such cases. A resize can also make one insertion O(n). `LinkedHashSet` pays for order links in memory; `TreeSet` pays for tree nodes and comparisons; `HashSet` capacity affects memory and iteration. Comparator/`hashCode`/`equals` costs add to these structural bounds.

# 14. DSA syntax cheat sheet

Use a `NavigableSet` or `TreeSet` variable for navigation methods. The class below is compilable; remove unused statements in a real problem.

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

class SetCheatSheet {
    static void demo() {
        Set<Integer> set = new HashSet<>();
        set.add(10);
        boolean has10 = set.contains(10);
        boolean removed = set.remove(10);
        int count = set.size();
        boolean empty = set.isEmpty();
        set.clear();
        for (Integer value : set) System.out.println(value);

        List<Integer> list = List.of(3, 1, 3, 2);
        Set<Integer> unique = new HashSet<>(list);
        Set<Integer> orderedUnique = new LinkedHashSet<>(list);
        Set<Integer> sorted = new TreeSet<>(list);
        NavigableSet<Integer> descending = new TreeSet<>(Comparator.reverseOrder());
        descending.addAll(list);

        NavigableSet<Integer> tree = new TreeSet<>(List.of(10, 20, 30));
        int first = tree.first();
        int last = tree.last();
        Integer lower = tree.lower(20);
        Integer floor = tree.floor(20);
        Integer higher = tree.higher(20);
        Integer ceiling = tree.ceiling(20);

        Set<Integer> a = new HashSet<>(List.of(1, 2));
        Set<Integer> b = Set.of(2, 3);
        Set<Integer> union = new HashSet<>(a);
        union.addAll(b);
        Set<Integer> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<Integer> difference = new HashSet<>(a);
        difference.removeAll(b);
    }
}
```

# What Should I Memorize?

### ⭐⭐⭐⭐⭐ Must Know

- `Set` means unique elements and no index; `HashSet` is the usual DSA choice when order does not matter.
- `add`/`contains`/`remove`/`size`, duplicate detection with `!seen.add(x)`, and expected O(1) hash operations.
- The element contract: equal objects must have equal `hashCode` values; use stable equality fields.

### ⭐⭐⭐⭐ Very Important

- `HashSet` order is unspecified; `LinkedHashSet` keeps insertion order; `TreeSet` keeps sorted order.
- `TreeSet` `floor`/`ceiling` include equality; `lower`/`higher` exclude it, with O(log n) navigation.
- `HashSet` average versus worst-case behavior, `TreeSet` comparator-based uniqueness, and natural-order null rejection.
- Union (`addAll`), intersection (`retainAll`), difference (`removeAll`), and subset direction (`containsAll`).

### ⭐⭐⭐ Useful

- Iterator/enhanced-for traversal, `first`/`last`, preserving list order with `LinkedHashSet`, and `Set` equality independent of iteration order.
- Backend role/tag/ID deduplication and the need for stable entity equality in hash collections.

### ⭐⭐ Low Priority for Now

- `headSet`/`tailSet`/`subSet` backed views, `descendingSet`, `pollFirst`/`pollLast`, and capacity/load-factor tuning.

### ⭐ Rarely Needed for Now

- Direct calls to a set's own `hashCode` and manual hash-table tuning without a measured need.

References: [Java SE 21 `Set`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Set.html), [`HashSet`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html), [`LinkedHashSet`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashSet.html), [`TreeSet`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeSet.html), and [`NavigableSet`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/NavigableSet.html).
