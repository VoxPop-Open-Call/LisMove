//
//  GPSPrimitive.swift
//  LisMoveSensorSdk
//
//  Created by Francesco Paolo Dellaquila on 25/11/21.
//

import Foundation

public struct GPSPrimitive{
    
    public var lat: Double
    public var lng: Double
    public var altitude: Double
    public var timestamp: Date
    
    
    public init(lat: Double, lng: Double, altitude: Double, timestamp: Date) {
        self.lat = lat
        self.lng = lng
        self.altitude = altitude
        self.timestamp = timestamp
    }
    
    

    
    
}
