//
//  LisMoveSdkConstants.swift
//  LisMoveSensorSdk
//
//

import Foundation
import CoreBluetooth

public struct BTConstants {
    public static let CadenceService         = "1816"
    public static let CadenceBattery         = "180F"
    public static let firmwareService        = "180A"
    public static let CSCMeasurementUUID     = "2A5B"
    public static let CSCFeatureUUID         = "2A5C"
    public static let SensorLocationUUID     = "2A5D"
    public static let ControlPointUUID       = "2A55"
    public static let batteryUUID            = "2A19"
    public static let firmwareUUID           = "2A28"
    public static let hardwareUUID           = "2A27"
    public static let controlServiceUUID     = "FD00"
    public static let k3NotifyServiceUUID    = "FD09"
    public static let controlCharUUID        = "FD0A"
    public static let WheelFlagMask:UInt8    = 0b01
    public static let CrankFlagMask:UInt8    = 0b10
    public static let DefaultWheelSize:UInt32   =  700     //2170  // In millimiters. 700x30 (by default my bike's wheels) :)
    public static let TimeScale              = 1024.0

    public static let devices = ["Cycplus S1", "Lis Move s1", "BK463S-000001", "Lis Move k2", "BK5SC 0091683", "Lis Move k3"]
    public static let otaDevicesServices = [CBUUID(string: "FE59"), CBUUID(string: "8ec90001-f315-4f60-9fb8-838830daea50")]
 
    public static let SensorUserDefaultsKey = "lastsensorused"

}

