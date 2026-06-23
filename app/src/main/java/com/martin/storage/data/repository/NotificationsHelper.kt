package com.martin.storage.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.unit.Constraints
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.martin.storage.data.DataKeys
import com.martin.storage.data.appJson
import com.martin.storage.data.appDataStore
import com.martin.storage.data.model.GroceryItem
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import java.util.concurrent.TimeUnit

private const val CHANNEL_STOCK  = "low_stock"
private const val CHANNEL_EXPIRY = "expiry"
private const val WORK_TAG       = "grocery_check"

fun scheduleGroceryChecks(context: Context) {
    createNotificationChannels(context)
    val request = PeriodicWorkRequestBuilder<GroceryCheckWorker>(
        repeatInterval         = 12,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    )
        .addTag(WORK_TAG)
        .setConstraints(Constraints.Builder().build())
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_STOCK, "Low Stock Alerts", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Notifies when items are running low." }
    )
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_EXPIRY, "Expiry Alerts", NotificationManager.IMPORTANCE_HIGH)
            .apply { description = "Notifies when items are expiring soon." }
    )
}

class GroceryCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = context.appDataStore.data.first()
        val itemsJson = prefs[DataKeys.GROCERY_ITEMS] ?: return Result.success()
        val settingsJson = prefs[DataKeys.USER_SETTINGS]

        val items = try {
            appJson.decodeFromString<List<GroceryItem>>(itemsJson)
        } catch (_: Exception) { return Result.success() }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notifId = 100

        // Low stock alerts
        val lowItems = items.filter { it.isLowStock || it.isOutOfStock }
        if (lowItems.isNotEmpty()) {
            val names = lowItems.take(3).joinToString(", ") { it.name }
            val more  = if (lowItems.size > 3) " +${lowItems.size - 3} more" else ""
            nm.notify(notifId++, NotificationCompat.Builder(context, CHANNEL_STOCK)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("🛒 Low Stock")
                .setContentText("$names$more running low.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    lowItems.joinToString("\n") { "• ${it.name}: ${it.amount} ${it.unit} left" }
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            )
        }

        // Expiry alerts
        val expiringItems = items.filter { it.isExpiringSoon }
        expiringItems.forEach { item ->
            val days = item.daysUntilExpiry ?: return@forEach
            val label = if (days == 0) "today" else "in $days day${if (days > 1) "s" else ""}"
            nm.notify(notifId++, NotificationCompat.Builder(context, CHANNEL_EXPIRY)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⏰ Expiring Soon")
                .setContentText("${item.name} expires $label!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            )
        }

        return Result.success()
    }
}