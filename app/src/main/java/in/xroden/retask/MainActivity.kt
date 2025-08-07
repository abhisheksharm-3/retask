package `in`.xroden.retask

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.xroden.retask.service.TaskNotificationService
import `in`.xroden.retask.ui.screens.MainScreen
import `in`.xroden.retask.ui.theme.RetaskTheme
import `in`.xroden.retask.ui.viewmodel.TaskViewModel
import `in`.xroden.retask.utils.NotificationHelper

/**
 * The main and only activity for the ReTask application.
 *
 * This activity is responsible for:
 * - Setting up the edge-to-edge UI.
 * - Handling runtime permissions for notifications (on Android 13+).
 * - Initializing notification channels and starting the core service.
 * - Hosting the main Jetpack Compose UI via [RetaskApp].
 */
class MainActivity : ComponentActivity() {

    // A single, modern ActivityResultLauncher to handle the permission request.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startNotificationService()
        } else {
            // Inform the user that reminders won't work without the permission.
            Toast.makeText(this, "Task reminders are disabled without notification permission.", Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for a modern, immersive UI.
        enableEdgeToEdge()

        // Perform initial setup.
        NotificationHelper.createNotificationChannels(this)
        requestNotificationPermission()

        setContent {
            RetaskTheme {
                RetaskApp()
            }
        }
    }

    /**
     * Encapsulates the entire logic for checking and requesting the POST_NOTIFICATIONS permission.
     * This is only relevant for Android 13 (API 33) and above.
     */
    private fun requestNotificationPermission() {

        when {
            // Case 1: Permission is already granted.
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                startNotificationService()
            }
            // Case 2: We should show a rationale explaining why we need the permission.
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showPermissionRationaleDialog()
            }
            // Case 3: Request the permission directly.
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Shows a dialog explaining to the user why the notification permission is essential.
     */
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("ReTask uses notifications to remind you of your upcoming tasks. Please grant permission to enable this core feature.")
            .setPositiveButton("Allow") { _, _ ->
                // After explaining, launch the permission request.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    /**
     * Starts the core background service responsible for managing task alarms.
     */
    private fun startNotificationService() {
        // This service is now redundant with TaskReminderService.
        // In a final version, you would choose one scheduling strategy.
        // For now, we'll keep it to ensure the persistent notification summary works.
        val serviceIntent = Intent(this, TaskNotificationService::class.java)
        startForegroundService(serviceIntent)
    }

    /**
     * A companion object to hold constants related to MainActivity.
     */
    companion object {
        // This key is used to pass a task ID via an Intent extra.
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
    }
}

/**
 * The root composable for the ReTask application.
 *
 * Sets up the main UI structure using a [Scaffold] and hosts the [MainScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetaskApp(modifier: Modifier = Modifier) {
    val viewModel: TaskViewModel = viewModel()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Retask", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        MainScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}