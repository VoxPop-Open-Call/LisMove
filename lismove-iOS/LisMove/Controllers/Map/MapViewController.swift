//
//  MapViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 17/05/21.
//

import UIKit
import GoogleMaps
import Floaty
import Toast_Swift
import SwiftLog
import SwiftLocation
import Sheeeeeeeeet
import Resolver

class MapViewController: UIViewController, GMSMapViewDelegate {
    @IBOutlet weak var googleMapsFrame: UIView!
    var mapView: GMSMapView?
    @Injected var repository: FountainRepository
    
    let user = DBManager.sharedInstance.getCurrentUser()
    var fountainList: [Fountain] = []
    var menuOptions : [String: MenuOption] = [:]
    var isMapInitialized = false
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if(isMapInitialized){
            reloadFountainsAndPolygon()
        }else{
            
            menuOptions.removeAll()
            
            //init menuOptions
            menuOptions["Fontanelle"] = MenuOption(name: "Fontanelle", isSelected: true)
            
            
            SessionManager.sharedInstance.pointManager?.organizationList.forEach{ item in
                menuOptions[item.title!] = MenuOption(name: item.title!, isSelected: true)
            }
            
            
            initView()
        }
    }
    
    func reloadFountainsAndPolygon(){
        mapView?.clear()
        
        initInitiativePath()
        downloadFountainList()
    }
    
    private func initView(){
        
        SwiftLocation.gpsLocation().then(queue: .main){[self] result in
            
            switch result {
            case .success(let location):
            
                //MARK: INIT MAP
                let camera = GMSCameraPosition.camera(withLatitude: location.coordinate.latitude, longitude: location.coordinate.longitude, zoom: 13.0)
                mapView = GMSMapView.map(withFrame: self.googleMapsFrame.frame, camera: camera)
                mapView?.isMyLocationEnabled = true
                mapView?.delegate = self
                self.googleMapsFrame.addSubview(mapView!)
                isMapInitialized = true
                reloadFountainsAndPolygon()
                
            case .failure(let error):
                break
                
            }
        }

    }
    
    @IBAction func goToMyLocation(_ sender: Any) {
        guard let lat = mapView?.myLocation?.coordinate.latitude,
              let lng = mapView?.myLocation?.coordinate.longitude else { return }

        let camera = GMSCameraPosition.camera(withLatitude: lat ,longitude: lng , zoom: 13.0)
        mapView?.animate(to: camera)
    }
    
    
    @IBAction func tapChooseMenuItem(_ sender: UIBarButtonItem) {

        let menu = MultiSelectMenu(options: Array(menuOptions.values))
        let sheet = menu.toActionSheet {[self] sheet, item in
            
            if(item.title == "Fontanelle"){
                
                if(menuOptions["Fontanelle"]?.isSelected == true){
                    //hide fountain
                    mapView?.clear()
                    
                    initInitiativePath()
                    
                    menuOptions["Fontanelle"]?.isSelected = false
                    
                }else{
                    //show fountain
                    initFountainList()
                    menuOptions["Fontanelle"]?.isSelected = true
                    
                }
                
            }else{
                
                if(menuOptions[item.title]?.isSelected == true){
                    
                    menuOptions[item.title]?.isSelected = false
                    
                    
                    //hide fountain
                    mapView?.clear()

                    
                    if(menuOptions["Fontanelle"]?.isSelected == true){
                        initFountainList()
                    }
                    
                    initInitiativePath()
  
 
                }else{

                    menuOptions[item.title]?.isSelected = true
                    
                    initInitiativePath()
                }
                
            }
            
        }
    

        sheet.present(in: self, from: self.view)

    }
    
    
    
    
    private func downloadFountainList(){
    
        repository.getActiveFountainList(onCompletition: {
            fountains, error in
            guard error == nil else{
                self.view.makeToast("Errore durante il download delle fontanelle")
                return
            }
            self.fountainList = fountains ?? []
            DispatchQueue.main.async {
                self.initFountainList()
            }
        })
        
    }
    
    
    private func initFountainList(){
        self.fountainList.forEach{item in
            
            // Creates a marker
            let marker = GMSMarker()
            marker.position = CLLocationCoordinate2D(latitude: item.lat, longitude: item.lng)
            marker.title = item.name
            marker.icon = UIImage(named: "fontanella")
            marker.map = mapView
            marker.isTappable = true
            
        }
    }
    
    private func initInitiativePath(){
        
        for (key,_) in menuOptions {
            
            if(key != "Fontanelle"){
                if(menuOptions[key]!.isSelected){
                    let org = SessionManager.sharedInstance.pointManager?.organizationList.first(where: {$0.title == key})
                    
                    if(SessionManager.sharedInstance.pointManager?.polygonList[org!.id!] != nil){
                        
                        SessionManager.sharedInstance.pointManager?.polygonList[org!.id!]!.forEach{mkPolygon in
                            let polygon = GMSPolygon()
                            let rect = GMSMutablePath()
                            
                            
                            mkPolygon.coordinates.forEach{cord in
                                rect.add(cord)
                            }
                            
                            polygon.path = rect
                            polygon.fillColor = UIColor.systemRed.withAlphaComponent(0.3)
                            polygon.strokeColor = UIColor.systemRed
                            polygon.strokeWidth = 2
                            polygon.map = mapView
                        }
                        
                    }
        
                }
            }
        }
        
    }

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        showDrinkingFountainActionDialog(marker)
        return true
    }
    
    fileprivate func showDrinkingFountainActionDialog(_ marker: GMSMarker) {
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        let navigateAction = UIAlertAction(title: "Navigare verso la fontanella", style: .default, handler: { action in
            
            let lat = marker.position.latitude
            let lng = marker.position.longitude
            
            var alert = UIAlertController(title: "Seleziona Provider", message: nil, preferredStyle: .actionSheet)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            let googleMaps = UIAlertAction(title: "Google Maps", style: .default){
                UIAlertAction in

                self.navigateToDrinkingFountain(lat, lng, "Google")
                
                
            }
            let appleMaps = UIAlertAction(title: "Apple Maps", style: .default){
                UIAlertAction in

                self.navigateToDrinkingFountain(lat, lng, "Apple")
                
            }
            let cancelAction = UIAlertAction(title: "Annulla", style: .cancel){
                UIAlertAction in
            }
            
            alert.addAction(googleMaps)
            alert.addAction(appleMaps)
            
            self.present(alert, animated: true, completion: nil)
            


        })
        
        let cancelAction = UIAlertAction(title: "Chiudi", style: .cancel, handler: nil)
        let deleteAction = UIAlertAction(title: "Seganla la fontanella come inesistente", style: .destructive, handler: { action in
            self.deleteDrinkingFountain(marker)
        })

        alert.addAction(navigateAction)
        alert.addAction(deleteAction)
        alert.addAction(cancelAction)

        self.present(alert, animated: true, completion: nil)
    }
    

    func deleteDrinkingFountain(_ marker: GMSMarker){
        let lat = marker.position.latitude
        let lng = marker.position.longitude
        
        view.makeToast("Eliminazione fontanella in corso...")
        guard let fountain = fountainList.first(where: {$0.lat == lat && $0.lng == lng}) else {return}
            
            repository.deleteFountain(fountain: fountain, uid: user?.uid ?? "", onCompletition: { error in
                guard error == nil else{
                    self.view.makeToast(error?.localizedDescription)
                    return
                }
                self.view.makeToast("Fontanella eliminata con successo")
                self.removeFountainFromMap(fountain: fountain, marker: marker)
            })
    }
    
    func removeFountainFromMap(fountain: Fountain, marker: GMSMarker){
        let lat = marker.position.latitude
        let lng = marker.position.longitude
        if let index = fountainList.firstIndex(where: {$0.lat == lat && $0.lng == lng}) {
            marker.map = nil
            fountainList.remove(at: index)
        }
    }
    
    func navigateToDrinkingFountain(_ lat: CLLocationDegrees, _ lng: CLLocationDegrees, _ type: String){
        let directionsURL = (type == "Apple") ?  "http://maps.apple.com/?daddr=\(lat),\(lng)&dirflg=c" : "http://maps.google.com/maps?daddr=\(lat),\(lng)"
        
        
        guard let url = URL(string: directionsURL) else {
            return
        }
        if #available(iOS 10.0, *) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        } else {
            UIApplication.shared.openURL(url)
        }
       
    }
}
