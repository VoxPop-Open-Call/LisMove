//
//  HomePointItemView.swift
//  LisMove
//
//

import UIKit
class HomePointItemView: UIView {
    static let CELL_HEIGHT = 62
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var organizationName: UILabel!
    @IBOutlet weak var pointLabel: UILabel!
    var data: HomePointItemData? = nil
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
   

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    func setupItemView(data: HomePointItemData){
        self.data = data
        commonInit()
    }
    
    
    
    func commonInit() {
        let bundle = Bundle.init(for: HomePointItemView.self)
        if let viewsToAdd = bundle.loadNibNamed("HomePointItemView", owner: self, options: nil), let contentView = viewsToAdd.first as? UIView {
            addSubview(contentView)
            contentView.frame = self.bounds
            contentView.autoresizingMask = [.flexibleHeight,.flexibleWidth]
            if let data = data{
                imageView.kf.setImage(with: URL(string: data.image ?? ""), placeholder: UIImage(named: "floatingButton"))
                imageView.round()
                organizationName.text = data.organizationName
                pointLabel.text = "\(data.points)"
            }
             

        }

        
    }
}
