//
//  SeatsTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 10/07/21.
//

import UIKit

class SeatsTableViewController: UITableViewController, UISearchBarDelegate, UISearchResultsUpdating {

  
    public var selectedOrganization: Organization?
    
    var seatList: [OrganizationSeat] = []
    var seatListFiltered: [OrganizationSeat] = []
    var lastSelection: IndexPath?
    var onDoneBlock : ((OrganizationSeat) -> Void)?
    
    lazy var searchBar:UISearchBar = UISearchBar()
    var resultSearchController = UISearchController()
    
    var seatSelected: OrganizationSeat?
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        syncSeats()
        
        //addNavigationBar()
        
    }
    
    private func syncSeats(){
        
        self.tableView.makeToast("Caricamento in corso")
        
        //get organization seat list
        NetworkingManager.sharedInstance.getOrganizationSeat(oid: self.selectedOrganization!.id!, completion: { result in
            switch result {
                case .success(let data):
                
                    
                    self.seatList = data
                    self.seatListFiltered = data
                    
                    self.tableView.reloadData()
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    break
            }
        })
    }
    
    private func addNavigationBar() {
        resultSearchController = ({
            let controller = UISearchController(searchResultsController: nil)
            controller.searchResultsUpdater = self
            controller.dimsBackgroundDuringPresentation = false
            controller.searchBar.sizeToFit()
            controller.searchBar.searchBarStyle = UISearchBar.Style.prominent
            controller.searchBar.placeholder = "Cerca sede..."
            controller.searchBar.isTranslucent = false

            tableView.tableHeaderView = controller.searchBar

            return controller
        })()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isBeingDismissed {
            if(self.seatSelected != nil){
                self.onDoneBlock!(self.seatSelected!)
            }
        }
    }


    func updateSearchResults(for searchController: UISearchController) {
        seatListFiltered.removeAll(keepingCapacity: false)

        let array = seatList.filter { $0.name!.contains(searchController.searchBar.text!) }
        seatListFiltered = array

        self.tableView.reloadData()
    }
    
    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        if  (resultSearchController.isActive) {
            return seatListFiltered.count
        } else {
            return seatList.count
        }
    
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cityCell", for: indexPath)

        
        if (resultSearchController.isActive) {
            cell.textLabel?.text = self.seatListFiltered[indexPath.row].name! + ", (" + self.seatListFiltered[indexPath.row].address! + ")"

        }else {
            cell.textLabel?.text = self.seatList[indexPath.row].name! + ", (" + self.seatList[indexPath.row].address! + ")"

        }
        
        cell.accessoryType = .none
        

        return cell
        
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        self.seatSelected = self.seatListFiltered[indexPath.row]
        
        self.view.makeToast("Sede selezionata correttamente", duration: 3.0, position: .bottom)
        
        self.tableView.cellForRow(at: indexPath)?.accessoryType = .checkmark
        
        self.dismiss(animated: true, completion: nil)


        /*
        if self.lastSelection != nil {
            self.tableView.cellForRow(at: indexPath)?.accessoryType = .checkmark
        }else{
            self.tableView.cellForRow(at: self.lastSelection!)?.accessoryType = .none
            self.tableView.cellForRow(at: indexPath)?.accessoryType = .checkmark

            self.lastSelection = indexPath
        }*/
    }

}
