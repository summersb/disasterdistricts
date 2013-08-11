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
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * org.adrianchia.geocoding.api.model.GeocodeResponse.java
 * GeocodeResponse Data Model. The main data model we are going to use.
 * 
 * @author Adrian Chia
 * @since 1.0.0
 * */
@XmlRootElement(name = "GeocodeResponse")
public class GeocodeResponse implements Serializable {

    private static final long serialVersionUID = 3429149918178930030L;
	
    @JsonProperty("results")
    private Result[] results;
    
    @JsonProperty("status")
    private String status;
    
    /**
     * Empty Constructor
     * */
    public GeocodeResponse() {
    }
    
    public GeocodeResponse(Result[] results, String status) {
        this.results = results;
        this.status = status;
    }
    
    /**
     * @return an array of geocoded address information and geometry information.
     * */
    @XmlElement(name = "result")
    public Result[] getResults() {
        return results;
    }
    
    /**
     * @param results the array of geocoded address information and geometry information.
     * */
    public void setResults(Result[] results) {
        this.results = results;
    }
    
    /**
     * @return the status of geocoding response which contains the metadata on the request
     * */
    @XmlElement(name = "status")
    public String getStatus() {
        return status;
    }
    
    /**
     * @param status the status of geocoding response which contains the metadata on the request
     * */
    public void setStatus(String status) {
        this.status = status;
    }
    
}
