package it.unibo.acdingnet.protelis.networmanager

import gnu.trove.list.array.TIntArrayList
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.mqttclientwrapper.MQTTClientSingleton
import it.unibo.mqttclientwrapper.MqttClientType
import it.unibo.protelisovermqtt.networkmanager.MQTTNetworkManager
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.CodePath
import org.protelis.vm.impl.DefaultTimeEfficientCodePath

class MQTTNetworkManagerTest: StringSpec() {

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        MQTTClientSingleton.ClientBuilder().build(MqttClientType.MOCK)
    }

    init {
        "the state should arrive to the neighbors" {
            val netId1 = StringUID("net1")
            val netId2 = StringUID("net2")
            val net1 = MQTTNetworkManager(netId1, MQTTClientSingleton.instance, "test", setOf(netId2))
            val net2 = MQTTNetworkManager(netId2, MQTTClientSingleton.instance, "test", setOf(netId1))

            val state = mapOf<CodePath, Int>(DefaultTimeEfficientCodePath(TIntArrayList()) to 0)
            net1.shareState(state)

            net2.neighborState[netId1] shouldBe state
        }
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        super.afterSpecClass(spec, results)
        MQTTClientSingleton.destruct()
    }
}