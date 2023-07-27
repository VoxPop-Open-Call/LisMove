//
//  SettingsDetailViewModel.swift
//  LisMove
//
//

import Foundation
class SettingsDetailViewModel {
    let SESSION_UPDATE_KEY = "sessionDelayUpdate"
    static let DEFAULT_SESSION_DELAY = "5.0"
    var currentDelay = DEFAULT_SESSION_DELAY
    func getActualSessionDelay() -> String {
        if(UserDefaults.standard.object(forKey: "sessionDelayUpdate") != nil){
            currentDelay = "\(UserDefaults.standard.double(forKey: SESSION_UPDATE_KEY))"
        }
    
        return currentDelay
        
    }
    
    func setSessionDelay(_ secondText: String?)-> String{
        let value = secondText ?? SettingsDetailViewModel.DEFAULT_SESSION_DELAY
        UserDefaults.standard.set(Double(value), forKey: SESSION_UPDATE_KEY)
        currentDelay = value
        return value
    }
}
