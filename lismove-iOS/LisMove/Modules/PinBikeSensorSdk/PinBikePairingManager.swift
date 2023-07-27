//
//  BluetoothPairingManager.swift
//  LisMoveSensorSdk
//
//

import Foundation
import CoreBluetooth

class LisMovePairingManager: NSObject{
    
    var delegate: LisMovePairingDelegate?
    
    public var centralManager: CBCentralManager!
    private let servicesToScan = [CBUUID(string: BTConstants.CadenceService)]
    var peripheral: CBPeripheral? = nil
    var deviceConnectionTimer: Timer? = nil
    var previouslyConnectedSensor: CadenceSensor? = nil
    
    init(delegate: LisMovePairingDelegate? = nil) {
        super.init()
        self.delegate = delegate
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    deinit {
        stopScan()
    }
    
    public func startScan(){
        centralManager.scanForPeripherals(withServices: servicesToScan, options: nil)
    }
    
    public func stopScan(){
        centralManager.stopScan()
    }
    
    private func saveDeviceToUserDefaults(uuid: String){
        UserDefaults.standard.set(uuid, forKey: BTConstants.SensorUserDefaultsKey)
        //UserDefaults.standard.synchronize()
    }
    
    func connectToSensorSingleTask(_ sensor: CadenceSensor){
        disconnectSensor(sensor)
        previouslyConnectedSensor = sensor
        self.connectToSensor()
    }
    
    func invalidateSensorConnectionSearch(){
        deviceConnectionTimer?.invalidate()
    }
    
    @objc func connectToSensor() {
        if let sensor = previouslyConnectedSensor{
            centralManager.connect(sensor.peripheral, options: nil)
        }
    }
        
    func disconnectSensor(_ sensor:CadenceSensor) {
        centralManager.cancelPeripheralConnection(sensor.peripheral)
    }
    
    func retrieveSensorWithIdentifier( _ identifier:String ) -> CadenceSensor? {
      guard let uuid  = UUID(uuidString: identifier) else  {
        return nil
      }
      guard let peripheral = centralManager.retrievePeripherals(withIdentifiers: [uuid]).first else {
        return nil
      }
      return CadenceSensor(peripheral: peripheral)
    }
    
    
}

extension LisMovePairingManager: CBCentralManagerDelegate {
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        delegate?.onStateChanged(central.state)
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        
        //invalidateSensorConnectionSearch()
        delegate?.onDeviceConnected(device: peripheral.toLisMoveDevice(), sensor: previouslyConnectedSensor)
        saveDeviceToUserDefaults(uuid: peripheral.identifier.uuidString)
        
        //start sensor service
        previouslyConnectedSensor?.initSensorStart()
        
        //MARK: SENSOR CONNECTED
        NotificationCenter.default.post(name: Notification.Name("SENSOR_CONNECTED"), object: nil, userInfo: ["sensor": previouslyConnectedSensor])
        NotificationCenter.default.post(name: Notification.Name("SENSOR_UPDATE"), object: nil)
        
    }
   
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        delegate?.onDeviceDisconnected()
        self.peripheral = nil
    }
    
    fileprivate func connectToSensor(_ peripheral: CBPeripheral) {
        centralManager.stopScan()
        self.peripheral = peripheral
        centralManager.connect(peripheral, options: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        if UserDefaults.standard.object(forKey: BTConstants.SensorUserDefaultsKey) != nil {
            
            if peripheral.identifier.uuidString == UserDefaults.standard.string(forKey: BTConstants.SensorUserDefaultsKey){
                connectToSensor(peripheral)
            }
            
        }else{
            
            if(BTConstants.devices.contains(peripheral.name ?? "")){
                connectToSensor(peripheral)
            }
            
        }
        
    }
}

public protocol LisMovePairingDelegate{
    
    func onStateChanged(_ state: CBManagerState)
    func onDeviceConnected(device: LisMoveDevice, sensor: CadenceSensor?)
    func onDeviceDisconnected()
    func onError(error: Error)
    func onDeviceNotFound()

}
