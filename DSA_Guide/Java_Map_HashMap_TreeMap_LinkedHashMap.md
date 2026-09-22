# Java `Map`, `HashMap`, `LinkedHashMap`, and `TreeMap`

A practical reference for DSA, interviews, and backend work, using Java 21. Examples assume `import java.util.*;` unless imports are shown. Short Java blocks are method-body examples. In complexity tables, `n` means the map's entry count; expected hash costs assume reasonably distributed key hashes and inexpensive equality checks.

# 1. Map fundamentals

`Map` belongs to the Java Collections Framework, but **`Map` does not extend `Collection`**. A map stores associations between keys and values; its `keySet()`, `values()`, and `entrySet()` expose collection views.

```text
Map<K,V> (interface)
    ├── HashMap<K,V>       (hashing; no iteration-order guarantee)
    ├── LinkedHashMap<K,V> (hashing; predictable encounter order)
    └── TreeMap<K,V>       (sorted keys)

Map<K,V> → SortedMap<K,V> → NavigableMap<K,V> → TreeMap<K,V>
```

The diagram is conceptual: `LinkedHashMap` actually extends `HashMap`; `TreeMap` implements `NavigableMap`, which extends `SortedMap` and `Map`.

## Keys and values

`Map<K,V>` is an interface. `K` is the key type and `V` is the value type. A **key** locates its current value. Keys are unique within a map; values may repeat. A map has no positional index like a list. Whether a null key or null value is accepted depends on implementation.

```java
Map<String, Integer> ages = new HashMap<>();
ages.put("John", 21);
ages.put("Anna", 21); // duplicate value is fine
ages.put("David", 25);
```

`Map` is the interface type, `String` is the key type, `Integer` is the value type, `ages` is the reference, and `new HashMap<>()` creates a mutable hash-based implementation. The diamond infers `<String,Integer>`.

```text
"John"  → 21
"Anna"  → 21
"David" → 25
```

Each key has at most one current value. Calling `put` again for an equal key replaces that value:

```java
Map<String, Integer> counts = new HashMap<>();
Integer old1 = counts.put("Java", 1); // null: no previous mapping
Integer old2 = counts.put("Java", 2); // 1: previous value
System.out.println(counts);           // {Java=2}
```

`put` returns the previous value or `null`; that `null` can also mean the key previously mapped to null. Use `containsKey` if you must distinguish absence from a null mapping.

| Feature | `List` | `Set` | `Map` |
| --- | --- | --- | --- |
| Main structure | Sequence of elements | Unique elements | Key → value associations |
| Duplicate elements/values | Elements may repeat | Elements do not repeat | Values may repeat; keys do not |
| Unique keys | N/A | N/A | Yes |
| Index access | Yes | No | No |
| Lookup mechanism | Index or linear search | Membership by equality/order | Key by equality/order |
| Typical use | Positional data | Unique values | Lookup/aggregation by key |
| DSA use | Inputs/results | Seen/visited values | Counts, indexes, groups |
| Backend use | Result rows | Roles/tags/IDs | Lookup tables, aggregation |

> **Common mistake:** `map.get(0)` is valid Java if `0` is a compatible **key**, but it does not mean “get the first entry.” Maps do not expose numeric positions.

# 2. Essential `Map` methods

This table rates methods for LeetCode, interviews, general Java, and backend work. `H` = `HashMap`, `L` = `LinkedHashMap`, and `T` = `TreeMap`. H/L basic key operations are **expected O(1)**, not guaranteed O(1); T key operations are O(log n). View creation is cheap, but traversing a view has the map's iteration cost.

