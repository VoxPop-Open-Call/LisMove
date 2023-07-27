//
//  Term.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/04/21.
//

import Foundation

struct Term {
    
    var title = ""
    var isSelected: Bool
    
    init(text: String){
        self.title = text
        self.isSelected = false
    }
}
