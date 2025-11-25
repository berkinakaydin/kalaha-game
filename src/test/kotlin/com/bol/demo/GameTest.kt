package com.bol.demo

import com.bol.demo.game.Game
import com.bol.demo.game.Pit
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameTest {
    @Test
    fun `a game should contain two players`() {
        val game = Game()

        assertEquals(game.players.size, 2)
    }

    @Test
    fun `each player should have 6 small pits and one big pit`() {
        val players = Game().players
        val numberOfSmallPits = 6

        players.forEach { player ->
            assertEquals(numberOfSmallPits, player.smallPits.size)
            assertNotNull(player.largePit)
        }
    }


    @Test
    fun `each small pits should contain 6 stones`() {
        val player = Game().players
        val numberOfStones = 6

        player.forEach { player ->
            player.smallPits.forEach { pit ->
                assertEquals(numberOfStones, pit.capacity)
            }
            assertEquals(0, player.largePit.capacity)
        }
    }

    @Test
    fun `each pits should be connected`() {
        val player = Game().players.first()

        assertSoftly {
            for (i in 0 until player.smallPits.size - 1) {
                val currentPit = player.smallPits[i]
                val nextPit = player.smallPits[i + 1]

                it.assertThat(currentPit.next).isEqualTo(nextPit)
            }
            it.assertThat(player.largePit).isEqualTo(player.smallPits.last().next)
        }
    }
}