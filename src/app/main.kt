package app

import db.DBModule
import db.Plugin
import di.DI
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
  System.setProperty("app.env", "prod")
  io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.main() {
  val di = DI()
  install(DBModule(if (System.getProperty("app.env") == "prod") "" else "_test").Plugin(di))

  routing {
    addController(di.require<CinemaController>())
  }
}

@Suppress("unused")
fun Application.common() {
  install(ContentNegotiation) {
    json()
  }

  install(StatusPages) {
    exception<Throwable> { call, cause ->
      call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
    }
  }
  install(CallLogging)

  routing {
    get("/api/health") {
      call.respond(HealthResponse())
    }
  }
}