| Priority | Method | Return Type | Purpose | Typical Complexity | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `put(k,v)` | `V` old or null | Insert/replace mapping | H/L expected O(1); T O(log n) | `map.put("A",1)` | Store count/index | Store lookup |
| ⭐⭐⭐ | `putIfAbsent(k,v)` | `V` current/old or null | Fill absent/null mapping | H/L expected O(1); T O(log n) | `map.putIfAbsent("A",1)` | First index | Default value |
| ⭐⭐⭐⭐⭐ | `get(k)` | `V` or null | Read mapped value | H/L expected O(1); T O(log n) | `map.get("A")` | Retrieve count/index | ID lookup |
| ⭐⭐⭐⭐⭐ | `getOrDefault(k,d)` | `V` | Read or default if absent | H/L expected O(1); T O(log n) | `freq.getOrDefault(c,0)` | Frequency count | Missing default |
| ⭐⭐⭐⭐⭐ | `containsKey(k)` | `boolean` | Test mapping presence | H/L expected O(1); T O(log n) | `map.containsKey("A")` | Seen key | Existence check |
| ⭐⭐ | `containsValue(v)` | `boolean` | Scan for a value | H O(n+capacity); L/T O(n) | `map.containsValue(1)` | Rare | Occasional audit |
| ⭐⭐⭐⭐ | `remove(k)` / `(k,v)` | `V` or null / `boolean` | Remove key / exact pair | H/L expected O(1); T O(log n) | `map.remove("A")` | Drop state | Remove mapping |
| ⭐⭐⭐ | `replace(k,v)` / `(k,old,new)` | `V` or null / `boolean` | Update only existing mapping | H/L expected O(1); T O(log n) | `map.replace("A",2)` | Update if present | Conditional update |
| ⭐⭐ | `replaceAll(fn)` | `void` | Recompute every value | Full traversal + function cost | `map.replaceAll((k,v)->v+1)` | Rare | Normalize values |
| ⭐⭐ | `compute(k,fn)` | `V` new or null | Recompute even if absent | One key lookup + function | `map.compute("A",(k,v)->1)` | Flexible count | Conditional logic |
| ⭐⭐⭐⭐ | `computeIfAbsent(k,fn)` | `V` existing/new or null | Create a non-null value if absent/null | One key lookup + function | `groups.computeIfAbsent(k,x->new ArrayList<>())` | Grouping | Group rows |
| ⭐⭐ | `computeIfPresent(k,fn)` | `V` new or null | Recompute only non-null mapping | One key lookup + function | `map.computeIfPresent("A",(k,v)->v+1)` | Rare | Update present value |
| ⭐⭐⭐⭐ | `merge(k,v,fn)` | `V` new or null | Insert or combine non-null value | One key lookup + function | `freq.merge(x,1,Integer::sum)` | Frequency count | Aggregate |
| ⭐⭐⭐⭐⭐ | `size()` | `int` | Count mappings | O(1) | `map.size()` | Distinct key count | Result size |
| ⭐⭐⭐⭐ | `isEmpty()` | `boolean` | Test no mappings | O(1) | `map.isEmpty()` | Edge case | Empty cache/result |
| ⭐⭐⭐ | `clear()` | `void` | Remove all mappings | Implementation/capacity dependent | `map.clear()` | Reset state | Reset local map |
| ⭐⭐⭐⭐⭐ | `keySet()` | `Set<K>` view | Traverse/test/remove keys | View creation cheap | `map.keySet()` | Scan keys | Process IDs |
| ⭐⭐⭐ | `values()` | `Collection<V>` view | Traverse values, duplicates possible | View creation cheap | `map.values()` | Scan counts | Process DTOs |
| ⭐⭐⭐⭐⭐ | `entrySet()` | `Set<Map.Entry<K,V>>` view | Traverse pairs | View creation cheap | `map.entrySet()` | Scan pairs | Transform mappings |
| ⭐⭐⭐ | `forEach(action)` | `void` | Visit each pair | H O(n+capacity); L/T O(n) | `map.forEach((k,v)->System.out.println(v))` | Simple visit | Process entries |
| ⭐⭐⭐ | `equals(o)` | `boolean` | Same key-value mappings | Depends on key lookups | `a.equals(b)` | Compare results | Compare config |
| ⭐ | `hashCode()` | `int` | Content-based map hash | Full traversal | `map.hashCode()` | Rare directly | Map as key (rare) |

The table describes key-operation structure, not arbitrary callback cost. The exact algorithm for `clear` and bulk traversal depends on implementation and capacity; avoid using a single bound for every map.

## `put`, `get`, defaults, and presence

```java
Map<String, Integer> map = new HashMap<>();
Integer before = map.put("A", 1); // null; new key
Integer replaced = map.put("A", 2); // 1; existing key
Integer now = map.get("A");       // 2
Integer missing = map.get("B");   // null
map.put("C", null);
Integer alsoNull = map.get("C");  // null, but C exists
boolean hasC = map.containsKey("C"); // true
Integer fallback = map.getOrDefault("B", 99); // 99
Integer storedNull = map.getOrDefault("C", 99); // null, not 99
```

`getOrDefault(k,d)` supplies `d` only when **no mapping exists**. If a key is present with a null value, it returns null. Therefore `map.get(k) != null` is not a reliable presence test when null values are allowed; use `containsKey(k)`. `containsValue` scans values and is usually much slower than hash-key lookup.

For frequency counting, a missing count is treated as zero, then incremented and stored:

```java
String s = "banana";
Map<Character, Integer> freq = new HashMap<>();
for (char c : s.toCharArray()) {
    freq.put(c, freq.getOrDefault(c, 0) + 1);
}
// b=1, a=3, n=2 (iteration order is unspecified)
```

`freq.getOrDefault(c, 0) + 1` means: read the current count if the key exists, otherwise start at `0`, then add one. This is a core DSA idiom. Counts should not be deliberately mapped to null; a present null would make the `+ 1` unboxing fail.

## Conditional updates and removal

```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
Integer previous = map.putIfAbsent("A", 9); // 1; remains 1
Integer inserted = map.putIfAbsent("B", 2); // null; B becomes 2
Integer old = map.replace("A", 3);          // 1; A becomes 3
boolean changed = map.replace("A", 3, 4);  // true; A becomes 4
boolean exactRemoved = map.remove("B", 2); // true
Integer removed = map.remove("A");         // 4
```

`putIfAbsent` fills a missing **or null-mapped** key. `if (!map.containsKey(k)) map.put(k,v)` differs for a key explicitly mapped to null. `replace(k,v)` updates only an existing key (even if its old value is null); `put` inserts or replaces. `replace(k,old,new)` succeeds only if the current value equals `old`. `remove(k)` returns the old value or null; `remove(k,v)` returns whether that exact key-value mapping was removed.

# 3. Traversing maps and their views

`keySet()` returns `Set<K>`, `values()` returns `Collection<V>` (not a `Set`, because values may repeat), and `entrySet()` returns `Set<Map.Entry<K,V>>`. A `Map.Entry` represents one key-value pair; `getKey()` and `getValue()` read it. On a modifiable map entry view, `setValue(v)` can replace its value, but the interface permits implementations that do not support this operation.

```java
Map<String, Integer> map = new LinkedHashMap<>();
map.put("A", 1);
map.put("B", 1);
for (String key : map.keySet()) {
    System.out.println(key);
}
for (Integer value : map.values()) {
    System.out.println(value); // 1 can appear twice
}
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    String key = entry.getKey();
    Integer value = entry.getValue();
    if (key.equals("A")) entry.setValue(value + 1);
}
map.forEach((key, value) -> System.out.println(key + ": " + value));
```

