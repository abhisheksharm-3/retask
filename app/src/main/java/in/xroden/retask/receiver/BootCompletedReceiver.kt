package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.xroden.retask.service.TaskNotificationService

/**
 * Listens for the device boot completion event to ensure services are rescheduled.
 *
 * This receiver starts the [TaskNotificationService] after the device reboots. This is
 * crucial for re-registering alarms or notifications that do not persist across restarts.
 *
 * **Manifest Requirement:** This receiver must be registered in the `AndroidManifest.xml`
 * with the `RECEIVE_BOOT_COMPLETED` permission and an intent-filter for the
 * `android.intent.action.BOOT_COMPLETED` action.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     * It checks if the device has finished booting and starts the notification service if it has.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received, which should contain the boot action.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // We only care about the boot completed event.
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, TaskNotificationService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}