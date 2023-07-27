//
//  LisMoveDeviceParser.swift
//  LisMoveSdk
//
//

import Foundation
import CoreBluetooth

extension CBPeripheral{
    func toLisMoveDevice() -> LisMoveDevice{
        return LisMoveDevice(name: name ?? "", macAddress: identifier.uuidString)
    }
}
