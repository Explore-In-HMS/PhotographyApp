/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.huawei.clustering;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.Marker;

public class MarkerState {

    private final Marker marker;
    private boolean isDirty;

    public MarkerState(@NonNull Marker marker, boolean isDirty) {
        this.marker = marker;
        this.isDirty = isDirty;
    }

    public MarkerState(@NonNull Marker marker) {
        this(marker, false);
    }

    @NonNull
    public Marker getMarker() {
        return marker;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerState that = (MarkerState) o;

        if (isDirty != that.isDirty) return false;
        return marker.equals(that.marker);
    }

    @Override
    public int hashCode() {
        int result = marker.hashCode();
        result = 31 * result + (isDirty ? 1 : 0);
        return result;
    }
}