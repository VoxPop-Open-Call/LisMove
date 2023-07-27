//
//  ProfileTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 30/05/21.
//

import UIKit
import FirebaseAuth
import Resolver
import nanopb
import FirebaseStorage
import Kingfisher

class ProfileTableViewController: UITableViewController {

    
    //UI
    @IBOutlet weak var userName: UILabel!
    @IBOutlet weak var userEmail: UILabel!
    
    @IBOutlet weak var userProfileImage: UIImageView!
    
    
    @IBOutlet weak var logoutButton: UIButton!
    
    //cell
    @IBOutlet weak var sessionCell: UITableViewCell!
    @IBOutlet weak var workSessionCell: UITableViewCell!
    @IBOutlet weak var myAwards: UITableViewCell!
    
    @IBOutlet weak var profileCell: UITableViewCell!

    //Data
    let user = DBManager.sharedInstance.getCurrentUser()
    var imagePicker: ImagePickerManager?
    
    @Injected var authRepository: AuthRepositoryImpl
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //init picker manager
        DispatchQueue.main.async {
            self.imagePicker =  ImagePickerManager()
        }
        
        //init view
        initView()
        
    }
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
 
        userName.text = user?.username ?? ""
        
        userEmail.text = user?.email ?? ""
        
        //init logout style button
        logoutButton.layer.cornerRadius = 8
        
        
        //add profile image tap gesture
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleImageTap(_:)))
        userProfileImage.addGestureRecognizer(tap)
        
        
        //load profile image
        userProfileImage.kf.setImage(with: URL(string: self.user!.avatarURL ?? ""), placeholder: UIImage(named: "floatingButton"))
        userProfileImage.round()
        
        
    
    }
    
    @objc func handleImageTap(_ sender: UITapGestureRecognizer? = nil) {
        
        imagePicker?.pickImage(self){ image in
            
            //show loading on image
            self.view.makeToast("Caricamento immagine profilo")
            
            //save offline
            self.uploadMedia(image: image, completion: { url in
                
                do{
                    
                    try DBManager.sharedInstance.getDB().safeWrite {
                        self.user?.avatarURL = url
                    }
                    
                }catch{}
                
                
                //update user
                NetworkingManager.sharedInstance.updateUser(user: self.user!, completion: { result in
                    
                    //show loading on image
                    self.view.makeToast("Caricamento completato")
                    
                    //set image
                    self.userProfileImage.image = image
                    self.userProfileImage.round()
                    
                })

                
            })

        }
        
    }
    

    
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return 5
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    
        if(indexPath.row == 0){
            
            //open account controller
            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "AccountController") as! UINavigationController
            let detailController = navController.topViewController as! AccountViewController
            detailController.isModifyProfileMode = true
            
            present(navController, animated: true, completion: nil)
            
        }else if(indexPath.row == 1){
            let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "sessionController") as! UINavigationController
            
            let detailController = navController.topViewController as! SessionTableViewController
            detailController.sessionTypeSelected = SessionType.all
            detailController.modalPresentationStyle = .fullScreen
            
            present(navController, animated: true, completion: nil)
            
           
            
        }else if(indexPath.row == 2){
            let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "sessionController") as! UINavigationController
            
            let detailController = navController.topViewController as! SessionTableViewController
            detailController.sessionTypeSelected = SessionType.work
            detailController.modalPresentationStyle = .fullScreen
            
            present(navController, animated: true, completion: nil)
           
            
        }else if(indexPath.row == 3){
            let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
            let awardsController = storyBoard.instantiateViewController(withIdentifier: "myAwards") as! UINavigationController
            awardsController.modalPresentationStyle = .popover
            if let popoverPresentationController = awardsController.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }

            self.present(awardsController, animated: true, completion: nil)
           
        }
        
        tableView.deselectRow(at: indexPath, animated: true)
    }

    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        
        return 72
    }
    

    

    
    @IBAction func logout(_ sender: Any) {
        
        authRepository.logout()
        
        //open terms controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        let SplashController = storyBoard.instantiateViewController(withIdentifier: "SplashController") as! SplashViewController
        SplashController.modalPresentationStyle = .fullScreen
    
        self.present(SplashController, animated: true, completion: nil)
        
    }
    
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {

        self.view.endEditing(true)
        return false
    }
    
    
    
}


//Firebase utils function
extension ProfileTableViewController{
    

    func uploadMedia(image: UIImage, completion: @escaping (_ url: String?) -> Void) {

        //resize image to avoid big data upload
        let resizedImage = image.resizeImage(512, opaque: false)
        
        let storageRef = Storage.storage().reference().child("users/avatars/\(user!.uid!)/\(Date().millisecondsSince1970).png")
        
        if let uploadImageData = resizedImage.pngData(){
            storageRef.putData(uploadImageData, metadata: nil, completion: { (metaData, error) in
                storageRef.downloadURL(completion: { (url, error) in
                    if let urlText = url?.absoluteString {

                        completion(urlText)
                    }
                })
            })
        }
        
        
        
    }
    
    
}
