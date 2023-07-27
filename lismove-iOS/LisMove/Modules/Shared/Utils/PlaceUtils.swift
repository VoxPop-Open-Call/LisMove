//
//  PlaceUtils.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 11/01/22.
//

import Foundation
import GooglePlaces

class PlaceUtil{
    
    /*
     parse google place data
     */
    static func parseDataFromPlace(place: GMSPlace?) -> (String?,String?,String?){
        var city = place?.addressComponents?.first(where: { $0.type == "locality" })?.name
        
        if(city == nil){
            city = place?.addressComponents?.first(where: { $0.type == "administrative_area_level_3" })?.name
        }
        let address = place?.addressComponents?.first(where: { $0.type == "route" })?.name
        var streetNumber = place?.addressComponents?.first(where: { $0.type == "street_number" })?.name
        
        
        return (city, address, streetNumber)
        
    }
    
}
