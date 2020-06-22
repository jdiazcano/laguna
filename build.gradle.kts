import org.apache.tools.ant.taskdefs.condition.Os
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

plugins {
    kotlin("multiplatform") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.70"
}

apply(from = Gradles.repositories)

project.ext["mainPackage"] = "com.jdiazcano.laguna"

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

val currentOs = when {
    Os.isFamily(Os.FAMILY_MAC) -> "macos"
    Os.isFamily(Os.FAMILY_UNIX) -> "linux"
    Os.isFamily(Os.FAMILY_WINDOWS) -> "windows"
    else -> TODO()
}

kotlin {
    val jvm = jvm {
        withJava()
        val jvmJar by tasks.getting(org.gradle.jvm.tasks.Jar::class) {
            doFirst {
                manifest {
                    attributes["Main-Class"] = "${project.ext["mainPackage"]}.LagunaKt"
                }
                from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
            }
        }
    }

    val linux = linuxX64("linux") {
        binaries {
            executable("laguna") {
                entryPoint = "${project.ext["mainPackage"]}.main"
            }
        }

        compilations["main"].cinterops {
            val libgit2 by creating {
                packageName = "libgit2"
                defFile(project.file("common-native/nativeInterop/libgit2.def"))
                includeDirs.headerFilterOnly("/usr/include")
            }
        }
    }

    val macos = macosX64("macos") {
        binaries {
            executable("laguna") {
                entryPoint = "${project.ext["mainPackage"]}.main"
            }
        }

        compilations["main"].cinterops {
            val libgit2 by creating {
                packageName = "libgit2"
                defFile(project.file("common-native/nativeInterop/libgit2.def"))
                includeDirs.headerFilterOnly("/opt/local/include", "/usr/local/include")
            }
        }
    }

//    val windows = mingwX64("windows") {
//        binaries {
//            executable("laguna") {
//                entryPoint = "${project.ext["mainPackage"]}.main"
//            }
//        }
//
//        compilations["main"].cinterops {
//            val libgit2 by creating {
//                packageName = "libgit2"
//                defFile(project.file("common-native/nativeInterop/libgit2.def"))
//                includeDirs.headerFilterOnly(mingwPath.resolve("include"))
//            }
//        }
//    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("common/src")
            dependencies {
                implementation(kotlin("stdlib-common", "1.3.70"))

                implementation(Libraries.jetbrains.kotlin.coroutinesCommon)
                implementation(Libraries.cliktMultiplatform)
                implementation(Libraries.korlibs.korte)
            }
        }

        val commonTest by getting {
            kotlin.srcDir("common/tst")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val native by creating {
            kotlin.srcDir("common-native/src")
            dependsOn(commonMain)
            dependencies {
                implementation(Libraries.jetbrains.kotlin.coroutinesNative)
            }
        }

        val nativeTest by creating {
            kotlin.srcDir("common-native/tst")
            dependsOn(commonTest)
            dependencies {
            }
        }

        val linuxMain by getting {
            kotlin.srcDirs("linux/src")
            dependsOn(native)
        }

        val linuxTest by getting {
            kotlin.srcDirs("linux/tst")
            dependsOn(nativeTest)
        }

//        val windowsMain by getting {
//            kotlin.srcDirs("windows/src")
//            dependsOn(native)
//        }
//
//        val windowsTest by getting {
//            kotlin.srcDirs("windows/tst")
//            dependsOn(windowsMain)
//        }
//
        val macosMain by getting {
            kotlin.srcDir("macos/src")
            dependsOn(native)
        }

        val macosTest by getting {
            kotlin.srcDirs("macos/tst")
            dependsOn(nativeTest)
        }

        val jvmMain by getting {
            kotlin.srcDir("jvm/src")
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(Libraries.ktor.client.kotlinxSerializationJvm)
                implementation(Libraries.ktor.client.cio)
            }
        }

        val jvmTest by getting {
            kotlin.srcDir("jvm/tst")
            dependsOn(commonTest)
            dependencies {
            }
        }
    }
}

val release by tasks.registering {
    dependsOn("linkLagunaReleaseExecutable${currentOs.capitalize()}")

    doLast {
        val from = Paths.get("build/bin/$currentOs/lagunaReleaseExecutable/laguna.kexe")
        val to = Paths.get("build/bin/executables/laguna")
        to.parent.toFile().mkdirs()
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
    }
}