//
//  SensorUpdateViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 16/10/21.
//

import UIKit
import LisMoveSensorSdk
import CoreBluetooth
import RxBluetoothKit
import RxSwift
import iOSDFULibrary
import Toast_Swift
import Bugsnag


class SensorUpdateViewController: UIViewController, LoggerDelegate, DFUServiceDelegate, DFUProgressDelegate {
    let FIRMWARE_K2_FILENAME = "k2_last"
    let FIRMWARE_K3_FILENAME = "k3_last"
    
    @IBOutlet weak var progressBar: UIProgressView!
    @IBOutlet weak var progressLabel: UILabel!
    
    let DEVICE_OTA_NAMES = ["_OTA", "BK463U", "BK463"]

    var firmwareUrl: URL? = nil;
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        //init alwayson
        UIApplication.shared.isIdleTimerDisabled = true
        
        updateLabelOnUi(text: "Preparazione aggiornamento...")
        showProgressUpdateOnUi(percentage: 0)
        
        let sensor = SessionManager.sharedInstance.sensorSDK.currentSensor
        
        //SensorUpdateManager.needsUpdate(hardwareVersion: sensor?.hardwareVersion, softwareVersion: sensor?.firmwareVersion)
        
        let sensorHardware = SensorUpdateManager.getSensorType(hardwareVersion: sensor?.hardwareVersion)
        if (sensorHardware == SensorUpdateManager.LismoveSensorHardware.K3) {
            firmwareUrl = Bundle.main.url(forResource: FIRMWARE_K3_FILENAME, withExtension: "zip")
        } else if (sensorHardware == SensorUpdateManager.LismoveSensorHardware.K2) {
            firmwareUrl = Bundle.main.url(forResource: FIRMWARE_K2_FILENAME, withExtension: "zip")
        } else {
            print("Device not recognized")
            showMessageOnUi(message: "Dispositivo non riconosciuto.")
            closeUpdateView()
            return
        }
        
        if (firmwareUrl == nil){
            let message = "Nuova versione firmware non trovata."
            print("Firmware file not found")
            reportError(message: message)
            showMessageOnUi(message: message)
            closeUpdateView()
            return
        }
        
        setDfuMode(sensor: sensor)
    }
    
    func closeUpdateView() {
        //disale alwayson
        UIApplication.shared.isIdleTimerDisabled = false
        
        UserDefaults.standard.set(false, forKey: "sensorUpdate")
        
        //end update controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let sensorUpdateComplete = storyBoard.instantiateViewController(withIdentifier: "sensorUpdateComplete")
        sensorUpdateComplete.modalPresentationStyle = .fullScreen
    
        
        self.present(sensorUpdateComplete, animated: true, completion: nil)
        
    }
    
    func showMessageOnUi(message: String) {
        self.view.makeToast(message)
    }
    
    func updateLabelOnUi(text: String) {
        progressLabel.text = text
    }
    
    func showProgressUpdateOnUi(percentage: Float) {
        let progress = percentage/100
        progressBar.setProgress(progress, animated: true)
    }
    
    
    func setDfuMode(sensor: CadenceSensor?) {
        updateLabelOnUi(text: "Invio comando di aggioramento...")
        
        let dfuModeCommandBytes: [UInt8] = [0xA2, 0x04, 0x01, 0xA7]
        let dfuModeCommandData = Data(dfuModeCommandBytes)
        let characteristic = sensor!.controlCharacteristic
        
        sensor!.peripheral.writeValue(dfuModeCommandData, for: characteristic!, type: .withoutResponse)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.updateLabelOnUi(text: "Connessione al dispositivo...")
            SessionManager.sharedInstance.sensorCompanionManager?.alwaysTryReconnectingToSensor = false
            SessionManager.sharedInstance.sensorSDK.stopScan()
            SessionManager.sharedInstance.sensorSDK.disconnectSensor(sensorID: sensor?.peripheral.identifier.uuidString)
         }

        
        // Wait for sensor to reboot in DFU mode
        DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
            self.discoverDfuDevices()
         }
    }
    
    func discoverDfuDevices() {
        let centralManager = CentralManager(queue: .main)
        centralManager.observeState()
             .startWith(centralManager.state)
             .filter { $0 == .poweredOn }
             .flatMap { cM in centralManager.scanForPeripherals(withServices: nil) }
             .filter({ scannedPeriph in
                 var nameMatched = false
                 self.DEVICE_OTA_NAMES.forEach { name in
                     let periphName = scannedPeriph.peripheral.name
                     print("Found " + (periphName ?? "No Name"))
                     
                     if (nameMatched == false) {
                         nameMatched = (periphName ?? "").contains(name)
                     }
                 }
                 
                 return nameMatched
             })
             .take(1)
             .subscribe(onNext: { ScannedPeripheral in
                 let dfuPeripheral = ScannedPeripheral.peripheral
                 
                 print("Selecter peripheral for DFU: " + (dfuPeripheral.name ?? "NO NAME"))
                 
                 self.startDfu(dfuPeriperal: dfuPeripheral.peripheral)
             }, onError: { Error in
                 // report Error
                 print("Scan error")
             }, onCompleted: {
                 // report Completed
                 print("Scan Completed")
             }, onDisposed: {
                 print("Disposed")
             })
    }
    
    func startDfu(dfuPeriperal: CBPeripheral) {
        let selectedFirmware = try DFUFirmware(urlToZipFile: firmwareUrl!, type: .softdeviceBootloaderApplication)

        let initiator = DFUServiceInitiator().with(firmware: selectedFirmware!)

        initiator.logger = self
        initiator.delegate = self
        initiator.progressDelegate = self
        
        updateLabelOnUi(text: "Installazione aggiornamento...")

        let controller = initiator.start(target: dfuPeriperal)
    }
    
    func dfuStateDidChange(to state: DFUState) {
        print("dfuStateDidChange to " + state.description())
        if (state == DFUState.completed) {
            SessionManager.sharedInstance.sensorCompanionManager?.alwaysTryReconnectingToSensor = true
            closeUpdateView()
        } else if (state == DFUState.aborted) {
            reportError(message: "DfuState aborted")
            showMessageOnUi(message: "Aggiornamento fallito, riprova più tardi")
            closeUpdateView()
        }
    }
    
    func dfuError(_ error: DFUError, didOccurWithMessage message: String) {
        print("Dfu error" + message)
        reportError(message: message)
    }
    
    func dfuProgressDidChange(for part: Int, outOf totalParts: Int, to progress: Int, currentSpeedBytesPerSecond: Double, avgSpeedBytesPerSecond: Double) {
        print("DfuProgressDidChange: " + String(progress) + "%")
        showProgressUpdateOnUi(percentage: Float(progress))
    }
    
    func logWith(_ level: LogLevel, message: String) {
        print(message)
    }
    
    func reportError(message: String) {
        let user = DBManager.sharedInstance.getCurrentUser()

        let exception = NSException(name:NSExceptionName(rawValue: "Sensor Update Exception"),
                                    reason:message,
                                    userInfo: ["userId": user?.uid ?? "NA"])
    
        Bugsnag.notify(exception)
    }
}
