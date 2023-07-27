//
//  Vehicle_BridgeViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 11/08/21.
//

import UIKit
import SwiftUI

class Vehicle_BridgeViewController: UIViewController {

    @IBOutlet var containerView: UIView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view
        let childView = UIHostingController(rootView: VehicleOnBoardController())
        
        childView.view.window?.overrideUserInterfaceStyle = .light
        addChild(childView)
        childView.view.frame = containerView.frame
        view.addSubview(childView.view)
        childView.didMove(toParent: self)
        
        
    }
    

}
