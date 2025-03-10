package perun_network_threshold_ecdsa.backend.response

import kotlinx.serialization.Serializable

@Serializable
data class SignerResponse(
    val ssid: String,
)

@Serializable
data class SignerDTO(
    val id : Int,
    val name : String,
)

@Serializable
data class NameResponse(
    val signers: List<SignerDTO>
)

@Serializable
data class PublicDataResponse(
    val ssid: String,
    val publicKey: String,
    val bigR: String,
)