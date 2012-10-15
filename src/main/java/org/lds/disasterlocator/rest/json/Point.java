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

import java.util.List;

/**
 *
 * @author Bert W Summers
 */
public class Point {
private List<Double> coordinates;

    @Override
    public String toString() {
        return "Point{" + "coordinates=" + coordinates + '}';
    }

    /**
     * @return the coordinates
     */
    public List<Double> getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
