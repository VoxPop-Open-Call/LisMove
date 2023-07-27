//
//  GMSAutocompleteViewController_Manual.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 07/01/22.
//

import Foundation

import UIKit
import GooglePlaces
import GoogleMaps

class GMSAutocompleteViewController_Manual: UIViewController, GMSMapViewDelegate {
    
    @IBOutlet weak var searcBarView: UIView!
    @IBOutlet weak var gMaps: UIView!
    
    @IBOutlet weak var topMessageView: UIView!
    @IBOutlet weak var persistentMarkerMessageView: UIView!
    
    @IBOutlet weak var addressLabel: UITextField!
    @IBOutlet weak var addressNumberLabel: UITextField!
    @IBOutlet weak var addressCityLabel: UITextField!
    @IBOutlet weak var refreshMapPinButton: UIButton!
    
    var mapView: GMSMapView?
    
    public var address: String?
    public var addressLatLng: (Double?, Double?)
    
    var selectedCoordinates: (Double, Double)?
    var selectedPlace: GMSPlace?
    
    var onDoneBlock : ((((Double, Double)?, GMSPlace?)) -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }

        
        initView()
        initPlaceAutocomplete()

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.markerClarificationMessage()
        }
    }
    
    private func markerClarificationMessage(){
        self.showBasicAlert(title: "", description: "Per il corretto funzionamento è importante che la posizione indicata sia corretta, controlla il marker sulla mappa prima di confermare")
    }
    
    private func initView() {
        
        //keyboard handler
        self.hideKeyboardWhenTappedAround()
        
        //customzie refresh map button
        refreshMapPinButton.layer.borderWidth = 0.5
        refreshMapPinButton.layer.borderColor  =  UIColor.lightGray.cgColor
        
        //persistent message view customiz
        persistentMarkerMessageView.layer.cornerRadius = 16
        persistentMarkerMessageView.layer.borderColor  =  UIColor.lightGray.cgColor
        persistentMarkerMessageView.layer.borderWidth = 0.5
        persistentMarkerMessageView.layer.shadowColor = UIColor.gray.cgColor
        persistentMarkerMessageView.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        persistentMarkerMessageView.layer.shadowRadius = 1.5
        persistentMarkerMessageView.layer.shadowOpacity = 0.7
        
        
        checkSelectedPlace()
        
        //add city tap recognize
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleCityTap(_:)))
        addressCityLabel.isUserInteractionEnabled = true
        addressCityLabel.addGestureRecognizer(tap)
    }
    
    @objc func handleCityTap(_ sender: UITapGestureRecognizer? = nil) {
        
        //open terms controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let cityController = storyBoard.instantiateViewController(withIdentifier: "cityController") as! CityTableViewController
        cityController.modalPresentationStyle = .popover
        if let popoverPresentationController = cityController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        self.present(cityController, animated: true, completion: nil)
        
        cityController.onDoneBlock = { result in
            if let _ =  result.keys.first{
                self.addressCityLabel.text = result.first?.value
                
            }
        }
        
    }
    
    private func checkSelectedPlace(){
        //check if user has already selected a place into previous controller
        if let _ = self.selectedPlace{
            
            loadAddressInfoIntoLables()
            
        }else{
            
            GMSPlacesClient.shared().findAutocompletePredictions(fromQuery: self.address!, filter: nil, sessionToken: nil, callback:{prediction, error in
                
                if error == nil {
                    GMSPlacesClient.shared().lookUpPlaceID(prediction?.first?.placeID ?? "") { (place, error) -> Void in
                            
                            if error == nil{
                                if let place = place {
                                
                                    self.selectedPlace = place
                                    
                                    self.loadAddressInfoIntoLables()
                                    
                                }

                            }
                        
                        }
                }
                
            })
        }
    }
    
    private func loadAddressInfoIntoLables(){
        let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlace)
        
        self.addressLabel.text = address
        self.addressNumberLabel.text = streetNumber
        self.addressCityLabel.text = city
    }

    private func initPlaceAutocomplete(forceAddress: Bool = false){
        
        
        //check if user has manual coordinates
        if(self.addressLatLng.0 != nil && self.addressLatLng.1 != nil && !forceAddress){
            
            refreshMapView(lat: self.addressLatLng.0, lng: self.addressLatLng.1)
            
        }else{
            let address = self.addressLabel.text
            let streetNumber = self.addressNumberLabel.text
            let city = self.addressCityLabel.text
            
            self.address = "\(address ?? ""),\(streetNumber ?? ""), \(city ?? "")"
            
            if (self.address != nil || !(self.address?.isEmpty ?? true)) {
                
                //retrieve place from user address
                initAddressMapView()
                
                
            }else{
                self.view.makeToast("Errore nel caricamento dell'indirizzo. Compila i campi")
            }
        }
        
    }

    func initAddressMapView(){
        retrieveAndShowAddressCoordinates()
    }

    func retrieveAndShowAddressCoordinates(){
        GMSPlacesClient.shared().findAutocompletePredictions(fromQuery: self.address!, filter: nil, sessionToken: nil, callback:{prediction, error in
            
            
            if error == nil {
                GMSPlacesClient.shared().lookUpPlaceID(prediction?.first?.placeID ?? "") { (place, error) -> Void in
                        
                        if error == nil{
                            if let place = place {
                            
                                self.selectedPlace = place
                                self.addressLatLng = (place.coordinate.latitude,place.coordinate.longitude)
                                
                                self.refreshMapView(place: place)
                                
                                
                            }

                        }
                    
                    }
            }
            
        })
    }
    
    private func refreshMapView(place: GMSPlace? = nil, lat: Double? = nil, lng: Double? = nil){
        mapView?.clear()
        if(place != nil){
            if(self.mapView == nil){
                let camera = GMSCameraPosition.camera(withLatitude: place!.coordinate.latitude, longitude: place!.coordinate.longitude, zoom: 16.0)
                mapView = GMSMapView.map(withFrame: self.view.frame, camera: camera)
                mapView?.isMyLocationEnabled = true
                mapView?.settings.myLocationButton = true
                mapView?.delegate = self
                self.gMaps.addSubview(mapView!)
            
            }else{
                let camera = GMSCameraPosition.camera(withLatitude: place!.coordinate.latitude, longitude: place!.coordinate.longitude, zoom: 16.0)
                mapView?.animate(to: camera)
            }
            // Creates a marker in the center of the map.
            let marker = GMSMarker()
            marker.position = CLLocationCoordinate2D(latitude: place!.coordinate.latitude, longitude: place!.coordinate.longitude)
            marker.title = place!.formattedAddress
            marker.isDraggable = true
            marker.map = mapView
            //marker.setIconSize(scaledToSize: .init(width: 56, height: 56))
            
            self.selectedCoordinates = (place!.coordinate.latitude, place!.coordinate.longitude)
            self.selectedPlace = place!
            
            //self.view.makeToast("Tieni premuto sul marker e spostalo nella posizione corretta")
            
            
            
        }else if(lat != nil && lng != nil){
            
            if(self.mapView == nil){
                
                let camera = GMSCameraPosition.camera(withLatitude: lat!, longitude: lng!, zoom: 18.0)
                mapView = GMSMapView.map(withFrame: self.view.frame, camera: camera)
                mapView?.isMyLocationEnabled = true
                mapView?.settings.myLocationButton = true
                mapView?.delegate = self
                self.gMaps.addSubview(mapView!)
            
            }else{
                let camera = GMSCameraPosition.camera(withLatitude: lat!, longitude: lng!, zoom: 18.0)
                mapView?.animate(to: camera)
            }
            
            // Creates a marker in the center of the map.
            let marker = GMSMarker()
            marker.position = CLLocationCoordinate2D(latitude: lat!, longitude: lng!)
            marker.title = "Nuova Fontanella"
            marker.isDraggable = true
            marker.map = mapView
            //marker.setIconSize(scaledToSize: .init(width: 56, height: 56))
            
            self.selectedCoordinates = (lat!, lng!)
            
            //self.view.makeToast("Tieni premuto sul marker e spostalo nella posizione corretta")
            
            
        }else{
            self.view.makeToast("Impossibile inizializzare la mappa")
        }
    }
    
    
    func mapView(_ mapView: GMSMapView, didEndDragging marker: GMSMarker) {
        //self.view.makeToast("Coordinate selezionate: \(marker.position.latitude) - \(marker.position.longitude)")
        
        //set new coordinates
        self.selectedCoordinates = (marker.position.latitude,marker.position.longitude)
    }
    
    
    
    @IBAction func checkCompleteTap(_ sender: Any) {
        self.onDoneBlock!((self.selectedCoordinates, self.selectedPlace))
    }
    
    @IBAction func refreshMapPin(_ sender: Any) {
        view.endEditing(true)

        
        self.initPlaceAutocomplete(forceAddress: true)
        
        
    }
    

}
