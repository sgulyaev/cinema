import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.0"
}


repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

sourceSets.main { java.srcDirs("src") }
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