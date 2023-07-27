//
//  SensorUpdateCompleteViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 07/12/21.
//

import UIKit

class SensorUpdateCompleteViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()


    }
    

    @IBAction func dismissAction(_ sender: Any) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
        DashboardController.modalPresentationStyle = .fullScreen
    
        
        self.present(DashboardController, animated: true, completion: nil)
    }
    

}
