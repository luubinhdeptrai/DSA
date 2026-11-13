# Java ImageIO and ProcessBuilder — Small Exercises

**Java version:** 21  
**Platform:** Windows 11 / PowerShell  
**Dependencies:** **Java standard library only** — no Maven, Spring Boot, images to download, native converters, accounts, or external infrastructure required.

> **How to use this file:** Follow **Exercise Instructions** first; implement the two small classes yourself. **Reference Solutions** are below a clearly marked divider. The examples deliberately work without any third-party tools.

## Learning objectives

After these exercises you can:

- Construct an in-memory `BufferedImage`, draw shapes, save PNG and read it back.
- Inspect width and height and resize while preserving aspect ratio.
- Convert transparency into an opaque RGB image and save JPEG.
- Use `ProcessBuilder` to launch the installed Java runtime, capture output, wait with a timeout, and inspect the exit code.
- Explain why image file bytes, decoded pixels, and subprocess output are different things.

---

# Exercise Instructions — Try these before viewing the solutions

## 1. Prerequisites and setup

Open **PowerShell** and check your Java 21 installation:

```powershell
java -version
javac -version
```

Both commands should report a Java 21 build. If not, configure a Java 21 JDK and ensure `java` and `javac` are available. No Maven project is needed; a plain Java program is the most direct way to learn these APIs. Maven is optional and would add no value to these two tiny tasks.

Create a new folder **anywhere you want**, for example under your home directory:

```powershell
Set-Location $HOME
New-Item -ItemType Directory -Force -Path 'java-image-process-exercises\src' | Out-Null
Set-Location 'java-image-process-exercises'
New-Item -ItemType Directory -Force -Path 'output', 'out' | Out-Null
code .
```

If `code .` is not installed as a shell command, open the folder using VS Code's **File → Open Folder** menu. Create these two Java files in the `src` folder:

```text
java-image-process-exercises/
├── src/
│   ├── ImageIoExercise.java
│   └── ProcessBuilderExercise.java
├── output/                 # generated PNG and JPEG go here
└── out/                    # compiled .class files
```

No package declarations and no `pom.xml` are needed. All paths in these examples are relative to the project folder from which you run the `java` command.

## 2. Exercise A — ImageIO (about 35–55 lines)

### Your goal

Write `src/ImageIoExercise.java` with a `main` method that performs the following **in order**:

1. Create a `240 × 120` `BufferedImage` of type `TYPE_INT_ARGB`.
2. Draw a colored rectangle and a colored circle with `Graphics2D`; leave at least some area transparent (new ARGB pixels begin transparent).
3. Ensure `output` exists; save the image as **`output/created.png`** using `ImageIO.write` and check the returned boolean.
4. Read `created.png` back with `ImageIO.read`; check for `null`, then print width and height.
5. Resize proportionally to **120 pixels wide**; calculate new height using `120.0 / originalWidth` (the answer should be **60 pixels**).
6. Use `Graphics2D.drawImage(...)` and bilinear interpolation to fill the resized target.
7. Make an **opaque RGB** image with `TYPE_INT_RGB`; fill it **white**, then draw the smaller ARGB image on top so transparency becomes white.
8. Save the opaque result to **`output/resized.jpg`**, check the writer result, and print the dimensions.

**What to code yourself:** the `BufferedImage` constructors, drawing calls, `ImageIO.write/read`, aspect-ratio calculation, RGB conversion, and messages. Use try/finally for each `Graphics2D` context so `dispose()` always runs.

### Hints (only look if stuck)

```java
BufferedImage original = new BufferedImage(240, 120, BufferedImage.TYPE_INT_ARGB);
// Graphics2D g = original.createGraphics(); ... g.dispose()
// ImageIO.write(..., "png", outputPath.toFile())
// BufferedImage loaded = ImageIO.read(pngPath.toFile());
// scale = 120.0 / loaded.getWidth();
// newHeight = (int) Math.round(loaded.getHeight() * scale);
```

PNG supports transparency; ordinary JPEG does not. Make the final image RGB, and deliberately choose the color behind transparent areas rather than trusting the writer to choose one.

### Expected files

| File | Expected result |
|---|---|
| `output/created.png` | A `240 × 120` PNG with colored drawing and transparent space |
| `output/resized.jpg` | A `120 × 60` JPEG, smaller drawing on an opaque white background |

Console output should have lines like:

```text
PNG saved: output\created.png
PNG decoded: 240 x 120
JPEG saved: output\resized.jpg
JPEG dimensions: 120 x 60
```

