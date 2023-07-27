//
//  SettingsDetailViewController.swift
//  LisMove
//
//

import UIKit

class SettingsDetailViewController: UITableViewController {
    @IBOutlet weak var sessionDelayLabel: UILabel!
    
    let SESSION_DELAY_INDEX = 0
    let viewModel = SettingsDetailViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()


        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        initInitialDelayValue()
    }
    
    func initInitialDelayValue(){
        let initialDelayValue = viewModel.getActualSessionDelay()
        updateSettingsDelayValue(value: initialDelayValue)
    }

    
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if(indexPath.row == SESSION_DELAY_INDEX){
            showSessionDelayAlert()
        }
        tableView.deselectRow(at: indexPath, animated: true)
    }
    func showSessionDelayAlert(){
        let sessionDelay = viewModel.currentDelay
        
        //session partial data
        let ac = UIAlertController(title: "Aggiornamento parziali sessione", message: nil, preferredStyle: .alert)
        if let popoverPresentationController = ac.popoverPresentationController {
            popoverPresentationController.sourceView = self.view
            popoverPresentationController.sourceRect = CGRect(x: self.view.bounds.size.width / 2.0, y: self.view.bounds.size.height / 2.0, width: 1.0, height: 1.0)
        }
        ac.addTextField()
        //load default partial update
        ac.textFields![0].text = sessionDelay

        let submitAction = UIAlertAction(title: "Ok", style: .default) { [unowned ac] _ in
            let partialSecond = ac.textFields![0]
            
            if(partialSecond.text != ""){
                if(Double(partialSecond.text!)! < 3){
                    
                    self.view.makeToast("Impossibile impostare un delay inferiore a 3s")
                    return
                }
                
                let newValue = self.viewModel.setSessionDelay(partialSecond.text)
                self.updateSettingsDelayValue(value: newValue)
            }
        }

        ac.addAction(submitAction)

        self.present(ac, animated: true)
    }
    
    func updateSettingsDelayValue(value: String){
        sessionDelayLabel.text = value
    }

}
