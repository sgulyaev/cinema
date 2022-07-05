package app.cinema

import app.cinema.BookingUseCase.Result
import app.cinema.BookingUseCase.Result.Conflict
import app.cinema.BookingUseCase.Result.Success
import db.DBTest
import db.Transaction
import db.exec
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

class BookingUseCaseConcurrentTransactionsTest : DBTest() {
  companion object {
    const val table = "concurrentBookingTest"

    @BeforeAll @JvmStatic @Suppress("unused")
    fun createTable() {
      db.exec("create table $table as table seats with no data")
    }

    @AfterAll @JvmStatic @Suppress("unused")
    fun dropTable() {
      db.exec("drop table $table")
    }
  }

  val cinema = CinemaRepository(db, table)
  val booking = BookingUseCase(cinema)

  @Test
  fun `concurrent booking attempts on same seats`() {
    Transaction.current()?.detach()
    repeat(10) {
      cinema.reset(9)
      val (alice, bob) = concurrentBook(aliceSeats = listOf(1, 2, 3, 4, 5), bobSeats = listOf(5, 6, 7, 8, 9))

      val bobSuccess = bob == Success && alice == Conflict && seatsWithOwner("bob") == setOf(5, 6, 7, 8, 9) && seatsWithOwner("alice") == emptySet<Int>()
      val aliceSuccess = alice == Success && bob == Conflict && seatsWithOwner("alice") == setOf(1, 2, 3, 4, 5) && seatsWithOwner("bob") == emptySet<Int>()

      assertTrue(aliceSuccess xor bobSuccess, "should be one conflict and one success")
    }
  }

  @Test
  fun `concurrent booking attempts on different seats`() {
    Transaction.current()?.detach()
    repeat(10) {
      cinema.reset(9)
      val (alice, bob) = concurrentBook(aliceSeats = listOf(1, 2, 3), bobSeats = listOf(5, 6, 7, 8, 9))
      assertTrue(bob == Success && seatsWithOwner("bob") == setOf(5, 6, 7, 8, 9))
      assertTrue(alice == Success && seatsWithOwner("alice") == setOf(1, 2, 3))
    }
  }


  private fun concurrentBook(aliceSeats: List<Int>, bobSeats: List<Int>): AliceAndBob {
    var alice: Result? = null
    var bob: Result? = null
    val thread1 = thread(start = true) { alice = booking.book(aliceSeats, "alice") }
    val thread2 = thread(start = true) { bob = booking.book(bobSeats, "bob") }
    thread1.join()
    thread2.join()
    return AliceAndBob(alice, bob)
  }

  data class AliceAndBob(val alice: Result?, val bob: Result?)

  fun seatsWithOwner(owner: String? = null): Set<Int> = cinema.getAll().filter {
    if (owner == null) it.owner != null else it.owner == owner
  }.map { it.id }.toSet()
}
