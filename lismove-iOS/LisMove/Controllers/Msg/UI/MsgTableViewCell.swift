//
//  MsgTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/10/21.
//

import UIKit

class MsgTableViewCell: UITableViewCell {
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private let containerView = UIStackView()
    private let cellView = CustomTableCellView()
    public let detailView = CustomTableDetailView()
    
    func setUI(with index: Int, msgList: [NotificationMsg]) {

        cellView.setUI(with: msgList[index].title ?? "", image: msgList[index].imageURL ?? "", createDate: msgList[index].createdDate ?? Date().millisecondsSince1970)
        
        detailView.setUI(with: msgList[index].body ?? "")

    }
    
    func commonInit() {
        selectionStyle = .none
        detailView.isHidden = true
        
        containerView.axis = .vertical

        contentView.addSubview(containerView)
        containerView.addArrangedSubview(cellView)
        containerView.addArrangedSubview(detailView)
        
        containerView.translatesAutoresizingMaskIntoConstraints = false
        cellView.translatesAutoresizingMaskIntoConstraints = false
        detailView.translatesAutoresizingMaskIntoConstraints = false
        
        containerView.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor).isActive = true
        containerView.trailingAnchor.constraint(equalTo: self.contentView.trailingAnchor, constant: -36).isActive = true
        containerView.topAnchor.constraint(equalTo: self.contentView.topAnchor).isActive = true
        containerView.bottomAnchor.constraint(equalTo: self.contentView.bottomAnchor).isActive = true
        

    }

}

extension MsgTableViewCell {
    
    var isDetailViewHidden: Bool{
        return detailView.isHidden
    }
    
    func showDetailView() {
        detailView.isHidden = false
    }
    
    func hideDetailView() {
        detailView.isHidden = true
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        if isDetailViewHidden, selected{
            showDetailView()
        } else {
            hideDetailView()
            
        }
    }
    
}
