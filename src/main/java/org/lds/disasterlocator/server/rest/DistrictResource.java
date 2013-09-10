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
package org.lds.disasterlocator.server.rest;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert W Summers
 */
@Path("/district")
@Consumes({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
public class DistrictResource {

    private final EntityManagerFactory emf;

    public DistrictResource() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    DistrictResource(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Path("/list")
    @GET
    public List<DistrictJpa> getDistrict() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<DistrictJpa> q = em.createNamedQuery("District.all", DistrictJpa.class);
        List<DistrictJpa> resultList = q.getResultList();
        em.close();
        return resultList;
    }

    @DELETE
    @Path("{leader}")
    public Response deleteDistrict(@PathParam("leader") String leader){
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        // get district first
        TypedQuery<DistrictJpa> distQuery = em.createNamedQuery("District.byLeader", DistrictJpa.class).setParameter("leader", leader);
        List<DistrictJpa> resultList = distQuery.getResultList();
        for (DistrictJpa districtJpa : resultList) {
            // reset leader and assistant district ids to 0
            MemberJpa member = em.find(MemberJpa.class, leader);
            if(member != null){
                member.setDistrict(0);
            }
            em.persist(member);
            Member assistant = districtJpa.getAssistant();
            if(assistant != null){
                MemberJpa assist = em.find(MemberJpa.class, assistant.getHousehold());
                assist.setDistrict(0);
                em.persist(assist);
            }
        }
        Query query = em.createNamedQuery("District.deleteByLeader").setParameter("leader", leader);
        query.executeUpdate();
        em.getTransaction().commit();
        em.close();
        return Response.ok().build();
    }

    // Maybe compute distances to all members here
    // should spread the requests out over time
    // maybe not hit a limit
    @POST
    @Path("{leader}")
    @Consumes(MediaType.WILDCARD)
    public DistrictJpa createNewDistrict(@PathParam("leader") String leaderHousehold) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<DistrictJpa> find = em.createQuery("select d from District d", DistrictJpa.class);
        int id = find.getResultList().size()+1;
        MemberJpa leader = em.find(MemberJpa.class, leaderHousehold);
        if(leader.getDistrict() != 0){
            id = leader.getDistrict();
        }
        DistrictJpa d = new DistrictJpa();
        d.setId(id);
        d.setLeader(leader);
        leader.setDistrict(id);
        em.persist(d);
        em.persist(leader);
        em.getTransaction().commit();
        return d;
    }

    @POST
    @Path("{leader}/{assistant}")
    @Consumes(MediaType.WILDCARD)
    public Response createNewDistrict(@PathParam("leader") String leaderHousehold, @PathParam("assistant") String assistantHousehold) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<DistrictJpa> find = em.createQuery("select d from District d", DistrictJpa.class);
        int id = find.getResultList().size()+1;
        MemberJpa leader = em.find(MemberJpa.class, leaderHousehold);
        MemberJpa assistant = em.find(MemberJpa.class, assistantHousehold);
        DistrictJpa d = new DistrictJpa();
        d.setId(id);
        d.setLeader(leader);
        d.setAssistant(assistant);
        leader.setDistrict(id);
        assistant.setDistrict(id);
        em.persist(d);
        em.persist(leader);
        em.persist(assistant);
        em.getTransaction().commit();
        return Response.ok().build();
    }

    @POST
    public Response createDistrict(DistrictJpa district) {
        EntityManager em = emf.createEntityManager();
        DistrictJpa find = em.find(DistrictJpa.class, district.getId());
        if (find != null) {
            em.merge(district);
        } else {
            em.persist(district);
        }
        return Response.ok().build();
    }
}
