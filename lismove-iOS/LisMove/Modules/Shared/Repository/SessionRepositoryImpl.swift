//
//  SessionRepositoryImpl.swift
//  LisMove
//
//

import Foundation
import LisMoveSensorSdk

class SessionRepositoryImpl: SessionRepository{
    
    func getFeedbackFormOptions(onCompleted listener:@escaping (Result<[FeedBackFormOption], Error>) -> ()){
        NetworkingManager.sharedInstance.getSessionRevisionTypes(completion: {result in
            switch result{
            case .success(let options):
                let optionParsed = options.map({FeedBackFormOption(id: $0.id, label: $0.value)})
                listener(Result.success(optionParsed))
                break
            case .failure(let error):
                listener(Result.failure(error))
                break
            }
        })
        /*listener(Result.success([FeedBackFormOption(id: 1, label: "Problemi di connessione al sensore Lis Move"), FeedBackFormOption(id: 2, label: "La sessione mostra +/- km rispetto a quanti percorsi effettivamente"), FeedBackFormOption(id: 3, label: "Calcolo dei punti errat"), FeedBackFormOption(id: 4, label: "Test")]))*/
    }
    
    func getPointFeedbackFormId(onCompleted listener: @escaping (Int)->()){
        getFeedbackFormOptions(onCompleted: {
            result in
            switch result{
            case .success(let options):
                let id  = options.first(where: {$0.label == "POINTS"})?.id ?? 2
                listener(id)
                break
            case .failure(_):
                listener(2)
                break
            }
        })
    }
    
    func requestSessionVerification(
        sessionId: String,
        reason: String,
        types: [Int],
        onCompleted listener:@escaping (Result<Session, Error>) -> ()
    ){
        let request = SessionValidationRequest(id: sessionId, verificationRequired: true, verificationRequiredNote: reason, revisionType: types)
        NetworkingManager.sharedInstance.requestSessionVerification(data: request, completion:  {result in
            switch result{
            case .success(let session):
                try? DBManager.sharedInstance.getDB().safeWrite {
                    DBManager.sharedInstance.saveSession(session: session)
                }
                listener(Result.success(session))
                break
            case .failure(let error):
                listener(Result.failure(error))
                break
                
            }
        })
    }
    
   func computeDistanceAndSendOfflineSession(sessions: [LisMoveSensorHistoryElement]) throws {
        let device = DBManager.sharedInstance.getLismoveDevice()
        let user = DBManager.sharedInstance.getCurrentUser()
        let wheel = device?.wheelDiameter.value != nil ? UInt32(device!.wheelDiameter.value!) : BTConstants.DefaultWheelSize
        let hub = device?.hubCoefficient ?? 1.0
        let req = sessions
            .filter({ element in
                return ((element.stopLap ?? 0) - (element.startLap ?? 0) > 0) && ((element.startUtc ?? 0) > 1515349099000)
            })
            .map({element -> OfflineDataRequest in
                let wheelDiff = (element.stopLap ?? 0) - (element.startLap ?? 0)
                let distance = SensorDataManager.computeDistanceInMeters(wheelDiff: UInt32(wheelDiff), wheelCircunferance: Double(wheel), hubCoefficient: hub) / 1000
                 return OfflineDataRequest(
                    distance: Float(distance),
                    endRevs: element.stopLap,
                    endTime: element.stopUtc,
                    sensor: device?.uuid,
                    startRevs: element.startLap,
                    startTime: element.startUtc,
                    user: user?.uid)
        })
        
        if (!req.isEmpty) {
            sendOfflineSessions(request: req)
        }
    }
    
    func sendOfflineSessions(request: [OfflineDataRequest]){
        NetworkingManager.sharedInstance.saveOfflineSession(sessions: request, completion: {result in
            switch(result){
            case .success(let res):
                LogHelper.log(message: res)
                break
            case .failure(let error):
                LogHelper.log(message: error.localizedDescription)
                break
            }
            })
    }

}
