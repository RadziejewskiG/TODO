package com.radziejewskig.todo.extension

import android.content.Context
import com.google.firebase.Timestamp
import com.radziejewskig.todo.R
import java.util.*

fun Int.addZeroIfLessThan10(): String = if(this < 10) "0$this" else this.toString()

fun Calendar.clearBelowDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.monthStringNameShort(context: Context): String = context.getString(
    when(this.get(Calendar.MONTH)) {
        0 -> R.string.january_
        1 -> R.string.february_
        2 -> R.string.march_
        3 -> R.string.april_
        4 -> R.string.may_
        5 -> R.string.june_
        6 -> R.string.july_
        7 -> R.string.august_
        8 -> R.string.september_
        9 -> R.string.october_
        10 -> R.string.november_
        else -> R.string.december_
    })

fun Calendar.dayOfWeekStringName(context: Context): String = context.getString(
    when(this.get(Calendar.DAY_OF_WEEK)) {
        1 -> R.string.sunday
        2 -> R.string.monday
        3 -> R.string.tuesday
        4 -> R.string.wednesday
        5 -> R.string.thursday
        6 -> R.string.friday
        else -> R.string.saturday
    })

fun Calendar.minute() : Int = this.get(Calendar.MINUTE)
fun Calendar.hour() : Int = this.get(Calendar.HOUR_OF_DAY)
fun Calendar.day() : Int = this.get(Calendar.DAY_OF_MONTH)
fun Calendar.month() : Int = this.get(Calendar.MONTH) + 1
fun Calendar.year() : Int = this.get(Calendar.YEAR)

fun Calendar.hourMinuteString(): String = hour().toString() + ":" + minute().addZeroIfLessThan10()

fun Calendar.dayMonthTimeString(context: Context): String
        = day().toString() + " " + monthStringNameShort(context) + " " + hour() + ":" + minute().addZeroIfLessThan10()

fun Calendar.fullDateString(context: Context): String
        = day().toString() + " " + monthStringNameShort(context) + " " + year() + " " + hour() + ":" + minute().addZeroIfLessThan10()

fun Calendar.isToday(actualCalendar: Calendar) : Boolean {
    return actualCalendar.day() == this.day() && actualCalendar.month() == this.month() && actualCalendar.year() == this.year()
}

fun Calendar.wasYesterday(actualCalendar: Calendar) : Boolean {
    val yesterdayCal: Calendar = actualCalendar.apply { add(Calendar.DAY_OF_YEAR, -1) }
    return yesterdayCal.day() == this.day() && yesterdayCal.month() == this.month() && yesterdayCal.year() == this.year()
}

fun Calendar.wasWithinOneWeek(actualCalendar: Calendar) : Boolean {
    val cal: Calendar = Calendar.getInstance()
    cal.timeInMillis = this.timeInMillis
    cal.clearBelowDay()

    val weekOldCal: Calendar = Calendar.getInstance().apply { timeInMillis = actualCalendar.timeInMillis }
    weekOldCal.clearBelowDay()
    weekOldCal.add(Calendar.DAY_OF_YEAR, -6)

    return cal.timeInMillis > weekOldCal.timeInMillis
}

fun Calendar.wasWithinOneYear(actualCalendar: Calendar) : Boolean {
    val cal: Calendar = Calendar.getInstance()
    cal.timeInMillis = this.timeInMillis
    cal.clearBelowDay()

    val yearOldCal: Calendar = Calendar.getInstance().apply { timeInMillis = actualCalendar.timeInMillis }
    yearOldCal.clearBelowDay()
    yearOldCal.add(Calendar.YEAR, -1)

    return cal.timeInMillis > yearOldCal.timeInMillis
}

fun Timestamp.toDateText(context: Context): String {
    val calendar = Calendar.getInstance().also { it.timeInMillis = this.toDate().time }
    val todayCal = Calendar.getInstance()
    return when {
        calendar.isToday(todayCal) -> {
            calendar.hourMinuteString()
        }
        calendar.wasYesterday(todayCal) -> {
            context.getString(R.string.yesterday) + " " + calendar.hourMinuteString()
        }
        calendar.wasWithinOneWeek(todayCal) -> {
            calendar.dayOfWeekStringName(context) + " " + calendar.hourMinuteString()
        }
        calendar.wasWithinOneYear(todayCal) -> {
            calendar.dayMonthTimeString(context)
        }
        else -> {
            calendar.fullDateString(context)
        }
    }
}
