//
//  ChoseRankingTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 20/07/21.
//

import UIKit

class ChoseRankingTableViewCell: UITableViewCell {

    @IBOutlet weak var organizationLogo: UIImageView!
    @IBOutlet weak var rankingTitle: UILabel!
    @IBOutlet weak var rankingSubtitle: UILabel!
    
    @IBOutlet weak var rankingDate: UILabel!
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
