# Java String: A Practical Reference

This guide uses Java 21 syntax. `String` and `StringBuilder` are in `java.lang`, so they normally need no import. Short Java blocks are **method-body examples**; where needed, their imports or complete class are shown.

# 1. What a `String` is

```java
String text = "Hello";
char first = 'H';
```

`java.lang.String` is a **class**, not a primitive type. A `String` object represents a sequence of characters and provides methods for searching, comparing, and transforming that sequence. A `char` is one primitive UTF-16 code unit; a `String` can contain zero or more such units. The distinction matters for some Unicode characters: one visible character may occupy two `char` positions. For common English-letter DSA problems, one index usually corresponds to one letter, but that assumption does not cover all text.

## Literals, `new`, and the String Pool

```java
String a = "hello";
String b = "hello";
String c = new String("hello");
System.out.println(a == b); // true: same pooled literal reference
System.out.println(a == c); // false: c refers to a distinct object
System.out.println(a.equals(c)); // true: same content
```

String literals are **interned**: equal literals normally refer to the same canonical object in the String Pool. `new String("hello")` explicitly creates another `String` object with the same contents, so it is usually unnecessary and adds an object. The variables hold references, not the characters themselves.

```text
a ─┐
   ├──> pooled String "hello"
b ─┘
c ─────> separate String "hello"
```

`c.intern()` returns the canonical pooled reference for the same contents. Pooling is an optimization and an interview topic; **use `equals` for content comparison** regardless of how a string was created. Do not assume a string assembled at runtime is the same object as a literal.

## Immutability

A `String` cannot change its contents after creation. Methods such as `concat`, `replace`, and `toUpperCase` return a result; they do not edit the original.

```java
String s = "Hello";
s.concat(" World");             // returned result is ignored
System.out.println(s);          // Hello
s = s.concat(" World");         // s now refers to another String
System.out.println(s);          // Hello World
```

Immutability makes it safe to share a `String` object between threads, lets pooled references be reused, and keeps a string's content stable while it is a `HashMap` key. It also prevents code holding a reference from changing a validated value behind another caller's back. A variable containing a string reference can still be reassigned, and `null` remains possible.

## Indexes, `length()`, and traversal

For `"Java"`, the indexes are:

```text
J a v a
0 1 2 3
```

Valid `charAt` indexes are `0` through `s.length() - 1`. An invalid index throws `StringIndexOutOfBoundsException`. `charAt(int)` returns a `char` in **O(1)** time. `length()` is a **method**, unlike an array's `length` **field**.

```java
String s = "Java";
int count = s.length();   // 4; an array would use array.length
char c = s.charAt(1);    // 'a'
for (int i = 0; i < s.length(); i++) {
    System.out.println(s.charAt(i));
}
for (char letter : s.toCharArray()) {
    System.out.println(letter);
}
```

The indexed loop is best when you need positions, two pointers, or neighboring characters. `toCharArray()` creates a **new `char[]`** in O(n) time and space, useful when you will sort or edit characters. Both approaches work with UTF-16 code units; if a problem asks about arbitrary Unicode code points, use the `codePoints()`/code-point APIs instead of assuming each `char` is a whole character.

# 2. Comparing and searching

`==` compares **references**. `equals` compares **contents**. `compareTo` compares lexicographically and returns a negative value, zero, or a positive value; its magnitude is not the point. Lexicographic comparison examines characters from left to right, then treats a shorter matching prefix as smaller.

```java
String a = new String("hello");
String b = new String("hello");
System.out.println(a == b);             // false
System.out.println(a.equals(b));        // true
System.out.println("apple".compareTo("banana") < 0); // true
System.out.println("abc".compareTo("abcd") < 0);     // true
System.out.println("JAVA".equalsIgnoreCase("java")); // true
System.out.println("A".compareToIgnoreCase("a"));    // 0
```

`equalsIgnoreCase` and `compareToIgnoreCase` are convenient simple case-insensitive comparisons, but they do not provide full language-specific collation or normalization. For human-language sorting, a locale-aware `Collator` may be needed.

## Searching within a string

```java
String path = "api/users/42";
boolean hasUsers = path.contains("users"); // true
int firstSlash = path.indexOf('/');        // 3
int lastSlash = path.lastIndexOf('/');     // 9
boolean apiPath = path.startsWith("api/"); // true
boolean is42 = path.endsWith("42");      // true
int absent = path.indexOf("missing");    // -1
```

