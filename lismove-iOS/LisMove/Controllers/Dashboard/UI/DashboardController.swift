//
//  DashboardController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 04/05/21.
//

import Foundation
import UIKit
import Floaty
import SwiftMessages
import Resolver

class DashboardController: UITabBarController{
    
    //MARK: Managers
    var sessionManager: SessionManager!
    @Injected var userRepository: UserRepositoryImpl
    
    
    //MARK: Services
    private var rotationDaemon: Timer?
    private var flickrDaemon: Timer?
    
    //MARK: Other
    var floaty: Floaty? = nil
    private var flickrLight = true
    
    //check sensor into DB
    let sensor = DBManager.sharedInstance.getLismoveDevice()
    
    //check sensor into DB
    let user = DBManager.sharedInstance.getCurrentUser()
    
    //FLOATY BUTTON
    var start: FloatyItem?
    var pause: FloatyItem?
    var stop: FloatyItem?

    enum UIUserInterfaceIdiom : Int {
        case unspecified
        
        case phone // iPhone and iPod touch style UI
        case pad   // iPad style UI (also includes macOS Catalyst)
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //init session
        self.sessionManager = SessionManager.sharedInstance
        
        //update offline sensor data
        userRepository.updateUserSensor(){
            
            //it's safely to do this here
            self.sessionManager.initCompanionManagers()
        }
        
        //update user info
        userRepository.updateUserLoginData()
        
        initView()
        
        //SESSION observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveSessionStart(_:)), name: NSNotification.Name(rawValue: "SESSION_START"), object: nil)
        //SESSION observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveSessionPause(_:)), name: NSNotification.Name(rawValue: "SESSION_PAUSE"), object: nil)
        //SESSION observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveSessionStop(_:)), name: NSNotification.Name(rawValue: "SESSION_STOP"), object: nil)
        

    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
        
        if(item.title == "Gaming"){
        
            let help = UIAlertController(title: "Gaming", message: "Scegli cosa visualizzare", preferredStyle: .actionSheet)
            
            let action1 = UIAlertAction(title: "Classifiche", style: .default, handler: { (action) -> Void in
            })
            
            let action2 = UIAlertAction(title: "Coppe", style: .default, handler: { (action) -> Void in
                let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                let trophyController = storyBoard.instantiateViewController(withIdentifier: "trophyTableViewController") as! TrophyTableViewController
                trophyController.modalPresentationStyle = .popover
                if let popoverPresentationController = trophyController.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }

                self.present(trophyController, animated: true, completion: nil)
            })
            
            let action3 = UIAlertAction(title: "Premi e incentivi", style: .default, handler: { (action) -> Void in

                let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                let awardsController = storyBoard.instantiateViewController(withIdentifier: "activeAwards") as! UINavigationController
                awardsController.modalPresentationStyle = .popover
                if let popoverPresentationController = awardsController.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }

                self.present(awardsController, animated: true, completion: nil)
            })
            
            let action4 = UIAlertAction(title: "I miei premi", style: .default, handler: { (action) -> Void in

                let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                let awardsController = storyBoard.instantiateViewController(withIdentifier: "myAwards") as! UINavigationController
                awardsController.modalPresentationStyle = .popover
                if let popoverPresentationController = awardsController.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }

                self.present(awardsController, animated: true, completion: nil)
            })
            
            help.addAction(action1)
            help.addAction(action2)
            //help.addAction(action3)
            help.addAction(action4)
            let popover = help.popoverPresentationController
            popover?.sourceView = self.tabBar
            popover?.sourceRect = CGRect(x: 32, y: 32, width: 56, height: 56)
            
            self.present(help, animated: true)
        }
        
        
    }
    
    
    @objc func onDidReceiveSessionStart(_ notification: Notification){

        fab_start()
       
    }
    
    @objc func onDidReceiveSessionPause(_ notification: Notification){
        
        fab_pause()
       
    }
    
    @objc func onDidReceiveSessionStop(_ notification: Notification){

        fab_init()
       
    }

}


