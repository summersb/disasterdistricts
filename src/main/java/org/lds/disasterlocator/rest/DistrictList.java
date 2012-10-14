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
package org.lds.disasterlocator.rest;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.lds.disasterlocator.jpa.District;
import org.lds.disasterlocator.jpa.Member;

/**
 *
 * @author Bert W Summers
 */
@Path("/district")
@Consumes({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
public class DistrictList {

    private final EntityManagerFactory emf;

    public DistrictList() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    @Path("/list")
    @GET
    public List<District> getDistrict() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<District> q = em.createQuery("select d from District d", District.class);
        List<District> resultList = q.getResultList();
        for (District district : resultList) {
            em.detach(district);
        }
        return resultList;
    }

    @POST
    public Response storeDistrict(District district) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        District find = em.find(District.class, district.getId());
        if (find != null) {
            em.merge(district);
        } else {
            em.persist(district);
        }
        em.getTransaction().commit();
        return Response.ok().build();
    }

      @GET
    @Path("csv")
    @Produces(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public String getWardListAsCsv() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<District> distQuery = em.createQuery("select m from District m", District.class);
        List<District> distList = distQuery.getResultList();
        StringBuilder sb = new StringBuilder("district,leader,assistant\n");
        for (District dist : distList) {
            sb.append(dist.getId());
            sb.append(",\"");
            sb.append(dist.getLeader().getHousehold());
            sb.append("\",");
            Member assist = dist.getAssistant();
            if(assist != null){
                sb.append("\"").append(assist.getHousehold()).append("\"");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
