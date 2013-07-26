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
package org.lds.disasterlocator.rest.json.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * org.adrianchia.geocoding.api.model.AddressComponent.java
 * Address Component Data Model
 * 
 * @author Adrian Chia
 * @since 1.0.0
 * */
public class AddressComponent implements Serializable {
	
    private static final long serialVersionUID = 662790229855240657L;
    
    @JsonProperty("long_name")
    private String longName;
    
    @JsonProperty("short_name")
    private String shortName;
    
    @JsonProperty("types")
    private String[] types;
    
    /**
     * Empty Constructor
     */
    public AddressComponent() {	
	}
    
    public AddressComponent(String longName, String shortName, String[] types){
        this.longName = longName;
        this.shortName = shortName;
        this.types = types;
    }
    
    /**
     * @return the long name of this address component
     */
    @XmlElement(name = "long_name")
    public String getLongName() {
        return longName;
    }
    
    /**
     * @param longName the long name of this address component
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }
    
    /**
     * @return the short name of this address component
     */
    @XmlElement(name = "short_name")
    public String getShortName() {
        return shortName;
    }
    
    /**
     * @param the short name of this address component
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    /**
     * @return the address component types 
     * see {@link https://developers.google.com/maps/documentation/geocoding/#Types}
     */
    @XmlElement(name = "type")
    public String[] getTypes() {
        return types;
    }
    
    /**
     * @param types set the address component types 
     * see {@link https://developers.google.com/maps/documentation/geocoding/#Types}
     */
    public void setTypes(String[] types) {
	    this.types = types;
    }
    
}
