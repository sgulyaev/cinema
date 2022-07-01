package app

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class CinemaControllerTest {
  lateinit var testClient: HttpClient
  val cinema = mockk<CinemaRepository> (relaxed = true)
  val controller = CinemaController(cinema)

  @Test
  fun receiveSeatsStatus() = test {
    val response = testClient.get("/api/seats")
    assertEquals("get-seats", response.bodyAsText())
  }

  @Test
  fun testBooking() = test {
    val response = testClient.put("/api/seats") {
      contentType(ContentType.Application.Json)
      setBody(listOf(Seat(1), Seat(2), Seat(2)))
    }
    assertEquals("put-seats[1,2]", response.bodyAsText())
  }

  private fun test(testCase: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    environment { config = ApplicationConfig("application-test.conf") }
    routing { addController(controller) }
    testClient = createClient {
      install(ContentNegotiation) { json() }
    }
    testCase()
  }
}