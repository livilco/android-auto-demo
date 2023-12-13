package co.livil.nna.androidautodemo

import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import co.livil.nna.androidautodemo.ui.theme.AndroidAutodemoTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationHandler by lazy { NotificationHandler(this) }
    private var replyHandler: Job? = null
    val snackbarHostState = SnackbarHostState()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationHandler.createNotificationChannel()
        getNotificationPermissions()


        setContent {
            AndroidAutodemoTheme {
                val snackbarHostState = remember { snackbarHostState }
                val scope = rememberCoroutineScope()

                NotificationHandler.replies.collectAsState(initial = null).value.let {
                    if (!it.isNullOrEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Reply: ${it}")
                        }
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { _ ->
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Button(onClick = {
                                onPostNotificationClicked()
                            }) {
                                Text(text = "Fire notification")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onPostNotificationClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Notifications are not enabled. Show a dialog or notification to inform the user.
                // Use an intent to open the app's notification settings page.
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            } else {
                // Notifications are enabled. Proceed with posting the notification.
                notificationHandler.sendNotification()
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar("Notification sent")
                }
            }
        } else {
            // For older versions, just send the notification as permission is not required.
            notificationHandler.sendNotification()
            lifecycleScope.launch {
                snackbarHostState.showSnackbar("Notification sent")
            }
        }
    }

    private fun getNotificationPermissions() {
        val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // permission denied or forever denied

                // Notifications are not enabled. Show a dialog or notification to inform the user.
                // Use an intent to open the app's notification settings page.
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            // permission already granted
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar("Please enable notifications")
                }
            } else {
                // first request or forever denied case
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

}