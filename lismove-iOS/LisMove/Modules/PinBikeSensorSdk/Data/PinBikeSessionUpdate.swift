//
//  LisMoveSessionUpdate.swift
//  LisMoveSensorSdk
//
//

import Foundation
public struct LisMoveSessionUpdate{
    
    

    public var deltaRevs: UInt32
    public var cumulativeWheel: UInt32
    public var wheelTime: Double
    public var speed: Double
    public var distance: Double
    public var battery: Int?
    public var firmwareV: String?
    public var avgSpeed: Double{
        distance/wheelTime
    }
    
    var distanceFormatter:LengthFormatter = {
      
      let formatter = LengthFormatter()
      formatter.numberFormatter.maximumFractionDigits = 1
      
      return formatter
    }()
    
    
    init(deltaRevs: UInt32, cumulativeWheel: UInt32, wheelTime: Double, speed: Double, distance: Double, battery: Int? = nil, firmwareV: String? = nil) {
        self.deltaRevs = deltaRevs
        self.cumulativeWheel = cumulativeWheel
        self.wheelTime = wheelTime
        self.speed = speed
        self.distance = distance
        self.battery = battery
        self.firmwareV = firmwareV
    }
    
    
    
     public func speedFormatted() -> String {
          return String(speed*3.6) //distanceFormatter.string(fromValue: speed*3.6, unit: .kilometer) //+ NSLocalizedString("/h", comment:"(km) Per hour")
    }
    
    public func avgSpeedFormatted() -> String {
         return String(avgSpeed*3.6) //distanceFormatter.string(fromValue: avgSpeed*3.6, unit: .kilometer) //+ NSLocalizedString("/h", comment:"(km) Per hour")
   }
    
    public func distanceFormatted() -> String {
         return distanceFormatter.string(fromMeters: distance)
   }
    

    
    
    
    
}
