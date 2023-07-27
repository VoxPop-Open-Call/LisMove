//
//  DateFilterViewController.swift
//  LisMove
//
//

import UIKit

class DateFilterViewController: UIViewController, UITextFieldDelegate {

    
    @IBOutlet weak var startDate: UITextField!
    @IBOutlet weak var endDate: UITextField!
    @IBOutlet weak var filterButton: UIButton!
    
    let dateFormatterView = DateFormatter()
    
    var startDateSelected: Date?
    var endDateSelected: Date?
    
    let datePicker = UIDatePicker()
    
    public var sessionListViewModel: SessionListViewModel?
    
    var onDoneBlock : ((Date?, Date?) -> Void)?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        

        dateFormatterView.dateFormat = "dd-MM-yyyy"
        
        initView()
        
    }
    
    
    private func initView(){

        startDate.text = dateFormatterView.string(from: startDateSelected ?? Date())
        endDate.text = dateFormatterView.string(from: endDateSelected ?? Date())
        
        initStartDatePicker()
        initEndDatePicker()
        
    }
    
    
    func initStartDatePicker(){
        //Formate Date
        datePicker.datePickerMode = .date

        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Conferma", style: .plain, target: self, action: #selector(donedatePickerStart));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Annulla", style: .plain, target: self, action: #selector(cancelDatePicker));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        
        startDate.inputAccessoryView = toolbar
        startDate.inputView = datePicker
        
        if #available(iOS 13.4, *) {
            datePicker.preferredDatePickerStyle = .wheels
        } else {
            // Fallback on earlier versions
        }
   }
    
    func initEndDatePicker(){
        //Formate Date
        datePicker.datePickerMode = .date

        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Conferma", style: .plain, target: self, action: #selector(donedatePickerEnd));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Annulla", style: .plain, target: self, action: #selector(cancelDatePicker));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        
        endDate.inputAccessoryView = toolbar
        endDate.inputView = datePicker
        
        if #available(iOS 13.4, *) {
            datePicker.preferredDatePickerStyle = .wheels
        } else {
            // Fallback on earlier versions
        }
   }
    
    @objc func donedatePickerStart(){

         startDate.text = dateFormatterView.string(from: datePicker.date)
         startDateSelected = datePicker.date
        
         self.view.endEditing(true)
     
    }
    
    @objc func donedatePickerEnd(){

         endDate.text = dateFormatterView.string(from: datePicker.date)
         endDateSelected = datePicker.date
        
         self.view.endEditing(true)
     
    }

    @objc func cancelDatePicker(){
         self.view.endEditing(true)
     }
    
    @IBAction func applyFilters(_ sender: Any) {
        
        
        if(endDateSelected! < startDateSelected!){
            
            self.showBasicAlert(title: "Attenzione", description: "La data di fine non può essere antecedente a quella di inizio ")
    
        }else{
            
            self.dismiss(animated: true, completion: {
                self.sessionListViewModel?.delegate?.onFilterDateComplete(startDate: self.startDateSelected!, endDate: self.endDateSelected!)
            })
        }
    
        
    }
    

}
