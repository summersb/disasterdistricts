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
public class PostalCode {
private String PostalCodeNumber;

    /**
     * @return the postalCodeNumber
     */
    public String getPostalCodeNumber() {
        return PostalCodeNumber;
    }

    /**
     * @param postalCodeNumber the postalCodeNumber to set
     */
    public void setPostalCodeNumber(String postalCodeNumber) {
        this.PostalCodeNumber = postalCodeNumber;
    }

    @Override
    public String toString() {
        return "PostalCode{" + "postalCodeNumber=" + PostalCodeNumber + '}';
    }
}
