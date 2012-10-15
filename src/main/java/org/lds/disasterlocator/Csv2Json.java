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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.rest.json.Geo;

/**
 *
 * @author Bert W Summers
 */
public class Csv2Json {

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = br.readLine();
        line = br.readLine();
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
            String add = tokens.get(3);
            Geo geo = getGeo(add, tokens.get(4), tokens.get(6));
            if(geo.getStatus().getCode() != 200){
                System.err.println("Error getting address for " + tokens.get(2));
                System.err.println(geo);
            }
            if(geo.getStatus().getCode() == 200 && geo.getPlacemark().size() != 1){
                System.err.println("Found to many address for " + tokens.get(2));
                System.err.println(geo);
            }
            // write out
            fos.write("{\"household\":\"".getBytes());
            fos.write(tokens.get(2).getBytes());
            fos.write("\",\"leader\":false,\"address\":\"".getBytes());
            if(geo.getStatus().getCode() == 200){
                fos.write(geo.getPlacemark().get(0).getAddress().getBytes());
            }else{
                fos.write(tokens.get(3).getBytes());
            }
            fos.write("\",\"lat\":\"".getBytes());
            if(geo.getStatus().getCode() == 200){
                fos.write(Double.toString(geo.getPlacemark().get(0).getPoint().getCoordinates().get(1)).getBytes());
            } else {
                fos.write(tokens.get(1).getBytes());
            }
            fos.write("\",\"lng\":\"".trim().getBytes());
            if(geo.getStatus().getCode() == 200){
                fos.write(Double.toString(geo.getPlacemark().get(0).getPoint().getCoordinates().get(0)).getBytes());
            } else {
                fos.write(tokens.get(0).getBytes());
            }
            fos.write("\",\"phone\":\"".getBytes());
            fos.write(tokens.get(7).getBytes());
            fos.write("\",\"email\":\"".getBytes());
            fos.write(tokens.get(8).getBytes());
            fos.write("\",\"city\":\"".getBytes());
            fos.write(tokens.get(4).getBytes());
            fos.write("\",\"zip\":\"".getBytes());
            fos.write(tokens.get(6).getBytes());
            fos.write("\"},".getBytes());
            fos.write("\n".getBytes());
            line = br.readLine();
        }
        fos.write("]".getBytes());
        fos.close();
    }

    private static Geo getGeo(String address, String city, String zip) throws Exception {
        String fullAddress = address + "," + city + "," + zip;
        DefaultHttpClient hc = new DefaultHttpClient();
        HttpGet post = new HttpGet("http://maps.google.com/maps/geo?q='" + fullAddress.replace(" ", "%20") + "'");//&output=csv");
        HttpResponse response = hc.execute(post);
        InputStream is = response.getEntity().getContent();
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Geo geo = mapper.readValue(is, Geo.class);
        is.close();
        return geo;
    }
}
