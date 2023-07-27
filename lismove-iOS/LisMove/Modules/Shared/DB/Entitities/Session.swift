// Session.swift

import Foundation
import Realm
import RealmSwift

// MARK: - Session
public class Session: Object, Codable{
    @objc public dynamic var sessionCode = UUID().uuidString
    
    @objc public dynamic var id: String?
    @objc public dynamic var uid: String?
    
    /*SDK */
    public dynamic var startBattery = RealmOptional<Int>()
    public dynamic var endBattery = RealmOptional<Int>()
    @objc public dynamic var firmwareVersion: String?
    public dynamic var hubCoefficient = RealmOptional<Double>()

    public dynamic var phoneEndBattery = RealmOptional<Double>()
    public dynamic var phoneStartBattery = RealmOptional<Double>()
    
    public dynamic var startTime = RealmOptional<Int64>()
    public dynamic var endTime = RealmOptional<Int64>()
    public dynamic var duration = RealmOptional<Int64>()
    
    
    @objc public dynamic var gyroDistance = 0.0
    public dynamic var partials = List<Partial>()
    //@objc public dynamic var totalKM = 0.0 // deprecated
    @objc public dynamic var sessionDescription: String?
    
    @objc public dynamic var gpsOnlyDistance = 0.0
    public dynamic var verificationRequired = RealmOptional<Bool>()
    @objc public dynamic var verificationRequiredNote: String?

    /* SERVER */
    public dynamic var type: SessionType?
    public dynamic var polyline: [String]?
    //@objc public dynamic var urbanKM = 0.0 // deprecated
    @objc public dynamic var nationalKM = 0.0
    @objc public dynamic var totalKM = 0.0
    @objc public dynamic var nationalPoints = 0
    let sessionPoints = List<SessionPoint>()
    public dynamic var valid = RealmOptional<Bool>()
    
    public dynamic var euro = RealmOptional<Double>()
    @objc public dynamic var certificated = false
    public dynamic var homeWorkPath = false
    //@objc public dynamic var multiplier = 1 // deprecated
    public dynamic var sessionStatus = RealmOptional<Int>()
    
    @objc public dynamic var sendToServer = false
    public dynamic var co2 = RealmOptional<Double>()
    @objc public dynamic var gmapsDistance = 0.0
    @objc public dynamic var gpsDistance = 0.0
    
    public dynamic var sessionSDkState = RealmOptional<Int>()

    public enum SessionStatusType: Int {
        case VALID = 0
        case ERROR = 1
        case DISTANCE_ERROR = 2
        case SPEED_ERROR = 3
        case VALID_OFFLINE = 4
        case CERTIFIED_ERROR = 5
        case DEBUG = 6
        case ACCELERATION_PEAK = 7
        case CERTIFICATED = 8

    }
    
    public enum SessionType: Int, Codable {
        case BIKE = 0
        case ELECTRIC_BIKE = 1
        case SCOOTER = 2
        case FOOT = 3
        case CARPOOLING = 4
    }
    
    
    enum CodingKeys: String, CodingKey {
        case certificated = "certificated"
        case co2 = "co2"
        case sessionDescription = "sessionDescription"
        case endTime = "endTime"
        case duration = "duration"
        case euro = "euro"
        case gpsDistance = "gpsDistance"
        case gmapsDistance = "gmapsDistance"
        case gyroDistance = "gyroDistance"
        case gpsOnlyDistance = "gpsOnlyDistance"
        case homeWorkPath = "homeWorkPath"
        case id = "id"
        
        case nationalKm = "nationalKm"
        case nationalPoints = "nationalPoints"
        case partials = "partials"
        case sessionPoints = "sessionPoints"
        
        case startBattery = "startBattery"
        case endBattery = "endBattery"
        case firmwareVersion = "firmware"
        
        
        case phoneEndBattery = "phoneEndBattery"
        case phoneStartBattery = "phoneStartBattery"
        
        case polyline = "polyline"
        case startTime = "startTime"
        case sessionStatus = "status"
        //case totalKM = "totalKM"
        case type = "type"
        case uid = "uid"

