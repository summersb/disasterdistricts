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
package org.lds.disasterlocator.jpa;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Bert W Summers
 */
@Entity
@Table(name="Member")
public class Member implements Serializable {

    @Override
    public String toString() {
        return "Member{" + "household=" + getHousehold() + ", address=" + getAddress() + ", city=" + getCity() + ", zip=" + getZip() + ", email=" + getEmail() + ", lat=" + getLat() + ", lng=" + getLng() + ", phone=" + getPhone() + ", district=" + getDistrict() + '}';
    }

    @Id
    private String household;
    private String address;
    private String city;
    private String zip;
    private String email;
    private String lat;
    private String lng;
    private String phone;
    private int district;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.getHousehold() != null ? this.getHousehold().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Member other = (Member) obj;
        if ((this.getHousehold() == null) ? (other.getHousehold() != null) : !this.household.equals(other.household)) {
            return false;
        }
        return true;
    }

    /**
     * @return the houseHold
     */
    public String getHousehold() {
        return household;
    }

    /**
     * @param houseHold the houseHold to set
     */
    public void setHouseHold(String houseHold) {
        this.setHousehold(houseHold);
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the lat
     */
    public String getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(String lat) {
        this.lat = lat;
    }

    /**
     * @return the lng
     */
    public String getLng() {
        return lng;
    }

    /**
     * @param lng the lng to set
     */
    public void setLng(String lng) {
        this.lng = lng;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @param household the household to set
     */
    public void setHousehold(String household) {
        this.household = household;
    }

    /**
     * @return the district
     */
    public int getDistrict() {
        return district;
    }

    /**
     * @param district the district to set
     */
    public void setDistrict(int district) {
        this.district = district;
    }
}
