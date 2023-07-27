//
//  ApiRouter.swift
//  NetworkingClients
//
//  Created by Francesco Paolo Dellaquila on 30/03/2020.
//  Copyright © 2020 Nextome. All rights reserved.
//

import Foundation
import Alamofire
import FirebaseAuth
import Resolver

/*
 Api router redirect url api call, changing the path and the request body, based on endpoint choice
 */

enum SessionEndpoint: URLRequestConvertible {
    
    
    // MARK: GET
    case sessionGet(baseUrl: String, uuid: String)
    
    //MARK: POST
    case sessionCreate(baseUrl: String, session: Session)
    case sessionCreateMultiple(baseUrl: String, sessions: [Session])
    case sendSessionHistory(baseUrl: String, sessions: [OfflineDataRequest])
   
    // MARK: PUT
    case requestSessionVerification(baseUrl: String, uuid: String, request: SessionValidationRequest)
    case getRevisionType(baseUrl: String)
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .sessionGet:
            return .get
        case .sessionCreate:
            return .post
        case .sessionCreateMultiple:
            return .post
        case .requestSessionVerification:
            return .put
        case .getRevisionType:
            return .get     
        case .sendSessionHistory:
            return .post
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        
        case .sessionGet(let baseUrl, let uuid):
            return baseUrl + "/sessions/\(uuid)"
            
        case .sessionCreate(let baseUrl, _):
            return baseUrl + "/sessions"
            
        case .sessionCreateMultiple(let baseUrl, _):
            return baseUrl + "/sessions"
        case .requestSessionVerification(let baseUrl, let uuid, _):
            return baseUrl + "/sessions/\(uuid)"
        case .getRevisionType(let baseUrl):
            return baseUrl + "/enums/revision-type"
        case .sendSessionHistory(let baseUrl, _):
            return baseUrl + "/sessions/offline"
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        
        case .sessionGet, .getRevisionType:
            return nil
            
        case .sessionCreate(_, let session):
            //convert struct to json
            let json = try! JSONEncoder().encode(session)
            return json
            
        case .sessionCreateMultiple(_, let sessions):
            //convert struct to json
            let json = try! JSONEncoder().encode(sessions)
            return json
        case .requestSessionVerification(_, _, request: let request):
            let json = try! JSONEncoder().encode(request)
            return json
        case .sendSessionHistory(_, let sessions):
            let json = try! JSONEncoder().encode(sessions)
            return json
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
