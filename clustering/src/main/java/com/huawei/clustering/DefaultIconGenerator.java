/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.huawei.clustering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;

/**
 * The implementation of {@link IconGenerator} that generates icons with the default style
 * and caches them for subsequent use. To customize the style of generated icons use
 * {@link DefaultIconGenerator#setIconStyle(IconStyle)}.
 */
public class DefaultIconGenerator<T extends ClusterItem> implements IconGenerator<T> {

    private static final int[] CLUSTER_ICON_BUCKETS = {10, 20, 50, 100, 500, 1000, 5000, 10000, 20000, 50000, 100000};

    private final Context mContext;

    private IconStyle mIconStyle;

    private BitmapDescriptor mClusterItemIcon;

    private final SparseArray<BitmapDescriptor> mClusterIcons = new SparseArray<>();

    /**
     * Creates an icon generator with the default icon style.
     */
    public DefaultIconGenerator(@NonNull Context context) {
        mContext = Preconditions.checkNotNull(context);
        setIconStyle(createDefaultIconStyle());
    }

    /**
     * Sets a custom icon style used to generate marker icons.
     *
     * @param iconStyle the custom icon style used to generate marker icons
     */
    public void setIconStyle(@NonNull IconStyle iconStyle) {
        mIconStyle = Preconditions.checkNotNull(iconStyle);
    }

    /**
     * Create an icon for the given cluster item.
     *
     * @return the icon of the cluster
     */
    @NonNull
    public BitmapDescriptor getClusterIcon(@NonNull Cluster<T> cluster) {
        int clusterBucket = getClusterIconBucket(cluster);
        BitmapDescriptor clusterIcon = mClusterIcons.get(clusterBucket);

        if (clusterIcon == null) {
            clusterIcon = createClusterIcon(clusterBucket);
            mClusterIcons.put(clusterBucket, clusterIcon);
        }

        return clusterIcon;
    }

    /**
     * Create an icon for the given marker
     *
     * @return the icon of the marker
     */
    @NonNull
    @Override
    public BitmapDescriptor getMarkerIcon(@NonNull T clusterItem) {
        if (mClusterItemIcon == null) {
            mClusterItemIcon = createClusterItemIcon();
        }
        return mClusterItemIcon;
    }

    @NonNull
    private IconStyle createDefaultIconStyle() {
        return new IconStyle.Builder(mContext).build();
    }

    @NonNull
    public BitmapDescriptor createClusterIcon(int clusterBucket) {
        @SuppressLint("InflateParams")
        TextView clusterIconView = (TextView) LayoutInflater.from(mContext)
                .inflate(R.layout.map_cluster_icon, null);
        clusterIconView.setBackground(createClusterBackground());
        clusterIconView.setTextColor(mIconStyle.getClusterTextColor());
        clusterIconView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mIconStyle.getClusterTextSize());

        clusterIconView.setText(getClusterIconText(clusterBucket));

        clusterIconView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        clusterIconView.layout(0, 0, clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight());

        Bitmap iconBitmap = Bitmap.createBitmap(clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(iconBitmap);
        clusterIconView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(iconBitmap);
    }

    @NonNull
    private Drawable createClusterBackground() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(mIconStyle.getClusterBackgroundColor());
        gradientDrawable.setStroke(mIconStyle.getClusterStrokeWidth(),
                mIconStyle.getClusterStrokeColor());
        return gradientDrawable;
    }

    @NonNull
    private BitmapDescriptor createClusterItemIcon() {
        return BitmapDescriptorFactory.fromResource(mIconStyle.getClusterIconResId());
    }

    private int getClusterIconBucket(@NonNull Cluster<T> cluster) {
        int itemCount = cluster.getItems().size();
        if (itemCount <= CLUSTER_ICON_BUCKETS[0]) {
            return itemCount;
        }

        for (int i = 0; i < CLUSTER_ICON_BUCKETS.length - 1; i++) {
            if (itemCount < CLUSTER_ICON_BUCKETS[i + 1]) {
                return CLUSTER_ICON_BUCKETS[i];
            }
        }

        return CLUSTER_ICON_BUCKETS[CLUSTER_ICON_BUCKETS.length - 1];
    }

    @NonNull
    private String getClusterIconText(int clusterIconBucket) {
        return (clusterIconBucket < CLUSTER_ICON_BUCKETS[0]) ?
                String.valueOf(clusterIconBucket) : String.valueOf(clusterIconBucket) + "+";
    }
}
