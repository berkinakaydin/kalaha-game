package com.bol.demo

import com.bol.demo.game.Game
import com.bol.demo.game.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class GameTest {
    private lateinit var game: Game
    private lateinit var player1: Player
    private lateinit var player2: Player

    @BeforeEach
    fun setUp(){
        game = Game()
        player1 = game.player1
        player2 = game.player2
    }

    @Test
    fun `a game should contain two players`() {
        val numberOfPlayers = 2

        assertThat(game.players.size).isEqualTo(numberOfPlayers)
    }

    @Test
    fun `each player should have 6 small pits and one big pit`() {
        val numberOfSmallPits = 6

        assertThat(player1.smallPits.size).isEqualTo(numberOfSmallPits)
        assertNotNull(player1.largePit)
    }

    @Test
    fun `each small pits should contain 6 stones`() {
        val numberOfStones = 6

        player1.smallPits.forEach { pit ->
            assertThat(pit.capacity).isEqualTo(numberOfStones)
        }
        assertThat(player1.largePit.capacity).isEqualTo(0)
    }

    @Test
    fun `each pits should be connected`() {
        assertSoftly {
            for (i in 0 until player1.smallPits.size - 1) {
                val currentPit = player1.smallPits[i]
                val nextPit = player1.smallPits[i + 1]

                it.assertThat(currentPit.next).isEqualTo(nextPit)
            }
            it.assertThat(player1.largePit).isEqualTo(player1.smallPits.last().next)
        }
    }

    @Test
    fun `each players should be connected`() {
        assertThat(player1.largePit.next).isEqualTo(player2.smallPits.first())
        assertThat(player2.largePit.next).isEqualTo(player1.smallPits.first())
    }

    @Test
    fun `when a player makes a move from first pit, stones should be sowed`(){
        val chosenPitIndex = 0
        var currentPit = player1.smallPits[chosenPitIndex]
        val numberOfStones = currentPit.capacity

        val oldStateOfPlayer = player1.copy()
        var oldStateOfCurrentPit = oldStateOfPlayer.smallPits[chosenPitIndex]

        game.makeMove(chosenPitIndex)
        assertThat(player1.smallPits[chosenPitIndex].capacity).isEqualTo(0)

        repeat(numberOfStones){
            val newPit = currentPit.next
            val oldPit = oldStateOfCurrentPit.next
            assertThat(newPit.capacity).isEqualTo(oldPit.capacity + 1)
            currentPit = newPit
            oldStateOfCurrentPit = oldPit
        }
    }

    @Test
    fun `while sowing the stones, stone should not be placed in opponents large pit`(){
        val chosenPitIndex = 0
        val currentPit = player1.smallPits[chosenPitIndex]
        currentPit.capacity = 13 //when it is at least 13, that means opponent's pits get stones

        val oldStateOfOpponentPlayer = player2.copy()

        game.makeMove(chosenPitIndex)
        assertThat(player2.largePit.capacity).isEqualTo(oldStateOfOpponentPlayer.largePit.capacity)
    }

    @Test
    fun `while sowing the stones, current players pits might be changed more than once`(){
        val chosenPitIndex = 0
        val currentPit = player1.smallPits[chosenPitIndex]
        currentPit.capacity = 36 //when it is at least 18, that means opponent's pits get stones

        game.makeMove(chosenPitIndex)

        assertThat(currentPit.capacity).isEqualTo(2)
    }
}