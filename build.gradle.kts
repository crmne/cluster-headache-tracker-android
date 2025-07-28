// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

// Spotless configuration
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "indent_size" to 4,
                    "continuation_indent_size" to 4,
                    "max_line_length" to 120,
                    "insert_final_newline" to true,
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt-config.yml")
    source.setFrom("app/src/main/java", "app/src/test/java", "app/src/androidTest/java")
}

// Task to install git hooks
tasks.register("installGitHooks", Copy::class) {
    from("$rootDir/scripts/git-hooks")
    into("$rootDir/.git/hooks")
    fileMode = 0b111101101 // -rwxr-xr-x
}

// Ensure git hooks are installed when the project is configured
afterEvaluate {
    tasks.getByName("prepareKotlinBuildScriptModel").dependsOn("installGitHooks")
}
