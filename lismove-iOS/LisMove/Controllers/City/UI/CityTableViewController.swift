//
//  CityTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/04/21.
//

import UIKit
import Foundation
import Toast_Swift

class CityTableViewController: UITableViewController, UISearchBarDelegate, UISearchResultsUpdating {
    
    
    //db
    var DB: DBManager?
    
    var cityList: [Comune] = []
    var cityListFiltered: [Comune] = []
    var lastSelection: IndexPath?
    var onDoneBlock : (([String: String]) -> Void)?
    
    lazy var searchBar:UISearchBar = UISearchBar()
    var resultSearchController = UISearchController()
    
    var citySelected: [String: String] = [:]
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        self.DB = DBManager.sharedInstance
        
        addNavigationBar()
    
        
        //load all city
        let decoder = JSONDecoder()
        
        if let file = Bundle.main.url(forResource: "comuni", withExtension: "json") {
            let json = try? Data(contentsOf: file)
            
            if let cities = try? decoder.decode([Comune].self, from: json!) {

                self.cityList = cities
                self.cityListFiltered = cities
                
                tableView.reloadData()
            }
            
        } else {
            print("no file")
        }
        
    }
    
    private func addNavigationBar() {
        
        resultSearchController = ({
            let controller = UISearchController(searchResultsController: nil)
            controller.searchResultsUpdater = self
            controller.dimsBackgroundDuringPresentation = false
            controller.searchBar.sizeToFit()
            controller.searchBar.searchBarStyle = UISearchBar.Style.prominent
            controller.searchBar.placeholder = "Cerca comune..."
            controller.searchBar.isTranslucent = false

            tableView.tableHeaderView = controller.searchBar

            return controller
        })()
    
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isBeingDismissed {
            self.onDoneBlock!(self.citySelected)
        }
    }

    
    func updateSearchResults(for searchController: UISearchController) {
        cityListFiltered.removeAll(keepingCapacity: false)

        let array = cityList.filter { $0.nome!.contains(searchController.searchBar.text!) }
        cityListFiltered = array

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
            return cityListFiltered.count
        } else {
            return cityList.count
        }
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cityCell", for: indexPath)

        if (resultSearchController.isActive) {
            cell.textLabel?.text = self.cityListFiltered[indexPath.row].nome! + ", (" + self.cityListFiltered[indexPath.row].cap![0] + ")"

        }
        else {
            cell.textLabel?.text = self.cityList[indexPath.row].nome! + ", (" + self.cityList[indexPath.row].cap![0] + ")"

        }
        
        cell.accessoryType = .none
        return cell
        
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        self.citySelected = [self.cityListFiltered[indexPath.row].codice ?? "0" : self.cityListFiltered[indexPath.row].nome!]

        //self.view.makeToast("Comune selezionato correttamente", duration: 3.0, position: .bottom)
        
        self.tableView.cellForRow(at: indexPath)?.accessoryType = .checkmark
        self.presentingViewController?.dismiss(animated: true)
        

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
