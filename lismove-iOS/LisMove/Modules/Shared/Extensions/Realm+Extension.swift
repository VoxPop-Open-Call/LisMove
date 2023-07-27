//
//  Realm+Extension.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/02/22.
//

import Foundation
import RealmSwift

//convert realm collection to list
extension RealmCollection
{
  func toArray<T>() ->[T]
  {
    return self.compactMap{$0 as? T}
  }
}


//safe write for realm
extension Realm {
    public func safeWrite(_ block: (() throws -> Void)) throws {
        if isInWriteTransaction {
            try block()
        } else {
            try write(block)
        }
    }
}

//Detach object from realm: is necessary for network request or thread manipulation
protocol RealmListDetachable {

    func detached() -> Self
}

extension List: RealmListDetachable where Element: Object {

    func detached() -> List<Element> {
        let detached = self.detached
        let result = List<Element>()
        result.append(objectsIn: detached)
        return result
    }

}

@objc extension Object {

    public func detached() -> Self {
        let detached = type(of: self).init()
        for property in objectSchema.properties {
            guard
                property != objectSchema.primaryKeyProperty,
                let value = value(forKey: property.name)
            else { continue }
            if let detachable = value as? Object {
                detached.setValue(detachable.detached(), forKey: property.name)
            } else if let list = value as? RealmListDetachable {
                detached.setValue(list.detached(), forKey: property.name)
            } else {
                detached.setValue(value, forKey: property.name)
            }
        }
        return detached
    }
}

extension Sequence where Iterator.Element: Object {

    public var detached: [Element] {
        return self.map({ $0.detached() })
    }

}