        case valid = "valid"
        case verificationRequiredNote = "verificationRequiredNote"
        case verificationRequired = "verificationRequired"
        case hubCoefficient = "hubCoefficient"
    }
    
    
    required public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.uid = try container.decodeIfPresent(String.self, forKey: .uid)
        self.certificated = try container.decodeIfPresent(Bool.self, forKey: .certificated) ?? false
        self.co2.value = try container.decodeIfPresent(Double.self, forKey: .co2)
        self.sessionDescription = try container.decodeIfPresent(String.self, forKey: .sessionDescription)
        self.euro.value =  try container.decodeIfPresent(Double.self, forKey: .euro)
        self.gpsDistance = try container.decodeIfPresent(Double.self, forKey: .gpsDistance) ?? 0.0
        self.gmapsDistance = try container.decodeIfPresent(Double.self, forKey: .gmapsDistance) ?? 0.0
        self.gyroDistance = try container.decodeIfPresent(Double.self, forKey: .gyroDistance)  ?? 0.0
        self.gpsOnlyDistance = try container.decodeIfPresent(Double.self, forKey: .gpsOnlyDistance)  ?? 0.0
        self.homeWorkPath = try container.decodeIfPresent(Bool.self, forKey: .homeWorkPath) ?? false
        self.id = try container.decodeIfPresent(String.self, forKey: .id)

        self.nationalKM = try container.decodeIfPresent(Double.self, forKey: .nationalKm)  ?? 0.0
        self.nationalPoints = try container.decodeIfPresent(Int.self, forKey: .nationalPoints)  ?? 0
        //self.partials = try container.decode(List<Partial>.self, forKey: .partials) // prevent memory leak during decode if session hasmany partials
        var points = try container.decodeIfPresent(List<SessionPoint>.self, forKey: .sessionPoints) ?? List<SessionPoint> ()
        sessionPoints.append(objectsIn: points)
        self.startBattery.value = try container.decodeIfPresent(Int.self, forKey: .startBattery)
        self.endBattery.value = try container.decodeIfPresent(Int.self, forKey: .endBattery)
        self.firmwareVersion = try container.decodeIfPresent(String.self, forKey: .firmwareVersion)
        
        self.phoneEndBattery.value = try container.decodeIfPresent(Double.self, forKey: .phoneEndBattery)
        self.phoneStartBattery.value = try container.decodeIfPresent(Double.self, forKey: .phoneStartBattery)
        
        self.hubCoefficient.value = try container.decodeIfPresent(Double.self, forKey: .hubCoefficient)

        
        self.endTime.value =  try container.decodeIfPresent(Int64.self, forKey: .endTime)
        self.startTime.value = try container.decodeIfPresent(Int64.self, forKey: .startTime)
        self.duration.value = try container.decodeIfPresent(Int64.self, forKey: .duration)
        self.polyline = try container.decodeIfPresent([String].self, forKey: .polyline) ?? []
        self.sessionStatus.value = try container.decodeIfPresent(Int.self, forKey: .sessionStatus)
        //self.totalKM = try container.decodeIfPresent(Double.self, forKey: .totalKM) ?? 0.0
        self.type = try container.decodeIfPresent(SessionType.self, forKey: .type)
 
        self.valid.value = try container.decodeIfPresent(Bool.self, forKey: .valid)
        self.verificationRequired.value = try container.decodeIfPresent(Bool.self, forKey: .verificationRequired)
        self.verificationRequiredNote = try container.decodeIfPresent(String.self, forKey: .verificationRequiredNote)
        super.init()
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(self.uid, forKey: .uid)
        //try container.encodeIfPresent(self.certificated , forKey: .certificated)
        //try container.encodeIfPresent(self.co2.value , forKey: .co2)
        //try container.encodeIfPresent(self.sessionDescription, forKey: .sessionDescription)
        //try container.encodeIfPresent(self.euro.value , forKey: .euro)
        //try container.encode(self.gpsDistance , forKey: .gpsDistance)
        //try container.encode(self.gmapsDistance , forKey: .gmapsDistance)
        try container.encode(self.gyroDistance , forKey: .gyroDistance)
        try container.encode(self.gpsOnlyDistance , forKey: .gpsOnlyDistance)
        //try container.encodeIfPresent(self.homeWorkPath , forKey: .homeWorkPath)
        try container.encodeIfPresent(self.id, forKey: .id)

        try container.encode(self.nationalPoints , forKey: .nationalPoints)
        try container.encodeIfPresent(self.partials, forKey: .partials)
        try container.encodeIfPresent(self.sessionPoints, forKey: .sessionPoints)
        
        try container.encodeIfPresent(self.startBattery.value , forKey: .startBattery)
        try container.encodeIfPresent(self.endBattery.value , forKey: .endBattery)
        try container.encodeIfPresent(self.firmwareVersion , forKey: .firmwareVersion)
        
