//
//  SensorConnectionManager.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 09/09/21.
//

import Foundation
import UIKit
import LisMoveSensorSdk
import CoreBluetooth
import SwiftLocation
import Resolver

public class SensorCompanionManager: NSObject{
    
    //MARK: Services
    public var sensorCheckUpdateDaemon: Timer?

    
    //MARK: Other
    var dbSensor = DBManager.sharedInstance.getLismoveDevice()
    var bluetoothStatus = false
    var gpsStatus = true
    
    var firmwareVersion: String?
    var hardwareVersion: String?
    var firmwareChecked = false
    var hardwareChecked = false
    var batteryChecked = false
    
    var sensorMessageConnectedLock = false
    var sensorMessageDisconnectedLock = false
    
    var alwaysTryReconnectingToSensor = true
    
    @Injected
    var sessionRepository: SessionRepository
    
    public override init(){
        
        super.init()
        
        //init delegate
        SessionManager.sharedInstance.sensorSDK.setPairingDelegate(delegate: self)
    }
    
    deinit{
        NotificationCenter.default.removeObserver(self)
    }
    
    public func initSensorConnection(){
        
        //check device
        if(dbSensor == nil){
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                
                self.dismissAnyAlertControllerIfPresent()
                
                //open pairing controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                let scanViewController = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! ScanViewController
                scanViewController.modalPresentationStyle = .fullScreen
            
                UIApplication.shared.topMostViewController()?.present(scanViewController, animated: true, completion: nil)
            }
            
        }else{
            
            //brute connect to sensor
            SessionManager.sharedInstance.sensorSDK.startScan()
            
            if let uuid = self.dbSensor!.uuid{
                SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: uuid)
            }

        
            NotificationCenter.default.addObserver(self, selector: #selector(firmwareVersionReceived), name: NSNotification.Name(rawValue: "FIRMWARE_READ"), object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(hardwareVersionReceived), name: NSNotification.Name(rawValue: "HARDWARE_READ"), object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(batteryLevelReceived), name: NSNotification.Name(rawValue: "BATTERY_READ"), object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(offlineSessionReceived), name: NSNotification.Name(rawValue: "OFFLINE_SESSION_READ"), object: nil)
            
            //periodically check connections status
            self.checkConnections()
        }

    }
    
    
    func checkConnections(){
        
        if(self.sensorCheckUpdateDaemon == nil){
            self.sensorCheckUpdateDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(30), repeats: true) { timer in
                self.dbSensor = DBManager.sharedInstance.getLismoveDevice()
                
                if (self.alwaysTryReconnectingToSensor) {
                    //MARK: 1 periodically check sensor is connected
                    if(SessionManager.sharedInstance.sensorSDK.currentSensor == nil){
                        
                        //reset firmware cehck
                        self.firmwareChecked = false
                        self.batteryChecked = false
                        
                        //change mode
                        SessionManager.sharedInstance.isGpsMode = true
                        
                        SessionManager.sharedInstance.sensorSDK.startScan()
                        
                        if let sensor = self.dbSensor {
                            if let uuid = sensor.uuid {
                                SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: uuid)
                            }
                        }
                        
                        
                        
                        //advise stream
                        NotificationCenter.default.post(name: Notification.Name("SENSOR_UPDATE"), object: nil, userInfo: ["sensor": false])
                        
                    }else{
                        
                        //change mode
                        SessionManager.sharedInstance.isGpsMode = false
                        
                        SessionManager.sharedInstance.sensorSDK.stopScan()
                        
                        
                        //advise stream
                        NotificationCenter.default.post(name: Notification.Name("SENSOR_UPDATE"), object: nil, userInfo: ["sensor": true])
                    }
                }
                
                
                //MARK: 2. check gps auth
                if(SessionManager.sharedInstance.sessionState?.state == SessionState.onGoing){
                    if(SwiftLocation.authorizationStatus == .denied || SwiftLocation.authorizationStatus == .restricted || SwiftLocation.authorizationStatus == .notDetermined){
                        SessionManager.sharedInstance.pauseSession()
                        
                        self.gpsStatus = false
                        UIApplication.shared.topMostViewController()?.view.makeToast("Abilita il Gps per riprendere la sessione")
                        
                    }else{
                        self.gpsStatus = true
                    }
                }
            }
            RunLoop.current.add(sensorCheckUpdateDaemon!, forMode: .common)
        }
    }

}



