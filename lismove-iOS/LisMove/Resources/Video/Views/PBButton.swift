//
//  PBButton.swift
//  LisMove
//
//

import UIKit
@IBDesignable
class PBButton: UIButton {
        var borderColor = UIColor.white.cgColor
        @IBInspectable var titleText: String? {
            didSet {
                self.setTitle(titleText, for: .normal)
                self.setTitleColor(UIColor.white,for: .normal)
            }
        }

        override init(frame: CGRect){
            super.init(frame: frame)
            setup()
        }

        required init?(coder: NSCoder) {
            super.init(coder: coder)
            setup()
        }
    
        override func prepareForInterfaceBuilder() {
            setup()
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            setup()
        }
    
   

        func setup() {
            self.clipsToBounds = true
            self.layer.cornerRadius = 16
            self.backgroundColor = UIColor.systemRed
            let padding = CGFloat(16)
            self.contentEdgeInsets =  UIEdgeInsets(top: padding, left:padding, bottom: padding, right: padding)
        }
}


