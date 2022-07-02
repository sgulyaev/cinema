package di

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class DI(private val log: Logger = LoggerFactory.getLogger("DI")) {
  private val singletons = mutableMapOf<KClass<*>, Any>()

  inline fun <reified T : Any> provide(singleton: T) = provide(T::class, singleton)

  fun <T : Any> provide(kClass: KClass<T>, singleton: T) {
    singletons[kClass] = singleton
  }

  inline fun <reified T : Any> require(): T = require(T::class)

  fun <T : Any> require(type: KClass<T>): T {
    singletons[type]?.let { @Suppress("UNCHECKED_CAST") return it as T }

    val constructor = type.primaryConstructor ?: type.constructors.minByOrNull { it.parameters.size }
    ?: error("$type has no constructor. Should be provided before require.")

    val args: Map<KParameter, Any> =
        constructor.parameters.filter { !it.isOptional }.associateWith { require(it.type.classifier as KClass<*>) }
    return constructor.apply { isAccessible = true }.callBy(args)
        .also { singletons[type] = it }
        .also { log.info("Auto-created ${type.simpleName}${args.values.map { it::class.simpleName }}") }
  }
}