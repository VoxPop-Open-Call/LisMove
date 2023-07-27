//
//  MyAwardTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import UIKit
import SkeletonView

class MyAwardTableViewCell: UITableViewCell {

    @IBOutlet weak var myAwardImage: UIImageView!
    @IBOutlet weak var awardCategory: UILabel!
    @IBOutlet weak var awardName: UILabel!
    @IBOutlet weak var awardQuantity: UILabel!
    @IBOutlet weak var awardType: UILabel!
    
    @IBOutlet weak var couponCheckImage: UIImageView!
    @IBOutlet weak var couponCheckLabel: UILabel!
    
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [myAwardImage,awardCategory,awardName,awardQuantity,awardType,couponCheckImage,couponCheckLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    func hideAnimation(){
        [myAwardImage,awardCategory,awardName,awardQuantity,awardType,couponCheckImage,couponCheckLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        myAwardImage.round()
    }

}
