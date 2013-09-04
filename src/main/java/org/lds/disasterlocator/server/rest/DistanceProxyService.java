/**
 * Copyright (C) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lds.disasterlocator.server.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.ws.rs.core.Response.Status;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.lds.disasterlocator.server.rest.jpa.DistanceJpa;
import org.lds.disasterlocator.server.rest.jpa.DistrictJpa;
import org.lds.disasterlocator.server.rest.jpa.MemberJpa;
import org.lds.disasterlocator.server.rest.json.DistanceMatrixResponse;
import org.lds.disasterlocator.server.rest.json.DistrictMatrixRequest;
import org.lds.disasterlocator.server.rest.json.Element;
import org.lds.disasterlocator.server.rest.json.LatLng;
import org.lds.disasterlocator.server.rest.json.Row;
import org.lds.disasterlocator.shared.Member;

/**
 *
 * @author Bert Summers
 */
@Path("distance")
@Consumes({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
public class DistanceProxyService {

    private static final Logger logger = Logger.getLogger(DistanceProxyService.class.getName());
    private final EntityManagerFactory emf;

    public DistanceProxyService() {
        emf = EntityManagerFactoryHelper.createEntityManagerFactory();
    }

    /**
     *
     * @param leaderLat
     * @param leaderLng
     * @param memberLat
     * @param memberLng
     * @return
     */
    @GET
    @Path("/{leaderlat}/{leaderlng}/{memberlat}/{memberlng}")
    public Response getDistance(@PathParam("leaderlat") double leaderLat,
            @PathParam("leaderlat") double leaderLng,
            @PathParam("memberlat") double memberLat,
            @PathParam("memberlat") double memberLng) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<DistanceJpa> query = em.createNamedQuery("Distance.find", DistanceJpa.class);
        query.setParameter("leaderLat", leaderLat);
        query.setParameter("leaderLng", leaderLng);
        query.setParameter("memberLat", memberLat);
        query.setParameter("memberLng", memberLng);
        List<DistanceJpa> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        DistanceJpa distance = resultList.get(0);
        em.close();
        return Response.ok().entity(new Integer(distance.getDistance())).build();
    }

    @POST
    public Response getDistanceMatrix(DistrictMatrixRequest dmr) {
        // add origin
        double leaderLat = dmr.getOrigins().get(0).getJb();
        double leaderLng = dmr.getOrigins().get(0).getKb();
        StringBuilder sb = new StringBuilder("http://maps.googleapis.com/maps/api/distancematrix/json?");
        sb.append("origins=").append(leaderLat).append(",").append(leaderLng);
        // add destinations
        sb.append("&destinations=");
        int count = 1;
        for (int i = 0; i < dmr.getDestinations().size() + 1; i++) {
            if (count % 10 != 0 && i < dmr.getDestinations().size()) {
                LatLng latLng = dmr.getDestinations().get(i);
                double memberLat = latLng.getJb();
                double memberLng = latLng.getKb();
                // check if we already have destination
                Response distance = getDistance(leaderLat, leaderLng, memberLat, memberLng);
                if (distance.getStatus() == 404) {
                    count++;
                    sb.append(memberLat).append(",").append(memberLng);
                    sb.append("|");
                }
            } else {
                if (count > 1) {
                    count = 1;
                    // make request, then reset sb and start again
                    // delete the trailing pipe character
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("&sensor=false&mode=walking&units=metric");
                    // request url
                    DistanceMatrixResponse response = runQuery(sb.toString());

                    if (response.getStatus().equals("OK")) {
                        logger.log(Level.INFO, "Successful query {0}", sb.toString());
                        List<Row> rows = response.getRows();
                        Row row = rows.get(0);
                        List<Element> elements = row.getElements();
                        List<LatLng> destinations = dmr.getDestinations();
                        for (int j = 0; j < elements.size(); j++) {
                            LatLng latLng = destinations.get(j);
                            // save address in db
                            Element element = elements.get(j);
                            saveDistance(leaderLat, leaderLng, latLng.getJb(), latLng.getKb(), element.getDistance().getValue());
                        }
                    } else if ("OVER_QUERY_LIMIT".equals(response.getStatus())) {
                        // need to deal with pause and run again
                        logger.info(("Over query limit, sleeping 10 seconds"));
                        try {
                            Thread.sleep(10000);
                            while ("OVER_QUERY_LIMIT".equals(response.getStatus())) {
                                response = runQuery(sb.toString());
                                logger.log(Level.INFO, "Received {0}", response.getStatus());
                            }
                            if(!response.getStatus().equals("OK")){
                                logger.severe("Received response " + response.getStatus() + " for query "+ sb.toString());
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DistanceProxyService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        // note only request 10 at a time
                        // check status to see if delay required
                        // add address to db
                        String[] args = {response.getStatus(), sb.toString()};
                        logger.log(Level.SEVERE, "Received bad response {0} for query {1}", args);
                        return Response.serverError().build();
                    }
                    sb = new StringBuilder("http://maps.googleapis.com/maps/api/distancematrix/json?");
                    sb.append("origins=").append(leaderLat).append(",").append(leaderLng);
                    // add destinations
                    sb.append("&destinations=");
                }
            }
        }
        return Response.ok().build();
    }

    private DistanceMatrixResponse runQuery(String s) {
        try {
            URL url = new URL(s);
            try (InputStream is = url.openStream()) {
                // convert back to DistanceMatrixResponse
                ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                DistanceMatrixResponse response = mapper.readValue(is, DistanceMatrixResponse.class);
                return response;
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Bad URL for distance matrix", ex);
            return null;
        }
    }

    @GET
    @Path("assignmembers")
    @Produces(MediaType.TEXT_HTML)
    public Response computeMembers() {
        // query distance table
        // select each member and order distance by closest leader
        // need to load leaders
        // need to load members, ingnore members who are leaders on non-auto
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<MemberJpa> query = em.createNamedQuery("Member.all", MemberJpa.class);
        List<MemberJpa> memberList = query.getResultList();
        TypedQuery<DistrictJpa> distanceQuery = em.createNamedQuery("District.all", DistrictJpa.class);
        List<DistrictJpa> districtList = distanceQuery.getResultList();
        List<Double> toLats = new ArrayList<>();
        List<Double> toLngs = new ArrayList<>();
        for (DistrictJpa districtJpa : districtList) {
            Member leader = districtJpa.getLeader();
            if (leader != null) {
                toLats.add(leader.getLat());
                toLngs.add(leader.getLng());
            }
        }
        StringBuilder sb = new StringBuilder("<html><body><table><tr><th>From</th><th>Address</th><th>To</th><th>Distance</th></tr>");
        for (MemberJpa memberJpa : memberList) {
            TypedQuery<DistanceJpa> distQuery = em.createNamedQuery("Distance.byClosest", DistanceJpa.class);
            distQuery.setParameter("memberLat", memberJpa.getLat());
            distQuery.setParameter("memberLng", memberJpa.getLng());
            // add all leaders lat/lng
            distQuery.setParameter("leaderLatList", toLats);
            distQuery.setParameter("leaderLngList", toLngs);
            List<DistanceJpa> list = distQuery.getResultList();
            for (DistanceJpa distanceJpa : list) {
                sb.append("<tr><td>");
                sb.append(memberJpa.getHousehold());
                sb.append("</td><td>");
                sb.append(memberJpa.getAddress());
                sb.append("</td><td>");
                sb.append(getLeaderNameAt(distanceJpa, districtList));
                sb.append("</td><td>").append(distanceJpa.getDistance());
                sb.append("</td></tr>");
            }
            if (list.size() > 0) {
                // assign member to first district found
                DistanceJpa dist = list.get(0);
                memberJpa.setDistrict(getDistrictId(dist, districtList));
                em.persist(memberJpa);
            }
        }
        sb.append("</table></body></html>");
        em.getTransaction().commit();
        em.close();
        return Response.ok().entity(sb.toString()).build();
    }

    private void saveDistance(double fromLat, double fromLng, double toLat, double toLng, int value) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DistanceJpa dist = new DistanceJpa();
        dist.setLeaderLng(fromLng);
        dist.setLeaderLat(fromLat);
        dist.setMemberLat(toLat);
        dist.setMemberLng(toLng);
        dist.setDistance(value);
        em.persist(dist);
        em.getTransaction().commit();
    }

    private String getLeaderNameAt(DistanceJpa distanceJpa, List<DistrictJpa> list) {
        String name = null;
        for (DistrictJpa d : list) {
            Member leader = d.getLeader();
            if (leader == null) {
                continue;
            }
            if (leader.getLat() == distanceJpa.getLeaderLat() && leader.getLng() == distanceJpa.getLeaderLng()) {
                name = d.getLeader().getHousehold();
                break;
            }
        }
        return name;
    }

    private int getDistrictId(DistanceJpa distanceJpa, List<DistrictJpa> list) {
        int id = 0;
        for (DistrictJpa d : list) {
            Member leader = d.getLeader();
            if (leader == null) {
                continue;
            }
            if (leader.getLat() == distanceJpa.getLeaderLat() && leader.getLng() == distanceJpa.getLeaderLng()) {
                id = d.getId();
                break;
            }
        }
        return id;

    }
}
