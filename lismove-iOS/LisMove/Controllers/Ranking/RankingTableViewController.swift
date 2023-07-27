//
//  RankingTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 14/07/21.
//

import UIKit
import SkeletonView
import Kingfisher
import SwiftUI
import Resolver

class RankingTableViewController: UIViewController, UITableViewDelegate, UITableViewDataSource  {

    @IBOutlet weak var tableView: UITableView!
    var user = DBManager.sharedInstance.getCurrentUser()
    var ranking: Ranking?
    
    var selectedRanking: Ranking?
    var selectedOrganization: Organization?
    
    @Injected var initiativeRepository: InitiativeRepository
    
    @IBOutlet weak var daysButton: UILabel!
    let refresh = UIRefreshControl()

    @IBOutlet weak var infoButton: UIButton!
    @IBOutlet weak var userPositionHeight: NSLayoutConstraint!
    @IBOutlet weak var rankingAwardLayout: UIStackView!
    @IBOutlet weak var rankingPickerArea: UIView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    
    @IBOutlet weak var userPositionLayout: UIView!
    @IBOutlet weak var userPosition: UILabel!
    @IBOutlet weak var userPositionLabel: UILabel!
    @IBOutlet weak var userAvatar: UIImageView!
    @IBOutlet weak var userNickname: UILabel!
    @IBOutlet weak var userPoints: UILabel!
    @IBOutlet weak var userPointsLabel: UILabel!
    @IBOutlet weak var emtyLabel: UILabel!
    @IBOutlet weak var userRankingDivider: UIView!
    
    var userRankingPosition: RankingPosition? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initView()
        
        //sync global ranking
        fetchInitialRanking()
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
        
        rankingAwardLayout.isHidden = true
        infoButton.isHidden = true

        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        daysButton.numberOfLines = 2
        daysButton.textAlignment = .center
        
        fixAppBar()
        tableView.dataSource = self
        tableView.delegate = self
        //tableView.refreshControl = nil
        userAvatar.round()
        startUserRankingLoadingAnimation()
        
        let goToAwardsGesture = UITapGestureRecognizer(target: self, action: #selector(goToAwards(_:)))

        rankingAwardLayout.addGestureRecognizer(goToAwardsGesture)

    }
    func startUserRankingLoadingAnimation(){
        [userAvatar,userPosition,userPositionLabel,userNickname,userPoints,userPointsLabel].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }
    func endUserRankingLoadingAnimation(){
        [userAvatar,userPosition,userPositionLabel,userNickname,userPoints,userPointsLabel].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    func fixAppBar(){
        //fix ios 15 appbar
        if #available(iOS 15, *) {
            let navigationBar = navigationController?.navigationBar
            let navigationBarAppearance = UINavigationBarAppearance()
            navigationBarAppearance.shadowColor = nil
            navigationBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
            navigationBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
            navigationBarAppearance.backgroundColor = UIColor.systemRed
            navigationBar?.scrollEdgeAppearance = navigationBarAppearance
        }else{
            let navigationBar = navigationController?.navigationBar
            let navigationBarAppearance = UINavigationBarAppearance()
            navigationBarAppearance.shadowColor = .clear
            navigationBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
            navigationBarAppearance.backgroundColor = UIColor.systemRed
            navigationBar?.standardAppearance = navigationBarAppearance
        }
    }
    
    
    override func didMove(toParent parent: UIViewController?) {
        super.didMove(toParent: parent)

        if parent != nil && self.navigationItem.titleView == nil {
            initNavigationItemTitleView()
        }
    }

