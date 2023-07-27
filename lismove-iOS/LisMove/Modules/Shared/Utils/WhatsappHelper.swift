//
//  WhatsappHelper.swift
//  LisMove
//
//
import Foundation
import UIKit

class WhatsappHelper{
    static let phone="393401642396"
    
    static func openWhatsappWithDefaultChat(){
        let urlWhats = "https://wa.me/\(phone)"

        if let urlString = urlWhats.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed){
            if let whatsappURL = NSURL(string: urlString) {
                UIApplication.shared.open(whatsappURL as URL, options: [:], completionHandler: nil)
            }
        }
    }
}
