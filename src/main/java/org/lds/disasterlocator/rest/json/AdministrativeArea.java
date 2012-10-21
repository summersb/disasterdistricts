/**
 * Copyright (C) 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
public class AdministrativeArea {

    private String AdministrativeAreaName;
    private SubAdministrativeArea SubAdministrativeArea;

    /**
     * @return the administrativeAreaName
     */
    public String getAdministrativeAreaName() {
        return AdministrativeAreaName;
    }

    @Override
    public String toString() {
        return "AdministrativeArea{" + "AdministrativeAreaName=" + AdministrativeAreaName + ", SubAdministrativeArea=" + SubAdministrativeArea + '}';
    }

    /**
     * @param administrativeAreaName the administrativeAreaName to set
     */
    public void setAdministrativeAreaName(String administrativeAreaName) {
        this.AdministrativeAreaName = administrativeAreaName;
    }

    /**
     * @return the SubAdministrativeArea
     */
    public SubAdministrativeArea getSubAdministrativeArea() {
        return SubAdministrativeArea;
    }

    /**
     * @param SubAdministrativeArea the SubAdministrativeArea to set
     */
    public void setSubAdministrativeArea(SubAdministrativeArea SubAdministrativeArea) {
        this.SubAdministrativeArea = SubAdministrativeArea;
    }
}
