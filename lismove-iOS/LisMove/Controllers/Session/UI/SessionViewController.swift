//
//  SessionViewController.swift
//  LismoveSensorSkdTestApp
//
//

import UIKit
import CoreLocation
import LisMoveSensorSdk
import CoreBluetooth
import PKHUD
import Floaty
import LabelSwitch
import AVFoundation
import SwiftLog
import Resolver

class SessionViewController: UIViewController{

    
    //MARK: Views
    @IBOutlet weak var sessionView: UIView!
    @IBOutlet weak var alertMessage: UILabel!
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var distanceLabel: UILabel!
    @IBOutlet weak var avgVelocityLabel: UILabel!
    @IBOutlet weak var velocityLabel: UILabel!
    @IBOutlet weak var sessionStatus: UILabel!
    
    @IBOutlet weak var gpsIcon: UIImageView!
    @IBOutlet weak var blwIcon: UIImageView!
    
    @IBOutlet weak var optionsCard: UIView!
    @IBOutlet weak var sensorConfigButton: UIButton!
    @IBOutlet weak var mediaButton: UIView!
    
    @IBOutlet weak var batteryIcon: UIImageView!
    
    //urabn text button
    @IBOutlet weak var urbanText: UILabel!
    @IBOutlet weak var urbanIcon: UIImageView!
    
    //report text button
    @IBOutlet weak var reportIcon: UIImageView!
    @IBOutlet weak var reportText: UILabel!
    
    //point
    @IBOutlet weak var nationalPointLabel: UILabel!
    @IBOutlet weak var projectPointLabel: UILabel!
    @IBOutlet weak var activeBonusLabel: UILabel!
    @IBOutlet weak var activeBonusIcon: UIImageView!
    @IBOutlet weak var projectNumber: UILabel!
    
    @IBOutlet weak var modeLabel: LabelSwitch!
    
    
    @IBOutlet weak var ledButton: UIButton!
    var ledEnabled = false
    
    @IBOutlet weak var clacsonButton: UIButton!
    
    //moltiplicator
    @IBOutlet weak var moltiplicatorNumber: UILabel!
    @IBOutlet weak var moltiplicatorLabel: UILabel!
    
    @IBOutlet weak var achievementLayout: UIStackView!
    @Injected
    var userRepository: UserRepository
    //Style preferences
    var sessionControllerMode = "Light" {
        didSet {
            UserDefaults.standard.set(sessionControllerMode, forKey: "sessionViewTheme")
        }
    }
    
    //MARK: Service
    var player: AVAudioPlayer?
    var alertIconTimer: Timer?
    private var flickrLight = false
    let device = DBManager.sharedInstance.getLismoveDevice()
    let user = DBManager.sharedInstance.getCurrentUser()
    var hasActiveAchievement = false{
        didSet{
            updateAchievementLayout()
        }
    }
    private var rotationDaemon: Timer?
    private var flickrDaemon: Timer?
    
    //MARK: Other
    var floaty: Floaty!
    var start: FloatyItem?
    var pause: FloatyItem?
    var stop: FloatyItem?

    //MARK: Manager
    var sessionManager = SessionManager.sharedInstance
    
    @Injected var fountainRepository: FountainRepository
    
