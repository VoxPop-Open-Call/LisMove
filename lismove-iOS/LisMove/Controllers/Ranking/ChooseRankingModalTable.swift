//
//  ChooseRankingModalTable.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 20/07/21.
//

import UIKit
import Kingfisher
import Resolver

class ChooseRankingModalTable: UITableViewController {

    var rankingList: [Ranking]?
    var organizationList: [Organization] = []
    
    var selectedRanking: Ranking?
    var selectedOrganization: Organization?
    
    let user = DBManager.sharedInstance.getCurrentUser()
    public var achivementSelect = false
    
    @Injected var initiativeRepository: InitiativeRepository
    
    var onDoneBlock : ((Ranking?, Organization?) -> Void)?
    

    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        //download all ranking
        syncRanking()
    }

    private func syncRanking(){
        if(achivementSelect){
            getAchievementElements()
        }else{
            getRankingElements()
        }
    }
    
    func getRankingElements(){
        NetworkingManager.sharedInstance.getUserRanking(uid: user!.uid!, completion: { result in
            switch result {
                case .success(let data):
                    
                self.rankingList = data.sorted(by: {$0.startDate ?? 0 > $1.endDate ?? 0})
                    
                    for item in self.rankingList!{
                        
                        guard let id = item.organization else{
                            continue
                        }
                        
                        self.initiativeRepository.getOrganization(oid: id){ result in
                            switch result {
                                case .success(let data):
                                    
                                    self.organizationList.append(data)
                                    
                                    DispatchQueue.main.async {
                                        self.tableView.reloadData()
                                    }

                                    
                                case .failure(let error):
                                    //MARK: ERROR STREAM
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    
                            }
                        }
                    }
                    self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Community", value: nil), at: 0)
                    //add global ranking
                
                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                    }
                    
                case .failure(let error):
                
                    self.rankingList = []
                    //add global ranking
                    if(self.achivementSelect){
                        self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Tutte", value: nil), at: 0)
                        self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Nazionali", value: nil), at: 1)
                    }else{
                        self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Community", value: nil), at: 0)
                    }
                
                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                    }
                
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
    }
    
    func getAchievementElements(){
        NetworkingManager.sharedInstance.getUserArchievement(uid: user!.uid!, completion: { result in
            switch result {
                case .success(let data):
            
                
                self.rankingList = [Ranking]()
             
                
                    
                    for item in data{
                        
                        guard let id = item.organization else{
                            continue
                        }
                        
                        self.initiativeRepository.getOrganization(oid: id)  { result in
                            switch result {
                                case .success(let data):
                                    
                                    self.organizationList.append(data)
                                    let ranking = Ranking(endDate: nil, filter: nil, filterValue: nil, id: data.id, organization: data.id, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: data.title, value: nil)
                                    self.rankingList?.append(ranking)
                                
                                    DispatchQueue.main.async {
                                        self.tableView.reloadData()
                                    }

                                    
                                case .failure(let error):
                                    //MARK: ERROR STREAM
                                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                    
                            }
                        }
                        
                        
                    }
                    
                    self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Tutte", value: nil), at: 0)
                    self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Community", value: nil), at: 1)
                
                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                    }
                    
                case .failure(let error):
                
                    self.rankingList = []
                    //add global ranking
                    self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Tutte", value: nil), at: 0)
                    self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Community", value: nil), at: 1)
                   
                
                    DispatchQueue.main.async {
                        self.tableView.reloadData()
                    }
                
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
        /*
        self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Tutte", value: nil), at: 0)
        self.rankingList?.insert(Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Nazionali", value: nil), at: 1)
         */
    }
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return self.rankingList?.count ?? 0
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "rankingCell", for: indexPath) as! ChoseRankingTableViewCell
        let data = self.rankingList?[indexPath.row]
        if(achivementSelect){
            cell.rankingTitle.text = data!.title
            cell.organizationLogo?.image = UIImage(named: "floatingButton")
            cell.rankingSubtitle.isHidden = true
            cell.rankingDate.isHidden = true
            let organization = self.organizationList.filter{$0.id == data!.organization}
            if let logo = organization.first?.initiativeLogo{
                let url = URL(string: logo)
                cell.organizationLogo?.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
                cell.organizationLogo?.round()
            }
        }else{
            if(data!.title == "Globale" || data!.title == "Tutte" || data!.title == "Community"){
                cell.rankingTitle.text = data!.title
                cell.organizationLogo?.image = UIImage(named: "floatingButton")
                cell.rankingSubtitle.isHidden = true
                cell.rankingDate.isHidden = !data!.hasValidDate()
                cell.rankingDate.text = data!.getDateIntervalLabel()
                
            }else{
                if self.rankingList != nil && !self.organizationList.isEmpty{
                    cell.rankingSubtitle?.text = data!.title
                    cell.rankingSubtitle.isHidden = data?.title == nil
            
                    if(data?.organization != nil){
                        let organization = self.organizationList.filter{$0.id == data!.organization}
                        let url = URL(string: organization.first?.notificationLogo ?? "")
                        cell.organizationLogo?.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
                        cell.organizationLogo?.round()
                        
                        cell.rankingTitle.text = organization.first?.title

                        cell.rankingDate.isHidden = !data!.hasValidDate()
                        cell.rankingDate.text = data!.getDateIntervalLabel()

                    }
                }
            }
        }
        

        return cell
    }
    

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let rankingPositions = self.rankingList else {
            return
        }
        
        
        self.selectedRanking = self.rankingList?[indexPath.row]
        
        if(self.selectedRanking != nil){
            self.selectedOrganization = self.organizationList.filter{$0.id == self.selectedRanking!.organization}.first
        }
        
        dismiss(animated: true, completion: nil)
    }
    /*
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 86
    }*/
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isBeingDismissed {
        
            self.onDoneBlock!(self.selectedRanking, self.selectedOrganization)
        }
    }

}
