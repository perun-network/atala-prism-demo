package perun_network.threshold_ecdsa_demo.backend

import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.kotlincrypto.hash.sha2.SHA256
import perun_network.ecdsa_threshold.ecdsa.*
import perun_network.ecdsa_threshold.precomp.PublicPrecomputation
import perun_network.ecdsa_threshold.sign.aux.AuxRound1Broadcast
import perun_network.ecdsa_threshold.sign.aux.AuxRound2Broadcast
import perun_network.ecdsa_threshold.sign.aux.AuxRound3Broadcast
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound1Broadcast
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound2Broadcast
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound3Broadcast
import perun_network.ecdsa_threshold.sign.presign.PresignRound1Broadcast
import perun_network.ecdsa_threshold.sign.presign.PresignRound2Broadcast
import perun_network.ecdsa_threshold.sign.presign.PresignRound3Broadcast

object ThresholdSigning {
    val signers = mutableMapOf<Int, BackendSigner>()
    var selectedSigners = mapOf<Int, BackendSigner>()
    val logger = KotlinLogging.logger {}
    var publicKey : PublicKey? = null
    var ssid : ByteArray? = null
    var bigR : Point? = null
    private lateinit var document : String
    private lateinit var signature : Signature

    fun keyGen() {
        val partyIds = signers.keys.toList()
        // KEYGEN ROUND 1
        val keygenRound1AllBroadcasts = mutableMapOf<Int, Map<Int, KeygenRound1Broadcast>>()
        for (i in partyIds) {
            keygenRound1AllBroadcasts[i] = signers[i]!!.thresholdSigner.keygenRound1(partyIds)
        }

        // KEYGEN ROUND 2
        val keygenRound2AllBroadcasts = mutableMapOf<Int, Map<Int, KeygenRound2Broadcast>>()
        for (i in partyIds) {
            keygenRound2AllBroadcasts[i] = signers[i]!!.thresholdSigner.keygenRound2(partyIds)
        }

        // KEYGEN ROUND 3
        val keygenRound3AllBroadcasts = mutableMapOf<Int, Map<Int, KeygenRound3Broadcast>>()
        for (i in partyIds) {
            keygenRound3AllBroadcasts[i] = signers[i]!!.thresholdSigner.keygenRound3(partyIds, keygenRound1AllBroadcasts, keygenRound2AllBroadcasts)
        }

        // KEYGEN OUTPUT
        val publicPoints = mutableMapOf<Int, Point>()
        for (i in partyIds) {
            publicPoints[i] = signers[i]!!.thresholdSigner.keygenOutput(partyIds, keygenRound2AllBroadcasts, keygenRound3AllBroadcasts)
        }


        // Check all public Points
        val publicPoint = publicPoints[partyIds[0]]!!
        for (i in partyIds) {
            if (publicPoints[i] != publicPoint) throw IllegalStateException("Inconsistent public Key")
        }
    }

    fun auxInfo() : Map<Int, PublicPrecomputation> {
        val partyIds = signers.keys.toList()

        // AUX ROUND 1
        val auxRound1AllBroadcasts = mutableMapOf<Int, Map<Int, AuxRound1Broadcast>>()
        for (i in partyIds) {
            auxRound1AllBroadcasts[i] = signers[i]!!.thresholdSigner.auxRound1(partyIds)
        }

        // AUX ROUND 2
        val auxRound2AllBroadcasts = mutableMapOf<Int, Map<Int, AuxRound2Broadcast>>()
        for (i in partyIds) {
            auxRound2AllBroadcasts[i] = signers[i]!!.thresholdSigner.auxRound2(partyIds)
        }

        // AUX ROUND 3
        val auxRound3AllBroadcasts = mutableMapOf<Int, Map<Int, AuxRound3Broadcast>>()
        for (i in partyIds) {
            auxRound3AllBroadcasts[i] = signers[i]!!.thresholdSigner.auxRound3(partyIds, auxRound1AllBroadcasts, auxRound2AllBroadcasts)
        }

        // AUX OUTPUT
        val publicPrecomps = mutableMapOf<Int, Map<Int, PublicPrecomputation>>()
        for (i in partyIds) {
            publicPrecomps[i] = signers[i]!!.thresholdSigner.auxOutput(partyIds, auxRound2AllBroadcasts, auxRound3AllBroadcasts)
        }


        // Check all public Points
        val publicPrecomp = publicPrecomps[partyIds[0]]!!
        for (i in partyIds) {
            for (j in partyIds) {
                if (publicPrecomps[i]!![j]!! != publicPrecomp[j]) {
                    throw IllegalStateException("Inconsistent public precomputations of index $j from party $i.")
                }
            }
        }

        return publicPrecomp
    }

