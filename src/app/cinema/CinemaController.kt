package app.cinema

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

interface Controller {
  fun installInto(routing: Routing)
}

fun Routing.addController(controller: Controller): Unit = controller.installInto(this)

class CinemaController(
    private val cinema: CinemaRepository,
    private val booking: BookingUseCase
) : Controller {
  override fun installInto(routing: Routing): Unit = routing.run {
    get("/api/seats") {
      call.respond(cinema.getAll().map { SeatResponse(it.id, it.isFree) })
    }

    put("/api/seats") {
      val request = call.receive<BookingRequest>()
      val result = booking.book(request.ids, request.owner)
      call.respond(if (result == BookingUseCase.Result.Success) HttpStatusCode.OK else HttpStatusCode.Conflict)
    }

    post("/api/seats/reset") {
      val size = minOf(10000, call.request.queryParameters["size"]?.toInt() ?: 10)
      cinema.reset(size)
      call.respondText("reset with size: $size")
    }
  }
}

@Serializable
data class BookingRequest(val ids: List<Int>, val owner: String)

@Serializable
data class SeatResponse(val id: Int, val isFree: Boolean)