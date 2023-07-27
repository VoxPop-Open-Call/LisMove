//
//  VehicleSettingsViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/09/21.
//

import Foundation
import UIKit

class VehicleSettingsViewController: UIViewController {

    @IBOutlet weak var alertView: UIView!
    
    @IBOutlet weak var vehicleCard: UIView!
    @IBOutlet weak var vehicleGeneration: UILabel!
    @IBOutlet weak var co2: UILabel!
    @IBOutlet weak var fuel: UILabel!
    @IBOutlet weak var brand: UILabel!
    
    @IBOutlet weak var configButton: UIButton!
    
    @IBOutlet weak var deleteButton: UIButton!
    
    
    
    // get current user
    let user = DBManager.sharedInstance.getCurrentUser()
    
    //user car
    var car: CarModification?
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        initView()
        syncUserCar()
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        self.alertView.isHidden = true
        self.vehicleCard.isHidden = true
        
        
        vehicleCard.layer.cornerRadius = 8
        vehicleCard.layer.shadowColor = UIColor.gray.cgColor
        vehicleCard.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        vehicleCard.layer.shadowRadius = 8.0
        vehicleCard.layer.shadowOpacity = 0.7
        vehicleCard.layer.borderWidth = 0.3
        vehicleCard.layer.borderColor = UIColor.gray.cgColor
        vehicleCard.layer.frame.inset(by: UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16))
        
        configButton.layer.cornerRadius = 16
        deleteButton.layer.cornerRadius = 16
        
    }
    
    private func syncUserCar(){
        
        self.view.makeToast("Caricamento dettagli veicolo")
        
        NetworkingManager.sharedInstance.getUserCar(uid: self.user!.uid!, completion: { vehicle in
            self.car = vehicle
            self.initVehicleInfo()
        })
    }
    
    private func initVehicleInfo(){
        if(car != nil){
            alertView.isHidden = true
            vehicleCard.isHidden = false
            
            vehicleGeneration.text = car?.generation?.name
            brand.text = "Brand: " + (car?.generation?.model?.brand?.name ?? "")
            co2.text = "Co2: " + (car?.co2 ?? "")
            fuel.text = "Carburante: " + (car?.fuel ?? "")
            
        }else{
            alertView.isHidden = false
            vehicleCard.isHidden = true
        }
    }


    @IBAction func onSensorConfig(_ sender: Any) {
        
        if(car != nil){
            
            self.openVehicleConfig()
            
        }else{
            let alertController = UIAlertController(title: "Attenzione", message:
                    "Rieffettuando la configurazione, cancellerai il veicolo attualmente salvato", preferredStyle: .alert)
            if let popoverPresentationController = alertController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            alertController.addAction(UIAlertAction(title: "Conferma", style: .destructive, handler: {context in
                self.openVehicleConfig()
            }))
            
            self.present(alertController, animated: true, completion: nil)
            
        }
        
    }
    
    private func openVehicleConfig(){
        //open c02 controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let vehicleController = storyBoard.instantiateViewController(withIdentifier: "vehicleBridgeController") as! Vehicle_BridgeViewController
        vehicleController.modalPresentationStyle = .fullScreen
        
        self.present(vehicleController, animated: true, completion: nil)
        
        NotificationCenter.default.addObserver(forName: NSNotification.Name("dismissSwiftUI"), object: nil, queue: nil) { (_) in
            vehicleController.dismiss(animated: true, completion: nil)
        }
    }
    
    @IBAction func deleteVehicle(_ sender: Any) {
        
        //adivse user
        let alertController = UIAlertController(title: "Attenzione", message:
                "Eliminare il veicolo?", preferredStyle: .alert)
        if let popoverPresentationController = alertController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        let action2 =  UIAlertAction(title: "Annulla", style: .default, handler: nil)
        let action = UIAlertAction(title: "ok", style: .destructive, handler: { [self] _ in
     
            //delete on server
            NetworkingManager.sharedInstance.deleteUserCar(uuid: self.user!.uid!, completion: { result in
                switch result{
                
                    case true:
                        //adivse user
                        let alertController = UIAlertController(title: "Successo", message:
                                "Veicolo eliminato con successo", preferredStyle: .alert)
                    
                        if let popoverPresentationController = alertController.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        
                        alertController.addAction(UIAlertAction(title: "ok", style: .default, handler: nil))
                        
                        self.present(alertController, animated: true, completion: nil)
                        
                        self.syncUserCar()
                        
                        
                    case false:
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile eliminare il veicolo"])
                }
            })
            
        })
    
        alertController.addAction(action)
        alertController.addAction(action2)
        
        
        self.present(alertController, animated: true, completion: nil)
        
        

    }
}
