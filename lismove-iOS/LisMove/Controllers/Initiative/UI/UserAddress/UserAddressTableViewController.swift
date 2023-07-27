//
//  NewInitiativeTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/07/21.
//

import UIKit
import SwiftLog
import RealmSwift
import GooglePlaces
import GoogleMaps
import SwiftUI
import CountryPickerView

class UserAddressTableViewController: UIViewController, UITableViewDelegate,  UITableViewDataSource {


    @IBOutlet weak var workAddressHeigth: NSLayoutConstraint!
    
    //MARK: UI
    @IBOutlet weak var termsLabel: UILabel!
    @IBOutlet weak var addWorkButton: UIButton!
    @IBOutlet weak var addWorkLabel: UILabel!
    
    @IBOutlet weak var workAddressTableView: UITableView!
    @IBOutlet weak var customFieldTableView: UITableView!
    
    @IBOutlet weak var workArea: UIView!
    @IBOutlet weak var workName: UITextField!
    
    //user address
    @IBOutlet weak var homeAddress: UITextField!
    @IBOutlet weak var homeArrow: UIImageView!
    
    //user phone
    @IBOutlet weak var phoneLabel: UITextField!
    @IBOutlet weak var phonePrefixLabel: UITextField!
    
    
    //user iban
    @IBOutlet weak var ibanLabelTitle: UILabel!
    @IBOutlet weak var ibanLabel: UITextField!
    
    //work address
    @IBOutlet weak var workAddress: UITextField!
    @IBOutlet weak var workArrow: UIImageView!

    
    @IBOutlet weak var saveButton: UIButton!
    
    
    //MARK: Data
    var currentUser: LismoveUser?
    var updatableUser: LismoveUser?
    
    public var selectedEnrollment: Enrollment?
    public var selectedOrganizationWithSettings: OrganizationWithSettings?
    public var selectedOrganization: Organization?
    public var isEnrollmentNew: Bool = false
    
    public var customFieldList: [CustomField] = []
    public var customFieldListValue: [CustomFieldValues] = []
    
    public var enrollmentType = UserAddressTableViewCell.CellType.Normal
    public var onNewInitiativeAdded: ()->() = {}
    let prefixPicker = CountryPickerView()

    var organizationRelatedWorkAddress: [WorkAddress]{
        get{
            return updatableUser?.workAddresses.detached.filter({$0.organization.value == self.selectedOrganization?.id}) ?? []
        }
    }
    let termsString = "termini e condizioni"
    let regulationString = "regolamento dell'iniziativa"

    var termsAndRegulationString: String{
        get{
            let termsHeader = (selectedEnrollment?.isClosed() ?? false) ? "Visualizza" : "Cliccando su salva accetti"
            if self.selectedOrganization?.regulation != nil && self.selectedOrganization?.termsConditions != nil{
                return  "\(termsHeader) i termini e condizioni e il regolamento dell'iniziativa"
            }else if (self.selectedOrganization?.regulation) != nil{
                return "\(termsHeader) il regolamento dell'iniziativa"
            }else if (self.selectedOrganization?.termsConditions) != nil{
                return "\(termsHeader) i termini e condizioni"
            }else{
                return ""
            }
        }
    }
    
    //autocomplete
    var isHomeAutocomplete = false
    
    //selectedPlace
    var selectedPlaceHome: GMSPlace?{
        didSet{
            let (parsedCity, parsedAddress, parsedStreetNumber) =  PlaceUtil.parseDataFromPlace(place: self.selectedPlaceHome)
            updatableUser?.homeAddress = parsedAddress
            updatableUser?.homeNumber = parsedStreetNumber
            updatableUser?.cityLisMove = parsedCity
            updatableUser?.homeCity.value = CityRepository.getCityCode(byName: parsedCity ?? "")
        }
    }
    
    var selectedPlaceHomeCoordinates: (Double, Double)?

    var selectedPlaceWork: GMSPlace?
    var selectedPlaceWorkCoordinates: (Double, Double)?


    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        initUpdatableUser()

