//
//  EscursionTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 17/05/21.
//

import UIKit
import Cosmos
import FaveButton
import SkeletonView

class SessionTableViewCell: UITableViewCell {

    @IBOutlet weak var date: UILabel!
    
    @IBOutlet weak var distanceText: UILabel!
    @IBOutlet weak var timeLabel: UILabel!
    
    @IBOutlet weak var nationalPoint: UILabel!
    @IBOutlet weak var initiativePoint: UILabel!
    
    @IBOutlet weak var syncStatus: UIImageView!
    
    //    @IBOutlet weak var likeButton: FaveButton!
//    var like_clicked = false
    
//    @IBOutlet weak var starsRating: CosmosView!
    
//    var onLikeTap: ((Any) -> Void)?
    
//    @IBAction func onLikeTap(_ sender: Any) {
//        self.onLikeTap?(sender)
//    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [distanceText,timeLabel,date,nationalPoint,initiativePoint, syncStatus].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
        
//        starsRating.isHidden = true
        
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func hideAnimation(){
        [distanceText,timeLabel,date,nationalPoint,initiativePoint, syncStatus].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
        
//        starsRating.isHidden = false
    }
    

}
