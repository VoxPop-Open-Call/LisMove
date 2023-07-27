//
//  AppDelegate+Injection.swift
//  LisMove
//
//
import Foundation
import Resolver

extension Resolver: ResolverRegistering {
    public static func registerAllServices() {
        registerBasicDependencies()
    }
}
