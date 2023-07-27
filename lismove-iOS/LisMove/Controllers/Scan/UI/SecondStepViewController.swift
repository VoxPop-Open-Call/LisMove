//
//  SecondStepViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 22/05/21.
//

import UIKit
import Toast_Swift

class SecondStepViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {
    
 
    @IBOutlet weak var wheelDiameterLabel: UITextField!
    @IBOutlet weak var bikeTypeLabel: UITextField!
    
    
    //sex chose
    var diameterList = ["29''", "28'' (700mm)", "27.5'' (650mm)","27''", "26''", "24'' (600mm)","22'' (550mm)","20'' (500mm)","18'' (450mm)","16'' (400mm)", "14'' (350mm)", "12''"]

    var bikeType = ["Tradizionale (muscolare)", "Elettrica (assistita)"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //load diameter
        wheelDiameterLabel.insertText(Sensor.getWheelDescription(type: UserDefaults.standard.integer(forKey: "wheelDiameter")))
        
        //init sex label dropdown
        createPickerView()
        dismissPickerView()
        
        //init default set
        UserDefaults.standard.set(Sensor.getWheel(type: self.wheelDiameterLabel.text!), forKey: "wheelDiameter")
        UserDefaults.standard.set(self.bikeTypeLabel.text ?? BikeType.Tradizionale.rawValue, forKey: "bikeType")
        
        
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if(pickerView == self.wheelDiameterLabel.inputView){
            return diameterList.count
        }else{
            return bikeType.count
        }
    
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if(pickerView == self.wheelDiameterLabel.inputView){
            return diameterList[row]
        }else{
            return bikeType[row]
        }
        
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if(pickerView == self.wheelDiameterLabel.inputView){
            wheelDiameterLabel.text = self.diameterList[row]
        }else{
            bikeTypeLabel.text = self.bikeType[row]
        }

    }
    
    func createPickerView() {
        let pickerView = UIPickerView()
        pickerView.delegate = self
        wheelDiameterLabel.inputView = pickerView
        
        let pickerView2 = UIPickerView()
        pickerView2.delegate = self
        bikeTypeLabel.inputView = pickerView2
        
    }
    
    func dismissPickerView() {
        let toolBar = UIToolbar()
        toolBar.sizeToFit()
        let button = UIBarButtonItem(title: "Fatto", style: .plain, target: self, action: #selector(self.action))
        toolBar.setItems([button], animated: true)
        toolBar.isUserInteractionEnabled = true
        wheelDiameterLabel.inputAccessoryView = toolBar
        bikeTypeLabel.inputAccessoryView = toolBar
    }
    
    
    
    @objc func action() {
        
        view.endEditing(true)
        
        UserDefaults.standard.set(Sensor.getWheel(type: self.wheelDiameterLabel.text!), forKey: "wheelDiameter")
        UserDefaults.standard.set(self.bikeTypeLabel.text ?? BikeType.Tradizionale.rawValue, forKey: "bikeType")
        
        //SessionManager.sharedInstance.scanManager.currentSensor?.wheelCircunference = UInt32(Sensor.getWheel(type: self.wheelDiameterLabel.text!))
                                                                                                    
    }

    @IBAction func abortConnection(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
    
    
    
    @IBAction func nextTap(_ sender: Any) {
        
        //send next action
        NotificationCenter.default.post(name: Notification.Name("NEXT_SCREEN"), object: nil, userInfo: ["controller": self])
    }
    
}
