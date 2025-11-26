package com.bol.demo.game

class Game {
    val players = listOf(Player(1), Player(2))
    val player1 = players[0]
    val player2 = players[1]

    init {
        setUp()
    }

    fun setUp() {
        player1.connectToOpponent(player2)
        player2.connectToOpponent(player1)
    }
}