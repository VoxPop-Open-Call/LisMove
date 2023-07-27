//
//  NetworkError.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 15/07/21.
//

import Foundation

class NetworkError: Codable{
    
    let status: Int?
    let error: String?
    let message: String?
    
    
    enum CodingKeys: String, CodingKey {
        case status = "status"
        case error = "error"
        case message = "message"
    }
    
    
    public init(status: Int?, error: String?, message: String?){
        self.status = status
        self.error = error
        self.message = message
    }
    
    
}

