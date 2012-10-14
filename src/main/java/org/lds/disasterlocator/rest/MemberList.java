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

import org.lds.disasterlocator.jpa.Member;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.lds.disasterlocator.jpa.District;

/**
 *
 * @author Bert W Summers
 */
@Path("/member")
@Consumes({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
public class MemberList {

    private final static Logger logger = Logger.getLogger(MemberList.class.getName());
    private final EntityManagerFactory emf;

    public MemberList() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    @GET
    @Path("/list")
    public List<Member> getWardList() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Member> q = em.createQuery("select m from Member m order by m.district", Member.class);
        List<Member> resultList = q.getResultList();
        for (Member member : resultList) {
            em.detach(member);
        }
        return resultList;
    }

    @GET
    @Path("csv")
    @Produces(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public String getWardListAsCsv() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<District> distQuery = em.createQuery("select m from District m", District.class);
        HashMap<Integer, District> district = new HashMap<Integer, District>();
        List<District> distList = distQuery.getResultList();
        for (District d : distList) {
            district.put(d.getId(), d);
        }
        TypedQuery<Member> memberQuery = em.createQuery("select m from Member m order by m.district DESC", Member.class);
        List<Member> resultList = memberQuery.getResultList();
        StringBuilder sb = new StringBuilder("district,household,address,city,zip,phone,email,leader,assistant\n");
        for (Member member : resultList) {
            sb.append(member.getDistrict());
            sb.append(",\"");
            sb.append(member.getHousehold());
            sb.append("\",");
            sb.append("\"").append(member.getAddress()).append("\"  ,");
            sb.append(member.getCity()).append(",");
            sb.append(member.getZip()).append(",");
            sb.append(member.getPhone()).append(",");
            sb.append(member.getEmail()).append(",");
            sb.append("\"").append(district.get(member.getDistrict()).getLeader().getHousehold()).append("\",");
            Member assist = district.get(member.getDistrict()).getAssistant();
            if (assist != null) {
                sb.append("\"").append(assist.getHousehold()).append("\"");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @POST
    public String makeLeader(Member member) throws MalformedURLException {
        EntityManager em = emf.createEntityManager();
        System.out.println(member);
        em.getTransaction().begin();
        Member find = em.find(Member.class, member.getHousehold());
        if (find != null) {
            em.merge(member);
        } else {
            em.persist(member);
        }
        em.getTransaction().commit();
        return "OK";
    }
}
