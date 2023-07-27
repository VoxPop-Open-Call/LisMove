//
//  NewInitiativeViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/10/21.
//

import UIKit
import Resolver

class NewInitiativeViewController: UIViewController,UITextFieldDelegate {

    @IBOutlet weak var codeLabel: UITextField!
    @IBOutlet weak var verifyCodeButton: UIButton!
    
    
    let user = DBManager.sharedInstance.getCurrentUser()
    var selectedEnrollment: Enrollment?
    var selectedOrganization: Organization?
    var isEnrollmentNew: Bool = false
    public var onNewInitiativeAdded: ()->() = {}
    
    public var isFirstStart = false
    
    @Injected var initiativeRepository: InitiativeRepository
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        self.hideKeyboardWhenTappedAround()
        self.initView()

    }
    
    
    private func initView(){
        
        verifyCodeButton.layer.cornerRadius = 16
        
        self.codeLabel.delegate = self
        
        
        if (!isFirstStart){
            self.navigationItem.rightBarButtonItem?.isEnabled = false
            self.navigationItem.rightBarButtonItem?.tintColor = UIColor.clear
        }
    }
    
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    @IBAction func skipAction(_ sender: Any) {
        //segue to dashboard
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
        DashboardController.modalPresentationStyle = .fullScreen
    
        
        self.present(DashboardController, animated: true, completion: nil)
    }
    
    @IBAction func verifyCode(_ sender: Any) {
        let code = codeLabel.text
        
        if(code != ""){
            
            NetworkingManager.sharedInstance.verifyCode(uid: user!.uid!, code: code!, completion: {result in
                switch result {
                    case .success(let enrollment):

                    guard let id = enrollment.organization else{
                        return
                    }
                    
                    self.initiativeRepository.getOrganization(oid: id){ result in
                        switch result {
                            case .success(let organization):
                                
                                //refresh session data
                                SessionManager.sharedInstance.pointManager?.syncInitiative()
                                
                                //advise user
                                let alert = UIAlertController(title: "Congratulazioni", message: "Codice iniziativa valido", preferredStyle: .alert)
                                if let popoverPresentationController = alert.popoverPresentationController {
                                    popoverPresentationController.sourceView = self.view
                                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                }
                                alert.addAction(UIAlertAction(title: "Continua", style: .default, handler: { action in
                                    
                                    //select enrollment
                                    self.selectedEnrollment = enrollment
                                    self.selectedOrganization = organization
                                    self.isEnrollmentNew = true
                                
                                    self.goToUserAddress()

                                    
                                    
                                }))
                                self.present(alert, animated: true, completion: nil)
                                
                            
                                
                            case .failure(let error):
                                //MARK: ERROR STREAM
                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                
                        }
                    }
                        
                    
                    case .failure(let error):
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                        
                }
            })
            
        }else{
            self.view.makeToast("Non hai inserito nessun codice")
        }
    }
    
    
    func goToUserAddress(){
        weak var pvc = self.presentingViewController
        
        self.dismiss(animated: true, completion: {
            let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
            let viewController = storyBoard.instantiateViewController(withIdentifier: "UserAddressTableViewController") as! UserAddressTableViewController

            viewController.selectedEnrollment = self.selectedEnrollment
            viewController.selectedOrganization = self.selectedOrganization
            viewController.isEnrollmentNew = self.isEnrollmentNew
            viewController.onNewInitiativeAdded = {
                
                
                if(self.isFirstStart){
                    
                    //dismiss user address controller
                    viewController.dismiss(animated: true, completion: nil)
                    
                    //segue to dashboard
                    let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                    let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
                    DashboardController.modalPresentationStyle = .fullScreen
                    
                    pvc?.present(DashboardController, animated: true, completion: nil)
                    
                }else{
                    self.onNewInitiativeAdded()
                }

            }
            viewController.enrollmentType = (self.selectedOrganization?.type == Organization.OrganizationType.COMPANY) ? UserAddressTableViewCell.CellType.Organization : UserAddressTableViewCell.CellType.Normal
            if(self.isFirstStart){
                viewController.modalPresentationStyle = .fullScreen
            }
            pvc?.present(viewController, animated: true, completion: nil)
            
        })
    }
    
}
