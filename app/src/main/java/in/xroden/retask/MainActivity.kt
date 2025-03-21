package `in`.xroden.retask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.ui.screens.MainScreen
import `in`.xroden.retask.ui.theme.RetaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp() {
    // Sample tasks for testing
    val tasks = listOf(
        Task(
            id = "1",
            title = "Coffee with Mel",
            dueDate = System.currentTimeMillis() - 30 * 60 * 1000, // 30 minutes ago
            colorHex = "#F6D8CE" // Peach color
        ),
        Task(
            id = "2",
            title = "Water plants",
            dueDate = System.currentTimeMillis() - 20 * 60 * 1000, // 20 minutes ago
            colorHex = "#D5F5E3" // Mint green
        ),
        Task(
            id = "3",
            title = "Get to the airport!",
            dueDate = System.currentTimeMillis(), // Now
            colorHex = "#FADBD8" // Light pink
        ),
        Task(
            id = "4",
            title = "Go to the gym",
            dueDate = System.currentTimeMillis() + 10 * 60 * 1000, // 10 minutes from now
            colorHex = "#D6EAF8" // Light blue
        )
    )
    MainScreen(
        tasks = tasks,
        onCompleteTask = { /* Handle complete task */ },
        onSnoozeTask = { /* Handle snooze task */ }
    )
}