package app.cinema

import db.exec
import db.select
import java.sql.ResultSet
import javax.sql.DataSource

data class Seat(val id: Int, val owner: String? = null) {
  val isFree get() = owner == null
}

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
    db.exec("update $table set owner = ? $whereExpr", listOf(owner) + ids)
  }

  fun reset(size: Int) {
    db.exec("delete from $table")
    val values = (1..size).joinToString(",") { "(?)" }
    db.exec("insert into $table (id) values $values", (1..size).toList())
  }
}

