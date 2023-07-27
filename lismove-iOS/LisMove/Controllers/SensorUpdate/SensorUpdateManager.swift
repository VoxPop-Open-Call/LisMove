//
//  SensorUpdateManager.swift
//  LisMove
//
//

import Foundation
import Bugsnag

class SensorUpdateManager {
    public static let LISMOVE_K2_HARDWARE_VERSION = "V1.2"
    public static let LISMOVE_K2_LASTEST_SOFTWARE_VERSION = "V3.11.0"
    public static let LISMOVE_K3_HARDWARE_VERSION = "HW1.0.0S"
    public static let LISMOVE_K3_HARDWARE_VERSION_2 = "V1.0.0S"
    public static let LISMOVE_K3_LASTEST_SOFTWARE_VERSION = "V3.13.7"
    
    public enum LismoveSensorHardware {
        case K2
        case K3
        case NA
    }
    
    public static func needsUpdate(hardwareVersion: String?, softwareVersion: String?) -> Bool {
        print("Checking sensor updates")
        
        if (hardwareVersion == nil) {
            print("No hardware version provided. Aborting update.")
            return false
        }
        
        if (softwareVersion == nil) {
            print("No software version provided. Aborting update.")
            return false
        }
        
        let sensorHardwareVersion = SensorUpdateManager.getSensorType(hardwareVersion: hardwareVersion!)
        
        if (sensorHardwareVersion == LismoveSensorHardware.K3) {
            print("Detected K3")
            if (softwareVersion == SensorUpdateManager.LISMOVE_K3_LASTEST_SOFTWARE_VERSION) {
            print("K3 is updated")
                return false
            } else {
            print("K3 is not updated")
                return true
            }
        } else if (sensorHardwareVersion == LismoveSensorHardware.K2) {
            print("Detected K2")
            if (softwareVersion == SensorUpdateManager.LISMOVE_K2_LASTEST_SOFTWARE_VERSION) {
                print("K2 is updated")
                return false
            } else {
                print("K2 is not updated")
                return true
            }
        } else {
            print("Detected incompatible version")
            return false
        }
    }
    
    public static func getSensorType(hardwareVersion: String?) -> LismoveSensorHardware {
        if (hardwareVersion == nil) { return LismoveSensorHardware.NA }
        
        if (hardwareVersion == SensorUpdateManager.LISMOVE_K3_HARDWARE_VERSION || hardwareVersion == SensorUpdateManager.LISMOVE_K3_HARDWARE_VERSION_2) {
            return LismoveSensorHardware.K3
        } else if (hardwareVersion == SensorUpdateManager.LISMOVE_K2_HARDWARE_VERSION) {
            return LismoveSensorHardware.K2
        } else {
            let message = "Sensor not recognized, hardware version: " + (hardwareVersion ?? "N/A")
            let exception = NSException(name:NSExceptionName(rawValue: "Ble Sensor "),
                                        reason: message,
                                        userInfo:nil)
        
            Bugsnag.notify(exception)
            print(message)
            return LismoveSensorHardware.NA
        }
    }
}
