package co.livil.nna.androidautodemo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationHandler(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "nna_channel_id"

        const val ACTION_REPLY = "co.livil.nna.androidautodemo.NOTIFICATION_ACTION_REPLY"
        const val ACTION_MARK_AS_READ = "co.livil.nna.androidautodemo.NOTIFICATION_ACTION__MARK_AS_READ"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val EXTRA_NOTIFICATION_ID_KEY = "extra_notification_id_key"

        private val _replies: MutableStateFlow<String?> = MutableStateFlow(null)
        val replies = _replies.asStateFlow()

        fun postReply(reply: String) {
            _replies.value = reply
        }
    }

    private var notificationCount = 0
    private val currentNotificationId
        get() = 1000 + notificationCount

    fun createNotificationChannel() {
        val channelId = CHANNEL_ID
        val channelName = context.getString(R.string.notification__channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = context.getString(R.string.notification__channel_description)
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createMessageNotification() // The method you created earlier
        notificationManager.notify(currentNotificationId, notification)
    }

    private fun createMessageNotification(): Notification {
        val channelId = CHANNEL_ID

        notificationCount++

        val sender = Person.Builder().setName("Sender").build()
        val style = NotificationCompat.MessagingStyle(sender)
            .addMessage("New message $notificationCount", System.currentTimeMillis(), sender)

        // Create the Notification
        val builder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            //setContentTitle("New Message ${notificationCount}")
            //setContentText("You've received a new message.")
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)

            // Both of these actions are required for Android Auto to display the notification
            addAction(createReplyAction())
            addAction(createMarkAsReadAction())

            setStyle(style)
        }

        return builder.build()
    }

    private fun createReplyAction(): NotificationCompat.Action {
        // Intent for the reply action
        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_NOTIFICATION_ID_KEY, currentNotificationId)
        }
        val replyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, replyFlags)

        // RemoteInput for the reply action
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel("Reply")
            build()
        }

        // Reply action
        return NotificationCompat.Action.Builder(R.drawable.ic_reply_icon, "Reply", replyPendingIntent)
            // Provide context to what firing Action does.
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            // The action doesn't show any UI (it's a requirement for apps to not show UI for Android Auto).
            .setShowsUserInterface(false)
            // Add remote input to the action
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun createMarkAsReadAction(): NotificationCompat.Action {
        // PendingIntent for mark as read action
        val readIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_AS_READ
            putExtra(EXTRA_NOTIFICATION_ID_KEY, currentNotificationId)
        }
        val readPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            readIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(R.drawable.ic_read_icon, "Mark as Read", readPendingIntent)
            // Provide context to what firing Action does.
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            // The action doesn't show any UI (it's a requirement for apps to not show UI for Android Auto).
            .setShowsUserInterface(false)
            .build()
    }

}