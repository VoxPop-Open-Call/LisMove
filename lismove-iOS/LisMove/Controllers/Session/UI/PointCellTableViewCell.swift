//
//  PointCellTableViewCell.swift
//  LisMove
//
//

import UIKit

class PointCellTableViewCell: UITableViewCell {
    @IBOutlet weak var leftLabel: UILabel!
    
    @IBOutlet weak var rightLabel: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setupCell(data: PointItemUI){
        leftLabel.text = data.name
        rightLabel.text = data.point
    }
}
