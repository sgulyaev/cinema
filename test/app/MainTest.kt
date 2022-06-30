package app

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MainTest {
  @Test
  fun `health check`() = testApplication {
    val response = client.get("/api/health")
    assertEquals(HttpStatusCode.OK, response.status)
    with(response.bodyAsText()) {
      assertContains(this, "\"status\"")
      assertContains(this, "\"up\"")
    }
  }
}