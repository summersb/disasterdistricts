/**
 * Copyright (C) 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lds.disasterlocator.rest.json;

/**
 *
 * @author Bert W Summers
 */
public class LatLonBox {
private long north;
private long south;
private long east;
private long west;

    /**
     * @return the north
     */
    public long getNorth() {
        return north;
    }

    /**
     * @param north the north to set
     */
    public void setNorth(long north) {
        this.north = north;
    }

    /**
     * @return the south
     */
    public long getSouth() {
        return south;
    }

    /**
     * @param south the south to set
     */
    public void setSouth(long south) {
        this.south = south;
    }

    /**
     * @return the east
     */
    public long getEast() {
        return east;
    }

    /**
     * @param east the east to set
     */
    public void setEast(long east) {
        this.east = east;
    }

    /**
     * @return the west
     */
    public long getWest() {
        return west;
    }

    /**
     * @param west the west to set
     */
    public void setWest(long west) {
        this.west = west;
    }

    @Override
    public String toString() {
        return "LatLonBox{" + "north=" + north + ", south=" + south + ", east=" + east + ", west=" + west + '}';
    }
}
