//
//  FourthStepViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/09/21.
//

import Foundation
import UIKit

class FourthStepViewController: UIViewController {
    
    
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    
    @IBAction func endTap(_ sender: Any) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
        DashboardController.modalPresentationStyle = .fullScreen
    
        
        self.present(DashboardController, animated: true, completion: nil)
    }
    
    
}
