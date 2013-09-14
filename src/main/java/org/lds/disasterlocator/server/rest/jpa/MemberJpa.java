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

import org.lds.disasterlocator.shared.Member;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.lds.disasterlocator.server.rest.json.LatLng;

/**
 *
 * @author Bert W Summers
 */
@Entity(name="Member")
@Table(name="Member")
@NamedQueries({
    @NamedQuery(name = "Member.all", query = "select m from Member m"),
    @NamedQuery(name = "Member.byHousehold", query = "select m from Member m where m.household=:household"),
    @NamedQuery(name = "Member.withoutDistance", query = "select m from Member m where "
        + "not exists (select d from Distance d where d.memberLat=m.lat and d.memberLng=m.lng)"),
    @NamedQuery(name = "Member.byAddress", query = "select m from Member m where m.address=:address"),
    @NamedQuery(name = "Member.byDistrictId", query = "select m from Member m where m.district=:id")
})
public class MemberJpa implements Serializable, Member {

    public MemberJpa(){}

    public MemberJpa(Member m){
        household = m.getHousehold();
        address = m.getAddress();
        city = m.getCity();
        state = m.getState();
        zip = m.getZip();
        email = m.getEmail();
        lat = m.getLat();
        lng = m.getLng();
        phone = m.getPhone();
        district = m.getDistrict();
    }

    @Override
    public String toString() {
        return "Member{" + "household=" + getHousehold() + ", address=" + getAddress() + ", city=" + getCity() + ", zip=" + getZip() + ", email=" + getEmail() + ", lat=" + getLat() + ", lng=" + getLng() + ", phone=" + getPhone() + ", district=" + getDistrict() + ", auto=" + isAuto() + "}";
    }

    public LatLng getLocation(){
        return new LatLng(getLat(), getLng());
    }

    @Id
    private String household;
    private String address;
    private String city;
    @Column(name="ST")
    private String state;
    private String zip;
    private String email;
    private double lat;
    private double lng;
    private String phone;
    private int district = 0;// 0 is unassigned
    private boolean auto = true;

    /**
     * @return the houseHold
     */
    @Override
    public String getHousehold() {
        return household;
    }

    /**
     * @param houseHold the houseHold to set
     */
    @Override
    public void setHousehold(String houseHold) {
        this.household = houseHold;
    }

    /**
     * @return the address
     */
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the city
     */
    @Override
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    @Override
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the zip
     */
    @Override
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    @Override
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the lat
     */
    @Override
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    @Override
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the lng
     */
    @Override
    public double getLng() {
        return lng;
    }

    /**
     * @param lng the lng to set
     */
    @Override
    public void setLng(double lng) {
        this.lng = lng;
    }

    /**
     * @return the email
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phone
     */
    @Override
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the district
     */
    @Override
    public int getDistrict() {
        return district;
    }

    /**
     * @param district the district to set
     */
    @Override
    public void setDistrict(int district) {
        this.district = district;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the auto
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * @param auto the auto to set
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }
}
