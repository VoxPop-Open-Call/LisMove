//
//  LisMoveSessionManager.swift
//  LisMoveSensorSdk
//
//
/*
import Foundation
import CoreBluetooth

class LisMoveSessionManager: CadenceSensorDelegate{
    func sensorUpdatedPrimitive(primitive: CadencePrimitive) {
        
    }
    
        
    var accumulatedDistance: Double?
    var accumulatedWheel: UInt32?
    var accumulatedWheelTime: Double?
    
    var delegate:LisMoveSessionDelegate?
    var timer: Timer?
    
    var startTime: Date?
    
    var pauseTime: Date?
    
    //difference for timer
    var diff: DateComponents!

    var isStarted = false
    
    var resetMode = false
    
    func setDelegate(sensor: CadenceSensor?){
        sensor!.start()
        sensor!.sensorDelegate = self
    }
    
    func startSession(sensor: CadenceSensor?){
        
        if(sensor != nil){
            sensor!.stop()
            sensor!.start()
            sensor!.sensorDelegate = self
        }
        
        if(startTime == nil){
            startTime = Date()
        }
        isStarted = true
        
        scheduleTimeUpdater()
    }

    
    func stopSession(sensor: CadenceSensor?){
        if(sensor != nil){
            sensor!.stop()
        }
        accumulatedDistance = nil
        accumulatedWheel = nil
        accumulatedWheelTime = nil
        startTime = nil
        timer?.invalidate()
        timer = nil
        
        isStarted = false
    }
    
    func pauseSession(sensor: CadenceSensor?, automaticPause: Bool){
        
        if(sensor != nil && !automaticPause){
            sensor!.stop()
        }
        timer?.invalidate()
        timer = nil
        
        isStarted = true
        
        self.pauseTime = Date()
    }
    
    func resumeSession(sensor: CadenceSensor?, automaticPause: Bool){
    
        if(sensor != nil && !automaticPause){
            sensor!.start()
        }
        
        //time from paused to resume -> ex: 5s
        let pauseDiffComponent = Calendar.current.dateComponents([.hour, .minute, .second], from: self.pauseTime!, to: Date())
        
        //sum difference with start time -> ex: 30 + 5
        self.startTime = Calendar.current.date(byAdding: pauseDiffComponent, to: self.startTime!)
        
        isStarted = true
        
        scheduleTimeUpdater()
    }
    
    func resetSensor(sensor: CadenceSensor?){
        if(sensor != nil){
            resetMode = true
            sensor!.stop()
            sensor!.start()
        }
    }
    
    func errorDiscoveringSensorInformation(_ error: NSError) {
        print("An error ocurred disconvering the sensor services/characteristics: \(error)")
    }
    
    func sensorReady() {
        
        if(!resetMode){
            print("Sensor ready to go...")
            accumulatedDistance = 0.0
            accumulatedWheel = 0
            accumulatedWheelTime = 0.0
    
        }
        
        resetMode = false

        
    }
    
    func scheduleTimeUpdater(){
        if(self.timer == nil){
            //self.timer = Timer(timeInterval: 1.0, target: self, selector: #selector(generatePartialTask), userInfo: nil, repeats: true)
            RunLoop.current.add(self.timer!, forMode: .common)
        }
    }
    
    @objc func updateUI(){
        self.diff = Calendar.current.dateComponents([.hour, .minute, .second], from: startTime ?? Date(), to: Date())
        delegate?.updateTimer(value: "\(formatTimeInt(diff.hour)):\(formatTimeInt(diff.minute)):\(formatTimeInt(diff.second))")
    }
    private func formatTimeInt(_ value: Int?)-> String{
        let displayNumber = value ?? 0
        return String(format: "%02d", displayNumber)
    }
    
    func sensorUpdatedValues(speedInMetersPerSecond speed: Double?, cadenceInRpm cadence: Double?, distanceInMeters distance: Double?, wheelDiff: UInt32?, cumulativeWheel: UInt32?, wheelTimeDiff: Double?, battery: Int?, firmwareV: String?) {
        accumulatedDistance? += distance ?? 0
        accumulatedWheelTime? += wheelTimeDiff ?? 0
        accumulatedWheel? += wheelDiff ?? 0
        
        let distance = accumulatedDistance
        let speed = speed
        let wheelDiff = accumulatedWheel
        let wheelTimeDiff = accumulatedWheelTime
        
        
        let update =  LisMoveSessionUpdate(deltaRevs: wheelDiff ?? 0, cumulativeWheel: cumulativeWheel ?? 0, wheelTime: wheelTimeDiff ?? 0, speed: speed ?? 0, distance: distance ?? 0, battery: battery, firmwareV: firmwareV)
        
        delegate?.onSessionUpdate(update: update)
        
        
    }
}

public protocol LisMoveSessionDelegate {
    func onSessionUpdate(update: LisMoveSessionUpdate)
    func updateTimer(value: String)
}
*/
