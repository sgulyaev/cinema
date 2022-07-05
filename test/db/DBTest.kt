package db

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import di.DI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class DBTest {
  companion object {
    lateinit var db: HikariDataSource

    @BeforeAll @JvmStatic @Suppress("unused")
    fun initDB() {
      db = try {
        DBModule("_test").createDataSource()
      } catch (e: HikariPool.PoolInitializationException) {
        throw IllegalStateException("Test DB not running, please use `docker-compose up -d db`\n${e.message}")
      }
    }

    @AfterAll @JvmStatic @Suppress("unused")
    fun closeHikari() = db.close()
  }

  @RegisterExtension @JvmField @Suppress("unused")
  val autoRollback = InTransactionRunner()

  class InTransactionRunner : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
      Transaction(db).attach()
    }

    override fun afterEach(context: ExtensionContext?) {
      Transaction.current()!!.close(commit = false)
    }
  }
}

class DbModuleTest : DBTest() {
  @Test
  fun `check connection`() {
    assert(db.select("select count(*) from seats") { getInt(1) }.isNotEmpty())
  }

  @Test
  fun `if installed as plugin should provide datasource to DI and close datasource when application stopped`() {
    val di = DI()
    val server = embeddedServer(Netty, 0) {
      install(DBModule("_test").Plugin(di))
    }.start()

    val hikariDataSource = di.require<DataSource>() as HikariDataSource
    assertFalse(hikariDataSource.isClosed)

    server.stop()
    assertTrue(hikariDataSource.isClosed)
  }
}

