//
//  SessionFeedbackViewController.swift
//  LisMove
//
//

import UIKit
import SimpleCheckbox
import PKHUD

class SessionFeedbackViewController: UIViewController, UITextViewDelegate {
    var sessionId: String = ""
    @IBOutlet weak var navigationTitle: UINavigationItem!
    var onSessionUpdated: ((_ session: Session)->())? = nil
    var isOnlyPoints = true
    var viewModel = SessionFeedbackViewModel()
    @IBOutlet weak var noteLabel: UILabel!

    @IBOutlet weak var navigationBar: UINavigationBar!
    @IBOutlet weak var noteTextView: UITextView!
    @IBOutlet weak var fieldStackView: UIStackView!
    @IBOutlet weak var sendButton: PBButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        viewModel.initViewModel(withSessionId:sessionId, isOnlyPoints: isOnlyPoints, withDelegate: self)
        noteLabel.text = isOnlyPoints ? "Se vuoi puoi allegare un messaggio:" : "Note: "
        navigationTitle.title = isOnlyPoints ? "Richiedi verifica manuale" : "Segnala un problema"
        let sendText = isOnlyPoints ? "Richiedi verifica" : "Invia"
        sendButton.setTitle(sendText, for: .normal)
        
        let gesture = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboardOnTap))
        self.view.addGestureRecognizer(gesture)
        noteTextView.delegate = self
        reloadFeedbackFormOptions()
        // Do any additional setup after loading the view.
    }
    
    @objc func dismissKeyboardOnTap(){
        noteTextView.resignFirstResponder()
    }
    
    func textFieldShouldReturn(textField: UITextField!) -> Bool {   //delegate method
      textField.resignFirstResponder()
      return true
    }
    
    
    func reloadFeedbackFormOptions(){
        fieldStackView.subviews.forEach({view in
            if let checkbox = view as? PBCheckbox{
                checkbox.removeFromSuperview()
            }
        }
        )
        viewModel.options.forEach({option in
        
            fieldStackView.addArrangedSubview(getCheckboxFromOption(option))
        })
    }
   
    func getCheckboxFromOption(_ data: FeedBackFormOption) -> PBCheckbox{
        let checkbox = PBCheckbox()
        checkbox.isChecked = false
        checkbox.text = data.label
        checkbox.tag = data.id
        return checkbox
    }
    
    @IBAction func onSendClicked(_ sender: Any) {
        sendRequest()
    }
    
    func sendRequest(){
        let selectedFields = getSelectedFields()
        let note = noteTextView.text ?? ""
        if(selectedFields.isEmpty && !isOnlyPoints){
            onError(message: "Seleziona almeno un problema")
        }else{
            viewModel.sendRequest(selectedOptions: selectedFields, notes: note)
        }
    }
    

    
    private func getSelectedFields()-> [Int]{
        var ids = [Int]()
        fieldStackView.subviews.forEach({view in
            if let checkbox = view as? PBCheckbox{
                if(checkbox.isChecked){
                    ids.append(checkbox.tag)
                }
            }
        })
        return ids
    }
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}

extension SessionFeedbackViewController: SessionFeedbackDelegate{
    func onLoading() {
        LogHelper.log(message: "Error")
    }
    
    func onDialogLoading() {
        HUD.show(.progress, onView: self.view)
    }
    
    func onError(message: String) {
        if(HUD.isVisible){
            HUD.flash(.labeledError(title: "Sì è verificato un errore", subtitle: "Riprova più tardi"), onView: self.view)
        }
        NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": message])
    }
    
    func onFeedbackReceived() {
        reloadFeedbackFormOptions()
    }
    
    func onSessionSent(session:Session) {
        HUD.flash(.success, delay: 2.0){success in
            self.onSessionUpdated?(session)
        }

    }
    
    
}
