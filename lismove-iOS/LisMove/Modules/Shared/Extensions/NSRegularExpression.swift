//
//  NSRegularExpression.swift
//  LisMove
//
//

import Foundation
extension NSRegularExpression{
    func matches(_ string: String) -> Bool {
           let range = NSRange(location: 0, length: string.utf16.count)
           return firstMatch(in: string, options: [], range: range) != nil
       }
}

extension String {
    var detectedLinks: [String] { DataDetector.find(all: .link, in: self) }
    var detectedFirstLink: String? { DataDetector.first(type: .link, in: self) }
    var detectedURLs: [URL] { detectedLinks.compactMap { URL(string: $0) } }
    var detectedFirstURL: URL? {
        guard let urlString = detectedFirstLink else { return nil }
        return URL(string: urlString)
    }
    
    func nsRange(from range: Range<String.Index>) -> NSRange {
        let from = range.lowerBound.samePosition(in: utf16)
        let to = range.upperBound.samePosition(in: utf16)
        return NSRange(location: utf16.distance(from: utf16.startIndex, to: from!),
                       length: utf16.distance(from: from!, to: to!))
    }
}


extension StringProtocol {
    func index<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> Index? {
        range(of: string, options: options)?.lowerBound
    }
    func endIndex<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> Index? {
        range(of: string, options: options)?.upperBound
    }
    func indices<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> [Index] {
        ranges(of: string, options: options).map(\.lowerBound)
    }
    func ranges<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> [Range<Index>] {
        var result: [Range<Index>] = []
        var startIndex = self.startIndex
        while startIndex < endIndex,
            let range = self[startIndex...]
                .range(of: string, options: options) {
                result.append(range)
                startIndex = range.lowerBound < range.upperBound ? range.upperBound :
                    index(range.lowerBound, offsetBy: 1, limitedBy: endIndex) ?? endIndex
        }
        return result
    }
}
