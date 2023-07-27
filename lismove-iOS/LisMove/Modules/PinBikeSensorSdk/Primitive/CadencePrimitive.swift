//
//  CadencePrimitive.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 25/11/21.
//

import Foundation


public struct CadencePrimitive{

    
    public var wheelRevs: UInt32
    public var wheelTimestamp: TimeInterval

    
    
    public init(data: Data){
        
        // Flags
        var flags:UInt8=0
        (data as NSData).getBytes(&flags, range: NSRange(location: 0, length: 1))

        
        var wheel:UInt32=0
        var wheelTime:UInt16=0
        
        var currentOffset = 1
        var length = 0
        
        length = MemoryLayout<UInt32>.size
        (data as NSData).getBytes(&wheel, range: NSRange(location: currentOffset, length: length))
        currentOffset += length
        
        length = MemoryLayout<UInt16>.size
        (data as NSData).getBytes(&wheelTime, range: NSRange(location: currentOffset, length: length))
        currentOffset += length
        
        
        wheelRevs     = CFSwapInt32LittleToHost(wheel)
        wheelTimestamp  = TimeInterval( Double(CFSwapInt16LittleToHost(wheelTime))/BTConstants.TimeScale)
        
        
        
    }
    
}
