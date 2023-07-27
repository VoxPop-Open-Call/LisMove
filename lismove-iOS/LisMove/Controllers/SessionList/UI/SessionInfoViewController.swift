//
//  EscursionInfoViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 13/06/21.
//

import UIKit
import Floaty
import GoogleMaps
import CoreGPX
import AppFolder
import Toast_Swift
import Resolver

class SessionInfoViewController: UIViewController, UITableViewDelegate,  UITableViewDataSource {
    let goToFeedbackRequestSegue = "sendFeedbackRequest"
    
    @IBOutlet weak var feedbackButtonLayout: UIStackView!
    @IBOutlet weak var sessionDataTable: UITableView!
 
    @IBOutlet weak var sessionDataTableHeight: NSLayoutConstraint!
    @IBOutlet weak var SESSIONInitiativePoint: UILabel!
    @IBOutlet weak var SESSIONNationalPoint: UILabel!
    @IBOutlet weak var googleMapsArea: UIView!
    
    @IBOutlet weak var sessionInitiativeLabel: UILabel!
    @IBOutlet weak var errorView: UIStackView!
    @IBOutlet weak var errorImage: UIImageView!
    @IBOutlet weak var errorLabel: UILabel!
    @IBOutlet weak var emptyPointsLabel: UILabel!
    @IBOutlet weak var moreInitiativeImage: UIImageView!
    
    public var session: Session!
    var onlyPointsClicked = false
    var mapView: GMSMapView?
    private var sessionUpdateDaemon: Timer?
    
    @IBOutlet weak var refundHeaderLayout: UIStackView!
    @IBOutlet weak var refundDescription: UILabel!
    @IBOutlet weak var refundAmount: UILabel!
    let errorImageName = "exclamationmark.circle"
    let successImageName = "checkmark.circle"
    var settings = [OrganizationSettings] ()
    var refundLoaded = false
    
    @Injected var initiativeRepository: InitiativeRepository

    override func viewDidLoad() {
        super.viewDidLoad()
        
        initView()
        
        if(session.certificated){
            loadOrganizationSettings(session: session)
        }else{
            loadSessionDetail()
        }
        
    }
    
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        if let date = session.startTime.value{
            self.title = DateTimeUtils.getReadableLongDateTime(date: Double(date))
        }else{
            self.title = ""
        }
        
        
        //load session data
        sessionDataTable.dataSource = self
        sessionDataTable.delegate = self
        sessionDataTable.register(UITableViewCell.self, forCellReuseIdentifier: "sessionDetailCell")
    
        self.sessionDataTable.reloadData()
        self.sessionDataTable.layoutIfNeeded()
        self.sessionDataTableHeight.constant = self.sessionDataTable.contentSize.height
        
        loadSessionDetail()
        
