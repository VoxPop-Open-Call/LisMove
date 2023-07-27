//
//  AuthRepositoryImpl.swift
//  LisMove
//
//

import Foundation
import FirebaseAuth

class AuthRepositoryImpl: AuthRepository{
    var token: String? = nil
    
    func refreshUserToken(onCompletition listener: @escaping (String?, Error?)->()){
        Auth.auth().currentUser?.getIDTokenResult(forcingRefresh: true, completion: { token, error in
            self.token = token?.token ?? ""
            listener(token?.token, error)
        })
        
    }
    
    func getUserToken()-> String{
        
        return token ?? ""
    }
    
    func getCurrentUser() -> String? {
        
        let auth = Auth.auth().currentUser
        return auth?.uid
    }
    
    func logout(){
        
        if SessionManager.sharedInstance.sensorSDK != nil{
            // stop sensor detection
            SessionManager.sharedInstance.sensorCompanionManager?.alwaysTryReconnectingToSensor = false
            SessionManager.sharedInstance.sensorSDK.stopScan()
            
            let sensor = SessionManager.sharedInstance.sensorSDK.currentSensor
            SessionManager.sharedInstance.sensorSDK.disconnectSensor(sensorID: sensor?.peripheral.identifier.uuidString)
        }

        
        //delete data from db
        DBManager.sharedInstance.logout()
        
        //logout from firebase
        try? Auth.auth().signOut()
 
    }
    
}
