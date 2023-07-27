//
//  OfflineDataRequest.swift
//  LisMove
//
//

import Foundation
public struct OfflineDataRequest: Codable{
    public let distance: Float?
    public let endRevs: Int?
    public let endTime: Int?
    public let sensor: String?
    public let startRevs: Int?
    public let startTime: Int?
    public let user: String?
}

