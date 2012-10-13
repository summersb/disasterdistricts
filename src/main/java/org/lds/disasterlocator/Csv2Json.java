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
                    System.out.println(tokens.get(tokens.size() - 1));
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
                    System.out.println(tokens.get(tokens.size() - 1));
                }
            }
            String add = tokens.get(3);
            String[] latLng = null;
            if ((!"unknown".equals(add) && !add.isEmpty())) {
                latLng = getLatLong(add, tokens.get(4), tokens.get(6));
            }
            // write out
            fos.write("{\"household\":\"".getBytes());
            fos.write(tokens.get(2).getBytes());
            fos.write("\",\"leader\":false,\"address\":\"".getBytes());
            fos.write(tokens.get(3).getBytes());
            fos.write("\",\"lat\":\"".getBytes());
            if (latLng != null) {
                fos.write(latLng[1].trim().getBytes());
            } else {
                fos.write(tokens.get(1).getBytes());
            }
            fos.write("\",\"lng\":\"".trim().getBytes());
            if (latLng != null) {
                fos.write(latLng[0].trim().getBytes());
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

    private static String[] getLatLong(String address, String city, String zip) throws Exception {
        System.out.println("Getting address for " + address);
        String fullAddress = address + "," + city + "," + zip;
        DefaultHttpClient hc = new DefaultHttpClient();
        HttpGet post = new HttpGet("http://maps.google.com/maps/geo?q='" + fullAddress.replace(" ", "%20") + "'");//&output=csv");
        HttpResponse response = hc.execute(post);
        InputStream is = response.getEntity().getContent();
        String body = IOUtils.toString(is);
        is.close();
        System.out.println(body);
//        return body;
        if (body.indexOf("coordinates") == -1) {
            return null;
        }
        String latLng = body.substring(body.indexOf("coordinates") + 15, body.indexOf("]", body.indexOf("coordinates")));
        String[] split1 = latLng.split(",");
        String[] split = new String[2];
        split[0] = split1[0];
        split[1] = split1[1];
        return split;
    }
}
