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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lds.disasterlocator.server.rest.jpa.DistanceJpa;
import org.lds.disasterlocator.server.rest.json.DistrictMatrixRequest;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import org.lds.disasterlocator.server.rest.json.Distance;
import org.lds.disasterlocator.server.rest.json.DistanceMatrixResponse;
import org.lds.disasterlocator.server.rest.json.Duration;
import org.lds.disasterlocator.server.rest.json.Element;
import org.lds.disasterlocator.server.rest.json.LatLng;
import org.lds.disasterlocator.server.rest.json.Row;

/**
 *
 * @author Bert Summers
 */
public class DistanceProxyServiceTest {

    private DistanceProxyService distanceProxyService;
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("disaster-test");

    class MyURLProvider implements DistanceProxyService.URLProvider {

        private final Logger logger = Logger.getLogger(MyURLProvider.class.getName());
        private List<String> data = new ArrayList<>();
        private List<String> url = new ArrayList<>();
        private String requestUrl;

        public MyURLProvider(String url, String data) {
            this.data.add(data);
            this.url.add(url);
        }

        public MyURLProvider(String[] urls, String[] responses){
            data.addAll(Arrays.asList(responses));
            url.addAll(Arrays.asList(urls));
        }

        @Override
        public void setURL(String url) {
            this.requestUrl = url;
        }

        @Override
        public InputStream openStream() throws IOException {
            for (int i = 0; i < url.size(); i++) {
                if(requestUrl.equals(url.get(i))){
                    return IOUtils.toInputStream(data.get(i));
                }
            }
            logger.log(Level.WARNING, "Failed to find url {0} ", requestUrl);
            return null;
        }
    }

    @Before
    public void setUp() {
        distanceProxyService = new DistanceProxyService(emf);
    }

