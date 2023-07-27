//
//  InitiativeRepositoryImpl.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation
import simd

class InitiativeRepositoryImpl: InitiativeRepository{
    
    //getInitiatives
    //getctiveInititives
    
    func getInitiatives(uid: String, onCompleted completition: @escaping((Result<[EnrollmentWithOrganization], Error>) -> ())){
        getEnrollements(uid: uid, onCompleted: {result in
            switch result{
            case .success(let enrollments):
                self.getOrganization(oids: Array(Set(enrollments.map({$0.organization ?? 0}))), onCompleted: { result in
                    switch result{
                    case .success(let organizations):
                        completition(Result.success(self.getEnrollmentWithOrganization(enrollments: enrollments, organizations: organizations)))
                    case .failure(let error):
                        completition(Result.failure(error))
                    }
                })
            case .failure(let error):
                completition(Result.failure(error))
            }
        })
    }
    
    private func getEnrollmentWithOrganization(enrollments: [Enrollment], organizations: [Organization])-> [EnrollmentWithOrganization]{
        var result = [EnrollmentWithOrganization]()
        enrollments.forEach({enrollment in
            if let organization = organizations.first(where: {$0.id == enrollment.organization}){
                result.append(EnrollmentWithOrganization(enrollment: enrollment, organization: organization))
            }
        })
        return result
    }
    
    //TODO: Filter this
    func getActiveInitiatives(uid: String, onCompleted completition: @escaping((Result<[EnrollmentWithOrganization], Error>) -> ())){
        getInitiatives(uid: uid, onCompleted: { result in
            completition(result)
        })
    }
    
    func getEnrollements(uid: String, onCompleted completition:@escaping (Result<[Enrollment], Error>) -> ()) {
        
        NetworkingManager.sharedInstance.getEnrollments(uid: uid) { result in
            switch result {
                case .success(let data):
                    
                //MARK: 1. order data
                let orderedEnrollement = data.sorted(by: {$0.endDate ?? 0 > $01.endDate ?? 0})
                
                //MARK: 2. update DB
                for item in orderedEnrollement{
                    DBManager.sharedInstance.saveEnrollment(enrollment: item)
                }

                
                //MARK: 3. return data
                return completition(Result.success(orderedEnrollement))

                
                case .failure(let error):
                if(error.isNetworkError()){
                    let enrollments = DBManager.sharedInstance.getUserEnrollment()
                    return completition(Result.success(enrollments))
                }else{
                    completition(Result.failure(error))
                }
            }
        }
            
    }
    
    
    func getOrganization(oid: Int, onCompleted completition:@escaping (Result<Organization, Error>) -> ()) {
        
        NetworkingManager.sharedInstance.getOrganization(oid: oid) { result in
            switch result {
                case .success(let data):
                
                //MARK: 1. update DB
                DBManager.sharedInstance.saveOrganization(organization: data)

                
                //MARK: 3. return data
                return completition(Result.success(data))

                
                case .failure(let error):
                if(error.isNetworkError()){
                    //MARK: 1. check offline enrollment
                    let organization = DBManager.sharedInstance.getUserOrganization(oid: oid)
                
                    if let organization = organization {
                        completition(Result.success(organization))
                    }else{
                        completition(Result.failure(error))
                    }
                }else{
                    completition(Result.failure(error))
                }
        }
        
        }
        
    }
    
    func getOrganization(oids: [Int], onCompleted completition:@escaping (Result<[Organization], Error>) -> ()) {
        
        var organizations: [Organization] = []
        var receivedError = false
        for id in oids {
            
            getOrganization(oid: id, onCompleted: {result in
                switch result{
                case .success(let organization):
                    organizations.append(organization)
                    if(organizations.count == oids.count){
                        completition(Result.success(organizations))
                    }
                case .failure(let error ):
                    if(!receivedError){
                        completition(Result.failure(error))
                        receivedError = true
                    }
                }
            })
        }
        
    }
    
    
    func getOrganizationSettings(oid: Int, onCompleted completition:@escaping (Result<OrganizationSettings, Error>) -> ()) {
        
        NetworkingManager.sharedInstance.getOrganizationSettings(oid: oid) { result in
            switch result {
                case .success(let data):
                
                //MARK: 1. update DB
                DBManager.sharedInstance.saveOrganizationSettings(organizationSettings: data)

                
                //MARK: 3. return data
                return completition(Result.success(data))

                
                case .failure(let error):
                
                    //MARK: 1. check offline enrollment
                    if(error.isNetworkError()){
                        let organizationSettings = DBManager.sharedInstance.getOrganizationSettings(oid: oid)
                        if let organizationSettings = organizationSettings {
                            return completition(Result.success(organizationSettings))
                        }else{
                            completition(Result.failure(error))
                        }

                    }else{
                        completition(Result.failure(error))
                    }
                
                    
            }
        }
        
    }
    
    func getOrganizationSettings(oids: [Int], onCompleted completition:@escaping (Result<[OrganizationSettings], Error>) -> ()) {
        
        var settings: [OrganizationSettings] = []
        var receivedError = false
        
        for id in oids {
            self.getOrganizationSettings(oid: id, onCompleted: {result in
                switch result {
                case .success(let setting):
                    settings.append(setting)
                    if(settings.count == oids.count){
                        completition(Result.success(settings))
                    }
                case .failure(let error):
                    if(!receivedError){
                        completition(Result.failure(error))
                        receivedError = true
                    }
                    
                }
            })
        }
        
    }
    
    
    func getOrganizationWithSettings(oid: Int, onCompleted completition:@escaping (Result<OrganizationWithSettings, Error>) -> ()) {
        
        self.getOrganization(oid: oid) {result in
            switch result {
                case .success(let organization):

            
                self.getOrganizationSettings(oid: oid) {result in
                    switch result {
                        case .success(let settings):
                        
                            return completition(Result.success(OrganizationWithSettings(organization: organization, settings: settings)))

                        
                        case .failure(let error):
                            
                            //MARK: 2. show error
                            return completition(Result.failure(error))
                    }
                    
                }

                
                case .failure(let error):
                
                    //MARK: 2. show error
                    return completition(Result.failure(error))
            }
        }
    }
    
    
    func getOrganizationWithSettings(oids: [Int], onCompleted completition:@escaping (Result<[OrganizationWithSettings], Error>) -> ()) {
        
        var organizationWithSettings = [OrganizationWithSettings]()
        var receivedError = false
        
        for id in oids{
            self.getOrganizationWithSettings(oid: id, onCompleted: { result in
                switch result{
                case .success(let element):
                    organizationWithSettings.append(element)
                    if(organizationWithSettings.count == oids.count){
                        completition(Result.success(organizationWithSettings))
                    }
                    
                    
                case .failure(let error):
                    if(!receivedError){
                        completition(Result.failure(error))
                        receivedError = true
                    }
                }
            })
        }
        
    }
    
}
