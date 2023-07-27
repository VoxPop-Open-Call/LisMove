//
//  FirstStepViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 10/09/21.
//

import Foundation
import UIKit
import CoreBluetooth

class FirstStepViewController: UIViewController  {
    
    var isiOSBluetoothOn = false
    var manager: CBCentralManager!
    
    @IBOutlet weak var startButton: UIButton!
    
    override func viewDidLoad() {
        manager = CBCentralManager(delegate: self, queue: nil)
    }

    
    @IBAction func abortConnection(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
    
    
    @IBAction func startTap(_ sender: Any) {
        
        //check bluetoooth
        if (!isiOSBluetoothOn){
            
            if(CBCentralManager().authorization != .allowedAlways){
                openAppOrSystemSettingsAlert(title: "Autorizzazioni negate", message: "I permessi Bluetooth per Lis Move sono attualmente negati. Concedili dalle impostazioni")
            }else{
                self.showBasicAlert(title: "Bluetooth Disattivato", description: "Per configurare il sensore è necessario attivare il Bluetooth")
            }
            
        }else{
            //send next action
            NotificationCenter.default.post(name: Notification.Name("NEXT_SCREEN"), object: nil, userInfo: ["controller": self])
        }
         
    }
    
    func openAppOrSystemSettingsAlert(title: String, message: String) {
        let alertController = UIAlertController (title: title, message: message, preferredStyle: .alert)
        let settingsAction = UIAlertAction(title: "Settings", style: .default) { (_) -> Void in
            guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else { return }
            if UIApplication.shared.canOpenURL(settingsUrl) {
                UIApplication.shared.open(settingsUrl, completionHandler: { (success) in
                    print("Settings opened: \(success)") // Prints true
                })
            }
        }
        alertController.addAction(settingsAction)
        let cancelAction = UIAlertAction(title: "Cancel", style: .default, handler: nil)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    

}

extension FirstStepViewController: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
           isiOSBluetoothOn = true
            break
        case .poweredOff:
           isiOSBluetoothOn = false
            break
        default:
            break
        }
    }
}
