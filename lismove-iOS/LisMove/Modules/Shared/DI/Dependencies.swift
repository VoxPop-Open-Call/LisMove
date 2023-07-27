//
//  Dependencies.swift
//  LisMove
//
//

import Foundation
import Resolver
extension Resolver {

    public static func registerBasicDependencies() {
        
        register { AuthRepositoryImpl() }
        .implements(AuthRepository.self).scope(.application) //scope is only for singleton
        
        register {UserRepositoryImpl()}
        .implements(UserRepository.self)
        
        register { PhoneRepositoryImpl() }
        .implements(PhoneRepository.self)
        
        register { FountainRepositoryImpl()}
        .implements(FountainRepository.self)

        register { SessionRepositoryImpl()}
        .implements(SessionRepository.self)
        
        register { SettingsRepositoryImpl()}
        .implements(SettingsRepository.self)

        register { InitiativeRepositoryImpl()}
        .implements(InitiativeRepository.self)

    }
    
}
