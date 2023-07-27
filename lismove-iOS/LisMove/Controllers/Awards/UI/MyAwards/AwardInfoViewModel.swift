//
//  AwardInfoViewModel.swift
//  LisMove
//
//

import Foundation
import UIKit
import Resolver

class AwardInfoViewModel{
    var delegate: AwardInfoDelegate? = nil
    var myAward: Award? = nil
    var rankingAward: AwardRanking? = nil
    var achievementAward: AwardAchievement? = nil
    
    var organization: Organization? = nil
    let TAG = "AwardInfoViewModel"
    @Injected
    var initiativeRepository: InitiativeRepository
    
    func initAward(myAward: Award?, rankingAward: AwardRanking?, achievementAward: AwardAchievement?){
        self.myAward = myAward
        self.rankingAward = rankingAward
        self.achievementAward = achievementAward
    }
    
    func loadData(){
        if let myAward = myAward {
            loadData(fromMyAward: myAward)
        }else if let rankingAward = rankingAward {
            loadData(fromRankingAward: rankingAward)
        }else if let achievementAward = achievementAward {
            loadData(fromAchievementAward: achievementAward)
        }else{
            delegate?.onError(message: "Si è verificato un errore nel recuperare il premio")
        }
    }
    
    func loadData(fromMyAward award: Award){

        delegate?.onLoading()
        if let organizationId = award.organizationId{
            
            initiativeRepository.getOrganization(oid: organizationId, onCompleted: {result in
                switch result{
                case .success(let organization):
                    self.organization = organization
                    LogHelper.log(message: "organization received", withTag: self.TAG)
                case .failure(let error):
                    LogHelper.logError(message: "Error fetching the organization \(error.localizedDescription)", withTag: self.TAG)
                }
                if let awardDetail = award.asAwardDetailUI(withOrganization: self.organization){
                    self.delegate?.onDataLoaded(detail: awardDetail)
                }else{
                    self.delegate?.onError(message: "Si è verificato un errore, riprova più tardi")
                }
            })
        }
      
    }
    
    func loadData(fromRankingAward award: AwardRanking){
        let value = award.value != nil ? "\(award.value!)" : nil
        let detail = AwardDetailUI(imageUrl: award.imageURL, title: award.name ?? "", description: award.description ?? "", valueLabel: award.getTypeLabel() , value: value)
        delegate?.onDataLoaded(detail: detail)
    }
    
    func loadData(fromAchievementAward award: AwardAchievement){
        let value = award.value != nil ? "\(award.value!)" : nil
        let detail = AwardDetailUI(imageUrl: award.imageURL, title: award.name ?? "", description: award.description ?? "", valueLabel: award.getTypeLabel(), value: value)
        delegate?.onDataLoaded(detail: detail)
    }
    
    
    
    
    
    
    
    
}


protocol AwardInfoDelegate{
    func onLoading()
    func onDataLoaded(detail: AwardDetailUI)
    func onError(message: String)
}

struct AwardDetailUI{
    
    
 
    let imageUrl: String?
    let title: String
    let description: String
    let valueLabel: String?
    let value: String?
    let header: String?
    let emissionDate: String?

    let hasCoupon: Bool

    let qrCode: String?
    let state: String?
    let stateColor: UIColor?
    let refundType: String?
    let refundLabel: String?
    let refundDate: String?
    let expiringDate: String?
    let shopLabel: String?
    let shopName: String?
    let showShopImage: Bool
    let shopImage: String?
    let articleName: String?
    let articleImage: String?

    internal init(imageUrl: String?, title: String, description: String, valueLabel: String? = nil, value: String?  = nil, header: String?  = nil, emissionDate: String?  = nil , hasCoupon: Bool = false, qrCode: String? = nil, state: String?  = nil, stateColor: UIColor?  = nil, refundType: String?  = nil, refundLabel: String?  = nil, refundDate: String?  = nil, expiringDate: String?  = nil, shopLabel: String?  = nil, shopName: String?  = nil, showShopImage: Bool = true, shopImage: String? = nil, articleName: String?  = nil, articleImage: String?  = nil ) {
        self.imageUrl = imageUrl
        self.title = title
        self.description = description
        self.valueLabel = valueLabel
        self.value = value
        self.header = header
        self.emissionDate = emissionDate
        self.hasCoupon = hasCoupon
        self.qrCode = qrCode
        self.state = state
        self.stateColor = stateColor
        self.refundType = refundType
        self.refundLabel = refundLabel
        self.refundDate = refundDate
        self.expiringDate = expiringDate
        self.shopLabel = shopLabel
        self.shopName = shopName
        self.showShopImage = showShopImage
        self.shopImage = shopImage
        self.articleName = articleName
        self.articleImage = articleImage
    }
   
}


extension Award{
    func asAwardDetailUI(withOrganization organization: Organization?) -> AwardDetailUI?{
        guard let awardType = type else{
            return nil
        }
        switch(awardType){
        case .EURO: return asEuroAward()
        case .POINTS: return asPointsAward()
        case .SHOP: return asShopAward(organization: organization)
        case .TOWN_HALL: return asTownHallAward()
        }
    }
    
