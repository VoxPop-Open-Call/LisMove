//
//  OfflineSensorReading.swift
//  LisMove
//
//

import Foundation

public struct LisMoveSensorHistoryElement {
    public var startLap: Int?
    public var stopLap: Int?
    public var startUtc: Int?
    public var stopUtc: Int?
    
    public init(startLap: Int?, stopLap: Int?, startUtc: Int?, stopUtc: Int?){
        self.startLap = startLap
        self.stopLap = stopLap
        self.startUtc = startUtc
        self.stopUtc = stopUtc
    }
}
