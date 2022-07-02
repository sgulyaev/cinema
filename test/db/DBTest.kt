package db

import com.zaxxer.hikari.pool.HikariPool
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.junit.jupiter.api.AfterAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class DBTest {
  companion object {
    val db = try {
      DBModule("_test").createDataSource()
    } catch (e: HikariPool.PoolInitializationException) {
      throw IllegalStateException("Test DB not running, please use `docker-compose up -d db`\n${e.message}")
    }

    @AfterAll @JvmStatic @Suppress("unused")
    fun closeHikari() = db.close()
  }
}

class DbModuleTest : DBTest() {
  @Test
  fun `check connection`() {
    db.connection.use { connection ->
      val st = connection.prepareStatement("select * from seats")
      with(st.executeQuery()) {
        val list = mutableListOf<Int>().also { while (next()) it += getInt(1) }
        assert(list.isNotEmpty())
      }
    }
  }

  @Test
  fun `if installed as plugin should close datasource when application stopped`() {
    val server = embeddedServer(Netty, 0) {
      install(DBModule("_test").plugin)
    }.start()
    assertFalse(DBModule.db.isClosed)

    server.stop()
    assertTrue(DBModule.db.isClosed)
  }
}



