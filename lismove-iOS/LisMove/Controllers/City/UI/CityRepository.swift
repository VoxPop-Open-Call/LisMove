//
//  CityRepository.swift
//  LisMove
//
//

import Foundation
class CityRepository{
    

    static func getCityCode(byName name: String)-> Int?{
        if name == "" { return nil }
        let cities = getAllCity()
        
        let city = cities?.filter { $0.nome == name }.first
        
        guard let code = city?.codice  else {
            return nil
        }
        
        return Int(code)

    }
    
    static func getCity(byName name: String)-> Comune?{
        if name == "" { return nil }
        let cities = getAllCity()
        
        let city = cities?.filter { $0.nome == name }.first
        return city
    }
    
    static func getCityName(byCode code: Int?)-> String?{
        return getCity(byCode: code)?.nome
    }
    
    static func getCity(byCode code: Int?)-> Comune?{
        guard code != nil else {return nil}
        let cities = getAllCity()
        
        let city = cities?.filter { Int($0.codice ?? "-1") == code }.first
        
        return city

    }
    static func getAllCity() -> [Comune]?{
        
        let decoder = JSONDecoder()
        if let file = Bundle.main.url(forResource: "comuni", withExtension: "json") {
            let json = try? Data(contentsOf: file)
            
            if let cities = try? decoder.decode([Comune].self, from: json!) {
                return cities
            }else{
                return nil
            }
            
        } else {
            LogHelper.logError(message: "Canno't find comuni file", withTag: "CityRepository")
            return nil
        }
        
    }
    
    static func searchCity(code: Int) -> [String? : String?] {

        if let city = getCity(byCode: code){
            return [city.nome : city.provincia?.nome]
        }else{
            return ["":""]
        }
     
    }
    
}
