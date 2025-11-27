package com.bol.demo.game

class Game {
    val player1 = Player(1)
    val player2 = Player(2)
    val players = listOf(player1, player2)

    var currentPlayer = player1
    val opponent = if (currentPlayer == player1) player2 else player1

    var winner: Player? = null

    init {
        setUp()
    }

    fun setUp() {
        player1.connectToOpponent(player2)
        player2.connectToOpponent(player1)
    }

    fun play(pitIndex: Int) {
        val currentPit = makeMove(pitIndex)

        checkEndGame()

        if (winner != null){
            return
        }

        if (currentPit != currentPlayer.largePit) {
            changePlayer()
        }
    }

    fun makeMove(pitIndex: Int): Pit {
        var currentPit = currentPlayer.smallPits[pitIndex]
        var numberOfStones = currentPit.capacity

        currentPit.capacity = 0

        //move
        while (numberOfStones > 0) {
            var nextPit = currentPit.next

            // Skip opponent's large pit
            if (nextPit == opponent.largePit) {
                nextPit = nextPit.next
            }

            nextPit.capacity += 1

            numberOfStones--
            currentPit = nextPit
        }

        // capture
        if (currentPit.capacity == 1 && currentPit in currentPlayer.smallPits) {
            val indexOfCurrentPit = currentPlayer.smallPits.indexOf(currentPit)

            val opponentsPit = opponent.smallPits[6 - indexOfCurrentPit - 1]
            currentPlayer.largePit.capacity += opponentsPit.capacity + 1

            currentPit.capacity = 0
            opponentsPit.capacity = 0
        }

        return currentPit
    }

    fun changePlayer() {
        val opponent = if (currentPlayer == player1) player2 else player1
        currentPlayer = opponent
    }

    fun checkEndGame() {
        if (currentPlayer.smallPits.all { it.capacity == 0 }) {
            opponent.largePit.capacity += opponent.smallPits.sumOf { it.capacity }
            opponent.smallPits.map { it.capacity = 0 }

            winner = if (currentPlayer.largePit.capacity > opponent.largePit.capacity) {
                currentPlayer
            } else {
                opponent
            }
        }
    }
}