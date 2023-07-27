//
//  SettingsRepository.swift
//  LisMove
//
//

import Foundation
protocol SettingsRepository {
    func getSettings(onCompleted listener: @escaping (Result<LisMoveSettings, Error>)->())
}
