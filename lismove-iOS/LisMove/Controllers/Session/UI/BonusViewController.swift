//
//  BonusViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 13/11/21.
//

import UIKit

class BonusViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var otherInitiativeLabel: UILabel!
    @IBOutlet weak var bonusTable: UITableView!
    
    private var bonusList: [ActiveProjectItemUI] = []
    

    override func viewDidLoad() {
        super.viewDidLoad()

        
        //set light theme
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        bonusTable.delegate = self
        bonusTable.dataSource = self
        
        //init bonus list
        self.bonusList = SessionManager.sharedInstance.pointManager?.enrollmentsList.map{ activeEnrollment -> ActiveProjectItemUI in
            let organization = SessionManager.sharedInstance.pointManager?.organizationList.first(where: {$0.id == activeEnrollment.organization})
            return ActiveProjectItemUI(image: organization?.initiativeLogo ?? "", regulation: organization?.regulation ?? "Nessun regolamento disponibile", organizationName: organization?.title ?? "")
            
        } ?? []
        
        DispatchQueue.main.async {
            self.bonusTable.reloadData()
        }

        
        
        //init view
        initView()
        
    }
    
    
    private func initView(){
        
        //set tap gesture on otherInitiativeLabel
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleTap(_:)))
        otherInitiativeLabel.addGestureRecognizer(tap)
        
        
    }

    @objc func handleTap(_ sender: UITapGestureRecognizer? = nil) {
        //open initiative controller
        let storyBoard: UIStoryboard = UIStoryboard(name: "Dashboard", bundle: nil)
        let termsViewController = storyBoard.instantiateViewController(withIdentifier: "InitiativeViewController") as! UINavigationController
        termsViewController.modalPresentationStyle = .popover
        
        if let popoverPresentationController = termsViewController.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        self.present(termsViewController, animated: true, completion: nil)
        
    }
    
    
    
    // MARK: - Table view data source

    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return bonusList.count
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
    
        return 1

    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "bonusCell", for: indexPath)
        
        let bonus = self.bonusList[indexPath.section]
        
        cell.textLabel?.text = bonus.organizationName
        cell.textLabel?.textAlignment = .center
        cell.textLabel?.font = UIFont(name: "OpenSans-Medium", size: 18)
        cell.textLabel?.textColor = .systemGray
        cell.textLabel?.numberOfLines = 1
        
        cell.detailTextLabel?.text = bonus.regulation
        cell.detailTextLabel?.numberOfLines = 0
        
        cell.backgroundColor = .white
        cell.clipsToBounds = true
        cell.contentView.frame = cell.contentView.frame.inset(by: UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8))
        
        return cell
        
    }

    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let link = self.bonusList[indexPath.section].regulationLink{
            UIApplication.shared.open(link)
        }
    }
    
    
}
