//
//  Partial.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 23/08/21.
//

import Foundation
import Realm
import RealmSwift
import LisMoveSensorSdk

// MARK: - Partial
public class Partial: Object, Codable{
    

    public enum PartialType: Int, Codable {
        case Unknown = 0
        case Start = 1
        case End = 2
        case InProgress = 3
        case Pause = 4
        case Resume = 5
        case Session = 6
        case System = 7
        case Service = 8
        case Ble = 9
        case Other = 11
    }
    
    @objc public dynamic var uuid = UUID().uuidString
    public dynamic var timestamp = RealmOptional<Int64>()
    @objc public dynamic var altitude = 0.0
    @objc public dynamic var latitude = 0.0
    @objc public dynamic var longitude = 0.0
    @objc public dynamic var type = PartialType.Start.rawValue
    @objc public dynamic var deltaRevs: Int64 = 0
    
    
    //gyro delta distnce
    public dynamic var gyroDeltaDistance = RealmOptional<Double>()
    //
    public dynamic var gyroDistance = RealmOptional<Double>()
    public dynamic var valid = RealmOptional<Bool>()
    
    
    
    //COMPANION DATA
    @objc public dynamic var wheelTime: Int = 0
    @objc public dynamic var speed: Double = 0.0
    @objc public dynamic var gpsDistance: Double = 0.0

    @objc public dynamic var elapsedTimeInMillis: Int64 = 0
    @objc public dynamic var averageSpeed: Double = 0.0
    @objc public dynamic var isGpsPartial: Bool = false
    
    //sensor battery
    public dynamic var  batteryLevel = RealmOptional<Int>()

    // Points
    @objc public dynamic var urban: Bool = false

    // Log data
    @objc public dynamic var rawData_wheel: Int64 = 0
    @objc public dynamic var rawData_ts: Int64 = 0
    @objc public dynamic var extra: String?

