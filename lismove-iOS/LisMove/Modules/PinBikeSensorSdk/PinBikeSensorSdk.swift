//
//  LisMoveSensorSdk.swift
//  LisMoveSensorSdk
//
//

import Foundation
import CoreBluetooth

public class LisMoveSensorSDK{
    private lazy var LISMOVEPairingMagager = LisMovePairingManager()
    
    //private lazy var LISMOVESessionManager = LisMoveSessionManager()
    public var currentSensor: CadenceSensor? = nil{
        didSet{
             currentSensor?.sensorDelegate = sensorDelegate
        }
    }
    
    var sensorID: String?
    private var sensorDelegate: CadenceSensorDelegate? = nil
    
    public init(){}
    
    deinit{
        NotificationCenter.default.removeObserver(self)
    }
    
    public func getCentralManager() -> CBCentralManager{
        return LISMOVEPairingMagager.centralManager
    }
    
    public func connectToSensor(sensorID: String) {
        
        self.sensorID = sensorID
        
        guard let sensor = LISMOVEPairingMagager.retrieveSensorWithIdentifier(sensorID) else {
            return
        }

        
        
        LISMOVEPairingMagager.disconnectSensor(sensor)
        LISMOVEPairingMagager.connectToSensorSingleTask(sensor)
        
        
        //MARK: Sensor connected listener
        NotificationCenter.default.addObserver(self, selector: #selector(sensorIsConnected), name: NSNotification.Name(rawValue: "SENSOR_CONNECTED"), object: nil)

    }
    
    public func setCadenceSensorDelegate(_ delegate: CadenceSensorDelegate){
        self.sensorDelegate = delegate
        currentSensor?.sensorDelegate = sensorDelegate
    }
    

    
    @objc func sensorIsConnected(_ notification: Notification){
        let check = notification.userInfo as! [String: CadenceSensor]
        let sensor = check["sensor"]
        
        self.currentSensor = sensor
    }
    
    public func disconnectSensor(sensorID: String?){
        if (sensorID == nil) {
            return
        }
        
        guard let sensor = LISMOVEPairingMagager.retrieveSensorWithIdentifier(sensorID!) else {
            return
        }
        
        LISMOVEPairingMagager.disconnectSensor(sensor)
    }
   
    /*
    public func startSession(){
        //self.resetSensor()
        LISMOVESessionManager.startSession(sensor: currentSensor)
    }*/
    
    /*public func resumeSession(automaticPause: Bool){
        LISMOVESessionManager.resumeSession(sensor: currentSensor, automaticPause: automaticPause)
    }*/
    /*
    public func stopSession(){
        LISMOVESessionManager.stopSession(sensor: currentSensor)
    }*/
    
    /*
    public func pauseSession(automaticPause: Bool){
        LISMOVESessionManager.pauseSession(sensor: currentSensor, automaticPause: automaticPause)
    }*/
    
    
    /*public func resetSensor(){
        LISMOVESessionManager.resetSensor(sensor: currentSensor)
    }*/
    
    public func setPairingDelegate(delegate: LisMovePairingDelegate ){
        LISMOVEPairingMagager.delegate = delegate
    }

    
    public func startScan(){
        LISMOVEPairingMagager.startScan()
    }
    
    public func stopScan(){
        LISMOVEPairingMagager.stopScan()
    }
    
    
}
