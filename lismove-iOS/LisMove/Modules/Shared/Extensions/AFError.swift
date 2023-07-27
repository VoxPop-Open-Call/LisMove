//
//  AFError.swift
//  LisMove
//
//

import Foundation
import Alamofire
extension AFError {
    func isNetworkError() -> Bool{
        if let underlyingUrlError = (underlyingError as NSError?)?.code{
            return underlyingUrlError == URLError.notConnectedToInternet.rawValue
        }else{
            return false
        } 
    }
}
