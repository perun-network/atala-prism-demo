package perun_network.threshold_ecdsa_demo.backend

import perun_network.ecdsa_threshold.sign.Signer

class BackendSigner (
    val name: String,
    val thresholdSigner : Signer,
)


