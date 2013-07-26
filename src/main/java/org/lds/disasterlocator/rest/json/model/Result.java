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
 * org.adrianchia.geocoding.api.model.Result.java
 * Result Data Model
 * 
 * @author Adrian Chia
 * @since 1.0.0
 * */
public class Result implements Serializable {

    private static final long serialVersionUID = -8138885373275438468L;

    @JsonProperty("address_components")
    private AddressComponent[] addressComponents;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    @JsonProperty("geometry")
    private Geometry geometry;

    @JsonProperty("types")
    private String[] types;

    /**
     * Empty Constructor
     * */
    public Result() {
    }

    public Result(AddressComponent[] addressComponents, String formattedAddress, Geometry geometry, String[] types) {
        this.addressComponents = addressComponents;
        this.formattedAddress = formattedAddress;
        this.geometry = geometry;
        this.types = types;
    }

    /**
     * @return the address components of this result
     * */
    @XmlElement(name = "address_component")
    public AddressComponent[] getAddressComponents() {
        return addressComponents;
    }
    
    /**
     * @param addressComponents set the address components of this result
     * */
    public void setAddressComponents(AddressComponent[] addressComponents) {
        this.addressComponents = addressComponents;
    }
    
    /**
     * @return the formatted address of this result
     * */
    @XmlElement(name = "formatted_address")
    public String getFormattedAddress() {
        return formattedAddress;
    }
    
    /**
     * @param formattedAddress the formatted address of this result
     * */
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
    
    /**
     * @Return the geometry of this result
     * */
    @XmlElement(name = "geometry")
    public Geometry getGeometry() {
        return geometry;
    }
    
    /**
     * @param geometry the geometry of this result
     * */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
    /**
     * @return the result types
     * */
    @XmlElement(name = "type")
    public String[] getTypes() {
        return types;
    }
    
    /**
     * @param types the result types
     * */
    public void setTypes(String[] types) {
        this.types = types;
    }


}
