package com.thornton.yuki.lunchmoneytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.thornton.yuki.lunchmoneytracker.entity.DailyWorkSchedule
import com.thornton.yuki.lunchmoneytracker.helper.NotificationHelper
import com.thornton.yuki.lunchmoneytracker.helper.WorkHelper
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager

private const val TAG = "LUNCH_DEV_LOW_BAL_NTF"

class LowBalanceNotificationWorker(context : Context, params : WorkerParameters): Worker(context, params)  {
    private lateinit var storage: StorageManager

    override fun doWork(): Result {

        Log.d(TAG, "LowBalanceNotificationWorker Started")
        storage = StorageManager.getInstance(applicationContext)

        val notificationSettings = storage.getLowBalanceNotificationSchedule()
        if (notificationSettings.activated) {
            sendNotification()
            scheduleNext(notificationSettings)
        } else {
            storage.removeExpenseSupervisingWorkerId()
        }

        return Result.success()
    }

    private fun sendNotification() {
        val balance = storage.getBalance()
        if (balance >= storage.getLowBalanceThreshold()) return

        NotificationHelper.registerChannel(applicationContext)
        val intent = NotificationHelper.createPendingIntent(applicationContext, MainActivity::class.java)
        val message = "Current balance is $balance"
        NotificationHelper.sendNotification(applicationContext, R.drawable.ic_local_cafe_black_24dp, "Low balance", message, intent)

    }

    private fun scheduleNext(schedule: DailyWorkSchedule) {
        val id = WorkHelper.scheduleTomorrow(LowBalanceNotificationWorker::class.java, schedule.time)
        storage.setLowBalanceNotificationWorkerId(id)
    }
}