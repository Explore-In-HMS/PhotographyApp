/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.huawei.clustering;

import androidx.annotation.NonNull;

class QuadTreeRect {

    final double north;
    final double west;
    final double south;
    final double east;

    QuadTreeRect(double north, double west, double south, double east) {
        this.north = north;
        this.west = west;
        this.south = south;
        this.east = east;
    }

    boolean contains(double latitude, double longitude) {
        return longitude >= west && longitude <= east && latitude <= north && latitude >= south;
    }

    boolean intersects(@NonNull QuadTreeRect bounds) {
        return west <= bounds.east && east >= bounds.west && north >= bounds.south && south <= bounds.north;
    }
}
