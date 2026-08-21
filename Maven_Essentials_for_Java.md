# Maven Essentials for Java

This guide teaches the small set of Maven ideas that covers most everyday Java and Java backend work. Read it once from top to bottom, complete the exercise, and then use the final sections as a quick reference.

The examples use Java 17 because it is a common baseline. The Maven concepts are the same with another supported JDK. Version numbers in examples are fixed, known versions for reproducible learning examples; they are not claims about the newest available release.

**Study-priority legend**

| Rating | Meaning | How to study it |
|---|---|---|
| ⭐⭐⭐⭐⭐ MUST KNOW | Used constantly | Be able to explain and use it without the guide |
| ⭐⭐⭐⭐ IMPORTANT | Used regularly | Understand it well and know where to look it up |
| ⭐⭐⭐ NICE TO KNOW | Useful context | Recognize it; memorization is unnecessary |
| ⭐⭐ LEARN LATER | Advanced or situational | Ignore it for now unless a project requires it |

---

## 1. Maven in One Minute

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

**Definition:** Apache Maven is a build and project-management tool. It reads a project's `pom.xml`, resolves the required libraries, and coordinates the steps that turn source code into a tested, packaged program.

```text
Java source code
      ↓
pom.xml describes the project
      ↓
Maven
      ↓
Download dependencies
      ↓
Compile
      ↓
Run tests
      ↓
Package
      ↓
JAR / WAR
```

A **build tool** automates repeatable project work such as compiling, testing, copying resources, and packaging. **Dependency management** means identifying external libraries by name and version, downloading them, and placing them on the correct Java classpath.

Maven is **not**:

| Maven is not... | What that thing actually is |
|---|---|
| The Java language | The syntax and rules used to write `.java` files |
| The JVM | The runtime that executes Java bytecode |
| The JDK | The development kit containing tools such as `java` and `javac` |
| The Java compiler | `javac` compiles `.java` into `.class`; Maven normally invokes it through a plugin |
| An IDE | IntelliJ IDEA, Eclipse, and VS Code provide editing and development interfaces |

**Mental model:** Maven is the project coordinator. The JDK and Maven plugins do the concrete work; `pom.xml` tells Maven what project it is coordinating.

**Remember:** Maven makes a Java build repeatable with one project description and a small set of commands.

---

## 2. Why Maven Exists

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Imagine a project that uses five external libraries. Without a dependency manager, every developer might have to do this:

```text
Find the correct library website
        ↓
Download library.jar
        ↓
Find and download its required libraries
        ↓
Copy all JARs into a folder
        ↓
Configure javac/java classpaths manually
        ↓
Remember the exact compile, test, and package commands
```

That approach becomes fragile quickly. A missing JAR, a different library version, or a different directory can make one developer's build behave differently from another's.

Maven was created to solve recurring project problems:

- **Repeatable builds:** the same command follows the same declared build process.
- **Dependency management:** dependencies are declared as coordinates instead of copied manually.
- **Conventional layout:** most Maven projects put the same kinds of files in the same places.
- **A standard lifecycle:** Java developers recognize commands such as `mvn test` and `mvn package`.
- **Tool integration:** IDEs and CI servers can understand a project by reading its POM.

### Build tool

A build is the process of turning developer inputs into usable outputs:

```text
source + resources + dependencies
                 ↓
              build
                 ↓
compiled classes + test results + packaged artifact
```

Maven automates that process. It does not replace `javac`; its compiler plugin arranges the classpath and asks the JDK compiler to do the compilation.

### Dependency management

Instead of committing arbitrary JAR files, a POM can declare:

```text
I need org.apache.commons : commons-lang3 : 3.17.0
```

Maven resolves that artifact from a repository and makes it available where its scope permits.

**Common mistake:** Thinking Maven is useful only for downloading libraries. Dependency resolution is a major feature, but the build lifecycle, plugins, standard layout, and repeatability are equally important.

**Remember:** Maven replaces a collection of manual, project-specific steps with a declared model and standard commands.

---

## 3. Maven's Role in a Java Project

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Maven connects several pieces that you already know from Java:

```text
src/main/java/*.java
          │
          ├────────────┐
          ↓            ↓
       pom.xml     dependency repositories
          │            │
          └──────┬─────┘
                 ↓
               Maven
                 ↓
       plugins + JDK tools
                 ↓
       compile → test → package
                 ↓
              target/
```

The division of responsibility is:

| Part | Responsibility |
|---|---|
| Your Java code | Application behavior |
| `pom.xml` | Project identity, dependencies, build configuration |
| Maven | Resolves the model and coordinates lifecycle execution |
| Maven plugins | Perform tasks such as compile, test, and package |
| JDK | Supplies the compiler and Java runtime |
| Repository | Stores downloadable artifacts such as libraries and plugins |
| IDE | Helps edit, navigate, debug, and invoke builds |

In a normal workday you often change code and run a command such as:

```bash
mvn test
```

Before sharing or releasing a change, a common check is:

```bash
mvn clean verify
```

**Common mistake:** Treating the IDE's project settings as the only source of truth. In a Maven project, the committed POM and source layout should normally describe what the command-line build needs.

**Remember:** Maven sits between the project description and the build tools. It turns the POM's declarations into concrete build actions.

---

## 4. Standard Maven Project Structure

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Maven favors **convention over configuration**: use the standard directories and you usually do not need to tell Maven where source files, tests, and resources live.

```text
my-project/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │
│   └── test/
│       ├── java/
│       └── resources/
│
└── target/
```

| Path | Purpose | Typical contents |
|---|---|---|
| `pom.xml` | Maven's project model | Coordinates, dependencies, properties, plugins |
| `src/main/java` | Main Java source root | Application `.java` files |
| `src/main/resources` | Main resources | `.properties`, `.yaml`, templates, text files |
| `src/test/java` | Test Java source root | Unit/integration test `.java` files |
| `src/test/resources` | Test-only resources | Test configuration and sample data |
| `target` | Generated build output | `.class` files, reports, generated files, JAR/WAR |

### What is a source root?

A **source root** is the directory from which Java package paths begin. `src/main/java` is the default main source root, so it is **not** part of a Java package name.

```text
File:
src/main/java/com/example/app/Main.java
                   └──────┬──────┘
                       package path

First line inside the file:
package com.example.app;
```

Example:

```java
package com.example.app;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Maven!");
    }
}
```

The same rule applies to tests:

```text
src/test/java/com/example/app/MainTest.java
→ package com.example.app;
```

Files in `src/main/resources` are normally copied into `target/classes`, making them available from the application's classpath. Test resources are normally copied into `target/test-classes`.

**Mental model:**

```text
src/    → inputs written or maintained by the developer
target/ → outputs generated by the build
```

**Common mistakes:**

- Writing `package src.main.java.com.example.app;`—the source root is never part of the package.
- Putting production code in `src/test/java`.
- editing generated files under `target`; the next build can replace them.
- Committing `target/` to Git; it is normally ignored because Maven can recreate it.

**Remember:** Learn the six paths in the table. You will see them in almost every Maven-based Java project.

---

## 5. Understanding pom.xml

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

**POM** means **Project Object Model**. `pom.xml` is the XML document Maven reads to understand the project.

At the highest level it is one `<project>` element:

```xml
<project>
    <!-- project model goes here -->
</project>
```

A small realistic POM looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>bank-account</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.11.4</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

These fixed versions make the example reproducible. In a real project, use the versions selected by your project generator, team, parent POM, or current official documentation.

### The important elements

