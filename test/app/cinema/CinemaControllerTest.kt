package app.cinema

import app.cinema.BookingUseCase.Result.Conflict
import app.cinema.BookingUseCase.Result.Success
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class CinemaControllerTest {
  lateinit var testClient: HttpClient
  val cinema = mockk<CinemaRepository>(relaxed = true)
  val booking = mockk<BookingUseCase>(relaxed = true)
  val controller = CinemaController(cinema, booking)

  @Test
  fun `receive seats statuses`() = test {
    every { cinema.getAll() } returns listOf(Seat(1, "owner1"), Seat(2), Seat(3, "owner2"))
    val response = testClient.get("/api/seats")
    val res = response.body<List<SeatResponse>>()

    assertEquals(setOf(1, 3), res.filter { !it.isFree }.map { it.id }.toSet())
    assertEquals(setOf(2), res.filter { it.isFree }.map { it.id }.toSet())
  }


  @Test
  fun `success booking`() = test {
    every { booking.book(any(), any()) } returns Success
    val response = testClient.put("/api/seats") {
      contentType(ContentType.Application.Json)
      setBody(BookingRequest(listOf(1, 2, 3), "owner1"))
    }
    verify { booking.book(listOf(1, 2, 3), "owner1") }
    assertEquals(HttpStatusCode.OK, response.status)
  }

  @Test
  fun `conflict booking`() = test {
    every { booking.book(any(), any()) } returns Conflict
    val response = testClient.put("/api/seats") {
      contentType(ContentType.Application.Json)
      setBody(BookingRequest(listOf(1, 2, 3), "owner1"))
    }
    assertEquals(HttpStatusCode.Conflict, response.status)
  }

  @Test
  fun reset() = test {
    testClient.post("/api/seats/reset?size=20")
    verify { cinema.reset(20) }
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