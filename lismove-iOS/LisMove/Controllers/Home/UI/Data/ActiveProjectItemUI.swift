//
//  ActiveInitiativeItemUI.swift
//  LisMove
//
//

import Foundation
struct ActiveProjectItemUI {
    var image: String
    let regulation: String
    let organizationName: String
    var regulationLink: URL? {
        var url: URL? = nil
        if(!regulation.isEmpty){
            let types: NSTextCheckingResult.CheckingType = [.link]
            let detector = try? NSDataDetector(types: types.rawValue)
            let range = NSRange(regulation.startIndex..<regulation.endIndex, in: regulation)
            detector?.enumerateMatches(in: regulation, options: [], range: range){
                (match, flags, _) in
                    guard let match = match else {
                        return
                    }

                    switch match.resultType {
                    case .link:
                        url = match.url
                        return
                    default:
                        return
                    }
                }
        }
        
        return url
    }
        
}
