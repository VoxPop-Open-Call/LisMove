//
//  InitiativeTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/07/21.
//

import UIKit
import Toast_Swift
import Kingfisher
import Resolver

class InitiativeTableViewController: UITableViewController {
    
    //MARK: UI
    @IBOutlet weak var newIniativeButton: UIButton!
    
    //MARK: Services
    var enrollmentsList: [Enrollment] = []
    var organizationList: [OrganizationWithSettings] = []
    
    var selectedEnrollment: Enrollment?
    var selectedOrganization: OrganizationWithSettings?
    var isEnrollmentNew: Bool = false
    
    let refresh = UIRefreshControl()
    
    @Injected var userRepository: UserRepositoryImpl
    @Injected var initiativeRepository: InitiativeRepository
    
    override func viewDidLoad() {
        super.viewDidLoad()

        
        initView()
        
        syncEnrollments()
        
    }
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        // Add Refresh Control to Table View
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refresh
        } else {
            tableView.addSubview(refresh)
        }
        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        
        newIniativeButton.layer.cornerRadius = 16
        newIniativeButton.setTitle("AGGIUNGI INIZIATIVA", for: .normal)
        
    }
    
    
    @objc private func refreshData(_ sender: Any) {
        LogHelper.log(message: "RefreshingData")
        syncEnrollments()
    }
    
    
    private func syncEnrollments(){
        
        //self.view.makeToast("Caricamento in corso")
        
        let user = DBManager.sharedInstance.getCurrentUser()
        
        //save temporary user into server
        self.initiativeRepository.getEnrollements(uid: user!.uid ?? "") { result in
            switch result {
                case .success(let data):
                    
                self.enrollmentsList = data.sorted(by: {$0.endDate ?? 0 > $01.endDate ?? 0})
                    
                    if(self.enrollmentsList.isEmpty){
                        
                        self.view.makeToast("Nessuna iniziativa presente")
                        
                    }else{
                        
                        self.organizationList.removeAll()
                        
                        self.initiativeRepository.getOrganizationWithSettings(oids: self.enrollmentsList.compactMap{$0.organization}) { result in
                            switch result {
                                case .success(let data):
                                    
                                    self.organizationList = data
                                
                                    DispatchQueue.main.async {
                                        self.tableView.reloadData()
                                    }

                                    
                                    self.refresh.endRefreshing()

                                    
                                case .failure(let error):
                                    //MARK: ERROR STREAM
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    
                            }
                        }
                        
                    }

                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        }
        
    }

    
    @IBAction func onNewIntiativeTap(_ sender: Any) {
        
        let checkuserCompleted = userRepository.checkUserProfileCompleted()
        if(!checkuserCompleted){
            
            let alert = UIAlertController(title: "Attenzione", message: "Completa ora il tuo profilo per accedere a tutti i servizi LisMove", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            let confirm = UIAlertAction(title: "Conferma", style: .default, handler: { action in
                
                //open account controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                let navController = storyBoard.instantiateViewController(withIdentifier: "AccountController") as! UINavigationController
                let detailController = navController.topViewController as! AccountViewController
                detailController.isModifyProfileMode = true
                
                self.present(navController, animated: true, completion: nil)
                
                
            })
            
            alert.addAction(confirm)
            
            self.present(alert, animated: true, completion: nil)
            
            
        }else{
            performSegue(withIdentifier: "addNewInitiativeSegue", sender: nil)
        }
           
    }
    
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.enrollmentsList.count
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 86
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "enrollmentCell", for: indexPath)

        if(self.enrollmentsList.count > 0){
            let data = self.enrollmentsList[indexPath.row]
        
            cell.textLabel?.text = self.organizationList.filter{$0.organization.id == data.organization}.first?.organization.title
            
            cell.detailTextLabel?.text = data.getDateIntervalString()

            cell.textLabel?.textColor = .black
            cell.detailTextLabel?.textColor = .gray
        }

        
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        self.selectedEnrollment = self.enrollmentsList[indexPath.row]
        self.selectedOrganization = self.organizationList.first(where: {$0.organization.id == self.selectedEnrollment?.organization})
        self.isEnrollmentNew = false
        
        
        if(self.selectedOrganization != nil){
            performSegue(withIdentifier: "newInitiativeSegue", sender: nil)
            tableView.deselectRow(at: indexPath, animated: true)
        }


    }
    
 
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "newInitiativeSegue" {
            
            let navController = segue.destination as! UINavigationController
            let viewController = navController.topViewController as! UserAddressTableViewController

            viewController.selectedEnrollment = self.selectedEnrollment
            viewController.selectedOrganization = self.selectedOrganization?.organization
            viewController.selectedOrganizationWithSettings = self.selectedOrganization
            viewController.isEnrollmentNew = self.isEnrollmentNew
            
            viewController.onNewInitiativeAdded = {
                self.dismiss(animated: true, completion: nil)
                self.refreshData(self)
            }
            viewController.enrollmentType = (self.selectedOrganization?.organization.type == Organization.OrganizationType.COMPANY) ? UserAddressTableViewCell.CellType.Organization : UserAddressTableViewCell.CellType.Normal
            present(navController, animated: true, completion: nil)
            
        
            
        }else if segue.identifier == "addNewInitiativeSegue" {
            
            let viewController = segue.destination as! NewInitiativeViewController

            viewController.onNewInitiativeAdded = {
                self.dismiss(animated: true, completion: nil)
                self.refreshData(self)
            }

            present(viewController, animated: true, completion: nil)
        }
    }
    
    
    @IBAction func dismissController(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
    
}
