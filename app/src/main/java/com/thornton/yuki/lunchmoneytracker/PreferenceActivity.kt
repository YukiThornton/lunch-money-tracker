package com.thornton.yuki.lunchmoneytracker

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.thornton.yuki.lunchmoneytracker.entity.DailyWorkSchedule
import com.thornton.yuki.lunchmoneytracker.helper.WorkHelper
import com.thornton.yuki.lunchmoneytracker.storage.StorageManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val TAG = "LUNCH_DEV_PREF_ACT"
private const val TIME_FORMAT = "HH:mm"
private val TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT)

class PreferenceActivity : AppCompatActivity() {

    private lateinit var storage: StorageManager
    private var expSpvDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        storage = StorageManager.getInstance(applicationContext)
        val expenseSupervisingSchedule = storage.getExpenseSupervisingSchedule()

        expenseSupervisingSwitch().also {
            it.isChecked = expenseSupervisingSchedule.activated
            it.setOnCheckedChangeListener(this::onExpenseSupervisingSwitchChanged)
        }
        updateExpenseSupervisingSummary()
    }

    private fun expenseSupervisingSwitch(): Switch = findViewById(R.id.exp_spv_switch)
    private fun expenseSupervisingSummary(): TextView = findViewById(R.id.exp_spv_setting_summary)

    private fun onExpenseSupervisingSwitchChanged(button: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "expenseSupervisingSwitch changed")
        if (isChecked) {
            openExpenseSupervisingDialog(storage.getExpenseSupervisingSchedule().activated)
        } else {
            val schedule = storage.getExpenseSupervisingSchedule()
            storage.setExpenseSupervisingSchedule(schedule.activate(false))
            updateExpenseSupervisingSummary()
            val previousWokerId = storage.getExpenseSupervisingWorkerId()
            if (previousWokerId != null) {
                WorkHelper.cancel(previousWokerId)
                storage.removeExpenseSupervisingWorkerId()
            }
        }
    }

    private fun updateExpenseSupervisingSummary() {
        if (!storage.getExpenseSupervisingSchedule().activated) {
            expenseSupervisingSummary().visibility = View.INVISIBLE
            return
        }
        expenseSupervisingSummary().apply {
            visibility = View.VISIBLE
            text = getExpenseSupervisingSummaryText()
        }
    }

    private fun getExpenseSupervisingSummaryText(): String {
        val amount = storage.getDefaultDailyExpenseAmount()
        val time = storage.getExpenseSupervisingSchedule().time
        return "\"-$amount\" at ${time.format(TIME_FORMATTER)} everyday"
    }


    private fun openExpenseSupervisingDialog(alreadyActivated: Boolean) {
        val positiveButtonText =
            if(alreadyActivated) R.string.edit_expense_supervising_dialog_positive_apply
            else R.string.edit_expense_supervising_dialog_positive_activate
        expSpvDialog = MaterialDialog(this)
            .title(R.string.edit_expense_supervising_dialog_title)
            .customView(R.layout.edit_expense_supervising_dialog)
            .positiveButton(positiveButtonText) {
                val timeStr = expenseSupervisingTimeView().text.toString()
                val amountStr = expenseSupervisingAmountView().text.toString()
                Log.d(TAG, "expenseSupervising settings changed: time=$timeStr, amount=$amountStr")
                val time = LocalTime.parse(timeStr, TIME_FORMATTER)
                storage.setExpenseSupervisingSchedule(DailyWorkSchedule.of(true, time))
                storage.setDefaultDailyExpenseAmount(amountStr.toInt())
                updateExpenseSupervisingSummary()
                // check if task exists
                val id = WorkHelper.schedule(ExpenseSupervisingWorker::class.java, time)
                storage.setExpenseSupervisingWorkerId(id)
            }
            .negativeButton(R.string.edit_expense_supervising_dialog_negative) {
                expenseSupervisingSwitch().isChecked = false
            }
        changeExpenseSupervisingTime(storage.getExpenseSupervisingSchedule().time)
        expenseSupervisingAmountView().setText(storage.getDefaultDailyExpenseAmount().toString(), TextView.BufferType.NORMAL)
        expSpvDialog!!.show()
    }

    private fun expenseSupervisingTimeView(): TextView = expSpvDialog!!.findViewById(R.id.exp_spv_time_input)
    private fun expenseSupervisingAmountView(): EditText = expSpvDialog!!.findViewById(R.id.exp_spv_amount_input)

    private fun changeExpenseSupervisingTime(localTime: LocalTime) {
        changeExpenseSupervisingTime(localTime.hour, localTime.minute)
    }

    private fun changeExpenseSupervisingTime(hourOfDay: Int, minute: Int) {
        expenseSupervisingTimeView().text = LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
    }

    fun onExpenseSupervisingTimeClicked(view: View) {
        val time = storage.getExpenseSupervisingSchedule().time
        val dialog = TimePickerDialog(this, this::onExpenseSupervisingTimeChanged, time.hour, time.minute, true)
        dialog.show()
    }

    private fun onExpenseSupervisingTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int){
        Log.d(TAG, "Show expenseSupervisingTime Dialog")
        changeExpenseSupervisingTime(hourOfDay, minute)
    }
}
