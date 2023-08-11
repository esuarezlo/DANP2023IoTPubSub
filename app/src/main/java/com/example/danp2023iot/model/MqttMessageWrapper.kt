package com.example.danp2023iot.model

data class MqttMessageWrapper(
    val uid: String,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttMessageWrapper

        if (uid != other.uid) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