Use `entrySet()` when both key and value are needed. Iterating `keySet()` and calling `map.get(key)` each time adds another lookup; for `HashMap` it may still be expected linear overall, but it does extra work. For `TreeMap`, those repeated lookups can make traversal O(n log n) instead of O(n). `forEach((key,value) -> ...)` is a concise alternative; the two lambda parameters are the current key and value.

The three collections are **backed views**, not independent copies. Removing from a view removes the corresponding map entry; a map change appears in its views. Adding directly to these views is unsupported because a key alone or value alone cannot define a new mapping.

```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
map.put("B", 2);
Set<String> keys = map.keySet();
keys.remove("A");             // removes A→1 from map
boolean stillHasA = map.containsKey("A"); // false
map.values().remove(2);        // removes a matching mapping, here B→2
```

If several keys share a value, `values().remove(value)` removes one matching mapping; do not assume which one in an unordered map. During iteration, use the view iterator's `remove()` for supported structural removal. Direct `map.put/remove` while an iterator is active can cause `ConcurrentModificationException` in these implementations; fail-fast detection is best-effort, not a thread-safety guarantee. `Map.equals` compares the same mappings regardless of iteration order; a map's own `hashCode` is separate from a **key's** `hashCode` used by `HashMap`.

# 4. `HashMap`: fast expected key lookup

`HashMap<K,V>` implements `Map` with a hash table. It allows **one null key**, many null values, duplicate values, and one current value per equal key. It gives **no predictable iteration order**. With suitably distributed hashes, key lookup/update/removal is expected O(1).

```java
Map<String, Integer> scores = new HashMap<>();
scores.put(null, 0);
scores.put("Ada", null);
scores.put("Lin", null); // duplicate values allowed
```

## Hashing and key equality

```text
key → hashCode() → bucket → candidate entries
                              ↓ compare keys with equals()
                            matching entry → value
```

Conceptually, `put/get/containsKey/remove` hash the **key**, choose a bucket, then distinguish candidates with key equality. A collision means different keys land in the same bucket; it does **not** mean they are equal. For two keys, `equals` decides whether they represent one mapping. `HashMap` handles its null key specially.

> **Key contract:** If `a.equals(b)` is true, `a.hashCode() == b.hashCode()` must also be true. Unequal keys may share a hash. A custom key whose equality uses fields must implement `equals` and `hashCode` consistently; otherwise a map may fail to find or replace an apparently equal key.

```java
record User(long id) {}
Map<User, String> owners = new HashMap<>();
owners.put(new User(7), "Ada");
String found = owners.get(new User(7)); // "Ada" via record value equality
```

A record automatically supplies equality/hash behavior based on its components. A regular class inheriting `Object.equals` normally compares identity, so two separate instances with the same-looking fields may be distinct keys. Key fields used for equality/hash should remain stable while in a hash map.

## Expected and worst-case cost

| Operation | Average / expected | Conservative worst case | Practical note |
| --- | ---: | ---: | --- |
| `put()` | O(1) amortized | O(n) | Resize or poor collisions can cost more |
| `get()` | O(1) | O(n) | Key hash/equality quality matters |
| `containsKey()` | O(1) | O(n) | Same key lookup path |
| `remove()` | O(1) | O(n) | Same key lookup path |
| `containsValue()` | O(n + capacity) | O(n + capacity) | Scans entries/buckets |
| Iterate all | O(n + capacity) | O(n + capacity) | Empty buckets can add work |

These are conservative bounds for arbitrary custom keys. Java 21 can use tree-shaped collision buckets in some cases, improving many heavy-collision lookups, but the Map API does not promise unconditional O(1). The cost of user-defined `hashCode` and `equals` adds to the structural cost.

**Capacity** is the bucket-array size. **Load factor** determines when enough entries have accumulated to trigger a larger table. **Resizing/rehashing** allocates a larger table and redistributes entries; one insertion can then take O(n), while many ordinary insertions are cheap. The Java 21 default constructor documents initial capacity 16 and load factor 0.75. These are lower-priority tuning details: use the default unless a known large workload justifies pre-sizing. `new HashMap<>(1000)` requests initial **capacity**, not 1000 pre-existing entries; `size()` remains zero.

## Mutable keys: an interview and backend trap

Changing a field used by `equals`/`hashCode` after insertion can leave the entry in a bucket chosen using its **old** hash. Looking up or removing that same key object after mutation may fail. The general Map contract warns that behavior is unspecified when equality-relevant key state changes inside a map.

```java
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class MutableKey {
    int id;
    MutableKey(int id) { this.id = id; }
    @Override public boolean equals(Object other) {
        return other instanceof MutableKey k && id == k.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

class MutableMapExample {
    static void demonstrateRisk() {
        MutableKey key = new MutableKey(1);
        Map<MutableKey, String> map = new HashMap<>();
        map.put(key, "value");
        key.id = 2; // unsafe while key is in the map
        // map.get(key) is no longer reliable for this changed key.
    }
}
```

Prefer immutable identity fields. For backend entities with generated IDs, choose a stable equality policy before using instances as hash keys.

# 5. `LinkedHashMap`: predictable encounter order

`LinkedHashMap<K,V>` extends `HashMap` with links that record order. By default, iteration follows **insertion order**. Replacing an existing key's value does not move that key. It accepts a null key and null values, usually has expected O(1) key operations, and pays some extra memory/bookkeeping for links. Iterating its views is O(n), independent of bucket capacity.

```java
Map<String, Integer> map = new LinkedHashMap<>();
map.put("C", 3);
map.put("A", 1);
map.put("B", 2);
map.put("A", 9); // changes A's value, not its insertion position
for (String key : map.keySet()) {
    System.out.println(key); // C, then A, then B
}
```

