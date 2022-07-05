package app.cinema

import app.cinema.BookingUseCase.Result.*

class BookingUseCase(private val cinema: CinemaRepository) {
  fun book(ids: List<Int>, owner: String): Result {
    val seats = cinema.getByIds(ids)
    if (seats.any { it.owner != null }) return Conflict
    cinema.changeOwnerFor(ids, owner)
    return Success
  }

  enum class Result {
    Success, Conflict
  }
}