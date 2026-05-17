# Term 2 — Lab Review

**Reviewer:** Anton Lukashin  
**Date:** 2026-05-06  
**Tasks reviewed:** Lab 1 (JSON Parser), Lab 2 (HTTP Server), Lab 3 (Load Testing Report)  
**Total branches reviewed:** 44 (43 local + 1 remote-only)  

---

## Summary Table

| Branch | Lab 1 /10 | Lab 2 /10 | Lab 3 /10 | Total /30 |

| 30101_Kartsev-Sergey | 6 | 7 | 7 | **20** |



-
---

## Detailed Reviews — Group 30101

---

---

## 30101_Kartsev-Sergey

### Lab 1 — JSON Parser
**Score: 6/10**
- Public API: `toJson`, `fromJson`, `fromJsonAsMap`, `fromJson(String, Class<T>)` — meets the core requirement.
- Handles primitives, boxed types, null, arrays, collections, maps, and arbitrary classes via reflection.
- Critical bug in `arrayToJson`: closes with `}` instead of `]`, then does a hack replacement (`replace("]","").replace("}","]")`) — fragile and wrong for nested structures.
- `objectToJson` silently skips null fields instead of serializing them as `"null"` — breaks round-trip for nullable fields.
- `parseString` does not handle `\n`, `\t`, `\r` on deserialization.
- No modern Java features used: no records, no switch expressions, no text blocks, no `var`, no streams.
- `@SuppressWarnings("unchecked")` used broadly without justification.

### Lab 2 — HTTP Server
**Score: 7/10**
- Uses `ServerSocketChannel` (NIO) correctly. Virtual/platform thread switching via `isVirtual` implemented.
- Supports all required HTTP methods; headers returned as `Map<String, String>`.
- Multipart form-data parsing implemented.
- Fixed-size `ByteBuffer.allocate(8192)` — body truncated for requests larger than 8 KB.
- No `stop()` method — the server cannot be shut down gracefully.
- No query parameter parsing.
- No modern Java features; Russian-language comments in production code.

### Lab 3 — Load Testing
**Score: 7/10**
- README in Russian; contains all required sections: how to run, experiment description, hardware, parameters, results table.
- All 4 combinations tested for 2 request types (I/O and CPU bound).
- `TestServer.java` uses own `HttpServer` and both parsers with a flag switcher.
- `LoadTester.java` uses raw sockets — measures end-to-end latency.
- I/O bound task uses file write+read (synchronized); CPU bound uses a loop sum.
- `LoadTester` hard-codes the path in a comment for switching endpoints — not a proper configuration mechanism.
- No p95 latency; no throughput numbers; no analysis/conclusions in README.

**Overall: 20/30**
**Summary:** Kartsev demonstrates functional implementations across all three labs with core requirements met, but the code has notable bugs (array serialization), lacks modern Java idioms entirely, has no graceful shutdown in the HTTP server, and the load test lacks CLI configurability.

---
## General Observations

### What was done well across submissions
- Most students who submitted work correctly used `ServerSocketChannel` for the HTTP server (Lab 2).
- Several students applied modern Java features: pattern matching `instanceof`, switch expressions, records, text blocks.
- The best submissions (Jankowski, Popov, Krutikov) demonstrate professional library design with proper encapsulation, immutable models, and clear public APIs.

### Common deficiencies

**Lab 1 — JSON Parser:**
- Missing or incomplete string escape sequence handling (especially `\uXXXX`, `\n`, `\t`) was the most common issue.
- Hardcoded string concatenation in loops (`word += c`) instead of `StringBuilder` — Java 1.4 anti-pattern.
- No traversal of the superclass chain during field reflection — inherited fields ignored.
- `public` exposure of internal implementation classes.

**Lab 2 — HTTP Server:**
- Mixing NIO `ServerSocketChannel` with legacy `socket().getInputStream()` — defeats the purpose of NIO.
- Missing `isVirtual` parameter or ignoring the thread count when virtual threads are enabled.
- No graceful `stop()` method.
- `Content-Length` set to character count instead of byte count.
- No 405 Method Not Allowed responses.

**Lab 3 — Load Testing:**
- Hardcoded configuration constants requiring recompilation instead of CLI arguments.
- Absent or empty README files.
- No warmup phase before measurements.
- No analysis/discussion of results — surprising patterns (e.g. virtual threads slower than classic) left unexplained.
- Sequential load test instead of concurrent (tests throughput, not latency under load).
- `GsonParser` wrapping own parser instead of real Gson (invalidates comparison).

