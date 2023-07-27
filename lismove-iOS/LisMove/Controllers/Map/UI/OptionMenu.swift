//
//  FoodMenu.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import Foundation
import Sheeeeeeeeet

class OptionMenu: Menu {
    
    override init(title: String? = nil, items: [MenuItem]) {
        let title = title ?? ""
        super.init(title: title, items: items)
    }
    
    static var cancelButton: MenuItem {
        CancelButton(title: "Annulla")
    }
    
    static var okButton: MenuItem {
        OkButton(title: "OK")
    }
    
    static var title: String {
        "Seleziona impostazioni"
    }
}