| Feature | `HashMap` | `LinkedHashMap` |
| --- | --- | --- |
| Ordering | Unspecified | Insertion order by default; optional access order |
| Average key lookup | Expected O(1) | Expected O(1) |
| Memory | Hash table | Hash table plus order links |
| Iteration predictability | None | Defined encounter order |
| Iteration cost | O(n + capacity) | O(n) |
| DSA use | Frequency/index/grouping | Stable-order output, LRU-style problems |
| Backend use | Lookup/aggregation | Stable unique-key result order |

Use `HashMap` when order is irrelevant. Use `LinkedHashMap` when a caller needs predictable first-insertion order, such as an explicitly ordered aggregation result. A persistence query or API still needs its own ordering contract; an in-memory map alone does not order database rows.

## Access order and eldest-entry hook

The constructor `new LinkedHashMap<>(initialCapacity, loadFactor, true)` uses **access order** instead of insertion order: a successful `get` moves an entry toward the most recently accessed end. This is useful for LRU-style caches.

```java
LinkedHashMap<String, Integer> recent = new LinkedHashMap<>(16, 0.75f, true);
recent.put("A", 1);
recent.put("B", 2);
recent.put("C", 3);
recent.get("A"); // encounter order is now B, C, A
```

A subclass can override `removeEldestEntry` to evict the eldest entry **after a new insertion** when a size rule says so. This is ⭐⭐ for general learning: useful in LRU interviews, but production caches often need concurrency, expiration, metrics, and dedicated tools. Access-order reads can change iteration order, so do not change order while actively iterating the same map.

```java
Map<Integer, String> bounded = new LinkedHashMap<Integer, String>(16, 0.75f, true) {
    @Override protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > 3;
    }
};
bounded.put(1, "A");
bounded.put(2, "B");
bounded.put(3, "C");
bounded.put(4, "D"); // evicts eldest key 1
```

# 6. `TreeMap`: sorted keys and nearest-key queries

`TreeMap<K,V>` implements `NavigableMap<K,V>` (hence `SortedMap` and `Map`). It stores **keys** in sorted order using a balanced search tree; values are not sorted and may repeat. Its core `put`, `get`, `containsKey`, and `remove` operations take O(log n) comparisons.

```java
NavigableMap<Integer, String> map = new TreeMap<>();
map.put(30, "C");
map.put(10, "A");
map.put(20, "B");
for (Integer key : map.keySet()) {
    System.out.println(key); // 10, then 20, then 30
}
```

`TreeMap<Integer,String>` uses `Integer` natural order, and `TreeMap<String,Integer>` uses `String` lexicographic order. Natural ordering comes from `Comparable.compareTo`; alternatively, supply a comparator for custom order:

```java
NavigableMap<Integer, String> descending = new TreeMap<>(Comparator.reverseOrder());
descending.put(10, "A");
descending.put(30, "C");
descending.put(20, "B");
System.out.println(descending.keySet()); // [30, 20, 10]
```

Declare a variable as `NavigableMap` or `TreeMap` when you need `floorKey` and other navigation methods; a variable declared merely `Map` does not expose them. Navigation follows the comparator's order, which may reverse ordinary numeric meaning.

> **Important:** A `TreeMap` treats two keys as the **same key** when its comparator or `compareTo` returns 0. For the general `Map` contract to behave as expected, this ordering should be consistent with `equals`. A comparator that uses only a person's name may merge two otherwise unequal person keys with the same name.

## TreeMap navigation and range methods

The examples assume `NavigableMap<Integer,String> t` with keys `10, 20, 30, 40`. The key methods return `K`; the entry methods return `Map.Entry<K,V>` so you can inspect both parts.

| Priority | Method | Return Type | Purpose | Complexity | Example | DSA Use |
| --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐ | `firstKey()` | `K` | Smallest key; throws if empty | O(log n) typical | `t.firstKey()` | Minimum key |
| ⭐⭐⭐ | `lastKey()` | `K` | Largest key; throws if empty | O(log n) typical | `t.lastKey()` | Maximum key |
| ⭐⭐ | `firstEntry()` | `Map.Entry<K,V>` or null | First mapping | O(log n) typical | `t.firstEntry()` | Minimum pair |
| ⭐⭐ | `lastEntry()` | `Map.Entry<K,V>` or null | Last mapping | O(log n) typical | `t.lastEntry()` | Maximum pair |
| ⭐⭐⭐⭐ | `lowerKey(x)` | `K` or null | Greatest key strictly below x | O(log n) | `t.lowerKey(20)` | Predecessor |
| ⭐⭐⭐⭐ | `higherKey(x)` | `K` or null | Least key strictly above x | O(log n) | `t.higherKey(20)` | Successor |
| ⭐⭐⭐⭐ | `floorKey(x)` | `K` or null | Greatest key at/below x | O(log n) | `t.floorKey(20)` | Nearest ≤ x |
| ⭐⭐⭐⭐ | `ceilingKey(x)` | `K` or null | Least key at/above x | O(log n) | `t.ceilingKey(20)` | Nearest ≥ x |
| ⭐⭐ | `lowerEntry(x)` | `Map.Entry<K,V>` or null | Pair for `lowerKey` | O(log n) | `t.lowerEntry(20)` | Predecessor pair |
| ⭐⭐ | `higherEntry(x)` | `Map.Entry<K,V>` or null | Pair for `higherKey` | O(log n) | `t.higherEntry(20)` | Successor pair |
| ⭐⭐ | `floorEntry(x)` | `Map.Entry<K,V>` or null | Pair for `floorKey` | O(log n) | `t.floorEntry(20)` | Nearest pair |
| ⭐⭐ | `ceilingEntry(x)` | `Map.Entry<K,V>` or null | Pair for `ceilingKey` | O(log n) | `t.ceilingEntry(20)` | Nearest pair |
| ⭐⭐ | `pollFirstEntry()` | `Map.Entry<K,V>` or null | Remove/return first pair | O(log n) | `t.pollFirstEntry()` | Consume minimum |
| ⭐⭐ | `pollLastEntry()` | `Map.Entry<K,V>` or null | Remove/return last pair | O(log n) | `t.pollLastEntry()` | Consume maximum |
| ⭐⭐ | `headMap(to)` | `SortedMap<K,V>` view | Keys strictly below `to` | Cheap view; queries O(log n) | `t.headMap(30)` | Bounded keys |
| ⭐⭐ | `tailMap(from)` | `SortedMap<K,V>` view | Keys at/above `from` | Cheap view; queries O(log n) | `t.tailMap(20)` | Bounded keys |
| ⭐⭐ | `subMap(from,to)` | `SortedMap<K,V>` view | Keys in `[from,to)` | Cheap view; queries O(log n) | `t.subMap(20,40)` | Range queries |
| ⭐⭐ | `descendingMap()` | `NavigableMap<K,V>` view | Reverse key-order view | Cheap view; traversal O(n) | `t.descendingMap()` | Reverse output |

