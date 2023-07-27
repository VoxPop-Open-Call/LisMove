//
//  moneyCollectionViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import MaterialComponents.MaterialCards


class MoneyCollectionViewCell: MDCCardCollectionCell {
    
    @IBOutlet weak var euro: UILabel!
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [euro].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    
    func hideAnimation(){
        [euro].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
}
