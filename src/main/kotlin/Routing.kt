package perun_network.threshold_ecdsa_demo

// ecdsa_threshold
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kotlincrypto.hash.sha2.SHA256
import perun_network.ecdsa_threshold.precomp.generateSessionId
import perun_network.ecdsa_threshold.sign.Signer
import perun_network.threshold_ecdsa_demo.backend.BackendSigner
import perun_network.threshold_ecdsa_demo.backend.ThresholdSigning
import perun_network.threshold_ecdsa_demo.backend.response.NameResponse
import perun_network.threshold_ecdsa_demo.backend.response.PublicDataResponse
import perun_network.threshold_ecdsa_demo.backend.response.SignerDTO
import perun_network.threshold_ecdsa_demo.backend.response.SignerResponse
import perun_network.threshold_ecdsa_demo.frontend.ServerHandler

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
                val renderedHtml = ServerHandler.handleAlice()
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
            get("/signature") {
                if (backend.bigR == null || backend.publicKey == null) {
                    call.respond(HttpStatusCode.NotFound, "Signature data not available.")
                } else {
                    try {
                        val signature = backend.getSignature()
                        call.respond(
                            HttpStatusCode.OK, mapOf(
                                "signature" to signature.toHexString().uppercase(),
                                "publicKey" to backend.publicKey!!.value.toHexString().uppercase()
                            )
                        )
                    } catch (exc: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Signature data not available.")
                        return@get
                    }
                }
            }

            post("/verify") {
                // Expect JSON payload with "signature" and "publicKey"
                val formData = call.receive<Map<String, String>>()
                val signatureStr = formData["signature"]
                val publicKeyStr = formData["publicKey"]

                if (signatureStr == null || publicKeyStr == null) {
                    call.respond(HttpStatusCode.BadRequest, "Signature and Public Key must be provided.")
                    return@post
                }

                try {
                    val isValid = backend.verifySignature(signatureStr, publicKeyStr)
                    backend.logger.info("Signature verification: $isValid")
                    if (isValid) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Verification failed")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error during verification")
                }
            }

            post("/sign") {
                // Read the request body as JSON and deserialize it
                val formData = call.receive<Map<String, String>>()
                // Retrieve backend.signers' names.
                val bigR = formData["bigR"]
                val firstPartialSig = formData["firstPartialSignature"]
                val secondPartialSignature = formData["secondPartialSignature"]

                if (bigR == null || firstPartialSig == null || secondPartialSignature == null) {
                    call.respond(HttpStatusCode.BadRequest, "All fields must be provided.")
                    return@post
                }
                try {
                    val signature = backend.combinePartialSignatures(bigR, firstPartialSig, secondPartialSignature)
                    backend.logger.info("Generated the signature: ${signature.toSecp256k1Signature().toHexString().uppercase()}")
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
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

                if (signer1Name == signer2Name || signer3Name == signer1Name || signer2Name == signer3Name) {
                    call.respond(HttpStatusCode.BadRequest, "Signers must have different names.")
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
                backend.logger.info("Generated the ssid: ${backend.ssid!!.toHexString().uppercase()}")

                // Start key generation process.
                try {
                    backend.keyGen()
                    backend.logger.info("Key generation finished.")

                    // Auxiliary information process.
                    backend.auxInfo()
                    backend.logger.info("Auxiliary info finished.")

                    call.respond(HttpStatusCode.OK, SignerResponse(backend.ssid.toString()))
                } catch (e: Exception) {
                    backend.logger.error(e.toString())
                    call.respond(HttpStatusCode.BadRequest)
                }

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
                    backend.logger.info("Selected signers: ${backend.selectedSigners.keys}")



                    // Scale Secret/Public Precomputations
                    val (publicPoint, _) = backend.scalePrecomputation()
                    // Calculate the public key.
                    backend.publicKey = publicPoint.toPublicKey()
                    backend.logger.info {"Scaled precomputations finished."}

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

            get("/document") {
                try {
                    val document = backend.getDocument()
                    if (document.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "No document available.")
                        return@get
                    }
                    call.respond(HttpStatusCode.OK, document)
                } catch (e: Exception) {
                    backend.logger.error("Error fetching document: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve document.")
                }
            }

            get("/partial_sign") {
                val signerName = call.request.queryParameters["signerName"]
                val documentParam = call.request.queryParameters["document"]
                backend.logger.info("Received document: $documentParam and signer: $signerName")

                // Validate signer name
                if (signerName.isNullOrEmpty() || backend.selectedSigners.filter { it.value.name == signerName }.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid or missing signer name.")
                    return@get
                }

                // Validate document
                if (documentParam.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Document is required.")
                    return@get
                }

                try {
                    val storedDocumentStr = backend.getDocument()

                    // Compare the two JSON objects for equality.
                    if (storedDocumentStr != documentParam) {
                        backend.logger.info("Inconsistent document for signing")
                    }

                    backend.logger.info("Signing document for signer: $signerName")
                    val hash = SHA256().digest(storedDocumentStr.toByteArray())
                    backend.logger.info("Sign the hash: ${hash.toHexString()}")

                    // Perform signing operation.
                    val signer = backend.selectedSigners.filter { it.value.name == signerName }.values.first()
                    val signature = signer.thresholdSigner.partialSignMessage(hash)

                    call.respond(HttpStatusCode.OK, mapOf("signature" to signature.toByteArray().toHexString().uppercase()))
                } catch (e: Exception) {
                    backend.logger.error("Error signing document for $signerName: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to sign document.")
                }
            }
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}