//
//  InitiativeHelper.swift
//  LisMove
//
//

import Foundation
import UIKit

class InitiativeHelper{
    
    static func openAlertDialog(title: String,
                                message: String,
                                link: URL?,
                                viewController: UIViewController){
        
        let linkFixedMessage = "Per consultare i periodi di validità di ogni singola iniziativa, navigare nell'app, menù "
        let linkText = " 'Altro -> Gestione Iniziative'  \n\n"
    
        let attributedString = NSMutableAttributedString()

        //attributes
        let attributes = [NSAttributedString.Key.underlineStyle: NSUnderlineStyle.thick.rawValue, NSAttributedString.Key.foregroundColor: UIColor.systemRed] as [NSAttributedString.Key : Any]

        //custom string
        let attributedQuote = NSAttributedString(string: linkText, attributes: attributes)
        
        attributedString.append(NSAttributedString(string: linkFixedMessage))
        attributedString.append(attributedQuote)
        attributedString.append(NSAttributedString(string: message))
    
        
        //ALERT VIEW
        let alert = UIAlertController(title: title, message: "", preferredStyle: .alert)
        
        
        alert.setValue(attributedString, forKey: "attributedMessage")
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = viewController.view
            popoverPresentationController.sourceRect =  CGRect(x: viewController.view.bounds.size.width / 2.0, y: viewController.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        alert.addAction(UIAlertAction(title:link != nil ? "Visita sito" : "Ok", style: .default, handler: { action in
            if let link = link{
                UIApplication.shared.open(link)
            }
        

        }))
        if(link != nil){
            alert.addAction(UIAlertAction(title: "Annulla", style: .cancel, handler: nil))
        }
        viewController.present(alert, animated: true, completion: nil)
    }
}
