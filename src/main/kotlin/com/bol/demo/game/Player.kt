package com.bol.demo.game

data class Player(
    val id: Int,
) {
    val smallPits: List<Pit> = List(6) {
        Pit(capacity = 6)
    }
    val largePit = Pit()
}