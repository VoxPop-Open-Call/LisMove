//
//  LoginViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 18/04/21.
//

import UIKit
import Firebase
import GoogleSignIn
import PKHUD
import AuthenticationServices
import FBSDKLoginKit
class LoginViewController: UIViewController, GIDSignInDelegate, ASAuthorizationControllerDelegate {

    @IBOutlet weak var facebookLoginButton: UIButton!
    @IBOutlet weak var googleLoginButton: UIButton!
    @IBOutlet weak var emailLoginButton: UIButton!
    @IBOutlet weak var appleLoginView: UIView!
    
    //db
    var DB = DBManager.sharedInstance
    var network = NetworkingManager.sharedInstance
    
    var currentNonce: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //ovveride google auth handler
        GIDSignIn.sharedInstance().delegate = self
        GIDSignIn.sharedInstance().presentingViewController = self

        initView()
    
    }
    

    private func initView(){
        facebookLoginButton.layer.cornerRadius = 16
        googleLoginButton.layer.cornerRadius = 16
        emailLoginButton.layer.cornerRadius = 16
        appleLoginView.layer.cornerRadius = 16
        
        if #available(iOS 13.0, *) {
            let authorizationButton = ASAuthorizationAppleIDButton(type: .signIn, style: .whiteOutline)
            authorizationButton.addTarget(self, action: #selector(handleAppleIdRequest), for: .touchUpInside)
            authorizationButton.cornerRadius = 16
            authorizationButton.largeContentTitle = "Accedi con Apple"
            self.appleLoginView.addSubview(authorizationButton)
            // Setup Layout Constraints to be in the center of the screen
            authorizationButton.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                authorizationButton.centerXAnchor.constraint(equalTo: self.appleLoginView.centerXAnchor),
                authorizationButton.centerYAnchor.constraint(equalTo: self.appleLoginView.centerYAnchor),
                authorizationButton.widthAnchor.constraint(equalToConstant: 245),
                authorizationButton.heightAnchor.constraint(equalToConstant: 55)
                ])
        }
        
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
                
                
                self.showBasicAlert(title: "Attenzione", description: "Impossibile accedere con Apple id. Prova con un altro servizio")
                
                return
            }
            
            //write offline data
            //MARK: Signin with Apple complete
            //redirect to dashboard: Apple privacy policy
            
            let newUser = LismoveUser()
            newUser.uid = authResult?.user.uid
            newUser.email = authResult?.user.email
            
            //handle user data
            self.getUser(user: newUser, appleAccess: true)
            
    
        }
      }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        //MARK: ERROR STREAM
        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
    }
    
    
    @IBAction func facebookLogin(_ sender: Any) {
        let loginManager = LoginManager.init()
        loginManager.logIn(permissions: ["user_photos", "email", "user_birthday", "public_profile"], from: self, handler: {
            result, error in
            guard error == nil else{
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error?.localizedDescription ?? ""])
                return
            }
            if(result == nil || result?.isCancelled == true){
                return
            }
            let req = GraphRequest(graphPath: "me", parameters: ["fields":"email,name"], tokenString: AccessToken.current!.tokenString, version: nil, httpMethod: .get)
            req.start(completionHandler: {connection, result, error in
                if let email = (result as? NSDictionary)?["email"] as? String{
                    self.network.checkUser(email:email) {result in
                        switch result {
                        case .success(let userAlreadyExists):
                            if(userAlreadyExists){
                                self.view.makeToast("Registrazione in corso", duration: 2.0, position: .bottom)
                                let credential = FacebookAuthProvider
                                  .credential(withAccessToken: AccessToken.current!.tokenString)
                                self.loginWithCredential(credential: credential)
                            }else{
                                self.showError(message: "Utente non esistente, effettua la registrazione")
                            }
                            break
                        case .failure(let error):
                            self.showError(message: error.localizedDescription)
                        }
                    }
                }else{
                    self.showError(message: "Si è verificato un errore nel recuperare l'indirizzo email")
                }
            })
            
        })
    }
    
    @IBAction func googleLogin(_ sender: Any) {
        
        GIDSignIn.sharedInstance().signIn()
    }
    
    
    private func getUser(user: LismoveUser, appleAccess: Bool){
        
        //save temporary user into server
        network.getUser(uid: user.uid ?? "") { result in
            switch result {
                case .success(let data):
                    //save into db
                    self.DB.saveUser(user: data)
                    
                    
                    //if apple access, skip validation
                    if(!appleAccess){
                        
                        //save access method
                        UserDefaults.standard.setValue("Other", forKey: "AccessMethod")
                        
                        
                        //check email verified on firebase auth
                        if(Auth.auth().currentUser!.isEmailVerified){
                            
                            //open dashboard
                            let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                            let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
                            DashboardController.modalPresentationStyle = .fullScreen
                            self.present(DashboardController, animated: true, completion: nil)
                            
                        }else{
                            
                            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                            let ConfirmEmailViewController = storyBoard.instantiateViewController(withIdentifier: "ConfirmEmailViewController")
                            ConfirmEmailViewController.modalPresentationStyle = .fullScreen
                                
                            self.present(ConfirmEmailViewController, animated: false, completion: nil)
                        }
                        
                    }else{
                        
                        //save access method
                        UserDefaults.standard.setValue("Apple", forKey: "AccessMethod")
                        
                        
                        //open dashboard
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
                        let alert = UIAlertController(title: "Avviso", message: "Il tuo account non risulta registrato. Accedi alla sezione dedicata per crearne uno, oppure scrivici per problemi", preferredStyle: .alert)
                        if let popoverPresentationController = alert.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
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

/*
 Google auth system
 */

extension LoginViewController{
    
    
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
        guard let authentication = user.authentication else { return }
        let credential = GoogleAuthProvider.credential(withIDToken: authentication.idToken,
                                                          accessToken: authentication.accessToken)
        loginWithCredential(credential: credential)
        
      }
      
        
    }
    
    func loginWithCredential(credential: AuthCredential){
        
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
                                
                                self.saveCurrentUserInfoAndHandleUserData()
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
            
            self.saveCurrentUserInfoAndHandleUserData()
            
          }
        }
    }
    
    func saveCurrentUserInfoAndHandleUserData(){
        if let user = Auth.auth().currentUser{
            //MARK: Signin with Google complete
            let newUser = LismoveUser()
            newUser.uid  = user.uid
            newUser.email = user.email
            
            //handle user data
            self.getUser(user: newUser, appleAccess: false)
        }
       
    }
    
    func showError(message: String){
        PKHUD.sharedHUD.contentView = PKHUDTextView(text:message)
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 1.0) { success in
            // Completion Handler
        }
    }
    
    func sign(_ signIn: GIDSignIn!, didDisconnectWith user: GIDGoogleUser!, withError error: Error!) {
        // Perform any operations when the user disconnects from app here.
        // ...
    }
    
}

