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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.jpa.Member;
import org.lds.disasterlocator.rest.EntityManagerFactoryHelper;

/**
 *
 * @author Bert W Summers
 */
public class LoadMemberList {

    private final EntityManagerFactory emf;

    public LoadMemberList() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    public String loadList(InputStream is) throws IOException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        // delete all entities
        Query q = em.createQuery("delete from Member m");
        q.executeUpdate();

        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Member[] mem = mapper.readValue(is, Member[].class);
        for (Member member : mem) {
            em.persist(member);
        }
        em.getTransaction().commit();
        return "OK";
    }

    public static void main(String[] args) throws Exception{
        new LoadMemberList().loadList(new FileInputStream(new File("members.js")));
    }
}
