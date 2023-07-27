//
//  Date.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/09/21.
//

import Foundation



extension Date {

    
    public var millisecondsSince1970:Int64 {
        return Int64((self.timeIntervalSince1970 * 1000.0).rounded())
    }

    public init(milliseconds:Int64) {
        self = Date(timeIntervalSince1970: TimeInterval(milliseconds) / 1000)
    }
    
    public static var yesterday: Date { return Date().dayBefore }
    public static var tomorrow:  Date { return Date().dayAfter }
    public var dayBefore: Date {
        return Calendar.current.date(byAdding: .day, value: -1, to: noon)!
    }
    public var dayAfter: Date {
        return Calendar.current.date(byAdding: .day, value: 1, to: noon)!
    }
    public var noon: Date {
        return Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: self)!
    }
    public var month: Int {
        return Calendar.current.component(.month,  from: self)
    }
    public var isLastDayOfMonth: Bool {
        return dayAfter.month != month
    }
    

    public func day(using calendar: Calendar = .current) -> Int {
        calendar.component(.day, from: self)
    }
    public func adding(_ component: Calendar.Component, value: Int, using calendar: Calendar = .current) -> Date {
        calendar.date(byAdding: component, value: value, to: self)!
    }
    public func monthSymbol(using calendar: Calendar = .current) -> String {
        calendar.monthSymbols[calendar.component(.month, from: self)-1]
    }
}


extension TimeInterval {
    public var milliseconds:Int64 {
        return Int64((self * 1_000))
    }
    
    
    var hour: Int {
        Int((self/3600).truncatingRemainder(dividingBy: 3600))
    }
    var minute: Int {
        Int((self/60).truncatingRemainder(dividingBy: 60))
    }
    var second: Int {
        Int(truncatingRemainder(dividingBy: 60))
    }
    
    
    var hourMinuteSecond: String {
           String(format:"%02d:%02d:%02d", hour, minute, second)
    }
}

