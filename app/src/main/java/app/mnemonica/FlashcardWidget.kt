package app.mnemonica

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.widget.RemoteViews
import kotlin.random.Random

class FlashcardWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TAP) onTap(context) else super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = deal(context)

    override fun onEnabled(context: Context) = DealerService.start(context)

    override fun onDisabled(context: Context) {
        context.stopService(Intent(context, DealerService::class.java))
    }

    companion object {
        private const val ACTION_TAP = "app.mnemonica.TAP"

        // Widgets only report single taps, so pair them up ourselves. Generous, because
        // each tap arrives as a broadcast.
        private const val DOUBLE_TAP_MS = 500
        private var lastTap = 0L

        private fun onTap(context: Context) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastTap < DOUBLE_TAP_MS) {
                lastTap = 0L
                deal(context)
            } else {
                lastTap = now
            }
        }

        // Coarsest first. Each layer's inAnimation shows it for its slice of the reveal.
        private val blur = floatArrayOf(72f, 48f, 32f, 21f, 13f, 7f, 3f, 0f)
        private val layers = intArrayOf(
            R.id.layer0, R.id.layer1, R.id.layer2, R.id.layer3,
            R.id.layer4, R.id.layer5, R.id.layer6, R.id.layer7,
        )
        private val flippers = intArrayOf(
            R.id.flipper0, R.id.flipper1, R.id.flipper2, R.id.flipper3,
            R.id.flipper4, R.id.flipper5, R.id.flipper6, R.id.flipper7,
        )

        fun ids(context: Context): IntArray = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, FlashcardWidget::class.java))

        /** Puts a new number/card pair on every widget, with one side hidden. */
        fun deal(context: Context) {
            val ids = ids(context)
            if (ids.isEmpty()) return
            val position = Deck.next()
            val card = mnemonica[position - 1]
            val hideCard = Random.nextBoolean()
            val packageName = context.packageName

            val number = { blur: Float -> Faces.number(position, blur) }
            val face = { blur: Float -> Faces.card(card, blur) }
            val views = RemoteViews(packageName, R.layout.widget).apply {
                // The host reapplies onto the existing views, so clear the old faces first.
                removeAllViews(R.id.number_slot)
                removeAllViews(R.id.card_slot)
                addView(R.id.number_slot, if (hideCard) plain(packageName, number) else reveal(packageName, number))
                addView(R.id.card_slot, if (hideCard) reveal(packageName, face) else plain(packageName, face))
                setContentDescription(R.id.number_slot, position.toString())
                setContentDescription(R.id.card_slot, "${card.rank} of ${card.suit.name.lowercase()}")
                setOnClickPendingIntent(
                    android.R.id.background,
                    PendingIntent.getBroadcast(
                        context, 0,
                        Intent(context, FlashcardWidget::class.java).setAction(ACTION_TAP),
                        PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
            }
            AppWidgetManager.getInstance(context).updateAppWidget(ids, views)
        }

        private fun plain(packageName: String, face: (blur: Float) -> Bitmap) =
            RemoteViews(packageName, R.layout.face_plain).apply { setImageViewBitmap(R.id.face, face(0f)) }

        /**
         * Widgets can't run code to animate, so each blur level sits in its own
         * ViewFlipper. Showing a flipper's child plays its inAnimation from the first frame
         * drawn, which means the reveal starts when the widget is actually seen.
         */
        private fun reveal(packageName: String, face: (blur: Float) -> Bitmap) =
            RemoteViews(packageName, R.layout.face_reveal).apply {
                for (i in layers.indices) {
                    setImageViewBitmap(layers[i], face(blur[i]))
                    setDisplayedChild(flippers[i], 0)
                }
            }
    }
}
