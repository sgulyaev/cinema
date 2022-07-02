package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import di.DI
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

class DBModule(private val suffix: String = "_test") {
  companion object {
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
}

@Suppress("FunctionName")
fun DBModule.Plugin(di: DI): ApplicationPlugin<Any> = createApplicationPlugin("DB") {
  createDataSource()
      .also { hikariDataSource -> di.provide<DataSource>(hikariDataSource) }
      .also { hikariDataSource -> on(MonitoringEvent(ApplicationStopped)) { hikariDataSource.close() } }
}


fun DataSource.migrate(configs: List<String>) {
  connection.use { conn ->
    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(conn))
    val liquibase = Liquibase("db/db.xml", ClassLoaderResourceAccessor(), database)
    //liquibase.dropAll() //use this to start from scratch quickly
    liquibase.update(configs.joinToString(","))
  }
}
