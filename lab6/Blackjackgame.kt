package com.example.lab6blackjack

enum class GameState { WAITING, BETTING, PLAYING, ENDED }

enum class HandResult { WIN, LOSE, PUSH, BLACKJACK, BUST }

data class GameSnapshot(
    val playerHand: List<Card>,
    val dealerHand: List<Card>,
    val splitHands: List<List<Card>>,
    val splitBets: List<Int>,
    val activeHandIndex: Int,
    val playerValue: Int,
    val dealerValue: Int,
    val deckRemainingCards: Int,
    val balance: Int,
    val currentBet: Int,
    val insuranceBet: Int,
    val gameState: GameState,
    val showDealerCards: Boolean,
    val resultMessage: String,
    val canSplit: Boolean,
    val canDouble: Boolean,
    val canInsurance: Boolean,
    val splitHandResults: List<HandResult>
)

class BlackjackGame {
    private val deck = Deck()

    var balance: Int = 1000
        private set
    var currentBet: Int = 0
        private set
    var insuranceBet: Int = 0
        private set

    var gameState: GameState = GameState.WAITING
        private set

    private val playerHand = Hand()
    private val dealerHand = Hand()
    private val splitHands = mutableListOf<Hand>()
    var activeHandIndex: Int = 0
        private set

    var showDealerCards: Boolean = false
        private set
    var resultMessage: String = ""
        private set

    var onStateChanged: (() -> Unit)? = null

    fun getSnapshot(): GameSnapshot {
        playerHand.calcHand()
        dealerHand.calcHand()

        val splitHandResults = if (gameState == GameState.ENDED) {
            splitHands.map { getHandResult(it) }
        } else emptyList()

        return GameSnapshot(
            playerHand = playerHand.cards.toList(),
            dealerHand = dealerHand.cards.toList(),
            splitHands = splitHands.map { it.cards.toList() },
            splitBets = splitHands.map { it.bet },
            activeHandIndex = activeHandIndex,
            playerValue = playerHand.value,
            dealerValue = dealerHand.value,
            balance = balance,
            currentBet = currentBet,
            insuranceBet = insuranceBet,
            gameState = gameState,
            deckRemainingCards = deck.remainingCards(),
            showDealerCards = showDealerCards,
            resultMessage = resultMessage,
            canSplit = canSplit(),
            canDouble = canDoubleDown(),
            canInsurance = canInsurance(),
            splitHandResults = splitHandResults
        )
    }

    fun placeBet(amount: Int): Boolean {
        if (gameState != GameState.WAITING && gameState != GameState.BETTING) return false
        if (amount > balance) return false
        currentBet += amount
        balance -= amount
        gameState = GameState.BETTING
        onStateChanged?.invoke()
        return true
    }

    fun clearBet() {
        if (gameState != GameState.BETTING) return
        balance += currentBet
        currentBet = 0
        gameState = GameState.WAITING
        onStateChanged?.invoke()
    }

    fun deal(): Boolean {
        if (gameState == GameState.PLAYING) return false
        if (currentBet == 0) return false

        dealerHand.clear()
        playerHand.clear()
        splitHands.clear()
        activeHandIndex = 0
        showDealerCards = false
        resultMessage = ""

        if (deck.remainingCards() < 15) deck.reset()

        repeat(2) {
            dealerHand.addCard(deck.deal())
            playerHand.addCard(deck.deal())
        }

        gameState = GameState.PLAYING
        onStateChanged?.invoke()
        checkBlackjack()
        return true
    }

    private fun checkBlackjack() {
        val playerBJ = playerHand.isBlackjack()
        val dealerBJ = dealerHand.isBlackjack()

        if (playerBJ || dealerBJ) {
            showDealerCards = true
            gameState = GameState.ENDED
            checkInsurancePayout()

            resultMessage = when {
                playerBJ && dealerBJ -> { balance += currentBet; "Блекджек у обох! Нічия" }
                playerBJ -> { balance += (currentBet * 2.5).toInt(); "Блекджек! Ти виграв 3:2!" }
                else -> "Блекджек у дилера! Ти програв"
            }
            onStateChanged?.invoke()
        }
    }

    fun hit(): Boolean {
        if (gameState != GameState.PLAYING) return false

        if (splitHands.isNotEmpty()) {
            val hand = splitHands[activeHandIndex]
            hand.addCard(deck.deal())
            if (hand.isBust() || hand.value == 21) {
                if (activeHandIndex < splitHands.size - 1) activeHandIndex++
                else finishRound()
            }
        } else {
            playerHand.addCard(deck.deal())
            when {
                playerHand.isBust() -> {
                    showDealerCards = true
                    gameState = GameState.ENDED
                    resultMessage = "Перебір! Ти програв"
                }
                playerHand.value == 21 -> stand()
            }
        }
        onStateChanged?.invoke()
        return true
    }

    fun stand(): Boolean {
        if (gameState != GameState.PLAYING) return false
        if (splitHands.isNotEmpty() && activeHandIndex < splitHands.size - 1) {
            activeHandIndex++
            onStateChanged?.invoke()
            return true
        }
        finishRound()
        return true
    }

