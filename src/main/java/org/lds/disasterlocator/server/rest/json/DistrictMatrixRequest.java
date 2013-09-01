/**
 * Copyright (C) 2013
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
package org.lds.disasterlocator.server.rest.json;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bert Summers
 */
public class DistrictMatrixRequest {

    private List<LatLng> origins;
    private List<LatLng> destinations;
    private String travelMode;
    private int unitSystem;

    /**
     * @return the origin
     */
    public List<LatLng> getOrigins() {
        if(origins == null){
            origins = new ArrayList<>();
        }
        return origins;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigins(List<LatLng> origin) {
        this.origins = origin;
    }

    /**
     * @return the destinations
     */
    public List<LatLng> getDestinations() {
        if(destinations == null){
            destinations = new ArrayList<>();
        }
        return destinations;
    }

    /**
     * @param destinations the destinations to set
     */
    public void setDestinations(List<LatLng> destinations) {
        this.destinations = destinations;
    }

    /**
     * @return the travelMode
     */
    public String getTravelMode() {
        return travelMode;
    }

    /**
     * @param travelMode the travelMode to set
     */
    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    /**
     * @return the unitSystem
     */
    public int getUnitSystem() {
        return unitSystem;
    }

    /**
     * @param unitSystem the unitSystem to set
     */
    public void setUnitSystem(int unitSystem) {
        this.unitSystem = unitSystem;
    }
}
