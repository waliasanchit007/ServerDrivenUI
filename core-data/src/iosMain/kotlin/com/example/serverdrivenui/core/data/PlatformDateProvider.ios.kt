package com.example.serverdrivenui.core.data

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSISO8601DateFormatter

actual object PlatformDateProvider {
    actual fun today(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.stringFromDate(NSDate())
    }
    
    actual fun now(): String {
        return NSISO8601DateFormatter().stringFromDate(NSDate())
    }
    
    actual fun addMonths(date: String, months: Int): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        val nsDate = formatter.dateFromString(date) ?: NSDate()
        val calendar = NSCalendar.currentCalendar
        val newDate = calendar.dateByAddingUnit(
            unit = NSCalendarUnitMonth,
            value = months.toLong(),
            toDate = nsDate,
            options = 0u
        )
        return formatter.stringFromDate(newDate ?: nsDate)
    }
    
    actual fun getDayOfWeek(dateStr: String): Int {
        // Returns 0=Sunday, 1=Monday, ..., 6=Saturday to match JS convention
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        val nsDate = formatter.dateFromString(dateStr) ?: NSDate()
        val calendar = NSCalendar.currentCalendar
        val weekday = calendar.component(NSCalendarUnitWeekday, nsDate)
        // NSCalendar weekday: 1=Sunday, 2=Monday, ..., 7=Saturday
        // Convert to 0=Sunday, 1=Monday, ..., 6=Saturday
        return (weekday - 1).toInt()
    }
}
