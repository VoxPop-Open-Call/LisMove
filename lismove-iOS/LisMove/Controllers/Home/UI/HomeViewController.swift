//
//  DashboardViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/05/21.
//

import UIKit
import Floaty
import FirebaseAuth
import Charts
import MobileCoreServices
import Kingfisher
import TRMosaicLayout
import Resolver
import MaterialComponents.MaterialCards
import MaterialComponents
import Bugsnag
import FirebaseDatabase
import simd


class HomeViewController: UIViewController, UICollectionViewDelegateFlowLayout, UICollectionViewDataSource, UICollectionViewDragDelegate, UICollectionViewDropDelegate {

    //dashboard card configurations
    @IBOutlet weak var collectionView: UICollectionView!
    
    //dashboard items
    //TODO COMPLETE CARDS
//    var items: [String] = ["device","user","msg","money","project","points","km","co2","carpooling","allarm","bike","dailyUse",] {
//        didSet {
//            UserDefaults.standard.set(items, forKey: "dashboardConfig")
//            mosaicLayout.items = items
//        }
//    }
    
    var items: [String] = ["device","user","msg","money","project","points","km","co2","dailyUse"] {
        didSet {
            UserDefaults.standard.set(items, forKey: "dashboardConfig")
            mosaicLayout.items = items
        }
    }
    
    let mosaicLayout = MosaicLayout()
    
    let user = DBManager.sharedInstance.getCurrentUser()
    var device = DBManager.sharedInstance.getLismoveDevice()
    var nationalPoints: Double = 0.0
    var userDashboard: UserDashboard? = nil
    var enrollmentsList: [Enrollment] = []
    var organizationList: [Organization] = []
    var activeInitiativeList: [ActiveProjectItemUI] = []
    var showDashboard = false
    
    var sensorConnectionRetry = 0
    //sensor connection warning
    var sensorConnectionTimeoutDaemon: Timer?
    var sensorConnectionTimeout = 0
    
    @Injected var authRepository: AuthRepository
    @Injected var userRepository: UserRepository
    @Injected var phoneRepository: PhoneRepository
    @Injected var initiativeRepository: InitiativeRepository
    
    override func viewDidLoad() {
        super.viewDidLoad()
        Bugsnag.setUser(user?.uid, withEmail: user?.email, andName: user?.getFullName())


        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        

        mosaicLayout.items = self.items
        self.collectionView?.collectionViewLayout = mosaicLayout
        mosaicLayout.delegate = self
        collectionView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: 150, right: 0)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView?.dragDelegate = self
        collectionView?.dropDelegate = self
        collectionView.dragInteractionEnabled = true
        
        
        //load dashboard configuration
        self.items = UserDefaults.standard.array(forKey: "dashboardConfig") as? [String] ?? ["device","user","msg","money","project","points","km","co2","dailyUse"]
        
        
        //fetch dashboard
        fetchHomeData()
        
