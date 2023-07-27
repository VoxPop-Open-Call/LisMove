//
//  UserTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import SkeletonView
import MaterialComponents.MaterialCards

class UserTableViewCell: MDCCardCollectionCell {

    @IBOutlet weak var username: UILabel!
    @IBOutlet weak var avatarurl: UIImageView!
    @IBOutlet weak var numberOfDays: UILabel!
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [username,avatarurl,numberOfDays].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }
    
    func hideAnimation(){
        [username,avatarurl,numberOfDays].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    override func layoutSubviews() {
        avatarurl.round()
    }


}
