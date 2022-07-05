package app

import db.exec
import db.select
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.sql.ResultSet
import javax.sql.DataSource

interface Controller {
  fun installInto(routing: Routing)
}

fun Routing.addController(controller: Controller): Unit = controller.installInto(this)

class CinemaController(private val cinema: CinemaRepository) : Controller {
  override fun installInto(routing: Routing): Unit = routing.run {
    get("/api/seats") {
      call.respondText("get-seats")
    }
    put("/api/seats") {
      val seats = call.receive<Set<Seat>>()
      call.respondText("put-seats[${seats.joinToString(",") { it.id.toString() }}]")
    }
  }
}

@Serializable
data class Seat(val id: Int, val owner: String? = null)

class CinemaRepository(val db: DataSource, private val table: String = "seats") {
  private val mapper: ResultSet.() -> Seat = { Seat(getInt(1), getString(2)) }

  fun getById(id: Int): Seat? = db.select("select id, owner from $table where id = ?", listOf(id), mapper).firstOrNull()

  fun getByIds(ids: List<Int>): List<Seat> {
    if (ids.isEmpty()) return emptyList()
    val whereExpr = "where id in (${ids.joinToString(",") { "?" }})"
    return db.select("select id, owner from $table $whereExpr", ids, mapper)
  }

  fun getAll(): List<Seat> = db.select("select id, owner from $table", mapper = mapper)

  fun changeOwnerFor(ids: List<Int>, owner: String?) {
    if (ids.isEmpty()) return
    val whereExpr = "where id in (${ids.joinToString(",") { "?" }})"
    db.exec("update seats set owner = ? $whereExpr", listOf(owner) + ids)
  }
}
