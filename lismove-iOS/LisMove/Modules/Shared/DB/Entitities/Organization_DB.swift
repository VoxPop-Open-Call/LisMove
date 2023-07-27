//
//  Organization_DB.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation
import RealmSwift

public class Organization_DB: Object{
    
    public dynamic var id = RealmOptional<Int>()
    @objc public dynamic var title: String?
    @objc public dynamic var notificationLogo: String?
    @objc public dynamic var logo: String?
    @objc public dynamic var initiativeLogo: String?
    @objc public dynamic var geojson: String?
    public dynamic var validation = RealmOptional<Bool>()
    public dynamic var type = RealmOptional<Int>()
    @objc public dynamic var regulation: String?
    @objc public dynamic var termsConditions: String?

    
    public convenience init(id: Int?, title: String?, notificationLogo: String?, logo: String?, initiativeLogo: String?, geojson: String?, validation: Bool?, type: Int?, regulation: String?, termsConditions: String?) {
        self.init()
        self.id.value = id
        self.title = title
        self.notificationLogo = notificationLogo
        self.logo = logo
        self.initiativeLogo = initiativeLogo
        self.geojson = geojson
        self.validation.value = validation
        self.type.value = type
        self.regulation = regulation
        self.termsConditions = termsConditions
    }
    
    public override static func primaryKey() -> String? {
        return "id"
    }
    
    public func asOrganization() -> Organization{
        return Organization(id: self.id.value,
                            type: (self.type.value == 0) ? .PA : .COMPANY,
                            title: self.title,
                            notificationLogo: self.notificationLogo,
                            geojson: self.geojson,
                            validation: self.validation.value,
                            logo: self.logo,
                            initiativeLogo: self.initiativeLogo,
                            regulation: self.regulation,
                            termsConditions: self.termsConditions)
    }
}