```java
NavigableMap<Integer, String> t = new TreeMap<>();
t.put(10, "A");
t.put(20, "B");
t.put(30, "C");
t.put(40, "D");
Integer lower = t.lowerKey(20);     // 10, strictly less
Integer floor = t.floorKey(20);     // 20, less or equal
Integer higher = t.higherKey(20);   // 30, strictly greater
Integer ceiling = t.ceilingKey(20); // 20, greater or equal
Map.Entry<Integer, String> floorPair = t.floorEntry(20); // 20=B
Integer absent = t.lowerKey(10);   // null: no smaller key
SortedMap<Integer, String> head = t.headMap(30);   // keys 10,20
SortedMap<Integer, String> tail = t.tailMap(20);   // keys 20,30,40
SortedMap<Integer, String> middle = t.subMap(20, 40); // keys 20,30
NavigableMap<Integer, String> reverseView = t.descendingMap();
```

`headMap(to)` excludes `to`; `tailMap(from)` includes `from`; `subMap(from,to)` includes `from` and excludes `to`. Boolean-overload versions let you choose each endpoint. These are **backed views**: edits through a view affect the original map, and an out-of-range insertion through a range view throws `IllegalArgumentException`. Copy the view into `new TreeMap<>(view)` for independent storage. `firstKey`/`lastKey` throw `NoSuchElementException` when empty; `firstEntry`/`lastEntry`, neighbor methods, and `poll` methods return null when no entry qualifies.

## Nulls and complexity

`HashMap` and `LinkedHashMap` accept one null key and multiple null values. A natural-order `TreeMap` rejects a null key because it cannot compare it with other keys; it **does** accept null values. A custom comparator that explicitly supports null can permit a null key, so “TreeMap never allows null keys” is too broad. Prefer non-null keys in ordinary sorted-map code.

```java
NavigableMap<String, Integer> nullableKeys = new TreeMap<>(
    Comparator.nullsFirst(Comparator.naturalOrder())
);
nullableKeys.put(null, 1); // accepted by this comparator
nullableKeys.put("A", null); // null value is allowed
```

| Operation | Typical tree complexity |
| --- | ---: |
| `put()` | O(log n) |
| `get()` | O(log n) |
| `containsKey()` | O(log n) |
| `remove()` | O(log n) |
| `firstKey()` / `lastKey()` | O(log n) typical |
| `floorKey()` / `ceilingKey()` | O(log n) |
| Iterate all | O(n), sorted by key |

A balanced tree keeps its height logarithmic as entries change. The Java API explicitly guarantees log n for core `containsKey`, `get`, `put`, and `remove`; the other figures describe typical tree navigation. `containsValue` still scans values in O(n).

# 7. Choosing among the three implementations

| Feature | `HashMap` | `LinkedHashMap` | `TreeMap` |
| --- | --- | --- | --- |
| Key uniqueness | Equality/hash based | Equality/hash based | Comparator result 0 |
| Duplicate values | Yes | Yes | Yes |
| Key order | Unspecified | Insertion order by default; optional access order | Sorted comparator order |
| Sorted keys | No | No | Yes |
| Null key | One | One | No natural-order null; comparator may allow |
| Null values | Yes | Yes | Yes |
| Average `get()` | Expected O(1) | Expected O(1) | O(log n) |
| Average `put()` | Expected O(1) amortized | Expected O(1) amortized | O(log n) |
| Memory | Hash table | Hash table plus order links | Tree nodes and links |
| Iteration order | No guarantee | Predictable encounter order | Sorted keys |
| DSA usefulness | Counts, indexes, grouping | Order-preserving answers, LRU | Nearest/range keys |
| Backend usefulness | Lookup/aggregation | Stable ordered results | Sorted schedules/ranges |

Choose based on the required **key order** and operations. Hash maps buy expected fast lookup; linked hash maps add encounter order; tree maps pay logarithmic cost for sorted order and navigation. None is automatically thread-safe for concurrent mutation.

