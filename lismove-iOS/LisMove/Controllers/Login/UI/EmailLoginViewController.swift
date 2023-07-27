//
//  EmailLoginViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 18/04/21.
//

import UIKit
import PKHUD
import FirebaseAuth

class EmailLoginViewController: UIViewController,UITextFieldDelegate {

    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var resetPasswordButton: UIButton!
    
    //db
    var DB = DBManager.sharedInstance
    var network = NetworkingManager.sharedInstance
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.hideKeyboardWhenTappedAround() 
        
        initView()

    }
    
    private func initView(){
        
        loginButton.layer.cornerRadius = 16
        resetPasswordButton.layer.cornerRadius = 16
        
        emailTextField.layer.borderColor = UIColor.white.cgColor
        passwordTextField.layer.borderColor = UIColor.white.cgColor

        emailTextField.layer.borderWidth = 1.0
        passwordTextField.layer.borderWidth = 1.0
        
        emailTextField.attributedPlaceholder = NSAttributedString(string: "Email",
                                                                   attributes: [NSAttributedString.Key.foregroundColor: UIColor.white])
        
        passwordTextField.attributedPlaceholder = NSAttributedString(string: "Password",
                                                                   attributes: [NSAttributedString.Key.foregroundColor: UIColor.white])
        
        self.emailTextField.delegate = self
        self.passwordTextField.delegate = self
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    
    @IBAction func emailLogin(_ sender: Any) {
        
        if(!self.emailTextField.text!.isEmpty && !self.passwordTextField.text!.isEmpty){
    
            Auth.auth().signIn(withEmail: emailTextField.text!, password: passwordTextField.text!) { [self] authResult, error in
                if let error = error{
                
                    
                    if let errCode = AuthErrorCode(rawValue: error._code) {

                        switch errCode {
                        case .wrongPassword:
                            
                            //reset password
                            network.resetPasswordUser(email: emailTextField.text!, completion: {result in
                                
                                switch result{
                                case .success(let check):
                                    
                                    if(check){
                                        //send reset password
                                        Auth.auth().sendPasswordReset(withEmail: emailTextField.text!) { error in
                                            
                                            if(error != nil){
                                                //MARK: ERROR STREAM
                                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])
                                            }else{
                                                
                                                //MARK: signup complete with reset password form
                                                let alert = UIAlertController(title: "Reimposta Password", message: "Per motivi di sicurezza è necessario reimpostare la propria password", preferredStyle: .alert)
                                                if let popoverPresentationController = alert.popoverPresentationController {
                                                    popoverPresentationController.sourceView = self.view
                                                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                                }
                                                alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
                                                self.present(alert, animated: true, completion: nil)
                                                
                                            }
                                            
                                        }
                                    }else{
                                        //MARK: ERROR STREAM
                                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    }

                                    
                                    
                                case .failure(_):
                                        //MARK: ERROR STREAM
                                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    
                                }
                            })
                            
                        default:
                                
                            DispatchQueue.main.async {
                                //show error popup
                                let alert = UIAlertController(title: "Errore", message: error.localizedDescription, preferredStyle: .alert)
                                if let popoverPresentationController = alert.popoverPresentationController {
                                    popoverPresentationController.sourceView = self.view
                                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                }
                                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                self.present(alert, animated: true, completion: nil)
                            }

                        }
                    }
        
                    
                }else{
                    
                    //advise user
                    self.view.makeToast("Login in corso", duration: 2.0, position: .bottom)
                    
                    network.getUser(uid: Auth.auth().currentUser!.uid){ result in
                        
                        switch result{
                            case .success(let user):
                                
                                //check uid
                                if(user.uid == nil){
                                    user.uid = Auth.auth().currentUser!.uid
                                }
                            
                                //save user offline
                                UserDefaults.standard.setValue("Other", forKey: "AccessMethod")

                                self.DB.saveUser(user: user)
                                
                                if(user.signupCompleted == false){
                                    //segue to dashboard
                                    //open terms controller
                                    let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                                    let termsViewController = storyBoard.instantiateViewController(withIdentifier: "termsController") as! UINavigationController
                                    termsViewController.modalPresentationStyle = .fullScreen
                                    
                                    self.present(termsViewController, animated: true, completion: nil)
                                    
                                }else{
                                    //segue to dashboard
                                    let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                                    let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
                                    DashboardController.modalPresentationStyle = .fullScreen
                                
                                    
                                    self.present(DashboardController, animated: true, completion: nil)
                                }
                            
                            case .failure(let error):
                                
                                if(error.responseCode == 401 || (error.localizedDescription.contains("User") && error.localizedDescription.contains("not found"))){
                                    //signup error
                                    DispatchQueue.main.async {
                                        //show error popup
                                        let alert = UIAlertController(title: "Avviso", message: "Il tuo account non risulta registrato. Accedi alla sezione dedicata per crearne uno.", preferredStyle: .alert)
                                        if let popoverPresentationController = alert.popoverPresentationController {
                                            popoverPresentationController.sourceView = self.view
                                            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                        }
                                        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                        self.present(alert, animated: true, completion: nil)
                                    }
                                    
                                }else{
                                    //generic error
                                    //MARK: ERROR STREAM
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                }
                        }
                    }
            
                }
            }
        }else{
            PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Errore. Controlla email e password")
            PKHUD.sharedHUD.show()
            PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
            }
        }
    }

    
    @IBAction func resetPassword(_ sender: Any) {
    
        if(self.emailTextField.text!.isEmpty){
            PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Inserisci un indirizzo email")
            PKHUD.sharedHUD.show()
            PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
            }
        }else{
            //send reset password
            Auth.auth().sendPasswordReset(withEmail: self.emailTextField.text!) { error in
                if let error = error{
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                }else{
                    PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Riceverai un'email per impostare una nuova password. Successivamente effettua il login")
                    PKHUD.sharedHUD.show()
                    PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
                    }
                }
            }
        }
    
        
    }
    
    
}
