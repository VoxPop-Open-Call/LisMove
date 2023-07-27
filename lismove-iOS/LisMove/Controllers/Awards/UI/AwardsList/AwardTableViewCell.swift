//
//  AwardTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import UIKit
import SkeletonView

class AwardTableViewCell: UITableViewCell {

    //@IBOutlet weak var positionNumberLabel: UILabel!
    //@IBOutlet weak var positionTitleLabel: UILabel!
    @IBOutlet weak var awardImage: UIImageView!
    //@IBOutlet weak var awardImageLeftMargin: NSLayoutConstraint!
    
    @IBOutlet weak var name: UILabel!
    @IBOutlet weak var value: UILabel!
    @IBOutlet weak var valueLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [awardImage,name, value, valueLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    func hideAnimation(){
        [awardImage,name, value, valueLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        awardImage.round()
    }
    
}
