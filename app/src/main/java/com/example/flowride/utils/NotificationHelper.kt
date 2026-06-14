package com.example.flowride.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.flowride.R

object NotificationHelper {

    fun showReservationConfirmation(
        context: Context,
        vehicleName: String,
        date: String,
        location: String,
        total: Int
    ) {
        val channelId = "flowride_reservations"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Rezervacije",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Obavijesti o rezervacijama"
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Rezervacija potvrđena!")
            .setContentText("$vehicleName • $date • $location")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Vozilo: $vehicleName\n" +
                                "Datum: $date\n" +
                                "Lokacija: $location\n" +
                                "Ukupno: $${total}"
                    )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}