extension DashboardController{
    
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        fab_init()
        
        //check apple access
        let access = UserDefaults.standard.string(forKey: "AccessMethod")
        let checkUserComplete = self.userRepository.checkUserProfileCompleted()
        
        if(access == "Apple" && !checkUserComplete){
            // Instantiate a message view from the provided card view layout. SwiftMessages searches for nib
            // files in the main bundle first, so you can easily copy them into your project and make changes.
            let view = MessageView.viewFromNib(layout: .cardView)

            // Theme message elements with the warning style.
            view.configureTheme(.warning)

            // Add a drop shadow.
            view.configureDropShadow()

            // Set message title, body, and icon. Here, we're overriding the default warning
            // image with an emoji character.
            view.configureContent(title: "Attenzione", body: "Completa ora il tuo profilo per accedere a tutti i servizi LisMove", iconImage: nil, iconText: nil, buttonImage: nil, buttonTitle: "Completa", buttonTapHandler: {button in
                
                //open account controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                let navController = storyBoard.instantiateViewController(withIdentifier: "AccountController") as! UINavigationController
                let detailController = navController.topViewController as! AccountViewController
                detailController.isModifyProfileMode = true
                
                self.present(navController, animated: true, completion: nil)
                
            })

            // Increase the external margin around the card. In general, the effect of this setting
            // depends on how the given layout is constrained to the layout margins.
            view.layoutMarginAdditions = UIEdgeInsets(top: 26, left: 26, bottom: 26, right: 26)

            // Reduce the corner radius (applicable to layouts featuring rounded corners).
            (view.backgroundView as? CornerRoundingView)?.cornerRadius = 16

            
            var config = SwiftMessages.Config()
            // Slide up from the bottom.
            config.presentationStyle = .bottom
            // Disable the default auto-hiding behavior.
            config.duration = .forever
            // Dim the background like a popover view. Hide when the background is tapped.
            config.dimMode = .gray(interactive: true)

            // Disable the interactive pan-to-hide gesture.
            config.interactiveHide = false
            
            // Show the message.
            SwiftMessages.show(config: config, view: view)
        }
    }
    
    
    
    //MARK: ============ fab manager
    private func fab_init(){
        let newFloaty = Floaty()
        newFloaty.buttonImage = UIImage(named: "floatingButton")!
        newFloaty.paddingY += 76
        newFloaty.size = 56
        newFloaty.layer.shadowColor = UIColor.gray.cgColor
        newFloaty.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        newFloaty.layer.shadowRadius = 8.0
        newFloaty.layer.shadowOpacity = 1.0
    
    
        newFloaty.friendlyTap = false
        
        //only for first start and open cruscotto
        //user can start directly session
        newFloaty.handleFirstItemDirectly = true
        
        
        //MARK: 2 start animation and invalidate flickr
        self.flickrDaemon?.invalidate()
        self.flickrDaemon = nil
        
        //MARK: 2 start animation and invalidate flickr
        self.rotationDaemon?.invalidate()
        self.rotationDaemon = nil
        

        start = newFloaty.addItem("Avvia/Riprendi", icon: UIImage(named: "icons8-play-48")!, handler: { item in

            
            //MARK: 1. check sensor connected
            if(self.sessionManager.sensorSDK.currentSensor == nil){
                
                //ASK reassociation sensor dialog
                let alert = UIAlertController(title: "Attenzione", message: "Il sensore non sembra essere collegato.", preferredStyle: .alert)
                
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                
                let confirm = UIAlertAction(title: "Riassocia", style: .default, handler: { action in
                    
                    
                    if(self.sensor == nil){
                        
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                        let controller = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! UIPageViewController
                        controller.modalPresentationStyle = .fullScreen
                        self.present(controller, animated: true, completion: nil)
                        
                    }else{
                        
                        var title = ""
                        var message = ""
                        
                        if(SessionManager.sharedInstance.sessionState?.state == SessionState.onGoing){
                            
                            title = "Attenzione: Stoppare la sessione?"
                            message = self.sensor != nil ?  "Rieffettuerai la connessione con il sessione" : "Nessun sensore associato. Vuoi associarne uno?"
                            
                            //stop session & reset sensor
                            SessionManager.sharedInstance.requestStop()
                           // SessionManager.sharedInstance.sensorSDK.stopSession()
                
                        }else{
                            
                            title = "Attenzione"
                            message = "I dati del precedente sensore andranno persi. Vuoi rieffettuare la connessione?"
                        }
                        
                        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
                        
                        if let popoverPresentationController = alertController.popoverPresentationController {
                            popoverPresentationController.sourceView = self.view
                            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                        }
                        
                        
                        let cancel = UIAlertAction(title: "Annulla", style: .default, handler: nil)
                        
                        let confirm = UIAlertAction(title: "Conferma", style: .destructive, handler: {context in
                            
                            SessionManager.sharedInstance.sensorSDK.disconnectSensor(sensorID: self.sensor?.uuid)
                            SessionManager.sharedInstance.sensorSDK.stopScan()
                            
                            //stop connection loop
                            SessionManager.sharedInstance.sensorCompanionManager?.sensorCheckUpdateDaemon?.invalidate()
                            SessionManager.sharedInstance.sensorCompanionManager?.sensorCheckUpdateDaemon = nil
                            
                            //delete sensor
                            NetworkingManager.sharedInstance.deleteSensor(uuid: self.sensor!.uuid!, uid: self.user!.uid!, completion: {result in
                                
                                if result{
                                    //delete from db
                                    //TODO: FIX THIS
                                    DBManager.sharedInstance.deleteDevice()
                                    SessionManager.sharedInstance.sensorSDK.currentSensor = nil
                                    
                                    //show scan view controller
                                    let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                                    let controller = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! UIPageViewController
                                    controller.modalPresentationStyle = .fullScreen
                                    self.present(controller, animated: true, completion: nil)
                                    
                                }else{
                                    
                                    let alert = UIAlertController(title: "Errore", message:
                                            "Impossibile dissociare il sensore. Riprova", preferredStyle: .alert)
                                    if let popoverPresentationController = alert.popoverPresentationController {
                                        popoverPresentationController.sourceView = self.view
                                        popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                                    }
                                    
                                    alert.addAction(UIAlertAction(title: "Conferma", style: .default, handler: nil))
                                    self.present(alert, animated: true, completion: nil)
                                }
                            })
                            
                        })
                        
                        alertController.addAction(cancel)
                        alertController.addAction(confirm)
                        
                        self.present(alertController, animated: true, completion: nil)
                        
                        
                    }
                    
                    
                    
                })
                
                alert.addAction(confirm)
                
                let close = UIAlertAction(title: "Prosegui", style: .destructive, handler: {action in
                    
                    //GPS ONLY SESSION
                    //MARK: 2 start session
                    self.sessionManager.startSession()

                    
                    //MARK: 3 start animation and invalidate flickr
                    self.flickrDaemon?.invalidate()
                    self.flickrDaemon = nil
                            
                    
                    newFloaty.close()
                    
                })
                
                alert.addAction(close)
                
                self.present(alert, animated: true, completion: nil)
      
            }else{
                
                //MARK: 2 start session
                self.sessionManager.startSession()

                
                //MARK: 3 start animation and invalidate flickr
                self.flickrDaemon?.invalidate()
                self.flickrDaemon = nil
                        
                
                newFloaty.close()
            }
            

        })

        floaty?.removeFromSuperview()
        floaty = newFloaty
        self.view.addSubview(floaty!)
  
    }
    
    private func fab_start(){
        
        fab_init()
        
        self.floaty?.removeItem(item: self.start!)
        
        //add cruscotto
        self.floaty?.addItem("Mostra Cruscotto", icon: UIImage(named: "icons8-gps-64")!, handler: { item in
            self.sessionManager.openSessionController()
            
            self.floaty?.close()
        })
        
        pause = self.floaty?.addItem("Pausa", icon: UIImage(named: "icons8-pause-100")!, handler: { item in
            
            //MARK: 1 pause session
            self.sessionManager.pauseSession()
            
            //MARK: 2 stop rotation
            self.rotationDaemon?.invalidate()
            self.rotationDaemon = nil
            
            
            self.floaty?.close()
        })
        
        //MARK: 3 add pausa and stop buttons
        stop = self.floaty?.addItem("Termina e invia", icon: UIImage(named: "icons8-stop-100")!, handler: { item in
            
            //advise user
            let alert = UIAlertController(title: "Attenzione", message: "Terminare e inviare la sessione?", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            let confirm = UIAlertAction(title: "Conferma", style: .default, handler: { action in
                //MARK: 1 stop session
                self.sessionManager.requestStop()
                
                //MARK: 2. remove button
                self.floaty?.removeItem(item: self.stop!)
                self.floaty?.removeItem(item: self.pause!)
                
                
                self.floaty?.close()
            })
            
            
            let close = UIAlertAction(title: "Annulla ed elimina", style: .destructive, handler: {action in
                
                //MARK: 1 stop session
                self.sessionManager.requestStop(cancelSession: true)
                
                //MARK: 2. remove button
                self.floaty?.removeItem(item: self.stop!)
                self.floaty?.removeItem(item: self.pause!)
                
                
                self.floaty?.close()
                
            })
            alert.addAction(close)
            alert.addAction(confirm)
            
            self.present(alert, animated: true, completion: nil)

        })
        
        
        //start animation dameon
        if(self.rotationDaemon == nil){
            self.rotationDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(0.1), repeats: true) { [self] timer in
                
                let image = self.floaty?.buttonImage
                self.floaty?.buttonImage = image?.rotate(radians: .pi/2)

            }
        }
    }
    
    private func fab_pause(){
        
        fab_init()
        
        self.floaty?.removeItem(item: self.start!)
        
        //add cruscotto
        self.floaty?.addItem("Mostra Cruscotto", icon: UIImage(named: "icons8-gps-64")!, handler: { item in
            //open terms controller
            let storyBoard: UIStoryboard = UIStoryboard(name: "Session", bundle: nil)
            let sessionViewController = storyBoard.instantiateViewController(withIdentifier: "sessionViewController") as! SessionViewController
            sessionViewController.modalPresentationStyle = .popover
            if let popoverPresentationController = sessionViewController.popoverPresentationController {
                popoverPresentationController.sourceView = self.floaty!
                popoverPresentationController.sourceRect = CGRect(x: self.floaty!.bounds.size.width / 2.0, y: self.floaty!.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            if(UIDevice.current.userInterfaceIdiom == .pad){
                sessionViewController.preferredContentSize = CGSize(width: UIScreen.main.bounds.width/2, height: UIScreen.main.bounds.height/1.3)
            }

        
            self.present(sessionViewController, animated: true, completion: nil)
            
            
            self.floaty?.close()
        })
        
        //MARK: 3 add pausa and stop buttons
        stop = self.floaty?.addItem("Termina e invia", icon: UIImage(named: "icons8-stop-100")!, handler: { item in
            
            //MARK: 1 stop session
            self.sessionManager.requestStop()
            
            
            self.floaty?.close()
        })
        
        //MARK: 3. start filckr
        if(self.flickrDaemon == nil){
            self.flickrDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(1), repeats: true) { [self] timer in
                
                switch flickrLight{
                    case true:
                        self.floaty?.buttonImage = UIImage(named: "floatingButton")!
                        flickrLight = false
                        
                    case false:
                        self.floaty?.buttonImage = UIImage(named: "fab_dark")!
                        flickrLight = true
                }
            }
        }
    }
    
}
