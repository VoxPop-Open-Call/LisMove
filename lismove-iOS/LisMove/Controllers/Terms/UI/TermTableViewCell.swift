//
//  TermTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/04/21.
//

import UIKit

class TermTableViewCell: UITableViewCell {

    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var termSwitch: UISwitch!
    
    var callback : ((Bool)->())?

    @IBAction func switchChanged(_ sender : UISwitch) {
        callback?(sender.isOn)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
