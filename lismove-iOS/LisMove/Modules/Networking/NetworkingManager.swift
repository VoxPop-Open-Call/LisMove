//
//  NetworkingClients.swift
//  NetworkingClients
//
//  Created by Francesco Paolo Dellaquila on 28/03/2020.
//  Copyright © 2020 Nextome. All rights reserved.
//

import Foundation
import Alamofire
import GoogleSignIn
import CryptoKit
import RealmSwift
import Resolver

public class NetworkingManager{
    
    //MARK: Singleton
    static let sharedInstance = NetworkingManager()
    @Injected var authRepository: AuthRepository
    
    //bundle parameters
    private static var baseUrl = LisMoveEnvironmentConfiguration.NETWORK_BASE_URL
    
    
    init(){}
    
    
    //MARK: ========== security
    // Adapted from https://auth0.com/docs/api-auth/tutorials/nonce#generate-a-cryptographically-random-nonce
    public func randomNonceString(length: Int = 32) -> String {
      precondition(length > 0)
      let charset: Array<Character> =
          Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
      var result = ""
      var remainingLength = length

      while remainingLength > 0 {
        let randoms: [UInt8] = (0 ..< 16).map { _ in
          var random: UInt8 = 0
          let errorCode = SecRandomCopyBytes(kSecRandomDefault, 1, &random)
          if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
          }
          return random
        }

        randoms.forEach { random in
          if remainingLength == 0 {
            return
          }

          if random < charset.count {
            result.append(charset[Int(random)])
            remainingLength -= 1
          }
        }
      }

