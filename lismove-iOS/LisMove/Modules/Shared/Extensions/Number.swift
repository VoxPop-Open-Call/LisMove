//
//  Number.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 24/11/21.
//

import Foundation



extension Double {
    /// Rounds the double to decimal places value
    func rounded(toPlaces places:Int) -> Double {
        let divisor = pow(10.0, Double(places))
        return (self * divisor).rounded() / divisor
    }
    
    func getRoundedString()->String{
        let doubleRounded = rounded(toPlaces: 2)
        return String(format: "%.2f", doubleRounded).replacingOccurrences(of: ".", with: ",")
    }
    func getIntString()->String{
        let doubleRounded = rounded(toPlaces: 2)
        return String(format: "%.0f", doubleRounded)
    }
}
