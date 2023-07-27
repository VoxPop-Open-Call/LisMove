//
//  OrganizationSettings_DB.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation
import RealmSwift


public class OrganizationSettings_DB: Object{

    @objc public dynamic var id: String?
    public dynamic var organizationId = RealmOptional<Int>()
    @objc public dynamic var isActiveUrbanPoints = false
    @objc public dynamic var startDateUrbanPoints: String?
    @objc public dynamic var endDateUrbanPoints: String?
    @objc public dynamic var startDateBonus: String?
    @objc public dynamic var endDateBonus: String?
    @objc public dynamic var endTimeBonus: String?
    @objc public dynamic var startTimeBonus: String?
    @objc public dynamic var multiplier = 1
    @objc public dynamic var isActiveTimeSlotBonus = false
    @objc public dynamic var exclusiveCustomField = false
    @objc public dynamic var ibanRequirement = false
    @objc public dynamic var homeWorkRefund = false
    @objc public dynamic var initiativeRefund = false
    
    public convenience init(organizationId: Int?, isActiveUrbanPoints: Bool = false, startDateUrbanPoints: String?, endDateUrbanPoints: String?, startDateBonus: String?, endDateBonus: String?, endTimeBonus: String?, startTimeBonus: String?, multiplier: Int = 1, isActiveTimeSlotBonus: Bool = false, exclusiveCustomField: Bool = false, ibanRequirement: Bool = false, homeWorkRefund: Bool = false, initiativeRefund: Bool = false) {
        self.init()
        
        self.id = UUID().uuidString
        self.organizationId.value = organizationId
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
    
    
    public override static func primaryKey() -> String? {
        return "id"
    }
    
    public func asOrganizationSettings() -> OrganizationSettings{
        return OrganizationSettings(organizationId: self.organizationId.value,
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