`contains` checks for a sequence; `indexOf`/`lastIndexOf` report the first/last matching index (or `-1`). `startsWith`/`endsWith` check prefixes/suffixes. `indexOf` has useful overloads for a character and a starting index. Remember that a found index of `0` is valid: test `>= 0`, not `> 0`.

## `substring`: inclusive start, exclusive end

```java
String s = "abcdef";
String fromTwo = s.substring(2);    // "cdef"
String middle = s.substring(2, 5); // "cde"
String empty = s.substring(2, 2);  // ""
```

The one-argument form runs from `beginIndex` to the end. The two-argument form selects `[beginIndex, endIndex)`. Valid bounds satisfy `0 <= beginIndex <= endIndex <= s.length()`; otherwise it throws `StringIndexOutOfBoundsException`. In modern Java, a nonempty substring is normally a new string with its own content storage, so extracting length `k` takes roughly **O(k) time and space**. Avoid creating many substrings inside a tight loop when indexes alone suffice.

# 3. Important `String` methods, ranked

Let `n` be this string's length, `m` a searched pattern's length, and `k` the result length. Costs are practical approximations: Unicode case conversion, regex, encoding, and implementation choices can change exact work. For searching, O(n·m) is a conservative worst-case mental model; typical cases may be faster. All methods below belong to `String` except the clearly marked static factories `String.join`, `String.valueOf`, and `String.format`.

