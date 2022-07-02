package app

import db.DBModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
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
  install(DBModule(if (System.getProperty("app.env") == "prod") "" else "_test").plugin)

  routing {
    addController(CinemaController(CinemaRepository()))
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

  routing {
    get("/api/health") {
      call.respond(HealthResponse())
    }
  }
}