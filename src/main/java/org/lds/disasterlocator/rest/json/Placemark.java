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
public class Placemark {
private String id;
private String address;
private AddressDetails AddressDetails;
private ExtendedData ExtendedData;
private Point Point;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @return the addressDetails
     */
    public AddressDetails getAddressDetails() {
        return AddressDetails;
    }

    /**
     * @param addressDetails the addressDetails to set
     */
    public void setAddressDetails(AddressDetails addressDetails) {
        this.AddressDetails = addressDetails;
    }

    /**
     * @return the extendedData
     */
    public ExtendedData getExtendedData() {
        return ExtendedData;
    }

    /**
     * @param extendedData the extendedData to set
     */
    public void setExtendedData(ExtendedData extendedData) {
        this.ExtendedData = extendedData;
    }

    /**
     * @return the point
     */
    public Point getPoint() {
        return Point;
    }

    /**
     * @param point the point to set
     */
    public void setPoint(Point point) {
        this.Point = point;
    }

    @Override
    public String toString() {
        return "Placemark{" + "id=" + id + ", address=" + address + ", addressDetails=" + AddressDetails + ", extendedData=" + ExtendedData + ", point=" + Point + '}';
    }
}
