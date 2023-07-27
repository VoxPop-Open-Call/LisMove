//
//  CadenceSensor.swift
//  LismoveSensorSkdTestApp
//
//


import Foundation
import CoreBluetooth

/*
// Bluetooth  "Cycling Speed and Cadence"
https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.cycling_speed_and_cadence.xml

Service Cycling Speed and Cadence. Characteristic [2A5B]  // Measurement
Service Cycling Speed and Cadence. Characteristic [2A5C]  // Supported Features
Service Cycling Speed and Cadence. Characteristic [2A5D]  // Sensor location
Service Cycling Speed and Cadence. Characteristic [2A55]  // Control Point

*/


public protocol CadenceSensorDelegate {
  
    func errorDiscoveringSensorInformation(_ error:NSError)
    //func sensorReady()
  //func sensorUpdatedValues(speedInMetersPerSecond speed:Double?, cadenceInRpm cadence:Double?, distanceInMeters distance:Double?, wheelDiff: UInt32?, cumulativeWheel: UInt32?, wheelTimeDiff: Double?, battery: Int?, firmwareV: String?)
    func sensorUpdatedPrimitive(primitive: CadencePrimitive)
}

public class CadenceSensor: NSObject {
    
    
  public let peripheral:CBPeripheral
  var sensorDelegate:CadenceSensorDelegate?
  var measurementCharasteristic:CBCharacteristic?
  var batteryCharasteristic: CBCharacteristic?
  public var controlCharacteristic: CBCharacteristic?
  public var k3NotifyCharacteristic: CBCharacteristic?
  var lastMeasurement:Measurement?
  public var wheelCircunference:UInt32
  
    var collectedOfflineValues = Array<LisMoveSensorHistoryElement>(){
    didSet {
        if(collectedOfflineValues.count == 3){
            //MARK: BATTERY STREAM
            NotificationCenter.default.post(name: Notification.Name("OFFLINE_SESSION_READ"), object: nil, userInfo: ["sessions": collectedOfflineValues])
        }
    }
}
  public var batteryLevel: Int? {
    didSet {
        
        //MARK: BATTERY STREAM
        NotificationCenter.default.post(name: Notification.Name("BATTERY_READ"), object: nil, userInfo: ["battery": batteryLevel!, "sensor": self])
        
    }
  }
    
  public var firmwareVersion: String? {
      didSet {
          print("Firmware Version is \(firmwareVersion)")
          //MARK: Firmware STREAM
          NotificationCenter.default.post(name: Notification.Name("FIRMWARE_READ"), object: nil, userInfo: ["version": firmwareVersion!, "sensor": self])
      }
  }
    
    public var hardwareVersion: String? {
        didSet {
            print("Hardware Version is \(hardwareVersion)")
            NotificationCenter.default.post(name: Notification.Name("HARDWARE_READ"), object: nil, userInfo: ["version": hardwareVersion!, "sensor": self])
        }
    }
    
    

  
  public init(peripheral:CBPeripheral, wheel:UInt32=BTConstants.DefaultWheelSize) {
    self.peripheral = peripheral

    if(UserDefaults.standard.object(forKey: "wheelDiameter") != nil){
        wheelCircunference = UInt32(UserDefaults.standard.integer(forKey: "wheelDiameter"))
        
    }else{
        
        wheelCircunference = wheel
        UserDefaults.standard.setValue(wheel, forKey: "wheelDiameter")
    }

  }
  
    
    
  func initSensorStart(){
    self.peripheral.discoverServices(nil)
    self.peripheral.delegate = self

    self.start()
  }
    
  func start() {
      if let measurementCharasteristic = measurementCharasteristic {
        peripheral.setNotifyValue(true, for: measurementCharasteristic)
      }
      
      if let batteryCharasteristic = batteryCharasteristic {
        peripheral.setNotifyValue(true, for: batteryCharasteristic)
      }
  }
  
  
  func stop() {
    if let measurementCharasteristic = measurementCharasteristic {
      peripheral.setNotifyValue(false, for: measurementCharasteristic)
    }
    
    if let batteryCharasteristic = batteryCharasteristic {
      peripheral.setNotifyValue(false, for: batteryCharasteristic)
    }
    
  }
  
  func handleValueData( _ data:Data ) {
    
      //let measurement = Measurement(data: data, wheelSize: wheelCircunference)
      //print("\(measurement)")
      
      let cadencePrimitive = CadencePrimitive(data: data)
      
    
      //let values = measurement.valuesForPreviousMeasurement(lastMeasurement)
      //lastMeasurement = measurement
    
    //sensorDelegate?.sensorUpdatedValues(speedInMetersPerSecond: values?.speedInMetersPerSecond, cadenceInRpm: values?.cadenceinRPM, distanceInMeters: values?.distanceinMeters, wheelDiff: values?.wheelDiff, cumulativeWheel: measurement.cumulativeWheel, wheelTimeDiff: values?.wheelTimeDiff, battery: self.batteryLevel, firmwareV: self.firmwareVersion)
      sensorDelegate?.sensorUpdatedPrimitive(primitive: cadencePrimitive)
      
      
  }
    
    
}



