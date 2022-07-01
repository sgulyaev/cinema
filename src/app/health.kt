package app

import kotlinx.serialization.Serializable
import java.time.Instant

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