| Priority | Method | Return Type | Purpose | Time Complexity | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `length()` | `int` | Count UTF-16 code units | O(1) | `s.length()` | Loop bounds | Validate length |
| ⭐⭐⭐⭐ | `isEmpty()` | `boolean` | Test length zero | O(1) | `s.isEmpty()` | Empty input | Empty field check |
| ⭐⭐⭐⭐ | `isBlank()` | `boolean` | Test only whitespace | O(n) | `s.isBlank()` | Edge cases | Blank input check |
| ⭐⭐⭐⭐⭐ | `charAt(i)` | `char` | Read indexed code unit | O(1) | `s.charAt(0)` | Scans/two pointers | Parse small tokens |
| ⭐⭐⭐⭐⭐ | `substring(begin[, end])` | `String` | Extract range | O(k) | `s.substring(1, 3)` | Substrings | Parse path/field |
| ⭐⭐⭐⭐ | `contains(seq)` | `boolean` | Test contained sequence | Search: up to O(n·m) | `s.contains("id")` | Pattern check | Filter text |
| ⭐⭐⭐⭐⭐ | `indexOf(x[, from])` | `int` | First match or `-1` | Search: up to O(n·m) | `s.indexOf(':')` | Locate boundary | Parse delimiter |
| ⭐⭐⭐ | `lastIndexOf(x)` | `int` | Last match or `-1` | Search: up to O(n·m) | `s.lastIndexOf('/')` | Reverse lookup | File/path suffix |
| ⭐⭐⭐⭐ | `startsWith(prefix)` | `boolean` | Check prefix | O(m) | `s.startsWith("api/")` | Prefix problems | Routing/validation |
| ⭐⭐⭐⭐ | `endsWith(suffix)` | `boolean` | Check suffix | O(m) | `s.endsWith(".csv")` | Suffix problems | Extension check |
| ⭐⭐⭐⭐⭐ | `equals(other)` | `boolean` | Content equality | O(n) worst case | `s.equals("ok")` | Correct comparison | Keys/validation |
| ⭐⭐⭐ | `equalsIgnoreCase(other)` | `boolean` | Ignore simple case differences | O(n) | `s.equalsIgnoreCase("yes")` | Occasional | User input |
| ⭐⭐⭐⭐ | `compareTo(other)` | `int` | Lexicographic order | O(min(n,m)) | `s.compareTo("z")` | Sort/order | Stable ordering rule |
| ⭐⭐ | `compareToIgnoreCase(other)` | `int` | Case-insensitive lexical order | O(min(n,m)) | `s.compareToIgnoreCase("z")` | Occasional | Simple ordering |
| ⭐⭐⭐⭐ | `toLowerCase()` / locale overload | `String` | Lowercase copy/result | O(n) typical | `s.toLowerCase(Locale.ROOT)` | Normalize input | Canonical keys |
| ⭐⭐⭐ | `toUpperCase()` / locale overload | `String` | Uppercase copy/result | O(n) typical | `s.toUpperCase(Locale.ROOT)` | Normalize input | Display/codes |
| ⭐⭐ | `trim()` | `String` | Remove edge chars `<= U+0020` | O(n) | `s.trim()` | Occasional | Legacy input cleanup |
| ⭐⭐⭐⭐ | `strip()` | `String` | Remove Unicode-aware edge whitespace | O(n) | `s.strip()` | Input cleanup | Form parsing |
| ⭐⭐ | `stripLeading()` | `String` | Remove leading whitespace | O(n) | `s.stripLeading()` | Occasional | Text import |
| ⭐⭐ | `stripTrailing()` | `String` | Remove trailing whitespace | O(n) | `s.stripTrailing()` | Occasional | Text import |
| ⭐⭐⭐⭐ | `replace(old, new)` | `String` | Literal replacement | Scan/output cost | `s.replace(".", "-")` | Text edits | Sanitize known tokens |
| ⭐⭐ | `replaceFirst(regex, repl)` | `String` | Replace first regex match | Regex-dependent | `s.replaceFirst("\\d+", "#")` | Rare | Targeted cleanup |
| ⭐⭐⭐ | `replaceAll(regex, repl)` | `String` | Replace all regex matches | Regex-dependent | `s.replaceAll("\\s+", " ")` | Occasional | Normalize text |
| ⭐⭐⭐⭐ | `split(regex[, limit])` | `String[]` | Split by regex | Regex/output-dependent | `s.split(",", -1)` | Parse input | Parse delimited fields |
| ⭐⭐⭐ | `String.join(delimiter, ...)` | `String` | Join pieces | O(total output) | `String.join("-", "a", "b")` | Build answer | CSV-like output |
| ⭐⭐ | `concat(other)` | `String` | Append string | O(n + m) | `s.concat("!")` | Rare; `+` clearer | Simple composition |
| ⭐⭐ | `repeat(count)` | `String` | Repeat content | O(result length) | `"-".repeat(3)` | Test patterns | Formatting |
| ⭐⭐ | `matches(regex)` | `boolean` | Match entire string against regex | Regex-dependent | `s.matches("[a-z]+")` | Rare | Simple validation |
| ⭐⭐⭐⭐⭐ | `toCharArray()` | `char[]` | Copy code units to array | O(n) | `s.toCharArray()` | Sort/edit characters | Interop |
| ⭐⭐⭐ | `getBytes(charset)` | `byte[]` | Encode text | O(n) typical | `s.getBytes(UTF_8)` | Rare | I/O and protocols |
| ⭐⭐⭐⭐ | `String.valueOf(value)` | `String` | Convert primitive/object/`char[]` | Depends on value/output | `String.valueOf(chars)` | Build answer | Serialization helpers |
| ⭐⭐⭐ | `String.format(fmt, args)` | `String` | Format values | Output/format-dependent | `String.format("%d", 42)` | Rare | Messages/reports |
| ⭐⭐ | `fmt.formatted(args)` | `String` | Format using receiver as template | Output/format-dependent | `"%d".formatted(42)` | Rare | Messages |
| ⭐⭐ | `hashCode()` | `int` | Content-derived hash | O(n) when computed; may be cached | `s.hashCode()` | Understand hash keys | Map/set keys |
| ⭐ | `intern()` | `String` | Return canonical pooled reference | Implementation-dependent | `s.intern()` | Interview concept | Rare; memory tradeoff |

For exact overloads and contracts, see the [Java SE 21 `String` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html).

# 4. Transforming and parsing text

## Case conversion and locale

```java
String lower = "Java".toLowerCase();
String upper = "Java".toUpperCase();
String key = "USER-ID".toLowerCase(java.util.Locale.ROOT);
```

The no-argument case methods use the default locale, which may vary between environments. For locale-independent backend identifiers and protocol-like keys, pass `Locale.ROOT`. Case conversion may change string length for some Unicode text; it is not a universal substitute for full language-aware or security-sensitive normalization.

## Whitespace: `trim` versus `strip`

