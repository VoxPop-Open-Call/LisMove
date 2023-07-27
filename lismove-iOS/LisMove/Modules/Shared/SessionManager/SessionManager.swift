//
//  SessionViewModel.swift
//  Lis Move
//
//

import Foundation
import LisMoveSensorSdk
import CoreBluetooth
import UIKit
import PKHUD
import SwiftLocation
import SwiftLog
import RealmSwift
import CoreLocation
import Bugsnag
import AVFoundation
import Resolver
import CryptoKit
import RxSwift

class SessionManager{

    
    //MARK: Singleton
    private static var privateSharedInstance: SessionManager? = nil
    static var sharedInstance: SessionManager {
        return getSharedInstance()
    }
    var TAG = "sessionManager"
    
    //MARK: Managers Always active
    var sensorSDK: LisMoveSensorSDK!
    var gpsManager: GpsScanner?
    var sensorCompanionManager: SensorCompanionManager?

    //MARK: Session Dependant Manager
    var pointManager: PointManager?
    var sensorDataManager: SensorDataManager?
    var coordinateSubscriber: GPSLocationRequest?
    
    private var writePartialOnDbTimer: Timer?
    var sessionDelay = 5.0
    private var automaticPauseSecond = 0
    
    var distanceFormatter:LengthFormatter = {
      
      let formatter = LengthFormatter()
      formatter.numberFormatter.maximumFractionDigits = 1
      
      return formatter
    }()

    //MARK: Session Data
    //session state
    public var sessionState: SessionUpdateUI? = nil
    
    //user position
    var lat: Double? = nil
    var lng: Double? = nil
    var altitude = 0.0
    var gpsTimestamp = Date()
    
    //GPS mode
    //MARK: is updated autopmatically from sensor companion manager
    var isGpsMode = false
    
    //current session
    var session: Session? = nil
    var lastCadenceEntity: CadencePrimitive? = nil
    var lastGeneratedPartial: Partial? = nil
    var lastSavedPartial: Partial? = nil
    var partialCache: [Partial] = []
    var isFirstPartial = true
    var isFirstStart = true
    
    private var generatebatteryDebugTimer: Timer?
    var generatePartialTaskTimer: Timer?
    //difference for timer
    var diff: DateComponents!
    
    
    var player: AVAudioPlayer?
    
    
   
    
    //MARK: init methods
    public init(){
        LogHelper.log(message: "initCompanionManagers", withTag: TAG)
        
        //init gps manager
        self.gpsManager = GpsScanner()
        
        //init pinbikSdkManager
        self.sensorSDK = LisMoveSensorSDK() //TODO forse questo sparisce
        self.sensorSDK.setCadenceSensorDelegate(self)
    }
    
    
    public func initCompanionManagers(){


        //init sensor companion manager
        self.sensorCompanionManager = SensorCompanionManager()
        self.sensorCompanionManager?.initSensorConnection()
    }

    static func getSharedInstance() -> SessionManager{
       
        if(privateSharedInstance == nil){
            privateSharedInstance = SessionManager()
            NetworkingManager.sharedInstance.syncWithServer()
        }
            //better but dangerous
            //privateSharedInstance?.initCompanionManagers()
        return privateSharedInstance!
    }

    
}

//MARK: SESSION METHODS
extension SessionManager{
    

    func startSession(){

        //checks
        if(needFirmwareUpdate()){
            dismissStartAndShowFirmwareUpdateAlert()
        }else if(gpsNotAvailable()){
            dismissStartAndShowGpsAlert()
        }else{
            startSessionAterCheck()
        }
        
        
    }
    
