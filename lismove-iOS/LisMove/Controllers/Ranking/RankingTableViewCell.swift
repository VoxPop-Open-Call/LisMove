//
//  RankingTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import UIKit
import SkeletonView


class RankingTableViewCell: UITableViewCell {

    @IBOutlet weak var positionNumberLabel: UILabel!
    @IBOutlet weak var positionTitleLabel: UILabel!
    @IBOutlet weak var avatarImage: UIImageView!
    
    @IBOutlet weak var nickname: UILabel!
    @IBOutlet weak var points: UILabel!
    @IBOutlet weak var pointLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [positionNumberLabel,positionTitleLabel,avatarImage,nickname,points,pointLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func hideAnimation(){
        [positionNumberLabel,positionTitleLabel,avatarImage,nickname,points,pointLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarImage.round()
    }

}