```java
String raw = "  Java  ";
String a = raw.trim();          // "Java"
String b = raw.strip();         // "Java"
String c = raw.stripLeading();  // "Java  "
String d = raw.stripTrailing(); // "  Java"
```

`trim()` removes leading/trailing characters with code values at most U+0020. `strip()` and its one-sided variants use `Character.isWhitespace`, so they handle more Unicode whitespace. They do not necessarily remove *every* character people might visually call a space. None mutates `raw`.

## Literal replacement versus regex replacement

```java
String text = "a.b.3";
String literal = text.replace(".", "-");      // "a-b-3"
String regex = text.replaceAll(".", "-");     // "-----": dot matches any character
String firstNumber = text.replaceFirst("\\d+", "#"); // "a.b.#"
```

`replace(char/CharSequence, ...)` treats the target literally. `replaceAll` and `replaceFirst` treat the first argument as a **regular expression**; replacement text also has special handling for `$` and `\`. Use `replace` for plain text. Regex is a separate topic, but recognize these methods so an unescaped `.` or `|` does not surprise you.

## `split`: regex and empty fields

```java
String text = "Java,Spring,SQL";
String[] parts = text.split(",");        // ["Java", "Spring", "SQL"]
String[] dot = "a.b".split("\\.");      // ["a", "b"]
String[] pipe = "a|b".split("\\|");     // ["a", "b"]
String[] words = "a  b\tc".split("\\s+"); // ["a", "b", "c"]
String[] dropped = "a,,b,".split(","); // ["a", "", "b"]
String[] kept = "a,,b,".split(",", -1); // ["a", "", "b", ""]
```

`split` returns `String[]`, and its delimiter is a **regex**. `"."` means almost any character, not a literal dot; use `"\\."`. `"|"` is a regex alternation operator; use `"\\|"`. The default limit discards **trailing** empty fields, while a negative limit preserves them; internal empty fields remain. For complex CSV with quoting and embedded commas, `split(",")` is insufficient: use a CSV parser.

## Joining, concatenating, and formatting

```java
String joined = String.join("-", "Java", "Spring", "SQL");
String firstName = "Ada";
String lastName = "Lovelace";
String fullName = firstName + " " + lastName;
String hello = "Hello" + " World";
String formatted = String.format("Name: %s, Age: %d", "Ada", 36);
String alsoFormatted = "Name: %s, Age: %d".formatted("Ada", 36);
```

`+` is clear and fine for a few pieces; Java can optimize a single concatenation expression. Repeatedly appending to an immutable `String` inside a loop can create many intermediate values and become quadratic as output grows. Use `StringBuilder` for a growing result. `String.join` is convenient for paths, labels, and lists of already prepared strings; it is not a CSV escaper.

Common format placeholders: `%s` text, `%d` integer, `%f` floating-point, `%.2f` two decimal places, and `%n` platform line separator. Formatting uses a locale unless one is supplied explicitly; use an explicit `Locale` when the output must be stable across servers.

## Conversions and encodings

```java
String intText = String.valueOf(42);
String alsoIntText = Integer.toString(42);
int number = Integer.parseInt("42");
long big = Long.parseLong("12345678900");
double decimal = Double.parseDouble("3.14");

char[] chars = {'a', 'b', 'c'};
String fromChars = String.valueOf(chars); // "abc"
char[] copiedChars = fromChars.toCharArray();
String fromLong = String.valueOf(42L);
String fromDouble = String.valueOf(2.5);
String fromBoolean = String.valueOf(true);
String fromChar = String.valueOf('x');
Object object = Integer.valueOf(7);
String fromObject = String.valueOf(object); // "7"; null Object becomes "null"