| Element | Priority | Meaning |
|---|---|---|
| `modelVersion` | ⭐⭐⭐⭐ IMPORTANT | Version of the POM model syntax; Maven 3 projects normally use `4.0.0` |
| `groupId` | ⭐⭐⭐⭐⭐ MUST KNOW | Organization or namespace |
| `artifactId` | ⭐⭐⭐⭐⭐ MUST KNOW | This project/library's name |
| `version` | ⭐⭐⭐⭐⭐ MUST KNOW | This project's version |
| `packaging` | ⭐⭐⭐⭐ IMPORTANT | Output/lifecycle type, commonly `jar` or `war`; default is `jar` |
| `properties` | ⭐⭐⭐⭐ IMPORTANT | Reusable values such as Java or dependency versions |
| `dependencies` | ⭐⭐⭐⭐⭐ MUST KNOW | Libraries required by project code or tests |
| `build` | ⭐⭐⭐⭐ IMPORTANT | Build-specific configuration |
| `plugins` | ⭐⭐⭐⭐ IMPORTANT | Tools and configuration that perform build work |

Property syntax uses `${...}`:

```xml
<junit.version>5.11.4</junit.version>
...
<version>${junit.version}</version>
```

`maven.compiler.release` tells the compiler plugin which Java release's language features, bytecode level, and public Java API to target. Maven's own Java runtime and this target release are related but distinct; section 21 explains the common mismatch.

### Dependencies are not plugins

This distinction matters:

```text
dependency → code your application/tests use
plugin     → tool Maven uses to build the project
```

JUnit is a dependency because test code imports JUnit classes. The Surefire plugin is a build plugin because it discovers and runs tests.

**Common mistakes:**

- Misspelling an XML element; Maven element names are case-sensitive.
- Putting a `<dependency>` directly under `<project>` instead of inside `<dependencies>`.
- Confusing a dependency's version with the project's own `<version>`.
- Changing versions randomly just to silence an error.
- Omitting plugin versions in a standalone long-lived project, which can make future builds less predictable.

**Remember:** For now, be comfortable reading coordinates, properties, dependencies, and the plugin list. You do not need to memorize the XML namespace.

---

## 6. Maven Coordinates

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Maven identifies a project or library with coordinates. The essential three parts are often called **GAV**:

```text
groupId : artifactId : version
```

Use this mental model:

```text
com.example
    ↓
organization / namespace

bank-account
    ↓
project or library name

1.0.0
    ↓
version of that artifact
```

Together:

```text
com.example:bank-account:1.0.0
```

Coordinates solve an important problem: `library.jar` is ambiguous, but this is specific:

```text
org.apache.commons:commons-lang3:3.17.0
```

Your own project has coordinates near the top of its POM. Every declared dependency has its own coordinates inside a `<dependency>`.

### Versions and `SNAPSHOT`

```text
1.0.0          → normally treated as a fixed release
1.1.0-SNAPSHOT → a changing development version
```

For now, recognize `SNAPSHOT`; advanced release and repository policies can wait.

**Common mistake:** Assuming `groupId` must exactly equal the Java package of every class. They often share an organization-style namespace, but Maven coordinates identify an artifact while Java packages organize classes inside artifacts.

**Remember:** When Maven reports that it cannot resolve an artifact, inspect `groupId`, `artifactId`, and `version` first.

---

## 7. Dependencies

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

A **dependency** is an external artifact that your project needs to compile, test, or run.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.17.0</version>
    </dependency>
</dependencies>
```

Because no scope is written, this dependency has the default `compile` scope.

### What Maven does with the declaration

```text
pom.xml
   ↓
dependency declaration
   ↓
Maven resolves its coordinates
   ↓
downloads the artifact JAR and metadata if needed
   ↓
stores them in the local repository
   ↓
adds the JAR to the appropriate classpath
   ↓
Java code can compile/use its classes
```

After declaring Commons Lang, code can import a class contained inside that artifact:

```java
package com.example.app;

import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        System.out.println(StringUtils.capitalize("maven"));
    }
}
```

Notice that the Maven coordinates and Java import are not the same:

```text
Maven artifact: org.apache.commons:commons-lang3:3.17.0
Java class:     org.apache.commons.lang3.StringUtils
```

The artifact is a JAR containing many Java packages and classes.

### Manual JARs versus Maven

Without Maven:

```text
Download library.jar
        ↓
move it into the project
        ↓
download that library's dependencies too
        ↓
manually configure javac -cp and java -cp
        ↓
tell every teammate to repeat the setup
```

With Maven:

```text
commit pom.xml
      ↓
run mvn test/package
      ↓
Maven resolves the declared dependency graph
and creates the classpaths
```

Maven is better because the library identity and version are reviewable, repeatable, and usable by the IDE and CI build. It does **not** usually copy dependency JARs into your project directory, and a normal JAR does not automatically contain all dependency JARs.

**Common mistakes:**

- Adding an `import` without declaring the artifact that contains the class.
- Assuming a dependency declaration creates an import; you still write the Java `import` when needed.
- Using an artifact from an untrusted or incorrect source because its name looks similar.
- Depending accidentally on a transitive library that your source code uses directly. If your code imports it, declare it directly.

**Remember:** A dependency declaration gives Maven an artifact identity and scope. Maven resolves it and supplies its classes on the appropriate classpath.

---

## 8. Transitive Dependencies

> Priority: ⭐⭐⭐⭐ IMPORTANT

A **transitive dependency** is a dependency of one of your dependencies.

```text
My Project
   ↓ declares
Library A
   ├── needs Library B
   └── needs Library C
```

If your project depends on A, and A's published POM says it depends on B, Maven can normally resolve B automatically:

```text
My Project → A → B
```

You usually do not need to copy or even know every transitive JAR before starting. This is one reason Maven scales much better than a folder of manually downloaded libraries.

There are two important practical rules:

1. Use `mvn dependency:tree` when you want to see both direct and transitive dependencies.
2. If your own source code directly imports a library, declare it directly instead of relying on another dependency to bring it accidentally.

Different dependency paths can request different versions of the same artifact. Maven has rules for choosing one, and exclusions or dependency management can change the result. That is a real topic, but advanced conflict mediation is ⭐⭐ LEARN LATER.

**Mental model:** Ordering one product may cause its required parts to arrive too. The dependency tree shows the full packing list.

**Common mistake:** Deleting a seemingly unused direct dependency without checking whether code or another build step truly needs it.

**Remember:** Your POM declares the dependencies you intentionally use; Maven expands them into a dependency graph.

---

## 9. Dependency Scopes

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

A **scope** says where a dependency belongs: main compilation, tests, or application runtime. Learn these four scopes first.

| Scope | Main compilation | Test compile/run | Application runtime classpath | Simple example |
|---|---:|---:|---:|---|
| `compile` | Yes | Yes | Yes | Commons Lang, a public API used by main code |
| `runtime` | No | Yes | Yes | PostgreSQL JDBC driver when main code compiles against `java.sql` APIs |
| `test` | No | Yes | No | JUnit |
| `provided` | Yes | Yes | No—expected from the runtime environment | Servlet API supplied by an external servlet container |

The table describes classpath availability. It does **not** mean Maven physically embeds the dependency inside a normal JAR; ordinary Maven JARs are usually thin.

### `compile`

`compile` is the default, so these are equivalent:

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.17.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.17.0</version>
    <scope>compile</scope>
</dependency>
```

Use it when main source code needs the library to compile and the application also needs it at runtime.

### `runtime`

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.5</version>
    <scope>runtime</scope>
</dependency>
```

Use it when the implementation is needed while running but main code does not compile against that implementation's classes. Database drivers are a common backend example.

### `test`

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
```

Test code can import JUnit, but production code under `src/main/java` cannot. The dependency is not part of the application's runtime classpath.

### `provided`

```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.1.0</version>
    <scope>provided</scope>
</dependency>
```

Main code can compile against it, but the deployment environment is expected to provide it. Using `provided` incorrectly often causes a runtime `ClassNotFoundException` or `NoClassDefFoundError`.

