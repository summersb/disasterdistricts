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
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        for (DistanceJpa distanceJpa : list) {
            em.remove(distanceJpa);
        }
        TypedQuery<DistrictJpa> districtQuery = em.createQuery("select d from District d", DistrictJpa.class);
        List<DistrictJpa> districtList = districtQuery.getResultList();
        for (DistrictJpa districtJpa : districtList) {
            em.remove(districtJpa);
        }
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
        em.getTransaction().begin();
        MemberJpa member = new MemberJpa();
        member.setHousehold("fred");
        member.setAddress("fred street");
        member.setLat(2.0);
        member.setLng(2.5);
        em.persist(member);
        em.getTransaction().commit();
        DistanceMatrixResponse distMatrix = new DistanceMatrixResponse();
        distMatrix.getOrigin_addresses().add("bob street");
        distMatrix.getDestination_addresses().add(member.getAddress());
        Row row = createDMRRows("fred street", 100, "1 km", 1);
        distMatrix.getRows().add(row);
        String url = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=1.0,1.5&destinations=2.0,2.5&sensor=false&mode=walking&units=metric";
        distanceProxyService.urlProvider = new MyURLProvider(url, distMatrix.toString());

        DistrictMatrixRequest dmr = new DistrictMatrixRequest();
        dmr.getOrigins().add(new LatLng(1.0, 1.5));
        dmr.getDestinations().add(member.getLocation());
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        // query distance, should be 1
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(1, list.size());
        DistanceJpa distance = list.get(0);
        assertEquals(1, distance.getDistance());
        assertEquals(1.0, distance.getLeaderLat(), 0.0);
        assertEquals(1.5, distance.getLeaderLng(), 0.0);
        assertEquals(2.0, distance.getMemberLat(), 0.0);
        assertEquals(2.5, distance.getMemberLng(), 0.0);
    }

    @Test
    public void testGet11DistanceMatrix() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < 11; i++) {
            MemberJpa member = new MemberJpa();
            member.setHousehold(i + ":member");
            member.setAddress(i + ":address");
            member.setLat(2.0+i);
            member.setLng(2.5+i);
            em.persist(member);
        }
        em.getTransaction().commit();

        DistanceMatrixResponse distMatrix = new DistanceMatrixResponse();
        distMatrix.getOrigin_addresses().add("bob street");
        for (int i = 0; i < 10; i++) {
            distMatrix.getDestination_addresses().add(i + ":address");
            Row row = createDMRRows(i+":long time", 100*i, i+ " km", i);
            distMatrix.getRows().add(row);
        }

        DistanceMatrixResponse distMatrix2 = new DistanceMatrixResponse();
        distMatrix2.getOrigin_addresses().add("bob street");
        for (int i = 10; i < 11; i++) {
            distMatrix2.getDestination_addresses().add(i + ":address");
            Row row = createDMRRows(i+":long time", 100*i, i+ " km", i);
            distMatrix2.getRows().add(row);
        }
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=1.0,1.5&destinations=2.0,2.5|3.0,3.5|4.0,4.5|5.0,5.5|6.0,6.5|7.0,7.5|8.0,8.5|9.0,9.5|10.0,10.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=1.0,1.5&destinations=12.0,12.5&sensor=false&mode=walking&units=metric"};
        String[] responses = {distMatrix.toString(), distMatrix2.toString()};
        distanceProxyService.urlProvider = new MyURLProvider(urls, responses);
        DistrictMatrixRequest dmr = new DistrictMatrixRequest();
        dmr.getOrigins().add(new LatLng(1.0, 1.5));
        for (int i = 0; i < 11; i++) {
            dmr.getDestinations().add(new LatLng(2.0+i, 2.5+i));
        }
        Response response = distanceProxyService.getDistanceMatrix(dmr);
        assertEquals(200, response.getStatus());
        // query distance, should be 11
        TypedQuery<DistanceJpa> query = em.createQuery("select d from Distance d", DistanceJpa.class);
        List<DistanceJpa> list = query.getResultList();
        assertEquals(11, list.size());

    }

    @Test
    public void testComputeMembers() {
        // create 20 members
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < 20; i++) {
            MemberJpa member = new MemberJpa();
            member.setHousehold(i + ":member");
            member.setAddress(i + ":address");
            member.setLat(2.0+i);
            member.setLng(2.5+i);
            em.persist(member);
        }
        MemberJpa member = new MemberJpa();
        member.setHousehold("Leader");
        member.setAddress("Address");
        member.setLat(0.0);
        member.setLng(0.5);
        member.setDistrict(1);
        em.persist(member);
        // create district and assign 1 leader
        DistrictJpa district = new DistrictJpa();
        district.setId(1);
        district.setLeader(member);
        em.persist(district);
        em.getTransaction().commit();

        // run getDistanceMatrix for 9 to leader
        DistanceMatrixResponse distMatrix = new DistanceMatrixResponse();
        distMatrix.getOrigin_addresses().add("Address");
        for (int i = 0; i < 9; i++) {
            distMatrix.getDestination_addresses().add(i + ":address");
            Row row = createDMRRows(i+":long time", 100*i, i+ " km", i);
            distMatrix.getRows().add(row);
        }
        DistanceMatrixResponse distMatrix2 = new DistanceMatrixResponse();
        distMatrix2.getOrigin_addresses().add("Address");
        for (int i = 9; i < 19; i++) {
            distMatrix2.getDestination_addresses().add(i + ":address");
            Row row = createDMRRows(i+":long time", 100*i, i+ " km", i);
            distMatrix2.getRows().add(row);
        }
        DistanceMatrixResponse distMatrix3 = new DistanceMatrixResponse();
        distMatrix3.getOrigin_addresses().add("Address");
        for (int i = 19; i < 20; i++) {
            distMatrix3.getDestination_addresses().add(i + ":address");
            Row row = createDMRRows(i+":long time", 100*i, i+ " km", i);
            distMatrix3.getRows().add(row);
        }
        distMatrix3.getDestination_addresses().add("Address");
        distMatrix3.getRows().add(createDMRRows("0 mins", 0, "0 m", 0));
        String [] urls = {
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=2.0,2.5|3.0,3.5|4.0,4.5|5.0,5.5|6.0,6.5|7.0,7.5|8.0,8.5|9.0,9.5|10.0,10.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=11.0,11.5|12.0,12.5|13.0,13.5|14.0,14.5|15.0,15.5|16.0,16.5|17.0,17.5|18.0,18.5|19.0,19.5&sensor=false&mode=walking&units=metric",
         "http://maps.googleapis.com/maps/api/distancematrix/json?origins=0.0,0.5&destinations=21.0,21.5|0.0,0.5&sensor=false&mode=walking&units=metric"};

        String[] responses = {distMatrix.toString(), distMatrix2.toString(), distMatrix3.toString()};
        distanceProxyService.urlProvider = new MyURLProvider(urls, responses);
        DistrictMatrixRequest dmr = new DistrictMatrixRequest();
        dmr.getOrigins().add(new LatLng(0.0, 0.5));
        for (int i = 0; i < 9; i++) {
            dmr.getDestinations().add(new LatLng(2.0+i, 2.5+i));
        }
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

    private Row createDMRRows(String durationText, int durationValue, String distanceText, int distanceValue) {
        Row row = new Row();
        Element element = new Element();
        row.getElements().add(element);
        Distance d = new Distance();
        d.setValue(distanceValue);
        d.setText(distanceText);
        element.setDistance(d);
        element.setStatus("OK");
        Duration duration = new Duration();
        duration.setText(durationText);
        duration.setValue(durationValue);
        element.setDuration(duration);
        return row;
    }
}