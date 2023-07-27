//
//  AwardInfoViewController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 03/12/21.
//

import UIKit
import ToastUI
import MaterialComponents.MaterialProgressView


class AwardInfoViewController: UIViewController, AwardInfoDelegate {
    let TAG = "AwardInfoViewController"

    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var progressBar: MDCProgressView!
    
    @IBOutlet weak var awardImage: UIImageView!
    @IBOutlet weak var awardTitle: UILabel!
    @IBOutlet weak var awardCategory: UILabel!
    @IBOutlet weak var awardValue: UILabel!
    @IBOutlet weak var awardDate: UILabel!
    @IBOutlet weak var awardDateLabel: UILabel!
    @IBOutlet weak var awardDescription: UILabel!
    @IBOutlet weak var awardType: UILabel!
    
    @IBOutlet weak var awardQrCodeView: UIStackView!
    @IBOutlet weak var awardQrCard: UIView!
    @IBOutlet weak var qrCodeImage: UIImageView!
    @IBOutlet weak var qrCodeValue: UILabel!
    
    @IBOutlet weak var awardState: UILabel!
    @IBOutlet weak var awardStateLabel: UILabel!
    
    @IBOutlet weak var awardGetMessage: UILabel!
    @IBOutlet weak var awardGetMessageLabel: UILabel!
    
    //DATE
    @IBOutlet weak var expireDateLabel: UILabel!
    @IBOutlet weak var expireDate: UILabel!
    @IBOutlet weak var useDateLabel: UILabel!
    @IBOutlet weak var useDate: UILabel!
    
    
    //SHOP
    @IBOutlet weak var shopTitleLabel: UILabel!
    @IBOutlet weak var shopImage: UIImageView!
    @IBOutlet weak var shopName: UILabel!
    
    //ARTICLE
    @IBOutlet weak var articleTitleLabel: UILabel!
    @IBOutlet weak var articleImage: UIImageView!
    @IBOutlet weak var articleName: UILabel!
    
    
    public var selectedAward: Award? = nil
    public var rankingAward: AwardRanking? = nil
    public var achievementAward: AwardAchievement? = nil
    
    //formatter
    let dateFormatterPrint = DateFormatter()
    
    //url check
    var awardDescriptionURLLink: String?

