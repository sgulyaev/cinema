package db

import org.intellij.lang.annotations.Language
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

fun <T> DataSource.select(@Language("SQL") expr: String, where: Map<String, Any> = emptyMap(), mapper: ResultSet.() -> T): List<T> = withConnection {
  prepareStatement(expr).use { stmt ->
    stmt.executeQuery().map(mapper)
  }
}

fun <T> ResultSet.map(mapper: ResultSet.() -> T): List<T> = mutableListOf<T>().also {
  while (next()) it += mapper()
}

fun DataSource.exec(@Language("SQL") expr: String, values: Sequence<Any?> = emptySequence()): Int = withConnection {
  prepareStatement(expr).use { stmt ->
    stmt.setAll(values)
    stmt.executeUpdate()
  }
}

fun PreparedStatement.setAll(values: Sequence<Any?>) = values.forEachIndexed { i, v -> setObject(i + 1, v) }