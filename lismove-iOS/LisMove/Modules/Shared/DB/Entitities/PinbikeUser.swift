//
//  User.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/05/21.
//

import Foundation
import Realm
import RealmSwift

// MARK: - User
public class LismoveUser: Object, Codable {
    
    //phone info
    @objc public dynamic var activePhone,activePhoneModel,activePhoneToken, activePhoneVersion: String?
    
    @objc public dynamic var avatarURL: String?
    public dynamic var birthDate = RealmOptional<Int64>()
    @objc public dynamic var cityLisMove, email: String?
    @objc public dynamic var emailVerified = false
    @objc public dynamic var firstName, gender, homeAddress: String?
    public dynamic var homeCity = RealmOptional<Int>()
    @objc public dynamic var homeNumber: String?
    
    public dynamic var homeLatitude = RealmOptional<Double>()
    public dynamic var homeLongitude = RealmOptional<Double>()
    
    @objc public dynamic var iban: String?

    public dynamic var lastLoggedIn = RealmOptional<Int64>()
    
    @objc public dynamic var lastName: String?
    @objc public dynamic var marketingTermsAccepted = false
    @objc public dynamic var password: String?
    public dynamic var phoneActivationTime = RealmOptional<Int>()
    @objc public dynamic var phoneNumber: String?
    @objc public dynamic var phoneNumberPrefix: String?

    @objc public dynamic var signupCompleted = false
    @objc public dynamic var termsAccepted = false
    @objc public dynamic var uid, username: String?
    public dynamic var userType: UserType?
    let  workAddresses = List<WorkAddress>()
    
    var fullName: String{
        get{
            let name =  firstName ?? ""
            let lastName = lastName ?? ""
            return name + " " + lastName
        }
    }
    public enum UserType: Int, Codable {
        case LismoveUser = 0
        case Manager = 1
        case Vendor = 2
        case Admin
    }
    
    
    
    enum CodingKeys: String, CodingKey {
        case uid = "uid"
        case userType = "userType"
        case username = "username"
        case avatarURL = "avatarUrl"
        
        case activePhone = "activePhone"
        case activePhoneModel = "activePhoneModel"
        case activePhoneToken = "activePhoneToken"
        case activePhoneVersion = "activePhoneVersion"
        
        case birthDate = "birthDate"
        case cityLisMove = "cityLisMove"
        case email = "email"
        case emailVerified = "emailVerified"
        case firstName = "firstName"
        case gender = "gender"
        case lastLoggedIn = "lastLoggedIn"
        case lastName = "lastName"
        case marketingTermsAccepted = "marketingTermsAccepted"
        case signupCompleted = "signupCompleted"
        case termsAccepted = "termsAccepted"

        //
        case homeAddress = "homeAddress"
        case homeCity = "homeCity"
        case homeNumber = "homeNumber"
        case homeLatitude = "homeLatitude"
        case homeLongitude = "homeLongitude"
        case iban = "iban"
        case password = "password"
        case phoneActivationTime = "phoneActivationTime"
        case phoneNumber = "phoneNumber"
        case phoneNumberPrefix = "phoneNumberPrefix"
        case workAddresses = "workAddresses"
        
    }
    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.uid = try container.decodeIfPresent(String.self, forKey: .uid)
        self.userType = try container.decodeIfPresent(UserType.self, forKey: .userType)
        self.firstName = try container.decodeIfPresent(String.self, forKey: .firstName)
        self.lastName = try container.decodeIfPresent(String.self, forKey: .lastName)
        self.username =  try container.decodeIfPresent(String.self, forKey: .username)
        self.email =  try container.decodeIfPresent(String.self, forKey: .email)
        self.birthDate.value =  try container.decodeIfPresent(Int64.self, forKey: .birthDate)
        self.termsAccepted = try container.decodeIfPresent(Bool.self, forKey: .termsAccepted) ?? false
        self.marketingTermsAccepted = try container.decodeIfPresent(Bool.self, forKey: .marketingTermsAccepted) ?? false
        self.gender = try container.decodeIfPresent(String.self, forKey: .gender)
        self.avatarURL = try container.decodeIfPresent(String.self, forKey: .avatarURL)
        self.emailVerified = try container.decodeIfPresent(Bool.self, forKey: .emailVerified) ?? false
        self.signupCompleted = try container.decodeIfPresent(Bool.self, forKey: .signupCompleted) ?? false
        self.cityLisMove = try container.decodeIfPresent(String.self, forKey: .cityLisMove)

