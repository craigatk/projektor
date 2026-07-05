---
name: cypress-debug
description: Use when running, reproducing, or debugging Cypress e2e test failures in this repo (ui/cypress) — including CI-reported failures pasted from a build log, flaky/intermittent failures, click handlers that silently do nothing, tooltip/graph assertions after a UI-library upgrade, or cy.intercept not behaving as expected. Covers how to start the dev server and run specs here, how to pull data out of the browser into terminal output, hit-testing a stuck click, and the two cy.intercept footguns (StaticResponse property names, substring URL matching) that have caused real failures in this codebase.
user-invocable: true
---

# Cypress debugging in this repo

All commands below assume `cwd` is `ui/` (that's where `package.json`, `cypress.config.ts`,
and `cypress/e2e/` live — there is no `baseUrl` configured, so specs `cy.visit()` full URLs
like `http://localhost:1234/...`).

## Running tests

| Goal | Command |
|---|---|
| Start the dev server (needed before any headless run) | `(yarn start > /tmp/vite-start.log 2>&1 &)` then wait ~3s and `curl -s -o /dev/null -w "%{http_code}\n" http://localhost:1234/` until `200` |
| Run one spec headless | `npx cypress run --spec cypress/e2e/<file>.cy.js --browser electron` |
| Run several specs headless | `npx cypress run --spec "cypress/e2e/a.cy.js,cypress/e2e/b.cy.js" --browser electron` |
| Run the whole suite (~90s+) | `npx cypress run --browser electron` — run this via Bash `run_in_background: true` (or as a background Agent call) and let the notification land; don't poll |
| Everything in one shot incl. server lifecycle | `yarn cy:test` (uses `start-server-and-test`) |
| Jest unit tests for a touched area | `yarn jest src/Repository --silent` (or no path for the full suite) |
| Stop the dev server when done | `pkill -f "vite serve"` |

Use `--browser electron` explicitly — it's the one guaranteed available headless in this
environment. Cypress auto-writes a screenshot on every failing test to
`cypress/screenshots/<spec>.cy.js/<test name> (failed).png` — **read it with the Read tool
first**, before writing any debug code. It's usually the fastest way to see what actually
rendered, and often makes the rest of this skill unnecessary.

Clean up `cypress/screenshots/` before finishing (it's gitignored noise, but stale
screenshots from earlier debug runs make it hard to tell which failure is current).

## Reproducing a CI-reported failure

1. Copy the exact spec file + test title from the CI error text.
2. Run that spec **in isolation** first (`--spec` to just that file) — order-dependent
   pollution from other specs is a real source of noise, best ruled out early.
3. If you suspect cross-test interference within the same spec, add `it.only(...)` around
   the failing test to isolate it completely. **Remove `.only` before finishing** — grep the
   diff for `.only(` before considering the work done.
4. Before assuming your own recent change caused a failure, check the baseline:
   `git stash`, rerun the same spec, compare, then `git stash pop`. This has repeatedly been
   the fastest way to tell "pre-existing flake, not my problem" from "I broke this."

## Getting data out of the browser and into your terminal

`cy.log()` and `console.log()` inside the app or a `.then()` callback do **not** show up in
`cypress run`'s stdout — they only land in the (usually unread) command log / video. The
reliable trick used repeatedly in this repo:

```js
cy.window().then((win) => {
  const info = { /* whatever you need to inspect */ };
  throw new Error("DEBUG " + JSON.stringify(info, null, 2));
});
```

The thrown error's message is captured by the reporter and printed in the normal failure
output — grep for your marker string. For structured extraction, run with
`--reporter json 2>err.log 1>out.log` and `grep -m1 "DEBUG" out.log`.

To inspect React component props/state that never reach the DOM (e.g. a Tooltip's
`props.payload`), temporarily stash them on `window` from inside the component:

```tsx
(window as any).__lastTooltipProps = props;
```

then read `win.__lastTooltipProps` from a `cy.window().then()` using the trick above.
**Always remove this instrumentation before finishing** — grep the diff for `window.__`
before calling the work done; it should never survive into a commit.

For one-off investigations, write throwaway spec files (e.g.
`cypress/e2e/_debug_<thing>.cy.js`) rather than editing the real spec — **delete them**
before finishing (`rm cypress/e2e/_debug_*.cy.js`), they are not test coverage.

## A click silently does nothing (no error, handler never fires)

This is almost always a hit-testing mismatch, not a missing/broken handler. Diagnose with
a capturing listener on `window` for `mousedown`, `mouseup`, and `click`, logging
`e.target`:

