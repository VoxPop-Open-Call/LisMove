package it.lismove.app.android.session.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import it.lismove.app.android.BuildConfig
import it.lismove.app.android.R
import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.android.databinding.ActivitySessionDetailBinding
import it.lismove.app.android.general.BUILD_VARIANT_PROD
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.*
import it.lismove.app.android.initiative.ui.view.ListAlertDialog
import it.lismove.app.android.session.ui.data.SessionDetailUI
import it.lismove.app.android.session.ui.data.TotalPoints
import it.lismove.app.android.session.ui.view.NestedScrollMapFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception
import java.util.*


class SessionDetailActivity : LisMoveBaseActivity(), LceView<SessionDetailUI>, OnMapReadyCallback {
    lateinit var binding: ActivitySessionDetailBinding
    val viewModel: SessionDetailViewModel by inject()
    companion object{
        const val INTENT_SESSION_ID = "session_id"
        const val INTENT_IS_FROM_HISTORY = "INTENT_IS_FROM_HISTORY"
        fun getIntent(context: Context, sessionId: String, isFromHistory: Boolean): Intent{
            return Intent(context, SessionDetailActivity::class.java).apply {
                putExtra(INTENT_SESSION_ID, sessionId)
                putExtra(INTENT_IS_FROM_HISTORY, isFromHistory)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.sessionDetailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Timber.d("OnCreate")
        val sessionId = intent.getStringExtra(INTENT_SESSION_ID) ?: ""
        val isFromHistory = intent.getBooleanExtra(INTENT_IS_FROM_HISTORY, false)
        viewModel.setData(sessionId, isFromHistory)
        Timber.d("SessionId is ${sessionId}")
        viewModel.sessionDetailObservable.observe(this, LceDispatcher(this))
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id){
            R.id.action_share ->shareSession()
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun shareSession(){
        Timber.d("Share session")
        if(viewModel.sessionDetail != null){
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, viewModel.getSessionShareString())
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }else{
            showError("Nessuna sessione presente", binding.root)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("OnResume")
        viewModel.loadSessionData()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.session_detail_menu, menu)
        return true
    }

    override fun onLoading() {
        if(viewModel.sessionDetail == null){
             with(binding){
                loading.visibility = View.VISIBLE
                content.visibility = View.GONE
            }
        }

    }

    override fun onSuccess(data: SessionDetailUI) {
        with(binding){
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
            //feedBackRequestLayout.isVisible = false
            feedBackRequestLayout.isVisible = data.validationRequired == null
            supportActionBar?.title = data.date

            distanceView.setRightTitle(data.distance)
            speedView.setRightTitle(data.speed)
            durationView.setRightTitle(data.duration)


            setupSessionPoints(data)

            setupTotalPoints(data)


            errorLayout.visibility = if(data.showInfoMessage) View.VISIBLE else View.GONE
            errorMessage.text = data.message
            infoMessageImage.setImageDrawable(AppCompatResources.getDrawable(this@SessionDetailActivity, data.messageIcon))
            infoMessageImage.setColorFilter(ContextCompat.getColor(this@SessionDetailActivity,data.messageIconTint))
            refundLayout.isVisible = data.showRefundLayout
            refundEuro.text = data.refundEuro
            refundString.text = data.refundLabel

            gpxButton.setOnClickListener {
                viewModel.shareGpx(data.id, this@SessionDetailActivity)
            }

            if (BuildConfig.FLAVOR != BUILD_VARIANT_PROD) {
                showDebugOptions(data)
            }

            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as NestedScrollMapFragment
            mapFragment.getMapAsync(this@SessionDetailActivity)

            binding.feedBackRequestLayout.setOnClickListener {
                startActivity(SessionFeedBackActivity.getIntent(this@SessionDetailActivity, viewModel.sessionId))
            }
        }
    }

    private fun setupTotalPoints(data: SessionDetailUI) {
        if (data.totalPoints != null) {
            showTotalPoints(data.totalPoints)
        } else {
            hideTotalPoints()
        }
    }

    private fun hideTotalPoints() {
        with(binding){
            pointsLayout.weightSum = 1f
            totalPointsLayout.isVisible = false
            dividerLayout.isVisible = false
        }
    }

    private fun showTotalPoints(totalPoints: TotalPoints){
        with(binding){
            pointsLayout.weightSum = 2f
            totalPointsLayout.isVisible = true
            dividerLayout.isVisible = true

            totalPointsNational.text = totalPoints.totalPointsNational
            totalPointsInitiativeLabel.text = "INIZIATIVA(x${ totalPoints.totalInitiativeNumber})"

            moreInitiativeTotalImage.setOnClickListener { showTotalPointsDetail(totalPoints.totalPointsInitiative) }
            sessionPointsInitiative.setOnClickListener {  showTotalPointsDetail(totalPoints.totalPointsInitiative) }

            if ((totalPoints.totalInitiativeNumber ?:0) < 2) {
                showTotalPointsOneOrZeroInitiative(totalPoints)
            } else {
                showTotalPointsMoreInitiative()
            }
        }
    }

    private fun showTotalPointsMoreInitiative(){
        with(binding){
            moreInitiativeTotalImage.visibility = View.VISIBLE
            totalnPointsInitiative.visibility = View.GONE
        }
    }

    private fun showTotalPointsOneOrZeroInitiative(totalPoints: TotalPoints){
        with(binding){
            moreInitiativeTotalImage.visibility = View.GONE
            totalnPointsInitiative.visibility = View.VISIBLE
            var initiativePoint = 0
            totalPoints.totalPointsInitiative.forEach { initiativePoint += it.points}
            totalnPointsInitiative.text = initiativePoint.toString()
        }
    }

    private fun setupSessionPoints(data: SessionDetailUI) {
        with(binding){

            sessionPointsInitiativeLabel.text = "INIZIATIVA(x${data.sessionInitiativeNumber})"
            sessionPointsNational.text = data.sessionPointsNational

            sessionPointsInitiative.setOnClickListener { showSessionPointsDetail() }
            moreInitiativeImage.setOnClickListener { showSessionPointsDetail() }

            zeroInitiativePointsLabel.isVisible = data.showZeroPointLabel
            zeroInitiativePointsLabel.setOnClickListener {
                val hasActiveInitiative = (data.sessionInitiativeNumber ?: 0) > 0
                showZeroInitiativeExplanationAlert(hasActiveInitiative)
            }

            if (data.showInitiativePoints) {
                showSessionPointsOneOrZeroInitiative(data)
            } else {
                showSessionPointsMoreInitiative()
            }
        }

    }

    private fun showSessionPointsMoreInitiative(){
        with(binding){
            moreInitiativeImage.visibility = View.VISIBLE
            sessionPointsInitiative.visibility = View.GONE
        }
    }

    private fun showSessionPointsOneOrZeroInitiative(data: SessionDetailUI){
        with(binding){
            moreInitiativeImage.visibility = View.GONE
            sessionPointsInitiative.visibility = View.VISIBLE
            sessionPointsInitiative.text = data.sessionInitiativePoints
        }
    }

    private fun showZeroInitiativeExplanationAlert(requestVerificationEnabled: Boolean) {
        val message = "I punti Iniziativa sono collegati ai progetti Lis Move promossi da organizzazioni quali Amministrazioni Comunali, Imprese o Istituti Scolastici.\n" +
                "\n" +
                "Possono essere accumulati esclusivamente con l'utilizzo del dispositivo hardware che viene fornito nel Kit Lis Move\n" +
                "\n" +
                "I motivi per cui non hai ricevuto i punti iniziativa possono essere i seguenti:" +
                "\n\n" +
                "\u2022 Non hai pedalato in un'area compresa nel progetto,\n\n" +
                "\u2022 Non hai inserito un codice Iniziativa,\n\n" +
                "\u2022 Ci sono stati problemi con la connessione al dispositivo.\n\n"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Perchè non ho ricevuto punti iniziativa?")
        builder.setMessage(message)
        builder.setNegativeButton(
            "Chiudi"
        ) { dialog, id ->

        }
        if(requestVerificationEnabled && viewModel.sessionDetail?.validationRequired == null){
            builder.setPositiveButton("Richiedi verifica manuale"){dialog, id ->
                openSessionVerificationDialog()
            }}else if(requestVerificationEnabled && viewModel.sessionDetail?.validationRequired == true){
            builder.setPositiveButton("Verifica manuale richiesta", null)
        }

        val dialog = builder.show()
        dialog.apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                it.isEnabled = (requestVerificationEnabled && viewModel.sessionDetail?.validationRequired == null)
            }
        }
    }