The path separator may display as `\` on Windows, and file sizes will vary depending on the Java writer.

### Compile, execute, and inspect

Run from the project root in PowerShell:

```powershell
javac -d out src\ImageIoExercise.java
java -cp out ImageIoExercise
Get-ChildItem .\output
Invoke-Item .\output\created.png
Invoke-Item .\output\resized.jpg
```

`Invoke-Item` opens each file using the Windows default application. Check that the PNG has transparent surroundings (some viewers display transparency as white or checkerboard) and the JPEG shows white where transparency existed.

## 3. Exercise B — ProcessBuilder (one tiny process)

### Your goal

Write `src/ProcessBuilderExercise.java` with a `main` method that:

1. Finds the current runtime's executable from `System.getProperty("java.home")` and `bin/java.exe` on Windows.
2. Builds a command with **separate arguments**: the executable path, then `"-version"`.
3. Merges stderr into stdout with `redirectErrorStream(true)`, because `java -version` commonly prints version information on stderr.
4. Captures the output safely. **For this exercise, redirect it to a temporary file** rather than reading potentially blocking pipes.
5. Starts the subprocess using `start()` and closes its stdin stream because there is no input to provide.
6. Waits up to **five seconds** with `waitFor(5, TimeUnit.SECONDS)`.
7. On timeout, forcibly terminates the child, waits for it to finish, and reports failure.
8. On success, reads and prints the captured output, checks the exit code, and prints `Exit code: 0` (normally).
9. Handles interruption by restoring the thread's interrupt flag. Cleans up the process if still alive and removes the temporary output file in `finally`.

**What to code yourself:** the `ProcessBuilder`, redirect configuration, timeout, exit-code check, cleanup, and `try`/`catch`/`finally` blocks.

### Hints

```java
Path executable = Path.of(System.getProperty("java.home"), "bin", "java.exe");
ProcessBuilder builder = new ProcessBuilder(executable.toString(), "-version");
builder.redirectErrorStream(true);
// builder.redirectOutput(tempFile.toFile());
// process = builder.start();
// process.waitFor(5, TimeUnit.SECONDS)
```

The reference solution also handles a non-Windows executable name so you can run it on another OS. The important idea is that `ProcessBuilder` launches **a separate Java process**, not a new thread in your existing JVM.

### Expected console output

The version/build numbers depend on your JDK installation:

```text
openjdk version "21..." ...
OpenJDK Runtime Environment ...
OpenJDK 64-Bit Server VM ...
Exit code: 0
```

### Compile and run

```powershell
javac -d out src\ProcessBuilderExercise.java
java -cp out ProcessBuilderExercise
```

The `java -version` child should finish quickly without requiring any third-party executable.

## 4. Your completion checklist

- [ ] Exercise A creates both files and shows the correct dimensions.
- [ ] I can explain why the JPEG is `TYPE_INT_RGB` and why its background is white.
- [ ] I know that `ImageIO.read` returns a **decoded** `BufferedImage`, not a PNG file.
- [ ] Exercise B prints the child Java version and `Exit code: 0`.
- [ ] I can explain why `redirectErrorStream(true)` is useful for `java -version`.
- [ ] I know that `waitFor(timeout)` does not automatically kill a child.

---

# Reference Solutions — Read after attempting the exercises

## 5. Exercise A solution: `src/ImageIoExercise.java`

```java
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public class ImageIoExercise {
    public static void main(String[] args) throws IOException {
        Path folder = Path.of("output");
        Files.createDirectories(folder);
        Path png = folder.resolve("created.png");
        Path jpeg = folder.resolve("resized.jpg");

        // 1. Draw a small ARGB image; untouched pixels stay transparent.
        BufferedImage source = new BufferedImage(
                240, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D artist = source.createGraphics();
        try {
            artist.setColor(new Color(25, 118, 210));
            artist.fillRoundRect(20, 15, 200, 90, 20, 20);
            artist.setColor(Color.YELLOW);
            artist.fillOval(95, 35, 50, 50);
        } finally {
            artist.dispose();
        }
        if (!ImageIO.write(source, "png", png.toFile())) {
            throw new IOException("No PNG writer available");
        }
        System.out.println("PNG saved: " + png);

        // 2. Decode the PNG and read dimensions.
        BufferedImage loaded = ImageIO.read(png.toFile());
        if (loaded == null) throw new IOException("Cannot decode PNG");
        System.out.printf("PNG decoded: %d x %d%n",
                loaded.getWidth(), loaded.getHeight());

        // 3. Resize proportionally from 240x120 to 120x60.
        int targetWidth = 120;
        double scale = (double) targetWidth / loaded.getWidth();
        int targetHeight = Math.max(1,
                (int) Math.round(loaded.getHeight() * scale));
        BufferedImage resized = new BufferedImage(
                targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D resizeGraphics = resized.createGraphics();
        try {
            resizeGraphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            resizeGraphics.drawImage(loaded, 0, 0,
                    targetWidth, targetHeight, null);
        } finally {
            resizeGraphics.dispose();
        }

        // 4. JPEG has no alpha: compose onto an opaque white RGB background.
        BufferedImage rgb = new BufferedImage(
                targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D rgbGraphics = rgb.createGraphics();
        try {
            rgbGraphics.setColor(Color.WHITE);
            rgbGraphics.fillRect(0, 0, targetWidth, targetHeight);
            rgbGraphics.drawImage(resized, 0, 0, null);
        } finally {
            rgbGraphics.dispose();
        }
        if (!ImageIO.write(rgb, "jpeg", jpeg.toFile())) {
            throw new IOException("No JPEG writer available");
        }
        System.out.println("JPEG saved: " + jpeg);
        System.out.printf("JPEG dimensions: %d x %d%n",
                rgb.getWidth(), rgb.getHeight());
    }
}
```

### Understand the important lines

| Line or expression | What it does | Why it matters |
|---|---|---|
| `new BufferedImage(240,120,TYPE_INT_ARGB)` | Allocates a pixel buffer | It is **not yet a PNG file** |
| `source.createGraphics()` | Gets a drawing context | Draws shapes into the in-memory pixels |
| `fillRoundRect` / `fillOval` | Changes pixel colors | Produces a real example with no downloaded image |
| `ImageIO.write(..., "png", ...)` | Encodes to PNG | Creates the file bytes; `false` means no writer |
| `ImageIO.read(png.toFile())` | Decodes file bytes to pixels | Creates an accessible `BufferedImage`; may return `null` |
| `targetWidth / loaded.getWidth()` | Computes a uniform scale | Preserves aspect ratio |
| `VALUE_INTERPOLATION_BILINEAR` | Requests smoother scaling | Interpolates values between neighboring pixels |
| `TYPE_INT_RGB` | Creates an opaque target buffer | Matches JPEG's lack of alpha |
| `fillRect(...white...)` | Sets background behind alpha pixels | Avoids accidental black/unwanted transparency conversion |
| `g.dispose()` | Releases graphics context resources | Good resource hygiene |

**In memory:** `source`, `loaded`, `resized`, and `rgb` are **separate pixel buffers**, even though some depict the same content. The PNG and JPEG are separately encoded files on disk, generally much smaller than the combined in-memory pixel buffers. Memory measurements depend on the actual `BufferedImage` representations and object overhead.

**Exceptions and ownership:** `ImageIO` can throw `IOException`; `ImageIO.read` can return `null`, and `ImageIO.write` can return `false`. `Files.createDirectories` can fail if the location is unavailable. Every created `Graphics2D` is disposed. The convenient `File` overloads of `ImageIO.read/write` manage the file resources they open internally.

## 6. Exercise B solution: `src/ProcessBuilderExercise.java`

```java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProcessBuilderExercise {
    public static void main(String[] args) throws Exception {
        boolean windows = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT).contains("win");
        Path executable = Path.of(System.getProperty("java.home"),
                "bin", windows ? "java.exe" : "java");
        Path capture = Files.createTempFile("java-version-", ".txt");
        Process process = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    executable.toString(), "-version");
            builder.redirectErrorStream(true);
            builder.redirectOutput(capture.toFile());
            process = builder.start();
            process.getOutputStream().close(); // no stdin for this command

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor();
                throw new IOException("java -version timed out");
            }

            String output = Files.readString(capture, StandardCharsets.UTF_8);
            System.out.print(output);
            int exitCode = process.exitValue();
            System.out.println("Exit code: " + exitCode);
            if (exitCode != 0) {
                throw new IOException("java -version failed: " + exitCode);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw interrupted;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            Files.deleteIfExists(capture);
        }
    }
}
```

### Understand the important lines

| Line or expression | Meaning |
|---|---|
| `java.home` | Path to the Java runtime running this class |
| `new ProcessBuilder(executable, "-version")` | A known executable and one argument token; no shell involved |
| `redirectErrorStream(true)` | Includes stderr with stdout (`java -version` commonly uses stderr) |
| `redirectOutput(capture.toFile())` | Directs the merged output to a temporary file; avoids full output pipes |
| `builder.start()` | Launches an independent OS process; may throw `IOException` |
| `process.getOutputStream().close()` | Tells the child no stdin input will be supplied |
| `waitFor(5, TimeUnit.SECONDS)` | Waits no more than five seconds for normal completion; returns a boolean |
| `destroyForcibly()` | Requests forceful termination after timeout or during cleanup |
| `process.waitFor()` after force | Ensures the direct child has exited before continuing |
| `exitValue()` | Gets the status after the child has finished |
| `Files.deleteIfExists(capture)` | Removes the temporary output file |

**Runtime behavior:** The parent JVM remains active while a **second JVM process** prints version information. Its stdout and stderr are captured into one temporary file, then read back into a Java string *after the child terminates*. No image or network I/O is involved.

**Error handling:** If the executable cannot launch, `start()` throws an `IOException`; if the child times out, the code requests forced termination; if the current thread is interrupted while waiting, it reasserts the interrupt flag and cleans up. Some failures during process termination or file deletion are platform-dependent, and robust services require more comprehensive cleanup. This is a deliberately small exercise.

---

# Verification, troubleshooting, and review

## 7. Verify the complete result

Run both classes from PowerShell in the project root:

```powershell
javac -d out src\ImageIoExercise.java src\ProcessBuilderExercise.java
java -cp out ImageIoExercise
java -cp out ProcessBuilderExercise
Get-ChildItem .\output | Select-Object Name,Length
```

Check that `output\created.png` and `output\resized.jpg` exist and can be opened by your image viewer. The JPEG may not reproduce every edge/color exactly because JPEG is lossy. Verify `240 × 120` and `120 × 60` in the console, and exit code `0` for the child process.

### Optional self-checks (no additional program needed)

- Change Exercise A's `targetWidth` to `80`; what height should result? **40**, because the source aspect ratio is 2:1.
- Change the rectangle's color; rerun and view the newly encoded files.
- Temporarily change `"-version"` to an invalid Java option; observe a nonzero exit status and diagnostic output. Restore `"-version"` afterward.

## 8. Common mistakes and fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| `'javac' is not recognized` | JDK missing or PATH not configured | Install/configure Java 21 JDK; reopen PowerShell |
| `file not found: src\...` | Running commands from the wrong folder | `Get-Location`; switch to the project root |
| `Could not find or load main class` | Wrong classpath or compilation failed | Compile to `out`, then run `java -cp out ClassName` |
| Output images not found | Wrong working directory or incorrect folder name | Create `output` and use relative paths from project root |
| `ImageIO.read(...)` is `null` | Unsupported/corrupt image; no registered reader | Check format and actual file contents |
| `ImageIO.write(...)` is `false` | Missing writer for requested format | Use `"png"` / `"jpeg"` in standard Java 21 |
| JPEG background is black | Alpha not flattened intentionally | Fill an RGB destination with white, draw the source onto it |
| Image looks stretched | New height was not derived from one scale factor | Use source aspect ratio |
| Process displays no output | `java -version` wrote to stderr | Merge stderr or capture it separately |
| `java -version` process never finishes | A child is blocked or not the expected command | Use a fixed executable, redirect output, timeout, and cleanup |
| `AccessDeniedException` or process cannot start | Path, file permissions, antivirus/OS restrictions | Verify executable path and permissions; inspect diagnostics |

## 9. Three review questions (with answers)

**Q1. Why does a saved PNG file have a different size from its `BufferedImage`?**  
**Answer:** A PNG stores an encoded, usually compressed representation; `BufferedImage` exposes decoded pixels. Their data structures, compression, and memory overhead differ.

**Q2. Why do we create a `TYPE_INT_RGB` image before saving a transparent picture as JPEG?**  
**Answer:** JPEG does not support an alpha channel, so we composite the transparent image onto a chosen opaque background, such as white, rather than leaving the conversion undefined.

**Q3. Why do we need a timeout and `destroyForcibly()` when running a child process?**  
**Answer:** A child process can hang or wait indefinitely; a timed wait merely reports that it has not finished. The parent must separately request termination and clean up associated resources.

## Final mental model

- **ImageIO:** Decodes/encodes image **files** into/from pixels.
- **BufferedImage:** An in-memory representation of pixels that Java can draw and resize.
- **Graphics2D:** The drawing/transform tool operating on the pixel buffer.
- **ProcessBuilder:** A tool for launching a separate OS process and supervising its output and termination.

The next step is the **theory** discussion about optional native-image decoders. You do not need to install any converter to complete these two exercises.
