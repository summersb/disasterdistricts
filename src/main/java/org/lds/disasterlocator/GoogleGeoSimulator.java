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
package org.lds.disasterlocator;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.server.rest.jpa.AddressMapJpa;
import org.lds.disasterlocator.server.rest.EntityManagerFactoryHelper;
import org.lds.disasterlocator.server.rest.json.DistanceMatrixResponse;

/**
 *
 * @author Bert W Summers
 */
public class GoogleGeoSimulator {

    private static final EntityManagerFactory emf = EntityManagerFactoryHelper.createEntityManagerFactory();

    public static void storeUrl(String url, String data) throws IOException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AddressMapJpa map = new AddressMapJpa();
        map.setKey(url);
        map.setValue(data);
        map.setId(getHash(url));
        em.persist(map);
        em.getTransaction().commit();
        em.close();
    }

    private static String getHash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return (new HexBinaryAdapter()).marshal(md.digest(key.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GoogleGeoSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;
    }

    public static String getUrl(String url) throws IOException {
        EntityManager em = emf.createEntityManager();
        AddressMapJpa address = em.find(AddressMapJpa.class, getHash(url));
        if (address == null) {
            em.close();
            return null;
        }
        String data = address.getValue();
        if (data != null) {
            System.out.println("Found cache");
            ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DistanceMatrixResponse dmr = mapper.readValue(IOUtils.toInputStream(data), DistanceMatrixResponse.class);
            if (dmr.getStatus().equals("OK") == false) {
                System.out.println("Bad cache, removing");
                em.getTransaction().begin();
                em.remove(address);
                em.getTransaction().commit();
                return null;
            }
        }
        return data;
    }
}
