package app

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

interface Controller {
  val installInto: Routing.() -> Unit
}
fun Routing.addController(controller: Controller): Unit = controller.installInto(this)

class CinemaController(private val cinema: CinemaRepository) : Controller {
  override val installInto: Routing.() -> Unit = {
    get("/api/seats") {
      call.respondText("get-seats")
    }
    put("/api/seats") {
      val seats = call.receive<Set<Seat>>()
      cinema.book(seats)
      call.respondText("put-seats[${seats.joinToString(",") { it.id.toString() }}]")
    }
  }
}

@Serializable
data class Seat(val id: Int)

class CinemaRepository {
  private val seats = (1..100).map { Seat(it) }.toSet()

  fun book(seats: Set<Seat>) {
    println("booked $seats")
  }

}
