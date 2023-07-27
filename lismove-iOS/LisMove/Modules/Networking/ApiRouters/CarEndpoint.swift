//
//  CarEndpoit.swift
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

enum CarEndpoint: URLRequestConvertible {
    
    
    // MARK: GET
    case getCarBrands(baseUrl: String)
    case getCarModels(baseUrl: String, bid: Int)
    case getCarGenerations(baseUrl: String, bid:Int, mid: Int)
    case getCarModifications(baseUrl: String, bid:Int, mid: Int, gid: Int)


    
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .getCarBrands:
            return .get
        case .getCarModels:
            return .get
        case .getCarGenerations:
            return .get
        case .getCarModifications:
            return .get
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        
        case .getCarBrands(let baseUrl):
            return baseUrl + "/carbrands"
    
        case .getCarModels(let baseUrl, let bid):
            return baseUrl + "/carbrands/\(bid)/models"
            
        case .getCarGenerations(let baseUrl, let bid, let mid):
            return baseUrl + "/carbrands/\(bid)/models/\(mid)/generations"
            
        case .getCarModifications(let baseUrl, let bid, let mid, let gid):
            return baseUrl + "/carbrands/\(bid)/models/\(mid)/generations/\(gid)/modifications"

            
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        
        case .getCarBrands:
            return nil
        case .getCarModels:
            return nil
        case .getCarGenerations:
            return nil
        case .getCarModifications:
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

