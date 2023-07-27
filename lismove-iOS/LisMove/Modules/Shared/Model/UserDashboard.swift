//
//  UserDashboard.swift
//  LisMove
//
//

import Foundation
public class UserDashboard: Codable{
    var co2: Double?
    var distance: Double?
    var euro: Double?
    var messages: Int?
    var sessionNumber: Int?
    var dailyDistance: [UserDistanceStats]?
    
    enum CodingKeys: String, CodingKey{
        case co2 = "co2"
        case distance = "distance"
        case euro = "euro"
        case messages = "messages"
        case sessionNumber = "sessionNumber"
        case dailyDistance = "dailyDistance"
    }
    
    public init(co2: Double?, distance: Double?, euro: Double?, messages: Int?, sessionNumber: Int?, dailyDistance: [UserDistanceStats]?){
        self.co2 = co2 ?? 0.0
        self.distance = distance ?? 0.0
        self.euro = distance ?? 0.0
        self.messages = messages ?? 0
        self.sessionNumber = sessionNumber ?? 0
        self.dailyDistance = dailyDistance ?? [UserDistanceStats]()
    }
    
    func getConvertedC02() -> Double{
        if let co2 = co2 {
            return co2 / 1000
        }else{
            return 0.0
        }
    }
    
}

public class UserDistanceStats: Codable{
    var day: String?
    var distance: Double?
    
    enum CodingKeys: String, CodingKey{
        case day = "day"
        case distance = "distance"
    }
    
    public init(day: String?, distance: Double?){
        self.day = day
        self.distance = distance
    }
    
    public func getDayString()-> String{
        return DateTimeUtils.getReadableMonthYear(dateTime: self.day ?? "")
    }
}