        self.activePhone = try container.decodeIfPresent(String.self, forKey: .activePhone)
        self.activePhoneModel = try container.decodeIfPresent(String.self, forKey: .activePhoneModel)
        self.activePhoneToken = try container.decodeIfPresent(String.self, forKey: .activePhoneToken)
        self.activePhoneVersion = try container.decodeIfPresent(String.self, forKey: .activePhoneVersion)
        
        self.lastLoggedIn.value = try container.decodeIfPresent(Int64.self, forKey: .lastLoggedIn)
        
        self.homeAddress = try container.decodeIfPresent(String.self, forKey: .homeAddress)
        self.homeCity.value = try container.decodeIfPresent(Int.self, forKey: .homeCity)
        self.homeNumber = try container.decodeIfPresent(String.self, forKey: .homeNumber)
        self.homeLatitude.value = try container.decodeIfPresent(Double.self, forKey: .homeLatitude)
        self.homeLongitude.value = try container.decodeIfPresent(Double.self, forKey: .homeLongitude)
        
        self.iban = try container.decodeIfPresent(String.self, forKey: .iban)
        
        self.password = try container.decodeIfPresent(String.self, forKey: .password)
        self.phoneActivationTime.value = try container.decodeIfPresent(Int.self, forKey: .phoneActivationTime)
        self.phoneNumber = try container.decodeIfPresent(String.self, forKey: .phoneNumber)
        self.phoneNumberPrefix = try container.decodeIfPresent(String.self, forKey: .phoneNumberPrefix)

        
        let workList = try container.decodeIfPresent([WorkAddress].self, forKey: .workAddresses) ?? []
        self.workAddresses.append(objectsIn: workList)
        
        super.init()
        
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(self.uid, forKey: .uid)
        try container.encodeIfPresent(self.userType, forKey: .userType)
        try container.encodeIfPresent(self.firstName, forKey: .firstName)
        try container.encodeIfPresent(self.lastName, forKey: .lastName)
        try container.encodeIfPresent(self.username, forKey: .username)
        try container.encodeIfPresent(self.email, forKey: .email)
        try container.encodeIfPresent(self.birthDate.value, forKey: .birthDate)
        try container.encodeIfPresent(self.termsAccepted, forKey: .termsAccepted)
        try container.encodeIfPresent(self.marketingTermsAccepted, forKey: .marketingTermsAccepted)
        try container.encodeIfPresent(self.gender, forKey: .gender)
        try container.encodeIfPresent(self.signupCompleted, forKey: .signupCompleted)

        try container.encodeIfPresent(self.avatarURL, forKey: .avatarURL)
        try container.encodeIfPresent(self.emailVerified, forKey: .emailVerified)
        try container.encodeIfPresent(self.cityLisMove, forKey: .cityLisMove)
        
        try container.encodeIfPresent(self.activePhone, forKey: .activePhone)
        try container.encodeIfPresent(self.activePhoneModel, forKey: .activePhoneModel)
        try container.encodeIfPresent(self.activePhoneToken, forKey: .activePhoneToken)
        try container.encodeIfPresent(self.activePhoneVersion, forKey: .activePhoneVersion)
        
        try container.encodeIfPresent(self.lastLoggedIn.value, forKey: .lastLoggedIn)
        
        try container.encodeIfPresent(self.homeAddress, forKey: .homeAddress)
        try container.encodeIfPresent(self.homeCity.value, forKey: .homeCity)
        
        try container.encodeIfPresent(self.homeNumber, forKey: .homeNumber)
        try container.encodeIfPresent(self.homeLatitude, forKey: .homeLatitude)
        try container.encodeIfPresent(self.homeLongitude, forKey: .homeLongitude)
        
        try container.encodeIfPresent(self.iban, forKey: .iban)
        
        try container.encodeIfPresent(self.password, forKey: .password)
        try container.encodeIfPresent(self.phoneActivationTime.value, forKey: .phoneActivationTime)
        try container.encodeIfPresent(self.phoneNumber, forKey: .phoneNumber)
        try container.encodeIfPresent(self.phoneNumberPrefix, forKey: .phoneNumberPrefix)

