package app

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.Instant

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
  install(ContentNegotiation) {
    json()
  }

  routing {
    get("/api/health") {
      call.respond(HealthResponse())
    }
  }
}

@Serializable
data class HealthResponse(
  val status: String = "up",
  val startedAt: String = startTime.toString(),
  val totalMemoryMib: Long = runtime.totalMemory() / 1024 / 1024,
  val usedMemoryMib: Long = totalMemoryMib - runtime.freeMemory() / 1024 / 1024
) {
  companion object {
    private val startTime = Instant.now()
    private val runtime = Runtime.getRuntime()
  }
}