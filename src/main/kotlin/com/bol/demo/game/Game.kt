package com.bol.demo.game

class Game {
    val player1 = Player(1)
    val player2 = Player(2)
    val players = listOf(player1, player2)

    var currentPlayer = player1
    var opponent = if (currentPlayer == player1) player2 else player1

    var winner: Player? = null
    var isFinished = false

    init {
        setUp()
    }

    fun setUp() {
        player1.connectToOpponent(player2)
        player2.connectToOpponent(player1)
    }

    fun play(pitIndex: Int): Result? {
        val currentPit = makeMove(pitIndex)

        isFinished = checkEndGame()

        if (isFinished){
            winner = if (currentPlayer.largePit.capacity > opponent.largePit.capacity) {
                currentPlayer
            } else if(currentPlayer.largePit.capacity < opponent.largePit.capacity) {
                opponent
            } else{
                null
            }

            return when(winner){
                player1 -> Result.PLAYER_1_WON
                player2 -> Result.PLAYER_2_WON
                else -> Result.TIE
            }
        }

        if (currentPit != currentPlayer.largePit) {
            changePlayer()
        }

        return null
    }

    fun makeMove(pitIndex: Int): Pit {
        validateInput(pitIndex)
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

    private fun validateInput(pitIndex: Int){
        require(pitIndex in 0..5){
            "Invalid pit index"
        }
        require(currentPlayer.smallPits[pitIndex].capacity > 0){
            "Pit is empty"
        }

        check(!isFinished){
            "Game is already over"
        }
    }

    private fun changePlayer() {
        val opponent = if (currentPlayer == player1) player2 else player1
        val tempPlayer = currentPlayer
        currentPlayer = opponent
        this.opponent = tempPlayer
    }

    private fun checkEndGame() : Boolean {
        if (currentPlayer.smallPits.all { it.capacity == 0 }) {
            opponent.largePit.capacity += opponent.smallPits.sumOf { it.capacity }
            opponent.smallPits.map { it.capacity = 0 }

            return true
        }
        return false
    }

    enum class Result{
        PLAYER_1_WON,
        PLAYER_2_WON,
        TIE
    }
}