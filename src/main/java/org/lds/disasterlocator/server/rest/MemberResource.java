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

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.shared.Member;
import org.lds.disasterlocator.shared.MyAutoBeanFactory;

/**
 *
 * @author Bert W Summers
 */
@Path("/member")
@Consumes({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
public class MemberResource {

    private final static Logger logger = Logger.getLogger(MemberResource.class.getName());
    private final EntityManagerFactory emf;

    public MemberResource() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    MemberResource(EntityManagerFactory emf){
        this.emf = emf;
    }

    @GET
    @Path("/list")
    public List<MemberJpa> getWardList() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<MemberJpa> q = em.createQuery("select m from Member m order by m.district", MemberJpa.class);
        List<MemberJpa> resultList = q.getResultList();
        em.close();
        return resultList;
    }

    @GET
    @Path("/csv")
    @Produces(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public String getWardListAsCsv() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<DistrictJpa> distQuery = em.createQuery("select m from District m", DistrictJpa.class);
        HashMap<Integer, DistrictJpa> district = new HashMap<Integer, DistrictJpa>();
        List<DistrictJpa> distList = distQuery.getResultList();
        for (DistrictJpa d : distList) {
            district.put(d.getId(), d);
        }
        TypedQuery<MemberJpa> memberQuery = em.createQuery("select m from Member m order by m.district DESC", MemberJpa.class);
        List<MemberJpa> resultList = memberQuery.getResultList();
        StringBuilder sb = new StringBuilder("district,household,address,city,zip,phone,email,leader,assistant\n");
        for (MemberJpa member : resultList) {
            sb.append(member.getDistrict());
            sb.append(",\"");
            sb.append(member.getHousehold());
            sb.append("\",");
            sb.append("\"").append(member.getAddress()).append("\"  ,");
            sb.append(member.getCity()).append(",");
            sb.append(member.getZip()).append(",");
            sb.append(member.getPhone()).append(",");
            sb.append(member.getEmail()).append(",");
            DistrictJpa dist = district.get(member.getDistrict());
            if (dist != null && dist.getLeader() != null) {
                sb.append("\"").append(district.get(member.getDistrict()).getLeader().getHousehold()).append("\"");
            }
            sb.append(",");
            if (dist != null && dist.getAssistant() != null) {
                Member assist = dist.getAssistant();
                if (assist != null) {
                    sb.append("\"").append(assist.getHousehold()).append("\"");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @PUT
    public Response updateMember(MemberJpa member){
        EntityManager em = emf.createEntityManager();
        MemberJpa find = em.find(MemberJpa.class, member.getHousehold());
        if (find != null) {
            em.getTransaction().begin();
            em.merge(member);
            em.getTransaction().commit();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @POST
    public Response createMember(String json) {
        MyAutoBeanFactory factory = AutoBeanFactorySource.create(MyAutoBeanFactory.class);
        AutoBean<Member> memberAB = AutoBeanCodex.decode(factory, Member.class, json);
        Member memberBean = memberAB.as();
        MemberJpa member = new MemberJpa(memberBean);
        EntityManager em = emf.createEntityManager();
        // check if member exists and throw 409 Conflict if so
        Member find = em.find(MemberJpa.class, member.getHousehold());
        if(find != null){
            em.close();
            return Response.status(Status.CONFLICT).build();
        }
        em.getTransaction().begin();
        em.persist(member);
        em.getTransaction().commit();
        em.close();
        return Response.ok().build();
    }

    @GET
    @Path("/{household}")
    public Response getMember(@PathParam("household") String household){
        EntityManager em = emf.createEntityManager();
        MemberJpa member = em.find(MemberJpa.class, household);
        if(member == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().entity(member).build();
    }

    @DELETE
    @Path("/{household}")
    public Response deleteMember(@PathParam("household")String household){
        EntityManager em = emf.createEntityManager();
        MemberJpa member = em.find(MemberJpa.class, household);
        if(member == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        em.getTransaction().begin();
        em.remove(member);
        em.getTransaction().commit();
        return Response.ok().build();
    }
}
