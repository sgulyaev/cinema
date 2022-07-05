package db

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.*
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
class JdbcSugarTest : DBTest() {
  val table = "jdbcTestTable"

  @BeforeAll fun createTable() {
    db.exec("create table $table(id int, data varchar)")
  }

  @AfterAll fun dropTable() {
    db.exec("drop table $table")
  }

  @Test
  fun `basic queries`() {
    assertEquals(0, db.select("select count(*) from $table") { getInt(1) }.first())
    db.exec("insert into $table values (?), (?)", listOf(1, 2))
    assertEquals(2, db.select("select count(*) from $table") { getInt(1) }.first())
  }
}