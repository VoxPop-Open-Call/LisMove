//
//  Award.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 04/12/21.
//

import Foundation
import UIKit

public class Award: Codable {
    var description: String?
    var imageUrl: String?
    var name: String?
    var organizationId: Int? //TODO: organizationName
    var timestamp: Int?
    var type: AwardType?
    var value: Double? = 0.0
    var achievementId: Int?
    var address: String?
    var city: Int?
    var startDate: Int?
    var endDate: Int?
    var latitude: Double?
    var longitude: Double?
    var number: Int?
    var radius: Double?
    var uid: String?
    var username: String?
    var range: String?
    var position: Int?
    var rankingId: Int?
    
    var coupon: Coupon?
    
    //custom comparator
    var refundOrderValue: Int {
        get {
            if(type == AwardType.SHOP || type == AwardType.TOWN_HALL){
                if(coupon?.redemptionDate == nil) {
                    return 1
                } else {
                    return 0
                }
                
            }else{
                return 0
            }
        }
    }
    
    
    public enum AwardType: Int, Codable {
        case EURO = 0
        case POINTS = 1
        case TOWN_HALL = 2
        case SHOP = 3
    }
    
    enum CodingKeys: String, CodingKey{
        case description = "description"
        case imageUrl = "imageUrl"
        case name = "name"
        case organizationId = "organizationId"
        case timestamp = "timestamp"
        case type = "type"
        case value = "value"
        case achievementId = "achievementId"
        case address = "address"
        case city = "city"
        case startDate = "startDate"
        case endDate = "endDate"
        case latitude = "latitude"
        case longitude = "longitude"
        case number = "number"
        case radius = "radius"
        case uid = "uid"
        case username = "username"
        case range = "range"
        case position = "position"
        case rankingId = "rankingId"
        case coupon = "coupon"
    }
    
    
    
    public init(description: String? = nil, imageUrl: String? = nil, name: String? = nil, organizationId: Int? = nil, timestamp: Int? = nil, type: AwardType? = nil, value: Double?, achievementId: Int? = nil, address: String? = nil, city: Int? = nil, startDate: Int? = nil, endDate: Int? = nil, latitude: Double? = nil, longitude: Double? = nil, number: Int? = nil, radius: Double? = nil, uid: String? = nil, username: String? = nil, range: String? = nil, position: Int? = nil, rankingId: Int? = nil, coupon: Coupon? = nil) {
        
        self.description = description
        self.imageUrl = imageUrl
        self.name = name
        self.organizationId = organizationId
        self.timestamp = timestamp
        self.type = type
        self.value = value
        self.achievementId = achievementId
        self.address = address
        self.city = city
        self.startDate = startDate
        self.endDate = endDate
        self.latitude = latitude
        self.longitude = longitude
        self.number = number
        self.radius = radius
        self.uid = uid
        self.username = username
        self.range = range
        self.position = position
        self.rankingId = rankingId
        self.coupon = coupon
    }
    
    
    private func getAwardTypeString(type: AwardType) -> String{
        switch type {
        case .EURO:
            return "Euro"
        case .POINTS:
            return "Punti"
        case .TOWN_HALL:
            return "Punti"
        case .SHOP:
            return "Punti"
        }
    }
    
    public func asAwardItemUI() -> AwardItemUI{
        return AwardItemUI(
            image: self.imageUrl,
            name: self.name,
            rightIcon: self.getRightIconImage(),
            rightText: self.getRightText() ?? "",
            rightElementsColor: self.getRightColor(),
            header: "Nessuna Categoria",
            value: String(self.value ?? 0.0),
            valueType: getAwardTypeString(type: self.type!))
    }
    
    
    private func getRightIconImage() -> String?{
        
        if(self.type != AwardType.EURO && self.type != AwardType.SHOP && self.type != AwardType.TOWN_HALL){
            return nil
        }
        if(self.type == AwardType.EURO){
            return getRefundImage()
        }else {
            return getRedeemImage()
        }
        
    }
    
    private func getRefundImage() -> String{
        if(coupon?.refundDate != nil) {
            return "ticket_check"
        }else{
            return "ticket_base"
        }
    }
    
    private func getRedeemImage() -> String{
        if(coupon?.redemptionDate != nil) {
            return "ticket_check"
        }else{
            return "ticket_base"
        }
    }
    
    private func getRightText() -> String?{
        switch self.type {
        case .EURO:
            return getEuroRightString()
        case .SHOP, .TOWN_HALL:
            return getRedeemRightString()
        default:
            return nil
        }
    }
    
    private func getEuroRightString() -> String?{
        if(coupon?.refundDate != nil){
            return "RIMBORSATO"
        }else{
            return "DA\nRIMBORSARE"
        }
    }


    private func getRedeemRightString() -> String?{
        if(coupon?.redemptionDate != nil){
            return "RISCATTATO"
        }else{
            return "DA\n RISCATTARE"
        }
    }
    
    func getRightColor() -> UIColor{
    
        switch self.type {
        case .EURO:
            return getRefundColor()
        case .SHOP, .TOWN_HALL:
            return getRedeemColor()
        default:
            return .systemGray
        }
    }
    
    
    private func getRefundColor() -> UIColor{
        if(coupon?.refundDate != nil){
            return .systemGray
        }else{
            return .systemRed
        }
    }

    private func getRedeemColor() -> UIColor{
        if(coupon?.redemptionDate != nil){
            return .systemGray
        }else{
            return .systemGreen
        }
    }
    
    
}

public struct AwardItemUI {
    var image: String?
    var name: String?
    var rightIcon: String?
    var rightText: String
    var rightElementsColor: UIColor
    var header: String?
    var value: String
    var valueType: String?
}


public class Coupon: Codable {
    
    var code: String?
    var emissionDate: Int?
    var expireDate: Int?
    var organizationId: Int?
    var redemptionDate: Int?
    var refundDate: Int?
    var title: String?
    var uid: String?
    var value: Double?
    var shopId: Int?
    var shopName: String?
    var shopLogo: String?
    var articleImage: String?
    var articleTitle: String?
    var isRedeemed: Bool {
        get {
            return self.redemptionDate != nil
        }
    }
    var isRefunded: Bool {
        get {
            return self.refundDate != nil
        }
    }
    
    enum CodingKeys: String, CodingKey{
        case code = "code"
        case emissionDate = "emissionDate"
        case expireDate = "expireDate"
        case organizationId = "organizationId"
        case redemptionDate = "redemptionDate"
        case refundDate = "refundDate"
        case title = "title"
        case uid = "uid"
        case value = "value"
        case shopId = "shopId"
        case shopName = "shopName"
        case shopLogo = "shopLogo"
        case articleImage = "articleImage"
        case articleTitle = "articleTitle"
    }
    
    public init(code: String? = nil, emissionDate: Int? = nil, expireDate: Int? = nil, organizationId: Int? = nil, redemptionDate: Int? = nil, refundDate: Int? = nil, title: String? = nil, uid: String? = nil, value: Double? = nil, shopName: String? = nil, shopLogo: String? = nil, articleImage: String? = nil, articleTitle: String? = nil) {
        self.code = code
        self.emissionDate = emissionDate
        self.expireDate = expireDate
        self.organizationId = organizationId
        self.redemptionDate = redemptionDate
        self.refundDate = refundDate
        self.title = title
        self.uid = uid
        self.value = value
        self.shopName = shopName
        self.shopLogo = shopLogo
        self.articleImage = articleImage
        self.articleTitle = articleTitle
    }
}