    private func asEuroAward() -> AwardDetailUI{
    
        return AwardDetailUI(
            imageUrl: imageUrl,
            title: name ?? "",
            description: description ?? "",
            valueLabel: "Euro",
            value: String(value ?? 0.0),
            header: nil,
            emissionDate: timestamp != nil ? DateTimeUtils.getReadableCompactDate(date: Double(timestamp!)) : nil,
            hasCoupon: true,
            qrCode: nil,
            state: coupon?.refundDate != nil ? "RIMBORSATO" : "DA RIMBORSARE",
            stateColor: getRightColor(),
            refundType: "Bonifico in conto corrente",
            refundLabel: coupon?.refundDate != nil ? "Data emissione bonifico" : nil,
            refundDate: coupon?.refundDate != nil ?  DateTimeUtils.getReadableCompactDate(date: Double(coupon!.refundDate!)): nil ,
            expiringDate: nil,
            shopLabel: nil,
            shopName: nil,
            showShopImage: false,
            shopImage: nil,
            articleName: nil,
            articleImage: nil)
       
    }
    
    private func asPointsAward() -> AwardDetailUI{
        return AwardDetailUI(
            imageUrl: imageUrl,
            title: name ?? "",
            description: description ?? "",
            valueLabel: "Punti",
            value: String(value ?? 0.0),
            header: nil,
            emissionDate: timestamp != nil ? DateTimeUtils.getReadableCompactDate(date: Double(timestamp!)) : nil,
            hasCoupon: false,
            qrCode: nil,
            state: nil,
            stateColor: nil,
            refundType: "Bonifico in conto corrente",
            refundLabel: nil,
            refundDate:  nil ,
            expiringDate: nil,
            shopLabel: nil,
            shopName: nil,
            showShopImage: false,
            shopImage: nil,
            articleName: nil,
            articleImage: nil)
       
    }
    
    
    private func asTownHallAward() -> AwardDetailUI{
        return AwardDetailUI(
            imageUrl: imageUrl,
            title: name ?? "",
            description: description ?? "",
            valueLabel: value != nil ? "Euro" : nil,
            value: value != nil ? String(value ?? 0.0) : nil,
            header: nil,
            emissionDate: timestamp != nil ? DateTimeUtils.getReadableCompactDate(date: Double(timestamp!)) : nil,
            hasCoupon: true,
            qrCode: coupon?.code,
            state: coupon?.redemptionDate != nil ? "RISCATTATO" : "DA RISCATTARE",
            stateColor: getRightColor(),
            refundType: "Riscattabili in comune",
            refundLabel: coupon?.redemptionDate != nil ? "Data riscossione" : nil,
            refundDate: coupon?.redemptionDate != nil ?  DateTimeUtils.getReadableCompactDate(date: Double(coupon!.redemptionDate!)): nil ,
            expiringDate: coupon?.expireDate != nil ?  DateTimeUtils.getReadableCompactDate(date: Double(coupon!.expireDate!)): nil,
            shopLabel: nil,
            shopName: nil,
            showShopImage: false,
            shopImage: nil,
            articleName: nil,
            articleImage: nil)
    }
    
    private func asShopAward(organization: Organization?) -> AwardDetailUI{
        var shopName = coupon?.shopName
        var shopLogo = coupon?.shopLogo
        let hasInitiativeShopName = coupon?.shopName == nil && coupon?.redemptionDate == nil && organization != nil
        if(hasInitiativeShopName){
            shopName = organization?.title
            shopLogo = organization?.initiativeLogo
        }
        
        var shopLabel = "Riscattato presso: "
        if(coupon?.redemptionDate == nil){
            if(hasInitiativeShopName){
                shopLabel = "Riscattabile presso i negozi aderenti all'iniziativa: "
            }else{
                shopLabel = "Riscattabile presso: "
            }
        }
        
        return AwardDetailUI(
            imageUrl: imageUrl,
            title: name ?? "",
            description: description ?? "",
            valueLabel: "Punti",
            value: String(value ?? 0.0),
            header: nil,
            emissionDate: timestamp != nil ? DateTimeUtils.getReadableCompactDate(date: Double(timestamp!)) : nil,
            hasCoupon: true,
            qrCode: coupon?.code,
            state: coupon?.redemptionDate != nil ? "RISCATTATO" : "DA RISCATTARE",
            stateColor: getRightColor(),
            refundType: "Riscattabile nei negozi aderenti",
            refundLabel: coupon?.redemptionDate != nil ? "Data riscossione" : nil,
            refundDate: coupon?.redemptionDate != nil ?  DateTimeUtils.getReadableCompactDate(date: Double(coupon!.redemptionDate!)): nil ,
            expiringDate: coupon?.expireDate != nil ?  DateTimeUtils.getReadableCompactDate(date: Double(coupon!.expireDate!)): nil,
            shopLabel: shopLabel,
            shopName: shopName,
            showShopImage: false,
            shopImage: shopLogo,
            articleName: coupon?.articleTitle,
            articleImage: coupon?.articleImage)

    }
    
}
