//
//  CarModels.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/08/21.
//

import Foundation


public struct CarModel: Codable, Hashable{
    
    var brand: CarBrand?
    public var id: Int?
    var name: String?
    
    enum CodingKeys: String, CodingKey{
        case brand = "brand"
        case id = "id"
        case name = "name"
    }
    
    public init(brand: CarBrand, id: Int, name: String) {
        self.brand = brand
        self.id = id
        self.name = name
    }
    
}
