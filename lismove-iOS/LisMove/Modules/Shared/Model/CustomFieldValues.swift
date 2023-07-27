//
//  CustomFieldValues.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/10/21.
//

import Foundation

public class CustomFieldValues: Codable {
    var customField, enrollment, id: Int?
    var value: Bool?

    enum CodingKeys: String, CodingKey{
        case customField = "customField"
        case enrollment = "enrollment"
        case id = "id"
        case value = "value"
    }
    
    
    public init(customField: Int?, enrollment: Int?, id: Int?, value: Bool?) {
        self.customField = customField
        self.enrollment = enrollment
        self.id = id
        self.value = value
    }
}
