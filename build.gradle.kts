plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.70"
}

repositories {
    mavenCentral()
    jcenter()
}

project.ext["mainClass"] = "com.jdiazcano.laguna.LagunaKt"

kotlin {
//    val macos = macosX64("macos")
    val jvm = jvm() {
        withJava()
        val jvmJar by tasks.getting(org.gradle.jvm.tasks.Jar::class) {
            doFirst {
                manifest {
                    attributes["Main-Class"] = project.ext["mainClass"]
                }
                from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("common/src")
            dependencies {
                implementation(kotlin("stdlib-common", "1.3.70"))

                implementation(Libraries.clikt)
            }
        }

        val commonTest by getting {
            kotlin.srcDir("common/tst")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

//        val native by creating {
//            kotlin.srcDir("common-native/src")
//            dependsOn(commonMain)
//            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:0.14.0")
//            }
//        }
//
//        val macosMain by getting {
//            kotlin.srcDir("macos")
//            dependsOn(native)
//        }



        val jvmMain by getting {
            kotlin.srcDir("jvm/src")
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        val jvmTest by getting {
            kotlin.srcDir("jvm/tst")
            dependsOn(jvmMain)
            dependencies {
            }
        }
    }
}