extension CadenceSensor : CBPeripheralDelegate {
  
  
    public func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        
        guard error == nil else {
          sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: 0, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("Error receiving measurements updates", comment:"")]))
          
          return
        }
        print("notification status changed for [\(characteristic.uuid)]...")
        
  }
  

    
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
    
        print("Updated [\(characteristic.uuid)]...")
    
        guard error == nil  , let data = characteristic.value  else {
          return
        }
        
        if(characteristic.uuid.uuidString == BTConstants.batteryUUID){
            let test = data.hexEncodedString()
            self.batteryLevel = Int(test, radix: 16)!
        }else if(characteristic.uuid.uuidString == BTConstants.firmwareUUID){
            self.firmwareVersion = String(decoding: data, as: UTF8.self)
        }else if (characteristic.uuid.uuidString == BTConstants.hardwareUUID) {
            self.hardwareVersion = String(decoding: data, as: UTF8.self)
        } else if (characteristic.uuid.uuidString == BTConstants.k3NotifyServiceUUID){
            // Offline values received
            decodeAndSendOfflineValues(data: data)
        } else if (characteristic.uuid.uuidString == BTConstants.CSCMeasurementUUID) {
            handleValueData(data)
        }
    }
  
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {

        //cadence service
        guard error == nil  else {
          sensorDelegate?.errorDiscoveringSensorInformation(error! as NSError)
          return
        }
        // Find the cadence service
        guard let cadenceService =  peripheral.services?.filter({ (service) -> Bool in
          return service.uuid == CBUUID(string: BTConstants.CadenceService)
        }).first else {
          
          sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: NSNotFound, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("Cadence service not found for this peripheral", comment:"")]))
          return
        }
        // Discover the cadence service characteristics
        peripheral.discoverCharacteristics(nil, for:cadenceService )
            
         
        print("Cadence service discovered")
            
            
        //cadence battery service
        guard error == nil  else {
            sensorDelegate?.errorDiscoveringSensorInformation(error! as NSError)
            return
        }
        // Find the cadence service
        guard let batteryService =  peripheral.services?.filter({ (service) -> Bool in
            return service.uuid == CBUUID(string: BTConstants.CadenceBattery)
        }).first else {
              
            sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: NSNotFound, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("Cadence service not found for this peripheral", comment:"")]))
            return
        }
        // Discover the cadence service characteristics
        peripheral.discoverCharacteristics(nil, for:batteryService )
                
             
        print("Cadence Battery service discovered")
            
            
        //cadence firmwaew service
        guard error == nil  else {
            sensorDelegate?.errorDiscoveringSensorInformation(error! as NSError)
            return
        }
        // Find the cadence service
        guard let firmwareService =  peripheral.services?.filter({ (service) -> Bool in
            return service.uuid == CBUUID(string: BTConstants.firmwareService)
        }).first else {
              
            sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: NSNotFound, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("Cadence service not found for this peripheral", comment:"")]))
            return
        }
        // Discover the cadence service characteristics
        peripheral.discoverCharacteristics(nil, for:firmwareService )
        
        // Find the control service
        guard let controlService =  peripheral.services?.filter({ (service) -> Bool in
            return service.uuid == CBUUID(string: BTConstants.controlServiceUUID)
        }).first else {
              
            sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: NSNotFound, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("Control service not found for this peripheral", comment:"")]))
            return
        }
             
        peripheral.discoverCharacteristics(nil, for: controlService)

        print("Firmware services discovered")
    
  }
  
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    
        guard let characteristics = service.characteristics else {
          sensorDelegate?.errorDiscoveringSensorInformation(NSError(domain: CBErrorDomain, code: NSNotFound, userInfo: [NSLocalizedDescriptionKey:NSLocalizedString("No characteristics found for the cadence service", comment:"")]))
          return
          
        }
        
        print("Received characteristics");
        
        // Enable notifications for the measurement characteristic
        for characteristic in characteristics {
          
          print("Service \(service.uuid). Characteristic [\(characteristic.uuid)]")
          
            //CADENCE Service discovered
          if characteristic.uuid == CBUUID(string: BTConstants.CSCMeasurementUUID) {
            
            print("Found measurement characteristic. Subscribing...")
            peripheral.setNotifyValue(true, for: characteristic)
            measurementCharasteristic  = characteristic
            
            //sensorDelegate?.sensorReady()
            
          }
            
        
          //Battery Service discovered
          if characteristic.uuid == CBUUID(string: BTConstants.batteryUUID){
            if(characteristic.value != nil){
                batteryCharasteristic = characteristic
                let test = characteristic.value!.hexEncodedString()
                self.batteryLevel = Int(test, radix: 16)!
            }
            peripheral.setNotifyValue(true, for: characteristic)
          }
            
          //Firmware Service discovered
          if characteristic.uuid == CBUUID(string: BTConstants.firmwareUUID){
                peripheral.readValue(for: characteristic)
          }
            
            // Hardware version discovered
            
            if characteristic.uuid == CBUUID(string: BTConstants.hardwareUUID) {
                peripheral.readValue(for: characteristic)
            }
            
          // Control Service
          if characteristic.uuid == CBUUID(string: BTConstants.controlCharUUID) {
              controlCharacteristic = characteristic
          }
            
          // K3 notify service (offline sessions)
            if characteristic.uuid == CBUUID(string: BTConstants.k3NotifyServiceUUID) {
                k3NotifyCharacteristic = characteristic
                // Get offline sessions
                peripheral.setNotifyValue(true, for: characteristic)
            }
        }
        
        getOfflineSessionsIfSupported(peripheral: peripheral)
    }
    
    func getOfflineSessionsIfSupported(peripheral: CBPeripheral?) {
        if (peripheral != nil && controlCharacteristic != nil && k3NotifyCharacteristic != nil) {
            
            // Update UTC time
            let timeCommand = Data(getTimestampSetCommand())
            peripheral!.writeValue(timeCommand, for: controlCharacteristic!, type: .withoutResponse)
            
            // Request history values
            let requestHistoryCommand = Data(getRequestHistoryCommand())
            peripheral!.writeValue(requestHistoryCommand, for: controlCharacteristic!, type: .withoutResponse)
        }
    }
    
    func getTimestampSetCommand() -> [UInt8] {
        let timeInSeconds = Int (Date().timeIntervalSince1970)
        
        let timeInBytes = bytes(of: timeInSeconds, to: UInt8.self, droppingZeros: false)
        
        let preamble: [UInt8] = [0xBB, 0x08, 0x07]
        let end: [UInt8] = [0xAA]
        
        var timestampCommand: [UInt8] = []
        
        timestampCommand.append(contentsOf: preamble)
        timestampCommand.append(timeInBytes[4])
        timestampCommand.append(timeInBytes[5])
        timestampCommand.append(timeInBytes[6])
        timestampCommand.append(timeInBytes[7])
        timestampCommand.append(contentsOf: end)
        
        print(timestampCommand)
        return timestampCommand
    }
    
    func getRequestHistoryCommand() -> [UInt8] {
        return [0xBB, 0x04, 0x08, 0xAA]
    }
    
    
    func decodeAndSendOfflineValues(data: Data?) {
        if (data == nil) {
            return
        }
        
        // Check response size
        if (data?.count != 15) {
            return
        }
        
        let receivedBytes = [UInt8](data!)
        
        
        // check preamble
        if (receivedBytes[0] != 0xBD && receivedBytes[1] != 0x0E) {
            return
        }
        
        
        // Preamble is correct, start parsing
        // values are in little endian, swich endianness before parsing
        let startLapLittleEndianRaw = Array(receivedBytes[2...3].reversed())
        let startLap = Int(fromByteArray(startLapLittleEndianRaw, UInt16.self))
        
        if (startLap == 0) {
            return
        }
        
        let stopLapLittleEndianRaw = Array(receivedBytes[4...5].reversed())
        let stopLap = Int(fromByteArray(stopLapLittleEndianRaw, UInt16.self))
        
                                           
        let startTimeLittleEndianRaw = Array(receivedBytes[6...9].reversed())
        let startTime = Int(fromByteArray(startTimeLittleEndianRaw, UInt32.self))
        
        let endTimeLittleEndianRaw = Array(receivedBytes[10...13].reversed())
        let endTime = Int(fromByteArray(endTimeLittleEndianRaw, UInt32.self))
        
        let startTimeInMillis = startTime * 1000
        let endTimeInMillis = endTime * 1000
        
        
        let parsedValue = LisMoveSensorHistoryElement(startLap: startLap, stopLap: stopLap, startUtc: startTimeInMillis, stopUtc: endTimeInMillis)
        
        collectedOfflineValues.append(parsedValue)
    
    }
    
    func fromByteArray<T>(_ value: [UInt8], _: T.Type) -> T {
            return value.withUnsafeBufferPointer {
                $0.baseAddress!.withMemoryRebound(to: T.self, capacity: 1) {
                    $0.pointee
                }
            }
        }
    
    func bytes<U: FixedWidthInteger,V: FixedWidthInteger>(
        of value    : U,
        to type     : V.Type,
        droppingZeros: Bool
        ) -> [V]{

        let sizeInput = MemoryLayout<U>.size
        let sizeOutput = MemoryLayout<V>.size

        precondition(sizeInput >= sizeOutput, "The input memory size should be greater than the output memory size")

        var value = value
        let a =  withUnsafePointer(to: &value, {
            $0.withMemoryRebound(
                to: V.self,
                capacity: sizeInput,
                {
                    Array(UnsafeBufferPointer(start: $0, count: sizeInput/sizeOutput))
            })
        })

        let lastNonZeroIndex =
            (droppingZeros ? a.lastIndex { $0 != 0 } : a.indices.last) ?? a.startIndex

        return Array(a[...lastNonZeroIndex].reversed())
    }
}


extension Data {
    func hexEncodedString() -> String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}
