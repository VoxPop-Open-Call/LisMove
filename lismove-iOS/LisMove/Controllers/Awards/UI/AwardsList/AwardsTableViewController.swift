//
//  RankingTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import UIKit
import SkeletonView
import Kingfisher

class AwardsTableViewController: UITableViewController {

    var user = DBManager.sharedInstance.getCurrentUser()
    var awards: [Any]?
    var selectedRanking: Ranking?
    var selectedOrganization: Organization?
    var selectedAchievement: Archievement?
    
    var achivementSelect = false
    
    let refresh = UIRefreshControl()
    

    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initView()
        
        if(achivementSelect){
            
            //sync global ranking
            syncAwardsByAchievement(aid: self.selectedAchievement?.id)
            
            if(self.selectedAchievement != nil){
                self.initNavigationItemTitleView(title: self.selectedAchievement?.name, subtitle: "")
            }
            
        }else{
            //sync global ranking
            syncAwards(rankingId: self.selectedRanking?.id)
            
            if(self.selectedRanking != nil){
                self.initNavigationItemTitleView(title: self.selectedRanking?.title, subtitle: (selectedOrganization != nil) ? selectedOrganization?.title : "")
            }
        }
        

        
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
    
    override func didMove(toParent parent: UIViewController?) {
        super.didMove(toParent: parent)

        if parent != nil && self.navigationItem.titleView == nil {
            initNavigationItemTitleView()
        }
    }

    private func initNavigationItemTitleView(title: String? = "Community", subtitle: String? = "") {
        self.navigationItem.titleView = self.setTitle(title: title!, subtitle: subtitle!)
    
    }
    
    
    
    @objc private func refreshData(_ sender: Any) {
        
        if(self.selectedRanking == nil){
            //fetch awards
            self.syncAwards(rankingId: nil)
        }else{
            self.syncAwards(rankingId: self.selectedRanking?.id)
        }
    }
    


    private func syncAwards(rankingId: Int?){
        
        if(rankingId != nil){
            //downlaod global ranking
            NetworkingManager.sharedInstance.getAwardRanking(rid: rankingId!, completion: { result in
                
                switch result {
                    case .success(let data):
                        
                        self.awards = data
                        
                        
                        self.refresh.endRefreshing()
                        
                        //persist animation
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            self.tableView.reloadData()
                        }
                    
                        
                        break
                        
                    case .failure(let error):
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                        break
                }
                
            })
            
            
        }else{
            self.awards = []
        }
        
    }
    
    private func syncAwardsByAchievement(aid: Int?){
        
        if(aid != nil){

            NetworkingManager.sharedInstance.getAchievementAwards(aid: aid! , completion: { result in
                
                switch result {
                    case .success(let data):
                        
                        self.awards = data
                        
                        
                        self.refresh.endRefreshing()
                        
                        //persist animation
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            self.tableView.reloadData()
                        }
                    
                        
                        break
                        
                    case .failure(let error):
                        //MARK: ERROR STREAM
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                        break
                }
                
            })
            
            
        }else{
            self.awards = []
        }
        
    }
    
    func setTitle(title:String, subtitle:String) -> UIView {
        let titleLabel = UILabel(frame: CGRect(x: 0, y: -2, width: 0, height: 0))

        titleLabel.backgroundColor = UIColor.clear
        titleLabel.textColor = UIColor.white
        titleLabel.font = UIFont.boldSystemFont(ofSize: 17)
        titleLabel.text = title
        titleLabel.sizeToFit()

        let subtitleLabel = UILabel(frame: CGRect(x: 0, y: 18, width: 0, height: 0))
        subtitleLabel.backgroundColor = UIColor.clear
        subtitleLabel.textColor = UIColor.white
        subtitleLabel.font = UIFont.systemFont(ofSize: 12)
        subtitleLabel.text = subtitle
        subtitleLabel.sizeToFit()

        let titleView = UIView(frame: CGRect(x: 0, y: 0, width: max(titleLabel.frame.size.width, subtitleLabel.frame.size.width), height: 30))
        titleView.addSubview(titleLabel)
        titleView.addSubview(subtitleLabel)

        let widthDiff = subtitleLabel.frame.size.width - titleLabel.frame.size.width

        if widthDiff < 0 {
            let newX = widthDiff / 2
            subtitleLabel.frame.origin.x = abs(newX)
        } else {
            let newX = widthDiff / 2
            titleLabel.frame.origin.x = newX
        }

        return titleView
    }

    
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return self.awards?.count ?? 10
    }
    
    
    private func renderAwardAchievementCell(_ cell: AwardTableViewCell, _ indexPath: IndexPath) -> AwardTableViewCell{
        
        let data = self.awards?[indexPath.row] as! AwardAchievement
        
        //avatar
        let url = URL(string: data.imageURL ?? "")
        cell.awardImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
        
        /*
        cell.positionTitleLabel.isHidden = true
        cell.positionNumberLabel.isHidden = true
        cell.awardImageLeftMargin.constant = 16
        */
        
        //name
        cell.name.text = data.name
        cell.value.text = data.value != nil ?  "\(data.value!)" : ""
        cell.valueLabel.text = data.value != nil ? data.getTypeLabel() : ""
        
        return cell

    }
    
    private func renderAwardRankingCell(_ cell: AwardTableViewCell, _ indexPath: IndexPath) -> AwardTableViewCell{
        
        let data = self.awards?[indexPath.row] as! AwardRanking
        
        //avatar
        let url = URL(string: data.imageURL ?? "")
        cell.awardImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
        
        cell.name.text = data.name
        cell.value.text = data.value != nil ?  "\(data.value!)" : ""
        cell.valueLabel.text = data.value != nil ? data.getTypeLabel() : ""
        
        //name
        cell.name.text = data.name
        
        
        return cell

    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "rankingCell", for: indexPath) as! AwardTableViewCell

        if(self.awards != nil){
            
            if(self.achivementSelect){
                cell = renderAwardAchievementCell(cell, indexPath)
            }else{
                cell = renderAwardRankingCell(cell, indexPath)
            }
            
            cell.hideAnimation()
        }

        
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let awards = self.awards else { return }
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "awardNavigationController") as! UINavigationController
        
        let detailController = navController.topViewController as! AwardInfoViewController
        if(self.achivementSelect){
            detailController.achievementAward = awards[indexPath.row] as? AwardAchievement
        }else{
            detailController.rankingAward = awards[indexPath.row] as? AwardRanking
        }
        present(navController, animated: true, completion: nil)
        tableView.deselectRow(at: indexPath, animated: true)
    }
    

    /*
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 82
    }
     */


}