    private func startSessionAterCheck(){
        
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: "SESSIONE AVVIATA")
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 2.0)
        
        
        self.initializeSession()
        
        
    }
    
    public func openSessionController() {
        //open session controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Session", bundle: nil)
        let sessionViewController = storyBoard.instantiateViewController(withIdentifier: "sessionViewController") as! SessionViewController
        sessionViewController.modalPresentationStyle = .popover
        if let popoverPresentationController = sessionViewController.popoverPresentationController {
            popoverPresentationController.sourceView = UIApplication.shared.topMostViewController()?.view
            popoverPresentationController.sourceRect = CGRect(x: UIApplication.shared.topMostViewController()!.view.bounds.size.width / 2.0, y: UIApplication.shared.topMostViewController()!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        if(UIDevice.current.userInterfaceIdiom == .pad){
            sessionViewController.preferredContentSize = CGSize(width: UIScreen.main.bounds.width/2, height: UIScreen.main.bounds.height/1.3)
        }
        
        
        UIApplication.shared.topMostViewController()?.present(sessionViewController, animated: true, completion: nil)
        
    }
    
    private func gpsNotAvailable() -> Bool {
        return (SwiftLocation.authorizationStatus == .denied || SwiftLocation.authorizationStatus == .restricted || SwiftLocation.authorizationStatus == .notDetermined)
    }
    
    private func dismissStartAndShowGpsAlert(){
        SwiftLocation.requestAuthorization(completion: { response in
            if(response == .denied || response == .restricted || response == .notDetermined){
                let controller = UIApplication.shared.topMostViewController()
                let alert = UIAlertController(title: "Attenzione", message: "Autorizzazioni Gps negate. Passa dalle impostazioni per cambiarle", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = controller!.view
                    popoverPresentationController.sourceRect = CGRect(x: controller!.view.bounds.size.width / 2.0, y: controller!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                controller!.present(alert, animated: true, completion: nil)
            }
        })
    }
    
    private func needFirmwareUpdate()->Bool{
        return UserDefaults.standard.bool(forKey: "sensorUpdate") == true

    }
    
    private func dismissStartAndShowFirmwareUpdateAlert(){
        let controller = UIApplication.shared.topMostViewController()
        let alert = UIAlertController(title: "Attenzione", message: "Per avviare una sessione dovrai prima aggiornare il sensore", preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = controller!.view
            popoverPresentationController.sourceRect = CGRect(x: controller!.view.bounds.size.width / 2.0, y: controller!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        alert.addAction(UIAlertAction(title: "Annulla", style: .destructive, handler: {_ in
            return
        }))
        alert.addAction(UIAlertAction(title: "Conferma", style: .default, handler: {_ in
           
            //open new controller for start update
            let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
            let updateController = storyBoard.instantiateViewController(withIdentifier: "updateController") as! SensorUpdateViewController
            updateController.modalPresentationStyle = .popover
            if let popoverPresentationController = updateController.popoverPresentationController {
                popoverPresentationController.sourceView = UIApplication.shared.topMostViewController()!.view
                popoverPresentationController.sourceRect = CGRect(x: UIApplication.shared.topMostViewController()!.view.bounds.size.width / 2.0, y: UIApplication.shared.topMostViewController()!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            UIApplication.shared.topMostViewController()!.present(updateController, animated: true, completion: nil)
            
        }))
        controller!.present(alert, animated: true, completion: nil)
    }
    
    

    private func checkEndTime() {

        if self.session?.endTime.value == nil{
            
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                    self.session?.endTime.value = Date().millisecondsSince1970
                }
            }catch{}
            
        }
    }
    
    //check if session has partials and min distance is more than 500mt
    private func checkSessionBeforeSendToServer() {

        if ((self.session?.getNonDebugPartials().count ?? 0 > 0) &&
                (self.session?.getTotalKM() ?? 0 > 0.5)) {
            
            self.checkEndTime()
            
            self.sendSessionToServer()
            
        }else{
            
            let controller = UIApplication.shared.topMostViewController()
            let alert = UIAlertController(title: "Attenzione", message: "Il minimo per una sessione valida è di 500 metri, quindi la sessione che vuoi inviare non ha sviluppato nessun punteggio valido", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = controller!.view
                popoverPresentationController.sourceRect = CGRect(x: controller!.view.bounds.size.width / 2.0, y: controller!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            alert.addAction(UIAlertAction(title: "ok", style: .destructive, handler: {_ in
                Bugsnag.leaveBreadcrumb(withMessage: "Request cancellation, session distance was less than 500 m")
                self.requestStop(cancelSession: true)
            }))
            
            controller!.present(alert, animated: true, completion: nil)
            
        }
    }
    
    func requestStop(cancelSession: Bool = false){
        LogHelper.log(message: "requestStop", withTag: TAG)
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: "SESSIONE TERMINATA")
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 2.0) { success in }
        
        self.stopSession()
        
        if(cancelSession){
            Bugsnag.leaveBreadcrumb(withMessage: "Request cancellation, debug dump: \(sensorDataManager?.getDebugDump() ?? "NA")")
            let exception = NSException(name:NSExceptionName(rawValue: "Delete current session"),
                                        reason:"Deleting Session",
                                        userInfo:nil)
           Bugsnag.notify(exception)
           deleteCurrentSessionAndClearSessionData()

        }else{
            
           checkSessionBeforeSendToServer()

        }
    
    }
    
    func deleteCurrentSessionAndClearSessionData(){
        
        do{
            try DBManager.sharedInstance.getDB().safeWrite {
                
                if let partials = self.session?.partials{
                    DBManager.sharedInstance.getDB().delete(partials)
                }
               
                if let points = self.session?.sessionPoints{
                    DBManager.sharedInstance.getDB().delete(points)
                }
                
                if let session = self.session{
                    DBManager.sharedInstance.getDB().delete(session)
                }
            }
        }catch{
            
        }
        
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: "SESSIONE ANNULLATA")
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 2.0) { success in
            
            // Completion Handler
            UIApplication.shared.topMostViewController()?.dismiss(animated: true, completion: nil)
        }
        
        self.cleanSessionData()

    }
    
    func requestResume(){
        LogHelper.log(message: "requestResume", withTag: TAG)
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: "SESSIONE RIPARTITA")
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in }
        self.resumeSession(isFirstTime: false)
    }
    

    private func sendSessionToServer(){
        
        
        //send data
        let data = self.session!.detached()
        if data.gyroDistance == 0.0 {
            Bugsnag.leaveBreadcrumb(withMessage: sensorDataManager?.getDebugDump() ?? "")
            
            let exception = NSException(name:NSExceptionName(rawValue: "SessionUploadError"),
                                        reason:"Sending session with gyro null",
                                        userInfo:nil)
            Bugsnag.notify(exception)
        }
        
        NetworkingManager.sharedInstance.saveSession(session: data){result in
            switch result{
            
            case .success(let sessionData):
                    
                    PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Sessione inviata al server")
                    PKHUD.sharedHUD.show()
                    PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
                        // Completion Handler
                    }
                    
                    do{
                        try DBManager.sharedInstance.getDB().safeWrite {
                            

                            self.session?.sendToServer = true
                            if let partials = self.session?.partials{
                                DBManager.sharedInstance.getDB().delete(partials)
                            }
                            if let points = self.session?.sessionPoints{
                                DBManager.sharedInstance.getDB().delete(points)

                            }
                            self.session?.partials.removeAll()
                           
                            self.session? = sessionData
                        }
                    }catch{
                
                    }
                
                self.dismissAndOpenDetailView()
                
                self.cleanSessionData()

                    
                    
                    
            case .failure(_):
                
                let jsonData = try! JSONEncoder().encode(data)
                let jsonString = String(data: jsonData, encoding: String.Encoding.utf8)

                
                //MARK: ERROR STREAM
                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Errore durante l'invio della sessione. Verrà riprovato in background al prossimo avvio dell'app", "json": jsonString])
                
                self.cleanSessionData()
                    
            }
        }
    }
    
    func pauseSession(){
        LogHelper.log(message: "pauseSession", withTag: TAG)
        PKHUD.sharedHUD.contentView = PKHUDTextView(text: "SESSIONE IN PAUSA")
        PKHUD.sharedHUD.show()
        PKHUD.sharedHUD.hide(afterDelay: 3.0) { success in
            // Completion Handler
        }
        
        self.pauseByUser()

    }

    
    func recoverPreviousSession(lastSession: Session, success: (() -> Void)? , cancel: (() -> Void)?){
        
        //MARK: 1. load old session
        self.session = lastSession
        
        //MARK: 2. check 3 hour treshold
        let time: Int64!
        if(self.session?.partials.count ?? 0 > 0){
            time = self.session?.partials.last!.timestamp.value
        }else{
            time = self.session?.startTime.value!
        }
        
        let pauseDiffComponent = Calendar.current.dateComponents([.hour, .minute, .second], from: Date(timeIntervalSince1970: TimeInterval(time) / 1000), to: Date())
        let date = Calendar.current.date(from: pauseDiffComponent)
        
        if(date != nil){
            if(Calendar.current.component(.hour, from: date!) >= 3){
                //MARK: Update last partial to END status
                do{
                    try? DBManager.sharedInstance.getDB().safeWrite {
                        //set last partial to pause
                        addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Session.rawValue, extra: "Session restored after more than 3 hours of inactivity")
                        self.session?.partials.last?.type = Partial.PartialType.End.rawValue
                        //TODO: Maybe stop

                    }
                }catch{}
                
                //advise user
                let controller = UIApplication.shared.topMostViewController()
                let alert = UIAlertController(title: "Sessione scaduta", message: "Non stai pedalando da più di tre ore, la sessione è stata automaticamente terminata", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = controller!.view
                    popoverPresentationController.sourceRect = CGRect(x: controller!.view.bounds.size.width / 2.0, y: controller!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                }
                alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: {[weak self] _ in
                    //TODO: CHECK THIS (HAS BEEN REMOVED)
                    //cancel?()
                }))
                controller!.present(alert, animated: true, completion: nil)
                Bugsnag.leaveBreadcrumb(withMessage: "Recovering Session: Session Expired, cancel")
                LogHelper.log(message: "Recovering Session: Session Expired, cancel", withTag: self.TAG)
                cancel?()
                
            }else{
                
                //Update last partial to PAUSE status
                do{
                    try? DBManager.sharedInstance.getDB().safeWrite {
                        //set last partial to pause
                        //TODO: Maybe restart
                        self.session?.partials.last?.type = Partial.PartialType.Pause.rawValue
                        
                    }
                }catch{}
            
                Bugsnag.leaveBreadcrumb(withMessage: "Recovering Session: success")
                LogHelper.log(message: "Recovering Session: success", withTag: self.TAG)
                success?()
            }
            
        }else{
            addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Session.rawValue, extra: "Error (CODE 3) while recovering session")
            Bugsnag.leaveBreadcrumb(withMessage: "Recovering Session: Date is nil, cancel")
            LogHelper.log(message: "Recovering Session: Date is nil, cancell", withTag: self.TAG)
            cancel?()
        }
    }
}


