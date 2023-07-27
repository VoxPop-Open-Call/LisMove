package it.lismove.app.android.maps

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import it.lismove.app.android.R
import it.lismove.app.android.maps.data.FountainClusterItem
import timber.log.Timber

internal class DrinkingFountainClusterManager(val context: Context, val map: GoogleMap, val clusterManager: ClusterManager<FountainClusterItem>) :
    DefaultClusterRenderer<FountainClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(person: FountainClusterItem, markerOptions: MarkerOptions) {
        // Draw a single person - show their profile photo and set the info window to show their name
        markerOptions
            .icon(getItemIcon(person))

    }

    override fun onClusterItemUpdated(person: FountainClusterItem, marker: Marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
        marker.setIcon(getItemIcon(person))
    }

    private fun getItemIcon(item: FountainClusterItem): BitmapDescriptor {
        val icon = AppCompatResources.getDrawable(context, R.drawable.pin_drinking_fountain)
        val bitmap = icon?.toBitmap(76, 76)
        return BitmapDescriptorFactory.fromBitmap(bitmap!!)
    }

    override fun setOnClusterClickListener(listener: ClusterManager.OnClusterClickListener<FountainClusterItem>?) {
        super.setOnClusterClickListener(listener)
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<FountainClusterItem>?) {
        super.setOnClusterItemClickListener(listener)

    }
}