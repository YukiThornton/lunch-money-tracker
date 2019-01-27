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
    private var lowBalNtfDialog: MaterialDialog? = null

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

        val lowBalNotificationSchedule = storage.getLowBalanceNotificationSchedule()
        lowBalanceNotificationSwitch().also {
            it.isChecked = lowBalNotificationSchedule.activated
            it.setOnCheckedChangeListener(this::onLowBalanceNotificationSwitchChanged)
        }
        updateLowBalanceNotificationSummary()
    }

    private fun expenseSupervisingSwitch(): Switch = findViewById(R.id.exp_spv_switch)
    private fun expenseSupervisingSummary(): TextView = findViewById(R.id.exp_spv_setting_summary)
    private fun lowBalanceNotificationSwitch(): Switch = findViewById(R.id.low_bal_ntf_switch)
    private fun lowBalanceNotificationSummary(): TextView = findViewById(R.id.low_bal_ntf_setting_summary)

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

    private fun onLowBalanceNotificationSwitchChanged(button: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "lowBalanceNotificationSwitch changed")
        if (isChecked) {
            openLowBalanceNotificationSettingDialog(storage.getLowBalanceNotificationSchedule().activated)
        } else {
            val schedule = storage.getLowBalanceNotificationSchedule()
            storage.setLowBalanceNotificationSchedule(schedule.activate(false))
            updateLowBalanceNotificationSummary()
            val previousWorkerId = storage.getLowBalanceNotificationWorkerId()
            if (previousWorkerId != null) {
                WorkHelper.cancel(previousWorkerId)
                storage.removeLowBalanceNotificationWorkerId()
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

    private fun updateLowBalanceNotificationSummary() {
        if (!storage.getLowBalanceNotificationSchedule().activated) {
            lowBalanceNotificationSummary().visibility = View.INVISIBLE
            return
        }
        lowBalanceNotificationSummary().apply {
            visibility = View.VISIBLE
            text = getLowBalanceNotificationSummaryText()
        }
    }

    private fun getLowBalanceNotificationSummaryText(): String {
        val threshold = storage.getLowBalanceThreshold()
        val time = storage.getLowBalanceNotificationSchedule().time
        return "When balance < $threshold at ${time.format(TIME_FORMATTER)} everyday"
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
                // check if previous task exists
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

    private fun openLowBalanceNotificationSettingDialog(alreadyActivated: Boolean) {
        val positiceButtonText =
            if (alreadyActivated) R.string.edit_low_balance_notification_setting_dialog_positive_apply
            else R.string.edit_low_balance_notification_setting_dialog_positive_activate
        lowBalNtfDialog = MaterialDialog(this)
            .title(R.string.edit_low_balance_notification_setting_dialog_title)
            .customView(R.layout.edit_low_balance_notification_dialog)
            .positiveButton(positiceButtonText) {
                val timeStr = lowBalanceNotificationTimeView().text.toString()
                val thresholdStr = lowBalanceNotificationThresholdView().text.toString()
                Log.d(TAG, "Low balance notification settings changed: time=$timeStr, threshold=$thresholdStr")
                val time = LocalTime.parse(timeStr, TIME_FORMATTER)
                storage.setLowBalanceNotificationSchedule(DailyWorkSchedule.of(true, time))
                storage.setLowBalanceThreshold(thresholdStr.toInt())
                updateLowBalanceNotificationSummary()
                // check if previous task exists
                val id = WorkHelper.schedule(LowBalanceNotificationWorker::class.java, time)
                storage.setLowBalanceNotificationWorkerId(id)
            }
            .negativeButton(R.string.edit_low_balance_notification_setting_dialog_negative) {
                lowBalanceNotificationSwitch().isChecked = false;
            }

        changeLowBalanceNotificationTime(storage.getLowBalanceNotificationSchedule().time)
        lowBalanceNotificationThresholdView().setText(storage.getLowBalanceThreshold().toString(), TextView.BufferType.NORMAL)
        lowBalNtfDialog!!.show()
    }

    private fun expenseSupervisingTimeView(): TextView = expSpvDialog!!.findViewById(R.id.exp_spv_time_input)
    private fun expenseSupervisingAmountView(): EditText = expSpvDialog!!.findViewById(R.id.exp_spv_amount_input)
    private fun lowBalanceNotificationTimeView(): TextView = lowBalNtfDialog!!.findViewById(R.id.low_bal_ntf_time_input)
    private fun lowBalanceNotificationThresholdView(): EditText = lowBalNtfDialog!!.findViewById(R.id.low_bal_ntf_threshold_input)

    private fun changeExpenseSupervisingTime(localTime: LocalTime) {
        changeExpenseSupervisingTime(localTime.hour, localTime.minute)
    }

    private fun changeExpenseSupervisingTime(hourOfDay: Int, minute: Int) {
        expenseSupervisingTimeView().text = LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
    }

    private fun changeLowBalanceNotificationTime(localTime: LocalTime) {
        changeLowBalanceNotificationTime(localTime.hour, localTime.minute)
    }

    private fun changeLowBalanceNotificationTime(hourOfDay: Int, minute: Int) {
        lowBalanceNotificationTimeView().text = LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
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

    fun onLowBalanceNotificationTimeClicked(view: View) {
        val time = storage.getLowBalanceNotificationSchedule().time
        val dialog = TimePickerDialog(this, this::onLowBalanceNotificationTimeChanged, time.hour, time.minute, true)
        dialog.show()
    }

    private fun onLowBalanceNotificationTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int){
        Log.d(TAG, "Show expenseSupervisingTime Dialog")
        changeLowBalanceNotificationTime(hourOfDay, minute)
    }
}
