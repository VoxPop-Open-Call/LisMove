//
//  SettingsDao.swift
//  LisMove
//
//

import Foundation
struct SettingsResponse: Codable{
    var id: Int? = -1
    var value: String? = ""
    var organizationSetting: String? = ""
    enum CodingKeys: String, CodingKey {
        case id = "id"
        case value = "value"
        case organizationSetting = "organizationSetting"
    }
}
