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
package org.lds.disasterlocator.shared;

/**
 *
 * @author Bert W Summers
 */
public interface Member {

    /**
     * @return the address
     */
    String getAddress();

    /**
     * @return the city
     */
    String getCity();
    String getState();

    /**
     * @return the district
     */
    int getDistrict();

    /**
     * @return the email
     */
    String getEmail();

    /**
     * @return the houseHold
     */
    String getHousehold();

    /**
     * @return the lat
     */
    double getLat();

    /**
     * @return the lng
     */
    double getLng();

    /**
     * @return the phone
     */
    String getPhone();

    /**
     * @return the zip
     */
    String getZip();

    /**
     * @param address the address to set
     */
    void setAddress(String address);

    /**
     * @param city the city to set
     */
    void setCity(String city);
    void setState(String state);

    /**
     * @param district the district to set
     */
    void setDistrict(int district);

    /**
     * @param email the email to set
     */
    void setEmail(String email);

    /**
     * @param household the household to set
     */
    void setHousehold(String household);

    /**
     * @param lat the lat to set
     */
    void setLat(double lat);

    /**
     * @param lng the lng to set
     */
    void setLng(double lng);

    /**
     * @param phone the phone to set
     */
    void setPhone(String phone);

    /**
     * @param zip the zip to set
     */
    void setZip(String zip);

    /**
     * If this value is true then this Member should be assigned
     * a district based on their proximity to a district leader
     * @return
     */
    boolean isAuto();
    void setAuto(boolean auto);

}
