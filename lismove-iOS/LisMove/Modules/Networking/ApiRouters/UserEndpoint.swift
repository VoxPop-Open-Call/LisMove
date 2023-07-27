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

enum UserEndpoint: URLRequestConvertible {
    
    // MARK: GET
    case userList(baseUrl: String)
    case userGet(baseUrl: String, uid: String)
    case userExist(baseUrl: String, email: String)
    case userSessionGet(baseUrl: String, uid: String, start: String, end: String)
    case userEnrollmentGet(baseUrl: String, uid: String)
    case consumeCode(baseUrl: String, uid: String, code: String)
    case verifyCode(baseUrl: String, uid: String, code: String)
    case stolenSensor(baseUrl: String, uid: String, uuid: String)
    case getSensor(baseUrl: String, uid: String)
    case getCar(baseUrl: String, uid: String)
    case resetPassword(baseUrl: String, email: String)
    case userRanking(baseUrl: String, uid: String)
    case getUserDashboard(baseUrl: String, uid: String)
    
    case getUserNotificationMessage(baseUrl: String, uid: String)
    case markMessageAsRead(baseUrl: String, uid: String, mid: Int)
    
    case getArchievement(baseUrl: String, uid: String)
    case getAwards(baseUrl: String, uid: String)
    
    //MARK: POST
    case userCreate(baseUrl: String, user: LismoveUser)
    case sensorSave(baseUrl: String, uid: String, sensor: Sensor)
    case carSave(baseUrl: String, uid: String, car: CarModification)
    
    //MARK: PUT
    case userUpdate(baseUrl: String, uid: String, user: LismoveUser)
    
    //MARK: DELETE
    case sensorDelete(baseUrl: String, uid: String, uuid: String)
    case carDelete(baseUrl: String, uid: String)
    
    
    // MARK: - HTTPMethod
    var method: HTTPMethod {
        switch self {
        case .userList:
            return .get
        case .userGet:
            return .get
        case .userExist:
            return .get
        case .userSessionGet:
            return .get
        case .userEnrollmentGet:
            return .get
        case .consumeCode:
            return .get
        case .verifyCode:
            return .get
        case .stolenSensor:
            return .get
        case .getSensor:
            return .get
        case .getCar:
            return .get
        case .resetPassword:
            return .get
        case .userRanking:
            return .get
        case .getUserDashboard:
            return .get
        case .getUserNotificationMessage:
            return .get
        case .markMessageAsRead:
            return .get
    
        case .getArchievement:
            return .get
            
        case .getAwards:
            return .get
            
        case .userCreate:
            return .post
        case .sensorSave:
            return .post
        case .carSave:
            return .post
        case .userUpdate:
            return .put
        case .sensorDelete:
            return .delete
        case .carDelete:
            return .delete
        }
    }
    
    // MARK: - Path
    var path: String {
        switch self {
        case .userList(let baseUrl):
            return baseUrl + "/users"
        case .userGet(let baseUrl, let uuid):
            return baseUrl + "/users/\(uuid)"
        case .userExist(let baseUrl, let email):
            return baseUrl + "/users/\(email)/exists"
        case .userSessionGet(let baseUrl, let uid, let start, let end):
            return baseUrl + "/users/\(uid)/sessions?end=\(end)&start=\(start)"
        case .getCar(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/car"
        case .userCreate(let baseUrl, _):
            return baseUrl + "/users"
        case .sensorSave(let baseUrl, let uuid, _):
            return baseUrl + "/users/\(uuid)/sensor"
        case .carSave(let baseUrl, let uuid, _):
            return baseUrl + "/users/\(uuid)/car"
        case .stolenSensor(let baseUrl, let uid, let uuid):
            return baseUrl + "/users/\(uid)/sensor/\(uuid)/stolen"
        case .getSensor(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/sensor?active=true"
        case .sensorDelete(let baseUrl, let uid, let uuid):
            return baseUrl + "/users/\(uid)/sensor/\(uuid)"
        case .carDelete(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/car"
        case .userUpdate(let baseUrl, let uid, _):
            return baseUrl + "/users/\(uid)"
        case .userEnrollmentGet(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/enrollments"
        case .consumeCode(let baseUrl, let uid, let code):
            return baseUrl + "/users/\(uid)/consume/\(code)"
        case .verifyCode(let baseUrl, let uid, let code):
            return baseUrl + "/users/\(uid)/verify/\(code)"
        case .resetPassword(let baseUrl, let email):
            return baseUrl + "/users/\(email)/reset-password"
        case .userRanking(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/rankings"
        case .getUserDashboard(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/dashboard"
        case .getUserNotificationMessage(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/messages"
        case .markMessageAsRead(let baseUrl, let uid, let mid):
            return baseUrl + "/users/\(uid)/messages/\(mid)"
        case .getArchievement(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/achievements"
        case .getAwards(let baseUrl, let uid):
            return baseUrl + "/users/\(uid)/awards"
            
        }
    }
    
    // MARK: - Parameters
    var parameters: Data? {
        switch self {
        case .userCreate(_, let user):
            //convert struct to json
            let json = try! JSONEncoder().encode(user)
            return json
            
        case .sensorSave(_, _, let sensor):
            //convert struct to json
            let json = try! JSONEncoder().encode(sensor)
            return json
            
        case .carSave(_, _, let car):
            //convert struct to json
            let json = try! JSONEncoder().encode(car)
            return json
            
        case .userUpdate(_, _, let user):
            //convert struct to json
            let json = try! JSONEncoder().encode(user)
            return json
            
        case .userList, .userGet, .userSessionGet, .userExist, .userEnrollmentGet,.sensorDelete, .getSensor, .stolenSensor, .consumeCode, .verifyCode, .getCar, .carDelete, .resetPassword, .userRanking, .getUserDashboard, .getUserNotificationMessage, .markMessageAsRead, .getArchievement, .getAwards:
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
        let token = authRepository.getUserToken()
        
        //Set the Authorization header: auth
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
