//
//  SessionInfoTableViewCell.swift
//  LisMove
//
//

import UIKit

class SessionInfoTableViewCell: UITableViewCell {

    @IBOutlet weak var divider: UIView!
    @IBOutlet weak var valueLabel: UILabel!
    @IBOutlet weak var valueUnitLabel: UILabel!
    @IBOutlet weak var leftLabel: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    func setupData(leftText: String, value: String, valueUnit: String, showDivider: Bool = true){
        leftLabel.text = leftText
        valueUnitLabel.text = valueUnit
        valueLabel.text = value
        divider.isHidden = !showDivider
    }
}
