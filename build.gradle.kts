plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("androidx.room") version "2.8.5" apply false
}

tasks.register<Exec>("sonar") {
    dependsOn(":app:testDebugUnitTest", ":app:jacocoTestReport", ":app:lintDebug")
    group = "verification"
    description = "Runs SonarQube code quality analysis using SonarScanner."

    val hostUrl = providers.environmentVariable("SONAR_HOST_URL")
        .orElse(providers.gradleProperty("sonar.host.url"))
        .orElse(providers.systemProperty("sonar.host.url"))
        .getOrElse("http://localhost:9000")

    val token = providers.environmentVariable("SONAR_TOKEN")
        .orElse(providers.gradleProperty("sonar.token"))
        .orElse(providers.systemProperty("sonar.token"))
        .getOrElse("")

    val dockerHost = if (hostUrl.contains("localhost") || hostUrl.contains("127.0.0.1")) {
        "http://sonarqube:9000"
    } else {
        hostUrl
    }

    commandLine(
        "docker", "run", "--rm",
        "--network", "sonar-net",
        "-v", "${rootProject.projectDir}:/usr/src",
        "-e", "SONAR_HOST_URL=$dockerHost",
        "-e", "SONAR_TOKEN=$token",
        "sonarsource/sonar-scanner-cli:latest"
    )
}
