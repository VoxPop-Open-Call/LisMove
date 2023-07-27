//
//  projectCollectionViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import MaterialComponents.MaterialCards


class ProjectCollectionViewCell: MDCCardCollectionCell {
    weak var viewController: UIViewController?

    @IBOutlet weak var stackView: UIStackView!
    var data = [ActiveProjectItemUI]()
    
    
    var selectedTappedAction: (() -> Void)? = nil
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [stackView].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }

    
    func hideAnimation(){
        [stackView].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }

    
    func setupCell(data: [ActiveProjectItemUI]){
        self.data = data
        let width = stackView.frame.width
        stackView.distribution = .fillEqually
        stackView.spacing = 36
        stackView.axis = .vertical
        stackView.subviews.forEach{$0.removeFromSuperview()}
        print("initiatives are \(data.count)")
        var index = 0
        data.forEach({ item in
            
            let imageView = UIImageView()
            imageView.kf.setImage(with: URL(string: item.image), placeholder: UIImage(named: "floatingButton"))
            //imageView.widthAnchor.constraint(equalToConstant: (width / 2)).isActive = true
            //imageView.heightAnchor.constraint(equalToConstant: 100).isActive = true
            imageView.contentMode = .scaleAspectFit
            imageView.clipsToBounds = true
            stackView.addArrangedSubview(imageView)
            
            setupGesture(imageView: imageView, index: index)
            index += 1
        })
    }
    
    func setupGesture(imageView: UIImageView, index: Int) {
        let gesture = UITapGestureRecognizer(target: self, action: #selector(self.openAlertDialog(_:)))
        imageView.isUserInteractionEnabled = true
        imageView.addGestureRecognizer(gesture)
        imageView.tag = index
        
    }
    
    @objc func openAlertDialog(_ gesture:UITapGestureRecognizer){
        
        let v = gesture.view!
        let index = v.tag
        
        let item = self.data[index]
        
       //BASIC INFORMATIONS
        let title = item.organizationName
        let message = item.regulation
        
        //ATTRIBUTED STRINGS
        let linkFixedMessage = "Per consultare i periodi di validità di ogni singola iniziativa, navigare nell'app, menù "
        let linkText = " 'Altro -> Gestione Iniziative'  \n\n"
    
        let attributedString = NSMutableAttributedString()

        //attributes
        let attributes = [NSAttributedString.Key.underlineStyle: NSUnderlineStyle.thick.rawValue, NSAttributedString.Key.foregroundColor: UIColor.systemRed] as [NSAttributedString.Key : Any]

        //custom string
        let attributedQuote = NSAttributedString(string: linkText, attributes: attributes)
        
        attributedString.append(NSAttributedString(string: linkFixedMessage))
        attributedString.append(attributedQuote)
        attributedString.append(NSAttributedString(string: message))
    
        
        //ALERT VIEW
        let alert = UIAlertController(title: title, message: "", preferredStyle: .alert)
        
        
        alert.setValue(attributedString, forKey: "attributedMessage")
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = viewController!.view
            popoverPresentationController.sourceRect =  CGRect(x: viewController!.view.bounds.size.width / 2.0, y: viewController!.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        alert.addAction(UIAlertAction(title:item.regulationLink != nil ? "Visita sito" : "Ok", style: .default, handler: { action in
            if let link = item.regulationLink{
                UIApplication.shared.open(link)
            }
        

        }))
        if(item.regulationLink != nil){
            alert.addAction(UIAlertAction(title: "Annulla", style: .cancel, handler: nil))
        }
        viewController?.present(alert, animated: true, completion: nil)
    
        
    }

}
