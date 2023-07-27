//
//  OrganizationSeat.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 10/07/21.
//

import Foundation

// MARK: - OrganizationSeat
public class OrganizationSeat: Codable {
    var id: Int?
    var address: String?
    var city: Int?
    var cityName: String?
    var latitude, longitude: Double?
    var name, number: String?
    var organization: Int?
    var validated: Bool?
    
    enum CodingKeys: String, CodingKey{
        case address = "address"
        case city = "city"
        case cityName = "cityName"
        case id = "id"
        case latitude = "latitude"
        case longitude = "longitude"
        case name = "name"
        case number = "number"
        case organization = "organization"
        case validated = "validated"
    }
    

    public init(address: String?, city: Int?, cityName: String?, id: Int?, latitude: Double?, longitude: Double?, name: String?, number: String?, organization: Int?, validated: Bool?) {
        self.address = address
        self.city = city
        self.cityName = cityName
        self.id = id
        self.latitude = latitude
        self.longitude = longitude
        self.name = name
        self.number = number
        self.organization = organization
        self.validated = validated
    }
}
