//
//  PointsCollectionViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import SkeletonView
import MaterialComponents.MaterialCards

class PointsCollectionViewCell: MDCCardCollectionCell {
    
    @IBOutlet weak var listOfPoints: UIStackView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [listOfPoints].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }


    
    func hideAnimation(){
        [listOfPoints].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    func setupCell(data: [HomePointItemData]){
        listOfPoints.arrangedSubviews.forEach({$0.removeFromSuperview()})
        data.forEach({ item in
            let view = HomePointItemView()
            view.setupItemView(data: item)
            listOfPoints.addArrangedSubview(view)
        })
        
        
    }

}
