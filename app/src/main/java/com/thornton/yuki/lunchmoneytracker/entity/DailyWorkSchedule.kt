package com.thornton.yuki.lunchmoneytracker.entity

import java.time.LocalTime

data class DailyWorkSchedule private constructor(val activated: Boolean, val time: LocalTime) {
    companion object {
        fun of(activated: Boolean, time: LocalTime): DailyWorkSchedule {
            return DailyWorkSchedule(activated, time)
        }
    }

    fun activate(activated: Boolean): DailyWorkSchedule {
        return DailyWorkSchedule(activated, time)
    }
}