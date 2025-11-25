package com.bol.demo.game

data class Player(
    val id: Int,
) {
    val pits: List<Pit> = List(7) {
        Pit()
    }
}