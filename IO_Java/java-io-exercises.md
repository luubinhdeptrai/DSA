# Java I/O Review Exercises

## Exercise 1 — Byte Stream

### Objective

Write a program that copies the contents of one file to another file, byte by byte, using `FileInputStream` and `FileOutputStream`.

You will practice:

* opening a byte input stream and a byte output stream
* reading one byte at a time in a loop
* detecting the end of the file
* writing bytes to the output stream
* closing resources safely with `try-with-resources`

**Why Byte Stream is appropriate here:** copying a file is a raw, format-agnostic operation — you don't care whether the bytes represent text, an image, or audio, you just move every byte from source to destination unchanged. `FileInputStream`/`FileOutputStream` work directly with raw bytes (`InputStream`/`OutputStream`), which is exactly what "copy any file exactly as-is" calls for. Character streams would be the wrong tool here because they interpret bytes as text using a character encoding, which can corrupt non-text data.

### Input

A file named `source.txt` (or any file, e.g. `source.jpg`) placed in your project folder. For testing, a simple text file works fine — the bytes don't need to be human-readable:

```text
source.txt

Hello Byte Stream!
```

### Expected Output

A new file named `copy.txt` with **identical contents** to `source.txt`:

```text
copy.txt

Hello Byte Stream!
```

### Requirements

Practice these classes and methods:

```java
FileInputStream
FileOutputStream
read()
write(int b)
close()   // via try-with-resources
```

### Starter Code

```java
public class Main {
    public static void main(String[] args) {
        // TODO: declare source and destination file paths

        // TODO: open FileInputStream and FileOutputStream inside try-with-resources

        // TODO: read bytes one at a time until end of file (-1)

        // TODO: write each byte to the output stream
    }
}
```

### Hints

* `FileInputStream` and `FileOutputStream` both take a file path (`String`) in their constructor.
* `read()` on a `FileInputStream` returns an `int`, not a `byte` — think about why an `int` is needed instead of a `byte`.
* A `while` loop is a natural fit for "keep reading until something happens."
* `try-with-resources` lets you skip writing explicit `close()` calls — just declare the streams inside the parentheses of `try (...)`.
* If you want to test with a binary file (like a `.jpg`), just change the file names — the code doesn't need to change at all.

---

