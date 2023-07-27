//
//  AccountViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/04/21.
//

import UIKit
import Toast_Swift
import FirebaseAuth
import PhoneNumberKit
import CountryPickerView
import GooglePlaces

class AccountViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate{

    @IBOutlet weak var nameLabel: UITextField!
    @IBOutlet weak var surnameLabel: UITextField!
    @IBOutlet weak var nicknameLabel: UITextField!
    @IBOutlet weak var addressLabel: UITextField!
    @IBOutlet weak var phoneNumberLabel: UITextField!
    @IBOutlet weak var birthdayLabel: UITextField!
    let datePicker = UIDatePicker()
    @IBOutlet weak var sexLabel: UITextField!
    @IBOutlet weak var phonePrefixLabel: UITextField!
    let prefixPicker = CountryPickerView()
    //sex chose
    var sexList = ["Uomo", "Donna", "Altro"]
    
    @IBOutlet weak var confirmButton: UIButton!
    //db
    var DB = DBManager.sharedInstance
    var networking = NetworkingManager.sharedInstance
    var currentUser: LismoveUser?
    var updatableUser: LismoveUser?
    
    //type
    var isModifyProfileMode = false
    
    //address
    var selectedCoordinates: (Double, Double)?
    var parsedCity, parsedAddress, parsedStreetNumber: String?
    
