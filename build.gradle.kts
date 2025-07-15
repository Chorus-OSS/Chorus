import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    `java-library`
    `maven-publish`
    jacoco
    id("com.gradleup.shadow") version "8.3.7"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin.jvmToolchain(21)

group = "org.chorus_oss"
version = "1.0-SNAPSHOT"
description = "Chorus"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
}

dependencies {
    api(libs.bundles.netty)
    api(libs.bundles.logging)
    api(libs.annotations)
    api(libs.jsr305)
    api(libs.gson)
    api(libs.guava)
    api(libs.commonsio)
    api(libs.snakeyaml)
    api(libs.stateless4j)

    implementation(libs.bundles.leveldb)
    implementation(libs.rng.simple)
    implementation(libs.rng.sampling)
    implementation(libs.asm)
    implementation(libs.jose4j)
    implementation(libs.joptsimple)
    implementation(libs.sentry)
    implementation(libs.sentry.log4j2)
    implementation(libs.disruptor)
    implementation(libs.oshi)
    implementation(libs.bundles.compress)
    implementation(libs.bundles.terminal)
    implementation(libs.caffeine)

    testImplementation(libs.bundles.test)
    testImplementation(libs.commonsio)
    testImplementation(libs.commonslang3)

    implementation(libs.chorus.protocol)
    implementation(libs.kotlinx.io)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.cryptography.core)
    implementation(libs.cryptography.random)
    implementation(libs.cryptography.provider.jdk)
    implementation(libs.jwt)
    implementation(libs.jwt.ecdsa)
    implementation(libs.ktoml.core)
    implementation(libs.ktoml.file)
    implementation(libs.json)
    implementation(libs.fleks)
    implementation(libs.kotlin.poet)
    implementation(libs.rwmutex)
    implementation(libs.kotlin.reflect)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xpkginfo:always")
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as CoreJavadocOptions).apply {
            addStringOption("source", java.sourceCompatibility.toString())
            addStringOption("Xdoclint:none", "-quiet")
        }
    }

    test {
        enabled = false // TODO: Fix tests. Use MockK

        useJUnitPlatform()
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED")
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            csv.required = false
            xml.required = true
            html.required = false
        }
    }

    shadowJar {
        archiveVersion.set("")

        manifest {
            attributes("Main-Class" to "org.chorus_oss.chorus.Chorus")
        }
        transform(Log4j2PluginsCacheFileTransformer::class.java) // required to fix shadowJar log4j2 issue
        destinationDirectory = layout.buildDirectory
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.register<DefaultTask>("buildSkipChores") {
    dependsOn(tasks.build)

    tasks["test"].enabled = false
    tasks["check"].enabled = false
    tasks["javadoc"].enabled = false
    tasks["javadocJar"].enabled = false
    tasks["sourcesJar"].enabled = false
    tasks["compileTestJava"].enabled = false
    tasks["processTestResources"].enabled = false
    tasks["testClasses"].enabled = false
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        pom {
            repositories {
                maven("https://jitpack.io")
                maven("https://repo.opencollab.dev/maven-releases/")
                maven("https://repo.opencollab.dev/maven-snapshots/")
            }
        }
    }
}