---

## Academic Integrity Analysis

### LLM / AI-Assisted Code

The following submissions show strong or moderate indicators of AI tool usage or direct copying from established library source code. None of this is conclusive proof by itself, but the combination of indicators warrants a conversation with each student.

---

#### 30101_Jankowski-Erik — **High probability**

**Evidence:**

The JSON library (`JsonMapper`) is a near-exact clone of the **Gson** public API:

| Feature in submission | Gson equivalent |
|---|---|
| `TypeAdapter<T>` interface with `write(T, JsonWriter)` / `read(Object, Type)` | `com.google.gson.TypeAdapter<T>` |
| `TypeReference<T>` capturing generic type via `getClass().getGenericSuperclass()` | `com.google.gson.reflect.TypeToken<T>` |
| `FieldNamingStrategy` functional interface | `com.google.gson.FieldNamingStrategy` |
| `FieldNamingStrategies.SNAKE_CASE` using regex `"([a-z])([A-Z])"` → `"$1_$2"` | Gson's internal snake-case converter uses the identical regex |
| `ClassInfo` with `ConcurrentHashMap<Class<?>, ClassInfo>` reflection cache | Gson's internal `ReflectionAdapterFactory` uses the same caching pattern |

The probability of independently designing this exact API surface — including the identical snake_case regex — is essentially zero. The student either used an LLM prompted with "write a JSON library like Gson" or directly referenced Gson's source code.

---

#### 30102_Krutikov-Daniil — **High probability**

**Evidence:**

The code exhibits professional patterns not typically acquired through a university Java course:

- `JsonConfig` builder with `failOnUnknownProperties` and `detectCycles` — these are **Jackson `DeserializationFeature`** flags by name and function.
- `BigDecimal` used for JSON number parsing — an advanced detail that mirrors Jackson's handling.
- `Semaphore(threadCount)` to rate-limit virtual thread concurrency — a sophisticated production pattern not covered in introductory Java.
- `AtomicBoolean.compareAndSet(false, true)` for server lifecycle — `compareAndSet` is rarely taught at student level.
- `AsynchronousCloseException` caught explicitly in the accept loop — a specific Java NIO edge case.
- **Only 3 commits for Labs 1+2** (Lab 1 on Feb 26, Lab 2 on Mar 23+28) — extremely sparse history suggesting bulk upload of pre-written code.
- Zero obvious student mistakes anywhere in 20+ files — an unusual uniformity of correctness.

---

#### 30101_Popov-Ilya — **Medium probability**

**Evidence:**

- Lab 1 committed as a **single upload at 02:19 AM** (764 lines across 9 files in one commit).
- Labs 2+3 committed in **37 seconds** (18:38:37 and 18:39:14 on the same day — 1310 and 571 insertions respectively). This indicates bulk upload of locally pre-written files; the development time is invisible.
- Consistent use of advanced features across all files: `switch` expressions, pattern matching `instanceof`, p95 percentile computation, `PRAGMA journal_mode=WAL` for SQLite, CLI argument parsing, Gradle fat JAR — all at the same quality level with no weak spots.
- The `JsonTokenizer` is the most distinctive part (a proper lexer stage) and is genuinely Java-idiomatic, which reduces suspicion slightly.
- The Javadoc on `Json.java` uses `{@code ...}` and `{@link ...}` inline tags with `<pre>` code blocks — consistently polished documentation rarely seen in student submissions.

Unlike Jankowski, Popov's API does not clone a specific existing library, and the code has a few genuine choices (like the `JsonToken` class being a `record`). Could be a very capable student who uses LLM for polish.

---

#### 30101_Vasyuk-Marina — **Low-medium probability**

**Evidence:**

- `JsonConfig` with `prettyPrint`, `ignoreUnknownFields`, `includeNulls` is inspired by popular library APIs.
- Full `\uXXXX` unicode escape parsing and switch expressions are used correctly — above average student Java knowledge.
- However, the code has genuine student mistakes: per-request `Json` instantiation, non-thread-safe `JsonParser`, missing results analysis in README. These "imperfections" suggest the student wrote the code themselves, possibly guided by documentation or an LLM for specific features rather than having the whole thing generated.

---

