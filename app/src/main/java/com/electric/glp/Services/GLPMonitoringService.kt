package com.electric.glp.Services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class GLPMonitoringService : Service() {

    private lateinit var databaseReference: DatabaseReference
    private var valueEventListener: ValueEventListener? = null
    private var isNotificationSent = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        // Forzar reinicio
        val restartIntent = Intent(applicationContext, this::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 1, restartIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent)

        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        setupFirebaseListener()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "glp_monitoring_service_channel"
        val channelName = "GLP Monitoring Service"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(notificationChannelId, channelName, importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setOngoing(true)
            .setSmallIcon(com.electric.glp.R.drawable.icon_glp)
            .setContentTitle("Monitorieo GLP activo")
            .setContentText("Obteniendo datos del sensor...")
        return notificationBuilder.build()
    }

    private fun setupFirebaseListener() {
        val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)!!

        databaseReference = FirebaseDatabase.getInstance().getReference("device/$deviceId")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val glpValue = snapshot.child("glp").getValue(Int::class.java) ?: return
                if (glpValue > 800 && !isNotificationSent) {
                    sendAlert("Alerta de GLP", "Nivel de GLP alto: $glpValue"+"ppm")
                    isNotificationSent = true
                } else if (glpValue <= 800) {
                    isNotificationSent = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        }
        databaseReference.addValueEventListener(valueEventListener!!)
    }


    private fun sendAlert(title: String, message: String) {
        val notificationId = 101  // ID único para la notificación

        // Verificar si ya está mostrando la notificación
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications =
            notificationManager.activeNotifications

        if (activeNotifications?.any { it.id == notificationId } == true) {
            // Si la notificación ya se está mostrando y el estado no ha cambiado, no hacer nada
            return
        }

        // Si la notificación no se encuentra activa, proceder a mostrar una nueva
        val channelId = "glp_alert_channel"  // ID del canal para notificaciones
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, "GLP Alertas", importance)
        notificationChannel.description = "Notificaciones de alertas de GLP"
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setAutoCancel(true)
            .setSmallIcon(com.electric.glp.R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)

        notificationManager.notify(notificationId, notificationBuilder.build())

        // Actualizar la bandera de notificación enviada
        isNotificationSent = true
    }

    override fun onDestroy() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        super.onDestroy()
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener!!)
            notificationManager.cancel(101)
        }
        isNotificationSent = false
    }
}
