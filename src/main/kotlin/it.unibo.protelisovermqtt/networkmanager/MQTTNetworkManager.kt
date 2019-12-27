package it.unibo.protelisovermqtt.networkmanager

import it.unibo.mqttclientwrapper.MQTTClientSingleton
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import it.unibo.protelisovermqtt.util.Topics
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.CodePath
import org.protelis.vm.NetworkManager
import java.io.Serializable

open class MQTTNetworkManager(
    val deviceUID: StringUID,
    protected var mqttClient: MqttClientBasicApi = MQTTClientSingleton.instance,
    protected val applicationEUI: String,
    private var neighbors: Set<StringUID> = emptySet()): NetworkManager {

    private var messages: Map<DeviceUID, Map<CodePath, Any>> = emptyMap()

    init {
        neighbors.forEach{subscribeToMqtt(it)}
    }

    protected fun subscribeToMqtt(deviceUID: StringUID) {
        mqttClient.subscribe(this, Topics.nodeStateTopic(applicationEUI, deviceUID), MessageState::class.java) { _, message ->
            messages += deviceUID to message.payload
        }
    }

    override fun shareState(toSend: Map<CodePath, Any>): Unit = mqttClient.publish(Topics.nodeStateTopic(applicationEUI, deviceUID), MessageState(toSend))

    override fun getNeighborState(): Map<DeviceUID, Map<CodePath, Any>> = messages.apply { messages = emptyMap() }

    fun setNeighbors(neighbors: Set<StringUID>) {
        //remove sensor not more neighbors
        this.neighbors.filter { !neighbors.contains(it) }.forEach{mqttClient.unsubscribe(this, Topics.nodeStateTopic(applicationEUI, it))}
        //add new neighbors
        neighbors.filter { !this.neighbors.contains(it) }.forEach{subscribeToMqtt(it)}
        this.neighbors = neighbors
    }

    protected fun getNeighbors() = neighbors

    private data class MessageState(val payload: Map<CodePath, Any>): Serializable, MqttMessageType
}