| Scenario | Likely choice | Tradeoff |
| --- | --- | --- |
| Fast key lookup | `HashMap` | Expected O(1), no order promise |
| Preserve insertion order | `LinkedHashMap` | Extra links/memory for predictable iteration |
| Keep keys always sorted | `TreeMap` | O(log n) updates/lookups instead of expected O(1) |
| Find `floorKey`/`ceilingKey` | `TreeMap` | Direct nearest-key navigation |
| Frequency counting | `HashMap` | Key order usually irrelevant |
| Group items by category | `HashMap`; `LinkedHashMap` if group order matters | Value lists hold each group's members |
| Backend API needs predictable collection iteration | `LinkedHashMap` when encounter order is contractual | Still define database/query/API ordering separately |
| LRU-style bounded cache exercise | Access-order `LinkedHashMap` | Good interview model; production caches need more features |

# 8. DSA patterns with maps

## Frequency counting

```java
int[] nums = {2, 3, 2, 2, 3};
Map<Integer, Integer> freq = new HashMap<>();
for (int num : nums) {
    freq.put(num, freq.getOrDefault(num, 0) + 1);
}
// freq maps 2→3 and 3→2
```

Line by line: create an empty map from value to count; visit each number; read its old count or zero; add one; store the new count. With suitable hashing, the loop is expected O(n) time and O(u) space for `u` distinct numbers. For a string, use `Map<Character,Integer>` and visit its `char` values; remember a `char` is a UTF-16 code unit, not necessarily a complete Unicode character.

```java
String text = "banana";
Map<Character, Integer> charFreq = new HashMap<>();
for (char c : text.toCharArray()) {
    charFreq.put(c, charFreq.getOrDefault(c, 0) + 1);
}
```

`merge` is a concise alternative; many beginners find the explicit `getOrDefault` form easier to read first:

```java
int[] nums = {2, 3, 2};
Map<Integer, Integer> freq = new HashMap<>();
for (int num : nums) {
    freq.merge(num, 1, Integer::sum);
}
```

For a missing or null-mapped key, `merge(k,1,...)` stores 1. Otherwise `Integer::sum` combines the old count and 1. The supplied value must be non-null, and a remapping function that returns null removes the mapping.

## Two Sum: value to earlier index

```java
import java.util.HashMap;
import java.util.Map;

class TwoSumPattern {
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> indexByValue = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int need = target - nums[i];
            if (indexByValue.containsKey(need)) {
                return new int[]{indexByValue.get(need), i};
            }
            indexByValue.put(nums[i], i);
        }
        return new int[0]; // no pair found
    }
}
```

The map stores each earlier value and its index. For each position, look up the complement before storing the current value, so the same index is not reused. This is expected O(n) time and O(n) space with suitable hashing, versus O(n²) time for checking every pair. Integer overflow may matter if the problem allows extreme values; handle that according to the problem's constraints.

## Grouping with `computeIfAbsent`

```java
List<String> words = List.of("apple", "ant", "boat");
Map<String, List<String>> groups = new HashMap<>();
for (String word : words) {
    String key = word.substring(0, 1);
    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
}
// "a" maps to ["apple", "ant"]; "b" maps to ["boat"]
```

`computeIfAbsent(key, k -> new ArrayList<>())` checks for a non-null list at `key`. If absent or null-mapped, it creates and stores a new list. It then returns that list, and `.add(word)` appends the word. The lambda's `k` is the missing key; it is not used in this simple constructor. This pattern also fits anagram groups, records by category, and users by role.

`getOrDefault` is ideal when you need a **fallback value to read**, such as zero for a count; it does **not** insert that fallback. `computeIfAbsent` is ideal when you need to **create and store** a reusable object such as a list. Both treat missing mappings usefully, but `getOrDefault` returns a mapped null whereas `computeIfAbsent` tries to compute when mapped null.

## Other common DSA roles

| Pattern | What the map stores |
| --- | --- |
| Frequency/counting occurrences | Value/character → count |
| Memoization | Problem state → computed answer |
| Index tracking | Value → latest or earliest index |
| Prefix sum technique | Prefix total → count or earlier index |
| Sliding window | Character/value → count or last position |
| ID lookup | ID → object |
| Graph adjacency | Node → neighbor list |
| Anagram grouping | Canonical signature → list of words |

The map supplies fast expected lookup by a meaningful key; each algorithm has its own invariants and edge cases, which belong in separate DSA topics.

## Nested maps

```java
Map<String, Map<String, Integer>> countsByTeam = new HashMap<>();
countsByTeam.computeIfAbsent("backend", k -> new HashMap<>())
            .merge("open", 1, Integer::sum);
int open = countsByTeam.get("backend").getOrDefault("open", 0); // 1
```

The outer key selects a team; the inner map holds status counts for that team. Nested maps are useful when two keys identify an aggregate. Use them only when a small domain type would not be clearer.

# 9. Maps in backend development

Common shapes include `Map<String,String>` for simple settings, `Map<String,Integer>` for counts, `Map<Long,User>` for ID lookup, `Map<String,List<User>>` for grouping, and `Map<String,Object>` for truly dynamic data. Repository results, DTO transformations, permission lookups, configuration, and response aggregation often use maps internally.

```java
record User(long id, String name) {}
List<User> users = List.of(new User(1, "Ada"), new User(2, "Lin"));
Map<Long, User> byId = new HashMap<>();
for (User user : users) byId.put(user.id(), user);
User selected = byId.get(2L); // Lin
```

A JSON object conceptually resembles string keys mapped to values:

```json
{"name": "John", "age": 21}
```

`Map<String,Object>` can represent flexible JSON-like fields, but strongly typed DTOs are usually better for structured backend APIs: they document expected fields/types and make validation and refactoring clearer. Use a dynamic map when the shape is genuinely variable.

```java
record User(long id, String name) {}
Map<Long, User> cache = new HashMap<>();
cache.put(7L, new User(7, "Ada"));
User cached = cache.get(7L);
```