    var selectedPlace: GMSPlace? {
        didSet {
            (parsedCity, parsedAddress, parsedStreetNumber) =  PlaceUtil.parseDataFromPlace(place: self.selectedPlace)
            updatableUser?.homeAddress = parsedAddress
            updatableUser?.homeNumber = parsedStreetNumber
            updatableUser?.cityLisMove = parsedCity
            updatableUser?.homeCity.value = CityRepository.getCityCode(byName: parsedCity ?? "")
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.hideKeyboardWhenTappedAround()
        
        //tableView.delegate = self
        //tableView.dataSource = self
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
    
        self.initUser()
        
        initView()
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    
    private func initUser(){
        self.currentUser = self.DB.getCurrentUser()
        if let currentUser = currentUser {
            self.updatableUser = RealmHelper.DetachedCopy(of: currentUser)
            self.updatableUser?.fixPhoneNumber()
        }
    }
    

    private func initView(){
        //add footer with bottom

        confirmButton.setTitle("Iscriviti", for: .normal)
        
        //init sex label dropdown
        createPickerView()
        dismissPickerView()
        //show date picker
        showDatePicker()
        
        setupPrefixPicker()
        
        //address tap action
        let tapAddress = UITapGestureRecognizer(target: self, action: #selector(self.handleAddressTap(_:)))
        self.addressLabel.isUserInteractionEnabled = true
        self.addressLabel.addGestureRecognizer(tapAddress)
            
        self.nameLabel.delegate = self
        self.surnameLabel.delegate = self
        self.nicknameLabel.delegate = self
        self.addressLabel.delegate = self
        self.nameLabel.text = self.updatableUser?.firstName
        self.surnameLabel.text = self.updatableUser?.lastName
        self.nicknameLabel.text = self.updatableUser?.username
        self.addressLabel.text = self.updatableUser?.getAddressLabel()
        self.phoneNumberLabel.text = self.updatableUser?.phoneNumber
        self.phonePrefixLabel.text = self.updatableUser?.phoneNumberPrefix
        
        let milisecond = self.updatableUser?.birthDate.value
        if(milisecond != nil) {
            let dateVar = Date.init(timeIntervalSince1970: TimeInterval(milisecond!)/1000)
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "dd/MM/yyyy"
            
            self.birthdayLabel.text = dateFormatter.string(from: dateVar)
            
            datePicker.setDate(Date.init(timeIntervalSince1970: TimeInterval(milisecond!)/1000), animated: true)
        }
        
        self.sexLabel.text = self.currentUser?.gender
        //check if user has data
        if(isModifyProfileMode){
            confirmButton.setTitle("Aggiorna", for: .normal)
        }
    }
    
    @objc func handleAddressTap(_ sender: UITapGestureRecognizer? = nil) {
        
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "GMSAutocompleteNavigation") as! UINavigationController
        let detailController = navController.topViewController as! GMSAutocompleteViewController
        
        if(self.selectedPlace != nil){
            detailController.address = "\(parsedAddress ?? ""), \(parsedStreetNumber ?? ""), \(parsedCity ?? "")"
            detailController.isAddressComplete = updatableUser?.isAddressComplete() ?? false
        }else{
            detailController.address = self.addressLabel.text
            detailController.isAddressComplete = updatableUser?.isAddressComplete() ?? false
        }

        
        
        if(self.selectedCoordinates != nil){
            detailController.addressLatLng = self.selectedCoordinates!
        }else{
            if(self.currentUser?.homeLatitude.value != nil || self.currentUser?.homeLongitude.value != nil){
                detailController.addressLatLng = (self.currentUser!.homeLatitude.value!, self.currentUser!.homeLongitude.value!)
            }
        }
        

        
        detailController.onDoneBlock = { result in
            
            if((result.1) != nil){
                self.selectedPlace = (result.1)
            }
 
            if((result.0) != nil){
                self.selectedCoordinates = (result.0)
            }
                
            self.addressLabel.text = self.updatableUser?.getAddressLabel()
            
            self.dismiss(animated: true, completion: nil)
            

        }
        
        present(navController, animated: true, completion: nil)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    func setupPrefixPicker(){
        let prefixGesture = UITapGestureRecognizer(target: self, action: #selector(showPrefixPicker))
        phonePrefixLabel.isUserInteractionEnabled = true
        phonePrefixLabel.addGestureRecognizer(prefixGesture)
        prefixPicker.delegate = self
        prefixPicker.showPhoneCodeInView = true
    }
    
    @objc func showPrefixPicker(){
        var phoneCode = phonePrefixLabel.text ?? ""
        phoneCode = phoneCode == "" ? "39" : phoneCode
        let phoneCodeWithPlus = "+\(phoneCode)"
        
        prefixPicker.setCountryByPhoneCode(phoneCodeWithPlus)
        prefixPicker.showCountriesList(from: self)
    }
    
    func showDatePicker(){
        //Formate Date
        
        datePicker.datePickerMode = .date

        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Conferma", style: .plain, target: self, action: #selector(donedatePicker));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Annulla", style: .plain, target: self, action: #selector(cancelDatePicker));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        
        birthdayLabel.inputAccessoryView = toolbar
        birthdayLabel.inputView = datePicker
        
        if #available(iOS 13.4, *) {
            datePicker.preferredDatePickerStyle = .wheels
        } else {
            // Fallback on earlier versions
        }


   }
    
   @objc func donedatePicker(){

        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        birthdayLabel.text = formatter.string(from: datePicker.date)
        self.view.endEditing(true)
    
   }

   @objc func cancelDatePicker(){
        self.view.endEditing(true)
    }
    
    @IBAction func onDoneClicked(_ sender: Any) {
        if(nameLabel.text!.isEmpty || surnameLabel.text!.isEmpty || birthdayLabel.text!.isEmpty || nicknameLabel.text!.isEmpty || addressLabel.text!.isEmpty ){
            
            let alert = UIAlertController(title: "Attenzione", message: "Completa tutti i campi con le tue informazioni", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = confirmButton.bounds
            }
            
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
            
            
        }else if(updatableUser?.isAddressComplete() != true){
            showBasicAlert(title: "Attenzione", description: "Inserisci un indirizzo di casa completo")
            return
        }else{
            
            //check nickname
            guard isNickNameValid(nicknameLabel.text!) else{
                showBasicAlert(title: "Attenzione", description: "Caratteri speciali non ammessi nel nickname")
                return
            }
            
            //check street number
            
            
            
            //apple login user
            if(!(updatableUser?.termsAccepted ?? false)){
                
                let alert = UIAlertController(title: "Attenzione", message: "Per proseguire è necessario accettare termini e condizioni", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: {context in
                    
                    //open terms controller
                    let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                    let navController = storyBoard.instantiateViewController(withIdentifier: "termsController") as! UINavigationController
                    navController.modalPresentationStyle = .popover
                    if let popoverPresentationController = navController.popoverPresentationController {
                        popoverPresentationController.sourceView = self.view
                        popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                    }

                    let detailController = navController.topViewController as! TermsViewController
                    detailController.isModifyProfileMode = true
                    
                    detailController.onDoneBlock = {
                        self.initUser()
                    }

                    self.present(navController, animated: true, completion: nil)
                    
                }))
                self.present(alert, animated: true, completion: nil)

            }else{
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "dd/MM/yyyy"
                let date = dateFormatter.date(from: self.birthdayLabel.text!)
                
                

                self.updatableUser?.firstName = self.nameLabel.text
                self.updatableUser?.lastName = self.surnameLabel.text
                self.updatableUser?.username = self.nicknameLabel.text
                self.updatableUser?.birthDate.value = Int64(date!.timeIntervalSince1970 * 1000)
                self.updatableUser?.gender = self.sexLabel.text
                self.updatableUser?.phoneNumber = self.phoneNumberLabel.text
                self.updatableUser?.phoneNumberPrefix = self.phonePrefixLabel.text
                
                if(self.selectedCoordinates != nil){
                    self.updatableUser?.homeLatitude.value = self.selectedCoordinates?.0
                    self.updatableUser?.homeLongitude.value = self.selectedCoordinates?.1
                }
                
                self.updatableUser?.signupCompleted = true
                
                updateUser()
            }
            

                   
        }

    }
    
   
    
    private func isNickNameValid(_ nickame: String) -> Bool{
        let regex = try! NSRegularExpression(pattern: "^[a-zA-Z0-9_-]+$", options: .caseInsensitive)
        return regex.matches(nickame)
    }

    
    
    private func updateUser(){
    
        //print("saving user with \(self.currentUser!.signupCompleted)")
        
        self.view.makeToast("Aggiornamento in corso")
        
        //1. save user to server
        networking.updateUser(user: self.updatableUser!){ result in
            switch result{
            case .success(_):
                
                self.DB.saveUser(user: self.updatableUser!)
                
                
                //check update or new user
                if(self.isModifyProfileMode){
                    self.dismiss(animated: true)
                }else{
                    //New profile
                    self.sendVerificationMailIfNotVerified()
                }
                    
                
            
            case .failure(let error):
                    
                    //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        }
    }
    
    private func sendVerificationMailIfNotVerified() {
        let authUser = Auth.auth().currentUser
        
        if authUser != nil {
            
            if(!authUser!.isEmailVerified){
                authUser!.sendEmailVerification(completion: { (error) in
                    // Notify the user that the mail has sent or couldn't because of an error.
                    if(error == nil){
                        let alert = UIAlertController(title: "Successo", message: "Ti è stava inviata un'email per confermare il tuo account.", preferredStyle: .alert)
                        if let popoverPresentationController = alert.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        
                        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
                            switch action.style{
                                case .default:
                                   
                                    let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                                    let ConfirmEmailViewController = storyBoard.instantiateViewController(withIdentifier: "ConfirmEmailViewController")
                                    ConfirmEmailViewController.modalPresentationStyle = .fullScreen
                                        
                                    self.present(ConfirmEmailViewController, animated: false, completion: nil)
                                    
                                    break
                            default:
                                break
                                
                                
                            }
                        }))
                        self.present(alert, animated: true, completion: nil)
                    }
                })
            }else{
                goToNewInitiative()
            }
            
        }else {
            // Either the user is not available, or the user is already verified.
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile continuare con la configurazione. Riavvia l'app"])
            
            
        }
    }
    
    func goToNewInitiative(){
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "newInitiativeNavigation") as! UINavigationController
        let detailController = navController.topViewController as! NewInitiativeViewController
        detailController.isFirstStart = true
        self.present(navController, animated: true, completion: nil)
    }
    
