//
//  InitiativeRepository.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation

protocol InitiativeRepository{
    func getInitiatives(uid: String, onCompleted completition: @escaping((Result<[EnrollmentWithOrganization], Error>) -> ()))
    
    func getEnrollements(uid: String, onCompleted completition:@escaping (Result<[Enrollment], Error>) -> () )
    
    func getOrganization(oid: Int, onCompleted completition:@escaping (Result<Organization, Error>) -> () )
    func getOrganization(oids: [Int], onCompleted completition:@escaping (Result<[Organization], Error>) -> () )
    
    func getOrganizationSettings(oid: Int, onCompleted completition:@escaping (Result<OrganizationSettings, Error>) -> () )
    func getOrganizationSettings(oids: [Int], onCompleted completition:@escaping (Result<[OrganizationSettings], Error>) -> () )
    
    func getOrganizationWithSettings(oid: Int, onCompleted completition:@escaping (Result<OrganizationWithSettings, Error>) -> () )
    func getOrganizationWithSettings(oids: [Int], onCompleted completition:@escaping (Result<[OrganizationWithSettings], Error>) -> () )
}
