package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.xroden.retask.service.TaskNotificationService

/**
 * Broadcast receiver that listens for the device boot completion event.
 *
 * This receiver automatically restarts the [TaskNotificationService] when the device
 * completes booting, ensuring that task notifications continue to function properly
 * after device restarts without requiring user intervention.
 *
 * Note: This receiver must be registered in the AndroidManifest.xml with the
 * RECEIVE_BOOT_COMPLETED permission and an intent-filter for ACTION_BOOT_COMPLETED.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            startTaskNotificationService(context)
        }
    }

    /**
     * Starts the TaskNotificationService.
     *
     * @param context The context used to start the service.
     */
    private fun startTaskNotificationService(context: Context) {
        val serviceIntent = Intent(context, TaskNotificationService::class.java)
        context.startForegroundService(serviceIntent)
    }
}