//
//  UserRepositoryImpl.swift
//  LisMove
//
//

import Foundation
import FirebaseMessaging
import Resolver
class UserRepositoryImpl: UserRepository{
    
    @Injected var phoneRepository: PhoneRepository
    let networkingManager = NetworkingManager.sharedInstance
    let db = DBManager.sharedInstance
    
    
    func getUser(uid: String, onCompletition listener: @escaping (_ user: LismoveUser?, _ error: Error?)->()){
        networkingManager.getUser(uid: uid, completion: { result in
            switch result{
            case .success(let result):
                self.db.saveUser(user: result)
                listener(result, nil)
            case .failure(let error):
          
                if(error.isNetworkError()){
                    let user = DBManager.sharedInstance.getCurrentUser()
                    listener(user, nil)
                }else{
                    LogHelper.log(message: "No Network Error")
                    listener(nil, error)

                }
            }
            
        })
    }
    
    //TODO: check order db - network when active phone check is implemented
    func updateUserLoginData(){
        let user = DBManager.sharedInstance.getCurrentUser()
        do{
            try DBManager.sharedInstance.getDB().safeWrite {
                //update login data
                user?.lastLoggedIn.value = Date().millisecondsSince1970
                user?.activePhone = "\(UIDevice.current.identifierForVendor!.uuidString)"
                user?.activePhoneModel = "\(UIDevice.current.modelName)|\(UIDevice.current.systemVersion)"
                user?.activePhoneVersion = phoneRepository.getAppVersion()
                
                //plus check in case during signup/login bool is not set
                user?.signupCompleted = true
                
                Messaging.messaging().token { token, error in
                  if let error = error {
                    print("Error fetching FCM registration token: \(error)")
                  } else if let token = token {
                      
                      do{
                         
                          try DBManager.sharedInstance.getDB().safeWrite {
                              user?.activePhoneToken = token
                              
                              self.networkingManager.updateUser(user: user!, completion: {result in })
                          }
                      }catch{}

                  }
                }
            }
        }catch{}
 
    }
    
    func updateUserSensor(completeSync:@escaping ()->Void){
        let user = DBManager.sharedInstance.getCurrentUser()
        
        self.networkingManager.getSensor(uid: user!.uid!, completion: { result in
            switch result{
            case .success(let result):
                
                //clean device db
                self.db.deleteDevice()
                
                result.forEach{device in
                    self.db.saveLismoveDevice(device: device)
                }
                
                completeSync()
                
                
            case .failure(let error):
        
                completeSync()
                
                break
            }
            
        })
        
        
    }

    
    
    func checkUserProfileCompleted() -> Bool{
        let user = DBManager.sharedInstance.getCurrentUser()
        
        return !(user!.firstName.isNullOrEmpty() || user!.lastName.isNullOrEmpty() || user!.username.isNullOrEmpty() || user!.email.isNullOrEmpty() || user!.termsAccepted == false)
    }
    
    func hasActiveAchievement(uid: String, onCompletition listener: @escaping (Result<Bool, Error>) -> ()) {
        NetworkingManager.sharedInstance.getUserArchievement(uid: uid, completion: {
            result in
            switch result{
            case .success(let achievements):
                listener(.success(!achievements.isEmpty))
                break
            case .failure(let error):
                listener(.failure(error))
                break
            }
        })
    }
    
    
}



extension Optional where Wrapped == String {
     
  func isNullOrEmpty() -> Bool{
      return (self == nil) || (self?.isEmpty ?? false)
  }
    
}
