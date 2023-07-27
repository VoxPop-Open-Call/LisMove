//
//  Ranking.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import Foundation

// MARK: - Ranking
public class Ranking: Codable {
    var endDate: Double?
    var filter: Int?
    var filterValue: String?
    var id, organization: Int?
    var rankingPositions: [RankingPosition]?
    var repeatNum: Int?
    var repeatType: RankingRepeat?
    var startDate: Double?
    var title: String?
    var value: Int?
    
    let valueLabel = ["km iniziativa", "km casa/lavoro", "sessioni casa/lavoro", "punti iniziativa", "punti community", "km community" ]
    
    enum CodingKeys: String, CodingKey{
        case endDate = "endDate"
        case filter = "filter"
        case id = "id"
        case organization = "organization"
        case rankingPositions = "rankingPositions"
        case repeatNum = "repeatNum"
        case repeatType = "repeatType"
        case startDate = "startDate"
        case title = "title"
        case value = "value"
    }
    
    public enum RankingRepeat: Int, Codable {
        case NONE = 0
        case MONTH = 1
        case CUSTOM = 2
    }
    
    
    public init(endDate: Double?, filter: Int?, filterValue: String?, id: Int?, organization: Int?, rankingPositions: [RankingPosition]?, repeatNum: Int?, repeatType: RankingRepeat?, startDate: Double?, title: String?, value: Int?) {
        self.endDate = endDate
        self.filter = filter
        self.filterValue = filterValue
        self.id = id
        self.organization = organization
        self.rankingPositions = rankingPositions
        self.repeatNum = repeatNum
        self.repeatType = repeatType
        self.startDate = startDate
        self.title = title
        self.value = value
    }
    
    func getDaysTillEndString() -> String{
        if let endDate = endDate{
            return "- \(DateTimeUtils.getDaysTillDate(endDate: endDate)) \n GIORNI"
        }else{
            return ""
        }
    }
    
    func hasDaysTillEnding()->Bool{
        if let endDate = endDate{
            return DateTimeUtils.getDaysTillDate(endDate: endDate)>=0
        }else{
            return false
        }
   }
    
    func hasValidDate() -> Bool{
        return startDate != nil
    }
    
    func getDateIntervalLabel() -> String? {
        if(startDate == nil){
            return nil
        }else if(endDate == nil){
            return "Dal \(DateTimeUtils.getReadableCompactDate(date: startDate!))"
        }else{
            return "Dal \(DateTimeUtils.getReadableCompactDate(date: startDate!)) al \(DateTimeUtils.getReadableCompactDate(date: endDate!))"
        }
    }
    
    func getValueLabel() -> String{
        guard let value = value, value < valueLabel.count else{
            return "punti community"
        }
        return valueLabel[value]
    }
}

// RankingPosition.swift

import Foundation

// MARK: - RankingPosition
public class RankingPosition: Codable {
    var avatarURL: String?
    var points: Double?
    var position: Int?
    var username: String?

    enum CodingKeys: String, CodingKey{
        case avatarURL = "avatarUrl"
        case points = "points"
        case position = "position"
        case username = "username"
    }
    
    
    public init(avatarURL: String?, points: Double?, position: Int?, username: String?) {
        self.avatarURL = avatarURL
        self.points = points
        self.position = position
        self.username = username
    }
    
    func getPositionLabel() -> String{
        guard let position = position else {return ""}
        if(position == 0  ) {return ""}
        else if(position == 11) {return "TH"}
        else if(position == 12){ return "TH"}
        else if(position == 13) {return "TH"}
        
        let lastDigit = position  % 10
        switch lastDigit {
        case 1:
            return  "ST"
        case 2:
            return "ND"
        case 3:
            return "RD"
        default:
            return"TH"
        }
    }
}
