//
//  ThirdStepViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 22/05/21.
//

import UIKit
import LisMoveSensorSdk
import CoreBluetooth
import Bugsnag

class ThirdStepViewController: UIViewController, LisMovePairingDelegate {

    //data
    var sessionManager = SessionManager.sharedInstance
    //db
    var DB = DBManager.sharedInstance
    //networking
    var networking = NetworkingManager.sharedInstance
    
    //start association
    let startAssociation = Int64(Date().timeIntervalSince1970 * 1000)
    
    //lock device connection
    var lockConnection = false
    
    //sensor connection warning
    var sensorConnectionTimeoutDaemon: Timer?
    var sensorConnectionTimeout = 0

    override func viewDidLoad() {
        super.viewDidLoad()
        
        sessionManager.sensorSDK.setPairingDelegate(delegate: self)
        
        //reset sensor connection
        sessionManager.sensorSDK.stopScan()
        sessionManager.sensorSDK.startScan()
        
        //start countdown bugsnag
        if(self.sensorConnectionTimeoutDaemon == nil){
            self.sensorConnectionTimeoutDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(1), repeats: true) { timer in
                
                self.sensorConnectionTimeout+=1
                
                if(self.sensorConnectionTimeout == 10){
                    self.sensorConnectionTimeoutDaemon?.invalidate()
                    
                    
                    let exception = NSException(name:NSExceptionName(rawValue: "Sensor Association Warning"),
                                                reason:"Time > 10s",
                                                userInfo:nil)
                
                    Bugsnag.notify(exception)
                }
            }
        }
    }
    
    func onStateChanged(_ state: CBManagerState) {
        print(state)
        switch state {
        case .poweredOn:
            //sessionManager.scanManager.startScan()
            break
        case .unknown:
            showAlert(title: "Bluetooth disabilitato o non disponibile", message: "Attiva il bluetooth per connettere Lis move al sensore di velocità")
        case .resetting:
            showAlert(title: "Bluetooth disabilitato o non disponibile", message: "Attiva il bluetooth per connettere Lis move al sensore di velocità")
        case .unsupported:
            showAlert(title: "Bluetooth disabilitato o non disponibile", message: "Attiva il bluetooth per connettere Lis move al sensore di velocità")
        case .unauthorized:
            showAlert(title: "Permessi bluetooth disabilitati", message: "Assicurati di aver concesso il permesso per l'utilizzo del bluetooth")
        case .poweredOff:
            showAlert(title: "Bluetooth disabilitato o non disponibile", message: "Attiva il bluetooth per connettere Lis move al sensore di velocità")
        @unknown default:
            showAlert(title: "Bluetooth disabilitato o non disponibile", message: "Attiva il bluetooth per connettere Lis move al sensore di velocità")
        }
    }
    
    func showAlert(title: String, message: String){
        let alertController = UIAlertController(title: title, message:
                message, preferredStyle: .alert)
            if let popoverPresentationController = alertController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            alertController.addAction(UIAlertAction(title: "OK", style: .default))
            self.present(alertController, animated: true, completion: nil)
    }
    
    func onDeviceConnected(device: LisMoveDevice,sensor: CadenceSensor?) {
        
        if(!lockConnection){
            
            lockConnection = !lockConnection
            self.sensorConnectionTimeoutDaemon?.invalidate()
            self.sensorConnectionTimeout = 0
            
            //save sensor to server
            //get user
            let user = self.DB.getCurrentUser()
            let sensor = Sensor.sensorFromDevice(device: device, startAssociation: self.startAssociation)
            
            networking.saveSensor(sensor: sensor, uid: user!.uid!, completion: { result in
                switch result{
                case .success(let sensorData):
                    
                    //save sensor offline
                    self.DB.saveLismoveDevice(device: sensorData)
                    
                    //update current sensor
                    self.sessionManager.sensorCompanionManager?.checkConnections()

                    //open last tutorial
                    let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                    let fourthstep = storyBoard.instantiateViewController(withIdentifier: "fourthStepTutorial")
                    fourthstep.modalPresentationStyle = .fullScreen
                
                    
                    self.present(fourthstep, animated: true, completion: nil)
                        
                
                case .failure(let error):
                        
                        //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                        break
                }
            })
        }
        

        

    }
    
    func onError(error: Error) {
        showAlert(title: "Si è verificato un errore", message: error.localizedDescription)
    }
    
    func onDeviceNotFound() {
        showAlert(title: "Dispositivo non trovato", message: "Gira la ruota per accendere il dispositivo e ritenta")

    }
    
    func onDeviceDisconnected() {
        //
    }
    
    @IBAction func abortConnection(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
}