        //check latest app version only DEV
        if(LisMoveEnvironmentConfiguration.IS_ENVIRONMENT_DEV){
            checkAppVersion()
        }
    
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }


    func fetchHomeData(){
        guard let uid = self.user?.uid else {
            return
        }
        
        NetworkingManager.sharedInstance.getUserDashboard(uid:uid, completion: {
            userDashboardResult in
            switch userDashboardResult {
                case .success(let data):
                
                
                    self.userDashboard = data
                
                    //set notification number
                    UIApplication.shared.applicationIconBadgeNumber = self.userDashboard?.messages ?? 0
                
                
                
                    NetworkingManager.sharedInstance.getGlobalRanking{ result in
                        switch result{
                            case .success(let ranking):
                            self.nationalPoints = ranking.rankingPositions?.first(where: {$0.username == self.user?.username})?.points ?? 0.0
                                
                            case .failure(_):
                                self.nationalPoints = 0
                                
                        }
                        
                        //persist animation
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            
                            self.initiativeRepository.getEnrollements(uid: self.user!.uid ?? "") { result in
                                switch result {
                                    case .success(let data):
                                    
                                        self.enrollmentsList = data.filter{!$0.isClosed()}
                                        
                                        self.organizationList.removeAll()
                                    
                                    self.initiativeRepository.getOrganization(oids: self.enrollmentsList.compactMap{$0.organization}) { result in
                                            switch result {
                                                case .success(let organizations):
                                                
                                                    self.organizationList = organizations
                                                
                                                    self.setupActiveInitiatives()
                                                    
                                                case .failure(let error):
                                                    //MARK: ERROR STREAM
                                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                                    
                                            }
                                        }


                                         
                                    case .failure(let error):
                                        //MARK: ERROR STREAM
                                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                        
                                }
                            }
                            
                            
                            self.observeSensorUpate()
                            
                        }
                                    
                    }
                    
                    
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
            
        })
        
       
    }
    
    func observeSensorUpate(){
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            //init refresh sensor card obsv
            NotificationCenter.default.addObserver(self, selector: #selector(self.onSensorUpdate(_:)), name: NSNotification.Name(rawValue: "SENSOR_UPDATE"), object: nil)
            
            self.showDashboard = true
            self.collectionView.reloadData()
        }
    }
    
    func setupActiveInitiatives() {
        //print("Current timestamp is \(DateTimeUtils.getCurrentTimestamp())")
        activeInitiativeList = enrollmentsList.map({
            activeEnrollment in
            let organization = organizationList.first(where: {$0.id == activeEnrollment.organization})
            return ActiveProjectItemUI(image: organization?.initiativeLogo ?? "", regulation: organization?.regulation ?? "Nessun regolamento disponibile", organizationName: organization?.title ?? "")
            
        })
    }
    
    
    @objc func onSensorUpdate(_ notification: Notification){
        
        self.device = DBManager.sharedInstance.getLismoveDevice()
        
        if(self.device?.isInvalidated ?? true){
            return
        }
        
        let indexPath = IndexPath(item: self.items.firstIndex(of: "device")!, section: 0)
        self.collectionView.reloadItems(at: [indexPath])
        
        if(SessionManager.sharedInstance.sensorSDK.currentSensor != nil){
            sensorConnectionRetry = 0
        }
        
    }
    
    
    
    //MARK: --- collection view setup
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 9
    }
    
    
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        var cell: MDCCardCollectionCell?
        
        
        switch self.items[indexPath.item]{
        
        case "dailyUse":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "dailyuseCell", for: indexPath) as! DailyUseTableViewCell
            let dailyUseCell = cell as! DailyUseTableViewCell
            dailyUseCell.setupCell(data: userDashboard?.dailyDistance ?? [UserDistanceStats]())
            if(showDashboard){
                (cell as! DailyUseTableViewCell).hideAnimation()
            }

        case "user":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "userCell", for: indexPath) as! UserTableViewCell

            if(showDashboard){
                (cell as! UserTableViewCell).hideAnimation()
            }
            let userCell = cell as! UserTableViewCell
            userCell.username.text = self.user?.username
            //avatar
            let url = URL(string: self.user?.avatarURL ?? "")
            userCell.avatarurl.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            
            userCell.numberOfDays.text = String(userDashboard?.sessionNumber ?? 0)
            
        case "msg":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "msgCell", for: indexPath) as! MsgCollectionViewCell
            let msgCell = cell as! MsgCollectionViewCell
            if(showDashboard){
                let messages = self.userDashboard?.messages ?? 0
                let showLabel = messages > 0
                msgCell.hideAnimation()
                msgCell.setData(messages: messages, showLabel: showLabel)
            }
            
        case "money":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "moneyCell", for: indexPath) as! MoneyCollectionViewCell
            let moneyCell = cell as! MoneyCollectionViewCell
            moneyCell.euro.text =  String(format: "%.2f", userDashboard?.euro ?? 0.0)
            if(showDashboard){
                (cell as! MoneyCollectionViewCell).hideAnimation()
            }
            
            
        case "project":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "projectCell", for: indexPath) as! ProjectCollectionViewCell
            let projectCell = (cell as! ProjectCollectionViewCell)
            if(showDashboard){
                projectCell.hideAnimation()
            }
        
            projectCell.viewController = self
            projectCell.setupCell(data: activeInitiativeList)
            
    
        case "points":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "pointsCell", for: indexPath) as! PointsCollectionViewCell
            let pointsCell = cell as! PointsCollectionViewCell
            if(showDashboard){
                pointsCell.hideAnimation()
            }
            var points = enrollmentsList.map({ enrollment -> HomePointItemData in
                let organization = self.organizationList.first(where: {$0.id == enrollment.organization})
                return HomePointItemData(image: organization?.notificationLogo, points: Double(enrollment.points ?? 0), organizationName: organization?.title ?? "")
            })
            points.append(HomePointItemData(image: nil, points: nationalPoints, organizationName: "Community"))
            pointsCell.setupCell(data: points)
            
        case "km":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "kmCell", for: indexPath) as! KmCollectionViewCell
            let kmCell = cell as! KmCollectionViewCell
            kmCell.kmLabel.text = String(format: "%.2f", userDashboard?.distance ?? 0.0)

            
        case "co2":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "co2Cell", for: indexPath) as! CO2CollectionViewCell
            let co2Cell = cell as! CO2CollectionViewCell
            co2Cell.co2Label.text =  String(format: "%.2f", userDashboard?.getConvertedC02() ?? 0.0)
        
        case "carpooling":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "carpoolingCell", for: indexPath) as! CarpoolingCollectionViewCell
            
        case "allarm":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "allarmCell", for: indexPath) as! AllarmCollectionViewCell
            
        case "bike":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "bikeCell", for: indexPath) as! BikeCollectionViewCell
 
        case "device":
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: "deviceCell", for: indexPath) as! DeviceCollectionViewCell
            
            if(showDashboard){
                
                //reload device
                self.device = DBManager.sharedInstance.getLismoveDevice()
                
                (cell as! DeviceCollectionViewCell).hideAnimation()
            }
            
            (cell as! DeviceCollectionViewCell).setDevice(name: self.device?.name, connected: SessionManager.sharedInstance.sensorSDK.currentSensor != nil)
            //init button
            if(SessionManager.sharedInstance.sensorSDK.currentSensor != nil){
                self.sensorConnectionRetry = 0
                
            }
            
            (cell as! DeviceCollectionViewCell).deviceButton.frame = CGRect(x: 0, y: 0, width: 200, height: 16)
            (cell as! DeviceCollectionViewCell).deviceButton.layer.cornerRadius = 8
            
            //refresh action
            (cell as! DeviceCollectionViewCell).refreshTappedAction = {
                
                UIView.animate(withDuration: 0.5, animations: {
                    (cell as! DeviceCollectionViewCell).deviceRefreshBUtton.transform = (cell as! DeviceCollectionViewCell).deviceRefreshBUtton.transform.rotated(by: .pi)
                    (cell as! DeviceCollectionViewCell).deviceRefreshBUtton.layoutIfNeeded()
                })
                
                self.view.makeToast("Aggiornamento del sensore in corso")
                
                
                //connect
                if(self.device != nil){
                    //start
                    SessionManager.sharedInstance.sensorSDK.startScan()
                    SessionManager.sharedInstance.sensorSDK.connectToSensor(sensorID: self.device!.uuid!)
                }
                
                
                //reset
                if(SessionManager.sharedInstance.sensorSDK.currentSensor == nil){
                    self.sensorConnectionRetry+=1
                }
                
                
                if(self.sensorConnectionRetry >= 2){
                    let alertController = UIAlertController(title: "Attenzione", message:
                            "Il sensore non sembra essere collegato. Vuoi rieffettuare l'associazione?", preferredStyle: .alert)
                    if let popoverPresentationController = alertController.popoverPresentationController {
                        popoverPresentationController.sourceView = self.view
                        popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
                    }
                    
                    let cancel = UIAlertAction(title: "Annulla", style: .default, handler: {context in
                        
                        //start countdown bugsnag
                        if(self.sensorConnectionTimeoutDaemon == nil){
                            self.sensorConnectionTimeoutDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(1), repeats: true) { timer in
                                
                                self.sensorConnectionTimeout+=1
                                
                                if(self.sensorConnectionTimeout == 10){
                                    self.sensorConnectionTimeoutDaemon?.invalidate()
                                    
                                    
                                    let exception = NSException(name:NSExceptionName(rawValue: "Sensor Refresh Warning"),
                                                                reason:"Time > 10s",
                                                                userInfo:nil)
                                
                                    Bugsnag.notify(exception)
                                }
                            }
                        }
                        
                    })
                    let confirm = UIAlertAction(title: "Conferma", style: .destructive, handler: {context in
                        
                        self.sensorConnectionRetry = 0
                        
                        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
                        let controller = storyBoard.instantiateViewController(withIdentifier: "scanViewController") as! UIPageViewController
                        controller.modalPresentationStyle = .fullScreen
                        self.present(controller, animated: true, completion: nil)
                    })
                    
                    alertController.addAction(cancel)
                    alertController.addAction(confirm)
                    
                    self.present(alertController, animated: true, completion: nil)
                }
  
            }
        
        
            //new sensor action
            (cell as! DeviceCollectionViewCell).configTappedAction = {
            
                self.openGenericController(storyboardName: "Profile", identifier: "SensorSettingNavigationController")
                
            }
            
        default:
            break
        }
        
        
        //cell layout with material card effect
        cell!.contentView.layer.masksToBounds = true
        cell!.layer.masksToBounds = false
        cell!.contentView.layer.cornerRadius = 8
        
