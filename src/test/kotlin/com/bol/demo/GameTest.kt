package com.bol.demo

import com.bol.demo.game.Game
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GameTest {
    @Test
    fun `a game should contain two players`() {
        val game = Game()

        assertEquals(game.players.size, 2)
    }

    @Test
    fun `each player should have 7 pits`(){
        val players = Game().players
        val numberOfPits = 7

        players.forEach { player ->
            assertEquals(player.pits.size, numberOfPits)
        }
    }
}