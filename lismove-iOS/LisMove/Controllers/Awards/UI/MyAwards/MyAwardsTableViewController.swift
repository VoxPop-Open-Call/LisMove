//
//  MyAwardsTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import UIKit

class MyAwardsTableViewController: UITableViewController {

    var myAwardList: [Award]? = nil
    var rankingList: [Ranking] = []
    
    var selectedAward: Award?
    
    var user = DBManager.sharedInstance.getCurrentUser()
    
    let refresh = UIRefreshControl()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        

        initView()
        syncMyAward()

    }
    

    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
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
    }
    
    @objc private func refreshData(_ sender: Any) {
        syncMyAward()
    }
    
    private func syncMyAward(){
        
        //downlaod global ranking
        
        NetworkingManager.sharedInstance.getUserAwards(uid: user!.uid!, completion: { result in
            
            switch result {
                case .success(let data):
                    
                    self.myAwardList = self.customAwardShuffle(data: data)
                
                    self.rankingList.removeAll()
                
                    data.forEach{item in
                        if(item.rankingId != nil){
                            self.syncSpecificRanking(rankingId: item.rankingId!)
                        }
                    }
                
                    self.refresh.endRefreshing()
                    
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        //persist animation
                        DispatchQueue.main.async {
                            self.tableView.reloadData()
                        }
                    }

                
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
            
        })
        
    }
    
    private func syncSpecificRanking(rankingId: Int){
        
        //downlaod global ranking
        NetworkingManager.sharedInstance.getRanking(id: rankingId, completion: { result in
            
            switch result {
                case .success(let data):
                    
                self.rankingList.append(data)
   
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
        return self.myAwardList?.count ?? 10
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "awardCell", for: indexPath) as! MyAwardTableViewCell
        
        if(self.myAwardList != nil){
            
            let award = self.myAwardList![indexPath.row]
            let data = award.asAwardItemUI()
            
            let url = URL(string: data.image ?? "")
            cell.myAwardImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            
            cell.awardCategory.text = self.rankingList.first(where: {$0.id == award.rankingId})?.title ?? ""
            cell.awardName.text = data.name
            cell.awardQuantity.text = data.value
            cell.awardType.text = data.valueType
            

            if let rightIcon = data.rightIcon{
                cell.couponCheckImage.isHidden = false
                cell.couponCheckImage.image = UIImage(named: rightIcon)!
            }else{
                cell.couponCheckImage.isHidden = true
            }

            cell.couponCheckImage.tintColor = data.rightElementsColor
            cell.couponCheckLabel.text = data.rightText
            cell.couponCheckLabel.textColor = data.rightElementsColor
            
            cell.hideAnimation()
        }
        

        return cell
        
    }
    
    
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let myAwardList = self.myAwardList else {
            tableView.deselectRow(at: indexPath, animated: true)
            return
        }

        if(indexPath.row > myAwardList.count){
            tableView.deselectRow(at: indexPath, animated: true)
            return
        }
        
        self.selectedAward = myAwardList[indexPath.row]
        
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "awardNavigationController") as! UINavigationController
        
        let detailController = navController.topViewController as! AwardInfoViewController
        detailController.selectedAward = self.selectedAward
        //detailController.rankingList = self.rankingList
        
        present(navController, animated: true, completion: nil)
        tableView.deselectRow(at: indexPath, animated: true)
        
    }

}


extension MyAwardsTableViewController {
    
    private func customAwardShuffle(data: [Award]) -> [Award]{
        
        let orderByReedem = data.sorted(by: {$0.refundOrderValue > $1.refundOrderValue})
        
        let orderByDate = orderByReedem.sorted(by: {$0.startDate ?? 0 > $1.startDate ?? 0})
        
        
        return orderByDate
    }
}
