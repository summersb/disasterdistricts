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
 * @author Adrian Chia
 * @since 1.0.0
 * */
public class Bound implements Serializable {
    
	private static final long serialVersionUID = 1768202017914686749L;
    
    @JsonProperty("northeast")
    private Coordinate northeast;
    
	@JsonProperty("southwest")
    private Coordinate southwest;
    
	/**
	 * Empty Constructor
	 * */
    public Bound() {
    }
    
    public Bound(Coordinate northeast, Coordinate southwest) {
        this.southwest = southwest;
        this.northeast = northeast;
    }
    
    /**
     * @return the northeast coordinates
     */
    @XmlElement(name = "northeast")
    public Coordinate getNortheast() {
        return northeast;
    }
    
    /**
     * @param northeast set the northeast coordinates
     */
    public void setNortheast(Coordinate northeast) {
        this.northeast = northeast;
    }
    
    /**
     * @return the southwest coordinates
     */
    @XmlElement(name = "southwest")
    public Coordinate getSouthwest() {
        return southwest;
    }
    
    /**
     * @param southwest set the southwest coordinates
     */
    public void setSouthwest(Coordinate southwest) {
        this.southwest = southwest;
    }

}