//MARK: DELEGATE EXTENSION
extension SessionManager{
  
    func onSessionUpdate(partial: Partial) {
        
        LogHelper.log(message: "onSessionUpdate \(session!.sessionSDkState)", withTag: TAG)
        
        let sessionState = SessionUpdateUI(
            state: SessionState.getFromInt(value: session!.sessionSDkState.value ?? 0),
            time: partial.getReadableElapsedTime(),
            distance: distanceFormatter.string(fromMeters: partial.getTotalDistance()),
            distanceinKm: partial.getTotalDistance(),
            avgSpeed: String(partial.averageSpeed),
            speed: String(partial.speed),
            speedValue: partial.speed,
            avgSpeedValue: partial.averageSpeed,
            multiplier: "x1",
            placeType: partial.urban ? "INIZIATIVA" : "COMMUNITY",
            deltaRevs: UInt32(partial.deltaRevs)
        )
        
        //MARK: SESSION STREAM
        self.sessionState = sessionState
        handleAutomaticPauseCountDown()
        
        NotificationCenter.default.post(name: Notification.Name("SESSION_UPDATE"), object: nil, userInfo: ["session": sessionState])
    }
    
    
    func handleAutomaticPauseCountDown(){
        /*
        if let sencondsTillPause = sensorDataManager?.secondsTillPause, sencondsTillPause <= 3, sencondsTillPause > 0{
            PKHUD.sharedHUD.contentView = PKHUDTextView(text: "Pausa automatica tra \n\(sencondsTillPause)\nsecondi")
            PKHUD.sharedHUD.contentView.tag = 0
            PKHUD.sharedHUD.show()
        }else{
            if(PKHUD.sharedHUD.isVisible && PKHUD.sharedHUD.contentView.tag == 0){
                PKHUD.sharedHUD.hide()
            }
        }
         
        LogHelper.log(message: "\(sensorDataManager?.secondsTillPause ?? -1)", withTag: "COUNTDOWN")
         */
         
    }
    
    
}


