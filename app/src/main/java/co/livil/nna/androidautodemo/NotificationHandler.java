package co.livil.nna.androidautodemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import org.jetbrains.annotations.NotNull;

import co.livil.nna.androidautodemo.R.drawable;
import co.livil.nna.androidautodemo.R.string;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

public final class NotificationHandler {
    private int notificationCount;
    private final Context context;
    @NotNull
    public static final String CHANNEL_ID = "nna_channel_id";
    @NotNull
    public static final String ACTION_REPLY = "co.livil.nna.androidautodemo.NOTIFICATION_ACTION_REPLY";
    @NotNull
    public static final String ACTION_MARK_AS_READ = "co.livil.nna.androidautodemo.NOTIFICATION_ACTION__MARK_AS_READ";
    @NotNull
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    @NotNull
    public static final String EXTRA_NOTIFICATION_ID_KEY = "extra_notification_id_key";
    private static final MutableStateFlow _replies = StateFlowKt.MutableStateFlow((Object)null);
    @NotNull
    public static final StateFlow replies;

    private final int getCurrentNotificationId() {
        return 1000 + this.notificationCount;
    }

    public final void createNotificationChannel() {
        String channelId = "nna_channel_id";
        String channelName = this.context.getString(string.notification__channel_name);
        NotificationChannel channel = new NotificationChannel(channelId, (CharSequence)channelName, NotificationManager.IMPORTANCE_DEFAULT);

        channel.setDescription(this.context.getString(string.notification__channel_description));
        Object service = this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.app.NotificationManager");
        } else {
            NotificationManager notificationManager = (NotificationManager)service;
            notificationManager.createNotificationChannel(channel);
        }
    }

    public final void sendNotification() {
        Object service = this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.app.NotificationManager");
        } else {
            NotificationManager notificationManager = (NotificationManager)service;
            Notification notification = this.createMessageNotification();
            notificationManager.notify(this.getCurrentNotificationId(), notification);
        }
    }

    private final Notification createMessageNotification() {
        String channelId = "nna_channel_id";
        this.notificationCount++;

        Person sender = (new Person.Builder()).setName((CharSequence)"Sender").build();
        NotificationCompat.MessagingStyle style = (new NotificationCompat.MessagingStyle(sender))
                .addMessage((CharSequence)("New message " + this.notificationCount), System.currentTimeMillis(), sender);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, channelId);
        builder.setSmallIcon(drawable.ic_notification_icon);
        builder.setPriority(1);
        builder.setAutoCancel(true);
        builder.addAction(this.createReplyAction());
        builder.addAction(this.createMarkAsReadAction());
        builder.setStyle((NotificationCompat.Style)style);

        return builder.build();
    }

    private final NotificationCompat.Action createReplyAction() {
        Intent replyIntent = new Intent(this.context, NotificationActionReceiver.class);
        replyIntent.setAction("co.livil.nna.androidautodemo.NOTIFICATION_ACTION_REPLY");
        replyIntent.putExtra("extra_notification_id_key", this.getCurrentNotificationId());

        int replyFlags = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            replyFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this.context, 0, replyIntent, replyFlags);
        RemoteInput.Builder builder = new RemoteInput.Builder("key_text_reply");
        builder.setLabel((CharSequence)"Reply");
        RemoteInput remoteInput = builder.build();

        NotificationCompat.Action remoteAction = (new NotificationCompat.Action.Builder(drawable.ic_reply_icon, (CharSequence)"Reply", replyPendingIntent))
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .setShowsUserInterface(false)
                .addRemoteInput(remoteInput)
                .build();

        return remoteAction;
    }

    private final NotificationCompat.Action createMarkAsReadAction() {
        Intent intent = new Intent(this.context, NotificationActionReceiver.class);
        intent.setAction("co.livil.nna.androidautodemo.NOTIFICATION_ACTION__MARK_AS_READ");
        intent.putExtra("extra_notification_id_key", this.getCurrentNotificationId());

        PendingIntent readPendingIntent = PendingIntent.getBroadcast(this.context, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Action remoteAction = (new NotificationCompat.Action.Builder(drawable.ic_read_icon, (CharSequence)"Mark as Read", readPendingIntent))
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                .setShowsUserInterface(false)
                .build();

        return remoteAction;
    }

    public NotificationHandler(@NotNull Context context) {
        super();
        this.context = context;
    }

    static {
        replies = FlowKt.asStateFlow(_replies);
    }

    @NotNull
    public final StateFlow getReplies() {
        return NotificationHandler.replies;
    }

    public static final void postReply(@NotNull String reply) {
        NotificationHandler._replies.setValue(reply);
    }
}