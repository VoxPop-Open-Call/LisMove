//
//  AppDelegate.swift
//  LisMove
//
//

import UIKit
import Firebase
import FirebaseAuth
import FirebaseMessaging
import PKHUD
import Bugsnag
import SwiftLog
import AppFolder
import GoogleSignIn
import GoogleMaps
import GooglePlaces
import UserNotifications
import Mobilisten
import MessageUI
import AppTrackingTransparency
import IQKeyboardManagerSwift
import Siren

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MFMailComposeViewControllerDelegate, MessagingDelegate {
    
    //iOS
    let TAG = "AppDelegate"
    var window: UIWindow?
    
    //DB
    var DB: DBManager?
    
    //notification check
    var notificationLock = false
    

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window?.makeKeyAndVisible()
        Siren.shared.wail()
        
        //MARK: Init services
        //start monitoring phone
        UIDevice.current.isBatteryMonitoringEnabled = true
        
        //Firebase
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        //Google services
        
        
       

        
        config.releaseStage = LisMoveEnvironmentConfiguration.ENVIRONMENT_NAME
        Bugsnag.start(with: config)
        
        //BUGSNAG report
        Bugsnag.leaveBreadcrumb(withMessage: "Lismove init complete")
        
        
        //MARK: 1. init log system
        initLogSystem()
      
        //MARK: 2. init DB
        self.DB = DBManager.sharedInstance

        
        //MARK: 3. init notification
        initNotification(application)
        
        
        IQKeyboardManager.shared.enable = true
        IQKeyboardManager.shared.toolbarDoneBarButtonItemText = "Ok"
        //MARK: 4. observers
        //Error observer
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveError(_:)), name: NSNotification.Name(rawValue: "ERROR_STREAM"), object: nil)
        
        //MARK: -- Other
        //check go in foreground
        NotificationCenter.default.addObserver(self, selector: #selector(appMovedToBackground), name: UIApplication.willResignActiveNotification, object: nil)
        
        //iOS 15 fix
        fixAppBar()
        fixTabBar()
        
        return true
    }
    

    //MARK: --- Notifications
    func application(_ application: UIApplication, didRegister notificationSettings: UIUserNotificationSettings) {
        application.registerForRemoteNotifications()
    }
    
    func application(_ application: UIApplication, didReceive notification: UILocalNotification) {
        UIApplication.shared.applicationIconBadgeNumber += 1
    }


    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.alert, .badge, .sound])
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {

            let deviceTokenString = deviceToken.reduce("", {$0 + String(format:
                "%02X", $1)})
        LogHelper.log(message: "didRegisterForRemoteNotificationsWithDeviceToken \(deviceTokenString)", withTag: TAG)

        ZohoSalesIQ.enablePush(deviceTokenString, isTestDevice: false, mode: APNSMode.production)
    }
    

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        LogHelper.log(message: "didReceive", withTag: TAG)

        if ZohoSalesIQ.isMobilistenNotification(response.notification.request.content.userInfo){
            switch response.actionIdentifier {
            case "Reply":
                ZohoSalesIQ.handleNotificationAction(response.notification.request.content.userInfo, response: (response as? UNTextInputNotificationResponse)?.userText)
                
            default: break
            }
            if response.actionIdentifier == UNNotificationDefaultActionIdentifier{
                ZohoSalesIQ.processNotificationWithInfo(response.notification.request.content.userInfo)
            }
        }else{
            AppDelegate.openResultController(response)
            completionHandler()
        }
       
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        LogHelper.log(message: "didReceiveRemoteNotification", withTag: TAG)

        ZohoSalesIQ.processNotificationWithInfo(userInfo)
        
        completionHandler(UIBackgroundFetchResult.newData)
    }

    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        LogHelper.log(message: "Fail to register", withTag: TAG)
    }
    
    
    //MARK: --- Utility
    func fixAppBar(){
        //fix ios 15 appbar
        let appearance = UINavigationBarAppearance()
        
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = .systemRed
        appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
       
    }
    
    
    func fixTabBar(){
        if #available(iOS 15, *) {
            let tabBarAppearance = UITabBarAppearance()
            tabBarAppearance.configureWithOpaqueBackground()
            tabBarAppearance.backgroundColor = .white
            UITabBar.appearance().standardAppearance = tabBarAppearance
            UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
        }
        
    }
    
    func initNotification(_ application: UIApplication){
        
        
        if #available(iOS 10.0, *) {
          // For iOS 10 display notification (sent via APNS)
          UNUserNotificationCenter.current().delegate = self

          let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
          UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { _, _ in
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                    self.requestPermission()
                }

                
            }
            
          )
        } else {
          let settings: UIUserNotificationSettings =
            UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
          application.registerUserNotificationSettings(settings)
        }


        application.registerForRemoteNotifications()

    }
    
    func requestPermission() {
        if #available(iOS 14, *) {
            ATTrackingManager.requestTrackingAuthorization { status in
                switch status {
                case .authorized:
                    // Tracking authorization dialog was shown
                    // and we are authorized
                    print("Authorized")

                case .denied:
                    // Tracking authorization dialog was
                    // shown and permission is denied
                    print("Denied")
                case .notDetermined:
                    // Tracking authorization dialog has not been shown
                    print("Not Determined")
                case .restricted:
                    print("Restricted")
                @unknown default:
                    print("Unknown")
                }
            }
        } else {
            // Fallback on earlier versions
        }
    }
    
    
    
    @objc func appMovedToBackground() {
        if(SessionManager.sharedInstance.sessionState?.state == SessionState.onGoing && !notificationLock) {
            AppDelegate.sendLocalPushNotification(title: "La sessione continuerà in background", subtitle: "Accedi al cruscotto per monitorare la sessione")
            self.notificationLock = true
        }
    }

    
    @objc func onDidReceiveError(_ notification: Notification){
        
        let check = notification.userInfo as! [String: String]
        let error = check["error"]!
        let json = check["json"]
        
        //show error popup
        let topMostViewController = UIApplication.shared.topMostViewController()
        
        let alert = UIAlertController(title: "Errore", message: error, preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = topMostViewController!.view
            popoverPresentationController.sourceRect =  CGRect(x: topMostViewController!.view.bounds.size.width / 2.0, y: topMostViewController!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        
        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
        
        
        //json send intent ONLY IN DEV
        if(json != nil && LisMoveEnvironmentConfiguration.IS_ENVIRONMENT_DEV){
            alert.addAction(UIAlertAction(title: "Assistenza", style: .destructive, handler: {_ in
                self.sendEmail(message: json!)
            }))
        }
        

        topMostViewController!.present(alert, animated: true, completion: nil)
        
    }
    
    func sendEmail(message: String) {
        
    }

    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true)
    }
    
    
    
    private static func openResultController(_ response: UNNotificationResponse) {
        guard let rootController = UIApplication.shared.windows.first?.rootViewController,
              let rawData = response.notification.request.content.userInfo["result"] as? String else {
            return
        }
        
        //ResultController.showWithData(rawData, in: rootController)
    }
    
    
    public static func sendLocalPushNotification(title: String, subtitle: String, object: Any? = nil, afterInterval: TimeInterval = 3) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.subtitle = subtitle
        content.sound = UNNotificationSound.default
        
        if let object = object {
            content.userInfo = ["result": object]
        }
        
        // show this notification five seconds from now
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: afterInterval, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request){error in
            
            guard error == nil else {
                
                print("Notification error! --- \(error!.localizedDescription)")
                return
                
            }
            
            print("Notification scheduled!")
        }
    }
    
    //MARK: init log system
    private func initLogSystem(){
        let now = Date()

        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.current
        formatter.dateFormat = "yyyy-MM-dd HH:mm"

        let dateString = formatter.string(from: now)
        
        
        //Set the name of the log files
        Log.logger.name = "Lismove-logfile-\(dateString)" //default is "logfile"

        //Set the max size of each log file. Value is in KB
        Log.logger.maxFileSize = 2048 //default is 1024

        //Set the max number of logs files that will be kept
        Log.logger.maxFileCount = 8 //default is 4

        //Set the directory in which the logs files will be written
        let folder = AppFolder.Library.Application_Support
        let fileURL = folder.url.appendingPathComponent("TT/Log")
        
        Log.logger.directory = fileURL.path//default is the standard logging directory for each platform.
        print(fileURL.path)

        //Set whether or not writing to the log also prints to the console
        Log.logger.printToConsole = true //default is true
        
    }
    

    
    // MARK: UISceneSession Lifecycle
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    @available(iOS 13.0, *)
    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
    
}

extension UIApplication {
    func topMostViewController() -> UIViewController? {
        return self.keyWindow?.rootViewController?.topMostViewController()
    }
}