extension SessionManager: CadenceSensorDelegate{
    
    func errorDiscoveringSensorInformation(_ error: NSError) {
        LogHelper.log(message: "candenceError", withTag: TAG)
    }
    
    
    func sensorUpdatedPrimitive(primitive: CadencePrimitive) {
        LogHelper.log(message: "Cadence Primitive \(primitive)", withTag: TAG)
        if(isGpsMode){
            isGpsMode = false
            addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Session.rawValue, extra: "Received a cadence primitive in gpsMode; automatically toggle gpsMode = false, revs: \(primitive.wheelRevs)")
            
        }
        if let firmwareVersion = sensorSDK.currentSensor?.firmwareVersion{
            sensorDataManager?.currentSensorFirmware = firmwareVersion
        }
        self.lastCadenceEntity = primitive
    }

}



extension SessionManager {
    
    private func initStartedSessionTimers(){
        scheduleGeneratePartialTaskTimer()
        scheduleWriteondbTaskTimer()
        scheduleGenerateBatteryDebugTaskTimer()
    }
    
    
    
    private func scheduleGeneratePartialTaskTimer() {
        //1. generate partial -> 1s
        if(self.generatePartialTaskTimer == nil){
            self.generatePartialTaskTimer = Timer(timeInterval: 1.0, target: self, selector: #selector(generatePartialTask), userInfo: nil, repeats: true)
            RunLoop.current.add(self.generatePartialTaskTimer!, forMode: .common)
        }
    }
    
     
    
    private func scheduleGenerateBatteryDebugTaskTimer() {
        //1. generate partial -> 1s
        if(self.generatebatteryDebugTimer == nil){
            LogHelper.log(message: "GenerateBatteryDebugTaskTimer", withTag: TAG)
            let batteryTimerValue = 3.0 * 60
            self.generatebatteryDebugTimer = Timer(timeInterval: batteryTimerValue, target: self, selector: #selector(generatBatteryDebugPartialTask), userInfo: nil, repeats: true)
            RunLoop.current.add(self.generatebatteryDebugTimer!, forMode: .common)
        }
    }
    
    private func scheduleWriteondbTaskTimer() {
        //2. start DB write dameon -> 5s
        //write On DB
        if(UserDefaults.standard.object(forKey: "sessionDelayUpdate") != nil){
            self.sessionDelay = UserDefaults.standard.double(forKey: "sessionDelayUpdate")
        }
        
        if(self.writePartialOnDbTimer == nil){
            LogHelper.log(message: "scheduleWriteondbTaskTimer", withTag: TAG)
            self.writePartialOnDbTimer = Timer(timeInterval: TimeInterval(sessionDelay), target: self, selector: #selector(writeOnDB), userInfo: nil, repeats: true)
            RunLoop.current.add(self.writePartialOnDbTimer!, forMode: .common)
        }
    }

    @objc func generatBatteryDebugPartialTask(){
        if sensorDataManager!.isSessionActive {
            LogHelper.log(message: "generatBatteryDebugPartialTask", withTag: "DebugPartial")
            addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: "Last sensor battery: \(sensorDataManager?.currentSensorBattery ?? -1)")
        }
        
    }
    
    @objc func generatePartialTask(){
        LogHelper.log(message: "generatePartialTask, isGps \(isGpsMode)", withTag: TAG)

        if(isGpsMode){
            if(sensorDataManager?.isSessionActive == false ){
                LogHelper.log(message: "generatePartialTask, return1", withTag: TAG)
                return
            }
        }
        
        
        guard   let lat = self.lat,
                let lng = self.lng,
                let generatedPartial = getGpsOrGyroPartial(),
                let session = self.session else{
                LogHelper.log(message: "generatePartialTask, generating empty partial", withTag: TAG)

                if let emptyPartial = sensorDataManager?.getEmptyPartial(){
                    self.onSessionUpdate(partial: emptyPartial)
                }
                    
            return
        }
        
        
        do{
            try? DBManager.sharedInstance.getDB().safeWrite {
                
                if let session = self.session {
                    if(!session.isInvalidated){
                        self.session =  self.pointManager?.calculatePoint(session: session, lat: lat, lng: lng, distance: generatedPartial.getTotalDistance())
                    }
                }
            }
        }catch{
            LogHelper.logError(message: "generatePartialTask catch1", withTag: TAG)
        }
        

        self.addToPartialCache(partial: generatedPartial)
        LogHelper.log(message: "generatePartialTask \(generatedPartial.getTotalDistance()) \(generatedPartial.elapsedTimeInMillis)", withTag: TAG)
        
        if(sensorDataManager?.isSessionActive == true){
            self.onSessionUpdate(partial: generatedPartial)
            //update UI points
            NotificationCenter.default.post(name: Notification.Name("POINTS_UPDATE"), object: nil, userInfo: ["points": ["tot":session.nationalPoints, "initiative":session.initiativePoints()]])
            LogHelper.log(message: "generatePartialTaskAndUpdateUI \(generatedPartial.getTotalDistance()) \(generatedPartial.elapsedTimeInMillis)", withTag: TAG)
        }
        

    }
    
    
    private func getGpsOrGyroPartial() -> Partial?{
        if (isGpsMode || self.lastCadenceEntity == nil) {
            LogHelper.log(message: "generating gps partial", withTag: "getGpsOrGyroPartial")
           return self.sensorDataManager?.getPartial(currentGpsEntity: GPSPrimitive(lat: self.lat!, lng: self.lng!, altitude: self.altitude, timestamp: self.gpsTimestamp))
        }else{
            LogHelper.log(message: "generating gyro partial", withTag: "getGpsOrGyroPartial")
            return self.sensorDataManager?.getPartial(primitive: lastCadenceEntity!)
        }
    }
    
    private func addToPartialCache(partial: Partial?){
        if let partial = partial {
            self.lastGeneratedPartial = partial
            self.partialCache.append(partial)
        }
    }
    
    
    @objc func writeOnDB(){
        LogHelper.log(message: "Write on Db sensorDataManager!.isSessionActive", withTag: TAG)
            if sensorDataManager!.isSessionActive {
                self.updatePartialSession(type: Partial.PartialType.InProgress)
            }
    }
    
    
public func updatePartialSession(type: Partial.PartialType){
    if let sensorDataManager = sensorDataManager{
        LogHelper.log(message: "updatePartialSession \(type)", withTag: TAG)
        LogHelper.log(message: "updatePartialSession \(type)", withTag: TAG)
        guard let lat = self.lat, let lng = self.lng else{ return }
        LogHelper.log(message: "updatePartialSession  post return \(type)", withTag: TAG)

        if(lastSavedPartial == nil){
            
            let partial = Partial(
                uuid: UUID().uuidString,
                timestamp: Date().millisecondsSince1970,
                altitude: self.altitude,
                latitude: lat,
                longitude: lng,
                type: type.rawValue,
                deltaRevs: 0,
                gyroDeltaDistance: 0.0,
                gyroDistance: 0,
                wheelTime: 0,
                speed: 0.0,
                gpsDistance: 0.0,
                elapsedTimeInMillis: 0,
                averageSpeed: 0,
                isGpsPartial: false,
                batteryLevel: sensorDataManager.currentSensorBattery,
                urban: false,
                totalGyroDistanceInKm: sensorDataManager.totalGyroDistanceInKm,
                totalGpsOnlyDistanceInKm: sensorDataManager.totalGpsOnlyDistanceInKm,
                totalGpsCacheDistanceInKm: sensorDataManager.totalGpsCacheDistanceInKm,
                sessionElapsedTimeInSec: sensorDataManager.sessionElapsedTimeInSec)
            
            
            if(isFirstPartial){
                partial.type = Partial.PartialType.Start.rawValue
                isFirstPartial = false
            }
            
            lastSavedPartial = partial
            
            
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                     
                    self.session?.partials.append(partial)
                    
                }
            }catch{}
            
            
            //clean cache
            LogHelper.log(message: "updatePartialSession, generrate first", withTag: TAG)


        }else{
            
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                     
    
                    //we MUST create new object to refresh Partial Istance
                    let partial = Partial(
                        uuid: UUID().uuidString,
                        timestamp: Date().millisecondsSince1970,
                        altitude: self.altitude,
                        latitude: lat,
                        longitude: lng,
                        type: type.rawValue,
                        deltaRevs: lastGeneratedPartial?.deltaRevs ?? 0,
                        gyroDeltaDistance: lastGeneratedPartial?.gyroDeltaDistance.value ?? 0.0,
                        gyroDistance: lastGeneratedPartial?.gyroDistance.value ?? 0.0,
                        wheelTime: 0,
                        speed: 0.0,
                        gpsDistance: lastGeneratedPartial?.gpsDistance ?? 0.0,
                        elapsedTimeInMillis: lastGeneratedPartial?.elapsedTimeInMillis ?? 0,
                        averageSpeed: 0,
                        isGpsPartial: lastGeneratedPartial?.isGpsPartial ?? false,
                        batteryLevel: sensorDataManager.currentSensorBattery,
                        urban: lastGeneratedPartial?.urban ?? false,
                        rawData_wheel: lastGeneratedPartial?.rawData_wheel ?? 0,
                        rawData_ts: lastGeneratedPartial?.rawData_ts ?? 0,
                        totalGyroDistanceInKm: sensorDataManager.totalGyroDistanceInKm,
                        totalGpsOnlyDistanceInKm: sensorDataManager.totalGpsOnlyDistanceInKm,
                        totalGpsCacheDistanceInKm: sensorDataManager.totalGpsCacheDistanceInKm,
                        sessionElapsedTimeInSec: sensorDataManager.sessionElapsedTimeInSec)
                    

                    if(partialCache.contains(where: {$0.gyroDeltaDistance.value == nil})){
                        
                        partial.deltaRevs = 0
                        partial.gyroDeltaDistance.value = nil
                        
                    }else{
                        
                        partial.deltaRevs = self.partialCache.map({$0.deltaRevs}).reduce(0, +)
                        partial.gyroDeltaDistance.value = self.partialCache.map({$0.gyroDeltaDistance.value ?? 0.0}).reduce(0.0, +)
                    }

                    lastSavedPartial = partial
                    
                    self.session?.partials.append(partial)
                    
                    self.partialCache.removeAll()
                    
                }
            LogHelper.log(message: "updatePartialSession, generrate last", withTag: TAG)


            }catch{}

        }
    }
        
    }
    
}

