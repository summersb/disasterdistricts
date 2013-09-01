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
package org.lds.disasterlocator.server.rest.jpa;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author Bert Summers
 */
@Entity(name="Distance")
@NamedQueries({
    @NamedQuery(name = "Distance.find", query = "select d from Distance d where d.fromLat=:fromLat and d.fromLng=:fromLng and d.toLat=:toLat and d.toLng=:toLng")
})
public class DistanceJpa implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double fromLat;
    private double fromLng;
    private double toLat;
    private double toLng;
    private int distance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DistanceJpa)) {
            return false;
        }
        DistanceJpa other = (DistanceJpa) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.lds.disasterlocator.server.rest.jpa.Distance[ id=" + getId() + " ]";
    }

    /**
     * @return the from_lat
     */
    public double getFromLat() {
        return fromLat;
    }

    /**
     * @param from_lat the from_lat to set
     */
    public void setFromLat(double from_lat) {
        this.fromLat = from_lat;
    }

    /**
     * @return the form_lng
     */
    public double getFormLng() {
        return fromLng;
    }

    /**
     * @param form_lng the form_lng to set
     */
    public void setFromLng(double form_lng) {
        this.fromLng = form_lng;
    }

    /**
     * @return the to_lat
     */
    public double getToLat() {
        return toLat;
    }

    /**
     * @param to_lat the to_lat to set
     */
    public void setToLat(double to_lat) {
        this.toLat = to_lat;
    }

    /**
     * @return the to_lng
     */
    public double getToLng() {
        return toLng;
    }

    /**
     * @param to_lng the to_lng to set
     */
    public void setToLng(double to_lng) {
        this.toLng = to_lng;
    }

    /**
     * @return the distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

}