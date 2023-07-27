//
//  Measurement.swift
//  LismoveSensorSkdTestApp
//
//


import Foundation
import CoreBluetooth

// CSC Measurement
// https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.csc_measurement.xml
//
//  Flags : 1 byte.  Bit 0: Wheel. Bit 1: Crank
//  Cumulative Wheel revolutions: 4 bytes uint32
//  Last wheel event time: 2 bytes. uint16 (1/1024s)
//  Cumulative Crank revolutions: 2 bytes uint16
//  Last cranck event time: 2 bytes. uint16 (1/1024s)


struct Measurement : CustomDebugStringConvertible {
    
    let hasWheel:Bool
    let hasCrank:Bool
    let cumulativeWheel:UInt32
    let lastWheelEventTime:TimeInterval
    let cumulativeCrank:UInt16
    let lastCrankEventTime:TimeInterval
    let wheelSize:UInt32
    
    
    init(data:Data, wheelSize:UInt32) {
        
        self.wheelSize = wheelSize
        // Flags
        var flags:UInt8=0
        (data as NSData).getBytes(&flags, range: NSRange(location: 0, length: 1))
        
        hasWheel = ((flags & BTConstants.WheelFlagMask) > 0)
        hasCrank = ((flags & BTConstants.CrankFlagMask) > 0)
        
        var wheel:UInt32=0
        var wheelTime:UInt16=0
        var crank:UInt16=0
        var crankTime:UInt16=0
        
        var currentOffset = 1
        var length = 0
        
        if ( hasWheel ) {
            
            length = MemoryLayout<UInt32>.size
            (data as NSData).getBytes(&wheel, range: NSRange(location: currentOffset, length: length))
            currentOffset += length
            
            length = MemoryLayout<UInt16>.size
            (data as NSData).getBytes(&wheelTime, range: NSRange(location: currentOffset, length: length))
            currentOffset += length
        }
        
        if ( hasCrank ) {
            
            length = MemoryLayout<UInt16>.size
            (data as NSData).getBytes(&crank, range: NSRange(location: currentOffset, length: length))
            currentOffset += length
            
            length = MemoryLayout<UInt16>.size
            (data as NSData).getBytes(&crankTime, range: NSRange(location: currentOffset, length: length))
            currentOffset += length
        }
        
        cumulativeWheel     = CFSwapInt32LittleToHost(wheel)
        lastWheelEventTime  = TimeInterval( Double(CFSwapInt16LittleToHost(wheelTime))/BTConstants.TimeScale)
        
        cumulativeCrank     = CFSwapInt16LittleToHost(crank)
        lastCrankEventTime  = TimeInterval( Double(CFSwapInt16LittleToHost(crankTime))/BTConstants.TimeScale)
        
    }
    
    func timeIntervalForCurrentSample( _ current:TimeInterval, previous:TimeInterval ) -> TimeInterval {
        var timeDiff:TimeInterval = 0
        if( current >= previous ) {
            timeDiff = current - previous
        }
        else {
            // passed the maximum value
            timeDiff =  ( TimeInterval((Double( UINT16_MAX) / BTConstants.TimeScale)) - previous) + current
        }
        return timeDiff
        
    }
    
    func valueDiffForCurrentSample<T:UnsignedInteger>( _ current:T, previous:T , max:T) -> T {
        
        var diff:T = 0
        if  ( current >= previous ) {
            diff = current - previous
        }
        else {
            diff = ( max - previous ) + current
        }
        return diff
    }
    
    
    func valuesForPreviousMeasurement( _ previousSample:Measurement? ) -> ( cadenceinRPM:Double?, distanceinMeters:Double?, speedInMetersPerSecond:Double?, wheelDiff: UInt32?, wheelTimeDiff: Double?)? {
        
        
        var distance:Double?, cadence:Double?, speed:Double?
        guard let previousSample = previousSample else {
            return nil
        }
        if ( hasWheel && previousSample.hasWheel ) {
            let wheelTimeDiff = timeIntervalForCurrentSample(lastWheelEventTime, previous: previousSample.lastWheelEventTime)
            var valueDiff = valueDiffForCurrentSample(cumulativeWheel, previous: previousSample.cumulativeWheel, max: UInt32.max)
            
            
            //handling safe valueDiff
            if(valueDiff > 50 && valueDiff <= 100){
                
                /*
                //warning
                let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                            reason:"Value Diff: \(valueDiff)",
                                            userInfo:nil)
            
                Bugsnag.notify(exception)*/
                
            }else if(valueDiff > 100){
                
                /*
                //warning
                let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                            reason:"Value Diff to high: \(valueDiff)",
                                            userInfo:nil)
            
                Bugsnag.notify(exception)*/
                
                
                valueDiff = 0
            }
            
            
            distance = Double( valueDiff * wheelSize) * Double.pi / 1000.0 // distance in meters
            if  distance != nil  &&  wheelTimeDiff > 0 {
                speed = (wheelTimeDiff == 0 ) ? 0 : distance! / wheelTimeDiff // m/s
            }
        
            
            print( "Cadence: \(String(describing: cadence)) RPM. Distance: \(String(describing: distance)) meters. Speed: \(String(describing: speed)) Km/h " )
            return ( cadenceinRPM:nil, distanceinMeters:distance, speedInMetersPerSecond:speed, wheelDiff: valueDiff, wheelTimeDiff:Double(wheelTimeDiff) )
        
        
        
        }else{
            return nil
        }
        
        
    }
    
    var debugDescription:String {
        get {
            return "Wheel Revs: \(cumulativeWheel). Last wheel event time: \(lastWheelEventTime). Crank Revs: \(cumulativeCrank). Last Crank event time: \(lastCrankEventTime)"
        }
    }
    
}
