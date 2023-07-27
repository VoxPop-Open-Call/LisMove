//
//  SessionPointsDetailTableViewController.swift
//  LisMove
//
//

import UIKit

class SessionPointsDetailTableViewController: UITableViewController {

    var viewModel = SessionPointDetailViewModel()
    var session: Session!
    
    var pointsUI: [PointItemUI]{
        get {
            return viewModel.pointsUI
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        viewModel.delegate = self
        viewModel.loadPointsFromSession(session: session)
        
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        let count =  pointsUI.count
        if(viewModel.dataLoaded){
            if count == 0{
                tableView.setEmptyView(title: "", message: "Nessuna iniziativa attiva")
            }else{
                tableView.restore()
            }
        }
        return count
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "pointCell", for: indexPath) as! PointCellTableViewCell
        let data = pointsUI[indexPath.row]
        
        cell.setupCell(data: data)
        // Configure the cell...

        return cell
    }
    

   

}

extension SessionPointsDetailTableViewController: SessionPointDetailDelegate{
    
    func onDatUpdate(points: [PointItemUI]) {
        tableView.reloadData()
    }
    

}

struct PointItemUI{
    var name: String
    var point: String
}
