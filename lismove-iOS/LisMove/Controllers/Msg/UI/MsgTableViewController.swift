//
//  MsgTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 01/10/21.
//

import UIKit


protocol MsgProtocol{
    func refreshMsgCard()
}

class MsgTableViewController: UITableViewController {

    let user = DBManager.sharedInstance.getCurrentUser()
    var msgList: [NotificationMsg] = []
    let refresh = UIRefreshControl()
    
    public var delegate:MsgProtocol?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        // Add Refresh Control to Table View
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refresh
        } else {
            tableView.addSubview(refresh)
        }
        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        
        self.tableView.register(MsgTableViewCell.self, forCellReuseIdentifier: "MsgCell")
        
        //advise user
        self.view.makeToast("Sincronizzazione in corso")
        
        //sync notification
        syncNotificationMessage()
        
        //add tableview long press gesture
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress(sender:)))
        tableView.addGestureRecognizer(longPress)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        delegate?.refreshMsgCard()
    }
    
    @objc private func handleLongPress(sender: UILongPressGestureRecognizer) {
        if sender.state == .began {
            let touchPoint = sender.location(in: tableView)
            if let indexPath = tableView.indexPathForRow(at: touchPoint) {
                
                let cell = tableView.cellForRow(at: indexPath) as! MsgTableViewCell
                
                let alert = UIAlertController(title: "Attenzione", message: "Aprire il link allegato?\n\(cell.detailView.url)", preferredStyle: .alert)
                if let popoverPresentationController = alert.popoverPresentationController {
                    popoverPresentationController.sourceView = cell
                    popoverPresentationController.sourceRect =  cell.bounds
                }
                let item1 = UIAlertAction(title: "Ok", style: .default, handler: { action in
                    switch action.style{
                        case .default:
                        
                        guard let url = URL(string: cell.detailView.url) else { return }
                            UIApplication.shared.open(url)
                        
                    case .cancel:
                        break
                    case .destructive:
                        break
                    @unknown default:
                        break
                    }
                })
                
                let item2 = UIAlertAction(title: "Annulla", style: .destructive, handler: nil)
                
                alert.addAction(item1)
                alert.addAction(item2)
                
                self.present(alert, animated: true, completion: nil)
            }
        }
    }
    
    @objc private func refreshData(_ sender: Any) {
        //fetch sessions
        syncNotificationMessage()
    }
    
    
    private func syncNotificationMessage(){
        //download all user session
        NetworkingManager.sharedInstance.getUserNotificationMessage(uid: self.user!.uid!, completion: {result in
            switch result {
                case .success(let data):
                    
                    self.msgList = data
                
                    //update notifications number
                    UIApplication.shared.applicationIconBadgeNumber = self.msgList.filter{!$0.read!}.count
                
                    self.calculateDynamicPadding()
                    
                    self.refreshControl?.endRefreshing()

                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                    }
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
            }
        })
    }
    
    private func calculateDynamicPadding(){
        //add padding to tableview to last cell ody heigth
        let body = msgList.last?.body ?? ""
        let insets = UIEdgeInsets(top: 0, left: 0, bottom: body.height(withConstrainedWidth: 150, font: UIFont.systemFont(ofSize: 12)), right: 0)
        tableView.contentInset = insets
    }
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return msgList.count
    }
    
//    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
//        return 80 + self.msgList[indexPath.row].title!.height(withConstrainedWidth: 150, font: UIFont.systemFont(ofSize: 12))
//    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "MsgCell") as! MsgTableViewCell
        
        cell.setUI(with: indexPath.row, msgList: self.msgList)
        cell.tintColor = .red
        
        cell.accessoryType =  !self.msgList[indexPath.row].read! ? .detailDisclosureButton : .none
        
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        //update cell
        UIView.animate(withDuration: 0.3) {
            self.tableView.performBatchUpdates(nil)
        }
        
        //set read message
        //download all user session
        NetworkingManager.sharedInstance.markNotificationAsRead(uid: self.user!.uid!, mid: self.msgList[indexPath.row].message!, completion: {result in
            switch result {
                case true:
                    
                //decrement notification number
                UIApplication.shared.applicationIconBadgeNumber-=1
                
                //disable accesory
                let cell = self.tableView.cellForRow(at: indexPath as IndexPath) as? MsgTableViewCell
                cell?.accessoryType = .none
                    
                case false:
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": "Impossibile impostare la notifica come letta"])
            }
        })
    }

    override func tableView(_ tableView: UITableView, didDeselectRowAt indexPath: IndexPath) {
        if let cell = self.tableView.cellForRow(at: indexPath) as? MsgTableViewCell {
            cell.hideDetailView()
        }
    }
    
}
