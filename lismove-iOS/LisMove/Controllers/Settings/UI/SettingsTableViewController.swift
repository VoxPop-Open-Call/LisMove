//
//  SettingsTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/06/21.
//

import UIKit
import Resolver

class SettingsTableViewController: UITableViewController {
    let infoSegue = "showInfoAndCondition"
    
    @IBOutlet weak var lismoveVersionLabel: UILabel!
    @Injected var phoneRepository: PhoneRepository
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        lismoveVersionLabel.text = "Lis Move v. " +  phoneRepository.getAppVersion()
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 7
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        if(!LisMoveEnvironmentConfiguration.IS_ENVIRONMENT_DEV){
            if(indexPath.row == 5){
                return 0
            }
        }
        
        if(indexPath.row == 2){
            return 0
        }else{
            return 72
        }
    }
    
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if(indexPath.row == 1){
            
            let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "SensorSettingNavigationController") as! UINavigationController
            
            
            present(navController, animated: true, completion: nil)
            
        }else if(indexPath.row == 2){
            
            openCO2SwiftUiController()

        }else if(indexPath.row == 3){
            //open profile controller
            let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
            let profileController = storyBoard.instantiateViewController(withIdentifier: "ProfileViewController") as! ProfileTableViewController
            profileController.modalPresentationStyle = .popover
            if let popoverPresentationController = profileController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }

            
            self.present(profileController, animated: true, completion: nil)

        }else if(indexPath.row == 4){
            
            //open help controller
            let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
            let HelpViewController = storyBoard.instantiateViewController(withIdentifier: "HelpViewController") as! UINavigationController
            HelpViewController.modalPresentationStyle = .popover
            if let popoverPresentationController = HelpViewController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }

            
            self.present(HelpViewController, animated: true, completion: nil)
            

        }else if(indexPath.row == 5){
            
            let storyBoard = UIStoryboard(name: "Dashboard", bundle: nil)
            let settingsDetailViewController = storyBoard.instantiateViewController(identifier: "SettingsDetailViewController")
            settingsDetailViewController.modalPresentationStyle = .popover
            if let popoverPresentationController = settingsDetailViewController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }

                
            self.present(settingsDetailViewController, animated: true, completion: nil)
            
           
        }else if(indexPath.row == 6){
            self.performSegue(withIdentifier: infoSegue, sender: self)
        }
        
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    
    private func openCO2SwiftUiController(){
    
        //open c02 controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let vehicleController = storyBoard.instantiateViewController(withIdentifier: "vehicleBridgeController") as! Vehicle_BridgeViewController
        vehicleController.modalPresentationStyle = .fullScreen
        
        self.present(vehicleController, animated: true, completion: nil)
        
        NotificationCenter.default.addObserver(forName: NSNotification.Name("dismissSwiftUI"), object: nil, queue: nil) { (_) in
            vehicleController.dismiss(animated: true, completion: nil)
        }
            
    }
    
}
