package it.lismove.app.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AnimationUtils

import android.widget.LinearLayout
import androidx.annotation.IntegerRes
import androidx.core.view.isVisible
import it.lismove.app.android.databinding.ItemFabMenuViewBinding
import it.lismove.app.android.databinding.ItemFabMenuItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class FabMenuView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes){

    var fabItem = FabItem()
    var CONFIRMATION_DURATION = 1500L
    private var menuItems: List<MenuItem> = listOf()
    private var menuItemsView: ArrayList<ItemFabMenuItemBinding> = arrayListOf()

    var binding: ItemFabMenuViewBinding

    var isOpen = false
    var isProgressPending = false

    var isFabBlink = false


    init {
        ItemFabMenuViewBinding.inflate(LayoutInflater.from(context), this, true).also { binding = it }
        menuItems.forEach {
            addMenuItem(it)
        }

        setupUI()
    }

    fun updateMenuItems(items: List<MenuItem>){
        Timber.d("Update req")
            Timber.d("Actual update")

            menuItems = items
            binding.menuItemsContainer.removeAllViews()
            menuItems.forEach {
                addMenuItem(it)
            }
    }

    fun setFabAnimationRotation(){
        with(binding.floatingActionButton){
            //stopBlink
            isFabBlink = false
            clearAnimation()

            binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
            startAnimation(AnimationUtils.loadAnimation(context, R.anim.clockwise))
        }
    }

    fun setFabAnimationBlink(scope: CoroutineScope){
        isFabBlink = true
        binding.floatingActionButton.clearAnimation()
        setBlinkFabAnimation(scope)

    }

    fun setFabAnimationNormal(){
        isFabBlink = false
        with(binding.floatingActionButton){
            clearAnimation()
            binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
    }

    private fun setBlinkFabAnimation(scope: CoroutineScope){

        isFabBlink = true
        scope.launch {
            while (isFabBlink){
                    binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    delay(1000)
                    binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.BLACK)
                     delay(1000)
                }
            binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.WHITE)

        }
    }

    private fun openFabMenu(){

        if(menuItems.isNotEmpty()){
            isOpen = true
            with(binding){
                activeFabBackgroundView.isVisible = true
                menuItemsView.forEachIndexed { index, itemBinding ->
                    with(itemBinding){
                        menuItemContainer.isVisible = true
                        menuItemContainer.animate().translationY(-resources.getDimension(R.dimen.fabSpace))
                    }
                }
                activeFabBackgroundView.animate().setDuration(500).alpha(0.5f).withEndAction { activeFabBackgroundView.isVisible = true }
            }
        }

    }

    fun closeFabMenu(){
        if(menuItems.isNotEmpty()){
            isOpen = false
            with(binding){
                activeFabBackgroundView.animate().setDuration(500).alpha(0.0f).withEndAction { activeFabBackgroundView.isVisible = false }
                menuItemsView.forEachIndexed { index, itemBinding ->
                    with(itemBinding){
                        menuItemContainer.animate().translationY(floatingActionButton.y+64).withEndAction {menuItemContainer.isVisible = false }
                    }
                }
            }
        } else {
            with (binding) {
                activeFabBackgroundView.animate().setDuration(500).alpha(0.0f)
                    .withEndAction { activeFabBackgroundView.isVisible = false }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(){
        with(binding){
            floatingActionButton.setOnClickListener {
                if (menuItems.isEmpty()){
                    fabItem.onClickListener()
                }else{
                   toggleAndUpdateFabMenu()
                }
            }
            activeFabBackgroundView.setOnTouchListener { v, event ->
                if(event.action == MotionEvent.ACTION_UP){
                    toggleAndUpdateFabMenu()
                }
                return@setOnTouchListener true
            }
        }
    }


    private fun toggleAndUpdateFabMenu(){
        if(isOpen){
            closeFabMenu()
        }else{
            openFabMenu()
        }
    }

    private fun startLoading(binding: ItemFabMenuItemBinding, item: MenuItem){
        isProgressPending = true
        with(binding){
            circularProgressBar.isVisible = true
            circularProgressBar.setProgressWithAnimation(100f, CONFIRMATION_DURATION)
            circularProgressBar.onProgressChangeListener = { progress ->
                if(progress == 100f){
                    isProgressPending = false
                    onSuccess(binding, item)
                }
            }
        }

    }

    fun getMenuItemsCount() = menuItems.size

    private fun abortLoading(binding: ItemFabMenuItemBinding, item: MenuItem){
        binding.circularProgressBar.setProgressWithAnimation(0f, 50)
        clearCircularIndicator(binding, item)
    }

    private fun onSuccess(binding: ItemFabMenuItemBinding, item: MenuItem){
        clearCircularIndicator(binding, item)
        closeFabMenu()
        item.onConfirmedListener()
    }

    private fun clearCircularIndicator(binding: ItemFabMenuItemBinding, item: MenuItem){
        isProgressPending = false
        binding.circularProgressBar.isVisible = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addMenuItem(item:MenuItem){
        val newItem = ItemFabMenuItemBinding.inflate(LayoutInflater.from(this.context))
        newItem.chip.setOnTouchListener { v, event ->
           if(item.isLongConfirmationEnabled){
               if(event.action == MotionEvent.ACTION_DOWN){
                   startLoading(newItem, item)
               }else if(event.action == MotionEvent.ACTION_UP){
                   if(isProgressPending){
                       abortLoading(newItem, item)
                   }
               }
           }
            return@setOnTouchListener item.isLongConfirmationEnabled
        }
        newItem.chip.setOnClickListener {
            if(item.isLongConfirmationEnabled.not()){
                onSuccess(newItem, item)
            }
        }
        menuItemsView.add(newItem)
        with(newItem){
            root.isVisible = false
            chip.text = item.label
        }
        binding.menuItemsContainer.addView(newItem.root)
    }
}

data class FabItem(@IntegerRes val icon: Int? = null, var onClickListener: ()->Unit = {})

data class MenuItem(val label: String, val isLongConfirmationEnabled: Boolean = false , val onConfirmedListener: ()->Unit = {})