    private fun openSessionVerificationDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Richiesta verifica manuale")

        val input = EditText(this)
        input.setHint("Se vuoi puoi allegare un messaggio")

        input.maxLines = 10
        val layout  = LinearLayout(this)
        layout.setPadding(48)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        input.layoutParams = layoutParams
        layout.addView(input)

        builder.setView(layout)

        builder.setPositiveButton("Richiedi verifica") { dialog, id ->
            var message = input.text.toString()
            requestVerification(message)
            dialog.dismiss()
        }
        builder.setNegativeButton("Annulla") { dialog, id ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun requestVerification(message: String){
        viewModel.viewModelScope.launch {
            showLoadingAlert()
            try{
                viewModel.requestSessionVerification(message)
                showSweetDialogSuccess("Richiesta verifica inviata", null)
            }catch (e: Exception) {
                showSweetDialogError(e.localizedMessage)
            }
        }
    }

    private fun showSessionPointsDetail(){
        val dialog = ListAlertDialog.build(this, "Punti iniziativa") {item, dialog ->}
        dialog.show()
        viewModel.getSessionPoints().observe(this){
            when(it){
                is LceLoading -> {dialog.showLoading()}
                is LceSuccess ->{dialog.setData(it.data)}
                is LceError -> {showError(it.error.localizedMessage ?: "", binding.root)}
            }
        }
    }

    private fun showTotalPointsDetail(totalPointsInitiative: List<RankingPointData>) {
        val dialog = ListAlertDialog.build(this, "Punti iniziativa") {item, dialog ->}
        dialog.show()
        dialog.setData(viewModel.getTotalPointsItem(totalPointsInitiative))
    }

    private fun showDebugOptions(data: SessionDetailUI) {
        // Show send manually button
        if (!data.isInSyncWithServer) {
            with (binding.debugSendManually) {
                visibility = View.VISIBLE
                setOnClickListener { viewModel.shareSessionManually(this@SessionDetailActivity) }
            }
        }
    }

    override fun onError(throwable: Throwable) {
        Toast.makeText(this, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(map: GoogleMap) {

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? NestedScrollMapFragment
        mapFragment?.let {
            it.setListener(object :NestedScrollMapFragment.OnTouchListener{
                override fun onTouch() {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                }

            })
        }
        var boundBuilder = LatLngBounds.builder()

        viewModel.sessionDetail?.polyline?.let {

            it.forEachIndexed { index, polyline ->
                val start =  BitmapFactory.decodeResource(resources,
                    R.drawable.ic_route_start_marker)
               val stop =  BitmapFactory.decodeResource(resources,
                    R.drawable.ic_route_pause_marker)
                val restart =  BitmapFactory.decodeResource(resources,
                    R.drawable.maps_and_flags_7)
                val end =  BitmapFactory.decodeResource(resources,
                    R.drawable.ic_route_finish_marker)
                val firstThumb = if(index == 0) start else restart
                val lastThumb = if(index == it.size - 1 ) end else stop
                drawPolyline(map, polyline, firstThumb, lastThumb, boundBuilder)
            }

            // Draw dashed line between various polylines
            var previousEndingPoint: LatLng? = null
            it.forEach { polyline ->
                if (previousEndingPoint != null) {
                    if (polyline.isNotEmpty()) {
                        val dashedPath = listOf(previousEndingPoint, polyline.first())
                        drawPolyline(map, dashedPath.filterNotNull(), null, null, boundBuilder, true)
                    }
                }

                previousEndingPoint = polyline.lastOrNull()
            }

            try {
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 100)
                map.moveCamera(cameraUpdate)
            }catch (e: Exception){
                it.firstOrNull()?.firstOrNull()?.let{
                    map.moveCamera(CameraUpdateFactory.newLatLng(it))
                }
            }

        }


    }

    fun drawPolyline(map: GoogleMap, points: List<LatLng>, first: Bitmap?, last: Bitmap?, boundBuilder: LatLngBounds.Builder, dashed: Boolean = false){
        if(points.isNotEmpty()){
            val polylineOption = PolylineOptions()
                .clickable(false)
                .addAll(points)

            val polyline = map.addPolyline(polylineOption)

            stylePolyline(polyline)

            if (dashed) styleDashedPolyline(polyline)

            points.forEach{
                boundBuilder.include(it)
            }

            first?.let {
                map.addMarker(
                    MarkerOptions()
                        .position(points.first())
                        .icon(BitmapDescriptorFactory.fromBitmap(it))
                )
            }

            last?.let {
                map.addMarker(
                    MarkerOptions()
                        .position(points.last())
                        .icon(BitmapDescriptorFactory.fromBitmap(it))
                )
            }
        }
    }

    private fun styleDashedPolyline(polyline: Polyline) {
        polyline.pattern = listOf(Dot(), Gap(20F),)
    }

    private fun stylePolyline(polyline: Polyline) {
        polyline.width = 10f
        polyline.color = ResourcesCompat.getColor(resources, R.color.red_main, null)
        polyline.jointType = JointType.ROUND
    }


}