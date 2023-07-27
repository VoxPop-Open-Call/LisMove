//
//  HelpViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import UIKit
import Mobilisten
import WebKit
import Floaty

class HelpViewController: UIViewController {

    @IBOutlet weak var helpWebView: WKWebView!
    
    @IBOutlet weak var whatsappFab: Floaty!
    override func viewDidLoad() {
        super.viewDidLoad()
    
        initWebView()
        
        if(LisMoveEnvironmentConfiguration.IS_CHAT_ENABLED){
                initZohoChat()
            }else{
                initWhatsappChat()
            }

    }
    
    
    private func initWebView(){
        let url = URL(string: "https://lismoveadmin.it/help-faq/")!
        helpWebView.load(URLRequest(url: url))
        helpWebView.allowsBackForwardNavigationGestures = true
    }
    
    private func initWhatsappChat(){
        let fabClickRecognizer = UITapGestureRecognizer(target: self, action: #selector(openWhatsapp(_:)))

            // Add Tap Gesture Recognizer
        whatsappFab.addGestureRecognizer(fabClickRecognizer)
        whatsappFab.isHidden = false
    }
    
    @objc func openWhatsapp(_ sender: UITapGestureRecognizer){
        WhatsappHelper.openWhatsappWithDefaultChat()
    }
    
    
    private func initZohoChat(){
        whatsappFab.isHidden = true

        //theme
        let customTheme = ZohoSalesIQ.Theme.baseTheme
        customTheme.themeColor = UIColor.systemRed
        customTheme.Navigation.backgroundColor = UIColor.white
        customTheme.Launcher.backgroundColor = UIColor.systemRed
        customTheme.Launcher.UnreadBadge.backgroundColor = UIColor.systemBlue
        ZohoSalesIQ.showLauncher(true)
        ZohoSalesIQ.Theme.setTheme(theme: customTheme)
        
        
        //title
        ZohoSalesIQ.Chat.setTitle("Lis Move Live Support")
        if let currentUser = DBManager.sharedInstance.getCurrentUser(){
            //user info
            ZohoSalesIQ.registerVisitor(currentUser.uid)
            ZohoSalesIQ.Visitor.setName(currentUser.getFullName())
            ZohoSalesIQ.Visitor.setEmail(currentUser.email)
            ZohoSalesIQ.Visitor.setContactNumber(currentUser.phoneNumber)
        }
       
    }
    

    
    override func viewWillDisappear(_ animated: Bool) {
        if(LisMoveEnvironmentConfiguration.IS_CHAT_ENABLED){
            ZohoSalesIQ.showLauncher(false)

        }
    }

}