### Solution — Do Not Read Until You Finish

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String sourcePath = "source.txt";
        String destPath = "copy.txt";

        try (FileInputStream input = new FileInputStream(sourcePath);
             FileOutputStream output = new FileOutputStream(destPath)) {

            int data;
            while ((data = input.read()) != -1) {
                output.write(data);
            }

            System.out.println("File copied successfully.");

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
```

### Explanation

```java
try (FileInputStream input = new FileInputStream(sourcePath);
     FileOutputStream output = new FileOutputStream(destPath)) {
```

* This is **try-with-resources**. Any resource declared inside the parentheses must implement `AutoCloseable`; Java automatically calls `close()` on it when the `try` block ends — even if an exception is thrown. This is why you don't see explicit `input.close()` / `output.close()` calls anywhere.

```java
int data;
while ((data = input.read()) != -1) {
    output.write(data);
}
```

* `input.read()` reads **one byte** from the file and returns it as an `int` in the range `0–255`.
* It is stored in an `int` (not a `byte`) because a `byte` in Java can only hold `-128` to `127`, which is not wide enough to distinguish all 256 possible byte values from the special "end of file" signal. Using `int` gives room for that extra signal value.
* `-1` is the special value `read()` returns when there is **nothing left to read** — i.e., the end of the file has been reached. `-1` can never be a real byte value (real values are `0–255`), so it's a safe sentinel.
* The loop condition `(data = input.read()) != -1` does two things at once: it assigns the result of `read()` to `data`, *then* checks whether that value is `-1`. As soon as `read()` returns `-1`, the loop stops.
* `output.write(data)` writes that single byte to the destination file.

---

## Exercise 2 — Character Stream

### Objective

Write a program that reads a text file **line by line**, adds a line number in front of each line, and writes the result to a new text file, using `BufferedReader` (wrapping a `FileReader`) and `BufferedWriter` (wrapping a `FileWriter`).

You will practice:

* opening a character input stream and a character output stream
* reading text line by line
* detecting the end of the file
* writing text and inserting line breaks manually
* closing resources safely with `try-with-resources`

**Why Character Stream is appropriate here:** this task processes human-readable text and needs to understand where each *line* ends, not just where each byte ends. `Reader`/`Writer` classes handle character encoding for you and provide line-oriented operations like `readLine()`, which byte streams don't have. Since the goal is to modify text content meaningfully (adding numbers to lines), working at the character/line level is the natural fit.

### Input

```text
input.txt

Java is interesting.
I am learning Java.
```

### Expected Output

```text
output.txt

1. Java is interesting.
2. I am learning Java.
```

### Requirements

Practice these classes and methods:

```java
FileReader
BufferedReader
FileWriter
BufferedWriter
readLine()
write(String s)
newLine()
close()   // via try-with-resources
```

### Starter Code

```java
public class Main {
    public static void main(String[] args) {
        // TODO: declare input and output file paths

        // TODO: open BufferedReader (wrapping FileReader) and
        //       BufferedWriter (wrapping FileWriter) inside try-with-resources

        // TODO: read lines one at a time until end of file (null)

        // TODO: keep a counter, prepend "N. " to each line, and write it out
    }
}
```

### Hints

* `BufferedReader` wraps a `FileReader`: `new BufferedReader(new FileReader(path))`.
* `BufferedWriter` wraps a `FileWriter` the same way.
* `readLine()` returns a `String`, and it returns something special when there's no more input — think about what value makes sense for "no more text," as opposed to `-1` for bytes.
* `BufferedWriter.write(String)` does **not** add a newline automatically — you need to call `newLine()` yourself after each `write()`.
* Keep a simple counter variable (e.g., starting at 1) and increment it each time you process a line.

---

### Solution — Do Not Read Until You Finish

```java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String inputPath = "input.txt";
        String outputPath = "output.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                writer.write(lineNumber + ". " + line);
                writer.newLine();
                lineNumber++;
            }

            System.out.println("File processed successfully.");

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
```

### Explanation

```java
try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
     BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
```

* `FileReader`/`FileWriter` are the basic character streams; wrapping them in `BufferedReader`/`BufferedWriter` adds buffering (more efficient) and — importantly — gives you `readLine()`, which plain `FileReader` does not have.
* Both resources are declared in `try-with-resources`, so they are automatically closed when the block finishes, whether or not an exception occurs.

```java
String line;
int lineNumber = 1;

while ((line = reader.readLine()) != null) {
    writer.write(lineNumber + ". " + line);
    writer.newLine();
    lineNumber++;
}
```

* `reader.readLine()` reads one full line of text (without the line-terminator characters) and returns it as a `String`.
* When there are no more lines to read, `readLine()` returns `null` instead of a string. `null` is the natural "nothing here" signal for a reference type like `String` — this is different from the byte-stream case, where `read()` returns a primitive `int` and uses `-1` (a value outside the valid byte range) as the sentinel instead.
* The loop condition `(line = reader.readLine()) != null` assigns the line to `line` and checks it in one step; the loop keeps running as long as a real line was read, and stops the moment `readLine()` returns `null`.
* `writer.write(...)` sends the modified string to the output file, but it does **not** add a line break by itself.
* `writer.newLine()` explicitly writes the platform-appropriate line separator, so each entry ends up on its own line in `output.txt`.

---

## Final Comparison

```text
Byte Stream
→ InputStream / OutputStream
→ binary data
→ FileInputStream / FileOutputStream
→ read() returns int, -1 means end of file
→ use when copying/moving raw data of any kind (images, audio, any file) where content meaning doesn't matter

Character Stream
→ Reader / Writer
→ text data
→ FileReader / FileWriter, BufferedReader / BufferedWriter
→ readLine() returns String, null means end of file
→ use when reading/writing/processing human-readable text, especially line by line
```

**Rule of thumb:** if you need to preserve or process the exact bytes of a file regardless of what they mean (e.g., copying any file type), use a **Byte Stream**. If you need to read or write text meaningfully — as characters, words, or lines — use a **Character Stream**.
