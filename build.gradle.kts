import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.shadow)
}

group = "org.chorus_oss"
version = "1.0-SNAPSHOT"
description = "Chorus"

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
}

kotlin {
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_21
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.chorus.protocol)
                implementation(libs.chorus.raknet)
                implementation(libs.chorus.kflate)
                implementation(libs.chorus.snappy)
                implementation(libs.kotlinx.io)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.bundles.cryptography)
                implementation(libs.bundles.ktoml)
                implementation(libs.json)
                implementation(libs.fleks)
                implementation(libs.kotlin.poet)
                implementation(libs.rwmutex)
                implementation(libs.kotlin.reflect)
                implementation(libs.clikt)

                implementation(compose.runtime)
                implementation(compose.components.resources)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmMain {
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
                implementation(libs.sentry)
                implementation(libs.sentry.log4j2)
                implementation(libs.oshi)
                implementation(libs.bundles.terminal)
                implementation(libs.caffeine)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.bundles.test)
                implementation(libs.commonsio)
                implementation(libs.commonslang3)
            }
        }
    }

    mavenPublishing {
        signAllPublications()

        coordinates(
            group.toString(),
            "chorus",
            version.toString()
        )

        pom {
            name = "Chorus"
            description = project.description
            inceptionYear = "2025"
            url = "https://github.com/Chorus-OSS/Chorus"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    distribution = "repo"
                }
            }
            developers {
                developer {
                    id = "omniacdev"
                    name = "OmniacDev"
                    url = "https://github.com/OmniacDev"
                    email = "omniacdev@chorus-oss.org"
                }
            }
            scm {
                url = "https://github.com/Chorus-OSS/Chorus"
                connection = "scm:git:git://github.com/Chorus-OSS/Chorus.git"
                developerConnection = "scm:git:ssh://github.com/Chorus-OSS/Chorus.git"
            }
            issueManagement {
                system = "GitHub Issues"
                url = "https://github.com/Chorus-OSS/Chorus/issues"
            }
        }

        configure(
            KotlinMultiplatform()
        )
    }
}

compose.resources {
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = provider {
            layout.projectDirectory.dir("src/commonMain/resources")
        }
    )
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveVersion = ""
        manifest {
            attributes["Main-Class"] = "org.chorus_oss.chorus.Chorus"
        }

        destinationDirectory = layout.buildDirectory

        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }

    build {
        dependsOn("shadowJar")
    }
}