//        cell!.layer.cornerRadius = 16
//        cell!.layer.borderColor  =  UIColor.lightGray.cgColor
//        cell!.layer.borderWidth = 1.0
//
//        cell!.layer.shadowColor = UIColor.gray.cgColor
//        cell!.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
//        cell!.layer.shadowRadius = 2.0
//        cell!.layer.shadowOpacity = 1.0

        cell!.layer.cornerRadius = 16
        cell!.backgroundColor = .white
        cell!.setShadowElevation(ShadowElevation(rawValue: 2), for: .normal)


        //content view layout
        cell!.contentView.layoutMargins = UIEdgeInsets(top: 8, left: 8, bottom: 16, right: 8)


        return cell!
    }
    

     
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
           return UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
        }
    
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        //style all different type of cells
        switch self.items[indexPath.item] {
        case "user":
            openProfileController()
        case "money":
            break
        case "km":
            openGenericController(storyboardName: "Profile", identifier: "sessionController")
        case "device":
            openGenericController(storyboardName: "Profile", identifier: "SensorSettingNavigationController")
        case "msg":
            openMSGController()
        case "co2":
            break
            //openCO2SwiftUiController()
        default:
            break
        }
    }
    
    
    //MARK: ======== open dedicated controller
    private func openGenericController(storyboardName: String, identifier: String){
        
        let storyBoard: UIStoryboard = UIStoryboard(name: storyboardName, bundle: nil)
        let profileController = storyBoard.instantiateViewController(withIdentifier: identifier) as! UINavigationController
        profileController.modalPresentationStyle = .popover
        if let popoverPresentationController = profileController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        self.present(profileController, animated: true, completion: nil)
    

    }
    
    private func openMSGController(){
        
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "msgViewController") as! UINavigationController
        let detailController = navController.topViewController as! MsgTableViewController
        detailController.delegate = self
        
        navController.modalPresentationStyle = .popover
        
        if let popoverPresentationController = navController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        self.present(navController, animated: true, completion: nil)

    }
    
    private func openProfileController(){
        let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
        let profileController = storyBoard.instantiateViewController(withIdentifier: "ProfileViewController") as! ProfileTableViewController
        profileController.modalPresentationStyle = .popover
        if let popoverPresentationController = profileController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }

        
        self.present(profileController, animated: true, completion: nil)
    }

    
    private func openCO2SwiftUiController(){
    
        //open c02 controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let vehicleController = storyBoard.instantiateViewController(withIdentifier: "vehicleBridgeController") as! Vehicle_BridgeViewController
        vehicleController.modalPresentationStyle = .fullScreen
        
        self.present(vehicleController, animated: true, completion: nil)
        
        NotificationCenter.default.addObserver(forName: NSNotification.Name("dismissSwiftUI"), object: nil, queue: nil) { (_) in
            vehicleController.dismiss(animated: true, completion: nil)
        }
            
    }

    
    func collectionView(_ collectionView: UICollectionView, itemsForBeginning session: UIDragSession, at indexPath: IndexPath) -> [UIDragItem] {
        
        
        let item = self.items[indexPath.row]
        let itemProvider = NSItemProvider(object: item as NSItemProviderWriting)
        let dragItem = UIDragItem(itemProvider: itemProvider)
        dragItem.localObject = item
        return [dragItem]

    }
    

    
    func collectionView(_ collectionView: UICollectionView, dropSessionDidUpdate session: UIDropSession, withDestinationIndexPath destinationIndexPath: IndexPath?) -> UICollectionViewDropProposal {
        
        if(collectionView.hasActiveDrag){
            return UICollectionViewDropProposal(operation: .move, intent: .insertAtDestinationIndexPath)
        }
        
        return UICollectionViewDropProposal(operation: .forbidden)
    }
    
    func collectionView(_ collectionView: UICollectionView, performDropWith coordinator: UICollectionViewDropCoordinator) {
        
        var destinationIndexPath: IndexPath
        if let indexPath = coordinator.destinationIndexPath{
            destinationIndexPath = indexPath
        } else {
            let row = collectionView.numberOfItems(inSection: 0)
            destinationIndexPath = IndexPath(item: row - 1, section: 0)
        }
        
        if (coordinator.proposal.operation == .move){
            self.reorderItems(coordinator: coordinator, destinationIndexPath: destinationIndexPath, collectionView: collectionView)
        }
    }

    
    private func reorderItems(coordinator: UICollectionViewDropCoordinator, destinationIndexPath: IndexPath, collectionView: UICollectionView){
        if let item = coordinator.items.first,
           let sourceIndexPath = item.sourceIndexPath {
            
            //MARK: Check card with same dimension
            if(self.checkCardMosaicLayout(source: item.sourceIndexPath!, destination: destinationIndexPath)){
                
                //cards have the same dimension. Drag theme
                collectionView.performBatchUpdates({
                    self.items.remove(at: sourceIndexPath.item)
                    self.items.insert(item.dragItem.localObject as! String, at: destinationIndexPath.item)
                    
                    collectionView.deleteItems(at: [sourceIndexPath])
                    collectionView.insertItems(at: [destinationIndexPath])
                    
                }, completion: nil)
                
                coordinator.drop(item.dragItem, toItemAt: destinationIndexPath)
                
            }
        }
    }

 
    //check card layout dimension
    private func checkCardMosaicLayout(source: IndexPath, destination: IndexPath) -> Bool{
        //1. get card type
        let sourceCard = self.getCardTypeFromIndex(indexPath: source)
        let destinationCard = self.getCardTypeFromIndex(indexPath: destination)
        switch (sourceCard, destinationCard) {
        case (.fiftyFifty, .fiftyFifty), (.fullWidth, .fullWidth), (.twoThirdsOneThird, .twoThirdsOneThird), (.oneThirdTwoThirds, .oneThirdTwoThirds), (.fullWidth, .fullWidthCustomHeight(_)), (.fullWidthCustomHeight(_), .fullWidth):
                return true
            default:
                return false
        }
    }
    
}



