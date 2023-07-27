//
//  OrganizationEndpoint.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 09/07/21.
//

import Foundation
import Alamofire
import FirebaseAuth
import Resolver
/*
 Api router redirect url api call, changing the path and the request body, based on endpoint choice
 */

enum OrganizationEndpoint: URLRequestConvertible {
    
    
    // MARK: GET
    case organizationDataGet(baseUrl: String, oid: String)
    case organizationSeat(baseUrl: String, oid: String)
    case organizationCustomFields(baseUrl: String, oid: Int)
    case organizationSettings(baseUrl: String, oid: Int)
    case organizationCustomFieldsValue(baseUrl: String, oid: String, eid: Int)
    
    //MARK: POST
    case createNewSeat(baseUrl: String, oid: Int, seat: OrganizationSeat)
    case updateCustomFieldsValue(baseUrl: String, oid: Int, field: CustomFieldValues)
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .updateCustomFieldsValue, .createNewSeat:
            return .post
        case .organizationDataGet, .organizationSeat, .organizationSettings, .organizationCustomFields, .organizationCustomFieldsValue:
            return .get
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        
        case .organizationDataGet(let baseUrl, let oid):
            return baseUrl + "/organizations/\(oid)"
            
        case .organizationSeat(let baseUrl, let oid):
            return baseUrl + "/organizations/\(oid)/seats"
            
        case .organizationSettings(let baseUrl, let oid):
            return baseUrl + "/organizations/\(oid)/settings"
            
        case .organizationCustomFields(let baseUrl, let oid):
            return baseUrl + "/organizations/\(oid)/custom-fields"
            
        case .organizationCustomFieldsValue(let baseUrl, let oid, let eid):
            return baseUrl + "/organizations/\(oid)/custom-field-values?eid=\(eid)"
            
        case .updateCustomFieldsValue(let baseUrl, let oid, let field):
            return baseUrl + "/organizations/\(oid)/custom-field-values"
            
        case .createNewSeat(let baseUrl, let oid, let seat):
            return baseUrl + "/organizations/\(oid)/seats"
            
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        
        case .updateCustomFieldsValue(_, _, let field):
            //convert struct to json
            let json = try! JSONEncoder().encode(field)
            return json
            
        case .createNewSeat(_, _, let seat):
            //convert struct to json
            let json = try! JSONEncoder().encode(seat)
            return json
            
            
        case .organizationDataGet, .organizationSeat, .organizationSettings, .organizationCustomFields, .organizationCustomFieldsValue:
            return nil
        
        }
    }
    
    
    // MARK: - URLRequestConvertible
    func asURLRequest() throws -> URLRequest {
        
        var urlRequest = URLRequest(url: try path.asURL())
        
        // HTTP Method
        urlRequest.httpMethod = method.rawValue
        
        //Common Headers
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")

        @Injected var authRepository: AuthRepository

        //Set the Authorization header: auth
        let token = authRepository.getUserToken()

        if(token != ""){
            urlRequest.setValue("Bearer " + (token), forHTTPHeaderField: "Authorization")
        }
        
        //Set the Authorization header: app version
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
        
        
        //Set the Authorization header: app os
        urlRequest.setValue("iOS", forHTTPHeaderField: "app-os")
        //Set the Authorization header: app version
        urlRequest.setValue(appVersion, forHTTPHeaderField: "app-version")
 
        // Parameters
        if let parameters = parameters {
            urlRequest.httpBody = parameters
        }
        
        return urlRequest
    }
    
    
}