extension SensorCompanionManager{
    
    //dsmiss all presented view controller to prevent bad view constraints
    func dismissAnyAlertControllerIfPresent() {
        guard let window :UIWindow = UIApplication.shared.keyWindow , var topVC = window.rootViewController?.presentedViewController else {return}
        while topVC.presentedViewController != nil  {
            topVC = topVC.presentedViewController!
        }
        if topVC.isKind(of: UIAlertController.self) {
            topVC.dismiss(animated: false, completion: nil)
        }
    }
    
    
    //bluetooth is disabled -> switch to gps mode
    private func showBluetoothBanner(state: Bool){
        
        switch state {
        case true:
            
            let controller = UIApplication.shared.topMostViewController()
            let alert = UIAlertController(title: "Attenzione", message: "Riattiva il Bluetooth per connetterti al sensore.", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = controller!.view
                popoverPresentationController.sourceRect = CGRect(x: controller!.view.bounds.size.width / 2.0, y: controller!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil ))
            controller!.present(alert, animated: true, completion: nil)
            
        case false:
            break
            
        }
    }
}


extension SensorCompanionManager: LisMovePairingDelegate{
    
    public func onStateChanged(_ state: CBManagerState) {
        switch state {
        case .poweredOn:
            LogHelper.log(message: "stateChangedOn", withTag: "SensorCompanionManager")
            if(self.dbSensor != nil){
                SessionManager.sharedInstance.sensorSDK.startScan()
                
                
                if let sensor = self.dbSensor {
                    if let uuid = sensor.uuid {
                        SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: uuid)
                    }
                }
                
                
            }
            
            bluetoothStatus = true
            SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] bluetooth status poweredOn ")
        case .unknown,
             .resetting,
             .unsupported,
             .unauthorized,
             .poweredOff:
            LogHelper.log(message: "stateChangedOff", withTag: "SensorCompanionManager")

            bluetoothStatus = false

            UIApplication.shared.topMostViewController()?.view.hideToast()
            UIApplication.shared.topMostViewController()?.view.makeToast("Nessun Sensore rilevato")
            
            //advise user
            showBluetoothBanner(state: true)
            
            //update sessionManager
            SessionManager.sharedInstance.isGpsMode = true
            
            if let sensor = self.dbSensor {
                if let uuid = sensor.uuid {
                    SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: uuid)
                }
            }
            SessionManager.sharedInstance.sensorSDK.stopScan()
            SessionManager.sharedInstance.sensorSDK.currentSensor = nil
            SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] bluetooth status off \(state.rawValue) ")
            
        @unknown default:
            LogHelper.log(message: "default", withTag: "SensorCompanionManager")
            //reset current sensor
            if let sensor = self.dbSensor {
                if let uuid = sensor.uuid {
                    SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: uuid)
                }
            }

            SessionManager.sharedInstance.sensorSDK.stopScan()
            SessionManager.sharedInstance.sensorSDK.currentSensor = nil

            SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] bluetooth status unknown \(state.rawValue) ")
            showBluetoothBanner(state: true)
            
        }
    }
    
    public func onDeviceConnected(device: LisMoveDevice, sensor: CadenceSensor?) {
        
        SessionManager.sharedInstance.sensorSDK.currentSensor = sensor
        sensorMessageDisconnectedLock = false
        SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] onDeviceConnected ")
        if(!sensorMessageConnectedLock){
            UIApplication.shared.topMostViewController()?.view.hideToast()
            UIApplication.shared.topMostViewController()?.view.makeToast("Sensore \(device.name) Connesso")
        }
        
        sensorMessageConnectedLock = true
        
        /*if(SessionManager.sharedInstance.sessionState?.state == SessionDataManager.SessionState.onGoing){
            SessionManager.sharedInstance.scanManager.resetSensor()
        }*/

    }
    
    public func onDeviceDisconnected() {

        SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] onDeviceDisconnected ")
        SessionManager.sharedInstance.sensorSDK.currentSensor = nil
        sensorMessageConnectedLock = false
        
        if(!sensorMessageDisconnectedLock){
            UIApplication.shared.topMostViewController()?.view.hideToast()
            UIApplication.shared.topMostViewController()?.view.makeToast("Nessun Sensore rilevato")
        }
        
        sensorMessageDisconnectedLock = true
        
    }
    
    public func onError(error: Error) {
        SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] onError \(error.localizedDescription) ")
        //
    }
    
    public func onDeviceNotFound() {
        //
        SessionManager.sharedInstance.addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "[SensorCompanionManager] onDeviceNotFound ")
    }
}

