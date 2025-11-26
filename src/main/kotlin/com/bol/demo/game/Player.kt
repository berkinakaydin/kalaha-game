package com.bol.demo.game

data class Player(
    val id: Int,
) {
    val smallPits: List<Pit> = List(6) {
        Pit(capacity = 6)
    }
    val largePit = Pit()

    init {
        for (i in 0 until smallPits.size - 1) {
            smallPits[i].next = smallPits[i + 1]
        }
        smallPits.last().next = largePit
    }

    fun connectToOpponent(opponent: Player) {
        largePit.next = opponent.smallPits.first()
    }
}