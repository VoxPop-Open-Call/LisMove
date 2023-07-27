//
//  CarGeneration.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/08/21.
//

import Foundation

public struct CarGeneration: Codable, Hashable{
    
    var model: CarModel?
    var modelYear: Int?
    public var id: Int?
    var name: String?
    
    enum CodingKeys: String, CodingKey{
        case model = "model"
        case modelYear = "modelYear"
        case id = "id"
        case name = "name"
    }
    
    public init(model: CarModel,modelYear: Int, id: Int, name: String) {
        self.model = model
        self.modelYear = modelYear
        self.id = id
        self.name = name
    }
    
}
