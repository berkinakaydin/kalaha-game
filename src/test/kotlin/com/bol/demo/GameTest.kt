package com.bol.demo

import com.bol.demo.game.Game
import com.bol.demo.game.Player
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import kotlin.test.assertNotNull

class GameTest {
    private val game: Game = Game()
    private val player1: Player = game.players[0]
    private val player2: Player = game.players[1]

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
}