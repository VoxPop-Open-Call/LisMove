//
//  PhoneRepositoryImpl.swift
//  LisMove
//
//

import Foundation
class PhoneRepositoryImpl: PhoneRepository{
    func getAppVersion() -> String{
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String  ?? ""

        let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String  ?? ""
        
        return "\(version) (\(build))"
    }
}