    fun presign() : Point {
        val signerIds = selectedSigners.keys.toList()

        // PRESIGN ROUND 1
        val presignRound1AllBroadcasts = mutableMapOf<Int, Map<Int, PresignRound1Broadcast>>()
        for (i in signerIds) {
            presignRound1AllBroadcasts[i] = signers[i]!!.thresholdSigner.presignRound1(signerIds)
        }

        // PRESIGN ROUND 2
        val presignRound2AllBroadcasts = mutableMapOf<Int, Map<Int, PresignRound2Broadcast>>()
        for (i in signerIds) {
            presignRound2AllBroadcasts[i] = signers[i]!!.thresholdSigner.presignRound2(signerIds, presignRound1AllBroadcasts)
        }

        // PRESIGN ROUND 3
        val presignRound3AllBroadcasts = mutableMapOf<Int, Map<Int, PresignRound3Broadcast>>()
        for (i in signerIds) {
            presignRound3AllBroadcasts[i] = signers[i]!!.thresholdSigner.presignRound3(signerIds, presignRound2AllBroadcasts)
        }

        // PRESIGN OUTPUT
        val bigRs = mutableMapOf<Int, Point>()
        for (i in signerIds) {
            bigRs[i] = signers[i]!!.thresholdSigner.presignOutput(signerIds, presignRound3AllBroadcasts)
        }

        // VERIFY OUTPUT CONSISTENCY
        val referenceBigR = bigRs[signerIds[0]]!!
        for (i in signerIds) {
            if (referenceBigR != bigRs[i]) throw IllegalStateException("Inconsistent public Key")
        }
        return referenceBigR
    }

    fun combinePartialSignatures(bigRString: String, firstPartialSignatureString: String, secondPartialSignatureString: String) : Signature {

        val bigR = Point.fromByteArray(bigRString.decodeHex())
        if (this.bigR != bigR) {
            logger.info("Inconsistent big R")
        }

        val r = bigR.xScalar()
        val firstPartialSignature = PartialSignature.fromByteArray(firstPartialSignatureString.decodeHex())
        val secondPartialSignature = PartialSignature.fromByteArray(secondPartialSignatureString.decodeHex())

        if (!firstPartialSignature.ssid.contentEquals(secondPartialSignature.ssid)) {
            logger.info("inconsistent ssid")
            throw IllegalStateException("Inconsistent ssid")
        }

        logger.info("Received signatures from: ${firstPartialSignature.id} and ${secondPartialSignature.id}")

        val partialSignatures = listOf(firstPartialSignature, secondPartialSignature)
        var sigma = Scalar.zero()
        for (partial in partialSignatures) {
            sigma = sigma.add(partial.sigmaShare)
        }

        val signature = Signature.newSignature(r, sigma)
        this.signature = signature
        val hash = SHA256().digest(document.toByteArray())

        if (!signature.verifySecp256k1(hash, publicKey!!)) {
            throw IllegalStateException("Inconsistent signature")
        }

        return signature
    }

    fun addDocument(newDoc : String) {
        val incomingDocJson: JsonElement = Json.parseToJsonElement(newDoc)
        this.document = normalizeJson(incomingDocJson)
    }

    fun getDocument(): String {
        return document
    }

    fun getSignature() : ByteArray {
        if (!::signature.isInitialized) {
            throw IllegalStateException("Signature is null")
        }
        return signature.toSecp256k1Signature()
    }

    fun verifySignature(signatureStr: String, publicKeyStr: String) : Boolean {
        val signature = signatureStr.decodeHex()
        val publicKey = PublicKey.newPublicKey(publicKeyStr.decodeHex())
        val hash = SHA256().digest(document.toByteArray())

        return Signature.fromSecp256k1Signature(signature).verifySecp256k1(hash, publicKey)
    }

    fun scalePrecomputation() : Pair<Point, Map<Int, PublicPrecomputation>> {
        val publicPoints = mutableMapOf<Int, Point>()
        val publicAllPrecomps = mutableMapOf<Int, Map<Int, PublicPrecomputation>>()
        for (i in selectedSigners.keys.toList()) {
            val (publicPrecomp, publicPoint) = selectedSigners[i]!!.thresholdSigner.scalePrecomputations(selectedSigners.keys.toList())
            publicPoints[i] = publicPoint
            publicAllPrecomps[i] = publicPrecomp
        }

        // Check output consistency
        val referencePoint = publicPoints[selectedSigners.keys.first()]!!
        val referencePrecomp = publicAllPrecomps[selectedSigners.keys.first()]!!
        for (i in selectedSigners.keys) {
            if (publicPoints[i] != referencePoint) throw IllegalStateException("Inconsistent public Key")
        }

        return referencePoint to referencePrecomp
    }

}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return this.lowercase().chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

// Helper function to normalize a JsonElement by sorting object keys.
fun normalizeJson(json: JsonElement): String {
    return when (json) {
        is JsonObject -> {
            val sortedEntries = json.entries.sortedBy { it.key }
            val normalizedEntries = sortedEntries.map { (key, value) ->
                "\"$key\":${normalizeJson(value)}"
            }
            "{${normalizedEntries.joinToString(",")}}"
        }
        is JsonArray -> {
            val normalizedItems = json.map { normalizeJson(it) }
            "[${normalizedItems.joinToString(",")}]"
        }
        is JsonPrimitive -> {
            // For primitives, use their JSON representation.
            json.toString()
        }
        else -> json.toString()
    }
}