    @IBAction func openVehicleController(_ sender: Any) {
        //open c02 controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let vehicleController = storyBoard.instantiateViewController(withIdentifier: "vehicleBridgeController") as! Vehicle_BridgeViewController
        vehicleController.modalPresentationStyle = .fullScreen
        
        self.present(vehicleController, animated: true, completion: nil)
        
        NotificationCenter.default.addObserver(forName: NSNotification.Name("dismissSwiftUI"), object: nil, queue: nil) { (_) in
            vehicleController.dismiss(animated: true, completion: nil)
        }
    }
    

    @IBAction func onBackClicked(_ sender: Any) {
        LogHelper.log(message: "RestartAccountSetup", withTag: "AccountViewController")

        if(isModifyProfileMode){
            dismiss(animated: true)
        }else{
            restartAccountSetup()
        }
        
    }
    
    func restartAccountSetup(){
        let alert = UIAlertController(title: "Attenzione", message: "Dovrai effettuare nuovamente l'accesso/iscrizione", preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        alert.addAction(UIAlertAction(title: "Annulla", style: .destructive))
        alert.addAction(UIAlertAction(title: "Conferma", style: .default, handler: {_ in
            
            //clean user table
            self.DB.restartAccountFlow()
            
            //restart from splash screen
            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
            let splashViewController = storyBoard.instantiateViewController(withIdentifier: "SplashController") as! SplashViewController
            splashViewController.modalPresentationStyle = .fullScreen
            
            self.present(splashViewController, animated: false, completion: nil)
            
            
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return sexList.count
    
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return sexList[row]
        
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        
        sexLabel.text = self.sexList[row]

    }
    
    func createPickerView() {
        let pickerView = UIPickerView()
        pickerView.delegate = self
        sexLabel.inputView = pickerView
        
    }
    
    func dismissPickerView() {
        let toolBar = UIToolbar()
        toolBar.sizeToFit()
        let button = UIBarButtonItem(title: "Conferma", style: .plain, target: self, action: #selector(self.action))
        toolBar.setItems([button], animated: true)
        toolBar.isUserInteractionEnabled = true
        sexLabel.inputAccessoryView = toolBar
    }
    
    
    
    @objc func action() {
          view.endEditing(true)
    }

    
}

extension AccountViewController: CountryPickerViewDelegate{
    func countryPickerView(_ countryPickerView: CountryPickerView, didSelectCountry country: Country) {
        phonePrefixLabel.text = country.phoneCode.replacingOccurrences(of: "+", with: "")
    }
    func countryPickerView(_ countryPickerView: CountryPickerView, willShow viewController: CountryPickerViewController){
        viewController.navigationController?.navigationBar.tintColor = UIColor.white
    }


}
