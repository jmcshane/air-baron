package mc.shane.games.airbaron

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.springframework.data.annotation.Id
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.persistence.GeneratedValue

@NodeEntity
class Game {
    @Id
    @GeneratedValue
    var id : Long? = null

    var name : String = ""
}

@Repository
interface GameRepository : Neo4jRepository<Game, Long> {
    fun findByName(name : String) : Game?
}

@Repository
interface PlayerRepository : Neo4jRepository<Player, Long> {
    fun findByName(name : String) : Player?

    @Query("MATCH (h:Player)-[:PLAYING]->(g:Game) WHERE ID(g)={gameId} RETURN h")
    fun getPlayersForGame(@Param("gameId") gameId : Long) : List<Player>

    @Query("MATCH (h:Player)-[:PLAYING]->(g:Game) WHERE ID(h)={playerId} RETURN g LIMIT 1")
    fun getGameFromPlayer(@Param("playerId") playerId : Long) : Game
}

@NodeEntity
open class Player() {

    constructor(name : String, game : Game) : this() {
        this.name = name
        this.game = game
        this.color = color
    }
    @Id
    @GeneratedValue
    var id : Long? = null
    var name: String = ""
    var color : String = ""
    var fareWarsState : Boolean = false
    var loanAmount : Int = 0
    var hasGovernmentContract : Boolean = false
    var moneyAmount : Int = 0

    @Relationship(type="PLAYING",direction= Relationship.UNDIRECTED)
    lateinit var game : Game
}

