//
//  DBManager.swift
//  Lismove
//
//  Created by Francesco Paolo Dellaquila on 16/02/21.
//

import Foundation
import RealmSwift
import AppFolder
import Bugsnag
import SwiftLog
import LisMoveSensorSdk
import Resolver


class DBManager{

    //MARK: Singleton
    static let sharedInstance = DBManager()
    //DB
    private var LismoveDB: Realm?
    static let path = AppFolder.Library.Application_Support.appending(LisMoveEnvironmentConfiguration.DB_NAME).url
    static let config = Realm.Configuration(
        fileURL: path,
        readOnly: false,
        schemaVersion: 40)
    
    
    @Injected var authRepository: AuthRepository
    

    //MARK: Init
    private init(){

        do{
            self.LismoveDB = try Realm(configuration: DBManager.config)
            print("-------DB PATH-------\n \(DBManager.path)")
        }
        catch{
            print(error.localizedDescription)
            //BUGSNAG report
            Bugsnag.leaveBreadcrumb(withMessage: "Database init exception: \(error.localizedDescription)")
        }
    }
    
    
    
    //MARK: public interfaces
    
    //MARK: common features---------------
    //MARK: get DB
    public func getDB() -> Realm{
        return self.LismoveDB!
    }
    
    
    //MARK: ========================USER
    
    //save user
    public func saveUser(user: LismoveUser){
        do{
            try self.LismoveDB!.safeWrite {
                                
                self.LismoveDB!.add(user, update: .modified)
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getCurrentUser() -> LismoveUser?{
        if let uid = authRepository.getCurrentUser(){
            return self.LismoveDB!.objects(LismoveUser.self).first(where: {$0.uid == uid})
        }else{
            return nil
        }
    }
    
    public func updateCurrentUser(firstName: String, lastName: String, date: Int64, gender: String, city: String){
        do{
            try self.LismoveDB!.safeWrite {

                let user = self.LismoveDB!.objects(LismoveUser.self).first
                
                user?.firstName = firstName
                user?.lastName = lastName
                
                user?.birthDate.value = date
                
                user?.gender = gender
                user?.cityLisMove = city

            }
        }catch{
            
        }
    }
    
    public func logout(){
        
        //delete sensor uuid
        resetDefaults()
        
        do{
            try self.LismoveDB!.safeWrite {
                
                //let user = self.getCurrentUser()
                //let device = self.getLismoveDevice()
                
                /*
                if(user != nil){
                    self.LismoveDB?.delete(user!)
                }
                
                if(device != nil){
                    self.LismoveDB?.delete(device!)
                }*/
                
                
                //MARK: clean all db table
                self.cleanDB()

            }
        }catch{
            
        }
    }
    
    func resetDefaults() {
        let defaults = UserDefaults.standard
        let dictionary = defaults.dictionaryRepresentation()
        dictionary.keys.forEach { key in
            defaults.removeObject(forKey: key)
        }
    }
    
    public func cleanDB(){

        do{
            try self.LismoveDB!.safeWrite {
                self.LismoveDB!.deleteAll()
            }
        }
        catch{
            print("Database clean exception")
        }
    }
    
    
    public func restartAccountFlow(){
        do{
            try self.LismoveDB!.safeWrite {
                
                let users = self.LismoveDB!.objects(LismoveUser.self)
                self.LismoveDB?.delete(users)

            }
        }catch{
            
        }
    }
    
    //MARK: =====================Session
    public func saveSession(session: Session){
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.add(session)
            
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getOnGoinSession() -> Session?{
        return self.LismoveDB!.objects(Session.self).filter("endTime == null").first
    }
    
    public func getPendingSession() -> [Session]{
        return Array(self.LismoveDB!.objects(Session.self).filter("endTime != null AND sendToServer == false"))
    }
    
    public func deleteAllSyncSession(){
        do{
            try self.LismoveDB!.safeWrite {
                
                let objects = self.LismoveDB!.objects(Session.self).filter("sendToServer == true")
                self.LismoveDB!.delete(objects)
            
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    
    public func getLastSession() -> Session?{
        return self.LismoveDB!.objects(Session.self).last
    }
    
    //MARK: =================LisMove Device
    public func saveLismoveDevice(device: Sensor){
        
        //save to user prefrences
        UserDefaults.standard.set(device.uuid, forKey: BTConstants.SensorUserDefaultsKey)
        
        //save to db
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.add(device, update: .modified)
            
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getLismoveDevice() -> Sensor?{
        return self.LismoveDB!.objects(Sensor.self).first
    }
    
    public func deleteDevice(){
        //delete from shared
        UserDefaults.standard.removeObject(forKey: BTConstants.SensorUserDefaultsKey)
        
        //delete from db
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.delete(self.LismoveDB!.objects(Sensor.self))
  
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    
    }
    
    //MARK: =============== ENROLLMENT
    public func saveEnrollment(enrollment: Enrollment){
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.add(enrollment.asEnrollment_DB(), update: .all)
  
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getUserEnrollment() -> [Enrollment]{
        return self.LismoveDB!.objects(Enrollment_DB.self).map{$0.asEnrollment()}
    }
    
    //MARK: =============== ORGANIZATION
    public func saveOrganization(organization: Organization){
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.add(organization.asOrganization_DB(), update: .all)
  
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getUserOrganization(oid: Int) -> Organization?{
        return self.LismoveDB!.objects(Organization_DB.self).filter("id = \(oid)").first?.asOrganization()
    }
    
    public func getAllUserOrganization() -> [Organization]{
        return self.LismoveDB!.objects(Organization_DB.self).map{$0.asOrganization()}
    }
    
    public func saveOrganizationSettings(organizationSettings: OrganizationSettings){
        do{
            try self.LismoveDB!.safeWrite {
                
                self.LismoveDB!.add(organizationSettings.asOrganizationSettings_DB(), update: .all)
  
            }
        }catch(let error){
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "DB Exception: \(error.localizedDescription)"])
        }
    }
    
    public func getOrganizationSettings(oid: Int) -> OrganizationSettings?{
        return self.LismoveDB!.objects(OrganizationSettings_DB.self).filter("id = \(oid)").first?.asOrganizationSettings()
    }
    
    public func getAllOrganizationSettings() -> [OrganizationSettings]{
        return self.LismoveDB!.objects(OrganizationSettings_DB.self).map{$0.asOrganizationSettings()}
    }
    
}

