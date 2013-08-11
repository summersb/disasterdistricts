/**
 * Copyright 2012 Adrian Chia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lds.disasterlocator.server.rest.json.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * org.adrianchia.geocoding.api.model.Coordinate.java
 * Coordinate Data Model
 * 
 * @author Adrian Chia
 * @since 1.0.0
 * */
public class Coordinate implements Serializable {
    
    private static final long serialVersionUID = 742467719304802931L;
    
    @JsonProperty("lat")
    private String latitude;
    
    @JsonProperty("lng")
    private String longitude;
    
    /**
     * @return the latitude of this coordinate
     */
    @XmlElement(name = "lat")
    public String getLatitude() {
        return latitude;
    }
    
    /**
     * @param latitude the latitude of this coordinate
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    /**
     * @return the longitude of this coordinate
     */
    @XmlElement(name = "lng")
    public String getLongitude() {
        return longitude;
    }
    
    /**
     * @param longitude the longitude of this coordinate
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
