import UIKit
import Gifu
import FirebaseAuth
import Toast_Swift
import Resolver

class InitialViewController: UIViewController {

    @Injected var authRepository: AuthRepository
    @Injected var userRepository: UserRepository
    @IBOutlet weak var rotateImage: GIFImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        rotateImage.animate(withGIFNamed: "splash_gif", animationBlock:  {})


    }
    
    override func viewDidAppear(_ animated: Bool) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            

            if(UserDefaults.standard.string(forKey: "AccessMethod") == nil){
                
                self.authRepository.logout()
                
                //open splash controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                let SplashController = storyBoard.instantiateViewController(withIdentifier: "SplashController") as! SplashViewController
                SplashController.modalPresentationStyle = .fullScreen
            
                self.present(SplashController, animated: true, completion: nil)
            }
            
            
            
            
            let userId = self.authRepository.getCurrentUser()
            if(userId != nil){
                self.userRepository.getUser(uid: userId!, onCompletition: {
                    user, error in
                    
                    guard error == nil else{
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])
                        return
                    }
                    
                    //advise user
                    UIApplication.shared.topMostViewController()?.view.makeToast("Bentornato \(user?.username ?? user?.email ?? "")", duration: 3.0, position: .bottom)
                    
                    //check apple access
                    let accessMethod = UserDefaults.standard.string(forKey: "AccessMethod")
                    if(accessMethod != "Apple"){
                        if(user!.signupCompleted == false){
                            //open terms controller
                            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                            let termsViewController = storyBoard.instantiateViewController(withIdentifier: "termsController") as! UINavigationController
                            termsViewController.modalPresentationStyle = .fullScreen
                            
                            self.present(termsViewController, animated: true, completion: nil)
                            
                        }else{
                            
                            Auth.auth().currentUser?.reload(completion: {
                                error in
                                if(error != nil){
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error!.localizedDescription])

                                }
                                if(Auth.auth().currentUser!.isEmailVerified){
                                    LogHelper.log(message: self.authRepository.getUserToken(), withTag: "USER-INFO: TOKEN")
                                    LogHelper.log(message: self.authRepository.getCurrentUser() ?? "", withTag: "USER-INFO: UID")
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
                            })
                        }
                    }else{
                        //open dashboard
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
                        let DashboardController = storyBoard.instantiateViewController(withIdentifier: "DashboardController") as! UITabBarController
                        DashboardController.modalPresentationStyle = .fullScreen
                    
                        
                        self.present(DashboardController, animated: true, completion: nil)
                    }
                })
                
                
            
                
            }else{
                //open splash controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                let splashViewController = storyBoard.instantiateViewController(withIdentifier: "SplashController") as! SplashViewController
                splashViewController.modalPresentationStyle = .fullScreen
                
                self.present(splashViewController, animated: false, completion: nil)
            }
        }
    }
    


}
