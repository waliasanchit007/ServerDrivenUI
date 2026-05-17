plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
    // Required for @Serializable data classes used as wire types in
    // ZiplineService method signatures (e.g. List<Quote> return type
    // on HostQuotesProvider). Without this plugin Zipline's
    // .serializer() lookup fails at take<> time with
    // "Serializer for class 'X' is not found".
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    js {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared-widget")) // Widget interfaces
            implementation(libs.redwood.treehouse)
            implementation(libs.redwoodProtocol)
            implementation(libs.redwood.protocol.host)
            api(libs.zipline)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Lint task: reject ZiplineService methods with function-typed parameters.
//
// Why: Zipline can only marshal `@Serializable` values or `ZiplineService`
// proxies across the QuickJS boundary. A raw `(T) -> Unit` parameter
// COMPILES — the Kotlin compiler accepts the signature — but the runtime
// proxy fails to construct on the guest side, so `take<T>()` returns a
// non-null but BROKEN proxy and every later call is a silent no-op. The
// failure mode is unobservable because the proxy-construction error
// surfaces to a Zipline stdout that gets dropped before the host-console
// polyfill is installed. See `docs/KNOWN_BUGS.md` U11 for the full
// history (the canonical workaround is to declare a callback
// `ZiplineService` like `SnackbarResultCallback` instead).
//
// This lint catches the bad shape at build time so adopters never ship
// it. Wired into `check` so it runs as part of any standard build.
val validateZiplineServiceShapes = tasks.register("validateZiplineServiceShapes") {
    description = "Reject ZiplineService methods with function-typed parameters (KNOWN_BUGS.md U11)."
    group = "verification"

    val protocolFile = file("src/commonMain/kotlin/com/example/serverdrivenui/shared/Protocol.kt")
    inputs.file(protocolFile)
    outputs.upToDateWhen { true } // pure-input check; no output file to track

    doLast {
        if (!protocolFile.exists()) return@doLast
        val content = protocolFile.readText()

        // Find each ZiplineService interface block. Handles nested braces
        // inside the body by walking the brace count rather than relying
        // on a single regex.
        val interfaceHeader = Regex("""interface\s+(\w+)\s*:\s*ZiplineService\s*\{""")
        val findings = mutableListOf<String>()
        interfaceHeader.findAll(content).forEach { match ->
            val name = match.groupValues[1]
            val bodyStart = match.range.last + 1
            // Walk braces to find the matching close.
            var depth = 1
            var i = bodyStart
            while (i < content.length && depth > 0) {
                when (content[i]) {
                    '{' -> depth++
                    '}' -> depth--
                }
                i++
            }
            val body = content.substring(bodyStart, i - 1)

            // Detect function-typed parameters. Looking for `name: (...) -> X` patterns
            // inside `fun X(...)` method signatures. The lambda type form
            // `(A, B) -> C` is what trips Zipline; a `ZiplineService`
            // callback type like `cb: MyCallback` (interface) is fine.
            //
            // The regex matches `<paramName>: (...stuff...) -> <returnType>`
            // optionally with `?` for nullable function types.
            val lambdaParam = Regex("""(\w+)\s*:\s*(?:\([^()]*\)|\([^()]*\([^()]*\)[^()]*\))\s*->\s*\w+\??""")
            lambdaParam.findAll(body).forEach { hit ->
                findings += "  - $name has function-typed parameter: `${hit.value.trim()}`"
            }
        }

        if (findings.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("ZiplineService interface(s) in shared/Protocol.kt have function-typed parameters.")
                    appendLine("Zipline can only marshal @Serializable values or ZiplineService proxies; raw")
                    appendLine("function types compile but the runtime proxy fails to construct on the guest")
                    appendLine("side, making every method call a silent no-op. See docs/KNOWN_BUGS.md U11.")
                    appendLine()
                    appendLine("Workaround: declare a callback ZiplineService (like SnackbarResultCallback)")
                    appendLine("and pass that instead. The guest wraps the user's lambda in an anonymous impl;")
                    appendLine("the host calls callback.onResult(...) then callback.close().")
                    appendLine()
                    appendLine("Found:")
                    findings.forEach { appendLine(it) }
                }
            )
        }
    }
}

tasks.named("check") {
    dependsOn(validateZiplineServiceShapes)
}
