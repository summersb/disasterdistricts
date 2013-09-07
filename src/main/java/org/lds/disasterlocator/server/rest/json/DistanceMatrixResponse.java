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
package org.lds.disasterlocator.server.rest.json;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Bert W Summers
 */
public class DistanceMatrixResponse {

    private List<String> destination_addresses;
    private List<String> origin_addresses;
    private List<Row> rows;
    private String status;

    /**
     * @return the destination_addresses
     */
    public List<String> getDestination_addresses() {
        if(destination_addresses == null){
            destination_addresses = new ArrayList<>();
        }
        return destination_addresses;
    }

    /**
     * @param destination_addresses the destination_addresses to set
     */
    public void setDestination_addresses(List<String> destination_addresses) {
        this.destination_addresses = destination_addresses;
    }

    /**
     * @return the origin_addresses
     */
    public List<String> getOrigin_addresses() {
        if(origin_addresses == null){
            origin_addresses = new ArrayList<>();
        }
        return origin_addresses;
    }

    /**
     * @param origin_addresses the origin_addresses to set
     */
    public void setOrigin_addresses(List<String> origin_addresses) {
        this.origin_addresses = origin_addresses;
    }

    /**
     * @return the rows
     */
    public List<Row> getRows() {
        if(rows == null){
            rows = new ArrayList<>();
        }
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\"origin_addresses\":[");
        for (String string : origin_addresses) {
            sb.append("\"").append(string).append("\",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("],\n\"destination_addresses\":[");
        for (String string : destination_addresses) {
            sb.append("\"").append(string).append("\",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("],\n\"rows\":[\n");
        for (Row row : rows) {
            sb.append(row.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("],\"status\":\"OK\"}");
        return sb.toString();
    }
}