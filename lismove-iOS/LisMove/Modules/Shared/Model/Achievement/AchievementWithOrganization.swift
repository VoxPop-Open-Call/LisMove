//
//  AwardWithOrganization.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 04/12/21.
//

import Foundation

public struct ArchievementWithOrganization{
    
    public var archievement: Archievement
    public var organization: Organization?
    
    public func asAchievementItemUI() ->  AchievementItemUI {
        var countDown: String? = nil
        if let endDate = self.archievement.endDate{
            let days = DateTimeUtils.getDaysTillDate(endDate: Double(endDate))
            countDown = "-\(days) GIORNI"
        }
        
        return AchievementItemUI(
            name: self.archievement.name,
            organizationLabel: (self.organization != nil) ? self.organization!.title ?? "" : "",
            percentage: (self.archievement.fullfilled) ? 1 : self.archievement.score / self.archievement.target,
            limitedTarget: (self.archievement.fullfilled) ? self.archievement.target : self.archievement.score,
            percentageValue: "\((self.archievement.fullfilled) ? self.archievement.target.getRoundedString(): self.archievement.score.getRoundedString())/\(self.archievement.target.getIntString()) \(self.archievement.getLabelForType(type: self.archievement.value))",
            imageUrl: self.archievement.logo,
            fullfilled: self.archievement.fullfilled,
            daysCounter: countDown
        )
    }
    
}



public struct AchievementItemUI{
    var name: String
    var organizationLabel: String
    var percentage: Double
    var limitedTarget: Double
    var percentageValue: String
    var imageUrl: String?
    var fullfilled: Bool
    var daysCounter: String? = nil
    
    
}
