package mc.shane.games.airbaron

import java.util.Random

fun getResultFromTwoDieRoll(rand : Random) =
        rand.nextInt(6) + rand.nextInt(6) + 2

