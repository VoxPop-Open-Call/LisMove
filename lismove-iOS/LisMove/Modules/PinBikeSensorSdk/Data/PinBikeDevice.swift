//
//  LisMoveDevice.swift
//  LisMoveSensorSdk
//
//

import Foundation

public struct LisMoveDevice {
    public var name: String
    public var macAddress: String
    
    public init(name: String, macAddress: String){
        self.name = name
        self.macAddress = macAddress
    }
}
