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
 * org.adrianchia.geocoding.api.model.Geometry.java
 * Geometry Data Model
 * 
 * @author Adrian Chia
 * @since 1.0.0
 * */
public class Geometry implements Serializable {

	private static final long serialVersionUID = -2780115883710972913L;

	@JsonProperty("location")
	private Coordinate location;
	
	@JsonProperty("location_type")
	private String locationType;
	
	@JsonProperty("viewport")
	private ViewPort viewport;
	
	@JsonProperty("bounds")
	private Bound bounds;

	/**
	 * Empty Constructor
	 * */
	public Geometry() {
	}

	public Geometry(Coordinate location, String locationType, ViewPort viewport, Bound bounds) {
		this.location = location;
		this.locationType = locationType;
		this.viewport = viewport;
		this.bounds = bounds;
	}

	/**
	 * @return the location of this geometry
	 */
	@XmlElement(name = "location")
	public Coordinate getLocation() {
		return location;
	}

	/**
	 * @param location the location of this geometry
	 */
	public void setLocation(Coordinate location) {
		this.location = location;
	}

	/**
	 * @return the location type of this geometry
	 */
	@XmlElement(name = "location_type")
	public String getLocationType() {
		return locationType;
	}

	/**
	 * @param locationType the location type of this geometry
	 */
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	/**
	 * @return the viewport of this geometry
	 */
	@XmlElement(name = "viewport")
	public ViewPort getViewport() {
		return viewport;
	}

	/**
	 * @param viewport the viewport of this geometry
	 */
	public void setViewport(ViewPort viewport) {
		this.viewport = viewport;
	}

	/**
	 * @return the bounding box of this geometry
	 */
	@XmlElement(name = "bound")
	public Bound getBounds() {
		return bounds;
	}

	/**
	 * @param bounds the bounding box of this geometry
	 */
	public void setBounds(Bound bounds) {
		this.bounds = bounds;
	}
	
}
