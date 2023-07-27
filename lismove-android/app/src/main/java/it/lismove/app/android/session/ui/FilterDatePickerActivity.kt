package it.lismove.app.android.session.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import it.lismove.app.android.databinding.ActivityDatePickerBinding
import it.lismove.app.android.general.utils.dismissKeyboard
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class FilterDatePickerActivity : AppCompatActivity() {
    lateinit var binding: ActivityDatePickerBinding
    var datePickerDialog: DatePickerDialog? = null
    val START_DATE = 0
    val END_DATE = 1
    var startDate: Date = Date()
    var endDate: Date = Date()

    companion object{
        val INTENT_START_DATE = "INTENT_START_DATE"
        val INTENT_END_DATE = "INTENT_END_DATE"

        fun getIntent(ctx: Context, startDate: Date, endDate: Date): Intent{
            return Intent(ctx, FilterDatePickerActivity::class.java).apply {
                putExtra(INTENT_START_DATE, Gson().toJson(startDate))
                putExtra(INTENT_END_DATE, Gson().toJson(endDate))

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Filtra sessioni"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.startDateTextField.setOnClickListener {
            openDatePicker(binding.startDateTextField, START_DATE)
        }

        binding.endDateTextField.setOnClickListener {
            openDatePicker(binding.endDateTextField, END_DATE)
        }
        binding.doneButton.setOnClickListener {
            if(areDateValid()){
                onDone()
            }else{
                Toast.makeText(this, "La data di inizio non può essere successiva alla data di fine", Toast.LENGTH_SHORT).show()
            }
        }
        loadFromIntent()
    }

    fun loadFromIntent(){
        val startDateString = intent.getStringExtra(INTENT_START_DATE)
        val endDateString = intent.getStringExtra(INTENT_END_DATE)


        startDateString?.let {
            startDate = Gson().fromJson(it, Date::class.java)

        }
        endDateString?.let {
            endDate = Gson().fromJson(it, Date::class.java)
        }
        with(binding){
            startDateTextField.setText(getStringFromDate(startDate))
            endDateTextField.setText(getStringFromDate(endDate))
        }
    }

    private fun openDatePicker(field: TextInputEditText, type: Int){
        val date = if(type == START_DATE){
            startDate
        }else{
            endDate
        }
        val cldr: Calendar = Calendar.getInstance()
        cldr.time = date
        val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
        val month: Int = cldr.get(Calendar.MONTH)
        val year: Int = cldr.get(Calendar.YEAR)
        // date picker dialog
        datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                field.setText("$dayOfMonth-${monthOfYear + 1}-$year")
                if(type == START_DATE){
                    startDate = getDate(dayOfMonth, monthOfYear +1, year)
                }else{
                    endDate = getDate(dayOfMonth, monthOfYear+1, year)
                }
            },
            year,
            month,
            day
        )
        datePickerDialog?.show()
        datePickerDialog?.setOnDismissListener {
            currentFocus?.dismissKeyboard()
            field.clearFocus()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getDate(dayOfMonth: Int, monthOfYear: Int, year: Int): Date{
        return DateTime().withDayOfMonth(dayOfMonth).withMonthOfYear(monthOfYear).withYear(year).withTimeAtStartOfDay().toDate()
    }
    fun areDateValid(): Boolean{
       return startDate.before(endDate) || startDate == endDate
    }

    fun getStringFromDate(date: Date): String{
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatter.format(date)

    }

    fun onDone(){
        val intent = Intent().apply {
            putExtra(INTENT_START_DATE, Gson().toJson(startDate))
            putExtra(INTENT_END_DATE, Gson().toJson(endDate))
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}