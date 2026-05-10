# CI setup

Caliclan ships a single GitHub Actions workflow at
`.github/workflows/ci.yml` that runs three Gradle tasks on `macos-latest`
on every push + PR:

| Task | Purpose |
|---|---|
| `:androidApp:assembleDebug` | Android host APK |
| `:composeApp:linkDebugFrameworkIosSimulatorArm64` | iOS sim framework (catches Kotlin/Native + ObjC interop breaks) |
| `:presenter:compileDevelopmentExecutableKotlinJsZipline` | Guest .zipline file |

The matrix is macOS-only because iOS link needs Xcode. We don't add a
Linux runner — past attempts (Konduit fork) hung at 58 min on the cache
restore step.

## Required secret: `KONDUIT_READ_TOKEN`

Caliclan depends on `dev.konduit:konduit-*` artifacts published to
GitHub Packages on the **`waliasanchit007/konduit`** repository
(separate repo). The default `GITHUB_TOKEN` provided by Actions only has
read access to packages **on the same repository**, so the workflow
needs an explicit Personal Access Token.

### One-time setup

1. **Create the PAT.** Open this exact URL — it goes to the
   **classic** tokens page (the legacy UI, not the fine-grained one):

   <https://github.com/settings/tokens/new>

   > **Important:** must be a **classic** PAT, *not* fine-grained.
   > Fine-grained tokens *cannot* read Maven packages from GitHub
   > Packages — only npm / Container / RubyGems are supported there.
   > A fine-grained token returns HTTP 404 (not 401) on Maven endpoints,
   > which makes the failure look like a missing artifact instead of an
   > auth issue.
   >
   > How to tell which kind you made:
   > - Classic: the page title says "New personal access token (classic)"
   >   and offers a checkbox grid of scopes (`repo`, `workflow`,
   >   `read:packages`, …).
   > - Fine-grained: the page title says "New fine-grained personal
   >   access token" and asks you to pick repository access + permission
   >   categories (Contents, Packages, …) with read/write dropdowns. **If
   >   you see this page, back out and use the URL above.**

   - Note (description field): something like `caliclan-read-packages`.
   - Expiry: 1 year is fine; rotate later.
   - Scopes: tick only **`read:packages`**. The other scopes can stay
     unchecked — the token doesn't need `repo`, `workflow`, etc. to
     fetch Maven artifacts.
   - Click **Generate token** and copy the `ghp_…` value.

2. **Verify the token before saving it as a secret.** Run this from any
   terminal (replace `ghp_…` with what you just copied):

   ```bash
   curl -s -i -u "waliasanchit007:ghp_…" https://api.github.com/user \
     | awk 'BEGIN{IGNORECASE=1} /^HTTP\/|^x-oauth-scopes/{print}'
   ```

   - **Expected** for a classic PAT with `read:packages`:

     ```
     HTTP/2 200
     x-oauth-scopes: read:packages
     ```

   - **Wrong (fine-grained):** `HTTP/2 200` shows but the
     `x-oauth-scopes` line is **missing entirely**. Regenerate via the
     URL above.
   - **Wrong (classic but missing scope):** `x-oauth-scopes:` line is
     present but empty or doesn't include `read:packages`.

3. **Add it to Caliclan as a repository secret.** Open Caliclan's repo
   settings:

   <https://github.com/waliasanchit007/ServerDrivenUI/settings/secrets/actions>

   _New repository secret_ (or _Update_ if `KONDUIT_READ_TOKEN` already
   exists):

   - **Name:** `KONDUIT_READ_TOKEN`
   - **Value:** the PAT from step 1

4. **Re-run CI.** Push any commit, or hit _Re-run all jobs_ on the
   latest run.

If the secret is missing or wrong, the workflow fails fast in the
_Verify Konduit read token is present + can read packages_ step. The
step prints the response headers from `/user` (so you can see the
token's scope inline) followed by a Maven artifact probe with a 404
triage tree pointing at the most likely cause.

## Local development

The workflow's auth setup also works for local builds — drop these into
`~/.gradle/gradle.properties`:

```properties
gpr.user=waliasanchit007
gpr.token=<the same PAT from step 1>
```

…and you can wipe `~/.m2/repository/dev/konduit/` to confirm packages
resolve from GitHub. In day-to-day dev the `mavenLocal()` repo at the
top of the resolver list short-circuits this, so the PAT only matters
on a clean machine.

## Caching

The workflow uses `gradle/actions/setup-gradle@v4` for Gradle's standard
build cache. Branches other than `main` / `konduit-main` use the cache
read-only so feature branches can't poison the canonical cache.

## Future additions

- iOS `.app` build via `xcodebuild` (commented out in `ci.yml`; uncomment
  if we start seeing ObjC interop regressions sneak past the framework
  link).
- Konduit fork → Caliclan token automation: rotate via 1Password and a
  GH org secret once we move out of the personal namespace.
- Lint / detekt — Tier 3.
- Snapshot tests — Tier 3 once we adopt `konduit-snapshot-testing`.
