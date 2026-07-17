---
name: mcp-server-dev
description: Use when adding tools to, modifying, or debugging Projektor's MCP server (server/server-app/src/main/kotlin/projektor/mcp/, served at /mcp via the Ktor mcpStatelessStreamableHttp helper). Covers the ContentNegotiation/Jackson footgun that silently breaks MCP JSON-RPC responses (wrong field casing, explicit nulls, wrong enum values) whenever a new SDK type is touched, how to verify a response is actually spec-compliant instead of just checking HTTP 200, the OAuth well-known discovery routes, and local/live testing commands.
user-invocable: true
---

# Developing Projektor's MCP server

The MCP server lives in `server/server-app/src/main/kotlin/projektor/mcp/`, built on
Ktor + the official `io.modelcontextprotocol:kotlin-sdk` (`mcpStatelessStreamableHttp`,
registered in `Application.kt` at `mcpStatelessStreamableHttp(path = "/mcp", ...)`). It's
deployed at `https://live.projektor.dev/mcp`.

Key files:
- `ProjektorMcpServer.kt` — `buildProjektorMcpServer(...)` builds the `Server` and registers
  tools via `server.addTool(name, description, inputSchema) { request -> ... }`.
- `McpJacksonCompat.kt` — **read this before touching anything else in this directory.** It's
  the fix for the ContentNegotiation footgun below; new SDK types you introduce (new tool
  result shapes, new content blocks) can silently fall outside its coverage.
- `../route/McpOAuthDiscoveryRoute.kt` — serves `/.well-known/oauth-protected-resource` and
  `/.well-known/oauth-authorization-server` with a small JSON body instead of Ktor's default
  empty 404 body (see "OAuth discovery routes" below).
- `../test/kotlin/projektor/mcp/McpPullRequestFailureContextApplicationTest.kt` — test patterns.

## The ContentNegotiation footgun (read this first)

The app's Ktor `ContentNegotiation` plugin (`Application.kt`) is configured for the REST API:
Jackson, with `propertyNamingStrategy = SNAKE_CASE`. Ktor's `ContentNegotiation` picks the
**first registered converter whose `serialize()` call succeeds** — and Jackson, being
reflection-based, "succeeds" for literally any object, including the MCP SDK's own
`@Serializable` (kotlinx) response types. So even though the SDK's `Server` machinery hands
Ktor properly-typed objects meant for its own `McpJson` (kotlinx) converter, **Jackson always
wins first** and reflects over them using the REST API's conventions instead:

