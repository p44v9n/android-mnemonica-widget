package app.mnemonica

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

/** Draws each side of the flash card as a bitmap so the hidden side can be blurred. */
object Faces {
    private const val WIDTH = 384
    private const val HEIGHT = 320
    private const val INK = 0xFFF4F4F4.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = INK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, 500, false)
        textSize = 100f
        // Size every face the same, so the widest one ("10♦") just fits.
        textSize *= WIDTH * 0.86f / measureText("10" + Suit.Diamonds.symbol)
    }

    // Digit height, so numbers and cards share a baseline.
    private val capHeight = Rect().also { paint.getTextBounds("0", 0, 1, it) }.height()

    /** [blur] is a Gaussian radius in pixels of the full-size face; 0 is sharp. */
    fun number(position: Int, blur: Float = 0f) = draw(position.toString(), blur)

    fun card(card: Card, blur: Float = 0f) = draw(card.rank + card.suit.symbol, blur)

    private fun draw(text: String, blur: Float): Bitmap {
        // Blurred faces have no detail to keep, so draw them at quarter size and let the
        // widget stretch them back up.
        val scale = if (blur > 0) 0.25f else 1f
        val bitmap = Bitmap.createBitmap((WIDTH * scale).toInt(), (HEIGHT * scale).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { scale(scale, scale) }
        paint.maskFilter = if (blur > 0) BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL) else null
        canvas.drawText(text, WIDTH / 2f, (HEIGHT + capHeight) / 2f, paint)
        return bitmap
    }
}
