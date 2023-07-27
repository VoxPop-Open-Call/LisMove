//
//  Award.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import Foundation

public class AwardAchievement: Codable {
    var achievement: Int?
    var description: String?
    var id: Int?
    var imageURL, name: String?
    var value: Int?
    var type: AwardAchievementType?
    
    public enum AwardAchievementType: Int, Codable{
        case TYPE_MONEY = 0
        case TYPE_POINTS = 1
    }
    
    enum CodingKeys: String, CodingKey{
        case achievement = "achievement"
        case description = "description"
        case id = "id"
        case imageURL = "imageUrl"
        case name = "name"
        case type = "type"
        case value = "value"
    }
    
    

    public init(description: String?, id: Int?, imageURL: String?, name: String?, type: AwardAchievementType?, value: Int?) {
        self.description = description
        self.id = id
        self.imageURL = imageURL
        self.name = name
        self.type = type
        self.value = value
    }
    
    func getTypeLabel() -> String{
        switch type {
        case .TYPE_MONEY: return "euro"
        case .TYPE_POINTS: return "punti"
        default: return ""
        }
    }
}