byte[] utf8 = "Java".getBytes(java.nio.charset.StandardCharsets.UTF_8);
String decoded = new String(utf8, java.nio.charset.StandardCharsets.UTF_8);
```

`String.valueOf(char[])` creates text from the characters; `chars.toString()` does **not** show the character contents. Numeric parsing may throw `NumberFormatException` for invalid or out-of-range input. For network/files/backend data, explicitly choose a charset such as UTF-8 for both encoding and decoding; `getBytes()` without a charset uses the JVM's default charset, which may differ between configurations.

## Regex methods in one place

`matches(regex)` tests the **whole** string. `split(regex)` separates at matches. `replaceAll(regex, replacement)` replaces all matches, and `replaceFirst` replaces only the first. Regex syntax and performance deserve a separate topic; for plain literal replacement, prefer `replace`.

# 5. Null, empty, and blank

`null` means a variable refers to no `String` object. `""` is an existing string of length zero. `" "` is an existing string containing a space.

```java
String missing = null;
String empty = "";
String space = " ";
boolean absent = missing == null; // true
boolean noChars = empty.isEmpty(); // true
boolean onlySpace = space.isBlank(); // true
// missing.length() would throw NullPointerException at runtime.
```

| Value | `isEmpty()` | `isBlank()` |
| --- | ---: | ---: |
| `""` | `true` | `true` |
| `" "` | `false` | `true` |
| `"   "` | `false` | `true` |
| `"\t"` | `false` | `true` |
| `"\n"` | `false` | `true` |
| `"Java"` | `false` | `false` |
| `null` | Throws `NullPointerException` | Throws `NullPointerException` |

Call `isEmpty()` when zero characters matters; call `isBlank()` when whitespace-only input should count as empty. If the reference might be null, check `s == null` before calling either. For null-safe constant comparison, `"yes".equals(s)` is useful.

# 6. `StringBuilder`: mutable text

`StringBuilder` holds a mutable character sequence. It is the usual tool for appending many pieces in a loop because it can reuse internal capacity. `append` of one character is amortized O(1); appending `k` characters is O(k) amortized, with occasional resizing. `toString()` produces an immutable `String` result.

```java
StringBuilder sb = new StringBuilder();
sb.append("Java");
sb.append(' ');
sb.append("Backend");
String result = sb.toString(); // "Java Backend"
```

In DSA, use a builder to construct a parsed token, filtered string, path, or answer one piece at a time. `StringBuilder` is mutable, so passing the same builder to another method allows that method to edit it.

## `StringBuilder` methods, ranked

| Priority | Method | Return Type | Purpose | Example | DSA Use | Backend Use |
| --- | --- | --- | --- | --- | --- | --- |
| ⭐⭐⭐⭐⭐ | `append(x)` | `StringBuilder` | Add text at end | `sb.append('x')` | Build answer | Build message |
| ⭐⭐⭐ | `insert(i, x)` | `StringBuilder` | Insert at index; shifts tail | `sb.insert(0, "#")` | Edit result | Text assembly |
| ⭐⭐⭐ | `delete(from, to)` | `StringBuilder` | Delete `[from,to)` | `sb.delete(1, 3)` | Remove range | Rewrite text |
| ⭐⭐⭐ | `deleteCharAt(i)` | `StringBuilder` | Delete one code unit | `sb.deleteCharAt(0)` | Edit token | Occasional |
| ⭐⭐ | `replace(from, to, s)` | `StringBuilder` | Replace range | `sb.replace(0, 2, "Hi")` | Occasional | Edit template |
| ⭐⭐⭐⭐⭐ | `reverse()` | `StringBuilder` | Reverse sequence | `sb.reverse()` | Reverse answer | Occasional |
| ⭐⭐⭐⭐ | `setCharAt(i, c)` | `void` | Overwrite a code unit | `sb.setCharAt(0, 'J')` | In-place edit | Edit buffer |
| ⭐⭐⭐ | `charAt(i)` | `char` | Read a code unit | `sb.charAt(0)` | Inspect result | Inspect buffer |
| ⭐⭐⭐⭐⭐ | `length()` | `int` | Current length | `sb.length()` | Loop bounds | Buffer size |
| ⭐⭐ | `setLength(n)` | `void` | Truncate or extend with NUL chars | `sb.setLength(0)` | Reuse builder | Reset buffer |
| ⭐⭐ | `substring(from[, to])` | `String` | Copy part as immutable string | `sb.substring(1, 3)` | Extract result | Extract token |
| ⭐⭐⭐⭐⭐ | `toString()` | `String` | Produce immutable result | `sb.toString()` | Return answer | Return text |

Indexes use the same UTF-16 code-unit model as `String`. `insert`/`delete` may shift O(n) characters; `reverse` is O(n), and `toString` copies O(n) content. `setLength(0)` clears the logical content but does not promise to erase sensitive data from memory. `StringBuilder.substring` returns a **`String`**, not a builder. `setLength` extended beyond the current length fills new positions with `\u0000`.

`StringBuilder` does not use `String`'s content-based `equals`. Compare resulting text with `sb.toString().equals(otherText)` when content equality is needed.

## `StringBuilder` versus `StringBuffer`

| Aspect | `StringBuilder` | `StringBuffer` |
| --- | --- | --- |
| Mutable? | Yes | Yes |
| Thread safety | Not synchronized | Methods are synchronized |
| Synchronization | No built-in locking | Built-in per-method locking |
| Performance | Usually preferable for single-threaded building | May have synchronization overhead |
| Typical use | Local text construction | Shared mutable text when synchronization is specifically needed |
| DSA usefulness | High | Low |
| Backend usefulness | High for local request processing | Occasional; coordination may still be required |

`StringBuffer`'s synchronized individual methods do not automatically make a multi-step operation atomic. Prefer `StringBuilder` for local variables, which is the usual DSA and backend case.

# 7. `String` versus `char[]` and the three text classes

```java
String s = "cab";
char[] chars = s.toCharArray();
java.util.Arrays.sort(chars);
chars[0] = 'x';
String changed = String.valueOf(chars); // "xbc"
System.out.println(s);                  // "cab": original unchanged
```

| Aspect | `String` | `char[]` |
| --- | --- | --- |
| Mutability | Immutable | Mutable elements |
| Memory | Implementation-dependent object storage | New array of UTF-16 code units |
| Modification | Create another `String` | Assign `chars[i]` |
| Access | `s.charAt(i)` | `chars[i]` |
| Sorting | Convert to array first | `Arrays.sort(chars)` |
| DSA use | Input, substring, keys, comparison | Sort, swap, edit, frequency work |
| Backend use | Most text fields and APIs | Mutable buffers, some interop |
| Security | Cannot wipe its contents on demand | Can overwrite the array, though copies may remain elsewhere |

> **Backend note:** A `char[]` can be overwritten after handling a secret, while a `String` cannot be cleared in place. This does not guarantee every copy is erased; follow the secret-handling API's contract.

| Feature | `String` | `StringBuilder` | `StringBuffer` |
| --- | --- | --- | --- |
| Mutable? | No | Yes | Yes |
| Thread-safe sharing | Content cannot change | No built-in synchronization | Synchronized methods |
| Repeated modification | Creates new results | Efficient append/build | Efficient append/build with locking |
| DSA usage | Input, compare, keys | Build answers | Rare |
| Backend usage | Domain values, messages, keys | Local assembly | Uncommon shared assembly |
| Typical purpose | Stable text value | Mutable local workspace | Mutable synchronized workspace |

# 8. Complexity expectations for DSA

Let `n` be input length, `m` pattern/other-string length, and `k` new output length. These are useful planning estimates, not guarantees about every JDK optimization or regex.

| Operation | Typical complexity | Practical reason |
| --- | ---: | --- |
| `length()` | O(1) | Stored length |
| `charAt()` | O(1) | Indexed code unit |
| `equals()` | O(min(n,m)) after length check; O(n) when equal length | Compare until mismatch/end |
| `compareTo()` | O(min(n,m)) | Compare until mismatch/end |
| `substring()` | O(k) time and space | Create requested text in modern Java |
| `contains()` | Search-dependent; up to O(n·m) as a safe bound | Locate pattern |
| `indexOf()` | Search-dependent; up to O(n·m) as a safe bound | Locate pattern |
| One concatenation | O(k) | Produce output |
| Repeated `+=` in a growing loop | Often O(n²) total for n one-char appends | Recopy growing prefix |
| `StringBuilder.append(char)` | Amortized O(1) | Usually write into existing capacity |
| `StringBuilder.append(k chars)` | Amortized O(k) | Copy appended data; occasional resize |
| `StringBuilder.reverse()` | O(n) | Visit/swap content |
| `toCharArray()` | O(n) time and space | Copy code units |

`StringBuilder` may resize, and regex runtime can depend strongly on the pattern. For an interview answer, state the model you assume, especially when using `split`, `replaceAll`, or a nested loop over substrings.

# 9. Common String operations for DSA

The methods below are one compilable class. They illustrate the usual ASCII/English-lowercase assumptions where stated; they do not fully solve every Unicode text problem.

```java
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class StringRecipes {
    static String reverse(String s) {                // O(n)
        return new StringBuilder(s).reverse().toString();
    }

    static boolean isPalindrome(String s) {         // O(n), O(1) extra space
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    static int count(String s, char target) {       // O(n)
        int total = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == target) total++;
        }
        return total;
    }

    static int[] lowercaseFrequencies(String s) {  // O(n), lowercase a-z only
        int[] freq = new int[26];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 'a' || c > 'z') throw new IllegalArgumentException("a-z only");
            freq[c - 'a']++;
        }
        return freq;
    }

    static String sortCharacters(String s) {       // O(n log n)
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return String.valueOf(chars);
    }

    static boolean sameContent(String a, String b) {
        return a.equals(b);
    }

    static boolean areAnagrams(String a, String b) { // O(n log n)
        if (a.length() != b.length()) return false;
        return sortCharacters(a).equals(sortCharacters(b));
    }

    static String removeCharacter(String s, char unwanted) { // O(n)
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != unwanted) out.append(c);
        }
        return out.toString();
    }

    static String buildFromChars(char[] chars) {    // O(n)
        StringBuilder out = new StringBuilder();
        for (char c : chars) out.append(c);
        return out.toString();
    }

    static String extract(String s, int left, int right) {
        return s.substring(left, right); // [left, right)
    }

    static boolean hasDuplicateCodeUnit(String s) { // expected O(n)
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            if (!seen.add(s.charAt(i))) return true;
        }
        return false;
    }

    static int digitValue(char c) {                // O(1), ASCII digit only
        if (c < '0' || c > '9') throw new IllegalArgumentException("digit required");
        return c - '0';
    }

    static char digitChar(int digit) {             // O(1), 0 through 9
        if (digit < 0 || digit > 9) throw new IllegalArgumentException("0-9 required");
        return (char) ('0' + digit);
    }
}
```

In `freq[c - 'a']`, subtracting `'a'` maps `'a'` to index 0 and `'z'` to 25. Use this only when the input domain is known to be lowercase English letters. For a broader set of UTF-16 code units, `HashMap<Character, Integer>` is a flexible frequency map; for full Unicode code points, use code-point-aware processing. `char` and `Character` alone do not represent every Unicode character.

## Where String patterns appear

| Pattern | Relationship to strings |
| --- | --- |
| Two pointers | Compare characters from ends or scan two strings |
| Sliding window | Maintain a moving substring with counts/positions |
| Hashing | Track seen substrings or count tokens |
| Frequency arrays | Count letters in a small known alphabet |
| Prefix techniques | Reuse information about earlier characters |
| Palindrome checking | Compare mirrored positions |
| Anagrams | Compare sorted characters or frequency counts |
| Pattern matching | Find a target sequence in text |
| Parsing | Identify delimiters and extract fields |
| Stack-based problems | Match brackets or undo/remove adjacent characters |

These are connections to later algorithm topics; the key first step is to identify the character domain, whether indexing by `char` is valid, and whether you need a mutable output buffer.

# 10. String hashing and `HashMap` keys

`String.hashCode()` returns an `int` derived from the contents. Equal strings have equal hash codes, though different strings can collide. `HashMap<String, ...>` and `HashSet<String>` are common because strings have content-based `equals` and a stable hash code: immutability means a key's content does not change after insertion. You usually do not call `hashCode()` yourself for a DSA map lookup.

# 11. Common mistakes

| Mistake | Better approach |
| --- | --- |
| `a == b` to compare text | Use `a.equals(b)`; null-safe constant form: `"yes".equals(a)` |
| Ignoring `s.toUpperCase()` or `s.concat(...)` result | Assign the returned string if needed |
| `s.length` | Use `s.length()`; arrays use `array.length` |
| Calling methods on `null` | Check for null or define a non-null contract |
| Treating substring end as inclusive | Use `[begin, end)` |
| `split(".")` expecting a literal period | Use `split("\\.")` |
| Repeated `result += c` in a loop | Append to `StringBuilder` |
| Confusing `'a'` and `"a"` | First is `char`, second is `String` |
| Assuming `charAt()` returns `String` | It returns `char` |
| Assuming a `String` character can be assigned | Convert to `char[]` or use `StringBuilder.setCharAt` |
| Assuming every visible Unicode character is one `char` | Use code-point-aware APIs when needed |

> **Intentionally incorrect Java:** `s.charAt(0) = 'A';` cannot assign to a method result. `s.length` is also invalid because `String` has `length()` rather than a `length` field.

# 12. Short interview questions

| Question | Answer |
| --- | --- |
| Is `String` primitive? | No, it is an immutable `java.lang` class. |
| Why immutable? | Stable shared contents help pooling, hashing, concurrency, and reliable value semantics. |
| What is the String Pool? | A store of canonical interned strings, including literals. |
| `==` versus `equals`? | Reference identity versus content equality. |
| `compareTo` versus `equals`? | Lexicographic ordering result versus boolean equality. |
| `String` vs builder vs buffer? | Immutable value, mutable unsynchronized builder, mutable synchronized buffer. |
| Why good `HashMap` key? | Content-based equality/hash and unchanging contents after insertion. |
| Can a string be `null`? | A `String` variable may refer to no object. |
| `null` vs `""` vs `" "`? | No object, zero-length object, one-space object. |
| `isEmpty` vs `isBlank`? | Zero length versus all whitespace. |
| `trim` vs `strip`? | Narrow older edge-char rule versus Unicode-aware whitespace rule. |
| What does `intern` do? | Returns the canonical pooled reference for equal contents. |
| Why can loop concatenation be slow? | Repeatedly recreating/copying a growing result can be quadratic. |
| `charAt` vs `toCharArray`? | Read one indexed code unit versus allocate a mutable copy of all code units. |

# 13. LeetCode syntax cheat sheet

This compact class keeps every example syntactically valid. In an actual problem, use only the statements you need.

```java
import java.util.Arrays;

