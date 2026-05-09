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
   <https://github.com/settings/tokens?type=beta> →
   _Generate new token (fine-grained)_.

   - Repository access: only **`waliasanchit007/konduit`**
   - Permissions:
     - **Repository permissions → Contents → Read-only**
     - **Repository permissions → Packages → Read-only** *(this is the one
       that actually matters)*
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
the _Verify Konduit read token is present_ step.

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
