package com.example.lab6blackjack

data class Card(val suit: String, val rank: String) {

    fun getImageResName(): String {
        val suitPrefix = when (suit) {
            "♠" -> "s"
            "♥" -> "h"
            "♦" -> "d"
            "♣" -> "c"
            else -> "s"
        }
        val rankSuffix = rank.lowercase() // "a", "2", "10", "j", "q", "k"
        return "$suitPrefix$rankSuffix"   // наприклад "ca", "d10", "hj", "sk"
    }

    fun getValue(): Int = when (rank) {
        "A" -> 11
        "J", "Q", "K" -> 10
        else -> rank.toInt()
    }

    fun getSuitColor(): Boolean = suit == "♥" || suit == "♦"
}

class Deck(private val numDecks: Int = 3) {
    val totalCards: Int get() = numDecks * 52
    private val suits = listOf("♠", "♥", "♦", "♣")
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

    private val cards = mutableListOf<Card>()
    private val discarded = mutableListOf<Card>()

    init {
        build()
        shuffle()
    }

    private fun build() {
        cards.clear()
        repeat(numDecks) {
            suits.forEach { suit ->
                ranks.forEach { rank ->
                    cards.add(Card(suit, rank))
                }
            }
        }
    }

    fun shuffle() = cards.shuffle()

    fun deal(): Card {
        if (cards.isEmpty()) {
            cards.addAll(discarded)
            discarded.clear()
            shuffle()
            if (cards.isEmpty()) build()
        }
        val card = cards.removeAt(cards.lastIndex)
        discarded.add(card)
        return card
    }

    fun remainingCards(): Int = cards.size

    fun reset() {
        build()
        shuffle()
    }
}

class Hand(var bet: Int = 0) {
    val cards = mutableListOf<Card>()
    var value: Int = 0

    fun addCard(card: Card) {
        cards.add(card)
        calcHand()
    }

    fun calcHand() {
        value = 0
        var aces = 0
        for (card in cards) {
            if (card.rank == "A") {
                aces++
                value += 11
            } else {
                value += card.getValue()
            }
        }
        while (value > 21 && aces > 0) {
            value -= 10
            aces--
        }
    }

    fun clear() {
        cards.clear()
        value = 0
    }

    fun isBlackjack(): Boolean = cards.size == 2 && value == 21
    fun isBust(): Boolean = value > 21
}