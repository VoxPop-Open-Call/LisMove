//
//  RankingEndpoit.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import Foundation
import Alamofire
import FirebaseAuth
import Resolver

/*
 Api router redirect url api call, changing the path and the request body, based on endpoint choice
 */

enum RankingEndpoint: URLRequestConvertible {
    
    
    // MARK: GET
    case getGlobalRanking(baseUrl: String)
    case getRanking(baseUrl: String, id:Int)
    case getAllRanking(baseUrl: String, active: Bool, national: Bool, withPositions: Bool)
    case getAwards(baseUrl: String, rid:Int)


    
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .getGlobalRanking:
            return .get
        case .getRanking:
            return .get
        case .getAllRanking:
            return .get
        case .getAwards:
            return .get
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        
        case .getRanking(let baseUrl, let id):
            return baseUrl + "/rankings/\(id)?withUsers=true"
    
        case .getGlobalRanking(let baseUrl):
            return baseUrl + "/rankings/global"
            
        case .getAllRanking(let baseUrl, let active, let national, let withPositions):
            return baseUrl + "/rankings?active=\(active)&national=\(national)&withPositions=\(withPositions)"
            
        case .getAwards(let baseUrl, let rif):
            return baseUrl + "/rankings/\(rif)/awards"

            
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        
        case .getRanking:
            return nil
        case .getGlobalRanking:
            return nil
        case .getAllRanking:
            return nil
        case .getAwards:
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
        var token = authRepository.getUserToken()

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

