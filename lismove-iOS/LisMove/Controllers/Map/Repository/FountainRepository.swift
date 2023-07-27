//
//  DrinkingFountainRepository.swift
//  LisMove
//
//

import Foundation
protocol FountainRepository{
    func getActiveFountainList(onCompletition listener: @escaping ([Fountain]?, Error?)->())
    func addFountain(fountain: Fountain, onCompletition listener: @escaping (Error?)->())
    func deleteFountain(fountain: Fountain, uid: String, onCompletition listener: @escaping (Error?)->())
}
