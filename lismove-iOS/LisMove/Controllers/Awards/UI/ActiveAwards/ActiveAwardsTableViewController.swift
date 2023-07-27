//
//  ActiveAwardsTableViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 13/11/21.
//

import UIKit
import Toast_Swift

class ActiveAwardsTableViewController: UITableViewController {

    private var bonusList: [ActiveProjectItemUI] = []
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        //set light theme
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        //init bonus list
        self.bonusList = SessionManager.sharedInstance.pointManager?.enrollmentsList.map{ activeEnrollment -> ActiveProjectItemUI in
            let organization = SessionManager.sharedInstance.pointManager?.organizationList.first(where: {$0.id == activeEnrollment.organization})
            return ActiveProjectItemUI(image: organization?.initiativeLogo ?? "", regulation: organization?.regulation ?? "Nessun regolamento disponibile", organizationName: organization?.title ?? "")
            
        } ?? []
        
        //advise user
        if(self.bonusList.count == 0){
            self.view.makeToast("Nessun premio presente")
        }
        
        DispatchQueue.main.async {
            self.tableView.reloadData()
        }
        
        
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return self.bonusList.count
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return 1
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "bonusCell", for: indexPath)
        
        let bonus = self.bonusList[indexPath.section]
        
        cell.textLabel?.text = bonus.organizationName
        cell.textLabel?.font = UIFont(name: "OpenSans-Medium", size: 18)
        cell.textLabel?.textColor = .systemGray
        cell.textLabel?.numberOfLines = 1
        
        cell.detailTextLabel?.text = "Visualizza regolamento completo"
        cell.textLabel?.textColor = .black
        cell.detailTextLabel?.numberOfLines = 1
    
        
        return cell
        
    }

    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        let item = self.bonusList[indexPath.section]
        
       //BASIC INFORMATIONS
        let title = item.organizationName
        let message = item.regulation
        
        //ATTRIBUTED STRINGS
        let linkFixedMessage = "Per consultare i periodi di validità di ogni singola iniziativa, navigare nell'app, menù "
        let linkText = " 'Altro -> Gestione Iniziative'  \n\n"
    
        let attributedString = NSMutableAttributedString()

        //attributes
        let attributes = [NSAttributedString.Key.underlineStyle: NSUnderlineStyle.thick.rawValue, NSAttributedString.Key.foregroundColor: UIColor.systemRed] as [NSAttributedString.Key : Any]

        //custom string
        let attributedQuote = NSAttributedString(string: linkText, attributes: attributes)
        
        attributedString.append(NSAttributedString(string: linkFixedMessage))
        attributedString.append(attributedQuote)
        attributedString.append(NSAttributedString(string: message))
    
        
        //ALERT VIEW
        let alert = UIAlertController(title: title, message: "", preferredStyle: .alert)
        
        
        alert.setValue(attributedString, forKey: "attributedMessage")
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect =  CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        alert.addAction(UIAlertAction(title:item.regulationLink != nil ? "Visita sito" : "Ok", style: .default, handler: { action in
            if let link = item.regulationLink{
                UIApplication.shared.open(link)
            }
        

        }))
        if(item.regulationLink != nil){
            alert.addAction(UIAlertAction(title: "Annulla", style: .cancel, handler: nil))
        }
        
        self.present(alert, animated: true, completion: nil)
        
        
    }

}