extension HomeViewController: MosaicLayoutDelegate {
    
    func collectionView(_ collectionView: UICollectionView, mosaicCellSizeTypeAtIndexPath indexPath: IndexPath) -> MosaicSegmentStyle {
        
            return getCardTypeFromIndex(indexPath: indexPath)
    }

    
    
    private func getCardTypeFromIndex(indexPath: IndexPath) -> MosaicSegmentStyle{
        switch self.items[indexPath.item] {
        case "user":
            return .twoThirdsOneThird
        case "msg":
            return .oneThirdTwoThirds
        case "money":
            return .oneThirdTwoThirds
        case "km":
            return .fiftyFifty
        case "co2":
            return .fiftyFifty
        case "allarm":
            return .fiftyFifty
        case "bike":
            return .fiftyFifty
        case "points":
            return .fullWidthCustomHeight(Float(80 + HomePointItemView.CELL_HEIGHT * (activeInitiativeList.count + 1 )))
        case "project":
            return .fullWidthCustomHeight(Float(80 + 50 * activeInitiativeList.count) )
        default:
            return .fullWidth
            
        }
    }
}


extension HomeViewController{
    
    private func checkAppVersion(){
        let PATH = "latestVersion"
        let ref = Database.database(url: LisMoveEnvironmentConfiguration.FIREBASE_REALTIME_URL).reference(withPath: PATH)
        
        let refHandle = ref.getData(completion:  { error, snapshot in
            
            guard error == nil else {
              print(error!.localizedDescription)
              return
            }
            
            
            if snapshot.exists() {
                let dictList = snapshot.children
                dictList.forEach{child in
                    let key = (child as! DataSnapshot).key
                    
                    if(key != "ios"){
                        return
                    }

                    if let appVersion = snapshot.childSnapshot(forPath: key).value as? String {
                        
                        let userVersion = self.phoneRepository.getAppVersion()
                        
                        if (appVersion != userVersion) {

                            DispatchQueue.main.async{
                                self.showBasicAlert(title: "Attenzione", description: "Una nuova versione dell'app è disponibile. \n Attuale: \(userVersion) \n Pubblicata: \(appVersion) ")
                            }
                        }
                        
                    }
                   
                }
            }
            
          })
    }
}


extension HomeViewController: MsgProtocol{
    
    func refreshMsgCard() {
        self.showDashboard = false
        
        let indexPath = IndexPath(item: self.items.firstIndex(of: "msg")!, section: 0)
        let cell = self.collectionView.cellForItem(at: indexPath) as! MsgCollectionViewCell
        cell.showAnimation()
        
        self.view.makeToast("Aggiornamento notifiche in corso")
        
        self.fetchHomeData()
    }
    
    
}
