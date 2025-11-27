package com.bol.demo

import com.bol.demo.game.Game
import com.bol.demo.game.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class GameTest {
    private lateinit var game: Game
    private lateinit var player1: Player
    private lateinit var player2: Player

    @BeforeEach
    fun setUp() {
        game = Game()
        player1 = game.player1
        player2 = game.player2
    }

    @Nested
    @DisplayName("Game Initialization")
    inner class GameInitializationTest {
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
    }

    @Nested
    @DisplayName("Making Moves")
    inner class MakingMovesTest {
        @Test
        fun `when a player makes a move from first pit, stones should be sowed`() {
            val chosenPitIndex = 0
            var currentPit = player1.smallPits[chosenPitIndex]
            val numberOfStones = currentPit.capacity

            val oldStateOfPlayer = player1.copy()
            var oldStateOfCurrentPit = oldStateOfPlayer.smallPits[chosenPitIndex]

            game.play(chosenPitIndex)
            assertThat(player1.smallPits[chosenPitIndex].capacity).isEqualTo(0)

            repeat(numberOfStones) {
                val newPit = currentPit.next
                val oldPit = oldStateOfCurrentPit.next
                assertThat(newPit.capacity).isEqualTo(oldPit.capacity + 1)
                currentPit = newPit
                oldStateOfCurrentPit = oldPit
            }
        }

        @Test
        fun `while sowing the stones, stone should not be placed in opponents large pit`() {
            val chosenPitIndex = 0
            val currentPit = player1.smallPits[chosenPitIndex]
            currentPit.capacity = 13 //when it is at least 13, that means opponent's pits get stones

            val oldStateOfOpponentPlayer = player2.copy()

            game.play(chosenPitIndex)
            assertThat(player2.largePit.capacity).isEqualTo(oldStateOfOpponentPlayer.largePit.capacity)
        }

        @Test
        fun `while sowing the stones, current players pits might be changed more than once`() {
            val chosenPitIndex = 0
            val currentPit = player1.smallPits[chosenPitIndex]
            currentPit.capacity = 36 //when it is at least 18, that means opponent's pits get stones

            game.play(chosenPitIndex)

            assertThat(currentPit.capacity).isEqualTo(2)
        }

        @Test
        fun `if players last stone lands not in own big pit, then opponent gets the turn`(){
            val chosenPitIndex = 0
            val currentPit = player1.smallPits[chosenPitIndex]

            currentPit.capacity = 5
            game.play(chosenPitIndex)

            assertThat(game.currentPlayer).isEqualTo(player2)
        }

        @Test
        fun `if players last stone lands in own big pit, then current player gets another turn`(){
            val chosenPitIndex = 0
            val currentPit = player1.smallPits[chosenPitIndex]

            currentPit.capacity = 6
            game.play(chosenPitIndex)

            assertThat(game.currentPlayer).isEqualTo(game.currentPlayer)
        }

        @Test
        fun `if players last stone lands in own small pit, player captures stones from the opposite pit`(){
            // given
            val chosenPitIndex = 0
            val currentPit = player1.smallPits[chosenPitIndex]
            currentPit.capacity = 1

            val opponentsPit = player2.smallPits[6 - (chosenPitIndex + currentPit.capacity) - 1]
            val opponentsPitCapacity = opponentsPit.capacity

            // when
            val emptyPit = player1.smallPits[chosenPitIndex + currentPit.capacity]
            emptyPit.capacity = 0

            game.play(chosenPitIndex)

            // then
            assertThat(game.player1.largePit.capacity).isEqualTo(opponentsPitCapacity + 1)
            assertThat(opponentsPit.capacity).isEqualTo(0)
            assertThat(emptyPit.capacity).isEqualTo(0)
        }
    }

    @Test
    fun `end game`(){
        //given
        val chosenPitIndex = 5
        val currentPit = player1.smallPits[chosenPitIndex]
        currentPit.capacity = 2

        //when
        player1.smallPits.filter { it != currentPit }.map { it.capacity = 0 }
        player2.smallPits.map { it.capacity = 10 }
        player1.largePit.capacity = 50

        game.play(chosenPitIndex)

        //then
        assertThat(player1.largePit.capacity).isEqualTo(51)
        assertThat(player2.largePit.capacity).isEqualTo(61)
        assertThat(game.winner).isNotNull.isEqualTo(player2)
    }
}