class StringCheatSheet {
    static String demo(String other, int number) {
        String s = "hello";
        int n = s.length();
        char first = s.charAt(0);
        String piece = s.substring(1, 3);
        boolean equal = s.equals(other);
        int pos = s.indexOf("ll");
        boolean has = s.contains("ll");
        boolean prefix = s.startsWith("h");
        boolean suffix = s.endsWith("o");
        String lower = s.toLowerCase();
        String upper = s.toUpperCase();

        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        String sorted = String.valueOf(chars);
        int parsed = Integer.parseInt("42");
        String numberText = String.valueOf(number);

        StringBuilder sb = new StringBuilder();
        sb.append(first);
        sb.reverse();
        if (sb.length() > 0) sb.setCharAt(0, 'H');
        return sb.toString();
    }
}
```

# What Should I Memorize?

### ⭐⭐⭐⭐⭐ Must Know

- `String` is immutable; `==` checks identity and `equals` checks content.
- `length()`, `charAt()`, `substring(begin, end)`, zero-based indexes, and indexed loops.
- `indexOf()` returning `-1`, `toCharArray()`, `StringBuilder.append()`/`toString()`, and the cost of repeated loop concatenation.

### ⭐⭐⭐⭐ Very Important

- `isEmpty()` versus `isBlank()`, `startsWith()`, `endsWith()`, `contains()`, and `compareTo()`.
- `split()` and `replaceAll()` use regex; `replace()` is literal. `split(..., -1)` preserves trailing empty fields.
- `String.valueOf()`, numeric parsing, explicit UTF-8 encoding, and basic character frequency arrays.
- `StringBuilder.reverse()`, `setCharAt()`, and `length()`.

### ⭐⭐⭐ Useful

- Locale-aware case conversion, `strip()`, `String.join()`, `String.format()`, and `lastIndexOf()`.
- `StringBuilder.insert()`/`delete()`, sorting a `char[]`, and code-point awareness for non-English text.

### ⭐⭐ Low Priority for Now

- `compareToIgnoreCase()`, `trim()`, `stripLeading()`/`stripTrailing()`, `concat()`, `repeat()`, `formatted()`, and `StringBuffer`.

### ⭐ Rarely Needed for Now

- `intern()` and deliberate pool management; use only when a real use case calls for it.

Reference: [Java SE 21 `String`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html), [`StringBuilder`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html), and [`StringBuffer`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuffer.html) APIs.
