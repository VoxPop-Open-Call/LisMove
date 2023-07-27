//
//  msgCollectionViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import MaterialComponents.MaterialCards


class MsgCollectionViewCell: MDCCardCollectionCell {
    
    @IBOutlet weak var msgNumber: UILabel!
    @IBOutlet weak var msgLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        showAnimation()
    }
    
    func showAnimation(){
        [msgNumber, msgLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }
    
    func setData(messages: Int, showLabel: Bool){
        msgNumber.text = String(messages)
        msgLabel.isHidden = !showLabel
    }
    
    

    
    func hideAnimation(){
        [msgNumber, msgLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
}
