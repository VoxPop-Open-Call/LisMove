//
//  SignupViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 18/04/21.
//

import UIKit
import Firebase
import GoogleSignIn
import FBSDKLoginKit
import PKHUD
import AuthenticationServices
import Resolver
class SignupViewController: UIViewController, GIDSignInDelegate, ASAuthorizationControllerDelegate, UITextFieldDelegate {
    @Injected var phoneRepository: PhoneRepository
    @IBOutlet weak var signupFacebookButton: UIButton!
    @IBOutlet weak var signupGoogleButton: UIButton!
    @IBOutlet weak var signupButton: UIButton!
    @IBOutlet weak var signupAppleButtonView: UIView!
    

    @IBOutlet weak var emailLabel: UITextField!
    @IBOutlet weak var passwordLabel: UITextField!
    @IBOutlet weak var confirmPassword: UITextField!
    
    //db
    var DB = DBManager.sharedInstance
    var networking = NetworkingManager.sharedInstance
    var currentNonce: String?
    
    let buttonCornerRadius = CGFloat(20)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.hideKeyboardWhenTappedAround()
        
        //override google auth handler
        GIDSignIn.sharedInstance().delegate = self
        GIDSignIn.sharedInstance().presentingViewController = self
        
        initView()
        
    }
    
    
    private func initView(){
        
        signupFacebookButton.layer.cornerRadius = buttonCornerRadius
        signupGoogleButton.layer.cornerRadius = buttonCornerRadius
        signupAppleButtonView.layer.cornerRadius = buttonCornerRadius
        signupButton.layer.cornerRadius = buttonCornerRadius

        if #available(iOS 13.0, *) {
            let authorizationButton = ASAuthorizationAppleIDButton(type: .signIn, style: .whiteOutline)
            authorizationButton.addTarget(self, action: #selector(handleAppleIdRequest), for: .touchUpInside)
            authorizationButton.cornerRadius = 20
            authorizationButton.largeContentTitle = "Registrati con Apple"
            self.signupAppleButtonView.addSubview(authorizationButton)
            // Setup Layout Constraints to be in the center of the screen
            authorizationButton.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                authorizationButton.centerXAnchor.constraint(equalTo: self.signupAppleButtonView.centerXAnchor),
                authorizationButton.centerYAnchor.constraint(equalTo: self.signupAppleButtonView.centerYAnchor),
                authorizationButton.widthAnchor.constraint(equalToConstant: 230),
                authorizationButton.heightAnchor.constraint(equalToConstant: 50)
                ])
        }
        
        emailLabel.layer.borderColor = UIColor.white.cgColor
        passwordLabel.layer.borderColor = UIColor.white.cgColor
        confirmPassword.layer.borderColor = UIColor.white.cgColor

        emailLabel.layer.borderWidth = 1.0
        passwordLabel.layer.borderWidth = 1.0
        confirmPassword.layer.borderWidth = 1.0
        
        emailLabel.attributedPlaceholder = NSAttributedString(string: "Email",
                                                                   attributes: [NSAttributedString.Key.foregroundColor: UIColor.white])
        
        passwordLabel.attributedPlaceholder = NSAttributedString(string: "Password",
                                                                   attributes: [NSAttributedString.Key.foregroundColor: UIColor.white])
        
        confirmPassword.attributedPlaceholder = NSAttributedString(string: "Confirm Password",
                                                                   attributes: [NSAttributedString.Key.foregroundColor: UIColor.white])
        
        self.emailLabel.delegate = self
        self.passwordLabel.delegate = self
        self.confirmPassword.delegate = self
        
    }
    
    @objc func handleAppleIdRequest() {
        let nonce = NetworkingManager.sharedInstance.randomNonceString()
        currentNonce = nonce
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = NetworkingManager.sharedInstance.sha256(nonce)
        
        let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self
        authorizationController.performRequests()
        
        
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
      if let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential {
        
        guard let nonce = currentNonce else {
          fatalError("Invalid state: A login callback was received, but no login request was sent.")
        }
        guard let appleIDToken = appleIDCredential.identityToken else {
          print("Unable to fetch identity token")
          return
        }
        guard let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
          print("Unable to serialize token string from data: \(appleIDToken.debugDescription)")
          return
        }
        // Initialize a Firebase credential.
        let credential = OAuthProvider.credential(withProviderID: "apple.com",
                                                  idToken: idTokenString,
                                                  rawNonce: nonce)
        
        
        // Sign in with Firebase.
        Auth.auth().signIn(with: credential) { (authResult, error) in
            if (error != nil) {
                print(error!.localizedDescription)
                return
            }
            
            //write offline data
            //MARK: Signin with Apple complete
            //redirect to dashboard: Apple privacy policy
            
            let newUser = LismoveUser()
            newUser.uid = authResult?.user.uid
            newUser.email = authResult?.user.email
            newUser.firstName = ""
            newUser.lastName = ""
            newUser.username = authResult?.user.displayName ?? ("LisMove_user_" + self.randomString(length: 6))
            
            
            
            self.view.makeToast("Registrazione in corso", duration: 2.0, position: .bottom)
            
            //handle user data
            self.saveUserAndGoToNextScreen(user: newUser, appleAccess: true)

        }
      }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        //MARK: ERROR STREAM
        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
    }
    
    
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    

    
    /*
     signup actions
     */
    @IBAction func signupWithGoogle(_ sender: Any) {

        
        GIDSignIn.sharedInstance().signIn()
        
    }
    
    @IBAction func signupWithFacebook(_ sender: Any) {
        let loginManager = LoginManager.init()
        loginManager.logIn(permissions: ["user_photos", "email", "user_birthday", "public_profile"], from: self, handler: {
            result, error in
            guard error == nil else{
                //TODO: SHOW ERROR
                self.showError(message: error?.localizedDescription ?? "Si è verificato un errore")
                return
            }
            
            if(result == nil || result?.isCancelled == true){
                return
            }
            
            let req = GraphRequest(graphPath: "me", parameters: ["fields":"email,name"], tokenString: AccessToken.current!.tokenString, version: nil, httpMethod: .get)
            req.start(completionHandler: {connection, result, error in
                if let email = (result as? NSDictionary)?["email"] as? String{
                    self.networking.checkUser(email:email) {result in
                        switch result {
                        case .success(let userAlreadyExists):
                            if(!userAlreadyExists){
                                self.view.makeToast("Registrazione in corso", duration: 2.0, position: .bottom)
                                let credential = FacebookAuthProvider
                                  .credential(withAccessToken: AccessToken.current!.tokenString)
                                self.signInWithSocialCredential(credential: credential)
                            }else{
                                self.showError(message: "Utente già registrato, prova ad effettuare il login")
                            }
                            break
                        case .failure(let error):
                            self.showError(message: error.localizedDescription)
                            break
                        }
                    }
                }else{
                    self.showError(message: "Si è verificato un errore nel recuperare l'indirizzo email")
                }
            })
         
        })
    }
    
    
    @IBAction func signupWithEmail(_ sender: Any) {
        
        if(!self.emailLabel.text!.isEmpty && !self.passwordLabel.text!.isEmpty && !self.confirmPassword.text!.isEmpty){

            
            if(self.passwordLabel.text! != self.confirmPassword.text!){
             
                //MARK: signup complete with reset password form
                let alert = UIAlertController(title: "Errore", message: "Le password non corrispondono", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                alert.addAction(UIAlertAction(title: "Riprova", style: .default, handler: nil))
                self.present(alert, animated: true, completion: nil)
                
                return
            }


            //get user data
            let email = self.emailLabel.text!
            let password = self.passwordLabel.text!
            
            //advise user
            self.view.makeToast("Registrazione in corso", duration: 2.0, position: .bottom)
            
            //1. check user on server
            networking.checkUser(email: email) {result in
                switch result {
                    case .success(let check):
                        
                        //2. create user on Firebase
                        Auth.auth().createUser(withEmail: (email), password: password) { (result, error) in
                            
                            if(error != nil){
                                //MARK: ERROR STREAM
                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])
                            }else{
                            
                                    if(check == true){
                                        //send reset password
                                        Auth.auth().sendPasswordReset(withEmail: email) { error in
                                            
                                            if(error != nil){
                                                //MARK: ERROR STREAM
                                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])
                                            }else{
                                                
                                                //MARK: signup complete with reset password form
                                                let alert = UIAlertController(title: "Successo", message: "Account creato correttamente. Riceverai un'email per impostare una nuova password. Successivamente effettua il login", preferredStyle: .alert)
                                                if let popoverPresentationController = alert.popoverPresentationController {
                                                    popoverPresentationController.sourceView = self.view
                                                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                                }
                                                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                                self.present(alert, animated: true, completion: nil)
                                                
                                            }
                                            
                                        }
                                    }else{
                                        
                                        //MARK: signup complete 
                                    
                                        //create temporary user with auth data
                                        let user = LismoveUser()
                                        user.uid = Auth.auth().currentUser?.uid
                                        user.email = email
                                    
                                        //handle user data
                                        self.saveUserAndGoToNextScreen(user: user, appleAccess: false)
                                        

                                    }

                            }
                            
                        }
                        
                    case .failure(let error):
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                        break
                }
            }
            
        }else{
            PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Errore. Controlla email e password")
            PKHUD.sharedHUD.show()
            PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
            }
        }
    }
    
    private func saveUserAndGoToNextScreen(user: LismoveUser, appleAccess: Bool){
        //save temporary user into server
        
        user.activePhoneVersion = phoneRepository.getAppVersion()
        networking.saveUser(user: user) { result in
            switch result {
            case .success(_):
                
                    //save into db
                    self.DB.saveUser(user: user)
                    
                
                    if(!appleAccess){
                        //save access method
                        UserDefaults.standard.setValue("Other", forKey: "AccessMethod")
                        
                        //go to terms view
                        //open terms controller
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                        let termsViewController = storyBoard.instantiateViewController(withIdentifier: "termsController") as! UINavigationController
                        termsViewController.modalPresentationStyle = .fullScreen
                        self.present(termsViewController, animated: true, completion: nil)
                        
                    }else{
                        
                        //save access method
                        UserDefaults.standard.setValue("Apple", forKey: "AccessMethod")
                        
                        
                        //dashboard
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                        let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
                        DashboardController.modalPresentationStyle = .fullScreen
                        self.present(DashboardController, animated: true, completion: nil)
                        
                        
                    }

                    
            case .failure(let error):
                    //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        }
    }
    
    
    private func randomString(length: Int) -> String {

        let letters : NSString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        let len = UInt32(letters.length)

        var randomString = ""

        for _ in 0 ..< length {
            let rand = arc4random_uniform(len)
            var nextChar = letters.character(at: Int(rand))
            randomString += NSString(characters: &nextChar, length: 1) as String
        }

        return randomString
    }
    
    func showError(message: String){
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: message)
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 1.0) { success in
            // Completion Handler
        }
    }
    
}

