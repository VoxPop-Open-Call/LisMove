//
//  SessionPointsDetailViewModel.swift
//  LisMove
//
//

import Foundation
import Resolver

class SessionPointDetailViewModel{
    var pointsUI = [PointItemUI]()
    var delegate: SessionPointDetailDelegate? = nil
    var organizationRetrieved = 0
    var dataLoaded = false
    @Injected var initiativeRepository: InitiativeRepository
    
    
    func loadPointsFromSession(session: Session){
        organizationRetrieved = 0
        pointsUI.removeAll()
        if(session.sessionPoints.isEmpty){
            self.dataLoaded = true
            self.delegate?.onDatUpdate(points: self.pointsUI)
        }
        session.sessionPoints.forEach({sessionPoint in
            guard let organizationId = sessionPoint.organizationId.value else {return}
            getOrganization(organizationId, onCompleted: { organization in
                let newPoint = PointItemUI(name: organization.title ?? "", point: "\(sessionPoint.points.value ?? 0)")
                self.pointsUI.append(newPoint)
                if(self.organizationRetrieved == session.sessionPoints.count){
                    self.dataLoaded = true
                    self.delegate?.onDatUpdate(points: self.pointsUI)
                }
            })
        })
    }
    
    func getOrganization(_ id: Int, onCompleted listener: @escaping (Organization)->()){
        
        self.initiativeRepository.getOrganization(oid: id) { result in
            switch result {
                case .success(let data):
                self.organizationRetrieved += 1
                    listener(data)
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        }
  
    }
}

protocol SessionPointDetailDelegate{
    func onDatUpdate(points: [PointItemUI])
}
