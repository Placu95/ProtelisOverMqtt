package it.unibo.acdingnet.protelis.neighborhood

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.mqttclientwrapper.MQTTClientSingleton
import it.unibo.mqttclientwrapper.MqttClientType
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.neighborhood.NeighborhoodManager
import it.unibo.protelisovermqtt.networkmanager.MQTTNetMgrWithMQTTNeighborhoodMgr
import org.protelis.lang.datatype.impl.StringUID

class MQTTNetMgrWithMQTTNeighborhoodMgrSpy(
    deviceUID: StringUID,
    mqttClient: MqttClientBasicApi,
    applicationEUI: String,
    initialPosition: LatLongPosition
) : MQTTNetMgrWithMQTTNeighborhoodMgr(deviceUID, mqttClient, applicationEUI, initialPosition) {

    fun getNodeNeighbors() = getNeighbors()
}

/**
 * Integration test between MQTTNetMgrWithMQTTNeighborhoodMgr and NeighborhoodManager
 */
class NeighborhoodIntegrationTest : StringSpec() {

    private val uid1 = StringUID("net1")
    private val uid2 = StringUID("net2")
    private val position1 = LatLongPosition.zero()
    private val neighborToPosition1 = LatLongPosition(0.0001, 0.0)
    private val notNeighborToPosition1 = LatLongPosition(100.0, 100.0)
    private val applicationID = "test"

    private fun getNeighborhoodManager() = NeighborhoodManager(applicationID, MQTTClientSingleton.instance, 50.0)

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        MQTTClientSingleton.ClientBuilder().build(MqttClientType.MOCK_SERIALIZATION)
    }

    init {
        "if there is only one node, it should not have any neighbor" {
            val neighborhoodManager = getNeighborhoodManager()
            neighborhoodManager.neighborhood.keys.size shouldBe 0

            val net = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid1, MQTTClientSingleton.instance, applicationID, position1)

            neighborhoodManager.neighborhood.keys.size shouldBe 1
            neighborhoodManager.neighborhood.filter { it.value.isEmpty() }.count() shouldBe 1
            net.getNodeNeighbors().isEmpty() shouldBe true
        }

        "two nodes with distance lower than range should be neighbors" {
            val neighborhoodManager = getNeighborhoodManager()
            neighborhoodManager.neighborhood.keys.size shouldBe 0

            val net1 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid1, MQTTClientSingleton.instance, applicationID, position1)
            val net2 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid2, MQTTClientSingleton.instance, applicationID, neighborToPosition1)

            neighborhoodManager.neighborhood.keys.size shouldBe 2
            neighborhoodManager.neighborhood.filter { it.value.size == 1 }.count() shouldBe 2
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid1.uid && it.value.all { it.uid.uid == uid2.uid } }.count() shouldBe 1
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid2.uid && it.value.all { it.uid.uid == uid1.uid } }.count() shouldBe 1
            net1.getNodeNeighbors().map { it.uid } shouldContain uid2.uid
            net2.getNodeNeighbors().map { it.uid } shouldContain uid1.uid
        }

        "two nodes with distance lower than range should not be neighbors" {
            val neighborhoodManager = getNeighborhoodManager()
            neighborhoodManager.neighborhood.keys.size shouldBe 0

            val net1 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid1, MQTTClientSingleton.instance, applicationID, position1)
            val net2 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid2, MQTTClientSingleton.instance, applicationID, notNeighborToPosition1)

            neighborhoodManager.neighborhood.keys.size shouldBe 2
            neighborhoodManager.neighborhood.filter { it.value.size == 0 }.count() shouldBe 2
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid1.uid && it.value.none { it.uid.uid == uid2.uid } }.count() shouldBe 1
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid2.uid && it.value.none { it.uid.uid == uid1.uid } }.count() shouldBe 1
            net1.getNodeNeighbors().map { it.uid }.isEmpty() shouldBe true
            net2.getNodeNeighbors().map { it.uid }.isEmpty() shouldBe true
        }

        "when a node leave the system, the neighborhoodManager should delete the node" {
            val neighborhoodManager = getNeighborhoodManager()
            neighborhoodManager.neighborhood.keys.size shouldBe 0

            val net1 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid1, MQTTClientSingleton.instance, applicationID, position1)
            val net2 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid2, MQTTClientSingleton.instance, applicationID, neighborToPosition1)
            net1.getNodeNeighbors().map { it.uid } shouldContain uid2.uid
            net2.nodeDeleted()

            neighborhoodManager.neighborhood.keys.size shouldBe 1
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid1.uid && it.value.isEmpty() }.count() shouldBe 1
            net1.getNodeNeighbors().map { it.uid }.isEmpty() shouldBe true
        }

        "when a node's neighbors change position and move outside the range of the node should exit from the neighborhood" {
            val neighborhoodManager = getNeighborhoodManager()
            neighborhoodManager.neighborhood.keys.size shouldBe 0

            val net1 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid1, MQTTClientSingleton.instance, applicationID, position1)
            val net2 = MQTTNetMgrWithMQTTNeighborhoodMgrSpy(uid2, MQTTClientSingleton.instance, applicationID, neighborToPosition1)

            neighborhoodManager.neighborhood.keys.size shouldBe 2
            neighborhoodManager.neighborhood.filter { it.value.size == 1 }.count() shouldBe 2
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid1.uid && it.value.all { it.uid.uid == uid2.uid } }.count() shouldBe 1
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid2.uid && it.value.all { it.uid.uid == uid1.uid } }.count() shouldBe 1
            net1.getNodeNeighbors().map { it.uid } shouldContain uid2.uid
            net2.getNodeNeighbors().map { it.uid } shouldContain uid1.uid

            net2.changePosition(notNeighborToPosition1)

            neighborhoodManager.neighborhood.keys.size shouldBe 2
            neighborhoodManager.neighborhood.filter { it.value.size == 0 }.count() shouldBe 2
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid1.uid && it.value.none { it.uid.uid == uid2.uid } }.count() shouldBe 1
            neighborhoodManager.neighborhood.filter { it.key.uid.uid == uid2.uid && it.value.none { it.uid.uid == uid1.uid } }.count() shouldBe 1
            net1.getNodeNeighbors().map { it.uid }.isEmpty() shouldBe true
            net2.getNodeNeighbors().map { it.uid }.isEmpty() shouldBe true
        }
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        super.afterSpecClass(spec, results)
        MQTTClientSingleton.destruct()
    }
}