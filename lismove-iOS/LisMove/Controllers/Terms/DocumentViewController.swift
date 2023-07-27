//
//  DocumentViewController.swift
//  LisMove
//
//

import UIKit
import AVKit
import WebKit

class DocumentViewController: UIViewController {
   
    let webView = WKWebView()
    var documentURL = ""
    var titleString = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.setNavigationBarHidden(false, animated: true)
        
        title = titleString
        
        webView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(webView)

        webView.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor).isActive = true
        webView.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor).isActive = true
        webView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor).isActive = true
        webView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor).isActive = true
        
        webView.load(URLRequest(url: URL(string: documentURL)!))

    }
    

}
