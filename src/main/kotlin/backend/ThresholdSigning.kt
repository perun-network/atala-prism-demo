package perun_network_threshold_ecdsa.backend

import mu.KotlinLogging
import perun_network.ecdsa_threshold.ecdsa.Point
import perun_network.ecdsa_threshold.ecdsa.PublicKey
import perun_network.ecdsa_threshold.precomp.PublicPrecomputation
import perun_network.ecdsa_threshold.sign.Signer
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
    private val documents = mutableListOf<String>()

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

    fun addDocument(newDoc : String) {
        documents.add(newDoc)
    }
}

