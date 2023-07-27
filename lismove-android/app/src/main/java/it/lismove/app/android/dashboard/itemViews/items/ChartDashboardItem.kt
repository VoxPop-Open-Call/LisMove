package it.lismove.app.android.dashboard.itemViews.items

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.faltenreich.skeletonlayout.createSkeleton
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.itemViews.data.ChartDashboardItemData
import it.lismove.app.android.databinding.ItemDashboardChartBinding

class ChartDashboardItem(val data: ChartDashboardItemData,
                         val binding: ItemDashboardChartBinding
){

    var entries = arrayListOf<Entry>()

    fun bind() {
        with(binding){
            root.isVisible = true
            initLineChart()
            setChartData()
        }


    }

    private fun setChartData(){
        entries = arrayListOf<Entry>()
        data.points.forEach {
            entries.add(Entry(it.xValue, it.yValue))
        }

        val lineDataSet = LineDataSet(entries, "Km percorsi").apply {
            color = ContextCompat.getColor(binding.root.context, R.color.main_orange_color)
            circleHoleColor = ContextCompat.getColor(binding.root.context, R.color.main_blue_color)
            setCircleColor(ContextCompat.getColor(binding.root.context, R.color.main_blue_color))
        }
        binding.chart.data = LineData(lineDataSet)
        binding.chart.invalidate()
    }

    private fun initLineChart() {

        with(binding){
            chart.axisLeft.setDrawGridLines(false)
            val xAxis: XAxis = binding.chart.xAxis
            val yAxis = binding.chart.axisLeft

            yAxis.setDrawAxisLine(true)

            xAxis.setDrawGridLines(true)
            xAxis.setDrawAxisLine(true)
            xAxis.setDrawLabels(true)

            yAxis.valueFormatter = YAxisFormatter()
            yAxis.axisMinimum = 0f
            chart.axisRight.isEnabled = false
            chart.legend.isEnabled = false
            chart.description.isEnabled = false
            //chart.animateX(1000, Easing.EaseInSine)

            xAxis.position = XAxis.XAxisPosition.BOTTOM

            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true

            xAxis.valueFormatter = XAxisFormatter()
            //xAxis.labelCount = 12

            chart.isHighlightPerTapEnabled = false
            chart.setPinchZoom(true)
        }

    }


    inner class XAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if(index < 0){
                    return ""
            }else if (index < entries.size) {
                    data.points[index].label

            } else {
                ""
            }
        }
    }
    inner class YAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < entries.size) {
                val formattedValue = String.format("%.2f", value)
                "$formattedValue km"
            } else {
                ""
            }
        }
    }



}