/*
 Google auth system
 */

extension SignupViewController{
    
    
    @available(iOS 9.0, *)
    func application(_ application: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any])
      -> Bool {
      return GIDSignIn.sharedInstance().handle(url)
    }
    
    
    func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        return GIDSignIn.sharedInstance().handle(url)
    }
    
    
    func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error?) {
      // ...
      if error != nil {
        showError(message: "Accesso con Google fallito. Riprova")
      }else{
    
        
        //handle user data
        self.view.makeToast("Registrazione in corso", duration: 2.0, position: .bottom)
        
        //1.check user exist
        networking.checkUser(email: user.profile.email) {result in
            switch result {
                case .success(let check):
                    
                    if(check == true){
                        //user exists on server
                        /*
                            1. create profile with temporary password
                            2. send email for reset password
                         */
                        
                        Auth.auth().createUser(withEmail: (user.profile.email), password: (UUID().uuidString)) { (result, error) in
                                   if let error = error {
                                    
                                    //MARK: ERROR STREAM
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    
                                   }else{
                                        //send reset password
                                        Auth.auth().sendPasswordReset(withEmail: user.profile.email) { error in
                                            if error != nil{
                                                //MARK: ERROR STREAM
                                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])
                                            }else{
                                                
                                                //MARK: signup complete with reset password form
                                                let alert = UIAlertController(title: "Successo", message: "Account creato correttamente. Riceverai un'email per impostare una nuova password. Successivamente effettua il login", preferredStyle: .alert)
                                                if let popoverPresentationController = alert.popoverPresentationController {
                                                    popoverPresentationController.sourceView = self.view
                                                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                                }
                                                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                                self.present(alert, animated: true, completion: nil)
                                                
                                            }
                                        }
                                   }
                            }
                        
                    }else{
                        guard let authentication = user.authentication else { return }
                        let credential = GoogleAuthProvider.credential(withIDToken: authentication.idToken,
                                                                          accessToken: authentication.accessToken)
                    
                        //TODO: Sign in with credentials
                        self.signInWithSocialCredential(credential: credential)
                    }
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        }
        
      }
      
        
    }

    func signInWithSocialCredential(credential: AuthCredential){
        //user is new: sign in directly
        Auth.auth().signIn(with: credential) { (authResult, error) in
        
          if let error = error {
            let authError = error as NSError
            if (authError.code == AuthErrorCode.secondFactorRequired.rawValue) {
              // The user is a multi-factor user. Second factor challenge is required.
              let resolver = authError.userInfo[AuthErrorUserInfoMultiFactorResolverKey] as! MultiFactorResolver
              var displayNameString = ""
              for tmpFactorInfo in (resolver.hints) {
                displayNameString += tmpFactorInfo.displayName ?? ""
                displayNameString += " "
              }
                
                let alert = UIAlertController(title: "Attenzione", message: "Seleziona autenticazione per \(displayNameString)", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in

                    var selectedHint: PhoneMultiFactorInfo?
                    for tmpFactorInfo in resolver.hints {
                      if (displayNameString == tmpFactorInfo.displayName) {
                        selectedHint = tmpFactorInfo as? PhoneMultiFactorInfo
                      }
                    }
                    PhoneAuthProvider.provider().verifyPhoneNumber(with: selectedHint!, uiDelegate: nil, multiFactorSession: resolver.session) { verificationID, error in
                        
                      if error != nil {
                       
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Multi factor finanlize sign in failed. Error: \(error.debugDescription)"])
                        
                      } else {

                        let alert = UIAlertController(title: "Attenzione", message: "Inserisci Codice di verifica per \(selectedHint?.displayName ?? "")", preferredStyle: .alert)
                        if let popoverPresentationController = alert.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }

                        alert.addTextField { (textField) in
                            textField.placeholder = "Codice di verifica"
                        }
                        
                        
                        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
                            
                            let credential: PhoneAuthCredential? = PhoneAuthProvider.provider().credential(withVerificationID: verificationID!, verificationCode: (alert.textFields![0].text)!)
                            
                            let assertion: MultiFactorAssertion? = PhoneMultiFactorGenerator.assertion(with: credential!)
                            resolver.resolveSignIn(with: assertion!) { authResult, error in
                              if error != nil {
                                
                                //MARK: ERROR STREAM
                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Multi factor finanlize sign in failed. Error: \(error.debugDescription)"])
                                
                              } else {
                                self.saveCurrentAuthUser()
                              }
                            }
                            
                        }))
                        self.present(alert, animated: true, completion: nil)
                
                      }
                    }
                    
                }))
                
                self.present(alert, animated: true, completion: nil)
        
            } else {
                
                //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])

            }
            

          }else{
            self.saveCurrentAuthUser()
            
          }
        }
    }
    
    func saveCurrentAuthUser(){       
        if let user = Auth.auth().currentUser{
            let newUser = LismoveUser()
            newUser.uid = user.uid
            newUser.email = user.email
            self.saveUserAndGoToNextScreen(user: newUser, appleAccess: false)
        }
    }
    
    func sign(_ signIn: GIDSignIn!, didDisconnectWith user: GIDGoogleUser!, withError error: Error!) {
        // Perform any operations when the user disconnects from app here.
        // ...
    }
    
}
