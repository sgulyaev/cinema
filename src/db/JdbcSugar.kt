package db

import org.intellij.lang.annotations.Language
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

fun <T> DataSource.select(@Language("SQL") expr: String, values: List<Any?> = emptyList(), mapper: ResultSet.() -> T): List<T> = withConnection {
  prepareStatement(expr).use { stmt ->
    stmt.setAll(values)
    stmt.executeQuery().map(mapper)
  }
}

fun <T> ResultSet.map(mapper: ResultSet.() -> T): List<T> = mutableListOf<T>().also {
  while (next()) it += mapper()
}

fun DataSource.exec(@Language("SQL") expr: String, values: List<Any?> = emptyList()): Int = withConnection {
  prepareStatement(expr).use { stmt ->
    stmt.setAll(values)
    stmt.executeUpdate()
  }
}

fun PreparedStatement.setAll(values: List<Any?>) = values.asSequence().forEachIndexed {
  i, v -> setObject(i + 1, v)
}