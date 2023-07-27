//
//  AuthRepository.swift
//  LisMove
//
//

import Foundation

protocol AuthRepository{
    func getCurrentUser() -> String?
    func refreshUserToken(onCompletition listener: @escaping (String?, Error?)->())
    func getUserToken() -> String
    func logout()
}
