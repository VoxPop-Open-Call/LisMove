//
//  SessionPoint.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 23/08/21.
//

import Foundation
import RealmSwift
import Realm

public class SessionPoint: Object, Codable {
    
    @objc public dynamic var id = UUID().uuidString
    @objc public dynamic var distance = 0.0
    @objc public dynamic var multiplier = 1.0
    public dynamic var organizationId = RealmOptional<Int>()
    public dynamic var points = RealmOptional<Int>()
    @objc public dynamic var sessionId: String?
    @objc public dynamic var organizationTitle: String?
    public dynamic var refundStatus = RealmOptional<Int>()

    enum CodingKeys: String, CodingKey {
        case distance = "distance"
        case multiplier = "multiplier"
        case organizationId = "organizationId"
        case points = "points"
        case sessionId = "sessionId"
        case organizationTitle = "organizationTitle"
        case refundStatus = "refundStatus"
    }
    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.distance = try container.decodeIfPresent(Double.self, forKey: .distance) ?? 0.0
        self.multiplier = try container.decodeIfPresent(Double.self, forKey: .multiplier) ?? 1.0
        self.organizationId.value = try container.decodeIfPresent(Int.self, forKey: .organizationId) ?? nil
        self.points.value = try container.decodeIfPresent(Int.self, forKey: .points) ?? 0
        self.sessionId = try container.decodeIfPresent(String.self, forKey: .sessionId) ?? ""
        self.organizationTitle = try container.decodeIfPresent(String.self, forKey: .organizationTitle) ?? ""
        self.refundStatus.value = try container.decodeIfPresent(Int.self, forKey: .points) ?? nil
        super.init()
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(self.distance , forKey: .distance)
        try container.encodeIfPresent(self.multiplier , forKey: .multiplier)
        try container.encodeIfPresent(self.organizationId , forKey: .organizationId)
        try container.encodeIfPresent(self.points , forKey: .points)
        try container.encodeIfPresent(self.sessionId, forKey: .sessionId)
    }

    public required override init() {
        super.init()
    }
    
    public override static func primaryKey() -> String? {
        return "id"
    }
    
    func getRefundStatus() -> String{
        switch(refundStatus.value){
            case 0:
                return "Sessione nazionale"
            case 1:
                return "L'intero importo è stato riconosciuto"
            case 2:
                return "Parzialmente riconosciuto per raggiungimento soglia giornaliera"
            case 3:
                return "Parzialmente riconosciuto per raggiungimento soglia mensile"
            case 4:
                return "Parzialmente riconosciuto per raggiungimento soglia iniziativa"
            case 5 :
                return "Non riconosciuto per raggiungimento soglia giornaliera"
            case 6:
                return "Non riconosciuto per raggiungimento soglia mensile"
            case 7:
                return "Non riconosciuto per raggiungimento soglia iniziativa"
             default:
                return ""

        }
    }
}