    private fun finishRound() {
        dealerHand.calcHand()
        while (dealerHand.value < 17) {
            dealerHand.addCard(deck.deal())
        }
        showDealerCards = true
        gameState = GameState.ENDED
        checkInsurancePayout()

        if (splitHands.isNotEmpty()) {
            var totalWin = 0
            val results = StringBuilder()
            splitHands.forEachIndexed { i, hand ->
                when (getHandResult(hand)) {
                    HandResult.WIN -> { totalWin += hand.bet * 2; results.append("Рука ${i+1}: Виграш! ") }
                    HandResult.PUSH -> { totalWin += hand.bet; results.append("Рука ${i+1}: Нічия. ") }
                    HandResult.BUST -> results.append("Рука ${i+1}: Перебір. ")
                    HandResult.LOSE -> results.append("Рука ${i+1}: Програш. ")
                    HandResult.BLACKJACK -> { totalWin += (hand.bet * 2.5).toInt(); results.append("Рука ${i+1}: Блекджек! ") }
                }
            }
            balance += totalWin
            resultMessage = results.toString().trim()
        } else {
            playerHand.calcHand()
            when {
                dealerHand.isBust() -> { balance += currentBet * 2; resultMessage = "Дилер перебрав! Ти виграв!" }
                dealerHand.value == playerHand.value -> { balance += currentBet; resultMessage = "Нічия!" }
                playerHand.value > dealerHand.value -> { balance += currentBet * 2; resultMessage = "Ти виграв!" }
                else -> resultMessage = "Ти програв "
            }
        }
        onStateChanged?.invoke()
    }

    fun doubleDown(): Boolean {
        if (!canDoubleDown()) return false
        if (splitHands.isNotEmpty()) {
            val hand = splitHands[activeHandIndex]
            balance -= hand.bet
            hand.bet *= 2
            hand.addCard(deck.deal())
            if (activeHandIndex < splitHands.size - 1) { activeHandIndex++; onStateChanged?.invoke() }
            else finishRound()
        } else {
            balance -= currentBet
            currentBet *= 2
            playerHand.addCard(deck.deal())
            if (playerHand.isBust()) {
                showDealerCards = true; gameState = GameState.ENDED
                resultMessage = "Перебір після подвоєння"; onStateChanged?.invoke()
            } else finishRound()
        }
        return true
    }

    fun splitHand(): Boolean {
        if (!canSplit()) return false
        val card1 = playerHand.cards.removeAt(0)
        val card2 = playerHand.cards.removeAt(0)
        val hand1 = Hand(bet = currentBet)
        val hand2 = Hand(bet = currentBet)
        hand1.addCard(card1); hand1.addCard(deck.deal())
        hand2.addCard(card2); hand2.addCard(deck.deal())
        balance -= currentBet
        splitHands.clear()
        splitHands.add(hand1)
        splitHands.add(hand2)
        activeHandIndex = 0
        onStateChanged?.invoke()
        return true
    }

    fun insurance(amount: Int): Boolean {
        if (!canInsurance()) return false
        val maxInsurance = minOf(currentBet / 2, balance)
        if (amount > maxInsurance) return false
        insuranceBet = amount
        balance -= amount
        onStateChanged?.invoke()
        return true
    }

    private fun checkInsurancePayout() {
        if (insuranceBet > 0 && dealerHand.isBlackjack()) balance += insuranceBet * 2
        insuranceBet = 0
    }

    fun resetGame() {
        if (deck.remainingCards() < 20) deck.reset()
        dealerHand.clear(); playerHand.clear(); splitHands.clear()
        activeHandIndex = 0; currentBet = 0; insuranceBet = 0
        showDealerCards = false; resultMessage = ""
        gameState = GameState.WAITING
        onStateChanged?.invoke()
    }

    private fun canSplit(): Boolean {
        if (splitHands.isNotEmpty() || playerHand.cards.size != 2) return false
        return playerHand.cards[0].rank == playerHand.cards[1].rank && balance >= currentBet
    }

    private fun canDoubleDown(): Boolean {
        return if (splitHands.isNotEmpty()) {
            val hand = splitHands[activeHandIndex]
            hand.cards.size == 2 && balance >= hand.bet
        } else {
            playerHand.cards.size == 2 && balance >= currentBet
        }
    }

    private fun canInsurance(): Boolean {
        return dealerHand.cards.isNotEmpty() &&
                dealerHand.cards[0].rank == "A" &&
                playerHand.cards.size == 2 &&
                insuranceBet == 0 && balance > 0 &&
                gameState == GameState.PLAYING
    }

    fun getHandResult(hand: Hand): HandResult {
        hand.calcHand(); dealerHand.calcHand()
        return when {
            hand.isBlackjack() -> HandResult.BLACKJACK
            hand.isBust() -> HandResult.BUST
            dealerHand.isBust() -> HandResult.WIN
            hand.value > dealerHand.value -> HandResult.WIN
            hand.value == dealerHand.value -> HandResult.PUSH
            else -> HandResult.LOSE
        }
    }
}