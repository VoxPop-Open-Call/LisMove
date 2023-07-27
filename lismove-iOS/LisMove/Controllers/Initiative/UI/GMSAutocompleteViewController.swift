//
//  GMSAutocompleteViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import UIKit
import GooglePlaces
import GoogleMaps

class GMSAutocompleteViewController: UIViewController, GMSMapViewDelegate {

    var resultsViewController: GMSAutocompleteResultsViewController?
    var searchController: UISearchController?
    var resultView: UITextView?
    
    var currentUser: LismoveUser?
    
    @IBOutlet weak var gMaps: UIView!
    @IBOutlet weak var searcBarView: UIView!
    
    @IBOutlet weak var topMessageView: UIView!
    @IBOutlet weak var persistentMarkerMessageView: UIView!
    @IBOutlet weak var banner: UIView!
    
    var mapView: GMSMapView?
    
    public var address: String?
    public var isAddressComplete: Bool = false
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

        self.currentUser = DBManager.sharedInstance.getCurrentUser()
        
        self.initView()
        self.initPlaceAutocomplete()
 
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.markerClarificationMessage()
        }
        
    }
    
    
    
    private func markerClarificationMessage(){
        self.showBasicAlert(title: "", description: "Per il corretto funzionamento è importante che la posizione indicata sia corretta, controlla il marker sulla mappa prima di confermare")
    }
    
    
    private func initView() {
        
        //persistent message view customiz
        persistentMarkerMessageView.layer.cornerRadius = 16
        persistentMarkerMessageView.layer.borderColor  =  UIColor.lightGray.cgColor
        persistentMarkerMessageView.layer.borderWidth = 0.5
        persistentMarkerMessageView.layer.shadowColor = UIColor.gray.cgColor
        persistentMarkerMessageView.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        persistentMarkerMessageView.layer.shadowRadius = 1.5
        persistentMarkerMessageView.layer.shadowOpacity = 0.7
        
        //hide manual banner
        self.banner.isHidden = true
        
        //handle tap on banner
        //let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleBannerTap(_:)))
        //banner.addGestureRecognizer(tap)
        
    }
    
    @objc func handleBannerTap(_ sender: UITapGestureRecognizer? = nil) {
        
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "addressManualDetailNavigationController") as! UINavigationController
        let detailController = navController.topViewController as! GMSAutocompleteViewController_Manual
        
        
        //setup address
        if(self.selectedPlace != nil){
            let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlace)
            
            detailController.address = "\(address ?? ""), \(streetNumber ?? ""), \(city ?? "")"
        }else{
            
            detailController.address = searchController?.searchBar.text
        }
        
        
        
        //setup coordinates
        if(self.selectedCoordinates != nil){
            detailController.addressLatLng = self.selectedCoordinates!
        }else{
            if(self.currentUser?.homeLatitude.value != nil || self.currentUser?.homeLongitude.value != nil){
                detailController.addressLatLng = (self.currentUser!.homeLatitude.value!, self.currentUser!.homeLongitude.value!)
            }
        }
     
        
        detailController.onDoneBlock = { result in
            
            if((result.1) != nil){
                self.selectedPlace = (result.1)
            }
 
            if((result.0) != nil){
                self.selectedCoordinates = (result.0)
            }
            
            self.onDoneBlock!((self.selectedCoordinates, self.selectedPlace))
            
            self.dismiss(animated: true, completion: nil)
            

        }
        
        present(navController, animated: true, completion: nil)
    }
    

    private func initPlaceAutocomplete(){
        resultsViewController = GMSAutocompleteResultsViewController()
        resultsViewController?.delegate = self

        searchController = UISearchController(searchResultsController: resultsViewController)
        searchController?.searchResultsUpdater = resultsViewController
    
        searcBarView.addSubview(searchController!.searchBar)
        
        searchController?.searchBar.sizeToFit()
        searchController?.hidesNavigationBarDuringPresentation = false

        // When UISearchController presents the results view, present it in
        // this view controller, not one further up the chain.
        definesPresentationContext = true
        searchController?.searchBar.tintColor = .white
        searchController?.searchBar.placeholder = "Cerca Indirizzo"
        
        if let address = self.address {
            //load user selected address
            if(isAddressComplete){
                searchController?.searchBar.text = address
            }else{
                searchController?.searchBar.text = ""
            }
        }
        
        //check coordinates
        if(self.addressLatLng.0 != nil && self.addressLatLng.1 != nil){
            
            refreshMapView(lat: self.addressLatLng.0, lng: self.addressLatLng.1)
            
        }else if(self.address != nil){
            
            initAddressMapView()

        }else{
            
            //set new title
            self.navigationItem.title = "Segnala Fontanella"
            
            //self.view.makeToast("Tieni premuto sul marker e spostalo nella posizione corretta")
            
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
                                
                                
                            } else {
                                //show error
                                //manually choose address
                                self.searchController?.isActive = true
                            }

                        }else{
                            
                            //show error
                            //manually choose address
                            self.searchController?.isActive = true
                        }
                    
                    }
            }else {
                
                //manually choose address
                self.searchController?.isActive = true

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
    
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        /*if isBeingDismissed {
            self.onDoneBlock!((self.selectedCoordinates, self.selectedPlace))
        }*/
    }
    
    
    @IBAction func checkCompleteTap(_ sender: Any) {
        if self.selectedPlace != nil {
            let (city,address,streetNumber) = PlaceUtil.parseDataFromPlace(place: self.selectedPlace)
            
            if(city == nil || address == nil){
                self.view.makeToast("Inserisci un indirizzo completo")
                return
            }else{
                self.onDoneBlock!((self.selectedCoordinates, self.selectedPlace))
            }
        }else if isAddressComplete{
            self.onDoneBlock!((self.selectedCoordinates, self.selectedPlace))
        }else{
            self.view.makeToast("Inserisci un indirizzo completo")

        }
       
    }
    
}


// Handle the user's selection.
extension GMSAutocompleteViewController: GMSAutocompleteResultsViewControllerDelegate {
  func resultsController(_ resultsController: GMSAutocompleteResultsViewController,
                         didAutocompleteWith place: GMSPlace) {
    
    searchController?.isActive = false
    searchController?.searchBar.text = self.address
    // Do something with the selected place.
    //print("Place name: \(place.name)")
    //print("Place address: \(place.formattedAddress)")
    //print("Place attributions: \(place.attributions)")
    
    
    self.searchController?.searchBar.text = place.formattedAddress
    refreshMapView(place: place)
    
    //self.view.makeToast("Tieni premuto sul marker e spostalo nella posizione corretta")
  }

  func resultsController(_ resultsController: GMSAutocompleteResultsViewController,
                         didFailAutocompleteWithError error: Error){
      
      
      //MARK: ERROR STREAM
      NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "GMaps Exception: \(error.localizedDescription)"])
    
  }

  // Turn the network activity indicator on and off again.
  func didRequestAutocompletePredictions(forResultsController resultsController: GMSAutocompleteResultsViewController) {
    UIApplication.shared.isNetworkActivityIndicatorVisible = true
  }

  func didUpdateAutocompletePredictions(forResultsController resultsController: GMSAutocompleteResultsViewController) {
    UIApplication.shared.isNetworkActivityIndicatorVisible = false
  }
}

extension GMSMarker {
    func setIconSize(scaledToSize newSize: CGSize) {
        UIGraphicsBeginImageContextWithOptions(newSize, false, 0.0)
        icon?.draw(in: CGRect(x: 0, y: 0, width: newSize.width, height: newSize.height))
        let newImage: UIImage = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()
        icon = newImage
    }
}
