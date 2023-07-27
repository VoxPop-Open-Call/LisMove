//
//  SensorDataManager.swift
//  LisMoveSensorSdk
//
//  Created by Francesco Paolo Dellaquila on 25/11/21.
//

import Foundation
import LisMoveSensorSdk
import Bugsnag
import SwiftLocation
import CoreLocation
import SwiftLog


protocol SensorDataManagerProtocol {
    
    func sessionBecameActive()
    func sessionBecamePaused()
    //func showAutomaticPauseTimer()
    //func dismissAutomaticPauseTimer()

}


class SensorDataManager{
    public var delegate: SensorDataManagerProtocol?
    
    
    var wheelCircunferance: Double = Double(BTConstants.DefaultWheelSize) * Double.pi
    var hubCoefficient: Double = 1.0
    
    private let MAX_SPEED_LIMIT_IN_KMH = 80.0 // max speed
    private let MIN_SPEED_LIMIT_IN_KMH = 5.0 // min speed for gps

    private var lastMeasurementSpeed = [Double](repeating: -1.0, count: 4)
    private var lastMeasurementSpeedIndex = 0
    private var speedInKmH: Double = 0.0

    var totalGyroDistanceInKm: Double = 0.0
    var totalGpsOnlyDistanceInKm: Double = 0.0
    // Cache for gps distance
    // If switch to gyro, this value is added in gyroDistance
    // If session pauses/closes in GPS, this value is added in gpsOnlyDistance
    var totalGpsCacheDistanceInKm: Double = 0.0


    //MARK: Sensor Data
    var currentSensorBattery = 0
    var startSensorBattery = 0
    var currentSensorFirmware = ""
    

    var sessionElapsedTimeInSec = 0

    var previousCadenceEntity: CadencePrimitive? = nil
    var previousGpsEntity: GPSPrimitive? = nil
    
    // private var lastGpsSessionDistanceInKm: Double = 0.0
    private var lastGpsSessionTimeSeconds: Int64? = nil
    
    var secondsTillPause: Int? = nil

    var isPauseForced = false
    var isSessionActive = false {
        didSet {
            if isSessionActive != oldValue {
                isSessionActive ? delegate?.sessionBecameActive() : delegate?.sessionBecamePaused()
            }
        }
    }

    // Don't pause again after a forced resume
    private var hasResumedShortly = false

    var lastSavedGyroDistance = 0.0
    
    
    //lat gps data
    var lastLat = SwiftLocation.lastKnownGPSLocation?.coordinate.latitude ?? 0.0
    var lastLng = SwiftLocation.lastKnownGPSLocation?.coordinate.longitude ?? 0.0
    var lastAltitude = 0.0
    var lastTimestamp = Date()
    
    //pause timer
    var pauseIndex = 0
    var pauseTimer: Timer?
    
