//
//  DrinkingFountainRepositoryImpl.swift
//  LisMove
//
//

import Foundation
import FirebaseDatabase
import CodableFirebase
class FountainRepositoryImpl: FountainRepository{
    static let PATH = "drinkingFountains"
    let ref: DatabaseReference = Database.database(url: LisMoveEnvironmentConfiguration.FIREBASE_REALTIME_URL).reference(withPath: PATH)
    
    func getActiveFountainList(onCompletition listener: @escaping ([Fountain]?, Error?)->()){
        ref.getData {(error, snapshot) in
            guard error == nil else{
                listener(nil, error)
                return
            }
            
            var fountainList: [Fountain] = []
            if snapshot.exists() {
                let dictList = snapshot.children
                dictList.forEach{child in
                    let key = (child as! DataSnapshot).key
                    
                    if let childSnapshot = snapshot.childSnapshot(forPath: key).value as? [String: Any] {
                        do{
                            var fountain = try FirebaseDecoder().decode(Fountain.self, from: childSnapshot)
                            fountain.id = key
                            if(!(fountain.isDeleted)){
                                fountainList.append(fountain)
                            }
                        }catch let error {
                            LogHelper.logError(message: "Error parsing fountain \(error.localizedDescription)", withTag: "FountainRepository [GET]")
                        }
                    }
                   
                }
            }
            listener(fountainList, nil)
        }
    }
    
    func addFountain(fountain: Fountain, onCompletition listener: @escaping (Error?)->()){
        let encodedFountain = try! FirebaseEncoder().encode(fountain)
        ref.child(UUID().uuidString).setValue(encodedFountain, withCompletionBlock: {
            error, _ in
            listener(error)
        })
    }
    
    func deleteFountain(fountain: Fountain, uid: String, onCompletition listener: @escaping (Error?)->()){
        guard let id = fountain.id else{
            LogHelper.logError(message: "Fountain id is null", withTag: "FountainRepository [DELETE]")
            listener(LisMoveError.runtimeError("Si è verificato un errore"))
            return
        }
        var updatedFountain = fountain
        updatedFountain.deleted = true
        updatedFountain.deletedAt = DateTimeUtils.getCurrentTimestamp()
        updatedFountain.deletedBy = uid
        let encodedFountain = try! FirebaseEncoder().encode(updatedFountain)
        ref.child(id).setValue(encodedFountain, withCompletionBlock: {
            error, _ in
            listener(error)
        })
    }
}
