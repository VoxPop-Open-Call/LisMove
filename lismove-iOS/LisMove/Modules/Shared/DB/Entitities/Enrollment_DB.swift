//
//  Enrollement_DB.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation
import RealmSwift


public class Enrollment_DB: Object {
    
    public dynamic var activationDate = RealmOptional<Int>()
    @objc public dynamic var code: String?
    public dynamic var endDate = RealmOptional<Double>()
    public dynamic var startDate = RealmOptional<Double>()
    public dynamic var id = RealmOptional<Int>()
    public dynamic var lastModifiedDate = RealmOptional<Int>()
    public dynamic var organization = RealmOptional<Int>()
    public dynamic var points = RealmOptional<Int>()
    @objc public dynamic var uid: String?
    @objc public dynamic var user: String?
    
    public convenience init(activationDate: Int?, code: String?, endDate: Double?, startDate: Double?, id: Int?, lastModifiedDate: Int?, organization: Int?, points: Int?, uid: String?, user: String?) {
        self.init()
        self.activationDate.value = activationDate
        self.code = code
        self.endDate.value = endDate
        self.startDate.value = startDate
        self.id.value = id
        self.lastModifiedDate.value = lastModifiedDate
        self.organization.value = organization
        self.points.value = points
        self.uid = uid
        self.user = user
    }
    
    public override static func primaryKey() -> String? {
        return "id"
    }
    
    public func asEnrollment() -> Enrollment{
        return Enrollment(
            activationDate: self.activationDate.value,
            code: self.code,
            endDate: self.endDate.value,
            id: self.id.value,
            lastModifiedDate: self.lastModifiedDate.value,
            organization: self.organization.value,
            points: self.points.value,
            startDate: self.startDate.value,
            uid: self.uid,
            user: self.user)
    }
    
}
