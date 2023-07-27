//
//  TermsViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 19/04/21.
//

import UIKit

class TermsViewController: UITableViewController{
    
    @IBOutlet weak var restartAccountArrow: UIBarButtonItem!
    
    //type
    var isModifyProfileMode = false
    
    //callback closure
    var onDoneBlock: (() -> Void)?
    
    var terms = [
        Term(text: "Accetto i termini d'uso"),
        Term(text: "Accetto la privacy policy"),
        Term(text: "Confermo di avere più di 14 anni"),
        Term(text: "Accetto finalità di marketing"),
    ]
    
    //db
    var DBManager = (UIApplication.shared.delegate as? AppDelegate)?.DB
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        tableView.delegate = self
        tableView.dataSource = self
        
        
        initView()
    }
    
    
    private func initView(){
        
        if(isModifyProfileMode){
            self.restartAccountArrow.customView?.isHidden = true
            self.navigationItem.leftBarButtonItem = nil
        }
        
        //add footer with bottom
        let customView = UIView(frame: CGRect(x: 0, y: 0, width: 200, height: 50))
        let button = UIButton(frame: CGRect(x: 0, y: 0, width: 200, height: 50))
        button.setTitle("Invia", for: .normal)
        button.addTarget(self, action: #selector(buttonAction), for: .touchUpInside)
        button.backgroundColor = .systemRed
        button.layer.cornerRadius = 16
        
        customView.addSubview(button)
        
        button.topAnchor.constraint(equalTo: customView.topAnchor).isActive = true
        button.bottomAnchor.constraint(equalTo: customView.bottomAnchor).isActive = true
        button.leadingAnchor.constraint(equalTo: customView.leadingAnchor).isActive = true
        button.trailingAnchor.constraint(equalTo: customView.trailingAnchor).isActive = true
        button.translatesAutoresizingMaskIntoConstraints = false
        button.layoutMargins =  UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16)
        
        
        tableView.tableFooterView = customView
    }
    
    @objc func buttonAction(_ sender: UIButton!) {
        //check all terms
        if(self.terms[0].isSelected
            && self.terms[1].isSelected
            && self.terms[2].isSelected){
            
            let user = self.DBManager!.getCurrentUser()
            
            do{
                try self.DBManager?.getDB().safeWrite {
                    user!.termsAccepted = true
                    
                    if(self.terms[3].isSelected){
                        user!.marketingTermsAccepted = true
                    }
                }
            }catch{}

        
            if(isModifyProfileMode){
                
                self.onDoneBlock?()
                
                self.dismiss(animated: true, completion: nil)
                
            }else{
                //open dashboard controller
                let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
                let AccountController = storyBoard.instantiateViewController(withIdentifier: "AccountController") as! UINavigationController
                AccountController.modalPresentationStyle = .fullScreen

                self.present(AccountController, animated: true, completion: nil)
            }
            

            
        }else{
            let alert = UIAlertController(title: "Ops", message: "Devi accettare tutti i termini e condizioni", preferredStyle: .alert)
            if let popoverPresentationController = alert.popoverPresentationController {
                popoverPresentationController.sourceView = self.view
                popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
            }
            
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    @IBAction func restartAccountSetup(_ sender: Any) {
        let alert = UIAlertController(title: "Attenzione", message: "Dovrai effettuare nuovamente l'accesso/iscrizione", preferredStyle: .alert)
        if let popoverPresentationController = alert.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        
        alert.addAction(UIAlertAction(title: "Annulla", style: .destructive))
        alert.addAction(UIAlertAction(title: "Conferma", style: .default, handler: {_ in 
            
            //clean user table
            self.DBManager?.restartAccountFlow()
            
            //restart from splash screen
            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
            let splashViewController = storyBoard.instantiateViewController(withIdentifier: "SplashController") as! SplashViewController
            splashViewController.modalPresentationStyle = .fullScreen
            
            self.present(splashViewController, animated: false, completion: nil)
            
            
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.terms.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        

        let cell = self.tableView.dequeueReusableCell(withIdentifier: "termsCell", for: indexPath) as! TermTableViewCell
        cell.title.text = self.terms[indexPath.row].title
        
        cell.callback = { newValue in
            self.terms[indexPath.row].isSelected = newValue
        }
        
        
        return cell
    }
    

}
