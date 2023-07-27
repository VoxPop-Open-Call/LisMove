//
//  DailyUseTableViewCell.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 26/07/21.
//

import UIKit
import Charts
import SkeletonView
import MaterialComponents.MaterialCards


class DailyUseTableViewCell: MDCCardCollectionCell {

    @IBOutlet weak var chart: LineChartView!
    var chartData = [UserDistanceStats]()
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        [chart].forEach{
            $0?.showAnimatedGradientSkeleton()
        }
    }
    
    func hideAnimation(){
        [chart].forEach{
            $0?.hideSkeleton(reloadDataAfter: true, transition: .none)
        }
    }
    
    func setupCell(data: [UserDistanceStats]){
        print("setupCell")
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
            
        if(data.count > 0){
            
            //order point data
            self.chartData = data.sorted(by: {dateFormatter.date(from:$0.day!)! < dateFormatter.date(from:$1.day!)! })
            
            var lineChartEntry: [ChartDataEntry] = []
            var i = 1
            self.chartData.forEach(){ graphPoint in
                let value = ChartDataEntry(x: Double(i), y: graphPoint.distance ?? 0.0)
                lineChartEntry.append(value)
                i += 1

            }
            let chartLine = LineChartDataSet(entries: lineChartEntry, label: "Km percorsi")
            
            chartLine.colors = [NSUIColor.systemOrange]
            chartLine.circleHoleColor = NSUIColor.systemBlue
            chartLine.circleColors = [NSUIColor.systemBlue]
            chartLine.circleRadius = 5
            let data = LineChartData()
            data.addDataSet(chartLine)
            chart.data = data
        }
        
        chart.rightAxis.enabled = false
        chart.leftAxis.axisMinimum = 0
        chart.xAxis.drawGridLinesEnabled = true
        chart.xAxis.labelPosition = .bottom
        chart.xAxis.granularity = 1.0
        
        let axisFormatter = XAxisFormatter()
        axisFormatter.chartData = chartData
        chart.xAxis.valueFormatter = axisFormatter
       
        
    }
    
    
    class XAxisFormatter: IndexAxisValueFormatter{
        var chartData = [UserDistanceStats]()
        
    
        override func stringForValue(_ value: Double,
                            axis: AxisBase?) -> String {

            let index = Int(value)
            print("stringForValue \(index) with size \(chartData.count)")
            if(index < 1){
                return ""
            }else if (index <= chartData.count){
                return chartData[index-1].getDayString()
            }
            return ""
        }
    }

}