- Kotlin property names get SNAKE_CASE'd (`protocolVersion` → `protocol_version`)
- Absent optional fields serialize as explicit `null` instead of being omitted
  (kotlinx's `McpJson` uses `explicitNulls = false`)
- `@SerialName`-overridden fields (`_meta`, `$schema`, `$defs` — see `WithMeta`, `ToolSchema`
  in the SDK's `io.modelcontextprotocol.kotlin.sdk.types` package) get output under their raw
  Kotlin property name instead, since Jackson doesn't understand kotlinx's `@SerialName`
  annotation at all
- Enums with a `@SerialName` (e.g. `ContentTypes.TEXT` → `"text"`) serialize as the Kotlin
  enum constant name instead (`"TEXT"`), for the same reason

This is spec-incompatible JSON-RPC and breaks real clients doing schema validation (e.g. the
official TS SDK's zod-validated client, or Claude Code's `claude mcp` connection) even though
casual testing (`curl` + eyeballing, or a test that only checks HTTP 200 / substring-contains)
looks fine.

**Two fixes were tried and empirically broke the REST API — don't repeat them:**
1. Reordering `ContentNegotiation` so `json(McpJson)` is registered before `jackson {}`. Broke
   139/299 tests: kotlinx's converter *throws* (`SerializationException`) instead of gracefully
   falling through for the REST API's non-`@Serializable` DTOs, so nothing falls back to
   Jackson for the rest of the app.
2. Installing a second, `/mcp`-scoped `ContentNegotiation` instance. Ktor allows this
   syntactically, but empirically it did **not** stay scoped to `/mcp` — it broke unrelated REST
   endpoints too (`SerializationException: Serializer for class 'TestCase' is not found`).

**The actual fix (`McpJacksonCompat.kt`):** make Jackson itself render the SDK's types
correctly, via a package-scoped `ClassIntrospector.MixInResolver` keyed on
`io.modelcontextprotocol.kotlin.sdk.types` — every class in that package gets a generic
camelCase + null-omitting mixin, with a few classes/kinds getting more specific treatment:
- `WithMeta` → `@JsonProperty("_meta")` override
- `ToolSchema` → `@JsonProperty("$schema")` / `@JsonProperty("$defs")` overrides
- any `enum` in the package → a generic `JsonSerializer` that reads `@SerialName` off the
  matched enum constant via reflection, falling back to `.name` if absent

Because it's resolved by **package**, not by an explicit list of classes, it automatically
covers new SDK types you introduce — new tool result shapes, new content blocks, new enums —
*as long as they're SDK types*. It does **not** automatically cover:
- A brand-new `@SerialName`-style deviation from plain camelCase that isn't `_meta` or
  `$schema`/`$defs` — check the SDK source (see "Reading the SDK source" below) for any new
  `@SerialName` on a property (not an enum constant) before assuming it'll Just Work.
- Anything serialized through `ProjektorMcpServer.kt`'s own `objectMapper` (used for embedding
  arbitrary domain JSON as a `TextContent` string body, e.g. the failing-test-context payload)
  — that's deliberately still SNAKE_CASE, since it's app data, not MCP protocol envelope.

## Verifying a response is actually spec-compliant

**HTTP 200 and eyeballing the JSON body is not enough** — the field-casing/null/enum bugs
above all returned 200 with plausible-looking JSON. Two reliable ways to actually validate:

1. **Grep the raw body for the specific footguns**, in a test or via `curl`:
   ```bash
   curl -s https://live.projektor.dev/mcp -X POST \
     -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
     -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
   ```
   Check for: camelCase keys (`protocolVersion`, `serverInfo`, not `_snake_case`), no bare
   `:null` for fields you expect to be omitted, and any enum-backed field (e.g. content
   `"type"`) matches the SDK's declared `@SerialName`, not the Kotlin constant name.

2. **Drive it with a real MCP client SDK for actual schema validation** — this is what caught
   the original bug (a raw curl response looked fine; a real client's zod validation didn't).
   From any scratch directory:
   ```bash
   npm install @modelcontextprotocol/sdk
   ```
   ```js
   // test.mjs
   import { Client } from "@modelcontextprotocol/sdk/client/index.js";
   import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
   const client = new Client({ name: "diagnostic-client", version: "1.0.0" });
   await client.connect(new StreamableHTTPClientTransport(new URL("https://live.projektor.dev/mcp")));
   console.log(await client.listTools());
   ```
   A `$ZodError` here is the real signal — it names the exact field/type mismatch.

3. **Add a test asserting on the literal substrings**, not just presence of expected data —
   see `McpPullRequestFailureContextApplicationTest.kt`'s
   `` `initialize response uses spec-compliant camelCase field names with no explicit nulls` ``
   test for the pattern: assert the camelCase key IS present, the snake_case key is NOT, and
   `:null` doesn't appear anywhere in the body.

## Adding a new tool

Add via `server.addTool(name, description, inputSchema) { request -> ... }` in
`buildProjektorMcpServer()`. After adding:
- If the tool returns a new/different result shape than `CallToolResult(content =
  listOf(TextContent(...)))`, re-check the ContentNegotiation footgun section above against
  whatever new SDK types are now in play.
- Add a test in the `projektor.mcp` test package following the existing pattern — call
  `/mcp` with a real `tools/call` body via the Ktor test client, don't just unit-test the
  handler function in isolation (the bugs above only manifest through the real HTTP
  serialization path).

## OAuth discovery routes

`McpOAuthDiscoveryRoute.kt` serves `/.well-known/oauth-protected-resource` (and
`/mcp` variant) and `/.well-known/oauth-authorization-server` with a `404` status but a
small JSON body (`{"error": "invalid_request", "error_description": "..."}`) instead of
Ktor's default bare/empty 404. This server doesn't use OAuth — these routes exist purely
because some MCP clients (including the Claude Code CLI, confirmed via decompiling its
bundled binary) probe them before connecting and crash trying to `JSON.parse()` an empty
body, aborting the connection entirely. Leave these in place even though the server has no
real OAuth flow; removing them reintroduces the crash for any client that does this
preflight probe.

## Local dev / testing commands

| Goal | Command |
|---|---|
| Compile | `./gradlew :server:server-app:compileKotlin` |
| Run MCP tests only | `./gradlew :server:server-app:test --tests "projektor.mcp.*"` |
| Run full server-app suite (do this before considering any MCP change done — see footgun section, changes here have broken unrelated REST tests twice) | `./gradlew :server:server-app:test` |
| Lint | `./gradlew :server:server-app:ktlintCheck` |
| Check live deployment status via Claude Code | `claude mcp get projektor-mcp` (needs the server approved in `.mcp.json` / `enabledMcpjsonServers` first — see `claude mcp list`) |

## Reading the SDK source when something looks wrong

The SDK ships proper Kotlin sources on Maven — pull them down rather than guessing from
memory when you need to check a type's exact `@SerialName`s or a helper's actual behavior:
```bash
find ~/.gradle -iname "kotlin-sdk-core-jvm-*-sources.jar" -o -iname "kotlin-sdk-server-jvm-*-sources.jar"
unzip -o -q <jar> -d /tmp/mcp-sdk-src
```
Types live under `commonMain/io/modelcontextprotocol/kotlin/sdk/types/`; the Ktor server glue
(`mcpStatelessStreamableHttp`, `installMcpContentNegotiation`) lives under
`commonMain/io/modelcontextprotocol/kotlin/sdk/server/`.
