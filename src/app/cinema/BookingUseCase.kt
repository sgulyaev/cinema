package app.cinema

import app.cinema.BookingUseCase.Result.*
import db.transaction
import java.sql.Connection.*
import java.sql.SQLException


class BookingUseCase(private val cinema: CinemaRepository) {
  fun book(ids: List<Int>, owner: String): Result = cinema.db.transaction {
    if (connection.transactionIsolation < TRANSACTION_REPEATABLE_READ) {
      connection.transactionIsolation = TRANSACTION_REPEATABLE_READ
    }

    val seats = cinema.getByIds(ids)
    if (seats.any { it.owner != null }) return@transaction Conflict
    try {
      cinema.changeOwnerFor(ids, owner)
      return@transaction Success
    } catch (e: SQLException) {
      if (e.message?.contains("could not serialize access due to concurrent update") == true) {
        needRollback = true
        return@transaction Conflict
      } else throw e
    }
  }

  enum class Result {
    Success, Conflict
  }
}