**Mental model:** A scope is an access pass for particular build situations.

**Common mistake:** Marking a dependency `test` or `provided` merely to make a package smaller, without considering whether the application needs that library at runtime.

**Remember:** `compile` is the default; JUnit is usually `test`; database drivers are often `runtime`; APIs supplied by an external container can be `provided`.

---

## 10. Maven Repositories

> Priority: ⭐⭐⭐⭐ IMPORTANT

A Maven **repository** stores artifacts and their metadata. Maven resolves dependencies and build plugins from repositories.

### Local repository

The local repository is on your computer. By default it is approximately:

```text
~/.m2/repository
```

Examples:

```text
Windows: C:\Users\your-name\.m2\repository
Linux:   /home/your-name/.m2/repository
macOS:   /Users/your-name/.m2/repository
```

`.m2` is Maven's per-user directory. In addition to the `repository` cache, it may contain `settings.xml` and other Maven-related data.

The local repository serves two common purposes:

- It caches artifacts downloaded from remote repositories, so they do not need to be downloaded on every build.
- `mvn install` places your own built artifact and POM there, making them resolvable by other local Maven builds.

### Remote repository and Maven Central

A **remote repository** is reachable over a network. **Maven Central** is the default public repository from which Maven resolves a huge number of open-source Java artifacts.

```text
pom.xml requests dependency
         ↓
check local repository
         ↓
       found?
  ┌──────┴──────┐
 YES             NO
  ↓              ↓
use it       request it from
             a remote repository
                    ↓
              download artifact
                    ↓
              save it locally
                    ↓
                  use it
```

The first build may therefore take longer and show many downloads. Later builds can reuse the local copies. A build may fail offline if a required artifact has never been cached.

**Common mistakes:**

- Treating `.m2/repository` as a folder to edit by hand.
- Deleting the entire local repository whenever a build fails. That forces every artifact to download again and often hides the real cause. If a cached artifact is genuinely corrupt, remove only the confirmed artifact directory.
- Assuming every JAR on the internet is in Maven Central.

Private company repositories, mirrors, credentials, Nexus, and Artifactory are ⭐⭐ LEARN LATER.

**Remember:** Maven checks locally first, downloads missing artifacts from a configured remote repository, and caches them under `.m2/repository`.

---

## 11. Maven Build Lifecycle

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

A Maven **lifecycle** is an ordered sequence of named build **phases**. Maven has three built-in lifecycles:

| Lifecycle | Priority | Purpose |
|---|---|---|
| `default` | ⭐⭐⭐⭐⭐ MUST KNOW | Builds, tests, packages, and distributes the project |
| `clean` | ⭐⭐⭐⭐ IMPORTANT | Removes output from an earlier build |
| `site` | ⭐⭐⭐ NICE TO KNOW | Generates project documentation/reports when configured |

### The key default-lifecycle phases

The full lifecycle contains more phases, but these are the 80/20 phases to know:

```text
validate
   ↓
compile
   ↓
test
   ↓
package
   ↓
verify
   ↓
install
   ↓
deploy
```

| Phase | What it means in a normal Java build |
|---|---|
| `validate` | Check that the project model is usable and required basic information exists |
| `compile` | Compile main source code and process main resources |
| `test` | Compile test code and run unit tests; a failing test normally fails/stops the build |
| `package` | Create the artifact for the packaging type, such as a JAR or WAR |
| `verify` | Run additional checks needed to confirm the package is valid; its extra value depends on configured plugins |
| `install` | Put the packaged artifact and POM into the local repository |
| `deploy` | Upload the artifact to a configured remote artifact repository for others/builds to use |

`deploy` here means **publish an artifact to a Maven repository**. It does not mean start the application or deploy it to a production server.

### The most important lifecycle rule

When you invoke a phase, Maven runs that lifecycle from its beginning **through that phase**.

```bash
mvn package
```

does not mean “only do the package action.” Conceptually it runs:

```text
validate
   ↓
compile
   ↓
test
   ↓
package
```

There are intermediate phases too, such as resource processing and test compilation. Maven executes them in their defined order even though the simplified diagram omits them.

The same rule applies later in the lifecycle:

```text
mvn install
    ↓
validate → compile → test → package → verify → install
```

### Combining lifecycles

```bash
mvn clean package
```

asks for two phases in the written order:

```text
clean lifecycle through clean
             ↓
delete previous target output
             ↓
default lifecycle through package
             ↓
compile → test → create fresh package
```

**Common mistakes:**

- Thinking `mvn package` skips compilation and tests.
- Thinking `mvn install` installs the application on the operating system. It installs an artifact into the local Maven repository.
- Thinking `mvn deploy` deploys a web application to a server.
- Running every phase name in one command, such as `mvn compile test package`; requesting `package` already includes the earlier phases and would repeat work.

**Remember:** A later phase includes all earlier phases in the same lifecycle. This is one of the most important Maven rules.

---

## 12. Essential Maven Commands

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Run build commands from the directory containing `pom.xml`. From elsewhere, point Maven to the POM explicitly—for example, `mvn -f path/to/pom.xml test`. On a wrapper-based project, replace `mvn` with the wrapper command from section 17.

| Command | What happens | When to use it | Usual output/change |
|---|---|---|---|
| `mvn --version` | Prints Maven, Maven home, Java runtime, and OS information | Verify installation and which JDK Maven is using | No project output |
| `mvn clean` | Runs the clean lifecycle through `clean` | Remove stale generated output | Deletes `target/` by default |
| `mvn compile` | Runs default lifecycle through main compilation | Quick check of production source | `target/classes`, copied main resources, metadata |
| `mvn test` | Compiles main/test code and runs unit tests | Normal feedback while coding | `target/classes`, `target/test-classes`, test reports |
| `mvn package` | Runs through tests and creates the configured artifact | Build a JAR/WAR | Artifact such as `target/my-app-1.0.0.jar` |
| `mvn verify` | Runs through packaging plus configured verification checks | Strong local/CI check before sharing | Package plus verification reports/results |
| `mvn install` | Runs through verification and installs the result locally | Another local Maven project needs this artifact | Package in `target/` and copy under `.m2/repository` |
| `mvn clean package` | Removes old output, then runs through package | Produce a fresh package | Recreated `target/` and JAR/WAR |
| `mvn clean install` | Removes old output, builds/tests/packages/verifies, then installs locally | Fresh build when a local downstream project needs the artifact | Recreated `target/` plus local-repository copy |

### `mvn --version`

```bash
mvn --version
```

Look for three independent facts:

```text
Apache Maven version
Maven home
Java version/runtime used to execute Maven
```

This is often the first command to run when diagnosing “works in my IDE but not in the terminal.”

### Which build command should be your habit?

- While changing a method: `mvn test`
- Before considering work ready: `mvn clean verify`
- When you specifically need the artifact: `mvn package` or `mvn clean package`
- When another local Maven project must resolve this artifact: `mvn install`

Do not use `clean` mechanically before every small build; incremental builds are useful. Use it when you need to prove a fresh build or suspect stale generated output.

**Common mistake:** Looking only at the last line. If Maven says `BUILD FAILURE`, scroll upward to the **first useful error** or failing test; later errors are often consequences.

**Remember by memory:** `--version`, `clean`, `compile`, `test`, `package`, `verify`, and `install`.

---

## 13. Understanding target/

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

`target/` is Maven's default build-output directory.

After compilation, tests, and packaging it may look like this:

```text
target/
├── classes/                 # compiled main .class files + main resources
├── test-classes/            # compiled test .class files + test resources
├── generated-sources/       # generated Java sources, if any
├── generated-test-sources/  # generated test sources, if any
├── maven-status/            # plugin/build bookkeeping
├── surefire-reports/        # unit-test reports
└── my-app-1.0.0.jar         # packaged artifact
```