        initView()
        loadCustomFields()

    }
    
    private func initUpdatableUser(){
        self.currentUser = DBManager.sharedInstance.getCurrentUser()
        if let currentUser = currentUser {
            self.updatableUser = RealmHelper.DetachedCopy(of: currentUser)
            self.updatableUser?.fixPhoneNumber()
        }
    }
    
    private func initView(){
        
        self.hideKeyboardWhenTappedAround()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        self.saveButton.layer.cornerRadius = 16
        
    
        //load session data
        workAddressTableView.dataSource = self
        workAddressTableView.delegate = self
        
        customFieldTableView.dataSource = self
        customFieldTableView.delegate = self
        
        //add tap gesture to labels
        let tapHome = UITapGestureRecognizer(target: self, action: #selector(self.autocompleteTapHome(_:)))
        let tapHome2 = UITapGestureRecognizer(target: self, action: #selector(self.autocompleteTapHome(_:)))
        homeAddress.addGestureRecognizer(tapHome)
        homeAddress.isUserInteractionEnabled = true
        homeArrow.addGestureRecognizer(tapHome2)
        homeArrow.isUserInteractionEnabled = true
        
        let tapWork = UITapGestureRecognizer(target: self, action: #selector(self.autocompleteTapWork(_:)))
        let tapWork2 = UITapGestureRecognizer(target: self, action: #selector(self.autocompleteTapWork(_:)))
        workAddress.addGestureRecognizer(tapWork)
        workAddress.isUserInteractionEnabled = true
        workArrow.addGestureRecognizer(tapWork2)
        workArrow.isUserInteractionEnabled = true
        

        //check type
        if(self.enrollmentType == UserAddressTableViewCell.CellType.Normal){
            self.addWorkButton.isHidden = true
            self.addWorkLabel.isHidden = true
            self.workAddressTableView.isHidden = true
            self.workArea.isHidden = false
            
            setupPAWorkAddress()

        }else{
            
            self.workArea.isHidden = true
            self.workAddressHeigth.constant = 0
            
            if(self.updatableUser!.workAddresses.count > 0){
                self.workAddressTableView.layoutIfNeeded()
                self.workAddressHeigth.constant = 250
            }
            
            //check organization
            self.checkOrganizationSeat()

        }
        
        //init home address
        self.homeAddress.text = self.updatableUser?.getAddressLabel()
        
        //init phone number
        setupPrefixPicker()

        self.phoneLabel.text = self.updatableUser?.phoneNumber
        self.phonePrefixLabel.text = self.updatableUser?.phoneNumberPrefix
        
        //init iban
        if let settings = self.selectedOrganizationWithSettings?.settings {
            
            if(settings.ibanRequirement){
                self.ibanLabel.text = self.currentUser?.iban
            }else{
                self.ibanLabel.isHidden = true
                self.ibanLabelTitle.isHidden = true
            }
            
        }else{
            self.ibanLabel.isHidden = true
            self.ibanLabelTitle.isHidden = true
        }

        
        //check date
        if(selectedEnrollment!.isClosed()){
            self.view.makeToast("Iniziativa scaduta")
            
            saveButton.isHidden = true
            addWorkButton.isHidden = true
            addWorkLabel.isHidden = true
            
        }
        
        //setup terms
        setupTermsLink()

    }
    
    func setupPAWorkAddress(){
        if let paWorkAddress = self.organizationRelatedWorkAddress.first{
            self.workName.text = paWorkAddress.name ?? ""
            self.workAddress.text = paWorkAddress.getAddress(cityExtended: [:])
            if(paWorkAddress.city.value != nil){
                let searchedCity =  CityRepository.searchCity(code: paWorkAddress.city.value!)
                self.workAddress.text = paWorkAddress.getAddress(cityExtended: searchedCity)
            }
        }
    
    }
    
    
    private func checkOrganizationSeat(){
        
        self.addWorkButton.tintColor = .systemGray
        self.addWorkButton.isUserInteractionEnabled = false
        
        //get organization seat list
        NetworkingManager.sharedInstance.getOrganizationSeat(oid: self.selectedOrganization!.id!, completion: { result in
            switch result {
                case .success(let data):
                
                if(data.count == 0){
                    
                    self.addWorkButton.isHidden = true
                    
                    self.workAddressHeigth.constant = 0
                    self.workAddressTableView.layoutIfNeeded()
                    
                    self.addWorkLabel.text = "Nessuna sede selezionabile"
                    
                    
                }else{
                    
                    self.addWorkButton.tintColor = .systemRed
                    self.addWorkButton.isUserInteractionEnabled = true
                    
                }

                case .failure(_):
                
                    self.addWorkButton.isHidden = true
 
                    self.workAddressHeigth.constant = 0
                    self.workAddressTableView.layoutIfNeeded()
                
                    self.addWorkLabel.text = "Nessuna sede selezionabile"
     
            }
        })
    }
    

    private func loadCustomFields(){
        
        NetworkingManager.sharedInstance.getCustomFields(oid: self.selectedOrganization!.id!, completion: { result in
            switch result {
            case .success(let data):
                
                self.customFieldList.removeAll()
                self.customFieldList = data
                
                self.customFieldList.forEach{ item in
                    
                    NetworkingManager.sharedInstance.getCustomFieldsValue(oid: self.selectedOrganization!.id!, eid: item.id!, completion: { result in
                        switch result {
                        case .success(let data):
                            

                            self.customFieldListValue.removeAll()
                            self.customFieldListValue = data

                            DispatchQueue.main.async {
                                self.customFieldTableView.reloadData()
                            }

                            
                        case .failure(let error):
                            //MARK: ERROR STREAM
                            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])

                        }
                    })
                    
                }
                
                
            case .failure(let error):
                //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])

            }
        })
    }
    
    func setupTermsLink(){
    
        let tap = UITapGestureRecognizer(target: self, action: #selector(handleTermsAndRegulationClick))
        termsLabel.addGestureRecognizer(tap)
        termsLabel.isUserInteractionEnabled = true
        
        if self.selectedOrganization?.regulation != nil,  self.selectedOrganization?.termsConditions != nil{
            setLink(termsAndRegulationString, [termsString, regulationString])
        }else if (self.selectedOrganization?.regulation) != nil{
            setLink(termsAndRegulationString, [regulationString])
        }else if (self.selectedOrganization?.termsConditions) != nil{
            setLink(termsAndRegulationString, [termsString])
        }
    }
    
    func setLink(_ initialText: String,_ strings: [String]){
        let formattedText = String.format(strings: strings,
                                          boldFont: UIFont.boldSystemFont(ofSize: 15),
                                          boldColor: UIColor.red,
                                          inString: initialText,
                                          font: UIFont.systemFont(ofSize: 15),
                                          color: UIColor.black)
        
        termsLabel.attributedText = formattedText
    }
    

    @objc func handleTermsAndRegulationClick(gesture: UITapGestureRecognizer){
        let termsRange = (termsAndRegulationString as NSString).range(of: termsString)
        let regulationRange = (termsAndRegulationString as NSString).range(of: regulationString)

        let tapLocation = gesture.location(in: termsLabel)
        let index = termsLabel.indexOfAttributedTextCharacterAtPoint(point: tapLocation)

        if checkRange(termsRange, contain: index) {
            openTerms(selectedOrganization?.termsConditions ?? "")
            return
        }else if checkRange(regulationRange, contain: index){
            openRegulation(selectedOrganization?.regulation ?? "")
            return
        }
    }
    
    func openTerms(_ url: String){
        let documentVc = DocumentViewController()
        documentVc.documentURL = url
        self.present(documentVc, animated: true, completion: nil)
    }
    
    func openRegulation(_ regulation: String){
        
        let alert = UIAlertController(title: "Regolamento dell'iniziativa", message:regulation, preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: selectedOrganization!.getRegulationLink() != nil ? "Visita" : "Ok", style: .default, handler: { action in
            if let link = self.selectedOrganization!.getRegulationLink(){
                UIApplication.shared.open(link)
            }
        }))
        
        if(selectedOrganization!.getRegulationLink() != nil){
            alert.addAction(UIAlertAction(title: "Annulla", style: .cancel, handler: nil))
        }
        present(alert, animated: true, completion: nil)
    
    }
    
    
    func checkRange(_ range: NSRange, contain index: Int) -> Bool {
        return index > range.location && index < range.location + range.length
    }

    
    //add work address
    @IBAction func addWorkAddress(_ sender: Any) {
        //open seats controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let seatsController = storyBoard.instantiateViewController(withIdentifier: "seatsController") as! SeatsTableViewController
        seatsController.modalPresentationStyle = .popover
        if let popoverPresentationController = seatsController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
 
        seatsController.selectedOrganization = self.selectedOrganization
        
        self.present(seatsController, animated: true, completion: nil)
        
        seatsController.onDoneBlock = { result in
            guard !self.organizationRelatedWorkAddress.contains(where: {$0.id.value == result.id}) else{
                self.view.makeToast("Sede già presente")
                return
            }
            //update user
            let workAddress = WorkAddress()
            workAddress.id.value = result.id
            workAddress.city.value = result.city
            workAddress.address = result.address
            workAddress.name = result.name
            workAddress.number = result.number
            workAddress.lat.value = result.latitude
            workAddress.lng.value = result.longitude
            workAddress.organization.value = result.organization
            
            self.updatableUser?.workAddresses.append(workAddress)
            
            self.workAddressTableView.reloadData()
            self.workAddressTableView.layoutIfNeeded()
            self.workAddressHeigth.constant = 250
            
        }
    }
    
    fileprivate func updateUserProfile() {
        NetworkingManager.sharedInstance.updateUser(user: self.updatableUser!, completion: { result in
            switch result {
            case .success(_):
                
                DBManager.sharedInstance.saveUser(user: self.updatableUser!)
                
                //reload updatableUser
                self.initUpdatableUser()
                
                self.view.makeToast("Salvataggio effettuato correttamente")
                
                self.onNewInitiativeAdded()
                //self.dismiss(animated: true, completion: nil)
                
            case .failure(let error):
                
                //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])

            }
        })
    }
    
    
    fileprivate func updateCustomFields() {
        
        //get updated CustomField
        self.customFieldListValue.forEach{item in
            NetworkingManager.sharedInstance.updateCustomFieldValue(oid: self.selectedOrganization!.id!, field: item, completion: { result in
                switch result {
                case .success(_):
                    break
                    

                    
                case .failure(let error):
                    
                    //MARK: ERROR STREAM
                    //NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break

                }
            })
        }

        

    }
    
    
    @IBAction func saveAll(_ sender: Any){
        
        guard updatableUser?.uid != nil && selectedEnrollment?.code != nil else{
            LogHelper.log(message: "Trying to save an empty user!")
            return
        }
        
        
        //check user phone number
        if(phoneLabel.text!.isEmpty){
            showBasicAlert(title: "Attenzione", description: "Inserisci un numero di telefono valido")
            return
        }
        if(updatableUser?.isAddressComplete() != true){
            showBasicAlert(title: "Attenzione", description: "Inserisci un numero di casa completo")
            return
        }
        
        //IF PA Check that the work address is not empty
        if(self.enrollmentType == UserAddressTableViewCell.CellType.Normal){
            if(self.workName.text == nil || self.workName.text == "" ){
                showBasicAlert(title: "Attenzione", description: "Inserisci il nome della tua scuola o azienda")
                return
            }
            if(self.workAddress.text == nil || self.workAddress.text == ""){
                showBasicAlert(title: "Attenzione", description: "Inserisci un indirizzo della tua scuola o azienda completo")
                return
            }
            if let newWorkAddress = selectedPlaceWork{
                if(!newWorkAddress.isAddressComplete()){
                    showBasicAlert(title: "Attenzione", description: "Inserisci un indirizzo di lavoro completo")
                    return
                }
            }else{
                let isPreviousValid = organizationRelatedWorkAddress.first?.city != nil && organizationRelatedWorkAddress.first?.address != nil && organizationRelatedWorkAddress.first?.address != ""
                if(!isPreviousValid){
                    showBasicAlert(title: "Attenzione", description: "Inserisci un indirizzo di lavoro completo")
                }
            }
        }
      
        
        self.updatableUser?.phoneNumber = self.phoneLabel.text
        self.updatableUser?.phoneNumberPrefix = self.phonePrefixLabel.text
        
        //save home address ONLy if user select a new home address
        if(self.selectedPlaceHome != nil){
            let (homeCity,homeAddress,homeStreetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlaceHome)
            self.updatableUser?.homeAddress = homeAddress ?? ""
            self.updatableUser?.homeNumber = homeStreetNumber ?? ""
            self.updatableUser?.cityLisMove = homeCity ?? ""
            self.updatableUser?.homeCity.value = CityRepository.getCityCode(byName: homeCity ?? "")
        }
        
        if(selectedPlaceHomeCoordinates != nil){
            self.updatableUser?.homeLatitude.value = self.selectedPlaceHomeCoordinates?.0
            self.updatableUser?.homeLongitude.value = self.selectedPlaceHomeCoordinates?.1
        }
        

        saveWorkAddressifPa(onComplete: { [self] in
            self.view.makeToast("Aggiornamento in corso")

            self.updateCustomFields()
            
            //update user
            if(isEnrollmentNew){
                self.consumeCodeAndUpdateProfile()
            }else{
                self.updateUserProfile()
            }
        })
        //save work address ONLY if user select a new work address
        
        
                
    }

    func saveWorkAddressifPa(onComplete: @escaping ()->()){
        let previousAddress = currentUser?.workAddresses.first(where: {$0.organization.value == selectedOrganization?.id})
        if(self.enrollmentType == UserAddressTableViewCell.CellType.Normal){
            
            if(self.selectedPlaceWork != nil || self.selectedPlaceWorkCoordinates != nil){
            
            
                if(selectedPlaceWork != nil){
                    let (workCity,workAddress,workStreetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlaceWork)
                    
                    //1. create new seat
                    let newSeat = OrganizationSeat(
                        address: workAddress,
                        city: workCity != nil ? CityRepository.getCityCode(byName: workCity ?? "") : nil,
                        cityName: workCity,
                        id: nil,
                        latitude: self.selectedPlaceWorkCoordinates?.0,
                        longitude: self.selectedPlaceWorkCoordinates?.1,
                        name: self.workName.text,
                        number: workStreetNumber,
                        organization: self.selectedOrganization?.id,
                        validated: nil
                    )
                    updateSeat(newSeat: newSeat, onSuccess: {
                        onComplete()
                    })
                    return

                }else if let waddress = self.updatableUser?.workAddresses.first(where: {$0.organization.value == self.selectedOrganization?.id}){
                    
                    let newSeat = OrganizationSeat(
                        address: waddress.address,
                        city: waddress.city.value,
                        cityName: waddress.city.value != nil ? CityRepository.getCityName(byCode: waddress.city.value!) : nil,
                        id: nil,
                        latitude: self.selectedPlaceWorkCoordinates?.0,
                        longitude: self.selectedPlaceWorkCoordinates?.1,
                        name: self.workName.text,
                        number: waddress.number,
                        organization: self.selectedOrganization?.id,
                        validated: nil
                    )
                    updateSeat(newSeat: newSeat, onSuccess: {
                        //update custom fields
                      onComplete()

                    })
                    return
            }else{
                    LogHelper.logError(message: "Errore nell'aggiornare l'indirizzo di lavoro", withTag: "USerAddressTableViewCOntroller")
                    onComplete()
                    return
                }
                
            }else if let previousAddress = previousAddress, previousAddress.name != self.workName.text {
                let newSeat = OrganizationSeat(
                    address: previousAddress.address,
                    city: previousAddress.city.value,
                    cityName: previousAddress.city.value != nil ? CityRepository.getCityName(byCode: previousAddress.city.value!) : nil,
                    id: nil,
                    latitude: previousAddress.lat.value,
                    longitude: previousAddress.lng.value,
                    name: self.workName.text,
                    number: previousAddress.number,
                    organization: self.selectedOrganization?.id,
                    validated: nil
                )
                updateSeat(newSeat: newSeat, onSuccess: {
                    //update custom fields
                  onComplete()

                })
                return
            }
        }
        onComplete()
    }
    
    
    
    
    func consumeCodeAndUpdateProfile(){
        NetworkingManager.sharedInstance.consumeCode(uid: self.updatableUser!.uid!, code: self.selectedEnrollment!.code!, completion: { result in
            switch result {
            case .success(_):
                    self.updateUserProfile()
                    
                case .failure(let error):
                
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
  
    }
    
    func updateSeat(newSeat: OrganizationSeat, onSuccess: @escaping()->()){
        NetworkingManager.sharedInstance.createOrganizationSeat(oid: self.selectedOrganization!.id!, seat: newSeat, completion: { result in
            switch result {
            case .success(let seat):
                    
                //2. create new addres
                let newWorkAddress = WorkAddress()

                newWorkAddress.city.value = newSeat.city
                newWorkAddress.id.value = seat.id
                
                newWorkAddress.address = newSeat.address
                newWorkAddress.name = newSeat.name
                newWorkAddress.number = newSeat.number
                newWorkAddress.organization.value = self.selectedOrganization?.id
                
                //update coordinates
                newWorkAddress.lat.value = newSeat.latitude
                newWorkAddress.lng.value = newSeat.longitude
            
                
                //clean old work addresses of same organizations
                let removableAddress = self.updatableUser?.workAddresses.filter{$0.organization.value == self.selectedOrganization?.id}
                
                removableAddress?.forEach{item in
                    if let index = self.updatableUser?.workAddresses.firstIndex(of: item){
                        self.updatableUser?.workAddresses.remove(at: index)
                    }
                }
                
                //add new address
                self.updatableUser?.workAddresses.append(newWorkAddress)
                onSuccess()
                case .failure(let error):
                
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
    }
    

    // MARK: - Table view data source

    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        
        if(tableView == self.workAddressTableView){
            //work address count + 1 fidex row of home
            switch self.enrollmentType {
            case .Normal:
                return 0
            case .Organization:
                return organizationRelatedWorkAddress.count
            }
        }else{
            
            return self.customFieldListValue.count
        }

    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if(tableView == self.workAddressTableView){
            let cell = tableView.dequeueReusableCell(withIdentifier: "userAddressCell", for: indexPath) as! UserAddressTableViewCell
            
            //user work address
            cell.type = UserAddressTableViewCell.CellType.Organization
            
            let workAddress = self.organizationRelatedWorkAddress[indexPath.row]
            cell.name.text = workAddress.name ?? ""
            cell.address.text = workAddress.address ?? ""
            cell.number.text = workAddress.number ?? ""
            
            let searchedCity = CityRepository.getCity(byCode: workAddress.city.value ?? -1)
            let city = searchedCity?.nome ?? ""
            let prov = searchedCity?.provincia?.nome ?? ""
            
            cell.city.text = "\(city ?? ""), \(prov ?? "")"
            cell.deleteAddress.isEnabled = !(self.selectedEnrollment?.isClosed() ?? true)
            cell.deleteAddressAction = {
                self.deleteAddress(indexPath: indexPath)
                
                DispatchQueue.main.async {
                    self.workAddressTableView.reloadData()
                }
            }
            
            return cell
            
        }else{
            let cell = tableView.dequeueReusableCell(withIdentifier: "customFieldCell", for: indexPath) as! CustomFieldTableViewCell
            cell.checkbox.isEnabled = !(self.selectedEnrollment?.isClosed() ?? true)
            cell.checkbox.isOn = self.customFieldListValue[indexPath.row].value ?? false
            cell.checkboxLabel.text = self.customFieldList[indexPath.row].name
            
            cell.checkAction = {
                
                self.customFieldListValue[indexPath.row].value = cell.checkbox.isOn
                
                //deselect all flag
                for (index,item) in self.customFieldListValue.enumerated(){
                    if(index != indexPath.row){
                        item.value = false
                        
                        let indexPath = IndexPath(item: index, section: 0)
                        tableView.reloadRows(at: [indexPath], with: .automatic)
                    }
                }
                
                
            }
            
            return cell
            
        } 
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
        
            self.deleteAddress(indexPath: indexPath)
        
        }
    }
    
    
    private func deleteAddress(indexPath: IndexPath){
        
        let workAddress = organizationRelatedWorkAddress[indexPath.row]
        if let workAddressIndex = self.updatableUser?.workAddresses.firstIndex(where: {$0.id.value == workAddress.id.value}){
            
            self.updatableUser?.workAddresses.remove(at: workAddressIndex)
            
            DispatchQueue.main.async {
                self.workAddressTableView.reloadData()
            }

        }
        
        self.view.makeToast("Inidirizzo rimosso con successo")
    }
    
    
    @IBAction func autocompleteTapHome(_ sender: Any) {
        isHomeAutocomplete = true
        self.performSegue(withIdentifier: "GMSAutocomplete", sender: nil)
    }
    
    
    @IBAction func autocompleteTapWork(_ sender: Any) {
        isHomeAutocomplete = false
        self.performSegue(withIdentifier: "GMSAutocomplete", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "GMSAutocomplete" {
            
            let navController = segue.destination as! UINavigationController
            let detailController = navController.topViewController as! GMSAutocompleteViewController

            if(isHomeAutocomplete){
                
                //setup address
                if(self.selectedPlaceHome != nil){
                    
                    let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlaceHome)
                    detailController.address = "\(address ?? ""), \(streetNumber ?? ""), \(city ?? "")"
                    detailController.isAddressComplete = updatableUser?.isAddressComplete() ?? false
                    
                }else{
                    
                    detailController.address = self.homeAddress.text
                    detailController.isAddressComplete = updatableUser?.isAddressComplete() ?? false

                }
                
                
                //setup coordinates
                if(self.selectedPlaceHomeCoordinates != nil){
                    
                    detailController.addressLatLng = (selectedPlaceHomeCoordinates?.0,
                                                      selectedPlaceHomeCoordinates?.1 )
                    
                }else{
                    
                    detailController.addressLatLng = (self.updatableUser?.homeLatitude.value, self.updatableUser?.homeLongitude.value)
                }
                
                
                
            }else{
                
                //setup address
                if(self.selectedPlaceWork != nil){
                    
                    let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlaceWork)
                    detailController.address = "\(address ?? ""), \(streetNumber ?? ""), \(city ?? "")"
                    detailController.isAddressComplete = self.selectedPlaceWork?.isAddressComplete() ?? false
                    
                }else{
                    
                    detailController.isAddressComplete = organizationRelatedWorkAddress.first?.isComplete() ?? false
                    detailController.address = self.workAddress.text
                }
                
                
                
                //setup coordinates
                if(self.selectedPlaceWorkCoordinates != nil){
                    detailController.addressLatLng = (self.selectedPlaceWorkCoordinates?.0, self.selectedPlaceWorkCoordinates?.1 )
                    
                }else{
                    
                    let address = organizationRelatedWorkAddress.first
                    detailController.addressLatLng = (address?.lat.value, address?.lng.value)
                    
                }
  
            }
            
            detailController.onDoneBlock = { result in
                
                if(self.isHomeAutocomplete){
                    
                    if((result.1) != nil){
                        self.selectedPlaceHome = (result.1)
                    }
         
                    if((result.0) != nil){
                        self.selectedPlaceHomeCoordinates = (result.0)
                    }
    
                }else{
                    if((result.1) != nil){
                        self.selectedPlaceWork = (result.1)
                    }
         
                    if((result.0) != nil){
                        self.selectedPlaceWorkCoordinates = (result.0)
                    }
                }

                
                //setup label
                self.setupAddress()
                
                self.dismiss(animated: true, completion: nil)
                
   
            }
            
            present(navController, animated: true, completion: nil)
            
        }
    }
    
    

}

extension UserAddressTableViewController{

    private func setupAddress(){
        
        if(self.isHomeAutocomplete){
            
            //setup label
            if(self.selectedPlaceHome != nil){
                
                if(self.updatableUser?.homeCity == nil || self.updatableUser?.homeAddress == nil){
                    
                    self.view.makeToast("Inserisci un indirizzo completo")
                    return
                }
                homeAddress.text = updatableUser?.getAddressLabel()
                
            }
            
            
        }else{
            
            if(self.selectedPlaceWork != nil){
                let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlaceWork)
                
                
                if(city == nil || address == nil){
                    
                    self.view.makeToast("Inserisci un indirizzo completo")
                    return
                }
                
                
                let searchedCity = CityRepository.getCity(byName: city ?? "")
                let completeCity = searchedCity?.nome
                let completeProv = searchedCity?.provincia?.nome
                
                
                //setup label
                self.workAddress.text = "\(address ?? "") \(streetNumber ?? ""), \(completeCity ?? ""), \(completeProv ?? "")"
            }

        }
        
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
    
}

extension UserAddressTableViewController: CountryPickerViewDelegate{
    func countryPickerView(_ countryPickerView: CountryPickerView, didSelectCountry country: Country) {
        phonePrefixLabel.text = country.phoneCode.replacingOccurrences(of: "+", with: "")
    }
    func countryPickerView(_ countryPickerView: CountryPickerView, willShow viewController: CountryPickerViewController){
        viewController.navigationController?.navigationBar.tintColor = UIColor.white
    }


}

extension GMSPlace{
    func isAddressComplete() -> Bool{
        let (parsedCity, parsedAddress, parsedStreetNumber) =  PlaceUtil.parseDataFromPlace(place: self)
        return parsedAddress != nil && parsedAddress != "" && parsedCity != nil
    }
}