extension SessionManager{


    public func initializeSession(){
        LogHelper.log(message: "initialize session", withTag: TAG)
        
        
        //init session
        //load previous session if present
        if let lastSession = DBManager.sharedInstance.getOnGoinSession() {
            LogHelper.log(message: "Recovering Session: one session found", withTag: self.TAG)
            Bugsnag.leaveBreadcrumb(withMessage: "Recovering Session: one session found")
            recoverPreviousSession(lastSession: lastSession, success: { () -> Void in
                Bugsnag.leaveBreadcrumb(withMessage: "Recovering Session: first check ok")

                //MARK: Restore data to continue sessione
                
                //load last partial
                self.lastGeneratedPartial = lastSession.getNonDebugPartials().last
   
                //complete init
                self.completeIniziatileSession(recoverSession: true)
                
            }, cancel: { () -> Void in
                
                LogHelper.logError(message: "Recovering session: Error during the recovering", withTag: self.TAG)
                Bugsnag.leaveBreadcrumb(withMessage: "Recovering session: Error during the recovering")
                self.requestStop()
            })
 
        }else{
            
            //create new session
            self.session = Session()
            //save offline
            DBManager.sharedInstance.saveSession(session: self.session!)
            
            completeIniziatileSession(recoverSession: false)
        }
        
    }
    
