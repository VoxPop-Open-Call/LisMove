//
//  Organization.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 09/07/21.
//

import Foundation

// MARK: - Organization
public class Organization: Codable {
    var id: Int?
    var title: String?
    var notificationLogo: String?
    var logo: String?
    var initiativeLogo: String?
    var geojson: String?
    var validation: Bool?
    var type: OrganizationType?
    var regulation: String?
    var termsConditions: String?

    
    public enum OrganizationType: Int, Codable {
        case PA = 0
        case COMPANY = 1
    }
    
    enum CodingKeys: String, CodingKey{ 
        case id = "id"
        case type = "type"
        case title = "title"
        case notificationLogo = "notificationLogo"
        case logo = "logo"
        case initiativeLogo = "initiativeLogo"
        case geojson = "geojson"
        case validation = "validation"
        case regulation = "regulation"
        case termsConditions = "termsConditions"
    }

    public init(id: Int?, type: OrganizationType?, title: String?, notificationLogo: String?, geojson: String?, validation: Bool?, logo: String?, initiativeLogo: String?, regulation: String?, termsConditions: String?) {
        self.id = id
        self.type = type
        self.title = title
        self.notificationLogo = notificationLogo
        self.geojson = geojson
        self.validation = validation
        self.logo = logo
        self.initiativeLogo = initiativeLogo
        self.regulation = regulation
        self.termsConditions = termsConditions
    }
    
    public func getCoordinates() -> [[Coordinates]]?{
        //load all city
        let decoder = JSONDecoder()
        
        guard let json = self.geojson?.data(using: .utf8) else{
            return nil
        }
        
        guard let coordinates =  try? decoder.decode([[Coordinates]].self, from: json) else {
           return nil
        }
        return coordinates
    }
    
    public func getRegulationLink() -> URL?{
        
        var url: URL? = nil
        if let regulation = regulation{
            if(!regulation.isEmpty){
                let types: NSTextCheckingResult.CheckingType = [.link]
                let detector = try? NSDataDetector(types: types.rawValue)
                let range = NSRange(regulation.startIndex..<regulation.endIndex, in: regulation)
                detector?.enumerateMatches(in: regulation, options: [], range: range){
                    (match, flags, _) in
                        guard let match = match else {
                            return
                        }

                        switch match.resultType {
                        case .link:
                            url = match.url
                            return
                        default:
                            return
                        }
                    }
            }
            
            return url
        }else{
            return nil
        }
    }
            
    
    public func asOrganization_DB() -> Organization_DB{
        return Organization_DB(id: self.id,
                               title: self.title,
                               notificationLogo: self.notificationLogo,
                               logo: self.logo,
                               initiativeLogo: self.initiativeLogo,
                               geojson: self.geojson,
                               validation: self.validation,
                               type: self.type?.rawValue,
                               regulation: self.regulation,
                               termsConditions: self.termsConditions)
    }
    
}


public class Coordinates: Codable{
    var lat: Double?
    var lng: Double?
    
    
    enum CodingKeys: String, CodingKey{
        case lat = "lat"
        case lng = "lng"
    }

    public init(lat: Double?, lng: Double?) {
        self.lat = lat
        self.lng = lng
    }
}
