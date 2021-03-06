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
package org.lds.disasterlocator.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.GoogleGeoSimulator;
import org.lds.disasterlocator.jpa.District;
import org.lds.disasterlocator.jpa.Member;
import org.lds.disasterlocator.rest.json.DistanceMatrixResponse;
import org.lds.disasterlocator.rest.json.DistrictCreateRequestContainer;
import org.lds.disasterlocator.rest.json.Element;

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
    @Path("/create/{leader}")
    @Consumes(MediaType.WILDCARD)
    public District createNewDistrict(@PathParam("leader") String leaderHousehold) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<District> find = em.createQuery("select d from District d", District.class);
        int id = find.getResultList().size();
        Member leader = em.find(Member.class, leaderHousehold);
        District d = new District();
        d.setId(id);
        d.setLeader(leader);
        leader.setDistrict(id);
        em.persist(d);
        em.persist(leader);
        em.getTransaction().commit();
        return d;
    }

    @POST
    @Path("/create/{leader}/{assistant}")
    @Consumes(MediaType.WILDCARD)
    public Response createNewDistrict(@PathParam("leader") String leaderHousehold, @PathParam("assistant") String assistantHousehold) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<District> find = em.createQuery("select d from District d", District.class);
        int id = find.getMaxResults();
        Member leader = em.find(Member.class, leaderHousehold);
        Member assistant = em.find(Member.class, assistantHousehold);
        District d = new District();
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
            if (assist != null) {
                sb.append("\"").append(assist.getHousehold()).append("\"");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @POST
    @Path("create")
    public Response createDistricts(List<DistrictCreateRequestContainer> request) throws IOException, InterruptedException, Exception {
        MemberList memberList = new MemberList();
        List<Member> wardList = memberList.getWardList();
        // reset district
        for (Member member : wardList) {
            member.setDistrict(-1);
        }
        HashMap<Integer, DistanceMatrixResponse> distances = getDistanceMatrix(request);
        computeClosestLeader(wardList, distances);

        for (Member member : wardList) {
            memberList.updateMember(member);
        }
        // make sure leader and assistant are assigned
        List<District> district = getDistrict();
        for (District d : district) {
            if (d.getLeader() != null) {
                d.getLeader().setDistrict(d.getId());
            }
            if (d.getAssistant() != null) {
                d.getAssistant().setDistrict(d.getId());
            }
            storeDistrict(d);
        }

        return Response.ok().build();
    }

    @POST
    public Response storeDistrict(District district) {
        EntityManager em = emf.createEntityManager();
        District find = em.find(District.class, district.getId());
        if (find != null) {
            em.merge(district);
        } else {
            em.persist(district);
        }
        return Response.ok().build();
    }

    private DistanceMatrixResponse getDistanceRequest(StringBuilder sb) throws IllegalStateException, IOException {
        String data = GoogleGeoSimulator.getUrl(sb.toString());
        if (data == null) {
            DefaultHttpClient hc = new DefaultHttpClient();
            HttpGet post = new HttpGet(sb.toString());
            HttpResponse response = hc.execute(post);
            InputStream is = response.getEntity().getContent();
            data = IOUtils.toString(is);
            is.close();
            ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DistanceMatrixResponse dmr = mapper.readValue(IOUtils.toInputStream(data), DistanceMatrixResponse.class);
            if (dmr.getStatus().equals("OK")) {
                GoogleGeoSimulator.storeUrl(sb.toString(), data);
            }
        }
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DistanceMatrixResponse dmr = mapper.readValue(IOUtils.toInputStream(data), DistanceMatrixResponse.class);
        return dmr;
    }

    private HashMap<Integer, Integer> getDistanceToLeaders(HashMap<Integer, DistanceMatrixResponse> distances, Member member) {
        // find member in distances and find closest leader
        HashMap<Integer, Integer> distMap = new HashMap<>();
        for (Integer districtNumber : distances.keySet()) {
            DistanceMatrixResponse dmr = distances.get(districtNumber);
            List<String> destination_addresses = dmr.getDestination_addresses();
            for (int i = 0; i < destination_addresses.size(); i++) {
                String da = destination_addresses.get(i);
                String ma = member.getAddress();
                if (!da.isEmpty() && !ma.isEmpty() && da.startsWith(ma)) {
                    // found address save distance
                    Element element = dmr.getRows().get(0).getElements().get(i);
                    if (element.getStatus().equals("NOT_FOUND")) {
                        System.out.println("Missing distance found for " + da + " and " + ma);
                        continue;
                    }
                    int value = element.getDistance().getValue();
                    System.out.println("Found distance for " + ma + " to " + da + " of " + value);
                    distMap.put(districtNumber, value);
                }
            }
        }
        return distMap;
    }

    private int getClosestDistrict(HashMap<Integer, Integer> distMap) {
        Iterator<Integer> keyIterator = distMap.keySet().iterator();
        if (keyIterator.hasNext()) {
            int key = keyIterator.next();
            int closest = distMap.get(key);
            int district = key;
            while (keyIterator.hasNext()) {
                key = keyIterator.next();
                int value = distMap.get(key);
                if (value < closest) {
                    closest = value;
                    district = key;
                }
            }
            return district;
        } else {
            System.err.println("No elements in distMap " + distMap.keySet());
        }
        return -1;
    }

    private HashMap<Integer, DistanceMatrixResponse> getDistanceMatrix(List<DistrictCreateRequestContainer> request) throws IOException, IllegalStateException, InterruptedException {
        HashMap<Integer, DistanceMatrixResponse> distances = new HashMap<>();

        int count = 0;
        for (DistrictCreateRequestContainer districtCreateRequestContainer : request) {
            StringBuilder sb = new StringBuilder("http://maps.googleapis.com/maps/api/distancematrix/json?origins=");
            sb.append(URLEncoder.encode(districtCreateRequestContainer.getLeader().getAddress(), "UTF-8")).append(",");
            sb.append("&destinations=");
            for (Member member : districtCreateRequestContainer.getMembers()) {
                sb.append(URLEncoder.encode(member.getAddress(), "UTF-8")).append(",");
                sb.append(URLEncoder.encode("|", "UTF-8"));
            }
            sb.append("&mode=walking&sensor=false");
            DistanceMatrixResponse dmr = getDistanceRequest(sb);
            if (dmr.getStatus().equals("OK")) {
                distances.put(districtCreateRequestContainer.getDistrictId(), dmr);
            } else {
                // to many requests per second, wait 10 seconds and retry
                while (dmr.getStatus().equals("OK") == false) {
                    System.out.println("To many requests, pausing " + dmr.getStatus() + ": Request " + count);
                    Thread.sleep(10000);
                    dmr = getDistanceRequest(sb);
                }
                distances.put(districtCreateRequestContainer.getLeader().getDistrict(), dmr);
            }
            count++;
        }
        return distances;
    }

    private void computeClosestLeader(List<Member> wardList, HashMap<Integer, DistanceMatrixResponse> distances) {
        // for each member compute closest leader
        for (Member member : wardList) {
            if (member.getDistrict() != -1) {
                continue;
            }
            HashMap<Integer, Integer> distMap = getDistanceToLeaders(distances, member);
            // dump out distance map
            System.out.println("Distance map for " + member.getHousehold());
            for (Integer integer : distMap.keySet()) {
                System.out.println("\t" + integer + ":" + distMap.get(integer));
            }
            int district = getClosestDistrict(distMap);
            if (district != -1) {
                System.out.println("Setting " + member.getHousehold() + " to district " + district);
            }
            member.setDistrict(district);
        }
    }

    public static void main(String[] args) throws IOException {
        String dist = "{\"id\":0, \"leader\":{\"household\":\"Aguilar, Jennifer\",\"address\":\"1242 Barbara Drive, Vista, CA 92084, USA\",\"city\":null,\"zip\":null,\"email\":\"\",\"lat\":\"33.18710\",\"lng\":\"-117.2176550\",\"phone\":\"760-724-9179\",\"district\":-1}"
                + ",\"assistant\":{\"household\":\"Alexander, Samantha Ann Turbyville\",\"address\":\"1371 Barbara Drive, Vista, CA 92084, USA\",\"city\":null,\"zip\":null,\"email\":\"\",\"lat\":\"33.184610\",\"lng\":\"-117.2179820\",\"phone\":\"\",\"district\":-1}}";
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        District dmr = mapper.readValue(IOUtils.toInputStream(dist), District.class);
        System.out.println(dmr.getId());
        System.out.println(dmr.getLeader().getHousehold());
        System.out.println(dmr.getAssistant().getHousehold());
    }
}
