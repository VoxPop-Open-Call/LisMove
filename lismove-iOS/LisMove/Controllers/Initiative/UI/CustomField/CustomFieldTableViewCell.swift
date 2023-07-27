//
//  CustomFieldTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 22/10/21.
//

import UIKit
import Foundation

class CustomFieldTableViewCell: UITableViewCell {


    @IBOutlet weak var checkbox: UISwitch!
    @IBOutlet weak var checkboxLabel: UILabel!
    
    var checkAction: (() -> Void)? = nil
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
   
    }

    
    @IBAction func switchChange(_ sender: Any) {
        self.checkAction?()
    }
    
    
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
