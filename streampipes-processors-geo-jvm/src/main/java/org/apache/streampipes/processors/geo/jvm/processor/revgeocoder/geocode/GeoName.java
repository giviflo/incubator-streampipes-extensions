/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
The MIT License (MIT)
[OSI Approved License]
The MIT License (MIT)
Copyright (c) 2014 Daniel Glasson
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package org.apache.streampipes.processors.geo.jvm.processor.revgeocoder.geocode;

import static java.lang.Math.*;

import org.apache.streampipes.processors.geo.jvm.processor.revgeocoder.geocode.kdtree.KDNodeComparator;

import java.util.Comparator;

/**
 * Created by Daniel Glasson on 18/05/2014.
 * This class works with a placenames files from http://download.geonames.org/export/dump/
 */

public class GeoName extends KDNodeComparator<GeoName> {
    public String name;
    public boolean majorPlace; // Major or minor place
    public double latitude;
    public double longitude;
    public double point[] = new double[3]; // The 3D coordinates of the point
    public String country;

    GeoName(String data) {
        String[] names = data.split("\t");
        name = names[1];
        majorPlace = names[6].equals("P");
        latitude = Double.parseDouble(names[4]);
        longitude = Double.parseDouble(names[5]);
        setPoint();
        country = names[8];
    }

    GeoName(Double latitude, Double longitude) {
        name = country = "Search";
        this.latitude = latitude;
        this.longitude = longitude;
        setPoint();
    }

    private void setPoint() {
        point[0] = cos(toRadians(latitude)) * cos(toRadians(longitude));
        point[1] = cos(toRadians(latitude)) * sin(toRadians(longitude));
        point[2] = sin(toRadians(latitude));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected double squaredDistance(GeoName other) {
        double x = this.point[0] - other.point[0];
        double y = this.point[1] - other.point[1];
        double z = this.point[2] - other.point[2];
        return (x*x) + (y*y) + (z*z);
    }

    @Override
    protected double axisSquaredDistance(GeoName other, int axis) {
        double distance = point[axis] - other.point[axis];
        return distance * distance;
    }

    @Override
    protected Comparator<GeoName> getComparator(int axis) {
        return GeoNameComparator.values()[axis];
    }

    protected static enum GeoNameComparator implements Comparator<GeoName> {
        x {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[0], b.point[0]);
            }
        },
        y {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[1], b.point[1]);
            }
        },
        z {
            @Override
            public int compare(GeoName a, GeoName b) {
                return Double.compare(a.point[2], b.point[2]);
            }
        };
    }
}
