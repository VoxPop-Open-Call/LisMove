//
//  UserAddressTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 10/07/21.
//

import UIKit

class UserAddressTableViewCell: UITableViewCell {

    @IBOutlet weak var name: UITextField!
    @IBOutlet weak var address: UITextField!
    @IBOutlet weak var number: UITextField!
    @IBOutlet weak var city: UITextField!
    @IBOutlet weak var deleteAddress: UIButton!
    
    private var selectedCity: [String: String] = [:]
    
    var deleteAddressAction: (() -> Void)? = nil
    
    var type = CellType.Normal
    
        
    enum CellType{
        case Normal, Organization
    }
        
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        if(self.type == CellType.Organization){
            self.name.isUserInteractionEnabled = false
            self.name.backgroundColor = .gray
            self.address.isUserInteractionEnabled = false
            self.address.backgroundColor = .gray
            self.number.isUserInteractionEnabled = false
            self.number.backgroundColor = .gray
            self.city.isUserInteractionEnabled = false
            self.city.backgroundColor = .gray
        }
        
        //city tap action
        //init homeComune gesture
        let tapHome = UITapGestureRecognizer(target: self, action: #selector(cityTap))
        self.city.isUserInteractionEnabled = true
        self.city.addGestureRecognizer(tapHome)
        
    }
    
    @IBAction func cityTap(sender: UITapGestureRecognizer) {

        //open city controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let cityController = storyBoard.instantiateViewController(withIdentifier: "cityController") as! CityTableViewController
        cityController.modalPresentationStyle = .popover
        if let popoverPresentationController = cityController.popoverPresentationController {
            popoverPresentationController.sourceView = self.window?.rootViewController?.view
            popoverPresentationController.sourceRect = CGRect(x: (self.window?.rootViewController?.view.bounds.size.width)! / 2.0, y: (self.window?.rootViewController?.view.bounds.size.height)! / 2.0, width: 1.0, height: 1.0)
        }

    
        self.window?.rootViewController?.present(cityController, animated: true, completion: nil)
        
        cityController.onDoneBlock = { result in
            self.selectedCity = result
            self.city.text = result.values.first
        }
    }
    
    
    @IBAction func removeTap(_ sender: Any) {
        deleteAddressAction?()
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
