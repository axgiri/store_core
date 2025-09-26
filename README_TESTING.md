Load / Functional Tests

What this provides
- `LoadFunctionalTest` - a JUnit test under `src/test/java/...` that seeds the DB using existing `SeederService` and issues jittered, weighted requests to non-seeder controllers to simulate real usage and collect simple memory snapshots.
- `TestSecurityConfig` - test configuration that disables HTTP security and provides a permissive `AccessControlService` so `@PreAuthorize` checks succeed.


How to run (locally)
1. Build & run tests with maven (this will execute the load test):

```bash
./mvnw -DskipTests=false test -Dtest=LoadFunctionalTest
```

2. To run only this test from an IDE, run the JUnit test `LoadFunctionalTest`.

Dynamic discovery
- `LoadFunctionalTest` now uses Spring's `RequestMappingHandlerMapping` to discover controller routes at test startup and automatically exercises them (excluding `/api/v1/seeder` and `/actuator`/`/management`). Path variables like `{id}` are replaced with sample values (`1`) so the routes can be invoked.

Tuning parameters
- You can change the hardcoded defaults in `LoadFunctionalTest` directly, or set system properties and recompile if you prefer. Main parameters to tune in the test source:
	- `seederService.seedAll(5)` — change `5` to seed more/less data.
	- `threads` — number of concurrent worker threads (default 8).
	- `durationSeconds` — test run duration in seconds (default 20).

Tips for collecting memory metrics
- The test uses a lightweight in-process measurement: `Runtime.totalMemory() - Runtime.freeMemory()` and logs values periodically. For more accurate heap measurements or to collect heap dumps:
	- Run the JVM with `-Xms`/`-Xmx` limits to set a stable baseline.
	- Use `jmap -heap <pid>` or `jcmd <pid> GC.heap_info` during or after the run.
	- To capture a heap dump at test end: `jmap -dump:live,format=b,file=heap.hprof <pid>`.

Safety & notes
- `TestSecurityConfig` disables security for the test context only. Do not copy it to production code.
- The automatic discovery fills simple JSON bodies (`{}`) for POST/PUT endpoints. For common endpoints the test now constructs more realistic payloads (signup/login/review/report) and — importantly — after seeding it picks real entity IDs (first person/shop/product/review) from the DB and uses them in URL path and payloads to reduce 404s and increase realistic coverage.

