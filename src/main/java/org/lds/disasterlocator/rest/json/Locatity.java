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
public class Locatity {
private String LocalityName;
private PostalCode PostalCode;
private Thoroughfare Thoroughfare;

    /**
     * @return the localityName
     */
    public String getLocalityName() {
        return LocalityName;
    }

    /**
     * @param localityName the localityName to set
     */
    public void setLocalityName(String localityName) {
        this.LocalityName = localityName;
    }

    /**
     * @return the postalCode
     */
    public PostalCode getPostalCode() {
        return PostalCode;
    }

    /**
     * @param postalCode the postalCode to set
     */
    public void setPostalCode(PostalCode postalCode) {
        this.PostalCode = postalCode;
    }

    /**
     * @return the thoroughFare
     */
    public Thoroughfare getThoroughFare() {
        return Thoroughfare;
    }

    /**
     * @param thoroughFare the thoroughFare to set
     */
    public void setThoroughFare(Thoroughfare thoroughFare) {
        this.Thoroughfare = thoroughFare;
    }

    @Override
    public String toString() {
        return "Locatity{" + "localityName=" + LocalityName + ", postalCode=" + PostalCode + ", thoroughFare=" + Thoroughfare + '}';
    }
}
