package app.mnemonica

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo

/**
 * Deals a new pair each time the screen turns off, so the next unlock shows a fresh card.
 *
 * Widgets never hear about the lock screen, and SCREEN_OFF only reaches receivers
 * registered at runtime in a live process, hence a foreground service. Without the
 * notification permission its notification stays out of the shade.
 */
class DealerService : Service() {

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = FlashcardWidget.deal(context)
    }

    override fun onCreate() {
        super.onCreate()
        val notifications = getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.channel_name), NotificationManager.IMPORTANCE_MIN),
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_card)
            .setContentTitle(getString(R.string.channel_name))
            .build()
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        registerReceiver(screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        unregisterReceiver(screenOff)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        private const val CHANNEL = "dealer"

        /** Only allowed from the foreground or from boot; elsewhere it quietly does nothing. */
        fun start(context: Context) {
            runCatching { context.startForegroundService(Intent(context, DealerService::class.java)) }
        }
    }
}

/** Restarts dealing after a reboot or an app update. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (FlashcardWidget.ids(context).isNotEmpty()) DealerService.start(context)
    }
}