    @After
    public void tearDown() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNamedQuery("Distance.deleteAll").executeUpdate();
        em.createNamedQuery("District.deleteAll").executeUpdate();
        TypedQuery<MemberJpa> memberQuery = em.createQuery("select m from Member m", MemberJpa.class);
        List<MemberJpa> memberList = memberQuery.getResultList();
        for (MemberJpa memberJpa : memberList) {
            em.remove(memberJpa);
        }
        em.getTransaction().commit();
        em.close();
    }

    @Test(expected = RollbackException.class)
    public void testUniqueViolation() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DistanceJpa dist = new DistanceJpa();
        dist.setDistance(1);
        dist.setLeaderLat(1.0);
        dist.setLeaderLng(1.5);
        dist.setMemberLat(2.0);
        dist.setMemberLng(2.5);
        DistanceJpa dist2 = new DistanceJpa();
        dist2.setDistance(1);
        dist2.setLeaderLat(1.0);
        dist2.setLeaderLng(1.5);
        dist2.setMemberLat(2.0);
        dist2.setMemberLng(2.5);
        em.persist(dist);
        em.persist(dist2);
        em.getTransaction().commit();
    }

    @Test
    public void testGetDistance() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DistanceJpa dist = new DistanceJpa();
        dist.setDistance(1);
        dist.setLeaderLat(1.0);
        dist.setLeaderLng(1.5);
        dist.setMemberLat(2.0);
        dist.setMemberLng(2.5);
        em.persist(dist);
        em.getTransaction().commit();
        Response response = distanceProxyService.getDistance(1.0, 1.5, 2.0, 2.5);
        assertEquals(200, response.getStatus());
        Integer i = (Integer) response.getEntity();
        assertEquals(1, i.intValue());

        response = distanceProxyService.getDistance(2.0, 2.0, 1.0, 1.0);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGetDistanceMatrix() {
        EntityManager em = emf.createEntityManager();
        MemberJpa leader = createLeader(em);
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(0, 1, em);
        String url = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=1.0,1.5&sensor=false&mode=walking&units=metric";
        distanceProxyService.urlProvider = new MyURLProvider(url, distMatrix.toString());
        DistrictMatrixRequest dmr = createDistanceMatrixRequest(0, 1, leader);
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        // query distance, should be 1
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        for (DistanceJpa distanceJpa : list) {
            System.out.println(distanceJpa);
        }
        assertEquals(1, list.size());
        DistanceJpa distance = list.get(0);
        assertEquals(0, distance.getDistance());
        assertEquals(0.0, distance.getLeaderLat(), 0.0);
        assertEquals(0.5, distance.getLeaderLng(), 0.0);
        assertEquals(1.0, distance.getMemberLat(), 0.0);
        assertEquals(1.5, distance.getMemberLng(), 0.0);
    }

    @Test
    public void testGet10DistanceMatrix() {
        EntityManager em = emf.createEntityManager();
        MemberJpa leader = createLeader(em);
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(0, 10, em);
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=1.0,1.5|2.0,2.5|3.0,3.5|4.0,4.5|5.0,5.5|6.0,6.5|7.0,7.5|8.0,8.5|9.0,9.5|10.0,10.5&sensor=false&mode=walking&units=metric"};
        String[] responses = {distMatrix.toString()};
        distanceProxyService.urlProvider = new MyURLProvider(urls, responses);
        DistrictMatrixRequest dmr = createDistanceMatrixRequest(0, 10, leader);
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        // query distance, should be 11
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(10, list.size());
    }

    @Test
    public void testGet11DistanceMatrix() {
        EntityManager em = emf.createEntityManager();
        MemberJpa leader = createLeader(em);
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(0, 10, em);
        DistanceMatrixResponse distMatrix2 = createDistanceMatrixResponse(10, 11, em);
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=1.0,1.5|2.0,2.5|3.0,3.5|4.0,4.5|5.0,5.5|6.0,6.5|7.0,7.5|8.0,8.5|9.0,9.5|10.0,10.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=11.0,11.5&sensor=false&mode=walking&units=metric"};
        String[] responses = {distMatrix.toString(), distMatrix2.toString()};
        distanceProxyService.urlProvider = new MyURLProvider(urls, responses);
        DistrictMatrixRequest dmr = createDistanceMatrixRequest(0, 11, leader);
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        // query distance, should be 11
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(11, list.size());
    }

    @Ignore
    @Test
    public void testComputeMembers() {
        // create 20 members
        EntityManager em = emf.createEntityManager();
        MemberJpa leader = createLeader(em);
        // run getDistanceMatrix for 9 to leader
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(0, 9, em);
        // matrix for remaining members
        DistanceMatrixResponse distMatrix2 = createDistanceMatrixResponse(9, 18, em);
        distMatrix2.getRows().get(0).getElements().add(0, createDMRElement("0 mins", 0, "0 m", 0));
        distMatrix2.getDestination_addresses().add(0, leader.getAddress());
        DistanceMatrixResponse distMatrix3 = createDistanceMatrixResponse(18, 20, em);
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=1.0,1.5|2.0,2.5|3.0,3.5|4.0,4.5|5.0,5.5|6.0,6.5|7.0,7.5|8.0,8.5|9.0,9.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=0.0,0.5|10.0,10.5|11.0,11.5|12.0,12.5|13.0,13.5|14.0,14.5|15.0,15.5|16.0,16.5|17.0,17.5|18.0,18.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=19.0,19.5|20.0,20.5&sensor=false&mode=walking&units=metric"};

        String[] responses = {distMatrix.toString(), distMatrix2.toString(), distMatrix3.toString()};
        distanceProxyService.urlProvider = new MyURLProvider(urls, responses);
        DistrictMatrixRequest dmr = createDistanceMatrixRequest(0, 9, leader);
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(9, list.size());
        // run computeMembers
        Response computeMembers = distanceProxyService.computeMembers();
        em.close();
        // em is caching members, need to restart to get new data
        em = emf.createEntityManager();
        // test all members have entry in distance table
        query = em.createQuery("select d from Distance d", DistanceJpa.class);
        list = query.getResultList();
        assertEquals(21, list.size());
        // test all members have district assigned
        TypedQuery<MemberJpa> memberQuery = em.createNamedQuery("Member.all", MemberJpa.class);
        List<MemberJpa> resultList = memberQuery.getResultList();
        for (MemberJpa memberJpa : resultList) {
            assertEquals(1, memberJpa.getDistrict());
        }
    }

    @Test
    public void testDuplicateLatLng(){
        // compute distance for address twice
        // two households at same address
        EntityManager em = emf.createEntityManager();
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(0, 1, em);
        // create leader
        MemberJpa leader = createLeader(em);
        em.getTransaction().begin();
        // make members have same address
        TypedQuery<MemberJpa> memberQuery = em.createNamedQuery("Member.all", MemberJpa.class);
        List<MemberJpa> resultList = memberQuery.getResultList();
        for (MemberJpa memberJpa : resultList) {
            if(memberJpa.getHousehold().equals("Leader")){
                continue;
            }
            memberJpa.setLat(1.0);
            memberJpa.setLng(1.5);
            em.persist(memberJpa);
        }
        em.getTransaction().commit();
        String [] urls = {
 "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=1.0,1.5&sensor=false&mode=walking&units=metric"};
        DistrictMatrixRequest dmr = createDistanceMatrixRequest(0, 2, leader);
        for (LatLng latLng : dmr.getDestinations()) {
            latLng.setJb(1.0);
            latLng.setKb(1.5);
        }
        distanceProxyService.urlProvider = new MyURLProvider(urls, new String[]{distMatrix.toString()});

        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(1, list.size());

        // test for same address in second request
        dmr = createDistanceMatrixRequest(0, 1, leader);
        response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        query = em.createQuery("select d from Distance d", DistanceJpa.class);
        list = query.getResultList();
        assertEquals(1, list.size());
    }

    @Test
    public void testMemberNotAuto(){
        EntityManager em = emf.createEntityManager();
        DistanceMatrixResponse distMatrix = createDistanceMatrixResponse(1, 2, em);
        // create leader
        MemberJpa leader = createLeader(em);
        distMatrix.getRows().get(0).getElements().add(createDMRElement("0 mins", 0, "0 m", 0));
        distMatrix.getDestination_addresses().add(leader.getAddress());
        em.getTransaction().begin();
        // make member non-auto
        MemberJpa member = new MemberJpa();
        member.setHousehold("0Member Name");
        member.setAddress("0:address");
        member.setLat(1.0);
        member.setLng(1.5);
        member.setAuto(false);
        em.persist(member);
        em.getTransaction().commit();
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=2.0,2.5|0.0,0.5&sensor=false&mode=walking&units=metric"};
        distanceProxyService.urlProvider = new MyURLProvider(urls, new String[]{distMatrix.toString()});
        Response response = distanceProxyService.computeMembers();
        assertEquals(200, response.getStatus());
        member = em.find(MemberJpa.class, "0Member Name");
        assertEquals(0, member.getDistrict());
    }

    private Element createDMRElement(String durationText, int durationValue, String distanceText, int distanceValue) {
        Element element = new Element();
        Distance d = new Distance();
        d.setValue(distanceValue);
        d.setText(distanceText);
        element.setDistance(d);
        element.setStatus("OK");
        Duration duration = new Duration();
        duration.setText(durationText);
        duration.setValue(durationValue);
        element.setDuration(duration);
        return element;
    }

    private MemberJpa createLeader(EntityManager em) {
        em.getTransaction().begin();
        MemberJpa leader = new MemberJpa();
        leader.setHousehold("Leader");
        leader.setAddress("Address");
        leader.setLat(0.0);
        leader.setLng(0.5);
        leader.setDistrict(1);
        // create district and assign 1 leader
        DistrictJpa district = new DistrictJpa();
        district.setId(1);
        district.setLeader(leader);
        em.persist(district);
        em.persist(leader);
        em.getTransaction().commit();
        return leader;
    }

    private DistanceMatrixResponse createDistanceMatrixResponse(int start, int stop, EntityManager em) {
        em.getTransaction().begin();
        DistanceMatrixResponse distMatrix = new DistanceMatrixResponse();
        distMatrix.getOrigin_addresses().add("Address");
        List<Row> rows = distMatrix.getRows();
        Row row = new Row();
        rows.add(row);
        for (int i = start; i < stop; i++) {
            MemberJpa member = new MemberJpa();
            member.setHousehold(i + "Member Name");
            member.setAddress(i+":address");
            member.setLat(1.0+i);
            member.setLng(1.5+i);
            em.persist(member);

            distMatrix.getDestination_addresses().add(i + ":address");
            row.getElements().add(createDMRElement(i+":long time", 100*i, i+ " km", i));
        }
        em.getTransaction().commit();
        return distMatrix;
    }

    private DistrictMatrixRequest createDistanceMatrixRequest(int start, int stop, MemberJpa leader) {
        DistrictMatrixRequest dmr = new DistrictMatrixRequest();
        dmr.getOrigins().add(leader.getLocation());
        for (int i = start; i < stop; i++) {
            dmr.getDestinations().add(new LatLng(1.0+i, 1.5+i));
        }
        return dmr;
    }
}