package `in`.xroden.retask

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.xroden.retask.service.TaskNotificationService
import `in`.xroden.retask.ui.screens.MainScreen
import `in`.xroden.retask.ui.theme.RetaskTheme
import `in`.xroden.retask.ui.viewmodel.TaskViewModel
import `in`.xroden.retask.utils.NotificationHelper

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class,
        ExperimentalSharedTransitionApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Check and request notification permissions
        checkAndRequestNotificationPermission()

        setContent {
            // Calculate window size class for responsive layouts
            val windowSizeClass = calculateWindowSizeClass(this)

            RetaskTheme {
                RetaskApp(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Permission request launcher
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start service
            startNotificationService()
        } else {
            // Permission denied, show explanation
            Toast.makeText(
                this,
                "Notification permission is required for task reminders",
                Toast.LENGTH_LONG
            ).show()

            // Show dialog with more explanation
            showNotificationPermissionRationale()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, start service
                    startNotificationService()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale then request
                    showNotificationPermissionRationale(shouldRequestAfter = true)
                }
                else -> {
                    // No rationale needed, request directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, notification permission is granted by default
            startNotificationService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showNotificationPermissionRationale(shouldRequestAfter: Boolean = false) {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("Task reminders require notification permission to alert you about upcoming tasks. Please enable notifications for this app.")
            .setPositiveButton("Allow") { _, _ ->
                if (shouldRequestAfter) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Open app settings
                    openAppSettings()
                }
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun startNotificationService() {
        val serviceIntent = Intent(this, TaskNotificationService::class.java)

        startForegroundService(serviceIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetaskApp(modifier: Modifier = Modifier) {
    // Get ViewModel instance
    val viewModel: TaskViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Retask",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(elevation = 2.dp) // Reduced shadow for more subtle effect
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(
                viewModel = viewModel,
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}