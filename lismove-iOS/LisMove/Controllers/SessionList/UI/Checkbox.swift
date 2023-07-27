//
//  Checkbox.swift
//  LisMove
//
//

import Foundation
import UIKit

class LisMoveCheckBox: UIButton{
    @IBInspectable var isChecked: Bool = false{
        didSet{
            let image = isChecked ? "checkmark.square" : "squareshape"
            let color = isChecked ? UIColor.systemRed  : UIColor.black
            setImage(UIImage.init(systemName: image), for: .normal)
            tintColor = color
        }
    }
    

    override func awakeFromNib() {
        super.awakeFromNib()
        setTitle("", for: .normal)
        addTarget(self, action: #selector(buttonClicked), for: UIControl.Event.touchUpInside)
        

    }
    override init(frame: CGRect){
        super.init(frame: frame)
        setTitle("", for: .normal)
        addTarget(self, action: #selector(buttonClicked), for: UIControl.Event.touchUpInside)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setTitle("", for: .normal)
        addTarget(self, action: #selector(buttonClicked), for: UIControl.Event.touchUpInside)
    }
    
    @objc func buttonClicked(){
        isChecked = !isChecked
    }
}




class PBCheckbox: UIView{
    
    @IBInspectable var isChecked: Bool {
        set{
            checbox.isChecked = newValue
        }
        get{
            checbox.isChecked
        }
    }
    
    @IBInspectable var text: String? {
        didSet {
            label.text = text
        }
    }
    @IBOutlet weak var stackview: UIStackView!
    @IBOutlet weak var checbox: LisMoveCheckBox!
    @IBOutlet weak var label: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        initView()

    }
    override init(frame: CGRect){
        super.init(frame: frame)
        initView()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        initView()
    }
    
    func initView(){
        let bundle = Bundle.init(for: PBCheckbox.self)
        if let viewsToAdd = bundle.loadNibNamed("PBCheckbox", owner: self, options: nil), let contentView = viewsToAdd.first as? UIView {
            addSubview(contentView)
            contentView.frame = self.bounds
            contentView.autoresizingMask = [.flexibleHeight,
                                            .flexibleWidth]
           
        }
    }
    
    @objc func buttonClicked(){
        isChecked = !isChecked
    }
}