    //MARK: AUTOMATIC PAUSE
    static let secondsToPause = 6
    var speedMeasurement = [Double](repeating: -1.0, count: secondsToPause)
    
    
    var TAG = "sensorDataManager"
    
    
    init(){
        
        loadSensorData()
        initCheckSessionActiveLoop()
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(batteryLevelReceived), name: NSNotification.Name(rawValue: "BATTERY_READ"), object: nil)
    }
    
    deinit{
        NotificationCenter.default.removeObserver(self)
    }
    
    func getDebugDump() -> String{
        return "totalGyroDistanceInKm: \(totalGyroDistanceInKm) totalGpsOnlyDistanceInKm: \(totalGpsOnlyDistanceInKm) totalGpsCacheDistanceInKm:\(totalGpsCacheDistanceInKm) currentSensorFirmware: \(currentSensorFirmware)"
    }
    
    private func loadSensorData(){
        hubCoefficient = DBManager.sharedInstance.getLismoveDevice()?.hubCoefficient ?? 1.0
        LogHelper.log(message: "hubCoefficient is \(hubCoefficient)", withTag: TAG)
        let check = UserDefaults.standard.object(forKey: "wheelDiameter")
        
        if(check != nil){
            
            self.wheelCircunferance =  (Double(UserDefaults.standard.integer(forKey: "wheelDiameter"))) * Double.pi
                                        
        }else{
                
            self.wheelCircunferance = Double(BTConstants.DefaultWheelSize) * Double.pi
        }
 
    }
    
    
    
    func initCheckSessionActiveLoop(){
            
        //1. check session pause -> 1s
        if(self.pauseTimer == nil){
            self.pauseTimer = Timer(timeInterval: 1.0, target: self, selector: #selector(checkPauseState), userInfo: nil, repeats: true)
            RunLoop.current.add(self.pauseTimer!, forMode: .common)
        }
        
    }
    
    
    
    
    public func resumeFromPause(){
        LogHelper.log(message: "resumeFromPause", withTag: TAG)
        isPauseForced = false
        
        isSessionActive = true
        hasResumedShortly = true
    }
    
    
    
    public func forcePause(){
        LogHelper.log(message: "forcePause", withTag: TAG)
        isPauseForced = true
    }
    
    
    
    @objc func checkPauseState(){
        LogHelper.log(message: "checkPauseState")
        if (!isPauseForced) {
             if (previousGpsEntity != nil) {
                 LogHelper.log(message: "previousGpsEntity")
                 // gps can't be automatically paused
                 sessionElapsedTimeInSec += 1

                 // hasResumedShortly = true
                 isSessionActive = true
     
             } else {
                 speedMeasurement[pauseIndex] = speedInKmH
                 handlePauseAlert()
                 pauseIndex = (pauseIndex + 1) % SensorDataManager.secondsToPause
                 // check if stopped for secondsToPause secs
                 if (speedMeasurement.filter { $0 == 0.0 }.count == speedMeasurement.count) {
                     if (!hasResumedShortly) {
                         
                         LogHelper.log(message: "SESSION INACTIVE", withTag: TAG)
                         isSessionActive = false
       
                     } else {
                         
                         // Resume was forced, even if velocity is 0, resume session
                         LogHelper.log(message: "EMITTED ACTIVE", withTag: TAG)
                         sessionElapsedTimeInSec += 1
                         isSessionActive = true
                    
                     }
                     // Automatically resume session only after two partials != 0
                 } else if (speedMeasurement.filter { $0 != 0.0}.count > 2) {

                     LogHelper.log(message: "EMITTED ACTIVE", withTag: TAG)
                     sessionElapsedTimeInSec += 1
                     isSessionActive = true
             
                     hasResumedShortly = false
                 }else {
                     if(isSessionActive){
                         sessionElapsedTimeInSec += 1
                     }
                 }
             }

         } else {
             isSessionActive = false
         }
    }
    
    private func handlePauseAlert(){
        LogHelper.log(message: "HANDLE", withTag: "PAUSE ALERT")

        guard isSessionActive else {
            secondsTillPause = nil
            return
        }
        if(speedMeasurement[pauseIndex] != 0.0){
            secondsTillPause = nil
            LogHelper.log(message: "NIL, \(speedMeasurement.description)", withTag: "PAUSE ALERT")

        }else{
            var consecutivesZeros = 0
            for i in 0..<speedMeasurement.count{
                var index = pauseIndex - i
                if(index < 0){
                    index = speedMeasurement.count + index
                }
                if(speedMeasurement[index] == 0.0){
                    consecutivesZeros += 1
                }else{
                    break
                }
            }
            LogHelper.log(message: "Show Pause alert with \(consecutivesZeros), index \(pauseIndex)", withTag: "PAUSE ALERT")
            LogHelper.log(message: "\(speedMeasurement.description)", withTag: "PAUSE ALERT")

            var countDown = SensorDataManager.secondsToPause - consecutivesZeros
            secondsTillPause = countDown
        }
        
    }
    
    
    public func getPartial(primitive: CadencePrimitive) -> Partial{
        LogHelper.logError(message: "gpsCache \(totalGpsCacheDistanceInKm)", withTag: TAG)
        LogHelper.logError(message: "gpsOnlyDistance \(totalGpsOnlyDistanceInKm)", withTag: TAG)
        LogHelper.logError(message: "gyroDistance \(totalGyroDistanceInKm)", withTag: TAG)
        
        let currentSample = primitive
        //TODO: Check with @Paolo for automatic pause
        previousGpsEntity = nil

        // if coming from GPS session, override GPS distance data with sensor one (except if started in gps)
        if (totalGpsCacheDistanceInKm != 0.0) {
            // Was started in GPS
            if (previousCadenceEntity == nil) {
                totalGpsOnlyDistanceInKm = totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm
            } else {
                // should recover distance from gyro data
            }

            totalGpsCacheDistanceInKm = 0.0
            previousGpsEntity = nil
        }

        if (previousCadenceEntity == nil) {

            // is first sample
            previousCadenceEntity = currentSample

            return getEmptyPartial()
            
        } else {
            
            let wheelTimeDiff = timeIntervalForCurrentSample(currentSample.wheelTimestamp, previous: previousCadenceEntity!.wheelTimestamp)
            var wheelDiff = valueDiffForCurrentSample(currentSample.wheelRevs, previous: previousCadenceEntity!.wheelRevs, max: UInt32.max)

            // Wheel has exceeded limit
            if (wheelDiff > 50) {
                
                let message = "Found a big wheelDiff value (\(wheelDiff)). Current wheel=\(currentSample.wheelRevs), previous wheel=\(previousCadenceEntity?.wheelRevs ?? 0)"
                
                
                //warning
                let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                            reason:message,
                                            userInfo:nil)
            
                Bugsnag.notify(exception)
            }

            if (wheelDiff > 100) {
                // Limit wheel diff values
                let message = "Found a big wheelDiff value (\(wheelDiff)). Value was ignored. Current wheel=\(currentSample.wheelRevs), previous wheel=\(previousCadenceEntity?.wheelRevs ?? 0)"
                
                
                
                //warning
                let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                            reason:message,
                                            userInfo:nil)
            
                Bugsnag.notify(exception)
                
                
                
                wheelDiff = 0
            }

            let sampleDistanceInMeters = SensorDataManager.computeDistanceInMeters(wheelDiff: wheelDiff, wheelCircunferance: wheelCircunferance, hubCoefficient: hubCoefficient)
            
            
            
            if  wheelTimeDiff > 0 {
                speedInKmH = ((wheelTimeDiff == 0 ) ? 0 : sampleDistanceInMeters / wheelTimeDiff ) * 3.6
            }else{
                speedInKmH = 0.0
            }


            if (isSessionActive) {
                totalGyroDistanceInKm += (sampleDistanceInMeters / 1000)
            }

            if (totalGyroDistanceInKm < 0.0) { totalGyroDistanceInKm = 0.0 }

            lastSavedGyroDistance = totalGyroDistanceInKm
            // Set this as previous sample
            previousCadenceEntity = currentSample

            //Avg for speed
            lastMeasurementSpeed[lastMeasurementSpeedIndex] = speedInKmH
            lastMeasurementSpeedIndex = (lastMeasurementSpeedIndex + 1) % 4
            var lastSpeedSum = 0.0
            var lastSpeedCount = 0

            lastMeasurementSpeed.forEach { speed in
                if (speed != -1.0) {
                    lastSpeedSum += speed
                    lastSpeedCount += 1
                }
            }
            
            var computedSpeed = 0.0
            if (lastSpeedCount != 0) {
                computedSpeed = lastSpeedSum / Double(lastSpeedCount)
            }
            
            

            if (computedSpeed >= MAX_SPEED_LIMIT_IN_KMH) {
                // ignore partial if exceeded max speed
                let message = "GYRO Speed exceeded max speed (got $computedSpeed, max is \(MAX_SPEED_LIMIT_IN_KMH) km/h)"
                
                
                
                //warning
                let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                            reason:message,
                                            userInfo:nil)
            
                Bugsnag.notify(exception)
            
                
                
                return getEmptyPartial()
            }

            let avgSpeed = getAverageSpeed()


            return Partial(
                uuid: UUID().uuidString,
                timestamp: Date().millisecondsSince1970,
                altitude: self.lastAltitude,
                latitude: self.lastLat,
                longitude: self.lastLng,
                type: Partial.PartialType.Unknown.rawValue,
                deltaRevs: Int64(wheelDiff),
                gyroDeltaDistance: sampleDistanceInMeters / 1000,
                gyroDistance: totalGyroDistanceInKm,
                wheelTime: 0,
                speed: computedSpeed,
                gpsDistance: totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm,
                elapsedTimeInMillis: (Int64(sessionElapsedTimeInSec) * 1000),
                averageSpeed: avgSpeed,
                isGpsPartial: false,
                batteryLevel: currentSensorBattery,
                urban: SessionManager.sharedInstance.pointManager?.isPositionInUrbanPolygon(lat: self.lastLat, lng: self.lastLng) ?? false,
                rawData_wheel: Int64(currentSample.wheelRevs),
                rawData_ts: currentSample.wheelTimestamp.milliseconds
            )
        
        }
        
    }
    
    
    
    public func getPartial(currentGpsEntity: GPSPrimitive?) -> Partial?{
        LogHelper.logError(message: "gps, gpsCache \(totalGpsCacheDistanceInKm)", withTag: TAG)
        LogHelper.logError(message: "gps,gpsOnlyDistance \(totalGpsOnlyDistanceInKm)", withTag: TAG)
        LogHelper.logError(message: "gps, gyroDistance \(totalGyroDistanceInKm)", withTag: TAG)
        LogHelper.logError(message: "gpsEntity is null?\(currentGpsEntity == nil)", withTag: TAG)
        
        if (!isSessionActive) { return nil }
        if (currentGpsEntity == nil) { return nil }

        
        if (previousGpsEntity == nil) {
            previousGpsEntity = currentGpsEntity
            lastGpsSessionTimeSeconds = Date().millisecondsSince1970 / 1000
            
            return nil
        }

        let currentGpsSessionTimeSeconds = Date().millisecondsSince1970 / 1000
        
        
        

        //calculate gps distance
        let previousCoordinate = CLLocation(latitude: self.previousGpsEntity!.lat, longitude: self.previousGpsEntity!.lng)
        let newCoordinate = CLLocation(latitude: currentGpsEntity!.lat, longitude: currentGpsEntity!.lng)
        let calculatedDistanceInKm = ( previousCoordinate.distance(from: newCoordinate) / 1000 )
        
        logw("PREVIOUS COORDINATE -> lat: \(previousCoordinate.coordinate.latitude)  lng: \(previousCoordinate.coordinate.longitude)")
        logw("NEW COORDINATE -> lat: \(newCoordinate.coordinate.latitude)  lng: \(newCoordinate.coordinate.longitude)")
        logw("DISTANCE -> distanceInKM: \(calculatedDistanceInKm)")
        
        
        
        var computedSpeedInKmH = 0.0
        if (lastGpsSessionTimeSeconds != nil) {
            computedSpeedInKmH = ((calculatedDistanceInKm / Double(currentGpsSessionTimeSeconds - (lastGpsSessionTimeSeconds ?? 0))) * 3600)
        }
        
        // update location, even if outlier (will be cleaned with correct position after a while)
        previousGpsEntity = currentGpsEntity

        if (computedSpeedInKmH >= MAX_SPEED_LIMIT_IN_KMH) {
            LogHelper.log(message: "computedSpeedInKmH >= MAX_SPEED_LIMIT_IN_KMH", withTag: TAG)
            let message = "GPS Speed exceeded max speed (got \(computedSpeedInKmH), max is \(MAX_SPEED_LIMIT_IN_KMH) km/h)"

            
            //warning
            let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                        reason:message,
                                        userInfo:nil)
        
            Bugsnag.notify(exception)
            
            

            // Discard partial and don't update average speed and total km
            return getEmptyPartial()
        }

        /*
        if (computedSpeedInKmH <= MIN_SPEED_LIMIT_IN_KMH && computedSpeedInKmH != 0.0) {
            LogHelper.log(message: "computedSpeedInKmH <= MIN_SPEED_LIMIT_IN_KMH && computedSpeedInKmH != 0.0", withTag: TAG)
            let message = "GPS Speed exceeded max speed (got \(computedSpeedInKmH), max is \(MAX_SPEED_LIMIT_IN_KMH) km/h)"
            
            
            //warning
            let exception = NSException(name:NSExceptionName(rawValue: "Session Partial Exception"),
                                        reason:message,
                                        userInfo:nil)
        
            Bugsnag.notify(exception)

            return getEmptyPartial()
        }*/

        if(isSessionActive){
            totalGpsCacheDistanceInKm += calculatedDistanceInKm
        }

        let avgSpeed = getAverageSpeed()

        // don't update time if partial was discarded. This is because if not,
        // the intervals would be always of 1 secs (and gps can jump a lot at a more slower rate)

        if (computedSpeedInKmH != 0.0) {
            lastGpsSessionTimeSeconds = currentGpsSessionTimeSeconds
        }

        return Partial(
            uuid: UUID().uuidString,
            timestamp: Date().millisecondsSince1970,
            altitude: currentGpsEntity!.altitude,
            latitude: currentGpsEntity!.lat,
            longitude: currentGpsEntity!.lng,
            type: Partial.PartialType.Unknown.rawValue,
            deltaRevs: nil,
            gyroDeltaDistance: nil,
            gyroDistance: (totalGyroDistanceInKm >= 0 ) ? totalGyroDistanceInKm : 0.0,
            wheelTime: 0,
            speed: computedSpeedInKmH,
            gpsDistance: totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm,
            elapsedTimeInMillis: (Int64(sessionElapsedTimeInSec) * 1000),
            averageSpeed: avgSpeed,
            isGpsPartial: true,
            batteryLevel: currentSensorBattery,
            urban: SessionManager.sharedInstance.pointManager?.isPositionInUrbanPolygon(lat: currentGpsEntity!.lat, lng: currentGpsEntity!.lng) ?? false
        )
        
        
    }

    static func computeDistanceInMeters(wheelDiff: UInt32, wheelCircunferance: Double, hubCoefficient: Double)-> Double{
        return Double( wheelDiff ) * wheelCircunferance / 1000.0 * hubCoefficient
    }
    
    
}



