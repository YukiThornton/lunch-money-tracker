package com.thornton.yuki.lunchmoneytracker.helper

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class WorkHelper {

    companion object {
        fun schedule(worker: Class<out ListenableWorker>, time: LocalTime): UUID {
            val today = LocalDate.now()
            val date = if (time.isAfter(LocalTime.now())) today else today.plusDays(1)
            val dateTime = LocalDateTime.of(date, time)
            return schedule(worker, dateTime)
        }

        fun schedule(worker: Class<out ListenableWorker>, dateTime: LocalDateTime): UUID {
            val expenseSupervisingWork = OneTimeWorkRequest.Builder(worker)
                .setInitialDelay(Duration.between(LocalDateTime.now(), dateTime))
                .build()
            WorkManager.getInstance().enqueue(expenseSupervisingWork)
            return expenseSupervisingWork.id
        }

        fun scheduleTomorrow(worker: Class<out ListenableWorker>, time: LocalTime): UUID {
            val dateTime = LocalDateTime.of(LocalDate.now().plusDays(1), time)
            return schedule(worker, dateTime)
        }

        fun cancel(id: UUID) {
            WorkManager.getInstance().cancelWorkById(id)
        }
    }

}