    private func completeIniziatileSession(recoverSession: Bool){
        LogHelper.log(message: "completeIniziatileSession", withTag: TAG)
        self.sensorDataManager = SensorDataManager()
        
        if(recoverSession){
            //load last total data
            self.sensorDataManager?.totalGpsOnlyDistanceInKm = self.lastGeneratedPartial?.totalGpsOnlyDistanceInKm ?? 0.0
            self.sensorDataManager?.totalGyroDistanceInKm = self.lastGeneratedPartial?.totalGyroDistanceInKm ?? 0.0
            self.sensorDataManager?.totalGpsCacheDistanceInKm = self.lastGeneratedPartial?.totalGpsCacheDistanceInKm ?? 0.0
            self.sensorDataManager?.sessionElapsedTimeInSec = self.lastGeneratedPartial?.sessionElapsedTimeInSec ?? 0
            addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Session.rawValue, extra: "Session recovered:  \(self.sensorDataManager?.getDebugDump() ?? "Dump not available")")
        }

        self.sensorDataManager?.delegate = self
        self.pointManager = PointManager(recoveredDistanceInKm: self.sensorDataManager?.getTotalDistance() ?? 0.0)
        
        self.initStartedSessionTimers()

        self.initGPS()
        self.coordinateSubscriber?.isEnabled = true
        self.isGpsMode = (sensorSDK.currentSensor == nil)
        
        self.resumeSession(isFirstTime: true)
        self.checkInitialBluetoothState()
        self.updatePartialSession(type: Partial.PartialType.Start)
        
        //MARK: SESSION START
        UserDefaults.standard.setValue("start", forKey: "session") //Check and then replace with internal variable
        
