package db

import java.sql.Connection
import javax.sql.DataSource

class Transaction(private val db: DataSource) {
  companion object {
    private val threadLocal = ThreadLocal<Transaction>()
    fun current(): Transaction? = threadLocal.get()
  }

  private var conn: Connection? = null

  val connection: Connection
    get() = conn ?: db.connection.also { it.autoCommit = false; conn = it }

  fun close(commit: Boolean) {
    try {
      conn?.apply {
        if (commit) commit() else rollback()
        autoCommit = true
        close()
      }
    } finally {
      conn = null
      detach()
    }
  }

  fun attach() = this.also { threadLocal.set(this) }
  fun detach() = threadLocal.remove()
}

fun <R> DataSource.transaction(block: Transaction.() -> R): R {
  val tx = Transaction.current()
  if (tx != null) return tx.block()

  val topLevel = Transaction(this).attach()
  return try {
    topLevel.block()
  } catch (e: Throwable) {
    topLevel.close(commit = false)
    throw e
  } finally {
    topLevel.detach()
  }
}

fun <R> DataSource.withConnection(block: Connection.() -> R): R {
  val tx = Transaction.current()
  return if (tx != null) tx.connection.block()
  else connection.use(block)
}
