package com.example.lab6blackjack

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var game: BlackjackGame

    // Звуки
    private var soundDraw: MediaPlayer? = null
    private var soundFlip: MediaPlayer? = null

    // UI елементи
    private lateinit var tvBalance: TextView
    private lateinit var tvBet: TextView
    private lateinit var tvPlayerScore: TextView
    private lateinit var tvDealerScore: TextView
    private lateinit var tvResult: TextView

    private lateinit var tvDeckCount: TextView

    private lateinit var ivDeckStack: ImageView

    private lateinit var layoutPlayerCards: LinearLayout
    private lateinit var layoutDealerCards: LinearLayout
    private lateinit var layoutSplitHands: LinearLayout

    private lateinit var btnBet10: Button
    private lateinit var btnBet50: Button
    private lateinit var btnBet100: Button
    private lateinit var btnDeal: Button
    private lateinit var btnHit: Button
    private lateinit var btnStand: Button
    private lateinit var btnDouble: Button
    private lateinit var btnSplit: Button
    private lateinit var btnClearBet: Button
    private lateinit var btnNewGame: Button

    private lateinit var layoutBettingButtons: LinearLayout
    private lateinit var layoutGameButtons: LinearLayout

    private var dealerHiddenCard: Card? = null
    private var isFlipping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSounds()
        setupGame()
        setupClickListeners()
    }

    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        tvBet = findViewById(R.id.tvBet)
        tvPlayerScore = findViewById(R.id.tvPlayerScore)
        tvDealerScore = findViewById(R.id.tvDealerScore)
        tvResult = findViewById(R.id.tvResult)
        tvDeckCount = findViewById(R.id.tvDeckCount)
        ivDeckStack = findViewById(R.id.ivDeckStack)


        layoutPlayerCards = findViewById(R.id.layoutPlayerCards)
        layoutDealerCards = findViewById(R.id.layoutDealerCards)
        layoutSplitHands = findViewById(R.id.layoutSplitHands)

        btnBet10 = findViewById(R.id.btnBet10)
        btnBet50 = findViewById(R.id.btnBet50)
        btnBet100 = findViewById(R.id.btnBet100)
        btnDeal = findViewById(R.id.btnDeal)
        btnHit = findViewById(R.id.btnHit)
        btnStand = findViewById(R.id.btnStand)
        btnDouble = findViewById(R.id.btnDouble)
        btnSplit = findViewById(R.id.btnSplit)
        btnClearBet = findViewById(R.id.btnClearBet)
        btnNewGame = findViewById(R.id.btnNewGame)

        layoutBettingButtons = findViewById(R.id.layoutBettingButtons)
        layoutGameButtons = findViewById(R.id.layoutGameButtons)
    }

    private fun initSounds() {
        try {
            soundDraw = MediaPlayer.create(this, R.raw.card_sounds_35956)
            soundFlip = MediaPlayer.create(this, R.raw.flipcard_91468)
        } catch (e: Exception) {
            // Якщо звуки не знайдено — просто не грає, помилки не буде
        }
    }

    private fun playDraw() {
        try { soundDraw?.let { if (!it.isPlaying) it.start() } } catch (e: Exception) {}
    }

    private fun playFlip() {
        try { soundFlip?.let { if (!it.isPlaying) it.start() } } catch (e: Exception) {}
    }

    private fun setupGame() {
        game = BlackjackGame()
        game.onStateChanged = { runOnUiThread { updateUI() } }
        updateUI()
    }

    private fun setupClickListeners() {
        btnBet10.setOnClickListener { placeBet(10) }
        btnBet50.setOnClickListener { placeBet(50) }
        btnBet100.setOnClickListener { placeBet(100) }
        btnClearBet.setOnClickListener { game.clearBet() }

        btnDeal.setOnClickListener {
            if (game.currentBet == 0) {
                showToast("Спочатку зробіть ставку!")
                return@setOnClickListener
            }
            playDraw()
            game.deal()

            val snap = game.getSnapshot()
            if (snap.canInsurance) {
                showInsuranceDialog()
            }
        }

        btnHit.setOnClickListener {
            playDraw()
            game.hit()
        }

        btnStand.setOnClickListener {
            // Анімація перевертання перед тим як дилер грає
            val snap = game.getSnapshot()
            if (snap.dealerHand.size >= 2 && !snap.showDealerCards) {
                animateDealerFlip(snap.dealerHand[1]) {
                    playFlip()
                    game.stand()
                }
            } else {
                game.stand()
            }
        }

        btnDouble.setOnClickListener {
            if (!game.doubleDown()) showToast("Не вистачає коштів для подвоєння")
            else playDraw()
        }

        btnSplit.setOnClickListener {
            if (!game.splitHand()) showToast("Неможливо розділити")
        }

        btnNewGame.setOnClickListener {
            dealerHiddenCard = null
            isFlipping = false
            if (game.balance == 0 && game.currentBet == 0) showRestartDialog()
            else game.resetGame()
        }
    }

    private fun placeBet(amount: Int) {
        if (!game.placeBet(amount)) showToast("Недостатньо коштів!")
    }
    private fun animateDealerFlip(card: Card, onComplete: () -> Unit) {
        if (isFlipping) return
        isFlipping = true

        // Знаходимо другу карту дилера (приховану)
        val hiddenCardView = layoutDealerCards.getChildAt(1) ?: run {
            onComplete(); return
        }

        // Фаза 1: стискаємо карту по горизонталі (рубашка зникає)
        val scaleDown = ObjectAnimator.ofFloat(hiddenCardView, "scaleX", 1f, 0f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
        }

        scaleDown.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Міняємо зображення на лицеву сторону
                val resName = card.getImageResName()
                val resId = resources.getIdentifier(resName, "drawable", packageName)
                if (hiddenCardView is ImageView) {
                    if (resId != 0) hiddenCardView.setImageResource(resId)
                    else {
                        val backId = resources.getIdentifier("back", "drawable", packageName)
                        if (backId != 0) hiddenCardView.setImageResource(backId)
                    }
                }

                // Фаза 2: розширюємо назад (лицева сторона з'являється)
                val scaleUp = ObjectAnimator.ofFloat(hiddenCardView, "scaleX", 0f, 1f).apply {
                    duration = 200
                    interpolator = DecelerateInterpolator()
                }
                scaleUp.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        isFlipping = false
                        onComplete()
                    }
                })
                scaleUp.start()
            }
        })

        scaleDown.start()
    }


    private fun updateUI() {
        val snap = game.getSnapshot()

        tvBalance.text = "Баланс: $${snap.balance}"
        tvBet.text = "Ставка: $${snap.currentBet}"

        // Лічильник колоди
        val deckPercent = (snap.balance * 100) / 1000 // умовний показник
        tvDeckCount.text = "${snap.deckRemainingCards}"

        // Оновлення іконки колоди
        val backId = resources.getIdentifier("back", "drawable", packageName)
        if (backId != 0) ivDeckStack.setImageResource(backId)
        else ivDeckStack.setImageResource(R.drawable.card_back)

        renderDealerCards(snap)
        renderPlayerCards(snap)
        renderSplitHands(snap)

        // Рахунок
        tvPlayerScore.text = when {
            snap.playerHand.isEmpty() && snap.splitHands.isEmpty() -> ""
            snap.splitHands.isNotEmpty() -> ""
            else -> "Твій рахунок: ${snap.playerValue}"
        }

        tvDealerScore.text = when {
            snap.dealerHand.isEmpty() -> ""
            snap.showDealerCards -> "Рахунок дилера: ${snap.dealerValue}"
            snap.dealerHand.isNotEmpty() -> "Рахунок дилера: ${snap.dealerHand[0].getValue()}+"
            else -> ""
        }

        // Результат
        if (snap.resultMessage.isNotEmpty()) {
            tvResult.text = snap.resultMessage
            tvResult.visibility = View.VISIBLE
            val color = when {
                snap.resultMessage.contains("виграв") || snap.resultMessage.contains("Виграш") ||
                        snap.resultMessage.contains("Блекджек") -> ContextCompat.getColor(this, R.color.green_win)
                snap.resultMessage.contains("Нічия") -> ContextCompat.getColor(this, R.color.grey_push)
                else -> ContextCompat.getColor(this, R.color.red_lose)
            }
            tvResult.setTextColor(color)
        } else {
            tvResult.visibility = View.GONE
        }

        // Кнопки
        val isWaiting = snap.gameState == GameState.WAITING
        val isBetting = snap.gameState == GameState.BETTING
        val isPlaying = snap.gameState == GameState.PLAYING
        val isEnded = snap.gameState == GameState.ENDED

        layoutBettingButtons.visibility = if (isWaiting || isBetting) View.VISIBLE else View.GONE
        btnClearBet.visibility = if (isBetting) View.VISIBLE else View.GONE
        btnDeal.visibility = if (isWaiting || isBetting) View.VISIBLE else View.GONE
        layoutGameButtons.visibility = if (isPlaying) View.VISIBLE else View.GONE
        btnDouble.visibility = if (isPlaying && snap.canDouble) View.VISIBLE else View.GONE
        btnSplit.visibility = if (isPlaying && snap.canSplit) View.VISIBLE else View.GONE
        btnNewGame.visibility = if (isEnded) View.VISIBLE else View.GONE
    }

    //РЕНДЕР КАРТ ДИЛЕРА

    private fun renderDealerCards(snap: GameSnapshot) {
        layoutDealerCards.removeAllViews()
        snap.dealerHand.forEachIndexed { index, card ->
            // Друга карта дилера — рубашка поки гра не закінчена і не відбулась анімація
            val showBack = !snap.showDealerCards && index == 1 && !isFlipping
            val cardView = createCardView(if (showBack) null else card)
            animateCardIn(cardView)
            layoutDealerCards.addView(cardView)
        }
    }

    //РЕНДЕР КАРТ ГРАВЦЯ

    private fun renderPlayerCards(snap: GameSnapshot) {
        layoutPlayerCards.removeAllViews()
        if (snap.splitHands.isNotEmpty()) return // Split hands рендеряться окремо
        snap.playerHand.forEach { card ->
            val cardView = createCardView(card)
            animateCardIn(cardView)
            layoutPlayerCards.addView(cardView)
        }
    }

    //РЕНДЕР SPLIT РУК

    private fun renderSplitHands(snap: GameSnapshot) {
        layoutSplitHands.removeAllViews()
        if (snap.splitHands.isEmpty()) return

        snap.splitHands.forEachIndexed { handIndex, cards ->
            val handContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 32
                layoutParams = params
            }

            // Підпис руки
            val isActive = handIndex == snap.activeHandIndex && snap.gameState == GameState.PLAYING
            val result = snap.splitHandResults.getOrNull(handIndex)
            val label = TextView(this).apply {
                text = buildString {
                    append("Рука ${handIndex + 1} ($${snap.splitBets[handIndex]})")
                    result?.let { append("\n${getResultText(it)}") }
                }
                setTextColor(ContextCompat.getColor(context,
                    if (isActive) R.color.accent_gold else R.color.white))
                textSize = 13f
                setPadding(0, 0, 0, 6)
            }
            handContainer.addView(label)

            // Карти
            val cardsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            cards.forEach { card ->
                val cv = createCardView(card)
                animateCardIn(cv)
                cardsRow.addView(cv)
            }
            handContainer.addView(cardsRow)

            // Рахунок руки
            val tempHand = Hand(bet = snap.splitBets[handIndex])
            cards.forEach { tempHand.addCard(it) }
            val scoreView = TextView(this).apply {
                text = "Рахунок: ${tempHand.value}"
                setTextColor(ContextCompat.getColor(context, when {
                    tempHand.value == 21 -> R.color.green_win
                    tempHand.isBust() -> R.color.red_lose
                    else -> R.color.white
                }))
                textSize = 12f
                setPadding(0, 4, 0, 0)
            }
            handContainer.addView(scoreView)
            layoutSplitHands.addView(handContainer)
        }
    }

    //СТВОРЕННЯ ВИГЛЯДУ КАРТИ
    // Використовує PNG зображення з папки res/drawable/
    // null = рубашка карти (back.png або card_back drawable)

    private fun createCardView(card: Card?): View {
        val imageView = ImageView(this)

        val cardWidthPx = resources.getDimensionPixelSize(R.dimen.card_width)
        val cardHeightPx = resources.getDimensionPixelSize(R.dimen.card_height)

        val params = LinearLayout.LayoutParams(cardWidthPx, cardHeightPx)
        params.marginEnd = resources.getDimensionPixelSize(R.dimen.card_margin)
        imageView.layoutParams = params
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        if (card == null) {
            // Рубашка — файл back.png в drawable
            val backId = resources.getIdentifier("back", "drawable", packageName)
            if (backId != 0) {
                imageView.setImageResource(backId)
            } else {
                imageView.setImageResource(R.drawable.card_back) // fallback з нашого xml
            }
        } else {
            // Шукаємо файл картинки: наприклад "ca.png" для туза треф
            val resName = card.getImageResName()
            val resId = resources.getIdentifier(resName, "drawable", packageName)
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                // Якщо картинки немає малюємо текстову карту
                imageView.setImageResource(R.drawable.card_face)
                // Можна також додати TextView поверх, але PNG повинні бути
            }
        }

        return imageView
    }

    //АНІМАЦІЯ ПОЯВИ КАРТИ 

    private fun animateCardIn(view: View) {
        view.alpha = 0f
        view.translationY = -60f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(view, "translationY", -60f, 0f)
            )
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    //ДОПОМІЖН

    private fun getResultText(result: HandResult): String = when (result) {
        HandResult.WIN -> "✓ Виграш"
        HandResult.BLACKJACK -> "★ Блекджек"
        HandResult.PUSH -> "= Нічия"
        HandResult.BUST -> "✗ Перебір"
        HandResult.LOSE -> "✗ Програш"
    }

    private fun showInsuranceDialog() {
        val maxInsurance = minOf(game.currentBet / 2, game.balance)
        AlertDialog.Builder(this, R.style.BlackjackDialog)
            .setTitle("Страхування")
            .setMessage("Дилер має туза!\nЗастрахуватись за $$maxInsurance?")
            .setPositiveButton("Страхувати") { _, _ -> game.insurance(maxInsurance) }
            .setNegativeButton("Не страхувати", null)
            .show()
    }

    private fun showRestartDialog() {
        AlertDialog.Builder(this, R.style.BlackjackDialog)
            .setTitle("Гра завершена")
            .setMessage("У вас закінчились кошти!\nПочати нову гру з \$1000?")
            .setPositiveButton("Так") { _, _ -> setupGame() }
            .setNegativeButton("Вийти") { _, _ -> finish() }
            .show()
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        soundDraw?.release()
        soundFlip?.release()
    }
}