      return result
    }
    
    @available(iOS 13, *)
    public func sha256(_ input: String) -> String {
      let inputData = Data(input.utf8)
      let hashedData = SHA256.hash(data: inputData)
      let hashString = hashedData.compactMap {
        return String(format: "%02x", $0)
      }.joined()

      return hashString
    }
    
    
    //MARK: ========================================================== Sync
    //initial sync with server
    public func syncWithServer(){
        
        //get all pending session from db
        let pendingSession = DBManager.sharedInstance.getPendingSession()
        
        if(pendingSession.count > 0){
            
            pendingSession.forEach{REALMsession in
                self.saveSession(session: REALMsession.detached(), completion: { result in
                    switch result{
                    case .success(let SYNCsession):
                            //delete old sinc session
                            //DBManager.sharedInstance.deleteAllSyncSession()
                        
                            do{
                                try DBManager.sharedInstance.getDB().safeWrite {
                                
                                    var sessionToUpdate = pendingSession.filter{$0.sessionCode == REALMsession.sessionCode}.first
                                    
                                    sessionToUpdate = SYNCsession
                                    sessionToUpdate?.sessionCode = REALMsession.sessionCode
                                    sessionToUpdate!.sendToServer = true
                                    DBManager.sharedInstance.getDB().delete(sessionToUpdate!.partials)
                                    
                                    
                                }
                            }catch{
                        
                            }
                            
                        
                    case .failure(_):
                        let jsonData = try! JSONEncoder().encode(pendingSession)
                        let jsonString = String(data: jsonData, encoding: String.Encoding.utf8)

                        
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Errore durante l'invio della sessione. Verrà riprovato in background al prossimo avvio dell'app", "json": jsonString])
                    }
                    
                    
                })
            }
            

        }

    }
    
    
    //MARK: ========================================================== User
    
    
    /*
     GET: reset password user
     */
    public func resetPasswordUser(email: String, completion:@escaping (AFResult<Bool>)->Void){
            
            //start request
            let request = AF.request(UserEndpoint.resetPassword(baseUrl: NetworkingManager.baseUrl, email: email))
            
            request.responseDecodable(of: Bool.self){ response in

                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            

    }
    
    /*
     GET: check user on server
     */
    public func checkUser(email: String, completion:@escaping (AFResult<Bool>)->Void){

            //start request
            let request = AF.request(UserEndpoint.userExist(baseUrl: NetworkingManager.baseUrl, email: email))
            
            request.responseDecodable(of: Bool.self){ response in

                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
    }
    
    
    /*
     POST: save user on server
     */
    public func saveUser(user: LismoveUser, completion:@escaping (AFResult<LismoveUser>)->Void){
        authRepository.refreshUserToken(onCompletition:{ token, error in
            
            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userCreate(baseUrl: NetworkingManager.baseUrl, user: user))
        
            request.responseDecodable(of: LismoveUser.self){ response in

                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
        })
    }
    
    /*
     PUT: update user on server
     */
    public func updateUser(user: LismoveUser, completion:@escaping (AFResult<LismoveUser>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userUpdate(baseUrl: NetworkingManager.baseUrl, uid: user.uid!, user: user.detached()))
            
            request.responseDecodable(of: LismoveUser.self){ response in

                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
            
                
                    completion(.failure(self.handleError(data: response.data, response: response.response)))
                    
                    return
                }
            }
            
        })
        
        
    }
    
    /*
     GET: get user from server
     */
    
    public func getUser(uid: String, completion:@escaping (AFResult<LismoveUser>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userGet(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: LismoveUser.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get user car
     */
    
    public func getUserCar(uid: String, completion:@escaping (CarModification?)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.getCar(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            //server return nil value -> workaround to fix
            request.response{ response in
                
                if response.response?.statusCode == 200{
                    
                    if(response.data != nil){
                        
                        let jsonDecoder = JSONDecoder()
                        let car = try? jsonDecoder.decode(CarModification.self, from: response.data!)
        
                        completion(car)
                        
                    }else{
                        
                        completion(nil)
                    }
                    
                }else{
                    
                    completion(nil)

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get user session
     */
    public func getUserSession(uuid: String, start: String, end: String, completion:@escaping (AFResult<[Session]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userSessionGet(baseUrl: NetworkingManager.baseUrl, uid: uuid, start: start, end: end))
            request.responseDecodable(of: [Session].self){ response in
                
                if response.response?.statusCode == 200{
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
        
    }
    
    /*
     GET: get user dashboard
     */
    public func getUserDashboard(uid: String, completion: @escaping (AFResult<UserDashboard>) -> Void){
        authRepository.refreshUserToken(onCompletition:{ token, error in
            UserDefaults.standard.setValue(token, forKey: "token")
            
            let request = AF.request(UserEndpoint.getUserDashboard(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: UserDashboard.self, completionHandler:{
                response in
                
                if(response.response?.statusCode == 200){
                    completion(response.result)
                }else{
                    completion(.failure(self.handleError(data: response.data, response: response.response)))
                    return 
                }
                
            })
            
        })
    }
    
    /*
     GET: get user notification
     */
    public func getUserNotificationMessage(uid: String, completion:@escaping (AFResult<[NotificationMsg]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.getUserNotificationMessage(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [NotificationMsg].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: mark notification as read
     */
    
    public func markNotificationAsRead(uid: String, mid: Int, completion:@escaping (Bool)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.markMessageAsRead(baseUrl: NetworkingManager.baseUrl, uid: uid, mid: mid))
            
            request.response{ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(true)
                    
                }else{
                    
                    completion(false)

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get archievement
     */
    
    public func getUserArchievement(uid: String, completion:@escaping (AFResult<[Archievement]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.getArchievement(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [Archievement].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    
    
    //MARK: =================== Session
    
    /*
     GET: get user session
     */
    public func getSessionDetail(uid: String, completion:@escaping (AFResult<Session>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(SessionEndpoint.sessionGet(baseUrl: NetworkingManager.baseUrl, uuid: uid))
            
            request.responseDecodable(of: Session.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
        
    }
    
    /*
     POST: save session on server
     */
    public func saveSession(session: Session, completion:@escaping (AFResult<Session>)->Void){
    
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(SessionEndpoint.sessionCreate(baseUrl: NetworkingManager.baseUrl, session: session))
            
            request.responseDecodable(of: Session.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    if(self.handleSessionTimeout(data: response.data)){
                        completion(response.result)
                    }else{
                        completion(.failure(self.handleError(data: response.data, response: response.response)))
                    }

                    return
                }
            }
            
        })
        
    }
    
    public func requestSessionVerification(data: SessionValidationRequest, completion:@escaping (AFResult<Session>)->Void){
    
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(SessionEndpoint.requestSessionVerification(baseUrl: NetworkingManager.baseUrl, uuid: data.id, request: data))
            
            request.responseDecodable(of: Session.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    if(self.handleSessionTimeout(data: response.data)){
                        completion(response.result)
                    }else{
                        completion(.failure(self.handleError(data: response.data, response: response.response)))
                    }

                    return
                }
            }})
    }
            
public func saveOfflineSession(sessions: [OfflineDataRequest], completion:@escaping (AFResult<String>)->Void){

    let request = AF.request(SessionEndpoint.sendSessionHistory(baseUrl: NetworkingManager.baseUrl, sessions: sessions))

    request.response(completionHandler: { response in
        if response.response?.statusCode == 200{
            completion(.success("Sessioni offline inviate correttamente"))
        }else{
            completion(.failure(self.handleError(data: response.data, response: response.response)))
        }
    })
           
}
        
    
    
    public func getSessionRevisionTypes(completion: @escaping (AFResult<[EnumDto]>)->Void){
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(SessionEndpoint.getRevisionType(baseUrl: NetworkingManager.baseUrl))
            
                 request.responseDecodable(of: [EnumDto].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    if(self.handleSessionTimeout(data: response.data)){
                        completion(response.result)
                    }else{
                        completion(.failure(self.handleError(data: response.data, response: response.response)))
                    }

                    return
                }
            }
            
        })
        
    }

    
    
    //MARK: =================== Sensor
    /*
     Get: sync sensor offline
     */
    public func getSensor(uid: String, completion:@escaping (AFResult<[Sensor]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.getSensor(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [Sensor].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
           
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
        
    }
    
    
    /*
     POST: save sensor on server
     */
    public func saveSensor(sensor: Sensor, uid: String, completion:@escaping (AFResult<Sensor>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.sensorSave(baseUrl: NetworkingManager.baseUrl, uid: uid, sensor: sensor))
            
            request.responseDecodable(of: Sensor.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
           
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
        
    }
    
    /*
     DELETE: delete sensor on server
     */
    public func deleteSensor(uuid: String, uid: String, completion:@escaping (Bool)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.sensorDelete(baseUrl: NetworkingManager.baseUrl, uid: uid, uuid: uuid))
            
            request.response { response in
                
                if response.response?.statusCode == 200{
                    
                    completion(true)
                    
                }else{
                    
                    completion(false)

                    return
                }
            }
            
        })
        
    }
    
    /*
     GET: set sensor stolen
     */
    public func stolenSensor(uuid: String, uid: String, completion:@escaping (Bool)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.stolenSensor(baseUrl: NetworkingManager.baseUrl, uid: uid, uuid: uuid))
            
            request.response { response in
                
                if response.response?.statusCode == 200{
                    
                    completion(true)
                    
                }else{
                    
                    completion(false)

                    return
                }
            }
            
        })
        
    }
    
    
    //MARK: ================== Enrollments
    
    /*
     GET: get user enrollments
     */
    public func getEnrollments(uid: String, completion:@escaping (AFResult<[Enrollment]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userEnrollmentGet(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [Enrollment].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                    return
                }
            }
            
        })
    }
    
    
    /*
     GET: get organization Data
     */
    public func getOrganization(oid: Int, completion:@escaping (AFResult<Organization>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.organizationDataGet(baseUrl: NetworkingManager.baseUrl, oid: String(oid)))
            
            request.responseDecodable(of: Organization.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                    return
                }
            }
            
        })
    }
    
    
    /*
     GET: get organization + settings
     */
    public func getOrganizationWithSettings(oid: Int, completion:@escaping (AFResult<OrganizationWithSettings>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            let request = AF.request(OrganizationEndpoint.organizationDataGet(baseUrl: NetworkingManager.baseUrl, oid: String(oid)))
            
            request.responseDecodable(of: Organization.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    let organization = response.value!
                    
                    
                    let request = AF.request(OrganizationEndpoint.organizationSettings(baseUrl: NetworkingManager.baseUrl, oid: oid))
                    
                    request.responseDecodable(of: [SettingsResponse].self){ response in
                        
                        
                        if response.response?.statusCode == 200{
                            
                            let settings = response.value ?? [SettingsResponse]()
                            let settingsFormatted = OrganizationSettings.getFromResponse(id: oid, response: settings)
                
                            let orgWithSett = OrganizationWithSettings(organization: organization, settings: settingsFormatted)
                            
                            completion(.success(orgWithSett))

                            
                        }else{
                            
                            completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                            return
                        }
                    }

                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get settings
     */
    public func getOrganizationSettings(oid: Int, completion:@escaping (AFResult<OrganizationSettings>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
          
                    let request = AF.request(OrganizationEndpoint.organizationSettings(baseUrl: NetworkingManager.baseUrl, oid: oid))
                    
                    request.responseDecodable(of: [SettingsResponse].self){ response in
                        
                        if response.response?.statusCode == 200{
                            
                            let settings = response.value ?? [SettingsResponse]()
                            
                            let settingsFormatted = OrganizationSettings.getFromResponse(id: oid, response: settings)
                            completion(.success(settingsFormatted))

                            
                        }else{
                            
                            completion(.failure(self.handleError(data: response.data, response: response.response, error: response.error)))

                            return
                        }
                    }
        })
    }

                    
             
    
    
    
    /*
     GET: get organization Seat
     */
    public func getOrganizationSeat(oid: Int, completion:@escaping (AFResult<[OrganizationSeat]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.organizationSeat(baseUrl: NetworkingManager.baseUrl, oid: String(oid)))
            
            request.responseDecodable(of: [OrganizationSeat].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     POST: create new OrganizationSeats
     */
    public func createOrganizationSeat(oid: Int, seat: OrganizationSeat, completion:@escaping (AFResult<OrganizationSeat>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.createNewSeat(baseUrl: NetworkingManager.baseUrl, oid: oid, seat: seat))
            
            request.responseDecodable(of: OrganizationSeat.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    
    /*
     GET: consume code
     */
    public func consumeCode(uid: String, code:String, completion:@escaping (AFResult<Enrollment>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.consumeCode(baseUrl: NetworkingManager.baseUrl, uid: uid, code: code))
            
            request.responseDecodable(of: Enrollment.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
   
    /*
     GET: verify code
     */
    public func verifyCode(uid: String, code:String, completion:@escaping (AFResult<Enrollment>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.verifyCode(baseUrl: NetworkingManager.baseUrl, uid: uid, code: code))
            
            request.responseDecodable(of: Enrollment.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get custom fields
     */
    public func getCustomFields(oid: Int, completion:@escaping (AFResult<[CustomField]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.organizationCustomFields(baseUrl: NetworkingManager.baseUrl, oid: oid))
            
            request.responseDecodable(of: [CustomField].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get custom fields Value
     */
    public func getCustomFieldsValue(oid: Int, eid: Int, completion:@escaping (AFResult<[CustomFieldValues]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.organizationCustomFields(baseUrl: NetworkingManager.baseUrl, oid: oid))
            
            request.responseDecodable(of: [CustomFieldValues].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    
    /*
     POST: update custom field value
     */
    public func updateCustomFieldValue(oid: Int, field: CustomFieldValues, completion:@escaping (AFResult<CustomFieldValues>)->Void){
        authRepository.refreshUserToken(onCompletition:{ token, error in
            
            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(OrganizationEndpoint.updateCustomFieldsValue(baseUrl: NetworkingManager.baseUrl, oid: oid, field: field))
        
            request.responseDecodable(of: CustomFieldValues.self){ response in

                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
        })
    }
    
    //MARK: ================== Rankings
    
    /*
     GET: download specific ranking by id
     */
    public func getRanking(id: Int, completion:@escaping (AFResult<Ranking>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(RankingEndpoint.getRanking(baseUrl: NetworkingManager.baseUrl, id: id))
            
            request.responseDecodable(of: Ranking.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: download all ranking
     */
    public func getAllRanking(active: Bool, national: Bool, withPositions: Bool, completion:@escaping (AFResult<[Ranking]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(RankingEndpoint.getAllRanking(baseUrl: NetworkingManager.baseUrl, active: active, national: national, withPositions: withPositions))
            
            request.responseDecodable(of: [Ranking].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    public func getUserRanking(uid: String, completion:@escaping (AFResult<[Ranking]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.userRanking(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [Ranking].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: download global ranking
     */
    public func getGlobalRanking(completion:@escaping (AFResult<Ranking>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(RankingEndpoint.getGlobalRanking(baseUrl: NetworkingManager.baseUrl))
            
            request.responseDecodable(of: Ranking.self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    /*
     GET: get ranking awards
     */
    public func getAwardRanking(rid: Int, completion:@escaping (AFResult<[AwardRanking]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(RankingEndpoint.getAwards(baseUrl: NetworkingManager.baseUrl, rid: rid))
            
            request.responseDecodable(of: [AwardRanking].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    //MARK: ================== Achievement
    /*
     GET: get achievement awards
     */
    public func getAchievementAwards(aid: Int, completion:@escaping (AFResult<[AwardAchievement]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(ArchievementEndpoint.getAwards(baseUrl: NetworkingManager.baseUrl, aid: aid))
            
            request.responseDecodable(of: [AwardAchievement].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    //MARK: ================== Awards
    public func getUserAwards(uid: String, completion:@escaping (AFResult<[Award]>)->Void){
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.getAwards(baseUrl: NetworkingManager.baseUrl, uid: uid))
            
            request.responseDecodable(of: [Award].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    //MARK: ================== Cars
    
    public func getCarBrand(completion:@escaping (AFResult<[CarBrand]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(CarEndpoint.getCarBrands(baseUrl: NetworkingManager.baseUrl))
            
            request.responseDecodable(of: [CarBrand].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    public func getCarModels(bid: Int, completion:@escaping (AFResult<[CarModel]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(CarEndpoint.getCarModels(baseUrl: NetworkingManager.baseUrl, bid: bid))
            
            request.responseDecodable(of: [CarModel].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    public func getCarGenerations(bid: Int, mid: Int, completion:@escaping (AFResult<[CarGeneration]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(CarEndpoint.getCarGenerations(baseUrl: NetworkingManager.baseUrl, bid: bid, mid: mid))
            
            request.responseDecodable(of: [CarGeneration].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    public func getCarModifications(bid: Int, mid: Int, gid:Int, completion:@escaping (AFResult<[CarModification]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(CarEndpoint.getCarModifications(baseUrl: NetworkingManager.baseUrl, bid: bid, mid: mid, gid: gid))
            
            request.responseDecodable(of: [CarModification].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }
    
    public func saveUserCar(uuid: String, car: CarModification, completion:@escaping (Bool)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.carSave(baseUrl: NetworkingManager.baseUrl, uid: uuid, car: car))
            
            request.responseDecodable(of: [CarModification].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(true)
                    
                }else{
                    
                    completion(false)

                    
                }
            }
            
        })
    }
    
    public func deleteUserCar(uuid: String, completion:@escaping (Bool)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(UserEndpoint.carDelete(baseUrl: NetworkingManager.baseUrl, uid: uuid))
            
            request.response{ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(true)
                    
                }else{
                    
                    completion(false)

                    
                }
            }
            
        })
    }
    
    
    //MARK: ======================== LogWall
    public func getLogWall(completion:@escaping (AFResult<[String]>)->Void){
        
        authRepository.refreshUserToken(onCompletition:{ token, error in

            UserDefaults.standard.setValue(token, forKey: "token")
            
            //start request
            let request = AF.request(LogWallEndpoint.getLogwall(baseUrl: NetworkingManager.baseUrl))
            
            request.responseDecodable(of: [String].self){ response in
                
                if response.response?.statusCode == 200{
                    
                    completion(response.result)
                    
                }else{
                    
                    completion(.failure(self.handleError(data: response.data, response: response.response)))

                    return
                }
            }
            
        })
    }

    
    //MARK: ======================== Network error dispatcher
    private func handleError(data: Data?, response: HTTPURLResponse?, error: AFError? = nil) -> AFError{
        let decoder = JSONDecoder()
        var isErrorNetwork = error?.isNetworkError()
        LogHelper.log(message: "isNetworkError \(isErrorNetwork))")

        guard let json = data else{
            //generic error
            if let error = (error?.underlyingError as? URLError){
                switch error.code {
                case .notConnectedToInternet:
                    let userInfo: [String : Any] =
                                [
                                    NSLocalizedDescriptionKey :  NSLocalizedString("La connessione ad internet risulta instabile o assente. Riprova più tardi" , value: "La connessione ad internet risulta instabile o assente. Riprova più tardi", comment: "")
                            ]
                   return AFError.createURLRequestFailed(error: NSError(domain:"", code: error.code.rawValue, userInfo: userInfo))
                default:
                    LogHelper.log(message: "default")
                    break
                }
            }
            return AFError.createURLRequestFailed(error: NSError(domain:"", code: response?.statusCode ?? 0, userInfo: nil))
        }
        
        let defaultMessage = "Si è verificato un errore temporaneo"
        guard let responseMessage = try? decoder.decode(NetworkError.self, from: json) else {
            
            //generic error
            return AFError.createURLRequestFailed(error: NSError(domain:"", code: response?.statusCode ?? 0, userInfo: nil))
        }
        let errorCode = response?.statusCode ?? 0
        let errorMessage = errorCode == 500 ? defaultMessage : responseMessage.message
        let error = errorCode == 500 ? "Errore temporeaneo" : (responseMessage.error ?? "Errore")
        
        let userInfo: [String : Any] =
                    [
                        NSLocalizedDescriptionKey :  NSLocalizedString(error , value: responseMessage.message ?? "Impossibile completare la richiesta. Riprova", comment: "")
                ]
        return AFError.createURLRequestFailed(error: NSError(domain:"", code: response?.statusCode ?? 0, userInfo: userInfo))
    }
    
   
    
    private func handleSessionTimeout(data: Data?) -> Bool{
        let decoder = JSONDecoder()
        
        guard let json = data else{
            //generic error
            return false
        }
        
        guard let responseMessage = try? decoder.decode(NetworkError.self, from: json) else {
            
            //generic error
            return false
        }
        
        if(responseMessage.error == "NSURLErrorTimedOut"){
            return true
        }
        
        
        return false
    }
}
