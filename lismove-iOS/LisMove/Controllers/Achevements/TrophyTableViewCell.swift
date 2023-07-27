//
//  TrophyTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import UIKit
import SkeletonView



class TrophyTableViewCell: UITableViewCell {

    
    @IBOutlet weak var trophyImage: UIImageView!
    @IBOutlet weak var trophyTitle: UILabel!
    @IBOutlet weak var trophyProgressBar: UIProgressView!
    @IBOutlet weak var trophyProgressText: UILabel!
    @IBOutlet weak var throphySubtitle: UILabel!
    @IBOutlet weak var fulfilledImage: UIImageView!
    @IBOutlet weak var thropyCountdown: UILabel!
    @IBOutlet weak var thropyFulfilledLabel: UILabel!
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [trophyImage,trophyTitle,trophyProgressBar,trophyProgressText, throphySubtitle, fulfilledImage, thropyCountdown, thropyFulfilledLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func hideAnimation(){
        [trophyImage,trophyTitle,trophyProgressBar,trophyProgressText, throphySubtitle, fulfilledImage, thropyCountdown, thropyFulfilledLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    override func layoutSubviews() {
        trophyImage.round()
    }
    
    

}