    private func initNavigationItemTitleView(title: String? = "Community", subtitle: String? = "") {
      
        self.setTitle(title: title!, subtitle: subtitle!)
        
        daysButton.text = selectedRanking?.getDaysTillEndString()
        daysButton.isHidden = !(selectedRanking?.hasDaysTillEnding() ?? false) 
        let recognizer = UITapGestureRecognizer(target: self, action: #selector(self.titleWasTapped))
        rankingPickerArea.isUserInteractionEnabled = true
        rankingPickerArea.addGestureRecognizer(recognizer)
    }

    //switch ranking
    @objc private func titleWasTapped() {
        //open modal controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let modalController = storyBoard.instantiateViewController(withIdentifier: "modalController") as! ChooseRankingModalTable
        modalController.modalPresentationStyle = .popover
        if let popoverPresentationController = modalController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        self.present(modalController, animated: true, completion: nil)
        
        modalController.onDoneBlock = { rank,orga in
            
            guard let selectedRank = rank else{
                return
            }
            
            self.selectedRanking = selectedRank
            
            self.updateRanking(orga: orga, selectedRank: selectedRank)
    

        }
    }
    
    func updateRanking(orga: Organization?, selectedRank: Ranking){
        //set button title
        self.selectedRanking = selectedRank
        self.selectedOrganization = orga
        let title = (orga != nil) ? orga?.title : ""
        self.initNavigationItemTitleView(title: title, subtitle: selectedRank.title)
        
        //clear ranking list
        self.ranking = nil
        
        
        self.view.makeToast("Aggiornamento in corso")
        
        if(selectedRank.title == "Community"){
            
            //download global
            self.selectedRanking = nil
            
            rankingAwardLayout.isHidden = true
            infoButton.isHidden = true
            self.syncRankingGlobal()
            
        }else{
            
            rankingAwardLayout.isHidden = false
            infoButton.isHidden = false

            //download new ranking
            self.syncSpecificRanking(rankingId: selectedRank.id!)
        }
    }
    
    func drawSuccessOrEmptyVisibility(selectedRank: Ranking){
        let successView = selectedRank.rankingPositions?.isEmpty == false
        emtyLabel.isHidden = successView
        tableView.isHidden = !successView
        userPositionLayout.isHidden = !successView
        userRankingDivider.isHidden = !successView
        
    }
    
    
    
    @objc private func refreshData(_ sender: Any) {
        
        if(self.selectedRanking == nil){
            //fetch ranking
            syncRankingGlobal()
        }else{
            syncSpecificRanking(rankingId: self.selectedRanking!.id!)
        }
    }
    
    
    private func fetchInitialRanking(){
        NetworkingManager.sharedInstance.getUserRanking(uid: user!.uid!, completion: { result in
            switch result {
                case .success(let data):
                
                let rankingList = data.sorted(by: {$0.startDate ?? 0 > $1.startDate ?? 0})
                
                    if(rankingList.isEmpty){
                        
                        self.updateRanking(orga: nil, selectedRank: Ranking(endDate: nil, filter: nil, filterValue: nil, id: nil, organization: nil, rankingPositions: nil, repeatNum: nil, repeatType: nil, startDate: nil, title: "Community", value: nil))
                        
                    }else if let ranking = rankingList.first, let organizationId = ranking.organization{
                        
                        self.initiativeRepository.getOrganization(oid: organizationId) { result in
                            switch result{
                            case .success(let data):
                                self.updateRanking(orga: data, selectedRank: ranking)

                            case .failure(let error):
                                NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                                break
                            }
                        }
                        
                    }else{
                        
                        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error":"Problema nel recuperare la classifica"])
                    }
                    
                    
                case .failure(let error):
                
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
    }

    
    private func syncRankingGlobal(){
        
        //downlaod global ranking
        NetworkingManager.sharedInstance.getGlobalRanking(completion: { result in
            
            switch result {
                case .success(let data):
                    
                    self.ranking = data
                    
                    //show my position
                    let mydata = self.ranking?.rankingPositions?.filter({$0.username == self.user?.username}).first
                    self.userRankingPosition = mydata

                    self.refresh.endRefreshing()
                    
                    //persist animation
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        self.reloadUI()
                    }
                
                    
                    break
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
            
        })
    }
    

    private func syncSpecificRanking(rankingId: Int){
        
        //downlaod global ranking
        NetworkingManager.sharedInstance.getRanking(id: rankingId, completion: { result in
            
            switch result {
                case .success(let data):
                    
                    self.ranking = data
                    //show my position
                    let mydata = self.ranking?.rankingPositions?.filter({$0.username == self.user?.username}).first
                    self.userRankingPosition = mydata
                    
                    
                    self.refresh.endRefreshing()
                    
                    //persist animation
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        self.reloadUI()
                    }
                    
                    
                    break
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
            
        })
    }
    
    
    func reloadUI(){
        reloadUserPosition()
        self.tableView.reloadData()
        if let selectedRanking = ranking {
            drawSuccessOrEmptyVisibility(selectedRank: selectedRanking)
        }
        
    }
    
    func reloadUserPosition(){
        if let position = userRankingPosition{
            
            let url = URL(string: position.avatarURL ?? "")
            
            userAvatar.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            userAvatar.round()
            userPositionLabel.text = position.getPositionLabel()
            //position
            userPosition.text = String(position.position ?? 0)
            
            //nickname
            userNickname.text = position.username
            
            //points
            userPoints.text = String(position.points ?? 0)
            
            userPointsLabel.text = self.ranking?.getValueLabel() ?? "punti"
            endUserRankingLoadingAnimation()
        }
    }
    
    
    
    func setTitle(title:String, subtitle:String) {
        titleLabel.text = title
        subtitleLabel.text = subtitle
        titleLabel.isHidden = title.isEmpty
        subtitleLabel.isHidden = subtitle.isEmpty
    }
    
    
    
    
    @IBAction func goToAwards(_ sender: Any) {
        self.performSegue(withIdentifier: "showAwards", sender: nil)
    }
    
    
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return self.ranking?.rankingPositions?.count ?? 10
    }
    
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "rankingCell", for: indexPath) as! RankingTableViewCell
        let data = self.ranking?.rankingPositions?[indexPath.row]

        if(self.ranking != nil){
            
            if(data?.username == user?.username){
                cell.backgroundColor = UIColor.systemGray4
            }else{
                cell.backgroundColor = UIColor.white
            }
            
            cell.positionTitleLabel.text = data?.getPositionLabel() ?? ""
            
            //avatar
            let url = URL(string: data?.avatarURL ?? "")
            cell.avatarImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            
            
            //position
            cell.positionNumberLabel.text = String(data?.position ?? 0)
            
            //nickname
            cell.nickname.text = data?.username
            
            //points
            cell.points.text = String(data?.points ?? 0)
            
            cell.pointLabel.text = self.ranking?.getValueLabel() ?? "punti"
            
            cell.hideAnimation()
        
        }

        
        return cell
    }
    
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let rankingPositions = self.ranking?.rankingPositions else {
            return
        }
        
        //tap on my username
        if(indexPath.row == 0){
            //show my position
            let mydata = self.ranking?.rankingPositions?.filter{$0.username == self.user?.username}.first
            
            UIView.transition(with: tableView, duration: 10, options: .curveLinear, animations: {self.reloadUI()}, completion:{ (success) in
                        if success {
                            self.scrollToPosition(position: mydata?.position ?? 0)
                        }
                    })
        }
    }
    
    private func scrollToPosition(position: Int){
        DispatchQueue.main.async {
            let indexPath = IndexPath(row: position-1, section: 0)
            self.tableView.scrollToRow(at: indexPath, at: .top, animated: true)
            
            let selectedINdex = IndexPath(row: position, section: 0)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.tableView.selectRow(at: selectedINdex, animated: true, scrollPosition: .top)
            }
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 82
    }
     
    @IBAction func onInfoClicked(_ sender: Any) {
        if let organization = selectedOrganization{
            InitiativeHelper.openAlertDialog(title: organization.title ?? "",
                                             message: organization.regulation ?? "",
                                             link: organization.getRegulationLink(),
                                             viewController: self)

        }
    }

    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showAwards" {
            
            let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
            let navController = storyBoard.instantiateViewController(withIdentifier: "awardsController") as! UINavigationController
            
            let detailController = navController.topViewController as! AwardsTableViewController
            detailController.selectedRanking = self.selectedRanking
            detailController.selectedOrganization = self.selectedOrganization
            present(navController, animated: true, completion: nil)
            
        }
    }


}
