//
//  ScanViewController.swift
//  LismoveSensorSkdTestApp
//
//

import UIKit
import LisMoveSensorSdk
import CoreBluetooth

class ScanViewController: UIPageViewController, UIPageViewControllerDelegate, UIPageViewControllerDataSource {
    
      
    //session
    var sessionManager = SessionManager.sharedInstance
    
    //view
    var pageControl = UIPageControl()
    lazy var orderedViewControllers: [UIViewController] = {
        return [self.newVc(viewController: "firstStepTutorial"),
                self.newVc(viewController: "secondStepTutorial"),
                self.newVc(viewController: "thirdStepTutorial")]
    }()

    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        self.dataSource = self
        self.delegate = self
        
        self.dismissAnyAlertControllerIfPresent()
        
        // This sets up the first view that will show up on our page control
        if let firstViewController = orderedViewControllers.first {
            setViewControllers([firstViewController],
                               direction: .forward,
                               animated: true,
                               completion: nil)
        }
        
        configurePageControl()
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(onDidReceiveNext(_:)), name: NSNotification.Name(rawValue: "NEXT_SCREEN"), object: nil)
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }

    @objc func onDidReceiveNext(_ notification: Notification){
        
        let data = notification.userInfo as! [String: UIViewController]
        let viewController = data["controller"]!
        
        guard let viewControllerIndex = orderedViewControllers.firstIndex(of: viewController) else {
            return
        }
        
        let controller = self.orderedViewControllers[viewControllerIndex+1]
        
        setViewControllers([controller],
                           direction: .forward,
                           animated: true,
                           completion: nil)
    }
    
    //dsmiss all presented view controller to prevent bad view constraints
    func dismissAnyAlertControllerIfPresent() {
        guard let window :UIWindow = UIApplication.shared.keyWindow , var topVC = window.rootViewController?.presentedViewController else {return}
        while topVC.presentedViewController != nil  {
            topVC = topVC.presentedViewController!
        }
        if topVC.isKind(of: UIAlertController.self) {
            topVC.dismiss(animated: false, completion: nil)
        }
    }
    
    
}

extension ScanViewController{
    
    func configurePageControl() {
        // The total number of pages that are available is based on how many available colors we have.
        pageControl = UIPageControl(frame: CGRect(x: 0,y: UIScreen.main.bounds.maxY - 50,width: UIScreen.main.bounds.width,height: 50))
        self.pageControl.numberOfPages = orderedViewControllers.count
        self.pageControl.currentPage = 0
        self.pageControl.tintColor = UIColor.black
        self.pageControl.pageIndicatorTintColor = UIColor.white
        self.pageControl.currentPageIndicatorTintColor = UIColor.black
        self.view.addSubview(pageControl)
    }
    
    func newVc(viewController: String) -> UIViewController {
        return UIStoryboard(name: "Wizard", bundle: nil).instantiateViewController(withIdentifier: viewController)
    }
    
    // MARK: Delegate methords
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        let pageContentViewController = pageViewController.viewControllers![0]
        self.pageControl.currentPage = orderedViewControllers.firstIndex(of: pageContentViewController)!
    }
    
    // MARK: Data source functions.
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        guard let viewControllerIndex = orderedViewControllers.firstIndex(of: viewController) else {
            return nil
        }
        
        let previousIndex = viewControllerIndex - 1
        
        // User is on the first view controller and swiped left to loop to
        // the last view controller.
        guard previousIndex >= 0 else {
             return nil
        }
        
        guard orderedViewControllers.count > previousIndex else {
            return nil
        }
        
        //stop tutorial on third screen
        if(viewControllerIndex == 2){
            
            return nil
        }
        
        return orderedViewControllers[previousIndex]
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        guard let viewControllerIndex = orderedViewControllers.firstIndex(of: viewController) else {
            return nil
        }
        
        let nextIndex = viewControllerIndex + 1
        let orderedViewControllersCount = orderedViewControllers.count
        
        // User is on the last view controller and swiped right to loop to
        // the first view controller.
        
        //stop tutorial on third screen
        if(nextIndex == 3){
        
            SessionManager.sharedInstance.sensorSDK.startScan()
            
            return nil
        }
        
        
        guard orderedViewControllersCount != nextIndex else {
             return nil
        }
        
        guard orderedViewControllersCount > nextIndex else {
            return nil
        }
        

        
        return orderedViewControllers[nextIndex]
    }
}