        DispatchQueue.main.async {
            
            if(self.session.getTotalKM() > 0){
                
                //init map
                self.initMapView()
                
            }else{
                
                let errorLabel = UILabel(frame: CGRect(x: 0, y: 0, width: self.googleMapsArea.frame.width, height: self.googleMapsArea.frame.height))
                errorLabel.textColor = UIColor.black
                errorLabel.text = "Caricamento della mappa non riuscito"
                errorLabel.textAlignment = .center
                
                self.googleMapsArea.addSubview(errorLabel)
            }
            
            

        }
        /*
        let button = UIBarButtonItem(title: "Salva GPX", style: .plain, target: self, action: #selector(gpxShare))
        button.tintColor = .white
        self.navigationItem.rightBarButtonItem = button
         */
        
    }
    @IBAction func onSaveGpxClicked(_ sender: Any) {
        self.view.makeToast("Generazione Gpx in corso")
        gpxShare()
    }
    
    private func initMapView(){
        
        if((self.session!.polyline?.count ?? 0) > 0){
            
            // Create array of GMSPath
            let sessionPathArray: [GMSPath] = self.session!.polyline!.compactMap{GMSPath.init(fromEncodedPath: $0)}
            
            drawRow()
        }

    }
    
    
    private func drawRow(){
        let sessionPathArray = session.polyline!.compactMap{GMSPath.init(fromEncodedPath: $0)}

        if (!sessionPathArray.isEmpty) {
            let startCoordinate = sessionPathArray.first?.coordinate(at: 0)
            
            //move camera
            let camera = GMSCameraPosition.camera(
                withLatitude: startCoordinate?.latitude ?? 0,
                longitude:startCoordinate?.longitude ?? 0, zoom: 18.0)
            
            mapView = GMSMapView.map(withFrame: self.view.frame, camera: camera)
        
            
            // Build main polyline
            sessionPathArray.forEach { path in
                let polyline = GMSPolyline(path: path)
                polyline.strokeColor = .systemRed.withAlphaComponent(0.7)
                polyline.strokeWidth = 3.0
                polyline.map = mapView
                self.googleMapsArea.addSubview(mapView!)
            }
            
            // Build dashed line between polylines and markers
            for i in (0..<sessionPathArray.count) {
                let startingPoint = sessionPathArray[0].coordinate(at: 0)
                
                // Add start mark
                let markerStart = GMSMarker()
                markerStart.position = startingPoint
                markerStart.title = "Inizio"
                markerStart.icon = UIImage(named: "ic_route_start_marker")
                markerStart.map = mapView
                
                if (i>0) {
                    let dashedPath = GMSMutablePath()
                    
                    let previousPath = sessionPathArray[i-1]
                    let currentpath = sessionPathArray[i]
                    
                    if (previousPath.count() != 0) {
                        // start point is end point of previous path segment
                        let startingPoint = previousPath.coordinate(at: UInt(previousPath.count()-1))
                        dashedPath.add(startingPoint)
                        
                        if (currentpath.count() != 0) {
                            let endPoint = currentpath.coordinate(at: 0)
                            dashedPath.add(endPoint)
                            
                            let polyline = GMSPolyline(path: dashedPath)

                            let styles: [Any] = [GMSStrokeStyle.solidColor(.systemRed.withAlphaComponent(0.4)), GMSStrokeStyle.solidColor(UIColor.clear)]
                            let lengths: [Any] = [10, 5]
                            
                            polyline.spans = GMSStyleSpans(dashedPath, styles as! [GMSStrokeStyle], lengths as! [NSNumber], GMSLengthKind.rhumb)
                            polyline.strokeColor = .systemRed
                            polyline.strokeWidth = 3.0
                            polyline.map = mapView
                            self.googleMapsArea.addSubview(mapView!)
                            
                            // Add resume
                            let markerResume = GMSMarker()
                            markerResume.position = endPoint
                            markerResume.title = "Ripresa"
                            markerResume.icon = UIImage(named: "ic_route_resume_marker")
                            markerResume.map = mapView

                            // Add pause
                            let markerPause = GMSMarker()
                            markerPause.position = startingPoint
                            markerPause.title = "Pausa"
                            markerPause.icon = UIImage(named: "ic_route_pause_marker")
                            markerPause.map = mapView
                            
                            if (i == sessionPathArray.count-1) {
                                // Add end marker
                                let markerEnd = GMSMarker()
                                markerEnd.position = currentpath.coordinate(at: UInt(currentpath.count()-1))
                                markerEnd.title = "Fine"
                                markerEnd.icon = UIImage(named: "ic_route_finish_marker")
                                markerEnd.map = mapView
                            }
                        }
                    }
                }
            }
        }
    }

    
    func gpxShare(){
        let root = GPXRoot(creator: "Lis Move")
        let path = GMSPath.init(fromEncodedPath: session.polyline!.joined())
        var trackpoints = [GPXTrackPoint]()
        
        if(path != nil){
            if(path!.count() > 0){
                for i in 0..<path!.count() {
                    let coordinate = path!.coordinate(at: i)

                    let trackpoint = GPXTrackPoint(latitude: coordinate.latitude, longitude: coordinate.longitude)
                    trackpoint.time = Date() // set time to current date
                    trackpoints.append(trackpoint)

                }

                let track = GPXTrack()                          // inits a track
                let tracksegment = GPXTrackSegment()            // inits a tracksegment
                tracksegment.add(trackpoints: trackpoints)      // adds an array of trackpoints to a track segment
                track.add(trackSegment: tracksegment)           // adds a track segment to a track
                root.add(track: track)                          // adds a track
                
               
                
                let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0] as URL
                        do {
                            try root.outputToFile(saveAt: url, fileName: ("LisMove-" + String(self.session.endTime.value!)))
                        }
                        catch {
                            print(error)
                        }
                        let file = url.appendingPathComponent("LisMove-" + String(self.session.endTime.value!) + ".gpx")

                        self.shareFile(file: file)
                
                
            }else{
                self.view.makeToast("La sessione non contiene coordinate")
            }
            

        }else{
            self.view.makeToast("La sessione non contiene coordinate")
        }

    }
    
    @IBAction func shareSessionDetail(_ sender: Any) {
        // text to share
        let text = "Ho concluso una sessione in bici con Lismove di \(self.session!.getTotalKM().rounded(toPlaces: 2)) km in \(self.session.getReadableDuration())" +
            " e ho guadagnato \(self.session.nationalPoints) punti community e \(self.session.initiativePoints()) punti Iniziativa.\n"
         
         // set up activity view controller
         let textToShare = [ text ]
         let activityViewController = UIActivityViewController(activityItems: textToShare, applicationActivities: nil)
         activityViewController.popoverPresentationController?.sourceView = self.view // so that iPads won't crash
         
         // exclude some activity types from the list (optional)
         activityViewController.excludedActivityTypes = []
         
         // present the view controller
         self.present(activityViewController, animated: true, completion: nil)
    }

    
    private func loadSessionDetail(){
        //check session points

        SESSIONInitiativePoint.text = String(self.session.getValidatedInitiativePoints())
        populateSessionInitiativePoints()
        
        populateErrorMessage(session: session)
        populateEmptySessionInitiativePoints(session: session)
        populateSendFeedbackButton()
        populateRefund()
        
        if (session.valid.value == nil){
            let alert = UIAlertController(title: "Attenzione", message: "I dati della sessione sono in elaborazione, i punti potrebbero cambiare.", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
            initPingRetrieve()
            
        }
        
    }
    
    func populateRefund(){
        if(!refundLoaded){
            refundHeaderLayout.isHidden = true
            refundDescription.isHidden = true
        }else{
            if(session.certificated && settings.contains(where: {$0.homeWorkRefund || $0.initiativeRefund})){
                refundHeaderLayout.isHidden = false
                refundDescription.isHidden = false
                refundAmount.text = session.euro.value?.getRoundedString() ?? "0.0"
                refundDescription.numberOfLines = 0
                var refundLabel = ""
                settings.filter({$0.homeWorkRefund || $0.initiativeRefund}).forEach({ settings in
                    if let point = session.sessionPoints.first(where: {$0.organizationId.value == settings.organizationId}){
                        refundLabel += "\(point.organizationTitle ?? ""): \(point.getRefundStatus()) \n"
                    }
                })
                refundDescription.text = refundLabel
            }else{
                refundHeaderLayout.isHidden = true
                refundDescription.isHidden = true
            }
            
        }
    }
    
    func populateSendFeedbackButton(){
        feedbackButtonLayout.isHidden = session.verificationRequired.value != nil
        let tagGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(goToFeedbackRequest))
        feedbackButtonLayout.addGestureRecognizer(tagGestureRecognizer)
    }
    
    @objc func goToFeedbackRequest(){
        performSegue(withIdentifier: goToFeedbackRequestSegue, sender: self)
    }

    @objc func openSessionPointDetail(){
        let storyBoard: UIStoryboard = UIStoryboard(name: "Session", bundle: nil)
        let modalController = storyBoard.instantiateViewController(withIdentifier: "InitiativePointsViewController") as! SessionPointsDetailTableViewController
        modalController.session = session
        modalController.modalPresentationStyle = .popover
        if let popoverPresentationController = modalController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        self.present(modalController, animated: true, completion: nil)
    }
    
    func populateSessionInitiativePoints(){

        sessionInitiativeLabel.text = "INIZIATIVA (x\(self.session.getInitiativeNumber()))"
        
        if(session.getValidatedInitiativePoints() == 0 || session.getInitiativeNumber()<2){
            SESSIONInitiativePoint.isHidden = false
            moreInitiativeImage.isHidden = true
            SESSIONNationalPoint.text = String(self.session.getValidatedNationalPoints())
            let gesture = UITapGestureRecognizer(target: self, action: #selector(openSessionPointDetail))
            SESSIONInitiativePoint.addGestureRecognizer(gesture)
        }else{
            
            SESSIONInitiativePoint.isHidden = true
            moreInitiativeImage.isHidden = false
            let gesture = UITapGestureRecognizer(target: self, action: #selector(openSessionPointDetail))
            moreInitiativeImage.addGestureRecognizer(gesture)
            
        }
       
    }
    
    func populateErrorMessage(session: Session){
        
        let errorMessage = session.getReadableStatusMessage()
        errorLabel.text = errorMessage
        errorImage.image = session.isVerified() ? UIImage.init(systemName: successImageName) : UIImage.init(systemName: errorImageName)
        errorImage.tintColor = session.isVerified() ? UIColor.systemGreen : UIColor.systemRed
        errorView.isHidden = false
        
    }
    
    @objc func showEmptyInitiativePointAlert(){
        let message = "I punti Iniziativa sono collegati ai progetti Lis Move promossi da organizzazioni quali Amministrazioni Comunali, Imprese o Istituti Scolastici.\n" +
        "\n" +
        "Possono essere accumulati esclusivamente con l'utilizzo del dispositivo hardware che viene fornito nel Kit Lis Move\n" +
        "\n" +
        "I motivi per cui non hai ricevuto i punti iniziativa possono essere i seguenti:" +
        "\n" +
        "・ Non hai pedalato in un'area compresa nel progetto,\n" +
        "・ Non hai inserito un codice Iniziativa,\n" +
        "・ Ci sono stati problemi con la connessione al dispositivo.\n"
        let alert = UIAlertController(title: "Perchè non ho ricevuto punti iniziativa?", message: message, preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        if( session.getValidatedInitiativePoints() == 0){
            if(session.verificationRequired.value == nil){
                alert.addAction(UIAlertAction(title: "Richiedi verifica manuale", style: .default, handler: { _ in
                    self.onlyPointsClicked = true
                    self.goToFeedbackRequest()
                }))
            }else if(session.verificationRequired.value == true){
                let action = UIAlertAction(title: "Verifica manuale richiesta", style: .default, handler: nil)
                action.isEnabled = false
                alert.addAction(action)
            }else{
                let action = UIAlertAction(title: "Verifica manuale effettuata", style: .default, handler:nil)
                action.isEnabled = false
                alert.addAction(action)
            }
        }
        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    
    func populateEmptySessionInitiativePoints(session: Session){
        let initiativaPoints = session.getValidatedInitiativePoints()
        emptyPointsLabel.isHidden = (initiativaPoints != 0)
        let openEmptyPointsReasonAlert = UITapGestureRecognizer(target: self, action: #selector(showEmptyInitiativePointAlert))
        emptyPointsLabel.addGestureRecognizer(openEmptyPointsReasonAlert)
    }
    
    private func initPingRetrieve(){
        if(self.sessionUpdateDaemon == nil){
            
            self.sessionUpdateDaemon = Timer.scheduledTimer(withTimeInterval: TimeInterval(10), repeats: true) { [self] timer in
                NetworkingManager.sharedInstance.getSessionDetail(uid: session.id!, completion: {result in
                    switch result {
                        case .success(let data):
                            self.session = data
                            if(session.certificated){
                                loadOrganizationSettings(session: data)
                            }else{
                                loadSessionDetail()
                            }
                            break
                        case .failure(let error):
                            //MARK: ERROR STREAM
                            LogHelper.log(message: error.localizedDescription)
                            /*NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])*/
                           break
                    }
                })
                
            }
        }

    }
    
    func loadOrganizationSettings(session: Session){
        self.refundLoaded = false
        settings = [OrganizationSettings]()

        if(session.sessionPoints.isEmpty){
            loadSessionDetail()
        }else{

            self.initiativeRepository.getOrganizationSettings(oids: session.sessionPoints.compactMap{$0.organizationId.value}) {result in
                switch result {
                    case .success(let data):

                        self.settings = data

                        self.refundLoaded = true
                        self.loadSessionDetail()

                    case .failure(_):
                        break

                }
            }
        }
    }
    
    private func shareFile(file: URL){

        // Make the activityViewContoller which shows the share-view
        let activityViewController = UIActivityViewController(activityItems: [file], applicationActivities: nil)
        activityViewController.popoverPresentationController?.sourceView = self.view
        
        // Show the share-view
        self.present(activityViewController, animated: true, completion: nil)
    }
     
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return 3
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = sessionDataTable.dequeueReusableCell(withIdentifier: "sessionInfoRow", for: indexPath) as! SessionInfoTableViewCell
        
        switch indexPath.row {
        case 0:
            cell.setupData(leftText: "DISTANZA", value: self.session.getTotalKM().getRoundedString(), valueUnit: "Km")
            break
            
        case 1:
            cell.setupData(leftText: "DURATA", value: session.getReadableDuration(), valueUnit: "")
            break
            
        case 2:
            var speed = 0.0
            if let duration = self.session.duration.value {
                speed = ((self.session.getTotalKM() / Double(duration)) * 3.6 * 1000).rounded(toPlaces: 2)
            }
            
            cell.setupData(leftText: "VELOCITA' MEDIA", value: "\(speed)", valueUnit: "Km/h", showDivider: false)
            break
            
        default:
            break
        }
    
        return cell
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let feedbackVC = segue.destination as? SessionFeedbackViewController{
            feedbackVC.sessionId = session.id ?? ""
            feedbackVC.isOnlyPoints = onlyPointsClicked
            self.onlyPointsClicked = false
            feedbackVC.onSessionUpdated = {session in
                self.updateSession(session: session)
            }
        }
    }
    
    private func updateSession(session: Session){
        self.session = session
        populateSendFeedbackButton()
        self.dismiss(animated: true, completion: nil)
    }


}
