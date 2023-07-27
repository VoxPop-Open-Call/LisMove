//
//  SettingsRepositoryImpl.swift
//  LisMove
//
//

import Foundation
import FirebaseDatabase
import CodableFirebase

class SettingsRepositoryImpl: SettingsRepository{
    static let PATH = "settings"
    let ref: DatabaseReference = Database.database(url: LisMoveEnvironmentConfiguration.FIREBASE_REALTIME_URL).reference(withPath: PATH)
    
    func getSettings(onCompleted listener: @escaping (Result<LisMoveSettings, Error>)->()){
        ref.getData(completion: { (error, snapshot) in
            guard error == nil else{
                // Error in firebase get value that calls callback on the background thread, should be fixed in new versions
                DispatchQueue.main.async {
                    listener(Result.failure(error!))
                }
                return
            }
            
            guard let value = snapshot.value else {return}
            do{
                let setting = try FirebaseDecoder().decode(LisMoveSettings.self, from: value)
                DispatchQueue.main.async {
                    listener(Result.success(setting))
                }
            }catch let error{
                DispatchQueue.main.async {
                    listener(Result.failure(error))
                }
            }
        
            
        })
    }

}