extension SensorDataManager {
    
    
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
    
    
    
    public func getTotalDistance() -> Double{
        return totalGpsOnlyDistanceInKm + totalGyroDistanceInKm + totalGpsCacheDistanceInKm
    }
    
    private func getAverageSpeed() -> Double{
        if(sessionElapsedTimeInSec != 0){
            return getTotalDistance() / Double((sessionElapsedTimeInSec * 1000)) * 1000000 * 3.6
        }else{
            return 0.0
        }
    }
    
    
    public func emptyCache() {
        // Check if there's data in gps cache
        if (totalGpsCacheDistanceInKm != 0.0) {
            totalGpsOnlyDistanceInKm += totalGpsCacheDistanceInKm
            totalGpsCacheDistanceInKm = 0.0
        }

        previousCadenceEntity = nil
        previousGpsEntity = nil
    }
    
    public func getEmptyPartial() -> Partial{
        return Partial(
            uuid: UUID().uuidString,
            timestamp: Date().millisecondsSince1970,
            altitude: 0.0,
            latitude: 0.0,
            longitude: 0.0,
            type: Partial.PartialType.Unknown.rawValue,
            deltaRevs: 0,
            gyroDeltaDistance: 0.0,
            gyroDistance: totalGyroDistanceInKm,
            wheelTime: 0,
            speed: 0.0,
            gpsDistance: 0.0,
            elapsedTimeInMillis: (Int64(sessionElapsedTimeInSec) * 1000),
            averageSpeed: getAverageSpeed(),
            isGpsPartial: true,
            batteryLevel: currentSensorBattery,
            urban: false)
    }
    
}


extension SensorDataManager{
    

    /*
     1) save first value of sensor battery
     */
    @objc func batteryLevelReceived(_ notification: Notification){
        let check = notification.userInfo as! [String: Any]
        let batteryLevel = check["battery"]! as! Int
        let sensorInterface = check["sensor"]! as! CadenceSensor
        if(startSensorBattery == 0){
            self.startSensorBattery = batteryLevel
        }
        self.currentSensorBattery = batteryLevel
        
    }
    
    
    


   
}
