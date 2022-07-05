package db

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.sql.DataSource

class TransactionTest {
  val db = mockk<DataSource>(relaxed = true)

  @Test
  fun `transaction does not open connection at start`() {
    val tx = Transaction(db)
    verify(exactly = 0) { db.connection }
    tx.close(true)
    verify(exactly = 0) { db.connection }
  }

  @Test
  fun `transaction creates and reuses connection on demand`() {
    val tx = Transaction(db).attach()
    val conn = db.withConnection { this }
    assertThat(db.withConnection { this }).isSameAs(conn)
    verify { conn.autoCommit = false }
    tx.close(true)
    verify(exactly = 1) {
      db.connection.apply {
        commit()
        autoCommit = true
        close()
      }
    }
  }

  @Test
  fun `transaction with rollbackOnly rolls back`() {
    val tx = Transaction(db).attach()
    val conn = db.withConnection { this }
    verify { conn.autoCommit = false }
    tx.close(false)
    verify(exactly = 1) {
      db.connection.apply {
        rollback()
        autoCommit = true
        close()
      }
    }
  }

  @Test
  fun `connection without transaction is closed`() {
    val conn = db.withConnection { this }
    verify { conn.close() }
    verify(exactly = 0) { conn.autoCommit = any() }
  }
}
