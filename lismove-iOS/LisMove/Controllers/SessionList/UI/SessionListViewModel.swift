//
//  SessionListViewModel.swift
//  LisMove
//
//

import Foundation
class SessionListViewModel{
    var isLoading: Bool = false
    var sessionType = SessionType.all
    
    var sessions: [Session]?
    var sessionManager = SessionManager.sharedInstance
    var networkinManager = NetworkingManager.sharedInstance
    var DB = DBManager.sharedInstance
    var delegate: SessionListDelegate? = nil
    
    func setSessionType(type: SessionType){
        self.sessionType = type
    }
    
    func refreshSessions(start: Date, end: Date){
        self.isLoading = true
        self.delegate?.onReloadTable()
        
        let user = self.DB.getCurrentUser()
        
        //download all user session
        let dateFormatterGet = DateFormatter()
        dateFormatterGet.dateFormat = "yyyy-MM-dd"
        
        networkinManager.getUserSession(uuid: user!.uid!, start: dateFormatterGet.string(from: start), end: dateFormatterGet.string(from: end), completion: {result in
            self.isLoading = false
            
            switch result {
                case .success(let data):
                    
                    self.sessions = data.sorted {
                        Date(timeIntervalSince1970: TimeInterval(($0.startTime.value ?? Date.distantPast.millisecondsSince1970) / 1000) ) > Date(timeIntervalSince1970: TimeInterval(($1.endTime.value ?? Date.distantPast.millisecondsSince1970) / 1000) )
                    }
                    
                    
                    //filter session by type
                    if(self.sessionType == SessionType.work){
                        self.sessions = self.sessions?.filter{$0.homeWorkPath == true}
                    }

                    self.delegate?.onReloadTable()
                
                    
                case .failure(let error):
                    //MARK: ERROR STREAM
                    NotificationCenter.default.post(name: Notification.Name("ERROR_STREAM"), object: nil, userInfo: ["error": error.localizedDescription])
                    
            }
        })
    }
}

protocol SessionListDelegate{
    func onReloadTable()
    func onFilterDateComplete(startDate: Date, endDate: Date)
}
