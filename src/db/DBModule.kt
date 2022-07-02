package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

class DBModule(private val suffix: String = "_test") {
  companion object {
    lateinit var db: HikariDataSource
    private const val dbName = "app"
  }

  fun createDataSource(): HikariDataSource = HikariDataSource(
      HikariConfig().apply {
        jdbcUrl = System.getenv("DATABASE_URL")
            ?: "jdbc:postgresql://${System.getenv("DB_HOST") ?: "localhost:65432"}/$dbName$suffix?user=$dbName&password=$dbName"
      }
  ).also {
    it.migrate(if (suffix == "_test") listOf("test", "test-data") else listOf("prod"))
  }


  val plugin: ApplicationPlugin<Any> by lazy {
    createApplicationPlugin("DB") {
      val hikariDataSource = createDataSource()
      db = hikariDataSource // should be some kind of DI here.
      on(MonitoringEvent(ApplicationStopped)) { db.close() }
    }
  }
}


fun DataSource.migrate(configs: List<String>) {
  connection.use { conn ->
    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(conn))
    val liquibase = Liquibase("db/db.xml", ClassLoaderResourceAccessor(), database)
    //liquibase.dropAll() //use this to start from scratch quickly
    liquibase.update(configs.joinToString(","))
  }
}
