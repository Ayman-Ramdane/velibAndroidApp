package fr.epf.min1.velib

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import fr.epf.min1.velib.model.Station

class StationRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Station>
) : DefaultClusterRenderer<Station>(context, map, clusterManager) {

    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            context,
            when (clusterItemsColor) {
                0 -> R.color.unfilteredCluster
                1 -> R.color.eBikeFilterCluster
                2 -> R.color.mechanicalBikeFilterCluster
                3 -> R.color.dockFilterCluster
                else -> {
                    R.color.unfilteredCluster
                }
            }
        )
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_pedal_bike,
            color
        )
    }

    override fun onBeforeClusterItemRendered(item: Station, markerOptions: MarkerOptions) {
        markerOptions.title(item.name)
            .position(LatLng(item.lat, item.lon))
            .icon(bicycleIcon)
    }

    override fun onClusterItemRendered(clusterItem: Station, marker: Marker) {
        marker.tag = clusterItem
    }

    private val bicyclesSeveralIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            context,
            when (clusterItemsColor) {
                0 -> R.color.unfilteredCluster
                1 -> R.color.eBikeFilterCluster
                2 -> R.color.mechanicalBikeFilterCluster
                3 -> R.color.dockFilterCluster
                else -> {
                    R.color.unfilteredCluster
                }
            }
        )
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_bikes_several_small_60,
            color
        )
    }

    override fun onBeforeClusterRendered(cluster: Cluster<Station>, markerOptions: MarkerOptions) {
        markerOptions.icon(bicyclesSeveralIcon)
    }

    override fun onClusterRendered(cluster: Cluster<Station>, marker: Marker) {
        marker.setIcon(bicyclesSeveralIcon)
    }
}
