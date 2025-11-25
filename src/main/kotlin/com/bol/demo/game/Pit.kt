package com.bol.demo.game

class Pit(
    val next: Pit? = null,
    val stone: List<Int> = listOf()
)