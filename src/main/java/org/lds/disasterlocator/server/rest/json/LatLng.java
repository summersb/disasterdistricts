/*
 * Copyright (C) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lds.disasterlocator.server.rest.json;

/**
 *
 * @author Bert Summers
 */
public class LatLng {

    private double jb;
    private double kb;

    /**
     * @return the Latitude
     */
    public double getJb() {
        return jb;
    }

    /**
     * @param jb the jb to set
     */
    public void setJb(double jb) {
        this.jb = jb;
    }

    /**
     * @return the Longitude
     */
    public double getKb() {
        return kb;
    }

    /**
     * @param kb the kb to set
     */
    public void setKb(double kb) {
        this.kb = kb;
    }
}