Not every build creates every entry. For example, `mvn compile` does not normally create a packaged JAR.

```text
src/
→ durable source and resource inputs maintained by the developer

target/
→ disposable generated output produced by Maven/plugins
```

`mvn clean` deletes `target/` by default because all of it should be reproducible from source code, resources, the POM, and resolved tools/dependencies.

**Common mistakes:**

- Editing a `.class` or copied resource in `target/`; the next build replaces it.
- Adding `target/` to Git.
- Searching for the JAR beside `pom.xml` instead of under `target/`.
- Assuming an absent `target/` means the project is broken; it may simply not have been built yet.

**Remember:** If you want to inspect what Maven produced, inspect `target/`. If you want to change the project, edit `src/` or `pom.xml`.

---

## 14. JAR and WAR Packaging

> Priority: JAR—⭐⭐⭐⭐⭐ MUST KNOW; WAR—⭐⭐⭐ NICE TO KNOW

Packaging is the step that turns compiled output and resources into a distributable artifact.

```text
.java source
     ↓ javac (through Maven compiler plugin)
.class bytecode
     ↓ packaging-specific plugin (for example, Maven JAR Plugin)
.jar or .war archive
```

### JAR

A **JAR** (Java Archive) is a ZIP-format archive commonly containing:

- compiled `.class` files;
- application resources;
- metadata under `META-INF/`.

For this project identity:

```xml
<artifactId>my-app</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

this command:

```bash
mvn package
```

normally produces:

```text
target/my-app-1.0.0.jar
```

You can inspect its contents with the JDK's `jar` tool:

```bash
jar tf target/my-app-1.0.0.jar
```

Two important details:

1. A normal Maven JAR is usually a **thin JAR**: its dependency JARs are not nested inside it.
2. `java -jar app.jar` works only when the archive has a suitable main-class manifest (and its runtime dependencies are available). Packaging as `jar` alone does not guarantee an executable JAR.

Framework plugins, including Spring Boot's plugin, can create executable archives with a different layout. That comes later.

### WAR

A **WAR** (Web Application Archive) is a packaging format traditionally used for a web application deployed to a servlet container/application server.

```xml
<packaging>war</packaging>
```

It can produce:

```text
target/my-web-app-1.0.0.war
```

Modern Spring Boot backend projects commonly use executable JARs, although WAR deployment still exists.

| Packaging | Typical purpose | Typical output |
|---|---|---|
| `jar` | Library, console app, service, Spring Boot app | `target/name-version.jar` |
| `war` | Web app for a servlet container | `target/name-version.war` |

**Common mistake:** Assuming all dependency classes are inside a normal JAR simply because the project compiled successfully.

**Remember:** `package` creates the artifact selected by `<packaging>`; for most projects you meet soon, that artifact is a JAR.

---

## 15. Maven Plugins

> Priority: ⭐⭐⭐⭐ IMPORTANT

Maven defines the project model and lifecycle, but **plugins perform the actual build tasks**.

```text
Maven coordinates the build
          ↓
plugins perform concrete tasks
          ↓
compile / test / package / clean / install
```

A plugin is a collection of one or more **goals**. Each goal is a concrete operation.

| Plugin | Example goal | Typical job |
|---|---|---|
| Maven Resources Plugin | `resources:resources` | Copy/filter main resources |
| Maven Compiler Plugin | `compiler:compile` | Compile main Java source |
| Maven Compiler Plugin | `compiler:testCompile` | Compile test Java source |
| Maven Surefire Plugin | `surefire:test` | Discover and run unit tests |
| Maven JAR Plugin | `jar:jar` | Create a JAR |
| Maven Clean Plugin | `clean:clean` | Remove the build-output directory |

For a normal `jar` project, Maven's lifecycle bindings connect appropriate goals to phases. For example:

```text
compile phase
     ↓ invokes its bound goal
Maven Compiler Plugin
     ↓
compiler:compile
     ↓
