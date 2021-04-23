package com.mycompany.astroweather

import android.os.Handler
import android.os.Looper

class LoopedTask(private val task: () -> Unit, private val intervalMillis: Long) {
    private val handler = Handler(Looper.getMainLooper())

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                task()
            } finally {
                handler.postDelayed(this, intervalMillis)
            }
        }
    }

    @Synchronized
    fun start() = handler.post(updateTask)

    @Synchronized
    fun stop() = handler.removeCallbacks(updateTask)
}