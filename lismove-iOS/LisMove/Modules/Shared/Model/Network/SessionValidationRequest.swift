//
//  SessionRequest.swift
//  LisMove
//
//

import Foundation
public struct SessionValidationRequest: Codable{
    public let id: String
    public let verificationRequired: Bool
    public var verificationRequiredNote: String?
    public var revisionType: [Int]
}
