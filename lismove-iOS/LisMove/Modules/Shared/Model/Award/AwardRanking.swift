//
//  Award.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import Foundation

public class AwardRanking: Codable {
    var description: String?
    var id: Int?
    var imageURL, name: String?
    var position: Int?
    var range: String?
    var ranking, value: Int?
    var type: AwardRankingType?
    
    
    public enum AwardRankingType: Int, Codable {
        case TYPE_MONEY = 0
        case TYPE_POINTS = 1
    }
    
    enum CodingKeys: String, CodingKey{
        case description = "description"
        case id = "id"
        case imageURL = "imageUrl"
        case name = "name"
        case position = "position"
        case range = "range"
        case ranking = "ranking"
        case type = "type"
        case value = "value"
    }
    
    

    public init(description: String?, id: Int?, imageURL: String?, name: String?, position: Int?, range: String?, ranking: Int?, type: AwardRankingType?, value: Int?) {
        self.description = description
        self.id = id
        self.imageURL = imageURL
        self.name = name
        self.position = position
        self.range = range
        self.ranking = ranking
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
