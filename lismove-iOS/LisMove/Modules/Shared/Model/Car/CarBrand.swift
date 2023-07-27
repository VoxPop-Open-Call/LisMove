//
//  Carbrand.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/08/21.
//

import Foundation

public struct CarBrand: Codable, Hashable{
    
    public var id: Int?
    var name: String?
    
    enum CodingKeys: String, CodingKey{
        case id = "id"
        case name = "name"
    }
    
    public init(id: Int, name: String) {
        self.id = id
        self.name = name
    }
    
}