        try container.encodeIfPresent(self.workAddresses, forKey: .workAddresses)
        
        
    }

    public required override init() {
        super.init()
    }
    
    
    public override static func primaryKey() -> String? {
        return "uid"
    }

    public func getFullName() -> String{
        let name = self.firstName ?? ""
        let surname = self.lastName ?? ""
        return "\(name) \(surname)"
    }
    
    public func getAddressLabel()->String{
        let homeNumberLabel = homeNumber != nil && homeNumber != "" ? ", \(homeNumber!)" : ""
        let cityName  = CityRepository.getCityName(byCode: homeCity.value) ?? ""
        let cityLabel = (cityName != "") ? ", \(cityName)" : ""
        return "\(homeAddress ?? "")\(homeNumberLabel)\(cityLabel)"
    }
    
    public func fixPhoneNumber(){
        phoneNumber = phoneNumber?.replacingOccurrences(of: " ", with: "")
        let phoneSize = phoneNumber?.count ?? 0
        if let phoneNumberNotNull = phoneNumber {
            if(phoneNumberNotNull.contains("+")){
                if(phoneSize>=10){
                        phoneNumber = String(phoneNumberNotNull.suffix(10))
                }else{
                    phoneNumber = ""
                }
            }
                
            }
        
        if(phoneNumberPrefix == nil || phoneNumberPrefix == ""){
            phoneNumberPrefix = "39"
        }
    }
    
    func isAddressComplete() -> Bool{
        return !(homeAddress == nil || homeAddress == "" || homeCity.value == nil)
    }

}

public class WorkAddress: Object, Codable{
    @objc public dynamic var uid = UUID().uuidString
    @objc public dynamic var address: String?
    public dynamic var city = RealmOptional<Int>()
    public dynamic var id = RealmOptional<Int>()
    public dynamic var lat = RealmOptional<Double>()
    public dynamic var lng = RealmOptional<Double>()
    @objc public dynamic var name, number: String?
    public dynamic var organization = RealmOptional<Int>()
    public dynamic var validated = RealmOptional<Bool>()
    
    enum CodingKeys: String, CodingKey {
        case address = "address"
        case city = "city"
        case id = "id"
        case latitude = "latitude"
        case longitude = "longitude"
        case name = "name"
        case number = "number"
        case organization = "organization"
        case validated = "validated"
        
    }
    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.address = try container.decodeIfPresent(String.self, forKey: .address)
        self.city.value = try container.decodeIfPresent(Int.self, forKey: .city)
        self.id.value = try container.decodeIfPresent(Int.self, forKey: .id)
        self.lat.value = try container.decodeIfPresent(Double.self, forKey: .latitude)
        self.lng.value =  try container.decodeIfPresent(Double.self, forKey: .longitude)
        self.name =  try container.decodeIfPresent(String.self, forKey: .name)
        self.number =  try container.decodeIfPresent(String.self, forKey: .number)
        self.organization.value = try container.decodeIfPresent(Int.self, forKey: .organization)
        self.validated.value = try container.decodeIfPresent(Bool.self, forKey: .validated)
        
        
        
        super.init()
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(self.address, forKey: .address)
        try container.encodeIfPresent(self.city, forKey: .city)
        try container.encodeIfPresent(self.id, forKey: .id)
        try container.encodeIfPresent(self.lat, forKey: .latitude)
        try container.encodeIfPresent(self.lng, forKey: .longitude)
        try container.encodeIfPresent(self.name, forKey: .name)
        try container.encodeIfPresent(self.number, forKey: .number)
        try container.encodeIfPresent(self.organization, forKey: .organization)
        try container.encodeIfPresent(self.validated, forKey: .validated)
        
        
    }

    public required override init() {
        super.init()
    }
    
    
    public override static func primaryKey() -> String? {
        return "uid"
    }
    
    func getAddress(cityExtended: [String? : String?]) -> String{
        
        if (address != nil && address != ""){
            var addressString = address ?? ""
            if(number != nil){
                addressString += ", \(number!)"
            }
            if(!cityExtended.isEmpty){
                let cityValue = cityExtended.first?.key ?? ""
                let prov = cityExtended.first?.value ?? ""
                addressString += ", \(cityValue), \(prov)"
            }
            return addressString
        }else{
            return ""
        }
    }
    
    func isComplete()-> Bool{
        return address != nil && address != "" && city != nil
    }
}
