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
package org.lds.disasterlocator.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.jpa.Member;

/**
 *
 * @author Bert W Summers
 */
@Path("/load")
@Produces("text/plain")
public class LoadMemberList {

    private final static Logger logger = Logger.getLogger(LoadMemberList.class.getName());
    private final EntityManagerFactory emf;

    public LoadMemberList() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    @GET
    public String loadList() throws IOException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        // delete all entities
        Query q = em.createQuery("delete from Member m");
        q.executeUpdate();

        InputStream resourceAsStream = this.getClass().getResourceAsStream("/members.js");
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Member[] mem = mapper.readValue(resourceAsStream, Member[].class);
        for (Member member : mem) {
            em.persist(member);
        }
        em.getTransaction().commit();
        return "OK";
    }
}
