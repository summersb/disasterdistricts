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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.lds.disasterlocator.Csv2Json;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.server.rest.json.model.GeocodeResponse;
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
    @Path("csv")
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
    public Response updateMember(MemberJpa member) throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        MemberJpa find = em.find(MemberJpa.class, member.getHousehold());
        if (find != null) {
            if (!find.getAddress().equals(member.getAddress())) {
                // run google geo query to get full address
                GeocodeResponse geo = Csv2Json.getGeo(member.getAddress());
                member.setAddress(geo.getResults()[0].getFormattedAddress());
//                member.setCity(locatity.getLocalityName());
//                member.setZip(locatity.getPostalCode().getPostalCodeNumber());
            }
            // save to server
            em.merge(member);
        } else {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        em.getTransaction().commit();

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
}