        //finally open session controller automatically
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.openSessionController()
        }

    }
    
    
    
    
    public func cleanSessionData(){
        LogHelper.log(message: "cleanSessionData", withTag: TAG)
        DispatchQueue.main.async {
            
            self.writePartialOnDbTimer?.invalidate()
            self.writePartialOnDbTimer = nil
            

            self.generatePartialTaskTimer?.invalidate()
            self.generatePartialTaskTimer = nil
            
            self.generatebatteryDebugTimer?.invalidate()
            self.generatebatteryDebugTimer = nil
        }
        
        self.sensorDataManager?.pauseTimer?.invalidate()
        self.sensorDataManager?.pauseTimer = nil
        
        
        //clean managers
        self.sensorDataManager = nil
        self.pointManager = nil
        
        //clean session data
        session = nil
        lastCadenceEntity = nil
        lastGeneratedPartial = nil
        lastSavedPartial = nil
        isFirstPartial = true
        isFirstStart = true
        partialCache = []

    }
    
    public func dismissAndOpenDetailView(){
        
        let lastSession = self.session
        
        if(lastSession == nil){
            return
        }
        
        UIApplication.shared.topMostViewController()?.dismiss(animated: true, completion: {
            
            //open session info controller
            let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "sessionDetailNavigationController") as! UINavigationController
            let detailController = navController.topViewController as! SessionInfoViewController
            detailController.session = lastSession
            

            detailController.modalPresentationStyle = .fullScreen
            
            UIApplication.shared.topMostViewController()?.present(navController, animated: true, completion: nil)
            
        })
    }
    
    
    
    //Periodically update gps coordinates here and in sensorDataManager (if initialized)
    public func initGPS(){
        //gps observer
        self.coordinateSubscriber = self.gpsManager!.getLocation()
        self.coordinateSubscriber!.then(queue: .main) { result in // you can attach one or more subscriptions via `then`.
            switch result {
            case .success(let location):
                
                self.lat = location.coordinate.latitude
                self.lng = location.coordinate.longitude
                self.altitude = location.altitude
                self.gpsTimestamp = location.timestamp
                LogHelper.log(message: "gettingCoordinate lat: \(self.lat), lng: \(self.lng)", withTag: self.TAG)

                self.sensorDataManager?.lastLat = location.coordinate.latitude
                self.sensorDataManager?.lastLng = location.coordinate.longitude
                self.sensorDataManager?.lastAltitude = location.altitude
                self.sensorDataManager?.lastTimestamp = location.timestamp

                
            case .failure(let error):
                logw("An error has occurred: \(error.localizedDescription)")
                //pause session
                self.pauseSession()
                
            }
        }
    }
}



extension SessionManager: SensorDataManagerProtocol{
    
    func sessionBecameActive() {
        LogHelper.log(message: "sessionBecameActive \(isFirstStart)", withTag: TAG)

        if !isFirstStart {
            self.updatePartialSession(type: Partial.PartialType.Resume)
        }else{
            isFirstStart = false
        }
        
        //resume session
        self.resumeSession(isFirstTime: false)
        
    }
    
    func sessionBecamePaused() {
        LogHelper.log(message: "sessionBecamePause", withTag: TAG)
        self.pauseByInactivity()
        
        self.updatePartialSession(type: Partial.PartialType.Pause)

        //pause gps
        self.coordinateSubscriber?.isEnabled = false
        
    }
    
    
}


extension SessionManager{
    
    private func resumeSession(isFirstTime: Bool){
        if let sensorDataManager = self.sensorDataManager{
            LogHelper.log(message: "resumeSession, firstTime \(isFirstTime)", withTag: TAG)
            //resume gps
            self.coordinateSubscriber?.isEnabled = true
            
            sensorDataManager.resumeFromPause()
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                     
                    self.session?.sessionSDkState.value = SessionState.onGoing.rawValue
                    self.session?.endBattery.value = sensorDataManager.currentSensorBattery
                    self.session?.phoneEndBattery.value = round(Double(UIDevice.current.batteryLevel) * 100)
                    self.session?.firmwareVersion = sensorDataManager.currentSensorFirmware
                    self.session?.hubCoefficient.value = sensorDataManager.hubCoefficient
                    
                    if(isFirstTime){
                        self.session?.uid = DBManager.sharedInstance.getCurrentUser()?.uid
                        self.session?.startTime.value = Date().millisecondsSince1970
                        self.session?.startBattery.value = sensorDataManager.startSensorBattery
                        self.session?.phoneStartBattery.value = round(Double(UIDevice.current.batteryLevel) * 100)

                    }
                }
            }catch{
                LogHelper.logError(message: "Error saving session", withTag: TAG)
            }
            
            
            
