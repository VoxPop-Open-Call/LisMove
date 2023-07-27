//
//  APIConfiguration.swift
//  NetworkingClients
//
//  Created by Francesco Paolo Dellaquila on 30/03/2020.
//  Copyright © 2020 Nextome. All rights reserved.
//

import Foundation
import Alamofire

/*
    main endpoint protocol
        @method
        @path
        @parameters
 */
protocol APIConfiguration: URLRequestConvertible {
    var method: HTTPMethod { get }
    var path: String { get }
    var parameters: Parameters? { get }
}
