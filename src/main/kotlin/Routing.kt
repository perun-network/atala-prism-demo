package perun_network_threshold_ecdsa

import com.typesafe.config.ConfigException.Null
import mu.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// backend
import perun_network_threshold_ecdsa.backend.*

// ecdsa_threshold
import perun_network.ecdsa_threshold.precomp.generateSessionId
import perun_network.ecdsa_threshold.precomp.publicKeyFromShares
import perun_network_threshold_ecdsa.backend.BackendSigner
import perun_network.ecdsa_threshold.sign.Signer
import perun_network_threshold_ecdsa.backend.response.NameResponse
import perun_network_threshold_ecdsa.backend.response.PublicDataResponse
import perun_network_threshold_ecdsa.backend.response.SignerDTO
import perun_network_threshold_ecdsa.backend.response.SignerResponse
import perun_network_threshold_ecdsa.frontend.ServerHandler
import java.nio.charset.Charset

import java.util.Base64

private val backend = ThresholdSigning

@OptIn(ExperimentalStdlibApi::class)
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

        get ("/faber") {
            try {
                val renderedHtml = ServerHandler.handleFaber()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/faber_select") {
            try {
                val renderedHtml = ServerHandler.handleFaberSelect()
                call.respondText(renderedHtml, contentType = ContentType.Text.Html)
            } catch (exc: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/faber_sign") {
            try {
                val renderedHtml = ServerHandler.handleFaberSigning()
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

            post("/setup") {
                // Read the raw document content sent from the frontend.
                val documentContent = call.receiveText()
                backend.logger.info("Received document for setup: $documentContent")
                // Here you can process, validate, or store the document as needed.

                backend.addDocument(documentContent)
                call.respond(HttpStatusCode.OK, "Document received")
            }

            get("/names") {
                if (backend.signers.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No backend.signers available.")
                    return@get
                }
                // Create a list of SignerDTOs from the in-memory backend.signers map.
                val signerList = backend.signers.toSortedMap().map { (id, backendSigner) ->
                    SignerDTO(id, backendSigner.name)
                }
                // Respond with the backend.signersResponse.
                call.respond(HttpStatusCode.OK, NameResponse(signerList))
            }

            get("/selected_signers") {
                if (backend.selectedSigners.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No backend.signers available.")
                    return@get
                }
                // Create a list of SignerDTOs from the in-memory backend.signers map.
                val signerList = backend.selectedSigners.toSortedMap().map { (id, backendSigner) ->
                    SignerDTO(id, backendSigner.name)
                }
                // Respond with the backend.signersResponse.
                call.respond(HttpStatusCode.OK, NameResponse(signerList))
            }

            get("/public_data") {
                if (backend.publicKey == null) {
                    call.respond(HttpStatusCode.NotFound, "No publicKey available.")
                }
                if (backend.ssid == null) {
                    call.respond(HttpStatusCode.NotFound, "No ssid available.")
                }

                if (backend.bigR == null) {
                    call.respond(HttpStatusCode.NotFound, "No BIG R available.")
                }

                val ssidString = backend.ssid!!.toHexString().uppercase()
                val publicKeyString = backend.publicKey!!.value.toHexString().uppercase()
                val bigRString = backend.bigR!!.toByteArray().toHexString().uppercase()

                call.respond(HttpStatusCode.OK, PublicDataResponse(ssidString, publicKeyString, bigRString))

            }

            post("/signers") {
                // Read the request body as JSON and deserialize it
                val formData = call.receive<Map<String, String>>()
                backend.logger.info("Received the form $formData")
                // Retrieve backend.signers' names.
                val signer1Name = formData["signer1"]
                val signer2Name = formData["signer2"]
                val signer3Name = formData["signer3"]

                if (signer1Name == null || signer2Name == null || signer3Name == null) {
                    call.respond(HttpStatusCode.BadRequest, "All signer names must be provided.")
                    return@post
                }

                backend.ssid = generateSessionId()

                backend.signers[1] = BackendSigner(
                    signer1Name,
                    Signer(
                        id = 1,
                        ssid = backend.ssid!!,
                        threshold = 2,
                    )
                )

                backend.signers[2] = BackendSigner(
                    signer2Name,
                    Signer(
                        id = 2,
                        ssid = backend.ssid!!,
                        threshold = 2
                    )
                )

                backend.signers[3] = BackendSigner(
                    signer3Name,
                    Signer(
                        id = 3,
                        ssid = backend.ssid!!,
                        threshold = 2
                    )
                )
                backend.logger.info("Generated the ssid: ${backend.ssid}")

                // Start key generation process.
                backend.keyGen()
                backend.logger.info("Key generation finished.")

                // Auxiliary information process.
                val publicPrecomps = backend.auxInfo()
                backend.logger.info("Auxiliary info finished.")

                // Calculate the public key.
                backend.publicKey = publicKeyFromShares(backend.signers.keys.toList(), publicPrecomps)

                call.respond(HttpStatusCode.OK, SignerResponse(backend.ssid.toString()))
            }

            post("select_signers") {
                try {
                    // Parse JSON request body
                    val requestBody = call.receive<Map<String, List<Int>>>()
                    val selectedSigners = requestBody["selectedSigners"]

                    // Validate input
                    if (selectedSigners == null || selectedSigners.size != 2) {
                        call.respond(HttpStatusCode.BadRequest, "Exactly two signers must be selected.")
                        return@post
                    }

                    // Validate that the signers exist
                    val validSigners = selectedSigners.all { backend.signers.containsKey(it) }
                    if (!validSigners) {
                        call.respond(HttpStatusCode.BadRequest, "One or more selected signers are invalid.")
                        return@post
                    }

                    // Store selected signers in the backend
                    backend.selectedSigners = backend.signers.filterKeys { it in selectedSigners}
                    backend.logger.info("Selected signers: $selectedSigners")

                    // Start pre-signing process.
                    backend.bigR = backend.presign()
                    backend.logger.info("Presigning finished.")

                    // Respond with success
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                } catch (e: Exception) {
                    backend.logger.error("Error selecting signers: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to process request.")
                }
            }

            // New endpoint for signing
            get("/signature") {
                val signerIdParam = call.request.queryParameters["signerId"]
                val signerId = signerIdParam?.toIntOrNull()
                if (signerId == null || !backend.signers.containsKey(signerId)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid or missing signerId.")
                    return@get
                }
                // Simulate signature generation.
                // In a real implementation, replace this with your signing logic.
                val signatureData = "signature-for-signer-$signerId".toByteArray()
                val base64Signature = Base64.getEncoder().encodeToString(signatureData)
                call.respond(HttpStatusCode.OK, mapOf("signature" to base64Signature))
            }
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
