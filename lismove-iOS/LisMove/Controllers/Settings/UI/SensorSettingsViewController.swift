//
//  SensorSettingsViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/07/21.
//

import UIKit
import Toast_Swift

class SensorSettingsViewController: UIViewController {

    @IBOutlet weak var alertView: UIView!
    
    @IBOutlet weak var sensorCard: UIView!
    @IBOutlet weak var sensorName: UILabel!
    @IBOutlet weak var sensorUUID: UILabel!
    @IBOutlet weak var firmwareUUID: UILabel!
    
    @IBOutlet weak var wheelDiameter: UILabel!
    
    @IBOutlet weak var configButton: UIButton!
    
    
    @IBOutlet weak var stolenButton: UIButton!
    @IBOutlet weak var deleteButton: UIButton!
    @IBOutlet weak var checkUpdateButton: UIButton!
    
    
    
    
    //check sensor into DB
    let sensor = DBManager.sharedInstance.getLismoveDevice()
    
    //check sensor into DB
    let user = DBManager.sharedInstance.getCurrentUser()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        initView()
        
    }
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        
        if(sensor != nil){
            alertView.isHidden = true
            sensorCard.isHidden = false
            
            sensorName.text = sensor?.name
            sensorUUID.text = sensor?.uuid
            
            firmwareUUID.text = "Firmware v. " + (UserDefaults.standard.string(forKey: "firmwareV") ?? "")
            
            wheelDiameter.text = Sensor.getWheelDescription(type: UserDefaults.standard.integer(forKey: "wheelDiameter"))
            
        }else{
            alertView.isHidden = false
            sensorCard.isHidden = true
        }
        
        
        sensorCard.layer.cornerRadius = 8
        sensorCard.layer.shadowColor = UIColor.gray.cgColor
        sensorCard.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        sensorCard.layer.shadowRadius = 8.0
        sensorCard.layer.shadowOpacity = 0.7
        sensorCard.layer.borderWidth = 0.3
        sensorCard.layer.borderColor = UIColor.gray.cgColor
        sensorCard.layer.frame.inset(by: UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16))
        
        configButton.layer.cornerRadius = 16

        stolenButton.layer.cornerRadius = 16
        
        checkUpdateButton.layer.cornerRadius = 16
        checkUpdateButton.layer.borderColor = UIColor.gray.cgColor
        checkUpdateButton.layer.borderWidth = 0.3
        deleteButton.layer.cornerRadius = 16
        deleteButton.layer.borderColor = UIColor.gray.cgColor
        deleteButton.layer.borderWidth = 0.3
        
    }


    fileprivate func configSensor() {
        if(self.sensor == nil){
            
            let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
            let controller = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! UIPageViewController
            controller.modalPresentationStyle = .fullScreen
            self.present(controller, animated: true, completion: nil)
            
        }else{
            
            var title = ""
            var message = ""
            
            if(SessionManager.sharedInstance.sessionState?.state == SessionState.onGoing){
                
                title = "Attenzione: Stoppare la sessione?"
                message = self.sensor != nil ?  "Rieffettuerai la connessione con il sessione" : "Nessun sensore associato. Vuoi associarne uno?"
                
                //stop session & reset sensor
                SessionManager.sharedInstance.requestStop()
                //SessionManager.sharedInstance.sensorSDK.stopSession()
                
            }else{
                
                title = "Attenzione"
                message = "I dati del precedente sensore andranno persi. Vuoi rieffettuare la connessione?"
            }
            
            let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
            
            if let popoverPresentationController = alertController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            
            let cancel = UIAlertAction(title: "Annulla", style: .default, handler: nil)
            
            let confirm = UIAlertAction(title: "Conferma", style: .destructive, handler: {context in
                
                SessionManager.sharedInstance.sensorSDK.disconnectSensor(sensorID: self.sensor?.uuid)
                SessionManager.sharedInstance.sensorSDK.stopScan()
                
                //stop connection loop
                SessionManager.sharedInstance.sensorCompanionManager?.sensorCheckUpdateDaemon?.invalidate()
                SessionManager.sharedInstance.sensorCompanionManager?.sensorCheckUpdateDaemon = nil
                
                //delete sensor
                NetworkingManager.sharedInstance.deleteSensor(uuid: self.sensor!.uuid!, uid: self.user!.uid!, completion: {result in
                    
                    if result{
                        //delete from db
                        //TODO: FIX THIS
                        DBManager.sharedInstance.deleteDevice()
                        SessionManager.sharedInstance.sensorSDK.currentSensor = nil
                        
                        //show scan view controller
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                        let controller = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! UIPageViewController
                        controller.modalPresentationStyle = .fullScreen
                        self.present(controller, animated: true, completion: nil)
                        
                    }else{
                        
                        let alert = UIAlertController(title: "Errore", message:
                                                        "Impossibile dissociare il sensore. Riprova", preferredStyle: .alert)
                        if let popoverPresentationController = alert.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        
                        alert.addAction(UIAlertAction(title: "Conferma", style: .default, handler: nil))
                        self.present(alert, animated: true, completion: nil)
                    }
                })
                
            })
            
            alertController.addAction(cancel)
            alertController.addAction(confirm)
            
            self.present(alertController, animated: true, completion: nil)
            
            
        }
    }
    
    @IBAction func onSensorConfig(_ sender: Any) {
        
        
        configSensor()
        
    }
    
    
    @IBAction func stolenSensorTap(_ sender: Any) {
        
        //adivse user
        let alertController = UIAlertController(title: "Attenzione", message:
                "Inviare segnalazione di furto?", preferredStyle: .alert)
        if let popoverPresentationController = alertController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        let action2 =  UIAlertAction(title: "Annulla", style: .default, handler: nil)
        let action = UIAlertAction(title: "Conferma", style: .destructive, handler: { [self] _ in
     
            NetworkingManager.sharedInstance.stolenSensor(uuid: sensor!.uuid!, uid: user!.uid!, completion: {result in
                
                switch result {
                    case true:
                        
                        //set sensor stolen
                        do{
                            try DBManager.sharedInstance.getDB().safeWrite {
                                self.sensor?.stolen = true
                            }
                        }catch{
                            
                        }
                        
                        
                        //adivse user
                        let alertController = UIAlertController(title: "Successo", message:
                                "Abbiamo ricevuto la tua segnalazione", preferredStyle: .alert)
                        if let popoverPresentationController = alertController.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        
                        alertController.addAction(UIAlertAction(title: "ok", style: .default, handler: nil))
                        
                        self.present(alertController, animated: true, completion: nil)
                        
                case false:
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile inviare la segnalazione. Riprova"])
                        
                }
                
            })
            
        })
        alertController.addAction(action)
        alertController.addAction(action2)
        
        self.present(alertController, animated: true, completion: nil)
        

    }
    
    
    @IBAction func deleteSensor(_ sender: Any) {

        configSensor()
    }
    
    
    
    @IBAction func onSensorUpdate(_ sender: Any) {
        
        
        if(SensorUpdateManager.needsUpdate(hardwareVersion: SessionManager.sharedInstance.sensorCompanionManager?.hardwareVersion, softwareVersion: SessionManager.sharedInstance.sensorCompanionManager?.firmwareVersion)){
             
             UserDefaults.standard.set(true, forKey: "sensorUpdate")
             
            //error: you must update
             //adivse user
             let alertController = UIAlertController(title: "Attenzione", message:
                     "Nuova versione firmware disponibile per il tuo sensore. Aggiorna per proseguire", preferredStyle: .alert)
             if let popoverPresentationController = alertController.popoverPresentationController {
                 popoverPresentationController.sourceView = UIApplication.shared.topMostViewController()!.view
                 popoverPresentationController.sourceRect = CGRect(x: UIApplication.shared.topMostViewController()!.view.bounds.size.width / 2.0, y: UIApplication.shared.topMostViewController()!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
             }
             
             alertController.addAction(UIAlertAction(title: "Annulla", style: .destructive, handler: nil))
                                       
             alertController.addAction(UIAlertAction(title: "Conferma", style: .default, handler: {_ in
                
                 //open new controller for start update
                 let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                 let updateController = storyBoard.instantiateViewController(withIdentifier: "updateController") as! SensorUpdateViewController
                 updateController.modalPresentationStyle = .popover
                 if let popoverPresentationController = updateController.popoverPresentationController {
                     popoverPresentationController.sourceView = UIApplication.shared.topMostViewController()!.view
                     popoverPresentationController.sourceRect = CGRect(x: UIApplication.shared.topMostViewController()!.view.bounds.size.width / 2.0, y: UIApplication.shared.topMostViewController()!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                 }
                 UIApplication.shared.topMostViewController()!.present(updateController, animated: true, completion: nil)
                 
             }))
             

             
             UIApplication.shared.topMostViewController()!.present(alertController, animated: true, completion: nil)
                                       
        }else{
            
            self.view.makeToast("Nessun aggiornamento disponibile")
            
            UserDefaults.standard.set(false, forKey: "sensorUpdate")
        }
    }
    
    
}
