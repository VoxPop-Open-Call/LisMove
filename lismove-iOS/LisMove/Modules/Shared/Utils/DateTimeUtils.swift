//
//  DateTimeUtils.swift
//  LisMove
//
//

import Foundation
class DateTimeUtils {
    static func getReadableMonthYear(dateTime: String) -> String{
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        if let date = dateFormatter.date(from: dateTime){
            dateFormatter.dateFormat = "MMM/yy"
            return dateFormatter.string(from: date)
        }
        return ""
    }
    
    static func getCurrentTimestamp() -> Double{
        return Date().timeIntervalSince1970 * 1000
    }
    static func getDaysTillDate(endDate: Double)-> Int{
        let today = Date.init()
        let endDate = Date.init(timeIntervalSince1970: endDate/1000)
        return numberOfDaysBetween(today, and: endDate)
    }
    static func numberOfDaysBetween(_ from: Date, and to: Date) -> Int {
    let calendar = Calendar(identifier: .gregorian)
    let fromDate = calendar.startOfDay(for: from) // <1>
    let toDate = calendar.startOfDay(for: to) // <2>
    let numberOfDays = calendar.dateComponents([.day], from: fromDate, to: toDate) // <3>

    return numberOfDays.day!
    }
    
    static func getReadableCompactDate(date: Double) -> String{
        let dateVar = Date.init(timeIntervalSince1970: TimeInterval(date/1000))
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd/MM/yyyy"
        return dateFormatter.string(from: dateVar)
    }
    
    static func getReadableLongDateTime(date: Double) -> String{
        let dateVar = Date.init(timeIntervalSince1970: TimeInterval(date/1000))
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd MMMM yyyy HH:mm"
        return dateFormatter.string(from: dateVar)
    }
    
    
    
}
