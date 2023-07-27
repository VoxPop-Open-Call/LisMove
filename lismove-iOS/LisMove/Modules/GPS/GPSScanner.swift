//
//  GPSScanner.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 04/05/21.
//

import Foundation
import SwiftLocation
import SwiftLog
import GoogleMaps


internal class GpsScanner{
    

    
    init(){}
    
    
    /*
     GPS subscriber to refresh user Coordinates
     */
    func getLocation() -> GPSLocationRequest{
        
        
        //init engine
        SwiftLocation.allowsBackgroundLocationUpdates = true
        
        
        SwiftLocation.requestAuthorization(.plist) { newStatus in
            print("New status \(newStatus.description)")
        }
        
        //force update last coordinates
        SwiftLocation.gpsLocation()
        
        return SwiftLocation.gpsLocationWith {
            // configure everything about your request
            $0.precise = .fullAccuracy
            $0.subscription = .continous // continous updated until you stop it
            $0.accuracy = .house
            //$0.minDistance = 50 // updated every x mts or more
            //$0.minTimeInterval = gpsDelay // updated each x seconds or more
            $0.activityType = .otherNavigation
        }
        
        
    }
    

}