```js
cy.window().then((win) => {
  win.__clicks = [];
  ["mousedown", "mouseup", "click"].forEach((type) => {
    win.addEventListener(
      type,
      (e) => win.__clicks.push({ type, target: e.target.outerHTML?.slice(0, 80) }),
      true,
    );
  });
});
```

If `mousedown`'s target differs from `mouseup`'s target, the browser fires `click` on their
**nearest common ancestor** — which may have no click handler at all, silently swallowing
the interaction. This happens when the app re-renders and mounts a new overlapping element
(e.g. a hover-triggered "active" state) *between* the mousedown and mouseup of the same
click gesture. `document.elementsFromPoint(x, y)` (not `elementFromPoint`) shows the full
stack of elements at a point if you need to see what's overlapping and in what order.

`.click({ force: true })` bypasses Cypress's actionability/covering checks and dispatches
directly to the queried element, sidestepping the browser's real hit-test. Useful as a
**diagnostic** — if `{force: true}` succeeds where a normal click doesn't, you've confirmed
a hit-testing/covering issue rather than a missing handler — but don't leave it as the
shipped fix if the underlying interaction (e.g. an `onClick` on a chart point) is genuinely
racy; fix the interaction instead (see the recharts note below for a concrete example).

## When behavior changed after a dependency bump (e.g. recharts)

Don't guess from changelogs or memory — read the installed package's actual source. It's
usually more informative and faster than searching upstream docs:

```bash
grep -rn "<keyword>" node_modules/<package>/es6/
```

This repo hit exactly this with recharts v3: tooltip data was resolving to the wrong row
when two points shared an identical x-axis label (traced to
`combineTooltipPayload.js`'s `findEntryInArray` doing a value-based lookup instead of a
positional one), and chart-point clicks silently stopped navigating because recharts now
mounts a separate "active dot" overlay on hover, causing the mousedown/mouseup mismatch
described above (fixed by switching the dot's handler from `onClick` to `onMouseUp`, which
reliably lands on whichever circle is topmost at release time).

## `cy.intercept` footguns (both have caused real failures here)

1. **Wrong StaticResponse property names fail silently.** The stub object needs
   `statusCode` and `body` — **not** `status` and `response`. `cy.intercept("GET", url, {
   status: 404, response: {} })` is not an error, but Cypress can't construct a fake
   response from it and lets the **real** request through unmocked. If a "should show an
   error" test unexpectedly renders real/success data, check this first before suspecting
   the app code.

2. **String URL patterns match by prefix/substring, not just exact path**, and intercepts
   are checked most-recently-registered-first. `cy.intercept("GET", "run/123", ...)` can
   also catch a request to `run/123/summary` if that intercept was registered later. If you
   need a mock to apply to exactly one path and not its sub-paths, anchor it with a regex:
   `cy.intercept("GET", new RegExp("run/123$"), ...)`.

3. **A test can be missing a shared setup helper.** If a component tree has a gating
   fetch that other tests mock via a shared helper (e.g. this repo's
   `cy.interceptTestRunBasicRequests(publicId)`, which stubs `results/{id}/status`), a test
   that skips that helper will hit the real backend for that one endpoint and hang/loop
   behind a "still processing" style gate, timing out on an assertion that looks unrelated.
   When a test fails but its siblings in the same file pass, diff their setup blocks first.

## Verifying a fix is real, not timing-masked

- A singular `findByTestId(...)` query can pass even when the DOM will end up with **two**
  matching elements, if it resolves before the second one mounts — this reproduces
  reliably in CI (slower, different timing) but not locally. When in doubt, temporarily
  add `cy.wait(500)` then `cy.findAllByTestId(...).should("have.length", 1)` to positively
  pin down the DOM shape, confirm it, then remove the temporary assertion (keep the
  underlying fix).
- Run the fixed spec **3–5 times in a row** before trusting it, especially for anything
  touching hover/animation/click timing.
- Run the **full suite**, not just the spec you touched, before calling it done — a fix to
  shared code (e.g. a `Dot` component reused by three different charts) can affect specs
  you didn't otherwise touch. Also rerun `yarn jest --silent` if any `.tsx`/`.ts` source
  (not just a `.cy.js` spec) changed.

## Cleanup checklist before finishing

- [ ] No `.only`/`.skip` left in any spec (grep the diff)
- [ ] No stray `window.__debug*` / temporary `console.log` in app source (grep the diff)
- [ ] Throwaway `cypress/e2e/_debug_*.cy.js` files deleted
- [ ] `cypress/screenshots/` removed
- [ ] Background dev server killed (`pkill -f "vite serve"`)