    var viewModel = AwardInfoViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        initView()
        viewModel.delegate = self
        viewModel.initAward(myAward: selectedAward, rankingAward: rankingAward, achievementAward: achievementAward)
        viewModel.loadData()
    }

    func onLoading() {
        scrollView.isHidden = true
        progressBar.progress = 0
        progressBar.mode = .indeterminate
        progressBar.trackTintColor = UIColor.systemRed.withAlphaComponent(0.2)
        progressBar.progressTintColor = UIColor.systemRed
        progressBar.startAnimating()
        progressBar.setHidden(false, animated: true)

        LogHelper.log(message: "Loading", withTag: TAG)
    }
    
    func onDataLoaded(detail: AwardDetailUI) {
        scrollView.isHidden = false
    
        progressBar.stopAnimating()
        progressBar.setHidden(true, animated: false)
               
            
        initAwardView(detail: detail)
        initCouponView(detail: detail)
    }
    
    func onError(message: String) {
        progressBar.isHidden = true
        scrollView.isHidden = false
        view.makeToast(message)
    }
    
    
    private func initView(){
        
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            overrideUserInterfaceStyle = .light
        }
        
        //init qr code card style
        awardQrCard.layer.cornerRadius = 16
        awardQrCard.layer.shadowColor = UIColor.gray.cgColor
        awardQrCard.layer.shadowOffset = CGSize(width: 0.0, height: 0.0)
        awardQrCard.layer.shadowRadius = 8.0
        awardQrCard.layer.shadowOpacity = 0.7
        
        //init formatter
        dateFormatterPrint.dateFormat = "dd MMM yyyy"

    }
    
    
    private func initAwardView(detail: AwardDetailUI){
        //load image
    
        let url = URL(string: detail.imageUrl ?? "")
        awardImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
        
        
        awardTitle.text = detail.title
        //awardCategory.text = self.rankingList.first(where: {$0.id == selectedAward?.rankingId})?.title ?? "Nessuna categoria"
        awardCategory.text = detail.header ?? ""

        awardType.isHidden = detail.valueLabel == nil || detail.valueLabel == ""
        awardType.text = detail.valueLabel ?? ""
        
        awardValue.text = detail.value ?? ""
        awardValue.isHidden = detail.value == nil || detail.value == ""
        
        awardDate.text = detail.emissionDate ?? ""
        awardDate.isHidden = detail.emissionDate == "" || detail.emissionDate == nil
        awardDateLabel.isHidden = awardDate.isHidden

        //description        
        awardDescription.text = detail.description
        awardDescriptionURLLink = findURL(str: detail.description)
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleDescriptionLinkTap(_:)))
        awardDescription.isUserInteractionEnabled = true
        awardDescription.addGestureRecognizer(tap)
        
        
    }
    
    @objc func handleDescriptionLinkTap(_ sender: UITapGestureRecognizer? = nil) {
        
        guard let url = awardDescriptionURLLink else {return}
    
        var customUrl = url
        
        if(!url.contains("http") && !url.contains("https")){
            customUrl = "https://" + url
        }
        
        guard let url = URL(string: customUrl) else { return }
        UIApplication.shared.open(url)
    }
    
    private func findURL(str: String) -> String?{
        
        //1. FIND URL with nsData detector
        if let URL = str.detectedFirstLink {
            changeURLColor(string: str, range: str.nsRange(from: str.range(of: URL)!))
            return URL
        }else{
            if let range = str.range(of: "link.") {
                
                let mySubstring = str[range.lowerBound..<str.endIndex]
                
                if let rangeDouble = mySubstring.range(of: "\n") {
                    
                    let customRange = range.lowerBound..<rangeDouble.lowerBound
                    let customURL = str[customRange]
                    
                    changeURLColor(string: str, range: str.nsRange(from: customRange))
                    
                    return String(customURL)
                    
                }

            }
        }


        return nil
    }
    
    private func changeURLColor(string: String, range: NSRange){
        
        let mutableAttributedString = NSMutableAttributedString.init(string: string)
        mutableAttributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.systemRed, range: range)
        
        awardDescription.attributedText = mutableAttributedString
    }
    
    
    private func initCouponView(detail: AwardDetailUI){
    
        awardQrCard.isHidden = !detail.hasCoupon
        if(detail.hasCoupon){
            bindQrCode(detail: detail)
            bindCouponState(detail: detail)
            bindCouponRefundType(detail: detail)
            bindCouponRefundDate(detail: detail)
            bindCouponExpireDate(detail: detail)
            bindCouponShop(detail: detail)
            bindCouponArticle(detail: detail)
        }
    }
    
    func bindQrCode(detail: AwardDetailUI){
        if  detail.qrCode != nil && detail.qrCode != ""{
            DispatchQueue.main.async {
                self.qrCodeImage.image = self.generateQRCode(from: detail.qrCode ?? "")
                self.qrCodeValue.text = detail.qrCode ?? ""
                self.qrCodeValue.isHidden = false
                self.qrCodeImage.isHidden = false
                self.awardQrCodeView.isHidden = false
            }
        }else{
            qrCodeValue.isHidden = true
            qrCodeImage.isHidden = true
            self.awardQrCodeView.isHidden = true

        }
    }
    
    func bindCouponState(detail: AwardDetailUI){
        if(detail.hasCoupon && detail.state != nil && detail.state != ""){
            awardState.isHidden = false
            awardStateLabel.isHidden = false
            awardState.text = detail.state
            awardState.textColor = detail.stateColor ?? UIColor.systemGray
        }else{
            awardState.isHidden = true
            awardStateLabel.isHidden = true

        }
    }
    
    func bindCouponRefundType(detail: AwardDetailUI){
        if(detail.hasCoupon && detail.refundType != nil && detail.refundType != ""){
            awardGetMessage.text = detail.refundType ?? ""
            awardGetMessage.isHidden = false
            awardGetMessageLabel.isHidden = false
        }else{
            awardGetMessage.isHidden = true
            awardGetMessageLabel.isHidden = true
        }
    }
    
    func bindCouponRefundDate(detail: AwardDetailUI){
        if(detail.refundDate != nil && detail.refundDate != ""){
            useDate.text = detail.refundDate ?? ""
            useDateLabel.text = detail.refundLabel ?? ""
            useDate.isHidden = false
            useDateLabel.isHidden = false
        }else{
            useDate.isHidden = true
            useDateLabel.isHidden = true
        }
    }
    
    func bindCouponExpireDate(detail: AwardDetailUI){
        if(detail.expiringDate != nil && detail.expiringDate != ""){
            expireDate.text = detail.expiringDate ?? ""
            expireDate.isHidden = false
            expireDateLabel.isHidden = false
        }else{
            expireDate.isHidden = true
            expireDateLabel.isHidden = true
        }
    }
    
    func bindCouponShop(detail: AwardDetailUI){
        if(detail.shopName != nil && detail.shopName != ""){
            shopName.isHidden = false
            shopTitleLabel.isHidden = false
            shopImage.isHidden = false
            shopTitleLabel.text = detail.shopLabel ?? ""
            shopName.text = detail.shopName ?? ""
            let url = URL(string: detail.shopImage ?? "")
            shopImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            shopImage.round()
        }else{
            shopName.isHidden = true
            shopTitleLabel.isHidden = true
            shopImage.isHidden = true
        }
    }
    
    func bindCouponArticle(detail: AwardDetailUI){
        if(detail.articleName != nil && detail.articleName != ""){
            articleName.isHidden = false
            articleTitleLabel.isHidden = false
            articleImage.isHidden = false
            shopTitleLabel.text = "Articolo riscattato"
            shopName.text = detail.articleName ?? ""
            let url = URL(string: detail.articleImage ?? "")
            articleImage.kf.setImage(with: url, placeholder: UIImage(named: "floatingButton"))
            articleImage.round()
        }else{
            articleName.isHidden = true
            articleTitleLabel.isHidden = true
            articleImage.isHidden = true
        }
    }
    
    private func generateQRCode(from string: String) -> UIImage? {
        let data = string.data(using: String.Encoding.ascii)

        if let filter = CIFilter(name: "CIQRCodeGenerator") {
            filter.setValue(data, forKey: "inputMessage")
            let transform = CGAffineTransform(scaleX: 3, y: 3)

            if let output = filter.outputImage?.transformed(by: transform) {
                return UIImage(ciImage: output)
            }
        }

        return nil
    }
    
    
}
