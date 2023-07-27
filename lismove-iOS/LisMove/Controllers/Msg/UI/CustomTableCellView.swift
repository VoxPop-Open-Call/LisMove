//
//  CustomTableCellView.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/10/21.
//

import Foundation
import UIKit
import Kingfisher
import Charts

final class CustomTableCellView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private let title = UILabel()
    private let timestamp = UILabel()
    private let imageView = UIImageView()
    
    func setUI(with string: String, image: String, createDate: Int64) {
        title.text = string
        title.numberOfLines = 2
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.current
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        let dateString = formatter.string(from: Date(milliseconds: createDate))
        
        timestamp.text = dateString
        timestamp.numberOfLines = 0
        
        let url = URL(string: image)
        imageView.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
    }
    
    func commonInit() {
        addSubview(timestamp)
        addSubview(title)
        addSubview(imageView)
        
        timestamp.translatesAutoresizingMaskIntoConstraints = false
        title.translatesAutoresizingMaskIntoConstraints = false
        
        imageView.widthAnchor.constraint(equalToConstant: 56).isActive = true
        imageView.heightAnchor.constraint(equalToConstant: 56).isActive = true
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.topAnchor.constraint(equalTo: self.topAnchor, constant: 10).isActive = true
        imageView.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -10).isActive = true
        imageView.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 30).isActive = true
        imageView.trailingAnchor.constraint(equalTo: title.leadingAnchor, constant: -10).isActive = true
        
        timestamp.topAnchor.constraint(equalTo: self.topAnchor, constant: 10).isActive = true
        timestamp.leadingAnchor.constraint(equalTo: self.imageView.trailingAnchor, constant: 10).isActive = true
        timestamp.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: 10).isActive = true
        //timestamp.centerYAnchor.constraint(equalTo: self.centerYAnchor).isActive = true
        timestamp.textColor = .gray
        title.font = UIFont.boldSystemFont(ofSize: 6.0)
        
        title.topAnchor.constraint(equalTo: self.timestamp.bottomAnchor, constant: 4).isActive = true
        title.leadingAnchor.constraint(equalTo: self.imageView.trailingAnchor, constant: 10).isActive = true
        title.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: 10).isActive = true
        
        //title.centerYAnchor.constraint(equalTo: self.centerYAnchor).isActive = true
        title.textColor = .black
        title.font = UIFont.boldSystemFont(ofSize: 16.0)


    }
}

final class CustomTableDetailView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private var title = UILabel()
    public var url = ""

    func setUI(with string: String) {
        title.text = string
        title.numberOfLines = 0
        
        //check string lenght
        if(title.text!.count < 100){
            title.centerYAnchor.constraint(equalTo: self.centerYAnchor).isActive = true
        }
        
        //find url
        findURL(string: string)

    }
    
    
    func findURL(string: String){
        let detector = try! NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
        let matches = detector.matches(in: string, options: [], range: NSRange(location: 0, length: string.utf16.count))

        let match = matches.first
        
        if(match != nil){
            let range = match!.range
            let url = string.substring(with: range)
            
            changeURLColor(string: string, range: range)
            
            self.url = String(url!)
        }
    

    }
    
    func changeURLColor(string: String, range: NSRange){
        
        let mutableAttributedString = NSMutableAttributedString.init(string: string)
        mutableAttributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.systemBlue, range: range)
        
        title.attributedText = mutableAttributedString
    }
    
    
    
    func commonInit() {
        addSubview(title)

        title.translatesAutoresizingMaskIntoConstraints = false
        
        title.topAnchor.constraint(equalTo: self.topAnchor, constant: 10).isActive = true
        title.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: -10).isActive = true
        title.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 96).isActive = true
        title.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: 24).isActive = true
        

        
    }
}
