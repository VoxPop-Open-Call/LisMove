//
//  OrganizationSettings.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/01/22.
//

import Foundation

// MARK: - OrganizationSettings
public class OrganizationSettings: Codable {
    
    var organizationId: Int?
    var isActiveUrbanPoints: Bool = true
    var startDateUrbanPoints: String?
    var endDateUrbanPoints: String?
    var startDateBonus: String?
    var endDateBonus: String?
    var endTimeBonus: String?
    var startTimeBonus: String?
    var multiplier: Int = 1
    var isActiveTimeSlotBonus: Bool = false
    var exclusiveCustomField: Bool = false
    var ibanRequirement: Bool = false
    var homeWorkRefund:Bool = false
    var initiativeRefund: Bool = false
    
    enum CodingKeys: String, CodingKey{
        case organizationId = "organizationId"
        case isActiveUrbanPoints = "isActiveUrbanPoints"
        case startDateUrbanPoints = "startDateUrbanPoints"
        case endDateUrbanPoints = "endDateUrbanPoints"
        case startDateBonus = "startDateBonus"
        case endDateBonus = "endDateBonus"
        case endTimeBonus = "endTimeBonus"
        case startTimeBonus = "startTimeBonus"
        case multiplier = "multiplier"
        case isActiveTimeSlotBonus = "isActiveTimeSlotBonus"
        case exclusiveCustomField = "exclusiveCustomField"
        case ibanRequirement = "ibanRequirement"
        case homeWorkRefund = "isActiveHomeWorkRefunds"
        case initiativeRefund = "isActiveUrbanPathRefunds"
    }
    
    
    public init(organizationId: Int?, isActiveUrbanPoints: Bool = true, startDateUrbanPoints: String? = nil, endDateUrbanPoints: String? = nil, startDateBonus: String? = nil, endDateBonus: String? = nil, endTimeBonus: String? = nil, startTimeBonus: String? = nil, multiplier: Int = 1, isActiveTimeSlotBonus: Bool = false, exclusiveCustomField: Bool = false, ibanRequirement: Bool = false, homeWorkRefund: Bool = false, initiativeRefund: Bool = false) {
        self.organizationId = organizationId
        self.isActiveUrbanPoints = isActiveUrbanPoints
        self.startDateUrbanPoints = startDateUrbanPoints
        self.endDateUrbanPoints = endDateUrbanPoints
        self.startDateBonus = startDateBonus
        self.endDateBonus = endDateBonus
        self.endTimeBonus = endTimeBonus
        self.startTimeBonus = startTimeBonus
        self.multiplier = multiplier
        self.isActiveTimeSlotBonus = isActiveTimeSlotBonus
        self.exclusiveCustomField = exclusiveCustomField
        self.ibanRequirement = ibanRequirement
        self.homeWorkRefund = homeWorkRefund
        self.initiativeRefund = initiativeRefund
    }
    
    static func getFromResponse(id: Int, response: [SettingsResponse]) -> OrganizationSettings{
    
        let isActiveUrbaPoints = getBoleanFrom(response: response, key: CodingKeys.isActiveUrbanPoints.rawValue)
        let isActiveTimeSlotBonus = getBoleanFrom(response: response, key: CodingKeys.isActiveTimeSlotBonus.rawValue)
        let exclusiveCustomField = getBoleanFrom(response: response, key: CodingKeys.exclusiveCustomField.rawValue)
        let ibanRequirement = getBoleanFrom(response: response, key: CodingKeys.ibanRequirement.rawValue)
        let homeWorkRefund = getBoleanFrom(response: response, key: CodingKeys.homeWorkRefund.rawValue)
        let initiativeRefund = getBoleanFrom(response: response, key: CodingKeys.initiativeRefund.rawValue)

        return OrganizationSettings(organizationId: id, isActiveUrbanPoints: isActiveUrbaPoints, isActiveTimeSlotBonus: isActiveTimeSlotBonus, exclusiveCustomField: exclusiveCustomField, ibanRequirement: ibanRequirement, homeWorkRefund: homeWorkRefund, initiativeRefund: initiativeRefund)
    }
    
    static func getBoleanFrom(response: [SettingsResponse], key: String) -> Bool{
        return Bool(response.first(where: {$0.organizationSetting == key})?.value ?? "false") ?? false
    }
    
    
    public func asOrganizationSettings_DB() -> OrganizationSettings_DB{
        return OrganizationSettings_DB(organizationId: self.organizationId,
                                    isActiveUrbanPoints: self.isActiveUrbanPoints,
                                    startDateUrbanPoints: self.startDateUrbanPoints,
                                    endDateUrbanPoints: self.endDateUrbanPoints,
                                    startDateBonus: self.startDateBonus,
                                    endDateBonus: self.endDateBonus,
                                    endTimeBonus: self.endTimeBonus,
                                    startTimeBonus: self.startTimeBonus,
                                    multiplier: self.multiplier,
                                    isActiveTimeSlotBonus: self.isActiveTimeSlotBonus,
                                    exclusiveCustomField: self.exclusiveCustomField,
                                    ibanRequirement: self.ibanRequirement,
                                    homeWorkRefund: self.homeWorkRefund,
                                    initiativeRefund: self.initiativeRefund)
    }

    
}
