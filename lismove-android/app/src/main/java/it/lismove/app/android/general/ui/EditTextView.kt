package it.lismove.app.android.general.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.lismove.app.android.R

class EditTextView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes){

    lateinit var textInputLayout: TextInputLayout
    lateinit var editText: TextInputEditText
    private lateinit var  errorTextView: TextView
    lateinit var hintTextView: TextView

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_text_edit, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                R.styleable.editTextView, 0, 0)
            val hint = resources.getText(typedArray
                .getResourceId(R.styleable
                    .editTextView_hint, R.string.app_name))
            val error = resources.getText(typedArray
                .getResourceId(R.styleable
                    .editTextView_errorMessage, R.string.app_name))
            textInputLayout = findViewById(R.id.inputLayout)
            editText = findViewById(R.id.textField)
            errorTextView = findViewById(R.id.errorText)
            hintTextView = findViewById(R.id.hintTextView)
            textInputLayout.hint = hint
            errorTextView.text = error
            errorTextView.visibility = View.INVISIBLE
            typedArray.recycle()
        }

    }




    fun setHint(text: String) {
        editText.hint = text
    }

    fun setHintText(text: String?){
        hintTextView.setText(text)
        hintTextView.isVisible = text.isNullOrEmpty().not()
    }

    fun raiseError(message: String){
        textInputLayout.error = ""
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    fun clearError(){
        textInputLayout.error = null
        errorTextView.visibility = View.INVISIBLE
    }

    fun getText(): String{
        return textInputLayout.editText?.editableText.toString()
    }



}