package com.bol.demo

import com.bol.demo.game.Game
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class GameTest {
    private lateinit var game: Game

    @BeforeEach
    fun setUp() {
        game = Game()
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

            assertThat(game.currentPlayer.smallPits.size).isEqualTo(numberOfSmallPits)
            assertNotNull(game.currentPlayer.largePit)
        }

        @Test
        fun `each small pits should contain 6 stones`() {
            val numberOfStones = 6

            game.currentPlayer.smallPits.forEach { pit ->
                assertThat(pit.capacity).isEqualTo(numberOfStones)
            }
            assertThat(game.currentPlayer.largePit.capacity).isEqualTo(0)
        }

        @Test
        fun `each pits should be connected`() {
            assertSoftly {
                for (i in 0 until game.currentPlayer.smallPits.size - 1) {
                    val currentPit = game.currentPlayer.smallPits[i]
                    val nextPit = game.currentPlayer.smallPits[i + 1]

                    it.assertThat(currentPit.next).isEqualTo(nextPit)
                }
                it.assertThat(game.currentPlayer.largePit).isEqualTo(game.currentPlayer.smallPits.last().next)
            }
        }

        @Test
        fun `each players should be connected`() {
            assertThat(game.currentPlayer.largePit.next).isEqualTo(game.opponent.smallPits.first())
            assertThat(game.opponent.largePit.next).isEqualTo(game.currentPlayer.smallPits.first())
        }
    }

    @Nested
    @DisplayName("Making Moves")
    inner class MakingMovesTest {
        @Test
        fun `when a player makes a move from first pit, stones should be sowed`() {
            val chosenPitIndex = 0
            var currentPit = game.currentPlayer.smallPits[chosenPitIndex]
            val numberOfStones = currentPit.capacity

            val oldStateOfPlayer = game.currentPlayer.copy()
            var oldStateOfCurrentPit = oldStateOfPlayer.smallPits[chosenPitIndex]

            game.play(chosenPitIndex)
            assertThat(game.currentPlayer.smallPits[chosenPitIndex].capacity).isEqualTo(0)

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
            val currentPlayer = game.currentPlayer
            val currentPit = currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 13 //when it is at least 13, that means opponent's pits get stones

            val opponent = game.opponent
            val oldStateOfOpponentPlayer = opponent.copy()

            game.play(chosenPitIndex)
            assertThat(opponent.largePit.capacity).isEqualTo(oldStateOfOpponentPlayer.largePit.capacity)
        }

        @Test
        fun `while sowing the stones, current players pits might be changed more than once`() {
            val chosenPitIndex = 0
            val currentPit = game.currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 36 //when it is at least 18, that means opponent's pits get stones

            game.play(chosenPitIndex)

            assertThat(currentPit.capacity).isEqualTo(2)
        }

        @Test
        fun `if players last stone lands not in own big pit, then opponent gets the turn`() {
            val chosenPitIndex = 0
            val currentPlayer = game.currentPlayer
            val currentPit = currentPlayer.smallPits[chosenPitIndex]

            currentPit.capacity = 5
            game.play(chosenPitIndex)

            assertThat(currentPlayer).isEqualTo(game.opponent)
        }

        @Test
        fun `if players last stone lands in own big pit, then current player gets another turn`() {
            val chosenPitIndex = 0
            val currentPlayer = game.currentPlayer
            val currentPit = currentPlayer.smallPits[chosenPitIndex]

            currentPit.capacity = 6
            game.play(chosenPitIndex)

            assertThat(currentPlayer).isEqualTo(game.currentPlayer)
        }

        @Test
        fun `if players last stone lands in own small pit, player captures stones from the opposite pit`() {
            // given
            val chosenPitIndex = 0
            val currentPlayer = game.currentPlayer
            val currentPit = currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 1

            val opponent = game.opponent
            val opponentsPit = opponent.smallPits[6 - (chosenPitIndex + currentPit.capacity) - 1]
            val opponentsPitCapacity = opponentsPit.capacity

            // when
            val emptyPit = currentPlayer.smallPits[chosenPitIndex + currentPit.capacity]
            emptyPit.capacity = 0

            game.play(chosenPitIndex)

            // then
            assertThat(currentPlayer.largePit.capacity).isEqualTo(opponentsPitCapacity + 1)
            assertThat(opponentsPit.capacity).isEqualTo(0)
            assertThat(emptyPit.capacity).isEqualTo(0)
        }

        @Test
        fun `if player chooses an empty pit, then player wont move`(){
            //given
            val chosenPitIndex = 0
            val currentPlayer = game.currentPlayer
            val currentPit = currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 0

            //when
            val result = runCatching { game.play(chosenPitIndex) }

            //then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(game.currentPlayer).isEqualTo(currentPlayer)
        }

        @Test
        fun `if player chooses invalid pit, then player wont move`(){
            //given
            val chosenPitIndex = 6
            val currentPlayer = game.currentPlayer

            //when
            val result = runCatching { game.play(chosenPitIndex) }

            //then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(game.currentPlayer).isEqualTo(currentPlayer)
        }

        @Test
        fun `prevent player movement if game has a winner`(){
            //given
            game.isFinished = true

            //when
            val result = runCatching { game.play(0) }

            //then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    @DisplayName("End Game Scenarios")
    inner class EndGameTests{
        @Test
        fun `end game`() {
            //given
            val chosenPitIndex = 5
            val currentPit = game.currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 2

            //when
            game.currentPlayer.smallPits.filter { it != currentPit }.map { it.capacity = 0 }
            game.opponent.smallPits.map { it.capacity = 10 }
            game.currentPlayer.largePit.capacity = 50

            game.play(chosenPitIndex)

            //then
            assertThat(game.currentPlayer.largePit.capacity).isEqualTo(51)
            assertThat(game.opponent.largePit.capacity).isEqualTo(61)
            assertThat(game.winner).isNotNull.isEqualTo(game.opponent)
        }

        @Test
        fun `tie game when both players have equal scores`(){
            //given
            val chosenPitIndex = 5
            val currentPit = game.currentPlayer.smallPits[chosenPitIndex]
            currentPit.capacity = 1

            //when
            game.currentPlayer.smallPits.filter { it != currentPit }.map { it.capacity = 0 }
            game.opponent.smallPits.map { it.capacity = 10 }

            game.currentPlayer.largePit.capacity = 60
            game.opponent.largePit.capacity = 1

            game.play(chosenPitIndex)

            //then
            assertThat(game.currentPlayer.largePit.capacity).isEqualTo(61)
            assertThat(game.opponent.largePit.capacity).isEqualTo(61)
            assertThat(game.winner).isNull()
        }
    }
}