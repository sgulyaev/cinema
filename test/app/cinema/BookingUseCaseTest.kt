package app.cinema

import app.cinema.BookingUseCase.Result.Conflict
import app.cinema.BookingUseCase.Result.Success
import db.DBTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BookingUseCaseTest : DBTest() {
  val cinema = CinemaRepository(db)
  val booking = BookingUseCase(cinema)

  @Test
  fun `booking seats success path`() {
    val res = booking.book(listOf(3, 4, 5), "owner1")
    assertEquals(Success, res)
    assertEquals(setOf(3, 4, 5), cinema.getAll().filter { it.owner == "owner1" }.map { it.id }.toSet())
  }

  @Test
  fun `booking seats conflict`() {
    booking.book(listOf(3, 4, 5), "owner1")
    val res = booking.book(listOf(5, 6, 7), "owner2")
    assertEquals(Conflict, res)
    assertEquals(emptySet(), cinema.getAll().filter { it.owner == "owner2" }.map { it.id }.toSet())
  }
}
