plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.0.4"
    id("com.gradleup.shadow") version "8.3.8"
    eclipse
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

group = "me.eccentric_nz.gamemodeinventories"

version = "3.4.3"

val apiVersion = "1.19"

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to project.version, "apiVersion" to apiVersion)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven { url = uri("https://maven.playpro.com/") }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:22.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly(files("lib/LogBlock.jar"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("org/purpurmc/**", "org/spigotmc/**")
    relocate("com.zaxxer.hikari", "${project.group}.shadow.hikari")
    minimize()
}

tasks.build {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.shadowJar)
}

tasks.jar { archiveClassifier.set("part") }

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:deprecation"))
    options.encoding = "UTF-8"
    options.isFork = true
}

spotless {
    java {
        removeUnusedImports()
        palantirJavaFormat()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}
