package perun_network_threshold_ecdsa.backend

import perun_network.ecdsa_threshold.ecdsa.Point
import perun_network.ecdsa_threshold.sign.Signer
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound1Broadcast
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound2Broadcast
import perun_network.ecdsa_threshold.sign.keygen.KeygenRound3Broadcast

class BackendSigner (
    val name: String,
    val thresholdSigner : Signer,
)


