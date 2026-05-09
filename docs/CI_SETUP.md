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

1. **Create the PAT.**  Go to
   <https://github.com/settings/tokens> → _Generate new token (classic)_.

   > **Important:** must be a **classic** PAT, *not* a fine-grained one.
   > Fine-grained tokens cannot read Maven packages from GitHub Packages —
   > only npm / Container / RubyGems are supported there. A fine-grained
   > token returns HTTP 404 (not 401) on Maven endpoints, which makes the
   > failure look like a missing artifact instead of an auth issue.

   - Scopes: only **`read:packages`** is required. (You can leave every
     other scope unchecked — the token doesn't need `repo`, `workflow`,
     etc. to fetch Maven artifacts.)
   - Expiry: 1 year is fine for now; rotate later.

2. **Add it to Caliclan as a repository secret.**  Open the Caliclan
   repo settings:

   <https://github.com/waliasanchit007/ServerDrivenUI/settings/secrets/actions>

   _New repository secret_:

   - **Name:** `KONDUIT_READ_TOKEN`
   - **Value:** the PAT from step 1

3. **Re-run the workflow.**  Pushing any commit (or hitting _Re-run all
   jobs_ on a previous run) will pick up the new secret.

If the secret is missing the workflow fails fast with a clear error in
the _Verify Konduit read token is present_ step. The same step also
probes `konduit-gradle-plugin-1.0.0-caliclan.2.pom` directly, so a
token that's present but lacks `read:packages` (e.g. a fine-grained
PAT) fails here too with a 404 → "PAT lacks read:packages" message,
rather than producing a confusing _plugin not found_ stack trace deep
in Gradle plugin resolution.

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
