//
//  SplashViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 17/04/21.
//

import UIKit

class SplashViewController: UIViewController {

    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var signupButton: UIButton!

    
    //db
    var DBManager = (UIApplication.shared.delegate as? AppDelegate)?.DB
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        initView()
        
    }
    
    
    private func initView(){
        loginButton.layer.cornerRadius = 16
        signupButton.layer.cornerRadius = 16
    }


    
}
