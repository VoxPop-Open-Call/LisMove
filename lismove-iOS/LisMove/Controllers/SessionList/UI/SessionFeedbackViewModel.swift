//
//  SessionFeedbackViewModel.swift
//  LisMove
//
//

import Foundation
import Resolver
import RxSwift
class SessionFeedbackViewModel{
    var delegate: SessionFeedbackDelegate?
    private var sessionId: String = ""
    var onlyPoints: Bool = false
    
    @Injected
    var repository: SessionRepository
    var options = [FeedBackFormOption]()
    
    func initViewModel(withSessionId sessionId: String,
                       isOnlyPoints: Bool,
                       withDelegate delegate: SessionFeedbackDelegate){
        self.sessionId = sessionId
        self.delegate = delegate
        self.onlyPoints = isOnlyPoints
        if(!isOnlyPoints){
            loadFeedBackOptions()
        }
        
    }
    
    func sendRequest(selectedOptions: [Int], notes: String){
        delegate?.onDialogLoading()
        LogHelper.log(message: "options: \(selectedOptions.description) \n notes: \(notes)")
        if(onlyPoints){
            repository.getPointFeedbackFormId(onCompleted: { type in
                self.repository.requestSessionVerification(sessionId: self.sessionId, reason: notes, types: [type], onCompleted: {
                    result in
                    self.manageRequestResult(result: result)
                })
            })
        }else{
            repository.requestSessionVerification(sessionId: sessionId, reason: notes, types: selectedOptions, onCompleted: {
                result in
                self.manageRequestResult(result: result)
            })
        }
    }
    
    

    private func manageRequestResult(result: Result<Session, Error>){
        switch result{
        case .success(let session):
            delegate?.onSessionSent(session: session)
            break
        case .failure(let error):
            delegate?.onError(message: error.localizedDescription)
            break
        }
    }
    private func loadFeedBackOptions(){
        delegate?.onLoading()
        repository.getFeedbackFormOptions(onCompleted: {result in
            self.manageFeedbackResult(result)
        })
    }
    
    private func manageFeedbackResult(_ result: Result<[FeedBackFormOption], Error>){
            switch result{
            case .success(let options):
                onOptionsReceived(options)
                break
            case .failure(let error):
                onOptionReceivedError(error)
                break
            }
    }
    
    private func onOptionsReceived(_ data: [FeedBackFormOption]){
        options = data
        delegate?.onFeedbackReceived()
    }
    
    private func onOptionReceivedError(_ data: Error){
        delegate?.onError(message: data.localizedDescription)
    }
}

protocol SessionFeedbackDelegate{
    func onLoading()
    func onDialogLoading()
    func onError(message: String)
    func onFeedbackReceived()
    func onSessionSent(session: Session)
}