            //MARK: SESSION START
            if let partial = lastGeneratedPartial{
                self.onSessionUpdate(partial: partial )
            }
            NotificationCenter.default.post(name: Notification.Name("SESSION_START"), object: nil)
            UserDefaults.standard.setValue("start", forKey: "session") //Check and then replace with internal variable
        }
     
    }
    
    func pauseByInactivity(){
        LogHelper.logError(message: "pauseByInactivity", withTag: TAG)
        if let sensorDataManager = sensorDataManager{
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                    self.session?.sessionSDkState.value = SessionState.automaticPause.rawValue
                    self.session?.startBattery.value = sensorDataManager.startSensorBattery
                    self.session?.hubCoefficient.value = sensorDataManager.hubCoefficient
                    self.session?.firmwareVersion = sensorDataManager.currentSensorFirmware
                    self.session?.endBattery.value = sensorDataManager.currentSensorBattery
                    self.session?.duration.value = Int64(sensorDataManager.sessionElapsedTimeInSec)
                    self.session?.phoneEndBattery.value = round(Double(UIDevice.current.batteryLevel) * 100)
                }
            }catch{
                LogHelper.logError(message: "Error saving session", withTag: TAG)
            }
            
            
            //MARK: SESSION PAUSE
            if let partial = lastGeneratedPartial{
                self.onSessionUpdate(partial: partial )
            }
            NotificationCenter.default.post(name: Notification.Name("SESSION_PAUSE"), object: nil)
            UserDefaults.standard.setValue("pause", forKey: "session") //Check and then replace with internal variable
        }

    }
    
    func pauseByUser(){
        if let sensorDataManager = sensorDataManager{
            LogHelper.logError(message: "pauseByUser", withTag: TAG)

            sensorDataManager.emptyCache()
            
            sensorDataManager.forcePause()
            
            //pause gps
            self.coordinateSubscriber?.isEnabled = false
            
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                     
                    self.session?.sessionSDkState.value = SessionState.paused.rawValue
                    self.session?.firmwareVersion = sensorDataManager.currentSensorFirmware
                    self.session?.startBattery.value = sensorDataManager.startSensorBattery
                    self.session?.endBattery.value = sensorDataManager.currentSensorBattery
                    self.session?.phoneEndBattery.value = round(Double(UIDevice.current.batteryLevel) * 100)
                    self.session?.duration.value = Int64(sensorDataManager.sessionElapsedTimeInSec)
                    self.session?.hubCoefficient.value = sensorDataManager.hubCoefficient

                }
            }catch{
                LogHelper.logError(message: "Error saving session", withTag: TAG)

            }
            
            //MARK: SESSION PAUSE
            if let partial = lastGeneratedPartial{
                self.onSessionUpdate(partial: partial )
            }
            NotificationCenter.default.post(name: Notification.Name("SESSION_PAUSE"), object: nil)
            UserDefaults.standard.setValue("pause", forKey: "session") //Check and then replace with internal variable
        }

    }
    
    
    func stopSession(){
        if let sensorDataManager = sensorDataManager{
            LogHelper.logError(message: "stopSession", withTag: TAG)
            LogHelper.log(message: "Gyro distance is \(sensorDataManager.totalGyroDistanceInKm)")
            sensorDataManager.emptyCache()
            
            updatePartialSession(type: Partial.PartialType.End)
            
            do{
                try DBManager.sharedInstance.getDB().safeWrite {
                    self.session?.startBattery.value = sensorDataManager.startSensorBattery
                    self.session?.sessionSDkState.value = SessionState.stopped.rawValue
                    self.session?.endTime.value = Date().millisecondsSince1970
                    self.session?.gyroDistance = sensorDataManager.totalGyroDistanceInKm
                    self.session?.totalKM = sensorDataManager.getTotalDistance()
                    self.session?.endBattery.value = sensorDataManager.currentSensorBattery
                    self.session?.duration.value = Int64(sensorDataManager.sessionElapsedTimeInSec)
                    self.session?.phoneEndBattery.value = round(Double(UIDevice.current.batteryLevel) * 100)
                    self.session?.firmwareVersion = sensorDataManager.currentSensorFirmware
                    self.session?.gpsOnlyDistance = sensorDataManager.totalGpsOnlyDistanceInKm
                    self.session?.hubCoefficient.value = sensorDataManager.hubCoefficient
                }
            }catch{
                LogHelper.logError(message: "Error saving session", withTag: TAG)

            }
            LogHelper.log(message: "SESSION STOP")

            
            //MARK: SESSION STOP
            if let partial = lastGeneratedPartial{
                self.onSessionUpdate(partial: partial )
            }
            NotificationCenter.default.post(name: Notification.Name("SESSION_STOP"), object: nil)
            UserDefaults.standard.setValue("stop", forKey: "session") //Check and then replace with internal variable
        }
        
        
    }
    
    func addDebugPartialIfCurrentSessionPresent(partialType: Int, extra: String){
        if let session = session{
            LogHelper.log(message: "writing new debug partial", withTag: "DebugPartial")

            let partial = Partial.getEmpty()
            partial.type = partialType
            partial.isDebug = true
            partial.extra = extra
            try? DBManager.sharedInstance.getDB().safeWrite {
                 
                self.session?.partials.append(partial)
                
            }
        }
    }
    
    func checkInitialBluetoothState(){
        var state = sensorSDK.getCentralManager().state.rawValue
        var currentSensor = sensorSDK.currentSensor
        var btStateString = "Initial bluetooth state is: \(getStateString(raw: state))"
        
        var currentSensorString = "CurrentSensor is null"

        if let currentSensor = currentSensor {
            currentSensorString = "CurrentSensor: Wheel: \(currentSensor.wheelCircunference) ,batteryLevel: \(currentSensor.batteryLevel ?? -1 ), firmware \(currentSensor.firmwareVersion ?? "NA")"
        }
        var savedSensor = "SensorUserDefaultsKey is null"
        if UserDefaults.standard.object(forKey: BTConstants.SensorUserDefaultsKey) != nil{
            savedSensor = "SensorUserDefaultsKey: \(UserDefaults.standard.string(forKey: BTConstants.SensorUserDefaultsKey) ?? "not available")"
        }
        addDebugPartialIfCurrentSessionPresent(partialType: Partial.PartialType.Ble.rawValue, extra: btStateString + " " + currentSensorString + " " + savedSensor)

    }
    
    private func getStateString(raw: Int) -> String{
        switch(raw){
            case 0:  return "unknown"

            case 1:  return "resetting"

            case 2:  return "unsupported"

            case 3:  return "unauthorized"

            case 4:  return "poweredOff"

            case 5:  return "poweredOn"
                
            default: return "default"
        }
    }
}