        try container.encodeIfPresent(self.phoneEndBattery.value , forKey: .phoneEndBattery)
        try container.encodeIfPresent(self.phoneStartBattery.value , forKey: .phoneStartBattery)
        try container.encodeIfPresent(self.hubCoefficient.value , forKey: .hubCoefficient)

        
        try container.encodeIfPresent(self.endTime.value , forKey: .endTime)
        try container.encodeIfPresent(self.startTime.value , forKey: .startTime)
        try container.encodeIfPresent(self.polyline, forKey: .polyline)
        //try container.encodeIfPresent(self.sessionStatus, forKey: .status)
        //try container.encode(self.totalKM , forKey: .totalKM)
        try container.encodeIfPresent(self.type, forKey: .type)
        try container.encodeIfPresent(self.duration, forKey: .duration)
        
        
        //try container.encodeIfPresent(self.valid.value, forKey: .valid)
    }

    public required override init() {
        super.init()
    }
    
    public override static func primaryKey() -> String? {
        return "sessionCode"
    }
    
    
    /*
     utility
     */
    
    func getReadableStatusMessage() -> String?{
        if(!sendToServer){
            switch SessionStatusType.init(rawValue: self.sessionStatus.value ?? 9999) {
            case .VALID:
                return "Sessione verificata"
            case .ERROR:
                return "Sessione corrotta"
            case .DISTANCE_ERROR:
                return "Distanza non accurata"
            case .SPEED_ERROR:
                return "Velocità non valida"
            case .VALID_OFFLINE:
                return "Sessione verificata"
            case .CERTIFIED_ERROR:
                return "Sessione non certificata"
            case .DEBUG:
                return "Debug"
            case .ACCELERATION_PEAK:
                return "Accelerazione non valida"
            case .CERTIFICATED:
                return "Sessione verificata"
            default:
                return nil
            }
        }else{
            return "Sessione non inviata"
        }
        
    }
    
    
    func initiativePoints() -> Int{
        return self.sessionPoints.map({$0.points.value ?? 0}).reduce(0, +)
    }
    
    func getValidatedInitiativePoints()-> Int{
        if(self.valid.value == false && !sendToServer){
            return 0
        }else{
            return initiativePoints()
        }
    }
    
    func getValidatedNationalPoints()-> Int{
        if(self.valid.value == false && !sendToServer){
            return 0
        }else{
            return nationalPoints
        }
    }
    
    func initiativeKm() -> Double{
        return self.sessionPoints.map({$0.distance}).reduce(0.0, +)
    }
    
    func getInitiativeNumber() -> Int{
        return sessionPoints.count
    }
    
    func getNonDebugPartials() -> [Partial]{
        return partials.filter({!$0.isDebug})
    }
    /**
        National Km is the session distance calculated by the server.
        
        If nationalKM is null, probabily the session hasn't be sent to server yet.
        In this case, use gyroDistance + gpsOnlyDistance that we have locally
     */
    func getTotalKM() ->Double{
        if (self.nationalKM != nil) {
            return nationalKM
        } else {
            return self.gyroDistance + self.gpsOnlyDistance
        }
    }
    
    
    func getPointsLog()-> String{
        var log = "[PointsLog] National points: \(self.nationalPoints), nationalKm: \(self.nationalKM)"
        for sessionPoint in sessionPoints{
            log += " Session Point \(sessionPoint.id): \(sessionPoint.points.value), distance: \(sessionPoint.distance)"
        }
        return log 
    }
    
    func getReadableDuration() -> String{
        let sessionDuration = duration.value ?? 0
        return TimeInterval(Int64(sessionDuration)).hourMinuteSecond
    }
    
    func isVerified()-> Bool{
        var isSuccess = false
        if let status = self.sessionStatus.value{
            isSuccess = status == Session.SessionStatusType.VALID.rawValue || status == Session.SessionStatusType.VALID_OFFLINE.rawValue ||
            status == Session.SessionStatusType.CERTIFICATED.rawValue
        }
        return isSuccess
    }
}


enum SessionState: Int, CaseIterable{
        case onGoing = 0
        case paused = 1
        case finished = 2
        case automaticPause = 3
        case stopped = 4
    
    static func getFromInt(value: Int)-> SessionState{
        return SessionState.allCases.first(where: {$0.rawValue == value}) ?? onGoing
    }
}
