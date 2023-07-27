//
//  Sensor.swift
//  
//
//  Created by Francesco Paolo Dellaquila on 24/05/21.
//

import Foundation
import Realm
import RealmSwift
import LisMoveSensorSdk

public class Sensor: Object, Codable {
    public dynamic var bikeType: BikeType?
    public dynamic var endAssociation = RealmOptional<Int64>()
    @objc public dynamic var firmware: String?
    public dynamic var history = RealmOptional<Int>()
    @objc public dynamic var name: String?
    public dynamic var startAssociation = RealmOptional<Int64>()
    @objc public dynamic var stolen = false
    @objc public dynamic var uuid: String?
    public dynamic var wheelDiameter = RealmOptional<Int>()
    @objc public dynamic var hubCoefficient: Double = 1.0
    
    enum CodingKeys: String, CodingKey {
        case bikeType = "bikeType"
        case endAssociation = "endAssociation"
        case firmware = "firmware"
        case history = "history"
        case name = "name"
        case startAssociation = "startAssociation"
        case stolen = "stolen"
        case uuid = "uuid"
        case wheelDiameter = "wheelDiameter"
        case hubCoefficient = "hubCoefficient"
    }
    
    

    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.bikeType = try container.decodeIfPresent(BikeType.self, forKey: .bikeType) ?? BikeType.Tradizionale
        self.endAssociation.value = try container.decodeIfPresent(Int64.self, forKey: .endAssociation)
        self.firmware = try container.decodeIfPresent(String.self, forKey: .firmware)
        self.history.value = try container.decodeIfPresent(Int.self, forKey: .history)
        self.name =  try container.decodeIfPresent(String.self, forKey: .name)
        self.startAssociation.value =  try container.decodeIfPresent(Int64.self, forKey: .startAssociation)
        self.stolen  =  try container.decodeIfPresent(Bool.self, forKey: .stolen) ?? false
        self.uuid = try container.decodeIfPresent(String.self, forKey: .uuid)
        self.wheelDiameter.value = try container.decodeIfPresent(Int.self, forKey: .wheelDiameter)
        self.hubCoefficient = try container.decodeIfPresent(Double.self, forKey: .hubCoefficient) ?? 1.0
        super.init()
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(self.bikeType, forKey: .bikeType)
        try container.encodeIfPresent(self.endAssociation.value, forKey: .endAssociation)
        try container.encodeIfPresent(self.firmware, forKey: .firmware)
        try container.encodeIfPresent(self.history.value, forKey: .history)
        try container.encodeIfPresent(self.name, forKey: .name)
        try container.encodeIfPresent(self.startAssociation.value, forKey: .startAssociation)
        try container.encodeIfPresent(self.stolen, forKey: .stolen)
        try container.encodeIfPresent(self.uuid, forKey: .uuid)
        try container.encodeIfPresent(self.wheelDiameter.value, forKey: .wheelDiameter)
        try container.encodeIfPresent(self.hubCoefficient, forKey: .hubCoefficient)

    }

    public required override init() {
        super.init()
    }
    
    public static func sensorFromDevice(device: LisMoveDevice, startAssociation: Int64)-> Sensor{
        let sensor = Sensor()
        sensor.uuid = device.macAddress
        sensor.name = device.name
        //add sensor data
        sensor.bikeType = BikeType(rawValue: UserDefaults.standard.string(forKey: "bikeType")!)
        sensor.wheelDiameter.value = UserDefaults.standard.integer(forKey: "wheelDiameter")
        sensor.startAssociation.value = startAssociation
        
        return sensor
    }
    
    public static func deviceFromSensor(sensor: Sensor)-> LisMoveDevice{
        return LisMoveDevice(name: sensor.name!, macAddress: sensor.uuid!)
    }
    
    
    public override static func primaryKey() -> String? {
        return "uuid"
    }
    
    
    public static func getWheel(type: String) -> Int  {
        switch type {
        case "29''":
            return 736
        case "28'' (700mm)":
            return 700
        case "27.5'' (650mm)":
            return 650
        case "27''":
            return 685
        case "26''":
            return 660
        case "24'' (600mm)":
            return 600
        case "22'' (550mm)":
            return 550
        case "20'' (500mm)":
            return 500
        case "18'' (450mm)":
            return 450
        case "16'' (400mm)":
            return 400
        case "14'' (350mm)":
            return 350
        case "12''":
            return 300
        default:
            return 300
        }
    }
    
    public static func getWheelDescription(type: Int) -> String  {
        switch type {
        case 736:
            return "29''"
        case 700:
            return "28'' (700mm)"
        case 650:
            return "27.5'' (650mm)"
        case 685:
            return "27''"
        case 660:
            return "26''"
        case 600:
            return "24'' (600mm)"
        case 550:
            return "22'' (550mm)"
        case 500:
            return "20'' (500mm)"
        case 450:
            return "18'' (450mm)"
        case 400:
            return "16'' (400mm)"
        case 350:
            return "14'' (350mm)"
        case 300:
            return "12''"
        default:
            return "28'' (700mm)"
        }
    }
    
}


public enum BikeType: String, Codable {
    case Tradizionale = "Tradizionale (muscolare)"
    case Elettrica = "Elettrica (assistita)"
}




extension BikeType {
    public init(from decoder: Decoder) throws {
        self = try BikeType(rawValue: decoder.singleValueContainer().decode(RawValue.self)) ?? .Tradizionale
    }
}
