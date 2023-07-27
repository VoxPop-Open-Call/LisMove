//
//  Enrollment.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/07/21.
//

import Foundation

public class Enrollment: Codable{
    var activationDate: Int?
    var code: String?
    var endDate, startDate: Double?
    var id, lastModifiedDate, organization: Int?
    var points: Int?
    var uid, user: String?
    
    enum CodingKeys: String, CodingKey {
        case activationDate = "activationDate"
        case code = "code"
        case endDate = "endDate"
        case id = "id"
        case lastModifiedDate = "lastModifiedDate"
        case organization = "organization"
        case points = "points"
        case startDate = "startDate"
        case uid = "uid"
        case user = "user"
    }

    public init(activationDate: Int?, code: String?, endDate: Double?, id: Int?, lastModifiedDate: Int?, organization: Int?, points: Int?, startDate: Double?, uid: String?, user: String?) {
        self.activationDate = activationDate
        self.code = code
        self.endDate = endDate
        self.id = id
        self.lastModifiedDate = lastModifiedDate
        self.organization = organization
        self.points = points
        self.startDate = startDate
        self.uid = uid
        self.user = user
    }
    
    func isClosed() -> Bool{
        let endDate =  Date(timeIntervalSince1970: TimeInterval(endDate! / 1000))
        return endDate < Date()
    }
    
    func getDateIntervalString() -> String{
        if let startDate = startDate, let endDate = endDate {
            return "Attiva dal \(DateTimeUtils.getReadableCompactDate(date: startDate)) al \(DateTimeUtils.getReadableCompactDate(date: endDate))"
        }else{
            return ""
        }

    }
    
    public func asEnrollment_DB() -> Enrollment_DB {
        return Enrollment_DB(
            activationDate: self.activationDate,
            code: self.code,
            endDate: self.endDate,
            startDate: self.startDate,
            id: self.id,
            lastModifiedDate: self.lastModifiedDate,
            organization: self.organization,
            points: self.points,
            uid: self.uid,
            user: self.user)
    }
}
