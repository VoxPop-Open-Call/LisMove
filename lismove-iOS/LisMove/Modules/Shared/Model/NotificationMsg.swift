//
//  Notification.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/10/21.
//

import Foundation

public class NotificationMsg: Codable {
    
    var message: Int?
    var body: String?
    var createdDate: Int64?
    var imageURL: String?
    var organization: Int?
    var read: Bool?
    var title: String?

    public init(message: Int?, body: String?, createdDate: Int64?, imageURL: String?, organization: Int?, read: Bool?, title: String?) {
        self.message = message
        self.body = body
        self.createdDate = createdDate
        self.imageURL = imageURL
        self.organization = organization
        self.read = read
        self.title = title
    }
    
    enum CodingKeys: String, CodingKey {
        case message = "message"
        case body = "body"
        case createdDate = "createdDate"
        case imageURL = "imageURL"
        case organization = "organization"
        case read = "read"
        case title = "title"
    }
}
