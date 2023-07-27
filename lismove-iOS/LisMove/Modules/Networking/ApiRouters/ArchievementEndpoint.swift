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

enum ArchievementEndpoint: URLRequestConvertible {
    
    
    // MARK: GET
    case getAwards(baseUrl: String, aid:Int)


    
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .getAwards:
            return .get
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        
        case .getAwards(let baseUrl, let aid):
            return baseUrl + "/achievements/\(aid)/awards"
   
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        
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

