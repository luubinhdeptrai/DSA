# Amazon S3 and Object Storage — Tiny Java Exercise

**Independent practice** · Java 21 · Maven · Windows 11 PowerShell · Docker Desktop · AWS SDK for Java v2 · **local MinIO only**

> **Goal:** Write **one main Java class** that creates/uses a bucket, uploads a small text file, downloads and prints it, deletes it, and verifies it is gone. **No AWS account, payment details, Spring Boot, or REST API required.**

## Table of contents

- [1. Exercise Instructions — try this first](#1-exercise-instructions--try-this-first)
- [2. Reference Solution — reveal only after trying](#2-reference-solution--reveal-only-after-trying)
- [3. Important syntax explained](#3-important-syntax-explained)
- [4. Troubleshooting](#4-troubleshooting)
- [5. Three review questions and answers](#5-three-review-questions-and-answers)
- [6. Sources and verification notes](#6-sources-and-verification-notes)

---

# 1. Exercise Instructions — try this first

## 1.1 Learning objectives

By the end, you should be able to explain:

1. What a **bucket** is and why an **object key** is not a Windows file path.
2. What `S3Client`, `PutObjectRequest`, `RequestBody`, `GetObjectRequest`, and `DeleteObjectRequest` do.
3. How endpoint, region, and credentials let Java talk to a local S3-compatible server.
4. Why you must verify deletion rather than just print a success message.

**Success condition:** Your program prints the contents of `hello.txt`, then verifies that `notes/hello.txt` no longer exists in the local bucket.

## 1.2 Prerequisites

- Docker Desktop is running with Linux containers enabled.
- Java 21 and Maven are installed and available in PowerShell.
- VS Code is optional; you can edit files with any text editor.

Check:

```powershell
java -version
mvn -version
docker version
```

If Maven is not recognized, install/configure Maven and reopen the terminal before continuing. All commands below are **PowerShell** commands, not Linux shell commands.

## 1.3 Start local object storage (MinIO)

Run this once in PowerShell:

```powershell
docker run -d `
  --name s3-study `
  -p 127.0.0.1:9000:9000 `
  -p 127.0.0.1:9001:9001 `
  -e MINIO_ROOT_USER=localadmin `
  -e MINIO_ROOT_PASSWORD=local-only-password-123 `
  -v s3-study-data:/data `
  quay.io/minio/minio:RELEASE.2025-09-06T17-38-46Z `
  server /data --console-address ":9001"
```

This creates one Docker container and a named volume for local persistent data.

Check it:

```powershell
docker ps --filter name=s3-study
docker logs --tail 25 s3-study
```

- **S3 API endpoint:** <http://127.0.0.1:9000>
- **Browser console:** <http://127.0.0.1:9001>
- **Console login:** `localadmin` / `local-only-password-123`

These are **deliberately local-only demo credentials**. Do not use them for real AWS or expose this service to the internet. The API and console use **different ports**.

If the container already exists, start it instead of creating another:

```powershell
docker start s3-study
```

## 1.4 Create the small standalone Java project

Choose a directory in your home folder:

```powershell
$project = Join-Path $HOME 's3-object-study'
New-Item -ItemType Directory -Force -Path "$project\src\main\java\edu\example\s3" | Out-Null
Set-Location $project
```

Folder structure:

```text
s3-object-study/
├── pom.xml
├── hello.txt
└── src/
    └── main/
        └── java/
            └── edu/
                └── example/
                    └── s3/
                        └── S3Practice.java
```

Create the text file using PowerShell:

```powershell
'Hello from local object storage!' | Set-Content -Path .\hello.txt -Encoding ascii
Get-Content .\hello.txt
```

The word `ascii` ensures the tiny demo text has a simple, predictable encoding. The newline added by `Set-Content` is normal; Java can trim it for display.

## 1.5 Required `pom.xml`

Create a file named `pom.xml` at the project root and paste the following complete content:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.example</groupId>
    <artifactId>s3-object-study</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <aws.sdk.version>2.57.13</aws.sdk.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>${aws.sdk.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.16.0</version>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.7.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

**Why the BOM?** It manages compatible versions of the SDK modules. The separate `s3` dependency actually adds the client library.

## 1.6 Configure local-only SDK credentials

In the **same PowerShell session** where you will run Maven:

```powershell
$env:AWS_ACCESS_KEY_ID = 'localadmin'
$env:AWS_SECRET_ACCESS_KEY = 'local-only-password-123'
```

These values match the local Docker server. They are **not** AWS credentials, and they are scoped to this terminal session and any child processes it launches.

## 1.7 Your coding task

Create `src/main/java/edu/example/s3/S3Practice.java` and implement:

1. Define constants `BUCKET = "s3-study"` and `KEY = "notes/hello.txt"`.
2. Build a **synchronous `S3Client`** with region `US_EAST_1`, endpoint `http://127.0.0.1:9000`, path-style access, and `DefaultCredentialsProvider`.
3. Check whether the bucket exists with `listBuckets()`; create it if absent with `createBucket()`.
4. Upload the existing `hello.txt` using `putObject()` and `RequestBody.fromFile(...)`; set `Content-Type` to `text/plain; charset=utf-8`.
5. Download the object using `getObjectAsBytes()` and print the text.
6. Delete it using `deleteObject()`.
7. Call `headObject()` and consider **HTTP 404** as proof that the object is no longer available at that key. Throw other errors rather than pretending they mean “not found.”
8. Close `S3Client` with **try-with-resources**.

**Hints:** Look up `S3Client.builder()`, `PutObjectRequest.builder()`, `GetObjectRequest.builder()`, `DeleteObjectRequest.builder()`, `HeadObjectRequest.builder()`, and `S3Exception.statusCode()`. The `notes/` prefix does not need a separate folder-creation command.

**Keep it small:** no controller, service classes, tests, database, extra libraries, or image processing.

## 1.8 Run your implementation

From the project root (where `pom.xml` lives):

```powershell
mvn -q compile exec:java '-Dexec.mainClass=edu.example.s3.S3Practice'
```

Expected essential output (minor SDK/Maven logging may vary):

```text
Bucket ready: s3-study
Uploaded: notes/hello.txt
Downloaded: Hello from local object storage!
Deleted: notes/hello.txt
Verified: object not found (HTTP 404)
```

You can open <http://127.0.0.1:9001> to inspect the bucket. After a successful run, the bucket should remain, but the object `notes/hello.txt` should be gone.

### How to verify the exercise

- You saw the exact text downloaded from storage, not merely the local source file.
- Your code used `getObjectAsBytes()` with the bucket and key.
- Your final `headObject()` produced the expected missing-object result after deletion.
- Run it **again**: it should succeed because the same bucket can be reused, and each run deletes its test object.

**Stop here and try writing the program before opening the next section.**

---

# 2. Reference Solution — reveal only after trying

The following is the **entire** `src/main/java/edu/example/s3/S3Practice.java`:

```java
package edu.example.s3;

import java.net.URI;
import java.nio.file.Path;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public final class S3Practice {

    private static final String BUCKET = "s3-study";
    private static final String KEY = "notes/hello.txt";

    public static void main(String[] args) {
        try (S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("http://127.0.0.1:9000"))
                .forcePathStyle(true)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            boolean exists = s3.listBuckets().buckets().stream()
                    .anyMatch(bucket -> BUCKET.equals(bucket.name()));

            if (!exists) {
                s3.createBucket(CreateBucketRequest.builder()
                        .bucket(BUCKET)
                        .build());
            }
            System.out.println("Bucket ready: " + BUCKET);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(KEY)
                    .contentType("text/plain; charset=utf-8")
                    .build();

            s3.putObject(putRequest, RequestBody.fromFile(Path.of("hello.txt")));
            System.out.println("Uploaded: " + KEY);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(KEY)
                    .build();

            ResponseBytes<GetObjectResponse> downloaded =
                    s3.getObjectAsBytes(getRequest);
            System.out.println("Downloaded: " + downloaded.asUtf8String().stripTrailing());

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(KEY)
                    .build());
            System.out.println("Deleted: " + KEY);

            try {
                s3.headObject(HeadObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(KEY)
                        .build());
                throw new IllegalStateException("Object still exists after delete");
            } catch (S3Exception e) {
                if (e.statusCode() != 404) {
                    throw e;
                }
                System.out.println("Verified: object not found (HTTP 404)");
            }
        }
    }
}
```

If you prefer to verify the file structure from PowerShell:

```powershell
Get-ChildItem -Recurse -File | Select-Object FullName
```

Run it using the same command from Section 1.8.

---

# 3. Important syntax explained

## 3.1 `S3Client.builder()`

```java
S3Client.builder()
    .region(Region.US_EAST_1)
    .endpointOverride(URI.create("http://127.0.0.1:9000"))
    .forcePathStyle(true)
    .credentialsProvider(DefaultCredentialsProvider.create())
    .build()
```

- **`S3Client`**: Your Java client for S3 API calls.
- **`builder()`**: Collects configuration before creating the client.
- **`region(...)`**: Supplies a region for request signing.
- **`endpointOverride(...)`**: Sends requests to your local Docker server instead of AWS.
- **`forcePathStyle(true)`**: Addresses an object as `http://127.0.0.1:9000/s3-study/notes/hello.txt` rather than trying to use a bucket subdomain.
- **`DefaultCredentialsProvider.create()`**: Retrieves the local credentials from the configured environment variables.
- **Try-with-resources**: Closes the client when finished.

## 3.2 `PutObjectRequest` versus `RequestBody`

```java
s3.putObject(putRequest, RequestBody.fromFile(Path.of("hello.txt")));
```

The **request** describes the destination `(bucket, key)` and metadata. The **body** supplies the actual bytes from `hello.txt`. A key such as `notes/hello.txt` is not a path on your Windows disk.

## 3.3 `GetObjectRequest` and `ResponseBytes`

```java
ResponseBytes<GetObjectResponse> downloaded = s3.getObjectAsBytes(getRequest);
System.out.println(downloaded.asUtf8String());
```

The `GetObjectRequest` identifies which object to retrieve. `getObjectAsBytes()` downloads its full body into memory; `asUtf8String()` interprets those bytes as UTF-8 text. This is appropriate because this exercise's file is tiny; it is not the standard recommendation for multi-gigabyte objects.

## 3.4 `DeleteObjectRequest` and `HeadObjectRequest`

```java
s3.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET).key(KEY).build());
```

Delete tells the service to remove the current object. A later HEAD asks for metadata without downloading the content, so it provides a simple check that the object is gone. In this local unversioned setup, a missing object returns HTTP 404; in real S3, permissions and versioning can affect responses.

## 3.5 Why `S3Exception.statusCode()`?

A network error, 403 permission denial, or a wrong endpoint should **not** be silently treated as successful deletion. We only interpret the specific expected 404 as “not found” and rethrow other service errors. If the SDK cannot reach the server at all, that is typically a client-side SDK exception rather than an HTTP 404.

**Mental Model:** `S3Client` is a courier, `PutObjectRequest` is the destination label, `RequestBody` is the package contents, `GetObjectRequest` names the package to fetch, and HEAD checks the label without opening the package.

---

# 4. Troubleshooting

| Symptom | Likely reason | PowerShell check or fix |
|---|---|---|
| `mvn` not recognized | Maven missing/not on PATH | `mvn -version`; fix installation/PATH and reopen shell |
| Cannot connect to `127.0.0.1:9000` | Docker container stopped or port conflict | `docker ps -a --filter name=s3-study`; `docker start s3-study`; `docker logs s3-study` |
| Port already in use | Another program uses 9000/9001 | `Get-NetTCPConnection -LocalPort 9000,9001 -ErrorAction SilentlyContinue` |
| Invalid credentials / signature | Java env vars differ from server settings | Re-run the two `$env:AWS_...` assignments; verify `docker logs` |
| Hostname or DNS error | SDK tries virtual-host style | Ensure `.forcePathStyle(true)` and correct `endpointOverride(...)` |
| File not found (`hello.txt`) | Maven started from wrong directory | `Get-Location` and `Test-Path .\hello.txt`; run from project root |
| Maven class not found | Wrong Java path, `package`, or `-Dexec.mainClass` | Verify `src/main/java/edu/example/s3/S3Practice.java` and package declaration |
| HTTP 403 | Credentials or permissions incorrect | Inspect local credentials and bucket policies; don't assume it means missing |
| HTTP 404 before upload | Wrong bucket/key or upload never succeeded | Compare `BUCKET`, `KEY`, and previous logs |
| Unexpected object still present | Delete not completed or wrong key | Check request and inspect MinIO console |

**If container already exists:** `docker run` will reject the duplicate name; use `docker start s3-study` instead.

**Stop or remove the practice server:**

```powershell
docker stop s3-study
# Only if you no longer need the container:
docker rm s3-study
# Optional: deletes ALL data in the named volume. Do this only intentionally.
# docker volume rm s3-study-data
```

The local MinIO API may use an unsecured HTTP connection on loopback only. This is acceptable for a controlled local exercise; **not** an internet-facing deployment configuration.

---

# 5. Three review questions and answers

**Q1.** The object key is `notes/hello.txt`. Must you create a real `notes` directory inside the bucket first?  
**Answer:** No. The slash is part of the key; storage consoles may display prefixes as virtual folders.

**Q2.** Why do we pass `RequestBody.fromFile(...)` in addition to `PutObjectRequest`?  
**Answer:** The request provides bucket/key and metadata; the body provides the file's actual byte content.

**Q3.** If `getObjectAsBytes()` works locally, does that prove your application has the correct permissions on real Amazon S3?  
**Answer:** No. The local service checks local credentials/policies. Real AWS IAM, region, networking, and feature behavior must be verified separately.

---

# 6. Sources and verification notes

Pinned versions and API syntax were checked against references available **13 November 2026**:

- [AWS SDK for Java 2.x — Maven setup/BOM](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup-project-maven.html)
- [AWS SDK for Java 2.x — S3 examples (Put/Get/Head/Delete)](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_s3_code_examples.html)
- [AWS SDK for Java 2.x — endpoint overrides](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/endpoint-config.html)
- [AWS SDK for Java 2.x — path-style option](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html)
- [AWS SDK for Java 2.x — credentials chain](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html)
- [MinIO Docker quickstart](https://github.com/minio/minio/blob/master/docs/docker/README.md)
- [MinIO pinned image in example Compose file](https://github.com/minio/minio/blob/master/docs/orchestration/docker-compose/docker-compose.yaml)
- [AWS SDK BOM 2.57.13](https://mvnrepository.com/artifact/software.amazon.awssdk/bom/2.57.13)
- [Compiler plugin 3.16.0](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin/3.16.0)
- [Exec plugin 3.7.0](https://mvnrepository.com/artifact/org.codehaus.mojo/exec-maven-plugin/3.7.0)

**Execution honesty:** The code, imports, paths, and commands have been cross-checked for consistency and against documented APIs. They have **not** been run end-to-end against Docker/MinIO and downloaded Maven dependencies in this environment; the expected output is therefore an example rather than a claimed observed run.