    // Backup data to restore service in case of sudden crash
    @objc public dynamic var totalGyroDistanceInKm: Double = 0.0
    @objc public dynamic var totalGpsOnlyDistanceInKm: Double = 0.0
    @objc public dynamic var totalGpsCacheDistanceInKm: Double = 0.0
    @objc public dynamic var sessionElapsedTimeInSec: Int = 0
    @objc public dynamic var isDebug: Bool = false

    
    enum CodingKeys: String, CodingKey {
        case altitude = "altitude"
        case deltaRevs = "deltaRevs"
        case latitude = "latitude"
        case longitude = "longitude"
        case gyroDeltaDistance = "sensorDistance"
        case timestamp = "timestamp"
        case urban = "urban"
        case valid = "valid"
        case type = "type"
        case rawData_ts = "rawData_ts"
        case rawData_wheel = "rawData_wheel"
        case extra = "extra"
        case isDebug = "isDebug"
    }
    
    
    public init(uuid: String = UUID().uuidString, timestamp: Int64?, altitude: Double = 0.0, latitude: Double = 0.0, longitude: Double = 0.0, type: Int = PartialType.Start.rawValue, deltaRevs: Int64?, gyroDeltaDistance: Double?, gyroDistance: Double?, valid: Bool? = nil, wheelTime: Int = 0, speed: Double = 0.0, gpsDistance: Double = 0.0, elapsedTimeInMillis: Int64 = 0, averageSpeed: Double = 0.0, isGpsPartial: Bool = false, batteryLevel: Int?, urban: Bool = false, rawData_wheel: Int64 = 0, rawData_ts: Int64 = 0, extra: String? = nil, totalGyroDistanceInKm: Double = 0.0, totalGpsOnlyDistanceInKm: Double = 0.0, totalGpsCacheDistanceInKm: Double = 0.0, sessionElapsedTimeInSec: Int = 0) {
        self.uuid = uuid
        self.timestamp.value = timestamp
        self.altitude = altitude
        self.latitude = latitude
        self.longitude = longitude
        self.type = type
        self.deltaRevs = deltaRevs ?? 0
        self.gyroDeltaDistance.value = gyroDeltaDistance
        self.gyroDistance.value = gyroDistance
        self.valid.value = valid
        self.wheelTime = wheelTime
        self.speed = speed
        self.gpsDistance = gpsDistance
        self.elapsedTimeInMillis = elapsedTimeInMillis
        self.averageSpeed = averageSpeed
        self.isGpsPartial = isGpsPartial
        self.batteryLevel.value = batteryLevel
        self.urban = urban
        self.rawData_wheel = rawData_wheel
        self.rawData_ts = rawData_ts
        self.extra = extra
        self.totalGyroDistanceInKm = totalGyroDistanceInKm
        self.totalGpsOnlyDistanceInKm = totalGpsOnlyDistanceInKm
        self.totalGpsCacheDistanceInKm = totalGpsCacheDistanceInKm
        self.sessionElapsedTimeInSec = sessionElapsedTimeInSec
        
    }

    
 
    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.altitude = try container.decodeIfPresent(Double.self, forKey: .altitude) ?? 0.0
        self.deltaRevs = try container.decodeIfPresent(Int64.self, forKey: .deltaRevs) ?? 0
        self.latitude = try container.decodeIfPresent(Double.self, forKey: .latitude) ?? 0.0
        self.longitude = try container.decodeIfPresent(Double.self, forKey: .longitude) ?? 0.0
        self.gyroDeltaDistance.value = try container.decodeIfPresent(Double.self, forKey: .gyroDeltaDistance) ?? 0.0
        self.timestamp.value = try container.decodeIfPresent(Int64.self, forKey: .timestamp)
        self.urban = try container.decodeIfPresent(Bool.self, forKey: .urban) ?? false
        self.valid.value = try container.decodeIfPresent(Bool.self, forKey: .valid)
        self.type = try container.decodeIfPresent(Int.self, forKey: .type) ?? 0
        super.init()
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(self.altitude , forKey: .altitude)
        try container.encode(self.deltaRevs , forKey: .deltaRevs)
        try container.encode(self.latitude , forKey: .latitude)
        try container.encode(self.longitude , forKey: .longitude)
        try container.encodeIfPresent(self.gyroDeltaDistance, forKey: .gyroDeltaDistance)
        try container.encodeIfPresent(self.timestamp.value , forKey: .timestamp)
        //try container.encodeIfPresent(self.urban , forKey: .urban)
        //try container.encodeIfPresent(self.valid , forKey: .valid)
        try container.encodeIfPresent(self.type , forKey: .type)
        try container.encodeIfPresent(self.rawData_ts , forKey: .rawData_ts)
        try container.encodeIfPresent(self.rawData_wheel , forKey: .rawData_wheel)
        try container.encodeIfPresent(self.extra , forKey: .extra)
    }

    public required override init() {
        super.init()
    }
    
    
    public override static func primaryKey() -> String? {
        return "uuid"
    }
    
    
    
    public func getTotalDistance() -> Double {
        return (gyroDistance.value ?? 0) + gpsDistance
    }
    
    static func getEmpty()->Partial{
        var empty =  Partial()
        empty.timestamp.value = Date().millisecondsSince1970
        return empty
    }
    
    
    /*public func getTotalDistance() -> Double {
        return totalGyroDistanceInKm + totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm
    }*/
    
    public func getReadableElapsedTime() -> String{
        return TimeInterval(Int64(elapsedTimeInMillis) / 1000).hourMinuteSecond
    }
    
    private func formatTimeInt(_ value: Int?)-> String{
        let displayNumber = value ?? 0
        return String(format: "%02d", displayNumber)
    }
    

}


/*
extension Partial {
    
    public func asSessionStateUpdate() -> LisMoveSessionUpdate{
        var update = LisMoveSessionUpdate(
            deltaRevs: <#T##UInt32#>,
            cumulativeWheel: <#T##UInt32#>,
            wheelTime: <#T##Double#>,
            speed: <#T##Double#>,
            distance: <#T##Double#>,
            battery: <#T##Int?#>,
            firmwareV: <#T##String?#>)

        return update
    }
    
    
    
}
*/
