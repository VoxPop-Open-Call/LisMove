//
//  ConfirmEmailViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/05/21.
//

import UIKit
import FirebaseAuth

class ConfirmEmailViewController: UIViewController {
    
    @IBOutlet weak var message: UILabel!
    @IBOutlet weak var continueButton: UIButton!
    @IBOutlet weak var resendEmailButton: UIButton!
    
    
    
    private var authUser : User? {
        return Auth.auth().currentUser
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        //load email into messageview
        self.message.text! += authUser?.email ?? ""
        
        continueButton.layer.cornerRadius = 16
        resendEmailButton.layer.cornerRadius = 16
        
    }
    
    
    @IBAction func sendEmail(_ sender: Any) {
        if self.authUser != nil && !self.authUser!.isEmailVerified {
            self.authUser!.sendEmailVerification(completion: { (error) in
                // Notify the user that the mail has sent or couldn't because of an error.
                if(error == nil){
                    let alert = UIAlertController(title: "Conferma Account", message: "Email inviata con successo", preferredStyle: .alert)
                    if let popoverPresentationController = alert.popoverPresentationController {
                        popoverPresentationController.sourceView = self.view
                        popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                    }
                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                    self.present(alert, animated: true, completion: nil)
                }
            })
        }
        else {
            // Either the user is not available, or the user is already verified.
        }
    }
    
    
    @IBAction func checkUser(_ sender: Any) {
        
        if authUser != nil {
            
            authUser?.reload(completion: {context in
                if(!self.authUser!.isEmailVerified){
                    let alert = UIAlertController(title: "Conferma Account", message: "Il tuo account non è stato ancora confermato. Controlla la tua casella di posta", preferredStyle: .alert)
                    if let popoverPresentationController = alert.popoverPresentationController {
                        popoverPresentationController.sourceView = self.view
                        popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                    }
                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                    self.present(alert, animated: true, completion: nil)
                    
                }else{
                    
                    let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                    let navController = storyBoard.instantiateViewController(withIdentifier: "newInitiativeNavigation") as! UINavigationController
                    let detailController = navController.topViewController as! NewInitiativeViewController
                    //TODO: CHECK
                    navController.modalPresentationStyle = .fullScreen
                    detailController.isFirstStart = true

                    self.present(navController, animated: true, completion: nil)

                }
            })
            

            
        }else {
            // Either the user is not available, or the user is already verified.
            //MARK: ERROR STREAM
            NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile continuare con la configurazione. Riavvia l'app"])
            
            
        }
    }
    
    
    @IBAction func signup(_ sender: Any) {
        //open signup
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        let signupViewController = storyBoard.instantiateViewController(withIdentifier: "signupViewController")
        signupViewController.modalPresentationStyle = .fullScreen
    
        
        self.present(signupViewController, animated: true, completion: nil)
    }
}
