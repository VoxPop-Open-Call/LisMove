//
//  TrophyTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import SkeletonView
import Kingfisher
import SwiftUI
import RealmSwift
import Toast_Swift
import Resolver

class TrophyTableViewController: UIViewController, UITableViewDelegate, UITableViewDataSource  {

    @IBOutlet weak var tableView: UITableView!
    var user = DBManager.sharedInstance.getCurrentUser()
    var trophyListWithOrganization: [ArchievementWithOrganization]? = nil
    
    var selectedArchievement: Archievement?
    var selectedOrganization: Organization?
    var selectedRanking: Ranking?
    
    let refresh = UIRefreshControl()
  
    @IBOutlet weak var rankingPickerArea: UIView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    
    @Injected var initiativeRepository: InitiativeRepository
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initView()
        
        //sync global trophy
        fetchTrophy()
        
        initNavigationItemTitleView()
       
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

        self.navigationItem.rightBarButtonItem?.isEnabled = false
        self.navigationItem.rightBarButtonItem?.tintColor = UIColor.clear
        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        
        fixAppBar()
        tableView.dataSource = self
        tableView.delegate = self
        
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

    private func initNavigationItemTitleView(title: String? = "Tutte", subtitle: String? = "") {
      
        self.setTitle(title: title!, subtitle: subtitle!)


        let recognizer = UITapGestureRecognizer(target: self, action: #selector(self.titleWasTapped))
        rankingPickerArea.isUserInteractionEnabled = true
        rankingPickerArea.addGestureRecognizer(recognizer)
    }

    //switch ranking
    @objc private func titleWasTapped() {
        //open modal controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Wizard", bundle: nil)
        let modalController = storyBoard.instantiateViewController(withIdentifier: "modalController") as! ChooseRankingModalTable
        modalController.achivementSelect = true
        
        modalController.modalPresentationStyle = .popover
        if let popoverPresentationController = modalController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        self.present(modalController, animated: true, completion: nil)
        
        modalController.onDoneBlock = { rank,orga in

            self.selectedOrganization = orga
            
            //safe exit
            if(orga == nil && rank == nil){
                return
            }
            let title = rank?.title ?? orga?.title ?? ""
            self.updateTrophy(title: title, orga: orga)
            

        }
    }
    
    func updateTrophy(title: String, orga: Organization?){
        
        self.selectedOrganization = orga
        self.initNavigationItemTitleView(title: title, subtitle: "")
        
        //clear ranking list
        self.trophyListWithOrganization = nil
        self.tableView.reloadData()

        
        self.view.makeToast("Aggiornamento in corso")
        
        
        
        if(orga != nil){
            //filter trophy by orga
            self.fetchTrophy(organizationId: orga?.id, filterStatus: true)
            
        }else{
            
            if(title == "Tutte"){
                
                //download global
                self.selectedArchievement = nil
                
                //hide right button
                self.navigationItem.rightBarButtonItem?.isEnabled = false
                self.navigationItem.rightBarButtonItem?.tintColor = UIColor.clear
                
                self.fetchTrophy()
                
            } else if(title == "Community"){
                
                //download global
                self.selectedArchievement = nil
                
                //hide right button
                self.navigationItem.rightBarButtonItem?.isEnabled = false
                self.navigationItem.rightBarButtonItem?.tintColor = UIColor.clear
                
                self.fetchTrophy(organizationId: nil, filterStatus: true)
                
            }else{
                self.fetchTrophy()
            }
            
            
            
        }
        
    }
    

    @objc private func refreshData(_ sender: Any) {
        
        
        if(self.selectedOrganization == nil){
            //fetch ranking
            fetchTrophy()
        }else{
            fetchTrophy(organizationId: self.selectedOrganization?.id, filterStatus: true)
        }
    }
    
    
    private func fetchTrophy(organizationId: Int? = nil, filterStatus: Bool = false){

        NetworkingManager.sharedInstance.getUserArchievement(uid: user!.uid!, completion: { result in
            switch result {
                case .success(let data):
                
                    var achievementList = data
                
                    if(filterStatus){
                        achievementList = data.filter{$0.organization == organizationId}
                    }
                
                
                    self.trophyListWithOrganization = []

                    if(achievementList.count > 0 ){

                        //load organization
                        achievementList.forEach{item in
                            
                            if item.organization != nil {
                                
                                self.initiativeRepository.getOrganization(oid: item.organization!){ result in
                                    
                                    switch result {
                                        case .success(let org):
                                        
                                        self.trophyListWithOrganization?.append(ArchievementWithOrganization(archievement: item, organization: org))
                                        self.trophyListWithOrganization?.sort(by:  {$0.archievement.target < $1.archievement.target})
                                        DispatchQueue.main.async {
                                            self.tableView.reloadData()
                                        }
                                        
                                        
                                        case .failure(let error):
                                            break
                                    }
                                    
                                }
                                
                            }else{
                                
                                self.trophyListWithOrganization?.append(ArchievementWithOrganization(archievement: item, organization: nil))
                            }
                            
                        }
                    }else{
                        
                        //advise user
                        if(self.trophyListWithOrganization!.count == 0){
                            self.view.makeToast("Nessuna coppa presente")
                        }
                        
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


    
    func setTitle(title:String, subtitle:String) {
        titleLabel.text = title
        subtitleLabel.text = subtitle
        titleLabel.isHidden = title.isEmpty
        subtitleLabel.isHidden = subtitle.isEmpty
    }
    
    
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return self.trophyListWithOrganization?.count ?? 10
    }
    
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "trophyCell", for: indexPath) as! TrophyTableViewCell

        if(self.trophyListWithOrganization != nil){
            let archievementUI = self.trophyListWithOrganization![indexPath.row].asAchievementItemUI()
            let url = URL(string: archievementUI.imageUrl ?? "")
            cell.trophyImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            
            cell.trophyTitle.text = archievementUI.name
            
            cell.fulfilledImage.isHidden = !archievementUI.fullfilled
            cell.trophyProgressBar.isHidden = archievementUI.fullfilled
            cell.trophyProgressText.isHidden = archievementUI.fullfilled
            
            cell.trophyProgressBar.progress = Float(archievementUI.percentage)
            cell.trophyProgressText.text = archievementUI.percentageValue
        
            cell.thropyCountdown.text = archievementUI.daysCounter ?? ""
            cell.thropyCountdown.isHidden = archievementUI.daysCounter == nil
            
            cell.throphySubtitle.text = archievementUI.organizationLabel
            cell.throphySubtitle.isHidden = archievementUI.organizationLabel == ""
            cell.thropyFulfilledLabel.text = archievementUI.fullfilled ? archievementUI.percentageValue : ""
            cell.hideAnimation()
   
        }

        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let selectedArchievement = self.selectedArchievement else {
            return
        }
        
        
        self.selectedArchievement = self.trophyListWithOrganization![indexPath.row].archievement

        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "awardsController") as! UINavigationController
        
        let detailController = navController.topViewController as! AwardsTableViewController
        detailController.selectedAchievement = self.selectedArchievement
        detailController.achivementSelect = true
        
        present(navController, animated: true, completion: nil)
        
        
    }
    @IBAction func onInfoClicked(_ sender: Any) {
        let alert = UIAlertController(title: "Regolamento", message: "Le coppe sono dei premi ottenuti in base ai km o punti percorsi in un determinato periodo temporale.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
        present(alert, animated: true, completion: nil)

    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 100
    }

    
}
