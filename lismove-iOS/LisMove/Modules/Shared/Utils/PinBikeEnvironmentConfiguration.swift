//
//  LisMoveConstants.swift
//  LisMove
//
//

import Foundation
class LisMoveEnvironmentConfiguration{
    private static let ENVIRONMENT_NAME_KEY = "environmentName"
    private static let ENVIRONMENT_DEV_VALUE = "it.lismove.app.test"
    private static let ENVIRONMENT_PROD_VALUE = "it.lismove.app"
    
    private static let ENVIRONMENT_DEV_NAME = "DEV"
    private static let ENVIRONMENT_PROD_NAME = "PROD"
    
    private static let CURRENT_ENVIRONMENT = Bundle.main.infoDictionary?["CFBundleIdentifier"] as? String ?? ""

    static let IS_ENVIRONMENT_DEV = CURRENT_ENVIRONMENT == ENVIRONMENT_DEV_VALUE
    static let ENVIRONMENT_NAME = IS_ENVIRONMENT_DEV ? ENVIRONMENT_DEV_NAME : ENVIRONMENT_PROD_NAME
    
    private static let NETWORK_BASE_URL_PROD = "https://api.lismove.nextome-ext.com"
    private static let NETWORK_BASE_URL_DEV = "https://api.lismove-test.nextome-ext.com"
    
    static let NETWORK_BASE_URL = IS_ENVIRONMENT_DEV ? NETWORK_BASE_URL_DEV : NETWORK_BASE_URL_PROD
    
    
    private static let FIREBASE_REALTIME_URL_PROD = "https://lismove-1521450884928.firebaseio.com/"
    private static let FIREBASE_REALTIME_URL_DEV = "https://tester-nxt-default-rtdb.europe-west1.firebasedatabase.app/"
    
    static let FIREBASE_REALTIME_URL = IS_ENVIRONMENT_DEV  ? FIREBASE_REALTIME_URL_DEV : FIREBASE_REALTIME_URL_PROD
    
    static let IS_CHAT_ENABLED = IS_ENVIRONMENT_DEV ? false : true
    
    static let DB_NAME = IS_ENVIRONMENT_DEV ? "LismoveDev.realm" : "Lismove.realm"
    
    
}