    //MARK: init
    override func viewDidLoad() {
        super.viewDidLoad()

        self.sessionControllerMode = UserDefaults.standard.string(forKey: "sessionViewTheme") ?? "Light"
       
        //Session observer
        NotificationCenter.default.addObserver(self, selector: #selector(onSessionUpdate(_:)), name: NSNotification.Name(rawValue: "SESSION_UPDATE"), object: nil)
        
        //Point observer
        NotificationCenter.default.addObserver(self, selector: #selector(onPointsUpdate(_:)), name: NSNotification.Name(rawValue: "POINTS_UPDATE"), object: nil)
    
        //SESSION observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveSessionStart(_:)), name: NSNotification.Name(rawValue: "SESSION_START"), object: nil)
        
        //SESSION observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveSessionPause(_:)), name: NSNotification.Name(rawValue: "SESSION_PAUSE"), object: nil)
        
        initView()
    }

    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    
    
    override func viewWillDisappear(_ animated: Bool) {
        
        //disable always on
        UIApplication.shared.isIdleTimerDisabled = false
    }
    
    
    
    private func initView(){
        
        
        //init theme
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            switch sessionControllerMode {
            case "Dark":
                overrideUserInterfaceStyle = .dark
                self.modeLabel.curState = LabelSwitchState.R
                
                clacsonButton.imageView?.tintColor = .lightGray
                ledButton.imageView?.tintColor = .lightGray
                
            default:
                overrideUserInterfaceStyle = .light
                self.modeLabel.curState = LabelSwitchState.L
                
                clacsonButton.imageView?.tintColor = .darkGray
                ledButton.imageView?.tintColor = .darkGray
            }
        }
        
        //init always on
        UIApplication.shared.isIdleTimerDisabled = true
        
        //init switch
        self.modeLabel.delegate = self


        //init layout
        sessionView.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.size.width, height: UIScreen.main.bounds.size.height)
        optionsCard.layer.cornerRadius = 16
        optionsCard.layer.shadowColor = UIColor.gray.cgColor
        optionsCard.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        optionsCard.layer.shadowRadius = 8.0
        optionsCard.layer.shadowOpacity = 0.7

        
        sensorConfigButton.layer.cornerRadius = 8
        mediaButton.layer.cornerRadius = 16
    
        //add mapview gesture
        let gestureUrban = UITapGestureRecognizer(target: self, action:  #selector(self.tapActionOnUrban(sender:)))
        let gestureUrban2 = UITapGestureRecognizer(target: self, action:  #selector(self.tapActionOnUrban(sender:)))
        self.urbanText.addGestureRecognizer(gestureUrban)
        self.urbanIcon.addGestureRecognizer(gestureUrban2)
        
        //add report gesture
        let gestureReport = UITapGestureRecognizer(target: self, action:  #selector(self.tapActionOnReport(sender:)))
        let gestureReport2 = UITapGestureRecognizer(target: self, action:  #selector(self.tapActionOnReport(sender:)))
        self.reportIcon.addGestureRecognizer(gestureReport)
        self.reportText.addGestureRecognizer(gestureReport2)
    
        //add molitplicator gesture
        let moltiplicatorReport = UITapGestureRecognizer(target: self, action:  #selector(self.tapMoltiplicator(sender:)))
        let moltiplicatorReport2 = UITapGestureRecognizer(target: self, action:  #selector(self.tapMoltiplicator(sender:)))
        self.moltiplicatorLabel.addGestureRecognizer(moltiplicatorReport)
        self.moltiplicatorNumber.addGestureRecognizer(moltiplicatorReport2)
        
        //add bonus gestire
        let bonusTap = UITapGestureRecognizer(target: self, action:  #selector(self.tapBonus(sender:)))
        let bonusTap2 = UITapGestureRecognizer(target: self, action:  #selector(self.tapBonus(sender:)))
        self.activeBonusLabel.addGestureRecognizer(bonusTap)
        self.activeBonusIcon.addGestureRecognizer(bonusTap2)
        
        updateSessionStatus()
        
        //init iniative label
        self.activeBonusLabel.text = String(self.sessionManager.pointManager?.organizationList.count ?? 0)
        self.projectNumber.text = "Punti Iniziativa (x" + String(self.sessionManager.pointManager?.organizationList.count ?? 0) + ")"
        
        
        fab_init()
        setupAchievementLayout()
        updateAchievementValueAndLayout()
        //check last session to recover from pause
        if let partial = SessionManager.sharedInstance.lastGeneratedPartial {
            SessionManager.sharedInstance.onSessionUpdate(partial: partial)
        }
  
    }
    
    func updateAchievementValueAndLayout(){
        if let userId = user?.uid {
            userRepository.hasActiveAchievement(uid: userId, onCompletition: {result in
                switch(result){
                case .success(let hasActive):
                    self.hasActiveAchievement = hasActive
                    break
                case .failure(_):
                    break
                }
            })

        }
    }
    
    @objc func onDidReceiveSessionStart(_ notification: Notification){
        LogHelper.log(message: "onDidReceiveSessionStart")
        self.flickrDaemon?.invalidate()
        self.flickrDaemon = nil
        
        self.floaty.buttonImage = UIImage(named: "floatingButton")!
        flickrLight = false
        
        //start animation dameon
        if(self.rotationDaemon == nil){
            self.rotationDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(0.1), repeats: true) { [self] timer in
                
                let image = self.floaty.buttonImage
                self.floaty.buttonImage = image!.rotate(radians: .pi/2)

            }
        }
        
        self.fab_start()
    }
    
    @objc func onDidReceiveSessionPause(_ notification: Notification){
        LogHelper.log(message: "onDidReceiveSessionPause", withTag: "SessionViewController")
        self.fab_pause()
    }
    
    
    //MARK: ============ fab manager
    private func fab_init(){
        floaty = Floaty()
        floaty.buttonImage = UIImage(named: "floatingButton")!
        floaty.paddingY += 96
        floaty.size = 56
        floaty.layer.shadowColor = UIColor.gray.cgColor
        floaty.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        floaty.layer.shadowRadius = 8.0
        floaty.layer.shadowOpacity = 1.0
        
        
        floaty.friendlyTap = false
        
        
        //MARK: 2 start animation and invalidate flickr
        self.flickrDaemon?.invalidate()
        self.flickrDaemon = nil
        
        //MARK: 2 start animation and invalidate flickr
        self.rotationDaemon?.invalidate()
        self.rotationDaemon = nil
        
        //hide view
        self.floaty.addItem("Nacondi Cruscotto", icon: UIImage(named: "icons8-gps-64")!, handler: { item in
            
            self.dismiss(animated: true, completion: nil)
            self.floaty.close()
        })
        
        //stop session
        self.floaty.addItem("Termina e invia", icon: UIImage(named: "icons8-stop-100")!, handler: { item in
            
            //advise user
            let alert = UIAlertController(title: "Attenzione", message: "Terminare e inviare la sessione?", preferredStyle: .alert)

            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            let confirm = UIAlertAction(title: "Conferma", style: .destructive, handler: { action in
                
                //MARK: 1 stop session
                self.sessionManager.requestStop()
                
                //MARK: 2 stop rotation
                self.rotationDaemon?.invalidate()
                self.rotationDaemon = nil
                
                self.floaty.close()
                
            })
            
            
            let delete = UIAlertAction(title: "Elimina sessione", style: .default, handler: {action in
                
                //MARK: 1 stop session
                self.sessionManager.requestStop(cancelSession: true)
                
                //MARK: 2 stop rotation
                self.rotationDaemon?.invalidate()
                self.rotationDaemon = nil
                
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                    self.dismiss(animated: true, completion: nil)
                }
                
                
                self.floaty.close()
                
            })
            
            let discard = UIAlertAction(title: "Annulla", style: .cancel, handler: {action in
                alert.dismiss(animated: true, completion: nil)
                self.floaty.close()
            })

            alert.addAction(confirm)
            
            alert.addAction(discard)
            
            alert.addAction(delete)
            
            

            self.present(alert, animated: true, completion: nil)
            
        })
        
        
        let type = UserDefaults.standard.string(forKey: "session")
        
        switch type {
        case "start":
            self.fab_start()
        case "pause":
            self.fab_pause()
        default:
            break
        }
        

        self.view.addSubview(floaty)
       
    }
    
    
    
    
    //MARK: Fab management

    private func fab_start(){
        if(start != nil){
            self.floaty.removeItem(item: start!)
        }
        if(pause != nil){
            self.floaty.removeItem(item: pause!)
        }
        pause = self.floaty.addItem("Pausa", icon: UIImage(named: "icons8-pause-100")!, handler: { item in
            
            //MARK: 1 pause session
            self.sessionManager.pauseSession()
            
            //MARK: 2 stop rotation
            self.rotationDaemon?.invalidate()
            self.rotationDaemon = nil

            
            self.floaty.close()
            
            //pause alert: advise user
            if(!UserDefaults.standard.bool(forKey: "pauseAlert")){

                let alert = UIAlertController(title: "Attenzione", message: "Hai messo in pausa manualmente la sessione, per riavviarla dovrai procedere manualmente. Ti ricordiamo che il sistema può andare in pausa e riavviare la sessione in maniera automatica", preferredStyle: .alert)

                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = self.view
                    popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                
                let confirm = UIAlertAction(title: "Ok", style: .default, handler: nil)
                
                
                let close = UIAlertAction(title: "Non mostrare più", style: .destructive, handler: {action in
                    
                    UserDefaults.standard.set(true, forKey: "pauseAlert")
                })
                

                alert.addAction(confirm)
                alert.addAction(close)
                
                self.present(alert, animated: true, completion: nil)
            }
            
            
        })
        
        
        if(UserDefaults.standard.string(forKey: "session") == "start"){
            //start animation dameon
            if(self.rotationDaemon == nil){
                self.rotationDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(0.1), repeats: true) { [self] timer in
                    
                    let image = self.floaty.buttonImage
                    self.floaty.buttonImage = image!.rotate(radians: .pi/2)

                }
            }
        }
  
    }
    
    
    private func fab_pause(){

        if(pause != nil){
            self.floaty.removeItem(item: pause!)
        }
        if(start != nil){
            self.floaty.removeItem(item: start!)
        }
        
        
        self.flickrDaemon?.invalidate()
        self.flickrDaemon = nil
        
        self.rotationDaemon?.invalidate()
        self.rotationDaemon = nil
        
        
        start = self.floaty.addItem("Riprendi", icon: UIImage(named: "icons8-play-48")!, handler: { item in

            //MARK: 2 start session
            self.sessionManager.requestResume()
            
            //MARK: 3 start animation and invalidate flickr
            self.flickrDaemon?.invalidate()
            self.flickrDaemon = nil
            
            self.fab_start()
            
            self.floaty.close()
        })
     
        
        //MARK: 3. start filckr
        if(self.flickrDaemon == nil){
            self.flickrDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(1), repeats: true) { [self] timer in
                
                switch flickrLight{
                    case true:
                        self.floaty.buttonImage = UIImage(named: "floatingButton")!
                        flickrLight = false
                        
                    case false:
                        self.floaty.buttonImage = UIImage(named: "fab_dark")!
                        flickrLight = true
                }
            }
        }
    }
    
    


    //MARK: Actions
    @objc func tapActionOnUrban(sender : UITapGestureRecognizer) {
        //open maps view controller
        //open dashboard
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let mapController = storyBoard.instantiateViewController(withIdentifier: "MapViewController") as! UINavigationController
    
        
        self.present(mapController, animated: true, completion: nil)
        
    }
    
    @objc func tapActionOnReport(sender : UITapGestureRecognizer) {
        //open maps view controller
        //open dashboard
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "GMSAutocompleteNavigation") as! UINavigationController
        
        let detailController = navController.topViewController as! GMSAutocompleteViewController
        detailController.modalPresentationStyle = .fullScreen

        detailController.addressLatLng = (self.sessionManager.lat,self.sessionManager.lng)
        
        detailController.onDoneBlock = { result in
            
            self.dismiss(animated: true, completion: nil)
        
            self.view.makeToast("Caricamento...")
            
            let latLng = ((result.0)?.0 , (result.0)?.1)
            
            //write new fountain on firebase
            let newFountain = Fountain(name: "", lat: latLng.0!, lng: latLng.1!, uid: self.user!.uid!, createdAt: DateTimeUtils.getCurrentTimestamp())
            self.fountainRepository.addFountain(fountain: newFountain, onCompletition: {
            error in
                let message = (error == nil) ? "Fontanella segnalata correttamente" : error!.localizedDescription
                self.view.makeToast(message)
            })
   
        }
        
        self.present(navController, animated: true, completion: nil)
        
    }
    
    
    
    @objc func tapBonus(sender : UITapGestureRecognizer) {

        //open initiative controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let termsViewController = storyBoard.instantiateViewController(withIdentifier: "BonusViewController") as! UINavigationController
        termsViewController.modalPresentationStyle = .popover
        
        if let popoverPresentationController = termsViewController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        self.present(termsViewController, animated: true, completion: nil)
        
        
    }
    
    
    @objc func tapMoltiplicator(sender : UITapGestureRecognizer) {
        let alertController = UIAlertController(title: "Attenzione", message:
                "Non ci sono moltiplicatori attivi per fasce orarie o periodi dell'anno", preferredStyle: .alert)
        if let popoverPresentationController = alertController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        let confirm = UIAlertAction(title: "Ok", style: .destructive, handler: nil)
        alertController.addAction(confirm)
        
        self.present(alertController, animated: true, completion: nil)
        
    }
    
    
    @IBAction func onClacsonTap(_ sender: Any) {
        
        //add light to clacson icon
        clacsonButton.imageView?.tintColor = .orange
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.clacsonButton.imageView?.tintColor = .darkGray
        }
        
        
        guard let url = Bundle.main.url(forResource: "bycycle_bell_ring", withExtension: "mp3") else { return }

        do {
            try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)

            player = try AVAudioPlayer(contentsOf: url, fileTypeHint: AVFileType.mp3.rawValue)

            guard let player = player else { return }

            player.play()

        } catch let error {
            print(error.localizedDescription)
        }
    }

    @IBAction func onLedTap(_ sender: Any) {
        
        //add light to led icon
        self.ledEnabled = !self.ledEnabled
        
        if(self.ledEnabled){
            self.ledButton.imageView?.tintColor = .orange
        }else{
            self.ledButton.imageView?.tintColor = .darkGray
        }
        
        guard let device = AVCaptureDevice.default(for: AVMediaType.video) else { return }
        guard device.hasTorch else { return }

        do {
            try device.lockForConfiguration()

            if (device.torchMode == AVCaptureDevice.TorchMode.on) {
                device.torchMode = AVCaptureDevice.TorchMode.off
            } else {
                do {
                    try device.setTorchModeOn(level: 1.0)
                } catch {
                    print(error)
                }
            }

            device.unlockForConfiguration()
        } catch {
            print(error)
        }
    }
    
    
    //MARK: Observables
    
    @objc func onPointsUpdate(_ notification: Notification) {
        print("SessionViewController: onPointsUpdate ")

        let data = notification.userInfo as! [String: [String:Int]]
        let pointData = data["points"]!
        
        let tot = pointData["tot"]
        let initiative = pointData["initiative"]
        
        //update ui
        self.nationalPointLabel.text = String(tot ?? 0)
        self.projectPointLabel.text = String(initiative ?? 0)
        
        
    }
    
    @objc func onSessionUpdate(_ notification: Notification) {
        print("SessionViewController: onSessionUpdate ")

        let data = notification.userInfo as! [String: SessionUpdateUI?]
        let session = data["session"]!
        
        self.updateUI(session: session)

        updateSessionStatus()
    }
    
    
    private func updateUI(session: SessionUpdateUI?){
        
        if session != nil{
            self.activeBonusLabel.text = String(self.sessionManager.pointManager?.organizationList.count ?? 0)
            self.projectNumber.text = "Punti Iniziativa (x" + String(self.sessionManager.pointManager?.organizationList.count ?? 0) + ")"

            timeLabel.text = session!.time
            velocityLabel.text = String(session!.speedValue.rounded())
            avgVelocityLabel.text = String(session!.avgSpeedValue.rounded())
            distanceLabel.text = String((session!.distanceinKm).rounded(toPlaces: 3))
            
            if(session!.batteryLevel != nil){
                
                batteryIcon.isHidden = false
                
                if(session!.batteryLevel! > 76){
                    batteryIcon.image = UIImage(named: "battery_full")
                }else if(session!.batteryLevel! >= 51 && session!.batteryLevel! <= 75){
                    batteryIcon.image = UIImage(named: "battery_mid")
                }else{
                    batteryIcon.image = UIImage(named: "battery_low")
                }
            }else{
                
                batteryIcon.isHidden = true
            }
            
        }
    }

}

extension SessionViewController: LabelSwitchDelegate{
    
