package it.unibo.protelisovermqtt.executioncontext

import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import it.unibo.protelisovermqtt.model.LatLongPosition
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

abstract class MQTTPositionedExecutionContext(
    _deviceUID: StringUID,
    nodePosition: LatLongPosition,
    protected val mqttClient: MqttClientBasicApi,
    val netmgr: NetworkManager,
    val randomSeed: Int = 1,
    protected val execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : PositionedExecutionContext(_deviceUID, nodePosition, netmgr, randomSeed, execEnvironment) {

    protected fun <T : MqttMessageType> subscribeTopic(topic: String, type: Class<T>, consumer: (topic: String, message: T) -> Unit) =
        mqttClient.subscribe(this, topic, type, consumer)
}
