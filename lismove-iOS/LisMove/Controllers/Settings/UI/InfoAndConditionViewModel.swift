//
//  InfoAndConditionViewModel.swift
//  LisMove
//
//

import Foundation
import Resolver

class InfoAndConditionViewModel{
    let TAG = "InfoAndConditionViewModel"
    
    @Injected
    var settingsRepository: SettingsRepository
    
    var settings: LisMoveSettings? = nil
    var delegate: InfoAndConditionDelegate? = nil
    
    func loadSetings(){
        settingsRepository.getSettings(onCompleted: {result in
            switch result {
            case .success(let settings):
                self.settings = settings
                if let banner = settings.infoBanner{
                    self.delegate?.onBannerReceived(url: banner)
                }
            case .failure(let error):
                LogHelper.logError(message: error.localizedDescription, withTag: self.TAG)
            }
        })
    }
    
}

protocol InfoAndConditionDelegate{
    func onBannerReceived(url: String)
}