    private func updateSessionStatus(){
        //init session type
        switch sessionManager.sessionState?.state {
        case .onGoing:
            self.sessionStatus.text! = "SESSIONE IN CORSO"
            self.sessionStatus.textColor = .green

        case .finished:
            self.sessionStatus.text! = "SESSIONE TERMINATA"
            self.sessionStatus.textColor = .red
            
        case .paused,
             .automaticPause:
            self.sessionStatus.text! = "SESSIONE IN PAUSA"
            self.sessionStatus.textColor = .orange
            
        default:
            self.sessionStatus.text! = "NUOVA SESSIONE"
        }
        
//        //set value
//        if(self.sessionManager.sessionState != nil){
//            self.timeLabel.text! = self.sessionManager.sessionState!.time
//            self.distanceLabel.text = self.sessionManager.sessionState!.distance
//
//        }
        
        //set device
        self.setDevice(name: self.device?.name, connected: SessionManager.sharedInstance.sensorSDK.currentSensor != nil)
        
        //check sensor connection
        if(SessionManager.sharedInstance.sensorSDK.currentSensor == nil){
            
            self.batteryIcon.isHidden = true

            self.alertMessage.isHidden = false
            
            if(self.alertIconTimer == nil){
                self.alertIconTimer = Timer.scheduledTimer(withTimeInterval: TimeInterval(1), repeats: true) { [self] timer in
                    
                    switch flickrLight{
                        case true:
                        self.alertMessage.textColor = .black
                            flickrLight = false
                            
                        case false:
                        self.alertMessage.textColor = .systemYellow
                            flickrLight = true

                    }
                }
            }
            
        }else{
            
            self.alertMessage.isHidden = true
            
            self.batteryIcon.isHidden = false
            self.alertIconTimer?.invalidate()
            self.alertIconTimer = nil
            
        }
        
        //set blw icon
        if(SessionManager.sharedInstance.sensorCompanionManager!.bluetoothStatus){
            self.blwIcon.tintColor = .systemGreen
        }else{
            self.blwIcon.tintColor = .systemRed
        }
        
        //set gps and blw icon
        if(SessionManager.sharedInstance.sensorCompanionManager!.gpsStatus){
            self.gpsIcon.tintColor = .systemGreen
        }else{
            self.gpsIcon.tintColor = .systemRed
        }
        
    }
    
    
    
