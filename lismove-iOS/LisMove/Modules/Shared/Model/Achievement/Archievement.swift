//
//  Archievement.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import Foundation

public class Archievement: Codable{
    
    var duration: Int
    var fullfilled: Bool
    var id: Int
    var logo: String?
    var name: String
    var organization: Int?
    var score: Double = 0.0
    var target: Double
    var user: String
    var value: Int
    var endDate: Int?
    
    enum CodingKeys: String, CodingKey{
        case duration = "duration"
        case fullfilled = "fullfilled"
        case id = "id"
        case logo = "logo"
        case name = "name"
        case organization = "organization"
        case score = "score"
        case target = "target"
        case user = "user"
        case value = "value"
        case endDate = "endDate"
    }
    
    
    public init(duration: Int, fullfilled: Bool, id: Int, logo: String? = nil, name: String, organization: Int?, score: Double = 0.0, target: Double, user: String, value: Int, endDate: Int) {
        self.duration = duration
        self.fullfilled = fullfilled
        self.id = id
        self.logo = logo
        self.name = name
        self.organization = organization
        self.score = score
        self.target = target
        self.user = user
        self.value = value
        self.endDate = endDate
    }
    

    public func getLabelForType(type: Int) -> String{
        
        switch type {
            case 0: return "km iniziativa"
            case 1: return "km casa/lavoro"
                
            case 2: return "sessioni casa/lavoro"
            case 3: return "punti iniziativa"
            case 4: return "km community"
            default: return "km"
            
        }
        
    }
    
    

    
    
}




