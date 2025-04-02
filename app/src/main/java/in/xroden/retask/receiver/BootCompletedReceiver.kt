package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.xroden.retask.service.TaskNotificationService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restart notification service after device boot
            val serviceIntent = Intent(context, TaskNotificationService::class.java)

            context.startForegroundService(serviceIntent)
        }
    }
}