target/classes/*.class
```

A POM can configure or add build plugins:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
        </plugin>
    </plugins>
</build>
```

The `maven.compiler.release` property shown earlier supplies the Java release setting used by the compiler plugin.

### Plugins versus dependencies

```text
Dependency
→ library used by your main or test code
→ appears on a code classpath according to scope

Build plugin
→ tool Maven executes to build the project
→ supplies goals and build configuration
```

JUnit is a dependency. Surefire is the plugin that runs JUnit tests. Commons Lang is a dependency. The compiler plugin compiles the code that imports it.

**Common mistake:** Putting a plugin under `<dependencies>` or putting an application library under `<build><plugins>`. Both are artifacts resolved from repositories, but they play different roles.

Writing custom plugins and advanced plugin execution configuration are ⭐⭐ LEARN LATER.

**Remember:** Maven is the coordinator; plugins are the workers.

---

## 16. Lifecycle, Phase, Plugin, and Goal

> Priority: ⭐⭐⭐⭐ IMPORTANT

These four terms fit together, but they are not synonyms:

| Term | Simple meaning | Example |
|---|---|---|
| Lifecycle | An ordered route through the build | `default` |
| Phase | A named stage/checkpoint on that route | `compile`, `test`, `package` |
| Plugin | A collection of tools Maven can run | Maven Compiler Plugin |
| Goal | One concrete task supplied by a plugin | `compiler:compile` |

The accurate relationship is:

```text
Lifecycle contains ordered phases
              ↓
a phase has zero, one, or several plugin goals bound to it
              ↓
Maven executes those goals when it reaches the phase
```

For a normal JAR build:

```text
default lifecycle
   │
   ├── compile phase ──→ compiler:compile goal
   ├── test phase ─────→ surefire:test goal
   └── package phase ──→ jar:jar goal
```

Bindings can differ by packaging type and POM configuration.

### Invoking a phase

```bash
mvn package
```

`package` is a **phase**. Maven follows the default lifecycle through that phase, executing all plugin goals bound along the route.

### Invoking a goal directly

The general form is:

```bash
mvn plugin:goal
```

Here, `plugin` is normally the plugin's command-line prefix, such as `dependency` or `compiler`.

Examples:

```bash
mvn dependency:tree
mvn compiler:compile
```

A direct goal normally executes that goal, **not** every earlier default-lifecycle phase. Some goals can deliberately start another lifecycle internally, but that is an advanced exception.

### Simple analogy

```text
Lifecycle = the full train route
Phase     = a station
Plugin    = a work crew
Goal      = a specific job performed by that crew
```

Asking Maven to reach the `package` station passes earlier stations. Asking one crew to perform one direct goal is a targeted request.

**Common mistake:** Calling `dependency:tree` a phase. It is the `tree` goal from the Maven Dependency Plugin.

**Remember:** A phase describes **where the lifecycle should reach**; a goal describes **the concrete task to run**.

---

## 17. Maven Wrapper

> Priority: ⭐⭐⭐⭐ IMPORTANT

The **Maven Wrapper** lets a project specify the Maven distribution its build should use. A wrapper-based project usually contains:

```text
my-project/
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
└── pom.xml
```

Depending on the wrapper type/version, `.mvn/wrapper` may contain additional wrapper files. Do not assume a wrapper JAR must always be present.

Use the script appropriate for the operating system:

**Windows PowerShell:**

```powershell
.\mvnw.cmd --version
.\mvnw.cmd test
.\mvnw.cmd package
```

**Linux/macOS:**

```bash
./mvnw --version
./mvnw test
./mvnw package
```

The wrapper reads its properties, obtains the configured Maven distribution if it is not already cached, and then forwards the command to that Maven version.

```text
project wrapper command
          ↓
read configured Maven distribution/version
          ↓
use cached copy or download it on first use
          ↓
run the requested Maven build
```

### Why teams use it

- The project controls the Maven version used by developers and CI.
- Developers do not have to coordinate identical global Maven installations.
- A new machine can run the build with only a suitable JDK plus the committed wrapper files; the first run normally needs network access.

The wrapper does **not** supply or control the JDK. Check the Java shown by the wrapper's `--version` output.

When a trusted project provides the wrapper, use it and commit its wrapper files. In an unfamiliar project, review the configured distribution URL before running downloaded tooling.

**Common mistake:** Typing `mvnw.cmd package` directly in PowerShell. PowerShell normally needs the explicit current-directory prefix: `.\mvnw.cmd package`.

**Remember:**

```text
mvn          → globally installed Maven
./mvnw       → project-controlled Maven on Linux/macOS
.\mvnw.cmd  → project-controlled Maven on Windows PowerShell
```

---

## 18. Maven and the Java Classpath

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

The Java **classpath** is the set of locations where the compiler or runtime looks for classes and resources.

Without Maven, a Windows command might look like:

```powershell
java -cp ".;library.jar" Main
```

On Linux/macOS, classpath entries use `:` instead of Windows's `;`.

As a project gains more libraries, manual classpaths become difficult:

```text
target/classes
+ dependency-a.jar
+ dependency-b.jar
+ dependency-of-b.jar
+ test framework JARs
+ test classes
```

Maven uses the POM, dependency graph, and scopes to construct task-specific classpaths:

```text
pom.xml
   ↓
Maven resolves dependencies
   ↓
selects artifacts allowed by each scope
   ↓
constructs the needed classpath
   ↓
javac / tests / Maven-run application
can find the required classes
```

### Different tasks need different classpaths

| Task | Simplified classpath contents |
|---|---|
| Compile main code | Main dependencies allowed for compilation |
| Compile/run tests | `target/classes`, `target/test-classes`, main dependencies, and test dependencies |
| Run the application through a configured Maven/framework plugin | `target/classes` plus runtime-eligible dependencies |

Maven does not permanently rewrite the operating system's `CLASSPATH`. Its plugins assemble a classpath for the build task they are running.

### Dependency declaration versus `import`

```text
POM dependency
→ makes an artifact available to the appropriate build classpath

Java import
→ lets source code refer to a class by a short name
```

You normally need both. An `import` cannot download a JAR, and a dependency declaration does not insert Java imports into source files.

### After packaging

A separately launched command such as:

```bash
java -jar target/my-app-1.0.0.jar
```

does not ask Maven to construct a classpath. The JAR must be executable, and its runtime libraries must be packaged or supplied appropriately. A successful Maven compile does not make a thin JAR self-contained.

**Mental model:** Maven replaces hand-written `-cp` strings during builds with classpaths calculated from declared dependencies and scopes.

**Common mistake:** Debugging a runtime missing-class error only by adding an `import`. Imports affect source names; runtime classpath availability is a separate issue.

**Remember:** Maven manages several classpaths, not one universal classpath.

---

## 19. Maven and IDEs

> Priority: ⭐⭐⭐⭐ IMPORTANT

Maven and an IDE have different jobs:

```text
IDE ≠ Maven
```

IntelliJ IDEA, Eclipse, and VS Code (with Java/Maven extensions) can recognize a `pom.xml` and import the Maven project.

```text
IDE opens/imports project
          ↓
reads pom.xml through Maven integration/resolver
          ↓
resolves dependencies and plugins as needed
          ↓
marks source/resource/test roots
          ↓
configures editor, compiler, test, and run classpaths
```

That is why opening a Maven project may immediately trigger dependency downloads. The IDE is synchronizing its project model with the POM.

### Practical rules

- After editing `pom.xml`, use the IDE's **Reload**, **Refresh**, **Reimport**, or **Update Maven Project** action if it does not synchronize automatically.
- Do not add a JAR only through an IDE-specific setting and expect the command-line Maven build to see it. Declare it in the POM.
- Use the project's wrapper command for a reproducible command-line check, especially before pushing code.
- Ensure the IDE and terminal use compatible JDKs.

An IDE may use its own incremental builder or may delegate actions to Maven. Therefore, an IDE build and `mvn verify` are not guaranteed to execute in exactly the same environment.

**Common mistake:** Seeing a red import immediately after adding a valid dependency and changing the coordinates repeatedly. First save the POM and refresh the Maven project.

**Remember:** The POM is the portable build description; the IDE imports that description to configure a convenient development environment.

---

## 20. dependency:tree

> Priority: ⭐⭐⭐⭐ IMPORTANT

Run this from the project root:

```bash
mvn dependency:tree
```

Or, when the project includes the wrapper:

```powershell
.\mvnw.cmd dependency:tree
```

The output shows the resolved dependency graph, including scopes and transitive children. A simplified example is:

```text
com.example:my-project:jar:1.0.0
+- org.apache.commons:commons-lang3:jar:3.17.0:compile
\- org.junit.jupiter:junit-jupiter:jar:5.11.4:test
   +- org.junit.jupiter:junit-jupiter-api:jar:5.11.4:test
   \- org.junit.jupiter:junit-jupiter-engine:jar:5.11.4:test
```

Read it as:

```text
my-project
├── declares Commons Lang directly
└── declares JUnit directly
    └── JUnit brings additional components transitively
```

The command helps answer:

> Why is this library in my project?

It is also useful for checking:

- whether a dependency is present;
- which scope it has;
- which direct dependency brought a transitive artifact;
- which version Maven selected.

`dependency:tree` shows project dependencies, not the complete tree of build-plugin dependencies. It is a plugin goal, so it does not perform the whole build lifecycle.

**Common mistake:** Looking only at the flat `<dependencies>` list and assuming it is the complete set of resolved JARs.

**Remember:** When an unexpected or missing library is involved, `mvn dependency:tree` is one of the first diagnostic commands to run.

---

## 21. Common Beginner Errors

> Priority: ⭐⭐⭐⭐ IMPORTANT

Maven often prints many lines after the real problem. Start with the first meaningful `[ERROR]`, compiler message, failing assertion, or `Caused by` message.

### Dependency not found

Typical message: Maven cannot resolve an artifact.

Possible causes:

- wrong `groupId`;
- wrong `artifactId`;
- a version that does not exist in configured repositories;
- missing required vendor/private repository;
- offline mode, proxy, DNS, TLS, or general network problem;
- a cached record of an earlier failed lookup.

First fix the coordinates, configured repository, offline/proxy setting, or network access. `mvn dependency:tree` can itself fail while a required artifact is unresolved; use it after resolution works when you need to investigate a transitive path or selected version.

If Maven previously cached a failed resolution and the coordinate is now valid, `mvn -U test` asks Maven to check remote updates again. Do not begin by deleting all of `.m2`.

### `mvn` command not recognized

Typical causes:

```text
Maven is not installed globally
or
Maven's bin directory is not on PATH
```

Actions:

1. If the project has a wrapper, use `.\mvnw.cmd --version` on Windows or `./mvnw --version` on Linux/macOS.
2. Otherwise install Maven and configure `PATH` according to its official installation instructions.
3. Confirm that a suitable JDK is installed and visible.

### Java version mismatch

Three versions are easy to confuse:

```text
Maven version
    ≠
JDK running Maven
    ≠
Java release targeted by the compiler configuration
```

Check the Maven process first:

```bash
mvn --version
```

Then inspect the POM:

```xml
<maven.compiler.release>17</maven.compiler.release>
```

Examples:

- A JDK too old to target the configured release can report `release version 17 not supported`.
- Running newer compiled bytecode on an older JVM can cause `UnsupportedClassVersionError`.
- The IDE may use JDK 21 while the terminal's `JAVA_HOME`/`PATH` selects JDK 11.

Use the JDK version required by the project, and align the IDE, terminal, and CI settings. In a plain Maven POM, prefer `maven.compiler.release`; a property named only `java.version` has no universal meaning unless a parent/framework interprets it.

### Dependency downloaded but import still fails

Possible causes:

- the class is not actually inside that artifact/version;
- the dependency has the wrong scope (for example, `test` while main code imports it);
- the Java import or package name is misspelled;
- the file is under the wrong source root;
- the IDE has not refreshed its Maven model;
- the editor is using stale indexes.

Check:

```text
1. POM coordinates and scope
2. mvn dependency:tree
3. Java file path and package declaration
4. IDE Maven refresh/reimport
5. mvn compile in the terminal
```

### Build works in the IDE but not on the command line

Compare these environments:

| Possible difference | What to compare |
|---|---|
| JDK | IDE project JDK versus Java printed by `mvn --version` |
| Maven | IDE-bundled/global Maven versus project wrapper |
| Maven settings | Mirrors, credentials, proxy, local-repository configuration |
| Project model | POM versus IDE-only libraries/settings |
| Environment | Variables, working directory, files not committed |
| Generated code | Annotation processing/generated-source configuration |

The wrapper command used in CI is usually the best reproducibility check.

### Maven cannot find a project

If a goal requires a project but Maven reports that no POM exists, check the current directory:

```text
my-project/       ← run Maven here
├── pom.xml
└── src/
```

### Tests are not running

Check that tests are under `src/test/java`, that the testing dependency has `test` scope, and that names follow the configured test plugin's discovery convention—for example, `SomethingTest.java` with Surefire's common defaults.

### Build succeeds but the JAR does not run

`mvn package` does not automatically create a self-contained executable JAR. The archive may lack a main-class manifest or runtime dependencies. Configure the appropriate packaging plugin/framework rather than treating compilation success as proof that `java -jar` will work.

### A short troubleshooting order

```text
Read the first useful error
        ↓
confirm you are beside pom.xml
        ↓
run mvn --version (or wrapper --version)
        ↓
inspect coordinates, scopes, and source paths
        ↓
run mvn dependency:tree when dependencies are involved
        ↓
refresh the IDE Maven model
        ↓
run mvn clean verify only if a fresh full check is useful
```

**Remember:** `clean` can remove stale generated output, but it cannot fix an incorrect coordinate, wrong scope, missing JDK, or broken network.

---

## 22. Practical Maven Exercise

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

**Estimated time:** 20–40 minutes

This exercise deliberately uses **exactly one direct dependency declaration**: JUnit Jupiter. You will use it from Java test code, see Maven place it only on the test classpath, and still produce an application JAR with no external runtime dependencies. Maven will resolve JUnit's additional components transitively.

### 22.1 Task description

Create a command-line project named `text-tool`. Its `TextFormatter` class will:

- trim surrounding whitespace;
- convert text to uppercase in a locale-independent way;
- reject `null` input;
- print a demonstration from `main`.

You will then add JUnit, write three tests, and build a runnable JAR.

```text
create project
      ↓
inspect standard structure
      ↓
edit pom.xml and add one dependency
      ↓
use JUnit in Java test code
      ↓
mvn compile → mvn test → mvn package
      ↓
inspect target/
```

### 22.2 Starter project structure

Create these directories and files with your IDE, terminal, or file explorer:

```text
text-tool/
│
├── pom.xml
│
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── example/
    │               └── text/
    │                   └── TextFormatter.java
    │
    └── test/
        └── java/
            └── com/
                └── example/
                    └── text/
                        └── TextFormatterTest.java
```

There is no `target/` yet; Maven will generate it.

Start `pom.xml` with this. The three plugin versions are pinned for a repeatable exercise. The JAR plugin is already configured with a main class so the final thin JAR can run; the application has no runtime library dependency.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>text-tool</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.11.4</junit.version>
    </properties>

    <dependencies>
        <!-- TODO: add JUnit Jupiter here -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.2</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.text.TextFormatter</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Start `TextFormatter.java` with:

```java
package com.example.text;

import java.util.Locale;

public final class TextFormatter {

    private TextFormatter() {
    }

    public static String shout(String input) {
        // TODO: reject null, then trim and convert to uppercase
        return "";
    }

    public static void main(String[] args) {
        String input = args.length == 0
                ? "  hello Maven  "
                : String.join(" ", args);

        System.out.println(shout(input));
    }
}
```

Start `TextFormatterTest.java` with:

```java
package com.example.text;

class TextFormatterTest {
    // TODO: add three JUnit tests
}
```

### 22.3 Steps

1. **Verify the tools.** Run `java --version` and `mvn --version`. Use JDK 17 or newer for this exercise.
2. **Inspect the paths.** Confirm that both files declare `package com.example.text;` because their paths after the source roots are `com/example/text`.
3. **Add one dependency.** Inside `<dependencies>`, declare `org.junit.jupiter:junit-jupiter:${junit.version}` with `test` scope.
4. **Implement `shout`.** Throw `IllegalArgumentException` when `input` is `null`; otherwise return `input.trim().toUpperCase(Locale.ROOT)`.
5. **Use the dependency in Java.** In `TextFormatterTest`, import JUnit's `@Test`, `assertEquals`, and `assertThrows`.
6. **Write three tests.** Test normal text, surrounding whitespace, and `null`.
7. **Compile, test, and package.** Use the commands in the next subsection.
8. **Inspect generated output.** Compare `target/classes`, `target/test-classes`, the Surefire reports, and the JAR.

The expected behavior is:

| Call | Expected result |
|---|---|
| `shout("Maven")` | `"MAVEN"` |
| `shout("  hello Maven  ")` | `"HELLO MAVEN"` |
| `shout(null)` | throws `IllegalArgumentException` |

### 22.4 Commands

Run every command from the `text-tool` directory containing `pom.xml`.

```bash
java --version
mvn --version

mvn compile
mvn test
mvn package
mvn dependency:tree
```

Inspect the JAR entries:

```bash
jar tf target/text-tool-1.0.0.jar
```

Because the JAR plugin added a `Main-Class` manifest and the main application uses only the JDK, run it with:

```bash
java -jar target/text-tool-1.0.0.jar
```

Finally, observe cleaning and rebuilding:

```bash
mvn clean
mvn clean package
```

If you add the Maven Wrapper later, the Windows equivalent of `mvn test` is `.\mvnw.cmd test`, and the Linux/macOS equivalent is `./mvnw test`.

### 22.5 Expected result

`mvn compile` should end with `BUILD SUCCESS` and create main bytecode:

```text
target/classes/com/example/text/TextFormatter.class
```

It should not create a JAR yet.

`mvn test` should report three passing tests, similar to:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

It should also create:

```text
target/test-classes/com/example/text/TextFormatterTest.class
target/surefire-reports/
```

`mvn package` should run the tests again as part of the lifecycle and produce:

```text
target/text-tool-1.0.0.jar
```

The final important output should resemble:

```text
target/
├── classes/
├── test-classes/
├── surefire-reports/
├── maven-status/
└── text-tool-1.0.0.jar
```

`jar tf` should show `com/example/text/TextFormatter.class`. It should not show `TextFormatterTest.class` or JUnit classes. Tests and test dependencies do not go into the main JAR.

`mvn dependency:tree` should show JUnit with `test` scope and several JUnit components beneath it transitively.

Running the JAR without arguments should print:

```text
HELLO MAVEN
```

### 22.6 Hints

1. A dependency belongs inside `<dependencies>`, not `<build><plugins>`.
2. The JUnit coordinates are:

   ```text
   org.junit.jupiter:junit-jupiter:5.11.4
   ```

3. JUnit must have `<scope>test</scope>`.
4. Test code is still Java code. Its external annotations and assertions need imports and a dependency just like main code would.
5. JUnit imports for this exercise are:

   ```java
   import org.junit.jupiter.api.Test;

   import static org.junit.jupiter.api.Assertions.assertEquals;
   import static org.junit.jupiter.api.Assertions.assertThrows;
   ```

6. For the exception test, pass the method call as a lambda:

   ```java
   assertThrows(IllegalArgumentException.class,
           () -> TextFormatter.shout(null));
   ```

7. If the editor shows red JUnit imports after you edit the POM, reload the Maven project.
8. The first build can be slower because Maven must download dependencies and plugins.
9. `mvn package` already runs earlier phases; you do not need `mvn compile test package`.

### 22.7 Solution

Try the exercise before comparing your work with this solution.

#### Complete `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>text-tool</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.11.4</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.2</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.text.TextFormatter</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Complete `TextFormatter.java`

```java
package com.example.text;

import java.util.Locale;

public final class TextFormatter {

    private TextFormatter() {
    }

    public static String shout(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }

        return input.trim().toUpperCase(Locale.ROOT);
    }

    public static void main(String[] args) {
        String input = args.length == 0
                ? "  hello Maven  "
                : String.join(" ", args);

        System.out.println(shout(input));
    }
}
```

#### Complete `TextFormatterTest.java`

```java
package com.example.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextFormatterTest {

    @Test
    void convertsTextToUppercase() {
        assertEquals("MAVEN", TextFormatter.shout("Maven"));
    }

    @Test
    void removesSurroundingWhitespace() {
        assertEquals(
                "HELLO MAVEN",
                TextFormatter.shout("  hello Maven  ")
        );
    }

    @Test
    void rejectsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TextFormatter.shout(null)
        );
    }
}
```

#### Final verification

```bash
mvn clean
mvn compile
mvn test
mvn package
mvn dependency:tree
java -jar target/text-tool-1.0.0.jar
```

```text
source compiles
      ↓
3 tests pass
      ↓
JAR is created
      ↓
application prints HELLO MAVEN
```

What the exercise proved:

- Maven recognized the conventional source/test paths.
- The POM identified the project and selected Java 17.
- One test-scoped dependency supplied JUnit classes only to tests.
- Maven resolved JUnit's transitive components.
- Plugins compiled code, ran tests, and packaged the JAR.
- Main classes, test classes, reports, and the artifact went to different places under `target/`.
- A later phase (`package`) included earlier work (`compile` and `test`).

---

## 23. Maven in Java Backend Projects

> Priority: ⭐⭐⭐⭐ IMPORTANT

In a Spring Boot backend, the Maven ideas stay the same; the dependency list simply represents backend capabilities.

```text
Spring Boot application
│
├── Spring Web          → HTTP endpoints and web infrastructure
├── Spring Data JPA     → database persistence support
├── PostgreSQL Driver   → PostgreSQL connection at runtime
├── Validation          → request/domain validation support
└── Testing             → backend test tools
```

A shortened POM may contain:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>USE-THE-VERSION-GENERATED-FOR-YOUR-PROJECT</version>
    <relativePath/>
</parent>

<groupId>com.example</groupId>
<artifactId>bank-api</artifactId>
<version>0.0.1-SNAPSHOT</version>

<properties>
    <java.version>17</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

The missing dependency and plugin versions are **not** a general Maven shortcut. The shown `spring-boot-starter-parent` manages compatible dependency versions and common plugin versions/configuration. Importing only Spring Boot's dependency BOM manages dependency versions, but not build-plugin versions or executions. Generate a real project with the supported Spring Boot version selected for that project rather than copying the placeholder literally.

The Spring Boot parent also gives `java.version` a framework-defined meaning; in a plain standalone Maven POM, use `maven.compiler.release` as taught earlier.

The Spring Boot Maven Plugin can repackage the application as an executable JAR containing its required libraries in a Boot-specific layout. That is different from a normal thin JAR.

Common wrapper commands later will look familiar:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run
```

`spring-boot:run` is a plugin goal, not a lifecycle phase.

**Remember:** Spring dependencies look special because they are numerous and often version-managed, but Maven still resolves coordinates, constructs classpaths, runs lifecycle phases, and creates output under `target/`.

---

## 24. What You Must Memorize

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Memorize this small core. It pays for itself in almost every Maven project.

1. **Maven's job:** read the POM, resolve dependencies/plugins, and coordinate a repeatable build.
2. **Standard paths:**

   ```text
   pom.xml
   src/main/java
   src/main/resources
   src/test/java
   src/test/resources
   target
   ```

3. **Coordinates:**

   ```text
   groupId:artifactId:version
   ```

4. **The most important POM areas:** coordinates, properties, dependencies/scopes, and build plugins.
5. **The four scopes:** `compile`, `runtime`, `test`, `provided`.
6. **Local repository:** `~/.m2/repository` by default.
7. **Important default phases:**

   ```text
   validate → compile → test → package → verify → install → deploy
   ```

8. **The cumulative rule:** invoking a phase also runs earlier phases in that lifecycle.
9. **Core commands:**

   ```text
   mvn --version
   mvn clean
   mvn compile
   mvn test
   mvn package
   mvn verify
   mvn install
   mvn clean package
   mvn clean install
   mvn dependency:tree
   ```

10. **Output rule:** `src/` is developer input; `target/` is generated output.
11. **JAR rule:** a normal Maven JAR is usually thin and is not automatically executable.
12. **Wrapper rule:** use `./mvnw` or `.\mvnw.cmd` when the project provides it.

If these facts become automatic, you know enough Maven to enter ordinary Java and beginner Spring Boot projects confidently.

---

## 25. What You Only Need to Understand

> Priority: ⭐⭐⭐⭐ IMPORTANT

Understand these ideas, but do not memorize their internal implementation:

| Concept | What is enough for now |
|---|---|
| Transitive dependencies | A dependency can bring its own dependencies; inspect them with `dependency:tree` |
| Repository resolution | Maven checks locally, then retrieves missing artifacts from configured remotes |
| Plugins and goals | Plugins do concrete work; goals can be bound to lifecycle phases or called directly |
| Complete lifecycle | More intermediate phases exist than the seven key phases you memorized |
| Scope-specific classpaths | Main compilation, tests, and runtime receive different sets of artifacts |
| IDE import | IDEs read the POM and reproduce Maven source roots/dependency classpaths |
| Maven Wrapper | The project can pin/download Maven without relying on a matching global install |
| JAR versus WAR | JAR is the everyday archive; WAR is a traditional servlet web-app archive |
| Spring Boot version management | A Boot parent/BOM supplies many compatible versions that plain Maven would require explicitly |

You should be able to explain **why** each exists. You do not need to know Maven's internal classes, resolution algorithms, or default plugin bindings by memory.

---

## 26. What You Can Learn Later

> Priority: ⭐⭐ LEARN LATER

These topics are real but outside the 80/20 goal of this guide:

| Learn-later topic | When it becomes relevant |
|---|---|
| Multi-module/aggregator projects | One repository builds several related Maven modules |
| Advanced Maven profiles | A build truly needs selectable environment or platform variations |
| `dependencyManagement` and BOM design | You manage versions across multiple dependencies/modules rather than merely consuming a managed stack |
| Detailed conflict mediation, exclusions, and forced versions | A dependency graph selects an incompatible transitive version |
| Advanced plugin executions/configuration | A build needs code generation, integration-test orchestration, shading, or unusual packaging |
| Writing custom Maven plugins | No existing plugin can perform a required reusable build task |
| Maven internals and custom lifecycles | You are maintaining Maven itself or highly specialized build tooling |
| Enterprise repository managers | A company uses Nexus, Artifactory, mirrors, proxies, or internal artifacts |
| Publishing libraries to Maven Central | You maintain a public Java library |
| Advanced `settings.xml`, credentials, and mirrors | A company or secured repository requires them |
| Advanced `deploy` configuration | A release pipeline publishes artifacts to remote repositories |
| Complex CI/CD configuration | A build is integrated with release, deployment, caching, and security pipelines |
| Advanced Spring Boot Maven configuration | A backend needs layered images, AOT/native processing, custom repackaging, and similar features |
| Gradle | You join a Gradle project; it is a different build tool, not an advanced Maven feature |

Recognize the names so they do not surprise you. Do not delay learning Java backend development to master them.

---

## 27. Maven Mental Model

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

Use one connected picture rather than memorizing isolated definitions:

```text
Developer inputs
├── src/main/java          application source
├── src/main/resources     application resources
├── src/test/java          test source
├── src/test/resources     test resources
└── pom.xml                identity + dependencies + build settings
            │
            ↓
          Maven
            │
            ├── reads coordinates and scopes
            │
            ├── checks ~/.m2/repository
            │         │
            │         └── if missing, downloads from remote repository
            │
            ├── resolves direct + transitive dependencies
            │
            ├── constructs compile/test/runtime classpaths
            │
            └── executes plugin goals along lifecycle phases
                         │
                         ↓
             validate → compile → test → package
                         │
                         ↓
Generated outputs       target/
├── classes/
├── test-classes/
├── test reports
└── artifact.jar or artifact.war
```

The pieces have simple roles:

```text
pom.xml      = project recipe and identity
repositories = artifact supply
Maven        = coordinator
plugins      = workers
JDK          = Java compiler/runtime tools
classpath    = locations visible to a particular task
target/      = generated result
```

The daily development story is therefore:

```text
change source/POM
      ↓
run the narrowest useful Maven phase
      ↓
Maven resolves what is needed
      ↓
plugins compile/test/package
      ↓
inspect success, errors, reports, or artifact
```

**Final mental rule:** The POM declares; Maven resolves and coordinates; plugins act; `target/` receives the outputs.

---

## 28. Final Cheat Sheet

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

### Project anatomy

| Path | Meaning |
|---|---|
| `pom.xml` | Project Object Model |
| `src/main/java` | Main Java source root |
| `src/main/resources` | Main resources |
| `src/test/java` | Test Java source root |
| `src/test/resources` | Test resources |
| `target/` | Generated output; removed by `mvn clean` |

```text
src/main/java/com/example/app/Main.java
→ package com.example.app;
```

### Coordinates and dependency shape

```text
groupId:artifactId:version
com.example:my-app:1.0.0
```

```xml
<dependency>
    <groupId>organization.namespace</groupId>
    <artifactId>library-name</artifactId>
    <version>library-version</version>
    <scope>test</scope> <!-- omit for default compile scope -->
</dependency>
```

### Scope matrix

| Scope | Main compile | Tests | App runtime |
|---|---:|---:|---:|
| `compile` | Yes | Yes | Yes |
| `runtime` | No | Yes | Yes |
| `test` | No | Yes | No |
| `provided` | Yes | Yes | No; environment supplies it |

Scopes control classpaths, not automatic embedding inside a normal JAR.

### Lifecycle

```text
validate → compile → test → package → verify → install → deploy
```

> Invoking a phase runs earlier phases in the same lifecycle. `mvn package` therefore compiles and tests before packaging.

```text
install → local Maven repository
deploy  → configured remote artifact repository
```

### Commands

| Need | Command |
|---|---|
| Check Maven and its Java runtime | `mvn --version` |
| Delete generated build output | `mvn clean` |
| Compile main code | `mvn compile` |
| Compile/run tests | `mvn test` |
| Create JAR/WAR | `mvn package` |
| Run full configured checks | `mvn verify` |
| Install artifact locally | `mvn install` |
| Fresh package | `mvn clean package` |
| Fresh locally installed artifact | `mvn clean install` |
| Explain resolved libraries | `mvn dependency:tree` |

### Wrapper

**Windows PowerShell:**

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

**Linux/macOS:**

```bash
./mvnw test
./mvnw package
```

### Quick diagnosis

```text
command missing      → use wrapper or fix Maven installation/PATH
Java mismatch        → inspect mvn --version + compiler release
dependency missing   → check GAV, scope, network, repositories
unexpected library   → mvn dependency:tree
red IDE import       → verify POM, then refresh/reimport Maven project
no POM error         → run command from project root
JAR will not run     → check executable manifest and runtime dependencies
```

### Artifact truths

```text
mvn package  → target/<artifactId>-<version>.jar (for jar packaging)
mvn install  → also copies artifact/POM to ~/.m2/repository
mvn clean    → removes target/, not the local dependency repository
```

---

## 29. Self-Review Checklist

> Priority: ⭐⭐⭐⭐⭐ MUST KNOW

You are ready to move forward when you can check these without guessing.

### Concepts

- [ ] I can explain Maven in one sentence without calling it an IDE or compiler.
- [ ] I can explain what “build tool” and “dependency management” mean.
- [ ] I can distinguish Java, the JDK, JVM, `javac`, Maven, an IDE, and the classpath.
- [ ] I know why Maven projects use a conventional directory layout.
- [ ] I can explain what POM means and what `pom.xml` describes.
- [ ] I can read `groupId:artifactId:version` coordinates.
- [ ] I understand the difference between a Java import and a Maven dependency.
- [ ] I can explain direct and transitive dependencies with an A → B diagram.
- [ ] I can choose among `compile`, `runtime`, `test`, and `provided` for basic cases.
- [ ] I know the difference between the local repository, a remote repository, and Maven Central.
- [ ] I can explain why the first build may download many artifacts.

### Project and build

- [ ] I put main source in `src/main/java` and tests in `src/test/java`.
- [ ] I can map a package such as `com.example.app` to its path below a source root.
- [ ] I know that `target/` is generated and can be deleted/rebuilt.
- [ ] I can say the important phase order from `validate` through `deploy`.
- [ ] I can explain why `mvn package` also compiles and tests.
- [ ] I understand that `clean` is a separate lifecycle.
- [ ] I know that `install` means local repository and `deploy` means remote artifact repository.
- [ ] I understand how a phase differs from a plugin goal.
- [ ] I know that dependencies and build plugins have different roles.

### Commands and troubleshooting

- [ ] I can run `mvn --version`, `mvn test`, `mvn package`, and `mvn clean verify` appropriately.
- [ ] I use `mvn install` only when I need the artifact in the local repository.
- [ ] I can use `mvn dependency:tree` to explain a transitive library.
- [ ] I know how to run `mvnw`/`mvnw.cmd` when a wrapper exists.
- [ ] I check the Java runtime printed by `mvn --version` when versions disagree.
- [ ] I refresh the IDE's Maven model after changing the POM.
- [ ] I read the first useful error rather than only the final `BUILD FAILURE` line.
- [ ] I know a normal JAR is usually thin and not automatically executable.

### Practical proof

- [ ] I completed the exercise or built a similar Maven project myself.
- [ ] I found main `.class` files under `target/classes`.
- [ ] I found test `.class` files under `target/test-classes`.
- [ ] I found test results under `target/surefire-reports`.
- [ ] I found the packaged artifact under `target/`.
- [ ] I deleted `target/` with `mvn clean` and regenerated it successfully.

Finally, explain this entire flow aloud in your own words:

```text
Create Java Maven project
        ↓
write source code in src/main/java
        ↓
configure pom.xml
        ↓
declare dependencies
        ↓
Maven resolves/downloads dependencies
        ↓
compile
        ↓
test
        ↓
package
        ↓
target/*.jar
```

If you can explain **why** every arrow exists, not just recite the labels, you have learned the essential Maven foundation needed for normal Java projects and the next step into Java backend development.

### Official references for future updates

> Priority: ⭐⭐⭐ NICE TO KNOW

- [Maven in Five Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [Introduction to the Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
- [Maven Wrapper](https://maven.apache.org/tools/wrapper/)
- [Maven Dependency Plugin: `dependency:tree`](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html)
