//
//  DeviceCollectionViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import SkeletonView
import MaterialComponents.MaterialCards

class DeviceCollectionViewCell: MDCCardCollectionCell {
    
    @IBOutlet weak var deviceButton: UIButton!
    @IBOutlet weak var deviceRefreshBUtton: UIButton!
    
    var refreshTappedAction: (() -> Void)? = nil
    var configTappedAction: (() -> Void)? = nil
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [deviceButton,deviceRefreshBUtton].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }
    
    func hideAnimation(){
        [deviceButton,deviceRefreshBUtton].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    @IBAction func refreshAction(_ sender: Any) {
        refreshTappedAction?()
    }
    
    @IBAction func configSensorAction(_ sender: Any) {
        configTappedAction?()
    }
    
    func setDevice(name: String?, connected: Bool?){
        if let name = name{
            deviceButton.isHidden = false
            
            deviceButton.setTitle(name, for: .normal)
            if(connected ?? false){
                deviceButton.backgroundColor = UIColor.systemGreen
                deviceButton.setImage(UIImage(named: "icons8-bluetooth-40"), for: .normal)
            }else{
                deviceButton.backgroundColor = UIColor.systemGray3
                deviceButton.setImage(UIImage(named: "icons8-bluetooth-40"), for: .normal)

            }
            
        }else{
            deviceButton.isHidden = true
        }
    }
}