This illustrates ID lookup, **not** a production cache. Real caches may need concurrency, eviction, expiry/TTL, and size limits. `HashMap` is not safe for arbitrary concurrent mutation; choose a suitable concurrent collection or caching library when needed.

# 10. `compute`, `computeIfPresent`, and `merge`

These methods combine lookup and update logic. Their callbacks can return `null`, which often means **remove or do not create** a mapping; read the method's contract before using a nullable result.

```java
Map<String, Integer> counts = new HashMap<>();
counts.put("A", 2);
Integer present = counts.computeIfPresent("A", (k, v) -> v + 1); // 3
Integer absent = counts.computeIfPresent("B", (k, v) -> v + 1);  // null, no call
Integer recomputed = counts.compute("B", (k, v) -> v == null ? 1 : v + 1); // 1
Integer combined = counts.merge("B", 1, Integer::sum); // 2
```

- `computeIfAbsent` (⭐⭐⭐⭐): run a key-to-value function only for a missing or null-mapped key; store a non-null result. Excellent for grouping.
- `computeIfPresent` (⭐⭐): run only when an existing mapping has a **non-null** value; a null result removes it.
- `compute` (⭐⭐): run whether the key is absent or present, passing the current value or null; a null result removes an existing mapping. Powerful but easy to misread.
- `merge` (⭐⭐⭐⭐): supply a **non-null** starting value; if no non-null current value exists, store it; otherwise combine old and supplied values. A null combine result removes the mapping.

Avoid structural edits to the same map from inside a mapping function unless the implementation's contract explicitly permits them. In ordinary code, keep these functions short and focused. They are not a substitute for a concurrent map when multiple threads mutate the same data.

# 11. Common mistakes and thread safety

| Mistake | Correct understanding |
| --- | --- |
| Treating `map.get(0)` as positional access | `0` is a key, if compatible; maps have no numeric index |
| Confusing keys with values | `get(key)` looks up a key; `containsValue(value)` scans values |
| Expecting duplicate keys | Equal keys share one current mapping; later `put` replaces the value |
| Forgetting `put` replacement | Use its old-value return or `containsKey` when replacement matters |
| Using `containsValue` for key existence | `containsKey` is the intended, usually faster operation |
| Assuming `HashMap` insertion order | Its iteration order is unspecified and may change |
| Assuming `LinkedHashMap` sorts keys | It normally preserves insertion order; access-order mode differs |
| Assuming `TreeMap` insertion order | It iterates by comparator/natural **key** order |
| Mutating a hashed key field | Lookup/removal can become unreliable |
| Inconsistent key `equals` and `hashCode` | Equal keys must have equal hash codes |
| Testing `map.get(k) != null` for presence | A present key may map to null; use `containsKey(k)` |
| Iterating keys then calling `get` for every value | `entrySet` reads pairs directly and avoids extra lookups |
| Direct structural mutation during iteration | Use the view iterator's `remove` when supported |
| Assuming `HashMap` is thread-safe | It is not safe for arbitrary concurrent mutation |
| Confusing `HashMap` and `HashSet` | Map stores key→value; Set stores unique elements |
| Treating `Map` as a `Collection` subtype | Map has collection views but does not extend Collection |
| Using raw `Map map = new HashMap()` | Use `Map<String,Integer> map = new HashMap<>()` for type safety |

`HashMap`, `LinkedHashMap`, and `TreeMap` are generally **not thread-safe for arbitrary concurrent mutation**. A backend service may handle requests on multiple threads, so do not put an ordinary mutable map in shared state without a concurrency design. `ConcurrentHashMap` is one important alternative, but its null and atomic-operation rules differ; learn concurrent collections separately.

> **Backend note:** A map local to one request/method is different from a map shared across all requests. The shared case needs deliberate synchronization, concurrent data structures, or a cache service.

# 12. Short interview questions

| Question | Short answer |
| --- | --- |
| What is a Map? | An interface associating unique keys with current values. |
| Is Map a Collection? | It is in the Collections Framework but does not extend `Collection`. |
| Map versus Set? | Key→value pairs versus unique standalone elements. |
| Map versus List? | Key lookup without positions versus an indexed sequence. |
| Duplicate keys? | No; putting an equal key replaces its value. |
| Duplicate values? | Yes; different keys may map to the same value. |
| Existing-key `put`? | Replaces value and returns the previous value. |
| `get` versus `getOrDefault`? | `get` returns null if absent; the latter returns a chosen default only if absent. |
| `containsKey` versus `containsValue`? | Key presence lookup versus value scan. |
| HashMap / LinkedHashMap / TreeMap? | Unordered hash / encounter-ordered hash / sorted-key tree. |
| How does HashMap work? | It hashes a key to a bucket, then checks candidate keys for equality. |
| What is a hash collision? | Unequal keys land in the same bucket; equality still distinguishes them. |
| Why `equals` and `hashCode`? | Equal keys must hash alike so lookups reach matching candidates. |
| HashMap null key? | Yes, one. |
| HashMap null values? | Yes, for multiple keys. |
| TreeMap null keys? | Natural order: no; a null-aware comparator may allow one. |
| Why is HashMap lookup usually O(1)? | Well-distributed hashes keep candidate buckets small on average. |
| Why TreeMap O(log n)? | Balanced tree height grows logarithmically. |
| Does HashMap preserve insertion order? | No. |
| Does LinkedHashMap preserve insertion order? | By default, yes; it can also use access order. |
| Does TreeMap sort keys? | Yes, by natural order or comparator. Values need not be sortable. |
| `keySet` / `values` / `entrySet`? | Backed views of keys / values / key-value entries. |
| Why use `entrySet`? | It provides key and value together without a second lookup. |
| What is `Map.Entry`? | One key-value mapping with `getKey` and `getValue`. |
| `computeIfAbsent`? | Compute/store non-null value for an absent or null-mapped key. |
| When use `getOrDefault`? | Read a fallback such as zero without inserting it. |
| `merge`? | Insert a starting value or combine it with the current non-null value. |
| Is HashMap thread-safe? | No, not for arbitrary concurrent mutation. |
| Why avoid mutable keys? | Changing equality/hash fields can make mappings hard to find or remove. |

