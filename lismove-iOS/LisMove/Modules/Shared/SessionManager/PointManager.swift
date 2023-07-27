//
//  PointManager.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 25/08/21.
//

import Foundation
import MapKit
import RealmSwift
import Bugsnag
import Resolver

class PointManager{
    let TAG = "PointManager"

    var polygonList: [Int: [MKPolygon]] = [:]
    public var organizationList: [Organization] = []
    public var enrollmentsList: [Enrollment] = []
    var lastDistanceInKm = 0.0
    @Injected var initiativeRepository: InitiativeRepository

    init(recoveredDistanceInKm: Double = 0.0){
        lastDistanceInKm = recoveredDistanceInKm
        syncInitiative()
    }
    
    //sync user initiative and start geofencing request
    public func syncInitiative(){
        LogHelper.log(message: "sync initiatives", withTag: TAG)

        let user = DBManager.sharedInstance.getCurrentUser()
    
        self.initiativeRepository.getEnrollements(uid: user!.uid ?? "") { result in
            switch result {
            case .success(let data):

                self.enrollmentsList = data.filter{!$0.isClosed()}

                self.organizationList.removeAll()

                self.initiativeRepository.getOrganization(oids: self.enrollmentsList.compactMap{$0.organization}) { result in
                    switch result {
                    case .success(let organizations):

                        self.organizationList = organizations

                        //init geofencing manager with all organizations coordinates
                        self.initGeofencing(organizations: organizations)
                        Bugsnag.leaveBreadcrumb(withMessage:"[PointManager] Get organizations, count: \(self.organizationList.count)")
                    case .failure(let error):
                        //MARK: ERROR STREAM
                        Bugsnag.leaveBreadcrumb(withMessage:"[PointManager] Failed to get organization")
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])

                    }
                }

            case .failure(let error):
                //MARK: ERROR STREAM
                Bugsnag.leaveBreadcrumb(withMessage:"[PointManager] Failed to get enrollment")
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                break
            }
        }
    }
    
    /*
     init polygon list area to check user position for initiative points
     */
    func initGeofencing(organizations: [Organization]){
        
        for organization in organizations {
            //convert string to GMS path
            guard let path = organization.getCoordinates() else{
                return
            }

            path.forEach{item in
                let coordinates = item.map{CLLocationCoordinate2D(latitude: $0.lat!, longitude: $0.lng!)}

                //create polygon
                let polygon = MKPolygon(coordinates: coordinates, count: coordinates.count)

                if(self.polygonList[organization.id!] == nil){
                    self.polygonList[organization.id!] = []
                }

                self.polygonList[organization.id!]?.append(polygon)

            }
            LogHelper.log(message: "Initialized geofancing for initiative \(organization.id)", withTag: TAG)
        }

    }
    
    func isPositionInUrbanPolygon(lat: Double, lng: Double) -> Bool{
        
        for (_,item) in self.polygonList{
            if(isPointInPolygon(lat: lat, lng: lng, polygon: item)){
                return true
            }
        }
        
        return false
    }

    func calculatePoint(session: Session, lat: Double, lng: Double, distance: Double) -> Session{
    
        var updatableSession = initSessionPointIfFirstTime(session: session)
        let deltaDistance = distance - lastDistanceInKm
        LogHelper.log(message: "calculatePoint \(deltaDistance), \(distance), \(lastDistanceInKm), \(updatableSession.nationalKM)", withTag:   TAG)
        lastDistanceInKm = distance
        if(deltaDistance == 0){
                return session
        }//update nation km and points
        
        updatableSession.nationalKM += deltaDistance
        updatableSession.nationalPoints = Int(session.nationalKM * 10.0)
        //update initative
        
        for (id,item) in self.polygonList{
            if(isPointInPolygon(lat: lat, lng: lng, polygon: item)){
                updatableSession = addInitiativeKm(distance: deltaDistance, initiativeId: id, session: session)
            }
        }
        
    

        LogHelper.log(message: updatableSession.getPointsLog(), withTag: TAG)
        return updatableSession
        
        

    }
    
    func addInitiativeKm(distance: Double, initiativeId: Int, session: Session) -> Session{
        LogHelper.log(message: "Add initiativeKm Distance \(distance), initiative \(initiativeId)", withTag: TAG)
        if let index = session.sessionPoints.firstIndex(where: {$0.organizationId.value == initiativeId}){
            LogHelper.log(message: "Add initiativeKm to index \(index)", withTag: TAG)

            session.sessionPoints[index].distance += distance
            session.sessionPoints[index].points.value = Int(session.sessionPoints[index].distance  * 10)
        }

        return session
    }
    
    func isPointInPolygon(lat: Double, lng: Double, polygon: [MKPolygon])-> Bool{
        var isInPoilygon = false
        polygon.forEach{ path in
            if path.contain(coor: CLLocationCoordinate2D(latitude: lat, longitude: lng)){
                isInPoilygon = true
            }
        }
        return isInPoilygon
    }
    
    func initSessionPointIfFirstTime(session: Session) -> Session{
        if(session.sessionPoints.isEmpty && !enrollmentsList.isEmpty){
            LogHelper.log(message: "InitSessionPointForTheFirstTime", withTag: TAG)
            session.sessionPoints.append(objectsIn: getInitialSessionPoints(sessionId: session.sessionCode ?? ""))
            lastDistanceInKm = session.nationalKM
            LogHelper.log(message: "SessionPointSize \(session.sessionPoints.count)",withTag: TAG)
        }
        return session
    }
    
    func getInitialSessionPoints(sessionId: String) -> List<SessionPoint>{
        var sessionPoints = List<SessionPoint>()
        enrollmentsList.forEach({enrollment in
            let sessionPoint = SessionPoint()
            sessionPoint.organizationId.value = enrollment.organization ?? -1
            sessionPoint.sessionId = sessionId
            sessionPoint.organizationTitle = organizationList.first(where: {$0.id == sessionPoint.organizationId.value})?.title ?? ""
            sessionPoints.append(sessionPoint)
        })
        return sessionPoints
    }
}


extension MKPolygon {
    func contain(coor: CLLocationCoordinate2D) -> Bool {
        let polygonRenderer = MKPolygonRenderer(polygon: self)
        let currentMapPoint: MKMapPoint = MKMapPoint(coor)
        let polygonViewPoint: CGPoint = polygonRenderer.point(for: currentMapPoint)
        if polygonRenderer.path == nil {
          return false
        }else{
          return polygonRenderer.path.contains(polygonViewPoint)
        }
    }
}

public extension MKMultiPoint {
    var coordinates: [CLLocationCoordinate2D] {
        var coords = [CLLocationCoordinate2D](repeating: kCLLocationCoordinate2DInvalid,
                                              count: pointCount)

        getCoordinates(&coords, range: NSRange(location: 0, length: pointCount))

        return coords
    }
}

