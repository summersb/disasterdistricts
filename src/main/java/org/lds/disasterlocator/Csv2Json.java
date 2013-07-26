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
package org.lds.disasterlocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.rest.json.model.GeocodeResponse;
import org.lds.disasterlocator.rest.json.model.Result;

/**
 *
 * @author Bert W Summers
 */
public class Csv2Json {

    public static void main(String[] args) throws Exception {
        File f = new File("/Users/summersb/Downloads/275859.csv");
        BufferedReader br = new BufferedReader(new FileReader(f));
        br.readLine();
        // Skip header line
        String line = br.readLine();
        File out = new File("members.js");
        FileOutputStream fos = new FileOutputStream(out);
        fos.write("[".getBytes());
        while (line != null) {
            char[] toCharArray = line.toCharArray();
            ArrayList<String> tokens = new ArrayList<String>();
            int pos = 0;
            int lastPos = 0;
            boolean inQuote = false;
            for (; pos < toCharArray.length; pos++) {
                if (toCharArray[pos] == '"' && inQuote) {
                    tokens.add(line.substring(lastPos, pos));
                    lastPos = pos + 2;
                    pos++;
                    inQuote = false;
                } else if (toCharArray[pos] == '"') {
                    inQuote = true;
                    lastPos++;
                } else if (toCharArray[pos] == ',' && !inQuote) {
                    if (lastPos == pos) {
                        tokens.add("");
                    } else {
                        tokens.add(line.substring(lastPos, pos));
                    }
                    lastPos = pos + 1;
                }
            }
            String add = tokens.get(4);
            GeocodeResponse geocodeResponse = getGeo(add, tokens.get(4), tokens.get(6));
            if(!"OK".equals(geocodeResponse.getStatus())){
                System.err.println("Error getting address (" + add + ") for " + tokens.get(1));
                System.err.println(geocodeResponse);
                line = br.readLine();
                continue;
            }
            if("OK".equals(geocodeResponse.getStatus()) && geocodeResponse.getResults().length != 1){
                System.err.println("Found to many address (" + add + ") for " + tokens.get(1));
                System.err.println(geocodeResponse);
                line = br.readLine();
                continue;
            }
            // get address
            Result addr = geocodeResponse.getResults()[0];
            // write out
            fos.write("{\"household\":\"".getBytes());
            fos.write(tokens.get(1).getBytes());
            fos.write("\",\"leader\":false,\"address\":\"".getBytes());
                fos.write(addr.getFormattedAddress().getBytes());
            fos.write("\",\"lat\":\"".getBytes());
                fos.write(addr.getGeometry().getLocation().getLatitude().getBytes());
            fos.write("\",\"lng\":\"".trim().getBytes());
                fos.write(addr.getGeometry().getLocation().getLongitude().getBytes());
            fos.write("\",\"phone\":\"".getBytes());
            fos.write(tokens.get(2).getBytes());
            fos.write("\",\"email\":\"".getBytes());
            fos.write(tokens.get(7).getBytes());
//            fos.write("\",\"city\":\"".getBytes());
//            fos.write(tokens.get(4).getBytes());
//            fos.write("\",\"zip\":\"".getBytes());
//            fos.write(tokens.get(6).getBytes());
            fos.write("\"},".getBytes());
            fos.write("\n".getBytes());
            line = br.readLine();
        }
        fos.write("]".getBytes());
        fos.close();
    }

    private static GeocodeResponse getGeo(String address, String city, String zip) throws Exception {
        String fullAddress = address;// + "," + city + "," + zip;
        return getGeo(fullAddress);
    }

    public static GeocodeResponse getGeo(String fullAddress) throws Exception{
        DefaultHttpClient hc = new DefaultHttpClient();
        // version 3 api
        //http://maps.googleapis.com/maps/api/geocode/json?address=some address&sensor=false
        HttpGet post = new HttpGet("http://maps.googleapis.com/maps/api/geocode/json?address=" 
                + fullAddress.replace(" ", "%20") + "&sensor=false");
        HttpResponse response = hc.execute(post);
        InputStream is = response.getEntity().getContent();
        String result = IOUtils.toString(is, "UTF-8");
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != 200){
            System.out.println(result);
        }
        
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GeocodeResponse geo = mapper.readValue(result, GeocodeResponse.class);
        is.close();
        return geo;
    }
}
