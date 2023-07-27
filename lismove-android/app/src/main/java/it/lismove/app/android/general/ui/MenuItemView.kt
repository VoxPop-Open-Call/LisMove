package it.lismove.app.android.general.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.isVisible
import it.lismove.app.android.R
import timber.log.Timber
import java.lang.Error

class MenuItemView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes){



    private lateinit var leftImage: ImageView
    private lateinit var menuTitleView: TextView
    private lateinit var  rightTitleView: TextView
    private lateinit var  rightLabelView: TextView
    private lateinit var  arrowImage: ImageView
    private lateinit var  dividerView: View

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_menu_entry, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                R.styleable.menuItemView, 0, 0)


            val error = resources.getText(typedArray
                .getResourceId(R.styleable
                    .editTextView_errorMessage, R.string.app_name))
            var image: Drawable? = null
            typedArray.hasValue(R.styleable.menuItemView_leftImage)

            var imageNotNull = typedArray.hasValue(R.styleable.menuItemView_leftImage)

            image = ResourcesCompat.getDrawable(
                resources,typedArray.getResourceId(
                    R.styleable.menuItemView_leftImage, R.drawable.ic_logo),
                null)


            val title = typedArray.getString(R.styleable.menuItemView_menuTitle)
            val showArrow = typedArray.getBoolean(R.styleable.menuItemView_showArrow, false)
            val showDivider = typedArray.getBoolean(R.styleable.menuItemView_showDivider, true)
            val rightTitle = typedArray.getString(R.styleable.menuItemView_rightTitle)
            val rightLabel = typedArray.getString(R.styleable.menuItemView_rightLabel)

            Timber.d("title $title")

            leftImage = findViewById(R.id.leftImageView)
            menuTitleView = findViewById(R.id.menuTitle)
            rightTitleView = findViewById(R.id.rightTitle)
            rightLabelView = findViewById(R.id.rightLabel)
            arrowImage = findViewById(R.id.rightImageView)
            dividerView = findViewById(R.id.divider)

            arrowImage.isVisible = showArrow
            dividerView.isVisible = showDivider
            menuTitleView.text = title

            leftImage.setImageDrawable(image)

            leftImage.visibility = if(imageNotNull ) View.VISIBLE else View.GONE

            rightTitleView.text = rightTitle
            rightTitleView.visibility = if(rightTitle != null ) View.VISIBLE else View.GONE

            rightLabelView.text = rightLabel
            rightLabelView.visibility = if(rightLabel != null ) View.VISIBLE else View.GONE

            typedArray.recycle()
        }

    }


    fun setRightTitle(title: String?){
        rightTitleView.text = title
        rightTitleView.visibility = if(title != null ) View.VISIBLE else View.GONE


    }





}