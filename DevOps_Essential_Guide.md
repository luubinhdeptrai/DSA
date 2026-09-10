# DevOps Essential Guide

## How to use this guide

This is a practical course for a Java backend learner, from operating a process on a laptop to explaining and practicing a small production delivery workflow. Read a chapter, predict its exercise outcomes, do the exercise, and explain the result without notes. Knowing why a connection fails is more useful than remembering fifty commands.

The running project is **task-api**: a Spring Boot REST API, PostgreSQL persistence, and later an optional Redis service. Redis is introduced to learn another dependency; the initial API does not need a cache. Work in a separate learning repository named `task-api`, not inside an unrelated application. All files shown below are files you create while studying; this guide itself is the single delivered artifact.

**Lab baseline:** Java 21, Spring Boot **3.5.16**, Maven 3.9.x through the Maven Wrapper, Linux/Bash, Docker Engine with the Compose plugin, PostgreSQL 17 in containers, and Redis 7.4. Spring Boot 3.5 is a deliberate teaching baseline that fits existing Spring learning material; it is not a claim that 3.5 is the newest major. Java 21 satisfies its documented requirements. Check support and security updates before an actual production deployment. Documentation was checked on **2026-09-07**, using Context7 and official sources. See [Spring Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

**Windows users:** run blocks labeled `bash` in WSL2 Ubuntu or a Linux VM. PowerShell has different environment-variable syntax, quoting, continuations, and binary-redirection behavior. Windows `mvnw.cmd` corresponds to Linux `./mvnw`; PowerShell uses `$env:DB_URL = '...'` where Bash uses `export DB_URL='...'`. Keep a WSL learning checkout in its Linux filesystem, for example `~/learning/task-api`. Do not paste Bash into PowerShell and diagnose the resulting syntax errors as application failures. Use an editor configured for LF line endings. Initial WSL/VM and Docker installation is a workstation prerequisite; follow the relevant official installer rather than mixing installation commands for different operating systems.

Install only what the current phase needs. Start with a JDK, Git, Bash, `curl`, and an editor; add PostgreSQL when persistence appears, Docker later, and cloud accounts only when reaching the cloud chapter. A Linux VM supports the full systemd labs; some containers and WSL configurations do not run systemd as PID 1.

**Reading conventions:** `replace-me`, `<owner>`, `<host>`, and `<digest>` are placeholders, never real credentials. Replace angle-bracket placeholders before running a command; shell `<` and `>` have their own meanings. A command block is run from the project root unless stated otherwise. Outputs are examples, not promises about process IDs or timings. No cloud service is provisioned merely by reading this file. Destructive actions are identified where taught. Exercises target disposable learning resources.

The starter has no authentication or authorization. Use loopback, an SSH tunnel, or an isolated lab network while learning. Public deployment of a real application requires the access controls you learn with Spring Security, appropriate TLS, protected operations endpoints, and the operational work in later chapters. An example that builds successfully has not thereby become a production application.

Role-depth labels used throughout mean **B** = Java backend developer, **D** = DevOps engineer, **S** = SRE, **C** = cloud engineer. *Explain* means reason about behavior; *use* means perform ordinary work; *operate* means diagnose, recover, and own changes. Job titles vary between organizations.

### Navigation

| Start and build | Deliver and host | Operate and consolidate |
|---|---|---|
| [1. Foundations](#1-devops-foundations) | [8. Docker](#8-docker) | [18. Observability](#18-observability) |
| [2. Linux](#2-linux) | [9. Compose](#9-docker-compose) | [19. Reliability](#19-reliability-and-sre) |
| [3. Networking](#3-networking) | [10. CI/CD](#10-cicd-with-github-actions) | [20. Database operations](#20-postgresql-operations) |
| [4. Git](#4-git-and-collaboration) | [11. Registries and environments](#11-registries-and-environments) | [21. Migrations](#21-database-migrations) |
| [5. Builds](#5-builds-and-artifacts) | [12. Deployment and releases](#12-deployment-and-release-strategies) | [22. Security](#22-security-and-devsecops) |
| [6. Processes and configuration](#6-processes-and-application-configuration) | [13. Proxies](#13-reverse-proxies-and-load-balancing) | [23. Troubleshooting](#23-troubleshooting-and-production-mindset) |
| [7. Starter project](#7-build-the-running-spring-boot-project) | [14. Cloud](#14-cloud-fundamentals), [15. IaC](#15-infrastructure-as-code) | [24. Spring operations](#24-operating-java-and-spring-boot) |
| [Roadmap](#devops-roadmap) | [16. Kubernetes](#16-kubernetes), [17. Secrets](#17-configuration-and-secrets) | [25. Comparisons](#25-commonly-confused-concepts), [26. Readiness](#26-final-readiness-tests), [27. Schedule](#27-study-schedule), [Cheat sheet](#28-final-cheat-sheet) |

## DevOps roadmap

Priorities describe what to learn **at this stage**, not the status of a technology. **⭐⭐⭐⭐⭐ MUST KNOW** means necessary for independently operating a small backend. **⭐⭐⭐⭐ IMPORTANT** means high practical value after foundations. **⭐⭐⭐ NICE TO KNOW** means recognize the option and practice when relevant. **⭐⭐ FUTURE / ADVANCED** means postpone depth until your system creates the need.

| Phase | Concepts and priority, with the reason | Evidence that you can move on |
|---|---|---|
| 0. Delivery mental model, ch. 1 | ⭐⭐⭐⭐⭐ Code-to-runtime and feedback: puts every later tool in context. ⭐⭐⭐⭐ Team ownership: changes require coordination. ⭐⭐ Organization-wide platform design: no need yet. | Draw code → artifact → process → feedback and assign owners. |
| 1. Linux, ch. 2 | ⭐⭐⭐⭐⭐ Paths, permissions, processes, logs, resources: every server failure touches these. ⭐⭐⭐⭐ SSH and services: needed to run remotely. ⭐⭐ Kernel tuning: defer until measurements justify it. | Find a process, its listening port, and its log; stop it gracefully. |
| 2. Networking, ch. 3 | ⭐⭐⭐⭐⭐ DNS, IP, TCP, ports, HTTP/TLS: explain connection failures. ⭐⭐⭐⭐ Subnets, NAT, firewall/proxy behavior: needed at deployment. ⭐⭐ Advanced routing: unnecessary for one backend. | Distinguish DNS failure, refused connection, timeout, and HTTP failure. |
| 3. Git, ch. 4 | ⭐⭐⭐⭐⭐ Commit, branch, review, fetch/merge: safely share change. ⭐⭐⭐⭐ Rebase, tags, releases: understand provenance. ⭐⭐⭐ GitFlow: recognize where release branches are justified. | Make and review a small change; resolve a conflict without losing work. |
| 4. Build and configure, ch. 5–7 | ⭐⭐⭐⭐⭐ Maven, tests, JARs, configuration: source is not a running service. ⭐⭐⭐⭐ Artifact checksums and parity: detect differences. ⭐⭐ Native images/JVM internals: postpone. | Produce and run one tested JAR with external DB configuration. |
| 5. Containers, ch. 8–9 | ⭐⭐⭐⭐⭐ Images, lifecycle, ports, storage, Compose: package predictable runtime. ⭐⭐⭐⭐ Resource limits, non-root, health: avoid misleading success. ⭐⭐⭐ Redis caching design: optional to API functionality. | Recreate app without losing DB rows; explain container localhost. |
| 6. CI and releases, ch. 10–13 | ⭐⭐⭐⭐⭐ CI tests, immutable artifact identity, manual deployment: make delivery inspectable. ⭐⭐⭐⭐ Registry, supervised runtime, TLS proxy, rollback: operate a hosted app. ⭐⭐⭐ Canary automation: learn after simple releases. | A commit produces a tested image; deploy a selected digest and reverse it. |
| 7. First operations loop, ch. 18–21 and 23–24 | ⭐⭐⭐⭐⭐ Logs, health, restore, migrations, timeouts: necessary **before** real users. ⭐⭐⭐⭐ Metrics/SLOs and incident practice: measure outcomes. ⭐⭐ Tracing every service: wait for distributed problems. | Diagnose a failed request and restore a backup into an isolated database. |
| 8. Cloud, ch. 14 | ⭐⭐⭐⭐⭐ IAM, firewall boundaries, billing, managed DB tradeoffs: avoid costly mistakes. ⭐⭐⭐⭐ Regions/AZs, storage, load balancers: make architecture deliberate. ⭐⭐ Multi-region: wait for recovery requirements. | Host an isolated lab and account for compute, disks, network, and cleanup. |
| 9. IaC, ch. 15 | ⭐⭐⭐⭐ Plan/state/drift/locking: make infrastructure repeatable after understanding manual setup. ⭐⭐⭐ Modules: useful after repetition appears. ⭐⭐ Large module frameworks: avoid abstraction before experience. | Review a plan and identify replacement, state risks, and cleanup. |
| 10. Kubernetes, ch. 16 | ⭐⭐⭐⭐ Pods, Deployment, Service, probes: useful platform literacy, not mandatory hosting. ⭐⭐⭐ Ingress, PVC, scaling labs: add when the project needs them. ⭐⭐ Cluster administration/service mesh: future specialization. | Explain failed scheduling versus failed readiness; roll out and inspect an app. |
| 11. Security, ch. 17 and 22 | ⭐⭐⭐⭐⭐ Secrets, least privilege, updates, TLS: apply from phase 1 onward. ⭐⭐⭐⭐ Scanning and supply-chain identity: strengthen delivery. ⭐⭐ Advanced security testing: specialist depth. | Rotate a lab credential and show where the old value stops working. |
| 12. Consolidation, ch. 25–28 | ⭐⭐⭐⭐⭐ Incident reasoning and recovery: integrate rather than memorize. ⭐⭐⭐⭐ Capacity and release practice: preparation for ownership. ⭐⭐ Sophisticated SRE mathematics: learn when decisions require it. | Complete the final readiness scenarios and explain evidence for each decision. |

The reference chapters put platform topics together for easy lookup. The recommended **practice order** moves basic operations ahead of spending money or exposing a service: **1–13 → 18–21 → 23–24 → 14 → 15 → 16**, with **17 and 22 revisited at every deployment**. Your first cloud deployment need not use Terraform or Kubernetes. Observability and security are continuous habits, not graduation prizes.

## 1. DevOps foundations

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW. **Role depth:** B use; D operate delivery; S operate reliability; C operate infrastructure.

### 1.1 The problem: working code is not delivered value

A developer can pass tests on a laptop while customers see errors: the server has a different JDK, a missing environment variable, an expired certificate, or a full database disk. Historically, development teams optimized for change and operations teams for stability, with handoffs hiding the information each needed. DevOps is a set of collaborative practices that makes delivering, operating, and improving software a shared responsibility. A tool purchase cannot produce that collaboration.

Think of the runtime as another consumer of your Java program's contract. A method needs arguments and dependencies; a deployed process needs configuration, compute, network access, storage, and a way to report failure. Delivery quality includes all of them.

```text
Code -> Version control -> Build -> Test -> Package -> Artifact
                                                        |
                                                        v
Improve <- Observe <- Run <- Deploy <--------------------+
   |
   +----------------------> next change
```

The arrows are transformations and checks. Source becomes bytecode; a JAR becomes an image; an image becomes a running process. Record which input produced each output. Runtime evidence flows back into changes, tests, alerts, and runbooks.

### 1.2 Culture, delivery, and ownership

| Concept | What it means in task-api | What it does not guarantee |
|---|---|---|
| Development | Implement and test `/tasks`, including failures. | A runnable service on a remote machine. |
| Operations | Keep it reachable, secure, observable, backed up, and recoverable. | Correct business rules. |
| DevOps culture | Developers and operators jointly review deployability and incidents; automate repeatable steps. | Everybody must become an expert in every tool. |
| DevOps tooling | Git, CI, images, IaC, telemetry support repeatability and feedback. | Good review, safe defaults, or shared ownership automatically. |
| Continuous Integration (CI) | Integrate small changes frequently and automatically build/test them. | A successful production deployment. |
| Continuous Delivery | Every accepted change can be released; an explicit release decision may remain. | Every commit reaches customers. |
| Continuous Deployment | Accepted changes reach production automatically after defined gates. | Tests catch every failure or rollback is always safe. |

SRE applies software engineering to operating reliable services, using measurable service objectives, automation, and incident learning. DevOps describes a broader approach to delivery and collaboration; SRE is one concrete way to organize reliability work. A platform team offers internal capabilities such as deployment templates and environments as a product, so feature teams can safely self-serve. A cloud engineer specializes in infrastructure built from cloud services. A DevOps engineer often owns delivery automation and shared runtime tooling. Actual responsibilities overlap; ask what a team owns rather than relying on its job titles.

A backend engineer should build a deployable JAR, externalize configuration, use Git and CI, understand ports and container behavior, interpret logs, expose useful health signals, design safe migrations, and participate in debugging. Running the entire Kubernetes control plane is not a prerequisite for writing good Spring services.

### Exercise

**Objective:** identify missing delivery responsibilities. **What to do:** draw the preceding lifecycle for an API you have written. **Starter:** make a table with `stage | input | output | failure evidence | owner`. **Expected result:** every stage has both an output and an observable failure. **How to verify:** answer where a wrong DB password would first be detected, and where a syntactically broken Java file would fail. **Common mistakes:** writing “Docker” as the owner or treating production as just a folder. **Explanation:** tools perform steps; people and teams own the outcomes and recovery.

### Checkpoint

1. Tests pass but customers cannot connect. Is CI broken?
2. A release needs a person to press “deploy.” Can the team still practice Continuous Delivery?

<details><summary>Attempt first, then reveal answers</summary>

1. Not necessarily. CI may lack a useful check, but DNS, networking, runtime configuration, and capacity can fail independently of tested code. Inspect the failing boundary.
2. Yes. Delivery means a releasable change; Deployment means automated production release after gates.

</details>

### Interview relevance

Backend: explain the runtime requirements of your service. Junior DevOps: explain a delivery pipeline, feedback, ownership, and the three meanings of CI/CD. Common question: “What would you improve if deployments repeatedly fail after tests pass?” Without notes, describe how to reproduce the environment difference and add the earliest meaningful check.

**Remember:** delivery is a chain of contracts with feedback, not a collection of product logos.

## 2. Linux

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW; service administration ⭐⭐⭐⭐ IMPORTANT. **Role depth:** B use; D/S/C operate.

### 2.1 Files, identity, and permissions

Linux organizes resources around processes, files, and permissions. A process runs as a user with groups. The operating system checks that identity when it opens a file or performs a privileged operation. Java file access exceptions often originate at this boundary, not in Spring.

| Location | Purpose and example | Common mistake |
|---|---|---|
| `/` | Root of the filesystem; `/etc/task-api` is absolute. | Confusing it with `/root`, a user's home. |
| `/home/learner` | Ordinary user's home and workspace. | Running everyday development as root. |
| `/etc` | Machine configuration, such as service configuration. | Putting frequently changing application data there. |
| `/var/log`, `/var/lib` | Logs and durable service data. | Deleting DB files to “fix disk full.” |
| `/tmp` | Temporary files with no durability promise. | Treating it as a backup location. |
| `/usr/bin`, `/usr/lib` | Installed executables and libraries. | Replacing managed JDK files manually. |
| `/opt/task-api` | A reasonable application installation location. | Assuming all distributions use the same layout. |
| `/proc` | Kernel-generated process/runtime information. | Treating it as ordinary stored files. |

Relative paths start from the current directory; absolute paths start at `/`. `pwd` prints that directory. `ls -la` lists details (`-l`) including hidden entries (`-a`). `cd ..` changes to the parent. Shells expand `~` to the current user's home. Names are case-sensitive. Quote paths with spaces, for example `cd 'my project'`.

```bash
mkdir -p ~/learning/task-api/logs
cd ~/learning/task-api
touch logs/app.log
ls -l logs/app.log
chmod 640 logs/app.log
id
```

`mkdir -p` creates parent directories if needed; `touch` creates an empty file or updates its modification time. `chmod` changes access bits: owner **6 = read(4)+write(2)**, group **4 = read**, others **0 = none**. Directory execute permission means traversal, not “run the directory.” `id` shows the current user and groups. `chown taskapp:taskapp file` changes ownership, usually requiring administrative privilege; only use the actual intended service identity. `chmod +x mvnw` lets the owner/group/others execute according to the existing mode and umask rules; commit the executable bit for Linux CI.

Use `sudo` for a specific administrative action, not as a cure for every permission error. `chmod 777` gives every local user write access and obscures the real ownership problem. Private keys and secret files usually require restrictive permissions such as `600` with an appropriate owner. The shell's `umask` removes permissions from defaults; `umask 077` helps when creating private files.

### 2.2 Shells transform streams, not just commands

The shell finds executables through `PATH`, splits arguments, expands variables, and connects streams. `command -v java` identifies the executable that will run; `java -version` identifies its version. Changing `JAVA_HOME` does not necessarily change the `java` found first on `PATH`.

```bash
export APP_MODE=dev
printf '%s\n' "$APP_MODE"
printf '%s\n' 'INFO started' 'ERROR DB unavailable' > logs/app.log
grep -n 'ERROR' logs/app.log
find . -type f -name '*.log'
tail -n 20 logs/app.log
```

`export` places a variable in the environment inherited by new child processes. Double quotes allow variable expansion without unwanted splitting; single quotes preserve literal text. `printf` formats text (`%s` is a string, `\n` a newline). `>` creates or **truncates** a file; here the disposable lab log is intentionally overwritten. `>>` appends. `2>` redirects standard error; `2>&1` connects it to the current standard output destination. Never casually redirect over a configuration or backup file.

`grep -n` selects matching lines with line numbers. A pipe, as in `journalctl -u task-api --since '10 minutes ago' | grep -i error`, sends one program's stdout into the next program's stdin. It does not transfer stderr unless redirected. `-i` ignores case. `find` walks directories; `-type f` selects regular files and the quoted wildcard is interpreted by `find`, not expanded prematurely by the shell. `tail -f` follows new log lines; Ctrl+C exits the viewer, not the application.

Shell scripts can automate known working steps; they do not make steps safe automatically. Inspect exit codes (`echo $?` immediately after a command), quote variables, and use clear failure handling. In Bash pipelines, `set -o pipefail` helps report earlier command failures that the last command's success would otherwise hide. Avoid dumping all environment variables when secrets may be present.

### 2.3 Diagnose a running service

| Problem → command | What happens and how to read it | Common mistake |
|---|---|---|
| Find processes: `ps -ef` | Lists processes and full command lines, including PID and parent. Match the service carefully. | Killing the first Java PID, which may belong to another app. |
| Identify an exact process: `ps -p 12345 -o pid,ppid,%cpu,%mem,args` | Shows selected columns for placeholder PID 12345. | Assuming a PID remains the same after restart. |
| Live resource use: `top` | Repeated snapshots; press `q` to leave. | Treating one momentary spike as a sustained bottleneck. |
| Ask it to stop: `kill -TERM 12345` | Sends SIGTERM; a Java shutdown hook can run. | Thinking `kill` always means an immediate forced kill. |
| Unresponsive process: `kill -KILL 12345` | **Disruptive last resort:** SIGKILL prevents cleanup. Verify target and impact first. | Using it routinely and losing in-flight work. |
| Memory: `free -h` | Human-readable used/available memory; cached memory may be reclaimable. | Expecting Linux to leave RAM unused. |
| Filesystem space: `df -h` | Capacity per mounted filesystem. | Looking at another mount from the one holding DB data. |
| Inodes: `df -i` | Shows count of file records available; can exhaust before bytes. | Assuming “space available” guarantees new files can be created. |
| Directory growth: `du -sh ./logs` | Summarizes bytes below that directory. | Scanning all `/` repeatedly during an incident. |
| Listening sockets: `ss -ltnp` | Listening TCP (`-l -t`), numeric (`-n`), process (`-p`, may need privileges). | Assuming a listener proves a healthy application. |

Ctrl+C usually sends SIGINT to the foreground process group. `java -jar target/task-api.jar &` starts a background process and `$!` captures its PID, but it is not a reliable service deployment: logout, failures, and log handling still need attention. A supervisor such as systemd runs services with defined identities, configuration, restart rules, and lifecycle control. Alternatives include a container runtime or a platform supervisor. None fixes a broken application.

### 2.4 Remote access, downloads, packages, and services

Use SSH for an authenticated, encrypted session: `ssh learner@lab-host`. `learner` is the remote account; `lab-host` resolves to the server. Verify the server's host-key fingerprint through your provider/administrator before accepting a new host. A changed key can be a legitimate rebuilt VM or an interception warning; investigate it rather than disabling checks. A private key stays private; install its public key on the server.

`scp target/task-api.jar learner@lab-host:/tmp/task-api.jar` copies a file over SSH; it does not start or install the service. `ssh -L 18080:127.0.0.1:8080 learner@lab-host` forwards laptop port 18080 through SSH to **the server's** loopback8080. Browse `http://127.0.0.1:18080` while the session is active. This is useful for a private learning API.

| Task | Command structure and effect | Mistake to avoid |
|---|---|---|
| Download a specified release file | `curl --fail --location --output release.tar.gz https://example.com/release.tar.gz` follows redirects and fails on HTTP errors. The URL is a placeholder. | Executing downloaded shell scripts without inspecting source and trust. |
| Alternative downloader | `wget -O release.tar.gz https://example.com/release.tar.gz` writes to the specified file. | Saving an HTML error response and assuming it is an archive. |
| Inspect an archive | `tar -tzf release.tar.gz` lists (`t`) a gzip (`z`) archive file (`f`). | Extracting unknown contents into a privileged directory. |
| Extract into a prepared scratch directory | `tar -xzf release.tar.gz -C scratch` extracts (`x`) there. | Overwriting existing files; first inspect names and choose an empty directory. |
| Compress a lab log while retaining original | `gzip -c logs/app.log > logs/app.log.gz` writes compressed output to stdout (`-c`). | Plain `gzip file` normally replaces the original with the compressed file. |
| Ubuntu/Debian package index | `sudo apt update` refreshes package metadata. | Thinking this alone upgrades installed software. |
| Install known packages | `sudo apt install git curl unzip` installs dependencies through the distribution repository. | Mixing `apt` with commands intended for Fedora/RHEL's `dnf`. |
| Inspect service | `systemctl status task-api --no-pager` reports service state. | Equating `active` with successful business requests. |
| Start/enable | `sudo systemctl start task-api`; `sudo systemctl enable task-api` | Start acts now; enable configures boot activation. Neither implies the other unless using `enable --now`. |
| Inspect logs | `journalctl -u task-api -n 100 --no-pager` selects that unit and recent entries. | Missing permissions to read the journal, or reading logs from a previous boot. |

The unit file appears in chapter12, after you have a JAR to supervise. `systemctl daemon-reload` makes systemd reread unit definitions; it does not restart the Java process or reload arbitrary application YAML. See the official [systemctl reference](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html).

### Exercise

**Objective:** connect process, log, and signal. **What to do:** in the scratch directory, run `sleep 120 &`, record `$!`, inspect that PID with `ps`, then send `kill -TERM` to that exact PID. **Commands/starter:** use the process commands above; create the sample log and find its error line. **Expected result:** the chosen sleep process disappears; the log remains. **How to verify:** `ps -p <recorded-pid>` no longer reports the process and `grep -n ERROR logs/app.log` finds line2. **Common mistakes:** using a PID copied from this guide, checking `$!` after launching another background job, or treating files as process memory. **Explanation:** process lifetime and file lifetime are separate.

### Checkpoint

1. A file is readable but its parent directory denies traversal. Can the service open it?
2. `df -h` is healthy but creating files fails with no space. What next?
3. Why is restarting the SSH connection not equivalent to restarting Java?

<details><summary>Answers</summary>

1. Not through that path without the required directory permissions. 2. Check inodes and the correct mount, quotas, and logs. 3. SSH and Java are separate processes; a properly supervised service outlives the login session.

</details>

### Interview relevance

Backend: find JDK version, a log, a PID, and an occupied port. Junior DevOps: explain identity, permissions, signals, disk/memory checks, SSH, and service lifecycle. Common question: “The app starts with sudo but not as its service user—what do you inspect?” Without notes, trace ownership and directory permissions instead of granting everyone access.

**Remember:** ask which process, which user, which file, and which resource.

## 3. Networking

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW; subnet/NAT/load-balancer details ⭐⭐⭐⭐ IMPORTANT. **Role depth:** B use; D/S/C operate.

### 3.1 An address identifies a destination in a context

An IP address identifies a network interface within a network scope. A public IP can be routed on the public Internet; a private IP belongs to ranges such as `10.0.0.0/8`, `172.16.0.0/12`, or `192.168.0.0/16`, which are not directly routed across the public Internet. Private does not mean authenticated or encrypted. IPv6 also matters; a host can be reachable over IPv6 even when you only inspected IPv4 rules.

A subnet groups addresses with a prefix: `192.168.10.0/24` means the first24 bits identify the network. A router forwards between networks; a gateway is the next router used to reach destinations outside the local network. `ip addr` shows local interfaces and addresses; `ip route` shows routes and the default gateway. Cloud subnets are a later application of the same idea.

`localhost` usually resolves to loopback `127.0.0.1` and/or `::1`. Loopback means **this network namespace**. On a laptop that is the laptop; in a normal container it is that container's namespace. `0.0.0.0` is an IPv4 wildcard used when **binding** a server to all its IPv4 interfaces. It is not a remote service's destination address, and binding it does not by itself open a firewall. Binding only `127.0.0.1` deliberately prevents connections addressed to the host's other interfaces.

### 3.2 Ports, sockets, transport, and HTTP

A port is a transport-level number that helps the OS deliver traffic to a process. A listening TCP socket is an OS endpoint associated with protocol, local address, and port. An established TCP connection has both local and remote endpoints; many connections can share server port8080 because their other endpoint information differs. The Java embedded web server opens the listening socket; `server.port` selects its port.

TCP supplies an ordered byte stream with connection setup and retransmission, but does not define application messages or guarantee application-level success. UDP sends datagrams without TCP's delivery/order guarantees; applications may add their own reliability. DNS commonly uses UDP and also TCP; HTTP/1.1 and HTTP/2 commonly use TCP, while HTTP/3 uses QUIC over UDP. You need these distinctions to understand firewall rules, not to implement transport protocols yourself.

HTTP adds requests, methods, paths, headers, status codes, and bodies. HTTPS protects HTTP using TLS: negotiate encryption, verify the server's certificate chain and identity, then exchange HTTP. Certificates bind identities to public keys; the **private key** proves possession and must remain secret. A public certificate normally is not confidential, but do not commit bundles that contain private keys or treat arbitrary certificate material as safe. A certificate must be valid for the hostname, within its validity period, and chain to a trusted issuer.

### 3.3 Follow an actual request

```text
Browser: https://api.example.com/tasks
  |
  +-- DNS lookup: hostname -> IP address (possibly cached)
  |
  v
Public IP -> network route -> firewall -> reverse proxy/load balancer :443
                                           | TLS ends here, by design
                                           v
                                     Spring Boot :8080
                                           |
                                      JDBC over TCP
                                           v
                                  private PostgreSQL :5432
```

DNS is a naming system, not a proxy carrying every subsequent HTTP request. A firewall allows or blocks traffic according to rules; cloud security groups commonly express allow rules around resources. NAT rewrites addresses or ports as traffic crosses a boundary, often allowing private hosts outbound access through a public address. NAT does not mean every private server becomes reachable inbound.

A **forward proxy** acts for clients accessing other services, such as a company's outbound proxy. A **reverse proxy** acts for servers receiving client traffic. A **load balancer** distributes requests/connections among backends; many reverse proxies can also load balance. DNS, TLS, proxy routing, and backend processing are different stages with different failure evidence. Inside Compose or Kubernetes, service DNS translates stable service names to the appropriate network endpoints, reducing reliance on changing container IPs.

### 3.4 Diagnose one layer at a time

```bash
getent hosts api.example.com
curl -v --connect-timeout 3 --max-time 10 https://api.example.com/tasks
ss -ltnp
```

`getent hosts` asks the OS name-service mechanism, including sources such as hosts files and DNS. `dig api.example.com` asks DNS directly and shows records/TTL; it may need the distribution's DNS utilities package. `curl -v` shows connection and TLS details, so avoid sharing verbose output containing authorization headers. `--connect-timeout` bounds connection establishment; `--max-time` bounds the whole operation. `curl -i` includes response headers; `curl --fail` makes HTTP4xx/5xx fail the command. A default curl exit0 means the transfer completed, not necessarily that HTTP returned success.

| Symptom | Meaning to test | Useful next evidence |
|---|---|---|
| DNS resolution failure | Name cannot be translated by the resolver used here. | `getent hosts`, `dig`, spelling, resolver configuration, namespace. |
| Connection refused | Destination actively rejected connection, often no listener at address/port. | `ss -ltnp` at server; container port publishing and bind address. |
| Connection timeout | No timely response during connection setup. | Routing, firewall drops, destination reachability, load balancer status. |
| Read/request timeout | Connection exists but operation exceeds time budget. | Application latency, pool waits, downstream query duration. |
| Port already in use | Another socket conflicts with the requested bind. | `ss -ltnp`; inspect owner before changing or stopping it. |
| Works locally, fails remotely | Loopback-only binding, wrong address, or filtering may be responsible. | Compare server `curl 127.0.0.1:8080` and an allowed remote request. |
| Certificate verification failure | Hostname, trust, clock, or expiry may be wrong. | Browser details or `openssl s_client -connect api.example.com:443 -servername api.example.com`. |

`openssl s_client` opens a diagnostic TLS client; `-connect` selects address/port and `-servername` sends the hostname used for SNI. Inspect verification results. Do not “fix” certificate failures by permanently using `curl -k`; it disables a protection your real clients rely on. Ping failure does not establish HTTP failure: ICMP may be blocked while TCP443 is permitted.

### Exercise

**Objective:** distinguish connection failure from HTTP failure. **What to do:** after chapter7 starts the API, call its valid endpoint, an unknown path, and an unused local port. **Commands/starter:** `curl -i http://127.0.0.1:8080/api/hello`, `curl -i http://127.0.0.1:8080/no-such-route`, `curl --connect-timeout 2 http://127.0.0.1:18081`. **Expected result:** HTTP200, HTTP404, and usually a refused connection, assuming18081 is unused. **How to verify:** match each response to the layer and inspect8080 with `ss`. **Common mistakes:** expecting404 for a server that cannot be contacted. **Explanation:** HTTP status requires reaching an HTTP-speaking component; transport failure occurs earlier. Before chapter7, predict results and inspect any existing listeners instead.

### Checkpoint

1. The app binds `0.0.0.0:8080`. What URL should a remote client use?
2. Does a successful DNS lookup prove the server is running?
3. Why can `localhost:5432` work on a host but fail inside its app container?

<details><summary>Answers</summary>

1. A reachable hostname/IP for that host, with the appropriate published port, routing, and firewall permission. 2. No; DNS is naming evidence only. 3. The container has a different loopback context; use the database's reachable service name or host address.

</details>

### Interview relevance

Backend: explain the path from HTTP request to JDBC call. Junior DevOps: locate failures at DNS, route/firewall, socket, TLS, proxy, or app. Common question: “What is the difference between refused and timed out?” Without notes, give one plausible cause and one discriminating check for each.

**Remember:** name → address → route → socket → TLS/HTTP → application → dependency.

## 4. Git and collaboration

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW; rebase/release workflows ⭐⭐⭐⭐ IMPORTANT. **Role depth:** B/D use daily; S/C use for operational changes.

### 4.1 Version control is a record of intended change

Copying folders named `final-v2-really-final` does not explain who changed a timeout or which code produced an image. Git stores versioned snapshots and relationships between them. A repository includes this history; the working tree is your checked-out files; the staging area is the proposed contents of your next commit. A branch is a movable reference to a commit, not another server environment.

```text
Working tree --git add--> Staging area --git commit--> Local history
     ^                                                   |
     | checkout/switch                                   | git push
     +---------------------------------------------------+--> Remote
                  Remote --git fetch--> remote-tracking references
```

Git is distributed: a remote such as `origin` is a named repository location. GitHub hosts repositories and adds pull requests, reviews, CI integration, and permissions; Git itself does not require GitHub. Alternatives to Git include other version-control systems, but the transferable idea is inspectable, reviewable changes.

### 4.2 The everyday workflow

```bash
git init -b main
git status
git switch -c feature/health-notes
git add README.md
git diff --staged
git commit -m "Document application health endpoint"
git log --oneline -5
```

In a **new lab directory**, `init -b main` initializes the main branch. `switch -c` creates and switches to a branch. Create/edit `README.md` before adding it. `add` selects content; `diff --staged` reviews exactly what the commit will contain. `commit -m` records that snapshot with a message; Git needs your configured author name/email first. `log --oneline -5` shows five concise commits. `git diff` without `--staged` shows unstaged differences instead. Review both before pushing.

For an existing repository, `git clone <repository-url>` creates a checkout and remote. After creating your own remote repository, `git remote add origin <repository-url>` connects a newly initialized local repo. `git push -u origin feature/health-notes` sends the branch and records its upstream. These commands contact the named remote; verify it is your intended repository.

`git fetch origin` updates your knowledge of remote history without merging it into your working branch. `git pull --ff-only` fetches and updates only when the local branch can move forward without combining divergent changes; otherwise it stops so you can choose a strategy. Ordinary `pull` may merge or rebase depending on configuration. See the official [Git user manual](https://git-scm.com/docs/user-manual).

A pull request proposes integrating a branch. Include why the change exists, how it behaves, and evidence it works. Reviews inspect correctness and operational impact: does a new query need an index, does a configuration key break deployments, and can the release be reversed? Short branches and small reviewed changes feeding a stable main branch are common in trunk-based workflows. GitFlow maintains longer-lived development and release/hotfix branches; it can fit scheduled parallel releases but adds coordination. Neither branch model compensates for missing tests.

### 4.3 Integrating, releasing, and undoing

A merge combines histories, possibly with a merge commit. A rebase replays commits on a new base, producing new commit identities. Rebase is useful for a private feature branch; rewriting shared history disrupts other contributors. Resolve a merge conflict by reading both intended changes, editing a coherent result, removing conflict markers, staging it, and completing the merge with `git commit`. Run the relevant tests. `git merge --abort` abandons an in-progress merge; preserve unrelated work before starting. For rebase, stage the resolution and run `git rebase --continue`, or use `git rebase --abort`. Do not mechanically select “ours” for every file.

`git revert <commit-sha>` adds a new commit undoing a prior change, useful for shared history. **Destructive commands:** `git reset --hard`, `git clean -fd`, and force pushes can discard work or rewrite others' history; they are not routine troubleshooting steps. Do not run them without inspecting precisely what would be lost. A Git revert is also not an automatic database or infrastructure rollback.

An annotated release tag, `git tag -a v0.1.0 -m "First learning release"`, names a particular commit; `git push origin v0.1.0` publishes that specific tag. Semantic versioning uses `MAJOR.MINOR.PATCH` to signal incompatible public-API changes, compatible features, and compatible fixes, under a documented API contract. A tag is a label; protecting tags and recording artifact digests makes release identity stronger. Avoid moving an already consumed release tag.

Create `.gitignore` **before** creating credentials:

```gitignore
target/
.idea/
*.log
.env
.env.*
!.env.example
.secrets/
*.pem
*.key
*.p12
*.dump
.terraform/
*.tfstate
*.tfstate.*
*.tfplan
```

Git reads this for untracked-file exclusion; it does not untrack files already committed or remove old secrets from history. Commit source, `pom.xml`, wrapper scripts and `.mvn/`, non-secret configuration, migrations, Dockerfiles, and workflows. Commit `.env.example` only with placeholders. Do not ignore the wrapper merely because it starts with a dot. If a credential was committed, revoke/rotate it first, then coordinate history cleanup where needed; deleting the line is insufficient.

### Exercise

**Objective:** practice a reviewed conflict resolution. **What to do:** in the lab repo create two branches that edit the same README sentence differently, commit both, and merge one into the other. **Commands/starter:** use `switch -c`, `add`, `commit`, then `git merge <other-branch>`. **Expected result:** a conflict or a clean merge depending on the exact changes; intentionally editing the same line usually conflicts. **How to verify:** `git status`, inspect the file, resolve, commit, and view `git log --oneline --graph --all`. **Common mistakes:** leaving `<<<<<<<` markers or forgetting unstaged work. **Explanation:** the human resolves meaning when Git cannot infer how the edits combine.

### Checkpoint

1. You edited a password out of the current file. Is the leaked value gone from history?
2. Does `fetch` deploy or merge the remote branch?

<details><summary>Answers</summary>

1. No; rotate it and handle stored history and downstream copies. 2. Neither; it updates objects and remote-tracking information. Integration and deployment are separate actions.

</details>

### Interview relevance

Backend: explain working/staged/committed state and review a PR. Junior DevOps: trace a release to source and distinguish fetch, pull, merge, rebase, revert, and deployment rollback. Common question: “Why should we avoid rebasing shared main?” Without notes, explain changed identities and disruption to downstream work.

**Remember:** Git records intended changes; the pipeline must still prove and deliver them.

## 5. Builds and artifacts

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW; reproducibility/repositories ⭐⭐⭐⭐ IMPORTANT. **Role depth:** B/D operate builds; S/C explain provenance and runtime inputs.

### 5.1 Build output is a different thing from source

Java source cannot be sent directly to a normal JVM as your complete production deployment. A build resolves dependencies, compiles source, runs configured checks, and packages output. Maven expresses these steps in a project model (`pom.xml`) and executes plugin goals attached to lifecycle phases. Alternatives include Gradle and other build systems; the important idea is a repeatable dependency-aware transformation.

```text
.java + resources + pom.xml + dependency versions
                     |
             javac via Maven -> tests -> package -> task-api.jar
                                                   |
                                           artifact repository
                                                   |
                                           same bytes deployed
```

| Maven command | Effect | Common misconception |
|---|---|---|
| `./mvnw compile` | Runs lifecycle phases through compilation of main code. | It runs every test. |
| `./mvnw test` | Also compiles/runs configured unit tests, commonly Surefire `*Test`. | It proves connectivity to a real PostgreSQL server. |
| `./mvnw package` | Adds packaging, here a JAR. | It uploads an artifact or deploys the application. |
| `./mvnw verify` | Runs through additional configured verification and integration-test phases. | Integration tests appear automatically with no tests/plugins/environment configured. |
| `./mvnw install` | Also puts the artifact in the **local Maven repository**, often `~/.m2/repository`. | It installs a Linux service or sends anything to customers. |
| `./mvnw deploy` | Also publishes to a configured **remote Maven artifact repository**. | Maven's name means application rollout. |
| `./mvnw clean verify` | Deletes build output via a separate clean lifecycle, then verifies. | It deletes the database. `target/` is rebuildable output only. |

Calling a later phase includes earlier phases of that lifecycle; `compile test package` as three separate commands usually repeats work. The Maven Wrapper (`./mvnw`) downloads/uses the project's chosen Maven distribution, reducing workstation differences; it does not install the JDK. `-B` selects noninteractive batch mode, `--no-transfer-progress` reduces download chatter. See [Maven lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) and [Maven Wrapper](https://maven.apache.org/tools/wrapper/).

### 5.2 Tests, dependencies, and artifact identity

Unit tests isolate small behavior and should run quickly. Integration tests exercise real boundaries such as JDBC against PostgreSQL. If you add tests named `*IT`, configure the Maven Failsafe plugin's `integration-test` and `verify` goals and supply a disposable DB; `verify` is not magic test discovery for arbitrary filenames. Later CI can start a database service or use Testcontainers. Keep unit tests independent of personal workstation credentials.

A JAR is an archive of bytecode/resources. A Spring Boot executable JAR additionally packages dependencies and bootstrap machinery so `java -jar` can start its embedded server. A WAR is a web archive, often deployed to a separate servlet container; it remains useful where that operational model is required. An artifact is any versioned build output—JAR, image, report—not only a container image.

Maven Central is a public dependency/artifact repository. Nexus and Artifactory can host internal artifacts, proxy public dependencies, and enforce policies. A Maven repository organizes coordinates such as `com.example:task-api:0.1.0`; a container registry organizes images and manifests. They solve related storage/distribution problems for different artifact formats. Neither proves the artifact is safe merely by storing it.

Declare direct dependencies, use the Spring Boot dependency-management baseline, inspect transitive dependencies, and avoid arbitrary overrides that break compatibility. `./mvnw dependency:tree` shows the resolved graph; `./mvnw help:effective-pom` shows inherited and local configuration. A `-SNAPSHOT` version is a changing development coordinate; a published release should be immutable. Pin dependencies/plugins and wrapper versions, control repositories, and record the JDK and base image used. Reproducible builds additionally control timestamps and other nondeterministic inputs; Maven supports `project.build.outputTimestamp` for cooperating archive plugins. Reproducibility improves confidence but is distinct from artifact promotion. See [Maven reproducible builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html).

```bash
./mvnw -B --no-transfer-progress clean verify
sha256sum target/task-api.jar
java -jar target/task-api.jar
```

`sha256sum` computes a content checksum. Matching checksums strongly support byte identity; a checksum supplied by the same compromised source is not independent proof of authenticity. Record the commit, build result, JAR checksum, and later image digest. **Build once** means test/package one release artifact, then promote it with different external configuration. Rebuilding separately in staging and production introduces another opportunity for dependencies, timestamps, or toolchains to differ. An image built from the tested JAR adds runtime layers; test/scan that image too, then promote its digest.

### Exercise

**Objective:** inspect the exact deployable artifact. **What to do:** after chapter7 creates the project, run `clean verify`, list the JAR, and record a checksum. **Commands/starter:** `jar tf target/task-api.jar` lists entries (`t` table, `f` file); look for application classes and `BOOT-INF/lib`. **Expected result:** a runnable packaged archive and test report under `target/surefire-reports`. **How to verify:** launch the exact file, make an HTTP request, and distinguish JAR identity from the version printed in `pom.xml`. **Common mistakes:** deploying the `.original` artifact or assuming identical filenames mean identical bytes. **Explanation:** packaging structure and content identity determine what actually runs.

### Checkpoint

1. Why can `mvn verify` pass without contacting PostgreSQL?
2. Is a new production build from the same Git commit necessarily identical to staging?

<details><summary>Answers</summary>

1. Only configured tests/checks execute; the sample starts with unit tests. 2. No; mutable dependencies, image tags, tool versions, and nondeterministic build inputs can differ. Promote the tested artifact.

</details>

### Interview relevance

Backend: lifecycle, test types, dependency resolution, executable JAR. Junior DevOps: wrapper/toolchain, immutable releases, artifact storage, checksum/digest, CI evidence. Common question: “Why is Maven install not deployment?” Without notes, distinguish local dependency publication from starting a runtime process.

**Remember:** build transforms source into evidence-backed bytes; deployment chooses where those bytes run.

## 6. Processes and application configuration

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW. **Role depth:** B/D/S operate; C use and supply infrastructure configuration.

### 6.1 One program, different runtime inputs

Hardcoding a database password in Java requires rebuilding to rotate it and spreads it through source and artifacts. External configuration separates the application from the environment in which it runs. Configuration includes ports and pool sizes; secrets include passwords, tokens, and private keys. Both need validation, but secrets additionally need restricted distribution and rotation.

```text
same task-api.jar + dev settings    -> dev process -> dev database
same task-api.jar + staging settings-> staging process -> staging database
same task-api.jar + prod settings   -> prod process -> production database
```

An environment variable belongs to a process environment inherited at launch. Editing your shell variable does not change an already-running JVM. A configuration file is input read by the application or supervisor according to its own lifecycle. Most simple Spring Boot configuration changes require a restart. Command-line arguments can override settings for a single run, but putting secrets there may expose them in process listings and shell history.

Spring Boot loads common `application.yml`, then applicable profile configuration such as `application-dev.yml` or `application-prod.yml`, with defined precedence among files, environment variables, system properties, and arguments. For ordinary app launch, an environment property can override file configuration and a command-line argument can override it again. `SPRING_PROFILES_ACTIVE=prod` activates a named profile; it does not provision a production environment or implement security. `DB_URL` is this guide's own variable name; its YAML placeholder maps it into Spring's datasource configuration. Alternatively, Spring's standard `SPRING_DATASOURCE_URL` maps directly. Use one clear scheme. See [Spring externalized configuration](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html).

### 6.2 Configuration discipline

| Practice | Why it matters to a Spring service | Boundary |
|---|---|---|
| Store non-secret defaults and templates in Git | Changes can be reviewed; onboarding is repeatable. | Never commit actual `.env`, private keys, or credentials. |
| Validate required values at startup | Fail fast with a clear error instead of failing customers later. | Avoid printing secret values in validation messages. |
| Keep environment parity | Similar DB versions, migrations, runtime images, and relevant topology reduce surprises. | Staging need not have the same scale or copied personal data. |
| Treat app instances as disposable | Replace a failed container instead of hand-editing it. | Database data and user uploads need external durable storage. |
| Send application logs to stdout/stderr | Supervisors and platforms can collect a consistent stream. | Logs must be bounded/retained and scrubbed of secrets. |
| Separate build, release configuration, and run | Promote identical bytes while attaching environment inputs. | Build-time secrets must not leak into image layers. |

These echo useful 12-factor principles: explicit dependencies, external configuration, attached backing services, stateless processes, port binding, disposability, environment parity, logs as streams, and separate build/release/run stages. They are design guidance, not a claim that every workload is stateless or every configuration belongs in an environment variable. Secret files and managed secret retrieval can be better fits; chapter17 compares them.

### Exercise

**Objective:** observe when configuration takes effect. **What to do:** after chapter7, run the JAR using `--server.port=8081`, call8081, then change an exported port variable without restarting. **Commands/starter:** `SERVER_PORT=8080 java -jar target/task-api.jar --server.port=8081` with the required DB variables already exported. **Expected result:** the command-line value wins; later shell changes do not move the listener. **How to verify:** inspect `ss -ltnp` and `curl`. **Common mistakes:** assuming profile names or variable changes hot-reload a running JVM. **Explanation:** Spring resolves configuration in a defined order during process startup.

### Checkpoint

1. Must a DB password change require a new image?
2. Can staging use the production database if the image is identical?

<details><summary>Answers</summary>

1. It should not; rotate the external secret and refresh/restart consumers using the appropriate procedure. 2. Avoid it. Isolated credentials and data prevent tests or failed migrations from affecting customers.

</details>

### Interview relevance

Backend: profiles, precedence, secret handling, stateless behavior. Junior DevOps: launch environment, supervision, parity, immutable replacement. Common question: “The YAML says8080 but the process listens on8081—why?” Without notes, inspect effective sources and launch arguments without dumping secrets.

**Remember:** the running service is artifact + runtime configuration + dependencies + resources.

## 7. Build the running Spring Boot project

**Priority:** ⭐⭐⭐⭐⭐ MUST KNOW. **Role depth:** B implement; D operate build/run contract; S diagnose; C understand dependencies.

### 7.1 A deliberately small application contract

Build the smallest application that makes delivery and failure observable: `GET /api/hello` exercises HTTP without the DB; `POST /tasks` writes a row; `GET /tasks` reads rows. Health probes describe process readiness. This is a learning scaffold, with intentionally simple validation and no authentication, pagination, or complete domain architecture.

Create a Maven project through Spring Initializr or your IDE using Java21 and Boot3.5.16, with Spring Web, JDBC API, PostgreSQL Driver, Actuator, and tests. Keep its generated `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/` files. If the generator no longer offers this teaching baseline, generate a compatible Maven Java project and use the following POM; do not mix Boot4-specific source imports into a Boot3 project. The project coordinates, paths, and JAR name below are the contract used by later chapters.

```text
task-api/
  pom.xml
  mvnw, mvnw.cmd, .mvn/wrapper/...
  .gitignore
  db/schema.sql
  src/main/java/com/example/taskapi/
    TaskApiApplication.java
    HelloController.java
    TaskController.java
  src/main/resources/application.yml
  src/test/java/com/example/taskapi/HelloControllerTest.java
```

`pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
  </parent>
  <groupId>com.example</groupId>
  <artifactId>task-api</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <properties><java.version>21</java.version></properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
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
    <finalName>task-api</finalName>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

Maven reads this during a build. The parent provides compatible dependency/plugin defaults; project coordinates identify our artifact. `java.version` configures the target baseline but still requires a suitable installed JDK. Starters collect related dependencies; the PostgreSQL driver is needed at runtime, and tests are not packaged as runtime dependencies. `finalName` ensures later scripts can refer to `target/task-api.jar`; the Boot plugin repackages it as executable. A wrong parent/dependency coordinate fails resolution; a missing repackage plugin can produce a JAR without the expected executable layout.

`TaskApiApplication.java`:

```java
package com.example.taskapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskApiApplication.class, args);
    }
}
```

`HelloController.java`:

```java
package com.example.taskapi;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from task-api");
    }
}
```

`TaskController.java`:

```java
package com.example.taskapi;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final JdbcTemplate jdbc;

    public TaskController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Task(long id, String title) {}
    public record NewTask(String title) {}

    @GetMapping
    public List<Task> list() {
        return jdbc.query("SELECT id, title FROM tasks ORDER BY id",
                (rs, rowNum) -> new Task(rs.getLong("id"), rs.getString("title")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody NewTask request) {
        if (request.title() == null || request.title().isBlank()
                || request.title().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "title must contain 1 to 200 characters");
        }
        return jdbc.queryForObject(
                "INSERT INTO tasks(title) VALUES (?) RETURNING id, title",
                (rs, rowNum) -> new Task(rs.getLong("id"), rs.getString("title")),
                request.title().strip());
    }
}
```

Spring constructs the application and controllers at startup, injects `JdbcTemplate`, and maps HTTP methods to Java methods. The JDBC calls borrow connections from the datasource pool. The SQL parameter (`?`) binds data separately from SQL structure; never build SQL by concatenating user-supplied titles. PostgreSQL's `RETURNING` returns the inserted row. Keeping SQL in the controller is a **local teaching shortcut**; move it into a repository/service layer as you learn design and transaction boundaries.

`src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: task-api
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/taskdb}
    username: ${DB_USERNAME:taskapp}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
      connection-timeout: 3000
  lifecycle:
    timeout-per-shutdown-phase: 20s
server:
  port: 8080
  shutdown: graceful
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState,db
```

Spring reads this on startup. `${NAME:default}` uses an environment/config value or a fallback; the password deliberately has **no fallback** so missing configuration is visible. Hikari caps the pool at five connections and bounds pool acquisition waiting to3000ms; it does not establish a JDBC query timeout. Graceful shutdown allows active requests time to finish. Exposure lists HTTP-accessible management endpoints; avoid enabling sensitive `env`, `heapdump`, or administrative endpoints publicly.

The readiness group includes DB connectivity because this application's useful work depends on PostgreSQL; it is a policy choice. Liveness should not include a shared DB outage: restarting every app instance would amplify it. A readiness failure removes eligibility for traffic only when a router/platform actually uses that signal. A DB health check does not prove that the `tasks` table exists, your SQL succeeds, or access control is correct. See [Spring Boot health probes](https://docs.spring.io/spring-boot/3.5/reference/actuator/endpoints.html#actuator.endpoints.kubernetes-probes).

`application-dev.yml` can contain a non-secret development log level, for example `logging.level.com.example.taskapi: DEBUG`. `application-prod.yml` can tighten non-secret logging defaults; don't embed production credentials. Activate a profile at launch, not by changing the artifact. The optional Compose profile is introduced in chapter9.

### 7.2 Establish a database without skipping the prerequisites

For this pre-container exercise use a locally installed PostgreSQL server. On an Ubuntu lab VM, `sudo apt install postgresql postgresql-client` installs the distribution-supported version; PostgreSQL16+ supports this example. Check `psql --version` and service status. Chapter8 introduces a fresh PostgreSQL17 container; this is a new database instance, so data does not move there automatically.

On an Ubuntu installation using the usual local postgres administrator account:

```bash
sudo -u postgres createuser --pwprompt taskapp
sudo -u postgres createdb --owner=taskapp taskdb
```

`sudo -u postgres` runs the administration command as the PostgreSQL OS account. `createuser --pwprompt` creates a login role and prompts for its password; use a lab-only value, represented throughout as `replace-me`. Do not grant the application superuser rights. `createdb --owner` creates an isolated database owned by that role. Other installations may use a different administrator connection method; use the server's authenticated administrator, not a made-up default password. These commands create resources and should be run once in the lab, not on an arbitrary production instance.

`db/schema.sql`:

```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL
);
```

```bash
psql -h 127.0.0.1 -p 5432 -U taskapp -d taskdb -W -v ON_ERROR_STOP=1 -f db/schema.sql
export DB_URL='jdbc:postgresql://127.0.0.1:5432/taskdb'
export DB_USERNAME='taskapp'
read -r -s -p 'Lab database password: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
```

`psql` is a DB client; `-h/-p/-U/-d` select host, port, user, and database. `-W` prompts for the password; `-v ON_ERROR_STOP=1` stops the script on SQL error; `-f` executes a file. Bash `read -r -s` reads the password without interpreting backslashes or echoing input, then `export` makes it available to the child JVM. It still exists in the process environment, so this is not equivalent to a hardened secret manager. Do not put real values in screenshots or Git. Manual schema creation is a bootstrap lab only: chapter21 moves schema changes into migrations. Running this SQL twice errors rather than silently pretending a mismatched existing table is correct.

### 7.3 Verify code separately from infrastructure

`HelloControllerTest.java`:

```java
package com.example.taskapi;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HelloControllerTest {
    @Test
    void helloReturnsStableJsonContract() throws Exception {
        MockMvcBuilders.standaloneSetup(new HelloController()).build()
                .perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello from task-api"));
    }
}
```

This test verifies an observable HTTP mapping and JSON contract without starting the full Spring context or a DB. Remove a generator-created `@SpringBootTest contextLoads` test until you explicitly supply its DB environment, or keep it only with a correctly configured integration setup. That choice must remain visible: the standalone test does **not** verify JDBC, packaging, migrations, network reachability, or production security.

```bash
chmod +x mvnw
./mvnw -B --no-transfer-progress clean verify
java -jar target/task-api.jar
```

In another terminal:

```bash
curl --fail http://127.0.0.1:8080/api/hello
curl --fail -i -H 'Content-Type: application/json' \
  -d '{"title":"Learn to restore a database"}' http://127.0.0.1:8080/tasks
curl --fail http://127.0.0.1:8080/tasks
curl --fail http://127.0.0.1:8080/actuator/health/readiness
```

`-H` sets the JSON content type; `-d` supplies a body and makes curl use POST unless told otherwise; `-i` includes the201 response status. Run one POST, record its ID, and find that exact row in GET. A repeated POST creates another task: this endpoint is not idempotent. Ctrl+C stops the foreground JVM gracefully; it should not delete the DB row. `unset DB_PASSWORD` removes the variable from the current shell when finished; it cannot revoke credentials already copied elsewhere.

### Exercise

**Objective:** establish the cumulative project and distinguish readiness from functionality. **What to build/do:** create the files, provision the lab DB, build, launch, and create a task. **Commands/starter:** use the preceding blocks; then stop/restart only the JVM. **Expected result:** the hello response is stable, POST returns201, GET returns the saved row, and readiness reports UP. **How to verify:** the recorded task ID/title survive the restart; test report records the standalone test. **Common mistakes:** wrong DB password, forgetting schema creation, missing executable wrapper permission, port8080 already occupied, or assuming a green unit test means a reachable database. **Explanation:** you now have separate evidence for code behavior, process startup, HTTP routing, and durable persistence.

### Checkpoint

1. The table is missing but readiness reports UP. Is that impossible?
2. The JAR works only when launched from your IDE. Which inputs might be implicit?
3. A task disappears when the JVM restarts. What storage assumption should you investigate?

<details><summary>Answers</summary>

1. No. A connectivity health check can succeed while application SQL fails; add a meaningful smoke/integration test. 2. JDK, environment variables, working directory, profile, classpath, or a different running DB. 3. Confirm you wrote to PostgreSQL and reconnect to the same instance/database; process memory is not durable storage.

</details>

### Interview relevance

Backend: demonstrate HTTP → controller → JdbcTemplate → Hikari → PostgreSQL and test each boundary. Junior DevOps: build/run the JAR without the IDE and identify required inputs, health endpoints, logs, and stop behavior. Common question: “What evidence proves the deployment is useful?” Without notes, distinguish unit tests, readiness, and a successful persisted business operation.

**Remember:** one runnable service with understood failure modes is the foundation for every platform that follows.

