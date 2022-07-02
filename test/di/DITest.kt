package di

import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test
import org.slf4j.Logger
import java.awt.Robot

class DITest {
  private val log = mockk<Logger>(relaxed = true)
  private val di = DI(log)

  @Test
  fun `create simple class without dependency`() {
    assertThat(di.require(Simple::class)).isInstanceOf(Simple::class.java)
    assertThat(di.require<Simple>()).isInstanceOf(Simple::class.java)
  }

  @Test
  fun `create class with dependency`() {
    val bean = di.require<WithDep>()
    assertThat(bean).isInstanceOf(WithDep::class.java)
    assertThat(bean.dep).isInstanceOf(Simple::class.java)
  }

  @Test
  fun `log creation of beans`() {
    di.require<WithDep>()
    verify {
      log.info(contains("Auto-created Simple"))
      log.info(contains("Auto-created WithDep[Simple]"))
    }
  }

  @Test
  fun `required bean should be singleton`() {
    assertThat(di.require<Simple>()).isSameAs(di.require<Simple>())
  }

  @Test
  fun `on require should return provided instance if any`() {
    val handMade = TheImpl()
    di.provide<TheInterface>(handMade)
    assertThat(di.require<TheInterface>()).isSameAs(handMade)
  }

  @Test
  fun `meaningful error message when instance is not provided and cannot be created`() {
    assertThatThrownBy { (di.require<TheInterface>()) }
      .message().contains("di.TheInterface has no constructor. Should be provided before require.")
  }

  @Test
  fun `provide robot, this is the case where there is no primary constructor`() {
    assertThat(di.require<Robot>()).isInstanceOf(Robot::class.java)
  }
}

private class Simple
private class WithDep(val dep: Simple)
private class TheImpl : TheInterface
private interface TheInterface

fun MockKMatcherScope.contains(expected: String) = match(object : Matcher<String> {
  override fun match(arg: String?) = arg?.contains(expected) ?: false
  override fun toString(): String = "contains: $expected"
})

