//
//  InfoAndConditionViewController.swift
//  LisMove
//
//

import UIKit
import Resolver
import Kingfisher

class InfoAndConditionViewController: UIViewController {
    var entries = [
        InfoEntry(label: "Termini e condizioni", url: "https://lismoveadmin.it/termini-condizioni-lis-move/"),
        InfoEntry(label: "Privacy Policy", url: "https://lismoveadmin.it/lis-move-privacy-policy/"),
        InfoEntry(label: "Crediti", url: "https://lismoveadmin.it/credits/")
    ]
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var bannerImage: UIImageView!
    
    let viewModel = InfoAndConditionViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        tableView.dataSource = self
        tableView.delegate = self
        
        viewModel.delegate = self
        viewModel.loadSetings()
        
       
        // Do any additional setup after loading the view.
    }
    



    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    
    func openWebView(url: String, title: String){
        
        let documentVc = DocumentViewController()
        let navController = UINavigationController(rootViewController: documentVc)
        documentVc.documentURL = url
        documentVc.titleString = title
        self.present(navController, animated: true, completion: nil)
    }
    
}


extension InfoAndConditionViewController: UITableViewDataSource{
    // Return the number of rows for the table.
    
    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return entries.count
    }

    // Provide a cell object for each row.
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
       // Fetch a cell of the appropriate type.
        let cell = tableView.dequeueReusableCell(withIdentifier: "infoEntry", for: indexPath) as! InfoTableViewCell
       let entry = entries[indexPath.row]
       // Configure the cell’s contents.
        cell.title?.text = entry.label
           
       return cell
    }
    
}

extension InfoAndConditionViewController: UITableViewDelegate{
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath){
        let entry = entries[indexPath.row]
        openWebView(url: entry.url, title: entry.label)
        tableView.deselectRow(at: indexPath, animated: true)
    }
    

}

extension InfoAndConditionViewController: InfoAndConditionDelegate{
    func onBannerReceived(url: String){
        bannerImage.isHidden = false
        guard let url = URL(string: url) else {
            return
        }
        bannerImage.kf.setImage(with: url)
    }
}

struct InfoEntry{
    let label: String
    let url: String
}
