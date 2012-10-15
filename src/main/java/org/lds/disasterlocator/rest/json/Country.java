/**
 * Copyright (C) 2012
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
package org.lds.disasterlocator.rest.json;

/**
 *
 * @author Bert W Summers
 */
public class Country {
private AdministrativeArea AdministrativeArea;
private String CountryName;
private String CountryNameCode;

    /**
     * @return the administrativeArea
     */
    public AdministrativeArea getAdministrativeArea() {
        return AdministrativeArea;
    }

    /**
     * @param administrativeArea the administrativeArea to set
     */
    public void setAdministrativeArea(AdministrativeArea administrativeArea) {
        this.AdministrativeArea = administrativeArea;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return CountryName;
    }

    /**
     * @param countryName the countryName to set
     */
    public void setCountryName(String countryName) {
        this.CountryName = countryName;
    }

    /**
     * @return the countryNameCode
     */
    public String getCountryNameCode() {
        return CountryNameCode;
    }

    /**
     * @param countryNameCode the countryNameCode to set
     */
    public void setCountryNameCode(String countryNameCode) {
        this.CountryNameCode = countryNameCode;
    }

    @Override
    public String toString() {
        return "Country{" + "administrativeArea=" + AdministrativeArea + ", countryName=" + CountryName + ", countryNameCode=" + CountryNameCode + '}';
    }
}
