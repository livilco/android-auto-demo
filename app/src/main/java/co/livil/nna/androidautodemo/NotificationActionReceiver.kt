package co.livil.nna.androidautodemo

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class NotificationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            NotificationHandler.ACTION_REPLY -> {
                // Cancel the notification after reply
                val notificationId = intent.getIntExtra(NotificationHandler.EXTRA_NOTIFICATION_ID_KEY, 0)
                val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)

                val replyText = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(NotificationHandler.KEY_TEXT_REPLY)
                replyText?.let {
                    NotificationHandler.postReply(it.toString())
                }
            }
            NotificationHandler.ACTION_MARK_AS_READ -> {
                // We're not doing anythign here, but you could do something like mark the message as read in the DB
            }
        }
    }
}