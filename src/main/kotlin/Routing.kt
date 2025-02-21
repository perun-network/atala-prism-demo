package perun_network_threshold_ecdsa

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import perun_network_threshold_ecdsa.frontend.ServerHandler


fun Application.configureRouting() {
    routing {
        get("/") {
            try {
                val renderedHtml = ServerHandler.handleHost()
                call.respondText(renderedHtml, ContentType.Text.Html)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/alice") {
            try {
                val renderedHtml = ServerHandler.handleWallet()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get ("/signer") {
            try {
                val renderedHtml = ServerHandler.handleSigner()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/import") {
            try {
                val renderedHtml = ServerHandler.handleImport()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/verify") {
            try {
                val renderedHtml = ServerHandler.handleVerify()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        route("/api/call") {
            get("/verify") {

            }

            get("/sign") {

            }

            get("/import") {

            }

            get("/setup") {

            }
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
