package com.bol.kalaha.game

class Pit(
    var capacity: Int = 0,
) {
    lateinit var next: Pit
}