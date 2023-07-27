//
//  CarMdification.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/08/21.
//

import Foundation

public struct CarModification: Codable, Hashable{
    
    var co2: String?
    var engineDisplacement: Int?
    var fuel, fuelConsumptionExtraurban, fuelConsumptionUrban: String?
    var generation: CarGeneration?
    var id: Int?
    
    enum CodingKeys: String, CodingKey{
        case co2 = "co2"
        case engineDisplacement = "engineDisplacement"
        case fuel = "fuel"
        case fuelConsumptionExtraurban = "fuelConsumptionExtraurban"
        case fuelConsumptionUrban = "fuelConsumptionUrban"
        case generation = "generation"
        case id = "id"
    }
    
    public init(co2: String, engineDisplacement: Int, fuel: String, fuelConsumptionExtraurban: String, fuelConsumptionUrban: String, generation: CarGeneration, id: Int) {
        self.co2 = co2
        self.engineDisplacement = engineDisplacement
        self.fuel = fuel
        self.fuelConsumptionUrban = fuelConsumptionUrban
        self.fuelConsumptionExtraurban = fuelConsumptionExtraurban
        self.generation = generation
        self.id = id
    }
    
}
