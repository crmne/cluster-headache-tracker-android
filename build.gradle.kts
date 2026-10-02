// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 compiles Kotlin itself; declaring the Kotlin plugins here pins the Kotlin Gradle plugin version.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

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
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt-config.yml")
    source.setFrom("app/src/main/java", "app/src/test/java", "app/src/androidTest/java")
}

tasks.register("installGitHooks", Copy::class) {
    from("$rootDir/scripts/git-hooks")
    into("$rootDir/.git/hooks")
    filePermissions { unix("rwxr-xr-x") }
}

tasks.matching { it.name == "prepareKotlinBuildScriptModel" }.configureEach {
    dependsOn("installGitHooks")
}
