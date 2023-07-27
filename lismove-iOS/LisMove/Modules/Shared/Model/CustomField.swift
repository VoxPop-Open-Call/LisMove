//
//  CustomField.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/10/21.
//

import Foundation

public class CustomField: Codable {
    var customFieldDescription: String?
    var id: Int?
    var name: String?
    var organization, type: Int?
    
    enum CodingKeys: String, CodingKey{
        case customFieldDescription = "customFieldDescription"
        case id = "id"
        case name = "name"
        case organization = "organization"
        case type = "type"
    }

 public init(customFieldDescription: String?, id: Int?, name: String?, organization: Int?, type: Int?) {
        self.customFieldDescription = customFieldDescription
        self.id = id
        self.name = name
        self.organization = organization
        self.type = type
    }
}
