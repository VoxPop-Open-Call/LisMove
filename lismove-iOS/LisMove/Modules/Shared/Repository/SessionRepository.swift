//
//  SessionRepository.swift
//  LisMove
//
//

import Foundation
import LisMoveSensorSdk

protocol SessionRepository{
    func getFeedbackFormOptions(onCompleted listener:@escaping (Result<[FeedBackFormOption], Error>) -> () )
    func getPointFeedbackFormId(onCompleted listener: @escaping (Int)->())
    func requestSessionVerification(
        sessionId: String,
        reason: String,
        types: [Int],
        onCompleted listener:@escaping (Result<Session, Error>) -> ()
    )
    func computeDistanceAndSendOfflineSession(sessions: [LisMoveSensorHistoryElement]) throws
}
