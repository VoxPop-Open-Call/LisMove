//
//  UserRepository.swift
//  LisMove
//
//

import Foundation
protocol UserRepository{
    
    /*
     get user informations
     */
    func getUser(uid: String, onCompletition listener: @escaping (_ user: LismoveUser?,_ error: Error?)->())
    
    
    /*
     update user data after all successfully login into app
     */
    func updateUserLoginData()
    
    /*
     update user sensor data after all successfully login into app
     */
    func updateUserSensor(completeSync:@escaping ()->Void)
    
    /*
     check if profile is complete
     */
    func checkUserProfileCompleted() -> Bool
    
    func hasActiveAchievement(uid: String, onCompletition listener: @escaping (Result<Bool, Error>)->())
    
}
