---
name: update-docs
description: Use when adding or changing user-facing documentation for Projektor — a new feature page, a new server environment variable, a new Gradle plugin or Node script config param, or a publisher changelog entry. The docs are NOT in this repo; they live in the sibling repo ../projektor-doc (Hugo site deployed to projektor.dev). Covers where each kind of change goes, the page front-matter and sidebar-ordering conventions, which pages need to change together, taking config names/defaults from the code, and how to build-check the site.
user-invocable: true
---

# Updating the Projektor docs

The docs site at https://projektor.dev is a separate repo checked out next to this one:

```
../projektor-doc        (git@github.com:craigatk/projektor-doc.git)
```

It's a [Hugo](https://gohugo.io/) site using the Whisper theme, deployed by Netlify from its `main` branch.
Doc changes are a separate branch/PR in that repo — never add user docs to this repo's READMEs instead.

## Where things go

| Change                                           | Where in `projektor-doc`                                                                                     |
|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| New user-facing feature                          | New page `content/docs/<slug>/index.md`                                                                       |
| New/changed server environment variable          | `content/docs/server/index.md` — the section for that feature (add a `##` section if the feature is new)     |
| New/changed Gradle plugin config param           | `content/docs/gradle-plugin/index.md` — "All configuration options" table, plus a section if it needs explaining |
| New/changed Node script config param             | `content/docs/node-script/index.md` — "All configuration options" table                                      |
| Gradle plugin / Node script release              | The `## Changelog` list at the bottom of that publisher's page (newest version first)                        |
| HTTP API endpoint                                | `content/docs/api/index.md`                                                                                   |
| Images                                           | `static/images/<slug>/…`, referenced as `/images/<slug>/file.png`                                             |

The pages to change together: a feature with server config gets **both** its own page (full setup, with the
env var list or table) **and** a short section in `server/index.md` that lists the env vars and links to the
feature page — see the "GitHub pull request comments" and "Slack notifications" sections there for the pattern.

## Page conventions

Front matter, copied from an existing page:

```yaml
---
title: 'Slack notifications'
date: 2026-10-14T19:30:08+10:00
draft: false
weight: 6
summary: One sentence shown on the docs index page
---
```

- `weight` sets the sidebar order (lower = higher up). Weights are shared by several pages and aren't unique — check
  them with `grep -H '^weight' content/docs/*/index.md` and put the page next to related features.
- Internal links use absolute paths with a trailing slash: `/docs/flaky-test/`, `/docs/gradle-plugin/#all-configuration-options`.
- Style: short paragraphs, `##`/`###` headings, bulleted lists, numbered steps for setup, env vars in a fenced code block
  as `NAME=<description>`, and tables for config options with defaults.
- Mark alpha/beta features at the top of the page and in the `server/index.md` section (e.g. `**Alpha:** …`).
- The Node script page already has a "Slack message" section for the old `writeSlackMessageFile` option, which
  is a different feature from the server-side Slack notifications. Don't mix them up.

## Get the facts from the code

Never document config from memory — take names and defaults from the source:

- Env vars: `server/server-app/src/main/resources/application.conf` (`${?ENV_VAR}` entries)
- Defaults and what "enabled" means: the feature's `*Config` class and its `create*Config` factory
  (e.g. `server/server-app/src/main/kotlin/projektor/notification/slack/SlackNotificationConfig.kt`)
- Module READMEs, which often already have the setup steps (e.g. `server/notification/slack/README.md`)
- Gradle plugin params: `publishers/gradle-plugin/src/main/groovy/projektor/plugin/ProjektorPublishPluginExtension.groovy`;
  Node script params: `publishers/node-script/src`
- Publisher versions: `publishers/gradle-plugin/gradle.properties`, `publishers/node-script/package.json`
- UI labels/buttons the docs tell users to click: the component in `ui/src` (use its exact text)

## Check the site builds

Build into a scratch directory so `public/` in the repo isn't touched:

```bash
cd ../projektor-doc && hugo --quiet -d <scratchpad>/site && ls <scratchpad>/site/docs/<slug>/
```

Use `hugo serve` to look at it in a browser. Netlify pins `HUGO_VERSION = "0.69.2"` in `netlify.toml`, which is older
than a typical local Hugo, so stick to plain Markdown and the shortcodes existing pages already use.
