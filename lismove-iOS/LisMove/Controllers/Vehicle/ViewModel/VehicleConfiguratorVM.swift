//
//  VehicleConfiguratorVM.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 12/08/21.
//

import Foundation


class VehicleConfiguratorVM: ObservableObject {
    
    //controller data
    @Published var cars: [CarGeneration] = []
    @Published var brand: [CarBrand] = []
    @Published var models: [CarModel] = []
    
    //state management
    var selectedBrand: CarBrand?
    var selectedModel: CarModel?
    var selectedCar: CarGeneration?
    var carModification: CarModification?
    
    let user = DBManager.sharedInstance.getCurrentUser()

    @Published var downloadMode = false
    @Published var success = false
    
    init(){
        
        self.syncCarBrands()
        
    }
    
    public func syncCarBrands(){
        downloadMode = true
        
        NetworkingManager.sharedInstance.getCarBrand(completion: { result in
            switch result {
                case .success(let data):
                    
                    self.brand = data
                    self.downloadMode = false
            
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        })
    }
    
    public func syncCarModels(bid: Int){
        downloadMode = true
        
        NetworkingManager.sharedInstance.getCarModels(bid: bid,completion: { result in
            switch result {
                case .success(let data):
                    
                    self.models = data
                    self.downloadMode = false
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        })
    }
    
    public func syncCarGenerations(bid: Int, mid: Int){
        downloadMode = true
        
        NetworkingManager.sharedInstance.getCarGenerations(bid: bid, mid: mid, completion: { result in
            switch result {
                case .success(let data):
                    
                    self.cars = data
                    self.downloadMode = false
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        })
    }

    
    public func saveUserCar(){
        NetworkingManager.sharedInstance.getCarModifications(bid: self.selectedBrand!.id!, mid: self.selectedModel!.id!, gid: self.selectedCar!.id!, completion: { result in
            switch result {
                case .success(let data):

                    NetworkingManager.sharedInstance.saveUserCar(uuid: self.user!.uid!, car: data.first!, completion: { result in
                        switch result{
                            case true:
                                
                                self.success = true
                                
                            
                            case false:
                                //MARK: ERROR STREAM
                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile salvare il veicolo"])
                        }
                    })
            
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
        

    }
}
