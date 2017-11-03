package mc.shane.games.airbaron

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.annotation.Id
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.persistence.GeneratedValue

@SpringBootApplication
class AirBaronApplication

fun main(args: Array<String>) {
    SpringApplication.run(AirBaronApplication::class.java, *args)
}

@RestController
class HubInfo @Autowired constructor (val hubRepo : HubRepository) {
    @GetMapping("/hub/search")
    @ResponseBody
    fun getHubByName(@RequestParam("name") name : String) =
            hubRepo.findByName(name)
}

@RestController
class PlayerInfo @Autowired constructor (val playerRepo : PlayerRepository) {
    @GetMapping("/player/search")
    @ResponseBody
    fun getPlayerByName(@RequestParam("name") name : String) =
            playerRepo.findByName(name)
}

@Repository
interface HubRepository : Neo4jRepository<Hub,Long> {
    fun findByName(name : String) : Hub?
}

@Repository
interface SpokeRepository : Neo4jRepository<Spoke,Long> {
    fun findByName(name : String) : Spoke?
}

@NodeEntity
open class Hub() {

    constructor(name : String, ownedValue : Int,
                controlledValue : Int, maxJets : Int,
                baseMarketShare : Int) : this() {
        this.name = name
        this.ownedValue = ownedValue
        this.controlledValue = controlledValue
        this.maxJets = maxJets
        this.baseMarketShare = baseMarketShare
    }
    @Id
    @GeneratedValue
    var id: Long? = null

    @Relationship(type="CONNECTED_TO",direction=Relationship.UNDIRECTED)
    var connectedHubs : List<Hub>? = null

    @Relationship(type="HUB_FOR",direction=Relationship.OUTGOING)
    var spokes : List<Spoke>? = null

    var name : String = ""
    var ownedValue : Int = 0
    var controlledValue : Int = 0
    var maxJets : Int = 0
    var baseMarketShare : Int = 0
}

@NodeEntity
open class Spoke() {

    constructor(name : String, price : Int) : this() {
        this.name = name
        this.price = price
    }
    @Id
    @GeneratedValue
    var id : Long? = null
    var name : String = ""
    var price : Int = 0
}

@Component
class Startup @Autowired constructor(
        val hubRepo : HubRepository,
        val spokeRepository: SpokeRepository,
        val playerRepo : PlayerRepository
    ): CommandLineRunner {
    val hubMap : MutableMap<String,Hub> = mutableMapOf()
    val spokeMap : MutableMap<String,Spoke> = mutableMapOf()

    override fun run(vararg args: String?) {
        if (!hubRepo.findAll().any()) {
            setupHubs()
        } else {
            hubRepo.findAll().forEach {
                hubMap.put(it.name, it)
            }
        }
        if (!spokeRepository.findAll().any()) {
            setupSpokes()
            spokeToHubs()
        }
    }

    private fun setupSpokes() {
        createSpoke("Las Vegas", 7)
        createSpoke("San Diego", 5)
        createSpoke("Honolulu", 8)
        createSpoke("Portland", 3)
        createSpoke("Reno", 1)
        createSpoke("Sacramento", 2)
        createSpoke("Anchorage", 1)
        createSpoke("Seattle", 7)
        createSpoke("Tucson", 1)
        createSpoke("Albuquerque", 2)
        createSpoke("El Paso", 2)
        createSpoke("Salt Lake City", 5)
        createSpoke("Omaha", 1)
        createSpoke("Kansas City", 3)
    }

    private fun spokeToHubs() {
        addSpokesTo("Phoenix", listOf("Tucson", "Albuquerque", "El Paso"))
        addSpokesTo("Los Angeles", listOf("Honolulu", "Las Vegas", "San Diego"))
        addSpokesTo("San Francisco", listOf("Reno", "Sacramento", "Anchorage", "Seattle", "Portland"))
        addSpokesTo("Denver", listOf("Omaha", "Salt Lake City", "Kansas City"))
    }

    private fun addSpokesTo(s: String, spokes: List<String>) {
        val hub = hubMap[s] ?: throw RuntimeException("Bad hub name $s")
        hub.spokes = spokes.map {
            spokeMap[it] ?: throw RuntimeException("Bad spoke name $it")
        }
        hubRepo.save(hub)
    }

    private fun createSpoke(name : String, price : Int) {
        spokeMap.put(name, spokeRepository.save(Spoke(name, price)))
    }

    private fun setupHubs() {
        var hub = Hub("Phoenix", 5, 10,1,20)
        hubMap.putAll(createHub(hub))
        hub = Hub("Los Angeles", 12, 24,3,50)
        hub.connectedHubs = connections(listOf("Phoenix"))
        hubMap.putAll(createHub(hub))
        hub = Hub("San Francisco", 7, 15,2,40)
        hub.connectedHubs = connections(listOf("Los Angeles"))
        hubMap.putAll(createHub(hub))
        hub = Hub("Denver", 5, 11, 1, 20)
        hub.connectedHubs = connections(listOf("Los Angeles", "San Francisco", "Phoenix"))
        hubMap.putAll(createHub(hub))
    }

    private fun connections(connections : List<String>) : List<Hub>? =
        connections.map { hubMap.getOrDefault(it,Hub()) }
                .filter { it.name != "" }

    private fun createHub(hub : Hub) : Map<String,Hub> =
            mapOf(Pair(hub.name,hubRepo.save(hub)))
}