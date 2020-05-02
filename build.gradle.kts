import org.apache.tools.ant.taskdefs.condition.Os

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

                implementation(Libraries.cliktMultiplatform)
//                implementation(Libraries.ktor.client.core)
//                implementation(Libraries.ktor.client.json)
//                implementation(Libraries.ktor.client.kotlinxSerialization)
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
                implementation(Libraries.ktor.client.coreNative)
                implementation(Libraries.ktor.client.curl)
                implementation(Libraries.ktor.client.kotlinxSerializationNative)
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

val releaseCurrent by tasks.registering {
    dependsOn("linkLagunaDebugExecutable${currentOs.capitalize()}")
    dependsOn("linkLagunaReleaseExecutable${currentOs.capitalize()}")
}