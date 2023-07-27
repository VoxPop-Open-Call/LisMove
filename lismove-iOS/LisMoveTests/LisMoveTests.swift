//
//  LisMoveTests.swift
//  LisMoveTests
//
//

import XCTest
@testable import LisMove

class LisMoveTests: XCTestCase {
    
    
    //MARK: Data
    var sessionData: Data!
    
    
    //MARK: TESTS
    
    //1. simulate session to check data
    func simulateSession(){
        //load dat
        let session = try? JSONDecoder().decode(Session.self, from: sessionData)
        
        session?.partials.forEach{item in
      
            SessionManager.sharedInstance.updatePartialSession(type: Partial.PartialType(rawValue: item.type) ?? Partial.PartialType.InProgress, lat: item.latitude, lng: item.longitude, altitude: item.latitude)
        }
    }
    
    
    //MARK: Test life cycle
    override func setUp() {
        super.setUp()
        // Using this, a new instance of ShoppingCart will be created
        // before each test is run.
        sessionData = getData(name: "sessione")
        simulateSession()
        
    }
    
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
        
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() throws {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
    }

    func testPerformanceExample() throws {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }
    
    
    //MARK: --------utils
    private func getData(name: String, withExtension: String = "json") -> Data {
        let bundle = Bundle(for: type(of: self))
        let fileUrl = bundle.url(forResource: name, withExtension: withExtension)
        let data = try! Data(contentsOf: fileUrl!)
        return data
    }

}