    func switchChangToState(sender: LabelSwitch) {
        
        if(sender == self.modeLabel){
            
            switch sender.curState {
                case .L:
                    
                    if #available(iOS 13.0, *) {
                        self.sessionControllerMode = "Light"
                        overrideUserInterfaceStyle = .light
                        
                        clacsonButton.imageView?.tintColor = .darkGray
                        ledButton.imageView?.tintColor = .darkGray
                        
                        
                    } else {
                        // Fallback on earlier versions
                    }
                
                    
                    
                case .R:
                    
                    if #available(iOS 13.0, *) {
                        self.sessionControllerMode = "Dark"
                        overrideUserInterfaceStyle = .dark
                        
                        clacsonButton.imageView?.tintColor = .lightGray
                        ledButton.imageView?.tintColor = .lightGray
                        

                        
                        
                    } else {
                        // Fallback on earlier versions
                    }
            }
            
        }
        
    }
    
    
    
    func setDevice(name: String?, connected: Bool?){
        if let name = name{
            sensorConfigButton.isHidden = false
            
            sensorConfigButton.setTitle(name, for: .normal)
            if(connected ?? false){
                sensorConfigButton.backgroundColor = UIColor.systemGreen
                sensorConfigButton.setImage(UIImage(named: "icons8-bluetooth-40"), for: .normal)
            }else{
                sensorConfigButton.backgroundColor = UIColor.systemGray3
                sensorConfigButton.setImage(UIImage(named: "icons8-bluetooth-40"), for: .normal)

            }
            
        }else{
            sensorConfigButton.isHidden = true
        }
    }
    
    func updateAchievementLayout(){
        achievementLayout.isHidden = !hasActiveAchievement
        
    }

    func setupAchievementLayout(){
        let gestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(openAchievement))
        achievementLayout.addGestureRecognizer(gestureRecognizer)
    }
    
    @objc func openAchievement(){
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let trophyController = storyBoard.instantiateViewController(withIdentifier: "trophyTableViewController") as! TrophyTableViewController
        trophyController.modalPresentationStyle = .popover
        if let popoverPresentationController = trophyController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }

        self.present(trophyController, animated: true, completion: nil)
    }
}

