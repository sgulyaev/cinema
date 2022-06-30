import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.0"
  kotlin("plugin.serialization") version "1.7.0"
}


repositories {
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-core:2.0.2")
  implementation("io.ktor:ktor-server-netty:2.0.2")
  implementation("io.ktor:ktor-server-content-negotiation:2.0.2")
  implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.2")

  implementation("ch.qos.logback:logback-classic:1.2.11")
  implementation("org.slf4j:jul-to-slf4j:1.7.36")

  testImplementation("io.ktor:ktor-server-test-host:2.0.2")
  testImplementation("io.ktor:ktor-server-tests-jvm:2.0.2")
  testImplementation(kotlin("test"))
}

sourceSets.main {
  java.srcDirs("src")
  resources.srcDirs("src/resources")
}
sourceSets.test { java.srcDirs("test") }

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "11"
    javaParameters = true
  }
}


tasks.register<Sync>("deps") {
  into("$buildDir/libs/deps")
  from(configurations.runtimeClasspath)
}

tasks.jar {
  dependsOn("deps")
  archiveBaseName.set("app")
  doFirst {
    manifest {
      attributes(
        "Main-Class" to "app.MainKt",
        "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}"}
      )
    }
  }
}