extension SensorCompanionManager{
    
    

    /*
     1) save first value of sensor battery
     */
    @objc func batteryLevelReceived(_ notification: Notification){
        let check = notification.userInfo as! [String: Any]
        let batteryLevel = check["battery"]! as! Int
        let sensorInterface = check["sensor"]! as! CadenceSensor
        
        
        if(self.batteryChecked){
            return
        }else{
            self.batteryChecked = true
        }
        
        
        //advise user
        if(batteryLevel < 60){
            
            let alertController = UIAlertController(title: "Attenzione", message:
                    "La batteria del sensore è scarica. Sostituiscila al più presto", preferredStyle: .alert)
            if let popoverPresentationController = alertController.popoverPresentationController {
                popoverPresentationController.sourceView = UIApplication.shared.topMostViewController()!.view
                popoverPresentationController.sourceRect = CGRect(x: UIApplication.shared.topMostViewController()!.view.bounds.size.width / 2.0, y: UIApplication.shared.topMostViewController()!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            alertController.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
            
            UIApplication.shared.topMostViewController()!.present(alertController, animated: true, completion: nil)
        }
        
    }
    
    
    
    /*
     1) use version to check firmware
     2) use sensoriInterface to manage bluetooth command and state for sensor
     */

    @objc func firmwareVersionReceived(_ notification: Notification){
        
        let check = notification.userInfo as! [String: Any]
        
        self.firmwareVersion = check["version"]! as! String
        UserDefaults.standard.set(self.firmwareVersion, forKey: "firmwareV")
        
        let sensorInterface = check["sensor"]! as! CadenceSensor
        
        //plus control to prevent advise user loop
        if self.firmwareChecked {
            return
        }else{
            self.firmwareChecked = true
        }
        
    
        checkSensorUpdate()
    }
    
    
    @objc func hardwareVersionReceived(_ notification: Notification){
        
        let check = notification.userInfo as! [String: Any]
        
        self.hardwareVersion = check["version"]! as! String
        UserDefaults.standard.set(self.hardwareVersion, forKey: "hardwareV")
        
        let sensorInterface = check["sensor"]! as! CadenceSensor
        
        //plus control to prevent advise user loop
        if self.hardwareChecked {
            return
        }else{
            self.hardwareChecked = true
        }
        
    
        checkSensorUpdate()
    }
    
    @objc func offlineSessionReceived(_ notification: Notification){
        let notificationPayload = notification.userInfo as! [String: Any]

        let offlineSessions = notificationPayload["sessions"]! as? Array<LisMoveSensorHistoryElement> ?? []
        if(!offlineSessions.isEmpty){
            do {
                try sessionRepository.computeDistanceAndSendOfflineSession(sessions: offlineSessions)
            } catch {
                print("error")
            }
        }
    }
    
    
    
    private func checkSensorUpdate(){
        if(self.firmwareVersion != nil && self.hardwareVersion != nil){
            if(SensorUpdateManager.needsUpdate(hardwareVersion: self.hardwareVersion, softwareVersion: self.firmwareVersion)){
                 
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
                
                UserDefaults.standard.set(false, forKey: "sensorUpdate")
            }
        }
    }
    
}
