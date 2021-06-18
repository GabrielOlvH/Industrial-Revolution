package me.steven.indrev.networks.client.node

interface ClientNodeInfo {
    val pos: Long
}

inline fun <reified T : ClientNodeInfo> ClientNodeInfo.to(): T {
    return this as T
}