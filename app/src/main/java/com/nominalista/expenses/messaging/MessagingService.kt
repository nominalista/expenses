package com.nominalista.expenses.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nominalista.expenses.R
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    /**
     * Remote message will be handled when app is in the foreground. Otherwise system will show
     * notification automatically.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Received remote message.")

        message.notification?.let { handleRemoteNotification(it) }
    }

    private fun handleRemoteNotification(remoteNotification: RemoteMessage.Notification) {
        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(this, R.color.expenses_blue))

        remoteNotification.title?.let { notificationBuilder.setContentTitle(it) }
        remoteNotification.body?.let { notificationBuilder.setContentText(it) }

        createNotificationChannel()

        NotificationManagerCompat.from(applicationContext)
            .notify(generateNotificationId(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name_default)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = getString(R.string.channel_description_default)
            }

            // Register the channel with the system service.
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateNotificationId() = Random.nextInt(1, 100)

    companion object {

        private const val TAG = "MessagingService"

        private const val CHANNEL_ID = "Expenses"
    }
}