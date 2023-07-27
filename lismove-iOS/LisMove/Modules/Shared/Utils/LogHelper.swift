//
//  LogHelper.swift
//  LisMove
//
//

import Foundation

class LogHelper {
    
    static func log(message text: String, withTag tag: String = "pbFilter"){
        print("pbFilter 🔎: [\(tag)]  \(text)")
    }
    
    static func logError(message text: String, withTag tag: String = "pbFilter"){
        print("pbFilter ❌: [\(tag)]  \(text)")
    }
    
    
}
