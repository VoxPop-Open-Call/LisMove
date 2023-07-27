//
//  EscursionTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 17/05/21.
//

import UIKit
import Floaty
import Toast_Swift
import Foundation
import SkeletonView

class SessionTableViewController: UITableViewController, SessionListDelegate {
  
    public var sessionTypeSelected = SessionType.all
    
    let dateFormatterGet = DateFormatter()
    let dateFormatterView = DateFormatter()
    
    var startDateSelected: Date!
    var endDateSelected: Date!
    
    
    @IBOutlet weak var filterLabel: UILabel!
    
    var viewmodel = SessionListViewModel()
    var sessions: [Session]?{
        get {
            return viewmodel.sessions
        }
    }
    
    //selected session
    var selectedSession: Session?
    
    let refresh = UIRefreshControl()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.tableView.delegate = self
        self.viewmodel.delegate = self
        
        dateFormatterGet.dateFormat = "yyyy-MM-dd"
        dateFormatterView.dateFormat = "dd-MM-yyyy"
        
        endDateSelected = Date()
        startDateSelected = endDateSelected.adding(.month, value: -1)
        

        initView()

        viewmodel.setSessionType(type: sessionTypeSelected)
        viewmodel.refreshSessions(start: startDateSelected, end: endDateSelected)
        
    }

    private func initView(){
        
        fixAppBar()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
    
        
        /*
        // Add Refresh Control to Table View
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refresh
        } else {
            tableView.addSubview(refresh)
        }
        
        // Configure Refresh Control
        refresh.addTarget(self, action: #selector(refreshData(_:)), for: .valueChanged)
        */


        
        initFilterLableView()
        
    }
    
    func initFilterLableView(){
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.openFilterSection(_:)))
        filterLabel.addGestureRecognizer(tap)
        filterLabel.isUserInteractionEnabled = true
        
        
        filterLabel.text = "\(dateFormatterView.string(from: startDateSelected)) - \(dateFormatterView.string(from: endDateSelected))"
    }
    
    
    @objc func openFilterSection(_ sender: UITapGestureRecognizer? = nil) {
        
        //open modal controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Profile", bundle: nil)
        let navController = storyBoard.instantiateViewController(withIdentifier: "filterSessionNavigation") as! UINavigationController
        let detailController = navController.topViewController as! DateFilterViewController
        navController.modalPresentationStyle = .popover
        if let popoverPresentationController = navController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        detailController.sessionListViewModel = self.viewmodel
        detailController.startDateSelected = self.startDateSelected
        detailController.endDateSelected = self.endDateSelected
        
        self.present(navController, animated: true, completion: nil)
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
    
    /*
    @objc private func refreshData(_ sender: Any) {
        //fetch sessions
        viewmodel.refreshSessions()
    }*/
    
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        var count = viewmodel.isLoading ? 10 : (sessions?.count ?? 0)
        return count
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return 1
    }

    // Set the spacing between sections
    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 8
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "sessionCell", for: indexPath) as! SessionTableViewCell
        
        guard let sessions = sessions else {
            return cell
        }

        
        if(!viewmodel.isLoading){
            let session = self.sessions![indexPath.section]
                        
            if let date = session.startTime.value{
                cell.date.text = DateTimeUtils.getReadableLongDateTime(date: Double(date))
                
            }else{
                cell.date.text = ""
            }
            
            //distance
            cell.distanceText.text = String(session.getTotalKM().rounded(toPlaces: 2))
            

            cell.timeLabel.isHidden = true
            //national point
            cell.nationalPoint.text = String(session.nationalPoints)
            //initiative point
            cell.initiativePoint.text = String(session.initiativePoints())
            
            //sync status
            if(session.sendToServer){
                //cell.syncStatus.isHidden = true
                cell.syncStatus.image = UIImage(systemName: "icloud.slash")
                cell.syncStatus.tintColor = .systemRed
            }else{
                cell.syncStatus.isHidden = true
            }


            cell.layer.cornerRadius = 8
            cell.layer.shadowColor = UIColor.gray.cgColor
            cell.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
            cell.layer.shadowRadius = 8.0
            cell.layer.shadowOpacity = 0.7
            cell.layer.borderWidth = 0.3
            cell.layer.borderColor = UIColor.gray.cgColor
            cell.layer.frame.inset(by: UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16))
            
            cell.hideAnimation()
        }

        
        return cell
    }
    

    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let sessions = sessions else {
            return
        }
        
        if let session = self.sessions?[indexPath.section]{
            self.selectedSession = session
            performSegue(withIdentifier: "showSessionDetails", sender: nil)
            tableView.deselectRow(at: indexPath, animated: true)

        }
        
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 102
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showSessionDetails" {
            
            if(self.selectedSession != nil){
                let navController = segue.destination as! UINavigationController
                let detailController = navController.topViewController as! SessionInfoViewController
                detailController.session = self.selectedSession
                
                
                detailController.modalPresentationStyle = .fullScreen
                
                present(navController, animated: true, completion: nil)
            }
        }
    }
    
    func secondsToHoursMinutesSeconds (seconds : Int) -> (Int, Int, Int) {
      return (seconds / 3600, (seconds % 3600) / 60, (seconds % 3600) % 60)
    }
    
    func onReloadTable() {
        if(refreshControl?.isRefreshing == true){
            refreshControl?.endRefreshing()
        }
        tableView.reloadData()
    }
    
    func onFilterDateComplete(startDate: Date, endDate: Date) {
        self.startDateSelected = startDate
        self.endDateSelected = endDate

        self.initFilterLableView()
        
        self.viewmodel.refreshSessions(start: startDate, end: endDate)
        
        self.view.makeToast("Aggiornamento sessioni in corso")
    }
    
    
}


public enum SessionType{
    case all
    case work
}
