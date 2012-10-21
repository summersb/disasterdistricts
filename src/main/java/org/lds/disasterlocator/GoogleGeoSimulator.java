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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.rest.json.DistanceMatrixResponse;

/**
 *
 * @author Bert W Summers
 */
public class GoogleGeoSimulator {

    private static final String FILE_NAME = "/Users/summersb/googlegeo.data";
    private static HashMap<String, String> map;

    public static void storeUrl(String url, String data) throws IOException {
        HashMap<String, String> map = getMap();
        map.put(url, data);
        saveMap(map);
        System.out.println("Saved url");
    }

    public static String getUrl(String url) throws IOException {
        HashMap<String, String> map = getMap();
        String data = map.get(url);
        if (data != null) {
            System.out.println("Found cache");
            ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DistanceMatrixResponse dmr = mapper.readValue(IOUtils.toInputStream(data), DistanceMatrixResponse.class);
            if (dmr.getStatus().equals("OK") == false) {
                System.out.println("Bad cache, removing");
                map.remove(url);
                saveMap(map);
            }

        }
        return data;
    }

    private static HashMap<String, String> getMap() throws IOException {
        if (map == null) {
            try {
                ObjectInput oi = new ObjectInputStream(new FileInputStream(new File(FILE_NAME)));
                map = (HashMap<String, String>) oi.readObject();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GoogleGeoSimulator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException e) {
                map = new HashMap<String, String>();
            }
        }
        return map;
    }

    private static void saveMap(HashMap<String, String> map) throws IOException {
        ObjectOutput oo = new ObjectOutputStream(new FileOutputStream(new File(FILE_NAME)));
        oo.writeObject(map);
        oo.close();
    }

    public static void main(String[] args) throws IOException {
        getMap();
        for (String key : map.keySet()) {
            System.out.println(key);
            System.out.println(map.get(key));
        }
    }
}
