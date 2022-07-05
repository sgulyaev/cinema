package app.cinema

import db.DBTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CinemaRepositoryTest : DBTest() {
  val cinema = CinemaRepository(db)

  @Test
  fun `get by id`() {
    assertEquals(Seat(7), cinema.getById(7))
  }

  @Test
  fun `get by ids`() {
    val seats = cinema.getByIds(listOf(1, 2, 3))
    assertEquals(setOf(Seat(1), Seat(2), Seat(3)), seats.toSet())
  }

  @Test
  fun `return empty list if empty ids`() {
    val seats = cinema.getByIds(listOf())
    assertTrue(seats.isEmpty())
  }

  @Test
  fun `get all`() {
    val seats = cinema.getAll()
    assertEquals(9, seats.size)
  }

  @Test
  fun `change owner`() {
    cinema.changeOwnerFor(listOf(3, 4, 6), "sg")
    assertEquals(setOf(3, 4, 6), cinema.getAll().filter { it.owner == "sg" }.map { it.id }.toSet())
  }

  @Test
  fun `reset with needed seats count`() {
    cinema.changeOwnerFor(listOf(3, 4, 6), "sg")
    cinema.reset(15)
    val seats = cinema.getAll()
    assertEquals(15, seats.size)
    assertTrue(seats.all { it.owner == null })
  }
}