//
//  LogWallTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 05/09/21.
//

import UIKit
import SkeletonView

class LogWallTableViewController: UITableViewController {

    var logList: [String]?
    let refresh = UIRefreshControl()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .dark
        }
        
        // Add Refresh Control to Table View
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refresh
        } else {
            tableView.addSubview(refresh)
        }
        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        
        //fix last element padding
        self.tableView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: 56, right: 0)
        
        downloadMessages()
        
    }
    
    
    
    @objc private func refreshData(_ sender: Any) {
        //fetch sessions
        downloadMessages()
    }
    
    
    private func downloadMessages(){
        //download all user session
        NetworkingManager.sharedInstance.getLogWall(completion: {result in
            switch result {
                case .success(let data):
                    
                self.logList = data.reversed()
                    
                    self.refreshControl?.endRefreshing()

                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                        if let index = self.logList?.count{
                            let indexPath = IndexPath(item: index - 1, section: 0)
                            self.tableView.scrollToRow(at: indexPath, at: UITableView.ScrollPosition.bottom, animated: false)
                        }
                        

                    }
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
            }
        })
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return logList?.count ?? 10
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "logCell", for: indexPath)
        
        if(self.logList != nil){
            cell.textLabel?.hideSkeleton(reloadDataAfter: true, transition: .none)
            let htmlText = self.logList![indexPath.row]
            let htmlTextAdjusted = "<span style=\"font-size: 16.0\">\(htmlText)</span>"
            cell.textLabel?.attributedText = htmlTextAdjusted.htmlToAttributedString
        
        }else{
            cell.textLabel?.showAnimatedGradientSkeleton()
        }

     

        return cell
    }

    
    
}
extension String {

    var htmlToAttributedString: NSAttributedString? {
        guard let data = data(using: .utf8) else { return nil }
        do {
            return try NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding:String.Encoding.utf8.rawValue], documentAttributes: nil)
        } catch {
            return nil
        }
    }
    
    
    var htmlToString: String {
        return htmlToAttributedString?.string ?? ""
    }
    
}
