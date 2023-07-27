//
//  File.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import Foundation

struct Fountain: Codable{
    var name: String
    var lat: Double
    var lng: Double
    var uid: String? = nil
    var createdAt: Double? = nil
    var deleted: Bool? = false
    var deletedAt: Double? = nil
    var deletedBy: String? = nil
    
    var id: String?
    
    var isDeleted: Bool{
        get { deleted ?? false}
    }
    enum CodingKeys: String, CodingKey{
        case name, lat, lng, uid, createdAt, deleted, deletedAt, deletedBy
    }
    
    
}