# 13. Master complexity comparison

H/L figures are **expected** with suitable hashing; pathological collisions or a resize can make an individual operation slower. `c` is hash-table capacity. TreeMap's sorted order costs O(log n) for core key operations. These structural figures exclude unusually expensive key `hashCode`, `equals`, or comparator work.

| Operation | `HashMap` | `LinkedHashMap` | `TreeMap` |
| --- | ---: | ---: | ---: |
| `put()` | Expected O(1) amortized | Expected O(1) amortized | O(log n) |
| `get()` | Expected O(1) | Expected O(1) | O(log n) |
| `containsKey()` | Expected O(1) | Expected O(1) | O(log n) |
| `remove()` | Expected O(1) | Expected O(1) | O(log n) |
| `containsValue()` | O(n + c) | O(n) | O(n) |
| Iterate all entries | O(n + c) | O(n) | O(n), key sorted |
| Find smallest key | O(n + c) scan | O(n) scan | `firstKey()` O(log n) typical |
| Find largest key | O(n + c) scan | O(n) scan | `lastKey()` O(log n) typical |
| `floorKey()` | N/A | N/A | O(log n) |
| `ceilingKey()` | N/A | N/A | O(log n) |

`HashMap` lookup can degrade toward O(n) for pathological custom keys/collisions, although Java 21 can treeify some heavy-collision buckets. One resize may cost O(n). `LinkedHashMap` adds order links and memory; `TreeMap` adds tree nodes and comparisons. HashMap iteration depends on capacity as well as size, so extreme over-allocation can slow a full scan.

# 14. DSA syntax cheat sheet

The class is compilable; use only the statements needed in an actual problem. A `NavigableMap` or `TreeMap` variable is needed for navigation methods.

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

class MapCheatSheet {
    static void demo(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        int key = 7;
        int value = 3;
        map.put(key, value);
        Integer found = map.get(key);
        int count = map.getOrDefault(key, 0);
        boolean hasKey = map.containsKey(key);
        boolean hasValue = map.containsValue(value);
        Integer removed = map.remove(key);
        int size = map.size();
        boolean empty = map.isEmpty();
        map.clear();

        Map<Integer, Integer> mergedCounts = new HashMap<>();
        for (int num : nums) {
            map.put(num, map.getOrDefault(num, 0) + 1);
            mergedCounts.merge(num, 1, Integer::sum); // alternative idiom
        }

        Map<String, List<String>> groups = new HashMap<>();
        String groupKey = "A";
        String item = "Ada";
        groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(item);

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer entryKey = entry.getKey();
            Integer entryValue = entry.getValue();
        }
        for (Integer eachKey : map.keySet()) System.out.println(eachKey);
        for (Integer eachValue : map.values()) System.out.println(eachValue);

        Map<Integer, String> insertionOrder = new LinkedHashMap<>();
        NavigableMap<Integer, String> sorted = new TreeMap<>();
        NavigableMap<Integer, String> descending = new TreeMap<>(Comparator.reverseOrder());
        sorted.put(10, "A");
        sorted.put(20, "B");
        Integer first = sorted.firstKey();
        Integer last = sorted.lastKey();
        Integer lower = sorted.lowerKey(key);
        Integer floor = sorted.floorKey(key);
        Integer higher = sorted.higherKey(key);
        Integer ceiling = sorted.ceilingKey(key);
    }
}
```

The frequency loop builds the same counts in **two separate maps** to show the `getOrDefault` and `merge` alternatives. In a real problem, choose one style.

# What Should I Memorize?

### ⭐⭐⭐⭐⭐ Must Know

- `Map<K,V>` stores one current value per unique key; values may repeat. `Map` does not extend `Collection`.
- `HashMap` with `put`, `get`, `getOrDefault`, `containsKey`, `remove`, and `size`.
- `entrySet`, `Map.Entry.getKey/getValue`, and `keySet` for traversal.
- Frequency counting and grouping patterns; correct key `equals`/`hashCode`; expected O(1) hash lookup, with worst-case caveats.

### ⭐⭐⭐⭐ Very Important

- `LinkedHashMap` insertion order, `TreeMap` sorted-key order, and why `HashMap` order must not be relied on.
- `computeIfAbsent` for groups, `merge` for counts, `putIfAbsent` for initial values.
- TreeMap `lowerKey`/`floorKey`/`higherKey`/`ceilingKey` when nearest keys matter.
- Missing key versus null-mapped key; `getOrDefault` versus `containsKey`.

### ⭐⭐⭐ Useful

- `values()` backed view, `Map.Entry.setValue`, `replace`, conditional `remove`, and map equality.
- Two Sum value-to-index maps, nested maps, and explicit backend result ordering.

### ⭐⭐ Low Priority for Now

- `compute`, `computeIfPresent`, `replaceAll`, TreeMap range/descending views, and access-order `LinkedHashMap` with `removeEldestEntry`.

### ⭐ Rarely Needed for Now

- Calling a map's own `hashCode`, manual capacity/load-factor tuning, and building a production cache from raw `LinkedHashMap` alone.

References: [Java SE 21 `Map`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html), [`HashMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html), [`LinkedHashMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html), [`TreeMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html), and [`NavigableMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/NavigableMap.html).
