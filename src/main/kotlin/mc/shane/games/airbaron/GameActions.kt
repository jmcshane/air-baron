package mc.shane.games.airbaron

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameActions @Autowired constructor(
        val playerRepository: PlayerRepository,
        val spokeRepository: SpokeRepository
) {
    @PostMapping("/purchase")
    @ResponseBody
    fun attemptPurchase(@RequestParam("playerId") playerId : Long,
                        @RequestParam("spokeId") spokeId : Long) {

    }
}