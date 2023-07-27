//
//  RealmHelper.swift
//  LisMove
//
//

import Foundation
class RealmHelper {
    //Used to expose generic
    static func DetachedCopy<T:Codable>(of object:T) -> T?{
       do{
           let json = try JSONEncoder().encode(object)
           return try JSONDecoder().decode(T.self, from: json)
       }
       catch let error{
           print(error)
           return nil
       }
    }
}
